package com.oppo.content;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Intent;

public class OppoIntent extends Intent {
    public static final String ACTION_CAMERA_MODE_CHANGE = "android.intent.action.CAMERA_MODE_CHANGE";
    public static final String ACTION_CLOSE_NOTIFICATION_DIALOG = "android.intent.action.CLOSE_NOTIFICATION_DIALOG";
    public static final String ACTION_DATA_COLLECT_CLEAR = "oppo.intent.action.DATA_COLLECT_CLEAR";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "add for mtk phone", property = OppoRomType.MTK)
    public static final String ACTION_DATA_DEFAULT_SIM_CHANGED = "android.intent.action.DATA_DEFAULT_SIM";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "add for mtk phone", property = OppoRomType.MTK)
    public static final String ACTION_DUAL_SIM_MODE_CHANGED = "android.intent.action.DUAL_SIM_MODE";
    public static final String ACTION_FILE_ENCRYPT_DECRYPT = "oppo.intent.action.decrypt";
    public static final String ACTION_FILE_ENCRYPT_ENCRYPT = "oppo.intent.action.encrypt";
    public static final String ACTION_FILE_ENCRYPT_STATE_CHANGED = "oppo.intent.action.encrypt.stateChanged";
    public static final String ACTION_GESTUREGUIDE_MODE_CHANGE = "oppo.intent.action.GESTUREGUIDE_MODE_CHANGE";
    public static final String ACTION_HOME_MODE_CHANGE = "android.intent.action.HOME_MODE_CHANGE";
    public static final String ACTION_LID_STATE_CHANGED = "com.oppo.intent.action.LID_STATE_CHANGED";
    public static final String ACTION_MEDIA_SCANNER_SCAN_ALL = "oppo.intent.action.MEDIA_SCAN_ALL";
    public static final String ACTION_MEDIA_SCANNER_SCAN_DIRECTORY = "oppo.intent.action.MEDIA_SCAN_DIRECTORY";
    public static final String ACTION_OPPO_OTA_UPDATE_FAILED = "oppo.intent.action.OPPO_OTA_UPDATE_FAILED";
    public static final String ACTION_OPPO_OTA_UPDATE_SUCCESSED = "oppo.intent.action.OPPO_OTA_UPDATE_SUCCESSED";
    public static final String ACTION_OPPO_PACKAGE_ADDED = "oppo.intent.action.OPPO_PACKAGE_ADDED";
    public static final String ACTION_OPPO_RECOVER_UPDATE_FAILED = "oppo.intent.action.OPPO_RECOVER_UPDATE_FAILED";
    public static final String ACTION_OPPO_RECOVER_UPDATE_SUCCESSED = "oppo.intent.action.OPPO_RECOVER_UPDATE_SUCCESSED";
    public static final String ACTION_PRE_MEDIA_SHARED = "android.intent.action.MEDIA_PRE_SHARED";
    public static final String ACTION_SAU_UPDATE_FAILED = "oppo.intent.action.OPPO_SAU_UPDATE_FAILED";
    public static final String ACTION_SAU_UPDATE_SUCCESSED = "oppo.intent.action.OPPO_SAU_UPDATE_SUCCESSED";
    public static final String ACTION_SCREEN_SHOT = "oppo.intent.action.SCREEN_SHOT";
    public static final String ACTION_SKIN_CHANGED = "oppo.intent.action.SKIN_CHANGED";
    public static final String ACTION_TRIGGER_PACKAGE = "android.intent.action.TRIGGER_PACKAGE";
    public static final String CATEGORY_MULTIAPP = "oppo.intent.category.MULTIAPP";
    public static final String EXTRA_DATA_BRIGHTNESS = "LightBreightness";
    public static final String EXTRA_DATA_ID = "LightID";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "add for mtk phone", property = OppoRomType.MTK)
    public static final String EXTRA_DUAL_SIM_MODE = "mode";
    public static final String EXTRA_LID_STATE = "lid_state";
    public static final String INTENT_CAMERA_OPEN_LIGHT = "com.oppo.camera.OpenLight";
    public static final int OPPO_FLAG_ACTIVITY_SECURE_POLICY = Integer.MIN_VALUE;
    public static final int OPPO_FLAG_MUTIL_APP = 1024;
    public static final int OPPO_FLAG_MUTIL_CHOOSER = 512;
}
