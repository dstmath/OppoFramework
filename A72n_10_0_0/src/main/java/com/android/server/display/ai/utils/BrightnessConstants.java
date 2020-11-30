package com.android.server.display.ai.utils;

import android.os.SystemProperties;

public class BrightnessConstants {
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_SPLINES_TRAINED = "spline_trained";
    public static final String DEFAULT_LAUNCHER_PACAKGE_NAME = "com.oppo.launcher";
    public static final String DEFAULT_SPLINE = "default";
    public static final String DETAILS_LUX = "ai_brightness_details";
    public static final int DETAILS_LUX_DEF = 0;
    public static final String EXTRA_CENTRAL_POINTS = "extra_central_points";
    public static final String INTERVAL_PATH = "/data/oppo/coloros/proton/aibrightness.csv";
    public static final float MAX_BRIGHTNESS_ADJUST_RATE = 1.4f;
    public static final int MSG_ON_RUS_CHANGE = 3;
    public static final int MSG_ON_SCREEN_OFF = 4;
    public static final int MSG_ON_TARGET_BRIGHTNESS_CHANGE = 1;
    public static final int MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY = 2;
    public static final String PERMISSION_OPPO_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String RUS_KEY_BRIGHTNESS_LIST = "sys_proton_brightness_list";
    public static final String SEPARATE_SOFT_CONFIG = SystemProperties.get("ro.separate.soft", "oppo");
    public static final String SETTINGS_AIBRIGHTNESS_XS = "settings.aibrightness.xs";
    public static final String SETTINGS_AIBRIGHTNESS_YS = "settings.aibrightness.ys";
    public static final String SETTINGS_NEW_POINTS = "settings_new_points";
    public static boolean sDebugDetailsLux;

    public static class AppSplineXml {
        public static final String FILE_NAME = "spline.xml";
        public static final String PATH = "/data/oppo/coloros/deepthinker/brightness/spline/";
        public static final String TAG_APP = "app";
        public static final String TAG_NAME = "name";
        public static final String TAG_PACKAGE = "package";
        public static final String TAG_POINT = "point";
        public static final String TAG_TRAIN_TIME = "time";
        public static final String TAG_VERIFIED = "verified";
    }

    public static class BrightnessTrainSwitch {
        public static final int DEFAULT_TRAIN_SWITCH = 1;
        public static final String SETTINGS_AIBRIGHTNESS_TRAIN_ENABLE = "settings.aibrightness.train.enable";
        public static final int TRAIN_DISABLE = 0;
        public static final int TRAIN_ENABLE = 1;
        public static final boolean TRAIN_SWITCH = true;
    }

    public static class DefaultConfig {
        public static final float[] XS = {0.0f, 1.0f, 2.0f, 3.0f, 10.0f, 20.0f, 36.0f, 100.0f, 300.0f, 1000.0f, 2250.0f, 4600.0f, 5900.0f, 8600.0f};
    }

    public static class Statistics {
        public static final String EVENT_ID_VERIFY = "ai_brightness_verify";
        public static final String KEY_VERIFY_DRAG_MONOTONE = "verify_drag_monotone";
        public static final String KEY_VERIFY_DRAG_MONOTONE_RESOLVE = "verify_drag_monotone_resolve";
        public static final String KEY_VERIFY_POINT = "verify_point";
        public static final String LOG_TAG = "ai_brightness";
    }
}
