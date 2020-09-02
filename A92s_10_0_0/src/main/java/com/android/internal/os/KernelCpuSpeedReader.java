package com.android.internal.os;

import android.os.StrictMode;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final long[] mDeltaSpeedTimesMs;
    private final long mJiffyMillis = (1000 / Os.sysconf(OsConstants._SC_CLK_TCK));
    private final long[] mLastSpeedTimesMs;
    private final int mNumSpeedSteps;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", Integer.valueOf(cpuNumber));
        this.mNumSpeedSteps = numSpeedSteps;
        this.mLastSpeedTimesMs = new long[numSpeedSteps];
        this.mDeltaSpeedTimesMs = new long[numSpeedSteps];
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.util.Arrays.fill(long[], long):void}
     arg types: [long[], int]
     candidates:
      ClspMth{java.util.Arrays.fill(double[], double):void}
      ClspMth{java.util.Arrays.fill(byte[], byte):void}
      ClspMth{java.util.Arrays.fill(boolean[], boolean):void}
      ClspMth{java.util.Arrays.fill(char[], char):void}
      ClspMth{java.util.Arrays.fill(short[], short):void}
      ClspMth{java.util.Arrays.fill(java.lang.Object[], java.lang.Object):void}
      ClspMth{java.util.Arrays.fill(int[], int):void}
      ClspMth{java.util.Arrays.fill(float[], float):void}
      ClspMth{java.util.Arrays.fill(long[], long):void} */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008c, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008f, code lost:
        throw r5;
     */
    public long[] readDelta() {
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            int speedIndex = 0;
            while (speedIndex < this.mLastSpeedTimesMs.length) {
                String line = reader.readLine();
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
            $closeResource(null, reader);
        } catch (IOException e) {
            Arrays.fill(this.mDeltaSpeedTimesMs, 0L);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(policy);
            throw th;
        }
        StrictMode.setThreadPolicy(policy);
        return this.mDeltaSpeedTimesMs;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.util.Arrays.fill(long[], long):void}
     arg types: [long[], int]
     candidates:
      ClspMth{java.util.Arrays.fill(double[], double):void}
      ClspMth{java.util.Arrays.fill(byte[], byte):void}
      ClspMth{java.util.Arrays.fill(boolean[], boolean):void}
      ClspMth{java.util.Arrays.fill(char[], char):void}
      ClspMth{java.util.Arrays.fill(short[], short):void}
      ClspMth{java.util.Arrays.fill(java.lang.Object[], java.lang.Object):void}
      ClspMth{java.util.Arrays.fill(int[], int):void}
      ClspMth{java.util.Arrays.fill(float[], float):void}
      ClspMth{java.util.Arrays.fill(long[], long):void} */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        throw r4;
     */
    public long[] readAbsolute() {
        String line;
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        long[] speedTimeMs = new long[this.mNumSpeedSteps];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            for (int speedIndex = 0; speedIndex < this.mNumSpeedSteps && (line = reader.readLine()) != null; speedIndex++) {
                splitter.setString(line);
                splitter.next();
                speedTimeMs[speedIndex] = Long.parseLong(splitter.next()) * this.mJiffyMillis;
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
            Arrays.fill(speedTimeMs, 0L);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(policy);
            throw th;
        }
        StrictMode.setThreadPolicy(policy);
        return speedTimeMs;
    }
}
