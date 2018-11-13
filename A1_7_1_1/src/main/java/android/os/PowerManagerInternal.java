package android.os;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;

public abstract class PowerManagerInternal {
    public static final int POWER_HINT_INTERACTION = 2;
    public static final int POWER_HINT_LAUNCH = 8;
    public static final int POWER_HINT_SUSTAINED_PERFORMANCE_MODE = 6;
    public static final int WAKEFULNESS_ASLEEP = 0;
    public static final int WAKEFULNESS_AWAKE = 1;
    public static final int WAKEFULNESS_DOZING = 3;
    public static final int WAKEFULNESS_DREAMING = 2;

    public interface LowPowerModeListener {
        void onLowPowerModeChanged(boolean z);
    }

    public abstract boolean getColorOsLowPowerModeEnabled();

    public abstract boolean getLowPowerModeEnabled();

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    public abstract int[] getWakeLockedPids();

    public abstract boolean isStartGoToSleep();

    public abstract void powerHint(int i, int i2);

    public abstract void registerColorOsLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract void registerLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract void setButtonBrightnessOverrideFromWindowManager(int i);

    public abstract boolean setDeviceIdleMode(boolean z);

    public abstract void setDeviceIdleTempWhitelist(int[] iArr);

    public abstract void setDeviceIdleWhitelist(int[] iArr);

    public abstract void setDozeOverrideFromDreamManager(int i, int i2);

    public abstract boolean setLightDeviceIdleMode(boolean z);

    public abstract void setMaximumScreenOffTimeoutFromDeviceAdmin(int i);

    public abstract void setScreenBrightnessOverrideFromWindowManager(int i);

    public abstract void setUserActivityTimeoutOverrideFromWindowManager(long j);

    public abstract void setUserInactiveOverrideFromWindowManager();

    public abstract void uidGone(int i);

    public abstract void updateUidProcState(int i, int i2);

    public static String wakefulnessToString(int wakefulness) {
        switch (wakefulness) {
            case 0:
                return "Asleep";
            case 1:
                return "Awake";
            case 2:
                return "Dreaming";
            case 3:
                return "Dozing";
            default:
                return Integer.toString(wakefulness);
        }
    }

    public static boolean isInteractive(int wakefulness) {
        return wakefulness == 1 || wakefulness == 2;
    }
}
