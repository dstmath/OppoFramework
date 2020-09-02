package com.android.server.wm;

public class ColorDirectMainWindow {
    private final WindowState mMainWindow;

    public ColorDirectMainWindow(WindowState win) {
        this.mMainWindow = win;
    }

    public WindowState getMainWindow() {
        return this.mMainWindow;
    }
}
