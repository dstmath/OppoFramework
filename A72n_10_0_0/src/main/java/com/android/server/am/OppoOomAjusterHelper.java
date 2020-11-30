package com.android.server.am;

import android.util.Slog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OppoOomAjusterHelper {
    private static final String TAG = "OppoOomAjusterHelper";

    public static boolean isOppoImportantApp(String processName) {
        if (processName.equals("com.tencent.mm")) {
            return true;
        }
        return false;
    }

    public static boolean isAgingTestTool(String processName) {
        return processName.contains("com.oppo.qetest") || processName.contains("com.oppo.qemonitor") || processName.contains("com.oppo.autotest.qetest") || processName.contains("com.oppo.autotest.qemonitor");
    }

    public static void updataKernelTopUid(ProcessRecord app) {
        Slog.d(TAG, "TOP_APP is " + app + " uid is " + app.uid);
        String uid = Integer.toString(app.uid);
        try {
            FileWriter writer = new FileWriter(new File("/proc/fg_info/fg_uids"));
            writer.write(uid);
            writer.close();
        } catch (IOException e) {
        }
    }
}
