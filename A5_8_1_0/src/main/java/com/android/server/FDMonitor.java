package com.android.server;

import java.io.File;

public class FDMonitor {
    private static final String FD_INFO_PATH = "/proc/self/fd";
    private static final int MAX_FD_COUNT = 2048;
    private static final int RESERVED_FD_COUNT = 100;
    static final String TAG = "FDMonitor";

    public static void monitor() {
        int curFDCount = 0;
        try {
            curFDCount = new File(FD_INFO_PATH).list().length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (curFDCount > 1948) {
            throw new RuntimeException(String.format("fd leak detected, suicide ! current fd count is [%d] ,while fd limit is [%d]", new Object[]{Integer.valueOf(curFDCount), Integer.valueOf(2048)}));
        }
    }
}
