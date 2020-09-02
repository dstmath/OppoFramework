package com.android.server;

import android.content.Context;
import android.util.Slog;

public class PswSystemServerEx extends OppoDummySystemServerEx implements IPswSystemServerEx {
    private static final String TAG = "PswSystemServerEx";

    public PswSystemServerEx(Context context) {
        super(context);
    }

    public void startBootstrapServices() {
        Slog.i(TAG, "startBootstrapServices");
    }

    public void startCoreServices() {
        Slog.i(TAG, "startCoreServices");
    }

    public void startOtherServices() {
        Slog.i(TAG, "startOtherServices");
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
    }

    public void systemRunning() {
        Slog.i(TAG, "systemRunning");
    }
}
