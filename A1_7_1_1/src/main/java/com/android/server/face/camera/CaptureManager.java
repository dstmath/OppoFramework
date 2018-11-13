package com.android.server.face.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;
import com.android.server.ServiceThread;
import com.android.server.am.OppoProcessManager;
import com.android.server.face.FaceHypnus;
import com.android.server.face.FaceService;
import com.android.server.face.sensor.SensorManagerClient;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;

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
public class CaptureManager {
    private static final int MAX_OPEN_CAMERA_ATTEMPTS = 5;
    private static final int MSG_CLOSE_CAMERA = 4;
    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_PREVIEW = 2;
    private static final int MSG_STOP_PREVIEW = 3;
    private static final String TAG = "FaceService.CaptureManager";
    private static Object mMutex;
    private static Object mPendingMutex;
    private static CaptureManager mSingleInstance;
    public final int HANDLE_NODE_BEGIN_CLOSE_CAMERA;
    public final int HANDLE_NODE_BEGIN_OPEN_CAMERA;
    public final int HANDLE_NODE_BEGIN_START_PREVIEW;
    public final int HANDLE_NODE_BEGIN_STOP_PREVIEW;
    public final int HANDLE_NODE_FINISH_CLOSE_CAMERA;
    public final int HANDLE_NODE_FINISH_OPEN_CAMERA;
    public final int HANDLE_NODE_FINISH_START_PREVIEW;
    public final int HANDLE_NODE_FINISH_STOP_PREVIEW;
    public final int QUEUE_NODE_CLOSE_CAMERA;
    public final int QUEUE_NODE_OPEN_CAMERA;
    public final int QUEUE_NODE_START_PREVIEW;
    public final int QUEUE_NODE_STOP_PREVIEW;
    private final CameraView mCameraView;
    private final Context mContext;
    private FaceDetectionListener mFaceListener;
    private int mFramecount;
    private int mHandlePendingNode;
    private ExHandler mHandler;
    private IPreviewCallback mIPreviewCallback;
    public volatile boolean mIsFirstFrameComming;
    public volatile boolean mIsNv21DataReady;
    public volatile boolean mIsTracking;
    private final Looper mLooper;
    public byte[] mNv21;
    private int mOpenCameraAttempts;
    private final PreviewCallback mPreviewCallback;
    private volatile int mQueuePendingNode;
    private final SensorManagerClient mSensorManagerClient;
    private final HandlerThread mServiceThread;
    private long mStartPriviewEndTime;
    private Surface mSurface;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.camera.CaptureManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.camera.CaptureManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.camera.CaptureManager.<clinit>():void");
    }

    public static CaptureManager initCaptureManager(Context c, IPreviewCallback callback) {
        return getCaptureManager(c, callback);
    }

    public static CaptureManager getCaptureManager(Context c, IPreviewCallback callback) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new CaptureManager(c, callback);
            }
            mSingleInstance.updateCallback(callback);
        }
        return mSingleInstance;
    }

    private void updateCallback(IPreviewCallback callback) {
        this.mIPreviewCallback = callback;
    }

    public static CaptureManager getCaptureManager() {
        return mSingleInstance;
    }

    public CaptureManager(Context context, IPreviewCallback callback) {
        this.QUEUE_NODE_OPEN_CAMERA = 1;
        this.QUEUE_NODE_START_PREVIEW = 2;
        this.QUEUE_NODE_STOP_PREVIEW = 3;
        this.QUEUE_NODE_CLOSE_CAMERA = 4;
        this.HANDLE_NODE_BEGIN_OPEN_CAMERA = 1;
        this.HANDLE_NODE_BEGIN_START_PREVIEW = 2;
        this.HANDLE_NODE_BEGIN_STOP_PREVIEW = 3;
        this.HANDLE_NODE_BEGIN_CLOSE_CAMERA = 4;
        this.HANDLE_NODE_FINISH_OPEN_CAMERA = 5;
        this.HANDLE_NODE_FINISH_START_PREVIEW = 6;
        this.HANDLE_NODE_FINISH_STOP_PREVIEW = 7;
        this.HANDLE_NODE_FINISH_CLOSE_CAMERA = 8;
        this.mQueuePendingNode = 4;
        this.mHandlePendingNode = 8;
        this.mOpenCameraAttempts = 0;
        this.mFramecount = 0;
        this.mNv21 = null;
        this.mIsNv21DataReady = false;
        this.mIsTracking = false;
        this.mIsFirstFrameComming = false;
        this.mSurface = null;
        this.mFaceListener = new FaceDetectionListener() {
            public void onFaceDetection(Face[] faces, Camera camera) {
                if (faces != null && faces.length > 0) {
                    LogUtil.d(CaptureManager.TAG, "facecount = " + faces.length + ", rect = " + faces[0].rect.toString());
                }
            }
        };
        this.mPreviewCallback = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (CaptureManager.this.mCameraView != null) {
                    CaptureManager.this.mCameraView.addCallbackBuffer();
                }
                if (FaceService.DEBUG_PERF && !CaptureManager.this.mIsFirstFrameComming) {
                    TimeUtils.calculateTime(CaptureManager.TAG, "FirstFrameComming", SystemClock.uptimeMillis() - CaptureManager.this.mStartPriviewEndTime);
                    CaptureManager.this.mIsFirstFrameComming = true;
                }
                CaptureManager.this.mIPreviewCallback.onNewPreviewFrameComming(data, CaptureManager.this.getPreViewWidth(), CaptureManager.this.getPreViewHeight(), CaptureManager.this.getDegrees());
            }
        };
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
                LogUtil.d(CaptureManager.TAG, "---------------| begin " + CaptureManager.this.msgToString(msg.what));
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
                LogUtil.d(CaptureManager.TAG, "---------------| end " + CaptureManager.this.msgToString(msg.what));
                super.handleMessage(msg);
            }
        };
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

    public void startCapture() {
        synchronized (mPendingMutex) {
            openCameraLocked();
            startPreviewLocked();
        }
    }

    public void stopCapture() {
        synchronized (mPendingMutex) {
            stopPreviewLocked();
            closeCameraLocked();
        }
    }

    public void stopPreview() {
        synchronized (mPendingMutex) {
            stopPreviewLocked();
        }
    }

    public int setPreviewSurface(Surface surface) {
        return handleSetPreviewSurface(surface);
    }

    private int handleSetPreviewSurface(Surface surface) {
        this.mSurface = surface;
        dumpCameraState("setPreviewSurface");
        if (!this.mCameraView.isCameraOpened()) {
            return -1;
        }
        LogUtil.d(TAG, "setPreviewSurface");
        return this.mCameraView.setPreviewSurface(surface);
    }

    private void handleOpenCamera() {
        this.mOpenCameraAttempts++;
        FaceHypnus.getInstance().hypnusSpeedUp(NetdResponseCode.NetInfoSipResult);
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
        FaceHypnus.getInstance().hypnusSpeedUp(NetdResponseCode.NetInfoSipResult);
        this.mSensorManagerClient.unRegister();
        LogUtil.d(TAG, "handleCloseCamera, result = " + this.mCameraView.releaseCamera());
        setHandlePendingNode(8);
    }

    private void handleStartPreview() {
        setHandlePendingNode(2);
        FaceHypnus.getInstance().hypnusSpeedUp(NetdResponseCode.NetInfoSipResult);
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
        synchronized (mPendingMutex) {
            if (operationType != (this.mQueuePendingNode % 4) + 1) {
                LogUtil.e(TAG, "sendMessageWithArgs, mQueuePendingNode: " + this.mQueuePendingNode + "  ---> " + what);
                return;
            }
            setQueuePendingNode(operationType);
            this.mHandler.obtainMessage(what, arg1, 0, null).sendToTarget();
        }
    }

    private void openCameraLocked() {
        synchronized (mPendingMutex) {
            LogUtil.d(TAG, "openCameraLocked, mQueuePendingNode = " + this.mQueuePendingNode + ", mHandlePendingNode = " + this.mHandlePendingNode);
            if (this.mQueuePendingNode == 4) {
                this.mOpenCameraAttempts = 0;
                sendMessageWithArgs(1, 1, 0);
            } else {
                LogUtil.d(TAG, "camera has been opened ,skip this message");
            }
        }
    }

    private void startPreviewLocked() {
        synchronized (mPendingMutex) {
            LogUtil.d(TAG, "startPreviewLocked, mQueuePendingNode = " + this.mQueuePendingNode + ", mHandlePendingNode = " + this.mHandlePendingNode);
            if (this.mQueuePendingNode == 1) {
                sendMessageWithArgs(2, 2, 0);
            } else {
                LogUtil.d(TAG, "camera has stared preview ,skip this message");
            }
        }
    }

    private void stopPreviewLocked() {
        synchronized (mPendingMutex) {
            LogUtil.d(TAG, "stopPreviewLocked, mQueuePendingNode = " + this.mQueuePendingNode + ", mHandlePendingNode = " + this.mHandlePendingNode);
            if (this.mQueuePendingNode == 2) {
                sendMessageWithArgs(3, 3, 0);
            } else {
                LogUtil.d(TAG, "camera has stoped preview ,skip this message");
            }
        }
    }

    private void closeCameraLocked() {
        synchronized (mPendingMutex) {
            LogUtil.d(TAG, "closeCameraLocked, mQueuePendingNode = " + this.mQueuePendingNode + ", mHandlePendingNode = " + this.mHandlePendingNode);
            if (this.mQueuePendingNode == 3) {
                sendMessageWithArgs(4, 4, 0);
            } else {
                LogUtil.d(TAG, "camera has been closed ,skip this message");
            }
        }
    }

    private void setQueuePendingNode(int operationType) {
        LogUtil.d(TAG, "setQueuePendingNode, operationType = " + operationType);
        this.mQueuePendingNode = operationType;
        LogUtil.d(TAG, "setQueuePendingNode, mQueuePendingNode = " + this.mQueuePendingNode);
    }

    private void setHandlePendingNode(int operationType) {
        LogUtil.d(TAG, "setHandlePendingNode, operationType = " + operationType);
        this.mHandlePendingNode = operationType;
    }

    private void dumpCameraState(String caller) {
        LogUtil.d(TAG, caller + ", dump Camera State ( isCameraOpened() = " + this.mCameraView.isCameraOpened() + ", isPreviewing() = " + this.mCameraView.isPreviewing() + " )");
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
        this.mCameraView.dump(pw, args, IElsaManager.EMPTY_PACKAGE);
    }

    public void updateCameraParameters(PrintWriter pw, String parameter_args) {
        this.mCameraView.updateCameraParameters(pw, parameter_args);
    }

    public void updateTouchAEParameter() {
        this.mCameraView.updateTouchAEParameter();
    }
}
