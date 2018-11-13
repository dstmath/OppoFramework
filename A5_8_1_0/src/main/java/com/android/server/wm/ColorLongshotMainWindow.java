package com.android.server.wm;

public class ColorLongshotMainWindow {
    private final boolean mIsUnsupported;
    private final WindowState mMainWindow;

    public ColorLongshotMainWindow(WindowState win, boolean isUnsupported) {
        this.mMainWindow = win;
        this.mIsUnsupported = isUnsupported;
    }

    public WindowState getMainWindow() {
        return this.mMainWindow;
    }

    public boolean isUnsupported() {
        return this.mIsUnsupported;
    }
}
