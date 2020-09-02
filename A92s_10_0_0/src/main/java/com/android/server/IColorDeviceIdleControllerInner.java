package com.android.server;

import android.util.ArrayMap;

public interface IColorDeviceIdleControllerInner {
    default int getState() {
        return 0;
    }

    default void setState(int mState) {
    }

    default boolean getDeepEnabled() {
        return false;
    }

    default void setDeepEnabled(boolean mDeepEnabled) {
    }

    default long getNextIdlePendingDelay() {
        return 0;
    }

    default void setNextIdlePendingDelay(long mNextIdlePendingDelay) {
    }

    default long getNextIdleDelay() {
        return 0;
    }

    default void setNextIdleDelay(long mNextIdleDelay) {
    }

    default void resetIdleManagementLocked() {
    }

    default ArrayMap<String, Integer> getPowerSaveWhitelistUserApps() {
        return null;
    }
}
