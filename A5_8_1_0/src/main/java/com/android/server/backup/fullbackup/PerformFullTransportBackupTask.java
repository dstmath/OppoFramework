package com.android.server.backup.fullbackup;

import android.app.IBackupAgent;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import com.android.server.EventLogTags;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.FullBackupJob;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.backup.internal.Operation;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.backup.utils.BackupObserverUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.job.controllers.JobStatus;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PerformFullTransportBackupTask extends FullBackupTask implements BackupRestoreTask {
    private static final String TAG = "PFTBT";
    private RefactoredBackupManagerService backupManagerService;
    IBackupObserver mBackupObserver;
    SinglePackageBackupRunner mBackupRunner;
    private final int mBackupRunnerOpToken;
    private volatile boolean mCancelAll;
    private final Object mCancelLock = new Object();
    private final int mCurrentOpToken;
    PackageInfo mCurrentPackage;
    private volatile boolean mIsDoingBackup;
    FullBackupJob mJob;
    CountDownLatch mLatch;
    IBackupManagerMonitor mMonitor;
    ArrayList<PackageInfo> mPackages;
    private volatile IBackupTransport mTransport;
    boolean mUpdateSchedule;
    boolean mUserInitiated;

    class SinglePackageBackupPreflight implements BackupRestoreTask, FullBackupPreflight {
        private final int mCurrentOpToken;
        final CountDownLatch mLatch = new CountDownLatch(1);
        final long mQuota;
        final AtomicLong mResult = new AtomicLong(-1003);
        final IBackupTransport mTransport;

        SinglePackageBackupPreflight(IBackupTransport transport, long quota, int currentOpToken) {
            this.mTransport = transport;
            this.mQuota = quota;
            this.mCurrentOpToken = currentOpToken;
        }

        public int preflightFullBackup(PackageInfo pkg, IBackupAgent agent) {
            int result;
            try {
                PerformFullTransportBackupTask.this.backupManagerService.prepareOperationTimeout(this.mCurrentOpToken, RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this, 0);
                PerformFullTransportBackupTask.this.backupManagerService.addBackupTrace("preflighting");
                agent.doMeasureFullBackup(this.mQuota, this.mCurrentOpToken, PerformFullTransportBackupTask.this.backupManagerService.getBackupManagerBinder());
                this.mLatch.await(RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                long totalSize = this.mResult.get();
                if (totalSize < 0) {
                    return (int) totalSize;
                }
                result = this.mTransport.checkFullBackupSize(totalSize);
                if (result == -1005) {
                    agent.doQuotaExceeded(totalSize, this.mQuota);
                }
                return result;
            } catch (Exception e) {
                Slog.w(PerformFullTransportBackupTask.TAG, "Exception preflighting " + pkg.packageName + ": " + e.getMessage());
                result = -1003;
            }
        }

        public void execute() {
        }

        public void operationComplete(long result) {
            this.mResult.set(result);
            this.mLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        public void handleCancel(boolean cancelAll) {
            this.mResult.set(-1003);
            this.mLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        public long getExpectedSizeOrErrorCode() {
            try {
                this.mLatch.await(RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                return this.mResult.get();
            } catch (InterruptedException e) {
                return -1;
            }
        }
    }

    class SinglePackageBackupRunner implements Runnable, BackupRestoreTask {
        final CountDownLatch mBackupLatch = new CountDownLatch(1);
        private volatile int mBackupResult = -1003;
        private final int mCurrentOpToken;
        private FullBackupEngine mEngine;
        private final int mEphemeralToken;
        private volatile boolean mIsCancelled;
        final ParcelFileDescriptor mOutput;
        final SinglePackageBackupPreflight mPreflight;
        final CountDownLatch mPreflightLatch = new CountDownLatch(1);
        private volatile int mPreflightResult = -1003;
        private final long mQuota;
        final PackageInfo mTarget;

        SinglePackageBackupRunner(ParcelFileDescriptor output, PackageInfo target, IBackupTransport transport, long quota, int currentOpToken) throws IOException {
            this.mOutput = ParcelFileDescriptor.dup(output.getFileDescriptor());
            this.mTarget = target;
            this.mCurrentOpToken = currentOpToken;
            this.mEphemeralToken = PerformFullTransportBackupTask.this.backupManagerService.generateRandomIntegerToken();
            this.mPreflight = new SinglePackageBackupPreflight(transport, quota, this.mEphemeralToken);
            this.mQuota = quota;
            registerTask();
        }

        void registerTask() {
            synchronized (PerformFullTransportBackupTask.this.backupManagerService.getCurrentOpLock()) {
                PerformFullTransportBackupTask.this.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 0));
            }
        }

        void unregisterTask() {
            synchronized (PerformFullTransportBackupTask.this.backupManagerService.getCurrentOpLock()) {
                PerformFullTransportBackupTask.this.backupManagerService.getCurrentOperations().remove(this.mCurrentOpToken);
            }
        }

        public void run() {
            this.mEngine = new FullBackupEngine(PerformFullTransportBackupTask.this.backupManagerService, new FileOutputStream(this.mOutput.getFileDescriptor()), this.mPreflight, this.mTarget, false, this, this.mQuota, this.mCurrentOpToken);
            try {
                if (!this.mIsCancelled) {
                    this.mPreflightResult = this.mEngine.preflightCheck();
                }
                this.mPreflightLatch.countDown();
                if (this.mPreflightResult == 0 && !this.mIsCancelled) {
                    this.mBackupResult = this.mEngine.backupOnePackage();
                }
                unregisterTask();
                this.mBackupLatch.countDown();
                try {
                    this.mOutput.close();
                } catch (IOException e) {
                    Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                }
            } catch (Exception e2) {
                try {
                    Slog.e(PerformFullTransportBackupTask.TAG, "Exception during full package backup of " + this.mTarget.packageName);
                    try {
                        this.mOutput.close();
                    } catch (IOException e3) {
                        Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                    }
                } finally {
                    unregisterTask();
                    this.mBackupLatch.countDown();
                    try {
                        this.mOutput.close();
                    } catch (IOException e4) {
                        Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                    }
                }
            } catch (Throwable th) {
                this.mPreflightLatch.countDown();
            }
        }

        public void sendQuotaExceeded(long backupDataBytes, long quotaBytes) {
            this.mEngine.sendQuotaExceeded(backupDataBytes, quotaBytes);
        }

        long getPreflightResultBlocking() {
            try {
                this.mPreflightLatch.await(RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                if (this.mIsCancelled) {
                    return -2003;
                }
                if (this.mPreflightResult == 0) {
                    return this.mPreflight.getExpectedSizeOrErrorCode();
                }
                return (long) this.mPreflightResult;
            } catch (InterruptedException e) {
                return -1003;
            }
        }

        int getBackupResultBlocking() {
            try {
                this.mBackupLatch.await(RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                if (this.mIsCancelled) {
                    return -2003;
                }
                return this.mBackupResult;
            } catch (InterruptedException e) {
                return -1003;
            }
        }

        public void execute() {
        }

        public void operationComplete(long result) {
        }

        public void handleCancel(boolean cancelAll) {
            Slog.w(PerformFullTransportBackupTask.TAG, "Full backup cancel of " + this.mTarget.packageName);
            PerformFullTransportBackupTask.this.mMonitor = BackupManagerMonitorUtils.monitorEvent(PerformFullTransportBackupTask.this.mMonitor, 4, PerformFullTransportBackupTask.this.mCurrentPackage, 2, null);
            this.mIsCancelled = true;
            PerformFullTransportBackupTask.this.backupManagerService.handleCancel(this.mEphemeralToken, cancelAll);
            PerformFullTransportBackupTask.this.backupManagerService.tearDownAgentAndKill(this.mTarget.applicationInfo);
            this.mPreflightLatch.countDown();
            this.mBackupLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }
    }

    public PerformFullTransportBackupTask(RefactoredBackupManagerService backupManagerService, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, boolean userInitiated) {
        super(observer);
        this.backupManagerService = backupManagerService;
        this.mUpdateSchedule = updateSchedule;
        this.mLatch = latch;
        this.mJob = runningJob;
        this.mPackages = new ArrayList(whichPackages.length);
        this.mBackupObserver = backupObserver;
        this.mMonitor = monitor;
        this.mUserInitiated = userInitiated;
        this.mCurrentOpToken = backupManagerService.generateRandomIntegerToken();
        this.mBackupRunnerOpToken = backupManagerService.generateRandomIntegerToken();
        if (backupManagerService.isBackupOperationInProgress()) {
            Slog.d(TAG, "Skipping full backup. A backup is already in progress.");
            this.mCancelAll = true;
            return;
        }
        registerTask();
        for (String pkg : whichPackages) {
            try {
                PackageInfo info = backupManagerService.getPackageManager().getPackageInfo(pkg, 64);
                this.mCurrentPackage = info;
                if (!AppBackupUtils.appIsEligibleForBackup(info.applicationInfo)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 9, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else if (!AppBackupUtils.appGetsFullBackup(info)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 10, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else if (AppBackupUtils.appIsStopped(info.applicationInfo)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 11, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else {
                    this.mPackages.add(info);
                }
            } catch (NameNotFoundException e) {
                Slog.i(TAG, "Requested package " + pkg + " not found; ignoring");
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 12, this.mCurrentPackage, 3, null);
            }
        }
    }

    private void registerTask() {
        synchronized (this.backupManagerService.getCurrentOpLock()) {
            Slog.d(TAG, "backupmanager pftbt token=" + Integer.toHexString(this.mCurrentOpToken));
            this.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 2));
        }
    }

    public void unregisterTask() {
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }

    public void execute() {
    }

    public void handleCancel(boolean cancelAll) {
        synchronized (this.mCancelLock) {
            if (!cancelAll) {
                Slog.wtf(TAG, "Expected cancelAll to be true.");
            }
            if (this.mCancelAll) {
                Slog.d(TAG, "Ignoring duplicate cancel call.");
                return;
            }
            this.mCancelAll = true;
            if (this.mIsDoingBackup) {
                this.backupManagerService.handleCancel(this.mBackupRunnerOpToken, cancelAll);
                try {
                    this.mTransport.cancelFullBackup();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Error calling cancelFullBackup() on transport: " + e);
                }
            }
        }
        return;
    }

    public void operationComplete(long result) {
    }

    /* JADX WARNING: Missing block: B:90:0x030c, code:
            if (r13 != 0) goto L_0x03eb;
     */
    /* JADX WARNING: Missing block: B:91:0x030e, code:
            r34[0].close();
            r34[0] = null;
            new java.lang.Thread(r36.mBackupRunner, "package-backup-bridge").start();
            r0 = new java.io.FileInputStream(r21[0].getFileDescriptor());
            r0 = new java.io.FileOutputStream(r34[1].getFileDescriptor());
            r32 = 0;
            r30 = r36.mBackupRunner.getPreflightResultBlocking();
     */
    /* JADX WARNING: Missing block: B:92:0x0351, code:
            if (r30 >= 0) goto L_0x0554;
     */
    /* JADX WARNING: Missing block: B:93:0x0353, code:
            r36.mMonitor = com.android.server.backup.utils.BackupManagerMonitorUtils.monitorEvent(r36.mMonitor, 16, r36.mCurrentPackage, 3, com.android.server.backup.utils.BackupManagerMonitorUtils.putMonitoringExtra(null, "android.app.backup.extra.LOG_PREFLIGHT_ERROR", r30));
            r13 = (int) r30;
     */
    /* JADX WARNING: Missing block: B:94:0x0373, code:
            r17 = r36.mBackupRunner.getBackupResultBlocking();
            r6 = r36.mCancelLock;
     */
    /* JADX WARNING: Missing block: B:95:0x037f, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:98:?, code:
            r36.mIsDoingBackup = false;
     */
    /* JADX WARNING: Missing block: B:99:0x0389, code:
            if (r36.mCancelAll != false) goto L_0x0399;
     */
    /* JADX WARNING: Missing block: B:100:0x038b, code:
            if (r17 != 0) goto L_0x0688;
     */
    /* JADX WARNING: Missing block: B:101:0x038d, code:
            r22 = r36.mTransport.finishBackup();
     */
    /* JADX WARNING: Missing block: B:102:0x0395, code:
            if (r13 != 0) goto L_0x0399;
     */
    /* JADX WARNING: Missing block: B:103:0x0397, code:
            r13 = r22;
     */
    /* JADX WARNING: Missing block: B:105:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:106:0x039a, code:
            if (r13 != 0) goto L_0x03a0;
     */
    /* JADX WARNING: Missing block: B:107:0x039c, code:
            if (r17 == 0) goto L_0x03a0;
     */
    /* JADX WARNING: Missing block: B:108:0x039e, code:
            r13 = r17;
     */
    /* JADX WARNING: Missing block: B:109:0x03a0, code:
            if (r13 == 0) goto L_0x03c9;
     */
    /* JADX WARNING: Missing block: B:110:0x03a2, code:
            android.util.Slog.e(TAG, "Error " + r13 + " backing up " + r29);
     */
    /* JADX WARNING: Missing block: B:111:0x03c9, code:
            r14 = r36.mTransport.requestFullBackupTime();
            android.util.Slog.i(TAG, "Transport suggested backoff=" + r14);
     */
    /* JADX WARNING: Missing block: B:113:0x03ef, code:
            if (r36.mUpdateSchedule == false) goto L_0x03fe;
     */
    /* JADX WARNING: Missing block: B:114:0x03f1, code:
            r36.backupManagerService.enqueueFullBackup(r29, java.lang.System.currentTimeMillis());
     */
    /* JADX WARNING: Missing block: B:116:0x0400, code:
            if (r13 != com.android.server.job.JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS) goto L_0x0694;
     */
    /* JADX WARNING: Missing block: B:117:0x0402, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
            android.util.Slog.i(TAG, "Transport rejected backup of " + r29 + ", skipping");
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_AGENT_FAILURE, new java.lang.Object[]{r29, "transport rejected"});
     */
    /* JADX WARNING: Missing block: B:118:0x0441, code:
            cleanUpPipes(r34);
            cleanUpPipes(r21);
     */
    /* JADX WARNING: Missing block: B:119:0x0451, code:
            if (r8.applicationInfo == null) goto L_0x0499;
     */
    /* JADX WARNING: Missing block: B:120:0x0453, code:
            android.util.Slog.i(TAG, "Unbinding agent in " + r29);
            r36.backupManagerService.addBackupTrace("unbinding " + r29);
     */
    /* JADX WARNING: Missing block: B:122:?, code:
            r36.backupManagerService.getActivityManager().unbindBackupAgent(r8.applicationInfo);
     */
    /* JADX WARNING: Missing block: B:128:0x04a0, code:
            r20 = move-exception;
     */
    /* JADX WARNING: Missing block: B:129:0x04a1, code:
            r16 = com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
     */
    /* JADX WARNING: Missing block: B:131:?, code:
            android.util.Slog.w(TAG, "Exception trying full transport backup", r20);
            r36.mMonitor = com.android.server.backup.utils.BackupManagerMonitorUtils.monitorEvent(r36.mMonitor, 19, r36.mCurrentPackage, 3, com.android.server.backup.utils.BackupManagerMonitorUtils.putMonitoringExtra(null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", android.util.Log.getStackTraceString(r20)));
     */
    /* JADX WARNING: Missing block: B:133:0x04d1, code:
            if (r36.mCancelAll != false) goto L_0x04d3;
     */
    /* JADX WARNING: Missing block: B:134:0x04d3, code:
            r16 = -2003;
     */
    /* JADX WARNING: Missing block: B:135:0x04d5, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
            com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
    /* JADX WARNING: Missing block: B:136:0x050f, code:
            if (r36.mJob != null) goto L_0x0511;
     */
    /* JADX WARNING: Missing block: B:137:0x0511, code:
            r36.mJob.finishBackupPass();
     */
    /* JADX WARNING: Missing block: B:139:0x0520, code:
            monitor-enter(r36.backupManagerService.getQueueLock());
     */
    /* JADX WARNING: Missing block: B:141:?, code:
            r36.backupManagerService.setRunningFullBackupTask(null);
     */
    /* JADX WARNING: Missing block: B:143:0x052a, code:
            r36.mLatch.countDown();
     */
    /* JADX WARNING: Missing block: B:144:0x0535, code:
            if (r36.mUpdateSchedule != false) goto L_0x0537;
     */
    /* JADX WARNING: Missing block: B:145:0x0537, code:
            r36.backupManagerService.scheduleNextFullBackupJob(r14);
     */
    /* JADX WARNING: Missing block: B:146:0x053e, code:
            android.util.Slog.i(TAG, "Full data backup pass finished.");
            r36.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Missing block: B:149:?, code:
            r27 = r0.read(r18);
     */
    /* JADX WARNING: Missing block: B:150:0x055e, code:
            if (r27 <= 0) goto L_0x05a3;
     */
    /* JADX WARNING: Missing block: B:151:0x0560, code:
            r0.write(r18, 0, r27);
            r6 = r36.mCancelLock;
     */
    /* JADX WARNING: Missing block: B:152:0x056e, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:155:0x0573, code:
            if (r36.mCancelAll != false) goto L_0x057f;
     */
    /* JADX WARNING: Missing block: B:156:0x0575, code:
            r13 = r36.mTransport.sendBackupData(r27);
     */
    /* JADX WARNING: Missing block: B:158:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:159:0x0580, code:
            r32 = r32 + ((long) r27);
     */
    /* JADX WARNING: Missing block: B:160:0x0589, code:
            if (r36.mBackupObserver == null) goto L_0x05a3;
     */
    /* JADX WARNING: Missing block: B:162:0x058f, code:
            if (r30 <= 0) goto L_0x05a3;
     */
    /* JADX WARNING: Missing block: B:163:0x0591, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnUpdate(r36.mBackupObserver, r29, new android.app.backup.BackupProgress(r30, r32));
     */
    /* JADX WARNING: Missing block: B:164:0x05a3, code:
            if (r27 <= 0) goto L_0x05a7;
     */
    /* JADX WARNING: Missing block: B:165:0x05a5, code:
            if (r13 == 0) goto L_0x0556;
     */
    /* JADX WARNING: Missing block: B:167:0x05a9, code:
            if (r13 != -1005) goto L_0x0373;
     */
    /* JADX WARNING: Missing block: B:168:0x05ab, code:
            android.util.Slog.w(TAG, "Package hit quota limit in-flight " + r29 + ": " + r32 + " of " + r10);
            r36.mMonitor = com.android.server.backup.utils.BackupManagerMonitorUtils.monitorEvent(r36.mMonitor, 18, r36.mCurrentPackage, 1, null);
            r36.mBackupRunner.sendQuotaExceeded(r32, r10);
     */
    /* JADX WARNING: Missing block: B:171:0x0603, code:
            if (r36.mCancelAll != false) goto L_0x0605;
     */
    /* JADX WARNING: Missing block: B:172:0x0605, code:
            r16 = -2003;
     */
    /* JADX WARNING: Missing block: B:173:0x0607, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
            com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
    /* JADX WARNING: Missing block: B:174:0x0641, code:
            if (r36.mJob != null) goto L_0x0643;
     */
    /* JADX WARNING: Missing block: B:175:0x0643, code:
            r36.mJob.finishBackupPass();
     */
    /* JADX WARNING: Missing block: B:177:0x0652, code:
            monitor-enter(r36.backupManagerService.getQueueLock());
     */
    /* JADX WARNING: Missing block: B:179:?, code:
            r36.backupManagerService.setRunningFullBackupTask(null);
     */
    /* JADX WARNING: Missing block: B:181:0x065c, code:
            r36.mLatch.countDown();
     */
    /* JADX WARNING: Missing block: B:182:0x0667, code:
            if (r36.mUpdateSchedule != false) goto L_0x0669;
     */
    /* JADX WARNING: Missing block: B:183:0x0669, code:
            r36.backupManagerService.scheduleNextFullBackupJob(r14);
     */
    /* JADX WARNING: Missing block: B:184:0x0670, code:
            android.util.Slog.i(TAG, "Full data backup pass finished.");
            r36.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Missing block: B:191:?, code:
            r36.mTransport.cancelFullBackup();
     */
    /* JADX WARNING: Missing block: B:197:0x0696, code:
            if (r13 != -1005) goto L_0x06c8;
     */
    /* JADX WARNING: Missing block: B:198:0x0698, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, -1005);
            android.util.Slog.i(TAG, "Transport quota exceeded for package: " + r29);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_QUOTA_EXCEEDED, r29);
     */
    /* JADX WARNING: Missing block: B:200:0x06ca, code:
            if (r13 != -1003) goto L_0x0705;
     */
    /* JADX WARNING: Missing block: B:201:0x06cc, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, -1003);
            android.util.Slog.w(TAG, "Application failure for package: " + r29);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.BACKUP_AGENT_FAILURE, r29);
            r36.backupManagerService.tearDownAgentAndKill(r8.applicationInfo);
     */
    /* JADX WARNING: Missing block: B:203:0x0707, code:
            if (r13 != -2003) goto L_0x0751;
     */
    /* JADX WARNING: Missing block: B:204:0x0709, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, -2003);
            android.util.Slog.w(TAG, "Backup cancelled. package=" + r29 + ", cancelAll=" + r36.mCancelAll);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_CANCELLED, r29);
            r36.backupManagerService.tearDownAgentAndKill(r8.applicationInfo);
     */
    /* JADX WARNING: Missing block: B:205:0x0751, code:
            if (r13 == 0) goto L_0x080b;
     */
    /* JADX WARNING: Missing block: B:206:0x0753, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            android.util.Slog.w(TAG, "Transport failed; aborting backup: " + r13);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_TRANSPORT_FAILURE, new java.lang.Object[0]);
     */
    /* JADX WARNING: Missing block: B:207:0x0780, code:
            r16 = com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
     */
    /* JADX WARNING: Missing block: B:208:0x0786, code:
            if (r36.mCancelAll == false) goto L_0x078a;
     */
    /* JADX WARNING: Missing block: B:209:0x0788, code:
            r16 = -2003;
     */
    /* JADX WARNING: Missing block: B:210:0x078a, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
            com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
    /* JADX WARNING: Missing block: B:211:0x07c4, code:
            if (r36.mJob == null) goto L_0x07cd;
     */
    /* JADX WARNING: Missing block: B:212:0x07c6, code:
            r36.mJob.finishBackupPass();
     */
    /* JADX WARNING: Missing block: B:213:0x07cd, code:
            r6 = r36.backupManagerService.getQueueLock();
     */
    /* JADX WARNING: Missing block: B:214:0x07d5, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:216:?, code:
            r36.backupManagerService.setRunningFullBackupTask(null);
     */
    /* JADX WARNING: Missing block: B:217:0x07de, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:218:0x07df, code:
            r36.mLatch.countDown();
     */
    /* JADX WARNING: Missing block: B:219:0x07ea, code:
            if (r36.mUpdateSchedule == false) goto L_0x07f3;
     */
    /* JADX WARNING: Missing block: B:220:0x07ec, code:
            r36.backupManagerService.scheduleNextFullBackupJob(r14);
     */
    /* JADX WARNING: Missing block: B:221:0x07f3, code:
            android.util.Slog.i(TAG, "Full data backup pass finished.");
            r36.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Missing block: B:222:0x0807, code:
            return;
     */
    /* JADX WARNING: Missing block: B:227:?, code:
            com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r36.mBackupObserver, r29, 0);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_SUCCESS, r29);
            r36.backupManagerService.logBackupComplete(r29);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        int i;
        ParcelFileDescriptor[] enginePipes = null;
        ParcelFileDescriptor[] transportPipes = null;
        long backoff = 0;
        int backupRunStatus = 0;
        if (this.backupManagerService.isEnabled() && (this.backupManagerService.isProvisioned() ^ 1) == 0) {
            this.mTransport = this.backupManagerService.getTransportManager().getCurrentTransportBinder();
            if (this.mTransport == null) {
                Slog.w(TAG, "Transport not present; full data backup not performed");
                backupRunStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 15, this.mCurrentPackage, 1, null);
                if (this.mCancelAll) {
                    backupRunStatus = -2003;
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                cleanUpPipes(null);
                cleanUpPipes(null);
                unregisterTask();
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                    this.backupManagerService.setRunningFullBackupTask(null);
                }
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    this.backupManagerService.scheduleNextFullBackupJob(0);
                }
                Slog.i(TAG, "Full data backup pass finished.");
                this.backupManagerService.getWakelock().release();
                return;
            }
            int N = this.mPackages.size();
            byte[] buffer = new byte[8192];
            i = 0;
            while (i < N) {
                PackageInfo currentPackage = (PackageInfo) this.mPackages.get(i);
                String packageName = currentPackage.packageName;
                Slog.i(TAG, "Initiating full-data transport backup of " + packageName + " token: " + this.mCurrentOpToken);
                EventLog.writeEvent(EventLogTags.FULL_BACKUP_PACKAGE, packageName);
                transportPipes = ParcelFileDescriptor.createPipe();
                int flags = this.mUserInitiated ? 1 : 0;
                long quota = JobStatus.NO_LATEST_RUNTIME;
                synchronized (this.mCancelLock) {
                    if (!this.mCancelAll) {
                        int backupPackageStatus = this.mTransport.performFullBackup(currentPackage, transportPipes[0], flags);
                        if (backupPackageStatus == 0) {
                            quota = this.mTransport.getBackupQuota(currentPackage.packageName, true);
                            enginePipes = ParcelFileDescriptor.createPipe();
                            this.mBackupRunner = new SinglePackageBackupRunner(enginePipes[1], currentPackage, this.mTransport, quota, this.mBackupRunnerOpToken);
                            enginePipes[1].close();
                            enginePipes[1] = null;
                            this.mIsDoingBackup = true;
                        }
                    }
                }
            }
            if (this.mCancelAll) {
                backupRunStatus = -2003;
            }
            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
            cleanUpPipes(transportPipes);
            cleanUpPipes(enginePipes);
            unregisterTask();
            if (this.mJob != null) {
                this.mJob.finishBackupPass();
            }
            synchronized (this.backupManagerService.getQueueLock()) {
                this.backupManagerService.setRunningFullBackupTask(null);
            }
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
                this.backupManagerService.scheduleNextFullBackupJob(backoff);
            }
            Slog.i(TAG, "Full data backup pass finished.");
            this.backupManagerService.getWakelock().release();
            return;
        }
        int monitoringEvent;
        Slog.i(TAG, "full backup requested but enabled=" + this.backupManagerService.isEnabled() + " provisioned=" + this.backupManagerService.isProvisioned() + "; ignoring");
        if (this.backupManagerService.isProvisioned()) {
            monitoringEvent = 13;
        } else {
            monitoringEvent = 14;
        }
        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, monitoringEvent, null, 3, null);
        this.mUpdateSchedule = false;
        backupRunStatus = -2001;
        if (this.mCancelAll) {
            backupRunStatus = -2003;
        }
        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
        cleanUpPipes(null);
        cleanUpPipes(null);
        unregisterTask();
        if (this.mJob != null) {
            this.mJob.finishBackupPass();
        }
        synchronized (this.backupManagerService.getQueueLock()) {
            this.backupManagerService.setRunningFullBackupTask(null);
        }
        this.mLatch.countDown();
        if (this.mUpdateSchedule) {
            this.backupManagerService.scheduleNextFullBackupJob(0);
        }
        Slog.i(TAG, "Full data backup pass finished.");
        this.backupManagerService.getWakelock().release();
        return;
        i++;
    }

    void cleanUpPipes(ParcelFileDescriptor[] pipes) {
        if (pipes != null) {
            ParcelFileDescriptor fd;
            if (pipes[0] != null) {
                fd = pipes[0];
                pipes[0] = null;
                try {
                    fd.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Unable to close pipe!");
                }
            }
            if (pipes[1] != null) {
                fd = pipes[1];
                pipes[1] = null;
                try {
                    fd.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "Unable to close pipe!");
                }
            }
        }
    }
}
