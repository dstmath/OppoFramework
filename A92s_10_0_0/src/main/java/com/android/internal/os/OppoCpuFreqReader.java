package com.android.internal.os;

import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

class OppoCpuFreqReader {
    private static final String TAG = "OppoCpuFreqReader";
    private boolean DEBUG = false;
    private long[] cpuCurrentFreq;
    private long[] cpuOnLineStatus;
    private long[] cpufreqMax;
    private int cpusPresent;
    private String[] curPath;
    private boolean initialized = false;
    private String[] isolatePath;
    private long[] isolateStatus;
    private String[] maxPath;
    private String[] onLinePath;

    OppoCpuFreqReader() {
    }

    private void init() {
        try {
            FileReader fin = new FileReader("/sys/devices/system/cpu/present");
            try {
                Scanner scanner = new Scanner(new BufferedReader(fin)).useDelimiter("[-\n]");
                scanner.nextInt();
                this.cpusPresent = scanner.nextInt() + 1;
                scanner.close();
            } catch (Exception e) {
                Slog.e(TAG, "Cannot do CPU stats due to /sys/devices/system/cpu/present parsing problem");
            } finally {
                fin.close();
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "Cannot do CPU stats since /sys/devices/system/cpu/present is missing");
        } catch (IOException e3) {
            Slog.e(TAG, "Error closing file");
        }
        int i = this.cpusPresent;
        this.cpufreqMax = new long[i];
        this.cpuCurrentFreq = new long[i];
        this.cpuOnLineStatus = new long[i];
        this.isolateStatus = new long[i];
        this.maxPath = new String[i];
        this.curPath = new String[i];
        this.onLinePath = new String[i];
        this.isolatePath = new String[i];
        for (int i2 = 0; i2 < this.cpusPresent; i2++) {
            this.cpufreqMax[i2] = 0;
            this.cpuCurrentFreq[i2] = 0;
            this.cpuOnLineStatus[i2] = 0;
            this.isolateStatus[i2] = 0;
            String[] strArr = this.maxPath;
            strArr[i2] = "/sys/devices/system/cpu/cpu" + i2 + "/cpufreq/cpuinfo_max_freq";
            String[] strArr2 = this.curPath;
            strArr2[i2] = "/sys/devices/system/cpu/cpu" + i2 + "/cpufreq/scaling_cur_freq";
            String[] strArr3 = this.onLinePath;
            strArr3[i2] = "/sys/devices/system/cpu/cpu" + i2 + "/online";
            String[] strArr4 = this.isolatePath;
            strArr4[i2] = "/sys/devices/system/cpu/cpu" + i2 + "/isolate";
        }
        this.initialized = true;
    }

    private void readCpuFreqValues() {
        if (!this.initialized) {
            init();
        }
        for (int i = 0; i < this.cpusPresent; i++) {
            long cpufreqMaxTemp = readFreqFromFile(this.maxPath[i]);
            this.cpufreqMax[i] = cpufreqMaxTemp;
            long cpufreqCurTemp = readFreqFromFile(this.curPath[i]);
            this.cpuCurrentFreq[i] = cpufreqCurTemp;
            long cpuOnLineTemp = readFreqFromFile(this.onLinePath[i]);
            this.cpuOnLineStatus[i] = cpuOnLineTemp;
            long isolateTemp = readFreqFromFile(this.isolatePath[i]);
            this.isolateStatus[i] = isolateTemp;
            if (this.DEBUG) {
                Slog.d(TAG, "cpufreqMax:" + cpufreqMaxTemp);
                Slog.d(TAG, "cpufreqCur:" + cpufreqCurTemp);
                Slog.d(TAG, "cpuOnLine:" + cpuOnLineTemp);
                Slog.d(TAG, "isolateTemp:" + isolateTemp);
            }
        }
    }

    private long readFreqFromFile(String fileName) {
        long number = 0;
        try {
            FileReader fin = new FileReader(fileName);
            try {
                Scanner scannerC = new Scanner(new BufferedReader(fin));
                number = scannerC.nextLong();
                scannerC.close();
            } catch (Exception e) {
            } finally {
                fin.close();
            }
        } catch (FileNotFoundException e2) {
        } catch (IOException e3) {
            Slog.e(TAG, "Error closing file");
        }
        return number;
    }

    public long[] getCpuMaxFreq() {
        readCpuFreqValues();
        return this.cpufreqMax;
    }

    public long[] getCpuCurrentFreq() {
        readCpuFreqValues();
        return this.cpuCurrentFreq;
    }

    public long[] getCpuOnLineStatus() {
        readCpuFreqValues();
        return this.cpuOnLineStatus;
    }

    public long[] getIsolateStatus() {
        readCpuFreqValues();
        return this.isolateStatus;
    }

    public String getSimpleCpuFreqInfor() {
        readCpuFreqValues();
        StringBuilder sbd = new StringBuilder();
        if (!(this.cpuOnLineStatus == null || this.cpufreqMax == null || this.cpuCurrentFreq == null)) {
            sbd.append("[");
            int len = this.cpuOnLineStatus.length;
            for (int i = 0; i < len; i++) {
                sbd.append("" + this.cpuOnLineStatus[i] + "/" + this.isolateStatus[i] + "/" + this.cpufreqMax[i] + "/" + this.cpuCurrentFreq[i]);
                if (i != len - 1) {
                    sbd.append(", ");
                }
            }
            sbd.append("]");
        }
        return sbd.toString();
    }
}
