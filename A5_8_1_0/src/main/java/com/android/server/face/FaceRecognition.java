package com.android.server.face;

import android.content.Context;
import android.hardware.face.ClientMode;
import android.hardware.face.CommandResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.security.KeyStore;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.biometrics.BiometricsService;
import com.android.server.face.camera.CaptureManager;
import com.android.server.face.camera.CaptureManager.IPreviewCallback;
import com.android.server.face.dcs.DcsUtil;
import com.android.server.face.health.HealthState;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.android.server.face.utils.Utils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback.Stub;

public class FaceRecognition implements IPreviewCallback {
    /* renamed from: -android-hardware-face-ClientModeSwitchesValues */
    private static final /* synthetic */ int[] f245-android-hardware-face-ClientModeSwitchesValues = null;
    private static final int MAX_AUTHENTICATE_RETRY_TIMES = 5;
    private static final long MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED = 2000;
    private static final int MSG_UNLOCK_NEXT_REGOGNITION = 1;
    public static final String PROP_FACE_AUTO_VERIFY = "persist.oppo.android.face.auto.verify";
    private static final int REMAINING_FOR_SIMILAR_OR_HACKER_FACE = 9999;
    private static final String TAG = "FaceService.FaceRecoginition";
    private static final long TIMEOUT_FOR_NEXT_ALL = 0;
    private static final long TIMEOUT_FOR_NEXT_AUTHEN = 0;
    private static final int TIME_OUT_FOR_FACE_TOUCH_AE_ENABLE = 300;
    private static final int TIME_OUT_OF_FACE_FILTER = 100;
    private static Object sMutex = new Object();
    private static FaceRecognition sSingleInstance;
    private static long sTimeoutForNextEnroll = 150;
    private long mAuthPreviewStartTime = 0;
    private final CaptureManager mCaptureManager;
    private final Context mContext;
    private ClientMode mCurrentMode;
    private final IBiometricsFaceClientCallback mDaemonCallback = new Stub() {
        public void onEnrollResult(long deviceId, int faceId, int groupId, int remaining) {
            LogUtil.d(FaceRecognition.TAG, "onEnrollResult, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
            if (remaining != FaceRecognition.REMAINING_FOR_SIMILAR_OR_HACKER_FACE) {
                FaceRecognition.this.mIRecognitionCallback.onEnrollResult(deviceId, faceId, groupId, remaining);
                FaceRecognition.this.updateLastFailedNotifyTime();
            }
        }

        public void onAcquired(long deviceId, int acquiredInfo) {
            if (FaceRecognition.this.shouldControllNotifyFreq(acquiredInfo)) {
                FaceRecognition.this.handleFailedAcquiredInfo(deviceId, acquiredInfo);
            } else {
                FaceRecognition.this.mIRecognitionCallback.onAcquired(deviceId, acquiredInfo);
            }
        }

        public void onAuthenticated(long deviceId, int faceId, int groupId, int retryTimes, ArrayList<Byte> tokenByte) {
            if (faceId != 0) {
                byte[] byteToken = new byte[tokenByte.size()];
                for (int i = 0; i < tokenByte.size(); i++) {
                    byteToken[i] = ((Byte) tokenByte.get(i)).byteValue();
                }
                KeyStore.getInstance().addAuthToken(byteToken);
            }
            LogUtil.d(FaceRecognition.TAG, "onAuthenticated, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
            FaceRecognition.this.debugTimeConsumingIfNeed(true, faceId != 0);
            if (faceId != 0) {
                LogUtil.d(FaceRecognition.TAG, "wake up for sucess");
                FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS);
            } else if (FaceRecognition.this.hasReachToMaxRetryTimes(retryTimes)) {
                int failReason = FaceRecognition.this.getFailReason();
                if (failReason != 0) {
                    FaceRecognition.this.mIRecognitionCallback.onAcquired(-1, failReason);
                }
            } else {
                LogUtil.d(FaceRecognition.TAG, "onAuthenticated failed  retryTimes = " + retryTimes + ", just retry");
                if (retryTimes == 1) {
                    LogUtil.d(FaceRecognition.TAG, "wake up for fail");
                    FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL);
                    FaceRecognition.this.updateTouchAEParameter();
                }
                return;
            }
            FaceRecognition.this.mFilterFailedAcquiredInfo.clear();
            FaceRecognition.this.mIRecognitionCallback.onAuthenticated(deviceId, faceId, groupId);
            FaceRecognition.this.updateLastFailedNotifyTime();
        }

        public void onError(long deviceId, int error) {
            LogUtil.d(FaceRecognition.TAG, "onError, error = " + error);
            FaceRecognition.this.debugTimeConsumingIfNeed(true, false);
            FaceRecognition.this.mIRecognitionCallback.onError(deviceId, error);
        }

        public void onRemoved(long deviceId, int faceId, int groupId) {
            FaceRecognition.this.mIRecognitionCallback.onRemoved(deviceId, faceId, groupId);
        }

        public void onEnumerate(long deviceId, ArrayList<Integer> faceIdsArray, int groupId) {
            int size = faceIdsArray.size();
            int[] faceIds = new int[size];
            for (int i = 0; i < size; i++) {
                faceIds[i] = ((Integer) faceIdsArray.get(i)).intValue();
            }
            FaceRecognition.this.mIRecognitionCallback.onEnumerate(deviceId, faceIds, groupId);
        }

        public void onCommandResult(long lenth, ArrayList<Integer> keysArray, ArrayList<String> valuesArray) {
            int i;
            int size = keysArray.size();
            int[] keys = new int[size];
            for (i = 0; i < size; i++) {
                keys[i] = ((Integer) keysArray.get(i)).intValue();
            }
            size = valuesArray.size();
            String[] values = new String[size];
            for (i = 0; i < size; i++) {
                values[i] = (String) valuesArray.get(i);
            }
            FaceRecognition.this.mIRecognitionCallback.onCommandResult(new CommandResult(keys, values));
        }

        public void onFilterFailedAcquired(long devId, int acquiredInfo) {
            LogUtil.d(FaceRecognition.TAG, "onFilterFailedAcquired, acquiredInfo =  " + acquiredInfo);
            if (!(FaceRecognition.this.mAuthPreviewStartTime == 0 || SystemClock.uptimeMillis() - FaceRecognition.this.mAuthPreviewStartTime <= 100 || (FaceRecognition.this.mHasUnblockScreenOn ^ 1) == 0)) {
                FaceRecognition.this.mIRecognitionCallback.unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT);
                FaceRecognition.this.mHasUnblockScreenOn = true;
            }
            if (FaceRecognition.this.getRecognitionMode() == ClientMode.AUTHEN && FaceRecognition.this.mAuthPreviewStartTime != 0 && SystemClock.uptimeMillis() - FaceRecognition.this.mAuthPreviewStartTime > 300) {
                FaceRecognition.this.updateTouchAEParameter();
            }
            FaceRecognition.this.handleFailedAcquiredInfo(-1, acquiredInfo);
        }

        public void onFailReasonAcquired(long devId, int acquiredInfo) {
            LogUtil.d(FaceRecognition.TAG, "onFailReasonAcquired, acquiredInfo = " + acquiredInfo);
            int count = 1;
            Integer acquiredIntegerInfo = new Integer(acquiredInfo);
            if (FaceRecognition.this.mFilterFailedAcquiredInfo.containsKey(acquiredIntegerInfo)) {
                count = ((Integer) FaceRecognition.this.mFilterFailedAcquiredInfo.get(acquiredIntegerInfo)).intValue() + 1;
            }
            FaceRecognition.this.mFilterFailedAcquiredInfo.put(acquiredIntegerInfo, new Integer(count));
        }

        public void unlockRawImageDelivery(int clientMode) {
            FaceRecognition.this.unLockRecognitionForNext(FaceRecognition.this.getClientMode(clientMode));
        }

        public void onStaticsReport(int sexuality, int age) {
            DcsUtil.getDcsUtil(FaceRecognition.this.mContext).sendFaceInfo(sexuality, age);
        }
    };
    private final HashMap<Integer, Integer> mFilterFailedAcquiredInfo = new HashMap();
    private ExHandler mHandler;
    private boolean mHasUnblockScreenOn = false;
    private IRecognitionCallback mIRecognitionCallback;
    private long mImageBufferSeq;
    private long mLastRecognitionFailedNotifyTime;
    private final Looper mLooper;
    private long mNumbersOfFirstDropFrames = 4;
    private long mRawImageBufferSeq;
    private boolean mRecognitionLocked = false;
    private boolean mRecognitionRuning = false;
    private final ServiceThread mServiceThread;
    private long mStartCaptureTime;

    /* renamed from: -getandroid-hardware-face-ClientModeSwitchesValues */
    private static /* synthetic */ int[] m171-getandroid-hardware-face-ClientModeSwitchesValues() {
        if (f245-android-hardware-face-ClientModeSwitchesValues != null) {
            return f245-android-hardware-face-ClientModeSwitchesValues;
        }
        int[] iArr = new int[ClientMode.values().length];
        try {
            iArr[ClientMode.AUTHEN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ClientMode.ENGINEERING_INFO.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ClientMode.ENROLL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ClientMode.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ClientMode.REMOVE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ClientMode.UPDATE_FEATURE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f245-android-hardware-face-ClientModeSwitchesValues = iArr;
        return iArr;
    }

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
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new FaceRecognition(context, recognitionCallback);
            }
            sSingleInstance.updateCallback(recognitionCallback);
        }
        return sSingleInstance;
    }

    private void updateCallback(IRecognitionCallback recognitionCallback) {
        this.mIRecognitionCallback = recognitionCallback;
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        FaceRecognition.this.unlockRecognition();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public IBiometricsFaceClientCallback getFaceDaemonCallback() {
        return this.mDaemonCallback;
    }

    private void handleFailedAcquiredInfo(long deviceId, int acquiredInfo) {
        long currentTime = SystemClock.uptimeMillis();
        long delta = currentTime - this.mLastRecognitionFailedNotifyTime;
        LogUtil.d(TAG, "handleFailedAcquiredInfo, acquiredInfo = " + acquiredInfo);
        if (currentTime - this.mLastRecognitionFailedNotifyTime > MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED) {
            this.mIRecognitionCallback.onAcquired(deviceId, acquiredInfo);
            updateLastFailedNotifyTime();
        }
    }

    private int getFailReason() {
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

    private boolean shouldControllNotifyFreq(int acquiredInfo) {
        return acquiredInfo != 0;
    }

    private void updateLastFailedNotifyTime() {
        this.mLastRecognitionFailedNotifyTime = SystemClock.uptimeMillis();
    }

    public void onNewPreviewFrameComming(byte[] previewData, int previewWidth, int previewHeight, int degrees) {
        this.mRawImageBufferSeq++;
        if (isRegonitionRuning() && !isRecognitionLocked()) {
            LogUtil.d(TAG, "onNewPreviewFrameComming mRawImageBufferSeq = " + this.mRawImageBufferSeq);
            this.mImageBufferSeq++;
            if (1 == this.mImageBufferSeq) {
                updateLastFailedNotifyTime();
                this.mAuthPreviewStartTime = SystemClock.uptimeMillis();
                this.mHasUnblockScreenOn = false;
            }
            lockRecognition();
            this.mIRecognitionCallback.onFaceFilterSucceeded(this.mImageBufferSeq, previewData, getRecognitionMode());
        }
    }

    public void onError(int error) {
        this.mIRecognitionCallback.onError(-1, 8);
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

    public void onPreviewStarted() {
        this.mIRecognitionCallback.onPreviewStarted();
    }

    public void authenticate(long sessionId, int groupId, int type) {
        LogUtil.d(TAG, "authenticate, sessionId = " + sessionId + ", groupId = " + groupId + ", type = " + type);
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

    private void updateTouchAEParameter() {
        this.mCaptureManager.updateTouchAEParameter();
    }

    private void sendMessageWithArgsDelayed(int what, int arg1, long delayMillis) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg1, triggerTime, null), delayMillis);
            LogUtil.d(TAG, "sendMessageWithArgsDelayed what = " + what + ", arg2 = " + triggerTime);
        }
    }

    private boolean hasReachToMaxRetryTimes(int retryTimes) {
        LogUtil.d(TAG, "hasReachToMaxRetryTimes, retryTimes = " + retryTimes);
        return retryTimes >= 5;
    }

    private void unLockRecognitionForNext(ClientMode mode) {
        LogUtil.d(TAG, "unLockRecognitionForNext, mode = " + mode);
        sendMessageWithArgsDelayed(1, 0, getTimeoutForNextRecognition(mode));
    }

    private void unlockRecognition() {
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

    private ClientMode getClientMode(int mode) {
        switch (mode) {
            case 0:
                return ClientMode.NONE;
            case 1:
                return ClientMode.ENROLL;
            case 2:
                return ClientMode.AUTHEN;
            default:
                return ClientMode.NONE;
        }
    }

    private long getTimeoutForNextRecognition(ClientMode mode) {
        switch (m171-getandroid-hardware-face-ClientModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return sTimeoutForNextEnroll;
            case 3:
                return 0;
            default:
                return -1;
        }
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

    private void debugTimeConsumingIfNeed(boolean authen, boolean all) {
        if (FaceService.DEBUG_PERF && all && this.mStartCaptureTime > 0) {
            TimeUtils.calculateTime(TAG, "AllAuthenticated_" + this.mImageBufferSeq, SystemClock.uptimeMillis() - this.mStartCaptureTime);
            this.mStartCaptureTime = 0;
        }
    }
}
