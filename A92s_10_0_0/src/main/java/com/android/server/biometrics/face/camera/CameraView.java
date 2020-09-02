package com.android.server.biometrics.face.camera;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.face.ClientMode;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Slog;
import android.view.Surface;
import com.android.server.SystemService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.biometrics.face.camera.CaptureManager;
import com.android.server.biometrics.face.health.HealthMonitor;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.face.utils.CameraUtils;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.TimeUtils;
import com.android.server.biometrics.face.utils.Utils;
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
    private static final int TIMEOUT_CAMERA_BINDERCALL_CHECK = 3000;
    private byte[] mBuffer;
    private Camera mCamera;
    private final CaptureManager.CameraErrorCallback mCameraErrorCallback;
    private final Context mContext;
    private int mCurrentExposureValue = 0;
    private String mCurrentISOValue = "auto";
    private ClientMode mCurrentMode = ClientMode.NONE;
    private int mDegrees;
    private List<Camera.Area> mFocusArea = null;
    private final HealthMonitor mHealthMonitor;
    private boolean mIsCameraOpened = false;
    private final boolean mIsMtkPlatform = Utils.isMtkPlatform();
    private boolean mIsPreviewing = false;
    private List<Camera.Area> mMeteringArea = null;
    private long mOpenCameraStartTime = 0;
    private Camera.Parameters mParameters;
    private Camera.PreviewCallback mPreviewCallback;
    private int mPreviewHeight = SystemService.PHASE_LOCK_SETTINGS_READY;
    private int mPreviewWidth = 640;
    private long mStarPreviewStartTime = 0;
    private SurfaceTexture mSurfaceTexture;

    public CameraView(Context context, CaptureManager.CameraErrorCallback cameraErrorCallback) {
        this.mContext = context;
        this.mCameraErrorCallback = cameraErrorCallback;
        this.mHealthMonitor = HealthMonitor.getHealthMonitor(context);
    }

    /* JADX INFO: finally extract failed */
    public int openCamera() throws RuntimeException {
        if (this.mCamera != null) {
            return -1;
        }
        try {
            Slog.d(TAG, "openCamera begin");
            this.mOpenCameraStartTime = SystemClock.uptimeMillis();
            Trace.traceBegin(1024, "FaceService.CameraView.openCamera");
            String session = String.valueOf(this.mOpenCameraStartTime);
            try {
                this.mHealthMonitor.start(HealthState.OPEN_CAMERA, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, session);
                this.mCamera = Camera.open(1);
                this.mHealthMonitor.stop(HealthState.OPEN_CAMERA, session);
                this.mCamera.setErrorCallback(this.mCameraErrorCallback);
                this.mIsCameraOpened = true;
                Trace.traceEnd(1024);
                TimeUtils.calculateTime(TAG, HealthState.OPEN_CAMERA, SystemClock.uptimeMillis() - this.mOpenCameraStartTime);
                Slog.d(TAG, "openCamera end");
                return 0;
            } catch (Throwable th) {
                this.mHealthMonitor.stop(HealthState.OPEN_CAMERA, session);
                throw th;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int setFaceDetectionListener(Camera.FaceDetectionListener listener) {
        if (this.mCamera == null || !Utils.canCatchLog()) {
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

    public int initCamera(Camera.PreviewCallback callback, Surface surface) {
        int result = getParameters();
        if (result != 0) {
            return result;
        }
        initParameters();
        this.mPreviewCallback = callback;
        int result2 = setParameters();
        if (result2 != 0) {
            return result2;
        }
        if (surface != null) {
            LogUtil.d(TAG, "surface.isValid() = " + surface.isValid());
            int result3 = setPreviewSurface(surface);
            LogUtil.d(TAG, HealthState.SET_PREVIEW_SURFACE);
            return result3;
        }
        int result4 = setSurfaceTextTure();
        LogUtil.d(TAG, "setSurfaceTextTure");
        return result4;
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
                this.mMeteringArea.add(new Camera.Area(new Rect(-878, -413, -215, 470), 1000));
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
            long startTime2 = SystemClock.uptimeMillis();
            this.mCamera.setDisplayOrientation(this.mDegrees);
            TimeUtils.calculateTime(TAG, "setDisplayOrientation", SystemClock.uptimeMillis() - startTime2);
            long startTime3 = SystemClock.uptimeMillis();
            this.mBuffer = new byte[(((this.mPreviewWidth * this.mPreviewHeight) * 3) / 2)];
            this.mCamera.addCallbackBuffer(this.mBuffer);
            TimeUtils.calculateTime(TAG, "addCallbackBuffer", SystemClock.uptimeMillis() - startTime3);
            return 0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void addCallbackBuffer() {
        Camera camera = this.mCamera;
        if (camera != null) {
            try {
                camera.addCallbackBuffer(this.mBuffer);
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

    /* JADX INFO: finally extract failed */
    public int startPreview() {
        if (this.mCamera == null) {
            return -1;
        }
        Slog.d(TAG, HealthState.START_PREVIEW);
        try {
            this.mStarPreviewStartTime = SystemClock.uptimeMillis();
            Trace.traceBegin(1024, "FaceService.CameraView.startPreview");
            String session = String.valueOf(this.mStarPreviewStartTime);
            try {
                this.mHealthMonitor.start(HealthState.START_PREVIEW, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, session);
                this.mCamera.setPreviewCallbackWithBuffer(this.mPreviewCallback);
                this.mCamera.startPreview();
                this.mHealthMonitor.stop(HealthState.START_PREVIEW, session);
                this.mIsPreviewing = true;
                Trace.traceEnd(1024);
                TimeUtils.calculateTime(TAG, HealthState.START_PREVIEW, SystemClock.uptimeMillis() - this.mStarPreviewStartTime);
                long startTime = SystemClock.uptimeMillis();
                this.mCamera.startFaceDetection();
                TimeUtils.calculateTime(TAG, "startFaceDetection", SystemClock.uptimeMillis() - startTime);
                return 0;
            } catch (Throwable th) {
                this.mHealthMonitor.stop(HealthState.START_PREVIEW, session);
                throw th;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* JADX INFO: finally extract failed */
    public int stopPreview() {
        if (this.mCamera == null) {
            return -1;
        }
        try {
            Slog.d(TAG, "stopPreview");
            long startTime = SystemClock.uptimeMillis();
            String session = String.valueOf(startTime);
            try {
                this.mHealthMonitor.start("stopPreview", BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, session);
                this.mCamera.stopFaceDetection();
                TimeUtils.calculateTime(TAG, "stopFaceDetection", SystemClock.uptimeMillis() - startTime);
                long startTime2 = SystemClock.uptimeMillis();
                this.mCamera.setPreviewCallbackWithBuffer(null);
                this.mCamera.stopPreview();
                this.mHealthMonitor.stop("stopPreview", session);
                this.mIsPreviewing = false;
                TimeUtils.calculateTime(TAG, "stopPreview", SystemClock.uptimeMillis() - startTime2);
                Slog.d(TAG, "From starPreview to stopPreview, time spent = " + (SystemClock.uptimeMillis() - this.mStarPreviewStartTime) + "ms");
                return 0;
            } catch (Throwable th) {
                this.mHealthMonitor.stop("stopPreview", session);
                throw th;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* JADX INFO: finally extract failed */
    public int releaseCamera() {
        if (this.mCamera != null) {
            Slog.d(TAG, HealthState.RELEASE_CAMERA);
            try {
                long startTime = SystemClock.uptimeMillis();
                String session = String.valueOf(startTime);
                try {
                    this.mHealthMonitor.start(HealthState.RELEASE_CAMERA, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, session);
                    this.mCamera.release();
                    this.mHealthMonitor.stop(HealthState.RELEASE_CAMERA, session);
                    this.mCamera = null;
                    TimeUtils.calculateTime(TAG, HealthState.RELEASE_CAMERA, SystemClock.uptimeMillis() - startTime);
                    Slog.d(TAG, "From openCamera to releaseCamera, time spent = " + (SystemClock.uptimeMillis() - this.mOpenCameraStartTime) + "ms");
                } catch (Throwable th) {
                    this.mHealthMonitor.stop(HealthState.RELEASE_CAMERA, session);
                    throw th;
                }
            } catch (Exception e) {
                this.mIsCameraOpened = false;
                this.mCamera = null;
                e.printStackTrace();
                return -1;
            }
        }
        this.mIsCameraOpened = false;
        SurfaceTexture surfaceTexture = this.mSurfaceTexture;
        if (surfaceTexture != null) {
            surfaceTexture.release();
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
        StringBuilder sb = new StringBuilder();
        sb.append("degrees = ");
        sb.append(this.mDegrees);
        pw.println(sb.toString());
        pw.print(subPrefix);
        pw.println("mCurrentExposureValue = " + this.mCurrentExposureValue);
        pw.print(subPrefix);
        pw.println("mCurrentISOValue = " + this.mCurrentISOValue);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.replace(char, char):java.lang.String}
     arg types: [int, int]
     candidates:
      ClspMth{java.lang.String.replace(java.lang.CharSequence, java.lang.CharSequence):java.lang.String}
      ClspMth{java.lang.String.replace(char, char):java.lang.String} */
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
