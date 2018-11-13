package android.os;

import android.content.Context;
import android.os.IDeviceIdleController.Stub;
import android.util.Log;
import java.util.Map;

public final class PowerManager {
    public static final int ACQUIRE_CAUSES_WAKEUP = 268435456;
    public static final String ACTION_DEVICE_IDLE_MODE_CHANGED = "android.os.action.DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED = "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL = "android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL";
    public static final String ACTION_POWER_SAVE_MODE_CHANGING = "android.os.action.POWER_SAVE_MODE_CHANGING";
    public static final String ACTION_POWER_SAVE_TEMP_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED";
    public static final String ACTION_POWER_SAVE_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_WHITELIST_CHANGED";
    @Deprecated
    public static final String ACTION_SCREEN_BRIGHTNESS_BOOST_CHANGED = "android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED";
    public static final int BRIGHTNESS_DEFAULT = -1;
    private static final int BRIGHTNESS_ELEVEN_BITS = 2047;
    public static int BRIGHTNESS_MULTIBITS_ON = 255;
    public static final int BRIGHTNESS_OFF = 0;
    public static final int BRIGHTNESS_ON = 255;
    private static final int BRIGHTNESS_TEN_BITS = 1023;
    public static final int DOZE_WAKE_LOCK = 64;
    public static final int DRAW_WAKE_LOCK = 128;
    public static final String EXTRA_POWER_SAVE_MODE = "mode";
    @Deprecated
    public static final int FULL_WAKE_LOCK = 26;
    public static final int GO_TO_SLEEP_FLAG_NO_DOZE = 1;
    public static final int GO_TO_SLEEP_REASON_APPLICATION = 0;
    public static final int GO_TO_SLEEP_REASON_DEVICE_ADMIN = 1;
    public static final int GO_TO_SLEEP_REASON_FINGERPRINT = 99;
    public static final int GO_TO_SLEEP_REASON_HDMI = 5;
    public static final int GO_TO_SLEEP_REASON_LID_SWITCH = 3;
    public static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    public static final int GO_TO_SLEEP_REASON_PROXIMITY = 7;
    public static final int GO_TO_SLEEP_REASON_SLEEP_BUTTON = 6;
    public static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    public static final int ON_AFTER_RELEASE = 536870912;
    public static final int PARTIAL_WAKE_LOCK = 1;
    public static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final String REBOOT_QUIESCENT = "quiescent";
    public static final String REBOOT_RECOVERY = "recovery";
    public static final String REBOOT_RECOVERY_UPDATE = "recovery-update";
    public static final String REBOOT_REQUESTED_BY_DEVICE_OWNER = "deviceowner";
    public static final String REBOOT_SAFE_MODE = "safemode";
    public static final int RELEASE_FLAG_TIMEOUT = 65536;
    public static final int RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY = 1;
    @Deprecated
    public static final int SCREEN_BRIGHT_WAKE_LOCK = 10;
    @Deprecated
    public static final int SCREEN_DIM_WAKE_LOCK = 6;
    public static final int SHUTDOWN_REASON_REBOOT = 2;
    public static final int SHUTDOWN_REASON_SHUTDOWN = 1;
    public static final int SHUTDOWN_REASON_THERMAL_SHUTDOWN = 4;
    public static final int SHUTDOWN_REASON_UNKNOWN = 0;
    public static final int SHUTDOWN_REASON_USER_REQUESTED = 3;
    public static final String SHUTDOWN_USER_REQUESTED = "userrequested";
    private static final String TAG = "PowerManager";
    public static final int UNIMPORTANT_FOR_LOGGING = 1073741824;
    public static final int USER_ACTIVITY_EVENT_ACCESSIBILITY = 3;
    public static final int USER_ACTIVITY_EVENT_BUTTON = 1;
    public static final int USER_ACTIVITY_EVENT_OTHER = 0;
    public static final int USER_ACTIVITY_EVENT_TOUCH = 2;
    public static final int USER_ACTIVITY_FLAG_INDIRECT = 2;
    public static final int USER_ACTIVITY_FLAG_NO_CHANGE_LIGHTS = 1;
    public static final int WAKE_LOCK_LEVEL_MASK = 65535;
    public static final String WAKE_UP_DUE_TO_DOUBLE_HOME = "android.service.fingerprint:DOUBLE_HOME";
    public static final String WAKE_UP_DUE_TO_DOUBLE_TAP_SCREEN = "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN";
    public static final String WAKE_UP_DUE_TO_FINGERPRINT = "android.service.fingerprint:WAKEUP";
    public static final String WAKE_UP_DUE_TO_LIFT_HAND = "oppo.wakeup.gesture:LIFT_HAND";
    public static final String WAKE_UP_DUE_TO_WINDOWMANAGER = "android.server.wm:TURN_ON";
    public static final int WAKE_UP_REASON_FINGERPRINT = 98;
    final Context mContext;
    final Handler mHandler;
    IDeviceIdleController mIDeviceIdleController;
    final IPowerManager mService;

    public final class WakeLock {
        private int mExternalCount;
        private int mFlags;
        private boolean mHeld;
        private String mHistoryTag;
        private int mInternalCount;
        private final String mPackageName;
        private boolean mRefCounted = true;
        private final Runnable mReleaser = new Runnable() {
            public void run() {
                WakeLock.this.release(65536);
            }
        };
        private String mTag;
        private final IBinder mToken;
        private final String mTraceName;
        private WorkSource mWorkSource;

        WakeLock(int flags, String tag, String packageName) {
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mToken = new Binder();
            this.mTraceName = "WakeLock (" + this.mTag + ")";
        }

        protected void finalize() throws Throwable {
            synchronized (this.mToken) {
                if (this.mHeld) {
                    Log.wtf(PowerManager.TAG, "WakeLock finalized while still held: " + this.mTag);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                    try {
                        PowerManager.this.mService.releaseWakeLock(this.mToken, 0);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }

        public void setReferenceCounted(boolean value) {
            synchronized (this.mToken) {
                this.mRefCounted = value;
            }
        }

        public void acquire() {
            synchronized (this.mToken) {
                acquireLocked();
            }
        }

        public void acquire(long timeout) {
            synchronized (this.mToken) {
                acquireLocked();
                PowerManager.this.mHandler.postDelayed(this.mReleaser, timeout);
            }
        }

        private void acquireLocked() {
            this.mInternalCount++;
            this.mExternalCount++;
            if (!this.mRefCounted || this.mInternalCount == 1) {
                PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                Trace.asyncTraceBegin(131072, this.mTraceName, 0);
                try {
                    PowerManager.this.mService.acquireWakeLock(this.mToken, this.mFlags, this.mTag, this.mPackageName, this.mWorkSource, this.mHistoryTag);
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void release() {
            release(0);
        }

        public void release(int flags) {
            synchronized (this.mToken) {
                this.mInternalCount--;
                if ((65536 & flags) == 0) {
                    this.mExternalCount--;
                }
                if (!this.mRefCounted || this.mInternalCount == 0) {
                    PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                    if (this.mHeld) {
                        Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                        try {
                            PowerManager.this.mService.releaseWakeLock(this.mToken, flags);
                            this.mHeld = false;
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                }
                if (!this.mRefCounted || this.mExternalCount >= 0) {
                } else {
                    throw new RuntimeException("WakeLock under-locked " + this.mTag);
                }
            }
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mToken) {
                z = this.mHeld;
            }
            return z;
        }

        public void setWorkSource(WorkSource ws) {
            synchronized (this.mToken) {
                boolean changed;
                if (ws != null) {
                    if (ws.size() == 0) {
                        ws = null;
                    }
                }
                if (ws == null) {
                    changed = this.mWorkSource != null;
                    this.mWorkSource = null;
                } else if (this.mWorkSource == null) {
                    changed = true;
                    this.mWorkSource = new WorkSource(ws);
                } else {
                    changed = this.mWorkSource.diff(ws);
                    if (changed) {
                        this.mWorkSource.set(ws);
                    }
                }
                if (changed && this.mHeld) {
                    try {
                        PowerManager.this.mService.updateWakeLockWorkSource(this.mToken, this.mWorkSource, this.mHistoryTag);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }

        public void setTag(String tag) {
            this.mTag = tag;
        }

        public String getTag() {
            return this.mTag;
        }

        public void setHistoryTag(String tag) {
            this.mHistoryTag = tag;
        }

        public void setUnimportantForLogging(boolean state) {
            if (state) {
                this.mFlags |= 1073741824;
            } else {
                this.mFlags &= -1073741825;
            }
        }

        public String toString() {
            String str;
            synchronized (this.mToken) {
                str = "WakeLock{" + Integer.toHexString(System.identityHashCode(this)) + " held=" + this.mHeld + ", refCount=" + this.mInternalCount + "}";
            }
            return str;
        }

        public Runnable wrap(Runnable r) {
            acquire();
            return new -$Lambda$OsaxDBgigpqjZN1F4C6nYRYm1YQ(this, r);
        }

        /* renamed from: lambda$-android_os_PowerManager$WakeLock_69275 */
        /* synthetic */ void m23lambda$-android_os_PowerManager$WakeLock_69275(Runnable r) {
            try {
                r.run();
            } finally {
                release();
            }
        }
    }

    public PowerManager(Context context, IPowerManager service, Handler handler) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = handler;
    }

    public int getMinBrightness() {
        try {
            return this.mService.getMinimumScreenBrightnessSetting();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getMaxBrightness() {
        try {
            BRIGHTNESS_MULTIBITS_ON = this.mService.getMaximumScreenBrightnessSetting();
            return BRIGHTNESS_MULTIBITS_ON;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getDefaultBrightness() {
        try {
            return this.mService.getDefaultScreenBrightnessSetting();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getMinimumScreenBrightnessSetting() {
        try {
            return this.mService.getMinimumScreenBrightnessSetting();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getMaximumScreenBrightnessSetting() {
        return 255;
    }

    public int getDefaultScreenBrightnessSetting() {
        try {
            int max = this.mService.getMaximumScreenBrightnessSetting();
            if (max == 2047) {
                return this.mService.getDefaultScreenBrightnessSetting() / 8;
            }
            if (max == 1023) {
                return this.mService.getDefaultScreenBrightnessSetting() / 4;
            }
            return this.mService.getDefaultScreenBrightnessSetting();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getMinimumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694848);
    }

    public int getMaximumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694847);
    }

    public int getDefaultScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694846);
    }

    public WakeLock newWakeLock(int levelAndFlags, String tag) {
        validateWakeLockParameters(levelAndFlags, tag);
        return new WakeLock(levelAndFlags, tag, this.mContext.getOpPackageName());
    }

    public static void validateWakeLockParameters(int levelAndFlags, String tag) {
        switch (65535 & levelAndFlags) {
            case 1:
            case 6:
            case 10:
            case 26:
            case 32:
            case 64:
            case 128:
                if (tag == null) {
                    throw new IllegalArgumentException("The tag must not be null.");
                }
                return;
            default:
                throw new IllegalArgumentException("Must specify a valid wake lock level.");
        }
    }

    @Deprecated
    public void userActivity(long when, boolean noChangeLights) {
        int i;
        if (noChangeLights) {
            i = 1;
        } else {
            i = 0;
        }
        userActivity(when, 0, i);
    }

    public void userActivity(long when, int event, int flags) {
        try {
            this.mService.userActivity(when, event, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void goToSleep(long time) {
        goToSleep(time, 0, 0);
    }

    public void goToSleep(long time, int reason, int flags) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "goToSleepBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.goToSleep(time, reason, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wakeUp(long time) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "wakeUpBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.wakeUp(time, "wakeUp", this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wakeUp(long time, String reason) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "wakeUpBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.wakeUp(time, reason, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void nap(long time) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "napBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.nap(time);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void boostScreenBrightness(long time) {
        try {
            this.mService.boostScreenBrightness(time);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isScreenBrightnessBoosted() {
        return false;
    }

    public void setBacklightBrightness(int brightness) {
        try {
            this.mService.setTemporaryScreenBrightnessSettingOverride(Math.round((float) ((brightness * this.mService.getMaximumScreenBrightnessSetting()) / 255)));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWakeLockLevelSupported(int level) {
        try {
            return this.mService.isWakeLockLevelSupported(level);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isScreenOn() {
        return isInteractive();
    }

    public boolean isInteractive() {
        try {
            return this.mService.isInteractive();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reboot(String reason) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "rebootBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.reboot(false, reason, true);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void rebootSafeMode() {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "rebootSafeModeBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.rebootSafeMode(false, true);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPowerSaveMode() {
        try {
            return this.mService.isPowerSaveMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getDisplayAodStatus() {
        try {
            return this.mService.getDisplayAodStatus();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPowerSaveMode(boolean mode) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "setPowerSaveModeBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            return this.mService.setPowerSaveMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isColorOsPowerSaveMode() {
        try {
            return this.mService.isColorOsPowerSaveMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setColorOsPowerSaveMode(boolean mode) {
        try {
            return this.mService.setColorOsPowerSaveMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PowerSaveState getPowerSaveState(int serviceType) {
        try {
            return this.mService.getPowerSaveState(serviceType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getScreenState() {
        try {
            return this.mService.getScreenState();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public boolean isDeviceIdleMode() {
        try {
            return this.mService.isDeviceIdleMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLightDeviceIdleMode() {
        try {
            return this.mService.isLightDeviceIdleMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isIgnoringBatteryOptimizations(String packageName) {
        synchronized (this) {
            if (this.mIDeviceIdleController == null) {
                this.mIDeviceIdleController = Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
            }
        }
        try {
            return this.mIDeviceIdleController.isPowerSaveWhitelistApp(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void shutdown(boolean confirm, String reason, boolean wait) {
        if (Context.USER_SERVICE.equals(Build.TYPE)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack != null) {
                int i = 0;
                while (i < 5 && i < stack.length) {
                    Log.i(TAG, "shutdownBy    |----" + stack[i].toString());
                    i++;
                }
            }
        }
        try {
            this.mService.shutdown(confirm, reason, wait);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getFrameworksBlockedTime() {
        try {
            return this.mService.getFrameworksBlockedTime();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<String, Long> getTopAppBlocked(int n) {
        try {
            return this.mService.getTopAppBlocked(n);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopChargeForSale() {
        try {
            this.mService.stopChargeForSale();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resumeChargeForSale() {
        try {
            this.mService.resumeChargeForSale();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getCurrentChargeStateForSale() {
        try {
            return this.mService.getCurrentChargeStateForSale();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSustainedPerformanceModeSupported() {
        return this.mContext.getResources().getBoolean(17957038);
    }

    public int getLastShutdownReason() {
        try {
            return this.mService.getLastShutdownReason();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
