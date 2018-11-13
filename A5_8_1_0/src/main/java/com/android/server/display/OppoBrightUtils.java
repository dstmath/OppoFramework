package com.android.server.display;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.dhcp.DhcpPacket;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.MathUtils;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.am.OppoProcessManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OppoBrightUtils {
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
    public static final long BRIGHTNESS_BOOST_SWITCHON_TIMEOUT = 1200;
    public static final int BRIGHTNESS_BOOST_SWITCH_ON = 3;
    private static final int BRIGHTNESS_DRAG_EFFECT_INTERVAL = 3;
    private static final float BRIGHTNESS_DRAG_MAX_SCALE = 0.3f;
    private static final int BRIGHTNESS_DRAG_NO_EFFECT_INTERVAL = 1;
    public static final long BRIGHTNESS_LIGHT_LIMIT = 70;
    public static final String BRIGHTNESS_MODE = "persist.sys.brightness.mode";
    public static final int BRIGHTNESS_OVERRIDE_IN = 1;
    public static final int BRIGHTNESS_OVERRIDE_NONE = -1;
    public static final int BRIGHTNESS_OVERRIDE_OUT = 0;
    public static int BRIGHTNESS_STEPS = 7;
    public static final int CAMERA_MODE_IN = 1;
    public static final int CAMERA_MODE_NONE = -1;
    public static final int CAMERA_MODE_OUT = 0;
    private static final int CODE_SEND_AD_STATUS_TO_SF = 11000;
    public static boolean DEBUG = false;
    public static boolean DEBUG_PRETEND_PROX_SENSOR_ABSENT = false;
    private static final String DEBUG_PRO = "oppo.brightness.debug";
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
    public static final float HIGH_BRIGHTENING_LUX_LIMITI = 10000.0f;
    public static final int HIGH_BRIGHTNESS_CAMERA_ON = 1;
    public static final int HIGH_BRIGHTNESS_DEBOUNCE_LUX = 18000;
    public static final int HIGH_BRIGHTNESS_DISABLE = 0;
    public static final int HIGH_BRIGHTNESS_LCD_OFF = 0;
    public static final int HIGH_BRIGHTNESS_LCD_ON = 1;
    public static final int HIGH_BRIGHTNESS_LEVEL_MAX = 8;
    public static final int HIGH_BRIGHTNESS_LEVEL_ONE = 4;
    public static final int HIGH_BRIGHTNESS_LUX_STEP = 3000;
    public static final int HIGH_BRIGHTNESS_MAX_LUX = 30000;
    public static final String HIGH_BRIGHTNESS_NODE = "/sys/devices/virtual/mtk_disp_mgr/mtk_disp_mgr/LCM_HBM";
    public static final int HIGH_BRIGHTNESS_VALUE_TWO = 2;
    private static final int INVAILD_SCREEN_DEFAULT_BRIGHTNESS = -1;
    public static final int INVERSE_OFF = 0;
    public static final int INVERSE_ON = 1;
    private static final String KEY_DEVICE_MANUFACTURE = "Device manufacture:";
    private static final String KEY_DEVICE_VERSION = "Device version:";
    public static final int LBR_MODE_LEVEL_MAX = 63;
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_BOE = "ro.lcd.backlight.config_boe";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM = "ro.lcd.backlight.config_dsjm";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_ILITEK = "ro.lcd.backlight.config_ilitek";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_INNOLUX = "ro.lcd.backlight.config_innolux";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_JDI = "ro.lcd.backlight.config_jdi";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG = "ro.lcd.backlight.config_samsung";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG_TENBITS = "ro.lcd.backlight.samsung_tenbit";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA = "ro.lcd.backlight.config_tianma";
    private static final String LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY = "ro.lcd.backlight.config_truly";
    private static String LCD_DEVICE_MANUFACTURE = Shell.NIGHT_MODE_STR_UNKNOWN;
    private static String LCD_DEVICE_VERSION = Shell.NIGHT_MODE_STR_UNKNOWN;
    private static final String LCD_DEVICE_VERSION_TD4310 = "td4310";
    private static final String LCD_MANUFACTURE_BOE = "boe";
    private static final String LCD_MANUFACTURE_DSJM = "dsjm";
    private static final String LCD_MANUFACTURE_ILITEK = "ilitek";
    private static final String LCD_MANUFACTURE_INNOLUX = "innolux";
    private static final String LCD_MANUFACTURE_JDI = "jdi";
    private static final String LCD_MANUFACTURE_SAMSUNG = "samsung";
    private static final String LCD_MANUFACTURE_SAMSUNG_TENBITS = "samsung1024";
    private static final String LCD_MANUFACTURE_TIANMA = "tianma";
    private static final String LCD_MANUFACTURE_TRULY = "truly";
    private static final float LOW_PERCENT_BRIGHTNESS = 0.012f;
    public static final float MAX_LUX_LIMITI = 31000.0f;
    public static final float MIN_LUX_LIMITI = 0.0f;
    private static final int MSG_ADJUST_AD = 1;
    private static final int MSG_SCENE_RESET = 1;
    public static final String OUTDOOR_BL_NODE = "/sys/devices/virtual/mtk_disp_mgr/mtk_disp_mgr/outdoorbl";
    private static final String PLATFORM_MTK = "oppo.hw.manufacturer.mtk";
    private static final String PLATFORM_QUALCOMM = "oppo.hw.manufacturer.qualcomm";
    public static final long POCKET_RIGNING_STATE_TIMEOUT = 4000;
    public static final int POCKET_RINGING_STATE = 1;
    public static final float QUICKLY_DARK_AMOUNT = 2.0f;
    private static final int SAMPLE_SIZE = 20;
    private static final long SCENE_RESET_DELAY = 65000;
    private static final long SCENE_RESET_DELAY_INCAR = 15000;
    public static final long SCREENON_BRIGHTNESS_BOOST_TIMEOUT = 4000;
    private static final int SCREEN_AUTO_BRIGHTNESS_ELEVENBITS_LENGTH = 10;
    private static final int SCREEN_AUTO_BRIGHTNESS_OFFSET = 2;
    private static final int SCREEN_DEFAULT_BRIGHTNESS_OFFSET = 1;
    public static final int SEED_LBR_LOW_LUX = 15000;
    public static final String SEED_LBR_NODE = "/sys/class/graphics/fb0/lbr";
    public static final int SEED_LBR_STEP_SIZE = 235;
    private static final String SHUTDOWNFLAG_NODE = "/sys/devices/virtual/graphics/fb0/shutdownflag";
    public static final int SPECIAL_AMBIENT_LIGHT_HORIZON = 60000;
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
    private static int[] brighness_int_config;
    public static float[] mAutoBrightnessLux;
    public static float[] mAutoBrightnessLuxMaxLimit;
    public static float[] mAutoBrightnessLuxMinLimit;
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
    public static long mDarkDebounce = 0;
    public static int mDefaultBrightness = 102;
    public static boolean mDisplayStateOn = false;
    public static boolean mFirstSetOverride = true;
    public static boolean mFirstSetScreenState = true;
    public static boolean mGalleryBacklight = false;
    public static int mGalleryMode = 0;
    public static boolean mHighBrightnessModeSupport = false;
    public static int mInverseMode = 0;
    public static boolean mIsOledHighBrightness = false;
    private static long mLampGapTime = 0;
    public static float mManualAmbientLuxBackup = MIN_LUX_LIMITI;
    public static int mManualBrightness = 0;
    public static int mManualBrightnessBackup = 0;
    public static boolean mManualSetAutoBrightness = false;
    public static float mManulAtAmbientLux = MIN_LUX_LIMITI;
    public static int mMaxBrightness = 255;
    private static int mMaxSampleLux = 0;
    public static int mMinBrightness = 2;
    private static int mMinSampleLux = 0;
    public static boolean mOutdoorBrightnessSupport = false;
    private static boolean mPlatformMtk = false;
    private static boolean mPlatformQualcomm = false;
    public static boolean mPocketRingingState = false;
    private static long mPreTurningPointTime = 0;
    private static int mSampleCount = 0;
    public static int mSampleScene = 0;
    public static boolean mSaveBrightnessByShutdown = false;
    public static boolean mSeedLbrModeSupport = false;
    public static boolean mSetBrihgtnessSlide = false;
    public static int mShouldAdjustRate = 0;
    private static long mTurningPointTime = 0;
    public static boolean mUseAutoBrightness = false;
    public static boolean mUseWindowBrightness = false;
    public static int mVideoMode = 0;
    private String SPECIALIGHTSENSOR;
    private String SPECIALIGHTSENSOR1;
    private String SPECIAL_LIGHT_SENSOR2;
    private String SPECIAL_LIGHT_SENSOR3;
    private String SPECIAL_LIGHT_SENSOR4;
    private String SPECIAL_LIGHT_SENSOR5;
    private int[] cameraGallerySamsung;
    private int mAdStatus;
    private int mAdStatus_before;
    private int mBootupBrightVaule;
    private Calendar mCalendar;
    private Context mContext;
    private int mEnable_before;
    private IBinder mFlinger;
    private Handler mHandler;
    private Sensor mLightSensor;
    private boolean mLightSensorAlwaysOn;
    private final SensorEventListener mLightSensorListener;
    private int mNightBrihtness;
    private int[] mSampleLux;
    private long[] mSampleTime;
    private Handler mSceneHandler;
    private SensorManager mSensorManager;
    public boolean mSpecialLightSensor;
    private boolean mStkCmLightSensor;

    private static class OppoBrightUtilsHolder {
        static final OppoBrightUtils INSTANCE = new OppoBrightUtils();

        private OppoBrightUtilsHolder() {
        }
    }

    /* synthetic */ OppoBrightUtils(OppoBrightUtils -this0) {
        this();
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
        this.mSampleLux = new int[20];
        this.mSampleTime = new long[20];
        this.cameraGallerySamsung = new int[]{0, 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 32, 36, 40, 44, 47, 50, 53, 56, 59, 62, 65, 68, 71, 74, 77, 80, 83, 85, 87, 89, 91, 94, 97, 100, 102, 104, 106, 108, 110, 112, 114, HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW, HdmiCecKeycode.CEC_KEYCODE_DATA, 120, 122, 124, 126, 128, 130, 132, NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT, NetworkConstants.ICMPV6_NEIGHBOR_ADVERTISEMENT, 138, 140, 142, 144, 146, 147, 148, 149, OppoProcessManager.MSG_READY_ENTER_STRICTMODE, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_PLUS, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_NEUTRAL, HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_MINUS, OppoProcessManager.MSG_UPLOAD, OppoProcessManager.MSG_REMOVE_RECORD, OppoProcessManager.MSG_APP_DIED, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, 196, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 205, 205, 206, 207, 207, 208, 209, 209, 210, 210, 211, 211, 212, 212, NetdResponseCode.InterfaceGetCfgResult, NetdResponseCode.InterfaceGetCfgResult, NetdResponseCode.SoftapStatusResult, NetdResponseCode.SoftapStatusResult, 215, 215, NetdResponseCode.InterfaceRxCounterResult, NetdResponseCode.InterfaceRxCounterResult, NetdResponseCode.InterfaceTxCounterResult, NetdResponseCode.InterfaceTxCounterResult, 218, 218, 219, 219, NetdResponseCode.QuotaCounterResult, NetdResponseCode.QuotaCounterResult, NetdResponseCode.QuotaCounterResult, NetdResponseCode.TetheringStatsResult, NetdResponseCode.TetheringStatsResult, NetdResponseCode.DnsProxyQueryResult, NetdResponseCode.DnsProxyQueryResult, NetdResponseCode.DnsProxyQueryResult, NetdResponseCode.ClatdStatusResult, NetdResponseCode.ClatdStatusResult, 224, 224, 224, 225, 225, 226, 226, 226, 227, 227, 228, 228, 228, 229, 229, 230, 230, 230, 231, 231, 232, 232, 232, 233, 233, 234, 234, 234, SEED_LBR_STEP_SIZE, SEED_LBR_STEP_SIZE, DhcpPacket.MIN_PACKET_LENGTH_BOOTP, DhcpPacket.MIN_PACKET_LENGTH_BOOTP, DhcpPacket.MIN_PACKET_LENGTH_BOOTP, 237, 237, 238, 238, 238, 239, 239, 240, 240, 240, 241, 241, 241, 242, 242, 242, 243, 243, 243, 244, 244, 244, 245, 245, 245, 246, 246, 246, 247, 247, 247, 247, 248, 248, 248, 249, 249, 249, 249, 250, 250, 250, 250, 251, 251, 251, 251, 252, 252, 252, 252, 253, 253, 253, 253, 254, 254, 254, 254, 254, 255, 255, 255, 255, 255, 255};
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        OppoBrightUtils.this.adjustAdStatus(msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mLightSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (OppoBrightUtils.this.mLightSensorAlwaysOn) {
                    float lux = event.values[0];
                    if ((OppoBrightUtils.this.mAdStatus != 1 || lux < OppoBrightUtils.ENABLE_AD_LUX) && (OppoBrightUtils.this.mAdStatus != 0 || lux >= OppoBrightUtils.ENABLE_AD_LUX)) {
                        OppoBrightUtils.this.mHandler.removeMessages(1);
                        Message msg = OppoBrightUtils.this.mHandler.obtainMessage(1);
                        if (lux >= OppoBrightUtils.ENABLE_AD_LUX) {
                            OppoBrightUtils.this.mAdStatus = 1;
                            msg.arg1 = OppoBrightUtils.this.mAdStatus;
                        } else {
                            OppoBrightUtils.this.mAdStatus = 0;
                            msg.arg1 = OppoBrightUtils.this.mAdStatus;
                        }
                        if (!(OppoBrightUtils.this.mAdStatus == 2 || OppoBrightUtils.this.mAdStatus == OppoBrightUtils.this.mAdStatus_before)) {
                            OppoBrightUtils.this.mHandler.sendMessageDelayed(msg, 4000);
                            OppoBrightUtils.this.mAdStatus_before = OppoBrightUtils.this.mAdStatus;
                        }
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mSceneHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        OppoBrightUtils.mSampleScene = 0;
                        Slog.d(OppoBrightUtils.TAG, "reset to SPECIAL_SCENE_NORMAL");
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void init(Context context) {
        this.mContext = context;
        DEBUG = SystemProperties.getBoolean(DEBUG_PRO, false);
    }

    public void getScreenAutoBrightnessConfig() {
        String config_string_from_property;
        String lcd_manufacture = getDeviceManufacture("lcd").toLowerCase(Locale.US);
        if (lcd_manufacture.contains(LCD_MANUFACTURE_BOE)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_BOE, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TRULY)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TRULY, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_TIANMA)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_TIANMA, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG_TENBITS)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG_TENBITS, "");
            mBrightnessBitsConfig = 2;
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_SAMSUNG, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_JDI)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_JDI, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_DSJM)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_DSJM, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_INNOLUX)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_INNOLUX, "");
        } else if (lcd_manufacture.contains(LCD_MANUFACTURE_ILITEK)) {
            config_string_from_property = SystemProperties.get(LCD_BACKLIGHT_CONFIG_PROPERTY_ILITEK, "");
        } else {
            config_string_from_property = null;
        }
        Slog.i(TAG, "config_string_from_property : " + config_string_from_property);
        if (config_string_from_property != null) {
            String[] brightness_string_config = config_string_from_property.trim().split(",");
            if (brightness_string_config != null && brightness_string_config.length > 2) {
                int config_length;
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
                    mDefaultBrightness = this.mContext.getResources().getInteger(17694849);
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
                        } catch (NumberFormatException e22) {
                            Slog.e(TAG, "brighness_int_config NumberFormatException:" + e22.getMessage());
                            brighness_int_config = null;
                        }
                    }
                }
            }
        }
        if (brighness_int_config == null) {
            brighness_int_config = this.mContext.getResources().getIntArray(17235982);
            for (int i2 : brighness_int_config) {
                Slog.i(TAG, "brighness_int_config : " + i2);
            }
            BRIGHTNESS_STEPS = brighness_int_config.length;
            mDefaultBrightness = this.mContext.getResources().getInteger(17694849);
            Slog.d(TAG, "BRIGHTNESS_STEPS = " + BRIGHTNESS_STEPS + " mDefaultBrightness = " + mDefaultBrightness);
        }
    }

    public int getMinimumScreenBrightnessSetting() {
        if (mBrightnessBitsConfig == 3) {
            mMinBrightness = this.mContext.getResources().getInteger(17694784);
        } else if (mBrightnessBitsConfig == 2) {
            mMinBrightness = this.mContext.getResources().getInteger(17694862);
        } else {
            mMinBrightness = this.mContext.getResources().getInteger(17694851);
        }
        return mMinBrightness;
    }

    public int getMaximumScreenBrightnessSetting() {
        if (mBrightnessBitsConfig == 3) {
            mMaxBrightness = this.mContext.getResources().getInteger(17694783);
        } else if (mBrightnessBitsConfig == 2) {
            mMaxBrightness = this.mContext.getResources().getInteger(17694861);
        } else {
            mMaxBrightness = this.mContext.getResources().getInteger(17694850);
        }
        return mMaxBrightness;
    }

    public int getDefaultScreenBrightnessSetting() {
        return mDefaultBrightness;
    }

    public void readFeatureProperty() {
        String version = LCD_DEVICE_VERSION.toLowerCase(Locale.US);
        String manufacture = LCD_DEVICE_MANUFACTURE.toLowerCase(Locale.US);
        mHighBrightnessModeSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.high.brightness.support");
        Slog.i(TAG, "mHighBrightnessModeSupport = " + mHighBrightnessModeSupport);
        mSeedLbrModeSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.seed.lbr.support");
        Slog.i(TAG, "mSeedLbrModeSupport = " + mSeedLbrModeSupport);
        mBrightnessLowLevelScreenSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.brightness.lowlevel.screen");
        Slog.i(TAG, "mBrightnessLowLevelScreenSupport = " + mBrightnessLowLevelScreenSupport);
        mOutdoorBrightnessSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.outdoor.brightness.support");
        if (mOutdoorBrightnessSupport && version.contains(LCD_DEVICE_VERSION_TD4310) && (manufacture.contains(LCD_MANUFACTURE_BOE) || manufacture.contains(LCD_MANUFACTURE_JDI))) {
            mOutdoorBrightnessSupport = false;
        }
        if (manufacture.contains(LCD_MANUFACTURE_SAMSUNG)) {
            SystemProperties.set(BRIGHTNESS_MODE, "0");
        } else {
            SystemProperties.set(BRIGHTNESS_MODE, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
        Slog.i(TAG, "mOutdoorBrightnessSupport = " + mOutdoorBrightnessSupport + "  manufacture = " + manufacture + "   curve mode = " + SystemProperties.get(BRIGHTNESS_MODE));
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
        if (3 == mBrightnessBitsConfig) {
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST = 1600;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SCREENON = 960;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SLOW = Vr2dDisplay.DEFAULT_VIRTUAL_DISPLAY_DPI;
            mAutoBrightnessLux = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMinLimit = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMaxLimit = new float[BRIGHTNESS_STEPS];
            SPECIAL_SCENE_STREET_LAMP_LUX = 10;
        } else if (2 == mBrightnessBitsConfig) {
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST = 800;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SCREENON = SystemService.PHASE_LOCK_SETTINGS_READY;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SLOW = 160;
            mAutoBrightnessLux = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMinLimit = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMaxLimit = new float[BRIGHTNESS_STEPS];
        } else {
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_FAST = 200;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SCREENON = 120;
            DisplayPowerController.BRIGHTNESS_RAMP_RATE_SLOW = 40;
            mAutoBrightnessLux = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMinLimit = new float[BRIGHTNESS_STEPS];
            mAutoBrightnessLuxMaxLimit = new float[BRIGHTNESS_STEPS];
        }
    }

    public int[] readAutoBrightnessLuxConfig() {
        int[] lux;
        int[] minLimit;
        int[] maxLimit;
        Resources resources = this.mContext.getResources();
        Slog.w(TAG, "mBrightnessBitsConfig= " + mBrightnessBitsConfig + " ,BRIGHTNESS_STEPS=" + BRIGHTNESS_STEPS);
        if (3 == mBrightnessBitsConfig) {
            lux = resources.getIntArray(17236007);
            minLimit = resources.getIntArray(17236009);
            maxLimit = resources.getIntArray(17236008);
        } else if (2 == mBrightnessBitsConfig) {
            lux = resources.getIntArray(17236038);
            minLimit = resources.getIntArray(17236040);
            maxLimit = resources.getIntArray(17236039);
        } else {
            lux = resources.getIntArray(17235983);
            minLimit = resources.getIntArray(17235985);
            maxLimit = resources.getIntArray(17235984);
        }
        if (lux == null || minLimit == null || maxLimit == null) {
            Slog.e(TAG, "lux, minLimit, maxLimit is null");
            return null;
        }
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
            lux[5] = Vr2dDisplay.DEFAULT_VIRTUAL_DISPLAY_WIDTH;
            minLimit[4] = 50;
            minLimit[5] = OppoProcessManager.MSG_READY_ENTER_STRICTMODE;
            minLimit[6] = 600;
            maxLimit[3] = 100;
            maxLimit[4] = 500;
            maxLimit[5] = 1200;
        }
        for (int i = 0; i < BRIGHTNESS_STEPS; i++) {
            if (i == 0) {
                mAutoBrightnessLux[i] = MIN_LUX_LIMITI;
            } else {
                mAutoBrightnessLux[i] = (float) lux[i - 1];
            }
            mAutoBrightnessLuxMinLimit[i] = (float) minLimit[i];
            mAutoBrightnessLuxMaxLimit[i] = (float) maxLimit[i];
            Slog.w(TAG, "mAutoBrightnessLux[" + i + "] = " + mAutoBrightnessLux[i] + " mAutoBrightnessLuxMinLimit[" + i + "] = " + mAutoBrightnessLuxMinLimit[i] + " mAutoBrightnessLuxMaxLimit[" + i + "] = " + mAutoBrightnessLuxMaxLimit[i]);
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
        if (3 == mBrightnessBitsConfig) {
            return brighness_int_config[1];
        }
        if (2 == mBrightnessBitsConfig) {
            return brighness_int_config[1];
        }
        if (1 == mBrightnessBitsConfig) {
            return brighness_int_config[1];
        }
        return brighness_int_config[1];
    }

    public float findAmbientLux(int area) {
        float lux = MIN_LUX_LIMITI;
        for (int k = 0; k < BRIGHTNESS_STEPS; k++) {
            if (area == k) {
                lux = mAutoBrightnessLux[k];
                break;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "area=" + area + ", lux=" + lux);
        }
        return lux;
    }

    public int resetAmbientLux(float ambientLux) {
        for (int k = 0; k < BRIGHTNESS_STEPS; k++) {
            if (Math.round(ambientLux) <= Math.round(mAutoBrightnessLux[k])) {
                return k + 1;
            }
        }
        return 0;
    }

    public void writeHighbrightnessNodeValue(int value) {
        writeFileNodeValue(HIGH_BRIGHTNESS_NODE, value);
    }

    public void writeSeedLbrNodeValue(int value) {
        writeFileNodeValue(SEED_LBR_NODE, value);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0045 A:{SYNTHETIC, Splitter: B:21:0x0045} */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0033 A:{SYNTHETIC, Splitter: B:15:0x0033} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeFileNodeValue(String str, int value) {
        Exception e;
        Throwable th;
        FileWriter writer = null;
        try {
            FileWriter writer2 = new FileWriter(new File(str));
            try {
                writer2.write(String.valueOf(value));
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (Exception e2) {
                        Slog.d(TAG, "writeFileNodeValue io stream close wrong");
                    }
                }
                writer = writer2;
            } catch (Exception e3) {
                e = e3;
                writer = writer2;
                try {
                    Slog.d(TAG, "writeFileNodeValue sorry write wrong");
                    e.printStackTrace();
                    if (writer == null) {
                        try {
                            writer.close();
                        } catch (Exception e4) {
                            Slog.d(TAG, "writeFileNodeValue io stream close wrong");
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (Exception e5) {
                            Slog.d(TAG, "writeFileNodeValue io stream close wrong");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                writer = writer2;
                if (writer != null) {
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            Slog.d(TAG, "writeFileNodeValue sorry write wrong");
            e.printStackTrace();
            if (writer == null) {
            }
        }
    }

    public int caclurateRateForIndex(float lux, int originBrightness, int targetBrightness) {
        int rate;
        if (3 == mBrightnessBitsConfig) {
            if (targetBrightness >= originBrightness) {
                rate = Math.round(((Math.abs(lux) * 150.0f) + 809850.0f) / 9000.0f);
                if (targetBrightness <= brighness_int_config[3]) {
                    rate = EIGHT_BITS_BRIGHT_MAX_RATE;
                }
                if (originBrightness <= brighness_int_config[3] && targetBrightness >= brighness_int_config[4]) {
                    rate = Math.round(((Math.abs(lux) * 100.0f) + 2699900.0f) / 9000.0f);
                }
                if (originBrightness >= brighness_int_config[4] && targetBrightness >= brighness_int_config[4]) {
                    rate = Math.round(((Math.abs(lux) * 120.0f) + 1079880.0f) / 9000.0f);
                }
                if (!this.mStkCmLightSensor) {
                    return rate;
                }
                rate += rate;
                if (rate > 500) {
                    return 500;
                }
                return rate;
            } else if (targetBrightness > originBrightness) {
                return 0;
            } else {
                rate = Math.round(((Math.abs(lux) * 80.0f) + 359920.0f) / 9000.0f);
                if (this.mStkCmLightSensor) {
                    rate += rate;
                }
                if (rate > 120) {
                    return 120;
                }
                return rate;
            }
        } else if (2 != mBrightnessBitsConfig) {
            return 0;
        } else {
            if (targetBrightness >= originBrightness) {
                return Math.round(((Math.abs(lux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            if (targetBrightness > originBrightness) {
                return 0;
            }
            rate = Math.round(((Math.abs(lux) * 96.0f) + 215880.0f) / 8999.0f);
            if (this.mStkCmLightSensor) {
                rate += rate;
            }
            if (rate > 120) {
                return 120;
            }
            return rate;
        }
    }

    public int caclurateRate(float nowLux, float lastLux) {
        int rate;
        if (3 == mBrightnessBitsConfig) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            rate = Math.round((((lastLux - nowLux) * 100.0f) + 449850.0f) / 8999.0f);
            if (rate > 120) {
                return 120;
            }
            return rate;
        } else if (2 == mBrightnessBitsConfig) {
            if (nowLux > lastLux) {
                return Math.round((((nowLux - lastLux) * 240.0f) + 1079640.0f) / 8999.0f);
            }
            return Math.round((((lastLux - nowLux) * 30.0f) + 134955.0f) / 8999.0f);
        } else if (nowLux > lastLux) {
            rate = Math.round((((nowLux - lastLux) * 20.0f) + 49979.0f) / 4999.0f);
            if (!this.mStkCmLightSensor) {
                return rate;
            }
            rate += rate;
            if (rate > EIGHT_BITS_BRIGHT_MAX_RATE) {
                return EIGHT_BITS_BRIGHT_MAX_RATE;
            }
            return rate;
        } else {
            rate = Math.round((((lastLux - nowLux) * 14.0f) + 29980.0f) / 4999.0f);
            if (this.mStkCmLightSensor) {
                rate += rate;
            }
            if (rate > 30) {
                return 30;
            }
            return rate;
        }
    }

    public int findNightBrightness(boolean stateon, int brightness) {
        int newScreenAutoBrightness = brightness;
        if (stateon) {
            this.mCalendar = Calendar.getInstance();
            if (this.mCalendar != null) {
                int hour = this.mCalendar.get(11);
                if (1 == mBrightnessBitsConfig) {
                    if (hour <= 19 && hour >= 7) {
                        this.mNightBrihtness = 0;
                    } else if (brightness == brighness_int_config[0]) {
                        this.mNightBrihtness = 3;
                    }
                } else if (3 == mBrightnessBitsConfig && this.mSpecialLightSensor) {
                    if (hour <= 6 || hour >= 19) {
                        this.mNightBrihtness = 0;
                    } else if (brightness == brighness_int_config[0]) {
                        this.mNightBrihtness = brighness_int_config[1] / 2;
                    }
                }
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "mNightBrihtness=" + this.mNightBrihtness + " ,mNightBrihtness=" + this.mNightBrihtness);
        }
        if (this.mNightBrihtness <= 0 || brightness != brighness_int_config[0]) {
            return newScreenAutoBrightness;
        }
        return this.mNightBrihtness;
    }

    public int changeMinBrightness(int brightness) {
        int tempbrightness = brightness;
        if (mBrightnessBitsConfig != 1) {
            return tempbrightness;
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar == null) {
            return tempbrightness;
        }
        int hour = calendar.get(11);
        if (hour <= 6 || hour >= 18 || brightness != mMinBrightness) {
            return tempbrightness;
        }
        return mMinBrightness + 1;
    }

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
            float uniform_brightness = ((float) brightness) / ((float) maxBrightness);
            float adjGamma = 0.6f;
            if (uniform_brightness > 0.196f) {
                adjGamma = 0.4f;
            }
            int adjust_brightness = Math.round(((float) maxBrightness) * MathUtils.pow(uniform_brightness, adjGamma));
            Slog.d(TAG, "adjust_brightness = " + adjust_brightness);
            return adjust_brightness;
        } else if (brightness >= 256 || brightness <= 0) {
            return 128;
        } else {
            return this.cameraGallerySamsung[brightness];
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b2 A:{SYNTHETIC, Splitter: B:29:0x00b2} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00d4 A:{SYNTHETIC, Splitter: B:42:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f1 A:{SYNTHETIC, Splitter: B:50:0x00f1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getMtkDeviceVersion() {
        Throwable th;
        String filePath = "/proc/devinfo/Sensor_alsps";
        File file = new File(filePath);
        String version = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + filePath + " is not exist...");
            return version;
        } else if (file.canRead()) {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String line = bufferedReader2.readLine();
                        if (line == null) {
                            break;
                        }
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION)) {
                            String[] lineSplit = line.split(":");
                            if (lineSplit != null && lineSplit.length > 1) {
                                version = lineSplit[1].trim();
                                if (version != null) {
                                    Slog.i(TAG, KEY_DEVICE_VERSION + version);
                                }
                            }
                        }
                    } catch (FileNotFoundException e) {
                        bufferedReader = bufferedReader2;
                        try {
                            Slog.i(TAG, "FileNotFoundException caught");
                            if (bufferedReader != null) {
                            }
                            return version;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e2) {
                        bufferedReader = bufferedReader2;
                        Slog.i(TAG, "IOException caught");
                        if (bufferedReader != null) {
                        }
                        return version;
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Slog.i(TAG, "bufferedReader.close IOException caught");
                            }
                        }
                        throw th;
                    }
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e4) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
            } catch (FileNotFoundException e5) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e6) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                return version;
            } catch (IOException e7) {
                Slog.i(TAG, "IOException caught");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                return version;
            }
            return version;
        } else {
            Slog.e(TAG, "No permission to read " + filePath);
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
            Sensor sensor = (Sensor) sensors.get(i);
            if (sensor.getType() == 8) {
                String[] splitSensorName = sensor.getName().split(" ");
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
        String type = DEFAULT_MANUFACTURE;
        mPlatformQualcomm = this.mContext.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
        mPlatformMtk = this.mContext.getPackageManager().hasSystemFeature(PLATFORM_MTK);
        Slog.i(TAG, "mPlatformQualcomm = " + mPlatformQualcomm + " mPlatformMtk " + mPlatformMtk);
        if (mPlatformQualcomm) {
            return getQualcommDeviceVersion();
        }
        if (mPlatformMtk) {
            return getMtkDeviceVersion();
        }
        return type;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0147 A:{SYNTHETIC, Splitter: B:59:0x0147} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0135 A:{SYNTHETIC, Splitter: B:53:0x0135} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x011a A:{SYNTHETIC, Splitter: B:45:0x011a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getDeviceManufacture(String deviceName) {
        Throwable th;
        String filePath = "/proc/devinfo/" + deviceName;
        File file = new File(filePath);
        String manufacture = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + filePath + " is not exist...");
            return DEFAULT_MANUFACTURE;
        } else if (file.canRead()) {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String line = bufferedReader2.readLine();
                        if (line == null) {
                            break;
                        }
                        String[] lineSplit;
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION)) {
                            lineSplit = line.split(":");
                            if (lineSplit != null && lineSplit.length > 1) {
                                manufacture = lineSplit[1].trim();
                                if (manufacture != null) {
                                    Slog.i(TAG, "version : " + manufacture);
                                    LCD_DEVICE_VERSION = manufacture;
                                }
                            }
                        }
                        if (line.contains(KEY_DEVICE_MANUFACTURE)) {
                            lineSplit = line.split(":");
                            if (lineSplit != null && lineSplit.length > 1) {
                                manufacture = lineSplit[1].trim();
                                if (manufacture != null) {
                                    Slog.i(TAG, "manufacture : " + manufacture);
                                    LCD_DEVICE_MANUFACTURE = manufacture;
                                    break;
                                }
                            }
                        }
                    } catch (FileNotFoundException e) {
                        bufferedReader = bufferedReader2;
                        Slog.i(TAG, "FileNotFoundException caught");
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e2) {
                                Slog.i(TAG, "bufferedReader.close IOException caught");
                            }
                        }
                        if (manufacture == null) {
                        }
                        return manufacture;
                    } catch (IOException e3) {
                        bufferedReader = bufferedReader2;
                        try {
                            Slog.i(TAG, "IOException caught");
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e4) {
                                    Slog.i(TAG, "bufferedReader.close IOException caught");
                                }
                            }
                            if (manufacture == null) {
                            }
                            return manufacture;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e5) {
                                    Slog.i(TAG, "bufferedReader.close IOException caught");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e6) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                bufferedReader = bufferedReader2;
            } catch (FileNotFoundException e7) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (bufferedReader != null) {
                }
                if (manufacture == null) {
                }
                return manufacture;
            } catch (IOException e8) {
                Slog.i(TAG, "IOException caught");
                if (bufferedReader != null) {
                }
                if (manufacture == null) {
                }
                return manufacture;
            }
            if (manufacture == null) {
                manufacture = DEFAULT_MANUFACTURE;
            }
            return manufacture;
        } else {
            Slog.e(TAG, "No permission to read " + filePath);
            return DEFAULT_MANUFACTURE;
        }
    }

    public boolean isAgingTestVersion() {
        return LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
    }

    public int getBootupBrightness() {
        int brightvalue = this.mBootupBrightVaule;
        brightvalue = (this.mBootupBrightVaule * (mMaxBrightness + 1)) / 256;
        if (mBrightnessBitsConfig == 2) {
            return brightvalue / 2;
        }
        return brightvalue;
    }

    private void adjustAdStatus(int status) {
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

    public int getPhoneState() {
        TelephonyManager manager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (manager != null) {
            return manager.getCallState();
        }
        if (DEBUG) {
            Slog.d(TAG, "mLightSensorAlwaysOn: False");
        }
        return 0;
    }

    public void writeOutdoorNodeValue(int value) {
        writeFileNodeValue(OUTDOOR_BL_NODE, value);
    }

    public void writeShutdownFlagNodeValue() {
        if (DEBUG) {
            Slog.d(TAG, "writeShutdownFlagNodeValue");
        }
        writeFileNodeValue(SHUTDOWNFLAG_NODE, 1);
    }

    public void setHighBrightness(int value) {
        if (DEBUG) {
            Slog.d(TAG, "setHighBrightness = " + value);
        }
        if (value == 8) {
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
            if (lManulScale > BRIGHTNESS_DRAG_MAX_SCALE) {
                lManulScale = BRIGHTNESS_DRAG_MAX_SCALE;
            }
            if (lIntervalGap <= 3) {
                lChangeStep = Math.round((((float) brightness) * lManulScale) * (1.0f - (((float) lIntervalGap) / 4.0f)));
            }
            if (nowInterval > 1) {
                if (manulBrightness > originBrightness) {
                    lDragBrightness = brightness + lChangeStep;
                } else if (manulBrightness < originBrightness) {
                    lDragBrightness = brightness - lChangeStep;
                }
            }
            lDragBrightness = MathUtils.constrain(lDragBrightness, mMinBrightness, mMaxBrightness);
        }
        if (DEBUG) {
            Slog.d(TAG, " manul = " + manulInterval + " now = " + nowInterval + " Drag = " + lDragBrightness + " Step = " + lChangeStep);
        }
        return lDragBrightness;
    }

    public void setPocketRingingState(boolean state) {
        mPocketRingingState = state;
    }

    public boolean isSpecialAdj(float value) {
        if (value == (((float) mMaxBrightness) * 500.0f) / 255.0f || value == (((float) mMaxBrightness) * 300.0f) / 255.0f || value == (((float) mMaxBrightness) * 301.0f) / 255.0f || value == 16384.0f || value == 32768.0f || value == 16385.0f || value == 32769.0f || value == 16386.0f) {
            return true;
        }
        return value == 32770.0f;
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
            buf.append(" ");
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
        Message msg;
        long debounce = mDarkDebounce;
        if (maxindex > 0 && maxindex < newindex && minindex < maxindex && this.mSampleLux[minindex] <= SPECIAL_SCENE_STREET_LAMP_LUX) {
            mTurningPointTime = this.mSampleTime[maxindex + 1];
            if (mTurningPointTime > mPreTurningPointTime && mPreTurningPointTime > 0) {
                mLampGapTime = mTurningPointTime - mPreTurningPointTime;
            }
            if (mLampGapTime > 0 && mLampGapTime < 5000) {
                mSampleScene = 2;
                debounce = 10000;
                Slog.d(TAG, "STREET_LAMP_INCAR dark debounce = " + 10000);
                this.mSceneHandler.removeMessages(1);
                msg = this.mSceneHandler.obtainMessage(1);
                msg.arg1 = 1;
                this.mSceneHandler.sendMessageDelayed(msg, SCENE_RESET_DELAY_INCAR);
            } else if (mLampGapTime > 0 && mLampGapTime < 60000) {
                mSampleScene = 1;
                debounce = STREET_LAMP_DARK_DEBOUNCE;
                Slog.d(TAG, "STREET_LAMP dark debounce = " + STREET_LAMP_DARK_DEBOUNCE);
                this.mSceneHandler.removeMessages(1);
                msg = this.mSceneHandler.obtainMessage(1);
                msg.arg1 = 1;
                this.mSceneHandler.sendMessageDelayed(msg, SCENE_RESET_DELAY);
            }
            if (mTurningPointTime > mPreTurningPointTime) {
                mPreTurningPointTime = mTurningPointTime;
            }
        }
        if (minindex <= maxindex || newindex <= minindex) {
            return debounce;
        }
        if (mSampleScene == 2) {
            mSampleScene = 2;
            Slog.d(TAG, "update dark debounce = " + 10000);
            this.mSceneHandler.removeMessages(1);
            msg = this.mSceneHandler.obtainMessage(1);
            msg.arg1 = 1;
            this.mSceneHandler.sendMessageDelayed(msg, SCENE_RESET_DELAY_INCAR);
            return 10000;
        } else if (mSampleScene != 1) {
            return debounce;
        } else {
            mSampleScene = 1;
            Slog.d(TAG, "update dark debounce = " + STREET_LAMP_DARK_DEBOUNCE);
            this.mSceneHandler.removeMessages(1);
            msg = this.mSceneHandler.obtainMessage(1);
            msg.arg1 = 1;
            this.mSceneHandler.sendMessageDelayed(msg, SCENE_RESET_DELAY);
            return STREET_LAMP_DARK_DEBOUNCE;
        }
    }

    public long checkSpecialScene(long time, int lux) {
        long darkDebounce = mDarkDebounce;
        int maxIndex = 19;
        int minIndex = 19;
        mMaxSampleLux = lux;
        mMinSampleLux = lux;
        int i = 0;
        while (i < 19) {
            this.mSampleTime[i] = this.mSampleTime[i + 1];
            this.mSampleLux[i] = this.mSampleLux[i + 1];
            if (time - this.mSampleTime[i] > VALID_SAMPLE_TIME_LIMIT) {
                this.mSampleTime[i] = 0;
                this.mSampleLux[i] = 0;
            }
            if (this.mSampleLux[i] >= mMaxSampleLux) {
                mMaxSampleLux = this.mSampleLux[i];
                maxIndex = i;
            }
            if (this.mSampleLux[i] <= mMinSampleLux && this.mSampleTime[i] > 0) {
                mMinSampleLux = this.mSampleLux[i];
                minIndex = i;
            }
            i++;
        }
        this.mSampleTime[19] = time;
        this.mSampleLux[19] = lux;
        if (this.mSampleLux[19] >= this.mSampleLux[maxIndex]) {
            maxIndex = 19;
            mMaxSampleLux = this.mSampleLux[19];
        }
        Slog.d(TAG, "lux sample = " + toString());
        Slog.d(TAG, "maxindex = " + maxIndex + " minindex = " + minIndex);
        return checkStreetLampScene(maxIndex, minIndex, 19);
    }
}
