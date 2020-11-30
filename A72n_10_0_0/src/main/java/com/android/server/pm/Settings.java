package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.SuspendDialogInfo;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.PersistableBundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.EventLogTags;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.notification.ZenModeHelper;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.Installer;
import com.android.server.pm.permission.BasePermission;
import com.android.server.pm.permission.PermissionSettings;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.theia.NoFocusWindow;
import com.android.server.voiceinteraction.DatabaseHelper;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.pm.PmsExt;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.SAXParserFactory;
import libcore.io.IoUtils;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public final class Settings {
    private static final String ATTR_APP_LINK_GENERATION = "app-link-generation";
    private static final String ATTR_BLOCKED = "blocked";
    @Deprecated
    private static final String ATTR_BLOCK_UNINSTALL = "blockUninstall";
    private static final String ATTR_CE_DATA_INODE = "ceDataInode";
    private static final String ATTR_DATABASE_VERSION = "databaseVersion";
    private static final String ATTR_DISTRACTION_FLAGS = "distraction_flags";
    private static final String ATTR_DOMAIN_VERIFICATON_STATE = "domainVerificationStatus";
    private static final String ATTR_ENABLED = "enabled";
    private static final String ATTR_ENABLED_CALLER = "enabledCaller";
    private static final String ATTR_ENFORCEMENT = "enforcement";
    private static final String ATTR_FINGERPRINT = "fingerprint";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_HARMFUL_APP_WARNING = "harmful-app-warning";
    private static final String ATTR_HIDDEN = "hidden";
    private static final String ATTR_INSTALLED = "inst";
    private static final String ATTR_INSTALL_REASON = "install-reason";
    private static final String ATTR_INSTANT_APP = "instant-app";
    public static final String ATTR_NAME = "name";
    private static final String ATTR_NOT_LAUNCHED = "nl";
    private static final String ATTR_OPPO_FREEZED = "ofs";
    private static final String ATTR_OPPO_FREEZED_FLAG = "of-flag";
    public static final String ATTR_PACKAGE = "package";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_SDK_VERSION = "sdkVersion";
    private static final String ATTR_STOPPED = "stopped";
    private static final String ATTR_SUSPENDED = "suspended";
    private static final String ATTR_SUSPENDING_PACKAGE = "suspending-package";
    @Deprecated
    private static final String ATTR_SUSPEND_DIALOG_MESSAGE = "suspend_dialog_message";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_VIRTUAL_PRELOAD = "virtual-preload";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    public static final int CURRENT_DATABASE_VERSION = 3;
    private static final boolean DEBUG_KERNEL = false;
    private static final boolean DEBUG_MU = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean DEBUG_STOPPED = false;
    static final Object[] FLAG_DUMP_SPEC = {1, "SYSTEM", 2, "DEBUGGABLE", 4, "HAS_CODE", 8, "PERSISTENT", 16, "FACTORY_TEST", 32, "ALLOW_TASK_REPARENTING", 64, "ALLOW_CLEAR_USER_DATA", 128, "UPDATED_SYSTEM_APP", 256, "TEST_ONLY", 16384, "VM_SAFE_MODE", 32768, "ALLOW_BACKUP", 65536, "KILL_AFTER_RESTORE", Integer.valueOf((int) DumpState.DUMP_INTENT_FILTER_VERIFIERS), "RESTORE_ANY_VERSION", Integer.valueOf((int) DumpState.DUMP_DOMAIN_PREFERRED), "EXTERNAL_STORAGE", Integer.valueOf((int) DumpState.DUMP_DEXOPT), "LARGE_HEAP"};
    private static int PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE = 268435456;
    private static int PRE_M_APP_INFO_FLAG_HIDDEN = 134217728;
    private static int PRE_M_APP_INFO_FLAG_PRIVILEGED = 1073741824;
    private static final Object[] PRIVATE_FLAG_DUMP_SPEC = {1024, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE", 4096, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION", 2048, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_UNRESIZEABLE", 134217728, "ALLOW_AUDIO_PLAYBACK_CAPTURE", 536870912, "PRIVATE_FLAG_REQUEST_LEGACY_EXTERNAL_STORAGE", 8192, "BACKUP_IN_FOREGROUND", 2, "CANT_SAVE_STATE", 32, "DEFAULT_TO_DEVICE_PROTECTED_STORAGE", 64, "DIRECT_BOOT_AWARE", 16, "HAS_DOMAIN_URLS", 1, "HIDDEN", 128, "EPHEMERAL", 32768, "ISOLATED_SPLIT_LOADING", Integer.valueOf((int) DumpState.DUMP_INTENT_FILTER_VERIFIERS), "OEM", 256, "PARTIALLY_DIRECT_BOOT_AWARE", 8, "PRIVILEGED", 512, "REQUIRED_FOR_SYSTEM_USER", 16384, "STATIC_SHARED_LIBRARY", Integer.valueOf((int) DumpState.DUMP_DOMAIN_PREFERRED), "VENDOR", Integer.valueOf((int) DumpState.DUMP_FROZEN), "PRODUCT", Integer.valueOf((int) DumpState.DUMP_COMPILER_STATS), "PRODUCT_SERVICES", 65536, "VIRTUAL_PRELOAD", 1073741824, "ODM"};
    private static final String RUNTIME_PERMISSIONS_FILE_NAME = "runtime-permissions.xml";
    private static final String TAG = "PackageSettings";
    private static final String TAG_ALL_INTENT_FILTER_VERIFICATION = "all-intent-filter-verifications";
    private static final String TAG_BLOCK_UNINSTALL = "block-uninstall";
    private static final String TAG_BLOCK_UNINSTALL_PACKAGES = "block-uninstall-packages";
    private static final String TAG_CHILD_PACKAGE = "child-package";
    static final String TAG_CROSS_PROFILE_INTENT_FILTERS = "crossProfile-intent-filters";
    private static final String TAG_DEFAULT_APPS = "default-apps";
    private static final String TAG_DEFAULT_BROWSER = "default-browser";
    private static final String TAG_DEFAULT_DIALER = "default-dialer";
    private static final String TAG_DISABLED_COMPONENTS = "disabled-components";
    private static final String TAG_DOMAIN_VERIFICATION = "domain-verification";
    private static final String TAG_ENABLED_COMPONENTS = "enabled-components";
    public static final String TAG_ITEM = "item";
    private static final String TAG_PACKAGE = "pkg";
    private static final String TAG_PACKAGE_RESTRICTIONS = "package-restrictions";
    private static final String TAG_PERMISSIONS = "perms";
    private static final String TAG_PERSISTENT_PREFERRED_ACTIVITIES = "persistent-preferred-activities";
    private static final String TAG_READ_EXTERNAL_STORAGE = "read-external-storage";
    private static final String TAG_RUNTIME_PERMISSIONS = "runtime-permissions";
    private static final String TAG_SHARED_USER = "shared-user";
    private static final String TAG_SUSPENDED_APP_EXTRAS = "suspended-app-extras";
    private static final String TAG_SUSPENDED_DIALOG_INFO = "suspended-dialog-info";
    private static final String TAG_SUSPENDED_LAUNCHER_EXTRAS = "suspended-launcher-extras";
    private static final String TAG_USES_STATIC_LIB = "uses-static-lib";
    private static final String TAG_VERSION = "version";
    private static int mFirstAvailableUid = 0;
    private static PmsExt sPmsExt = MtkSystemServiceFactory.getInstance().makePmsExt();
    private final ArrayList<SettingBase> mAppIds = new ArrayList<>();
    private final File mBackupSettingsFilename;
    private final File mBackupStoppedPackagesFilename;
    private final File mBackupVerifiedFilename;
    private final File mBackupVerifiedFilenameToBeDeleted;
    private final SparseArray<ArraySet<String>> mBlockUninstallPackages = new SparseArray<>();
    final SparseArray<CrossProfileIntentResolver> mCrossProfileIntentResolvers = new SparseArray<>();
    final SparseArray<String> mDefaultBrowserApp = new SparseArray<>();
    private final ArrayMap<String, PackageSetting> mDisabledSysPackages = new ArrayMap<>();
    final ArraySet<String> mInstallerPackages = new ArraySet<>();
    private final ArrayMap<String, KernelPackageState> mKernelMapping = new ArrayMap<>();
    private final File mKernelMappingFilename;
    public final KeySetManagerService mKeySetManagerService = new KeySetManagerService(this.mPackages);
    private final ArrayMap<Long, Integer> mKeySetRefs = new ArrayMap<>();
    private final Object mLock;
    final SparseIntArray mNextAppLinkGeneration = new SparseIntArray();
    private final SparseArray<SettingBase> mOtherAppIds = new SparseArray<>();
    private final File mPackageListFilename;
    private final Object mPackageVerify = new Object();
    final ArrayMap<String, PackageSetting> mPackages = new ArrayMap<>();
    private final ArrayList<Signature> mPastSignatures = new ArrayList<>();
    private final ArrayList<PackageSetting> mPendingPackages = new ArrayList<>();
    final PermissionSettings mPermissions;
    final SparseArray<PersistentPreferredIntentResolver> mPersistentPreferredActivities = new SparseArray<>();
    final SparseArray<PreferredIntentResolver> mPreferredActivities = new SparseArray<>();
    Boolean mReadExternalStorageEnforced;
    final StringBuilder mReadMessages = new StringBuilder();
    private final ArrayMap<String, String> mRenamedPackages = new ArrayMap<>();
    private final ArrayMap<String, IntentFilterVerificationInfo> mRestoredIntentFilterVerifications = new ArrayMap<>();
    private final RuntimePermissionPersistence mRuntimePermissionsPersistence;
    private final File mSettingsFilename;
    private final File mSettingsFilenameToBeDeleted;
    final ArrayMap<String, SharedUserSetting> mSharedUsers = new ArrayMap<>();
    private final File mStoppedPackagesFilename;
    private final File mSystemDir;
    private VerifierDeviceIdentity mVerifierDeviceIdentity;
    private ArrayMap<String, VersionInfo> mVersion = new ArrayMap<>();
    PackageManagerService packagems;

    public static class DatabaseVersion {
        public static final int FIRST_VERSION = 1;
        public static final int SIGNATURE_END_ENTITY = 2;
        public static final int SIGNATURE_MALFORMED_RECOVER = 3;
    }

    /* access modifiers changed from: private */
    public static final class KernelPackageState {
        int appId;
        int[] excludedUserIds;

        private KernelPackageState() {
        }
    }

    public static class VersionInfo {
        int databaseVersion;
        String fingerprint;
        int sdkVersion;

        public void forceCurrent() {
            this.sdkVersion = Build.VERSION.SDK_INT;
            this.databaseVersion = 3;
            this.fingerprint = Build.FINGERPRINT;
        }
    }

    Settings(File dataDir, PermissionSettings permission, Object lock) {
        this.mLock = lock;
        this.mPermissions = permission;
        this.mRuntimePermissionsPersistence = new RuntimePermissionPersistence(this.mLock);
        this.mSystemDir = new File(dataDir, "system");
        this.mSystemDir.mkdirs();
        FileUtils.setPermissions(this.mSystemDir.toString(), 509, -1, -1);
        this.mSettingsFilename = new File(this.mSystemDir, "packages.xml");
        this.mBackupSettingsFilename = new File(this.mSystemDir, "packages-backup.xml");
        this.mPackageListFilename = new File(this.mSystemDir, "packages.list");
        FileUtils.setPermissions(this.mPackageListFilename, 416, 1000, 1032);
        this.mBackupVerifiedFilename = new File(this.mSystemDir, "packages-backup-verified.xml");
        this.mSettingsFilenameToBeDeleted = new File(this.mSystemDir, "packages-deleted.xml");
        this.mBackupVerifiedFilenameToBeDeleted = new File(this.mSystemDir, "packages-backup-verified-deleted.xml");
        File kernelDir = new File("/config/sdcardfs");
        this.mKernelMappingFilename = kernelDir.exists() ? kernelDir : null;
        this.mStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped.xml");
        this.mBackupStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped-backup.xml");
    }

    /* access modifiers changed from: package-private */
    public PackageSetting getPackageLPr(String pkgName) {
        return this.mPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String getRenamedPackageLPr(String pkgName) {
        return this.mRenamedPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String addRenamedPackageLPw(String pkgName, String origPkgName) {
        return this.mRenamedPackages.put(pkgName, origPkgName);
    }

    public boolean canPropagatePermissionToInstantApp(String permName) {
        return this.mPermissions.canPropagatePermissionToInstantApp(permName);
    }

    /* access modifiers changed from: package-private */
    public void setInstallerPackageName(String pkgName, String installerPkgName) {
        PackageSetting p = this.mPackages.get(pkgName);
        if (p != null) {
            p.setInstallerPackageName(installerPkgName);
            if (installerPkgName != null) {
                this.mInstallerPackages.add(installerPkgName);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting getSharedUserLPw(String name, int pkgFlags, int pkgPrivateFlags, boolean create) throws PackageManagerException {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null && create) {
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = acquireAndRegisterNewAppIdLPw(s);
            if (s.userId >= 0) {
                Log.i("PackageManager", "New shared user " + name + ": id=" + s.userId);
                this.mSharedUsers.put(name, s);
            } else {
                throw new PackageManagerException(-4, "Creating shared user " + name + " failed");
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public Collection<SharedUserSetting> getAllSharedUsersLPw() {
        return this.mSharedUsers.values();
    }

    /* access modifiers changed from: package-private */
    public boolean disableSystemPackageLPw(String name, boolean replaced) {
        PackageSetting disabled;
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not an installed package");
            return false;
        } else if (this.mDisabledSysPackages.get(name) != null || p.pkg == null || ((!p.pkg.isSystem() || p.pkg.isUpdatedSystemApp()) && !sPmsExt.isRemovableSysApp(name))) {
            return false;
        } else {
            if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
                p.pkg.applicationInfo.flags |= 128;
            }
            if (replaced) {
                disabled = new PackageSetting(p);
            } else {
                disabled = p;
            }
            this.mDisabledSysPackages.put(name, disabled);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public PackageSetting enableSystemPackageLPw(String name) {
        PackageSetting p = this.mDisabledSysPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not disabled");
            return null;
        }
        if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
            p.pkg.applicationInfo.flags &= -129;
        }
        PackageSetting ret = addPackageLPw(name, p.realName, p.codePath, p.resourcePath, p.legacyNativeLibraryPathString, p.primaryCpuAbiString, p.secondaryCpuAbiString, p.cpuAbiOverrideString, p.appId, p.versionCode, p.pkgFlags, p.pkgPrivateFlags, p.parentPackageName, p.childPackageNames, p.usesStaticLibraries, p.usesStaticLibrariesVersions);
        this.mDisabledSysPackages.remove(name);
        return ret;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisabledSystemPackageLPr(String name) {
        return this.mDisabledSysPackages.containsKey(name);
    }

    /* access modifiers changed from: package-private */
    public void removeDisabledSystemPackageLPw(String name) {
        this.mDisabledSysPackages.remove(name);
    }

    /* access modifiers changed from: package-private */
    public PackageSetting addPackageLPw(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int uid, long vc, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames, String[] usesStaticLibraries, long[] usesStaticLibraryNames) {
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            PackageSetting p2 = new PackageSetting(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, vc, pkgFlags, pkgPrivateFlags, parentPackageName, childPackageNames, 0, usesStaticLibraries, usesStaticLibraryNames);
            p2.appId = uid;
            if (!registerExistingAppIdLPw(uid, p2, name)) {
                return null;
            }
            this.mPackages.put(name, p2);
            return p2;
        } else if (p.appId == uid) {
            return p;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate package, keeping first: " + name);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void addAppOpPackage(String permName, String packageName) {
        this.mPermissions.addAppOpPackage(permName, packageName);
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null) {
            SharedUserSetting s2 = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s2.userId = uid;
            if (!registerExistingAppIdLPw(uid, s2, name)) {
                return null;
            }
            this.mSharedUsers.put(name, s2);
            return s2;
        } else if (s.userId == uid) {
            return s;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void pruneSharedUsersLPw() {
        ArrayList<String> removeStage = new ArrayList<>();
        for (Map.Entry<String, SharedUserSetting> entry : this.mSharedUsers.entrySet()) {
            SharedUserSetting sus = entry.getValue();
            if (sus == null) {
                removeStage.add(entry.getKey());
            } else {
                Iterator<PackageSetting> iter = sus.packages.iterator();
                while (iter.hasNext()) {
                    if (this.mPackages.get(iter.next().name) == null) {
                        iter.remove();
                    }
                }
                if (sus.packages.size() == 0) {
                    removeStage.add(entry.getKey());
                }
            }
        }
        for (int i = 0; i < removeStage.size(); i++) {
            this.mSharedUsers.remove(removeStage.get(i));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00f9, code lost:
        if (isAdbInstallDisallowed(r63, r6.id) != false) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0100, code lost:
        if (r4 == r6.id) goto L_0x0105;
     */
    static PackageSetting createNewSetting(String pkgName, PackageSetting originalPkg, PackageSetting disabledPkg, String realPkgName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, long versionCode, int pkgFlags, int pkgPrivateFlags, UserHandle installUser, boolean allowInstall, boolean instantApp, boolean virtualPreload, String parentPkgName, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) {
        PackageSetting pkgSetting;
        boolean installed;
        if (originalPkg != null) {
            if (PackageManagerService.DEBUG_UPGRADE) {
                Log.v("PackageManager", "Package " + pkgName + " is adopting original package " + originalPkg.name);
            }
            pkgSetting = new PackageSetting(originalPkg, pkgName);
            pkgSetting.childPackageNames = childPkgNames != null ? new ArrayList(childPkgNames) : null;
            pkgSetting.codePath = codePath;
            pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            pkgSetting.parentPackageName = parentPkgName;
            pkgSetting.pkgFlags = pkgFlags;
            pkgSetting.pkgPrivateFlags = pkgPrivateFlags;
            pkgSetting.primaryCpuAbiString = primaryCpuAbi;
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
            pkgSetting.signatures = new PackageSignatures();
            pkgSetting.versionCode = versionCode;
            pkgSetting.usesStaticLibraries = usesStaticLibraries;
            pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
            pkgSetting.setTimeStamp(codePath.lastModified());
        } else {
            pkgSetting = new PackageSetting(pkgName, realPkgName, codePath, resourcePath, legacyNativeLibraryPath, primaryCpuAbi, secondaryCpuAbi, null, versionCode, pkgFlags, pkgPrivateFlags, parentPkgName, childPkgNames, 0, usesStaticLibraries, usesStaticLibrariesVersions);
            pkgSetting.setTimeStamp(codePath.lastModified());
            pkgSetting.sharedUser = sharedUser;
            if ((pkgFlags & 1) == 0) {
                List<UserInfo> users = getAllUsers(userManager);
                int installUserId = installUser != null ? installUser.getIdentifier() : 0;
                if (users != null && allowInstall) {
                    Iterator<UserInfo> it = users.iterator();
                    while (it.hasNext()) {
                        UserInfo user = it.next();
                        if (user.id != 999) {
                            if (installUser != null) {
                                if (installUserId == -1) {
                                }
                            }
                            installed = true;
                            pkgSetting.setUserState(user.id, 0, 0, installed, true, true, false, 0, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                        }
                        installed = false;
                        pkgSetting.setUserState(user.id, 0, 0, installed, true, true, false, 0, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                    }
                }
            }
            if (sharedUser != null) {
                pkgSetting.appId = sharedUser.userId;
            } else if (disabledPkg != null) {
                pkgSetting.signatures = new PackageSignatures(disabledPkg.signatures);
                pkgSetting.appId = disabledPkg.appId;
                pkgSetting.getPermissionsState().copyFrom(disabledPkg.getPermissionsState());
                List<UserInfo> users2 = getAllUsers(userManager);
                if (users2 != null) {
                    for (UserInfo user2 : users2) {
                        int userId = user2.id;
                        pkgSetting.setDisabledComponentsCopy(disabledPkg.getDisabledComponents(userId), userId);
                        pkgSetting.setEnabledComponentsCopy(disabledPkg.getEnabledComponents(userId), userId);
                    }
                }
            }
        }
        return pkgSetting;
    }

    static void updatePackageSetting(PackageSetting pkgSetting, PackageSetting disabledPkg, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, int pkgFlags, int pkgPrivateFlags, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) throws PackageManagerException {
        String str;
        String pkgName = pkgSetting.name;
        if (pkgSetting.sharedUser != sharedUser) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package ");
            sb.append(pkgName);
            sb.append(" shared user changed from ");
            String str2 = "<nothing>";
            sb.append(pkgSetting.sharedUser != null ? pkgSetting.sharedUser.name : str2);
            sb.append(" to ");
            if (sharedUser != null) {
                str2 = sharedUser.name;
            }
            sb.append(str2);
            PackageManagerService.reportSettingsProblem(5, sb.toString());
            throw new PackageManagerException(-8, "Updating application package " + pkgName + " failed");
        }
        OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).updateMultiUserInstallAppState(pkgSetting.name, pkgSetting.getInstalled(0), pkgSetting.getInstalled(ZenModeHelper.OPPO_MULTI_USER_ID));
        String str3 = " system";
        String str4 = "";
        if (!pkgSetting.codePath.equals(codePath)) {
            boolean isSystem = pkgSetting.isSystem();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Update");
            str = str3;
            if (!isSystem) {
                str3 = str4;
            }
            sb2.append(str3);
            sb2.append(" package ");
            sb2.append(pkgName);
            sb2.append(" code path from ");
            sb2.append(pkgSetting.codePathString);
            sb2.append(" to ");
            sb2.append(codePath.toString());
            sb2.append("; Retain data and using new");
            Slog.i("PackageManager", sb2.toString());
            if (!isSystem) {
                if ((pkgFlags & 1) != 0 && disabledPkg == null) {
                    List<UserInfo> allUserInfos = getAllUsers(userManager);
                    if (allUserInfos != null) {
                        for (UserInfo userInfo : allUserInfos) {
                            pkgSetting.setInstalled(true, userInfo.id);
                            isSystem = isSystem;
                            allUserInfos = allUserInfos;
                        }
                    }
                }
                pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            }
            pkgSetting.codePath = codePath;
            pkgSetting.codePathString = codePath.toString();
        } else {
            str = str3;
        }
        if (!pkgSetting.resourcePath.equals(resourcePath)) {
            boolean isSystem2 = pkgSetting.isSystem();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Update");
            if (isSystem2) {
                str4 = str;
            }
            sb3.append(str4);
            sb3.append(" package ");
            sb3.append(pkgName);
            sb3.append(" resource path from ");
            sb3.append(pkgSetting.resourcePathString);
            sb3.append(" to ");
            sb3.append(resourcePath.toString());
            sb3.append("; Retain data and using new");
            Slog.i("PackageManager", sb3.toString());
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.resourcePathString = resourcePath.toString();
        }
        pkgSetting.pkgFlags &= -2;
        pkgSetting.pkgPrivateFlags &= -1076756489;
        pkgSetting.pkgFlags |= pkgFlags & 1;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & 8;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_DOMAIN_PREFERRED;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_FROZEN;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_COMPILER_STATS;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & 1073741824;
        pkgSetting.primaryCpuAbiString = primaryCpuAbi;
        pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
        if (childPkgNames != null) {
            pkgSetting.childPackageNames = new ArrayList(childPkgNames);
        }
        if (usesStaticLibraries == null || usesStaticLibrariesVersions == null || usesStaticLibraries.length != usesStaticLibrariesVersions.length) {
            pkgSetting.usesStaticLibraries = null;
            pkgSetting.usesStaticLibrariesVersions = null;
            return;
        }
        pkgSetting.usesStaticLibraries = usesStaticLibraries;
        pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
    }

    /* access modifiers changed from: package-private */
    public boolean registerAppIdLPw(PackageSetting p) throws PackageManagerException {
        boolean createdNew;
        if (p.appId == 0) {
            p.appId = acquireAndRegisterNewAppIdLPw(p);
            createdNew = true;
        } else {
            createdNew = registerExistingAppIdLPw(p.appId, p, p.name);
        }
        if (p.appId >= 0) {
            return createdNew;
        }
        PackageManagerService.reportSettingsProblem(5, "Package " + p.name + " could not be assigned a valid UID");
        throw new PackageManagerException(-4, "Package " + p.name + " could not be assigned a valid UID");
    }

    /* access modifiers changed from: package-private */
    public void writeUserRestrictionsLPw(PackageSetting newPackage, PackageSetting oldPackage) {
        List<UserInfo> allUsers;
        PackageUserState oldUserState;
        if (!(getPackageLPr(newPackage.name) == null || (allUsers = getAllUsers(UserManagerService.getInstance())) == null)) {
            for (UserInfo user : allUsers) {
                if (oldPackage == null) {
                    oldUserState = PackageSettingBase.DEFAULT_USER_STATE;
                } else {
                    oldUserState = oldPackage.readUserState(user.id);
                }
                if (!oldUserState.equals(newPackage.readUserState(user.id))) {
                    writePackageRestrictionsLPr(user.id);
                }
            }
        }
    }

    static boolean isAdbInstallDisallowed(UserManagerService userManager, int userId) {
        return userManager.hasUserRestriction("no_debugging_features", userId);
    }

    /* access modifiers changed from: package-private */
    public void insertPackageSettingLPw(PackageSetting p, PackageParser.Package pkg) {
        if (p.signatures.mSigningDetails.signatures == null) {
            p.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        if (p.sharedUser != null && p.sharedUser.signatures.mSigningDetails.signatures == null) {
            p.sharedUser.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        addPackageSettingLPw(p, p.sharedUser);
    }

    private void addPackageSettingLPw(PackageSetting p, SharedUserSetting sharedUser) {
        this.mPackages.put(p.name, p);
        if (sharedUser != null) {
            if (p.sharedUser != null && p.sharedUser != sharedUser) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user " + p.sharedUser + " but is now " + sharedUser + "; I am not changing its files so it will probably fail!");
                p.sharedUser.removePackage(p);
            } else if (p.appId != sharedUser.userId) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user id " + p.appId + " but is now user " + sharedUser + " with id " + sharedUser.userId + "; I am not changing its files so it will probably fail!");
            }
            sharedUser.addPackage(p);
            p.sharedUser = sharedUser;
            p.appId = sharedUser.userId;
        }
        Object userIdPs = getSettingLPr(p.appId);
        if (sharedUser == null) {
            if (!(userIdPs == null || userIdPs == p)) {
                replaceAppIdLPw(p.appId, p);
            }
        } else if (!(userIdPs == null || userIdPs == sharedUser)) {
            replaceAppIdLPw(p.appId, sharedUser);
        }
        IntentFilterVerificationInfo ivi = this.mRestoredIntentFilterVerifications.get(p.name);
        if (ivi != null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.i(TAG, "Applying restored IVI for " + p.name + " : " + ivi.getStatusString());
            }
            this.mRestoredIntentFilterVerifications.remove(p.name);
            p.setIntentFilterVerificationInfo(ivi);
        }
    }

    /* access modifiers changed from: package-private */
    public int updateSharedUserPermsLPw(PackageSetting deletedPs, int userId) {
        if (deletedPs == null || deletedPs.pkg == null) {
            Slog.i("PackageManager", "Trying to update info for null package. Just ignoring");
            return -10000;
        } else if (deletedPs.sharedUser == null) {
            return -10000;
        } else {
            SharedUserSetting sus = deletedPs.sharedUser;
            int affectedUserId = -10000;
            Iterator it = deletedPs.pkg.requestedPermissions.iterator();
            while (it.hasNext()) {
                String eachPerm = (String) it.next();
                BasePermission bp = this.mPermissions.getPermission(eachPerm);
                if (bp != null) {
                    boolean used = false;
                    Iterator<PackageSetting> it2 = sus.packages.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        PackageSetting pkg = it2.next();
                        if (pkg.pkg != null && !pkg.pkg.packageName.equals(deletedPs.pkg.packageName) && pkg.pkg.requestedPermissions.contains(eachPerm)) {
                            used = true;
                            break;
                        }
                    }
                    if (!used) {
                        PermissionsState permissionsState = sus.getPermissionsState();
                        PackageSetting disabledPs = getDisabledSystemPkgLPr(deletedPs.pkg.packageName);
                        if (disabledPs != null) {
                            boolean reqByDisabledSysPkg = false;
                            Iterator it3 = disabledPs.pkg.requestedPermissions.iterator();
                            while (true) {
                                if (it3.hasNext()) {
                                    if (((String) it3.next()).equals(eachPerm)) {
                                        reqByDisabledSysPkg = true;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (reqByDisabledSysPkg) {
                            }
                        }
                        permissionsState.updatePermissionFlags(bp, userId, 64511, 0);
                        if (permissionsState.revokeInstallPermission(bp) == 1) {
                            affectedUserId = -1;
                        }
                        if (permissionsState.revokeRuntimePermission(bp, userId) == 1) {
                            if (affectedUserId == -10000) {
                                affectedUserId = userId;
                            } else if (affectedUserId != userId) {
                                affectedUserId = -1;
                            }
                        }
                    }
                }
            }
            return affectedUserId;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePackageLPw(String name) {
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            return -1;
        }
        this.mPackages.remove(name);
        removeInstallerPackageStatus(name);
        if (p.sharedUser != null) {
            p.sharedUser.removePackage(p);
            if (p.sharedUser.packages.size() != 0) {
                return -1;
            }
            this.mSharedUsers.remove(p.sharedUser.name);
            removeAppIdLPw(p.sharedUser.userId);
            return p.sharedUser.userId;
        }
        removeAppIdLPw(p.appId);
        return p.appId;
    }

    private void removeInstallerPackageStatus(String packageName) {
        if (this.mInstallerPackages.contains(packageName)) {
            for (int i = 0; i < this.mPackages.size(); i++) {
                PackageSetting ps = this.mPackages.valueAt(i);
                String installerPackageName = ps.getInstallerPackageName();
                if (installerPackageName != null && installerPackageName.equals(packageName)) {
                    ps.setInstallerPackageName(null);
                    ps.isOrphaned = true;
                }
            }
            this.mInstallerPackages.remove(packageName);
        }
    }

    private boolean registerExistingAppIdLPw(int appId, SettingBase obj, Object name) {
        if (appId > 19999) {
            return false;
        }
        if (appId >= 10000) {
            int index = appId - 10000;
            for (int size = this.mAppIds.size(); index >= size; size++) {
                this.mAppIds.add(null);
            }
            if (this.mAppIds.get(index) != null) {
                PackageManagerService.reportSettingsProblem(6, "Adding duplicate app id: " + appId + " name=" + name);
                return false;
            }
            this.mAppIds.set(index, obj);
            return true;
        } else if (this.mOtherAppIds.get(appId) != null) {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared id: " + appId + " name=" + name);
            return false;
        } else {
            this.mOtherAppIds.put(appId, obj);
            return true;
        }
    }

    public SettingBase getSettingLPr(int appId) {
        if (appId < 10000) {
            return this.mOtherAppIds.get(appId);
        }
        int index = appId - 10000;
        if (index < this.mAppIds.size()) {
            return this.mAppIds.get(index);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeAppIdLPw(int appId) {
        if (appId >= 10000) {
            int index = appId - 10000;
            if (index < this.mAppIds.size()) {
                this.mAppIds.set(index, null);
            }
        } else {
            this.mOtherAppIds.remove(appId);
        }
        setFirstAvailableUid(appId + 1);
    }

    private void replaceAppIdLPw(int appId, SettingBase obj) {
        if (appId >= 10000) {
            int index = appId - 10000;
            if (index < this.mAppIds.size()) {
                this.mAppIds.set(index, obj);
                return;
            }
            return;
        }
        this.mOtherAppIds.put(appId, obj);
    }

    /* access modifiers changed from: package-private */
    public PreferredIntentResolver editPreferredActivitiesLPw(int userId) {
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            return pir;
        }
        PreferredIntentResolver pir2 = new PreferredIntentResolver();
        this.mPreferredActivities.put(userId, pir2);
        return pir2;
    }

    /* access modifiers changed from: package-private */
    public PersistentPreferredIntentResolver editPersistentPreferredActivitiesLPw(int userId) {
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            return ppir;
        }
        PersistentPreferredIntentResolver ppir2 = new PersistentPreferredIntentResolver();
        this.mPersistentPreferredActivities.put(userId, ppir2);
        return ppir2;
    }

    /* access modifiers changed from: package-private */
    public CrossProfileIntentResolver editCrossProfileIntentResolverLPw(int userId) {
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            return cpir;
        }
        CrossProfileIntentResolver cpir2 = new CrossProfileIntentResolver();
        this.mCrossProfileIntentResolvers.put(userId, cpir2);
        return cpir2;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo getIntentFilterVerificationLPr(String packageName) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return ps.getIntentFilterVerificationInfo();
        }
        if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return null;
        }
        Slog.w("PackageManager", "No package known: " + packageName);
        return null;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo createIntentFilterVerificationIfNeededLPw(String packageName, ArraySet<String> domains) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (ivi == null) {
                ivi = new IntentFilterVerificationInfo(packageName, domains);
                ps.setIntentFilterVerificationInfo(ivi);
                if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.d("PackageManager", "Creating new IntentFilterVerificationInfo for pkg: " + packageName);
                }
            } else {
                ivi.setDomains(domains);
                if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.d("PackageManager", "Setting domains to existing IntentFilterVerificationInfo for pkg: " + packageName + " and with domains: " + ivi.getDomainsString());
                }
            }
            return ivi;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return null;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public int getIntentFilterVerificationStatusLPr(String packageName, int userId) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return (int) (ps.getDomainVerificationStatusForUser(userId) >> 32);
        }
        if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return 0;
        }
        Slog.w("PackageManager", "No package known: " + packageName);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean updateIntentFilterVerificationStatusLPw(String packageName, int status, int userId) {
        int alwaysGeneration;
        PackageSetting current = this.mPackages.get(packageName);
        if (current != null) {
            if (status == 2) {
                alwaysGeneration = this.mNextAppLinkGeneration.get(userId) + 1;
                this.mNextAppLinkGeneration.put(userId, alwaysGeneration);
            } else {
                alwaysGeneration = 0;
            }
            current.setDomainVerificationStatusForUser(status, alwaysGeneration, userId);
            return true;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return false;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public List<IntentFilterVerificationInfo> getIntentFilterVerificationsLPr(String packageName) {
        if (packageName == null) {
            return Collections.emptyList();
        }
        ArrayList<IntentFilterVerificationInfo> result = new ArrayList<>();
        for (PackageSetting ps : this.mPackages.values()) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (ivi != null && !TextUtils.isEmpty(ivi.getPackageName()) && ivi.getPackageName().equalsIgnoreCase(packageName)) {
                result.add(ivi);
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int userId, boolean alsoResetStatus) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            if (alsoResetStatus) {
                ps.clearDomainVerificationStatusForUser(userId);
            }
            ps.setIntentFilterVerificationInfo(null);
            return true;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return false;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int[] userIds) {
        boolean result = false;
        for (int userId : userIds) {
            result |= removeIntentFilterVerificationLPw(packageName, userId, true);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public String removeDefaultBrowserPackageNameLPw(int userId) {
        if (userId == -1) {
            return null;
        }
        return (String) this.mDefaultBrowserApp.removeReturnOld(userId);
    }

    private File getUserPackagesStateFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), "package-restrictions.xml");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private File getUserRuntimePermissionsFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), RUNTIME_PERMISSIONS_FILE_NAME);
    }

    private File getUserPackagesStateBackupFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), "package-restrictions-backup.xml");
    }

    /* access modifiers changed from: package-private */
    public void writeAllUsersPackageRestrictionsLPr() {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        if (users != null) {
            for (UserInfo user : users) {
                writePackageRestrictionsLPr(user.id);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllRuntimePermissionsLPr() {
        for (int userId : UserManagerService.getInstance().getUserIds()) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
        return this.mRuntimePermissionsPersistence.areDefaultRuntimePermissionsGrantedLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void setRuntimePermissionsFingerPrintLPr(String fingerPrint, int userId) {
        this.mRuntimePermissionsPersistence.setRuntimePermissionsFingerPrintLPr(fingerPrint, userId);
    }

    /* access modifiers changed from: package-private */
    public int getDefaultRuntimePermissionsVersionLPr(int userId) {
        return this.mRuntimePermissionsPersistence.getVersionLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void setDefaultRuntimePermissionsVersionLPr(int version, int userId) {
        this.mRuntimePermissionsPersistence.setVersionLPr(version, userId);
    }

    public VersionInfo findOrCreateVersion(String volumeUuid) {
        VersionInfo ver = this.mVersion.get(volumeUuid);
        if (ver != null) {
            return ver;
        }
        VersionInfo ver2 = new VersionInfo();
        this.mVersion.put(volumeUuid, ver2);
        return ver2;
    }

    public VersionInfo getInternalVersion() {
        return this.mVersion.get(StorageManager.UUID_PRIVATE_INTERNAL);
    }

    public VersionInfo getExternalVersion() {
        return this.mVersion.get("primary_physical");
    }

    public void onVolumeForgotten(String fsUuid) {
        this.mVersion.remove(fsUuid);
    }

    /* access modifiers changed from: package-private */
    public void readPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                try {
                    if (parser.getName().equals(TAG_ITEM)) {
                        PreferredActivity pa = new PreferredActivity(parser);
                        if (pa.mPref.getParseError() == null) {
                            PreferredIntentResolver resolver = editPreferredActivitiesLPw(userId);
                            ArrayList<PreferredActivity> pal = resolver.findFilters(pa);
                            if (pal == null || pal.size() == 0) {
                                resolver.addFilter(pa);
                            }
                        } else {
                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + pa.mPref.getParseError() + " at " + parser.getPositionDescription());
                        }
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                } catch (Exception e) {
                    PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                }
            }
        }
    }

    private void readPersistentPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    editPersistentPreferredActivitiesLPw(userId).addFilter(new PersistentPreferredActivity(parser));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <persistent-preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readCrossProfileIntentFiltersLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_ITEM)) {
                    editCrossProfileIntentResolverLPw(userId).addFilter(new CrossProfileIntentFilter(parser));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under crossProfile-intent-filters: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readDomainVerificationLPw(XmlPullParser parser, PackageSettingBase packageSetting) throws XmlPullParserException, IOException {
        packageSetting.setIntentFilterVerificationInfo(new IntentFilterVerificationInfo(parser));
    }

    private void readRestoredIntentFilterVerifications(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Restored IVI for " + ivi.getPackageName() + " status=" + ivi.getStatusString());
                    }
                    this.mRestoredIntentFilterVerifications.put(ivi.getPackageName(), ivi);
                } else {
                    Slog.w(TAG, "Unknown element: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readDefaultAppsLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                    this.mDefaultBrowserApp.put(userId, parser.getAttributeValue(null, "packageName"));
                } else if (!tagName.equals(TAG_DEFAULT_DIALER)) {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under default-apps: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readBlockUninstallPackagesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        ArraySet<String> packages = new ArraySet<>();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_BLOCK_UNINSTALL)) {
                        packages.add(parser.getAttributeValue(null, "packageName"));
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under block-uninstall-packages: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
        if (packages.isEmpty()) {
            this.mBlockUninstallPackages.remove(userId);
        } else {
            this.mBlockUninstallPackages.put(userId, packages);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:238:0x0652 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:228:0x062e */
    /* JADX DEBUG: Multi-variable search result rejected for r6v21, resolved type: com.android.server.pm.PackageSetting */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r7v34 ??: [D('tagName' java.lang.String), D('installed' boolean)] */
    /* JADX INFO: Multiple debug info for r4v48 ??: [D('ceDataInode' long), D('linkGeneration' int)] */
    /* JADX WARN: Type inference failed for: r7v11, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r4v14, types: [boolean] */
    /* JADX WARN: Type inference failed for: r7v12 */
    /* JADX WARN: Type inference failed for: r4v15 */
    /* JADX WARN: Type inference failed for: r7v14 */
    /* JADX WARN: Type inference failed for: r4v17 */
    /* JADX WARN: Type inference failed for: r4v23 */
    /* JADX WARN: Type inference failed for: r4v24, types: [com.android.server.pm.Settings] */
    /* JADX WARN: Type inference failed for: r7v21, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r4v25, types: [com.android.server.pm.Settings] */
    /* JADX WARN: Type inference failed for: r7v34, types: [boolean] */
    /* JADX WARN: Type inference failed for: r4v48, types: [long] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x063f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0640, code lost:
        r5 = r32;
        r2 = r33;
        r7 = r37;
        r3 = 6;
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x06bb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x06bc, code lost:
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x06c9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x06ca, code lost:
        r4 = r62;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x06cc, code lost:
        r7 = r37;
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x06d0, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x06d1, code lost:
        r4 = r62;
        r7 = r37;
        r5 = r32;
        r2 = r33;
        r3 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x0742, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x0743, code lost:
        r4 = r1;
        r7 = r35;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:279:0x0781, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x0782, code lost:
        r4 = r1;
        r5 = r32;
        r2 = r33;
        r7 = r35;
        r3 = 6;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x06bb A[Catch:{ XmlPullParserException -> 0x0729, IOException -> 0x0725 }, ExcHandler: IOException (e java.io.IOException), PHI: r4 
      PHI: (r4v23 ??) = (r4v24 ??), (r4v24 ??), (r4v25 ??), (r4v25 ??) binds: [B:238:0x0652, B:239:?, B:232:0x0638, B:233:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:232:0x0638] */
    /* JADX WARNING: Removed duplicated region for block: B:259:0x06c9 A[Catch:{ XmlPullParserException -> 0x0729, IOException -> 0x0725 }, ExcHandler: IOException (e java.io.IOException), PHI: r37 r49 
      PHI: (r37v1 java.lang.String) = (r37v2 java.lang.String), (r37v2 java.lang.String), (r37v3 java.lang.String) binds: [B:228:0x062e, B:229:?, B:199:0x0531] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r49v10 'str' java.io.FileInputStream) = (r49v12 'str' java.io.FileInputStream), (r49v12 'str' java.io.FileInputStream), (r49v26 'str' java.io.FileInputStream) binds: [B:228:0x062e, B:229:?, B:199:0x0531] A[DONT_GENERATE, DONT_INLINE], Splitter:B:199:0x0531] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0742 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:56:0x0195] */
    /* JADX WARNING: Unknown variable types count: 2 */
    public void readPackageRestrictionsLPr(int userId) {
        FileInputStream str;
        String str2;
        String str3;
        Settings settings;
        int i;
        String str4;
        XmlPullParserException e;
        String str5;
        String str6;
        String str7;
        Settings settings2;
        IOException e2;
        String str8;
        File userPackagesStateFile;
        FileInputStream str9;
        String str10;
        Settings settings3;
        int type;
        int i2;
        Settings settings4;
        FileInputStream str11;
        int i3;
        FileInputStream str12;
        String str13;
        int i4;
        File userPackagesStateFile2;
        int oppoFreezeState;
        String str14;
        int outerDepth;
        int type2;
        String str15;
        String str16;
        Settings settings5;
        XmlPullParser parser;
        long ceDataInode;
        String harmfulAppWarning;
        int oppoFreezeState2;
        int oppoFreezeFlag;
        int verifState;
        int linkGeneration;
        int maxAppLinkGeneration;
        int installReason;
        SuspendDialogInfo suspendDialogInfo;
        PersistableBundle suspendedAppExtras;
        PersistableBundle suspendedLauncherExtras;
        ArraySet<String> enabledComponents;
        ArraySet<String> disabledComponents;
        int type3;
        String str17;
        XmlPullParser parser2;
        int i5;
        String name;
        int packageDepth;
        String str18;
        char c;
        String str19;
        Settings settings6 = this;
        int i6 = userId;
        String str20 = TAG_PACKAGE;
        File userPackagesStateFile3 = getUserPackagesStateFile(userId);
        File backupFile = getUserPackagesStateBackupFile(userId);
        String str21 = "PackageManager";
        int i7 = 4;
        if (backupFile.exists()) {
            try {
                FileInputStream str22 = new FileInputStream(backupFile);
                settings6.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (userPackagesStateFile3.exists()) {
                    Slog.w(str21, "Cleaning up stopped packages file " + userPackagesStateFile3);
                    userPackagesStateFile3.delete();
                }
                str = str22;
            } catch (IOException e3) {
                str = null;
            }
        } else {
            str = null;
        }
        String str23 = "Error reading package manager stopped packages";
        String str24 = "Error reading: ";
        boolean z = true;
        boolean z2 = false;
        if (str == null) {
            try {
                if (!userPackagesStateFile3.exists()) {
                    try {
                        settings6.mReadMessages.append("No stopped packages file found\n");
                        PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                        for (PackageSetting pkg : settings6.mPackages.values()) {
                            str6 = str24;
                            str5 = str23;
                            try {
                                pkg.setUserState(userId, 0, 0, !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiAppUserId(i6) ? true : z2, false, false, false, 0, false, null, null, null, null, false, false, null, null, null, 0, 0, 0, null);
                                i6 = userId;
                                str24 = str6;
                                str23 = str5;
                                backupFile = backupFile;
                                str21 = str21;
                                userPackagesStateFile3 = userPackagesStateFile3;
                                z2 = false;
                            } catch (XmlPullParserException e4) {
                                e = e4;
                                settings = settings6;
                                str3 = str6;
                                str4 = str5;
                                str2 = str21;
                                i = 6;
                                settings.mReadMessages.append(str3 + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf(str2, str4, e);
                            } catch (IOException e5) {
                                e2 = e5;
                                settings2 = settings6;
                                str7 = str21;
                                settings2.mReadMessages.append(str6 + e2.toString());
                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                Slog.wtf(str7, str5, e2);
                            }
                        }
                        return;
                    } catch (XmlPullParserException e6) {
                        e = e6;
                        settings = settings6;
                        str4 = str23;
                        str2 = str21;
                        str3 = str24;
                        i = 6;
                        settings.mReadMessages.append(str3 + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf(str2, str4, e);
                    } catch (IOException e7) {
                        e2 = e7;
                        str5 = str23;
                        str6 = str24;
                        settings2 = settings6;
                        str7 = str21;
                        settings2.mReadMessages.append(str6 + e2.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                        Slog.wtf(str7, str5, e2);
                    }
                } else {
                    str5 = str23;
                    str8 = str21;
                    str6 = str24;
                    try {
                        userPackagesStateFile = userPackagesStateFile3;
                        try {
                            str9 = new FileInputStream(userPackagesStateFile);
                        } catch (XmlPullParserException e8) {
                            e = e8;
                            settings = settings6;
                            str3 = str6;
                            str4 = str5;
                            str2 = str8;
                            i = 6;
                            settings.mReadMessages.append(str3 + e.toString());
                            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                            Slog.wtf(str2, str4, e);
                        } catch (IOException e9) {
                            e2 = e9;
                            settings2 = settings6;
                            str7 = str8;
                            settings2.mReadMessages.append(str6 + e2.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                            Slog.wtf(str7, str5, e2);
                        }
                    } catch (XmlPullParserException e10) {
                        e = e10;
                        settings = settings6;
                        str3 = str6;
                        str4 = str5;
                        str2 = str8;
                        i = 6;
                        settings.mReadMessages.append(str3 + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf(str2, str4, e);
                    } catch (IOException e11) {
                        e2 = e11;
                        settings2 = settings6;
                        str7 = str8;
                        settings2.mReadMessages.append(str6 + e2.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                        Slog.wtf(str7, str5, e2);
                    }
                }
            } catch (XmlPullParserException e12) {
                e = e12;
                settings = settings6;
                str4 = str23;
                str2 = str21;
                str3 = str24;
                i = 6;
                settings.mReadMessages.append(str3 + e.toString());
                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                Slog.wtf(str2, str4, e);
            } catch (IOException e13) {
                e2 = e13;
                str5 = str23;
                str6 = str24;
                settings2 = settings6;
                str7 = str21;
                settings2.mReadMessages.append(str6 + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                Slog.wtf(str7, str5, e2);
            }
        } else {
            str5 = str23;
            str8 = str21;
            userPackagesStateFile = userPackagesStateFile3;
            str6 = str24;
            str9 = str;
        }
        try {
            XmlPullParser parser3 = Xml.newPullParser();
            parser3.setInput(str9, StandardCharsets.UTF_8.name());
            do {
                type = parser3.next();
                i2 = 2;
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                try {
                    settings6.mReadMessages.append("No start tag found in package restrictions file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                } catch (XmlPullParserException e14) {
                    e = e14;
                    settings = settings6;
                    str3 = str6;
                    str4 = str5;
                    str2 = str8;
                    i = 6;
                    settings.mReadMessages.append(str3 + e.toString());
                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                    Slog.wtf(str2, str4, e);
                } catch (IOException e15) {
                    e2 = e15;
                    settings2 = settings6;
                    str7 = str8;
                    settings2.mReadMessages.append(str6 + e2.toString());
                    PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                    Slog.wtf(str7, str5, e2);
                }
            } else {
                int maxAppLinkGeneration2 = 0;
                int outerDepth2 = parser3.getDepth();
                String str25 = null;
                while (true) {
                    int type4 = parser3.next();
                    if (type4 == z) {
                        settings4 = settings6;
                        str11 = str9;
                        i3 = userId;
                    } else if (type4 == 3 && parser3.getDepth() <= outerDepth2) {
                        settings4 = settings6;
                        str11 = str9;
                        i3 = userId;
                    } else if (type4 == 3 || type4 == i7) {
                        parser3 = parser3;
                        settings6 = settings6;
                        str8 = str8;
                        str25 = str25;
                        outerDepth2 = outerDepth2;
                        str20 = str20;
                        i2 = i2;
                        userPackagesStateFile = userPackagesStateFile;
                        str9 = str9;
                        i7 = i7;
                        z = true;
                    } else {
                        ?? name2 = parser3.getName();
                        ?? equals = name2.equals(str20);
                        if (equals != 0) {
                            try {
                                String name3 = parser3.getAttributeValue(str25, ATTR_NAME);
                                PackageSetting ps = settings6.mPackages.get(name3);
                                if (ps == 0) {
                                    try {
                                        Slog.w(str8, "No package known for stopped package " + name3);
                                        XmlUtils.skipCurrentTag(parser3);
                                        str8 = str8;
                                        i2 = 2;
                                    } catch (XmlPullParserException e16) {
                                        e = e16;
                                        settings = settings6;
                                        str2 = str8;
                                        str3 = str6;
                                        str4 = str5;
                                        i = 6;
                                        settings.mReadMessages.append(str3 + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf(str2, str4, e);
                                    } catch (IOException e17) {
                                        e2 = e17;
                                        settings2 = settings6;
                                        str7 = str8;
                                        settings2.mReadMessages.append(str6 + e2.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                        Slog.wtf(str7, str5, e2);
                                    }
                                } else {
                                    String str26 = str20;
                                    try {
                                        ceDataInode = XmlUtils.readLongAttribute(parser3, ATTR_CE_DATA_INODE, 0);
                                        str13 = str8;
                                    } catch (XmlPullParserException e18) {
                                        e = e18;
                                        settings3 = this;
                                        str10 = str8;
                                        str3 = str6;
                                        str4 = str5;
                                        i = 6;
                                        settings = settings3;
                                        str2 = str10;
                                        settings.mReadMessages.append(str3 + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf(str2, str4, e);
                                    } catch (IOException e19) {
                                        e2 = e19;
                                        settings2 = this;
                                        str7 = str8;
                                        settings2.mReadMessages.append(str6 + e2.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                        Slog.wtf(str7, str5, e2);
                                    }
                                    try {
                                        String tagName = name2;
                                        name2 = XmlUtils.readBooleanAttribute(parser3, ATTR_INSTALLED, z);
                                        boolean stopped = XmlUtils.readBooleanAttribute(parser3, ATTR_STOPPED, false);
                                        boolean notLaunched = XmlUtils.readBooleanAttribute(parser3, ATTR_NOT_LAUNCHED, false);
                                        String blockedStr = parser3.getAttributeValue(str25, ATTR_BLOCKED);
                                        boolean hidden = blockedStr == null ? false : Boolean.parseBoolean(blockedStr);
                                        String hiddenStr = parser3.getAttributeValue(str25, ATTR_HIDDEN);
                                        boolean hidden2 = hiddenStr == null ? hidden : Boolean.parseBoolean(hiddenStr);
                                        int distractionFlags = XmlUtils.readIntAttribute(parser3, ATTR_DISTRACTION_FLAGS, 0);
                                        outerDepth = outerDepth2;
                                        boolean suspended = XmlUtils.readBooleanAttribute(parser3, ATTR_SUSPENDED, false);
                                        String suspendingPackage = parser3.getAttributeValue(null, ATTR_SUSPENDING_PACKAGE);
                                        try {
                                            String dialogMessage = parser3.getAttributeValue(null, ATTR_SUSPEND_DIALOG_MESSAGE);
                                            String suspendingPackage2 = (!suspended || suspendingPackage != null) ? suspendingPackage : PackageManagerService.PLATFORM_PACKAGE_NAME;
                                            boolean blockUninstall = XmlUtils.readBooleanAttribute(parser3, ATTR_BLOCK_UNINSTALL, false);
                                            boolean instantApp = XmlUtils.readBooleanAttribute(parser3, ATTR_INSTANT_APP, false);
                                            boolean virtualPreload = XmlUtils.readBooleanAttribute(parser3, ATTR_VIRTUAL_PRELOAD, false);
                                            int enabled = XmlUtils.readIntAttribute(parser3, ATTR_ENABLED, 0);
                                            String enabledCaller = parser3.getAttributeValue(null, ATTR_ENABLED_CALLER);
                                            try {
                                                harmfulAppWarning = parser3.getAttributeValue(null, ATTR_HARMFUL_APP_WARNING);
                                                oppoFreezeState2 = XmlUtils.readIntAttribute(parser3, ATTR_OPPO_FREEZED, 0);
                                                oppoFreezeFlag = XmlUtils.readIntAttribute(parser3, ATTR_OPPO_FREEZED_FLAG, 0);
                                                verifState = XmlUtils.readIntAttribute(parser3, ATTR_DOMAIN_VERIFICATON_STATE, 0);
                                                linkGeneration = XmlUtils.readIntAttribute(parser3, ATTR_APP_LINK_GENERATION, 0);
                                                if (linkGeneration > maxAppLinkGeneration2) {
                                                    maxAppLinkGeneration = linkGeneration;
                                                } else {
                                                    maxAppLinkGeneration = maxAppLinkGeneration2;
                                                }
                                                installReason = XmlUtils.readIntAttribute(parser3, ATTR_INSTALL_REASON, 0);
                                                suspendDialogInfo = null;
                                                int packageDepth2 = parser3.getDepth();
                                                suspendedAppExtras = null;
                                                suspendedLauncherExtras = null;
                                                enabledComponents = null;
                                                disabledComponents = null;
                                                while (true) {
                                                    int type5 = parser3.next();
                                                    if (type5 != 1) {
                                                        if (type5 == 3) {
                                                            try {
                                                                packageDepth = packageDepth2;
                                                                if (parser3.getDepth() <= packageDepth) {
                                                                    packageDepth2 = packageDepth;
                                                                    type3 = type5;
                                                                    str17 = str26;
                                                                }
                                                            } catch (XmlPullParserException e20) {
                                                                e = e20;
                                                                settings = settings6;
                                                                str3 = str6;
                                                                str4 = str5;
                                                                str2 = str13;
                                                                i = 6;
                                                                settings.mReadMessages.append(str3 + e.toString());
                                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                                Slog.wtf(str2, str4, e);
                                                            } catch (IOException e21) {
                                                                e2 = e21;
                                                                settings2 = settings6;
                                                                str7 = str13;
                                                                settings2.mReadMessages.append(str6 + e2.toString());
                                                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                                                Slog.wtf(str7, str5, e2);
                                                            }
                                                        } else {
                                                            packageDepth = packageDepth2;
                                                        }
                                                        if (type5 == 3) {
                                                            packageDepth2 = packageDepth;
                                                            str18 = str26;
                                                        } else if (type5 == 4) {
                                                            packageDepth2 = packageDepth;
                                                            str18 = str26;
                                                        } else {
                                                            String name4 = parser3.getName();
                                                            switch (name4.hashCode()) {
                                                                case -2027581689:
                                                                    packageDepth2 = packageDepth;
                                                                    if (name4.equals(TAG_DISABLED_COMPONENTS)) {
                                                                        c = 1;
                                                                        break;
                                                                    }
                                                                    c = 65535;
                                                                    break;
                                                                case -1963032286:
                                                                    packageDepth2 = packageDepth;
                                                                    if (name4.equals(TAG_ENABLED_COMPONENTS)) {
                                                                        c = 0;
                                                                        break;
                                                                    }
                                                                    c = 65535;
                                                                    break;
                                                                case -1592287551:
                                                                    packageDepth2 = packageDepth;
                                                                    if (name4.equals(TAG_SUSPENDED_APP_EXTRAS)) {
                                                                        c = 2;
                                                                        break;
                                                                    }
                                                                    c = 65535;
                                                                    break;
                                                                case -1422791362:
                                                                    packageDepth2 = packageDepth;
                                                                    if (name4.equals(TAG_SUSPENDED_LAUNCHER_EXTRAS)) {
                                                                        c = 3;
                                                                        break;
                                                                    }
                                                                    c = 65535;
                                                                    break;
                                                                case 1660896545:
                                                                    packageDepth2 = packageDepth;
                                                                    if (name4.equals(TAG_SUSPENDED_DIALOG_INFO)) {
                                                                        c = 4;
                                                                        break;
                                                                    }
                                                                    c = 65535;
                                                                    break;
                                                                default:
                                                                    packageDepth2 = packageDepth;
                                                                    c = 65535;
                                                                    break;
                                                            }
                                                            if (c == 0) {
                                                                str19 = str26;
                                                                enabledComponents = settings6.readComponentsLPr(parser3);
                                                            } else if (c == 1) {
                                                                str19 = str26;
                                                                disabledComponents = settings6.readComponentsLPr(parser3);
                                                            } else if (c == 2) {
                                                                str19 = str26;
                                                                suspendedAppExtras = PersistableBundle.restoreFromXml(parser3);
                                                            } else if (c == 3) {
                                                                str19 = str26;
                                                                suspendedLauncherExtras = PersistableBundle.restoreFromXml(parser3);
                                                            } else if (c != 4) {
                                                                StringBuilder sb = new StringBuilder();
                                                                sb.append("Unknown tag ");
                                                                sb.append(parser3.getName());
                                                                sb.append(" under tag ");
                                                                str19 = str26;
                                                                sb.append(str19);
                                                                Slog.wtf(TAG, sb.toString());
                                                            } else {
                                                                str19 = str26;
                                                                suspendDialogInfo = SuspendDialogInfo.restoreFromXml(parser3);
                                                            }
                                                            str26 = str19;
                                                            tagName = tagName;
                                                        }
                                                        str26 = str18;
                                                        tagName = tagName;
                                                    } else {
                                                        type3 = type5;
                                                        str17 = str26;
                                                    }
                                                }
                                                if (suspendDialogInfo == null && !TextUtils.isEmpty(dialogMessage)) {
                                                    suspendDialogInfo = new SuspendDialogInfo.Builder().setMessage(dialogMessage).build();
                                                }
                                                if (blockUninstall) {
                                                    i5 = userId;
                                                    parser2 = parser3;
                                                    name = name3;
                                                    try {
                                                        settings6.setBlockUninstallLPw(i5, name, true);
                                                    } catch (XmlPullParserException e22) {
                                                        e = e22;
                                                        settings = settings6;
                                                        str3 = str6;
                                                        str4 = str5;
                                                        str2 = str13;
                                                        i = 6;
                                                    } catch (IOException e23) {
                                                        e2 = e23;
                                                        settings2 = settings6;
                                                        str7 = str13;
                                                        settings2.mReadMessages.append(str6 + e2.toString());
                                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                                        Slog.wtf(str7, str5, e2);
                                                    }
                                                } else {
                                                    i5 = userId;
                                                    parser2 = parser3;
                                                    name = name3;
                                                }
                                                str12 = str9;
                                                userPackagesStateFile2 = userPackagesStateFile;
                                                i4 = 4;
                                                str14 = str17;
                                                equals = ceDataInode;
                                                oppoFreezeState = 2;
                                            } catch (XmlPullParserException e24) {
                                                e = e24;
                                                settings = this;
                                                str3 = str6;
                                                str4 = str5;
                                                str2 = str13;
                                                i = 6;
                                                settings.mReadMessages.append(str3 + e.toString());
                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                Slog.wtf(str2, str4, e);
                                            } catch (IOException e25) {
                                                e2 = e25;
                                                settings2 = this;
                                                str7 = str13;
                                                settings2.mReadMessages.append(str6 + e2.toString());
                                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                                Slog.wtf(str7, str5, e2);
                                            }
                                            try {
                                                ps.setUserState(userId, equals, enabled, name2, stopped, notLaunched, hidden2, distractionFlags, suspended, suspendingPackage2, suspendDialogInfo, suspendedAppExtras, suspendedLauncherExtras, instantApp, virtualPreload, enabledCaller, enabledComponents, disabledComponents, verifState, linkGeneration, installReason, harmfulAppWarning);
                                                ps.setOppoFreezeState(oppoFreezeState2, i5);
                                                ps.setOppoFreezeFlag(oppoFreezeFlag, i5);
                                                if ("com.coloros.lockassistant".equals(name)) {
                                                    ps.setEnabled(1, i5, enabledCaller);
                                                    ps.setInstalled(true, i5);
                                                    ps.setHidden(false, i5);
                                                    str15 = null;
                                                    ps.setDisabledComponents(null, i5);
                                                } else {
                                                    str15 = null;
                                                }
                                                settings5 = this;
                                                str16 = str13;
                                                parser = parser2;
                                                maxAppLinkGeneration2 = maxAppLinkGeneration;
                                                type2 = type3;
                                            } catch (XmlPullParserException e26) {
                                                e = e26;
                                                settings = this;
                                                str3 = str6;
                                                str4 = str5;
                                                str2 = str13;
                                                i = 6;
                                                settings.mReadMessages.append(str3 + e.toString());
                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                Slog.wtf(str2, str4, e);
                                            } catch (IOException e27) {
                                            }
                                        } catch (XmlPullParserException e28) {
                                            e = e28;
                                            settings = this;
                                            str3 = str6;
                                            str4 = str5;
                                            str2 = str13;
                                            i = 6;
                                            settings.mReadMessages.append(str3 + e.toString());
                                            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                            Slog.wtf(str2, str4, e);
                                        } catch (IOException e29) {
                                            e2 = e29;
                                            settings2 = this;
                                            str7 = str13;
                                            settings2.mReadMessages.append(str6 + e2.toString());
                                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                            Slog.wtf(str7, str5, e2);
                                        }
                                    } catch (XmlPullParserException e30) {
                                        e = e30;
                                        settings3 = this;
                                        str3 = str6;
                                        str4 = str5;
                                        str10 = str13;
                                        i = 6;
                                        settings = settings3;
                                        str2 = str10;
                                        settings.mReadMessages.append(str3 + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf(str2, str4, e);
                                    } catch (IOException e31) {
                                        e2 = e31;
                                        settings2 = this;
                                        str7 = str13;
                                        settings2.mReadMessages.append(str6 + e2.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                        Slog.wtf(str7, str5, e2);
                                    }
                                }
                            } catch (XmlPullParserException e32) {
                                e = e32;
                                settings3 = this;
                                str3 = str6;
                                str4 = str5;
                                str10 = str8;
                                i = 6;
                                settings = settings3;
                                str2 = str10;
                                settings.mReadMessages.append(str3 + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf(str2, str4, e);
                            } catch (IOException e33) {
                                e2 = e33;
                                settings2 = this;
                                str7 = str8;
                                settings2.mReadMessages.append(str6 + e2.toString());
                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                Slog.wtf(str7, str5, e2);
                            }
                        } else {
                            oppoFreezeState = i2;
                            type2 = type4;
                            outerDepth = outerDepth2;
                            str12 = str9;
                            userPackagesStateFile2 = userPackagesStateFile;
                            i4 = i7;
                            str14 = str20;
                            str13 = str8;
                            str15 = str25;
                            if (name2.equals("preferred-activities")) {
                                equals = this;
                                parser = parser3;
                                try {
                                    equals.readPreferredActivitiesLPw(parser, userId);
                                    str16 = str13;
                                    settings5 = equals;
                                } catch (XmlPullParserException e34) {
                                    e = e34;
                                    str2 = str13;
                                    str3 = str6;
                                    str4 = str5;
                                    i = 6;
                                    settings = equals;
                                    settings.mReadMessages.append(str3 + e.toString());
                                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                    Slog.wtf(str2, str4, e);
                                } catch (IOException e35) {
                                }
                            } else {
                                equals = this;
                                parser = parser3;
                                if (name2.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                    equals.readPersistentPreferredActivitiesLPw(parser, userId);
                                    str16 = str13;
                                    settings5 = equals;
                                } else if (name2.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                    equals.readCrossProfileIntentFiltersLPw(parser, userId);
                                    str16 = str13;
                                    settings5 = equals;
                                } else if (name2.equals(TAG_DEFAULT_APPS)) {
                                    equals.readDefaultAppsLPw(parser, userId);
                                    str16 = str13;
                                    settings5 = equals;
                                } else if (name2.equals(TAG_BLOCK_UNINSTALL_PACKAGES)) {
                                    equals.readBlockUninstallPackagesLPw(parser, userId);
                                    str16 = str13;
                                    settings5 = equals;
                                } else {
                                    name2 = str13;
                                    try {
                                        Slog.w((String) name2, "Unknown element under <stopped-packages>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                        settings5 = equals;
                                        str16 = name2;
                                    } catch (XmlPullParserException e36) {
                                        e = e36;
                                        str3 = str6;
                                        str4 = str5;
                                        i = 6;
                                        settings = equals;
                                        str2 = name2;
                                        settings.mReadMessages.append(str3 + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf(str2, str4, e);
                                    } catch (IOException e37) {
                                        e2 = e37;
                                        Settings settings7 = equals;
                                        String str27 = name2;
                                        settings2 = settings7;
                                        str7 = str27;
                                        settings2.mReadMessages.append(str6 + e2.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                        Slog.wtf(str7, str5, e2);
                                    }
                                }
                            }
                        }
                        parser3 = parser;
                        settings6 = settings5;
                        str8 = str16;
                        str25 = str15;
                        outerDepth2 = outerDepth;
                        str20 = str14;
                        i2 = oppoFreezeState;
                        userPackagesStateFile = userPackagesStateFile2;
                        str9 = str12;
                        i7 = i4;
                        z = true;
                    }
                }
                str11.close();
                settings4.mNextAppLinkGeneration.put(i3, maxAppLinkGeneration2 + 1);
            }
        } catch (XmlPullParserException e38) {
            e = e38;
            settings3 = settings6;
            str10 = str8;
            str3 = str6;
            str4 = str5;
            i = 6;
            settings = settings3;
            str2 = str10;
            settings.mReadMessages.append(str3 + e.toString());
            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
            Slog.wtf(str2, str4, e);
        } catch (IOException e39) {
        }
    }

    /* access modifiers changed from: package-private */
    public void setBlockUninstallLPw(int userId, String packageName, boolean blockUninstall) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (blockUninstall) {
            if (packages == null) {
                packages = new ArraySet<>();
                this.mBlockUninstallPackages.put(userId, packages);
            }
            packages.add(packageName);
        } else if (packages != null) {
            packages.remove(packageName);
            if (packages.isEmpty()) {
                this.mBlockUninstallPackages.remove(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getBlockUninstallLPr(int userId, String packageName) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages == null) {
            return false;
        }
        return packages.contains(packageName);
    }

    private ArraySet<String> readComponentsLPr(XmlPullParser parser) throws IOException, XmlPullParserException {
        String componentName;
        ArraySet<String> components = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return components;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM) || (componentName = parser.getAttributeValue(null, ATTR_NAME)) == null)) {
                if (components == null) {
                    components = new ArraySet<>();
                }
                components.add(componentName);
            }
        }
        return components;
    }

    /* access modifiers changed from: package-private */
    public void writePreferredActivitiesLPr(XmlSerializer serializer, int userId, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, "preferred-activities");
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            for (PreferredActivity pa : pir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                if (pa != null) {
                    pa.writeToXml(serializer, full);
                }
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, "preferred-activities");
    }

    /* access modifiers changed from: package-private */
    public void writePersistentPreferredActivitiesLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            for (PersistentPreferredActivity ppa : ppir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                ppa.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
    }

    /* access modifiers changed from: package-private */
    public void writeCrossProfileIntentFiltersLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            for (CrossProfileIntentFilter cpif : cpir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                cpif.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
    }

    /* access modifiers changed from: package-private */
    public void writeDomainVerificationsLPr(XmlSerializer serializer, IntentFilterVerificationInfo verificationInfo) throws IllegalArgumentException, IllegalStateException, IOException {
        if (verificationInfo != null && verificationInfo.getPackageName() != null) {
            serializer.startTag(null, TAG_DOMAIN_VERIFICATION);
            verificationInfo.writeToXml(serializer);
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.d(TAG, "Wrote domain verification for package: " + verificationInfo.getPackageName());
            }
            serializer.endTag(null, TAG_DOMAIN_VERIFICATION);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllDomainVerificationsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
        int N = this.mPackages.size();
        for (int i = 0; i < N; i++) {
            IntentFilterVerificationInfo ivi = this.mPackages.valueAt(i).getIntentFilterVerificationInfo();
            if (ivi != null) {
                writeDomainVerificationsLPr(serializer, ivi);
            }
        }
        serializer.endTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
    }

    /* access modifiers changed from: package-private */
    public void readAllDomainVerificationsLPr(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        this.mRestoredIntentFilterVerifications.clear();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    String pkgName = ivi.getPackageName();
                    PackageSetting ps = this.mPackages.get(pkgName);
                    if (ps != null) {
                        ps.setIntentFilterVerificationInfo(ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for existing app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    } else {
                        this.mRestoredIntentFilterVerifications.put(pkgName, ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for pending app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <all-intent-filter-verification>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDefaultAppsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_DEFAULT_APPS);
        String defaultBrowser = this.mDefaultBrowserApp.get(userId);
        if (!TextUtils.isEmpty(defaultBrowser)) {
            serializer.startTag(null, TAG_DEFAULT_BROWSER);
            serializer.attribute(null, "packageName", defaultBrowser);
            serializer.endTag(null, TAG_DEFAULT_BROWSER);
        }
        serializer.endTag(null, TAG_DEFAULT_APPS);
    }

    /* access modifiers changed from: package-private */
    public void writeBlockUninstallPackagesLPr(XmlSerializer serializer, int userId) throws IOException {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages != null) {
            serializer.startTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
            for (int i = 0; i < packages.size(); i++) {
                serializer.startTag(null, TAG_BLOCK_UNINSTALL);
                serializer.attribute(null, "packageName", packages.valueAt(i));
                serializer.endTag(null, TAG_BLOCK_UNINSTALL);
            }
            serializer.endTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0320  */
    /* JADX WARNING: Removed duplicated region for block: B:135:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void writePackageRestrictionsLPr(int userId) {
        PackageUserState userPackagesStateFile;
        String str;
        IOException e;
        FileOutputStream fstr;
        String str2;
        String str3;
        String str4 = TAG_SUSPENDED_DIALOG_INFO;
        String str5 = TAG_PACKAGE_RESTRICTIONS;
        long startTime = SystemClock.uptimeMillis();
        PackageUserState ustate = getUserPackagesStateFile(userId);
        File backupFile = getUserPackagesStateBackupFile(userId);
        new File(ustate.getParent()).mkdirs();
        if (ustate.exists()) {
            if (backupFile.exists()) {
                ustate.delete();
                Slog.w("PackageManager", "Preserving older stopped packages backup");
            } else if (!ustate.renameTo(backupFile)) {
                Slog.wtf("PackageManager", "Unable to backup user packages state file, current changes will be lost at reboot");
                return;
            }
        }
        try {
            FileOutputStream fstr2 = new FileOutputStream((File) ustate);
            BufferedOutputStream str6 = new BufferedOutputStream(fstr2);
            XmlSerializer serializer = new FastXmlSerializer();
            str = "PackageManager";
            BufferedOutputStream str7 = str6;
            try {
                serializer.setOutput(str7, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, str5);
                Iterator<PackageSetting> it = this.mPackages.values().iterator();
                while (it.hasNext()) {
                    PackageSetting pkg = it.next();
                    PackageUserState ustate2 = pkg.readUserState(userId);
                    userPackagesStateFile = ustate;
                    try {
                        serializer.startTag(null, TAG_PACKAGE);
                        serializer.attribute(null, ATTR_NAME, pkg.name);
                    } catch (IOException e2) {
                        e = e2;
                        Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                        if (userPackagesStateFile.exists() && !userPackagesStateFile.delete()) {
                            Log.i(str, "Failed to clean up mangled file: " + this.mStoppedPackagesFilename);
                            return;
                        }
                    }
                    try {
                        if (ustate2.ceDataInode != 0) {
                            XmlUtils.writeLongAttribute(serializer, ATTR_CE_DATA_INODE, ustate2.ceDataInode);
                        }
                        if (!ustate2.installed) {
                            serializer.attribute(null, ATTR_INSTALLED, TemperatureProvider.SWITCH_OFF);
                        }
                        if (ustate2.stopped) {
                            serializer.attribute(null, ATTR_STOPPED, TemperatureProvider.SWITCH_ON);
                        }
                        if (ustate2.notLaunched) {
                            serializer.attribute(null, ATTR_NOT_LAUNCHED, TemperatureProvider.SWITCH_ON);
                        }
                        if (ustate2.hidden) {
                            serializer.attribute(null, ATTR_HIDDEN, TemperatureProvider.SWITCH_ON);
                        }
                        if (ustate2.distractionFlags != 0) {
                            fstr = fstr2;
                            serializer.attribute(null, ATTR_DISTRACTION_FLAGS, Integer.toString(ustate2.distractionFlags));
                        } else {
                            fstr = fstr2;
                        }
                        if (ustate2.suspended) {
                            serializer.attribute(null, ATTR_SUSPENDED, TemperatureProvider.SWITCH_ON);
                            if (ustate2.suspendingPackage != null) {
                                serializer.attribute(null, ATTR_SUSPENDING_PACKAGE, ustate2.suspendingPackage);
                            }
                            if (ustate2.dialogInfo != null) {
                                serializer.startTag(null, str4);
                                ustate2.dialogInfo.saveToXml(serializer);
                                serializer.endTag(null, str4);
                            }
                            if (ustate2.suspendedAppExtras != null) {
                                str3 = str4;
                                serializer.startTag(null, TAG_SUSPENDED_APP_EXTRAS);
                                try {
                                    ustate2.suspendedAppExtras.saveToXml(serializer);
                                    str2 = str5;
                                } catch (XmlPullParserException xmle) {
                                    StringBuilder sb = new StringBuilder();
                                    str2 = str5;
                                    sb.append("Exception while trying to write suspendedAppExtras for ");
                                    sb.append(pkg);
                                    sb.append(". Will be lost on reboot");
                                    Slog.wtf(TAG, sb.toString(), xmle);
                                }
                                serializer.endTag(null, TAG_SUSPENDED_APP_EXTRAS);
                            } else {
                                str3 = str4;
                                str2 = str5;
                            }
                            if (ustate2.suspendedLauncherExtras != null) {
                                serializer.startTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                                try {
                                    ustate2.suspendedLauncherExtras.saveToXml(serializer);
                                } catch (XmlPullParserException xmle2) {
                                    Slog.wtf(TAG, "Exception while trying to write suspendedLauncherExtras for " + pkg + ". Will be lost on reboot", xmle2);
                                }
                                serializer.endTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                            }
                        } else {
                            str3 = str4;
                            str2 = str5;
                        }
                        if (ustate2.instantApp) {
                            serializer.attribute(null, ATTR_INSTANT_APP, TemperatureProvider.SWITCH_ON);
                        }
                        if (ustate2.virtualPreload) {
                            serializer.attribute(null, ATTR_VIRTUAL_PRELOAD, TemperatureProvider.SWITCH_ON);
                        }
                        if (ustate2.enabled != 0) {
                            serializer.attribute(null, ATTR_ENABLED, Integer.toString(ustate2.enabled));
                            if (ustate2.lastDisableAppCaller != null) {
                                serializer.attribute(null, ATTR_ENABLED_CALLER, ustate2.lastDisableAppCaller);
                            }
                        }
                        if (ustate2.oppoFreezeState != 0) {
                            serializer.attribute(null, ATTR_OPPO_FREEZED, Integer.toString(ustate2.oppoFreezeState));
                        }
                        if (!(ustate2.oppoFreezeFlag == 0 || ustate2.oppoFreezeState == 0)) {
                            serializer.attribute(null, ATTR_OPPO_FREEZED_FLAG, Integer.toString(ustate2.oppoFreezeFlag));
                        }
                        if (ustate2.domainVerificationStatus != 0) {
                            XmlUtils.writeIntAttribute(serializer, ATTR_DOMAIN_VERIFICATON_STATE, ustate2.domainVerificationStatus);
                        }
                        if (ustate2.appLinkGeneration != 0) {
                            XmlUtils.writeIntAttribute(serializer, ATTR_APP_LINK_GENERATION, ustate2.appLinkGeneration);
                        }
                        if (ustate2.installReason != 0) {
                            serializer.attribute(null, ATTR_INSTALL_REASON, Integer.toString(ustate2.installReason));
                        }
                        if (ustate2.harmfulAppWarning != null) {
                            serializer.attribute(null, ATTR_HARMFUL_APP_WARNING, ustate2.harmfulAppWarning);
                        }
                        if (!ArrayUtils.isEmpty(ustate2.enabledComponents)) {
                            serializer.startTag(null, TAG_ENABLED_COMPONENTS);
                            Iterator it2 = ustate2.enabledComponents.iterator();
                            while (it2.hasNext()) {
                                serializer.startTag(null, TAG_ITEM);
                                serializer.attribute(null, ATTR_NAME, (String) it2.next());
                                serializer.endTag(null, TAG_ITEM);
                            }
                            serializer.endTag(null, TAG_ENABLED_COMPONENTS);
                        }
                        if (!ArrayUtils.isEmpty(ustate2.disabledComponents)) {
                            serializer.startTag(null, TAG_DISABLED_COMPONENTS);
                            Iterator it3 = ustate2.disabledComponents.iterator();
                            while (it3.hasNext()) {
                                serializer.startTag(null, TAG_ITEM);
                                serializer.attribute(null, ATTR_NAME, (String) it3.next());
                                serializer.endTag(null, TAG_ITEM);
                            }
                            serializer.endTag(null, TAG_DISABLED_COMPONENTS);
                        }
                        serializer.endTag(null, TAG_PACKAGE);
                        backupFile = backupFile;
                        it = it;
                        ustate = userPackagesStateFile;
                        str7 = str7;
                        fstr2 = fstr;
                        str4 = str3;
                        str5 = str2;
                    } catch (IOException e3) {
                        e = e3;
                        Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                        if (userPackagesStateFile.exists()) {
                        }
                    }
                }
                writePreferredActivitiesLPr(serializer, userId, true);
                writePersistentPreferredActivitiesLPr(serializer, userId);
                writeCrossProfileIntentFiltersLPr(serializer, userId);
                writeDefaultAppsLPr(serializer, userId);
                writeBlockUninstallPackagesLPr(serializer, userId);
                serializer.endTag(null, str5);
                serializer.endDocument();
                str7.flush();
                FileUtils.sync(fstr2);
                str7.close();
                backupFile.delete();
                FileUtils.setPermissions(ustate.toString(), 432, -1, -1);
                EventLogTags.writeCommitSysConfigFile("package-user-" + userId, SystemClock.uptimeMillis() - startTime);
            } catch (IOException e4) {
                e = e4;
                userPackagesStateFile = ustate;
                Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                if (userPackagesStateFile.exists()) {
                }
            }
        } catch (IOException e5) {
            e = e5;
            userPackagesStateFile = ustate;
            str = "PackageManager";
            Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
            if (userPackagesStateFile.exists()) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readInstallPermissionsLPr(XmlPullParser parser, PermissionsState permissionsState) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            boolean granted = true;
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type != 3) {
                if (type != 4) {
                    if (parser.getName().equals(TAG_ITEM)) {
                        String name = parser.getAttributeValue(null, ATTR_NAME);
                        BasePermission bp = this.mPermissions.getPermission(name);
                        if (bp == null) {
                            Slog.w("PackageManager", "Unknown permission: " + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            String grantedStr = parser.getAttributeValue(null, ATTR_GRANTED);
                            int flags = 0;
                            if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                                granted = false;
                            }
                            String flagsStr = parser.getAttributeValue(null, ATTR_FLAGS);
                            if (flagsStr != null) {
                                flags = Integer.parseInt(flagsStr, 16);
                            }
                            if (granted) {
                                if (permissionsState.grantInstallPermission(bp) == -1) {
                                    Slog.w("PackageManager", "Permission already added: " + name);
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    permissionsState.updatePermissionFlags(bp, -1, 64511, flags);
                                }
                            } else if (permissionsState.revokeInstallPermission(bp) == -1) {
                                Slog.w("PackageManager", "Permission already added: " + name);
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                permissionsState.updatePermissionFlags(bp, -1, 64511, flags);
                            }
                        }
                    } else {
                        Slog.w("PackageManager", "Unknown element under <permissions>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionsLPr(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
        if (!permissionStates.isEmpty()) {
            serializer.startTag(null, TAG_PERMISSIONS);
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, TAG_ITEM);
                serializer.attribute(null, ATTR_NAME, permissionState.getName());
                serializer.attribute(null, ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, TAG_ITEM);
            }
            serializer.endTag(null, TAG_PERMISSIONS);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeChildPackagesLPw(XmlSerializer serializer, List<String> childPackageNames) throws IOException {
        if (childPackageNames != null) {
            int childCount = childPackageNames.size();
            for (int i = 0; i < childCount; i++) {
                serializer.startTag(null, TAG_CHILD_PACKAGE);
                serializer.attribute(null, ATTR_NAME, childPackageNames.get(i));
                serializer.endTag(null, TAG_CHILD_PACKAGE);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readUsesStaticLibLPw(XmlPullParser parser, PackageSetting outPs) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String libName = parser.getAttributeValue(null, ATTR_NAME);
                long libVersion = -1;
                try {
                    libVersion = Long.parseLong(parser.getAttributeValue(null, "version"));
                } catch (NumberFormatException e) {
                }
                if (libName != null && libVersion >= 0) {
                    outPs.usesStaticLibraries = (String[]) ArrayUtils.appendElement(String.class, outPs.usesStaticLibraries, libName);
                    outPs.usesStaticLibrariesVersions = ArrayUtils.appendLong(outPs.usesStaticLibrariesVersions, libVersion);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeUsesStaticLibLPw(XmlSerializer serializer, String[] usesStaticLibraries, long[] usesStaticLibraryVersions) throws IOException {
        if (!(ArrayUtils.isEmpty(usesStaticLibraries) || ArrayUtils.isEmpty(usesStaticLibraryVersions) || usesStaticLibraries.length != usesStaticLibraryVersions.length)) {
            int libCount = usesStaticLibraries.length;
            for (int i = 0; i < libCount; i++) {
                String libName = usesStaticLibraries[i];
                long libVersion = usesStaticLibraryVersions[i];
                serializer.startTag(null, TAG_USES_STATIC_LIB);
                serializer.attribute(null, ATTR_NAME, libName);
                serializer.attribute(null, "version", Long.toString(libVersion));
                serializer.endTag(null, TAG_USES_STATIC_LIB);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009e A[Catch:{ XmlPullParserException -> 0x0165, IOException -> 0x0133 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ac A[Catch:{ XmlPullParserException -> 0x0165, IOException -> 0x0133 }] */
    public void readStoppedLPw() {
        int type;
        FileInputStream str = null;
        int i = 4;
        if (this.mBackupStoppedPackagesFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupStoppedPackagesFilename);
                this.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up stopped packages file " + this.mStoppedPackagesFilename);
                    this.mStoppedPackagesFilename.delete();
                }
            } catch (IOException e) {
            }
        }
        if (str == null) {
            try {
                if (!this.mStoppedPackagesFilename.exists()) {
                    this.mReadMessages.append("No stopped packages file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                    for (PackageSetting pkg : this.mPackages.values()) {
                        pkg.setStopped(false, 0);
                        pkg.setNotLaunched(false, 0);
                    }
                    return;
                }
                str = new FileInputStream(this.mStoppedPackagesFilename);
            } catch (XmlPullParserException e2) {
                StringBuilder sb = this.mReadMessages;
                sb.append("Error reading: " + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                return;
            } catch (IOException e3) {
                StringBuilder sb2 = this.mReadMessages;
                sb2.append("Error reading: " + e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e3);
                return;
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, null);
        while (true) {
            type = parser.next();
            if (type == 2 || type == 1) {
                if (type == 2) {
                    this.mReadMessages.append("No start tag found in stopped packages file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                    return;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        str.close();
                    } else {
                        if (type2 != 3) {
                            if (type2 != i) {
                                if (parser.getName().equals(TAG_PACKAGE)) {
                                    String name = parser.getAttributeValue(null, ATTR_NAME);
                                    PackageSetting ps = this.mPackages.get(name);
                                    if (ps != null) {
                                        ps.setStopped(true, 0);
                                        if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(parser.getAttributeValue(null, ATTR_NOT_LAUNCHED))) {
                                            ps.setNotLaunched(true, 0);
                                        }
                                    } else {
                                        Slog.w("PackageManager", "No package known for stopped package " + name);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                        }
                        i = 4;
                    }
                }
                str.close();
                return;
            }
        }
        if (type == 2) {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x026d  */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void writeLPr() {
        String str;
        IOException e;
        long startTime = SystemClock.uptimeMillis();
        if (this.mSettingsFilename.exists()) {
            if (this.mBackupSettingsFilename.exists()) {
                this.mSettingsFilename.delete();
                Slog.w("PackageManager", "Preserving older settings backup");
            } else if (!this.mSettingsFilename.renameTo(this.mBackupSettingsFilename)) {
                Slog.wtf("PackageManager", "Unable to backup package manager settings,  current changes will be lost at reboot");
                return;
            }
        }
        this.mPastSignatures.clear();
        try {
            FileOutputStream fstr = new FileOutputStream(this.mSettingsFilename);
            BufferedOutputStream str2 = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            str = "PackageManager";
            try {
                serializer.setOutput(str2, StandardCharsets.UTF_8.name());
            } catch (IOException e2) {
                e = e2;
                Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
                if (this.mSettingsFilename.exists()) {
                    return;
                }
            }
            try {
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "packages");
                int i = 0;
                while (i < this.mVersion.size()) {
                    VersionInfo ver = this.mVersion.valueAt(i);
                    serializer.startTag(null, "version");
                    XmlUtils.writeStringAttribute(serializer, ATTR_VOLUME_UUID, this.mVersion.keyAt(i));
                    XmlUtils.writeIntAttribute(serializer, ATTR_SDK_VERSION, ver.sdkVersion);
                    XmlUtils.writeIntAttribute(serializer, ATTR_DATABASE_VERSION, ver.databaseVersion);
                    XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINT, ver.fingerprint);
                    serializer.endTag(null, "version");
                    i++;
                    fstr = fstr;
                }
                if (this.mVerifierDeviceIdentity != null) {
                    serializer.startTag(null, "verifier");
                    serializer.attribute(null, "device", this.mVerifierDeviceIdentity.toString());
                    serializer.endTag(null, "verifier");
                }
                if (this.mReadExternalStorageEnforced != null) {
                    serializer.startTag(null, TAG_READ_EXTERNAL_STORAGE);
                    serializer.attribute(null, ATTR_ENFORCEMENT, this.mReadExternalStorageEnforced.booleanValue() ? NoFocusWindow.HUNG_CONFIG_ENABLE : "0");
                    serializer.endTag(null, TAG_READ_EXTERNAL_STORAGE);
                }
                serializer.startTag(null, "permission-trees");
                this.mPermissions.writePermissionTrees(serializer);
                serializer.endTag(null, "permission-trees");
                serializer.startTag(null, "permissions");
                this.mPermissions.writePermissions(serializer);
                serializer.endTag(null, "permissions");
                for (PackageSetting pkg : this.mPackages.values()) {
                    writePackageLPr(serializer, pkg);
                }
                for (PackageSetting pkg2 : this.mDisabledSysPackages.values()) {
                    writeDisabledSysPackageLPr(serializer, pkg2);
                }
                for (SharedUserSetting usr : this.mSharedUsers.values()) {
                    serializer.startTag(null, TAG_SHARED_USER);
                    serializer.attribute(null, ATTR_NAME, usr.name);
                    serializer.attribute(null, "userId", Integer.toString(usr.userId));
                    usr.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
                    writePermissionsLPr(serializer, usr.getPermissionsState().getInstallPermissionStates());
                    serializer.endTag(null, TAG_SHARED_USER);
                }
                if (this.mRenamedPackages.size() > 0) {
                    for (Map.Entry<String, String> e3 : this.mRenamedPackages.entrySet()) {
                        serializer.startTag(null, "renamed-package");
                        serializer.attribute(null, "new", e3.getKey());
                        serializer.attribute(null, "old", e3.getValue());
                        serializer.endTag(null, "renamed-package");
                    }
                }
                int numIVIs = this.mRestoredIntentFilterVerifications.size();
                if (numIVIs > 0) {
                    if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Writing restored-ivi entries to packages.xml");
                    }
                    serializer.startTag(null, "restored-ivi");
                    for (int i2 = 0; i2 < numIVIs; i2++) {
                        writeDomainVerificationsLPr(serializer, this.mRestoredIntentFilterVerifications.valueAt(i2));
                    }
                    serializer.endTag(null, "restored-ivi");
                } else if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.i(TAG, "  no restored IVI entries to write");
                }
                this.mKeySetManagerService.writeKeySetManagerServiceLPr(serializer);
                serializer.endTag(null, "packages");
                serializer.endDocument();
                str2.flush();
                FileUtils.sync(fstr);
                str2.close();
                this.mBackupSettingsFilename.delete();
                FileUtils.setPermissions(this.mSettingsFilename.toString(), 432, -1, -1);
                notifyPackageXmlVerify(this.packagems);
                writeKernelMappingLPr();
                writePackageListLPr();
                writeAllUsersPackageRestrictionsLPr();
                writeAllRuntimePermissionsLPr();
                EventLogTags.writeCommitSysConfigFile(ATTR_PACKAGE, SystemClock.uptimeMillis() - startTime);
            } catch (IOException e4) {
                e = e4;
                Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
                if (this.mSettingsFilename.exists()) {
                }
            }
        } catch (IOException e5) {
            e = e5;
            str = "PackageManager";
            Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
            if (!(this.mSettingsFilename.exists() || this.mSettingsFilename.delete())) {
                Slog.wtf(str, "Failed to clean up mangled file: " + this.mSettingsFilename);
            }
        }
    }

    private void notifyPackageXmlVerify(PackageManagerService pms) {
        Runnable task = new Runnable() {
            /* class com.android.server.pm.Settings.AnonymousClass1 */

            public void run() {
                synchronized (Settings.this.mPackageVerify) {
                    String package_xml_sha1 = Settings.this.fileToSHA1(Settings.this.mSettingsFilename.toString());
                    if (package_xml_sha1 != null && package_xml_sha1.length() > 0) {
                        try {
                            SystemProperties.set("persist.sys.package.SHA1", package_xml_sha1);
                            if (PackageManagerService.DEBUG_PMS) {
                                Slog.i("PackageManager", "set persist.sys.package.SHA1 as " + package_xml_sha1);
                            }
                        } catch (RuntimeException ex) {
                            ex.fillInStackTrace();
                            Slog.e("PackageManager", "set persist.sys.package.SHA1 failed", ex);
                        }
                    }
                    boolean checkresult = Settings.oppoParsePackagesXml(Settings.this.mSettingsFilename);
                    if (checkresult) {
                        if (Settings.this.mBackupVerifiedFilename.exists()) {
                            Settings.this.mBackupVerifiedFilename.delete();
                        }
                        FileUtils.copyFile(Settings.this.mSettingsFilename, Settings.this.mBackupVerifiedFilename);
                        FileUtils.setPermissions(Settings.this.mBackupVerifiedFilename.toString(), 432, -1, -1);
                    }
                    if (PackageManagerService.DEBUG_PMS) {
                        Slog.d("PackageManager", "notifyPackageXmlVerify done. result=" + checkresult);
                    }
                }
            }
        };
        if (pms != null && pms.mHandler != null) {
            pms.mHandler.postDelayed(task, 0);
        }
    }

    public void checkPackageXml(PackageManagerService pms) {
        this.packagems = pms;
        boolean settingsExists = this.mSettingsFilename.exists();
        boolean packageVerifiedExists = this.mBackupVerifiedFilename.exists();
        boolean package_xml_fine = false;
        if (settingsExists) {
            try {
                String package_xml_sha1 = SystemProperties.get("persist.sys.package.SHA1", "");
                String package_xml_sha1_calc = fileToSHA1(this.mSettingsFilename.toString());
                package_xml_fine = (package_xml_sha1 == null || package_xml_sha1_calc == null || !package_xml_sha1.equals(package_xml_sha1_calc)) ? false : true;
            } catch (RuntimeException ex) {
                ex.fillInStackTrace();
                Slog.e("PackageManager", "get persist.sys.package.SHA1 failed", ex);
            }
        }
        Slog.i("PackageManager", "settingsExists=" + settingsExists + " packageVerifiedExists=" + packageVerifiedExists + " package_xml_fine=" + package_xml_fine);
        if (settingsExists && (package_xml_fine || oppoParsePackagesXml(this.mSettingsFilename))) {
            Slog.i("PackageManager", "packages.xml is ok");
        } else if (!packageVerifiedExists || !oppoParsePackagesXml(this.mBackupVerifiedFilename)) {
            if (settingsExists) {
                Slog.w("PackageManager", "delete mSettingsFilename xml");
                if (!this.mSettingsFilename.renameTo(this.mSettingsFilenameToBeDeleted)) {
                    Slog.wtf("PackageManager", "Unable to rename mSettingsFilenameToBeDeleted");
                }
            }
            if (packageVerifiedExists) {
                Slog.w("PackageManager", "delete mBackupVerifiedFilename xml");
                if (!this.mBackupVerifiedFilename.renameTo(this.mBackupVerifiedFilenameToBeDeleted)) {
                    Slog.wtf("PackageManager", "Unable to rename mBackupVerifiedFilenameToBeDeleted");
                }
            }
        } else {
            this.mSettingsFilename.delete();
            Slog.i("PackageManager", "delete damaged packages.xml and restore from packages-backup-verified.xml");
            if (!this.mBackupVerifiedFilename.renameTo(this.mSettingsFilename)) {
                Slog.wtf("PackageManager", "Unable to renameTo mSettingsFilename in checkPackageXml");
            }
            SystemProperties.set("sys.oppo.packagesxmldamaged", NoFocusWindow.HUNG_CONFIG_ENABLE);
        }
    }

    public static boolean oppoParsePackagesXml(File xmlFile) {
        if (!xmlFile.exists()) {
            return false;
        }
        InputStream input = null;
        try {
            input = new FileInputStream(xmlFile);
            SAXParserFactory.newInstance().newSAXParser().parse(input, new DefaultHandler());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Time time_lastmodified = new Time();
            time_lastmodified.set(xmlFile.lastModified());
            String errFile = "/data/packages-error_" + time_lastmodified.format2445() + ".xml";
            Log.i("PackageManager", "copyFile:" + xmlFile + " to " + errFile);
            FileUtils.copyFile(xmlFile, new File(errFile));
            Log.e("PackageManager", "parse " + xmlFile + " error!!!", ex);
            return false;
        } finally {
            IoUtils.closeQuietly(input);
        }
    }

    private void writeKernelRemoveUserLPr(int userId) {
        File file = this.mKernelMappingFilename;
        if (file != null) {
            writeIntToFile(new File(file, "remove_userid"), userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr() {
        File file = this.mKernelMappingFilename;
        if (file != null) {
            String[] known = file.list();
            ArraySet<String> knownSet = new ArraySet<>(known.length);
            for (String name : known) {
                knownSet.add(name);
            }
            for (PackageSetting ps : this.mPackages.values()) {
                knownSet.remove(ps.name);
                writeKernelMappingLPr(ps);
            }
            for (int i = 0; i < knownSet.size(); i++) {
                String name2 = knownSet.valueAt(i);
                this.mKernelMapping.remove(name2);
                new File(this.mKernelMappingFilename, name2).delete();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr(PackageSetting ps) {
        if (this.mKernelMappingFilename != null && ps != null && ps.name != null) {
            writeKernelMappingLPr(ps.name, ps.appId, ps.getNotInstalledUserIds());
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr(String name, int appId, int[] excludedUserIds) {
        KernelPackageState cur = this.mKernelMapping.get(name);
        boolean userIdsChanged = false;
        boolean firstTime = cur == null;
        if (firstTime || !Arrays.equals(excludedUserIds, cur.excludedUserIds)) {
            userIdsChanged = true;
        }
        File dir = new File(this.mKernelMappingFilename, name);
        if (firstTime) {
            dir.mkdir();
            cur = new KernelPackageState();
            this.mKernelMapping.put(name, cur);
        }
        if (cur.appId != appId) {
            writeIntToFile(new File(dir, "appid"), appId);
        }
        if (userIdsChanged) {
            for (int i = 0; i < excludedUserIds.length; i++) {
                if (cur.excludedUserIds == null || !ArrayUtils.contains(cur.excludedUserIds, excludedUserIds[i])) {
                    writeIntToFile(new File(dir, "excluded_userids"), excludedUserIds[i]);
                }
            }
            if (cur.excludedUserIds != null) {
                for (int i2 = 0; i2 < cur.excludedUserIds.length; i2++) {
                    if (!ArrayUtils.contains(excludedUserIds, cur.excludedUserIds[i2])) {
                        writeIntToFile(new File(dir, "clear_userid"), cur.excludedUserIds[i2]);
                    }
                }
            }
            cur.excludedUserIds = excludedUserIds;
        }
    }

    private void writeIntToFile(File file, int value) {
        try {
            FileUtils.bytesToFile(file.getAbsolutePath(), Integer.toString(value).getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            Slog.w(TAG, "Couldn't write " + value + " to " + file.getAbsolutePath());
        }
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr() {
        writePackageListLPr(-1);
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr(int creatingUserId) {
        String ctx = SELinux.fileSelabelLookup(this.mPackageListFilename.getAbsolutePath());
        if (ctx == null) {
            Slog.wtf(TAG, "Failed to get SELinux context for " + this.mPackageListFilename.getAbsolutePath());
        }
        if (!SELinux.setFSCreateContext(ctx)) {
            Slog.wtf(TAG, "Failed to set packages.list SELinux context");
        }
        try {
            writePackageListLPrInternal(creatingUserId);
        } finally {
            SELinux.setFSCreateContext((String) null);
        }
    }

    private void writePackageListLPrInternal(int creatingUserId) {
        Exception e;
        List<UserInfo> users;
        String str;
        String str2 = StringUtils.SPACE;
        List<UserInfo> users2 = getUsers(UserManagerService.getInstance(), true);
        int[] userIds = new int[users2.size()];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = users2.get(i).id;
        }
        if (creatingUserId != -1) {
            userIds = ArrayUtils.appendInt(userIds, creatingUserId);
        }
        JournaledFile journal = new JournaledFile(this.mPackageListFilename, new File(this.mPackageListFilename.getAbsolutePath() + ".tmp"));
        BufferedWriter writer = null;
        try {
            FileOutputStream fstr = new FileOutputStream(journal.chooseForWrite());
            writer = new BufferedWriter(new OutputStreamWriter(fstr, Charset.defaultCharset()));
            FileUtils.setPermissions(fstr.getFD(), 416, 1000, 1032);
            StringBuilder sb = new StringBuilder();
            for (PackageSetting pkg : this.mPackages.values()) {
                if (pkg.pkg == null || pkg.pkg.applicationInfo == null) {
                    users = users2;
                    str = str2;
                } else if (pkg.pkg.applicationInfo.dataDir == null) {
                    users = users2;
                    str = str2;
                } else {
                    ApplicationInfo ai = pkg.pkg.applicationInfo;
                    String dataPath = ai.dataDir;
                    boolean isDebug = (ai.flags & 2) != 0;
                    int[] gids = pkg.getPermissionsState().computeGids(userIds);
                    try {
                        if (dataPath.indexOf(32) >= 0) {
                            users2 = users2;
                        } else {
                            sb.setLength(0);
                            sb.append(ai.packageName);
                            sb.append(str2);
                            sb.append(ai.uid);
                            sb.append(isDebug ? " 1 " : " 0 ");
                            sb.append(dataPath);
                            sb.append(str2);
                            sb.append(ai.seInfo);
                            sb.append(str2);
                            if (gids == null || gids.length <= 0) {
                                sb.append("none");
                            } else {
                                sb.append(gids[0]);
                                int i2 = 1;
                                while (i2 < gids.length) {
                                    sb.append(",");
                                    sb.append(gids[i2]);
                                    i2++;
                                    isDebug = isDebug;
                                }
                            }
                            sb.append(str2);
                            sb.append(ai.isProfileableByShell() ? NoFocusWindow.HUNG_CONFIG_ENABLE : "0");
                            sb.append(str2);
                            sb.append(String.valueOf(ai.longVersionCode));
                            sb.append(StringUtils.LF);
                            writer.append((CharSequence) sb);
                            str2 = str2;
                            users2 = users2;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        Slog.wtf(TAG, "Failed to write packages.list", e);
                        IoUtils.closeQuietly(writer);
                        journal.rollback();
                    }
                }
                if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg.name)) {
                    Slog.w(TAG, "Skipping " + pkg + " due to missing metadata");
                    str2 = str;
                    users2 = users;
                } else {
                    str2 = str;
                    users2 = users;
                }
            }
            writer.flush();
            FileUtils.sync(fstr);
            writer.close();
            journal.commit();
        } catch (Exception e3) {
            e = e3;
            Slog.wtf(TAG, "Failed to write packages.list", e);
            IoUtils.closeQuietly(writer);
            journal.rollback();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDisabledSysPackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, "updated-package");
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        if (pkg.sharedUser == null) {
            writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        }
        serializer.endTag(null, "updated-package");
    }

    /* access modifiers changed from: package-private */
    public void writePackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, ATTR_PACKAGE);
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        serializer.attribute(null, "publicFlags", Integer.toString(pkg.pkgFlags));
        serializer.attribute(null, "privateFlags", Integer.toString(pkg.pkgPrivateFlags));
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.uidError) {
            serializer.attribute(null, "uidError", TemperatureProvider.SWITCH_ON);
        }
        if (pkg.installerPackageName != null) {
            serializer.attribute(null, "installer", pkg.installerPackageName);
        }
        if (pkg.isOrphaned) {
            serializer.attribute(null, "isOrphaned", TemperatureProvider.SWITCH_ON);
        }
        if (pkg.volumeUuid != null) {
            serializer.attribute(null, ATTR_VOLUME_UUID, pkg.volumeUuid);
        }
        if (pkg.categoryHint != -1) {
            serializer.attribute(null, "categoryHint", Integer.toString(pkg.categoryHint));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        if (pkg.updateAvailable) {
            serializer.attribute(null, "updateAvailable", TemperatureProvider.SWITCH_ON);
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        pkg.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
        writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        writeSigningKeySetLPr(serializer, pkg.keySetData);
        writeUpgradeKeySetsLPr(serializer, pkg.keySetData);
        writeKeySetAliasesLPr(serializer, pkg.keySetData);
        writeDomainVerificationsLPr(serializer, pkg.verificationInfo);
        serializer.endTag(null, ATTR_PACKAGE);
    }

    /* access modifiers changed from: package-private */
    public void writeSigningKeySetLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        serializer.startTag(null, "proper-signing-keyset");
        serializer.attribute(null, "identifier", Long.toString(data.getProperSigningKeySet()));
        serializer.endTag(null, "proper-signing-keyset");
    }

    /* access modifiers changed from: package-private */
    public void writeUpgradeKeySetsLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        if (data.isUsingUpgradeKeySets()) {
            long[] upgradeKeySets = data.getUpgradeKeySets();
            for (long id : upgradeKeySets) {
                serializer.startTag(null, "upgrade-keyset");
                serializer.attribute(null, "identifier", Long.toString(id));
                serializer.endTag(null, "upgrade-keyset");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKeySetAliasesLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        for (Map.Entry<String, Long> e : data.getAliases().entrySet()) {
            serializer.startTag(null, "defined-keyset");
            serializer.attribute(null, "alias", e.getKey());
            serializer.attribute(null, "identifier", Long.toString(e.getValue().longValue()));
            serializer.endTag(null, "defined-keyset");
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionLPr(XmlSerializer serializer, BasePermission bp) throws IOException {
        bp.writeLPr(serializer);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00b3 A[Catch:{ XmlPullParserException -> 0x02d8, IOException -> 0x02a9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00c2 A[Catch:{ XmlPullParserException -> 0x02d8, IOException -> 0x02a9 }] */
    public boolean readLPw(List<UserInfo> users) {
        int type;
        FileInputStream str = null;
        int i = 4;
        if (this.mBackupSettingsFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupSettingsFilename);
                this.mReadMessages.append("Reading from backup settings file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup settings file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up settings file " + this.mSettingsFilename);
                    this.mSettingsFilename.delete();
                }
            } catch (IOException e) {
            }
        }
        this.mPendingPackages.clear();
        this.mPastSignatures.clear();
        this.mKeySetRefs.clear();
        this.mInstallerPackages.clear();
        int i2 = 1;
        int i3 = 0;
        if (str == null) {
            try {
                if (!this.mSettingsFilename.exists()) {
                    this.mReadMessages.append("No settings file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No settings file; creating initial state");
                    findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL).forceCurrent();
                    findOrCreateVersion("primary_physical").forceCurrent();
                    return false;
                }
                str = new FileInputStream(this.mSettingsFilename);
            } catch (XmlPullParserException e2) {
                this.mReadMessages.append("Error reading: " + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager settings", e2);
            } catch (IOException e3) {
                this.mReadMessages.append("Error reading: " + e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.wtf("PackageManager", "Error reading package manager settings", e3);
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, StandardCharsets.UTF_8.name());
        while (true) {
            type = parser.next();
            if (type == 2 || type == 1) {
                if (type == 2) {
                    this.mReadMessages.append("No start tag found in settings file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager settings");
                    Slog.wtf("PackageManager", "No start tag found in package manager settings");
                    return false;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == i2 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        str.close();
                    } else {
                        if (type2 != 3) {
                            if (type2 != i) {
                                String tagName = parser.getName();
                                if (tagName.equals(ATTR_PACKAGE)) {
                                    readPackageLPw(parser);
                                } else if (tagName.equals("permissions")) {
                                    this.mPermissions.readPermissions(parser);
                                } else if (tagName.equals("permission-trees")) {
                                    this.mPermissions.readPermissionTrees(parser);
                                } else if (tagName.equals(TAG_SHARED_USER)) {
                                    readSharedUserLPw(parser);
                                } else if (!tagName.equals("preferred-packages")) {
                                    if (tagName.equals("preferred-activities")) {
                                        readPreferredActivitiesLPw(parser, i3);
                                    } else if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                        readPersistentPreferredActivitiesLPw(parser, i3);
                                    } else if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                        readCrossProfileIntentFiltersLPw(parser, i3);
                                    } else if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                                        readDefaultAppsLPw(parser, i3);
                                    } else if (tagName.equals("updated-package")) {
                                        readDisabledSysPackageLPw(parser);
                                    } else if (tagName.equals("renamed-package")) {
                                        String nname = parser.getAttributeValue(null, "new");
                                        String oname = parser.getAttributeValue(null, "old");
                                        if (!(nname == null || oname == null)) {
                                            this.mRenamedPackages.put(nname, oname);
                                        }
                                    } else if (tagName.equals("restored-ivi")) {
                                        readRestoredIntentFilterVerifications(parser);
                                    } else if (tagName.equals("last-platform-version")) {
                                        VersionInfo internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                        VersionInfo external = findOrCreateVersion("primary_physical");
                                        internal.sdkVersion = XmlUtils.readIntAttribute(parser, "internal", i3);
                                        external.sdkVersion = XmlUtils.readIntAttribute(parser, "external", i3);
                                        String readStringAttribute = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                        external.fingerprint = readStringAttribute;
                                        internal.fingerprint = readStringAttribute;
                                    } else if (tagName.equals("database-version")) {
                                        VersionInfo internal2 = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                        VersionInfo external2 = findOrCreateVersion("primary_physical");
                                        internal2.databaseVersion = XmlUtils.readIntAttribute(parser, "internal", i3);
                                        external2.databaseVersion = XmlUtils.readIntAttribute(parser, "external", i3);
                                    } else if (tagName.equals("verifier")) {
                                        try {
                                            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.parse(parser.getAttributeValue(null, "device"));
                                        } catch (IllegalArgumentException e4) {
                                            Slog.w("PackageManager", "Discard invalid verifier device id: " + e4.getMessage());
                                        }
                                    } else if (TAG_READ_EXTERNAL_STORAGE.equals(tagName)) {
                                        this.mReadExternalStorageEnforced = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(parser.getAttributeValue(null, ATTR_ENFORCEMENT)) ? Boolean.TRUE : Boolean.FALSE;
                                    } else if (tagName.equals("keyset-settings")) {
                                        this.mKeySetManagerService.readKeySetsLPw(parser, this.mKeySetRefs);
                                    } else if ("version".equals(tagName)) {
                                        VersionInfo ver = findOrCreateVersion(XmlUtils.readStringAttribute(parser, ATTR_VOLUME_UUID));
                                        ver.sdkVersion = XmlUtils.readIntAttribute(parser, ATTR_SDK_VERSION);
                                        ver.databaseVersion = XmlUtils.readIntAttribute(parser, ATTR_DATABASE_VERSION);
                                        ver.fingerprint = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                    } else {
                                        Slog.w("PackageManager", "Unknown element under <packages>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                        i = 4;
                        i2 = 1;
                        i3 = 0;
                    }
                }
                str.close();
                int N = this.mPendingPackages.size();
                for (int i4 = 0; i4 < N; i4++) {
                    PackageSetting p = this.mPendingPackages.get(i4);
                    int sharedUserId = p.getSharedUserId();
                    Object idObj = getSettingLPr(sharedUserId);
                    if (idObj instanceof SharedUserSetting) {
                        SharedUserSetting sharedUser = (SharedUserSetting) idObj;
                        p.sharedUser = sharedUser;
                        p.appId = sharedUser.userId;
                        addPackageSettingLPw(p, sharedUser);
                    } else if (idObj != null) {
                        String msg = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not a shared uid\n";
                        this.mReadMessages.append(msg);
                        PackageManagerService.reportSettingsProblem(6, msg);
                    } else {
                        String msg2 = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not defined\n";
                        this.mReadMessages.append(msg2);
                        PackageManagerService.reportSettingsProblem(6, msg2);
                    }
                }
                this.mPendingPackages.clear();
                if (this.mBackupStoppedPackagesFilename.exists() || this.mStoppedPackagesFilename.exists()) {
                    readStoppedLPw();
                    this.mBackupStoppedPackagesFilename.delete();
                    this.mStoppedPackagesFilename.delete();
                    writePackageRestrictionsLPr(0);
                } else {
                    for (UserInfo user : users) {
                        readPackageRestrictionsLPr(user.id);
                    }
                }
                for (UserInfo user2 : users) {
                    this.mRuntimePermissionsPersistence.readStateForUserSyncLPr(user2.id);
                }
                for (PackageSetting disabledPs : this.mDisabledSysPackages.values()) {
                    Object id = getSettingLPr(disabledPs.appId);
                    if (id != null && (id instanceof SharedUserSetting)) {
                        disabledPs.sharedUser = (SharedUserSetting) id;
                    }
                }
                this.mReadMessages.append("Read completed successfully: " + this.mPackages.size() + " packages, " + this.mSharedUsers.size() + " shared uids\n");
                writeKernelMappingLPr();
                return true;
            }
        }
        if (type == 2) {
        }
    }

    /* JADX INFO: Multiple debug info for r16v10 'pmInternal'  android.content.pm.PackageManagerInternal: [D('pmInternal' android.content.pm.PackageManagerInternal), D('type' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01cf A[SYNTHETIC] */
    public void applyDefaultPreferredAppsLPw(int userId) {
        PackageManagerInternal pmInternal;
        Throwable th;
        XmlPullParserException e;
        IOException e2;
        int type;
        PackageManagerInternal pmInternal2 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        for (PackageSetting ps : this.mPackages.values()) {
            if (!((1 & ps.pkgFlags) == 0 || ps.pkg == null || ps.pkg.preferredActivityFilters == null)) {
                ArrayList<PackageParser.ActivityIntentInfo> intents = ps.pkg.preferredActivityFilters;
                for (int i = 0; i < intents.size(); i++) {
                    PackageParser.ActivityIntentInfo aii = intents.get(i);
                    applyDefaultPreferredActivityLPw(pmInternal2, aii, new ComponentName(ps.name, aii.activity.className), userId);
                }
            }
        }
        File preferredDir = new File(Environment.getRootDirectory(), "etc/preferred-apps");
        if (!preferredDir.exists()) {
            return;
        }
        if (preferredDir.isDirectory()) {
            if (!preferredDir.canRead()) {
                Slog.w(TAG, "Directory " + preferredDir + " cannot be read");
                return;
            }
            File[] listFiles = preferredDir.listFiles();
            int length = listFiles.length;
            int i2 = 0;
            while (i2 < length) {
                File f = listFiles[i2];
                if (!f.getPath().endsWith(".xml")) {
                    Slog.i(TAG, "Non-xml file " + f + " in " + preferredDir + " directory, ignoring");
                    pmInternal = pmInternal2;
                } else if (!f.canRead()) {
                    Slog.w(TAG, "Preferred apps file " + f + " cannot be read");
                    pmInternal = pmInternal2;
                } else {
                    if (PackageManagerService.DEBUG_PREFERRED) {
                        Log.d(TAG, "Reading default preferred " + f);
                    }
                    InputStream str = null;
                    try {
                        str = new BufferedInputStream(new FileInputStream(f));
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(str, null);
                        while (true) {
                            int type2 = parser.next();
                            if (type2 == 2) {
                                type = type2;
                                pmInternal = pmInternal2;
                                break;
                            }
                            type = type2;
                            pmInternal = pmInternal2;
                            if (type == 1) {
                                break;
                            }
                            pmInternal2 = pmInternal;
                        }
                        if (type != 2) {
                            try {
                                Slog.w(TAG, "Preferred apps file " + f + " does not have start tag");
                                try {
                                    str.close();
                                } catch (IOException e3) {
                                }
                            } catch (XmlPullParserException e4) {
                                e = e4;
                            } catch (IOException e5) {
                                e2 = e5;
                                try {
                                    Slog.w(TAG, "Error reading apps file " + f, e2);
                                    if (str == null) {
                                    }
                                    i2++;
                                    pmInternal2 = pmInternal;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                        } else if (!"preferred-activities".equals(parser.getName())) {
                            Slog.w(TAG, "Preferred apps file " + f + " does not start with 'preferred-activities'");
                            str.close();
                        } else {
                            readDefaultPreferredActivitiesLPw(parser, userId);
                            try {
                                str.close();
                            } catch (IOException e6) {
                            }
                        }
                    } catch (XmlPullParserException e7) {
                        e = e7;
                        pmInternal = pmInternal2;
                        Slog.w(TAG, "Error reading apps file " + f, e);
                        if (str != null) {
                            str.close();
                        }
                        i2++;
                        pmInternal2 = pmInternal;
                    } catch (IOException e8) {
                        e2 = e8;
                        pmInternal = pmInternal2;
                        Slog.w(TAG, "Error reading apps file " + f, e2);
                        if (str == null) {
                            str.close();
                        }
                        i2++;
                        pmInternal2 = pmInternal;
                    } catch (Throwable th3) {
                        th = th3;
                        if (str != null) {
                            try {
                                str.close();
                            } catch (IOException e9) {
                            }
                        }
                        throw th;
                    }
                }
                i2++;
                pmInternal2 = pmInternal;
            }
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerInternal pmInternal, IntentFilter tmpPa, ComponentName cn, int userId) {
        int ischeme;
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Processing preferred:");
            tmpPa.dump(new LogPrinter(3, TAG), "  ");
        }
        Intent intent = new Intent();
        intent.setAction(tmpPa.getAction(0));
        int flags = 786432;
        for (int i = 0; i < tmpPa.countCategories(); i++) {
            String cat = tmpPa.getCategory(i);
            if (cat.equals("android.intent.category.DEFAULT")) {
                flags |= 65536;
            } else {
                intent.addCategory(cat);
            }
        }
        int dataSchemesCount = tmpPa.countDataSchemes();
        boolean hasSchemes = false;
        boolean doNonData = true;
        int ischeme2 = 0;
        while (ischeme2 < dataSchemesCount) {
            String scheme = tmpPa.getDataScheme(ischeme2);
            if (scheme != null && !scheme.isEmpty()) {
                hasSchemes = true;
            }
            boolean doScheme = true;
            int issp = 0;
            for (int dataSchemeSpecificPartsCount = tmpPa.countDataSchemeSpecificParts(); issp < dataSchemeSpecificPartsCount; dataSchemeSpecificPartsCount = dataSchemeSpecificPartsCount) {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(scheme);
                PatternMatcher ssp = tmpPa.getDataSchemeSpecificPart(issp);
                builder.opaquePart(ssp.getPath());
                Intent finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent, flags, cn, scheme, ssp, null, null, userId);
                doScheme = false;
                issp++;
                scheme = scheme;
                dataSchemesCount = dataSchemesCount;
            }
            int dataAuthoritiesCount = tmpPa.countDataAuthorities();
            int iauth = 0;
            while (iauth < dataAuthoritiesCount) {
                IntentFilter.AuthorityEntry auth = tmpPa.getDataAuthority(iauth);
                int dataPathsCount = tmpPa.countDataPaths();
                int ipath = 0;
                boolean doScheme2 = doScheme;
                boolean doAuth = true;
                while (ipath < dataPathsCount) {
                    Uri.Builder builder2 = new Uri.Builder();
                    builder2.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder2.authority(auth.getHost());
                    }
                    PatternMatcher path = tmpPa.getDataPath(ipath);
                    builder2.path(path.getPath());
                    Intent finalIntent2 = new Intent(intent);
                    finalIntent2.setData(builder2.build());
                    applyDefaultPreferredActivityLPw(pmInternal, finalIntent2, flags, cn, scheme, null, auth, path, userId);
                    doScheme2 = false;
                    doAuth = false;
                    ipath++;
                    dataPathsCount = dataPathsCount;
                    iauth = iauth;
                    dataAuthoritiesCount = dataAuthoritiesCount;
                }
                if (doAuth) {
                    Uri.Builder builder3 = new Uri.Builder();
                    builder3.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder3.authority(auth.getHost());
                    }
                    Intent finalIntent3 = new Intent(intent);
                    finalIntent3.setData(builder3.build());
                    applyDefaultPreferredActivityLPw(pmInternal, finalIntent3, flags, cn, scheme, null, auth, null, userId);
                    doScheme = false;
                } else {
                    doScheme = doScheme2;
                }
                iauth++;
                dataAuthoritiesCount = dataAuthoritiesCount;
            }
            if (doScheme) {
                Uri.Builder builder4 = new Uri.Builder();
                builder4.scheme(scheme);
                Intent finalIntent4 = new Intent(intent);
                finalIntent4.setData(builder4.build());
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent4, flags, cn, scheme, null, null, null, userId);
            }
            doNonData = false;
            ischeme2++;
            dataSchemesCount = dataSchemesCount;
        }
        boolean doNonData2 = doNonData;
        for (int idata = 0; idata < tmpPa.countDataTypes(); idata++) {
            String mimeType = tmpPa.getDataType(idata);
            if (hasSchemes) {
                Uri.Builder builder5 = new Uri.Builder();
                int ischeme3 = 0;
                while (ischeme3 < tmpPa.countDataSchemes()) {
                    String scheme2 = tmpPa.getDataScheme(ischeme3);
                    if (scheme2 == null || scheme2.isEmpty()) {
                        ischeme = ischeme3;
                    } else {
                        Intent finalIntent5 = new Intent(intent);
                        builder5.scheme(scheme2);
                        finalIntent5.setDataAndType(builder5.build(), mimeType);
                        ischeme = ischeme3;
                        applyDefaultPreferredActivityLPw(pmInternal, finalIntent5, flags, cn, scheme2, null, null, null, userId);
                    }
                    ischeme3 = ischeme + 1;
                }
            } else {
                Intent finalIntent6 = new Intent(intent);
                finalIntent6.setType(mimeType);
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent6, flags, cn, null, null, null, null, userId);
            }
            doNonData2 = false;
        }
        if (doNonData2) {
            applyDefaultPreferredActivityLPw(pmInternal, intent, flags, cn, null, null, null, null, userId);
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerInternal pmInternal, Intent intent, int flags, ComponentName cn, String scheme, PatternMatcher ssp, IntentFilter.AuthorityEntry auth, PatternMatcher path, int userId) {
        ComponentName haveNonSys;
        List<ResolveInfo> ri = pmInternal.queryIntentActivities(intent, intent.getType(), flags, Binder.getCallingUid(), 0);
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Queried " + intent + " results: " + ri);
        }
        int numMatches = ri == null ? 0 : ri.size();
        if (numMatches <= 1) {
            Slog.w(TAG, "No potential matches found for " + intent + " while setting preferred " + cn.flattenToShortString());
            return;
        }
        boolean haveAct = false;
        ComponentName haveNonSys2 = null;
        ComponentName[] set = new ComponentName[ri.size()];
        int i = 0;
        int systemMatch = 0;
        while (true) {
            if (i >= numMatches) {
                break;
            }
            ActivityInfo ai = ri.get(i).activityInfo;
            set[i] = new ComponentName(ai.packageName, ai.name);
            if ((ai.applicationInfo.flags & 1) != 0) {
                haveNonSys = haveNonSys2;
                if (cn.getPackageName().equals(ai.packageName) && cn.getClassName().equals(ai.name)) {
                    if (PackageManagerService.DEBUG_PREFERRED) {
                        Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": default!");
                    }
                    haveAct = true;
                    systemMatch = ri.get(i).match;
                } else if (PackageManagerService.DEBUG_PREFERRED) {
                    Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": skipped");
                }
            } else if (ri.get(i).match >= 0) {
                if (PackageManagerService.DEBUG_PREFERRED) {
                    Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": non-system!");
                }
                haveNonSys2 = set[i];
            } else {
                haveNonSys = haveNonSys2;
            }
            i++;
            haveNonSys2 = haveNonSys;
            numMatches = numMatches;
        }
        if (haveNonSys2 != null && 0 < systemMatch) {
            haveNonSys2 = null;
        }
        if (haveAct && haveNonSys2 == null) {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            if (intent.getCategories() != null) {
                for (String cat : intent.getCategories()) {
                    filter.addCategory(cat);
                }
            }
            if ((flags & 65536) != 0) {
                filter.addCategory("android.intent.category.DEFAULT");
            }
            if (scheme != null) {
                filter.addDataScheme(scheme);
            }
            if (ssp != null) {
                filter.addDataSchemeSpecificPart(ssp.getPath(), ssp.getType());
            }
            if (auth != null) {
                filter.addDataAuthority(auth);
            }
            if (path != null) {
                filter.addDataPath(path);
            }
            if (intent.getType() != null) {
                try {
                    filter.addDataType(intent.getType());
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    Slog.w(TAG, "Malformed mimetype " + intent.getType() + " for " + cn);
                }
            }
            editPreferredActivitiesLPw(userId).addFilter(new PreferredActivity(filter, systemMatch, set, cn, true));
        } else if (haveNonSys2 == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No component ");
            sb.append(cn.flattenToShortString());
            sb.append(" found setting preferred ");
            sb.append(intent);
            sb.append("; possible matches are ");
            for (int i2 = 0; i2 < set.length; i2++) {
                if (i2 > 0) {
                    sb.append(", ");
                }
                sb.append(set[i2].flattenToShortString());
            }
            Slog.w(TAG, sb.toString());
        } else {
            Slog.i(TAG, "Not setting preferred " + intent + "; found third party match " + haveNonSys2.flattenToShortString());
        }
    }

    private void readDefaultPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        PackageManagerInternal pmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    PreferredActivity tmpPa = new PreferredActivity(parser);
                    if (tmpPa.mPref.getParseError() == null) {
                        applyDefaultPreferredActivityLPw(pmInternal, tmpPa, tmpPa.mPref.mComponent, userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + tmpPa.mPref.getParseError() + " at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00bf A[SYNTHETIC, Splitter:B:21:0x00bf] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00e4 A[SYNTHETIC, Splitter:B:31:0x00e4] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f5 A[SYNTHETIC, Splitter:B:36:0x00f5] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0107  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0113  */
    private void readDisabledSysPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        String primaryCpuAbiStr;
        String resourcePathStr;
        long versionCode;
        int pkgPrivateFlags;
        PackageSetting ps;
        String timeStampStr;
        String timeStampStr2;
        String timeStampStr3;
        int type;
        String name = parser.getAttributeValue(null, ATTR_NAME);
        String realName = parser.getAttributeValue(null, "realName");
        String codePathStr = parser.getAttributeValue(null, "codePath");
        String resourcePathStr2 = parser.getAttributeValue(null, "resourcePath");
        String legacyCpuAbiStr = parser.getAttributeValue(null, "requiredCpuAbi");
        String legacyNativeLibraryPathStr = parser.getAttributeValue(null, "nativeLibraryPath");
        String parentPackageName = parser.getAttributeValue(null, "parentPackageName");
        String primaryCpuAbiStr2 = parser.getAttributeValue(null, "primaryCpuAbi");
        String secondaryCpuAbiStr = parser.getAttributeValue(null, "secondaryCpuAbi");
        String cpuAbiOverrideStr = parser.getAttributeValue(null, "cpuAbiOverride");
        if (primaryCpuAbiStr2 != null || legacyCpuAbiStr == null) {
            primaryCpuAbiStr = primaryCpuAbiStr2;
        } else {
            primaryCpuAbiStr = legacyCpuAbiStr;
        }
        if (resourcePathStr2 == null) {
            resourcePathStr = codePathStr;
        } else {
            resourcePathStr = resourcePathStr2;
        }
        String version = parser.getAttributeValue(null, "version");
        if (version != null) {
            try {
                versionCode = Long.parseLong(version);
            } catch (NumberFormatException e) {
            }
            int pkgFlags = 0 | 1;
            if (!PackageManagerService.locationIsPrivileged(codePathStr)) {
                pkgPrivateFlags = 0 | 8;
            } else {
                pkgPrivateFlags = 0;
            }
            ps = new PackageSetting(name, realName, new File(codePathStr), new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName, null, 0, null, null);
            timeStampStr = parser.getAttributeValue(null, "ft");
            if (timeStampStr == null) {
                try {
                    ps.setTimeStamp(Long.parseLong(timeStampStr, 16));
                } catch (NumberFormatException e2) {
                }
            } else {
                String timeStampStr4 = parser.getAttributeValue(null, "ts");
                if (timeStampStr4 != null) {
                    try {
                        ps.setTimeStamp(Long.parseLong(timeStampStr4));
                    } catch (NumberFormatException e3) {
                    }
                }
            }
            timeStampStr2 = parser.getAttributeValue(null, "it");
            if (timeStampStr2 != null) {
                try {
                    ps.firstInstallTime = Long.parseLong(timeStampStr2, 16);
                } catch (NumberFormatException e4) {
                }
            }
            timeStampStr3 = parser.getAttributeValue(null, "ut");
            if (timeStampStr3 != null) {
                try {
                    ps.lastUpdateTime = Long.parseLong(timeStampStr3, 16);
                } catch (NumberFormatException e5) {
                }
            }
            String idStr = parser.getAttributeValue(null, "userId");
            int i = 0;
            ps.appId = idStr == null ? Integer.parseInt(idStr) : 0;
            if (ps.appId <= 0) {
                String sharedIdStr = parser.getAttributeValue(null, "sharedUserId");
                if (sharedIdStr != null) {
                    i = Integer.parseInt(sharedIdStr);
                }
                ps.appId = i;
            }
            int outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    this.mDisabledSysPackages.put(name, ps);
                } else if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, ps.getPermissionsState());
                    } else if (parser.getName().equals(TAG_CHILD_PACKAGE)) {
                        String childPackageName = parser.getAttributeValue(null, ATTR_NAME);
                        if (ps.childPackageNames == null) {
                            ps.childPackageNames = new ArrayList();
                        }
                        ps.childPackageNames.add(childPackageName);
                    } else if (parser.getName().equals(TAG_USES_STATIC_LIB)) {
                        readUsesStaticLibLPw(parser, ps);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <updated-package>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            this.mDisabledSysPackages.put(name, ps);
        }
        versionCode = 0;
        int pkgFlags2 = 0 | 1;
        if (!PackageManagerService.locationIsPrivileged(codePathStr)) {
        }
        ps = new PackageSetting(name, realName, new File(codePathStr), new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, pkgFlags2, pkgPrivateFlags, parentPackageName, null, 0, null, null);
        timeStampStr = parser.getAttributeValue(null, "ft");
        if (timeStampStr == null) {
        }
        timeStampStr2 = parser.getAttributeValue(null, "it");
        if (timeStampStr2 != null) {
        }
        timeStampStr3 = parser.getAttributeValue(null, "ut");
        if (timeStampStr3 != null) {
        }
        String idStr2 = parser.getAttributeValue(null, "userId");
        int i2 = 0;
        ps.appId = idStr2 == null ? Integer.parseInt(idStr2) : 0;
        if (ps.appId <= 0) {
        }
        int outerDepth2 = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1) {
                break;
            }
            break;
        }
        this.mDisabledSysPackages.put(name, ps);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:200:0x03d2 */
    /* JADX DEBUG: Multi-variable search result rejected for r2v33, resolved type: java.lang.StringBuilder */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r9v19 'codePathStr'  java.lang.String: [D('cpuAbiOverrideString' java.lang.String), D('codePathStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r6v37 'legacyNativeLibraryPathStr'  java.lang.String: [D('resourcePathStr' java.lang.String), D('legacyNativeLibraryPathStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v33 'name'  java.lang.String: [D('sharedUserId' int), D('name' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r4v52 'resourcePathStr'  java.lang.String: [D('idStr' java.lang.String), D('resourcePathStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r6v60 'legacyNativeLibraryPathStr'  java.lang.String: [D('legacyNativeLibraryPathStr' java.lang.String), D('resourcePathStr' java.lang.String)] */
    /* JADX WARN: Type inference failed for: r5v36, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r5v41 */
    /* JADX WARN: Type inference failed for: r5v43, types: [java.lang.String] */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x022b A[SYNTHETIC, Splitter:B:138:0x022b] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0252 A[Catch:{ NumberFormatException -> 0x025b }] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0256 A[Catch:{ NumberFormatException -> 0x025b }] */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0271 A[Catch:{ NumberFormatException -> 0x025b }] */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0274 A[Catch:{ NumberFormatException -> 0x025b }] */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0279  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x027c  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0283  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0289 A[SYNTHETIC, Splitter:B:153:0x0289] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02a8  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02ac A[SYNTHETIC, Splitter:B:160:0x02ac] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x0a97  */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0b2c  */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0b34  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0b42  */
    /* JADX WARNING: Removed duplicated region for block: B:444:0x0d48  */
    /* JADX WARNING: Removed duplicated region for block: B:446:0x0d3f A[SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        String str;
        String str2;
        String parentPackageName;
        String systemStr;
        String updateAvailable;
        String secondaryCpuAbiString;
        String idStr;
        String primaryCpuAbiString;
        String cpuAbiOverrideString;
        int categoryHint;
        String legacyNativeLibraryPathStr;
        String name;
        String idStr2;
        String uidError;
        PackageSetting packageSetting;
        int categoryHint2;
        Settings settings;
        int type;
        String legacyNativeLibraryPathStr2;
        String enabledStr;
        String installerPackageName;
        int outerDepth;
        boolean z;
        String installerPackageName2;
        String legacyNativeLibraryPathStr3;
        long lastUpdateTime;
        String resourcePathStr;
        String uidError2;
        String primaryCpuAbiString2;
        int i;
        String codePathStr;
        String secondaryCpuAbiString2;
        String name2;
        String sharedIdStr;
        String idStr3;
        String sharedIdStr2;
        String legacyCpuAbiString;
        long timeStamp;
        long firstInstallTime;
        String timeStampStr;
        int userId;
        String legacyNativeLibraryPathStr4;
        String realName;
        String str3;
        long firstInstallTime2;
        long timeStamp2;
        String resourcePathStr2;
        String name3;
        long lastUpdateTime2;
        int i2;
        String codePathStr2 = "Error in package manager settings: package ";
        String name4 = null;
        String realName2 = null;
        String legacyNativeLibraryPathStr5 = null;
        String primaryCpuAbiString3 = null;
        String installerPackageName3 = null;
        String isOrphaned = null;
        String volumeUuid = null;
        String updateAvailable2 = null;
        int categoryHint3 = -1;
        int pkgFlags = 0;
        int pkgPrivateFlags = 0;
        long timeStamp3 = 0;
        PackageSetting packageSetting2 = null;
        long versionCode = 0;
        try {
            name4 = parser.getAttributeValue(null, ATTR_NAME);
            realName2 = parser.getAttributeValue(null, "realName");
            String idStr4 = parser.getAttributeValue(null, "userId");
            try {
                uidError2 = parser.getAttributeValue(null, "uidError");
                try {
                    sharedIdStr2 = parser.getAttributeValue(null, "sharedUserId");
                    try {
                        legacyNativeLibraryPathStr5 = parser.getAttributeValue(null, "codePath");
                        try {
                            resourcePathStr = parser.getAttributeValue(null, "resourcePath");
                            try {
                                legacyCpuAbiString = parser.getAttributeValue(null, "requiredCpuAbi");
                            } catch (NumberFormatException e) {
                                str2 = TemperatureProvider.SWITCH_ON;
                                systemStr = " at ";
                                str = ATTR_NAME;
                                name2 = name4;
                                sharedIdStr = idStr4;
                                idStr3 = " has bad userId ";
                                legacyNativeLibraryPathStr3 = null;
                                secondaryCpuAbiString2 = null;
                                codePathStr = null;
                                lastUpdateTime = 0;
                                i = 5;
                                primaryCpuAbiString2 = null;
                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                idStr = sharedIdStr;
                                parentPackageName = name2;
                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                primaryCpuAbiString = primaryCpuAbiString2;
                                idStr2 = installerPackageName3;
                                name = isOrphaned;
                                legacyNativeLibraryPathStr = volumeUuid;
                                updateAvailable = updateAvailable2;
                                categoryHint = categoryHint3;
                                packageSetting = packageSetting2;
                                uidError = uidError2;
                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e2) {
                            str2 = TemperatureProvider.SWITCH_ON;
                            systemStr = " at ";
                            str = ATTR_NAME;
                            name2 = name4;
                            sharedIdStr = idStr4;
                            idStr3 = " has bad userId ";
                            resourcePathStr = null;
                            legacyNativeLibraryPathStr3 = null;
                            secondaryCpuAbiString2 = null;
                            codePathStr = null;
                            lastUpdateTime = 0;
                            i = 5;
                            primaryCpuAbiString2 = null;
                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                            idStr = sharedIdStr;
                            parentPackageName = name2;
                            secondaryCpuAbiString = secondaryCpuAbiString2;
                            primaryCpuAbiString = primaryCpuAbiString2;
                            idStr2 = installerPackageName3;
                            name = isOrphaned;
                            legacyNativeLibraryPathStr = volumeUuid;
                            updateAvailable = updateAvailable2;
                            categoryHint = categoryHint3;
                            packageSetting = packageSetting2;
                            uidError = uidError2;
                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e3) {
                        str2 = TemperatureProvider.SWITCH_ON;
                        systemStr = " at ";
                        str = ATTR_NAME;
                        name2 = name4;
                        sharedIdStr = idStr4;
                        idStr3 = " has bad userId ";
                        resourcePathStr = null;
                        legacyNativeLibraryPathStr3 = null;
                        secondaryCpuAbiString2 = null;
                        codePathStr = null;
                        lastUpdateTime = 0;
                        i = 5;
                        primaryCpuAbiString2 = null;
                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                        idStr = sharedIdStr;
                        parentPackageName = name2;
                        secondaryCpuAbiString = secondaryCpuAbiString2;
                        primaryCpuAbiString = primaryCpuAbiString2;
                        idStr2 = installerPackageName3;
                        name = isOrphaned;
                        legacyNativeLibraryPathStr = volumeUuid;
                        updateAvailable = updateAvailable2;
                        categoryHint = categoryHint3;
                        packageSetting = packageSetting2;
                        uidError = uidError2;
                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e4) {
                    str2 = TemperatureProvider.SWITCH_ON;
                    systemStr = " at ";
                    str = ATTR_NAME;
                    name2 = name4;
                    sharedIdStr = idStr4;
                    idStr3 = " has bad userId ";
                    resourcePathStr = null;
                    legacyNativeLibraryPathStr3 = null;
                    secondaryCpuAbiString2 = null;
                    codePathStr = null;
                    lastUpdateTime = 0;
                    i = 5;
                    primaryCpuAbiString2 = null;
                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                    idStr = sharedIdStr;
                    parentPackageName = name2;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    primaryCpuAbiString = primaryCpuAbiString2;
                    idStr2 = installerPackageName3;
                    name = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e5) {
                str2 = TemperatureProvider.SWITCH_ON;
                systemStr = " at ";
                str = ATTR_NAME;
                name2 = name4;
                sharedIdStr = idStr4;
                idStr3 = " has bad userId ";
                resourcePathStr = null;
                uidError2 = null;
                legacyNativeLibraryPathStr3 = null;
                secondaryCpuAbiString2 = null;
                codePathStr = null;
                lastUpdateTime = 0;
                i = 5;
                primaryCpuAbiString2 = null;
                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                idStr = sharedIdStr;
                parentPackageName = name2;
                secondaryCpuAbiString = secondaryCpuAbiString2;
                primaryCpuAbiString = primaryCpuAbiString2;
                idStr2 = installerPackageName3;
                name = isOrphaned;
                legacyNativeLibraryPathStr = volumeUuid;
                updateAvailable = updateAvailable2;
                categoryHint = categoryHint3;
                packageSetting = packageSetting2;
                uidError = uidError2;
                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                if (packageSetting != null) {
                }
            }
            try {
                String parentPackageName2 = parser.getAttributeValue(null, "parentPackageName");
                String legacyNativeLibraryPathStr6 = parser.getAttributeValue(null, "nativeLibraryPath");
                try {
                    primaryCpuAbiString3 = parser.getAttributeValue(null, "primaryCpuAbi");
                    secondaryCpuAbiString2 = parser.getAttributeValue(null, "secondaryCpuAbi");
                    try {
                        codePathStr = parser.getAttributeValue(null, "cpuAbiOverride");
                        try {
                            updateAvailable2 = parser.getAttributeValue(null, "updateAvailable");
                            if (primaryCpuAbiString3 != null || legacyCpuAbiString == null) {
                                primaryCpuAbiString2 = primaryCpuAbiString3;
                            } else {
                                primaryCpuAbiString2 = legacyCpuAbiString;
                            }
                            try {
                                String version = parser.getAttributeValue(null, "version");
                                if (version != null) {
                                    try {
                                        versionCode = Long.parseLong(version);
                                    } catch (NumberFormatException e6) {
                                    }
                                }
                                installerPackageName3 = parser.getAttributeValue(null, "installer");
                                isOrphaned = parser.getAttributeValue(null, "isOrphaned");
                                volumeUuid = parser.getAttributeValue(null, ATTR_VOLUME_UUID);
                                String categoryHintString = parser.getAttributeValue(null, "categoryHint");
                                if (categoryHintString != null) {
                                    try {
                                        categoryHint3 = Integer.parseInt(categoryHintString);
                                    } catch (NumberFormatException e7) {
                                    }
                                }
                                String systemStr2 = parser.getAttributeValue(null, "publicFlags");
                                if (systemStr2 != null) {
                                    try {
                                        pkgFlags = Integer.parseInt(systemStr2);
                                    } catch (NumberFormatException e8) {
                                    }
                                    try {
                                        String systemStr3 = parser.getAttributeValue(null, "privateFlags");
                                        if (systemStr3 != null) {
                                            try {
                                                pkgPrivateFlags = Integer.parseInt(systemStr3);
                                            } catch (NumberFormatException e9) {
                                            }
                                        }
                                    } catch (NumberFormatException e10) {
                                        str2 = TemperatureProvider.SWITCH_ON;
                                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                        systemStr = " at ";
                                        str = ATTR_NAME;
                                        lastUpdateTime = 0;
                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                        name2 = name4;
                                        sharedIdStr = idStr4;
                                        idStr3 = " has bad userId ";
                                        i = 5;
                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                        idStr = sharedIdStr;
                                        parentPackageName = name2;
                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                        primaryCpuAbiString = primaryCpuAbiString2;
                                        idStr2 = installerPackageName3;
                                        name = isOrphaned;
                                        legacyNativeLibraryPathStr = volumeUuid;
                                        updateAvailable = updateAvailable2;
                                        categoryHint = categoryHint3;
                                        packageSetting = packageSetting2;
                                        uidError = uidError2;
                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                        if (packageSetting != null) {
                                        }
                                    }
                                } else {
                                    String systemStr4 = parser.getAttributeValue(null, ATTR_FLAGS);
                                    if (systemStr4 != null) {
                                        try {
                                            pkgFlags = Integer.parseInt(systemStr4);
                                        } catch (NumberFormatException e11) {
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_HIDDEN) != 0) {
                                            pkgPrivateFlags = 0 | 1;
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE) != 0) {
                                            pkgPrivateFlags |= 2;
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_PRIVILEGED) != 0) {
                                            pkgPrivateFlags |= 8;
                                        }
                                        pkgFlags &= ~(PRE_M_APP_INFO_FLAG_HIDDEN | PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE | PRE_M_APP_INFO_FLAG_PRIVILEGED);
                                    } else {
                                        String systemStr5 = parser.getAttributeValue(null, "system");
                                        if (systemStr5 != null) {
                                            try {
                                                if (TemperatureProvider.SWITCH_ON.equalsIgnoreCase(systemStr5)) {
                                                    i2 = 1;
                                                } else {
                                                    i2 = 0;
                                                }
                                                pkgFlags = 0 | i2;
                                            } catch (NumberFormatException e12) {
                                                str2 = TemperatureProvider.SWITCH_ON;
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                str = ATTR_NAME;
                                                lastUpdateTime = 0;
                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                name2 = name4;
                                                sharedIdStr = idStr4;
                                                systemStr = " at ";
                                                idStr3 = " has bad userId ";
                                                i = 5;
                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                idStr = sharedIdStr;
                                                parentPackageName = name2;
                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                idStr2 = installerPackageName3;
                                                name = isOrphaned;
                                                legacyNativeLibraryPathStr = volumeUuid;
                                                updateAvailable = updateAvailable2;
                                                categoryHint = categoryHint3;
                                                packageSetting = packageSetting2;
                                                uidError = uidError2;
                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                if (packageSetting != null) {
                                                }
                                            }
                                        } else {
                                            pkgFlags = 0 | 1;
                                        }
                                    }
                                }
                                String timeStampStr2 = parser.getAttributeValue(null, "ft");
                                if (timeStampStr2 != null) {
                                    try {
                                        timeStamp3 = Long.parseLong(timeStampStr2, 16);
                                    } catch (NumberFormatException e13) {
                                    }
                                    timeStamp = timeStamp3;
                                } else {
                                    try {
                                        String timeStampStr3 = parser.getAttributeValue(null, "ts");
                                        if (timeStampStr3 != null) {
                                            try {
                                                timeStamp = Long.parseLong(timeStampStr3);
                                            } catch (NumberFormatException e14) {
                                            }
                                        }
                                        timeStamp = 0;
                                    } catch (NumberFormatException e15) {
                                        name2 = name4;
                                        systemStr = " at ";
                                        str = ATTR_NAME;
                                        str2 = TemperatureProvider.SWITCH_ON;
                                        sharedIdStr = idStr4;
                                        idStr3 = " has bad userId ";
                                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                        lastUpdateTime = 0;
                                        i = 5;
                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                        idStr = sharedIdStr;
                                        parentPackageName = name2;
                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                        primaryCpuAbiString = primaryCpuAbiString2;
                                        idStr2 = installerPackageName3;
                                        name = isOrphaned;
                                        legacyNativeLibraryPathStr = volumeUuid;
                                        updateAvailable = updateAvailable2;
                                        categoryHint = categoryHint3;
                                        packageSetting = packageSetting2;
                                        uidError = uidError2;
                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                        if (packageSetting != null) {
                                        }
                                    }
                                }
                                try {
                                    String timeStampStr4 = parser.getAttributeValue(null, "it");
                                    if (timeStampStr4 != null) {
                                        try {
                                            firstInstallTime = Long.parseLong(timeStampStr4, 16);
                                        } catch (NumberFormatException e16) {
                                        }
                                        timeStampStr = parser.getAttributeValue(null, "ut");
                                        if (timeStampStr != null) {
                                            try {
                                                lastUpdateTime = Long.parseLong(timeStampStr, 16);
                                            } catch (NumberFormatException e17) {
                                            }
                                            if (PackageManagerService.DEBUG_SETTINGS) {
                                                try {
                                                    Log.v("PackageManager", "Reading package: " + name4 + " userId=" + idStr4 + " sharedUserId=" + sharedIdStr2);
                                                } catch (NumberFormatException e18) {
                                                    name2 = name4;
                                                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                    systemStr = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = TemperatureProvider.SWITCH_ON;
                                                    sharedIdStr = idStr4;
                                                    idStr3 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                    idStr = sharedIdStr;
                                                    parentPackageName = name2;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                    idStr2 = installerPackageName3;
                                                    name = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            }
                                            if (idStr4 != null) {
                                                userId = Integer.parseInt(idStr4);
                                            } else {
                                                userId = 0;
                                            }
                                            int sharedUserId = sharedIdStr2 != null ? Integer.parseInt(sharedIdStr2) : 0;
                                            if (resourcePathStr == null) {
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                legacyNativeLibraryPathStr4 = legacyNativeLibraryPathStr5;
                                            } else {
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                legacyNativeLibraryPathStr4 = resourcePathStr;
                                            }
                                            if (realName2 != null) {
                                                try {
                                                    realName = realName2.intern();
                                                } catch (NumberFormatException e19) {
                                                    name2 = name4;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    systemStr = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = TemperatureProvider.SWITCH_ON;
                                                    sharedIdStr = idStr4;
                                                    idStr3 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                    idStr = sharedIdStr;
                                                    parentPackageName = name2;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                    idStr2 = installerPackageName3;
                                                    name = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else {
                                                realName = realName2;
                                            }
                                            if (name4 == null) {
                                                try {
                                                    try {
                                                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <package> has no name at " + parser.getPositionDescription());
                                                        name3 = name4;
                                                        str3 = " at ";
                                                        str = ATTR_NAME;
                                                        str2 = TemperatureProvider.SWITCH_ON;
                                                        timeStamp2 = timeStamp;
                                                        firstInstallTime2 = firstInstallTime;
                                                        resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                        sharedIdStr = idStr4;
                                                    } catch (NumberFormatException e20) {
                                                        name2 = name4;
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        str = ATTR_NAME;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        str2 = TemperatureProvider.SWITCH_ON;
                                                        sharedIdStr = idStr4;
                                                        idStr3 = " has bad userId ";
                                                        i = 5;
                                                        systemStr = " at ";
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e21) {
                                                    name2 = name4;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    systemStr = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = TemperatureProvider.SWITCH_ON;
                                                    sharedIdStr = idStr4;
                                                    idStr3 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                    idStr = sharedIdStr;
                                                    parentPackageName = name2;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                    idStr2 = installerPackageName3;
                                                    name = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else if (legacyNativeLibraryPathStr5 == null) {
                                                try {
                                                    StringBuilder sb = new StringBuilder();
                                                    try {
                                                        sb.append("Error in package manager settings: <package> has no codePath at ");
                                                        sb.append(parser.getPositionDescription());
                                                        try {
                                                            PackageManagerService.reportSettingsProblem(5, sb.toString());
                                                            name3 = name4;
                                                            str3 = " at ";
                                                            str = ATTR_NAME;
                                                            sharedIdStr = idStr4;
                                                            str2 = TemperatureProvider.SWITCH_ON;
                                                            timeStamp2 = timeStamp;
                                                            firstInstallTime2 = firstInstallTime;
                                                            resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                        } catch (NumberFormatException e22) {
                                                            name2 = name4;
                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                            idStr3 = " has bad userId ";
                                                            str = ATTR_NAME;
                                                            i = 5;
                                                            sharedIdStr = idStr4;
                                                            str2 = TemperatureProvider.SWITCH_ON;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            systemStr = " at ";
                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                            idStr = sharedIdStr;
                                                            parentPackageName = name2;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                            idStr2 = installerPackageName3;
                                                            name = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e23) {
                                                        name2 = name4;
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        systemStr = " at ";
                                                        idStr3 = " has bad userId ";
                                                        str = ATTR_NAME;
                                                        sharedIdStr = idStr4;
                                                        str2 = TemperatureProvider.SWITCH_ON;
                                                        i = 5;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e24) {
                                                    name2 = name4;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    systemStr = " at ";
                                                    idStr3 = " has bad userId ";
                                                    str = ATTR_NAME;
                                                    sharedIdStr = idStr4;
                                                    str2 = TemperatureProvider.SWITCH_ON;
                                                    i = 5;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                    idStr = sharedIdStr;
                                                    parentPackageName = name2;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                    idStr2 = installerPackageName3;
                                                    name = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else if (userId > 0) {
                                                try {
                                                    String intern = name4.intern();
                                                    try {
                                                        File file = new File(legacyNativeLibraryPathStr5);
                                                        ?? file2 = new File(legacyNativeLibraryPathStr4);
                                                        str2 = TemperatureProvider.SWITCH_ON;
                                                        str3 = " at ";
                                                        str = ATTR_NAME;
                                                        resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                        try {
                                                            PackageSetting packageSetting3 = addPackageLPw(intern, realName, file, file2, resourcePathStr2, primaryCpuAbiString2, secondaryCpuAbiString2, codePathStr, userId, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName2, null, null, null);
                                                            try {
                                                                if (PackageManagerService.DEBUG_SETTINGS) {
                                                                    try {
                                                                        StringBuilder sb2 = new StringBuilder();
                                                                        sb2.append("Reading package ");
                                                                        file2 = name4;
                                                                        try {
                                                                            sb2.append((String) file2);
                                                                            sb2.append(": userId=");
                                                                            sb2.append(userId);
                                                                            sb2.append(" pkg=");
                                                                            sb2.append(packageSetting3);
                                                                            Log.i("PackageManager", sb2.toString());
                                                                            name3 = file2;
                                                                        } catch (NumberFormatException e25) {
                                                                            packageSetting2 = packageSetting3;
                                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                            sharedIdStr = idStr4;
                                                                            codePathStr2 = codePathStr2;
                                                                            systemStr = str3;
                                                                            idStr3 = " has bad userId ";
                                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                                            i = 5;
                                                                            name2 = file2;
                                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                            idStr = sharedIdStr;
                                                                            parentPackageName = name2;
                                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                                            idStr2 = installerPackageName3;
                                                                            name = isOrphaned;
                                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                                            updateAvailable = updateAvailable2;
                                                                            categoryHint = categoryHint3;
                                                                            packageSetting = packageSetting2;
                                                                            uidError = uidError2;
                                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e26) {
                                                                        name2 = name4;
                                                                        packageSetting2 = packageSetting3;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        sharedIdStr = idStr4;
                                                                        codePathStr2 = codePathStr2;
                                                                        systemStr = str3;
                                                                        idStr3 = " has bad userId ";
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        i = 5;
                                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                        idStr = sharedIdStr;
                                                                        parentPackageName = name2;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = installerPackageName3;
                                                                        name = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } else {
                                                                    name3 = name4;
                                                                }
                                                                if (packageSetting3 == null) {
                                                                    PackageManagerService.reportSettingsProblem(6, "Failure adding uid " + userId + " while parsing settings at " + parser.getPositionDescription());
                                                                    timeStamp2 = timeStamp;
                                                                    firstInstallTime2 = firstInstallTime;
                                                                    lastUpdateTime2 = lastUpdateTime;
                                                                } else {
                                                                    timeStamp2 = timeStamp;
                                                                    try {
                                                                        packageSetting3.setTimeStamp(timeStamp2);
                                                                        firstInstallTime2 = firstInstallTime;
                                                                        try {
                                                                            packageSetting3.firstInstallTime = firstInstallTime2;
                                                                            lastUpdateTime2 = lastUpdateTime;
                                                                            try {
                                                                                packageSetting3.lastUpdateTime = lastUpdateTime2;
                                                                            } catch (NumberFormatException e27) {
                                                                                packageSetting2 = packageSetting3;
                                                                                lastUpdateTime = lastUpdateTime2;
                                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                                sharedIdStr = idStr4;
                                                                                codePathStr2 = codePathStr2;
                                                                                systemStr = str3;
                                                                                idStr3 = " has bad userId ";
                                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                                i = 5;
                                                                                name2 = name3;
                                                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                                idStr = sharedIdStr;
                                                                                parentPackageName = name2;
                                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                                                idStr2 = installerPackageName3;
                                                                                name = isOrphaned;
                                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                                updateAvailable = updateAvailable2;
                                                                                categoryHint = categoryHint3;
                                                                                packageSetting = packageSetting2;
                                                                                uidError = uidError2;
                                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                                if (packageSetting != null) {
                                                                                }
                                                                            }
                                                                        } catch (NumberFormatException e28) {
                                                                            packageSetting2 = packageSetting3;
                                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                            sharedIdStr = idStr4;
                                                                            codePathStr2 = codePathStr2;
                                                                            systemStr = str3;
                                                                            idStr3 = " has bad userId ";
                                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                                            i = 5;
                                                                            name2 = name3;
                                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                            idStr = sharedIdStr;
                                                                            parentPackageName = name2;
                                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                                            idStr2 = installerPackageName3;
                                                                            name = isOrphaned;
                                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                                            updateAvailable = updateAvailable2;
                                                                            categoryHint = categoryHint3;
                                                                            packageSetting = packageSetting2;
                                                                            uidError = uidError2;
                                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e29) {
                                                                        packageSetting2 = packageSetting3;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        sharedIdStr = idStr4;
                                                                        codePathStr2 = codePathStr2;
                                                                        systemStr = str3;
                                                                        idStr3 = " has bad userId ";
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        i = 5;
                                                                        name2 = name3;
                                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                        idStr = sharedIdStr;
                                                                        parentPackageName = name2;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = installerPackageName3;
                                                                        name = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                }
                                                                packageSetting2 = packageSetting3;
                                                                sharedIdStr = idStr4;
                                                                codePathStr2 = codePathStr2;
                                                            } catch (NumberFormatException e30) {
                                                                name2 = name4;
                                                                packageSetting2 = packageSetting3;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                sharedIdStr = idStr4;
                                                                codePathStr2 = codePathStr2;
                                                                systemStr = str3;
                                                                idStr3 = " has bad userId ";
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                i = 5;
                                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                idStr = sharedIdStr;
                                                                parentPackageName = name2;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                                idStr2 = installerPackageName3;
                                                                name = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e31) {
                                                            name2 = name4;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            sharedIdStr = idStr4;
                                                            codePathStr2 = codePathStr2;
                                                            systemStr = str3;
                                                            idStr3 = " has bad userId ";
                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                            i = 5;
                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                            idStr = sharedIdStr;
                                                            parentPackageName = name2;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                            idStr2 = installerPackageName3;
                                                            name = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e32) {
                                                        name2 = name4;
                                                        str = ATTR_NAME;
                                                        str2 = TemperatureProvider.SWITCH_ON;
                                                        i = 5;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        sharedIdStr = idStr4;
                                                        systemStr = " at ";
                                                        idStr3 = " has bad userId ";
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e33) {
                                                    name2 = name4;
                                                    str = ATTR_NAME;
                                                    str2 = TemperatureProvider.SWITCH_ON;
                                                    i = 5;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    sharedIdStr = idStr4;
                                                    systemStr = " at ";
                                                    idStr3 = " has bad userId ";
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                    idStr = sharedIdStr;
                                                    parentPackageName = name2;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                    idStr2 = installerPackageName3;
                                                    name = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else {
                                                str3 = " at ";
                                                str = ATTR_NAME;
                                                str2 = TemperatureProvider.SWITCH_ON;
                                                timeStamp2 = timeStamp;
                                                firstInstallTime2 = firstInstallTime;
                                                resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                name3 = name4;
                                                if (sharedIdStr2 == null) {
                                                    codePathStr2 = codePathStr2;
                                                    try {
                                                        StringBuilder sb3 = new StringBuilder();
                                                        sb3.append(codePathStr2);
                                                        sb3.append(name3);
                                                        idStr3 = " has bad userId ";
                                                        try {
                                                            sb3.append(idStr3);
                                                            sharedIdStr = idStr4;
                                                            try {
                                                                sb3.append(sharedIdStr);
                                                                sb3.append(str3);
                                                                str3 = str3;
                                                                try {
                                                                    sb3.append(parser.getPositionDescription());
                                                                    try {
                                                                        PackageManagerService.reportSettingsProblem(5, sb3.toString());
                                                                    } catch (NumberFormatException e34) {
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        lastUpdateTime = lastUpdateTime;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        systemStr = str3;
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        i = 5;
                                                                        name2 = name3;
                                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                        idStr = sharedIdStr;
                                                                        parentPackageName = name2;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = installerPackageName3;
                                                                        name = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e35) {
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    lastUpdateTime = lastUpdateTime;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    systemStr = str3;
                                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                                    i = 5;
                                                                    name2 = name3;
                                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                    idStr = sharedIdStr;
                                                                    parentPackageName = name2;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                                    idStr2 = installerPackageName3;
                                                                    name = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e36) {
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                lastUpdateTime = lastUpdateTime;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                systemStr = str3;
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                i = 5;
                                                                name2 = name3;
                                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                idStr = sharedIdStr;
                                                                parentPackageName = name2;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                                idStr2 = installerPackageName3;
                                                                name = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e37) {
                                                            sharedIdStr = idStr4;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            lastUpdateTime = lastUpdateTime;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            systemStr = str3;
                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                            i = 5;
                                                            name2 = name3;
                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                            idStr = sharedIdStr;
                                                            parentPackageName = name2;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                            idStr2 = installerPackageName3;
                                                            name = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e38) {
                                                        sharedIdStr = idStr4;
                                                        idStr3 = " has bad userId ";
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        lastUpdateTime = lastUpdateTime;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        systemStr = str3;
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        i = 5;
                                                        name2 = name3;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else if (sharedUserId > 0) {
                                                    try {
                                                    } catch (NumberFormatException e39) {
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        lastUpdateTime = lastUpdateTime;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        sharedIdStr = idStr4;
                                                        codePathStr2 = codePathStr2;
                                                        systemStr = str3;
                                                        idStr3 = " has bad userId ";
                                                        i = 5;
                                                        name2 = name3;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                    try {
                                                        try {
                                                            try {
                                                                PackageSetting packageSetting4 = new PackageSetting(name3.intern(), realName, new File(legacyNativeLibraryPathStr5), new File(legacyNativeLibraryPathStr4), resourcePathStr2, primaryCpuAbiString2, secondaryCpuAbiString2, codePathStr, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName2, null, sharedUserId, null, null);
                                                                try {
                                                                    packageSetting4.setTimeStamp(timeStamp2);
                                                                    packageSetting4.firstInstallTime = firstInstallTime2;
                                                                    packageSetting4.lastUpdateTime = lastUpdateTime;
                                                                    try {
                                                                        this.mPendingPackages.add(packageSetting4);
                                                                        if (PackageManagerService.DEBUG_SETTINGS) {
                                                                            Log.i("PackageManager", "Reading package " + name3 + ": sharedUserId=" + sharedUserId + " pkg=" + packageSetting4);
                                                                        }
                                                                        packageSetting2 = packageSetting4;
                                                                        sharedIdStr = idStr4;
                                                                        codePathStr2 = codePathStr2;
                                                                    } catch (NumberFormatException e40) {
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        packageSetting2 = packageSetting4;
                                                                        lastUpdateTime = lastUpdateTime;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        sharedIdStr = idStr4;
                                                                        codePathStr2 = codePathStr2;
                                                                        systemStr = str3;
                                                                        idStr3 = " has bad userId ";
                                                                        i = 5;
                                                                        name2 = name3;
                                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                        idStr = sharedIdStr;
                                                                        parentPackageName = name2;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = installerPackageName3;
                                                                        name = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e41) {
                                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    packageSetting2 = packageSetting4;
                                                                    lastUpdateTime = lastUpdateTime;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    sharedIdStr = idStr4;
                                                                    codePathStr2 = codePathStr2;
                                                                    systemStr = str3;
                                                                    idStr3 = " has bad userId ";
                                                                    i = 5;
                                                                    name2 = name3;
                                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                    idStr = sharedIdStr;
                                                                    parentPackageName = name2;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                                    idStr2 = installerPackageName3;
                                                                    name = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e42) {
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                lastUpdateTime = lastUpdateTime;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                sharedIdStr = idStr4;
                                                                codePathStr2 = codePathStr2;
                                                                systemStr = str3;
                                                                idStr3 = " has bad userId ";
                                                                i = 5;
                                                                name2 = name3;
                                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                idStr = sharedIdStr;
                                                                parentPackageName = name2;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                                idStr2 = installerPackageName3;
                                                                name = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e43) {
                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            lastUpdateTime = lastUpdateTime;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            sharedIdStr = idStr4;
                                                            codePathStr2 = codePathStr2;
                                                            systemStr = str3;
                                                            idStr3 = " has bad userId ";
                                                            i = 5;
                                                            name2 = name3;
                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                            idStr = sharedIdStr;
                                                            parentPackageName = name2;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                            idStr2 = installerPackageName3;
                                                            name = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e44) {
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        lastUpdateTime = lastUpdateTime;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        sharedIdStr = idStr4;
                                                        codePathStr2 = codePathStr2;
                                                        systemStr = str3;
                                                        idStr3 = " has bad userId ";
                                                        i = 5;
                                                        name2 = name3;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else {
                                                    try {
                                                        StringBuilder sb4 = new StringBuilder();
                                                        codePathStr2 = codePathStr2;
                                                        try {
                                                            sb4.append(codePathStr2);
                                                            sb4.append(name3);
                                                            sb4.append(" has bad sharedId ");
                                                            try {
                                                                sb4.append(sharedIdStr2);
                                                                try {
                                                                    sb4.append(str3);
                                                                    try {
                                                                        sb4.append(parser.getPositionDescription());
                                                                        try {
                                                                            PackageManagerService.reportSettingsProblem(5, sb4.toString());
                                                                            str3 = str3;
                                                                            sharedIdStr = idStr4;
                                                                        } catch (NumberFormatException e45) {
                                                                            systemStr = str3;
                                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                            lastUpdateTime = lastUpdateTime;
                                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                            sharedIdStr = idStr4;
                                                                            i = 5;
                                                                            idStr3 = " has bad userId ";
                                                                            name2 = name3;
                                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                            idStr = sharedIdStr;
                                                                            parentPackageName = name2;
                                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                                            idStr2 = installerPackageName3;
                                                                            name = isOrphaned;
                                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                                            updateAvailable = updateAvailable2;
                                                                            categoryHint = categoryHint3;
                                                                            packageSetting = packageSetting2;
                                                                            uidError = uidError2;
                                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e46) {
                                                                        systemStr = str3;
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        lastUpdateTime = lastUpdateTime;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        sharedIdStr = idStr4;
                                                                        idStr3 = " has bad userId ";
                                                                        i = 5;
                                                                        name2 = name3;
                                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                        idStr = sharedIdStr;
                                                                        parentPackageName = name2;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = installerPackageName3;
                                                                        name = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e47) {
                                                                    systemStr = str3;
                                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    lastUpdateTime = lastUpdateTime;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    sharedIdStr = idStr4;
                                                                    idStr3 = " has bad userId ";
                                                                    i = 5;
                                                                    name2 = name3;
                                                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                    idStr = sharedIdStr;
                                                                    parentPackageName = name2;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    primaryCpuAbiString = primaryCpuAbiString2;
                                                                    idStr2 = installerPackageName3;
                                                                    name = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e48) {
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                lastUpdateTime = lastUpdateTime;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                sharedIdStr = idStr4;
                                                                systemStr = str3;
                                                                idStr3 = " has bad userId ";
                                                                i = 5;
                                                                name2 = name3;
                                                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                                idStr = sharedIdStr;
                                                                parentPackageName = name2;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                primaryCpuAbiString = primaryCpuAbiString2;
                                                                idStr2 = installerPackageName3;
                                                                name = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e49) {
                                                            resourcePathStr = legacyNativeLibraryPathStr4;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            lastUpdateTime = lastUpdateTime;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            sharedIdStr = idStr4;
                                                            systemStr = str3;
                                                            idStr3 = " has bad userId ";
                                                            i = 5;
                                                            name2 = name3;
                                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                            idStr = sharedIdStr;
                                                            parentPackageName = name2;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            primaryCpuAbiString = primaryCpuAbiString2;
                                                            idStr2 = installerPackageName3;
                                                            name = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e50) {
                                                        codePathStr2 = codePathStr2;
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        lastUpdateTime = lastUpdateTime;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        sharedIdStr = idStr4;
                                                        systemStr = str3;
                                                        idStr3 = " has bad userId ";
                                                        i = 5;
                                                        name2 = name3;
                                                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                                        idStr = sharedIdStr;
                                                        parentPackageName = name2;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        primaryCpuAbiString = primaryCpuAbiString2;
                                                        idStr2 = installerPackageName3;
                                                        name = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                }
                                            }
                                            parentPackageName = name3;
                                            primaryCpuAbiString = primaryCpuAbiString2;
                                            name = isOrphaned;
                                            updateAvailable = updateAvailable2;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            systemStr = str3;
                                            idStr = sharedIdStr;
                                            cpuAbiOverrideString = resourcePathStr2;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            idStr2 = installerPackageName3;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                            categoryHint = categoryHint3;
                                            if (packageSetting != null) {
                                                packageSetting.uidError = str2.equals(uidError);
                                                packageSetting.installerPackageName = idStr2;
                                                packageSetting.isOrphaned = str2.equals(name);
                                                packageSetting.volumeUuid = legacyNativeLibraryPathStr;
                                                packageSetting.categoryHint = categoryHint;
                                                packageSetting.legacyNativeLibraryPathString = cpuAbiOverrideString;
                                                packageSetting.primaryCpuAbiString = primaryCpuAbiString;
                                                packageSetting.secondaryCpuAbiString = secondaryCpuAbiString;
                                                packageSetting.updateAvailable = str2.equals(updateAvailable);
                                                String enabledStr2 = parser.getAttributeValue(null, ATTR_ENABLED);
                                                if (enabledStr2 != null) {
                                                    try {
                                                        categoryHint2 = 0;
                                                        try {
                                                            packageSetting.setEnabled(Integer.parseInt(enabledStr2), 0, null);
                                                        } catch (NumberFormatException e51) {
                                                        }
                                                    } catch (NumberFormatException e52) {
                                                        categoryHint2 = 0;
                                                        if (enabledStr2.equalsIgnoreCase(str2)) {
                                                            packageSetting.setEnabled(1, categoryHint2, null);
                                                        } else if (enabledStr2.equalsIgnoreCase(TemperatureProvider.SWITCH_OFF)) {
                                                            packageSetting.setEnabled(2, categoryHint2, null);
                                                        } else if (enabledStr2.equalsIgnoreCase(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
                                                            packageSetting.setEnabled(categoryHint2, categoryHint2, null);
                                                        } else {
                                                            PackageManagerService.reportSettingsProblem(5, codePathStr2 + parentPackageName + " has bad enabled value: " + idStr + systemStr + parser.getPositionDescription());
                                                        }
                                                        if (idStr2 == null) {
                                                        }
                                                        int outerDepth2 = parser.getDepth();
                                                        while (true) {
                                                            type = parser.next();
                                                            if (type != 1) {
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    categoryHint2 = 0;
                                                    packageSetting.setEnabled(0, 0, null);
                                                }
                                                if (idStr2 == null) {
                                                    settings = this;
                                                    settings.mInstallerPackages.add(idStr2);
                                                } else {
                                                    settings = this;
                                                }
                                                int outerDepth22 = parser.getDepth();
                                                while (true) {
                                                    type = parser.next();
                                                    if (type != 1) {
                                                        return;
                                                    }
                                                    if (type == 3 && parser.getDepth() <= outerDepth22) {
                                                        return;
                                                    }
                                                    if (type == 3) {
                                                        idStr2 = idStr2;
                                                        enabledStr2 = enabledStr2;
                                                        categoryHint2 = 0;
                                                    } else if (type != 4) {
                                                        String tagName = parser.getName();
                                                        if (tagName.equals(TAG_DISABLED_COMPONENTS)) {
                                                            settings.readDisabledComponentsLPw(packageSetting, parser, categoryHint2);
                                                            outerDepth = outerDepth22;
                                                            installerPackageName = idStr2;
                                                            enabledStr = enabledStr2;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            installerPackageName2 = str;
                                                            z = false;
                                                        } else if (tagName.equals(TAG_ENABLED_COMPONENTS)) {
                                                            settings.readEnabledComponentsLPw(packageSetting, parser, categoryHint2);
                                                            outerDepth = outerDepth22;
                                                            installerPackageName = idStr2;
                                                            enabledStr = enabledStr2;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            installerPackageName2 = str;
                                                            z = false;
                                                        } else if (tagName.equals("sigs")) {
                                                            packageSetting.signatures.readXml(parser, settings.mPastSignatures);
                                                            outerDepth = outerDepth22;
                                                            installerPackageName = idStr2;
                                                            enabledStr = enabledStr2;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            installerPackageName2 = str;
                                                            z = false;
                                                        } else if (tagName.equals(TAG_PERMISSIONS)) {
                                                            settings.readInstallPermissionsLPr(parser, packageSetting.getPermissionsState());
                                                            packageSetting.installPermissionsFixed = true;
                                                            outerDepth = outerDepth22;
                                                            installerPackageName = idStr2;
                                                            enabledStr = enabledStr2;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            installerPackageName2 = str;
                                                            z = false;
                                                        } else {
                                                            if (tagName.equals("proper-signing-keyset")) {
                                                                long id = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                                                outerDepth = outerDepth22;
                                                                installerPackageName = idStr2;
                                                                Integer refCt = settings.mKeySetRefs.get(Long.valueOf(id));
                                                                if (refCt != null) {
                                                                    enabledStr = enabledStr2;
                                                                    settings.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(refCt.intValue() + 1));
                                                                } else {
                                                                    enabledStr = enabledStr2;
                                                                    settings.mKeySetRefs.put(Long.valueOf(id), 1);
                                                                }
                                                                packageSetting.keySetData.setProperSigningKeySet(id);
                                                            } else {
                                                                outerDepth = outerDepth22;
                                                                installerPackageName = idStr2;
                                                                enabledStr = enabledStr2;
                                                                if (!tagName.equals("signing-keyset")) {
                                                                    if (tagName.equals("upgrade-keyset")) {
                                                                        packageSetting.keySetData.addUpgradeKeySetById(Long.parseLong(parser.getAttributeValue(null, "identifier")));
                                                                        legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                        installerPackageName2 = str;
                                                                        z = false;
                                                                    } else if (tagName.equals("defined-keyset")) {
                                                                        long id2 = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                                                        String alias = parser.getAttributeValue(null, "alias");
                                                                        Integer refCt2 = settings.mKeySetRefs.get(Long.valueOf(id2));
                                                                        if (refCt2 != null) {
                                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                            settings.mKeySetRefs.put(Long.valueOf(id2), Integer.valueOf(refCt2.intValue() + 1));
                                                                        } else {
                                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                            settings.mKeySetRefs.put(Long.valueOf(id2), 1);
                                                                        }
                                                                        packageSetting.keySetData.addDefinedKeySet(id2, alias);
                                                                        installerPackageName2 = str;
                                                                        z = false;
                                                                    } else {
                                                                        legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                        if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                                                                            settings.readDomainVerificationLPw(parser, packageSetting);
                                                                            installerPackageName2 = str;
                                                                            z = false;
                                                                        } else if (tagName.equals(TAG_CHILD_PACKAGE)) {
                                                                            installerPackageName2 = str;
                                                                            z = false;
                                                                            String childPackageName = parser.getAttributeValue(null, installerPackageName2);
                                                                            if (packageSetting.childPackageNames == null) {
                                                                                packageSetting.childPackageNames = new ArrayList();
                                                                            }
                                                                            packageSetting.childPackageNames.add(childPackageName);
                                                                        } else {
                                                                            installerPackageName2 = str;
                                                                            z = false;
                                                                            PackageManagerService.reportSettingsProblem(5, "Unknown element under <package>: " + parser.getName());
                                                                            XmlUtils.skipCurrentTag(parser);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            installerPackageName2 = str;
                                                            z = false;
                                                        }
                                                        str = installerPackageName2;
                                                        outerDepth22 = outerDepth;
                                                        idStr2 = installerPackageName;
                                                        enabledStr2 = enabledStr;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr2;
                                                        categoryHint2 = 0;
                                                    }
                                                }
                                            } else {
                                                XmlUtils.skipCurrentTag(parser);
                                                return;
                                            }
                                        }
                                        lastUpdateTime = 0;
                                        try {
                                            if (PackageManagerService.DEBUG_SETTINGS) {
                                            }
                                            if (idStr4 != null) {
                                            }
                                            if (sharedIdStr2 != null) {
                                            }
                                            if (resourcePathStr == null) {
                                            }
                                            if (realName2 != null) {
                                            }
                                            if (name4 == null) {
                                            }
                                            parentPackageName = name3;
                                            primaryCpuAbiString = primaryCpuAbiString2;
                                            name = isOrphaned;
                                            updateAvailable = updateAvailable2;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            systemStr = str3;
                                            idStr = sharedIdStr;
                                            cpuAbiOverrideString = resourcePathStr2;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            idStr2 = installerPackageName3;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                            categoryHint = categoryHint3;
                                        } catch (NumberFormatException e53) {
                                            name2 = name4;
                                            systemStr = " at ";
                                            str = ATTR_NAME;
                                            str2 = TemperatureProvider.SWITCH_ON;
                                            sharedIdStr = idStr4;
                                            idStr3 = " has bad userId ";
                                            legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                            i = 5;
                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                            idStr = sharedIdStr;
                                            parentPackageName = name2;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            primaryCpuAbiString = primaryCpuAbiString2;
                                            idStr2 = installerPackageName3;
                                            name = isOrphaned;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                            updateAvailable = updateAvailable2;
                                            categoryHint = categoryHint3;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                            if (packageSetting != null) {
                                            }
                                        }
                                        if (packageSetting != null) {
                                        }
                                    }
                                    firstInstallTime = 0;
                                } catch (NumberFormatException e54) {
                                    name2 = name4;
                                    systemStr = " at ";
                                    str = ATTR_NAME;
                                    str2 = TemperatureProvider.SWITCH_ON;
                                    sharedIdStr = idStr4;
                                    idStr3 = " has bad userId ";
                                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                    lastUpdateTime = 0;
                                    i = 5;
                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                    idStr = sharedIdStr;
                                    parentPackageName = name2;
                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                    primaryCpuAbiString = primaryCpuAbiString2;
                                    idStr2 = installerPackageName3;
                                    name = isOrphaned;
                                    legacyNativeLibraryPathStr = volumeUuid;
                                    updateAvailable = updateAvailable2;
                                    categoryHint = categoryHint3;
                                    packageSetting = packageSetting2;
                                    uidError = uidError2;
                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                    if (packageSetting != null) {
                                    }
                                }
                            } catch (NumberFormatException e55) {
                                str2 = TemperatureProvider.SWITCH_ON;
                                systemStr = " at ";
                                str = ATTR_NAME;
                                name2 = name4;
                                sharedIdStr = idStr4;
                                idStr3 = " has bad userId ";
                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                lastUpdateTime = 0;
                                i = 5;
                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                                idStr = sharedIdStr;
                                parentPackageName = name2;
                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                primaryCpuAbiString = primaryCpuAbiString2;
                                idStr2 = installerPackageName3;
                                name = isOrphaned;
                                legacyNativeLibraryPathStr = volumeUuid;
                                updateAvailable = updateAvailable2;
                                categoryHint = categoryHint3;
                                packageSetting = packageSetting2;
                                uidError = uidError2;
                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e56) {
                            str2 = TemperatureProvider.SWITCH_ON;
                            systemStr = " at ";
                            str = ATTR_NAME;
                            name2 = name4;
                            sharedIdStr = idStr4;
                            idStr3 = " has bad userId ";
                            legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                            primaryCpuAbiString2 = primaryCpuAbiString3;
                            lastUpdateTime = 0;
                            i = 5;
                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                            idStr = sharedIdStr;
                            parentPackageName = name2;
                            secondaryCpuAbiString = secondaryCpuAbiString2;
                            primaryCpuAbiString = primaryCpuAbiString2;
                            idStr2 = installerPackageName3;
                            name = isOrphaned;
                            legacyNativeLibraryPathStr = volumeUuid;
                            updateAvailable = updateAvailable2;
                            categoryHint = categoryHint3;
                            packageSetting = packageSetting2;
                            uidError = uidError2;
                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e57) {
                        str2 = TemperatureProvider.SWITCH_ON;
                        systemStr = " at ";
                        str = ATTR_NAME;
                        name2 = name4;
                        sharedIdStr = idStr4;
                        idStr3 = " has bad userId ";
                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                        codePathStr = null;
                        lastUpdateTime = 0;
                        i = 5;
                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                        primaryCpuAbiString2 = primaryCpuAbiString3;
                        PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                        idStr = sharedIdStr;
                        parentPackageName = name2;
                        secondaryCpuAbiString = secondaryCpuAbiString2;
                        primaryCpuAbiString = primaryCpuAbiString2;
                        idStr2 = installerPackageName3;
                        name = isOrphaned;
                        legacyNativeLibraryPathStr = volumeUuid;
                        updateAvailable = updateAvailable2;
                        categoryHint = categoryHint3;
                        packageSetting = packageSetting2;
                        uidError = uidError2;
                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e58) {
                    str2 = TemperatureProvider.SWITCH_ON;
                    systemStr = " at ";
                    str = ATTR_NAME;
                    name2 = name4;
                    sharedIdStr = idStr4;
                    idStr3 = " has bad userId ";
                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                    secondaryCpuAbiString2 = null;
                    codePathStr = null;
                    lastUpdateTime = 0;
                    i = 5;
                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                    primaryCpuAbiString2 = primaryCpuAbiString3;
                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                    idStr = sharedIdStr;
                    parentPackageName = name2;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    primaryCpuAbiString = primaryCpuAbiString2;
                    idStr2 = installerPackageName3;
                    name = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
                try {
                    timeStampStr = parser.getAttributeValue(null, "ut");
                    if (timeStampStr != null) {
                    }
                    lastUpdateTime = 0;
                    if (PackageManagerService.DEBUG_SETTINGS) {
                    }
                    if (idStr4 != null) {
                    }
                    if (sharedIdStr2 != null) {
                    }
                    if (resourcePathStr == null) {
                    }
                    if (realName2 != null) {
                    }
                    if (name4 == null) {
                    }
                    parentPackageName = name3;
                    primaryCpuAbiString = primaryCpuAbiString2;
                    name = isOrphaned;
                    updateAvailable = updateAvailable2;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    systemStr = str3;
                    idStr = sharedIdStr;
                    cpuAbiOverrideString = resourcePathStr2;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    idStr2 = installerPackageName3;
                    legacyNativeLibraryPathStr = volumeUuid;
                    categoryHint = categoryHint3;
                } catch (NumberFormatException e59) {
                    name2 = name4;
                    systemStr = " at ";
                    str = ATTR_NAME;
                    str2 = TemperatureProvider.SWITCH_ON;
                    sharedIdStr = idStr4;
                    idStr3 = " has bad userId ";
                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                    lastUpdateTime = 0;
                    i = 5;
                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                    PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                    idStr = sharedIdStr;
                    parentPackageName = name2;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    primaryCpuAbiString = primaryCpuAbiString2;
                    idStr2 = installerPackageName3;
                    name = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e60) {
                str2 = TemperatureProvider.SWITCH_ON;
                systemStr = " at ";
                str = ATTR_NAME;
                name2 = name4;
                sharedIdStr = idStr4;
                idStr3 = " has bad userId ";
                legacyNativeLibraryPathStr3 = null;
                secondaryCpuAbiString2 = null;
                codePathStr = null;
                lastUpdateTime = 0;
                i = 5;
                primaryCpuAbiString2 = null;
                PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
                idStr = sharedIdStr;
                parentPackageName = name2;
                secondaryCpuAbiString = secondaryCpuAbiString2;
                primaryCpuAbiString = primaryCpuAbiString2;
                idStr2 = installerPackageName3;
                name = isOrphaned;
                legacyNativeLibraryPathStr = volumeUuid;
                updateAvailable = updateAvailable2;
                categoryHint = categoryHint3;
                packageSetting = packageSetting2;
                uidError = uidError2;
                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                if (packageSetting != null) {
                }
            }
        } catch (NumberFormatException e61) {
            str2 = TemperatureProvider.SWITCH_ON;
            systemStr = " at ";
            str = ATTR_NAME;
            name2 = name4;
            sharedIdStr = null;
            idStr3 = " has bad userId ";
            i = 5;
            resourcePathStr = null;
            uidError2 = null;
            legacyNativeLibraryPathStr3 = null;
            secondaryCpuAbiString2 = null;
            codePathStr = null;
            lastUpdateTime = 0;
            primaryCpuAbiString2 = null;
            PackageManagerService.reportSettingsProblem(i, codePathStr2 + name2 + idStr3 + sharedIdStr + systemStr + parser.getPositionDescription());
            idStr = sharedIdStr;
            parentPackageName = name2;
            secondaryCpuAbiString = secondaryCpuAbiString2;
            primaryCpuAbiString = primaryCpuAbiString2;
            idStr2 = installerPackageName3;
            name = isOrphaned;
            legacyNativeLibraryPathStr = volumeUuid;
            updateAvailable = updateAvailable2;
            categoryHint = categoryHint3;
            packageSetting = packageSetting2;
            uidError = uidError2;
            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
            if (packageSetting != null) {
            }
        }
        if (packageSetting != null) {
        }
    }

    private void readDisabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addDisabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <disabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <disabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readEnabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addEnabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <enabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <enabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readSharedUserLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int pkgFlags = 0;
        SharedUserSetting su = null;
        try {
            String name = parser.getAttributeValue(null, ATTR_NAME);
            String idStr = parser.getAttributeValue(null, "userId");
            int userId = idStr != null ? Integer.parseInt(idStr) : 0;
            if (TemperatureProvider.SWITCH_ON.equals(parser.getAttributeValue(null, "system"))) {
                pkgFlags = 0 | 1;
            }
            if (name == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <shared-user> has no name at " + parser.getPositionDescription());
            } else if (userId == 0) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: shared-user " + name + " has bad userId " + idStr + " at " + parser.getPositionDescription());
            } else {
                SharedUserSetting addSharedUserLPw = addSharedUserLPw(name.intern(), userId, pkgFlags, 0);
                su = addSharedUserLPw;
                if (addSharedUserLPw == null) {
                    PackageManagerService.reportSettingsProblem(6, "Occurred while parsing settings at " + parser.getPositionDescription());
                }
            }
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + ((String) null) + " has bad userId " + ((String) null) + " at " + parser.getPositionDescription());
        }
        if (su != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String tagName = parser.getName();
                    if (tagName.equals("sigs")) {
                        su.signatures.readXml(parser, this.mPastSignatures);
                    } else if (tagName.equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, su.getPermissionsState());
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <shared-user>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        } else {
            XmlUtils.skipCurrentTag(parser);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b6, code lost:
        r8 = 0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0082  */
    public void createNewUserLI(PackageManagerService service, Installer installer, int userHandle, String[] disallowedPackages) {
        Throwable th;
        int packagesCount;
        String[] volumeUuids;
        String[] names;
        int[] appIds;
        String[] seinfos;
        int[] targetSdkVersions;
        int i;
        int i2;
        Installer.InstallerException e;
        Collection<PackageSetting> packages;
        boolean shouldInstall;
        boolean shouldInstall2;
        synchronized (this.mPackages) {
            try {
                Collection<PackageSetting> packages2 = this.mPackages.values();
                packagesCount = packages2.size();
                volumeUuids = new String[packagesCount];
                names = new String[packagesCount];
                appIds = new int[packagesCount];
                seinfos = new String[packagesCount];
                targetSdkVersions = new int[packagesCount];
                Iterator<PackageSetting> packagesIterator = packages2.iterator();
                int i3 = 0;
                while (i3 < packagesCount) {
                    try {
                        PackageSetting ps = packagesIterator.next();
                        if (ps.pkg == null) {
                            packages = packages2;
                        } else if (ps.pkg.applicationInfo == null) {
                            packages = packages2;
                        } else {
                            boolean isCustomDataApp = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isCustomDataApp(ps.name);
                            if (!ps.isSystem()) {
                                if (!isCustomDataApp) {
                                    shouldInstall = false;
                                    packages = packages2;
                                    shouldInstall2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldInstall(shouldInstall, userHandle, ps.name);
                                    ps.setInstalled(shouldInstall2, userHandle);
                                    if (!shouldInstall2) {
                                        writeKernelMappingLPr(ps);
                                    }
                                    volumeUuids[i3] = ps.volumeUuid;
                                    names[i3] = ps.name;
                                    appIds[i3] = ps.appId;
                                    seinfos[i3] = ps.pkg.applicationInfo.seInfo;
                                    targetSdkVersions[i3] = ps.pkg.applicationInfo.targetSdkVersion;
                                }
                            }
                            if (!ArrayUtils.contains(disallowedPackages, ps.name) && !ps.pkg.applicationInfo.hiddenUntilInstalled) {
                                shouldInstall = true;
                                packages = packages2;
                                shouldInstall2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldInstall(shouldInstall, userHandle, ps.name);
                                ps.setInstalled(shouldInstall2, userHandle);
                                if (!shouldInstall2) {
                                }
                                volumeUuids[i3] = ps.volumeUuid;
                                names[i3] = ps.name;
                                appIds[i3] = ps.appId;
                                seinfos[i3] = ps.pkg.applicationInfo.seInfo;
                                targetSdkVersions[i3] = ps.pkg.applicationInfo.targetSdkVersion;
                            }
                            shouldInstall = false;
                            packages = packages2;
                            shouldInstall2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldInstall(shouldInstall, userHandle, ps.name);
                            ps.setInstalled(shouldInstall2, userHandle);
                            if (!shouldInstall2) {
                            }
                            volumeUuids[i3] = ps.volumeUuid;
                            names[i3] = ps.name;
                            appIds[i3] = ps.appId;
                            seinfos[i3] = ps.pkg.applicationInfo.seInfo;
                            targetSdkVersions[i3] = ps.pkg.applicationInfo.targetSdkVersion;
                        }
                        i3++;
                        packages2 = packages;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        while (i < packagesCount) {
            if (names[i] == null) {
                i2 = i;
            } else if (userHandle != 999 || OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiUserInstallApp(names[i])) {
                try {
                    i2 = i;
                    try {
                        installer.createAppData(volumeUuids[i], names[i], userHandle, 3, appIds[i], seinfos[i], targetSdkVersions[i]);
                    } catch (Installer.InstallerException e2) {
                        e = e2;
                    }
                } catch (Installer.InstallerException e3) {
                    e = e3;
                    i2 = i;
                    Slog.w(TAG, "Failed to prepare app data", e);
                    i = i2 + 1;
                }
            } else {
                i2 = i;
            }
            i = i2 + 1;
        }
        synchronized (this.mPackages) {
            applyDefaultPreferredAppsLPw(userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserLPw(int userId) {
        for (Map.Entry<String, PackageSetting> entry : this.mPackages.entrySet()) {
            entry.getValue().removeUser(userId);
        }
        this.mPreferredActivities.remove(userId);
        getUserPackagesStateFile(userId).delete();
        getUserPackagesStateBackupFile(userId).delete();
        removeCrossProfileIntentFiltersLPw(userId);
        this.mRuntimePermissionsPersistence.onUserRemovedLPw(userId);
        writePackageListLPr();
        writeKernelRemoveUserLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void removeCrossProfileIntentFiltersLPw(int userId) {
        synchronized (this.mCrossProfileIntentResolvers) {
            if (this.mCrossProfileIntentResolvers.get(userId) != null) {
                this.mCrossProfileIntentResolvers.remove(userId);
                writePackageRestrictionsLPr(userId);
            }
            int count = this.mCrossProfileIntentResolvers.size();
            for (int i = 0; i < count; i++) {
                int sourceUserId = this.mCrossProfileIntentResolvers.keyAt(i);
                CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(sourceUserId);
                boolean needsWriting = false;
                Iterator<CrossProfileIntentFilter> it = new ArraySet<>(cpir.filterSet()).iterator();
                while (it.hasNext()) {
                    CrossProfileIntentFilter cpif = it.next();
                    if (cpif.getTargetUserId() == userId) {
                        needsWriting = true;
                        cpir.removeFilter(cpif);
                    }
                }
                if (needsWriting) {
                    writePackageRestrictionsLPr(sourceUserId);
                }
            }
        }
    }

    private void setFirstAvailableUid(int uid) {
        if (uid > mFirstAvailableUid) {
            mFirstAvailableUid = uid;
        }
    }

    private int acquireAndRegisterNewAppIdLPw(SettingBase obj) {
        int size = this.mAppIds.size();
        for (int i = mFirstAvailableUid; i < size; i++) {
            if (this.mAppIds.get(i) == null) {
                this.mAppIds.set(i, obj);
                return i + 10000;
            }
        }
        if (size > 9999) {
            return -1;
        }
        this.mAppIds.add(obj);
        return size + 10000;
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentityLPw() {
        if (this.mVerifierDeviceIdentity == null) {
            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.generate();
            writeLPr();
        }
        return this.mVerifierDeviceIdentity;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOtherDisabledSystemPkgWithChildLPr(String parentPackageName, String childPackageName) {
        int packageCount = this.mDisabledSysPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageSetting disabledPs = this.mDisabledSysPackages.valueAt(i);
            if (disabledPs.childPackageNames != null && !disabledPs.childPackageNames.isEmpty() && !disabledPs.name.equals(parentPackageName)) {
                int childCount = disabledPs.childPackageNames.size();
                for (int j = 0; j < childCount; j++) {
                    if (((String) disabledPs.childPackageNames.get(j)).equals(childPackageName)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public PackageSetting getDisabledSystemPkgLPr(String name) {
        return this.mDisabledSysPackages.get(name);
    }

    public PackageSetting getDisabledSystemPkgLPr(PackageSetting enabledPackageSetting) {
        if (enabledPackageSetting == null) {
            return null;
        }
        return getDisabledSystemPkgLPr(enabledPackageSetting.name);
    }

    public PackageSetting[] getChildSettingsLPr(PackageSetting parentPackageSetting) {
        if (parentPackageSetting == null || !parentPackageSetting.hasChildPackages()) {
            return null;
        }
        int childCount = parentPackageSetting.childPackageNames.size();
        PackageSetting[] children = new PackageSetting[childCount];
        for (int i = 0; i < childCount; i++) {
            children[i] = this.mPackages.get(parentPackageSetting.childPackageNames.get(i));
        }
        return children;
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabledAndMatchLPr(ComponentInfo componentInfo, int flags, int userId) {
        PackageSetting ps = this.mPackages.get(componentInfo.packageName);
        if (ps == null) {
            return false;
        }
        if (ps.readUserState(userId).isMatch(componentInfo, flags)) {
            return true;
        }
        PackageUserState userState2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getPackageUserState(userId, ps);
        if (userState2 == null || !userState2.isMatch(componentInfo, flags)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public String getInstallerPackageNameLPr(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.installerPackageName;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean isOrphaned(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.isOrphaned;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getApplicationEnabledSettingLPr(String packageName, int userId) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.getEnabled(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getComponentEnabledSettingLPr(ComponentName componentName, int userId) {
        PackageSetting pkg = this.mPackages.get(componentName.getPackageName());
        if (pkg != null) {
            return pkg.getCurrentEnabledStateLPr(componentName.getClassName(), userId);
        }
        throw new IllegalArgumentException("Unknown component: " + componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean wasPackageEverLaunchedLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return !pkgSetting.getNotLaunched(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean setPackageStoppedStateLPw(PackageManagerService pm, String packageName, boolean stopped, boolean allowedByPermission, int uid, int userId) {
        int appId = UserHandle.getAppId(uid);
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!allowedByPermission && appId != pkgSetting.appId) {
            throw new SecurityException("Permission Denial: attempt to change stopped state from pid=" + Binder.getCallingPid() + ", uid=" + uid + ", package uid=" + pkgSetting.appId);
        } else if (pkgSetting.getStopped(userId) == stopped) {
            return false;
        } else {
            pkgSetting.setStopped(stopped, userId);
            if (!pkgSetting.getNotLaunched(userId)) {
                return true;
            }
            if (pkgSetting.installerPackageName != null) {
                pm.notifyFirstLaunch(pkgSetting.name, pkgSetting.installerPackageName, userId);
            }
            pkgSetting.setNotLaunched(false, userId);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHarmfulAppWarningLPw(String packageName, CharSequence warning, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            pkgSetting.setHarmfulAppWarning(userId, warning == null ? null : warning.toString());
            return;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public String getHarmfulAppWarningLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return pkgSetting.getHarmfulAppWarning(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    private static List<UserInfo> getAllUsers(UserManagerService userManager) {
        return getUsers(userManager, false);
    }

    /* JADX INFO: finally extract failed */
    private static List<UserInfo> getUsers(UserManagerService userManager, boolean excludeDying) {
        long id = Binder.clearCallingIdentity();
        try {
            List<UserInfo> users = userManager.getUsers(excludeDying);
            Binder.restoreCallingIdentity(id);
            return users;
        } catch (NullPointerException e) {
            Binder.restoreCallingIdentity(id);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public List<PackageSetting> getVolumePackagesLPr(String volumeUuid) {
        ArrayList<PackageSetting> res = new ArrayList<>();
        for (int i = 0; i < this.mPackages.size(); i++) {
            PackageSetting setting = this.mPackages.valueAt(i);
            if (Objects.equals(volumeUuid, setting.volumeUuid)) {
                res.add(setting);
            }
        }
        return res;
    }

    static void printFlags(PrintWriter pw, int val, Object[] spec) {
        pw.print("[ ");
        for (int i = 0; i < spec.length; i += 2) {
            if ((val & ((Integer) spec[i]).intValue()) != 0) {
                pw.print(spec[i + 1]);
                pw.print(StringUtils.SPACE);
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpVersionLPr(IndentingPrintWriter pw) {
        pw.increaseIndent();
        for (int i = 0; i < this.mVersion.size(); i++) {
            String volumeUuid = this.mVersion.keyAt(i);
            VersionInfo ver = this.mVersion.valueAt(i);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
                pw.println("Internal:");
            } else if (Objects.equals("primary_physical", volumeUuid)) {
                pw.println("External:");
            } else {
                pw.println("UUID " + volumeUuid + ":");
            }
            pw.increaseIndent();
            pw.printPair(ATTR_SDK_VERSION, Integer.valueOf(ver.sdkVersion));
            pw.printPair(ATTR_DATABASE_VERSION, Integer.valueOf(ver.databaseVersion));
            pw.println();
            pw.printPair(ATTR_FINGERPRINT, ver.fingerprint);
            pw.println();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    /* access modifiers changed from: package-private */
    public void dumpPackageLPr(PrintWriter pw, String prefix, String checkinTag, ArraySet<String> permissionNames, PackageSetting ps, SimpleDateFormat sdf, Date date, List<UserInfo> users, boolean dumpAll, boolean dumpAllComponents) {
        String str;
        if (checkinTag != null) {
            pw.print(checkinTag);
            pw.print(",");
            pw.print(ps.realName != null ? ps.realName : ps.name);
            pw.print(",");
            pw.print(ps.appId);
            pw.print(",");
            pw.print(ps.versionCode);
            pw.print(",");
            pw.print(ps.firstInstallTime);
            pw.print(",");
            pw.print(ps.lastUpdateTime);
            pw.print(",");
            pw.print(ps.installerPackageName != null ? ps.installerPackageName : "?");
            pw.println();
            if (ps.pkg != null) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("splt,");
                pw.print("base,");
                pw.println(ps.pkg.baseRevisionCode);
                if (ps.pkg.splitNames != null) {
                    for (int i = 0; i < ps.pkg.splitNames.length; i++) {
                        pw.print(checkinTag);
                        pw.print("-");
                        pw.print("splt,");
                        pw.print(ps.pkg.splitNames[i]);
                        pw.print(",");
                        pw.println(ps.pkg.splitRevisionCodes[i]);
                    }
                }
            }
            for (UserInfo user : users) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("usr");
                pw.print(",");
                pw.print(user.id);
                pw.print(",");
                pw.print(ps.getInstalled(user.id) ? "I" : "i");
                pw.print(ps.getHidden(user.id) ? "B" : "b");
                pw.print(ps.getSuspended(user.id) ? "SU" : "su");
                pw.print(ps.getStopped(user.id) ? "S" : "s");
                pw.print(ps.getNotLaunched(user.id) ? "l" : "L");
                pw.print(ps.getInstantApp(user.id) ? "IA" : "ia");
                pw.print(ps.getVirtulalPreload(user.id) ? "VPI" : "vpi");
                pw.print(ps.getHarmfulAppWarning(user.id) != null ? "HA" : "ha");
                pw.print(",");
                pw.print(ps.getEnabled(user.id));
                String lastDisabledAppCaller = ps.getLastDisabledAppCaller(user.id);
                pw.print(",");
                if (lastDisabledAppCaller != null) {
                    str = lastDisabledAppCaller;
                } else {
                    str = "?";
                }
                pw.print(str);
                pw.print(",");
                pw.println();
            }
            return;
        }
        pw.print(prefix);
        pw.print("Package [");
        pw.print(ps.realName != null ? ps.realName : ps.name);
        pw.print("] (");
        pw.print(Integer.toHexString(System.identityHashCode(ps)));
        pw.println("):");
        if (ps.realName != null) {
            pw.print(prefix);
            pw.print("  compat name=");
            pw.println(ps.name);
        }
        pw.print(prefix);
        pw.print("  userId=");
        pw.println(ps.appId);
        if (ps.sharedUser != null) {
            pw.print(prefix);
            pw.print("  sharedUser=");
            pw.println(ps.sharedUser);
        }
        pw.print(prefix);
        pw.print("  pkg=");
        pw.println(ps.pkg);
        pw.print(prefix);
        pw.print("  codePath=");
        pw.println(ps.codePathString);
        if (permissionNames == null) {
            pw.print(prefix);
            pw.print("  resourcePath=");
            pw.println(ps.resourcePathString);
            pw.print(prefix);
            pw.print("  legacyNativeLibraryDir=");
            pw.println(ps.legacyNativeLibraryPathString);
            pw.print(prefix);
            pw.print("  primaryCpuAbi=");
            pw.println(ps.primaryCpuAbiString);
            pw.print(prefix);
            pw.print("  secondaryCpuAbi=");
            pw.println(ps.secondaryCpuAbiString);
        }
        pw.print(prefix);
        pw.print("  versionCode=");
        pw.print(ps.versionCode);
        if (ps.pkg != null) {
            pw.print(" minSdk=");
            pw.print(ps.pkg.applicationInfo.minSdkVersion);
            pw.print(" targetSdk=");
            pw.print(ps.pkg.applicationInfo.targetSdkVersion);
        }
        pw.println();
        if (ps.pkg != null) {
            if (ps.pkg.parentPackage != null) {
                PackageParser.Package parentPkg = ps.pkg.parentPackage;
                PackageSetting pps = this.mPackages.get(parentPkg.packageName);
                if (pps == null || !pps.codePathString.equals(parentPkg.codePath)) {
                    pps = this.mDisabledSysPackages.get(parentPkg.packageName);
                }
                if (pps != null) {
                    pw.print(prefix);
                    pw.print("  parentPackage=");
                    pw.println(pps.realName != null ? pps.realName : pps.name);
                }
            } else if (ps.pkg.childPackages != null) {
                pw.print(prefix);
                pw.print("  childPackages=[");
                int childCount = ps.pkg.childPackages.size();
                for (int i2 = 0; i2 < childCount; i2++) {
                    PackageParser.Package childPkg = (PackageParser.Package) ps.pkg.childPackages.get(i2);
                    PackageSetting cps = this.mPackages.get(childPkg.packageName);
                    if (cps == null || !cps.codePathString.equals(childPkg.codePath)) {
                        cps = this.mDisabledSysPackages.get(childPkg.packageName);
                    }
                    if (cps != null) {
                        if (i2 > 0) {
                            pw.print(", ");
                        }
                        pw.print(cps.realName != null ? cps.realName : cps.name);
                    }
                }
                pw.println("]");
            }
            pw.print(prefix);
            pw.print("  versionName=");
            pw.println(ps.pkg.mVersionName);
            pw.print(prefix);
            pw.print("  splits=");
            dumpSplitNames(pw, ps.pkg);
            pw.println();
            int apkSigningVersion = ps.pkg.mSigningDetails.signatureSchemeVersion;
            pw.print(prefix);
            pw.print("  apkSigningVersion=");
            pw.println(apkSigningVersion);
            pw.print(prefix);
            pw.print("  applicationInfo=");
            pw.println(ps.pkg.applicationInfo.toString());
            pw.print(prefix);
            pw.print("  flags=");
            printFlags(pw, ps.pkg.applicationInfo.flags, FLAG_DUMP_SPEC);
            pw.println();
            if (ps.pkg.applicationInfo.privateFlags != 0) {
                pw.print(prefix);
                pw.print("  privateFlags=");
                printFlags(pw, ps.pkg.applicationInfo.privateFlags, PRIVATE_FLAG_DUMP_SPEC);
                pw.println();
            }
            pw.print(prefix);
            pw.print("  dataDir=");
            pw.println(ps.pkg.applicationInfo.dataDir);
            pw.print(prefix);
            pw.print("  supportsScreens=[");
            boolean first = true;
            if ((ps.pkg.applicationInfo.flags & 512) != 0) {
                if (1 == 0) {
                    pw.print(", ");
                }
                first = false;
                pw.print("small");
            }
            if ((ps.pkg.applicationInfo.flags & 1024) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("medium");
            }
            if ((ps.pkg.applicationInfo.flags & 2048) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("large");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_FROZEN) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("xlarge");
            }
            if ((ps.pkg.applicationInfo.flags & 4096) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("resizeable");
            }
            if ((ps.pkg.applicationInfo.flags & 8192) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print("anyDensity");
            }
            pw.println("]");
            if (ps.pkg.libraryNames != null && ps.pkg.libraryNames.size() > 0) {
                pw.print(prefix);
                pw.println("  dynamic libraries:");
                for (int i3 = 0; i3 < ps.pkg.libraryNames.size(); i3++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.libraryNames.get(i3));
                }
            }
            if (ps.pkg.staticSharedLibName != null) {
                pw.print(prefix);
                pw.println("  static library:");
                pw.print(prefix);
                pw.print("    ");
                pw.print("name:");
                pw.print(ps.pkg.staticSharedLibName);
                pw.print(" version:");
                pw.println(ps.pkg.staticSharedLibVersion);
            }
            if (ps.pkg.usesLibraries != null && ps.pkg.usesLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesLibraries:");
                for (int i4 = 0; i4 < ps.pkg.usesLibraries.size(); i4++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesLibraries.get(i4));
                }
            }
            if (ps.pkg.usesStaticLibraries != null && ps.pkg.usesStaticLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesStaticLibraries:");
                for (int i5 = 0; i5 < ps.pkg.usesStaticLibraries.size(); i5++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print((String) ps.pkg.usesStaticLibraries.get(i5));
                    pw.print(" version:");
                    pw.println(ps.pkg.usesStaticLibrariesVersions[i5]);
                }
            }
            if (ps.pkg.usesOptionalLibraries != null && ps.pkg.usesOptionalLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesOptionalLibraries:");
                for (int i6 = 0; i6 < ps.pkg.usesOptionalLibraries.size(); i6++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesOptionalLibraries.get(i6));
                }
            }
            if (ps.pkg.usesLibraryFiles != null && ps.pkg.usesLibraryFiles.length > 0) {
                pw.print(prefix);
                pw.println("  usesLibraryFiles:");
                for (int i7 = 0; i7 < ps.pkg.usesLibraryFiles.length; i7++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(ps.pkg.usesLibraryFiles[i7]);
                }
            }
        }
        pw.print(prefix);
        pw.print("  timeStamp=");
        date.setTime(ps.timeStamp);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  firstInstallTime=");
        date.setTime(ps.firstInstallTime);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  lastUpdateTime=");
        date.setTime(ps.lastUpdateTime);
        pw.println(sdf.format(date));
        if (ps.installerPackageName != null) {
            pw.print(prefix);
            pw.print("  installerPackageName=");
            pw.println(ps.installerPackageName);
        }
        if (ps.volumeUuid != null) {
            pw.print(prefix);
            pw.print("  volumeUuid=");
            pw.println(ps.volumeUuid);
        }
        pw.print(prefix);
        pw.print("  signatures=");
        pw.println(ps.signatures);
        pw.print(prefix);
        pw.print("  installPermissionsFixed=");
        pw.print(ps.installPermissionsFixed);
        pw.println();
        pw.print(prefix);
        pw.print("  pkgFlags=");
        printFlags(pw, ps.pkgFlags, FLAG_DUMP_SPEC);
        pw.println();
        if (!(ps.pkg == null || ps.pkg.mOverlayTarget == null)) {
            pw.print(prefix);
            pw.print("  overlayTarget=");
            pw.println(ps.pkg.mOverlayTarget);
            pw.print(prefix);
            pw.print("  overlayCategory=");
            pw.println(ps.pkg.mOverlayCategory);
        }
        if (!(ps.pkg == null || ps.pkg.permissions == null || ps.pkg.permissions.size() <= 0)) {
            ArrayList<PackageParser.Permission> perms = ps.pkg.permissions;
            pw.print(prefix);
            pw.println("  declared permissions:");
            for (int i8 = 0; i8 < perms.size(); i8++) {
                PackageParser.Permission perm = perms.get(i8);
                if (permissionNames == null || permissionNames.contains(perm.info.name)) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(perm.info.name);
                    pw.print(": prot=");
                    pw.print(PermissionInfo.protectionToString(perm.info.protectionLevel));
                    if ((perm.info.flags & 1) != 0) {
                        pw.print(", COSTS_MONEY");
                    }
                    if ((perm.info.flags & 2) != 0) {
                        pw.print(", HIDDEN");
                    }
                    if ((perm.info.flags & 1073741824) != 0) {
                        pw.print(", INSTALLED");
                    }
                    pw.println();
                }
            }
        }
        if ((permissionNames != null || dumpAll) && ps.pkg != null && ps.pkg.requestedPermissions != null && ps.pkg.requestedPermissions.size() > 0) {
            ArrayList<String> perms2 = ps.pkg.requestedPermissions;
            pw.print(prefix);
            pw.println("  requested permissions:");
            for (int i9 = 0; i9 < perms2.size(); i9++) {
                String perm2 = perms2.get(i9);
                if (permissionNames == null || permissionNames.contains(perm2)) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(perm2);
                    BasePermission bp = this.mPermissions.getPermission(perm2);
                    if (bp == null || !bp.isHardOrSoftRestricted()) {
                        pw.println();
                    } else {
                        pw.println(": restricted=true");
                    }
                }
            }
        }
        if (ps.sharedUser == null || permissionNames != null || dumpAll) {
            dumpInstallPermissionsLPr(pw, prefix + "  ", permissionNames, ps.getPermissionsState());
        }
        if (dumpAllComponents) {
            dumpComponents(pw, prefix + "  ", ps);
        }
        for (UserInfo user2 : users) {
            pw.print(prefix);
            pw.print("  User ");
            pw.print(user2.id);
            pw.print(": ");
            pw.print("ceDataInode=");
            pw.print(ps.getCeDataInode(user2.id));
            pw.print(" installed=");
            pw.print(ps.getInstalled(user2.id));
            pw.print(" hidden=");
            pw.print(ps.getHidden(user2.id));
            pw.print(" suspended=");
            pw.print(ps.getSuspended(user2.id));
            if (ps.getSuspended(user2.id)) {
                PackageUserState pus = ps.readUserState(user2.id);
                pw.print(" suspendingPackage=");
                pw.print(pus.suspendingPackage);
                pw.print(" dialogInfo=");
                pw.print(pus.dialogInfo);
            }
            pw.print(" stopped=");
            pw.print(ps.getStopped(user2.id));
            pw.print(" notLaunched=");
            pw.print(ps.getNotLaunched(user2.id));
            pw.print(" enabled=");
            pw.print(ps.getEnabled(user2.id));
            pw.print(" ofs=");
            pw.print(ps.getOppoFreezeState(user2.id));
            pw.print(" instant=");
            pw.print(ps.getInstantApp(user2.id));
            pw.print(" virtual=");
            pw.println(ps.getVirtulalPreload(user2.id));
            String[] overlayPaths = ps.getOverlayPaths(user2.id);
            if (overlayPaths != null && overlayPaths.length > 0) {
                pw.print(prefix);
                pw.println("  overlay paths:");
                for (String path : overlayPaths) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(path);
                }
            }
            String lastDisabledAppCaller2 = ps.getLastDisabledAppCaller(user2.id);
            if (lastDisabledAppCaller2 != null) {
                pw.print(prefix);
                pw.print("    lastDisabledCaller: ");
                pw.println(lastDisabledAppCaller2);
            }
            if (ps.sharedUser == null) {
                PermissionsState permissionsState = ps.getPermissionsState();
                dumpGidsLPr(pw, prefix + "    ", permissionsState.computeGids(user2.id));
                dumpRuntimePermissionsLPr(pw, prefix + "    ", permissionNames, permissionsState.getRuntimePermissionStates(user2.id), dumpAll);
            }
            String harmfulAppWarning = ps.getHarmfulAppWarning(user2.id);
            if (harmfulAppWarning != null) {
                pw.print(prefix);
                pw.print("      harmfulAppWarning: ");
                pw.println(harmfulAppWarning);
            }
            if (permissionNames == null) {
                ArraySet<String> cmp = ps.getDisabledComponents(user2.id);
                if (cmp != null && cmp.size() > 0) {
                    pw.print(prefix);
                    pw.println("    disabledComponents:");
                    Iterator<String> it = cmp.iterator();
                    while (it.hasNext()) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(it.next());
                    }
                }
                ArraySet<String> cmp2 = ps.getEnabledComponents(user2.id);
                if (cmp2 != null && cmp2.size() > 0) {
                    pw.print(prefix);
                    pw.println("    enabledComponents:");
                    Iterator<String> it2 = cmp2.iterator();
                    while (it2.hasNext()) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(it2.next());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        boolean printedSomething;
        PrintWriter printWriter = pw;
        String str = packageName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        boolean printedSomething2 = false;
        boolean dumpAllComponents = dumpState.isOptionEnabled(2);
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        Iterator<PackageSetting> it = this.mPackages.values().iterator();
        while (true) {
            String str2 = null;
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = it.next();
            if ((str == null || str.equals(ps.realName) || str.equals(ps.name)) && (permissionNames == null || ps.getPermissionsState().hasRequestedPermission(permissionNames))) {
                if (!checkin && str != null) {
                    dumpState.setSharedUser(ps.sharedUser);
                }
                if (checkin || printedSomething2) {
                    printedSomething = printedSomething2;
                } else {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    printWriter.println("Packages:");
                    printedSomething = true;
                }
                if (checkin) {
                    str2 = TAG_PACKAGE;
                }
                dumpPackageLPr(pw, "  ", str2, permissionNames, ps, sdf, date, users, str != null, dumpAllComponents);
                printedSomething2 = printedSomething;
            }
        }
        boolean printedSomething3 = false;
        if (this.mRenamedPackages.size() > 0 && permissionNames == null) {
            for (Map.Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                if (str == null || str.equals(e.getKey()) || str.equals(e.getValue())) {
                    if (!checkin) {
                        if (!printedSomething3) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            printWriter.println("Renamed packages:");
                            printedSomething3 = true;
                        }
                        printWriter.print("  ");
                    } else {
                        printWriter.print("ren,");
                    }
                    printWriter.print(e.getKey());
                    printWriter.print(checkin ? " -> " : ",");
                    printWriter.println(e.getValue());
                }
            }
        }
        boolean printedSomething4 = false;
        if (this.mDisabledSysPackages.size() > 0 && permissionNames == null) {
            for (PackageSetting ps2 : this.mDisabledSysPackages.values()) {
                if (str == null || str.equals(ps2.realName) || str.equals(ps2.name)) {
                    if (!checkin && !printedSomething4) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        printWriter.println("Hidden system packages:");
                        printedSomething4 = true;
                    }
                    dumpPackageLPr(pw, "  ", checkin ? "dis" : null, permissionNames, ps2, sdf, date, users, str != null, dumpAllComponents);
                    printWriter = pw;
                    str = packageName;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesProto(ProtoOutputStream proto) {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        int count = this.mPackages.size();
        for (int i = 0; i < count; i++) {
            this.mPackages.valueAt(i).writeToProto(proto, 2246267895813L, users);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPermissionsLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState) {
        this.mPermissions.dumpPermissions(pw, packageName, permissionNames, this.mReadExternalStorageEnforced == Boolean.TRUE, dumpState);
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        boolean printedSomething;
        PermissionsState permissionsState;
        int[] iArr;
        int i;
        int i2;
        boolean printedSomething2 = false;
        for (SharedUserSetting su : this.mSharedUsers.values()) {
            if ((packageName == null || su == dumpState.getSharedUser()) && (permissionNames == null || su.getPermissionsState().hasRequestedPermission(permissionNames))) {
                if (!checkin) {
                    if (!printedSomething2) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("Shared users:");
                        printedSomething = true;
                    } else {
                        printedSomething = printedSomething2;
                    }
                    pw.print("  SharedUser [");
                    pw.print(su.name);
                    pw.print("] (");
                    pw.print(Integer.toHexString(System.identityHashCode(su)));
                    pw.println("):");
                    pw.print("    ");
                    pw.print("userId=");
                    pw.println(su.userId);
                    pw.print("    ");
                    pw.println("Packages");
                    int numPackages = su.packages.size();
                    for (int i3 = 0; i3 < numPackages; i3++) {
                        PackageSetting ps = su.packages.valueAt(i3);
                        if (ps != null) {
                            pw.print("      ");
                            pw.println(ps.toString());
                        } else {
                            pw.print("      ");
                            pw.println("NULL?!");
                        }
                    }
                    if (dumpState.isOptionEnabled(4)) {
                        printedSomething2 = printedSomething;
                    } else {
                        PermissionsState permissionsState2 = su.getPermissionsState();
                        dumpInstallPermissionsLPr(pw, "    ", permissionNames, permissionsState2);
                        int[] userIds = UserManagerService.getInstance().getUserIds();
                        int length = userIds.length;
                        int i4 = 0;
                        while (i4 < length) {
                            int userId = userIds[i4];
                            int[] gids = permissionsState2.computeGids(userId);
                            List<PermissionsState.PermissionState> permissions = permissionsState2.getRuntimePermissionStates(userId);
                            if (!ArrayUtils.isEmpty(gids) || !permissions.isEmpty()) {
                                pw.print("    ");
                                i2 = i4;
                                pw.print("User ");
                                pw.print(userId);
                                pw.println(": ");
                                dumpGidsLPr(pw, "      ", gids);
                                i = length;
                                iArr = userIds;
                                permissionsState = permissionsState2;
                                dumpRuntimePermissionsLPr(pw, "      ", permissionNames, permissions, packageName != null);
                            } else {
                                i2 = i4;
                                i = length;
                                iArr = userIds;
                                permissionsState = permissionsState2;
                            }
                            i4 = i2 + 1;
                            length = i;
                            userIds = iArr;
                            permissionsState2 = permissionsState;
                        }
                        printedSomething2 = printedSomething;
                    }
                } else {
                    pw.print("suid,");
                    pw.print(su.userId);
                    pw.print(",");
                    pw.println(su.name);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersProto(ProtoOutputStream proto) {
        int count = this.mSharedUsers.size();
        for (int i = 0; i < count; i++) {
            this.mSharedUsers.valueAt(i).writeToProto(proto, 2246267895814L);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpReadMessagesLPr(PrintWriter pw, DumpState dumpState) {
        pw.println("Settings parse messages:");
        pw.print(this.mReadMessages.toString());
    }

    private static void dumpSplitNames(PrintWriter pw, PackageParser.Package pkg) {
        if (pkg == null) {
            pw.print(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            return;
        }
        pw.print("[");
        pw.print("base");
        if (pkg.baseRevisionCode != 0) {
            pw.print(":");
            pw.print(pkg.baseRevisionCode);
        }
        if (pkg.splitNames != null) {
            for (int i = 0; i < pkg.splitNames.length; i++) {
                pw.print(", ");
                pw.print(pkg.splitNames[i]);
                if (pkg.splitRevisionCodes[i] != 0) {
                    pw.print(":");
                    pw.print(pkg.splitRevisionCodes[i]);
                }
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpGidsLPr(PrintWriter pw, String prefix, int[] gids) {
        if (!ArrayUtils.isEmpty(gids)) {
            pw.print(prefix);
            pw.print("gids=");
            pw.println(PackageManagerService.arrayToString(gids));
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpRuntimePermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, List<PermissionsState.PermissionState> permissionStates, boolean dumpAll) {
        if (!permissionStates.isEmpty() || dumpAll) {
            pw.print(prefix);
            pw.println("runtime permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    private static String permissionFlagsToString(String prefix, int flags) {
        StringBuilder flagsString = null;
        while (flags != 0) {
            if (flagsString == null) {
                flagsString = new StringBuilder();
                flagsString.append(prefix);
                flagsString.append("[ ");
            }
            int flag = 1 << Integer.numberOfTrailingZeros(flags);
            flags &= ~flag;
            flagsString.append(PackageManager.permissionFlagToString(flag));
            if (flags != 0) {
                flagsString.append('|');
            }
        }
        if (flagsString == null) {
            return "";
        }
        flagsString.append(']');
        return flagsString.toString();
    }

    /* access modifiers changed from: package-private */
    public void dumpInstallPermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, PermissionsState permissionsState) {
        List<PermissionsState.PermissionState> permissionStates = permissionsState.getInstallPermissionStates();
        if (!permissionStates.isEmpty()) {
            pw.print(prefix);
            pw.println("install permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpComponents(PrintWriter pw, String prefix, PackageSetting ps) {
        dumpComponents(pw, prefix, ps, "activities:", ps.pkg.activities);
        dumpComponents(pw, prefix, ps, "services:", ps.pkg.services);
        dumpComponents(pw, prefix, ps, "receivers:", ps.pkg.receivers);
        dumpComponents(pw, prefix, ps, "providers:", ps.pkg.providers);
        dumpComponents(pw, prefix, ps, "instrumentations:", ps.pkg.instrumentation);
    }

    /* access modifiers changed from: package-private */
    public void dumpComponents(PrintWriter pw, String prefix, PackageSetting ps, String label, List<? extends PackageParser.Component<?>> list) {
        int size = CollectionUtils.size(list);
        if (size != 0) {
            pw.print(prefix);
            pw.println(label);
            for (int i = 0; i < size; i++) {
                pw.print(prefix);
                pw.print("  ");
                pw.println(((PackageParser.Component) list.get(i)).getComponentName().flattenToShortString());
            }
        }
    }

    public void writeRuntimePermissionsForUserLPr(int userId, boolean sync) {
        if (sync) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserSyncLPr(userId);
        } else {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    /* access modifiers changed from: private */
    public final class RuntimePermissionPersistence {
        private static final int INITIAL_VERSION = 0;
        private static final long MAX_WRITE_PERMISSIONS_DELAY_MILLIS = 2000;
        private static final int UPGRADE_VERSION = -1;
        private static final long WRITE_PERMISSIONS_DELAY_MILLIS = 200;
        @GuardedBy({"mLock"})
        private final SparseBooleanArray mDefaultPermissionsGranted = new SparseBooleanArray();
        @GuardedBy({"mLock"})
        private final SparseArray<String> mFingerprints = new SparseArray<>();
        private final Handler mHandler = new MyHandler();
        @GuardedBy({"mLock"})
        private final SparseLongArray mLastNotWrittenMutationTimesMillis = new SparseLongArray();
        private final Object mPersistenceLock;
        @GuardedBy({"mLock"})
        private final SparseIntArray mVersions = new SparseIntArray();
        @GuardedBy({"mLock"})
        private final SparseBooleanArray mWriteScheduled = new SparseBooleanArray();

        public RuntimePermissionPersistence(Object persistenceLock) {
            this.mPersistenceLock = persistenceLock;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"Settings.this.mLock"})
        public int getVersionLPr(int userId) {
            return this.mVersions.get(userId, 0);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"Settings.this.mLock"})
        public void setVersionLPr(int version, int userId) {
            this.mVersions.put(userId, version);
            writePermissionsForUserAsyncLPr(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
            return this.mDefaultPermissionsGranted.get(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public void setRuntimePermissionsFingerPrintLPr(String fingerPrint, int userId) {
            this.mFingerprints.put(userId, fingerPrint);
            writePermissionsForUserAsyncLPr(userId);
        }

        public void writePermissionsForUserSyncLPr(int userId) {
            this.mHandler.removeMessages(userId);
            writePermissionsSync(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public void writePermissionsForUserAsyncLPr(int userId) {
            long currentTimeMillis = SystemClock.uptimeMillis();
            if (this.mWriteScheduled.get(userId)) {
                this.mHandler.removeMessages(userId);
                long lastNotWrittenMutationTimeMillis = this.mLastNotWrittenMutationTimesMillis.get(userId);
                if (currentTimeMillis - lastNotWrittenMutationTimeMillis >= MAX_WRITE_PERMISSIONS_DELAY_MILLIS) {
                    this.mHandler.obtainMessage(userId).sendToTarget();
                    return;
                }
                long writeDelayMillis = Math.min((long) WRITE_PERMISSIONS_DELAY_MILLIS, Math.max((MAX_WRITE_PERMISSIONS_DELAY_MILLIS + lastNotWrittenMutationTimeMillis) - currentTimeMillis, 0L));
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), writeDelayMillis);
                return;
            }
            this.mLastNotWrittenMutationTimesMillis.put(userId, currentTimeMillis);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), WRITE_PERMISSIONS_DELAY_MILLIS);
            this.mWriteScheduled.put(userId, true);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void writePermissionsSync(int userId) {
            File userRuntimePermissionsFile = Settings.this.getUserRuntimePermissionsFile(userId);
            AtomicFile destination = new AtomicFile(userRuntimePermissionsFile, "package-perms-" + userId);
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForPackage = new ArrayMap<>();
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForSharedUser = new ArrayMap<>();
            synchronized (this.mPersistenceLock) {
                this.mWriteScheduled.delete(userId);
                int packageCount = Settings.this.mPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    String packageName = Settings.this.mPackages.keyAt(i);
                    PackageSetting packageSetting = Settings.this.mPackages.valueAt(i);
                    if (packageSetting.sharedUser == null) {
                        List<PermissionsState.PermissionState> permissionsStates = packageSetting.getPermissionsState().getRuntimePermissionStates(userId);
                        if (!permissionsStates.isEmpty()) {
                            permissionsForPackage.put(packageName, permissionsStates);
                        }
                    }
                }
                int sharedUserCount = Settings.this.mSharedUsers.size();
                for (int i2 = 0; i2 < sharedUserCount; i2++) {
                    String sharedUserName = Settings.this.mSharedUsers.keyAt(i2);
                    List<PermissionsState.PermissionState> permissionsStates2 = Settings.this.mSharedUsers.valueAt(i2).getPermissionsState().getRuntimePermissionStates(userId);
                    if (!permissionsStates2.isEmpty()) {
                        permissionsForSharedUser.put(sharedUserName, permissionsStates2);
                    }
                }
            }
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                serializer.attribute(null, "version", Integer.toString(this.mVersions.get(userId, 0)));
                String fingerprint = this.mFingerprints.get(userId);
                if (fingerprint != null) {
                    serializer.attribute(null, Settings.ATTR_FINGERPRINT, fingerprint);
                }
                int packageCount2 = permissionsForPackage.size();
                for (int i3 = 0; i3 < packageCount2; i3++) {
                    serializer.startTag(null, Settings.TAG_PACKAGE);
                    serializer.attribute(null, Settings.ATTR_NAME, permissionsForPackage.keyAt(i3));
                    writePermissions(serializer, permissionsForPackage.valueAt(i3));
                    serializer.endTag(null, Settings.TAG_PACKAGE);
                }
                int sharedUserCount2 = permissionsForSharedUser.size();
                for (int i4 = 0; i4 < sharedUserCount2; i4++) {
                    serializer.startTag(null, Settings.TAG_SHARED_USER);
                    serializer.attribute(null, Settings.ATTR_NAME, permissionsForSharedUser.keyAt(i4));
                    writePermissions(serializer, permissionsForSharedUser.valueAt(i4));
                    serializer.endTag(null, Settings.TAG_SHARED_USER);
                }
                serializer.endTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                serializer.endDocument();
                destination.finishWrite(out);
                if (Build.FINGERPRINT.equals(fingerprint)) {
                    this.mDefaultPermissionsGranted.put(userId, true);
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
            IoUtils.closeQuietly(out);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"Settings.this.mLock"})
        private void onUserRemovedLPw(int userId) {
            this.mHandler.removeMessages(userId);
            for (SettingBase sb : Settings.this.mPackages.values()) {
                revokeRuntimePermissionsAndClearFlags(sb, userId);
            }
            for (SettingBase sb2 : Settings.this.mSharedUsers.values()) {
                revokeRuntimePermissionsAndClearFlags(sb2, userId);
            }
            this.mDefaultPermissionsGranted.delete(userId);
            this.mVersions.delete(userId);
            this.mFingerprints.remove(userId);
        }

        private void revokeRuntimePermissionsAndClearFlags(SettingBase sb, int userId) {
            PermissionsState permissionsState = sb.getPermissionsState();
            for (PermissionsState.PermissionState permissionState : permissionsState.getRuntimePermissionStates(userId)) {
                BasePermission bp = Settings.this.mPermissions.getPermission(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeRuntimePermission(bp, userId);
                    permissionsState.updatePermissionFlags(bp, userId, 64511, 0);
                }
            }
        }

        public void deleteUserRuntimePermissionsFile(int userId) {
            Settings.this.getUserRuntimePermissionsFile(userId).delete();
        }

        /*  JADX ERROR: IndexOutOfBoundsException in pass: DeboxingVisitor
            java.lang.IndexOutOfBoundsException: Index: -1
            	at java.util.Collections$EmptyList.get(Collections.java:4454)
            	at jadx.core.dex.visitors.ConstInlineVisitor.needExplicitCast(ConstInlineVisitor.java:251)
            	at jadx.core.dex.visitors.ConstInlineVisitor.replaceArg(ConstInlineVisitor.java:230)
            	at jadx.core.dex.visitors.ConstInlineVisitor.replaceConst(ConstInlineVisitor.java:186)
            	at jadx.core.dex.visitors.ConstInlineVisitor.checkInsn(ConstInlineVisitor.java:107)
            	at jadx.core.dex.visitors.ConstInlineVisitor.process(ConstInlineVisitor.java:52)
            	at jadx.core.dex.visitors.DeboxingVisitor.visit(DeboxingVisitor.java:81)
            */
        @com.android.internal.annotations.GuardedBy({"Settings.this.mLock"})
        public void readStateForUserSyncLPr(int r9) {
            /*
            // Method dump skipped, instructions count: 115
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.Settings.RuntimePermissionPersistence.readStateForUserSyncLPr(int):void");
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00c7  */
        @GuardedBy({"Settings.this.mLock"})
        private void parseRuntimePermissionsLPr(XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
            boolean z;
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != 111052) {
                        if (hashCode != 160289295) {
                            if (hashCode == 485578803 && name.equals(Settings.TAG_SHARED_USER)) {
                                z = true;
                                if (z) {
                                    this.mVersions.put(userId, XmlUtils.readIntAttribute(parser, "version", -1));
                                    String fingerprint = parser.getAttributeValue(null, Settings.ATTR_FINGERPRINT);
                                    this.mFingerprints.put(userId, fingerprint);
                                    this.mDefaultPermissionsGranted.put(userId, Build.FINGERPRINT.equals(fingerprint));
                                } else if (z) {
                                    String name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                                    PackageSetting ps = Settings.this.mPackages.get(name2);
                                    if (ps == null) {
                                        Slog.w("PackageManager", "Unknown package:" + name2);
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        parsePermissionsLPr(parser, ps.getPermissionsState(), userId);
                                    }
                                } else if (z) {
                                    String name3 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                                    SharedUserSetting sus = Settings.this.mSharedUsers.get(name3);
                                    if (sus == null) {
                                        Slog.w("PackageManager", "Unknown shared user:" + name3);
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        parsePermissionsLPr(parser, sus.getPermissionsState(), userId);
                                    }
                                }
                            }
                        } else if (name.equals(Settings.TAG_RUNTIME_PERMISSIONS)) {
                            z = false;
                            if (z) {
                            }
                        }
                    } else if (name.equals(Settings.TAG_PACKAGE)) {
                        z = true;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                }
            }
        }

        private void parsePermissionsLPr(XmlPullParser parser, PermissionsState permissionsState, int userId) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                boolean granted = true;
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    char c = 65535;
                    int flags = 0;
                    if (name.hashCode() == 3242771 && name.equals(Settings.TAG_ITEM)) {
                        c = 0;
                    }
                    if (c == 0) {
                        String name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        BasePermission bp = Settings.this.mPermissions.getPermission(name2);
                        if (bp == null) {
                            Slog.w("PackageManager", "Unknown permission:" + name2);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            String grantedStr = parser.getAttributeValue(null, Settings.ATTR_GRANTED);
                            if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                                granted = false;
                            }
                            String flagsStr = parser.getAttributeValue(null, Settings.ATTR_FLAGS);
                            if (flagsStr != null) {
                                flags = Integer.parseInt(flagsStr, 16);
                            }
                            if (granted) {
                                permissionsState.grantRuntimePermission(bp, userId);
                                permissionsState.updatePermissionFlags(bp, userId, 64511, flags);
                            } else {
                                permissionsState.updatePermissionFlags(bp, userId, 64511, flags);
                            }
                        }
                    }
                }
            }
        }

        private void writePermissions(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, Settings.TAG_ITEM);
                serializer.attribute(null, Settings.ATTR_NAME, permissionState.getName());
                serializer.attribute(null, Settings.ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, Settings.ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, Settings.TAG_ITEM);
            }
        }

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            public void handleMessage(Message message) {
                int userId = message.what;
                Runnable callback = (Runnable) message.obj;
                RuntimePermissionPersistence.this.writePermissionsSync(userId);
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String fileToSHA1(String filePath) {
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
            byte[] buffer = new byte[4096];
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int byteRead = 0;
            while (byteRead != -1) {
                byteRead = in.read(buffer);
                if (byteRead > 0) {
                    digest.update(buffer, 0, byteRead);
                }
            }
            return convertHashToString(digest.digest());
        } catch (Exception ex) {
            Slog.e("PackageManager", "compute fileToSHA1 failed", ex);
            return null;
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private static String convertHashToString(byte[] hashBytes) {
        String returnVal = "";
        for (int i = 0; i < hashBytes.length; i++) {
            returnVal = returnVal + Integer.toString((hashBytes[i] & OppoNfcChipVersion.NONE) + 256, 16).substring(1);
        }
        return returnVal.toLowerCase();
    }
}
