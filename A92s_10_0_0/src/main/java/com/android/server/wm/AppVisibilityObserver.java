package com.android.server.wm;

public interface AppVisibilityObserver {
    default void onAppVisible(ActivityRecord r) {
    }
}
