package com.android.server.wm;

public interface IColorActivityStackSupervisorInner {
    default <T extends ActivityStack> T getStack(int windowingMode, int activityType) {
        return null;
    }
}
