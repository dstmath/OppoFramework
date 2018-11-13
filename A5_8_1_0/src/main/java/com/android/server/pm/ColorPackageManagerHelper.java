package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.display.OppoBrightUtils;
import com.android.server.storage.OppoDeviceStorageMonitorService;
import com.color.widget.ColorResolveInfoHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class ColorPackageManagerHelper {
    private static final String ACTION_EVENTID_PREVENT_UNINSTALL = "PMS_prevent_uninstall";
    private static final String ACTION_OPPO_INSTALL_FAILED = "oppo.intent.action.OPPO_INSTALL_FAILED";
    private static final String ACTION_OPPO_START_INSTALL = "oppo.intent.action.OPPO_START_INSTALL";
    private static final String ACTION_OPPO_TEMP_UNFREEZE = "oppo.intent.action.OPPO_TEMP_UNFREEZE";
    private static final String APPSTORE_INSTANT_UID = "oppo.uid.instant";
    private static final String APP_CODE = "20120";
    private static final String BUILDIN_NOT_LAUNCHED_PATH = "/data/format_unclear/launcher/builtin_thirdpart_pkgs";
    private static final ArrayList<String> CAMERA_DEFAULT_GRANT_PEMISSION = new ArrayList();
    private static final String CAMERA_SHARE_UID_NEW = "oppo.uid.camera";
    private static final String CAMERA_SHARE_UID_OLD = "com.oppo.camera.Camera";
    public static final int CLOSE_RESTORE_SANDBOX_STATE = 2;
    public static final int CLOSE_SANDBOX_SWITCH = 0;
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final ArrayList<String> CTTL_PACKAGE_LIST = new ArrayList();
    private static final String DATA_APP_LOCATION = "/data/app/";
    private static final ArrayList<String> DATA_SIGNATURE_PERM_APPS = new ArrayList();
    public static final ArrayList<String> DEFAULT_APP_WHITE_LIST = new ArrayList(Arrays.asList(new String[]{"com.salesforce.chatter,android.intent.action.PICK,*/*,^.*$", "cn.cj.pe,android.intent.action.VIEW,image/vnd.dwg,^.*.dwg$"}));
    public static final int DEFAULT_APP_WHITE_LIST_INDEX = 677;
    private static final ArrayList<String> DEFAULT_OPPO_HIDE_APP_LIST = new ArrayList();
    public static final List<String> DEFAULT_PROTECT_HIDE_APP = Collections.unmodifiableList(Arrays.asList(new String[]{"com.oppo.launcher", "com.coloros.safecenter", "com.coloros.sau", "com.coloros.sauhelper", "com.oppo.ota", "com.nearme.romupdate", "com.oppo.market", "com.nearme.gamecenter", "com.nearme.themespace"}));
    public static final ArrayList<String> DEFAULT_SHOP_TO_VERFITY_LIST = new ArrayList(Arrays.asList(new String[]{"com.android.vending"}));
    private static final List<String> DEFAULT_SYSTEM_APP_IN_DATA = Collections.unmodifiableList(Arrays.asList(new String[]{"com.coloros.video", "com.coloros.gallery3d", "com.oppo.music", "com.oppo.book", "com.nearme.gamecenter", "com.oppo.community", "com.coloros.weather", "com.android.calculator2", "com.coloros.compass", "com.nearme.note", "com.oppo.reader"}));
    private static final ArrayList<String> EXP_SYSTEM_DEFAULT_PACKAGES = new ArrayList();
    private static final ArrayList<String> EXP_SYSTEM_FORCE_PACKAGES = new ArrayList();
    private static final String FILE_NOT_LAUNCHED_LIST = "notLaunchedPkgs.xml";
    public static final List<String> FILTER_RUNTIME_PERM_GROUPS = Arrays.asList(new String[]{"CALENDAR", "CAMERA", "CONTACTS", "LOCATION", "MICROPHONE", "PHONE", "SENSORS", "SMS", "STORAGE"});
    private static final ArrayList<String> FORBID_UNINSTALL_DATA_APPS = new ArrayList();
    private static final ArrayList<String> GAMECENTER_DEFAULT_GRANT_PEMISSION = new ArrayList();
    private static final String GAMECENTER_SHARE_UID = "oppo.uid.gc";
    private static final ArrayList<String> GRANT_SIG_PERM_DATA_APPS = new ArrayList();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final ArrayList<String> INSTANT_DEFAULT_GRANT_PEMISSION = new ArrayList();
    private static final String LAUNCHER_SHARE_UID = "oppo.uid.launcher";
    private static final ArrayList<String> NEARME_DEFAULT_GRANT_PEMISSION = new ArrayList();
    private static final String NEARME_SHARE_UID = "oppo.uid.nearme";
    public static final List<String> NON_STOP_STATE_PKG_LIST = Arrays.asList(new String[0]);
    private static final String ODEX_ARM64_FILE = "/arm64/base.odex/";
    private static final String ODEX_ARM_FILE = "/arm/base.odex/";
    private static final int ODEX_DATA_MIN_SIZE = 200;
    public static final int OPEN_SANDBOX_SWITCH = 1;
    private static final String OPPO_DEFAULT_PACKAGE_XML = "sys_pms_defaultpackage_list";
    private static final String OPPO_DEFAULT_PKG_CONFIG = "/data/oppo/coloros/config/sys_pms_defaultpackage_list.xml";
    private static final String OPPO_DEFAULT_PKG_PATH = "/data/oppo/coloros/config";
    public static final String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
    private static final ArrayList<String> OPPO_FIXED_PERM_LIST = new ArrayList();
    public static final String OPPO_FORBID_INSTALL_ACTION = "oppo.android.intent.action.FORBID_INSTALL";
    private static final ArrayList<String> OPPO_OVERSEA_FIXED_PERM_LIST = new ArrayList();
    private static final ArrayList<String> OPPO_OVERSEA_NONFIXED_PERM_LIST = new ArrayList();
    private static final String OPPO_RUNTIME_PERM_FILTER_FILE = "/data/system/config/sys_pms_runtimeperm_filter_list.xml";
    public static final int OPPO_SHOP_VERIFY_LIST_INDEX = 681;
    private static final String OPPO_SYSTEM_APP_PATH = "/system/etc/security/pl.fs";
    private static final String OPPO_SYSTEM_APP_PWD = "a";
    private static final String OPPO_VENDOR_APP_PATH = "/vendor/etc/security/pl.fs";
    private static final String PACKAGE_NEARME_STATISTICS = "com.nearme.statistics.rom";
    private static final String PACKAGE_OPPO_LAUNCHER = "com.oppo.launcher";
    private static final String PATH_NOT_LAUNCHED_LIST = "/data/oppo/common/";
    public static final int PROTECT_HIDE_APP_INDEX = 676;
    private static final ArrayList<String> SEPCIAL_DEFAULT_SECURE_APP = new ArrayList();
    private static final ArrayList<String> SKIP_APP_PACKAGE_LIST = new ArrayList();
    private static final ArrayList<String> SKIP_SAFECENTER_PACKAGE_LIST = new ArrayList();
    private static final ArrayList<String> SOFTSIM_GRANT_PEMISSION = new ArrayList();
    private static final String SOFTSIM_SHARE_UID = "oppo.uid.softsim";
    private static final ArrayList<String> SYSTEM_DEFAULT_PACKAGES = new ArrayList();
    private static final ArrayList<String> SYSTEM_FORCE_PACKAGES = new ArrayList();
    public static final String TAG = "ColorPackageManager";
    private static final String TAG_FIXED_RUNTIME_PERM = "FixedRuntimePermFilter";
    private static final String TAG_OPPO_CTS_APP = "OppoCtsApp";
    private static final String TAG_OPPO_CTS_PREFIX = "OppoCtsPreFix";
    private static final String TAG_OPPO_DEFAULT_APP = "OppoDefaultApp";
    private static final String TAG_OPPO_FORCE_APP = "OppoForceApp";
    private static final String TAG_OPPO_HIDE_APP = "OppoHideApp";
    private static final String TAG_OPPO_ICON_CACHE_MAX_NUM = "OppoIconCacheMaxNum";
    private static final String TAG_OPPO_ICON_CACHE_MIN_MEM = "OppoIconCacheMinMem";
    private static final String TAG_OPPO_SANDBOX_SWITCH = "OppoSandboxSwitch";
    private static final String TAG_OPPO_SECUREPAY_SWITCH = "OppoSecurepaySwitch";
    private static final String TAG_OVERSEA_FIXED_RUNTIME_PERM = "OverseaFixedRuntimePermFilter";
    private static final String TAG_SPECIAL_SECURE_APP = "OppoSepcialSecureApp";
    private static final String TAG_SYSTEM_DATA_APP = "OppoSystemDataApp";
    private static final ArrayList<String> mOppoApkList = new ArrayList();
    public static ArrayMap<Integer, ArrayMap<String, OppoFreezeInfo>> mOppoFreezeInfoUserMap = new ArrayMap();
    private static FileObserverPolicy sConfigFileObserver = null;
    private static ArrayList<String> sFailedParsedCtsPkgList = new ArrayList();
    private static int sIconCacheMaxNum = 200;
    private static float sIconCacheMinMemory = OppoBrightUtils.MIN_LUX_LIMITI;
    private static ArrayList<String> sLocalCtsPkgList = new ArrayList();
    private static ArrayList<String> sLocalCtsSigList = new ArrayList();
    private static ArrayList<String> sNotLaunchedPkgs = new ArrayList();
    private static ArrayList<String> sOppoCtsPkgList = new ArrayList();
    private static ArrayList<String> sOppoCtsPrefixList = new ArrayList();
    private static ArrayList<String> sOppoDefaultPkgList = new ArrayList();
    private static ArrayList<RuntimePermFilterInfo> sOppoFixedPermInfos = new ArrayList();
    private static ArrayList<String> sOppoForcePkgList = new ArrayList();
    private static ArrayList<String> sOppoHidePkgList = new ArrayList();
    private static ArrayList<RuntimePermFilterInfo> sOppoOverseaFixedPermInfos = new ArrayList();
    private static ArrayList<RuntimePermFilterInfo> sOppoOverseaNonFixedPermInfos = new ArrayList();
    public static ArrayList<PackageSetting> sOppoSystemToDataList = new ArrayList();
    private static SparseArray<ArrayList<String>> sPmsWhiteList = new SparseArray();
    private static String sSecurePaySwitch = "true";
    private static ArrayList<String> sSpecialSecureAppList = new ArrayList();
    private static ArrayList<String> sSystemDataPkgList = new ArrayList();
    private static float sTotalMemorySize = -1.0f;

    private static class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorPackageManagerHelper.OPPO_DEFAULT_PKG_CONFIG)) {
                Slog.i(ColorPackageManagerHelper.TAG, "onEvent: focusPath = OPPO_CRASH_CLEAR_CONFIG_PATH");
                ColorPackageManagerHelper.sOppoDefaultPkgList.clear();
                ColorPackageManagerHelper.sOppoForcePkgList.clear();
                ColorPackageManagerHelper.sOppoCtsPkgList.clear();
                ColorPackageManagerHelper.sOppoCtsPrefixList.clear();
                ColorPackageManagerHelper.sSpecialSecureAppList.clear();
                ColorPackageManagerHelper.sOppoHidePkgList.clear();
                ColorPackageManagerHelper.sPmsWhiteList.clear();
                ColorPackageManagerHelper.readConfigFile();
            }
        }
    }

    public static class OppoFreezeInfo {
        int mFreezeFlag;
        String mPackageName;
        int mState;
        int mUserId;

        public OppoFreezeInfo(String packageName, int userId, int state, int flags) {
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mState = state;
            this.mFreezeFlag = flags;
        }

        public String toString() {
            return this.mPackageName + "/" + this.mUserId + "/" + this.mState + "/" + this.mFreezeFlag;
        }
    }

    public static class RuntimePermFilterInfo {
        public boolean mAddAll;
        public int mFixType;
        public ArrayList<String> mGroups;
        public boolean mOverSea;
        public String mPackageName;
    }

    private static class SyncNotLaunchedPkgsToFileRunnable implements Runnable {
        boolean mNotify;
        String mPkg;

        public SyncNotLaunchedPkgsToFileRunnable(String pkg, boolean needNotify) {
            this.mPkg = pkg;
            this.mNotify = needNotify;
        }

        public void run() {
            synchronized (ColorPackageManagerHelper.sNotLaunchedPkgs) {
                ColorPackageManagerHelper.writeNotLaunchedListToFile(ColorPackageManagerHelper.sNotLaunchedPkgs);
            }
        }
    }

    static {
        OPPO_FIXED_PERM_LIST.add("com.iflytek.speechcloud");
        OPPO_FIXED_PERM_LIST.add("com.coloros.fingerprint");
        OPPO_FIXED_PERM_LIST.add("com.coloros.activation");
        OPPO_FIXED_PERM_LIST.add("com.oppo.c2u");
        OPPO_FIXED_PERM_LIST.add("com.coloros.speechassist.engine");
        OPPO_FIXED_PERM_LIST.add("com.android.dlna.service");
        OPPO_FIXED_PERM_LIST.add("com.coloros.phonenoareainquire");
        OPPO_FIXED_PERM_LIST.add("com.android.mms.service");
        OPPO_FIXED_PERM_LIST.add("com.android.incallui");
        OPPO_FIXED_PERM_LIST.add("com.ted.number");
        OPPO_FIXED_PERM_LIST.add("com.nearme.romupdate");
        OPPO_FIXED_PERM_LIST.add("com.coloros.safesdkproxy");
        OPPO_FIXED_PERM_LIST.add("com.coloros.pictorial");
        OPPO_FIXED_PERM_LIST.add("com.android.contacts#CONTACTS#PHONE");
        OPPO_FIXED_PERM_LIST.add("com.android.mms#SMS#PHONE");
        OPPO_FIXED_PERM_LIST.add("com.oppo.ota");
        OPPO_FIXED_PERM_LIST.add("com.mediatek.omacp");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.coloros.phonenoareainquire");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.ted.number");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.coloros.safesdkproxy");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.oppo.market#PHONE#STORAGE");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.oppo.smartvolume");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.baidu.map.location");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.amap.android.location");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.amap.android.ams");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.mediatek.omacp");
        OPPO_OVERSEA_NONFIXED_PERM_LIST.add("com.LogiaGroup.LogiaDeck");
        SEPCIAL_DEFAULT_SECURE_APP.add("com.eg.android.AlipayGphone");
        SEPCIAL_DEFAULT_SECURE_APP.add("com.tencent.mm");
        SYSTEM_DEFAULT_PACKAGES.add("com.android.browser");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.music");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.launcher");
        SYSTEM_DEFAULT_PACKAGES.add("com.android.contacts");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.camera");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.gallery3d");
        SYSTEM_DEFAULT_PACKAGES.add("com.coloros.gallery3d");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.wirelesssettings");
        SYSTEM_DEFAULT_PACKAGES.add("com.coloros.wirelesssettings");
        SYSTEM_DEFAULT_PACKAGES.add("com.android.packageinstaller");
        SYSTEM_DEFAULT_PACKAGES.add("com.oppo.market");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.oppo.launcher");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.android.contacts");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.oppo.camera");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.oppo.wirelesssettings");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.coloros.wirelesssettings");
        EXP_SYSTEM_DEFAULT_PACKAGES.add("com.google.android.packageinstaller");
        EXP_SYSTEM_FORCE_PACKAGES.add("com.oppo.wirelesssettings");
        EXP_SYSTEM_FORCE_PACKAGES.add("com.coloros.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.coloros.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.android.packageinstaller");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.launcher");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.market");
        SKIP_APP_PACKAGE_LIST.add("com.coloros.regservice");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.tencent.tvoem");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.book");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.nearme.note");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.community");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.news");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.android.email");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.findphone.client");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.reader");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.weather");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.nearme.gamecenter");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.compass");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.android.calculator2");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.sauhelper");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safecenter");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safesdkproxy");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safe.service.framework");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.roaming");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.virtual.softsim");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.phonemanager");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.oppo.roaming");
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
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.INSTALL_PACKAGES");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.DELETE_PACKAGES");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.MOVE_PACKAGE");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.READ_LOGS");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.PACKAGE_USAGE_STATS");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.GRANT_RUNTIME_PERMISSIONS");
        NEARME_DEFAULT_GRANT_PEMISSION.add("android.permission.WRITE_SECURE_SETTINGS");
        GAMECENTER_DEFAULT_GRANT_PEMISSION.add("android.permission.INSTALL_PACKAGES");
        GAMECENTER_DEFAULT_GRANT_PEMISSION.add("android.permission.DELETE_PACKAGES");
        INSTANT_DEFAULT_GRANT_PEMISSION.add("oppo.permission.OPPO_COMPONENT_SAFE");
        SOFTSIM_GRANT_PEMISSION.add("oppo.permission.OPPO_COMPONENT_SAFE");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.WRITE_SETTINGS");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.WRITE_APN_SETTINGS");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.READ_NETWORK_USAGE_HISTORY");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.CONNECTIVITY_INTERNAL");
        SOFTSIM_GRANT_PEMISSION.add("android.permission.MODIFY_PHONE_STATE");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder oppo.permission.OPPO_COMPONENT_SAFE");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.WRITE_SETTINGS");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.REAL_GET_TASKS");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.screenrecorder android.permission.STATUS_BAR");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.oppopods oppo.permission.OPPO_COMPONENT_SAFE");
        DATA_SIGNATURE_PERM_APPS.add("com.coloros.smartdrive oppo.permission.OPPO_COMPONENT_SAFE");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.screenrecorder 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.oppopods 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        GRANT_SIG_PERM_DATA_APPS.add("com.coloros.smartdrive 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
    }

    public static void initDefaultPackageList(String defaultBrowser) {
        EXP_SYSTEM_DEFAULT_PACKAGES.add(defaultBrowser);
    }

    public static boolean isGrantedPermissionForShareUid(String shareUid, String permission) {
        if (LAUNCHER_SHARE_UID.equals(shareUid) || CAMERA_SHARE_UID_NEW.equals(shareUid)) {
            return true;
        }
        if (NEARME_SHARE_UID.equals(shareUid)) {
            return NEARME_DEFAULT_GRANT_PEMISSION.contains(permission);
        }
        if (APPSTORE_INSTANT_UID.equals(shareUid)) {
            return INSTANT_DEFAULT_GRANT_PEMISSION.contains(permission);
        }
        if (SOFTSIM_SHARE_UID.equals(shareUid)) {
            return SOFTSIM_GRANT_PEMISSION.contains(permission);
        }
        return false;
    }

    public static boolean isGrantedPermissionForGameCenter(String shareUid, String permission) {
        if (GAMECENTER_SHARE_UID.equals(shareUid)) {
            return GAMECENTER_DEFAULT_GRANT_PEMISSION.contains(permission);
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isAllowSigPermForDataApp(Package pkg, String perm) {
        if (!(pkg == null || pkg.packageName == null || perm == null || !isInAllowSigPermApps(pkg.packageName, perm))) {
            Slog.d(TAG, pkg.packageName + " : " + perm + " is in AllowSigPermApps");
            if (isInPkgCertList(pkg, GRANT_SIG_PERM_DATA_APPS)) {
                Slog.d(TAG, pkg.packageName + " is in PkgCertList");
                return true;
            }
        }
        return false;
    }

    public static boolean isInAllowSigPermApps(String pkg, String perm) {
        if (pkg == null || perm == null) {
            return false;
        }
        String buildStr = pkg + " " + perm;
        for (String str : DATA_SIGNATURE_PERM_APPS) {
            if (str != null && str.equals(buildStr)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInPkgCertList(Package pkg, List<String> pkgCertList) {
        if (pkgCertList == null || pkgCertList.isEmpty() || pkg == null || TextUtils.isEmpty(pkg.packageName)) {
            return false;
        }
        String cert = computePackageCertDigest(pkg);
        if (cert == null) {
            return false;
        }
        String buildCert = buildPackageCertString(pkg.packageName, cert);
        for (String str : pkgCertList) {
            if (str != null && str.equals(buildCert)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForbidUninstallDataApp(Package pkg) {
        if (pkg == null || pkg.packageName == null) {
            return false;
        }
        String cert = computePackageCertDigest(pkg);
        if (cert == null) {
            return false;
        }
        String buildCert = buildPackageCertString(pkg.packageName, cert);
        Slog.d(TAG, "isForbidUninstallDataApp buildCert=" + buildCert);
        for (String str : FORBID_UNINSTALL_DATA_APPS) {
            if (str != null && str.equals(buildCert)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportSecurePay() {
        return "true".equals(sSecurePaySwitch);
    }

    private static void setSandboxSwitchState(String value) {
        int valueState = Integer.parseInt(value);
        if (valueState == 0) {
            Slog.d(TAG, "setSandboxSwitchState valueState = " + valueState);
            SystemProperties.set("persist.sys.coloros.sandbox", String.valueOf(2));
        }
    }

    public static boolean isOppoApkList(String string) {
        return mOppoApkList.contains(string);
    }

    public static boolean isOppoDefaultApp(String string) {
        if (sOppoDefaultPkgList.size() >= 1) {
            return sOppoDefaultPkgList.contains(string);
        }
        return isExpROM() ? EXP_SYSTEM_DEFAULT_PACKAGES.contains(string) : SYSTEM_DEFAULT_PACKAGES.contains(string);
    }

    private static boolean isExpROM() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ^ 1;
    }

    public static boolean isSystemDataApp(String pkg) {
        if (pkg == null) {
            return false;
        }
        if (sSystemDataPkgList.isEmpty()) {
            return DEFAULT_SYSTEM_APP_IN_DATA.contains(pkg);
        }
        return sSystemDataPkgList.contains(pkg);
    }

    public static boolean isSpecialSecureApp(String pkg) {
        if (SEPCIAL_DEFAULT_SECURE_APP.contains(pkg)) {
            return true;
        }
        return sSpecialSecureAppList.size() > 0 && sSpecialSecureAppList.contains(pkg);
    }

    public static boolean isCtsApp(String pkg) {
        if (sLocalCtsPkgList.contains(pkg)) {
            return true;
        }
        if (sOppoCtsPkgList.size() > 0 && sOppoCtsPkgList.contains(pkg)) {
            return true;
        }
        if (sOppoCtsPrefixList.size() > 0) {
            for (String ctsPkg : sOppoCtsPrefixList) {
                if (pkg.contains(ctsPkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void initCtsToolList() {
        File permFile = new File(Environment.getRootDirectory(), "oppo/oppo_cts_list.xml");
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (!parser.getName().equals("CtsToolList")) {
                                if (!parser.getName().equals("PkgCertDig")) {
                                    if (parser.getName().equals("FailParseCtsPkg")) {
                                        eventType = parser.next();
                                        String pkg = parser.getText();
                                        if (!TextUtils.isEmpty(pkg)) {
                                            sFailedParsedCtsPkgList.add(pkg);
                                            break;
                                        }
                                    }
                                }
                                eventType = parser.next();
                                String sig = parser.getText();
                                if (!TextUtils.isEmpty(sig)) {
                                    sLocalCtsSigList.add(sig);
                                    break;
                                }
                            }
                            eventType = parser.next();
                            sLocalCtsPkgList.add(parser.getText());
                            break;
                            break;
                    }
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got execption parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got execption parsing permissions.", e2);
            }
            if (permReader != null) {
                try {
                    permReader.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open oppo_cts_list file " + permFile);
        }
    }

    public static boolean dataAppContainCtsPkg() {
        File dataAppDir = new File(DATA_APP_LOCATION);
        if (!dataAppDir.exists()) {
            return false;
        }
        String[] pkgList = dataAppDir.list();
        if (pkgList == null || pkgList.length <= 0) {
            return false;
        }
        for (String pkg : pkgList) {
            if (isCtsApp(pkg.substring(0, pkg.length() - 2))) {
                return true;
            }
        }
        return false;
    }

    public static boolean dataAppContainCtsPkgBySig(PackageManagerService mService) {
        boolean result = false;
        synchronized (mService.mPackages) {
            for (Package pkg : mService.mPackages.values()) {
                if (pkg != null && !pkg.isSystemApp() && isCtsPkgBySig(pkg)) {
                    Slog.d(TAG, "dataAppContainCtsPkgBySig found cts : " + pkg.packageName);
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isCtsPkgBySig(Package pkg) {
        String certString = computePackageCertDigest(pkg);
        if (TextUtils.isEmpty(certString)) {
            return false;
        }
        return isCtsSig(buildPackageCertString(pkg.packageName, certString));
    }

    public static boolean isCtsSig(String certString) {
        if (TextUtils.isEmpty(certString)) {
            return false;
        }
        return sLocalCtsSigList.contains(certString);
    }

    public static boolean isCtsAppFileBySig(String packageName, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        boolean result = false;
        boolean hasException = false;
        try {
            Package pkg = new PackageParser().parsePackage(file, 128);
            PackageParser.collectCertificates(pkg, 64);
            if (pkg != null) {
                String certString = computePackageCertDigest(pkg);
                if (!TextUtils.isEmpty(certString)) {
                    result = isCtsSig(buildPackageCertString(pkg.packageName, certString));
                }
            }
        } catch (PackageParserException e) {
            hasException = true;
            Slog.w(TAG, "PackageParserException while isCtsAppFileBySig for " + path);
        } catch (Exception e2) {
            hasException = true;
            Slog.w(TAG, "isCtsAppFileBySig for " + path + " " + e2);
        }
        if (hasException && sFailedParsedCtsPkgList.contains(packageName)) {
            Slog.d(TAG, packageName + " parse failed but in FailedParsedCtsPkgList, silent install still");
            result = true;
        }
        return result;
    }

    public static boolean isCtsAppFileByPkgName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        for (String str : sLocalCtsSigList) {
            String pkg = getPkgFromCertString(str);
            if (pkg != null && pkg.equals(packageName)) {
                return true;
            }
        }
        return sFailedParsedCtsPkgList.contains(packageName);
    }

    private static String getPkgFromCertString(String str) {
        if (TextUtils.isEmpty(str) || !str.contains(" ")) {
            return null;
        }
        String[] splits = str.split(" ");
        if (splits == null || splits.length < 1) {
            return null;
        }
        return splits[0];
    }

    public static String computePackageCertDigest(Package pkg) {
        if (pkg == null || pkg.mSignatures == null || pkg.mSignatures.length == 0 || pkg.mSignatures[0] == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            if (messageDigest == null) {
                return null;
            }
            messageDigest.update(pkg.mSignatures[0].toByteArray());
            byte[] digest = messageDigest.digest();
            if (digest == null || digest.length == 0) {
                return null;
            }
            int digestLength = digest.length;
            char[] chars = new char[(digestLength * 2)];
            for (int i = 0; i < digestLength; i++) {
                int byteHex = digest[i] & 255;
                chars[i * 2] = HEX_ARRAY[byteHex >>> 4];
                chars[(i * 2) + 1] = HEX_ARRAY[byteHex & 15];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String buildPackageCertString(String pkg, String cert) {
        return pkg + " " + cert;
    }

    public static boolean isOppoForceApp(String string) {
        if (sOppoForcePkgList.size() >= 1) {
            return sOppoForcePkgList.contains(string);
        }
        return isExpROM() ? EXP_SYSTEM_FORCE_PACKAGES.contains(string) : SYSTEM_FORCE_PACKAGES.contains(string);
    }

    public static boolean moveOdexToOatDir(Context context) {
        File dataAppDir = new File(DATA_APP_LOCATION);
        if (!dataAppDir.exists()) {
            return false;
        }
        String[] pkgList = dataAppDir.list();
        if (pkgList == null || pkgList.length <= 0) {
            return false;
        }
        for (String pkg : pkgList) {
            String appPkg = pkg.substring(0, pkg.length() - 2);
            if (isOppoApkList(appPkg)) {
                Slog.d(TAG, "skip oppo pkg = " + appPkg);
            } else {
                String arm64OdexStr = DATA_APP_LOCATION + pkg + ODEX_ARM64_FILE;
                File armOdexFile = new File(DATA_APP_LOCATION + pkg + ODEX_ARM_FILE);
                File arm64OdexFile = new File(arm64OdexStr);
                if (armOdexFile.exists() || (arm64OdexFile.exists() ^ 1) == 0) {
                    boolean isArm;
                    if (armOdexFile.exists() && (arm64OdexFile.exists() ^ 1) != 0) {
                        Slog.d(TAG, "Pkg = " + appPkg + " exist arm odex!");
                        isArm = true;
                    } else if (armOdexFile.exists() || !arm64OdexFile.exists()) {
                        Slog.d(TAG, "Pkg = " + appPkg + " exist arm and arm64 odex!");
                    } else {
                        Slog.d(TAG, "Pkg = " + appPkg + " exist arm64 odex!");
                        isArm = false;
                    }
                    String arm64OatStr = DATA_APP_LOCATION + pkg + "/oat" + ODEX_ARM64_FILE;
                    File armOatFile = new File(DATA_APP_LOCATION + pkg + "/oat" + ODEX_ARM_FILE);
                    File arm64OatFile = new File(arm64OatStr);
                    if (armOatFile.exists()) {
                        Slog.d(TAG, "delete oat " + armOatFile);
                        armOatFile.delete();
                    }
                    if (arm64OatFile.exists()) {
                        Slog.d(TAG, "delete oat " + arm64OatFile);
                        arm64OatFile.delete();
                    }
                    if (getAvaiDataSize() < 200) {
                        return false;
                    }
                    File oatFile;
                    if (isArm) {
                        oatFile = new File(DATA_APP_LOCATION + pkg + "/oat/arm");
                        if (!(oatFile.exists() || (oatFile.isDirectory() ^ 1) == 0)) {
                            Slog.d(TAG, "mkdir odex file : " + oatFile);
                            if (oatFile.mkdir()) {
                                oatFile.setLastModified(new File(DATA_APP_LOCATION + pkg + "/base.apk").lastModified());
                            }
                        }
                        if (oatFile.exists()) {
                            moveOdex(armOdexFile, armOatFile);
                        }
                    } else {
                        oatFile = new File(DATA_APP_LOCATION + pkg + "/oat/arm64");
                        if (!(oatFile.exists() || (oatFile.isDirectory() ^ 1) == 0)) {
                            Slog.d(TAG, "mkdir odex file : " + oatFile);
                            if (oatFile.mkdir()) {
                                oatFile.setLastModified(new File(DATA_APP_LOCATION + pkg + "/base.apk").lastModified());
                            }
                        }
                        if (oatFile.exists()) {
                            moveOdex(arm64OdexFile, arm64OatFile);
                        }
                    }
                } else {
                    Slog.d(TAG, "Pkg = " + appPkg + " not exist new odex!");
                }
            }
        }
        return true;
    }

    private static void moveOdex(File fromFile, File toFile) {
        if (fromFile.renameTo(toFile)) {
            Slog.d(TAG, "delete file : " + fromFile);
            fromFile.delete();
            return;
        }
        Slog.e(TAG, "Unable to rename odex file : " + fromFile);
    }

    public static int readEncryptFiles() {
        int result1 = readEncryptFile(OPPO_SYSTEM_APP_PATH);
        int result2 = readEncryptFile(OPPO_VENDOR_APP_PATH);
        if (result1 == -1 || result2 == -1) {
            return -1;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0083 A:{SYNTHETIC, Splitter: B:38:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0073 A:{SYNTHETIC, Splitter: B:28:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x008f A:{SYNTHETIC, Splitter: B:45:0x008f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int readEncryptFile(String filePath) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        FileInputStream fis = null;
        try {
            Slog.d(TAG, "readEncryptFile!!!");
            File path = new File(filePath);
            if (!path.exists()) {
                return -1;
            }
            int len = (int) path.length();
            byte[] buf = new byte[len];
            byte[] b = OPPO_SYSTEM_APP_PWD.getBytes("UTF-8");
            FileInputStream fis2 = new FileInputStream(path);
            try {
                fis2.read(buf);
                for (int i = 0; i < len; i++) {
                    buf[i] = (byte) (buf[i] ^ b[0]);
                    buf[i] = (byte) (~buf[i]);
                }
                String[] line = new String(buf, 0, len).split("\n");
                for (Object add : line) {
                    mOppoApkList.add(add);
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return 0;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                fis = fis2;
                e2.printStackTrace();
                if (fis != null) {
                }
                return -1;
            } catch (IOException e5) {
                e3 = e5;
                fis = fis2;
                try {
                    e3.printStackTrace();
                    if (fis != null) {
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    if (fis != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            e2.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            return -1;
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            return -1;
        }
    }

    public static void oppoCheckSuApp(String codePath, Context ct) throws PackageManagerException {
        IOException e;
        Throwable th;
        if (SystemProperties.getBoolean("debug.restrict.install", true)) {
            File appFile = new File(codePath);
            File codeFile = null;
            if (appFile.exists()) {
                if (PackageParser.isApkFile(appFile)) {
                    codeFile = appFile;
                } else {
                    for (File file : appFile.listFiles()) {
                        if (PackageParser.isApkFile(file)) {
                            Slog.d(TAG, "apk file == " + file);
                            codeFile = file;
                        }
                    }
                }
                ZipInputStream zipInputStream = null;
                if (codeFile == null) {
                    try {
                        Slog.e(TAG, "codeFile is null!!");
                        return;
                    } catch (IOException e2) {
                        e = e2;
                        try {
                            Slog.e(TAG, "oppoCheckSuApp fatal error:" + e);
                            if (zipInputStream != null) {
                                try {
                                    Slog.d(TAG, "check finish!!");
                                    zipInputStream.close();
                                } catch (IOException e3) {
                                    Slog.e(TAG, "oppoCheckSuApp fatal error:" + e3);
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (zipInputStream != null) {
                                try {
                                    Slog.d(TAG, "check finish!!");
                                    zipInputStream.close();
                                } catch (IOException e32) {
                                    Slog.e(TAG, "oppoCheckSuApp fatal error:" + e32);
                                }
                            }
                            throw th;
                        }
                    }
                }
                Slog.d(TAG, "code path == " + codeFile);
                ZipInputStream zin = new ZipInputStream(new FileInputStream(codeFile));
                while (true) {
                    try {
                        ZipEntry entry = zin.getNextEntry();
                        if (entry != null) {
                            String[] splitName = entry.getName().split("/");
                            for (String isRootFile : splitName) {
                                if (isRootFile(isRootFile)) {
                                    showMessageToUSer(ct);
                                    Slog.d(TAG, "This dangerous app!!");
                                    throw new PackageManagerException(-2, "This dangerous app, forbid it");
                                }
                            }
                            zin.closeEntry();
                        } else {
                            if (zin != null) {
                                try {
                                    Slog.d(TAG, "check finish!!");
                                    zin.close();
                                } catch (IOException e322) {
                                    Slog.e(TAG, "oppoCheckSuApp fatal error:" + e322);
                                }
                            }
                        }
                    } catch (IOException e4) {
                        e322 = e4;
                        zipInputStream = zin;
                    } catch (Throwable th3) {
                        th = th3;
                        zipInputStream = zin;
                    }
                }
                return;
            }
            Slog.e(TAG, "appFile is not exist!!");
            return;
        }
        Slog.d(TAG, "debug allow app install");
    }

    private static void showMessageToUSer(Context context) {
        context.sendBroadcast(new Intent(OPPO_FORBID_INSTALL_ACTION));
    }

    private static boolean isRootFile(String file) {
        if (file.equalsIgnoreCase("su") || file.equalsIgnoreCase("root") || file.equalsIgnoreCase("superuser.apk") || file.equalsIgnoreCase("kinguser.apk") || file.equalsIgnoreCase("libsu.so")) {
            return true;
        }
        return file.equalsIgnoreCase("libroot.so");
    }

    private static String getDataFromProvider(Context mContext, String filterName) {
        Cursor cursor = null;
        String xmlValue = null;
        try {
            cursor = mContext.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_NAME_XML}, "filtername=\"" + filterName + "\"", null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                Slog.w(TAG, "The Filtrate app cursor is null !!!");
            } else {
                int xmlColumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                cursor.moveToNext();
                xmlValue = cursor.getString(xmlColumnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
            return xmlValue;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            Slog.w(TAG, "We can not get Filtrate app data from provider,because of " + e);
            return null;
        }
    }

    public static void oppoReadDefaultPkg(Context mContext) {
        initDir();
        initFileObserver();
        readConfigFile();
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c4 A:{SYNTHETIC, Splitter: B:42:0x00c4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_DEFAULT_PKG_CONFIG);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String value;
                                    if (!parser.getName().equals(TAG_OPPO_DEFAULT_APP)) {
                                        if (!parser.getName().equals(TAG_OPPO_FORCE_APP)) {
                                            if (!parser.getName().equals(TAG_OPPO_CTS_APP)) {
                                                if (!parser.getName().equals(TAG_OPPO_CTS_PREFIX)) {
                                                    if (!parser.getName().equals(TAG_OPPO_SANDBOX_SWITCH)) {
                                                        if (!parser.getName().equals(TAG_OPPO_SECUREPAY_SWITCH)) {
                                                            if (!parser.getName().equals(TAG_SPECIAL_SECURE_APP)) {
                                                                if (!parser.getName().equals(TAG_OPPO_ICON_CACHE_MAX_NUM)) {
                                                                    if (!parser.getName().equals(TAG_OPPO_ICON_CACHE_MIN_MEM)) {
                                                                        if (!parser.getName().equals(TAG_SYSTEM_DATA_APP)) {
                                                                            if (!parser.getName().equals(TAG_OPPO_HIDE_APP)) {
                                                                                char[] typeChar = parser.getName().toCharArray();
                                                                                if (typeChar.length <= 3) {
                                                                                    int type = char2int(typeChar);
                                                                                    eventType = parser.next();
                                                                                    if (type >= 0) {
                                                                                        ArrayList<String> tmp = (ArrayList) sPmsWhiteList.get(type);
                                                                                        if (tmp != null) {
                                                                                            tmp.add(parser.getText());
                                                                                            break;
                                                                                        }
                                                                                        tmp = new ArrayList();
                                                                                        tmp.add(parser.getText());
                                                                                        sPmsWhiteList.put(type, tmp);
                                                                                        break;
                                                                                    }
                                                                                }
                                                                            }
                                                                            value = parser.nextText().trim();
                                                                            if (value != null) {
                                                                                Slog.d(TAG, "TAG_OPPO_HIDE_APP : " + value);
                                                                                sOppoHidePkgList.add(value);
                                                                                break;
                                                                            }
                                                                        }
                                                                        value = parser.nextText().trim();
                                                                        if (value != null) {
                                                                            Slog.d(TAG, "TAG_SYSTEM_DATA_APP : " + value);
                                                                            sSystemDataPkgList.add(value);
                                                                            break;
                                                                        }
                                                                    }
                                                                    value = parser.nextText().trim();
                                                                    if (value != null) {
                                                                        Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MIN_MEM : " + value);
                                                                        if (Float.parseFloat(value) >= OppoBrightUtils.MIN_LUX_LIMITI) {
                                                                            sIconCacheMinMemory = Float.parseFloat(value);
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                value = parser.nextText().trim();
                                                                if (value != null) {
                                                                    Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MAX_NUM : " + value);
                                                                    if (Integer.parseInt(value) >= 0) {
                                                                        sIconCacheMaxNum = Integer.parseInt(value);
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            value = parser.nextText();
                                                            if (value != null) {
                                                                Slog.d(TAG, "TAG_SPECIAL_SECURE_APP : " + value);
                                                                sSpecialSecureAppList.add(value);
                                                                break;
                                                            }
                                                        }
                                                        value = parser.nextText();
                                                        if (value != null) {
                                                            Slog.d(TAG, "TAG_OPPO_SECUREPAY_SWITCH : " + value);
                                                            sSecurePaySwitch = value;
                                                            break;
                                                        }
                                                    }
                                                    value = parser.nextText();
                                                    if (value != null) {
                                                        Slog.d(TAG, "TAG_OPPO_SANDBOX_SWITCH : " + value);
                                                        setSandboxSwitchState(value);
                                                        break;
                                                    }
                                                }
                                                value = parser.nextText();
                                                if (value != null) {
                                                    Slog.d(TAG, "TAG_OPPO_CTS_PREFIX : " + value);
                                                    sOppoCtsPrefixList.add(value);
                                                    break;
                                                }
                                            }
                                            value = parser.nextText();
                                            if (value != null) {
                                                Slog.d(TAG, "TAG_OPPO_CTS_APP : " + value);
                                                sOppoCtsPkgList.add(value);
                                                break;
                                            }
                                        }
                                        value = parser.nextText();
                                        if (value != null) {
                                            Slog.d(TAG, "TAG_OPPO_FORCE_APP : " + value);
                                            sOppoForcePkgList.add(value);
                                            break;
                                        }
                                    }
                                    value = parser.nextText();
                                    if (value != null) {
                                        Slog.d(TAG, "TAG_OPPO_DEFAULT_APP : " + value);
                                        sOppoDefaultPkgList.add(value);
                                        break;
                                    }
                                    break;
                            }
                        }
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e2) {
                                Slog.w(TAG, "Got execption close permReader.", e2);
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = xmlReader;
                        try {
                            Slog.w(TAG, "Got execption parsing permissions.", e);
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e22) {
                                    Slog.w(TAG, "Got execption close permReader.", e22);
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException e222) {
                                Slog.w(TAG, "Got execption close permReader.", e222);
                            }
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_pms_defaultpackage_list file " + xmlFile);
                }
            } catch (Exception e5) {
                e = e5;
            }
        }
    }

    private static int char2int(char[] in) {
        int out = 0;
        if (in.length < 1) {
            return -1;
        }
        for (int n = 0; n < in.length; n++) {
            out = (int) (((double) out) + (((double) (in[n] - 97)) * Math.pow(26.0d, (double) ((in.length - n) - 1))));
        }
        return out;
    }

    public static boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        if (sPmsWhiteList.indexOfKey(type) < 0) {
            return defaultList != null && defaultList.contains(verifyStr);
        } else {
            if (((ArrayList) sPmsWhiteList.get(type)).contains(verifyStr)) {
                return true;
            }
        }
    }

    private static boolean parseXmlValue(String xmlValue) {
        if (xmlValue == null || xmlValue.isEmpty()) {
            Slog.d(TAG, "xmlValue is null !!!");
            return false;
        }
        boolean bSuccess = false;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(xmlValue));
            parser.nextTag();
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    String value;
                    if (TAG_OPPO_DEFAULT_APP.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            sOppoDefaultPkgList.add(value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_OPPO_FORCE_APP.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            sOppoForcePkgList.add(value);
                            continue;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } while (type != 1);
            bSuccess = true;
        } catch (NullPointerException e) {
            Slog.w(TAG, "failed parsing ", e);
        } catch (XmlPullParserException e2) {
            Slog.w(TAG, "failed parsing ", e2);
        } catch (IOException e3) {
            Slog.w(TAG, "failed parsing ", e3);
        } catch (IndexOutOfBoundsException e4) {
            Slog.w(TAG, "failed parsing ", e4);
        }
        return bSuccess;
    }

    private static void initDir() {
        File defaultAppFilePath = new File(OPPO_DEFAULT_PKG_PATH);
        File defaultAppConfigPath = new File(OPPO_DEFAULT_PKG_CONFIG);
        try {
            if (!defaultAppFilePath.exists()) {
                defaultAppFilePath.mkdirs();
            }
            if (!defaultAppConfigPath.exists()) {
                defaultAppConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init defaultAppConfigPath Dir failed!!!");
        }
    }

    private static void initFileObserver() {
        sConfigFileObserver = new FileObserverPolicy(OPPO_DEFAULT_PKG_CONFIG);
        sConfigFileObserver.startWatching();
    }

    public static int getAvaiDataSize() {
        StatFs sf = new StatFs("/data");
        return (int) ((sf.getBlockSizeLong() * sf.getAvailableBlocksLong()) / OppoDeviceStorageMonitorService.MB_BYTES);
    }

    public static boolean isSetContainsOppoDefaultPkg(ComponentName[] set, ComponentName activity) {
        String PKG_OPPO_LAUNCHER = "com.oppo.launcher";
        if (set == null || activity == null || activity.getPackageName() == null) {
            return false;
        }
        String dstPkg = activity.getPackageName();
        if (isExpROM() ? EXP_SYSTEM_DEFAULT_PACKAGES.contains(dstPkg) : SYSTEM_DEFAULT_PACKAGES.contains(dstPkg)) {
            return false;
        }
        boolean result = false;
        for (ComponentName comp : set) {
            if (comp != null) {
                String pkg = comp.getPackageName();
                if (pkg == null) {
                    continue;
                } else {
                    int contains;
                    if (!isExpROM()) {
                        contains = SYSTEM_DEFAULT_PACKAGES.contains(pkg);
                    } else if (EXP_SYSTEM_DEFAULT_PACKAGES.contains(pkg)) {
                        contains = "com.oppo.launcher".equals(pkg) ^ 1;
                    } else {
                        continue;
                    }
                    if (contains != 0) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static boolean isQueryListContainsOppoDefaultPkg(List<ResolveInfo> query) {
        if (query == null) {
            return false;
        }
        boolean result = false;
        int len = query.size();
        for (int i = 0; i < len; i++) {
            ResolveInfo ri = (ResolveInfo) query.get(i);
            if (!(ri == null || ri.activityInfo == null)) {
                String pkg = ri.activityInfo.packageName;
                if (pkg == null) {
                    continue;
                } else {
                    boolean contains;
                    if (isExpROM()) {
                        contains = EXP_SYSTEM_DEFAULT_PACKAGES.contains(pkg);
                    } else {
                        contains = SYSTEM_DEFAULT_PACKAGES.contains(pkg);
                    }
                    if (contains) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static boolean isCttlApp(String pkg) {
        boolean isCttlPkg = false;
        if (pkg == null) {
            return false;
        }
        if (CTTL_PACKAGE_LIST.contains(pkg)) {
            isCttlPkg = true;
        }
        return isCttlPkg;
    }

    public static void initRuntimeFilterInfos() {
        File systemConfigPah = new File("/data/system/config");
        File runtimeFilterFilePath = new File(OPPO_RUNTIME_PERM_FILTER_FILE);
        try {
            if (!systemConfigPah.exists()) {
                systemConfigPah.mkdirs();
            }
            if (!runtimeFilterFilePath.exists()) {
                runtimeFilterFilePath.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init runtimeFilterFilePath Dir failed!!!");
        }
        parseRuntimePermFilterInfos();
    }

    public static ArrayList<RuntimePermFilterInfo> getDefaultPermFilterInfosFromStr(ArrayList<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<RuntimePermFilterInfo> tempList = new ArrayList();
        for (String value : list) {
            RuntimePermFilterInfo info = new RuntimePermFilterInfo();
            if (value.contains("#")) {
                String[] splits = value.split("#");
                if (splits.length >= 2) {
                    info.mPackageName = splits[0];
                    info.mAddAll = false;
                    ArrayList<String> groups = new ArrayList();
                    for (int i = 1; i < splits.length; i++) {
                        String group = splits[i];
                        if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                            groups.add(group);
                        }
                    }
                    info.mGroups = groups;
                } else {
                    info.mPackageName = splits[0];
                    info.mAddAll = true;
                }
            } else {
                info.mPackageName = value;
                info.mAddAll = true;
            }
            tempList.add(info);
        }
        return tempList;
    }

    public static ArrayList<RuntimePermFilterInfo> getFixedRuntimePermInfos(boolean overSea) {
        if (overSea) {
            if (sOppoOverseaFixedPermInfos == null || (sOppoOverseaFixedPermInfos.isEmpty() ^ 1) == 0) {
                return getDefaultPermFilterInfosFromStr(OPPO_OVERSEA_FIXED_PERM_LIST);
            }
            return sOppoOverseaFixedPermInfos;
        } else if (sOppoFixedPermInfos == null || (sOppoFixedPermInfos.isEmpty() ^ 1) == 0) {
            return getDefaultPermFilterInfosFromStr(OPPO_FIXED_PERM_LIST);
        } else {
            return sOppoFixedPermInfos;
        }
    }

    public static ArrayList<RuntimePermFilterInfo> getNonFixedRuntimePermInfos(boolean overSea) {
        if (overSea) {
            return getDefaultPermFilterInfosFromStr(OPPO_OVERSEA_NONFIXED_PERM_LIST);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00e1 A:{SYNTHETIC, Splitter: B:51:0x00e1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void parseRuntimePermFilterInfos() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_RUNTIME_PERM_FILTER_FILE);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    Reader fileReader2 = new FileReader(xmlFile);
                    try {
                        parser.setInput(fileReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String value;
                                    RuntimePermFilterInfo info;
                                    String[] splits;
                                    ArrayList<String> groups;
                                    int i;
                                    String group;
                                    if (!parser.getName().equals(TAG_FIXED_RUNTIME_PERM)) {
                                        if (parser.getName().equals(TAG_OVERSEA_FIXED_RUNTIME_PERM)) {
                                            value = parser.nextText();
                                            if (value != null) {
                                                info = new RuntimePermFilterInfo();
                                                info.mOverSea = true;
                                                if (value.contains("#")) {
                                                    splits = value.split("#");
                                                    if (splits.length >= 2) {
                                                        info.mPackageName = splits[0];
                                                        info.mAddAll = false;
                                                        groups = new ArrayList();
                                                        for (i = 1; i < splits.length; i++) {
                                                            group = splits[i];
                                                            if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                                                                groups.add(group);
                                                            }
                                                        }
                                                        info.mGroups = groups;
                                                    } else {
                                                        info.mPackageName = splits[0];
                                                        info.mAddAll = true;
                                                    }
                                                } else {
                                                    info.mPackageName = value;
                                                    info.mAddAll = true;
                                                }
                                                sOppoOverseaFixedPermInfos.add(info);
                                                break;
                                            }
                                        }
                                    }
                                    value = parser.nextText();
                                    if (value != null) {
                                        info = new RuntimePermFilterInfo();
                                        info.mOverSea = false;
                                        if (value.contains("#")) {
                                            splits = value.split("#");
                                            if (splits.length >= 2) {
                                                info.mPackageName = splits[0];
                                                info.mAddAll = false;
                                                groups = new ArrayList();
                                                for (i = 1; i < splits.length; i++) {
                                                    group = splits[i];
                                                    if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                                                        groups.add(group);
                                                    }
                                                }
                                                info.mGroups = groups;
                                            } else {
                                                info.mPackageName = splits[0];
                                                info.mAddAll = true;
                                            }
                                        } else {
                                            info.mPackageName = value;
                                            info.mAddAll = true;
                                        }
                                        sOppoFixedPermInfos.add(info);
                                        break;
                                    }
                                    break;
                            }
                        }
                        if (fileReader2 != null) {
                            try {
                                fileReader2.close();
                            } catch (IOException e2) {
                                Slog.w(TAG, "Got execption close permReader.", e2);
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = fileReader2;
                        try {
                            Slog.w(TAG, "Got execption parsing permissions.", e);
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e22) {
                                    Slog.w(TAG, "Got execption close permReader.", e22);
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e222) {
                                    Slog.w(TAG, "Got execption close permReader.", e222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = fileReader2;
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_pms_runtimeperm_filter_list file ");
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            Slog.d(TAG, "sys_pms_runtimeperm_filter_list.xml not exist");
        }
    }

    public static void sendDcsSilentInstallBroadcast(String packageName, Bundle extras, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (!(am == null || packageName == null)) {
            String packageinstaller = "com.android.packageinstaller";
            if (installerPackageName == null || !packageinstaller.equals(installerPackageName)) {
                String packageinstallerExp = "com.google.android.packageinstaller";
                if (installerPackageName == null || !packageinstallerExp.equals(installerPackageName)) {
                    try {
                        Uri fromParts;
                        String str = "oppo.intent.action.OPPO_PACKAGE_ADDED";
                        if (packageName != null) {
                            fromParts = Uri.fromParts("package", packageName, null);
                        } else {
                            fromParts = null;
                        }
                        Intent intent = new Intent(str, fromParts);
                        if (extras != null) {
                            intent.putExtras(extras);
                        }
                        intent.setPackage(PACKAGE_NEARME_STATISTICS);
                        String str2 = "oppo_extra_pkg_name";
                        if (installerPackageName == null) {
                            installerPackageName = "";
                        }
                        intent.putExtra(str2, installerPackageName);
                        am.broadcastIntent(null, intent, null, null, 0, null, null, null, -1, null, true, false, userId);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static void sendOppoStartInstallBro(String path, String installerPackageName, String packageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && path != null) {
            try {
                Intent intent = new Intent(ACTION_OPPO_START_INSTALL);
                String str = "apkPath";
                if (path == null) {
                    path = "";
                }
                intent.putExtra(str, path);
                str = "installerPackageName";
                if (installerPackageName == null) {
                    installerPackageName = "";
                }
                intent.putExtra(str, installerPackageName);
                str = "packageName";
                if (packageName == null) {
                    packageName = "";
                }
                intent.putExtra(str, packageName);
                intent.setPackage("com.oppo.launcher");
                am.broadcastIntent(null, intent, null, null, 0, null, null, new String[]{"oppo.permission.OPPO_COMPONENT_SAFE"}, -1, null, true, false, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void sendOppoInstallFailBro(String packageName, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            try {
                Uri fromParts;
                String str = ACTION_OPPO_INSTALL_FAILED;
                if (packageName != null) {
                    fromParts = Uri.fromParts("package", packageName, null);
                } else {
                    fromParts = null;
                }
                Intent intent = new Intent(str, fromParts);
                String str2 = "installerPackageName";
                if (installerPackageName == null) {
                    installerPackageName = "";
                }
                intent.putExtra(str2, installerPackageName);
                str2 = "packageName";
                if (packageName == null) {
                    packageName = "";
                }
                intent.putExtra(str2, packageName);
                String[] requiredPermissions = new String[]{"oppo.permission.OPPO_COMPONENT_SAFE"};
                intent.setPackage("com.oppo.launcher");
                am.broadcastIntent(null, intent, null, null, 0, null, null, requiredPermissions, -1, null, true, false, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int getIconCacheMaxNum() {
        if (getTotalMemorySize() <= sIconCacheMinMemory) {
            sIconCacheMaxNum = 0;
        }
        Slog.d(TAG, "sIconCacheMaxNum:" + sIconCacheMaxNum + ",sIconCacheMinMemory:" + sIconCacheMinMemory);
        return sIconCacheMaxNum;
    }

    public static void sendDcsNonSilentInstallBroadcastExp(String packageName, boolean updateState, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            String oppoExtraPid = OppoPackageManagerHelper.OPPO_EXTRA_PID;
            String oppoExtraUid = OppoPackageManagerHelper.OPPO_EXTRA_UID;
            String oppoExtraPkgName = "oppo_extra_pkg_name";
            String oppoExtraInstallPackage = "oppo_extra_install_package";
            String actionOppoDcsCallerInfo = "oppo.intent.action.oppo.dcs.caller.info";
            try {
                Intent intent = new Intent();
                intent.setAction(actionOppoDcsCallerInfo);
                intent.setPackage(PACKAGE_NEARME_STATISTICS);
                intent.putExtra(oppoExtraPid, -99);
                intent.putExtra(oppoExtraUid, -99);
                intent.putExtra(oppoExtraPkgName, installerPackageName);
                intent.putExtra("android.intent.extra.REPLACING", updateState);
                if (packageName == null) {
                    packageName = "";
                }
                intent.putExtra(oppoExtraInstallPackage, packageName);
                am.broadcastIntent(null, intent, null, null, 0, null, null, null, -1, null, true, false, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isPrivilegedHideApp(String packageName) {
        if (packageName == null) {
            return false;
        }
        return SKIP_APP_PACKAGE_LIST.contains(packageName);
    }

    public static void addPrivilegedHideApp() {
        String CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
        List<String> enterpriseAppList = loadGovEnterpriseAppList("/system/etc/oppo_customize_whitelist.xml");
        if (enterpriseAppList != null && enterpriseAppList.size() > 0) {
            SKIP_APP_PACKAGE_LIST.addAll(enterpriseAppList);
        }
    }

    public static ArrayList<String> getIgnoreAppList() {
        return SKIP_APP_PACKAGE_LIST;
    }

    public static boolean isSafeCenterApp(String packageName) {
        boolean z = true;
        if (packageName == null) {
            return false;
        }
        if (sOppoHidePkgList.size() < 1) {
            if (!SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName)) {
                z = DEFAULT_OPPO_HIDE_APP_LIST.contains(packageName);
            }
            return z;
        }
        if (!SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName)) {
            z = sOppoHidePkgList.contains(packageName);
        }
        return z;
    }

    public static boolean isForbiddenUninstallApp(IPackageManager pm, String packageName, int callingUid) {
        boolean z = false;
        boolean isSystemCaller = true;
        try {
            String callerName = pm.getNameForUid(callingUid);
            if (callingUid < 10000) {
                isSystemCaller = true;
            } else if (callerName != null) {
                if (callerName.contains(":")) {
                    String[] shareName = callerName.split(":");
                    if (!(shareName[0] == null || shareName[1] == null)) {
                        if (OppoPackageManagerHelper.isShareUid(shareName[0])) {
                            isSystemCaller = true;
                        }
                        String[] shareUidPkg = pm.getPackagesForUid(Integer.parseInt(shareName[1]));
                        if (!(shareUidPkg == null || shareUidPkg[0] == null)) {
                            isSystemCaller = isOppoApkList(shareUidPkg[0]);
                        }
                    }
                } else {
                    isSystemCaller = isOppoApkList(callerName);
                }
            }
            if (!isSystemCaller) {
                z = sOppoHidePkgList.contains(packageName);
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOppoHideApp(String packageName) {
        if (packageName == null) {
            return false;
        }
        if (sOppoHidePkgList.size() < 1) {
            return DEFAULT_OPPO_HIDE_APP_LIST.contains(packageName);
        }
        return sOppoHidePkgList.contains(packageName);
    }

    public static float getTotalMemorySize() {
        if (sTotalMemorySize < OppoBrightUtils.MIN_LUX_LIMITI) {
            MemInfoReader reader = new MemInfoReader();
            reader.readMemInfo();
            sTotalMemorySize = ((float) reader.getTotalSize()) / 1.07374182E9f;
        }
        return sTotalMemorySize;
    }

    public static void initNotLaunchedList() {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (file.exists()) {
            sNotLaunchedPkgs = readNotLaunchedListFromFile();
            for (String str : sNotLaunchedPkgs) {
                Slog.d(TAG, "init not launched ~~~~~ " + str);
            }
            return;
        }
        try {
            if (!file.createNewFile()) {
                Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
            }
            Runtime.getRuntime().exec("chmod 774 /data/oppo/common//notLaunchedPkgs.xml");
        } catch (IOException e) {
        }
    }

    public static void initNotLaunchedList(PackageManagerService service, boolean isFirstBoot) {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
                }
                Runtime.getRuntime().exec("chmod 774 /data/oppo/common//notLaunchedPkgs.xml");
            } catch (IOException e) {
                return;
            }
        }
        if (file.exists()) {
            if (!isFirstBoot) {
                sNotLaunchedPkgs = readNotLaunchedListFromFile();
                for (String str : sNotLaunchedPkgs) {
                    Slog.d(TAG, "init not launched " + str);
                }
            } else if (service != null && service.mPackages != null) {
                File buildinThirdpartFile = new File(BUILDIN_NOT_LAUNCHED_PATH);
                if (buildinThirdpartFile.exists()) {
                    List<String> buildinDataList = readFromFileLocked(buildinThirdpartFile);
                    if (!(buildinDataList == null || (buildinDataList.isEmpty() ^ 1) == 0)) {
                        for (String name : buildinDataList) {
                            if (!(TextUtils.isEmpty(name) || service.mPackages.get(name) == null)) {
                                Slog.d(TAG, "first-boot find not-Launch installed app " + name);
                                sNotLaunchedPkgs.add(name);
                            }
                        }
                        if (!(sNotLaunchedPkgs == null || (sNotLaunchedPkgs.isEmpty() ^ 1) == 0)) {
                            writeNotLaunchedListToFile(sNotLaunchedPkgs);
                        }
                    }
                }
            }
        }
    }

    private static ArrayList<String> readNotLaunchedListFromFile() {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
                    return new ArrayList();
                }
            } catch (IOException e) {
            }
        }
        return (ArrayList) readFromFileLocked(file);
    }

    private static void writeNotLaunchedListToFile(ArrayList<String> list) {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    Runtime.getRuntime().exec("chmod 774 /data/oppo/common//notLaunchedPkgs.xml");
                } else {
                    Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
                    return;
                }
            } catch (IOException e) {
            }
        }
        writeDataToFile(file, list);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00ef A:{SYNTHETIC, Splitter: B:47:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ca A:{SYNTHETIC, Splitter: B:41:0x00ca} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a6 A:{SYNTHETIC, Splitter: B:35:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0083 A:{SYNTHETIC, Splitter: B:29:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0060 A:{SYNTHETIC, Splitter: B:23:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0114 A:{SYNTHETIC, Splitter: B:53:0x0114} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static List<String> readFromFileLocked(File file) {
        Throwable th;
        FileInputStream stream = null;
        List<String> list = new ArrayList();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String pkg = parser.getAttributeValue(null, "att");
                            if (pkg != null) {
                                list.add(pkg);
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e);
                    }
                }
                stream = stream2;
            } catch (NullPointerException e2) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e3);
                    }
                }
                return list;
            } catch (NumberFormatException e4) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e32) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e32);
                    }
                }
                return list;
            } catch (XmlPullParserException e5) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e322) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e322);
                    }
                }
                return list;
            } catch (IOException e6) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3222) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e3222);
                    }
                }
                return list;
            } catch (IndexOutOfBoundsException e7) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e32222) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e32222);
                    }
                }
                return list;
            } catch (Throwable th2) {
                th = th2;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e322222) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e322222);
                    }
                }
                throw th;
            }
        } catch (NullPointerException e8) {
            if (stream != null) {
            }
            return list;
        } catch (NumberFormatException e9) {
            if (stream != null) {
            }
            return list;
        } catch (XmlPullParserException e10) {
            if (stream != null) {
            }
            return list;
        } catch (IOException e11) {
            if (stream != null) {
            }
            return list;
        } catch (IndexOutOfBoundsException e12) {
            if (stream != null) {
            }
            return list;
        } catch (Throwable th3) {
            th = th3;
            if (stream != null) {
            }
            throw th;
        }
        return list;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00e7 A:{SYNTHETIC, Splitter: B:41:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c3 A:{SYNTHETIC, Splitter: B:35:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a0 A:{SYNTHETIC, Splitter: B:29:0x00a0} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x007d A:{SYNTHETIC, Splitter: B:23:0x007d} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010c A:{SYNTHETIC, Splitter: B:47:0x010c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeDataToFile(File file, List<String> list) {
        Throwable th;
        if (file != null) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos2, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.startTag(null, "gs");
                    for (int i = 0; i < list.size(); i++) {
                        String pkg = (String) list.get(i);
                        if (pkg != null) {
                            serializer.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                            serializer.attribute(null, "att", pkg);
                            serializer.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                        }
                    }
                    serializer.endTag(null, "gs");
                    serializer.endDocument();
                    serializer.flush();
                    if (fileos2 != null) {
                        try {
                            fileos2.close();
                        } catch (IOException e) {
                            Slog.i(TAG, "failed close stream " + e);
                        }
                    }
                    fileos = fileos2;
                } catch (IllegalArgumentException e2) {
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                } catch (IllegalStateException e3) {
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                } catch (IOException e4) {
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                } catch (Exception e5) {
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e6) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e7) {
                        Slog.i(TAG, "failed close stream " + e7);
                    }
                }
            } catch (IllegalStateException e8) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e72) {
                        Slog.i(TAG, "failed close stream " + e72);
                    }
                }
            } catch (IOException e9) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e722) {
                        Slog.i(TAG, "failed close stream " + e722);
                    }
                }
            } catch (Exception e10) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e7222) {
                        Slog.i(TAG, "failed close stream " + e7222);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e72222) {
                        Slog.i(TAG, "failed close stream " + e72222);
                    }
                }
                throw th;
            }
        }
    }

    public static boolean addPkgToNotLaunchedList(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        boolean result = false;
        synchronized (sNotLaunchedPkgs) {
            if (!sNotLaunchedPkgs.contains(pkg)) {
                Slog.d(TAG, "addPkgToNotLaunchedList " + pkg);
                sNotLaunchedPkgs.add(pkg);
                new Thread(new SyncNotLaunchedPkgsToFileRunnable(pkg, false)).start();
                result = true;
            }
        }
        return result;
    }

    public static boolean removePkgFromNotLaunchedList(String pkg, boolean notify) {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        boolean result = false;
        synchronized (sNotLaunchedPkgs) {
            if (sNotLaunchedPkgs.contains(pkg)) {
                Slog.d(TAG, "removePkgFromNotLaunchedList " + pkg);
                sNotLaunchedPkgs.remove(pkg);
                new Thread(new SyncNotLaunchedPkgsToFileRunnable(pkg, notify)).start();
                result = true;
            }
        }
        return result;
    }

    public static void sendDcsPreventUninstallSystemApp(Context context, String callingPackage, String packageName) {
        if (context != null) {
            try {
                HashMap<String, String> map = new HashMap();
                map.put("caller_pkg", callingPackage);
                map.put("app_pkg", packageName);
                OppoStatistics.onCommon(context, "20120", ACTION_EVENTID_PREVENT_UNINSTALL, map, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean forbiddenSetPreferredActivity(PackageManagerService pms, IntentFilter filter) {
        String PKG_CHILDREN = "com.coloros.childrenspace";
        String KEY_CHILDREN_MODE = "children_mode_on";
        String FEATURE_CHILDREN_MODE = "oppo.childspace.support";
        if (!(pms == null || filter == null || !pms.hasSystemFeature("oppo.childspace.support", 0))) {
            String callingPkg = OppoPackageManagerHelper.getProcessNameByPid(Binder.getCallingPid());
            if (Global.getInt(pms.mContext.getContentResolver(), "children_mode_on", 0) == 1 && ("com.coloros.childrenspace".equals(callingPkg) ^ 1) != 0 && filter.hasCategory("android.intent.category.HOME")) {
                Slog.d(TAG, "forbidden set launcher in children mode, skip!");
                return true;
            }
        }
        return false;
    }

    public static void filterBlackList(Context context, Intent intent, List<ResolveInfo> query) {
        if (query != null && !query.isEmpty()) {
            ComponentName comp = intent.getComponent();
            if (comp == null && intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
            if (comp != null) {
                Slog.i(TAG, "filterBlackList, skip for intent component=" + comp);
                return;
            }
            String pkgName = intent.getPackage();
            if (TextUtils.isEmpty(pkgName)) {
                List<String> blackList = ColorResolveInfoHelper.getInstance(context).getCloudBlackList(intent);
                if (!(blackList == null || (blackList.isEmpty() ^ 1) == 0)) {
                    Collection black = null;
                    for (ResolveInfo ri : query) {
                        if (blackList.contains(ri.activityInfo.packageName)) {
                            if (black == null) {
                                black = new ArrayList();
                            }
                            black.add(ri);
                        }
                    }
                    if (black != null) {
                        query.removeAll(black);
                    }
                }
                return;
            }
            Slog.i(TAG, "filterBlackList, skip for intent package=" + pkgName);
        }
    }

    public static boolean hasFileManagerOpenFlag(Intent intent, String callingPackage) {
        String extraOpenFlag = "oppo_filemanager_openflag";
        String PACKAGE_FILE_MANAGER = "com.coloros.filemanager";
        if ("com.coloros.filemanager".equals(callingPackage)) {
            boolean openFlag;
            try {
                openFlag = intent.getBooleanExtra("oppo_filemanager_openflag", false);
            } catch (Exception e) {
                e.printStackTrace();
                openFlag = false;
            }
            return openFlag;
        }
        try {
            if (intent.hasExtra("oppo_filemanager_openflag")) {
                Slog.d(TAG, "hasFileManagerOpenFlag, remove openFlag, callingPackage = " + callingPackage);
                intent.removeExtra("oppo_filemanager_openflag");
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return false;
    }

    private static boolean inDefaultAppWhiteList(String callerName, Intent intent) {
        String SPLIT_CHAR = ",";
        if (intent == null) {
            return false;
        }
        ArrayList<String> whiteList;
        if (sPmsWhiteList.indexOfKey(677) >= 0) {
            whiteList = (ArrayList) sPmsWhiteList.get(677);
            if (whiteList == null) {
                return false;
            }
        }
        whiteList = DEFAULT_APP_WHITE_LIST;
        for (String rule : whiteList) {
            String[] splitRuleArray = rule.split(",");
            if (splitRuleArray.length >= 4 && isStrEqual(splitRuleArray[0], callerName) && isStrEqual(splitRuleArray[1], intent.getAction()) && isStrEqual(splitRuleArray[2], intent.getType()) && isStrEqual(splitRuleArray[3], intent.getDataString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStrEqual(String pattern, String src) {
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        boolean result;
        if (TextUtils.isEmpty(src)) {
            src = " ";
        }
        try {
            result = Pattern.compile(pattern).matcher(src).matches();
        } catch (Exception e) {
            result = pattern.equals(src);
        }
        return result;
    }

    public static boolean isDefaultAppEnabled(String callerName, boolean isCtsAppInstall, Intent intent) {
        boolean z = false;
        if (inDefaultAppWhiteList(callerName, intent)) {
            Slog.d(TAG, "inDefaultAppWhiteList:" + callerName);
            return false;
        }
        if (!(callerName == null || (callerName.contains("android.uid.nfc") ^ 1) == 0 || (callerName.equals("com.android.cts.stub") ^ 1) == 0 || (callerName.equals("com.android.cts.normalapp") ^ 1) == 0 || (callerName.equals("com.android.cts.deviceandprofileowner") ^ 1) == 0)) {
            if (!callerName.contains("android.uid.system")) {
                isCtsAppInstall = false;
            }
            z = isCtsAppInstall ^ 1;
        }
        return z;
    }

    public static int getOppoFreezePackageState(String pkgName, int userId) {
        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
        if (freezeInfoMap == null) {
            return 0;
        }
        OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.get(pkgName);
        if (info == null) {
            return 0;
        }
        return info.mState;
    }

    public static boolean inOppoFreezePackageList(String pkgName, int userId) {
        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
        if (freezeInfoMap == null) {
            return false;
        }
        OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.get(pkgName);
        if (info == null) {
            return false;
        }
        return info.mState == 2 || info.mState == 1;
    }

    public static List<String> getOppoFreezedPackageList(int userId) {
        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
        if (freezeInfoMap == null) {
            return null;
        }
        List<String> list = new ArrayList();
        for (int i = 0; i < freezeInfoMap.size(); i++) {
            OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.valueAt(i);
            if (info != null && (info.mState == 2 || info.mState == 1)) {
                list.add(info.mPackageName);
            }
        }
        return list;
    }

    public static int getOppoPackageFreezeFlag(String pkgName, int userId) {
        if (TextUtils.isEmpty(pkgName)) {
            return 0;
        }
        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
        if (freezeInfoMap != null) {
            OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.get(pkgName);
            if (!(info == null || (TextUtils.isEmpty(info.mPackageName) ^ 1) == 0)) {
                return info.mFreezeFlag;
            }
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00fa A:{SYNTHETIC, Splitter: B:54:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00f1 A:{SYNTHETIC, Splitter: B:49:0x00f1} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e5 A:{SYNTHETIC, Splitter: B:42:0x00e5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void syncFreezeInfoToDisk() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (mOppoFreezeInfoUserMap != null) {
            File file = new File("/data/oppo/coloros/freeze/freeze-packages.xml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(file);
                try {
                    XmlSerializer out = Xml.newSerializer();
                    out.setOutput(stream2, "utf-8");
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "gs");
                    for (int i = 0; i < mOppoFreezeInfoUserMap.size(); i++) {
                        int userId = ((Integer) mOppoFreezeInfoUserMap.keyAt(i)).intValue();
                        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.valueAt(i);
                        if (freezeInfoMap != null) {
                            for (int j = 0; j < freezeInfoMap.size(); j++) {
                                OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.valueAt(j);
                                if (!(TextUtils.isEmpty((String) freezeInfoMap.keyAt(j)) || info == null || (TextUtils.isEmpty(info.mPackageName) ^ 1) == 0)) {
                                    out.startTag(null, "package");
                                    out.attribute(null, "name", info.mPackageName);
                                    out.attribute(null, "userId", String.valueOf(info.mUserId));
                                    out.attribute(null, "state", String.valueOf(info.mState));
                                    out.attribute(null, "flag", String.valueOf(info.mFreezeFlag));
                                    out.endTag(null, "package");
                                }
                            }
                        }
                    }
                    out.endTag(null, "gs");
                    out.endDocument();
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e4) {
                        }
                    }
                    stream = stream2;
                } catch (FileNotFoundException e5) {
                    e2 = e5;
                    stream = stream2;
                    e2.printStackTrace();
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e6) {
                        }
                    }
                } catch (IOException e7) {
                    e3 = e7;
                    stream = stream2;
                    try {
                        e3.printStackTrace();
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e8) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e9) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e2 = e10;
                e2.printStackTrace();
                if (stream != null) {
                }
            } catch (IOException e11) {
                e3 = e11;
                e3.printStackTrace();
                if (stream != null) {
                }
            }
        }
    }

    public static void initMapFromDisk() {
        Slog.i(TAG, "init oppo-dis map from disk");
        if (mOppoFreezeInfoUserMap == null) {
            mOppoFreezeInfoUserMap = new ArrayMap();
        }
        File file = new File("/data/oppo/coloros/freeze/freeze-packages.xml");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fileReader);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                if (parser.getName().equals("package")) {
                                    String userIdText = parser.getAttributeValue(null, "userId");
                                    int userId = -1;
                                    if (!TextUtils.isEmpty(userIdText)) {
                                        try {
                                            userId = Integer.parseInt(userIdText);
                                        } catch (NumberFormatException e) {
                                            userId = -1;
                                        }
                                    }
                                    if (userId != -1) {
                                        ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
                                        if (freezeInfoMap == null) {
                                            freezeInfoMap = new ArrayMap();
                                            mOppoFreezeInfoUserMap.put(Integer.valueOf(userId), freezeInfoMap);
                                        }
                                        String name = parser.getAttributeValue(null, "name");
                                        int state = -1;
                                        try {
                                            state = Integer.parseInt(parser.getAttributeValue(null, "state"));
                                        } catch (Exception e2) {
                                        }
                                        int flag = -1;
                                        try {
                                            flag = Integer.parseInt(parser.getAttributeValue(null, "flag"));
                                        } catch (Exception e3) {
                                        }
                                        if (!(TextUtils.isEmpty(name) || state == -1 || flag == -1)) {
                                            freezeInfoMap.put(name, new OppoFreezeInfo(name, userId, state, flag));
                                            break;
                                        }
                                    }
                                }
                                break;
                        }
                    }
                } catch (XmlPullParserException e4) {
                    Slog.w(TAG, "Got execption parsing permissions.", e4);
                } catch (IOException e5) {
                    Slog.w(TAG, "Got execption parsing permissions.", e5);
                }
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e52) {
                        e52.printStackTrace();
                    }
                }
                return;
            } catch (FileNotFoundException e6) {
                Slog.w(TAG, "Couldn't find or open oppo_cts_list file " + file);
                return;
            }
        }
        Slog.e(TAG, "initMapFromDisk can't find file");
    }

    public static void sendTempUnfreezeBro(String packageName, int state, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && (TextUtils.isEmpty(packageName) ^ 1) != 0) {
            try {
                Intent intent = new Intent(ACTION_OPPO_TEMP_UNFREEZE);
                intent.putExtra("packageName", packageName);
                intent.putExtra("freezeState", state);
                intent.putExtra("userId", userId);
                intent.addFlags(16777216);
                am.broadcastIntent(null, intent, null, null, 0, null, null, new String[]{"oppo.permission.OPPO_COMPONENT_SAFE"}, -1, null, true, false, 0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void dumpOppoFreezeInfo(PrintWriter pw, String[] args) {
        if (mOppoFreezeInfoUserMap == null) {
            pw.println("no freeze info map found");
            return;
        }
        for (int i = 0; i < mOppoFreezeInfoUserMap.size(); i++) {
            pw.println("USER " + ((Integer) mOppoFreezeInfoUserMap.keyAt(i)).intValue());
            ArrayMap<String, OppoFreezeInfo> freezeInfoMap = (ArrayMap) mOppoFreezeInfoUserMap.valueAt(i);
            if (freezeInfoMap != null) {
                for (int j = 0; j < freezeInfoMap.size(); j++) {
                    OppoFreezeInfo info = (OppoFreezeInfo) freezeInfoMap.valueAt(j);
                    if (info != null) {
                        pw.println("-- " + info.mPackageName + "  userId=" + info.mUserId + "  state=" + info.mState + ", flag=" + info.mFreezeFlag);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0136 A:{SYNTHETIC, Splitter: B:78:0x0136} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0128 A:{SYNTHETIC, Splitter: B:72:0x0128} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x010d A:{SYNTHETIC, Splitter: B:64:0x010d} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f4 A:{SYNTHETIC, Splitter: B:56:0x00f4} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00db A:{SYNTHETIC, Splitter: B:48:0x00db} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c2 A:{SYNTHETIC, Splitter: B:40:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a9 A:{SYNTHETIC, Splitter: B:32:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static List<String> loadGovEnterpriseAppList(String path) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Exception e6;
        Throwable th;
        ArrayList<String> emptyList = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            ArrayList<String> ret = new ArrayList();
            FileInputStream stream = null;
            boolean success = false;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                                String value = parser.getAttributeValue(null, "att");
                                if (value != null) {
                                    ret.add(value);
                                }
                            }
                        }
                    } while (type != 1);
                    success = true;
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e7) {
                            e7.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (NullPointerException e8) {
                    e2 = e8;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e2);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e72) {
                            e72.printStackTrace();
                        }
                    }
                    if (success) {
                    }
                } catch (NumberFormatException e9) {
                    e3 = e9;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e3);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e722) {
                            e722.printStackTrace();
                        }
                    }
                    if (success) {
                    }
                } catch (XmlPullParserException e10) {
                    e4 = e10;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e4);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e7222) {
                            e7222.printStackTrace();
                        }
                    }
                    if (success) {
                    }
                } catch (IOException e11) {
                    e7222 = e11;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e7222);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e72222) {
                            e72222.printStackTrace();
                        }
                    }
                    if (success) {
                    }
                } catch (IndexOutOfBoundsException e12) {
                    e5 = e12;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e722222) {
                            e722222.printStackTrace();
                        }
                    }
                    if (success) {
                    }
                } catch (Exception e13) {
                    e6 = e13;
                    stream = stream2;
                    try {
                        Slog.w(TAG, "failed parsing ", e6);
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e7222222) {
                                e7222222.printStackTrace();
                            }
                        }
                        if (success) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e72222222) {
                                e72222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (NullPointerException e14) {
                e2 = e14;
                Slog.w(TAG, "failed parsing ", e2);
                if (stream != null) {
                }
                if (success) {
                }
            } catch (NumberFormatException e15) {
                e3 = e15;
                Slog.w(TAG, "failed parsing ", e3);
                if (stream != null) {
                }
                if (success) {
                }
            } catch (XmlPullParserException e16) {
                e4 = e16;
                Slog.w(TAG, "failed parsing ", e4);
                if (stream != null) {
                }
                if (success) {
                }
            } catch (IOException e17) {
                e72222222 = e17;
                Slog.w(TAG, "failed parsing ", e72222222);
                if (stream != null) {
                }
                if (success) {
                }
            } catch (IndexOutOfBoundsException e18) {
                e5 = e18;
                Slog.w(TAG, "failed parsing ", e5);
                if (stream != null) {
                }
                if (success) {
                }
            } catch (Exception e19) {
                e6 = e19;
                Slog.w(TAG, "failed parsing ", e6);
                if (stream != null) {
                }
                if (success) {
                }
            }
            if (success) {
                Slog.i(TAG, "loadGovEnterpriseAppList sucess!");
                return ret;
            }
            Slog.e(TAG, path + " file failed parsing!");
            return emptyList;
        }
        Slog.w(TAG, path + " file don't exist!");
        return emptyList;
    }

    public static boolean needVerifyInstall(Context context, String installerPackageName) {
        if (isExpROM() || installerPackageName == null) {
            Slog.d(TAG, "needVerifyInstall installerPackageName," + installerPackageName);
            return false;
        }
        ArrayList<String> oppoAppNameList;
        if (sPmsWhiteList.indexOfKey(681) >= 0) {
            oppoAppNameList = (ArrayList) sPmsWhiteList.get(681);
            if (oppoAppNameList == null) {
                return false;
            }
        }
        oppoAppNameList = DEFAULT_SHOP_TO_VERFITY_LIST;
        try {
            if (oppoAppNameList.contains(installerPackageName) && Secure.getInt(context.getContentResolver(), "settings_install_authentication", 0) == 1) {
                boolean frequencyAlways = Secure.getInt(context.getContentResolver(), "settings_install_authentication_frequency", 0) == 0;
                long lastVerifyTime = Secure.getLong(context.getContentResolver(), "account_verify_time", -1);
                long interval = SystemClock.elapsedRealtime() - lastVerifyTime;
                boolean needVerify = true;
                if (!(frequencyAlways || lastVerifyTime == -1 || interval > 900000)) {
                    needVerify = false;
                }
                return needVerify;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to read Settings ", e);
        }
        return false;
    }

    public static boolean handleForShopInstaller(PackageManagerService pms, String callerPackageName, String packageName, String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (pms == null || packageName == null || apkPath == null) {
            Slog.d(TAG, "handleForShopInstaller pms or packageName or apkPath = null!");
            return false;
        } else if (!new File(apkPath).exists() && !apkPath.startsWith("/storage") && !apkPath.startsWith("/sdcard")) {
            return false;
        } else {
            Intent intent = new Intent("oppo.intent.action.OPPO_INSTALL_FROM_ADB");
            intent.addFlags(16777216);
            intent.putExtra("apkPath", apkPath);
            intent.putExtra("callerpkg", callerPackageName);
            intent.putExtra("installFlags", installFlags);
            pms.mContext.sendBroadcast(intent);
            OppoAdbInstallerEntry oaie = OppoAdbInstallerEntry.Builder(apkPath, observer, packageName);
            synchronized (pms.mOppoPackageInstallerList) {
                pms.mOppoPackageInstallerList.add(oaie);
            }
            return true;
        }
    }
}
