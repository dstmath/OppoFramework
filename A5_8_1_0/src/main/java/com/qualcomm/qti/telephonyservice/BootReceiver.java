package com.qualcomm.qti.telephonyservice;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final boolean DBG = true;
    private static final String TAG = "QtiTelephonyService BootReceiver";
    private static final String mClassName = QtiTelephonyService.class.getName();

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent);
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && !intent.getAction().equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {
            Log.e(TAG, "Received unsupported intent");
        } else if (isServiceRunning(context)) {
            Log.i(TAG, mClassName + " is already running. " + intent + " ignored.");
        } else if (context.startService(new Intent(context, QtiTelephonyService.class)) == null) {
            Log.e(TAG, "Could not start service");
        } else {
            Log.d(TAG, "Successfully started service");
        }
    }

    private boolean isServiceRunning(Context context) {
        for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (mClassName.equals(service.service.getClassName())) {
                return DBG;
            }
        }
        return false;
    }
}
