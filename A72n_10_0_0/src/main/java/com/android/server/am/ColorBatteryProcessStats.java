package com.android.server.am;

import android.os.OppoBaseBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.ProcessCpuTracker;
import com.color.util.ColorTypeCastingHelper;

public class ColorBatteryProcessStats implements IColorBatteryProcessStats {
    private BatteryStatsImpl mBatteryStats;

    public void updatePackageListAndPid(ProcessRecord pr, BatteryStatsImpl.Uid.Proc ps) {
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, ps);
        if (basePs != null) {
            basePs.updatePackageList(pr.pkgList.mPkgList.keySet());
            if (pr.pid > 0) {
                basePs.setCurrentPid(pr.pid);
            }
        }
    }

    public void addPackageForBatteryStats(BatteryStatsImpl.Uid.Proc ps, ProcessCpuTracker.Stats st) {
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, ps);
        if (basePs != null) {
            basePs.addPackage(st.name);
        }
    }

    public BatteryStatsImpl.Uid.Proc updateProcWhenSetPid(ActivityManagerService ams, int uid, int pid, String processName, String packageName, BatteryStatsImpl.Uid.Proc curProcBatteryStats) {
        if (this.mBatteryStats == null) {
            return curProcBatteryStats;
        }
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = null;
        BatteryStatsImpl.Uid.Proc curProcBatteryStats2 = generateProcBatteryStats(ams, uid, processName, curProcBatteryStats);
        if (curProcBatteryStats2 != null) {
            basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, curProcBatteryStats2);
            if (basePs == null) {
                return curProcBatteryStats2;
            }
            basePs.addPackage(packageName);
        }
        if (basePs != null && pid > 0 && curProcBatteryStats2 != null && curProcBatteryStats2.isActive()) {
            basePs.setCurrentPid(pid);
        }
        return curProcBatteryStats2;
    }

    public BatteryStatsImpl.Uid.Proc updateProcWhenAddPackage(ActivityManagerService ams, int uid, int pid, String processName, String packageName, BatteryStatsImpl.Uid.Proc curProcBatteryStats) {
        getBatteryStats(ams);
        if (this.mBatteryStats == null) {
            return curProcBatteryStats;
        }
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = null;
        BatteryStatsImpl.Uid.Proc curProcBatteryStats2 = generateProcBatteryStats(ams, uid, processName, curProcBatteryStats);
        if (curProcBatteryStats2 != null && pid > 0) {
            basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, curProcBatteryStats2);
            if (basePs == null) {
                return curProcBatteryStats2;
            }
            basePs.setCurrentPid(pid);
        }
        if (!(basePs == null || curProcBatteryStats2 == null || !curProcBatteryStats2.isActive())) {
            basePs.addPackage(packageName);
        }
        return curProcBatteryStats2;
    }

    private BatteryStatsImpl.Uid.Proc generateProcBatteryStats(ActivityManagerService ams, int uid, String processName, BatteryStatsImpl.Uid.Proc curProcBatteryStats) {
        getBatteryStats(ams);
        if (curProcBatteryStats != null && curProcBatteryStats.isActive()) {
            return curProcBatteryStats;
        }
        BatteryStatsImpl batteryStatsImpl = this.mBatteryStats;
        return batteryStatsImpl.getProcessStatsLocked(batteryStatsImpl.mapUid(uid), processName);
    }

    private BatteryStatsImpl getBatteryStats(ActivityManagerService ams) {
        if (this.mBatteryStats == null) {
            this.mBatteryStats = ams.mBatteryStatsService.getActiveStatistics();
        }
        return this.mBatteryStats;
    }
}
