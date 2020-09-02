package com.android.server.power;

import android.content.ContentResolver;

public interface IPswPowerManagerServiceAODInner {
    public static final IPswPowerManagerServiceAODInner DEFAULT = new IPswPowerManagerServiceAODInner() {
        /* class com.android.server.power.IPswPowerManagerServiceAODInner.AnonymousClass1 */
    };

    default void setDozeAfterScreenOff(boolean on) {
    }

    default void setDecoupleHalAutoSuspendModeFromDisplayConfig(boolean on) {
    }

    default void setDecoupleHalInteractiveModeFromDisplayConfig(boolean on) {
    }

    default void setDreamsEnabledSetting(boolean on) {
    }

    default void setDreamsActivateOnSleepSetting(boolean on) {
    }

    default void setAlwaysOnEnabled(boolean on) {
    }

    default void systemReady(ContentResolver resolver) {
    }

    default void onDisplayStateChange(int state) {
    }
}
