package com.android.server.face.camera;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.face.ClientMode;
import android.os.SystemClock;
import android.os.Trace;
import android.view.Surface;
import com.android.server.SystemService;
import com.android.server.face.FaceService;
import com.android.server.face.camera.CaptureManager.CameraErrorCallback;
import com.android.server.face.health.HealthState;
import com.android.server.face.utils.CameraUtils;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.android.server.face.utils.Utils;
import com.android.server.storage.OppoDeviceStorageMonitorService;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CameraView {
    private static final String ISO_VALUE_AUTO = "auto";
    private static final String KEY_FACEPREVIEW_MODE = "face-preview-mode";
    private static final String KEY_ISO = "iso";
    private static final String KEY_MTK_ISOSPEED_MODE = "iso-speed";
    private static final String KEY_PREEEMPTIVE_PRIORITY = "preemptive-priority";
    private static final int PRIORITY_VALUE_OF_FACESERVICE = 100;
    private static final String PROP_EXPOSURE_VALUE = "exposure";
    private static final String PROP_ISO_VALUE = "iso";
    private static final String TAG = "FaceService.CameraView";
    private byte[] mBuffer;
    private Camera mCamera;
    private final CameraErrorCallback mCameraErrorCallback;
    private final Context mContext;
    private int mCurrentExposureValue = 0;
    private String mCurrentISOValue = "auto";
    private ClientMode mCurrentMode = ClientMode.NONE;
    private int mDegrees;
    private List<Area> mFocusArea = null;
    private boolean mIsCameraOpened = false;
    private final boolean mIsMtkPlatform = Utils.isMtkPlatform();
    private boolean mIsPreviewing = false;
    private List<Area> mMeteringArea = null;
    private long mOpenCameraStartTime = 0;
    private Parameters mParameters;
    private PreviewCallback mPreviewCallback;
    private int mPreviewHeight = SystemService.PHASE_LOCK_SETTINGS_READY;
    private int mPreviewWidth = VoldResponseCode.DISK_CREATED;
    private long mStarPreviewStartTime = 0;
    private SurfaceTexture mSurfaceTexture;

    public CameraView(Context context, CameraErrorCallback cameraErrorCallback) {
        this.mContext = context;
        this.mCameraErrorCallback = cameraErrorCallback;
    }

    public int openCamera() throws RuntimeException {
        if (this.mCamera != null) {
            return -1;
        }
        try {
            LogUtil.d(TAG, "openCamera begin");
            this.mOpenCameraStartTime = SystemClock.uptimeMillis();
            Trace.traceBegin(OppoDeviceStorageMonitorService.KB_BYTES, "FaceService.CameraView.openCamera");
            this.mCamera = Camera.open(1);
            this.mCamera.setErrorCallback(this.mCameraErrorCallback);
            this.mIsCameraOpened = true;
            Trace.traceEnd(OppoDeviceStorageMonitorService.KB_BYTES);
            TimeUtils.calculateTime(TAG, "openCamera", SystemClock.uptimeMillis() - this.mOpenCameraStartTime);
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
            LogUtil.d(TAG, HealthState.SET_PREVIEW_SURFACE);
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
        if (this.mCurrentMode == ClientMode.ENROLL) {
            this.mParameters.set(KEY_FACEPREVIEW_MODE, 1);
        } else {
            this.mParameters.set(KEY_FACEPREVIEW_MODE, 2);
        }
        this.mParameters.setPreviewFormat(17);
        this.mParameters.setPictureFormat(256);
        setExposureCompensation(this.mCurrentExposureValue);
        setISOValue(this.mCurrentISOValue);
        this.mParameters.set(KEY_PREEEMPTIVE_PRIORITY, 100);
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
            this.mParameters.set("iso", isoValue);
            this.mCurrentISOValue = isoValue;
        } else if ("auto".equals(isoValue)) {
            LogUtil.d(TAG, "setISOValue, auto in mtk platform");
            this.mParameters.set("iso", isoValue);
            this.mParameters.set(KEY_MTK_ISOSPEED_MODE, isoValue);
            this.mCurrentISOValue = isoValue;
        } else if (isoValue.contains("ISO")) {
            String tmp = isoValue.replace("ISO", "");
            LogUtil.d(TAG, "setISOValue, tmp = " + tmp + " in mtk platform");
            this.mParameters.set("iso", tmp);
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

    public void setCurrentMode(ClientMode mode) {
        this.mCurrentMode = mode;
    }

    public int setPreviewSurface(Surface surface) {
        LogUtil.d(TAG, HealthState.SET_PREVIEW_SURFACE);
        if (this.mCamera == null) {
            return -1;
        }
        try {
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.setPreviewSurface(surface);
            TimeUtils.calculateTime(TAG, HealthState.SET_PREVIEW_SURFACE, SystemClock.uptimeMillis() - startTime);
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
            this.mStarPreviewStartTime = SystemClock.uptimeMillis();
            Trace.traceBegin(OppoDeviceStorageMonitorService.KB_BYTES, "FaceService.CameraView.startPreview");
            this.mCamera.setPreviewCallbackWithBuffer(this.mPreviewCallback);
            this.mCamera.startPreview();
            this.mIsPreviewing = true;
            Trace.traceEnd(OppoDeviceStorageMonitorService.KB_BYTES);
            TimeUtils.calculateTime(TAG, "startPreview", SystemClock.uptimeMillis() - this.mStarPreviewStartTime);
            long startTime = SystemClock.uptimeMillis();
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
            LogUtil.d(TAG, HealthState.STOP_RREVIEW);
            long startTime = SystemClock.uptimeMillis();
            this.mCamera.stopFaceDetection();
            TimeUtils.calculateTime(TAG, "stopFaceDetection", SystemClock.uptimeMillis() - startTime);
            startTime = SystemClock.uptimeMillis();
            this.mCamera.setPreviewCallbackWithBuffer(null);
            this.mCamera.stopPreview();
            this.mIsPreviewing = false;
            TimeUtils.calculateTime(TAG, HealthState.STOP_RREVIEW, SystemClock.uptimeMillis() - startTime);
            LogUtil.d(TAG, "From starPreview to stopPreview, time spent = " + (SystemClock.uptimeMillis() - this.mStarPreviewStartTime) + "ms");
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
                LogUtil.d(TAG, "From openCamera to releaseCamera, time spent = " + (SystemClock.uptimeMillis() - this.mOpenCameraStartTime) + "ms");
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

    public void updateCameraParameters(PrintWriter pw, String parameterArgs) {
        LogUtil.d(TAG, "updateCameraParameters, " + parameterArgs);
        StringReader sr = new StringReader(parameterArgs.replace(',', 13));
        Properties pros = new Properties();
        try {
            pros.load(sr);
            String iso = pros.getProperty("iso");
            if (iso != null) {
                this.mCurrentISOValue = iso;
            }
            LogUtil.d(TAG, "iso = " + iso + ", mCurrentISOValue = " + this.mCurrentISOValue);
            String exposure = pros.getProperty(PROP_EXPOSURE_VALUE);
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
