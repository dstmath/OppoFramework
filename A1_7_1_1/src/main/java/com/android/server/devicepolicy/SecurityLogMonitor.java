package com.android.server.devicepolicy;

import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.os.Process;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class SecurityLogMonitor implements Runnable {
    private static final int BUFFER_ENTRIES_MAXIMUM_LEVEL = 10240;
    private static final int BUFFER_ENTRIES_NOTIFICATION_LEVEL = 1024;
    private static final boolean DEBUG = false;
    private static final long POLLING_INTERVAL_MILLISECONDS = 0;
    private static final long RATE_LIMIT_INTERVAL_MILLISECONDS = 0;
    private static final String TAG = "SecurityLogMonitor";
    @GuardedBy("mLock")
    private boolean mAllowedToRetrieve;
    private final Lock mLock;
    @GuardedBy("mLock")
    private Thread mMonitorThread;
    @GuardedBy("mLock")
    private long mNextAllowedRetrivalTimeMillis;
    @GuardedBy("mLock")
    private ArrayList<SecurityEvent> mPendingLogs;
    private final DevicePolicyManagerService mService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.devicepolicy.SecurityLogMonitor.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.devicepolicy.SecurityLogMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.SecurityLogMonitor.<clinit>():void");
    }

    SecurityLogMonitor(DevicePolicyManagerService service) {
        this.mLock = new ReentrantLock();
        this.mMonitorThread = null;
        this.mPendingLogs = new ArrayList();
        this.mAllowedToRetrieve = false;
        this.mNextAllowedRetrivalTimeMillis = -1;
        this.mService = service;
    }

    void start() {
        this.mLock.lock();
        try {
            if (this.mMonitorThread == null) {
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = -1;
                this.mMonitorThread = new Thread(this);
                this.mMonitorThread.start();
            }
            this.mLock.unlock();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }

    void stop() {
        this.mLock.lock();
        try {
            if (this.mMonitorThread != null) {
                this.mMonitorThread.interrupt();
                this.mMonitorThread.join(TimeUnit.SECONDS.toMillis(5));
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = -1;
                this.mMonitorThread = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for thread to stop", e);
        } catch (Throwable th) {
            this.mLock.unlock();
        }
        this.mLock.unlock();
    }

    List<SecurityEvent> retrieveLogs() {
        this.mLock.lock();
        try {
            if (this.mAllowedToRetrieve) {
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = System.currentTimeMillis() + RATE_LIMIT_INTERVAL_MILLISECONDS;
                List<SecurityEvent> result = this.mPendingLogs;
                this.mPendingLogs = new ArrayList();
                return result;
            }
            this.mLock.unlock();
            return null;
        } finally {
            this.mLock.unlock();
        }
    }

    public void run() {
        Process.setThreadPriority(10);
        ArrayList<SecurityEvent> logs = new ArrayList();
        long lastLogTimestampNanos = -1;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(POLLING_INTERVAL_MILLISECONDS);
                if (lastLogTimestampNanos < 0) {
                    SecurityLog.readEvents(logs);
                } else {
                    SecurityLog.readEventsSince(1 + lastLogTimestampNanos, logs);
                }
                if (!logs.isEmpty()) {
                    this.mLock.lockInterruptibly();
                    this.mPendingLogs.addAll(logs);
                    if (this.mPendingLogs.size() > BUFFER_ENTRIES_MAXIMUM_LEVEL) {
                        this.mPendingLogs = new ArrayList(this.mPendingLogs.subList(this.mPendingLogs.size() - 5120, this.mPendingLogs.size()));
                    }
                    this.mLock.unlock();
                    lastLogTimestampNanos = ((SecurityEvent) logs.get(logs.size() - 1)).getTimeNanos();
                    logs.clear();
                }
                notifyDeviceOwnerIfNeeded();
            } catch (IOException e) {
                Log.e(TAG, "Failed to read security log", e);
            } catch (InterruptedException e2) {
                Log.i(TAG, "Thread interrupted, exiting.", e2);
                return;
            } catch (Throwable th) {
                this.mLock.unlock();
            }
        }
    }

    private void notifyDeviceOwnerIfNeeded() throws InterruptedException {
        boolean shouldNotifyDO = false;
        boolean allowToRetrieveNow = false;
        this.mLock.lockInterruptibly();
        try {
            int logSize = this.mPendingLogs.size();
            if (logSize >= 1024) {
                allowToRetrieveNow = true;
            } else if (logSize > 0) {
                if (this.mNextAllowedRetrivalTimeMillis == -1 || System.currentTimeMillis() >= this.mNextAllowedRetrivalTimeMillis) {
                    allowToRetrieveNow = true;
                }
            }
            shouldNotifyDO = !this.mAllowedToRetrieve ? allowToRetrieveNow : false;
            this.mAllowedToRetrieve = allowToRetrieveNow;
            if (shouldNotifyDO) {
                this.mService.sendDeviceOwnerCommand("android.app.action.SECURITY_LOGS_AVAILABLE", null);
            }
        } finally {
            this.mLock.unlock();
        }
    }
}
