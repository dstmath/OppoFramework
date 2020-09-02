package com.mediatek.gnssdebugreport;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class GnssDebugReportManager {
    private static final String SERVICE_CLASS = "com.mediatek.gnssdebugreport.GnssDebugReportService";
    private static final String SERVICE_PKG = "com.mediatek.gnssdebugreport";

    public static boolean bindService(Context context, ServiceConnection serviceConnection) {
        Intent it = new Intent();
        it.setClassName(SERVICE_PKG, SERVICE_CLASS);
        return context.bindService(it, serviceConnection, 1);
    }

    public static void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }
}
