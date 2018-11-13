package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManagerInternal;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.accounts.AuthenticatorDescription;
import android.accounts.CantAddAccountActivity;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManager.Stub;
import android.accounts.IAccountManagerResponse;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AccountManagerService extends Stub implements RegisteredServicesCacheListener<AuthenticatorDescription> {
    private static final Intent ACCOUNTS_CHANGED_INTENT = null;
    static final String ACCOUNTS_ID = "_id";
    private static final String ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS = "last_password_entry_time_millis_epoch";
    static final String ACCOUNTS_NAME = "name";
    private static final String ACCOUNTS_PASSWORD = "password";
    private static final String ACCOUNTS_PREVIOUS_NAME = "previous_name";
    private static final String ACCOUNTS_TYPE = "type";
    private static final String ACCOUNTS_TYPE_COUNT = "count(type)";
    private static final String[] ACCOUNT_TYPE_COUNT_PROJECTION = null;
    private static final String AUTHTOKENS_ACCOUNTS_ID = "accounts_id";
    private static final String AUTHTOKENS_AUTHTOKEN = "authtoken";
    private static final String AUTHTOKENS_ID = "_id";
    private static final String AUTHTOKENS_TYPE = "type";
    private static final String CE_DATABASE_NAME = "accounts_ce.db";
    private static final int CE_DATABASE_VERSION = 10;
    private static final String CE_DB_PREFIX = "ceDb.";
    private static final String CE_TABLE_ACCOUNTS = "ceDb.accounts";
    private static final String CE_TABLE_AUTHTOKENS = "ceDb.authtokens";
    private static final String CE_TABLE_EXTRAS = "ceDb.extras";
    private static final String[] COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN = null;
    private static final String[] COLUMNS_EXTRAS_KEY_AND_VALUE = null;
    private static final String COUNT_OF_MATCHING_GRANTS = "SELECT COUNT(*) FROM grants, accounts WHERE accounts_id=_id AND uid=? AND auth_token_type=? AND name=? AND type=?";
    private static final String COUNT_OF_MATCHING_GRANTS_ANY_TOKEN = "SELECT COUNT(*) FROM grants, accounts WHERE accounts_id=_id AND uid=? AND name=? AND type=?";
    private static final String DATABASE_NAME = "accounts.db";
    private static final String DE_DATABASE_NAME = "accounts_de.db";
    private static final int DE_DATABASE_VERSION = 1;
    private static final Account[] EMPTY_ACCOUNT_ARRAY = null;
    private static final String EXTRAS_ACCOUNTS_ID = "accounts_id";
    private static final String EXTRAS_ID = "_id";
    private static final String EXTRAS_KEY = "key";
    private static final String EXTRAS_VALUE = "value";
    static final String GRANTS_ACCOUNTS_ID = "accounts_id";
    private static final String GRANTS_AUTH_TOKEN_TYPE = "auth_token_type";
    static final String GRANTS_GRANTEE_UID = "uid";
    private static final int MAX_DEBUG_DB_SIZE = 64;
    private static final int MESSAGE_COPY_SHARED_ACCOUNT = 4;
    private static final int MESSAGE_TIMED_OUT = 3;
    private static final String META_KEY = "key";
    private static final String META_KEY_DELIMITER = ":";
    private static final String META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX = "auth_uid_for_type:";
    private static final String META_VALUE = "value";
    private static final String PRE_N_DATABASE_NAME = "accounts.db";
    private static final int PRE_N_DATABASE_VERSION = 9;
    private static final String SELECTION_AUTHTOKENS_BY_ACCOUNT = "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)";
    private static final String SELECTION_META_BY_AUTHENTICATOR_TYPE = "key LIKE ?";
    private static final String SELECTION_USERDATA_BY_ACCOUNT = "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)";
    private static final String SHARED_ACCOUNTS_ID = "_id";
    static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_AUTHTOKENS = "authtokens";
    private static final String TABLE_EXTRAS = "extras";
    static final String TABLE_GRANTS = "grants";
    private static final String TABLE_META = "meta";
    private static final String TABLE_SHARED_ACCOUNTS = "shared_accounts";
    private static final String TAG = "AccountManager";
    private static AtomicReference<AccountManagerService> sThis;
    private final AppOpsManager mAppOpsManager;
    private final CopyOnWriteArrayList<OnAppPermissionChangeListener> mAppPermissionChangeListeners;
    private final IAccountAuthenticatorCache mAuthenticatorCache;
    final Context mContext;
    private final SparseBooleanArray mLocalUnlockedUsers;
    final MessageHandler mMessageHandler;
    private final AtomicInteger mNotificationIds;
    private final PackageManager mPackageManager;
    private final LinkedHashMap<String, Session> mSessions;
    private UserManager mUserManager;
    private final SparseArray<UserAccounts> mUsers;

    final /* synthetic */ class -void__init__android_content_Context_context_android_content_pm_PackageManager_packageManager_com_android_server_accounts_IAccountAuthenticatorCache_authenticatorCache_LambdaImpl0 implements OnPermissionsChangedListener {
        public void onPermissionsChanged(int arg0) {
            AccountManagerService.this.m13-com_android_server_accounts_AccountManagerService_lambda$1(arg0);
        }
    }

    final /* synthetic */ class -void_grantAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0 implements Runnable {
        private /* synthetic */ Account val$account;
        private /* synthetic */ OnAppPermissionChangeListener val$listener;
        private /* synthetic */ int val$uid;

        public /* synthetic */ -void_grantAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0(OnAppPermissionChangeListener onAppPermissionChangeListener, Account account, int i) {
            this.val$listener = onAppPermissionChangeListener;
            this.val$account = account;
            this.val$uid = i;
        }

        public void run() {
            this.val$listener.onAppPermissionChanged(this.val$account, this.val$uid);
        }
    }

    final /* synthetic */ class -void_revokeAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0 implements Runnable {
        private /* synthetic */ Account val$account;
        private /* synthetic */ OnAppPermissionChangeListener val$listener;
        private /* synthetic */ int val$uid;

        public /* synthetic */ -void_revokeAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0(OnAppPermissionChangeListener onAppPermissionChangeListener, Account account, int i) {
            this.val$listener = onAppPermissionChangeListener;
            this.val$account = account;
            this.val$uid = i;
        }

        public void run() {
            this.val$listener.onAppPermissionChanged(this.val$account, this.val$uid);
        }
    }

    private abstract class Session extends IAccountAuthenticatorResponse.Stub implements DeathRecipient, ServiceConnection {
        final String mAccountName;
        final String mAccountType;
        protected final UserAccounts mAccounts;
        final boolean mAuthDetailsRequired;
        IAccountAuthenticator mAuthenticator;
        final long mCreationTime;
        final boolean mExpectActivityLaunch;
        private int mNumErrors;
        private int mNumRequestContinued;
        public int mNumResults;
        IAccountManagerResponse mResponse;
        private final boolean mStripAuthTokenFromResult;
        final boolean mUpdateLastAuthenticatedTime;

        public abstract void run() throws RemoteException;

        public Session(AccountManagerService this$0, UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired) {
            this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult, accountName, authDetailsRequired, false);
        }

        public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            this.mNumResults = 0;
            this.mNumRequestContinued = 0;
            this.mNumErrors = 0;
            this.mAuthenticator = null;
            if (accountType == null) {
                throw new IllegalArgumentException("accountType is null");
            }
            this.mAccounts = accounts;
            this.mStripAuthTokenFromResult = stripAuthTokenFromResult;
            this.mResponse = response;
            this.mAccountType = accountType;
            this.mExpectActivityLaunch = expectActivityLaunch;
            this.mCreationTime = SystemClock.elapsedRealtime();
            this.mAccountName = accountName;
            this.mAuthDetailsRequired = authDetailsRequired;
            this.mUpdateLastAuthenticatedTime = updateLastAuthenticatedTime;
            synchronized (AccountManagerService.this.mSessions) {
                AccountManagerService.this.mSessions.put(toString(), this);
            }
            if (response != null) {
                try {
                    response.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    this.mResponse = null;
                    binderDied();
                }
            }
        }

        IAccountManagerResponse getResponseAndClose() {
            if (this.mResponse == null) {
                return null;
            }
            IAccountManagerResponse response = this.mResponse;
            close();
            return response;
        }

        protected void checkKeyIntent(int authUid, Intent intent) throws SecurityException {
            intent.setFlags(intent.getFlags() & -196);
            long bid = Binder.clearCallingIdentity();
            try {
                PackageManager pm = AccountManagerService.this.mContext.getPackageManager();
                ActivityInfo targetActivityInfo = pm.resolveActivityAsUser(intent, 0, this.mAccounts.userId).activityInfo;
                if (pm.checkSignatures(authUid, targetActivityInfo.applicationInfo.uid) != 0) {
                    String pkgName = targetActivityInfo.packageName;
                    Object[] objArr = new Object[3];
                    objArr[0] = targetActivityInfo.name;
                    objArr[1] = pkgName;
                    objArr[2] = this.mAccountType;
                    throw new SecurityException(String.format("KEY_INTENT resolved to an Activity (%s) in a package (%s) that does not share a signature with the supplying authenticator (%s).", objArr));
                }
            } finally {
                Binder.restoreCallingIdentity(bid);
            }
        }

        /* JADX WARNING: Missing block: B:9:0x001d, code:
            if (r4.mResponse == null) goto L_0x002b;
     */
        /* JADX WARNING: Missing block: B:10:0x001f, code:
            r4.mResponse.asBinder().unlinkToDeath(r4, 0);
            r4.mResponse = null;
     */
        /* JADX WARNING: Missing block: B:11:0x002b, code:
            cancelTimeout();
            unbind();
     */
        /* JADX WARNING: Missing block: B:12:0x0031, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void close() {
            synchronized (AccountManagerService.this.mSessions) {
                if (AccountManagerService.this.mSessions.remove(toString()) == null) {
                }
            }
        }

        public void binderDied() {
            this.mResponse = null;
            close();
        }

        protected String toDebugString() {
            return toDebugString(SystemClock.elapsedRealtime());
        }

        protected String toDebugString(long now) {
            return "Session: expectLaunch " + this.mExpectActivityLaunch + ", connected " + (this.mAuthenticator != null) + ", stats (" + this.mNumResults + "/" + this.mNumRequestContinued + "/" + this.mNumErrors + ")" + ", lifetime " + (((double) (now - this.mCreationTime)) / 1000.0d);
        }

        void bind() {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "initiating bind to authenticator type " + this.mAccountType);
            }
            if (!bindToAuthenticator(this.mAccountType)) {
                Log.d(AccountManagerService.TAG, "bind attempt failed for " + toDebugString());
                onError(1, "bind failure");
            }
        }

        private void unbind() {
            if (this.mAuthenticator != null) {
                this.mAuthenticator = null;
                AccountManagerService.this.mContext.unbindService(this);
            }
        }

        public void cancelTimeout() {
            AccountManagerService.this.mMessageHandler.removeMessages(3, this);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            try {
                run();
            } catch (RemoteException e) {
                onError(1, "remote exception");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(1, "disconnected");
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onServiceDisconnected: caught RemoteException while responding", e);
                    }
                }
            }
        }

        public void onTimedOut() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(1, "timeout");
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onTimedOut: caught RemoteException while responding", e);
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x00f1  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            Intent intent = null;
            if (result != null) {
                boolean isSuccessfulUpdateCredsOrAddAccount;
                boolean isSuccessfulConfirmCreds = result.getBoolean("booleanResult", false);
                if (result.containsKey("authAccount")) {
                    isSuccessfulUpdateCredsOrAddAccount = result.containsKey("accountType");
                } else {
                    isSuccessfulUpdateCredsOrAddAccount = false;
                }
                boolean needUpdate = this.mUpdateLastAuthenticatedTime ? !isSuccessfulConfirmCreds ? isSuccessfulUpdateCredsOrAddAccount : true : false;
                if (needUpdate || this.mAuthDetailsRequired) {
                    boolean accountPresent = AccountManagerService.this.isAccountPresentForCaller(this.mAccountName, this.mAccountType);
                    if (needUpdate && accountPresent) {
                        AccountManagerService.this.updateLastAuthenticatedTime(new Account(this.mAccountName, this.mAccountType));
                    }
                    if (this.mAuthDetailsRequired) {
                        long lastAuthenticatedTime = -1;
                        if (accountPresent) {
                            String[] strArr = new String[2];
                            strArr[0] = this.mAccountName;
                            strArr[1] = this.mAccountType;
                            lastAuthenticatedTime = DatabaseUtils.longForQuery(this.mAccounts.openHelper.getReadableDatabase(), "SELECT last_password_entry_time_millis_epoch FROM accounts WHERE name=? AND type=?", strArr);
                        }
                        result.putLong("lastAuthenticatedTime", lastAuthenticatedTime);
                    }
                }
            }
            if (result != null) {
                intent = (Intent) result.getParcelable("intent");
                if (intent != null) {
                    checkKeyIntent(Binder.getCallingUid(), intent);
                }
            }
            if (result != null) {
                if (!TextUtils.isEmpty(result.getString(AccountManagerService.AUTHTOKENS_AUTHTOKEN))) {
                    String accountName = result.getString("authAccount");
                    String accountType = result.getString("accountType");
                    if (!(TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType))) {
                        AccountManagerService.this.cancelNotification(AccountManagerService.this.getSigninRequiredNotificationId(this.mAccounts, new Account(accountName, accountType)).intValue(), new UserHandle(this.mAccounts.userId));
                    }
                }
            }
            if (this.mExpectActivityLaunch && result != null) {
                if (result.containsKey("intent")) {
                    response = this.mResponse;
                    if (response != null) {
                        return;
                    }
                    if (result == null) {
                        try {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                            }
                            response.onError(5, "null bundle returned");
                            return;
                        } catch (RemoteException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                                return;
                            }
                            return;
                        }
                    }
                    if (this.mStripAuthTokenFromResult) {
                        result.remove(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                        response.onResult(result);
                        return;
                    } else {
                        response.onError(result.getInt("errorCode"), result.getString("errorMessage"));
                        return;
                    }
                }
            }
            response = getResponseAndClose();
            if (response != null) {
            }
        }

        public void onRequestContinued() {
            this.mNumRequestContinued++;
        }

        public void onError(int errorCode, String errorMessage) {
            this.mNumErrors++;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                }
                try {
                    response.onError(errorCode, errorMessage);
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onError: caught RemoteException while responding", e);
                    }
                }
            } else if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "Session.onError: already closed");
            }
        }

        private boolean bindToAuthenticator(String authenticatorType) {
            ServiceInfo<AuthenticatorDescription> authenticatorInfo = AccountManagerService.this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(authenticatorType), this.mAccounts.userId);
            if (authenticatorInfo == null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "there is no authenticator for " + authenticatorType + ", bailing out");
                }
                return false;
            } else if (AccountManagerService.this.isLocalUnlockedUser(this.mAccounts.userId) || authenticatorInfo.componentInfo.directBootAware) {
                Intent intent = new Intent();
                intent.setAction("android.accounts.AccountAuthenticator");
                intent.setComponent(authenticatorInfo.componentName);
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "performing bindService to " + authenticatorInfo.componentName);
                }
                if (AccountManagerService.this.mContext.bindServiceAsUser(intent, this, 1, UserHandle.of(this.mAccounts.userId))) {
                    return true;
                }
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "bindService to " + authenticatorInfo.componentName + " failed");
                }
                return false;
            } else {
                Slog.w(AccountManagerService.TAG, "Blocking binding to authenticator " + authenticatorInfo.componentName + " which isn't encryption aware");
                return false;
            }
        }
    }

    private abstract class StartAccountSession extends Session {
        private final boolean mIsPasswordForwardingAllowed;

        public StartAccountSession(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticationTime, boolean isPasswordForwardingAllowed) {
            super(accounts, response, accountType, expectActivityLaunch, true, accountName, authDetailsRequired, updateLastAuthenticationTime);
            this.mIsPasswordForwardingAllowed = isPasswordForwardingAllowed;
        }

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            Intent intent = null;
            if (result != null) {
                intent = (Intent) result.getParcelable("intent");
                if (intent != null) {
                    checkKeyIntent(Binder.getCallingUid(), intent);
                }
            }
            if (this.mExpectActivityLaunch && result != null && result.containsKey("intent")) {
                response = this.mResponse;
            } else {
                response = getResponseAndClose();
            }
            if (response != null) {
                if (result == null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                    }
                    AccountManagerService.this.sendErrorResponse(response, 5, "null bundle returned");
                } else if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                    if (!this.mIsPasswordForwardingAllowed) {
                        result.remove(AccountManagerService.ACCOUNTS_PASSWORD);
                    }
                    result.remove(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle sessionBundle = result.getBundle("accountSessionBundle");
                    if (sessionBundle != null) {
                        String accountType = sessionBundle.getString("accountType");
                        if (TextUtils.isEmpty(accountType) || !this.mAccountType.equalsIgnoreCase(accountType)) {
                            Log.w(AccountManagerService.TAG, "Account type in session bundle doesn't match request.");
                        }
                        sessionBundle.putString("accountType", this.mAccountType);
                        try {
                            result.putBundle("accountSessionBundle", CryptoHelper.getInstance().encryptBundle(sessionBundle));
                        } catch (GeneralSecurityException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 3)) {
                                Log.v(AccountManagerService.TAG, "Failed to encrypt session bundle!", e);
                            }
                            AccountManagerService.this.sendErrorResponse(response, 5, "failed to encrypt session bundle");
                            return;
                        }
                    }
                    AccountManagerService.this.sendResponse(response, result);
                } else {
                    AccountManagerService.this.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                }
            }
        }
    }

    private final class AccountManagerInternalImpl extends AccountManagerInternal {
        @GuardedBy("mLock")
        private AccountManagerBackupHelper mBackupHelper;
        private final Object mLock;

        /* synthetic */ AccountManagerInternalImpl(AccountManagerService this$0, AccountManagerInternalImpl accountManagerInternalImpl) {
            this();
        }

        private AccountManagerInternalImpl() {
            this.mLock = new Object();
        }

        public void requestAccountAccess(Account account, String packageName, int userId, RemoteCallback callback) {
            if (account == null) {
                Slog.w(AccountManagerService.TAG, "account cannot be null");
            } else if (packageName == null) {
                Slog.w(AccountManagerService.TAG, "packageName cannot be null");
            } else if (userId < 0) {
                Slog.w(AccountManagerService.TAG, "user id must be concrete");
            } else if (callback == null) {
                Slog.w(AccountManagerService.TAG, "callback cannot be null");
            } else if (AccountManagerService.this.hasAccountAccess(account, packageName, new UserHandle(userId))) {
                Bundle result = new Bundle();
                result.putBoolean("booleanResult", true);
                callback.sendResult(result);
            } else {
                try {
                    UserAccounts userAccounts;
                    Intent intent = AccountManagerService.this.newRequestAccountAccessIntent(account, packageName, AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, userId), callback);
                    synchronized (AccountManagerService.this.mUsers) {
                        userAccounts = (UserAccounts) AccountManagerService.this.mUsers.get(userId);
                    }
                    AccountManagerService.this.doNotification(userAccounts, account, null, intent, packageName, userId);
                } catch (NameNotFoundException e) {
                    Slog.e(AccountManagerService.TAG, "Unknown package " + packageName);
                }
            }
        }

        public void addOnAppPermissionChangeListener(OnAppPermissionChangeListener listener) {
            AccountManagerService.this.mAppPermissionChangeListeners.add(listener);
        }

        public boolean hasAccountAccess(Account account, int uid) {
            return AccountManagerService.this.hasAccountAccess(account, null, uid);
        }

        public byte[] backupAccountAccessPermissions(int userId) {
            byte[] backupAccountAccessPermissions;
            synchronized (this.mLock) {
                if (this.mBackupHelper == null) {
                    this.mBackupHelper = new AccountManagerBackupHelper(AccountManagerService.this, this);
                }
                backupAccountAccessPermissions = this.mBackupHelper.backupAccountAccessPermissions(userId);
            }
            return backupAccountAccessPermissions;
        }

        public void restoreAccountAccessPermissions(byte[] data, int userId) {
            synchronized (this.mLock) {
                if (this.mBackupHelper == null) {
                    this.mBackupHelper = new AccountManagerBackupHelper(AccountManagerService.this, this);
                }
                this.mBackupHelper.restoreAccountAccessPermissions(data, userId);
            }
        }
    }

    static class CeDatabaseHelper extends SQLiteOpenHelper {
        public CeDatabaseHelper(Context context, String ceDatabaseName) {
            super(context, ceDatabaseName, null, 10);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(AccountManagerService.TAG, "Creating CE database " + getDatabaseName());
            db.execSQL("CREATE TABLE accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, password TEXT, UNIQUE(name,type))");
            db.execSQL("CREATE TABLE authtokens (  _id INTEGER PRIMARY KEY AUTOINCREMENT,  accounts_id INTEGER NOT NULL, type TEXT NOT NULL,  authtoken TEXT,  UNIQUE (accounts_id,type))");
            db.execSQL("CREATE TABLE extras ( _id INTEGER PRIMARY KEY AUTOINCREMENT, accounts_id INTEGER, key TEXT NOT NULL, value TEXT, UNIQUE(accounts_id,key))");
            createAccountsDeletionTrigger(db);
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM authtokens     WHERE accounts_id=OLD._id ;   DELETE FROM extras     WHERE accounts_id=OLD._id ; END");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(AccountManagerService.TAG, "Upgrade CE from version " + oldVersion + " to version " + newVersion);
            if (oldVersion == 9) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "onUpgrade upgrading to v10");
                }
                db.execSQL("DROP TABLE IF EXISTS meta");
                db.execSQL("DROP TABLE IF EXISTS shared_accounts");
                db.execSQL("DROP TRIGGER IF EXISTS accountsDelete");
                createAccountsDeletionTrigger(db);
                db.execSQL("DROP TABLE IF EXISTS grants");
                db.execSQL("DROP TABLE IF EXISTS " + DebugDbHelper.TABLE_DEBUG);
                oldVersion++;
            }
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts_ce.db");
            }
        }

        static String findAccountPasswordByNameAndType(SQLiteDatabase db, String name, String type) {
            String str = AccountManagerService.CE_TABLE_ACCOUNTS;
            String[] strArr = new String[1];
            strArr[0] = AccountManagerService.ACCOUNTS_PASSWORD;
            String[] strArr2 = new String[2];
            strArr2[0] = name;
            strArr2[1] = type;
            Cursor cursor = db.query(str, strArr, "name=? AND type=?", strArr2, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    String string = cursor.getString(0);
                    return string;
                }
                cursor.close();
                return null;
            } finally {
                cursor.close();
            }
        }

        static List<Account> findCeAccountsNotInDe(SQLiteDatabase db) {
            Cursor cursor = db.rawQuery("SELECT name,type FROM ceDb.accounts WHERE NOT EXISTS  (SELECT _id FROM accounts WHERE _id=ceDb.accounts._id )", null);
            try {
                List<Account> accounts = new ArrayList(cursor.getCount());
                while (cursor.moveToNext()) {
                    accounts.add(new Account(cursor.getString(0), cursor.getString(1)));
                }
                return accounts;
            } finally {
                cursor.close();
            }
        }

        static CeDatabaseHelper create(Context context, int userId, File preNDatabaseFile, File ceDatabaseFile) {
            boolean newDbExists = ceDatabaseFile.exists();
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "CeDatabaseHelper.create userId=" + userId + " oldDbExists=" + preNDatabaseFile.exists() + " newDbExists=" + newDbExists);
            }
            boolean removeOldDb = false;
            if (!newDbExists && preNDatabaseFile.exists()) {
                removeOldDb = migratePreNDbToCe(preNDatabaseFile, ceDatabaseFile);
            }
            CeDatabaseHelper ceHelper = new CeDatabaseHelper(context, ceDatabaseFile.getPath());
            ceHelper.getWritableDatabase();
            ceHelper.close();
            if (removeOldDb) {
                Slog.i(AccountManagerService.TAG, "Migration complete - removing pre-N db " + preNDatabaseFile);
                if (!SQLiteDatabase.deleteDatabase(preNDatabaseFile)) {
                    Slog.e(AccountManagerService.TAG, "Cannot remove pre-N db " + preNDatabaseFile);
                }
            }
            return ceHelper;
        }

        private static boolean migratePreNDbToCe(File oldDbFile, File ceDbFile) {
            Slog.i(AccountManagerService.TAG, "Moving pre-N DB " + oldDbFile + " to CE " + ceDbFile);
            try {
                FileUtils.copyFileOrThrow(oldDbFile, ceDbFile);
                return true;
            } catch (IOException e) {
                Slog.e(AccountManagerService.TAG, "Cannot copy file to " + ceDbFile + " from " + oldDbFile, e);
                AccountManagerService.deleteDbFileWarnIfFailed(ceDbFile);
                return false;
            }
        }
    }

    static class DeDatabaseHelper extends SQLiteOpenHelper {
        private volatile boolean mCeAttached;
        private final int mUserId;

        private DeDatabaseHelper(Context context, int userId, String deDatabaseName) {
            super(context, deDatabaseName, null, 1);
            this.mUserId = userId;
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(AccountManagerService.TAG, "Creating DE database for user " + this.mUserId);
            db.execSQL("CREATE TABLE accounts ( _id INTEGER PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, previous_name TEXT, last_password_entry_time_millis_epoch INTEGER DEFAULT 0, UNIQUE(name,type))");
            db.execSQL("CREATE TABLE meta ( key TEXT PRIMARY KEY NOT NULL, value TEXT)");
            createGrantsTable(db);
            createSharedAccountsTable(db);
            createAccountsDeletionTrigger(db);
            DebugDbHelper.createDebugTable(db);
        }

        private void createSharedAccountsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE shared_accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, UNIQUE(name,type))");
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM grants     WHERE accounts_id=OLD._id ; END");
        }

        private void createGrantsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE grants (  accounts_id INTEGER NOT NULL, auth_token_type STRING NOT NULL,  uid INTEGER NOT NULL,  UNIQUE (accounts_id,auth_token_type,uid))");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(AccountManagerService.TAG, "upgrade from version " + oldVersion + " to version " + newVersion);
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void attachCeDatabase(File ceDbFile) {
            getWritableDatabase().execSQL("ATTACH DATABASE '" + ceDbFile.getPath() + "' AS ceDb");
            this.mCeAttached = true;
        }

        public boolean isCeDatabaseAttached() {
            return this.mCeAttached;
        }

        public SQLiteDatabase getReadableDatabaseUserIsUnlocked() {
            if (!this.mCeAttached) {
                Log.wtf(AccountManagerService.TAG, "getReadableDatabaseUserIsUnlocked called while user " + this.mUserId + " is still locked. CE database is not yet available.", new Throwable());
            }
            return super.getReadableDatabase();
        }

        public SQLiteDatabase getWritableDatabaseUserIsUnlocked() {
            if (!this.mCeAttached) {
                Log.wtf(AccountManagerService.TAG, "getWritableDatabaseUserIsUnlocked called while user " + this.mUserId + " is still locked. CE database is not yet available.", new Throwable());
            }
            return super.getWritableDatabase();
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts_de.db");
            }
        }

        private void migratePreNDbToDe(File preNDbFile) {
            Log.i(AccountManagerService.TAG, "Migrate pre-N database to DE preNDbFile=" + preNDbFile);
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("ATTACH DATABASE '" + preNDbFile.getPath() + "' AS preNDb");
            db.beginTransaction();
            db.execSQL("INSERT INTO accounts(_id,name,type, previous_name, last_password_entry_time_millis_epoch) SELECT _id,name,type, previous_name, last_password_entry_time_millis_epoch FROM preNDb.accounts");
            db.execSQL("INSERT INTO shared_accounts(_id,name,type) SELECT _id,name,type FROM preNDb.shared_accounts");
            db.execSQL("INSERT INTO " + DebugDbHelper.TABLE_DEBUG + "(" + "_id" + "," + DebugDbHelper.ACTION_TYPE + "," + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.CALLER_UID + "," + DebugDbHelper.TABLE_NAME + "," + DebugDbHelper.KEY + ") " + "SELECT " + "_id" + "," + DebugDbHelper.ACTION_TYPE + "," + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.CALLER_UID + "," + DebugDbHelper.TABLE_NAME + "," + DebugDbHelper.KEY + " FROM preNDb." + DebugDbHelper.TABLE_DEBUG);
            db.execSQL("INSERT INTO grants(accounts_id,auth_token_type,uid) SELECT accounts_id,auth_token_type,uid FROM preNDb.grants");
            db.execSQL("INSERT INTO meta(key,value) SELECT key,value FROM preNDb.meta");
            db.setTransactionSuccessful();
            db.endTransaction();
            db.execSQL("DETACH DATABASE preNDb");
        }

        static DeDatabaseHelper create(Context context, int userId, File preNDatabaseFile, File deDatabaseFile) {
            boolean newDbExists = deDatabaseFile.exists();
            DeDatabaseHelper deDatabaseHelper = new DeDatabaseHelper(context, userId, deDatabaseFile.getPath());
            if (!newDbExists && preNDatabaseFile.exists()) {
                PreNDatabaseHelper preNDatabaseHelper = new PreNDatabaseHelper(context, userId, preNDatabaseFile.getPath());
                preNDatabaseHelper.getWritableDatabase();
                preNDatabaseHelper.close();
                deDatabaseHelper.migratePreNDbToDe(preNDatabaseFile);
            }
            return deDatabaseHelper;
        }
    }

    private static class DebugDbHelper {
        private static String ACTION_ACCOUNT_ADD;
        private static String ACTION_ACCOUNT_REMOVE;
        private static String ACTION_ACCOUNT_REMOVE_DE;
        private static String ACTION_ACCOUNT_RENAME;
        private static String ACTION_AUTHENTICATOR_REMOVE;
        private static String ACTION_CALLED_ACCOUNT_ADD;
        private static String ACTION_CALLED_ACCOUNT_REMOVE;
        private static String ACTION_CALLED_ACCOUNT_SESSION_FINISH;
        private static String ACTION_CALLED_START_ACCOUNT_ADD;
        private static String ACTION_CLEAR_PASSWORD;
        private static String ACTION_SET_PASSWORD;
        private static String ACTION_SYNC_DE_CE_ACCOUNTS;
        private static String ACTION_TYPE;
        private static String CALLER_UID;
        private static String KEY;
        private static String TABLE_DEBUG;
        private static String TABLE_NAME;
        private static String TIMESTAMP;
        private static SimpleDateFormat dateFromat;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void");
        }

        private DebugDbHelper() {
        }

        private static void createDebugTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_DEBUG + " ( " + "_id" + " INTEGER," + ACTION_TYPE + " TEXT NOT NULL, " + TIMESTAMP + " DATETIME," + CALLER_UID + " INTEGER NOT NULL," + TABLE_NAME + " TEXT NOT NULL," + KEY + " INTEGER PRIMARY KEY)");
            db.execSQL("CREATE INDEX timestamp_index ON " + TABLE_DEBUG + " (" + TIMESTAMP + ")");
        }
    }

    private class GetAccountsByTypeAndFeatureSession extends Session {
        private volatile Account[] mAccountsOfType;
        private volatile ArrayList<Account> mAccountsWithFeatures;
        private final int mCallingUid;
        private volatile int mCurrentAccount;
        private final String[] mFeatures;

        public GetAccountsByTypeAndFeatureSession(UserAccounts accounts, IAccountManagerResponse response, String type, String[] features, int callingUid) {
            super(AccountManagerService.this, accounts, response, type, false, true, null, false);
            this.mAccountsOfType = null;
            this.mAccountsWithFeatures = null;
            this.mCurrentAccount = 0;
            this.mCallingUid = callingUid;
            this.mFeatures = features;
        }

        public void run() throws RemoteException {
            synchronized (this.mAccounts.cacheLock) {
                this.mAccountsOfType = AccountManagerService.this.getAccountsFromCacheLocked(this.mAccounts, this.mAccountType, this.mCallingUid, null);
            }
            this.mAccountsWithFeatures = new ArrayList(this.mAccountsOfType.length);
            this.mCurrentAccount = 0;
            checkAccount();
        }

        public void checkAccount() {
            if (this.mCurrentAccount >= this.mAccountsOfType.length) {
                sendResult();
                return;
            }
            IAccountAuthenticator accountAuthenticator = this.mAuthenticator;
            if (accountAuthenticator == null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "checkAccount: aborting session since we are no longer connected to the authenticator, " + toDebugString());
                }
                return;
            }
            try {
                accountAuthenticator.hasFeatures(this, this.mAccountsOfType[this.mCurrentAccount], this.mFeatures);
            } catch (RemoteException e) {
                onError(1, "remote exception");
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            if (result == null) {
                onError(5, "null bundle");
                return;
            }
            if (result.getBoolean("booleanResult", false)) {
                this.mAccountsWithFeatures.add(this.mAccountsOfType[this.mCurrentAccount]);
            }
            this.mCurrentAccount++;
            checkAccount();
        }

        public void sendResult() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    Account[] accounts = new Account[this.mAccountsWithFeatures.size()];
                    for (int i = 0; i < accounts.length; i++) {
                        accounts[i] = (Account) this.mAccountsWithFeatures.get(i);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle result = new Bundle();
                    result.putParcelableArray(AccountManagerService.TABLE_ACCOUNTS, accounts);
                    response.onResult(result);
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                    }
                }
            }
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", getAccountsByTypeAndFeatures").append(", ");
            if (this.mFeatures != null) {
                str = TextUtils.join(",", this.mFeatures);
            }
            return append.append(str).toString();
        }
    }

    public static class Lifecycle extends SystemService {
        private AccountManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new AccountManagerService(getContext());
            publishBinderService("account", this.mService);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    msg.obj.onTimedOut();
                    return;
                case 4:
                    AccountManagerService.this.copyAccountToUser(null, (Account) msg.obj, msg.arg1, msg.arg2);
                    return;
                default:
                    throw new IllegalStateException("unhandled message: " + msg.what);
            }
        }
    }

    static class PreNDatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;
        private final int mUserId;

        public PreNDatabaseHelper(Context context, int userId, String preNDatabaseName) {
            super(context, preNDatabaseName, null, 9);
            this.mContext = context;
            this.mUserId = userId;
        }

        public void onCreate(SQLiteDatabase db) {
            throw new IllegalStateException("Legacy database cannot be created - only upgraded!");
        }

        private void createSharedAccountsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE shared_accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, UNIQUE(name,type))");
        }

        private void addLastSuccessfullAuthenticatedTimeColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN last_password_entry_time_millis_epoch DEFAULT 0");
        }

        private void addOldAccountNameColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN previous_name");
        }

        private void addDebugTable(SQLiteDatabase db) {
            DebugDbHelper.createDebugTable(db);
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM authtokens     WHERE accounts_id=OLD._id ;   DELETE FROM extras     WHERE accounts_id=OLD._id ;   DELETE FROM grants     WHERE accounts_id=OLD._id ; END");
        }

        private void createGrantsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE grants (  accounts_id INTEGER NOT NULL, auth_token_type STRING NOT NULL,  uid INTEGER NOT NULL,  UNIQUE (accounts_id,auth_token_type,uid))");
        }

        private void populateMetaTableWithAuthTypeAndUID(SQLiteDatabase db, Map<String, Integer> authTypeAndUIDMap) {
            for (Entry<String, Integer> entry : authTypeAndUIDMap.entrySet()) {
                ContentValues values = new ContentValues();
                values.put("key", AccountManagerService.META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + ((String) entry.getKey()));
                values.put("value", (Integer) entry.getValue());
                db.insert(AccountManagerService.TABLE_META, null, values);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(AccountManagerService.TAG, "upgrade from version " + oldVersion + " to version " + newVersion);
            if (oldVersion == 1) {
                oldVersion++;
            }
            if (oldVersion == 2) {
                createGrantsTable(db);
                db.execSQL("DROP TRIGGER accountsDelete");
                createAccountsDeletionTrigger(db);
                oldVersion++;
            }
            if (oldVersion == 3) {
                db.execSQL("UPDATE accounts SET type = 'com.google' WHERE type == 'com.google.GAIA'");
                oldVersion++;
            }
            if (oldVersion == 4) {
                createSharedAccountsTable(db);
                oldVersion++;
            }
            if (oldVersion == 5) {
                addOldAccountNameColumn(db);
                oldVersion++;
            }
            if (oldVersion == 6) {
                addLastSuccessfullAuthenticatedTimeColumn(db);
                oldVersion++;
            }
            if (oldVersion == 7) {
                addDebugTable(db);
                oldVersion++;
            }
            if (oldVersion == 8) {
                populateMetaTableWithAuthTypeAndUID(db, AccountManagerService.getAuthenticatorTypeAndUIDForUser(this.mContext, this.mUserId));
                oldVersion++;
            }
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts.db");
            }
        }
    }

    private class RemoveAccountSession extends Session {
        final Account mAccount;

        public RemoveAccountSession(UserAccounts accounts, IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
            super(AccountManagerService.this, accounts, response, account.type, expectActivityLaunch, true, account.name, false);
            this.mAccount = account;
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", removeAccount" + ", account " + this.mAccount;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.getAccountRemovalAllowed(this, this.mAccount);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (!(result == null || !result.containsKey("booleanResult") || result.containsKey("intent"))) {
                boolean removalAllowed = result.getBoolean("booleanResult");
                if (removalAllowed) {
                    AccountManagerService.this.removeAccountInternal(this.mAccounts, this.mAccount, getCallingUid());
                }
                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle result2 = new Bundle();
                    result2.putBoolean("booleanResult", removalAllowed);
                    try {
                        response.onResult(result2);
                    } catch (RemoteException e) {
                    }
                }
            }
            super.onResult(result);
        }
    }

    private class TestFeaturesSession extends Session {
        private final Account mAccount;
        private final String[] mFeatures;

        public TestFeaturesSession(UserAccounts accounts, IAccountManagerResponse response, Account account, String[] features) {
            super(AccountManagerService.this, accounts, response, account.type, false, true, account.name, false);
            this.mFeatures = features;
            this.mAccount = account;
        }

        public void run() throws RemoteException {
            try {
                this.mAuthenticator.hasFeatures(this, this.mAccount, this.mFeatures);
            } catch (RemoteException e) {
                onError(1, "remote exception");
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                if (result == null) {
                    try {
                        response.onError(5, "null bundle");
                    } catch (RemoteException e) {
                        if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                            Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                        }
                    }
                } else {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle newResult = new Bundle();
                    newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                    response.onResult(newResult);
                }
            }
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", hasFeatures").append(", ").append(this.mAccount).append(", ");
            if (this.mFeatures != null) {
                str = TextUtils.join(",", this.mFeatures);
            }
            return append.append(str).toString();
        }
    }

    static class UserAccounts {
        final HashMap<String, Account[]> accountCache;
        private final TokenCache accountTokenCaches;
        private final HashMap<Account, HashMap<String, String>> authTokenCache;
        final Object cacheLock;
        private final HashMap<Pair<Pair<Account, String>, Integer>, Integer> credentialsPermissionNotificationIds;
        private int debugDbInsertionPoint;
        final DeDatabaseHelper openHelper;
        private final HashMap<Account, AtomicReference<String>> previousNameCache;
        private final HashMap<Account, Integer> signinRequiredNotificationIds;
        private SQLiteStatement statementForLogging;
        private final HashMap<Account, HashMap<String, String>> userDataCache;
        private final int userId;

        UserAccounts(Context context, int userId, File preNDbFile, File deDbFile) {
            this.credentialsPermissionNotificationIds = new HashMap();
            this.signinRequiredNotificationIds = new HashMap();
            this.cacheLock = new Object();
            this.accountCache = new LinkedHashMap();
            this.userDataCache = new HashMap();
            this.authTokenCache = new HashMap();
            this.accountTokenCaches = new TokenCache();
            this.previousNameCache = new HashMap();
            this.debugDbInsertionPoint = -1;
            this.userId = userId;
            synchronized (this.cacheLock) {
                this.openHelper = DeDatabaseHelper.create(context, userId, preNDbFile, deDbFile);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.accounts.AccountManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.accounts.AccountManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.<clinit>():void");
    }

    public static AccountManagerService getSingleton() {
        return (AccountManagerService) sThis.get();
    }

    public AccountManagerService(Context context) {
        this(context, context.getPackageManager(), new AccountAuthenticatorCache(context));
    }

    public AccountManagerService(Context context, PackageManager packageManager, IAccountAuthenticatorCache authenticatorCache) {
        this.mSessions = new LinkedHashMap();
        this.mNotificationIds = new AtomicInteger(1);
        this.mUsers = new SparseArray();
        this.mLocalUnlockedUsers = new SparseBooleanArray();
        this.mAppPermissionChangeListeners = new CopyOnWriteArrayList();
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mMessageHandler = new MessageHandler(FgThread.get().getLooper());
        this.mAuthenticatorCache = authenticatorCache;
        this.mAuthenticatorCache.setListener(this, null);
        sThis.set(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context1, Intent intent) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    new Thread(new Runnable() {
                        public void run() {
                            AccountManagerService.this.purgeOldGrantsAll();
                        }
                    }).start();
                }
            }
        }, intentFilter);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    AccountManagerService.this.onUserRemoved(intent);
                }
            }
        }, UserHandle.ALL, userFilter, null, null);
        LocalServices.addService(AccountManagerInternal.class, new AccountManagerInternalImpl(this, null));
        new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }
        }.register(this.mContext, this.mMessageHandler.getLooper(), UserHandle.ALL, true);
        this.mAppOpsManager.startWatchingMode(62, null, new OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                long identity;
                try {
                    int uid = AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, ActivityManager.getCurrentUser());
                    if (AccountManagerService.this.mAppOpsManager.checkOpNoThrow(62, uid, packageName) == 0) {
                        identity = Binder.clearCallingIdentity();
                        AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(packageName, uid, true);
                        Binder.restoreCallingIdentity(identity);
                    }
                } catch (NameNotFoundException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        });
        this.mPackageManager.addOnPermissionsChangeListener(new -void__init__android_content_Context_context_android_content_pm_PackageManager_packageManager_com_android_server_accounts_IAccountAuthenticatorCache_authenticatorCache_LambdaImpl0());
    }

    /* renamed from: -com_android_server_accounts_AccountManagerService_lambda$1 */
    /* synthetic */ void m13-com_android_server_accounts_AccountManagerService_lambda$1(int uid) {
        Account[] accounts = null;
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames != null) {
            int userId = UserHandle.getUserId(uid);
            long identity = Binder.clearCallingIdentity();
            try {
                for (String packageName : packageNames) {
                    if (this.mContext.getPackageManager().checkPermission(OppoPermissionConstants.PERMISSION_GET_ACCOUNTS, packageName) == 0) {
                        if (accounts == null) {
                            accounts = getAccountsAsUser(null, userId, "android");
                            if (ArrayUtils.isEmpty(accounts)) {
                                return;
                            }
                        }
                        for (Account account : accounts) {
                            cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, true);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), "android")) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, checkAccess);
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(String packageName, int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), "android")) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, checkAccess);
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(Account account, int uid, boolean checkAccess) {
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, checkAccess);
            }
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(Account account, int uid, String packageName, boolean checkAccess) {
        if (!checkAccess || hasAccountAccess(account, packageName, UserHandle.getUserHandleForUid(uid))) {
            cancelNotification(getCredentialPermissionNotificationId(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid).intValue(), packageName, UserHandle.getUserHandleForUid(uid));
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Account Manager Crash", e);
            }
            throw e;
        }
    }

    private UserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(this.mContext);
        }
        return this.mUserManager;
    }

    public void validateAccounts(int userId) {
        validateAccountsInternal(getUserAccounts(userId), true);
    }

    private void validateAccountsInternal(UserAccounts accounts, boolean invalidateAuthenticatorCache) {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "validateAccountsInternal " + accounts.userId + " isCeDatabaseAttached=" + accounts.openHelper.isCeDatabaseAttached() + " userLocked=" + this.mLocalUnlockedUsers.get(accounts.userId));
        }
        if (invalidateAuthenticatorCache) {
            this.mAuthenticatorCache.invalidateCache(accounts.userId);
        }
        HashMap<String, Integer> knownAuth = getAuthenticatorTypeAndUIDForUser(this.mAuthenticatorCache, accounts.userId);
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        synchronized (accounts.cacheLock) {
            String accountType;
            ArrayList<String> accountNames;
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            boolean accountDeleted = false;
            String str = TABLE_META;
            String[] strArr = new String[2];
            strArr[0] = "key";
            strArr[1] = "value";
            String str2 = SELECTION_META_BY_AUTHENTICATOR_TYPE;
            String[] strArr2 = new String[1];
            strArr2[0] = "auth_uid_for_type:%";
            Cursor metaCursor = db.query(str, strArr, str2, strArr2, null, null, "key");
            HashSet<String> obsoleteAuthType = Sets.newHashSet();
            SparseBooleanArray sparseBooleanArray = null;
            while (metaCursor.moveToNext()) {
                try {
                    String type = TextUtils.split(metaCursor.getString(0), META_KEY_DELIMITER)[1];
                    String uid = metaCursor.getString(1);
                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(uid)) {
                        Slog.e(TAG, "Auth type empty: " + TextUtils.isEmpty(type) + ", uid empty: " + TextUtils.isEmpty(uid));
                    } else {
                        Integer knownUid = (Integer) knownAuth.get(type);
                        if (knownUid != null) {
                            if (uid.equals(knownUid.toString())) {
                                knownAuth.remove(type);
                            }
                        }
                        if (sparseBooleanArray == null) {
                            sparseBooleanArray = getUidsOfInstalledOrUpdatedPackagesAsUser(accounts.userId);
                        }
                        if (!sparseBooleanArray.get(Integer.parseInt(uid))) {
                            obsoleteAuthType.add(type);
                            String[] strArr3 = new String[2];
                            strArr3[0] = META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + type;
                            strArr3[1] = uid;
                            db.delete(TABLE_META, "key=? AND value=?", strArr3);
                        }
                    }
                } catch (Throwable th) {
                    metaCursor.close();
                }
            }
            metaCursor.close();
            for (Entry<String, Integer> entry : knownAuth.entrySet()) {
                ContentValues values = new ContentValues();
                values.put("key", META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + ((String) entry.getKey()));
                values.put("value", (Integer) entry.getValue());
                db.insertWithOnConflict(TABLE_META, null, values, 5);
            }
            str = TABLE_ACCOUNTS;
            strArr = new String[3];
            strArr[0] = "_id";
            strArr[1] = SoundModelContract.KEY_TYPE;
            strArr[2] = ACCOUNTS_NAME;
            Cursor cursor = db.query(str, strArr, null, null, null, null, "_id");
            accounts.accountCache.clear();
            HashMap<String, ArrayList<String>> accountNamesByType = new LinkedHashMap();
            while (cursor.moveToNext()) {
                long accountId = cursor.getLong(0);
                accountType = cursor.getString(1);
                String accountName = cursor.getString(2);
                if (obsoleteAuthType.contains(accountType)) {
                    Slog.w(TAG, "deleting account " + accountName + " because type " + accountType + "'s registered authenticator no longer exist.");
                    db.beginTransaction();
                    db.delete(TABLE_ACCOUNTS, "_id=" + accountId, null);
                    if (userUnlocked) {
                        db.delete(CE_TABLE_ACCOUNTS, "_id=" + accountId, null);
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    accountDeleted = true;
                    logRecord(db, DebugDbHelper.ACTION_AUTHENTICATOR_REMOVE, TABLE_ACCOUNTS, accountId, accounts);
                    Account account = new Account(accountName, accountType);
                    accounts.userDataCache.remove(account);
                    accounts.authTokenCache.remove(account);
                    accounts.accountTokenCaches.remove(account);
                } else {
                    accountNames = (ArrayList) accountNamesByType.get(accountType);
                    if (accountNames == null) {
                        accountNames = new ArrayList();
                        accountNamesByType.put(accountType, accountNames);
                    }
                    accountNames.add(accountName);
                }
            }
            for (Entry<String, ArrayList<String>> cur : accountNamesByType.entrySet()) {
                accountType = (String) cur.getKey();
                accountNames = (ArrayList) cur.getValue();
                Object accountsForType = new Account[accountNames.size()];
                for (int i = 0; i < accountsForType.length; i++) {
                    accountsForType[i] = new Account((String) accountNames.get(i), accountType, UUID.randomUUID().toString());
                }
                accounts.accountCache.put(accountType, accountsForType);
            }
            cursor.close();
            if (accountDeleted) {
                sendAccountsChangedBroadcast(accounts.userId);
            }
        }
    }

    private SparseBooleanArray getUidsOfInstalledOrUpdatedPackagesAsUser(int userId) {
        List<PackageInfo> pkgsWithData = this.mPackageManager.getInstalledPackagesAsUser(DumpState.DUMP_PREFERRED_XML, userId);
        SparseBooleanArray knownUids = new SparseBooleanArray(pkgsWithData.size());
        for (PackageInfo pkgInfo : pkgsWithData) {
            if (!(pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 8388608) == 0)) {
                knownUids.put(pkgInfo.applicationInfo.uid, true);
            }
        }
        return knownUids;
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(Context context, int userId) {
        return getAuthenticatorTypeAndUIDForUser(new AccountAuthenticatorCache(context), userId);
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(IAccountAuthenticatorCache authCache, int userId) {
        HashMap<String, Integer> knownAuth = new HashMap();
        for (ServiceInfo<AuthenticatorDescription> service : authCache.getAllServices(userId)) {
            knownAuth.put(((AuthenticatorDescription) service.type).type, Integer.valueOf(service.uid));
        }
        return knownAuth;
    }

    private UserAccounts getUserAccountsForCaller() {
        return getUserAccounts(UserHandle.getCallingUserId());
    }

    protected UserAccounts getUserAccounts(int userId) {
        UserAccounts accounts;
        synchronized (this.mUsers) {
            accounts = (UserAccounts) this.mUsers.get(userId);
            boolean validateAccounts = false;
            if (accounts == null) {
                accounts = new UserAccounts(this.mContext, userId, new File(getPreNDatabaseName(userId)), new File(getDeDatabaseName(userId)));
                initializeDebugDbSizeAndCompileSqlStatementForLogging(accounts.openHelper.getWritableDatabase(), accounts);
                this.mUsers.append(userId, accounts);
                purgeOldGrants(accounts);
                validateAccounts = true;
            }
            if (!accounts.openHelper.isCeDatabaseAttached() && this.mLocalUnlockedUsers.get(userId)) {
                Log.i(TAG, "User " + userId + " is unlocked - opening CE database");
                synchronized (accounts.cacheLock) {
                    File preNDatabaseFile = new File(getPreNDatabaseName(userId));
                    File ceDatabaseFile = new File(getCeDatabaseName(userId));
                    CeDatabaseHelper.create(this.mContext, userId, preNDatabaseFile, ceDatabaseFile);
                    accounts.openHelper.attachCeDatabase(ceDatabaseFile);
                }
                syncDeCeAccountsLocked(accounts);
            }
            if (validateAccounts) {
                validateAccountsInternal(accounts, true);
            }
        }
        return accounts;
    }

    private void syncDeCeAccountsLocked(UserAccounts accounts) {
        Preconditions.checkState(Thread.holdsLock(this.mUsers), "mUsers lock must be held");
        List<Account> accountsToRemove = CeDatabaseHelper.findCeAccountsNotInDe(accounts.openHelper.getReadableDatabaseUserIsUnlocked());
        if (!accountsToRemove.isEmpty()) {
            Slog.i(TAG, "Accounts " + accountsToRemove + " were previously deleted while user " + accounts.userId + " was locked. Removing accounts from CE tables");
            logRecord(accounts, DebugDbHelper.ACTION_SYNC_DE_CE_ACCOUNTS, TABLE_ACCOUNTS);
            for (Account account : accountsToRemove) {
                removeAccountInternal(accounts, account, Process.myUid());
            }
        }
    }

    private void purgeOldGrantsAll() {
        synchronized (this.mUsers) {
            for (int i = 0; i < this.mUsers.size(); i++) {
                purgeOldGrants((UserAccounts) this.mUsers.valueAt(i));
            }
        }
    }

    private void purgeOldGrants(UserAccounts accounts) {
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            String str = TABLE_GRANTS;
            String[] strArr = new String[1];
            strArr[0] = "uid";
            Cursor cursor = db.query(str, strArr, null, null, "uid", null, null);
            while (cursor.moveToNext()) {
                try {
                    int uid = cursor.getInt(0);
                    if (!(this.mPackageManager.getPackagesForUid(uid) != null)) {
                        Log.d(TAG, "deleting grants for UID " + uid + " because its package is no longer installed");
                        String[] strArr2 = new String[1];
                        strArr2[0] = Integer.toString(uid);
                        db.delete(TABLE_GRANTS, "uid=?", strArr2);
                    }
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            cursor.close();
        }
    }

    private void onUserRemoved(Intent intent) {
        int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
        if (userId >= 1) {
            UserAccounts accounts;
            synchronized (this.mUsers) {
                accounts = (UserAccounts) this.mUsers.get(userId);
                this.mUsers.remove(userId);
                boolean userUnlocked = this.mLocalUnlockedUsers.get(userId);
                this.mLocalUnlockedUsers.delete(userId);
            }
            if (accounts != null) {
                synchronized (accounts.cacheLock) {
                    accounts.openHelper.close();
                }
            }
            Log.i(TAG, "Removing database files for user " + userId);
            deleteDbFileWarnIfFailed(new File(getDeDatabaseName(userId)));
            if (!StorageManager.isFileEncryptedNativeOrEmulated() || userUnlocked) {
                File ceDb = new File(getCeDatabaseName(userId));
                if (ceDb.exists()) {
                    deleteDbFileWarnIfFailed(ceDb);
                }
            }
        }
    }

    private static void deleteDbFileWarnIfFailed(File dbFile) {
        if (!SQLiteDatabase.deleteDatabase(dbFile)) {
            Log.w(TAG, "Database at " + dbFile + " was not deleted successfully");
        }
    }

    void onUserUnlocked(Intent intent) {
        onUnlockUser(intent.getIntExtra("android.intent.extra.user_handle", -1));
    }

    void onUnlockUser(int userId) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "onUserUnlocked " + userId);
        }
        synchronized (this.mUsers) {
            this.mLocalUnlockedUsers.put(userId, true);
        }
        if (userId >= 1) {
            syncSharedAccounts(userId);
        }
    }

    private void syncSharedAccounts(int userId) {
        int i = 0;
        Account[] sharedAccounts = getSharedAccountsAsUser(userId);
        if (sharedAccounts != null && sharedAccounts.length != 0) {
            int parentUserId;
            Account[] accounts = getAccountsAsUser(null, userId, this.mContext.getOpPackageName());
            if (UserManager.isSplitSystemUser()) {
                parentUserId = getUserManager().getUserInfo(userId).restrictedProfileParentId;
            } else {
                parentUserId = 0;
            }
            if (parentUserId < 0) {
                Log.w(TAG, "User " + userId + " has shared accounts, but no parent user");
                return;
            }
            int length = sharedAccounts.length;
            while (i < length) {
                Account sa = sharedAccounts[i];
                if (!ArrayUtils.contains(accounts, sa)) {
                    copyAccountToUser(null, sa, parentUserId, userId);
                }
                i++;
            }
        }
    }

    public void onServiceChanged(AuthenticatorDescription desc, int userId, boolean removed) {
        validateAccountsInternal(getUserAccounts(userId), false);
    }

    public String getPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPassword: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                String readPasswordInternal = readPasswordInternal(getUserAccounts(userId), account);
                return readPasswordInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", objArr));
        }
    }

    private String readPasswordInternal(UserAccounts accounts, Account account) {
        if (account == null) {
            return null;
        }
        if (isLocalUnlockedUser(accounts.userId)) {
            String findAccountPasswordByNameAndType;
            synchronized (accounts.cacheLock) {
                findAccountPasswordByNameAndType = CeDatabaseHelper.findAccountPasswordByNameAndType(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account.name, account.type);
            }
            return findAccountPasswordByNameAndType;
        }
        Log.w(TAG, "Password is not available - user " + accounts.userId + " data is locked");
        return null;
    }

    public String getPreviousName(Account account) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPreviousName: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            String readPreviousNameInternal = readPreviousNameInternal(getUserAccounts(userId), account);
            return readPreviousNameInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private String readPreviousNameInternal(UserAccounts accounts, Account account) {
        Throwable th;
        if (account == null) {
            return null;
        }
        synchronized (accounts.cacheLock) {
            AtomicReference<String> previousNameRef = (AtomicReference) accounts.previousNameCache.get(account);
            String str;
            if (previousNameRef == null) {
                SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                str = TABLE_ACCOUNTS;
                String[] strArr = new String[1];
                strArr[0] = ACCOUNTS_PREVIOUS_NAME;
                String[] strArr2 = new String[2];
                strArr2[0] = account.name;
                strArr2[1] = account.type;
                Cursor cursor = db.query(str, strArr, "name=? AND type=?", strArr2, null, null, null);
                try {
                    if (cursor.moveToNext()) {
                        String previousName = cursor.getString(0);
                        AtomicReference<String> previousNameRef2 = new AtomicReference(previousName);
                        try {
                            accounts.previousNameCache.put(account, previousNameRef2);
                            cursor.close();
                            return previousName;
                        } catch (Throwable th2) {
                            th = th2;
                            previousNameRef = previousNameRef2;
                            cursor.close();
                            throw th;
                        }
                    }
                    cursor.close();
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    cursor.close();
                    throw th;
                }
            }
            str = (String) previousNameRef.get();
            return str;
        }
    }

    public String getUserData(Account account, String key) {
        Object[] objArr;
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            objArr = new Object[4];
            objArr[0] = account;
            objArr[1] = key;
            objArr[2] = Integer.valueOf(callingUid);
            objArr[3] = Integer.valueOf(Binder.getCallingPid());
            Log.v(TAG, String.format("getUserData( account: %s, key: %s, callerUid: %s, pid: %s", objArr));
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
                objArr = new Object[2];
                objArr[0] = Integer.valueOf(callingUid);
                objArr[1] = account.type;
                throw new SecurityException(String.format("uid %s cannot get user data for accounts of type: %s", objArr));
            } else if (isLocalUnlockedUser(userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(userId);
                    synchronized (accounts.cacheLock) {
                        if (accountExistsCacheLocked(accounts, account)) {
                            String readUserDataInternalLocked = readUserDataInternalLocked(accounts, account, key);
                            restoreCallingIdentity(identityToken);
                            return readUserDataInternalLocked;
                        }
                    }
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Log.w(TAG, "User " + userId + " data is locked. callingUid " + callingUid);
                return null;
            }
        }
        return null;
    }

    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthenticatorTypes: for user id " + userId + " caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (isCrossUser(callingUid, userId)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(UserHandle.getCallingUserId());
            objArr[1] = Integer.valueOf(userId);
            throw new SecurityException(String.format("User %s tying to get authenticator types for %s", objArr));
        }
        long identityToken = clearCallingIdentity();
        try {
            AuthenticatorDescription[] authenticatorTypesInternal = getAuthenticatorTypesInternal(userId);
            return authenticatorTypesInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private AuthenticatorDescription[] getAuthenticatorTypesInternal(int userId) {
        this.mAuthenticatorCache.updateServices(userId);
        Collection<ServiceInfo<AuthenticatorDescription>> authenticatorCollection = this.mAuthenticatorCache.getAllServices(userId);
        AuthenticatorDescription[] types = new AuthenticatorDescription[authenticatorCollection.size()];
        int i = 0;
        for (ServiceInfo<AuthenticatorDescription> authenticator : authenticatorCollection) {
            types[i] = (AuthenticatorDescription) authenticator.type;
            i++;
        }
        return types;
    }

    private boolean isCrossUser(int callingUid, int userId) {
        if (userId == UserHandle.getCallingUserId() || callingUid == Process.myUid() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return false;
        }
        return true;
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        Bundle.setDefusable(extras, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccountExplicitly: " + account + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                boolean addAccountInternal = addAccountInternal(getUserAccounts(userId), account, password, extras, callingUid);
                return addAccountInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", objArr));
        }
    }

    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        if (isCrossUser(Binder.getCallingUid(), -1)) {
            throw new SecurityException("Calling copyAccountToUser requires android.permission.INTERACT_ACROSS_USERS_FULL");
        }
        UserAccounts fromAccounts = getUserAccounts(userFrom);
        final UserAccounts toAccounts = getUserAccounts(userTo);
        if (fromAccounts == null || toAccounts == null) {
            if (response != null) {
                Bundle result = new Bundle();
                result.putBoolean("booleanResult", false);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to report error back to the client." + e);
                }
            }
            return;
        }
        Slog.d(TAG, "Copying account " + account.name + " from user " + userFrom + " to user " + userTo);
        long identityToken = clearCallingIdentity();
        try {
            final Account account2 = account;
            final IAccountManagerResponse iAccountManagerResponse = response;
            final int i = userFrom;
            new Session(this, fromAccounts, response, account.type, false, false, account.name, false) {
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account2.type;
                }

                public void run() throws RemoteException {
                    this.mAuthenticator.getAccountCredentialsForCloning(this, account2);
                }

                public void onResult(Bundle result) {
                    Bundle.setDefusable(result, true);
                    if (result == null || !result.getBoolean("booleanResult", false)) {
                        super.onResult(result);
                        return;
                    }
                    this.completeCloningAccount(iAccountManagerResponse, result, account2, toAccounts, i);
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean accountAuthenticated(Account account) {
        Object[] objArr;
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            objArr = new Object[2];
            objArr[0] = account;
            objArr[1] = Integer.valueOf(callingUid);
            Log.v(TAG, String.format("accountAuthenticated( account: %s, callerUid: %s)", objArr));
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot notify authentication for accounts of type: %s", objArr));
        } else if (!canUserModifyAccounts(userId, callingUid) || !canUserModifyAccountsForType(userId, account.type, callingUid)) {
            return false;
        } else {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                boolean updateLastAuthenticatedTime = updateLastAuthenticatedTime(account);
                return updateLastAuthenticatedTime;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    private boolean updateLastAuthenticatedTime(Account account) {
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.cacheLock) {
            ContentValues values = new ContentValues();
            values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, Long.valueOf(System.currentTimeMillis()));
            String[] strArr = new String[2];
            strArr[0] = account.name;
            strArr[1] = account.type;
            if (accounts.openHelper.getWritableDatabase().update(TABLE_ACCOUNTS, values, "name=? AND type=?", strArr) > 0) {
                return true;
            }
            return false;
        }
    }

    private void completeCloningAccount(IAccountManagerResponse response, Bundle accountCredentials, Account account, UserAccounts targetUser, int parentUserId) {
        Bundle.setDefusable(accountCredentials, true);
        long id = clearCallingIdentity();
        try {
            final Account account2 = account;
            final int i = parentUserId;
            final Bundle bundle = accountCredentials;
            new Session(this, targetUser, response, account.type, false, false, account.name, false) {
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account2.type;
                }

                public void run() throws RemoteException {
                    synchronized (this.getUserAccounts(i).cacheLock) {
                        for (Account acc : this.getAccounts(i, this.mContext.getOpPackageName())) {
                            if (acc.equals(account2)) {
                                this.mAuthenticator.addAccountFromCredentials(this, account2, bundle);
                                break;
                            }
                        }
                    }
                }

                public void onResult(Bundle result) {
                    Bundle.setDefusable(result, true);
                    super.onResult(result);
                }

                public void onError(int errorCode, String errorMessage) {
                    super.onError(errorCode, errorMessage);
                }
            }.bind();
        } finally {
            restoreCallingIdentity(id);
        }
    }

    /* JADX WARNING: Missing block: B:58:0x0200, code:
            if (getUserManager().getUserInfo(com.android.server.accounts.AccountManagerService.UserAccounts.-get8(r22)).canHaveProfile() == false) goto L_0x020d;
     */
    /* JADX WARNING: Missing block: B:59:0x0202, code:
            addAccountToLinkedRestrictedUsers(r23, com.android.server.accounts.AccountManagerService.UserAccounts.-get8(r22));
     */
    /* JADX WARNING: Missing block: B:60:0x020d, code:
            sendAccountsChangedBroadcast(com.android.server.accounts.AccountManagerService.UserAccounts.-get8(r22));
     */
    /* JADX WARNING: Missing block: B:61:0x0217, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean addAccountInternal(UserAccounts accounts, Account account, String password, Bundle extras, int callingUid) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            return false;
        }
        if (isLocalUnlockedUser(accounts.userId)) {
            synchronized (accounts.cacheLock) {
                SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                db.beginTransaction();
                try {
                    String[] strArr = new String[2];
                    strArr[0] = account.name;
                    strArr[1] = account.type;
                    if (DatabaseUtils.longForQuery(db, "select count(*) from ceDb.accounts WHERE name=? AND type=?", strArr) > 0) {
                        Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping since the account already exists");
                        db.endTransaction();
                        return false;
                    }
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_NAME, account.name);
                    values.put(SoundModelContract.KEY_TYPE, account.type);
                    values.put(ACCOUNTS_PASSWORD, password);
                    long accountId = db.insert(CE_TABLE_ACCOUNTS, ACCOUNTS_NAME, values);
                    if (accountId < 0) {
                        Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
                        db.endTransaction();
                        return false;
                    }
                    values = new ContentValues();
                    values.put("_id", Long.valueOf(accountId));
                    values.put(ACCOUNTS_NAME, account.name);
                    values.put(SoundModelContract.KEY_TYPE, account.type);
                    values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, Long.valueOf(System.currentTimeMillis()));
                    if (db.insert(TABLE_ACCOUNTS, ACCOUNTS_NAME, values) < 0) {
                        Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
                        db.endTransaction();
                        return false;
                    }
                    if (extras != null) {
                        for (String key : extras.keySet()) {
                            if (insertExtraLocked(db, accountId, key, extras.getString(key)) < 0) {
                                Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping since insertExtra failed for key " + key);
                                db.endTransaction();
                                return false;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                    logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_ACCOUNTS, accountId, accounts, callingUid);
                    insertAccountIntoCacheLocked(accounts, account);
                    db.endTransaction();
                } catch (Throwable th) {
                    db.endTransaction();
                }
            }
        } else {
            Log.w(TAG, "Account " + account + " cannot be added - user " + accounts.userId + " is locked. callingUid=" + callingUid);
            return false;
        }
    }

    private boolean isLocalUnlockedUser(int userId) {
        boolean z;
        synchronized (this.mUsers) {
            z = this.mLocalUnlockedUsers.get(userId);
        }
        return z;
    }

    private void addAccountToLinkedRestrictedUsers(Account account, int parentUserId) {
        for (UserInfo user : getUserManager().getUsers()) {
            if (user.isRestricted() && parentUserId == user.restrictedProfileParentId) {
                addSharedAccountAsUser(account, user.id);
                if (isLocalUnlockedUser(user.id)) {
                    this.mMessageHandler.sendMessage(this.mMessageHandler.obtainMessage(4, parentUserId, user.id, account));
                }
            }
        }
    }

    private long insertExtraLocked(SQLiteDatabase db, long accountId, String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("accounts_id", Long.valueOf(accountId));
        values.put("value", value);
        return db.insert(CE_TABLE_EXTRAS, "key", values);
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "hasFeatures: " + account + ", response " + response + ", features " + stringArrayToString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features == null) {
            throw new IllegalArgumentException("features is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            checkReadAccountsPermitted(callingUid, account.type, userId, opPackageName);
            long identityToken = clearCallingIdentity();
            try {
                new TestFeaturesSession(getUserAccounts(userId), response, account, features).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "renameAccount: " + accountToRename + " -> " + newName + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (accountToRename == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(accountToRename.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                Account resultingAccount = renameAccountInternal(getUserAccounts(userId), accountToRename, newName);
                Bundle result = new Bundle();
                result.putString("authAccount", resultingAccount.name);
                result.putString("accountType", resultingAccount.type);
                result.putString("accountAccessId", resultingAccount.getAccessId());
                response.onResult(result);
            } catch (RemoteException e) {
                Log.w(TAG, e.getMessage());
            } catch (Throwable th) {
                restoreCallingIdentity(identityToken);
            }
            restoreCallingIdentity(identityToken);
            return;
        }
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(callingUid);
        objArr[1] = accountToRename.type;
        throw new SecurityException(String.format("uid %s cannot rename accounts of type: %s", objArr));
    }

    private Account renameAccountInternal(UserAccounts accounts, Account accountToRename, String newName) {
        Account renamedAccount;
        cancelNotification(getSigninRequiredNotificationId(accounts, accountToRename).intValue(), new UserHandle(accounts.userId));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                if (accountToRename.equals(((Pair) pair.first).first)) {
                    cancelNotification(((Integer) accounts.credentialsPermissionNotificationIds.get(pair)).intValue(), new UserHandle(accounts.userId));
                }
            }
        }
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            db.beginTransaction();
            Account account = new Account(newName, accountToRename.type);
            try {
                long accountId = getAccountIdLocked(db, accountToRename);
                if (accountId >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_NAME, newName);
                    String[] argsAccountId = new String[1];
                    argsAccountId[0] = String.valueOf(accountId);
                    db.update(CE_TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                    values.put(ACCOUNTS_PREVIOUS_NAME, accountToRename.name);
                    db.update(TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                    db.setTransactionSuccessful();
                    logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_ACCOUNTS, accountId, accounts);
                }
                renamedAccount = insertAccountIntoCacheLocked(accounts, account);
                HashMap<String, String> tmpData = (HashMap) accounts.userDataCache.get(accountToRename);
                HashMap<String, String> tmpTokens = (HashMap) accounts.authTokenCache.get(accountToRename);
                removeAccountFromCacheLocked(accounts, accountToRename);
                accounts.userDataCache.put(renamedAccount, tmpData);
                accounts.authTokenCache.put(renamedAccount, tmpTokens);
                accounts.previousNameCache.put(renamedAccount, new AtomicReference(accountToRename.name));
                Account resultAccount = renamedAccount;
                int parentUserId = accounts.userId;
                if (canHaveProfile(parentUserId)) {
                    for (UserInfo user : getUserManager().getUsers(true)) {
                        if (user.isRestricted() && user.restrictedProfileParentId == parentUserId) {
                            renameSharedAccountAsUser(accountToRename, newName, user.id);
                        }
                    }
                }
                sendAccountsChangedBroadcast(accounts.userId);
            } finally {
                db.endTransaction();
            }
        }
        return renamedAccount;
    }

    private boolean canHaveProfile(int parentUserId) {
        UserInfo userInfo = getUserManager().getUserInfo(parentUserId);
        return userInfo != null ? userInfo.canHaveProfile() : false;
    }

    public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
        removeAccountAsUser(response, account, expectActivityLaunch, UserHandle.getCallingUserId());
    }

    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "removeAccount: " + account + ", response " + response + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (isCrossUser(callingUid, userId)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(UserHandle.getCallingUserId());
            objArr[1] = Integer.valueOf(userId);
            throw new SecurityException(String.format("User %s tying remove account for %s", objArr));
        } else {
            UserHandle user = UserHandle.of(userId);
            if (!isAccountManagedByCaller(account.type, callingUid, user.getIdentifier()) && !isSystemUid(callingUid)) {
                Object[] objArr2 = new Object[2];
                objArr2[0] = Integer.valueOf(callingUid);
                objArr2[1] = account.type;
                throw new SecurityException(String.format("uid %s cannot remove accounts of type: %s", objArr2));
            } else if (canUserModifyAccounts(userId, callingUid)) {
                if (canUserModifyAccountsForType(userId, account.type, callingUid)) {
                    long identityToken = clearCallingIdentity();
                    UserAccounts accounts = getUserAccounts(userId);
                    cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), user);
                    synchronized (accounts.credentialsPermissionNotificationIds) {
                        for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                            if (account.equals(((Pair) pair.first).first)) {
                                cancelNotification(((Integer) accounts.credentialsPermissionNotificationIds.get(pair)).intValue(), user);
                            }
                        }
                    }
                    SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                    logRecord(db, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS, getAccountIdLocked(db, account), accounts, callingUid);
                    try {
                        new RemoveAccountSession(accounts, response, account, expectActivityLaunch).bind();
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    try {
                        response.onError(101, "User cannot modify accounts of this type (policy).");
                    } catch (RemoteException e) {
                    }
                }
            } else {
                try {
                    response.onError(100, "User cannot modify accounts");
                } catch (RemoteException e2) {
                }
            }
        }
    }

    public boolean removeAccountExplicitly(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "removeAccountExplicitly: " + account + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        int userId = Binder.getCallingUserHandle().getIdentifier();
        if (account == null) {
            Log.e(TAG, "account is null");
            return false;
        } else if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            UserAccounts accounts = getUserAccountsForCaller();
            SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
            logRecord(db, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS, getAccountIdLocked(db, account), accounts, callingUid);
            long identityToken = clearCallingIdentity();
            try {
                boolean removeAccountInternal = removeAccountInternal(accounts, account, callingUid);
                return removeAccountInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", objArr));
        }
    }

    protected void removeAccountInternal(Account account) {
        removeAccountInternal(getUserAccountsForCaller(), account, getCallingUid());
    }

    private boolean removeAccountInternal(UserAccounts accounts, Account account, int callingUid) {
        boolean isChanged = false;
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        if (!userUnlocked) {
            Slog.i(TAG, "Removing account " + account + " while user " + accounts.userId + " is still locked. CE data will be removed later");
        }
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db;
            if (userUnlocked) {
                db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            } else {
                db = accounts.openHelper.getWritableDatabase();
            }
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    String[] strArr = new String[2];
                    strArr[0] = account.name;
                    strArr[1] = account.type;
                    db.delete(TABLE_ACCOUNTS, "name=? AND type=?", strArr);
                    if (userUnlocked) {
                        strArr = new String[2];
                        strArr[0] = account.name;
                        strArr[1] = account.type;
                        db.delete(CE_TABLE_ACCOUNTS, "name=? AND type=?", strArr);
                    }
                    db.setTransactionSuccessful();
                    isChanged = true;
                }
                db.endTransaction();
                if (isChanged) {
                    String action;
                    removeAccountFromCacheLocked(accounts, account);
                    sendAccountsChangedBroadcast(accounts.userId);
                    if (userUnlocked) {
                        action = DebugDbHelper.ACTION_ACCOUNT_REMOVE;
                    } else {
                        action = DebugDbHelper.ACTION_ACCOUNT_REMOVE_DE;
                    }
                    logRecord(db, action, TABLE_ACCOUNTS, accountId, accounts);
                }
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        long id = Binder.clearCallingIdentity();
        try {
            int parentUserId = accounts.userId;
            if (canHaveProfile(parentUserId)) {
                for (UserInfo user : getUserManager().getUsers(true)) {
                    if (user.isRestricted() && parentUserId == user.restrictedProfileParentId) {
                        removeSharedAccountAsUser(account, user.id, callingUid);
                    }
                }
            }
            Binder.restoreCallingIdentity(id);
            if (isChanged) {
                synchronized (accounts.credentialsPermissionNotificationIds) {
                    for (Pair<Pair<Account, String>, Integer> key : accounts.credentialsPermissionNotificationIds.keySet()) {
                        if (account.equals(((Pair) key.first).first) && "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE".equals(((Pair) key.first).second)) {
                            this.mMessageHandler.post(new AccountManagerService$-boolean_removeAccountInternal_com_android_server_accounts_AccountManagerService$UserAccounts_accounts_android_accounts_Account_account_int_callingUid_LambdaImpl0(this, account, ((Integer) key.second).intValue()));
                        }
                    }
                }
            }
            return isChanged;
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(id);
        }
    }

    /* renamed from: -com_android_server_accounts_AccountManagerService_lambda$2 */
    /* synthetic */ void m14-com_android_server_accounts_AccountManagerService_lambda$2(Account account, int uid) {
        cancelAccountAccessRequestNotificationIfNeeded(account, uid, false);
    }

    public void invalidateAuthToken(String accountType, String authToken) {
        int callerUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "invalidateAuthToken: accountType " + accountType + ", caller's uid " + callerUid + ", pid " + Binder.getCallingPid());
        }
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authToken == null) {
            throw new IllegalArgumentException("authToken is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                synchronized (accounts.cacheLock) {
                    SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                    db.beginTransaction();
                    try {
                        invalidateAuthTokenLocked(accounts, db, accountType, authToken);
                        invalidateCustomTokenLocked(accounts, accountType, authToken);
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    } catch (Throwable th) {
                        db.endTransaction();
                    }
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    private void invalidateCustomTokenLocked(UserAccounts accounts, String accountType, String authToken) {
        if (authToken != null && accountType != null) {
            accounts.accountTokenCaches.remove(accountType, authToken);
        }
    }

    private void invalidateAuthTokenLocked(UserAccounts accounts, SQLiteDatabase db, String accountType, String authToken) {
        if (authToken != null && accountType != null) {
            String[] strArr = new String[2];
            strArr[0] = authToken;
            strArr[1] = accountType;
            Cursor cursor = db.rawQuery("SELECT ceDb.authtokens._id, ceDb.accounts.name, ceDb.authtokens.type FROM ceDb.accounts JOIN ceDb.authtokens ON ceDb.accounts._id = ceDb.authtokens.accounts_id WHERE ceDb.authtokens.authtoken = ? AND ceDb.accounts.type = ?", strArr);
            while (cursor.moveToNext()) {
                try {
                    long authTokenId = cursor.getLong(0);
                    String accountName = cursor.getString(1);
                    String authTokenType = cursor.getString(2);
                    db.delete(CE_TABLE_AUTHTOKENS, "_id=" + authTokenId, null);
                    writeAuthTokenIntoCacheLocked(accounts, db, new Account(accountName, accountType), authTokenType, null);
                } finally {
                    cursor.close();
                }
            }
        }
    }

    private void saveCachedToken(UserAccounts accounts, Account account, String callerPkg, byte[] callerSigDigest, String tokenType, String token, long expiryMillis) {
        if (account != null && tokenType != null && callerPkg != null && callerSigDigest != null) {
            cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), UserHandle.of(accounts.userId));
            synchronized (accounts.cacheLock) {
                accounts.accountTokenCaches.put(account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
            }
        }
    }

    private boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type, String authToken) {
        if (account == null || type == null) {
            return false;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), UserHandle.of(accounts.userId));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId < 0) {
                    db.endTransaction();
                    return false;
                }
                String str = CE_TABLE_AUTHTOKENS;
                String str2 = "accounts_id=" + accountId + " AND " + SoundModelContract.KEY_TYPE + "=?";
                String[] strArr = new String[1];
                strArr[0] = type;
                db.delete(str, str2, strArr);
                ContentValues values = new ContentValues();
                values.put("accounts_id", Long.valueOf(accountId));
                values.put(SoundModelContract.KEY_TYPE, type);
                values.put(AUTHTOKENS_AUTHTOKEN, authToken);
                if (db.insert(CE_TABLE_AUTHTOKENS, AUTHTOKENS_AUTHTOKEN, values) >= 0) {
                    db.setTransactionSuccessful();
                    writeAuthTokenIntoCacheLocked(accounts, db, account, type, authToken);
                    db.endTransaction();
                    return true;
                }
                db.endTransaction();
                return false;
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "peekAuthToken: " + account + ", authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(callingUid);
                objArr[1] = account.type;
                throw new SecurityException(String.format("uid %s cannot peek the authtokens associated with accounts of type: %s", objArr));
            } else if (isLocalUnlockedUser(userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    String readAuthTokenInternal = readAuthTokenInternal(getUserAccounts(userId), account, authTokenType);
                    return readAuthTokenInternal;
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Log.w(TAG, "Authtoken not available - user " + userId + " data is locked. callingUid " + callingUid);
                return null;
            }
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken: " + account + ", authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(account.type, callingUid, userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    saveAuthTokenToDatabase(getUserAccounts(userId), account, authTokenType, authToken);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(callingUid);
                objArr[1] = account.type;
                throw new SecurityException(String.format("uid %s cannot set auth tokens associated with accounts of type: %s", objArr));
            }
        }
    }

    public void setPassword(Account account, String password) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken: " + account + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                setPasswordInternal(getUserAccounts(userId), account, password, callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot set secrets for accounts of type: %s", objArr));
        }
    }

    private void setPasswordInternal(UserAccounts accounts, Account account, String password, int callingUid) {
        if (account != null) {
            boolean isChanged = false;
            synchronized (accounts.cacheLock) {
                SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_PASSWORD, password);
                    long accountId = getAccountIdLocked(db, account);
                    if (accountId >= 0) {
                        String action;
                        String[] argsAccountId = new String[1];
                        argsAccountId[0] = String.valueOf(accountId);
                        db.update(CE_TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                        db.delete(CE_TABLE_AUTHTOKENS, "accounts_id=?", argsAccountId);
                        accounts.authTokenCache.remove(account);
                        accounts.accountTokenCaches.remove(account);
                        db.setTransactionSuccessful();
                        isChanged = true;
                        if (password == null || password.length() == 0) {
                            action = DebugDbHelper.ACTION_CLEAR_PASSWORD;
                        } else {
                            action = DebugDbHelper.ACTION_SET_PASSWORD;
                        }
                        logRecord(db, action, TABLE_ACCOUNTS, accountId, accounts, callingUid);
                    }
                    db.endTransaction();
                    if (isChanged) {
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                } catch (Throwable th) {
                    db.endTransaction();
                    if (isChanged) {
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                }
            }
        }
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Log.i(TAG, "the accounts changed, sending broadcast of " + ACCOUNTS_CHANGED_INTENT.getAction());
        this.mContext.sendBroadcastAsUser(ACCOUNTS_CHANGED_INTENT, new UserHandle(userId));
    }

    public void clearPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "clearPassword: " + account + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                setPasswordInternal(getUserAccounts(userId), account, null, callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = account.type;
            throw new SecurityException(String.format("uid %s cannot clear passwords for accounts of type: %s", objArr));
        }
    }

    public void setUserData(Account account, String key, String value) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setUserData: " + account + ", key " + key + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(account.type, callingUid, userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(userId);
                    synchronized (accounts.cacheLock) {
                        if (accountExistsCacheLocked(accounts, account)) {
                            setUserdataInternalLocked(accounts, account, key, value);
                            restoreCallingIdentity(identityToken);
                        }
                    }
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(callingUid);
                objArr[1] = account.type;
                throw new SecurityException(String.format("uid %s cannot set user data for accounts of type: %s", objArr));
            }
        }
    }

    private boolean accountExistsCacheLocked(UserAccounts accounts, Account account) {
        if (accounts.accountCache.containsKey(account.type)) {
            for (Account acc : (Account[]) accounts.accountCache.get(account.type)) {
                if (acc.name.equals(account.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setUserdataInternalLocked(UserAccounts accounts, Account account, String key, String value) {
        if (account != null && key != null) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    long extrasId = getExtrasIdLocked(db, accountId, key);
                    if (extrasId >= 0) {
                        ContentValues values = new ContentValues();
                        values.put("value", value);
                        if (1 != db.update(TABLE_EXTRAS, values, "_id=" + extrasId, null)) {
                            db.endTransaction();
                            return;
                        }
                    } else if (insertExtraLocked(db, accountId, key, value) < 0) {
                        db.endTransaction();
                        return;
                    }
                    writeUserDataIntoCacheLocked(accounts, db, account, key, value);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            } finally {
                db.endTransaction();
            }
        }
    }

    private void onResult(IAccountManagerResponse response, Bundle result) {
        if (result == null) {
            Log.e(TAG, "the result is unexpectedly null", new Exception());
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
        }
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) throws RemoteException {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int callingUid = getCallingUid();
            clearCallingIdentity();
            if (UserHandle.getAppId(callingUid) != 1000) {
                throw new SecurityException("can only call from system");
            }
            int userId = UserHandle.getUserId(callingUid);
            long identityToken = clearCallingIdentity();
            try {
                final String str = accountType;
                final String str2 = authTokenType;
                new Session(this, getUserAccounts(userId), response, accountType, false, false, null, false) {
                    protected String toDebugString(long now) {
                        return super.toDebugString(now) + ", getAuthTokenLabel" + ", " + str + ", authTokenType " + str2;
                    }

                    public void run() throws RemoteException {
                        this.mAuthenticator.getAuthTokenLabel(this, str2);
                    }

                    public void onResult(Bundle result) {
                        Bundle.setDefusable(result, true);
                        if (result != null) {
                            String label = result.getString("authTokenLabelKey");
                            Bundle bundle = new Bundle();
                            bundle.putString("authTokenLabelKey", label);
                            super.onResult(bundle);
                            return;
                        }
                        super.onResult(result);
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthToken: " + account + ", response " + response + ", authTokenType " + authTokenType + ", notifyOnAuthFailure " + notifyOnAuthFailure + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            try {
                Slog.w(TAG, "getAuthToken called with null account");
                response.onError(7, "account is null");
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to report error back to the client." + e);
            }
        } else if (authTokenType == null) {
            Slog.w(TAG, "getAuthToken called with null authTokenType");
            response.onError(7, "authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                ServiceInfo<AuthenticatorDescription> authenticatorInfo = this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(account.type), accounts.userId);
                final boolean customTokens = authenticatorInfo != null ? ((AuthenticatorDescription) authenticatorInfo.type).customTokens : false;
                final int callerUid = Binder.getCallingUid();
                final boolean permissionGranted = !customTokens ? permissionIsGranted(account, authTokenType, callerUid, userId) : true;
                String callerPkg = loginOptions.getString("androidPackageName");
                ident = Binder.clearCallingIdentity();
                try {
                    List<String> callerOwnedPackageNames = Arrays.asList(this.mPackageManager.getPackagesForUid(callerUid));
                    if (callerPkg == null || !callerOwnedPackageNames.contains(callerPkg)) {
                        Object[] objArr = new Object[2];
                        objArr[0] = Integer.valueOf(callerUid);
                        objArr[1] = callerPkg;
                        throw new SecurityException(String.format("Uid %s is attempting to illegally masquerade as package %s!", objArr));
                    }
                    loginOptions.putInt("callerUid", callerUid);
                    loginOptions.putInt("callerPid", Binder.getCallingPid());
                    if (notifyOnAuthFailure) {
                        loginOptions.putBoolean("notifyOnAuthFailure", true);
                    }
                    long identityToken = clearCallingIdentity();
                    try {
                        Bundle result;
                        byte[] callerPkgSigDigest = calculatePackageSignatureDigest(callerPkg);
                        if (!customTokens && permissionGranted) {
                            String authToken = readAuthTokenInternal(accounts, account, authTokenType);
                            if (authToken != null) {
                                result = new Bundle();
                                result.putString(AUTHTOKENS_AUTHTOKEN, authToken);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                return;
                            }
                        }
                        if (customTokens) {
                            String token = readCachedTokenInternal(accounts, account, authTokenType, callerPkg, callerPkgSigDigest);
                            if (token != null) {
                                if (Log.isLoggable(TAG, 2)) {
                                    Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                                }
                                result = new Bundle();
                                result.putString(AUTHTOKENS_AUTHTOKEN, token);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                restoreCallingIdentity(identityToken);
                                return;
                            }
                        }
                        final Bundle bundle = loginOptions;
                        final Account account2 = account;
                        final String str = authTokenType;
                        final boolean z = notifyOnAuthFailure;
                        final String str2 = callerPkg;
                        final byte[] bArr = callerPkgSigDigest;
                        final UserAccounts userAccounts = accounts;
                        new Session(this, accounts, response, account.type, expectActivityLaunch, false, account.name, false) {
                            protected String toDebugString(long now) {
                                if (bundle != null) {
                                    bundle.keySet();
                                }
                                return super.toDebugString(now) + ", getAuthToken" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle + ", notifyOnAuthFailure " + z;
                            }

                            public void run() throws RemoteException {
                                if (permissionGranted) {
                                    this.mAuthenticator.getAuthToken(this, account2, str, bundle);
                                } else {
                                    this.mAuthenticator.getAuthTokenLabel(this, str);
                                }
                            }

                            public void onResult(Bundle result) {
                                Bundle.setDefusable(result, true);
                                if (result != null) {
                                    Intent intent;
                                    if (result.containsKey("authTokenLabelKey")) {
                                        intent = this.newGrantCredentialsPermissionIntent(account2, null, callerUid, new AccountAuthenticatorResponse(this), str, true);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("intent", intent);
                                        onResult(bundle);
                                        return;
                                    }
                                    String authToken = result.getString(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                                    if (authToken != null) {
                                        String name = result.getString("authAccount");
                                        String type = result.getString("accountType");
                                        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                                            onError(5, "the type and name should not be empty");
                                            return;
                                        }
                                        Account resultAccount = new Account(name, type);
                                        if (!customTokens) {
                                            this.saveAuthTokenToDatabase(this.mAccounts, resultAccount, str, authToken);
                                        }
                                        long expiryMillis = result.getLong("android.accounts.expiry", 0);
                                        if (customTokens && expiryMillis > System.currentTimeMillis()) {
                                            this.saveCachedToken(this.mAccounts, account2, str2, bArr, str, authToken, expiryMillis);
                                        }
                                    }
                                    intent = (Intent) result.getParcelable("intent");
                                    if (!(intent == null || !z || customTokens)) {
                                        checkKeyIntent(Binder.getCallingUid(), intent);
                                        this.doNotification(this.mAccounts, account2, result.getString("authFailedMessage"), intent, "android", userAccounts.userId);
                                    }
                                }
                                super.onResult(result);
                            }
                        }.bind();
                        restoreCallingIdentity(identityToken);
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private byte[] calculatePackageSignatureDigest(String callerPkg) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
            for (Signature sig : this.mPackageManager.getPackageInfo(callerPkg, 64).signatures) {
                digester.update(sig.toByteArray());
            }
        } catch (NoSuchAlgorithmException x) {
            Log.wtf(TAG, "SHA-256 should be available", x);
            digester = null;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Could not find packageinfo for: " + callerPkg);
            digester = null;
        }
        if (digester == null) {
            return null;
        }
        return digester.digest();
    }

    private void createNoCredentialsPermissionNotification(Account account, Intent intent, String packageName, int userId) {
        int uid = intent.getIntExtra("uid", -1);
        String authTokenType = intent.getStringExtra("authTokenType");
        Context context = this.mContext;
        Object[] objArr = new Object[1];
        objArr[0] = account.name;
        String titleAndSubtitle = context.getString(17040503, objArr);
        int index = titleAndSubtitle.indexOf(10);
        String title = titleAndSubtitle;
        String subtitle = IElsaManager.EMPTY_PACKAGE;
        if (index > 0) {
            title = titleAndSubtitle.substring(0, index);
            subtitle = titleAndSubtitle.substring(index + 1);
        }
        UserHandle user = new UserHandle(userId);
        Context contextForUser = getContextForUser(user);
        Notification n = new Builder(contextForUser).setSmallIcon(17301642).setWhen(0).setColor(contextForUser.getColor(17170523)).setContentTitle(title).setContentText(subtitle).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, user)).build();
        installNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), n, packageName, user.getIdentifier());
    }

    private Intent newGrantCredentialsPermissionIntent(Account account, String packageName, int uid, AccountAuthenticatorResponse response, String authTokenType, boolean startInNewTask) {
        Intent intent = new Intent(this.mContext, GrantCredentialsPermissionActivity.class);
        if (startInNewTask) {
            intent.setFlags(268435456);
        }
        StringBuilder append = new StringBuilder().append(getCredentialPermissionNotificationId(account, authTokenType, uid));
        if (packageName == null) {
            packageName = IElsaManager.EMPTY_PACKAGE;
        }
        intent.addCategory(String.valueOf(append.append(packageName).toString()));
        intent.putExtra("account", account);
        intent.putExtra("authTokenType", authTokenType);
        intent.putExtra("response", response);
        intent.putExtra("uid", uid);
        return intent;
    }

    private Integer getCredentialPermissionNotificationId(Account account, String authTokenType, int uid) {
        Integer id;
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            Pair<Pair<Account, String>, Integer> key = new Pair(new Pair(account, authTokenType), Integer.valueOf(uid));
            id = (Integer) accounts.credentialsPermissionNotificationIds.get(key);
            if (id == null) {
                id = Integer.valueOf(this.mNotificationIds.incrementAndGet());
                accounts.credentialsPermissionNotificationIds.put(key, id);
            }
        }
        return id;
    }

    private Integer getSigninRequiredNotificationId(UserAccounts accounts, Account account) {
        Integer id;
        synchronized (accounts.signinRequiredNotificationIds) {
            id = (Integer) accounts.signinRequiredNotificationIds.get(account);
            if (id == null) {
                id = Integer.valueOf(this.mNotificationIds.incrementAndGet());
                accounts.signinRequiredNotificationIds.put(account, id);
            }
        }
        return id;
    }

    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getUserId(uid);
            if (!canUserModifyAccounts(userId, uid)) {
                try {
                    response.onError(100, "User is not allowed to add an account!");
                } catch (RemoteException e) {
                }
                showCantAddAccount(100, userId);
            } else if (canUserModifyAccountsForType(userId, accountType, uid)) {
                int pid = Binder.getCallingPid();
                final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
                options.putInt("callerUid", uid);
                options.putInt("callerPid", pid);
                int usrId = UserHandle.getCallingUserId();
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(usrId);
                    logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, uid);
                    final String str = authTokenType;
                    final String[] strArr = requiredFeatures;
                    final String str2 = accountType;
                    new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.addAccount(this, this.mAccountType, str, strArr, options);
                        }

                        protected String toDebugString(long now) {
                            String str = null;
                            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", addAccount").append(", accountType ").append(str2).append(", requiredFeatures ");
                            if (strArr != null) {
                                str = TextUtils.join(",", strArr);
                            }
                            return append.append(str).toString();
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                try {
                    response.onError(101, "User cannot modify accounts of this type (policy).");
                } catch (RemoteException e2) {
                }
                showCantAddAccount(101, userId);
            }
        }
    }

    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn, int userId) {
        Bundle.setDefusable(optionsIn, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (isCrossUser(callingUid, userId)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(UserHandle.getCallingUserId());
            objArr[1] = Integer.valueOf(userId);
            throw new SecurityException(String.format("User %s trying to add account for %s", objArr));
        } else if (!canUserModifyAccounts(userId, callingUid)) {
            try {
                response.onError(100, "User is not allowed to add an account!");
            } catch (RemoteException e) {
            }
            showCantAddAccount(100, userId);
        } else if (canUserModifyAccountsForType(userId, accountType, callingUid)) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, userId);
                final String str = authTokenType;
                final String[] strArr = requiredFeatures;
                final String str2 = accountType;
                new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.addAccount(this, this.mAccountType, str, strArr, options);
                    }

                    protected String toDebugString(long now) {
                        String str = null;
                        StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", addAccount").append(", accountType ").append(str2).append(", requiredFeatures ");
                        if (strArr != null) {
                            str = TextUtils.join(",", strArr);
                        }
                        return append.append(str).toString();
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            try {
                response.onError(101, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(101, userId);
        }
    }

    public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "startAddAccountSession: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int userId = UserHandle.getUserId(uid);
                if (!canUserModifyAccounts(userId, uid)) {
                    try {
                        response.onError(100, "User is not allowed to add an account!");
                    } catch (RemoteException e) {
                    }
                    showCantAddAccount(100, userId);
                    return;
                } else if (canUserModifyAccountsForType(userId, accountType, uid)) {
                    int pid = Binder.getCallingPid();
                    final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
                    options.putInt("callerUid", uid);
                    options.putInt("callerPid", pid);
                    String callerPkg = optionsIn.getString("androidPackageName");
                    String[] strArr = new String[1];
                    strArr[0] = "android.permission.GET_PASSWORD";
                    boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, strArr);
                    long identityToken = clearCallingIdentity();
                    try {
                        UserAccounts accounts = getUserAccounts(userId);
                        logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_START_ACCOUNT_ADD, TABLE_ACCOUNTS, uid);
                        final String str = authTokenType;
                        final String[] strArr2 = requiredFeatures;
                        final String str2 = accountType;
                        new StartAccountSession(this, accounts, response, accountType, expectActivityLaunch, null, false, true, isPasswordForwardingAllowed) {
                            public void run() throws RemoteException {
                                this.mAuthenticator.startAddAccountSession(this, this.mAccountType, str, strArr2, options);
                            }

                            protected String toDebugString(long now) {
                                String requiredFeaturesStr = TextUtils.join(",", strArr2);
                                StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", startAddAccountSession").append(", accountType ").append(str2).append(", requiredFeatures ");
                                if (strArr2 == null) {
                                    requiredFeaturesStr = null;
                                }
                                return append.append(requiredFeaturesStr).toString();
                            }
                        }.bind();
                        return;
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    try {
                        response.onError(101, "User cannot modify accounts of this type (policy).");
                    } catch (RemoteException e2) {
                    }
                    showCantAddAccount(101, userId);
                    return;
                }
            }
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(uid);
            throw new SecurityException(String.format("uid %s cannot stat add account session.", objArr));
        }
    }

    public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) {
        Bundle.setDefusable(sessionBundle, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "finishSession: response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", caller's user id " + UserHandle.getCallingUserId() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (sessionBundle == null || sessionBundle.size() == 0) {
            throw new IllegalArgumentException("sessionBundle is empty");
        } else if (isCrossUser(callingUid, userId)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(UserHandle.getCallingUserId());
            objArr[1] = Integer.valueOf(userId);
            throw new SecurityException(String.format("User %s trying to finish session for %s without cross user permission", objArr));
        } else if (!isSystemUid(callingUid)) {
            Object[] objArr2 = new Object[1];
            objArr2[0] = Integer.valueOf(callingUid);
            throw new SecurityException(String.format("uid %s cannot finish session because it's not system uid.", objArr2));
        } else if (canUserModifyAccounts(userId, callingUid)) {
            int pid = Binder.getCallingPid();
            try {
                final Bundle decryptedBundle = CryptoHelper.getInstance().decryptBundle(sessionBundle);
                if (decryptedBundle == null) {
                    sendErrorResponse(response, 8, "failed to decrypt session bundle");
                    return;
                }
                String accountType = decryptedBundle.getString("accountType");
                if (TextUtils.isEmpty(accountType)) {
                    sendErrorResponse(response, 7, "accountType is empty");
                    return;
                }
                if (appInfo != null) {
                    decryptedBundle.putAll(appInfo);
                }
                decryptedBundle.putInt("callerUid", callingUid);
                decryptedBundle.putInt("callerPid", pid);
                if (canUserModifyAccountsForType(userId, accountType, callingUid)) {
                    long identityToken = clearCallingIdentity();
                    try {
                        UserAccounts accounts = getUserAccounts(userId);
                        logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_SESSION_FINISH, TABLE_ACCOUNTS, callingUid);
                        final String str = accountType;
                        new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                            public void run() throws RemoteException {
                                this.mAuthenticator.finishSession(this, this.mAccountType, decryptedBundle);
                            }

                            protected String toDebugString(long now) {
                                return super.toDebugString(now) + ", finishSession" + ", accountType " + str;
                            }
                        }.bind();
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    sendErrorResponse(response, 101, "User cannot modify accounts of this type (policy).");
                    showCantAddAccount(101, userId);
                }
            } catch (Throwable e) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.v(TAG, "Failed to decrypt session bundle!", e);
                }
                sendErrorResponse(response, 8, "failed to decrypt session bundle");
            }
        } else {
            sendErrorResponse(response, 100, "User is not allowed to add an account!");
            showCantAddAccount(100, userId);
        }
    }

    private void showCantAddAccount(int errorCode, int userId) {
        Intent cantAddAccount = new Intent(this.mContext, CantAddAccountActivity.class);
        cantAddAccount.putExtra("android.accounts.extra.ERROR_CODE", errorCode);
        cantAddAccount.addFlags(268435456);
        long identityToken = clearCallingIdentity();
        try {
            this.mContext.startActivityAsUser(cantAddAccount, new UserHandle(userId));
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) {
        Bundle.setDefusable(options, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "confirmCredentials: " + account + ", response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (isCrossUser(callingUid, userId)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(UserHandle.getCallingUserId());
            objArr[1] = Integer.valueOf(userId);
            throw new SecurityException(String.format("User %s trying to confirm account credentials for %s", objArr));
        } else if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            long identityToken = clearCallingIdentity();
            try {
                final Account account2 = account;
                final Bundle bundle = options;
                new Session(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, true, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.confirmCredentials(this, account2, bundle);
                    }

                    protected String toDebugString(long now) {
                        return super.toDebugString(now) + ", confirmCredentials" + ", " + account2;
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "updateCredentials: " + account + ", response " + response + ", authTokenType " + authTokenType + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                final Account account2 = account;
                final String str = authTokenType;
                final Bundle bundle = loginOptions;
                new Session(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, false, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.updateCredentials(this, account2, str, bundle);
                    }

                    protected String toDebugString(long now) {
                        if (bundle != null) {
                            bundle.keySet();
                        }
                        return super.toDebugString(now) + ", updateCredentials" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle;
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "startUpdateCredentialsSession: " + account + ", response " + response + ", authTokenType " + authTokenType + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int userId = UserHandle.getCallingUserId();
                String callerPkg = loginOptions.getString("androidPackageName");
                String[] strArr = new String[1];
                strArr[0] = "android.permission.GET_PASSWORD";
                boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, strArr);
                long identityToken = clearCallingIdentity();
                try {
                    final Account account2 = account;
                    final String str = authTokenType;
                    final Bundle bundle = loginOptions;
                    new StartAccountSession(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, account.name, false, true, isPasswordForwardingAllowed) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.startUpdateCredentialsSession(this, account2, str, bundle);
                        }

                        protected String toDebugString(long now) {
                            if (bundle != null) {
                                bundle.keySet();
                            }
                            return super.toDebugString(now) + ", startUpdateCredentialsSession" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle;
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(uid);
                throw new SecurityException(String.format("uid %s cannot start update credentials session.", objArr));
            }
        }
    }

    public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account, String statusToken) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "isCredentialsUpdateSuggested: " + account + ", response " + response + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (TextUtils.isEmpty(statusToken)) {
            throw new IllegalArgumentException("status token is empty");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int usrId = UserHandle.getCallingUserId();
                long identityToken = clearCallingIdentity();
                try {
                    final Account account2 = account;
                    final String str = statusToken;
                    new Session(this, getUserAccounts(usrId), response, account.type, false, false, account.name, false) {
                        protected String toDebugString(long now) {
                            return super.toDebugString(now) + ", isCredentialsUpdateSuggested" + ", " + account2;
                        }

                        public void run() throws RemoteException {
                            this.mAuthenticator.isCredentialsUpdateSuggested(this, account2, str);
                        }

                        public void onResult(Bundle result) {
                            Bundle.setDefusable(result, true);
                            IAccountManagerResponse response = getResponseAndClose();
                            if (response != null) {
                                if (result == null) {
                                    this.sendErrorResponse(response, 5, "null bundle");
                                    return;
                                }
                                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                                }
                                if (result.getInt("errorCode", -1) > 0) {
                                    this.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                                } else if (result.containsKey("booleanResult")) {
                                    Bundle newResult = new Bundle();
                                    newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                                    this.sendResponse(response, newResult);
                                } else {
                                    this.sendErrorResponse(response, 5, "no result in response");
                                }
                            }
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(uid);
                throw new SecurityException(String.format("uid %s cannot stat add account session.", objArr));
            }
        }
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "editProperties: accountType " + accountType + ", response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(accountType, callingUid, userId) || isSystemUid(callingUid)) {
                long identityToken = clearCallingIdentity();
                try {
                    final String str = accountType;
                    new Session(this, getUserAccounts(userId), response, accountType, expectActivityLaunch, true, null, false) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.editProperties(this, this.mAccountType);
                        }

                        protected String toDebugString(long now) {
                            return super.toDebugString(now) + ", editProperties" + ", accountType " + str;
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(callingUid);
                objArr[1] = accountType;
                throw new SecurityException(String.format("uid %s cannot edit authenticator properites for account type: %s", objArr));
            }
        }
    }

    public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Can be called only by system UID");
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
        int userId = userHandle.getIdentifier();
        Preconditions.checkArgumentInRange(userId, 0, Integer.MAX_VALUE, "user must be concrete");
        try {
            return hasAccountAccess(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId));
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean hasAccountAccess(Account account, String packageName, int uid) {
        boolean z = true;
        if (packageName == null) {
            String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
            if (ArrayUtils.isEmpty(packageNames)) {
                return false;
            }
            packageName = packageNames[0];
        }
        if (permissionIsGranted(account, null, uid, UserHandle.getUserId(uid))) {
            return true;
        }
        if (!checkUidPermission("android.permission.GET_ACCOUNTS_PRIVILEGED", uid, packageName)) {
            z = checkUidPermission(OppoPermissionConstants.PERMISSION_GET_ACCOUNTS, uid, packageName);
        }
        return z;
    }

    private boolean checkUidPermission(String permission, int uid, String opPackageName) {
        boolean z = true;
        long identity = Binder.clearCallingIdentity();
        try {
            if (ActivityThread.getPackageManager().checkUidPermission(permission, uid) != 0) {
                return false;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (!(opCode == -1 || this.mAppOpsManager.noteOpNoThrow(opCode, uid, opPackageName) == 0)) {
                z = false;
            }
            Binder.restoreCallingIdentity(identity);
            return z;
        } catch (RemoteException e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Can be called only by system UID");
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
        int userId = userHandle.getIdentifier();
        Preconditions.checkArgumentInRange(userId, 0, Integer.MAX_VALUE, "user must be concrete");
        try {
            Intent intent = newRequestAccountAccessIntent(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId), null);
            long identity = Binder.clearCallingIdentity();
            try {
                IntentSender intentSender = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, new UserHandle(userId)).getIntentSender();
                return intentSender;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "Unknown package " + packageName);
            return null;
        }
    }

    private Intent newRequestAccountAccessIntent(Account account, String packageName, int uid, RemoteCallback callback) {
        final Account account2 = account;
        final int i = uid;
        final String str = packageName;
        final RemoteCallback remoteCallback = callback;
        AccountAuthenticatorResponse accountAuthenticatorResponse = new AccountAuthenticatorResponse(new IAccountAuthenticatorResponse.Stub() {
            public void onResult(Bundle value) throws RemoteException {
                handleAuthenticatorResponse(true);
            }

            public void onRequestContinued() {
            }

            public void onError(int errorCode, String errorMessage) throws RemoteException {
                handleAuthenticatorResponse(false);
            }

            private void handleAuthenticatorResponse(boolean accessGranted) throws RemoteException {
                AccountManagerService.this.cancelNotification(AccountManagerService.this.getCredentialPermissionNotificationId(account2, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", i).intValue(), str, UserHandle.getUserHandleForUid(i));
                if (remoteCallback != null) {
                    Bundle result = new Bundle();
                    result.putBoolean("booleanResult", accessGranted);
                    remoteCallback.sendResult(result);
                }
            }
        });
        return newGrantCredentialsPermissionIntent(account, packageName, uid, accountAuthenticatorResponse, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", false);
    }

    public boolean someUserHasAccount(Account account) {
        if (UserHandle.isSameApp(1000, Binder.getCallingUid())) {
            long token = Binder.clearCallingIdentity();
            try {
                AccountAndUser[] allAccounts = getAllAccounts();
                for (int i = allAccounts.length - 1; i >= 0; i--) {
                    if (allAccounts[i].account.equals(account)) {
                        return true;
                    }
                }
                Binder.restoreCallingIdentity(token);
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Only system can check for accounts across users");
        }
    }

    public Account[] getAccounts(int userId, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
        if (visibleAccountTypes.isEmpty()) {
            return new Account[0];
        }
        long identityToken = clearCallingIdentity();
        try {
            Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, null, visibleAccountTypes);
            return accountsInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public AccountAndUser[] getRunningAccounts() {
        try {
            return getAccounts(ActivityManagerNative.getDefault().getRunningUserIds());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public AccountAndUser[] getAllAccounts() {
        List<UserInfo> users = getUserManager().getUsers(true);
        int[] userIds = new int[users.size()];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = ((UserInfo) users.get(i)).id;
        }
        return getAccounts(userIds);
    }

    private AccountAndUser[] getAccounts(int[] userIds) {
        ArrayList<AccountAndUser> runningAccounts = Lists.newArrayList();
        for (int userId : userIds) {
            UserAccounts userAccounts = getUserAccounts(userId);
            if (userAccounts != null) {
                synchronized (userAccounts.cacheLock) {
                    Account[] accounts = getAccountsFromCacheLocked(userAccounts, null, Binder.getCallingUid(), null);
                    for (Account accountAndUser : accounts) {
                        runningAccounts.add(new AccountAndUser(accountAndUser, userId));
                    }
                }
            }
        }
        return (AccountAndUser[]) runningAccounts.toArray(new AccountAndUser[runningAccounts.size()]);
    }

    public Account[] getAccountsAsUser(String type, int userId, String opPackageName) {
        return getAccountsAsUser(type, userId, null, -1, opPackageName);
    }

    private Account[] getAccountsAsUser(String type, int userId, String callingPackage, int packageUid, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (userId == UserHandle.getCallingUserId() || callingUid == Process.myUid() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "getAccounts: accountType " + type + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
            }
            if (packageUid != -1 && UserHandle.isSameApp(callingUid, Process.myUid())) {
                callingUid = packageUid;
                opPackageName = callingPackage;
            }
            List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
            if (visibleAccountTypes.isEmpty() || (type != null && !visibleAccountTypes.contains(type))) {
                return new Account[0];
            }
            if (visibleAccountTypes.contains(type)) {
                visibleAccountTypes = new ArrayList();
                visibleAccountTypes.add(type);
            }
            long identityToken = clearCallingIdentity();
            try {
                Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, callingPackage, visibleAccountTypes);
                return accountsInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException("User " + UserHandle.getCallingUserId() + " trying to get account for " + userId);
        }
    }

    private Account[] getAccountsInternal(UserAccounts userAccounts, int callingUid, String callingPackage, List<String> visibleAccountTypes) {
        Account[] result;
        synchronized (userAccounts.cacheLock) {
            ArrayList<Account> visibleAccounts = new ArrayList();
            for (String visibleType : visibleAccountTypes) {
                Account[] accountsForType = getAccountsFromCacheLocked(userAccounts, visibleType, callingUid, callingPackage);
                if (accountsForType != null) {
                    visibleAccounts.addAll(Arrays.asList(accountsForType));
                }
            }
            result = new Account[visibleAccounts.size()];
            for (int i = 0; i < visibleAccounts.size(); i++) {
                result[i] = (Account) visibleAccounts.get(i);
            }
        }
        return result;
    }

    public void addSharedAccountsFromParentUser(int parentUserId, int userId) {
        checkManageOrCreateUsersPermission("addSharedAccountsFromParentUser");
        for (Account account : getAccountsAsUser(null, parentUserId, this.mContext.getOpPackageName())) {
            addSharedAccountAsUser(account, userId);
        }
    }

    private boolean addSharedAccountAsUser(Account account, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, account.name);
        values.put(SoundModelContract.KEY_TYPE, account.type);
        String[] strArr = new String[2];
        strArr[0] = account.name;
        strArr[1] = account.type;
        db.delete(TABLE_SHARED_ACCOUNTS, "name=? AND type=?", strArr);
        long accountId = db.insert(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME, values);
        if (accountId < 0) {
            Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
            return false;
        }
        logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_SHARED_ACCOUNTS, accountId, accounts);
        return true;
    }

    public boolean renameSharedAccountAsUser(Account account, String newName, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, newName);
        String[] strArr = new String[2];
        strArr[0] = account.name;
        strArr[1] = account.type;
        int r = db.update(TABLE_SHARED_ACCOUNTS, values, "name=? AND type=?", strArr);
        if (r > 0) {
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_SHARED_ACCOUNTS, sharedTableAccountId, accounts, getCallingUid());
            renameAccountInternal(accounts, account, newName);
        }
        return r > 0;
    }

    public boolean removeSharedAccountAsUser(Account account, int userId) {
        return removeSharedAccountAsUser(account, userId, getCallingUid());
    }

    private boolean removeSharedAccountAsUser(Account account, int userId, int callingUid) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        String[] strArr = new String[2];
        strArr[0] = account.name;
        strArr[1] = account.type;
        int r = db.delete(TABLE_SHARED_ACCOUNTS, "name=? AND type=?", strArr);
        if (r > 0) {
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_REMOVE, TABLE_SHARED_ACCOUNTS, sharedTableAccountId, accounts, callingUid);
            removeAccountInternal(accounts, account, callingUid);
        }
        return r > 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x005d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Account[] getSharedAccountsAsUser(int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        ArrayList<Account> accountList = new ArrayList();
        Cursor cursor = null;
        try {
            SQLiteDatabase readableDatabase = accounts.openHelper.getReadableDatabase();
            String str = TABLE_SHARED_ACCOUNTS;
            String[] strArr = new String[2];
            strArr[0] = ACCOUNTS_NAME;
            strArr[1] = SoundModelContract.KEY_TYPE;
            cursor = readableDatabase.query(str, strArr, null, null, null, null, null);
            Account[] accountArray;
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                accountArray = new Account[accountList.size()];
                accountList.toArray(accountArray);
                return accountArray;
            }
            int nameIndex = cursor.getColumnIndex(ACCOUNTS_NAME);
            int typeIndex = cursor.getColumnIndex(SoundModelContract.KEY_TYPE);
            do {
                accountList.add(new Account(cursor.getString(nameIndex), cursor.getString(typeIndex)));
            } while (cursor.moveToNext());
            if (cursor != null) {
            }
            accountArray = new Account[accountList.size()];
            accountList.toArray(accountArray);
            return accountArray;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Account[] getAccounts(String type, String opPackageName) {
        return getAccountsAsUser(type, UserHandle.getCallingUserId(), opPackageName);
    }

    public Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, Process.myUid())) {
            return getAccountsAsUser(null, UserHandle.getCallingUserId(), packageName, uid, opPackageName);
        }
        throw new SecurityException("getAccountsForPackage() called from unauthorized uid " + callingUid + " with uid=" + uid);
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName, String opPackageName) {
        try {
            return getAccountsAsUser(type, UserHandle.getCallingUserId(), packageName, AppGlobals.getPackageManager().getPackageUid(packageName, DumpState.DUMP_PREFERRED_XML, UserHandle.getCallingUserId()), opPackageName);
        } catch (RemoteException re) {
            Slog.e(TAG, "Couldn't determine the packageUid for " + packageName + re);
            return new Account[0];
        }
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccounts: accountType " + type + ", response " + response + ", features " + stringArrayToString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (type == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            Bundle result;
            if (getTypesVisibleToCaller(callingUid, userId, opPackageName).contains(type)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts userAccounts = getUserAccounts(userId);
                    if (features == null || features.length == 0) {
                        Account[] accounts;
                        synchronized (userAccounts.cacheLock) {
                            accounts = getAccountsFromCacheLocked(userAccounts, type, callingUid, null);
                        }
                        result = new Bundle();
                        result.putParcelableArray(TABLE_ACCOUNTS, accounts);
                        onResult(response, result);
                        return;
                    }
                    new GetAccountsByTypeAndFeatureSession(userAccounts, response, type, features, callingUid).bind();
                    restoreCallingIdentity(identityToken);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                result = new Bundle();
                result.putParcelableArray(TABLE_ACCOUNTS, new Account[0]);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot respond to caller do to exception.", e);
                }
            }
        }
    }

    private long getAccountIdFromSharedTable(SQLiteDatabase db, Account account) {
        String str = TABLE_SHARED_ACCOUNTS;
        String[] strArr = new String[1];
        strArr[0] = "_id";
        String[] strArr2 = new String[2];
        strArr2[0] = account.name;
        strArr2[1] = account.type;
        Cursor cursor = db.query(str, strArr, "name=? AND type=?", strArr2, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getAccountIdLocked(SQLiteDatabase db, Account account) {
        String str = TABLE_ACCOUNTS;
        String[] strArr = new String[1];
        strArr[0] = "_id";
        String[] strArr2 = new String[2];
        strArr2[0] = account.name;
        strArr2[1] = account.type;
        Cursor cursor = db.query(str, strArr, "name=? AND type=?", strArr2, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getExtrasIdLocked(SQLiteDatabase db, long accountId, String key) {
        String str = CE_TABLE_EXTRAS;
        String[] strArr = new String[1];
        strArr[0] = "_id";
        String str2 = "accounts_id=" + accountId + " AND " + "key" + "=?";
        String[] strArr2 = new String[1];
        strArr2[0] = key;
        Cursor cursor = db.query(str, strArr, str2, strArr2, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    public void onAccountAccessed(String token) throws RemoteException {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1000) {
            int userId = UserHandle.getCallingUserId();
            long identity = Binder.clearCallingIdentity();
            try {
                for (Account account : getAccounts(userId, this.mContext.getOpPackageName())) {
                    if (Objects.equals(account.getAccessId(), token) && !hasAccountAccess(account, null, uid)) {
                        updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid, true);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    String getPreNDatabaseName(int userId) {
        File systemDir = Environment.getDataSystemDirectory();
        File databaseFile = new File(Environment.getUserSystemDirectory(userId), "accounts.db");
        if (userId == 0) {
            File oldFile = new File(systemDir, "accounts.db");
            if (oldFile.exists() && !databaseFile.exists()) {
                File userDir = Environment.getUserSystemDirectory(userId);
                if (!userDir.exists() && !userDir.mkdirs()) {
                    throw new IllegalStateException("User dir cannot be created: " + userDir);
                } else if (!oldFile.renameTo(databaseFile)) {
                    throw new IllegalStateException("User dir cannot be migrated: " + databaseFile);
                }
            }
        }
        return databaseFile.getPath();
    }

    String getDeDatabaseName(int userId) {
        return new File(Environment.getDataSystemDeDirectory(userId), DE_DATABASE_NAME).getPath();
    }

    String getCeDatabaseName(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), CE_DATABASE_NAME).getPath();
    }

    private void logRecord(UserAccounts accounts, String action, String tableName) {
        logRecord(accounts.openHelper.getWritableDatabase(), action, tableName, -1, accounts);
    }

    private void logRecordWithUid(UserAccounts accounts, String action, String tableName, int uid) {
        logRecord(accounts.openHelper.getWritableDatabase(), action, tableName, -1, accounts, uid);
    }

    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId, UserAccounts userAccount) {
        logRecord(db, action, tableName, accountId, userAccount, getCallingUid());
    }

    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId, UserAccounts userAccount, int callingUid) {
        SQLiteStatement logStatement = userAccount.statementForLogging;
        logStatement.bindLong(1, accountId);
        logStatement.bindString(2, action);
        logStatement.bindString(3, DebugDbHelper.dateFromat.format(new Date()));
        logStatement.bindLong(4, (long) callingUid);
        logStatement.bindString(5, tableName);
        logStatement.bindLong(6, (long) userAccount.debugDbInsertionPoint);
        try {
            logStatement.execute();
        } catch (Exception e) {
            Slog.w(TAG, "Failed to insert a log record. Error: " + e);
        } finally {
            logStatement.clearBindings();
        }
        userAccount.debugDbInsertionPoint = (userAccount.debugDbInsertionPoint + 1) % 64;
    }

    private void initializeDebugDbSizeAndCompileSqlStatementForLogging(SQLiteDatabase db, UserAccounts userAccount) {
        int size = (int) getDebugTableRowCount(db);
        if (size >= 64) {
            userAccount.debugDbInsertionPoint = (int) getDebugTableInsertionPoint(db);
        } else {
            userAccount.debugDbInsertionPoint = size;
        }
        compileSqlStatementForLogging(db, userAccount);
    }

    private void compileSqlStatementForLogging(SQLiteDatabase db, UserAccounts userAccount) {
        userAccount.statementForLogging = db.compileStatement("INSERT OR REPLACE INTO " + DebugDbHelper.TABLE_DEBUG + " VALUES (?,?,?,?,?,?)");
    }

    private long getDebugTableRowCount(SQLiteDatabase db) {
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + DebugDbHelper.TABLE_DEBUG, null);
    }

    private long getDebugTableInsertionPoint(SQLiteDatabase db) {
        return DatabaseUtils.longForQuery(db, "SELECT " + DebugDbHelper.KEY + " FROM " + DebugDbHelper.TABLE_DEBUG + " ORDER BY " + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.KEY + " LIMIT 1", null);
    }

    public IBinder onBind(Intent intent) {
        return asBinder();
    }

    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            fout.println("Permission Denial: can't dump AccountsManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        boolean isCheckinRequest = !scanArgs(args, "--checkin") ? scanArgs(args, "-c") : true;
        IndentingPrintWriter ipw = new IndentingPrintWriter(fout, "  ");
        for (UserInfo user : getUserManager().getUsers()) {
            ipw.println("User " + user + META_KEY_DELIMITER);
            ipw.increaseIndent();
            dumpUser(getUserAccounts(user.id), fd, ipw, args, isCheckinRequest);
            ipw.println();
            ipw.decreaseIndent();
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.accounts.AccountManagerService.dumpUser(com.android.server.accounts.AccountManagerService$UserAccounts, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[], boolean):void, dom blocks: [B:6:0x0021, B:29:0x00ce]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void dumpUser(com.android.server.accounts.AccountManagerService.UserAccounts r21, java.io.FileDescriptor r22, java.io.PrintWriter r23, java.lang.String[] r24, boolean r25) {
        /*
        r20 = this;
        r0 = r21;
        r0 = r0.cacheLock;
        r19 = r0;
        monitor-enter(r19);
        r0 = r21;	 Catch:{ all -> 0x0056 }
        r5 = r0.openHelper;	 Catch:{ all -> 0x0056 }
        r4 = r5.getReadableDatabase();	 Catch:{ all -> 0x0056 }
        if (r25 == 0) goto L_0x0060;	 Catch:{ all -> 0x0056 }
    L_0x0011:
        r5 = "accounts";	 Catch:{ all -> 0x0056 }
        r6 = ACCOUNT_TYPE_COUNT_PROJECTION;	 Catch:{ all -> 0x0056 }
        r9 = "type";	 Catch:{ all -> 0x0056 }
        r7 = 0;	 Catch:{ all -> 0x0056 }
        r8 = 0;	 Catch:{ all -> 0x0056 }
        r10 = 0;	 Catch:{ all -> 0x0056 }
        r11 = 0;	 Catch:{ all -> 0x0056 }
        r14 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x0056 }
    L_0x0021:
        r5 = r14.moveToNext();	 Catch:{ all -> 0x004f }
        if (r5 == 0) goto L_0x0059;	 Catch:{ all -> 0x004f }
    L_0x0027:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x004f }
        r5.<init>();	 Catch:{ all -> 0x004f }
        r6 = 0;	 Catch:{ all -> 0x004f }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x004f }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004f }
        r6 = ",";	 Catch:{ all -> 0x004f }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004f }
        r6 = 1;	 Catch:{ all -> 0x004f }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x004f }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004f }
        r5 = r5.toString();	 Catch:{ all -> 0x004f }
        r0 = r23;	 Catch:{ all -> 0x004f }
        r0.println(r5);	 Catch:{ all -> 0x004f }
        goto L_0x0021;
    L_0x004f:
        r5 = move-exception;
        if (r14 == 0) goto L_0x0055;
    L_0x0052:
        r14.close();	 Catch:{ all -> 0x0056 }
    L_0x0055:
        throw r5;	 Catch:{ all -> 0x0056 }
    L_0x0056:
        r5 = move-exception;
        monitor-exit(r19);
        throw r5;
    L_0x0059:
        if (r14 == 0) goto L_0x005e;
    L_0x005b:
        r14.close();	 Catch:{ all -> 0x0056 }
    L_0x005e:
        monitor-exit(r19);
        return;
    L_0x0060:
        r5 = android.os.Process.myUid();	 Catch:{ all -> 0x0056 }
        r6 = 0;	 Catch:{ all -> 0x0056 }
        r7 = 0;	 Catch:{ all -> 0x0056 }
        r0 = r20;	 Catch:{ all -> 0x0056 }
        r1 = r21;	 Catch:{ all -> 0x0056 }
        r13 = r0.getAccountsFromCacheLocked(r1, r6, r5, r7);	 Catch:{ all -> 0x0056 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0056 }
        r5.<init>();	 Catch:{ all -> 0x0056 }
        r6 = "Accounts: ";	 Catch:{ all -> 0x0056 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0056 }
        r6 = r13.length;	 Catch:{ all -> 0x0056 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0056 }
        r5 = r5.toString();	 Catch:{ all -> 0x0056 }
        r0 = r23;	 Catch:{ all -> 0x0056 }
        r0.println(r5);	 Catch:{ all -> 0x0056 }
        r5 = 0;	 Catch:{ all -> 0x0056 }
        r6 = r13.length;	 Catch:{ all -> 0x0056 }
    L_0x008a:
        if (r5 >= r6) goto L_0x00aa;	 Catch:{ all -> 0x0056 }
    L_0x008c:
        r12 = r13[r5];	 Catch:{ all -> 0x0056 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0056 }
        r7.<init>();	 Catch:{ all -> 0x0056 }
        r8 = "  ";	 Catch:{ all -> 0x0056 }
        r7 = r7.append(r8);	 Catch:{ all -> 0x0056 }
        r7 = r7.append(r12);	 Catch:{ all -> 0x0056 }
        r7 = r7.toString();	 Catch:{ all -> 0x0056 }
        r0 = r23;	 Catch:{ all -> 0x0056 }
        r0.println(r7);	 Catch:{ all -> 0x0056 }
        r5 = r5 + 1;	 Catch:{ all -> 0x0056 }
        goto L_0x008a;	 Catch:{ all -> 0x0056 }
    L_0x00aa:
        r23.println();	 Catch:{ all -> 0x0056 }
        r5 = com.android.server.accounts.AccountManagerService.DebugDbHelper.TABLE_DEBUG;	 Catch:{ all -> 0x0056 }
        r11 = com.android.server.accounts.AccountManagerService.DebugDbHelper.TIMESTAMP;	 Catch:{ all -> 0x0056 }
        r6 = 0;	 Catch:{ all -> 0x0056 }
        r7 = 0;	 Catch:{ all -> 0x0056 }
        r8 = 0;	 Catch:{ all -> 0x0056 }
        r9 = 0;	 Catch:{ all -> 0x0056 }
        r10 = 0;	 Catch:{ all -> 0x0056 }
        r14 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x0056 }
        r5 = "AccountId, Action_Type, timestamp, UID, TableName, Key";	 Catch:{ all -> 0x0056 }
        r0 = r23;	 Catch:{ all -> 0x0056 }
        r0.println(r5);	 Catch:{ all -> 0x0056 }
        r5 = "Accounts History";	 Catch:{ all -> 0x0056 }
        r0 = r23;	 Catch:{ all -> 0x0056 }
        r0.println(r5);	 Catch:{ all -> 0x0056 }
    L_0x00ce:
        r5 = r14.moveToNext();	 Catch:{ all -> 0x013c }
        if (r5 == 0) goto L_0x0141;	 Catch:{ all -> 0x013c }
    L_0x00d4:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x013c }
        r5.<init>();	 Catch:{ all -> 0x013c }
        r6 = 0;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = ",";	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = 1;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = ",";	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = 2;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = ",";	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = 3;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = ",";	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = 4;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = ",";	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r6 = 5;	 Catch:{ all -> 0x013c }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013c }
        r5 = r5.toString();	 Catch:{ all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x013c }
        r0.println(r5);	 Catch:{ all -> 0x013c }
        goto L_0x00ce;
    L_0x013c:
        r5 = move-exception;
        r14.close();	 Catch:{ all -> 0x0056 }
        throw r5;	 Catch:{ all -> 0x0056 }
    L_0x0141:
        r14.close();	 Catch:{ all -> 0x0056 }
        r23.println();	 Catch:{ all -> 0x0056 }
        r0 = r20;	 Catch:{ all -> 0x0056 }
        r6 = r0.mSessions;	 Catch:{ all -> 0x0056 }
        monitor-enter(r6);	 Catch:{ all -> 0x0056 }
        r16 = android.os.SystemClock.elapsedRealtime();	 Catch:{ all -> 0x01a7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01a7 }
        r5.<init>();	 Catch:{ all -> 0x01a7 }
        r7 = "Active Sessions: ";	 Catch:{ all -> 0x01a7 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a7 }
        r0 = r20;	 Catch:{ all -> 0x01a7 }
        r7 = r0.mSessions;	 Catch:{ all -> 0x01a7 }
        r7 = r7.size();	 Catch:{ all -> 0x01a7 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a7 }
        r5 = r5.toString();	 Catch:{ all -> 0x01a7 }
        r0 = r23;	 Catch:{ all -> 0x01a7 }
        r0.println(r5);	 Catch:{ all -> 0x01a7 }
        r0 = r20;	 Catch:{ all -> 0x01a7 }
        r5 = r0.mSessions;	 Catch:{ all -> 0x01a7 }
        r5 = r5.values();	 Catch:{ all -> 0x01a7 }
        r18 = r5.iterator();	 Catch:{ all -> 0x01a7 }
    L_0x017d:
        r5 = r18.hasNext();	 Catch:{ all -> 0x01a7 }
        if (r5 == 0) goto L_0x01aa;	 Catch:{ all -> 0x01a7 }
    L_0x0183:
        r15 = r18.next();	 Catch:{ all -> 0x01a7 }
        r15 = (com.android.server.accounts.AccountManagerService.Session) r15;	 Catch:{ all -> 0x01a7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01a7 }
        r5.<init>();	 Catch:{ all -> 0x01a7 }
        r7 = "  ";	 Catch:{ all -> 0x01a7 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a7 }
        r7 = r15.toDebugString(r16);	 Catch:{ all -> 0x01a7 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a7 }
        r5 = r5.toString();	 Catch:{ all -> 0x01a7 }
        r0 = r23;	 Catch:{ all -> 0x01a7 }
        r0.println(r5);	 Catch:{ all -> 0x01a7 }
        goto L_0x017d;
    L_0x01a7:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0056 }
        throw r5;	 Catch:{ all -> 0x0056 }
    L_0x01aa:
        monitor-exit(r6);	 Catch:{ all -> 0x0056 }
        r23.println();	 Catch:{ all -> 0x0056 }
        r0 = r20;	 Catch:{ all -> 0x0056 }
        r5 = r0.mAuthenticatorCache;	 Catch:{ all -> 0x0056 }
        r6 = r21.userId;	 Catch:{ all -> 0x0056 }
        r0 = r22;	 Catch:{ all -> 0x0056 }
        r1 = r23;	 Catch:{ all -> 0x0056 }
        r2 = r24;	 Catch:{ all -> 0x0056 }
        r5.dump(r0, r1, r2, r6);	 Catch:{ all -> 0x0056 }
        goto L_0x005e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.dumpUser(com.android.server.accounts.AccountManagerService$UserAccounts, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[], boolean):void");
    }

    private void doNotification(UserAccounts accounts, Account account, CharSequence message, Intent intent, String packageName, int userId) {
        long identityToken = clearCallingIdentity();
        try {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "doNotification: " + message + " intent:" + intent);
            }
            if (intent.getComponent() == null || !GrantCredentialsPermissionActivity.class.getName().equals(intent.getComponent().getClassName())) {
                Context contextForUser = getContextForUser(new UserHandle(userId));
                Integer notificationId = getSigninRequiredNotificationId(accounts, account);
                intent.addCategory(String.valueOf(notificationId));
                String notificationTitleFormat = contextForUser.getText(17039624).toString();
                Builder color = new Builder(contextForUser).setWhen(0).setSmallIcon(17301642).setColor(contextForUser.getColor(17170523));
                Object[] objArr = new Object[1];
                objArr[0] = account.name;
                Builder contentText = color.setContentTitle(String.format(notificationTitleFormat, objArr)).setContentText(message);
                Notification n = contentText.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, new UserHandle(userId))).build();
                installNotification(notificationId.intValue(), n, packageName, userId);
            } else {
                createNoCredentialsPermissionNotification(account, intent, packageName, userId);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    protected void installNotification(int notificationId, Notification notification, UserHandle user) {
        installNotification(notificationId, notification, "android", user.getIdentifier());
    }

    private void installNotification(int notificationId, Notification notification, String packageName, int userId) {
        long token = clearCallingIdentity();
        try {
            try {
                NotificationManager.getService().enqueueNotificationWithTag(packageName, packageName, null, notificationId, notification, new int[1], userId);
            } catch (RemoteException e) {
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    protected void cancelNotification(int id, UserHandle user) {
        cancelNotification(id, this.mContext.getPackageName(), user);
    }

    protected void cancelNotification(int id, String packageName, UserHandle user) {
        long identityToken = clearCallingIdentity();
        try {
            INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)).cancelNotificationWithTag(packageName, null, id, user.getIdentifier());
        } catch (RemoteException e) {
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean isPermitted(String opPackageName, int callingUid, String... permissions) {
        for (String perm : permissions) {
            if (this.mContext.checkCallingOrSelfPermission(perm) == 0) {
                if (Log.isLoggable(TAG, 2)) {
                    Log.v(TAG, "  caller uid " + callingUid + " has " + perm);
                }
                int opCode = AppOpsManager.permissionToOpCode(perm);
                if (opCode == -1 || this.mAppOpsManager.noteOp(opCode, callingUid, opPackageName) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int handleIncomingUser(int userId) {
        try {
            return ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, IElsaManager.EMPTY_PACKAGE, null);
        } catch (RemoteException e) {
            return userId;
        }
    }

    private boolean isPrivileged(int callingUid) {
        try {
            PackageManager userPackageManager = this.mContext.createPackageContextAsUser("android", 0, new UserHandle(UserHandle.getUserId(callingUid))).getPackageManager();
            String[] packages = userPackageManager.getPackagesForUid(callingUid);
            int length = packages.length;
            int i = 0;
            while (i < length) {
                try {
                    PackageInfo packageInfo = userPackageManager.getPackageInfo(packages[i], 0);
                    if (packageInfo != null && (packageInfo.applicationInfo.privateFlags & 8) != 0) {
                        return true;
                    }
                    i++;
                } catch (NameNotFoundException e) {
                    return false;
                }
            }
            return false;
        } catch (NameNotFoundException e2) {
            return false;
        }
    }

    private boolean permissionIsGranted(Account account, String authTokenType, int callerUid, int userId) {
        if (UserHandle.getAppId(callerUid) == 1000) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid is system");
            }
            return true;
        } else if (isPrivileged(callerUid)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " privileged");
            }
            return true;
        } else if (account != null && isAccountManagedByCaller(account.type, callerUid, userId)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " manages the account");
            }
            return true;
        } else if (account == null || !hasExplicitlyGrantedPermission(account, authTokenType, callerUid)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " not granted for uid " + callerUid);
            }
            return false;
        } else {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " user granted access");
            }
            return true;
        }
    }

    private boolean isAccountVisibleToCaller(String accountType, int callingUid, int userId, String opPackageName) {
        if (accountType == null) {
            return false;
        }
        return getTypesVisibleToCaller(callingUid, userId, opPackageName).contains(accountType);
    }

    private boolean isAccountManagedByCaller(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return false;
        }
        return getTypesManagedByCaller(callingUid, userId).contains(accountType);
    }

    private List<String> getTypesVisibleToCaller(int callingUid, int userId, String opPackageName) {
        String[] strArr = new String[2];
        strArr[0] = OppoPermissionConstants.PERMISSION_GET_ACCOUNTS;
        strArr[1] = "android.permission.GET_ACCOUNTS_PRIVILEGED";
        return getTypesForCaller(callingUid, userId, isPermitted(opPackageName, callingUid, strArr));
    }

    private List<String> getTypesManagedByCaller(int callingUid, int userId) {
        return getTypesForCaller(callingUid, userId, false);
    }

    private List<String> getTypesForCaller(int callingUid, int userId, boolean isOtherwisePermitted) {
        List<String> managedAccountTypes = new ArrayList();
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            for (ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                int sigChk = this.mPackageManager.checkSignatures(serviceInfo.uid, callingUid);
                if (isOtherwisePermitted || sigChk == 0) {
                    managedAccountTypes.add(((AuthenticatorDescription) serviceInfo.type).type);
                }
            }
            return managedAccountTypes;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private boolean isAccountPresentForCaller(String accountName, String accountType) {
        if (getUserAccountsForCaller().accountCache.containsKey(accountType)) {
            for (Account account : (Account[]) getUserAccountsForCaller().accountCache.get(accountType)) {
                if (account.name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void checkManageUsersPermission(String message) {
        if (ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", Binder.getCallingUid(), -1, true) != 0) {
            throw new SecurityException("You need MANAGE_USERS permission to: " + message);
        }
    }

    private static void checkManageOrCreateUsersPermission(String message) {
        if (ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", Binder.getCallingUid(), -1, true) != 0 && ActivityManager.checkComponentPermission("android.permission.CREATE_USERS", Binder.getCallingUid(), -1, true) != 0) {
            throw new SecurityException("You need MANAGE_USERS or CREATE_USERS permission to: " + message);
        }
    }

    /* JADX WARNING: Missing block: B:21:0x009d, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasExplicitlyGrantedPermission(Account account, String authTokenType, int callerUid) {
        boolean permissionGranted = false;
        if (UserHandle.getAppId(callerUid) == 1000) {
            return true;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(callerUid));
        synchronized (accounts.cacheLock) {
            String query;
            String[] args;
            SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
            if (authTokenType != null) {
                query = COUNT_OF_MATCHING_GRANTS;
                args = new String[4];
                args[0] = String.valueOf(callerUid);
                args[1] = authTokenType;
                args[2] = account.name;
                args[3] = account.type;
            } else {
                query = COUNT_OF_MATCHING_GRANTS_ANY_TOKEN;
                args = new String[3];
                args[0] = String.valueOf(callerUid);
                args[1] = account.name;
                args[2] = account.type;
            }
            if (DatabaseUtils.longForQuery(db, query, args) != 0) {
                permissionGranted = true;
            }
            if (permissionGranted || !ActivityManager.isRunningInTestHarness()) {
            } else {
                Log.d(TAG, "no credentials permission for usage of " + account + ", " + authTokenType + " by uid " + callerUid + " but ignoring since device is in test harness.");
                return true;
            }
        }
    }

    private boolean isSystemUid(int callingUid) {
        String[] packages = null;
        long ident = Binder.clearCallingIdentity();
        try {
            packages = this.mPackageManager.getPackagesForUid(callingUid);
            if (packages != null) {
                for (String name : packages) {
                    try {
                        PackageInfo packageInfo = this.mPackageManager.getPackageInfo(name, 0);
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 1) == 0)) {
                            return true;
                        }
                    } catch (NameNotFoundException e) {
                        String str = TAG;
                        Object[] objArr = new Object[1];
                        objArr[0] = name;
                        Log.w(str, String.format("Could not find package [%s]", objArr), e);
                    }
                }
            } else {
                Log.w(TAG, "No known packages with uid " + callingUid);
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void checkReadAccountsPermitted(int callingUid, String accountType, int userId, String opPackageName) {
        if (!isAccountVisibleToCaller(accountType, callingUid, userId, opPackageName)) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(callingUid);
            objArr[1] = accountType;
            String msg = String.format("caller uid %s cannot access %s accounts", objArr);
            Log.w(TAG, "  " + msg);
            throw new SecurityException(msg);
        }
    }

    private boolean canUserModifyAccounts(int userId, int callingUid) {
        if (!isProfileOwner(callingUid) && getUserManager().getUserRestrictions(new UserHandle(userId)).getBoolean("no_modify_accounts")) {
            return false;
        }
        return true;
    }

    private boolean canUserModifyAccountsForType(int userId, String accountType, int callingUid) {
        if (isProfileOwner(callingUid)) {
            return true;
        }
        String[] typesArray = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getAccountTypesWithManagementDisabledAsUser(userId);
        if (typesArray == null) {
            return true;
        }
        for (String forbiddenType : typesArray) {
            if (forbiddenType.equals(accountType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isProfileOwner(int uid) {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null) {
            return dpmi.isActiveAdminWithPolicy(uid, -1);
        }
        return false;
    }

    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) throws RemoteException {
        if (UserHandle.getAppId(getCallingUid()) != 1000) {
            throw new SecurityException();
        } else if (value) {
            grantAppPermission(account, authTokenType, uid);
        } else {
            revokeAppPermission(account, authTokenType, uid);
        }
    }

    void grantAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "grantAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    ContentValues values = new ContentValues();
                    values.put("accounts_id", Long.valueOf(accountId));
                    values.put(GRANTS_AUTH_TOKEN_TYPE, authTokenType);
                    values.put("uid", Integer.valueOf(uid));
                    db.insert(TABLE_GRANTS, "accounts_id", values);
                    db.setTransactionSuccessful();
                }
                db.endTransaction();
                cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), UserHandle.of(accounts.userId));
                cancelAccountAccessRequestNotificationIfNeeded(account, uid, true);
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        for (OnAppPermissionChangeListener listener : this.mAppPermissionChangeListeners) {
            this.mMessageHandler.post(new -void_grantAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0(listener, account, uid));
        }
    }

    private void revokeAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "revokeAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    String[] strArr = new String[3];
                    strArr[0] = String.valueOf(accountId);
                    strArr[1] = authTokenType;
                    strArr[2] = String.valueOf(uid);
                    db.delete(TABLE_GRANTS, "accounts_id=? AND auth_token_type=? AND uid=?", strArr);
                    db.setTransactionSuccessful();
                }
                db.endTransaction();
                cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), new UserHandle(accounts.userId));
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        for (OnAppPermissionChangeListener listener : this.mAppPermissionChangeListeners) {
            this.mMessageHandler.post(new -void_revokeAppPermission_android_accounts_Account_account_java_lang_String_authTokenType_int_uid_LambdaImpl0(listener, account, uid));
        }
    }

    private static final String stringArrayToString(String[] value) {
        return value != null ? "[" + TextUtils.join(",", value) + "]" : null;
    }

    private void removeAccountFromCacheLocked(UserAccounts accounts, Account account) {
        Account[] oldAccountsForType = (Account[]) accounts.accountCache.get(account.type);
        if (oldAccountsForType != null) {
            ArrayList<Account> newAccountsList = new ArrayList();
            for (Account curAccount : oldAccountsForType) {
                if (!curAccount.equals(account)) {
                    newAccountsList.add(curAccount);
                }
            }
            if (newAccountsList.isEmpty()) {
                accounts.accountCache.remove(account.type);
            } else {
                accounts.accountCache.put(account.type, (Account[]) newAccountsList.toArray(new Account[newAccountsList.size()]));
            }
        }
        accounts.userDataCache.remove(account);
        accounts.authTokenCache.remove(account);
        accounts.previousNameCache.remove(account);
    }

    private Account insertAccountIntoCacheLocked(UserAccounts accounts, Account account) {
        int oldLength;
        String token;
        Account[] accountsForType = (Account[]) accounts.accountCache.get(account.type);
        if (accountsForType != null) {
            oldLength = accountsForType.length;
        } else {
            oldLength = 0;
        }
        Account[] newAccountsForType = new Account[(oldLength + 1)];
        if (accountsForType != null) {
            System.arraycopy(accountsForType, 0, newAccountsForType, 0, oldLength);
        }
        if (account.getAccessId() != null) {
            token = account.getAccessId();
        } else {
            token = UUID.randomUUID().toString();
        }
        newAccountsForType[oldLength] = new Account(account, token);
        accounts.accountCache.put(account.type, newAccountsForType);
        return newAccountsForType[oldLength];
    }

    private Account[] filterSharedAccounts(UserAccounts userAccounts, Account[] unfiltered, int callingUid, String callingPackage) {
        if (getUserManager() == null || userAccounts == null || userAccounts.userId < 0 || callingUid == Process.myUid()) {
            return unfiltered;
        }
        UserInfo user = getUserManager().getUserInfo(userAccounts.userId);
        if (user == null || !user.isRestricted()) {
            return unfiltered;
        }
        int i;
        int i2;
        String[] packages = this.mPackageManager.getPackagesForUid(callingUid);
        String whiteList = this.mContext.getResources().getString(17039461);
        for (String packageName : packages) {
            if (whiteList.contains(";" + packageName + ";")) {
                return unfiltered;
            }
        }
        ArrayList<Account> allowed = new ArrayList();
        Account[] sharedAccounts = getSharedAccountsAsUser(userAccounts.userId);
        if (sharedAccounts == null || sharedAccounts.length == 0) {
            return unfiltered;
        }
        String requiredAccountType = IElsaManager.EMPTY_PACKAGE;
        PackageInfo pi;
        if (callingPackage == null) {
            for (String packageName2 : packages) {
                pi = this.mPackageManager.getPackageInfo(packageName2, 0);
                if (pi != null && pi.restrictedAccountType != null) {
                    requiredAccountType = pi.restrictedAccountType;
                    break;
                }
            }
        } else {
            try {
                pi = this.mPackageManager.getPackageInfo(callingPackage, 0);
                if (!(pi == null || pi.restrictedAccountType == null)) {
                    requiredAccountType = pi.restrictedAccountType;
                }
            } catch (NameNotFoundException e) {
            }
        }
        i = 0;
        int length = unfiltered.length;
        while (true) {
            i2 = i;
            if (i2 < length) {
                Account account = unfiltered[i2];
                if (account.type.equals(requiredAccountType)) {
                    allowed.add(account);
                } else {
                    boolean found = false;
                    for (Account shared : sharedAccounts) {
                        if (shared.equals(account)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allowed.add(account);
                    }
                }
                i = i2 + 1;
            } else {
                Account[] filtered = new Account[allowed.size()];
                allowed.toArray(filtered);
                return filtered;
            }
        }
    }

    protected Account[] getAccountsFromCacheLocked(UserAccounts userAccounts, String accountType, int callingUid, String callingPackage) {
        Account[] accounts;
        if (accountType != null) {
            accounts = (Account[]) userAccounts.accountCache.get(accountType);
            if (accounts == null) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            return filterSharedAccounts(userAccounts, (Account[]) Arrays.copyOf(accounts, accounts.length), callingUid, callingPackage);
        }
        int totalLength = 0;
        for (Account[] accounts2 : userAccounts.accountCache.values()) {
            totalLength += accounts2.length;
        }
        if (totalLength == 0) {
            return EMPTY_ACCOUNT_ARRAY;
        }
        accounts2 = new Account[totalLength];
        totalLength = 0;
        for (Account[] accountsOfType : userAccounts.accountCache.values()) {
            System.arraycopy(accountsOfType, 0, accounts2, totalLength, accountsOfType.length);
            totalLength += accountsOfType.length;
        }
        return filterSharedAccounts(userAccounts, accounts2, callingUid, callingPackage);
    }

    protected void writeUserDataIntoCacheLocked(UserAccounts accounts, SQLiteDatabase db, Account account, String key, String value) {
        HashMap<String, String> userDataForAccount = (HashMap) accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = readUserDataForAccountFromDatabaseLocked(db, account);
            accounts.userDataCache.put(account, userDataForAccount);
        }
        if (value == null) {
            userDataForAccount.remove(key);
        } else {
            userDataForAccount.put(key, value);
        }
    }

    protected String readCachedTokenInternal(UserAccounts accounts, Account account, String tokenType, String callingPackage, byte[] pkgSigDigest) {
        String str;
        synchronized (accounts.cacheLock) {
            str = accounts.accountTokenCaches.get(account, tokenType, callingPackage, pkgSigDigest);
        }
        return str;
    }

    protected void writeAuthTokenIntoCacheLocked(UserAccounts accounts, SQLiteDatabase db, Account account, String key, String value) {
        HashMap<String, String> authTokensForAccount = (HashMap) accounts.authTokenCache.get(account);
        if (authTokensForAccount == null) {
            authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(db, account);
            accounts.authTokenCache.put(account, authTokensForAccount);
        }
        if (value == null) {
            authTokensForAccount.remove(key);
        } else {
            authTokensForAccount.put(key, value);
        }
    }

    protected String readAuthTokenInternal(UserAccounts accounts, Account account, String authTokenType) {
        String str;
        synchronized (accounts.cacheLock) {
            HashMap<String, String> authTokensForAccount = (HashMap) accounts.authTokenCache.get(account);
            if (authTokensForAccount == null) {
                authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account);
                accounts.authTokenCache.put(account, authTokensForAccount);
            }
            str = (String) authTokensForAccount.get(authTokenType);
        }
        return str;
    }

    protected String readUserDataInternalLocked(UserAccounts accounts, Account account, String key) {
        HashMap<String, String> userDataForAccount = (HashMap) accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = readUserDataForAccountFromDatabaseLocked(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account);
            accounts.userDataCache.put(account, userDataForAccount);
        }
        return (String) userDataForAccount.get(key);
    }

    protected HashMap<String, String> readUserDataForAccountFromDatabaseLocked(SQLiteDatabase db, Account account) {
        HashMap<String, String> userDataForAccount = new HashMap();
        String[] strArr = new String[2];
        strArr[0] = account.name;
        strArr[1] = account.type;
        Cursor cursor = db.query(CE_TABLE_EXTRAS, COLUMNS_EXTRAS_KEY_AND_VALUE, "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)", strArr, null, null, null);
        while (cursor.moveToNext()) {
            try {
                userDataForAccount.put(cursor.getString(0), cursor.getString(1));
            } finally {
                cursor.close();
            }
        }
        return userDataForAccount;
    }

    protected HashMap<String, String> readAuthTokensForAccountFromDatabaseLocked(SQLiteDatabase db, Account account) {
        HashMap<String, String> authTokensForAccount = new HashMap();
        String[] strArr = new String[2];
        strArr[0] = account.name;
        strArr[1] = account.type;
        Cursor cursor = db.query(CE_TABLE_AUTHTOKENS, COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN, "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)", strArr, null, null, null);
        while (cursor.moveToNext()) {
            try {
                authTokensForAccount.put(cursor.getString(0), cursor.getString(1));
            } finally {
                cursor.close();
            }
        }
        return authTokensForAccount;
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            return this.mContext;
        }
    }

    private void sendResponse(IAccountManagerResponse response, Bundle result) {
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    private void sendErrorResponse(IAccountManagerResponse response, int errorCode, String errorMessage) {
        try {
            response.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }
}
