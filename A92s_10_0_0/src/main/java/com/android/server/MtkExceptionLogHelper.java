package com.android.server;

import android.os.Process;
import android.util.Slog;
import com.mediatek.aee.ExceptionLog;

public class MtkExceptionLogHelper {
    public static void generateSystemCrashLog(String unknownCrashReason) {
        try {
            ExceptionLog.getInstance().handle("system_server_crash", unknownCrashReason, Integer.toString(Process.myPid()));
        } catch (Exception e) {
            Slog.e("MtkExceptionLogHelper", "generateSystemCrashLog :" + e.toString());
        }
    }
}
