package com.android.server.oppo;

import android.os.SystemProperties;
import android.util.Slog;
import com.oppo.hypnus.HypnusManager;

public class ScreenOnCpuBoostHelper {
    private static boolean DEBUG = false;
    private static final int DEFAULT_BOOST_TIMEOUT = 1000;
    private static final String TAG = "ScreenOnCpuBoostHelper";
    private static boolean enableBoost = true;
    private boolean isBoostCpu;
    private HypnusManager mHM;

    public ScreenOnCpuBoostHelper() {
        this.mHM = null;
        this.isBoostCpu = false;
        this.mHM = new HypnusManager();
        DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    public void setEnable(boolean b) {
        enableBoost = b;
    }

    public void acquireCpuBoost() {
        acquireCpuBoost(1000);
    }

    public void acquireCpuBoost(int timeout) {
        if (enableBoost) {
            if (this.mHM != null) {
                this.mHM.hypnusSetAction(15, timeout);
            }
            this.isBoostCpu = true;
            if (DEBUG) {
                Slog.i(TAG, "Start screen on CPU boost");
            }
        }
    }

    public void releaseCpuBoost() {
        if (enableBoost && this.isBoostCpu) {
            this.isBoostCpu = false;
            if (this.mHM != null) {
                this.mHM.hypnusSetAction(15, 0);
            }
            if (DEBUG) {
                Slog.i(TAG, "Stop screen on CPU boost");
            }
        }
    }
}
