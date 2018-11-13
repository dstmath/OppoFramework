package com.android.server.face.camera;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;
import com.android.server.SystemService;
import com.android.server.face.FaceService;
import com.android.server.face.camera.CaptureManager.CameraErrorCallback;
import com.android.server.face.utils.CameraUtils;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.android.server.face.utils.Utils;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CameraView {
    private static final String KEY_ISO = "iso";
    private static final String KEY_MTK_ISOSPEED_MODE = "iso-speed";
    private static final int SCALE_TYPE_16_9 = 2;
    private static final int SCALE_TYPE_4_3 = 1;
    private static final int SCALE_TYPE_AUTO = 0;
    private static final String TAG = "FaceService.CameraView";
    private final String ISO_VALUE_AUTO = "auto";
    private final boolean IS_MTK_PLATFORM = Utils.isMtkPlatform();
    private final String KEY_PREEEMPTIVE_PRIORITY = "preemptive-priority";
    private final int PRIORITY_VALUE_OF_FACESERVICE = 100;
    private final String PROP_EXPOSURE_VALUE = "exposure";
    private final String PROP_ISO_VALUE = KEY_ISO;
    private byte[] mBuffer;
    private Camera mCamera;
    private final CameraErrorCallback mCameraErrorCallback;
    private final Context mContext;
    private int mCurrentExposureValue = 0;
    private String mCurrentISOValue = "auto";
    private int mDegrees;
    private List<Area> mFocusArea = null;
    private boolean mIsCameraOpened = false;
    private boolean mIsPreviewing = false;
    private List<Area> mMeteringArea = null;
    private Parameters mParameters;
    private PreviewCallback mPreviewCallback;
    private int mPreviewHeight;
    private final float mPreviewScale;
    private Size mPreviewSize;
    private int mPreviewWidth;
    private final int mResolution = SystemService.PHASE_LOCK_SETTINGS_READY;
    private SurfaceTexture mSurfaceTexture;

    public CameraView(Context context, CameraErrorCallback cameraErrorCallback) {
        this.mContext = context;
        this.mCameraErrorCallback = cameraErrorCallback;
        this.mPreviewScale = getPreviewScale(1);
    }

    public int openCamera() throws RuntimeException {
        if (this.mCamera != null) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "openCamera begin");
            long startTime = SystemClock.uptimeMillis();
            this.mCamera = Camera.open(1);
            this.mCamera.setErrorCallback(this.mCameraErrorCallback);
            this.mIsCameraOpened = true;
            TimeUtils.calculateTime(TAG, "openCamera", SystemClock.uptimeMillis() - startTime);
            LogUtil.d(TAG, "openCamera end");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int setFaceDetectionListener(FaceDetectionListener listener) {
        if (this.mCamera == null || !FaceService.DEBUG) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "setFaceDetectionListener begin");
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.setFaceDetectionListener(listener);
            TimeUtils.calculateTime(TAG, "setFaceDetectionListener", SystemClock.uptimeMillis() - startTime);
            LogUtil.d(TAG, "setFaceDetectionListener end");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int initCamera(PreviewCallback callback, Surface surface) {
        int result = getParameters();
        if (result != 0) {
            return result;
        }
        initParameters();
        this.mPreviewCallback = callback;
        result = setParameters();
        if (result != 0) {
            return result;
        }
        if (surface != null) {
            LogUtil.d(TAG, "surface.isValid() = " + surface.isValid());
            result = setPreviewSurface(surface);
            LogUtil.d(TAG, "setPreviewSurface");
        } else {
            result = setSurfaceTextTure();
            LogUtil.d(TAG, "setSurfaceTextTure");
        }
        return result;
    }

    private int getParameters() {
        LogUtil.d(TAG, "getParameters");
        if (this.mCamera == null) {
            return -1;
        }
        try {
            long startTime = SystemClock.uptimeMillis();
            this.mParameters = this.mCamera.getParameters();
            TimeUtils.calculateTime(TAG, "getParameters", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void initParameters() {
        long startTime = SystemClock.uptimeMillis();
        List<String> supportedFlashModes = this.mParameters.getSupportedFlashModes();
        if (supportedFlashModes != null && supportedFlashModes.contains("off")) {
            this.mParameters.setFlashMode("off");
        }
        List<String> supportedFocusModes = this.mParameters.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains("auto")) {
            this.mParameters.setFocusMode("auto");
        }
        this.mParameters.setPreviewFormat(17);
        this.mParameters.setPictureFormat(256);
        setExposureCompensation(this.mCurrentExposureValue);
        setISOValue(this.mCurrentISOValue);
        this.mParameters.set("preemptive-priority", 100);
        this.mPreviewSize = getFitPreviewSize(this.mParameters);
        this.mPreviewWidth = this.mPreviewSize.width;
        this.mPreviewHeight = this.mPreviewSize.height;
        this.mParameters.setPreviewSize(this.mPreviewWidth, this.mPreviewHeight);
        this.mDegrees = CameraUtils.getDisplayRotation(this.mContext);
        TimeUtils.calculateTime(TAG, "initParameters", SystemClock.uptimeMillis() - startTime);
    }

    private void setExposureCompensation(int value) {
        long startTime = SystemClock.uptimeMillis();
        int minValue = this.mParameters.getMinExposureCompensation();
        int maxValue = this.mParameters.getMaxExposureCompensation();
        LogUtil.d(TAG, "setExposureCompensation value = " + value + ", minValue = " + minValue + ", maxValue = " + maxValue);
        if (value < minValue) {
            value = minValue;
        }
        if (value > maxValue) {
            value = maxValue;
        }
        this.mParameters.setExposureCompensation(value);
        this.mCurrentExposureValue = value;
        TimeUtils.calculateTime(TAG, "setExposureCompensation", SystemClock.uptimeMillis() - startTime);
    }

    public void setISOValue(String isoValue) {
        long startTime = SystemClock.uptimeMillis();
        if (Utils.isQualcommPlatform(this.mContext)) {
            LogUtil.d(TAG, "setISOValue, mCurrentISOValue = " + isoValue + " in isQualcommPlatform");
            this.mParameters.set(KEY_ISO, isoValue);
            this.mCurrentISOValue = isoValue;
        } else if ("auto".equals(isoValue)) {
            LogUtil.d(TAG, "setISOValue, auto in mtk platform");
            this.mParameters.set(KEY_ISO, isoValue);
            this.mParameters.set(KEY_MTK_ISOSPEED_MODE, isoValue);
            this.mCurrentISOValue = isoValue;
        } else if (isoValue.contains("ISO")) {
            String tmp = isoValue.replace("ISO", IElsaManager.EMPTY_PACKAGE);
            LogUtil.d(TAG, "setISOValue, tmp = " + tmp + " in mtk platform");
            this.mParameters.set(KEY_ISO, tmp);
            this.mParameters.set(KEY_MTK_ISOSPEED_MODE, tmp);
            this.mCurrentISOValue = tmp;
        }
        TimeUtils.calculateTime(TAG, "setISOValue", SystemClock.uptimeMillis() - startTime);
    }

    public int updateTouchAEParameter() {
        if (this.mCamera == null) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "setParameters");
            long startTime = SystemClock.uptimeMillis();
            if (this.mMeteringArea == null) {
                this.mMeteringArea = new ArrayList();
                this.mMeteringArea.add(new Area(new Rect(-878, -413, -215, 470), 1000));
            }
            this.mParameters.setMeteringAreas(this.mMeteringArea);
            this.mCamera.setParameters(this.mParameters);
            TimeUtils.calculateTime(TAG, "updateTouchAEParameter", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int setParameters() {
        if (this.mCamera == null) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "setParameters");
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.setParameters(this.mParameters);
            TimeUtils.calculateTime(TAG, "setParameters", SystemClock.uptimeMillis() - startTime);
            startTime = SystemClock.uptimeMillis();
            this.mCamera.setDisplayOrientation(this.mDegrees);
            TimeUtils.calculateTime(TAG, "setDisplayOrientation", SystemClock.uptimeMillis() - startTime);
            startTime = SystemClock.uptimeMillis();
            this.mBuffer = new byte[(((this.mPreviewWidth * this.mPreviewHeight) * 3) / 2)];
            this.mCamera.addCallbackBuffer(this.mBuffer);
            TimeUtils.calculateTime(TAG, "addCallbackBuffer", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void addCallbackBuffer() {
        if (this.mCamera != null) {
            try {
                this.mCamera.addCallbackBuffer(this.mBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int setSurfaceTextTure() {
        this.mSurfaceTexture = new SurfaceTexture(9999);
        if (this.mCamera != null) {
            try {
                if (this.mSurfaceTexture != null) {
                    LogUtil.d(TAG, "setPreviewTexture");
                    long startTime = SystemClock.uptimeMillis();
                    this.mCamera.setPreviewTexture(this.mSurfaceTexture);
                    TimeUtils.calculateTime(TAG, "setPreviewTexture", SystemClock.uptimeMillis() - startTime);
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    public int setPreviewSurface(Surface surface) {
        LogUtil.d(TAG, "setPreviewSurface");
        if (this.mCamera == null) {
            return -1;
        }
        try {
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.setPreviewSurface(surface);
            TimeUtils.calculateTime(TAG, "setPreviewSurface", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int startPreview() {
        if (this.mCamera == null) {
            return -1;
        }
        LogUtil.d(TAG, "startPreview");
        try {
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.setPreviewCallbackWithBuffer(this.mPreviewCallback);
            this.mCamera.startPreview();
            this.mIsPreviewing = true;
            TimeUtils.calculateTime(TAG, "startPreview", SystemClock.uptimeMillis() - startTime);
            startTime = SystemClock.uptimeMillis();
            this.mCamera.startFaceDetection();
            TimeUtils.calculateTime(TAG, "startFaceDetection", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int stopPreview() {
        if (this.mCamera == null) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "stopPreview");
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.stopFaceDetection();
            TimeUtils.calculateTime(TAG, "stopFaceDetection", SystemClock.uptimeMillis() - startTime);
            startTime = SystemClock.uptimeMillis();
            this.mCamera.setPreviewCallbackWithBuffer(null);
            this.mCamera.stopPreview();
            this.mIsPreviewing = false;
            TimeUtils.calculateTime(TAG, "stopPreview", SystemClock.uptimeMillis() - startTime);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int releaseCamera() {
        if (this.mCamera != null) {
            LogUtil.d(TAG, "releaseCamera");
            try {
                long startTime = SystemClock.uptimeMillis();
                this.mCamera.release();
                this.mCamera = null;
                TimeUtils.calculateTime(TAG, "releaseCamera", SystemClock.uptimeMillis() - startTime);
            } catch (Exception e) {
                this.mIsCameraOpened = false;
                this.mCamera = null;
                e.printStackTrace();
                return -1;
            }
        }
        this.mIsCameraOpened = false;
        if (this.mSurfaceTexture != null) {
            this.mSurfaceTexture.release();
        }
        return 0;
    }

    private float getPreviewScale(int type) {
        if (type == 1) {
            return 0.75f;
        }
        if (type == 2) {
            return 0.5625f;
        }
        return getScreenScale();
    }

    private float getScreenScale() {
        float scale;
        long startTime = SystemClock.uptimeMillis();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        float width = (float) displayMetrics.widthPixels;
        float height = (float) displayMetrics.heightPixels;
        if (width > height) {
            scale = height / width;
        } else {
            scale = width / height;
        }
        LogUtil.d(TAG, "width = " + width + "height = " + height + "scale : " + scale);
        TimeUtils.calculateTime(TAG, "getScreenScale", SystemClock.uptimeMillis() - startTime);
        if (Math.abs(scale - 0.75f) > Math.abs(scale - 0.5625f)) {
            return 0.5625f;
        }
        return 0.75f;
    }

    private Size getFitPreviewSize(Parameters parameters) {
        long startTime = SystemClock.uptimeMillis();
        List<Size> previewSizes = parameters.getSupportedPreviewSizes();
        int minDelta = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < previewSizes.size(); i++) {
            Size previewSize = (Size) previewSizes.get(i);
            if (((float) previewSize.width) * this.mPreviewScale == ((float) previewSize.height)) {
                int delta = Math.abs(480 - previewSize.height);
                if (delta == 0) {
                    return previewSize;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        TimeUtils.calculateTime(TAG, "getFitPreviewSize", SystemClock.uptimeMillis() - startTime);
        return (Size) previewSizes.get(index);
    }

    public Camera getCamera() {
        return this.mCamera;
    }

    public boolean isCameraOpened() {
        return this.mIsCameraOpened;
    }

    public boolean isPreviewing() {
        return this.mIsPreviewing;
    }

    public int getPreviewWidth() {
        return this.mPreviewWidth;
    }

    public int getPreviewHeight() {
        return this.mPreviewHeight;
    }

    public int getDegrees() {
        return this.mDegrees;
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        pw.print(prefix);
        pw.println("CaptureView dump");
        pw.print(subPrefix);
        pw.println("isCameraOpend = " + this.mIsCameraOpened);
        pw.print(subPrefix);
        pw.println("isPreviewing = " + this.mIsPreviewing);
        pw.print(subPrefix);
        pw.println("PreviewWidth = " + this.mPreviewWidth + ", PreviewHeight = " + this.mPreviewHeight);
        pw.print(subPrefix);
        pw.println("degrees = " + this.mDegrees);
        pw.print(subPrefix);
        pw.println("mCurrentExposureValue = " + this.mCurrentExposureValue);
        pw.print(subPrefix);
        pw.println("mCurrentISOValue = " + this.mCurrentISOValue);
    }

    public void updateCameraParameters(PrintWriter pw, String parameter_args) {
        LogUtil.d(TAG, "updateCameraParameters, " + parameter_args);
        StringReader sr = new StringReader(parameter_args.replace(',', 13));
        Properties pros = new Properties();
        try {
            pros.load(sr);
            String iso = pros.getProperty(KEY_ISO);
            if (iso != null) {
                this.mCurrentISOValue = iso;
            }
            LogUtil.d(TAG, "iso = " + iso + ", mCurrentISOValue = " + this.mCurrentISOValue);
            String exposure = pros.getProperty("exposure");
            if (iso != null) {
                try {
                    this.mCurrentExposureValue = Integer.parseInt(exposure);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            LogUtil.d(TAG, "exposure = " + exposure + ", mCurrentExposureValue = " + this.mCurrentExposureValue);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
