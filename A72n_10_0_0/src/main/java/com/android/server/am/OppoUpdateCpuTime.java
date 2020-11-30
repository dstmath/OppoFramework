package com.android.server.am;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import com.android.internal.os.OppoBaseProcessCpuTracker;
import com.android.internal.os.OppoBatteryStatsImpl;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.color.util.ColorTypeCastingHelper;

public class OppoUpdateCpuTime {
    public static void updateProcessCpuTimeLocked(OppoBatteryStatsImpl oppobstats, boolean haveNewCpuStats, ActivityManagerService.PidMap mPidsSelfLocked, ProcessCpuTracker mProcessCpuTracker) {
        Throwable th;
        Throwable th2;
        synchronized (oppobstats) {
            try {
                synchronized (mPidsSelfLocked) {
                    if (haveNewCpuStats) {
                        try {
                            if (oppobstats.startAddingCpuLocked()) {
                                int totalUTime = 0;
                                int N = mProcessCpuTracker.countStats();
                                int totalSTime = 0;
                                for (int i = 0; i < N; i++) {
                                    ProcessCpuTracker.Stats st = mProcessCpuTracker.getStats(i);
                                    OppoBaseProcessCpuTracker.OppoBaseStats baseSt = typeCasting(st);
                                    if (st.working) {
                                        ProcessRecord pr = mPidsSelfLocked.get(st.pid);
                                        OppoBaseProcessRecord basePr = typeCasting(pr);
                                        totalUTime += st.rel_utime;
                                        totalSTime += st.rel_stime;
                                        if (pr == null || basePr == null) {
                                            OppoBatteryStatsImpl.Uid.Proc ps = baseSt != null ? baseSt.oppobatteryStats : null;
                                            if ((ps == null || !ps.isActive()) && baseSt != null) {
                                                OppoBatteryStatsImpl.Uid.Proc processStatsLocked = oppobstats.getProcessStatsLocked(oppobstats.mapUid(st.uid), st.name);
                                                ps = processStatsLocked;
                                                baseSt.oppobatteryStats = processStatsLocked;
                                            }
                                            ps.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                        } else {
                                            OppoBatteryStatsImpl.Uid.Proc ps2 = basePr.curProcOppoBatteryStats;
                                            if (ps2 == null || !ps2.isActive()) {
                                                OppoBatteryStatsImpl.Uid.Proc processStatsLocked2 = oppobstats.getProcessStatsLocked(pr.info.uid, pr.processName);
                                                ps2 = processStatsLocked2;
                                                basePr.curProcOppoBatteryStats = processStatsLocked2;
                                            }
                                            ps2.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                            pr.curCpuTime += (long) (st.rel_utime + st.rel_stime);
                                            if (pr.lastCpuTime == 0) {
                                                pr.lastCpuTime = pr.curCpuTime;
                                            }
                                        }
                                    }
                                }
                                oppobstats.finishAddingCpuLocked(totalUTime, totalSTime, mProcessCpuTracker.getLastUserTime(), mProcessCpuTracker.getLastSystemTime(), mProcessCpuTracker.getLastIoWaitTime(), mProcessCpuTracker.getLastIrqTime(), mProcessCpuTracker.getLastSoftIrqTime(), mProcessCpuTracker.getLastIdleTime());
                            }
                        } catch (Throwable th3) {
                            th2 = th3;
                            throw th2;
                        }
                    }
                    try {
                    } catch (Throwable th4) {
                        th = th4;
                        throw th;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }

    public static void noteActivityResumedLocked(OppoBatteryStatsImpl oppostats, int uid, ComponentName component) {
        synchronized (oppostats) {
            oppostats.noteActivityResumedLocked(uid, component);
        }
    }

    public static void noteProcessDiedLocked(OppoBatteryStatsImpl oppostats, ProcessRecord app, int pid) {
        synchronized (oppostats) {
            oppostats.noteProcessDiedLocked(app.info.uid, pid);
        }
    }

    public static OppoBatteryStatsImpl.Uid.Pkg.Serv setBackupRecordBatteryStats(OppoBatteryStatsImpl oppostats, BackupRecord r, ApplicationInfo app) {
        OppoBatteryStatsImpl.Uid.Pkg.Serv opposs;
        synchronized (oppostats) {
            opposs = oppostats.getServiceStatsLocked(app.uid, app.packageName, app.name);
        }
        return opposs;
    }

    public static void noteCurrentTimeChangedLocked(OppoBatteryStatsImpl oppostats) {
        synchronized (oppostats) {
            oppostats.noteCurrentTimeChangedLocked();
        }
    }

    public static OppoBaseProcessCpuTracker.OppoBaseStats typeCasting(ProcessCpuTracker.Stats st) {
        return (OppoBaseProcessCpuTracker.OppoBaseStats) ColorTypeCastingHelper.typeCasting(OppoBaseProcessCpuTracker.OppoBaseStats.class, st);
    }

    public static OppoBaseProcessRecord typeCasting(ProcessRecord pr) {
        return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, pr);
    }
}
