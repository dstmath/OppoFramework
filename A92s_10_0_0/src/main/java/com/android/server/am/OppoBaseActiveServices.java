package com.android.server.am;

import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.os.OppoBatteryStatsImpl;

public abstract class OppoBaseActiveServices {
    private static final String TAG = "ActiveServices";
    final ActivityManagerService mBaseAm;

    public OppoBaseActiveServices(ActivityManagerService service) {
        this.mBaseAm = service;
    }

    public void ajustRestartTime(ServiceRecord r, long minDuration) {
        long now = SystemClock.uptimeMillis();
        if (r.processName != null && r.processName.equals("com.tencent.mm:push") && r.restartDelay > (this.mBaseAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN / 2) + minDuration) {
            r.restartDelay = (this.mBaseAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN / 2) + minDuration;
            r.nextRestartTime = r.restartDelay + now;
            Slog.d(TAG, "adjust restart mm:push in " + r.restartDelay);
        }
    }

    public OppoBatteryStatsImpl.Uid.Pkg.Serv getServiceStatsLocked(OppoBatteryStatsImpl oppostats, int uid, String pkg, String name) {
        OppoBatteryStatsImpl.Uid.Pkg.Serv opposs;
        synchronized (oppostats) {
            opposs = oppostats.getServiceStatsLocked(uid, pkg, name);
        }
        return opposs;
    }
}
