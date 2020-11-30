package com.android.server.power;

import android.common.OppoFeatureCache;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.wm.IColorAppSwitchManager;
import com.oppo.hypnus.Hypnus;

public class ColorBatterySaveExtend implements IColorBatterySaveExtend {
    private static final String BACKUP_FEEDBACK_STATE = "powersave_backup_feedback_state";
    private static final String BACKUP_SCREENOFF_TIME = "powersave_backup_screenoff_time";
    private static final long DEFAULT_SCREEN_OFF_TIME_VALUE = 30000;
    private static final String IS_FEEDBACK_CHANGE_BY_USER = "is_feedback_change_by_user";
    private static final String KEY_POWER_SAVE_SUB_BACKLIGHT = "power_save_backlight_state";
    private static final int NOTIF_SRC_LOW_POWER_MODE = 3;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_OFF = 21;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_ON = 20;
    private static final long POWER_SAVER_MODE_SCREEN_OFF_TIME = 15000;
    private static final String SREENOFF_TIME_SWITCH_STATE = "power_save_screenoff_time_state";
    private static final String TAG = "ColorBatterySaveExtend";
    private static boolean mOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorBatterySaveExtend sColorBatterySaveExtend = null;
    private IColorAppSwitchManager.ActivityChangedListener mActivityChangedListener = new IColorAppSwitchManager.ActivityChangedListener() {
        /* class com.android.server.power.ColorBatterySaveExtend.AnonymousClass2 */

        public void onActivityChanged(String prePkg, String nextPkg) {
            if (ColorBatterySaveExtend.this.mBatterySaveEnabled && nextPkg != null && !"com.coloros.recents".equals(nextPkg)) {
                boolean isSpeedUpGame = OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).inGameSpacePkgList(nextPkg);
                if (ColorBatterySaveExtend.mOppoDebug) {
                    Log.d(ColorBatterySaveExtend.TAG, "hypnusLowPowerModeOn isSpeedUpGame=" + isSpeedUpGame + ", pkg=" + nextPkg);
                }
                if (isSpeedUpGame) {
                    ColorBatterySaveExtend.this.hypnusLowPowerModeOn(false);
                } else {
                    ColorBatterySaveExtend.this.hypnusLowPowerModeOn(true);
                }
            }
        }
    };
    private boolean mBatterySaveEnabled;
    private Context mContext;
    private FeedbackStateObserver mFeedbackStateObserver;
    private Hypnus mHyp;
    private boolean mHypnusLowPowerenabled;
    private boolean mIsSystemStartup;
    private Runnable mRunBatterySaveReg = new Runnable() {
        /* class com.android.server.power.ColorBatterySaveExtend.AnonymousClass1 */

        public void run() {
            if (ColorBatterySaveExtend.mOppoDebug) {
                Log.d(ColorBatterySaveExtend.TAG, "RunBatterySaveReg");
            }
            OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).setActivityChangedListener(ColorBatterySaveExtend.this.mActivityChangedListener);
            ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class)).registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                /* class com.android.server.power.ColorBatterySaveExtend.AnonymousClass1.AnonymousClass1 */

                public int getServiceType() {
                    return 0;
                }

                public void onLowPowerModeChanged(PowerSaveState state) {
                    ColorBatterySaveExtend.this.onLowPowerModeChangedInternal(state.batterySaverEnabled);
                }
            });
        }
    };

    private ColorBatterySaveExtend(Context context) {
        this.mContext = context;
    }

    public static ColorBatterySaveExtend getInstance(Context context) {
        if (sColorBatterySaveExtend == null) {
            synchronized (ColorBatterySaveExtend.class) {
                if (sColorBatterySaveExtend == null) {
                    sColorBatterySaveExtend = new ColorBatterySaveExtend(context);
                }
            }
        }
        return sColorBatterySaveExtend;
    }

    public void init() {
        HandlerThread hd = new HandlerThread("batterySaveExtend");
        hd.start();
        Handler handler = new Handler(hd.getLooper());
        handler.post(this.mRunBatterySaveReg);
        this.mFeedbackStateObserver = new FeedbackStateObserver(handler);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SREENOFF_TIME_SWITCH_STATE), false, new ScreenOffTimeSwitchObserver(handler), 0);
    }

    public boolean isClosedSuperFirewall(PackageManager packageManager) {
        if (OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall()) {
            return true;
        }
        return false;
    }

    public void onBatterySaveChanged(ContentResolver contentResolver, boolean enable) {
        Settings.System.putIntForUser(contentResolver, "is_smart_enable", enable ? 1 : 0, 0);
    }

    public void setSystemStartup(boolean startup) {
        this.mIsSystemStartup = startup;
    }

    public void onAdjustBrightnessChanged(ContentResolver contentResolver, boolean enableAdjustBrightness) {
        Settings.System.putIntForUser(contentResolver, KEY_POWER_SAVE_SUB_BACKLIGHT, enableAdjustBrightness ? 1 : 0, 0);
    }

    public int getGpsMode(Context context) {
        int gpsMode;
        if (isClosedSuperFirewall(context.getPackageManager())) {
            gpsMode = 2;
        } else {
            gpsMode = 0;
        }
        if (mOppoDebug) {
            Log.d(TAG, "getGpsMode: gpsMode=" + gpsMode);
        }
        return gpsMode;
    }

    public int getGpsMode(Context context, String settings, String keyGpsMode) {
        int locationMode;
        if (settings == null) {
            Log.d(TAG, "getGpsMode: settings is null");
            return 0;
        }
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                locationMode = parser.getInt(keyGpsMode, 2);
            } else {
                locationMode = 0;
            }
            if (mOppoDebug) {
                Log.d(TAG, "getGpsMode: locationMode=" + locationMode);
            }
            return locationMode;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getGpsMode: Bad battery saver constants: " + settings);
            return 0;
        }
    }

    public boolean getFireWallDisabled(Context context, String settings, String keyFirewallDisabled) {
        boolean fireWallDisabled;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                fireWallDisabled = parser.getBoolean(keyFirewallDisabled, false);
            } else {
                fireWallDisabled = parser.getBoolean(keyFirewallDisabled, true);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getFireWallDisabled: fireWallDisabled=" + fireWallDisabled);
            }
            return fireWallDisabled;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getFireWallDisabled: Bad battery saver constants: " + settings);
            return true;
        }
    }

    public boolean getAdjustBrightnessDisabled(Context context, String settings, String keyAdjustBrightnessDisabled) {
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                return parser.getBoolean(keyAdjustBrightnessDisabled, true);
            }
            return parser.getBoolean(keyAdjustBrightnessDisabled, false);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getAdjustBrightnessDisabled: Bad battery saver constants: " + settings);
            return false;
        }
    }

    public boolean getVibrationDisabledConfig(Context context, String settings, String keyVibrationDisabledConfig) {
        boolean vibrationDisabledConfig;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                vibrationDisabledConfig = parser.getBoolean(keyVibrationDisabledConfig, true);
            } else {
                vibrationDisabledConfig = parser.getBoolean(keyVibrationDisabledConfig, false);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getVibrationDisabledConfig: vibrationDisabledConfig=" + vibrationDisabledConfig);
            }
            return vibrationDisabledConfig;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getVibrationDisabledConfig: Bad battery saver constants: " + settings);
            return false;
        }
    }

    public boolean getOptionalSensorsDisabled(Context context, String settings, String keyOptionalSensorsDisabled) {
        boolean optionalSensorsDisabled;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                optionalSensorsDisabled = parser.getBoolean(keyOptionalSensorsDisabled, true);
            } else {
                optionalSensorsDisabled = parser.getBoolean(keyOptionalSensorsDisabled, false);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getOptionalSensorsDisabled: optionalSensorsDisabled=" + optionalSensorsDisabled);
            }
            return optionalSensorsDisabled;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getOptionalSensorsDisabled: Bad battery saver constants: " + settings);
            return false;
        }
    }

    public boolean getAodDisabled(Context context, String settings, String keyAodDisabled) {
        boolean aodDisabled;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                aodDisabled = parser.getBoolean(keyAodDisabled, true);
            } else {
                aodDisabled = parser.getBoolean(keyAodDisabled, false);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getAodDisabled: aodDisabled=" + aodDisabled);
            }
            return aodDisabled;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getAodDisabled: Bad battery saver constants: " + settings);
            return false;
        }
    }

    public boolean getLaunchBoostDisabled(Context context, String settings, String keyLaunchBoostDisabled) {
        boolean launchBoostDisabled;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                launchBoostDisabled = parser.getBoolean(keyLaunchBoostDisabled, true);
            } else {
                launchBoostDisabled = parser.getBoolean(keyLaunchBoostDisabled, false);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getLaunchBoostDisabled: launchBoostDisabled=" + launchBoostDisabled);
            }
            return launchBoostDisabled;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getLaunchBoostDisabled: Bad battery saver constants: " + settings);
            return false;
        }
    }

    public boolean getSoundTriggerDisabled(Context context, String settings, String key) {
        boolean soundTriggerDisabled;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
            if (isClosedSuperFirewall(context.getPackageManager())) {
                soundTriggerDisabled = parser.getBoolean(key, true);
            } else {
                soundTriggerDisabled = parser.getBoolean(key, false);
            }
            if (mOppoDebug) {
                Log.d(TAG, "getSoundTriggerDisabled: soundTriggerDisabled=" + soundTriggerDisabled);
            }
            return soundTriggerDisabled;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getSoundTriggerDisabled: Bad battery saver constants: " + settings);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLowPowerModeChangedInternal(boolean enabled) {
        if (this.mBatterySaveEnabled == enabled) {
            Log.d(TAG, "onLowPowerModeChanged: no change. ignore. enabled=" + enabled);
            return;
        }
        this.mBatterySaveEnabled = enabled;
        screenOffTimeSwitch(this.mContext, enabled);
        feedbadkSwitch(this.mContext, enabled);
        hypnusLowPowerModeOn(enabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void screenOffTimeSwitch(Context context, boolean enable) {
        if (!isClosedSuperFirewall(context.getPackageManager())) {
            long settingScreenOffTime = getScreenOffTimeValue(context);
            if (!enable) {
                long backupScreenOffTime = getBackupScreenOffTime(context);
                if (settingScreenOffTime == POWER_SAVER_MODE_SCREEN_OFF_TIME && backupScreenOffTime > POWER_SAVER_MODE_SCREEN_OFF_TIME) {
                    setScreenOffTimeValue(context, backupScreenOffTime);
                    if (mOppoDebug) {
                        Log.d(TAG, "screenOffTimeSwitch: restore screenoff time to " + backupScreenOffTime);
                    }
                }
            } else if (this.mIsSystemStartup) {
                if (mOppoDebug) {
                    Log.d(TAG, "screenOffTimeSwitch: SystemStartup. ignore");
                }
            } else if (isSceenoffTimeSwitchOn(context)) {
                backupScreenOffTime(context, settingScreenOffTime);
                if (settingScreenOffTime > POWER_SAVER_MODE_SCREEN_OFF_TIME) {
                    setScreenOffTimeValue(context, POWER_SAVER_MODE_SCREEN_OFF_TIME);
                    if (mOppoDebug) {
                        Log.d(TAG, "screenOffTimeSwitch: set screenoff time to 15000");
                    }
                }
            } else {
                backupScreenOffTime(context, -1);
            }
        }
    }

    private void feedbadkSwitch(Context context, boolean enable) {
        boolean backupState;
        if (!enable) {
            if (!getFeedbackChgByUser(context) && (backupState = getbackupHapticFeedback(context))) {
                setHapticFeedbackEnable(context, backupState);
            }
            context.getContentResolver().unregisterContentObserver(this.mFeedbackStateObserver);
        } else if (!this.mIsSystemStartup) {
            boolean isFeedbackEnabled = getHapticFeedbackEnable(context);
            backupHapticFeedback(context, isFeedbackEnabled);
            if (isFeedbackEnabled) {
                setHapticFeedbackEnable(context, false);
            }
            setFeedbackChgByUser(this.mContext, false);
            context.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_enabled"), false, this.mFeedbackStateObserver, -2);
        } else if (mOppoDebug) {
            Log.d(TAG, "feedbadkSwitch: SystemStartup. ignore");
        }
    }

    private long getScreenOffTimeValue(Context context) {
        return Settings.System.getLongForUser(context.getContentResolver(), "screen_off_timeout", 30000, -2);
    }

    private void setScreenOffTimeValue(Context context, long time) {
        Settings.System.putLongForUser(context.getContentResolver(), "screen_off_timeout", time, -2);
        if (mOppoDebug) {
            Log.d(TAG, "setScreenOffTimeValue: time=" + time);
        }
    }

    private void backupScreenOffTime(Context context, long time) {
        Settings.System.putLongForUser(context.getContentResolver(), BACKUP_SCREENOFF_TIME, time, -2);
        if (mOppoDebug) {
            Log.d(TAG, "backupScreenOffTime: time=" + time);
        }
    }

    private long getBackupScreenOffTime(Context context) {
        return Settings.System.getLongForUser(context.getContentResolver(), BACKUP_SCREENOFF_TIME, 30000, -2);
    }

    private void setHapticFeedbackEnable(Context context, boolean enable) {
        Settings.System.putIntForUser(context.getContentResolver(), "haptic_feedback_enabled", enable ? 1 : 0, -2);
        if (mOppoDebug) {
            Log.d(TAG, "setHapticFeedbackEnable: enable=" + enable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getHapticFeedbackEnable(Context context) {
        if (Settings.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 1) {
            return true;
        }
        return false;
    }

    private void backupHapticFeedback(Context context, boolean enable) {
        Settings.System.putIntForUser(context.getContentResolver(), BACKUP_FEEDBACK_STATE, enable ? 1 : 0, -2);
        if (mOppoDebug) {
            Log.d(TAG, "backupHapticFeedback: enable=" + enable);
        }
    }

    private boolean getbackupHapticFeedback(Context context) {
        if (Settings.System.getIntForUser(context.getContentResolver(), BACKUP_FEEDBACK_STATE, 0, -2) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFeedbackChgByUser(Context context, boolean change) {
        Settings.System.putIntForUser(context.getContentResolver(), IS_FEEDBACK_CHANGE_BY_USER, change ? 1 : 0, -2);
    }

    private boolean getFeedbackChgByUser(Context context) {
        if (Settings.System.getIntForUser(context.getContentResolver(), IS_FEEDBACK_CHANGE_BY_USER, 0, -2) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSceenoffTimeSwitchOn(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(), SREENOFF_TIME_SWITCH_STATE, 1, 0) != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hypnusLowPowerModeOn(boolean enable) {
        if (this.mHyp == null) {
            this.mHyp = new Hypnus();
        }
        if (enable && !this.mHypnusLowPowerenabled) {
            this.mHyp.hypnusSetNotification(3, 20);
            this.mHypnusLowPowerenabled = true;
            if (mOppoDebug) {
                Log.d(TAG, "hypnusLowPowerModeOn enable");
            }
        } else if (!enable && this.mHypnusLowPowerenabled) {
            this.mHyp.hypnusSetNotification(3, 21);
            this.mHypnusLowPowerenabled = false;
            if (mOppoDebug) {
                Log.d(TAG, "hypnusLowPowerModeOn disable");
            }
        }
    }

    /* access modifiers changed from: private */
    public class FeedbackStateObserver extends ContentObserver {
        public FeedbackStateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            ColorBatterySaveExtend colorBatterySaveExtend = ColorBatterySaveExtend.this;
            boolean enabled = colorBatterySaveExtend.getHapticFeedbackEnable(colorBatterySaveExtend.mContext);
            if (enabled) {
                ColorBatterySaveExtend colorBatterySaveExtend2 = ColorBatterySaveExtend.this;
                colorBatterySaveExtend2.setFeedbackChgByUser(colorBatterySaveExtend2.mContext, true);
                if (ColorBatterySaveExtend.mOppoDebug) {
                    Log.d(ColorBatterySaveExtend.TAG, "FeedbackStateObserver: status changed by user");
                }
            }
            if (ColorBatterySaveExtend.mOppoDebug) {
                Log.d(ColorBatterySaveExtend.TAG, "FeedbackStateObserver: enabled = " + enabled);
            }
        }
    }

    private class ScreenOffTimeSwitchObserver extends ContentObserver {
        public ScreenOffTimeSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            ColorBatterySaveExtend colorBatterySaveExtend = ColorBatterySaveExtend.this;
            boolean isSwithOn = colorBatterySaveExtend.isSceenoffTimeSwitchOn(colorBatterySaveExtend.mContext);
            if (ColorBatterySaveExtend.this.mBatterySaveEnabled) {
                ColorBatterySaveExtend colorBatterySaveExtend2 = ColorBatterySaveExtend.this;
                colorBatterySaveExtend2.screenOffTimeSwitch(colorBatterySaveExtend2.mContext, isSwithOn);
            }
            if (ColorBatterySaveExtend.mOppoDebug) {
                Log.d(ColorBatterySaveExtend.TAG, "screenoff time switch change: switch = " + isSwithOn + ", power save enable = " + ColorBatterySaveExtend.this.mBatterySaveEnabled);
            }
        }
    }
}
