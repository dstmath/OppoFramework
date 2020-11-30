package com.android.server.wm;

import android.app.servertransaction.ClientTransaction;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.am.ActivityManagerService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientLifecycleMonitor {
    private static final boolean DEBUG = false;
    private static boolean ENABLE = false;
    private static final String TAG = ClientLifecycleMonitor.class.getSimpleName();
    private static final int TIMEOUT = 3000;
    private static final String TRACE_PATH = "/data/anr";
    private static int mNextSeq;
    private static SimpleDateFormat sAnrFileDateFormat;
    private ArraySet<Integer> mDeadPids;
    private final CheckThread mThread;
    private SparseArray<WaitingRecord> mWaitingMap;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.sys.engineering.pre", false) || !SystemProperties.getBoolean("ro.build.release_type", false)) {
            z = true;
        }
        ENABLE = z;
    }

    private ClientLifecycleMonitor() {
        this.mWaitingMap = new SparseArray<>();
        this.mDeadPids = new ArraySet<>();
        if (ENABLE) {
            this.mThread = new CheckThread("ClientLifecycleMonitor");
            this.mThread.start();
            return;
        }
        this.mThread = null;
    }

    /* access modifiers changed from: private */
    public static class LazyHolder {
        private static final ClientLifecycleMonitor INSTANCE = new ClientLifecycleMonitor();

        private LazyHolder() {
        }
    }

    public static ClientLifecycleMonitor getInstance() {
        return LazyHolder.INSTANCE;
    }

    private synchronized int nextSeq() {
        int i;
        i = mNextSeq + 1;
        mNextSeq = i;
        return i;
    }

    /* access modifiers changed from: package-private */
    public void transactionStart(ClientTransaction transaction) {
        if (ENABLE) {
            int seq = nextSeq();
            transaction.seq = seq;
            synchronized (this) {
                this.mWaitingMap.put(seq, new WaitingRecord(transaction.getActivityToken(), seq, SystemClock.uptimeMillis()));
            }
            scheduleTimeout();
        }
    }

    public void transactionEnd(IBinder activityToken, int seq) {
        transactionEnd(activityToken, seq, false);
    }

    /* access modifiers changed from: package-private */
    public void transactionEnd(IBinder activityToken, int seq, boolean fromTimeout) {
        if (ENABLE) {
            boolean ignore = true;
            int pid = -1;
            synchronized (this) {
                this.mWaitingMap.remove(seq);
                ActivityRecord record = ActivityRecord.forTokenLocked(activityToken);
                if (!(record == null || record.app == null)) {
                    pid = record.app.mPid;
                    if (!fromTimeout) {
                        this.mDeadPids.remove(Integer.valueOf(pid));
                    } else if (!this.mDeadPids.contains(Integer.valueOf(pid))) {
                        this.mDeadPids.add(Integer.valueOf(pid));
                        ignore = false;
                    }
                }
            }
            if (!ignore) {
                new Thread(new Runnable(pid) {
                    /* class com.android.server.wm.$$Lambda$ClientLifecycleMonitor$2ESDU2FQPJseEm_SSb_pI21Eers */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ClientLifecycleMonitor.this.lambda$transactionEnd$0$ClientLifecycleMonitor(this.f$1);
                    }
                }).start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: onTransactionTimeout */
    public void lambda$transactionEnd$0$ClientLifecycleMonitor(int pid) {
        try {
            ArrayList<Integer> pids = new ArrayList<>();
            pids.add(Integer.valueOf(pid));
            pids.add(Integer.valueOf(Process.myPid()));
            ActivityManagerService.dumpStackTraces(createDumpFile("/data/anr").getAbsolutePath(), pids, (ArrayList<Integer>) null, (ArrayList<Integer>) null);
        } catch (Exception e) {
        }
    }

    private static synchronized File createDumpFile(String tracesDir) {
        synchronized (ClientLifecycleMonitor.class) {
            if (sAnrFileDateFormat == null) {
                sAnrFileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
            }
            File dumpFile = new File(tracesDir, "clm_" + sAnrFileDateFormat.format(new Date()));
            try {
                if (dumpFile.createNewFile()) {
                    FileUtils.setPermissions(dumpFile.getAbsolutePath(), 384, -1, -1);
                    return dumpFile;
                }
            } catch (IOException e) {
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class WaitingRecord {
        IBinder mActivityToken;
        int seq;
        long startTime;

        WaitingRecord(IBinder token, int seq2, long startTime2) {
            this.mActivityToken = token;
            this.startTime = startTime2;
            this.seq = seq2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCheckLocked() {
        for (int index = 0; index < this.mWaitingMap.size(); index++) {
            WaitingRecord record = this.mWaitingMap.valueAt(index);
            if (SystemClock.uptimeMillis() >= record.startTime + BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS) {
                transactionEnd(record.mActivityToken, record.seq, true);
            } else {
                return;
            }
        }
    }

    private void scheduleTimeout() {
        CheckThread checkThread = this.mThread;
        if (checkThread != null) {
            synchronized (checkThread) {
                this.mThread.notify();
            }
        }
    }

    private void abort() {
        CheckThread checkThread = this.mThread;
        if (checkThread != null) {
            synchronized (checkThread) {
                this.mThread.mForceStop = true;
                this.mThread.notify();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getNextCheckDuration() {
        long duration = -1;
        synchronized (this) {
            if (this.mWaitingMap.size() > 0) {
                duration = (this.mWaitingMap.valueAt(0).startTime + BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS) - SystemClock.uptimeMillis();
            }
        }
        return duration;
    }

    /* access modifiers changed from: private */
    public class CheckThread extends Thread {
        private boolean mForceStop;

        CheckThread(String name) {
            super(name);
        }

        private void delayLock(long duration) {
            if (duration > 0) {
                long durationRemaining = duration;
                long bedtime = SystemClock.uptimeMillis() + duration;
                do {
                    try {
                        wait(durationRemaining);
                    } catch (InterruptedException e) {
                    }
                    if (!this.mForceStop) {
                        durationRemaining = bedtime - SystemClock.uptimeMillis();
                    } else {
                        return;
                    }
                } while (durationRemaining > 0);
            }
        }

        private void waitForeverLock() {
            boolean interrupted = false;
            do {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                if (this.mForceStop) {
                    return;
                }
            } while (interrupted);
        }

        public void run() {
            while (!this.mForceStop) {
                long duration = ClientLifecycleMonitor.this.getNextCheckDuration();
                synchronized (this) {
                    if (duration > 0) {
                        try {
                            delayLock(duration);
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else if (duration < 0) {
                        waitForeverLock();
                    }
                }
                synchronized (ClientLifecycleMonitor.this) {
                    ClientLifecycleMonitor.this.doCheckLocked();
                }
            }
        }
    }
}
