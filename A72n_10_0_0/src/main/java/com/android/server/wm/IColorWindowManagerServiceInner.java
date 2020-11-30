package com.android.server.wm;

public interface IColorWindowManagerServiceInner {
    default void updateAppOpsState() {
    }

    default void updateAppOpsState(String packageName, Boolean state) {
    }

    default WindowState getFocusedWindow() {
        return null;
    }

    default String getFocusWindowPkgName() {
        return "";
    }

    default void resetAnimationSetting() {
    }
}
