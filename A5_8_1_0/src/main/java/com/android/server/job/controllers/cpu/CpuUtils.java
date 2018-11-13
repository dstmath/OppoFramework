package com.android.server.job.controllers.cpu;

import android.os.Process;
import android.os.SystemClock;
import android.system.OsConstants;
import android.util.Slog;
import com.android.server.display.OppoBrightUtils;
import libcore.io.Libcore;

public class CpuUtils {
    private static final boolean DEBUG = false;
    private static final int[] SYSTEM_CPU_FORMAT = new int[]{288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
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
    private final long mJiffyMillis = (1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK));
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

    public void update() {
        long nowUptime = SystemClock.uptimeMillis();
        long nowRealtime = SystemClock.elapsedRealtime();
        long[] sysCpu = this.mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long usertime = (sysCpu[0] + sysCpu[1]) * this.mJiffyMillis;
            long systemtime = sysCpu[2] * this.mJiffyMillis;
            long idletime = sysCpu[3] * this.mJiffyMillis;
            long iowaittime = sysCpu[4] * this.mJiffyMillis;
            long irqtime = sysCpu[5] * this.mJiffyMillis;
            long softirqtime = sysCpu[6] * this.mJiffyMillis;
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (iowaittime - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (irqtime - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (idletime - this.mBaseIdleTime);
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = iowaittime;
            this.mBaseIrqTime = irqtime;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = idletime;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
    }

    public final float getTotalCpuPercent() {
        int denom = ((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime) + this.mRelIdleTime;
        if (denom <= 0) {
            return OppoBrightUtils.MIN_LUX_LIMITI;
        }
        return (((float) ((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime)) * 100.0f) / ((float) denom);
    }

    public final float getSpecialPercent() {
        return (((float) (((this.mRelUserTime + this.mRelSystemTime) + this.mRelIrqTime) / ((int) this.mJiffyMillis))) * 100.0f) / ((float) (this.mCurrentSampleTime - this.mLastSampleTime));
    }
}
