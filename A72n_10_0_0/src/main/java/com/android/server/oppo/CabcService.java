package com.android.server.oppo;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.cabc.ICabcManager;
import com.android.server.theia.NoFocusWindow;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CabcService extends ICabcManager.Stub {
    private static boolean DEBUG = false;
    private static final String PROP_LOG_CABC = "persist.sys.assert.panic";
    private static final String TAG = "CabcService";
    private static final String cabc_node = "/sys/kernel/oppo_display/cabc";
    private Context mContext;
    private int mInitModeFromDriver = getMode();

    public CabcService(Context context) {
        this.mContext = context;
        DEBUG = SystemProperties.getBoolean(PROP_LOG_CABC, false);
    }

    public void setMode(int mode) {
        if (DEBUG) {
            Slog.d(TAG, "setMode mode = " + mode);
        }
        String modeStr = getModeStr(mode);
        if (modeStr == null) {
            if (DEBUG) {
                Slog.d(TAG, "setMode mode = " + mode + " failed! illegal param.");
            }
        } else if (writeCabcNode(modeStr) && DEBUG) {
            Slog.d(TAG, "setMode mode = " + mode + " successful!");
        }
    }

    public int getMode() {
        return parseMode(getCurrentCabcMode());
    }

    private String getModeStr(int mode) {
        if (mode == 0 || mode == 1 || mode == 2 || mode == 3) {
            return String.valueOf(mode);
        }
        return null;
    }

    public void closeCabc() {
        if (DEBUG) {
            Slog.d(TAG, "closeCabc.");
        }
        if (parseMode(getCurrentCabcMode()) != 0) {
            writeCabcNode(getModeStr(0));
        }
    }

    public void openCabc() {
        if (DEBUG) {
            Slog.d(TAG, "openCabc, mInitModeFromDriver is:" + this.mInitModeFromDriver);
        }
        writeCabcNode(getModeStr(this.mInitModeFromDriver));
    }

    private boolean writeCabcNode(String value) {
        if (DEBUG) {
            Slog.d(TAG, "writeCabcNode, new mode is:" + value);
        }
        if (value == null || value.length() <= 0) {
            Slog.w(TAG, "writeCabcNode:mode unavailable!");
            return false;
        }
        try {
            FileWriter fr = new FileWriter(new File(cabc_node));
            fr.write(value);
            fr.close();
            if (!DEBUG) {
                return true;
            }
            Slog.d(TAG, "write cabc node succeed!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Slog.e(TAG, "write cabc node failed!");
            return false;
        }
    }

    private String getCurrentCabcMode() {
        char[] a = new char[10];
        String result = "";
        try {
            FileReader fr = new FileReader(new File(cabc_node));
            fr.read(a);
            result = new String(a).trim();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
            Slog.e(TAG, "read cabc node failed!");
        }
        if (DEBUG) {
            Slog.d(TAG, "getCurrentCabcMode:" + result);
        }
        return result;
    }

    private int parseMode(String mode) {
        if (DEBUG) {
            Slog.d(TAG, "parseMode mode:" + mode);
        }
        if ("0".equalsIgnoreCase(mode)) {
            return 0;
        }
        if (NoFocusWindow.HUNG_CONFIG_ENABLE.equalsIgnoreCase(mode)) {
            return 1;
        }
        if ("2".equalsIgnoreCase(mode)) {
            return 2;
        }
        if ("3".equalsIgnoreCase(mode)) {
            return 3;
        }
        return 0;
    }

    public void isSunnyBrightnessMode(boolean enable) {
        String modeStr;
        if (enable) {
            if (DEBUG) {
                Slog.d(TAG, "(SunnyBrightness)--close cabc lock");
            }
            modeStr = String.valueOf(9);
        } else {
            if (DEBUG) {
                Slog.d(TAG, "(SunnyBrightness)--close cabc unlock");
            }
            modeStr = String.valueOf(8);
        }
        if (writeCabcNode(modeStr)) {
            if (DEBUG) {
                Slog.d(TAG, "lockCabcMode successful!");
            }
        } else if (DEBUG) {
            Slog.d(TAG, "lockCabcMode fail!");
        }
    }
}
