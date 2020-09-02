package com.mediatek.mtklogger.c2klogger;

import android.app.Service;
import android.util.Log;

public class C2KLoggerProxy {
    private static C2KLogService sC2KLogService;

    public static void startService(Service service) {
        Log.d(C2KLogUtils.TAG_APP, "-->>>>>C2KLoggerStart");
        sC2KLogService = new C2KLogService(service);
        sC2KLogService.onCreate();
        sC2KLogService.onStartCommand();
        Log.d(C2KLogUtils.TAG_APP, "<<<<<<<<<<---------C2KLoggerStart end!");
    }

    public static void stopService(Service service) {
        Log.d(C2KLogUtils.TAG_APP, "-->>>>>stopService");
        sC2KLogService.onDestroy();
        Log.d(C2KLogUtils.TAG_APP, "<<<<<<<<<<---------stopService end!");
    }
}
