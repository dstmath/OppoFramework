package com.android.server.am;

import android.content.res.Resources;
import android.graphics.Point;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.MemInfoReader;
import com.android.server.display.OppoBrightUtils;
import com.android.server.wm.WindowManagerService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
final class ProcessList {
    static final int BACKUP_APP_ADJ = 300;
    static final int CACHED_APP_MAX_ADJ = 906;
    static final int CACHED_APP_MIN_ADJ = 900;
    static final int FOREGROUND_APP_ADJ = 0;
    static final int HEAVY_WEIGHT_APP_ADJ = 400;
    static final int HOME_APP_ADJ = 600;
    static final int INVALID_ADJ = -10000;
    static final byte LMK_PROCPRIO = (byte) 1;
    static final byte LMK_PROCREMOVE = (byte) 2;
    static final byte LMK_TARGET = (byte) 0;
    static final int MAX_CACHED_APPS = 32;
    private static final int MAX_EMPTY_APPS = 0;
    static final long MAX_EMPTY_TIME = 1800000;
    static final int MIN_CACHED_APPS = 2;
    static final int MIN_CRASH_INTERVAL = 60000;
    static final int NATIVE_ADJ = -1000;
    static final int PAGE_SIZE = 4096;
    static final int PERCEPTIBLE_APP_ADJ = 200;
    static final int PERSISTENT_PROC_ADJ = -800;
    static final int PERSISTENT_SERVICE_ADJ = -700;
    static final int PREVIOUS_APP_ADJ = 700;
    public static final int PROC_MEM_CACHED = 4;
    public static final int PROC_MEM_IMPORTANT = 2;
    public static final int PROC_MEM_PERSISTENT = 0;
    public static final int PROC_MEM_SERVICE = 3;
    public static final int PROC_MEM_TOP = 1;
    public static final int PSS_ALL_INTERVAL = 600000;
    private static final int PSS_FIRST_BACKGROUND_INTERVAL = 20000;
    private static final int PSS_FIRST_CACHED_INTERVAL = 30000;
    private static final int PSS_FIRST_TOP_INTERVAL = 10000;
    public static final int PSS_MAX_INTERVAL = 1800000;
    public static final int PSS_MIN_TIME_FROM_STATE_CHANGE = 15000;
    public static final int PSS_SAFE_TIME_FROM_STATE_CHANGE = 1000;
    private static final int PSS_SAME_CACHED_INTERVAL = 1800000;
    private static final int PSS_SAME_IMPORTANT_INTERVAL = 900000;
    private static final int PSS_SAME_SERVICE_INTERVAL = 1200000;
    private static final int PSS_SHORT_INTERVAL = 120000;
    private static final int PSS_TEST_FIRST_BACKGROUND_INTERVAL = 5000;
    private static final int PSS_TEST_FIRST_TOP_INTERVAL = 3000;
    public static final int PSS_TEST_MIN_TIME_FROM_STATE_CHANGE = 10000;
    private static final int PSS_TEST_SAME_BACKGROUND_INTERVAL = 15000;
    private static final int PSS_TEST_SAME_IMPORTANT_INTERVAL = 10000;
    static final int SCHED_GROUP_BACKGROUND = 0;
    static final int SCHED_GROUP_DEFAULT = 1;
    static final int SCHED_GROUP_TOP_APP = 2;
    static final int SCHED_GROUP_TOP_APP_BOUND = 3;
    static final int SERVICE_ADJ = 500;
    static final int SERVICE_B_ADJ = 800;
    static final int SYSTEM_ADJ = -900;
    private static final String TAG = null;
    static final int TRIM_CACHED_APPS = 0;
    static final int TRIM_CRITICAL_THRESHOLD = 3;
    static final int TRIM_EMPTY_APPS = 0;
    static final int TRIM_LOW_THRESHOLD = 5;
    static final int UNKNOWN_ADJ = 1001;
    static final int VISIBLE_APP_ADJ = 100;
    static final int VISIBLE_APP_LAYER_MAX = 99;
    private static final long[] sFirstAwakePssTimes = null;
    private static OutputStream sLmkdOutputStream;
    private static LocalSocket sLmkdSocket;
    private static final int[] sProcStateToProcMem = null;
    private static final long[] sSameAwakePssTimes = null;
    private static final long[] sTestFirstAwakePssTimes = null;
    private static final long[] sTestSameAwakePssTimes = null;
    final long HIGH_LEVEL_SIZE;
    final long LOW_LEVEL_SIZE;
    private long mCachedRestoreLevel;
    private boolean mHaveDisplaySize;
    private int[] mOomAdj;
    private int[] mOomMinFree;
    private final int[] mOomMinFreeHigh;
    private final int[] mOomMinFreeLow;
    private final long mTotalMemMb;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ProcessList.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ProcessList.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ProcessList.<clinit>():void");
    }

    ProcessList() {
        this.mOomMinFreeLow = new int[]{12288, 18432, 24576, 36864, 43008, 49152};
        this.mOomMinFreeHigh = new int[]{73728, 92160, 110592, 129024, 147456, 184320};
        this.LOW_LEVEL_SIZE = 512;
        this.HIGH_LEVEL_SIZE = 1024;
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        this.mTotalMemMb = minfo.getTotalSize() / 1048576;
        this.mOomAdj = Resources.getSystem().getIntArray(134479880);
        this.mOomMinFree = Resources.getSystem().getIntArray(134479881);
        if (this.mOomAdj == null || this.mOomMinFree == null || this.mOomMinFree[0] == 0) {
            if (this.mTotalMemMb <= 512) {
                this.mOomAdj = new int[]{0, 100, 200, 300, CACHED_APP_MIN_ADJ, CACHED_APP_MAX_ADJ};
                this.mOomMinFree = new int[]{24576, 31744, 38912, 49152, 122880, 122880};
            } else if (this.mTotalMemMb <= 1024) {
                this.mOomAdj = new int[]{0, 100, 200, 300, CACHED_APP_MIN_ADJ, CACHED_APP_MAX_ADJ};
                this.mOomMinFree = new int[]{36864, 49152, 61440, 73728, 204800, 296960};
            } else {
                this.mOomAdj = new int[]{0, 100, 200, 300, CACHED_APP_MIN_ADJ, CACHED_APP_MAX_ADJ};
                this.mOomMinFree = new int[this.mOomAdj.length];
            }
        }
        updateOomLevels(0, 0, false);
    }

    void applyDisplaySize(WindowManagerService wm) {
        if (!this.mHaveDisplaySize) {
            Point p = new Point();
            wm.getBaseDisplaySize(0, p);
            if (p.x != 0 && p.y != 0) {
                updateOomLevels(p.x, p.y, true);
                this.mHaveDisplaySize = true;
            }
        }
    }

    private void updateOomLevels(int displayWidth, int displayHeight, boolean write) {
        ByteBuffer buf;
        int i;
        int reserve;
        if (this.mTotalMemMb <= 1024) {
            this.mCachedRestoreLevel = (getMemLevel(CACHED_APP_MAX_ADJ) / 1024) / 3;
            Slog.i(TAG, "mCachedRestoreLevel = " + Long.toString(this.mCachedRestoreLevel));
            if (write) {
                buf = ByteBuffer.allocate(((this.mOomAdj.length * 2) + 1) * 4);
                buf.putInt(0);
                for (i = 0; i < this.mOomAdj.length; i++) {
                    buf.putInt((this.mOomMinFree[i] * 1024) / 4096);
                    buf.putInt(this.mOomAdj[i]);
                }
                writeLmkd(buf);
                if (this.mTotalMemMb > 512) {
                    reserve = Resources.getSystem().getInteger(134938636);
                    if (reserve < 0) {
                        reserve = (((displayWidth * displayHeight) * 4) * 3) / 1024;
                    }
                    SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(reserve));
                }
            }
            return;
        }
        float scale;
        float scaleMem = ((float) (this.mTotalMemMb - 350)) / 350.0f;
        float scaleDisp = (((float) (displayWidth * displayHeight)) - 384000.0f) / ((float) 640000);
        if (scaleMem > scaleDisp) {
            scale = scaleMem;
        } else {
            scale = scaleDisp;
        }
        if (scale < OppoBrightUtils.MIN_LUX_LIMITI) {
            scale = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (scale > 1.0f) {
            scale = 1.0f;
        }
        int minfree_adj = Resources.getSystem().getInteger(17694729);
        int minfree_abs = Resources.getSystem().getInteger(17694728);
        boolean is64bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;
        for (i = 0; i < this.mOomAdj.length; i++) {
            int low = this.mOomMinFreeLow[i];
            int high = this.mOomMinFreeHigh[i];
            if (is64bit) {
                if (i == 4) {
                    high = (high * 3) / 2;
                } else if (i == 5) {
                    high = (high * 7) / 4;
                }
            }
            this.mOomMinFree[i] = (int) (((float) low) + (((float) (high - low)) * scale));
        }
        if (minfree_abs >= 0) {
            for (i = 0; i < this.mOomAdj.length; i++) {
                this.mOomMinFree[i] = (int) ((((float) minfree_abs) * ((float) this.mOomMinFree[i])) / ((float) this.mOomMinFree[this.mOomAdj.length - 1]));
            }
        }
        if (minfree_adj != 0) {
            for (i = 0; i < this.mOomAdj.length; i++) {
                int[] iArr = this.mOomMinFree;
                iArr[i] = iArr[i] + ((int) ((((float) minfree_adj) * ((float) this.mOomMinFree[i])) / ((float) this.mOomMinFree[this.mOomAdj.length - 1])));
                if (this.mOomMinFree[i] < 0) {
                    this.mOomMinFree[i] = 0;
                }
            }
        }
        this.mCachedRestoreLevel = (getMemLevel(CACHED_APP_MAX_ADJ) / 1024) / 3;
        Slog.i(TAG, "mCachedRestoreLevel = " + Long.toString(this.mCachedRestoreLevel));
        reserve = (((displayWidth * displayHeight) * 4) * 3) / 1024;
        int reserve_adj = Resources.getSystem().getInteger(17694731);
        int reserve_abs = Resources.getSystem().getInteger(17694730);
        if (reserve_abs >= 0) {
            reserve = reserve_abs;
        }
        if (reserve_adj != 0) {
            reserve += reserve_adj;
            if (reserve < 0) {
                reserve = 0;
            }
        }
        if (write) {
            buf = ByteBuffer.allocate(((this.mOomAdj.length * 2) + 1) * 4);
            buf.putInt(0);
            for (i = 0; i < this.mOomAdj.length; i++) {
                buf.putInt((this.mOomMinFree[i] * 1024) / 4096);
                buf.putInt(this.mOomAdj[i]);
            }
            writeLmkd(buf);
            SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(reserve));
        }
    }

    public static int computeEmptyProcessLimit(int totalProcessLimit) {
        return totalProcessLimit / 2;
    }

    private static String buildOomTag(String prefix, String space, int val, int base) {
        if (val != base) {
            return prefix + "+" + Integer.toString(val - base);
        }
        if (space == null) {
            return prefix;
        }
        return prefix + "  ";
    }

    public static String makeOomAdjString(int setAdj) {
        if (setAdj >= CACHED_APP_MIN_ADJ) {
            return buildOomTag("cch", "  ", setAdj, CACHED_APP_MIN_ADJ);
        }
        if (setAdj >= SERVICE_B_ADJ) {
            return buildOomTag("svcb ", null, setAdj, SERVICE_B_ADJ);
        }
        if (setAdj >= PREVIOUS_APP_ADJ) {
            return buildOomTag("prev ", null, setAdj, PREVIOUS_APP_ADJ);
        }
        if (setAdj >= 600) {
            return buildOomTag("home ", null, setAdj, 600);
        }
        if (setAdj >= 500) {
            return buildOomTag("svc  ", null, setAdj, 500);
        }
        if (setAdj >= 400) {
            return buildOomTag("hvy  ", null, setAdj, 400);
        }
        if (setAdj >= 300) {
            return buildOomTag("bkup ", null, setAdj, 300);
        }
        if (setAdj >= 200) {
            return buildOomTag("prcp ", null, setAdj, 200);
        }
        if (setAdj >= 100) {
            return buildOomTag("vis  ", null, setAdj, 100);
        }
        if (setAdj >= 0) {
            return buildOomTag("fore ", null, setAdj, 0);
        }
        if (setAdj >= PERSISTENT_SERVICE_ADJ) {
            return buildOomTag("psvc ", null, setAdj, PERSISTENT_SERVICE_ADJ);
        }
        if (setAdj >= PERSISTENT_PROC_ADJ) {
            return buildOomTag("pers ", null, setAdj, PERSISTENT_PROC_ADJ);
        }
        if (setAdj >= SYSTEM_ADJ) {
            return buildOomTag("sys  ", null, setAdj, SYSTEM_ADJ);
        }
        if (setAdj >= -1000) {
            return buildOomTag("ntv  ", null, setAdj, -1000);
        }
        return Integer.toString(setAdj);
    }

    public static String makeProcStateString(int curProcState) {
        switch (curProcState) {
            case -1:
                return "N ";
            case 0:
                return "P ";
            case 1:
                return "PU";
            case 2:
                return "T ";
            case 3:
                return "SB";
            case 4:
                return "SF";
            case 5:
                return "TS";
            case 6:
                return "IF";
            case 7:
                return "IB";
            case 8:
                return "BU";
            case 9:
                return "HW";
            case 10:
                return "S ";
            case 11:
                return "R ";
            case 12:
                return "HO";
            case 13:
                return "LA";
            case 14:
                return "CA";
            case 15:
                return "Ca";
            case 16:
                return "CE";
            default:
                return "??";
        }
    }

    public static void appendRamKb(StringBuilder sb, long ramKb) {
        int j = 0;
        int fact = 10;
        while (j < 6) {
            if (ramKb < ((long) fact)) {
                sb.append(' ');
            }
            j++;
            fact *= 10;
        }
        sb.append(ramKb);
    }

    public static boolean procStatesDifferForMem(int procState1, int procState2) {
        return sProcStateToProcMem[procState1] != sProcStateToProcMem[procState2];
    }

    public static long minTimeFromStateChange(boolean test) {
        return (long) (test ? 10000 : 15000);
    }

    public static long computeNextPssTime(int procState, boolean first, boolean test, boolean sleeping, long now) {
        long[] table;
        if (test) {
            if (first) {
                table = sTestFirstAwakePssTimes;
            } else {
                table = sTestSameAwakePssTimes;
            }
        } else if (first) {
            table = sFirstAwakePssTimes;
        } else {
            table = sSameAwakePssTimes;
        }
        return table[procState] + now;
    }

    long getMemLevel(int adjustment) {
        for (int i = 0; i < this.mOomAdj.length; i++) {
            if (adjustment <= this.mOomAdj[i]) {
                return (long) (this.mOomMinFree[i] * 1024);
            }
        }
        return (long) (this.mOomMinFree[this.mOomAdj.length - 1] * 1024);
    }

    long getCachedRestoreThresholdKb() {
        return this.mCachedRestoreLevel;
    }

    public static final void setOomAdj(int pid, int uid, int amt) {
        if (amt != 1001) {
            if (Process.getUidForPid(pid) != uid) {
                Slog.w("ActivityManager", "Process " + pid + " does not match uid " + uid + " ignore setting hits oom adj");
                return;
            }
            long start = SystemClock.elapsedRealtime();
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.putInt(1);
            buf.putInt(pid);
            buf.putInt(uid);
            buf.putInt(amt);
            writeLmkd(buf);
            long now = SystemClock.elapsedRealtime();
            if (now - start > 250) {
                Slog.w("ActivityManager", "SLOW OOM ADJ: " + (now - start) + "ms for pid " + pid + " = " + amt);
            }
        }
    }

    public static final void remove(int pid) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putInt(2);
        buf.putInt(pid);
        writeLmkd(buf);
    }

    private static boolean openLmkdSocket() {
        try {
            sLmkdSocket = new LocalSocket(3);
            sLmkdSocket.connect(new LocalSocketAddress("lmkd", Namespace.RESERVED));
            sLmkdOutputStream = sLmkdSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "lowmemorykiller daemon socket open failed");
            sLmkdSocket = null;
            return false;
        }
    }

    private static void writeLmkd(ByteBuffer buf) {
        int i = 0;
        while (i < 3) {
            if (sLmkdSocket != null || openLmkdSocket()) {
                try {
                    sLmkdOutputStream.write(buf.array(), 0, buf.position());
                    return;
                } catch (IOException e) {
                    Slog.w(TAG, "Error writing to lowmemorykiller socket");
                    try {
                        sLmkdSocket.close();
                    } catch (IOException e2) {
                    }
                    sLmkdSocket = null;
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e3) {
                }
                i++;
            }
        }
    }

    static void exportProcessADJ() {
    }
}
