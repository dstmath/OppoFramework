package com.android.internal.os;

import android.os.BatteryStats;
import android.util.Log;

public class CameraPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "CameraPowerCalculator";
    private final double mCameraPowerOnAvg;

    public CameraPowerCalculator(PowerProfile profile) {
        this.mCameraPowerOnAvg = profile.getAveragePower(PowerProfile.POWER_CAMERA);
        Log.d(TAG, "cameraPowerOnAvg= " + this.mCameraPowerOnAvg);
    }

    @Override // com.android.internal.os.PowerCalculator
    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatteryStats.Timer timer = u.getCameraTurnedOnTimer();
        if (timer != null) {
            long totalTime = timer.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            app.cameraTimeMs = totalTime;
            app.cameraPowerMah = (((double) totalTime) * this.mCameraPowerOnAvg) / 3600000.0d;
        } else {
            app.cameraTimeMs = 0;
            app.cameraPowerMah = 0.0d;
        }
        BatteryStats.Timer timerBg = u.getBgCameraTurnedOnTimer();
        if (timerBg != null) {
            long totalTime2 = timerBg.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            app.cameraBgTimeMs = totalTime2;
            app.cameraBgPowerMah = (((double) totalTime2) * this.mCameraPowerOnAvg) / 3600000.0d;
            return;
        }
        app.cameraBgTimeMs = 0;
        app.cameraBgPowerMah = 0.0d;
    }
}
