package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

public class OppoModemLogManager {
    public static final String ACTION_SAVE_MDLOGBUFFER = "oppo.intent.action.QxdmStopAndPostLog";
    public static final String ACTION_START_MDLOGBUFFER = "android.intent.action.StartModemLogPostBack";
    public static final String ACTION_START_MDLOGNORAL = "android.intent.action.StartModemLogNormal";
    public static final String ACTION_STOP_MODEMLOG = "android.intent.action.StopModemLogPostBack";
    public static final String DEFAULT_MODEMDUMP_POSTBACK = "0";
    private static final int DEFAULT_MODEM_DUMP_POSTBACK = 0;
    public static final String PROPERTY_MODEMDUMP_POSTBACK = "persist.sys.mdlog_dumpback";
    public static final String PROPERTY_MODEMLOG_TYPE = "persist.sys.mdlogtype";
    public static final String PROPERTY_MODEM_MDLOG_NRT = "persist.sys.odlnrtmode";
    public static final String TAG = "OppoModemLogManager";
    public static final int TYPE_MODEMLOG_BUFFER = 1;
    public static final int TYPE_MODEMLOG_CLOSE = 0;
    public static final int TYPE_MODEMLOG_NORMAL = 3;
    public static final int TYPE_MODEMLOG_NRT = 2;

    public static int getModemLogType() {
        return SystemProperties.getInt(PROPERTY_MODEMLOG_TYPE, 0);
    }

    public static boolean supportCircleBuffer() {
        return true;
    }

    public static void updateMdlogType(Context context, int type) {
        if (type < 0 || type > 3) {
            Log.d(TAG, "updateMdlogType. type is wrong");
            return;
        }
        SystemProperties.set(PROPERTY_MODEMLOG_TYPE, PhoneConfigurationManager.SSSS + type);
        if (type == 0) {
            closeModemLogPostBack(context);
        } else if (type == 1) {
            openModemLogPostBack(context);
        } else if (type == 2 || type == 3) {
            SystemProperties.set(PROPERTY_MODEM_MDLOG_NRT, type == 2 ? "1" : DEFAULT_MODEMDUMP_POSTBACK);
            openDeviceLog(context);
        }
    }

    public static void openDeviceLog(Context context) {
        boolean enabled = getModemLogType() == 2 || getModemLogType() == 3;
        Log.d(TAG, "openDeviceLog:" + enabled);
        if (enabled) {
            context.sendBroadcast(new Intent(ACTION_START_MDLOGNORAL));
        }
    }

    public static void openModemLogPostBack(Context context) {
        boolean enabled = true;
        if (getModemLogType() != 1) {
            enabled = false;
        }
        Log.d(TAG, "openModemLogPostBack:" + enabled);
        if (enabled) {
            context.sendBroadcast(new Intent(ACTION_START_MDLOGBUFFER));
        }
    }

    public static void disableModemLogPostBack(Context context, boolean close) {
        Log.d(TAG, "disableModemLogPostBack.");
    }

    private static boolean canTriggerSaveLog() {
        return getModemLogType() == 1 && SystemProperties.get("sys.modemlog.postback", "unknown").equals("run");
    }

    public static void saveModemLogPostBack(Context context, String type, String desc) {
        Log.d(TAG, "saveModemLogPostBack.");
        if (canTriggerSaveLog()) {
            Intent intent = new Intent(ACTION_SAVE_MDLOGBUFFER);
            intent.putExtra("logtype", type);
            intent.putExtra("logdesc", desc);
            context.sendBroadcast(intent);
        }
    }

    public static void closeModemLogPostBack(Context context) {
        Log.d(TAG, "closeModemLogPostBack.");
        context.sendBroadcast(new Intent(ACTION_STOP_MODEMLOG));
    }

    public static boolean canStartMdmSSRPostBack() {
        return SystemProperties.get(PROPERTY_MODEMDUMP_POSTBACK, DEFAULT_MODEMDUMP_POSTBACK).equals("1") && SystemProperties.get("persist.sys.dump", PhoneConfigurationManager.SSSS).equals(DEFAULT_MODEMDUMP_POSTBACK);
    }

    public static boolean isModemDumpBackEnable() {
        return SystemProperties.getInt(PROPERTY_MODEMDUMP_POSTBACK, 0) == 1;
    }

    public static void enableModemDumpPostBack(boolean enable) {
        Log.d(TAG, "enableModemDumpPostBack:" + enable);
        if (enable) {
            SystemProperties.set(PROPERTY_MODEMDUMP_POSTBACK, "1");
            openModemDumpPostBack();
            return;
        }
        Log.d(TAG, "closeModemDumpPostBack:" + enable);
        SystemProperties.set(PROPERTY_MODEMDUMP_POSTBACK, DEFAULT_MODEMDUMP_POSTBACK);
        closeModemDumpPostBack();
    }

    public static void openModemDumpPostBack() {
        Log.d(TAG, "openModemDumpPostBack.");
        if (SystemProperties.getInt(PROPERTY_MODEMDUMP_POSTBACK, 0) == 1 && SystemProperties.get("persist.sys.dump", PhoneConfigurationManager.SSSS).equals(DEFAULT_MODEMDUMP_POSTBACK)) {
            Log.d(TAG, "openModemDumpPostBack 1.");
            SystemProperties.set("persist.vendor.ssr.enable_ramdumps", "1");
            SystemProperties.set("persist.sys.enable_modem_dump", "1");
        }
    }

    public static void closeModemDumpPostBack() {
        Log.d(TAG, "closeModemDumpPostBack.");
        if (SystemProperties.get("persist.vendor.ssr.enable_ramdumps", PhoneConfigurationManager.SSSS).equals("1")) {
            Log.d(TAG, "closeModemDumpPostBack1.");
            SystemProperties.set("persist.vendor.ssr.enable_ramdumps", DEFAULT_MODEMDUMP_POSTBACK);
            SystemProperties.set("persist.sys.enable_modem_dump", DEFAULT_MODEMDUMP_POSTBACK);
        }
    }
}
