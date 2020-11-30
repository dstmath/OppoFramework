package com.android.server.net;

import android.content.Intent;
import android.os.SystemClock;
import android.os.SystemProperties;

public class OppoClientModeManagerHelper {
    private static final long THRES_RESTORE_DELAY = 60000;
    private static OppoClientModeManagerHelper mInstance = null;

    private OppoClientModeManagerHelper() {
    }

    public static OppoClientModeManagerHelper getInstance() {
        if (mInstance == null) {
            synchronized (OppoClientModeManagerHelper.class) {
                if (mInstance == null) {
                    mInstance = new OppoClientModeManagerHelper();
                }
            }
        }
        return mInstance;
    }

    public void intentPutExtraForDeepSleep(Intent intent) {
        if (isDeepSleepRestoreNetwork()) {
            intent.putExtra("deepsleeprestore", true);
        } else if (isDeepSleepDisableNetwork()) {
            intent.putExtra("deepsleepdisable", true);
        }
    }

    private boolean isDeepSleepRestoreNetwork() {
        long netRestoreTime = SystemProperties.getLong("sys.deepsleep.restore.network", 0);
        if (netRestoreTime != 0 && SystemClock.elapsedRealtime() - netRestoreTime <= 60000) {
            return true;
        }
        return false;
    }

    private boolean isDeepSleepDisableNetwork() {
        long netDisableTime = SystemProperties.getLong("sys.deepsleep.disable.network", 0);
        if (netDisableTime != 0 && SystemClock.elapsedRealtime() - netDisableTime <= 60000) {
            return true;
        }
        return false;
    }
}
