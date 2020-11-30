package com.android.server.am;

import android.app.ContentProviderHolder;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.common.OppoFeatureCache;
import android.content.Intent;
import android.content.OppoBaseIntent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.StorageManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.android.server.pm.IColorPackageManagerServiceInner;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.IColorActivityRecordEx;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ColorMultiAppManagerService implements IColorMultiAppManager {
    public static final int FLAG_MULTI_APP = 67108864;
    public static final int GET_MULTI_APP = 134217728;
    private static final String MULTI_APP_USER_NAME = "MultiApp";
    private static final String OPPO_RECENTS_PACKAGE_NAME = "com.coloros.recents";
    public static final String TAG = "CMAService";
    public static final int USER_ID = 999;
    public static final int USER_ORIGINAL = 0;
    private static final String WHATSAPP_THUMBNAIL = "s.whatsapp.net";
    private static final List<String> mActionListForSystemUser = new ArrayList();
    private static final Object mAppLock = new Object();
    private static final Object mLock = new Object();
    private static final Object mSyncLock = new Object();
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorMultiAppManagerService sInstance = null;
    boolean DEBUG_SWITCH;
    private ActivityManagerService mAms;
    private IColorActivityManagerServiceEx mAmsEx;
    private IColorActivityManagerServiceInner mAmsInner;
    private int mCurrentUserId = -1;
    boolean mDynamicDebug;
    private PackageManagerService mPms;
    private IColorPackageManagerServiceInner mPmsInner;
    private boolean mSupportMultiApp = false;
    private Runnable mUnlockedUserRunnable = new Runnable() {
        /* class com.android.server.am.ColorMultiAppManagerService.AnonymousClass1 */

        public void run() {
            Log.i(ColorMultiAppManagerService.TAG, "unlocked user runnable");
            ColorMultiAppManagerService.this.mAms.mSystemServiceManager.unlockUser((int) ColorMultiAppManagerService.USER_ID);
            FgThread.getHandler().post(new Runnable() {
                /* class com.android.server.am.$$Lambda$ColorMultiAppManagerService$1$UkMHyvXn9O6XcoNzP7TPlZ1YLd8 */

                public final void run() {
                    ColorMultiAppManagerService.AnonymousClass1.this.lambda$run$0$ColorMultiAppManagerService$1();
                }
            });
            ColorMultiAppManagerService.this.mAms.mUserController.finishUserUnlocked(ColorMultiAppManagerService.this.mUss);
        }

        public /* synthetic */ void lambda$run$0$ColorMultiAppManagerService$1() {
            synchronized (ColorMultiAppManagerService.mSyncLock) {
                ColorMultiAppManagerService.this.mAms.mAtmInternal.loadRecentTasksForUser((int) ColorMultiAppManagerService.USER_ID);
            }
        }
    };
    private UserManagerService mUserManager;
    private UserManagerInternal mUserManagerInternal;
    private UserState mUss;
    private ColorMultiAppManagerUtil mUtil;

    static {
        mActionListForSystemUser.add("android.intent.action.PACKAGE_DATA_CLEARED");
    }

    public static ColorMultiAppManagerService getInstance() {
        ColorMultiAppManagerService colorMultiAppManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new ColorMultiAppManagerService();
            }
            colorMultiAppManagerService = sInstance;
        }
        return colorMultiAppManagerService;
    }

    private ColorMultiAppManagerService() {
        boolean z = sDebugfDetail;
        this.mDynamicDebug = z;
        this.DEBUG_SWITCH = z | this.mDynamicDebug;
    }

    public void init(IColorActivityManagerServiceEx amsEx, IColorPackageManagerServiceEx pmsEx) {
        if ((this.mPms == null || this.mPmsInner == null) && pmsEx != null) {
            this.mPms = pmsEx.getPackageManagerService();
            this.mPmsInner = pmsEx.getColorPackageManagerServiceInner();
            this.mSupportMultiApp = pmsEx.getPackageManagerService().hasSystemFeature("oppo.multiapp.support", 0);
            Slog.v(TAG, "init got PMS mSupportMultiApp=" + this.mSupportMultiApp);
            if (this.mSupportMultiApp) {
                this.mUtil = ColorMultiAppManagerUtil.getInstance();
                this.mUtil.init(this.mPms);
                registerLogModule();
                this.mUserManager = UserManagerService.getInstance();
                if (this.mUserManager.exists((int) USER_ID)) {
                    synchronized (mSyncLock) {
                        Log.v(TAG, "init multi app user already created");
                        this.mCurrentUserId = USER_ID;
                    }
                }
            }
        }
        if ((this.mAms == null || this.mAmsInner == null) && amsEx != null) {
            Slog.v(TAG, "init got AMS");
            this.mAms = amsEx.getActivityManagerService();
            this.mAmsInner = amsEx.getColorActivityManagerServiceInner();
        }
        Log.v(TAG, "multi app init mSupportMultiApp=" + this.mSupportMultiApp);
    }

    public boolean isSupportMultiApp() {
        return this.mSupportMultiApp;
    }

    /* access modifiers changed from: package-private */
    public UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    public boolean isCurrentProfile(int userId) {
        if (this.mSupportMultiApp && userId == this.mCurrentUserId && this.mAms.mUserController.getCurrentUserIdLU() == 0) {
            return true;
        }
        return false;
    }

    public void createUser(UserInfo info) {
        if (this.mSupportMultiApp && this.mAms != null && info != null) {
            if ((info.flags & FLAG_MULTI_APP) != 0) {
                Log.v(TAG, "multi app: createUser " + info);
                synchronized (mSyncLock) {
                    this.mCurrentUserId = info.id;
                }
                setStartedUsersLocked();
            }
        }
    }

    public void startUnlockMultiUser(int userId) {
        if (this.mSupportMultiApp && userId == 0) {
            UserManagerService userManagerService = this.mUserManager;
            if (userManagerService == null) {
                Log.v(TAG, "multi app: startUnlockMultiUser mUserManager is null. userId=" + userId);
            } else if (userManagerService.exists((int) USER_ID)) {
                Log.v(TAG, "multi app: startUnlockMultiUser 999");
                setStartedUsersLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public class CreateUserProgressListener extends IProgressListener.Stub {
        private CreateUserProgressListener() {
        }

        public void onStarted(int id, Bundle extras) {
        }

        public void onProgress(int id, int progress, Bundle extras) {
        }

        public void onFinished(int id, Bundle extras) {
            Log.d(ColorMultiAppManagerService.TAG, "broadcast user unlock id=" + id);
            Intent intent = new Intent("oppo.intent.action.MULTI_APP_USER_UNLOCKED");
            intent.putExtra("android.intent.extra.user_handle", id);
            intent.addFlags(1342177280);
            ColorMultiAppManagerService.this.mAms.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM, "oppo.permission.OPPO_COMPONENT_SAFE");
        }
    }

    private void setStartedUsersLocked() {
        if (this.mAmsInner == null) {
            Log.e(TAG, "setStartedUsersLocked mAmsInner == null");
            return;
        }
        if (this.mAmsInner.startUser((int) USER_ID, false, new CreateUserProgressListener())) {
            Settings.Secure.putIntForUser(this.mAms.mContext.getContentResolver(), "user_setup_complete", 1, USER_ID);
        }
    }

    public void removeUser(UserInfo info) {
        if (this.mSupportMultiApp && this.mAms != null && info != null) {
            if ((info.flags & FLAG_MULTI_APP) != 0) {
                Log.v(TAG, "multi app: removeUser " + info);
                synchronized (mSyncLock) {
                    this.mCurrentUserId = -1;
                }
            }
        }
    }

    public boolean enforceCrossUserPermission(int callingUid, int userId) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        int i = this.mCurrentUserId;
        if (userId == i) {
            return true;
        }
        if (i == UserHandle.getUserId(callingUid)) {
            return true;
        }
        return false;
    }

    public List<String> getAllowedMultiApp() {
        if (!this.mSupportMultiApp) {
            return Collections.emptyList();
        }
        return this.mUtil.getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        if (!this.mSupportMultiApp) {
            return Collections.emptyList();
        }
        return this.mUtil.getCreatedMultiApp();
    }

    public String getAliasMultiApp(String pkgName) {
        String label;
        if (!this.mSupportMultiApp) {
            return null;
        }
        String alias = this.mUtil.getAliasByPackage(pkgName);
        if (alias != null && !"".equals(alias)) {
            return alias;
        }
        try {
            label = (String) this.mAms.mContext.getPackageManager().getApplicationLabel(this.mAms.mContext.getPackageManager().getApplicationInfo(pkgName, ColorHansRestriction.HANS_RESTRICTION_BLOCK_BINDER));
        } catch (PackageManager.NameNotFoundException e) {
            label = "Application";
        }
        String multiPrefix = Resources.getSystem().getString(201590120);
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1) {
            return label + multiPrefix + "‏";
        }
        return label + multiPrefix;
    }

    public boolean isMultiAllowedApp(String pkgName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        return this.mUtil.isMultiAllowedApp(pkgName);
    }

    public boolean isMultiApp(String pkgName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        return this.mUtil.isMultiApp(pkgName);
    }

    public boolean isMultiApp(int userId, String pkgName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        return this.mUtil.isMultiApp(userId, pkgName);
    }

    public boolean isMultiAppUserId(int userId) {
        if (this.mSupportMultiApp && userId == 999) {
            return true;
        }
        return false;
    }

    public int[] getMultiUserGids(ProcessRecord processRecord, int[] gids) {
        if (!this.mSupportMultiApp || gids == null || gids.length <= 0) {
            return gids;
        }
        if (processRecord.info.packageName != null && processRecord.userId == 999 && this.mUtil.isMultiApp(processRecord.info.packageName)) {
            int len = gids.length;
            int[] newgids = new int[(len + 1)];
            System.arraycopy(gids, 0, newgids, 0, len);
            newgids[len] = 9997;
            return newgids;
        } else if (processRecord.userId != 0) {
            return gids;
        } else {
            int len2 = gids.length;
            int[] newgids2 = new int[(len2 + 1)];
            System.arraycopy(gids, 0, newgids2, 0, len2);
            newgids2[len2] = 99909997;
            return newgids2;
        }
    }

    public boolean shouldUseLastTargetUid(int callingUid, int lastTargetUid, String authority) {
        if (this.mSupportMultiApp && UserHandle.getUserId(callingUid) == 999) {
            try {
                String packageName = this.mPms.getNameForUid(callingUid);
                String targetName = this.mPms.getNameForUid(lastTargetUid);
                if (!"com.viber.voip".equals(packageName) || !"com.google.android.gm".equals(targetName) || !"media".equals(authority)) {
                    return false;
                }
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean shouldUseLastTargetUid(int callingUid, String targetPkg) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        if ((callingUid == 1000 || UserHandle.getUserId(callingUid) == 999) && this.mUtil.isMultiApp(targetPkg)) {
            return true;
        }
        return false;
    }

    public ContentProviderRecord getCorrectCpr(ContentProviderRecord cpr, ProviderMap map, String name, int userId) {
        if (this.mSupportMultiApp && cpr != null && userId == 999) {
            String cprPkgName = "";
            if (cpr.appInfo != null) {
                cprPkgName = cpr.appInfo.packageName;
            }
            if (!this.mUtil.isMultiApp(cprPkgName) && !"com.coloros.securitypermission".equals(cprPkgName)) {
                if (this.mDynamicDebug) {
                    Slog.v(TAG, "multi app getContentProviderImpl: cpr = " + cpr + "  name = " + name + "  userId = " + userId + "  cprPkgName = " + cprPkgName);
                }
                return map.getProviderByName(name, 0);
            }
        }
        return null;
    }

    public ProviderInfo getCorrectCpi(ProviderInfo cpi, String name, int userId) {
        if (!this.mSupportMultiApp) {
            return null;
        }
        if (this.mPms == null) {
            Slog.e(TAG, "getCorrectCpi pms is null");
            return null;
        }
        if (cpi != null && userId == 999) {
            String cpiPkgName = "";
            if (cpi.applicationInfo != null) {
                cpiPkgName = cpi.applicationInfo.packageName;
            }
            if (!this.mUtil.isMultiApp(cpiPkgName) && !"com.coloros.securitypermission".equals(cpiPkgName)) {
                if (this.mDynamicDebug) {
                    Slog.v(TAG, "multi app getContentProviderImpl: cpi = " + cpi + "  name = " + name + "  userId = " + userId + "  cpiPkgName = " + cpiPkgName);
                }
                return this.mPms.resolveContentProvider(name, 3072, 0);
            }
        }
        return null;
    }

    public boolean shouldStopBroadcast(int cmd, int userId) {
        if (this.mSupportMultiApp && cmd == 0 && userId == 999) {
            return true;
        }
        return false;
    }

    public boolean shouldChangeHolder(ContentProviderHolder holder, int userId, int callingUid) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            Slog.e(TAG, "shouldChangeHolder pms is null");
            return false;
        }
        if (holder == null && userId == 999) {
            if (this.mUtil.isMultiApp(packageManagerService.getNameForUid(callingUid))) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldFilterPackageInfo(int flags, int userId, String packageName) {
        if (this.mSupportMultiApp && (134217728 & flags) != 0 && userId == 999 && !this.mUtil.isMultiApp(packageName)) {
            return true;
        }
        return false;
    }

    public boolean shouldSkipLeaveUser(Intent resultData, IColorActivityRecordEx record) {
        if (this.mSupportMultiApp && record != null && resultData != null && record != null && 999 == record.getResultToUserId() && (("com.android.documentsui".equals(record.getPackageName()) || "com.google.android.documentsui".equals(record.getPackageName())) && "com.imo.android.imoim".equals(record.getResultToPackageName()) && resultData.getDataString() != null && resultData.getDataString().contains("com.android.providers.media.documents"))) {
            return true;
        }
        return false;
    }

    public int getCorrectUserId(int userId) {
        if (this.mSupportMultiApp && userId == 999) {
            return 0;
        }
        return userId;
    }

    public int getCorrectUserId(int userId, Intent service) {
        if (this.mSupportMultiApp && userId == 999) {
            if (this.mAmsInner == null) {
                Slog.d(TAG, "getCorrectUserId mAmsInner is null");
                return userId;
            }
            String pName = null;
            if (!(service == null || service.getPackage() == null)) {
                pName = service.getPackage();
            }
            if (!(pName != null || service == null || service.getComponent() == null)) {
                pName = service.getComponent().getPackageName();
            }
            if (pName != null && !this.mUtil.isMultiApp(pName)) {
                return this.mAmsInner.getCurrentUserIdLU();
            }
        }
        return userId;
    }

    public int getCorrectUserId(int userId, String packageName) {
        if (this.mSupportMultiApp && userId == 999 && this.mUtil.isMultiAllowedApp(packageName)) {
            return 0;
        }
        return userId;
    }

    public int getCorrectUserId(int userId, String packageName, boolean created) {
        if (!this.mSupportMultiApp || userId != 999) {
            return userId;
        }
        if (created) {
            if (this.mUtil.isMultiApp(packageName)) {
                return 0;
            }
            return userId;
        } else if (this.mUtil.isMultiAllowedApp(packageName)) {
            return 0;
        } else {
            return userId;
        }
    }

    public int correctUserId(int userId, ActivityInfo aInfo) {
        if (!this.mSupportMultiApp) {
            return userId;
        }
        if (this.mAms == null) {
            Slog.e(TAG, "correctUserId mAms is null");
            return userId;
        }
        if (!(userId != 999 || aInfo == null || aInfo.applicationInfo == null || this.mAms.mUserController == null)) {
            String pName = aInfo.applicationInfo.packageName;
            Log.d(TAG, "correctUserId userId=" + userId + " pName=" + pName + " aInfo.applicationInfo.uid=" + aInfo.applicationInfo.uid);
            if (!this.mUtil.isMultiApp(pName) && !this.mUtil.isGms(pName)) {
                userId = this.mAms.mUserController.getCurrentUserIdLU();
                aInfo.applicationInfo.uid = UserHandle.getUid(userId, aInfo.applicationInfo.uid);
            }
        }
        Log.d(TAG, "correctUserId2 userId=" + userId);
        return userId;
    }

    public int getCorrectUserIdByFlags(int userId, int flags) {
        if (this.mSupportMultiApp && (67108864 & flags) != 0) {
            return USER_ID;
        }
        return userId;
    }

    public int getCorrectUserId(int flags, int userId, String packageName) {
        if (this.mSupportMultiApp && (134217728 & flags) == 0 && userId == 999 && packageName != null && !this.mUtil.isMultiApp(packageName)) {
            return 0;
        }
        return userId;
    }

    public int getCorrectUid(int uid) {
        return uid;
    }

    public int getCorrectUid2(int uid) {
        return uid;
    }

    public int getCorrectUid(int uid, String packageName) {
        return uid;
    }

    public UserInfo getUserInfo(int userId) {
        if (!this.mSupportMultiApp) {
            return null;
        }
        UserManagerService userManagerService = this.mUserManager;
        if (userManagerService == null) {
            Slog.e(TAG, "getUserInfo userId=" + userId + " callback=" + Log.getStackTraceString(new Exception()));
            return null;
        } else if (userId == 999) {
            return userManagerService.getUserInfo((int) USER_ID);
        } else {
            return null;
        }
    }

    public UserInfo getUserInfoByFlags(int flags) {
        if (!this.mSupportMultiApp) {
            return null;
        }
        UserManagerService userManagerService = this.mUserManager;
        if (userManagerService == null) {
            Slog.e(TAG, "getUserInfoByFlags flags=" + flags + " callback=" + Log.getStackTraceString(new Exception()));
            return null;
        } else if ((67108864 & flags) == 0 || !userManagerService.exists((int) USER_ID)) {
            return null;
        } else {
            return this.mUserManager.getUserInfo((int) USER_ID);
        }
    }

    public UserHandle getCorrectUserHandle(UserHandle user, int flags, boolean isCtsAppInstall) {
        if (!this.mSupportMultiApp) {
            return user;
        }
        UserManagerService userManagerService = this.mUserManager;
        if (userManagerService == null) {
            Slog.e(TAG, "getCorrectUserHandle flags=" + flags + " callback=" + Log.getStackTraceString(new Exception()));
            return user;
        } else if ((flags & 32) == 0) {
            return user;
        } else {
            int[] users = userManagerService.getUserIds();
            if (isCtsAppInstall || users.length <= 1) {
                return user;
            }
            Slog.d(TAG, "multi app: change adb install all to owner");
            return new UserHandle(0);
        }
    }

    public PackageUserState getPackageUserState(int userId, PackageSetting ps) {
        if (this.mSupportMultiApp && ps != null && userId == 999) {
            return ps.readUserState(0);
        }
        return null;
    }

    public int getDeleteFlags(int deleteFlags, int userId, String packageName) {
        if (this.mSupportMultiApp && this.mUtil.isMainApp(userId, packageName) && (deleteFlags & 2) == 0) {
            return deleteFlags | 2;
        }
        return deleteFlags;
    }

    public ContentProviderHolder fixGetContentProvider(ContentProviderHolder holder, IApplicationThread caller, String name, IBinder token, int callingUid, String callingPackage, String callingTag, boolean stable, int userId) {
        String[] packages;
        if (!this.mSupportMultiApp) {
            return holder;
        }
        if (this.mAmsInner == null) {
            Slog.e(TAG, "fixGetContentProvider mAmsInner is null");
            return holder;
        }
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            Slog.e(TAG, "fixGetContentProvider mPms is null");
            return holder;
        }
        if (holder == null) {
            if (userId == 999 && (packages = packageManagerService.getPackagesForUid(Binder.getCallingUid())) != null) {
                for (String pkgName : packages) {
                    if (this.mUtil.isMultiApp(pkgName)) {
                        return this.mAmsInner.getContentProviderImpl(caller, name, token, callingUid, callingPackage, callingTag, stable, 0);
                    }
                }
            }
        }
        return holder;
    }

    public void handleMultiAppPackageRemove(Intent intent, String action) {
        String str;
        boolean z;
        if (this.mSupportMultiApp) {
            if (this.mDynamicDebug) {
                Slog.d(TAG, "multi app: AMS dealing with multi app removed.");
            }
            if (this.mAmsInner == null) {
                Slog.d(TAG, "handleMultiAppPackageRemove mAmsInner is null");
                return;
            }
            Uri data = intent.getData();
            if (data != null) {
                String ssp = data.getSchemeSpecificPart();
                if (ssp != null) {
                    boolean removed = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED".equals(action);
                    boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                    boolean killProcess = !intent.getBooleanExtra("android.intent.extra.DONT_KILL_APP", false);
                    boolean fullUninstall = removed && !replacing;
                    if (removed) {
                        if (killProcess) {
                            str = "android.intent.extra.UID";
                            z = false;
                            this.mAmsInner.forceStopPackageLocked(ssp, UserHandle.getAppId(intent.getIntExtra("android.intent.extra.UID", -1)), false, true, true, false, fullUninstall, (int) USER_ID, removed ? "pkg removed" : "pkg changed");
                        } else {
                            str = "android.intent.extra.UID";
                            z = false;
                        }
                        if (fullUninstall) {
                            this.mAms.mAppOpsService.packageRemoved(intent.getIntExtra(str, -1), ssp);
                            this.mAmsInner.removeUriPermissionsForPackage(ssp, (int) USER_ID, true, z);
                            this.mAmsInner.removeRecentTasksByPackageName(ssp, (int) USER_ID);
                            this.mAmsInner.forceStopPackageLocked(ssp, (int) USER_ID);
                            return;
                        }
                        return;
                    }
                    if (killProcess) {
                        int extraUid = intent.getIntExtra("android.intent.extra.UID", -1);
                        this.mAmsInner.killPackageProcessesLocked(ssp, UserHandle.getAppId(extraUid), (int) USER_ID, -10000, "change " + ssp);
                    }
                    this.mAmsInner.cleanupDisabledPackageComponentsLocked(ssp, (int) USER_ID, intent.getStringArrayExtra("android.intent.extra.changed_component_name_list"));
                }
            }
        }
    }

    public boolean shouldSkipReceiver(int userId, int callingUserId, String callerPackage) {
        if (this.mSupportMultiApp && callingUserId == 0 && userId == 999 && this.mUtil.isMultiApp(callerPackage)) {
            return true;
        }
        return false;
    }

    public boolean shouldAccessProfile(int callingUserId, int targetUserId) {
        if (this.mSupportMultiApp && callingUserId == 0 && targetUserId == 999) {
            return true;
        }
        return false;
    }

    public int filterReceiver(int userId, int callingUserId, String packageName, List<ResolveInfo> newReceivers, int index) {
        if (!this.mSupportMultiApp || callingUserId != 0 || userId != 999 || this.mUtil.isMultiApp(packageName)) {
            return index;
        }
        newReceivers.remove(index);
        return index - 1;
    }

    public void filterReceivers(int userId, int callingUserId, List<ResolveInfo> newReceivers) {
        if (this.mSupportMultiApp) {
            if (userId == 0 && newReceivers != null) {
                int i = 0;
                while (i < newReceivers.size()) {
                    ResolveInfo ri = newReceivers.get(i);
                    if (callingUserId == 999 && this.mUtil.isMultiApp(ri.activityInfo.packageName)) {
                        newReceivers.remove(i);
                        i--;
                    }
                    i++;
                }
            }
            if ((callingUserId == 999 || callingUserId == 0) && userId == 999) {
                int i2 = 0;
                while (i2 < newReceivers.size()) {
                    ResolveInfo ri2 = newReceivers.get(i2);
                    if ("com.google.android.gms".equals(ri2.activityInfo.packageName) && UserHandle.getUserId(ri2.activityInfo.applicationInfo.uid) == 999) {
                        newReceivers.remove(i2);
                        i2--;
                    }
                    i2++;
                }
            }
        }
    }

    public int[] changeUsers(int callingUid, int[] users, String callerPackage) {
        if (!this.mSupportMultiApp) {
            return users;
        }
        UserHandle.getUserId(callingUid);
        int len = users.length;
        boolean hasMultiUser = false;
        boolean hasOwnerUser = false;
        for (int i = 0; i < len; i++) {
            if (users[i] == 0) {
                hasOwnerUser = true;
            } else if (users[i] == 999) {
                hasMultiUser = true;
            }
        }
        if (hasMultiUser && !hasOwnerUser && this.mUtil.isMultiApp(callerPackage)) {
            int[] newUsers = new int[(len + 1)];
            for (int n = 0; n < len; n++) {
                newUsers[n] = users[n];
            }
            newUsers[len] = 0;
            if (this.mDynamicDebug) {
                Slog.d(TAG, "collectReceiverComponents: add user 0!");
            }
            users = newUsers;
        }
        if (hasMultiUser || !hasOwnerUser || this.mUtil.isMultiApp(callerPackage)) {
            return users;
        }
        int[] newUsers2 = new int[(len + 1)];
        for (int n2 = 0; n2 < len; n2++) {
            newUsers2[n2] = users[n2];
        }
        newUsers2[len] = 999;
        return newUsers2;
    }

    public int handleBroadCastIntent(Intent intent, int callingUid, String callerPackage, int userId) {
        if (!this.mSupportMultiApp) {
            return userId;
        }
        if (userId == 999 && intent != null && mActionListForSystemUser.contains(intent.getAction())) {
            Slog.d(TAG, "handleBroadCastIntent  multi app user. the action for main user.");
            return 0;
        } else if (this.mAmsInner == null) {
            Slog.d(TAG, "handleBroadCastIntent mAmsInner is null");
            return userId;
        } else {
            String actions = intent.getAction();
            Intent shortCutIntent = null;
            if (actions != null && isMultiApp(UserHandle.getUserId(callingUid), callerPackage) && (actions.equals("com.android.launcher.action.INSTALL_SHORTCUT") || actions.equals("com.android.launcher.action.UNINSTALL_SHORTCUT"))) {
                Slog.d(TAG, "multi app: broadcastIntentLocked: multi app is creating shortCut. ");
                userId = this.mAmsInner.getCurrentUserIdLU();
                if (intent.getExtras() != null) {
                    shortCutIntent = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
                }
                if (shortCutIntent != null) {
                    shortCutIntent.addCategory("com.multiple.launcher");
                }
            }
            return userId;
        }
    }

    public int checkCategory(int userId, Intent intent) {
        if (!this.mSupportMultiApp || intent == null || intent.getCategories() == null || !intent.getCategories().contains("com.multiple.launcher")) {
            return userId;
        }
        intent.removeCategory("com.multiple.launcher");
        return USER_ID;
    }

    public int checkSpecialApp(int userId, String callingPackage, boolean componentSpecified, Intent intent, ActivityStackSupervisor supervisor) {
        if (!this.mSupportMultiApp) {
            return userId;
        }
        return userId;
    }

    public int checkIntent(int userId, String callingPackage, boolean componentSpecified, Intent intent) {
        if (this.mSupportMultiApp && 999 == userId && !componentSpecified && intent.getPackage() == null && intent.getType() == null && intent.getCategories() == null && "com.taobao.taobao".equals(callingPackage) && intent.getAction() != null && "android.intent.action.VIEW".equals(intent.getAction()) && intent.getData() != null && intent.getDataString().startsWith("http")) {
            return 0;
        }
        return userId;
    }

    public void addRecentTaskUsers(int aimUserId, Set<Integer> users) {
        int i;
        if (this.mSupportMultiApp && (i = this.mCurrentUserId) > 0 && aimUserId == 0) {
            users.add(Integer.valueOf(i));
        }
    }

    public boolean shouldFilterTask(boolean userSetupComplete, int userId) {
        if (!this.mSupportMultiApp || userSetupComplete || userId == 999) {
            return false;
        }
        if (!this.mDynamicDebug) {
            return true;
        }
        Slog.d(TAG, "Skipping, user setup not complete");
        return true;
    }

    public boolean isCustomDataApp(String packageName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null || this.mPmsInner == null) {
            Slog.e(TAG, "isCustomDataApp mPms or mPmsInner is null");
            return false;
        } else if (packageManagerService.hasSystemFeature("oppo.multiuser.install.data.app.unsupport", 0)) {
            return false;
        } else {
            return this.mPmsInner.isCustomDataApp(packageName);
        }
    }

    public boolean shouldInstall(boolean shouldInstall, int userHandle, String packageName) {
        if (!this.mSupportMultiApp || userHandle != 999) {
            return shouldInstall;
        }
        if (this.mUtil.isMultiUserInstallApp(packageName)) {
            return true;
        }
        return false;
    }

    private boolean isGmsUid(int uid) {
        String[] pkgNames = this.mPms.getPackagesForUid(uid);
        if (pkgNames != null) {
            for (String name : pkgNames) {
                if (this.mUtil.isGms(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldChangeUserHandle(int uid, String msg) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            Slog.e(TAG, "shouldChangeUserHandle mPms is null");
            return false;
        }
        String pkgName = packageManagerService.getNameForUid(uid);
        if (msg == null || 999 != UserHandle.getUserId(uid) || ((!this.mUtil.isMultiApp(pkgName) && !isGmsUid(uid)) || !msg.startsWith("Failed to find provider"))) {
            return false;
        }
        return true;
    }

    public void onExternalStoragePolicyChanged(String packageName, PackageSetting ps, int uid) {
        UserManagerService userManagerService;
        if (this.mSupportMultiApp && packageName != null && (userManagerService = this.mUserManager) != null && ps != null) {
            if (this.mPmsInner == null) {
                Slog.e(TAG, "onExternalStoragePolicyChanged mPmsInner is null");
            } else if (userManagerService.exists((int) USER_ID) && this.mUtil.isMultiApp(packageName) && this.mPmsInner.getInstalled(ps, (int) USER_ID)) {
                ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).onExternalStoragePolicyChanged(UserHandle.getUid(USER_ID, uid), packageName);
            }
        }
    }

    public boolean shouldSkipNotification(int userId, String packageName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        if (this.mDynamicDebug) {
            Slog.v(TAG, "enqueueNotificationInternal Not showing " + packageName + " userId:" + userId);
        }
        if (userId != 999 || this.mUtil.isMultiApp(packageName)) {
            return false;
        }
        return true;
    }

    public boolean shouldSkipPermissionCheck() {
        String[] packages;
        if (!this.mSupportMultiApp) {
            return false;
        }
        if (this.mPms == null) {
            Slog.e(TAG, "shouldSkipPermissionCheck mPms is null");
            return false;
        }
        if (UserHandle.getCallingUserId() == 999 && (packages = this.mPms.getPackagesForUid(Binder.getCallingUid())) != null) {
            for (String pkgName : packages) {
                if (this.mUtil.isMultiApp(pkgName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldSkipResetPermission(int userId, String packageName) {
        String stackTraceString;
        if (this.mSupportMultiApp && userId == 999 && packageName != null && this.mUtil.isMultiApp(packageName) && (stackTraceString = Log.getStackTraceString(new Throwable())) != null && stackTraceString.contains("deletePackageLI")) {
            return true;
        }
        return false;
    }

    public boolean startActivity(ActivityInfo aInfo, Intent intent, IColorActivityRecordEx sourceRecord, IColorActivityRecordEx resultRecord, String callingPackage, int callingUid) {
        return startActivity(aInfo, intent, null, sourceRecord, resultRecord, callingPackage, callingUid);
    }

    public boolean startActivity(ActivityInfo aInfo, Intent intent, Bundle bundle, IColorActivityRecordEx sourceRecord, IColorActivityRecordEx resultRecord, String callingPackage, int callingUid) {
        OppoBaseIntent baseIntent = typeCasting(intent);
        if (!this.mSupportMultiApp) {
            return false;
        }
        if (aInfo != null && aInfo.applicationInfo != null && aInfo.applicationInfo.packageName != null) {
            String pName = aInfo.applicationInfo.packageName;
            int userId = aInfo.applicationInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
            if (userId == 0 || userId == 999) {
                if (intent != null && baseIntent != null) {
                    if (sourceRecord == null || !sourceRecord.isActivityTypeHome()) {
                        if (!"com.oppo.launcher".equals(callingPackage)) {
                            if (!"android".equals(callingPackage)) {
                                if (!"com.coloros.safecenter".equals(callingPackage)) {
                                    if (!OPPO_RECENTS_PACKAGE_NAME.equals(callingPackage)) {
                                        if (baseIntent.getIsFromGameSpace() != 1) {
                                            if (!OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).isTenIntencept(callingPackage, intent)) {
                                                if (baseIntent.getLaunchStackId() != 1) {
                                                    if (2048 != (baseIntent.getOppoFlags() & 2048)) {
                                                        if (this.mUtil.isMultiApp(pName) && !pName.equals(callingPackage)) {
                                                            String tempClass = null;
                                                            if (!(intent.getComponent() == null || intent.getComponent().getClassName() == null)) {
                                                                tempClass = intent.getComponent().getClassName().trim();
                                                            }
                                                            boolean isSkip = false;
                                                            if (intent.getAction() != null && "com.sina.weibo.sdk.action.ACTION_WEIBO_ACTIVITY".equals(intent.getAction())) {
                                                                isSkip = true;
                                                            } else if ("com.facebook.orca".equals(callingPackage) && "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias".equals(tempClass) && intent.getType() != null && "application/instant-games".equals(intent.getType())) {
                                                                isSkip = true;
                                                            } else if ("com.coloros.speechassist".equals(callingPackage) && intent.getCategories() != null) {
                                                                Iterator<String> it = intent.getCategories().iterator();
                                                                while (true) {
                                                                    if (it.hasNext()) {
                                                                        String str = it.next();
                                                                        if (str != null && str.contains("USERID")) {
                                                                            isSkip = true;
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (!isSkip) {
                                                                if (!this.mUtil.isInFilter(tempClass)) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("intent", intent);
                                                                    map.put("bundle", bundle);
                                                                    Message msg = this.mAms.mHandler.obtainMessage(1001, intent);
                                                                    msg.obj = map;
                                                                    this.mAms.mHandler.sendMessageAtFrontOfQueue(msg);
                                                                    return true;
                                                                }
                                                            }
                                                            Slog.v(TAG, "multi app: startLocked: callback continue");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (intent != null && baseIntent != null && this.mUtil.isMultiApp(callingPackage) && ("android.settings.APPLICATION_DETAILS_SETTINGS".equals(intent.getAction()) || "android.settings.MANAGE_UNKNOWN_APP_SOURCES".equals(intent.getAction()) || "android.settings.action.MANAGE_WRITE_SETTINGS".equals(intent.getAction()))) {
                baseIntent.addOppoFlags((int) OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
                baseIntent.setOppoUserId(UserHandle.getUserId(callingUid));
            }
            if (intent != null && "com.android.settings".equals(pName) && this.mUtil.isMultiApp(callingPackage)) {
                if ("android.settings.CHANNEL_NOTIFICATION_SETTINGS".equals(intent.getAction()) || "android.settings.APP_NOTIFICATION_SETTINGS".equals(intent.getAction())) {
                    intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, callingUid);
                }
            }
            if (!(!"android".equals(callingPackage) || intent == null || (intent.getFlags() & 512) == 0)) {
                aInfo.taskAffinity = "coloros_multiapp_chooser";
            }
        }
        return false;
    }

    public void handleChooseActivityMsg(Message msg) {
        if (this.mSupportMultiApp) {
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                if (activityManagerService.mSystemThread != null) {
                    HashMap map = (HashMap) msg.obj;
                    Intent in = (Intent) map.get("intent");
                    in.addFlags(OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
                    Slog.d(TAG, "start chooser activity from system");
                    Intent chooserIntent = Intent.createChooser(in, null);
                    chooserIntent.addFlags(67109376);
                    chooserIntent.putExtra("android.app.extra.OPTIONS", (Bundle) map.get("bundle"));
                    ActivityManagerService activityManagerService2 = this.mAms;
                    activityManagerService2.startActivity(activityManagerService2.mSystemThread.getApplicationThread(), this.mAms.mContext.getBasePackageName(), chooserIntent, (String) null, (IBinder) null, (String) null, -1, 0, (ProfilerInfo) null, (Bundle) null);
                    return;
                }
            }
            Slog.e(TAG, "handleChooseActivityMsg mAms OR mAms.mSystemThread is null");
        }
    }

    public void intentFixUris(int mCallingUid, Intent mIntent, IColorActivityRecordEx sourceRecord) {
        String pkgName;
        if (this.mSupportMultiApp && UserHandle.getUserId(mCallingUid) == 999 && mIntent != null && mIntent.getAction() != null) {
            if (sourceRecord == null || sourceRecord.getPackageName() == null) {
                pkgName = this.mAms.getPackageManagerInternalLocked().getNameForUid(mCallingUid);
            } else {
                pkgName = sourceRecord.getPackageName();
            }
            if (ColorFreeformManagerService.FREEFORM_CALLER_PKG.equals(pkgName) || "android".equals(pkgName)) {
                pkgName = this.mAms.getPackageManagerInternalLocked().getNameForUid(mCallingUid);
                Slog.d(TAG, "multi app: intentFixUris repkgName " + pkgName);
            }
            String action = mIntent.getAction();
            String clipdatastring = null;
            if (mIntent.getClipData() != null) {
                clipdatastring = mIntent.getClipData().toString();
            }
            String data = mIntent.getDataString();
            if (pkgName != null && this.mUtil.isMultiApp(pkgName)) {
                if ((clipdatastring == null || !clipdatastring.contains(pkgName)) && (data == null || !data.contains(pkgName) || data.contains(WHATSAPP_THUMBNAIL))) {
                    if (clipdatastring == null) {
                        return;
                    }
                    if (!clipdatastring.contains("com.instagram.fileprovider") && !clipdatastring.contains("amazon.mobile.mash.fileprovider")) {
                        return;
                    }
                }
                if (action.equals("android.intent.action.SEND") || action.equals("android.intent.action.VIEW") || action.equals("android.media.action.IMAGE_CAPTURE") || action.equals("android.media.action.VIDEO_CAPTURE") || action.equals("android.intent.action.SEND_MULTIPLE")) {
                    mIntent.fixUris(USER_ID);
                }
            }
        }
    }

    public void setOppoUserId(ActivityInfo aInfo, Intent intent, int userId) {
        if (this.mSupportMultiApp) {
            OppoBaseIntent baseIntent = typeCasting(intent);
            String pkgName = aInfo.applicationInfo.packageName;
            if (baseIntent != null && pkgName != null && this.mUtil.isMultiApp(pkgName)) {
                Slog.d(TAG, "multi app: putExtra userId = " + userId + "   pkgName = " + pkgName);
                baseIntent.setOppoUserId(userId);
            }
        }
    }

    public void handleInstallApp(int userId, String packageName) {
        if (this.mSupportMultiApp && userId == 999 && this.mUtil.isMultiAllowedApp(packageName) && !this.mUtil.isMultiApp(userId, packageName)) {
            this.mUtil.addToCreatedMultiApp(packageName);
            Slog.d(TAG, "multi app: install pkg: " + packageName + " user= " + userId);
        }
    }

    public void handleUninstallPkg(int userId, String packageName) {
        if (this.mSupportMultiApp && userId == 999) {
            if (this.mUtil.isMainApp(userId, packageName) || this.mUtil.isMultiApp(userId, packageName)) {
                this.mUtil.removeFromCreatedMultiApp(packageName);
                Slog.d(TAG, "multi app: deletePackageX delete package: " + packageName + " USER: " + userId);
            }
        }
    }

    public ServiceInfo fixServiceInfo(ServiceInfo info, int userId, int callingUid, PackageParser.Service s, int flags, PackageSetting ps) {
        if (!(this.mSupportMultiApp && info == null && userId == 999)) {
            return info;
        }
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            Slog.e(TAG, "fixServiceInfo mPms is null");
            return info;
        }
        String[] packages = packageManagerService.getPackagesForUid(callingUid);
        if (packages == null) {
            return info;
        }
        for (String pkgName : packages) {
            if (this.mUtil.isMultiApp(pkgName)) {
                return PackageParser.generateServiceInfo(s, flags, ps.readUserState(0), 0);
            }
        }
        return info;
    }

    public int fixIntentAndUserId(int userId, Intent intent, String pkg) {
        if (!this.mSupportMultiApp || intent == null || pkg == null) {
            return userId;
        }
        String pkgAction = intent.getAction();
        if (userId == 999 && this.mUtil.isMultiAllowedApp(pkg)) {
            if ("android.intent.action.PACKAGE_REMOVED".equals(pkgAction)) {
                pkgAction = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
                userId = 0;
                if (this.mDynamicDebug) {
                    Slog.d(TAG, "multi app: PMS " + pkgAction + " removed " + pkg + " userId= 0");
                }
                intent.addFlags(16777216);
                intent.setAction(pkgAction);
            }
            if ("android.intent.action.PACKAGE_ADDED".equals(pkgAction)) {
                userId = 0;
                if (this.mDynamicDebug) {
                    Slog.d(TAG, "multi app: PMS oppo.intent.action.MULTI_APP_PACKAGE_ADDED added " + pkg + " userId= 0");
                }
                intent.addFlags(16777216);
                intent.setAction("oppo.intent.action.MULTI_APP_PACKAGE_ADDED");
            }
        }
        return userId;
    }

    public ActivityInfo fixActivityInfo(ActivityInfo ai, int userId, PackageParser.Activity activity, int mFlags, PackageSetting ps) {
        if (!this.mSupportMultiApp) {
            return ai;
        }
        if (userId == 999 && ai == null) {
            ai = PackageParser.generateActivityInfo(activity, mFlags, ps.readUserState(0), 0);
            if (this.mDynamicDebug) {
                Slog.v(TAG, "multi app: userId changed from 999 to 0");
            }
        }
        return ai;
    }

    public ApplicationInfo fixApplicationInfo(ApplicationInfo ai, String packageName, int flags) {
        if (!this.mSupportMultiApp) {
            return ai;
        }
        if (this.mPmsInner == null) {
            Slog.d(TAG, "fixApplicationInfo mPmsInner is null");
            return ai;
        } else if (ai != null || Binder.getCallingUserHandle().getIdentifier() != 999 || this.mUtil.isMultiApp(packageName)) {
            return ai;
        } else {
            try {
                String[] packages = this.mPmsInner.getPackagesForUid(Binder.getCallingUid());
                if (packages == null) {
                    return ai;
                }
                for (String pkgName : packages) {
                    if (this.mUtil.isMultiApp(pkgName)) {
                        return this.mPmsInner.getApplicationInfoInternal(packageName, flags, Binder.getCallingUid(), 0);
                    }
                }
                return ai;
            } catch (Exception e) {
                return ai;
            }
        }
    }

    public ResolveInfo fixResolveInfo(ResolveInfo info, int userId, Intent intent, String resolvedType, int flags, boolean resolveForStart, int filterCallingUid) {
        if (!this.mSupportMultiApp) {
            return info;
        }
        IColorPackageManagerServiceInner iColorPackageManagerServiceInner = this.mPmsInner;
        if (iColorPackageManagerServiceInner == null) {
            Slog.d(TAG, "fixResolveInfo mPmsInner is null");
            return info;
        }
        if (info == null) {
            if (userId == 999) {
                try {
                    String[] packages = iColorPackageManagerServiceInner.getPackagesForUid(filterCallingUid);
                    if (packages != null) {
                        for (String pkgName : packages) {
                            if (this.mUtil.isMultiApp(pkgName)) {
                                return this.mPmsInner.resolveIntentInternal(intent, resolvedType, flags, 0, false, filterCallingUid);
                            }
                        }
                    }
                    return info;
                } catch (Exception e) {
                }
            }
        }
        return info;
    }

    public ParceledListSlice<ResolveInfo> fixIntentReceivers(int uid, ParceledListSlice<ResolveInfo> parceledList, Intent intent, String resolvedType, int flags, int userId, boolean allowDynamicSplits) {
        if (!this.mSupportMultiApp) {
            return parceledList;
        }
        if (this.mPmsInner == null) {
            Slog.d(TAG, "fixIntentReceivers mPmsInner is null");
            return parceledList;
        }
        if (userId == 999) {
            if (parceledList == null || parceledList.getList().isEmpty()) {
                try {
                    try {
                        String[] packages = this.mPmsInner.getPackagesForUid(uid);
                        if (packages != null) {
                            for (String pkgName : packages) {
                                if (this.mUtil.isMultiApp(pkgName)) {
                                    return new ParceledListSlice<>(this.mPmsInner.queryIntentReceiversInternal(intent, resolvedType, flags, 0, allowDynamicSplits));
                                }
                            }
                        }
                        return parceledList;
                    } catch (Exception e) {
                    }
                } catch (Exception e2) {
                }
            }
        }
        return parceledList;
    }

    public ParceledListSlice<ResolveInfo> fixIntentServices(ParceledListSlice<ResolveInfo> parceledList, int userId, Intent intent, String resolvedType, int flags, int callingUid, boolean includeInstantApps) {
        if (!this.mSupportMultiApp) {
            return parceledList;
        }
        if (this.mPmsInner == null) {
            Slog.d(TAG, "fixIntentServices mPmsInner is null");
            return parceledList;
        }
        if (userId == 999) {
            if (parceledList == null || parceledList.getList().isEmpty()) {
                try {
                    try {
                        String[] packages = this.mPmsInner.getPackagesForUid(callingUid);
                        if (packages != null) {
                            for (String pkgName : packages) {
                                if (this.mUtil.isMultiApp(pkgName)) {
                                    return new ParceledListSlice<>(this.mPmsInner.queryIntentServicesInternal(intent, resolvedType, flags, 0, callingUid, includeInstantApps));
                                }
                            }
                        }
                        return parceledList;
                    } catch (Exception e) {
                    }
                } catch (Exception e2) {
                }
            }
        }
        return parceledList;
    }

    public ParceledListSlice<ResolveInfo> fixIntentContentProviders(int uid, int userId, ParceledListSlice<ResolveInfo> parceledList, Intent intent, String resolvedType, int flags) {
        if (!this.mSupportMultiApp) {
            return parceledList;
        }
        if (this.mPmsInner == null) {
            Slog.d(TAG, "fixIntentContentProviders mPmsInner is null");
            return parceledList;
        } else if (userId != 999) {
            return parceledList;
        } else {
            if (!(parceledList == null || parceledList.getList().isEmpty())) {
                return parceledList;
            }
            try {
                String[] packages = this.mPmsInner.getPackagesForUid(uid);
                if (packages == null) {
                    return parceledList;
                }
                for (String pkgName : packages) {
                    if (this.mUtil.isMultiApp(pkgName)) {
                        return new ParceledListSlice<>(this.mPmsInner.queryIntentContentProvidersInternal(intent, resolvedType, flags, 0));
                    }
                }
                return parceledList;
            } catch (Exception e) {
                return parceledList;
            }
        }
    }

    public boolean shouldFilterPackage(String callingPkg, String getPkg, int userId) {
        if (callingPkg == null || getPkg == null || userId != 999) {
            return false;
        }
        if (("com.google.android.gms".equals(getPkg) || "com.android.vending".equals(getPkg) || "com.google.android.gsf".equals(getPkg)) && "com.whatsapp".equals(callingPkg)) {
            return true;
        }
        return false;
    }

    public int getLaunchedFromUid(String packageName, int fromUid) {
        if (!this.mSupportMultiApp || packageName == null) {
            return fromUid;
        }
        if ((!"com.google.android.packageinstaller".equals(packageName) && !"com.android.packageinstaller".equals(packageName)) || UserHandle.getUserId(fromUid) != 999) {
            return fromUid;
        }
        Log.d(TAG, "reset uid for multi app, uid = " + fromUid);
        return fromUid % 99900000;
    }

    public boolean isMultiUserInstallApp(String pkgName) {
        if (!this.mSupportMultiApp) {
            return false;
        }
        return this.mUtil.isMultiUserInstallApp(pkgName);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorMultiAppManagerService.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public void updateMultiUserInstallAppState(String name, boolean mainInstalled, boolean multiInstalled) {
        if (this.mUtil == null) {
            this.mUtil = ColorMultiAppManagerUtil.getInstance();
        }
        this.mUtil.updateMultiUserInstallAppState(name, mainInstalled, multiInstalled);
    }

    public void checkMultiUserInstallApp() {
        if (this.mUtil == null) {
            this.mUtil = ColorMultiAppManagerUtil.getInstance();
        }
        this.mUtil.checkMultiUserInstallApp();
    }

    public void syncPermissionsAfterOta() {
        PermissionInfo[] permissionInfos;
        int i;
        int i2;
        String permission;
        Log.i(TAG, "syncPermissionsAfterOta: ...");
        for (String pkgName : getCreatedMultiApp()) {
            int i3 = 0;
            PackageInfo packageInfo = this.mPms.getPackageInfo(pkgName, 4096, 0);
            if (packageInfo != null) {
                int mainUid = packageInfo.applicationInfo.uid;
                int cloneUid = UserHandle.getUid(USER_ID, UserHandle.getAppId(mainUid));
                int i4 = 1;
                if (!(this.mAms.mAppOpsService.checkOperationRaw(87, cloneUid, pkgName) == 0)) {
                    this.mAms.mAppOpsService.setMode(87, cloneUid, pkgName, 0);
                }
                if (this.mAms.mAppOpsService.checkOperationRaw(24, mainUid, pkgName) == 0) {
                    this.mAms.mAppOpsService.setMode(24, cloneUid, pkgName, 0);
                }
                PermissionInfo[] permissionInfos2 = packageInfo.permissions;
                int i5 = 0;
                while (i5 < packageInfo.requestedPermissions.length) {
                    String permission2 = packageInfo.requestedPermissions[i5];
                    int permissionFlag = packageInfo.requestedPermissionsFlags[i5];
                    PermissionInfo requestPermissionInfo = this.mPms.getPermissionInfo(permission2, pkgName, i3);
                    StringBuilder sb = new StringBuilder();
                    sb.append("syncPermissionsAfterOta: permission: ");
                    sb.append(permission2);
                    sb.append(" getProtection: ");
                    sb.append(requestPermissionInfo == null ? "null" : Integer.valueOf(requestPermissionInfo.getProtection()));
                    Log.i(TAG, sb.toString());
                    if (requestPermissionInfo == null) {
                        i = i5;
                        permissionInfos = permissionInfos2;
                        i2 = i4;
                    } else if (i4 == requestPermissionInfo.getProtection()) {
                        Log.i(TAG, "enter syncPermissionsAfterOta: permission: " + permission2);
                        if ("android.permission.WRITE_EXTERNAL_STORAGE".equals(permission2) || "android.permission.READ_EXTERNAL_STORAGE".equals(permission2)) {
                            int clonePermissionFlag = this.mPms.getPermissionFlags(permission2, pkgName, (int) USER_ID) | 8192;
                            permission = permission2;
                            i = i5;
                            permissionInfos = permissionInfos2;
                            i2 = i4;
                            this.mPms.updatePermissionFlags(permission2, pkgName, clonePermissionFlag, clonePermissionFlag, false, (int) USER_ID);
                        } else {
                            permission = permission2;
                            i = i5;
                            permissionInfos = permissionInfos2;
                            i2 = i4;
                        }
                        if ((permissionFlag & 2) != 0) {
                            this.mPms.grantRuntimePermission(pkgName, permission, (int) USER_ID);
                        }
                    } else {
                        i = i5;
                        permissionInfos = permissionInfos2;
                        i2 = i4;
                    }
                    i5 = i + 1;
                    i4 = i2;
                    permissionInfos2 = permissionInfos;
                    i3 = 0;
                }
            }
        }
    }

    public int[] noticeMultiApp(String action, int userId, int[] users) {
        boolean isMultiAppUserExisting = false;
        UserManagerService userManagerService = this.mUserManager;
        if (userManagerService != null) {
            isMultiAppUserExisting = userManagerService.exists((int) USER_ID);
        }
        if (!this.mSupportMultiApp || !isMultiAppUserExisting || userId != 0 || !"android.intent.action.HEADSET_PLUG".equals(action)) {
            return null;
        }
        return new int[]{userId, 999};
    }

    private static OppoBaseIntent typeCasting(Intent intent) {
        if (intent != null) {
            return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
        }
        return null;
    }
}
