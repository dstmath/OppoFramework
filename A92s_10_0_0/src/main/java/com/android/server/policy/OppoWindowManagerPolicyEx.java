package com.android.server.policy;

import android.util.Slog;

public interface OppoWindowManagerPolicyEx {
    default void onWakeUp(String wakeUpReason) {
        Slog.w("OppoWindowManagerPolicyEx", "default onWakeUp, must override by sub class!");
    }
}
