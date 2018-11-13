package com.qti.server.power;

import android.util.Log;

public final class ShutdownOem {
    private static final String TAG = "QualcommShutdown";

    public void rebootOrShutdown(boolean reboot, String reason) {
        Log.i(TAG, "Qualcomm reboot/shutdown.");
        if (SubSystemShutdown.shutdown() != 0) {
            Log.e(TAG, "Failed to shutdown modem.");
        } else {
            Log.i(TAG, "Modem shutdown successful.");
        }
    }
}
