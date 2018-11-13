package android.hardware;

import android.app.ActivityThread;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.display.DisplayManagerGlobal;
import android.media.IAudioService.Stub;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Element.DataKind;
import android.renderscript.Element.DataType;
import android.renderscript.RSIllegalArgumentException;
import android.renderscript.RenderScript;
import android.renderscript.Type.Builder;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.util.Log;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.oppo.hypnus.HypnusManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import oppo.util.OppoStatistics;

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
@Deprecated
public class Camera {
    @Deprecated
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";
    @Deprecated
    public static final String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";
    public static final int CAMERA_ERROR_EVICTED = 2;
    public static final int CAMERA_ERROR_NO_MEMORY = 1000;
    public static final int CAMERA_ERROR_RESET = 1001;
    public static final int CAMERA_ERROR_SERVER_DIED = 100;
    public static final int CAMERA_ERROR_UNKNOWN = 1;
    private static final int CAMERA_FACE_DETECTION_HW = 0;
    private static final int CAMERA_FACE_DETECTION_SW = 1;
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
    private static final int CAMERA_TIME_HYPNUS_0 = 0;
    private static final int CAMERA_TIME_HYPNUS_1500 = 1500;
    private static final int CAMERA_TIME_HYPNUS_2500 = 2500;
    private static final int CAMERA_TIME_HYPNUS_4000 = 4000;
    private static final int MTK_CAMERA_MSG_EXT_DATA = 536870912;
    private static final int MTK_CAMERA_MSG_EXT_DATA_AF = 2;
    private static final int MTK_CAMERA_MSG_EXT_DATA_AUTORAMA = 1;
    private static final int MTK_CAMERA_MSG_EXT_DATA_BURST_SHOT = 3;
    private static final int MTK_CAMERA_MSG_EXT_DATA_FACEBEAUTY = 6;
    private static final int MTK_CAMERA_MSG_EXT_DATA_HDR = 8;
    private static final int MTK_CAMERA_MSG_EXT_DATA_JPS = 17;
    private static final int MTK_CAMERA_MSG_EXT_DATA_OT = 5;
    private static final int MTK_CAMERA_MSG_EXT_DATA_RAW16 = 19;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_CLEAR_IMAGE = 21;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DBG = 18;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DEPTHMAP = 20;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_DEPTHWRAPPER = 32;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_LDC = 22;
    private static final int MTK_CAMERA_MSG_EXT_DATA_STEREO_N3D = 25;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY = 1073741824;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_ASD = 2;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_BURST_SHUTTER = 4;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_CONTINUOUS_END = 6;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_GESTURE_DETECT = 19;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_IMAGE_UNCOMPRESSED = 23;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_METADATA_DONE = 22;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_RAW_DUMP_STOPPED = 18;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_STEREO_DISTANCE = 21;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_STEREO_WARNING = 20;
    private static final int MTK_CAMERA_MSG_EXT_NOTIFY_ZSD_PREVIEW_DONE = 7;
    private static final int NO_ERROR = 0;
    private static final String TAG = "CameraFramework";
    private static HypnusManager sHM;
    private AFDataCallback mAFDataCallback;
    private AsdCallback mAsdCallback;
    private AutoFocusCallback mAutoFocusCallback;
    private final Object mAutoFocusCallbackLock;
    private AutoFocusMoveCallback mAutoFocusMoveCallback;
    private AutoRamaCallback mAutoRamaCallback;
    private AutoRamaMoveCallback mAutoRamaMoveCallback;
    private ContinuousShotCallback mCSDoneCallback;
    private int mCameraId;
    private DistanceInfoCallback mDistanceInfoCallback;
    private boolean mEnableRaw16;
    private ErrorCallback mErrorCallback;
    private EventHandler mEventHandler;
    private boolean mFaceDetectionRunning;
    private FaceDetectionListener mFaceListener;
    private FbOriginalCallback mFbOriginalCallback;
    private GestureCallback mGestureCallback;
    private HdrOriginalCallback mHdrOriginalCallback;
    private boolean mHypnusCtrl;
    private PictureCallback mJpegCallback;
    private boolean mLongshotEnable;
    private MetadataCallback mMetadataCallbacks;
    private long mNativeContext;
    private final Object mObjectCallbackLock;
    private Face mObjectFace;
    private ObjectTrackingListener mObjectListener;
    private Rect mObjectRect;
    private boolean mOneShot;
    private PictureCallback mPostviewCallback;
    private PreviewCallback mPreviewCallback;
    private ZSDPreviewDone mPreviewDoneCallback;
    private PreviewRawDumpCallback mPreviewRawDumpCallback;
    private PictureCallback mRaw16Callbacks;
    private PictureCallback mRawImageCallback;
    private ShutterCallback mShutterCallback;
    private boolean mStereo3DModeForCamera;
    private StereoCameraDataCallback mStereoCameraDataCallback;
    private StereoCameraWarningCallback mStereoCameraWarningCallback;
    private PictureCallback mUncompressedImageCallback;
    private boolean mUsingPreviewAllocation;
    private VendorDataCallback mVendorDataCallback;
    private boolean mWithBuffer;
    private OnZoomChangeListener mZoomListener;

    public interface AFDataCallback {
        void onAFData(byte[] bArr, Camera camera);
    }

    @Deprecated
    public static class Area {
        public Rect rect;
        public int weight;

        public Area(Rect rect, int weight) {
            this.rect = rect;
            this.weight = weight;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Area)) {
                return false;
            }
            Area a = (Area) obj;
            if (this.rect == null) {
                if (a.rect != null) {
                    return false;
                }
            } else if (!this.rect.equals(a.rect)) {
                return false;
            }
            if (this.weight == a.weight) {
                z = true;
            }
            return z;
        }
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

    private class EventHandler extends Handler {
        private final Camera mCamera;

        public EventHandler(Camera c, Looper looper) {
            super(looper);
            this.mCamera = c;
        }

        /* JADX WARNING: Missing block: B:178:0x06cb, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            Log.i(Camera.TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case 1:
                    Log.e(Camera.TAG, "Error " + msg.arg1);
                    if (Camera.this.mErrorCallback != null) {
                        Camera.this.mErrorCallback.onError(msg.arg1, this.mCamera);
                    }
                    return;
                case 2:
                    if (Camera.this.mShutterCallback != null) {
                        Camera.this.mShutterCallback.onShutter();
                    }
                    return;
                case 4:
                    AutoFocusCallback cb;
                    synchronized (Camera.this.mAutoFocusCallbackLock) {
                        cb = Camera.this.mAutoFocusCallback;
                    }
                    if (cb != null) {
                        cb.onAutoFocus(msg.arg1 != 0, this.mCamera);
                    }
                    return;
                case 8:
                    if (Camera.this.mZoomListener != null) {
                        Camera.this.mZoomListener.onZoomChange(msg.arg1, msg.arg2 != 0, this.mCamera);
                    }
                    return;
                case 16:
                    PreviewCallback pCb = Camera.this.mPreviewCallback;
                    if (pCb != null) {
                        if (Camera.this.mOneShot) {
                            Camera.this.mPreviewCallback = null;
                        } else if (!Camera.this.mWithBuffer) {
                            Camera.this.setHasPreviewCallback(true, false);
                        }
                        pCb.onPreviewFrame((byte[]) msg.obj, this.mCamera);
                    }
                    return;
                case 64:
                    if (Camera.this.mPostviewCallback != null) {
                        Camera.this.mPostviewCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                    return;
                case 128:
                    if (Camera.this.mRawImageCallback != null) {
                        Camera.this.mRawImageCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                    return;
                case 256:
                    if (Camera.this.mJpegCallback != null) {
                        Camera.this.mJpegCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                        if (Camera.sHM == null) {
                            Camera.sHM = new HypnusManager();
                        }
                        if (!(Camera.sHM == null || !Camera.this.mHypnusCtrl || Camera.this.mLongshotEnable)) {
                            Camera.sHM.hypnusSetAction(15, 0);
                        }
                    }
                    return;
                case 1024:
                    if (Camera.this.mFaceListener != null) {
                        Camera.this.mFaceListener.onFaceDetection((Face[]) msg.obj, this.mCamera);
                    }
                    return;
                case 2048:
                    if (Camera.this.mAutoFocusMoveCallback != null) {
                        Camera.this.mAutoFocusMoveCallback.onAutoFocusMoving(msg.arg1 != 0, this.mCamera);
                    }
                    return;
                case 536870912:
                    byte[] byteArray;
                    IntBuffer intBuf;
                    byte[] jpegData;
                    switch (msg.arg1) {
                        case 1:
                            byteArray = msg.obj;
                            byte[] byteHead = new byte[16];
                            System.arraycopy(byteArray, 0, byteHead, 0, 16);
                            intBuf = ByteBuffer.wrap(byteHead).order(ByteOrder.nativeOrder()).asIntBuffer();
                            if (intBuf.get(0) == 0) {
                                if (Camera.this.mAutoRamaMoveCallback != null) {
                                    Camera.this.mAutoRamaMoveCallback.onFrame(((65535 & intBuf.get(1)) << 16) + (65535 & intBuf.get(2)), intBuf.get(3));
                                    return;
                                }
                            } else if (Camera.this.mAutoRamaCallback != null) {
                                if (1 == intBuf.get(0)) {
                                    Camera.this.mAutoRamaCallback.onCapture(null);
                                } else {
                                    if (2 == intBuf.get(0)) {
                                        jpegData = new byte[(byteArray.length - 4)];
                                        System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                        Camera.this.mAutoRamaCallback.onCapture(jpegData);
                                    }
                                }
                                return;
                            }
                            break;
                        case 2:
                            byteArray = (byte[]) msg.obj;
                            if (Camera.this.mAFDataCallback != null) {
                                Camera.this.mAFDataCallback.onAFData((byte[]) msg.obj, this.mCamera);
                                return;
                            }
                            break;
                        case 5:
                            intBuf = ByteBuffer.wrap((byte[]) msg.obj).order(ByteOrder.nativeOrder()).asIntBuffer();
                            synchronized (Camera.this.mObjectCallbackLock) {
                                if (Camera.this.mObjectListener != null) {
                                    if (intBuf.get(0) != 1) {
                                        Camera.this.mObjectListener.onObjectTracking(null, this.mCamera);
                                        break;
                                    }
                                    Camera.this.mObjectRect.left = intBuf.get(1);
                                    Camera.this.mObjectRect.top = intBuf.get(2);
                                    Camera.this.mObjectRect.right = intBuf.get(3);
                                    Camera.this.mObjectRect.bottom = intBuf.get(4);
                                    Camera.this.mObjectFace.rect = Camera.this.mObjectRect;
                                    Camera.this.mObjectFace.score = intBuf.get(5);
                                    Camera.this.mObjectListener.onObjectTracking(Camera.this.mObjectFace, this.mCamera);
                                    break;
                                }
                            }
                            break;
                        case 6:
                            if (Camera.this.mFbOriginalCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                if (SystemProperties.getInt("ro.mtk_cam_vfb", 0) != 1 || Camera.this.mJpegCallback == null) {
                                    Camera.this.mFbOriginalCallback.onCapture(jpegData);
                                } else {
                                    Camera.this.mJpegCallback.onPictureTaken(jpegData, this.mCamera);
                                }
                                return;
                            }
                            break;
                        case 8:
                            if (Camera.this.mHdrOriginalCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mHdrOriginalCallback.onCapture(jpegData);
                                return;
                            }
                            break;
                        case 17:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onJpsCapture(jpegData);
                                return;
                            }
                            break;
                        case 18:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onMaskCapture(jpegData);
                                return;
                            }
                            break;
                        case 19:
                            if (Camera.this.mEnableRaw16) {
                                Camera.this.mRaw16Callbacks.onPictureTaken((byte[]) msg.obj, this.mCamera);
                                return;
                            }
                            break;
                        case 20:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onDepthMapCapture(jpegData);
                                return;
                            }
                            break;
                        case 21:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onClearImageCapture(jpegData);
                                return;
                            }
                            break;
                        case 22:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onLdcCapture(jpegData);
                                return;
                            }
                            break;
                        case 25:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onN3dCapture(jpegData);
                                return;
                            }
                            break;
                        case 32:
                            if (Camera.this.mStereoCameraDataCallback != null) {
                                byteArray = (byte[]) msg.obj;
                                jpegData = new byte[(byteArray.length - 4)];
                                System.arraycopy(byteArray, 4, jpegData, 0, byteArray.length - 4);
                                Camera.this.mStereoCameraDataCallback.onDepthWrapperCapture(jpegData);
                                return;
                            }
                            break;
                        default:
                            Log.e(Camera.TAG, "Unknown MTK-extended data message type " + msg.arg1);
                            break;
                    }
                    if (Camera.this.mVendorDataCallback != null) {
                        Camera.this.mVendorDataCallback.onDataCallback(msg);
                    }
                    return;
                case 1073741824:
                    switch (msg.arg1) {
                        case 2:
                            if (Camera.this.mAsdCallback != null) {
                                Camera.this.mAsdCallback.onDetected(msg.arg2);
                                return;
                            }
                            break;
                        case 6:
                            if (Camera.this.mCSDoneCallback != null) {
                                Camera.this.mCSDoneCallback.onConinuousShotDone(msg.arg2);
                                return;
                            }
                            break;
                        case 7:
                            if (Camera.this.mPreviewDoneCallback != null) {
                                Camera.this.mPreviewDoneCallback.onPreviewDone();
                                return;
                            }
                            break;
                        case 18:
                            if (Camera.this.mPreviewRawDumpCallback != null) {
                                Camera.this.mPreviewRawDumpCallback.onNotify(18);
                                return;
                            }
                            break;
                        case 19:
                            if (Camera.this.mGestureCallback != null) {
                                Camera.this.mGestureCallback.onGesture();
                                return;
                            }
                            break;
                        case 20:
                            if (Camera.this.mStereoCameraWarningCallback != null) {
                                int warnType;
                                int message = msg.arg2;
                                int[] type = new int[3];
                                for (int i = 0; i < 3; i++) {
                                    type[i] = message & 1;
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
                                    Camera.this.mStereoCameraWarningCallback.onWarning(warnType);
                                }
                                return;
                            }
                            break;
                        case 21:
                            if (Camera.this.mDistanceInfoCallback != null) {
                                String info = String.valueOf(msg.arg2);
                                if (info != null) {
                                    Camera.this.mDistanceInfoCallback.onInfo(info);
                                }
                                return;
                            }
                            break;
                        case 22:
                            if (Camera.this.mEnableRaw16) {
                                try {
                                    CameraMetadataNative resultMeta = new CameraMetadataNative();
                                    CameraMetadataNative characteristicMeta = new CameraMetadataNative();
                                    Camera.this.getMetadata(resultMeta, characteristicMeta);
                                    Camera.this.mMetadataCallbacks.onMetadataReceived(new CaptureResult(resultMeta, 0), new CameraCharacteristics(characteristicMeta));
                                } catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            break;
                        case 23:
                            if (Camera.this.mUncompressedImageCallback != null) {
                                Camera.this.mUncompressedImageCallback.onPictureTaken(null, this.mCamera);
                                return;
                            }
                            break;
                        default:
                            Log.e(Camera.TAG, "Unknown MTK-extended notify message type " + msg.arg1);
                            break;
                    }
                    if (Camera.this.mVendorDataCallback != null) {
                        Camera.this.mVendorDataCallback.onDataCallback(msg);
                    }
                    return;
                default:
                    Log.e(Camera.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
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

    public interface GestureCallback {
        void onGesture();
    }

    public interface HdrOriginalCallback {
        void onCapture(byte[] bArr);
    }

    public interface MetadataCallback {
        void onMetadataReceived(CaptureResult captureResult, CameraCharacteristics cameraCharacteristics);
    }

    public interface ObjectTrackingListener {
        void onObjectTracking(Face face, Camera camera);
    }

    @Deprecated
    public interface OnZoomChangeListener {
        void onZoomChange(int i, boolean z, Camera camera);
    }

    @Deprecated
    public class Parameters {
        public static final String ANTIBANDING_50HZ = "50hz";
        public static final String ANTIBANDING_60HZ = "60hz";
        public static final String ANTIBANDING_AUTO = "auto";
        public static final String ANTIBANDING_OFF = "off";
        public static final int CAMERA_MODE_MTK_PRV = 1;
        public static final int CAMERA_MODE_MTK_VDO = 2;
        public static final int CAMERA_MODE_MTK_VT = 3;
        public static final int CAMERA_MODE_NORMAL = 0;
        public static final String CAPTURE_MODE_ASD = "asd";
        public static final String CAPTURE_MODE_BEST_SHOT = "bestshot";
        public static final String CAPTURE_MODE_BURST_SHOT = "burstshot";
        public static final String CAPTURE_MODE_CONTINUOUS_SHOT = "continuousshot";
        public static final String CAPTURE_MODE_EV_BRACKET_SHOT = "evbracketshot";
        public static final String CAPTURE_MODE_FB = "face_beauty";
        public static final String CAPTURE_MODE_GESTURE_SHOT = "gestureshot";
        public static final String CAPTURE_MODE_HDR = "hdr";
        public static final String CAPTURE_MODE_NORMAL = "normal";
        public static final String CAPTURE_MODE_PANORAMA3D = "panorama3dmode";
        public static final String CAPTURE_MODE_PANORAMA_SHOT = "autorama";
        public static final String CAPTURE_MODE_S3D = "single3d";
        public static final String EFFECT_AQUA = "aqua";
        public static final String EFFECT_BLACKBOARD = "blackboard";
        public static final String EFFECT_MONO = "mono";
        public static final String EFFECT_NEGATIVE = "negative";
        public static final String EFFECT_NONE = "none";
        public static final String EFFECT_POSTERIZE = "posterize";
        public static final String EFFECT_SEPIA = "sepia";
        public static final String EFFECT_SOLARIZE = "solarize";
        public static final String EFFECT_WHITEBOARD = "whiteboard";
        public static final String EIS_MODE_OFF = "off";
        public static final String EIS_MODE_ON = "on";
        private static final String FALSE = "false";
        public static final String FLASH_MODE_AUTO = "auto";
        public static final String FLASH_MODE_OFF = "off";
        public static final String FLASH_MODE_ON = "on";
        public static final String FLASH_MODE_RED_EYE = "red-eye";
        public static final String FLASH_MODE_TORCH = "torch";
        public static final int FOCUS_DISTANCE_FAR_INDEX = 2;
        public static final int FOCUS_DISTANCE_NEAR_INDEX = 0;
        public static final int FOCUS_DISTANCE_OPTIMAL_INDEX = 1;
        public static final int FOCUS_ENG_MODE_BRACKET = 1;
        public static final int FOCUS_ENG_MODE_FULLSCAN = 2;
        public static final int FOCUS_ENG_MODE_FULLSCAN_REPEAT = 3;
        public static final int FOCUS_ENG_MODE_NONE = 0;
        public static final int FOCUS_ENG_MODE_REPEAT = 4;
        public static final String FOCUS_MODE_AUTO = "auto";
        public static final String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";
        public static final String FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video";
        public static final String FOCUS_MODE_EDOF = "edof";
        public static final String FOCUS_MODE_FIXED = "fixed";
        public static final String FOCUS_MODE_FULLSCAN = "fullscan";
        public static final String FOCUS_MODE_INFINITY = "infinity";
        public static final String FOCUS_MODE_MACRO = "macro";
        public static final String FOCUS_MODE_MANUAL = "manual";
        private static final String KEY_AFLAMP_MODE = "aflamp-mode";
        private static final String KEY_ANTIBANDING = "antibanding";
        private static final String KEY_AUTO_EXPOSURE_LOCK = "auto-exposure-lock";
        private static final String KEY_AUTO_EXPOSURE_LOCK_SUPPORTED = "auto-exposure-lock-supported";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK = "auto-whitebalance-lock";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED = "auto-whitebalance-lock-supported";
        private static final String KEY_BRIGHTNESS_MODE = "brightness";
        private static final String KEY_BURST_SHOT_NUM = "burst-num";
        private static final String KEY_CAMERA_MODE = "mtk-cam-mode";
        private static final String KEY_CAPTURE_MODE = "cap-mode";
        private static final String KEY_CAPTURE_PATH = "capfname";
        private static final String KEY_CONTINUOUS_SPEED_MODE = "continuous-shot-speed";
        private static final String KEY_CONTRAST_MODE = "contrast";
        private static final String KEY_DYNAMIC_FRAME_RATE = "dynamic-frame-rate";
        private static final String KEY_DYNAMIC_FRAME_RATE_SUPPORTED = "dynamic-frame-rate-supported";
        private static final String KEY_EDGE_MODE = "edge";
        private static final String KEY_EFFECT = "effect";
        private static final String KEY_EIS_MODE = "eis-mode";
        private static final String KEY_ENG_AE_ENABLE = "ae-e";
        private static final String KEY_ENG_CAPTURE_ISO = "cap-iso";
        private static final String KEY_ENG_CAPTURE_ISP_GAIN = "cap-isp-g";
        private static final String KEY_ENG_CAPTURE_SENSOR_GAIN = "cap-sr-g";
        private static final String KEY_ENG_CAPTURE_SHUTTER_SPEED = "cap-ss";
        private static final String KEY_ENG_EV_CALBRATION_OFFSET_VALUE = "ev-cal-o";
        private static final String KEY_ENG_FLASH_DUTY_MAX = "flash-duty-max";
        private static final String KEY_ENG_FLASH_DUTY_MIN = "flash-duty-min";
        private static final String KEY_ENG_FLASH_DUTY_VALUE = "flash-duty-value";
        private static final String KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL = "focus-fs-fi";
        private static final String KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL_MAX = "focus-fs-fi-max";
        private static final String KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL_MIN = "focus-fs-fi-min";
        private static final String KEY_ENG_MFLL_ENABLE = "eng-mfll-e";
        private static final String KEY_ENG_MFLL_PICTURE_COUNT = "eng-mfll-pc";
        private static final String KEY_ENG_MFLL_SUPPORTED = "eng-mfll-s";
        private static final String KEY_ENG_MSG = "eng-msg";
        private static final String KEY_ENG_MTK_1to3_SHADING_ENABLE = "mtk-123-shad-e";
        private static final String KEY_ENG_MTK_1to3_SHADING_SUPPORTED = "mtk-123-shad-s";
        private static final String KEY_ENG_MTK_AWB_ENABLE = "mtk-awb-e";
        private static final String KEY_ENG_MTK_AWB_SUPPORTED = "mtk-awb-s";
        private static final String KEY_ENG_MTK_SHADING_ENABLE = "mtk-shad-e";
        private static final String KEY_ENG_MTK_SHADING_SUPPORTED = "mtk-shad-s";
        private static final String KEY_ENG_PARAMETER1 = "eng-p1";
        private static final String KEY_ENG_PARAMETER2 = "eng-p2";
        private static final String KEY_ENG_PARAMETER3 = "eng-p3";
        private static final String KEY_ENG_PREVIEW_AE_INDEX = "prv-ae-i";
        private static final String KEY_ENG_PREVIEW_FPS = "eng-prv-fps";
        private static final String KEY_ENG_PREVIEW_FRAME_INTERVAL_IN_US = "eng-prv-fius";
        private static final String KEY_ENG_PREVIEW_ISP_GAIN = "prv-isp-g";
        private static final String KEY_ENG_PREVIEW_SENSOR_GAIN = "prv-sr-g";
        private static final String KEY_ENG_PREVIEW_SHUTTER_SPEED = "prv-ss";
        private static final String KEY_ENG_SAVE_SHADING_TABLE = "eng-s-shad-t";
        private static final String KEY_ENG_SENOSR_MODE_SLIM_VIDEO1_SUPPORTED = "sv1-s";
        private static final String KEY_ENG_SENOSR_MODE_SLIM_VIDEO2_SUPPORTED = "sv2-s";
        private static final String KEY_ENG_SENSOR_AWB_ENABLE = "sr-awb-e";
        private static final String KEY_ENG_SENSOR_AWB_SUPPORTED = "sr-awb-s";
        private static final String KEY_ENG_SENSOR_SHADNING_ENABLE = "sr-shad-e";
        private static final String KEY_ENG_SENSOR_SHADNING_SUPPORTED = "sr-shad-s";
        private static final String KEY_ENG_SHADING_TABLE = "eng-shad-t";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_CROP_CENTER_2M_SUPPORTED = "vdr-cc2m-s";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_ENABLE = "vrd-mfr-e";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_MAX = "vrd-mfr-max";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_MIN = "vrd-mfr-min";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_RANGE_HIGH = "vrd-mfr-high";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_RANGE_LOW = "vrd-mfr-low";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_MANUAL_FRAME_RATE_SUPPORTED = "vrd-mfr-s";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_RESIZE_TO_2M_SUPPORTED = "vdr-r2m-s";
        private static final String KEY_ENG_VIDEO_RAW_DUMP_RESIZE_TO_4K2K_SUPPORTED = "vdr-r4k2k-s";
        private static final String KEY_ENG_ZSD_ENABLE = "eng-zsd-e";
        private static final String KEY_EXPOSURE_COMPENSATION = "exposure-compensation";
        private static final String KEY_EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
        private static final String KEY_EXPOSURE_METER_MODE = "exposure-meter";
        private static final String KEY_FD_MODE = "fd-mode";
        private static final String KEY_FLASH_MODE = "flash-mode";
        private static final String KEY_FOCAL_LENGTH = "focal-length";
        private static final String KEY_FOCUS_AREAS = "focus-areas";
        private static final String KEY_FOCUS_DISTANCES = "focus-distances";
        private static final String KEY_FOCUS_ENG_BEST_STEP = "afeng-best-focus-step";
        private static final String KEY_FOCUS_ENG_MAX_STEP = "afeng-max-focus-step";
        private static final String KEY_FOCUS_ENG_MIN_STEP = "afeng-min-focus-step";
        private static final String KEY_FOCUS_ENG_MODE = "afeng-mode";
        private static final String KEY_FOCUS_ENG_STEP = "afeng-pos";
        private static final String KEY_FOCUS_MODE = "focus-mode";
        private static final String KEY_FPS_MODE = "fps-mode";
        private static final String KEY_GPS_ALTITUDE = "gps-altitude";
        private static final String KEY_GPS_LATITUDE = "gps-latitude";
        private static final String KEY_GPS_LONGITUDE = "gps-longitude";
        private static final String KEY_GPS_PROCESSING_METHOD = "gps-processing-method";
        private static final String KEY_GPS_TIMESTAMP = "gps-timestamp";
        private static final String KEY_HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";
        private static final String KEY_HSVR_PRV_FPS = "hsvr-prv-fps";
        private static final String KEY_HSVR_PRV_SIZE = "hsvr-prv-size";
        private static final String KEY_HUE_MODE = "hue";
        private static final String KEY_ISOSPEED_MODE = "iso-speed";
        private static final String KEY_IS_OPPO_APP = "oppo-app";
        private static final String KEY_JPEG_QUALITY = "jpeg-quality";
        private static final String KEY_JPEG_THUMBNAIL_HEIGHT = "jpeg-thumbnail-height";
        private static final String KEY_JPEG_THUMBNAIL_QUALITY = "jpeg-thumbnail-quality";
        private static final String KEY_JPEG_THUMBNAIL_SIZE = "jpeg-thumbnail-size";
        private static final String KEY_JPEG_THUMBNAIL_WIDTH = "jpeg-thumbnail-width";
        private static final String KEY_MATV_PREVIEW_DELAY = "tv-delay";
        private static final String KEY_MAX_EXPOSURE_COMPENSATION = "max-exposure-compensation";
        private static final String KEY_MAX_FRAME_RATE_ZSD_OFF = "pip-fps-zsd-off";
        private static final String KEY_MAX_FRAME_RATE_ZSD_ON = "pip-fps-zsd-on";
        private static final String KEY_MAX_NUM_DETECTED_FACES_HW = "max-num-detected-faces-hw";
        private static final String KEY_MAX_NUM_DETECTED_FACES_SW = "max-num-detected-faces-sw";
        public static final String KEY_MAX_NUM_DETECTED_OBJECT = "max-num-ot";
        private static final String KEY_MAX_NUM_FOCUS_AREAS = "max-num-focus-areas";
        private static final String KEY_MAX_NUM_METERING_AREAS = "max-num-metering-areas";
        private static final String KEY_MAX_ZOOM = "max-zoom";
        private static final String KEY_METERING_AREAS = "metering-areas";
        private static final String KEY_MIN_EXPOSURE_COMPENSATION = "min-exposure-compensation";
        private static final String KEY_MUTE_RECORDING_SOUND = "rec-mute-ogg";
        private static final String KEY_PDAF_SUPPORTED = "pdaf-supported";
        private static final String KEY_PICTURE_FORMAT = "picture-format";
        private static final String KEY_PICTURE_SIZE = "picture-size";
        private static final String KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO = "preferred-preview-size-for-video";
        private static final String KEY_PREVIEW_DUMP_RESOLUTION = "prv-dump-res";
        private static final String KEY_PREVIEW_FORMAT = "preview-format";
        private static final String KEY_PREVIEW_FPS_RANGE = "preview-fps-range";
        private static final String KEY_PREVIEW_FRAME_RATE = "preview-frame-rate";
        private static final String KEY_PREVIEW_SIZE = "preview-size";
        private static final String KEY_RAW_DUMP_FLAG = "afeng_raw_dump_flag";
        private static final String KEY_RAW_SAVE_MODE = "rawsave-mode";
        private static final String KEY_RECORDING_HINT = "recording-hint";
        private static final String KEY_REFOCUS_JPS_FILE_NAME = "refocus-jps-file-name";
        private static final String KEY_ROTATION = "rotation";
        private static final String KEY_SATURATION_MODE = "saturation";
        private static final String KEY_SCENE_MODE = "scene-mode";
        private static final String KEY_SENSOR_DEV = "sensor-dev";
        private static final String KEY_SENSOR_TYPE = "sensor-type";
        private static final String KEY_SMOOTH_ZOOM_SUPPORTED = "smooth-zoom-supported";
        public static final String KEY_STEREO3D_MODE = "mode";
        private static final String KEY_STEREO3D_PRE = "stereo3d-";
        public static final String KEY_STEREO3D_TYPE = "type";
        private static final String KEY_STEREO_DEPTHAF_MODE = "stereo-depth-af";
        private static final String KEY_STEREO_DISTANCE_MODE = "stereo-distance-measurement";
        private static final String KEY_STEREO_REFOCUS_MODE = "stereo-image-refocus";
        private static final String KEY_VERTICAL_VIEW_ANGLE = "vertical-view-angle";
        private static final String KEY_VIDEO_SIZE = "video-size";
        private static final String KEY_VIDEO_SNAPSHOT_SUPPORTED = "video-snapshot-supported";
        private static final String KEY_VIDEO_STABILIZATION = "video-stabilization";
        private static final String KEY_VIDEO_STABILIZATION_SUPPORTED = "video-stabilization-supported";
        private static final String KEY_WHITE_BALANCE = "whitebalance";
        private static final String KEY_ZOOM = "zoom";
        private static final String KEY_ZOOM_RATIOS = "zoom-ratios";
        private static final String KEY_ZOOM_SUPPORTED = "zoom-supported";
        private static final String KEY_ZSD_MODE = "zsd-mode";
        private static final String KEY_ZSD_SUPPORTED = "zsd-supported";
        private static final String OFF = "off";
        private static final String ON = "on";
        private static final String PIXEL_FORMAT_BAYER_RGGB = "bayer-rggb";
        private static final String PIXEL_FORMAT_JPEG = "jpeg";
        private static final String PIXEL_FORMAT_RGB565 = "rgb565";
        private static final String PIXEL_FORMAT_YUV420P = "yuv420p";
        private static final String PIXEL_FORMAT_YUV420SP = "yuv420sp";
        private static final String PIXEL_FORMAT_YUV422I = "yuv422i-yuyv";
        private static final String PIXEL_FORMAT_YUV422SP = "yuv422sp";
        public static final int PREVIEW_DUMP_RESOLUTION_CROP = 1;
        public static final int PREVIEW_DUMP_RESOLUTION_NORMAL = 0;
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
        public static final String SENSOR_DEV_ATV = "atv";
        public static final String SENSOR_DEV_MAIN = "main";
        public static final String SENSOR_DEV_SUB = "sub";
        public static final String STEREO3D_TYPE_FRAMESEQ = "frame_seq";
        public static final String STEREO3D_TYPE_OFF = "off";
        public static final String STEREO3D_TYPE_SIDEBYSIDE = "sidebyside";
        public static final String STEREO3D_TYPE_TOPBOTTOM = "topbottom";
        private static final String SUPPORTED_VALUES_SUFFIX = "-values";
        private static final String TRUE = "true";
        public static final String WHITE_BALANCE_AUTO = "auto";
        public static final String WHITE_BALANCE_CLOUDY_DAYLIGHT = "cloudy-daylight";
        public static final String WHITE_BALANCE_DAYLIGHT = "daylight";
        public static final String WHITE_BALANCE_FLUORESCENT = "fluorescent";
        public static final String WHITE_BALANCE_INCANDESCENT = "incandescent";
        public static final String WHITE_BALANCE_SHADE = "shade";
        public static final String WHITE_BALANCE_TUNGSTEN = "tungsten";
        public static final String WHITE_BALANCE_TWILIGHT = "twilight";
        public static final String WHITE_BALANCE_WARM_FLUORESCENT = "warm-fluorescent";
        private LinkedHashMap<String, String> mMap;
        private boolean mStereo3DMode;

        /* synthetic */ Parameters(Camera this$0, Parameters parameters) {
            this();
        }

        private Parameters() {
            this.mStereo3DMode = false;
            this.mMap = new LinkedHashMap(128);
        }

        public Parameters copy() {
            Parameters para = new Parameters();
            para.mMap = new LinkedHashMap(this.mMap);
            return para;
        }

        public void copyFrom(Parameters other) {
            if (other == null) {
                throw new NullPointerException("other must not be null");
            }
            this.mMap.putAll(other.mMap);
        }

        private Camera getOuter() {
            return Camera.this;
        }

        public boolean same(Parameters other) {
            if (this == other) {
                return true;
            }
            return other != null ? this.mMap.equals(other.mMap) : false;
        }

        @Deprecated
        public void dump() {
            Log.e(Camera.TAG, "dump: size=" + this.mMap.size());
            for (String k : this.mMap.keySet()) {
                Log.e(Camera.TAG, "dump: " + k + "=" + ((String) this.mMap.get(k)));
            }
        }

        public String flatten() {
            StringBuilder flattened = new StringBuilder(128);
            for (String k : this.mMap.keySet()) {
                flattened.append(k);
                flattened.append("=");
                flattened.append((String) this.mMap.get(k));
                flattened.append(";");
            }
            flattened.deleteCharAt(flattened.length() - 1);
            return flattened.toString();
        }

        public void unflatten(String flattened) {
            this.mMap.clear();
            StringSplitter<String> splitter = new SimpleStringSplitter(';');
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
                Area area = (Area) areas.get(i);
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
            return (String) this.mMap.get(key);
        }

        public int getInt(String key) {
            return Integer.parseInt((String) this.mMap.get(key));
        }

        public void setPreviewSize(int width, int height) {
            set((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PREVIEW_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPreviewSize() {
            return strToSize(get((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PREVIEW_SIZE));
        }

        public List<Size> getSupportedPreviewSizes() {
            return splitSize(get((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PREVIEW_SIZE + SUPPORTED_VALUES_SUFFIX));
        }

        public List<Size> getSupportedVideoSizes() {
            return splitSize(get("video-size-values"));
        }

        public Size getPreferredPreviewSizeForVideo() {
            return strToSize(get(KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO));
        }

        public Size getPreferredPreviewSizeForSlowMotionVideo() {
            return strToSize(get(KEY_HSVR_PRV_SIZE));
        }

        public List<Size> getSupportedSlowMotionVideoSizes() {
            return splitSize(get("hsvr-prv-size-values"));
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
            set(KEY_PREVIEW_FPS_RANGE, "" + min + "," + max);
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
            if (s == null) {
                throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
            }
            set(KEY_PREVIEW_FORMAT, s);
        }

        public int getPreviewFormat() {
            return pixelFormatForCameraFormat(get(KEY_PREVIEW_FORMAT));
        }

        public List<Integer> getSupportedPreviewFormats() {
            String str = get("preview-format-values");
            ArrayList<Integer> formats = new ArrayList();
            for (String s : split(str)) {
                int f = pixelFormatForCameraFormat(s);
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        public void setPictureSize(int width, int height) {
            set((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PICTURE_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPictureSize() {
            return strToSize(get((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PICTURE_SIZE));
        }

        public List<Size> getSupportedPictureSizes() {
            return splitSize(get((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + KEY_PICTURE_SIZE + SUPPORTED_VALUES_SUFFIX));
        }

        public void setPictureFormat(int pixel_format) {
            String s = cameraFormatForPixelFormat(pixel_format);
            if (s == null) {
                throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
            }
            set(KEY_PICTURE_FORMAT, s);
        }

        public int getPictureFormat() {
            return pixelFormatForCameraFormat(get(KEY_PICTURE_FORMAT));
        }

        public List<Integer> getSupportedPictureFormats() {
            String str = get("picture-format-values");
            ArrayList<Integer> formats = new ArrayList();
            for (String s : split(str)) {
                int f = pixelFormatForCameraFormat(s);
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        private String cameraFormatForPixelFormat(int pixel_format) {
            switch (pixel_format) {
                case 4:
                    return PIXEL_FORMAT_RGB565;
                case 16:
                    return PIXEL_FORMAT_YUV422SP;
                case 17:
                    return PIXEL_FORMAT_YUV420SP;
                case 20:
                    return PIXEL_FORMAT_YUV422I;
                case 256:
                    return PIXEL_FORMAT_JPEG;
                case ImageFormat.YV12 /*842094169*/:
                    return PIXEL_FORMAT_YUV420P;
                default:
                    return null;
            }
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

        public String getEisMode() {
            return get(KEY_EIS_MODE);
        }

        public void setEisMode(String eis) {
            set(KEY_EIS_MODE, eis);
        }

        public List<String> getSupportedEisMode() {
            return split(get("eis-mode-values"));
        }

        public String getAFLampMode() {
            return get(KEY_AFLAMP_MODE);
        }

        public void setAFLampMode(String aflamp) {
            set(KEY_AFLAMP_MODE, aflamp);
        }

        public List<String> getSupportedAFLampMode() {
            return split(get("aflamp-mode-values"));
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
            return getFloat(KEY_EXPOSURE_COMPENSATION_STEP, TonemapCurve.LEVEL_BLACK);
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

        public void setCameraMode(int value) {
            Log.d(Camera.TAG, "setCameraMode=" + value);
            set(KEY_CAMERA_MODE, value);
        }

        public String getISOSpeed() {
            return get(KEY_ISOSPEED_MODE);
        }

        public void setISOSpeed(String value) {
            set(KEY_ISOSPEED_MODE, value);
        }

        public List<String> getSupportedISOSpeed() {
            return split(get("iso-speed-values"));
        }

        public int getMaxNumDetectedObjects() {
            return getInt(KEY_MAX_NUM_DETECTED_OBJECT, 0);
        }

        public String getFDMode() {
            return get(KEY_FD_MODE);
        }

        public void setFDMode(String value) {
            set(KEY_FD_MODE, value);
        }

        public List<String> getSupportedFDMode() {
            return split(get("fd-mode-values"));
        }

        public String getEdgeMode() {
            return get(KEY_EDGE_MODE);
        }

        public void setEdgeMode(String value) {
            set(KEY_EDGE_MODE, value);
        }

        public List<String> getSupportedEdgeMode() {
            return split(get("edge-values"));
        }

        public String getHueMode() {
            return get(KEY_HUE_MODE);
        }

        public void setHueMode(String value) {
            set(KEY_HUE_MODE, value);
        }

        public List<String> getSupportedHueMode() {
            return split(get("hue-values"));
        }

        public String getSaturationMode() {
            return get(KEY_SATURATION_MODE);
        }

        public void setSaturationMode(String value) {
            set(KEY_SATURATION_MODE, value);
        }

        public List<String> getSupportedSaturationMode() {
            return split(get("saturation-values"));
        }

        public String getBrightnessMode() {
            return get(KEY_BRIGHTNESS_MODE);
        }

        public void setBrightnessMode(String value) {
            set(KEY_BRIGHTNESS_MODE, value);
        }

        public List<String> getSupportedBrightnessMode() {
            return split(get("brightness-values"));
        }

        public String getContrastMode() {
            return get(KEY_CONTRAST_MODE);
        }

        public void setContrastMode(String value) {
            set(KEY_CONTRAST_MODE, value);
        }

        public List<String> getSupportedContrastMode() {
            return split(get("contrast-values"));
        }

        public String getCaptureMode() {
            return get(KEY_CAPTURE_MODE);
        }

        public void setCaptureMode(String value) {
            set(KEY_CAPTURE_MODE, value);
        }

        public List<String> getSupportedCaptureMode() {
            return split(get("cap-mode-values"));
        }

        public void setCapturePath(String value) {
            if (value == null) {
                remove(KEY_CAPTURE_PATH);
            } else {
                set(KEY_CAPTURE_PATH, value);
            }
        }

        public void setBurstShotNum(int value) {
            set(KEY_BURST_SHOT_NUM, value);
        }

        public void setFocusEngMode(int mode) {
            set(KEY_FOCUS_ENG_MODE, mode);
        }

        public int getBestFocusStep() {
            return getInt(KEY_FOCUS_ENG_BEST_STEP, 0);
        }

        public void setRawDumpFlag(boolean toggle) {
            set(KEY_RAW_DUMP_FLAG, toggle ? TRUE : FALSE);
        }

        public void setPreviewRawDumpResolution(int value) {
            set(KEY_PREVIEW_DUMP_RESOLUTION, value);
        }

        public int getMaxFocusStep() {
            return getInt(KEY_FOCUS_ENG_MAX_STEP, 0);
        }

        public int getMinFocusStep() {
            return getInt(KEY_FOCUS_ENG_STEP, 0);
        }

        public void setFocusEngStep(int step) {
            set(KEY_FOCUS_ENG_STEP, step);
        }

        public void setExposureMeterMode(String mode) {
            set(KEY_EXPOSURE_METER_MODE, mode);
        }

        public String getExposureMeterMode() {
            return get(KEY_EXPOSURE_METER_MODE);
        }

        public int getSensorType() {
            return getInt(KEY_SENSOR_TYPE, 0);
        }

        public void setEngAEEnable(int enable) {
            set(KEY_ENG_AE_ENABLE, enable);
        }

        public void setEngFlashDuty(int duty) {
            set(KEY_ENG_FLASH_DUTY_VALUE, duty);
        }

        public void setEngZSDEnable(int enable) {
            set(KEY_ENG_ZSD_ENABLE, enable);
        }

        public int getEngPreviewShutterSpeed() {
            return getInt(KEY_ENG_PREVIEW_SHUTTER_SPEED, 0);
        }

        public int getEngPreviewSensorGain() {
            return getInt(KEY_ENG_PREVIEW_SENSOR_GAIN, 0);
        }

        public int getEngPreviewISPGain() {
            return getInt(KEY_ENG_PREVIEW_ISP_GAIN, 0);
        }

        public int getEngPreviewAEIndex() {
            return getInt(KEY_ENG_PREVIEW_AE_INDEX, 0);
        }

        public int getEngCaptureSensorGain() {
            return getInt(KEY_ENG_CAPTURE_SENSOR_GAIN, 0);
        }

        public int getEngCaptureISPGain() {
            return getInt(KEY_ENG_CAPTURE_ISP_GAIN, 0);
        }

        public int getEngCaptureShutterSpeed() {
            return getInt(KEY_ENG_CAPTURE_SHUTTER_SPEED, 0);
        }

        public int getEngCaptureISO() {
            return getInt(KEY_ENG_CAPTURE_ISO, 0);
        }

        public int getEngFlashDutyMin() {
            return getInt(KEY_ENG_FLASH_DUTY_MIN, 0);
        }

        public int getEngFlashDutyMax() {
            return getInt(KEY_ENG_FLASH_DUTY_MAX, 0);
        }

        public int getEngPreviewFPS() {
            return getInt(KEY_ENG_PREVIEW_FPS, 0);
        }

        public String getEngEngMSG() {
            return get(KEY_ENG_MSG);
        }

        public void setEngFocusFullScanFrameInterval(int n) {
            set(KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL, n);
        }

        public int getEngFocusFullScanFrameIntervalMax() {
            return getInt(KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL_MAX, 0);
        }

        public int getEngFocusFullScanFrameIntervalMin() {
            return getInt(KEY_ENG_FOCUS_FULLSCAN_FRAME_INTERVAL_MIN, 0);
        }

        public int getEngPreviewFrameIntervalInUS() {
            return getInt(KEY_ENG_PREVIEW_FRAME_INTERVAL_IN_US, 0);
        }

        public void setEngParameter1(String value) {
            set(KEY_ENG_PARAMETER1, value);
        }

        public void setEngParameter2(String value) {
            set(KEY_ENG_PARAMETER2, value);
        }

        public void setEngParameter3(String value) {
            set(KEY_ENG_PARAMETER3, value);
        }

        public void setEngSaveShadingTable(int save) {
            set(KEY_ENG_SAVE_SHADING_TABLE, save);
        }

        public void setEngShadingTable(int shading_table) {
            set(KEY_ENG_SHADING_TABLE, shading_table);
        }

        public int getEngEVCalOffset() {
            return getInt(KEY_ENG_EV_CALBRATION_OFFSET_VALUE, 0);
        }

        public void setMATVDelay(int ms) {
            set(KEY_MATV_PREVIEW_DELAY, ms);
        }

        public String getStereo3DType() {
            return get((this.mStereo3DMode ? KEY_STEREO3D_PRE : "") + "type");
        }

        public void setStereo3DMode(boolean enable) {
            this.mStereo3DMode = enable;
        }

        public void setContinuousSpeedMode(String value) {
            set(KEY_CONTINUOUS_SPEED_MODE, value);
        }

        public String getZSDMode() {
            return get(KEY_ZSD_MODE);
        }

        public void setZSDMode(String value) {
            set(KEY_ZSD_MODE, value);
        }

        public List<String> getSupportedZSDMode() {
            return split(get("zsd-mode-values"));
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
            set(KEY_FOCUS_AREAS, (List) focusAreas);
        }

        public int getMaxNumMeteringAreas() {
            return getInt(KEY_MAX_NUM_METERING_AREAS, 0);
        }

        public List<Area> getMeteringAreas() {
            return splitArea(get(KEY_METERING_AREAS));
        }

        public void setMeteringAreas(List<Area> meteringAreas) {
            set(KEY_METERING_AREAS, (List) meteringAreas);
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

        public boolean isPdafSupported() {
            return TRUE.equals(get(KEY_PDAF_SUPPORTED));
        }

        public void enableRecordingSound(String value) {
            if (value.equals(WifiEnterpriseConfig.ENGINE_ENABLE) || value.equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
                set(KEY_MUTE_RECORDING_SOUND, value);
            }
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

        public List<Integer> getPIPFrameRateZSDOn() {
            return splitInt(get(KEY_MAX_FRAME_RATE_ZSD_ON));
        }

        public List<Integer> getPIPFrameRateZSDOff() {
            return splitInt(get(KEY_MAX_FRAME_RATE_ZSD_OFF));
        }

        public boolean getDynamicFrameRate() {
            return TRUE.equals(get(KEY_DYNAMIC_FRAME_RATE));
        }

        public void setDynamicFrameRate(boolean toggle) {
            set(KEY_DYNAMIC_FRAME_RATE, toggle ? TRUE : FALSE);
        }

        public boolean isDynamicFrameRateSupported() {
            return TRUE.equals(get(KEY_DYNAMIC_FRAME_RATE_SUPPORTED));
        }

        public void setRefocusJpsFileName(String fineName) {
            set(KEY_REFOCUS_JPS_FILE_NAME, fineName);
        }

        public void setRefocusMode(boolean toggle) {
            set(KEY_STEREO_REFOCUS_MODE, toggle ? "on" : "off");
        }

        public String getRefocusMode() {
            return get(KEY_STEREO_REFOCUS_MODE);
        }

        public void setDepthAFMode(boolean toggle) {
            set(KEY_STEREO_DEPTHAF_MODE, toggle ? "on" : "off");
        }

        public String getDepthAFMode() {
            return get(KEY_STEREO_DEPTHAF_MODE);
        }

        public void setDistanceMode(boolean toggle) {
            set(KEY_STEREO_DISTANCE_MODE, toggle ? "on" : "off");
        }

        public String getDistanceMode() {
            return get(KEY_STEREO_DISTANCE_MODE);
        }

        private ArrayList<String> split(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<String> substrings = new ArrayList();
            for (String s : splitter) {
                substrings.add(s);
            }
            return substrings;
        }

        private ArrayList<Integer> splitInt(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Integer> substrings = new ArrayList();
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
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(str);
                int index = 0;
                for (String s : splitter) {
                    int index2 = index + 1;
                    output[index] = Integer.parseInt(s);
                    index = index2;
                }
            }
        }

        private void splitFloat(String str, float[] output) {
            if (str != null) {
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(str);
                int index = 0;
                for (String s : splitter) {
                    int index2 = index + 1;
                    output[index] = Float.parseFloat(s);
                    index = index2;
                }
            }
        }

        private float getFloat(String key, float defaultValue) {
            try {
                return Float.parseFloat((String) this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private int getInt(String key, int defaultValue) {
            try {
                return Integer.parseInt((String) this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private ArrayList<Size> splitSize(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Size> sizeList = new ArrayList();
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
            if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
                ArrayList<int[]> rangeList = new ArrayList();
                int fromIndex = 1;
                int endIndex;
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

        private ArrayList<Area> splitArea(String str) {
            if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
                ArrayList<Area> result = new ArrayList();
                int fromIndex = 1;
                int[] array = new int[5];
                int endIndex;
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
                    Area area = (Area) result.get(0);
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

    @Deprecated
    public interface PictureCallback {
        void onPictureTaken(byte[] bArr, Camera camera);
    }

    @Deprecated
    public interface PreviewCallback {
        void onPreviewFrame(byte[] bArr, Camera camera);
    }

    public interface PreviewRawDumpCallback {
        void onNotify(int i);
    }

    @Deprecated
    public interface ShutterCallback {
        void onShutter();
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
            boolean z = false;
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            if (this.width == s.width && this.height == s.height) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.width * 32713) + this.height;
        }
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

    public interface ZSDPreviewDone {
        void onPreviewDone();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.Camera.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.Camera.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.Camera.<clinit>():void");
    }

    private final native void _addCallbackBuffer(byte[] bArr, int i);

    private final native boolean _enableShutterSound(boolean z);

    private static native void _getCameraInfo(int i, CameraInfo cameraInfo);

    private final native void _startFaceDetection(int i);

    private final native void _stopFaceDetection();

    private final native void _stopPreview();

    private native void cancelGDPreview();

    private final native void cancelMainFace();

    private native void enableFocusMoveCallback(int i);

    public static native int getNumberOfCameras();

    private static native int native_CameraHardwareCheck(int i);

    private final native void native_autoFocus();

    private final native void native_cancelAutoFocus();

    private final native String native_getParameters();

    private static native String native_getProperty(String str, String str2);

    private final native void native_release();

    private final native void native_setParameters(String str);

    private static native void native_setProperty(String str, String str2);

    private final native int native_setup(Object obj, int i, int i2, String str);

    private final native void native_takePicture(int i);

    private final native void setHasPreviewCallback(boolean z, boolean z2);

    private final native void setMainFace(int i, int i2);

    private final native void setPreviewCallbackSurface(Surface surface);

    private final native void startAUTORAMA(int i);

    private native void startGDPreview();

    private final native void startOT(int i, int i2);

    private native void stopAUTORAMA(int i);

    private final native void stopOT();

    public native void cancelContinuousShot();

    public native void cancelPanorama();

    public final native void doPanorama(int i);

    public final native void enableRaw16Callback(boolean z);

    public final native void getMetadata(CameraMetadataNative cameraMetadataNative, CameraMetadataNative cameraMetadataNative2);

    public final native void lock();

    public final native boolean previewEnabled();

    public final native void reconnect() throws IOException;

    public native void setContinuousShotSpeed(int i);

    public final native void setDisplayOrientation(int i);

    public final native void setPreviewSurface(Surface surface) throws IOException;

    public final native void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

    public final native void start3DSHOT(int i);

    public final native void startPreview();

    public final native void startSmoothZoom(int i);

    public native void stop3DSHOT(int i);

    public final native void stopSmoothZoom();

    public final native void unlock();

    public static void getCameraInfo(int cameraId, CameraInfo cameraInfo) {
        _getCameraInfo(cameraId, cameraInfo);
        try {
            if (Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE)).isCameraSoundForced()) {
                cameraInfo.canDisableShutterSound = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Audio service is unavailable for queries");
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

    public static Camera openLegacy(int cameraId, int halVersion) {
        if (halVersion >= 256) {
            return new Camera(cameraId, halVersion);
        }
        throw new IllegalArgumentException("Invalid HAL version " + halVersion);
    }

    private Camera(int cameraId, int halVersion) {
        this.mStereo3DModeForCamera = false;
        this.mEnableRaw16 = false;
        this.mFaceDetectionRunning = false;
        this.mObjectFace = new Face();
        this.mObjectRect = new Rect();
        this.mAutoFocusCallbackLock = new Object();
        this.mObjectCallbackLock = new Object();
        this.mLongshotEnable = false;
        int err = cameraInitVersion(cameraId, halVersion);
        if (!checkInitErrors(err)) {
            return;
        }
        if (err == (-OsConstants.EACCES)) {
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
    }

    private int cameraInitVersion(int cameraId, int halVersion) {
        this.mShutterCallback = null;
        this.mRawImageCallback = null;
        this.mJpegCallback = null;
        this.mPreviewCallback = null;
        this.mPreviewRawDumpCallback = null;
        this.mPostviewCallback = null;
        this.mUsingPreviewAllocation = false;
        this.mZoomListener = null;
        this.mCameraId = cameraId;
        if ("off".equals(SystemProperties.get("persist.camera.hypnus.contrl", "on"))) {
            this.mHypnusCtrl = false;
        } else {
            this.mHypnusCtrl = true;
        }
        Log.d(TAG, "mHypnusCtrl is " + this.mHypnusCtrl);
        if (sHM == null) {
            sHM = new HypnusManager();
        }
        if (sHM != null && this.mHypnusCtrl) {
            sHM.hypnusSetAction(15, CAMERA_TIME_HYPNUS_2500);
        }
        HashMap<String, String> eventMap = new HashMap();
        eventMap.put(OppoManager.PARAM_PKG_NAME, ActivityThread.currentOpPackageName());
        eventMap.put("cameraId", String.valueOf(this.mCameraId));
        OppoStatistics.onCommon(ActivityThread.currentApplication().getApplicationContext(), "2012002", "openCamera", eventMap, false);
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        return native_setup(new WeakReference(this), cameraId, halVersion, ActivityThread.currentOpPackageName());
    }

    private int cameraInitNormal(int cameraId) {
        return cameraInitVersion(cameraId, -2);
    }

    public int cameraInitUnspecified(int cameraId) {
        return cameraInitVersion(cameraId, -1);
    }

    Camera(int cameraId) {
        this.mStereo3DModeForCamera = false;
        this.mEnableRaw16 = false;
        this.mFaceDetectionRunning = false;
        this.mObjectFace = new Face();
        this.mObjectRect = new Rect();
        this.mAutoFocusCallbackLock = new Object();
        this.mObjectCallbackLock = new Object();
        this.mLongshotEnable = false;
        int err = cameraInitNormal(cameraId);
        if (!checkInitErrors(err)) {
            return;
        }
        if (err == (-OsConstants.EACCES)) {
            throw new RuntimeException("Fail to connect to camera service");
        } else if (err == (-OsConstants.ENODEV)) {
            throw new RuntimeException("Camera initialization failed");
        } else {
            throw new RuntimeException("Unknown camera error");
        }
    }

    public static boolean checkInitErrors(int err) {
        return err != 0;
    }

    public static Camera openUninitialized() {
        return new Camera();
    }

    Camera() {
        this.mStereo3DModeForCamera = false;
        this.mEnableRaw16 = false;
        this.mFaceDetectionRunning = false;
        this.mObjectFace = new Face();
        this.mObjectRect = new Rect();
        this.mAutoFocusCallbackLock = new Object();
        this.mObjectCallbackLock = new Object();
        this.mLongshotEnable = false;
    }

    protected void finalize() {
        release();
    }

    public final void release() {
        if (sHM == null) {
            sHM = new HypnusManager();
        }
        if (sHM != null && this.mHypnusCtrl) {
            sHM.hypnusSetAction(15, CAMERA_TIME_HYPNUS_1500);
        }
        native_release();
        this.mFaceDetectionRunning = false;
    }

    public final void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        if (holder != null) {
            setPreviewSurface(holder.getSurface());
        } else {
            setPreviewSurface((Surface) null);
        }
    }

    public final void stopPreview() {
        if (sHM == null) {
            sHM = new HypnusManager();
        }
        if (sHM != null && this.mHypnusCtrl) {
            sHM.hypnusSetAction(15, CAMERA_TIME_HYPNUS_1500);
        }
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
        boolean z = true;
        this.mPreviewCallback = cb;
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
        boolean z = false;
        this.mPreviewCallback = cb;
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

    public final void addRawImageCallbackBuffer(byte[] callbackBuffer) {
        addCallbackBuffer(callbackBuffer, 128);
    }

    private final void addCallbackBuffer(byte[] callbackBuffer, int msgType) {
        if (msgType == 16 || msgType == 128) {
            _addCallbackBuffer(callbackBuffer, msgType);
            return;
        }
        throw new IllegalArgumentException("Unsupported message type: " + msgType);
    }

    public final Allocation createPreviewAllocation(RenderScript rs, int usage) throws RSIllegalArgumentException {
        Size previewSize = getParameters().getPreviewSize();
        Builder yuvBuilder = new Builder(rs, Element.createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_YUV));
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
            } else if (previewAllocation.getType().getElement().getDataKind() != DataKind.PIXEL_YUV) {
                throw new IllegalArgumentException("Allocation is not of a YUV type");
            } else {
                previewSurface = previewAllocation.getSurface();
                this.mUsingPreviewAllocation = true;
            }
        } else {
            this.mUsingPreviewAllocation = false;
        }
        setPreviewCallbackSurface(previewSurface);
    }

    private static void postEventFromNative(Object camera_ref, int what, int arg1, int arg2, Object obj) {
        Camera c = (Camera) ((WeakReference) camera_ref).get();
        if (!(c == null || c.mEventHandler == null)) {
            c.mEventHandler.sendMessage(c.mEventHandler.obtainMessage(what, arg1, arg2, obj));
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
            msgType = 2;
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
        if (sHM == null) {
            sHM = new HypnusManager();
        }
        if (!(sHM == null || !this.mHypnusCtrl || this.mLongshotEnable)) {
            sHM.hypnusSetAction(15, 4000);
        }
        native_takePicture(msgType);
        this.mFaceDetectionRunning = false;
        String pkgName = ActivityThread.currentPackageName();
        HashMap<String, String> eventMap = new HashMap();
        eventMap.put(OppoManager.PARAM_PKG_NAME, pkgName);
        eventMap.put("cameraId", String.valueOf(this.mCameraId));
        OppoStatistics.onCommon(ActivityThread.currentApplication().getApplicationContext(), "2012002", "capture", eventMap, false);
    }

    public final void setRaw16Callback(MetadataCallback meta, PictureCallback raw16) {
        this.mMetadataCallbacks = meta;
        this.mRaw16Callbacks = raw16;
    }

    public final void enableRaw16(boolean enable) {
        this.mEnableRaw16 = enable;
        enableRaw16Callback(this.mEnableRaw16);
    }

    public final boolean enableShutterSound(boolean enabled) {
        if (!enabled) {
            try {
                if (Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE)).isCameraSoundForced()) {
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Audio service is unavailable for queries");
            }
        }
        return _enableShutterSound(enabled);
    }

    public final boolean disableShutterSound() {
        return _enableShutterSound(false);
    }

    public final void setZoomChangeListener(OnZoomChangeListener listener) {
        this.mZoomListener = listener;
    }

    public final void setFaceDetectionListener(FaceDetectionListener listener) {
        this.mFaceListener = listener;
    }

    public final void startFaceDetection() {
        if (this.mFaceDetectionRunning) {
            throw new RuntimeException("Face detection is already running");
        }
        _startFaceDetection(0);
        this.mFaceDetectionRunning = true;
    }

    public final void stopFaceDetection() {
        _stopFaceDetection();
        this.mFaceDetectionRunning = false;
    }

    public final void setErrorCallback(ErrorCallback cb) {
        this.mErrorCallback = cb;
    }

    public static String getProperty(String key, String def) {
        return native_getProperty(key, def);
    }

    public static void setProperty(String key, String val) {
        native_setProperty(key, val);
    }

    public static int cameraHardwareCheck(int cameraId) {
        return native_CameraHardwareCheck(cameraId);
    }

    public final void setGestureCallback(GestureCallback cb) {
        this.mGestureCallback = cb;
    }

    public void startGestureDetection() {
        startGDPreview();
    }

    public void stopGestureDetection() {
        cancelGDPreview();
    }

    public final void setAsdCallback(AsdCallback cb) {
        this.mAsdCallback = cb;
    }

    public final void setAFDataCallback(AFDataCallback cb) {
        this.mAFDataCallback = cb;
    }

    public final void setAutoRamaCallback(AutoRamaCallback cb) {
        this.mAutoRamaCallback = cb;
    }

    public final void setAutoRamaMoveCallback(AutoRamaMoveCallback cb) {
        this.mAutoRamaMoveCallback = cb;
    }

    public final void setHdrOriginalCallback(HdrOriginalCallback cb) {
        this.mHdrOriginalCallback = cb;
    }

    public final void setStereoCameraDataCallback(StereoCameraDataCallback cb) {
        this.mStereoCameraDataCallback = cb;
    }

    public final void setStereoCameraWarningCallback(StereoCameraWarningCallback cb) {
        this.mStereoCameraWarningCallback = cb;
    }

    public final void setDistanceInfoCallback(DistanceInfoCallback cb) {
        this.mDistanceInfoCallback = cb;
    }

    public final void setFbOriginalCallback(FbOriginalCallback cb) {
        this.mFbOriginalCallback = cb;
    }

    public final void setUncompressedImageCallback(PictureCallback cb) {
        this.mUncompressedImageCallback = cb;
    }

    public final void startAutoRama(int num) {
        startAUTORAMA(num);
    }

    public void stopAutoRama(int isMerge) {
        stopAUTORAMA(isMerge);
    }

    public final void setMainFaceCoordinate(int x, int y) {
        setMainFace(x, y);
    }

    public final void cancelMainFaceInfo() {
        cancelMainFace();
    }

    public final void startObjectTracking(int x, int y) {
        startOT(x, y);
    }

    public final void stopObjectTracking() {
        stopOT();
    }

    public final void setObjectTrackingListener(ObjectTrackingListener listener) {
        synchronized (this.mObjectCallbackLock) {
            this.mObjectListener = listener;
        }
    }

    public void setPreviewRawDumpCallback(PreviewRawDumpCallback callback) {
        this.mPreviewRawDumpCallback = callback;
    }

    public void setPreviewDoneCallback(ZSDPreviewDone callback) {
        this.mPreviewDoneCallback = callback;
    }

    public void setContinuousShotCallback(ContinuousShotCallback callback) {
        this.mCSDoneCallback = callback;
    }

    public void setVendorDataCallback(VendorDataCallback callback) {
        this.mVendorDataCallback = callback;
    }

    public boolean isParameterSame(Parameters newparams) {
        Parameters oldParam = getParameters();
        List<String> keyList = new ArrayList();
        for (String add : newparams.mMap.keySet()) {
            keyList.add(add);
        }
        Collections.sort(keyList);
        for (String key : keyList) {
            if (!key.equals("disp-rot-supported") && !((String) newparams.mMap.get(key)).equals(oldParam.mMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    public void setParameters(Parameters params) {
        if (this.mUsingPreviewAllocation) {
            Size newPreviewSize = params.getPreviewSize();
            Size currentPreviewSize = getParameters().getPreviewSize();
            if (!(newPreviewSize.width == currentPreviewSize.width && newPreviewSize.height == currentPreviewSize.height)) {
                throw new IllegalStateException("Cannot change preview size while a preview allocation is configured.");
            }
        }
        String curPackageName = ActivityThread.currentOpPackageName();
        String yicheapp = "com.yiche.price";
        Log.d(TAG, "current activity packname: " + curPackageName);
        if (curPackageName != null && yicheapp.equals(curPackageName)) {
            Log.d(TAG, "it's special app ");
            if (isParameterSame(params)) {
                Log.d(TAG, "setParameters is same... ");
                return;
            }
        }
        String value = params.get("cap-mode");
        if (Parameters.CAPTURE_MODE_CONTINUOUS_SHOT.equals(value) && !this.mLongshotEnable) {
            this.mLongshotEnable = true;
            if (sHM == null) {
                sHM = new HypnusManager();
            }
            if (sHM != null && this.mHypnusCtrl) {
                sHM.hypnusSetAction(15, 4000);
            }
        } else if (!Parameters.CAPTURE_MODE_CONTINUOUS_SHOT.equals(value) && this.mLongshotEnable) {
            this.mLongshotEnable = false;
            if (sHM == null) {
                sHM = new HypnusManager();
            }
            if (sHM != null && this.mHypnusCtrl) {
                sHM.hypnusSetAction(15, 0);
            }
        }
        String s = params.flatten();
        printParameter(s);
        native_setParameters(s);
    }

    public static boolean isRestricted(int pid) {
        boolean ret = false;
        try {
            InputStreamReader inReader = new InputStreamReader(new FileInputStream("/proc/" + pid + "/cmdline"));
            StringBuilder buffer = new StringBuilder();
            char[] buf = new char[1];
            while (inReader.read(buf) != -1) {
                try {
                    buffer.append(buf[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            inReader.close();
            if (buffer.toString().contains("com.google.android.apps.unveil")) {
                ret = true;
            }
            return ret;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public static String getScreenSize() {
        String mHD720NOPK = "1184x720";
        String mWVGAScreenSize = "800x480";
        String mQhdScreenSize = "960x540";
        String m720P = "1280x720";
        String screenSize = "800x480";
        Display dispaly = DisplayManagerGlobal.getInstance().getCompatibleDisplay(0, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
        Point size = new Point();
        dispaly.getSize(size);
        if (size.x > size.y) {
            screenSize = size.x + "x" + size.y;
        } else {
            screenSize = size.y + "x" + size.x;
        }
        if ("1184x720".equals(screenSize)) {
            return "800x480";
        }
        if ("960x540".equals(screenSize)) {
            return "800x480";
        }
        if ("1280x720".equals(screenSize)) {
            return "800x480";
        }
        return screenSize;
    }

    public void setStereo3DModeForCamera(boolean enable) {
        this.mStereo3DModeForCamera = enable;
    }

    public Parameters getParameters() {
        Parameters p = new Parameters(this, null);
        String s = native_getParameters();
        p.unflatten(s);
        printParameter(s);
        p.setStereo3DMode(this.mStereo3DModeForCamera);
        return p;
    }

    public static Parameters getEmptyParameters() {
        Camera camera = new Camera();
        camera.getClass();
        return new Parameters(camera, null);
    }

    public static Parameters getParametersCopy(Parameters parameters) {
        if (parameters == null) {
            throw new NullPointerException("parameters must not be null");
        }
        Camera camera = parameters.getOuter();
        camera.getClass();
        Parameters p = new Parameters(camera, null);
        p.copyFrom(parameters);
        return p;
    }

    private void printParameter(String parameters) {
        if (!Log.isLoggable(TAG, 3)) {
            return;
        }
        if (((long) parameters.length()) <= 1000) {
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
