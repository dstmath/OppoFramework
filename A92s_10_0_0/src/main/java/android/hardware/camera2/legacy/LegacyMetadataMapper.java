package android.hardware.camera2.legacy;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.utils.ArrayUtils;
import android.hardware.camera2.utils.ListUtils;
import android.hardware.camera2.utils.ParamsUtils;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LegacyMetadataMapper {
    private static final long APPROXIMATE_CAPTURE_DELAY_MS = 200;
    private static final long APPROXIMATE_JPEG_ENCODE_TIME_MS = 600;
    private static final long APPROXIMATE_SENSOR_AREA_PX = 8388608;
    private static final boolean DEBUG = ParameterUtils.DEBUG;
    private static final boolean DEBUG_DUMP = ParameterUtils.DEBUG_DUMP;
    public static final int HAL_PIXEL_FORMAT_BGRA_8888 = 5;
    public static final int HAL_PIXEL_FORMAT_BLOB = 33;
    public static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    public static final int HAL_PIXEL_FORMAT_RGBA_8888 = 1;
    private static final float LENS_INFO_MINIMUM_FOCUS_DISTANCE_FIXED_FOCUS = 0.0f;
    static final boolean LIE_ABOUT_AE_MAX_REGIONS = false;
    static final boolean LIE_ABOUT_AE_STATE = false;
    static final boolean LIE_ABOUT_AF = false;
    static final boolean LIE_ABOUT_AF_MAX_REGIONS = false;
    static final boolean LIE_ABOUT_AWB = false;
    static final boolean LIE_ABOUT_AWB_STATE = false;
    private static final long NS_PER_MS = 1000000;
    private static final float PREVIEW_ASPECT_RATIO_TOLERANCE = 0.01f;
    private static final int REQUEST_MAX_NUM_INPUT_STREAMS_COUNT = 0;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC = 3;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL = 1;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW = 0;
    private static final int REQUEST_PIPELINE_MAX_DEPTH_HAL1 = 3;
    private static final int REQUEST_PIPELINE_MAX_DEPTH_OURS = 3;
    private static final String TAG = "LegacyMetadataMapper";
    static final int UNKNOWN_MODE = -1;
    private static final int[] sAllowedTemplates = {1, 2, 3};
    private static final int[] sEffectModes = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final String[] sLegacyEffectMode = {"none", Camera.Parameters.EFFECT_MONO, Camera.Parameters.EFFECT_NEGATIVE, Camera.Parameters.EFFECT_SOLARIZE, Camera.Parameters.EFFECT_SEPIA, Camera.Parameters.EFFECT_POSTERIZE, Camera.Parameters.EFFECT_WHITEBOARD, Camera.Parameters.EFFECT_BLACKBOARD, Camera.Parameters.EFFECT_AQUA};
    private static final String[] sLegacySceneModes = {"auto", "action", Camera.Parameters.SCENE_MODE_PORTRAIT, Camera.Parameters.SCENE_MODE_LANDSCAPE, Camera.Parameters.SCENE_MODE_NIGHT, Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT, Camera.Parameters.SCENE_MODE_THEATRE, Camera.Parameters.SCENE_MODE_BEACH, Camera.Parameters.SCENE_MODE_SNOW, Camera.Parameters.SCENE_MODE_SUNSET, Camera.Parameters.SCENE_MODE_STEADYPHOTO, Camera.Parameters.SCENE_MODE_FIREWORKS, Camera.Parameters.SCENE_MODE_SPORTS, Camera.Parameters.SCENE_MODE_PARTY, Camera.Parameters.SCENE_MODE_CANDLELIGHT, Camera.Parameters.SCENE_MODE_BARCODE, Camera.Parameters.SCENE_MODE_HDR};
    private static final int[] sSceneModes = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18};

    public static CameraCharacteristics createCharacteristics(Camera.Parameters parameters, Camera.CameraInfo info, int cameraId, Size displaySize) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        String paramStr = parameters.flatten();
        CameraInfo outerInfo = new CameraInfo();
        outerInfo.info = info;
        return createCharacteristics(paramStr, outerInfo, cameraId, displaySize);
    }

    public static CameraCharacteristics createCharacteristics(String parameters, CameraInfo info, int cameraId, Size displaySize) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        Preconditions.checkNotNull(info.info, "info.info must not be null");
        CameraMetadataNative m = new CameraMetadataNative();
        mapCharacteristicsFromInfo(m, info.info);
        Camera.Parameters params = Camera.getEmptyParameters();
        params.unflatten(parameters);
        mapCharacteristicsFromParameters(m, params);
        if (DEBUG_DUMP) {
            Log.v(TAG, "createCharacteristics metadata:");
            Log.v(TAG, "--------------------------------------------------- (start)");
            m.dumpToLog();
            Log.v(TAG, "--------------------------------------------------- (end)");
        }
        m.setCameraId(cameraId);
        m.setDisplaySize(displaySize);
        return new CameraCharacteristics(m);
    }

    private static void mapCharacteristicsFromInfo(CameraMetadataNative m, Camera.CameraInfo i) {
        m.set(CameraCharacteristics.LENS_FACING, Integer.valueOf(i.facing == 0 ? 1 : 0));
        m.set(CameraCharacteristics.SENSOR_ORIENTATION, Integer.valueOf(i.orientation));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    private static void mapCharacteristicsFromParameters(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, new int[]{1, 2});
        mapControlAe(m, p);
        mapControlAf(m, p);
        mapControlAwb(m, p);
        mapControlOther(m, p);
        mapLens(m, p);
        mapFlash(m, p);
        mapJpeg(m, p);
        m.set(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, new int[]{1, 2});
        mapScaler(m, p);
        mapSensor(m, p);
        mapStatistics(m, p);
        mapSync(m, p);
        m.set((CameraCharacteristics.Key) CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, (Object) 2);
        mapScalerStreamConfigs(m, p);
        mapRequest(m, p);
    }

    private static void mapScalerStreamConfigs(CameraMetadataNative m, Camera.Parameters p) {
        ArrayList<StreamConfiguration> availableStreamConfigs = new ArrayList<>();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        List<Camera.Size> jpegSizes = p.getSupportedPictureSizes();
        SizeAreaComparator areaComparator = new SizeAreaComparator();
        Collections.sort(previewSizes, areaComparator);
        Camera.Size maxJpegSize = SizeAreaComparator.findLargestByArea(jpegSizes);
        float f = 1.0f;
        float jpegAspectRatio = (((float) maxJpegSize.width) * 1.0f) / ((float) maxJpegSize.height);
        int i = 3;
        if (DEBUG) {
            Log.v(TAG, String.format("mapScalerStreamConfigs - largest JPEG area %dx%d, AR=%f", Integer.valueOf(maxJpegSize.width), Integer.valueOf(maxJpegSize.height), Float.valueOf(jpegAspectRatio)));
        }
        while (!previewSizes.isEmpty()) {
            int index = previewSizes.size() - 1;
            Camera.Size size = previewSizes.get(index);
            float previewAspectRatio = (((float) size.width) * f) / ((float) size.height);
            if (Math.abs(jpegAspectRatio - previewAspectRatio) < PREVIEW_ASPECT_RATIO_TOLERANCE) {
                break;
            }
            previewSizes.remove(index);
            if (DEBUG) {
                Object[] objArr = new Object[i];
                objArr[0] = Integer.valueOf(size.width);
                objArr[1] = Integer.valueOf(size.height);
                objArr[2] = Float.valueOf(previewAspectRatio);
                Log.v(TAG, String.format("mapScalerStreamConfigs - removed preview size %dx%d, AR=%f was not the same", objArr));
            }
            f = 1.0f;
            i = 3;
        }
        if (previewSizes.isEmpty()) {
            Log.w(TAG, "mapScalerStreamConfigs - failed to find any preview size matching JPEG aspect ratio " + jpegAspectRatio);
            previewSizes = p.getSupportedPreviewSizes();
        }
        Collections.sort(previewSizes, Collections.reverseOrder(areaComparator));
        appendStreamConfig(availableStreamConfigs, 34, previewSizes);
        appendStreamConfig(availableStreamConfigs, 35, previewSizes);
        for (Integer num : p.getSupportedPreviewFormats()) {
            int format = num.intValue();
            if (ImageFormat.isPublicFormat(format) && format != 17) {
                appendStreamConfig(availableStreamConfigs, format, previewSizes);
            } else if (DEBUG) {
                Log.v(TAG, String.format("mapStreamConfigs - Skipping format %x", Integer.valueOf(format)));
            }
        }
        appendStreamConfig(availableStreamConfigs, 33, p.getSupportedPictureSizes());
        m.set(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS, (StreamConfiguration[]) availableStreamConfigs.toArray(new StreamConfiguration[0]));
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS, new StreamConfigurationDuration[0]);
        StreamConfigurationDuration[] jpegStalls = new StreamConfigurationDuration[jpegSizes.size()];
        int i2 = 0;
        long longestStallDuration = -1;
        for (Camera.Size s : jpegSizes) {
            long stallDuration = calculateJpegStallDuration(s);
            int i3 = i2 + 1;
            jpegStalls[i2] = new StreamConfigurationDuration(33, s.width, s.height, stallDuration);
            if (longestStallDuration < stallDuration) {
                longestStallDuration = stallDuration;
            }
            i2 = i3;
        }
        m.set(CameraCharacteristics.SCALER_AVAILABLE_STALL_DURATIONS, jpegStalls);
        m.set(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION, Long.valueOf(longestStallDuration));
    }

    private static void mapControlAe(CameraMetadataNative m, Camera.Parameters p) {
        List<String> antiBandingModes = p.getSupportedAntibanding();
        char c = 0;
        if (antiBandingModes == null || antiBandingModes.size() <= 0) {
            m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, new int[0]);
        } else {
            int[] modes = new int[antiBandingModes.size()];
            int j = 0;
            Iterator<String> it = antiBandingModes.iterator();
            while (it.hasNext()) {
                String mode = it.next();
                int convertedMode = convertAntiBandingMode(mode);
                if (!DEBUG || convertedMode != -1) {
                    modes[j] = convertedMode;
                    j++;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Antibanding mode ");
                    sb.append(mode == null ? WifiEnterpriseConfig.EMPTY_VALUE : mode);
                    sb.append(" not supported, skipping...");
                    Log.v(TAG, sb.toString());
                }
            }
            m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, Arrays.copyOf(modes, j));
        }
        List<int[]> fpsRanges = p.getSupportedPreviewFpsRange();
        if (fpsRanges != null) {
            int rangesSize = fpsRanges.size();
            if (rangesSize > 0) {
                Range<Integer>[] ranges = new Range[rangesSize];
                int i = 0;
                for (int[] r : fpsRanges) {
                    ranges[i] = Range.create(Integer.valueOf((int) Math.floor(((double) r[c]) / 1000.0d)), Integer.valueOf((int) Math.ceil(((double) r[1]) / 1000.0d)));
                    i++;
                    fpsRanges = fpsRanges;
                    c = 0;
                }
                m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, ranges);
                int[] aeAvail = ArrayUtils.convertStringListToIntArray(p.getSupportedFlashModes(), new String[]{"off", "auto", Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_RED_EYE, Camera.Parameters.FLASH_MODE_TORCH}, new int[]{1, 2, 3, 4});
                if (aeAvail == null || aeAvail.length == 0) {
                    aeAvail = new int[]{1};
                }
                m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, aeAvail);
                m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, Range.create(Integer.valueOf(p.getMinExposureCompensation()), Integer.valueOf(p.getMaxExposureCompensation())));
                m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, ParamsUtils.createRational(p.getExposureCompensationStep()));
                m.set(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoExposureLockSupported()));
                return;
            }
            throw new AssertionError("At least one FPS range must be supported.");
        }
        throw new AssertionError("Supported FPS ranges cannot be null.");
    }

    private static void mapControlAf(CameraMetadataNative m, Camera.Parameters p) {
        List<Integer> afAvail = ArrayUtils.convertStringListToIntList(p.getSupportedFocusModes(), new String[]{"auto", Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, Camera.Parameters.FOCUS_MODE_EDOF, Camera.Parameters.FOCUS_MODE_INFINITY, Camera.Parameters.FOCUS_MODE_MACRO, Camera.Parameters.FOCUS_MODE_FIXED}, new int[]{1, 4, 3, 5, 0, 2, 0});
        if (afAvail == null || afAvail.size() == 0) {
            Log.w(TAG, "No AF modes supported (HAL bug); defaulting to AF_MODE_OFF only");
            afAvail = new ArrayList(1);
            afAvail.add(0);
        }
        m.set(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, ArrayUtils.toIntArray(afAvail));
        if (DEBUG) {
            Log.v(TAG, "mapControlAf - control.afAvailableModes set to " + ListUtils.listToString(afAvail));
        }
    }

    private static void mapControlAwb(CameraMetadataNative m, Camera.Parameters p) {
        List<Integer> awbAvail = ArrayUtils.convertStringListToIntList(p.getSupportedWhiteBalance(), new String[]{"auto", Camera.Parameters.WHITE_BALANCE_INCANDESCENT, Camera.Parameters.WHITE_BALANCE_FLUORESCENT, Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT, Camera.Parameters.WHITE_BALANCE_DAYLIGHT, Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT, Camera.Parameters.WHITE_BALANCE_TWILIGHT, Camera.Parameters.WHITE_BALANCE_SHADE}, new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        if (awbAvail == null || awbAvail.size() == 0) {
            Log.w(TAG, "No AWB modes supported (HAL bug); defaulting to AWB_MODE_AUTO only");
            awbAvail = new ArrayList(1);
            awbAvail.add(1);
        }
        m.set(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, ArrayUtils.toIntArray(awbAvail));
        if (DEBUG) {
            Log.v(TAG, "mapControlAwb - control.awbAvailableModes set to " + ListUtils.listToString(awbAvail));
        }
        m.set(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoWhiteBalanceLockSupported()));
    }

    private static void mapControlOther(CameraMetadataNative m, Camera.Parameters p) {
        int[] supportedEffectModes;
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, p.isVideoStabilizationSupported() ? new int[]{0, 1} : new int[]{0});
        m.set(CameraCharacteristics.CONTROL_MAX_REGIONS, new int[]{p.getMaxNumMeteringAreas(), 0, p.getMaxNumFocusAreas()});
        List<String> effectModes = p.getSupportedColorEffects();
        if (effectModes == null) {
            supportedEffectModes = new int[0];
        } else {
            supportedEffectModes = ArrayUtils.convertStringListToIntArray(effectModes, sLegacyEffectMode, sEffectModes);
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, supportedEffectModes);
        int maxNumDetectedFaces = p.getMaxNumDetectedFaces();
        List<String> sceneModes = p.getSupportedSceneModes();
        List<Integer> supportedSceneModes = ArrayUtils.convertStringListToIntList(sceneModes, sLegacySceneModes, sSceneModes);
        if (sceneModes != null && sceneModes.size() == 1 && sceneModes.get(0).equals("auto")) {
            supportedSceneModes = null;
        }
        boolean sceneModeSupported = true;
        if (supportedSceneModes == null && maxNumDetectedFaces == 0) {
            sceneModeSupported = false;
        }
        if (sceneModeSupported) {
            if (supportedSceneModes == null) {
                supportedSceneModes = new ArrayList<>();
            }
            if (maxNumDetectedFaces > 0) {
                supportedSceneModes.add(1);
            }
            if (supportedSceneModes.contains(0)) {
                do {
                } while (supportedSceneModes.remove(new Integer(0)));
            }
            m.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, ArrayUtils.toIntArray(supportedSceneModes));
        } else {
            m.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, new int[]{0});
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_MODES, sceneModeSupported ? new int[]{1, 2} : new int[]{1});
    }

    private static void mapLens(CameraMetadataNative m, Camera.Parameters p) {
        if (DEBUG) {
            Log.v(TAG, "mapLens - focus-mode='" + p.getFocusMode() + "'");
        }
        if (Camera.Parameters.FOCUS_MODE_FIXED.equals(p.getFocusMode())) {
            m.set(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE, Float.valueOf(0.0f));
            if (DEBUG) {
                Log.v(TAG, "mapLens - lens.info.minimumFocusDistance = 0");
            }
        } else if (DEBUG) {
            Log.v(TAG, "mapLens - lens.info.minimumFocusDistance is unknown");
        }
        m.set(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, new float[]{p.getFocalLength()});
    }

    private static void mapFlash(CameraMetadataNative m, Camera.Parameters p) {
        boolean flashAvailable = false;
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        if (supportedFlashModes != null) {
            flashAvailable = !ListUtils.listElementsEqualTo(supportedFlashModes, "off");
        }
        m.set(CameraCharacteristics.FLASH_INFO_AVAILABLE, Boolean.valueOf(flashAvailable));
    }

    private static void mapJpeg(CameraMetadataNative m, Camera.Parameters p) {
        List<Camera.Size> thumbnailSizes = p.getSupportedJpegThumbnailSizes();
        if (thumbnailSizes != null) {
            Size[] sizes = ParameterUtils.convertSizeListToArray(thumbnailSizes);
            Arrays.sort(sizes, new SizeAreaComparator());
            m.set(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, sizes);
        }
    }

    /* JADX INFO: Multiple debug info for r4v7 int[]: [D('defaultAvailableKeys' android.hardware.camera2.CaptureResult$Key<?>[]), D('outputStreams' int[])] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Byte>, byte]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    private static void mapRequest(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, new int[]{0});
        List<CameraCharacteristics.Key<?>> characteristicsKeys = new ArrayList<>(Arrays.asList(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, CameraCharacteristics.CONTROL_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_MAX_REGIONS, CameraCharacteristics.FLASH_INFO_AVAILABLE, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, CameraCharacteristics.SCALER_CROPPING_TYPE, CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, CameraCharacteristics.SENSOR_ORIENTATION, CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, CameraCharacteristics.SYNC_MAX_LATENCY));
        if (m.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) != null) {
            characteristicsKeys.add(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        }
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CHARACTERISTICS_KEYS, getTagsForKeys((CameraCharacteristics.Key[]) characteristicsKeys.toArray(new CameraCharacteristics.Key[0])));
        ArrayList<CaptureRequest.Key<?>> availableKeys = new ArrayList<>(Arrays.asList(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureRequest.CONTROL_AE_LOCK, CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AWB_LOCK, CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.FLASH_MODE, CaptureRequest.JPEG_GPS_COORDINATES, CaptureRequest.JPEG_GPS_PROCESSING_METHOD, CaptureRequest.JPEG_GPS_TIMESTAMP, CaptureRequest.JPEG_ORIENTATION, CaptureRequest.JPEG_QUALITY, CaptureRequest.JPEG_THUMBNAIL_QUALITY, CaptureRequest.JPEG_THUMBNAIL_SIZE, CaptureRequest.LENS_FOCAL_LENGTH, CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.SCALER_CROP_REGION, CaptureRequest.STATISTICS_FACE_DETECT_MODE));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AF_REGIONS);
        }
        CaptureRequest.Key<?>[] availableRequestKeys = new CaptureRequest.Key[availableKeys.size()];
        availableKeys.toArray(availableRequestKeys);
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_REQUEST_KEYS, getTagsForKeys(availableRequestKeys));
        List<CaptureResult.Key<?>> availableKeys2 = new ArrayList<>(Arrays.asList(CaptureResult.COLOR_CORRECTION_ABERRATION_MODE, CaptureResult.CONTROL_AE_ANTIBANDING_MODE, CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureResult.CONTROL_AE_LOCK, CaptureResult.CONTROL_AE_MODE, CaptureResult.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_STATE, CaptureResult.CONTROL_AWB_MODE, CaptureResult.CONTROL_AWB_LOCK, CaptureResult.CONTROL_MODE, CaptureResult.FLASH_MODE, CaptureResult.JPEG_GPS_COORDINATES, CaptureResult.JPEG_GPS_PROCESSING_METHOD, CaptureResult.JPEG_GPS_TIMESTAMP, CaptureResult.JPEG_ORIENTATION, CaptureResult.JPEG_QUALITY, CaptureResult.JPEG_THUMBNAIL_QUALITY, CaptureResult.LENS_FOCAL_LENGTH, CaptureResult.NOISE_REDUCTION_MODE, CaptureResult.REQUEST_PIPELINE_DEPTH, CaptureResult.SCALER_CROP_REGION, CaptureResult.SENSOR_TIMESTAMP, CaptureResult.STATISTICS_FACE_DETECT_MODE));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AF_REGIONS);
        }
        CaptureResult.Key<?>[] availableResultKeys = new CaptureResult.Key[availableKeys2.size()];
        availableKeys2.toArray(availableResultKeys);
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_RESULT_KEYS, getTagsForKeys(availableResultKeys));
        m.set(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, new int[]{0, 3, 1});
        m.set((CameraCharacteristics.Key) CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS, (Object) 0);
        m.set((CameraCharacteristics.Key) CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, (Object) 1);
        m.set((CameraCharacteristics.Key) CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, (Object) (byte) 6);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    private static void mapScaler(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, Float.valueOf(ParameterUtils.getMaxZoomRatio(p)));
        m.set((CameraCharacteristics.Key) CameraCharacteristics.SCALER_CROPPING_TYPE, (Object) 0);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    private static void mapSensor(CameraMetadataNative m, Camera.Parameters p) {
        Size largestJpegSize = ParameterUtils.getLargestSupportedJpegSizeByArea(p);
        Rect activeArrayRect = ParamsUtils.createRect(largestJpegSize);
        m.set(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, activeArrayRect);
        m.set(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE, activeArrayRect);
        m.set(CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, new int[]{0});
        m.set(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, largestJpegSize);
        float focalLength = p.getFocalLength();
        m.set(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, new SizeF((float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getHorizontalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d)), (float) Math.abs(((double) (focalLength * 2.0f)) * Math.tan(((((double) p.getVerticalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d))));
        m.set((CameraCharacteristics.Key) CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, (Object) 0);
    }

    private static void mapStatistics(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, p.getMaxNumDetectedFaces() > 0 ? new int[]{0, 1} : new int[]{0});
        m.set(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, Integer.valueOf(p.getMaxNumDetectedFaces()));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CameraCharacteristics$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void */
    private static void mapSync(CameraMetadataNative m, Camera.Parameters p) {
        m.set((CameraCharacteristics.Key) CameraCharacteristics.SYNC_MAX_LATENCY, (Object) -1);
    }

    private static void appendStreamConfig(ArrayList<StreamConfiguration> configs, int format, List<Camera.Size> sizes) {
        for (Camera.Size size : sizes) {
            configs.add(new StreamConfiguration(format, size.width, size.height, false));
        }
    }

    static int convertSceneModeFromLegacy(String mode) {
        if (mode == null) {
            return 0;
        }
        int index = ArrayUtils.getArrayIndex(sLegacySceneModes, mode);
        if (index < 0) {
            return -1;
        }
        return sSceneModes[index];
    }

    static String convertSceneModeToLegacy(int mode) {
        if (mode == 1) {
            return "auto";
        }
        int index = ArrayUtils.getArrayIndex(sSceneModes, mode);
        if (index < 0) {
            return null;
        }
        return sLegacySceneModes[index];
    }

    static int convertEffectModeFromLegacy(String mode) {
        if (mode == null) {
            return 0;
        }
        int index = ArrayUtils.getArrayIndex(sLegacyEffectMode, mode);
        if (index < 0) {
            return -1;
        }
        return sEffectModes[index];
    }

    static String convertEffectModeToLegacy(int mode) {
        int index = ArrayUtils.getArrayIndex(sEffectModes, mode);
        if (index < 0) {
            return null;
        }
        return sLegacyEffectMode[index];
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int convertAntiBandingMode(String mode) {
        boolean z;
        if (mode == null) {
            return -1;
        }
        switch (mode.hashCode()) {
            case 109935:
                if (mode.equals("off")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 1628397:
                if (mode.equals(Camera.Parameters.ANTIBANDING_50HZ)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1658188:
                if (mode.equals(Camera.Parameters.ANTIBANDING_60HZ)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 3005871:
                if (mode.equals("auto")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 0;
        }
        if (z) {
            return 1;
        }
        if (z) {
            return 2;
        }
        if (z) {
            return 3;
        }
        Log.w(TAG, "convertAntiBandingMode - Unknown antibanding mode " + mode);
        return -1;
    }

    static int convertAntiBandingModeOrDefault(String mode) {
        int antiBandingMode = convertAntiBandingMode(mode);
        if (antiBandingMode == -1) {
            return 0;
        }
        return antiBandingMode;
    }

    private static int[] convertAeFpsRangeToLegacy(Range<Integer> fpsRange) {
        return new int[]{fpsRange.getLower().intValue(), fpsRange.getUpper().intValue()};
    }

    private static long calculateJpegStallDuration(Camera.Size size) {
        return (((long) size.width) * ((long) size.height) * 71) + 200000000;
    }

    public static void convertRequestMetadata(LegacyRequest request) {
        LegacyRequestMapper.convertRequestMetadata(request);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CaptureRequest$Key<java.lang.Integer>, int]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void
     arg types: [android.hardware.camera2.CaptureRequest$Key<java.lang.Boolean>, boolean]
     candidates:
      MutableMD:<T>:(android.hardware.camera2.CameraCharacteristics$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureResult$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.impl.CameraMetadataNative$Key, java.lang.Object):void
      MutableMD:<T>:(android.hardware.camera2.CaptureRequest$Key, java.lang.Object):void */
    public static CameraMetadataNative createRequestTemplate(CameraCharacteristics c, int templateId) {
        int captureIntent;
        int afMode;
        if (ArrayUtils.contains(sAllowedTemplates, templateId)) {
            CameraMetadataNative m = new CameraMetadataNative();
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AWB_MODE, (Object) 1);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, (Object) 3);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (Object) 0);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AE_LOCK, (Object) false);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, (Object) 0);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AF_TRIGGER, (Object) 0);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AWB_MODE, (Object) 1);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AWB_LOCK, (Object) false);
            Rect activeArray = (Rect) c.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            MeteringRectangle[] activeRegions = {new MeteringRectangle(0, 0, activeArray.width() - 1, activeArray.height() - 1, 0)};
            m.set(CaptureRequest.CONTROL_AE_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AWB_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AF_REGIONS, activeRegions);
            if (templateId == 1) {
                captureIntent = 1;
            } else if (templateId == 2) {
                captureIntent = 2;
            } else if (templateId == 3) {
                captureIntent = 3;
            } else {
                throw new AssertionError("Impossible; keep in sync with sAllowedTemplates");
            }
            m.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(captureIntent));
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_AE_MODE, (Object) 1);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_MODE, (Object) 1);
            Float minimumFocusDistance = (Float) c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (minimumFocusDistance == null || minimumFocusDistance.floatValue() != 0.0f) {
                if (templateId == 3 || templateId == 4) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 3)) {
                        afMode = 3;
                    }
                } else if ((templateId == 1 || templateId == 2) && ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 4)) {
                    afMode = 4;
                }
                afMode = 1;
            } else {
                afMode = 0;
            }
            if (DEBUG) {
                Log.v(TAG, "createRequestTemplate (templateId=" + templateId + "), afMode=" + afMode + ", minimumFocusDistance=" + minimumFocusDistance);
            }
            m.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(afMode));
            Range<Integer>[] availableFpsRange = (Range[]) c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> bestRange = availableFpsRange[0];
            for (Range<Integer> r : availableFpsRange) {
                if (bestRange.getUpper().intValue() < r.getUpper().intValue()) {
                    bestRange = r;
                } else if (bestRange.getUpper() == r.getUpper() && bestRange.getLower().intValue() < r.getLower().intValue()) {
                    bestRange = r;
                }
            }
            m.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, bestRange);
            m.set((CaptureRequest.Key) CaptureRequest.CONTROL_SCENE_MODE, (Object) 0);
            m.set((CaptureRequest.Key) CaptureRequest.STATISTICS_FACE_DETECT_MODE, (Object) 0);
            m.set((CaptureRequest.Key) CaptureRequest.FLASH_MODE, (Object) 0);
            if (templateId == 2) {
                m.set((CaptureRequest.Key) CaptureRequest.NOISE_REDUCTION_MODE, (Object) 2);
            } else {
                m.set((CaptureRequest.Key) CaptureRequest.NOISE_REDUCTION_MODE, (Object) 1);
            }
            if (templateId == 2) {
                m.set((CaptureRequest.Key) CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, (Object) 2);
            } else {
                m.set((CaptureRequest.Key) CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, (Object) 1);
            }
            m.set(CaptureRequest.LENS_FOCAL_LENGTH, Float.valueOf(((float[]) c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))[0]));
            Size[] sizes = (Size[]) c.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
            m.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, sizes.length > 1 ? sizes[1] : sizes[0]);
            return m;
        }
        throw new IllegalArgumentException("templateId out of range");
    }

    private static int[] getTagsForKeys(CameraCharacteristics.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureRequest.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureResult.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    static String convertAfModeToLegacy(int mode, List<String> supportedFocusModes) {
        if (supportedFocusModes == null || supportedFocusModes.isEmpty()) {
            Log.w(TAG, "No focus modes supported; API1 bug");
            return null;
        }
        String param = null;
        if (mode != 0) {
            if (mode == 1) {
                param = "auto";
            } else if (mode == 2) {
                param = Camera.Parameters.FOCUS_MODE_MACRO;
            } else if (mode == 3) {
                param = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
            } else if (mode == 4) {
                param = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
            } else if (mode == 5) {
                param = Camera.Parameters.FOCUS_MODE_EDOF;
            }
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            param = Camera.Parameters.FOCUS_MODE_FIXED;
        } else {
            param = Camera.Parameters.FOCUS_MODE_INFINITY;
        }
        if (supportedFocusModes.contains(param)) {
            return param;
        }
        String defaultMode = supportedFocusModes.get(0);
        Log.w(TAG, String.format("convertAfModeToLegacy - ignoring unsupported mode %d, defaulting to %s", Integer.valueOf(mode), defaultMode));
        return defaultMode;
    }
}
