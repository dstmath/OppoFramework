package com.android.server.wm;

public interface IColorWindowStateInner {
    public static final IColorWindowStateInner DEFAULT = new IColorWindowStateInner() {
        /* class com.android.server.wm.IColorWindowStateInner.AnonymousClass1 */
    };

    default boolean getAppOpVisibility() {
        return false;
    }

    default void setAppOpVisibilityLw(boolean state) {
    }
}
