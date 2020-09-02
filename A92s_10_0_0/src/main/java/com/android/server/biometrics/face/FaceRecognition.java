package com.android.server.biometrics.face;

import android.content.Context;
import android.hardware.face.ClientMode;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.face.camera.CaptureManager;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.TimeUtils;
import com.android.server.biometrics.face.utils.Utils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback;

public class FaceRecognition implements CaptureManager.IPreviewCallback {
    private static final int MAX_AUTHENTICATE_RETRY_TIMES = 5;
    private static final long MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED = 2000;
    private static final int MSG_TIME_OUT_OF_FACE_FILTER = 2;
    private static final int MSG_UNLOCK_NEXT_REGOGNITION = 1;
    private static final int NUM_OF_FRAMES = 3;
    public static final String PROP_FACE_AUTO_VERIFY = "persist.sys.android.face.auto.verify";
    private static final String PROP_FACE_FEEDBACK_REASON = "persist.sys.android.face.feedback.reason";
    private static final String PROP_FACE_PERFORMANCE_HIDE = "persist.sys.face.performance.hide";
    private static final int REMAINING_FOR_SIMILAR_OR_HACKER_FACE = 9999;
    private static final String TAG = "FaceService.FaceRecoginition";
    private static final long TIMEOUT_FOR_NEXT_ALL = 0;
    private static final long TIMEOUT_FOR_NEXT_AUTHEN = 0;
    private static final int TIME_OUT_FOR_FACE_TOUCH_AE_ENABLE = 300;
    private static final int TIME_OUT_OF_FACE_FILTER = 100;
    private static Object sMutex = new Object();
    private static FaceRecognition sSingleInstance;
    private static long sTimeoutForNextEnroll = 150;
    /* access modifiers changed from: private */
    public long mAuthPreviewStartTime = 0;
    private final CaptureManager mCaptureManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private ClientMode mCurrentMode;
    private final IBiometricsFaceClientCallback mDaemonCallback = new IBiometricsFaceClientCallback.Stub() {
        /* class com.android.server.biometrics.face.FaceRecognition.AnonymousClass2 */

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onEnrollResult(long deviceId, int faceId, int userId, int remaining) {
            LogUtil.d(FaceRecognition.TAG, "onEnrollResult, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
            if (remaining != FaceRecognition.REMAINING_FOR_SIMILAR_OR_HACKER_FACE) {
                FaceRecognition.this.mIRecognitionCallback.onEnrollResult(deviceId, faceId, userId, remaining);
                FaceRecognition.this.updateLastFailedNotifyTime();
            }
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onAcquired(long deviceId, int userId, int acquiredInfo, int vendorCode) {
            if (FaceRecognition.this.shouldControllNotifyFreq(acquiredInfo)) {
                FaceRecognition.this.handleFailedAcquiredInfo(deviceId, acquiredInfo);
            } else {
                FaceRecognition.this.mIRecognitionCallback.onAcquired(deviceId, userId, acquiredInfo, vendorCode);
            }
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onAuthenticated(long deviceId, int faceId, int userId, int retryTimes, ArrayList<Byte> token) {
            if (FaceRecognition.this.mHandler.hasMessages(2)) {
                FaceRecognition.this.mHandler.removeMessage(2);
                LogUtil.d(FaceRecognition.TAG, "onAuthenticated, removeMessage MSG_TIME_OUT_OF_FACE_FILTER");
            }
            Slog.d(FaceRecognition.TAG, "onAuthenticated, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
            FaceRecognition.this.debugTimeConsumingIfNeed(true, faceId != 0);
            if (faceId != 0) {
                LogUtil.d(FaceRecognition.TAG, "mIRecognitionCallback.onAuthenticated");
                boolean unused = FaceRecognition.this.mHasAuthenticated = true;
                FaceRecognition.this.mFilterFailedAcquiredInfo.clear();
                FaceRecognition.this.mIRecognitionCallback.onAuthenticated(deviceId, faceId, userId, token);
                LogUtil.d(FaceRecognition.TAG, "wake up for sucess");
                boolean unused2 = FaceRecognition.this.mHasUnblockScreenOn = true;
                FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS);
                FaceRecognition.this.updateLastFailedNotifyTime();
            } else if (!FaceRecognition.this.hasReachToMaxRetryTimes(retryTimes)) {
                LogUtil.d(FaceRecognition.TAG, "onAuthenticated failed  retryTimes = " + retryTimes + ", just retry");
                if (retryTimes == 1) {
                    LogUtil.d(FaceRecognition.TAG, "wake up for fail");
                    boolean unused3 = FaceRecognition.this.mHasUnblockScreenOn = true;
                    FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_FACE_AUTHENTICATE_FAIL);
                    FaceRecognition.this.updateTouchAEParameter();
                }
            } else {
                int failReason = FaceRecognition.this.getFailReason();
                if (failReason != 0) {
                    FaceRecognition.this.mIRecognitionCallback.onAcquired(-1, userId, failReason, 0);
                }
                String feedbackReason = SystemProperties.get(FaceRecognition.PROP_FACE_FEEDBACK_REASON, "off");
                if (feedbackReason.equals("dark") && FaceRecognition.this.mIRecognitionCallback.getFailedAttempts() >= 4) {
                    LogUtil.d(FaceRecognition.TAG, "test to send " + feedbackReason + " message to keyguard");
                    FaceRecognition.this.mIRecognitionCallback.onAcquired(-1, userId, 3, 0);
                }
                boolean unused4 = FaceRecognition.this.mHasAuthenticated = true;
                FaceRecognition.this.mFilterFailedAcquiredInfo.clear();
                FaceRecognition.this.mIRecognitionCallback.onAuthenticated(deviceId, faceId, userId, token);
                FaceRecognition.this.updateLastFailedNotifyTime();
            }
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onError(long deviceId, int userId, int error, int vendorCode) {
            LogUtil.d(FaceRecognition.TAG, "onError, error = " + error);
            if (FaceRecognition.this.mHandler.hasMessages(2)) {
                FaceRecognition.this.mHandler.removeMessage(2);
                LogUtil.d(FaceRecognition.TAG, "onError, removeMessage MSG_TIME_OUT_OF_FACE_FILTER");
            }
            FaceRecognition.this.debugTimeConsumingIfNeed(true, false);
            FaceRecognition.this.mIRecognitionCallback.onError(deviceId, userId, error, vendorCode);
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onRemoved(long deviceId, ArrayList<Integer> faceIds, int userId) {
            LogUtil.d(FaceRecognition.TAG, "mIRecognitionCallback.onRemoved");
            FaceRecognition.this.mIRecognitionCallback.onRemoved(deviceId, faceIds, userId);
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onEnumerate(long deviceId, ArrayList<Integer> faceIds, int userId) throws RemoteException {
            FaceRecognition.this.mIRecognitionCallback.onEnumerate(deviceId, faceIds, userId);
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onLockoutChanged(long duration) {
            LogUtil.d(FaceRecognition.TAG, "onLockoutChanged: " + duration);
            FaceRecognition.this.mIRecognitionCallback.onLockoutChanged(duration);
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onFilterFailedAcquired(long devId, int acquiredInfo) {
            LogUtil.d(FaceRecognition.TAG, "onFilterFailedAcquired, acquiredInfo =  " + acquiredInfo);
            if (1 == FaceRecognition.this.mImageBufferSeq && FaceRecognition.this.mAuthPreviewStartTime != 0) {
                long authTime = SystemClock.uptimeMillis() - FaceRecognition.this.mAuthPreviewStartTime;
                long unblockRemainingTime = authTime >= 100 ? 0 : 100 - authTime;
                LogUtil.d(FaceRecognition.TAG, "onFilterFailedAcquired, plan to unblock after " + unblockRemainingTime + " ms");
                FaceRecognition.this.sendMessageWithArgsDelayed(2, 0, unblockRemainingTime);
            }
            if (FaceRecognition.this.mImageBufferSeq >= 3 && !FaceRecognition.this.mHasUnblockScreenOn) {
                if (FaceRecognition.this.mHandler.hasMessages(2)) {
                    FaceRecognition.this.mHandler.removeMessage(2);
                    LogUtil.d(FaceRecognition.TAG, "onFilterFailedAcquired, ready to unblock");
                }
                FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT);
                boolean unused = FaceRecognition.this.mHasUnblockScreenOn = true;
            }
            if (FaceRecognition.this.getRecognitionMode() == ClientMode.AUTHEN && FaceRecognition.this.mAuthPreviewStartTime != 0 && SystemClock.uptimeMillis() - FaceRecognition.this.mAuthPreviewStartTime > 300) {
                FaceRecognition.this.updateTouchAEParameter();
            }
            FaceRecognition.this.handleFailedAcquiredInfo(-1, acquiredInfo);
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onFailReasonAcquired(long devId, int acquiredInfo) {
            LogUtil.d(FaceRecognition.TAG, "onFailReasonAcquired, acquiredInfo = " + acquiredInfo);
            int count = 1;
            Integer acquiredIntegerInfo = new Integer(acquiredInfo);
            if (FaceRecognition.this.mFilterFailedAcquiredInfo.containsKey(acquiredIntegerInfo)) {
                count = ((Integer) FaceRecognition.this.mFilterFailedAcquiredInfo.get(acquiredIntegerInfo)).intValue() + 1;
            }
            FaceRecognition.this.mFilterFailedAcquiredInfo.put(acquiredIntegerInfo, new Integer(count));
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void unlockRawImageDelivery(int clientMode) {
            FaceRecognition faceRecognition = FaceRecognition.this;
            faceRecognition.unLockRecognitionForNext(faceRecognition.getClientMode(clientMode));
        }

        @Override // vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onStaticsReport(int sexuality, int age) {
            DcsUtil.getDcsUtil(FaceRecognition.this.mContext).sendFaceInfo(sexuality, age);
        }
    };
    /* access modifiers changed from: private */
    public final HashMap<Integer, Integer> mFilterFailedAcquiredInfo = new HashMap<>();
    /* access modifiers changed from: private */
    public ExHandler mHandler;
    /* access modifiers changed from: private */
    public boolean mHasAuthenticated = false;
    /* access modifiers changed from: private */
    public boolean mHasUnblockScreenOn = false;
    /* access modifiers changed from: private */
    public IRecognitionCallback mIRecognitionCallback;
    /* access modifiers changed from: private */
    public long mImageBufferSeq;
    private long mLastRecognitionFailedNotifyTime;
    private final Looper mLooper;
    private long mNumbersOfFirstDropFrames = 4;
    private long mRawImageBufferSeq;
    private boolean mRecognitionLocked = false;
    private boolean mRecognitionRuning = false;
    private final ServiceThread mServiceThread;
    private long mStartCaptureTime;

    public FaceRecognition(Context context, IRecognitionCallback recognitionCallback) {
        this.mContext = context;
        if (Utils.isMtkPlatform()) {
            sTimeoutForNextEnroll = 0;
        }
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        this.mLooper = this.mServiceThread.getLooper();
        initHandler();
        this.mCaptureManager = CaptureManager.initCaptureManager(context, this);
        this.mImageBufferSeq = 0;
        this.mRawImageBufferSeq = 0;
        updateCallback(recognitionCallback);
    }

    public static FaceRecognition getInstance(Context context, IRecognitionCallback recognitionCallback) {
        FaceRecognition faceRecognition;
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new FaceRecognition(context, recognitionCallback);
            }
            sSingleInstance.updateCallback(recognitionCallback);
            faceRecognition = sSingleInstance;
        }
        return faceRecognition;
    }

    private void updateCallback(IRecognitionCallback recognitionCallback) {
        this.mIRecognitionCallback = recognitionCallback;
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.face.FaceRecognition.AnonymousClass1 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    FaceRecognition.this.unlockRecognition();
                } else if (i == 2) {
                    boolean unused = FaceRecognition.this.mHasUnblockScreenOn = true;
                    FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT);
                }
                super.handleMessage(msg);
            }
        };
    }

    public IBiometricsFaceClientCallback getFaceDaemonCallback() {
        return this.mDaemonCallback;
    }

    /* access modifiers changed from: private */
    public void handleFailedAcquiredInfo(long deviceId, int acquiredInfo) {
        long currentTime = SystemClock.uptimeMillis();
        long j = currentTime - this.mLastRecognitionFailedNotifyTime;
        LogUtil.d(TAG, "handleFailedAcquiredInfo, acquiredInfo = " + acquiredInfo);
        if (currentTime - this.mLastRecognitionFailedNotifyTime > MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED) {
            this.mIRecognitionCallback.onAcquired(deviceId, UserHandle.myUserId(), acquiredInfo, 0);
            updateLastFailedNotifyTime();
        }
    }

    /* access modifiers changed from: private */
    public int getFailReason() {
        int size = this.mFilterFailedAcquiredInfo.size();
        if (size <= 0) {
            return 0;
        }
        Object[] failReasons = this.mFilterFailedAcquiredInfo.keySet().toArray();
        Object[] failCount = this.mFilterFailedAcquiredInfo.values().toArray();
        int count = ((Integer) failCount[0]).intValue();
        int reason = ((Integer) failReasons[0]).intValue();
        for (int i = 0; i < size; i++) {
            int value = ((Integer) failCount[i]).intValue();
            if (value > count) {
                count = value;
                reason = ((Integer) failReasons[i]).intValue();
            }
        }
        LogUtil.d(TAG, "getFailReason, all reason num = " + size + ", reason = " + reason + ", count = " + count);
        return reason;
    }

    /* access modifiers changed from: private */
    public boolean shouldControllNotifyFreq(int acquiredInfo) {
        return acquiredInfo != 0;
    }

    /* access modifiers changed from: private */
    public void updateLastFailedNotifyTime() {
        this.mLastRecognitionFailedNotifyTime = SystemClock.uptimeMillis();
    }

    @Override // com.android.server.biometrics.face.camera.CaptureManager.IPreviewCallback
    public void onNewPreviewFrameComming(byte[] previewData, int previewWidth, int previewHeight, int degrees) {
        if (HealthState.VERIFYFACE.equals(SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""))) {
            LogUtil.d(TAG, "PROP_FACE_PERFORMANCE_HIDE = " + SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""));
            return;
        }
        this.mRawImageBufferSeq++;
        if (isRegonitionRuning() && !isRecognitionLocked()) {
            LogUtil.d(TAG, "onNewPreviewFrameComming mRawImageBufferSeq = " + this.mRawImageBufferSeq);
            this.mImageBufferSeq = this.mImageBufferSeq + 1;
            if (1 == this.mImageBufferSeq) {
                updateLastFailedNotifyTime();
                this.mAuthPreviewStartTime = SystemClock.uptimeMillis();
                this.mHasUnblockScreenOn = false;
                this.mHasAuthenticated = false;
            }
            if (this.mHasAuthenticated) {
                LogUtil.d(TAG, "mHasAuthenticated = " + this.mHasAuthenticated + ", ignore");
                return;
            }
            lockRecognition();
            this.mIRecognitionCallback.onFaceFilterSucceeded(this.mImageBufferSeq, previewData, getRecognitionMode());
        }
    }

    @Override // com.android.server.biometrics.face.camera.CaptureManager.IPreviewCallback
    public void onError(int error) {
        this.mIRecognitionCallback.onError(-1, UserHandle.myUserId(), 0, 0);
    }

    private void setRecognitionRunningLocked(boolean running) {
        synchronized (sMutex) {
            this.mRecognitionRuning = running;
            LogUtil.d(TAG, "setRecognitionRunningLocked mRecognitionRuning = " + this.mRecognitionRuning);
        }
        this.mFilterFailedAcquiredInfo.clear();
        this.mRawImageBufferSeq = 0;
        this.mImageBufferSeq = 0;
        if (!running) {
            this.mStartCaptureTime = 0;
        }
    }

    private boolean isRegonitionRuning() {
        boolean running;
        synchronized (sMutex) {
            running = this.mRecognitionRuning;
        }
        return running;
    }

    private void setRecognitionModeLocked(ClientMode mode) {
        synchronized (sMutex) {
            this.mCurrentMode = mode;
        }
    }

    public ClientMode getRecognitionMode() {
        ClientMode mode;
        synchronized (sMutex) {
            mode = this.mCurrentMode;
        }
        return mode;
    }

    @Override // com.android.server.biometrics.face.camera.CaptureManager.IPreviewCallback
    public void onPreviewStarted() {
        this.mIRecognitionCallback.onPreviewStarted();
    }

    public void authenticate(long operationId, int groupId) {
        LogUtil.d(TAG, "authenticate, operationId = " + operationId + ", groupId = " + groupId);
        this.mIRecognitionCallback.blockScreenOn();
        setRecognitionModeLocked(ClientMode.AUTHEN);
        startCapture(ClientMode.AUTHEN);
        setRecognitionRunningLocked(true);
        unLockRecognitionForNext(ClientMode.NONE);
    }

    public void enroll(int groupId, int timeout) {
        LogUtil.d(TAG, "enroll, groupId = " + groupId + ", timeout = " + timeout);
        setRecognitionModeLocked(ClientMode.ENROLL);
        startCapture(ClientMode.ENROLL);
        setRecognitionRunningLocked(true);
        unLockRecognitionForNext(ClientMode.NONE);
    }

    public void cancelRecognition() {
        LogUtil.d(TAG, HealthState.CANCEL_RECOGNITION);
        setRecognitionModeLocked(ClientMode.NONE);
        stopCapture();
    }

    public void stopPreview() {
        LogUtil.d(TAG, "stopPreview begin");
        setRecognitionModeLocked(ClientMode.NONE);
        setRecognitionRunningLocked(false);
        unLockRecognitionForNext(ClientMode.NONE);
        this.mCaptureManager.stopPreview();
        LogUtil.d(TAG, "stopPreview end");
    }

    public void setPreviewSurface(Surface surface) {
        LogUtil.d(TAG, "setPreviewSurface, surface = " + surface);
        this.mCaptureManager.setPreviewSurface(surface);
    }

    public int getPreViewWidth() {
        return this.mCaptureManager.getPreViewWidth();
    }

    public int getPreViewHeight() {
        return this.mCaptureManager.getPreViewHeight();
    }

    private void startCapture(ClientMode mode) {
        LogUtil.d(TAG, "startCapture");
        if (FaceService.DEBUG_PERF) {
            this.mStartCaptureTime = SystemClock.uptimeMillis();
        }
        this.mCaptureManager.startCapture(mode);
    }

    private void stopCapture() {
        LogUtil.d(TAG, "stopCapture");
        setRecognitionRunningLocked(false);
        unLockRecognitionForNext(ClientMode.NONE);
        this.mCaptureManager.stopCapture();
    }

    /* access modifiers changed from: private */
    public void updateTouchAEParameter() {
        this.mCaptureManager.updateTouchAEParameter();
    }

    /* access modifiers changed from: private */
    public void sendMessageWithArgsDelayed(int what, int arg1, long delayMillis) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg1, triggerTime, null), delayMillis);
            LogUtil.d(TAG, "sendMessageWithArgsDelayed what = " + what + ", arg2 = " + triggerTime);
        }
    }

    /* access modifiers changed from: private */
    public boolean hasReachToMaxRetryTimes(int retryTimes) {
        LogUtil.d(TAG, "hasReachToMaxRetryTimes, retryTimes = " + retryTimes);
        return retryTimes >= 5;
    }

    /* access modifiers changed from: private */
    public void unLockRecognitionForNext(ClientMode mode) {
        LogUtil.d(TAG, "unLockRecognitionForNext, mode = " + mode);
        sendMessageWithArgsDelayed(1, 0, getTimeoutForNextRecognition(mode));
    }

    /* access modifiers changed from: private */
    public void unlockRecognition() {
        LogUtil.d(TAG, "unlockRecognition");
        synchronized (sMutex) {
            this.mRecognitionLocked = false;
        }
    }

    private void lockRecognition() {
        LogUtil.d(TAG, "lockRecognition");
        synchronized (sMutex) {
            this.mRecognitionLocked = true;
        }
    }

    private boolean isRecognitionLocked() {
        boolean locked;
        synchronized (sMutex) {
            locked = this.mRecognitionLocked;
        }
        return locked;
    }

    /* access modifiers changed from: private */
    public ClientMode getClientMode(int mode) {
        if (mode == 0) {
            return ClientMode.NONE;
        }
        if (mode == 1) {
            return ClientMode.ENROLL;
        }
        if (mode != 2) {
            return ClientMode.NONE;
        }
        return ClientMode.AUTHEN;
    }

    /* renamed from: com.android.server.biometrics.face.FaceRecognition$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$hardware$face$ClientMode = new int[ClientMode.values().length];

        static {
            try {
                $SwitchMap$android$hardware$face$ClientMode[ClientMode.ENROLL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$hardware$face$ClientMode[ClientMode.AUTHEN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$hardware$face$ClientMode[ClientMode.NONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private long getTimeoutForNextRecognition(ClientMode mode) {
        int i = AnonymousClass3.$SwitchMap$android$hardware$face$ClientMode[mode.ordinal()];
        if (i != 1) {
            return (i == 2 || i == 3) ? 0 : -1;
        }
        return sTimeoutForNextEnroll;
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        pw.print(prefix);
        pw.println("FaceRecognition dump");
        pw.print(subPrefix);
        pw.println("mode = " + getRecognitionMode());
        pw.print(subPrefix);
        pw.println("running = " + isRegonitionRuning());
        pw.print(subPrefix);
        pw.println("imageBufferSeq = " + this.mImageBufferSeq);
        pw.print(subPrefix);
        pw.print(subPrefix);
        pw.println("autoVerify = " + SystemProperties.get(PROP_FACE_AUTO_VERIFY, "0"));
        pw.print(subPrefix);
        this.mCaptureManager.dump(pw, args, "");
    }

    public void updateEnrollTimeout(PrintWriter pw, String enrollTimeout) {
        try {
            sTimeoutForNextEnroll = (long) Integer.parseInt(enrollTimeout);
        } catch (NumberFormatException e) {
            LogUtil.d(TAG, "error happend " + e);
        }
        LogUtil.d(TAG, "updateEnrollTimeout , sTimeoutForNextEnroll = " + sTimeoutForNextEnroll);
    }

    public void updateCameraParameters(PrintWriter pw, String parameterArgs) {
        this.mCaptureManager.updateCameraParameters(pw, parameterArgs);
    }

    public void autoVerify(PrintWriter pw, String on) {
        SystemProperties.set(PROP_FACE_AUTO_VERIFY, on);
    }

    public void adjustDropFrames(PrintWriter pw, String frames) {
        if (frames != null) {
            try {
                this.mNumbersOfFirstDropFrames = (long) Integer.parseInt(frames);
            } catch (NumberFormatException e) {
                LogUtil.d(TAG, "adjustDropFrames error happend " + e);
            }
        }
        LogUtil.d(TAG, "adjustDropFrames mNumbersOfFirstDropFrames = " + this.mNumbersOfFirstDropFrames);
    }

    /* access modifiers changed from: private */
    public void debugTimeConsumingIfNeed(boolean authen, boolean all) {
        if (FaceService.DEBUG_PERF && all && this.mStartCaptureTime > 0) {
            TimeUtils.calculateTime(TAG, "AllAuthenticated_" + this.mImageBufferSeq, SystemClock.uptimeMillis() - this.mStartCaptureTime);
            this.mStartCaptureTime = 0;
        }
    }

    @Override // com.android.server.biometrics.face.camera.CaptureManager.IPreviewCallback
    public void unBlockScreenOnByCameraTimeout() {
        this.mHasUnblockScreenOn = true;
        this.mIRecognitionCallback.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_CAMERA_TIMEOUT);
    }

    public void removeCameraTimeoutMessage() {
        this.mCaptureManager.removeCameraTimeoutMessage();
    }
}
