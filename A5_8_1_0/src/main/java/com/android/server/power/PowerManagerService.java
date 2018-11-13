package com.android.server.power;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.media.AudioManager;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IPowerManager.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.OppoDisplayPerformanceHelper;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.KeyValueListParser;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.LockGuard;
import com.android.server.RescueParty;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoAppSwitchManager;
import com.android.server.am.OppoAppSwitchManager.ActivityChangedListener;
import com.android.server.am.OppoGameSpaceManager;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.oppo.LMServiceManager;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.oppo.hypnus.Hypnus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.util.Objects;
import oppo.util.OppoStatistics;

public final class PowerManagerService extends SystemService implements Monitor {
    private static String BLACKLIGHT_PATH = "/sys/devices/virtual/graphics/fb0/closebl";
    private static final int CANCEL_BACKLIGHT_DELAY_TIME = 2000;
    private static final int CNT_NO_PLAYBACK = 3;
    private static boolean DEBUG = false;
    private static boolean DEBUG_PANIC = false;
    private static boolean DEBUG_SPEW = (DEBUG);
    private static final int DEFAULT_DOUBLE_TAP_TO_WAKE = 0;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP_TIMEOUT = -1;
    private static final long DELAY_CHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 7000;
    private static final long DELAY_RECHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 3000;
    private static final int DELAY_REG_PHONESTATE = 500;
    private static final int DIRTY_ACTUAL_DISPLAY_POWER_STATE_UPDATED = 8;
    private static final int DIRTY_BATTERY_STATE = 256;
    private static final int DIRTY_BOOT_COMPLETED = 16;
    private static final int DIRTY_DOCK_STATE = 1024;
    private static final int DIRTY_IS_POWERED = 64;
    private static final int DIRTY_PROXIMITY_POSITIVE = 512;
    private static final int DIRTY_QUIESCENT = 4096;
    private static final int DIRTY_SCREEN_BRIGHTNESS_BOOST = 2048;
    private static final int DIRTY_SETTINGS = 32;
    private static final int DIRTY_STAY_ON = 128;
    private static final int DIRTY_USER_ACTIVITY = 4;
    private static final int DIRTY_VR_MODE_CHANGED = 8192;
    private static final int DIRTY_WAKEFULNESS = 2;
    protected static final int DIRTY_WAKE_LOCKS = 1;
    private static final int DUMP_WAKE_LOCKS_LIST_DELAY = 1800000;
    private static final int HALT_MODE_REBOOT = 1;
    private static final int HALT_MODE_REBOOT_SAFE_MODE = 2;
    private static final int HALT_MODE_SHUTDOWN = 0;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_MAINTAIN = 2;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_RECHECK = 3;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_RESETTED = 1;
    private static final String LAST_REBOOT_LOCATION = "/data/misc/reboot/last_reboot_reason";
    private static List<String> LIST_TAG_AUDIO_APP = Arrays.asList(new String[]{"android.media.MediaPlayer"});
    private static List<String> LIST_TAG_AUDIO_MEDIA_UID = Arrays.asList(new String[]{"AudioMix", "AudioDirectOut", "AudioOffload", "AudioDup", "AudioUnknown"});
    private static final int MAX_COUNT_REG_PHONESTATE = 40;
    static final long MIN_LONG_WAKE_CHECK_INTERVAL = 60000;
    private static final int MSG_BIOMETRICS_SET_ALPHA_TIMEOUT = 6;
    private static final int MSG_CHECK_FOR_LONG_WAKELOCKS = 4;
    private static final int MSG_DUMP_WAKE_LOCKS_LIST = 7;
    private static final int MSG_OPPO_PHONE_HEADSET_HANGUP = 5;
    private static final int MSG_SANDMAN = 2;
    private static final int MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 101;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 3;
    private static final int MSG_USER_ACTIVITY_TIMEOUT = 1;
    private static final int NOTIF_SRC_LOW_POWER_MODE = 3;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_OFF = 21;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_ON = 20;
    private static final int POWER_FEATURE_DOUBLE_TAP_TO_WAKE = 1;
    private static final String REASON_REBOOT = "reboot";
    private static final String REASON_SHUTDOWN = "shutdown";
    private static final String REASON_THERMAL_SHUTDOWN = "thermal-shutdown";
    private static final String REASON_USERREQUESTED = "userrequested";
    private static final long SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 15000;
    private static final int SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 5000;
    private static final int SCREEN_ON_LATENCY_WARNING_MS = 200;
    private static final String START_MMI_TEST_CMD = "oppo.intent.action.START_OPPO_AT_SERVER";
    private static final String STOP_MMI_TEST_CMD = "oppo.intent.action.STOP_OPPO_AT_SERVER";
    private static final String SYSTEM_PROPERTY_QUIESCENT = "ro.boot.quiescent";
    private static final String SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED = "sys.retaildemo.enabled";
    private static final String TAG = "PowerManagerService";
    private static final String TRACE_SCREEN_ON = "Screen turning on";
    public static final String UNBLOCK_REASON_GO_TO_SLEEP = "UNBLOCK_REASON_GO_TO_SLEEP";
    private static final int USER_ACTIVITY_SCREEN_BRIGHT = 1;
    private static final int USER_ACTIVITY_SCREEN_DIM = 2;
    private static final int USER_ACTIVITY_SCREEN_DREAM = 4;
    private static final int WAKE_LOCK_BUTTON_BRIGHT = 8;
    private static final int WAKE_LOCK_CPU = 1;
    private static final int WAKE_LOCK_DOZE = 64;
    private static final int WAKE_LOCK_DRAW = 128;
    private static final int WAKE_LOCK_PROXIMITY_SCREEN_OFF = 16;
    private static final int WAKE_LOCK_SCREEN_BRIGHT = 2;
    private static final int WAKE_LOCK_SCREEN_DIM = 4;
    private static final int WAKE_LOCK_STAY_AWAKE = 32;
    private static OppoBrightUtils mOppoBrightUtils;
    public static boolean mOppoShutdownIng = false;
    public static int mScreenBrightnessSettingMaximum;
    private static int mScreenBrightnessSettingMinimum;
    private static boolean sQuiescent;
    private final int AUDIO_WL_HELD_UID;
    private ActivityChangedListener mActivityChangedListener;
    private boolean mAlwaysOnEnabled;
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    private IAppOpsService mAppOps;
    private Light mAttentionLight;
    private boolean mAutoLowPowerModeConfigured;
    private boolean mAutoLowPowerModeSnoozing;
    private int mBatteryLevel;
    private boolean mBatteryLevelLow;
    private int mBatteryLevelWhenDreamStarted;
    private BatteryManagerInternal mBatteryManagerInternal;
    private final BatterySaverPolicy mBatterySaverPolicy;
    private IBatteryStats mBatteryStats;
    private BiometricsManagerInternal mBiometricsManager;
    private boolean mBootCompleted;
    private Runnable[] mBootCompletedRunnables;
    private final Runnable mCancelBacklightLimit;
    private AtomicInteger mCntNoPlayback;
    private int mCntPhoneStateReg;
    private AtomicBoolean mColorOsLowPowerModeEnabled;
    private final ArrayList<LowPowerModeListener> mColorOsLowPowerModeListeners;
    final Constants mConstants;
    private final Context mContext;
    private boolean mDecoupleHalAutoSuspendModeFromDisplayConfig;
    private boolean mDecoupleHalInteractiveModeFromDisplayConfig;
    private boolean mDeviceIdleMode;
    int[] mDeviceIdleTempWhitelist;
    int[] mDeviceIdleWhitelist;
    protected int mDirty;
    private DisplayManagerInternal mDisplayManagerInternal;
    private OppoDisplayPerformanceHelper mDisplayPerformanceHelper;
    private final DisplayPowerCallbacks mDisplayPowerCallbacks;
    private final DisplayPowerRequest mDisplayPowerRequest;
    private boolean mDisplayReady;
    private final SuspendBlocker mDisplaySuspendBlocker;
    private int mDockState;
    private boolean mDoubleTapWakeEnabled;
    private boolean mDozeAfterScreenOffConfig;
    private int mDozeScreenBrightnessOverrideFromDreamManager;
    private int mDozeScreenStateOverrideFromDreamManager;
    private DreamManagerInternal mDreamManager;
    private boolean mDreamsActivateOnDockSetting;
    private boolean mDreamsActivateOnSleepSetting;
    private boolean mDreamsActivatedOnDockByDefaultConfig;
    private boolean mDreamsActivatedOnSleepByDefaultConfig;
    private int mDreamsBatteryLevelDrainCutoffConfig;
    private int mDreamsBatteryLevelMinimumWhenNotPoweredConfig;
    private int mDreamsBatteryLevelMinimumWhenPoweredConfig;
    private boolean mDreamsEnabledByDefaultConfig;
    private boolean mDreamsEnabledOnBatteryConfig;
    private boolean mDreamsEnabledSetting;
    private boolean mDreamsSupportedConfig;
    private boolean mHalAutoSuspendModeEnabled;
    private boolean mHalInteractiveModeEnabled;
    private final PowerManagerHandler mHandler;
    private final ServiceThread mHandlerThread;
    private boolean mHoldingDisplaySuspendBlocker;
    private boolean mHoldingWakeLockSuspendBlocker;
    private Hypnus mHyp;
    private AtomicBoolean mHypnusLowPowerenabled;
    private boolean mIsMmiTesting;
    private boolean mIsPowered;
    private boolean mIsSellModeVersion;
    private boolean mIsVrModeEnabled;
    private KeyguardManager mKeyguardManager;
    private LMServiceManager mLMServiceManager;
    private long mLastInteractivePowerHintTime;
    private long mLastScreenBrightnessBoostTime;
    private long mLastSleepTime;
    private long mLastUserActivityTime;
    private long mLastUserActivityTimeNoChangeLights;
    private int mLastWakeLockSummary;
    private long mLastWakeTime;
    private long mLastWarningAboutUserActivityPermission;
    private boolean mLightDeviceIdleMode;
    private LightsManager mLightsManager;
    private final Object mLock;
    private boolean mLowPowerModeEnabled;
    private final ArrayList<LowPowerModeListener> mLowPowerModeListeners;
    private boolean mLowPowerModeSetting;
    private int mMaximumScreenDimDurationConfig;
    private float mMaximumScreenDimRatioConfig;
    private int mMaximumScreenOffTimeoutFromDeviceAdmin;
    private int mMinimumScreenOffTimeoutConfig;
    private Notifier mNotifier;
    private long mNotifyLongDispatched;
    private long mNotifyLongNextCheck;
    private long mNotifyLongScheduled;
    private boolean mOppoButtonReady;
    OppoHelper mOppoHelper;
    private long mOverriddenTimeout;
    private AtomicInteger mPhoneState;
    private boolean mPhoneStateRegSucess;
    PhoneStateListener mPhoneStatelistener;
    private int mPlugType;
    private WindowManagerPolicy mPolicy;
    private PowerMonitor mPowerMonitor;
    private boolean mProximityLockFromInCallUi;
    private boolean mProximityPositive;
    private boolean mRequestWaitForNegativeProximity;
    private final Runnable mRunPhoneStateRegister;
    private boolean mSandmanScheduled;
    private boolean mSandmanSummoned;
    private float mScreenAutoBrightnessAdjustmentSetting;
    private boolean mScreenBrightnessBoostInProgress;
    private int mScreenBrightnessForVrSetting;
    private int mScreenBrightnessForVrSettingDefault;
    private int mScreenBrightnessModeSetting;
    private int mScreenBrightnessOverrideFromWindowManager;
    private int mScreenBrightnessSetting;
    private int mScreenBrightnessSettingDefault;
    private int mScreenOffReason;
    private int mScreenOffTimeoutSetting;
    ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    private SettingsObserver mSettingsObserver;
    private int mSleepTimeoutSetting;
    private boolean mStartGoToSleep;
    private boolean mStayOn;
    private int mStayOnWhilePluggedInSetting;
    private boolean mSupportsDoubleTapWakeConfig;
    private final ArrayList<SuspendBlocker> mSuspendBlockers;
    private boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mSystemReady;
    private float mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
    private int mTemporaryScreenBrightnessSettingOverride;
    private boolean mTheaterModeEnabled;
    private final SparseArray<UidState> mUidState;
    private boolean mUidsChanged;
    private boolean mUidsChanging;
    private int mUserActivitySummary;
    private long mUserActivityTimeoutOverrideFromWindowManager;
    private boolean mUserInactiveOverrideFromWindowManager;
    private final IVrStateCallbacks mVrStateCallbacks;
    private OppoWakeLockCheck mWakeLockCheck;
    private int mWakeLockSummary;
    private final SuspendBlocker mWakeLockSuspendBlocker;
    protected final ArrayList<WakeLock> mWakeLocks;
    private boolean mWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig;
    private int mWakefulness;
    private boolean mWakefulnessChanging;
    private WirelessChargerDetector mWirelessChargerDetector;
    private QCNsrmPowerExtension qcNsrmPowExt;
    private boolean useProximityForceSuspend;

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(PowerManagerService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new PowerManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uid) {
            if (uid < 0) {
                uid = Binder.getCallingUid();
            }
            acquireWakeLock(lock, flags, tag, packageName, new WorkSource(uid), null);
        }

        public void powerHint(int hintId, int data) {
            if (PowerManagerService.this.mSystemReady) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                PowerManagerService.this.powerHintInternal(hintId, data);
            }
        }

        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            } else if (packageName == null) {
                throw new IllegalArgumentException("packageName must not be null");
            } else {
                PowerManager.validateWakeLockParameters(flags, tag);
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if ((flags & 64) != 0) {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                }
                if (ws == null || ws.size() == 0) {
                    ws = null;
                } else {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                }
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.acquireWakeLockInternal(lock, flags, tag, packageName, ws, historyTag, uid, pid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void releaseWakeLock(IBinder lock, int flags) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.releaseWakeLockInternal(lock, flags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void updateWakeLockUids(IBinder lock, int[] uids) {
            WorkSource ws = null;
            if (uids != null) {
                ws = new WorkSource();
                for (int add : uids) {
                    ws.add(add);
                }
            }
            updateWakeLockWorkSource(lock, ws, null);
        }

        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
            if (ws == null || ws.size() == 0) {
                ws = null;
            } else {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
            }
            int callingUid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.updateWakeLockWorkSourceInternal(lock, ws, historyTag, callingUid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isWakeLockLevelSupported(int level) {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap14 = PowerManagerService.this.isWakeLockLevelSupportedInternal(level);
                return -wrap14;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void userActivity(long eventTime, int event, int flags) {
            long now = SystemClock.uptimeMillis();
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 && PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.USER_ACTIVITY") != 0) {
                synchronized (PowerManagerService.this.mLock) {
                    if (now >= PowerManagerService.this.mLastWarningAboutUserActivityPermission + RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL) {
                        PowerManagerService.this.mLastWarningAboutUserActivityPermission = now;
                        Slog.w(PowerManagerService.TAG, "Ignoring call to PowerManager.userActivity() because the caller does not have DEVICE_POWER or USER_ACTIVITY permission.  Please fix your app!   pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid());
                    }
                }
            } else if (eventTime > now) {
                throw new IllegalArgumentException("event time must not be in the future");
            } else {
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.userActivityInternal(eventTime, event, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void wakeUp(long eventTime, String reason, String opPackageName) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.wakeUpInternal(eventTime, reason, uid, opPackageName, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void goToSleep(long eventTime, int reason, int flags) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.goToSleepInternal(eventTime, reason, flags, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void nap(long eventTime) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.napInternal(eventTime, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isInteractive() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap11 = PowerManagerService.this.isInteractiveInternal();
                return -wrap11;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap12 = PowerManagerService.this.isLowPowerModeInternal();
                return -wrap12;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean getDisplayAodStatus() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap2 = PowerManagerService.this.getDisplayAodStatusInternal();
                return -wrap2;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isColorOsPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap8 = PowerManagerService.this.isColorOsLowPowerModeInternal();
                return -wrap8;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getScreenState() {
            long ident = Binder.clearCallingIdentity();
            try {
                int -wrap19 = PowerManagerService.this.getScreenStateInternal();
                return -wrap19;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getMinimumScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                int -get27 = PowerManagerService.mScreenBrightnessSettingMinimum;
                return -get27;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getMaximumScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                int i = PowerManagerService.mScreenBrightnessSettingMaximum;
                return i;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getDefaultScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                int -get26 = PowerManagerService.this.mScreenBrightnessSettingDefault;
                return -get26;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public PowerSaveState getPowerSaveState(int serviceType) {
            long ident = Binder.clearCallingIdentity();
            try {
                PowerSaveState batterySaverPolicy;
                synchronized (PowerManagerService.this.mLock) {
                    batterySaverPolicy = PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType, PowerManagerService.this.isLowPowerModeInternal());
                }
                return batterySaverPolicy;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPowerSaveMode(boolean mode) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap16 = PowerManagerService.this.setLowPowerModeInternal(mode);
                return -wrap16;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setColorOsPowerSaveMode(boolean mode) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap15 = PowerManagerService.this.setColorOsLowPowerModeInternal(mode);
                return -wrap15;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean isDeviceIdleModeInternal = PowerManagerService.this.isDeviceIdleModeInternal();
                return isDeviceIdleModeInternal;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isLightDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean isLightDeviceIdleModeInternal = PowerManagerService.this.isLightDeviceIdleModeInternal();
                return isLightDeviceIdleModeInternal;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getLastShutdownReason() {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                int lastShutdownReasonInternal = PowerManagerService.this.getLastShutdownReasonInternal(new File(PowerManagerService.LAST_REBOOT_LOCATION));
                return lastShutdownReasonInternal;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void reboot(boolean confirm, String reason, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            if ("recovery".equals(reason) || "recovery-update".equals(reason)) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(1, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void rebootSafeMode(boolean confirm, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(2, confirm, "safemode", wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void shutdown(boolean confirm, String reason, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(0, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void crash(String message) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.crashInternal(message);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setStayOnSetting(int val) {
            int uid = Binder.getCallingUid();
            if (uid == 0 || Settings.checkAndNoteWriteSettingsOperation(PowerManagerService.this.mContext, uid, Settings.getPackageNameForUid(PowerManagerService.this.mContext, uid), true)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.setStayOnSettingInternal(val);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void setTemporaryScreenBrightnessSettingOverride(int brightness) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setTemporaryScreenBrightnessSettingOverrideInternal(brightness);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(float adj) {
            HashSet<Integer> targetUids = new HashSet();
            try {
                PackageManager pm = PowerManagerService.this.mContext.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo("com.coloros.gallery3d", 1);
                if (ai != null) {
                    targetUids.add(Integer.valueOf(ai.uid));
                }
                ai = pm.getApplicationInfo("com.coloros.video", 1);
                if (ai != null) {
                    targetUids.add(Integer.valueOf(ai.uid));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            int uid = Binder.getCallingUid();
            Integer uid_integer = new Integer(uid);
            if (PowerManagerService.DEBUG) {
                Slog.d(PowerManagerService.TAG, "setTemporaryScreenAutoBrightnessAdjustmentSettingOverride: targetUids = " + targetUids + ", uid = " + uid);
            }
            if (targetUids.size() <= 0 || !targetUids.contains(uid_integer)) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal(adj);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setAttentionLight(boolean on, int color) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setAttentionLightInternal(on, color);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void boostScreenBrightness(long eventTime) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.boostScreenBrightnessInternal(eventTime, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isScreenBrightnessBoosted() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap13 = PowerManagerService.this.isScreenBrightnessBoostedInternal();
                return -wrap13;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(PowerManagerService.this.mContext, PowerManagerService.TAG, pw) && !PowerManagerService.this.dynamicallyConfigPowerManagerServiceLogTag(fd, pw, args)) {
                long ident = Binder.clearCallingIdentity();
                boolean isDumpProto = false;
                for (String arg : args) {
                    if (arg.equals("--proto")) {
                        isDumpProto = true;
                    }
                }
                if (!PowerManagerService.this.dumpPossibleMusicPlayer(fd, pw, args)) {
                    if (isDumpProto) {
                        try {
                            PowerManagerService.this.dumpProto(fd);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else {
                        PowerManagerService.this.dumpInternal(pw);
                    }
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void updateBlockedUids(int uid, boolean isBlocked) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.qcNsrmPowExt.processPmsBlockedUid(uid, isBlocked, PowerManagerService.this.mWakeLocks);
            }
        }

        public long getFrameworksBlockedTime() {
            long ident = Binder.clearCallingIdentity();
            try {
                long frameworksBlockedTime = PowerManagerService.this.mPowerMonitor.getFrameworksBlockedTime();
                return frameworksBlockedTime;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public Map getTopAppBlocked(int n) {
            long ident = Binder.clearCallingIdentity();
            try {
                Map topAppBlocked = PowerManagerService.this.mPowerMonitor.getTopAppBlocked(n);
                return topAppBlocked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void stopChargeForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            if (uid != 1000 && uid != 0) {
                throw new SecurityException("No permission to stopChargeForSale");
            } else if (PowerManagerService.this.mIsSellModeVersion) {
                try {
                    PowerManagerService.this.updateChargeStateForSaleInternal(false);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Slog.d(PowerManagerService.TAG, "Only support for sell mode version");
            }
        }

        public void resumeChargeForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            if (uid != 1000 && uid != 0) {
                throw new SecurityException("No permission to resumeChargeForSale");
            } else if (PowerManagerService.this.mIsSellModeVersion) {
                try {
                    PowerManagerService.this.updateChargeStateForSaleInternal(true);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Slog.d(PowerManagerService.TAG, "Only support for sell mode version");
            }
        }

        public int getCurrentChargeStateForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int -wrap18 = PowerManagerService.this.getCurrentChargeStateForSaleInternal();
                return -wrap18;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final class Constants extends ContentObserver {
        private static final boolean DEFAULT_NO_CACHED_WAKE_LOCKS = true;
        private static final String KEY_NO_CACHED_WAKE_LOCKS = "no_cached_wake_locks";
        public boolean NO_CACHED_WAKE_LOCKS = true;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("power_manager_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (PowerManagerService.this.mLock) {
                try {
                    this.mParser.setString(Global.getString(this.mResolver, "power_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(PowerManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.NO_CACHED_WAKE_LOCKS = this.mParser.getBoolean(KEY_NO_CACHED_WAKE_LOCKS, true);
            }
            return;
        }

        void dump(PrintWriter pw) {
            pw.println("  Settings power_manager_constants:");
            pw.print("    ");
            pw.print(KEY_NO_CACHED_WAKE_LOCKS);
            pw.print("=");
            pw.println(this.NO_CACHED_WAKE_LOCKS);
        }

        void dumpProto(ProtoOutputStream proto) {
            long constantsToken = proto.start(1172526071809L);
            proto.write(1155346202625L, this.NO_CACHED_WAKE_LOCKS);
            proto.end(constantsToken);
        }
    }

    private final class LocalService extends PowerManagerInternal {
        /* synthetic */ LocalService(PowerManagerService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void setScreenBrightnessOverrideFromWindowManager(int screenBrightness) {
            if (screenBrightness < -1 || screenBrightness > PowerManager.BRIGHTNESS_MULTIBITS_ON) {
                screenBrightness = -1;
            }
            PowerManagerService.this.setScreenBrightnessOverrideFromWindowManagerInternal(screenBrightness);
        }

        public void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
            switch (screenState) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    break;
                default:
                    screenState = 0;
                    break;
            }
            if (screenBrightness < -1 || screenBrightness > PowerManager.BRIGHTNESS_MULTIBITS_ON) {
                screenBrightness = -1;
            }
            PowerManagerService.this.setDozeOverrideFromDreamManagerInternal(screenState, screenBrightness);
        }

        public void setUserInactiveOverrideFromWindowManager() {
            PowerManagerService.this.setUserInactiveOverrideFromWindowManagerInternal();
        }

        public void setUserActivityTimeoutOverrideFromWindowManager(long timeoutMillis) {
            PowerManagerService.this.setUserActivityTimeoutOverrideFromWindowManagerInternal(timeoutMillis);
        }

        public void setMaximumScreenOffTimeoutFromDeviceAdmin(int timeMs) {
            PowerManagerService.this.setMaximumScreenOffTimeoutFromDeviceAdminInternal(timeMs);
        }

        public PowerSaveState getLowPowerState(int serviceType) {
            PowerSaveState batterySaverPolicy;
            synchronized (PowerManagerService.this.mLock) {
                batterySaverPolicy = PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType, PowerManagerService.this.mLowPowerModeEnabled);
            }
            return batterySaverPolicy;
        }

        public void registerLowPowerModeObserver(LowPowerModeListener listener) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mLowPowerModeListeners.add(listener);
            }
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
        public int[] getWakeLockedPids() {
            int[] res;
            synchronized (PowerManagerService.this.mLock) {
                int N = PowerManagerService.this.mWakeLocks.size();
                res = new int[N];
                for (int i = 0; i < N; i++) {
                    res[i] = ((WakeLock) PowerManagerService.this.mWakeLocks.get(i)).mOwnerPid;
                }
            }
            return res;
        }

        public void registerColorOsLowPowerModeObserver(LowPowerModeListener listener) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mColorOsLowPowerModeListeners.add(listener);
            }
        }

        public boolean getColorOsLowPowerModeEnabled() {
            boolean z;
            synchronized (PowerManagerService.this.mLock) {
                z = PowerManagerService.this.mColorOsLowPowerModeEnabled.get();
            }
            return z;
        }

        public boolean setDeviceIdleMode(boolean enabled) {
            return PowerManagerService.this.setDeviceIdleModeInternal(enabled);
        }

        public boolean setLightDeviceIdleMode(boolean enabled) {
            return PowerManagerService.this.setLightDeviceIdleModeInternal(enabled);
        }

        public void setDeviceIdleWhitelist(int[] appids) {
            PowerManagerService.this.setDeviceIdleWhitelistInternal(appids);
        }

        public void setDeviceIdleTempWhitelist(int[] appids) {
            PowerManagerService.this.setDeviceIdleTempWhitelistInternal(appids);
        }

        public void startUidChanges() {
            PowerManagerService.this.startUidChangesInternal();
        }

        public void finishUidChanges() {
            PowerManagerService.this.finishUidChangesInternal();
        }

        public void updateUidProcState(int uid, int procState) {
            PowerManagerService.this.updateUidProcStateInternal(uid, procState);
        }

        public void uidGone(int uid) {
            PowerManagerService.this.uidGoneInternal(uid);
        }

        public void uidActive(int uid) {
            PowerManagerService.this.uidActiveInternal(uid);
        }

        public void uidIdle(int uid) {
            PowerManagerService.this.uidIdleInternal(uid);
        }

        public void powerHint(int hintId, int data) {
            PowerManagerService.this.powerHintInternal(hintId, data);
        }

        public boolean isStartGoToSleep() {
            return PowerManagerService.this.mStartGoToSleep;
        }

        public void wakeUpAndBlockScreenOn(String reason) {
            Slog.d(PowerManagerService.TAG, "wakeUpAndBlockScreenOn reason= " + reason);
            PowerManagerService.this.startWakeUpAndBlockScreenOn(reason);
        }

        public void unblockScreenOn(String reason) {
            Slog.d(PowerManagerService.TAG, "unblockScreenOn reason= " + reason);
            PowerManagerService.this.startUnblockScreenOn(reason);
        }

        public void gotoSleepWhenScreenOnBlocked(String reason) {
            Slog.d(PowerManagerService.TAG, "gotoSleepWhenScreenOnBlocked reason= " + reason);
            PowerManagerService.this.startGotoSleepWhenScreenOnBlocked(reason);
        }

        public boolean isBiometricsWakeUpReason(String reason) {
            return PowerManagerService.this.isBiometricsBlockReason(reason);
        }

        public boolean isFingerprintWakeUpReason(String reason) {
            return PowerManagerService.this.isFingerprintBlockReason(reason);
        }

        public boolean isFaceWakeUpReason(String reason) {
            return PowerManagerService.this.isFaceBlockReason(reason);
        }

        public boolean isBlockedByFace() {
            return PowerManagerService.this.hasBlockedByFace();
        }

        public boolean isBlockedByFingerprint() {
            return PowerManagerService.this.hasBlockedByFingerprint();
        }
    }

    private final class PowerManagerHandler extends Handler {
        public PowerManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PowerManagerService.this.handleUserActivityTimeout();
                    return;
                case 2:
                    PowerManagerService.this.handleSandman();
                    return;
                case 3:
                    PowerManagerService.this.handleScreenBrightnessBoostTimeout();
                    return;
                case 4:
                    PowerManagerService.this.checkForLongWakeLocks();
                    return;
                case 5:
                    Slog.d(PowerManagerService.TAG, "PowerMS OppoPhoneHeadsetReceiver: HANDLE MES");
                    PowerManagerService.this.goToSleepInternal(SystemClock.uptimeMillis(), 4, 0, 1000);
                    return;
                case 6:
                    if (PowerManagerService.this.mDisplayManagerInternal != null) {
                        if (PowerManagerService.DEBUG) {
                            Slog.d(PowerManagerService.TAG, "unblockScreenOnByBiometrics, alpha has been changed");
                        }
                        PowerManagerService.this.mDisplayManagerInternal.unblockScreenOnByBiometrics("MSG_BIOMETRICS_SET_ALPHA_TIMEOUT");
                    }
                    PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                    android.os.PowerManager.WakeLock partial = msg.obj;
                    if (partial != null) {
                        partial.release();
                        return;
                    }
                    return;
                case 7:
                    synchronized (PowerManagerService.this.mLock) {
                        PowerManagerService.this.dumpWakeLockLocked();
                    }
                    PowerManagerService.this.tryToTrackWakelocks();
                    return;
                case 101:
                    if (PowerManagerService.this.mKeyguardManager != null && (PowerManagerService.this.mKeyguardManager.isKeyguardLocked() ^ 1) == 0) {
                        HashMap<String, String> eventMap = new HashMap();
                        int ret = PowerManagerService.this.keyguardLockedScreenoffTimeout(eventMap);
                        if (1 == ret) {
                            PowerManagerService.this.uploadScreenoffTimeoutDcs(eventMap, true);
                            return;
                        } else if (2 == ret) {
                            PowerManagerService.this.uploadScreenoffTimeoutDcs(eventMap, false);
                            return;
                        } else if (3 == ret) {
                            PowerManagerService.this.mHandler.sendEmptyMessageDelayed(101, PowerManagerService.DELAY_RECHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED);
                            if (PowerManagerService.DEBUG_PANIC) {
                                Slog.d(PowerManagerService.TAG, "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED, recheck");
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    } else if (PowerManagerService.DEBUG_PANIC) {
                        Slog.d(PowerManagerService.TAG, "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED, not in keyguard.");
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private final class SuspendBlockerImpl implements SuspendBlocker {
        private final String mName;
        private int mReferenceCount;
        private final String mTraceName;

        public SuspendBlockerImpl(String name) {
            this.mName = name;
            this.mTraceName = "SuspendBlocker (" + name + ")";
        }

        protected void finalize() throws Throwable {
            try {
                if (this.mReferenceCount != 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was finalized without being released!");
                    this.mReferenceCount = 0;
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        public void acquire() {
            synchronized (this) {
                this.mReferenceCount++;
                if (this.mReferenceCount == 1) {
                    if (PowerManagerService.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Acquiring suspend blocker \"" + this.mName + "\".");
                    }
                    Trace.asyncTraceBegin(131072, this.mTraceName, 0);
                    PowerManagerService.nativeAcquireSuspendBlocker(this.mName);
                    PowerManagerService.this.mPowerMonitor.acquireSuspendBlocker(this.mName);
                }
            }
        }

        public void release() {
            synchronized (this) {
                this.mReferenceCount--;
                if (this.mReferenceCount == 0) {
                    if (PowerManagerService.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Releasing suspend blocker \"" + this.mName + "\".");
                    }
                    PowerManagerService.this.mPowerMonitor.releaseSuspendBlocker(this.mName);
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                } else if (this.mReferenceCount < 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was released without being acquired!", new Throwable());
                    this.mReferenceCount = 0;
                }
            }
        }

        public String toString() {
            String str;
            synchronized (this) {
                str = this.mName + ": ref count=" + this.mReferenceCount;
            }
            return str;
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long sbToken = proto.start(fieldId);
            synchronized (this) {
                proto.write(1159641169921L, this.mName);
                proto.write(1112396529666L, this.mReferenceCount);
            }
            proto.end(sbToken);
        }
    }

    private final class BatteryReceiver extends BroadcastReceiver {
        /* synthetic */ BatteryReceiver(PowerManagerService this$0, BatteryReceiver -this1) {
            this();
        }

        private BatteryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleBatteryStateChangedLocked();
            }
        }
    }

    private final class DockReceiver extends BroadcastReceiver {
        /* synthetic */ DockReceiver(PowerManagerService this$0, DockReceiver -this1) {
            this();
        }

        private DockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                if (PowerManagerService.this.mDockState != dockState) {
                    PowerManagerService.this.mDockState = dockState;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= 1024;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }
        }
    }

    private final class DreamReceiver extends BroadcastReceiver {
        /* synthetic */ DreamReceiver(PowerManagerService this$0, DreamReceiver -this1) {
            this();
        }

        private DreamReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.scheduleSandmanLocked();
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HaltMode {
    }

    class OppoHelper {
        private static final int BUTTON_LIGHT_BRIGHTNESS = 102;
        Light mButtonLight;

        int getUserActivitySumm() {
            return PowerManagerService.this.mUserActivitySummary;
        }

        int getWakefulness() {
            return PowerManagerService.this.mWakefulness;
        }

        int getScreenBrightDefault() {
            return PowerManagerService.this.mScreenBrightnessSettingDefault;
        }

        public OppoHelper(LightsManager mLightsManager) {
            this.mButtonLight = mLightsManager.getLight(2);
        }

        void updateButtonBrightness(PowerManagerService service, boolean condition) {
            if (!PowerManagerService.mOppoShutdownIng && !PowerManagerService.this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                int tmp = getUserActivitySumm();
                boolean b1 = (tmp & 2) != 0;
                boolean b2 = tmp == 0;
                boolean b = getWakefulness() == 1;
                if (!condition) {
                    this.mButtonLight.setBrightness(102);
                } else if (!(!b || (b1 ^ 1) == 0 || (b2 ^ 1) == 0)) {
                    this.mButtonLight.setBrightness(102);
                }
            }
        }

        void turnOffButtonLight() {
            this.mButtonLight.turnOff();
        }
    }

    private final class OppoPhoneHeadsetReceiver extends BroadcastReceiver {
        /* synthetic */ OppoPhoneHeadsetReceiver(PowerManagerService this$0, OppoPhoneHeadsetReceiver -this1) {
            this();
        }

        private OppoPhoneHeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Slog.d(PowerManagerService.TAG, "PowerMS OppoPhoneHeadsetReceiver: oppo.action.phone.headset.hangup");
            if (PowerManagerService.this.mProximityPositive && (PowerManagerService.this.mWakeLockSummary & 16) == 0) {
                Message msg = PowerManagerService.this.mHandler.obtainMessage(5);
                msg.setAsynchronous(true);
                Slog.d(PowerManagerService.TAG, "PowerMS OppoPhoneHeadsetReceiver: SEND MES");
                PowerManagerService.this.mHandler.sendMessageDelayed(msg, 2000);
                Slog.d(PowerManagerService.TAG, "PowerMS OppoPhoneHeadsetReceiver: SEND MES DONE");
            }
        }
    }

    private final class OppoShutDownReceiver extends BroadcastReceiver {
        /* synthetic */ OppoShutDownReceiver(PowerManagerService this$0, OppoShutDownReceiver -this1) {
            this();
        }

        private OppoShutDownReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (PowerManagerService.DEBUG_PANIC) {
                Slog.d(PowerManagerService.TAG, "PowerMS NotSleepingWhenShutdowning: received a shutdown broadcast");
            }
            PowerManagerService.mOppoShutdownIng = true;
            if (PowerManagerService.this.mOppoHelper != null) {
                PowerManagerService.this.mOppoHelper.turnOffButtonLight();
            }
        }
    }

    private final class OppoUserPresentReceiver extends BroadcastReceiver {
        /* synthetic */ OppoUserPresentReceiver(PowerManagerService this$0, OppoUserPresentReceiver -this1) {
            this();
        }

        private OppoUserPresentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (PowerManagerService.DEBUG_PANIC) {
                Slog.d(PowerManagerService.TAG, "action:" + intent.getAction());
            }
            synchronized (PowerManagerService.this.mLock) {
                if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    PowerManagerService.this.mHandler.removeMessages(101);
                } else if (PowerManagerService.START_MMI_TEST_CMD.equals(intent.getAction())) {
                    PowerManagerService.this.mIsMmiTesting = true;
                } else if (PowerManagerService.STOP_MMI_TEST_CMD.equals(intent.getAction())) {
                    PowerManagerService.this.mIsMmiTesting = false;
                }
            }
        }
    }

    class PowerMonitor {
        private static final String CPUBLOCKER = "PowerManagerService.WakeLocks";
        private static final int MAX_APP_WAKELOCK_SIZE_LIMIT = 1000;
        private Map<String, Long> mAppWakeupMap = new ArrayMap();
        private long mFrameworksBlockedTime = 0;
        private long mLastBlockedTime = -1;
        private long mLastScreenOffTime = 0;

        PowerMonitor() {
        }

        public void screenOff() {
            this.mLastScreenOffTime = SystemClock.uptimeMillis();
            this.mLastBlockedTime = SystemClock.uptimeMillis();
            this.mFrameworksBlockedTime = 0;
        }

        public void screenOn() {
            if (this.mLastBlockedTime != -1) {
                this.mFrameworksBlockedTime += SystemClock.uptimeMillis() - this.mLastBlockedTime;
                this.mLastBlockedTime = -1;
            }
            long now = SystemClock.uptimeMillis();
            int numWakeLocks = PowerManagerService.this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = (WakeLock) PowerManagerService.this.mWakeLocks.get(i);
                if (PowerManagerService.isCPULock(wakeLock) && (wakeLock.mDisabled ^ 1) != 0) {
                    long total_time = now - wakeLock.mActiveSince;
                    if (this.mFrameworksBlockedTime <= total_time) {
                        total_time = this.mFrameworksBlockedTime;
                    }
                    releaseWakeLock(wakeLock.mPackageName, wakeLock.mTag, total_time);
                }
            }
        }

        public void acquireSuspendBlocker(String name) {
            if (name.equals(CPUBLOCKER) && (PowerManagerService.this.isInteractiveInternal() ^ 1) != 0 && this.mLastBlockedTime == -1) {
                this.mLastBlockedTime = SystemClock.uptimeMillis();
            }
        }

        public void releaseSuspendBlocker(String name) {
            if (name.equals(CPUBLOCKER) && (PowerManagerService.this.isInteractiveInternal() ^ 1) != 0) {
                long releaseTime = SystemClock.uptimeMillis();
                if (this.mLastScreenOffTime > this.mLastBlockedTime) {
                    this.mFrameworksBlockedTime += releaseTime - this.mLastScreenOffTime;
                } else {
                    this.mFrameworksBlockedTime += releaseTime - this.mLastBlockedTime;
                }
                this.mLastBlockedTime = -1;
            }
        }

        public void acquireWakeLock(String packageName, String tag, int level) {
        }

        public void releaseWakeLock(String packageName, String tag, long totalTime) {
            String key = packageName + ":" + tag;
            long screenOffTime = SystemClock.uptimeMillis() - this.mLastScreenOffTime;
            long wakeLockTime = screenOffTime <= totalTime ? screenOffTime : totalTime;
            synchronized (this.mAppWakeupMap) {
                if (this.mAppWakeupMap.containsKey(key)) {
                    this.mAppWakeupMap.put(key, Long.valueOf(((Long) this.mAppWakeupMap.get(key)).longValue() + wakeLockTime));
                } else if (this.mAppWakeupMap.size() < 1000) {
                    this.mAppWakeupMap.put(key, Long.valueOf(wakeLockTime));
                }
            }
        }

        public long getFrameworksBlockedTime() {
            return this.mFrameworksBlockedTime;
        }

        public Map getTopAppBlocked(int n) {
            if (n < 1) {
                return null;
            }
            Map<String, Long> appWakeupResult = new ArrayMap();
            synchronized (this.mAppWakeupMap) {
                List<Entry<String, Long>> appWakeupList = new ArrayList(new HashMap(this.mAppWakeupMap).entrySet());
                Collections.sort(appWakeupList, new Comparator<Entry<String, Long>>() {
                    public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                        long v2 = ((Long) o2.getValue()).longValue();
                        long v1 = ((Long) o1.getValue()).longValue();
                        if (v2 > v1) {
                            return 1;
                        }
                        if (v1 > v2) {
                            return -1;
                        }
                        return 0;
                    }
                });
                int limit = n < appWakeupList.size() ? n : appWakeupList.size();
                for (int i = 0; i < limit; i++) {
                    Entry<String, Long> m = (Entry) appWakeupList.get(i);
                    appWakeupResult.put(((String) m.getKey()).toString(), (Long) m.getValue());
                }
            }
            return appWakeupResult;
        }

        public void clear() {
            this.mLastScreenOffTime = 0;
            this.mFrameworksBlockedTime = 0;
            this.mLastBlockedTime = -1;
            synchronized (this.mAppWakeupMap) {
                this.mAppWakeupMap.clear();
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleSettingsChangedLocked();
            }
        }
    }

    static final class UidState {
        boolean mActive;
        int mNumWakeLocks;
        int mProcState;
        final int mUid;

        UidState(int uid) {
            this.mUid = uid;
        }
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        /* synthetic */ UserSwitchedReceiver(PowerManagerService this$0, UserSwitchedReceiver -this1) {
            this();
        }

        private UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleSettingsChangedLocked();
            }
        }
    }

    protected final class WakeLock implements DeathRecipient {
        public long mAcquireTime;
        public long mActiveSince = 0;
        public boolean mDisabled;
        public int mFlags;
        public String mHistoryTag;
        public final IBinder mLock;
        public boolean mNotifiedAcquired;
        public boolean mNotifiedLong;
        public final int mOwnerPid;
        public final int mOwnerUid;
        public final String mPackageName;
        public String mTag;
        public long mTotalTime = 0;
        public final UidState mUidState;
        public WorkSource mWorkSource;

        public WakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid, UidState uidState) {
            this.mLock = lock;
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
            this.mHistoryTag = historyTag;
            this.mOwnerUid = ownerUid;
            this.mOwnerPid = ownerPid;
            this.mUidState = uidState;
        }

        public void binderDied() {
            PowerManagerService.this.handleWakeLockDeath(this);
        }

        public boolean hasSameProperties(int flags, String tag, WorkSource workSource, int ownerUid, int ownerPid) {
            if (this.mFlags == flags && this.mTag.equals(tag) && hasSameWorkSource(workSource) && this.mOwnerUid == ownerUid && this.mOwnerPid == ownerPid) {
                return true;
            }
            return false;
        }

        public void updateProperties(int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            if (!this.mPackageName.equals(packageName)) {
                throw new IllegalStateException("Existing wake lock package name changed: " + this.mPackageName + " to " + packageName);
            } else if (this.mOwnerUid != ownerUid) {
                throw new IllegalStateException("Existing wake lock uid changed: " + this.mOwnerUid + " to " + ownerUid);
            } else if (this.mOwnerPid != ownerPid) {
                throw new IllegalStateException("Existing wake lock pid changed: " + this.mOwnerPid + " to " + ownerPid);
            } else {
                this.mFlags = flags;
                this.mTag = tag;
                updateWorkSource(workSource);
                this.mHistoryTag = historyTag;
            }
        }

        public boolean hasSameWorkSource(WorkSource workSource) {
            return Objects.equal(this.mWorkSource, workSource);
        }

        public void updateWorkSource(WorkSource workSource) {
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getLockLevelString());
            sb.append(" '");
            sb.append(this.mTag);
            sb.append("'");
            sb.append(getLockFlagsString());
            if (this.mDisabled) {
                sb.append(" DISABLED");
            }
            if (this.mNotifiedAcquired) {
                sb.append(" ACQ=");
                TimeUtils.formatDuration(this.mAcquireTime - SystemClock.uptimeMillis(), sb);
            }
            if (this.mNotifiedLong) {
                sb.append(" LONG");
            }
            sb.append(" (uid=");
            sb.append(this.mOwnerUid);
            if (this.mOwnerPid != 0) {
                sb.append(" pid=");
                sb.append(this.mOwnerPid);
            }
            if (this.mWorkSource != null) {
                sb.append(" ws=");
                sb.append(this.mWorkSource);
            }
            sb.append(")");
            return sb.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            boolean z = true;
            long wakeLockToken = proto.start(fieldId);
            proto.write(1168231104513L, this.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI);
            proto.write(1159641169922L, this.mTag);
            long wakeLockFlagsToken = proto.start(1172526071811L);
            proto.write(1155346202625L, (this.mFlags & 268435456) != 0);
            if ((this.mFlags & 536870912) == 0) {
                z = false;
            }
            proto.write(1155346202626L, z);
            proto.end(wakeLockFlagsToken);
            proto.write(1155346202628L, this.mDisabled);
            if (this.mNotifiedAcquired) {
                proto.write(1116691496965L, this.mAcquireTime);
            }
            proto.write(1155346202630L, this.mNotifiedLong);
            proto.write(1112396529671L, this.mOwnerUid);
            proto.write(1112396529672L, this.mOwnerPid);
            if (this.mWorkSource != null) {
                this.mWorkSource.writeToProto(proto, 1172526071817L);
            }
            proto.end(wakeLockToken);
        }

        public String getLockLevelString() {
            switch (this.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                case 1:
                    return "PARTIAL_WAKE_LOCK             ";
                case 6:
                    return "SCREEN_DIM_WAKE_LOCK          ";
                case 10:
                    return "SCREEN_BRIGHT_WAKE_LOCK       ";
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    return "FULL_WAKE_LOCK                ";
                case 32:
                    return "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
                case 64:
                    return "DOZE_WAKE_LOCK                ";
                case 128:
                    return "DRAW_WAKE_LOCK                ";
                default:
                    return "???                           ";
            }
        }

        private String getLockFlagsString() {
            String result = "";
            if ((this.mFlags & 268435456) != 0) {
                result = result + " ACQUIRE_CAUSES_WAKEUP";
            }
            if ((this.mFlags & 536870912) != 0) {
                return result + " ON_AFTER_RELEASE";
            }
            return result;
        }
    }

    private static native void nativeAcquireSuspendBlocker(String str);

    private native void nativeInit();

    private static native void nativeReleaseSuspendBlocker(String str);

    private static native void nativeSendPowerHint(int i, int i2);

    private static native void nativeSetAutoSuspend(boolean z);

    private static native void nativeSetFeature(int i, int i2);

    private static native void nativeSetInteractive(boolean z);

    public PowerManagerService(Context context) {
        super(context);
        this.mPhoneStateRegSucess = false;
        this.mPhoneState = new AtomicInteger(0);
        this.mCntNoPlayback = new AtomicInteger(0);
        this.mIsSellModeVersion = false;
        this.mLock = LockGuard.installNewLock(1);
        this.mSuspendBlockers = new ArrayList();
        this.mWakeLocks = new ArrayList();
        this.mDisplayPowerRequest = new DisplayPowerRequest();
        this.mDockState = 0;
        this.mMaximumScreenOffTimeoutFromDeviceAdmin = Integer.MAX_VALUE;
        this.mScreenBrightnessOverrideFromWindowManager = -1;
        this.mOverriddenTimeout = -1;
        this.mUserActivityTimeoutOverrideFromWindowManager = -1;
        this.mTemporaryScreenBrightnessSettingOverride = -1;
        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        this.mDozeScreenStateOverrideFromDreamManager = 0;
        this.mDozeScreenBrightnessOverrideFromDreamManager = -1;
        this.mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mDeviceIdleWhitelist = new int[0];
        this.mDeviceIdleTempWhitelist = new int[0];
        this.mUidState = new SparseArray();
        this.mLowPowerModeListeners = new ArrayList();
        this.mColorOsLowPowerModeListeners = new ArrayList();
        this.mOppoButtonReady = true;
        this.useProximityForceSuspend = false;
        this.mProximityLockFromInCallUi = false;
        this.mScreenOffReason = 0;
        this.mScreenOnCpuBoostHelper = null;
        this.mStartGoToSleep = false;
        this.mDisplayPerformanceHelper = null;
        this.mLMServiceManager = null;
        this.mCancelBacklightLimit = new Runnable() {
            public void run() {
                Slog.d(PowerManagerService.TAG, "run processBlackLight");
                PowerManagerService.this.processBlackLight(PowerManagerService.this.mContext);
            }
        };
        this.mDisplayPowerCallbacks = new DisplayPowerCallbacks() {
            private int mDisplayState = 0;

            public void onStateChanged() {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= 8;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositive() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositive");
                    PowerManagerService.this.mProximityPositive = true;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= 512;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityNegative() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityNegative");
                    PowerManagerService.this.mProximityPositive = false;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= 512;
                    PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositiveForceSuspend() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositiveForceSuspend");
                    PowerManagerService.this.mProximityPositive = true;
                    if (PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 7, 0, 1000)) {
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }

            public void onProximityNegativeForceSuspend() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityNegativeForceSuspend");
                    PowerManagerService.this.mProximityPositive = false;
                    PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                }
            }

            public void onDisplayStateChange(int state) {
                synchronized (PowerManagerService.this.mLock) {
                    if (this.mDisplayState != state) {
                        this.mDisplayState = state;
                        if (state == 1) {
                            if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                PowerManagerService.this.setHalInteractiveModeLocked(false);
                            }
                            if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                PowerManagerService.this.setHalAutoSuspendModeLocked(true);
                            }
                        } else {
                            if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                PowerManagerService.this.setHalAutoSuspendModeLocked(false);
                            }
                            if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                PowerManagerService.this.setHalInteractiveModeLocked(true);
                            }
                        }
                    }
                }
            }

            public void acquireSuspendBlocker() {
                PowerManagerService.this.mDisplaySuspendBlocker.acquire();
            }

            public void releaseSuspendBlocker() {
                PowerManagerService.this.mDisplaySuspendBlocker.release();
            }

            public String toString() {
                String str;
                synchronized (this) {
                    str = "state=" + Display.stateToString(this.mDisplayState);
                }
                return str;
            }
        };
        this.mHyp = null;
        this.mHypnusLowPowerenabled = new AtomicBoolean(false);
        this.mActivityChangedListener = new ActivityChangedListener() {
            public void onActivityChanged(String prePkg, String nextPkg) {
                if (PowerManagerService.this.mColorOsLowPowerModeEnabled.get() && nextPkg != null && !"com.coloros.recents".equals(nextPkg)) {
                    boolean isSpeedUpGame = OppoGameSpaceManager.getInstance().inGameSpacePkgList(nextPkg);
                    Slog.d(PowerManagerService.TAG, "hypnusLowPowerModeOn isSpeedUpGame=" + isSpeedUpGame + ", pkg=" + nextPkg);
                    if (isSpeedUpGame) {
                        PowerManagerService.this.hypnusLowPowerModeOn(false);
                    } else {
                        PowerManagerService.this.hypnusLowPowerModeOn(true);
                    }
                }
            }
        };
        this.mVrStateCallbacks = new IVrStateCallbacks.Stub() {
            public void onVrStateChanged(boolean enabled) {
                PowerManagerService.this.powerHintInternal(7, enabled ? 1 : 0);
                synchronized (PowerManagerService.this.mLock) {
                    if (PowerManagerService.this.mIsVrModeEnabled != enabled) {
                        PowerManagerService.this.setVrModeEnabled(enabled);
                        PowerManagerService powerManagerService = PowerManagerService.this;
                        powerManagerService.mDirty |= 8192;
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }
        };
        this.mWakeLockCheck = null;
        this.AUDIO_WL_HELD_UID = 1041;
        this.mIsMmiTesting = false;
        this.mRunPhoneStateRegister = new Runnable() {
            public void run() {
                Slog.d(PowerManagerService.TAG, "Runnable: mPhoneStateRegSucess = " + PowerManagerService.this.mPhoneStateRegSucess);
                if (!PowerManagerService.this.mPhoneStateRegSucess) {
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    int -get4 = powerManagerService.mCntPhoneStateReg;
                    powerManagerService.mCntPhoneStateReg = -get4 + 1;
                    if (-get4 < 40) {
                        PowerManagerService.this.mHandler.postDelayed(PowerManagerService.this.mRunPhoneStateRegister, 500);
                    }
                    TelephonyManager.from(PowerManagerService.this.mContext).listen(PowerManagerService.this.mPhoneStatelistener, 32);
                    Slog.d(PowerManagerService.TAG, "Runnable: telephoneManager.listen. mCntPhoneStateReg=" + PowerManagerService.this.mCntPhoneStateReg);
                }
            }
        };
        this.mPhoneStatelistener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (!PowerManagerService.this.mPhoneStateRegSucess) {
                    PowerManagerService.this.mPhoneStateRegSucess = true;
                    PowerManagerService.this.mHandler.removeCallbacks(PowerManagerService.this.mRunPhoneStateRegister);
                    Slog.d(PowerManagerService.TAG, "onCallStateChanged: set mPhoneStateRegSucess true");
                }
                PowerManagerService.this.mPhoneState.getAndSet(state);
                if (PowerManagerService.DEBUG_PANIC) {
                    Slog.d(PowerManagerService.TAG, "onCallStateChanged: state=" + state + ", mPhoneState=" + PowerManagerService.this.mPhoneState.get());
                }
            }
        };
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mAmbientDisplayConfiguration = new AmbientDisplayConfiguration(this.mContext);
        this.mBatterySaverPolicy = new BatterySaverPolicy(this.mHandler);
        this.qcNsrmPowExt = new QCNsrmPowerExtension(this);
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.init(this.mContext);
        mOppoBrightUtils.getScreenAutoBrightnessConfig();
        mScreenBrightnessSettingMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        mScreenBrightnessSettingMaximum = mOppoBrightUtils.getMaximumScreenBrightnessSetting();
        this.mScreenBrightnessSettingDefault = mOppoBrightUtils.getDefaultScreenBrightnessSetting();
        this.mPowerMonitor = new PowerMonitor();
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        synchronized (this.mLock) {
            this.mWakeLockSuspendBlocker = createSuspendBlockerLocked("PowerManagerService.WakeLocks");
            this.mDisplaySuspendBlocker = createSuspendBlockerLocked("PowerManagerService.Display");
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
            this.mHalAutoSuspendModeEnabled = false;
            this.mHalInteractiveModeEnabled = true;
            this.mWakefulness = 1;
            sQuiescent = SystemProperties.get(SYSTEM_PROPERTY_QUIESCENT, "0").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            nativeInit();
            nativeSetAutoSuspend(false);
            nativeSetInteractive(true);
            nativeSetFeature(1, 0);
        }
    }

    PowerManagerService(Context context, BatterySaverPolicy batterySaverPolicy) {
        super(context);
        this.mPhoneStateRegSucess = false;
        this.mPhoneState = new AtomicInteger(0);
        this.mCntNoPlayback = new AtomicInteger(0);
        this.mIsSellModeVersion = false;
        this.mLock = LockGuard.installNewLock(1);
        this.mSuspendBlockers = new ArrayList();
        this.mWakeLocks = new ArrayList();
        this.mDisplayPowerRequest = new DisplayPowerRequest();
        this.mDockState = 0;
        this.mMaximumScreenOffTimeoutFromDeviceAdmin = Integer.MAX_VALUE;
        this.mScreenBrightnessOverrideFromWindowManager = -1;
        this.mOverriddenTimeout = -1;
        this.mUserActivityTimeoutOverrideFromWindowManager = -1;
        this.mTemporaryScreenBrightnessSettingOverride = -1;
        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        this.mDozeScreenStateOverrideFromDreamManager = 0;
        this.mDozeScreenBrightnessOverrideFromDreamManager = -1;
        this.mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mDeviceIdleWhitelist = new int[0];
        this.mDeviceIdleTempWhitelist = new int[0];
        this.mUidState = new SparseArray();
        this.mLowPowerModeListeners = new ArrayList();
        this.mColorOsLowPowerModeListeners = new ArrayList();
        this.mOppoButtonReady = true;
        this.useProximityForceSuspend = false;
        this.mProximityLockFromInCallUi = false;
        this.mScreenOffReason = 0;
        this.mScreenOnCpuBoostHelper = null;
        this.mStartGoToSleep = false;
        this.mDisplayPerformanceHelper = null;
        this.mLMServiceManager = null;
        this.mCancelBacklightLimit = /* anonymous class already generated */;
        this.mDisplayPowerCallbacks = /* anonymous class already generated */;
        this.mHyp = null;
        this.mHypnusLowPowerenabled = new AtomicBoolean(false);
        this.mActivityChangedListener = /* anonymous class already generated */;
        this.mVrStateCallbacks = /* anonymous class already generated */;
        this.mWakeLockCheck = null;
        this.AUDIO_WL_HELD_UID = 1041;
        this.mIsMmiTesting = false;
        this.mRunPhoneStateRegister = /* anonymous class already generated */;
        this.mPhoneStatelistener = /* anonymous class already generated */;
        this.mBatterySaverPolicy = batterySaverPolicy;
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mAmbientDisplayConfiguration = new AmbientDisplayConfiguration(this.mContext);
        this.mDisplaySuspendBlocker = null;
        this.mWakeLockSuspendBlocker = null;
    }

    public void onStart() {
        publishBinderService("power", new BinderService(this, null));
        publishLocalService(PowerManagerInternal.class, new LocalService(this, null));
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
    }

    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            if (phase == 600) {
                incrementBootCount();
            } else if (phase == 1000) {
                long now = SystemClock.uptimeMillis();
                this.mBootCompleted = true;
                this.mDirty |= 16;
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                updatePowerStateLocked();
                this.mHandler.postDelayed(this.mCancelBacklightLimit, 2000);
                if (!ArrayUtils.isEmpty(this.mBootCompletedRunnables)) {
                    Slog.d(TAG, "Posting " + this.mBootCompletedRunnables.length + " delayed runnables");
                    for (Runnable r : this.mBootCompletedRunnables) {
                        BackgroundThread.getHandler().post(r);
                    }
                }
                this.mBootCompletedRunnables = null;
                OppoAppSwitchManager.getInstance().setActivityChangedListener(this.mActivityChangedListener);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
                this.mHandler.postDelayed(this.mRunPhoneStateRegister, 500);
                TelephonyManager.from(this.mContext).listen(this.mPhoneStatelistener, 32);
                Slog.d(TAG, "PHASE_BOOT_COMPLETED: telephoneManager.listen.");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0074 A:{SYNTHETIC, Splitter: B:33:0x0074} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{SYNTHETIC, Splitter: B:25:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0080 A:{SYNTHETIC, Splitter: B:39:0x0080} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int readSilenceFlagValue() {
        FileNotFoundException e;
        Throwable th;
        File file = new File(BLACKLIGHT_PATH);
        if (file.exists()) {
            InputStream instream = null;
            try {
                InputStream instream2 = new FileInputStream(file);
                if (instream2 != null) {
                    try {
                        int value = Integer.parseInt(new BufferedReader(new InputStreamReader(instream2)).readLine());
                        Slog.d(TAG, "silence flags file value is :" + value);
                        if (instream2 != null) {
                            try {
                                instream2.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        return value;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        instream = instream2;
                        e.printStackTrace();
                        if (instream != null) {
                            try {
                                instream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        return 0;
                    } catch (IOException e4) {
                        instream = instream2;
                        try {
                            Slog.d(TAG, "read silence flags file exception");
                            if (instream != null) {
                                try {
                                    instream.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                            return 0;
                        } catch (Throwable th2) {
                            th = th2;
                            if (instream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        instream = instream2;
                        if (instream != null) {
                            try {
                                instream.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                if (instream2 != null) {
                    try {
                        instream2.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                instream = instream2;
                return 0;
            } catch (FileNotFoundException e5) {
                e = e5;
                e.printStackTrace();
                if (instream != null) {
                }
                return 0;
            } catch (IOException e6) {
                Slog.d(TAG, "read silence flags file exception");
                if (instream != null) {
                }
                return 0;
            }
        }
        Slog.d(TAG, "silence flags file no exist");
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0057 A:{SYNTHETIC, Splitter: B:31:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004b A:{SYNTHETIC, Splitter: B:25:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:46:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003c A:{SYNTHETIC, Splitter: B:17:0x003c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSilenceFlagValue() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(BLACKLIGHT_PATH);
        String content = "0";
        if (file.exists()) {
            OutputStream outstream = null;
            try {
                OutputStream outstream2 = new FileOutputStream(file);
                if (outstream2 != null) {
                    try {
                        Slog.d(TAG, "write 0 to silence flags file ");
                        outstream2.write(content.getBytes());
                        outstream2.flush();
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        outstream = outstream2;
                        e.printStackTrace();
                        if (outstream == null) {
                            try {
                                outstream.close();
                                return;
                            } catch (IOException e22) {
                                e22.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (IOException e4) {
                        e22 = e4;
                        outstream = outstream2;
                        try {
                            e22.printStackTrace();
                            if (outstream == null) {
                                try {
                                    outstream.close();
                                    return;
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                    return;
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (outstream != null) {
                                try {
                                    outstream.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        outstream = outstream2;
                        if (outstream != null) {
                        }
                        throw th;
                    }
                }
                if (outstream2 != null) {
                    try {
                        outstream2.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                e.printStackTrace();
                if (outstream == null) {
                }
            } catch (IOException e6) {
                e22222 = e6;
                e22222.printStackTrace();
                if (outstream == null) {
                }
            }
        } else {
            Slog.d(TAG, "write flags file no exist");
        }
    }

    private void processBlackLight(Context context) {
        if (1 == readSilenceFlagValue()) {
            Slog.d(TAG, "now, there is no backlight, so open it!");
            writeSilenceFlagValue();
            goToSleepInternal(SystemClock.uptimeMillis(), 4, 0, 1000);
        }
    }

    public void systemReady(IAppOpsService appOps) {
        synchronized (this.mLock) {
            this.mSystemReady = true;
            this.mAppOps = appOps;
            this.mDreamManager = (DreamManagerInternal) getLocalService(DreamManagerInternal.class);
            this.mDisplayManagerInternal = (DisplayManagerInternal) getLocalService(DisplayManagerInternal.class);
            this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
            this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
            SystemProperties.set("sys.oppo.multibrightness", Integer.toString(mScreenBrightnessSettingMaximum));
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            mScreenBrightnessSettingMaximum = pm.getMaxBrightness();
            this.mScreenBrightnessForVrSettingDefault = pm.getDefaultScreenBrightnessForVrSetting();
            SensorManager sensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
            this.mBatteryStats = BatteryStatsService.getService();
            this.mNotifier = new Notifier(Looper.getMainLooper(), this.mContext, this.mBatteryStats, this.mAppOps, createSuspendBlockerLocked("PowerManagerService.Broadcasts"), this.mPolicy);
            this.mWirelessChargerDetector = new WirelessChargerDetector(sensorManager, createSuspendBlockerLocked("PowerManagerService.WirelessChargerDetector"), this.mHandler);
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            this.mLightsManager = (LightsManager) getLocalService(LightsManager.class);
            this.mAttentionLight = this.mLightsManager.getLight(5);
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.button.light.auto.off")) {
                this.mOppoHelper = new OppoHelper(this.mLightsManager);
            }
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.guard.elf.support")) {
                this.mWakeLockCheck = new OppoWakeLockCheck(this.mWakeLocks, this.mLock, this.mContext, this, createSuspendBlockerLocked("WakeLockCheck"));
            }
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
                this.mIsSellModeVersion = true;
            }
            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            this.mDisplayManagerInternal.initPowerManagement(this.mDisplayPowerCallbacks, this.mHandler, sensorManager);
            readConfigurationLocked();
            updateSettingsLocked();
            this.mDirty |= 256;
            updatePowerStateLocked();
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mConstants.start(resolver);
        this.mBatterySaverPolicy.start(resolver);
        resolver.registerContentObserver(Secure.getUriFor("screensaver_enabled"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_sleep"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_dock"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Secure.getUriFor("sleep_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_brightness_for_vr"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Global.getUriFor("low_power"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Global.getUriFor("low_power_trigger_level"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Global.getUriFor("theater_mode_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Secure.getUriFor("doze_always_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Secure.getUriFor("double_tap_to_wake"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Global.getUriFor("device_demo_mode"), false, this.mSettingsObserver, 0);
        IVrManager vrManager = (IVrManager) getBinderService("vrmanager");
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e);
            }
        }
        this.mLMServiceManager = new LMServiceManager(this.mContext, this.mHandler);
        if (this.mLMServiceManager != null) {
            this.mLMServiceManager.systemReady();
        }
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(new BatteryReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DREAMING_STARTED");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        this.mContext.registerReceiver(new DreamReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new UserSwitchedReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DOCK_EVENT");
        this.mContext.registerReceiver(new DockReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(new OppoShutDownReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("oppo.action.phone.headset.hangup");
        this.mContext.registerReceiver(new OppoPhoneHeadsetReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction(START_MMI_TEST_CMD);
        filter.addAction(STOP_MMI_TEST_CMD);
        this.mContext.registerReceiver(new OppoUserPresentReceiver(this, null), filter, null, this.mHandler);
        this.mDisplayPerformanceHelper = new OppoDisplayPerformanceHelper(this.mContext);
        this.mDisplayPerformanceHelper.initUpdateBroadcastReceiver();
    }

    private void readConfigurationLocked() {
        Resources resources = this.mContext.getResources();
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = resources.getBoolean(17956994);
        this.mDecoupleHalInteractiveModeFromDisplayConfig = resources.getBoolean(17956995);
        this.mWakeUpWhenPluggedOrUnpluggedConfig = resources.getBoolean(17957046);
        this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig = resources.getBoolean(17956884);
        this.mSuspendWhenScreenOffDueToProximityConfig = resources.getBoolean(17957037);
        this.mDreamsSupportedConfig = resources.getBoolean(17956938);
        this.mDreamsEnabledByDefaultConfig = resources.getBoolean(17956936);
        this.mDreamsActivatedOnSleepByDefaultConfig = resources.getBoolean(17956935);
        this.mDreamsActivatedOnDockByDefaultConfig = resources.getBoolean(17956934);
        this.mDreamsEnabledOnBatteryConfig = resources.getBoolean(17956937);
        this.mDreamsBatteryLevelMinimumWhenPoweredConfig = resources.getInteger(17694782);
        this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = resources.getInteger(17694781);
        this.mDreamsBatteryLevelDrainCutoffConfig = resources.getInteger(17694780);
        this.mDozeAfterScreenOffConfig = resources.getBoolean(17956931);
        this.mMinimumScreenOffTimeoutConfig = resources.getInteger(17694812);
        this.mMaximumScreenDimDurationConfig = resources.getInteger(17694810);
        this.mMaximumScreenDimRatioConfig = resources.getFraction(18022402, 1, 1);
        this.mSupportsDoubleTapWakeConfig = resources.getBoolean(17957028);
    }

    private void updateSettingsLocked() {
        int i;
        boolean z;
        ContentResolver resolver = this.mContext.getContentResolver();
        String str = "screensaver_enabled";
        if (this.mDreamsEnabledByDefaultConfig) {
            i = 1;
        } else {
            i = 0;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mDreamsEnabledSetting = z;
        str = "screensaver_activate_on_sleep";
        if (this.mDreamsActivatedOnSleepByDefaultConfig) {
            i = 1;
        } else {
            i = 0;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mDreamsActivateOnSleepSetting = z;
        str = "screensaver_activate_on_dock";
        if (this.mDreamsActivatedOnDockByDefaultConfig) {
            i = 1;
        } else {
            i = 0;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mDreamsActivateOnDockSetting = z;
        this.mScreenOffTimeoutSetting = System.getIntForUser(resolver, "screen_off_timeout", 15000, -2);
        this.mSleepTimeoutSetting = Secure.getIntForUser(resolver, "sleep_timeout", -1, -2);
        this.mStayOnWhilePluggedInSetting = Global.getInt(resolver, "stay_on_while_plugged_in", 1);
        this.mTheaterModeEnabled = Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
        this.mAlwaysOnEnabled = this.mAmbientDisplayConfiguration.alwaysOnEnabled(-2);
        if (this.mSupportsDoubleTapWakeConfig) {
            boolean doubleTapWakeEnabled = Secure.getIntForUser(resolver, "double_tap_to_wake", 0, -2) != 0;
            if (doubleTapWakeEnabled != this.mDoubleTapWakeEnabled) {
                this.mDoubleTapWakeEnabled = doubleTapWakeEnabled;
                if (this.mDoubleTapWakeEnabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                nativeSetFeature(1, i);
            }
        }
        String retailDemoValue = UserManager.isDeviceInDemoMode(this.mContext) ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
        if (!retailDemoValue.equals(SystemProperties.get(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED))) {
            SystemProperties.set(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, retailDemoValue);
        }
        int oldScreenBrightnessSetting = getCurrentBrightnessSettingLocked();
        this.mScreenBrightnessForVrSetting = System.getIntForUser(resolver, "screen_brightness_for_vr", this.mScreenBrightnessForVrSettingDefault, -2);
        this.mScreenBrightnessSetting = System.getIntForBrightness(resolver, "screen_brightness", this.mScreenBrightnessSettingDefault, -2);
        if (oldScreenBrightnessSetting != getCurrentBrightnessSettingLocked()) {
            this.mTemporaryScreenBrightnessSettingOverride = -1;
        }
        float oldScreenAutoBrightnessAdjustmentSetting = this.mScreenAutoBrightnessAdjustmentSetting;
        this.mScreenAutoBrightnessAdjustmentSetting = System.getFloatForUser(resolver, "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
        if (oldScreenAutoBrightnessAdjustmentSetting != this.mScreenAutoBrightnessAdjustmentSetting) {
            this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        }
        this.mScreenBrightnessModeSetting = System.getIntForUser(resolver, "screen_brightness_mode", 0, -2);
        boolean lowPowerModeEnabled = Global.getInt(resolver, "low_power", 0) != 0;
        boolean autoLowPowerModeConfigured = Global.getInt(resolver, "low_power_trigger_level", 0) != 0;
        if (!(lowPowerModeEnabled == this.mLowPowerModeSetting && autoLowPowerModeConfigured == this.mAutoLowPowerModeConfigured)) {
            this.mLowPowerModeSetting = lowPowerModeEnabled;
            this.mAutoLowPowerModeConfigured = autoLowPowerModeConfigured;
            updateLowPowerModeLocked();
        }
        this.mDirty |= 32;
    }

    private int getCurrentBrightnessSettingLocked() {
        return this.mIsVrModeEnabled ? this.mScreenBrightnessForVrSetting : this.mScreenBrightnessSetting;
    }

    private void postAfterBootCompleted(Runnable r) {
        if (this.mBootCompleted) {
            BackgroundThread.getHandler().post(r);
            return;
        }
        Slog.d(TAG, "Delaying runnable until system is booted");
        this.mBootCompletedRunnables = (Runnable[]) ArrayUtils.appendElement(Runnable.class, this.mBootCompletedRunnables, r);
    }

    private void updateLowPowerModeLocked() {
        boolean autoLowPowerModeEnabled;
        int i = 0;
        if ((this.mIsPowered || !(this.mBatteryLevelLow || (this.mBootCompleted ^ 1) == 0)) && this.mLowPowerModeSetting) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateLowPowerModeLocked: powered or booting with sufficient battery, turning setting off");
            }
            Global.putInt(this.mContext.getContentResolver(), "low_power", 0);
            this.mLowPowerModeSetting = false;
        }
        if (this.mIsPowered || !this.mAutoLowPowerModeConfigured || (this.mAutoLowPowerModeSnoozing ^ 1) == 0) {
            autoLowPowerModeEnabled = false;
        } else {
            autoLowPowerModeEnabled = this.mBatteryLevelLow;
        }
        final boolean lowPowerModeEnabled = !this.mLowPowerModeSetting ? autoLowPowerModeEnabled : true;
        if (this.mLowPowerModeEnabled != lowPowerModeEnabled) {
            this.mLowPowerModeEnabled = lowPowerModeEnabled;
            if (lowPowerModeEnabled) {
                i = 1;
            }
            powerHintInternal(5, i);
            postAfterBootCompleted(new Runnable() {
                public void run() {
                    ArrayList<LowPowerModeListener> listeners;
                    PowerManagerService.this.mContext.sendBroadcast(new Intent("android.os.action.POWER_SAVE_MODE_CHANGING").putExtra("mode", PowerManagerService.this.mLowPowerModeEnabled).addFlags(1073741824));
                    synchronized (PowerManagerService.this.mLock) {
                        listeners = new ArrayList(PowerManagerService.this.mLowPowerModeListeners);
                    }
                    for (int i = 0; i < listeners.size(); i++) {
                        LowPowerModeListener listener = (LowPowerModeListener) listeners.get(i);
                        listener.onLowPowerModeChanged(PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(listener.getServiceType(), lowPowerModeEnabled));
                    }
                    Intent intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED");
                    intent.addFlags(1073741824);
                    PowerManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL");
                    intent.addFlags(1073741824);
                    PowerManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.DEVICE_POWER");
                }
            });
        }
    }

    private void handleSettingsChangedLocked() {
        updateSettingsLocked();
        updatePowerStateLocked();
    }

    /* JADX WARNING: Missing block: B:28:0x0108, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        synchronized (this.mLock) {
            WakeLock wakeLock;
            boolean notifyAcquire;
            if (this.mWakeLockCheck != null) {
                if (!this.mWakeLockCheck.canSyncWakeLockAcq(uid, tag)) {
                    return;
                } else if (!this.mWakeLockCheck.allowAcquireWakelock(packageName, flags, ws, uid)) {
                    return;
                }
            }
            if (DEBUG_SPEW) {
                Slog.d(TAG, "acquireWakeLockInternal: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + tag + "\", ws=" + ws + ", uid=" + uid + ", pid=" + pid);
            }
            this.mPowerMonitor.acquireWakeLock(packageName, tag, flags);
            int index = findWakeLockIndexLocked(lock);
            if (index >= 0) {
                wakeLock = (WakeLock) this.mWakeLocks.get(index);
                if (!wakeLock.hasSameProperties(flags, tag, ws, uid, pid)) {
                    notifyWakeLockChangingLocked(wakeLock, flags, tag, packageName, uid, pid, ws, historyTag);
                    wakeLock.updateProperties(flags, tag, packageName, ws, historyTag, uid, pid);
                }
                notifyAcquire = false;
            } else {
                UidState state = (UidState) this.mUidState.get(uid);
                if (state == null) {
                    state = new UidState(uid);
                    state.mProcState = 18;
                    this.mUidState.put(uid, state);
                }
                state.mNumWakeLocks++;
                wakeLock = new WakeLock(lock, flags, tag, packageName, ws, historyTag, uid, pid, state);
                try {
                    lock.linkToDeath(wakeLock, 0);
                    this.mWakeLocks.add(wakeLock);
                    setWakeLockDisabledStateLocked(wakeLock);
                    this.qcNsrmPowExt.checkPmsBlockedWakelocks(uid, pid, flags, tag, wakeLock);
                    notifyAcquire = true;
                    wakeLock.mActiveSince = SystemClock.uptimeMillis();
                } catch (RemoteException e) {
                    throw new IllegalArgumentException("Wake lock is already dead.");
                }
            }
            applyWakeLockFlagsOnAcquireLocked(wakeLock, uid);
            this.mDirty |= 1;
            updatePowerStateLocked();
            if (notifyAcquire) {
                notifyWakeLockAcquiredLocked(wakeLock);
            }
        }
    }

    private static boolean isScreenLock(WakeLock wakeLock) {
        switch (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
            case 6:
            case 10:
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return true;
            default:
                return false;
        }
    }

    private void applyWakeLockFlagsOnAcquireLocked(WakeLock wakeLock, int uid) {
        if ((wakeLock.mFlags & 268435456) != 0 && isScreenLock(wakeLock)) {
            if (this.useProximityForceSuspend && this.mProximityPositive) {
                if (DEBUG_PANIC) {
                    Slog.i(TAG, "wakeLock : " + wakeLock.mTag + ", lock = " + Objects.hashCode(wakeLock.mLock) + " try to wakeup device while proximity positive");
                }
                userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, uid);
                return;
            }
            String opPackageName;
            int opUid;
            if (wakeLock.mWorkSource == null || wakeLock.mWorkSource.getName(0) == null) {
                opPackageName = wakeLock.mPackageName;
                if (wakeLock.mWorkSource != null) {
                    opUid = wakeLock.mWorkSource.get(0);
                } else {
                    opUid = wakeLock.mOwnerUid;
                }
            } else {
                opPackageName = wakeLock.mWorkSource.getName(0);
                opUid = wakeLock.mWorkSource.get(0);
            }
            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), wakeLock.mTag, opUid, opPackageName, opUid);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x003f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void releaseWakeLockInternal(IBinder lock, int flags) {
        synchronized (this.mLock) {
            int index = findWakeLockIndexLocked(lock);
            if (index >= 0) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(index);
                wakeLock.mTotalTime = SystemClock.uptimeMillis() - wakeLock.mActiveSince;
                if (DEBUG_SPEW) {
                    Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.toString() + "], flags=0x" + Integer.toHexString(flags) + ", total_time=" + wakeLock.mTotalTime + "ms");
                }
                if (!(!isCPULock(wakeLock) || (wakeLock.mDisabled ^ 1) == 0 || (isInteractiveInternal() ^ 1) == 0)) {
                    this.mPowerMonitor.releaseWakeLock(wakeLock.mPackageName, wakeLock.mTag, wakeLock.mTotalTime);
                }
                if ((flags & 1) != 0) {
                    this.mRequestWaitForNegativeProximity = true;
                }
                wakeLock.mLock.unlinkToDeath(wakeLock, 0);
                removeWakeLockLocked(wakeLock, index);
            } else if (DEBUG_PANIC || DEBUG_SPEW) {
                Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
            }
        }
    }

    private void handleWakeLockDeath(WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            int index = this.mWakeLocks.indexOf(wakeLock);
            if (index < 0) {
                return;
            }
            removeWakeLockLocked(wakeLock, index);
        }
    }

    private void removeWakeLockLocked(WakeLock wakeLock, int index) {
        this.mWakeLocks.remove(index);
        UidState state = wakeLock.mUidState;
        state.mNumWakeLocks--;
        if (state.mNumWakeLocks <= 0 && state.mProcState == 18) {
            this.mUidState.remove(state.mUid);
        }
        notifyWakeLockReleasedLocked(wakeLock);
        applyWakeLockFlagsOnReleaseLocked(wakeLock);
        this.mDirty |= 1;
        updatePowerStateLocked();
    }

    private void applyWakeLockFlagsOnReleaseLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & 536870912) != 0 && isScreenLock(wakeLock)) {
            userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, wakeLock.mOwnerUid);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0037, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x0096, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateWakeLockWorkSourceInternal(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        synchronized (this.mLock) {
            int index = findWakeLockIndexLocked(lock);
            if (index >= 0) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(index);
                if (DEBUG_SPEW) {
                    Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws);
                }
                if (!wakeLock.hasSameWorkSource(ws)) {
                    notifyWakeLockChangingLocked(wakeLock, wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, ws, historyTag);
                    wakeLock.mHistoryTag = historyTag;
                    wakeLock.updateWorkSource(ws);
                }
            } else if (DEBUG_SPEW) {
                Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [not found], ws=" + ws);
            }
        }
    }

    private int findWakeLockIndexLocked(IBinder lock) {
        int count = this.mWakeLocks.size();
        for (int i = 0; i < count; i++) {
            if (((WakeLock) this.mWakeLocks.get(i)).mLock == lock) {
                return i;
            }
        }
        return -1;
    }

    protected void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
        if (this.mSystemReady && (wakeLock.mDisabled ^ 1) != 0) {
            wakeLock.mNotifiedAcquired = true;
            this.mNotifier.onWakeLockAcquired(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            restartNofifyLongTimerLocked(wakeLock);
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.noteWakeLockChange(wakeLock, true);
            }
        }
    }

    private void enqueueNotifyLongMsgLocked(long time) {
        this.mNotifyLongScheduled = time;
        Message msg = this.mHandler.obtainMessage(4);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, time);
    }

    private void restartNofifyLongTimerLocked(WakeLock wakeLock) {
        wakeLock.mAcquireTime = SystemClock.uptimeMillis();
        if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && this.mNotifyLongScheduled == 0) {
            enqueueNotifyLongMsgLocked(wakeLock.mAcquireTime + 60000);
        }
    }

    private void notifyWakeLockLongStartedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && (wakeLock.mDisabled ^ 1) != 0) {
            wakeLock.mNotifiedLong = true;
            this.mNotifier.onLongPartialWakeLockStart(wakeLock.mTag, wakeLock.mOwnerUid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    private void notifyWakeLockLongFinishedLocked(WakeLock wakeLock) {
        if (wakeLock.mNotifiedLong) {
            wakeLock.mNotifiedLong = false;
            this.mNotifier.onLongPartialWakeLockFinish(wakeLock.mTag, wakeLock.mOwnerUid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    private void notifyWakeLockChangingLocked(WakeLock wakeLock, int flags, String tag, String packageName, int uid, int pid, WorkSource ws, String historyTag) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            this.mNotifier.onWakeLockChanging(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag, flags, tag, packageName, uid, pid, ws, historyTag);
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.noteWorkSourceChange(wakeLock, ws);
            }
            notifyWakeLockLongFinishedLocked(wakeLock);
            restartNofifyLongTimerLocked(wakeLock);
        }
    }

    protected void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            wakeLock.mNotifiedAcquired = false;
            wakeLock.mAcquireTime = 0;
            this.mNotifier.onWakeLockReleased(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.noteWakeLockChange(wakeLock, false);
            }
            notifyWakeLockLongFinishedLocked(wakeLock);
        }
    }

    private void dumpWakeLockLocked() {
        int numWakeLocks = this.mWakeLocks.size();
        if (numWakeLocks > 0) {
            Slog.d(TAG, "wakelock list dump: mLocks.size=" + numWakeLocks + ":");
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
                long total_time = SystemClock.uptimeMillis() - wakeLock.mActiveSince;
                Slog.d(TAG, "No." + i + ": " + wakeLock.getLockLevelString() + " '" + wakeLock.mTag + "'" + "activated" + "(flags=" + wakeLock.mFlags + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid + ")" + " total=" + total_time + "ms)");
            }
        }
    }

    private static boolean isCPULock(WakeLock wakeLock) {
        return ((wakeLock.mFlags & 1) == 0 && (wakeLock.mFlags & 128) == 0) ? false : true;
    }

    private void tryToTrackWakelocks() {
        if (!this.mHandler.hasMessages(7)) {
            Message msg = this.mHandler.obtainMessage(7);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 1800000);
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0017, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isWakeLockLevelSupportedInternal(int level) {
        boolean z = false;
        synchronized (this.mLock) {
            switch (level) {
                case 1:
                case 6:
                case 10:
                case H.DO_ANIMATION_CALLBACK /*26*/:
                case 64:
                case 128:
                    return true;
                case 32:
                    if (this.mSystemReady) {
                        z = this.mDisplayManagerInternal.isProximitySensorAvailable();
                        break;
                    }
                    break;
                default:
                    return false;
            }
        }
    }

    private void userActivityFromNative(long eventTime, int event, int flags) {
        userActivityInternal(eventTime, event, flags, 1000);
    }

    private void userActivityInternal(long eventTime, int event, int flags, int uid) {
        synchronized (this.mLock) {
            if (userActivityNoUpdateLocked(eventTime, event, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean userActivityNoUpdateLocked(long eventTime, int event, int flags, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "userActivityNoUpdateLocked: eventTime=" + eventTime + ", event=" + event + ", flags=0x" + Integer.toHexString(flags) + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || eventTime < this.mLastWakeTime || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0) {
            return false;
        }
        Trace.traceBegin(131072, "userActivity");
        try {
            if (eventTime > this.mLastInteractivePowerHintTime) {
                powerHintInternal(2, 0);
                this.mLastInteractivePowerHintTime = eventTime;
            }
            this.mNotifier.onUserActivity(event, uid);
            if (this.mUserInactiveOverrideFromWindowManager) {
                this.mUserInactiveOverrideFromWindowManager = false;
                this.mOverriddenTimeout = -1;
            }
            if (this.mWakefulness == 0 || this.mWakefulness == 3 || (flags & 2) != 0) {
                Trace.traceEnd(131072);
                return false;
            }
            if ((flags & 1) != 0) {
                if (eventTime > this.mLastUserActivityTimeNoChangeLights && eventTime > this.mLastUserActivityTime) {
                    this.mLastUserActivityTimeNoChangeLights = eventTime;
                    this.mDirty |= 4;
                    if (event == 1) {
                        this.mDirty |= 4096;
                    }
                    Trace.traceEnd(131072);
                    return true;
                }
            } else if (eventTime > this.mLastUserActivityTime) {
                this.mLastUserActivityTime = eventTime;
                this.mDirty |= 4;
                if (event == 1) {
                    this.mDirty |= 4096;
                }
                Trace.traceEnd(131072);
                return true;
            }
            Trace.traceEnd(131072);
            return false;
        } catch (Throwable th) {
            Trace.traceEnd(131072);
        }
    }

    private boolean isBiometricsBlockReason(String reason) {
        if ("android.service.fingerprint:WAKEUP".equals(reason) || "android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason)) {
            return true;
        }
        return "oppo.wakeup.gesture:LIFT_HAND".equals(reason);
    }

    private boolean isFingerprintBlockReason(String reason) {
        return "android.service.fingerprint:WAKEUP".equals(reason);
    }

    private boolean isFaceBlockReason(String reason) {
        if ("android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason)) {
            return true;
        }
        return "oppo.wakeup.gesture:LIFT_HAND".equals(reason);
    }

    private boolean hasBlockedByFace() {
        if (this.mBiometricsManager == null || !this.mDisplayManagerInternal.isBlockDisplayByBiometrics()) {
            return false;
        }
        if (this.mDisplayManagerInternal.hasBiometricsBlockedReason("android.policy:POWER") || this.mDisplayManagerInternal.hasBiometricsBlockedReason("oppo.wakeup.gesture:DOUBLE_TAP_SCREEN")) {
            return true;
        }
        return this.mDisplayManagerInternal.hasBiometricsBlockedReason("oppo.wakeup.gesture:LIFT_HAND");
    }

    private boolean hasBlockedByFingerprint() {
        if (this.mBiometricsManager == null || !this.mDisplayManagerInternal.isBlockDisplayByBiometrics()) {
            return false;
        }
        return this.mDisplayManagerInternal.hasBiometricsBlockedReason("android.service.fingerprint:WAKEUP");
    }

    private boolean hasBlockedByOtherBiometrics(String reason) {
        if (isFingerprintBlockReason(reason)) {
            if (!hasBlockedByFingerprint() && hasBlockedByFace()) {
                return true;
            }
        } else if (isFaceBlockReason(reason) && !hasBlockedByFace() && hasBlockedByFingerprint()) {
            return true;
        }
        return false;
    }

    private void startWakeUpAndBlockScreenOn(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startWakeUpAndBlockScreenOn, reason = " + reason);
        }
        if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            wakeUpInternal(SystemClock.uptimeMillis(), reason, 1000, this.mContext.getOpPackageName(), 1000);
            return;
        }
        this.mDisplayManagerInternal.blockScreenOnByBiometrics(reason);
    }

    private void startUnblockScreenOn(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startUnblockScreenOn, reason = " + reason);
        }
        this.mStartGoToSleep = false;
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
        }
        if (this.mDisplayManagerInternal != null) {
            this.mDisplayManagerInternal.unblockScreenOnByBiometrics(reason);
        }
        wakeUpInternal(SystemClock.uptimeMillis(), reason, 1000, this.mContext.getOpPackageName(), 1000);
    }

    private void startGotoSleepWhenScreenOnBlocked(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startGotoSleepWhenScreenOnBlocked, reason = " + reason);
        }
        if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics() && this.mHandler.hasMessages(6)) {
            Slog.d(TAG, "Screen turning on for fingerprint, ignore verify failed");
        }
    }

    private int getScreenStateInternal() {
        int result = 0;
        if (!(this.mDisplayManagerInternal == null || this.mDisplayManagerInternal.getScreenState() != 1 || (this.mStartGoToSleep ^ 1) == 0)) {
            result = 1;
        }
        if (DEBUG_PANIC) {
            Slog.d(TAG, "get Screen State, result = " + result + ", start sleep = " + this.mStartGoToSleep);
        }
        return result;
    }

    private void wakeUpInternal(long eventTime, String reason, int uid, String opPackageName, int opUid) {
        synchronized (this.mLock) {
            if (wakeUpNoUpdateLocked(eventTime, reason, uid, opPackageName, opUid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean wakeUpNoUpdateLocked(long eventTime, String reason, int reasonUid, String opPackageName, int opUid) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "wakeUpNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", uid=" + reasonUid + ", opPackageName=" + opPackageName);
        }
        if (DEBUG_SPEW) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, "PowerMS    |----" + element.toString());
            }
        }
        if ("android.server.wm:TURN_ON".equals(reason)) {
            if (this.useProximityForceSuspend && this.mProximityPositive) {
                Slog.i(TAG, "windowmanager try to wakeup device while proximity positive");
                userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, reasonUid);
                return false;
            }
        } else if (isBiometricsBlockReason(reason)) {
            this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
        } else if ("android.service.fingerprint:DOUBLE_HOME".equals(reason) || "android.policy.wakeup.slient".equals(reason)) {
            this.mScreenOnCpuBoostHelper.acquireCpuBoost(500);
        }
        if (isFingerprintBlockReason(reason)) {
            if (this.mBiometricsManager != null && this.mDisplayManagerInternal.isBlockDisplayByBiometrics()) {
                Slog.d(TAG, "onWakeUp, not the first block by different biometrics");
                this.mBiometricsManager.onWakeUp(reason);
                return false;
            }
        } else if (isFaceBlockReason(reason) && this.mBiometricsManager != null && this.mBiometricsManager.isFaceAutoUnlockEnabled()) {
            if (hasBlockedByOtherBiometrics(reason)) {
                Slog.d(TAG, "onWakeUp, not the first block by different biometrics");
                this.mBiometricsManager.onWakeUp(reason);
                return false;
            }
        } else if (!this.mDisplayManagerInternal.isBlockDisplayByBiometrics() || this.mBiometricsManager == null) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "unblockScreenOnByBiometrics, reason = " + reason);
            }
            this.mDisplayManagerInternal.unblockScreenOnByBiometrics(reason);
        } else {
            Slog.d(TAG, "screenOnUnBlockedByOther, delay 400ms for alpha change");
            this.mBiometricsManager.onScreenOnUnBlockedByOther(reason);
            PowerManager power = (PowerManager) this.mContext.getSystemService("power");
            Object partial = null;
            if (power != null) {
                partial = power.newWakeLock(1, "fingerprint_delay");
                partial.acquire(500);
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = 6;
            msg.obj = partial;
            this.mHandler.sendEmptyMessageDelayed(6, 400);
            return false;
        }
        if (eventTime < this.mLastSleepTime || this.mWakefulness == 1 || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0) {
            return false;
        }
        this.mStartGoToSleep = false;
        if (DEBUG_SPEW) {
            Slog.i(TAG, "BiometricsService.onWakeUp in");
        }
        if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            this.mDisplayManagerInternal.blockScreenOnByBiometrics(reason);
        }
        if (this.mBiometricsManager != null) {
            this.mBiometricsManager.onWakeUp(reason);
        }
        Trace.asyncTraceBegin(131072, TRACE_SCREEN_ON, 0);
        Trace.traceBegin(131072, "wakeUp");
        try {
            switch (this.mWakefulness) {
                case 0:
                    Slog.i(TAG, "Waking up from sleep (uid=" + reasonUid + " reason=" + reason + ")...");
                    break;
                case 2:
                    Slog.i(TAG, "Waking up from dream (uid=" + reasonUid + " reason=" + reason + ")...");
                    break;
                case 3:
                    Slog.i(TAG, "Waking up from dozing (uid=" + reasonUid + " reason=" + reason + ")...");
                    break;
            }
            this.mLastWakeTime = eventTime;
            this.mPowerMonitor.screenOn();
            if ("android.service.fingerprint:WAKEUP".equals(reason)) {
                setWakefulnessLocked(1, 98);
            } else {
                setWakefulnessLocked(1, 0);
            }
            if (this.mHandler.hasMessages(7)) {
                this.mHandler.removeMessages(7);
            }
            this.mNotifier.onWakeUp(reason, reasonUid, opPackageName, opUid);
            userActivityNoUpdateLocked(eventTime, 0, 0, reasonUid);
            if (this.mOppoHelper != null) {
                this.mOppoHelper.updateButtonBrightness(this, false);
            }
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.PartialWakelockCheckStop();
            }
            this.mCntNoPlayback.set(0);
            this.mHandler.sendEmptyMessageDelayed(101, DELAY_CHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    private void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
        if (reason == 99 && (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics() ^ 1) != 0) {
            return;
        }
        if (reason == 4 && hasBlockedByFingerprint()) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "unblockScreenOnByBiometrics, reason = POWER-GotoSleep");
            }
            this.mDisplayManagerInternal.unblockScreenOnByBiometrics("android.server.power:POWER");
            if (this.mBiometricsManager != null) {
                this.mBiometricsManager.notifyPowerKeyPressed();
            }
            if (DEBUG_SPEW) {
                Slog.d(TAG, "Not goTosleep( " + reason + " ) due to fingerPrint", new Throwable("FP DEBUG"));
            }
        } else if (reason == 4 && hasBlockedByFace()) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "ignore power key while block by face");
            }
        } else {
            synchronized (this.mLock) {
                if (goToSleepNoUpdateLocked(eventTime, reason, flags, uid)) {
                    updatePowerStateLocked();
                }
            }
        }
    }

    private boolean goToSleepNoUpdateLocked(long eventTime, int reason, int flags, int uid) {
        if (DEBUG_SPEW) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, "PowerMS    |----" + element.toString());
            }
        }
        if (DEBUG_PANIC || DEBUG_SPEW) {
            Slog.d(TAG, "goToSleepNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", flags=" + flags + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || this.mWakefulness == 3 || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0) {
            return false;
        }
        if (mOppoShutdownIng) {
            Slog.d(TAG, "goToSleepNoUpdateLocked: Not go to sleep when shutdown!!!");
            return false;
        }
        this.mStartGoToSleep = true;
        if (this.mBiometricsManager != null) {
            if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                this.mDisplayManagerInternal.unblockScreenOnByBiometrics(UNBLOCK_REASON_GO_TO_SLEEP);
            }
            this.mBiometricsManager.onGoToSleep();
        }
        Trace.traceBegin(131072, "goToSleep");
        switch (reason) {
            case 1:
                Slog.i(TAG, "Going to sleep due to device administration policy (uid " + uid + ")...");
                break;
            case 2:
                Slog.i(TAG, "Going to sleep due to screen timeout (uid " + uid + ")...");
                break;
            case 3:
                Slog.i(TAG, "Going to sleep due to lid switch (uid " + uid + ")...");
                break;
            case 4:
                Slog.i(TAG, "Going to sleep due to power button (uid " + uid + ")...");
                break;
            case 5:
                Slog.i(TAG, "Going to sleep due to HDMI standby (uid " + uid + ")...");
                break;
            case 6:
                Slog.i(TAG, "Going to sleep due to sleep button (uid " + uid + ")...");
                break;
            case 7:
                Slog.i(TAG, "Going to sleep due to proximity (uid " + uid + ")...");
                break;
            default:
                try {
                    Slog.i(TAG, "Going to sleep by application request (uid " + uid + ")...");
                    reason = 0;
                    break;
                } catch (Throwable th) {
                    Trace.traceEnd(131072);
                }
        }
        this.mScreenOffReason = reason;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (PowerManagerService.DEBUG_PANIC) {
                    Slog.i(PowerManagerService.TAG, "update sys.power.screenoff.reason");
                }
                SystemProperties.set("sys.power.screenoff.reason", "" + PowerManagerService.this.mScreenOffReason);
            }
        });
        this.mLastSleepTime = eventTime;
        this.mSandmanSummoned = true;
        setWakefulnessLocked(3, reason);
        int numWakeLocksCleared = 0;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = 0; i < numWakeLocks; i++) {
            switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                case 6:
                case 10:
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    numWakeLocksCleared++;
                    break;
                default:
                    break;
            }
        }
        EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
        dumpWakeLockLocked();
        tryToTrackWakelocks();
        if ((flags & 1) != 0 || this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            reallyGoToSleepNoUpdateLocked(eventTime, uid);
        }
        Trace.traceEnd(131072);
        if (this.mOppoHelper != null) {
            this.mOppoHelper.turnOffButtonLight();
        }
        if (this.mWakeLockCheck != null) {
            this.mWakeLockCheck.PartialWakelockCheckStart();
        }
        this.mHandler.removeMessages(101);
        this.mPowerMonitor.clear();
        this.mPowerMonitor.screenOff();
        return true;
    }

    private void napInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (napNoUpdateLocked(eventTime, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean napNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "napNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness != 1 || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0) {
            return false;
        }
        Trace.traceBegin(131072, "nap");
        try {
            Slog.i(TAG, "Nap time (uid " + uid + ")...");
            this.mSandmanSummoned = true;
            setWakefulnessLocked(2, 0);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    private boolean reallyGoToSleepNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_PANIC || DEBUG_SPEW) {
            Slog.d(TAG, "reallyGoToSleepNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0) {
            return false;
        }
        Trace.traceBegin(131072, "reallyGoToSleep");
        try {
            Slog.i(TAG, "Sleeping (uid " + uid + ")...");
            setWakefulnessLocked(0, 2);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    void setWakefulnessLocked(int wakefulness, int reason) {
        if (this.mWakefulness != wakefulness) {
            this.mWakefulness = wakefulness;
            this.mWakefulnessChanging = true;
            this.mDirty |= 2;
            if (this.mNotifier != null) {
                this.mNotifier.onWakefulnessChangeStarted(wakefulness, reason);
            }
        }
    }

    private void logSleepTimeoutRecapturedLocked() {
        long savedWakeTimeMs = this.mOverriddenTimeout - SystemClock.uptimeMillis();
        if (savedWakeTimeMs >= 0) {
            EventLog.writeEvent(EventLogTags.POWER_SOFT_SLEEP_REQUESTED, savedWakeTimeMs);
            this.mOverriddenTimeout = -1;
        }
    }

    private void logScreenOn() {
        Trace.asyncTraceEnd(131072, TRACE_SCREEN_ON, 0);
        int latencyMs = (int) (SystemClock.uptimeMillis() - this.mLastWakeTime);
        LogMaker log = new LogMaker(198);
        log.setType(1);
        log.setSubtype(0);
        log.setLatency((long) latencyMs);
        MetricsLogger.action(log);
        EventLogTags.writePowerScreenState(1, 0, 0, 0, latencyMs);
        if (latencyMs >= 200) {
            Slog.w(TAG, "Screen on took " + latencyMs + " ms");
        }
    }

    private void finishWakefulnessChangeIfNeededLocked() {
        if (this.mWakefulnessChanging && this.mDisplayReady && (this.mWakefulness != 3 || (this.mWakeLockSummary & 64) != 0)) {
            if (this.mWakefulness == 3 || this.mWakefulness == 0) {
                logSleepTimeoutRecapturedLocked();
            }
            if (this.mWakefulness == 1) {
                logScreenOn();
            }
            this.mWakefulnessChanging = false;
            if (this.mBiometricsManager != null && this.mWakefulness == 1) {
                this.mBiometricsManager.onWakeUpFinish();
            }
            this.mNotifier.onWakefulnessChangeFinished();
        }
    }

    protected void updatePowerStateLocked() {
        if (this.mSystemReady && this.mDirty != 0) {
            if (!Thread.holdsLock(this.mLock)) {
                Slog.wtf(TAG, "Power manager lock was not held when calling updatePowerStateLocked");
            }
            Trace.traceBegin(131072, "updatePowerState");
            try {
                updateIsPoweredLocked(this.mDirty);
                updateStayOnLocked(this.mDirty);
                updateScreenBrightnessBoostLocked(this.mDirty);
                long now = SystemClock.uptimeMillis();
                int dirtyPhase2 = 0;
                while (true) {
                    int dirtyPhase1 = this.mDirty;
                    dirtyPhase2 |= dirtyPhase1;
                    this.mDirty = 0;
                    updateWakeLockSummaryLocked(dirtyPhase1);
                    updateUserActivitySummaryLocked(now, dirtyPhase1);
                    if (!updateWakefulnessLocked(dirtyPhase1)) {
                        break;
                    }
                }
                updateDreamLocked(dirtyPhase2, updateDisplayPowerStateLocked(dirtyPhase2));
                finishWakefulnessChangeIfNeededLocked();
                updateSuspendBlockerLocked();
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    private void updateIsPoweredLocked(int dirty) {
        if ((dirty & 256) != 0) {
            boolean wasPowered = this.mIsPowered;
            int oldPlugType = this.mPlugType;
            boolean oldLevelLow = this.mBatteryLevelLow;
            this.mIsPowered = this.mBatteryManagerInternal.isPowered(7);
            this.mPlugType = this.mBatteryManagerInternal.getPlugType();
            this.mBatteryLevel = this.mBatteryManagerInternal.getBatteryLevel();
            this.mBatteryLevelLow = this.mBatteryManagerInternal.getBatteryLevelLow();
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateIsPoweredLocked: wasPowered=" + wasPowered + ", mIsPowered=" + this.mIsPowered + ", oldPlugType=" + oldPlugType + ", mPlugType=" + this.mPlugType + ", mBatteryLevel=" + this.mBatteryLevel);
            }
            if (!(wasPowered == this.mIsPowered && oldPlugType == this.mPlugType)) {
                this.mDirty |= 64;
                boolean dockedOnWirelessCharger = this.mWirelessChargerDetector.update(this.mIsPowered, this.mPlugType, this.mBatteryLevel);
                long now = SystemClock.uptimeMillis();
                if (shouldWakeUpWhenPluggedOrUnpluggedLocked(wasPowered, oldPlugType, dockedOnWirelessCharger)) {
                    wakeUpNoUpdateLocked(now, "android.server.power:POWER", 1000, this.mContext.getOpPackageName(), 1000);
                }
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                if (dockedOnWirelessCharger) {
                    this.mNotifier.onWirelessChargingStarted();
                }
            }
            if (wasPowered != this.mIsPowered || oldLevelLow != this.mBatteryLevelLow) {
                if (!(oldLevelLow == this.mBatteryLevelLow || (this.mBatteryLevelLow ^ 1) == 0)) {
                    if (DEBUG_SPEW) {
                        Slog.d(TAG, "updateIsPoweredLocked: resetting low power snooze");
                    }
                    this.mAutoLowPowerModeSnoozing = false;
                }
                updateLowPowerModeLocked();
            }
        }
    }

    private boolean shouldWakeUpWhenPluggedOrUnpluggedLocked(boolean wasPowered, int oldPlugType, boolean dockedOnWirelessCharger) {
        if (!this.mWakeUpWhenPluggedOrUnpluggedConfig) {
            return false;
        }
        if (wasPowered && (this.mIsPowered ^ 1) != 0 && oldPlugType == 4) {
            return false;
        }
        if (!wasPowered && this.mIsPowered && this.mPlugType == 4 && (dockedOnWirelessCharger ^ 1) != 0) {
            return false;
        }
        if (this.mIsPowered && this.mWakefulness == 2) {
            return false;
        }
        if (this.mTheaterModeEnabled && (this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig ^ 1) != 0) {
            return false;
        }
        if (this.mAlwaysOnEnabled && this.mWakefulness == 3) {
            return false;
        }
        return true;
    }

    private void updateStayOnLocked(int dirty) {
        if ((dirty & 288) != 0) {
            boolean wasStayOn = this.mStayOn;
            if (this.mStayOnWhilePluggedInSetting == 0 || (isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() ^ 1) == 0) {
                this.mStayOn = false;
            } else {
                this.mStayOn = this.mBatteryManagerInternal.isPowered(this.mStayOnWhilePluggedInSetting);
            }
            if (this.mStayOn != wasStayOn) {
                this.mDirty |= 128;
            }
        }
    }

    private void updateWakeLockSummaryLocked(int dirty) {
        if ((dirty & 3) != 0) {
            this.mWakeLockSummary = 0;
            this.mProximityLockFromInCallUi = false;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
                switch (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                    case 1:
                        if (!wakeLock.mDisabled) {
                            this.mWakeLockSummary |= 1;
                            break;
                        }
                        break;
                    case 6:
                        this.mWakeLockSummary |= 4;
                        break;
                    case 10:
                        this.mWakeLockSummary |= 2;
                        break;
                    case H.DO_ANIMATION_CALLBACK /*26*/:
                        this.mWakeLockSummary |= 10;
                        break;
                    case 32:
                        this.mWakeLockSummary |= 16;
                        if (!wakeLock.mPackageName.equals("com.android.incallui")) {
                            break;
                        }
                        this.mProximityLockFromInCallUi = true;
                        break;
                    case 64:
                        this.mWakeLockSummary |= 64;
                        break;
                    case 128:
                        this.mWakeLockSummary |= 128;
                        break;
                    default:
                        break;
                }
            }
            if (this.mWakefulness != 3) {
                this.mWakeLockSummary &= -193;
            }
            if (this.mWakefulness == 0 || (this.mWakeLockSummary & 64) != 0) {
                this.mWakeLockSummary &= -15;
                if (this.mWakefulness == 0) {
                    this.mWakeLockSummary &= -17;
                }
            }
            if (this.mProximityLockFromInCallUi) {
                this.mWakeLockSummary |= 16;
            }
            if ((this.mWakeLockSummary & 6) != 0) {
                if (this.mWakefulness == 1) {
                    this.mWakeLockSummary |= 33;
                } else if (this.mWakefulness == 2) {
                    this.mWakeLockSummary |= 1;
                }
            }
            if ((this.mWakeLockSummary & 128) != 0) {
                this.mWakeLockSummary |= 1;
            }
            if ((this.mWakeLockSummary & 16) == 0) {
                this.mProximityPositive = false;
            }
            this.mLastWakeLockSummary = this.mWakeLockSummary;
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateWakeLockSummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            }
        }
    }

    void checkForLongWakeLocks() {
        synchronized (this.mLock) {
            long now = SystemClock.uptimeMillis();
            this.mNotifyLongDispatched = now;
            long when = now - 60000;
            long nextCheckTime = JobStatus.NO_LATEST_RUNTIME;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
                if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && wakeLock.mNotifiedAcquired && (wakeLock.mNotifiedLong ^ 1) != 0) {
                    if (wakeLock.mAcquireTime < when) {
                        notifyWakeLockLongStartedLocked(wakeLock);
                    } else {
                        long checkTime = wakeLock.mAcquireTime + 60000;
                        if (checkTime < nextCheckTime) {
                            nextCheckTime = checkTime;
                        }
                    }
                }
            }
            this.mNotifyLongScheduled = 0;
            this.mHandler.removeMessages(4);
            if (nextCheckTime != JobStatus.NO_LATEST_RUNTIME) {
                this.mNotifyLongNextCheck = nextCheckTime;
                enqueueNotifyLongMsgLocked(nextCheckTime);
            } else {
                this.mNotifyLongNextCheck = 0;
            }
        }
    }

    private void updateUserActivitySummaryLocked(long now, int dirty) {
        if ((dirty & 39) != 0) {
            this.mHandler.removeMessages(1);
            long nextTimeout = 0;
            if (this.mWakefulness == 1 || this.mWakefulness == 2 || this.mWakefulness == 3) {
                int sleepTimeout = getSleepTimeoutLocked();
                int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
                int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
                boolean userInactiveOverride = this.mUserInactiveOverrideFromWindowManager;
                this.mUserActivitySummary = 0;
                if (this.mLastUserActivityTime >= this.mLastWakeTime) {
                    nextTimeout = (this.mLastUserActivityTime + ((long) screenOffTimeout)) - ((long) screenDimDuration);
                    if (now < nextTimeout) {
                        this.mUserActivitySummary = 1;
                    } else {
                        nextTimeout = this.mLastUserActivityTime + ((long) screenOffTimeout);
                        if (now < nextTimeout) {
                            this.mUserActivitySummary = 2;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0 && this.mLastUserActivityTimeNoChangeLights >= this.mLastWakeTime) {
                    nextTimeout = this.mLastUserActivityTimeNoChangeLights + ((long) screenOffTimeout);
                    if (now < nextTimeout) {
                        if (this.mDisplayPowerRequest.policy == 3 || this.mDisplayPowerRequest.policy == 4) {
                            this.mUserActivitySummary = 1;
                        } else if (this.mDisplayPowerRequest.policy == 2) {
                            this.mUserActivitySummary = 2;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0) {
                    if (sleepTimeout >= 0) {
                        long anyUserActivity = Math.max(this.mLastUserActivityTime, this.mLastUserActivityTimeNoChangeLights);
                        if (anyUserActivity >= this.mLastWakeTime) {
                            nextTimeout = anyUserActivity + ((long) sleepTimeout);
                            if (now < nextTimeout) {
                                this.mUserActivitySummary = 4;
                            }
                        }
                    } else {
                        this.mUserActivitySummary = 4;
                        nextTimeout = -1;
                    }
                }
                if (this.mUserActivitySummary != 4 && userInactiveOverride) {
                    if ((this.mUserActivitySummary & 3) != 0 && nextTimeout >= now && this.mOverriddenTimeout == -1) {
                        this.mOverriddenTimeout = nextTimeout;
                    }
                    this.mUserActivitySummary = 4;
                    nextTimeout = -1;
                }
                if (this.mUserActivitySummary != 0 && nextTimeout >= 0) {
                    Message msg = this.mHandler.obtainMessage(1);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, nextTimeout);
                }
            } else {
                this.mUserActivitySummary = 0;
            }
            Object obj = (!this.mProximityPositive || (this.mWakeLockSummary & 16) == 0) ? null : 1;
            if (obj == null && (dirty & 1) == 0 && nextTimeout >= 0 && this.mOppoHelper != null) {
                this.mOppoHelper.updateButtonBrightness(this, true);
            }
            if (this.mWakeLockCheck != null) {
                if (needScreenOnWakelockCheck()) {
                    this.mWakeLockCheck.screenOnWakelockCheckStart();
                } else {
                    this.mWakeLockCheck.screenOnWakelockCheckStop();
                }
            }
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateUserActivitySummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", nextTimeout=" + TimeUtils.formatUptime(nextTimeout));
            }
        }
        if (this.mOppoHelper != null && (dirty & 512) != 0 && this.mProximityPositive) {
            this.mOppoHelper.turnOffButtonLight();
        }
    }

    private void handleUserActivityTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_PANIC || DEBUG_SPEW) {
                Slog.d(TAG, "handleUserActivityTimeout");
            }
            this.mDirty |= 4;
            updatePowerStateLocked();
        }
    }

    private int getSleepTimeoutLocked() {
        int timeout = this.mSleepTimeoutSetting;
        if (timeout <= 0) {
            return -1;
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private int getScreenOffTimeoutLocked(int sleepTimeout) {
        int timeout = this.mScreenOffTimeoutSetting;
        if (isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
            timeout = Math.min(timeout, this.mMaximumScreenOffTimeoutFromDeviceAdmin);
        }
        if (this.mUserActivityTimeoutOverrideFromWindowManager >= 0) {
            timeout = (int) Math.min((long) timeout, this.mUserActivityTimeoutOverrideFromWindowManager);
        }
        if (sleepTimeout >= 0) {
            timeout = Math.min(timeout, sleepTimeout);
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private int getScreenDimDurationLocked(int screenOffTimeout) {
        return Math.min(this.mMaximumScreenDimDurationConfig, (int) (((float) screenOffTimeout) * this.mMaximumScreenDimRatioConfig));
    }

    private boolean updateWakefulnessLocked(int dirty) {
        if ((dirty & 1687) == 0 || this.mWakefulness != 1 || !isItBedTimeYetLocked()) {
            return false;
        }
        if (DEBUG_PANIC || DEBUG_SPEW) {
            Slog.d(TAG, "updateWakefulnessLocked: Bed time...");
        }
        long time = SystemClock.uptimeMillis();
        if (shouldNapAtBedTimeLocked()) {
            return napNoUpdateLocked(time, 1000);
        }
        return goToSleepNoUpdateLocked(time, 2, 0, 1000);
    }

    private boolean shouldNapAtBedTimeLocked() {
        if (this.mDreamsActivateOnSleepSetting) {
            return true;
        }
        if (this.mDreamsActivateOnDockSetting) {
            return this.mDockState != 0;
        } else {
            return false;
        }
    }

    private boolean isItBedTimeYetLocked() {
        return this.mBootCompleted ? isBeingKeptAwakeLocked() ^ 1 : false;
    }

    private boolean isBeingKeptAwakeLocked() {
        if (this.mStayOn || ((this.mProximityPositive && (this.useProximityForceSuspend ^ 1) != 0) || (this.mWakeLockSummary & 32) != 0 || (this.mUserActivitySummary & 3) != 0)) {
            return true;
        }
        return this.mScreenBrightnessBoostInProgress;
    }

    private void updateDreamLocked(int dirty, boolean displayBecameReady) {
        if (((dirty & 1015) != 0 || displayBecameReady) && this.mDisplayReady) {
            scheduleSandmanLocked();
        }
    }

    private void scheduleSandmanLocked() {
        if (!this.mSandmanScheduled) {
            this.mSandmanScheduled = true;
            Message msg = this.mHandler.obtainMessage(2);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    /* JADX WARNING: Missing block: B:34:0x0059, code:
            return;
     */
    /* JADX WARNING: Missing block: B:63:0x00e7, code:
            if (r0 == false) goto L_0x00ee;
     */
    /* JADX WARNING: Missing block: B:64:0x00e9, code:
            r12.mDreamManager.stopDream(false);
     */
    /* JADX WARNING: Missing block: B:65:0x00ee, code:
            return;
     */
    /* JADX WARNING: Missing block: B:67:0x00f0, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleSandman() {
        int wakefulness;
        boolean startDreaming;
        boolean isDreaming;
        synchronized (this.mLock) {
            this.mSandmanScheduled = false;
            wakefulness = this.mWakefulness;
            if (this.mSandmanSummoned && this.mDisplayReady) {
                startDreaming = !canDreamLocked() ? canDozeLocked() : true;
                this.mSandmanSummoned = false;
            } else {
                startDreaming = false;
            }
        }
        if (this.mDreamManager != null) {
            if (startDreaming) {
                boolean z;
                this.mDreamManager.stopDream(false);
                DreamManagerInternal dreamManagerInternal = this.mDreamManager;
                if (wakefulness == 3) {
                    z = true;
                } else {
                    z = false;
                }
                dreamManagerInternal.startDream(z);
            }
            isDreaming = this.mDreamManager.isDreaming();
        } else {
            isDreaming = false;
        }
        synchronized (this.mLock) {
            if (startDreaming && isDreaming) {
                this.mBatteryLevelWhenDreamStarted = this.mBatteryLevel;
                if (wakefulness == 3) {
                    Slog.i(TAG, "Dozing...");
                } else {
                    Slog.i(TAG, "Dreaming...");
                }
            }
            if (this.mSandmanSummoned || this.mWakefulness != wakefulness) {
            } else if (wakefulness == 2) {
                if (isDreaming) {
                    if (canDreamLocked()) {
                        if (this.mDreamsBatteryLevelDrainCutoffConfig < 0 || this.mBatteryLevel >= this.mBatteryLevelWhenDreamStarted - this.mDreamsBatteryLevelDrainCutoffConfig || (isBeingKeptAwakeLocked() ^ 1) == 0) {
                        } else {
                            Slog.i(TAG, "Stopping dream because the battery appears to be draining faster than it is charging.  Battery level when dream started: " + this.mBatteryLevelWhenDreamStarted + "%.  " + "Battery level now: " + this.mBatteryLevel + "%.");
                        }
                    }
                }
                if (isItBedTimeYetLocked()) {
                    Slog.i(TAG, "handleSandman: Bed time and goToSleepNoUpdateLocked");
                    goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 2, 0, 1000);
                    updatePowerStateLocked();
                } else {
                    Slog.i(TAG, "handleSandman: time to wakeUpNoUpdateLocked");
                    wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:DREAM", 1000, this.mContext.getOpPackageName(), 1000);
                    updatePowerStateLocked();
                }
            } else if (wakefulness == 3) {
                if (isDreaming) {
                } else {
                    reallyGoToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 1000);
                    updatePowerStateLocked();
                }
            }
        }
    }

    private boolean canDreamLocked() {
        if (this.mWakefulness != 2 || (this.mDreamsSupportedConfig ^ 1) != 0 || (this.mDreamsEnabledSetting ^ 1) != 0 || (this.mDisplayPowerRequest.isBrightOrDim() ^ 1) != 0 || this.mDisplayPowerRequest.isVr() || (this.mUserActivitySummary & 7) == 0 || (this.mBootCompleted ^ 1) != 0) {
            return false;
        }
        if (!isBeingKeptAwakeLocked()) {
            if (!this.mIsPowered && (this.mDreamsEnabledOnBatteryConfig ^ 1) != 0) {
                return false;
            }
            if (!this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig) {
                return false;
            }
            if (this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenPoweredConfig) {
                return false;
            }
        }
        return true;
    }

    private boolean canDozeLocked() {
        return this.mWakefulness == 3;
    }

    private boolean updateDisplayPowerStateLocked(int dirty) {
        boolean oldDisplayReady = this.mDisplayReady;
        if ((dirty & 14399) != 0) {
            boolean autoBrightness;
            OppoBrightUtils oppoBrightUtils;
            this.mDisplayPowerRequest.policy = getDesiredScreenPolicyLocked();
            this.useProximityForceSuspend = false;
            boolean brightnessSetByUser = true;
            int screenBrightness = this.mScreenBrightnessSettingDefault;
            float screenAutoBrightnessAdjustment = OppoBrightUtils.MIN_LUX_LIMITI;
            if (this.mScreenBrightnessModeSetting == 1) {
                autoBrightness = true;
            } else {
                autoBrightness = false;
            }
            boolean screenBrightnessOverrideFromWindowManager = false;
            if (this.mIsVrModeEnabled) {
                screenBrightness = this.mScreenBrightnessForVrSetting;
                autoBrightness = false;
            } else if (isValidBrightness(this.mScreenBrightnessOverrideFromWindowManager)) {
                screenBrightness = this.mScreenBrightnessOverrideFromWindowManager;
                if (autoBrightness) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mUseWindowBrightness = true;
                } else {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mUseWindowBrightness = false;
                }
                screenBrightnessOverrideFromWindowManager = true;
                autoBrightness = false;
                brightnessSetByUser = false;
            } else if (isValidBrightness(this.mTemporaryScreenBrightnessSettingOverride)) {
                screenBrightness = this.mTemporaryScreenBrightnessSettingOverride;
            } else if (isValidBrightness(this.mScreenBrightnessSetting)) {
                screenBrightness = this.mScreenBrightnessSetting;
            }
            if (this.mScreenBrightnessOverrideFromWindowManager == -1 && screenBrightnessOverrideFromWindowManager) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mUseWindowBrightness = true;
            }
            if (autoBrightness) {
                screenBrightness = mOppoBrightUtils.getBootupBrightness();
            }
            this.mDisplayPowerRequest.screenBrightness = Math.max(Math.min(screenBrightness, mScreenBrightnessSettingMaximum), mScreenBrightnessSettingMinimum);
            if (isValidAutoBrightnessAdjustment(this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride)) {
                screenAutoBrightnessAdjustment = this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
            } else if (isValidAutoBrightnessAdjustment(this.mScreenAutoBrightnessAdjustmentSetting)) {
                screenAutoBrightnessAdjustment = this.mScreenAutoBrightnessAdjustmentSetting;
            }
            if (screenAutoBrightnessAdjustment > OppoBrightUtils.MIN_LUX_LIMITI) {
                if (!mOppoBrightUtils.isSpecialAdj(screenAutoBrightnessAdjustment)) {
                    screenAutoBrightnessAdjustment = Math.max(Math.min(screenAutoBrightnessAdjustment, (float) mScreenBrightnessSettingMaximum), (float) mScreenBrightnessSettingMinimum);
                }
                this.mDisplayPowerRequest.screenAutoBrightnessAdjustment = screenAutoBrightnessAdjustment;
            }
            this.mDisplayPowerRequest.brightnessSetByUser = brightnessSetByUser;
            this.mDisplayPowerRequest.useAutoBrightness = autoBrightness;
            this.mDisplayPowerRequest.useProximitySensor = shouldUseProximitySensorLocked();
            this.mDisplayPowerRequest.lowPowerMode = this.mLowPowerModeEnabled;
            this.mDisplayPowerRequest.boostScreenBrightness = shouldBoostScreenBrightness();
            updatePowerRequestFromBatterySaverPolicy(this.mDisplayPowerRequest);
            if (this.mDisplayPowerRequest.policy == 1) {
                this.mDisplayPowerRequest.dozeScreenState = this.mDozeScreenStateOverrideFromDreamManager;
                if (this.mDisplayPowerRequest.dozeScreenState == 4 && (this.mWakeLockSummary & 128) != 0) {
                    this.mDisplayPowerRequest.dozeScreenState = 3;
                }
                this.mDisplayPowerRequest.dozeScreenBrightness = this.mDozeScreenBrightnessOverrideFromDreamManager;
            } else {
                this.mDisplayPowerRequest.dozeScreenState = 0;
                this.mDisplayPowerRequest.dozeScreenBrightness = -1;
            }
            if (!this.mProximityLockFromInCallUi && this.mWakefulness == 1) {
                this.mDisplayManagerInternal.setUseProximityForceSuspend(false);
            } else if (this.mProximityLockFromInCallUi) {
                this.mDisplayManagerInternal.setUseProximityForceSuspend(true);
                this.useProximityForceSuspend = true;
            }
            this.mDisplayReady = this.mDisplayManagerInternal.requestPowerState(this.mDisplayPowerRequest, this.mRequestWaitForNegativeProximity);
            this.mRequestWaitForNegativeProximity = false;
            if ((dirty & 4096) != 0) {
                sQuiescent = false;
            }
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateDisplayPowerStateLocked: mDisplayReady=" + this.mDisplayReady + ", policy=" + DisplayPowerRequest.policyToString(this.mDisplayPowerRequest.policy) + ", mWakefulness=" + this.mWakefulness + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", mBootCompleted=" + this.mBootCompleted + ", mScreenBrightnessBoostInProgress=" + this.mScreenBrightnessBoostInProgress + ", mIsVrModeEnabled= " + this.mIsVrModeEnabled + ", sQuiescent=" + sQuiescent);
            }
        }
        if (this.mDisplayReady) {
            return oldDisplayReady ^ 1;
        }
        return false;
    }

    private void updateScreenBrightnessBoostLocked(int dirty) {
        if ((dirty & 2048) != 0 && this.mScreenBrightnessBoostInProgress) {
            long now = SystemClock.uptimeMillis();
            this.mHandler.removeMessages(3);
            if (this.mLastScreenBrightnessBoostTime > this.mLastSleepTime) {
                long boostTimeout = this.mLastScreenBrightnessBoostTime + FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
                if (boostTimeout > now) {
                    Message msg = this.mHandler.obtainMessage(3);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, boostTimeout);
                    return;
                }
            }
            this.mScreenBrightnessBoostInProgress = false;
            this.mNotifier.onScreenBrightnessBoostChanged();
            userActivityNoUpdateLocked(now, 0, 0, 1000);
        }
    }

    private boolean shouldBoostScreenBrightness() {
        return !this.mIsVrModeEnabled ? this.mScreenBrightnessBoostInProgress : false;
    }

    private static boolean isValidBrightness(int value) {
        return value >= 0 && value <= mScreenBrightnessSettingMaximum;
    }

    private static boolean isValidAutoBrightnessAdjustment(float value) {
        if (value < ((float) mScreenBrightnessSettingMinimum) || value > ((float) mScreenBrightnessSettingMaximum)) {
            return mOppoBrightUtils.isSpecialAdj(value);
        }
        return true;
    }

    int getDesiredScreenPolicyLocked() {
        if (this.mWakefulness == 0 || sQuiescent) {
            return 0;
        }
        if (this.mWakefulness == 3) {
            if ((this.mWakeLockSummary & 64) != 0) {
                return 1;
            }
            if (this.mDozeAfterScreenOffConfig) {
                return 0;
            }
        }
        if (this.mIsVrModeEnabled) {
            return 4;
        }
        if ((this.mWakeLockSummary & 2) == 0 && (this.mUserActivitySummary & 1) == 0 && (this.mBootCompleted ^ 1) == 0 && !this.mScreenBrightnessBoostInProgress) {
            return 2;
        }
        return 3;
    }

    private boolean shouldUseProximitySensorLocked() {
        return (this.mIsVrModeEnabled || (this.mWakeLockSummary & 16) == 0) ? false : true;
    }

    private void updateSuspendBlockerLocked() {
        boolean needWakeLockSuspendBlocker = (this.mWakeLockSummary & 1) != 0;
        boolean needDisplaySuspendBlocker = needDisplaySuspendBlockerLocked();
        boolean autoSuspend = needDisplaySuspendBlocker ^ 1;
        boolean interactive = this.mDisplayPowerRequest.isBrightOrDim();
        if (!autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(false);
        }
        if (needWakeLockSuspendBlocker && (this.mHoldingWakeLockSuspendBlocker ^ 1) != 0) {
            this.mHoldingWakeLockSuspendBlocker = true;
            this.mWakeLockSuspendBlocker.acquire();
        }
        if (needDisplaySuspendBlocker && (this.mHoldingDisplaySuspendBlocker ^ 1) != 0) {
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
        }
        if (this.mDecoupleHalInteractiveModeFromDisplayConfig && (interactive || this.mDisplayReady)) {
            setHalInteractiveModeLocked(interactive);
        }
        if (!needWakeLockSuspendBlocker && this.mHoldingWakeLockSuspendBlocker) {
            this.mWakeLockSuspendBlocker.release();
            this.mHoldingWakeLockSuspendBlocker = false;
        }
        if (!needDisplaySuspendBlocker && this.mHoldingDisplaySuspendBlocker) {
            this.mDisplaySuspendBlocker.release();
            this.mHoldingDisplaySuspendBlocker = false;
        }
        if (autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(true);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0020, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean needDisplaySuspendBlockerLocked() {
        if (!this.mDisplayReady) {
            return true;
        }
        if ((!this.mDisplayPowerRequest.isBrightOrDim() || (this.mDisplayPowerRequest.useProximitySensor && (this.mProximityPositive ^ 1) == 0 && (this.mSuspendWhenScreenOffDueToProximityConfig ^ 1) == 0)) && !this.mScreenBrightnessBoostInProgress) {
            return false;
        }
        return true;
    }

    private void setHalAutoSuspendModeLocked(boolean enable) {
        if (enable != this.mHalAutoSuspendModeEnabled) {
            if (DEBUG_PANIC || DEBUG) {
                Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable);
            }
            this.mHalAutoSuspendModeEnabled = enable;
            Trace.traceBegin(131072, "setHalAutoSuspend(" + enable + ")");
            try {
                nativeSetAutoSuspend(enable);
            } finally {
                Trace.traceEnd(131072);
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable + " done");
                }
            }
        }
    }

    private void setHalInteractiveModeLocked(boolean enable) {
        if (enable != this.mHalInteractiveModeEnabled) {
            if (DEBUG_PANIC || DEBUG) {
                Slog.d(TAG, "Setting HAL interactive mode to " + enable);
            }
            this.mHalInteractiveModeEnabled = enable;
            Trace.traceBegin(131072, "setHalInteractive(" + enable + ")");
            try {
                nativeSetInteractive(enable);
            } finally {
                Trace.traceEnd(131072);
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "Setting HAL interactive mode to " + enable + " done");
                }
            }
        }
    }

    private boolean isInteractiveInternal() {
        boolean isInteractive;
        synchronized (this.mLock) {
            isInteractive = PowerManagerInternal.isInteractive(this.mWakefulness);
        }
        return isInteractive;
    }

    private boolean isLowPowerModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLowPowerModeEnabled;
        }
        return z;
    }

    private boolean getDisplayAodStatusInternal() {
        synchronized (this.mLock) {
            if (this.mWakefulness != 3) {
                return false;
            } else if (this.mDozeScreenStateOverrideFromDreamManager == 4 || this.mDozeScreenStateOverrideFromDreamManager == 3) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean setLowPowerModeInternal(boolean mode) {
        int i = 0;
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "setLowPowerModeInternal " + mode + " mIsPowered=" + this.mIsPowered);
            }
            if (this.mIsPowered) {
                return false;
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "low_power";
            if (mode) {
                i = 1;
            }
            Global.putInt(contentResolver, str, i);
            this.mLowPowerModeSetting = mode;
            if (this.mAutoLowPowerModeConfigured && this.mBatteryLevelLow) {
                if (mode && this.mAutoLowPowerModeSnoozing) {
                    if (DEBUG_SPEW) {
                        Slog.d(TAG, "setLowPowerModeInternal: clearing low power mode snooze");
                    }
                    this.mAutoLowPowerModeSnoozing = false;
                } else if (!mode) {
                    if ((this.mAutoLowPowerModeSnoozing ^ 1) != 0) {
                        if (DEBUG_SPEW) {
                            Slog.d(TAG, "setLowPowerModeInternal: snoozing low power mode");
                        }
                        this.mAutoLowPowerModeSnoozing = true;
                    }
                }
            }
            updateLowPowerModeLocked();
            return true;
        }
    }

    private boolean isColorOsLowPowerModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mColorOsLowPowerModeEnabled.get();
        }
        return z;
    }

    private boolean setColorOsLowPowerModeInternal(final boolean mode) {
        synchronized (this.mLock) {
            if (this.mColorOsLowPowerModeEnabled.get() == mode) {
                Slog.d(TAG, "setColorOsLowPowerModeInternal. mode no change. mode = " + mode);
                return false;
            }
            Slog.d(TAG, "setColorOsLowPowerModeInternal. mode =" + mode);
            this.mColorOsLowPowerModeEnabled.set(mode);
            postAfterBootCompleted(new Runnable() {
                public void run() {
                    ArrayList<LowPowerModeListener> listeners;
                    Slog.d(PowerManagerService.TAG, "setColorOsLowPowerModeInternal run " + mode);
                    synchronized (PowerManagerService.this.mLock) {
                        listeners = new ArrayList(PowerManagerService.this.mColorOsLowPowerModeListeners);
                    }
                    for (int i = 0; i < listeners.size(); i++) {
                        LowPowerModeListener listener = (LowPowerModeListener) listeners.get(i);
                        listener.onLowPowerModeChanged(PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(listener.getServiceType(), PowerManagerService.this.mColorOsLowPowerModeEnabled.get()));
                    }
                    PowerManagerService.this.hypnusLowPowerModeOn(mode);
                }
            });
            return true;
        }
    }

    private void hypnusLowPowerModeOn(boolean enable) {
        if (this.mHyp == null) {
            this.mHyp = new Hypnus();
        }
        if (enable && (this.mHypnusLowPowerenabled.get() ^ 1) != 0) {
            this.mHyp.hypnusSetNotification(3, 20);
            this.mHypnusLowPowerenabled.set(true);
            Slog.d(TAG, "hypnusLowPowerModeOn enable=" + enable);
        } else if (!enable && this.mHypnusLowPowerenabled.get()) {
            this.mHyp.hypnusSetNotification(3, 21);
            this.mHypnusLowPowerenabled.set(false);
            Slog.d(TAG, "hypnusLowPowerModeOn enable=" + enable);
        }
    }

    boolean isDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceIdleMode;
        }
        return z;
    }

    boolean isLightDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLightDeviceIdleMode;
        }
        return z;
    }

    private void handleBatteryStateChangedLocked() {
        this.mDirty |= 256;
        updatePowerStateLocked();
    }

    private void shutdownOrRebootInternal(final int haltMode, final boolean confirm, final String reason, boolean wait) {
        if (this.mHandler == null || (this.mSystemReady ^ 1) != 0) {
            if (RescueParty.isAttemptingFactoryReset()) {
                lowLevelReboot(reason);
            } else {
                throw new IllegalStateException("Too early to call shutdown() or reboot()");
            }
        }
        Runnable runnable = new Runnable() {
            public void run() {
                synchronized (this) {
                    if (haltMode == 2) {
                        ShutdownThread.rebootSafeMode(PowerManagerService.this.getUiContext(), confirm);
                    } else if (haltMode == 1) {
                        ShutdownThread.reboot(PowerManagerService.this.getUiContext(), reason, confirm);
                    } else {
                        ShutdownThread.shutdown(PowerManagerService.this.getUiContext(), reason, confirm);
                    }
                }
            }
        };
        Message msg = Message.obtain(UiThread.getHandler(), runnable);
        msg.setAsynchronous(true);
        UiThread.getHandler().sendMessage(msg);
        if (wait) {
            synchronized (runnable) {
                while (true) {
                    try {
                        runnable.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private void crashInternal(final String message) {
        Thread t = new Thread("PowerManagerService.crash()") {
            public void run() {
                throw new RuntimeException(message);
            }
        };
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            Slog.wtf(TAG, e);
        }
    }

    void updatePowerRequestFromBatterySaverPolicy(DisplayPowerRequest displayPowerRequest) {
        PowerSaveState state = this.mBatterySaverPolicy.getBatterySaverPolicy(7, this.mLowPowerModeEnabled);
        displayPowerRequest.lowPowerMode = state.batterySaverEnabled;
        displayPowerRequest.screenLowPowerBrightnessFactor = state.brightnessFactor;
    }

    void setStayOnSettingInternal(int val) {
        Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", val);
    }

    void setMaximumScreenOffTimeoutFromDeviceAdminInternal(int timeMs) {
        synchronized (this.mLock) {
            this.mMaximumScreenOffTimeoutFromDeviceAdmin = timeMs;
            this.mDirty |= 32;
            updatePowerStateLocked();
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0012, code:
            if (r2.mWakeLockCheck == null) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:13:0x0014, code:
            if (r3 == false) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:14:0x0016, code:
            r2.mWakeLockCheck.onDeviceIdle();
     */
    /* JADX WARNING: Missing block: B:15:0x001b, code:
            if (r3 == false) goto L_0x0028;
     */
    /* JADX WARNING: Missing block: B:16:0x001d, code:
            com.android.server.EventLogTags.writeDeviceIdleOnPhase("power");
     */
    /* JADX WARNING: Missing block: B:18:0x0024, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:22:0x0028, code:
            com.android.server.EventLogTags.writeDeviceIdleOffPhase("power");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean setDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode == enabled) {
                return false;
            }
            this.mDeviceIdleMode = enabled;
            updateWakeLockDisabledStatesLocked();
        }
    }

    boolean setLightDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mLightDeviceIdleMode != enabled) {
                this.mLightDeviceIdleMode = enabled;
                return true;
            }
            return false;
        }
    }

    void setDeviceIdleWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void setDeviceIdleTempWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleTempWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void startUidChangesInternal() {
        synchronized (this.mLock) {
            this.mUidsChanging = true;
        }
    }

    void finishUidChangesInternal() {
        synchronized (this.mLock) {
            this.mUidsChanging = false;
            if (this.mUidsChanged) {
                updateWakeLockDisabledStatesLocked();
                this.mUidsChanged = false;
            }
        }
    }

    private void handleUidStateChangeLocked() {
        if (this.mUidsChanging) {
            this.mUidsChanged = true;
        } else {
            updateWakeLockDisabledStatesLocked();
        }
    }

    void updateUidProcStateInternal(int uid, int procState) {
        boolean z = false;
        synchronized (this.mLock) {
            UidState state = (UidState) this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                this.mUidState.put(uid, state);
            }
            boolean oldShouldAllow = state.mProcState <= 12;
            state.mProcState = procState;
            if (state.mNumWakeLocks > 0) {
                if (this.mDeviceIdleMode) {
                    handleUidStateChangeLocked();
                } else if (!state.mActive) {
                    if (procState <= 12) {
                        z = true;
                    }
                    if (oldShouldAllow != z) {
                        handleUidStateChangeLocked();
                    }
                }
            }
        }
    }

    void uidGoneInternal(int uid) {
        synchronized (this.mLock) {
            int index = this.mUidState.indexOfKey(uid);
            if (index >= 0) {
                UidState state = (UidState) this.mUidState.valueAt(index);
                state.mProcState = 18;
                state.mActive = false;
                this.mUidState.removeAt(index);
                if (this.mDeviceIdleMode && state.mNumWakeLocks > 0) {
                    handleUidStateChangeLocked();
                }
            }
        }
    }

    void uidActiveInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = (UidState) this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                state.mProcState = 17;
                this.mUidState.put(uid, state);
            }
            state.mActive = true;
            if (state.mNumWakeLocks > 0) {
                handleUidStateChangeLocked();
            }
        }
    }

    void uidIdleInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = (UidState) this.mUidState.get(uid);
            if (state != null) {
                state.mActive = false;
                if (state.mNumWakeLocks > 0) {
                    handleUidStateChangeLocked();
                }
            }
        }
    }

    private void updateWakeLockDisabledStatesLocked() {
        boolean changed = false;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = 0; i < numWakeLocks; i++) {
            WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
            if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && setWakeLockDisabledStateLocked(wakeLock)) {
                changed = true;
                if (wakeLock.mDisabled) {
                    notifyWakeLockReleasedLocked(wakeLock);
                } else {
                    notifyWakeLockAcquiredLocked(wakeLock);
                }
            }
        }
        if (changed) {
            this.mDirty |= 1;
            updatePowerStateLocked();
        }
    }

    private boolean setWakeLockDisabledStateLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1) {
            boolean disabled = false;
            if (wakeLock.mPackageName != null && wakeLock.mPackageName.equals("com.mobiletools.systemhelper")) {
                return false;
            }
            int appid = UserHandle.getAppId(wakeLock.mOwnerUid);
            if (appid >= 10000) {
                if (this.mConstants.NO_CACHED_WAKE_LOCKS) {
                    disabled = (wakeLock.mUidState.mActive || wakeLock.mUidState.mProcState == 18) ? false : wakeLock.mUidState.mProcState > 12;
                }
                if (this.mDeviceIdleMode) {
                    UidState state = wakeLock.mUidState;
                    if (Arrays.binarySearch(this.mDeviceIdleWhitelist, appid) < 0 && Arrays.binarySearch(this.mDeviceIdleTempWhitelist, appid) < 0 && state.mProcState != 18 && state.mProcState > 4) {
                        disabled = true;
                    }
                }
            }
            if (wakeLock.mDisabled != disabled) {
                wakeLock.mDisabled = disabled;
                return true;
            }
        }
        return false;
    }

    private boolean isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() {
        if (this.mMaximumScreenOffTimeoutFromDeviceAdmin < 0 || this.mMaximumScreenOffTimeoutFromDeviceAdmin >= Integer.MAX_VALUE) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:10:0x000d, code:
            if (r5 == false) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:11:0x000f, code:
            r1 = 3;
     */
    /* JADX WARNING: Missing block: B:12:0x0010, code:
            r0.setFlashing(r6, 2, r1, 0);
     */
    /* JADX WARNING: Missing block: B:13:0x0014, code:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x0018, code:
            r1 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setAttentionLightInternal(boolean on, int color) {
        synchronized (this.mLock) {
            if (this.mSystemReady) {
                Light light = this.mAttentionLight;
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void boostScreenBrightnessInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (this.mSystemReady && this.mWakefulness != 0) {
                if (eventTime >= this.mLastScreenBrightnessBoostTime) {
                    Slog.i(TAG, "Brightness boost activated (uid " + uid + ")...");
                    this.mLastScreenBrightnessBoostTime = eventTime;
                    if (!this.mScreenBrightnessBoostInProgress) {
                        this.mScreenBrightnessBoostInProgress = true;
                        this.mNotifier.onScreenBrightnessBoostChanged();
                    }
                    this.mDirty |= 2048;
                    userActivityNoUpdateLocked(eventTime, 0, 0, uid);
                    updatePowerStateLocked();
                }
            }
        }
    }

    private boolean isScreenBrightnessBoostedInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenBrightnessBoostInProgress;
        }
        return z;
    }

    private void handleScreenBrightnessBoostTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleScreenBrightnessBoostTimeout");
            }
            this.mDirty |= 2048;
            updatePowerStateLocked();
        }
    }

    private void setScreenBrightnessOverrideFromWindowManagerInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mScreenBrightnessOverrideFromWindowManager != brightness) {
                this.mScreenBrightnessOverrideFromWindowManager = brightness;
                OppoBrightUtils oppoBrightUtils;
                if (this.mScreenBrightnessModeSetting != 1) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 1;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAdj = 0;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = false;
                } else if (brightness == -1) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 0;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = true;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetOverride = true;
                } else {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 1;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = false;
                    oppoBrightUtils = mOppoBrightUtils;
                    if (OppoBrightUtils.mFirstSetOverride) {
                        oppoBrightUtils = mOppoBrightUtils;
                        oppoBrightUtils = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAdj = OppoBrightUtils.mManualBrightness;
                        oppoBrightUtils = mOppoBrightUtils;
                        oppoBrightUtils = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.mManulAtAmbientLux;
                        oppoBrightUtils = mOppoBrightUtils;
                        OppoBrightUtils.mFirstSetOverride = false;
                    }
                }
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManager = " + brightness);
                }
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    private void setUserInactiveOverrideFromWindowManagerInternal() {
        synchronized (this.mLock) {
            this.mUserInactiveOverrideFromWindowManager = true;
            this.mDirty |= 4;
            updatePowerStateLocked();
        }
    }

    private void setUserActivityTimeoutOverrideFromWindowManagerInternal(long timeoutMillis) {
        synchronized (this.mLock) {
            if (this.mUserActivityTimeoutOverrideFromWindowManager != timeoutMillis) {
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "UA TimeoutOverrideFromWindowManagerInternal = " + timeoutMillis);
                }
                this.mUserActivityTimeoutOverrideFromWindowManager = timeoutMillis;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    private void setTemporaryScreenBrightnessSettingOverrideInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mTemporaryScreenBrightnessSettingOverride != brightness) {
                this.mTemporaryScreenBrightnessSettingOverride = brightness;
                if (DEBUG_PANIC || DEBUG) {
                    Slog.d(TAG, "mTemporaryScreenBrightnessSettingOverride = " + brightness);
                }
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    private void setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal(float adj) {
        synchronized (this.mLock) {
            OppoBrightUtils oppoBrightUtils;
            if (adj == (((float) mScreenBrightnessSettingMaximum) * 300.0f) / 255.0f || adj == (((float) mScreenBrightnessSettingMaximum) * 301.0f) / 255.0f) {
                Slog.d(TAG, "adj == " + adj);
                if (adj == (((float) mScreenBrightnessSettingMaximum) * 300.0f) / 255.0f) {
                    Slog.d(TAG, "Camera(boot gallery from camera)");
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mCameraBacklight = true;
                } else {
                    Slog.d(TAG, "boot gallery from desk or file system");
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mGalleryBacklight = true;
                }
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraMode = 1;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraUseAdjustmentSetting = true;
                this.mDisplayManagerInternal.setOutdoorMode(true);
            } else if (adj == (((float) mScreenBrightnessSettingMaximum) * 500.0f) / 255.0f) {
                Slog.d(TAG, "adj == " + adj);
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraBacklight = false;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mGalleryBacklight = false;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraMode = 0;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraUseAdjustmentSetting = true;
                this.mDisplayManagerInternal.setOutdoorMode(false);
                if (this.mScreenBrightnessModeSetting == 0) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mCameraMode = -1;
                }
            }
            if (adj == 16384.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mInverseMode = 1;
            } else if (adj == 32768.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mInverseMode = 0;
            }
            if (adj == 16385.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mGalleryMode = 1;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mShouldAdjustRate = 1;
            } else if (adj == 32769.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mGalleryMode = 0;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mShouldAdjustRate = 1;
            }
            if (adj == 16386.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mVideoMode = 1;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mShouldAdjustRate = 1;
            } else if (adj == 32770.0f) {
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mVideoMode = 0;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mShouldAdjustRate = 1;
            }
            if (this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride != adj) {
                if (DEBUG_PANIC || DEBUG) {
                    Slog.d(TAG, "setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal = " + adj);
                }
                this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = adj;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    private void setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        synchronized (this.mLock) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    void setVrModeEnabled(boolean enabled) {
        this.mIsVrModeEnabled = enabled;
    }

    private void powerHintInternal(int hintId, int data) {
        nativeSendPowerHint(hintId, data);
    }

    public static void lowLevelShutdown(String reason) {
        if (reason == null) {
            reason = "";
        }
        SystemProperties.set("sys.powerctl", "shutdown," + reason);
        Slog.d(TAG, "lowLevelShutdown, sys.powerctl = shutdown");
    }

    public static void lowLevelReboot(String reason) {
        if (reason == null) {
            reason = "";
        }
        if (reason.equals("quiescent")) {
            sQuiescent = true;
            reason = "";
        } else if (reason.endsWith(",quiescent")) {
            sQuiescent = true;
            reason = reason.substring(0, (reason.length() - "quiescent".length()) - 1);
        }
        if (reason.equals("recovery") || reason.equals("recovery-update")) {
            reason = "recovery";
        } else {
            HashSet<String> validReasons = new HashSet();
            validReasons.add("recovery");
            validReasons.add("rf");
            validReasons.add("wlan");
            validReasons.add("mos");
            validReasons.add("ftm");
            validReasons.add("silence");
            validReasons.add("sau");
            if (!validReasons.contains(reason)) {
                Slog.w(TAG, "ignore unknown reboot reason [" + reason + "]");
                reason = "";
            }
        }
        if (sQuiescent) {
            reason = reason + ",quiescent";
        }
        SystemProperties.set("sys.powerctl", "reboot," + reason);
        Slog.d(TAG, "lowLevelReboot, reboot reason is " + reason);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Slog.wtf(TAG, "Unexpected return from lowLevelReboot!");
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    protected boolean dynamicallyConfigPowerManagerServiceLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (!"log".equals(args[0])) {
            return false;
        }
        if (args.length != 3) {
            pw.println("Invalid argument! Get detail help as bellow:");
            logOutPowerManagerServiceLogTagHelp(pw);
            return true;
        }
        pw.println("dynamicallyConfigPowerManagerServiceLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigPowerManagerServiceLogTag, args[" + index + "]:" + args[index]);
        }
        String logCategoryTag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigPowerManagerServiceLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
        if ("all".equals(logCategoryTag)) {
            DEBUG_PANIC = on;
            DEBUG = on;
            DEBUG_SPEW = on;
            LightsService.DEBUG = on;
            Notifier.DEBUG_PANIC = on;
        } else {
            pw.println("Invalid log tag argument! Get detail help as bellow:");
            logOutPowerManagerServiceLogTagHelp(pw);
        }
        return true;
    }

    protected void logOutPowerManagerServiceLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All PowerManagerService log");
        pw.println("cmd: dumpsys power log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private void dumpInternal(PrintWriter pw) {
        WirelessChargerDetector wcd;
        pw.println("POWER MANAGER (dumpsys power)\n");
        synchronized (this.mLock) {
            pw.println("Power Manager State:");
            this.mConstants.dump(pw);
            pw.println("  mDirty=0x" + Integer.toHexString(this.mDirty));
            pw.println("  mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness));
            pw.println("  mWakefulnessChanging=" + this.mWakefulnessChanging);
            pw.println("  mIsPowered=" + this.mIsPowered);
            pw.println("  mPlugType=" + this.mPlugType);
            pw.println("  mBatteryLevel=" + this.mBatteryLevel);
            pw.println("  mBatteryLevelWhenDreamStarted=" + this.mBatteryLevelWhenDreamStarted);
            pw.println("  mDockState=" + this.mDockState);
            pw.println("  mStayOn=" + this.mStayOn);
            pw.println("  mProximityPositive=" + this.mProximityPositive);
            pw.println("  mBootCompleted=" + this.mBootCompleted);
            pw.println("  mSystemReady=" + this.mSystemReady);
            pw.println("  mHalAutoSuspendModeEnabled=" + this.mHalAutoSuspendModeEnabled);
            pw.println("  mHalInteractiveModeEnabled=" + this.mHalInteractiveModeEnabled);
            pw.println("  mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            pw.print("  mNotifyLongScheduled=");
            if (this.mNotifyLongScheduled == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongScheduled, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.print("  mNotifyLongDispatched=");
            if (this.mNotifyLongDispatched == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongDispatched, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.print("  mNotifyLongNextCheck=");
            if (this.mNotifyLongNextCheck == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongNextCheck, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.println("  mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary));
            pw.println("  mRequestWaitForNegativeProximity=" + this.mRequestWaitForNegativeProximity);
            pw.println("  mSandmanScheduled=" + this.mSandmanScheduled);
            pw.println("  mSandmanSummoned=" + this.mSandmanSummoned);
            pw.println("  mLowPowerModeEnabled=" + this.mLowPowerModeEnabled);
            pw.println("  mBatteryLevelLow=" + this.mBatteryLevelLow);
            pw.println("  mLightDeviceIdleMode=" + this.mLightDeviceIdleMode);
            pw.println("  mDeviceIdleMode=" + this.mDeviceIdleMode);
            pw.println("  mDeviceIdleWhitelist=" + Arrays.toString(this.mDeviceIdleWhitelist));
            pw.println("  mDeviceIdleTempWhitelist=" + Arrays.toString(this.mDeviceIdleTempWhitelist));
            pw.println("  mLastWakeTime=" + TimeUtils.formatUptime(this.mLastWakeTime));
            pw.println("  mLastSleepTime=" + TimeUtils.formatUptime(this.mLastSleepTime));
            pw.println("  mLastUserActivityTime=" + TimeUtils.formatUptime(this.mLastUserActivityTime));
            pw.println("  mLastUserActivityTimeNoChangeLights=" + TimeUtils.formatUptime(this.mLastUserActivityTimeNoChangeLights));
            pw.println("  mLastInteractivePowerHintTime=" + TimeUtils.formatUptime(this.mLastInteractivePowerHintTime));
            pw.println("  mLastScreenBrightnessBoostTime=" + TimeUtils.formatUptime(this.mLastScreenBrightnessBoostTime));
            pw.println("  mScreenBrightnessBoostInProgress=" + this.mScreenBrightnessBoostInProgress);
            pw.println("  mDisplayReady=" + this.mDisplayReady);
            pw.println("  mHoldingWakeLockSuspendBlocker=" + this.mHoldingWakeLockSuspendBlocker);
            pw.println("  mHoldingDisplaySuspendBlocker=" + this.mHoldingDisplaySuspendBlocker);
            pw.println();
            pw.println("Settings and Configuration:");
            pw.println("  mDecoupleHalAutoSuspendModeFromDisplayConfig=" + this.mDecoupleHalAutoSuspendModeFromDisplayConfig);
            pw.println("  mDecoupleHalInteractiveModeFromDisplayConfig=" + this.mDecoupleHalInteractiveModeFromDisplayConfig);
            pw.println("  mWakeUpWhenPluggedOrUnpluggedConfig=" + this.mWakeUpWhenPluggedOrUnpluggedConfig);
            pw.println("  mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig=" + this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig);
            pw.println("  mTheaterModeEnabled=" + this.mTheaterModeEnabled);
            pw.println("  mSuspendWhenScreenOffDueToProximityConfig=" + this.mSuspendWhenScreenOffDueToProximityConfig);
            pw.println("  mDreamsSupportedConfig=" + this.mDreamsSupportedConfig);
            pw.println("  mDreamsEnabledByDefaultConfig=" + this.mDreamsEnabledByDefaultConfig);
            pw.println("  mDreamsActivatedOnSleepByDefaultConfig=" + this.mDreamsActivatedOnSleepByDefaultConfig);
            pw.println("  mDreamsActivatedOnDockByDefaultConfig=" + this.mDreamsActivatedOnDockByDefaultConfig);
            pw.println("  mDreamsEnabledOnBatteryConfig=" + this.mDreamsEnabledOnBatteryConfig);
            pw.println("  mDreamsBatteryLevelMinimumWhenPoweredConfig=" + this.mDreamsBatteryLevelMinimumWhenPoweredConfig);
            pw.println("  mDreamsBatteryLevelMinimumWhenNotPoweredConfig=" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            pw.println("  mDreamsBatteryLevelDrainCutoffConfig=" + this.mDreamsBatteryLevelDrainCutoffConfig);
            pw.println("  mDreamsEnabledSetting=" + this.mDreamsEnabledSetting);
            pw.println("  mDreamsActivateOnSleepSetting=" + this.mDreamsActivateOnSleepSetting);
            pw.println("  mDreamsActivateOnDockSetting=" + this.mDreamsActivateOnDockSetting);
            pw.println("  mDozeAfterScreenOffConfig=" + this.mDozeAfterScreenOffConfig);
            pw.println("  mLowPowerModeSetting=" + this.mLowPowerModeSetting);
            pw.println("  mAutoLowPowerModeConfigured=" + this.mAutoLowPowerModeConfigured);
            pw.println("  mAutoLowPowerModeSnoozing=" + this.mAutoLowPowerModeSnoozing);
            pw.println("  mMinimumScreenOffTimeoutConfig=" + this.mMinimumScreenOffTimeoutConfig);
            pw.println("  mMaximumScreenDimDurationConfig=" + this.mMaximumScreenDimDurationConfig);
            pw.println("  mMaximumScreenDimRatioConfig=" + this.mMaximumScreenDimRatioConfig);
            pw.println("  mScreenOffTimeoutSetting=" + this.mScreenOffTimeoutSetting);
            pw.println("  mSleepTimeoutSetting=" + this.mSleepTimeoutSetting);
            pw.println("  mMaximumScreenOffTimeoutFromDeviceAdmin=" + this.mMaximumScreenOffTimeoutFromDeviceAdmin + " (enforced=" + isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() + ")");
            pw.println("  mStayOnWhilePluggedInSetting=" + this.mStayOnWhilePluggedInSetting);
            pw.println("  mScreenBrightnessSetting=" + this.mScreenBrightnessSetting);
            pw.println("  mScreenAutoBrightnessAdjustmentSetting=" + this.mScreenAutoBrightnessAdjustmentSetting);
            pw.println("  mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
            pw.println("  mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
            pw.println("  mUserActivityTimeoutOverrideFromWindowManager=" + this.mUserActivityTimeoutOverrideFromWindowManager);
            pw.println("  mUserInactiveOverrideFromWindowManager=" + this.mUserInactiveOverrideFromWindowManager);
            pw.println("  mTemporaryScreenBrightnessSettingOverride=" + this.mTemporaryScreenBrightnessSettingOverride);
            pw.println("  mTemporaryScreenAutoBrightnessAdjustmentSettingOverride=" + this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride);
            pw.println("  mDozeScreenStateOverrideFromDreamManager=" + this.mDozeScreenStateOverrideFromDreamManager);
            pw.println("  mDozeScreenBrightnessOverrideFromDreamManager=" + this.mDozeScreenBrightnessOverrideFromDreamManager);
            pw.println("  mScreenBrightnessSettingMinimum=" + mScreenBrightnessSettingMinimum);
            pw.println("  mScreenBrightnessSettingMaximum=" + mScreenBrightnessSettingMaximum);
            pw.println("  mScreenBrightnessSettingDefault=" + this.mScreenBrightnessSettingDefault);
            pw.println("  mScreenBrightnessForVrSettingDefault=" + this.mScreenBrightnessForVrSettingDefault);
            pw.println("  mScreenBrightnessForVrSetting=" + this.mScreenBrightnessForVrSetting);
            pw.println("  mDoubleTapWakeEnabled=" + this.mDoubleTapWakeEnabled);
            pw.println("  mIsVrModeEnabled=" + this.mIsVrModeEnabled);
            int sleepTimeout = getSleepTimeoutLocked();
            int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            pw.println();
            pw.println("Sleep timeout: " + sleepTimeout + " ms");
            pw.println("Screen off timeout: " + screenOffTimeout + " ms");
            pw.println("Screen dim duration: " + screenDimDuration + " ms");
            pw.println();
            pw.print("UID states (changing=");
            pw.print(this.mUidsChanging);
            pw.print(" changed=");
            pw.print(this.mUidsChanged);
            pw.println("):");
            for (int i = 0; i < this.mUidState.size(); i++) {
                UidState state = (UidState) this.mUidState.valueAt(i);
                pw.print("  UID ");
                UserHandle.formatUid(pw, this.mUidState.keyAt(i));
                pw.print(": ");
                if (state.mActive) {
                    pw.print("  ACTIVE ");
                } else {
                    pw.print("INACTIVE ");
                }
                pw.print(" count=");
                pw.print(state.mNumWakeLocks);
                pw.print(" state=");
                pw.println(state.mProcState);
            }
            pw.println();
            pw.println("Looper state:");
            this.mHandler.getLooper().dump(new PrintWriterPrinter(pw), "  ");
            pw.println();
            pw.println("Wake Locks: size=" + this.mWakeLocks.size());
            for (WakeLock wl : this.mWakeLocks) {
                pw.println("  " + wl);
            }
            pw.println();
            pw.println("Suspend Blockers: size=" + this.mSuspendBlockers.size());
            for (SuspendBlocker sb : this.mSuspendBlockers) {
                pw.println("  " + sb);
            }
            pw.println();
            pw.println("Display Power: " + this.mDisplayPowerCallbacks);
            this.mBatterySaverPolicy.dump(pw);
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.dump(pw);
        }
    }

    private void updateChargeStateForSaleInternal(boolean enable) {
        if (enable) {
            enableCharge();
        } else {
            disableCharge();
        }
    }

    private void disableCharge() {
        SystemProperties.set("sys.engineermode.chargeswitch", "false");
        Slog.d(TAG, "disableCharge");
    }

    private void enableCharge() {
        SystemProperties.set("sys.engineermode.chargeswitch", "true");
        Slog.d(TAG, "enableCharge");
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a8 A:{SYNTHETIC, Splitter: B:33:0x00a8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getCurrentChargeStateForSaleInternal() {
        IOException e;
        Throwable th;
        File file = new File("/sys/class/power_supply/battery/mmi_charging_enable");
        BufferedReader reader = null;
        String tempString;
        int result;
        try {
            if (file.exists()) {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    tempString = reader2.readLine();
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e1) {
                            Slog.e(TAG, "readIntFromFile io close exception :" + e1.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
                if (tempString != null || tempString.trim().length() == 0) {
                    result = -1;
                } else {
                    try {
                        result = Integer.valueOf(tempString).intValue();
                    } catch (NumberFormatException e3) {
                        result = -1;
                        Slog.e(TAG, "readIntFromFile NumberFormatException:" + e3.getMessage());
                    }
                }
                return result;
            }
            Slog.e(TAG, "mmi_charging_enable is no existed");
            return -2;
        } catch (IOException e4) {
            e = e4;
            tempString = null;
            try {
                Slog.e(TAG, "readIntFromFile io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e12.getMessage());
                    }
                }
                if (tempString != null) {
                }
                result = -1;
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
    }

    private void dumpProto(FileDescriptor fd) {
        WirelessChargerDetector wcd;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            this.mConstants.dumpProto(proto);
            proto.write(1112396529666L, this.mDirty);
            proto.write(1168231104515L, this.mWakefulness);
            proto.write(1155346202628L, this.mWakefulnessChanging);
            proto.write(1155346202629L, this.mIsPowered);
            proto.write(1168231104518L, this.mPlugType);
            proto.write(1112396529671L, this.mBatteryLevel);
            proto.write(1112396529672L, this.mBatteryLevelWhenDreamStarted);
            proto.write(1168231104521L, this.mDockState);
            proto.write(1155346202634L, this.mStayOn);
            proto.write(1155346202635L, this.mProximityPositive);
            proto.write(1155346202636L, this.mBootCompleted);
            proto.write(1155346202637L, this.mSystemReady);
            proto.write(1155346202638L, this.mHalAutoSuspendModeEnabled);
            proto.write(1155346202639L, this.mHalInteractiveModeEnabled);
            long activeWakeLocksToken = proto.start(1172526071824L);
            proto.write(1155346202625L, (this.mWakeLockSummary & 1) != 0);
            proto.write(1155346202626L, (this.mWakeLockSummary & 2) != 0);
            proto.write(1155346202627L, (this.mWakeLockSummary & 4) != 0);
            proto.write(1155346202628L, (this.mWakeLockSummary & 8) != 0);
            proto.write(1155346202629L, (this.mWakeLockSummary & 16) != 0);
            proto.write(1155346202630L, (this.mWakeLockSummary & 32) != 0);
            proto.write(1155346202631L, (this.mWakeLockSummary & 64) != 0);
            proto.write(1155346202632L, (this.mWakeLockSummary & 128) != 0);
            proto.end(activeWakeLocksToken);
            proto.write(1116691496977L, this.mNotifyLongScheduled);
            proto.write(1116691496978L, this.mNotifyLongDispatched);
            proto.write(1116691496979L, this.mNotifyLongNextCheck);
            long userActivityToken = proto.start(1172526071828L);
            proto.write(1155346202625L, (this.mUserActivitySummary & 1) != 0);
            proto.write(1155346202626L, (this.mUserActivitySummary & 2) != 0);
            proto.write(1155346202627L, (this.mUserActivitySummary & 4) != 0);
            proto.end(userActivityToken);
            proto.write(1155346202645L, this.mRequestWaitForNegativeProximity);
            proto.write(1155346202646L, this.mSandmanScheduled);
            proto.write(1155346202647L, this.mSandmanSummoned);
            proto.write(1155346202648L, this.mLowPowerModeEnabled);
            proto.write(1155346202649L, this.mBatteryLevelLow);
            proto.write(1155346202650L, this.mLightDeviceIdleMode);
            proto.write(1155346202651L, this.mDeviceIdleMode);
            for (int id : this.mDeviceIdleWhitelist) {
                proto.write(2211908157468L, id);
            }
            for (int id2 : this.mDeviceIdleTempWhitelist) {
                proto.write(2211908157469L, id2);
            }
            proto.write(1116691496990L, this.mLastWakeTime);
            proto.write(1116691496991L, this.mLastSleepTime);
            proto.write(1116691496992L, this.mLastUserActivityTime);
            proto.write(1116691496993L, this.mLastUserActivityTimeNoChangeLights);
            proto.write(1116691496994L, this.mLastInteractivePowerHintTime);
            proto.write(1116691496995L, this.mLastScreenBrightnessBoostTime);
            proto.write(1155346202660L, this.mScreenBrightnessBoostInProgress);
            proto.write(1155346202661L, this.mDisplayReady);
            proto.write(1155346202662L, this.mHoldingWakeLockSuspendBlocker);
            proto.write(1155346202663L, this.mHoldingDisplaySuspendBlocker);
            long settingsAndConfigurationToken = proto.start(1172526071848L);
            proto.write(1155346202625L, this.mDecoupleHalAutoSuspendModeFromDisplayConfig);
            proto.write(1155346202626L, this.mDecoupleHalInteractiveModeFromDisplayConfig);
            proto.write(1155346202627L, this.mWakeUpWhenPluggedOrUnpluggedConfig);
            proto.write(1155346202628L, this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig);
            proto.write(1155346202629L, this.mTheaterModeEnabled);
            proto.write(1155346202630L, this.mSuspendWhenScreenOffDueToProximityConfig);
            proto.write(1155346202631L, this.mDreamsSupportedConfig);
            proto.write(1155346202632L, this.mDreamsEnabledByDefaultConfig);
            proto.write(1155346202633L, this.mDreamsActivatedOnSleepByDefaultConfig);
            proto.write(1155346202634L, this.mDreamsActivatedOnDockByDefaultConfig);
            proto.write(1155346202635L, this.mDreamsEnabledOnBatteryConfig);
            proto.write(1129576398860L, this.mDreamsBatteryLevelMinimumWhenPoweredConfig);
            proto.write(1129576398861L, this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            proto.write(1129576398862L, this.mDreamsBatteryLevelDrainCutoffConfig);
            proto.write(1155346202639L, this.mDreamsEnabledSetting);
            proto.write(1155346202640L, this.mDreamsActivateOnSleepSetting);
            proto.write(1155346202641L, this.mDreamsActivateOnDockSetting);
            proto.write(1155346202642L, this.mDozeAfterScreenOffConfig);
            proto.write(1155346202643L, this.mLowPowerModeSetting);
            proto.write(1155346202644L, this.mAutoLowPowerModeConfigured);
            proto.write(1155346202645L, this.mAutoLowPowerModeSnoozing);
            proto.write(1112396529686L, this.mMinimumScreenOffTimeoutConfig);
            proto.write(1112396529687L, this.mMaximumScreenDimDurationConfig);
            proto.write(1108101562392L, this.mMaximumScreenDimRatioConfig);
            proto.write(1112396529689L, this.mScreenOffTimeoutSetting);
            proto.write(1129576398874L, this.mSleepTimeoutSetting);
            proto.write(1112396529691L, this.mMaximumScreenOffTimeoutFromDeviceAdmin);
            proto.write(1155346202652L, isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked());
            long stayOnWhilePluggedInToken = proto.start(1172526071837L);
            proto.write(1155346202625L, (this.mStayOnWhilePluggedInSetting & 1) != 0);
            proto.write(1155346202626L, (this.mStayOnWhilePluggedInSetting & 2) != 0);
            proto.write(1155346202627L, (this.mStayOnWhilePluggedInSetting & 4) != 0);
            proto.end(stayOnWhilePluggedInToken);
            proto.write(1129576398878L, this.mScreenBrightnessSetting);
            proto.write(1108101562399L, this.mScreenAutoBrightnessAdjustmentSetting);
            proto.write(1168231104544L, this.mScreenBrightnessModeSetting);
            proto.write(1129576398881L, this.mScreenBrightnessOverrideFromWindowManager);
            proto.write(1133871366178L, this.mUserActivityTimeoutOverrideFromWindowManager);
            proto.write(1155346202659L, this.mUserInactiveOverrideFromWindowManager);
            proto.write(1129576398884L, this.mTemporaryScreenBrightnessSettingOverride);
            proto.write(1108101562405L, this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride);
            proto.write(1168231104550L, this.mDozeScreenStateOverrideFromDreamManager);
            proto.write(1108101562407L, this.mDozeScreenBrightnessOverrideFromDreamManager);
            long screenBrightnessSettingLimitsToken = proto.start(1172526071848L);
            proto.write(1112396529665L, mScreenBrightnessSettingMinimum);
            proto.write(1112396529666L, mScreenBrightnessSettingMaximum);
            proto.write(1112396529667L, this.mScreenBrightnessSettingDefault);
            proto.write(1112396529668L, this.mScreenBrightnessForVrSettingDefault);
            proto.end(screenBrightnessSettingLimitsToken);
            proto.write(1112396529705L, this.mScreenBrightnessForVrSetting);
            proto.write(1155346202666L, this.mDoubleTapWakeEnabled);
            proto.write(1155346202667L, this.mIsVrModeEnabled);
            proto.end(settingsAndConfigurationToken);
            int sleepTimeout = getSleepTimeoutLocked();
            int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            proto.write(1129576398889L, sleepTimeout);
            proto.write(1112396529706L, screenOffTimeout);
            proto.write(1112396529707L, screenDimDuration);
            proto.write(1155346202668L, this.mUidsChanging);
            proto.write(1155346202669L, this.mUidsChanged);
            for (int i = 0; i < this.mUidState.size(); i++) {
                UidState state = (UidState) this.mUidState.valueAt(i);
                long uIDToken = proto.start(2272037699630L);
                int uid = this.mUidState.keyAt(i);
                proto.write(1112396529665L, uid);
                proto.write(1159641169922L, UserHandle.formatUid(uid));
                proto.write(1155346202627L, state.mActive);
                proto.write(1112396529668L, state.mNumWakeLocks);
                if (state.mProcState == -1) {
                    proto.write(1155346202629L, true);
                } else {
                    proto.write(1168231104518L, state.mProcState);
                }
                proto.end(uIDToken);
            }
            this.mHandler.getLooper().writeToProto(proto, 1172526071855L);
            for (WakeLock wl : this.mWakeLocks) {
                wl.writeToProto(proto, 2272037699632L);
            }
            for (SuspendBlocker sb : this.mSuspendBlockers) {
                sb.writeToProto(proto, 2272037699633L);
            }
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.writeToProto(proto, 1172526071858L);
        }
        proto.flush();
    }

    private SuspendBlocker createSuspendBlockerLocked(String name) {
        SuspendBlocker suspendBlocker = new SuspendBlockerImpl(name);
        this.mSuspendBlockers.add(suspendBlocker);
        return suspendBlocker;
    }

    private void incrementBootCount() {
        synchronized (this.mLock) {
            int count;
            try {
                count = Global.getInt(getContext().getContentResolver(), "boot_count");
            } catch (SettingNotFoundException e) {
                count = 0;
            }
            Global.putInt(getContext().getContentResolver(), "boot_count", count + 1);
        }
    }

    private static WorkSource copyWorkSource(WorkSource workSource) {
        return workSource != null ? new WorkSource(workSource) : null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0036 A:{SYNTHETIC, Splitter: B:24:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0049 A:{Catch:{ IOException -> 0x003c }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x003b A:{SYNTHETIC, Splitter: B:27:0x003b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int getLastShutdownReasonInternal(File lastRebootReason) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        String line = "";
        BufferedReader bufferedReader = null;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(lastRebootReason));
            try {
                line = bufferedReader2.readLine();
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        bufferedReader = bufferedReader2;
                    }
                } else {
                    if (line != null) {
                        return 0;
                    }
                    if (line.equals(REASON_SHUTDOWN)) {
                        return 1;
                    }
                    if (line.equals(REASON_REBOOT)) {
                        return 2;
                    }
                    if (line.equals(REASON_USERREQUESTED)) {
                        return 3;
                    }
                    if (line.equals(REASON_THERMAL_SHUTDOWN)) {
                        return 4;
                    }
                    return 0;
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Slog.e(TAG, "Failed to read last_reboot_reason file", e);
                        if (line != null) {
                        }
                    }
                } else {
                    throw th;
                }
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedReader != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public int getUserActivitySummary() {
        return this.mUserActivitySummary;
    }

    public int getwakefulness() {
        return this.mWakefulness;
    }

    public boolean needScreenOnWakelockCheck() {
        if (this.mWakefulness == 1 && this.mUserActivitySummary == 4 && (this.mHandler.hasMessages(1) ^ 1) != 0) {
            return true;
        }
        return false;
    }

    public void releaseWakeLockByGuardElf(IBinder lock, int flags) {
        releaseWakeLockInternal(lock, flags);
    }

    public WakeLock cloneWakeLock(WakeLock wl) {
        return new WakeLock(wl.mLock, wl.mFlags, wl.mTag, wl.mPackageName, wl.mWorkSource, wl.mHistoryTag, wl.mOwnerUid, wl.mOwnerPid, wl.mUidState);
    }

    private boolean dumpPossibleMusicPlayer(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1) {
            return false;
        }
        String cmd = args[0];
        if ("possibleMusicPlayer".equals(cmd)) {
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.dumpPossibleMusicPlayer(pw);
            }
            return true;
        } else if ("wakelockCheck".equals(cmd) && "log".equals(args[1])) {
            if ("on".equals(args[2])) {
                this.mWakeLockCheck.logSwitch(true);
                pw.println("wakelockCheck log on.");
            } else if ("off".equals(args[2])) {
                this.mWakeLockCheck.logSwitch(false);
                pw.println("wakelockCheck log off.");
            }
            return true;
        } else if (!"cameraState".equals(cmd)) {
            return false;
        } else {
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.dumpCameraState(pw);
            }
            return true;
        }
    }

    /* JADX WARNING: Missing block: B:34:0x00ab, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:43:0x00b9, code:
            if (r0 == false) goto L_0x00db;
     */
    /* JADX WARNING: Missing block: B:45:0x00bf, code:
            if (isSystemPlayback() == false) goto L_0x00db;
     */
    /* JADX WARNING: Missing block: B:47:0x00c3, code:
            if (DEBUG_PANIC == false) goto L_0x00ce;
     */
    /* JADX WARNING: Missing block: B:48:0x00c5, code:
            android.util.Slog.d(TAG, "keyguardLockedScreenoffTimeout. is playback.");
     */
    /* JADX WARNING: Missing block: B:49:0x00ce, code:
            r10.put("palyback", "true");
     */
    /* JADX WARNING: Missing block: B:50:0x00d7, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:55:0x00e1, code:
            if (r9.mCntNoPlayback.incrementAndGet() >= 3) goto L_0x00e4;
     */
    /* JADX WARNING: Missing block: B:56:0x00e3, code:
            return 3;
     */
    /* JADX WARNING: Missing block: B:57:0x00e4, code:
            r10.put("palyback", "false");
            r4 = r9.mLock;
     */
    /* JADX WARNING: Missing block: B:58:0x00ef, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:61:?, code:
            r9.mUserActivityTimeoutOverrideFromWindowManager = SCREENOFF_TIMEOUT_KEYGUARD_LOCKED;
            r2 = r9.mHandler.obtainMessage(1);
            r2.setAsynchronous(true);
            r9.mHandler.sendMessage(r2);
     */
    /* JADX WARNING: Missing block: B:62:0x0106, code:
            if (DEBUG_PANIC == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:63:0x0108, code:
            android.util.Slog.d(TAG, "keyguardLockedScreenoffTimeout. reset");
     */
    /* JADX WARNING: Missing block: B:64:0x0111, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:65:0x0112, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int keyguardLockedScreenoffTimeout(HashMap<String, String> eventMap) {
        if (!isInteractiveInternal()) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "keyguardLockedScreenoffTimeout, is screen off.");
            }
            eventMap.put("screenOff", "true");
            return 2;
        } else if (this.mPhoneState.get() != 0) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "keyguardLockedScreenoffTimeout. isPhoneInCall.");
            }
            eventMap.put("isPhoneInCall", "true");
            return 2;
        } else {
            eventMap.put("isPhoneInCall", "false");
            List<String> listTopPkg = getAllTopPkgName();
            if (DEBUG_PANIC) {
                Slog.d(TAG, "keyguardLockedScreenoffTimeout: listTopPkg=" + listTopPkg);
            }
            if (listTopPkg == null || !listTopPkg.contains("com.coloros.pictorial")) {
                eventMap.put("pictorial", "false");
                synchronized (this.mLock) {
                    if (this.mIsMmiTesting) {
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, "keyguardLockedScreenoffTimeout, MMI test.");
                        }
                    } else if (isScreenOffTimeoutNeedResetLocked(eventMap)) {
                        boolean hasAudioWL = hasAudioWakelockLocked();
                    } else {
                        return 2;
                    }
                }
            }
            if (DEBUG_PANIC) {
                Slog.d(TAG, "keyguardLockedScreenoffTimeout. pictorial.");
            }
            eventMap.put("pictorial", "true");
            return 2;
        }
    }

    private boolean isScreenOffTimeoutNeedResetLocked(HashMap<String, String> eventMap) {
        int sleepTimeout = getSleepTimeoutLocked();
        int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
        if (DEBUG_PANIC) {
            Slog.d(TAG, "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED. sleepTimeout=" + sleepTimeout + ", screenOffTimeout=" + screenOffTimeout + ", mScreenOffTimeoutSetting=" + this.mScreenOffTimeoutSetting + ", mUserActivityTimeoutOverrideFromWindowManager=" + this.mUserActivityTimeoutOverrideFromWindowManager);
        }
        eventMap.put("activityTimeout", String.valueOf(this.mUserActivityTimeoutOverrideFromWindowManager));
        eventMap.put("screenOffTimeout", String.valueOf(screenOffTimeout));
        eventMap.put("screenOffTimeoutSetting", String.valueOf(this.mScreenOffTimeoutSetting));
        if (screenOffTimeout == this.mScreenOffTimeoutSetting && this.mUserActivityTimeoutOverrideFromWindowManager < 0 && ((long) screenOffTimeout) > SCREENOFF_TIMEOUT_KEYGUARD_LOCKED) {
            return true;
        }
        if (DEBUG_PANIC) {
            Slog.d(TAG, "keyguardLockedScreenoffTimeout. normal.");
        }
        return false;
    }

    private boolean hasAudioWakelockLocked() {
        boolean hasAudio = false;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = 0; i < numWakeLocks; i++) {
            WakeLock wl = (WakeLock) this.mWakeLocks.get(i);
            int directOwnerUid = wl.mOwnerUid;
            String tagName = wl.mTag;
            if ((wl.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1) {
                if (directOwnerUid == 1041 && LIST_TAG_AUDIO_MEDIA_UID.contains(tagName)) {
                    hasAudio = true;
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "hasAudioWakelockLocked: wl=" + wl);
                    }
                    return hasAudio;
                } else if (LIST_TAG_AUDIO_APP.contains(tagName)) {
                    hasAudio = true;
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "hasAudioWakelockLocked: wl=" + wl);
                    }
                    return hasAudio;
                }
            }
        }
        return hasAudio;
    }

    private boolean isSystemPlayback() {
        boolean isPlayback = true;
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_silence"))) {
            isPlayback = false;
        }
        if (DEBUG_PANIC) {
            Slog.d(TAG, "isSystemPlayback: " + isPlayback);
        }
        return isPlayback;
    }

    private void uploadScreenoffTimeoutDcs(HashMap<String, String> eventMap, boolean resetted) {
        eventMap.put("resetTimeout", String.valueOf(resetted));
        eventMap.put("topApp", getTopAppName());
        OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "Screenoff_timeout_reset", eventMap, false);
    }

    private String getTopAppName() {
        ComponentName cn = ((ActivityManager) this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).getTopAppName();
        return cn != null ? cn.getPackageName() : "";
    }

    private List<String> getAllTopPkgName() {
        try {
            return ((ActivityManager) this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).getAllTopPkgName();
        } catch (Exception e) {
            Slog.w(TAG, "getAllTopPkgName exception");
            return null;
        }
    }
}
