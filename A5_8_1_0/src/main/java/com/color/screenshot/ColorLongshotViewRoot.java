package com.color.screenshot;

import com.color.util.ColorLog;

public class ColorLongshotViewRoot {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private boolean mIsConnected = false;

    public void setConnected(boolean isConnected) {
        ColorLog.d(DBG, "LongshotDump", "setConnected : " + isConnected);
        this.mIsConnected = isConnected;
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }
}
