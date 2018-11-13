package com.android.internal.os;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import libcore.io.Libcore;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final long[] mDeltaSpeedTimesMs;
    private final long mJiffyMillis = (1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK));
    private final long[] mLastSpeedTimesMs;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", new Object[]{Integer.valueOf(cpuNumber)});
        this.mLastSpeedTimesMs = new long[numSpeedSteps];
        this.mDeltaSpeedTimesMs = new long[numSpeedSteps];
    }

    public long[] readDelta() {
        Throwable th;
        ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        Throwable th2 = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mProcFile));
            try {
                SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
                int speedIndex = 0;
                while (speedIndex < this.mLastSpeedTimesMs.length) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    try {
                        splitter.setString(line);
                        splitter.next();
                        long time = Long.parseLong(splitter.next()) * this.mJiffyMillis;
                        if (time < this.mLastSpeedTimesMs[speedIndex]) {
                            this.mDeltaSpeedTimesMs[speedIndex] = time;
                        } else {
                            this.mDeltaSpeedTimesMs[speedIndex] = time - this.mLastSpeedTimesMs[speedIndex];
                        }
                        this.mLastSpeedTimesMs[speedIndex] = time;
                    } catch (NumberFormatException ex) {
                        Slog.e(TAG, "speedIndex = " + speedIndex + ", Failed to read cpu-freq: " + ex.getMessage());
                        this.mDeltaSpeedTimesMs[speedIndex] = 0;
                        this.mLastSpeedTimesMs[speedIndex] = 0;
                        speedIndex++;
                    }
                    speedIndex++;
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
                    } catch (Throwable th4) {
                        th = th4;
                        StrictMode.setThreadPolicy(policy);
                        throw th;
                    }
                }
                StrictMode.setThreadPolicy(policy);
                return this.mDeltaSpeedTimesMs;
            } catch (Throwable th5) {
                th = th5;
                reader = reader2;
            }
            try {
                Arrays.fill(this.mDeltaSpeedTimesMs, 0);
                StrictMode.setThreadPolicy(policy);
                return this.mDeltaSpeedTimesMs;
            } catch (Throwable th6) {
                th = th6;
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th8) {
                    if (th2 == null) {
                        th2 = th8;
                    } else if (th2 != th8) {
                        th2.addSuppressed(th8);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e2) {
                }
            } else {
                throw th;
            }
        }
    }
}
