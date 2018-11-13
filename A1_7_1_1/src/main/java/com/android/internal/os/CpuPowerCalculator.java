package com.android.internal.os;

import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Proc;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CpuPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final boolean OPPODEBUG = false;
    private static final String TAG = "CpuPowerCalculator";
    private final PowerProfile mProfile;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.CpuPowerCalculator.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.CpuPowerCalculator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.CpuPowerCalculator.<clinit>():void");
    }

    public CpuPowerCalculator(PowerProfile profile) {
        this.mProfile = profile;
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        int cluster;
        int speedsForCluster;
        int speed;
        app.cpuTimeMs = (u.getUserCpuTimeUs(statsType) + u.getSystemCpuTimeUs(statsType)) / 1000;
        if (app.cpuTimeMs < 0) {
            if (OPPODEBUG) {
                Log.d(TAG, "UID " + u.getUid() + ": CPU time " + app.cpuTimeMs + " ms");
            }
            app.cpuTimeMs = 0;
        }
        long totalTime = 0;
        int numClusters = this.mProfile.getNumCpuClusters();
        for (cluster = 0; cluster < numClusters; cluster++) {
            speedsForCluster = this.mProfile.getNumSpeedStepsInCpuCluster(cluster);
            for (speed = 0; speed < speedsForCluster; speed++) {
                long speedTime = u.getTimeAtCpuSpeed(cluster, speed, statsType);
                if (speedTime >= 0) {
                    totalTime += u.getTimeAtCpuSpeed(cluster, speed, statsType);
                } else if (OPPODEBUG) {
                    Log.d(TAG, "UID=" + u.getUid() + ", speedTime=" + speedTime + ", cluster=" + cluster + ", speed=" + speed);
                }
            }
        }
        totalTime = Math.max(totalTime, 1);
        double cpuPowerMaMs = 0.0d;
        for (cluster = 0; cluster < numClusters; cluster++) {
            speedsForCluster = this.mProfile.getNumSpeedStepsInCpuCluster(cluster);
            for (speed = 0; speed < speedsForCluster; speed++) {
                double cpuSpeedStepPower = (((double) app.cpuTimeMs) * (((double) u.getTimeAtCpuSpeed(cluster, speed, statsType)) / ((double) totalTime))) * this.mProfile.getAveragePowerForCpu(cluster, speed);
                if (cpuSpeedStepPower >= 0.0d) {
                    cpuPowerMaMs += cpuSpeedStepPower;
                }
            }
        }
        if (cpuPowerMaMs < 0.0d) {
            if (OPPODEBUG) {
                Log.d(TAG, "UID " + u.getUid() + ": cpuPowerMaMs " + cpuPowerMaMs);
            }
            cpuPowerMaMs = 0.0d;
        }
        app.cpuPowerMah = cpuPowerMaMs / 3600000.0d;
        double highestDrain = 0.0d;
        long totalCpuTimeSec = 0;
        app.cpuFgTimeMs = 0;
        ArrayMap<String, ? extends Proc> processStats = u.getProcessStats();
        int processStatsCount = processStats.size();
        for (int i = 0; i < processStatsCount; i++) {
            long cpuTimeSec;
            Proc ps = (Proc) processStats.valueAt(i);
            String processName = (String) processStats.keyAt(i);
            app.cpuFgTimeMs += ps.getForegroundTime(statsType);
            long costValue = (ps.getUserTime(statsType) + ps.getSystemTime(statsType)) + ps.getForegroundTime(statsType);
            if (app.packageWithHighestDrain != null) {
                if (!app.packageWithHighestDrain.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                    if (highestDrain < ((double) costValue) && !processName.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                        highestDrain = (double) costValue;
                        app.packageWithHighestDrain = processName;
                    }
                    if (BatteryStatsHelper.isSystemUid(u.getUid()) && costValue > 0) {
                        cpuTimeSec = costValue / 1000;
                        totalCpuTimeSec += cpuTimeSec;
                        app.setSystemUidApkCpuTime(processName, cpuTimeSec);
                    }
                }
            }
            highestDrain = (double) costValue;
            app.packageWithHighestDrain = processName;
            cpuTimeSec = costValue / 1000;
            totalCpuTimeSec += cpuTimeSec;
            app.setSystemUidApkCpuTime(processName, cpuTimeSec);
        }
        if (BatteryStatsHelper.isSystemUid(u.getUid())) {
            app.calSystemUidApkCpuPower(totalCpuTimeSec);
        }
        if (app.cpuFgTimeMs > app.cpuTimeMs) {
            app.cpuTimeMs = app.cpuFgTimeMs;
        }
    }
}
