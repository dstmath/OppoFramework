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

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
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
