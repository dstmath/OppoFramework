package com.android.server.backup.fullbackup;

import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.KeyValueAdbBackupEngine;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.PasswordUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PerformAdbBackupTask extends FullBackupTask implements BackupRestoreTask {
    private RefactoredBackupManagerService backupManagerService;
    boolean mAllApps;
    FullBackupEngine mBackupEngine;
    boolean mCompress;
    private final int mCurrentOpToken;
    String mCurrentPassword;
    PackageInfo mCurrentTarget;
    DeflaterOutputStream mDeflater;
    boolean mDoWidgets;
    String mEncryptPassword;
    boolean mIncludeApks;
    boolean mIncludeObbs;
    boolean mIncludeShared;
    boolean mIncludeSystem;
    boolean mKeyValue;
    final AtomicBoolean mLatch;
    ParcelFileDescriptor mOutputFile;
    ArrayList<String> mPackages;

    public PerformAdbBackupTask(RefactoredBackupManagerService backupManagerService, ParcelFileDescriptor fd, IFullBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, String curPassword, String encryptPassword, boolean doAllApps, boolean doSystem, boolean doCompress, boolean doKeyValue, String[] packages, AtomicBoolean latch) {
        ArrayList arrayList;
        super(observer);
        this.backupManagerService = backupManagerService;
        this.mCurrentOpToken = backupManagerService.generateRandomIntegerToken();
        this.mLatch = latch;
        this.mOutputFile = fd;
        this.mIncludeApks = includeApks;
        this.mIncludeObbs = includeObbs;
        this.mIncludeShared = includeShared;
        this.mDoWidgets = doWidgets;
        this.mAllApps = doAllApps;
        this.mIncludeSystem = doSystem;
        if (packages == null) {
            arrayList = new ArrayList();
        } else {
            arrayList = new ArrayList(Arrays.asList(packages));
        }
        this.mPackages = arrayList;
        this.mCurrentPassword = curPassword;
        if (encryptPassword == null || "".equals(encryptPassword)) {
            this.mEncryptPassword = curPassword;
        } else {
            this.mEncryptPassword = encryptPassword;
        }
        this.mCompress = doCompress;
        this.mKeyValue = doKeyValue;
    }

    void addPackagesToSet(TreeMap<String, PackageInfo> set, List<String> pkgNames) {
        for (String pkgName : pkgNames) {
            if (!set.containsKey(pkgName)) {
                try {
                    set.put(pkgName, this.backupManagerService.getPackageManager().getPackageInfo(pkgName, 64));
                } catch (NameNotFoundException e) {
                    Slog.w(RefactoredBackupManagerService.TAG, "Unknown package " + pkgName + ", skipping");
                }
            }
        }
    }

    private OutputStream emitAesBackupHeader(StringBuilder headerbuf, OutputStream ofstream) throws Exception {
        byte[] newUserSalt = this.backupManagerService.randomBytes(512);
        SecretKey userKey = PasswordUtils.buildPasswordKey(BackupPasswordManager.PBKDF_CURRENT, this.mEncryptPassword, newUserSalt, 10000);
        byte[] masterPw = new byte[32];
        this.backupManagerService.getRng().nextBytes(masterPw);
        byte[] checksumSalt = this.backupManagerService.randomBytes(512);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec masterKeySpec = new SecretKeySpec(masterPw, "AES");
        c.init(1, masterKeySpec);
        OutputStream finalOutput = new CipherOutputStream(ofstream, c);
        headerbuf.append(PasswordUtils.ENCRYPTION_ALGORITHM_NAME);
        headerbuf.append(10);
        headerbuf.append(PasswordUtils.byteArrayToHex(newUserSalt));
        headerbuf.append(10);
        headerbuf.append(PasswordUtils.byteArrayToHex(checksumSalt));
        headerbuf.append(10);
        headerbuf.append(10000);
        headerbuf.append(10);
        Cipher mkC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        mkC.init(1, userKey);
        headerbuf.append(PasswordUtils.byteArrayToHex(mkC.getIV()));
        headerbuf.append(10);
        byte[] IV = c.getIV();
        byte[] mk = masterKeySpec.getEncoded();
        byte[] checksum = PasswordUtils.makeKeyChecksum(BackupPasswordManager.PBKDF_CURRENT, masterKeySpec.getEncoded(), checksumSalt, 10000);
        ByteArrayOutputStream blob = new ByteArrayOutputStream(((IV.length + mk.length) + checksum.length) + 3);
        DataOutputStream mkOut = new DataOutputStream(blob);
        mkOut.writeByte(IV.length);
        mkOut.write(IV);
        mkOut.writeByte(mk.length);
        mkOut.write(mk);
        mkOut.writeByte(checksum.length);
        mkOut.write(checksum);
        mkOut.flush();
        headerbuf.append(PasswordUtils.byteArrayToHex(mkC.doFinal(blob.toByteArray())));
        headerbuf.append(10);
        return finalOutput;
    }

    private void finalizeBackup(OutputStream out) {
        try {
            out.write(new byte[1024]);
        } catch (IOException e) {
            Slog.w(RefactoredBackupManagerService.TAG, "Error attempting to finalize backup stream");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:138:0x03d5 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0363 A:{PHI: r7 , Splitter: B:89:0x027d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x04fe A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0381 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0363 A:{PHI: r7 , Splitter: B:89:0x027d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x044f A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0363 A:{PHI: r7 , Splitter: B:89:0x027d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* JADX WARNING: Missing block: B:114:?, code:
            android.util.Slog.e(com.android.server.backup.RefactoredBackupManagerService.TAG, "App died during full backup");
     */
    /* JADX WARNING: Missing block: B:115:0x036d, code:
            if (r7 != null) goto L_0x036f;
     */
    /* JADX WARNING: Missing block: B:117:?, code:
            r7.flush();
            r7.close();
     */
    /* JADX WARNING: Missing block: B:118:0x0375, code:
            r40.mOutputFile.close();
     */
    /* JADX WARNING: Missing block: B:120:0x0380, code:
            monitor-enter(r40.mLatch);
     */
    /* JADX WARNING: Missing block: B:122:?, code:
            r40.mLatch.set(true);
            r40.mLatch.notifyAll();
     */
    /* JADX WARNING: Missing block: B:124:0x0391, code:
            sendEndBackup();
            r0.tearDown();
            android.util.Slog.d(com.android.server.backup.RefactoredBackupManagerService.TAG, "Full backup pass complete.");
            r40.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Missing block: B:208:0x05b5, code:
            r22 = move-exception;
     */
    /* JADX WARNING: Missing block: B:209:0x05b6, code:
            android.util.Slog.e(com.android.server.backup.RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + r22.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        int i;
        PackageInfo pkg;
        Throwable e;
        Slog.i(RefactoredBackupManagerService.TAG, "--- Performing adb backup" + (this.mKeyValue ? ", including key-value backups" : "") + " ---");
        TreeMap<String, PackageInfo> packagesToBackup = new TreeMap();
        FullBackupObbConnection fullBackupObbConnection = new FullBackupObbConnection(this.backupManagerService);
        fullBackupObbConnection.establish();
        sendStartBackup();
        if (this.mAllApps) {
            List<PackageInfo> allPackages = this.backupManagerService.getPackageManager().getInstalledPackages(64);
            for (i = 0; i < allPackages.size(); i++) {
                pkg = (PackageInfo) allPackages.get(i);
                if (this.mIncludeSystem || (pkg.applicationInfo.flags & 1) == 0) {
                    packagesToBackup.put(pkg.packageName, pkg);
                }
            }
        }
        if (this.mDoWidgets) {
            List<String> pkgs = AppWidgetBackupBridge.getWidgetParticipants(0);
            if (pkgs != null) {
                addPackagesToSet(packagesToBackup, pkgs);
            }
        }
        if (this.mPackages != null) {
            addPackagesToSet(packagesToBackup, this.mPackages);
        }
        ArrayList<PackageInfo> keyValueBackupQueue = new ArrayList();
        Iterator<Entry<String, PackageInfo>> iter = packagesToBackup.entrySet().iterator();
        while (iter.hasNext()) {
            pkg = (PackageInfo) ((Entry) iter.next()).getValue();
            if (!AppBackupUtils.appIsEligibleForBackup(pkg.applicationInfo) || AppBackupUtils.appIsStopped(pkg.applicationInfo)) {
                iter.remove();
                Slog.i(RefactoredBackupManagerService.TAG, "Package " + pkg.packageName + " is not eligible for backup, removing.");
            } else if (AppBackupUtils.appIsKeyValueOnly(pkg)) {
                iter.remove();
                Slog.i(RefactoredBackupManagerService.TAG, "Package " + pkg.packageName + " is key-value.");
                keyValueBackupQueue.add(pkg);
            }
        }
        ArrayList<PackageInfo> arrayList = new ArrayList(packagesToBackup.values());
        OutputStream fileOutputStream = new FileOutputStream(this.mOutputFile.getFileDescriptor());
        OutputStream out = null;
        try {
            boolean encrypting = this.mEncryptPassword != null && this.mEncryptPassword.length() > 0;
            if (!this.backupManagerService.deviceIsEncrypted() || (encrypting ^ 1) == 0) {
                OutputStream finalOutput = fileOutputStream;
                if (this.backupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
                    OutputStream finalOutput2;
                    StringBuilder stringBuilder = new StringBuilder(1024);
                    stringBuilder.append(RefactoredBackupManagerService.BACKUP_FILE_HEADER_MAGIC);
                    stringBuilder.append(5);
                    stringBuilder.append(this.mCompress ? "\n1\n" : "\n0\n");
                    if (encrypting) {
                        try {
                            finalOutput2 = emitAesBackupHeader(stringBuilder, fileOutputStream);
                        } catch (Exception e2) {
                            e = e2;
                            Slog.e(RefactoredBackupManagerService.TAG, "Unable to emit archive header", e);
                            try {
                                this.mOutputFile.close();
                            } catch (IOException e3) {
                                Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e3.getMessage());
                            }
                            synchronized (this.mLatch) {
                            }
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                            this.backupManagerService.getWakelock().release();
                            return;
                        } catch (RemoteException e4) {
                        }
                    } else {
                        stringBuilder.append("none\n");
                        finalOutput2 = finalOutput;
                    }
                    try {
                        fileOutputStream.write(stringBuilder.toString().getBytes("UTF-8"));
                        if (this.mCompress) {
                            fileOutputStream = new DeflaterOutputStream(finalOutput2, new Deflater(9), true);
                        } else {
                            finalOutput = finalOutput2;
                        }
                        out = finalOutput;
                        if (this.mIncludeShared) {
                            try {
                                arrayList.add(this.backupManagerService.getPackageManager().getPackageInfo(RefactoredBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, 0));
                            } catch (NameNotFoundException e5) {
                                Slog.e(RefactoredBackupManagerService.TAG, "Unable to find shared-storage backup handler");
                            }
                        }
                        int N = arrayList.size();
                        i = 0;
                        while (i < N) {
                            pkg = (PackageInfo) arrayList.get(i);
                            Slog.i(RefactoredBackupManagerService.TAG, "--- Performing full backup for package " + pkg.packageName + " ---");
                            boolean isSharedStorage = pkg.packageName.equals(RefactoredBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                            this.mBackupEngine = new FullBackupEngine(this.backupManagerService, out, null, pkg, this.mIncludeApks, this, JobStatus.NO_LATEST_RUNTIME, this.mCurrentOpToken);
                            sendOnBackupPackage(isSharedStorage ? "Shared storage" : pkg.packageName);
                            this.mCurrentTarget = pkg;
                            this.mBackupEngine.backupOnePackage();
                            if (!this.mIncludeObbs || fullBackupObbConnection.backupObbs(pkg, out)) {
                                i++;
                            } else {
                                throw new RuntimeException("Failure writing OBB stack for " + pkg);
                            }
                        }
                        if (this.mKeyValue) {
                            for (PackageInfo keyValuePackage : keyValueBackupQueue) {
                                Slog.i(RefactoredBackupManagerService.TAG, "--- Performing key-value backup for package " + keyValuePackage.packageName + " ---");
                                KeyValueAdbBackupEngine kvBackupEngine = new KeyValueAdbBackupEngine(out, keyValuePackage, this.backupManagerService, this.backupManagerService.getPackageManager(), this.backupManagerService.getBaseStateDir(), this.backupManagerService.getDataDir());
                                sendOnBackupPackage(keyValuePackage.packageName);
                                kvBackupEngine.backupOnePackage();
                            }
                        }
                        finalizeBackup(out);
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException e32) {
                                Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e32.getMessage());
                            }
                        }
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                            this.mLatch.set(true);
                            this.mLatch.notifyAll();
                        }
                        sendEndBackup();
                        fullBackupObbConnection.tearDown();
                        Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                        this.backupManagerService.getWakelock().release();
                    } catch (Exception e6) {
                        e = e6;
                        finalOutput = finalOutput2;
                        Slog.e(RefactoredBackupManagerService.TAG, "Unable to emit archive header", e);
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                            this.mLatch.set(true);
                            this.mLatch.notifyAll();
                        }
                        sendEndBackup();
                        fullBackupObbConnection.tearDown();
                        Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                        this.backupManagerService.getWakelock().release();
                        return;
                    } catch (RemoteException e42) {
                    } catch (Throwable th) {
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException e322) {
                                Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e322.getMessage());
                                synchronized (this.mLatch) {
                                }
                            }
                        }
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                            this.mLatch.set(true);
                            this.mLatch.notifyAll();
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                            this.backupManagerService.getWakelock().release();
                        }
                    }
                    return;
                }
                Slog.w(RefactoredBackupManagerService.TAG, "Backup password mismatch; aborting");
                try {
                    this.mOutputFile.close();
                } catch (IOException e3222) {
                    Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e3222.getMessage());
                }
                synchronized (this.mLatch) {
                    this.mLatch.set(true);
                    this.mLatch.notifyAll();
                }
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                this.backupManagerService.getWakelock().release();
                return;
            }
            Slog.e(RefactoredBackupManagerService.TAG, "Unencrypted backup of encrypted device; aborting");
            try {
                this.mOutputFile.close();
            } catch (IOException e32222) {
                Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e32222.getMessage());
            }
            synchronized (this.mLatch) {
                this.mLatch.set(true);
                this.mLatch.notifyAll();
            }
            sendEndBackup();
            fullBackupObbConnection.tearDown();
            Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
            this.backupManagerService.getWakelock().release();
        } catch (RemoteException e422) {
        } catch (Throwable e7) {
            Slog.e(RefactoredBackupManagerService.TAG, "Internal exception during full backup", e7);
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e322222) {
                    Slog.e(RefactoredBackupManagerService.TAG, "IO error closing adb backup file: " + e322222.getMessage());
                    synchronized (this.mLatch) {
                    }
                }
            }
            this.mOutputFile.close();
            synchronized (this.mLatch) {
                this.mLatch.set(true);
                this.mLatch.notifyAll();
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                Slog.d(RefactoredBackupManagerService.TAG, "Full backup pass complete.");
                this.backupManagerService.getWakelock().release();
            }
        }
    }

    public void execute() {
    }

    public void operationComplete(long result) {
    }

    public void handleCancel(boolean cancelAll) {
        PackageInfo target = this.mCurrentTarget;
        Slog.w(RefactoredBackupManagerService.TAG, "adb backup cancel of " + target);
        if (target != null) {
            this.backupManagerService.tearDownAgentAndKill(this.mCurrentTarget.applicationInfo);
        }
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }
}
