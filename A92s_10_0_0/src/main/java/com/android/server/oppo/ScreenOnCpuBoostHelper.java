package com.android.server.oppo;

import android.os.SystemProperties;
import android.util.Slog;
import com.oppo.hypnus.Hypnus;
import com.oppo.hypnus.HypnusManager;

public class ScreenOnCpuBoostHelper {
    private static boolean DEBUG = false;
    private static final int DEFAULT_BOOST_TIMEOUT = 1000;
    private static final String TAG = "ScreenOnCpuBoostHelper";
    private static boolean mEnableBoost = true;
    private HypnusManager mHM;
    private boolean mIsBoostCpu;

    public ScreenOnCpuBoostHelper() {
        this.mHM = null;
        this.mIsBoostCpu = false;
        this.mHM = new HypnusManager();
        DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    public void setEnable(boolean b) {
        mEnableBoost = b;
    }

    public void acquireCpuBoost() {
        acquireCpuBoost(1000);
    }

    public void acquireCpuBoost(int timeout) {
        if (mEnableBoost) {
            HypnusManager hypnusManager = this.mHM;
            if (hypnusManager != null) {
                hypnusManager.hypnusSetSignatureAction(15, timeout, Hypnus.getLocalSignature());
            }
            this.mIsBoostCpu = true;
            if (DEBUG) {
                Slog.i(TAG, "Start screen on CPU boost");
            }
        }
    }

    public void releaseCpuBoost() {
        if (mEnableBoost && this.mIsBoostCpu) {
            this.mIsBoostCpu = false;
            HypnusManager hypnusManager = this.mHM;
            if (hypnusManager != null) {
                hypnusManager.hypnusSetSignatureAction(15, 0, Hypnus.getLocalSignature());
            }
            if (DEBUG) {
                Slog.i(TAG, "Stop screen on CPU boost");
            }
        }
    }
}
