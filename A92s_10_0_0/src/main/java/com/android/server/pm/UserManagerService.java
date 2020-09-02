package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyEventLogger;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.security.GateKeeper;
import android.service.gatekeeper.IGateKeeperService;
import android.util.AtomicFile;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsService;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.SystemService;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.UserState;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.UserManagerService;
import com.android.server.storage.DeviceStorageMonitorInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UserManagerService extends OppoBaseUserManagerService {
    private static final int ALLOWED_FLAGS_FOR_CREATE_USERS_PERMISSION = 812;
    private static final String ATTR_CREATION_TIME = "created";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GUEST_TO_REMOVE = "guestToRemove";
    private static final String ATTR_ICON_PATH = "icon";
    private static final String ATTR_ID = "id";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_LAST_LOGGED_IN_FINGERPRINT = "lastLoggedInFingerprint";
    private static final String ATTR_LAST_LOGGED_IN_TIME = "lastLoggedIn";
    private static final String ATTR_MULTIPLE = "m";
    private static final String ATTR_NEXT_SERIAL_NO = "nextSerialNumber";
    private static final String ATTR_PARTIAL = "partial";
    private static final String ATTR_PROFILE_BADGE = "profileBadge";
    private static final String ATTR_PROFILE_GROUP_ID = "profileGroupId";
    private static final String ATTR_RESTRICTED_PROFILE_PARENT_ID = "restrictedProfileParentId";
    private static final String ATTR_SEED_ACCOUNT_NAME = "seedAccountName";
    private static final String ATTR_SEED_ACCOUNT_TYPE = "seedAccountType";
    private static final String ATTR_SERIAL_NO = "serialNumber";
    private static final String ATTR_TYPE_BOOLEAN = "b";
    private static final String ATTR_TYPE_BUNDLE = "B";
    private static final String ATTR_TYPE_BUNDLE_ARRAY = "BA";
    private static final String ATTR_TYPE_INTEGER = "i";
    private static final String ATTR_TYPE_STRING = "s";
    private static final String ATTR_TYPE_STRING_ARRAY = "sa";
    private static final String ATTR_USER_VERSION = "version";
    private static final String ATTR_VALUE_TYPE = "type";
    static final boolean DBG = false;
    private static final boolean DBG_WITH_STACKTRACE = false;
    private static final long EPOCH_PLUS_30_YEARS = 946080000000L;
    private static final String LOG_TAG = "UserManagerService";
    @VisibleForTesting
    static final int MAX_MANAGED_PROFILES = 1;
    @VisibleForTesting
    static final int MAX_RECENTLY_REMOVED_IDS_SIZE = 100;
    @VisibleForTesting
    static final int MAX_USER_ID = 21474;
    @VisibleForTesting
    static final int MIN_USER_ID = 10;
    private static final boolean RELEASE_DELETED_USER_ID = false;
    private static final String RESTRICTIONS_FILE_PREFIX = "res_";
    private static final String TAG_ACCOUNT = "account";
    private static final String TAG_DEVICE_OWNER_USER_ID = "deviceOwnerUserId";
    private static final String TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS = "device_policy_global_restrictions";
    private static final String TAG_DEVICE_POLICY_RESTRICTIONS = "device_policy_restrictions";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_GLOBAL_RESTRICTION_OWNER_ID = "globalRestrictionOwnerUserId";
    private static final String TAG_GUEST_RESTRICTIONS = "guestRestrictions";
    private static final String TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL = "lastRequestQuietModeEnabledCall";
    private static final String TAG_NAME = "name";
    private static final String TAG_RESTRICTIONS = "restrictions";
    private static final String TAG_SEED_ACCOUNT_OPTIONS = "seedAccountOptions";
    private static final String TAG_USER = "user";
    private static final String TAG_USERS = "users";
    private static final String TAG_VALUE = "value";
    private static final String TRON_DEMO_CREATED = "users_demo_created";
    private static final String TRON_GUEST_CREATED = "users_guest_created";
    private static final String TRON_USER_CREATED = "users_user_created";
    private static final String USER_INFO_DIR = ("system" + File.separator + "users");
    private static final String USER_LIST_FILENAME = "userlist.xml";
    private static final String USER_PHOTO_FILENAME = "photo.png";
    private static final String USER_PHOTO_FILENAME_TMP = "photo.png.tmp";
    private static final int USER_VERSION = 7;
    static final int WRITE_USER_DELAY = 2000;
    static final int WRITE_USER_MSG = 1;
    private static final String XML_SUFFIX = ".xml";
    /* access modifiers changed from: private */
    public static final IBinder mUserRestriconToken = new Binder();
    private static UserManagerService sInstance;
    private final String ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK;
    /* access modifiers changed from: private */
    public IAppOpsService mAppOpsService;
    private final Object mAppRestrictionsLock;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mAppliedUserRestrictions;
    /* access modifiers changed from: private */
    @GuardedBy({"mRestrictionsLock"})
    public final SparseArray<Bundle> mBaseUserRestrictions;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mCachedEffectiveUserRestrictions;
    /* access modifiers changed from: private */
    public final Context mContext;
    @GuardedBy({"mRestrictionsLock"})
    private int mDeviceOwnerUserId;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mDevicePolicyGlobalUserRestrictions;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mDevicePolicyLocalUserRestrictions;
    private final BroadcastReceiver mDisableQuietModeCallback;
    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public boolean mForceEphemeralUsers;
    @GuardedBy({"mGuestRestrictions"})
    private final Bundle mGuestRestrictions;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public boolean mIsDeviceManaged;
    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public final SparseBooleanArray mIsUserManaged;
    private final LocalService mLocalService;
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy({"mPackagesLock"})
    private int mNextSerialNumber;
    /* access modifiers changed from: private */
    public final Object mPackagesLock;
    private final PackageManagerService mPm;
    @GuardedBy({"mUsersLock"})
    private final LinkedList<Integer> mRecentlyRemovedIds;
    @GuardedBy({"mUsersLock"})
    private final SparseBooleanArray mRemovingUserIds;
    /* access modifiers changed from: private */
    public final Object mRestrictionsLock;
    private final UserDataPreparer mUserDataPreparer;
    @GuardedBy({"mUsersLock"})
    private int[] mUserIds;
    private final File mUserListFile;
    /* access modifiers changed from: private */
    @GuardedBy({"mUserRestrictionsListeners"})
    public final ArrayList<UserManagerInternal.UserRestrictionsListener> mUserRestrictionsListeners;
    /* access modifiers changed from: private */
    @GuardedBy({"mUserStates"})
    public final SparseIntArray mUserStates;
    private int mUserVersion;
    @GuardedBy({"mUsersLock"})
    private final SparseArray<UserData> mUsers;
    private final File mUsersDir;
    /* access modifiers changed from: private */
    public final Object mUsersLock;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class UserData {
        String account;
        UserInfo info;
        private long mLastRequestQuietModeEnabledMillis;
        boolean persistSeedData;
        String seedAccountName;
        PersistableBundle seedAccountOptions;
        String seedAccountType;
        long startRealtime;
        long unlockRealtime;

        UserData() {
        }

        /* access modifiers changed from: package-private */
        public void setLastRequestQuietModeEnabledMillis(long millis) {
            this.mLastRequestQuietModeEnabledMillis = millis;
        }

        /* access modifiers changed from: package-private */
        public long getLastRequestQuietModeEnabledMillis() {
            return this.mLastRequestQuietModeEnabledMillis;
        }

        /* access modifiers changed from: package-private */
        public void clearSeedAccountData() {
            this.seedAccountName = null;
            this.seedAccountType = null;
            this.seedAccountOptions = null;
            this.persistSeedData = false;
        }
    }

    private class DisableQuietModeUserUnlockedCallback extends IProgressListener.Stub {
        private final IntentSender mTarget;

        public DisableQuietModeUserUnlockedCallback(IntentSender target) {
            Preconditions.checkNotNull(target);
            this.mTarget = target;
        }

        public void onStarted(int id, Bundle extras) {
        }

        public void onProgress(int id, int progress, Bundle extras) {
        }

        public void onFinished(int id, Bundle extras) {
            try {
                UserManagerService.this.mContext.startIntentSender(this.mTarget, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Slog.e(UserManagerService.LOG_TAG, "Failed to start the target in the callback", e);
            }
        }
    }

    public static UserManagerService getInstance() {
        UserManagerService userManagerService;
        synchronized (UserManagerService.class) {
            userManagerService = sInstance;
        }
        return userManagerService;
    }

    public static class LifeCycle extends SystemService {
        private UserManagerService mUms;

        public LifeCycle(Context context) {
            super(context);
        }

        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.pm.UserManagerService, android.os.IBinder] */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mUms = UserManagerService.getInstance();
            publishBinderService(UserManagerService.TAG_USER, this.mUms);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mUms.cleanupPartialUsers();
            }
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.startRealtime = SystemClock.elapsedRealtime();
                }
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.unlockRealtime = SystemClock.elapsedRealtime();
                }
            }
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.startRealtime = 0;
                    user.unlockRealtime = 0;
                }
            }
        }
    }

    @VisibleForTesting
    UserManagerService(Context context) {
        this(context, null, null, new Object(), context.getCacheDir());
    }

    UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
        this(context, pm, userDataPreparer, packagesLock, Environment.getDataDirectory());
    }

    private UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock, File dataDir) {
        this.mUsersLock = LockGuard.installNewLock(2);
        this.mRestrictionsLock = new Object();
        this.mAppRestrictionsLock = new Object();
        this.mUsers = new SparseArray<>();
        this.mBaseUserRestrictions = new SparseArray<>();
        this.mCachedEffectiveUserRestrictions = new SparseArray<>();
        this.mAppliedUserRestrictions = new SparseArray<>();
        this.mDevicePolicyGlobalUserRestrictions = new SparseArray<>();
        this.mDeviceOwnerUserId = -10000;
        this.mDevicePolicyLocalUserRestrictions = new SparseArray<>();
        this.mGuestRestrictions = new Bundle();
        this.mRemovingUserIds = new SparseBooleanArray();
        this.mRecentlyRemovedIds = new LinkedList<>();
        this.mUserVersion = 0;
        this.mIsUserManaged = new SparseBooleanArray();
        this.mUserRestrictionsListeners = new ArrayList<>();
        this.ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK = "com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK";
        this.mDisableQuietModeCallback = new BroadcastReceiver() {
            /* class com.android.server.pm.UserManagerService.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK".equals(intent.getAction())) {
                    BackgroundThread.getHandler().post(new Runnable(intent.getIntExtra("android.intent.extra.USER_ID", -10000), (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT")) {
                        /* class com.android.server.pm.$$Lambda$UserManagerService$1$DQ_02g7kZ7QrJXO6aCATwE6DYCE */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ IntentSender f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            UserManagerService.AnonymousClass1.this.lambda$onReceive$0$UserManagerService$1(this.f$1, this.f$2);
                        }
                    });
                }
            }

            public /* synthetic */ void lambda$onReceive$0$UserManagerService$1(int userHandle, IntentSender target) {
                UserManagerService.this.setQuietModeEnabled(userHandle, false, target, null);
            }
        };
        this.mUserStates = new SparseIntArray();
        this.mContext = context;
        this.mPm = pm;
        this.mPackagesLock = packagesLock;
        this.mHandler = new MainHandler();
        this.mUserDataPreparer = userDataPreparer;
        synchronized (this.mPackagesLock) {
            this.mUsersDir = new File(dataDir, USER_INFO_DIR);
            this.mUsersDir.mkdirs();
            new File(this.mUsersDir, String.valueOf(0)).mkdirs();
            FileUtils.setPermissions(this.mUsersDir.toString(), 509, -1, -1);
            this.mUserListFile = new File(this.mUsersDir, USER_LIST_FILENAME);
            initDefaultGuestRestrictions();
            readUserListLP();
            sInstance = this;
        }
        this.mLocalService = new LocalService();
        LocalServices.addService(UserManagerInternal.class, this.mLocalService);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mUserStates.put(0, 0);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        synchronized (this.mRestrictionsLock) {
            applyUserRestrictionsLR(0);
            prePareMultiAppUser(this.mUserDataPreparer, this.mPm);
        }
        UserInfo currentGuestUser = findCurrentGuestUser();
        if (currentGuestUser != null && !hasUserRestriction("no_config_wifi", currentGuestUser.id)) {
            setUserRestriction("no_config_wifi", true, currentGuestUser.id);
        }
        this.mContext.registerReceiver(this.mDisableQuietModeCallback, new IntentFilter("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK"), null, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public void cleanupPartialUsers() {
        ArrayList<UserInfo> partials = new ArrayList<>();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if ((ui.partial || ui.guestToRemove || ui.isEphemeral()) && i != 0) {
                    partials.add(ui);
                    addRemovingUserIdLocked(ui.id);
                    ui.partial = true;
                }
            }
        }
        int partialsSize = partials.size();
        for (int i2 = 0; i2 < partialsSize; i2++) {
            UserInfo ui2 = partials.get(i2);
            Slog.w(LOG_TAG, "Removing partially created user " + ui2.id + " (name=" + ui2.name + ")");
            removeUserState(ui2.id);
        }
    }

    public String getUserAccount(int userId) {
        String str;
        checkManageUserAndAcrossUsersFullPermission("get user account");
        synchronized (this.mUsersLock) {
            str = this.mUsers.get(userId).account;
        }
        return str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003c, code lost:
        if (r0 == null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        writeUserLP(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        return;
     */
    public void setUserAccount(int userId, String accountName) {
        checkManageUserAndAcrossUsersFullPermission("set user account");
        UserData userToUpdate = null;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = this.mUsers.get(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "User not found for setting user account: u" + userId);
                } else if (!Objects.equals(userData.account, accountName)) {
                    userData.account = accountName;
                    userToUpdate = userData;
                }
            }
        }
    }

    public UserInfo getPrimaryUser() {
        checkManageUsersPermission("query users");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (ui.isPrimary() && !this.mRemovingUserIds.get(ui.id)) {
                    return ui;
                }
            }
            return null;
        }
    }

    public List<UserInfo> getUsers(boolean excludeDying) {
        ArrayList<UserInfo> users;
        checkManageOrCreateUsersPermission("query users");
        synchronized (this.mUsersLock) {
            users = new ArrayList<>(this.mUsers.size());
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (!ui.partial) {
                    if (!excludeDying || !this.mRemovingUserIds.get(ui.id)) {
                        users.add(userWithName(ui));
                    }
                }
            }
        }
        return users;
    }

    public List<UserInfo> getProfiles(int userId, boolean enabledOnly) {
        List<UserInfo> profilesLU;
        boolean returnFullInfo = true;
        if (userId != UserHandle.getCallingUserId()) {
            checkManageOrCreateUsersPermission("getting profiles related to user " + userId);
        } else {
            returnFullInfo = hasManageUsersPermission();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUsersLock) {
                profilesLU = getProfilesLU(userId, enabledOnly, returnFullInfo);
            }
            return profilesLU;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int[] getProfileIds(int userId, boolean enabledOnly) {
        int[] array;
        if (userId != UserHandle.getCallingUserId()) {
            checkManageOrCreateUsersPermission("getting profiles related to user " + userId);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUsersLock) {
                array = getProfileIdsLU(userId, enabledOnly).toArray();
            }
            return array;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @GuardedBy({"mUsersLock"})
    private List<UserInfo> getProfilesLU(int userId, boolean enabledOnly, boolean fullInfo) {
        UserInfo userInfo;
        IntArray profileIds = getProfileIdsLU(userId, enabledOnly);
        ArrayList<UserInfo> users = new ArrayList<>(profileIds.size());
        for (int i = 0; i < profileIds.size(); i++) {
            UserInfo userInfo2 = this.mUsers.get(profileIds.get(i)).info;
            if (!fullInfo) {
                userInfo = new UserInfo(userInfo2);
                userInfo.name = null;
                userInfo.iconPath = null;
            } else {
                userInfo = userWithName(userInfo2);
            }
            users.add(userInfo);
        }
        return users;
    }

    @GuardedBy({"mUsersLock"})
    private IntArray getProfileIdsLU(int userId, boolean enabledOnly) {
        UserInfo user = getUserInfoLU(userId);
        IntArray result = new IntArray(this.mUsers.size());
        if (user == null) {
            return result;
        }
        int userSize = this.mUsers.size();
        for (int i = 0; i < userSize; i++) {
            UserInfo profile = this.mUsers.valueAt(i).info;
            if (isProfileOf(user, profile) && ((!enabledOnly || profile.isEnabled()) && !this.mRemovingUserIds.get(profile.id) && !profile.partial)) {
                result.add(profile.id);
            }
        }
        return result;
    }

    public int getCredentialOwnerProfile(int userHandle) {
        checkManageUsersPermission("get the credential owner");
        int userHandle2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserId(userHandle);
        if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userHandle2)) {
            synchronized (this.mUsersLock) {
                UserInfo profileParent = getProfileParentLU(userHandle2);
                if (profileParent != null) {
                    int i = profileParent.id;
                    return i;
                }
            }
        }
        return userHandle2;
    }

    public boolean isSameProfileGroup(int userId, int otherUserId) {
        if (userId == otherUserId) {
            return true;
        }
        checkManageUsersPermission("check if in the same profile group");
        return isSameProfileGroupNoChecks(userId, otherUserId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0024, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0026, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0028, code lost:
        return false;
     */
    private boolean isSameProfileGroupNoChecks(int userId, int otherUserId) {
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            boolean z = false;
            if (userInfo != null) {
                if (userInfo.profileGroupId != -10000) {
                    UserInfo otherUserInfo = getUserInfoLU(otherUserId);
                    if (otherUserInfo != null) {
                        if (otherUserInfo.profileGroupId != -10000) {
                            if (userInfo.profileGroupId == otherUserInfo.profileGroupId) {
                                z = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public UserInfo getProfileParent(int userHandle) {
        UserInfo profileParentLU;
        checkManageUsersPermission("get the profile parent");
        synchronized (this.mUsersLock) {
            profileParentLU = getProfileParentLU(userHandle);
        }
        return profileParentLU;
    }

    public int getProfileParentId(int userHandle) {
        checkManageUsersPermission("get the profile parent");
        return this.mLocalService.getProfileParentId(userHandle);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public UserInfo getProfileParentLU(int userHandle) {
        int parentUserId;
        UserInfo profile = getUserInfoLU(userHandle);
        if (profile == null || (parentUserId = profile.profileGroupId) == userHandle || parentUserId == -10000) {
            return null;
        }
        return getUserInfoLU(parentUserId);
    }

    private static boolean isProfileOf(UserInfo user, UserInfo profile) {
        return user.id == profile.id || (user.profileGroupId != -10000 && user.profileGroupId == profile.profileGroupId);
    }

    private void broadcastProfileAvailabilityChanges(UserHandle profileHandle, UserHandle parentHandle, boolean inQuietMode) {
        Intent intent = new Intent();
        if (inQuietMode) {
            intent.setAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        } else {
            intent.setAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        }
        intent.putExtra("android.intent.extra.QUIET_MODE", inQuietMode);
        intent.putExtra("android.intent.extra.USER", profileHandle);
        intent.putExtra("android.intent.extra.user_handle", profileHandle.getIdentifier());
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, parentHandle);
    }

    public boolean requestQuietModeEnabled(String callingPackage, boolean enableQuietMode, int userHandle, IntentSender target) {
        Preconditions.checkNotNull(callingPackage);
        if (!enableQuietMode || target == null) {
            boolean needToShowConfirmCredential = true;
            ensureCanModifyQuietMode(callingPackage, Binder.getCallingUid(), target != null);
            long identity = Binder.clearCallingIdentity();
            boolean result = false;
            if (enableQuietMode) {
                try {
                    setQuietModeEnabled(userHandle, true, target, callingPackage);
                    result = true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else {
                if (!this.mLockPatternUtils.isSecure(userHandle) || StorageManager.isUserKeyUnlocked(userHandle)) {
                    needToShowConfirmCredential = false;
                }
                if (needToShowConfirmCredential) {
                    showConfirmCredentialToDisableQuietMode(userHandle, target);
                } else {
                    setQuietModeEnabled(userHandle, false, target, callingPackage);
                    result = true;
                }
            }
            Binder.restoreCallingIdentity(identity);
            return result;
        }
        throw new IllegalArgumentException("target should only be specified when we are disabling quiet mode.");
    }

    private void ensureCanModifyQuietMode(String callingPackage, int callingUid, boolean startIntent) {
        if (!hasManageUsersPermission()) {
            if (startIntent) {
                throw new SecurityException("MANAGE_USERS permission is required to start intent after disabling quiet mode.");
            } else if (!hasPermissionGranted("android.permission.MODIFY_QUIET_MODE", callingUid)) {
                verifyCallingPackage(callingPackage, callingUid);
                ShortcutServiceInternal shortcutInternal = (ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class);
                if (shortcutInternal == null || !shortcutInternal.isForegroundDefaultLauncher(callingPackage, callingUid)) {
                    throw new SecurityException("Can't modify quiet mode, caller is neither foreground default launcher nor has MANAGE_USERS/MODIFY_QUIET_MODE permission");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003e, code lost:
        r4 = r6.mPackagesLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        writeUserLP(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0044, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0045, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
        if (r8 == false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        android.app.ActivityManager.getService().stopUser(r7, true, (android.app.IStopUserCallback) null);
        ((android.app.ActivityManagerInternal) com.android.server.LocalServices.getService(android.app.ActivityManagerInternal.class)).killForegroundAppsForUser(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005e, code lost:
        if (r9 == null) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0060, code lost:
        r0 = new com.android.server.pm.UserManagerService.DisableQuietModeUserUnlockedCallback(r6, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
        android.app.ActivityManager.getService().startUserInBackgroundWithListener(r7, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        logQuietModeEnabled(r7, r8, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0073, code lost:
        r0.rethrowAsRuntimeException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0076, code lost:
        broadcastProfileAvailabilityChanges(r1.getUserHandle(), r2.getUserHandle(), r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0081, code lost:
        return;
     */
    public void setQuietModeEnabled(int userHandle, boolean enableQuietMode, IntentSender target, String callingPackage) {
        synchronized (this.mUsersLock) {
            UserInfo profile = getUserInfoLU(userHandle);
            UserInfo parent = getProfileParentLU(userHandle);
            if (profile == null || !profile.isManagedProfile()) {
                throw new IllegalArgumentException("User " + userHandle + " is not a profile");
            } else if (profile.isQuietModeEnabled() == enableQuietMode) {
                Slog.i(LOG_TAG, "Quiet mode is already " + enableQuietMode);
            } else {
                profile.flags ^= 128;
                UserData profileUserData = getUserDataLU(profile.id);
            }
        }
    }

    private void logQuietModeEnabled(int userHandle, boolean enableQuietMode, String callingPackage) {
        UserData userData;
        long period;
        synchronized (this.mUsersLock) {
            userData = getUserDataLU(userHandle);
        }
        if (userData != null) {
            long now = System.currentTimeMillis();
            if (userData.getLastRequestQuietModeEnabledMillis() != 0) {
                period = now - userData.getLastRequestQuietModeEnabledMillis();
            } else {
                period = now - userData.info.creationTime;
            }
            DevicePolicyEventLogger.createEvent(55).setStrings(new String[]{callingPackage}).setBoolean(enableQuietMode).setTimePeriod(period).write();
            userData.setLastRequestQuietModeEnabledMillis(now);
        }
    }

    public boolean isQuietModeEnabled(int userHandle) {
        UserInfo info;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userHandle);
            }
            if (info == null || !info.isManagedProfile()) {
                return false;
            }
            boolean isQuietModeEnabled = info.isQuietModeEnabled();
            return isQuietModeEnabled;
        }
    }

    private void showConfirmCredentialToDisableQuietMode(int userHandle, IntentSender target) {
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userHandle);
        if (unlockIntent != null) {
            Intent callBackIntent = new Intent("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK");
            if (target != null) {
                callBackIntent.putExtra("android.intent.extra.INTENT", target);
            }
            callBackIntent.putExtra("android.intent.extra.USER_ID", userHandle);
            callBackIntent.setPackage(this.mContext.getPackageName());
            callBackIntent.addFlags(268435456);
            unlockIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, callBackIntent, 1409286144).getIntentSender());
            unlockIntent.setFlags(276824064);
            this.mContext.startActivity(unlockIntent);
        }
    }

    public void setUserEnabled(int userId) {
        UserInfo info;
        checkManageUsersPermission("enable user");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userId);
            }
            if (info != null && !info.isEnabled()) {
                info.flags ^= 64;
                writeUserLP(getUserDataLU(info.id));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0039, code lost:
        return;
     */
    public void setUserAdmin(int userId) {
        UserInfo info;
        checkManageUserAndAcrossUsersFullPermission("set user admin");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userId);
            }
            if (info != null && !info.isAdmin()) {
                info.flags ^= 2;
                writeUserLP(getUserDataLU(info.id));
                setUserRestriction("no_sms", false, userId);
                setUserRestriction("no_outgoing_calls", false, userId);
            }
        }
    }

    public void evictCredentialEncryptionKey(int userId) {
        checkManageUsersPermission("evict CE key");
        IActivityManager am = ActivityManagerNative.getDefault();
        long identity = Binder.clearCallingIdentity();
        try {
            am.restartUserInBackground(userId);
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException re) {
            throw re.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public UserInfo getUserInfo(int userId) {
        UserInfo userWithName;
        checkManageOrCreateUsersPermission("query user");
        synchronized (this.mUsersLock) {
            userWithName = userWithName(getUserInfoLU(userId));
        }
        return userWithName;
    }

    private UserInfo userWithName(UserInfo orig) {
        if (orig == null || orig.name != null || orig.id != 0) {
            return orig;
        }
        UserInfo withName = new UserInfo(orig);
        withName.name = getOwnerName();
        return withName;
    }

    public int getManagedProfileBadge(int userId) {
        int i;
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "getManagedProfileBadge");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            i = userInfo != null ? userInfo.profileBadge : 0;
        }
        return i;
    }

    public boolean isManagedProfile(int userId) {
        boolean z;
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isManagedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            z = userInfo != null && userInfo.isManagedProfile();
        }
        return z;
    }

    public boolean isUserUnlockingOrUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlockingOrUnlocked");
        return this.mLocalService.isUserUnlockingOrUnlocked(userId);
    }

    public boolean isUserUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlocked");
        return this.mLocalService.isUserUnlocked(userId);
    }

    public boolean isUserRunning(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserRunning");
        return this.mLocalService.isUserRunning(userId);
    }

    public String getUserName() {
        String str;
        if (hasManageUsersOrPermission("android.permission.GET_ACCOUNTS_PRIVILEGED")) {
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mUsersLock) {
                UserInfo userInfo = userWithName(getUserInfoLU(userId));
                str = userInfo == null ? "" : userInfo.name;
            }
            return str;
        }
        throw new SecurityException("You need MANAGE_USERS or GET_ACCOUNTS_PRIVILEGED permissions to: get user name");
    }

    public long getUserStartRealtime() {
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mUsersLock) {
            UserData user = getUserDataLU(userId);
            if (user == null) {
                return 0;
            }
            long j = user.startRealtime;
            return j;
        }
    }

    public long getUserUnlockRealtime() {
        synchronized (this.mUsersLock) {
            UserData user = getUserDataLU(UserHandle.getUserId(Binder.getCallingUid()));
            if (user == null) {
                return 0;
            }
            long j = user.unlockRealtime;
            return j;
        }
    }

    private void checkManageOrInteractPermIfCallerInOtherProfileGroup(int userId, String name) {
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId != userId && !isSameProfileGroupNoChecks(callingUserId, userId) && !hasManageUsersPermission() && !hasPermissionGranted("android.permission.INTERACT_ACROSS_USERS", Binder.getCallingUid())) {
            throw new SecurityException("You need INTERACT_ACROSS_USERS or MANAGE_USERS permission to: check " + name);
        }
    }

    public boolean isDemoUser(int userId) {
        boolean z;
        if (UserHandle.getCallingUserId() == userId || hasManageUsersPermission()) {
            synchronized (this.mUsersLock) {
                UserInfo userInfo = getUserInfoLU(userId);
                z = userInfo != null && userInfo.isDemo();
            }
            return z;
        }
        throw new SecurityException("You need MANAGE_USERS permission to query if u=" + userId + " is a demo user");
    }

    public boolean isRestricted() {
        boolean isRestricted;
        synchronized (this.mUsersLock) {
            isRestricted = getUserInfoLU(UserHandle.getCallingUserId()).isRestricted();
        }
        return isRestricted;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002c, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
        return false;
     */
    public boolean canHaveRestrictedProfile(int userId) {
        checkManageUsersPermission("canHaveRestrictedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            boolean z = false;
            if (userInfo != null) {
                if (userInfo.canHaveProfile()) {
                    if (!userInfo.isAdmin()) {
                        return false;
                    }
                    if (!this.mIsDeviceManaged && !this.mIsUserManaged.get(userId)) {
                        z = true;
                    }
                }
            }
        }
    }

    public boolean hasRestrictedProfiles() {
        checkManageUsersPermission("hasRestrictedProfiles");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo profile = this.mUsers.valueAt(i).info;
                if (callingUserId != profile.id && profile.restrictedProfileParentId == callingUserId) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public UserInfo getUserInfoLU(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData != null && userData.info.partial && !this.mRemovingUserIds.get(userId)) {
            Slog.w(LOG_TAG, "getUserInfo: unknown user #" + userId);
            return null;
        } else if (userData != null) {
            return userData.info;
        } else {
            return null;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mUsersLock"})
    public UserData getUserDataLU(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData == null || !userData.info.partial || this.mRemovingUserIds.get(userId)) {
            return userData;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public UserInfo getUserInfoNoChecks(int userId) {
        UserInfo userInfo;
        synchronized (this.mUsersLock) {
            UserData userData = this.mUsers.get(userId);
            userInfo = userData != null ? userData.info : null;
        }
        return userInfo;
    }

    /* access modifiers changed from: private */
    public UserData getUserDataNoChecks(int userId) {
        UserData userData;
        synchronized (this.mUsersLock) {
            userData = this.mUsers.get(userId);
        }
        return userData;
    }

    public boolean exists(int userId) {
        return this.mLocalService.exists(userId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        r1 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        sendUserInfoChangedBroadcast(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        return;
     */
    public void setUserName(int userId, String name) {
        checkManageUsersPermission("rename users");
        boolean changed = false;
        synchronized (this.mPackagesLock) {
            UserData userData = getUserDataNoChecks(userId);
            if (userData != null) {
                if (!userData.info.partial) {
                    if (name != null && !name.equals(userData.info.name)) {
                        userData.info.name = name;
                        writeUserLP(userData);
                        changed = true;
                    }
                }
            }
            Slog.w(LOG_TAG, "setUserName: unknown user #" + userId);
        }
    }

    public void setUserIcon(int userId, Bitmap bitmap) {
        checkManageUsersPermission("update users");
        if (hasUserRestriction("no_set_user_icon", userId)) {
            Log.w(LOG_TAG, "Cannot set user icon. DISALLOW_SET_USER_ICON is enabled.");
        } else {
            this.mLocalService.setUserIcon(userId, bitmap);
        }
    }

    /* access modifiers changed from: private */
    public void sendUserInfoChangedBroadcast(int userId) {
        Intent changedIntent = new Intent("android.intent.action.USER_INFO_CHANGED");
        changedIntent.putExtra("android.intent.extra.user_handle", userId);
        changedIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changedIntent, UserHandle.ALL);
    }

    public ParcelFileDescriptor getUserIcon(int targetUserId) {
        if (hasManageUsersOrPermission("android.permission.GET_ACCOUNTS_PRIVILEGED")) {
            synchronized (this.mPackagesLock) {
                UserInfo targetUserInfo = getUserInfoNoChecks(targetUserId);
                if (targetUserInfo != null) {
                    if (!targetUserInfo.partial) {
                        int callingUserId = UserHandle.getCallingUserId();
                        int callingGroupId = getUserInfoNoChecks(callingUserId).profileGroupId;
                        boolean sameGroup = callingGroupId != -10000 && callingGroupId == targetUserInfo.profileGroupId;
                        if (callingUserId != targetUserId && !sameGroup) {
                            checkManageUsersPermission("get the icon of a user who is not related");
                        }
                        if (targetUserInfo.iconPath == null) {
                            return null;
                        }
                        String iconPath = targetUserInfo.iconPath;
                        try {
                            return ParcelFileDescriptor.open(new File(iconPath), 268435456);
                        } catch (FileNotFoundException e) {
                            Log.e(LOG_TAG, "Couldn't find icon file", e);
                            return null;
                        }
                    }
                }
                Slog.w(LOG_TAG, "getUserIcon: unknown user #" + targetUserId);
                return null;
            }
        }
        throw new SecurityException("You need MANAGE_USERS or GET_ACCOUNTS_PRIVILEGED permissions to: get user icon");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        scheduleWriteUser(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    public void makeInitialized(int userId) {
        checkManageUsersPermission("makeInitialized");
        boolean scheduleWriteUser = false;
        synchronized (this.mUsersLock) {
            UserData userData = this.mUsers.get(userId);
            if (userData != null) {
                if (!userData.info.partial) {
                    if ((userData.info.flags & 16) == 0) {
                        userData.info.flags |= 16;
                        scheduleWriteUser = true;
                    }
                }
            }
            Slog.w(LOG_TAG, "makeInitialized: unknown user #" + userId);
        }
    }

    private void initDefaultGuestRestrictions() {
        synchronized (this.mGuestRestrictions) {
            if (this.mGuestRestrictions.isEmpty()) {
                this.mGuestRestrictions.putBoolean("no_config_wifi", true);
                this.mGuestRestrictions.putBoolean("no_install_unknown_sources", true);
                this.mGuestRestrictions.putBoolean("no_outgoing_calls", true);
                this.mGuestRestrictions.putBoolean("no_sms", true);
            }
        }
    }

    public Bundle getDefaultGuestRestrictions() {
        Bundle bundle;
        checkManageUsersPermission("getDefaultGuestRestrictions");
        synchronized (this.mGuestRestrictions) {
            bundle = new Bundle(this.mGuestRestrictions);
        }
        return bundle;
    }

    public void setDefaultGuestRestrictions(Bundle restrictions) {
        checkManageUsersPermission("setDefaultGuestRestrictions");
        synchronized (this.mGuestRestrictions) {
            this.mGuestRestrictions.clear();
            this.mGuestRestrictions.putAll(restrictions);
        }
        synchronized (this.mPackagesLock) {
            writeUserListLP();
        }
    }

    /* access modifiers changed from: private */
    public void setDevicePolicyUserRestrictionsInner(int userId, Bundle restrictions, boolean isDeviceOwner, int cameraRestrictionScope) {
        boolean globalChanged;
        boolean localChanged;
        Bundle global = new Bundle();
        Bundle local = new Bundle();
        UserRestrictionsUtils.sortToGlobalAndLocal(restrictions, isDeviceOwner, cameraRestrictionScope, global, local);
        synchronized (this.mRestrictionsLock) {
            globalChanged = updateRestrictionsIfNeededLR(userId, global, this.mDevicePolicyGlobalUserRestrictions);
            localChanged = updateRestrictionsIfNeededLR(userId, local, this.mDevicePolicyLocalUserRestrictions);
            if (isDeviceOwner) {
                this.mDeviceOwnerUserId = userId;
            } else if (this.mDeviceOwnerUserId == userId) {
                this.mDeviceOwnerUserId = -10000;
            }
        }
        synchronized (this.mPackagesLock) {
            if (localChanged || globalChanged) {
                writeUserLP(getUserDataNoChecks(userId));
            }
        }
        synchronized (this.mRestrictionsLock) {
            if (globalChanged) {
                try {
                    applyUserRestrictionsForAllUsersLR();
                } catch (Throwable th) {
                    throw th;
                }
            } else if (localChanged) {
                applyUserRestrictionsLR(userId);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updateRestrictionsIfNeededLR(int userId, Bundle restrictions, SparseArray<Bundle> restrictionsArray) {
        boolean changed = !UserRestrictionsUtils.areEqual(restrictionsArray.get(userId), restrictions);
        if (changed) {
            if (!UserRestrictionsUtils.isEmpty(restrictions)) {
                restrictionsArray.put(userId, restrictions);
            } else {
                restrictionsArray.delete(userId);
            }
        }
        return changed;
    }

    @GuardedBy({"mRestrictionsLock"})
    private Bundle computeEffectiveUserRestrictionsLR(int userId) {
        Bundle baseRestrictions = UserRestrictionsUtils.nonNull(this.mBaseUserRestrictions.get(userId));
        Bundle global = UserRestrictionsUtils.mergeAll(this.mDevicePolicyGlobalUserRestrictions);
        Bundle local = this.mDevicePolicyLocalUserRestrictions.get(userId);
        if (UserRestrictionsUtils.isEmpty(global) && UserRestrictionsUtils.isEmpty(local)) {
            return baseRestrictions;
        }
        Bundle effective = UserRestrictionsUtils.clone(baseRestrictions);
        UserRestrictionsUtils.merge(effective, global);
        UserRestrictionsUtils.merge(effective, local);
        return effective;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mRestrictionsLock"})
    public void invalidateEffectiveUserRestrictionsLR(int userId) {
        this.mCachedEffectiveUserRestrictions.remove(userId);
    }

    private Bundle getEffectiveUserRestrictions(int userId) {
        Bundle restrictions;
        synchronized (this.mRestrictionsLock) {
            restrictions = this.mCachedEffectiveUserRestrictions.get(userId);
            if (restrictions == null) {
                restrictions = computeEffectiveUserRestrictionsLR(userId);
                this.mCachedEffectiveUserRestrictions.put(userId, restrictions);
            }
        }
        return restrictions;
    }

    public boolean hasUserRestriction(String restrictionKey, int userId) {
        Bundle restrictions;
        if (UserRestrictionsUtils.isValidRestriction(restrictionKey) && (restrictions = getEffectiveUserRestrictions(userId)) != null && restrictions.getBoolean(restrictionKey)) {
            return true;
        }
        return false;
    }

    public boolean hasUserRestrictionOnAnyUser(String restrictionKey) {
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        List<UserInfo> users = getUsers(true);
        for (int i = 0; i < users.size(); i++) {
            Bundle restrictions = getEffectiveUserRestrictions(users.get(i).id);
            if (restrictions != null && restrictions.getBoolean(restrictionKey)) {
                return true;
            }
        }
        return false;
    }

    public int getUserRestrictionSource(String restrictionKey, int userId) {
        List<UserManager.EnforcingUser> enforcingUsers = getUserRestrictionSources(restrictionKey, userId);
        int result = 0;
        for (int i = enforcingUsers.size() - 1; i >= 0; i--) {
            result |= enforcingUsers.get(i).getUserRestrictionSource();
        }
        return result;
    }

    public List<UserManager.EnforcingUser> getUserRestrictionSources(String restrictionKey, int userId) {
        checkManageUsersPermission("getUserRestrictionSource");
        if (!hasUserRestriction(restrictionKey, userId)) {
            return Collections.emptyList();
        }
        List<UserManager.EnforcingUser> result = new ArrayList<>();
        if (hasBaseUserRestriction(restrictionKey, userId)) {
            result.add(new UserManager.EnforcingUser(-10000, 1));
        }
        synchronized (this.mRestrictionsLock) {
            if (UserRestrictionsUtils.contains(this.mDevicePolicyLocalUserRestrictions.get(userId), restrictionKey)) {
                result.add(getEnforcingUserLocked(userId));
            }
            for (int i = this.mDevicePolicyGlobalUserRestrictions.size() - 1; i >= 0; i--) {
                int profileUserId = this.mDevicePolicyGlobalUserRestrictions.keyAt(i);
                if (UserRestrictionsUtils.contains(this.mDevicePolicyGlobalUserRestrictions.valueAt(i), restrictionKey)) {
                    result.add(getEnforcingUserLocked(profileUserId));
                }
            }
        }
        return result;
    }

    @GuardedBy({"mRestrictionsLock"})
    private UserManager.EnforcingUser getEnforcingUserLocked(int userId) {
        int source;
        if (this.mDeviceOwnerUserId == userId) {
            source = 2;
        } else {
            source = 4;
        }
        return new UserManager.EnforcingUser(userId, source);
    }

    public Bundle getUserRestrictions(int userId) {
        return UserRestrictionsUtils.clone(getEffectiveUserRestrictions(userId));
    }

    public boolean hasBaseUserRestriction(String restrictionKey, int userId) {
        checkManageUsersPermission("hasBaseUserRestriction");
        boolean z = false;
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        synchronized (this.mRestrictionsLock) {
            Bundle bundle = this.mBaseUserRestrictions.get(userId);
            if (bundle != null && bundle.getBoolean(restrictionKey, false)) {
                z = true;
            }
        }
        return z;
    }

    public void setUserRestriction(String key, boolean value, int userId) {
        checkManageUsersPermission("setUserRestriction");
        if (UserRestrictionsUtils.isValidRestriction(key)) {
            synchronized (this.mRestrictionsLock) {
                Bundle newRestrictions = UserRestrictionsUtils.clone(this.mBaseUserRestrictions.get(userId));
                newRestrictions.putBoolean(key, value);
                updateUserRestrictionsInternalLR(newRestrictions, userId);
            }
        }
    }

    @GuardedBy({"mRestrictionsLock"})
    private void updateUserRestrictionsInternalLR(Bundle newBaseRestrictions, final int userId) {
        Bundle prevAppliedRestrictions = UserRestrictionsUtils.nonNull(this.mAppliedUserRestrictions.get(userId));
        if (newBaseRestrictions != null) {
            boolean z = true;
            Preconditions.checkState(this.mBaseUserRestrictions.get(userId) != newBaseRestrictions);
            if (this.mCachedEffectiveUserRestrictions.get(userId) == newBaseRestrictions) {
                z = false;
            }
            Preconditions.checkState(z);
            if (updateRestrictionsIfNeededLR(userId, newBaseRestrictions, this.mBaseUserRestrictions)) {
                scheduleWriteUser(getUserDataNoChecks(userId));
            }
        }
        final Bundle effective = computeEffectiveUserRestrictionsLR(userId);
        this.mCachedEffectiveUserRestrictions.put(userId, effective);
        if (this.mAppOpsService != null) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.pm.UserManagerService.AnonymousClass2 */

                public void run() {
                    try {
                        UserManagerService.this.mAppOpsService.setUserRestrictions(effective, UserManagerService.mUserRestriconToken, userId);
                    } catch (RemoteException e) {
                        Log.w(UserManagerService.LOG_TAG, "Unable to notify AppOpsService of UserRestrictions");
                    }
                }
            });
        }
        propagateUserRestrictionsLR(userId, effective, prevAppliedRestrictions);
        this.mAppliedUserRestrictions.put(userId, new Bundle(effective));
    }

    private void propagateUserRestrictionsLR(final int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        if (!UserRestrictionsUtils.areEqual(newRestrictions, prevRestrictions)) {
            final Bundle newRestrictionsFinal = new Bundle(newRestrictions);
            final Bundle prevRestrictionsFinal = new Bundle(prevRestrictions);
            this.mHandler.post(new Runnable() {
                /* class com.android.server.pm.UserManagerService.AnonymousClass3 */

                public void run() {
                    UserManagerInternal.UserRestrictionsListener[] listeners;
                    UserRestrictionsUtils.applyUserRestrictions(UserManagerService.this.mContext, userId, newRestrictionsFinal, prevRestrictionsFinal);
                    synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                        listeners = new UserManagerInternal.UserRestrictionsListener[UserManagerService.this.mUserRestrictionsListeners.size()];
                        UserManagerService.this.mUserRestrictionsListeners.toArray(listeners);
                    }
                    for (UserManagerInternal.UserRestrictionsListener userRestrictionsListener : listeners) {
                        userRestrictionsListener.onUserRestrictionsChanged(userId, newRestrictionsFinal, prevRestrictionsFinal);
                    }
                    UserManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.os.action.USER_RESTRICTIONS_CHANGED").setFlags(1073741824), UserHandle.of(userId));
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mRestrictionsLock"})
    public void applyUserRestrictionsLR(int userId) {
        updateUserRestrictionsInternalLR(null, userId);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mRestrictionsLock"})
    public void applyUserRestrictionsForAllUsersLR() {
        this.mCachedEffectiveUserRestrictions.clear();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.pm.UserManagerService.AnonymousClass4 */

            public void run() {
                try {
                    int[] runningUsers = ActivityManager.getService().getRunningUserIds();
                    synchronized (UserManagerService.this.mRestrictionsLock) {
                        for (int i : runningUsers) {
                            UserManagerService.this.applyUserRestrictionsLR(i);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(UserManagerService.LOG_TAG, "Unable to access ActivityManagerService");
                }
            }
        });
    }

    private boolean isUserLimitReached() {
        int count;
        synchronized (this.mUsersLock) {
            count = getAliveUsersExcludingGuestsCountLU();
            if (hasMultiAppUser(this.mUserIds)) {
                count--;
            }
        }
        return count >= UserManager.getMaxSupportedUsers();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0055, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0057, code lost:
        return false;
     */
    public boolean canAddMoreManagedProfiles(int userId, boolean allowedToRemoveOne) {
        checkManageUsersPermission("check if more managed profiles can be added.");
        boolean z = false;
        if (ActivityManager.isLowRamDeviceStatic() || !this.mContext.getPackageManager().hasSystemFeature("android.software.managed_users")) {
            return false;
        }
        int managedProfilesCount = getProfiles(userId, false).size() - 1;
        int profilesRemovedCount = (managedProfilesCount <= 0 || !allowedToRemoveOne) ? 0 : 1;
        if (managedProfilesCount - profilesRemovedCount >= getMaxManagedProfiles()) {
            return false;
        }
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo != null) {
                if (userInfo.canHaveProfile()) {
                    int usersCountAfterRemoving = getAliveUsersExcludingGuestsCountLU() - profilesRemovedCount;
                    if (usersCountAfterRemoving == 1 || usersCountAfterRemoving < UserManager.getMaxSupportedUsers()) {
                        z = true;
                    }
                }
            }
        }
    }

    @GuardedBy({"mUsersLock"})
    private int getAliveUsersExcludingGuestsCountLU() {
        int aliveUserCount = 0;
        int totalUserCount = this.mUsers.size();
        for (int i = 0; i < totalUserCount; i++) {
            UserInfo user = this.mUsers.valueAt(i).info;
            if (!this.mRemovingUserIds.get(user.id) && !user.isGuest()) {
                aliveUserCount++;
            }
        }
        return aliveUserCount;
    }

    private static final void checkManageUserAndAcrossUsersFullPermission(String message) {
        int uid = Binder.getCallingUid();
        if (uid != 1000 && uid != 0) {
            if (!hasPermissionGranted("android.permission.MANAGE_USERS", uid) || !hasPermissionGranted("android.permission.INTERACT_ACROSS_USERS_FULL", uid)) {
                throw new SecurityException("You need MANAGE_USERS and INTERACT_ACROSS_USERS_FULL permission to: " + message);
            }
        }
    }

    private static boolean hasPermissionGranted(String permission, int uid) {
        return ActivityManager.checkComponentPermission(permission, uid, -1, true) == 0;
    }

    private static final void checkManageUsersPermission(String message) {
        if (!hasManageUsersPermission()) {
            throw new SecurityException("You need MANAGE_USERS permission to: " + message);
        }
    }

    private static final void checkManageOrCreateUsersPermission(String message) {
        if (!hasManageOrCreateUsersPermission()) {
            throw new SecurityException("You either need MANAGE_USERS or CREATE_USERS permission to: " + message);
        }
    }

    private static final void checkManageOrCreateUsersPermission(int creationFlags) {
        if ((creationFlags & -813) == 0) {
            if (!hasManageOrCreateUsersPermission()) {
                throw new SecurityException("You either need MANAGE_USERS or CREATE_USERS permission to create an user with flags: " + creationFlags);
            }
        } else if (!hasManageUsersPermission()) {
            throw new SecurityException("You need MANAGE_USERS permission to create an user  with flags: " + creationFlags);
        }
    }

    private static final boolean hasManageUsersPermission() {
        int callingUid = Binder.getCallingUid();
        return UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || hasPermissionGranted("android.permission.MANAGE_USERS", callingUid);
    }

    private static final boolean hasManageUsersOrPermission(String alternativePermission) {
        int callingUid = Binder.getCallingUid();
        return UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || hasPermissionGranted("android.permission.MANAGE_USERS", callingUid) || hasPermissionGranted(alternativePermission, callingUid);
    }

    private static final boolean hasManageOrCreateUsersPermission() {
        return hasManageUsersOrPermission("android.permission.CREATE_USERS");
    }

    private static void checkSystemOrRoot(String message) {
        int uid = Binder.getCallingUid();
        if (!UserHandle.isSameApp(uid, 1000) && uid != 0) {
            throw new SecurityException("Only system may: " + message);
        }
    }

    /* access modifiers changed from: private */
    public void writeBitmapLP(UserInfo info, Bitmap bitmap) {
        try {
            File dir = new File(this.mUsersDir, Integer.toString(info.id));
            File file = new File(dir, USER_PHOTO_FILENAME);
            File tmp = new File(dir, USER_PHOTO_FILENAME_TMP);
            if (!dir.exists()) {
                dir.mkdir();
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
            FileOutputStream os = new FileOutputStream(tmp);
            if (bitmap.compress(compressFormat, 100, os) && tmp.renameTo(file) && SELinux.restorecon(file)) {
                info.iconPath = file.getAbsolutePath();
            }
            try {
                os.close();
            } catch (IOException e) {
            }
            tmp.delete();
        } catch (FileNotFoundException e2) {
            Slog.w(LOG_TAG, "Error setting photo for user ", e2);
        }
    }

    public int[] getUserIds() {
        int[] iArr;
        synchronized (this.mUsersLock) {
            iArr = this.mUserIds;
        }
        return iArr;
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void readUserListLP() {
        int type;
        if (!this.mUserListFile.exists()) {
            fallbackToSingleUserLP();
            return;
        }
        FileInputStream fis = new AtomicFile(this.mUserListFile).openRead();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fis, StandardCharsets.UTF_8.name());
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            Slog.e(LOG_TAG, "Unable to read user list");
            fallbackToSingleUserLP();
            IoUtils.closeQuietly(fis);
            return;
        }
        try {
            this.mNextSerialNumber = -1;
            if (parser.getName().equals("users")) {
                String lastSerialNumber = parser.getAttributeValue(null, ATTR_NEXT_SERIAL_NO);
                if (lastSerialNumber != null) {
                    this.mNextSerialNumber = Integer.parseInt(lastSerialNumber);
                }
                String versionNumber = parser.getAttributeValue(null, "version");
                if (versionNumber != null) {
                    this.mUserVersion = Integer.parseInt(versionNumber);
                }
            }
            Bundle oldDevicePolicyGlobalUserRestrictions = null;
            while (true) {
                int type2 = parser.next();
                if (type2 == 1) {
                    updateUserIds();
                    upgradeIfNecessaryLP(oldDevicePolicyGlobalUserRestrictions);
                    break;
                } else if (type2 == 2) {
                    String name = parser.getName();
                    if (name.equals(TAG_USER)) {
                        UserData userData = readUserLP(Integer.parseInt(parser.getAttributeValue(null, ATTR_ID)));
                        if (userData != null) {
                            synchronized (this.mUsersLock) {
                                this.mUsers.put(userData.info.id, userData);
                                if (this.mNextSerialNumber < 0 || this.mNextSerialNumber <= userData.info.id) {
                                    this.mNextSerialNumber = userData.info.id + 1;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else if (name.equals(TAG_GUEST_RESTRICTIONS)) {
                        while (true) {
                            int type3 = parser.next();
                            if (type3 == 1 || type3 == 3) {
                                break;
                            } else if (type3 == 2) {
                                if (parser.getName().equals(TAG_RESTRICTIONS)) {
                                    synchronized (this.mGuestRestrictions) {
                                        UserRestrictionsUtils.readRestrictions(parser, this.mGuestRestrictions);
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                    } else if (name.equals(TAG_DEVICE_OWNER_USER_ID) || name.equals(TAG_GLOBAL_RESTRICTION_OWNER_ID)) {
                        String ownerUserId = parser.getAttributeValue(null, ATTR_ID);
                        if (ownerUserId != null) {
                            this.mDeviceOwnerUserId = Integer.parseInt(ownerUserId);
                        }
                    } else if (name.equals(TAG_DEVICE_POLICY_RESTRICTIONS)) {
                        oldDevicePolicyGlobalUserRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            fallbackToSingleUserLP();
        } catch (Throwable th) {
            IoUtils.closeQuietly(fis);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void upgradeIfNecessaryLP(Bundle oldGlobalUserRestrictions) {
        int originalVersion = this.mUserVersion;
        int userVersion = this.mUserVersion;
        if (userVersion < 1) {
            UserData userData = getUserDataNoChecks(0);
            if ("Primary".equals(userData.info.name)) {
                userData.info.name = this.mContext.getResources().getString(17040534);
                scheduleWriteUser(userData);
            }
            userVersion = 1;
        }
        if (userVersion < 2) {
            UserData userData2 = getUserDataNoChecks(0);
            if ((userData2.info.flags & 16) == 0) {
                userData2.info.flags |= 16;
                scheduleWriteUser(userData2);
            }
            userVersion = 2;
        }
        if (userVersion < 4) {
            userVersion = 4;
        }
        if (userVersion < 5) {
            initDefaultGuestRestrictions();
            userVersion = 5;
        }
        if (userVersion < 6) {
            boolean splitSystemUser = UserManager.isSplitSystemUser();
            synchronized (this.mUsersLock) {
                for (int i = 0; i < this.mUsers.size(); i++) {
                    UserData userData3 = this.mUsers.valueAt(i);
                    if (!splitSystemUser && userData3.info.isRestricted() && userData3.info.restrictedProfileParentId == -10000) {
                        userData3.info.restrictedProfileParentId = 0;
                        scheduleWriteUser(userData3);
                    }
                }
            }
            userVersion = 6;
        }
        if (userVersion < 7) {
            synchronized (this.mRestrictionsLock) {
                if (!UserRestrictionsUtils.isEmpty(oldGlobalUserRestrictions) && this.mDeviceOwnerUserId != -10000) {
                    this.mDevicePolicyGlobalUserRestrictions.put(this.mDeviceOwnerUserId, oldGlobalUserRestrictions);
                }
                UserRestrictionsUtils.moveRestriction("ensure_verify_apps", this.mDevicePolicyLocalUserRestrictions, this.mDevicePolicyGlobalUserRestrictions);
            }
            userVersion = 7;
        }
        if (userVersion < 7) {
            Slog.w(LOG_TAG, "User version " + this.mUserVersion + " didn't upgrade as expected to " + 7);
            return;
        }
        this.mUserVersion = userVersion;
        if (originalVersion < this.mUserVersion) {
            writeUserListLP();
        }
    }

    @GuardedBy({"mPackagesLock", "mRestrictionsLock"})
    private void fallbackToSingleUserLP() {
        int flags = 16;
        if (!UserManager.isSplitSystemUser()) {
            flags = 16 | 3;
        }
        UserData userData = putUserInfo(new UserInfo(0, (String) null, (String) null, flags));
        this.mNextSerialNumber = 10;
        this.mUserVersion = 7;
        Bundle restrictions = new Bundle();
        try {
            String[] defaultFirstUserRestrictions = this.mContext.getResources().getStringArray(17236007);
            for (String userRestriction : defaultFirstUserRestrictions) {
                if (UserRestrictionsUtils.isValidRestriction(userRestriction)) {
                    restrictions.putBoolean(userRestriction, true);
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Couldn't find resource: config_defaultFirstUserRestrictions", e);
        }
        if (!restrictions.isEmpty()) {
            synchronized (this.mRestrictionsLock) {
                this.mBaseUserRestrictions.append(0, restrictions);
            }
        }
        updateUserIds();
        initDefaultGuestRestrictions();
        writeUserLP(userData);
        writeUserListLP();
    }

    private String getOwnerName() {
        return this.mContext.getResources().getString(17040534);
    }

    private void scheduleWriteUser(UserData UserData2) {
        if (!this.mHandler.hasMessages(1, UserData2)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, UserData2), 2000);
        }
    }

    /* access modifiers changed from: private */
    public void writeUserLP(UserData userData) {
        FileOutputStream fos = null;
        File file = this.mUsersDir;
        AtomicFile userFile = new AtomicFile(new File(file, userData.info.id + XML_SUFFIX));
        try {
            fos = userFile.startWrite();
            writeUserLP(userData, new BufferedOutputStream(fos));
            userFile.finishWrite(fos);
        } catch (Exception ioe) {
            Slog.e(LOG_TAG, "Error writing user info " + userData.info.id, ioe);
            userFile.failWrite(fos);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void writeUserLP(UserData userData, OutputStream os) throws IOException, XmlPullParserException {
        XmlSerializer serializer = new FastXmlSerializer();
        serializer.setOutput(os, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        UserInfo userInfo = userData.info;
        serializer.startTag(null, TAG_USER);
        serializer.attribute(null, ATTR_ID, Integer.toString(userInfo.id));
        serializer.attribute(null, ATTR_SERIAL_NO, Integer.toString(userInfo.serialNumber));
        serializer.attribute(null, ATTR_FLAGS, Integer.toString(userInfo.flags));
        serializer.attribute(null, ATTR_CREATION_TIME, Long.toString(userInfo.creationTime));
        serializer.attribute(null, ATTR_LAST_LOGGED_IN_TIME, Long.toString(userInfo.lastLoggedInTime));
        if (userInfo.lastLoggedInFingerprint != null) {
            serializer.attribute(null, ATTR_LAST_LOGGED_IN_FINGERPRINT, userInfo.lastLoggedInFingerprint);
        }
        if (userInfo.iconPath != null) {
            serializer.attribute(null, ATTR_ICON_PATH, userInfo.iconPath);
        }
        if (userInfo.partial) {
            serializer.attribute(null, ATTR_PARTIAL, TemperatureProvider.SWITCH_ON);
        }
        if (userInfo.guestToRemove) {
            serializer.attribute(null, ATTR_GUEST_TO_REMOVE, TemperatureProvider.SWITCH_ON);
        }
        if (userInfo.profileGroupId != -10000) {
            serializer.attribute(null, ATTR_PROFILE_GROUP_ID, Integer.toString(userInfo.profileGroupId));
        }
        serializer.attribute(null, ATTR_PROFILE_BADGE, Integer.toString(userInfo.profileBadge));
        if (userInfo.restrictedProfileParentId != -10000) {
            serializer.attribute(null, ATTR_RESTRICTED_PROFILE_PARENT_ID, Integer.toString(userInfo.restrictedProfileParentId));
        }
        if (userData.persistSeedData) {
            if (userData.seedAccountName != null) {
                serializer.attribute(null, ATTR_SEED_ACCOUNT_NAME, userData.seedAccountName);
            }
            if (userData.seedAccountType != null) {
                serializer.attribute(null, ATTR_SEED_ACCOUNT_TYPE, userData.seedAccountType);
            }
        }
        if (userInfo.name != null) {
            serializer.startTag(null, "name");
            serializer.text(userInfo.name);
            serializer.endTag(null, "name");
        }
        synchronized (this.mRestrictionsLock) {
            UserRestrictionsUtils.writeRestrictions(serializer, this.mBaseUserRestrictions.get(userInfo.id), TAG_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, this.mDevicePolicyLocalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, this.mDevicePolicyGlobalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS);
        }
        if (userData.account != null) {
            serializer.startTag(null, TAG_ACCOUNT);
            serializer.text(userData.account);
            serializer.endTag(null, TAG_ACCOUNT);
        }
        if (userData.persistSeedData && userData.seedAccountOptions != null) {
            serializer.startTag(null, TAG_SEED_ACCOUNT_OPTIONS);
            userData.seedAccountOptions.saveToXml(serializer);
            serializer.endTag(null, TAG_SEED_ACCOUNT_OPTIONS);
        }
        if (userData.getLastRequestQuietModeEnabledMillis() != 0) {
            serializer.startTag(null, TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL);
            serializer.text(String.valueOf(userData.getLastRequestQuietModeEnabledMillis()));
            serializer.endTag(null, TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL);
        }
        serializer.endTag(null, TAG_USER);
        serializer.endDocument();
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void writeUserListLP() {
        int[] userIdsToWrite;
        int i;
        AtomicFile userListFile = new AtomicFile(this.mUserListFile);
        try {
            FileOutputStream fos = userListFile.startWrite();
            OutputStream bos = new BufferedOutputStream(fos);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "users");
            serializer.attribute(null, ATTR_NEXT_SERIAL_NO, Integer.toString(this.mNextSerialNumber));
            serializer.attribute(null, "version", Integer.toString(this.mUserVersion));
            serializer.startTag(null, TAG_GUEST_RESTRICTIONS);
            synchronized (this.mGuestRestrictions) {
                UserRestrictionsUtils.writeRestrictions(serializer, this.mGuestRestrictions, TAG_RESTRICTIONS);
            }
            serializer.endTag(null, TAG_GUEST_RESTRICTIONS);
            serializer.startTag(null, TAG_DEVICE_OWNER_USER_ID);
            serializer.attribute(null, ATTR_ID, Integer.toString(this.mDeviceOwnerUserId));
            serializer.endTag(null, TAG_DEVICE_OWNER_USER_ID);
            synchronized (this.mUsersLock) {
                userIdsToWrite = new int[this.mUsers.size()];
                for (int i2 = 0; i2 < userIdsToWrite.length; i2++) {
                    userIdsToWrite[i2] = this.mUsers.valueAt(i2).info.id;
                }
            }
            for (int id : userIdsToWrite) {
                serializer.startTag(null, TAG_USER);
                serializer.attribute(null, ATTR_ID, Integer.toString(id));
                serializer.endTag(null, TAG_USER);
            }
            serializer.endTag(null, "users");
            serializer.endDocument();
            userListFile.finishWrite(fos);
        } catch (Exception e) {
            userListFile.failWrite(null);
            Slog.e(LOG_TAG, "Error writing user list");
        }
    }

    private UserData readUserLP(int id) {
        FileInputStream fis = null;
        try {
            File file = this.mUsersDir;
            fis = new AtomicFile(new File(file, Integer.toString(id) + XML_SUFFIX)).openRead();
            UserData readUserLP = readUserLP(id, fis);
            IoUtils.closeQuietly(fis);
            return readUserLP;
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error reading user list");
        } catch (XmlPullParserException e2) {
            Slog.e(LOG_TAG, "Error reading user list");
        } catch (Throwable th) {
            IoUtils.closeQuietly(fis);
            throw th;
        }
        IoUtils.closeQuietly(fis);
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x02f1, code lost:
        return r0;
     */
    @VisibleForTesting
    public UserData readUserLP(int id, InputStream is) throws IOException, XmlPullParserException {
        int type;
        Bundle globalRestrictions;
        Bundle localRestrictions;
        Bundle baseRestrictions;
        long lastRequestQuietModeEnabledTimestamp;
        PersistableBundle seedAccountOptions;
        boolean persistSeedData;
        String seedAccountType;
        String seedAccountName;
        String account;
        int profileBadge;
        boolean guestToRemove;
        boolean partial;
        long lastLoggedInTime;
        long creationTime;
        int serialNumber;
        int type2;
        String name;
        int profileBadge2;
        int restrictedProfileParentId;
        int type3;
        String iconPath = null;
        long lastRequestQuietModeEnabledTimestamp2 = 0;
        String lastLoggedInFingerprint = null;
        boolean partial2 = false;
        boolean guestToRemove2 = false;
        boolean persistSeedData2 = false;
        PersistableBundle seedAccountOptions2 = null;
        Bundle baseRestrictions2 = null;
        Bundle localRestrictions2 = null;
        Bundle globalRestrictions2 = null;
        XmlPullParser parser = Xml.newPullParser();
        String name2 = null;
        parser.setInput(is, StandardCharsets.UTF_8.name());
        while (true) {
            int type4 = parser.next();
            if (type4 == 2) {
                type = type4;
                break;
            }
            type = type4;
            if (type == 1) {
                break;
            }
        }
        if (type != 2) {
            Slog.e(LOG_TAG, "Unable to read user " + id);
            return null;
        }
        if (type != 2 || !parser.getName().equals(TAG_USER)) {
            lastRequestQuietModeEnabledTimestamp = 0;
            restrictedProfileParentId = -10000;
            partial = false;
            persistSeedData = false;
            seedAccountName = null;
            seedAccountType = null;
            seedAccountOptions = null;
            baseRestrictions = null;
            localRestrictions = null;
            globalRestrictions = null;
            name = null;
            type2 = 0;
            account = null;
            lastLoggedInTime = 0;
            profileBadge2 = 0;
            creationTime = 0;
            profileBadge = -10000;
            guestToRemove = false;
            serialNumber = id;
        } else if (readIntAttribute(parser, ATTR_ID, -1) != id) {
            Slog.e(LOG_TAG, "User id does not match the file name");
            return null;
        } else {
            int serialNumber2 = readIntAttribute(parser, ATTR_SERIAL_NO, id);
            int flags = readIntAttribute(parser, ATTR_FLAGS, 0);
            iconPath = parser.getAttributeValue(null, ATTR_ICON_PATH);
            long creationTime2 = readLongAttribute(parser, ATTR_CREATION_TIME, 0);
            long lastLoggedInTime2 = readLongAttribute(parser, ATTR_LAST_LOGGED_IN_TIME, 0);
            lastLoggedInFingerprint = parser.getAttributeValue(null, ATTR_LAST_LOGGED_IN_FINGERPRINT);
            int profileGroupId = readIntAttribute(parser, ATTR_PROFILE_GROUP_ID, -10000);
            int profileBadge3 = readIntAttribute(parser, ATTR_PROFILE_BADGE, 0);
            int restrictedProfileParentId2 = readIntAttribute(parser, ATTR_RESTRICTED_PROFILE_PARENT_ID, -10000);
            if (TemperatureProvider.SWITCH_ON.equals(parser.getAttributeValue(null, ATTR_PARTIAL))) {
                partial2 = true;
            }
            String tag = parser.getAttributeValue(null, ATTR_GUEST_TO_REMOVE);
            if (TemperatureProvider.SWITCH_ON.equals(tag)) {
                guestToRemove2 = true;
            }
            String seedAccountName2 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_NAME);
            String seedAccountType2 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_TYPE);
            if (!(seedAccountName2 == null && seedAccountType2 == null)) {
                persistSeedData2 = true;
            }
            int outerDepth = parser.getDepth();
            String account2 = null;
            while (true) {
                int type5 = parser.next();
                if (type5 == 1) {
                    type3 = type5;
                    break;
                }
                type3 = type5;
                if (type3 == 3 && parser.getDepth() <= outerDepth) {
                    break;
                } else if (type3 == 3 || type3 == 4) {
                    tag = tag;
                    outerDepth = outerDepth;
                    account2 = account2;
                } else {
                    String tag2 = parser.getName();
                    if ("name".equals(tag2)) {
                        if (parser.next() == 4) {
                            name2 = parser.getText();
                            account2 = account2;
                        } else {
                            account2 = account2;
                        }
                    } else if (TAG_RESTRICTIONS.equals(tag2)) {
                        baseRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_DEVICE_POLICY_RESTRICTIONS.equals(tag2)) {
                        localRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS.equals(tag2)) {
                        globalRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_ACCOUNT.equals(tag2)) {
                        if (parser.next() == 4) {
                            account2 = parser.getText();
                        } else {
                            account2 = account2;
                        }
                    } else if (TAG_SEED_ACCOUNT_OPTIONS.equals(tag2)) {
                        seedAccountOptions2 = PersistableBundle.restoreFromXml(parser);
                        persistSeedData2 = true;
                        account2 = account2;
                    } else if (!TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL.equals(tag2)) {
                        account2 = account2;
                    } else if (parser.next() == 4) {
                        lastRequestQuietModeEnabledTimestamp2 = Long.parseLong(parser.getText());
                        account2 = account2;
                    } else {
                        account2 = account2;
                    }
                    tag = tag;
                    outerDepth = outerDepth;
                }
            }
            lastRequestQuietModeEnabledTimestamp = lastRequestQuietModeEnabledTimestamp2;
            restrictedProfileParentId = restrictedProfileParentId2;
            partial = partial2;
            persistSeedData = persistSeedData2;
            seedAccountName = seedAccountName2;
            seedAccountType = seedAccountType2;
            seedAccountOptions = seedAccountOptions2;
            baseRestrictions = baseRestrictions2;
            localRestrictions = localRestrictions2;
            globalRestrictions = globalRestrictions2;
            name = name2;
            account = account2;
            lastLoggedInTime = lastLoggedInTime2;
            profileBadge2 = profileBadge3;
            type2 = flags;
            creationTime = creationTime2;
            profileBadge = profileGroupId;
            guestToRemove = guestToRemove2;
            serialNumber = serialNumber2;
        }
        UserInfo userInfo = new UserInfo(id, name, iconPath, type2);
        userInfo.serialNumber = serialNumber;
        userInfo.creationTime = creationTime;
        userInfo.lastLoggedInTime = lastLoggedInTime;
        userInfo.lastLoggedInFingerprint = lastLoggedInFingerprint;
        userInfo.partial = partial;
        userInfo.guestToRemove = guestToRemove;
        userInfo.profileGroupId = profileBadge;
        userInfo.profileBadge = profileBadge2;
        userInfo.restrictedProfileParentId = restrictedProfileParentId;
        UserData userData = new UserData();
        userData.info = userInfo;
        userData.account = account;
        userData.seedAccountName = seedAccountName;
        userData.seedAccountType = seedAccountType;
        userData.persistSeedData = persistSeedData;
        userData.seedAccountOptions = seedAccountOptions;
        userData.setLastRequestQuietModeEnabledMillis(lastRequestQuietModeEnabledTimestamp);
        synchronized (this.mRestrictionsLock) {
            if (baseRestrictions != null) {
                try {
                    this.mBaseUserRestrictions.put(id, baseRestrictions);
                } catch (Throwable th) {
                    th = th;
                }
            }
            if (localRestrictions != null) {
                try {
                    this.mDevicePolicyLocalUserRestrictions.put(id, localRestrictions);
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            if (globalRestrictions != null) {
                try {
                    this.mDevicePolicyGlobalUserRestrictions.put(id, globalRestrictions);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    private int readIntAttribute(XmlPullParser parser, String attr, int defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long readLongAttribute(XmlPullParser parser, String attr, long defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void cleanAppRestrictionsForPackageLAr(String pkg, int userId) {
        File resFile = new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(pkg));
        if (resFile.exists()) {
            resFile.delete();
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, userId, disallowedPackages);
    }

    public UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userId, String[] disallowedPackages) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternalUnchecked(name, flags, userId, disallowedPackages);
    }

    public boolean removeUserEvenWhenDisallowed(int userHandle) {
        checkManageOrCreateUsersPermission("Only the system can remove users");
        return removeUserUnchecked(userHandle);
    }

    public UserInfo createUser(String name, int flags) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, -10000);
    }

    private UserInfo createUserInternal(String name, int flags, int parentId) {
        return createUserInternal(name, flags, parentId, null);
    }

    private UserInfo createUserInternal(String name, int flags, int parentId, String[] disallowedPackages) {
        String restriction;
        if ((flags & 32) != 0) {
            restriction = "no_add_managed_profile";
        } else {
            restriction = "no_add_user";
        }
        if (!hasUserRestriction(restriction, UserHandle.getCallingUserId())) {
            return createUserInternalUnchecked(name, flags, parentId, disallowedPackages);
        }
        Log.w(LOG_TAG, "Cannot add user. " + restriction + " is enabled.");
        return null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0180, code lost:
        if (r7.info.isEphemeral() != false) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:?, code lost:
        writeUserLP(r0);
        writeUserListLP();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x01f8, code lost:
        if (r7 == null) goto L_0x0231;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x01fa, code lost:
        if (r0 == false) goto L_0x0216;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0202, code lost:
        if (r7.info.profileGroupId != -10000) goto L_0x020f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0204, code lost:
        r7.info.profileGroupId = r7.info.id;
        writeUserLP(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x020f, code lost:
        r0.profileGroupId = r7.info.profileGroupId;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0216, code lost:
        if (r0 == false) goto L_0x0231;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x021e, code lost:
        if (r7.info.restrictedProfileParentId != -10000) goto L_0x022b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x0220, code lost:
        r7.info.restrictedProfileParentId = r7.info.id;
        writeUserLP(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x022b, code lost:
        r0.restrictedProfileParentId = r7.info.restrictedProfileParentId;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:?, code lost:
        ((android.os.storage.StorageManager) r26.mContext.getSystemService(android.os.storage.StorageManager.class)).createUserKey(r0, r0.serialNumber, r0.isEphemeral());
        r26.mUserDataPreparer.prepareUserData(r0, r0.serialNumber, 3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:?, code lost:
        r26.mPm.createNewUser(r0, r30);
        r0.partial = false;
        r7 = r26.mPackagesLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x025a, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:?, code lost:
        writeUserLP(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x025e, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x025f, code lost:
        updateUserIds();
        r0 = new android.os.Bundle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0268, code lost:
        if (r0 == false) goto L_0x0277;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x026a, code lost:
        r13 = r26.mGuestRestrictions;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x026c, code lost:
        monitor-enter(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:?, code lost:
        r0.putAll(r26.mGuestRestrictions);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0272, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0277, code lost:
        r13 = r26.mRestrictionsLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0279, code lost:
        monitor-enter(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:?, code lost:
        r26.mBaseUserRestrictions.append(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x027f, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x0280, code lost:
        android.common.OppoFeatureCache.get(com.android.server.am.IColorMultiAppManager.DEFAULT).createUser(r0);
        r26.mPm.onNewUserCreated(r0);
        r0 = new android.content.Intent("android.intent.action.USER_ADDED");
        r0.putExtra("android.intent.extra.user_handle", r0);
        r26.mContext.sendBroadcastAsUser(r0, android.os.UserHandle.ALL, "android.permission.MANAGE_USERS");
        r2 = r26.mContext;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x02a9, code lost:
        if (r0 == false) goto L_0x02af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x02ab, code lost:
        r13 = com.android.server.pm.UserManagerService.TRON_GUEST_CREATED;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x02af, code lost:
        if (r0 == false) goto L_0x02b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x02b1, code lost:
        r13 = com.android.server.pm.UserManagerService.TRON_DEMO_CREATED;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x02b5, code lost:
        r13 = com.android.server.pm.UserManagerService.TRON_USER_CREATED;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x02b8, code lost:
        com.android.internal.logging.MetricsLogger.count(r2, r13, 1);
        android.os.Binder.restoreCallingIdentity(r24);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x02c1, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x02c7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x02ce, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x02d0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:0x02d3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x02d8, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0315, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0316, code lost:
        r2 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x033e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x01c4  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x01c9  */
    public UserInfo createUserInternalUnchecked(String name, int flags, int parentId, String[] disallowedPackages) {
        long ident;
        UserData parent;
        int userId;
        boolean ephemeralGuests;
        int flags2;
        int userId2;
        long now;
        long ident2;
        UserData parent2;
        int flags3 = flags;
        if (((DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class)).isMemoryLow()) {
            Log.w(LOG_TAG, "Cannot add user. Not enough space on disk.");
            return null;
        }
        boolean isGuest = (flags3 & 4) != 0;
        boolean isManagedProfile = (flags3 & 32) != 0;
        boolean isRestricted = (flags3 & 8) != 0;
        boolean isDemo = (flags3 & 512) != 0;
        long ident3 = Binder.clearCallingIdentity();
        UserInfo userInfo = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getUserInfoByFlags(flags3);
        if (userInfo != null) {
            return userInfo;
        }
        try {
            synchronized (this.mPackagesLock) {
                if (parentId != -10000) {
                    try {
                        synchronized (this.mUsersLock) {
                            parent2 = getUserDataLU(parentId);
                        }
                        if (parent2 == null) {
                            Binder.restoreCallingIdentity(ident3);
                            return null;
                        }
                        parent = parent2;
                    } catch (Throwable th) {
                        th = th;
                        ident = ident3;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                } else {
                    parent = null;
                }
                if (isManagedProfile && !canAddMoreManagedProfiles(parentId, false)) {
                    Log.e(LOG_TAG, "Cannot add more managed profiles for user " + parentId);
                    Binder.restoreCallingIdentity(ident3);
                    return null;
                } else if (!isGuest && !isManagedProfile && !isDemo && isUserLimitReached() && !hasMultiAppFlag(flags3)) {
                    Log.e(LOG_TAG, "Cannot add user. Maximum user limit is reached.");
                    Binder.restoreCallingIdentity(ident3);
                    return null;
                } else if (isGuest && findCurrentGuestUser() != null) {
                    Log.e(LOG_TAG, "Cannot add guest user. Guest user already exists.");
                    Binder.restoreCallingIdentity(ident3);
                    return null;
                } else if (!isRestricted || UserManager.isSplitSystemUser() || parentId == 0) {
                    if (isRestricted && UserManager.isSplitSystemUser()) {
                        if (parent == null) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be specified");
                            Binder.restoreCallingIdentity(ident3);
                            return null;
                        } else if (!parent.info.canHaveProfile()) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - profiles cannot be created for the specified parent user id " + parentId);
                            Binder.restoreCallingIdentity(ident3);
                            return null;
                        }
                    }
                    try {
                        if (UserManager.isSplitSystemUser() && !isGuest && !isManagedProfile && getPrimaryUser() == null) {
                            flags3 |= 1;
                            try {
                                synchronized (this.mUsersLock) {
                                    if (!this.mIsDeviceManaged) {
                                        flags3 |= 2;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                ident = ident3;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            userId = getNextAvailableId();
                            Environment.getUserSystemDirectory(userId).mkdirs();
                            ephemeralGuests = Resources.getSystem().getBoolean(17891462);
                        } catch (Throwable th4) {
                            th = th4;
                            ident = ident3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            synchronized (this.mUsersLock) {
                                if (!isGuest || !ephemeralGuests) {
                                    try {
                                        if (!this.mForceEphemeralUsers) {
                                            if (parent != null) {
                                                try {
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    flags2 = flags3;
                                                    ident = ident3;
                                                    while (true) {
                                                        try {
                                                            break;
                                                        } catch (Throwable th6) {
                                                            th = th6;
                                                        }
                                                    }
                                                    throw th;
                                                }
                                            }
                                            userId2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserIdByFlags(userId, flags3);
                                            UserInfo userInfo2 = new UserInfo(userId2, name, (String) null, flags3);
                                            try {
                                                int i = this.mNextSerialNumber;
                                                flags2 = flags3;
                                                try {
                                                    this.mNextSerialNumber = i + 1;
                                                    userInfo2.serialNumber = i;
                                                    now = System.currentTimeMillis();
                                                    if (now <= EPOCH_PLUS_30_YEARS) {
                                                        ident = ident3;
                                                        ident2 = now;
                                                    } else {
                                                        ident = ident3;
                                                        ident2 = 0;
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    ident = ident3;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                                try {
                                                    userInfo2.creationTime = ident2;
                                                    userInfo2.partial = true;
                                                    userInfo2.lastLoggedInFingerprint = Build.FINGERPRINT;
                                                    if (isManagedProfile && parentId != -10000) {
                                                        userInfo2.profileBadge = getFreeProfileBadgeLU(parentId);
                                                    }
                                                    UserData userData = new UserData();
                                                    userData.info = userInfo2;
                                                    this.mUsers.put(userId2, userData);
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } catch (Throwable th9) {
                                                th = th9;
                                                flags2 = flags3;
                                                ident = ident3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        ident = ident3;
                                        flags2 = flags3;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                flags3 |= 256;
                                try {
                                    userId2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserIdByFlags(userId, flags3);
                                } catch (Throwable th11) {
                                    th = th11;
                                    flags2 = flags3;
                                    ident = ident3;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    UserInfo userInfo22 = new UserInfo(userId2, name, (String) null, flags3);
                                    int i2 = this.mNextSerialNumber;
                                    flags2 = flags3;
                                    this.mNextSerialNumber = i2 + 1;
                                    userInfo22.serialNumber = i2;
                                    now = System.currentTimeMillis();
                                    if (now <= EPOCH_PLUS_30_YEARS) {
                                    }
                                    userInfo22.creationTime = ident2;
                                    userInfo22.partial = true;
                                    userInfo22.lastLoggedInFingerprint = Build.FINGERPRINT;
                                    userInfo22.profileBadge = getFreeProfileBadgeLU(parentId);
                                    UserData userData2 = new UserData();
                                    userData2.info = userInfo22;
                                    this.mUsers.put(userId2, userData2);
                                } catch (Throwable th12) {
                                    th = th12;
                                    flags2 = flags3;
                                    ident = ident3;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                        } catch (Throwable th13) {
                            th = th13;
                            ident = ident3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th14) {
                        th = th14;
                        ident = ident3;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be owner");
                    Binder.restoreCallingIdentity(ident3);
                    return null;
                }
            }
        } catch (Throwable th15) {
            th = th15;
            ident = ident3;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        while (true) {
        }
        while (true) {
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UserData putUserInfo(UserInfo userInfo) {
        UserData userData = new UserData();
        userData.info = userInfo;
        synchronized (this.mUsers) {
            this.mUsers.put(userInfo.id, userData);
        }
        return userData;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void removeUserInfo(int userId) {
        synchronized (this.mUsers) {
            this.mUsers.remove(userId);
        }
    }

    public UserInfo createRestrictedProfile(String name, int parentUserId) {
        checkManageOrCreateUsersPermission("setupRestrictedProfile");
        UserInfo user = createProfileForUser(name, 8, parentUserId, null);
        if (user == null) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            setUserRestriction("no_modify_accounts", true, user.id);
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", 0, user.id);
            setUserRestriction("no_share_location", true, user.id);
            return user;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private UserInfo findCurrentGuestUser() {
        synchronized (this.mUsersLock) {
            int size = this.mUsers.size();
            for (int i = 0; i < size; i++) {
                UserInfo user = this.mUsers.valueAt(i).info;
                if (user.isGuest() && !user.guestToRemove && !this.mRemovingUserIds.get(user.id)) {
                    return user;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
        if (r5.info.isGuest() != false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0048, code lost:
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004c, code lost:
        r5.info.guestToRemove = true;
        r5.info.flags |= 64;
        writeUserLP(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0067, code lost:
        return false;
     */
    public boolean markGuestForDeletion(int userHandle) {
        checkManageUsersPermission("Only the system can remove users");
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean("no_remove_user", false)) {
            Log.w(LOG_TAG, "Cannot remove user. DISALLOW_REMOVE_USER is enabled.");
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    UserData userData = this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (this.mRemovingUserIds.get(userHandle)) {
                        }
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean removeUser(int userHandle) {
        boolean isManagedProfile;
        Slog.i(LOG_TAG, "removeUser u" + userHandle);
        checkManageOrCreateUsersPermission("Only the system can remove users");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            isManagedProfile = userInfo != null && userInfo.isManagedProfile();
        }
        String restriction = isManagedProfile ? "no_remove_managed_profile" : "no_remove_user";
        if (!getUserRestrictions(UserHandle.getCallingUserId()).getBoolean(restriction, false)) {
            return removeUserUnchecked(userHandle);
        }
        Log.w(LOG_TAG, "Cannot remove user. " + restriction + " is enabled.");
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0075, code lost:
        r6.info.partial = true;
        r6.info.flags |= 64;
        writeUserLP(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r11.mAppOpsService.removeUser(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008c, code lost:
        android.util.Log.w(com.android.server.pm.UserManagerService.LOG_TAG, "Unable to notify AppOpsService of removing user.", r4);
     */
    public boolean removeUserUnchecked(int userHandle) {
        UserData userData;
        long ident = Binder.clearCallingIdentity();
        try {
            boolean z = false;
            if (ActivityManager.getCurrentUser() == userHandle) {
                Log.w(LOG_TAG, "Current user cannot be removed.");
                return false;
            }
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    userData = this.mUsers.get(userHandle);
                    if (userHandle == 0) {
                        Log.e(LOG_TAG, "System user cannot be removed.");
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else if (userData == null) {
                        Log.e(LOG_TAG, String.format("Cannot remove user %d, invalid user id provided.", Integer.valueOf(userHandle)));
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else if (this.mRemovingUserIds.get(userHandle)) {
                        Log.e(LOG_TAG, String.format("User %d is already scheduled for removal.", Integer.valueOf(userHandle)));
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else {
                        addRemovingUserIdLocked(userHandle);
                    }
                }
            }
            OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).removeUser(userData.info);
            removeExtraConfigurationForUser(this.mContext.getContentResolver(), userHandle);
            if (userData.info.profileGroupId != -10000 && userData.info.isManagedProfile()) {
                sendProfileRemovedBroadcast(userData.info.profileGroupId, userData.info.id);
            }
            try {
                if (ActivityManager.getService().stopUser(userHandle, true, new IStopUserCallback.Stub() {
                    /* class com.android.server.pm.UserManagerService.AnonymousClass5 */

                    public void userStopped(int userId) {
                        UserManagerService.this.finishRemoveUser(userId);
                    }

                    public void userStopAborted(int userId) {
                    }
                }) == 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Failed to stop user during removal.", e);
                Binder.restoreCallingIdentity(ident);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUsersLock"})
    @VisibleForTesting
    public void addRemovingUserIdLocked(int userId) {
        this.mRemovingUserIds.put(userId, true);
        this.mRecentlyRemovedIds.add(Integer.valueOf(userId));
        if (this.mRecentlyRemovedIds.size() > 100) {
            this.mRecentlyRemovedIds.removeFirst();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRemoveUser(final int userHandle) {
        long ident = Binder.clearCallingIdentity();
        try {
            Intent addedIntent = new Intent("android.intent.action.USER_REMOVED");
            addedIntent.putExtra("android.intent.extra.user_handle", userHandle);
            this.mContext.sendOrderedBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS", new BroadcastReceiver() {
                /* class com.android.server.pm.UserManagerService.AnonymousClass6 */

                public void onReceive(Context context, Intent intent) {
                    new Thread() {
                        /* class com.android.server.pm.UserManagerService.AnonymousClass6.AnonymousClass1 */

                        public void run() {
                            ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).onUserStopped(userHandle);
                            UserManagerService.this.removeUserState(userHandle);
                        }
                    }.start();
                }
            }, null, -1, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public void removeUserState(int userHandle) {
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).destroyUserKey(userHandle);
        } catch (IllegalStateException e) {
            Slog.i(LOG_TAG, "Destroying key for user " + userHandle + " failed, continuing anyway", e);
        }
        try {
            IGateKeeperService gk = GateKeeper.getService();
            if (gk != null) {
                gk.clearSecureUserId(userHandle);
            }
        } catch (Exception e2) {
            Slog.w(LOG_TAG, "unable to clear GK secure user id");
        }
        this.mPm.cleanUpUser(this, userHandle);
        this.mUserDataPreparer.destroyUserData(userHandle, 3);
        synchronized (this.mUsersLock) {
            this.mUsers.remove(userHandle);
            this.mIsUserManaged.delete(userHandle);
        }
        synchronized (this.mUserStates) {
            this.mUserStates.delete(userHandle);
        }
        synchronized (this.mRestrictionsLock) {
            this.mBaseUserRestrictions.remove(userHandle);
            this.mAppliedUserRestrictions.remove(userHandle);
            this.mCachedEffectiveUserRestrictions.remove(userHandle);
            this.mDevicePolicyLocalUserRestrictions.remove(userHandle);
            if (this.mDevicePolicyGlobalUserRestrictions.get(userHandle) != null) {
                this.mDevicePolicyGlobalUserRestrictions.remove(userHandle);
                applyUserRestrictionsForAllUsersLR();
            }
        }
        synchronized (this.mPackagesLock) {
            writeUserListLP();
        }
        File file = this.mUsersDir;
        new AtomicFile(new File(file, userHandle + XML_SUFFIX)).delete();
        updateUserIds();
    }

    private void sendProfileRemovedBroadcast(int parentUserId, int removedUserId) {
        Intent managedProfileIntent = new Intent("android.intent.action.MANAGED_PROFILE_REMOVED");
        managedProfileIntent.addFlags(1342177280);
        managedProfileIntent.putExtra("android.intent.extra.USER", new UserHandle(removedUserId));
        managedProfileIntent.putExtra("android.intent.extra.user_handle", removedUserId);
        this.mContext.sendBroadcastAsUser(managedProfileIntent, new UserHandle(parentUserId), null);
    }

    public Bundle getApplicationRestrictions(String packageName) {
        return getApplicationRestrictionsForUser(packageName, UserHandle.getCallingUserId());
    }

    public Bundle getApplicationRestrictionsForUser(String packageName, int userId) {
        Bundle readApplicationRestrictionsLAr;
        if (UserHandle.getCallingUserId() != userId || !UserHandle.isSameApp(Binder.getCallingUid(), getUidForPackage(packageName))) {
            checkSystemOrRoot("get application restrictions for other user/app " + packageName);
        }
        synchronized (this.mAppRestrictionsLock) {
            readApplicationRestrictionsLAr = readApplicationRestrictionsLAr(packageName, userId);
        }
        return readApplicationRestrictionsLAr;
    }

    public void setApplicationRestrictions(String packageName, Bundle restrictions, int userId) {
        checkSystemOrRoot("set application restrictions");
        if (restrictions != null) {
            restrictions.setDefusable(true);
        }
        synchronized (this.mAppRestrictionsLock) {
            if (restrictions != null) {
                if (!restrictions.isEmpty()) {
                    writeApplicationRestrictionsLAr(packageName, restrictions, userId);
                }
            }
            cleanAppRestrictionsForPackageLAr(packageName, userId);
        }
        Intent changeIntent = new Intent("android.intent.action.APPLICATION_RESTRICTIONS_CHANGED");
        changeIntent.setPackage(packageName);
        changeIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changeIntent, UserHandle.of(userId));
    }

    private int getUidForPackage(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_CHANGES).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @GuardedBy({"mAppRestrictionsLock"})
    private static Bundle readApplicationRestrictionsLAr(String packageName, int userId) {
        return readApplicationRestrictionsLAr(new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    @GuardedBy({"mAppRestrictionsLock"})
    @VisibleForTesting
    static Bundle readApplicationRestrictionsLAr(AtomicFile restrictionsFile) {
        Bundle restrictions = new Bundle();
        ArrayList<String> values = new ArrayList<>();
        if (!restrictionsFile.getBaseFile().exists()) {
            return restrictions;
        }
        FileInputStream fis = restrictionsFile.openRead();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fis, StandardCharsets.UTF_8.name());
        XmlUtils.nextElement(parser);
        if (parser.getEventType() != 2) {
            Slog.e(LOG_TAG, "Unable to read restrictions file " + restrictionsFile.getBaseFile());
            IoUtils.closeQuietly(fis);
            return restrictions;
        }
        while (parser.next() != 1) {
            try {
                readEntry(restrictions, values, parser);
            } catch (IOException | XmlPullParserException e) {
                Log.w(LOG_TAG, "Error parsing " + restrictionsFile.getBaseFile(), e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(fis);
                throw th;
            }
        }
        IoUtils.closeQuietly(fis);
        return restrictions;
    }

    private static void readEntry(Bundle restrictions, ArrayList<String> values, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2 && parser.getName().equals(TAG_ENTRY)) {
            String key = parser.getAttributeValue(null, ATTR_KEY);
            String valType = parser.getAttributeValue(null, "type");
            String multiple = parser.getAttributeValue(null, ATTR_MULTIPLE);
            if (multiple != null) {
                values.clear();
                int count = Integer.parseInt(multiple);
                while (count > 0) {
                    int type = parser.next();
                    if (type == 1) {
                        break;
                    } else if (type == 2 && parser.getName().equals(TAG_VALUE)) {
                        values.add(parser.nextText().trim());
                        count--;
                    }
                }
                String[] valueStrings = new String[values.size()];
                values.toArray(valueStrings);
                restrictions.putStringArray(key, valueStrings);
            } else if (ATTR_TYPE_BUNDLE.equals(valType)) {
                restrictions.putBundle(key, readBundleEntry(parser, values));
            } else if (ATTR_TYPE_BUNDLE_ARRAY.equals(valType)) {
                int outerDepth = parser.getDepth();
                ArrayList<Bundle> bundleList = new ArrayList<>();
                while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                    bundleList.add(readBundleEntry(parser, values));
                }
                restrictions.putParcelableArray(key, (Parcelable[]) bundleList.toArray(new Bundle[bundleList.size()]));
            } else {
                String value = parser.nextText().trim();
                if (ATTR_TYPE_BOOLEAN.equals(valType)) {
                    restrictions.putBoolean(key, Boolean.parseBoolean(value));
                } else if (ATTR_TYPE_INTEGER.equals(valType)) {
                    restrictions.putInt(key, Integer.parseInt(value));
                } else {
                    restrictions.putString(key, value);
                }
            }
        }
    }

    private static Bundle readBundleEntry(XmlPullParser parser, ArrayList<String> values) throws IOException, XmlPullParserException {
        Bundle childBundle = new Bundle();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            readEntry(childBundle, values, parser);
        }
        return childBundle;
    }

    @GuardedBy({"mAppRestrictionsLock"})
    private static void writeApplicationRestrictionsLAr(String packageName, Bundle restrictions, int userId) {
        writeApplicationRestrictionsLAr(restrictions, new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    @GuardedBy({"mAppRestrictionsLock"})
    @VisibleForTesting
    static void writeApplicationRestrictionsLAr(Bundle restrictions, AtomicFile restrictionsFile) {
        FileOutputStream fos = null;
        try {
            fos = restrictionsFile.startWrite();
            OutputStream bos = new BufferedOutputStream(fos);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, TAG_RESTRICTIONS);
            writeBundle(restrictions, serializer);
            serializer.endTag(null, TAG_RESTRICTIONS);
            serializer.endDocument();
            restrictionsFile.finishWrite(fos);
        } catch (Exception e) {
            restrictionsFile.failWrite(fos);
            Slog.e(LOG_TAG, "Error writing application restrictions list", e);
        }
    }

    private static void writeBundle(Bundle restrictions, XmlSerializer serializer) throws IOException {
        for (String key : restrictions.keySet()) {
            Object value = restrictions.get(key);
            serializer.startTag(null, TAG_ENTRY);
            serializer.attribute(null, ATTR_KEY, key);
            if (value instanceof Boolean) {
                serializer.attribute(null, "type", ATTR_TYPE_BOOLEAN);
                serializer.text(value.toString());
            } else if (value instanceof Integer) {
                serializer.attribute(null, "type", ATTR_TYPE_INTEGER);
                serializer.text(value.toString());
            } else {
                String str = "";
                if (value == null || (value instanceof String)) {
                    serializer.attribute(null, "type", ATTR_TYPE_STRING);
                    if (value != null) {
                        str = (String) value;
                    }
                    serializer.text(str);
                } else if (value instanceof Bundle) {
                    serializer.attribute(null, "type", ATTR_TYPE_BUNDLE);
                    writeBundle((Bundle) value, serializer);
                } else {
                    int i = 0;
                    if (value instanceof Parcelable[]) {
                        serializer.attribute(null, "type", ATTR_TYPE_BUNDLE_ARRAY);
                        Parcelable[] array = (Parcelable[]) value;
                        int length = array.length;
                        while (i < length) {
                            Parcelable parcelable = array[i];
                            if (parcelable instanceof Bundle) {
                                serializer.startTag(null, TAG_ENTRY);
                                serializer.attribute(null, "type", ATTR_TYPE_BUNDLE);
                                writeBundle((Bundle) parcelable, serializer);
                                serializer.endTag(null, TAG_ENTRY);
                                i++;
                            } else {
                                throw new IllegalArgumentException("bundle-array can only hold Bundles");
                            }
                        }
                        continue;
                    } else {
                        serializer.attribute(null, "type", ATTR_TYPE_STRING_ARRAY);
                        String[] values = (String[]) value;
                        serializer.attribute(null, ATTR_MULTIPLE, Integer.toString(values.length));
                        int length2 = values.length;
                        while (i < length2) {
                            String choice = values[i];
                            serializer.startTag(null, TAG_VALUE);
                            serializer.text(choice != null ? choice : str);
                            serializer.endTag(null, TAG_VALUE);
                            i++;
                        }
                    }
                }
            }
            serializer.endTag(null, TAG_ENTRY);
        }
    }

    public int getUserSerialNumber(int userHandle) {
        int i;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            i = userInfo != null ? userInfo.serialNumber : -1;
        }
        return i;
    }

    public boolean isUserNameSet(int userHandle) {
        boolean z;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            z = (userInfo == null || userInfo.name == null) ? false : true;
        }
        return z;
    }

    public int getUserHandle(int userSerialNumber) {
        synchronized (this.mUsersLock) {
            int[] iArr = this.mUserIds;
            for (int userId : iArr) {
                UserInfo info = getUserInfoLU(userId);
                if (info != null && info.serialNumber == userSerialNumber) {
                    return userId;
                }
            }
            return -1;
        }
    }

    public long getUserCreationTime(int userHandle) {
        int callingUserId = UserHandle.getCallingUserId();
        UserInfo userInfo = null;
        synchronized (this.mUsersLock) {
            if (callingUserId == userHandle) {
                userInfo = getUserInfoLU(userHandle);
            } else {
                UserInfo parent = getProfileParentLU(userHandle);
                if (parent != null && parent.id == callingUserId) {
                    userInfo = getUserInfoLU(userHandle);
                }
            }
        }
        if (userInfo != null) {
            return userInfo.creationTime;
        }
        throw new SecurityException("userHandle can only be the calling user or a managed profile associated with this user");
    }

    /* JADX INFO: Multiple debug info for r3v2 int[]: [D('i' int), D('newUsers' int[])] */
    private void updateUserIds() {
        int num = 0;
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                if (!this.mUsers.valueAt(i).info.partial) {
                    num++;
                }
            }
            int[] newUsers = new int[num];
            int n = 0;
            for (int i2 = 0; i2 < userSize; i2++) {
                if (!this.mUsers.valueAt(i2).info.partial) {
                    newUsers[n] = this.mUsers.keyAt(i2);
                    n++;
                }
            }
            this.mUserIds = newUsers;
        }
    }

    public void onBeforeStartUser(int userId) {
        UserInfo userInfo = getUserInfo(userId);
        if (userInfo != null) {
            this.mUserDataPreparer.prepareUserData(userId, userInfo.serialNumber, 1);
            this.mPm.reconcileAppsData(userId, 1, !Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint));
            if (userId != 0) {
                synchronized (this.mRestrictionsLock) {
                    applyUserRestrictionsLR(userId);
                }
            }
        }
    }

    public void onBeforeUnlockUser(int userId) {
        UserInfo userInfo = getUserInfo(userId);
        if (userInfo != null) {
            this.mUserDataPreparer.prepareUserData(userId, userInfo.serialNumber, 2);
            this.mPm.reconcileAppsData(userId, 2, !Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint));
        }
    }

    /* access modifiers changed from: package-private */
    public void reconcileUsers(String volumeUuid) {
        this.mUserDataPreparer.reconcileUsers(volumeUuid, getUsers(true));
    }

    public void onUserLoggedIn(int userId) {
        UserData userData = getUserDataNoChecks(userId);
        if (userData == null || userData.info.partial) {
            Slog.w(LOG_TAG, "userForeground: unknown user #" + userId);
            return;
        }
        long now = System.currentTimeMillis();
        if (now > EPOCH_PLUS_30_YEARS) {
            userData.info.lastLoggedInTime = now;
        }
        userData.info.lastLoggedInFingerprint = Build.FINGERPRINT;
        scheduleWriteUser(userData);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        if (r1 < 0) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        throw new java.lang.IllegalStateException("No user id available!");
     */
    @VisibleForTesting
    public int getNextAvailableId() {
        synchronized (this.mUsersLock) {
            int nextId = scanNextAvailableIdLocked();
            if (nextId >= 0) {
                return nextId;
            }
            if (this.mRemovingUserIds.size() > 0) {
                Slog.i(LOG_TAG, "All available IDs are used. Recycling LRU ids.");
                this.mRemovingUserIds.clear();
                Iterator<Integer> it = this.mRecentlyRemovedIds.iterator();
                while (it.hasNext()) {
                    this.mRemovingUserIds.put(it.next().intValue(), true);
                }
                nextId = scanNextAvailableIdLocked();
            }
        }
    }

    @GuardedBy({"mUsersLock"})
    private int scanNextAvailableIdLocked() {
        for (int i = 10; i < MAX_USER_ID; i++) {
            if (this.mUsers.indexOfKey(i) < 0 && !this.mRemovingUserIds.get(i)) {
                return i;
            }
        }
        return -1;
    }

    private static String packageToRestrictionsFileName(String packageName) {
        return RESTRICTIONS_FILE_PREFIX + packageName + XML_SUFFIX;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
        if (r11 == false) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        writeUserLP(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        return;
     */
    public void setSeedAccountData(int userId, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) {
        checkManageUsersPermission("Require MANAGE_USERS permission to set user seed data");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = getUserDataLU(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "No such user for settings seed data u=" + userId);
                    return;
                }
                userData.seedAccountName = accountName;
                userData.seedAccountType = accountType;
                userData.seedAccountOptions = accountOptions;
                userData.persistSeedData = persist;
            }
        }
    }

    public String getSeedAccountName() throws RemoteException {
        String str;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            str = getUserDataLU(UserHandle.getCallingUserId()).seedAccountName;
        }
        return str;
    }

    public String getSeedAccountType() throws RemoteException {
        String str;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            str = getUserDataLU(UserHandle.getCallingUserId()).seedAccountType;
        }
        return str;
    }

    public PersistableBundle getSeedAccountOptions() throws RemoteException {
        PersistableBundle persistableBundle;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            persistableBundle = getUserDataLU(UserHandle.getCallingUserId()).seedAccountOptions;
        }
        return persistableBundle;
    }

    public void clearSeedAccountData() throws RemoteException {
        checkManageUsersPermission("Cannot clear seed account information");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = getUserDataLU(UserHandle.getCallingUserId());
                if (userData != null) {
                    userData.clearSeedAccountData();
                    writeUserLP(userData);
                }
            }
        }
    }

    public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
        checkManageUsersPermission("Cannot check seed account information");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserData data = this.mUsers.valueAt(i);
                if (!data.info.isInitialized()) {
                    if (data.seedAccountName == null) {
                        continue;
                    } else if (data.seedAccountName.equals(accountName)) {
                        if (data.seedAccountType == null) {
                            continue;
                        } else if (data.seedAccountType.equals(accountType)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell().exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: package-private */
    public int onShellCommand(Shell shell, String cmd) {
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        try {
            if ((cmd.hashCode() == 3322014 && cmd.equals("list")) ? false : true) {
                return shell.handleDefaultCommands(cmd);
            }
            return runList(pw);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int runList(PrintWriter pw) throws RemoteException {
        IActivityManager am = ActivityManager.getService();
        List<UserInfo> users = getUsers(false);
        if (users == null) {
            pw.println("Error: couldn't get users");
            return 1;
        }
        pw.println("Users:");
        for (int i = 0; i < users.size(); i++) {
            String running = am.isUserRunning(users.get(i).id, 0) ? " running" : "";
            pw.println("\t" + users.get(i).toString() + running);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01d5, code lost:
        r0 = th;
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Object obj;
        int i;
        long nowRealtime;
        int state;
        UserManagerService userManagerService = this;
        if (DumpUtils.checkDumpPermission(userManagerService.mContext, LOG_TAG, pw)) {
            long now = System.currentTimeMillis();
            long nowRealtime2 = SystemClock.elapsedRealtime();
            StringBuilder sb = new StringBuilder();
            synchronized (userManagerService.mPackagesLock) {
                try {
                    Object obj2 = userManagerService.mUsersLock;
                    synchronized (obj2) {
                        try {
                            pw.println("Users:");
                            int i2 = 0;
                            while (i2 < userManagerService.mUsers.size()) {
                                UserData userData = userManagerService.mUsers.valueAt(i2);
                                if (userData == null) {
                                    obj = obj2;
                                    i = i2;
                                    nowRealtime = nowRealtime2;
                                } else {
                                    UserInfo userInfo = userData.info;
                                    int userId = userInfo.id;
                                    pw.print("  ");
                                    pw.print(userInfo);
                                    pw.print(" serialNo=");
                                    pw.print(userInfo.serialNumber);
                                    if (userManagerService.mRemovingUserIds.get(userId)) {
                                        try {
                                            pw.print(" <removing> ");
                                        } catch (Throwable th) {
                                            th = th;
                                            obj = obj2;
                                        }
                                    }
                                    if (userInfo.partial) {
                                        pw.print(" <partial>");
                                    }
                                    pw.println();
                                    pw.print("    State: ");
                                    synchronized (userManagerService.mUserStates) {
                                        state = userManagerService.mUserStates.get(userId, -1);
                                    }
                                    pw.println(UserState.stateToString(state));
                                    pw.print("    Created: ");
                                    try {
                                        nowRealtime = nowRealtime2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        obj = obj2;
                                        throw th;
                                    }
                                    try {
                                        dumpTimeAgo(pw, sb, now, userInfo.creationTime);
                                        pw.print("    Last logged in: ");
                                        obj = obj2;
                                        i = i2;
                                        try {
                                            dumpTimeAgo(pw, sb, now, userInfo.lastLoggedInTime);
                                            pw.print("    Last logged in fingerprint: ");
                                            pw.println(userInfo.lastLoggedInFingerprint);
                                            pw.print("    Start time: ");
                                            dumpTimeAgo(pw, sb, nowRealtime, userData.startRealtime);
                                            pw.print("    Unlock time: ");
                                            dumpTimeAgo(pw, sb, nowRealtime, userData.unlockRealtime);
                                            pw.print("    Has profile owner: ");
                                            userManagerService = this;
                                            try {
                                                pw.println(userManagerService.mIsUserManaged.get(userId));
                                                pw.println("    Restrictions:");
                                                synchronized (userManagerService.mRestrictionsLock) {
                                                    UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mBaseUserRestrictions.get(userInfo.id));
                                                    pw.println("    Device policy global restrictions:");
                                                    UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mDevicePolicyGlobalUserRestrictions.get(userInfo.id));
                                                    pw.println("    Device policy local restrictions:");
                                                    UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mDevicePolicyLocalUserRestrictions.get(userInfo.id));
                                                    pw.println("    Effective restrictions:");
                                                    UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mCachedEffectiveUserRestrictions.get(userInfo.id));
                                                }
                                                if (userData.account != null) {
                                                    pw.print("    Account name: " + userData.account);
                                                    pw.println();
                                                }
                                                if (userData.seedAccountName != null) {
                                                    pw.print("    Seed account name: " + userData.seedAccountName);
                                                    pw.println();
                                                    if (userData.seedAccountType != null) {
                                                        pw.print("         account type: " + userData.seedAccountType);
                                                        pw.println();
                                                    }
                                                    if (userData.seedAccountOptions != null) {
                                                        pw.print("         account options exist");
                                                        pw.println();
                                                    }
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                throw th;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            throw th;
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        obj = obj2;
                                        throw th;
                                    }
                                }
                                i2 = i + 1;
                                nowRealtime2 = nowRealtime;
                                obj2 = obj;
                            }
                            try {
                                pw.println();
                                pw.println("  Device owner id:" + userManagerService.mDeviceOwnerUserId);
                                pw.println();
                                pw.println("  Guest restrictions:");
                                synchronized (userManagerService.mGuestRestrictions) {
                                    UserRestrictionsUtils.dumpRestrictions(pw, "    ", userManagerService.mGuestRestrictions);
                                }
                                synchronized (userManagerService.mUsersLock) {
                                    pw.println();
                                    pw.println("  Device managed: " + userManagerService.mIsDeviceManaged);
                                    if (userManagerService.mRemovingUserIds.size() > 0) {
                                        pw.println();
                                        pw.println("  Recently removed userIds: " + userManagerService.mRecentlyRemovedIds);
                                    }
                                }
                                synchronized (userManagerService.mUserStates) {
                                    pw.println("  Started users state: " + userManagerService.mUserStates);
                                }
                                pw.println();
                                pw.println("  Max users: " + UserManager.getMaxSupportedUsers());
                                pw.println("  Supports switchable users: " + UserManager.supportsMultipleUsers());
                                pw.println("  All guests ephemeral: " + Resources.getSystem().getBoolean(17891462));
                                return;
                            } catch (Throwable th6) {
                                th = th6;
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            obj = obj2;
                            throw th;
                        }
                    }
                } catch (Throwable th8) {
                    th = th8;
                    throw th;
                }
            }
        } else {
            return;
        }
        while (true) {
        }
    }

    private static void dumpTimeAgo(PrintWriter pw, StringBuilder sb, long nowTime, long time) {
        if (time == 0) {
            pw.println("<unknown>");
            return;
        }
        sb.setLength(0);
        TimeUtils.formatDuration(nowTime - time, sb);
        sb.append(" ago");
        pw.println(sb);
    }

    final class MainHandler extends Handler {
        MainHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                removeMessages(1, msg.obj);
                synchronized (UserManagerService.this.mPackagesLock) {
                    UserData userData = UserManagerService.this.getUserDataNoChecks(((UserData) msg.obj).info.id);
                    if (userData != null) {
                        UserManagerService.this.writeUserLP(userData);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isUserInitialized(int userId) {
        return this.mLocalService.isUserInitialized(userId);
    }

    private class LocalService extends UserManagerInternal {
        private LocalService() {
        }

        public void setDevicePolicyUserRestrictions(int userId, Bundle restrictions, boolean isDeviceOwner, int cameraRestrictionScope) {
            UserManagerService.this.setDevicePolicyUserRestrictionsInner(userId, restrictions, isDeviceOwner, cameraRestrictionScope);
        }

        public Bundle getBaseUserRestrictions(int userId) {
            Bundle bundle;
            synchronized (UserManagerService.this.mRestrictionsLock) {
                bundle = (Bundle) UserManagerService.this.mBaseUserRestrictions.get(userId);
            }
            return bundle;
        }

        public void setBaseUserRestrictionsByDpmsForMigration(int userId, Bundle baseRestrictions) {
            synchronized (UserManagerService.this.mRestrictionsLock) {
                if (UserManagerService.this.updateRestrictionsIfNeededLR(userId, new Bundle(baseRestrictions), UserManagerService.this.mBaseUserRestrictions)) {
                    UserManagerService.this.invalidateEffectiveUserRestrictionsLR(userId);
                }
            }
            UserData userData = UserManagerService.this.getUserDataNoChecks(userId);
            synchronized (UserManagerService.this.mPackagesLock) {
                if (userData != null) {
                    UserManagerService.this.writeUserLP(userData);
                } else {
                    Slog.w(UserManagerService.LOG_TAG, "UserInfo not found for " + userId);
                }
            }
        }

        public boolean getUserRestriction(int userId, String key) {
            return UserManagerService.this.getUserRestrictions(userId).getBoolean(key);
        }

        public void addUserRestrictionsListener(UserManagerInternal.UserRestrictionsListener listener) {
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserManagerService.this.mUserRestrictionsListeners.add(listener);
            }
        }

        public void removeUserRestrictionsListener(UserManagerInternal.UserRestrictionsListener listener) {
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserManagerService.this.mUserRestrictionsListeners.remove(listener);
            }
        }

        public void setDeviceManaged(boolean isManaged) {
            synchronized (UserManagerService.this.mUsersLock) {
                boolean unused = UserManagerService.this.mIsDeviceManaged = isManaged;
            }
        }

        public void setUserManaged(int userId, boolean isManaged) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserManagerService.this.mIsUserManaged.put(userId, isManaged);
            }
        }

        public void setUserIcon(int userId, Bitmap bitmap) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UserManagerService.this.mPackagesLock) {
                    UserData userData = UserManagerService.this.getUserDataNoChecks(userId);
                    if (userData != null) {
                        if (!userData.info.partial) {
                            UserManagerService.this.writeBitmapLP(userData.info, bitmap);
                            UserManagerService.this.writeUserLP(userData);
                            UserManagerService.this.sendUserInfoChangedBroadcast(userId);
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                    }
                    Slog.w(UserManagerService.LOG_TAG, "setUserIcon: unknown user #" + userId);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setForceEphemeralUsers(boolean forceEphemeralUsers) {
            synchronized (UserManagerService.this.mUsersLock) {
                boolean unused = UserManagerService.this.mForceEphemeralUsers = forceEphemeralUsers;
            }
        }

        public void removeAllUsers() {
            if (ActivityManager.getCurrentUser() == 0) {
                UserManagerService.this.removeNonSystemUsers();
                return;
            }
            BroadcastReceiver userSwitchedReceiver = new BroadcastReceiver() {
                /* class com.android.server.pm.UserManagerService.LocalService.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == 0) {
                        UserManagerService.this.mContext.unregisterReceiver(this);
                        UserManagerService.this.removeNonSystemUsers();
                    }
                }
            };
            IntentFilter userSwitchedFilter = new IntentFilter();
            userSwitchedFilter.addAction("android.intent.action.USER_SWITCHED");
            UserManagerService.this.mContext.registerReceiver(userSwitchedReceiver, userSwitchedFilter, null, UserManagerService.this.mHandler);
            ((ActivityManager) UserManagerService.this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY)).switchUser(0);
        }

        public void onEphemeralUserStop(int userId) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo userInfo = UserManagerService.this.getUserInfoLU(userId);
                if (userInfo != null && userInfo.isEphemeral()) {
                    userInfo.flags |= 64;
                    if (userInfo.isGuest()) {
                        userInfo.guestToRemove = true;
                    }
                }
            }
        }

        public UserInfo createUserEvenWhenDisallowed(String name, int flags, String[] disallowedPackages) {
            UserInfo user = UserManagerService.this.createUserInternalUnchecked(name, flags, -10000, disallowedPackages);
            if (user != null && !user.isAdmin() && !user.isDemo()) {
                UserManagerService.this.setUserRestriction("no_sms", true, user.id);
                UserManagerService.this.setUserRestriction("no_outgoing_calls", true, user.id);
            }
            return user;
        }

        public boolean removeUserEvenWhenDisallowed(int userId) {
            return UserManagerService.this.removeUserUnchecked(userId);
        }

        public boolean isUserRunning(int userId) {
            boolean z;
            synchronized (UserManagerService.this.mUserStates) {
                z = UserManagerService.this.mUserStates.get(userId, -1) >= 0;
            }
            return z;
        }

        public void setUserState(int userId, int userState) {
            synchronized (UserManagerService.this.mUserStates) {
                UserManagerService.this.mUserStates.put(userId, userState);
            }
        }

        public void removeUserState(int userId) {
            synchronized (UserManagerService.this.mUserStates) {
                UserManagerService.this.mUserStates.delete(userId);
            }
        }

        public int[] getUserIds() {
            return UserManagerService.this.getUserIds();
        }

        public boolean isUserUnlockingOrUnlocked(int userId) {
            int state;
            synchronized (UserManagerService.this.mUserStates) {
                state = UserManagerService.this.mUserStates.get(userId, -1);
            }
            if (state == 4 || state == 5) {
                return StorageManager.isUserKeyUnlocked(userId);
            }
            return state == 2 || state == 3;
        }

        public boolean isUserUnlocked(int userId) {
            int state;
            synchronized (UserManagerService.this.mUserStates) {
                state = UserManagerService.this.mUserStates.get(userId, -1);
            }
            if (state == 4 || state == 5) {
                return StorageManager.isUserKeyUnlocked(userId);
            }
            return state == 3;
        }

        public boolean isUserInitialized(int userId) {
            return (UserManagerService.this.getUserInfo(userId).flags & 16) != 0;
        }

        public boolean exists(int userId) {
            return UserManagerService.this.getUserInfoNoChecks(userId) != null;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0096, code lost:
            return false;
         */
        public boolean isProfileAccessible(int callingUserId, int targetUserId, String debugMsg, boolean throwSecurityException) {
            if (targetUserId == callingUserId) {
                return true;
            }
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo callingUserInfo = UserManagerService.this.getUserInfoLU(callingUserId);
                if (callingUserInfo == null || callingUserInfo.isManagedProfile()) {
                    if (throwSecurityException) {
                        throw new SecurityException(debugMsg + " for another profile " + targetUserId + " from " + callingUserId);
                    } else if (callingUserInfo == null) {
                        Slog.w(UserManagerService.LOG_TAG, " Cannot find user info for " + callingUserId);
                        return false;
                    }
                }
                UserInfo targetUserInfo = UserManagerService.this.getUserInfoLU(targetUserId);
                if (targetUserInfo != null) {
                    if (targetUserInfo.isEnabled()) {
                        if (targetUserInfo.profileGroupId != -10000) {
                            if (targetUserInfo.profileGroupId == callingUserInfo.profileGroupId) {
                                return true;
                            }
                        }
                        if (!throwSecurityException) {
                            return false;
                        }
                        throw new SecurityException(debugMsg + " for unrelated profile " + targetUserId);
                    }
                }
                if (throwSecurityException) {
                    Slog.w(UserManagerService.LOG_TAG, debugMsg + " for disabled profile " + targetUserId + " from " + callingUserId);
                }
            }
        }

        public int getProfileParentId(int userId) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo profileParent = UserManagerService.this.getProfileParentLU(userId);
                if (profileParent == null) {
                    return userId;
                }
                int i = profileParent.id;
                return i;
            }
        }

        public boolean isSettingRestrictedForUser(String setting, int userId, String value, int callingUid) {
            return UserRestrictionsUtils.isSettingRestrictedForUser(UserManagerService.this.mContext, setting, userId, value, callingUid);
        }
    }

    /* access modifiers changed from: private */
    public void removeNonSystemUsers() {
        ArrayList<UserInfo> usersToRemove = new ArrayList<>();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (ui.id != 0) {
                    usersToRemove.add(ui);
                }
            }
        }
        Iterator<UserInfo> it = usersToRemove.iterator();
        while (it.hasNext()) {
            removeUser(it.next().id);
        }
    }

    private class Shell extends ShellCommand {
        private Shell() {
        }

        public int onCommand(String cmd) {
            return UserManagerService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("User manager (user) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("");
            pw.println("  list");
            pw.println("    Prints all users on the system.");
        }
    }

    private static void debug(String message) {
        Log.d(LOG_TAG, message + "");
    }

    @VisibleForTesting
    static int getMaxManagedProfiles() {
        if (!Build.IS_DEBUGGABLE) {
            return 1;
        }
        return SystemProperties.getInt("persist.sys.max_profiles", 1);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUsersLock"})
    @VisibleForTesting
    public int getFreeProfileBadgeLU(int parentUserId) {
        int maxManagedProfiles = getMaxManagedProfiles();
        boolean[] usedBadges = new boolean[maxManagedProfiles];
        int userSize = this.mUsers.size();
        for (int i = 0; i < userSize; i++) {
            UserInfo ui = this.mUsers.valueAt(i).info;
            if (ui.isManagedProfile() && ui.profileGroupId == parentUserId && !this.mRemovingUserIds.get(ui.id) && ui.profileBadge < maxManagedProfiles) {
                usedBadges[ui.profileBadge] = true;
            }
        }
        for (int i2 = 0; i2 < maxManagedProfiles; i2++) {
            if (!usedBadges[i2]) {
                return i2;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasManagedProfile(int userId) {
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo profile = this.mUsers.valueAt(i).info;
                if (userId != profile.id && isProfileOf(userInfo, profile)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void verifyCallingPackage(String callingPackage, int callingUid) {
        if (this.mPm.getPackageUid(callingPackage, 0, UserHandle.getUserId(callingUid)) != callingUid) {
            throw new SecurityException("Specified package " + callingPackage + " does not match the calling uid " + callingUid);
        }
    }
}
