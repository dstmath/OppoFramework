package com.android.internal.os;

import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import libcore.io.Libcore;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class KernelCpuSpeedReader {
    private static final boolean OPPODEBUG = false;
    private static final String TAG = "KernelCpuSpeedReader";
    private final long[] mDeltaSpeedTimes;
    private final long mJiffyMillis;
    private final long[] mLastSpeedTimes;
    private final String mProcFile;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.KernelCpuSpeedReader.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.KernelCpuSpeedReader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.KernelCpuSpeedReader.<clinit>():void");
    }

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(cpuNumber);
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", objArr);
        this.mLastSpeedTimes = new long[numSpeedSteps];
        this.mDeltaSpeedTimes = new long[numSpeedSteps];
        this.mJiffyMillis = 1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK);
    }

    public long[] readDelta() {
        Throwable th;
        Throwable th2 = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mProcFile));
            try {
                SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
                int speedIndex = 0;
                while (speedIndex < this.mLastSpeedTimes.length) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    try {
                        splitter.setString(line);
                        Long.parseLong(splitter.next());
                        long time = Long.parseLong(splitter.next()) * this.mJiffyMillis;
                        if (time < this.mLastSpeedTimes[speedIndex]) {
                            this.mDeltaSpeedTimes[speedIndex] = time;
                        } else {
                            this.mDeltaSpeedTimes[speedIndex] = time - this.mLastSpeedTimes[speedIndex];
                        }
                        this.mLastSpeedTimes[speedIndex] = time;
                        if (this.mDeltaSpeedTimes[speedIndex] < 0) {
                            if (OPPODEBUG) {
                                Slog.d(TAG, "mDeltaSpeedTimes[" + speedIndex + "]=" + this.mDeltaSpeedTimes[speedIndex]);
                            }
                            this.mDeltaSpeedTimes[speedIndex] = 0;
                        }
                        if (this.mLastSpeedTimes[speedIndex] < 0) {
                            if (OPPODEBUG) {
                                Slog.d(TAG, "mLastSpeedTimes[" + speedIndex + "]=" + this.mLastSpeedTimes[speedIndex]);
                            }
                            this.mLastSpeedTimes[speedIndex] = 0;
                        }
                        speedIndex++;
                    } catch (NumberFormatException ex) {
                        Slog.e(TAG, "speedIndex = " + speedIndex + ", Failed to read cpu-freq: " + ex.getMessage());
                        this.mDeltaSpeedTimes[speedIndex] = 0;
                        this.mLastSpeedTimes[speedIndex] = 0;
                        speedIndex++;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e) {
                        reader = reader2;
                    }
                } else {
                    return this.mDeltaSpeedTimes;
                }
            } catch (Throwable th4) {
                th = th4;
                reader = reader2;
            }
        } catch (Throwable th5) {
            th = th5;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e2) {
                    Arrays.fill(this.mDeltaSpeedTimes, 0);
                    return this.mDeltaSpeedTimes;
                }
            }
            throw th;
        }
    }
}
