package com.color.screenshot;

import com.color.util.ColorLog;

public class ColorLongshotViewRoot {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private boolean mIsConnected = false;

    public void setConnected(boolean isConnected) {
        boolean z = DBG;
        ColorLog.d(z, "LongshotDump", "setConnected : " + isConnected);
        this.mIsConnected = isConnected;
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }
}
