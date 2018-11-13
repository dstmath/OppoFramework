package com.android.internal.net;

import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ProcFileReader;
import com.android.server.NetworkManagementSocketTagger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Objects;
import libcore.io.IoUtils;

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
public class NetworkStatsFactory {
    private static final boolean DEBUG = false;
    private static final boolean SANITY_CHECK_NATIVE = false;
    private static final String TAG = "NetworkStatsFactory";
    private static final boolean USE_NATIVE_PARSING = true;
    @GuardedBy("sStackedIfaces")
    private static final ArrayMap<String, String> sStackedIfaces = null;
    private final File mStatsXtIfaceAll;
    private final File mStatsXtIfaceFmt;
    private final File mStatsXtUid;
    private final File mStatsXtUidWithPids;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.NetworkStatsFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.NetworkStatsFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.NetworkStatsFactory.<clinit>():void");
    }

    public static native int nativeReadNetworkStatsDetail(NetworkStats networkStats, String str, int i, String[] strArr, int i2);

    public static native int nativeReadNetworkStatsDetailWithPids(NetworkStats networkStats, String str, int i);

    public static void noteStackedIface(String stackedIface, String baseIface) {
        synchronized (sStackedIfaces) {
            if (baseIface != null) {
                sStackedIfaces.put(stackedIface, baseIface);
            } else {
                sStackedIfaces.remove(stackedIface);
            }
        }
    }

    public NetworkStatsFactory() {
        this(new File("/proc/"));
    }

    public NetworkStatsFactory(File procRoot) {
        this.mStatsXtIfaceAll = new File(procRoot, "net/xt_qtaguid/iface_stat_all");
        this.mStatsXtIfaceFmt = new File(procRoot, "net/xt_qtaguid/iface_stat_fmt");
        this.mStatsXtUid = new File(procRoot, "net/xt_qtaguid/stats");
        this.mStatsXtUidWithPids = new File(procRoot, "net/xt_qtaguid/stats_pid");
    }

    public NetworkStats readNetworkStatsSummaryDev() throws IOException {
        NullPointerException e;
        Throwable th;
        NumberFormatException e2;
        Object reader;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable reader2 = null;
        try {
            ProcFileReader reader3 = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceAll));
            while (reader3.hasMoreData()) {
                try {
                    entry.iface = reader3.nextString();
                    entry.uid = -1;
                    entry.set = -1;
                    entry.tag = 0;
                    boolean active = reader3.nextInt() != 0;
                    entry.rxBytes = reader3.nextLong();
                    entry.rxPackets = reader3.nextLong();
                    entry.txBytes = reader3.nextLong();
                    entry.txPackets = reader3.nextLong();
                    if (active) {
                        entry.rxBytes += reader3.nextLong();
                        entry.rxPackets += reader3.nextLong();
                        entry.txBytes += reader3.nextLong();
                        entry.txPackets += reader3.nextLong();
                    }
                    stats.addValues(entry);
                    reader3.finishLine();
                } catch (NullPointerException e3) {
                    e = e3;
                    reader2 = reader3;
                    try {
                        throw new ProtocolException("problem parsing stats", e);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(reader2);
                        StrictMode.setThreadPolicy(savedPolicy);
                        throw th;
                    }
                } catch (NumberFormatException e4) {
                    e2 = e4;
                    reader2 = reader3;
                    throw new ProtocolException("problem parsing stats", e2);
                } catch (Throwable th3) {
                    th = th3;
                    reader2 = reader3;
                    IoUtils.closeQuietly(reader2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            }
            IoUtils.closeQuietly(reader3);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException e5) {
            e = e5;
            throw new ProtocolException("problem parsing stats", e);
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing stats", e2);
        }
    }

    public NetworkStats readNetworkStatsSummaryXt() throws IOException {
        NullPointerException e;
        Throwable th;
        NumberFormatException e2;
        Object reader;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (!this.mStatsXtIfaceFmt.exists()) {
            return null;
        }
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable reader2 = null;
        try {
            ProcFileReader reader3 = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceFmt));
            try {
                reader3.finishLine();
                while (reader3.hasMoreData()) {
                    entry.iface = reader3.nextString();
                    entry.uid = -1;
                    entry.set = -1;
                    entry.tag = 0;
                    entry.rxBytes = reader3.nextLong();
                    entry.rxPackets = reader3.nextLong();
                    entry.txBytes = reader3.nextLong();
                    entry.txPackets = reader3.nextLong();
                    stats.addValues(entry);
                    reader3.finishLine();
                }
                IoUtils.closeQuietly(reader3);
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            } catch (NullPointerException e3) {
                e = e3;
                reader2 = reader3;
                try {
                    throw new ProtocolException("problem parsing stats", e);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(reader2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (NumberFormatException e4) {
                e2 = e4;
                reader2 = reader3;
                throw new ProtocolException("problem parsing stats", e2);
            } catch (Throwable th3) {
                th = th3;
                reader2 = reader3;
                IoUtils.closeQuietly(reader2);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (NullPointerException e5) {
            e = e5;
            throw new ProtocolException("problem parsing stats", e);
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing stats", e2);
        }
    }

    public NetworkStats readNetworkStatsDetail() throws IOException {
        return readNetworkStatsDetail(-1, null, -1, null);
    }

    public NetworkStats readNetworkStatsDetail(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        int i;
        Entry entry;
        NetworkStats stats = readNetworkStatsDetailInternal(limitUid, limitIfaces, limitTag, lastStats);
        synchronized (sStackedIfaces) {
            int size = sStackedIfaces.size();
            for (i = 0; i < size; i++) {
                String stackedIface = (String) sStackedIfaces.keyAt(i);
                Entry adjust = new Entry((String) sStackedIfaces.valueAt(i), 0, 0, 0, 0, 0, 0, 0, 0);
                entry = null;
                for (int j = 0; j < stats.size(); j++) {
                    entry = stats.getValues(j, entry);
                    if (Objects.equals(entry.iface, stackedIface)) {
                        adjust.txBytes -= entry.txBytes;
                        adjust.txPackets -= entry.txPackets;
                    }
                }
                if (adjust.txBytes < 0) {
                    if (stats.findIndex(adjust.iface, adjust.uid, adjust.set, adjust.tag, adjust.roaming) == -1) {
                        Slog.e(TAG, "negative entry in adjust.txBytes:" + adjust.txBytes);
                        adjust.txBytes = Math.max(adjust.txBytes, 0);
                    }
                }
                if (adjust.txPackets < 0) {
                    if (stats.findIndex(adjust.iface, adjust.uid, adjust.set, adjust.tag, adjust.roaming) == -1) {
                        Slog.e(TAG, "negative entry in adjust.txPackets:" + adjust.txPackets);
                        adjust.txPackets = Math.max(adjust.txPackets, 0);
                    }
                }
                stats.combineValues(adjust);
            }
        }
        entry = null;
        for (i = 0; i < stats.size(); i++) {
            entry = stats.getValues(i, entry);
            if (entry.iface != null && entry.iface.startsWith("clat")) {
                entry.rxBytes = entry.rxPackets * 20;
                entry.rxPackets = 0;
                entry.txBytes = 0;
                entry.txPackets = 0;
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    private NetworkStats readNetworkStatsDetailInternal(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        NetworkStats stats;
        if (lastStats != null) {
            stats = lastStats;
            lastStats.setElapsedRealtime(SystemClock.elapsedRealtime());
        } else {
            stats = new NetworkStats(SystemClock.elapsedRealtime(), -1);
        }
        if (nativeReadNetworkStatsDetail(stats, this.mStatsXtUid.getAbsolutePath(), limitUid, limitIfaces, limitTag) == 0) {
            return stats;
        }
        throw new IOException("Failed to parse network stats");
    }

    public NetworkStats readNetworkStatsDetailWithPids() throws IOException {
        return readNetworkStatsDetailWithPids(-1);
    }

    public NetworkStats readNetworkStatsDetailWithPids(int limitUid) throws IOException {
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 0);
        stats.setContainExtendDataFlag(true);
        if (nativeReadNetworkStatsDetailWithPids(stats, this.mStatsXtUidWithPids.getAbsolutePath(), limitUid) == 0) {
            return stats;
        }
        throw new IOException("readNetworkStatsDetailWithPids:Failed to parse network stats with pid");
    }

    public static NetworkStats javaReadNetworkStatsDetailWithPids(File detailPath, int limitUid) throws IOException {
        return null;
    }

    /* JADX WARNING: Missing block: B:33:0x00fd, code:
            if (com.android.internal.util.ArrayUtils.contains((java.lang.Object[]) r14, r2.iface) != false) goto L_0x00ff;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static NetworkStats javaReadNetworkStatsDetail(File detailPath, int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        NullPointerException e;
        Throwable th;
        NumberFormatException e2;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        Entry entry = new Entry();
        int idx = 1;
        int lastIdx = 1;
        AutoCloseable reader = null;
        try {
            ProcFileReader reader2 = new ProcFileReader(new FileInputStream(detailPath));
            try {
                reader2.finishLine();
                while (reader2.hasMoreData()) {
                    idx = reader2.nextInt();
                    if (idx != lastIdx + 1) {
                        throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                    }
                    lastIdx = idx;
                    entry.iface = reader2.nextString();
                    entry.tag = NetworkManagementSocketTagger.kernelToTag(reader2.nextString());
                    entry.uid = reader2.nextInt();
                    entry.set = reader2.nextInt();
                    entry.rxBytes = reader2.nextLong();
                    entry.rxPackets = reader2.nextLong();
                    entry.txBytes = reader2.nextLong();
                    entry.txPackets = reader2.nextLong();
                    if (entry.isNegative()) {
                        Slog.e(TAG, "negative entry found in advance:" + entry);
                        throw new IllegalArgumentException("tried recording negative data in advance");
                    }
                    if (limitIfaces != null) {
                    }
                    if ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag)) {
                        stats.addValues(entry);
                    }
                    reader2.finishLine();
                }
                IoUtils.closeQuietly(reader2);
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            } catch (NullPointerException e3) {
                e = e3;
                reader = reader2;
                try {
                    throw new ProtocolException("problem parsing idx " + idx, e);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(reader);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (NumberFormatException e4) {
                e2 = e4;
                reader = reader2;
                throw new ProtocolException("problem parsing idx " + idx, e2);
            } catch (Throwable th3) {
                th = th3;
                Object reader3 = reader2;
                IoUtils.closeQuietly(reader);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (NullPointerException e5) {
            e = e5;
            throw new ProtocolException("problem parsing idx " + idx, e);
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing idx " + idx, e2);
        }
    }

    public void assertEquals(NetworkStats expected, NetworkStats actual) {
        if (expected.size() != actual.size()) {
            throw new AssertionError("Expected size " + expected.size() + ", actual size " + actual.size());
        }
        Entry expectedRow = null;
        Entry actualRow = null;
        int i = 0;
        while (i < expected.size()) {
            expectedRow = expected.getValues(i, expectedRow);
            actualRow = actual.getValues(i, actualRow);
            if (expectedRow.equals(actualRow)) {
                i++;
            } else {
                throw new AssertionError("Expected row " + i + ": " + expectedRow + ", actual row " + actualRow);
            }
        }
    }
}
