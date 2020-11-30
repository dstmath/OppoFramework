package com.android.internal.os;

import android.batterySipper.OppoBaseBatterySipper;
import android.os.BatteryStats;
import android.os.OppoBaseBatteryStats;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.color.util.ColorTypeCastingHelper;

public class ColorCpuPowerCalculator {
    private static final boolean DEBUG = false;
    private static final long MICROSEC_IN_HR = 3600000000L;
    private static final String SYSTEM_UID_PROCESS = "android";
    private static final String TAG = "ColorCpuPowerCalculator";
    private final PowerProfile mProfile;

    public ColorCpuPowerCalculator(PowerProfile profile) {
        this.mProfile = profile;
    }

    /* JADX INFO: Multiple debug info for r24v1 double: [D('cpuBgPowerMah' double), D('cpuPowerMah' double)] */
    /* JADX INFO: Multiple debug info for r6v5 double: [D('cpuBgClusterTimes' long[]), D('highestDrain' double)] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01c0  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01ee  */
    public void calculateApps(SparseArray<ArrayMap<String, BatterySipper>> listSippers, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        double cpuBgPowerMah;
        double cpuPowerMaUs;
        long[] cpuBgClusterTimes;
        long[] cpuBgClusterTimes2;
        int processStatsCount;
        int i;
        BatterySipper appHighestDrain;
        BatterySipper appHighestDrain2;
        OppoBaseBatterySipper baseAppHighestDrain;
        long[] cpuBgClusterTimes3;
        long[] cpuClusterTimes;
        BatteryStats.Uid uid = u;
        if (listSippers == null) {
            Log.d(TAG, "calculateApps: listSippers is null!!!");
            return;
        }
        OppoBaseBatteryStats.OppoBaseUid baseUid = typeCasting(uid);
        int uid2 = u.getUid();
        ArrayMap<String, BatterySipper> uidSippers = listSippers.get(uid2);
        if (uidSippers == null) {
            Log.d(TAG, "calculateApps: uidSippers is null!!!");
        } else if (uidSippers.size() == 0) {
            Log.d(TAG, "calculateApps: uidSippers size is zero!!!");
        } else {
            double cpuPowerMah = 0.0d;
            double cpuBgPowerMah2 = 0.0d;
            int numClusters = this.mProfile.getNumCpuClusters();
            long userCpuTimeUs = (uid.getUserCpuTimeUs(statsType) + uid.getSystemCpuTimeUs(statsType)) / 1000;
            int cluster = 0;
            double cpuPowerMaUs2 = 0.0d;
            double cpuPowerMaUs3 = 0.0d;
            while (cluster < numClusters) {
                int speed = 0;
                double cpuBgPowerMaUs = cpuPowerMaUs2;
                double cpuPowerMaUs4 = cpuPowerMaUs3;
                for (int speedsForCluster = this.mProfile.getNumSpeedStepsInCpuCluster(cluster); speed < speedsForCluster; speedsForCluster = speedsForCluster) {
                    double cpuSpeedStepPower = ((double) uid.getTimeAtCpuSpeed(cluster, speed, statsType)) * this.mProfile.getAveragePowerForCpuCore(cluster, speed);
                    long timeBgUs = 0;
                    if (baseUid != null) {
                        timeBgUs = baseUid.getBgTimeAtCpuSpeed(cluster, speed, statsType);
                    }
                    cpuPowerMaUs4 += cpuSpeedStepPower;
                    cpuBgPowerMaUs += ((double) timeBgUs) * this.mProfile.getAveragePowerForCpuCore(cluster, speed);
                    speed++;
                    uid = u;
                    cpuPowerMah = cpuPowerMah;
                }
                cluster++;
                uid = u;
                cpuPowerMaUs3 = cpuPowerMaUs4;
                cpuPowerMaUs2 = cpuBgPowerMaUs;
            }
            double cpuActivePowerProfile = this.mProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE);
            double cpuPowerMaUs5 = cpuPowerMaUs3 + (((double) (u.getCpuActiveTime() * 1000)) * cpuActivePowerProfile);
            if (baseUid != null) {
                cpuBgPowerMah2 = 0.0d + (((double) (baseUid.getBgCpuActiveTime() * 1000)) * cpuActivePowerProfile);
            }
            long[] cpuClusterTimes2 = u.getCpuClusterTimes();
            if (cpuClusterTimes2 == null) {
                cpuBgPowerMah = cpuBgPowerMah2;
            } else if (cpuClusterTimes2.length == numClusters) {
                int i2 = 0;
                while (i2 < numClusters) {
                    cpuPowerMaUs5 += ((double) (cpuClusterTimes2[i2] * 1000)) * this.mProfile.getAveragePowerForCpuCluster(i2);
                    i2++;
                    cpuBgPowerMah2 = cpuBgPowerMah2;
                }
                cpuBgPowerMah = cpuBgPowerMah2;
                cpuPowerMaUs = cpuPowerMaUs5;
                cpuBgClusterTimes = baseUid == null ? baseUid.getBgCpuClusterTimes() : null;
                if (cpuBgClusterTimes != null) {
                    cpuBgClusterTimes2 = cpuBgClusterTimes;
                } else if (cpuBgClusterTimes.length == numClusters) {
                    int i3 = 0;
                    double cpuBgPowerMah3 = cpuBgPowerMah;
                    while (i3 < numClusters) {
                        cpuBgPowerMah3 += ((double) (cpuBgClusterTimes[i3] * 1000)) * this.mProfile.getAveragePowerForCpuCluster(i3);
                        i3++;
                        cpuBgClusterTimes = cpuBgClusterTimes;
                    }
                    cpuBgClusterTimes2 = cpuBgClusterTimes;
                } else {
                    cpuBgClusterTimes2 = cpuBgClusterTimes;
                }
                double cpuPowerMah2 = cpuPowerMaUs / 3.6E9d;
                double cpuPowerMah3 = cpuPowerMaUs2 / 3.6E9d;
                String highestProcess = null;
                ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
                processStatsCount = processStats.size();
                i = 0;
                long totalCpuTime = 0;
                double highestDrain = 0.0d;
                while (i < processStatsCount) {
                    BatteryStats.Uid.Proc ps = (BatteryStats.Uid.Proc) processStats.valueAt(i);
                    String processName = processStats.keyAt(i);
                    long costValue = ps.getUserTime(statsType) + ps.getSystemTime(statsType) + ps.getForegroundTime(statsType);
                    totalCpuTime += costValue;
                    if (uid2 == 0) {
                        highestProcess = "root";
                        cpuBgClusterTimes3 = cpuBgClusterTimes2;
                        cpuClusterTimes = cpuClusterTimes2;
                    } else if (uid2 == 1000) {
                        highestProcess = "android";
                        cpuBgClusterTimes3 = cpuBgClusterTimes2;
                        cpuClusterTimes = cpuClusterTimes2;
                    } else {
                        if (highestProcess == null) {
                            cpuBgClusterTimes3 = cpuBgClusterTimes2;
                            cpuClusterTimes = cpuClusterTimes2;
                        } else if (highestProcess.startsWith("*")) {
                            cpuBgClusterTimes3 = cpuBgClusterTimes2;
                            cpuClusterTimes = cpuClusterTimes2;
                        } else {
                            cpuBgClusterTimes3 = cpuBgClusterTimes2;
                            cpuClusterTimes = cpuClusterTimes2;
                            if (highestDrain < ((double) costValue) && !processName.startsWith("*")) {
                                highestProcess = processName;
                                highestDrain = (double) costValue;
                            }
                        }
                        highestProcess = processName;
                        highestDrain = (double) costValue;
                    }
                    i++;
                    cpuClusterTimes2 = cpuClusterTimes;
                    cpuBgClusterTimes2 = cpuBgClusterTimes3;
                }
                appHighestDrain = getExistSipper(highestProcess, uidSippers);
                OppoBaseBatterySipper baseAppHighestDrain2 = typeCasting(appHighestDrain);
                if (appHighestDrain != null) {
                    BatterySipper appHighestDrain3 = uidSippers.valueAt(0);
                    OppoBaseBatterySipper baseAppHighestDrain3 = typeCasting(appHighestDrain3);
                    if (highestProcess != null && uidSippers.size() == 1 && baseAppHighestDrain3 != null && baseAppHighestDrain3.pkgName.equals(ScreenPowerCalculator.PKGNAME_NO_PKG)) {
                        baseAppHighestDrain3.pkgName = highestProcess;
                        appHighestDrain3.packageWithHighestDrain = highestProcess;
                    }
                    appHighestDrain2 = appHighestDrain3;
                    baseAppHighestDrain = baseAppHighestDrain3;
                } else {
                    appHighestDrain2 = appHighestDrain;
                    baseAppHighestDrain = baseAppHighestDrain2;
                }
                if (baseAppHighestDrain != null) {
                    baseAppHighestDrain.isSharedUidHighestDrain = true;
                }
                calculatePkgPower(processStats, uidSippers, appHighestDrain2, statsType, cpuPowerMah2, cpuPowerMah3, totalCpuTime);
            } else {
                cpuBgPowerMah = cpuBgPowerMah2;
            }
            cpuPowerMaUs = cpuPowerMaUs5;
            cpuBgClusterTimes = baseUid == null ? baseUid.getBgCpuClusterTimes() : null;
            if (cpuBgClusterTimes != null) {
            }
            double cpuPowerMah22 = cpuPowerMaUs / 3.6E9d;
            double cpuPowerMah32 = cpuPowerMaUs2 / 3.6E9d;
            String highestProcess2 = null;
            ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats2 = u.getProcessStats();
            processStatsCount = processStats2.size();
            i = 0;
            long totalCpuTime2 = 0;
            double highestDrain2 = 0.0d;
            while (i < processStatsCount) {
            }
            appHighestDrain = getExistSipper(highestProcess2, uidSippers);
            OppoBaseBatterySipper baseAppHighestDrain22 = typeCasting(appHighestDrain);
            if (appHighestDrain != null) {
            }
            if (baseAppHighestDrain != null) {
            }
            calculatePkgPower(processStats2, uidSippers, appHighestDrain2, statsType, cpuPowerMah22, cpuPowerMah32, totalCpuTime2);
        }
    }

    private void calculatePkgPower(ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats, ArrayMap<String, BatterySipper> uidSippers, BatterySipper appHighestDrain, int statsType, double cpuPowerMah, double cpuBgPowerMah, long totalCpuTime) {
        ColorCpuPowerCalculator colorCpuPowerCalculator = this;
        ArrayMap<String, ? extends BatteryStats.Uid.Proc> arrayMap = processStats;
        int i = statsType;
        if (appHighestDrain != null) {
            OppoBaseBatterySipper baseAppHighestDrain = colorCpuPowerCalculator.typeCasting(appHighestDrain);
            if (totalCpuTime > 0 || baseAppHighestDrain == null) {
                int i2 = 0;
                while (i2 < processStats.size()) {
                    BatteryStats.Uid.Proc ps = (BatteryStats.Uid.Proc) arrayMap.valueAt(i2);
                    BatterySipper app = colorCpuPowerCalculator.getExistSipper(arrayMap.keyAt(i2), uidSippers);
                    if (app == null) {
                        app = appHighestDrain;
                    }
                    OppoBaseBatterySipper baseApp = colorCpuPowerCalculator.typeCasting(app);
                    app.cpuFgTimeMs += ps.getForegroundTime(i);
                    app.cpuTimeMs += ps.getUserTime(i) + ps.getSystemTime(i);
                    long costValue = ps.getUserTime(i) + ps.getSystemTime(i) + ps.getForegroundTime(i);
                    app.cpuPowerMah += (((double) costValue) * cpuPowerMah) / ((double) totalCpuTime);
                    if (baseApp != null) {
                        baseApp.cpuBgPowerMah += (((double) costValue) * cpuBgPowerMah) / ((double) totalCpuTime);
                    }
                    i2++;
                    colorCpuPowerCalculator = this;
                    arrayMap = processStats;
                    i = statsType;
                }
                return;
            }
            appHighestDrain.cpuPowerMah += cpuPowerMah;
            baseAppHighestDrain.cpuBgPowerMah += cpuBgPowerMah;
        }
    }

    private BatterySipper getExistSipper(String processName, ArrayMap<String, BatterySipper> uidSippers) {
        if (processName == null) {
            return null;
        }
        for (int i = 0; i < uidSippers.size(); i++) {
            BatterySipper sipper = uidSippers.valueAt(i);
            OppoBaseBatterySipper baseSipper = typeCasting(sipper);
            if (!(baseSipper == null || baseSipper.pkgName == null)) {
                if (processName.equals(baseSipper.pkgName)) {
                    return sipper;
                }
                if (!"android".equals(baseSipper.pkgName) && processName.contains(baseSipper.pkgName)) {
                    return sipper;
                }
            }
        }
        return null;
    }

    private OppoBaseBatteryStats.OppoBaseUid typeCasting(BatteryStats.Uid uid) {
        if (uid != null) {
            return (OppoBaseBatteryStats.OppoBaseUid) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.class, uid);
        }
        return null;
    }

    private OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg typeCasting(BatteryStats.Uid.Pkg pkg) {
        if (pkg != null) {
            return (OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg.class, pkg);
        }
        return null;
    }

    private OppoBaseBatteryStats typeCasting(BatteryStats bs) {
        if (bs != null) {
            return (OppoBaseBatteryStats) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.class, bs);
        }
        return null;
    }

    private OppoBaseBatterySipper typeCasting(BatterySipper batterySipper) {
        if (batterySipper != null) {
            return (OppoBaseBatterySipper) ColorTypeCastingHelper.typeCasting(OppoBaseBatterySipper.class, batterySipper);
        }
        return null;
    }
}
