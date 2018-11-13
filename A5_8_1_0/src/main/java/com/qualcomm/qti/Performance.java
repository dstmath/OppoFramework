package com.qualcomm.qti;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import com.qualcomm.qti.IPerfManager.Stub;

public class Performance {
    private static final boolean DEBUG;
    private static final String PERF_SERVICE_BINDER_NAME = "vendor.perfservice";
    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCEEDED = 0;
    private static final String TAG = "Perf";
    private static boolean sLoaded = DEBUG;
    private static IPerfManager sPerfService;
    private static IBinder sPerfServiceBinder;
    private static PerfServiceDeathRecipient sPerfServiceDeathRecipient;
    private static final boolean sPerfServiceDisabled = SystemProperties.getBoolean("persist.vendor.perfservice.disable", DEBUG);
    private int mHandle = 0;
    private boolean mIsSystemApp = true;
    private final Object mLock = new Object();

    private final class PerfServiceDeathRecipient implements DeathRecipient {
        /* synthetic */ PerfServiceDeathRecipient(Performance this$0, PerfServiceDeathRecipient -this1) {
            this();
        }

        private PerfServiceDeathRecipient() {
        }

        public void binderDied() {
            synchronized (Performance.this.mLock) {
                Log.e(Performance.TAG, "Perf Service died.");
                if (Performance.sPerfServiceBinder != null) {
                    Performance.sPerfServiceBinder.unlinkToDeath(this, 0);
                }
                Performance.sPerfServiceBinder = null;
                Performance.sPerfService = null;
            }
        }
    }

    private native int native_perf_hint(int i, String str, int i2, int i3);

    private native int native_perf_io_prefetch_start(int i, String str, String str2);

    private native int native_perf_io_prefetch_stop();

    private native int native_perf_lock_acq(int i, int i2, int[] iArr);

    private native int native_perf_lock_rel(int i);

    private native int native_perf_uxEngine_events(int i, int i2, String str, int i3);

    private native String native_perf_uxEngine_trigger(int i);

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

    public Performance(Context context) {
        if (DEBUG) {
            Trace.traceBegin(1, "Create Performance instance");
        }
        synchronized (Performance.class) {
            if (!sLoaded) {
                connectPerfServiceLocked();
                if (sPerfService != null || (sPerfServiceDisabled ^ 1) == 0) {
                    sLoaded = true;
                } else {
                    Log.e(TAG, "Perf service is unavailable.");
                }
            }
        }
        checkAppPlatformSigned(context);
        if (DEBUG) {
            Trace.traceEnd(1);
        }
    }

    private void connectPerfServiceLocked() {
        if (sPerfService == null && !sPerfServiceDisabled) {
            if (DEBUG) {
                Trace.traceBegin(1, "connectPerfServiceLocked");
            }
            Log.i(TAG, "Connecting to perf service.");
            sPerfServiceBinder = ServiceManager.getService(PERF_SERVICE_BINDER_NAME);
            if (sPerfServiceBinder == null) {
                Log.e(TAG, "Perf service is now down, set sPerfService as null.");
                if (DEBUG) {
                    Trace.traceEnd(1);
                }
                return;
            }
            try {
                sPerfServiceDeathRecipient = new PerfServiceDeathRecipient(this, null);
                sPerfServiceBinder.linkToDeath(sPerfServiceDeathRecipient, 0);
                if (sPerfServiceBinder != null) {
                    sPerfService = Stub.asInterface(sPerfServiceBinder);
                }
                if (DEBUG) {
                    Trace.traceEnd(1);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Perf service is now down, leave sPerfService as null.");
                if (DEBUG) {
                    Trace.traceEnd(1);
                }
            }
        }
    }

    public int perfLockAcquire(int duration, int... list) {
        if (this.mIsSystemApp) {
            this.mHandle = native_perf_lock_acq(this.mHandle, duration, list);
        } else {
            synchronized (this.mLock) {
                try {
                    if (sPerfService != null) {
                        this.mHandle = sPerfService.perfLockAcquire(duration, list);
                    } else {
                        return -1;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error calling perfLockAcquire", e);
                    return -1;
                }
            }
        }
        if (this.mHandle <= 0) {
            return -1;
        }
        return this.mHandle;
    }

    public int perfLockRelease() {
        int retValue;
        if (this.mIsSystemApp) {
            retValue = native_perf_lock_rel(this.mHandle);
            this.mHandle = 0;
            return retValue;
        }
        synchronized (this.mLock) {
            try {
                if (sPerfService != null) {
                    retValue = sPerfService.perfLockRelease();
                } else {
                    retValue = -1;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error calling perfLockRelease", e);
                return -1;
            }
        }
        return retValue;
    }

    public int perfLockReleaseHandler(int _handle) {
        if (this.mIsSystemApp) {
            return native_perf_lock_rel(_handle);
        }
        int retValue;
        synchronized (this.mLock) {
            try {
                if (sPerfService != null) {
                    retValue = sPerfService.perfLockReleaseHandler(_handle);
                } else {
                    retValue = -1;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error calling perfLockRelease(handle)", e);
                return -1;
            }
        }
        return retValue;
    }

    public int perfHint(int hint, String userDataStr, int userData1, int userData2) {
        if (this.mIsSystemApp) {
            this.mHandle = native_perf_hint(hint, userDataStr, userData1, userData2);
        } else {
            synchronized (this.mLock) {
                try {
                    if (sPerfService != null) {
                        this.mHandle = sPerfService.perfHint(hint, userDataStr, userData1, userData2);
                    } else {
                        return -1;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error calling perfHint", e);
                    return -1;
                }
            }
        }
        if (this.mHandle <= 0) {
            return -1;
        }
        return this.mHandle;
    }

    public int perfIOPrefetchStart(int PId, String Pkg_name, String Code_path) {
        return native_perf_io_prefetch_start(PId, Pkg_name, Code_path);
    }

    public int perfIOPrefetchStop() {
        return native_perf_io_prefetch_stop();
    }

    public int perfUXEngine_events(int opcode, int pid, String pkg_name, int lat) {
        return native_perf_uxEngine_events(opcode, pid, pkg_name, lat);
    }

    public String perfUXEngine_trigger(int opcode) {
        return native_perf_uxEngine_trigger(opcode);
    }

    private void checkAppPlatformSigned(Context context) {
        if (context != null) {
            if (DEBUG) {
                Trace.traceBegin(1, "checkAppPlatformSigned");
            }
            try {
                if ((context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).flags & 1) == 0) {
                    this.mIsSystemApp = DEBUG;
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "packageName is not found.");
                this.mIsSystemApp = true;
            }
            if (DEBUG) {
                Trace.traceEnd(1);
            }
        }
    }
}
