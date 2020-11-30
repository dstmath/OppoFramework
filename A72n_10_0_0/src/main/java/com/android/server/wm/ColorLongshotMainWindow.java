package com.android.server.wm;

import android.graphics.Rect;

public class ColorLongshotMainWindow {
    public static final int FLAG_SECURE = 1;
    public static final int FLAG_UNSUPPORT = 4;
    public static final int FLAG_VOLUME = 2;
    private int mFlags = 0;
    private final WindowState mMainWindow;

    public ColorLongshotMainWindow(WindowState win) {
        this.mMainWindow = win;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        dumpWindow(sb);
        dumpHex(sb, "mFlags", this.mFlags);
        return sb.toString();
    }

    public WindowState getMainWindow() {
        return this.mMainWindow;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public boolean hasUnsupported() {
        return hasFlags(4);
    }

    public boolean hasVolume() {
        return hasFlags(2);
    }

    public boolean hasSecure() {
        return hasFlags(1);
    }

    public Rect getContentFrame() {
        return this.mMainWindow.getContentFrameLw();
    }

    private boolean hasFlags(int flags) {
        return (this.mFlags & flags) == flags;
    }

    private void dumpWindow(StringBuilder sb) {
        sb.append("[MainWindow][");
        sb.append(this.mMainWindow);
        sb.append("]");
        sb.append(this.mMainWindow.getContentFrameLw());
        sb.append(this.mMainWindow.getAttrs());
    }

    private void dumpHex(StringBuilder sb, String name, int hex) {
        sb.append(", ");
        sb.append(name);
        sb.append("=");
        sb.append(String.format("0x%08x", Integer.valueOf(hex)));
    }
}
