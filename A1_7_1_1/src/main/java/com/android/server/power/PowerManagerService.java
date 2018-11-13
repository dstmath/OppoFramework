package com.android.server.power;

import android.annotation.IntDef;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.BroadcastReceiver;
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
import android.hardware.display.WifiDisplayStatus;
import android.hardware.face.FaceInternal;
import android.hardware.fingerprint.FingerprintInternal;
import android.hardware.fingerprint.FingerprintManager.ScreenOnCallback;
import android.net.Uri;
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
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.DisplayPerformanceHelper;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.view.Display;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoGameSpaceManager;
import com.android.server.am.OppoProtectEyeManagerService;
import com.android.server.am.OppoProtectEyeManagerService.ActivityChangedListener;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.oppo.IElsaManager;
import com.android.server.oppo.LMServiceManager;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.vr.VrManagerService;
import com.oppo.FaceHook;
import com.oppo.FingerprintHook;
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
import libcore.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class PowerManagerService extends SystemService implements Monitor {
    private static String BLACKLIGHT_PATH = null;
    private static final int CANCEL_BACKLIGHT_DELAY_TIME = 2000;
    private static boolean DEBUG = false;
    private static boolean DEBUG_PANIC = false;
    private static boolean DEBUG_SPEW = false;
    private static final int DEFAULT_DOUBLE_TAP_TO_WAKE = 0;
    private static final int DEFAULT_FINGERPRINT_VERIFY_TIMEOUT = 3000;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP_TIMEOUT = -1;
    private static final int DIRTY_ACTUAL_DISPLAY_POWER_STATE_UPDATED = 8;
    private static final int DIRTY_BATTERY_STATE = 256;
    private static final int DIRTY_BOOT_COMPLETED = 16;
    private static final int DIRTY_BOOT_IPO = 4096;
    private static final int DIRTY_DOCK_STATE = 1024;
    private static final int DIRTY_IS_POWERED = 64;
    private static final int DIRTY_PROXIMITY_POSITIVE = 512;
    private static final int DIRTY_SCREEN_BRIGHTNESS_BOOST = 2048;
    private static final int DIRTY_SD_STATE = 8192;
    private static final int DIRTY_SETTINGS = 32;
    private static final int DIRTY_STAY_ON = 128;
    private static final int DIRTY_USER_ACTIVITY = 4;
    private static final int DIRTY_WAKEFULNESS = 2;
    private static final int DIRTY_WAKE_LOCKS = 1;
    private static final int HALT_MODE_REBOOT = 1;
    private static final int HALT_MODE_REBOOT_SAFE_MODE = 2;
    private static final int HALT_MODE_SHUTDOWN = 0;
    public static final String IPO_BOOT = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_PREBOOT = "android.intent.action.ACTION_PREBOOT_IPO";
    private static final float MAXIMUM_SCREEN_BUTTON_RATIO = 0.3f;
    static final long MIN_LONG_WAKE_CHECK_INTERVAL = 60000;
    private static final int MSG_CHECK_FOR_LONG_WAKELOCKS = 4;
    private static final int MSG_FINGERPRINT_SET_ALPHA_TIMEOUT = 6;
    private static final int MSG_FINGERPRINT_VERIFY_TIMEOUT = 7;
    private static final int MSG_OPPO_PHONE_HEADSET_HANGUP = 5;
    private static final int MSG_SANDMAN = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 3;
    private static final int MSG_USER_ACTIVITY_TIMEOUT = 1;
    private static final int NOTIF_SRC_LOW_POWER_MODE = 3;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_OFF = 21;
    private static final int NOTIF_TYPE_LOW_POWER_MODE_ON = 20;
    private static final int POWER_FEATURE_DOUBLE_TAP_TO_WAKE = 1;
    private static final int POWER_HINT_LOW_POWER = 5;
    private static final int POWER_HINT_VR_MODE = 7;
    private static final int SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 5000;
    private static final int SCREEN_BUTTON_LIGHT_DURATION = 8000;
    private static final String TAG = "PowerManagerService";
    private static final int USER_ACTIVITY_BUTTON_BRIGHT = 8;
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
    public static boolean mOppoShutdownIng;
    private static int mScreenBrightnessSettingMaximum;
    private static int mScreenBrightnessSettingMinimum;
    private ActivityChangedListener mActivityChangedListener;
    private IAppOpsService mAppOps;
    private Light mAttentionLight;
    private boolean mAutoLowPowerModeConfigured;
    private boolean mAutoLowPowerModeSnoozing;
    private Light mBacklight;
    private int mBatteryLevel;
    private boolean mBatteryLevelLow;
    private int mBatteryLevelWhenDreamStarted;
    private BatteryManagerInternal mBatteryManagerInternal;
    private IBatteryStats mBatteryStats;
    private boolean mBootCompleted;
    private Runnable[] mBootCompletedRunnables;
    private boolean mBrightnessUseTwilight;
    private Light mButtonLight;
    private final Runnable mCancelBacklightLimit;
    private AtomicBoolean mColorOsLowPowerModeEnabled;
    private final ArrayList<LowPowerModeListener> mColorOsLowPowerModeListeners;
    private final Context mContext;
    private boolean mDecoupleHalAutoSuspendModeFromDisplayConfig;
    private boolean mDecoupleHalInteractiveModeFromDisplayConfig;
    private boolean mDeviceIdleMode;
    int[] mDeviceIdleTempWhitelist;
    int[] mDeviceIdleWhitelist;
    private int mDirty;
    private DisplayManagerInternal mDisplayManagerInternal;
    private DisplayPerformanceHelper mDisplayPerformanceHelper;
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
    private FaceHook mFaceHook;
    private FaceInternal mFaceInternal;
    private FingerprintHook mFingerprintHook;
    private FingerprintInternal mFingerprintInternal;
    private FingerprintScreenOnCallBack mFingerprintScreenOnCallBack;
    private boolean mHalAutoSuspendModeEnabled;
    private boolean mHalInteractiveModeEnabled;
    private final PowerManagerHandler mHandler;
    private final ServiceThread mHandlerThread;
    private boolean mHoldingDisplaySuspendBlocker;
    private boolean mHoldingWakeLockSuspendBlocker;
    private Hypnus mHyp;
    private AtomicBoolean mHypnusLowPowerenabled;
    private boolean mIPOShutdown;
    private boolean mIsPowered;
    private LMServiceManager mLMServiceManager;
    private long mLastInteractivePowerHintTime;
    private long mLastScreenBrightnessBoostTime;
    private long mLastSleepTime;
    private long mLastUserActivityButtonTime;
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
    private int mPlugType;
    private WindowManagerPolicy mPolicy;
    private PowerMonitor mPowerMonitor;
    private boolean mPreWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mProximityLockFromInCallUi;
    private boolean mProximityPositive;
    private boolean mRequestWaitForNegativeProximity;
    private boolean mSandmanScheduled;
    private boolean mSandmanSummoned;
    private float mScreenAutoBrightnessAdjustmentSetting;
    private boolean mScreenBrightnessBoostInProgress;
    private int mScreenBrightnessModeSetting;
    private int mScreenBrightnessOverrideFromWindowManager;
    private int mScreenBrightnessSetting;
    private int mScreenBrightnessSettingDefault;
    private int mScreenOffReason;
    private int mScreenOffTimeoutSetting;
    private ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    private SettingsObserver mSettingsObserver;
    private boolean mShutdownFlag;
    private int mSleepTimeoutSetting;
    private boolean mStartGoToSleep;
    private boolean mStayOn;
    private int mStayOnWhilePluggedInSetting;
    private boolean mStayOnWithoutDim;
    private boolean mSupportsDoubleTapWakeConfig;
    private final ArrayList<SuspendBlocker> mSuspendBlockers;
    private boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mSystemReady;
    private float mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
    private int mTemporaryScreenBrightnessSettingOverride;
    private boolean mTheaterModeEnabled;
    private final SparseIntArray mUidState;
    private int mUserActivitySummary;
    private boolean mUserActivityTimeoutMin;
    private int mUserActivityTimeoutOverrideFromCMD;
    private long mUserActivityTimeoutOverrideFromWindowManager;
    private boolean mUserInactiveOverrideFromWindowManager;
    private final IVrStateCallbacks mVrStateCallbacks;
    private OppoWakeLockCheck mWakeLockCheck;
    private int mWakeLockSummary;
    private final SuspendBlocker mWakeLockSuspendBlocker;
    private final ArrayList<WakeLock> mWakeLocks;
    private boolean mWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig;
    private int mWakefulness;
    private boolean mWakefulnessChanging;
    private boolean mWfdEnabled;
    private boolean mWfdShouldBypass;
    private WirelessChargerDetector mWirelessChargerDetector;
    private boolean useProximityForceSuspend;

    private final class BatteryReceiver extends BroadcastReceiver {
        /* synthetic */ BatteryReceiver(PowerManagerService this$0, BatteryReceiver batteryReceiver) {
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

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(PowerManagerService this$0, BinderService binderService) {
            this();
        }

        private BinderService() {
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
                boolean -wrap7 = PowerManagerService.this.isWakeLockLevelSupportedInternal(level);
                return -wrap7;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void userActivity(long eventTime, int event, int flags) {
            long now = SystemClock.uptimeMillis();
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 && PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.USER_ACTIVITY") != 0) {
                synchronized (PowerManagerService.this.mLock) {
                    if (now >= PowerManagerService.this.mLastWarningAboutUserActivityPermission + 300000) {
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
                boolean -wrap4 = PowerManagerService.this.isInteractiveInternal();
                return -wrap4;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap5 = PowerManagerService.this.isLowPowerModeInternal();
                return -wrap5;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isColorOsPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap3 = PowerManagerService.this.isColorOsLowPowerModeInternal();
                return -wrap3;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getScreenState() {
            long ident = Binder.clearCallingIdentity();
            try {
                int -wrap13 = PowerManagerService.this.getScreenStateInternal();
                return -wrap13;
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
                int -get26 = PowerManagerService.mScreenBrightnessSettingMaximum;
                return -get26;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getDefaultScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                int -get25 = PowerManagerService.this.mScreenBrightnessSettingDefault;
                return -get25;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPowerSaveMode(boolean mode) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap9 = PowerManagerService.this.setLowPowerModeInternal(mode);
                return -wrap9;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setColorOsPowerSaveMode(boolean mode) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap8 = PowerManagerService.this.setColorOsLowPowerModeInternal(mode);
                return -wrap8;
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
                boolean -wrap6 = PowerManagerService.this.isScreenBrightnessBoostedInternal();
                return -wrap6;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump PowerManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (!PowerManagerService.this.dynamicallyConfigPowerManagerServiceLogTag(fd, pw, args) && !PowerManagerService.this.dumpPossibleMusicPlayer(fd, pw, args)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.dumpInternal(pw);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void startBacklight(int delay_msec) {
            synchronized (PowerManagerService.this.mLock) {
                if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                    Slog.d(PowerManagerService.TAG, "startBacklight");
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    long ident = Binder.clearCallingIdentity();
                    try {
                        PowerManagerService.this.mDisplayManagerInternal.setIPOScreenOnDelay(delay_msec);
                        PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                        PowerManagerService.this.updatePowerStateLocked();
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.d(PowerManagerService.TAG, "skip startBacklight because MTK_IPO_SUPPORT not enabled");
                }
            }
        }

        public void stopBacklight() {
            synchronized (PowerManagerService.this.mLock) {
                if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                    Slog.d(PowerManagerService.TAG, "stopBacklight");
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    long ident = Binder.clearCallingIdentity();
                    try {
                        PowerManagerService.this.mDisplayManagerInternal.setIPOScreenOnDelay(0);
                        PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                        PowerManagerService.this.updatePowerStateLocked();
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.d(PowerManagerService.TAG, "skip stopBacklight because MTK_IPO_SUPPORT not enabled");
                }
            }
        }

        public void setBacklightOffForWfd(boolean enable) {
            if (enable) {
                Slog.d(PowerManagerService.TAG, "setBacklightOffForWfd true");
                PowerManagerService.this.mBacklight.setBrightness(0);
            } else if (PowerManagerService.this.mWfdShouldBypass) {
                Slog.d(PowerManagerService.TAG, "setBacklightOffForWfd false ignored due to screen is off by power key");
            } else {
                Slog.d(PowerManagerService.TAG, "setBacklightOffForWfd false");
                PowerManagerService.this.mBacklight.setBrightness(PowerManagerService.this.mScreenBrightnessSetting);
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
            if (uid == 1000 || uid == 0) {
                try {
                    PowerManagerService.this.updateChargeStateForSaleInternal(false);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new SecurityException("No permission to stopChargeForSale");
            }
        }

        public void resumeChargeForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            if (uid == 1000 || uid == 0) {
                try {
                    PowerManagerService.this.updateChargeStateForSaleInternal(true);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new SecurityException("No permission to resumeChargeForSale");
            }
        }

        public int getCurrentChargeStateForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int -wrap12 = PowerManagerService.this.getCurrentChargeStateForSaleInternal();
                return -wrap12;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final class DockReceiver extends BroadcastReceiver {
        /* synthetic */ DockReceiver(PowerManagerService this$0, DockReceiver dockReceiver) {
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
                    powerManagerService.mDirty = powerManagerService.mDirty | 1024;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }
        }
    }

    private final class DreamReceiver extends BroadcastReceiver {
        /* synthetic */ DreamReceiver(PowerManagerService this$0, DreamReceiver dreamReceiver) {
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

    private class FingerprintScreenOnCallBack implements ScreenOnCallback {
        /* synthetic */ FingerprintScreenOnCallBack(PowerManagerService this$0, FingerprintScreenOnCallBack fingerprintScreenOnCallBack) {
            this();
        }

        private FingerprintScreenOnCallBack() {
        }

        public void onVerifyDone(int result) {
            Slog.i("ray", "FingerprintScreenOnCallBack : onVerifyDone = " + result);
            if (PowerManagerService.this.mFingerprintHook != null) {
                PowerManagerService.this.mFingerprintHook.onVerifyDone(result);
            }
            PowerManagerService.this.mFingerprintScreenOnCallBack = null;
            switch (result) {
                case -1:
                case 1:
                    PowerManagerService.this.mStartGoToSleep = false;
                    if (PowerManagerService.this.mHandler.hasMessages(3)) {
                        PowerManagerService.this.mHandler.removeMessages(3);
                    }
                    if (PowerManagerService.this.mDisplayManagerInternal != null) {
                        PowerManagerService.this.mDisplayManagerInternal.unblockScreenOnByFingerPrint(true);
                    }
                    PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                    break;
                case 0:
                    if (PowerManagerService.this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
                        if (!PowerManagerService.this.mHandler.hasMessages(6)) {
                            PowerManagerService.this.goToSleepInternal(SystemClock.uptimeMillis(), 99, 1, 1000);
                            break;
                        } else {
                            Slog.d(PowerManagerService.TAG, "Screen turning on for fingerprint, ignore verify failed");
                            return;
                        }
                    }
                    return;
            }
        }
    }

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HaltMode {
    }

    private final class LocalService extends PowerManagerInternal {
        /* synthetic */ LocalService(PowerManagerService this$0, LocalService localService) {
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

        public void setButtonBrightnessOverrideFromWindowManager(int screenBrightness) {
        }

        public void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
            switch (screenState) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
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

        public boolean getLowPowerModeEnabled() {
            boolean -get19;
            synchronized (PowerManagerService.this.mLock) {
                -get19 = PowerManagerService.this.mLowPowerModeEnabled;
            }
            return -get19;
        }

        public void registerLowPowerModeObserver(LowPowerModeListener listener) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mLowPowerModeListeners.add(listener);
            }
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

        public void updateUidProcState(int uid, int procState) {
            PowerManagerService.this.updateUidProcStateInternal(uid, procState);
        }

        public void uidGone(int uid) {
            PowerManagerService.this.uidGoneInternal(uid);
        }

        public void powerHint(int hintId, int data) {
            PowerManagerService.this.powerHintInternal(hintId, data);
        }

        public boolean isStartGoToSleep() {
            return PowerManagerService.this.mStartGoToSleep;
        }
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
            if (!PowerManagerService.mOppoShutdownIng && !PowerManagerService.this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
                int tmp = getUserActivitySumm();
                boolean b1 = (tmp & 2) != 0;
                boolean b2 = tmp == 0;
                boolean b = getWakefulness() == 1;
                if (!condition) {
                    this.mButtonLight.setBrightness(102);
                } else if (!(!b || b1 || b2)) {
                    this.mButtonLight.setBrightness(102);
                }
            }
        }

        void turnOffButtonLight() {
            this.mButtonLight.turnOff();
        }
    }

    private final class OppoPhoneHeadsetReceiver extends BroadcastReceiver {
        /* synthetic */ OppoPhoneHeadsetReceiver(PowerManagerService this$0, OppoPhoneHeadsetReceiver oppoPhoneHeadsetReceiver) {
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
        /* synthetic */ OppoShutDownReceiver(PowerManagerService this$0, OppoShutDownReceiver oppoShutDownReceiver) {
            this();
        }

        private OppoShutDownReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (PowerManagerService.DEBUG_PANIC) {
                Slog.d(PowerManagerService.TAG, "PowerMS NotSleepingWhenShutdowning: received a shutdown broadcast");
            }
            PowerManagerService.mOppoShutdownIng = true;
            PowerManagerService.this.mOppoHelper.turnOffButtonLight();
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
                    Slog.d(PowerManagerService.TAG, "PowerMS OppoPhoneHeadsetReceiver: HANDLE MES DONE");
                    return;
                case 6:
                    if (PowerManagerService.this.mDisplayManagerInternal != null) {
                        Slog.d(PowerManagerService.TAG, "screenOnUnBlockedByOther, alpha has been changed");
                        PowerManagerService.this.mDisplayManagerInternal.unblockScreenOnByFingerPrint(true);
                    }
                    PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                    android.os.PowerManager.WakeLock partial = msg.obj;
                    if (partial != null) {
                        partial.release();
                        return;
                    }
                    return;
                case 7:
                    if (PowerManagerService.this.mFingerprintScreenOnCallBack != null) {
                        PowerManagerService.this.mFingerprintScreenOnCallBack.onVerifyDone(-1);
                        return;
                    }
                    return;
                default:
                    return;
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
                if (!((wakeLock.mFlags & 1) == 0 || wakeLock.mDisabled)) {
                    releaseWakeLock(wakeLock.mPackageName, wakeLock.mTag, now - wakeLock.mActiveSince);
                }
            }
        }

        public void acquireSuspendBlocker(String name) {
            if (name.equals(CPUBLOCKER) && PowerManagerService.this.mWakefulness == 0 && this.mLastBlockedTime == -1) {
                this.mLastBlockedTime = SystemClock.uptimeMillis();
            }
        }

        public void releaseSuspendBlocker(String name) {
            if (name.equals(CPUBLOCKER) && PowerManagerService.this.mWakefulness == 0) {
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
            if (PowerManagerService.this.mWakefulness == 0) {
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
                    Trace.asyncTraceEnd(524288, this.mTraceName, 0);
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
                    Trace.asyncTraceBegin(524288, this.mTraceName, 0);
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
                    Trace.asyncTraceEnd(524288, this.mTraceName, 0);
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
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        /* synthetic */ UserSwitchedReceiver(PowerManagerService this$0, UserSwitchedReceiver userSwitchedReceiver) {
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
        public long mColorAcquireTime = -1;
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
        public WorkSource mWorkSource;

        public WakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            this.mLock = lock;
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
            this.mHistoryTag = historyTag;
            this.mOwnerUid = ownerUid;
            this.mOwnerPid = ownerPid;
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

        public String getLockLevelString() {
            switch (this.mFlags & 65535) {
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
            String result = IElsaManager.EMPTY_PACKAGE;
            if ((this.mFlags & 268435456) != 0) {
                result = result + " ACQUIRE_CAUSES_WAKEUP";
            }
            if ((this.mFlags & 536870912) != 0) {
                return result + " ON_AFTER_RELEASE";
            }
            return result;
        }
    }

    private final class WifiDisplayStatusChangedReceiver extends BroadcastReceiver {
        /* synthetic */ WifiDisplayStatusChangedReceiver(PowerManagerService this$0, WifiDisplayStatusChangedReceiver wifiDisplayStatusChangedReceiver) {
            this();
        }

        private WifiDisplayStatusChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                if (intent.getAction().equals("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED")) {
                    PowerManagerService.this.mWfdEnabled = 2 == ((WifiDisplayStatus) intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS")).getActiveDisplayState();
                    Slog.d(PowerManagerService.TAG, "<<<<< WifiDisplayStatusChangedReceiver >>>>> mWfdEnabled = " + PowerManagerService.this.mWfdEnabled);
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.power.PowerManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.power.PowerManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.PowerManagerService.<clinit>():void");
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
        this.mLock = new Object();
        this.mSuspendBlockers = new ArrayList();
        this.mWakeLocks = new ArrayList();
        this.mDisplayPowerRequest = new DisplayPowerRequest();
        this.mDockState = 0;
        this.mWfdShouldBypass = false;
        this.mWfdEnabled = false;
        this.mMaximumScreenOffTimeoutFromDeviceAdmin = Integer.MAX_VALUE;
        this.mStayOnWithoutDim = false;
        this.mScreenBrightnessOverrideFromWindowManager = -1;
        this.mOverriddenTimeout = -1;
        this.mUserActivityTimeoutOverrideFromWindowManager = -1;
        this.mTemporaryScreenBrightnessSettingOverride = -1;
        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        this.mDozeScreenStateOverrideFromDreamManager = 0;
        this.mDozeScreenBrightnessOverrideFromDreamManager = -1;
        this.mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
        this.mIPOShutdown = false;
        this.mShutdownFlag = false;
        this.mUserActivityTimeoutMin = false;
        this.mUserActivityTimeoutOverrideFromCMD = 10;
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mDeviceIdleWhitelist = new int[0];
        this.mDeviceIdleTempWhitelist = new int[0];
        this.mUidState = new SparseIntArray();
        this.mLowPowerModeListeners = new ArrayList();
        this.mColorOsLowPowerModeListeners = new ArrayList();
        this.mOppoButtonReady = true;
        this.mScreenOffReason = 0;
        this.useProximityForceSuspend = false;
        this.mProximityLockFromInCallUi = false;
        this.mLMServiceManager = null;
        this.mScreenOnCpuBoostHelper = null;
        this.mFingerprintScreenOnCallBack = null;
        this.mStartGoToSleep = false;
        this.mDisplayPerformanceHelper = null;
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
                    powerManagerService.mDirty = powerManagerService.mDirty | 8;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositive() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositive");
                    PowerManagerService.this.mProximityPositive = true;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty = powerManagerService.mDirty | 512;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityNegative() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityNegative");
                    PowerManagerService.this.mProximityPositive = false;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty = powerManagerService.mDirty | 512;
                    PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositiveForceSuspend() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositiveForceSuspend");
                    PowerManagerService.this.mProximityPositive = true;
                    if (PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 8, 0, 1000)) {
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

            public void onFingerprintVerifyDone(int result) {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onFingerprintVerifyDone result : " + result);
                    if (result == 1) {
                        PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                    } else {
                        if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                            PowerManagerService.this.setHalInteractiveModeLocked(false);
                        }
                        if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                            PowerManagerService.this.setHalAutoSuspendModeLocked(true);
                        }
                    }
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
                    if (OppoGameSpaceManager.getInstance().inGameSpacePkgList(nextPkg)) {
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
            }
        };
        this.mWakeLockCheck = null;
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.init(this.mContext);
        mOppoBrightUtils.getScreenAutoBrightnessConfig();
        mScreenBrightnessSettingMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        mScreenBrightnessSettingMaximum = mOppoBrightUtils.getMaximumScreenBrightnessSetting();
        this.mScreenBrightnessSettingDefault = mOppoBrightUtils.getDefaultScreenBrightnessSetting();
        this.mPowerMonitor = new PowerMonitor();
        synchronized (this.mLock) {
            this.mWakeLockSuspendBlocker = createSuspendBlockerLocked("PowerManagerService.WakeLocks");
            this.mDisplaySuspendBlocker = createSuspendBlockerLocked("PowerManagerService.Display");
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
            this.mHalAutoSuspendModeEnabled = false;
            this.mHalInteractiveModeEnabled = true;
            this.mWakefulness = 1;
            nativeInit();
            nativeSetAutoSuspend(false);
            nativeSetInteractive(true);
            nativeSetFeature(1, 0);
        }
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
                OppoProtectEyeManagerService.setActivityChangedListener(this.mActivityChangedListener);
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
            this.mDreamManager = (DreamManagerInternal) -wrap1(DreamManagerInternal.class);
            this.mDisplayManagerInternal = (DisplayManagerInternal) -wrap1(DisplayManagerInternal.class);
            this.mPolicy = (WindowManagerPolicy) -wrap1(WindowManagerPolicy.class);
            this.mBatteryManagerInternal = (BatteryManagerInternal) -wrap1(BatteryManagerInternal.class);
            SystemProperties.set("sys.oppo.multibrightness", Integer.toString(mScreenBrightnessSettingMaximum));
            mScreenBrightnessSettingMaximum = ((PowerManager) this.mContext.getSystemService("power")).getMaxBrightness();
            if (DEBUG_SPEW) {
                Slog.d(TAG, "mScreenBrightnessSettingMinimum = " + mScreenBrightnessSettingMinimum + " mScreenBrightnessSettingMinimum = " + mScreenBrightnessSettingMaximum + " mScreenBrightnessSettingDefault = " + this.mScreenBrightnessSettingDefault);
            }
            SensorManager sensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
            this.mBatteryStats = BatteryStatsService.getService();
            this.mNotifier = new Notifier(Looper.getMainLooper(), this.mContext, this.mBatteryStats, this.mAppOps, createSuspendBlockerLocked("PowerManagerService.Broadcasts"), this.mPolicy);
            this.mWirelessChargerDetector = new WirelessChargerDetector(sensorManager, createSuspendBlockerLocked("PowerManagerService.WirelessChargerDetector"), this.mHandler);
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            this.mLightsManager = (LightsManager) -wrap1(LightsManager.class);
            this.mAttentionLight = this.mLightsManager.getLight(5);
            this.mButtonLight = this.mLightsManager.getLight(2);
            this.mOppoHelper = new OppoHelper(this.mLightsManager);
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.guard.elf.support")) {
                this.mWakeLockCheck = new OppoWakeLockCheck(this.mWakeLocks, this.mLock, this.mContext, this, createSuspendBlockerLocked("WakeLockCheck"));
            }
            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            this.mDisplayManagerInternal.initPowerManagement(this.mDisplayPowerCallbacks, this.mHandler, sensorManager);
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(Secure.getUriFor("screensaver_enabled"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_sleep"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_dock"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Secure.getUriFor("sleep_timeout"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Global.getUriFor("low_power"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Global.getUriFor("low_power_trigger_level"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Global.getUriFor("theater_mode_on"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Secure.getUriFor("double_tap_to_wake"), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(Secure.getUriFor("brightness_use_twilight"), false, this.mSettingsObserver, -1);
            try {
                ((IVrManager) getBinderService(VrManagerService.VR_MANAGER_BINDER_SERVICE)).registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e);
            }
            this.mLMServiceManager = new LMServiceManager(this.mContext, this.mHandler);
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
            this.mFingerprintHook = FingerprintHook.getInstance(this.mFingerprintInternal);
            this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
            this.mFaceInternal = (FaceInternal) LocalServices.getService(FaceInternal.class);
            this.mFaceHook = FaceHook.getInstance(this.mFaceInternal);
            if (DEBUG_PANIC) {
                Slog.d(TAG, "system ready!");
            }
            readConfigurationLocked();
            updateSettingsLocked();
            this.mDirty |= 256;
            updatePowerStateLocked();
        }
        if (this.mLMServiceManager != null) {
            this.mLMServiceManager.systemReady();
        }
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
        if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_BOOT_IPO");
            filter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    synchronized (PowerManagerService.this.mLock) {
                        PowerManagerService powerManagerService;
                        if ("android.intent.action.ACTION_PREBOOT_IPO".equals(intent.getAction())) {
                            Slog.d(PowerManagerService.TAG, "PREBOOT_IPO");
                            PowerManagerService.this.mStayOnWithoutDim = true;
                            PowerManagerService.this.mIPOShutdown = false;
                            powerManagerService = PowerManagerService.this;
                            powerManagerService.mDirty = powerManagerService.mDirty | 4096;
                        } else if ("android.intent.action.ACTION_BOOT_IPO".equals(intent.getAction())) {
                            Slog.d(PowerManagerService.TAG, "IPO_BOOT");
                            PowerManagerService.this.mStayOnWithoutDim = false;
                            PowerManagerService.this.mIPOShutdown = false;
                            powerManagerService = PowerManagerService.this;
                            powerManagerService.mDirty = powerManagerService.mDirty | 4096;
                            PowerManagerService.this.mDisplayManagerInternal.setIPOScreenOnDelay(0);
                            if (PowerManagerService.this.mWakefulness != 1) {
                                PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                            } else {
                                PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                            }
                        }
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }, filter, null, this.mHandler);
            filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (PowerManagerService.DEBUG) {
                        Slog.d(PowerManagerService.TAG, "ACTION_SHUTDOWN_IPO.");
                    }
                    PowerManagerService.this.mIPOShutdown = true;
                }
            }, filter, null, this.mHandler);
            filter = new IntentFilter();
            filter.addAction("android.intent.action.normal.shutdown");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!PowerManagerService.this.mBootCompleted) {
                        Slog.d(PowerManagerService.TAG, "set mBootCompleted as true");
                        PowerManagerService.this.mBootCompleted = true;
                    }
                }
            }, filter, null, this.mHandler);
        }
        this.mContext.registerReceiver(new WifiDisplayStatusChangedReceiver(this, null), new IntentFilter("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED"), null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("com.mediatek.SCREEN_TIMEOUT_MINIMUM");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (PowerManagerService.DEBUG) {
                    Slog.d(PowerManagerService.TAG, "SCREEN_TIMEOUT_MINIMUM.");
                }
                PowerManagerService.this.mPreWakeUpWhenPluggedOrUnpluggedConfig = PowerManagerService.this.mWakeUpWhenPluggedOrUnpluggedConfig;
                PowerManagerService.this.mWakeUpWhenPluggedOrUnpluggedConfig = false;
                PowerManagerService.this.mUserActivityTimeoutMin = true;
            }
        }, filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("com.mediatek.SCREEN_TIMEOUT_NORMAL");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (PowerManagerService.DEBUG) {
                    Slog.d(PowerManagerService.TAG, "SCREEN_TIMEOUT_NORMAL.");
                }
                PowerManagerService.this.mWakeUpWhenPluggedOrUnpluggedConfig = PowerManagerService.this.mPreWakeUpWhenPluggedOrUnpluggedConfig;
                PowerManagerService.this.mUserActivityTimeoutMin = false;
            }
        }, filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(new OppoShutDownReceiver(this, null), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("oppo.action.phone.headset.hangup");
        this.mContext.registerReceiver(new OppoPhoneHeadsetReceiver(this, null), filter, null, this.mHandler);
        this.mDisplayPerformanceHelper = new DisplayPerformanceHelper(this.mContext);
        this.mDisplayPerformanceHelper.initUpdateBroadcastReceiver();
        return;
    }

    private void readConfigurationLocked() {
        Resources resources = this.mContext.getResources();
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = resources.getBoolean(17956978);
        this.mDecoupleHalInteractiveModeFromDisplayConfig = resources.getBoolean(17956979);
        this.mWakeUpWhenPluggedOrUnpluggedConfig = resources.getBoolean(17956899);
        this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig = resources.getBoolean(17956904);
        this.mSuspendWhenScreenOffDueToProximityConfig = resources.getBoolean(17956927);
        this.mDreamsSupportedConfig = resources.getBoolean(17956971);
        this.mDreamsEnabledByDefaultConfig = resources.getBoolean(17956972);
        this.mDreamsActivatedOnSleepByDefaultConfig = resources.getBoolean(17956974);
        this.mDreamsActivatedOnDockByDefaultConfig = resources.getBoolean(17956973);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
            Slog.d(TAG, "has feature exp.sellmode");
            this.mDreamsEnabledOnBatteryConfig = true;
            if (DEBUG_SPEW) {
                Slog.d(TAG, "mDreamsEnabledOnBatteryConfig =" + this.mDreamsEnabledOnBatteryConfig + " mDreamsBatteryLevelMinimumWhenNotPoweredConfig =" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            }
        } else {
            this.mDreamsEnabledOnBatteryConfig = resources.getBoolean(17956975);
        }
        this.mDreamsBatteryLevelMinimumWhenPoweredConfig = resources.getInteger(17694854);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
            Slog.d(TAG, "has feature exp.sellmode");
            this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = 20;
            if (DEBUG_SPEW) {
                Slog.d(TAG, "mDreamsEnabledOnBatteryConfig =" + this.mDreamsEnabledOnBatteryConfig + " mDreamsBatteryLevelMinimumWhenNotPoweredConfig =" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            }
        } else {
            this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = resources.getInteger(17694855);
        }
        this.mDreamsBatteryLevelDrainCutoffConfig = resources.getInteger(17694856);
        this.mDozeAfterScreenOffConfig = resources.getBoolean(17956976);
        this.mMinimumScreenOffTimeoutConfig = resources.getInteger(17694857);
        this.mMaximumScreenDimDurationConfig = resources.getInteger(17694858);
        this.mMaximumScreenDimRatioConfig = resources.getFraction(18022403, 1, 1);
        this.mSupportsDoubleTapWakeConfig = resources.getBoolean(17957028);
    }

    private void updateSettingsLocked() {
        int i;
        boolean z;
        boolean z2 = true;
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
        int oldScreenBrightnessSetting = this.mScreenBrightnessSetting;
        this.mScreenBrightnessSetting = System.getIntForBrightness(resolver, "screen_brightness", this.mScreenBrightnessSettingDefault, -2);
        if (oldScreenBrightnessSetting != this.mScreenBrightnessSetting) {
            this.mTemporaryScreenBrightnessSettingOverride = -1;
        }
        float oldScreenAutoBrightnessAdjustmentSetting = this.mScreenAutoBrightnessAdjustmentSetting;
        this.mScreenAutoBrightnessAdjustmentSetting = System.getFloatForUser(resolver, "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
        if (oldScreenAutoBrightnessAdjustmentSetting != this.mScreenAutoBrightnessAdjustmentSetting) {
            this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        }
        this.mScreenBrightnessModeSetting = System.getIntForUser(resolver, "screen_brightness_mode", 0, -2);
        if (DEBUG_SPEW) {
            Slog.d(TAG, "updateSettingsLocked: mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
        }
        if (Secure.getIntForUser(resolver, "brightness_use_twilight", 0, -2) == 0) {
            z2 = false;
        }
        this.mBrightnessUseTwilight = z2;
        boolean lowPowerModeEnabled = Global.getInt(resolver, "low_power", 0) != 0;
        boolean autoLowPowerModeConfigured = Global.getInt(resolver, "low_power_trigger_level", 0) != 0;
        if (!(lowPowerModeEnabled == this.mLowPowerModeSetting && autoLowPowerModeConfigured == this.mAutoLowPowerModeConfigured)) {
            this.mLowPowerModeSetting = lowPowerModeEnabled;
            this.mAutoLowPowerModeConfigured = autoLowPowerModeConfigured;
            updateLowPowerModeLocked();
        }
        this.mDirty |= 32;
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
        if ((this.mIsPowered || !(this.mBatteryLevelLow || this.mBootCompleted)) && this.mLowPowerModeSetting) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateLowPowerModeLocked: powered or booting with sufficient battery, turning setting off");
            }
            Global.putInt(this.mContext.getContentResolver(), "low_power", 0);
            this.mLowPowerModeSetting = false;
        }
        if (this.mIsPowered || !this.mAutoLowPowerModeConfigured || this.mAutoLowPowerModeSnoozing) {
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
                        ((LowPowerModeListener) listeners.get(i)).onLowPowerModeChanged(lowPowerModeEnabled);
                    }
                    Intent intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED");
                    intent.addFlags(1073741824);
                    PowerManagerService.this.mContext.sendBroadcast(intent);
                    PowerManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL"), UserHandle.ALL, "android.permission.DEVICE_POWER");
                }
            });
        }
    }

    private void handleSettingsChangedLocked() {
        updateSettingsLocked();
        updatePowerStateLocked();
    }

    /* JADX WARNING: Missing block: B:23:0x00f2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        synchronized (this.mLock) {
            if (this.mWakeLockCheck == null || this.mWakeLockCheck.canSyncWakeLockAcq(uid, tag)) {
                WakeLock wakeLock;
                boolean notifyAcquire;
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
                    wakeLock = new WakeLock(lock, flags, tag, packageName, ws, historyTag, uid, pid);
                    try {
                        lock.linkToDeath(wakeLock, 0);
                        wakeLock.mColorAcquireTime = SystemClock.uptimeMillis();
                        this.mWakeLocks.add(wakeLock);
                        setWakeLockDisabledStateLocked(wakeLock);
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
    }

    private static boolean isScreenLock(WakeLock wakeLock) {
        switch (wakeLock.mFlags & 65535) {
            case 6:
            case 10:
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return true;
            default:
                return false;
        }
    }

    private void applyWakeLockFlagsOnAcquireLocked(WakeLock wakeLock, int uid) {
        if (!(this.mIPOShutdown || (wakeLock.mFlags & 268435456) == 0 || !isScreenLock(wakeLock))) {
            if (this.useProximityForceSuspend && this.mProximityPositive) {
                if (DEBUG_SPEW) {
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

    /* JADX WARNING: Missing block: B:9:0x003b, code:
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
                    Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], flags=0x" + Integer.toHexString(flags) + ", total_time=" + wakeLock.mTotalTime + "ms");
                }
                this.mPowerMonitor.releaseWakeLock(wakeLock.mPackageName, wakeLock.mTag, wakeLock.mTotalTime);
                if ((flags & 1) != 0) {
                    this.mRequestWaitForNegativeProximity = true;
                }
                wakeLock.mLock.unlinkToDeath(wakeLock, 0);
                removeWakeLockLocked(wakeLock, index);
            } else if (DEBUG_PANIC) {
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

    private void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
            wakeLock.mNotifiedAcquired = true;
            this.mNotifier.onWakeLockAcquired(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.noteWakeLockChange(wakeLock, true);
            }
            restartNofifyLongTimerLocked(wakeLock);
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
        if ((wakeLock.mFlags & 65535) == 1 && this.mNotifyLongScheduled == 0) {
            enqueueNotifyLongMsgLocked(wakeLock.mAcquireTime + 60000);
        }
    }

    private void notifyWakeLockLongStartedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
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

    private void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
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
                String type = IElsaManager.EMPTY_PACKAGE;
                switch (wakeLock.mFlags & 65535) {
                    case 1:
                        type = "PARTIAL_WAKE_LOCK";
                        break;
                    case 6:
                        type = "SCREEN_DIM_WAKE_LOCK";
                        break;
                    case 10:
                        type = "SCREEN_BRIGHT_WAKE_LOCK";
                        break;
                    case H.DO_ANIMATION_CALLBACK /*26*/:
                        type = "FULL_WAKE_LOCK";
                        break;
                    case 32:
                        type = "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
                        break;
                    case 64:
                        type = "DOZE_WAKE_LOCK";
                        break;
                    default:
                        break;
                }
                Slog.d(TAG, "No." + i + ": " + type + " '" + wakeLock.mTag + "'" + "activated" + "(flags=" + wakeLock.mFlags + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid + ")" + " total=" + (SystemClock.uptimeMillis() - wakeLock.mActiveSince) + "ms)");
            }
        }
    }

    private boolean canBrightnessOverRange() {
        if (this.mWfdEnabled) {
            return true;
        }
        return false;
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
        if (DEBUG_SPEW) {
            Slog.d(TAG, "userActivityFromNative");
        }
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
        if (eventTime < this.mLastSleepTime || eventTime < this.mLastWakeTime || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(524288, "userActivity");
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
                Trace.traceEnd(524288);
                return false;
            }
            if ((flags & 1) != 0) {
                if (eventTime > this.mLastUserActivityTimeNoChangeLights && eventTime > this.mLastUserActivityTime) {
                    this.mLastUserActivityTimeNoChangeLights = eventTime;
                    this.mDirty |= 4;
                    Trace.traceEnd(524288);
                    return true;
                }
            } else if (eventTime > this.mLastUserActivityTime) {
                this.mLastUserActivityTime = eventTime;
                this.mDirty |= 4;
                if (event == 1) {
                    this.mLastUserActivityButtonTime = eventTime;
                }
                Trace.traceEnd(524288);
                return true;
            }
            Trace.traceEnd(524288);
            return false;
        } catch (Throwable th) {
            Trace.traceEnd(524288);
        }
    }

    private int getScreenStateInternal() {
        int result = 0;
        if (!(this.mDisplayManagerInternal == null || this.mDisplayManagerInternal.getScreenState() != 1 || this.mStartGoToSleep)) {
            result = 1;
        }
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "get Screen State, result = " + result + ", start sleep = " + this.mStartGoToSleep);
        }
        return result;
    }

    /* JADX WARNING: Missing block: B:14:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void wakeUpInternal(long eventTime, String reason, int uid, String opPackageName, int opUid) {
        synchronized (this.mLock) {
            if (this.mIPOShutdown && reason != "shutdown") {
            } else if (wakeUpNoUpdateLocked(eventTime, reason, uid, opPackageName, opUid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean wakeUpNoUpdateLocked(long eventTime, String reason, int reasonUid, String opPackageName, int opUid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "wakeUpNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", uid=" + reasonUid);
        }
        if (DEBUG) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, "   |----" + element.toString());
            }
        }
        if (DEBUG_SPEW) {
            Slog.d(TAG, "wakeUpNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", uid=" + reasonUid);
        }
        if (reason == "shutdown") {
            synchronized (this.mLock) {
                this.mShutdownFlag = false;
                Slog.d(TAG, "mShutdownFlag = " + this.mShutdownFlag);
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
            return true;
        }
        if ("android.server.wm:TURN_ON".equals(reason)) {
            if (this.useProximityForceSuspend && this.mProximityPositive) {
                if (DEBUG_SPEW) {
                    Slog.i(TAG, "windowmanager try to wakeup device while proximity positive");
                }
                userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, reasonUid);
                return false;
            }
        } else if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            Slog.i(TAG, "wakeup due to fingerprint");
            if (this.mFingerprintScreenOnCallBack == null) {
                this.mFingerprintScreenOnCallBack = new FingerprintScreenOnCallBack(this, null);
            }
            if (this.mFingerprintScreenOnCallBack != null) {
                this.mFingerprintInternal.setOnVerifyMonitor(this.mFingerprintScreenOnCallBack);
            }
            this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
        } else if ("android.service.fingerprint:DOUBLE_HOME".equals(reason)) {
            if (DEBUG_PANIC) {
                Slog.i(TAG, "wakeup due to double home");
            }
            this.mScreenOnCpuBoostHelper.acquireCpuBoost(500);
        } else if ("oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason) || "oppo.wakeup.gesture:LIFT_HAND".equals(reason)) {
            this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
        }
        if (!"android.service.fingerprint:WAKEUP".equals(reason)) {
            if (!this.mDisplayManagerInternal.isBlockDisplayByFingerPrint() || this.mFingerprintHook == null) {
                this.mDisplayManagerInternal.unblockScreenOnByFingerPrint(true);
            } else {
                Slog.d(TAG, "screenOnUnBlockedByOther, delay 400ms for alpha change");
                this.mFingerprintHook.onScreenOnUnBlockedByOther();
                if (("android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason) || "oppo.wakeup.gesture:LIFT_HAND".equals(reason)) && this.mFaceHook != null) {
                    this.mFaceHook.onScreenOnUnBlockedByOther(reason);
                }
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
        }
        if (eventTime < this.mLastSleepTime || this.mWakefulness == 1 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        this.mStartGoToSleep = false;
        if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            this.mDisplayManagerInternal.blockScreenOnByFingerPrint();
            if (this.mFingerprintHook != null) {
                this.mFingerprintHook.onWakeUp(true);
            }
        } else if (this.mFingerprintHook != null) {
            this.mFingerprintHook.onWakeUp(false);
        }
        if (("android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason) || "oppo.wakeup.gesture:LIFT_HAND".equals(reason)) && this.mFaceHook != null) {
            this.mFaceHook.onWakeUp(reason);
        }
        Trace.traceBegin(524288, "wakeUp");
        try {
            switch (this.mWakefulness) {
                case 0:
                    Slog.i(TAG, "Waking up from sleep (uid " + reasonUid + ")...");
                    break;
                case 2:
                    Slog.i(TAG, "Waking up from dream (uid " + reasonUid + ")...");
                    break;
                case 3:
                    Slog.i(TAG, "Waking up from dozing (uid " + reasonUid + ")...");
                    break;
            }
            this.mLastWakeTime = eventTime;
            if ("android.service.fingerprint:WAKEUP".equals(reason)) {
                setWakefulnessLocked(1, 98);
            } else {
                setWakefulnessLocked(1, 0);
            }
            this.mPowerMonitor.screenOn();
            this.mNotifier.onWakeUp(reason, reasonUid, opPackageName, opUid);
            userActivityNoUpdateLocked(eventTime, 0, 0, reasonUid);
            this.mOppoHelper.updateButtonBrightness(this, false);
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.PartialWakelockCheckStop();
            }
            return true;
        } finally {
            Trace.traceEnd(524288);
        }
    }

    private void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
        if (reason == 99 && !this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
            return;
        }
        if (reason == 4 && this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
            this.mDisplayManagerInternal.unblockScreenOnByFingerPrint(true);
            this.mFingerprintInternal.notifyPowerKeyPressed();
            Slog.d(TAG, "Not goTosleep( " + reason + " ) due to FingerPrint", new Throwable("FP DEBUG"));
            return;
        }
        synchronized (this.mLock) {
            if (goToSleepNoUpdateLocked(eventTime, reason, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean goToSleepNoUpdateLocked(long eventTime, int reason, int flags, int uid) {
        if (DEBUG) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, " \t|----" + element.toString());
            }
        }
        if (DEBUG_PANIC) {
            Slog.d(TAG, "goToSleepNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", flags=" + flags + ", uid=" + uid);
        }
        if (reason == 7) {
            this.mDirty |= 32;
            this.mShutdownFlag = true;
            Slog.d(TAG, "mShutdownFlag = " + this.mShutdownFlag);
            return true;
        } else if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || this.mWakefulness == 3 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        } else {
            if (mOppoShutdownIng) {
                Slog.d(TAG, "goToSleepNoUpdateLocked: Not go to sleep when shutdown!!!");
                return false;
            }
            this.mStartGoToSleep = true;
            if (this.mFingerprintHook != null) {
                this.mFingerprintHook.onGoToSleep();
            }
            if (this.mFaceHook != null) {
                this.mFaceHook.onGoToSleep();
            }
            Trace.traceBegin(524288, "goToSleep");
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
                case 8:
                    Slog.i(TAG, "Going to sleep due to proximity (uid " + uid + ")...");
                    break;
                default:
                    try {
                        Slog.i(TAG, "Going to sleep by application request (uid " + uid + ")...");
                        reason = 0;
                        break;
                    } catch (Throwable th) {
                        Trace.traceEnd(524288);
                    }
            }
            this.mScreenOffReason = reason;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (PowerManagerService.DEBUG_PANIC) {
                        Slog.i(PowerManagerService.TAG, "update sys.power.screenoff.reason");
                    }
                    SystemProperties.set("sys.power.screenoff.reason", IElsaManager.EMPTY_PACKAGE + PowerManagerService.this.mScreenOffReason);
                }
            });
            this.mLastSleepTime = eventTime;
            this.mSandmanSummoned = true;
            setWakefulnessLocked(3, reason);
            int numWakeLocksCleared = 0;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
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
            if ((flags & 1) != 0 || this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
            }
            Trace.traceEnd(524288);
            this.mPowerMonitor.clear();
            this.mPowerMonitor.screenOff();
            this.mOppoHelper.turnOffButtonLight();
            if (this.mWakeLockCheck != null) {
                this.mWakeLockCheck.PartialWakelockCheckStart();
            }
            return true;
        }
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
        if (eventTime < this.mLastWakeTime || this.mWakefulness != 1 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(524288, "nap");
        try {
            Slog.i(TAG, "Nap time (uid " + uid + ")...");
            this.mSandmanSummoned = true;
            setWakefulnessLocked(2, 0);
            return true;
        } finally {
            Trace.traceEnd(524288);
        }
    }

    private boolean reallyGoToSleepNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "reallyGoToSleepNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(524288, "reallyGoToSleep");
        try {
            Slog.i(TAG, "Sleeping (uid " + uid + ")...");
            setWakefulnessLocked(0, 2);
            return true;
        } finally {
            Trace.traceEnd(524288);
        }
    }

    private void setWakefulnessLocked(int wakefulness, int reason) {
        if (this.mWakefulness != wakefulness) {
            this.mWakefulness = wakefulness;
            this.mWakefulnessChanging = true;
            this.mDirty |= 2;
            this.mNotifier.onWakefulnessChangeStarted(wakefulness, reason);
        }
    }

    private void logSleepTimeoutRecapturedLocked() {
        long savedWakeTimeMs = this.mOverriddenTimeout - SystemClock.uptimeMillis();
        if (savedWakeTimeMs >= 0) {
            EventLog.writeEvent(EventLogTags.POWER_SOFT_SLEEP_REQUESTED, savedWakeTimeMs);
            this.mOverriddenTimeout = -1;
        }
    }

    private void finishWakefulnessChangeIfNeededLocked() {
        if (this.mWakefulnessChanging && this.mDisplayReady && (this.mWakefulness != 3 || (this.mWakeLockSummary & 64) != 0)) {
            if (this.mWakefulness == 3 || this.mWakefulness == 0) {
                logSleepTimeoutRecapturedLocked();
            }
            this.mWakefulnessChanging = false;
            if (this.mFingerprintHook != null && this.mWakefulness == 1) {
                this.mFingerprintHook.onWakeUpFinish();
            }
            if (this.mFaceHook != null && this.mWakefulness == 1) {
                this.mFaceHook.onWakeUpFinish();
            }
            this.mNotifier.onWakefulnessChangeFinished();
        }
    }

    private void updatePowerStateLocked() {
        if (this.mSystemReady && this.mDirty != 0) {
            if (!Thread.holdsLock(this.mLock)) {
                Slog.wtf(TAG, "Power manager lock was not held when calling updatePowerStateLocked");
            }
            Trace.traceBegin(524288, "updatePowerState");
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
                Trace.traceEnd(524288);
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
                if (!(oldLevelLow == this.mBatteryLevelLow || this.mBatteryLevelLow)) {
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
        if (wasPowered && !this.mIsPowered && oldPlugType == 4) {
            return false;
        }
        if (!wasPowered && this.mIsPowered && this.mPlugType == 4 && !dockedOnWirelessCharger) {
            return false;
        }
        if (this.mIsPowered && this.mWakefulness == 2) {
            return false;
        }
        if (!this.mTheaterModeEnabled || this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig) {
            return true;
        }
        return false;
    }

    private void updateStayOnLocked(int dirty) {
        if ((dirty & 288) != 0) {
            boolean wasStayOn = this.mStayOn;
            if (this.mStayOnWhilePluggedInSetting == 0 || isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
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
                switch (wakeLock.mFlags & 65535) {
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

    private boolean bypassUserActivityTimeout() {
        if (this.mStayOnWithoutDim) {
            Slog.d(TAG, "bypass UserActivityTimeout because of setting");
            return true;
        } else if (!SystemProperties.getBoolean("persist.keep.awake", false)) {
            return false;
        } else {
            Slog.d(TAG, "bypass UserActivityTimeout because of system property request");
            return true;
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
                if ((wakeLock.mFlags & 65535) == 1 && wakeLock.mNotifiedAcquired && !wakeLock.mNotifiedLong) {
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
                int screenButtonLightDuration = getButtonLightDurationLocked(screenOffTimeout);
                this.mUserActivitySummary = 0;
                if (this.mLastUserActivityTime >= this.mLastWakeTime) {
                    if (now < (this.mLastUserActivityTime + ((long) screenOffTimeout)) - ((long) screenDimDuration)) {
                        nextTimeout = (this.mLastUserActivityTime + ((long) screenOffTimeout)) - ((long) screenDimDuration);
                        this.mUserActivitySummary |= 1;
                    } else {
                        nextTimeout = this.mLastUserActivityTime + ((long) screenOffTimeout);
                        if (now < nextTimeout) {
                            this.mUserActivitySummary |= 2;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0 && this.mLastUserActivityTimeNoChangeLights >= this.mLastWakeTime) {
                    nextTimeout = this.mLastUserActivityTimeNoChangeLights + ((long) screenOffTimeout);
                    if (now < nextTimeout) {
                        if (this.mDisplayPowerRequest.policy == 3) {
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
            if (obj == null && (dirty & 1) == 0 && nextTimeout >= 0) {
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
        if ((dirty & 512) != 0 && this.mProximityPositive) {
            this.mOppoHelper.turnOffButtonLight();
        }
    }

    private void handleUserActivityTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_PANIC) {
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
        if (this.mUserActivityTimeoutMin) {
            timeout = Math.min(timeout, this.mUserActivityTimeoutOverrideFromCMD);
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private int getScreenDimDurationLocked(int screenOffTimeout) {
        return Math.min(this.mMaximumScreenDimDurationConfig, (int) (((float) screenOffTimeout) * this.mMaximumScreenDimRatioConfig));
    }

    private int getButtonLightDurationLocked(int screenOffTimeout) {
        return Math.min(SCREEN_BUTTON_LIGHT_DURATION, (int) (((float) screenOffTimeout) * MAXIMUM_SCREEN_BUTTON_RATIO));
    }

    private boolean updateWakefulnessLocked(int dirty) {
        if ((dirty & 1687) == 0 || this.mWakefulness != 1 || !isItBedTimeYetLocked()) {
            return false;
        }
        if (DEBUG_PANIC) {
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
        return (!this.mBootCompleted || isBeingKeptAwakeLocked() || this.mIPOShutdown) ? false : true;
    }

    private boolean isBeingKeptAwakeLocked() {
        if (this.mStayOn || ((this.mProximityPositive && !this.useProximityForceSuspend) || (this.mWakeLockSummary & 32) != 0 || (this.mUserActivitySummary & 3) != 0 || this.mScreenBrightnessBoostInProgress)) {
            return true;
        }
        return bypassUserActivityTimeout();
    }

    private void updateDreamLocked(int dirty, boolean displayBecameReady) {
        if (((dirty & 13303) != 0 || displayBecameReady) && this.mDisplayReady) {
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

    /* JADX WARNING: Missing block: B:37:0x0077, code:
            return;
     */
    /* JADX WARNING: Missing block: B:62:0x00ae, code:
            return;
     */
    /* JADX WARNING: Missing block: B:69:0x0105, code:
            if (r0 == false) goto L_0x0119;
     */
    /* JADX WARNING: Missing block: B:71:0x0109, code:
            if (DEBUG_SPEW == false) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:72:0x010b, code:
            android.util.Slog.i(TAG, "handleSandman stopDream(false)");
     */
    /* JADX WARNING: Missing block: B:73:0x0114, code:
            r12.mDreamManager.stopDream(false);
     */
    /* JADX WARNING: Missing block: B:74:0x0119, code:
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
                if (DEBUG_SPEW) {
                    Slog.i(TAG, "handleSandman startDreaming = " + startDreaming);
                }
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
                        if (this.mDreamsBatteryLevelDrainCutoffConfig < 0 || this.mBatteryLevel >= this.mBatteryLevelWhenDreamStarted - this.mDreamsBatteryLevelDrainCutoffConfig || isBeingKeptAwakeLocked()) {
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
        if (DEBUG_SPEW) {
            Slog.i(TAG, "canDreamLocked mWakefulness = " + this.mWakefulness + ", mDreamsSupportedConfig = " + this.mDreamsSupportedConfig + ", mDreamsEnabledSetting = " + this.mDreamsEnabledSetting + ", mDisplayPowerRequest.isBrightOrDim() = " + this.mDisplayPowerRequest.isBrightOrDim() + ", mUserActivitySummary = " + this.mUserActivitySummary + ", mBootCompleted = " + this.mBootCompleted);
        }
        if (this.mWakefulness != 2 || !this.mDreamsSupportedConfig || !this.mDreamsEnabledSetting || !this.mDisplayPowerRequest.isBrightOrDim() || (this.mUserActivitySummary & 7) == 0 || !this.mBootCompleted) {
            return false;
        }
        if (!isBeingKeptAwakeLocked()) {
            if (!this.mIsPowered && !this.mDreamsEnabledOnBatteryConfig) {
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
        if ((dirty & 6207) != 0) {
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
            if (isValidBrightness(this.mScreenBrightnessOverrideFromWindowManager)) {
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
            this.mDisplayPowerRequest.boostScreenBrightness = this.mScreenBrightnessBoostInProgress;
            this.mDisplayPowerRequest.useTwilight = this.mBrightnessUseTwilight;
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
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateDisplayPowerStateLocked: mDisplayReady=" + this.mDisplayReady + ", policy=" + DisplayPowerRequest.policyToString(this.mDisplayPowerRequest.policy) + ", mWakefulness=" + this.mWakefulness + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", mBootCompleted=" + this.mBootCompleted + ", mScreenBrightnessBoostInProgress=" + this.mScreenBrightnessBoostInProgress);
            }
        }
        if (!this.mDisplayReady || oldDisplayReady) {
            return false;
        }
        return true;
    }

    private void updateScreenBrightnessBoostLocked(int dirty) {
        if ((dirty & 2048) != 0 && this.mScreenBrightnessBoostInProgress) {
            long now = SystemClock.uptimeMillis();
            this.mHandler.removeMessages(3);
            if (this.mLastScreenBrightnessBoostTime > this.mLastSleepTime) {
                long boostTimeout = this.mLastScreenBrightnessBoostTime + 5000;
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

    private static boolean isValidBrightness(int value) {
        return value >= 0 && value <= mScreenBrightnessSettingMaximum;
    }

    private static boolean isValidAutoBrightnessAdjustment(float value) {
        if (value < ((float) mScreenBrightnessSettingMinimum) || value > ((float) mScreenBrightnessSettingMaximum)) {
            return mOppoBrightUtils.isSpecialAdj(value);
        }
        return true;
    }

    private int getDesiredScreenPolicyLocked() {
        if (this.mWakefulness == 0) {
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
        if ((this.mWakeLockSummary & 2) == 0 && (this.mUserActivitySummary & 1) == 0 && this.mBootCompleted && !this.mScreenBrightnessBoostInProgress && !bypassUserActivityTimeout()) {
            return 2;
        }
        return 3;
    }

    private boolean shouldUseProximitySensorLocked() {
        return (this.mWakeLockSummary & 16) != 0;
    }

    private void updateSuspendBlockerLocked() {
        boolean needWakeLockSuspendBlocker = (this.mWakeLockSummary & 1) != 0;
        boolean needDisplaySuspendBlocker = needDisplaySuspendBlockerLocked();
        boolean autoSuspend = !needDisplaySuspendBlocker;
        boolean interactive = this.mDisplayPowerRequest.isBrightOrDim();
        if (!autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(false);
        }
        if (needWakeLockSuspendBlocker && !this.mHoldingWakeLockSuspendBlocker) {
            this.mHoldingWakeLockSuspendBlocker = true;
            this.mWakeLockSuspendBlocker.acquire();
        }
        if (needDisplaySuspendBlocker && !this.mHoldingDisplaySuspendBlocker) {
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

    /* JADX WARNING: Missing block: B:14:0x0021, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean needDisplaySuspendBlockerLocked() {
        if (!this.mDisplayReady) {
            return true;
        }
        if ((!this.mDisplayPowerRequest.isBrightOrDim() || (this.mDisplayPowerRequest.useProximitySensor && this.mProximityPositive && this.mSuspendWhenScreenOffDueToProximityConfig)) && !this.mScreenBrightnessBoostInProgress) {
            return false;
        }
        return true;
    }

    private void setHalAutoSuspendModeLocked(boolean enable) {
        if (enable != this.mHalAutoSuspendModeEnabled) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable);
            }
            this.mHalAutoSuspendModeEnabled = enable;
            Trace.traceBegin(524288, "setHalAutoSuspend(" + enable + ")");
            try {
                nativeSetAutoSuspend(enable);
            } finally {
                Trace.traceEnd(524288);
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable + " done");
                }
            }
        }
    }

    private void setHalInteractiveModeLocked(boolean enable) {
        if (enable != this.mHalInteractiveModeEnabled) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "Setting HAL interactive mode to " + enable);
            }
            this.mHalInteractiveModeEnabled = enable;
            Trace.traceBegin(524288, "setHalInteractive(" + enable + ")");
            try {
                nativeSetInteractive(enable);
            } finally {
                Trace.traceEnd(524288);
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
                    if (!this.mAutoLowPowerModeSnoozing) {
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
                        ((LowPowerModeListener) listeners.get(i)).onLowPowerModeChanged(PowerManagerService.this.mColorOsLowPowerModeEnabled.get());
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
        if (enable && !this.mHypnusLowPowerenabled.get()) {
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
        if (this.mHandler == null || !this.mSystemReady) {
            throw new IllegalStateException("Too early to call shutdown() or reboot()");
        }
        Runnable runnable = new Runnable() {
            public void run() {
                synchronized (this) {
                    if (haltMode == 2) {
                        ShutdownThread.rebootSafeMode(PowerManagerService.this.mContext, confirm);
                    } else if (haltMode == 1) {
                        ShutdownThread.reboot(PowerManagerService.this.mContext, reason, confirm);
                    } else {
                        ShutdownThread.shutdown(PowerManagerService.this.mContext, reason, confirm);
                    }
                }
            }
        };
        Message msg = Message.obtain(this.mHandler, runnable);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
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

    /* JADX WARNING: Missing block: B:11:0x0010, code:
            if (r3 == false) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:12:0x0012, code:
            com.android.server.EventLogTags.writeDeviceIdleOnPhase("power");
     */
    /* JADX WARNING: Missing block: B:14:0x0019, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:18:0x001d, code:
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

    void updateUidProcStateInternal(int uid, int procState) {
        synchronized (this.mLock) {
            this.mUidState.put(uid, procState);
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void uidGoneInternal(int uid) {
        synchronized (this.mLock) {
            this.mUidState.delete(uid);
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    private void updateWakeLockDisabledStatesLocked() {
        boolean changed = false;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = 0; i < numWakeLocks; i++) {
            WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
            if ((wakeLock.mFlags & 65535) == 1 && setWakeLockDisabledStateLocked(wakeLock)) {
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
        if ((wakeLock.mFlags & 65535) == 1) {
            boolean disabled = false;
            if (this.mDeviceIdleMode) {
                int appid = UserHandle.getAppId(wakeLock.mOwnerUid);
                if (appid >= 10000 && Arrays.binarySearch(this.mDeviceIdleWhitelist, appid) < 0 && Arrays.binarySearch(this.mDeviceIdleTempWhitelist, appid) < 0 && this.mUidState.get(wakeLock.mOwnerUid, 16) > 4) {
                    disabled = true;
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
                this.mScreenBrightnessOverrideFromWindowManager = brightness;
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
                if (DEBUG_PANIC) {
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
            if (this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride != adj) {
                if (DEBUG_PANIC) {
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

    private void powerHintInternal(int hintId, int data) {
        nativeSendPowerHint(hintId, data);
    }

    public static void lowLevelShutdown(String reason) {
        if (reason == null) {
            reason = IElsaManager.EMPTY_PACKAGE;
        }
        SystemProperties.set("sys.powerctl", "shutdown," + reason);
        Slog.d(TAG, "lowLevelShutdown, sys.powerctl = shutdown");
    }

    public static void lowLevelReboot(String reason) {
        if (reason == null) {
            reason = IElsaManager.EMPTY_PACKAGE;
        }
        if (reason.equals("recovery") || reason.equals("recovery-update")) {
            SystemProperties.set("sys.powerctl", "reboot,recovery");
        } else if (reason.equals("meta_wifi")) {
            SystemProperties.set("persist.meta.connecttype", "wifi");
            SystemProperties.set("ctl.start", "pre_meta");
        } else if (reason.equals("meta_usb")) {
            SystemProperties.set("persist.meta.connecttype", "usb");
            SystemProperties.set("ctl.start", "pre_meta");
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
                reason = IElsaManager.EMPTY_PACKAGE;
            }
            SystemProperties.set("sys.powerctl", "reboot," + reason);
            Slog.d(TAG, "lowLevelReboot, reboot reason is " + reason);
        }
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
        boolean z = false;
        if (args.length < 1) {
            return false;
        }
        String cmd = args[0];
        if ("log".equals(cmd)) {
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
            } else if ("power".equals(logCategoryTag)) {
                DEBUG_SPEW = on;
            } else {
                pw.println("Invalid log tag argument! Get detail help as bellow:");
                logOutPowerManagerServiceLogTagHelp(pw);
            }
            return true;
        } else if (!"debug_switch".equals(cmd)) {
            return false;
        } else {
            StringBuilder append = new StringBuilder().append("  all=");
            if (DEBUG_PANIC && DEBUG && DEBUG_SPEW && LightsService.DEBUG) {
                z = Notifier.DEBUG_PANIC;
            }
            pw.println(append.append(z).toString());
            return true;
        }
    }

    protected void logOutPowerManagerServiceLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All PowerManagerService log");
        pw.println("cmd: dumpsys power log all 0/1");
        pw.println("2 power PowerManagerService log");
        pw.println("cmd: dumpsys power log power 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private void dumpInternal(PrintWriter pw) {
        WirelessChargerDetector wcd;
        pw.println("POWER MANAGER (dumpsys power)\n");
        synchronized (this.mLock) {
            PrintWriter printWriter;
            pw.println("Power Manager State:");
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
            pw.println("  mDoubleTapWakeEnabled=" + this.mDoubleTapWakeEnabled);
            int sleepTimeout = getSleepTimeoutLocked();
            int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            pw.println();
            pw.println("Sleep timeout: " + sleepTimeout + " ms");
            pw.println("Screen off timeout: " + screenOffTimeout + " ms");
            pw.println("Screen dim duration: " + screenDimDuration + " ms");
            pw.println();
            pw.println("UID states:");
            for (int i = 0; i < this.mUidState.size(); i++) {
                pw.print("  UID ");
                UserHandle.formatUid(pw, this.mUidState.keyAt(i));
                pw.print(": ");
                pw.println(this.mUidState.valueAt(i));
            }
            pw.println();
            pw.println("Looper state:");
            this.mHandler.getLooper().dump(new PrintWriterPrinter(pw), "  ");
            pw.println();
            pw.println("Wake Locks: size=" + this.mWakeLocks.size());
            for (WakeLock wl : this.mWakeLocks) {
                printWriter = pw;
                printWriter.println("  " + wl);
            }
            pw.println();
            pw.println("Suspend Blockers: size=" + this.mSuspendBlockers.size());
            for (SuspendBlocker sb : this.mSuspendBlockers) {
                printWriter = pw;
                printWriter.println("  " + sb);
            }
            pw.println();
            pw.println("Display Power: " + this.mDisplayPowerCallbacks);
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

    public int getUserActivitySummary() {
        return this.mUserActivitySummary;
    }

    public int getwakefulness() {
        return this.mWakefulness;
    }

    public boolean needScreenOnWakelockCheck() {
        if (this.mWakefulness == 1 && this.mUserActivitySummary == 4 && !this.mHandler.hasMessages(1)) {
            return true;
        }
        return false;
    }

    public void releaseWakeLockByGuardElf(IBinder lock, int flags) {
        releaseWakeLockInternal(lock, flags);
    }

    public WakeLock cloneWakeLock(WakeLock wl) {
        return new WakeLock(wl.mLock, wl.mFlags, wl.mTag, wl.mPackageName, wl.mWorkSource, wl.mHistoryTag, wl.mOwnerUid, wl.mOwnerPid);
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
        } else if (!"wakelockCheck".equals(cmd) || !"log".equals(args[1])) {
            return false;
        } else {
            if ("on".equals(args[2])) {
                this.mWakeLockCheck.logSwitch(true);
                pw.println("wakelockCheck log on.");
            } else if ("off".equals(args[2])) {
                this.mWakeLockCheck.logSwitch(false);
                pw.println("wakelockCheck log off.");
            }
            return true;
        }
    }
}
