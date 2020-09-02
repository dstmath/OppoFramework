package com.android.server.power;

import android.annotation.OppoHook;
import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.hardware.display.AmbientDisplayConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.BatteryManagerInternal;
import android.os.BatterySaverPolicyConfig;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.KeyValueListParser;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.server.EventLogTags;
import com.android.server.LockGuard;
import com.android.server.ServiceThread;
import com.android.server.Watchdog;
import com.android.server.am.BatteryStatsService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.oppo.OppoUsageService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.batterysaver.BatterySaverController;
import com.android.server.power.batterysaver.BatterySaverPolicy;
import com.android.server.power.batterysaver.BatterySaverStateMachine;
import com.android.server.power.batterysaver.BatterySavingStats;
import com.android.server.utils.PriorityDump;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PowerManagerService extends OppoBasePowerManagerService implements Watchdog.Monitor {
    public static final String AodUserSetEnable = "Setting_AodEnable";
    private static final int DEFAULT_DOUBLE_TAP_TO_WAKE = 0;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP_TIMEOUT = -1;
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
    private static final int DIRTY_WAKE_LOCKS = 1;
    public static final String FINGERPRINT_UNLOCK = "show_fingerprint_when_screen_off";
    public static final String FINGERPRINT_UNLOCK_SWITCH = "coloros_fingerprint_unlock_switch";
    private static final int HALT_MODE_REBOOT = 1;
    private static final int HALT_MODE_REBOOT_SAFE_MODE = 2;
    private static final int HALT_MODE_SHUTDOWN = 0;
    static final long MIN_LONG_WAKE_CHECK_INTERVAL = 60000;
    private static final int MSG_CHECK_FOR_LONG_WAKELOCKS = 4;
    private static final int MSG_FSTART_DEAM = 105;
    private static final int MSG_SANDMAN = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 3;
    private static final int MSG_STOP_DEAM = 102;
    private static final int MSG_USER_ACTIVITY_TIMEOUT = 1;
    private static final int POWER_FEATURE_DOUBLE_TAP_TO_WAKE = 1;
    private static final String REASON_BATTERY_THERMAL_STATE = "shutdown,thermal,battery";
    private static final String REASON_LOW_BATTERY = "shutdown,battery";
    private static final String REASON_REBOOT = "reboot";
    private static final String REASON_SHUTDOWN = "shutdown";
    private static final String REASON_THERMAL_SHUTDOWN = "shutdown,thermal";
    private static final String REASON_USERREQUESTED = "shutdown,userrequested";
    private static final String REBOOT_PROPERTY = "sys.boot.reason";
    private static final int SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 5000;
    private static final int SCREEN_ON_LATENCY_WARNING_MS = 100;
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
    public static boolean mOppoAodSupport = false;
    private static OppoBrightUtils mOppoBrightUtils;
    public static boolean mOppoShutdownIng = false;
    public static int mScreenBrightnessSettingMaximum;
    /* access modifiers changed from: private */
    public static int mScreenBrightnessSettingMinimum;
    private static boolean sQuiescent;
    boolean DEBUG;
    boolean DEBUG_PANIC;
    boolean DEBUG_SPEW;
    private boolean mAlwaysOnEnabled;
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    public int mAodUserSetEnable;
    private IAppOpsService mAppOps;
    private final AttentionDetector mAttentionDetector;
    private Light mAttentionLight;
    private int mBatteryLevel;
    private boolean mBatteryLevelLow;
    private int mBatteryLevelWhenDreamStarted;
    private BatteryManagerInternal mBatteryManagerInternal;
    /* access modifiers changed from: private */
    public final BatterySaverController mBatterySaverController;
    /* access modifiers changed from: private */
    public final BatterySaverPolicy mBatterySaverPolicy;
    /* access modifiers changed from: private */
    public final BatterySaverStateMachine mBatterySaverStateMachine;
    private final BatterySavingStats mBatterySavingStats;
    private IBatteryStats mBatteryStats;
    private final BinderService mBinderService;
    private boolean mBootCompleted;
    final Constants mConstants;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDecoupleHalAutoSuspendModeFromDisplayConfig;
    /* access modifiers changed from: private */
    public boolean mDecoupleHalInteractiveModeFromDisplayConfig;
    private boolean mDeviceIdleMode;
    int[] mDeviceIdleTempWhitelist;
    int[] mDeviceIdleWhitelist;
    private int mDirty;
    /* access modifiers changed from: private */
    public DisplayManagerInternal mDisplayManagerInternal;
    private final DisplayManagerInternal.DisplayPowerCallbacks mDisplayPowerCallbacks;
    private final DisplayManagerInternal.DisplayPowerRequest mDisplayPowerRequest;
    private boolean mDisplayReady;
    /* access modifiers changed from: private */
    public final SuspendBlocker mDisplaySuspendBlocker;
    /* access modifiers changed from: private */
    public int mDockState;
    private boolean mDoubleTapWakeEnabled;
    private boolean mDozeAfterScreenOff;
    private int mDozeScreenBrightnessOverrideFromDreamManager;
    private int mDozeScreenStateOverrideFromDreamManager;
    public HashMap<Integer, Integer> mDozeStateMap;
    private boolean mDrawWakeLockOverrideFromSidekick;
    /* access modifiers changed from: private */
    public DreamManagerInternal mDreamManager;
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
    public boolean mFingerprintOpticalSupport;
    public int mFingerprintUnlock;
    public int mFingerprintUnlockswitch;
    private IBinder mFlinger;
    private boolean mForceSuspendActive;
    /* access modifiers changed from: private */
    public int mForegroundProfile;
    private boolean mHalAutoSuspendModeEnabled;
    private boolean mHalInteractiveModeEnabled;
    /* access modifiers changed from: private */
    public final PowerManagerHandler mHandler;
    private final ServiceThread mHandlerThread;
    private boolean mHoldingDisplaySuspendBlocker;
    private boolean mHoldingWakeLockSuspendBlocker;
    private final Injector mInjector;
    private boolean mIsPowered;
    /* access modifiers changed from: private */
    public boolean mIsVrModeEnabled;
    private long mLastInteractivePowerHintTime;
    private long mLastScreenBrightnessBoostTime;
    private int mLastSleepReason;
    protected long mLastSleepTime;
    /* access modifiers changed from: private */
    public int mLastSystemUiPid;
    private long mLastUserActivityTime;
    private long mLastUserActivityTimeNoChangeLights;
    private int mLastWakeReason;
    protected long mLastWakeTime;
    /* access modifiers changed from: private */
    public long mLastWarningAboutUserActivityPermission;
    private boolean mLightDeviceIdleMode;
    private LightsManager mLightsManager;
    private final LocalService mLocalService;
    /* access modifiers changed from: private */
    public final Object mLock;
    public final Object mMapLock;
    private long mMaximumScreenDimDurationConfig;
    private float mMaximumScreenDimRatioConfig;
    private long mMaximumScreenOffTimeoutFromDeviceAdmin;
    private long mMinimumScreenOffTimeoutConfig;
    /* access modifiers changed from: private */
    public final NativeWrapper mNativeWrapper;
    private Notifier mNotifier;
    private long mNotifyLongDispatched;
    private long mNotifyLongNextCheck;
    private long mNotifyLongScheduled;
    OppoHelper mOppoHelper;
    /* access modifiers changed from: private */
    public OppoPowerManagerHelper mOppoPowerManagerHelper;
    private long mOverriddenTimeout;
    private int mPlugType;
    private WindowManagerPolicy mPolicy;
    /* access modifiers changed from: private */
    public PowerMonitor mPowerMonitor;
    private final SparseArray<ProfilePowerState> mProfilePowerState;
    /* access modifiers changed from: private */
    public boolean mProximityPositive;
    private boolean mRequestWaitForNegativeProximity;
    private boolean mSandmanScheduled;
    private boolean mSandmanSummoned;
    private boolean mScreenBrightnessBoostInProgress;
    private int mScreenBrightnessModeSetting;
    private int mScreenBrightnessOverrideFromWindowManager;
    private int mScreenBrightnessSetting;
    /* access modifiers changed from: private */
    public int mScreenBrightnessSettingDefault;
    /* access modifiers changed from: private */
    public long mScreenOffTimeoutSetting;
    public int mScreenState;
    private SettingsObserver mSettingsObserver;
    private boolean mShutdownFlag;
    private long mSleepTimeoutSetting;
    private boolean mStayOn;
    private int mStayOnWhilePluggedInSetting;
    private boolean mSupportsDoubleTapWakeConfig;
    /* access modifiers changed from: private */
    public final ArrayList<SuspendBlocker> mSuspendBlockers;
    private boolean mSuspendWhenScreenOffDueToProximityConfig;
    /* access modifiers changed from: private */
    public boolean mSystemReady;
    private float mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
    private boolean mTheaterModeEnabled;
    /* access modifiers changed from: private */
    public final SparseArray<UidState> mUidState;
    private boolean mUidsChanged;
    private boolean mUidsChanging;
    /* access modifiers changed from: private */
    public int mUserActivitySummary;
    /* access modifiers changed from: private */
    public long mUserActivityTimeoutOverrideFromWindowManager;
    private boolean mUserInactiveOverrideFromWindowManager;
    private final IVrStateCallbacks mVrStateCallbacks;
    private int mWakeLockSummary;
    private final SuspendBlocker mWakeLockSuspendBlocker;
    /* access modifiers changed from: private */
    public final ArrayList<WakeLock> mWakeLocks;
    private boolean mWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig;
    /* access modifiers changed from: private */
    public int mWakefulness;
    private boolean mWakefulnessChanging;
    private WirelessChargerDetector mWirelessChargerDetector;

    @Retention(RetentionPolicy.SOURCE)
    public @interface HaltMode {
    }

    /* access modifiers changed from: private */
    public static native void nativeAcquireSuspendBlocker(String str);

    /* access modifiers changed from: private */
    public static native boolean nativeForceSuspend();

    /* access modifiers changed from: private */
    public native void nativeInit();

    /* access modifiers changed from: private */
    public static native void nativeReleaseSuspendBlocker(String str);

    /* access modifiers changed from: private */
    public static native void nativeSendPowerHint(int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetAutoSuspend(boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetFeature(int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetInteractive(boolean z);

    static /* synthetic */ int access$1676(PowerManagerService x0, int x1) {
        int i = x0.mDirty | x1;
        x0.mDirty = i;
        return i;
    }

    private final class ForegroundProfileObserver extends SynchronousUserSwitchObserver {
        private ForegroundProfileObserver() {
        }

        public void onUserSwitching(int newUserId) throws RemoteException {
        }

        public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
            long now = SystemClock.uptimeMillis();
            synchronized (PowerManagerService.this.mLock) {
                int unused = PowerManagerService.this.mForegroundProfile = newProfileId;
                PowerManagerService.this.maybeUpdateForegroundProfileLastActivityLocked(now);
            }
        }
    }

    private static final class ProfilePowerState {
        long mLastUserActivityTime = SystemClock.uptimeMillis();
        boolean mLockingNotified;
        long mScreenOffTimeout;
        final int mUserId;
        int mWakeLockSummary;

        public ProfilePowerState(int userId, long screenOffTimeout) {
            this.mUserId = userId;
            this.mScreenOffTimeout = screenOffTimeout;
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
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("power_manager_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (PowerManagerService.this.mLock) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "power_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(PowerManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.NO_CACHED_WAKE_LOCKS = this.mParser.getBoolean(KEY_NO_CACHED_WAKE_LOCKS, true);
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("  Settings power_manager_constants:");
            pw.print("    ");
            pw.print(KEY_NO_CACHED_WAKE_LOCKS);
            pw.print("=");
            pw.println(this.NO_CACHED_WAKE_LOCKS);
        }

        /* access modifiers changed from: package-private */
        public void dumpProto(ProtoOutputStream proto) {
            long constantsToken = proto.start(1146756268033L);
            proto.write(1133871366145L, this.NO_CACHED_WAKE_LOCKS);
            proto.end(constantsToken);
        }
    }

    @VisibleForTesting
    public static class NativeWrapper {
        public void nativeInit(PowerManagerService service) {
            service.nativeInit();
        }

        public void nativeAcquireSuspendBlocker(String name) {
            PowerManagerService.nativeAcquireSuspendBlocker(name);
        }

        public void nativeReleaseSuspendBlocker(String name) {
            PowerManagerService.nativeReleaseSuspendBlocker(name);
        }

        public void nativeSetInteractive(boolean enable) {
            PowerManagerService.nativeSetInteractive(enable);
        }

        public void nativeSetAutoSuspend(boolean enable) {
            PowerManagerService.nativeSetAutoSuspend(enable);
        }

        public void nativeSendPowerHint(int hintId, int data) {
            PowerManagerService.nativeSendPowerHint(hintId, data);
        }

        public void nativeSetFeature(int featureId, int data) {
            PowerManagerService.nativeSetFeature(featureId, data);
        }

        public boolean nativeForceSuspend() {
            return PowerManagerService.nativeForceSuspend();
        }
    }

    @VisibleForTesting
    static class Injector {
        Injector() {
        }

        /* access modifiers changed from: package-private */
        public Notifier createNotifier(Looper looper, Context context, IBatteryStats batteryStats, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
            return new Notifier(looper, context, batteryStats, suspendBlocker, policy);
        }

        /* access modifiers changed from: package-private */
        public SuspendBlocker createSuspendBlocker(PowerManagerService service, String name) {
            Objects.requireNonNull(service);
            SuspendBlockerImpl suspendBlockerImpl = new SuspendBlockerImpl(name);
            service.mSuspendBlockers.add(suspendBlockerImpl);
            return suspendBlockerImpl;
        }

        /* access modifiers changed from: package-private */
        public BatterySaverPolicy createBatterySaverPolicy(Object lock, Context context, BatterySavingStats batterySavingStats) {
            return new BatterySaverPolicy(lock, context, batterySavingStats);
        }

        /* access modifiers changed from: package-private */
        public NativeWrapper createNativeWrapper() {
            return new NativeWrapper();
        }

        /* access modifiers changed from: package-private */
        public WirelessChargerDetector createWirelessChargerDetector(SensorManager sensorManager, SuspendBlocker suspendBlocker, Handler handler) {
            return new WirelessChargerDetector(sensorManager, suspendBlocker, handler);
        }

        /* access modifiers changed from: package-private */
        public AmbientDisplayConfiguration createAmbientDisplayConfiguration(Context context) {
            return new AmbientDisplayConfiguration(context);
        }
    }

    public PowerManagerService(Context context) {
        this(context, new Injector());
    }

    @VisibleForTesting
    PowerManagerService(Context context, Injector injector) {
        super(context);
        this.DEBUG = false;
        this.DEBUG_SPEW = this.DEBUG;
        this.DEBUG_PANIC = false;
        this.mAodUserSetEnable = 0;
        this.mFingerprintUnlock = 0;
        this.mFingerprintUnlockswitch = 0;
        this.mFingerprintOpticalSupport = false;
        this.mDozeStateMap = new HashMap<>();
        this.mMapLock = new Object();
        this.mScreenState = 0;
        this.mLock = LockGuard.installNewLock(1);
        this.mSuspendBlockers = new ArrayList<>();
        this.mWakeLocks = new ArrayList<>();
        this.mDisplayPowerRequest = new DisplayManagerInternal.DisplayPowerRequest();
        this.mDockState = 0;
        this.mMaximumScreenOffTimeoutFromDeviceAdmin = JobStatus.NO_LATEST_RUNTIME;
        this.mScreenBrightnessOverrideFromWindowManager = -1;
        this.mOverriddenTimeout = -1;
        this.mUserActivityTimeoutOverrideFromWindowManager = -1;
        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        this.mDozeScreenStateOverrideFromDreamManager = 0;
        this.mDozeScreenBrightnessOverrideFromDreamManager = -1;
        this.mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
        this.mShutdownFlag = false;
        this.mDeviceIdleWhitelist = new int[0];
        this.mDeviceIdleTempWhitelist = new int[0];
        this.mUidState = new SparseArray<>();
        this.mLastSystemUiPid = -1;
        this.mProfilePowerState = new SparseArray<>();
        this.mDisplayPowerCallbacks = new DisplayManagerInternal.DisplayPowerCallbacks() {
            /* class com.android.server.power.PowerManagerService.AnonymousClass1 */
            private int mDisplayState = 0;

            public void onStateChanged() {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService.access$1676(PowerManagerService.this, 8);
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositive() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositive");
                    SystemProperties.set("sys.power.screenoff.positive", "1");
                    boolean unused = PowerManagerService.this.mProximityPositive = true;
                    PowerManagerService.access$1676(PowerManagerService.this, 512);
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityNegative() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityNegative");
                    SystemProperties.set("sys.power.screenoff.positive", "0");
                    boolean unused = PowerManagerService.this.mProximityPositive = false;
                    PowerManagerService.access$1676(PowerManagerService.this, 512);
                    boolean unused2 = PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositiveForceSuspend() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityPositiveForceSuspend");
                    boolean unused = PowerManagerService.this.mProximityPositive = true;
                    if (PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 9, 0, 1000)) {
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }

            public void onProximityNegativeForceSuspend() {
                synchronized (PowerManagerService.this.mLock) {
                    Slog.i(PowerManagerService.TAG, "onProximityNegativeForceSuspend");
                    boolean unused = PowerManagerService.this.mProximityPositive = false;
                    PowerManagerService.this.wakeUpInternal(SystemClock.uptimeMillis(), 97, "android.service.power:proximity", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000);
                }
            }

            public void onDisplayStateChange(int state) {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService.this.mOppoPowerManagerHelper.onDisplayStateChange(state);
                    if (this.mDisplayState != state) {
                        this.mDisplayState = state;
                        if (state != 1) {
                            if (state != 4) {
                                if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                    PowerManagerService.this.setHalAutoSuspendModeLocked(false);
                                }
                                if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                    PowerManagerService.this.setHalInteractiveModeLocked(true);
                                }
                            }
                        }
                        if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                            PowerManagerService.this.setHalInteractiveModeLocked(false);
                        }
                        if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                            PowerManagerService.this.setHalAutoSuspendModeLocked(true);
                        }
                    }
                }
                PowerManagerService powerManagerService = PowerManagerService.this;
                powerManagerService.mScreenState = state;
                if (powerManagerService.mDreamManager != null) {
                    if (PowerManagerService.this.DEBUG) {
                        Slog.i(PowerManagerService.TAG, "onDisplayStateChange state = " + state + "isDreaming = " + PowerManagerService.this.mDreamManager.isDreaming());
                    }
                    if (PowerManagerService.this.mDreamManager.isDreaming() && state == 2 && !PowerManagerService.this.mFingerprintOpticalSupport) {
                        PowerManagerService.this.mDreamManager.stopDream(false);
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
        this.mVrStateCallbacks = new IVrStateCallbacks.Stub() {
            /* class com.android.server.power.PowerManagerService.AnonymousClass4 */

            public void onVrStateChanged(boolean enabled) {
                PowerManagerService.this.powerHintInternal(7, enabled ? 1 : 0);
                synchronized (PowerManagerService.this.mLock) {
                    if (PowerManagerService.this.mIsVrModeEnabled != enabled) {
                        PowerManagerService.this.setVrModeEnabled(enabled);
                        PowerManagerService.access$1676(PowerManagerService.this, 8192);
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }
        };
        this.mContext = new ColorPowerNotifierContext(context);
        this.mBinderService = new BinderService(this.mContext, this);
        this.mLocalService = new LocalService();
        this.mNativeWrapper = injector.createNativeWrapper();
        this.mInjector = injector;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mAmbientDisplayConfiguration = this.mInjector.createAmbientDisplayConfiguration(context);
        this.mAttentionDetector = new AttentionDetector(new Runnable() {
            /* class com.android.server.power.$$Lambda$PowerManagerService$FUW_osZ9SregUE_DR9vDwaRuXo */

            public final void run() {
                PowerManagerService.this.onUserAttention();
            }
        }, this.mLock);
        this.mBatterySavingStats = new BatterySavingStats(this.mLock);
        this.mBatterySaverPolicy = this.mInjector.createBatterySaverPolicy(this.mLock, this.mContext, this.mBatterySavingStats);
        this.mBatterySaverController = new BatterySaverController(this.mLock, this.mContext, BackgroundThread.get().getLooper(), this.mBatterySaverPolicy, this.mBatterySavingStats);
        this.mBatterySaverStateMachine = new BatterySaverStateMachine(this.mLock, this.mContext, this.mBatterySaverController);
        this.mOppoPowerManagerHelper = new OppoPowerManagerHelper(this.mWakeLocks, this.mContext, this.mLock, this, this.mHandler);
        this.mPowerMonitor = new PowerMonitor(this);
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.init(this.mContext);
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        this.DEBUG = OppoBrightUtils.DEBUG;
        mScreenBrightnessSettingMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        mScreenBrightnessSettingMaximum = mOppoBrightUtils.getMaximumScreenBrightnessSetting();
        this.mScreenBrightnessSettingDefault = mOppoBrightUtils.getDefaultScreenBrightnessSetting();
        synchronized (this.mLock) {
            this.mWakeLockSuspendBlocker = this.mInjector.createSuspendBlocker(this, "PowerManagerService.WakeLocks");
            this.mDisplaySuspendBlocker = this.mInjector.createSuspendBlocker(this, "PowerManagerService.Display");
            if (this.mDisplaySuspendBlocker != null) {
                this.mDisplaySuspendBlocker.acquire();
                this.mHoldingDisplaySuspendBlocker = true;
            }
            this.mHalAutoSuspendModeEnabled = false;
            this.mHalInteractiveModeEnabled = true;
            this.mWakefulness = 1;
            sQuiescent = SystemProperties.get(SYSTEM_PROPERTY_QUIESCENT, "0").equals("1");
            this.mNativeWrapper.nativeInit(this);
            this.mNativeWrapper.nativeSetAutoSuspend(false);
            this.mNativeWrapper.nativeSetInteractive(true);
            this.mNativeWrapper.nativeSetFeature(1, 0);
        }
        this.mColorPowerMSInner = new ColorPowerManagerServiceInner();
        OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).initArgs(this.mContext, this.mLock, this, this.mHandler, this.mColorPowerMSInner);
        this.mOppoPowerFuncHelper = new OppoPowerFuncHelper(this.mContext, this.mLock, this, TAG);
        onOppoInit(this.mWakeLocks, this.mLock, this.mColorPowerMSInner);
        onOppoInit(this.mWakeLocks, this.mLock, this.mColorPowerMSInner);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.power.PowerManagerService$BinderService, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("power", new BinderService(this.mContext, this));
        publishLocalService(PowerManagerInternal.class, this.mLocalService);
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
        onOppoStart();
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            if (phase == 600) {
                try {
                    incrementBootCount();
                } catch (Throwable th) {
                    throw th;
                }
            } else if (phase == 1000) {
                long now = SystemClock.uptimeMillis();
                this.mBootCompleted = true;
                this.mDirty |= 16;
                this.mBatterySaverStateMachine.onBootCompleted();
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                updatePowerStateLocked();
                OppoFeatureCache.get(IColorSilentRebootManager.DEFAULT).postProcessBlackLightTask(this.mHandler);
                this.mOppoPowerManagerHelper.onBootPhaseCompleted();
                OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).onBootPhaseStep();
                mOppoBrightUtils.initBrightnessCallback(this.mContext);
                onBootComplete();
            }
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
            this.mAttentionDetector.systemReady(this.mContext);
            mOppoBrightUtils.initParams();
            mScreenBrightnessSettingMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
            mScreenBrightnessSettingMaximum = mOppoBrightUtils.getMaximumScreenBrightnessSetting();
            this.mScreenBrightnessSettingDefault = mOppoBrightUtils.getDefaultScreenBrightnessSetting();
            Slog.d(TAG, "mScreenBrightnessSettingMaximum = " + mScreenBrightnessSettingMaximum);
            SystemProperties.set("sys.oppo.multibrightness", Integer.toString(mScreenBrightnessSettingMaximum));
            mOppoBrightUtils.registerGHBMContent();
            mOppoBrightUtils.initWAArgsHelper();
            mScreenBrightnessSettingMaximum = ((PowerManager) this.mContext.getSystemService("power")).getMaxBrightness();
            SensorManager sensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
            this.mBatteryStats = BatteryStatsService.getService();
            this.mNotifier = this.mInjector.createNotifier(Looper.getMainLooper(), this.mContext, this.mBatteryStats, this.mInjector.createSuspendBlocker(this, "PowerManagerService.Broadcasts"), this.mPolicy);
            this.mWirelessChargerDetector = this.mInjector.createWirelessChargerDetector(sensorManager, this.mInjector.createSuspendBlocker(this, "PowerManagerService.WirelessChargerDetector"), this.mHandler);
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            this.mLightsManager = (LightsManager) getLocalService(LightsManager.class);
            this.mAttentionLight = this.mLightsManager.getLight(5);
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.button.light.auto.off")) {
                this.mOppoHelper = new OppoHelper(this.mLightsManager);
            }
            try {
                if (this.mContext.getPackageManager().hasSystemFeature("oppo.guard.elf.support")) {
                    OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).initArgs(this.mWakeLocks, this.mLock, this.mContext, this, this.mInjector.createSuspendBlocker(this, "WakeLockCheck"));
                }
            } catch (Exception e) {
                Slog.d("PowerTest", "feature got exception ");
                e.printStackTrace();
            }
            Slog.d("PowerTest", "is this running ? ");
            this.DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            this.mOppoPowerManagerHelper.onSystemReady(this.mDisplayManagerInternal);
            this.mOppoPowerManagerHelper.updateDebugSwitch(this.DEBUG_PANIC);
            this.mDisplayManagerInternal.initPowerManagement(this.mDisplayPowerCallbacks, this.mHandler, sensorManager);
            try {
                ActivityManager.getService().registerUserSwitchObserver(new ForegroundProfileObserver(), TAG);
            } catch (RemoteException e2) {
            }
            readConfigurationLocked();
            updateSettingsLocked();
            handleAodChanged();
            this.mDirty |= 256;
            updatePowerStateLocked();
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mConstants.start(resolver);
        this.mBatterySaverController.systemReady();
        this.mBatterySaverPolicy.systemReady();
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_enabled"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_activate_on_sleep"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_activate_on_dock"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("sleep_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("stay_on_while_plugged_in"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("theater_mode_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("double_tap_to_wake"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("device_demo_mode"), false, this.mSettingsObserver, 0);
        resolver.registerContentObserver(Settings.Secure.getUriFor("Setting_AodEnable"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("show_fingerprint_when_screen_off"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("coloros_fingerprint_unlock_switch"), false, this.mSettingsObserver, -1);
        IVrManager vrManager = IVrManager.Stub.asInterface(getBinderService("vrmanager"));
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e3) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e3);
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(new BatteryReceiver(), filter, null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.DREAMING_STARTED");
        filter2.addAction("android.intent.action.DREAMING_STOPPED");
        this.mContext.registerReceiver(new DreamReceiver(), filter2, null, this.mHandler);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new UserSwitchedReceiver(), filter3, null, this.mHandler);
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.intent.action.DOCK_EVENT");
        this.mContext.registerReceiver(new DockReceiver(), filter4, null, this.mHandler);
        onOppoSystemReady();
        OppoFeatureCache.get(IColorBatterySaveExtend.DEFAULT).init();
        OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).registerOppoUserPresentReceiver();
        this.mFlinger = ServiceManager.getService("SurfaceFlinger");
        if (this.mFlinger != null) {
            Slog.d(TAG, "get SurfaceFlinger Service sucess");
        } else {
            Slog.d(TAG, "get SurfaceFlinger Service failed");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void readConfigurationLocked() {
        Resources resources = this.mContext.getResources();
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = resources.getBoolean(17891495);
        this.mDecoupleHalInteractiveModeFromDisplayConfig = resources.getBoolean(17891496);
        this.mWakeUpWhenPluggedOrUnpluggedConfig = resources.getBoolean(17891554);
        this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig = resources.getBoolean(17891356);
        this.mSuspendWhenScreenOffDueToProximityConfig = resources.getBoolean(17891544);
        this.mDreamsSupportedConfig = resources.getBoolean(17891427);
        this.mDreamsEnabledByDefaultConfig = resources.getBoolean(17891425);
        this.mDreamsActivatedOnSleepByDefaultConfig = resources.getBoolean(17891424);
        this.mDreamsActivatedOnDockByDefaultConfig = resources.getBoolean(17891423);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
            Slog.d(TAG, "has feature exp.sellmode");
            this.mDreamsEnabledOnBatteryConfig = true;
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "mDreamsEnabledOnBatteryConfig =" + this.mDreamsEnabledOnBatteryConfig + " mDreamsBatteryLevelMinimumWhenNotPoweredConfig =" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            }
        } else {
            this.mDreamsEnabledOnBatteryConfig = resources.getBoolean(17891426);
        }
        this.mDreamsBatteryLevelMinimumWhenPoweredConfig = resources.getInteger(17694804);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
            Slog.d(TAG, "has feature exp.sellmode");
            this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = 20;
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "mDreamsEnabledOnBatteryConfig =" + this.mDreamsEnabledOnBatteryConfig + " mDreamsBatteryLevelMinimumWhenNotPoweredConfig =" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            }
        } else {
            this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = resources.getInteger(17694803);
        }
        if (this.mContext.getPackageManager().hasSystemFeature(FingerprintService.OPTICAL_FINGERPRINT_FEATURE)) {
            this.mFingerprintOpticalSupport = true;
            Slog.d(TAG, "mFingerprintOpticalSupport = " + this.mFingerprintOpticalSupport);
        }
        this.mDreamsBatteryLevelDrainCutoffConfig = resources.getInteger(17694802);
        this.mDozeAfterScreenOff = resources.getBoolean(17891417);
        this.mMinimumScreenOffTimeoutConfig = (long) resources.getInteger(17694844);
        this.mMaximumScreenDimDurationConfig = (long) resources.getInteger(17694839);
        this.mMaximumScreenDimRatioConfig = resources.getFraction(18022402, 1, 1);
        this.mSupportsDoubleTapWakeConfig = resources.getBoolean(17891532);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.aod.support")) {
            mOppoAodSupport = true;
            this.mDozeAfterScreenOff = true;
            this.mDecoupleHalAutoSuspendModeFromDisplayConfig = true;
            this.mDecoupleHalInteractiveModeFromDisplayConfig = true;
            Slog.d(TAG, "mOppoAodSupport = " + mOppoAodSupport);
        }
    }

    private void updateSettingsLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mDreamsEnabledSetting = Settings.Secure.getIntForUser(resolver, "screensaver_enabled", this.mDreamsEnabledByDefaultConfig ? 1 : 0, -2) != 0;
        this.mDreamsActivateOnSleepSetting = Settings.Secure.getIntForUser(resolver, "screensaver_activate_on_sleep", this.mDreamsActivatedOnSleepByDefaultConfig ? 1 : 0, -2) != 0;
        this.mDreamsActivateOnDockSetting = Settings.Secure.getIntForUser(resolver, "screensaver_activate_on_dock", this.mDreamsActivatedOnDockByDefaultConfig ? 1 : 0, -2) != 0;
        this.mScreenOffTimeoutSetting = (long) Settings.System.getIntForUser(resolver, "screen_off_timeout", 15000, -2);
        this.mSleepTimeoutSetting = (long) Settings.Secure.getIntForUser(resolver, "sleep_timeout", -1, -2);
        this.mStayOnWhilePluggedInSetting = Settings.Global.getInt(resolver, "stay_on_while_plugged_in", 1);
        this.mTheaterModeEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
        this.mAlwaysOnEnabled = this.mAmbientDisplayConfiguration.alwaysOnEnabled(-2);
        if (this.mSupportsDoubleTapWakeConfig) {
            boolean doubleTapWakeEnabled = Settings.Secure.getIntForUser(resolver, "double_tap_to_wake", 0, -2) != 0;
            if (doubleTapWakeEnabled != this.mDoubleTapWakeEnabled) {
                this.mDoubleTapWakeEnabled = doubleTapWakeEnabled;
                this.mNativeWrapper.nativeSetFeature(1, this.mDoubleTapWakeEnabled ? 1 : 0);
            }
        }
        String retailDemoValue = UserManager.isDeviceInDemoMode(this.mContext) ? "1" : "0";
        if (!retailDemoValue.equals(SystemProperties.get(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED))) {
            SystemProperties.set(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, retailDemoValue);
        }
        this.mScreenBrightnessSetting = Settings.System.getIntForBrightness(resolver, "screen_brightness", this.mScreenBrightnessSettingDefault, -2);
        this.mScreenBrightnessModeSetting = Settings.System.getIntForUser(resolver, "screen_brightness_mode", 0, -2);
        this.mDirty |= 32;
        this.mOppoPowerManagerHelper.updateSettingsLocked();
    }

    private boolean isShouldGoAod() {
        if (this.mFingerprintOpticalSupport) {
            if (this.mAodUserSetEnable == 1 || (this.mFingerprintUnlockswitch == 1 && this.mFingerprintUnlock == 1)) {
                return true;
            }
            return false;
        } else if (this.mAodUserSetEnable == 1) {
            return true;
        } else {
            return false;
        }
    }

    private void getAodSettingStatus() {
        if (isShouldGoAod()) {
            this.mDreamsEnabledSetting = true;
            this.mDecoupleHalAutoSuspendModeFromDisplayConfig = true;
            this.mDreamsActivateOnSleepSetting = true;
            this.mAlwaysOnEnabled = true;
            if (this.DEBUG) {
                Slog.i(TAG, "mDreamsEnabledSetting ==true");
                return;
            }
            return;
        }
        this.mDreamsEnabledSetting = false;
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = false;
        this.mDreamsActivateOnSleepSetting = false;
        this.mAlwaysOnEnabled = false;
        if (this.DEBUG) {
            Slog.i(TAG, "mDreamsEnabledSetting ==false");
        }
    }

    private void handleAodChanged() {
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mAodUserSetEnable = Settings.Secure.getIntForUser(resolver, "Setting_AodEnable", 0, -2);
        if (this.mAodUserSetEnable == 0) {
            this.mAodUserSetEnable = Settings.Secure.getIntForUser(resolver, "doze_always_on", 0, -2);
            if (this.mAodUserSetEnable == 1) {
                Slog.w(TAG, "mAodUserSetEnable set by Settings.Secure.DOZE_ALWAYS_ON");
            }
        }
        this.mFingerprintUnlock = Settings.Secure.getIntForUser(resolver, "show_fingerprint_when_screen_off", 0, -2);
        this.mFingerprintUnlockswitch = Settings.Secure.getIntForUser(resolver, "coloros_fingerprint_unlock_switch", 0, -2);
    }

    /* access modifiers changed from: private */
    public void handleSettingsChangedLocked() {
        handleAodChanged();
        updateSettingsLocked();
        updatePowerStateLocked();
    }

    /* access modifiers changed from: private */
    public void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        Object obj;
        int index;
        WakeLock wakeLock;
        boolean notifyAcquire;
        UidState state;
        int index2;
        StringBuilder sb;
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                if (OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).canSyncWakeLockAcq(uid, tag)) {
                    OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).allowAcquireShortimeHandle(lock, packageName, flags, ws, uid);
                    if (this.DEBUG_SPEW) {
                        try {
                            sb = new StringBuilder();
                            sb.append("acquireWakeLockInternal: lock=");
                            sb.append(Objects.hashCode(lock));
                            sb.append(", flags=0x");
                            sb.append(Integer.toHexString(flags));
                            sb.append(", tag=\"");
                            sb.append(tag);
                            sb.append("\", ws=");
                        } catch (Throwable th) {
                            ex = th;
                            obj = obj2;
                            throw ex;
                        }
                        try {
                            sb.append(ws);
                            sb.append(", uid=");
                            sb.append(uid);
                            sb.append(", pid=");
                            sb.append(pid);
                            Slog.d(TAG, sb.toString());
                        } catch (Throwable th2) {
                            ex = th2;
                            obj = obj2;
                            throw ex;
                        }
                    }
                    this.mPowerMonitor.acquireWakeLock(packageName, tag, flags);
                    int index3 = findWakeLockIndexLocked(lock);
                    if (index3 >= 0) {
                        try {
                            wakeLock = this.mWakeLocks.get(index3);
                            if (!wakeLock.hasSameProperties(flags, tag, ws, uid, pid)) {
                                index2 = index3;
                                obj = obj2;
                                notifyWakeLockChangingLocked(wakeLock, flags, tag, packageName, uid, pid, ws, historyTag);
                                wakeLock.updateProperties(flags, tag, packageName, ws, historyTag, uid, pid);
                            } else {
                                index2 = index3;
                                obj = obj2;
                            }
                            notifyAcquire = false;
                            index = uid;
                        } catch (Throwable th3) {
                            ex = th3;
                            throw ex;
                        }
                    } else {
                        obj = obj2;
                        UidState state2 = this.mUidState.get(uid);
                        if (state2 == null) {
                            UidState state3 = new UidState(uid);
                            state3.mProcState = 21;
                            this.mUidState.put(uid, state3);
                            state = state3;
                        } else {
                            state = state2;
                        }
                        state.mNumWakeLocks++;
                        index = uid;
                        try {
                            WakeLock wakeLock2 = new WakeLock(lock, flags, tag, packageName, ws, historyTag, uid, pid, state);
                            try {
                                lock.linkToDeath(wakeLock2, 0);
                                this.mWakeLocks.add(wakeLock2);
                                setWakeLockDisabledStateLocked(wakeLock2);
                                notifyAcquire = true;
                                wakeLock2.mActiveSince = SystemClock.uptimeMillis();
                                wakeLock = wakeLock2;
                            } catch (RemoteException e) {
                                throw new IllegalArgumentException("Wake lock is already dead.");
                            } catch (Throwable th4) {
                                ex = th4;
                                throw ex;
                            }
                        } catch (Throwable th5) {
                            ex = th5;
                            throw ex;
                        }
                    }
                    applyWakeLockFlagsOnAcquireLocked(wakeLock, index);
                    this.mDirty |= 1;
                    updatePowerStateLocked();
                    if (notifyAcquire) {
                        notifyWakeLockAcquiredLocked(wakeLock);
                    }
                }
            } catch (Throwable th6) {
                ex = th6;
                obj = obj2;
                throw ex;
            }
        }
    }

    private static boolean isScreenLock(WakeLock wakeLock) {
        int i = wakeLock.mFlags & 65535;
        if (i == 6 || i == 10 || i == 26) {
            return true;
        }
        return false;
    }

    private static WorkSource.WorkChain getFirstNonEmptyWorkChain(WorkSource workSource) {
        if (workSource.getWorkChains() == null) {
            return null;
        }
        Iterator it = workSource.getWorkChains().iterator();
        while (it.hasNext()) {
            WorkSource.WorkChain workChain = (WorkSource.WorkChain) it.next();
            if (workChain.getSize() > 0) {
                return workChain;
            }
        }
        return null;
    }

    private void applyWakeLockFlagsOnAcquireLocked(WakeLock wakeLock, int uid) {
        String opPackageName;
        int opUid;
        if ((wakeLock.mFlags & 268435456) != 0 && isScreenLock(wakeLock)) {
            if (this.mOppoPowerManagerHelper.isOppoProximityPositiveSuspend()) {
                if (this.DEBUG_PANIC) {
                    Slog.i(TAG, "wakeLock : " + wakeLock.mTag + ", lock = " + Objects.hashCode(wakeLock.mLock) + " try to wakeup device while proximity positive");
                }
                userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, uid);
                return;
            }
            if (wakeLock.mWorkSource == null || wakeLock.mWorkSource.isEmpty()) {
                opPackageName = wakeLock.mPackageName;
                opUid = wakeLock.mOwnerUid;
            } else {
                WorkSource workSource = wakeLock.mWorkSource;
                WorkSource.WorkChain workChain = getFirstNonEmptyWorkChain(workSource);
                if (workChain != null) {
                    opPackageName = workChain.getAttributionTag();
                    opUid = workChain.getAttributionUid();
                } else {
                    String opPackageName2 = workSource.getName(0) != null ? workSource.getName(0) : wakeLock.mPackageName;
                    opUid = workSource.get(0);
                    opPackageName = opPackageName2;
                }
            }
            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), 2, wakeLock.mTag, opUid, opPackageName, opUid);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        return;
     */
    public void releaseWakeLockInternal(IBinder lock, int flags) {
        synchronized (this.mLock) {
            int index = findWakeLockIndexLocked(lock);
            if (index >= 0) {
                WakeLock wakeLock = this.mWakeLocks.get(index);
                wakeLock.mTotalTime = SystemClock.uptimeMillis() - wakeLock.mActiveSince;
                if (this.DEBUG_SPEW) {
                    Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.toString() + "], flags=0x" + Integer.toHexString(flags) + ", total_time=" + wakeLock.mTotalTime + "ms");
                }
                if (this.mOppoPowerManagerHelper.isCPULock(wakeLock) && !wakeLock.mDisabled && !isInteractiveInternal()) {
                    this.mPowerMonitor.releaseWakeLock(wakeLock.mPackageName, wakeLock.mTag, wakeLock.mTotalTime);
                }
                if ((flags & 1) != 0) {
                    this.mRequestWaitForNegativeProximity = true;
                }
                wakeLock.mLock.unlinkToDeath(wakeLock, 0);
                removeWakeLockLocked(wakeLock, index);
            } else if (this.DEBUG_PANIC || this.DEBUG_SPEW) {
                Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWakeLockDeath(WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            int index = this.mWakeLocks.indexOf(wakeLock);
            if (index >= 0) {
                removeWakeLockLocked(wakeLock, index);
            }
        }
    }

    private void removeWakeLockLocked(WakeLock wakeLock, int index) {
        this.mWakeLocks.remove(index);
        UidState state = wakeLock.mUidState;
        state.mNumWakeLocks--;
        if (state.mNumWakeLocks <= 0 && state.mProcState == 21) {
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

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0093, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0034, code lost:
        return;
     */
    public void updateWakeLockWorkSourceInternal(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        synchronized (this.mLock) {
            try {
                int index = findWakeLockIndexLocked(lock);
                if (index >= 0) {
                    WakeLock wakeLock = this.mWakeLocks.get(index);
                    if (this.DEBUG_SPEW) {
                        Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws);
                    }
                    if (!wakeLock.hasSameWorkSource(ws)) {
                        notifyWakeLockChangingLocked(wakeLock, wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, ws, historyTag);
                        wakeLock.mHistoryTag = historyTag;
                        wakeLock.updateWorkSource(ws);
                    }
                } else if (this.DEBUG_SPEW) {
                    Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [not found], ws=" + ws);
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private int findWakeLockIndexLocked(IBinder lock) {
        int count = this.mWakeLocks.size();
        for (int i = 0; i < count; i++) {
            if (this.mWakeLocks.get(i).mLock == lock) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
            wakeLock.mNotifiedAcquired = true;
            this.mNotifier.onWakeLockAcquired(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            restartNofifyLongTimerLocked(wakeLock);
            OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).noteWakeLockChange(wakeLock, true);
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
            OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).noteWorkSourceChange(wakeLock, ws);
            notifyWakeLockLongFinishedLocked(wakeLock);
            restartNofifyLongTimerLocked(wakeLock);
        }
    }

    /* access modifiers changed from: private */
    public void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            wakeLock.mNotifiedAcquired = false;
            wakeLock.mAcquireTime = 0;
            this.mNotifier.onWakeLockReleased(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).noteWakeLockChange(wakeLock, false);
            notifyWakeLockLongFinishedLocked(wakeLock);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0031, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0033, code lost:
        return true;
     */
    public boolean isWakeLockLevelSupportedInternal(int level) {
        synchronized (this.mLock) {
            boolean z = true;
            if (!(level == 1 || level == 6 || level == 10 || level == 26)) {
                if (level != 32) {
                    if (!(level == 64 || level == 128)) {
                        try {
                            return false;
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                } else if (!this.mSystemReady || !this.mDisplayManagerInternal.isProximitySensorAvailable()) {
                    z = false;
                }
            }
        }
    }

    private void userActivityFromNative(long eventTime, int event, int flags) {
        userActivityInternal(eventTime, event, flags, 1000);
    }

    /* access modifiers changed from: private */
    public void userActivityInternal(long eventTime, int event, int flags, int uid) {
        synchronized (this.mLock) {
            if (userActivityNoUpdateLocked(eventTime, event, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserAttention() {
        synchronized (this.mLock) {
            if (userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 4, 0, 1000)) {
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean userActivityNoUpdateLocked(long eventTime, int event, int flags, int uid) {
        if (this.DEBUG_SPEW) {
            Slog.d(TAG, "userActivityNoUpdateLocked: eventTime=" + eventTime + ", event=" + event + ", flags=0x" + Integer.toHexString(flags) + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || eventTime < this.mLastWakeTime || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        if (isInteractiveInternal() && (event == 2 || event == 0)) {
            this.mOppoPowerFuncHelper.mCntUserActivity.getAndIncrement();
        }
        Trace.traceBegin(131072, HealthState.USERACTIVITY);
        try {
            if (eventTime > this.mLastInteractivePowerHintTime) {
                powerHintInternal(2, 0);
                this.mLastInteractivePowerHintTime = eventTime;
            }
            this.mNotifier.onUserActivity(event, uid);
            this.mAttentionDetector.onUserActivity(eventTime, event);
            if (this.mUserInactiveOverrideFromWindowManager) {
                this.mUserInactiveOverrideFromWindowManager = false;
                this.mOverriddenTimeout = -1;
            }
            this.mOppoPowerManagerHelper.onUserActivityNoUpdateLocked(this.mWakefulness);
            if (!(this.mWakefulness == 0 || this.mWakefulness == 3)) {
                if ((flags & 2) == 0) {
                    maybeUpdateForegroundProfileLastActivityLocked(eventTime);
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
                }
            }
            return false;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    /* access modifiers changed from: private */
    public void maybeUpdateForegroundProfileLastActivityLocked(long eventTime) {
        ProfilePowerState profile = this.mProfilePowerState.get(this.mForegroundProfile);
        if (profile != null && eventTime > profile.mLastUserActivityTime) {
            profile.mLastUserActivityTime = eventTime;
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpInternal(long eventTime, int reason, String details, int uid, String opPackageName, int opUid) {
        int reason2;
        String details2 = this.mOppoPowerManagerHelper.handleWakeUpReasonEarly(details, uid, opPackageName, opUid);
        if ("oppo.wakeup.systemui:clean up".equals(details2)) {
            reason2 = 103;
        } else {
            reason2 = reason;
        }
        synchronized (this.mLock) {
            if (wakeUpNoUpdateLocked(eventTime, reason2, details2, uid, opPackageName, opUid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean wakeUpNoUpdateLocked(long eventTime, int reason, String details, int reasonUid, String opPackageName, int opUid) {
        long j;
        boolean z;
        if (this.DEBUG_PANIC) {
            Slog.d(TAG, "wakeUpNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", uid=" + reasonUid + ", opPackageName=" + opPackageName);
        }
        if (this.DEBUG_SPEW) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, "PowerMS    |----" + element.toString());
            }
        }
        if (this.mOppoPowerManagerHelper.oppoIntercepetWakeUpMeantimeLocked(eventTime, details, reasonUid, opPackageName, opUid) || eventTime < this.mLastSleepTime || this.mWakefulness == 1 || !this.mBootCompleted || !this.mSystemReady || this.mForceSuspendActive) {
            return false;
        }
        this.mOppoPowerManagerHelper.onWakeUpNoUpdatedLockedBegin(eventTime, details, reasonUid, opPackageName, opUid);
        Trace.asyncTraceBegin(131072, TRACE_SCREEN_ON, 0);
        Trace.traceBegin(131072, "wakeUp");
        try {
            Slog.i(TAG, "Waking up from " + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + " (uid=" + reasonUid + ", reason=" + PowerManager.wakeReasonToString(reason) + ", details=" + details + ")...");
            this.mLastWakeTime = eventTime;
            this.mLastWakeReason = reason;
            this.mPowerMonitor.screenOnLocked();
            if ("android.service.fingerprint:WAKEUP".equals(Integer.valueOf(reason))) {
                z = true;
                setWakefulnessLocked(1, 98, eventTime);
            } else {
                z = true;
                setWakefulnessLocked(1, reason, eventTime);
            }
            this.mOppoPowerManagerHelper.stopTrackWakelocks();
            j = 131072;
            try {
                this.mNotifier.onWakeUp(reason, details, reasonUid, opPackageName, opUid);
                userActivityNoUpdateLocked(eventTime, 0, 0, reasonUid);
                Trace.traceEnd(131072);
                OppoHelper oppoHelper = this.mOppoHelper;
                if (oppoHelper != null) {
                    oppoHelper.updateButtonBrightness(this, false);
                }
                OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).PartialWakelockCheckStop();
                OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).readySendScreenOffTimeOffMessage();
                this.mOppoPowerFuncHelper.mCntUserActivity.set(0);
                this.mOppoPowerFuncHelper.mKeyguardLockEverUnlock.set(false);
                this.mOppoPowerFuncHelper.mWakeupReason = PowerManager.wakeReasonToString(reason);
                return z;
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(j);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            j = 131072;
            Trace.traceEnd(j);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
        if (this.mOppoPowerManagerHelper.oppoIntercepetGoToSleepEarly(eventTime, reason, flags, uid)) {
            Slog.d(TAG, "needIntercepet set true by fingerprint reason=" + reason);
            return;
        }
        synchronized (this.mLock) {
            if (goToSleepNoUpdateLocked(eventTime, reason, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean goToSleepNoUpdateLocked(long eventTime, int reason, int flags, int uid) {
        int i;
        if (this.DEBUG_SPEW) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Slog.d(TAG, "PowerMS    |----" + element.toString());
            }
        }
        if (this.DEBUG_PANIC || this.DEBUG_SPEW) {
            Slog.d(TAG, "goToSleepNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", flags=" + flags + ", uid=" + uid);
        }
        if (reason == 8) {
            this.mDirty |= 32;
            this.mShutdownFlag = true;
            Slog.d(TAG, "go to sleep due to quick shutdown");
            return true;
        } else if (eventTime < this.mLastWakeTime || (i = this.mWakefulness) == 0 || i == 3 || !this.mBootCompleted || !this.mSystemReady || this.mOppoPowerManagerHelper.oppoIntercepetGoToSleepMeantimeLocked(eventTime, reason, flags, uid)) {
            return false;
        } else {
            this.mOppoPowerManagerHelper.onGoToSleepNoUpdateLockedBegin(eventTime, reason, flags, uid);
            Trace.traceBegin(131072, "goToSleep");
            try {
                int reason2 = Math.min(11, Math.max(reason, 0));
                try {
                    Slog.i(TAG, "Going to sleep due to " + PowerManager.sleepReasonToString(reason2) + " (uid " + uid + ")...");
                    this.mOppoPowerManagerHelper.recordScreenOffReason(reason2);
                    this.mLastSleepTime = eventTime;
                    this.mLastSleepReason = reason2;
                    this.mSandmanSummoned = true;
                    setWakefulnessLocked(3, reason2, eventTime);
                    int numWakeLocksCleared = 0;
                    int numWakeLocks = this.mWakeLocks.size();
                    for (int i2 = 0; i2 < numWakeLocks; i2++) {
                        int i3 = this.mWakeLocks.get(i2).mFlags & 65535;
                        if (i3 == 6 || i3 == 10 || i3 == 26) {
                            numWakeLocksCleared++;
                        }
                    }
                    EventLogTags.writePowerSleepRequested(numWakeLocksCleared);
                    this.mOppoPowerManagerHelper.dumpWakeLockLocked();
                    this.mOppoPowerManagerHelper.tryToTrackWakelocks();
                    if ((flags & 1) != 0 || this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                        reallyGoToSleepNoUpdateLocked(eventTime, uid);
                    }
                    Trace.traceEnd(131072);
                    OppoHelper oppoHelper = this.mOppoHelper;
                    if (oppoHelper != null) {
                        oppoHelper.turnOffButtonLight();
                    }
                    OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).PartialWakelockCheckStart();
                    OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).removeScreenOffTimeOutMessage();
                    this.mOppoPowerFuncHelper.mSleepReason = reason2;
                    this.mPowerMonitor.clear();
                    this.mPowerMonitor.screenOffLocked();
                    return true;
                } catch (Throwable th) {
                    th = th;
                    Trace.traceEnd(131072);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(131072);
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void napInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (napNoUpdateLocked(eventTime, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean napNoUpdateLocked(long eventTime, int uid) {
        if (this.DEBUG_SPEW) {
            Slog.d(TAG, "napNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness != 1 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(131072, "nap");
        try {
            Slog.i(TAG, "Nap time (uid " + uid + ")...");
            this.mSandmanSummoned = true;
            setWakefulnessLocked(2, 0, eventTime);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean reallyGoToSleepNoUpdateLocked(long eventTime, int uid) {
        if (this.DEBUG_PANIC || this.DEBUG_SPEW) {
            Slog.d(TAG, "reallyGoToSleepNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(131072, "reallyGoToSleep");
        try {
            Slog.i(TAG, "Sleeping (uid " + uid + ")...");
            setWakefulnessLocked(0, 2, eventTime);
            Trace.traceEnd(131072);
            return true;
        } catch (Throwable th) {
            Trace.traceEnd(131072);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setWakefulnessLocked(int wakefulness, int reason, long eventTime) {
        if (this.mWakefulness != wakefulness) {
            this.mWakefulness = wakefulness;
            this.mWakefulnessChanging = true;
            this.mDirty |= 2;
            Notifier notifier = this.mNotifier;
            if (notifier != null) {
                notifier.onWakefulnessChangeStarted(wakefulness, reason, eventTime);
            }
            this.mOppoPowerManagerHelper.onWakefulnessChangeStarted(wakefulness, reason, eventTime);
            PowerMonitor powerMonitor = this.mPowerMonitor;
            if (powerMonitor != null) {
                powerMonitor.onWakeFullnessChanged(wakefulness);
            }
            this.mAttentionDetector.onWakefulnessChangeStarted(wakefulness);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getWakefulness() {
        return this.mWakefulness;
    }

    private void logSleepTimeoutRecapturedLocked() {
        long savedWakeTimeMs = this.mOverriddenTimeout - SystemClock.uptimeMillis();
        if (savedWakeTimeMs >= 0) {
            EventLogTags.writePowerSoftSleepRequested(savedWakeTimeMs);
            this.mOverriddenTimeout = -1;
        }
    }

    private void notifySfUnBlockScreenOn() {
        if (this.DEBUG_PANIC) {
            Slog.d(TAG, "notifySfUnBlockScreenOn");
        }
        if (this.mFlinger == null) {
            Slog.d(TAG, "notifySfUnBlockScreenOn, mFlinger is null ");
            this.mFlinger = ServiceManager.getService("SurfaceFlinger");
        }
        try {
            if (this.mFlinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                this.mFlinger.transact(20003, data, null, 1);
                data.recycle();
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "get SurfaceFlinger Service failed");
        }
    }

    private void finishWakefulnessChangeIfNeededLocked() {
        if (this.mWakefulnessChanging && this.mDisplayReady) {
            if (this.mWakefulness != 3 || (this.mWakeLockSummary & 64) != 0) {
                int i = this.mWakefulness;
                if (i == 3 || i == 0) {
                    logSleepTimeoutRecapturedLocked();
                }
                if (this.mWakefulness == 1) {
                    Trace.asyncTraceEnd(131072, TRACE_SCREEN_ON, 0);
                    int latencyMs = (int) (SystemClock.uptimeMillis() - this.mLastWakeTime);
                    if (latencyMs >= 100) {
                        Slog.w(TAG, "Screen on took " + latencyMs + " ms");
                        StringBuilder sb = new StringBuilder();
                        sb.append("ScreenOnTook ");
                        sb.append(latencyMs);
                        Slog.p("Quality", sb.toString());
                    }
                    notifySfUnBlockScreenOn();
                    OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessNoAnimation = false;
                }
                this.mWakefulnessChanging = false;
                this.mOppoPowerManagerHelper.onWakefulnessChangeFinished(this.mWakefulness);
                this.mNotifier.onWakefulnessChangeFinished();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePowerStateLocked() {
        int dirtyPhase1;
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
                do {
                    dirtyPhase1 = this.mDirty;
                    dirtyPhase2 |= dirtyPhase1;
                    this.mDirty = 0;
                    updateWakeLockSummaryLocked(dirtyPhase1);
                    updateUserActivitySummaryLocked(now, dirtyPhase1);
                } while (updateWakefulnessLocked(dirtyPhase1));
                updateProfilesLocked(now);
                updateDreamLocked(dirtyPhase2, updateDisplayPowerStateLocked(dirtyPhase2));
                finishWakefulnessChangeIfNeededLocked();
                updateSuspendBlockerLocked();
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    private void updateProfilesLocked(long now) {
        int numProfiles = this.mProfilePowerState.size();
        for (int i = 0; i < numProfiles; i++) {
            ProfilePowerState profile = this.mProfilePowerState.valueAt(i);
            if (isProfileBeingKeptAwakeLocked(profile, now)) {
                profile.mLockingNotified = false;
            } else if (!profile.mLockingNotified) {
                profile.mLockingNotified = true;
                this.mNotifier.onProfileTimeout(profile.mUserId);
            }
        }
    }

    private boolean isProfileBeingKeptAwakeLocked(ProfilePowerState profile, long now) {
        return profile.mLastUserActivityTime + profile.mScreenOffTimeout > now || (profile.mWakeLockSummary & 32) != 0 || (this.mProximityPositive && (profile.mWakeLockSummary & 16) != 0);
    }

    private void updateIsPoweredLocked(int dirty) {
        if ((dirty & 256) != 0) {
            boolean wasPowered = this.mIsPowered;
            int oldPlugType = this.mPlugType;
            boolean z = this.mBatteryLevelLow;
            this.mIsPowered = this.mBatteryManagerInternal.isPowered(7);
            this.mPlugType = this.mBatteryManagerInternal.getPlugType();
            this.mBatteryLevel = this.mBatteryManagerInternal.getBatteryLevel();
            this.mBatteryLevelLow = this.mBatteryManagerInternal.getBatteryLevelLow();
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "updateIsPoweredLocked: wasPowered=" + wasPowered + ", mIsPowered=" + this.mIsPowered + ", oldPlugType=" + oldPlugType + ", mPlugType=" + this.mPlugType + ", mBatteryLevel=" + this.mBatteryLevel);
            }
            if (!(wasPowered == this.mIsPowered && oldPlugType == this.mPlugType)) {
                this.mDirty |= 64;
                boolean dockedOnWirelessCharger = this.mWirelessChargerDetector.update(this.mIsPowered, this.mPlugType);
                long now = SystemClock.uptimeMillis();
                if (shouldWakeUpWhenPluggedOrUnpluggedLocked(wasPowered, oldPlugType, dockedOnWirelessCharger)) {
                    wakeUpNoUpdateLocked(now, 3, "android.server.power:PLUGGED:" + this.mIsPowered, 1000, this.mContext.getOpPackageName(), 1000);
                }
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                if (this.mBootCompleted) {
                    if (this.mIsPowered && !BatteryManager.isPlugWired(oldPlugType) && BatteryManager.isPlugWired(this.mPlugType)) {
                        this.mNotifier.onWiredChargingStarted(this.mForegroundProfile);
                    } else if (dockedOnWirelessCharger) {
                        this.mNotifier.onWirelessChargingStarted(this.mBatteryLevel, this.mForegroundProfile);
                    }
                }
            }
            boolean dockedOnWirelessCharger2 = this.mIsPowered;
            if (wasPowered != dockedOnWirelessCharger2) {
                OppoPowerManagerHelper oppoPowerManagerHelper = this.mOppoPowerManagerHelper;
                int i = this.mBatteryLevel;
                oppoPowerManagerHelper.onBatteryStatusChanged(dockedOnWirelessCharger2, i, i);
            }
            this.mBatterySaverStateMachine.setBatteryStatus(this.mIsPowered, this.mBatteryLevel, this.mBatteryLevelLow);
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
            this.mOppoPowerManagerHelper.setProximityLockFromInCallUiValueLocked(false);
            int numProfiles = this.mProfilePowerState.size();
            for (int i = 0; i < numProfiles; i++) {
                this.mProfilePowerState.valueAt(i).mWakeLockSummary = 0;
            }
            int numWakeLocks = this.mWakeLocks.size();
            for (int i2 = 0; i2 < numWakeLocks; i2++) {
                WakeLock wakeLock = this.mWakeLocks.get(i2);
                int wakeLockFlags = getWakeLockSummaryFlags(wakeLock);
                this.mWakeLockSummary |= wakeLockFlags;
                for (int j = 0; j < numProfiles; j++) {
                    ProfilePowerState profile = this.mProfilePowerState.valueAt(j);
                    if (wakeLockAffectsUser(wakeLock, profile.mUserId)) {
                        profile.mWakeLockSummary |= wakeLockFlags;
                    }
                }
            }
            this.mWakeLockSummary = adjustWakeLockSummaryLocked(this.mWakeLockSummary);
            for (int i3 = 0; i3 < numProfiles; i3++) {
                ProfilePowerState profile2 = this.mProfilePowerState.valueAt(i3);
                profile2.mWakeLockSummary = adjustWakeLockSummaryLocked(profile2.mWakeLockSummary);
            }
            if ((this.mWakeLockSummary & 16) == 0) {
                this.mProximityPositive = false;
            }
            this.mOppoPowerManagerHelper.setLastWakeLockSummary(this.mWakeLockSummary);
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "updateWakeLockSummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            }
        }
    }

    private int adjustWakeLockSummaryLocked(int wakeLockSummary) {
        if (this.mWakefulness != 3) {
            wakeLockSummary &= -193;
        }
        if (this.mWakefulness == 0 || (wakeLockSummary & 64) != 0) {
            wakeLockSummary &= -15;
            if (this.mWakefulness == 0) {
                wakeLockSummary &= -17;
            }
        }
        if (this.mOppoPowerManagerHelper.getProximityLockFromInCallUiValueLocked()) {
            wakeLockSummary |= 16;
        }
        if ((wakeLockSummary & 6) != 0) {
            int i = this.mWakefulness;
            if (i == 1) {
                wakeLockSummary |= 33;
            } else if (i == 2) {
                wakeLockSummary |= 1;
            }
        }
        if ((wakeLockSummary & 128) != 0) {
            return wakeLockSummary | 1;
        }
        return wakeLockSummary;
    }

    private int getWakeLockSummaryFlags(WakeLock wakeLock) {
        int i = wakeLock.mFlags & 65535;
        if (i != 1) {
            if (i == 6) {
                return 4;
            }
            if (i == 10) {
                return 2;
            }
            if (i == 26) {
                return 10;
            }
            if (i == 32) {
                boolean googledialer = SystemProperties.get("sys.oppo.proximity.googledialer", "0").equals("1");
                if (!wakeLock.mPackageName.equals("com.android.incallui") && !wakeLock.mTag.equals("MotorManagerService") && !googledialer && !wakeLock.mPackageName.equals("com.google.android.dialer")) {
                    return 16;
                }
                this.mOppoPowerManagerHelper.setProximityLockFromInCallUiValueLocked(true);
                return 16;
            } else if (i == 64) {
                return 64;
            } else {
                if (i != 128) {
                    return 0;
                }
                return 128;
            }
        } else if (!wakeLock.mDisabled) {
            return 1;
        } else {
            return 0;
        }
    }

    private boolean wakeLockAffectsUser(WakeLock wakeLock, int userId) {
        if (wakeLock.mWorkSource != null) {
            for (int k = 0; k < wakeLock.mWorkSource.size(); k++) {
                if (userId == UserHandle.getUserId(wakeLock.mWorkSource.get(k))) {
                    return true;
                }
            }
            ArrayList<WorkSource.WorkChain> workChains = wakeLock.mWorkSource.getWorkChains();
            if (workChains != null) {
                for (int k2 = 0; k2 < workChains.size(); k2++) {
                    if (userId == UserHandle.getUserId(workChains.get(k2).getAttributionUid())) {
                        return true;
                    }
                }
            }
        }
        if (userId == UserHandle.getUserId(wakeLock.mOwnerUid)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void checkForLongWakeLocks() {
        synchronized (this.mLock) {
            long now = SystemClock.uptimeMillis();
            this.mNotifyLongDispatched = now;
            long when = now - 60000;
            long nextCheckTime = JobStatus.NO_LATEST_RUNTIME;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = this.mWakeLocks.get(i);
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
            int i = this.mWakefulness;
            if (i == 1 || i == 2 || i == 3) {
                long sleepTimeout = getSleepTimeoutLocked();
                long screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
                long screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
                boolean userInactiveOverride = this.mUserInactiveOverrideFromWindowManager;
                long nextProfileTimeout = getNextProfileTimeoutLocked(now);
                this.mUserActivitySummary = 0;
                long j = this.mLastUserActivityTime;
                if (j >= this.mLastWakeTime) {
                    nextTimeout = (j + screenOffTimeout) - screenDimDuration;
                    if (now < nextTimeout) {
                        this.mUserActivitySummary = 1;
                    } else {
                        nextTimeout = j + screenOffTimeout;
                        if (now < nextTimeout) {
                            this.mUserActivitySummary = 2;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0) {
                    long j2 = this.mLastUserActivityTimeNoChangeLights;
                    if (j2 >= this.mLastWakeTime) {
                        nextTimeout = j2 + screenOffTimeout;
                        if (now < nextTimeout) {
                            if (this.mDisplayPowerRequest.policy == 3 || this.mDisplayPowerRequest.policy == 4) {
                                this.mUserActivitySummary = 1;
                            } else if (this.mDisplayPowerRequest.policy == 2) {
                                this.mUserActivitySummary = 2;
                            }
                        }
                    }
                }
                if (this.mUserActivitySummary == 0) {
                    if (sleepTimeout >= 0) {
                        long anyUserActivity = Math.max(this.mLastUserActivityTime, this.mLastUserActivityTimeNoChangeLights);
                        if (anyUserActivity >= this.mLastWakeTime) {
                            long nextTimeout2 = anyUserActivity + sleepTimeout;
                            if (now < nextTimeout2) {
                                this.mUserActivitySummary = 4;
                            }
                            nextTimeout = nextTimeout2;
                        }
                    } else {
                        this.mUserActivitySummary = 4;
                        nextTimeout = -1;
                    }
                }
                int i2 = this.mUserActivitySummary;
                if (i2 != 4 && userInactiveOverride) {
                    if ((3 & i2) != 0 && nextTimeout >= now && this.mOverriddenTimeout == -1) {
                        this.mOverriddenTimeout = nextTimeout;
                    }
                    this.mUserActivitySummary = 4;
                    nextTimeout = -1;
                }
                if ((this.mUserActivitySummary & 1) != 0 && (this.mWakeLockSummary & 32) == 0) {
                    nextTimeout = this.mAttentionDetector.updateUserActivity(nextTimeout);
                }
                if (nextProfileTimeout > 0) {
                    nextTimeout = Math.min(nextTimeout, nextProfileTimeout);
                }
                if (this.mUserActivitySummary != 0 && nextTimeout >= 0) {
                    scheduleUserInactivityTimeout(nextTimeout);
                }
            } else {
                this.mUserActivitySummary = 0;
            }
            if (this.mOppoHelper != null && ((!this.mProximityPositive || (this.mWakeLockSummary & 16) == 0) && (dirty & 1) == 0 && nextTimeout >= 0)) {
                this.mOppoHelper.updateButtonBrightness(this, true);
            }
            if (needScreenOnWakelockCheck()) {
                OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).screenOnWakelockCheckStart();
            } else {
                OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).screenOnWakelockCheckStop();
            }
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "updateUserActivitySummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", nextTimeout=" + TimeUtils.formatUptime(nextTimeout));
            }
        }
        OppoHelper oppoHelper = this.mOppoHelper;
        if (oppoHelper != null && (dirty & 512) != 0 && this.mProximityPositive) {
            oppoHelper.turnOffButtonLight();
        }
    }

    private void scheduleUserInactivityTimeout(long timeMs) {
        Message msg = this.mHandler.obtainMessage(1);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, timeMs);
    }

    private long getNextProfileTimeoutLocked(long now) {
        long nextTimeout = -1;
        int numProfiles = this.mProfilePowerState.size();
        for (int i = 0; i < numProfiles; i++) {
            ProfilePowerState profile = this.mProfilePowerState.valueAt(i);
            long timeout = profile.mLastUserActivityTime + profile.mScreenOffTimeout;
            if (timeout > now && (nextTimeout == -1 || timeout < nextTimeout)) {
                nextTimeout = timeout;
            }
        }
        return nextTimeout;
    }

    /* access modifiers changed from: private */
    public void handleUserActivityTimeout() {
        synchronized (this.mLock) {
            if (this.DEBUG_PANIC || this.DEBUG_SPEW) {
                Slog.d(TAG, "handleUserActivityTimeout");
            }
            this.mDirty |= 4;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: private */
    public long getSleepTimeoutLocked() {
        long timeout = this.mSleepTimeoutSetting;
        if (timeout <= 0) {
            return -1;
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    /* access modifiers changed from: private */
    public long getScreenOffTimeoutLocked(long sleepTimeout) {
        long timeout = this.mScreenOffTimeoutSetting;
        if (isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
            timeout = Math.min(timeout, this.mMaximumScreenOffTimeoutFromDeviceAdmin);
        }
        long j = this.mUserActivityTimeoutOverrideFromWindowManager;
        if (j >= 0) {
            timeout = Math.min(timeout, j);
        }
        if (sleepTimeout >= 0) {
            timeout = Math.min(timeout, sleepTimeout);
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private long getScreenDimDurationLocked(long screenOffTimeout) {
        return Math.min(this.mMaximumScreenDimDurationConfig, (long) (((float) screenOffTimeout) * this.mMaximumScreenDimRatioConfig));
    }

    private boolean updateWakefulnessLocked(int dirty) {
        if ((dirty & 1687) == 0 || this.mWakefulness != 1 || !isItBedTimeYetLocked()) {
            return false;
        }
        if (this.DEBUG_PANIC || this.DEBUG_SPEW) {
            Slog.d(TAG, "updateWakefulnessLocked: Bed time...");
        }
        long time = SystemClock.uptimeMillis();
        if (shouldNapAtBedTimeLocked()) {
            return napNoUpdateLocked(time, 1000);
        }
        return goToSleepNoUpdateLocked(time, 2, 0, 1000);
    }

    private boolean shouldNapAtBedTimeLocked() {
        return this.mDreamsActivateOnSleepSetting || (this.mDreamsActivateOnDockSetting && this.mDockState != 0);
    }

    private boolean isItBedTimeYetLocked() {
        return this.mBootCompleted && !isBeingKeptAwakeLocked();
    }

    private boolean isBeingKeptAwakeLocked() {
        return this.mStayOn || (this.mProximityPositive && !this.mOppoPowerManagerHelper.getUseProximityForceSuspendValueLocked()) || (this.mWakeLockSummary & 32) != 0 || (this.mUserActivitySummary & 3) != 0 || this.mScreenBrightnessBoostInProgress;
    }

    private void updateDreamLocked(int dirty, boolean displayBecameReady) {
        if (((dirty & OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_RECOVERY_INFO) != 0 || displayBecameReady) && this.mDisplayReady) {
            scheduleSandmanLocked();
        }
    }

    /* access modifiers changed from: private */
    public void scheduleSandmanLocked() {
        if (!this.mSandmanScheduled) {
            this.mSandmanScheduled = true;
            Message msg = this.mHandler.obtainMessage(2);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00db, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x012e, code lost:
        if (r4 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0132, code lost:
        if (r15.mFingerprintOpticalSupport == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x013a, code lost:
        if (r15.mOppoPowerManagerHelper.hasBlockedByFingerprint() != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x013c, code lost:
        r15.mDreamManager.stopDream(false);
        r0 = r15.mMapLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0143, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:?, code lost:
        r15.mDozeStateMap.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0149, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0150, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        return;
     */
    public void handleSandman() {
        int wakefulness;
        boolean z;
        boolean startDreaming;
        boolean isDreaming;
        synchronized (this.mLock) {
            this.mSandmanScheduled = false;
            wakefulness = this.mWakefulness;
            z = true;
            if (!this.mSandmanSummoned || !this.mDisplayReady) {
                startDreaming = false;
            } else {
                startDreaming = (canDreamLocked() || canDozeLocked()) && isShouldGoAod();
                this.mSandmanSummoned = false;
            }
        }
        if (this.mDreamManager != null) {
            if (startDreaming) {
                if (this.mHandler.hasMessages(102)) {
                    Slog.d(TAG, "removeMessages MSG_STOP_DEAM ");
                    this.mHandler.removeMessages(102);
                    this.mDreamManager.stopDream(false);
                    synchronized (this.mMapLock) {
                        this.mDozeStateMap.clear();
                    }
                }
                DreamManagerInternal dreamManagerInternal = this.mDreamManager;
                if (wakefulness != 3) {
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
            if (!this.mSandmanSummoned) {
                if (this.mWakefulness == wakefulness) {
                    if (wakefulness == 2) {
                        if (isDreaming && canDreamLocked()) {
                            if (this.mDreamsBatteryLevelDrainCutoffConfig >= 0 && this.mBatteryLevel < this.mBatteryLevelWhenDreamStarted - this.mDreamsBatteryLevelDrainCutoffConfig && !isBeingKeptAwakeLocked()) {
                                Slog.i(TAG, "Stopping dream because the battery appears to be draining faster than it is charging.  Battery level when dream started: " + this.mBatteryLevelWhenDreamStarted + "%.  Battery level now: " + this.mBatteryLevel + "%.");
                            }
                        }
                        if (isItBedTimeYetLocked()) {
                            Slog.i(TAG, "handleSandman: Bed time and goToSleepNoUpdateLocked");
                            goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 2, 0, 1000);
                            updatePowerStateLocked();
                        } else {
                            Slog.i(TAG, "handleSandman: time to wakeUpNoUpdateLocked");
                            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), 0, "android.server.power:DREAM_FINISHED", 1000, this.mContext.getOpPackageName(), 1000);
                            updatePowerStateLocked();
                        }
                    } else if (wakefulness == 3) {
                        if (!isDreaming) {
                            reallyGoToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 1000);
                            updatePowerStateLocked();
                        }
                    }
                }
            }
        }
    }

    private boolean canDreamLocked() {
        int i;
        int i2;
        if (this.mWakefulness != 2 || !this.mDreamsSupportedConfig || !this.mDreamsEnabledSetting || !this.mDisplayPowerRequest.isBrightOrDim() || this.mDisplayPowerRequest.isVr() || (this.mUserActivitySummary & 7) == 0 || !this.mBootCompleted) {
            return false;
        }
        if (isBeingKeptAwakeLocked()) {
            return true;
        }
        if (!this.mIsPowered && !this.mDreamsEnabledOnBatteryConfig) {
            return false;
        }
        if (!this.mIsPowered && (i2 = this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig) >= 0 && this.mBatteryLevel < i2) {
            return false;
        }
        if (!this.mIsPowered || (i = this.mDreamsBatteryLevelMinimumWhenPoweredConfig) < 0 || this.mBatteryLevel >= i) {
            return true;
        }
        return false;
    }

    private boolean canDozeLocked() {
        return this.mWakefulness == 3;
    }

    private boolean updateDisplayPowerStateLocked(int dirty) {
        int screenBrightnessOverride;
        boolean oldDisplayReady = this.mDisplayReady;
        if ((dirty & 14399) != 0) {
            this.mDisplayPowerRequest.policy = getDesiredScreenPolicyLocked();
            this.mOppoPowerManagerHelper.setUseProximityForceSuspendValueLocked(false);
            boolean autoBrightness = this.mScreenBrightnessModeSetting == 1;
            boolean screenBrightnessOverrideFromWindowManager = false;
            if (!this.mBootCompleted) {
                screenBrightnessOverride = mOppoBrightUtils.getBootupBrightness();
                if (!autoBrightness && isValidBrightness(this.mScreenBrightnessSetting)) {
                    screenBrightnessOverride = this.mScreenBrightnessSetting;
                }
            } else if (isValidBrightness(this.mScreenBrightnessOverrideFromWindowManager)) {
                if (this.mScreenBrightnessOverrideFromWindowManager > 0) {
                    screenBrightnessOverride = this.mScreenBrightnessOverrideFromWindowManager;
                    Slog.d(TAG, "brightness from window brightness = " + screenBrightnessOverride);
                } else {
                    screenBrightnessOverride = this.mScreenBrightnessOverrideFromWindowManager;
                    Slog.d(TAG, "brightness from window else brightness = " + screenBrightnessOverride);
                }
                if (autoBrightness) {
                    OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mUseWindowBrightness = true;
                } else {
                    OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                    OppoBrightUtils.mUseWindowBrightness = false;
                }
                screenBrightnessOverrideFromWindowManager = true;
                autoBrightness = false;
            } else {
                screenBrightnessOverride = -1;
            }
            if (this.mScreenBrightnessOverrideFromWindowManager == -1 && screenBrightnessOverrideFromWindowManager) {
                OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                OppoBrightUtils.mUseWindowBrightness = true;
            }
            DisplayManagerInternal.DisplayPowerRequest displayPowerRequest = this.mDisplayPowerRequest;
            displayPowerRequest.screenBrightnessOverride = screenBrightnessOverride;
            displayPowerRequest.useAutoBrightness = autoBrightness;
            displayPowerRequest.useProximitySensor = shouldUseProximitySensorLocked();
            this.mDisplayPowerRequest.boostScreenBrightness = shouldBoostScreenBrightness();
            updatePowerRequestFromBatterySaverPolicy(this.mDisplayPowerRequest);
            if (this.mDisplayPowerRequest.policy == 1) {
                DisplayManagerInternal.DisplayPowerRequest displayPowerRequest2 = this.mDisplayPowerRequest;
                displayPowerRequest2.dozeScreenState = this.mDozeScreenStateOverrideFromDreamManager;
                if ((this.mWakeLockSummary & 128) != 0 && !this.mDrawWakeLockOverrideFromSidekick && displayPowerRequest2.dozeScreenState == 4 && (this.DEBUG_PANIC || this.DEBUG)) {
                    Slog.i(TAG, "find someone get abnormal wakelock when Aod!!!\n");
                }
                this.mDisplayPowerRequest.dozeScreenBrightness = this.mDozeScreenBrightnessOverrideFromDreamManager;
                if (this.mDozeScreenStateOverrideFromDreamManager == 3) {
                    this.mOppoPowerManagerHelper.scheduleStartAutomaticAodBacklightAdjustment();
                }
            } else {
                DisplayManagerInternal.DisplayPowerRequest displayPowerRequest3 = this.mDisplayPowerRequest;
                displayPowerRequest3.dozeScreenState = 0;
                displayPowerRequest3.dozeScreenBrightness = -1;
            }
            if (this.mDisplayPowerRequest.policy == 0 || this.mDisplayPowerRequest.policy == 3 || this.mDisplayPowerRequest.policy == 2 || this.mDisplayPowerRequest.policy == 4) {
                this.mOppoPowerManagerHelper.scheduleStopAutomaticAodBacklightAdjustment();
            }
            this.mOppoPowerManagerHelper.updateOppoProximityScreenoffPolicyLocked(this.mWakeLockSummary, this.mWakefulness);
            this.mDisplayReady = this.mDisplayManagerInternal.requestPowerState(this.mDisplayPowerRequest, this.mRequestWaitForNegativeProximity);
            this.mRequestWaitForNegativeProximity = false;
            if ((dirty & 4096) != 0) {
                sQuiescent = false;
            }
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "updateDisplayPowerStateLocked: mDisplayReady=" + this.mDisplayReady + ", policy=" + DisplayManagerInternal.DisplayPowerRequest.policyToString(this.mDisplayPowerRequest.policy) + ", mWakefulness=" + this.mWakefulness + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", mBootCompleted=" + this.mBootCompleted + ", screenBrightnessOverride=" + screenBrightnessOverride + ", useAutoBrightness=" + autoBrightness + ", mScreenBrightnessBoostInProgress=" + this.mScreenBrightnessBoostInProgress + ", mIsVrModeEnabled= " + this.mIsVrModeEnabled + ", sQuiescent=" + sQuiescent);
            }
        }
        return this.mDisplayReady && !oldDisplayReady;
    }

    private void updateScreenBrightnessBoostLocked(int dirty) {
        if ((dirty & 2048) != 0 && this.mScreenBrightnessBoostInProgress) {
            long now = SystemClock.uptimeMillis();
            this.mHandler.removeMessages(3);
            long j = this.mLastScreenBrightnessBoostTime;
            if (j > this.mLastSleepTime) {
                long boostTimeout = j + 5000;
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
        return !this.mIsVrModeEnabled && this.mScreenBrightnessBoostInProgress;
    }

    private static boolean isValidBrightness(int value) {
        return value >= 0 && value <= mScreenBrightnessSettingMaximum;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getDesiredScreenPolicyLocked() {
        int i = this.mWakefulness;
        if (i == 0 || sQuiescent || this.mShutdownFlag) {
            return 0;
        }
        if (i == 3) {
            if ((this.mWakeLockSummary & 64) != 0) {
                return 1;
            }
            if (this.mDozeAfterScreenOff) {
                return 0;
            }
        }
        if (this.mIsVrModeEnabled) {
            return 4;
        }
        if ((this.mWakeLockSummary & 2) != 0 || (this.mUserActivitySummary & 1) != 0 || !this.mBootCompleted || this.mScreenBrightnessBoostInProgress) {
            return 3;
        }
        return 2;
    }

    private boolean shouldUseProximitySensorLocked() {
        return !this.mIsVrModeEnabled && (this.mWakeLockSummary & 16) != 0;
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

    private boolean needDisplaySuspendBlockerLocked() {
        if (!this.mDisplayReady) {
            return true;
        }
        if ((!this.mDisplayPowerRequest.isBrightOrDim() || (this.mDisplayPowerRequest.useProximitySensor && this.mProximityPositive && this.mSuspendWhenScreenOffDueToProximityConfig)) && !this.mScreenBrightnessBoostInProgress) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setHalAutoSuspendModeLocked(boolean enable) {
        if (enable != this.mHalAutoSuspendModeEnabled) {
            if (this.DEBUG_PANIC || this.DEBUG) {
                Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable);
            }
            this.mHalAutoSuspendModeEnabled = enable;
            Trace.traceBegin(131072, "setHalAutoSuspend(" + enable + ")");
            try {
                this.mNativeWrapper.nativeSetAutoSuspend(enable);
            } finally {
                Trace.traceEnd(131072);
                if (this.DEBUG_PANIC) {
                    Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable + " done");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setHalInteractiveModeLocked(boolean enable) {
        if (enable != this.mHalInteractiveModeEnabled) {
            if (this.DEBUG_PANIC || this.DEBUG) {
                Slog.d(TAG, "Setting HAL interactive mode to " + enable);
            }
            this.mHalInteractiveModeEnabled = enable;
            Trace.traceBegin(131072, "setHalInteractive(" + enable + ")");
            try {
                this.mNativeWrapper.nativeSetInteractive(enable);
            } finally {
                Trace.traceEnd(131072);
                if (this.DEBUG_PANIC) {
                    Slog.d(TAG, "Setting HAL interactive mode to " + enable + " done");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isInteractiveInternal() {
        boolean isInteractive;
        synchronized (this.mLock) {
            isInteractive = PowerManagerInternal.isInteractive(this.mWakefulness);
        }
        return isInteractive;
    }

    /* access modifiers changed from: private */
    public boolean getDisplayAodStatusInternal() {
        synchronized (this.mLock) {
            if (this.mWakefulness != 3) {
                return false;
            }
            if (this.mDozeScreenStateOverrideFromDreamManager != 4) {
                if (this.mDozeScreenStateOverrideFromDreamManager != 3) {
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean setLowPowerModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.DEBUG) {
                Slog.d(TAG, "setLowPowerModeInternal " + enabled + " mIsPowered=" + this.mIsPowered);
            }
            if (this.mIsPowered && OppoFeatureCache.get(IColorBatterySaveExtend.DEFAULT).isClosedSuperFirewall(this.mContext.getPackageManager())) {
                return false;
            }
            this.mBatterySaverStateMachine.setBatterySaverEnabledManually(enabled);
            userActivityInternal(SystemClock.uptimeMillis(), 0, 0, 1000);
            Slog.d(TAG, "setLowPowerModeInternal " + enabled);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceIdleMode;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isLightDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLightDeviceIdleMode;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void handleBatteryStateChangedLocked() {
        this.mDirty |= 256;
        updatePowerStateLocked();
    }

    /* JADX WARN: Unexpected block without predecessors: B:15:0x0032 */
    /* access modifiers changed from: private */
    /*  JADX ERROR: JadxRuntimeException in pass: BlockFinish
        jadx.core.utils.exceptions.JadxRuntimeException: Dominance frontier not set for block: B:0:0x0000
        	at jadx.core.dex.nodes.BlockNode.lock(BlockNode.java:75)
        	at jadx.core.utils.ImmutableList.forEach(ImmutableList.java:113)
        	at jadx.core.dex.nodes.MethodNode.finishBasicBlocks(MethodNode.java:363)
        	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:27)
        */
    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:0:0x0000
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:426)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:64)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:46)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstInlineVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ConstInlineVisitor.checkForSynchronizeBlock(ConstInlineVisitor.java:121)
        	at jadx.core.dex.visitors.ConstInlineVisitor.checkInsn(ConstInlineVisitor.java:86)
        	at jadx.core.dex.visitors.ConstInlineVisitor.process(ConstInlineVisitor.java:53)
        	at jadx.core.dex.visitors.ConstInlineVisitor.visit(ConstInlineVisitor.java:45)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstructorVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.mods.ConstructorInsn.<init>(ConstructorInsn.java:47)
        	at jadx.core.dex.visitors.ConstructorVisitor.processInvoke(ConstructorVisitor.java:64)
        	at jadx.core.dex.visitors.ConstructorVisitor.replaceInvoke(ConstructorVisitor.java:48)
        	at jadx.core.dex.visitors.ConstructorVisitor.visit(ConstructorVisitor.java:37)
        */
    /*  JADX ERROR: NullPointerException in pass: InitCodeVariables
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVar(InitCodeVariables.java:56)
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVars(InitCodeVariables.java:45)
        	at jadx.core.dex.visitors.InitCodeVariables.visit(InitCodeVariables.java:32)
        */
    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.processMoveException(ModVisitor.java:551)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:134)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:91)
        */
    /*  JADX ERROR: NullPointerException in pass: SSATransform
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:88)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:53)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    /* JADX WARNING: Can't wrap try/catch for region: R(2:11|10) */
    /* JADX WARNING: Can't wrap try/catch for region: R(2:12|13) */
    public void shutdownOrRebootInternal(int r4, boolean r5, java.lang.String r6, boolean r7) {
        /*
            r3 = this;
            com.android.server.power.PowerManagerService$PowerManagerHandler r0 = r3.mHandler
            if (r0 == 0) goto L_0x0008
            boolean r0 = r3.mSystemReady
            if (r0 != 0) goto L_0x0011
        L_0x0008:
            boolean r0 = com.android.server.RescueParty.isAttemptingFactoryReset()
            if (r0 == 0) goto L_0x0036
            lowLevelReboot(r6)
        L_0x0011:
            com.android.server.power.PowerManagerService$2 r0 = new com.android.server.power.PowerManagerService$2
            r0.<init>(r4, r5, r6)
            android.os.Handler r1 = com.android.server.UiThread.getHandler()
            android.os.Message r1 = android.os.Message.obtain(r1, r0)
            r2 = 1
            r1.setAsynchronous(r2)
            android.os.Handler r2 = com.android.server.UiThread.getHandler()
            r2.sendMessage(r1)
            if (r7 == 0) goto L_0x0035
            monitor-enter(r0)
        L_0x002c:
            r0.wait()     // Catch:{ InterruptedException -> 0x0033 }
        L_0x002f:
            goto L_0x002c
        L_0x0030:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0030 }
            throw r2
        L_0x0033:
            r2 = move-exception
            goto L_0x002f
        L_0x0035:
            return
        L_0x0036:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "Too early to call shutdown() or reboot()"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.PowerManagerService.shutdownOrRebootInternal(int, boolean, java.lang.String, boolean):void");
    }

    /* access modifiers changed from: private */
    public void crashInternal(final String message) {
        Thread t = new Thread("PowerManagerService.crash()") {
            /* class com.android.server.power.PowerManagerService.AnonymousClass3 */

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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePowerRequestFromBatterySaverPolicy(DisplayManagerInternal.DisplayPowerRequest displayPowerRequest) {
        PowerSaveState state = this.mBatterySaverPolicy.getBatterySaverPolicy(7);
        displayPowerRequest.lowPowerMode = state.batterySaverEnabled;
        displayPowerRequest.screenLowPowerBrightnessFactor = state.brightnessFactor;
    }

    /* access modifiers changed from: package-private */
    public void setStayOnSettingInternal(int val) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", val);
    }

    /* access modifiers changed from: package-private */
    public void setMaximumScreenOffTimeoutFromDeviceAdminInternal(int userId, long timeMs) {
        if (userId < 0) {
            Slog.wtf(TAG, "Attempt to set screen off timeout for invalid user: " + userId);
            return;
        }
        synchronized (this.mLock) {
            if (userId == 0) {
                try {
                    this.mMaximumScreenOffTimeoutFromDeviceAdmin = timeMs;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (timeMs != JobStatus.NO_LATEST_RUNTIME) {
                    if (timeMs != 0) {
                        ProfilePowerState profile = this.mProfilePowerState.get(userId);
                        if (profile != null) {
                            profile.mScreenOffTimeout = timeMs;
                        } else {
                            this.mProfilePowerState.put(userId, new ProfilePowerState(userId, timeMs));
                            this.mDirty |= 1;
                        }
                    }
                }
                this.mProfilePowerState.delete(userId);
            }
            this.mDirty |= 32;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
        if (r3 == false) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        android.common.OppoFeatureCache.get(com.android.server.power.IColorWakeLockCheck.DEFAULT).onDeviceIdle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        if (r3 == false) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOnPhase("power");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOffPhase("power");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return true;
     */
    public boolean setDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode == enabled) {
                return false;
            }
            this.mDeviceIdleMode = enabled;
            updateWakeLockDisabledStatesLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setLightDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mLightDeviceIdleMode == enabled) {
                return false;
            }
            this.mLightDeviceIdleMode = enabled;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceIdleWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceIdleTempWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleTempWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startUidChangesInternal() {
        synchronized (this.mLock) {
            this.mUidsChanging = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishUidChangesInternal() {
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

    /* access modifiers changed from: package-private */
    public void updateUidProcStateInternal(int uid, int procState) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                this.mUidState.put(uid, state);
            }
            boolean z = true;
            boolean oldShouldAllow = state.mProcState <= 12;
            state.mProcState = procState;
            if (state.mNumWakeLocks > 0) {
                if (this.mDeviceIdleMode) {
                    handleUidStateChangeLocked();
                } else if (!state.mActive) {
                    if (procState > 12) {
                        z = false;
                    }
                    if (oldShouldAllow != z) {
                        handleUidStateChangeLocked();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidGoneInternal(int uid) {
        synchronized (this.mLock) {
            int index = this.mUidState.indexOfKey(uid);
            if (index >= 0) {
                UidState state = this.mUidState.valueAt(index);
                state.mProcState = 21;
                state.mActive = false;
                this.mUidState.removeAt(index);
                if (this.mDeviceIdleMode && state.mNumWakeLocks > 0) {
                    handleUidStateChangeLocked();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidActiveInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                state.mProcState = 20;
                this.mUidState.put(uid, state);
            }
            state.mActive = true;
            if (state.mNumWakeLocks > 0) {
                handleUidStateChangeLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidIdleInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
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
            WakeLock wakeLock = this.mWakeLocks.get(i);
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

    /* access modifiers changed from: private */
    public boolean setWakeLockDisabledStateLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & 65535) == 1) {
            boolean disabled = false;
            if (wakeLock.mPackageName != null && wakeLock.mPackageName.equals("com.mobiletools.systemhelper")) {
                return false;
            }
            int appid = UserHandle.getAppId(wakeLock.mOwnerUid);
            if (appid >= 10000) {
                if (this.mConstants.NO_CACHED_WAKE_LOCKS) {
                    disabled = this.mForceSuspendActive || (!wakeLock.mUidState.mActive && wakeLock.mUidState.mProcState != 21 && wakeLock.mUidState.mProcState > 12);
                }
                if (this.mDeviceIdleMode) {
                    UidState state = wakeLock.mUidState;
                    if (Arrays.binarySearch(this.mDeviceIdleWhitelist, appid) < 0 && Arrays.binarySearch(this.mDeviceIdleTempWhitelist, appid) < 0 && state.mProcState != 21 && state.mProcState > 6) {
                        disabled = true;
                    }
                }
            }
            if (wakeLock.mDisabledByHans) {
                disabled = true;
            }
            if (wakeLock.mDisabled != disabled) {
                wakeLock.mDisabled = disabled;
                return true;
            }
        }
        return false;
    }

    private boolean isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() {
        long j = this.mMaximumScreenOffTimeoutFromDeviceAdmin;
        return j >= 0 && j < JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r5 == false) goto L_0x0012;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        r3 = 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0013, code lost:
        r1.setFlashing(r6, 2, r3, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0016, code lost:
        return;
     */
    public void setAttentionLightInternal(boolean on, int color) {
        synchronized (this.mLock) {
            if (this.mSystemReady) {
                Light light = this.mAttentionLight;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDozeAfterScreenOffInternal(boolean on) {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004f, code lost:
        return;
     */
    public void boostScreenBrightnessInternal(long eventTime, int uid) {
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

    /* access modifiers changed from: private */
    public boolean isScreenBrightnessBoostedInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenBrightnessBoostInProgress;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void handleScreenBrightnessBoostTimeout() {
        synchronized (this.mLock) {
            if (this.DEBUG_SPEW) {
                Slog.d(TAG, "handleScreenBrightnessBoostTimeout");
            }
            this.mDirty |= 2048;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: private */
    public void setScreenBrightnessOverrideFromWindowManagerInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mScreenBrightnessOverrideFromWindowManager != brightness) {
                this.mScreenBrightnessOverrideFromWindowManager = brightness;
                if (this.DEBUG_PANIC) {
                    Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManager = " + brightness);
                }
                if (this.mScreenBrightnessModeSetting != 1) {
                    OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 1;
                    OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAdj = 0;
                    OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                    OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = false;
                } else if (brightness == -1) {
                    OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 0;
                    OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = true;
                    OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetOverride = true;
                } else {
                    OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = 1;
                    OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                    OppoBrightUtils.mSetBrihgtnessSlide = false;
                    OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                    if (OppoBrightUtils.mFirstSetOverride) {
                        OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                        OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAdj = OppoBrightUtils.mManualBrightness;
                        OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
                        OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.mManulAtAmbientLux;
                        OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
                        OppoBrightUtils.mFirstSetOverride = false;
                    }
                }
                Slog.d(TAG, "brightness from window brightness = " + brightness + " mode = " + this.mScreenBrightnessModeSetting);
                this.mDirty = this.mDirty | 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setUserInactiveOverrideFromWindowManagerInternal() {
        synchronized (this.mLock) {
            this.mUserInactiveOverrideFromWindowManager = true;
            this.mDirty |= 4;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: private */
    public void setUserActivityTimeoutOverrideFromWindowManagerInternal(long timeoutMillis) {
        synchronized (this.mLock) {
            if (this.mUserActivityTimeoutOverrideFromWindowManager != timeoutMillis) {
                if (this.DEBUG_PANIC) {
                    Slog.d(TAG, "UA TimeoutOverrideFromWindowManagerInternal = " + timeoutMillis);
                }
                this.mUserActivityTimeoutOverrideFromWindowManager = timeoutMillis;
                EventLogTags.writeUserActivityTimeoutOverride(timeoutMillis);
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0105  */
    private void setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal(float adj) {
        synchronized (this.mLock) {
            if (adj != (((float) mScreenBrightnessSettingMaximum) * 300.0f) / 255.0f) {
                if (adj != (((float) mScreenBrightnessSettingMaximum) * 301.0f) / 255.0f) {
                    if (adj == (((float) mScreenBrightnessSettingMaximum) * 500.0f) / 255.0f) {
                        Slog.d(TAG, "adj == " + adj);
                        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                        OppoBrightUtils.mCameraBacklight = false;
                        OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mGalleryBacklight = false;
                        OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                        OppoBrightUtils.mCameraMode = 0;
                        OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                        OppoBrightUtils.mCameraUseAdjustmentSetting = true;
                        this.mDisplayManagerInternal.setOutdoorMode(false);
                        if (this.mScreenBrightnessModeSetting == 0) {
                            OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                            OppoBrightUtils.mCameraMode = -1;
                        }
                    }
                    if (adj != 16384.0f) {
                        OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                        OppoBrightUtils.mInverseMode = 1;
                    } else if (adj == 32768.0f) {
                        OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                        OppoBrightUtils.mInverseMode = 0;
                    }
                    if (adj != 16385.0f) {
                        OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                        OppoBrightUtils.mGalleryMode = 1;
                        OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                        OppoBrightUtils.mShouldAdjustRate = 1;
                        this.mDisplayManagerInternal.setOutdoorMode(true);
                    } else if (adj == 32769.0f) {
                        OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                        OppoBrightUtils.mGalleryMode = 0;
                        OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                        OppoBrightUtils.mShouldAdjustRate = 1;
                        this.mDisplayManagerInternal.setOutdoorMode(false);
                    }
                    if (adj != 16386.0f) {
                        OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
                        OppoBrightUtils.mVideoMode = 1;
                        OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
                        OppoBrightUtils.mShouldAdjustRate = 1;
                    } else if (adj == 32770.0f) {
                        OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
                        OppoBrightUtils.mVideoMode = 0;
                        OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
                        OppoBrightUtils.mShouldAdjustRate = 1;
                    }
                    if (this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride != adj) {
                        if (this.DEBUG_PANIC || this.DEBUG) {
                            Slog.d(TAG, "setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal = " + adj);
                        }
                        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = adj;
                        this.mDirty |= 32;
                        updatePowerStateLocked();
                    }
                }
            }
            Slog.d(TAG, "adj == " + adj);
            if (adj == (((float) mScreenBrightnessSettingMaximum) * 300.0f) / 255.0f) {
                Slog.d(TAG, "Camera(boot gallery from camera)");
                OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
                OppoBrightUtils.mCameraBacklight = true;
            } else {
                Slog.d(TAG, "boot gallery from desk or file system");
                OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
                OppoBrightUtils.mGalleryBacklight = true;
            }
            OppoBrightUtils oppoBrightUtils18 = mOppoBrightUtils;
            OppoBrightUtils.mCameraMode = 1;
            OppoBrightUtils oppoBrightUtils19 = mOppoBrightUtils;
            OppoBrightUtils.mCameraUseAdjustmentSetting = true;
            this.mDisplayManagerInternal.setOutdoorMode(true);
            if (adj != 16384.0f) {
            }
            if (adj != 16385.0f) {
            }
            if (adj != 16386.0f) {
            }
            if (this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride != adj) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        int screenState2;
        int value = 0;
        if (screenState == 1) {
            value = 0;
        } else if (screenState == 2) {
            value = 3;
        } else if (screenState == 3) {
            value = 2;
        } else if (screenState == 4) {
            value = 1;
        }
        synchronized (this.mMapLock) {
            for (Integer num : this.mDozeStateMap.keySet()) {
                int key = num.intValue();
                int tmp = this.mDozeStateMap.get(Integer.valueOf(key)).intValue();
                Slog.d(TAG, "setDozeOverrideInternal key:" + key + " tmp:" + tmp);
                if (tmp > value) {
                    value = tmp;
                }
            }
        }
        if (this.DEBUG_PANIC || this.DEBUG) {
            Slog.d(TAG, "setDozeOverrideInternal state=" + screenState + " color=" + screenBrightness + " value=" + value + " mDozeState=" + this.mDozeScreenStateOverrideFromDreamManager);
        }
        if (value == 0) {
            screenState2 = 1;
        } else if (value == 1) {
            screenState2 = 4;
        } else if (value == 2) {
            screenState2 = 3;
        } else if (value != 3) {
            screenState2 = screenState;
        } else {
            screenState2 = 2;
        }
        synchronized (this.mLock) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState2 && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState2;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDrawWakeLockOverrideFromSidekickInternal(boolean keepState) {
        synchronized (this.mLock) {
            if (this.mDrawWakeLockOverrideFromSidekick != keepState) {
                this.mDrawWakeLockOverrideFromSidekick = keepState;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setVrModeEnabled(boolean enabled) {
        this.mIsVrModeEnabled = enabled;
    }

    /* access modifiers changed from: private */
    public void powerHintInternal(int hintId, int data) {
        if (hintId != 8 || data != 1 || !this.mBatterySaverController.isLaunchBoostDisabled()) {
            this.mNativeWrapper.nativeSendPowerHint(hintId, data);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean wasDeviceIdleForInternal(long ms) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLastUserActivityTime + ms < SystemClock.uptimeMillis();
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void onUserActivity() {
        synchronized (this.mLock) {
            this.mLastUserActivityTime = SystemClock.uptimeMillis();
        }
    }

    /* access modifiers changed from: private */
    public boolean forceSuspendInternal(int uid) {
        try {
            synchronized (this.mLock) {
                this.mForceSuspendActive = true;
                goToSleepInternal(SystemClock.uptimeMillis(), 8, 1, uid);
                updateWakeLockDisabledStatesLocked();
            }
            Slog.i(TAG, "Force-Suspending (uid " + uid + ")...");
            boolean success = this.mNativeWrapper.nativeForceSuspend();
            if (!success) {
                Slog.i(TAG, "Force-Suspending failed in native.");
            }
            synchronized (this.mLock) {
                this.mForceSuspendActive = false;
                updateWakeLockDisabledStatesLocked();
            }
            return success;
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mForceSuspendActive = false;
                updateWakeLockDisabledStatesLocked();
                throw th;
            }
        }
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
            HashSet<String> validReasons = new HashSet<>();
            validReasons.add("recovery");
            validReasons.add("rf");
            validReasons.add("wlan");
            validReasons.add("mos");
            validReasons.add("ftm");
            validReasons.add("silence");
            validReasons.add("sau");
            validReasons.add("sblmemtest");
            validReasons.add("usermemaging");
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

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* JADX INFO: Multiple debug info for r8v23 com.android.server.power.WirelessChargerDetector: [D('i' int), D('wcd' com.android.server.power.WirelessChargerDetector)] */
    /* access modifiers changed from: private */
    public void dumpInternal(PrintWriter pw) {
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
            pw.println("  mMotionFlags=" + this.mOppoPowerManagerHelper.getMotionFlags() + " mMotionGameDisplayState=" + this.mOppoPowerManagerHelper.getMotionGameDisplayStateLocked());
            StringBuilder sb = new StringBuilder();
            sb.append("  mUserActivitySummary=0x");
            sb.append(Integer.toHexString(this.mUserActivitySummary));
            pw.println(sb.toString());
            pw.println("  mRequestWaitForNegativeProximity=" + this.mRequestWaitForNegativeProximity);
            pw.println("  mSandmanScheduled=" + this.mSandmanScheduled);
            pw.println("  mSandmanSummoned=" + this.mSandmanSummoned);
            pw.println("  mBatteryLevelLow=" + this.mBatteryLevelLow);
            pw.println("  mLightDeviceIdleMode=" + this.mLightDeviceIdleMode);
            pw.println("  mDeviceIdleMode=" + this.mDeviceIdleMode);
            pw.println("  mDeviceIdleWhitelist=" + Arrays.toString(this.mDeviceIdleWhitelist));
            pw.println("  mDeviceIdleTempWhitelist=" + Arrays.toString(this.mDeviceIdleTempWhitelist));
            pw.println("  mLastWakeTime=" + TimeUtils.formatUptime(this.mLastWakeTime));
            pw.println("  mLastSleepTime=" + TimeUtils.formatUptime(this.mLastSleepTime));
            pw.println("  mLastSleepReason=" + PowerManager.sleepReasonToString(this.mLastSleepReason));
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
            pw.println("  mDozeAfterScreenOff=" + this.mDozeAfterScreenOff);
            pw.println("  mMinimumScreenOffTimeoutConfig=" + this.mMinimumScreenOffTimeoutConfig);
            pw.println("  mMaximumScreenDimDurationConfig=" + this.mMaximumScreenDimDurationConfig);
            pw.println("  mMaximumScreenDimRatioConfig=" + this.mMaximumScreenDimRatioConfig);
            pw.println("  mScreenOffTimeoutSetting=" + this.mScreenOffTimeoutSetting);
            pw.println("  mSleepTimeoutSetting=" + this.mSleepTimeoutSetting);
            pw.println("  mMaximumScreenOffTimeoutFromDeviceAdmin=" + this.mMaximumScreenOffTimeoutFromDeviceAdmin + " (enforced=" + isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() + ")");
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  mStayOnWhilePluggedInSetting=");
            sb2.append(this.mStayOnWhilePluggedInSetting);
            pw.println(sb2.toString());
            pw.println("  mScreenBrightnessSetting=" + this.mScreenBrightnessSetting);
            pw.println("  mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
            pw.println("  mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
            pw.println("  mUserActivityTimeoutOverrideFromWindowManager=" + this.mUserActivityTimeoutOverrideFromWindowManager);
            pw.println("  mUserInactiveOverrideFromWindowManager=" + this.mUserInactiveOverrideFromWindowManager);
            pw.println("  mDozeScreenStateOverrideFromDreamManager=" + this.mDozeScreenStateOverrideFromDreamManager);
            pw.println("  mDrawWakeLockOverrideFromSidekick=" + this.mDrawWakeLockOverrideFromSidekick);
            pw.println("  mDozeScreenBrightnessOverrideFromDreamManager=" + this.mDozeScreenBrightnessOverrideFromDreamManager);
            pw.println("  mScreenBrightnessSettingMinimum=" + mScreenBrightnessSettingMinimum);
            pw.println("  mScreenBrightnessSettingMaximum=" + mScreenBrightnessSettingMaximum);
            pw.println("  mScreenBrightnessSettingDefault=" + this.mScreenBrightnessSettingDefault);
            pw.println("  mDoubleTapWakeEnabled=" + this.mDoubleTapWakeEnabled);
            pw.println("  mIsVrModeEnabled=" + this.mIsVrModeEnabled);
            pw.println("  mForegroundProfile=" + this.mForegroundProfile);
            long sleepTimeout = getSleepTimeoutLocked();
            long screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            long screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
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
                UidState state = this.mUidState.valueAt(i);
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
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                pw.println("  " + it.next());
            }
            pw.println();
            pw.println("Suspend Blockers: size=" + this.mSuspendBlockers.size());
            Iterator<SuspendBlocker> it2 = this.mSuspendBlockers.iterator();
            while (it2.hasNext()) {
                pw.println("  " + it2.next());
            }
            pw.println();
            pw.println("Display Power: " + this.mDisplayPowerCallbacks);
            this.mBatterySaverPolicy.dump(pw);
            this.mBatterySaverStateMachine.dump(pw);
            this.mAttentionDetector.dump(pw);
            pw.println();
            int numProfiles = this.mProfilePowerState.size();
            pw.println("Profile power states: size=" + numProfiles);
            for (int i2 = 0; i2 < numProfiles; i2++) {
                ProfilePowerState profile = this.mProfilePowerState.valueAt(i2);
                pw.print("  mUserId=");
                pw.print(profile.mUserId);
                pw.print(" mScreenOffTimeout=");
                pw.print(profile.mScreenOffTimeout);
                pw.print(" mWakeLockSummary=");
                pw.print(profile.mWakeLockSummary);
                pw.print(" mLastUserActivityTime=");
                pw.print(profile.mLastUserActivityTime);
                pw.print(" mLockingNotified=");
                pw.println(profile.mLockingNotified);
            }
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.dump(pw);
        }
    }

    /* JADX INFO: Multiple debug info for r5v15 long: [D('screenOffTimeout' long), D('screenDimDuration' long)] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.min(long, long):long}
     arg types: [long, int]
     candidates:
      ClspMth{java.lang.Math.min(double, double):double}
      ClspMth{java.lang.Math.min(float, float):float}
      ClspMth{java.lang.Math.min(int, int):int}
      ClspMth{java.lang.Math.min(long, long):long} */
    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd) {
        WirelessChargerDetector wcd;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            this.mConstants.dumpProto(proto);
            proto.write(1120986464258L, this.mDirty);
            proto.write(1159641169923L, this.mWakefulness);
            proto.write(1133871366148L, this.mWakefulnessChanging);
            proto.write(1133871366149L, this.mIsPowered);
            proto.write(1159641169926L, this.mPlugType);
            proto.write(1120986464263L, this.mBatteryLevel);
            proto.write(1120986464264L, this.mBatteryLevelWhenDreamStarted);
            proto.write(1159641169929L, this.mDockState);
            proto.write(1133871366154L, this.mStayOn);
            proto.write(1133871366155L, this.mProximityPositive);
            proto.write(1133871366156L, this.mBootCompleted);
            proto.write(1133871366157L, this.mSystemReady);
            proto.write(1133871366158L, this.mHalAutoSuspendModeEnabled);
            proto.write(1133871366159L, this.mHalInteractiveModeEnabled);
            long activeWakeLocksToken = proto.start(1146756268048L);
            proto.write(1133871366145L, (this.mWakeLockSummary & 1) != 0);
            proto.write(1133871366146L, (this.mWakeLockSummary & 2) != 0);
            proto.write(1133871366147L, (this.mWakeLockSummary & 4) != 0);
            proto.write(1133871366148L, (this.mWakeLockSummary & 8) != 0);
            proto.write(1133871366149L, (this.mWakeLockSummary & 16) != 0);
            proto.write(1133871366150L, (this.mWakeLockSummary & 32) != 0);
            proto.write(1133871366151L, (this.mWakeLockSummary & 64) != 0);
            proto.write(1133871366152L, (this.mWakeLockSummary & 128) != 0);
            proto.end(activeWakeLocksToken);
            proto.write(1112396529681L, this.mNotifyLongScheduled);
            proto.write(1112396529682L, this.mNotifyLongDispatched);
            proto.write(1112396529683L, this.mNotifyLongNextCheck);
            long userActivityToken = proto.start(1146756268052L);
            proto.write(1133871366145L, (this.mUserActivitySummary & 1) != 0);
            proto.write(1133871366146L, (this.mUserActivitySummary & 2) != 0);
            proto.write(1133871366147L, (this.mUserActivitySummary & 4) != 0);
            proto.end(userActivityToken);
            proto.write(1133871366165L, this.mRequestWaitForNegativeProximity);
            proto.write(1133871366166L, this.mSandmanScheduled);
            proto.write(1133871366167L, this.mSandmanSummoned);
            proto.write(1133871366168L, this.mBatteryLevelLow);
            proto.write(1133871366169L, this.mLightDeviceIdleMode);
            proto.write(1133871366170L, this.mDeviceIdleMode);
            for (int id : this.mDeviceIdleWhitelist) {
                proto.write(2220498092059L, id);
            }
            for (int id2 : this.mDeviceIdleTempWhitelist) {
                proto.write(2220498092060L, id2);
            }
            proto.write(1112396529693L, this.mLastWakeTime);
            proto.write(1112396529694L, this.mLastSleepTime);
            proto.write(1112396529695L, this.mLastUserActivityTime);
            proto.write(1112396529696L, this.mLastUserActivityTimeNoChangeLights);
            proto.write(1112396529697L, this.mLastInteractivePowerHintTime);
            proto.write(1112396529698L, this.mLastScreenBrightnessBoostTime);
            proto.write(1133871366179L, this.mScreenBrightnessBoostInProgress);
            proto.write(1133871366180L, this.mDisplayReady);
            proto.write(1133871366181L, this.mHoldingWakeLockSuspendBlocker);
            proto.write(1133871366182L, this.mHoldingDisplaySuspendBlocker);
            long settingsAndConfigurationToken = proto.start(1146756268071L);
            proto.write(1133871366145L, this.mDecoupleHalAutoSuspendModeFromDisplayConfig);
            proto.write(1133871366146L, this.mDecoupleHalInteractiveModeFromDisplayConfig);
            proto.write(1133871366147L, this.mWakeUpWhenPluggedOrUnpluggedConfig);
            proto.write(1133871366148L, this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig);
            proto.write(1133871366149L, this.mTheaterModeEnabled);
            proto.write(1133871366150L, this.mSuspendWhenScreenOffDueToProximityConfig);
            proto.write(1133871366151L, this.mDreamsSupportedConfig);
            proto.write(1133871366152L, this.mDreamsEnabledByDefaultConfig);
            proto.write(1133871366153L, this.mDreamsActivatedOnSleepByDefaultConfig);
            proto.write(1133871366154L, this.mDreamsActivatedOnDockByDefaultConfig);
            proto.write(1133871366155L, this.mDreamsEnabledOnBatteryConfig);
            proto.write(1172526071820L, this.mDreamsBatteryLevelMinimumWhenPoweredConfig);
            proto.write(1172526071821L, this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            proto.write(1172526071822L, this.mDreamsBatteryLevelDrainCutoffConfig);
            proto.write(1133871366159L, this.mDreamsEnabledSetting);
            proto.write(1133871366160L, this.mDreamsActivateOnSleepSetting);
            proto.write(1133871366161L, this.mDreamsActivateOnDockSetting);
            proto.write(1133871366162L, this.mDozeAfterScreenOff);
            proto.write(1120986464275L, this.mMinimumScreenOffTimeoutConfig);
            proto.write(1120986464276L, this.mMaximumScreenDimDurationConfig);
            proto.write(1108101562389L, this.mMaximumScreenDimRatioConfig);
            proto.write(1120986464278L, this.mScreenOffTimeoutSetting);
            proto.write(1172526071831L, this.mSleepTimeoutSetting);
            proto.write(1120986464280L, Math.min(this.mMaximumScreenOffTimeoutFromDeviceAdmin, 2147483647L));
            proto.write(1133871366169L, isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked());
            long stayOnWhilePluggedInToken = proto.start(1146756268058L);
            proto.write(1133871366145L, (this.mStayOnWhilePluggedInSetting & 1) != 0);
            proto.write(1133871366146L, (this.mStayOnWhilePluggedInSetting & 2) != 0);
            proto.write(1133871366147L, (this.mStayOnWhilePluggedInSetting & 4) != 0);
            proto.end(stayOnWhilePluggedInToken);
            proto.write(1159641169947L, this.mScreenBrightnessModeSetting);
            proto.write(1172526071836L, this.mScreenBrightnessOverrideFromWindowManager);
            proto.write(1176821039133L, this.mUserActivityTimeoutOverrideFromWindowManager);
            proto.write(1133871366174L, this.mUserInactiveOverrideFromWindowManager);
            proto.write(1159641169951L, this.mDozeScreenStateOverrideFromDreamManager);
            proto.write(1133871366180L, this.mDrawWakeLockOverrideFromSidekick);
            proto.write(1108101562400L, this.mDozeScreenBrightnessOverrideFromDreamManager);
            long screenBrightnessSettingLimitsToken = proto.start(1146756268065L);
            proto.write(1120986464257L, mScreenBrightnessSettingMinimum);
            proto.write(1120986464258L, mScreenBrightnessSettingMaximum);
            proto.write(1120986464259L, this.mScreenBrightnessSettingDefault);
            proto.end(screenBrightnessSettingLimitsToken);
            proto.write(1133871366178L, this.mDoubleTapWakeEnabled);
            proto.write(1133871366179L, this.mIsVrModeEnabled);
            proto.end(settingsAndConfigurationToken);
            long sleepTimeout = getSleepTimeoutLocked();
            long screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            long screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            proto.write(1172526071848L, sleepTimeout);
            proto.write(1120986464297L, screenOffTimeout);
            long screenDimDuration2 = screenDimDuration;
            proto.write(1120986464298L, screenDimDuration2);
            proto.write(1133871366187L, this.mUidsChanging);
            proto.write(1133871366188L, this.mUidsChanged);
            int i = 0;
            while (i < this.mUidState.size()) {
                UidState state = this.mUidState.valueAt(i);
                long uIDToken = proto.start(2246267895853L);
                int uid = this.mUidState.keyAt(i);
                proto.write(1120986464257L, uid);
                proto.write(1138166333442L, UserHandle.formatUid(uid));
                proto.write(1133871366147L, state.mActive);
                proto.write(1120986464260L, state.mNumWakeLocks);
                proto.write(1159641169925L, ActivityManager.processStateAmToProto(state.mProcState));
                proto.end(uIDToken);
                i++;
                screenDimDuration2 = screenDimDuration2;
                settingsAndConfigurationToken = settingsAndConfigurationToken;
                stayOnWhilePluggedInToken = stayOnWhilePluggedInToken;
            }
            this.mBatterySaverStateMachine.dumpProto(proto, 1146756268082L);
            this.mHandler.getLooper().writeToProto(proto, 1146756268078L);
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(proto, 2246267895855L);
            }
            Iterator<SuspendBlocker> it2 = this.mSuspendBlockers.iterator();
            while (it2.hasNext()) {
                it2.next().writeToProto(proto, 2246267895856L);
            }
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.writeToProto(proto, 1146756268081L);
        }
        proto.flush();
    }

    private void incrementBootCount() {
        int count;
        synchronized (this.mLock) {
            try {
                count = Settings.Global.getInt(getContext().getContentResolver(), "boot_count");
            } catch (Settings.SettingNotFoundException e) {
                count = 0;
            }
            Settings.Global.putInt(getContext().getContentResolver(), "boot_count", count + 1);
        }
    }

    /* access modifiers changed from: private */
    public static WorkSource copyWorkSource(WorkSource workSource) {
        if (workSource != null) {
            return new WorkSource(workSource);
        }
        return null;
    }

    @VisibleForTesting
    final class BatteryReceiver extends BroadcastReceiver {
        BatteryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleBatteryStateChangedLocked();
            }
        }
    }

    private final class DreamReceiver extends BroadcastReceiver {
        private DreamReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.scheduleSandmanLocked();
            }
        }
    }

    @VisibleForTesting
    final class UserSwitchedReceiver extends BroadcastReceiver {
        UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleSettingsChangedLocked();
            }
        }
    }

    private final class DockReceiver extends BroadcastReceiver {
        private DockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                if (PowerManagerService.this.mDockState != dockState) {
                    int unused = PowerManagerService.this.mDockState = dockState;
                    PowerManagerService.access$1676(PowerManagerService.this, 1024);
                    PowerManagerService.this.updatePowerStateLocked();
                }
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

    /* access modifiers changed from: private */
    public final class PowerManagerHandler extends Handler {
        public PowerManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                PowerManagerService.this.handleUserActivityTimeout();
            } else if (i == 2) {
                PowerManagerService.this.handleSandman();
            } else if (i == 3) {
                PowerManagerService.this.handleScreenBrightnessBoostTimeout();
            } else if (i == 4) {
                PowerManagerService.this.checkForLongWakeLocks();
            } else if (i == 101) {
                OppoFeatureCache.get(IColorScreenOffOptimization.DEFAULT).handleScreenOffTimeOutKeyGuardLocked();
            } else if (i == 102 && PowerManagerService.this.mDreamManager != null && PowerManagerService.this.mDreamManager.isDreaming()) {
                PowerManagerService.this.mDreamManager.stopDream(true);
                synchronized (PowerManagerService.this.mMapLock) {
                    PowerManagerService.this.mDozeStateMap.clear();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public final class WakeLock implements IBinder.DeathRecipient, IColorWakeLockEx {
        public long mAcquireTime;
        public long mActiveSince = 0;
        public boolean mDisabled;
        public boolean mDisabledByHans;
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
            return this.mFlags == flags && this.mTag.equals(tag) && hasSameWorkSource(workSource) && this.mOwnerUid == ownerUid && this.mOwnerPid == ownerPid;
        }

        public void updateProperties(int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            if (!this.mPackageName.equals(packageName)) {
                throw new IllegalStateException("Existing wake lock package name changed: " + this.mPackageName + " to " + packageName);
            } else if (this.mOwnerUid != ownerUid) {
                throw new IllegalStateException("Existing wake lock uid changed: " + this.mOwnerUid + " to " + ownerUid);
            } else if (this.mOwnerPid == ownerPid) {
                this.mFlags = flags;
                this.mTag = tag;
                updateWorkSource(workSource);
                this.mHistoryTag = historyTag;
            } else {
                throw new IllegalStateException("Existing wake lock pid changed: " + this.mOwnerPid + " to " + ownerPid);
            }
        }

        public boolean hasSameWorkSource(WorkSource workSource) {
            return Objects.equals(this.mWorkSource, workSource);
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
            long wakeLockToken = proto.start(fieldId);
            proto.write(1159641169921L, this.mFlags & 65535);
            proto.write(1138166333442L, this.mTag);
            long wakeLockFlagsToken = proto.start(1146756268035L);
            boolean z = true;
            proto.write(1133871366145L, (this.mFlags & 268435456) != 0);
            if ((this.mFlags & 536870912) == 0) {
                z = false;
            }
            proto.write(1133871366146L, z);
            proto.end(wakeLockFlagsToken);
            proto.write(1133871366148L, this.mDisabled);
            if (this.mNotifiedAcquired) {
                proto.write(1112396529669L, this.mAcquireTime);
            }
            proto.write(1133871366150L, this.mNotifiedLong);
            proto.write(1120986464263L, this.mOwnerUid);
            proto.write(1120986464264L, this.mOwnerPid);
            WorkSource workSource = this.mWorkSource;
            if (workSource != null) {
                workSource.writeToProto(proto, 1146756268041L);
            }
            proto.end(wakeLockToken);
        }

        public String getLockLevelString() {
            int i = this.mFlags & 65535;
            if (i == 1) {
                return "PARTIAL_WAKE_LOCK             ";
            }
            if (i == 6) {
                return "SCREEN_DIM_WAKE_LOCK          ";
            }
            if (i == 10) {
                return "SCREEN_BRIGHT_WAKE_LOCK       ";
            }
            if (i == 26) {
                return "FULL_WAKE_LOCK                ";
            }
            if (i == 32) {
                return "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
            }
            if (i == 64) {
                return "DOZE_WAKE_LOCK                ";
            }
            if (i != 128) {
                return "???                           ";
            }
            return "DRAW_WAKE_LOCK                ";
        }

        private String getLockFlagsString() {
            String result = "";
            if ((this.mFlags & 268435456) != 0) {
                result = result + " ACQUIRE_CAUSES_WAKEUP";
            }
            if ((this.mFlags & 536870912) == 0) {
                return result;
            }
            return result + " ON_AFTER_RELEASE";
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public int getOwnerUid() {
            return this.mOwnerUid;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public int getWakeLockFlags() {
            return this.mFlags;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public String getTagName() {
            return this.mTag;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public String getPackageName() {
            return this.mPackageName;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public long getActiveSince() {
            return this.mActiveSince;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public long getTotalTime() {
            long j;
            synchronized (PowerManagerService.this.mLock) {
                j = this.mTotalTime;
            }
            return j;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public boolean isDisable() {
            return this.mDisabled;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public boolean isDisabledByHans() {
            return this.mDisabledByHans;
        }

        @Override // com.android.server.power.IColorWakeLockEx
        public String getLockLevelStringEx() {
            return getLockLevelString();
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

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mReferenceCount != 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was finalized without being released!");
                    this.mReferenceCount = 0;
                    PowerManagerService.this.mNativeWrapper.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                }
            } finally {
                super.finalize();
            }
        }

        @Override // com.android.server.power.SuspendBlocker
        public void acquire() {
            synchronized (this) {
                this.mReferenceCount++;
                if (this.mReferenceCount == 1) {
                    if (PowerManagerService.this.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Acquiring suspend blocker \"" + this.mName + "\".");
                    }
                    Trace.asyncTraceBegin(131072, this.mTraceName, 0);
                    PowerManagerService.this.mNativeWrapper.nativeAcquireSuspendBlocker(this.mName);
                    PowerManagerService.this.mPowerMonitor.acquireSuspendBlocker(this.mName);
                }
            }
        }

        @Override // com.android.server.power.SuspendBlocker
        public void release() {
            synchronized (this) {
                this.mReferenceCount--;
                if (this.mReferenceCount == 0) {
                    if (PowerManagerService.this.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Releasing suspend blocker \"" + this.mName + "\".");
                    }
                    PowerManagerService.this.mPowerMonitor.releaseSuspendBlocker(this.mName);
                    PowerManagerService.this.mNativeWrapper.nativeReleaseSuspendBlocker(this.mName);
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

        @Override // com.android.server.power.SuspendBlocker
        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long sbToken = proto.start(fieldId);
            synchronized (this) {
                proto.write(1138166333441L, this.mName);
                proto.write(1120986464258L, this.mReferenceCount);
            }
            proto.end(sbToken);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class UidState {
        boolean mActive;
        int mNumWakeLocks;
        int mProcState;
        final int mUid;

        UidState(int uid) {
            this.mUid = uid;
        }
    }

    @VisibleForTesting
    final class BinderService extends ColorBasePowerBinderService {
        public BinderService(Context context, PowerManagerService service) {
            super(context, service);
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
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                PowerManagerService.this.powerHintInternal(hintId, data);
            }
        }

        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) {
            WorkSource ws2;
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            } else if (packageName != null) {
                PowerManager.validateWakeLockParameters(flags, tag);
                this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if ((flags & 64) != 0) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                }
                if (ws == null || ws.isEmpty()) {
                    ws2 = null;
                } else {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                    ws2 = ws;
                }
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.acquireWakeLockInternal(lock, flags, tag, packageName, ws2, historyTag, uid, pid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("packageName must not be null");
            }
        }

        public void releaseWakeLock(IBinder lock, int flags) {
            if (lock != null) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.releaseWakeLockInternal(lock, flags);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("lock must not be null");
            }
        }

        public void updateWakeLockUids(IBinder lock, int[] uids) {
            WorkSource ws = null;
            if (uids != null) {
                ws = new WorkSource();
                for (int i : uids) {
                    ws.add(i);
                }
            }
            updateWakeLockWorkSource(lock, ws, null);
        }

        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) {
            if (lock != null) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if (ws == null || ws.isEmpty()) {
                    ws = null;
                } else {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                }
                int callingUid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.updateWakeLockWorkSourceInternal(lock, ws, historyTag, callingUid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("lock must not be null");
            }
        }

        public boolean isWakeLockLevelSupported(int level) {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isWakeLockLevelSupportedInternal(level);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void userActivity(long eventTime, int event, int flags) {
            long now = SystemClock.uptimeMillis();
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.USER_ACTIVITY") != 0) {
                synchronized (PowerManagerService.this.mLock) {
                    if (now >= PowerManagerService.this.mLastWarningAboutUserActivityPermission + BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
                        long unused = PowerManagerService.this.mLastWarningAboutUserActivityPermission = now;
                        Slog.w(PowerManagerService.TAG, "Ignoring call to PowerManager.userActivity() because the caller does not have DEVICE_POWER or USER_ACTIVITY permission.  Please fix your app!   pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid());
                    }
                }
            } else if (eventTime <= now) {
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.userActivityInternal(eventTime, event, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void wakeUp(long eventTime, int reason, String details, String opPackageName) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.wakeUpInternal(eventTime, reason, details, uid, opPackageName, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void goToSleep(long eventTime, int reason, int flags) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    Slog.i(PowerManagerService.TAG, "check mAodUserSetEnable =" + PowerManagerService.this.mAodUserSetEnable + ", mFingerprintUnlock:" + PowerManagerService.this.mFingerprintUnlock);
                    PowerManagerService.this.goToSleepInternal(eventTime, reason, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void nap(long eventTime) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.napInternal(eventTime, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public boolean isInteractive() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isInteractiveInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverController.isEnabled();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getScreenState() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mOppoPowerManagerHelper.getScreenStateInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean getDisplayAodStatus() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.getDisplayAodStatusInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public PowerSaveState getPowerSaveState(int serviceType) {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPowerSaveModeEnabled(boolean enabled) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.POWER_SAVER") != 0) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.setLowPowerModeInternal(enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.POWER_SAVER", "updateDynamicPowerSavings");
            long ident = Binder.clearCallingIdentity();
            try {
                ContentResolver resolver = this.mContext.getContentResolver();
                boolean success = Settings.Global.putInt(resolver, "dynamic_power_savings_disable_threshold", disableThreshold);
                if (success) {
                    success &= Settings.Global.putInt(resolver, "dynamic_power_savings_enabled", powerSaveHint ? 1 : 0);
                }
                return success;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.POWER_SAVER") != 0) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", "setAdaptivePowerSavePolicy");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverStateMachine.setAdaptiveBatterySaverPolicy(config);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setAdaptivePowerSaveEnabled(boolean enabled) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.POWER_SAVER") != 0) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", "setAdaptivePowerSaveEnabled");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverStateMachine.setAdaptiveBatterySaverEnabled(enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getPowerSaveModeTrigger() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.POWER_SAVER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return Settings.Global.getInt(this.mContext.getContentResolver(), "automatic_power_save_mode", 0);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isDeviceIdleModeInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isLightDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isLightDeviceIdleModeInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getLastShutdownReason() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.getLastShutdownReasonInternal(PowerManagerService.REBOOT_PROPERTY);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getLastSleepReason() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.getLastSleepReasonInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void reboot(boolean confirm, String reason, boolean wait) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            if ("recovery".equals(reason) || "recovery-update".equals(reason)) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(1, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void rebootSafeMode(boolean confirm, boolean wait) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(2, confirm, "safemode", wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void shutdown(boolean confirm, String reason, boolean wait) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(0, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void crash(String message) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.crashInternal(message);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setStayOnSetting(int val) {
            int uid = Binder.getCallingUid();
            if (uid == 0 || Settings.checkAndNoteWriteSettingsOperation(this.mContext, uid, Settings.getPackageNameForUid(this.mContext, uid), true)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.setStayOnSettingInternal(val);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void setAttentionLight(boolean on, int color) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setAttentionLightInternal(on, color);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setDozeAfterScreenOff(boolean on) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setDozeAfterScreenOffInternal(on);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void boostScreenBrightness(long eventTime) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.boostScreenBrightnessInternal(eventTime, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public boolean isScreenBrightnessBoosted() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isScreenBrightnessBoostedInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean forceSuspend() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.forceSuspendInternal(uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* JADX INFO: finally extract failed */
        public void setDozeOverride(int screenState, int screenBrightness) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (PowerManagerService.this.mScreenState == 2) {
                Slog.d(PowerManagerService.TAG, "setDozeOverride screenState=" + screenState + " mScreenState=" + PowerManagerService.this.mScreenState + " color=" + screenBrightness + " invalid state");
                return;
            }
            int value = 0;
            boolean sysUiKilled = true;
            if (screenState == 1) {
                value = 0;
            } else if (screenState == 2) {
                value = 3;
            } else if (screenState == 3) {
                value = 2;
            } else if (screenState == 4) {
                value = 1;
            }
            int pid = Binder.getCallingPid();
            synchronized (PowerManagerService.this.mMapLock) {
                PowerManagerService.this.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
                if (PowerManagerService.this.mLastSystemUiPid == -1 || pid == PowerManagerService.this.mLastSystemUiPid) {
                    sysUiKilled = false;
                }
                if (PowerManagerService.this.mDozeStateMap.size() > 2 || sysUiKilled) {
                    Slog.d(PowerManagerService.TAG, "systemUI have been killed, clear map");
                    PowerManagerService.this.mDozeStateMap.clear();
                    PowerManagerService.this.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
                }
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setDozeOverrideFromDreamManagerInternal(screenState, screenBrightness);
                Binder.restoreCallingIdentity(ident);
                int unused = PowerManagerService.this.mLastSystemUiPid = pid;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public int getMinimumScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.mScreenBrightnessSettingMinimum;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getMaximumScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.mScreenBrightnessSettingMaximum;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getDefaultScreenBrightnessSetting() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mScreenBrightnessSettingDefault;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(this.mContext, PowerManagerService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                boolean isDumpProto = false;
                for (String arg : args) {
                    if (arg.equals(PriorityDump.PROTO_ARG)) {
                        isDumpProto = true;
                    }
                }
                if (!PowerManagerService.this.mOppoPowerManagerHelper.dynamicallyConfigPowerManagerServiceLogTag(fd, pw, args) && !PowerManagerService.this.dumpPossibleMusicPlayer(fd, pw, args) && !PowerManagerService.this.mOppoPowerFuncHelper.dumpShortScreenOn(fd, pw, args)) {
                    if (isDumpProto) {
                        try {
                            PowerManagerService.this.dumpProto(fd);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    } else {
                        PowerManagerService.this.dumpInternal(pw);
                    }
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public long getFrameworksBlockedTime() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mPowerMonitor.getFrameworksBlockedTime();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public Map getTopAppBlocked(int n) {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mPowerMonitor.getTopAppBlocked(n);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void stopChargeForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            if (uid == 1000 || uid == 0) {
                try {
                    PowerManagerService.this.mOppoPowerManagerHelper.updateChargeStateForSaleInternal(false);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Slog.d(PowerManagerService.TAG, "No permission to stopChargeForSale");
            }
        }

        public void resumeChargeForSale() {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            if (uid == 1000 || uid == 0) {
                try {
                    PowerManagerService.this.mOppoPowerManagerHelper.updateChargeStateForSaleInternal(true);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Slog.d(PowerManagerService.TAG, "No permission to resumeChargeForSale");
            }
        }

        public int getCurrentChargeStateForSale() {
            Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mOppoPowerManagerHelper.getCurrentChargeStateForSaleInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int[] getWakeLockedUids() {
            if (Binder.getCallingUid() == 1000) {
                long ident = Binder.clearCallingIdentity();
                try {
                    return PowerManagerService.this.getWakeLockUids();
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new SecurityException("No permission to getWakeLockedUids");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public BinderService getBinderServiceInstance() {
        return this.mBinderService;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public LocalService getLocalServiceInstance() {
        return this.mLocalService;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getLastShutdownReasonInternal(String lastRebootReasonProperty) {
        String line = SystemProperties.get(lastRebootReasonProperty);
        if (line == null) {
            return 0;
        }
        char c = 65535;
        switch (line.hashCode()) {
            case -2117951935:
                if (line.equals(REASON_THERMAL_SHUTDOWN)) {
                    c = 3;
                    break;
                }
                break;
            case -1099647817:
                if (line.equals(REASON_LOW_BATTERY)) {
                    c = 4;
                    break;
                }
                break;
            case -934938715:
                if (line.equals(REASON_REBOOT)) {
                    c = 1;
                    break;
                }
                break;
            case -852189395:
                if (line.equals(REASON_USERREQUESTED)) {
                    c = 2;
                    break;
                }
                break;
            case -169343402:
                if (line.equals(REASON_SHUTDOWN)) {
                    c = 0;
                    break;
                }
                break;
            case 1218064802:
                if (line.equals(REASON_BATTERY_THERMAL_STATE)) {
                    c = 5;
                    break;
                }
                break;
        }
        if (c == 0) {
            return 1;
        }
        if (c == 1) {
            return 2;
        }
        if (c == 2) {
            return 3;
        }
        if (c == 3) {
            return 4;
        }
        if (c == 4) {
            return 5;
        }
        if (c != 5) {
            return 0;
        }
        return 6;
    }

    /* access modifiers changed from: private */
    public int getLastSleepReasonInternal() {
        int i;
        synchronized (this.mLock) {
            i = this.mLastSleepReason;
        }
        return i;
    }

    /* access modifiers changed from: private */
    public PowerManager.WakeData getLastWakeupInternal() {
        PowerManager.WakeData wakeData;
        synchronized (this.mLock) {
            wakeData = new PowerManager.WakeData(this.mLastWakeTime, this.mLastWakeReason);
        }
        return wakeData;
    }

    private final class LocalService extends PowerManagerInternal {
        private LocalService() {
        }

        public ArrayList<Integer> getMusicPlayerList() {
            return OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).getMusicPlayerList();
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
                case 6:
                    break;
                default:
                    screenState = 0;
                    break;
            }
            if (screenBrightness < -1 || screenBrightness > PowerManager.BRIGHTNESS_MULTIBITS_ON) {
                screenBrightness = -1;
            }
            int value = 0;
            if (screenState == 1) {
                value = 0;
            } else if (screenState == 2) {
                value = 3;
            } else if (screenState == 3) {
                value = 2;
            } else if (screenState == 4) {
                value = 1;
            }
            int pid = Binder.getCallingPid();
            synchronized (PowerManagerService.this.mMapLock) {
                PowerManagerService.this.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
            }
            Slog.d(PowerManagerService.TAG, "setDozeOverrideFromDreamManager screenState:" + screenState + " screenBrightness:" + screenBrightness + " map(" + PowerManagerService.this.mDozeStateMap.size() + "," + pid + "," + value + ")");
            PowerManagerService.this.setDozeOverrideFromDreamManagerInternal(screenState, screenBrightness);
        }

        public void setUserInactiveOverrideFromWindowManager() {
            PowerManagerService.this.setUserInactiveOverrideFromWindowManagerInternal();
        }

        public void setUserActivityTimeoutOverrideFromWindowManager(long timeoutMillis) {
            PowerManagerService.this.setUserActivityTimeoutOverrideFromWindowManagerInternal(timeoutMillis);
        }

        public void setDrawWakeLockOverrideFromSidekick(boolean keepState) {
            PowerManagerService.this.setDrawWakeLockOverrideFromSidekickInternal(keepState);
        }

        public void setMaximumScreenOffTimeoutFromDeviceAdmin(int userId, long timeMs) {
            PowerManagerService.this.setMaximumScreenOffTimeoutFromDeviceAdminInternal(userId, timeMs);
        }

        public PowerSaveState getLowPowerState(int serviceType) {
            return PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType);
        }

        @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoHook.OppoRomType.ROM)
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

        public void registerLowPowerModeObserver(PowerManagerInternal.LowPowerModeListener listener) {
            PowerManagerService.this.mBatterySaverController.addListener(listener);
        }

        public String getShortScreenOnStatus() {
            return PowerManagerService.this.mOppoPowerFuncHelper.getShortScreenOnStatusInternal();
        }

        public String getScreenOnReason() {
            return PowerManagerService.this.mOppoPowerFuncHelper.mWakeupReason;
        }

        public String getSleepReason() {
            return PowerManagerService.this.mOppoPowerFuncHelper.sleepReasonToString();
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
            return PowerManagerService.this.mOppoPowerManagerHelper.isStartGoToSleep();
        }

        public void wakeUpAndBlockScreenOn(String reason) {
            PowerManagerService.this.mOppoPowerManagerHelper.wakeUpAndBlockScreenOn(reason);
        }

        public void unblockScreenOn(String reason) {
            PowerManagerService.this.mOppoPowerManagerHelper.unblockScreenOn(reason);
        }

        public void gotoSleepWhenScreenOnBlocked(String reason) {
            PowerManagerService.this.mOppoPowerManagerHelper.gotoSleepWhenScreenOnBlocked(reason);
        }

        public boolean isBiometricsWakeUpReason(String reason) {
            return PowerManagerService.this.mOppoPowerManagerHelper.isBiometricsBlockReason(reason);
        }

        public boolean isFingerprintWakeUpReason(String reason) {
            return PowerManagerService.this.mOppoPowerManagerHelper.isFingerprintBlockReason(reason);
        }

        public boolean isFaceWakeUpReason(String reason) {
            return PowerManagerService.this.mOppoPowerManagerHelper.isFaceBlockReason(reason);
        }

        public boolean isBlockedByFace() {
            return PowerManagerService.this.mOppoPowerManagerHelper.hasBlockedByFace();
        }

        public boolean isBlockedByFingerprint() {
            return PowerManagerService.this.mOppoPowerManagerHelper.hasBlockedByFingerprint();
        }

        public boolean wasDeviceIdleFor(long ms) {
            return PowerManagerService.this.wasDeviceIdleForInternal(ms);
        }

        public PowerManager.WakeData getLastWakeup() {
            return PowerManagerService.this.getLastWakeupInternal();
        }

        public void notifyMotionGameAppForeground(String packageName, boolean foreground) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mOppoPowerManagerHelper.notifyMotionGameAppForegroundLocked(packageName, foreground);
            }
        }
    }

    class OppoHelper {
        private static final int BUTTON_LIGHT_BRIGHTNESS = 102;
        Light mButtonLight;

        /* access modifiers changed from: package-private */
        public int getUserActivitySumm() {
            return PowerManagerService.this.mUserActivitySummary;
        }

        /* access modifiers changed from: package-private */
        public int getWakefulness() {
            return PowerManagerService.this.mWakefulness;
        }

        /* access modifiers changed from: package-private */
        public int getScreenBrightDefault() {
            return PowerManagerService.this.mScreenBrightnessSettingDefault;
        }

        public OppoHelper(LightsManager mLightsManager) {
            this.mButtonLight = mLightsManager.getLight(2);
        }

        /* access modifiers changed from: package-private */
        public void updateButtonBrightness(PowerManagerService service, boolean condition) {
            if (!PowerManagerService.mOppoShutdownIng && !PowerManagerService.this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                int tmp = getUserActivitySumm();
                boolean b = false;
                boolean b1 = (tmp & 2) != 0;
                boolean b2 = tmp == 0;
                if (getWakefulness() == 1) {
                    b = true;
                }
                if (!condition) {
                    this.mButtonLight.setBrightness(102);
                } else if (b && !b1 && !b2) {
                    this.mButtonLight.setBrightness(102);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void turnOffButtonLight() {
            this.mButtonLight.turnOff();
        }
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

    /* access modifiers changed from: private */
    public boolean dumpPossibleMusicPlayer(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1) {
            return false;
        }
        String cmd = args[0];
        if ("possibleMusicPlayer".equals(cmd)) {
            OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).dumpPossibleMusicPlayer(pw);
            return true;
        } else if ("wakelockCheck".equals(cmd) && "log".equals(args[1])) {
            if ("on".equals(args[2])) {
                OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).logSwitch(true);
                pw.println("wakelockCheck log on.");
            } else if ("off".equals(args[2])) {
                OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).logSwitch(false);
                pw.println("wakelockCheck log off.");
            }
            return true;
        } else if (!"cameraState".equals(cmd)) {
            return false;
        } else {
            OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).dumpCameraState(pw);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public int[] getWakeLockUids() {
        List<Integer> uidList = new ArrayList<>();
        synchronized (this.mLock) {
            int N = this.mWakeLocks.size();
            for (int i = 0; i < N; i++) {
                WakeLock wl = this.mWakeLocks.get(i);
                uidList.add(Integer.valueOf(wl.mOwnerUid));
                WorkSource ws = wl.mWorkSource;
                if (ws != null) {
                    for (int j = 0; j < ws.size(); j++) {
                        uidList.add(Integer.valueOf(ws.get(j)));
                    }
                }
            }
        }
        int length = uidList.size();
        int[] res = new int[length];
        for (int i2 = 0; i2 < length; i2++) {
            res[i2] = uidList.get(i2).intValue();
        }
        return res;
    }

    public final class ColorPowerManagerServiceInner implements IColorPowerManagerServiceInner {
        public ColorPowerManagerServiceInner() {
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public boolean isInteractiveInternal() {
            return PowerManagerService.this.isInteractiveInternal();
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void setUserActivityValue(long value) {
            long unused = PowerManagerService.this.mUserActivityTimeoutOverrideFromWindowManager = value;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getUserActivityValue() {
            return PowerManagerService.this.mUserActivityTimeoutOverrideFromWindowManager;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getScreenOffTimeoutSettingValue() {
            return PowerManagerService.this.mScreenOffTimeoutSetting;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public int getMsgUserActivityTimeoutValue() {
            return 1;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getSleepTimeoutLocked() {
            return PowerManagerService.this.getSleepTimeoutLocked();
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getScreenOffTimeoutLocked(long sleepTimeout) {
            return PowerManagerService.this.getScreenOffTimeoutLocked(sleepTimeout);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public ArrayList<IColorWakeLockEx> getArrayListOfWakeLocks() {
            ArrayList<IColorWakeLockEx> mIColorWakeLockExs = new ArrayList<>();
            Iterator wakeLockIterator = PowerManagerService.this.mWakeLocks.iterator();
            while (wakeLockIterator.hasNext()) {
                mIColorWakeLockExs.add((IColorWakeLockEx) wakeLockIterator.next());
            }
            return mIColorWakeLockExs;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void setKeyguardLockEverUnlockValue(Boolean value) {
            PowerManagerService.this.mOppoPowerFuncHelper.mKeyguardLockEverUnlock.set(value.booleanValue());
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getLastWakeTime() {
            return PowerManagerService.this.mLastWakeTime;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public long getLastSleepTime() {
            return PowerManagerService.this.mLastSleepTime;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public boolean getDisabledByHans(WakeLock wakeLock) {
            return wakeLock.mDisabledByHans;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void setDisableByHans(WakeLock wakeLock, boolean value) {
            wakeLock.mDisabledByHans = value;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public boolean setWakeLockDisabledStateLocked(WakeLock wakeLock) {
            return PowerManagerService.this.setWakeLockDisabledStateLocked(wakeLock);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
            PowerManagerService.this.notifyWakeLockReleasedLocked(wakeLock);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
            PowerManagerService.this.notifyWakeLockAcquiredLocked(wakeLock);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void updatePowerStateLocked() {
            PowerManagerService.this.updatePowerStateLocked();
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void updateDirtyByHans() {
            PowerManagerService.access$1676(PowerManagerService.this, 1);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void releaseWakeLockInternal(IBinder lock, int flags) {
            PowerManagerService.this.releaseWakeLockInternal(lock, flags);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public WakeLock cloneWakeLock(WakeLock wl) {
            return new WakeLock(wl.mLock, wl.mFlags, wl.mTag, wl.mPackageName, wl.mWorkSource, wl.mHistoryTag, wl.mOwnerUid, wl.mOwnerPid, wl.mUidState);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
            PowerManagerService.this.goToSleepInternal(eventTime, reason, flags, uid);
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public int getwakefulness() {
            return PowerManagerService.this.mWakefulness;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public boolean needScreenOnWakelockCheck() {
            if (PowerManagerService.this.mWakefulness == 1 && PowerManagerService.this.mUserActivitySummary == 4 && !PowerManagerService.this.mHandler.hasMessages(1)) {
                return true;
            }
            return false;
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public boolean isForgroundUid(int uid) {
            synchronized (PowerManagerService.this.mLock) {
                UidState state = (UidState) PowerManagerService.this.mUidState.get(uid);
                if (state == null) {
                    return false;
                }
                if (state.mProcState <= 2) {
                    return true;
                }
                return false;
            }
        }

        @Override // com.android.server.power.IColorPowerManagerServiceInner
        public int getUserActivitySummary() {
            return PowerManagerService.this.mUserActivitySummary;
        }
    }

    public void goToSleepExported(long eventTime, int reason, int flags, int uid) {
        goToSleepInternal(eventTime, reason, flags, uid);
    }

    public boolean getProximityPositiveValueLocked() {
        return this.mProximityPositive;
    }

    /* access modifiers changed from: package-private */
    public void oppoGoToSleepInternal(long eventTime, int reason, int flags, int uid) {
        goToSleepInternal(eventTime, reason, flags, uid);
    }

    /* access modifiers changed from: package-private */
    public void oppoUserActivityNoUpdateLocked(long eventTime, int event, int flags, int uid) {
        userActivityNoUpdateLocked(eventTime, event, flags, uid);
    }

    /* access modifiers changed from: package-private */
    public void oppoWakeUpInternal(long eventTime, int reasonId, String reason, int uid, String opPackageName, int opUid) {
        wakeUpInternal(eventTime, reasonId, reason, uid, opPackageName, opUid);
    }

    /* access modifiers changed from: package-private */
    public void removeScreenBrightnessBoostMessage() {
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
        }
    }

    /* access modifiers changed from: package-private */
    public void turnOffButtonLight() {
        OppoHelper oppoHelper = this.mOppoHelper;
        if (oppoHelper != null) {
            oppoHelper.turnOffButtonLight();
        }
    }

    /* access modifiers changed from: package-private */
    public void setOppoShutdownIngStatus(boolean shutdowning) {
        mOppoShutdownIng = shutdowning;
    }

    /* access modifiers changed from: package-private */
    public boolean isShouldEnterOppoAod() {
        return isShouldGoAod();
    }

    /* access modifiers changed from: package-private */
    public void scheduleNotifySfUnBlockScreenOn() {
        notifySfUnBlockScreenOn();
    }

    /* access modifiers changed from: package-private */
    public void scheduleNotifyStopDream() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(102));
    }

    /* access modifiers changed from: package-private */
    public void removeScreenBrightnessBoost() {
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
        }
    }

    /* access modifiers changed from: package-private */
    public int getLastUpdatedBrightness() {
        int lastScreenBrightness;
        if (this.mScreenBrightnessSetting > 0) {
            lastScreenBrightness = this.mScreenBrightnessSetting;
        } else {
            lastScreenBrightness = this.mScreenBrightnessSettingDefault;
        }
        if (this.mScreenBrightnessOverrideFromWindowManager != -1) {
            lastScreenBrightness = this.mScreenBrightnessOverrideFromWindowManager;
        }
        Slog.d(TAG, "get last Brightness:" + lastScreenBrightness + "  mScreenBrightnessOverrideFromWindowManager:" + this.mScreenBrightnessOverrideFromWindowManager);
        return lastScreenBrightness;
    }

    /* access modifiers changed from: package-private */
    public boolean checkProximityScreenOffWakeLockAcquired() {
        if ((this.mWakeLockSummary & 16) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsStartGoToSleep() {
        return this.mOppoPowerManagerHelper.isStartGoToSleep();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public void onTmpWakeUpAndBlockScreenOn(String reason) {
        this.mOppoPowerManagerHelper.wakeUpAndBlockScreenOn(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public void onTmpUnblockScreenOn(String reason) {
        this.mOppoPowerManagerHelper.unblockScreenOn(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public void onTmpGotoSleepWhenScreenOnBlocked(String reason) {
        this.mOppoPowerManagerHelper.gotoSleepWhenScreenOnBlocked(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsBiometricsWakeUpReason(String reason) {
        return this.mOppoPowerManagerHelper.isBiometricsBlockReason(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsFingerprintWakeUpReason(String reason) {
        return this.mOppoPowerManagerHelper.isFingerprintBlockReason(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsFaceWakeUpReason(String reason) {
        return this.mOppoPowerManagerHelper.isFaceBlockReason(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsBlockedByFace() {
        return this.mOppoPowerManagerHelper.hasBlockedByFace();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public boolean onTmpIsBlockedByFingerprint() {
        return this.mOppoPowerManagerHelper.hasBlockedByFingerprint();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.power.OppoBasePowerManagerService
    public void onTmpNotifyMotionGameAppForeground(String packageName, boolean foregrounds) {
        this.mOppoPowerManagerHelper.notifyMotionGameAppForegroundLocked(packageName, foregrounds);
    }
}
