package com.android.server.job.controllers.cpu;

import android.os.Process;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;

public class CpuUtils {
    private static final boolean DEBUG = false;
    private static final int[] SYSTEM_CPU_FORMAT = {288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final String TAG = "JobScheduler.Cpu";
    private static CpuUtils mCpuUtils;
    private long mBaseIdleTime;
    private long mBaseIoWaitTime;
    private long mBaseIrqTime;
    private long mBaseSoftIrqTime;
    private long mBaseSystemTime;
    private long mBaseUserTime;
    private long mCurrentSampleRealTime;
    private long mCurrentSampleTime;
    private final long mJiffyMillis = (1000 / Os.sysconf(OsConstants._SC_CLK_TCK));
    private long mLastSampleRealTime;
    private long mLastSampleTime;
    private int mRelIdleTime;
    private int mRelIoWaitTime;
    private int mRelIrqTime;
    private int mRelSoftIrqTime;
    private boolean mRelStatsAreGood;
    private int mRelSystemTime;
    private int mRelUserTime;
    private final long[] mSystemCpuData = new long[7];

    private CpuUtils() {
        Slog.d(TAG, "mJiffyMillis=" + this.mJiffyMillis);
    }

    public static synchronized CpuUtils getInstance() {
        CpuUtils cpuUtils;
        synchronized (CpuUtils.class) {
            if (mCpuUtils == null) {
                mCpuUtils = new CpuUtils();
            }
            cpuUtils = mCpuUtils;
        }
        return cpuUtils;
    }

    /* JADX INFO: Multiple debug info for r3v3 long: [D('iowaittime' long), D('nowRealtime' long)] */
    /* JADX INFO: Multiple debug info for r1v3 long: [D('nowUptime' long), D('irqtime' long)] */
    public void update() {
        long nowUptime;
        long nowRealtime;
        long nowUptime2 = SystemClock.uptimeMillis();
        long nowRealtime2 = SystemClock.elapsedRealtime();
        long[] sysCpu = this.mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long j = sysCpu[0] + sysCpu[1];
            long j2 = this.mJiffyMillis;
            long usertime = j * j2;
            long systemtime = sysCpu[2] * j2;
            long idletime = sysCpu[3] * j2;
            nowRealtime = nowRealtime2;
            long nowRealtime3 = sysCpu[4] * j2;
            nowUptime = nowUptime2;
            long irqtime = sysCpu[5] * j2;
            long softirqtime = j2 * sysCpu[6];
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (nowRealtime3 - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (irqtime - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (idletime - this.mBaseIdleTime);
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = nowRealtime3;
            this.mBaseIrqTime = irqtime;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = idletime;
        } else {
            nowUptime = nowUptime2;
            nowRealtime = nowRealtime2;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
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

    public final float getSpecialPercent() {
        return (((float) (((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime) / ((int) this.mJiffyMillis))) * 100.0f) / ((float) (this.mCurrentSampleTime - this.mLastSampleTime));
    }
}
