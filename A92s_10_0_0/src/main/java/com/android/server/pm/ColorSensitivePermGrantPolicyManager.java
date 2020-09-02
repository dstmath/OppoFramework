package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.notification.OpenID;
import com.android.server.pm.rsa.RSAUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ColorSensitivePermGrantPolicyManager implements IColorSensitivePermGrantPolicyManager {
    public static final ArrayList<String> ALLOW_ADD_INSTALL_PERM_DATA_APPS = new ArrayList<>(Arrays.asList("com.coloros.onekeylockscreen", "com.nearme.note", "com.coloros.note", "com.nearme.play", "com.coloros.screenrecorder", "com.coloros.familyguard", "com.oppo.community", "com.coloros.securityguard", "com.coloros.compass", "com.coloros.compass2", "com.coloros.weather", "com.coloros.weather2", GAME_CENTER_PKGNAME, "com.coloros.yoli", "com.coloros.digitalwellbeing", "com.coloros.colorfilestand", "com.coloros.oppopods", "com.coloros.accegamesdk", "com.android.calculator2", "com.android.calculator", "com.coloros.favorite", "com.coloros.personalassistant", "com.coloros.apprecover", "com.heytap.yoli", GAME_CENTER_PKGNAME_NEW, "com.heytap.play", "com.coloros.aruler", "com.coloros.calculator", "com.coloros.gamespaceui", "com.coloros.operationtips", "com.coloros.soundrecorder", "com.coloros.wallet", "com.finshel.com", "com.heytap.book", "com.heytap.health", "com.heytap.reader", "com.heytap.smarthome", "com.nearme.note2", "com.oppo.ohome", "com.oppo.store", "com.redteamobile.roaming", "com.coloros.videoeditor", "com.coloros.shortcuts"));
    private static final String APPSTORE_INSTANT_UID = "oppo.uid.instant";
    private static final String APPSTORE_INSTANT_UID_DEAMON_PKG = "com.nearme.instant.platform";
    private static final ArrayList<String> CAMERA_DEFAULT_GRANT_PEMISSION = new ArrayList<>();
    private static final ArrayList<String> DATA_SIGNATURE_PERM_APPS = new ArrayList<>();
    private static final boolean DEBUG_REFINED_PERMISSION = SystemProperties.getBoolean("persist.sys.debug_refined_perm", (boolean) DEBUG_REFINED_PERMISSION);
    private static final String ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkPEAxZKJ0SSQRxlUCFbK4GiOH2OcwN5wRls3d5mogetljPRlKkFWOwyklFFG7tMICH0qcLWWtTEnIXVr++VqrPzK7UAzkWBw59nEZ7s82qq91Qe9seaPcrzS4V0RMatgLc5HmoDx/9Wu1zh99JzT2r9nuWNf2/dRgf2bZO92vq7nPEwgF1hstjWHITEYx3dqY+G+DoOIurk33/E+Z60laVZFfe1OHRnhO71zuzVth1Z1K4oNPmTSsvUpTCaS9yBz73bJlrMMen0Gzyn5dx7Rk4WATnM4rougsR3OUApquJEXD2dDgJHRu20Bp95R8/0pXtOt+00QQcxVxRbsTw/15wIDAQAB";
    private static final String EXTRA_TAG = "extras:";
    private static final ArrayList<String> GAMECENTER_DEFAULT_GRANT_PEMISSION = new ArrayList<>();
    private static final String GAMECENTER_SHARE_UID = "oppo.uid.gc";
    private static final String GAME_CENTER_PKGNAME = "com.nearme.gamecenter";
    private static final String GAME_CENTER_PKGNAME_NEW = "com.heytap.gamecenter";
    private static final String GAME_CENTER_SYSTEM_APP = "com.nearme.deamon";
    private static final ArrayList<String> GRANT_SIG_PERM_DATA_APPS = new ArrayList<>();
    private static final int HIGH_PERM = 1;
    private static final ArrayList<String> HIGH_SECURITY_PERMISSIONS = new ArrayList<>(Arrays.asList(LOW_PERM_GROUP, MIDDLE_PERM_GROUP, "com.oppo.permission.safe.PRIVATE", "com.oppo.permission.safe.RUS", "com.oppo.permission.safe.SAU", "com.oppo.permission.safe.FACE", "com.oppo.permission.safe.FINGERPRINT", "com.oppo.permission.safe.BACKUP", "com.oppo.permission.safe.SECURITY"));
    private static final ArrayList<String> INSTANT_DEFAULT_GRANT_PEMISSION = new ArrayList<>();
    private static final ArrayList<String> LAUNCHER_GRANT_PEMISSION = new ArrayList<>();
    private static final String LAUNCHER_SHARE_UID = "oppo.uid.launcher";
    private static final int LOW_PERM = 3;
    private static final String LOW_PERM_GROUP = "low_perm_group";
    private static final String LOW_SECURITY_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String METADATA_TAG = "OppoPermissionKey";
    private static final int MIDDLE_PERM = 2;
    private static final String MIDDLE_PERM_GROUP = "middle_perm_group";
    private static final ArrayList<String> MIDDLE_SECURITY_PERMISSIONS = new ArrayList<>(Arrays.asList("com.oppo.permission.safe.ACCOUNTS", "com.oppo.permission.safe.SAFE_MANAGER", "com.oppo.permission.safe.APP_MANAGER", "com.oppo.permission.safe.DCS", "com.oppo.permission.safe.UPDATE", "com.oppo.permission.safe.PUSH", "com.oppo.permission.safe.AI_APP", "com.oppo.permission.safe.MMS", "com.oppo.permission.safe.PHONE", "com.oppo.permission.safe.KEYGUARD", "com.oppo.permission.safe.LOCATION", "com.oppo.permission.safe.NFC", "com.oppo.permission.safe.CONNECTIVITY", "com.oppo.permission.safe.BLUETOOTH", "com.oppo.permission.safe.CAMERA", "com.oppo.permission.safe.PICTURE", "com.oppo.permission.safe.CLIPBOARD", "com.oppo.permission.safe.LOG", "com.oppo.permission.safe.CLOUD", "com.oppo.permission.safe.SENSOR", "com.oppo.permission.safe.IOT", "com.oppo.permission.safe.PROTECT", "com.oppo.permission.safe.SDCARD", "com.oppo.permission.safe.USB", "com.oppo.permission.safe.EMAIL", "com.oppo.permission.safe.SETTINGS"));
    private static final List<String> OPPO_ALLOW_SILENTLY_UNINSTALL_APPS = new ArrayList();
    private static final int PERMISSION_KEY_CERT_DIGEST = 3;
    private static final int PERMISSION_KEY_ENCRYPT_DIGEST = 0;
    private static final int PERMISSION_KEY_PACKAGE_NAME = 2;
    private static final int PERMISSION_KEY_TAG_MIN_SIZE = 4;
    private static final String PKG_NAME_COM_HEYTAP_MARKET = "com.heytap.market";
    private static final int ROM_DEBUGGABLE = SystemProperties.getInt("ro.debuggable", 0);
    private static final ArrayList<String> SOFTSIM_GRANT_PEMISSION = new ArrayList<>();
    private static final String SOFTSIM_SHARE_UID = "oppo.uid.softsim";
    private static final String SOFTSIM_SHARE_UID_DEAMON_PKG = "com.redteamobile.roaming.deamon";
    public static final String TAG = "ColorSensitivePermGrantPolicyManager";
    private static ColorSensitivePermGrantPolicyManager sColorSensitivePermGrantPolicyManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG_REFINED_PERMISSION);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private AllowedRefinedPermCache mAllowedRefinedPermCache;
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    boolean mDynamicDebug = DEBUG_REFINED_PERMISSION;
    private PackageManagerService mPms = null;

    static {
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.DEVICE_POWER");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.INTERACT_ACROSS_USERS_FULL");
        CAMERA_DEFAULT_GRANT_PEMISSION.add(OppoPermissionConstants.PERMISSION_NFC);
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.WRITE_SETTINGS");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.WRITE_MEDIA_STORAGE");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.SYSTEM_ALERT_WINDOW");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.CONTROL_KEYGUARD");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.WRITE_SETTINGS");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.INSTALL_PACKAGES");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("com.coloros.speechassist.permission.SPEECH_SERVICE");
        CAMERA_DEFAULT_GRANT_PEMISSION.add("android.permission.GRANT_RUNTIME_PERMISSIONS");
        LAUNCHER_GRANT_PEMISSION.add("com.oppo.usercenter.permission.READ");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.REAL_GET_TASKS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.DOWNLOAD_WITHOUT_NOTIFICATION");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.WRITE_SETTINGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.READ_WALLPAPER_INTERNAL");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.SYSTEM_ALERT_WINDOW");
        LAUNCHER_GRANT_PEMISSION.add("com.oppo.launcher.permission.WRITE_SETTINGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.INSTALL_PACKAGES");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.CHANGE_COMPONENT_ENABLED_STATE");
        LAUNCHER_GRANT_PEMISSION.add("com.nearme.themespace.permission.RECIEVE_MCS_MESSAGE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.RECEIVE_BOOT_COMPLETED");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.EXPAND_STATUS_BAR");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.BLUETOOTH");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.WRITE_MEDIA_STORAGE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.GET_TASKS");
        LAUNCHER_GRANT_PEMISSION.add(OppoPermissionConstants.PERMISSION_SEND_MMS_INTERNET);
        LAUNCHER_GRANT_PEMISSION.add("android.permission.INTERACT_ACROSS_USERS_FULL");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.BIND_APPWIDGET");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.WRITE_SECURE_SETTINGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.STATUS_BAR_SERVICE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.READ_APP_BADGE");
        LAUNCHER_GRANT_PEMISSION.add("com.nearme.themespace.permission.ACS_SERVICE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.ACCESS_KEYGUARD_SECURE_STORAGE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.MANAGE_USERS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.SET_WALLPAPER_COMPONENT");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.ACCESS_NETWORK_STATE");
        LAUNCHER_GRANT_PEMISSION.add("com.oppo.launcher.permission.READ_SETTINGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.DISABLE_KEYGUARD");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.CHANGE_CONFIGURATION");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.READ_LOGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.INTERACT_ACROSS_USERS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.SET_WALLPAPER");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.KILL_BACKGROUND_PROCESSES");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.SET_WALLPAPER_HINTS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.READ_SYNC_SETTINGS");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.FORCE_STOP_PACKAGES");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.VIBRATE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.ACCESS_WIFI_STATE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.STATUS_BAR");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.READ_FRAME_BUFFER");
        LAUNCHER_GRANT_PEMISSION.add("oppo.permission.OPPO_COMPONENT_SAFE");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.WAKE_LOCK");
        LAUNCHER_GRANT_PEMISSION.add("android.permission.DELETE_PACKAGES");
        GAMECENTER_DEFAULT_GRANT_PEMISSION.add("android.permission.INSTALL_PACKAGES");
        GAMECENTER_DEFAULT_GRANT_PEMISSION.add("android.permission.DELETE_PACKAGES");
        INSTANT_DEFAULT_GRANT_PEMISSION.add("oppo.permission.OPPO_COMPONENT_SAFE");
        SOFTSIM_GRANT_PEMISSION.add("oppo.permission.OPPO_COMPONENT_SAFE");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.WRITE_SETTINGS");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.WRITE_APN_SETTINGS");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.READ_NETWORK_USAGE_HISTORY");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.CONNECTIVITY_INTERNAL");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.MODIFY_PHONE_STATE");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.WRITE_SETTINGS");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.REAL_GET_TASKS");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.STATUS_BAR");
        DATA_SIGNATURE_PERM_APPS.add("com.nearme.gamecenter android.permission.INSTALL_PACKAGES");
        DATA_SIGNATURE_PERM_APPS.add("com.nearme.gamecenter android.permission.DELETE_PACKAGES");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.personalassistant android.permission.SET_ACTIVITY_WATCHER");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.soundrecorder android.permission.WRITE_MEDIA_STORAGE");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.safesdkproxy android.permission.WRITE_MEDIA_STORAGE");
        DATA_SIGNATURE_PERM_APPS.add("com.heytap.gamecenter android.permission.INSTALL_PACKAGES");
        DATA_SIGNATURE_PERM_APPS.add("com.heytap.gamecenter android.permission.DELETE_PACKAGES");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.screenrecorder 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        GRANT_SIG_PERM_DATA_APPS.add("com.nearme.gamecenter 84D27678ADF5BABDC2A65D89DD2A77F08ABE16A0B76183324CCAA9B3D648465A");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.personalassistant 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.soundrecorder 94CBB5595955C7E1180565B3223F8F9E3C003F5F27C1B5915090B2579CE45F8E");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.safesdkproxy 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        GRANT_SIG_PERM_DATA_APPS.add("com.heytap.gamecenter 84D27678ADF5BABDC2A65D89DD2A77F08ABE16A0B76183324CCAA9B3D648465A");
        OPPO_ALLOW_SILENTLY_UNINSTALL_APPS.add(PKG_NAME_COM_HEYTAP_MARKET);
    }

    public static boolean isGrantedPermissionForShareUid(String shareUid, String permission, PackageManagerService pms) {
        if (LAUNCHER_SHARE_UID.equals(shareUid)) {
            return LAUNCHER_GRANT_PEMISSION.contains(permission);
        }
        if (APPSTORE_INSTANT_UID.equals(shareUid)) {
            PackageSetting ps = null;
            if (!(pms == null || pms.mSettings == null)) {
                ps = pms.mSettings.getPackageLPr(APPSTORE_INSTANT_UID_DEAMON_PKG);
            }
            if (ps == null) {
                return DEBUG_REFINED_PERMISSION;
            }
            if (ps.isSystem() || ps.isUpdatedSystem()) {
                return INSTANT_DEFAULT_GRANT_PEMISSION.contains(permission);
            }
            return DEBUG_REFINED_PERMISSION;
        } else if (!SOFTSIM_SHARE_UID.equals(shareUid)) {
            return DEBUG_REFINED_PERMISSION;
        } else {
            PackageSetting ps2 = null;
            if (!(pms == null || pms.mSettings == null)) {
                ps2 = pms.mSettings.getPackageLPr(SOFTSIM_SHARE_UID_DEAMON_PKG);
            }
            if (ps2 == null) {
                return DEBUG_REFINED_PERMISSION;
            }
            if (ps2.isSystem() || ps2.isUpdatedSystem()) {
                return SOFTSIM_GRANT_PEMISSION.contains(permission);
            }
            return DEBUG_REFINED_PERMISSION;
        }
    }

    public static boolean isGrantedPermissionForGameCenter(String shareUid, String permission) {
        if (GAMECENTER_SHARE_UID.equals(shareUid)) {
            return GAMECENTER_DEFAULT_GRANT_PEMISSION.contains(permission);
        }
        return DEBUG_REFINED_PERMISSION;
    }

    private static boolean isSha256CertMatchPackage(PackageParser.Package pkg, String targetCertString) {
        byte[] bytes;
        if (pkg == null || (bytes = FileUtil.getInstance().hex2bytes(targetCertString)) == null) {
            return DEBUG_REFINED_PERMISSION;
        }
        if (sDebugfDetail) {
            Slog.d(TAG, "isSha256CertMatchPackage target cert bytes: " + Arrays.toString(bytes));
        }
        PackageParser.SigningDetails details = pkg.mSigningDetails;
        if (details == null) {
            return DEBUG_REFINED_PERMISSION;
        }
        return details.hasSha256Certificate(bytes);
    }

    public static boolean isAllowSigPermForDataApp(PackageParser.Package pkg, String perm) {
        if (!(pkg == null || pkg.packageName == null || perm == null || !isInAllowSigPermApps(pkg.packageName, perm))) {
            Slog.d(TAG, pkg.packageName + " : " + perm + " is in AllowSigPermApps");
            if (isInPkgCertList(pkg, GRANT_SIG_PERM_DATA_APPS)) {
                Slog.d(TAG, pkg.packageName + " is in PkgCertList");
                return true;
            }
        }
        return DEBUG_REFINED_PERMISSION;
    }

    public static boolean isInAllowSigPermApps(String pkg, String perm) {
        if (pkg == null || perm == null) {
            return DEBUG_REFINED_PERMISSION;
        }
        String buildStr = pkg + " " + perm;
        Iterator<String> it = DATA_SIGNATURE_PERM_APPS.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (str != null && str.equals(buildStr)) {
                return true;
            }
        }
        return DEBUG_REFINED_PERMISSION;
    }

    public static boolean isInPkgCertList(PackageParser.Package pkg, List<String> pkgCertList) {
        if (pkgCertList == null || pkgCertList.isEmpty() || pkg == null || TextUtils.isEmpty(pkg.packageName)) {
            return DEBUG_REFINED_PERMISSION;
        }
        String targetPackage = pkg.packageName + " ";
        String targetCert = null;
        Iterator<String> it = pkgCertList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String str = it.next();
            if (str != null && str.startsWith(targetPackage)) {
                String[] tags = str.split(" ");
                if (tags.length > 1) {
                    targetCert = tags[1];
                    break;
                }
            }
        }
        if (targetCert == null) {
            return DEBUG_REFINED_PERMISSION;
        }
        return isSha256CertMatchPackage(pkg, targetCert);
    }

    public boolean allowAddInstallPermForDataApp(String pkg) {
        if (!TextUtils.isEmpty(pkg) && ALLOW_ADD_INSTALL_PERM_DATA_APPS.contains(pkg)) {
            return true;
        }
        return DEBUG_REFINED_PERMISSION;
    }

    public static ColorSensitivePermGrantPolicyManager getInstance() {
        if (sColorSensitivePermGrantPolicyManager == null) {
            sColorSensitivePermGrantPolicyManager = new ColorSensitivePermGrantPolicyManager();
        }
        return sColorSensitivePermGrantPolicyManager;
    }

    private ColorSensitivePermGrantPolicyManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        registerLogModule();
    }

    public boolean grantPermissionOppoPolicy(PackageParser.Package pkg, String perm, boolean allowed) {
        if (pkg.mSharedUserId != null) {
            if (isGrantedPermissionForShareUid(pkg.mSharedUserId, perm, this.mPms)) {
                return true;
            }
            PackageSetting ps = (PackageSetting) pkg.mExtras;
            if ((GAME_CENTER_PKGNAME.equals(pkg.packageName) || GAME_CENTER_PKGNAME_NEW.equals(pkg.packageName)) && ps != null && pkg.isSystem() && isGrantedPermissionForGameCenter(pkg.mSharedUserId, perm)) {
                return true;
            }
        }
        if (!allowed && isAllowSigPermForDataApp(pkg, perm)) {
            Slog.d(TAG, "isAllowSigPermForDataApp " + perm + " for " + pkg.packageName);
            return true;
        } else if (allowed || !isRefinedPermAllowed(pkg, perm)) {
            return DEBUG_REFINED_PERMISSION;
        } else {
            Slog.d(TAG, "allow refined permission " + perm + " for " + pkg.packageName);
            return true;
        }
    }

    public boolean allowSilentUninstall(int callingUid) {
        PackageSetting ps;
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            Slog.d(TAG, "allowSilentUninstall mPms is null");
            return DEBUG_REFINED_PERMISSION;
        }
        String name = packageManagerService.getNameForUid(callingUid);
        if (!TextUtils.isEmpty(name)) {
            if (name.contains(":")) {
                int index = name.indexOf(":");
                if (index == -1 || !OppoPackageManagerHelper.isShareUid(name.substring(0, index))) {
                    return DEBUG_REFINED_PERMISSION;
                }
                return true;
            } else if (OPPO_ALLOW_SILENTLY_UNINSTALL_APPS.contains(name) && (ps = this.mPms.mSettings.getPackageLPr(name)) != null && (ps.isSystem() || ps.isUpdatedSystem())) {
                if (this.mDynamicDebug) {
                    Slog.d(TAG, "package " + name + " has silently uninstall permission");
                }
                return true;
            }
        }
        return DEBUG_REFINED_PERMISSION;
    }

    private static class AllowedRefinedPermCache {
        ArrayList<String> allowedHighPerms;
        String certDigest;
        boolean isLowPermAllowed = ColorSensitivePermGrantPolicyManager.DEBUG_REFINED_PERMISSION;
        boolean isMiddlePermAllowed = ColorSensitivePermGrantPolicyManager.DEBUG_REFINED_PERMISSION;
        String packageName;
        String permissionKey;

        public AllowedRefinedPermCache(String packageName2, String certDigest2, String permissionKey2, ArrayList<String> allowedHighPerms2) {
            this.packageName = packageName2;
            this.certDigest = certDigest2;
            this.permissionKey = permissionKey2;
            this.allowedHighPerms = allowedHighPerms2;
            if (allowedHighPerms2 != null) {
                if (allowedHighPerms2.remove(ColorSensitivePermGrantPolicyManager.MIDDLE_PERM_GROUP)) {
                    this.isMiddlePermAllowed = true;
                }
                if (allowedHighPerms2.remove(ColorSensitivePermGrantPolicyManager.LOW_PERM_GROUP)) {
                    this.isLowPermAllowed = true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isCacheMatchPackage(String packageName2, String certDigest2, String permissionKey2) {
            if (!TextUtils.equals(this.packageName, packageName2) || !TextUtils.equals(this.certDigest, certDigest2) || !TextUtils.equals(this.permissionKey, permissionKey2)) {
                return ColorSensitivePermGrantPolicyManager.DEBUG_REFINED_PERMISSION;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean isPermissionAllowed(String perm, int permLevel) {
            if (permLevel == 3) {
                return this.isLowPermAllowed;
            }
            if (permLevel == 2) {
                return this.isMiddlePermAllowed;
            }
            ArrayList<String> arrayList = this.allowedHighPerms;
            if (arrayList == null || !arrayList.contains(perm)) {
                return ColorSensitivePermGrantPolicyManager.DEBUG_REFINED_PERMISSION;
            }
            return true;
        }
    }

    /* JADX INFO: Multiple debug info for r10v8 java.lang.String: [D('encryptDigest' java.lang.String), D('currentCert' java.lang.String)] */
    private boolean isRefinedPermAllowed(PackageParser.Package pkg, String perm) {
        int permLevel;
        String decryptDigest;
        if ("oppo.permission.OPPO_COMPONENT_SAFE".equals(perm)) {
            permLevel = 3;
        } else if (MIDDLE_SECURITY_PERMISSIONS.contains(perm)) {
            permLevel = 2;
        } else if (!HIGH_SECURITY_PERMISSIONS.contains(perm)) {
            return DEBUG_REFINED_PERMISSION;
        } else {
            permLevel = 1;
        }
        if (pkg == null) {
            return DEBUG_REFINED_PERMISSION;
        }
        if (ROM_DEBUGGABLE == 1 && DEBUG_REFINED_PERMISSION) {
            return true;
        }
        if ("oppo.permission.OPPO_COMPONENT_SAFE".equals(perm) && isSystemApp(pkg)) {
            return true;
        }
        if (pkg.mAppMetaData == null || pkg.packageName == null) {
            Slog.d(TAG, "no meta-data or package name in pkg");
            return DEBUG_REFINED_PERMISSION;
        }
        String permissionKey = pkg.mAppMetaData.getString(METADATA_TAG);
        if (TextUtils.isEmpty(permissionKey)) {
            Slog.d(TAG, "no OppoPermissionKey in " + pkg.packageName);
            return DEBUG_REFINED_PERMISSION;
        }
        String certString = null;
        if (!(pkg.mSigningDetails.signatures == null || pkg.mSigningDetails.signatures.length <= 0 || pkg.mSigningDetails.signatures[0] == null)) {
            certString = FileUtil.getInstance().computeDigest(pkg.mSigningDetails.signatures[0].toByteArray(), OpenID.SHA256, DEBUG_REFINED_PERMISSION);
        }
        if (TextUtils.isEmpty(certString)) {
            Slog.d(TAG, "fail to compute cert digest for " + pkg.packageName);
            return DEBUG_REFINED_PERMISSION;
        }
        AllowedRefinedPermCache allowedRefinedPermCache = this.mAllowedRefinedPermCache;
        if (allowedRefinedPermCache != null && allowedRefinedPermCache.isCacheMatchPackage(pkg.packageName, certString, permissionKey)) {
            return this.mAllowedRefinedPermCache.isPermissionAllowed(perm, permLevel);
        }
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "try creating allowed refined perm cache for package: " + pkg.packageName);
        }
        this.mAllowedRefinedPermCache = new AllowedRefinedPermCache(pkg.packageName, certString, permissionKey, null);
        String[] tags = permissionKey.split(" ");
        if (tags.length < 4) {
            return DEBUG_REFINED_PERMISSION;
        }
        String encryptDigest = tags[0];
        String decryptDigest2 = RSAUtil.publicDecrypt(encryptDigest, ENCODED_PUBLIC_KEY);
        if (TextUtils.isEmpty(decryptDigest2)) {
            Slog.d(TAG, "fail to decrypt permission-key for " + pkg.packageName + " with key as " + permissionKey);
            return DEBUG_REFINED_PERMISSION;
        }
        String restOfKeyDigest = FileUtil.getInstance().computeDigest(permissionKey.substring(encryptDigest.length(), permissionKey.length()).trim().getBytes(), OpenID.SHA1, DEBUG_REFINED_PERMISSION);
        if (!TextUtils.equals(decryptDigest2, restOfKeyDigest)) {
            Slog.d(TAG, "bad permission-key for " + pkg.packageName + " for " + decryptDigest2 + " not equal to " + restOfKeyDigest);
            return DEBUG_REFINED_PERMISSION;
        } else if (!pkg.packageName.equals(tags[2])) {
            Slog.d(TAG, "permission-key is not suitable for " + pkg.packageName + ": " + tags[2]);
            return DEBUG_REFINED_PERMISSION;
        } else {
            String[] certArray = tags[3].toUpperCase().split(";");
            if (isPermKeyInBlackList(pkg.packageName, certArray)) {
                Slog.d(TAG, "permission-key is in blacklist for " + pkg.packageName + ": " + tags[3]);
                return DEBUG_REFINED_PERMISSION;
            }
            int length = certArray.length;
            boolean certMatch = false;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String currentCert = certArray[i];
                if (isSha256CertMatchPackage(pkg, currentCert)) {
                    certMatch = true;
                    break;
                }
                if (this.DEBUG_SWITCH) {
                    StringBuilder sb = new StringBuilder();
                    decryptDigest = decryptDigest2;
                    sb.append("pkg cert not match: ");
                    sb.append(currentCert);
                    Slog.d(TAG, sb.toString());
                } else {
                    decryptDigest = decryptDigest2;
                }
                i++;
                encryptDigest = encryptDigest;
                certArray = certArray;
                decryptDigest2 = decryptDigest;
            }
            if (!certMatch) {
                Slog.d(TAG, "permission-key is not suitable for " + pkg.packageName + ": " + tags[3]);
                return DEBUG_REFINED_PERMISSION;
            }
            ArrayList<String> allowedPermissions = new ArrayList<>();
            if (tags.length > 4 && !EXTRA_TAG.equals(tags[4])) {
                String permTag = new StringBuilder(tags[4]).reverse().toString();
                int size = permTag.length();
                int charIndex = 0;
                int currentChar = "0123456789ABCDEF".indexOf(permTag.charAt(0));
                int i2 = 0;
                while (true) {
                    if (i2 >= HIGH_SECURITY_PERMISSIONS.size()) {
                        break;
                    }
                    if ((charIndex + 1) * 4 <= i2) {
                        if (charIndex >= size - 1) {
                            break;
                        }
                        charIndex++;
                        currentChar = "0123456789ABCDEF".indexOf(permTag.charAt(charIndex));
                    }
                    if (currentChar < 0) {
                        allowedPermissions.clear();
                        break;
                    }
                    if (((1 << (i2 % 4)) & currentChar) != 0) {
                        allowedPermissions.add(HIGH_SECURITY_PERMISSIONS.get(i2));
                    }
                    i2++;
                }
            }
            if (allowedPermissions.isEmpty() && tags.length == 4) {
                allowedPermissions.add(LOW_PERM_GROUP);
            }
            this.mAllowedRefinedPermCache = new AllowedRefinedPermCache(pkg.packageName, certString, permissionKey, allowedPermissions);
            return this.mAllowedRefinedPermCache.isPermissionAllowed(perm, permLevel);
        }
    }

    private boolean isPermKeyInBlackList(String packageName, String[] certArray) {
        for (String cert : certArray) {
            if (ColorPackageManagerHelper.isPermKeyInBlackList(cert, packageName)) {
                return true;
            }
        }
        return DEBUG_REFINED_PERMISSION;
    }

    private static boolean isSystemApp(PackageParser.Package pkg) {
        if ((pkg.applicationInfo.flags & 1) != 0) {
            return true;
        }
        return DEBUG_REFINED_PERMISSION;
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
            m.invoke(cls.newInstance(), ColorSensitivePermGrantPolicyManager.class.getName());
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
}
