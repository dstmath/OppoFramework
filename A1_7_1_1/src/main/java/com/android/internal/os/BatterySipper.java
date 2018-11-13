package com.android.internal.os;

import android.os.BatteryStats.Uid;
import android.util.ArrayMap;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BatterySipper implements Comparable<BatterySipper> {
    public double attributedPowerMah;
    public double bluetoothPowerMah;
    public long bluetoothRunningTimeMs;
    public long btRxBytes;
    public long btTxBytes;
    public double cameraPowerMah;
    public long cameraTimeMs;
    public long cpuFgTimeMs;
    public double cpuPowerMah;
    public long cpuTimeMs;
    public DrainType drainType;
    public double flashlightPowerMah;
    public long flashlightTimeMs;
    public double gpsPowerMah;
    public long gpsTimeMs;
    public String[] mPackages;
    ArrayMap<String, SystemUidApk> mSystemUidApks;
    public long mobileActive;
    public int mobileActiveCount;
    public double mobileRadioPowerMah;
    public long mobileRxBytes;
    public long mobileRxPackets;
    public long mobileTxBytes;
    public long mobileTxPackets;
    public double mobilemspp;
    public double noCoveragePercent;
    public String packageWithHighestDrain;
    public double percent;
    public double screenHoldTimeMs;
    public double screenPowerMah;
    public double sensorPowerMah;
    public double totalPowerMah;
    public Uid uidObj;
    public double usagePowerMah;
    public long usageTimeMs;
    public int userId;
    public double wakeLockPowerMah;
    public long wakeLockTimeMs;
    public double wifiPowerMah;
    public long wifiRunningTimeMs;
    public long wifiRxBytes;
    public long wifiRxPackets;
    public long wifiTxBytes;
    public long wifiTxPackets;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum DrainType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.BatterySipper.DrainType.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.BatterySipper.DrainType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatterySipper.DrainType.<clinit>():void");
        }
    }

    class SystemUidApk {
        long mCpuTimeSec;
        String mPkgName;
        double mPowerCpuMah;
        double mPowerMah;
        double mPowerScreenMah;
        long mScreenTimeMs;
        final /* synthetic */ BatterySipper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.os.BatterySipper.SystemUidApk.<init>(com.android.internal.os.BatterySipper, long, double, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public SystemUidApk(com.android.internal.os.BatterySipper r1, long r2, double r4, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.os.BatterySipper.SystemUidApk.<init>(com.android.internal.os.BatterySipper, long, double, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatterySipper.SystemUidApk.<init>(com.android.internal.os.BatterySipper, long, double, java.lang.String):void");
        }
    }

    public BatterySipper(DrainType drainType, Uid uid, double value) {
        this.totalPowerMah = value;
        this.drainType = drainType;
        this.uidObj = uid;
    }

    public BatterySipper(DrainType drainType, Uid uid, SystemUidApk apk) {
        this.screenHoldTimeMs = (double) apk.mScreenTimeMs;
        this.screenPowerMah = apk.mPowerScreenMah;
        this.cpuPowerMah = apk.mPowerCpuMah;
        this.cpuTimeMs = apk.mCpuTimeSec * 1000;
        this.totalPowerMah = this.screenPowerMah + this.cpuPowerMah;
        this.packageWithHighestDrain = apk.mPkgName;
        this.drainType = drainType;
        this.uidObj = uid;
    }

    public void computeMobilemspp() {
        long packets = this.mobileRxPackets + this.mobileTxPackets;
        this.mobilemspp = packets > 0 ? ((double) this.mobileActive) / ((double) packets) : 0.0d;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object other) {
        return compareTo((BatterySipper) other);
    }

    public int compareTo(BatterySipper other) {
        if (this.drainType != other.drainType) {
            if (this.drainType == DrainType.OVERCOUNTED) {
                return 1;
            }
            if (other.drainType == DrainType.OVERCOUNTED) {
                return -1;
            }
        }
        return Double.compare(other.totalPowerMah, this.totalPowerMah);
    }

    public String[] getPackages() {
        return this.mPackages;
    }

    public int getUid() {
        if (this.uidObj == null) {
            return 0;
        }
        return this.uidObj.getUid();
    }

    public void add(BatterySipper other) {
        this.totalPowerMah += other.totalPowerMah;
        this.usageTimeMs += other.usageTimeMs;
        this.usagePowerMah += other.usagePowerMah;
        this.cpuTimeMs += other.cpuTimeMs;
        this.gpsTimeMs += other.gpsTimeMs;
        this.wifiRunningTimeMs += other.wifiRunningTimeMs;
        this.cpuFgTimeMs += other.cpuFgTimeMs;
        this.wakeLockTimeMs += other.wakeLockTimeMs;
        this.cameraTimeMs += other.cameraTimeMs;
        this.flashlightTimeMs += other.flashlightTimeMs;
        this.bluetoothRunningTimeMs += other.bluetoothRunningTimeMs;
        this.mobileRxPackets += other.mobileRxPackets;
        this.mobileTxPackets += other.mobileTxPackets;
        this.mobileActive += other.mobileActive;
        this.mobileActiveCount += other.mobileActiveCount;
        this.wifiRxPackets += other.wifiRxPackets;
        this.wifiTxPackets += other.wifiTxPackets;
        this.mobileRxBytes += other.mobileRxBytes;
        this.mobileTxBytes += other.mobileTxBytes;
        this.wifiRxBytes += other.wifiRxBytes;
        this.wifiTxBytes += other.wifiTxBytes;
        this.btRxBytes += other.btRxBytes;
        this.btTxBytes += other.btTxBytes;
        this.wifiPowerMah += other.wifiPowerMah;
        this.gpsPowerMah += other.gpsPowerMah;
        this.cpuPowerMah += other.cpuPowerMah;
        this.sensorPowerMah += other.sensorPowerMah;
        this.mobileRadioPowerMah += other.mobileRadioPowerMah;
        this.wakeLockPowerMah += other.wakeLockPowerMah;
        this.cameraPowerMah += other.cameraPowerMah;
        this.flashlightPowerMah += other.flashlightPowerMah;
        this.bluetoothPowerMah += other.bluetoothPowerMah;
        this.screenPowerMah += other.screenPowerMah;
        this.screenHoldTimeMs += other.screenHoldTimeMs;
        this.attributedPowerMah += other.attributedPowerMah;
    }

    public double sumPower() {
        double d = ((((((((((this.usagePowerMah + this.wifiPowerMah) + this.gpsPowerMah) + this.cpuPowerMah) + this.sensorPowerMah) + this.mobileRadioPowerMah) + this.wakeLockPowerMah) + this.cameraPowerMah) + this.flashlightPowerMah) + this.bluetoothPowerMah) + this.screenPowerMah) + this.attributedPowerMah;
        this.totalPowerMah = d;
        return d;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("drainType(").append(this.drainType).append("), uid(").append(getUid()).append("), [ ").append(this.packageWithHighestDrain).append(" ], totalPower(").append(BatteryStatsHelper.makemAh(this.totalPowerMah)).append("mAh)");
        if (this.usagePowerMah != 0.0d) {
            sb.append(", usagePowerMah(").append(BatteryStatsHelper.makemAh(this.usagePowerMah)).append("mAh)");
        }
        if (this.wifiPowerMah != 0.0d) {
            sb.append(", wifiPowerMah(").append(BatteryStatsHelper.makemAh(this.wifiPowerMah)).append("mAh)");
        }
        if (this.gpsPowerMah != 0.0d) {
            sb.append(", gpsPowerMah(").append(BatteryStatsHelper.makemAh(this.gpsPowerMah)).append("mAh)");
        }
        if (this.cpuPowerMah != 0.0d) {
            sb.append(", cpuPowerMah(").append(BatteryStatsHelper.makemAh(this.cpuPowerMah)).append("mAh)");
        }
        if (this.sensorPowerMah != 0.0d) {
            sb.append(", wifiPowerMah(").append(BatteryStatsHelper.makemAh(this.sensorPowerMah)).append("mAh)");
        }
        if (this.mobileRadioPowerMah != 0.0d) {
            sb.append(", mobileRadioPowerMah(").append(BatteryStatsHelper.makemAh(this.mobileRadioPowerMah)).append("mAh)");
        }
        if (this.wakeLockPowerMah != 0.0d) {
            sb.append(", wakeLockPowerMah(").append(BatteryStatsHelper.makemAh(this.wakeLockPowerMah)).append("mAh)");
        }
        if (this.cameraPowerMah != 0.0d) {
            sb.append(", cameraPowerMah(").append(BatteryStatsHelper.makemAh(this.cameraPowerMah)).append("mAh)");
        }
        if (this.flashlightPowerMah != 0.0d) {
            sb.append(", flashlightPowerMah(").append(BatteryStatsHelper.makemAh(this.flashlightPowerMah)).append("mAh)");
        }
        if (this.bluetoothPowerMah != 0.0d) {
            sb.append(", bluetoothPowerMah(").append(BatteryStatsHelper.makemAh(this.bluetoothPowerMah)).append("mAh)");
        }
        if (this.screenPowerMah != 0.0d) {
            sb.append(", screenPowerMah(").append(BatteryStatsHelper.makemAh(this.screenPowerMah)).append("mAh)");
        }
        if (this.attributedPowerMah != 0.0d) {
            sb.append(", attributedPowerMah(").append(BatteryStatsHelper.makemAh(this.attributedPowerMah)).append("mAh)");
        }
        return sb.toString();
    }

    public void newSystemUidApk(long screenTimeMs, double powerScreen, String pkgName) {
        if (this.mSystemUidApks == null) {
            this.mSystemUidApks = new ArrayMap();
        }
        this.mSystemUidApks.put(pkgName, new SystemUidApk(this, screenTimeMs, powerScreen, pkgName));
    }

    public void setSystemUidApkCpuTime(String processName, long timeSec) {
        if (!(this.mSystemUidApks == null || this.mSystemUidApks == null || processName == null)) {
            SystemUidApk apk = (SystemUidApk) this.mSystemUidApks.get(processName);
            if (apk != null) {
                apk.mCpuTimeSec = timeSec;
            }
        }
    }

    public void calSystemUidApkCpuPower(long totalCpuTimeSec) {
        if (this.cpuPowerMah > 0.0d && this.mSystemUidApks != null && totalCpuTimeSec > 0) {
            double powerSeparated = 0.0d;
            for (int i = 0; i < this.mSystemUidApks.size(); i++) {
                SystemUidApk apk = (SystemUidApk) this.mSystemUidApks.valueAt(i);
                double ratio = (double) (apk.mCpuTimeSec / totalCpuTimeSec);
                if (ratio > 0.0d && ratio <= 1.0d) {
                    double powerCpu = this.cpuPowerMah * ratio;
                    apk.mPowerCpuMah = powerCpu;
                    apk.mPowerMah += powerCpu;
                    powerSeparated += powerCpu;
                }
            }
            this.cpuPowerMah -= powerSeparated;
            if (this.cpuPowerMah < 0.0d) {
                this.cpuPowerMah = 0.0d;
            }
        }
    }
}
