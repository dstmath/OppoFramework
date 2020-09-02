package android.hardware;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.batterySipper.OppoBaseBatterySipper;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.IAudioService;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSIllegalArgumentException;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.system.Os;
import android.system.OsConstants;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.oppo.atlas.OppoAtlasManagerDefine;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import oppo.util.OppoStatistics;

@Deprecated
public class Camera {
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";
    public static final String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";
    public static final int CAMERA_ERROR_DISABLED = 3;
    public static final int CAMERA_ERROR_EVICTED = 2;
    public static final int CAMERA_ERROR_SERVER_DIED = 100;
    public static final int CAMERA_ERROR_UNKNOWN = 1;
    private static final int CAMERA_FACE_DETECTION_HW = 0;
    private static final int CAMERA_FACE_DETECTION_SW = 1;
    @UnsupportedAppUsage
    public static final int CAMERA_HAL_API_VERSION_1_0 = 256;
    private static final int CAMERA_HAL_API_VERSION_NORMAL_CONNECT = -2;
    private static final int CAMERA_HAL_API_VERSION_UNSPECIFIED = -1;
    private static final int CAMERA_MSG_COMPRESSED_IMAGE = 256;
    private static final int CAMERA_MSG_ERROR = 1;
    private static final int CAMERA_MSG_FOCUS = 4;
    private static final int CAMERA_MSG_FOCUS_MOVE = 2048;
    private static final int CAMERA_MSG_POSTVIEW_FRAME = 64;
    private static final int CAMERA_MSG_PREVIEW_FRAME = 16;
    private static final int CAMERA_MSG_PREVIEW_METADATA = 1024;
    private static final int CAMERA_MSG_RAW_IMAGE = 128;
    private static final int CAMERA_MSG_RAW_IMAGE_NOTIFY = 512;
    private static final int CAMERA_MSG_SHUTTER = 2;
    private static final int CAMERA_MSG_VIDEO_FRAME = 32;
    private static final int CAMERA_MSG_ZOOM = 8;
    private static final int MTK_CAMERA_MSG_EXT_DATA = 536870912;
    private static final int MTK_CAMERA_MSG_EXT_DATA_AF = 2;
    private static final int MTK_CAMERA_MSG_EXT_DATA_AUTORAMA = 1;
    private static final int MTK_CAMERA_MSG_EXT_DATA_FACEBEAUTY = 6;
    private static final int MTK_CAMERA_MSG_EXT_DATA_JPS = 17;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_CLEAR_IMAGE = 21;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DBG = 18;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DEPTHMAP = 20;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DEPTHWRAPPER = 32;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_LDC = 22;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_N3D = 25;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY = 1073741824;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_ASD = 2;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_CONTINUOUS_END = 6;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_IMAGE_UNCOMPRESSED = 23;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_STEREO_DISTANCE = 21;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_STEREO_WARNING = 20;
    private static final int NO_ERROR = 0;
    private static final String TAG = "Camera";
    private AFDataCallback mAFDataCallback;
    private IAppOpsService mAppOps;
    private IAppOpsCallback mAppOpsCallback;
    private AsdCallback mAsdCallback;
    /* access modifiers changed from: private */
    public AutoFocusCallback mAutoFocusCallback;
    /* access modifiers changed from: private */
    public final Object mAutoFocusCallbackLock = new Object();
    /* access modifiers changed from: private */
    public AutoFocusMoveCallback mAutoFocusMoveCallback;
    private AutoRamaCallback mAutoRamaCallback;
    private AutoRamaMoveCallback mAutoRamaMoveCallback;
    private ContinuousShotCallback mCSDoneCallback;
    private int mCameraId = 0;
    private long mConnectTime = 0;
    /* access modifiers changed from: private */
    public ErrorCallback mDetailedErrorCallback;
    private long mDisconnectTime = 0;
    private DistanceInfoCallback mDistanceInfoCallback;
    /* access modifiers changed from: private */
    public ErrorCallback mErrorCallback;
    private EventHandler mEventHandler;
    private boolean mFaceDetectionRunning = false;
    /* access modifiers changed from: private */
    public FaceDetectionListener mFaceListener;
    private FbOriginalCallback mFbOriginalCallback;
    @GuardedBy({"mShutterSoundLock"})
    private boolean mHasAppOpsPlayAudio = true;
    /* access modifiers changed from: private */
    public PictureCallback mJpegCallback;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private long mNativeContext;
    /* access modifiers changed from: private */
    public boolean mOneShot;
    /* access modifiers changed from: private */
    public PictureCallback mPostviewCallback;
    /* access modifiers changed from: private */
    public PreviewCallback mPreviewCallback;
    /* access modifiers changed from: private */
    public PictureCallback mRawImageCallback;
    /* access modifiers changed from: private */
    public ShutterCallback mShutterCallback;
    @GuardedBy({"mShutterSoundLock"})
    private boolean mShutterSoundEnabledFromApp = true;
    private final Object mShutterSoundLock = new Object();
    private StereoCameraDataCallback mStereoCameraDataCallback;
    private StereoCameraWarningCallback mStereoCameraWarningCallback;
    private PictureCallback mUncompressedImageCallback;
    private boolean mUsingPreviewAllocation;
    private VendorDataCallback mVendorDataCallback;
    /* access modifiers changed from: private */
    public boolean mWithBuffer;
    /* access modifiers changed from: private */
    public OnZoomChangeListener mZoomListener;

    public interface AFDataCallback {
        void onAFData(byte[] bArr, Camera camera);
    }

    public interface AsdCallback {
        void onDetected(int i);
    }

    @Deprecated
    public interface AutoFocusCallback {
        void onAutoFocus(boolean z, Camera camera);
    }

    @Deprecated
    public interface AutoFocusMoveCallback {
        void onAutoFocusMoving(boolean z, Camera camera);
    }

    public interface AutoRamaCallback {
        void onCapture(byte[] bArr);
    }

    public interface AutoRamaMoveCallback {
        void onFrame(int i, int i2);
    }

    @Deprecated
    public static class CameraInfo {
        public static final int CAMERA_FACING_BACK = 0;
        public static final int CAMERA_FACING_FRONT = 1;
        public boolean canDisableShutterSound;
        public int facing;
        public int orientation;
    }

    public interface ContinuousShotCallback {
        void onConinuousShotDone(int i);
    }

    public interface DistanceInfoCallback {
        void onInfo(String str);
    }

    @Deprecated
    public interface ErrorCallback {
        void onError(int i, Camera camera);
    }

    @Deprecated
    public static class Face {
        public int id = -1;
        public Point leftEye = null;
        public Point mouth = null;
        public Rect rect;
        public Point rightEye = null;
        public int score;
    }

    @Deprecated
    public interface FaceDetectionListener {
        void onFaceDetection(Face[] faceArr, Camera camera);
    }

    public interface FbOriginalCallback {
        void onCapture(byte[] bArr);
    }

    @Deprecated
    public interface OnZoomChangeListener {
        void onZoomChange(int i, boolean z, Camera camera);
    }

    @Deprecated
    public interface PictureCallback {
        void onPictureTaken(byte[] bArr, Camera camera);
    }

    @Deprecated
    public interface PreviewCallback {
        void onPreviewFrame(byte[] bArr, Camera camera);
    }

    @Deprecated
    public interface ShutterCallback {
        void onShutter();
    }

    public interface StereoCameraDataCallback {
        void onClearImageCapture(byte[] bArr);

        void onDepthMapCapture(byte[] bArr);

        void onDepthWrapperCapture(byte[] bArr);

        void onJpsCapture(byte[] bArr);

        void onLdcCapture(byte[] bArr);

        void onMaskCapture(byte[] bArr);

        void onN3dCapture(byte[] bArr);
    }

    public interface StereoCameraWarningCallback {
        void onWarning(int i);
    }

    public interface VendorDataCallback {
        void onDataCallback(Message message);
    }

    private final native void _addCallbackBuffer(byte[] bArr, int i);

    private final native boolean _enableShutterSound(boolean z);

    private static native void _getCameraInfo(int i, CameraInfo cameraInfo);

    public static native int _getNumberOfCameras();

    private final native void _startFaceDetection(int i);

    private final native void _stopFaceDetection();

    private final native void _stopPreview();

    private native void enableFocusMoveCallback(int i);

    private final native void native_autoFocus();

    private final native void native_cancelAutoFocus();

    @UnsupportedAppUsage
    private final native String native_getParameters();

    private static native String native_getProperty(String str, String str2);

    private final native void native_release();

    @UnsupportedAppUsage
    private final native void native_setParameters(String str);

    private static native void native_setProperty(String str, String str2);

    @UnsupportedAppUsage
    private final native int native_setup(Object obj, int i, int i2, String str);

    private final native void native_takePicture(int i);

    /* access modifiers changed from: private */
    public final native void setHasPreviewCallback(boolean z, boolean z2);

    private final native void setPreviewCallbackSurface(Surface surface);

    private final native void startAUTORAMA(int i);

    private native void stopAUTORAMA(int i);

    public native void cancelContinuousShot();

    public final native void lock();

    @UnsupportedAppUsage
    public final native boolean previewEnabled();

    public final native void reconnect() throws IOException;

    public native void setContinuousShotSpeed(int i);

    public final native void setDisplayOrientation(int i);

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final native void setPreviewSurface(Surface surface) throws IOException;

    public final native void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

    public final native void startPreview();

    public final native void startSmoothZoom(int i);

    public final native void stopSmoothZoom();

    public final native void unlock();

    public static int getNumberOfCameras() {
        boolean isDualCamAllowed;
        String packageName = ActivityThread.currentOpPackageName();
        if (packageName.equals(OppoAtlasManagerDefine.CAMERA_PACKAGE_NAME) || packageName.equals("com.oppo.engineermode") || packageName.equals("com.mediatek.engineermode")) {
            isDualCamAllowed = true;
        } else {
            isDualCamAllowed = false;
        }
        int numberOfCameras = _getNumberOfCameras();
        if (isDualCamAllowed || numberOfCameras <= 2) {
            return numberOfCameras;
        }
        return 2;
    }

    private static boolean isOppoApp() {
        String packageName = ActivityThread.currentOpPackageName();
        if (packageName.equals(OppoAtlasManagerDefine.CAMERA_PACKAGE_NAME) || packageName.equals("com.oppo.engineermode") || packageName.equals("com.coloros.healthcheck") || packageName.equals("com.mediatek.engineermode")) {
            return true;
        }
        return false;
    }

    public static void getCameraInfo(int cameraId, CameraInfo cameraInfo) {
        if (isOppoApp() || cameraId < 2) {
            _getCameraInfo(cameraId, cameraInfo);
            try {
                if (IAudioService.Stub.asInterface(ServiceManager.getService("audio")).isCameraSoundForced()) {
                    cameraInfo.canDisableShutterSound = false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Audio service is unavailable for queries");
            }
        } else {
            throw new RuntimeException("Camera initialization failed because the input arugments are invalid");
        }
    }

    public static Camera open(int cameraId) {
        return new Camera(cameraId);
    }

    public static Camera open() {
        int numberOfCameras = getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return new Camera(i);
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public static Camera openLegacy(int cameraId, int halVersion) {
        if (halVersion >= 256) {
            return new Camera(cameraId, halVersion);
        }
        throw new IllegalArgumentException("Invalid HAL version " + halVersion);
    }

    private Camera(int cameraId, int halVersion) {
        if (isOppoApp() || cameraId < 2) {
            int err = cameraInitVersion(cameraId, halVersion);
            if (!checkInitErrors(err)) {
                this.mConnectTime = System.currentTimeMillis();
                addInfo(cameraId, 0);
            } else if (err == (-OsConstants.EACCES)) {
                throw new RuntimeException("Fail to connect to camera service");
            } else if (err == (-OsConstants.ENODEV)) {
                throw new RuntimeException("Camera initialization failed");
            } else if (err == (-OsConstants.ENOSYS)) {
                throw new RuntimeException("Camera initialization failed because some methods are not implemented");
            } else if (err == (-OsConstants.EOPNOTSUPP)) {
                throw new RuntimeException("Camera initialization failed because the hal version is not supported by this device");
            } else if (err == (-OsConstants.EINVAL)) {
                throw new RuntimeException("Camera initialization failed because the input arugments are invalid");
            } else if (err == (-OsConstants.EBUSY)) {
                throw new RuntimeException("Camera initialization failed because the camera device was already opened");
            } else if (err == (-OsConstants.EUSERS)) {
                throw new RuntimeException("Camera initialization failed because the max number of camera devices were already opened");
            } else {
                throw new RuntimeException("Unknown camera error");
            }
        } else {
            throw new RuntimeException("Camera initialization failed because the input arugments are invalid");
        }
    }

    private int cameraInitVersion(int cameraId, int halVersion) {
        this.mShutterCallback = null;
        this.mRawImageCallback = null;
        this.mJpegCallback = null;
        this.mPreviewCallback = null;
        this.mPostviewCallback = null;
        this.mUsingPreviewAllocation = false;
        this.mZoomListener = null;
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper looper2 = Looper.getMainLooper();
            if (looper2 != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        return native_setup(new WeakReference(this), cameraId, halVersion, ActivityThread.currentOpPackageName());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    private void addInfo(int cameraId, long disconnectTime) {
        if (this.mConnectTime > 1) {
            this.mCameraId = cameraId;
            HashMap<String, String> eventMap = new HashMap<>();
            eventMap.put(OppoBaseBatterySipper.BundlePkgName, ActivityThread.currentOpPackageName());
            eventMap.put("cameraId", String.valueOf(cameraId));
            eventMap.put("apLevel", WifiEnterpriseConfig.ENGINE_ENABLE);
            eventMap.put("halLevel", WifiEnterpriseConfig.ENGINE_ENABLE);
            eventMap.put("connentTime", String.valueOf(this.mConnectTime));
            if (disconnectTime > 1) {
                eventMap.put("disconnectTime", String.valueOf(disconnectTime));
                eventMap.put("timeCost", String.valueOf(disconnectTime - this.mConnectTime));
                this.mConnectTime = 0;
            }
            OppoStatistics.onCommon(ActivityThread.currentApplication().getApplicationContext(), "2012002", "openCamera", (Map<String, String>) eventMap, false);
        }
    }

    private int cameraInitNormal(int cameraId) {
        return cameraInitVersion(cameraId, -2);
    }

    public int cameraInitUnspecified(int cameraId) {
        return cameraInitVersion(cameraId, -1);
    }

    Camera(int cameraId) {
        ((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).setAction(153, Os.getuid());
        if (isOppoApp() || cameraId < 2) {
            int err = cameraInitNormal(cameraId);
            if (!checkInitErrors(err)) {
                this.mConnectTime = System.currentTimeMillis();
                addInfo(cameraId, 0);
                initAppOps();
            } else if (err == (-OsConstants.EACCES)) {
                throw new RuntimeException("Fail to connect to camera service");
            } else if (err == (-OsConstants.ENODEV)) {
                throw new RuntimeException("Camera initialization failed");
            } else {
                throw new RuntimeException("Unknown camera error");
            }
        } else {
            throw new RuntimeException("Camera initialization failed because the input arugments are invalid");
        }
    }

    public static boolean checkInitErrors(int err) {
        return err != 0;
    }

    public static Camera openUninitialized() {
        return new Camera();
    }

    Camera() {
    }

    private void initAppOps() {
        this.mAppOps = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        updateAppOpsPlayAudio();
        this.mAppOpsCallback = new IAppOpsCallbackWrapper(this);
        try {
            this.mAppOps.startWatchingMode(28, ActivityThread.currentPackageName(), this.mAppOpsCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Error registering appOps callback", e);
            this.mHasAppOpsPlayAudio = false;
        }
    }

    private void releaseAppOps() {
        try {
            if (this.mAppOps != null) {
                this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
            }
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }

    public final void release() {
        native_release();
        this.mFaceDetectionRunning = false;
        releaseAppOps();
        this.mDisconnectTime = System.currentTimeMillis();
        addInfo(this.mCameraId, this.mDisconnectTime);
    }

    public final void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        if (holder != null) {
            setPreviewSurface(holder.getSurface());
        } else {
            setPreviewSurface(null);
        }
    }

    public final void stopPreview() {
        _stopPreview();
        this.mFaceDetectionRunning = false;
        this.mShutterCallback = null;
        this.mRawImageCallback = null;
        this.mPostviewCallback = null;
        this.mJpegCallback = null;
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = null;
        }
        this.mAutoFocusMoveCallback = null;
    }

    public final void setPreviewCallback(PreviewCallback cb) {
        this.mPreviewCallback = cb;
        this.mOneShot = false;
        this.mWithBuffer = false;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        setHasPreviewCallback(cb != null, false);
    }

    public final void setOneShotPreviewCallback(PreviewCallback cb) {
        this.mPreviewCallback = cb;
        boolean z = true;
        this.mOneShot = true;
        this.mWithBuffer = false;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        if (cb == null) {
            z = false;
        }
        setHasPreviewCallback(z, false);
    }

    public final void setPreviewCallbackWithBuffer(PreviewCallback cb) {
        this.mPreviewCallback = cb;
        boolean z = false;
        this.mOneShot = false;
        this.mWithBuffer = true;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        if (cb != null) {
            z = true;
        }
        setHasPreviewCallback(z, true);
    }

    public final void addCallbackBuffer(byte[] callbackBuffer) {
        _addCallbackBuffer(callbackBuffer, 16);
    }

    @UnsupportedAppUsage
    public final void addRawImageCallbackBuffer(byte[] callbackBuffer) {
        addCallbackBuffer(callbackBuffer, 128);
    }

    @UnsupportedAppUsage
    private final void addCallbackBuffer(byte[] callbackBuffer, int msgType) {
        if (msgType == 16 || msgType == 128) {
            _addCallbackBuffer(callbackBuffer, msgType);
            return;
        }
        throw new IllegalArgumentException("Unsupported message type: " + msgType);
    }

    public final Allocation createPreviewAllocation(RenderScript rs, int usage) throws RSIllegalArgumentException {
        Size previewSize = getParameters().getPreviewSize();
        Type.Builder yuvBuilder = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
        yuvBuilder.setYuvFormat(ImageFormat.YV12);
        yuvBuilder.setX(previewSize.width);
        yuvBuilder.setY(previewSize.height);
        return Allocation.createTyped(rs, yuvBuilder.create(), usage | 32);
    }

    public final void setPreviewCallbackAllocation(Allocation previewAllocation) throws IOException {
        Surface previewSurface = null;
        if (previewAllocation != null) {
            Size previewSize = getParameters().getPreviewSize();
            if (previewSize.width != previewAllocation.getType().getX() || previewSize.height != previewAllocation.getType().getY()) {
                throw new IllegalArgumentException("Allocation dimensions don't match preview dimensions: Allocation is " + previewAllocation.getType().getX() + ", " + previewAllocation.getType().getY() + ". Preview is " + previewSize.width + ", " + previewSize.height);
            } else if ((previewAllocation.getUsage() & 32) == 0) {
                throw new IllegalArgumentException("Allocation usage does not include USAGE_IO_INPUT");
            } else if (previewAllocation.getType().getElement().getDataKind() == Element.DataKind.PIXEL_YUV) {
                previewSurface = previewAllocation.getSurface();
                this.mUsingPreviewAllocation = true;
            } else {
                throw new IllegalArgumentException("Allocation is not of a YUV type");
            }
        } else {
            this.mUsingPreviewAllocation = false;
        }
        setPreviewCallbackSurface(previewSurface);
    }

    private class EventHandler extends Handler {
        private final Camera mCamera;

        @UnsupportedAppUsage
        public EventHandler(Camera c, Looper looper) {
            super(looper);
            this.mCamera = c;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            AutoFocusCallback cb;
            Log.i(Camera.TAG, "handleMessage: " + msg.what);
            int i = msg.what;
            boolean success = true;
            if (i == 1) {
                Log.e(Camera.TAG, "Error " + msg.arg1);
                if (Camera.this.mDetailedErrorCallback != null) {
                    Camera.this.mDetailedErrorCallback.onError(msg.arg1, this.mCamera);
                } else if (Camera.this.mErrorCallback == null) {
                } else {
                    if (msg.arg1 == 3) {
                        Camera.this.mErrorCallback.onError(2, this.mCamera);
                    } else {
                        Camera.this.mErrorCallback.onError(msg.arg1, this.mCamera);
                    }
                }
            } else if (i != 2) {
                if (i == 4) {
                    synchronized (Camera.this.mAutoFocusCallbackLock) {
                        cb = Camera.this.mAutoFocusCallback;
                    }
                    if (cb != null) {
                        if (msg.arg1 == 0) {
                            success = false;
                        }
                        cb.onAutoFocus(success, this.mCamera);
                    }
                } else if (i != 8) {
                    if (i == 16) {
                        PreviewCallback pCb = Camera.this.mPreviewCallback;
                        if (pCb != null) {
                            if (Camera.this.mOneShot) {
                                PreviewCallback unused = Camera.this.mPreviewCallback = null;
                            } else if (!Camera.this.mWithBuffer) {
                                Camera.this.setHasPreviewCallback(true, false);
                            }
                            pCb.onPreviewFrame((byte[]) msg.obj, this.mCamera);
                        }
                    } else if (i != 64) {
                        if (i != 128) {
                            if (i != 256) {
                                if (i != 1024) {
                                    if (i != 2048) {
                                        if (i == 536870912) {
                                            Camera.this.handleExtData(msg, this.mCamera);
                                        } else if (i != 1073741824) {
                                            Log.e(Camera.TAG, "Unknown message type " + msg.what);
                                        } else {
                                            Camera.this.handleExtNotify(msg, this.mCamera);
                                        }
                                    } else if (Camera.this.mAutoFocusMoveCallback != null) {
                                        AutoFocusMoveCallback access$1400 = Camera.this.mAutoFocusMoveCallback;
                                        if (msg.arg1 == 0) {
                                            success = false;
                                        }
                                        access$1400.onAutoFocusMoving(success, this.mCamera);
                                    }
                                } else if (Camera.this.mFaceListener != null) {
                                    Camera.this.mFaceListener.onFaceDetection((Face[]) msg.obj, this.mCamera);
                                }
                            } else if (Camera.this.mJpegCallback != null) {
                                Camera.this.mJpegCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                            }
                        } else if (Camera.this.mRawImageCallback != null) {
                            Camera.this.mRawImageCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                        }
                    } else if (Camera.this.mPostviewCallback != null) {
                        Camera.this.mPostviewCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                } else if (Camera.this.mZoomListener != null) {
                    OnZoomChangeListener access$1000 = Camera.this.mZoomListener;
                    int i2 = msg.arg1;
                    if (msg.arg2 == 0) {
                        success = false;
                    }
                    access$1000.onZoomChange(i2, success, this.mCamera);
                }
            } else if (Camera.this.mShutterCallback != null) {
                Camera.this.mShutterCallback.onShutter();
            }
        }
    }

    @UnsupportedAppUsage
    private static void postEventFromNative(Object camera_ref, int what, int arg1, int arg2, Object obj) {
        EventHandler eventHandler;
        Camera c = (Camera) ((WeakReference) camera_ref).get();
        if (c != null && (eventHandler = c.mEventHandler) != null) {
            c.mEventHandler.sendMessage(eventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    public final void autoFocus(AutoFocusCallback cb) {
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = cb;
        }
        native_autoFocus();
    }

    public final void cancelAutoFocus() {
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = null;
        }
        native_cancelAutoFocus();
        this.mEventHandler.removeMessages(4);
    }

    public void setAutoFocusMoveCallback(AutoFocusMoveCallback cb) {
        this.mAutoFocusMoveCallback = cb;
        enableFocusMoveCallback(this.mAutoFocusMoveCallback != null ? 1 : 0);
    }

    public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
        takePicture(shutter, raw, null, jpeg);
    }

    public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback postview, PictureCallback jpeg) {
        this.mShutterCallback = shutter;
        this.mRawImageCallback = raw;
        this.mPostviewCallback = postview;
        this.mJpegCallback = jpeg;
        int msgType = 0;
        if (this.mShutterCallback != null) {
            msgType = 0 | 2;
        }
        if (this.mRawImageCallback != null) {
            msgType |= 128;
        }
        if (this.mPostviewCallback != null) {
            msgType |= 64;
        }
        if (this.mJpegCallback != null) {
            msgType |= 256;
        }
        native_takePicture(msgType);
        this.mFaceDetectionRunning = false;
    }

    public final boolean enableShutterSound(boolean enabled) {
        boolean ret;
        boolean canDisableShutterSound = true;
        try {
            if (IAudioService.Stub.asInterface(ServiceManager.getService("audio")).isCameraSoundForced()) {
                canDisableShutterSound = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Audio service is unavailable for queries");
        }
        if (!enabled && !canDisableShutterSound) {
            return false;
        }
        synchronized (this.mShutterSoundLock) {
            this.mShutterSoundEnabledFromApp = enabled;
            ret = _enableShutterSound(enabled);
            if (enabled && !this.mHasAppOpsPlayAudio) {
                Log.i(TAG, "Shutter sound is not allowed by AppOpsManager");
                if (canDisableShutterSound) {
                    _enableShutterSound(false);
                }
            }
        }
        return ret;
    }

    public final boolean disableShutterSound() {
        return _enableShutterSound(false);
    }

    private static class IAppOpsCallbackWrapper extends IAppOpsCallback.Stub {
        private final WeakReference<Camera> mWeakCamera;

        IAppOpsCallbackWrapper(Camera camera) {
            this.mWeakCamera = new WeakReference<>(camera);
        }

        @Override // com.android.internal.app.IAppOpsCallback
        public void opChanged(int op, int uid, String packageName) {
            Camera camera;
            if (op == 28 && (camera = this.mWeakCamera.get()) != null) {
                camera.updateAppOpsPlayAudio();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005e, code lost:
        return;
     */
    public void updateAppOpsPlayAudio() {
        synchronized (this.mShutterSoundLock) {
            boolean oldHasAppOpsPlayAudio = this.mHasAppOpsPlayAudio;
            int mode = 1;
            try {
                if (this.mAppOps != null) {
                    mode = this.mAppOps.checkAudioOperation(28, 13, Process.myUid(), ActivityThread.currentPackageName());
                }
                this.mHasAppOpsPlayAudio = mode == 0;
            } catch (RemoteException e) {
                Log.e(TAG, "AppOpsService check audio operation failed");
                this.mHasAppOpsPlayAudio = false;
            }
            if (oldHasAppOpsPlayAudio != this.mHasAppOpsPlayAudio) {
                if (!this.mHasAppOpsPlayAudio) {
                    try {
                        if (IAudioService.Stub.asInterface(ServiceManager.getService("audio")).isCameraSoundForced()) {
                            return;
                        }
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Audio service is unavailable for queries");
                    }
                    _enableShutterSound(false);
                } else {
                    enableShutterSound(this.mShutterSoundEnabledFromApp);
                }
            }
        }
    }

    public final void setZoomChangeListener(OnZoomChangeListener listener) {
        this.mZoomListener = listener;
    }

    public final void setFaceDetectionListener(FaceDetectionListener listener) {
        this.mFaceListener = listener;
    }

    public final void startFaceDetection() {
        if (!this.mFaceDetectionRunning) {
            _startFaceDetection(0);
            this.mFaceDetectionRunning = true;
            return;
        }
        throw new RuntimeException("Face detection is already running");
    }

    public final void stopFaceDetection() {
        _stopFaceDetection();
        this.mFaceDetectionRunning = false;
    }

    public final void setErrorCallback(ErrorCallback cb) {
        this.mErrorCallback = cb;
    }

    public final void setDetailedErrorCallback(ErrorCallback cb) {
        this.mDetailedErrorCallback = cb;
    }

    public void setParameters(Parameters params) {
        if (this.mUsingPreviewAllocation) {
            Size newPreviewSize = params.getPreviewSize();
            Size currentPreviewSize = getParameters().getPreviewSize();
            if (!(newPreviewSize.width == currentPreviewSize.width && newPreviewSize.height == currentPreviewSize.height)) {
                throw new IllegalStateException("Cannot change preview size while a preview allocation is configured.");
            }
        }
        String s = params.flatten();
        printParameter(s);
        native_setParameters(s);
    }

    public Parameters getParameters() {
        Parameters p = new Parameters();
        String s = native_getParameters();
        p.unflatten(s);
        printParameter(s);
        return p;
    }

    @UnsupportedAppUsage
    public static Parameters getEmptyParameters() {
        Camera camera = new Camera();
        Objects.requireNonNull(camera);
        return new Parameters();
    }

    public static Parameters getParametersCopy(Parameters parameters) {
        if (parameters != null) {
            Camera camera = parameters.getOuter();
            Objects.requireNonNull(camera);
            Parameters p = new Parameters();
            p.copyFrom(parameters);
            return p;
        }
        throw new NullPointerException("parameters must not be null");
    }

    @Deprecated
    public class Size {
        public int height;
        public int width;

        public Size(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            if (this.width == s.width && this.height == s.height) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (this.width * 32713) + this.height;
        }
    }

    @Deprecated
    public static class Area {
        public Rect rect;
        public int weight;

        public Area(Rect rect2, int weight2) {
            this.rect = rect2;
            this.weight = weight2;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Area)) {
                return false;
            }
            Area a = (Area) obj;
            Rect rect2 = this.rect;
            if (rect2 == null) {
                if (a.rect != null) {
                    return false;
                }
            } else if (!rect2.equals(a.rect)) {
                return false;
            }
            if (this.weight == a.weight) {
                return true;
            }
            return false;
        }
    }

    @Deprecated
    public class Parameters {
        public static final String ANTIBANDING_50HZ = "50hz";
        public static final String ANTIBANDING_60HZ = "60hz";
        public static final String ANTIBANDING_AUTO = "auto";
        public static final String ANTIBANDING_OFF = "off";
        public static final String EFFECT_AQUA = "aqua";
        public static final String EFFECT_BLACKBOARD = "blackboard";
        public static final String EFFECT_MONO = "mono";
        public static final String EFFECT_NEGATIVE = "negative";
        public static final String EFFECT_NONE = "none";
        public static final String EFFECT_POSTERIZE = "posterize";
        public static final String EFFECT_SEPIA = "sepia";
        public static final String EFFECT_SOLARIZE = "solarize";
        public static final String EFFECT_WHITEBOARD = "whiteboard";
        private static final String FALSE = "false";
        public static final String FLASH_MODE_AUTO = "auto";
        public static final String FLASH_MODE_OFF = "off";
        public static final String FLASH_MODE_ON = "on";
        public static final String FLASH_MODE_RED_EYE = "red-eye";
        public static final String FLASH_MODE_TORCH = "torch";
        public static final int FOCUS_DISTANCE_FAR_INDEX = 2;
        public static final int FOCUS_DISTANCE_NEAR_INDEX = 0;
        public static final int FOCUS_DISTANCE_OPTIMAL_INDEX = 1;
        public static final String FOCUS_MODE_AUTO = "auto";
        public static final String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";
        public static final String FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video";
        public static final String FOCUS_MODE_EDOF = "edof";
        public static final String FOCUS_MODE_FIXED = "fixed";
        public static final String FOCUS_MODE_INFINITY = "infinity";
        public static final String FOCUS_MODE_MACRO = "macro";
        private static final String KEY_ANTIBANDING = "antibanding";
        private static final String KEY_AUTO_EXPOSURE_LOCK = "auto-exposure-lock";
        private static final String KEY_AUTO_EXPOSURE_LOCK_SUPPORTED = "auto-exposure-lock-supported";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK = "auto-whitebalance-lock";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED = "auto-whitebalance-lock-supported";
        private static final String KEY_EFFECT = "effect";
        private static final String KEY_EXPOSURE_COMPENSATION = "exposure-compensation";
        private static final String KEY_EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
        private static final String KEY_FLASH_MODE = "flash-mode";
        private static final String KEY_FOCAL_LENGTH = "focal-length";
        private static final String KEY_FOCUS_AREAS = "focus-areas";
        private static final String KEY_FOCUS_DISTANCES = "focus-distances";
        private static final String KEY_FOCUS_MODE = "focus-mode";
        private static final String KEY_GPS_ALTITUDE = "gps-altitude";
        private static final String KEY_GPS_LATITUDE = "gps-latitude";
        private static final String KEY_GPS_LONGITUDE = "gps-longitude";
        private static final String KEY_GPS_PROCESSING_METHOD = "gps-processing-method";
        private static final String KEY_GPS_TIMESTAMP = "gps-timestamp";
        private static final String KEY_HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";
        private static final String KEY_JPEG_QUALITY = "jpeg-quality";
        private static final String KEY_JPEG_THUMBNAIL_HEIGHT = "jpeg-thumbnail-height";
        private static final String KEY_JPEG_THUMBNAIL_QUALITY = "jpeg-thumbnail-quality";
        private static final String KEY_JPEG_THUMBNAIL_SIZE = "jpeg-thumbnail-size";
        private static final String KEY_JPEG_THUMBNAIL_WIDTH = "jpeg-thumbnail-width";
        private static final String KEY_MAX_EXPOSURE_COMPENSATION = "max-exposure-compensation";
        private static final String KEY_MAX_NUM_DETECTED_FACES_HW = "max-num-detected-faces-hw";
        private static final String KEY_MAX_NUM_DETECTED_FACES_SW = "max-num-detected-faces-sw";
        private static final String KEY_MAX_NUM_FOCUS_AREAS = "max-num-focus-areas";
        private static final String KEY_MAX_NUM_METERING_AREAS = "max-num-metering-areas";
        private static final String KEY_MAX_ZOOM = "max-zoom";
        private static final String KEY_METERING_AREAS = "metering-areas";
        private static final String KEY_MIN_EXPOSURE_COMPENSATION = "min-exposure-compensation";
        private static final String KEY_PICTURE_FORMAT = "picture-format";
        private static final String KEY_PICTURE_SIZE = "picture-size";
        private static final String KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO = "preferred-preview-size-for-video";
        private static final String KEY_PREVIEW_FORMAT = "preview-format";
        private static final String KEY_PREVIEW_FPS_RANGE = "preview-fps-range";
        private static final String KEY_PREVIEW_FRAME_RATE = "preview-frame-rate";
        private static final String KEY_PREVIEW_SIZE = "preview-size";
        private static final String KEY_RECORDING_HINT = "recording-hint";
        private static final String KEY_ROTATION = "rotation";
        private static final String KEY_SCENE_MODE = "scene-mode";
        private static final String KEY_SMOOTH_ZOOM_SUPPORTED = "smooth-zoom-supported";
        private static final String KEY_VERTICAL_VIEW_ANGLE = "vertical-view-angle";
        private static final String KEY_VIDEO_SIZE = "video-size";
        private static final String KEY_VIDEO_SNAPSHOT_SUPPORTED = "video-snapshot-supported";
        private static final String KEY_VIDEO_STABILIZATION = "video-stabilization";
        private static final String KEY_VIDEO_STABILIZATION_SUPPORTED = "video-stabilization-supported";
        private static final String KEY_WHITE_BALANCE = "whitebalance";
        private static final String KEY_ZOOM = "zoom";
        private static final String KEY_ZOOM_RATIOS = "zoom-ratios";
        private static final String KEY_ZOOM_SUPPORTED = "zoom-supported";
        private static final String PIXEL_FORMAT_BAYER_RGGB = "bayer-rggb";
        private static final String PIXEL_FORMAT_JPEG = "jpeg";
        private static final String PIXEL_FORMAT_RGB565 = "rgb565";
        private static final String PIXEL_FORMAT_YUV420P = "yuv420p";
        private static final String PIXEL_FORMAT_YUV420SP = "yuv420sp";
        private static final String PIXEL_FORMAT_YUV422I = "yuv422i-yuyv";
        private static final String PIXEL_FORMAT_YUV422SP = "yuv422sp";
        public static final int PREVIEW_FPS_MAX_INDEX = 1;
        public static final int PREVIEW_FPS_MIN_INDEX = 0;
        public static final String SCENE_MODE_ACTION = "action";
        public static final String SCENE_MODE_AUTO = "auto";
        public static final String SCENE_MODE_BARCODE = "barcode";
        public static final String SCENE_MODE_BEACH = "beach";
        public static final String SCENE_MODE_CANDLELIGHT = "candlelight";
        public static final String SCENE_MODE_FIREWORKS = "fireworks";
        public static final String SCENE_MODE_HDR = "hdr";
        public static final String SCENE_MODE_LANDSCAPE = "landscape";
        public static final String SCENE_MODE_NIGHT = "night";
        public static final String SCENE_MODE_NIGHT_PORTRAIT = "night-portrait";
        public static final String SCENE_MODE_PARTY = "party";
        public static final String SCENE_MODE_PORTRAIT = "portrait";
        public static final String SCENE_MODE_SNOW = "snow";
        public static final String SCENE_MODE_SPORTS = "sports";
        public static final String SCENE_MODE_STEADYPHOTO = "steadyphoto";
        public static final String SCENE_MODE_SUNSET = "sunset";
        public static final String SCENE_MODE_THEATRE = "theatre";
        private static final String SUPPORTED_VALUES_SUFFIX = "-values";
        private static final String TRUE = "true";
        public static final String WHITE_BALANCE_AUTO = "auto";
        public static final String WHITE_BALANCE_CLOUDY_DAYLIGHT = "cloudy-daylight";
        public static final String WHITE_BALANCE_DAYLIGHT = "daylight";
        public static final String WHITE_BALANCE_FLUORESCENT = "fluorescent";
        public static final String WHITE_BALANCE_INCANDESCENT = "incandescent";
        public static final String WHITE_BALANCE_SHADE = "shade";
        public static final String WHITE_BALANCE_TWILIGHT = "twilight";
        public static final String WHITE_BALANCE_WARM_FLUORESCENT = "warm-fluorescent";
        private LinkedHashMap<String, String> mMap;

        private Parameters() {
            this.mMap = new LinkedHashMap<>(128);
        }

        public Parameters copy() {
            Parameters para = new Parameters();
            para.mMap = new LinkedHashMap<>(this.mMap);
            return para;
        }

        @UnsupportedAppUsage
        public void copyFrom(Parameters other) {
            if (other != null) {
                this.mMap.putAll(other.mMap);
                return;
            }
            throw new NullPointerException("other must not be null");
        }

        /* access modifiers changed from: private */
        public Camera getOuter() {
            return Camera.this;
        }

        public boolean same(Parameters other) {
            if (this == other) {
                return true;
            }
            if (other == null || !this.mMap.equals(other.mMap)) {
                return false;
            }
            return true;
        }

        @UnsupportedAppUsage
        @Deprecated
        public void dump() {
            Log.e(Camera.TAG, "dump: size=" + this.mMap.size());
            for (String k : this.mMap.keySet()) {
                Log.e(Camera.TAG, "dump: " + k + "=" + this.mMap.get(k));
            }
        }

        public String flatten() {
            StringBuilder flattened = new StringBuilder(128);
            for (String k : this.mMap.keySet()) {
                flattened.append(k);
                flattened.append("=");
                flattened.append(this.mMap.get(k));
                flattened.append(";");
            }
            flattened.deleteCharAt(flattened.length() - 1);
            return flattened.toString();
        }

        public void unflatten(String flattened) {
            this.mMap.clear();
            TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(';');
            splitter.setString(flattened);
            for (String kv : splitter) {
                int pos = kv.indexOf(61);
                if (pos != -1) {
                    this.mMap.put(kv.substring(0, pos), kv.substring(pos + 1));
                }
            }
        }

        public void remove(String key) {
            this.mMap.remove(key);
        }

        public void set(String key, String value) {
            if (key.indexOf(61) != -1 || key.indexOf(59) != -1 || key.indexOf(0) != -1) {
                Log.e(Camera.TAG, "Key \"" + key + "\" contains invalid character (= or ; or \\0)");
            } else if (value.indexOf(61) == -1 && value.indexOf(59) == -1 && value.indexOf(0) == -1) {
                put(key, value);
            } else {
                Log.e(Camera.TAG, "Value \"" + value + "\" contains invalid character (= or ; or \\0)");
            }
        }

        public void set(String key, int value) {
            put(key, Integer.toString(value));
        }

        private void put(String key, String value) {
            this.mMap.remove(key);
            this.mMap.put(key, value);
        }

        private void set(String key, List<Area> areas) {
            if (areas == null) {
                set(key, "(0,0,0,0,0)");
                return;
            }
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < areas.size(); i++) {
                Area area = areas.get(i);
                Rect rect = area.rect;
                buffer.append('(');
                buffer.append(rect.left);
                buffer.append(',');
                buffer.append(rect.top);
                buffer.append(',');
                buffer.append(rect.right);
                buffer.append(',');
                buffer.append(rect.bottom);
                buffer.append(',');
                buffer.append(area.weight);
                buffer.append(')');
                if (i != areas.size() - 1) {
                    buffer.append(',');
                }
            }
            set(key, buffer.toString());
        }

        public String get(String key) {
            return this.mMap.get(key);
        }

        public int getInt(String key) {
            return Integer.parseInt(this.mMap.get(key));
        }

        public void setPreviewSize(int width, int height) {
            set(KEY_PREVIEW_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPreviewSize() {
            return strToSize(get(KEY_PREVIEW_SIZE));
        }

        public List<Size> getSupportedPreviewSizes() {
            return splitSize(get("preview-size-values"));
        }

        public List<Size> getSupportedVideoSizes() {
            return splitSize(get("video-size-values"));
        }

        public Size getPreferredPreviewSizeForVideo() {
            return strToSize(get(KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO));
        }

        public void setJpegThumbnailSize(int width, int height) {
            set(KEY_JPEG_THUMBNAIL_WIDTH, width);
            set(KEY_JPEG_THUMBNAIL_HEIGHT, height);
        }

        public Size getJpegThumbnailSize() {
            return new Size(getInt(KEY_JPEG_THUMBNAIL_WIDTH), getInt(KEY_JPEG_THUMBNAIL_HEIGHT));
        }

        public List<Size> getSupportedJpegThumbnailSizes() {
            return splitSize(get("jpeg-thumbnail-size-values"));
        }

        public void setJpegThumbnailQuality(int quality) {
            set(KEY_JPEG_THUMBNAIL_QUALITY, quality);
        }

        public int getJpegThumbnailQuality() {
            return getInt(KEY_JPEG_THUMBNAIL_QUALITY);
        }

        public void setJpegQuality(int quality) {
            set(KEY_JPEG_QUALITY, quality);
        }

        public int getJpegQuality() {
            return getInt(KEY_JPEG_QUALITY);
        }

        @Deprecated
        public void setPreviewFrameRate(int fps) {
            set(KEY_PREVIEW_FRAME_RATE, fps);
        }

        @Deprecated
        public int getPreviewFrameRate() {
            return getInt(KEY_PREVIEW_FRAME_RATE);
        }

        @Deprecated
        public List<Integer> getSupportedPreviewFrameRates() {
            return splitInt(get("preview-frame-rate-values"));
        }

        public void setPreviewFpsRange(int min, int max) {
            set(KEY_PREVIEW_FPS_RANGE, "" + min + SmsManager.REGEX_PREFIX_DELIMITER + max);
        }

        public void getPreviewFpsRange(int[] range) {
            if (range == null || range.length != 2) {
                throw new IllegalArgumentException("range must be an array with two elements.");
            }
            splitInt(get(KEY_PREVIEW_FPS_RANGE), range);
        }

        public List<int[]> getSupportedPreviewFpsRange() {
            return splitRange(get("preview-fps-range-values"));
        }

        public void setPreviewFormat(int pixel_format) {
            String s = cameraFormatForPixelFormat(pixel_format);
            if (s != null) {
                set(KEY_PREVIEW_FORMAT, s);
                return;
            }
            throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
        }

        public int getPreviewFormat() {
            return pixelFormatForCameraFormat(get(KEY_PREVIEW_FORMAT));
        }

        public List<Integer> getSupportedPreviewFormats() {
            String str = get("preview-format-values");
            ArrayList<Integer> formats = new ArrayList<>();
            Iterator<String> it = split(str).iterator();
            while (it.hasNext()) {
                int f = pixelFormatForCameraFormat(it.next());
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        public void setPictureSize(int width, int height) {
            set(KEY_PICTURE_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPictureSize() {
            return strToSize(get(KEY_PICTURE_SIZE));
        }

        public List<Size> getSupportedPictureSizes() {
            return splitSize(get("picture-size-values"));
        }

        public void setPictureFormat(int pixel_format) {
            String s = cameraFormatForPixelFormat(pixel_format);
            if (s != null) {
                set(KEY_PICTURE_FORMAT, s);
                return;
            }
            throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
        }

        public int getPictureFormat() {
            return pixelFormatForCameraFormat(get(KEY_PICTURE_FORMAT));
        }

        public List<Integer> getSupportedPictureFormats() {
            String str = get("picture-format-values");
            ArrayList<Integer> formats = new ArrayList<>();
            Iterator<String> it = split(str).iterator();
            while (it.hasNext()) {
                int f = pixelFormatForCameraFormat(it.next());
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        private String cameraFormatForPixelFormat(int pixel_format) {
            if (pixel_format == 4) {
                return PIXEL_FORMAT_RGB565;
            }
            if (pixel_format == 20) {
                return PIXEL_FORMAT_YUV422I;
            }
            if (pixel_format == 256) {
                return PIXEL_FORMAT_JPEG;
            }
            if (pixel_format == 842094169) {
                return PIXEL_FORMAT_YUV420P;
            }
            if (pixel_format == 16) {
                return PIXEL_FORMAT_YUV422SP;
            }
            if (pixel_format != 17) {
                return null;
            }
            return PIXEL_FORMAT_YUV420SP;
        }

        private int pixelFormatForCameraFormat(String format) {
            if (format == null) {
                return 0;
            }
            if (format.equals(PIXEL_FORMAT_YUV422SP)) {
                return 16;
            }
            if (format.equals(PIXEL_FORMAT_YUV420SP)) {
                return 17;
            }
            if (format.equals(PIXEL_FORMAT_YUV422I)) {
                return 20;
            }
            if (format.equals(PIXEL_FORMAT_YUV420P)) {
                return ImageFormat.YV12;
            }
            if (format.equals(PIXEL_FORMAT_RGB565)) {
                return 4;
            }
            if (format.equals(PIXEL_FORMAT_JPEG)) {
                return 256;
            }
            return 0;
        }

        public void setRotation(int rotation) {
            if (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
                set(KEY_ROTATION, Integer.toString(rotation));
                return;
            }
            throw new IllegalArgumentException("Invalid rotation=" + rotation);
        }

        public void setGpsLatitude(double latitude) {
            set(KEY_GPS_LATITUDE, Double.toString(latitude));
        }

        public void setGpsLongitude(double longitude) {
            set(KEY_GPS_LONGITUDE, Double.toString(longitude));
        }

        public void setGpsAltitude(double altitude) {
            set(KEY_GPS_ALTITUDE, Double.toString(altitude));
        }

        public void setGpsTimestamp(long timestamp) {
            set(KEY_GPS_TIMESTAMP, Long.toString(timestamp));
        }

        public void setGpsProcessingMethod(String processing_method) {
            set(KEY_GPS_PROCESSING_METHOD, processing_method);
        }

        public void removeGpsData() {
            remove(KEY_GPS_LATITUDE);
            remove(KEY_GPS_LONGITUDE);
            remove(KEY_GPS_ALTITUDE);
            remove(KEY_GPS_TIMESTAMP);
            remove(KEY_GPS_PROCESSING_METHOD);
        }

        public String getWhiteBalance() {
            return get(KEY_WHITE_BALANCE);
        }

        public void setWhiteBalance(String value) {
            if (!same(value, get(KEY_WHITE_BALANCE))) {
                set(KEY_WHITE_BALANCE, value);
                set(KEY_AUTO_WHITEBALANCE_LOCK, FALSE);
            }
        }

        public List<String> getSupportedWhiteBalance() {
            return split(get("whitebalance-values"));
        }

        public String getColorEffect() {
            return get(KEY_EFFECT);
        }

        public void setColorEffect(String value) {
            set(KEY_EFFECT, value);
        }

        public List<String> getSupportedColorEffects() {
            return split(get("effect-values"));
        }

        public String getAntibanding() {
            return get(KEY_ANTIBANDING);
        }

        public void setAntibanding(String antibanding) {
            set(KEY_ANTIBANDING, antibanding);
        }

        public List<String> getSupportedAntibanding() {
            return split(get("antibanding-values"));
        }

        public String getSceneMode() {
            return get(KEY_SCENE_MODE);
        }

        public void setSceneMode(String value) {
            set(KEY_SCENE_MODE, value);
        }

        public List<String> getSupportedSceneModes() {
            return split(get("scene-mode-values"));
        }

        public String getFlashMode() {
            return get(KEY_FLASH_MODE);
        }

        public void setFlashMode(String value) {
            set(KEY_FLASH_MODE, value);
        }

        public List<String> getSupportedFlashModes() {
            return split(get("flash-mode-values"));
        }

        public String getFocusMode() {
            return get(KEY_FOCUS_MODE);
        }

        public void setFocusMode(String value) {
            set(KEY_FOCUS_MODE, value);
        }

        public List<String> getSupportedFocusModes() {
            return split(get("focus-mode-values"));
        }

        public float getFocalLength() {
            return Float.parseFloat(get(KEY_FOCAL_LENGTH));
        }

        public float getHorizontalViewAngle() {
            return Float.parseFloat(get(KEY_HORIZONTAL_VIEW_ANGLE));
        }

        public float getVerticalViewAngle() {
            return Float.parseFloat(get(KEY_VERTICAL_VIEW_ANGLE));
        }

        public int getExposureCompensation() {
            return getInt(KEY_EXPOSURE_COMPENSATION, 0);
        }

        public void setExposureCompensation(int value) {
            set(KEY_EXPOSURE_COMPENSATION, value);
        }

        public int getMaxExposureCompensation() {
            return getInt(KEY_MAX_EXPOSURE_COMPENSATION, 0);
        }

        public int getMinExposureCompensation() {
            return getInt(KEY_MIN_EXPOSURE_COMPENSATION, 0);
        }

        public float getExposureCompensationStep() {
            return getFloat(KEY_EXPOSURE_COMPENSATION_STEP, 0.0f);
        }

        public void setAutoExposureLock(boolean toggle) {
            set(KEY_AUTO_EXPOSURE_LOCK, toggle ? TRUE : FALSE);
        }

        public boolean getAutoExposureLock() {
            return TRUE.equals(get(KEY_AUTO_EXPOSURE_LOCK));
        }

        public boolean isAutoExposureLockSupported() {
            return TRUE.equals(get(KEY_AUTO_EXPOSURE_LOCK_SUPPORTED));
        }

        public void setAutoWhiteBalanceLock(boolean toggle) {
            set(KEY_AUTO_WHITEBALANCE_LOCK, toggle ? TRUE : FALSE);
        }

        public boolean getAutoWhiteBalanceLock() {
            return TRUE.equals(get(KEY_AUTO_WHITEBALANCE_LOCK));
        }

        public boolean isAutoWhiteBalanceLockSupported() {
            return TRUE.equals(get(KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED));
        }

        public int getZoom() {
            return getInt(KEY_ZOOM, 0);
        }

        public void setZoom(int value) {
            set(KEY_ZOOM, value);
        }

        public boolean isZoomSupported() {
            return TRUE.equals(get(KEY_ZOOM_SUPPORTED));
        }

        public int getMaxZoom() {
            return getInt(KEY_MAX_ZOOM, 0);
        }

        public List<Integer> getZoomRatios() {
            return splitInt(get(KEY_ZOOM_RATIOS));
        }

        public boolean isSmoothZoomSupported() {
            return TRUE.equals(get(KEY_SMOOTH_ZOOM_SUPPORTED));
        }

        public void getFocusDistances(float[] output) {
            if (output == null || output.length != 3) {
                throw new IllegalArgumentException("output must be a float array with three elements.");
            }
            splitFloat(get(KEY_FOCUS_DISTANCES), output);
        }

        public int getMaxNumFocusAreas() {
            return getInt(KEY_MAX_NUM_FOCUS_AREAS, 0);
        }

        public List<Area> getFocusAreas() {
            return splitArea(get(KEY_FOCUS_AREAS));
        }

        public void setFocusAreas(List<Area> focusAreas) {
            set(KEY_FOCUS_AREAS, focusAreas);
        }

        public int getMaxNumMeteringAreas() {
            return getInt(KEY_MAX_NUM_METERING_AREAS, 0);
        }

        public List<Area> getMeteringAreas() {
            return splitArea(get(KEY_METERING_AREAS));
        }

        public void setMeteringAreas(List<Area> meteringAreas) {
            set(KEY_METERING_AREAS, meteringAreas);
        }

        public int getMaxNumDetectedFaces() {
            return getInt(KEY_MAX_NUM_DETECTED_FACES_HW, 0);
        }

        public void setRecordingHint(boolean hint) {
            set(KEY_RECORDING_HINT, hint ? TRUE : FALSE);
        }

        public boolean isVideoSnapshotSupported() {
            return TRUE.equals(get(KEY_VIDEO_SNAPSHOT_SUPPORTED));
        }

        public void setVideoStabilization(boolean toggle) {
            set(KEY_VIDEO_STABILIZATION, toggle ? TRUE : FALSE);
        }

        public boolean getVideoStabilization() {
            return TRUE.equals(get(KEY_VIDEO_STABILIZATION));
        }

        public boolean isVideoStabilizationSupported() {
            return TRUE.equals(get(KEY_VIDEO_STABILIZATION_SUPPORTED));
        }

        private ArrayList<String> split(String str) {
            if (str == null) {
                return null;
            }
            TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<String> substrings = new ArrayList<>();
            for (String s : splitter) {
                substrings.add(s);
            }
            return substrings;
        }

        private ArrayList<Integer> splitInt(String str) {
            if (str == null) {
                return null;
            }
            TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Integer> substrings = new ArrayList<>();
            for (String s : splitter) {
                substrings.add(Integer.valueOf(Integer.parseInt(s)));
            }
            if (substrings.size() == 0) {
                return null;
            }
            return substrings;
        }

        private void splitInt(String str, int[] output) {
            if (str != null) {
                TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(',');
                splitter.setString(str);
                int index = 0;
                for (String s : splitter) {
                    output[index] = Integer.parseInt(s);
                    index++;
                }
            }
        }

        private void splitFloat(String str, float[] output) {
            if (str != null) {
                TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(',');
                splitter.setString(str);
                int index = 0;
                for (String s : splitter) {
                    output[index] = Float.parseFloat(s);
                    index++;
                }
            }
        }

        private float getFloat(String key, float defaultValue) {
            try {
                return Float.parseFloat(this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private int getInt(String key, int defaultValue) {
            try {
                return Integer.parseInt(this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private ArrayList<Size> splitSize(String str) {
            if (str == null) {
                return null;
            }
            TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Size> sizeList = new ArrayList<>();
            for (String s : splitter) {
                Size size = strToSize(s);
                if (size != null) {
                    sizeList.add(size);
                }
            }
            if (sizeList.size() == 0) {
                return null;
            }
            return sizeList;
        }

        private Size strToSize(String str) {
            if (str == null) {
                return null;
            }
            int pos = str.indexOf(120);
            if (pos != -1) {
                return new Size(Integer.parseInt(str.substring(0, pos)), Integer.parseInt(str.substring(pos + 1)));
            }
            Log.e(Camera.TAG, "Invalid size parameter string=" + str);
            return null;
        }

        private ArrayList<int[]> splitRange(String str) {
            int endIndex;
            if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
                ArrayList<int[]> rangeList = new ArrayList<>();
                int fromIndex = 1;
                do {
                    int[] range = new int[2];
                    endIndex = str.indexOf("),(", fromIndex);
                    if (endIndex == -1) {
                        endIndex = str.length() - 1;
                    }
                    splitInt(str.substring(fromIndex, endIndex), range);
                    rangeList.add(range);
                    fromIndex = endIndex + 3;
                } while (endIndex != str.length() - 1);
                if (rangeList.size() == 0) {
                    return null;
                }
                return rangeList;
            }
            Log.e(Camera.TAG, "Invalid range list string=" + str);
            return null;
        }

        @UnsupportedAppUsage
        private ArrayList<Area> splitArea(String str) {
            int endIndex;
            if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
                ArrayList<Area> result = new ArrayList<>();
                int fromIndex = 1;
                int[] array = new int[5];
                do {
                    endIndex = str.indexOf("),(", fromIndex);
                    if (endIndex == -1) {
                        endIndex = str.length() - 1;
                    }
                    splitInt(str.substring(fromIndex, endIndex), array);
                    result.add(new Area(new Rect(array[0], array[1], array[2], array[3]), array[4]));
                    fromIndex = endIndex + 3;
                } while (endIndex != str.length() - 1);
                if (result.size() == 0) {
                    return null;
                }
                if (result.size() == 1) {
                    Area area = result.get(0);
                    Rect rect = area.rect;
                    if (rect.left == 0 && rect.top == 0 && rect.right == 0 && rect.bottom == 0 && area.weight == 0) {
                        return null;
                    }
                    return result;
                }
                return result;
            }
            Log.e(Camera.TAG, "Invalid area string=" + str);
            return null;
        }

        private boolean same(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return true;
            }
            if (s1 == null || !s1.equals(s2)) {
                return false;
            }
            return true;
        }
    }

    public void setVendorDataCallback(VendorDataCallback callback) {
        this.mVendorDataCallback = callback;
    }

    public final void setAutoRamaCallback(AutoRamaCallback cb) {
        this.mAutoRamaCallback = cb;
    }

    public final void setAutoRamaMoveCallback(AutoRamaMoveCallback cb) {
        this.mAutoRamaMoveCallback = cb;
    }

    public final void startAutoRama(int num) {
        startAUTORAMA(num);
    }

    public void setContinuousShotCallback(ContinuousShotCallback callback) {
        this.mCSDoneCallback = callback;
    }

    public void stopAutoRama(int isMerge) {
        stopAUTORAMA(isMerge);
    }

    public final void setAsdCallback(AsdCallback cb) {
        this.mAsdCallback = cb;
    }

    public final void setAFDataCallback(AFDataCallback cb) {
        this.mAFDataCallback = cb;
    }

    public final void setFbOriginalCallback(FbOriginalCallback cb) {
        this.mFbOriginalCallback = cb;
    }

    public final void setDistanceInfoCallback(DistanceInfoCallback cb) {
        this.mDistanceInfoCallback = cb;
    }

    public final void setStereoCameraDataCallback(StereoCameraDataCallback cb) {
        this.mStereoCameraDataCallback = cb;
    }

    public final void setStereoCameraWarningCallback(StereoCameraWarningCallback cb) {
        this.mStereoCameraWarningCallback = cb;
    }

    public final void setUncompressedImageCallback(PictureCallback cb) {
        this.mUncompressedImageCallback = cb;
    }

    /* access modifiers changed from: private */
    public void handleExtNotify(Message msg, Camera camera) {
        int warnType;
        int i = msg.arg1;
        if (i == 2) {
            AsdCallback asdCallback = this.mAsdCallback;
            if (asdCallback != null) {
                asdCallback.onDetected(msg.arg2);
                return;
            }
        } else if (i == 6) {
            ContinuousShotCallback continuousShotCallback = this.mCSDoneCallback;
            if (continuousShotCallback != null) {
                continuousShotCallback.onConinuousShotDone(msg.arg2);
                return;
            }
        } else if (i == 23) {
            PictureCallback pictureCallback = this.mUncompressedImageCallback;
            if (pictureCallback != null) {
                pictureCallback.onPictureTaken(null, camera);
                return;
            }
        } else if (i != 20) {
            if (i != 21) {
                Log.e(TAG, "Unknown MTK-extended notify message type " + msg.arg1);
            } else if (this.mDistanceInfoCallback != null) {
                String info = String.valueOf(msg.arg2);
                if (info != null) {
                    this.mDistanceInfoCallback.onInfo(info);
                    return;
                }
                return;
            }
        } else if (this.mStereoCameraWarningCallback != null) {
            int message = msg.arg2;
            int[] type = new int[3];
            for (int i2 = 0; i2 < 3; i2++) {
                type[i2] = message & 1;
                message >>= 1;
            }
            if (type[0] == 1) {
                warnType = 0;
            } else if (type[2] == 1) {
                warnType = 2;
            } else if (type[1] == 1) {
                warnType = 1;
            } else {
                warnType = 3;
            }
            if (warnType != -1) {
                this.mStereoCameraWarningCallback.onWarning(warnType);
                return;
            }
            return;
        }
        VendorDataCallback vendorDataCallback = this.mVendorDataCallback;
        if (vendorDataCallback != null) {
            vendorDataCallback.onDataCallback(msg);
        }
    }

    /* access modifiers changed from: private */
    public void handleExtData(Message msg, Camera camera) {
        PictureCallback pictureCallback;
        int i = msg.arg1;
        if (i == 1) {
            byte[] byteArray = (byte[]) msg.obj;
            byte[] byteHead = new byte[16];
            System.arraycopy(byteArray, 0, byteHead, 0, 16);
            IntBuffer intBuf = ByteBuffer.wrap(byteHead).order(ByteOrder.nativeOrder()).asIntBuffer();
            if (intBuf.get(0) == 0) {
                if (this.mAutoRamaMoveCallback != null) {
                    this.mAutoRamaMoveCallback.onFrame(((intBuf.get(1) & 65535) << 16) + (65535 & intBuf.get(2)), intBuf.get(3));
                    return;
                }
            } else if (this.mAutoRamaCallback != null) {
                if (1 == intBuf.get(0)) {
                    this.mAutoRamaCallback.onCapture(null);
                    return;
                } else if (2 == intBuf.get(0)) {
                    byte[] jpegData = new byte[(byteArray.length - 4)];
                    System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                    this.mAutoRamaCallback.onCapture(jpegData);
                    return;
                } else {
                    return;
                }
            }
        } else if (i != 2) {
            if (i != 6) {
                if (i != 25) {
                    if (i != 32) {
                        if (i != 17) {
                            if (i != 18) {
                                switch (i) {
                                    case 20:
                                        if (this.mStereoCameraDataCallback != null) {
                                            byte[] byteArray2 = (byte[]) msg.obj;
                                            byte[] jpegData2 = new byte[(byteArray2.length - 4)];
                                            System.arraycopy(byteArray2, 4, jpegData2, 0, byteArray2.length - 4);
                                            this.mStereoCameraDataCallback.onDepthMapCapture(jpegData2);
                                            return;
                                        }
                                        break;
                                    case 21:
                                        if (this.mStereoCameraDataCallback != null) {
                                            byte[] byteArray3 = (byte[]) msg.obj;
                                            byte[] jpegData3 = new byte[(byteArray3.length - 4)];
                                            System.arraycopy(byteArray3, 4, jpegData3, 0, byteArray3.length - 4);
                                            this.mStereoCameraDataCallback.onClearImageCapture(jpegData3);
                                            return;
                                        }
                                        break;
                                    case 22:
                                        if (this.mStereoCameraDataCallback != null) {
                                            byte[] byteArray4 = (byte[]) msg.obj;
                                            byte[] jpegData4 = new byte[(byteArray4.length - 4)];
                                            System.arraycopy(byteArray4, 4, jpegData4, 0, byteArray4.length - 4);
                                            this.mStereoCameraDataCallback.onLdcCapture(jpegData4);
                                            return;
                                        }
                                        break;
                                    default:
                                        Log.e(TAG, "Unknown MTK-extended data message type " + msg.arg1);
                                        break;
                                }
                            } else if (this.mStereoCameraDataCallback != null) {
                                byte[] byteArray5 = (byte[]) msg.obj;
                                byte[] jpegData5 = new byte[(byteArray5.length - 4)];
                                System.arraycopy(byteArray5, 4, jpegData5, 0, byteArray5.length - 4);
                                this.mStereoCameraDataCallback.onMaskCapture(jpegData5);
                                return;
                            }
                        } else if (this.mStereoCameraDataCallback != null) {
                            byte[] byteArray6 = (byte[]) msg.obj;
                            byte[] jpegData6 = new byte[(byteArray6.length - 4)];
                            System.arraycopy(byteArray6, 4, jpegData6, 0, byteArray6.length - 4);
                            this.mStereoCameraDataCallback.onJpsCapture(jpegData6);
                            return;
                        }
                    } else if (this.mStereoCameraDataCallback != null) {
                        byte[] byteArray7 = (byte[]) msg.obj;
                        byte[] jpegData7 = new byte[(byteArray7.length - 4)];
                        System.arraycopy(byteArray7, 4, jpegData7, 0, byteArray7.length - 4);
                        this.mStereoCameraDataCallback.onDepthWrapperCapture(jpegData7);
                        return;
                    }
                } else if (this.mStereoCameraDataCallback != null) {
                    byte[] byteArray8 = (byte[]) msg.obj;
                    byte[] jpegData8 = new byte[(byteArray8.length - 4)];
                    System.arraycopy(byteArray8, 4, jpegData8, 0, byteArray8.length - 4);
                    this.mStereoCameraDataCallback.onN3dCapture(jpegData8);
                    return;
                }
            } else if (this.mFbOriginalCallback != null) {
                byte[] byteArray9 = (byte[]) msg.obj;
                byte[] jpegData9 = new byte[(byteArray9.length - 4)];
                System.arraycopy(byteArray9, 4, jpegData9, 0, byteArray9.length - 4);
                if (SystemProperties.getInt("ro.mtk_cam_vfb", 0) != 1 || (pictureCallback = this.mJpegCallback) == null) {
                    this.mFbOriginalCallback.onCapture(jpegData9);
                    return;
                } else {
                    pictureCallback.onPictureTaken(jpegData9, camera);
                    return;
                }
            }
        } else if (this.mAFDataCallback != null) {
            this.mAFDataCallback.onAFData((byte[]) msg.obj, camera);
            return;
        }
        VendorDataCallback vendorDataCallback = this.mVendorDataCallback;
        if (vendorDataCallback != null) {
            vendorDataCallback.onDataCallback(msg);
        }
    }

    public static String getProperty(String key, String def) {
        return native_getProperty(key, def);
    }

    public static void setProperty(String key, String val) {
        native_setProperty(key, val);
    }

    private void printParameter(String parameters) {
        if (!Log.isLoggable(TAG, 3)) {
            return;
        }
        if (((long) parameters.length()) <= ((long) 1000)) {
            Log.d(TAG, parameters);
            return;
        }
        for (int i = 0; i < parameters.length(); i += 1000) {
            if (i + 1000 < parameters.length()) {
                Log.d(TAG, parameters.substring(i, i + 1000));
            } else {
                Log.d(TAG, parameters.substring(i, parameters.length()));
            }
        }
    }
}
