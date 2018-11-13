package com.android.server.face.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.hardware.face.ClientMode;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.am.OppoProcessManager;
import com.android.server.face.FaceHypnus;
import com.android.server.face.FaceService;
import com.android.server.face.health.HealthState;
import com.android.server.face.sensor.SensorManagerClient;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import java.io.PrintWriter;

public class CaptureManager {
    private static final int MAX_OPEN_CAMERA_ATTEMPTS = 5;
    private static final int MSG_CLOSE_CAMERA = 4;
    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_PREVIEW = 2;
    private static final int MSG_STOP_PREVIEW = 3;
    private static final String TAG = "FaceService.CaptureManager";
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
    private final CameraView mCameraView;
    private final Context mContext;
    private FaceDetectionListener mFaceListener = new FaceDetectionListener() {
        public void onFaceDetection(Face[] faces, Camera camera) {
            if (faces != null && faces.length > 0) {
                LogUtil.d(CaptureManager.TAG, "facecount = " + faces.length + ", rect = " + faces[0].rect.toString());
            }
        }
    };
    private int mFramecount = 0;
    private int mHandlePendingNode = 8;
    private ExHandler mHandler;
    private IPreviewCallback mIPreviewCallback;
    public volatile boolean mIsFirstFrameComming = false;
    public volatile boolean mIsNv21DataReady = false;
    public volatile boolean mIsTracking = false;
    private final Looper mLooper;
    public byte[] mNv21 = null;
    private int mOpenCameraAttempts = 0;
    private final PreviewCallback mPreviewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (CaptureManager.this.mCameraView != null) {
                CaptureManager.this.mCameraView.addCallbackBuffer();
            }
            if (FaceService.DEBUG_PERF && (CaptureManager.this.mIsFirstFrameComming ^ 1) != 0) {
                TimeUtils.calculateTime(CaptureManager.TAG, "FirstFrameComming", SystemClock.uptimeMillis() - CaptureManager.this.mStartPriviewEndTime);
                CaptureManager.this.mIsFirstFrameComming = true;
            }
            CaptureManager.this.mIPreviewCallback.onNewPreviewFrameComming(data, CaptureManager.this.getPreViewWidth(), CaptureManager.this.getPreViewHeight(), CaptureManager.this.getDegrees());
        }
    };
    private volatile int mQueuePendingNode = 4;
    private final SensorManagerClient mSensorManagerClient;
    private final HandlerThread mServiceThread;
    private long mStartPriviewEndTime;
    private Surface mSurface = null;

    public interface IPreviewCallback {
        void onError(int i);

        void onNewPreviewFrameComming(byte[] bArr, int i, int i2, int i3);

        void onPreviewStarted();
    }

    public class CameraErrorCallback implements ErrorCallback {
        public void onError(int error, Camera camera) {
            LogUtil.d(CaptureManager.TAG, "onError , error = " + error + ", isCameraOpened = " + CaptureManager.this.mCameraView.isCameraOpened());
            if ((2 == error || 100 == error) && CaptureManager.this.mCameraView.isCameraOpened()) {
                CaptureManager.this.mIPreviewCallback.onError(8);
            }
        }
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
        this.mServiceThread.start();
        this.mLooper = this.mServiceThread.getLooper();
        initHandler();
        this.mCameraView = new CameraView(context, new CameraErrorCallback());
        this.mSensorManagerClient = SensorManagerClient.getInstance(context);
        updateCallback(callback);
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                CaptureManager.this.dumpCameraState(CaptureManager.this.msgToString(msg.what));
                switch (msg.what) {
                    case 1:
                        CaptureManager.this.handleOpenCamera();
                        break;
                    case 2:
                        CaptureManager.this.handleStartPreview();
                        break;
                    case 3:
                        CaptureManager.this.handleStopPreview();
                        break;
                    case 4:
                        CaptureManager.this.handleCloseCamera();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public boolean isCameraClosed() {
        return this.mCameraView.isCameraOpened() ^ 1;
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
        synchronized (sPendingMutex) {
            this.mCameraView.setCurrentMode(mode);
            openCameraLocked();
            startPreviewLocked();
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

    private void handleOpenCamera() {
        this.mOpenCameraAttempts++;
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        LogUtil.d(TAG, "handleOpenCamera, mOpenCameraAttempts = " + this.mOpenCameraAttempts);
        setHandlePendingNode(1);
        this.mSensorManagerClient.register();
        int result = this.mCameraView.openCamera();
        if (result != 0) {
            LogUtil.e(TAG, "openCamera Failed, result = " + result);
            this.mOpenCameraAttempts = 0;
            setHandlePendingNode(8);
            LogUtil.e(TAG, "handleOpenCamera, result = " + result);
            this.mIPreviewCallback.onError(8);
            return;
        }
        this.mOpenCameraAttempts = 0;
        this.mCameraView.setFaceDetectionListener(this.mFaceListener);
        if (this.mCameraView.initCamera(this.mPreviewCallback, this.mSurface) == 0) {
            setHandlePendingNode(5);
        } else {
            this.mOpenCameraAttempts = 0;
            this.mIPreviewCallback.onError(8);
        }
    }

    private void handleCloseCamera() {
        setHandlePendingNode(4);
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        this.mSensorManagerClient.unRegister();
        LogUtil.d(TAG, "handleCloseCamera, result = " + this.mCameraView.releaseCamera());
        setHandlePendingNode(8);
    }

    private void handleStartPreview() {
        setHandlePendingNode(2);
        FaceHypnus.getInstance().hypnusSpeedUp(250);
        int result = this.mCameraView.startPreview();
        if (FaceService.DEBUG_PERF) {
            this.mStartPriviewEndTime = SystemClock.uptimeMillis();
            this.mIsFirstFrameComming = false;
        }
        if (result != 0) {
            this.mIPreviewCallback.onError(8);
            LogUtil.e(TAG, "handleStartPreview, result = " + result);
            return;
        }
        this.mIPreviewCallback.onPreviewStarted();
        setHandlePendingNode(6);
    }

    private void handleStopPreview() {
        setHandlePendingNode(3);
        this.mFramecount = 0;
        FaceHypnus.getInstance().hypnusSpeedUp(OppoProcessManager.MSG_READY_ENTER_STRICTMODE);
        int result = this.mCameraView.stopPreview();
        this.mSurface = null;
        setHandlePendingNode(7);
        if (result != 0) {
            LogUtil.e(TAG, "handleStopPreview, result = " + result);
            handleCloseCamera();
        }
    }

    private void sendMessageWithArgs(int operationType, int what, int arg1) {
        synchronized (sPendingMutex) {
            if (operationType != (this.mQueuePendingNode % 4) + 1) {
                LogUtil.e(TAG, "sendMessageWithArgs, mQueuePendingNode: " + this.mQueuePendingNode + "  ---> " + what);
                return;
            }
            setQueuePendingNode(operationType);
            this.mHandler.obtainMessage(what, arg1, 0, null).sendToTarget();
        }
    }

    private void openCameraLocked() {
        synchronized (sPendingMutex) {
            if (this.mQueuePendingNode == 4) {
                LogUtil.d(TAG, "openCameraLocked");
                this.mOpenCameraAttempts = 0;
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

    private void dumpCameraState(String caller) {
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
            default:
                return "unknown-code";
        }
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        pw.print(prefix);
        pw.println("CaptureManager dump");
        pw.print(subPrefix);
        pw.println("mQueuePendingNode = " + this.mQueuePendingNode);
        this.mCameraView.dump(pw, args, "");
    }

    public void updateCameraParameters(PrintWriter pw, String parameterArgs) {
        this.mCameraView.updateCameraParameters(pw, parameterArgs);
    }

    public void updateTouchAEParameter() {
        this.mCameraView.updateTouchAEParameter();
    }
}
