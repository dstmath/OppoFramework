package com.android.internal.os;

import android.os.SystemClock;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
public class KernelUidCpuTimeReader {
    private static final boolean OPPODEBUG = false;
    private static final String TAG = "KernelUidCpuTimeReader";
    private static final String sProcFile = "/proc/uid_cputime/show_uid_stat";
    private static final String sRemoveUidProcFile = "/proc/uid_cputime/remove_uid_range";
    private SparseLongArray mLastPowerMaUs;
    private SparseLongArray mLastSystemTimeUs;
    private long mLastTimeReadUs;
    private SparseLongArray mLastUserTimeUs;

    public interface Callback {
        void onUidCpuTime(int i, long j, long j2, long j3);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.KernelUidCpuTimeReader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.KernelUidCpuTimeReader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.KernelUidCpuTimeReader.<clinit>():void");
    }

    public KernelUidCpuTimeReader() {
        this.mLastUserTimeUs = new SparseLongArray();
        this.mLastSystemTimeUs = new SparseLongArray();
        this.mLastPowerMaUs = new SparseLongArray();
        this.mLastTimeReadUs = 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x0377 A:{SYNTHETIC, Splitter: B:69:0x0377} */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0400 A:{Catch:{ IOException -> 0x037d }} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x037c A:{SYNTHETIC, Splitter: B:72:0x037c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDelta(Callback callback) {
        Throwable th;
        long nowUs = SystemClock.elapsedRealtime() * 1000;
        Throwable th2 = null;
        BufferedReader reader = null;
        IOException e;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(sProcFile));
            try {
                SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(' ');
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    long powerMaUs;
                    simpleStringSplitter.setString(line);
                    String uidStr = simpleStringSplitter.next();
                    int uid = Integer.parseInt(uidStr.substring(0, uidStr.length() - 1), 10);
                    long userTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    long systemTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    if (simpleStringSplitter.hasNext()) {
                        powerMaUs = Long.parseLong(simpleStringSplitter.next(), 10) / 1000;
                    } else {
                        powerMaUs = 0;
                    }
                    if (!(callback == null || this.mLastTimeReadUs == 0)) {
                        long userTimeDeltaUs = userTimeUs;
                        long systemTimeDeltaUs = systemTimeUs;
                        long powerDeltaMaUs = powerMaUs;
                        int index = this.mLastUserTimeUs.indexOfKey(uid);
                        if (index >= 0) {
                            userTimeDeltaUs = userTimeUs - this.mLastUserTimeUs.valueAt(index);
                            systemTimeDeltaUs = systemTimeUs - this.mLastSystemTimeUs.valueAt(index);
                            powerDeltaMaUs -= this.mLastPowerMaUs.valueAt(index);
                            long timeDiffUs = nowUs - this.mLastTimeReadUs;
                            if (userTimeDeltaUs < 0 || systemTimeDeltaUs < 0 || powerDeltaMaUs < 0) {
                                StringBuilder stringBuilder = new StringBuilder("Malformed cpu data for UID=");
                                stringBuilder.append(uid).append("!\n");
                                stringBuilder.append("Time between reads: ");
                                TimeUtils.formatDuration(timeDiffUs / 1000, stringBuilder);
                                stringBuilder.append("\n");
                                stringBuilder.append("Previous times: u=");
                                TimeUtils.formatDuration(this.mLastUserTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(this.mLastSystemTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder.append(" p=").append(this.mLastPowerMaUs.valueAt(index) / 1000);
                                stringBuilder.append("mAms\n");
                                stringBuilder.append("Current times: u=");
                                TimeUtils.formatDuration(userTimeUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeUs / 1000, stringBuilder);
                                stringBuilder.append(" p=").append(powerMaUs / 1000);
                                stringBuilder.append("mAms\n");
                                stringBuilder.append("Delta: u=");
                                TimeUtils.formatDuration(userTimeDeltaUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeDeltaUs / 1000, stringBuilder);
                                stringBuilder.append(" p=").append(powerDeltaMaUs / 1000).append("mAms");
                                Slog.e(TAG, stringBuilder.toString());
                                userTimeDeltaUs = 0;
                                systemTimeDeltaUs = 0;
                                powerDeltaMaUs = 0;
                            }
                        }
                        if (userTimeDeltaUs < 0) {
                            if (OPPODEBUG) {
                                Slog.d(TAG, "userTimeDeltaUs=" + userTimeDeltaUs + ", uid=" + uid);
                            }
                            userTimeDeltaUs = 0;
                        }
                        if (systemTimeDeltaUs < 0) {
                            if (OPPODEBUG) {
                                Slog.d(TAG, "systemTimeDeltaUs=" + systemTimeDeltaUs + ", uid=" + uid);
                            }
                            systemTimeDeltaUs = 0;
                        }
                        if (powerDeltaMaUs < 0) {
                            if (OPPODEBUG) {
                                Slog.d(TAG, "powerDeltaMaUs=" + powerDeltaMaUs + ", uid=" + uid);
                            }
                            powerDeltaMaUs = 0;
                        }
                        if (!(userTimeDeltaUs == 0 && systemTimeDeltaUs == 0 && powerDeltaMaUs == 0)) {
                            callback.onUidCpuTime(uid, userTimeDeltaUs, systemTimeDeltaUs, powerDeltaMaUs);
                        }
                    }
                    if (userTimeUs < 0) {
                        if (OPPODEBUG) {
                            Slog.d(TAG, "userTimeUs=" + userTimeUs + ", uid=" + uid);
                        }
                        this.mLastUserTimeUs.put(uid, 0);
                    } else {
                        this.mLastUserTimeUs.put(uid, userTimeUs);
                    }
                    if (systemTimeUs < 0) {
                        if (OPPODEBUG) {
                            Slog.d(TAG, "systemTimeUs=" + systemTimeUs + ", uid=" + uid);
                        }
                        this.mLastSystemTimeUs.put(uid, 0);
                    } else {
                        this.mLastSystemTimeUs.put(uid, systemTimeUs);
                    }
                    if (powerMaUs < 0) {
                        if (OPPODEBUG) {
                            Slog.d(TAG, "powerMaUs=" + powerMaUs + ", uid=" + uid);
                        }
                        this.mLastPowerMaUs.put(uid, 0);
                    } else {
                        this.mLastPowerMaUs.put(uid, powerMaUs);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        reader = bufferedReader;
                    }
                } else {
                    reader = bufferedReader;
                    this.mLastTimeReadUs = nowUs;
                }
            } catch (Throwable th4) {
                th = th4;
                reader = bufferedReader;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
            Slog.e(TAG, "Failed to read uid_cputime: " + e.getMessage());
            this.mLastTimeReadUs = nowUs;
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
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e3) {
                    e = e3;
                }
            } else {
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0067 A:{SYNTHETIC, Splitter: B:25:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x007a A:{Catch:{ IOException -> 0x006d }} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006c A:{SYNTHETIC, Splitter: B:28:0x006c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeUid(int uid) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        int index = this.mLastUserTimeUs.indexOfKey(uid);
        if (index >= 0) {
            this.mLastUserTimeUs.removeAt(index);
            this.mLastSystemTimeUs.removeAt(index);
            this.mLastPowerMaUs.removeAt(index);
        }
        FileWriter writer = null;
        try {
            FileWriter writer2 = new FileWriter(sRemoveUidProcFile);
            try {
                writer2.write(Integer.toString(uid) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + Integer.toString(uid));
                writer2.flush();
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        writer = writer2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                writer = writer2;
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Slog.e(TAG, "failed to remove uid from uid_cputime module", e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (writer != null) {
            }
            if (th2 == null) {
            }
        }
    }
}
