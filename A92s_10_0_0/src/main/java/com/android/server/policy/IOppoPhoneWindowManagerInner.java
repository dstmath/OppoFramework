package com.android.server.policy;

import com.android.server.policy.OppoBasePhoneWindowManager;

public interface IOppoPhoneWindowManagerInner {
    public static final IOppoPhoneWindowManagerInner DEFAULT = new IOppoPhoneWindowManagerInner() {
        /* class com.android.server.policy.IOppoPhoneWindowManagerInner.AnonymousClass1 */
    };

    default void cancelPreloadRecentApps() {
    }

    default Object getLock() {
        return new Object();
    }

    default void powerPress(long eventTime, boolean interactive, int count) {
    }

    default void cancelPendingPowerKeyAction() {
    }

    default void launchAssistAction(String hint, int deviceId, OppoBasePhoneWindowManager.AssistManagerLaunchMode launchMode) {
    }

    default void wakeUpFromPowerKey(long eventTime) {
    }
}
