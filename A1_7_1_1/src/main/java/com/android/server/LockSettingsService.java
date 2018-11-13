package com.android.server;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.backup.BackupManager;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IProgressListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.security.KeyStore;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.ILockSettings.Stub;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.StrongAuthTracker;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.LockSettingsStorage.Callback;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.am.OppoProcessManager;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore.Entry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import libcore.util.HexEncoding;

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
public class LockSettingsService extends Stub {
    private static final Intent ACTION_NULL = null;
    private static final boolean DEBUG = false;
    private static final int FBE_ENCRYPTED_NOTIFICATION = 0;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    private static final int PROFILE_KEY_IV_SIZE = 12;
    private static final String[] READ_CONTACTS_PROTECTED_SETTINGS = null;
    private static final String[] READ_PASSWORD_PROTECTED_SETTINGS = null;
    private static final String SEPARATE_PROFILE_CHALLENGE_KEY = "lockscreen.profilechallenge";
    private static final String[] SETTINGS_TO_BACKUP = null;
    private static final int[] SYSTEM_CREDENTIAL_UIDS = null;
    private static final String TAG = "LockSettingsService";
    private static final String[] VALID_SETTINGS = null;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mFirstCallToVold;
    private IGateKeeperService mGateKeeperService;
    private final Handler mHandler;
    private boolean mIsQualcommTimeoutFlagOn;
    private final KeyStore mKeyStore;
    private LockPatternUtils mLockPatternUtils;
    private NotificationManager mNotificationManager;
    private final Object mSeparateChallengeLock;
    private final LockSettingsStorage mStorage;
    private final LockSettingsStrongAuth mStrongAuth;
    private final SynchronizedStrongAuthTracker mStrongAuthTracker;
    private UserManager mUserManager;

    final /* synthetic */ class -void_notifyPasswordChanged_int_userId_LambdaImpl0 implements Runnable {
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_notifyPasswordChanged_int_userId_LambdaImpl0(int i) {
            this.val$userId = i;
        }

        public void run() {
            LockSettingsService.this.m7-com_android_server_LockSettingsService_lambda$1(this.val$userId);
        }
    }

    private interface CredentialUtil {
        String adjustForKeystore(String str);

        void setCredential(String str, String str2, int i) throws RemoteException;

        byte[] toHash(String str, int i);
    }

    private class GateKeeperDiedRecipient implements DeathRecipient {
        /* synthetic */ GateKeeperDiedRecipient(LockSettingsService this$0, GateKeeperDiedRecipient gateKeeperDiedRecipient) {
            this();
        }

        private GateKeeperDiedRecipient() {
        }

        public void binderDied() {
            LockSettingsService.this.mGateKeeperService.asBinder().unlinkToDeath(this, 0);
            LockSettingsService.this.mGateKeeperService = null;
        }
    }

    public static final class Lifecycle extends SystemService {
        private LockSettingsService mLockSettingsService;

        public Lifecycle(Context context) {
            super(context);
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-12-25 : Modify for ColorOS Service", property = OppoRomType.ROM)
        public void onStart() {
            AndroidKeyStoreProvider.install();
            this.mLockSettingsService = new ColorLockSettingsService(getContext());
            publishBinderService("lock_settings", this.mLockSettingsService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mLockSettingsService.maybeShowEncryptionNotifications();
            } else {
                if (phase == 1000) {
                }
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mLockSettingsService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            this.mLockSettingsService.onCleanupUser(userHandle);
        }
    }

    private class SynchronizedStrongAuthTracker extends StrongAuthTracker {
        public SynchronizedStrongAuthTracker(Context context) {
            super(context);
        }

        protected void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
            synchronized (this) {
                super.handleStrongAuthRequiredChanged(strongAuthFlags, userId);
            }
        }

        public int getStrongAuthForUser(int userId) {
            int strongAuthForUser;
            synchronized (this) {
                strongAuthForUser = super.getStrongAuthForUser(userId);
            }
            return strongAuthForUser;
        }

        void register() {
            LockSettingsService.this.mStrongAuth.registerStrongAuthTracker(this.mStub);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.LockSettingsService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.LockSettingsService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LockSettingsService.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x007e A:{Splitter: B:23:0x0059, ExcHandler: java.security.NoSuchAlgorithmException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:26:0x007e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x007f, code:
            android.util.Slog.e(TAG, "Fail to tie managed profile", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void tieManagedProfileLockIfNecessary(int managedUserId, String managedUserPassword) {
        if (UserManager.get(this.mContext).getUserInfo(managedUserId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId) && !this.mStorage.hasChildProfileLock(managedUserId)) {
            int parentId = this.mUserManager.getProfileParent(managedUserId).id;
            if (this.mStorage.hasPassword(parentId) || this.mStorage.hasPattern(parentId)) {
                try {
                    if (getGateKeeperService().getSecureUserId(parentId) != 0) {
                        byte[] randomLockSeed = new byte[0];
                        try {
                            String newPassword = String.valueOf(HexEncoding.encode(SecureRandom.getInstance("SHA1PRNG").generateSeed(40)));
                            setLockPasswordInternal(newPassword, managedUserPassword, managedUserId);
                            setLong("lockscreen.password_type", 327680, managedUserId);
                            tieProfileLockToParent(managedUserId, newPassword);
                        } catch (Exception e) {
                        }
                    }
                } catch (RemoteException e2) {
                    Slog.e(TAG, "Failed to talk to GateKeeper service", e2);
                }
            }
        }
    }

    public LockSettingsService(Context context) {
        this.mSeparateChallengeLock = new Object();
        this.mKeyStore = KeyStore.getInstance();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int userHandle;
                if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, true);
                    }
                    KeyStore ks = KeyStore.getInstance();
                    UserInfo parentInfo = LockSettingsService.this.mUserManager.getProfileParent(userHandle);
                    ks.onUserAdded(userHandle, parentInfo != null ? parentInfo.id : -1);
                } else if ("android.intent.action.USER_STARTING".equals(intent.getAction())) {
                    LockSettingsService.this.mStorage.prefetchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, false);
                    }
                }
            }
        };
        this.mContext = context;
        this.mHandler = new Handler();
        this.mStrongAuth = new LockSettingsStrongAuth(context);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mFirstCallToVold = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_STARTING");
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mStorage = new LockSettingsStorage(context, new Callback() {
            public void initialize(SQLiteDatabase db) {
                if (SystemProperties.getBoolean("ro.lockscreen.disable.default", false)) {
                    LockSettingsService.this.mStorage.writeKeyValue(db, "lockscreen.disabled", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, 0);
                }
            }
        });
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mStrongAuthTracker = new SynchronizedStrongAuthTracker(this.mContext);
        this.mStrongAuthTracker.register();
    }

    private void maybeShowEncryptionNotifications() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            UserHandle userHandle = user.getUserHandle();
            if ((!this.mStorage.hasPassword(user.id) ? this.mStorage.hasPattern(user.id) : true) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle) && user.isManagedProfile()) {
                UserInfo parent = this.mUserManager.getProfileParent(user.id);
                if (!(parent == null || !this.mUserManager.isUserUnlockingOrUnlocked(parent.getUserHandle()) || this.mUserManager.isQuietModeEnabled(userHandle))) {
                    showEncryptionNotificationForProfile(userHandle);
                }
            }
        }
    }

    private void showEncryptionNotificationForProfile(UserHandle user) {
        Resources r = this.mContext.getResources();
        CharSequence title = r.getText(17040888);
        CharSequence message = r.getText(17040892);
        CharSequence detail = r.getText(17040891);
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, user.getIdentifier());
        if (unlockIntent != null) {
            unlockIntent.setFlags(276824064);
            showEncryptionNotification(user, title, message, detail, PendingIntent.getActivity(this.mContext, 0, unlockIntent, 134217728));
        }
    }

    private void showEncryptionNotification(UserHandle user, CharSequence title, CharSequence message, CharSequence detail, PendingIntent intent) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            this.mNotificationManager.notifyAsUser(null, 0, new Builder(this.mContext).setSmallIcon(17302600).setWhen(0).setOngoing(true).setTicker(title).setDefaults(0).setPriority(2).setColor(this.mContext.getColor(17170523)).setContentTitle(title).setContentText(message).setSubText(detail).setVisibility(1).setContentIntent(intent).build(), user);
        }
    }

    public void hideEncryptionNotification(UserHandle userHandle) {
        this.mNotificationManager.cancelAsUser(null, 0, userHandle);
    }

    public void onCleanupUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
    }

    public void onUnlockUser(final int userId) {
        hideEncryptionNotification(new UserHandle(userId));
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    LockSettingsService.this.tieManagedProfileLockIfNecessary(userId, null);
                }
            });
        }
        List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
        for (int i = 0; i < profiles.size(); i++) {
            UserInfo profile = (UserInfo) profiles.get(i);
            if ((!this.mStorage.hasPassword(profile.id) ? this.mStorage.hasPattern(profile.id) : true) && profile.isManagedProfile()) {
                UserHandle userHandle = profile.getUserHandle();
                if (!(this.mUserManager.isUserUnlockingOrUnlocked(userHandle) || this.mUserManager.isQuietModeEnabled(userHandle))) {
                    showEncryptionNotificationForProfile(userHandle);
                }
            }
        }
    }

    public void systemReady() {
        migrateOldData();
        try {
            getGateKeeperService();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failure retrieving IGateKeeperService", e);
        }
        this.mStorage.prefetchUser(0);
        this.mIsQualcommTimeoutFlagOn = this.mLockPatternUtils.isQualcommTimeoutFlagOn(0);
        byte[] pw = new byte[]{(byte) 49, (byte) 49, (byte) 49, (byte) 49, (byte) 49, (byte) 49};
        if (this.mIsQualcommTimeoutFlagOn) {
            try {
                GateKeeperResponse gateKeeperResponse = getGateKeeperService().verify(0, getCurrentHandle(0), pw);
                if (gateKeeperResponse.getResponseCode() == 1) {
                    this.mLockPatternUtils.setLockoutAttemptDeadline(0, gateKeeperResponse.getTimeout());
                } else {
                    clearQualcommTimeoutFlag();
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "RemoteException e= " + e2);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Missing block: B:88:0x0363, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:89:0x0364, code:
            android.util.Slog.e(TAG, "Unable to remove tied profile key", r13);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void migrateOldData() {
        try {
            ContentResolver cr;
            List<UserInfo> users;
            int userId;
            int i;
            if (getString("migrated", null, 0) == null) {
                cr = this.mContext.getContentResolver();
                for (String validSetting : VALID_SETTINGS) {
                    String value = Secure.getString(cr, validSetting);
                    if (value != null) {
                        setString(validSetting, value, 0);
                    }
                }
                setString("migrated", "true", 0);
                Slog.i(TAG, "Migrated lock settings to new location");
            }
            if (getString("migrated_user_specific", null, 0) == null) {
                cr = this.mContext.getContentResolver();
                users = this.mUserManager.getUsers();
                for (int user = 0; user < users.size(); user++) {
                    userId = ((UserInfo) users.get(user)).id;
                    String OWNER_INFO = "lock_screen_owner_info";
                    String ownerInfo = Secure.getStringForUser(cr, "lock_screen_owner_info", userId);
                    if (!TextUtils.isEmpty(ownerInfo)) {
                        setString("lock_screen_owner_info", ownerInfo, userId);
                        Secure.putStringForUser(cr, "lock_screen_owner_info", IElsaManager.EMPTY_PACKAGE, userId);
                    }
                    String OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
                    try {
                        setLong("lock_screen_owner_info_enabled", (long) (Secure.getIntForUser(cr, "lock_screen_owner_info_enabled", userId) != 0 ? 1 : 0), userId);
                    } catch (SettingNotFoundException e) {
                        if (!TextUtils.isEmpty(ownerInfo)) {
                            setLong("lock_screen_owner_info_enabled", 1, userId);
                        }
                    }
                    Secure.putIntForUser(cr, "lock_screen_owner_info_enabled", 0, userId);
                }
                setString("migrated_user_specific", "true", 0);
                Slog.i(TAG, "Migrated per-user lock settings to new location");
            }
            if (getString("migrated_biometric_weak", null, 0) == null) {
                users = this.mUserManager.getUsers();
                for (i = 0; i < users.size(); i++) {
                    userId = ((UserInfo) users.get(i)).id;
                    long type = getLong("lockscreen.password_type", 0, userId);
                    long alternateType = getLong("lockscreen.password_type_alternate", 0, userId);
                    if (type == 32768) {
                        setLong("lockscreen.password_type", alternateType, userId);
                    }
                    setLong("lockscreen.password_type_alternate", 0, userId);
                }
                setString("migrated_biometric_weak", "true", 0);
                Slog.i(TAG, "Migrated biometric weak to use the fallback instead");
            }
            if (getString("migrated_lockscreen_disabled", null, 0) == null) {
                users = this.mUserManager.getUsers();
                int userCount = users.size();
                int switchableUsers = 0;
                for (i = 0; i < userCount; i++) {
                    if (((UserInfo) users.get(i)).supportsSwitchTo()) {
                        switchableUsers++;
                    }
                }
                if (switchableUsers > 1) {
                    for (i = 0; i < userCount; i++) {
                        int id = ((UserInfo) users.get(i)).id;
                        if (getBoolean("lockscreen.disabled", false, id)) {
                            setBoolean("lockscreen.disabled", false, id);
                        }
                    }
                }
                setString("migrated_lockscreen_disabled", "true", 0);
                Slog.i(TAG, "Migrated lockscreen disabled flag");
            }
            users = this.mUserManager.getUsers();
            for (i = 0; i < users.size(); i++) {
                UserInfo userInfo = (UserInfo) users.get(i);
                if (userInfo.isManagedProfile() && this.mStorage.hasChildProfileLock(userInfo.id)) {
                    long quality = getLong("lockscreen.password_type", 0, userInfo.id);
                    if (quality == 0) {
                        Slog.i(TAG, "Migrated tied profile lock type");
                        setLong("lockscreen.password_type", 327680, userInfo.id);
                    } else if (quality != 327680) {
                        Slog.e(TAG, "Invalid tied profile lock type: " + quality);
                    }
                }
                try {
                    String alias = "profile_key_name_encrypt_" + userInfo.id;
                    java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                    if (keyStore.containsAlias(alias)) {
                        keyStore.deleteEntry(alias);
                    }
                } catch (Exception e2) {
                }
            }
        } catch (Throwable re) {
            Slog.e(TAG, "Unable to migrate old data", re);
        }
    }

    private final void checkWritePermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsWrite");
    }

    private final void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsRead");
    }

    private final void checkReadPermission(String requestedKey, int userId) {
        int callingUid = Binder.getCallingUid();
        int i = 0;
        while (i < READ_CONTACTS_PROTECTED_SETTINGS.length) {
            if (!READ_CONTACTS_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_CONTACTS) == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + OppoPermissionConstants.PERMISSION_READ_CONTACTS + " to read " + requestedKey + " for user " + userId);
            }
        }
        i = 0;
        while (i < READ_PASSWORD_PROTECTED_SETTINGS.length) {
            if (!READ_PASSWORD_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + PERMISSION + " to read " + requestedKey + " for user " + userId);
            }
        }
    }

    public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
        boolean z;
        checkReadPermission(SEPARATE_PROFILE_CHALLENGE_KEY, userId);
        synchronized (this.mSeparateChallengeLock) {
            z = getBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, false, userId);
        }
        return z;
    }

    public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, enabled, userId);
            if (enabled) {
                this.mStorage.removeChildProfileLock(userId);
                removeKeystoreProfileKey(userId);
            } else {
                tieManagedProfileLockIfNecessary(userId, managedUserPassword);
            }
        }
    }

    public void setBoolean(String key, boolean value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
    }

    public void setLong(String key, long value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, Long.toString(value));
    }

    public void setString(String key, String value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value);
    }

    private void setStringUnchecked(String key, int userId, String value) {
        this.mStorage.writeKeyValue(key, value, userId);
        if (ArrayUtils.contains(SETTINGS_TO_BACKUP, key)) {
            BackupManager.dataChanged("com.android.providers.settings");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return !value.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) ? value.equals("true") : true;
    }

    public long getLong(String key, long defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        return TextUtils.isEmpty(value) ? defaultValue : Long.parseLong(value);
    }

    public String getString(String key, String defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        return getStringUnchecked(key, defaultValue, userId);
    }

    public String getStringUnchecked(String key, String defaultValue, int userId) {
        if ("lock_pattern_autolock".equals(key)) {
            long ident = Binder.clearCallingIdentity();
            try {
                String str = this.mLockPatternUtils.isLockPatternEnabled(userId) ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
                Binder.restoreCallingIdentity(ident);
                return str;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            if ("legacy_lock_pattern_enabled".equals(key)) {
                key = "lock_pattern_autolock";
            }
            return this.mStorage.readKeyValue(key, defaultValue, userId);
        }
    }

    public boolean havePassword(int userId) throws RemoteException {
        return this.mStorage.hasPassword(userId);
    }

    public boolean havePattern(int userId) throws RemoteException {
        return this.mStorage.hasPattern(userId);
    }

    private void setKeystorePassword(String password, int userHandle) {
        KeyStore.getInstance().onUserPasswordChanged(userHandle, password);
    }

    private void unlockKeystore(String password, int userHandle) {
        KeyStore.getInstance().unlock(userHandle, password);
    }

    private String getDecryptedPasswordForTiedProfile(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = this.mStorage.readChildProfileLock(userId);
        if (storedData == null) {
            throw new FileNotFoundException("Child profile lock file not found");
        }
        byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
        byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey decryptionKey = (SecretKey) keyStore.getKey("profile_key_name_decrypt_" + userId, null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, decryptionKey, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x000e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0011, code:
            if ((r0 instanceof java.io.FileNotFoundException) != false) goto L_0x0013;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            android.util.Slog.i(TAG, "Child profile key not found");
     */
    /* JADX WARNING: Missing block: B:6:0x001d, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r0);
     */
    /* JADX WARNING: Missing block: B:8:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void unlockChildProfile(int profileHandle) throws RemoteException {
        try {
            doVerifyPassword(getDecryptedPasswordForTiedProfile(profileHandle), false, 0, profileHandle, null);
        } catch (Exception e) {
        }
    }

    private void unlockUser(int userId, byte[] token, byte[] secret) {
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            ActivityManagerNative.getDefault().unlockUser(userId, token, secret, new IProgressListener.Stub() {
                public void onStarted(int id, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser started");
                }

                public void onProgress(int id, int progress, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser progress " + progress);
                }

                public void onFinished(int id, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser finished");
                    latch.countDown();
                }
            });
            try {
                latch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                if (!this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                    for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
                        if (pi.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) && this.mStorage.hasChildProfileLock(pi.id)) {
                            unlockChildProfile(pi.id);
                        }
                    }
                }
            } catch (RemoteException e2) {
                Log.d(TAG, "Failed to unlock child profile", e2);
            }
        } catch (RemoteException e22) {
            throw e22.rethrowAsRuntimeException();
        }
    }

    private byte[] getCurrentHandle(int userId) {
        byte[] currentHandle;
        int currentHandleType = this.mStorage.getStoredCredentialType(userId);
        CredentialHash credential;
        switch (currentHandleType) {
            case 1:
                credential = this.mStorage.readPatternHash(userId);
                if (credential == null) {
                    currentHandle = null;
                    break;
                }
                currentHandle = credential.hash;
                break;
            case 2:
                credential = this.mStorage.readPasswordHash(userId);
                if (credential == null) {
                    currentHandle = null;
                    break;
                }
                currentHandle = credential.hash;
                break;
            default:
                currentHandle = null;
                break;
        }
        if (currentHandleType != -1 && currentHandle == null) {
            Slog.e(TAG, "Stored handle type [" + currentHandleType + "] but no handle available");
        }
        return currentHandle;
    }

    private void onUserLockChanged(int userId) throws RemoteException {
        if (!this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            boolean isSecure = !this.mStorage.hasPassword(userId) ? this.mStorage.hasPattern(userId) : true;
            List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
            int size = profiles.size();
            for (int i = 0; i < size; i++) {
                UserInfo profile = (UserInfo) profiles.get(i);
                if (profile.isManagedProfile()) {
                    int managedUserId = profile.id;
                    if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId)) {
                        if (isSecure) {
                            tieManagedProfileLockIfNecessary(managedUserId, null);
                        } else {
                            clearUserKeyProtection(managedUserId);
                            getGateKeeperService().clearSecureUserId(managedUserId);
                            this.mStorage.writePatternHash(null, managedUserId);
                            setKeystorePassword(null, managedUserId);
                            fixateNewestUserKeyAuth(managedUserId);
                            this.mStorage.removeChildProfileLock(managedUserId);
                            removeKeystoreProfileKey(managedUserId);
                        }
                    }
                }
            }
        }
    }

    private boolean isManagedProfileWithUnifiedLock(int userId) {
        if (!this.mUserManager.getUserInfo(userId).isManagedProfile() || this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
            return false;
        }
        return true;
    }

    private boolean isManagedProfileWithSeparatedLock(int userId) {
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId);
        }
        return false;
    }

    public void setLockPattern(String pattern, String savedCredential, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setLockPatternInternal(pattern, savedCredential, userId);
            setSeparateProfileChallengeEnabled(userId, true, null);
            notifyPasswordChanged(userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x0051, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x0054, code:
            if ((r9 instanceof java.io.FileNotFoundException) != false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:15:0x0056, code:
            android.util.Slog.i(TAG, "Child profile key not found");
     */
    /* JADX WARNING: Missing block: B:16:0x0060, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setLockPatternInternal(String pattern, String savedCredential, int userId) throws RemoteException {
        byte[] currentHandle = getCurrentHandle(userId);
        if (pattern == null) {
            clearUserKeyProtection(userId);
            getGateKeeperService().clearSecureUserId(userId);
            this.mStorage.writePatternHash(null, userId);
            setKeystorePassword(null, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            notifyActivePasswordMetricsAvailable(null, userId);
            return;
        }
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (Exception e) {
            }
        } else if (currentHandle == null) {
            if (savedCredential != null) {
                Slog.w(TAG, "Saved credential provided, but none stored");
            }
            savedCredential = null;
        }
        byte[] enrolledHandle = enrollCredential(currentHandle, savedCredential, pattern, userId);
        if (enrolledHandle != null) {
            setUserKeyProtection(userId, pattern, doVerifyPattern(pattern, new CredentialHash(enrolledHandle, 1), true, 0, userId, null));
            this.mStorage.writePatternHash(enrolledHandle, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            return;
        }
        throw new RemoteException("Failed to enroll pattern");
    }

    public void setLockPassword(String password, String savedCredential, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setLockPasswordInternal(password, savedCredential, userId);
            setSeparateProfileChallengeEnabled(userId, true, null);
            notifyPasswordChanged(userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Splitter: B:6:0x002a, ExcHandler: java.security.UnrecoverableKeyException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x0051, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x0052, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r10);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setLockPasswordInternal(String password, String savedCredential, int userId) throws RemoteException {
        byte[] currentHandle = getCurrentHandle(userId);
        if (password == null) {
            clearUserKeyProtection(userId);
            getGateKeeperService().clearSecureUserId(userId);
            this.mStorage.writePasswordHash(null, userId);
            setKeystorePassword(null, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            notifyActivePasswordMetricsAvailable(null, userId);
            return;
        }
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "Child profile key not found");
            } catch (Exception e2) {
            }
        } else if (currentHandle == null) {
            if (savedCredential != null) {
                Slog.w(TAG, "Saved credential provided, but none stored");
            }
            savedCredential = null;
        }
        byte[] enrolledHandle = enrollCredential(currentHandle, savedCredential, password, userId);
        if (enrolledHandle != null) {
            setUserKeyProtection(userId, password, doVerifyPassword(password, new CredentialHash(enrolledHandle, 1), true, 0, userId, null));
            this.mStorage.writePasswordHash(enrolledHandle, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            return;
        }
        throw new RemoteException("Failed to enroll password");
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x014c A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x014c, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x0155, code:
            throw new java.lang.RuntimeException("Failed to encrypt key", r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void tieProfileLockToParent(int userId, String password) {
        byte[] randomLockSeed = password.getBytes(StandardCharsets.UTF_8);
        java.security.KeyStore keyStore;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String str = "profile_key_name_encrypt_" + userId;
            Entry secretKeyEntry = new SecretKeyEntry(secretKey);
            KeyProtection.Builder builder = new KeyProtection.Builder(1);
            String[] strArr = new String[1];
            strArr[0] = "GCM";
            builder = builder.setBlockModes(strArr);
            strArr = new String[1];
            strArr[0] = "NoPadding";
            keyStore.setEntry(str, secretKeyEntry, builder.setEncryptionPaddings(strArr).build());
            str = "profile_key_name_decrypt_" + userId;
            secretKeyEntry = new SecretKeyEntry(secretKey);
            builder = new KeyProtection.Builder(2);
            strArr = new String[1];
            strArr[0] = "GCM";
            builder = builder.setBlockModes(strArr);
            strArr = new String[1];
            strArr[0] = "NoPadding";
            keyStore.setEntry(str, secretKeyEntry, builder.setEncryptionPaddings(strArr).setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(30).build());
            SecretKey keyStoreEncryptionKey = (SecretKey) keyStore.getKey("profile_key_name_encrypt_" + userId, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, keyStoreEncryptionKey);
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                if (iv.length != 12) {
                    throw new RuntimeException("Invalid iv length: " + iv.length);
                }
                outputStream.write(iv);
                outputStream.write(encryptionResult);
                this.mStorage.writeChildProfileLock(userId, outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to concatenate byte arrays", e);
            }
        } catch (Exception e2) {
        } catch (Throwable th) {
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
        }
    }

    private byte[] enrollCredential(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] enrolledCredentialBytes;
        byte[] toEnrollBytes;
        checkWritePermission(userId);
        if (enrolledCredential == null) {
            enrolledCredentialBytes = null;
        } else {
            enrolledCredentialBytes = enrolledCredential.getBytes();
        }
        if (toEnroll == null) {
            toEnrollBytes = null;
        } else {
            toEnrollBytes = toEnroll.getBytes();
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredentialBytes, toEnrollBytes);
        if (response == null) {
            return null;
        }
        byte[] hash = response.getPayload();
        if (hash != null) {
            setKeystorePassword(toEnroll, userId);
        } else {
            Slog.e(TAG, "Throttled while enrolling a password");
        }
        return hash;
    }

    private void setUserKeyProtection(int userId, String credential, VerifyCredentialResponse vcr) throws RemoteException {
        if (vcr == null) {
            throw new RemoteException("Null response verifying a credential we just set");
        } else if (vcr.getResponseCode() != 0) {
            throw new RemoteException("Non-OK response verifying a credential we just set: " + vcr.getResponseCode());
        } else {
            byte[] token = vcr.getPayload();
            if (token == null) {
                throw new RemoteException("Empty payload verifying a credential we just set");
            }
            addUserKeyAuth(userId, token, secretFromCredential(credential));
        }
    }

    private void clearUserKeyProtection(int userId) throws RemoteException {
        addUserKeyAuth(userId, null, null);
    }

    private static byte[] secretFromCredential(String credential) throws RemoteException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(Arrays.copyOf("Android FBE credential hash".getBytes(StandardCharsets.UTF_8), 128));
            digest.update(credential.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512");
        }
    }

    private void addUserKeyAuth(int userId, byte[] token, byte[] secret) throws RemoteException {
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(userId);
        IMountService mountService = getMountService();
        long callingId = Binder.clearCallingIdentity();
        try {
            mountService.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        IMountService mountService = getMountService();
        long callingId = Binder.clearCallingIdentity();
        try {
            mountService.fixateNewestUserKeyAuth(userId);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073 A:{Splitter: B:11:0x0040, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x0073, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0074, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resetKeyStore(int userId) throws RemoteException {
        checkWritePermission(userId);
        int managedUserId = -1;
        String managedUserDecryptedPassword = null;
        for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
            if (pi.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) && this.mStorage.hasChildProfileLock(pi.id)) {
                if (managedUserId == -1) {
                    try {
                        managedUserDecryptedPassword = getDecryptedPasswordForTiedProfile(pi.id);
                        managedUserId = pi.id;
                    } catch (Exception e) {
                    }
                } else {
                    Slog.e(TAG, "More than one managed profile, uid1:" + managedUserId + ", uid2:" + pi.id);
                }
            }
        }
        try {
            for (int profileId : this.mUserManager.getProfileIdsWithDisabled(userId)) {
                for (int uid : SYSTEM_CREDENTIAL_UIDS) {
                    this.mKeyStore.clearUid(UserHandle.getUid(profileId, uid));
                }
            }
        } finally {
            if (!(managedUserId == -1 || managedUserDecryptedPassword == null)) {
                tieProfileLockToParent(managedUserId, managedUserDecryptedPassword);
            }
        }
    }

    public VerifyCredentialResponse checkPattern(String pattern, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        return doVerifyPattern(pattern, false, 0, userId, progressCallback);
    }

    public VerifyCredentialResponse verifyPattern(String pattern, long challenge, int userId) throws RemoteException {
        return doVerifyPattern(pattern, true, challenge, userId, null);
    }

    private VerifyCredentialResponse doVerifyPattern(String pattern, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (TextUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("Pattern can't be null or empty");
        }
        return doVerifyPattern(pattern, this.mStorage.readPatternHash(userId), hasChallenge, challenge, userId, progressCallback);
    }

    private VerifyCredentialResponse doVerifyPattern(String pattern, CredentialHash storedHash, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if (TextUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("Pattern can't be null or empty");
        }
        String patternToVerify;
        boolean shouldReEnrollBaseZero = storedHash != null ? storedHash.isBaseZeroPattern : false;
        if (shouldReEnrollBaseZero) {
            patternToVerify = LockPatternUtils.patternStringToBaseZero(pattern);
        } else {
            patternToVerify = pattern;
        }
        VerifyCredentialResponse response = verifyCredential(userId, storedHash, patternToVerify, hasChallenge, challenge, new CredentialUtil() {
            public void setCredential(String pattern, String oldPattern, int userId) throws RemoteException {
                LockSettingsService.this.setLockPatternInternal(pattern, oldPattern, userId);
            }

            public byte[] toHash(String pattern, int userId) {
                return LockPatternUtils.patternToHash(LockPatternUtils.stringToPattern(pattern));
            }

            public String adjustForKeystore(String pattern) {
                return LockPatternUtils.patternStringToBaseZero(pattern);
            }
        }, progressCallback);
        if (response.getResponseCode() == 0 && shouldReEnrollBaseZero) {
            setLockPatternInternal(pattern, patternToVerify, userId);
        }
        return response;
    }

    public VerifyCredentialResponse checkPassword(String password, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        return doVerifyPassword(password, false, 0, userId, progressCallback);
    }

    public VerifyCredentialResponse verifyPassword(String password, long challenge, int userId) throws RemoteException {
        return doVerifyPassword(password, true, challenge, userId, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x005d A:{Splitter: B:11:0x0048, ExcHandler: java.security.UnrecoverableKeyException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x005d, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x005e, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r2);
     */
    /* JADX WARNING: Missing block: B:16:0x006f, code:
            throw new android.os.RemoteException("Unable to get tied profile token");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public VerifyCredentialResponse verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (isManagedProfileWithUnifiedLock(userId)) {
            VerifyCredentialResponse parentResponse;
            int parentProfileId = this.mUserManager.getProfileParent(userId).id;
            if (isPattern) {
                parentResponse = doVerifyPattern(password, true, challenge, parentProfileId, null);
            } else {
                parentResponse = doVerifyPassword(password, true, challenge, parentProfileId, null);
            }
            if (parentResponse.getResponseCode() != 0) {
                return parentResponse;
            }
            try {
                return doVerifyPassword(getDecryptedPasswordForTiedProfile(userId), true, challenge, userId, null);
            } catch (Exception e) {
            }
        } else {
            throw new RemoteException("User id must be managed profile with unified lock");
        }
    }

    private VerifyCredentialResponse doVerifyPassword(String password, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password can't be null or empty");
        }
        return doVerifyPassword(password, this.mStorage.readPasswordHash(userId), hasChallenge, challenge, userId, progressCallback);
    }

    private VerifyCredentialResponse doVerifyPassword(String password, CredentialHash storedHash, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password can't be null or empty");
        }
        return verifyCredential(userId, storedHash, password, hasChallenge, challenge, new CredentialUtil() {
            public void setCredential(String password, String oldPassword, int userId) throws RemoteException {
                LockSettingsService.this.setLockPasswordInternal(password, oldPassword, userId);
            }

            public byte[] toHash(String password, int userId) {
                return LockSettingsService.this.mLockPatternUtils.passwordToHash(password, userId);
            }

            public String adjustForKeystore(String password) {
                return password;
            }
        }, progressCallback);
    }

    private VerifyCredentialResponse verifyCredential(int userId, CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, CredentialUtil credentialUtil, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            return VerifyCredentialResponse.OK;
        }
        if (TextUtils.isEmpty(credential)) {
            return VerifyCredentialResponse.ERROR;
        }
        if (storedHash == null || storedHash.hash.length == 0) {
            return VerifyCredentialResponse.OK;
        }
        VerifyCredentialResponse response;
        if (storedHash.version == 0) {
            if (!Arrays.equals(credentialUtil.toHash(credential, userId), storedHash.hash)) {
                return VerifyCredentialResponse.ERROR;
            }
            unlockKeystore(credentialUtil.adjustForKeystore(credential), userId);
            Slog.i(TAG, "Unlocking user with fake token: " + userId);
            byte[] fakeToken = String.valueOf(userId).getBytes();
            unlockUser(userId, fakeToken, fakeToken);
            credentialUtil.setCredential(credential, null, userId);
            if (!hasChallenge) {
                notifyActivePasswordMetricsAvailable(credential, userId);
                return VerifyCredentialResponse.OK;
            }
        }
        boolean shouldReEnroll = false;
        GateKeeperResponse gateKeeperResponse = getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes());
        int responseCode = gateKeeperResponse.getResponseCode();
        if (responseCode == 1) {
            response = new VerifyCredentialResponse(gateKeeperResponse.getTimeout());
            if (gateKeeperResponse.getTimeout() > 0) {
                this.mIsQualcommTimeoutFlagOn = true;
                this.mLockPatternUtils.setQualcommTimeoutFlagOn(true, 0);
            }
        } else if (responseCode == 0) {
            byte[] token = gateKeeperResponse.getPayload();
            if (token == null) {
                Slog.e(TAG, "verifyChallenge response had no associated payload");
                response = VerifyCredentialResponse.ERROR;
            } else {
                shouldReEnroll = gateKeeperResponse.getShouldReEnroll();
                response = new VerifyCredentialResponse(token);
            }
            if (this.mIsQualcommTimeoutFlagOn) {
                clearQualcommTimeoutFlag();
            }
        } else {
            response = VerifyCredentialResponse.ERROR;
            if (this.mIsQualcommTimeoutFlagOn) {
                clearQualcommTimeoutFlag();
            }
        }
        if (response.getResponseCode() == 0) {
            if (progressCallback != null) {
                progressCallback.onCredentialVerified();
            }
            notifyActivePasswordMetricsAvailable(credential, userId);
            unlockKeystore(credential, userId);
            Slog.i(TAG, "Unlocking user " + userId + " with token length " + response.getPayload().length);
            unlockUser(userId, response.getPayload(), secretFromCredential(credential));
            if (isManagedProfileWithSeparatedLock(userId)) {
                ((TrustManager) this.mContext.getSystemService("trust")).setDeviceLockedForUser(userId, false);
            }
            if (shouldReEnroll) {
                credentialUtil.setCredential(credential, credential, userId);
            }
        } else if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
            requireStrongAuth(8, userId);
        }
        return response;
    }

    private void notifyActivePasswordMetricsAvailable(final String password, final int userId) {
        final int quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userId);
        this.mHandler.post(new Runnable() {
            public void run() {
                int length = 0;
                int letters = 0;
                int uppercase = 0;
                int lowercase = 0;
                int numbers = 0;
                int symbols = 0;
                int nonletter = 0;
                if (password != null) {
                    length = password.length();
                    for (int i = 0; i < length; i++) {
                        char c = password.charAt(i);
                        if (c >= 'A' && c <= 'Z') {
                            letters++;
                            uppercase++;
                        } else if (c >= 'a' && c <= 'z') {
                            letters++;
                            lowercase++;
                        } else if (c < '0' || c > '9') {
                            symbols++;
                            nonletter++;
                        } else {
                            numbers++;
                            nonletter++;
                        }
                    }
                }
                ((DevicePolicyManager) LockSettingsService.this.mContext.getSystemService("device_policy")).setActivePasswordState(quality, length, letters, uppercase, lowercase, numbers, symbols, nonletter, userId);
            }
        });
    }

    private void notifyPasswordChanged(int userId) {
        this.mHandler.post(new -void_notifyPasswordChanged_int_userId_LambdaImpl0(userId));
    }

    /* renamed from: -com_android_server_LockSettingsService_lambda$1 */
    /* synthetic */ void m7-com_android_server_LockSettingsService_lambda$1(int userId) {
        ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).reportPasswordChanged(userId);
    }

    public boolean checkVoldPassword(int userId) throws RemoteException {
        if (!this.mFirstCallToVold) {
            return false;
        }
        this.mFirstCallToVold = false;
        checkPasswordReadPermission(userId);
        IMountService service = getMountService();
        long identity = Binder.clearCallingIdentity();
        try {
            String password = service.getPassword();
            service.clearPassword();
            if (password == null) {
                return false;
            }
            try {
                if (this.mLockPatternUtils.isLockPatternEnabled(userId) && checkPattern(password, userId, null).getResponseCode() == 0) {
                    return true;
                }
            } catch (Exception e) {
            }
            try {
                return this.mLockPatternUtils.isLockPasswordEnabled(userId) && checkPassword(password, userId, null).getResponseCode() == 0;
            } catch (Exception e2) {
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeUser(int userId, boolean unknownUser) {
        this.mStorage.removeUser(userId);
        this.mStrongAuth.removeUser(userId);
        KeyStore.getInstance().onUserRemoved(userId);
        try {
            IGateKeeperService gk = getGateKeeperService();
            if (gk != null) {
                gk.clearSecureUserId(userId);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "unable to clear GK secure user id");
        }
        if (unknownUser || this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            removeKeystoreProfileKey(userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x003a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x003b, code:
            android.util.Slog.e(TAG, "Unable to remove keystore profile key for user:" + r6, r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeKeystoreProfileKey(int targetUserId) {
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry("profile_key_name_encrypt_" + targetUserId);
            keyStore.deleteEntry("profile_key_name_decrypt_" + targetUserId);
        } catch (Exception e) {
        }
    }

    public void registerStrongAuthTracker(IStrongAuthTracker tracker) {
        checkPasswordReadPermission(-1);
        this.mStrongAuth.registerStrongAuthTracker(tracker);
    }

    public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) {
        checkPasswordReadPermission(-1);
        this.mStrongAuth.unregisterStrongAuthTracker(tracker);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        checkWritePermission(userId);
        this.mStrongAuth.requireStrongAuth(strongAuthReason, userId);
    }

    public void userPresent(int userId) {
        checkWritePermission(userId);
        this.mStrongAuth.reportUnlock(userId);
    }

    public int getStrongAuthForUser(int userId) {
        checkPasswordReadPermission(userId);
        return this.mStrongAuthTracker.getStrongAuthForUser(userId);
    }

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR);
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    private synchronized IGateKeeperService getGateKeeperService() throws RemoteException {
        if (this.mGateKeeperService != null) {
            return this.mGateKeeperService;
        }
        IBinder service = ServiceManager.getService("android.service.gatekeeper.IGateKeeperService");
        if (service != null) {
            service.linkToDeath(new GateKeeperDiedRecipient(this, null), 0);
            this.mGateKeeperService = IGateKeeperService.Stub.asInterface(service);
            return this.mGateKeeperService;
        }
        Slog.e(TAG, "Unable to acquire GateKeeperService");
        return null;
    }

    private void clearQualcommTimeoutFlag() throws RemoteException {
        this.mIsQualcommTimeoutFlagOn = false;
        this.mLockPatternUtils.setQualcommTimeoutFlagOn(false, 0);
        setLong("lockscreen.lockoutattemptdeadline", 0, 0);
        setLong("lockscreen.lockoutattempttimeoutmss", 0, 0);
    }
}
