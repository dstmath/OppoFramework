package com.android.server;

import android.content.Context;
import android.content.Intent;

public class OppoMasterClearThread extends Thread {
    private static final String TAG = "OppoMasterClearThread";
    private final IColorMasterClearEx mColorMasterClearEx;
    private final Thread mThread;

    public OppoMasterClearThread(Context context, String reason, boolean shutdown, boolean forceWipe, boolean wipeEsims, Intent intent, Thread thread) {
        super(thread.getName());
        this.mThread = thread;
        this.mColorMasterClearEx = ColorServiceFactory.getInstance().getColorMasterClearEx(context);
    }

    public void run() {
        IColorMasterClearEx iColorMasterClearEx = this.mColorMasterClearEx;
        if (iColorMasterClearEx != null) {
            iColorMasterClearEx.run();
        }
        this.mThread.run();
    }
}
