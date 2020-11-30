package com.mediatek.camcorder;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CamcorderProfileEx {
    public static final int QUALITY_FINE = 111;
    public static final int QUALITY_FINE_4K2K = 123;
    public static final int QUALITY_H264_HIGH = 117;
    public static final int QUALITY_HIGH = 110;
    public static final int QUALITY_LIST_END = 123;
    private static final int QUALITY_LIST_START = getQualityNum("QUALITY_LIST_START");
    public static final int QUALITY_LIVE_EFFECT = 116;
    public static final int QUALITY_LOW = 108;
    public static final int QUALITY_MEDIUM = 109;
    public static final int QUALITY_NIGHT_FINE = 115;
    public static final int QUALITY_NIGHT_HIGH = 114;
    public static final int QUALITY_NIGHT_LOW = 112;
    public static final int QUALITY_NIGHT_MEDIUM = 113;
    public static final int QUALITY_TIME_LAPSE_LIST_END = (QUALITY_TIME_LAPSE_LIST_START + 123);
    public static final int QUALITY_TIME_LAPSE_LIST_START = getQualityNum("QUALITY_TIME_LAPSE_LIST_START");
    public static final int SLOW_MOTION_FHD_120FPS = 2251;
    public static final int SLOW_MOTION_FHD_60FPS = 2250;
    public static final int SLOW_MOTION_HD_120FPS = 2241;
    public static final int SLOW_MOTION_HD_180FPS = 2242;
    public static final int SLOW_MOTION_HD_60FPS = 2240;
    private static final int SLOW_MOTION_LIST_END = 2251;
    private static final int SLOW_MOTION_LIST_START = 2231;
    public static final int SLOW_MOTION_VGA_120FPS = 2231;
    private static final String TAG = "CamcorderProfileEx";

    private static int getQualityNum(String qualityName) {
        try {
            Field f = CamcorderProfile.class.getDeclaredField(qualityName);
            f.setAccessible(true);
            return f.getInt(null);
        } catch (SecurityException e) {
            Log.e(TAG, "getQualityNum error");
            return 0;
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getQualityNum error");
            return 0;
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "getQualityNum error");
            return 0;
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "getQualityNum error");
            return 0;
        }
    }

    public static CamcorderProfile getProfile(int quality) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return getProfile(i, quality);
            }
        }
        return null;
    }

    public static CamcorderProfile getProfile(int cameraId, int quality) {
        if ((quality >= QUALITY_LIST_START && quality <= 123) || ((quality >= QUALITY_TIME_LAPSE_LIST_START && quality <= QUALITY_TIME_LAPSE_LIST_END) || (quality >= 2231 && quality <= 2251))) {
            return native_get_camcorder_profile(cameraId, quality);
        }
        throw new IllegalArgumentException("Unsupported quality level: " + quality);
    }

    private static final CamcorderProfile native_get_camcorder_profile(int cameraId, int quality) {
        try {
            Method m = CamcorderProfile.class.getDeclaredMethod("native_get_camcorder_profile", Integer.TYPE, Integer.TYPE);
            m.setAccessible(true);
            return (CamcorderProfile) m.invoke(null, Integer.valueOf(cameraId), Integer.valueOf(quality));
        } catch (SecurityException e) {
            Log.e(TAG, "native_get_camcorder_profile error");
            return null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "native_get_camcorder_profile error");
            return null;
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "native_get_camcorder_profile error");
            return null;
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "native_get_camcorder_profile error");
            return null;
        } catch (InvocationTargetException e5) {
            Log.e(TAG, "native_get_camcorder_profile error");
            return null;
        }
    }
}
