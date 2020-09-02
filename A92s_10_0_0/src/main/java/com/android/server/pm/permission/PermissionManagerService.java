package com.android.server.pm.permission;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoPackageManagerInternal;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.permission.PermissionControllerManager;
import android.permission.PermissionManager;
import android.permission.PermissionManagerInternal;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.Watchdog;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceUtils;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.SharedUserSetting;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.policy.PermissionPolicyInternal;
import com.android.server.policy.SoftRestrictedPermissionPolicy;
import com.mediatek.cta.CtaManagerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import libcore.util.EmptyArray;

public class PermissionManagerService {
    private static final long BACKUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final int BLOCKING_PERMISSION_FLAGS = 52;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    public static boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final Map<String, String> FULLER_PERMISSION_MAP = new HashMap();
    private static final int GRANT_DENIED = 1;
    private static final int GRANT_INSTALL = 2;
    private static final int GRANT_RUNTIME = 3;
    private static final int GRANT_UPGRADE = 4;
    private static final int MAX_PERMISSION_TREE_FOOTPRINT = 32768;
    private static final String TAG = "PackageManager";
    private static final int UPDATE_PERMISSIONS_ALL = 1;
    private static final int UPDATE_PERMISSIONS_REPLACE_ALL = 4;
    private static final int UPDATE_PERMISSIONS_REPLACE_PKG = 2;
    private static final int USER_PERMISSION_FLAGS = 3;
    @GuardedBy({"mLock"})
    private ArrayMap<String, List<String>> mBackgroundPermissions;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final DefaultPermissionGrantPolicy mDefaultPermissionGrantPolicy;
    private final int[] mGlobalGids;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    @GuardedBy({"mLock"})
    private final SparseBooleanArray mHasNoDelayedPermBackup = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public final Object mLock;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final OppoPackageManagerInternal mOppoPackageManagerInt;
    private final PackageManagerInternal mPackageManagerInt;
    private PermissionControllerManager mPermissionControllerManager;
    @GuardedBy({"mLock"})
    private PermissionPolicyInternal mPermissionPolicyInternal;
    @GuardedBy({"mLock"})
    private ArraySet<String> mPrivappPermissionsViolations;
    @GuardedBy({"mLock"})
    private final ArrayList<PermissionManagerInternal.OnRuntimePermissionStateChangedListener> mRuntimePermissionStateChangedListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public final PermissionSettings mSettings;
    private final SparseArray<ArraySet<String>> mSystemPermissions;
    @GuardedBy({"mLock"})
    private boolean mSystemReady;
    private final UserManagerInternal mUserManagerInt;

    static {
        FULLER_PERMISSION_MAP.put("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION");
        FULLER_PERMISSION_MAP.put("android.permission.INTERACT_ACROSS_USERS", "android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    PermissionManagerService(Context context, Object externalLock) {
        this.mContext = context;
        this.mLock = externalLock;
        this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mUserManagerInt = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mSettings = new PermissionSettings(this.mLock);
        this.mHandlerThread = new ServiceThread(TAG, 10, true);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        Watchdog.getInstance().addThread(this.mHandler);
        this.mDefaultPermissionGrantPolicy = new DefaultPermissionGrantPolicy(context, this.mHandlerThread.getLooper(), this);
        this.mOppoPackageManagerInt = (OppoPackageManagerInternal) LocalServices.getService(OppoPackageManagerInternal.class);
        SystemConfig systemConfig = SystemConfig.getInstance();
        this.mSystemPermissions = systemConfig.getSystemPermissions();
        this.mGlobalGids = systemConfig.getGlobalGids();
        ArrayMap<String, SystemConfig.PermissionEntry> permConfig = SystemConfig.getInstance().getPermissions();
        synchronized (this.mLock) {
            for (int i = 0; i < permConfig.size(); i++) {
                SystemConfig.PermissionEntry perm = permConfig.valueAt(i);
                BasePermission bp = this.mSettings.getPermissionLocked(perm.name);
                if (bp == null) {
                    bp = new BasePermission(perm.name, PackageManagerService.PLATFORM_PACKAGE_NAME, 1);
                    this.mSettings.putPermissionLocked(perm.name, bp);
                }
                if (perm.gids != null) {
                    bp.setGids(perm.gids, perm.perUser);
                }
            }
        }
        PermissionManagerServiceInternalImpl localService = new PermissionManagerServiceInternalImpl();
        LocalServices.addService(PermissionManagerServiceInternal.class, localService);
        LocalServices.addService(PermissionManagerInternal.class, localService);
    }

    public static PermissionManagerServiceInternal create(Context context, Object externalLock) {
        PermissionManagerServiceInternal permMgrInt = (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
        if (permMgrInt != null) {
            return permMgrInt;
        }
        new PermissionManagerService(context, externalLock);
        return (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
    }

    /* access modifiers changed from: package-private */
    public BasePermission getPermission(String permName) {
        BasePermission permissionLocked;
        synchronized (this.mLock) {
            permissionLocked = this.mSettings.getPermissionLocked(permName);
        }
        return permissionLocked;
    }

    /* access modifiers changed from: private */
    public int checkPermission(String permName, String pkgName, int callingUid, int userId) {
        PackageParser.Package pkg;
        if (!this.mUserManagerInt.exists(userId) || (pkg = this.mPackageManagerInt.getPackage(pkgName)) == null || pkg.mExtras == null || this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
            return -1;
        }
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        boolean instantApp = ps.getInstantApp(userId);
        PermissionsState permissionsState = ps.getPermissionsState();
        if (permissionsState.hasPermission(permName, userId)) {
            if (!instantApp) {
                return 0;
            }
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(permName);
                if (bp != null && bp.isInstant()) {
                    return 0;
                }
            }
        }
        if (isImpliedPermissionGranted(permissionsState, permName, userId)) {
            return 0;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00db A[RETURN] */
    public int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
        int callingUserId = UserHandle.getUserId(callingUid);
        boolean isUidInstantApp = true;
        boolean isCallerInstantApp = this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null;
        if (this.mPackageManagerInt.getInstantAppPackageName(uid) == null) {
            isUidInstantApp = false;
        }
        int userId = UserHandle.getUserId(uid);
        if (!this.mUserManagerInt.exists(userId)) {
            return -1;
        }
        if (pkg != null) {
            if (pkg.mSharedUserId != null) {
                if (isCallerInstantApp) {
                    Slog.d(TAG, "Permission Denied: checkUidPermission, isCallerInstantApp, permName: " + permName + ", pkgName: " + pkg.packageName + ", uid: " + uid + ", callingUid: " + callingUid);
                    return -1;
                }
            } else if (this.mPackageManagerInt.filterAppAccess(pkg, callingUid, callingUserId)) {
                Slog.d(TAG, "Permission Denied: checkUidPermission, filterAppAccess, permName: " + permName + ", pkgName: " + pkg.packageName + ", uid: " + uid + ", callingUid: " + callingUid);
                return -1;
            }
            PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
            if ((!permissionsState.hasPermission(permName, userId) || (isUidInstantApp && !this.mSettings.isPermissionInstant(permName))) && !isImpliedPermissionGranted(permissionsState, permName, userId)) {
                return -1;
            }
            return 0;
        }
        ArraySet<String> perms = this.mSystemPermissions.get(uid);
        if (perms != null) {
            if (perms.contains(permName)) {
                return 0;
            }
            if (FULLER_PERMISSION_MAP.containsKey(permName) && perms.contains(FULLER_PERMISSION_MAP.get(permName))) {
                return 0;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public byte[] backupRuntimePermissions(UserHandle user) {
        CompletableFuture<byte[]> backup = new CompletableFuture<>();
        PermissionControllerManager permissionControllerManager = this.mPermissionControllerManager;
        Executor mainExecutor = this.mContext.getMainExecutor();
        Objects.requireNonNull(backup);
        permissionControllerManager.getRuntimePermissionBackup(user, mainExecutor, new PermissionControllerManager.OnGetRuntimePermissionBackupCallback(backup) {
            /* class com.android.server.pm.permission.$$Lambda$js2BSmz1ucAEj8fgl3jw5trxIjw */
            private final /* synthetic */ CompletableFuture f$0;

            {
                this.f$0 = r1;
            }

            public final void onGetRuntimePermissionsBackup(byte[] bArr) {
                this.f$0.complete(bArr);
            }
        });
        try {
            return backup.get(BACKUP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Slog.e(TAG, "Cannot create permission backup for " + user, e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void restoreRuntimePermissions(byte[] backup, UserHandle user) {
        synchronized (this.mLock) {
            this.mHasNoDelayedPermBackup.delete(user.getIdentifier());
            this.mPermissionControllerManager.restoreRuntimePermissionBackup(backup, user);
        }
    }

    /* access modifiers changed from: private */
    public void restoreDelayedRuntimePermissions(String packageName, UserHandle user) {
        synchronized (this.mLock) {
            if (!this.mHasNoDelayedPermBackup.get(user.getIdentifier(), false)) {
                this.mPermissionControllerManager.restoreDelayedRuntimePermissionBackup(packageName, user, this.mContext.getMainExecutor(), new Consumer(user) {
                    /* class com.android.server.pm.permission.$$Lambda$PermissionManagerService$KZ0FIR02GsOfMAAOdWzIbkVHHM */
                    private final /* synthetic */ UserHandle f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionManagerService.this.lambda$restoreDelayedRuntimePermissions$0$PermissionManagerService(this.f$1, (Boolean) obj);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$restoreDelayedRuntimePermissions$0$PermissionManagerService(UserHandle user, Boolean hasMoreBackup) {
        if (!hasMoreBackup.booleanValue()) {
            synchronized (this.mLock) {
                this.mHasNoDelayedPermBackup.put(user.getIdentifier(), true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void addOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
        synchronized (this.mLock) {
            this.mRuntimePermissionStateChangedListeners.add(listener);
        }
    }

    /* access modifiers changed from: private */
    public void removeOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
        synchronized (this.mLock) {
            this.mRuntimePermissionStateChangedListeners.remove(listener);
        }
    }

    private void notifyRuntimePermissionStateChanged(String packageName, int userId) {
        FgThread.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g.INSTANCE, this, packageName, Integer.valueOf(userId)));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        if (r2 >= r0) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        r1.get(r2).onRuntimePermissionStateChanged(r5, r6);
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        r0 = r1.size();
        r2 = 0;
     */
    public void doNotifyRuntimePermissionStateChanged(String packageName, int userId) {
        synchronized (this.mLock) {
            if (!this.mRuntimePermissionStateChangedListeners.isEmpty()) {
                ArrayList<PermissionManagerInternal.OnRuntimePermissionStateChangedListener> listeners = new ArrayList<>(this.mRuntimePermissionStateChangedListeners);
            }
        }
    }

    private static boolean isImpliedPermissionGranted(PermissionsState permissionsState, String permName, int userId) {
        if (!CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported() && FULLER_PERMISSION_MAP.containsKey(permName) && permissionsState.hasPermission(FULLER_PERMISSION_MAP.get(permName), userId)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
        PermissionGroupInfo generatePermissionGroupInfo;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            generatePermissionGroupInfo = PackageParser.generatePermissionGroupInfo(this.mSettings.mPermissionGroups.get(groupName), flags);
        }
        return generatePermissionGroupInfo;
    }

    /* access modifiers changed from: private */
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
        ArrayList<PermissionGroupInfo> out;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            out = new ArrayList<>(this.mSettings.mPermissionGroups.size());
            for (PackageParser.PermissionGroup pg : this.mSettings.mPermissionGroups.values()) {
                out.add(PackageParser.generatePermissionGroupInfo(pg, flags));
            }
        }
        return out;
    }

    /* access modifiers changed from: private */
    public PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            BasePermission bp = this.mSettings.getPermissionLocked(permName);
            if (bp == null) {
                return null;
            }
            PermissionInfo generatePermissionInfo = bp.generatePermissionInfo(adjustPermissionProtectionFlagsLocked(bp.getProtectionLevel(), packageName, callingUid), flags);
            return generatePermissionInfo;
        }
    }

    /* access modifiers changed from: private */
    public List<PermissionInfo> getPermissionInfoByGroup(String groupName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            if (groupName != null) {
                if (!this.mSettings.mPermissionGroups.containsKey(groupName)) {
                    return null;
                }
            }
            ArrayList<PermissionInfo> out = new ArrayList<>(10);
            for (BasePermission bp : this.mSettings.mPermissions.values()) {
                PermissionInfo pi = bp.generatePermissionInfo(groupName, flags);
                if (pi != null) {
                    out.add(pi);
                }
            }
            return out;
        }
    }

    private int adjustPermissionProtectionFlagsLocked(int protectionLevel, String packageName, int uid) {
        int appId;
        PackageParser.Package pkg;
        int protectionLevelMasked = protectionLevel & 3;
        if (protectionLevelMasked == 2 || (appId = UserHandle.getAppId(uid)) == 1000 || appId == 0 || appId == 2000 || (pkg = this.mPackageManagerInt.getPackage(packageName)) == null) {
            return protectionLevel;
        }
        if (pkg.applicationInfo.targetSdkVersion < 26) {
            return protectionLevelMasked;
        }
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps == null || ps.getAppId() == appId) {
            return protectionLevel;
        }
        return protectionLevel;
    }

    /* access modifiers changed from: private */
    public void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerServiceInternal.PermissionCallback permissionCallback) {
        String newPermissionGroupName;
        String oldPermissionGroupName;
        int[] userIds;
        int numUserIds;
        int userIdNum;
        String permissionName;
        PermissionManagerService permissionManagerService = this;
        int numOldPackagePermissions = oldPackage.permissions.size();
        ArrayMap<String, String> oldPermissionNameToGroupName = new ArrayMap<>(numOldPackagePermissions);
        for (int i = 0; i < numOldPackagePermissions; i++) {
            PackageParser.Permission permission = (PackageParser.Permission) oldPackage.permissions.get(i);
            if (permission.group != null) {
                oldPermissionNameToGroupName.put(permission.info.name, permission.group.info.name);
            }
        }
        int numNewPackagePermissions = newPackage.permissions.size();
        int newPermissionNum = 0;
        while (newPermissionNum < numNewPackagePermissions) {
            PackageParser.Permission newPermission = (PackageParser.Permission) newPackage.permissions.get(newPermissionNum);
            if ((newPermission.info.getProtection() & 1) != 0) {
                String permissionName2 = newPermission.info.name;
                String newPermissionGroupName2 = newPermission.group == null ? null : newPermission.group.info.name;
                String oldPermissionGroupName2 = oldPermissionNameToGroupName.get(permissionName2);
                if (newPermissionGroupName2 != null) {
                    if (!newPermissionGroupName2.equals(oldPermissionGroupName2)) {
                        int[] userIds2 = permissionManagerService.mUserManagerInt.getUserIds();
                        int numUserIds2 = userIds2.length;
                        int userIdNum2 = 0;
                        while (userIdNum2 < numUserIds2) {
                            int userId = userIds2[userIdNum2];
                            int numPackages = allPackageNames.size();
                            int packageNum = 0;
                            while (packageNum < numPackages) {
                                String packageName = allPackageNames.get(packageNum);
                                if (permissionManagerService.checkPermission(permissionName2, packageName, 0, userId) == 0) {
                                    userIdNum = userIdNum2;
                                    EventLog.writeEvent(1397638484, "72710897", Integer.valueOf(newPackage.applicationInfo.uid), "Revoking permission " + permissionName2 + " from package " + packageName + " as the group changed from " + oldPermissionGroupName2 + " to " + newPermissionGroupName2);
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                    try {
                                        revokeRuntimePermission(permissionName2, packageName, false, userId, permissionCallback);
                                    } catch (IllegalArgumentException e) {
                                        Slog.e(TAG, "Could not revoke " + permissionName + " from " + packageName, e);
                                    }
                                } else {
                                    userIdNum = userIdNum2;
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                }
                                packageNum++;
                                permissionName2 = permissionName;
                                numPackages = numPackages;
                                userIdNum2 = userIdNum;
                                numUserIds2 = numUserIds;
                                userIds2 = userIds;
                                oldPermissionGroupName2 = oldPermissionGroupName;
                                newPermissionGroupName2 = newPermissionGroupName;
                                permissionManagerService = this;
                            }
                            userIdNum2++;
                            numOldPackagePermissions = numOldPackagePermissions;
                            oldPermissionNameToGroupName = oldPermissionNameToGroupName;
                            permissionManagerService = this;
                        }
                    }
                }
            }
            newPermissionNum++;
            permissionManagerService = this;
            numOldPackagePermissions = numOldPackagePermissions;
            oldPermissionNameToGroupName = oldPermissionNameToGroupName;
        }
    }

    /* access modifiers changed from: private */
    public void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
        int N = pkg.permissions.size();
        for (int i = 0; i < N; i++) {
            PackageParser.Permission p = (PackageParser.Permission) pkg.permissions.get(i);
            p.info.flags &= -1073741825;
            synchronized (this.mLock) {
                if (pkg.applicationInfo.targetSdkVersion > 22) {
                    p.group = this.mSettings.mPermissionGroups.get(p.info.group);
                    if (PackageManagerService.DEBUG_PERMISSIONS && p.info.group != null && p.group == null) {
                        Slog.i(TAG, "Permission " + p.info.name + " from package " + p.info.packageName + " in an unknown group " + p.info.group);
                    }
                }
                if (p.tree) {
                    this.mSettings.putPermissionTreeLocked(p.info.name, BasePermission.createOrUpdate(this.mSettings.getPermissionTreeLocked(p.info.name), p, pkg, this.mSettings.getAllPermissionTreesLocked(), chatty));
                } else {
                    this.mSettings.putPermissionLocked(p.info.name, BasePermission.createOrUpdate(this.mSettings.getPermissionLocked(p.info.name), p, pkg, this.mSettings.getAllPermissionTreesLocked(), chatty));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
        int N = pkg.permissionGroups.size();
        StringBuilder r = null;
        for (int i = 0; i < N; i++) {
            PackageParser.PermissionGroup pg = (PackageParser.PermissionGroup) pkg.permissionGroups.get(i);
            PackageParser.PermissionGroup cur = this.mSettings.mPermissionGroups.get(pg.info.name);
            boolean isPackageUpdate = pg.info.packageName.equals(cur == null ? null : cur.info.packageName);
            if (cur == null || isPackageUpdate) {
                this.mSettings.mPermissionGroups.put(pg.info.name, pg);
                if (chatty && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    if (isPackageUpdate) {
                        r.append("UPD:");
                    }
                    r.append(pg.info.name);
                }
            } else {
                Slog.w(TAG, "Permission group " + pg.info.name + " from package " + pg.info.packageName + " ignored: original from " + cur.info.packageName);
                if (chatty && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append("DUP:");
                    r.append(pg.info.name);
                }
            }
        }
        if (r != null && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
            Log.d(TAG, "  Permission Groups: " + ((Object) r));
        }
    }

    /* access modifiers changed from: private */
    public void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
        ArraySet<String> appOpPkgs;
        ArraySet<String> appOpPkgs2;
        synchronized (this.mLock) {
            int N = pkg.permissions.size();
            StringBuilder r = null;
            for (int i = 0; i < N; i++) {
                PackageParser.Permission p = (PackageParser.Permission) pkg.permissions.get(i);
                BasePermission bp = this.mSettings.mPermissions.get(p.info.name);
                if (bp == null) {
                    bp = this.mSettings.mPermissionTrees.get(p.info.name);
                }
                if (bp != null && bp.isPermission(p)) {
                    bp.setPermission(null);
                    if (PackageManagerService.DEBUG_REMOVE && chatty) {
                        if (r == null) {
                            r = new StringBuilder(256);
                        } else {
                            r.append(' ');
                        }
                        r.append(p.info.name);
                    }
                }
                if (p.isAppOp() && (appOpPkgs2 = this.mSettings.mAppOpPermissionPackages.get(p.info.name)) != null) {
                    appOpPkgs2.remove(pkg.packageName);
                }
            }
            if (r != null && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + ((Object) r));
            }
            int N2 = pkg.requestedPermissions.size();
            for (int i2 = 0; i2 < N2; i2++) {
                String perm = (String) pkg.requestedPermissions.get(i2);
                if (this.mSettings.isPermissionAppOp(perm) && (appOpPkgs = this.mSettings.mAppOpPermissionPackages.get(perm)) != null) {
                    appOpPkgs.remove(pkg.packageName);
                    if (appOpPkgs.isEmpty()) {
                        this.mSettings.mAppOpPermissionPackages.remove(perm);
                    }
                }
            }
            if (0 != 0 && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + ((Object) null));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean addDynamicPermission(PermissionInfo info, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        boolean added;
        boolean changed;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            throw new SecurityException("Instant apps can't add permissions");
        } else if (info.labelRes == 0 && info.nonLocalizedLabel == null) {
            throw new SecurityException("Label must be specified in permission");
        } else {
            BasePermission tree = this.mSettings.enforcePermissionTree(info.name, callingUid);
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(info.name);
                added = bp == null;
                int fixedLevel = PermissionInfo.fixProtectionLevel(info.protectionLevel);
                if (added) {
                    enforcePermissionCapLocked(info, tree);
                    bp = new BasePermission(info.name, tree.getSourcePackageName(), 2);
                } else if (!bp.isDynamic()) {
                    throw new SecurityException("Not allowed to modify non-dynamic permission " + info.name);
                }
                changed = bp.addToTree(fixedLevel, info, tree);
                if (added) {
                    this.mSettings.putPermissionLocked(info.name, bp);
                }
            }
            if (changed && callback != null) {
                callback.onPermissionChanged();
            }
            return added;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0042, code lost:
        return;
     */
    public void removeDynamicPermission(String permName, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) == null) {
            this.mSettings.enforcePermissionTree(permName, callingUid);
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(permName);
                if (bp != null) {
                    if (bp.isDynamic()) {
                        Slog.wtf(TAG, "Not allowed to modify non-dynamic permission " + permName);
                    }
                    this.mSettings.removePermissionLocked(permName);
                    if (callback != null) {
                        callback.onPermissionRemoved();
                    }
                }
            }
        } else {
            throw new SecurityException("Instant applications don't have access to this method");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:496:0x0ad4, code lost:
        if (r12.isSystem() == false) goto L_0x0aed;
     */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0431  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x043c  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x043e  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0441  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x049f A[Catch:{ all -> 0x04f5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x04d2 A[Catch:{ all -> 0x04f5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x04d9 A[Catch:{ all -> 0x04f5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:265:0x059f A[SYNTHETIC, Splitter:B:265:0x059f] */
    /* JADX WARNING: Removed duplicated region for block: B:269:0x05b9  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x05cb  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x05cd  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x05da  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x05dc  */
    /* JADX WARNING: Removed duplicated region for block: B:282:0x05df  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0611  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0618  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x0737  */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x07b3 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x07fb  */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x07ff  */
    /* JADX WARNING: Removed duplicated region for block: B:413:0x0807 A[Catch:{ all -> 0x0821 }] */
    /* JADX WARNING: Removed duplicated region for block: B:519:0x0b33  */
    /* JADX WARNING: Removed duplicated region for block: B:522:0x0b3a A[LOOP:5: B:521:0x0b38->B:522:0x0b3a, LOOP_END] */
    private void restorePermissionState(PackageParser.Package pkg, boolean replace, String packageOfInterest, PermissionManagerServiceInternal.PermissionCallback callback) {
        PermissionsState origPermissions;
        boolean runtimePermissionsRevoked;
        Object obj;
        PackageSetting ps;
        int i;
        String upgradedActivityRecognitionPermission;
        Object obj2;
        int[] currentUserIds;
        PackageSetting ps2;
        ArraySet<String> newImplicitPermissions;
        PackageParser.Package packageR;
        String str;
        PermissionsState origPermissions2;
        boolean z;
        boolean pkgReviewRequired;
        String permName;
        ArraySet<String> newImplicitPermissions2;
        int grant;
        boolean allowedSig;
        int grant2;
        boolean pkgReviewRequired2;
        int userId;
        boolean permissionPolicyInitialized;
        PermissionsState.PermissionState permState;
        int flags;
        boolean wasChanged;
        int[] updatedUserIds;
        int i2;
        String perm;
        PackageParser.Package packageR2;
        String str2;
        int flags2;
        boolean z2;
        boolean pkgReviewRequired3;
        boolean hardRestricted;
        int flags3;
        boolean permissionPolicyInitialized2;
        int userId2;
        boolean wasChanged2;
        PermissionsState origPermissions3;
        PermissionsState.PermissionState permState2;
        ArraySet<String> newImplicitPermissions3;
        PermissionManagerService permissionManagerService = this;
        PackageParser.Package packageR3 = pkg;
        boolean z3 = replace;
        String str3 = packageOfInterest;
        PackageSetting ps3 = (PackageSetting) packageR3.mExtras;
        if (ps3 != null) {
            PermissionsState permissionsState = ps3.getPermissionsState();
            int[] currentUserIds2 = UserManagerService.getInstance().getUserIds();
            boolean runtimePermissionsRevoked2 = false;
            int[] updatedUserIds2 = EMPTY_INT_ARRAY;
            if (z3) {
                ps3.setInstallPermissionsFixed(false);
                if (!ps3.isSharedUser()) {
                    PermissionsState origPermissions4 = new PermissionsState(permissionsState);
                    permissionsState.reset();
                    origPermissions = origPermissions4;
                    runtimePermissionsRevoked = false;
                } else {
                    synchronized (permissionManagerService.mLock) {
                        updatedUserIds2 = permissionManagerService.revokeUnusedSharedUserPermissionsLocked(ps3.getSharedUser(), UserManagerService.getInstance().getUserIds());
                        if (!ArrayUtils.isEmpty(updatedUserIds2)) {
                            runtimePermissionsRevoked2 = true;
                        }
                    }
                    origPermissions = permissionsState;
                    runtimePermissionsRevoked = runtimePermissionsRevoked2;
                }
            } else {
                origPermissions = permissionsState;
                runtimePermissionsRevoked = false;
            }
            permissionsState.setGlobalGids(permissionManagerService.mGlobalGids);
            Object obj3 = permissionManagerService.mLock;
            synchronized (obj3) {
                try {
                    ArraySet<String> newImplicitPermissions4 = new ArraySet<>();
                    int N = packageR3.requestedPermissions.size();
                    boolean pkgReviewRequired4 = permissionManagerService.isPackageNeedsReview(packageR3, ps3.getSharedUser());
                    int[] updatedUserIds3 = updatedUserIds2;
                    int i3 = 0;
                    boolean changedInstallPermission = false;
                    while (i3 < N) {
                        try {
                            String permName2 = (String) packageR3.requestedPermissions.get(i3);
                            BasePermission bp = permissionManagerService.mSettings.getPermissionLocked(permName2);
                            boolean appSupportsRuntimePermissions = packageR3.applicationInfo.targetSdkVersion >= 23;
                            try {
                                if (PackageManagerService.DEBUG_INSTALL) {
                                    upgradedActivityRecognitionPermission = null;
                                    i = i3;
                                    try {
                                        Log.i(TAG, "Package " + packageR3.packageName + " checking " + permName2 + ": " + bp);
                                    } catch (Throwable th) {
                                        th = th;
                                        obj = obj3;
                                    }
                                } else {
                                    i = i3;
                                    upgradedActivityRecognitionPermission = null;
                                }
                                if (bp != null) {
                                    try {
                                        if (bp.getSourcePackageSetting() == null) {
                                            newImplicitPermissions2 = newImplicitPermissions4;
                                            obj2 = obj3;
                                            permName = permName2;
                                            ps2 = ps3;
                                            currentUserIds = currentUserIds2;
                                            origPermissions2 = origPermissions;
                                            z = replace;
                                            pkgReviewRequired = pkgReviewRequired4;
                                            packageR = packageR3;
                                            str = str3;
                                        } else {
                                            if (origPermissions.hasRequestedPermission(permName2)) {
                                                newImplicitPermissions = newImplicitPermissions4;
                                            } else if (packageR3.implicitPermissions.contains(permName2) || permName2.equals("android.permission.ACTIVITY_RECOGNITION")) {
                                                if (!packageR3.implicitPermissions.contains(permName2)) {
                                                    int numSplitPerms = PermissionManager.SPLIT_PERMISSIONS.size();
                                                    int splitPermNum = 0;
                                                    while (true) {
                                                        if (splitPermNum >= numSplitPerms) {
                                                            newImplicitPermissions = newImplicitPermissions4;
                                                            break;
                                                        }
                                                        PermissionManager.SplitPermissionInfo sp = (PermissionManager.SplitPermissionInfo) PermissionManager.SPLIT_PERMISSIONS.get(splitPermNum);
                                                        String splitPermName = sp.getSplitPermission();
                                                        if (!sp.getNewPermissions().contains(permName2)) {
                                                            newImplicitPermissions3 = newImplicitPermissions4;
                                                        } else if (origPermissions.hasInstallPermission(splitPermName)) {
                                                            upgradedActivityRecognitionPermission = splitPermName;
                                                            newImplicitPermissions4.add(permName2);
                                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                                newImplicitPermissions = newImplicitPermissions4;
                                                                Slog.i(TAG, permName2 + " is newly added for " + packageR3.packageName);
                                                            } else {
                                                                newImplicitPermissions = newImplicitPermissions4;
                                                            }
                                                        } else {
                                                            newImplicitPermissions3 = newImplicitPermissions4;
                                                        }
                                                        splitPermNum++;
                                                        numSplitPerms = numSplitPerms;
                                                        newImplicitPermissions4 = newImplicitPermissions3;
                                                    }
                                                } else {
                                                    newImplicitPermissions4.add(permName2);
                                                    if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                        Slog.i(TAG, permName2 + " is newly added for " + packageR3.packageName);
                                                        newImplicitPermissions = newImplicitPermissions4;
                                                    } else {
                                                        newImplicitPermissions = newImplicitPermissions4;
                                                    }
                                                }
                                            } else {
                                                newImplicitPermissions = newImplicitPermissions4;
                                            }
                                            if (packageR3.applicationInfo.isInstantApp() && !bp.isInstant()) {
                                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                    Log.i(TAG, "Denying non-ephemeral permission " + bp.getName() + " for package " + packageR3.packageName);
                                                    z = replace;
                                                    obj2 = obj3;
                                                    ps2 = ps3;
                                                    currentUserIds = currentUserIds2;
                                                    origPermissions2 = origPermissions;
                                                    pkgReviewRequired = pkgReviewRequired4;
                                                    packageR = packageR3;
                                                    str = str3;
                                                } else {
                                                    z = replace;
                                                    obj2 = obj3;
                                                    ps2 = ps3;
                                                    currentUserIds = currentUserIds2;
                                                    origPermissions2 = origPermissions;
                                                    pkgReviewRequired = pkgReviewRequired4;
                                                    packageR = packageR3;
                                                    str = str3;
                                                }
                                                updatedUserIds3 = updatedUserIds3;
                                                i3 = i + 1;
                                                permissionManagerService = this;
                                                pkgReviewRequired4 = pkgReviewRequired;
                                                str3 = str;
                                                packageR3 = packageR;
                                                N = N;
                                                newImplicitPermissions4 = newImplicitPermissions;
                                                ps3 = ps2;
                                                currentUserIds2 = currentUserIds;
                                                obj3 = obj2;
                                                origPermissions = origPermissions2;
                                                z3 = z;
                                                runtimePermissionsRevoked = runtimePermissionsRevoked;
                                            } else if (!bp.isRuntimeOnly() || appSupportsRuntimePermissions) {
                                                String perm2 = bp.getName();
                                                if (bp.isAppOp()) {
                                                    allowedSig = false;
                                                    grant = 1;
                                                    permissionManagerService.mSettings.addAppOpPackage(perm2, packageR3.packageName);
                                                } else {
                                                    allowedSig = false;
                                                    grant = 1;
                                                }
                                                if (bp.isNormal()) {
                                                    grant2 = 2;
                                                } else if (bp.isRuntime()) {
                                                    if (origPermissions.hasInstallPermission(bp.getName()) || upgradedActivityRecognitionPermission != null) {
                                                        grant2 = 4;
                                                    } else {
                                                        grant2 = 3;
                                                    }
                                                } else if (bp.isSignature()) {
                                                    boolean allowedSig2 = permissionManagerService.grantSignaturePermission(perm2, packageR3, bp, origPermissions);
                                                    if (allowedSig2) {
                                                        grant2 = 2;
                                                        allowedSig = allowedSig2;
                                                    } else {
                                                        allowedSig = allowedSig2;
                                                        grant2 = grant;
                                                    }
                                                } else {
                                                    grant2 = grant;
                                                }
                                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                    StringBuilder sb = new StringBuilder();
                                                    pkgReviewRequired2 = pkgReviewRequired4;
                                                    sb.append("Considering granting permission ");
                                                    sb.append(perm2);
                                                    sb.append(" to package ");
                                                    sb.append(packageR3.packageName);
                                                    Slog.i(TAG, sb.toString());
                                                } else {
                                                    pkgReviewRequired2 = pkgReviewRequired4;
                                                }
                                                if (grant2 != 1) {
                                                    if (!ps3.isSystem() && ps3.areInstallPermissionsFixed() && !bp.isRuntime() && !allowedSig && !origPermissions.hasInstallPermission(perm2) && !permissionManagerService.isNewPlatformPermissionForPackage(perm2, packageR3)) {
                                                        if (permissionManagerService.mOppoPackageManagerInt == null || permissionManagerService.mOppoPackageManagerInt.allowAddInstallPermForDataApp(packageR3.packageName)) {
                                                            Slog.d(TAG, "filter data app add install perms for " + packageR3.packageName);
                                                        } else {
                                                            grant2 = 1;
                                                        }
                                                    }
                                                    if (grant2 == 2) {
                                                        z = replace;
                                                        obj2 = obj3;
                                                        String perm3 = perm2;
                                                        ps2 = ps3;
                                                        currentUserIds = currentUserIds2;
                                                        PermissionsState origPermissions5 = origPermissions;
                                                        pkgReviewRequired = pkgReviewRequired2;
                                                        packageR = packageR3;
                                                        str = str3;
                                                        try {
                                                            int[] userIds = UserManagerService.getInstance().getUserIds();
                                                            int length = userIds.length;
                                                            int[] updatedUserIds4 = updatedUserIds3;
                                                            int i4 = 0;
                                                            while (i4 < length) {
                                                                try {
                                                                    int userId3 = userIds[i4];
                                                                    if (origPermissions5.getRuntimePermissionState(perm3, userId3) != null) {
                                                                        origPermissions5.revokeRuntimePermission(bp, userId3);
                                                                        origPermissions5.updatePermissionFlags(bp, userId3, 64511, 0);
                                                                        updatedUserIds4 = ArrayUtils.appendInt(updatedUserIds4, userId3);
                                                                    }
                                                                    i4++;
                                                                    origPermissions5 = origPermissions5;
                                                                    perm3 = perm3;
                                                                } catch (Throwable th2) {
                                                                    th = th2;
                                                                    obj = obj2;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            origPermissions2 = origPermissions5;
                                                            if (permissionsState.grantInstallPermission(bp) != -1) {
                                                                changedInstallPermission = true;
                                                                updatedUserIds3 = updatedUserIds4;
                                                            } else {
                                                                updatedUserIds3 = updatedUserIds4;
                                                            }
                                                            i3 = i + 1;
                                                            permissionManagerService = this;
                                                            pkgReviewRequired4 = pkgReviewRequired;
                                                            str3 = str;
                                                            packageR3 = packageR;
                                                            N = N;
                                                            newImplicitPermissions4 = newImplicitPermissions;
                                                            ps3 = ps2;
                                                            currentUserIds2 = currentUserIds;
                                                            obj3 = obj2;
                                                            origPermissions = origPermissions2;
                                                            z3 = z;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        } catch (Throwable th3) {
                                                            th = th3;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (grant2 == 3) {
                                                        String perm4 = perm2;
                                                        ps2 = ps3;
                                                        PermissionsState origPermissions6 = origPermissions;
                                                        try {
                                                            boolean hardRestricted2 = bp.isHardRestricted();
                                                            boolean softRestricted = bp.isSoftRestricted();
                                                            int length2 = currentUserIds2.length;
                                                            int[] updatedUserIds5 = updatedUserIds3;
                                                            int i5 = 0;
                                                            while (i5 < length2) {
                                                                try {
                                                                    userId = currentUserIds2[i5];
                                                                    if (this.mPermissionPolicyInternal != null) {
                                                                        try {
                                                                            if (this.mPermissionPolicyInternal.isInitialized(userId)) {
                                                                                permissionPolicyInitialized = true;
                                                                                permState = origPermissions6.getRuntimePermissionState(perm4, userId);
                                                                                if (permState == null) {
                                                                                    try {
                                                                                        flags = permState.getFlags();
                                                                                    } catch (Throwable th4) {
                                                                                        th = th4;
                                                                                        obj = obj3;
                                                                                    }
                                                                                } else {
                                                                                    flags = 0;
                                                                                }
                                                                                wasChanged = false;
                                                                                boolean restrictionExempt = (origPermissions6.getPermissionFlags(bp.name, userId) & 14336) == 0;
                                                                                boolean restrictionApplied = (origPermissions6.getPermissionFlags(bp.name, userId) & 16384) == 0;
                                                                                if (!appSupportsRuntimePermissions) {
                                                                                    if (!permissionPolicyInitialized || !hardRestricted2) {
                                                                                        origPermissions6 = origPermissions6;
                                                                                        flags2 = flags;
                                                                                        if (permissionPolicyInitialized && softRestricted && !restrictionExempt && !restrictionApplied) {
                                                                                            flags2 |= 16384;
                                                                                            wasChanged = true;
                                                                                        }
                                                                                    } else if (!restrictionExempt) {
                                                                                        if (permState != null) {
                                                                                            try {
                                                                                                if (permState.isGranted()) {
                                                                                                    origPermissions6 = origPermissions6;
                                                                                                    if (permissionsState.revokeRuntimePermission(bp, userId) != -1) {
                                                                                                        wasChanged = true;
                                                                                                    }
                                                                                                    if (restrictionApplied) {
                                                                                                        flags2 = flags | 16384;
                                                                                                        wasChanged = true;
                                                                                                    } else {
                                                                                                        flags2 = flags;
                                                                                                    }
                                                                                                }
                                                                                            } catch (Throwable th5) {
                                                                                                th = th5;
                                                                                                obj = obj3;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                throw th;
                                                                                            }
                                                                                        }
                                                                                        origPermissions6 = origPermissions6;
                                                                                        if (restrictionApplied) {
                                                                                        }
                                                                                    } else {
                                                                                        origPermissions6 = origPermissions6;
                                                                                        flags2 = flags;
                                                                                    }
                                                                                    if ((flags2 & 64) != 0) {
                                                                                        try {
                                                                                            perm = perm4;
                                                                                            i2 = i5;
                                                                                            obj2 = obj3;
                                                                                            pkgReviewRequired3 = pkgReviewRequired2;
                                                                                            if (CtaManagerFactory.getInstance().makeCtaManager().needClearReviewFlagAfterUpgrade(pkgReviewRequired3, bp.getSourcePackageName(), bp.getName())) {
                                                                                                flags2 &= -65;
                                                                                                wasChanged = true;
                                                                                            }
                                                                                        } catch (Throwable th6) {
                                                                                            th = th6;
                                                                                            obj = obj2;
                                                                                            while (true) {
                                                                                                break;
                                                                                            }
                                                                                            throw th;
                                                                                        }
                                                                                    } else {
                                                                                        obj2 = obj3;
                                                                                        i2 = i5;
                                                                                        perm = perm4;
                                                                                        pkgReviewRequired3 = pkgReviewRequired2;
                                                                                    }
                                                                                    if ((flags2 & 8) != 0) {
                                                                                        flags2 &= -9;
                                                                                        wasChanged = true;
                                                                                    } else if ((!permissionPolicyInitialized || !hardRestricted2 || restrictionExempt) && permState != null && permState.isGranted() && permissionsState.grantRuntimePermission(bp, userId) == -1) {
                                                                                        wasChanged = true;
                                                                                    }
                                                                                    try {
                                                                                        if (!CtaManagerFactory.getInstance().makeCtaManager().isPlatformPermission(bp.getSourcePackageName(), bp.getName()) || !pkgReviewRequired3) {
                                                                                            packageR2 = pkg;
                                                                                            z2 = replace;
                                                                                            str2 = packageOfInterest;
                                                                                        } else if ((flags2 & 64) != 0 || (flags2 & 16) != 0) {
                                                                                            packageR2 = pkg;
                                                                                            z2 = replace;
                                                                                            str2 = packageOfInterest;
                                                                                        } else if (!bp.isRemoved()) {
                                                                                            str2 = packageOfInterest;
                                                                                            if (str2 != null) {
                                                                                                packageR2 = pkg;
                                                                                                try {
                                                                                                    if (str2.equals(packageR2.packageName)) {
                                                                                                        z2 = replace;
                                                                                                        if (!z2) {
                                                                                                            flags2 |= 64;
                                                                                                            wasChanged = true;
                                                                                                            updatedUserIds = updatedUserIds5;
                                                                                                        }
                                                                                                    } else {
                                                                                                        z2 = replace;
                                                                                                    }
                                                                                                } catch (Throwable th7) {
                                                                                                    th = th7;
                                                                                                    obj = obj2;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    throw th;
                                                                                                }
                                                                                            } else {
                                                                                                packageR2 = pkg;
                                                                                                z2 = replace;
                                                                                            }
                                                                                        } else {
                                                                                            packageR2 = pkg;
                                                                                            z2 = replace;
                                                                                            str2 = packageOfInterest;
                                                                                        }
                                                                                        updatedUserIds = updatedUserIds5;
                                                                                    } catch (Throwable th8) {
                                                                                        th = th8;
                                                                                        obj = obj2;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                } else {
                                                                                    obj2 = obj3;
                                                                                    i2 = i5;
                                                                                    origPermissions6 = origPermissions6;
                                                                                    pkgReviewRequired3 = pkgReviewRequired2;
                                                                                    int flags4 = flags;
                                                                                    z2 = replace;
                                                                                    str2 = packageOfInterest;
                                                                                    perm = perm4;
                                                                                    packageR2 = pkg;
                                                                                    if (permState == null) {
                                                                                        updatedUserIds = updatedUserIds5;
                                                                                        try {
                                                                                            if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName()) && !bp.isRemoved()) {
                                                                                                wasChanged = true;
                                                                                                flags4 |= 72;
                                                                                            }
                                                                                        } catch (Throwable th9) {
                                                                                            th = th9;
                                                                                            obj = obj2;
                                                                                            while (true) {
                                                                                                break;
                                                                                            }
                                                                                            throw th;
                                                                                        }
                                                                                    } else {
                                                                                        updatedUserIds = updatedUserIds5;
                                                                                    }
                                                                                    try {
                                                                                        if (!permissionsState.hasRuntimePermission(bp.name, userId) && permissionsState.grantRuntimePermission(bp, userId) != -1) {
                                                                                            wasChanged = true;
                                                                                        }
                                                                                        if (permissionPolicyInitialized && ((hardRestricted2 || softRestricted) && !restrictionExempt && !restrictionApplied)) {
                                                                                            flags2 |= 16384;
                                                                                            wasChanged = true;
                                                                                        }
                                                                                    } catch (Throwable th10) {
                                                                                        th = th10;
                                                                                        obj = obj2;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                }
                                                                                if (!permissionPolicyInitialized) {
                                                                                    if (((!hardRestricted2 && !softRestricted) || restrictionExempt) && restrictionApplied) {
                                                                                        int flags5 = flags2 & -16385;
                                                                                        if (!appSupportsRuntimePermissions) {
                                                                                            flags3 = flags5 | 64;
                                                                                        } else {
                                                                                            flags3 = flags5;
                                                                                        }
                                                                                        wasChanged = true;
                                                                                    }
                                                                                    hardRestricted = hardRestricted2;
                                                                                    if (CtaManagerFactory.getInstance().makeCtaManager().isPlatformPermission(bp.getSourcePackageName(), bp.getName()) && pkgReviewRequired3 && (flags2 & 64) == 0 && !bp.isRemoved() && str2 != null && str2.equals(packageR2.packageName) && !z2) {
                                                                                        flags2 |= 64;
                                                                                        wasChanged = true;
                                                                                    }
                                                                                } else {
                                                                                    hardRestricted = hardRestricted2;
                                                                                }
                                                                                if (!wasChanged) {
                                                                                    try {
                                                                                        updatedUserIds5 = ArrayUtils.appendInt(updatedUserIds, userId);
                                                                                    } catch (Throwable th11) {
                                                                                        th = th11;
                                                                                        obj = obj2;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                } else {
                                                                                    updatedUserIds5 = updatedUserIds;
                                                                                }
                                                                                permissionsState.updatePermissionFlags(bp, userId, 64511, flags2);
                                                                                i5 = i2 + 1;
                                                                                pkgReviewRequired2 = pkgReviewRequired3;
                                                                                length2 = length2;
                                                                                currentUserIds2 = currentUserIds2;
                                                                                obj3 = obj2;
                                                                                hardRestricted2 = hardRestricted;
                                                                                perm4 = perm;
                                                                            }
                                                                        } catch (Throwable th12) {
                                                                            th = th12;
                                                                            obj = obj3;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    }
                                                                    permissionPolicyInitialized = false;
                                                                } catch (Throwable th13) {
                                                                    th = th13;
                                                                    obj = obj3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                                try {
                                                                    permState = origPermissions6.getRuntimePermissionState(perm4, userId);
                                                                    if (permState == null) {
                                                                    }
                                                                    wasChanged = false;
                                                                    if ((origPermissions6.getPermissionFlags(bp.name, userId) & 14336) == 0) {
                                                                    }
                                                                } catch (Throwable th14) {
                                                                    th = th14;
                                                                    obj = obj3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                                try {
                                                                    if ((origPermissions6.getPermissionFlags(bp.name, userId) & 16384) == 0) {
                                                                    }
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    if (!permissionPolicyInitialized) {
                                                                    }
                                                                    if (!wasChanged) {
                                                                    }
                                                                    permissionsState.updatePermissionFlags(bp, userId, 64511, flags2);
                                                                    i5 = i2 + 1;
                                                                    pkgReviewRequired2 = pkgReviewRequired3;
                                                                    length2 = length2;
                                                                    currentUserIds2 = currentUserIds2;
                                                                    obj3 = obj2;
                                                                    hardRestricted2 = hardRestricted;
                                                                    perm4 = perm;
                                                                } catch (Throwable th15) {
                                                                    th = th15;
                                                                    obj = obj3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            packageR = pkg;
                                                            z = replace;
                                                            str = packageOfInterest;
                                                            obj2 = obj3;
                                                            currentUserIds = currentUserIds2;
                                                            pkgReviewRequired = pkgReviewRequired2;
                                                            updatedUserIds3 = updatedUserIds5;
                                                            origPermissions2 = origPermissions6;
                                                            i3 = i + 1;
                                                            permissionManagerService = this;
                                                            pkgReviewRequired4 = pkgReviewRequired;
                                                            str3 = str;
                                                            packageR3 = packageR;
                                                            N = N;
                                                            newImplicitPermissions4 = newImplicitPermissions;
                                                            ps3 = ps2;
                                                            currentUserIds2 = currentUserIds;
                                                            obj3 = obj2;
                                                            origPermissions = origPermissions2;
                                                            z3 = z;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        } catch (Throwable th16) {
                                                            th = th16;
                                                            obj = obj3;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (grant2 == 4) {
                                                        try {
                                                            PermissionsState.PermissionState permState3 = origPermissions.getInstallPermissionState(perm2);
                                                            int flags6 = permState3 != null ? permState3.getFlags() : 0;
                                                            BasePermission bpToRevoke = upgradedActivityRecognitionPermission == null ? bp : permissionManagerService.mSettings.getPermissionLocked(upgradedActivityRecognitionPermission);
                                                            if (origPermissions.revokeInstallPermission(bpToRevoke) != -1) {
                                                                origPermissions.updatePermissionFlags(bpToRevoke, -1, 48127, 0);
                                                                changedInstallPermission = true;
                                                            }
                                                            boolean hardRestricted3 = bp.isHardRestricted();
                                                            boolean softRestricted2 = bp.isSoftRestricted();
                                                            int length3 = currentUserIds2.length;
                                                            int[] updatedUserIds6 = updatedUserIds3;
                                                            int i6 = 0;
                                                            ps2 = ps3;
                                                            int flags7 = flags6;
                                                            while (i6 < length3) {
                                                                try {
                                                                    int userId4 = currentUserIds2[i6];
                                                                    if (permissionManagerService.mPermissionPolicyInternal != null) {
                                                                        try {
                                                                            userId2 = userId4;
                                                                            if (permissionManagerService.mPermissionPolicyInternal.isInitialized(userId2)) {
                                                                                permissionPolicyInitialized2 = true;
                                                                                wasChanged2 = false;
                                                                                boolean restrictionExempt2 = (origPermissions.getPermissionFlags(bp.name, userId2) & 14336) == 0;
                                                                                boolean restrictionApplied2 = (origPermissions.getPermissionFlags(bp.name, userId2) & 16384) == 0;
                                                                                if (!appSupportsRuntimePermissions) {
                                                                                    if (!permissionPolicyInitialized2 || !hardRestricted3) {
                                                                                        permState2 = permState3;
                                                                                        origPermissions3 = origPermissions;
                                                                                        if (permissionPolicyInitialized2 && softRestricted2 && !restrictionExempt2 && !restrictionApplied2) {
                                                                                            flags7 |= 16384;
                                                                                            wasChanged2 = true;
                                                                                        }
                                                                                    } else if (!restrictionExempt2) {
                                                                                        if (permState3 == null || !permState3.isGranted()) {
                                                                                            permState2 = permState3;
                                                                                            origPermissions3 = origPermissions;
                                                                                        } else {
                                                                                            permState2 = permState3;
                                                                                            origPermissions3 = origPermissions;
                                                                                            if (permissionsState.revokeRuntimePermission(bp, userId2) != -1) {
                                                                                                wasChanged2 = true;
                                                                                            }
                                                                                        }
                                                                                        if (!restrictionApplied2) {
                                                                                            flags7 |= 16384;
                                                                                            wasChanged2 = true;
                                                                                        }
                                                                                    } else {
                                                                                        permState2 = permState3;
                                                                                        origPermissions3 = origPermissions;
                                                                                    }
                                                                                    if ((flags7 & 64) != 0) {
                                                                                        flags7 &= -65;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                    if ((flags7 & 8) != 0) {
                                                                                        flags7 &= -9;
                                                                                        wasChanged2 = true;
                                                                                    } else if (!permissionPolicyInitialized2 || !hardRestricted3 || restrictionExempt2) {
                                                                                        try {
                                                                                            if (permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                                                                                wasChanged2 = true;
                                                                                            }
                                                                                        } catch (Throwable th17) {
                                                                                            th = th17;
                                                                                            obj = obj3;
                                                                                            while (true) {
                                                                                                try {
                                                                                                    break;
                                                                                                } catch (Throwable th18) {
                                                                                                    th = th18;
                                                                                                }
                                                                                            }
                                                                                            throw th;
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    permState2 = permState3;
                                                                                    origPermissions3 = origPermissions;
                                                                                    if (!permissionsState.hasRuntimePermission(bp.name, userId2) && permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                                                                        flags7 |= 64;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                    if (permissionPolicyInitialized2 && ((hardRestricted3 || softRestricted2) && !restrictionExempt2 && !restrictionApplied2)) {
                                                                                        flags7 |= 16384;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                }
                                                                                if (permissionPolicyInitialized2 && (((!hardRestricted3 && !softRestricted2) || restrictionExempt2) && restrictionApplied2)) {
                                                                                    int flags8 = flags7 & -16385;
                                                                                    if (!appSupportsRuntimePermissions) {
                                                                                        flags8 |= 64;
                                                                                    }
                                                                                    wasChanged2 = true;
                                                                                    flags7 = flags8;
                                                                                }
                                                                                if (wasChanged2) {
                                                                                    updatedUserIds6 = ArrayUtils.appendInt(updatedUserIds6, userId2);
                                                                                }
                                                                                permissionsState.updatePermissionFlags(bp, userId2, 64511, flags7);
                                                                                i6++;
                                                                                permissionManagerService = this;
                                                                                perm2 = perm2;
                                                                                length3 = length3;
                                                                                permState3 = permState2;
                                                                                origPermissions = origPermissions3;
                                                                            }
                                                                        } catch (Throwable th19) {
                                                                            th = th19;
                                                                            obj = obj3;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } else {
                                                                        userId2 = userId4;
                                                                    }
                                                                    permissionPolicyInitialized2 = false;
                                                                    wasChanged2 = false;
                                                                    if ((origPermissions.getPermissionFlags(bp.name, userId2) & 14336) == 0) {
                                                                    }
                                                                    if ((origPermissions.getPermissionFlags(bp.name, userId2) & 16384) == 0) {
                                                                    }
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    int flags82 = flags7 & -16385;
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    wasChanged2 = true;
                                                                    flags7 = flags82;
                                                                    if (wasChanged2) {
                                                                    }
                                                                    permissionsState.updatePermissionFlags(bp, userId2, 64511, flags7);
                                                                    i6++;
                                                                    permissionManagerService = this;
                                                                    perm2 = perm2;
                                                                    length3 = length3;
                                                                    permState3 = permState2;
                                                                    origPermissions = origPermissions3;
                                                                } catch (Throwable th20) {
                                                                    th = th20;
                                                                    obj = obj3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            packageR = pkg;
                                                            z = replace;
                                                            str = packageOfInterest;
                                                            obj2 = obj3;
                                                            updatedUserIds3 = updatedUserIds6;
                                                            currentUserIds = currentUserIds2;
                                                            pkgReviewRequired = pkgReviewRequired2;
                                                            origPermissions2 = origPermissions;
                                                            i3 = i + 1;
                                                            permissionManagerService = this;
                                                            pkgReviewRequired4 = pkgReviewRequired;
                                                            str3 = str;
                                                            packageR3 = packageR;
                                                            N = N;
                                                            newImplicitPermissions4 = newImplicitPermissions;
                                                            ps3 = ps2;
                                                            currentUserIds2 = currentUserIds;
                                                            obj3 = obj2;
                                                            origPermissions = origPermissions2;
                                                            z3 = z;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        } catch (Throwable th21) {
                                                            th = th21;
                                                            obj = obj3;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (str3 == null || str3.equals(packageR3.packageName)) {
                                                        if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                            Slog.i(TAG, "Not granting permission " + perm2 + " to package " + packageR3.packageName + " because it was previously installed without");
                                                            z = replace;
                                                            obj2 = obj3;
                                                            ps2 = ps3;
                                                            currentUserIds = currentUserIds2;
                                                            origPermissions2 = origPermissions;
                                                            pkgReviewRequired = pkgReviewRequired2;
                                                            packageR = packageR3;
                                                            str = str3;
                                                        } else {
                                                            z = replace;
                                                            obj2 = obj3;
                                                            ps2 = ps3;
                                                            currentUserIds = currentUserIds2;
                                                            origPermissions2 = origPermissions;
                                                            pkgReviewRequired = pkgReviewRequired2;
                                                            packageR = packageR3;
                                                            str = str3;
                                                        }
                                                        updatedUserIds3 = updatedUserIds3;
                                                        i3 = i + 1;
                                                        permissionManagerService = this;
                                                        pkgReviewRequired4 = pkgReviewRequired;
                                                        str3 = str;
                                                        packageR3 = packageR;
                                                        N = N;
                                                        newImplicitPermissions4 = newImplicitPermissions;
                                                        ps3 = ps2;
                                                        currentUserIds2 = currentUserIds;
                                                        obj3 = obj2;
                                                        origPermissions = origPermissions2;
                                                        z3 = z;
                                                        runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                    } else {
                                                        z = replace;
                                                        obj2 = obj3;
                                                        ps2 = ps3;
                                                        currentUserIds = currentUserIds2;
                                                        origPermissions2 = origPermissions;
                                                        pkgReviewRequired = pkgReviewRequired2;
                                                        packageR = packageR3;
                                                        str = str3;
                                                        updatedUserIds3 = updatedUserIds3;
                                                        i3 = i + 1;
                                                        permissionManagerService = this;
                                                        pkgReviewRequired4 = pkgReviewRequired;
                                                        str3 = str;
                                                        packageR3 = packageR;
                                                        N = N;
                                                        newImplicitPermissions4 = newImplicitPermissions;
                                                        ps3 = ps2;
                                                        currentUserIds2 = currentUserIds;
                                                        obj3 = obj2;
                                                        origPermissions = origPermissions2;
                                                        z3 = z;
                                                        runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                    }
                                                } else {
                                                    z = replace;
                                                    obj2 = obj3;
                                                    ps2 = ps3;
                                                    currentUserIds = currentUserIds2;
                                                    origPermissions2 = origPermissions;
                                                    pkgReviewRequired = pkgReviewRequired2;
                                                    packageR = packageR3;
                                                    str = str3;
                                                    try {
                                                        if (permissionsState.revokeInstallPermission(bp) != -1) {
                                                            permissionsState.updatePermissionFlags(bp, -1, 64511, 0);
                                                            try {
                                                                Slog.i(TAG, "Un-granting permission " + perm2 + " from package " + packageR.packageName + " (protectionLevel=" + bp.getProtectionLevel() + " flags=0x" + Integer.toHexString(packageR.applicationInfo.flags) + ")");
                                                                changedInstallPermission = true;
                                                                updatedUserIds3 = updatedUserIds3;
                                                                i3 = i + 1;
                                                                permissionManagerService = this;
                                                                pkgReviewRequired4 = pkgReviewRequired;
                                                                str3 = str;
                                                                packageR3 = packageR;
                                                                N = N;
                                                                newImplicitPermissions4 = newImplicitPermissions;
                                                                ps3 = ps2;
                                                                currentUserIds2 = currentUserIds;
                                                                obj3 = obj2;
                                                                origPermissions = origPermissions2;
                                                                z3 = z;
                                                                runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            } catch (Throwable th22) {
                                                                th = th22;
                                                                obj = obj2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            if (bp.isAppOp() && PackageManagerService.DEBUG_PERMISSIONS && (str == null || str.equals(packageR.packageName))) {
                                                                Slog.i(TAG, "Not granting permission " + perm2 + " to package " + packageR.packageName + " (protectionLevel=" + bp.getProtectionLevel() + " flags=0x" + Integer.toHexString(packageR.applicationInfo.flags) + ")");
                                                            }
                                                            updatedUserIds3 = updatedUserIds3;
                                                            i3 = i + 1;
                                                            permissionManagerService = this;
                                                            pkgReviewRequired4 = pkgReviewRequired;
                                                            str3 = str;
                                                            packageR3 = packageR;
                                                            N = N;
                                                            newImplicitPermissions4 = newImplicitPermissions;
                                                            ps3 = ps2;
                                                            currentUserIds2 = currentUserIds;
                                                            obj3 = obj2;
                                                            origPermissions = origPermissions2;
                                                            z3 = z;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        }
                                                    } catch (Throwable th23) {
                                                        th = th23;
                                                        obj = obj2;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                }
                                            } else {
                                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                    Log.i(TAG, "Denying runtime-only permission " + bp.getName() + " for package " + packageR3.packageName);
                                                    z = replace;
                                                    obj2 = obj3;
                                                    ps2 = ps3;
                                                    currentUserIds = currentUserIds2;
                                                    origPermissions2 = origPermissions;
                                                    pkgReviewRequired = pkgReviewRequired4;
                                                    packageR = packageR3;
                                                    str = str3;
                                                } else {
                                                    z = replace;
                                                    obj2 = obj3;
                                                    ps2 = ps3;
                                                    currentUserIds = currentUserIds2;
                                                    origPermissions2 = origPermissions;
                                                    pkgReviewRequired = pkgReviewRequired4;
                                                    packageR = packageR3;
                                                    str = str3;
                                                }
                                                updatedUserIds3 = updatedUserIds3;
                                                i3 = i + 1;
                                                permissionManagerService = this;
                                                pkgReviewRequired4 = pkgReviewRequired;
                                                str3 = str;
                                                packageR3 = packageR;
                                                N = N;
                                                newImplicitPermissions4 = newImplicitPermissions;
                                                ps3 = ps2;
                                                currentUserIds2 = currentUserIds;
                                                obj3 = obj2;
                                                origPermissions = origPermissions2;
                                                z3 = z;
                                                runtimePermissionsRevoked = runtimePermissionsRevoked;
                                            }
                                        }
                                    } catch (Throwable th24) {
                                        th = th24;
                                        obj = obj3;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } else {
                                    newImplicitPermissions2 = newImplicitPermissions4;
                                    obj2 = obj3;
                                    permName = permName2;
                                    ps2 = ps3;
                                    currentUserIds = currentUserIds2;
                                    origPermissions2 = origPermissions;
                                    z = replace;
                                    pkgReviewRequired = pkgReviewRequired4;
                                    packageR = packageR3;
                                    str = str3;
                                }
                                if (str == null || str.equals(packageR.packageName)) {
                                    if (PackageManagerService.DEBUG_PERMISSIONS) {
                                        Slog.i(TAG, "Unknown permission " + permName + " in package " + packageR.packageName);
                                    }
                                }
                                updatedUserIds3 = updatedUserIds3;
                                i3 = i + 1;
                                permissionManagerService = this;
                                pkgReviewRequired4 = pkgReviewRequired;
                                str3 = str;
                                packageR3 = packageR;
                                N = N;
                                newImplicitPermissions4 = newImplicitPermissions;
                                ps3 = ps2;
                                currentUserIds2 = currentUserIds;
                                obj3 = obj2;
                                origPermissions = origPermissions2;
                                z3 = z;
                                runtimePermissionsRevoked = runtimePermissionsRevoked;
                            } catch (Throwable th25) {
                                th = th25;
                                obj = obj3;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th26) {
                            th = th26;
                            obj = obj3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    if (changedInstallPermission || z3) {
                        try {
                            if (!ps3.areInstallPermissionsFixed()) {
                            }
                        } catch (Throwable th27) {
                            th = th27;
                            obj = obj3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    if (!ps3.isUpdatedSystem()) {
                        ps = ps3;
                        obj = obj3;
                        try {
                            try {
                                int[] updatedUserIds7 = checkIfLegacyStorageOpsNeedToBeUpdated(packageR3, z3, setInitialGrantForNewImplicitPermissionsLocked(origPermissions, permissionsState, pkg, newImplicitPermissions4, revokePermissionsNoLongerImplicitLocked(permissionsState, packageR3, updatedUserIds3)));
                                if (callback != null) {
                                    callback.onPermissionUpdated(updatedUserIds7, runtimePermissionsRevoked);
                                }
                                for (int userId5 : updatedUserIds7) {
                                    notifyRuntimePermissionStateChanged(packageR3.packageName, userId5);
                                }
                            } catch (Throwable th28) {
                                th = th28;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th29) {
                            th = th29;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    ps = ps3;
                    try {
                        ps.setInstallPermissionsFixed(true);
                    } catch (Throwable th30) {
                        th = th30;
                        obj = obj3;
                    }
                    try {
                        obj = obj3;
                        int[] updatedUserIds72 = checkIfLegacyStorageOpsNeedToBeUpdated(packageR3, z3, setInitialGrantForNewImplicitPermissionsLocked(origPermissions, permissionsState, pkg, newImplicitPermissions4, revokePermissionsNoLongerImplicitLocked(permissionsState, packageR3, updatedUserIds3)));
                        if (callback != null) {
                        }
                        while (r2 < r0) {
                        }
                    } catch (Throwable th31) {
                        th = th31;
                        obj = obj3;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th32) {
                    th = th32;
                    obj = obj3;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private int[] revokePermissionsNoLongerImplicitLocked(PermissionsState ps, PackageParser.Package pkg, int[] updatedUserIds) {
        boolean supportsRuntimePermissions;
        PackageParser.Package packageR = pkg;
        String pkgName = packageR.packageName;
        boolean supportsRuntimePermissions2 = packageR.applicationInfo.targetSdkVersion >= 23;
        int[] users = UserManagerService.getInstance().getUserIds();
        int numUsers = users.length;
        int i = 0;
        int[] updatedUserIds2 = updatedUserIds;
        while (i < numUsers) {
            int userId = users[i];
            for (String permission : ps.getPermissions(userId)) {
                if (packageR.implicitPermissions.contains(permission)) {
                    supportsRuntimePermissions = supportsRuntimePermissions2;
                } else if (!ps.hasInstallPermission(permission)) {
                    int flags = ps.getRuntimePermissionState(permission, userId).getFlags();
                    if ((flags & 128) != 0) {
                        BasePermission bp = this.mSettings.getPermissionLocked(permission);
                        int flagsToRemove = 128;
                        if ((flags & 52) != 0 || !supportsRuntimePermissions2) {
                            supportsRuntimePermissions = supportsRuntimePermissions2;
                        } else {
                            if (ps.revokeRuntimePermission(bp, userId) == -1) {
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                            } else if (PackageManagerService.DEBUG_PERMISSIONS) {
                                StringBuilder sb = new StringBuilder();
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                                sb.append("Revoking runtime permission ");
                                sb.append(permission);
                                sb.append(" for ");
                                sb.append(pkgName);
                                sb.append(" as it is now requested");
                                Slog.i(TAG, sb.toString());
                            } else {
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                            }
                            flagsToRemove = 128 | 3;
                        }
                        ps.updatePermissionFlags(bp, userId, flagsToRemove, 0);
                        updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId);
                    } else {
                        supportsRuntimePermissions = supportsRuntimePermissions2;
                    }
                } else {
                    supportsRuntimePermissions = supportsRuntimePermissions2;
                }
                packageR = pkg;
                supportsRuntimePermissions2 = supportsRuntimePermissions;
            }
            i++;
            packageR = pkg;
        }
        return updatedUserIds2;
    }

    private void inheritPermissionStateToNewImplicitPermissionLocked(ArraySet<String> sourcePerms, String newPerm, PermissionsState ps, PackageParser.Package pkg, int userId) {
        String pkgName = pkg.packageName;
        boolean isGranted = false;
        int flags = 0;
        int numSourcePerm = sourcePerms.size();
        for (int i = 0; i < numSourcePerm; i++) {
            String sourcePerm = sourcePerms.valueAt(i);
            if (ps.hasRuntimePermission(sourcePerm, userId) || ps.hasInstallPermission(sourcePerm)) {
                if (!isGranted) {
                    flags = 0;
                }
                isGranted = true;
                flags |= ps.getPermissionFlags(sourcePerm, userId);
            } else if (!isGranted) {
                flags |= ps.getPermissionFlags(sourcePerm, userId);
            }
        }
        if (isGranted) {
            if (PackageManagerService.DEBUG_PERMISSIONS) {
                Slog.i(TAG, newPerm + " inherits runtime perm grant from " + sourcePerms + " for " + pkgName);
            }
            ps.grantRuntimePermission(this.mSettings.getPermissionLocked(newPerm), userId);
        }
        ps.updatePermissionFlags(this.mSettings.getPermission(newPerm), userId, flags, flags);
    }

    private int[] checkIfLegacyStorageOpsNeedToBeUpdated(PackageParser.Package pkg, boolean replace, int[] updatedUserIds) {
        if (!replace || !pkg.applicationInfo.hasRequestedLegacyExternalStorage() || (!pkg.requestedPermissions.contains("android.permission.READ_EXTERNAL_STORAGE") && !pkg.requestedPermissions.contains("android.permission.WRITE_EXTERNAL_STORAGE"))) {
            return updatedUserIds;
        }
        return UserManagerService.getInstance().getUserIds();
    }

    private int[] setInitialGrantForNewImplicitPermissionsLocked(PermissionsState origPs, PermissionsState ps, PackageParser.Package pkg, ArraySet<String> newImplicitPermissions, int[] updatedUserIds) {
        boolean inheritsFromInstallPerm;
        String pkgName = pkg.packageName;
        ArrayMap<String, ArraySet<String>> newToSplitPerms = new ArrayMap<>();
        int numSplitPerms = PermissionManager.SPLIT_PERMISSIONS.size();
        for (int splitPermNum = 0; splitPermNum < numSplitPerms; splitPermNum++) {
            PermissionManager.SplitPermissionInfo spi = (PermissionManager.SplitPermissionInfo) PermissionManager.SPLIT_PERMISSIONS.get(splitPermNum);
            List<String> newPerms = spi.getNewPermissions();
            int numNewPerms = newPerms.size();
            for (int newPermNum = 0; newPermNum < numNewPerms; newPermNum++) {
                String newPerm = newPerms.get(newPermNum);
                ArraySet<String> splitPerms = newToSplitPerms.get(newPerm);
                if (splitPerms == null) {
                    splitPerms = new ArraySet<>();
                    newToSplitPerms.put(newPerm, splitPerms);
                }
                splitPerms.add(spi.getSplitPermission());
            }
        }
        int numNewImplicitPerms = newImplicitPermissions.size();
        int[] updatedUserIds2 = updatedUserIds;
        for (int newImplicitPermNum = 0; newImplicitPermNum < numNewImplicitPerms; newImplicitPermNum++) {
            String newPerm2 = newImplicitPermissions.valueAt(newImplicitPermNum);
            ArraySet<String> sourcePerms = newToSplitPerms.get(newPerm2);
            if (sourcePerms != null && !ps.hasInstallPermission(newPerm2)) {
                BasePermission bp = this.mSettings.getPermissionLocked(newPerm2);
                int[] users = UserManagerService.getInstance().getUserIds();
                int numUsers = users.length;
                int userNum = 0;
                while (true) {
                    if (userNum >= numUsers) {
                        break;
                    }
                    int userId = users[userNum];
                    if (!newPerm2.equals("android.permission.ACTIVITY_RECOGNITION")) {
                        ps.updatePermissionFlags(bp, userId, 128, 128);
                    }
                    int[] updatedUserIds3 = ArrayUtils.appendInt(updatedUserIds2, userId);
                    boolean inheritsFromInstallPerm2 = false;
                    int sourcePermNum = 0;
                    while (true) {
                        inheritsFromInstallPerm = inheritsFromInstallPerm2;
                        if (sourcePermNum >= sourcePerms.size()) {
                            break;
                        } else if (ps.hasInstallPermission(sourcePerms.valueAt(sourcePermNum))) {
                            inheritsFromInstallPerm = true;
                            break;
                        } else {
                            sourcePermNum++;
                            inheritsFromInstallPerm2 = inheritsFromInstallPerm;
                        }
                    }
                    if (origPs.hasRequestedPermission(sourcePerms) || inheritsFromInstallPerm) {
                        inheritPermissionStateToNewImplicitPermissionLocked(sourcePerms, newPerm2, ps, pkg, userId);
                        userNum++;
                        updatedUserIds2 = updatedUserIds3;
                        numUsers = numUsers;
                        users = users;
                        bp = bp;
                    } else {
                        if (PackageManagerService.DEBUG_PERMISSIONS) {
                            Slog.i(TAG, newPerm2 + " does not inherit from " + sourcePerms + " for " + pkgName + " as split permission is also new");
                        }
                        updatedUserIds2 = updatedUserIds3;
                    }
                }
            }
        }
        return updatedUserIds2;
    }

    private boolean isNewPlatformPermissionForPackage(String perm, PackageParser.Package pkg) {
        int NP = PackageParser.NEW_PERMISSIONS.length;
        int ip = 0;
        while (ip < NP) {
            PackageParser.NewPermissionInfo npi = PackageParser.NEW_PERMISSIONS[ip];
            if (!npi.name.equals(perm) || pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                ip++;
            } else {
                Log.i(TAG, "Auto-granting " + perm + " to old pkg " + pkg.packageName);
                return true;
            }
        }
        return false;
    }

    private boolean hasPrivappWhitelistEntry(String perm, PackageParser.Package pkg) {
        ArraySet<String> wlPermissions;
        if (pkg.isVendor()) {
            wlPermissions = SystemConfig.getInstance().getVendorPrivAppPermissions(pkg.packageName);
        } else if (pkg.isProduct()) {
            wlPermissions = SystemConfig.getInstance().getProductPrivAppPermissions(pkg.packageName);
        } else if (pkg.isProductServices()) {
            wlPermissions = SystemConfig.getInstance().getProductServicesPrivAppPermissions(pkg.packageName);
        } else {
            wlPermissions = SystemConfig.getInstance().getPrivAppPermissions(pkg.packageName);
        }
        if (!(wlPermissions != null && wlPermissions.contains(perm))) {
            return pkg.parentPackage != null && hasPrivappWhitelistEntry(perm, pkg.parentPackage);
        }
        return true;
    }

    private boolean grantSignaturePermission(String perm, PackageParser.Package pkg, BasePermission bp, PermissionsState origPermissions) {
        Iterator it;
        PackageSetting disabledChildPs;
        ArraySet<String> deniedPermissions;
        boolean oemPermission = bp.isOEM();
        boolean vendorPrivilegedPermission = bp.isVendorPrivileged();
        boolean privilegedPermission = bp.isPrivileged() || bp.isVendorPrivileged();
        boolean privappPermissionsDisable = RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_DISABLE;
        boolean platformPermission = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName());
        boolean platformPackage = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg.packageName);
        if (!privappPermissionsDisable && privilegedPermission && pkg.isPrivileged() && !platformPackage && platformPermission && !hasPrivappWhitelistEntry(perm, pkg)) {
            if (!this.mSystemReady && !pkg.isUpdatedSystemApp()) {
                if (pkg.isVendor()) {
                    deniedPermissions = SystemConfig.getInstance().getVendorPrivAppDenyPermissions(pkg.packageName);
                } else if (pkg.isProduct()) {
                    deniedPermissions = SystemConfig.getInstance().getProductPrivAppDenyPermissions(pkg.packageName);
                } else if (pkg.isProductServices()) {
                    deniedPermissions = SystemConfig.getInstance().getProductServicesPrivAppDenyPermissions(pkg.packageName);
                } else {
                    deniedPermissions = SystemConfig.getInstance().getPrivAppDenyPermissions(pkg.packageName);
                }
                if (!(deniedPermissions == null || !deniedPermissions.contains(perm))) {
                    return false;
                }
                Slog.w(TAG, "Privileged permission " + perm + " for package " + pkg.packageName + " - not in privapp-permissions whitelist");
                if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                    if (this.mPrivappPermissionsViolations == null) {
                        this.mPrivappPermissionsViolations = new ArraySet<>();
                    }
                    this.mPrivappPermissionsViolations.add(pkg.packageName + ": " + perm);
                }
            }
            if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                return false;
            }
        }
        PackageParser.Package systemPackage = this.mPackageManagerInt.getPackage(this.mPackageManagerInt.getKnownPackageName(0, 0));
        boolean allowed = pkg.mSigningDetails.hasAncestorOrSelf(bp.getSourcePackageSetting().getSigningDetails()) || bp.getSourcePackageSetting().getSigningDetails().checkCapability(pkg.mSigningDetails, 4) || pkg.mSigningDetails.hasAncestorOrSelf(systemPackage.mSigningDetails) || systemPackage.mSigningDetails.checkCapability(pkg.mSigningDetails, 4);
        OppoPackageManagerInternal oppoPackageManagerInternal = this.mOppoPackageManagerInt;
        if (oppoPackageManagerInternal != null && oppoPackageManagerInternal.grantPermissionOppoPolicy(pkg, perm, allowed)) {
            allowed = true;
        }
        if (!allowed) {
            if (privilegedPermission || oemPermission) {
                if (pkg.isSystem()) {
                    if (pkg.isUpdatedSystemApp()) {
                        PackageParser.Package disabledPkg = this.mPackageManagerInt.getDisabledSystemPackage(pkg.packageName);
                        PackageSetting disabledPs = disabledPkg != null ? (PackageSetting) disabledPkg.mExtras : null;
                        if (disabledPs == null || !disabledPs.getPermissionsState().hasInstallPermission(perm)) {
                            if (disabledPs != null && disabledPkg != null && isPackageRequestingPermission(disabledPkg, perm) && ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, perm)))) {
                                allowed = true;
                            }
                            if (pkg.parentPackage != null) {
                                PackageParser.Package disabledParentPkg = this.mPackageManagerInt.getDisabledSystemPackage(((PackageParser.Package) pkg.parentPackage).packageName);
                                PackageSetting disabledParentPs = disabledParentPkg != null ? (PackageSetting) disabledParentPkg.mExtras : null;
                                if (disabledParentPkg != null) {
                                    if (!privilegedPermission || !disabledParentPs.isPrivileged()) {
                                        if (oemPermission) {
                                            if (!disabledParentPs.isOem()) {
                                            }
                                        }
                                    }
                                    if (isPackageRequestingPermission(disabledParentPkg, perm) && canGrantOemPermission(disabledParentPs, perm)) {
                                        allowed = true;
                                    } else if (disabledParentPkg.childPackages != null) {
                                        Iterator it2 = disabledParentPkg.childPackages.iterator();
                                        while (true) {
                                            if (!it2.hasNext()) {
                                                break;
                                            }
                                            PackageParser.Package disabledChildPkg = (PackageParser.Package) it2.next();
                                            if (disabledChildPkg != null) {
                                                it = it2;
                                                disabledChildPs = (PackageSetting) disabledChildPkg.mExtras;
                                            } else {
                                                it = it2;
                                                disabledChildPs = null;
                                            }
                                            if (isPackageRequestingPermission(disabledChildPkg, perm) && canGrantOemPermission(disabledChildPs, perm)) {
                                                allowed = true;
                                                break;
                                            }
                                            it2 = it;
                                            disabledParentPkg = disabledParentPkg;
                                        }
                                    }
                                }
                            }
                        } else if ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, perm))) {
                            allowed = true;
                        }
                    } else {
                        allowed = (privilegedPermission && pkg.isPrivileged()) || (oemPermission && pkg.isOem() && canGrantOemPermission((PackageSetting) pkg.mExtras, perm));
                    }
                    if (allowed && privilegedPermission && !vendorPrivilegedPermission && pkg.isVendor()) {
                        Slog.w(TAG, "Permission " + perm + " cannot be granted to privileged vendor apk " + pkg.packageName + " because it isn't a 'vendorPrivileged' permission.");
                        allowed = false;
                    }
                }
            }
        }
        if (allowed) {
            return allowed;
        }
        if (!allowed && bp.isPre23() && pkg.applicationInfo.targetSdkVersion < 23) {
            allowed = true;
        }
        if (!allowed && bp.isInstaller() && (pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(2, 0)) || pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(6, 0)))) {
            allowed = true;
        }
        if (!allowed && bp.isVerifier() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(3, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isPreInstalled() && pkg.isSystem()) {
            allowed = true;
        }
        if (!allowed && bp.isDevelopment()) {
            allowed = origPermissions.hasInstallPermission(perm);
        }
        if (!allowed && bp.isSetup() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(1, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isSystemTextClassifier() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(5, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isConfigurator() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(9, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isWellbeing() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(7, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isDocumenter() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(8, 0))) {
            allowed = true;
        }
        if (!allowed && bp.isIncidentReportApprover() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(10, 0))) {
            allowed = true;
        }
        if (allowed || !bp.isAppPredictor() || !pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(11, 0))) {
            return allowed;
        }
        return true;
    }

    private static boolean canGrantOemPermission(PackageSetting ps, String permission) {
        if (!ps.isOem()) {
            return false;
        }
        Boolean granted = (Boolean) SystemConfig.getInstance().getOemPermissions(ps.name).get(permission);
        if (granted == null) {
            throw new IllegalStateException("OEM permission" + permission + " requested by package " + ps.name + " must be explicitly declared granted or not");
        } else if (Boolean.TRUE == granted) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
        if (!EXP_VERSION || pkg == null || pkg.mExtras == null) {
            return false;
        }
        if (pkg.applicationInfo.targetSdkVersion >= 23 && !CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported()) {
            return false;
        }
        boolean reviewRequired = ((PackageSetting) pkg.mExtras).getPermissionsState().isPermissionReviewRequired(userId);
        if (CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported()) {
            return CtaManagerFactory.getInstance().makeCtaManager().isPermissionReviewRequired(pkg, userId, reviewRequired);
        }
        return reviewRequired;
    }

    private boolean isPackageRequestingPermission(PackageParser.Package pkg, String permission) {
        int permCount = pkg.requestedPermissions.size();
        for (int j = 0; j < permCount; j++) {
            if (permission.equals((String) pkg.requestedPermissions.get(j))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void grantRuntimePermissionsGrantedToDisabledPackageLocked(PackageParser.Package pkg, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        PackageParser.Package disabledPkg;
        int i;
        int i2;
        if (pkg.parentPackage != null && pkg.requestedPermissions != null && (disabledPkg = this.mPackageManagerInt.getDisabledSystemPackage(pkg.parentPackage.packageName)) != null && disabledPkg.mExtras != null) {
            PackageSetting disabledPs = (PackageSetting) disabledPkg.mExtras;
            if (disabledPs.isPrivileged() && !disabledPs.hasChildPackages()) {
                int permCount = pkg.requestedPermissions.size();
                for (int i3 = 0; i3 < permCount; i3++) {
                    String permission = (String) pkg.requestedPermissions.get(i3);
                    BasePermission bp = this.mSettings.getPermissionLocked(permission);
                    if (bp != null && (bp.isRuntime() || bp.isDevelopment())) {
                        int[] userIds = this.mUserManagerInt.getUserIds();
                        int length = userIds.length;
                        int i4 = 0;
                        while (i4 < length) {
                            int userId = userIds[i4];
                            if (disabledPs.getPermissionsState().hasRuntimePermission(permission, userId)) {
                                i2 = i4;
                                i = length;
                                grantRuntimePermission(permission, pkg.packageName, false, callingUid, userId, callback);
                            } else {
                                i2 = i4;
                                i = length;
                            }
                            i4 = i2 + 1;
                            length = i;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        for (int userId : userIds) {
            grantRequestedRuntimePermissionsForUser(pkg, userId, grantedPermissions, callingUid, callback);
        }
    }

    /* access modifiers changed from: private */
    public List<String> getWhitelistedRestrictedPermissions(PackageParser.Package pkg, int whitelistFlags, int userId) {
        PackageSetting packageSetting = (PackageSetting) pkg.mExtras;
        if (packageSetting == null) {
            return null;
        }
        PermissionsState permissionsState = packageSetting.getPermissionsState();
        int queryFlags = 0;
        if ((whitelistFlags & 1) != 0) {
            queryFlags = 0 | 4096;
        }
        if ((whitelistFlags & 4) != 0) {
            queryFlags |= 8192;
        }
        if ((whitelistFlags & 2) != 0) {
            queryFlags |= 2048;
        }
        ArrayList<String> whitelistedPermissions = null;
        int permissionCount = pkg.requestedPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            String permissionName = (String) pkg.requestedPermissions.get(i);
            if ((permissionsState.getPermissionFlags(permissionName, userId) & queryFlags) != 0) {
                if (whitelistedPermissions == null) {
                    whitelistedPermissions = new ArrayList<>();
                }
                whitelistedPermissions.add(permissionName);
            }
        }
        return whitelistedPermissions;
    }

    /* access modifiers changed from: private */
    public void setWhitelistedRestrictedPermissions(PackageParser.Package pkg, int[] userIds, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
        for (int userId : userIds) {
            setWhitelistedRestrictedPermissionsForUser(pkg, userId, permissions, callingUid, whitelistFlags, callback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ae, code lost:
        r0 = th;
     */
    private void grantRequestedRuntimePermissionsForUser(PackageParser.Package pkg, int userId, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        BasePermission bp;
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps != null) {
            PermissionsState permissionsState = ps.getPermissionsState();
            boolean supportsRuntimePermissions = pkg.applicationInfo.targetSdkVersion >= 23;
            boolean instantApp = this.mPackageManagerInt.isInstantApp(pkg.packageName, userId);
            Iterator it = pkg.requestedPermissions.iterator();
            while (it.hasNext()) {
                String permission = (String) it.next();
                synchronized (this.mLock) {
                    bp = this.mSettings.getPermissionLocked(permission);
                }
                if (bp != null) {
                    if ((bp.isRuntime() || bp.isDevelopment()) && ((!instantApp || bp.isInstant()) && ((supportsRuntimePermissions || !bp.isRuntimeOnly()) && (grantedPermissions == null || ArrayUtils.contains(grantedPermissions, permission))))) {
                        int flags = permissionsState.getPermissionFlags(permission, userId);
                        if (supportsRuntimePermissions) {
                            if ((flags & 20) == 0) {
                                grantRuntimePermission(permission, pkg.packageName, false, callingUid, userId, callback);
                            }
                        } else if ((flags & 64) != 0) {
                            updatePermissionFlags(permission, pkg.packageName, 64, 0, callingUid, userId, false, callback);
                        }
                    }
                }
            }
            return;
        }
        return;
        while (true) {
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.am.IColorMultiAppManager.onExternalStoragePolicyChanged(java.lang.String, com.android.server.pm.PackageSetting, int):void
     arg types: [java.lang.String, android.content.pm.PackageParser$Package, int]
     candidates:
      com.android.server.am.IColorMultiAppManager.onExternalStoragePolicyChanged(int, java.lang.String, int):void
      com.android.server.am.IColorMultiAppManager.onExternalStoragePolicyChanged(java.lang.String, com.android.server.pm.PackageSetting, int):void */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x027a, code lost:
        r0 = th;
     */
    public void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
        BasePermission bp;
        PackageParser.Package pkg;
        if (!this.mUserManagerInt.exists(userId)) {
            Log.e(TAG, "No such user:" + userId);
            return;
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "grantRuntimePermission");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantRuntimePermission");
        }
        enforceCrossUserPermission(callingUid, userId, true, true, false, "grantRuntimePermission");
        PackageParser.Package pkg2 = this.mPackageManagerInt.getPackage(packageName);
        if (pkg2 == null || pkg2.mExtras == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        }
        synchronized (this.mLock) {
            bp = this.mSettings.getPermissionLocked(permName);
        }
        if (bp == null) {
            throw new IllegalArgumentException("Unknown permission: " + permName);
        } else if (!this.mPackageManagerInt.filterAppAccess(pkg2, callingUid, userId)) {
            bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg2);
            if (pkg2.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                int uid = UserHandle.getUid(userId, pkg2.applicationInfo.uid);
                PackageParser.Package ps = (PackageSetting) pkg2.mExtras;
                PermissionsState permissionsState = ps.getPermissionsState();
                int flags = permissionsState.getPermissionFlags(permName, userId);
                if ((flags & 16) != 0) {
                    Log.e(TAG, "Cannot grant system fixed permission " + permName + " for package " + packageName);
                    return;
                } else if (!overridePolicy && (flags & 4) != 0) {
                    Log.e(TAG, "Cannot grant policy fixed permission " + permName + " for package " + packageName);
                    return;
                } else if (bp.isHardRestricted() && (flags & 14336) == 0) {
                    Log.e(TAG, "Cannot grant hard restricted non-exempt permission " + permName + " for package " + packageName);
                    return;
                } else if (bp.isSoftRestricted() && !SoftRestrictedPermissionPolicy.forPermission(this.mContext, pkg2.applicationInfo, UserHandle.of(userId), permName).canBeGranted()) {
                    Log.e(TAG, "Cannot grant soft restricted permission " + permName + " for package " + packageName);
                    return;
                } else if (bp.isDevelopment()) {
                    if (permissionsState.grantInstallPermission(bp) != -1 && callback != null) {
                        callback.onInstallPermissionGranted();
                        return;
                    }
                    return;
                } else if (ps.getInstantApp(userId) && !bp.isInstant()) {
                    throw new SecurityException("Cannot grant non-ephemeral permission" + permName + " for package " + packageName);
                } else if (pkg2.applicationInfo.targetSdkVersion < 23) {
                    Slog.w(TAG, "Cannot grant runtime permission to a legacy app");
                    return;
                } else {
                    int result = permissionsState.grantRuntimePermission(bp, userId);
                    if (result != -1) {
                        if (result == 1 && callback != null) {
                            callback.onGidsChanged(UserHandle.getAppId(pkg2.applicationInfo.uid), userId);
                        }
                        Slog.d(TAG, permName + " is granted, packageName: " + packageName + ", callingUid: " + callingUid + ", userId: " + callingUid);
                        if (bp.isRuntime()) {
                            logPermission(1243, permName, packageName);
                        }
                        if (callback != null) {
                            callback.onPermissionGranted(uid, userId);
                        }
                        OppoPackageManagerInternal oppoPackageManagerInternal = this.mOppoPackageManagerInt;
                        if (oppoPackageManagerInternal != null) {
                            pkg = ps;
                            oppoPackageManagerInternal.grantOppoPermissionByGroupAsUser(pkg2, permName, packageName, Binder.getCallingUid(), userId);
                        } else {
                            pkg = ps;
                        }
                        if (bp.isRuntime()) {
                            notifyRuntimePermissionStateChanged(packageName, userId);
                        }
                        if ("android.permission.READ_EXTERNAL_STORAGE".equals(permName) || "android.permission.WRITE_EXTERNAL_STORAGE".equals(permName)) {
                            long token = Binder.clearCallingIdentity();
                            try {
                                if (this.mUserManagerInt.isUserInitialized(userId)) {
                                    ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).onExternalStoragePolicyChanged(uid, packageName);
                                }
                                OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).onExternalStoragePolicyChanged(packageName, (PackageSetting) pkg, uid);
                                return;
                            } finally {
                                Binder.restoreCallingIdentity(token);
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
        } else {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        }
        while (true) {
        }
    }

    public boolean isPackageNeedsReview(PackageParser.Package pkg, SharedUserSetting suid) {
        if (!CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported()) {
            return false;
        }
        boolean appSupportsRuntimePermissions = pkg.applicationInfo.targetSdkVersion >= 23;
        if (pkg.mSharedUserId != null) {
            if (suid != null) {
                for (PackageParser.Package pkg2 : suid.getPackages()) {
                    if (appSupportsRuntimePermissions) {
                        if (isSystemApp(pkg2)) {
                            return false;
                        }
                    } else if (pkg2.applicationInfo.targetSdkVersion >= 23) {
                        return false;
                    }
                }
            }
            return true;
        } else if (!appSupportsRuntimePermissions || !isSystemApp(pkg)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSystemApp(PackageParser.Package pkg) {
        return (pkg.applicationInfo.flags & 1) != 0;
    }

    /* access modifiers changed from: private */
    public void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
        if (!this.mUserManagerInt.exists(userId)) {
            Log.e(TAG, "No such user:" + userId);
            return;
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "revokeRuntimePermission");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeRuntimePermission");
        }
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, false, "revokeRuntimePermission");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg == null || pkg.mExtras == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!this.mPackageManagerInt.filterAppAccess(pkg, Binder.getCallingUid(), userId)) {
            BasePermission bp = this.mSettings.getPermissionLocked(permName);
            if (bp != null) {
                bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg);
                if (pkg.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                    PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                    int flags = permissionsState.getPermissionFlags(permName, userId);
                    if ((flags & 16) != 0 && UserHandle.getCallingAppId() != 1000) {
                        throw new SecurityException("Non-System UID cannot revoke system fixed permission " + permName + " for package " + packageName);
                    } else if (!overridePolicy && (flags & 4) != 0) {
                        throw new SecurityException("Cannot revoke policy fixed permission " + permName + " for package " + packageName);
                    } else if (bp.isDevelopment()) {
                        if (permissionsState.revokeInstallPermission(bp) != -1 && callback != null) {
                            callback.onInstallPermissionRevoked();
                        }
                    } else if (permissionsState.hasRuntimePermission(permName, userId) && permissionsState.revokeRuntimePermission(bp, userId) != -1) {
                        if (bp.isRuntime()) {
                            logPermission(1245, permName, packageName);
                        }
                        if (callback != null && !this.mOppoPackageManagerInt.onPermissionRevoked(pkg.applicationInfo, userId)) {
                            callback.onPermissionRevoked(pkg.applicationInfo.uid, userId);
                        }
                        OppoPackageManagerInternal oppoPackageManagerInternal = this.mOppoPackageManagerInt;
                        if (oppoPackageManagerInternal != null) {
                            oppoPackageManagerInternal.revokeOppoPermissionByGroupAsUser(pkg, permName, packageName, Binder.getCallingUid(), userId);
                        }
                        if (bp.isRuntime()) {
                            notifyRuntimePermissionStateChanged(packageName, userId);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown permission: " + permName);
            }
        } else {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        }
    }

    private void setWhitelistedRestrictedPermissionsForUser(PackageParser.Package pkg, int userId, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i;
        int permissionCount;
        ArraySet<String> oldGrantedRestrictedPermissions;
        int newFlags;
        int mask;
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps != null) {
            PermissionsState permissionsState = ps.getPermissionsState();
            ArraySet<String> oldGrantedRestrictedPermissions2 = null;
            boolean updatePermissions = false;
            int permissionCount2 = pkg.requestedPermissions.size();
            int i2 = 0;
            while (i2 < permissionCount2) {
                String permissionName = (String) pkg.requestedPermissions.get(i2);
                BasePermission bp = this.mSettings.getPermissionLocked(permissionName);
                if (bp == null) {
                    Slog.w(TAG, "Cannot whitelist unknown permission: " + permissionName);
                } else if (bp.isHardOrSoftRestricted()) {
                    if (permissionsState.hasPermission(permissionName, userId)) {
                        if (oldGrantedRestrictedPermissions2 == null) {
                            oldGrantedRestrictedPermissions2 = new ArraySet<>();
                        }
                        oldGrantedRestrictedPermissions2.add(permissionName);
                        oldGrantedRestrictedPermissions = oldGrantedRestrictedPermissions2;
                    } else {
                        oldGrantedRestrictedPermissions = oldGrantedRestrictedPermissions2;
                    }
                    int oldFlags = permissionsState.getPermissionFlags(permissionName, userId);
                    int newFlags2 = oldFlags;
                    int mask2 = 0;
                    int whitelistFlagsCopy = whitelistFlags;
                    while (whitelistFlagsCopy != 0) {
                        int flag = 1 << Integer.numberOfTrailingZeros(whitelistFlagsCopy);
                        whitelistFlagsCopy &= ~flag;
                        if (flag == 1) {
                            mask2 |= 4096;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -4097;
                            } else {
                                newFlags2 |= 4096;
                            }
                        } else if (flag == 2) {
                            mask2 |= 2048;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -2049;
                            } else {
                                newFlags2 |= 2048;
                            }
                        } else if (flag == 4) {
                            mask2 |= 8192;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -8193;
                            } else {
                                newFlags2 |= 8192;
                            }
                        }
                    }
                    if (oldFlags == newFlags2) {
                        i = i2;
                        permissionCount = permissionCount2;
                        oldGrantedRestrictedPermissions2 = oldGrantedRestrictedPermissions;
                    } else {
                        boolean wasWhitelisted = oldFlags & true;
                        boolean isWhitelisted = (newFlags2 & 14336) != 0;
                        if ((oldFlags & 4) != 0) {
                            boolean isGranted = permissionsState.hasPermission(permissionName, userId);
                            if (!isWhitelisted && isGranted) {
                                mask2 |= 4;
                                newFlags2 &= -5;
                            }
                        }
                        if (pkg.applicationInfo.targetSdkVersion >= 23 || wasWhitelisted || !isWhitelisted) {
                            newFlags = newFlags2;
                            mask = mask2;
                        } else {
                            newFlags = newFlags2 | 64;
                            mask = mask2 | 64;
                        }
                        i = i2;
                        permissionCount = permissionCount2;
                        updatePermissionFlags(permissionName, pkg.packageName, mask, newFlags, callingUid, userId, false, null);
                        oldGrantedRestrictedPermissions2 = oldGrantedRestrictedPermissions;
                        updatePermissions = true;
                    }
                    i2 = i + 1;
                    permissionCount2 = permissionCount;
                }
                i = i2;
                permissionCount = permissionCount2;
                i2 = i + 1;
                permissionCount2 = permissionCount;
            }
            if (updatePermissions) {
                restorePermissionState(pkg, false, pkg.packageName, callback);
                if (oldGrantedRestrictedPermissions2 != null) {
                    int oldGrantedCount = oldGrantedRestrictedPermissions2.size();
                    for (int i3 = 0; i3 < oldGrantedCount; i3++) {
                        if (!ps.getPermissionsState().hasPermission(oldGrantedRestrictedPermissions2.valueAt(i3), userId)) {
                            callback.onPermissionRevoked(pkg.applicationInfo.uid, userId);
                            return;
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r7v2 int[]: [D('runtimePermissionChangedUserIds' int[]), D('i' int)] */
    @GuardedBy({"mLock"})
    private int[] revokeUnusedSharedUserPermissionsLocked(SharedUserSetting suSetting, int[] allUserIds) {
        char c;
        boolean z;
        char c2;
        boolean z2;
        BasePermission bp;
        PermissionManagerService permissionManagerService = this;
        ArraySet<String> usedPermissions = new ArraySet<>();
        List<PackageParser.Package> pkgList = suSetting.getPackages();
        if (pkgList == null || pkgList.size() == 0) {
            return EmptyArray.INT;
        }
        for (PackageParser.Package pkg : pkgList) {
            if (pkg.requestedPermissions != null) {
                int requestedPermCount = pkg.requestedPermissions.size();
                for (int j = 0; j < requestedPermCount; j++) {
                    String permission = (String) pkg.requestedPermissions.get(j);
                    if (permissionManagerService.mSettings.getPermissionLocked(permission) != null) {
                        usedPermissions.add(permission);
                    }
                }
            }
        }
        PermissionsState permissionsState = suSetting.getPermissionsState();
        List<PermissionsState.PermissionState> installPermStates = permissionsState.getInstallPermissionStates();
        int i = installPermStates.size() - 1;
        while (true) {
            c = 64511;
            z = false;
            if (i < 0) {
                break;
            }
            PermissionsState.PermissionState permissionState = installPermStates.get(i);
            if (!usedPermissions.contains(permissionState.getName()) && (bp = permissionManagerService.mSettings.getPermissionLocked(permissionState.getName())) != null) {
                permissionsState.revokeInstallPermission(bp);
                permissionsState.updatePermissionFlags(bp, -1, 64511, 0);
            }
            i--;
        }
        int[] runtimePermissionChangedUserIds = EmptyArray.INT;
        int length = allUserIds.length;
        int[] runtimePermissionChangedUserIds2 = runtimePermissionChangedUserIds;
        int i2 = 0;
        while (i2 < length) {
            int userId = allUserIds[i2];
            List<PermissionsState.PermissionState> runtimePermStates = permissionsState.getRuntimePermissionStates(userId);
            int i3 = runtimePermStates.size() - 1;
            while (i3 >= 0) {
                PermissionsState.PermissionState permissionState2 = runtimePermStates.get(i3);
                if (!usedPermissions.contains(permissionState2.getName())) {
                    BasePermission bp2 = permissionManagerService.mSettings.getPermissionLocked(permissionState2.getName());
                    if (bp2 != null) {
                        permissionsState.revokeRuntimePermission(bp2, userId);
                        z2 = false;
                        c2 = 64511;
                        permissionsState.updatePermissionFlags(bp2, userId, 64511, 0);
                        runtimePermissionChangedUserIds2 = ArrayUtils.appendInt(runtimePermissionChangedUserIds2, userId);
                    } else {
                        z2 = false;
                        c2 = 64511;
                    }
                } else {
                    z2 = z;
                    c2 = 64511;
                }
                i3--;
                c = c2;
                z = z2;
                permissionManagerService = this;
            }
            i2++;
            z = z;
            permissionManagerService = this;
        }
        return runtimePermissionChangedUserIds2;
    }

    /* access modifiers changed from: private */
    public String[] getAppOpPermissionPackages(String permName) {
        if (this.mPackageManagerInt.getInstantAppPackageName(Binder.getCallingUid()) != null) {
            return null;
        }
        synchronized (this.mLock) {
            ArraySet<String> pkgs = this.mSettings.mAppOpPermissionPackages.get(permName);
            if (pkgs == null) {
                return null;
            }
            if (this.mOppoPackageManagerInt != null) {
                Iterator<String> it = this.mOppoPackageManagerInt.getIgnoreAppList().iterator();
                while (it.hasNext()) {
                    pkgs.remove(it.next());
                }
            }
            String[] strArr = (String[]) pkgs.toArray(new String[pkgs.size()]);
            return strArr;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        if (r9.mPackageManagerInt.filterAppAccess(r0, r12, r13) == false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004a, code lost:
        return ((com.android.server.pm.PackageSetting) r0.mExtras).getPermissionsState().getPermissionFlags(r10, r13);
     */
    public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
        if (!this.mUserManagerInt.exists(userId)) {
            return 0;
        }
        enforceGrantRevokeGetRuntimePermissionPermissions("getPermissionFlags");
        enforceCrossUserPermission(callingUid, userId, true, false, false, "getPermissionFlags");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg == null || pkg.mExtras == null) {
            return 0;
        }
        synchronized (this.mLock) {
            if (this.mSettings.getPermissionLocked(permName) == null) {
                return 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i = 0;
        int i2 = pkg != null ? 1 : 0;
        if (replaceGrant) {
            i = 2;
        }
        int flags = i | i2;
        updatePermissions(packageName, pkg, getVolumeUuidForPackage(pkg), flags, allPackages, callback);
        if (pkg != null && pkg.childPackages != null) {
            Iterator it = pkg.childPackages.iterator();
            while (it.hasNext()) {
                PackageParser.Package childPkg = (PackageParser.Package) it.next();
                updatePermissions(childPkg.packageName, childPkg, getVolumeUuidForPackage(childPkg), flags, allPackages, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i;
        if (sdkUpdated) {
            i = 6;
        } else {
            i = 0;
        }
        updatePermissions(null, null, volumeUuid, i | 1, allPackages, callback);
    }

    private void updatePermissions(String changingPkgName, PackageParser.Package changingPkg, String replaceVolumeUuid, int flags, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int flags2 = updatePermissions(changingPkgName, changingPkg, updatePermissionTrees(changingPkgName, changingPkg, flags), callback);
        synchronized (this.mLock) {
            if (this.mBackgroundPermissions == null) {
                this.mBackgroundPermissions = new ArrayMap<>();
                for (BasePermission bp : this.mSettings.getAllPermissionsLocked()) {
                    if (!(bp.perm == null || bp.perm.info == null || bp.perm.info.backgroundPermission == null)) {
                        String fgPerm = bp.name;
                        String bgPerm = bp.perm.info.backgroundPermission;
                        List<String> fgPerms = this.mBackgroundPermissions.get(bgPerm);
                        if (fgPerms == null) {
                            fgPerms = new ArrayList();
                            this.mBackgroundPermissions.put(bgPerm, fgPerms);
                        }
                        fgPerms.add(fgPerm);
                    }
                }
            }
        }
        Trace.traceBegin(262144, "restorePermissionState");
        boolean replace = false;
        if ((flags2 & 1) != 0) {
            for (PackageParser.Package pkg : allPackages) {
                if (pkg != changingPkg) {
                    restorePermissionState(pkg, (flags2 & 4) != 0 && Objects.equals(replaceVolumeUuid, getVolumeUuidForPackage(pkg)), changingPkgName, callback);
                }
            }
        }
        if (changingPkg != null) {
            String volumeUuid = getVolumeUuidForPackage(changingPkg);
            if ((flags2 & 2) != 0 && Objects.equals(replaceVolumeUuid, volumeUuid)) {
                replace = true;
            }
            restorePermissionState(changingPkg, replace, changingPkgName, callback);
        }
        Trace.traceEnd(262144);
    }

    private int updatePermissions(String packageName, PackageParser.Package pkg, int flags, PermissionManagerServiceInternal.PermissionCallback callback) {
        Set<BasePermission> needsUpdate = null;
        synchronized (this.mLock) {
            Iterator<BasePermission> it = this.mSettings.mPermissions.values().iterator();
            while (it.hasNext()) {
                BasePermission bp = it.next();
                if (bp.isDynamic()) {
                    bp.updateDynamicPermission(this.mSettings.mPermissionTrees.values());
                }
                if (bp.getSourcePackageSetting() == null) {
                    if (needsUpdate == null) {
                        needsUpdate = new ArraySet<>(this.mSettings.mPermissions.size());
                    }
                    needsUpdate.add(bp);
                } else if (packageName != null && packageName.equals(bp.getSourcePackageName())) {
                    if (pkg == null || !hasPermission(pkg, bp.getName())) {
                        Slog.i(TAG, "Removing old permission tree: " + bp.getName() + " from package " + bp.getSourcePackageName());
                        if (bp.isRuntime()) {
                            for (int userId : this.mUserManagerInt.getUserIds()) {
                                this.mPackageManagerInt.forEachPackage(new Consumer(bp, userId, callback) {
                                    /* class com.android.server.pm.permission.$$Lambda$PermissionManagerService$w2aPgVKY5ZkiKKZQUVsj6t4Bn4c */
                                    private final /* synthetic */ BasePermission f$1;
                                    private final /* synthetic */ int f$2;
                                    private final /* synthetic */ PermissionManagerServiceInternal.PermissionCallback f$3;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                    }

                                    @Override // java.util.function.Consumer
                                    public final void accept(Object obj) {
                                        PermissionManagerService.this.lambda$updatePermissions$1$PermissionManagerService(this.f$1, this.f$2, this.f$3, (PackageParser.Package) obj);
                                    }
                                });
                            }
                        }
                        flags |= 1;
                        it.remove();
                    }
                }
            }
        }
        if (needsUpdate != null) {
            for (BasePermission bp2 : needsUpdate) {
                PackageParser.Package sourcePkg = this.mPackageManagerInt.getPackage(bp2.getSourcePackageName());
                synchronized (this.mLock) {
                    if (sourcePkg != null) {
                        if (sourcePkg.mExtras != null) {
                            PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                            if (bp2.getSourcePackageSetting() == null) {
                                bp2.setSourcePackageSetting(sourcePs);
                            }
                        }
                    }
                    Slog.w(TAG, "Removing dangling permission: " + bp2.getName() + " from package " + bp2.getSourcePackageName());
                    this.mSettings.removePermissionLocked(bp2.getName());
                }
            }
        }
        return flags;
    }

    public /* synthetic */ void lambda$updatePermissions$1$PermissionManagerService(BasePermission bp, int userId, PermissionManagerServiceInternal.PermissionCallback callback, PackageParser.Package p) {
        String pName = p.packageName;
        ApplicationInfo appInfo = this.mPackageManagerInt.getApplicationInfo(pName, 0, 1000, 0);
        if (appInfo == null || appInfo.targetSdkVersion >= 23) {
            String permissionName = bp.getName();
            if (checkPermission(permissionName, pName, 1000, userId) == 0) {
                try {
                    revokeRuntimePermission(permissionName, pName, false, userId, callback);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Failed to revoke " + permissionName + " from " + pName, e);
                }
            }
        }
    }

    private int updatePermissionTrees(String packageName, PackageParser.Package pkg, int flags) {
        Set<BasePermission> needsUpdate = null;
        synchronized (this.mLock) {
            Iterator<BasePermission> it = this.mSettings.mPermissionTrees.values().iterator();
            while (it.hasNext()) {
                BasePermission bp = it.next();
                if (bp.getSourcePackageSetting() == null) {
                    if (needsUpdate == null) {
                        needsUpdate = new ArraySet<>(this.mSettings.mPermissionTrees.size());
                    }
                    needsUpdate.add(bp);
                } else if (packageName != null && packageName.equals(bp.getSourcePackageName())) {
                    if (pkg == null || !hasPermission(pkg, bp.getName())) {
                        Slog.i(TAG, "Removing old permission tree: " + bp.getName() + " from package " + bp.getSourcePackageName());
                        flags |= 1;
                        it.remove();
                    }
                }
            }
        }
        if (needsUpdate != null) {
            for (BasePermission bp2 : needsUpdate) {
                PackageParser.Package sourcePkg = this.mPackageManagerInt.getPackage(bp2.getSourcePackageName());
                synchronized (this.mLock) {
                    if (sourcePkg != null) {
                        if (sourcePkg.mExtras != null) {
                            PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                            if (bp2.getSourcePackageSetting() == null) {
                                bp2.setSourcePackageSetting(sourcePs);
                            }
                        }
                    }
                    Slog.w(TAG, "Removing dangling permission tree: " + bp2.getName() + " from package " + bp2.getSourcePackageName());
                    this.mSettings.removePermissionLocked(bp2.getName());
                }
            }
        }
        return flags;
    }

    /* access modifiers changed from: private */
    public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, boolean overridePolicy, PermissionManagerServiceInternal.PermissionCallback callback) {
        int flagValues2;
        int flagValues3;
        BasePermission bp;
        if (this.mUserManagerInt.exists(userId)) {
            enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlags");
            enforceCrossUserPermission(callingUid, userId, true, true, false, "updatePermissionFlags");
            if ((flagMask & 4) == 0 || overridePolicy) {
                if (callingUid != 1000) {
                    flagValues2 = flagValues & -17 & -33 & -65 & -4097 & -2049 & -8193 & -16385;
                    flagValues3 = flagMask & -17 & -33;
                } else {
                    flagValues3 = flagMask;
                    flagValues2 = flagValues;
                }
                PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
                if (pkg == null || pkg.mExtras == null) {
                    Log.e(TAG, "Unknown package: " + packageName);
                } else if (!this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
                    synchronized (this.mLock) {
                        bp = this.mSettings.getPermissionLocked(permName);
                    }
                    if (bp != null) {
                        PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                        boolean hadState = permissionsState.getRuntimePermissionState(permName, userId) != null;
                        boolean permissionUpdated = permissionsState.updatePermissionFlags(bp, userId, flagValues3, flagValues2);
                        if (permissionUpdated && bp.isRuntime()) {
                            notifyRuntimePermissionStateChanged(packageName, userId);
                        }
                        if (permissionUpdated && callback != null) {
                            if (CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported() && (flagValues3 & 64) != 0 && (flagValues2 & 64) == 0 && pkg.mSharedUserId != null && !permissionsState.isPermissionReviewRequired(userId)) {
                                permissionsState.updateReviewRequiredCache(userId);
                            }
                            if (permissionsState.getInstallPermissionState(permName) != null) {
                                callback.onInstallPermissionUpdated();
                            } else if (permissionsState.getRuntimePermissionState(permName, userId) != null || hadState) {
                                callback.onPermissionUpdated(new int[]{userId}, false);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown permission: " + permName);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
            } else {
                throw new SecurityException("updatePermissionFlags requires android.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerServiceInternal.PermissionCallback callback) {
        if (!this.mUserManagerInt.exists(userId)) {
            return false;
        }
        enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlagsForAllApps");
        enforceCrossUserPermission(callingUid, userId, true, true, false, "updatePermissionFlagsForAllApps");
        if (callingUid != 1000) {
            flagMask &= -17;
            flagValues &= -17;
        }
        boolean changed = false;
        for (PackageParser.Package pkg : packages) {
            PackageSetting ps = (PackageSetting) pkg.mExtras;
            if (ps != null) {
                changed |= ps.getPermissionsState().updatePermissionFlagsForAllPermissions(userId, flagMask, flagValues);
            }
        }
        return changed;
    }

    /* access modifiers changed from: private */
    public void enforceGrantRevokeRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires " + "android.permission.GRANT_RUNTIME_PERMISSIONS" + " or " + "android.permission.REVOKE_RUNTIME_PERMISSIONS");
        }
    }

    private void enforceGrantRevokeGetRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GET_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires " + "android.permission.GRANT_RUNTIME_PERMISSIONS" + " or " + "android.permission.REVOKE_RUNTIME_PERMISSIONS" + " or " + "android.permission.GET_RUNTIME_PERMISSIONS");
        }
    }

    /* access modifiers changed from: private */
    public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
        if (userId >= 0) {
            if (checkShell) {
                PackageManagerServiceUtils.enforceShellRestriction("no_debugging_features", callingUid, userId);
            }
            if ((!requirePermissionWhenSameUser && userId == UserHandle.getUserId(callingUid)) || OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).enforceCrossUserPermission(callingUid, userId) || callingUid == 1000 || callingUid == 0) {
                return;
            }
            if (requireFullPermission) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                return;
            }
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
            } catch (SecurityException e) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", message);
            }
        } else {
            throw new IllegalArgumentException("Invalid userId " + userId);
        }
    }

    @GuardedBy({"mSettings.mLock", "mLock"})
    private int calculateCurrentPermissionFootprintLocked(BasePermission tree) {
        int size = 0;
        for (BasePermission perm : this.mSettings.mPermissions.values()) {
            size += tree.calculateFootprint(perm);
        }
        return size;
    }

    @GuardedBy({"mSettings.mLock", "mLock"})
    private void enforcePermissionCapLocked(PermissionInfo info, BasePermission tree) {
        if (tree.getUid() != 1000) {
            if (info.calculateFootprint() + calculateCurrentPermissionFootprintLocked(tree) > 32768) {
                throw new SecurityException("Permission tree size cap exceeded");
            }
        }
    }

    /* access modifiers changed from: private */
    public void systemReady() {
        this.mSystemReady = true;
        if (this.mPrivappPermissionsViolations != null) {
            Slog.wtf(TAG, "Signature|privileged permissions not in privapp-permissions whitelist: " + this.mPrivappPermissionsViolations);
        }
        this.mPermissionControllerManager = (PermissionControllerManager) this.mContext.getSystemService(PermissionControllerManager.class);
        this.mPermissionPolicyInternal = (PermissionPolicyInternal) LocalServices.getService(PermissionPolicyInternal.class);
    }

    private static String getVolumeUuidForPackage(PackageParser.Package pkg) {
        if (pkg == null) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        }
        if (!pkg.isExternal()) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        }
        if (TextUtils.isEmpty(pkg.volumeUuid)) {
            return "primary_physical";
        }
        return pkg.volumeUuid;
    }

    private static boolean hasPermission(PackageParser.Package pkgInfo, String permName) {
        for (int i = pkgInfo.permissions.size() - 1; i >= 0; i--) {
            if (((PackageParser.Permission) pkgInfo.permissions.get(i)).info.name.equals(permName)) {
                return true;
            }
        }
        return false;
    }

    private void logPermission(int action, String name, String packageName) {
        LogMaker log = new LogMaker(action);
        log.setPackageName(packageName);
        log.addTaggedData(1241, name);
        this.mMetricsLogger.write(log);
    }

    public ArrayMap<String, List<String>> getBackgroundPermissions() {
        return this.mBackgroundPermissions;
    }

    private class PermissionManagerServiceInternalImpl extends PermissionManagerServiceInternal {
        private PermissionManagerServiceInternalImpl() {
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void systemReady() {
            PermissionManagerService.this.systemReady();
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
            return PermissionManagerService.this.isPermissionsReviewRequired(pkg, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerServiceInternal.PermissionCallback permissionCallback) {
            PermissionManagerService.this.revokeRuntimePermissionsIfGroupChanged(newPackage, oldPackage, allPackageNames, permissionCallback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissions(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissionGroups(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.removeAllPermissions(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean addDynamicPermission(PermissionInfo info, boolean async, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            return PermissionManagerService.this.addDynamicPermission(info, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void removeDynamicPermission(String permName, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.removeDynamicPermission(permName, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermission(permName, packageName, overridePolicy, callingUid, userId, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRequestedRuntimePermissions(pkg, userIds, grantedPermissions, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<String> getWhitelistedRestrictedPermissions(PackageParser.Package pkg, int whitelistFlags, int userId) {
            return PermissionManagerService.this.getWhitelistedRestrictedPermissions(pkg, whitelistFlags, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void setWhitelistedRestrictedPermissions(PackageParser.Package pkg, int[] userIds, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
            Slog.d(PermissionManagerService.TAG, "setWhitelistedRestrictedPermissions, pkg: " + pkg + ", callingUid: " + callingUid + ", whitelistFlags: " + whitelistFlags);
            PermissionManagerService.this.setWhitelistedRestrictedPermissions(pkg, userIds, permissions, callingUid, whitelistFlags, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRuntimePermissionsGrantedToDisabledPackage(PackageParser.Package pkg, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermissionsGrantedToDisabledPackageLocked(pkg, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.revokeRuntimePermission(permName, packageName, overridePolicy, userId, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
            Slog.d(PermissionManagerService.TAG, "updatePermissions, packageName: " + packageName + ", pkg: " + pkg + ", replaceGrant: " + replaceGrant);
            PermissionManagerService.this.updatePermissions(packageName, pkg, replaceGrant, allPackages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
            Slog.d(PermissionManagerService.TAG, "updateAllPermissions, volumeUuid: " + volumeUuid + ", sdkUpdated: " + sdkUpdated);
            PermissionManagerService.this.updateAllPermissions(volumeUuid, sdkUpdated, allPackages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public String[] getAppOpPermissionPackages(String permName) {
            return PermissionManagerService.this.getAppOpPermissionPackages(permName);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.getPermissionFlags(permName, packageName, callingUid, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, boolean overridePolicy, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.updatePermissionFlags(permName, packageName, flagMask, flagValues, callingUid, userId, overridePolicy, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerServiceInternal.PermissionCallback callback) {
            return PermissionManagerService.this.updatePermissionFlagsForAllApps(flagMask, flagValues, callingUid, userId, packages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, false, message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, requirePermissionWhenSameUser, message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceGrantRevokeRuntimePermissionPermissions(String message) {
            PermissionManagerService.this.enforceGrantRevokeRuntimePermissionPermissions(message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int checkPermission(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.checkPermission(permName, packageName, callingUid, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
            return PermissionManagerService.this.checkUidPermission(permName, pkg, uid, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionGroupInfo(groupName, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
            return PermissionManagerService.this.getAllPermissionGroups(flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfo(permName, packageName, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<PermissionInfo> getPermissionInfoByGroup(String group, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfoByGroup(group, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionSettings getPermissionSettings() {
            return PermissionManagerService.this.mSettings;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public DefaultPermissionGrantPolicy getDefaultPermissionGrantPolicy() {
            return PermissionManagerService.this.mDefaultPermissionGrantPolicy;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public BasePermission getPermissionTEMP(String permName) {
            BasePermission permissionLocked;
            synchronized (PermissionManagerService.this.mLock) {
                permissionLocked = PermissionManagerService.this.mSettings.getPermissionLocked(permName);
            }
            return permissionLocked;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public ArrayList<PermissionInfo> getAllPermissionWithProtectionLevel(int protectionLevel) {
            ArrayList<PermissionInfo> matchingPermissions = new ArrayList<>();
            synchronized (PermissionManagerService.this.mLock) {
                int numTotalPermissions = PermissionManagerService.this.mSettings.mPermissions.size();
                for (int i = 0; i < numTotalPermissions; i++) {
                    BasePermission bp = PermissionManagerService.this.mSettings.mPermissions.valueAt(i);
                    if (!(bp.perm == null || bp.perm.info == null || bp.protectionLevel != protectionLevel)) {
                        matchingPermissions.add(bp.perm.info);
                    }
                }
            }
            return matchingPermissions;
        }

        public byte[] backupRuntimePermissions(UserHandle user) {
            return PermissionManagerService.this.backupRuntimePermissions(user);
        }

        public void restoreRuntimePermissions(byte[] backup, UserHandle user) {
            PermissionManagerService.this.restoreRuntimePermissions(backup, user);
        }

        public void restoreDelayedRuntimePermissions(String packageName, UserHandle user) {
            PermissionManagerService.this.restoreDelayedRuntimePermissions(packageName, user);
        }

        public void addOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
            PermissionManagerService.this.addOnRuntimePermissionStateChangedListener(listener);
        }

        public void removeOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
            PermissionManagerService.this.removeOnRuntimePermissionStateChangedListener(listener);
        }
    }
}
