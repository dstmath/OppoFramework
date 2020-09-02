package com.mediatek.mtklogger.c2klogger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class C2KLogUtils {
    public static final String ACTION_VIA_SET_ETS_DEV = "via.cdma.action.set.ets.dev.c2klogger";
    public static final String C2KLOG_CUSTOMIZE_CONFIG_FILE = "c2klog-config.prop";
    public static final String C2KLOG_CUSTOMIZE_CONFIG_FOLDER = "c2klog_config";
    public static final String C2KLOG_RECYCLE_CONFIG_FILE = "recycle_file_tree.txt";
    public static final String C2K_MODEM_LOG_PATH = "c2kmdlog";
    public static final String CONFIG_FILE_NAME = "log_settings";
    public static final String CONIFG_DEVICEPATH = "com.mediatek.mtklogger.c2klogger.devicepath";
    public static final String CONIFG_FILTERFILE = "com.mediatek.mtklogger.c2klogger.filterfile";
    public static final String CONIFG_PATH = "com.mediatek.mtklogger.c2klogger.configpath";
    public static final String CONIFG_PERLOGSIZE = "com.mediatek.mtklogger.c2klogger.perlogsize";
    public static final String CONIFG_TOTALLOGSIZE = "com.mediatek.mtklogger.c2klogger.totallogsize";
    public static final String DEFAULT_CONIFG_DEVICEPATH = "/dev/ttySDIO1";
    public static final String DEFAULT_CONIFG_FILTERFILE = "ets_1x_do_hybrid_default_v5.bcfg";
    public static final String DEFAULT_CONIFG_PATH = "/mnt/sdcard/c2klog_config/";
    public static final int DEFAULT_CONIFG_PERLOGSIZE = 6;
    public static final int DEFAULT_CONIFG_TOTALLOGSIZE = 3072;
    public static final String EXTRAL_VIA_ETS_DEV = "via.cdma.extral.ets.dev";
    public static final String EXTRAL_VIA_ETS_DEV_RESULT = "set.ets.dev.result";
    public static final String KEY_C2K_MODEM_LOGGING_PATH = "c2k_modem_logging_path";
    public static final String LOG_PATH_TYPE_EXTERNAL_SD_KEY = "ExternalLogPath";
    public static final String LOG_PATH_TYPE_INTERNAL_SD_KEY = "InternalLogPath";
    public static final String LOG_TREE_FILE = "file_tree.txt";
    public static final String TAG_APP = "saber";
    public static final int USER_ID = UserHandle.myUserId();
    public static final int USER_ID_OWNER = 0;
    public static final int USER_ID_UNDEFINED = -1;
    public int sWaitCbpS = 10;

    public static boolean isDeviceOwner() {
        int i = USER_ID;
        return i == 0 || i == -1;
    }

    public static void sendBroadCast(Context context, Intent intent) {
        if (!isDeviceOwner() || context == null) {
            Log.w(TAG_APP, "Is not device owner, no permission to send broadcast!");
        } else {
            context.sendBroadcast(intent);
        }
    }

    public static boolean isServiceRunning(Context context, String serviceClassName) {
        for (ActivityManager.RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileWriter.<init>(java.io.File, boolean):void throws java.io.IOException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileWriter.<init>(java.lang.String, boolean):void throws java.io.IOException}
      ClspMth{java.io.FileWriter.<init>(java.io.File, boolean):void throws java.io.IOException} */
    public static void writerToFileTree(String fileTreePath, String logPath) {
        Log.i(TAG_APP, "writerToFileTree() logPath = " + logPath);
        if (!new File(logPath).exists()) {
            Log.w(TAG_APP, "The logPath:" + logPath + " is not exists.");
            return;
        }
        try {
            FileWriter fw = new FileWriter(new File(fileTreePath), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(logPath + "\r\n");
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
