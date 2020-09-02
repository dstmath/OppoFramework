package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class OppoShortcutServiceUtil {
    private static final boolean DEBUG_COLOROS_SS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static String TAG = "OppoShortcutServiceUtil";

    OppoShortcutServiceUtil() {
    }

    public static boolean isBootFromOTA() {
        if (new File("/cache/recovery/intent").exists()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            String otaResultStr = readOTAUpdateResult("/cache/recovery/intent");
            if ("0".equals(otaResultStr)) {
                if (DEBUG_COLOROS_SS) {
                    Slog.i(TAG, "is boot from OTA");
                }
                return true;
            } else if ("1".equals(otaResultStr)) {
                if (DEBUG_COLOROS_SS) {
                    Slog.i(TAG, "not boot from OTA,normal boot");
                }
                return false;
            } else if ("2".equals(otaResultStr)) {
                if (DEBUG_COLOROS_SS) {
                    Slog.i(TAG, "is boot from recover");
                }
                return true;
            } else if ("3".equals(otaResultStr)) {
                if (DEBUG_COLOROS_SS) {
                    Slog.i(TAG, "not boot from recover,normal boot");
                }
                return false;
            } else {
                if (DEBUG_COLOROS_SS) {
                    Slog.i(TAG, "OTA update file's date is invalid, normal boot");
                }
                return false;
            }
        } else {
            if (DEBUG_COLOROS_SS) {
                Slog.i(TAG, "OTA file path is no exist,normal boot");
            }
            return false;
        }
    }

    private static String readOTAUpdateResult(String fileName) {
        String resultStr = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            resultStr = reader.readLine();
            try {
                reader.close();
            } catch (IOException e1) {
                Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
            }
        } catch (IOException e) {
            Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                }
            }
            throw th;
        }
        return resultStr;
    }
}
