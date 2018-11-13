package com.qualcomm.qti;

import android.os.SystemProperties;
import android.os.Trace;
import com.qualcomm.qti.IPerfManager.Stub;

public class PerfService extends Stub {
    private static final boolean DEBUG;
    private static final int REQUEST_FAILED = -1;
    private static final int REQUEST_SUCCEEDED = 0;
    private static final String TAG = "PerfService";
    private int mHandle = 0;

    private native int native_perf_hint(int i, String str, int i2, int i3);

    private native int native_perf_lock_acq(int i, int i2, int[] iArr);

    private native int native_perf_lock_rel(int i);

    static {
        boolean z = true;
        if (SystemProperties.getInt("debug.trace.perf", 0) != 1) {
            z = DEBUG;
        }
        DEBUG = z;
        try {
            System.loadLibrary("qti_performance");
        } catch (UnsatisfiedLinkError e) {
        }
    }

    public int perfLockAcquire(int duration, int[] list) {
        this.mHandle = native_perf_lock_acq(this.mHandle, duration, list);
        if (this.mHandle <= 0) {
            return -1;
        }
        return this.mHandle;
    }

    public int perfHint(int hint, String userDataStr, int userData1, int userData2) {
        if (DEBUG) {
            Trace.traceBegin(1, "[perfservice]perfHint:" + hint + " " + userDataStr + " " + userData1 + " " + userData2);
        }
        this.mHandle = native_perf_hint(hint, userDataStr, userData1, userData2);
        if (DEBUG) {
            Trace.traceEnd(1);
        }
        if (this.mHandle <= 0) {
            return -1;
        }
        return this.mHandle;
    }

    public int perfLockRelease() {
        if (DEBUG) {
            Trace.traceBegin(1, "[perfservice]perfLockRelease handle:" + this.mHandle);
        }
        int retValue = native_perf_lock_rel(this.mHandle);
        if (DEBUG) {
            Trace.traceEnd(1);
        }
        this.mHandle = 0;
        return retValue;
    }

    public int perfLockReleaseHandler(int _handle) {
        if (DEBUG) {
            Trace.traceBegin(1, "[perfservice]perfLockRelease _handle:" + _handle);
        }
        int retValue = native_perf_lock_rel(_handle);
        if (DEBUG) {
            Trace.traceEnd(1);
        }
        return retValue;
    }
}
