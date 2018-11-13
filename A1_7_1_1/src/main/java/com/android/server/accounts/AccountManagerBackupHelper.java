package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountManagerInternal;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.PackageUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public final class AccountManagerBackupHelper {
    private static final String ACCOUNT_ACCESS_GRANTS = "SELECT name, uid FROM accounts, grants WHERE accounts_id=_id";
    private static final String ATTR_ACCOUNT_SHA_256 = "account-sha-256";
    private static final String ATTR_DIGEST = "digest";
    private static final String ATTR_PACKAGE = "package";
    private static final long PENDING_RESTORE_TIMEOUT_MILLIS = 3600000;
    private static final String TAG = "AccountManagerBackupHelper";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSIONS = "permissions";
    private final AccountManagerInternal mAccountManagerInternal;
    private final AccountManagerService mAccountManagerService;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private Runnable mRestoreCancelCommand;
    @GuardedBy("mLock")
    private RestorePackageMonitor mRestorePackageMonitor;
    @GuardedBy("mLock")
    private List<PendingAppPermission> mRestorePendingAppPermissions;

    private final class CancelRestoreCommand implements Runnable {
        /* synthetic */ CancelRestoreCommand(AccountManagerBackupHelper this$0, CancelRestoreCommand cancelRestoreCommand) {
            this();
        }

        private CancelRestoreCommand() {
        }

        public void run() {
            synchronized (AccountManagerBackupHelper.this.mLock) {
                AccountManagerBackupHelper.this.mRestorePendingAppPermissions = null;
                if (AccountManagerBackupHelper.this.mRestorePackageMonitor != null) {
                    AccountManagerBackupHelper.this.mRestorePackageMonitor.unregister();
                    AccountManagerBackupHelper.this.mRestorePackageMonitor = null;
                }
            }
        }
    }

    private final class PendingAppPermission {
        private final String accountDigest;
        private final String certDigest;
        private final String packageName;
        private final int userId;

        public PendingAppPermission(String accountDigest, String packageName, String certDigest, int userId) {
            this.accountDigest = accountDigest;
            this.packageName = packageName;
            this.certDigest = certDigest;
            this.userId = userId;
        }

        public boolean apply(PackageManager packageManager) {
            Account account = null;
            UserAccounts accounts = AccountManagerBackupHelper.this.mAccountManagerService.getUserAccounts(this.userId);
            synchronized (accounts.cacheLock) {
                for (Account[] accountsPerType : accounts.accountCache.values()) {
                    for (Account accountPerType : (Account[]) accountsPerType$iterator.next()) {
                        if (this.accountDigest.equals(PackageUtils.computeSha256Digest(accountPerType.name.getBytes()))) {
                            account = accountPerType;
                            continue;
                            break;
                        }
                    }
                    if (account != null) {
                        break;
                    }
                }
            }
            if (account == null) {
                return false;
            }
            try {
                PackageInfo packageInfo = packageManager.getPackageInfoAsUser(this.packageName, 64, this.userId);
                if (!this.certDigest.equals(PackageUtils.computeCertSha256Digest(packageInfo.signatures[0]))) {
                    return false;
                }
                int uid = packageInfo.applicationInfo.uid;
                if (!AccountManagerBackupHelper.this.mAccountManagerInternal.hasAccountAccess(account, uid)) {
                    AccountManagerBackupHelper.this.mAccountManagerService.grantAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid);
                }
                return true;
            } catch (NameNotFoundException e) {
                return false;
            }
        }
    }

    private final class RestorePackageMonitor extends PackageMonitor {
        /* synthetic */ RestorePackageMonitor(AccountManagerBackupHelper this$0, RestorePackageMonitor restorePackageMonitor) {
            this();
        }

        private RestorePackageMonitor() {
        }

        /* JADX WARNING: Missing block: B:31:0x0094, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageAdded(String packageName, int uid) {
            synchronized (AccountManagerBackupHelper.this.mLock) {
                if (AccountManagerBackupHelper.this.mRestorePendingAppPermissions == null) {
                } else if (UserHandle.getUserId(uid) != 0) {
                } else {
                    for (int i = AccountManagerBackupHelper.this.mRestorePendingAppPermissions.size() - 1; i >= 0; i--) {
                        PendingAppPermission pendingAppPermission = (PendingAppPermission) AccountManagerBackupHelper.this.mRestorePendingAppPermissions.get(i);
                        if (pendingAppPermission.packageName.equals(packageName) && pendingAppPermission.apply(AccountManagerBackupHelper.this.mAccountManagerService.mContext.getPackageManager())) {
                            AccountManagerBackupHelper.this.mRestorePendingAppPermissions.remove(i);
                        }
                    }
                    if (AccountManagerBackupHelper.this.mRestorePendingAppPermissions.isEmpty() && AccountManagerBackupHelper.this.mRestoreCancelCommand != null) {
                        AccountManagerBackupHelper.this.mAccountManagerService.mMessageHandler.removeCallbacks(AccountManagerBackupHelper.this.mRestoreCancelCommand);
                        AccountManagerBackupHelper.this.mRestoreCancelCommand.run();
                        AccountManagerBackupHelper.this.mRestoreCancelCommand = null;
                    }
                }
            }
        }
    }

    public AccountManagerBackupHelper(AccountManagerService accountManagerService, AccountManagerInternal accountManagerInternal) {
        this.mAccountManagerService = accountManagerService;
        this.mAccountManagerInternal = accountManagerInternal;
    }

    public byte[] backupAccountAccessPermissions(int userId) {
        Throwable th;
        Cursor cursor;
        Throwable th2;
        UserAccounts accounts = this.mAccountManagerService.getUserAccounts(userId);
        synchronized (accounts.cacheLock) {
            th = null;
            cursor = null;
            try {
                cursor = accounts.openHelper.getReadableDatabase().rawQuery(ACCOUNT_ACCESS_GRANTS, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    }
                    if (th != null) {
                        throw th;
                    } else {
                        return null;
                    }
                }
                int nameColumnIdx = cursor.getColumnIndex("name");
                int uidColumnIdx = cursor.getColumnIndex(PackageProcessList.KEY_UID);
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                try {
                    XmlSerializer serializer = new FastXmlSerializer();
                    serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.startTag(null, TAG_PERMISSIONS);
                    PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
                    do {
                        String accountName = cursor.getString(nameColumnIdx);
                        String[] packageNames = packageManager.getPackagesForUid(cursor.getInt(uidColumnIdx));
                        if (packageNames != null) {
                            for (String packageName : packageNames) {
                                String digest = PackageUtils.computePackageCertSha256Digest(packageManager, packageName, userId);
                                if (digest != null) {
                                    serializer.startTag(null, TAG_PERMISSION);
                                    serializer.attribute(null, ATTR_ACCOUNT_SHA_256, PackageUtils.computeSha256Digest(accountName.getBytes()));
                                    serializer.attribute(null, ATTR_PACKAGE, packageName);
                                    serializer.attribute(null, ATTR_DIGEST, digest);
                                    serializer.endTag(null, TAG_PERMISSION);
                                }
                            }
                        }
                    } while (cursor.moveToNext());
                    serializer.endTag(null, TAG_PERMISSIONS);
                    serializer.endDocument();
                    serializer.flush();
                    byte[] toByteArray = dataStream.toByteArray();
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    if (th != null) {
                        throw th;
                    } else {
                        return toByteArray;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error backing up account access grants", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th5) {
                    th = th5;
                }
            } catch (Throwable th6) {
                Throwable th7 = th6;
                th6 = th2;
                th2 = th7;
            }
        }
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable th8) {
                if (th6 == null) {
                    th6 = th8;
                } else if (th6 != th8) {
                    th6.addSuppressed(th8);
                }
            }
        }
        if (th6 != null) {
            throw th6;
        }
        throw th2;
        if (th6 == null) {
            return null;
        }
        throw th6;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d9 A:{Splitter: B:0:0x0000, ExcHandler: org.xmlpull.v1.XmlPullParserException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:35:0x00d9, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:36:0x00da, code:
            android.util.Log.e(TAG, "Error restoring app permissions", r9);
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void restoreAccountAccessPermissions(byte[] data, int userId) {
        try {
            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(dataStream, StandardCharsets.UTF_8.name());
            PackageManager packageManager = this.mAccountManagerService.mContext.getPackageManager();
            int permissionsOuterDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, permissionsOuterDepth)) {
                if (TAG_PERMISSIONS.equals(parser.getName())) {
                    int permissionOuterDepth = parser.getDepth();
                    while (XmlUtils.nextElementWithin(parser, permissionOuterDepth)) {
                        if (TAG_PERMISSION.equals(parser.getName())) {
                            String accountDigest = parser.getAttributeValue(null, ATTR_ACCOUNT_SHA_256);
                            if (TextUtils.isEmpty(accountDigest)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            String packageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                            if (TextUtils.isEmpty(packageName)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            String digest = parser.getAttributeValue(null, ATTR_DIGEST);
                            if (TextUtils.isEmpty(digest)) {
                                XmlUtils.skipCurrentTag(parser);
                            }
                            PendingAppPermission pendingAppPermission = new PendingAppPermission(accountDigest, packageName, digest, userId);
                            if (pendingAppPermission.apply(packageManager)) {
                                continue;
                            } else {
                                synchronized (this.mLock) {
                                    if (this.mRestorePackageMonitor == null) {
                                        this.mRestorePackageMonitor = new RestorePackageMonitor(this, null);
                                        this.mRestorePackageMonitor.register(this.mAccountManagerService.mContext, this.mAccountManagerService.mMessageHandler.getLooper(), true);
                                    }
                                    if (this.mRestorePendingAppPermissions == null) {
                                        this.mRestorePendingAppPermissions = new ArrayList();
                                    }
                                    this.mRestorePendingAppPermissions.add(pendingAppPermission);
                                }
                            }
                        }
                    }
                    continue;
                }
            }
            synchronized (this.mLock) {
                this.mRestoreCancelCommand = new CancelRestoreCommand(this, null);
            }
            this.mAccountManagerService.mMessageHandler.postDelayed(this.mRestoreCancelCommand, PENDING_RESTORE_TIMEOUT_MILLIS);
        } catch (Exception e) {
        }
    }
}
