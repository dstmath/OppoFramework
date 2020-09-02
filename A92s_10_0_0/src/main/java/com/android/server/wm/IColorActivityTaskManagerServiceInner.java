package com.android.server.wm;

public interface IColorActivityTaskManagerServiceInner {
    default boolean getShowDialogs() {
        return false;
    }
}
