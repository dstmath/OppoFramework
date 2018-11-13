package com.mediatek.amplus;

import android.app.PendingIntent;
import android.content.Context;

public class AlarmManagerPlus {
    private static final String TAG = "AlarmManager";
    private PowerSavingUtils mPowerSavingUtils = null;

    public AlarmManagerPlus(Context context) {
        this.mPowerSavingUtils = new PowerSavingUtils(context);
    }

    public long getMaxTriggerTime(int i, long j, long j2, long j3, PendingIntent pendingIntent, int i2, boolean z) {
        return this.mPowerSavingUtils.getMaxTriggerTime(i, j, j2, j3, pendingIntent, i2, z);
    }

    public boolean isPowerSavingStart() {
        return this.mPowerSavingUtils.isPowerSavingStart();
    }
}
