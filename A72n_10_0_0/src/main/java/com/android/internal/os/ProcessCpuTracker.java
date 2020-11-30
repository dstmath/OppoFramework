package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.OppoThermalManager;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.SettingsStringUtil;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.OppoBaseProcessCpuTracker;
import com.android.internal.util.FastPrintWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ProcessCpuTracker {
    private static final boolean DEBUG = false;
    private static final int[] LOAD_AVERAGE_FORMAT = {16416, 16416, 16416};
    private static final int[] PROCESS_FULL_STATS_FORMAT = {32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    static final int PROCESS_FULL_STAT_MAJOR_FAULTS = 2;
    static final int PROCESS_FULL_STAT_MINOR_FAULTS = 1;
    static final int PROCESS_FULL_STAT_STIME = 4;
    static final int PROCESS_FULL_STAT_UTIME = 3;
    static final int PROCESS_FULL_STAT_VSIZE = 5;
    private static final int[] PROCESS_STATS_FORMAT = {32, MetricsProto.MetricsEvent.DIALOG_WIFI_SKIP, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224};
    static final int PROCESS_STAT_MAJOR_FAULTS = 1;
    static final int PROCESS_STAT_MINOR_FAULTS = 0;
    static final int PROCESS_STAT_STIME = 3;
    static final int PROCESS_STAT_UTIME = 2;
    private static final int[] SYSTEM_CPU_FORMAT = {288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final String TAG = "ProcessCpuTracker";
    private static final boolean localLOGV = false;
    private static final Comparator<Stats> sLoadComparator = new Comparator<Stats>() {
        /* class com.android.internal.os.ProcessCpuTracker.AnonymousClass1 */

        public final int compare(Stats sta, Stats stb) {
            int ta = sta.rel_utime + sta.rel_stime;
            int tb = stb.rel_utime + stb.rel_stime;
            if (ta != tb) {
                return ta > tb ? -1 : 1;
            }
            if (sta.added != stb.added) {
                return sta.added ? -1 : 1;
            }
            if (sta.removed != stb.removed) {
                return sta.added ? -1 : 1;
            }
            return 0;
        }
    };
    private long mBaseIdleTime;
    private long mBaseIoWaitTime;
    private long mBaseIrqTime;
    private long mBaseSoftIrqTime;
    private long mBaseSystemTime;
    private long mBaseUserTime;
    private int[] mCurPids;
    private int[] mCurThreadPids;
    private long mCurrentSampleRealTime;
    private long mCurrentSampleTime;
    private long mCurrentSampleWallTime;
    private boolean mFirst = true;
    private final boolean mIncludeThreads;
    private final long mJiffyMillis;
    private long mLastSampleRealTime;
    private long mLastSampleTime;
    private long mLastSampleWallTime;
    private long mLastTopSampleTime;
    private float mLoad1 = 0.0f;
    private float mLoad15 = 0.0f;
    private float mLoad5 = 0.0f;
    private final float[] mLoadAverageData = new float[3];
    private final ArrayList<Stats> mProcStats = new ArrayList<>();
    private final long[] mProcessFullStatsData = new long[6];
    private final String[] mProcessFullStatsStringData = new String[6];
    private final long[] mProcessStatsData = new long[4];
    private int mRelIdleTime;
    private int mRelIoWaitTime;
    private int mRelIrqTime;
    private int mRelSoftIrqTime;
    private boolean mRelStatsAreGood;
    private int mRelSystemTime;
    private int mRelUserTime;
    private String mSimpleTopProcessInfo = "";
    private final long[] mSinglePidStatsData = new long[4];
    private final long[] mSystemCpuData = new long[7];
    private ArrayMap<String, String> mTopThreeProcessesSnapShot = new ArrayMap<>();
    private final ArrayList<Stats> mWorkingProcs = new ArrayList<>();
    private boolean mWorkingProcsSorted;
    private String maxCpuProName = "null";
    private int maxCpuThousandths = -1;

    public interface FilterStats {
        boolean needed(Stats stats);
    }

    public static class Stats extends OppoBaseProcessCpuTracker.OppoBaseStats {
        public boolean active;
        public boolean added;
        public String baseName;
        public long base_majfaults;
        public long base_minfaults;
        public long base_stime;
        public long base_uptime;
        public long base_utime;
        public BatteryStatsImpl.Uid.Proc batteryStats;
        final String cmdlineFile;
        public int cpuThousandths;
        public boolean interesting;
        @UnsupportedAppUsage
        public String name;
        public int nameWidth;
        public final int pid;
        public int rel_majfaults;
        public int rel_minfaults;
        @UnsupportedAppUsage
        public int rel_stime;
        @UnsupportedAppUsage
        public long rel_uptime;
        @UnsupportedAppUsage
        public int rel_utime;
        public boolean removed;
        final String statFile;
        final ArrayList<Stats> threadStats;
        final String threadsDir;
        public final int uid;
        public long vsize;
        public boolean working;
        final ArrayList<Stats> workingThreads;

        Stats(int _pid, int parentPid, boolean includeThreads) {
            this.pid = _pid;
            if (parentPid < 0) {
                File procDir = new File("/proc", Integer.toString(this.pid));
                this.uid = getUid(procDir.toString());
                this.statFile = new File(procDir, "stat").toString();
                this.cmdlineFile = new File(procDir, "cmdline").toString();
                this.threadsDir = new File(procDir, "task").toString();
                if (includeThreads) {
                    this.threadStats = new ArrayList<>();
                    this.workingThreads = new ArrayList<>();
                    return;
                }
                this.threadStats = null;
                this.workingThreads = null;
                return;
            }
            File taskDir = new File(new File(new File("/proc", Integer.toString(parentPid)), "task"), Integer.toString(this.pid));
            this.uid = getUid(taskDir.toString());
            this.statFile = new File(taskDir, "stat").toString();
            this.cmdlineFile = null;
            this.threadsDir = null;
            this.threadStats = null;
            this.workingThreads = null;
        }

        private static int getUid(String path) {
            try {
                return Os.stat(path).st_uid;
            } catch (ErrnoException e) {
                Slog.w(ProcessCpuTracker.TAG, "Failed to stat(" + path + "): " + e);
                return -1;
            }
        }
    }

    @UnsupportedAppUsage
    public ProcessCpuTracker(boolean includeThreads) {
        this.mIncludeThreads = includeThreads;
        this.mJiffyMillis = 1000 / Os.sysconf(OsConstants._SC_CLK_TCK);
    }

    public void onLoadChanged(float load1, float load5, float load15) {
    }

    public int onMeasureProcessName(String name) {
        return 0;
    }

    public int getLoad1() {
        return (int) (this.mLoad1 * 10.0f);
    }

    public int getLoad5() {
        return (int) (this.mLoad5 * 10.0f);
    }

    public int getLoad15() {
        return (int) (this.mLoad15 * 10.0f);
    }

    public ArrayMap<String, String> getSimpleTopProcessesSnapShot() {
        long topInterval = ((long) OppoThermalManager.mHeatTopProInterval) * 1000;
        boolean topEnabled = OppoThermalManager.mHeatTopProFeatureOn;
        if (topInterval < 60000) {
            topInterval = 180000;
        }
        if (topEnabled && SystemClock.elapsedRealtime() - this.mLastTopSampleTime >= topInterval) {
            this.mWorkingProcsSorted = false;
            this.mLastTopSampleTime = SystemClock.elapsedRealtime();
            buildWorkingProcs();
        }
        return this.mTopThreeProcessesSnapShot;
    }

    private void collectSimpleTopThreeProcessesInfo(ArrayList<Stats> workingProcs) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (!(this.mTopThreeProcessesSnapShot == null || workingProcs == null || workingProcs.size() < OppoThermalManager.mHeatTopProCounts)) {
            this.mTopThreeProcessesSnapShot.clear();
            int len = OppoThermalManager.mHeatTopProCounts;
            for (int i = 0; i < len; i++) {
                Stats st = workingProcs.get(i);
                this.mTopThreeProcessesSnapShot.put(st.name, calcuteRatio(st));
            }
            this.mTopThreeProcessesSnapShot.put("lastSamepleWallTime", sdf.format(new Date(this.mLastSampleWallTime)));
        }
    }

    private String getRatioString(long numerator, long denominator) {
        long thousands = (1000 * numerator) / denominator;
        long hundreds = thousands / 10;
        String ratio = Long.toString(hundreds);
        if (hundreds >= 10) {
            return ratio;
        }
        long remainder = thousands - (10 * hundreds);
        if (remainder == 0) {
            return ratio;
        }
        return ratio + "." + Long.toString(remainder);
    }

    private String calcuteRatio(Stats st) {
        int totalTime = (int) st.rel_uptime;
        if (totalTime == 0) {
            totalTime = 1;
        }
        return getRatioString((long) (st.rel_utime + st.rel_stime), (long) totalTime);
    }

    public void init() {
        this.mFirst = true;
        update();
    }

    /* JADX INFO: finally extract failed */
    /* JADX INFO: Multiple debug info for r12v2 long: [D('idletime' long), D('nowWallTime' long)] */
    /* JADX INFO: Multiple debug info for r10v2 long: [D('nowRealtime' long), D('iowaittime' long)] */
    /* JADX INFO: Multiple debug info for r8v2 long: [D('irqtime' long), D('nowUptime' long)] */
    @UnsupportedAppUsage
    public void update() {
        long nowUptime;
        long nowRealtime;
        long nowWallTime;
        char c;
        long nowUptime2 = SystemClock.uptimeMillis();
        long nowRealtime2 = SystemClock.elapsedRealtime();
        long nowWallTime2 = System.currentTimeMillis();
        long[] sysCpu = this.mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long j = sysCpu[0] + sysCpu[1];
            long j2 = this.mJiffyMillis;
            long usertime = j * j2;
            long systemtime = sysCpu[2] * j2;
            nowWallTime = nowWallTime2;
            long nowWallTime3 = sysCpu[3] * j2;
            nowRealtime = nowRealtime2;
            long iowaittime = sysCpu[4] * j2;
            nowUptime = nowUptime2;
            long nowUptime3 = sysCpu[5] * j2;
            long softirqtime = j2 * sysCpu[6];
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (iowaittime - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (nowUptime3 - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (nowWallTime3 - this.mBaseIdleTime);
            c = 1;
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = iowaittime;
            this.mBaseIrqTime = nowUptime3;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = nowWallTime3;
        } else {
            nowUptime = nowUptime2;
            nowRealtime = nowRealtime2;
            nowWallTime = nowWallTime2;
            c = 1;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
        this.mLastSampleWallTime = this.mCurrentSampleWallTime;
        this.mCurrentSampleWallTime = nowWallTime;
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            this.mCurPids = collectStats("/proc", -1, this.mFirst, this.mCurPids, this.mProcStats);
            StrictMode.setThreadPolicy(savedPolicy);
            float[] loadAverages = this.mLoadAverageData;
            if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, null, null, loadAverages)) {
                float load1 = loadAverages[0];
                float load5 = loadAverages[c];
                float load15 = loadAverages[2];
                if (!(load1 == this.mLoad1 && load5 == this.mLoad5 && load15 == this.mLoad15)) {
                    this.mLoad1 = load1;
                    this.mLoad5 = load5;
                    this.mLoad15 = load15;
                    onLoadChanged(load1, load5, load15);
                }
            }
            this.mWorkingProcsSorted = false;
            this.mFirst = false;
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    /* JADX INFO: Multiple debug info for r15v6 int: [D('tempMaxCpuThousandths' int), D('i' int)] */
    /* JADX INFO: Multiple debug info for r0v12 int: [D('NS' int), D('i' int)] */
    /* JADX INFO: Multiple debug info for r4v12 long[]: [D('uptime' long), D('procStats' long[])] */
    /* JADX INFO: Multiple debug info for r13v5 long: [D('NP' int), D('majfaults' long)] */
    private int[] collectStats(String statsFile, int parentPid, boolean first, int[] curPids, ArrayList<Stats> allProcs) {
        String tempMaxCpuProName;
        int NS;
        int[] pids;
        int i;
        int NP;
        int tempMaxCpuThousandths;
        int[] pids2;
        int i2;
        int pid;
        int tempMaxCpuThousandths2;
        String tempMaxCpuProName2;
        String tempMaxCpuProName3;
        int curStatsIndex;
        int i3;
        int NS2;
        boolean z;
        long minfaults;
        String tempMaxCpuProName4;
        long uptime;
        int i4;
        long majfaults;
        Stats st;
        long minfaults2;
        long utime;
        int NS3;
        int i5 = parentPid;
        ArrayList<Stats> arrayList = allProcs;
        int[] pids3 = Process.getPids(statsFile, curPids);
        boolean z2 = false;
        int NP2 = pids3 == null ? 0 : pids3.length;
        try {
            int curStatsIndex2 = 0;
            int NS4 = allProcs.size();
            int tempMaxCpuThousandths3 = 0;
            int tempMaxCpuThousandths4 = -1;
            String tempMaxCpuProName5 = "null";
            while (true) {
                if (tempMaxCpuThousandths3 >= NP2) {
                    tempMaxCpuProName = tempMaxCpuProName5;
                    NS = NS4;
                    pids = pids3;
                    i = tempMaxCpuThousandths4;
                    break;
                }
                int pid2 = pids3[tempMaxCpuThousandths3];
                if (pid2 < 0) {
                    tempMaxCpuProName = tempMaxCpuProName5;
                    i = tempMaxCpuThousandths4;
                    NS = NS4;
                    pids = pids3;
                    break;
                }
                Stats st2 = curStatsIndex2 < NS4 ? arrayList.get(curStatsIndex2) : null;
                if (st2 == null || st2.pid != pid2) {
                    String tempMaxCpuProName6 = tempMaxCpuProName5;
                    pids2 = pids3;
                    NP = NP2;
                    tempMaxCpuThousandths = tempMaxCpuThousandths3;
                    int tempMaxCpuThousandths5 = tempMaxCpuThousandths4;
                    if (st2 != null) {
                        pid = pid2;
                        if (st2.pid > pid) {
                            arrayList = allProcs;
                        } else {
                            st2.rel_utime = 0;
                            st2.rel_stime = 0;
                            st2.rel_minfaults = 0;
                            st2.rel_majfaults = 0;
                            st2.removed = true;
                            st2.working = true;
                            arrayList = allProcs;
                            arrayList.remove(curStatsIndex2);
                            NS4--;
                            i2 = parentPid;
                            tempMaxCpuThousandths--;
                            tempMaxCpuThousandths4 = tempMaxCpuThousandths5;
                            tempMaxCpuProName5 = tempMaxCpuProName6;
                        }
                    } else {
                        arrayList = allProcs;
                        pid = pid2;
                    }
                    i2 = parentPid;
                    Stats st3 = new Stats(pid, i2, this.mIncludeThreads);
                    arrayList.add(curStatsIndex2, st3);
                    int curStatsIndex3 = curStatsIndex2 + 1;
                    int NS5 = NS4 + 1;
                    String[] procStatsString = this.mProcessFullStatsStringData;
                    long[] procStats = this.mProcessFullStatsData;
                    st3.base_uptime = SystemClock.uptimeMillis();
                    if (Process.readProcFile(st3.statFile.toString(), PROCESS_FULL_STATS_FORMAT, procStatsString, procStats, null)) {
                        st3.vsize = procStats[5];
                        st3.interesting = true;
                        st3.baseName = procStatsString[0];
                        st3.base_minfaults = procStats[1];
                        st3.base_majfaults = procStats[2];
                        long j = procStats[3];
                        long j2 = this.mJiffyMillis;
                        st3.base_utime = j * j2;
                        st3.base_stime = procStats[4] * j2;
                        int denom = this.mRelUserTime + this.mRelSystemTime + this.mRelIrqTime + this.mRelIdleTime;
                        if (denom <= 0) {
                            st3.cpuThousandths = 0;
                        } else {
                            st3.cpuThousandths = (int) (((((long) st3.rel_utime) + st3.base_stime) * 1000) / ((long) denom));
                        }
                        if (tempMaxCpuThousandths5 < st3.cpuThousandths) {
                            tempMaxCpuThousandths2 = st3.cpuThousandths;
                            tempMaxCpuProName2 = st3.name;
                        } else {
                            tempMaxCpuThousandths2 = tempMaxCpuThousandths5;
                            tempMaxCpuProName2 = tempMaxCpuProName6;
                        }
                        tempMaxCpuProName6 = tempMaxCpuProName2;
                        tempMaxCpuThousandths5 = tempMaxCpuThousandths2;
                    } else {
                        st3.baseName = MediaStore.UNKNOWN_STRING;
                        st3.base_stime = 0;
                        st3.base_utime = 0;
                        st3.base_majfaults = 0;
                        st3.base_minfaults = 0;
                    }
                    if (i2 < 0) {
                        getName(st3, st3.cmdlineFile);
                        if (st3.threadStats != null) {
                            this.mCurThreadPids = collectStats(st3.threadsDir, pid, true, this.mCurThreadPids, st3.threadStats);
                        }
                    } else if (st3.interesting) {
                        st3.name = st3.baseName;
                        st3.nameWidth = onMeasureProcessName(st3.name);
                    }
                    st3.rel_utime = 0;
                    st3.rel_stime = 0;
                    st3.rel_minfaults = 0;
                    st3.rel_majfaults = 0;
                    st3.added = true;
                    if (!first && st3.interesting) {
                        st3.working = true;
                    }
                    NS4 = NS5;
                    curStatsIndex2 = curStatsIndex3;
                    tempMaxCpuThousandths4 = tempMaxCpuThousandths5;
                    tempMaxCpuProName5 = tempMaxCpuProName6;
                } else {
                    st2.added = z2;
                    st2.working = z2;
                    int curStatsIndex4 = curStatsIndex2 + 1;
                    if (st2.interesting) {
                        long uptime2 = SystemClock.uptimeMillis();
                        long[] procStats2 = this.mProcessStatsData;
                        if (!Process.readProcFile(st2.statFile.toString(), PROCESS_STATS_FORMAT, null, procStats2, null)) {
                            tempMaxCpuProName3 = tempMaxCpuProName5;
                            NS2 = NS4;
                            pids2 = pids3;
                            NP = NP2;
                            curStatsIndex = curStatsIndex4;
                            tempMaxCpuThousandths = tempMaxCpuThousandths3;
                            i3 = tempMaxCpuThousandths4;
                        } else {
                            long minfaults3 = procStats2[0];
                            NP = NP2;
                            curStatsIndex = curStatsIndex4;
                            long majfaults2 = procStats2[1];
                            long j3 = procStats2[2];
                            long j4 = this.mJiffyMillis;
                            long utime2 = j3 * j4;
                            long stime = j4 * procStats2[3];
                            if (utime2 == st2.base_utime && stime == st2.base_stime) {
                                st2.rel_utime = 0;
                                st2.rel_stime = 0;
                                st2.rel_minfaults = 0;
                                st2.rel_majfaults = 0;
                                if (st2.active) {
                                    st2.active = false;
                                    pids2 = pids3;
                                    NS2 = NS4;
                                    tempMaxCpuProName3 = tempMaxCpuProName5;
                                    tempMaxCpuThousandths = tempMaxCpuThousandths3;
                                    i3 = tempMaxCpuThousandths4;
                                } else {
                                    pids2 = pids3;
                                    NS2 = NS4;
                                    tempMaxCpuProName3 = tempMaxCpuProName5;
                                    tempMaxCpuThousandths = tempMaxCpuThousandths3;
                                    i3 = tempMaxCpuThousandths4;
                                }
                            } else {
                                if (!st2.active) {
                                    z = true;
                                    st2.active = true;
                                } else {
                                    z = true;
                                }
                                if (i5 < 0) {
                                    getName(st2, st2.cmdlineFile);
                                    if (st2.threadStats != null) {
                                        utime = utime2;
                                        pids2 = pids3;
                                        st = st2;
                                        tempMaxCpuProName4 = tempMaxCpuProName5;
                                        majfaults = uptime2;
                                        uptime = majfaults2;
                                        minfaults = minfaults3;
                                        minfaults2 = stime;
                                        tempMaxCpuThousandths = tempMaxCpuThousandths3;
                                        NS3 = NS4;
                                        i4 = tempMaxCpuThousandths4;
                                        this.mCurThreadPids = collectStats(st2.threadsDir, pid2, false, this.mCurThreadPids, st2.threadStats);
                                    } else {
                                        utime = utime2;
                                        minfaults = minfaults3;
                                        pids2 = pids3;
                                        NS3 = NS4;
                                        tempMaxCpuProName4 = tempMaxCpuProName5;
                                        st = st2;
                                        minfaults2 = stime;
                                        tempMaxCpuThousandths = tempMaxCpuThousandths3;
                                        i4 = tempMaxCpuThousandths4;
                                        majfaults = uptime2;
                                        uptime = majfaults2;
                                    }
                                } else {
                                    utime = utime2;
                                    minfaults = minfaults3;
                                    pids2 = pids3;
                                    NS3 = NS4;
                                    tempMaxCpuProName4 = tempMaxCpuProName5;
                                    st = st2;
                                    minfaults2 = stime;
                                    tempMaxCpuThousandths = tempMaxCpuThousandths3;
                                    i4 = tempMaxCpuThousandths4;
                                    majfaults = uptime2;
                                    uptime = majfaults2;
                                }
                                st.rel_uptime = majfaults - st.base_uptime;
                                st.base_uptime = majfaults;
                                st.rel_utime = (int) (utime - st.base_utime);
                                st.rel_stime = (int) (minfaults2 - st.base_stime);
                                st.base_utime = utime;
                                st.base_stime = minfaults2;
                                st.rel_minfaults = (int) (minfaults - st.base_minfaults);
                                st.rel_majfaults = (int) (uptime - st.base_majfaults);
                                st.base_minfaults = minfaults;
                                st.base_majfaults = uptime;
                                st.working = true;
                                int denom2 = this.mRelUserTime + this.mRelSystemTime + this.mRelIrqTime + this.mRelIdleTime;
                                if (denom2 <= 0) {
                                    st.cpuThousandths = 0;
                                } else {
                                    st.cpuThousandths = ((st.rel_utime + st.rel_stime) * 1000) / denom2;
                                }
                                if (i4 < st.cpuThousandths) {
                                    i4 = st.cpuThousandths;
                                    tempMaxCpuProName4 = st.name;
                                }
                                i2 = parentPid;
                                arrayList = allProcs;
                                NS4 = NS3;
                                tempMaxCpuThousandths4 = i4;
                                curStatsIndex2 = curStatsIndex;
                                tempMaxCpuProName5 = tempMaxCpuProName4;
                            }
                        }
                    } else {
                        tempMaxCpuProName3 = tempMaxCpuProName5;
                        NS2 = NS4;
                        pids2 = pids3;
                        NP = NP2;
                        curStatsIndex = curStatsIndex4;
                        tempMaxCpuThousandths = tempMaxCpuThousandths3;
                        i3 = tempMaxCpuThousandths4;
                    }
                    i2 = parentPid;
                    arrayList = allProcs;
                    NS4 = NS2;
                    tempMaxCpuThousandths4 = i3;
                    curStatsIndex2 = curStatsIndex;
                    tempMaxCpuProName5 = tempMaxCpuProName3;
                }
                tempMaxCpuThousandths3 = tempMaxCpuThousandths + 1;
                i5 = i2;
                pids3 = pids2;
                NP2 = NP;
                z2 = false;
            }
            this.maxCpuThousandths = i;
            this.maxCpuProName = tempMaxCpuProName;
            while (curStatsIndex2 < NS) {
                Stats st4 = arrayList.get(curStatsIndex2);
                st4.rel_utime = 0;
                st4.rel_stime = 0;
                st4.rel_minfaults = 0;
                st4.rel_majfaults = 0;
                st4.removed = true;
                st4.working = true;
                arrayList.remove(curStatsIndex2);
                NS--;
            }
            return pids;
        } catch (NullPointerException e) {
            Slog.i(TAG, "collectStats(): allProcs is null!");
            return null;
        }
    }

    public long getCpuTimeForPid(int pid) {
        synchronized (this.mSinglePidStatsData) {
            String statFile = "/proc/" + pid + "/stat";
            long[] statsData = this.mSinglePidStatsData;
            if (!Process.readProcFile(statFile, PROCESS_STATS_FORMAT, null, statsData, null)) {
                return 0;
            }
            return this.mJiffyMillis * (statsData[2] + statsData[3]);
        }
    }

    public final int getLastUserTime() {
        return this.mRelUserTime;
    }

    public final int getLastSystemTime() {
        return this.mRelSystemTime;
    }

    public final int getLastIoWaitTime() {
        return this.mRelIoWaitTime;
    }

    public final int getLastIrqTime() {
        return this.mRelIrqTime;
    }

    public final int getLastSoftIrqTime() {
        return this.mRelSoftIrqTime;
    }

    public final int getLastIdleTime() {
        return this.mRelIdleTime;
    }

    public final int getMaxCpuThousandths() {
        return this.maxCpuThousandths;
    }

    public final String getMaxCpuProName() {
        return this.maxCpuProName;
    }

    public final boolean hasGoodLastStats() {
        return this.mRelStatsAreGood;
    }

    public final float getTotalCpuPercent() {
        int i = this.mRelUserTime;
        int i2 = this.mRelSystemTime;
        int i3 = this.mRelIrqTime;
        int denom = i + i2 + i3 + this.mRelIdleTime;
        if (denom <= 0) {
            return 0.0f;
        }
        return (((float) ((i + i2) + i3)) * 100.0f) / ((float) denom);
    }

    /* access modifiers changed from: package-private */
    public final void buildWorkingProcs() {
        if (!this.mWorkingProcsSorted) {
            this.mWorkingProcs.clear();
            int N = this.mProcStats.size();
            for (int i = 0; i < N; i++) {
                Stats stats = this.mProcStats.get(i);
                if (stats.working) {
                    this.mWorkingProcs.add(stats);
                    if (stats.threadStats != null && stats.threadStats.size() > 1) {
                        stats.workingThreads.clear();
                        int M = stats.threadStats.size();
                        for (int j = 0; j < M; j++) {
                            Stats tstats = stats.threadStats.get(j);
                            if (tstats.working) {
                                stats.workingThreads.add(tstats);
                            }
                        }
                        Collections.sort(stats.workingThreads, sLoadComparator);
                    }
                }
            }
            Collections.sort(this.mWorkingProcs, sLoadComparator);
            collectSimpleTopThreeProcessesInfo(this.mWorkingProcs);
            this.mWorkingProcsSorted = true;
        }
    }

    public final int countStats() {
        return this.mProcStats.size();
    }

    public final Stats getStats(int index) {
        return this.mProcStats.get(index);
    }

    public final List<Stats> getStats(FilterStats filter) {
        ArrayList<Stats> statses = new ArrayList<>(this.mProcStats.size());
        int N = this.mProcStats.size();
        for (int p = 0; p < N; p++) {
            Stats stats = this.mProcStats.get(p);
            if (filter.needed(stats)) {
                statses.add(stats);
            }
        }
        return statses;
    }

    @UnsupportedAppUsage
    public final int countWorkingStats() {
        buildWorkingProcs();
        return this.mWorkingProcs.size();
    }

    @UnsupportedAppUsage
    public final Stats getWorkingStats(int index) {
        return this.mWorkingProcs.get(index);
    }

    public final String printCurrentLoad() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter((Writer) sw, false, 128);
        pw.print("Load: ");
        pw.print(this.mLoad1);
        pw.print(" / ");
        pw.print(this.mLoad5);
        pw.print(" / ");
        pw.println(this.mLoad15);
        pw.flush();
        return sw.toString();
    }

    public final String printCurrentState(long now) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        buildWorkingProcs();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter((Writer) sw, false, 1024);
        pw.print("CPU usage from ");
        long j = this.mLastSampleTime;
        if (now > j) {
            pw.print(now - j);
            pw.print("ms to ");
            pw.print(now - this.mCurrentSampleTime);
            pw.print("ms ago");
        } else {
            pw.print(j - now);
            pw.print("ms to ");
            pw.print(this.mCurrentSampleTime - now);
            pw.print("ms later");
        }
        pw.print(" (");
        pw.print(sdf.format(new Date(this.mLastSampleWallTime)));
        pw.print(" to ");
        pw.print(sdf.format(new Date(this.mCurrentSampleWallTime)));
        pw.print(")");
        long sampleTime = this.mCurrentSampleTime - this.mLastSampleTime;
        long sampleRealTime = this.mCurrentSampleRealTime - this.mLastSampleRealTime;
        long j2 = 0;
        if (sampleRealTime > 0) {
            j2 = (sampleTime * 100) / sampleRealTime;
        }
        long percAwake = j2;
        if (percAwake != 100) {
            pw.print(" with ");
            pw.print(percAwake);
            pw.print("% awake");
        }
        pw.println(SettingsStringUtil.DELIMITER);
        int totalTime = this.mRelUserTime + this.mRelSystemTime + this.mRelIoWaitTime + this.mRelIrqTime + this.mRelSoftIrqTime + this.mRelIdleTime;
        int N = this.mWorkingProcs.size();
        int i = 0;
        while (i < N) {
            Stats st = this.mWorkingProcs.get(i);
            printProcessCPU(pw, st.added ? " +" : st.removed ? " -" : "  ", st.pid, st.name, (int) st.rel_uptime, st.rel_utime, st.rel_stime, 0, 0, 0, st.rel_minfaults, st.rel_majfaults);
            Stats st2 = st;
            if (!st2.removed && st2.workingThreads != null) {
                int M = st2.workingThreads.size();
                int j3 = 0;
                while (j3 < M) {
                    Stats tst = st2.workingThreads.get(j3);
                    printProcessCPU(pw, tst.added ? "   +" : tst.removed ? "   -" : "    ", tst.pid, tst.name, (int) st2.rel_uptime, tst.rel_utime, tst.rel_stime, 0, 0, 0, 0, 0);
                    j3++;
                    M = M;
                    st2 = st2;
                }
            }
            i++;
            N = N;
            pw = pw;
            percAwake = percAwake;
        }
        printProcessCPU(pw, "", -1, "TOTAL", totalTime, this.mRelUserTime, this.mRelSystemTime, this.mRelIoWaitTime, this.mRelIrqTime, this.mRelSoftIrqTime, 0, 0);
        pw.flush();
        return sw.toString();
    }

    private void printRatio(PrintWriter pw, long numerator, long denominator) {
        long thousands = (1000 * numerator) / denominator;
        long hundreds = thousands / 10;
        pw.print(hundreds);
        if (hundreds < 10) {
            long remainder = thousands - (10 * hundreds);
            if (remainder != 0) {
                pw.print('.');
                pw.print(remainder);
            }
        }
    }

    private void printProcessCPU(PrintWriter pw, String prefix, int pid, String label, int totalTime, int user, int system, int iowait, int irq, int softIrq, int minFaults, int majFaults) {
        String str;
        pw.print(prefix);
        int totalTime2 = totalTime == 0 ? 1 : totalTime;
        printRatio(pw, (long) (user + system + iowait + irq + softIrq), (long) totalTime2);
        pw.print("% ");
        if (pid >= 0) {
            pw.print(pid);
            pw.print("/");
        }
        pw.print(label);
        pw.print(": ");
        printRatio(pw, (long) user, (long) totalTime2);
        pw.print("% user + ");
        printRatio(pw, (long) system, (long) totalTime2);
        pw.print("% kernel");
        if (iowait > 0) {
            pw.print(" + ");
            str = " + ";
            printRatio(pw, (long) iowait, (long) totalTime2);
            pw.print("% iowait");
        } else {
            str = " + ";
        }
        if (irq > 0) {
            pw.print(str);
            printRatio(pw, (long) irq, (long) totalTime2);
            pw.print("% irq");
        }
        if (softIrq > 0) {
            pw.print(str);
            printRatio(pw, (long) softIrq, (long) totalTime2);
            pw.print("% softirq");
        }
        if (minFaults > 0 || majFaults > 0) {
            pw.print(" / faults:");
            if (minFaults > 0) {
                pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.print(minFaults);
                pw.print(" minor");
            }
            if (majFaults > 0) {
                pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.print(majFaults);
                pw.print(" major");
            }
        }
        pw.println();
    }

    private void getName(Stats st, String cmdlineFile) {
        int i;
        String newName = st.name;
        if (st.name == null || st.name.equals("app_process") || st.name.equals("<pre-initialized>") || st.name.equals("usap32") || st.name.equals("usap64")) {
            String cmdName = ProcStatsUtil.readTerminatedProcFile(cmdlineFile, (byte) 0);
            if (cmdName != null && cmdName.length() > 1 && (i = (newName = cmdName).lastIndexOf("/")) > 0 && i < newName.length() - 1) {
                newName = newName.substring(i + 1);
            }
            if (newName == null) {
                newName = st.baseName;
            }
        }
        if (st.name == null || !newName.equals(st.name)) {
            st.name = newName;
            st.nameWidth = onMeasureProcessName(st.name);
        }
    }
}
