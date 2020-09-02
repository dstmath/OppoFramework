package com.android.server.biometrics.face.camera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.face.ClientMode;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.biometrics.face.FaceHypnus;
import com.android.server.biometrics.face.FaceService;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.face.sensor.SensorManagerClient;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.TimeUtils;
import com.android.server.display.color.DisplayTransformManager;
import java.io.PrintWriter;

public class CaptureManager {
    private static String COLSECAMERALIGHT = "oppo.intent.action.stop.PINHOLE";
    private static final int MAX_OPEN_CAMERA_ATTEMPTS = 5;
    private static final int MSG_CAMERA_TIMEOUT = 5;
    private static final int MSG_CAMERA_WORKING_TIMEOUT = 8;
    private static final int MSG_CLOSE_CAMERA = 4;
    private static final int MSG_HANDLERTHREAD_TIMEOUT = 7;
    private static final int MSG_NEW_PREVIEW_FRAME_COMMING = 6;
    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_PREVIEW = 2;
    private static final int MSG_STOP_PREVIEW = 3;
    private static String OPENCAMERALIGHT = "oppo.intent.action.start.PINHOLE";
    private static final String PROP_FACE_PERFORMANCE_HIDE = "persist.sys.face.performance.hide";
    private static final String TAG = "FaceService.CaptureManager";
    public static final long TIMEOUT_CAMERA_PREVIEW_FRAME_CHECK = 1000;
    public static final long TIMEOUT_CAMERA_WORKING_THREAD = 30000;
    private static Object sMutex = new Object();
    private static Object sPendingMutex = new Object();
    private static CaptureManager sSingleInstance;
    public final int HANDLE_NODE_BEGIN_CLOSE_CAMERA = 4;
    public final int HANDLE_NODE_BEGIN_OPEN_CAMERA = 1;
    public final int HANDLE_NODE_BEGIN_START_PREVIEW = 2;
    public final int HANDLE_NODE_BEGIN_STOP_PREVIEW = 3;
    public final int HANDLE_NODE_FINISH_CLOSE_CAMERA = 8;
    public final int HANDLE_NODE_FINISH_OPEN_CAMERA = 5;
    public final int HANDLE_NODE_FINISH_START_PREVIEW = 6;
    public final int HANDLE_NODE_FINISH_STOP_PREVIEW = 7;
    public final int QUEUE_NODE_CLOSE_CAMERA = 4;
    public final int QUEUE_NODE_OPEN_CAMERA = 1;
    public final int QUEUE_NODE_START_PREVIEW = 2;
    public final int QUEUE_NODE_STOP_PREVIEW = 3;
    /* access modifiers changed from: private */
    public final CameraView mCameraView;
    private final Context mContext;
    /* access modifiers changed from: private */
    public byte[] mData = null;
    private Camera.FaceDetectionListener mFaceListener = new Camera.FaceDetectionListener() {
        /* class com.android.server.biometrics.face.camera.CaptureManager.AnonymousClass1 */

        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces != null && faces.length > 0) {
                LogUtil.d(CaptureManager.TAG, "facecount = " + faces.length + ", rect = " + faces[0].rect.toString());
            }
        }
    };
    private final HandlerThread mFaceServiceUnblockThread;
    private int mFramecount = 0;
    private int mHandlePendingNode = 8;
    private ExHandler mHandler;
    /* access modifiers changed from: private */
    public IPreviewCallback mIPreviewCallback;
    public volatile boolean mIsFirstFrameComming = false;
    public volatile boolean mIsNv21DataReady = false;
    public volatile boolean mIsTracking = false;
    private final Looper mLooper;
    /* access modifiers changed from: private */
    public ExHandler mNewPreviewFrameHandler;
    private final HandlerThread mNewPreviewFrameThread;
    public byte[] mNv21 = null;
    private int mOpenCameraAttempts = 0;
    private final Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        /* class com.android.server.biometrics.face.camera.CaptureManager.AnonymousClass2 */

        public void onPreviewFrame(byte[] data, Camera camera) {
            if (CaptureManager.this.mCameraView != null) {
                CaptureManager.this.mCameraView.addCallbackBuffer();
            }
            if (FaceService.DEBUG_PERF && !CaptureManager.this.mIsFirstFrameComming) {
                TimeUtils.calculateTime(CaptureManager.TAG, "FirstFrameComming", SystemClock.uptimeMillis() - CaptureManager.this.mStartPriviewEndTime);
                CaptureManager.this.mIsFirstFrameComming = true;
            }
            byte[] unused = CaptureManager.this.mData = data;
            CaptureManager.this.mNewPreviewFrameHandler.obtainMessage(6, 0, 0, null).sendToTarget();
        }
    };
    private volatile int mQueuePendingNode = 4;
    private final SensorManagerClient mSensorManagerClient;
    private final HandlerThread mServiceThread;
    /* access modifiers changed from: private */
    public long mStartPriviewEndTime;
    /* access modifiers changed from: private */
    public ExHandler mSubHandler;
    private Surface mSurface = null;

    public interface IPreviewCallback {
        void onError(int i);

        void onNewPreviewFrameComming(byte[] bArr, int i, int i2, int i3);

        void onPreviewStarted();

        void unBlockScreenOnByCameraTimeout();
    }

    public static CaptureManager initCaptureManager(Context c, IPreviewCallback callback) {
        return getCaptureManager(c, callback);
    }

    public static CaptureManager getCaptureManager(Context c, IPreviewCallback callback) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new CaptureManager(c, callback);
            }
            sSingleInstance.updateCallback(callback);
        }
        return sSingleInstance;
    }

    private void updateCallback(IPreviewCallback callback) {
        this.mIPreviewCallback = callback;
    }

    public static CaptureManager getCaptureManager() {
        return sSingleInstance;
    }

    public CaptureManager(Context context, IPreviewCallback callback) {
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mFaceServiceUnblockThread = new ServiceThread("FaceService.CaptureManagerSub", -2, true);
        this.mNewPreviewFrameThread = new ServiceThread("FaceService.CaptureManagerFrame", -2, true);
        this.mServiceThread.start();
        this.mFaceServiceUnblockThread.start();
        this.mNewPreviewFrameThread.start();
        this.mLooper = this.mServiceThread.getLooper();
        initHandler();
        this.mCameraView = new CameraView(context, new CameraErrorCallback());
        this.mSensorManagerClient = SensorManagerClient.getInstance(context);
        updateCallback(callback);
    }

    public class CameraErrorCallback implements Camera.ErrorCallback {
        public CameraErrorCallback() {
        }

        public void onError(int error, Camera camera) {
            LogUtil.d(CaptureManager.TAG, "onError , error = " + error + ", isCameraOpened = " + CaptureManager.this.mCameraView.isCameraOpened());
            if ((2 == error || 100 == error) && CaptureManager.this.mCameraView.isCameraOpened()) {
                CaptureManager.this.mIPreviewCallback.onError(0);
            }
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.face.camera.CaptureManager.AnonymousClass3 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                LogUtil.d(CaptureManager.TAG, "Enter handleMessage()");
                CaptureManager captureManager = CaptureManager.this;
                captureManager.dumpCameraState(captureManager.msgToString(msg.what));
                CaptureManager.this.dumpCameraMessageQueue(msg.what);
                LogUtil.d(CaptureManager.TAG, "msg.what = " + msg.what);
                int i = msg.what;
                if (i == 1) {
                    CaptureManager.this.handleOpenCamera();
                    LogUtil.d(CaptureManager.TAG, "handleOpenCamera()");
                } else if (i == 2) {
                    CaptureManager.this.handleStartPreview();
                    LogUtil.d(CaptureManager.TAG, "handleStartPreview()");
                } else if (i == 3) {
                    CaptureManager.this.handleStopPreview();
                    LogUtil.d(CaptureManager.TAG, "handleStopPreview()");
                } else if (i == 4) {
                    if (CaptureManager.this.mSubHandler.hasMessages(7)) {
                        CaptureManager.this.mSubHandler.removeMessage(7);
                        LogUtil.d(CaptureManager.TAG, "remove MSG_HANDLERTHREAD_TIMEOUT");
                    }
                    CaptureManager.this.removeAllCameraMessagesWithCondition();
                    CaptureManager.this.handleCloseCamera();
                    LogUtil.d(CaptureManager.TAG, "handleCloseCamera()");
                }
                super.handleMessage(msg);
            }
        };
        this.mSubHandler = new ExHandler(this.mFaceServiceUnblockThread.getLooper()) {
            /* class com.android.server.biometrics.face.camera.CaptureManager.AnonymousClass4 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                CaptureManager captureManager = CaptureManager.this;
                captureManager.dumpCameraState(captureManager.msgToString(msg.what));
                CaptureManager.this.dumpCameraMessageQueue(msg.what);
                int i = msg.what;
                if (i == 5) {
                    CaptureManager.this.handleCameraTimeout();
                } else if (i == 7) {
                    LogUtil.d(CaptureManager.TAG, "MSG_HANDLERTHREAD_TIMEOUT so need clear mHandler isCameraOpen:" + CaptureManager.this.mCameraView.isCameraOpened());
                    CaptureManager.this.removeAllCameraMessagesWithCondition();
                    if (CaptureManager.this.mCameraView.isCameraOpened()) {
                        CaptureManager.this.handleCloseCamera();
                    }
                } else if (i == 8) {
                    CaptureManager.this.handleCameraWorkingTimeout();
                }
                super.handleMessage(msg);
            }
        };
        this.mNewPreviewFrameHandler = new ExHandler(this.mNewPreviewFrameThread.getLooper()) {
            /* class com.android.server.biometrics.face.camera.CaptureManager.AnonymousClass5 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                CaptureManager captureManager = CaptureManager.this;
                captureManager.dumpCameraState(captureManager.msgToString(msg.what));
                CaptureManager.this.dumpCameraMessageQueue(msg.what);
                if (msg.what == 6) {
                    CaptureManager.this.mIPreviewCallback.onNewPreviewFrameComming(CaptureManager.this.mData, CaptureManager.this.getPreViewWidth(), CaptureManager.this.getPreViewHeight(), CaptureManager.this.getDegrees());
                }
                super.handleMessage(msg);
            }
        };
    }

    public void removeAllCameraMessagesWithCondition() {
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mQueuePendingNode == 2 || FaceService.mCanRemoveCameraMessageFromQueue) {
            this.mHandler.obtainMessage(1, 0, 0, null).sendToTarget();
            this.mHandler.obtainMessage(2, 0, 0, null).sendToTarget();
            if (FaceService.mCanDumpCameraMessageQueue) {
                LogUtil.d(TAG, "removeAllCameraMessagesWithCondition, add MSG_OPEN_CAMERA & MSG_START_PREVIEW back");
            }
        }
    }

    public boolean isCameraClosed() {
        return !this.mCameraView.isCameraOpened();
    }

    public int getPreViewWidth() {
        return this.mCameraView.getPreviewWidth();
    }

    public int getPreViewHeight() {
        return this.mCameraView.getPreviewHeight();
    }

    public int getDegrees() {
        return this.mCameraView.getDegrees();
    }

    public void startCapture(ClientMode mode) {
        if (mode == ClientMode.AUTHEN) {
            this.mSubHandler.sendMessageDelayed(this.mSubHandler.obtainMessage(5), 1000);
            this.mSubHandler.sendMessageDelayed(this.mSubHandler.obtainMessage(8), 30000);
        }
        synchronized (sPendingMutex) {
            this.mCameraView.setCurrentMode(mode);
            if ("camera".equals(SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""))) {
                LogUtil.d(TAG, "PROP_FACE_PERFORMANCE_HIDE = " + SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""));
                this.mHandler.post(new Runnable(SystemClock.uptimeMillis(), new byte[460800]) {
                    /* class com.android.server.biometrics.face.camera.$$Lambda$CaptureManager$RXu6mnxp3AmhS4GTUiHkg5Ljxys */
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ byte[] f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r4;
                    }

                    public final void run() {
                        CaptureManager.this.lambda$startCapture$0$CaptureManager(this.f$1, this.f$2);
                    }
                });
                LogUtil.d(TAG, "face without camera end");
                return;
            }
            openCameraLocked();
            startPreviewLocked();
        }
    }

    public /* synthetic */ void lambda$startCapture$0$CaptureManager(long startTime, byte[] data) {
        while (SystemClock.uptimeMillis() - startTime < 5000) {
            this.mPreviewCallback.onPreviewFrame(data, null);
        }
    }

    public void stopCapture() {
        synchronized (sPendingMutex) {
            stopPreviewLocked();
            closeCameraLocked();
        }
    }

    public void stopPreview() {
        synchronized (sPendingMutex) {
            stopPreviewLocked();
        }
    }

    public int setPreviewSurface(Surface surface) {
        return handleSetPreviewSurface(surface);
    }

    private int handleSetPreviewSurface(Surface surface) {
        this.mSurface = surface;
        dumpCameraState(HealthState.SET_PREVIEW_SURFACE);
        if (!this.mCameraView.isCameraOpened()) {
            return -1;
        }
        LogUtil.d(TAG, HealthState.SET_PREVIEW_SURFACE);
        return this.mCameraView.setPreviewSurface(surface);
    }

    /* access modifiers changed from: private */
    public void handleOpenCamera() {
        LogUtil.d(TAG, "Enter handleOpenCamera()");
        this.mOpenCameraAttempts++;
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        LogUtil.d(TAG, "handleOpenCamera, mOpenCameraAttempts = " + this.mOpenCameraAttempts);
        setHandlePendingNode(1);
        this.mSensorManagerClient.register();
        int result = this.mCameraView.openCamera();
        LogUtil.d(TAG, "mCameraView.openCamera()");
        if (result != 0) {
            LogUtil.e(TAG, "openCamera Failed, result = " + result);
            int i = this.mOpenCameraAttempts;
            if (i < 5) {
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ExHandler exHandler = this.mHandler;
                exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(1, 0, 0, null));
            } else if (i >= 5) {
                this.mOpenCameraAttempts = 0;
                setHandlePendingNode(8);
                LogUtil.e(TAG, "handleOpenCamera, result = " + result);
                this.mIPreviewCallback.onError(0);
                removeCameraWorkingTimeoutMessage();
            }
        } else {
            this.mOpenCameraAttempts = 0;
            this.mCameraView.setFaceDetectionListener(this.mFaceListener);
            if (this.mCameraView.initCamera(this.mPreviewCallback, this.mSurface) == 0) {
                setHandlePendingNode(5);
                return;
            }
            this.mOpenCameraAttempts = 0;
            this.mIPreviewCallback.onError(0);
        }
    }

    /* access modifiers changed from: private */
    public void handleCloseCamera() {
        setHandlePendingNode(4);
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        this.mSensorManagerClient.unRegister();
        int result = this.mCameraView.releaseCamera();
        LogUtil.d(TAG, "handleCloseCamera, result = " + result);
        removeCameraWorkingTimeoutMessage();
        setHandlePendingNode(8);
    }

    /* access modifiers changed from: private */
    public void handleStartPreview() {
        setHandlePendingNode(2);
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        int result = this.mCameraView.startPreview();
        LogUtil.d(TAG, "mCameraView.startPreview");
        if (FaceService.DEBUG_PERF) {
            this.mStartPriviewEndTime = SystemClock.uptimeMillis();
            this.mIsFirstFrameComming = false;
        }
        if (result != 0) {
            this.mIPreviewCallback.onError(0);
            LogUtil.e(TAG, "handleStartPreview, result = " + result);
            return;
        }
        sendCameraLightBroadcast(OPENCAMERALIGHT);
        this.mIPreviewCallback.onPreviewStarted();
        setHandlePendingNode(6);
    }

    /* access modifiers changed from: private */
    public void handleStopPreview() {
        setHandlePendingNode(3);
        this.mFramecount = 0;
        FaceHypnus.getInstance().hypnusSpeedUp(DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION);
        int result = this.mCameraView.stopPreview();
        this.mSurface = null;
        setHandlePendingNode(7);
        if (result != 0) {
            LogUtil.e(TAG, "handleStopPreview, result = " + result);
            handleCloseCamera();
        }
    }

    /* access modifiers changed from: private */
    public void handleCameraTimeout() {
        LogUtil.e(TAG, "handleCameraTimeout timeout");
        this.mIPreviewCallback.unBlockScreenOnByCameraTimeout();
    }

    /* access modifiers changed from: private */
    public void handleCameraWorkingTimeout() {
        LogUtil.e(TAG, "handleCameraWorkingTimeout!");
        DcsUtil dcs = DcsUtil.getDcsUtil(this.mContext);
        if (dcs != null) {
            dcs.sendCameraWorkingTimeoutInfo(1);
        }
        this.mIPreviewCallback.unBlockScreenOnByCameraTimeout();
        if (this.mCameraView.isCameraOpened()) {
            handleCloseCamera();
        }
    }

    private void sendCameraLightBroadcast(String broadCastAction) {
        if (!"".equals(SystemProperties.get("ro.oppo.screenhole.positon", ""))) {
            LogUtil.e(TAG, "send broadcast " + broadCastAction);
            Intent intent = new Intent(broadCastAction);
            intent.setPackage("com.android.systemui");
            this.mContext.sendBroadcast(intent);
        }
    }

    private void sendMessageWithArgs(int operationType, int what, int arg1) {
        synchronized (sPendingMutex) {
            if (operationType != (this.mQueuePendingNode % 4) + 1) {
                LogUtil.e(TAG, "sendMessageWithArgs, mQueuePendingNode: " + this.mQueuePendingNode + "  ---> " + what);
                return;
            }
            setQueuePendingNode(operationType);
            if (!FaceService.mCanRemoveCameraMessageFromQueue || !(operationType == 1 || operationType == 2)) {
                this.mHandler.obtainMessage(what, arg1, 0, null).sendToTarget();
            } else {
                LogUtil.d(TAG, "test not to add MSG_OPEN_CAMERA and MSG_START_PREVIEW");
            }
        }
    }

    private void openCameraLocked() {
        synchronized (sPendingMutex) {
            if (this.mQueuePendingNode == 4) {
                LogUtil.d(TAG, "openCameraLocked");
                this.mOpenCameraAttempts = 0;
                testToAddManyCameraMessages();
                sendMessageWithArgs(1, 1, 0);
            } else {
                LogUtil.d(TAG, "camera has been opened ,skip this message");
            }
        }
    }

    private void startPreviewLocked() {
        synchronized (sPendingMutex) {
            if (this.mQueuePendingNode == 1) {
                LogUtil.d(TAG, "startPreviewLocked");
                sendMessageWithArgs(2, 2, 0);
            } else {
                LogUtil.d(TAG, "camera has stared preview ,skip this message");
            }
        }
    }

    private void stopPreviewLocked() {
        synchronized (sPendingMutex) {
            if (this.mQueuePendingNode == 2) {
                LogUtil.d(TAG, "stopPreviewLocked");
                sendMessageWithArgs(3, 3, 0);
            } else {
                LogUtil.d(TAG, "camera has stoped preview ,skip this message");
            }
        }
    }

    private void closeCameraLocked() {
        synchronized (sPendingMutex) {
            if (this.mQueuePendingNode == 3) {
                LogUtil.d(TAG, "closeCameraLocked");
                this.mSubHandler.sendEmptyMessageDelayed(7, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                sendMessageWithArgs(4, 4, 0);
            } else {
                LogUtil.d(TAG, "camera has been closed ,skip this message");
            }
        }
    }

    private void setQueuePendingNode(int operationType) {
        this.mQueuePendingNode = operationType;
        LogUtil.d(TAG, "setQueuePendingNode, mQueuePendingNode = " + this.mQueuePendingNode);
    }

    private void setHandlePendingNode(int operationType) {
        LogUtil.d(TAG, "setHandlePendingNode, operationType = " + operationType);
        this.mHandlePendingNode = operationType;
    }

    /* access modifiers changed from: private */
    public void dumpCameraState(String caller) {
    }

    /* access modifiers changed from: private */
    public void dumpCameraMessageQueue(int cameraMessage) {
        if (FaceService.mCanDumpCameraMessageQueue) {
            LogUtil.d(TAG, "cameraMessage = " + cameraMessage + " = " + msgToString(cameraMessage));
            this.mLooper.getQueue().dumpMessage();
            LogUtil.d(TAG, "dumpMessage end");
        }
    }

    private void testToAddManyCameraMessages() {
        if (FaceService.mCanAddCameraMessageToQueue) {
            for (int i = 0; i < 2; i++) {
                this.mHandler.obtainMessage(1, 0, 0, null).sendToTarget();
                this.mHandler.obtainMessage(2, 0, 0, null).sendToTarget();
                this.mHandler.obtainMessage(3, 0, 0, null).sendToTarget();
                this.mHandler.obtainMessage(4, 0, 0, null).sendToTarget();
            }
            LogUtil.d(TAG, "testToAddManyCameraMessages");
        }
    }

    public String msgToString(int msgCode) {
        switch (msgCode) {
            case 1:
                return "MSG_OPEN_CAMERA";
            case 2:
                return "MSG_START_PREVIEW";
            case 3:
                return "MSG_STOP_PREVIEW";
            case 4:
                return "MSG_CLOSE_CAMERA";
            case 5:
                return "MSG_CAMERA_TIMEOUT";
            case 6:
                return "MSG_NEW_PREVIEW_FRAME_COMMING";
            case 7:
            default:
                return "unknown-code";
            case 8:
                return "MSG_CAMERA_WORKING_TIMEOUT";
        }
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        pw.print(prefix);
        pw.println("CaptureManager dump");
        pw.print("  " + prefix);
        pw.println("mQueuePendingNode = " + this.mQueuePendingNode);
        this.mCameraView.dump(pw, args, "");
    }

    public void updateCameraParameters(PrintWriter pw, String parameterArgs) {
        this.mCameraView.updateCameraParameters(pw, parameterArgs);
    }

    public void updateTouchAEParameter() {
        this.mCameraView.updateTouchAEParameter();
    }

    public void removeCameraTimeoutMessage() {
        if (this.mSubHandler.hasMessages(5)) {
            this.mSubHandler.removeMessage(5);
            LogUtil.d(TAG, "removeCameraTimeoutMessage");
        }
    }

    public void removeCameraWorkingTimeoutMessage() {
        if (this.mSubHandler.hasMessages(8)) {
            this.mSubHandler.removeMessage(8);
            LogUtil.d(TAG, "removeCameraWorkingTimeoutMessage");
        }
    }
}
