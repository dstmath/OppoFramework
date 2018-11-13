package com.android.server.oppo;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.cabc.ICabcManager.Stub;
import com.android.server.LocationManagerService;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CabcService extends Stub {
    private static boolean DEBUG = false;
    private static final String PROP_LOG_CABC = "persist.sys.assert.panic";
    private static final String TAG = "CabcService";
    private static final String cabc_node = "/sys/devices/virtual/graphics/fb0/cabc";
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
            return;
        }
        if (writeCabcNode(modeStr) && DEBUG) {
            Slog.d(TAG, "setMode mode = " + mode + " successful!");
        }
    }

    public int getMode() {
        return parseMode(getCurrentCabcMode());
    }

    private String getModeStr(int mode) {
        switch (mode) {
            case 0:
            case 1:
            case 2:
            case 3:
                return String.valueOf(mode);
            default:
                return null;
        }
    }

    public void closeCabc() {
        if (DEBUG) {
            Slog.d(TAG, "closeCabc.");
        }
    }

    public void openCabc() {
        if (DEBUG) {
            Slog.d(TAG, "openCabc, mInitModeFromDriver is:" + this.mInitModeFromDriver);
        }
    }

    private boolean writeCabcNode(String value) {
        if (DEBUG) {
            Slog.d(TAG, "writeCabcNode, new mode is:" + value);
        }
        return true;
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
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equalsIgnoreCase(mode)) {
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
}
