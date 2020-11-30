package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.ProcessCpuTracker;

public interface IColorBatteryProcessStats extends IOppoCommonFeature {
    public static final IColorBatteryProcessStats DEFAULT = new IColorBatteryProcessStats() {
        /* class com.android.server.am.IColorBatteryProcessStats.AnonymousClass1 */
    };
    public static final String NAME = "OppoBatteryProcessStats";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBatteryProcessStats;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void updatePackageListAndPid(ProcessRecord pr, BatteryStatsImpl.Uid.Proc ps) {
    }

    default void addPackageForBatteryStats(BatteryStatsImpl.Uid.Proc ps, ProcessCpuTracker.Stats st) {
    }

    default BatteryStatsImpl.Uid.Proc updateProcWhenSetPid(ActivityManagerService ams, int uid, int pid, String processName, String packageName, BatteryStatsImpl.Uid.Proc curProcBatteryStats) {
        return curProcBatteryStats;
    }

    default BatteryStatsImpl.Uid.Proc updateProcWhenAddPackage(ActivityManagerService ams, int uid, int pid, String processName, String packageName, BatteryStatsImpl.Uid.Proc curProcBatteryStats) {
        return curProcBatteryStats;
    }
}
