package com.android.server.display.ai.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileUtils;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansRestriction;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.bean.SplineModel;
import com.android.server.display.ai.broadcastreceiver.AIBrightnessTrainSwitch;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.notification.OppoNotificationManager;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ModelConfigUtil {
    private static final String ATTR_BRIGHTNESS_TRAIN_SWITCH = "brightness_train_switch";
    private static final String ATTR_DARK_LUX_THRESHOLD = "dark_lux_threshold";
    private static final String ATTR_DAY_HOURS = "day_hours";
    private static final String ATTR_DEFAULT_BRIGHTNESS = "default_brightness";
    private static final String ATTR_DELTA_DOWN_SCALE = "delta_down_scale";
    private static final String ATTR_DELTA_UP_SCALE = "delta_up_scale";
    private static final String ATTR_DEVICE = "device";
    private static final String ATTR_DRAG_EXPAND_MULTIPLE_LEFT = "drag_expand_multiple_left";
    private static final String ATTR_DRAG_EXPAND_MULTIPLE_RIGHT = "drag_expand_multiple_right";
    private static final String ATTR_FRAME_DURATION = "frame_duration";
    private static final String ATTR_HBM_MAX_XS = "hbm_x_max_list";
    private static final String ATTR_HBM_MIN_XS = "hbm_x_min_list";
    private static final String ATTR_HBM_XS = "hbm_x_list";
    private static final String ATTR_HBM_YS = "hbm_y_list";
    private static final String ATTR_LEFT_SCALE_IN_DARK_ENV = "left_scale_in_dark_env";
    private static final String ATTR_MAIN_SWITCH = "main_switch";
    private static final String ATTR_MAX_BRIGHTNESS_CHANGE = "max_brightness_change";
    private static final String ATTR_MIN_BRIGHTNESS_CHANGE = "min_brightness_change";
    private static final String ATTR_MIN_LIGHT_IN_DNM = "min_light_in_dnm";
    private static final String ATTR_NIGHT_HOURS = "night_hours";
    private static final String ATTR_NORMAL_MAX_BRIGHTNESS = "normal_max_brightness";
    private static final String ATTR_REVISE_X = "revise_x";
    private static final String ATTR_REVISE_X_CHANGE_POINT = "revise_x_change_point";
    private static final String ATTR_REVISE_X_MULTIPLE = "revise_x_multiple";
    private static final String ATTR_RIGHT_SCALE_IN_DARK_ENV = "right_scale_in_dark_env";
    private static final String ATTR_SPEED_MULTIPLE_IN_DISTANCE = "speed_multiple_in_distance";
    private static final String ATTR_SPLINE_POINT_SIZE = "spline_point_size";
    private static final String ATTR_STABLE_DOWN_MIN_PERCENT = "stable_down_min_percent";
    private static final String ATTR_STABLE_DOWN_SELF_PERCENT = "stable_down_self_percent";
    private static final String ATTR_STABLE_RIGHT_MIN_LUX = "stable_right_min_lux";
    private static final String ATTR_STABLE_SMALL_CHANGE_PERCENT = "stable_small_change_percent";
    private static final String ATTR_STABLE_SMALL_CHANGE_SCALE_IN_DARK_ENV = "stable_small_change_scale_in_dark_env";
    private static final String ATTR_STABLE_SMALL_CHANGE_TARGET_PERCENT = "stable_small_change_target_percent";
    private static final String ATTR_STABLE_SMALL_RIGHT_MIN_LUX = "stable_small_right_min_lux";
    private static final String ATTR_STABLE_UP_MIN_PERCENT = "stable_up_min_percent";
    private static final String ATTR_STABLE_UP_SELF_PERCENT = "stable_up_self_percent";
    private static final String ATTR_TRIGGER_TURN_ON_AUTO_BRIGHTNESS = "trigger_turn_on_auto_brightness";
    private static final int AUTO_BRIGHTNESS_MODE_OFF = 0;
    private static final int AUTO_BRIGHTNESS_MODE_ON = 1;
    private static final String COLUMN_NAME_VERSION = "version";
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String LOCAL_FILE_PATH = "/data/oppo/coloros/proton/sys_proton_brightness_list";
    private static final String SPLIT_SIGN = ",";
    private static final String START_TAG_RUS_LIST_CONTENT = "sys_proton_brightness_list_content = ";
    private static final String START_TAG_RUS_LIST_VERSION = "sys_proton_brightness_list_version = ";
    private static final String TAG = "ModelConfigUtil";
    private static final String TAG_ATTR = "attr";
    private static final String TAG_DEFAULT_CONFIG = "DefaultConfig";
    private static final String TAG_DEVICE_CONFIG = "DeviceConfig";
    private static final String TAG_MAIN_CONFIG = "MainConfig";
    private static final String TAG_SCENE = "scene";
    private static final String TAG_XS = "lux_list";
    private static final String TAG_YS = "brightness_list";
    private static volatile ModelConfigUtil sModelConfigUtil;
    private boolean mBrightnessTrainSwitch = true;
    private ModelConfig mDefaultModelConfig;
    private String mLocalContent;
    private int mLocalVersion;
    private ModelConfig mModelConfigFromFramework;
    private HashMap<String, ModelConfig> mModelConfigMap = new HashMap<>();
    private boolean mTriggerTurnOnAutoBrightness = false;

    private ModelConfigUtil() {
    }

    public static ModelConfigUtil getInstance() {
        if (sModelConfigUtil == null) {
            synchronized (ModelConfigUtil.class) {
                if (sModelConfigUtil == null) {
                    sModelConfigUtil = new ModelConfigUtil();
                }
            }
        }
        return sModelConfigUtil;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0140  */
    public synchronized void initialize(Context context) {
        Throwable th;
        boolean parseSuccess;
        Exception e;
        if (context != null) {
            Cursor cursor = null;
            String content = null;
            int version = 0;
            try {
                cursor = context.getContentResolver().query(CONTENT_URI, new String[]{"version", COLUMN_NAME_XML}, "filtername=\"sys_proton_brightness_list\"", null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0) {
                            int versionIndex = cursor.getColumnIndex("version");
                            int xmlIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                            cursor.moveToNext();
                            version = cursor.getInt(versionIndex);
                            content = cursor.getString(xmlIndex);
                        }
                    } catch (Exception e2) {
                        e = e2;
                        try {
                            ColorAILog.w(TAG, "initialize cannot get list from provider:" + e);
                            if (cursor != null) {
                            }
                            updateLocalXml();
                            ColorAILog.i(TAG, "initialize:" + content);
                            parseSuccess = false;
                            if (TextUtils.isEmpty(content) && version > this.mLocalVersion) {
                            }
                            if (!parseSuccess) {
                            }
                            AIBrightnessTrainSwitch.getInstance().setRusSwitch(this.mBrightnessTrainSwitch);
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e3) {
                e = e3;
                ColorAILog.w(TAG, "initialize cannot get list from provider:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                updateLocalXml();
                ColorAILog.i(TAG, "initialize:" + content);
                parseSuccess = false;
                if (TextUtils.isEmpty(content) && version > this.mLocalVersion) {
                }
                if (!parseSuccess) {
                }
                AIBrightnessTrainSwitch.getInstance().setRusSwitch(this.mBrightnessTrainSwitch);
            }
            updateLocalXml();
            ColorAILog.i(TAG, "initialize:" + content);
            parseSuccess = false;
            if (TextUtils.isEmpty(content) && version > this.mLocalVersion) {
                ColorAILog.i(TAG, "initialize parse rus xml");
                try {
                    parseSuccess = parserListXmlValue(content);
                } catch (IOException | XmlPullParserException e4) {
                    e4.printStackTrace();
                } catch (Exception e5) {
                    ColorAILog.w(TAG, "initialize rus xml error:" + e5);
                }
                if (parseSuccess) {
                    saveCurrentVersion(version, content);
                    ColorAILog.i(TAG, "initialize parse success, save to sp.");
                    if (this.mTriggerTurnOnAutoBrightness) {
                        ColorAILog.i(TAG, "initialize, receive new list and mTriggerTurnOffAutoBrightness is true, next SCREEN_OFF will open AUTO_BRIGHTNESS_MODE.");
                        new TriggerTurnOnAutoBrightnessReceiver().turnOnAutoBrightnessInNextScreenOff(context);
                    }
                } else {
                    ColorAILog.e(TAG, "initialize parse failed, pls check xml!!.");
                }
            }
            if (!parseSuccess) {
                ColorAILog.i(TAG, "initialize use local xml.");
                if (!TextUtils.isEmpty(this.mLocalContent)) {
                    ColorAILog.i(TAG, "initialize has local xml, use local xml.");
                    try {
                        parserListXmlValue(this.mLocalContent);
                    } catch (IOException | XmlPullParserException e6) {
                        e6.printStackTrace();
                    } catch (Exception e7) {
                        ColorAILog.w(TAG, "initialize rus xml error:" + e7);
                    }
                } else {
                    ColorAILog.i(TAG, "initialize has no local xml, use default value.");
                    ModelConfig config = new ModelConfig();
                    this.mModelConfigMap.put(config.getDevice(), config);
                }
            }
            AIBrightnessTrainSwitch.getInstance().setRusSwitch(this.mBrightnessTrainSwitch);
        } else {
            throw new IllegalArgumentException("context is null!");
        }
    }

    private void updateLocalXml() {
        File localFile = new File(LOCAL_FILE_PATH);
        if (localFile.exists()) {
            try {
                String localText = FileUtils.readTextFile(localFile, 0, null);
                if (!TextUtils.isEmpty(localText)) {
                    int indexVersion = localText.indexOf(START_TAG_RUS_LIST_VERSION);
                    int indexContent = localText.indexOf(START_TAG_RUS_LIST_CONTENT);
                    this.mLocalVersion = Integer.parseInt(localText.substring(START_TAG_RUS_LIST_VERSION.length() + indexVersion, indexContent).trim());
                    this.mLocalContent = localText.substring(START_TAG_RUS_LIST_CONTENT.length() + indexContent);
                    ColorAILog.i(TAG, "updateLocalXml mLocalVersion is " + this.mLocalVersion + "\nmLocalContent is " + this.mLocalContent);
                    return;
                }
                this.mLocalVersion = 0;
                this.mLocalContent = null;
            } catch (Exception e) {
                e.printStackTrace();
                this.mLocalVersion = 0;
                this.mLocalContent = null;
                if (localFile.delete()) {
                    ColorAILog.i(TAG, "updateLocalXml failed, delete the useless file.");
                }
            }
        } else {
            this.mLocalVersion = 0;
            this.mLocalContent = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b9, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00be, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bf, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c2, code lost:
        throw r5;
     */
    private void saveCurrentVersion(int ver, String content) {
        File localFile = new File(LOCAL_FILE_PATH);
        if (localFile.exists() && localFile.delete()) {
            ColorAILog.i(TAG, "saveCurrentVersion delete old file ok");
        }
        File dirFile = localFile.getParentFile();
        if (dirFile != null) {
            try {
                if (dirFile.exists()) {
                    if (!localFile.createNewFile()) {
                        ColorAILog.i(TAG, "saveCurrentVersion createNewFile failed," + localFile);
                    } else {
                        ColorAILog.i(TAG, "saveCurrentVersion createNewFile success");
                    }
                } else if (!dirFile.mkdirs()) {
                    ColorAILog.w(TAG, "saveCurrentVersion mkdirs failed");
                } else if (!localFile.createNewFile()) {
                    ColorAILog.i(TAG, "saveCurrentVersion createNewFile failed," + localFile);
                } else {
                    ColorAILog.i(TAG, "saveCurrentVersion createNewFile success");
                }
                ColorAILog.i(TAG, "saveCurrentVersion spFile.exists() is " + localFile.exists());
                String text = START_TAG_RUS_LIST_VERSION + ver + "\n" + START_TAG_RUS_LIST_CONTENT + content;
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
                    writer.write(text);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                ColorAILog.i(TAG, "saveCurrentVersion spFile.exists() is " + localFile.exists());
            } catch (Throwable th) {
                ColorAILog.i(TAG, "saveCurrentVersion spFile.exists() is " + localFile.exists());
                throw th;
            }
        } else {
            ColorAILog.e(TAG, "saveCurrentVersion error getParentFile is null." + localFile);
            ColorAILog.i(TAG, "saveCurrentVersion spFile.exists() is " + localFile.exists());
        }
    }

    public synchronized ModelConfig getModelConfig(Context context) {
        ColorAILog.d(TAG, "getModelConfig init: " + BrightnessConstants.SEPARATE_SOFT_CONFIG);
        ModelConfig modelConfig = this.mModelConfigMap.get(BrightnessConstants.SEPARATE_SOFT_CONFIG);
        if (modelConfig == null) {
            modelConfig = this.mModelConfigFromFramework;
            if (modelConfig != null) {
                saveDefaultPointsToSettings(modelConfig, context);
                return modelConfig;
            } else if (this.mDefaultModelConfig != null) {
                ColorAILog.w(TAG, "getModelConfig the device not exist, use default in list.");
                return this.mDefaultModelConfig;
            }
        }
        ColorAILog.w(TAG, "getModelConfig success.");
        return modelConfig;
    }

    private void saveDefaultPointsToSettings(ModelConfig modelConfig, Context context) {
        if (modelConfig == null || context == null) {
            ColorAILog.w(TAG, "modelConfig is null.Failed to save it to settings.");
            return;
        }
        float[] xs = modelConfig.getXs();
        float[] ys = modelConfig.getYs();
        if (xs == null || ys == null || xs.length < 1 || xs.length != ys.length) {
            ColorAILog.w(TAG, "The modelConfig is invalid.");
            return;
        }
        String lastSavedXs = Settings.System.getString(context.getContentResolver(), BrightnessConstants.SETTINGS_AIBRIGHTNESS_XS);
        String lastSavedYs = Settings.System.getString(context.getContentResolver(), BrightnessConstants.SETTINGS_AIBRIGHTNESS_YS);
        ColorAILog.d(TAG, "saveDefaultPointsToSettings lastSavedXs:" + lastSavedXs + "\nlastSavedYs:" + lastSavedYs);
        StringBuilder xsStrToSave = new StringBuilder();
        StringBuilder ysStrToSave = new StringBuilder();
        for (int i = 0; i < xs.length; i++) {
            if (xsStrToSave.length() < 1) {
                xsStrToSave.append(xs[i]);
                ysStrToSave.append(ys[i]);
            } else {
                xsStrToSave.append(SPLIT_SIGN);
                xsStrToSave.append(xs[i]);
                ysStrToSave.append(SPLIT_SIGN);
                ysStrToSave.append(ys[i]);
            }
        }
        if (!TextUtils.equals(xsStrToSave, lastSavedXs)) {
            ColorAILog.d(TAG, "saveDefaultPointsToSettings xsStrToSave:" + ((Object) xsStrToSave) + "\nlastSavedXs:" + lastSavedXs);
            Settings.System.putString(context.getContentResolver(), BrightnessConstants.SETTINGS_AIBRIGHTNESS_XS, xsStrToSave.toString());
        }
        if (!TextUtils.equals(ysStrToSave, lastSavedYs)) {
            ColorAILog.d(TAG, "saveDefaultPointsToSettings ysStrToSave:" + ((Object) ysStrToSave) + "\nlastSavedYs:" + lastSavedYs);
            Settings.System.putString(context.getContentResolver(), BrightnessConstants.SETTINGS_AIBRIGHTNESS_YS, ysStrToSave.toString());
        }
    }

    public void setDefaultModelConfig(ModelConfig modelConfig) {
        this.mModelConfigFromFramework = modelConfig;
    }

    public boolean isBrightnessTrainSwitchOpen() {
        return this.mBrightnessTrainSwitch;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:339:0x08bc  */
    /* JADX WARNING: Removed duplicated region for block: B:349:0x0901  */
    private boolean parserListXmlValue(String xmlValue) throws XmlPullParserException, IOException {
        boolean z;
        char c;
        char c2;
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        if (!TextUtils.isEmpty(xmlValue)) {
            parser.setInput(new StringReader(xmlValue));
            String str = null;
            this.mDefaultModelConfig = null;
            this.mModelConfigMap.clear();
            String currentTag = null;
            ModelConfig currentModelConfig = null;
            HashMap<String, SplineModel> splineModelMap = null;
            String splineName = null;
            float[] xs = null;
            float[] ys = null;
            int evenType = parser.getEventType();
            while (evenType != 1) {
                if (2 == evenType) {
                    String tagName = parser.getName();
                    switch (tagName.hashCode()) {
                        case -1226553000:
                            if (tagName.equals(TAG_DEVICE_CONFIG)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case -764741938:
                            if (tagName.equals(TAG_XS)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3004913:
                            if (tagName.equals(TAG_ATTR)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 109254796:
                            if (tagName.equals(TAG_SCENE)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1309933763:
                            if (tagName.equals(TAG_DEFAULT_CONFIG)) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1400850380:
                            if (tagName.equals(TAG_YS)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1995596411:
                            if (tagName.equals(TAG_MAIN_CONFIG)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            currentTag = tagName;
                            continue;
                        case 1:
                            currentTag = tagName;
                            this.mDefaultModelConfig = new ModelConfig();
                            currentModelConfig = this.mDefaultModelConfig;
                            continue;
                        case 2:
                            currentTag = tagName;
                            ModelConfig modelConfig = this.mDefaultModelConfig;
                            if (modelConfig == null) {
                                ColorAILog.w(TAG, "parserListXmlValue defaultModelConfig parse failed!");
                                break;
                            } else {
                                currentModelConfig = new ModelConfig(modelConfig);
                                ColorAILog.i(TAG, "parserListXmlValue begin TAG_DEVICE_CONFIG");
                                continue;
                            }
                        case 3:
                            if (currentTag != null) {
                                if (parser.getAttributeCount() <= 0) {
                                    break;
                                } else {
                                    String attributeValue = parser.getAttributeValue(0);
                                    switch (attributeValue.hashCode()) {
                                        case -2105904519:
                                            if (attributeValue.equals(ATTR_BRIGHTNESS_TRAIN_SWITCH)) {
                                                c2 = 1;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -2001852549:
                                            if (attributeValue.equals(ATTR_STABLE_RIGHT_MIN_LUX)) {
                                                c2 = ' ';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1973227906:
                                            if (attributeValue.equals(ATTR_SPLINE_POINT_SIZE)) {
                                                c2 = 21;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1480319960:
                                            if (attributeValue.equals(ATTR_NIGHT_HOURS)) {
                                                c2 = '#';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1366589679:
                                            if (attributeValue.equals(ATTR_HBM_XS)) {
                                                c2 = 24;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1337960528:
                                            if (attributeValue.equals(ATTR_HBM_YS)) {
                                                c2 = 27;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1335157162:
                                            if (attributeValue.equals(ATTR_DEVICE)) {
                                                c2 = 3;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1191263457:
                                            if (attributeValue.equals(ATTR_MIN_LIGHT_IN_DNM)) {
                                                c2 = '$';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1104756826:
                                            if (attributeValue.equals(ATTR_RIGHT_SCALE_IN_DARK_ENV)) {
                                                c2 = 30;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -1055933340:
                                            if (attributeValue.equals(ATTR_NORMAL_MAX_BRIGHTNESS)) {
                                                c2 = 23;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -873699637:
                                            if (attributeValue.equals(ATTR_STABLE_DOWN_SELF_PERCENT)) {
                                                c2 = '\n';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -838384802:
                                            if (attributeValue.equals(ATTR_HBM_MIN_XS)) {
                                                c2 = 25;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -792850916:
                                            if (attributeValue.equals(ATTR_DRAG_EXPAND_MULTIPLE_LEFT)) {
                                                c2 = 14;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -730694534:
                                            if (attributeValue.equals(ATTR_MAIN_SWITCH)) {
                                                c2 = 0;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -590473917:
                                            if (attributeValue.equals(ATTR_MAX_BRIGHTNESS_CHANGE)) {
                                                c2 = 19;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -516951272:
                                            if (attributeValue.equals(ATTR_SPEED_MULTIPLE_IN_DISTANCE)) {
                                                c2 = 20;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -412942515:
                                            if (attributeValue.equals(ATTR_DELTA_UP_SCALE)) {
                                                c2 = 15;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -269907796:
                                            if (attributeValue.equals(ATTR_DAY_HOURS)) {
                                                c2 = '\"';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case -260790543:
                                            if (attributeValue.equals(ATTR_REVISE_X)) {
                                                c2 = 5;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 149912572:
                                            if (attributeValue.equals(ATTR_TRIGGER_TURN_ON_AUTO_BRIGHTNESS)) {
                                                c2 = 2;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 288683697:
                                            if (attributeValue.equals(ATTR_MIN_BRIGHTNESS_CHANGE)) {
                                                c2 = 18;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 347157439:
                                            if (attributeValue.equals(ATTR_STABLE_DOWN_MIN_PERCENT)) {
                                                c2 = '\t';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 384266142:
                                            if (attributeValue.equals(ATTR_REVISE_X_MULTIPLE)) {
                                                c2 = 6;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 395204086:
                                            if (attributeValue.equals(ATTR_STABLE_SMALL_CHANGE_SCALE_IN_DARK_ENV)) {
                                                c2 = 31;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 614083442:
                                            if (attributeValue.equals(ATTR_STABLE_SMALL_CHANGE_PERCENT)) {
                                                c2 = 11;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 921569394:
                                            if (attributeValue.equals(ATTR_STABLE_UP_SELF_PERCENT)) {
                                                c2 = '\b';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 937811852:
                                            if (attributeValue.equals(ATTR_HBM_MAX_XS)) {
                                                c2 = 26;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1172695311:
                                            if (attributeValue.equals(ATTR_DEFAULT_BRIGHTNESS)) {
                                                c2 = 22;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1197086375:
                                            if (attributeValue.equals(ATTR_DRAG_EXPAND_MULTIPLE_RIGHT)) {
                                                c2 = '\r';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1236353336:
                                            if (attributeValue.equals(ATTR_STABLE_UP_MIN_PERCENT)) {
                                                c2 = 7;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1415989571:
                                            if (attributeValue.equals(ATTR_STABLE_SMALL_RIGHT_MIN_LUX)) {
                                                c2 = '!';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1537054666:
                                            if (attributeValue.equals(ATTR_STABLE_SMALL_CHANGE_TARGET_PERCENT)) {
                                                c2 = '\f';
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1596725457:
                                            if (attributeValue.equals(ATTR_LEFT_SCALE_IN_DARK_ENV)) {
                                                c2 = 29;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1713338162:
                                            if (attributeValue.equals(ATTR_DARK_LUX_THRESHOLD)) {
                                                c2 = 28;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1878235855:
                                            if (attributeValue.equals(ATTR_REVISE_X_CHANGE_POINT)) {
                                                c2 = 4;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 1946820646:
                                            if (attributeValue.equals(ATTR_FRAME_DURATION)) {
                                                c2 = 17;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        case 2084480596:
                                            if (attributeValue.equals(ATTR_DELTA_DOWN_SCALE)) {
                                                c2 = 16;
                                                break;
                                            }
                                            c2 = 65535;
                                            break;
                                        default:
                                            c2 = 65535;
                                            break;
                                    }
                                    switch (c2) {
                                        case 0:
                                            continue;
                                        case 1:
                                            this.mBrightnessTrainSwitch = Boolean.parseBoolean(parser.nextText());
                                            ColorAILog.d(TAG, "parserXml ATTR_BRIGHTNESS_TRAIN_SWITCH set :" + this.mBrightnessTrainSwitch);
                                            continue;
                                        case 2:
                                            this.mTriggerTurnOnAutoBrightness = Boolean.parseBoolean(parser.nextText());
                                            ColorAILog.d(TAG, "parserXml ATTR_TRIGGER_TURN_OFF_AUTO_BRIGHTNESS set :" + this.mTriggerTurnOnAutoBrightness);
                                            continue;
                                        case 3:
                                            String nextText = parser.nextText();
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDevice(nextText);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DEVICE set :" + nextText);
                                            continue;
                                        case 4:
                                            float reviseXChangePoint = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setReviseXChangePoint(reviseXChangePoint);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_REVISE_X_CHANGE_POINT set :" + reviseXChangePoint);
                                            continue;
                                        case 5:
                                            float reviseX = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setReviseX(reviseX);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_REVISE_X set :" + reviseX);
                                            continue;
                                        case 6:
                                            float reviseXMultiple = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setReviseXMultiple(reviseXMultiple);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_REVISE_X_MULTIPLE set :" + reviseXMultiple);
                                            continue;
                                        case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                                            float stableUpMinPercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableUpMinPercent(stableUpMinPercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_UP_MIN_PERCENT set :" + stableUpMinPercent);
                                            continue;
                                        case '\b':
                                            float stableUpSelfPercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableUpSelfPercent(stableUpSelfPercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_UP_SELF_PERCENT set :" + stableUpSelfPercent);
                                            continue;
                                        case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                                            float stableDownMinPercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableDownMinPercent(stableDownMinPercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_DOWN_MIN_PERCENT set :" + stableDownMinPercent);
                                            continue;
                                        case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /* 10 */:
                                            float stableDownSelfPercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableDownSelfPercent(stableDownSelfPercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_DOWN_SELF_PERCENT set :" + stableDownSelfPercent);
                                            continue;
                                        case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /* 11 */:
                                            float stableSmallChangePercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableSmallChangePercent(stableSmallChangePercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_SMALL_CHANGE_PERCENT set :" + stableSmallChangePercent);
                                            continue;
                                        case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                                            float stableSmallChangeTargetPercent = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableSmallChangeTargetPercent(stableSmallChangeTargetPercent);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_SMALL_CHANGE_TARGET_PERCENT set :" + stableSmallChangeTargetPercent);
                                            continue;
                                        case '\r':
                                            float dragExpandMultipleRight = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDragExpandMultipleRight(dragExpandMultipleRight);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DRAG_EXPAND_MULTIPLE_RIGHT set :" + dragExpandMultipleRight);
                                            continue;
                                        case 14:
                                            float dragExpandMultipleLeft = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDragExpandMultipleLeft(dragExpandMultipleLeft);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DRAG_EXPAND_MULTIPLE_LEFT set :" + dragExpandMultipleLeft);
                                            continue;
                                        case 15:
                                            float deltaUpScale = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDeltaUpScale(deltaUpScale);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DELAY_BRIGHTNESS_UP set :" + deltaUpScale);
                                            continue;
                                        case ColorHansRestriction.HANS_RESTRICTION_BLOCK_ALARM /* 16 */:
                                            float deltaDownScale = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDeltaDownScale(deltaDownScale);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DELAY_BRIGHTNESS_DOWN set :" + deltaDownScale);
                                            continue;
                                        case 17:
                                            int frameDuration = Integer.parseInt(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setFrameDuration(frameDuration);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_FRAME_DURATION set :" + frameDuration);
                                            continue;
                                        case 18:
                                            float minBrightnessChange = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setMinBrightnessChange(minBrightnessChange);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_MIN_BRIGHTNESS_CHANGE set :" + minBrightnessChange);
                                            continue;
                                        case 19:
                                            float maxBrightnessChange = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setMaxBrightnessChange(maxBrightnessChange);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_MAX_BRIGHTNESS_CHANGE set :" + maxBrightnessChange);
                                            continue;
                                        case ColorHansManager.HansMainHandler.HANS_MSG_KILL_ABNORMAL_APP /* 20 */:
                                            float speedMultipleInDistance = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setSpeedMultipleInDistance(speedMultipleInDistance);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_SPEED_MULTIPLE_IN_DISTANCE set :" + speedMultipleInDistance);
                                            continue;
                                        case ColorHansManager.HansMainHandler.MSG_CHECK_JOB_WAKELOCK /* 21 */:
                                            int splinePointSize = Integer.parseInt(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setSplinePointSize(splinePointSize);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_SPLINE_POINT_SIZE set :" + splinePointSize);
                                            continue;
                                        case 22:
                                            float defaultBrightness = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDefaultBrightness(defaultBrightness);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DEFAULT_BRIGHTNESS set :" + defaultBrightness);
                                            continue;
                                        case 23:
                                            float normalMaxBrightness = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setNormalMaxBrightness(normalMaxBrightness);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_NORMAL_MAX_BRIGHTNESS set :" + normalMaxBrightness);
                                            continue;
                                        case 24:
                                            String nextText2 = parser.nextText();
                                            float[] hbmXs = parseFloatArray(nextText2);
                                            if (hbmXs == null || hbmXs.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setHbmXs(hbmXs);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_HBM_XS set :" + nextText2);
                                                continue;
                                            }
                                        case 25:
                                            String nextText3 = parser.nextText();
                                            float[] hbmMinXs = parseFloatArray(nextText3);
                                            if (hbmMinXs == null || hbmMinXs.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setHbmMinXs(hbmMinXs);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_HBM_MIN_XS set :" + nextText3);
                                                continue;
                                            }
                                        case OppoNotificationManager.SDK_INT_26 /* 26 */:
                                            String nextText4 = parser.nextText();
                                            float[] hbmMaxXs = parseFloatArray(nextText4);
                                            if (hbmMaxXs == null || hbmMaxXs.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setHbmMaxXs(hbmMaxXs);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_HBM_MAX_YS set :" + nextText4);
                                                continue;
                                            }
                                        case 27:
                                            String nextText5 = parser.nextText();
                                            float[] hbmYs = parseFloatArray(nextText5);
                                            if (hbmYs == null || hbmYs.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setHbmYs(hbmYs);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_HBM_YS set :" + nextText5);
                                                continue;
                                            }
                                        case 28:
                                            float darkLuxThreshold = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setDarkLuxThreshold(darkLuxThreshold);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_DARK_LUX_THRESHOLD set :" + darkLuxThreshold);
                                            continue;
                                        case 29:
                                            float left_scale_in_dark_env = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setLeftScaleInDarkEnv(left_scale_in_dark_env);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_LEFT_SCALE_IN_DARK_ENV set :" + left_scale_in_dark_env);
                                            continue;
                                        case 30:
                                            float right_scale_in_dark_env = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setRightScaleInDarkEnv(right_scale_in_dark_env);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_RIGHT_SCALE_IN_DARK_ENV set :" + right_scale_in_dark_env);
                                            continue;
                                        case 31:
                                            float stableSmallChangeScaleInDarkEnv = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableSmallChangeScaleInDarkEnv(stableSmallChangeScaleInDarkEnv);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_SMALL_CHANGE_SCALE_IN_DARK_ENV set :" + stableSmallChangeScaleInDarkEnv);
                                            continue;
                                        case ColorHansRestriction.HANS_RESTRICTION_BLOCK_SYNC /* 32 */:
                                            float stable_right_min_lux = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableRightMinLux(stable_right_min_lux);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_RIGHT_MIN_LUX set :" + stable_right_min_lux);
                                            continue;
                                        case '!':
                                            float stableSmallRightMinLux = Float.parseFloat(parser.nextText());
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setStableSmallRightMinLux(stableSmallRightMinLux);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_STABLE_SMALL_RIGHT_MIN_LUX set :" + stableSmallRightMinLux);
                                            continue;
                                        case '\"':
                                            String nextText6 = parser.nextText();
                                            int[] dayHours = parseIntArray(nextText6);
                                            if (dayHours == null || dayHours.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setDayHours(dayHours);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_DAY_HOURS set :" + nextText6);
                                                continue;
                                            }
                                            break;
                                        case '#':
                                            String nextText7 = parser.nextText();
                                            int[] nightHours = parseIntArray(nextText7);
                                            if (nightHours == null || nightHours.length <= 0) {
                                                break;
                                            } else {
                                                if (currentModelConfig != null) {
                                                    currentModelConfig.setNightHours(nightHours);
                                                }
                                                ColorAILog.d(TAG, "parserXml ATTR_NIGHT_HOURS set :" + nextText7);
                                                continue;
                                            }
                                            break;
                                        case '$':
                                            String nextText8 = parser.nextText();
                                            float[] minLightInDnm = parseFloatArray(nextText8);
                                            if (currentModelConfig != null) {
                                                currentModelConfig.setMinLightInDNM(minLightInDnm);
                                            }
                                            ColorAILog.d(TAG, "parserXml ATTR_MIN_LIGHT_IN_DNM set :" + nextText8);
                                            continue;
                                        default:
                                            continue;
                                    }
                                }
                            } else {
                                ColorAILog.w(TAG, "parserListXmlValue failed! currentTag == null");
                                continue;
                            }
                            break;
                        case 4:
                            splineName = parser.getAttributeValue(str, BrightnessConstants.AppSplineXml.TAG_NAME);
                            ColorAILog.d(TAG, "parserXml scene:" + splineName);
                            continue;
                        case 5:
                            String nextText9 = parser.nextText();
                            xs = parseFloatArray(nextText9);
                            ColorAILog.d(TAG, "parserXml ATTR_XS set :" + nextText9);
                            continue;
                        case 6:
                            String nextText10 = parser.nextText();
                            ys = parseFloatArray(nextText10);
                            ColorAILog.d(TAG, "parserXml ATTR_YS set :" + nextText10);
                            continue;
                    }
                } else if (3 == evenType) {
                    String tagName2 = parser.getName();
                    int hashCode = tagName2.hashCode();
                    if (hashCode != -1226553000) {
                        if (hashCode == 109254796 && tagName2.equals(TAG_SCENE)) {
                            z = true;
                            if (z) {
                                if (z && xs != null && ys != null && !TextUtils.isEmpty(splineName)) {
                                    SplineModel splineModel = new SplineModel();
                                    splineModel.setXs(xs);
                                    splineModel.setYs(ys);
                                    if (splineModelMap == null) {
                                        splineModelMap = new HashMap<>();
                                    }
                                    ColorAILog.d(TAG, "add scene spline:" + splineName + " " + splineModel);
                                    splineModelMap.put(splineName, splineModel);
                                    splineName = null;
                                    xs = null;
                                    ys = null;
                                }
                            } else if (currentModelConfig != null) {
                                if (!TextUtils.isEmpty(currentModelConfig.getDevice())) {
                                    ColorAILog.d(TAG, "Add ModelConfig to mModelConfigMap:" + currentModelConfig.getDevice());
                                    this.mModelConfigMap.put(currentModelConfig.getDevice(), currentModelConfig);
                                }
                                if (splineModelMap != null && !splineModelMap.isEmpty()) {
                                    currentModelConfig.setSplineModelHashMap(splineModelMap);
                                    splineModelMap = null;
                                }
                            }
                        }
                    } else if (tagName2.equals(TAG_DEVICE_CONFIG)) {
                        z = false;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                }
                evenType = parser.next();
                str = null;
            }
            return true;
        }
        ColorAILog.e(TAG, "parserListXmlValue failed! All contents are empty!");
        return false;
    }

    private int[] parseIntArray(String text) {
        String[] split = text.trim().replace(" ", "").split(SPLIT_SIGN);
        int length = split.length;
        if (length <= 0) {
            return null;
        }
        int[] fArray = new int[length];
        for (int i = 0; i < length; i++) {
            fArray[i] = Integer.parseInt(split[i]);
        }
        return fArray;
    }

    private float[] parseFloatArray(String text) {
        String[] split = text.trim().replace(" ", "").split(SPLIT_SIGN);
        int length = split.length;
        if (length <= 0) {
            return null;
        }
        float[] fArray = new float[length];
        for (int i = 0; i < length; i++) {
            fArray[i] = Float.parseFloat(split[i]);
        }
        return fArray;
    }

    public class TriggerTurnOnAutoBrightnessReceiver extends BroadcastReceiver {
        public TriggerTurnOnAutoBrightnessReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                if (Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0) == 0) {
                    Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 1);
                    ColorAILog.i(ModelConfigUtil.TAG, "onReceive, open AUTO_BRIGHTNESS_MODE success.");
                } else {
                    ColorAILog.i(ModelConfigUtil.TAG, "onReceive, AUTO_BRIGHTNESS_MODE already open.");
                }
                try {
                    context.unregisterReceiver(this);
                } catch (Exception e) {
                    ColorAILog.w(ModelConfigUtil.TAG, "onReceive unregister unregisterReceiver error:" + e);
                }
            }
        }

        public void turnOnAutoBrightnessInNextScreenOff(Context context) {
            context.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }
    }
}
