package com.android.server.face;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.face.ClientMode;
import android.hardware.face.CommandResult;
import android.hardware.face.IFaceDaemonCallback;
import android.hardware.face.IFaceDaemonCallback.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.face.camera.CaptureManager;
import com.android.server.face.camera.CaptureManager.IPreviewCallback;
import com.android.server.face.sensetime.SenseTimeSdkManager;
import com.android.server.face.sensetime.SenseTimeSdkManager.IFaceFilterListener;
import com.android.server.face.test.oppo.util.ImageUtil;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.android.server.oppo.IElsaManager;
import com.sensetime.faceapi.model.FaceInfo;
import java.io.PrintWriter;
import java.util.HashMap;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class FaceRecognition implements IPreviewCallback, IFaceFilterListener {
    public static final String PROP_FACE_AUTO_VERIFY = "persist.face.auto.verify";
    private static final String TAG = "FaceService.FaceRecoginition";
    private static final String TAG_PREVIEW = "PreviewCallback";
    private static Object mMutex;
    private static FaceRecognition mSingleInstance;
    private final long MAX_AUTHENTICATE_RETRY_TIMES;
    private final long MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED;
    private long NUMBERS_OF_FIRST_DROP_FRAMES;
    protected int REMAINING_FOR_SIMILAR_OR_HACKER_FACE;
    private long mAuthPreviewStartTime;
    private final CaptureManager mCaptureManager;
    private final Context mContext;
    private long mCurrentAuthenticateRetryTimes;
    private ClientMode mCurrentMode;
    private final IFaceDaemonCallback mDaemonCallback;
    private final HashMap<Integer, Integer> mFilterFailedAcquiredInfo;
    private long mFilterSucceededFrameSendTime;
    private ExHandler mHandler;
    private IRecognitionCallback mIRecognitionCallback;
    private long mImageBufferSeq;
    private long mLastRecognitionFailedNotifyTime;
    private final Looper mLooper;
    private long mRawImageBufferSeq;
    private boolean mRecognitionRuning;
    private final SenseTimeSdkManager mSdkManager;
    private final ServiceThread mServiceThread;
    private long mStartCaptureTime;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.FaceRecognition.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.FaceRecognition.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.FaceRecognition.<clinit>():void");
    }

    public FaceRecognition(Context context, IRecognitionCallback recognitionCallback) {
        this.NUMBERS_OF_FIRST_DROP_FRAMES = 4;
        this.mRecognitionRuning = false;
        this.mAuthPreviewStartTime = 0;
        this.MIN_INTERNAL_OF_NOTIFY_RECOGNITION_FAILED = 2000;
        this.MAX_AUTHENTICATE_RETRY_TIMES = 5;
        this.mCurrentAuthenticateRetryTimes = 5;
        this.mFilterFailedAcquiredInfo = new HashMap();
        this.REMAINING_FOR_SIMILAR_OR_HACKER_FACE = 9999;
        this.mDaemonCallback = new Stub() {
            public void onEnrollResult(long deviceId, int faceId, int groupId, int remaining) {
                LogUtil.d(FaceRecognition.TAG, "onEnrollResult, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
                FaceRecognition.this.mSdkManager.resetTrack();
                FaceRecognition.this.mSdkManager.resetSelect();
                if (remaining != FaceRecognition.this.REMAINING_FOR_SIMILAR_OR_HACKER_FACE) {
                    FaceRecognition.this.mIRecognitionCallback.onEnrollResult(deviceId, faceId, groupId, remaining);
                    FaceRecognition.this.updateLastFailedNotifyTime();
                }
                if (remaining != 0) {
                    FaceRecognition.this.mSdkManager.unLockRecognitionForNext(1);
                }
            }

            public void onAcquired(long deviceId, int acquiredInfo) {
                if (FaceRecognition.this.shouldControllNotifyFreq(acquiredInfo)) {
                    FaceRecognition.this.handleFailedAcquiredInfo(-1, deviceId, acquiredInfo);
                } else {
                    FaceRecognition.this.mIRecognitionCallback.onAcquired(deviceId, acquiredInfo);
                }
            }

            public void onAuthenticated(long deviceId, int faceId, int groupId) {
                LogUtil.d(FaceRecognition.TAG, "onAuthenticated, faceId = " + faceId + ", mImageBufferSeq = " + FaceRecognition.this.mImageBufferSeq);
                FaceRecognition.this.debugTimeConsumingIfNeed(true, faceId != 0);
                if (faceId == 0) {
                    FaceRecognition.this.increaseRetryTimes();
                    if (FaceRecognition.this.mCurrentAuthenticateRetryTimes == 1) {
                        LogUtil.d(FaceRecognition.TAG, "wake up for fail");
                        FaceRecognition.this.mIRecognitionCallback.updateScreenOnBlockedState(false, true, 48);
                    }
                    if (FaceRecognition.this.hasReachToMaxRetryTimes()) {
                        int failReason = FaceRecognition.this.getFailReason();
                        if (failReason != 0) {
                            FaceRecognition.this.mIRecognitionCallback.onAcquired(-1, failReason);
                        }
                    } else {
                        FaceRecognition.this.mSdkManager.unLockRecognitionForNext(2);
                        LogUtil.d(FaceRecognition.TAG, "onAuthenticated failed  currentAuthenticateRetryTimes = " + FaceRecognition.this.getCurrentAuthenticateRetryTimes() + ",just retry");
                        return;
                    }
                }
                LogUtil.d(FaceRecognition.TAG, "wake up for sucess");
                FaceRecognition.this.mIRecognitionCallback.updateScreenOnBlockedState(false, false, 0);
                FaceRecognition.this.mFilterFailedAcquiredInfo.clear();
                FaceRecognition.this.mIRecognitionCallback.onAuthenticated(deviceId, faceId, groupId);
                FaceRecognition.this.updateLastFailedNotifyTime();
            }

            public void onError(long deviceId, int error) {
                LogUtil.d(FaceRecognition.TAG, "onEnrollResult, deviceId =  " + deviceId + ", error = " + error);
                FaceRecognition.this.debugTimeConsumingIfNeed(true, false);
                FaceRecognition.this.mIRecognitionCallback.onError(deviceId, error);
            }

            public void onRemoved(long deviceId, int faceId, int groupId) {
                FaceRecognition.this.mIRecognitionCallback.onRemoved(deviceId, faceId, groupId);
            }

            public void onEnumerate(long deviceId, int[] faceIds, int groupId) {
                FaceRecognition.this.mIRecognitionCallback.onEnumerate(deviceId, faceIds, groupId);
            }

            public void onCommandResult(CommandResult info) {
                FaceRecognition.this.mIRecognitionCallback.onCommandResult(info);
            }
        };
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        this.mLooper = this.mServiceThread.getLooper();
        initHandler();
        this.mSdkManager = SenseTimeSdkManager.initSenseTimeSdkManager(context, this);
        this.mCaptureManager = CaptureManager.initCaptureManager(context, this);
        this.mImageBufferSeq = 0;
        this.mRawImageBufferSeq = 0;
        updateCallback(recognitionCallback);
    }

    public static FaceRecognition getInstance(Context context, IRecognitionCallback recognitionCallback) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new FaceRecognition(context, recognitionCallback);
            }
            mSingleInstance.updateCallback(recognitionCallback);
        }
        return mSingleInstance;
    }

    private void updateCallback(IRecognitionCallback recognitionCallback) {
        this.mIRecognitionCallback = recognitionCallback;
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                LogUtil.d(FaceRecognition.TAG, "handleMessage, begin msg.what = " + FaceRecognition.this.msgToString(msg.what) + ", msg.arg2 = " + msg.arg2);
                int i = msg.what;
                LogUtil.d(FaceRecognition.TAG, "handleMessage, end msg.what = " + FaceRecognition.this.msgToString(msg.what) + ", msg.arg2 = " + msg.arg2);
                super.handleMessage(msg);
            }
        };
    }

    public IFaceDaemonCallback getFaceDaemonCallback() {
        return this.mDaemonCallback;
    }

    public void onFaceFilterFailed(long imageBufferSeq, int acquiredInfo) {
        if (this.mAuthPreviewStartTime != 0 && SystemClock.uptimeMillis() - this.mAuthPreviewStartTime > 200) {
            this.mIRecognitionCallback.updateScreenOnBlockedState(false, true, 48);
        }
        handleFailedAcquiredInfo(imageBufferSeq, -1, acquiredInfo);
    }

    private void handleFailedAcquiredInfo(long imageBufferSeq, long deviceId, int acquiredInfo) {
        long currentTime = SystemClock.uptimeMillis();
        long delta = currentTime - this.mLastRecognitionFailedNotifyTime;
        LogUtil.d(TAG, "handleFailedAcquiredInfo, imageBufferSeq = " + imageBufferSeq + ", acquiredInfo = " + acquiredInfo);
        if (currentTime - this.mLastRecognitionFailedNotifyTime > 2000) {
            this.mIRecognitionCallback.onAcquired(deviceId, acquiredInfo);
            updateLastFailedNotifyTime();
        }
    }

    public void onFailReasonAcquired(long imageBufferSeq, int acquiredInfo) {
        LogUtil.d(TAG, "onFailReasonAcquired, mCurrentAuthenticateRetryTimes = " + this.mCurrentAuthenticateRetryTimes + ", acquiredInfo = " + acquiredInfo);
        int count = 1;
        Integer acquiredIntegerInfo = new Integer(acquiredInfo);
        if (this.mFilterFailedAcquiredInfo.containsKey(acquiredIntegerInfo)) {
            count = ((Integer) this.mFilterFailedAcquiredInfo.get(acquiredIntegerInfo)).intValue() + 1;
        }
        this.mFilterFailedAcquiredInfo.put(acquiredIntegerInfo, new Integer(count));
    }

    private int getFailReason() {
        int size = this.mFilterFailedAcquiredInfo.size();
        LogUtil.d(TAG, "getFailReason, size = " + size);
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
        LogUtil.d(TAG, "getFailReason, count = " + count + ", reason = " + reason);
        return reason;
    }

    private boolean shouldControllNotifyFreq(int acquiredInfo) {
        switch (acquiredInfo) {
            case 104:
                return true;
            case 105:
                return true;
            default:
                return false;
        }
    }

    public void onFaceFilterSucceeded(long imageBufferSeq, int acquiredInfo, byte[] nv21ImageData, int nv21ImageDataWidth, int nv21ImageDataHeight, FaceInfo face) {
        LogUtil.d(TAG, "onFaceFilterSucceeded, imageBufferSeq = " + imageBufferSeq + ", acquiredInfo = " + acquiredInfo + ", mCurrentAuthenticateRetryTimes = " + this.mCurrentAuthenticateRetryTimes);
        this.mAuthPreviewStartTime = 0;
        if (FaceService.DEBUG_PERF && ClientMode.AUTHEN == getRecognitionMode()) {
            this.mFilterSucceededFrameSendTime = SystemClock.uptimeMillis();
        }
        this.mIRecognitionCallback.onFaceFilterSucceeded(nv21ImageData, nv21ImageDataWidth, nv21ImageDataHeight, face, getRecognitionMode());
    }

    private void updateLastFailedNotifyTime() {
        this.mLastRecognitionFailedNotifyTime = SystemClock.uptimeMillis();
    }

    public void onNewPreviewFrameComming(byte[] previewData, int previewWidth, int previewHeight, int degrees) {
        this.mRawImageBufferSeq++;
        if (!isRegonitionRuning()) {
            LogUtil.d(TAG_PREVIEW, "onNewPreviewFrameComming, mRegonitionRuning = false, return, mRawImageBufferSeq = " + this.mRawImageBufferSeq);
        } else if (this.mSdkManager.isRecognitionLocked()) {
            LogUtil.d(TAG_PREVIEW, "onNewPreviewFrameComming, isRecognitionLocked, return  mRawImageBufferSeq = " + this.mRawImageBufferSeq);
        } else {
            LogUtil.d(TAG_PREVIEW, "onNewPreviewFrameComming mRawImageBufferSeq = " + this.mRawImageBufferSeq);
            if (this.mRawImageBufferSeq < this.NUMBERS_OF_FIRST_DROP_FRAMES) {
                ImageUtil.getImageUtil().saveBytes(previewData, ClientMode.AUTHEN, -1, null);
            }
            this.mImageBufferSeq++;
            if (1 == this.mImageBufferSeq) {
                updateLastFailedNotifyTime();
                this.mAuthPreviewStartTime = SystemClock.uptimeMillis();
            }
            if (getRecognitionMode() == ClientMode.ENROLL) {
                this.mSdkManager.faceEnroll(this.mImageBufferSeq, previewData, previewWidth, previewHeight, degrees);
            } else if (getRecognitionMode() == ClientMode.AUTHEN) {
                this.mSdkManager.faceVerification(this.mImageBufferSeq, previewData, previewWidth, previewHeight, degrees);
            }
        }
    }

    public void onError(int error) {
        this.mIRecognitionCallback.onError(-1, 8);
    }

    private void setRecognitionRunningLocked(boolean running) {
        synchronized (mMutex) {
            this.mRecognitionRuning = running;
        }
        this.mFilterFailedAcquiredInfo.clear();
        this.mRawImageBufferSeq = 0;
        this.mImageBufferSeq = 0;
        this.mFilterSucceededFrameSendTime = 0;
        if (!running) {
            this.mStartCaptureTime = 0;
        }
    }

    private boolean isRegonitionRuning() {
        boolean running;
        synchronized (mMutex) {
            running = this.mRecognitionRuning;
        }
        return running;
    }

    private void setRecognitionModeLocked(ClientMode mode) {
        synchronized (mMutex) {
            this.mCurrentMode = mode;
            if (ClientMode.AUTHEN == mode) {
                resetRetryTimes();
            }
        }
    }

    public ClientMode getRecognitionMode() {
        ClientMode mode;
        synchronized (mMutex) {
            mode = this.mCurrentMode;
        }
        return mode;
    }

    public void onPreviewStarted() {
        this.mIRecognitionCallback.onPreviewStarted();
    }

    public void init() {
        this.mSdkManager.initLicense();
        this.mSdkManager.loadAlgorithmModel();
    }

    public void authenticate(long sessionId, int groupId, int type) {
        LogUtil.d(TAG, "authenticate, sessionId = " + sessionId + ", groupId = " + groupId + ", type = " + type);
        setRecognitionModeLocked(ClientMode.AUTHEN);
        startCapture();
        setRecognitionRunningLocked(true);
        this.mSdkManager.unLockRecognitionForNext(3);
    }

    public void enroll(int groupId, int timeout) {
        LogUtil.d(TAG, "enroll, groupId = " + groupId + ", timeout = " + timeout);
        setRecognitionModeLocked(ClientMode.ENROLL);
        startCapture();
        setRecognitionRunningLocked(true);
        this.mSdkManager.unLockRecognitionForNext(3);
    }

    public void cancelRecognition() {
        LogUtil.d(TAG, "cancelRecognition");
        setRecognitionModeLocked(ClientMode.NONE);
        stopCapture();
    }

    public void stopPreview() {
        LogUtil.d(TAG, "stopPreview begin");
        setRecognitionModeLocked(ClientMode.NONE);
        setRecognitionRunningLocked(false);
        this.mSdkManager.unLockRecognitionForNext(3);
        this.mCaptureManager.stopPreview();
        LogUtil.d(TAG, "stopPreview end");
    }

    public int preRecognition() {
        LogUtil.d(TAG, "preRecognition");
        startCapture();
        return 0;
    }

    public void setPreviewFrame(Rect rect) {
        LogUtil.d(TAG, "setPreviewFrame, rect = " + rect);
        this.mSdkManager.setPreviewCoordinate(rect);
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

    private void startCapture() {
        LogUtil.d(TAG, "startCapture begin");
        if (FaceService.DEBUG_PERF) {
            this.mStartCaptureTime = SystemClock.uptimeMillis();
        }
        this.mCaptureManager.startCapture();
        LogUtil.d(TAG, "startCapture end");
    }

    private void stopCapture() {
        LogUtil.d(TAG, "stopCapture begin");
        setRecognitionRunningLocked(false);
        this.mSdkManager.unLockRecognitionForNext(3);
        this.mCaptureManager.stopCapture();
        LogUtil.d(TAG, "stopCapture end");
    }

    public String msgToString(int msgCode) {
        return "unknown-code";
    }

    private void sendMessageWithArgs(int what, int arg1) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.obtainMessage(what, arg1, triggerTime, null).sendToTarget();
            LogUtil.d(TAG, "sendMessageWithArgs, what = " + msgToString(what) + ", arg2 = " + triggerTime);
        }
    }

    private void sendMessageWithArgsDelayed(int what, int arg1, long delayMillis) {
        if (!this.mHandler.hasMessages(what)) {
            int triggerTime = (int) SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg1, triggerTime, null), delayMillis);
            LogUtil.d(TAG, "sendMessageWithArgs, what = " + msgToString(what) + ", arg2 = " + triggerTime);
        }
    }

    private long getCurrentAuthenticateRetryTimes() {
        return this.mCurrentAuthenticateRetryTimes;
    }

    private void resetRetryTimes() {
        this.mCurrentAuthenticateRetryTimes = 0;
    }

    private void increaseRetryTimes() {
        this.mCurrentAuthenticateRetryTimes++;
    }

    private boolean hasReachToMaxRetryTimes() {
        LogUtil.d(TAG, "hasReachToMaxRetryTimes(), mCurrentAuthenticateRetryTimes = " + this.mCurrentAuthenticateRetryTimes);
        return this.mCurrentAuthenticateRetryTimes >= 5;
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
        pw.println("mCurrentAuthenticateRetryTimes = " + this.mCurrentAuthenticateRetryTimes);
        pw.print(subPrefix);
        pw.println("autoVerify = " + SystemProperties.get(PROP_FACE_AUTO_VERIFY, "0"));
        pw.print(subPrefix);
        this.mSdkManager.dump(pw, args, IElsaManager.EMPTY_PACKAGE);
        this.mCaptureManager.dump(pw, args, IElsaManager.EMPTY_PACKAGE);
    }

    private void updateTouchAEParameter() {
        this.mCaptureManager.updateTouchAEParameter();
    }

    public void updateCameraParameters(PrintWriter pw, String parameter_args) {
        this.mCaptureManager.updateCameraParameters(pw, parameter_args);
    }

    public void updateEnrollTimeout(PrintWriter pw, String enrollTimeout) {
        this.mSdkManager.updateEnrollTimeout(enrollTimeout);
    }

    public void autoVerify(PrintWriter pw, String on) {
        SystemProperties.set(PROP_FACE_AUTO_VERIFY, on);
    }

    public void adjustDropFrames(PrintWriter pw, String frames) {
        if (frames != null) {
            try {
                this.NUMBERS_OF_FIRST_DROP_FRAMES = (long) Integer.parseInt(frames);
            } catch (NumberFormatException e) {
                LogUtil.d(TAG, "adjustDropFrames error happend " + e);
            }
        }
        LogUtil.d(TAG, "adjustDropFrames NUMBERS_OF_FIRST_DROP_FRAMES = " + this.NUMBERS_OF_FIRST_DROP_FRAMES);
    }

    private void debugTimeConsumingIfNeed(boolean authen, boolean all) {
        if (FaceService.DEBUG_PERF) {
            if (authen && this.mFilterSucceededFrameSendTime > 0) {
                TimeUtils.calculateTime(TAG, "SendFrameAuthenticated", SystemClock.uptimeMillis() - this.mFilterSucceededFrameSendTime);
                this.mFilterSucceededFrameSendTime = 0;
            }
            if (all && this.mStartCaptureTime > 0) {
                TimeUtils.calculateTime(TAG, "AllAuthenticated_" + this.mImageBufferSeq, SystemClock.uptimeMillis() - this.mStartCaptureTime);
                this.mStartCaptureTime = 0;
            }
        }
    }
}
