package com.android.server.display;

import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import com.android.server.EventLogTags;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.ProcessList;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.input.InputManagerService;
import com.android.server.lights.OppoLightsService;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.power.IPswShutdownFeature;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.vr.Vr2dDisplay;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import oppo.util.OppoStatistics;

public class OppoBrightUtils implements IOppoBrightness {
    public static final int ADJUSTMENT_GALLERY_IN = 16385;
    public static final int ADJUSTMENT_GALLERY_OUT = 32769;
    public static final int ADJUSTMENT_INVERSE_OFF = 32768;
    public static final int ADJUSTMENT_INVERSE_ON = 16384;
    public static final int ADJUSTMENT_VIDEO_IN = 16386;
    public static final int ADJUSTMENT_VIDEO_OUT = 32770;
    private static final long ADJUST_AD_TIMEOUT = 4000;
    private static final int AD_DISABLE = 0;
    private static final int AD_ENABLE = 1;
    public static final int BRIGHTNESS_BOOST_AFTER_SCREEN_ON = 2;
    public static final int BRIGHTNESS_BOOST_DEFAULT_STATE = 0;
    public static final int BRIGHTNESS_BOOST_FINISH_STATE = 4;
    public static final long BRIGHTNESS_BOOST_SCREENON_TIMEOUT = 4000;
    public static final int BRIGHTNESS_BOOST_SCREEN_ON = 1;
    public static final long BRIGHTNESS_BOOST_SWITCHON_TIMEOUT = 600;
    public static final int BRIGHTNESS_BOOST_SWITCH_ON = 3;
    private static final int BRIGHTNESS_DRAG_EFFECT_INTERVAL = 3;
    private static final float BRIGHTNESS_DRAG_MAX_SCALE = 0.3f;
    private static final int BRIGHTNESS_DRAG_NO_EFFECT_INTERVAL = 1;
    public static final long BRIGHTNESS_LIGHT_LIMIT = 70;
    public static final String BRIGHTNESS_MODE = "persist.sys.brightness.mode";
    public static final int BRIGHTNESS_OVERRIDE_IN = 1;
    public static final int BRIGHTNESS_OVERRIDE_NONE = -1;
    public static final int BRIGHTNESS_OVERRIDE_OUT = 0;
    public static int BRIGHTNESS_RAMP_RATE_FAST = 0;
    public static int BRIGHTNESS_RAMP_RATE_SCREENON = 0;
    public static int BRIGHTNESS_RAMP_RATE_SLOW = 0;
    public static int BRIGHTNESS_STEPS = 7;
    public static final int CAMERA_MODE_IN = 1;
    public static final int CAMERA_MODE_NONE = -1;
    public static final int CAMERA_MODE_OUT = 0;
    private static final int CODE_SEND_AD_STATUS_TO_SF = 11000;
    public static final int DC_MODE_BRIGHT_EDGE = 260;
    public static boolean DEBUG = false;
    public static boolean DEBUG_PRETEND_PROX_SENSOR_ABSENT = false;
    private static final String DEBUG_PRO = "oppo.brightness.debug";
    public static boolean DEBUG_REPORT = false;
    private static final String DEFAULT_MANUFACTURE = "UNKNOWN";
    private static final String DEVICE_PATH = "/proc/devinfo";
    public static final int EIGHT_BITS_BRIGHTNESS_CONFIG = 1;
    private static final int EIGHT_BITS_BRIGHT_MAX_RATE = 90;
    public static final int EIGHT_BITS_MAXBRIGHTNESS = 255;
    public static final long ELEVENBITSBRIGHTNESS_LIGHT_LIMIT = 300;
    public static final int ELEVEN_BITS_BRIGHTNESS_CONFIG = 3;
    private static final int ELEVEN_BITS_BRIGHT_MAX_RATE = 500;
    public static final int ELEVEN_BITS_MAXBRIGHTNESS = 2047;
    public static final float ELVENBITS_QUICKLY_DARK_AMOUNT = 20.0f;
    private static final float ENABLE_AD_LUX = 1000.0f;
    public static final int GALLERY_IN = 1;
    public static final int GALLERY_OUT = 0;
    public static final String GLOBAL_HBM_SELL_MODE = "global_hbm_sell_mode";
    public static int HBM_EXTEND_MAXBRIGHTNESS = ELEVEN_BITS_MAXBRIGHTNESS;
    public static float HBM_LUX_LEVEL_EIGHT = 100000.0f;
    public static float HBM_LUX_LEVEL_FIVE = 30000.0f;
    public static float HBM_LUX_LEVEL_FOUR = 20000.0f;
    public static float HBM_LUX_LEVEL_SEVEN = 60000.0f;
    public static float HBM_LUX_LEVEL_SIX = 40000.0f;
    public static final int HIGH_BRIGHTNESS_CAMERA_ON = 1;
    public static final int HIGH_BRIGHTNESS_DISABLE = 0;
    public static final int HIGH_BRIGHTNESS_LCD_OFF = 0;
    public static final int HIGH_BRIGHTNESS_LCD_ON = 1;
    public static final int HIGH_BRIGHTNESS_LEVEL_FIVE = 5;
    public static final int HIGH_BRIGHTNESS_LEVEL_FOUR = 4;
    public static final int HIGH_BRIGHTNESS_LEVEL_MAX = 8;
    public static final int HIGH_BRIGHTNESS_LEVEL_SEVEN = 7;
    public static final int HIGH_BRIGHTNESS_LEVEL_SIX = 6;
    public static final String HIGH_BRIGHTNESS_NODE = "/sys/kernel/oppo_display/hbm";
    private static final int INVAILD_SCREEN_DEFAULT_BRIGHTNESS = -1;
    public static final int INVERSE_OFF = 0;
    public static final int INVERSE_ON = 1;
    private static final String KEY_DEVICE_MANUFACTURE = "Device manufacture:";
    private static final String KEY_DEVICE_VERSION = "Device version:";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_BOE = "ro.lcd.backlight.config_boe";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_BOE_TENBITS = "ro.lcd.backlight.boe_tenbit";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM = "ro.lcd.backlight.config_dsjm";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_HX_JDI_ELEVENBITS = "ro.lcd.backlight.jdi_himax_2048";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_JDI = "ro.lcd.backlight.config_jdi";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_NT_JDI_ELEVENBITS = "ro.lcd.backlight.jdi_nt_2048";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_NT_TIANMA_ELEVENBITS = "ro.lcd.backlight.tianma_nt_2048";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG = "ro.lcd.backlight.config_samsung";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG_TENBITS = "ro.lcd.backlight.samsung_tenbit";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA = "ro.lcd.backlight.config_tianma";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY = "ro.lcd.backlight.config_truly";
    private static String LCD_DEVICE_MANUFACTURE = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
    private static String LCD_DEVICE_VERSION = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
    private static final String LCD_DEVICE_VERSION_TD4310 = "td4310";
    private static final String LCD_MANUFACTURE_BOE = "boe";
    private static final String LCD_MANUFACTURE_BOE_TENBITS = "boe1024";
    private static final String LCD_MANUFACTURE_DSJM = "dsjm";
    private static final String LCD_MANUFACTURE_HX_JDI_ELEVENBITS = "hx2048";
    private static final String LCD_MANUFACTURE_JDI = "jdi";
    private static final String LCD_MANUFACTURE_NT_JDI_ELEVENBITS = "nt2048";
    private static final String LCD_MANUFACTURE_NT_TIANMA_ELEVENBITS = "tm2048";
    private static final String LCD_MANUFACTURE_SAMSUNG = "samsung";
    private static final String LCD_MANUFACTURE_SAMSUNG_TENBITS = "samsung1024";
    private static final String LCD_MANUFACTURE_TIANMA = "tianma";
    private static final String LCD_MANUFACTURE_TRULY = "truly";
    private static final float LOW_PERCENT_BRIGHTNESS = 0.012f;
    public static final float MAX_LUX_LIMITI = 31000.0f;
    public static final float MIN_LUX_LIMITI = 0.0f;
    private static final int MSG_ADJUST_AD = 1;
    private static final int MSG_REPORT_APP_PROCESS = 2;
    private static final int MSG_SCENE_RESET = 1;
    private static final int MSG_UNREGIST_REPORT_SENSOR = 3;
    public static final String OPPO_GHBM_MODE = "display_gloableHBM_settings";
    public static final String OUTDOOR_BL_NODE = "/sys/devices/virtual/mtk_disp_mgr/mtk_disp_mgr/outdoorbl";
    private static final String PLATFORM_MTK = "oppo.hw.manufacturer.mtk";
    private static final String PLATFORM_QUALCOMM = "oppo.hw.manufacturer.qualcomm";
    public static final long POCKET_RIGNING_STATE_TIMEOUT = 4000;
    public static final int POCKET_RINGING_STATE = 1;
    public static final float QUICKLY_DARK_AMOUNT = 2.0f;
    public static boolean REPORT_FEATURE_ON = true;
    private static final String REPORT_PROPERITY = "oppo.brightness.report";
    private static final int SAMPLE_SIZE = 20;
    private static final long SCENE_RESET_DELAY = 65000;
    private static final long SCENE_RESET_DELAY_INCAR = 15000;
    public static final long SCREENON_BRIGHTNESS_BOOST_TIMEOUT = 4000;
    private static final int SCREEN_AUTO_BRIGHTNESS_ELEVENBITS_LENGTH = 10;
    private static final int SCREEN_AUTO_BRIGHTNESS_OFFSET = 2;
    private static final int SCREEN_DEFAULT_BRIGHTNESS_OFFSET = 1;
    public static final int SPECIAL_AMBIENT_LIGHT_HORIZON = 60000;
    public static final int SPECIAL_BRIGHTNEES_MASK = -1;
    public static final int SPECIAL_BRIGHTNESS_GAMESPACE = 1;
    public static final int SPECIAL_SCENE_NORMAL = 0;
    public static final int SPECIAL_SCENE_STREET_LAMP = 1;
    public static final int SPECIAL_SCENE_STREET_LAMP_INCAR = 2;
    private static int SPECIAL_SCENE_STREET_LAMP_LUX = 50;
    private static final long STREET_LAMP_DARK_DEBOUNCE = 50000;
    private static final long STREET_LAMP_INCAR_DARK_DEBOUNCE = 10000;
    private static final long STREET_LAMP_MAX_TIME = 60000;
    private static final long STREET_LAMP_MIN_TIME = 5000;
    private static final String TAG = "OppoBrightUtils";
    public static final float TENBITS_QUICKLY_DARK_AMOUNT = 10.0f;
    public static final int TEN_BITS_BRIGHTNESS_CONFIG = 2;
    private static final int TEN_BITS_BRIGHT_MAX_RATE = 120;
    public static final int TEN_BITS_MAXBRIGHTNESS = 1023;
    private static final long VALID_SAMPLE_TIME_LIMIT = 70000;
    public static final int VIDEO_IN = 1;
    public static final int VIDEO_OUT = 0;
    public static int[] brighness_int_config = null;
    public static boolean mAppliedLowPower = false;
    public static float mAutoAmbientLuxValue = MIN_LUX_LIMITI;
    public static float[] mAutoBrightnessLux = null;
    public static float[] mAutoBrightnessLuxMaxLimit = null;
    public static float[] mAutoBrightnessLuxMinLimit = null;
    public static final String mBacklightReportTag = "psw_multimedia";
    public static int mBrightnessBitsConfig = 1;
    public static int mBrightnessBoost = 0;
    public static boolean mBrightnessLowLevelScreenSupport = false;
    public static boolean mBrightnessNoAnimation = false;
    public static int mBrightnessOverride = -1;
    public static int mBrightnessOverrideAdj = 0;
    public static float mBrightnessOverrideAmbientLux = MIN_LUX_LIMITI;
    public static boolean mCameraBacklight = false;
    public static int mCameraMode = -1;
    public static boolean mCameraUseAdjustmentSetting = false;
    public static boolean mClearManualBritness = false;
    public static long mDarkDebounce = 0;
    public static int mDefaultBrightness = 102;
    public static boolean mDisplayStateOn = false;
    public static boolean mFingerprintOpticalSupport = false;
    public static boolean mFirstSetOverride = true;
    public static boolean mFirstSetScreenState = true;
    public static boolean mGalleryBacklight = false;
    public static int mGalleryMode = 0;
    public static boolean mHighBrightnessModeSupport = false;
    public static int mInverseMode = 0;
    public static boolean mIsOledHighBrightness = false;
    private static long mLampGapTime = 0;
    public static int mLastBrightness = -1;
    private static final Object mLock = new Object();
    public static float mManualAmbientLuxBackup = MIN_LUX_LIMITI;
    public static int mManualBrightness = 0;
    public static int mManualBrightnessBackup = 0;
    public static boolean mManualSetAutoBrightness = false;
    public static float mManulAtAmbientLux = MIN_LUX_LIMITI;
    public static int mMaxBrightness = 255;
    private static int mMaxSampleLux = 0;
    public static int mMinBrightness = 2;
    private static int mMinSampleLux = 0;
    public static boolean mOppoScreenBrightnessSupport = true;
    public static boolean mOutdoorBrightnessSupport = false;
    private static boolean mPlatformMtk = false;
    private static boolean mPlatformQualcomm = false;
    public static boolean mPocketRingingState = false;
    private static long mPreTurningPointTime = 0;
    public static boolean mReduceBrightnessAnimating = false;
    public static int mReduceBrightnessMode = 0;
    public static float mReduceBrightnessRate = 1.0f;
    public static int mRefManualToAutoBritness = 0;
    private static int mSampleCount = 0;
    public static int mSampleScene = 0;
    public static boolean mSaveBrightnessByShutdown = false;
    public static boolean mScreenGlobalHBMSupport = false;
    public static boolean mScreenUnderLightSensorSupport = false;
    public static boolean mSetBrihgtnessSlide = false;
    public static int mShouldAdjustRate = 0;
    public static boolean mShouldFastRate = false;
    public static int mSpecialBrightnessFlag = 0;
    private static long mTurningPointTime = 0;
    public static boolean mUseAutoBrightness = false;
    public static boolean mUseWindowBrightness = false;
    public static int mVideoMode = 0;
    public static ArrayList<Long> sAutoLuxWatchPointTotalTimes = new ArrayList<>(10);
    public static int sGlobalHbmSellMode = 0;
    public static boolean sHbmAutoBrightness = false;
    public static ArrayList<Float> sHbmLevels = new ArrayList<>(10);
    public static ArrayList<Long> sManuLuxWatchPointTotalTimes = new ArrayList<>(10);
    public static boolean sPSensorNotSupport = false;
    public static boolean sUseRotateSupport = false;
    private String SPECIALIGHTSENSOR;
    private String SPECIALIGHTSENSOR1;
    private String SPECIAL_LIGHT_SENSOR2;
    private String SPECIAL_LIGHT_SENSOR3;
    private String SPECIAL_LIGHT_SENSOR4;
    private String SPECIAL_LIGHT_SENSOR5;
    private int[] cameraGallerySamsung;
    private ContentObserver cob;
    public boolean isAIBrightnessFeatureOpen;
    /* access modifiers changed from: private */
    public boolean isRegiterLightSensor;
    public int mAIBrightness;
    private AIBrightnessHelper mAIBrightnessHelper;
    /* access modifiers changed from: private */
    public int mAdStatus;
    /* access modifiers changed from: private */
    public int mAdStatus_before;
    /* access modifiers changed from: private */
    public long mAppDurationTime;
    private int mBootBrightConfig;
    private int mBootupBrightVaule;
    private String mBrightnessConfig;
    private Calendar mCalendar;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public String mCurrentAppProcessName;
    private int mEnable_before;
    private ArrayList<String> mFeatures;
    private IBinder mFlinger;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public String mLastAppProcessName;
    /* access modifiers changed from: private */
    public Sensor mLightSensor;
    /* access modifiers changed from: private */
    public boolean mLightSensorAlwaysOn;
    /* access modifiers changed from: private */
    public final SensorEventListener mLightSensorListener;
    /* access modifiers changed from: private */
    public float mManulAmbientLuxValue;
    private int mNightBrihtness;
    private OppoBrightnessWAArgsHelper mOppoWAArgsHelper;
    private final SensorEventListener mReportLightSensorListener;
    /* access modifiers changed from: private */
    public HashMap<String, String> mReportMap;
    private SensorManager mReportSensorManager;
    private int[] mSampleLux;
    private long[] mSampleTime;
    private Handler mSceneHandler;
    /* access modifiers changed from: private */
    public SensorManager mSensorManager;
    public boolean mSpecialLightSensor;
    private boolean mStkCmLightSensor;

    private static class OppoBrightUtilsHolder {
        static final OppoBrightUtils INSTANCE = new OppoBrightUtils();

        private OppoBrightUtilsHolder() {
        }
    }

    public static OppoBrightUtils getInstance() {
        return OppoBrightUtilsHolder.INSTANCE;
    }

    private OppoBrightUtils() {
        this.mBootupBrightVaule = 200;
        this.mNightBrihtness = 0;
        this.mSpecialLightSensor = false;
        this.SPECIALIGHTSENSOR = "apds9921";
        this.SPECIALIGHTSENSOR1 = "cm36686";
        this.SPECIAL_LIGHT_SENSOR2 = "stk3210";
        this.SPECIAL_LIGHT_SENSOR3 = "cm36286s";
        this.SPECIAL_LIGHT_SENSOR4 = "stk3220";
        this.SPECIAL_LIGHT_SENSOR5 = "apds9922_s";
        this.mStkCmLightSensor = false;
        this.mLightSensorAlwaysOn = false;
        this.mAdStatus = 2;
        this.mAdStatus_before = 2;
        this.mEnable_before = 0;
        this.mFlinger = null;
        this.mFeatures = new ArrayList<>(10);
        this.mSampleLux = new int[20];
        this.mSampleTime = new long[20];
        this.mManulAmbientLuxValue = MIN_LUX_LIMITI;
        this.isRegiterLightSensor = false;
        this.mAppDurationTime = System.currentTimeMillis();
        this.mLastAppProcessName = "";
        this.mCurrentAppProcessName = "";
        this.mOppoWAArgsHelper = null;
        this.mBrightnessConfig = null;
        this.mBootBrightConfig = -1;
        this.isAIBrightnessFeatureOpen = false;
        this.cob = new ContentObserver(new Handler()) {
            /* class com.android.server.display.OppoBrightUtils.AnonymousClass1 */

            public void onChange(boolean selfChange, Uri uri) {
                if (OppoBrightUtils.DEBUG) {
                    Slog.d(OppoBrightUtils.TAG, "GHBM onChange");
                }
                String modeFromUri = uri.toString();
                if (modeFromUri.contains(OppoBrightUtils.OPPO_GHBM_MODE)) {
                    int temp_ghbm_mode = Settings.Secure.getInt(OppoBrightUtils.this.mContext.getContentResolver(), OppoBrightUtils.OPPO_GHBM_MODE, -1);
                    Slog.d(OppoBrightUtils.TAG, "GHBM temp_ghbm_mode=" + temp_ghbm_mode);
                    if (temp_ghbm_mode == 0) {
                        OppoBrightUtils.setGloableHBMFeature(false);
                    } else if (temp_ghbm_mode >= 1) {
                        OppoBrightUtils.setGloableHBMFeature(true);
                    }
                } else {
                    Slog.d(OppoBrightUtils.TAG, "onChange:modeFromUri[" + modeFromUri + "], but we cannot handle this yet.");
                }
            }
        };
        this.cameraGallerySamsung = new int[]{0, 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 32, 36, 40, 44, 47, 50, 53, 56, 59, 62, 65, 68, 71, 74, 77, 80, 83, 85, 87, 89, 91, 94, 97, 100, 102, 104, 106, 108, 110, HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE, HdmiCecKeycode.CEC_KEYCODE_F2_RED, HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW, HdmiCecKeycode.CEC_KEYCODE_DATA, TEN_BITS_BRIGHT_MAX_RATE, 122, 124, 126, 128, 130, 132, 134, 136, 138, 140, 142, 144, 146, 147, 148, 149, DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_PLUS, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_NEUTRAL, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_MINUS, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, 196, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 205, 205, 206, 207, 207, 208, 209, 209, 210, 210, 211, 211, InputManagerService.SW_JACK_BITS, InputManagerService.SW_JACK_BITS, 213, 213, 214, 214, 215, 215, 216, 216, 217, 217, 218, 218, 219, 219, UsbDescriptor.CLASSID_DIAGNOSTIC, UsbDescriptor.CLASSID_DIAGNOSTIC, UsbDescriptor.CLASSID_DIAGNOSTIC, 221, 221, 222, 222, 222, 223, 223, UsbDescriptor.CLASSID_WIRELESS, UsbDescriptor.CLASSID_WIRELESS, UsbDescriptor.CLASSID_WIRELESS, 225, 225, 226, 226, 226, 227, 227, 228, 228, 228, 229, 229, 230, 230, 230, 231, 231, 232, 232, 232, 233, 233, 234, 234, 234, 235, 235, 236, 236, 236, 237, 237, 238, 238, 238, UsbDescriptor.CLASSID_MISC, UsbDescriptor.CLASSID_MISC, 240, 240, 240, 241, 241, 241, 242, 242, 242, 243, 243, 243, 244, 244, 244, 245, 245, 245, 246, 246, 246, 247, 247, 247, 247, 248, 248, 248, 249, 249, 249, 249, 250, 250, 250, 250, 251, 251, 251, 251, 252, 252, 252, 252, 253, 253, 253, 253, UsbDescriptor.CLASSID_APPSPECIFIC, UsbDescriptor.CLASSID_APPSPECIFIC, UsbDescriptor.CLASSID_APPSPECIFIC, UsbDescriptor.CLASSID_APPSPECIFIC, UsbDescriptor.CLASSID_APPSPECIFIC, 255, 255, 255, 255, 255, 255};
        this.mHandler = new Handler() {
            /* class com.android.server.display.OppoBrightUtils.AnonymousClass2 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    OppoBrightUtils.this.adjustAdStatus(msg.arg1);
                } else if (i == 2) {
                    long time = System.currentTimeMillis() - OppoBrightUtils.this.mAppDurationTime;
                    if (!OppoBrightUtils.this.mLastAppProcessName.equals("com.oppo.launcher") || time >= 60000) {
                        if (OppoBrightUtils.this.mReportMap == null) {
                            HashMap unused = OppoBrightUtils.this.mReportMap = new HashMap();
                        }
                        OppoBrightUtils.this.mReportMap.put("AppBrightness", String.valueOf(OppoLightsService.mScreenBrightness));
                        if (OppoBrightUtils.this.isRegiterLightSensor) {
                            OppoBrightUtils.this.mReportMap.put("AmbientLux", String.valueOf(OppoBrightUtils.this.mManulAmbientLuxValue));
                        } else {
                            OppoBrightUtils.this.mReportMap.put("AmbientLux", String.valueOf(OppoBrightUtils.mAutoAmbientLuxValue));
                        }
                        OppoBrightUtils.this.mReportMap.put("AppDurationTime", OppoBrightUtils.this.getDurationTime(time));
                        OppoBrightUtils.this.mReportMap.put("AppProcess", OppoBrightUtils.this.mLastAppProcessName);
                        OppoBrightUtils.this.mReportMap.put("SystemTime", OppoBrightUtils.this.getSystemTime());
                        OppoBrightUtils oppoBrightUtils = OppoBrightUtils.this;
                        oppoBrightUtils.autoBackLightReport("20180005", oppoBrightUtils.mReportMap);
                        long unused2 = OppoBrightUtils.this.mAppDurationTime = System.currentTimeMillis();
                        OppoBrightUtils oppoBrightUtils2 = OppoBrightUtils.this;
                        String unused3 = oppoBrightUtils2.mLastAppProcessName = oppoBrightUtils2.mCurrentAppProcessName;
                        OppoBrightUtils.this.mReportMap.clear();
                        return;
                    }
                    OppoBrightUtils oppoBrightUtils3 = OppoBrightUtils.this;
                    String unused4 = oppoBrightUtils3.mLastAppProcessName = oppoBrightUtils3.mCurrentAppProcessName;
                } else if (i == 3) {
                    OppoBrightUtils.this.unregisterReportSensor();
                }
            }
        };
        this.mLightSensorListener = new SensorEventListener() {
            /* class com.android.server.display.OppoBrightUtils.AnonymousClass4 */

            public void onSensorChanged(SensorEvent event) {
                if (OppoBrightUtils.this.mLightSensorAlwaysOn) {
                    float lux = event.values[0];
                    if (OppoBrightUtils.this.mAdStatus == 1 && lux >= OppoBrightUtils.ENABLE_AD_LUX) {
                        return;
                    }
                    if (OppoBrightUtils.this.mAdStatus != 0 || lux >= OppoBrightUtils.ENABLE_AD_LUX) {
                        OppoBrightUtils.this.mHandler.removeMessages(1);
                        Message msg = OppoBrightUtils.this.mHandler.obtainMessage(1);
                        if (lux >= OppoBrightUtils.ENABLE_AD_LUX) {
                            int unused = OppoBrightUtils.this.mAdStatus = 1;
                            msg.arg1 = OppoBrightUtils.this.mAdStatus;
                        } else {
                            int unused2 = OppoBrightUtils.this.mAdStatus = 0;
                            msg.arg1 = OppoBrightUtils.this.mAdStatus;
                        }
                        if (OppoBrightUtils.this.mAdStatus != 2 && OppoBrightUtils.this.mAdStatus != OppoBrightUtils.this.mAdStatus_before) {
                            OppoBrightUtils.this.mHandler.sendMessageDelayed(msg, 4000);
                            OppoBrightUtils oppoBrightUtils = OppoBrightUtils.this;
                            int unused3 = oppoBrightUtils.mAdStatus_before = oppoBrightUtils.mAdStatus;
                        }
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mSceneHandler = new Handler() {
            /* class com.android.server.display.OppoBrightUtils.AnonymousClass5 */

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    OppoBrightUtils.mSampleScene = 0;
                    Slog.d(OppoBrightUtils.TAG, "reset to SPECIAL_SCENE_NORMAL");
                }
            }
        };
        this.mReportLightSensorListener = new SensorEventListener() {
            /* class com.android.server.display.OppoBrightUtils.AnonymousClass6 */

            public void onSensorChanged(SensorEvent event) {
                float unused = OppoBrightUtils.this.mManulAmbientLuxValue = event.values[0];
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void init(Context context) {
        this.mContext = context;
        if ("user".equals(SystemProperties.get("ro.build.type", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN))) {
            DEBUG = false;
        } else {
            DEBUG = SystemProperties.getBoolean(DEBUG_PRO, true);
        }
        REPORT_FEATURE_ON = SystemProperties.getBoolean(REPORT_PROPERITY, true);
        initHbmLevels();
    }

    public void initBrightnessCallback(Context context) {
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).setOppoBrightnessCallback(this);
    }

    public static void initHbmLevels() {
        sHbmLevels.clear();
        sHbmLevels.add(Float.valueOf(20000.0f));
        sHbmLevels.add(Float.valueOf(30000.0f));
        sHbmLevels.add(Float.valueOf(40000.0f));
        sHbmLevels.add(Float.valueOf(60000.0f));
        sHbmLevels.add(Float.valueOf(100000.0f));
        initWatchPointTotalTimes();
    }

    public static void initWatchPointTotalTimes() {
        sManuLuxWatchPointTotalTimes.clear();
        sAutoLuxWatchPointTotalTimes.clear();
        for (int i = 0; i < sHbmLevels.size(); i++) {
            sManuLuxWatchPointTotalTimes.add(0L);
            sAutoLuxWatchPointTotalTimes.add(0L);
        }
    }

    public void initParams() {
        readFeatureProperty();
        isSpecialSensor();
        getScreenAutoBrightnessConfig();
        configAutoBrightness();
    }

    public void getScreenAutoBrightnessConfig() {
        String config_string_from_property;
        String[] brightness_string_config;
        int config_length;
        String lcd_manufacture = getDeviceManufacture("lcd").toLowerCase(Locale.US);
        if (lcd_manufacture.contains(LCD_MANUFACTURE_BOE)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_BOE, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TRULY)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TIANMA)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG_TENBITS)) {
            config_string_from_property = "16,155,8,41,65,117,196,312,478,596,842,1023,1086,1135,1255,1337,2047";
            mBrightnessBitsConfig = 2;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_JDI)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_JDI, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_DSJM)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_BOE_TENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_BOE_TENBITS, "");
            mBrightnessBitsConfig = 2;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_NT_JDI_ELEVENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_NT_JDI_ELEVENBITS, "");
            mBrightnessBitsConfig = 3;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_NT_TIANMA_ELEVENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_NT_TIANMA_ELEVENBITS, "");
            mBrightnessBitsConfig = 3;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_HX_JDI_ELEVENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_HX_JDI_ELEVENBITS, "");
        } else {
            config_string_from_property = null;
        }
        this.mBrightnessConfig = config_string_from_property;
        Slog.i(TAG, "config_string_from_property : " + config_string_from_property + "  lcd_manufacture = " + lcd_manufacture);
        if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            SystemProperties.set(BRIGHTNESS_MODE, "0");
        } else {
            SystemProperties.set(BRIGHTNESS_MODE, "1");
        }
        Slog.i(TAG, "mOutdoorBrightnessSupport = " + mOutdoorBrightnessSupport + "  manufacture = " + lcd_manufacture + "   curve mode = " + SystemProperties.get(BRIGHTNESS_MODE));
        StringBuilder sb = new StringBuilder();
        sb.append("config_string_from_property : ");
        sb.append(config_string_from_property);
        Slog.i(TAG, sb.toString());
        if (!(config_string_from_property == null || (brightness_string_config = config_string_from_property.trim().split(",")) == null || brightness_string_config.length <= 2)) {
            try {
                config_length = Integer.valueOf(brightness_string_config[0]).intValue();
            } catch (NumberFormatException e) {
                Slog.e(TAG, "config_length NumberFormatException:" + e.getMessage());
                config_length = -1;
            }
            BRIGHTNESS_STEPS = config_length - 1;
            if (mBrightnessBitsConfig != 2 && config_length >= 10) {
                mBrightnessBitsConfig = 3;
            }
            if (config_length + 1 != brightness_string_config.length) {
                mDefaultBrightness = -1;
            } else {
                try {
                    mDefaultBrightness = Integer.valueOf(brightness_string_config[1]).intValue();
                    Slog.i(TAG, "mDefaultBrightness : " + mDefaultBrightness);
                } catch (NumberFormatException e2) {
                    Slog.e(TAG, "mDefaultBrightness NumberFormatException:" + e2.getMessage());
                    mDefaultBrightness = -1;
                }
            }
            if (mDefaultBrightness == -1) {
                mDefaultBrightness = this.mContext.getResources().getInteger(17694887);
            }
            if (config_length + 1 != brightness_string_config.length) {
                brighness_int_config = null;
            } else {
                brighness_int_config = new int[(config_length - 1)];
                int i = 0;
                while (i < config_length - 1) {
                    try {
                        brighness_int_config[i] = Integer.valueOf(brightness_string_config[i + 2]).intValue();
                        Slog.i(TAG, "brighness_int_config : " + brighness_int_config[i]);
                        i++;
                    } catch (NumberFormatException e3) {
                        Slog.e(TAG, "brighness_int_config NumberFormatException:" + e3.getMessage());
                        brighness_int_config = null;
                    }
                }
            }
        }
        if (brighness_int_config == null) {
            brighness_int_config = this.mContext.getResources().getIntArray(17235990);
            int j = 0;
            while (true) {
                int[] iArr = brighness_int_config;
                if (j < iArr.length) {
                    Slog.i(TAG, "brighness_int_config : " + brighness_int_config[j]);
                    j++;
                } else {
                    BRIGHTNESS_STEPS = iArr.length;
                    mDefaultBrightness = this.mContext.getResources().getInteger(17694887);
                    Slog.d(TAG, "BRIGHTNESS_STEPS = " + BRIGHTNESS_STEPS + " mDefaultBrightness = " + mDefaultBrightness);
                    return;
                }
            }
        }
    }

    @Override // com.android.server.display.IOppoBrightness
    public int getMinimumScreenBrightnessSetting() {
        int i = mBrightnessBitsConfig;
        if (i == 3) {
            mMinBrightness = this.mContext.getResources().getInteger(17694808);
        } else if (i == 2) {
            mMinBrightness = this.mContext.getResources().getInteger(17694900);
        } else {
            mMinBrightness = this.mContext.getResources().getInteger(17694889);
        }
        return mMinBrightness;
    }

    @Override // com.android.server.display.IOppoBrightness
    public int getMaximumScreenBrightnessSetting() {
        int i = mBrightnessBitsConfig;
        if (i == 3) {
            mMaxBrightness = this.mContext.getResources().getInteger(17694807);
        } else if (i == 2) {
            mMaxBrightness = this.mContext.getResources().getInteger(17694899);
        } else {
            mMaxBrightness = this.mContext.getResources().getInteger(17694888);
        }
        return mMaxBrightness;
    }

    @Override // com.android.server.display.IOppoBrightness
    public int getDefaultScreenBrightnessSetting() {
        return mDefaultBrightness;
    }

    public void readFeatureProperty() {
        String version = LCD_DEVICE_VERSION.toLowerCase(Locale.US);
        String manufacture = LCD_DEVICE_MANUFACTURE.toLowerCase(Locale.US);
        this.mFeatures.clear();
        mHighBrightnessModeSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.high.brightness.support");
        Slog.i(TAG, "mHighBrightnessModeSupport = " + mHighBrightnessModeSupport);
        if (mHighBrightnessModeSupport) {
            this.mFeatures.add("mHighBrightnessModeSupport");
        }
        mBrightnessLowLevelScreenSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.brightness.lowlevel.screen");
        Slog.i(TAG, "mBrightnessLowLevelScreenSupport = " + mBrightnessLowLevelScreenSupport);
        if (mBrightnessLowLevelScreenSupport) {
            this.mFeatures.add("mBrightnessLowLevelScreenSupport");
        }
        mOutdoorBrightnessSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.outdoor.brightness.support");
        if (mOutdoorBrightnessSupport && version.contains(LCD_DEVICE_VERSION_TD4310) && (manufacture.contains(LCD_MANUFACTURE_BOE) || manufacture.contains(LCD_MANUFACTURE_JDI))) {
            mOutdoorBrightnessSupport = false;
        }
        if (mOutdoorBrightnessSupport) {
            this.mFeatures.add("mOutdoorBrightnessSupport");
        }
        if (manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            SystemProperties.set(BRIGHTNESS_MODE, "0");
        } else {
            SystemProperties.set(BRIGHTNESS_MODE, "1");
        }
        Slog.i(TAG, "mOutdoorBrightnessSupport = " + mOutdoorBrightnessSupport + "  manufacture = " + manufacture + "   curve mode = " + SystemProperties.get(BRIGHTNESS_MODE));
        mScreenGlobalHBMSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.display.screen.gloablehbm.support");
        StringBuilder sb = new StringBuilder();
        sb.append("mScreenGlobalHBMSupport = ");
        sb.append(mScreenGlobalHBMSupport);
        Slog.i(TAG, sb.toString());
        if (mScreenGlobalHBMSupport) {
            this.mFeatures.add("sScreenGlobalHBMSupport");
        }
        mFingerprintOpticalSupport = this.mContext.getPackageManager().hasSystemFeature(FingerprintService.OPTICAL_FINGERPRINT_FEATURE);
        Slog.i(TAG, "mFingerprintOpticalSupport = " + mFingerprintOpticalSupport);
        if (mFingerprintOpticalSupport) {
            this.mFeatures.add("sHardwareFingerprint");
        }
        mScreenUnderLightSensorSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.display.screen.underlightsensor.support");
        Slog.i(TAG, "mScreenUnderLightSensorSupport = " + mScreenUnderLightSensorSupport);
        if (mScreenUnderLightSensorSupport) {
            this.mFeatures.add("sUnderLightsensor");
        }
        sUseRotateSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.autolight.userotate.support");
        sPSensorNotSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.autolight.psensor.notsupport");
    }

    public void isSpecialSensor() {
        this.mSpecialLightSensor = false;
        getSensorAndSfPrepared();
        String lightsensor_version = getDeviceVersion().toLowerCase(Locale.US);
        if (lightsensor_version != null) {
            if (lightsensor_version.contains(this.SPECIALIGHTSENSOR) || lightsensor_version.contains(this.SPECIALIGHTSENSOR1)) {
                this.mSpecialLightSensor = true;
            }
            if (lightsensor_version.contains(this.SPECIAL_LIGHT_SENSOR2) || lightsensor_version.contains(this.SPECIAL_LIGHT_SENSOR3) || lightsensor_version.contains(this.SPECIAL_LIGHT_SENSOR4) || lightsensor_version.contains(this.SPECIAL_LIGHT_SENSOR5)) {
                this.mStkCmLightSensor = true;
            }
        }
        Slog.i(TAG, "mSpecialLightSensor = " + this.mSpecialLightSensor + " mStkCmLightSensor = " + this.mStkCmLightSensor);
    }

    private void getSensorAndSfPrepared() {
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mFlinger = ServiceManager.getService("SurfaceFlinger");
    }

    public void configAutoBrightness() {
        Slog.i(TAG, "mBrightnessBitsConfig = " + mBrightnessBitsConfig);
        int i = mBrightnessBitsConfig;
        if (3 == i) {
            BRIGHTNESS_RAMP_RATE_FAST = 1600;
            BRIGHTNESS_RAMP_RATE_SCREENON = 960;
            BRIGHTNESS_RAMP_RATE_SLOW = Vr2dDisplay.DEFAULT_VIRTUAL_DISPLAY_DPI;
            int i2 = BRIGHTNESS_STEPS;
            mAutoBrightnessLux = new float[i2];
            mAutoBrightnessLuxMinLimit = new float[i2];
            mAutoBrightnessLuxMaxLimit = new float[i2];
            SPECIAL_SCENE_STREET_LAMP_LUX = 10;
        } else if (2 == i) {
            BRIGHTNESS_RAMP_RATE_FAST = 800;
            BRIGHTNESS_RAMP_RATE_SCREENON = SystemService.PHASE_LOCK_SETTINGS_READY;
            BRIGHTNESS_RAMP_RATE_SLOW = 160;
            int i3 = BRIGHTNESS_STEPS;
            mAutoBrightnessLux = new float[i3];
            mAutoBrightnessLuxMinLimit = new float[i3];
            mAutoBrightnessLuxMaxLimit = new float[i3];
        } else {
            BRIGHTNESS_RAMP_RATE_FAST = 200;
            BRIGHTNESS_RAMP_RATE_SCREENON = TEN_BITS_BRIGHT_MAX_RATE;
            BRIGHTNESS_RAMP_RATE_SLOW = 40;
            int i4 = BRIGHTNESS_STEPS;
            mAutoBrightnessLux = new float[i4];
            mAutoBrightnessLuxMinLimit = new float[i4];
            mAutoBrightnessLuxMaxLimit = new float[i4];
        }
    }

    public int[] readAutoBrightnessLuxConfig() {
        int min;
        Resources resources = this.mContext.getResources();
        Slog.w(TAG, "mBrightnessBitsConfig= " + mBrightnessBitsConfig + " ,BRIGHTNESS_STEPS=" + BRIGHTNESS_STEPS);
        int i = mBrightnessBitsConfig;
        if (3 == i) {
            resources.getIntArray(17236024);
            resources.getIntArray(17236026);
            resources.getIntArray(17236025);
        } else if (2 == i) {
            resources.getIntArray(17236076);
            resources.getIntArray(17236078);
            resources.getIntArray(17236077);
        } else {
            resources.getIntArray(17235991);
            resources.getIntArray(17235993);
            resources.getIntArray(17235992);
        }
        int[] lux = {12, 18, 30, 360, 1200, 2250, 4600, 10000, 20000, 30000, EventLogTags.VOLUME_CHANGED, SPECIAL_AMBIENT_LIGHT_HORIZON, 80000, 100000};
        int[] minLimit = {0, 5, 10, 18, 30, 300, 1000, 2000, 4000, EventLogTags.JOB_DEFERRED_EXECUTION, 18000, 25000, 30000, EventLogTags.VOLUME_CHANGED, 75000};
        int[] maxLimit = {9, 17, 30, 50, CompatibilityHelper.FORCE_DELAY_TO_USE_POST, 1600, 2940, 5900, ProcessList.PSS_MIN_TIME_FROM_STATE_CHANGE, 25000, EventLogTags.AUTO_BRIGHTNESS_ADJ, 45000, com.android.server.policy.EventLogTags.SCREEN_TOGGLED, 90000, 120000};
        if (this.mSpecialLightSensor && 1 == mBrightnessBitsConfig) {
            maxLimit[0] = 2;
            maxLimit[1] = 110;
            minLimit[2] = 2;
        } else if (this.mSpecialLightSensor && 3 == mBrightnessBitsConfig) {
            maxLimit[0] = 2;
            minLimit[2] = 2;
        }
        if (this.mStkCmLightSensor && 1 == mBrightnessBitsConfig) {
            lux[1] = 780;
            lux[2] = 1300;
            maxLimit[0] = 4;
            maxLimit[1] = 110;
        } else if (this.mStkCmLightSensor && 3 == mBrightnessBitsConfig) {
            lux[2] = 60;
            lux[3] = 200;
            lux[4] = 800;
            lux[5] = 1400;
            minLimit[4] = 50;
            minLimit[5] = 150;
            minLimit[6] = 600;
            maxLimit[3] = 100;
            maxLimit[4] = 500;
            maxLimit[5] = 1200;
        }
        int min2 = lux.length < minLimit.length ? lux.length : minLimit.length;
        int length = min2 < maxLimit.length ? min2 : maxLimit.length;
        int i2 = BRIGHTNESS_STEPS;
        if (i2 == minLimit.length && i2 == maxLimit.length) {
            min = BRIGHTNESS_STEPS;
        } else {
            min = 7;
            Slog.e(TAG, "please check check check overlay config file...");
        }
        for (int i3 = 0; i3 < min; i3++) {
            if (i3 == 0) {
                mAutoBrightnessLux[i3] = 0.0f;
            } else {
                mAutoBrightnessLux[i3] = (float) lux[i3 - 1];
            }
            mAutoBrightnessLuxMinLimit[i3] = (float) minLimit[i3];
            mAutoBrightnessLuxMaxLimit[i3] = (float) maxLimit[i3];
            Slog.w(TAG, "mAutoBrightnessLux[" + i3 + "] = " + mAutoBrightnessLux[i3] + " mAutoBrightnessLuxMinLimit[" + i3 + "] = " + mAutoBrightnessLuxMinLimit[i3] + " mAutoBrightnessLuxMaxLimit[" + i3 + "] = " + mAutoBrightnessLuxMaxLimit[i3]);
        }
        return lux;
    }

    public int[] readAutoBrightnessConfig() {
        if (brighness_int_config == null) {
            Slog.w(TAG, "brighness_int_config is null.");
        }
        return brighness_int_config;
    }

    public int getSlowAnimationBrightness() {
        int i = mBrightnessBitsConfig;
        if (3 == i) {
            return brighness_int_config[1];
        }
        if (2 == i) {
            return brighness_int_config[1];
        }
        if (1 == i) {
            return brighness_int_config[1];
        }
        return brighness_int_config[1];
    }

    public float findAmbientLux(int area) {
        float lux = MIN_LUX_LIMITI;
        int k = 0;
        while (true) {
            if (k >= BRIGHTNESS_STEPS) {
                break;
            } else if (area == k) {
                lux = mAutoBrightnessLux[k];
                break;
            } else {
                k++;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "area=" + area + ", lux=" + lux);
        }
        return lux;
    }

    public int getBrightness(int area, float ambientLux) {
        if (DEBUG) {
            Slog.d(TAG, "area=" + area + ", ambientLux=" + ambientLux);
        }
        int tempBrightness = brighness_int_config[0];
        if (area < 0) {
            area = resetAmbientLux(ambientLux) - 1;
        }
        int i = BRIGHTNESS_STEPS;
        if (area >= i) {
            area = i - 1;
        } else if (area < 0) {
            area = 0;
            Slog.d(TAG, "this sequence maybe error-1 ambientLux=" + ambientLux + " area=" + 0, new Throwable());
        }
        try {
            if (mAutoBrightnessLux[area] == ambientLux) {
                tempBrightness = brighness_int_config[area];
            } else {
                area = resetAmbientLux(ambientLux) - 1;
                if (area >= BRIGHTNESS_STEPS) {
                    area = BRIGHTNESS_STEPS - 1;
                } else if (area < 0) {
                    area = 0;
                    Slog.d(TAG, "this sequence maybe error-2 ambientLux=" + ambientLux + " area=" + 0, new Throwable());
                }
                tempBrightness = brighness_int_config[area];
            }
        } catch (Exception e) {
            Slog.d(TAG, "Exception area=" + area + ", tempBrightness=" + tempBrightness);
        }
        if (!mScreenGlobalHBMSupport && tempBrightness > mMaxBrightness) {
            tempBrightness = mMaxBrightness;
        }
        if (DEBUG) {
            Slog.d(TAG, "area=" + area + ", tempBrightness=" + tempBrightness);
        }
        return tempBrightness;
    }

    public int resetAmbientLux(float ambientLux) {
        for (int k = 0; k < BRIGHTNESS_STEPS; k++) {
            if (Math.round(ambientLux) <= Math.round(mAutoBrightnessLux[k])) {
                return k + 1;
            }
            if (Math.round(ambientLux) > Math.round(mAutoBrightnessLux[BRIGHTNESS_STEPS - 1])) {
                return BRIGHTNESS_STEPS;
            }
        }
        return 0;
    }

    public static void setHighbrightnessDebounceValue(float level1, float level2, float level3) {
        Slog.d(TAG, "RUS update HBM level1=" + level1 + ",level2=" + level2 + "level3=" + level3);
    }

    public void registerGHBMContent() {
        ContentResolver resolver = this.mContext.getContentResolver();
        mScreenGlobalHBMSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.display.screen.gloablehbm.support");
        if (mScreenGlobalHBMSupport) {
            Settings.Secure.putInt(resolver, OPPO_GHBM_MODE, 1);
        } else {
            Settings.Secure.putInt(resolver, OPPO_GHBM_MODE, 0);
        }
        if (mScreenGlobalHBMSupport) {
            resolver.registerContentObserver(Settings.Secure.getUriFor(OPPO_GHBM_MODE), false, this.cob);
            if (DEBUG) {
                Slog.d(TAG, "registerContentObserver for engineer mode");
            }
        }
    }

    public static void setGloableHBMFeature(boolean on) {
        mScreenGlobalHBMSupport = on;
        Slog.d(TAG, "GloableHBM=" + mScreenGlobalHBMSupport);
    }

    public void writeHighbrightnessNodeValue(int value) {
        writeFileNodeValue(HIGH_BRIGHTNESS_NODE, value);
    }

    public void writeFileNodeValue(String str, int value) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(str));
            writer.write(String.valueOf(value));
            try {
                writer.close();
            } catch (Exception e) {
                Slog.d(TAG, "writeFileNodeValue io stream close wrong");
            }
        } catch (Exception e2) {
            Slog.d(TAG, "writeFileNodeValue sorry write wrong");
            e2.printStackTrace();
            if (writer != null) {
                writer.close();
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e3) {
                    Slog.d(TAG, "writeFileNodeValue io stream close wrong");
                }
            }
            throw th;
        }
    }

    public int caclurateRateForIndex(float lux, int originBrightness, int targetBrightness) {
        int i = mBrightnessBitsConfig;
        if (3 == i) {
            if (targetBrightness >= originBrightness) {
                int rate = Math.round(((Math.abs(lux) * 150.0f) + 809850.0f) / 9000.0f);
                if (targetBrightness <= brighness_int_config[3]) {
                    rate = EIGHT_BITS_BRIGHT_MAX_RATE;
                }
                int[] iArr = brighness_int_config;
                if (originBrightness <= iArr[3] && targetBrightness >= iArr[4]) {
                    rate = Math.round(((Math.abs(lux) * 100.0f) + 2699900.0f) / 9000.0f);
                }
                int[] iArr2 = brighness_int_config;
                if (originBrightness >= iArr2[4] && targetBrightness >= iArr2[4]) {
                    rate = Math.round(((Math.abs(lux) * 120.0f) + 1079880.0f) / 9000.0f);
                }
                if (!this.mStkCmLightSensor) {
                    return rate;
                }
                int rate2 = rate + rate;
                if (rate2 > 500) {
                    return 500;
                }
                return rate2;
            } else if (targetBrightness > originBrightness) {
                return 0;
            } else {
                int rate3 = Math.round(((Math.abs(lux) * 80.0f) + 359920.0f) / 9000.0f);
                if (this.mStkCmLightSensor) {
                    rate3 += rate3;
                }
                if (rate3 > TEN_BITS_BRIGHT_MAX_RATE) {
                    return TEN_BITS_BRIGHT_MAX_RATE;
                }
                return rate3;
            }
        } else if (2 != i) {
            return 0;
        } else {
            if (targetBrightness >= originBrightness) {
                return Math.round(((Math.abs(lux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            if (targetBrightness > originBrightness) {
                return 0;
            }
            int rate4 = Math.round(((Math.abs(lux) * 96.0f) + 215880.0f) / 8999.0f);
            if (this.mStkCmLightSensor) {
                rate4 += rate4;
            }
            if (rate4 > TEN_BITS_BRIGHT_MAX_RATE) {
                return TEN_BITS_BRIGHT_MAX_RATE;
            }
            return rate4;
        }
    }

    public int caclurateRate(float nowLux, float lastLux) {
        int i = mBrightnessBitsConfig;
        if (3 == i) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            int rate = Math.round((((lastLux - nowLux) * 100.0f) + 449850.0f) / 8999.0f);
            if (rate > TEN_BITS_BRIGHT_MAX_RATE) {
                return TEN_BITS_BRIGHT_MAX_RATE;
            }
            return rate;
        } else if (2 == i) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            return Math.round((((lastLux - nowLux) * 30.0f) + 134955.0f) / 8999.0f);
        } else if (nowLux > lastLux) {
            int rate2 = Math.round((((nowLux - lastLux) * 20.0f) + 49979.0f) / 4999.0f);
            if (!this.mStkCmLightSensor) {
                return rate2;
            }
            int rate3 = rate2 + rate2;
            if (rate3 > EIGHT_BITS_BRIGHT_MAX_RATE) {
                return EIGHT_BITS_BRIGHT_MAX_RATE;
            }
            return rate3;
        } else {
            int rate4 = Math.round((((lastLux - nowLux) * 14.0f) + 29980.0f) / 4999.0f);
            if (this.mStkCmLightSensor) {
                rate4 += rate4;
            }
            if (rate4 > 30) {
                return 30;
            }
            return rate4;
        }
    }

    public int findNightBrightness(boolean stateon, int brightness) {
        if (stateon) {
            this.mCalendar = Calendar.getInstance();
            Calendar calendar = this.mCalendar;
            if (calendar != null) {
                int hour = calendar.get(11);
                int i = mBrightnessBitsConfig;
                if (1 == i) {
                    if (hour <= 19 && hour >= 7) {
                        this.mNightBrihtness = 0;
                    } else if (brightness == brighness_int_config[0]) {
                        this.mNightBrihtness = 3;
                    }
                } else if (3 == i && this.mSpecialLightSensor) {
                    if (hour <= 6 || hour >= 19) {
                        this.mNightBrihtness = 0;
                    } else {
                        int[] iArr = brighness_int_config;
                        if (brightness == iArr[0]) {
                            this.mNightBrihtness = iArr[1] / 2;
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "brightness=" + brightness + " ,mNightBrihtness=" + this.mNightBrihtness);
        }
        if (this.mNightBrihtness <= 0 || brightness != brighness_int_config[0]) {
            return brightness;
        }
        return this.mNightBrihtness;
    }

    @Override // com.android.server.display.IOppoBrightness
    public int changeMinBrightness(int brightness) {
        Calendar calendar;
        int hour;
        int i;
        if (mBrightnessBitsConfig != 1 || (calendar = Calendar.getInstance()) == null || (hour = calendar.get(11)) <= 6 || hour >= 18 || brightness != (i = mMinBrightness)) {
            return brightness;
        }
        return i + 1;
    }

    @Override // com.android.server.display.IOppoBrightness
    public int adjustInverseModeBrightness(int brightness) {
        int mLowestBrightnessForInverseMode = (int) (((float) mMaxBrightness) * LOW_PERCENT_BRIGHTNESS);
        if (brightness < mLowestBrightnessForInverseMode) {
            Slog.d(TAG, "adjustInverseModeBrightness:" + mLowestBrightnessForInverseMode);
            return mLowestBrightnessForInverseMode;
        }
        Slog.d(TAG, "adjustInverseModeBrightness:" + brightness);
        return brightness;
    }

    public int adjustCameraBrightness(int brightness) {
        if (1 != mBrightnessBitsConfig) {
            int maxBrightness = getMaximumScreenBrightnessSetting();
            if (!mScreenGlobalHBMSupport || brightness <= maxBrightness) {
                float uniform_brightness = ((float) brightness) / ((float) maxBrightness);
                float adjGamma = 0.6f;
                if (uniform_brightness > 0.196f) {
                    adjGamma = 0.4f;
                }
                int adjust_brightness = Math.round(((float) maxBrightness) * MathUtils.pow(uniform_brightness, adjGamma));
                Slog.d(TAG, "adjust_brightness = " + adjust_brightness);
                return adjust_brightness;
            }
            Slog.d(TAG, "adjust_brightness = " + brightness);
            return brightness;
        } else if (brightness >= 256 || brightness <= 0) {
            return 128;
        } else {
            return this.cameraGallerySamsung[brightness];
        }
    }

    private String getMtkDeviceVersion() {
        String[] lineSplit;
        File file = new File("/proc/devinfo/Sensor_alsps");
        String version = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + "/proc/devinfo/Sensor_alsps" + " is not exist...");
            return version;
        } else if (!file.canRead()) {
            Slog.e(TAG, "No permission to read " + "/proc/devinfo/Sensor_alsps");
            return version;
        } else {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION) && (lineSplit = line.split(":")) != null && lineSplit.length > 1 && (version = lineSplit[1].trim()) != null) {
                            Slog.i(TAG, KEY_DEVICE_VERSION + version);
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.i(TAG, "bufferedReader.close IOException caught");
                        }
                    }
                }
                bufferedReader2.close();
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Slog.i(TAG, "IOException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                throw th;
            }
            return version;
        }
    }

    private String getQualcommDeviceVersion() {
        String type = DEFAULT_MANUFACTURE;
        List<Sensor> sensors = this.mSensorManager.getSensorList(8);
        if (sensors == null) {
            return type;
        }
        for (int i = 0; i < sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            if (sensor.getType() == 8) {
                String[] splitSensorName = sensor.getName().split(StringUtils.SPACE);
                String sensorVersion = sensor.getName();
                if (splitSensorName.length > 1) {
                    sensorVersion = splitSensorName[0];
                }
                Slog.d(TAG, "sensorVersion : " + sensorVersion);
                type = sensorVersion;
                String[] sensorMode = sensor.getName().split("-");
                if (sensorMode.length > 1) {
                    type = sensorMode[0];
                }
            }
        }
        return type;
    }

    private String getDeviceVersion() {
        mPlatformQualcomm = this.mContext.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
        mPlatformMtk = this.mContext.getPackageManager().hasSystemFeature(PLATFORM_MTK);
        Slog.i(TAG, "mPlatformQualcomm = " + mPlatformQualcomm + " mPlatformMtk " + mPlatformMtk);
        if (mPlatformQualcomm) {
            return getQualcommDeviceVersion();
        }
        if (mPlatformMtk) {
            return getMtkDeviceVersion();
        }
        return DEFAULT_MANUFACTURE;
    }

    public String getDeviceManufacture(String deviceName) {
        String[] lineSplit;
        String[] lineSplit2;
        String filePath = "/proc/devinfo/" + deviceName;
        File file = new File(filePath);
        String manufacture = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + filePath + " is not exist...");
            return DEFAULT_MANUFACTURE;
        } else if (!file.canRead()) {
            Slog.e(TAG, "No permission to read " + filePath);
            return DEFAULT_MANUFACTURE;
        } else {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION) && (lineSplit2 = line.split(":")) != null && lineSplit2.length > 1 && (manufacture = lineSplit2[1].trim()) != null) {
                            Slog.i(TAG, "version : " + manufacture);
                            LCD_DEVICE_VERSION = manufacture;
                        }
                        if (line.contains(KEY_DEVICE_MANUFACTURE) && (lineSplit = line.split(":")) != null && lineSplit.length > 1 && (manufacture = lineSplit[1].trim()) != null) {
                            Slog.i(TAG, "manufacture : " + manufacture);
                            LCD_DEVICE_MANUFACTURE = manufacture;
                            break;
                        }
                    }
                }
                try {
                    bufferedReader2.close();
                    break;
                } catch (IOException e) {
                    Slog.i(TAG, "bufferedReader.close IOException caught");
                }
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Slog.i(TAG, "IOException caught");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                throw th;
            }
            if (manufacture == null) {
                return DEFAULT_MANUFACTURE;
            }
            return manufacture;
        }
    }

    public boolean isAgingTestVersion() {
        return "1".equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
    }

    public int getBootupBrightness() {
        int i = this.mBootupBrightVaule;
        int brightvalue = (this.mBootupBrightVaule * (mMaxBrightness + 1)) / 256;
        if (mBrightnessBitsConfig == 2) {
            brightvalue /= 2;
        }
        this.mBootBrightConfig = brightvalue;
        return brightvalue;
    }

    /* access modifiers changed from: private */
    public void adjustAdStatus(int status) {
        if (this.mEnable_before != status) {
            sendCmdToSF(status);
            this.mEnable_before = status;
        }
    }

    private void sendCmdToSF(int cmd) {
        if (cmd == 0 || cmd == 1) {
            try {
                if (this.mFlinger != null) {
                    Slog.d(TAG, "adjust ad: sending to SF, cmd = " + cmd);
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                    data.writeInt(cmd);
                    this.mFlinger.transact(CODE_SEND_AD_STATUS_TO_SF, data, null, 0);
                    data.recycle();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "OppoBrightUtils mFlinger is null!");
                e.printStackTrace();
            }
        }
    }

    public void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorAlwaysOn) {
                this.mLightSensorAlwaysOn = true;
                if (DEBUG) {
                    Slog.d(TAG, "mLightSensorAlwaysOn: True");
                }
                Thread thread = new Thread(new Runnable() {
                    /* class com.android.server.display.OppoBrightUtils.AnonymousClass3 */

                    public void run() {
                        OppoBrightUtils.this.mSensorManager.registerListener(OppoBrightUtils.this.mLightSensorListener, OppoBrightUtils.this.mLightSensor, 500000, OppoBrightUtils.this.mHandler);
                    }
                }, "getLightSensorThread");
                thread.setPriority(10);
                thread.start();
            }
        } else if (this.mLightSensorAlwaysOn) {
            this.mLightSensorAlwaysOn = false;
            if (DEBUG) {
                Slog.d(TAG, "mLightSensorAlwaysOn: False");
            }
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            this.mAdStatus = 2;
        }
    }

    @Override // com.android.server.display.IOppoBrightness
    public int getPhoneState() {
        TelephonyManager manager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (manager != null) {
            return manager.getCallState();
        }
        if (!DEBUG) {
            return 0;
        }
        Slog.d(TAG, "mLightSensorAlwaysOn: False");
        return 0;
    }

    public void writeOutdoorNodeValue(int value) {
        writeFileNodeValue(OUTDOOR_BL_NODE, value);
    }

    public void setHighBrightness(int value) {
        Slog.d(TAG, "setHighBrightness = " + value);
        if (value > 0) {
            mIsOledHighBrightness = true;
        } else {
            mIsOledHighBrightness = false;
        }
        if (mHighBrightnessModeSupport) {
            writeHighbrightnessNodeValue(value);
        } else if (mOutdoorBrightnessSupport) {
            writeOutdoorNodeValue(value);
        }
    }

    public int calDragBrightness(int originBrightness, int manulBrightness, int nowInterval, int manulInterval, int brightness) {
        int lChangeStep = 0;
        int lIntervalGap = Math.abs(nowInterval - manulInterval);
        int lDragBrightness = brightness;
        if (originBrightness > 0) {
            float lManulScale = ((float) Math.abs(manulBrightness - originBrightness)) / ((float) originBrightness);
            float lManulScale2 = 0.3f;
            if (lManulScale <= 0.3f) {
                lManulScale2 = lManulScale;
            }
            if (lIntervalGap <= 3) {
                lChangeStep = Math.round(((float) brightness) * lManulScale2 * (1.0f - (((float) lIntervalGap) / ((float) 4))));
            }
            if (nowInterval > 1) {
                if (manulBrightness > originBrightness) {
                    lDragBrightness += lChangeStep;
                } else if (manulBrightness < originBrightness) {
                    lDragBrightness -= lChangeStep;
                }
            }
            if (mScreenGlobalHBMSupport) {
                lDragBrightness = MathUtils.constrain(lDragBrightness, mMinBrightness, HBM_EXTEND_MAXBRIGHTNESS);
            } else {
                lDragBrightness = MathUtils.constrain(lDragBrightness, mMinBrightness, mMaxBrightness);
            }
        }
        if (DEBUG) {
            Slog.d(TAG, " manul = " + manulInterval + " now = " + nowInterval + " Drag = " + lDragBrightness + " Step = " + lChangeStep);
        }
        return lDragBrightness;
    }

    public void setPocketRingingState(boolean state) {
        mPocketRingingState = state;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isSpecialAdj(float value) {
        int i = mMaxBrightness;
        boolean specialAdj = value == (((float) i) * 500.0f) / 255.0f || value == (((float) i) * 300.0f) / 255.0f || value == (((float) i) * 301.0f) / 255.0f || value == 16384.0f || value == 32768.0f || value == 16385.0f || value == 32769.0f || value == 16386.0f || Math.round(value) == 1930286 || Math.round(value) == 1930287 || value == 32770.0f;
        if (DEBUG) {
            Slog.d(TAG, "isSpecialAdj=" + specialAdj + " value=" + value + " flag=" + mSpecialBrightnessFlag);
        }
        return specialAdj;
    }

    public void resetSampleCount() {
        mSampleCount = 0;
        for (int i = 0; i < 20; i++) {
            this.mSampleTime[i] = 0;
            this.mSampleLux[i] = 0;
        }
        mSampleScene = 0;
        mLampGapTime = 0;
    }

    private double getLux(int index) {
        return (double) this.mSampleLux[index % 20];
    }

    private long getTime(int index) {
        return this.mSampleTime[index % 20];
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        for (int i = 0; i < 20; i++) {
            buf.append("(");
            buf.append(getTime(i));
            buf.append(StringUtils.SPACE);
            buf.append(getLux(i));
            buf.append(")");
            if (i + 1 < 20) {
                buf.append(",");
            }
        }
        buf.append(']');
        return buf.toString();
    }

    private long checkStreetLampScene(int maxindex, int minindex, int newindex) {
        long debounce = mDarkDebounce;
        if (maxindex > 0 && maxindex < newindex && minindex < maxindex && this.mSampleLux[minindex] <= SPECIAL_SCENE_STREET_LAMP_LUX) {
            mTurningPointTime = this.mSampleTime[maxindex + 1];
            long j = mTurningPointTime;
            long j2 = mPreTurningPointTime;
            if (j > j2 && j2 > 0) {
                mLampGapTime = j - j2;
            }
            long j3 = mLampGapTime;
            if (j3 <= 0 || j3 >= 5000) {
                long j4 = mLampGapTime;
                if (j4 > 0 && j4 < 60000) {
                    mSampleScene = 1;
                    debounce = STREET_LAMP_DARK_DEBOUNCE;
                    Slog.d(TAG, "STREET_LAMP dark debounce = " + STREET_LAMP_DARK_DEBOUNCE);
                    this.mSceneHandler.removeMessages(1);
                    Message msg = this.mSceneHandler.obtainMessage(1);
                    msg.arg1 = 1;
                    this.mSceneHandler.sendMessageDelayed(msg, SCENE_RESET_DELAY);
                }
            } else {
                mSampleScene = 2;
                debounce = 10000;
                Slog.d(TAG, "STREET_LAMP_INCAR dark debounce = " + 10000L);
                this.mSceneHandler.removeMessages(1);
                Message msg2 = this.mSceneHandler.obtainMessage(1);
                msg2.arg1 = 1;
                this.mSceneHandler.sendMessageDelayed(msg2, SCENE_RESET_DELAY_INCAR);
            }
            long j5 = mTurningPointTime;
            if (j5 > mPreTurningPointTime) {
                mPreTurningPointTime = j5;
            }
        }
        if (minindex <= maxindex || newindex <= minindex) {
            return debounce;
        }
        int i = mSampleScene;
        if (i == 2) {
            mSampleScene = 2;
            Slog.d(TAG, "update dark debounce = " + 10000L);
            this.mSceneHandler.removeMessages(1);
            Message msg3 = this.mSceneHandler.obtainMessage(1);
            msg3.arg1 = 1;
            this.mSceneHandler.sendMessageDelayed(msg3, SCENE_RESET_DELAY_INCAR);
            return 10000;
        } else if (i != 1) {
            return debounce;
        } else {
            mSampleScene = 1;
            Slog.d(TAG, "update dark debounce = " + STREET_LAMP_DARK_DEBOUNCE);
            this.mSceneHandler.removeMessages(1);
            Message msg4 = this.mSceneHandler.obtainMessage(1);
            msg4.arg1 = 1;
            this.mSceneHandler.sendMessageDelayed(msg4, SCENE_RESET_DELAY);
            return STREET_LAMP_DARK_DEBOUNCE;
        }
    }

    public long checkSpecialScene(long time, int lux) {
        long j = mDarkDebounce;
        int maxIndex = 19;
        int minIndex = 19;
        mMaxSampleLux = lux;
        mMinSampleLux = lux;
        for (int i = 0; i < 19; i++) {
            long[] jArr = this.mSampleTime;
            jArr[i] = jArr[i + 1];
            int[] iArr = this.mSampleLux;
            iArr[i] = iArr[i + 1];
            if (time - jArr[i] > VALID_SAMPLE_TIME_LIMIT) {
                jArr[i] = 0;
                iArr[i] = 0;
            }
            int[] iArr2 = this.mSampleLux;
            if (iArr2[i] >= mMaxSampleLux) {
                mMaxSampleLux = iArr2[i];
                maxIndex = i;
            }
            int[] iArr3 = this.mSampleLux;
            if (iArr3[i] <= mMinSampleLux && this.mSampleTime[i] > 0) {
                mMinSampleLux = iArr3[i];
                minIndex = i;
            }
        }
        this.mSampleTime[19] = time;
        int[] iArr4 = this.mSampleLux;
        iArr4[19] = lux;
        if (iArr4[19] >= iArr4[maxIndex]) {
            maxIndex = 19;
            mMaxSampleLux = iArr4[19];
        }
        Slog.d(TAG, "lux sample = " + toString());
        Slog.d(TAG, "maxindex = " + maxIndex + " minindex = " + minIndex);
        return checkStreetLampScene(maxIndex, minIndex, 19);
    }

    public int getBrightnessBit() {
        return mBrightnessBitsConfig;
    }

    public void initWAArgsHelper() {
        this.mOppoWAArgsHelper = new OppoBrightnessWAArgsHelper(this.mContext);
        this.mOppoWAArgsHelper.initUpdateBroadcastReceiver();
    }

    public boolean isOpen() {
        return this.mOppoWAArgsHelper.isOpen();
    }

    public long getWASleepTime() {
        return this.mOppoWAArgsHelper.getWASleepTime();
    }

    public int getWAManualBrightness() {
        return this.mOppoWAArgsHelper.getWAManualBrightness();
    }

    public int getWASensorLux() {
        return this.mOppoWAArgsHelper.getWASensorLux();
    }

    @Override // com.android.server.display.IOppoBrightness
    public void notifySfRepaintEverything() {
    }

    public void dump(PrintWriter pw) {
        float[] fArr;
        pw.println();
        pw.println("Brightness config:");
        pw.println("  manu: " + LCD_DEVICE_MANUFACTURE);
        StringBuilder sb = new StringBuilder();
        sb.append("  config: ");
        String str = this.mBrightnessConfig;
        if (str == null) {
            str = "NULL";
        }
        sb.append(str);
        pw.println(sb.toString());
        pw.println("  bit:" + mBrightnessBitsConfig);
        pw.println("  defaultBrightness:" + mDefaultBrightness);
        pw.println("  bootBrightness:" + this.mBootBrightConfig);
        pw.println("  dragData(" + mManualBrightness + "," + mManulAtAmbientLux + ")");
        pw.println("  wa_config=[" + isOpen() + "," + getWAManualBrightness() + "," + getWASensorLux() + "," + getWASleepTime() + "]");
        if (brighness_int_config != null) {
            pw.print("  brighness_int_config=");
            for (int j = 0; j < brighness_int_config.length; j++) {
                pw.print(brighness_int_config[j] + StringUtils.SPACE);
            }
            pw.println();
        }
        float[] fArr2 = mAutoBrightnessLux;
        if (fArr2 != null && (fArr = mAutoBrightnessLuxMinLimit) != null && fArr != null) {
            if (fArr2.length == fArr.length && fArr.length == mAutoBrightnessLuxMaxLimit.length) {
                pw.println("  luxConfig:");
                for (int j2 = 0; j2 < mAutoBrightnessLux.length; j2++) {
                    pw.println("  lux[" + j2 + "]=" + mAutoBrightnessLux[j2] + " min[" + j2 + "] = " + mAutoBrightnessLuxMinLimit[j2] + " max[" + j2 + "] = " + mAutoBrightnessLuxMaxLimit[j2]);
                }
                return;
            }
            pw.print("lux config error please check code");
        }
    }

    public void autoBackLightReport(String mEventId, HashMap<String, String> reportMap) {
        OppoStatistics.onCommon(this.mContext, mBacklightReportTag, mEventId, reportMap, false);
        if (DEBUG_REPORT) {
            for (String key : reportMap.keySet()) {
                Slog.d(TAG, "autoBackLightReport:key= " + key + " value= " + reportMap.get(key));
            }
        }
    }

    public String getTopPackageName() {
        ComponentName cn = ((ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY)).getTopAppName();
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    public String getSystemTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }

    public String getDurationTime(long durationTime) {
        long time = durationTime / 1000;
        if (time >= 86400) {
            time = 0;
        }
        return String.valueOf(time);
    }

    public void regiterReportSensor() {
        if (!this.isRegiterLightSensor) {
            this.mReportSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            SensorManager sensorManager = this.mReportSensorManager;
            sensorManager.registerListener(this.mReportLightSensorListener, sensorManager.getDefaultSensor(5), 3);
            this.isRegiterLightSensor = true;
            if (DEBUG_REPORT) {
                Slog.d(TAG, "regiterReportSensor.");
            }
        } else if (DEBUG_REPORT) {
            Slog.d(TAG, "already regiterReportSensor, direct return.");
        }
    }

    public void unregisterReportSensor() {
        if (this.isRegiterLightSensor) {
            this.mReportSensorManager.unregisterListener(this.mReportLightSensorListener);
            this.isRegiterLightSensor = false;
            if (DEBUG_REPORT) {
                Slog.d(TAG, "unregisterReportSensor.");
            }
        }
    }

    public void sendMsgUnregisterSensor() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 1000);
    }

    public float getManulAmbientLux() {
        return this.mManulAmbientLuxValue;
    }

    public void reportAppProcess(String processName) {
        this.mCurrentAppProcessName = processName;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 2000);
    }

    public void setAppDurationTime() {
        this.mAppDurationTime = System.currentTimeMillis();
    }

    public String getCurAppProcessName() {
        return this.mCurrentAppProcessName;
    }

    public static Spline createAutoBrightnessSpline(int[] lux, int[] brightness) {
        if (lux == null || lux.length == 0 || brightness == null || brightness.length == 0) {
            Slog.e(TAG, "Could not create auto-brightness spline.");
            return null;
        }
        try {
            int n = brightness.length;
            float[] x = new float[n];
            float[] y = new float[n];
            if (mScreenUnderLightSensorSupport) {
                x[0] = 5.0f;
                Slog.w(TAG, "ScreenUnderLightSensorSupport = true, so used first value 5.");
            }
            y[0] = normalizeAbsoluteBrightness(brightness[0]);
            for (int i = 1; i < n; i++) {
                x[i] = (float) lux[i - 1];
                y[i] = normalizeAbsoluteBrightness(brightness[i]);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length - 1]) * 1.25f; v *= 1.25f) {
                    Slog.d(TAG, String.format("  %7.1f: %7.1f", Float.valueOf(v), Float.valueOf(spline.interpolate(v))));
                }
            }
            return spline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public static float normalizeAbsoluteBrightness(int value) {
        return ((float) clampAbsoluteBrightness(value)) / ((float) PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    public static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    public void setAIBrightnessLux(float lux) {
        AIBrightnessHelper aIBrightnessHelper = this.mAIBrightnessHelper;
        if (aIBrightnessHelper != null) {
            aIBrightnessHelper.setLux(lux);
            if (DEBUG) {
                Slog.d(TAG, "setAIBrightnessLux lux=" + lux);
            }
        }
    }

    public void resetAIBrightness() {
        AIBrightnessHelper aIBrightnessHelper = this.mAIBrightnessHelper;
        if (aIBrightnessHelper != null) {
            aIBrightnessHelper.reset();
            Slog.d(TAG, "resetAIBrightness");
        }
    }

    public boolean isAIBrightnessOpen() {
        return this.isAIBrightnessFeatureOpen;
    }

    public void setBrightnessByUser(int brightess) {
        AIBrightnessHelper aIBrightnessHelper = this.mAIBrightnessHelper;
        if (aIBrightnessHelper != null) {
            aIBrightnessHelper.setBrightnessByUser((float) brightess);
            if (DEBUG) {
                Slog.d(TAG, "setBrightnessByUser:brightness=" + brightess);
            }
        }
    }

    public void initAIBrightness(Context mcontext) {
        if (this.mAIBrightnessHelper == null) {
            this.mAIBrightnessHelper = new AIBrightnessHelper(this.mContext, ModelConfig.DefaultConfig.DEVICE_NAME);
        }
        this.isAIBrightnessFeatureOpen = this.mAIBrightnessHelper.isFeatureOpen();
        Slog.d(TAG, "isInAIBrightness=" + this.isAIBrightnessFeatureOpen);
    }

    public AIBrightnessHelper getAIBrightnessHelper() {
        if (this.mAIBrightnessHelper == null) {
            initAIBrightness(this.mContext);
        }
        return this.mAIBrightnessHelper;
    }

    @Override // com.android.server.display.IOppoBrightness
    public void resetBrightnessAdj() {
        if (this.mContext == null) {
            Slog.e(TAG, "resetBrightnessAdj mContext is illegal, required systemServer context!");
            return;
        }
        if (mManualBrightness == 0) {
            mManualBrightness = mBrightnessOverrideAdj;
            mManulAtAmbientLux = mBrightnessOverrideAmbientLux;
        }
        mManualBrightnessBackup = mManualBrightness;
        mManualAmbientLuxBackup = mManulAtAmbientLux;
        Settings.System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", (float) mManualBrightness, -2);
        Settings.System.putFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", mManulAtAmbientLux, -2);
        mManualBrightness = 0;
        mManulAtAmbientLux = MIN_LUX_LIMITI;
        mSaveBrightnessByShutdown = true;
        Log.d(TAG, "resetBrightnessAdj brightness backup = " + mManualBrightnessBackup);
    }

    @Override // com.android.server.display.IOppoBrightness
    public void setBrightnessNoAnimation(boolean noAnimation) {
        mBrightnessNoAnimation = noAnimation;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isFingerprintOpticalSupport() {
        return mFingerprintOpticalSupport;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isScreenUnderLightSensorSupport() {
        return mScreenUnderLightSensorSupport;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isUseAutoBrightness() {
        return mUseAutoBrightness;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isManualSetAutoBrightness() {
        return mManualSetAutoBrightness;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean enableDebug() {
        return DEBUG;
    }

    @Override // com.android.server.display.IOppoBrightness
    public int getReduceBrightnessMode() {
        return mReduceBrightnessMode;
    }

    @Override // com.android.server.display.IOppoBrightness
    public float getReduceBrightnessRate() {
        return mReduceBrightnessRate;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isLowPowerMode() {
        return mAppliedLowPower;
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean isReduceBrightnessAnimating() {
        return mReduceBrightnessAnimating;
    }

    @Override // com.android.server.display.IOppoBrightness
    public IOppoBrightness getAIBrightness() {
        Slog.i(TAG, "getAIBrightness");
        return getInstance();
    }

    @Override // com.android.server.display.IOppoBrightness
    public boolean setStateChanged(int msgId, Bundle extraData) {
        if (this.mAIBrightnessHelper == null) {
            return false;
        }
        if (DEBUG) {
            Slog.d(TAG, "setStateChanged.");
        }
        return this.mAIBrightnessHelper.setStateChanged(msgId, extraData);
    }
}
