package com.android.server.location;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Date;

/* access modifiers changed from: package-private */
public class NtpTimeHelper {
    private static final boolean DEBUG = Log.isLoggable("NtpTimeHelper", 3);
    private static final long MAX_RETRY_INTERVAL = 14400000;
    @VisibleForTesting
    static final long NTP_INTERVAL = 86400000;
    @VisibleForTesting
    static final long RETRY_INTERVAL = 300000;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final int STATE_RETRIEVING_AND_INJECTING = 1;
    private static final String TAG = "NtpTimeHelper";
    private static final String WAKELOCK_KEY = "NtpTimeHelper";
    private static final long WAKELOCK_TIMEOUT_MILLIS = 60000;
    @GuardedBy({"this"})
    private final InjectNtpTimeCallback mCallback;
    private final ConnectivityManager mConnMgr;
    private final Handler mHandler;
    @GuardedBy({"this"})
    private int mInjectNtpTimeState;
    private final ExponentialBackOff mNtpBackOff;
    private final NtpTrustedTime mNtpTime;
    @GuardedBy({"this"})
    private boolean mOnDemandTimeInjection;
    private final PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: package-private */
    public interface InjectNtpTimeCallback {
        void injectTime(long j, long j2, int i);
    }

    /* renamed from: lambda$xWqlqJuq4jBJ5-xhFLCwEKGVB0k  reason: not valid java name */
    public static /* synthetic */ void m18lambda$xWqlqJuq4jBJ5xhFLCwEKGVB0k(NtpTimeHelper ntpTimeHelper) {
        ntpTimeHelper.blockingGetNtpTimeAndInject();
    }

    @VisibleForTesting
    NtpTimeHelper(Context context, Looper looper, InjectNtpTimeCallback callback, NtpTrustedTime ntpTime) {
        this.mNtpBackOff = new ExponentialBackOff(300000, 14400000);
        this.mInjectNtpTimeState = 0;
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mCallback = callback;
        this.mNtpTime = ntpTime;
        this.mHandler = new Handler(looper);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "NtpTimeHelper");
    }

    NtpTimeHelper(Context context, Looper looper, InjectNtpTimeCallback callback) {
        this(context, looper, callback, NtpTrustedTime.getInstance(context));
    }

    /* access modifiers changed from: package-private */
    public synchronized void enablePeriodicTimeInjection() {
        this.mOnDemandTimeInjection = true;
    }

    /* access modifiers changed from: package-private */
    public synchronized void onNetworkAvailable() {
        if (this.mInjectNtpTimeState == 0) {
            retrieveAndInjectNtpTime();
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* access modifiers changed from: package-private */
    public synchronized void retrieveAndInjectNtpTime() {
        if (this.mInjectNtpTimeState != 1) {
            if (!isNetworkConnected()) {
                this.mInjectNtpTimeState = 0;
                return;
            }
            this.mInjectNtpTimeState = 1;
            this.mWakeLock.acquire(60000);
            new Thread(new Runnable() {
                /* class com.android.server.location.$$Lambda$NtpTimeHelper$xWqlqJuq4jBJ5xhFLCwEKGVB0k */

                public final void run() {
                    NtpTimeHelper.m18lambda$xWqlqJuq4jBJ5xhFLCwEKGVB0k(NtpTimeHelper.this);
                }
            }).start();
        }
    }

    private void blockingGetNtpTimeAndInject() {
        boolean refreshSuccess;
        long delay;
        if (this.mNtpTime.getCacheAge() >= 86400000) {
            refreshSuccess = this.mNtpTime.forceRefresh();
        } else {
            refreshSuccess = true;
        }
        synchronized (this) {
            this.mInjectNtpTimeState = 2;
            if (this.mNtpTime.getCacheAge() < 86400000) {
                long time = this.mNtpTime.getCachedNtpTime();
                long timeReference = this.mNtpTime.getCachedNtpTimeReference();
                long certainty = this.mNtpTime.getCacheCertainty();
                if (DEBUG) {
                    long now = System.currentTimeMillis();
                    Log.d("NtpTimeHelper", "NTP server returned: " + time + " (" + new Date(time) + ") reference: " + timeReference + " certainty: " + certainty + " system time offset: " + (time - now));
                }
                this.mHandler.post(new Runnable(time, timeReference, certainty) {
                    /* class com.android.server.location.$$Lambda$NtpTimeHelper$xPxgficKWFyuwUj60WMuiGEEjdg */
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ long f$2;
                    private final /* synthetic */ long f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r4;
                        this.f$3 = r6;
                    }

                    public final void run() {
                        NtpTimeHelper.this.lambda$blockingGetNtpTimeAndInject$0$NtpTimeHelper(this.f$1, this.f$2, this.f$3);
                    }
                });
                this.mNtpBackOff.reset();
                delay = 86400000;
            } else {
                Log.e("NtpTimeHelper", "requestTime failed");
                delay = this.mNtpBackOff.nextBackoffMillis();
            }
            if (DEBUG) {
                Log.d("NtpTimeHelper", String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", Boolean.valueOf(this.mOnDemandTimeInjection), Boolean.valueOf(refreshSuccess), Long.valueOf(delay)));
            }
            if (this.mOnDemandTimeInjection || !refreshSuccess) {
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.android.server.location.$$Lambda$7zgzwOWgEFtr6DuyW9EYKot7bHU */

                    public final void run() {
                        NtpTimeHelper.this.retrieveAndInjectNtpTime();
                    }
                }, delay);
            }
        }
        try {
            this.mWakeLock.release();
        } catch (Exception e) {
        }
    }

    public /* synthetic */ void lambda$blockingGetNtpTimeAndInject$0$NtpTimeHelper(long time, long timeReference, long certainty) {
        this.mCallback.injectTime(time, timeReference, (int) certainty);
    }

    public void setNtpTimeStateIdle() {
        this.mInjectNtpTimeState = 2;
    }
}
