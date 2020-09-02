package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.OppoApplicationInfoEx;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.am.ColorCommonListManager;
import com.android.server.notification.OpenID;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.storage.ColorDeviceStorageMonitorService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.color.widget.ColorResolveInfoHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorPackageManagerHelper {
    private static final String ACTION_EVENTID_NEW_INSTALL_INFO = "PMS_installed_app";
    private static final String ACTION_EVENTID_PREVENT_UNINSTALL = "PMS_prevent_uninstall";
    private static final String ACTION_OPPO_TEMP_UNFREEZE = "oppo.intent.action.OPPO_TEMP_UNFREEZE";
    private static final String ACTION_OPPO_THIRD_APP_LAUNCH = "oppo.intent.action.OPPO_THIRD_APP_FIRST_LAUNCH";
    private static final String APP_CODE = "20120";
    private static final String BUILDIN_NOT_LAUNCHED_PATH = "/data/format_unclear/launcher/builtin_thirdpart_pkgs";
    private static final int CERT_RESULT_MAX_SIZE = 60;
    private static final int CERT_SHORT_MODE_SIZE = 10;
    public static final int CLOSE_RESTORE_SANDBOX_STATE = 2;
    public static final int CLOSE_SANDBOX_SWITCH = 0;
    private static final String COLUMN_GUARD_ID = "guard_id";
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final ArrayList<String> CTTL_PACKAGE_LIST = new ArrayList<>();
    private static final String DATA_APP_LOCATION = "/data/app/";
    public static final ArrayList<String> DEFAULT_APP_WHITE_LIST = new ArrayList<>(Arrays.asList("com.salesforce.chatter,android.intent.action.PICK,*/*,^.*$", "cn.cj.pe,android.intent.action.VIEW,image/vnd.dwg,^.*.dwg$", "com.android.chrome,android.intent.action.VIEW,*/*,^.*$", "^.*$,android.intent.action.VIEW,text/plain,^.*$", "^.*$,android.intent.action.VIEW,,^.*.hangouts.google.com.*$", "com.facebook.orca,android.intent.action.VIEW,,^.*.www.facebook.com.*$", "android.uid.bluetooth:1002,android.intent.action.VIEW,text/*,^.*$", "com.baidu.searchbox,android.intent.action.VIEW,,^.*$", "com.google.android.gm,android.intent.action.VIEW,text/html,^.*$", "^.*$,android.intent.action.VIEW,video/*,^.*$"));
    public static final int DEFAULT_APP_WHITE_LIST_INDEX = 677;
    private static final ArrayList<String> DEFAULT_FORBID_INSTALL_LIST = new ArrayList<>();
    private static final ArrayList<String> DEFAULT_NOT_MULTI_APP_INSTALL_LIST = new ArrayList<>(Arrays.asList("com.oppo.otaui", "com.oppo.ota", "com.coloros.sau"));
    public static final ArrayList<String> DEFAULT_OPPO_APP_NAME_LIST = new ArrayList<>(Arrays.asList("^com\\.oppo\\..*$", "^com\\.coloros\\..*$"));
    public static final List<String> DEFAULT_OPPO_APP_NAME_WHITE_LIST = Collections.unmodifiableList(Arrays.asList("com.oppo.im", "com.oppo.mo", "com.coloros.bbs", "com.coloros.bbs2", "com.coloros.flashnote", "com.oppo.speechassist", "com.oppo.osim", "com.oppo.rms", "com.oppo.swpcontrol", "com.oppo.oppomediacontrol", "com.oppo.PhenixTestServer", "com.coloros.favorite", "com.coloros.mcs.tool", "com.oppo.opnfive", "com.oppo.marketdemo", "com.coloros.screensavershelper", "com.coloros.yoli", "com.coloros.colorfilestand", "com.coloros.wallet", "com.oppo.book", "com.oppo.reader", "com.coloros.aruler", "com.oppo.ohome", OppoPhoneWindowManager.WALLET_PACKAGE_NAME, "com.heytap.yoli"));
    public static final ArrayList<String> DEFAULT_OPPO_APP_SIGNATURE_LIST = new ArrayList<>(Arrays.asList("84D27678ADF5BABDC2A65D89DD2A77F08ABE16A0B76183324CCAA9B3D648465A", "413B5DCD6ABD09D5766ABF75B6FB98B75B032A801A064214A59EE21330A1849A", "D698B6B802CC6597E421702FF7BF5452B9682DF69DA0C6018425D177C47C94ED", "3B92460AAE5A3919E5CA9A9AEEE4ABDE56E67631B806C3AEBA0E42E86847E5A0", "8A3308EF6FD59C363E15D96DCF449292094233B35BB9A9949AC2A27CBA0F7AA4", "9775BD62AD20C480A0A847F3CFD345138C679FF277AED2C795EEFF58C8D0328F", "DD2A5CDEBD86EACA8D5B83BBDD29D8E8932D6941F5016030E2BF756B1EECB9A2", "64AAFAF1D5BC9155A9E417A849E4F8EDA1D0D1341667C28ED7C443C76F820B9A"));
    private static final ArrayList<String> DEFAULT_OPPO_HIDE_APP_LIST = new ArrayList<>();
    public static final ArrayList<String> DEFAULT_OPPO_HIDE_DESKTOP_ICON_LIST = new ArrayList<>(Arrays.asList("com.google.android.gms", "com.google.android.partnersetup", "com.google.android.gsf", "com.google.android.syncadapters.calendar", "com.google.android.syncadapters.contacts", "com.google.android.marvin.talkback"));
    private static final HashMap<String, ArraySet<String>> DEFAULT_PERM_KEY_BLACKLIST = new HashMap<>();
    public static final List<String> DEFAULT_PROTECT_HIDE_APP = Collections.unmodifiableList(Arrays.asList("com.oppo.launcher", "com.coloros.safecenter", "com.coloros.sau", "com.coloros.sauhelper", "com.oppo.ota", "com.nearme.romupdate", "com.oppo.market", "com.nearme.gamecenter", "com.nearme.themespace", "com.nearme.themestore", "com.heytap.gamecenter", "com.heytap.themestore", "com.heytap.market"));
    public static final ArrayList<String> DEFAULT_SAFECENTER_WHITE_NAME_LIST = new ArrayList<>(Arrays.asList("jp.co.daj.consumer.ifilter", "jp.co.daj.consumer.ifilter.shop", "jp.co.daj.consumer.ifilter.mb"));
    public static final ArrayList<String> DEFAULT_SHOP_PACKAGE_NAME_LIST = new ArrayList<>(Arrays.asList("com.nearme.gamecenter", "com.android.vending", "com.oppo.market", "com.coloros.backuprestore", "com.heytap.gamecenter", "com.heytap.market"));
    public static final ArrayList<String> DEFAULT_SHOP_TO_VERFITY_LIST = new ArrayList<>(Arrays.asList("com.android.vending", "com.oppo.market", "com.nearme.gamecenter", "com.heytap.market", "com.heytap.gamecenter"));
    private static final ArrayList<String> DEFAULT_SKIP_INSTALL_UPLOAD_LIST = new ArrayList<>(Arrays.asList("com.nearme.gamecenter", "com.android.vending", "com.oppo.market", "com.coloros.backuprestore", "com.heytap.gamecenter", "com.heytap.market"));
    private static final List<String> DEFAULT_SYSTEM_APP_IN_DATA = Collections.unmodifiableList(Arrays.asList("com.coloros.video", "com.coloros.gallery3d", "com.oppo.music", "com.oppo.book", "com.nearme.gamecenter", "com.oppo.community", "com.coloros.weather", "com.coloros.weather2", "com.android.calculator2", "com.coloros.calculator", "com.coloros.compass", "com.coloros.compass2", "com.nearme.note", "com.coloros.note", "com.oppo.reader", "com.heytap.gamecenter"));
    private static final ArrayList<String> EXP_SYSTEM_DEFAULT_PACKAGES = new ArrayList<>();
    private static final ArrayList<String> EXP_SYSTEM_FORCE_PACKAGES = new ArrayList<>();
    private static final String FILE_NOT_LAUNCHED_LIST = "notLaunchedPkgs.xml";
    private static final ArrayList<String> FORBID_UNINSTALL_DATA_APPS = new ArrayList<>();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final List<String> NON_STOP_STATE_PKG_LIST = Arrays.asList(new String[0]);
    private static final String ODEX_ARM64_FILE = "/arm64/base.odex/";
    private static final String ODEX_ARM_FILE = "/arm/base.odex/";
    private static final int ODEX_DATA_MIN_SIZE = 200;
    public static final int OPEN_SANDBOX_SWITCH = 1;
    private static final String OPPO_DEFAULT_PACKAGE_XML = "sys_pms_defaultpackage_list";
    private static final String OPPO_DEFAULT_PKG_CONFIG = "/data/oppo/coloros/config/sys_pms_defaultpackage_list.xml";
    private static final String OPPO_DEFAULT_PKG_PATH = "/data/oppo/coloros/config";
    public static final String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
    public static final String OPPO_FORBID_INSTALL_ACTION = "oppo.android.intent.action.FORBID_INSTALL";
    public static final int OPPO_FORBID_INSTALL_LIST_INDEX = 683;
    private static final String OPPO_FORBID_UNINSTALL_DATA_PKG_FILE = "/data/oppo/coloros/config/sys_pms_forbiduninstall_data_pkg_list.xml";
    public static final int OPPO_GET_INSTALLPACKAGE_HIDE_APP_LIST_INDEX = 687;
    public static final int OPPO_HIDE_DESKTOP_ICON_LIST_INDEX = 685;
    public static final int OPPO_PACKAGE_NAME_LIST_INDEX = 678;
    public static final int OPPO_PACKAGE_SIGNATURE_LIST_INDEX = 687;
    public static final int OPPO_PACKAGE_WHITE_LIST_INDEX = 680;
    public static final int OPPO_SAFECENTER_WHITE_LIST_INDEX = 686;
    public static final int OPPO_SHOP_LIST_INDEX = 682;
    public static final int OPPO_SHOP_VERIFY_LIST_INDEX = 681;
    private static final String OPPO_SYSTEM_APP_PATH = "/system/etc/security/pl.fs";
    private static final String OPPO_SYSTEM_APP_PWD = "a";
    public static final int OPPO_UPLOAD_INSTALL_WHITE_LIST_INDEX = 684;
    private static final String OPPO_VENDOR_APP_PATH = "/vendor/etc/security/pl.fs";
    private static final String PACKAGE_NEARME_STATISTICS = "com.nearme.statistics.rom";
    private static final String PATH_NOT_LAUNCHED_LIST = "/data/oppo/common/";
    private static final String PKG_OPPO_LAUNCHER = "com.oppo.launcher";
    public static final int PROTECT_HIDE_APP_INDEX = 676;
    private static final String SAFE_CENTER_AUTHORITY = "com.color.provider.SafeProvider";
    private static final Uri SAFE_CENTER_AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    private static final ArrayList<String> SECURITY_EVENT_FORBID_UNINSTALL_PKGS = new ArrayList<String>() {
        /* class com.android.server.pm.ColorPackageManagerHelper.AnonymousClass1 */

        {
            add("com.coloros.securityguard");
            add("com.coloros.digitalwellbeing");
        }
    };
    private static final ArrayList<String> SEPCIAL_DEFAULT_SECURE_APP = new ArrayList<>();
    private static final ArrayList<String> SKIP_APP_PACKAGE_LIST = new ArrayList<>();
    private static final ArrayList<String> SKIP_SAFECENTER_PACKAGE_LIST = new ArrayList<>();
    private static final ArrayList<String> SYSTEM_DEFAULT_PACKAGES = new ArrayList<>();
    private static final ArrayList<String> SYSTEM_FORCE_PACKAGES = new ArrayList<>();
    private static final String TABLE_APP_GUARD_INFO = "guard_info";
    public static final String TAG = "ColorPackageManager";
    private static final String TAG_FORBID_UNINSTALL_APP_VER = "ForbidUninstallAppVer";
    private static final String TAG_FORBID_UNINSTALL_APP_VER_SWITCH = "ForbidUninstallSwitch";
    private static final String TAG_MULTI_USER_INSTALL = "MultiUserInstall";
    private static final String TAG_MULTI_USER_NOT_INSTALL = "MultiUserNotInstall";
    private static final String TAG_OPPO_CTS_APP = "OppoCtsApp";
    private static final String TAG_OPPO_CTS_PREFIX = "OppoCtsPreFix";
    private static final String TAG_OPPO_DEFAULT_APP = "OppoDefaultApp";
    private static final String TAG_OPPO_FORCE_APP = "OppoForceApp";
    private static final String TAG_OPPO_HIDE_APP = "OppoHideApp";
    private static final String TAG_OPPO_ICON_CACHE_MAX_NUM = "OppoIconCacheMaxNum";
    private static final String TAG_OPPO_ICON_CACHE_MIN_MEM = "OppoIconCacheMinMem";
    private static final String TAG_OPPO_SANDBOX_SWITCH = "OppoSandboxSwitch";
    private static final String TAG_OPPO_SECUREPAY_SWITCH = "OppoSecurepaySwitch";
    private static final String TAG_PERM_KEY_BLACKLIST = "BlackPermKey";
    private static final String TAG_SPECIAL_SECURE_APP = "OppoSepcialSecureApp";
    private static final String TAG_SYSTEM_DATA_APP = "OppoSystemDataApp";
    private static final Uri URI_APP_GUARD_INFO = Uri.withAppendedPath(SAFE_CENTER_AUTHORITY_URI, TABLE_APP_GUARD_INFO);
    private static boolean mAllowSetLauncher = false;
    private static final ArrayList<String> mOppoApkList = new ArrayList<>();
    private static List<String> oppoSigList = new ArrayList();
    private static FileObserverPolicy sConfigFileObserver = null;
    private static ArrayList<String> sFailedParsedCtsPkgList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sForbidUninstallDataAppList = new ArrayList<>();
    private static boolean sForbidUninstallDataAppSwitch = true;
    private static FileObserverForbidUninstallFile sForbidUninstallLisObserver = null;
    private static boolean sHasDefaultLauncherFeature = false;
    private static int sIconCacheMaxNum = 0;
    private static float sIconCacheMinMemory = 0.0f;
    private static ArrayList<String> sLocalCtsPkgList = new ArrayList<>();
    private static ArrayList<String> sLocalCtsSigList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sMultiUserInstallList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sMultiUserNotInstallList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sNotLaunchedPkgs = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sOppoCtsPkgList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sOppoCtsPrefixList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sOppoDefaultPkgList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sOppoForcePkgList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sOppoHidePkgList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static HashMap<String, ArraySet<String>> sPermKeyBlackList = new HashMap<>();
    /* access modifiers changed from: private */
    public static SparseArray<ArrayList<String>> sPmsWhiteList = new SparseArray<>();
    private static String sSecurePaySwitch = "true";
    /* access modifiers changed from: private */
    public static ArrayList<String> sSpecialSecureAppList = new ArrayList<>();
    /* access modifiers changed from: private */
    public static ArrayList<String> sSystemDataPkgList = new ArrayList<>();
    private static float sTotalMemorySize = -1.0f;

    static {
        DEFAULT_PERM_KEY_BLACKLIST.put("728E6B5E6D3FAA00E2DE12CC464D027BFFE2DD87329967F72028F2FD13C122E9", null);
        SEPCIAL_DEFAULT_SECURE_APP.add("com.eg.android.AlipayGphone");
        SEPCIAL_DEFAULT_SECURE_APP.add(ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
        SYSTEM_DEFAULT_PACKAGES.add("com.android.browser");
        SYSTEM_DEFAULT_PACKAGES.add("com.coloros.browser");
        SYSTEM_DEFAULT_PACKAGES.add("com.nearme.browser");
        SYSTEM_DEFAULT_PACKAGES.add("com.heytap.browser");
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
        SYSTEM_DEFAULT_PACKAGES.add("com.heytap.market");
        EXP_SYSTEM_FORCE_PACKAGES.add("com.oppo.wirelesssettings");
        EXP_SYSTEM_FORCE_PACKAGES.add("com.coloros.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.coloros.wirelesssettings");
        SYSTEM_FORCE_PACKAGES.add("com.android.packageinstaller");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.launcher");
        SYSTEM_FORCE_PACKAGES.add("com.oppo.market");
        SYSTEM_FORCE_PACKAGES.add("com.heytap.market");
        SKIP_APP_PACKAGE_LIST.add("com.coloros.regservice");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.tencent.tvoem");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.book");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.nearme.note");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.note");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.community");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.news");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.android.email");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.findphone.client");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.findphone.client2");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.oppo.reader");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.weather");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.weather2");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.nearme.gamecenter");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.compass");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.compass2");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.android.calculator2");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.calculator");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.coloros.sauhelper");
        DEFAULT_OPPO_HIDE_APP_LIST.add("com.heytap.gamecenter");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safecenter");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safesdkproxy");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.safe.service.framework");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.roaming");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.virtual.softsim");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.coloros.phonemanager");
        SKIP_SAFECENTER_PACKAGE_LIST.add("com.redteamobile.oppo.roaming");
        FORBID_UNINSTALL_DATA_APPS.add("com.coloros.personalassistant 8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        if (isExpROM()) {
            FORBID_UNINSTALL_DATA_APPS.add("com.simeji.android.oppo 4E5A78C0450316E38AFAB3ECB6BAC9932C095F2B34360C40129857B78657E24F");
        }
        oppoSigList.add("8BEC659C16F7A438F85FA57E9D835393AFE6AB2B45311522ACA74D1D4202FBAF");
        oppoSigList.add("D698B6B802CC6597E421702FF7BF5452B9682DF69DA0C6018425D177C47C94ED");
    }

    public static void initDefaultPackageList(String defaultBrowser) {
        if (defaultBrowser != null) {
            Slog.d(TAG, "do not add browser's package in android Q : " + defaultBrowser);
        }
    }

    public static boolean isForbidUninstallByBindSecurityEvent(Context context, PackageParser.Package pkg) {
        if (pkg == null || pkg.applicationInfo == null) {
            return false;
        }
        boolean signWithOppoKey = false;
        OppoApplicationInfoEx oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(pkg.applicationInfo);
        if (oppoAppInfoEx != null) {
            signWithOppoKey = oppoAppInfoEx.isSignedWithOppoKey();
        }
        if (!SECURITY_EVENT_FORBID_UNINSTALL_PKGS.contains(pkg.packageName)) {
            return false;
        }
        if ((pkg.applicationInfo.isSignedWithPlatformKey() || signWithOppoKey) && isBindSecurityEvent(context)) {
            return true;
        }
        return false;
    }

    private static boolean isBindSecurityEvent(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_APP_GUARD_INFO, new String[]{COLUMN_GUARD_ID}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor == null) {
                    return false;
                }
                cursor.close();
                return false;
            }
            cursor.close();
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "getGuarderList() e: " + e.getMessage());
            if (cursor == null) {
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static boolean isForbidUninstallDataApp(PackageParser.Package pkg) {
        if (pkg == null || pkg.packageName == null) {
            return false;
        }
        String targetPackage = pkg.packageName + " ";
        String targetCert = null;
        Iterator<String> it = FORBID_UNINSTALL_DATA_APPS.iterator();
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
            return false;
        }
        Slog.d(TAG, "isForbidUninstallDataApp packageName=" + pkg.packageName);
        return isSha256CertMatchPackage(pkg, targetCert);
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
        return (isExpROM() ? EXP_SYSTEM_DEFAULT_PACKAGES : SYSTEM_DEFAULT_PACKAGES).contains(string);
    }

    private static boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
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
        if (sSpecialSecureAppList.size() <= 0 || !sSpecialSecureAppList.contains(pkg)) {
            return false;
        }
        return true;
    }

    public static boolean isCtsApp(String pkg) {
        if (sLocalCtsPkgList.contains(pkg)) {
            return true;
        }
        if (sOppoCtsPkgList.size() > 0 && sOppoCtsPkgList.contains(pkg)) {
            return true;
        }
        if (sOppoCtsPrefixList.size() > 0) {
            Iterator<String> it = sOppoCtsPrefixList.iterator();
            while (it.hasNext()) {
                if (pkg.contains(it.next())) {
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
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (parser.getName().equals("CtsToolList")) {
                                parser.next();
                                sLocalCtsPkgList.add(parser.getText());
                            } else if (parser.getName().equals("PkgCertDig")) {
                                parser.next();
                                String sig = parser.getText();
                                if (!TextUtils.isEmpty(sig)) {
                                    sLocalCtsSigList.add(sig);
                                }
                            } else if (parser.getName().equals("FailParseCtsPkg")) {
                                parser.next();
                                String pkg = parser.getText();
                                if (!TextUtils.isEmpty(pkg)) {
                                    sFailedParsedCtsPkgList.add(pkg);
                                }
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got execption parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got execption parsing permissions.", e2);
            }
            try {
                permReader.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        } catch (FileNotFoundException e4) {
            Slog.w(TAG, "Couldn't find or open oppo_cts_list file " + permFile);
        }
    }

    public static boolean dataAppContainCtsPkg() {
        String[] pkgList;
        File dataAppDir = new File(DATA_APP_LOCATION);
        if (!dataAppDir.exists() || (pkgList = dataAppDir.list()) == null || pkgList.length <= 0) {
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
            Iterator it = mService.mPackages.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                PackageParser.Package pkg = (PackageParser.Package) it.next();
                if (pkg != null) {
                    if (!pkg.isSystem()) {
                        if (isCtsPkgBySig(pkg)) {
                            Slog.d(TAG, "dataAppContainCtsPkgBySig found cts : " + pkg.packageName);
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean isCtsPkgBySig(PackageParser.Package pkg) {
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
            PackageParser.Package pkg = new PackageParser().parsePackage(file, 128);
            PackageParser.collectCertificates(pkg, false);
            if (pkg != null) {
                String certString = computePackageCertDigest(pkg);
                if (!TextUtils.isEmpty(certString)) {
                    result = isCtsSig(buildPackageCertString(pkg.packageName, certString));
                }
            }
        } catch (PackageParser.PackageParserException e) {
            hasException = true;
            Slog.w(TAG, "PackageParserException while isCtsAppFileBySig for " + path);
        } catch (Exception e2) {
            hasException = true;
            Slog.w(TAG, "isCtsAppFileBySig for " + path + " " + e2);
        }
        if (!hasException || !sFailedParsedCtsPkgList.contains(packageName)) {
            return result;
        }
        Slog.d(TAG, packageName + " parse failed but in FailedParsedCtsPkgList, silent install still");
        return true;
    }

    public static boolean isCtsAppFileByPkgName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Iterator<String> it = sLocalCtsSigList.iterator();
        while (it.hasNext()) {
            String pkg = getPkgFromCertString(it.next());
            if (pkg != null && pkg.equals(packageName)) {
                return true;
            }
        }
        if (sFailedParsedCtsPkgList.contains(packageName)) {
            return true;
        }
        return false;
    }

    private static String getPkgFromCertString(String str) {
        String[] splits;
        if (!TextUtils.isEmpty(str) && str.contains(" ") && (splits = str.split(" ")) != null && splits.length >= 1) {
            return splits[0];
        }
        return null;
    }

    private static boolean isSha256CertMatchPackage(PackageParser.Package pkg, String targetCertString) {
        byte[] bytes;
        if (pkg == null || pkg.mSigningDetails == null || (bytes = FileUtil.getInstance().hex2bytes(targetCertString)) == null) {
            return false;
        }
        return pkg.mSigningDetails.hasSha256Certificate(bytes);
    }

    public static String computePackageCertDigest(PackageParser.Package pkg) {
        if (pkg == null || pkg.mSigningDetails.signatures == null || pkg.mSigningDetails.signatures.length == 0 || pkg.mSigningDetails.signatures[0] == null) {
            return null;
        }
        return computeSignatureCertDigest(pkg.mSigningDetails.signatures[0], false);
    }

    public static String computeSignatureCertDigest(Signature signature, boolean shortMode) {
        if (signature == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(OpenID.SHA256);
            if (messageDigest == null) {
                return null;
            }
            messageDigest.update(signature.toByteArray());
            byte[] digest = messageDigest.digest();
            if (digest == null || digest.length == 0) {
                return null;
            }
            int digestLength = digest.length;
            char[] chars = new char[(digestLength * 2)];
            for (int i = 0; i < digestLength; i++) {
                int byteHex = digest[i] & 255;
                char[] cArr = HEX_ARRAY;
                chars[i * 2] = cArr[byteHex >>> 4];
                chars[(i * 2) + 1] = cArr[byteHex & 15];
            }
            if (!shortMode || chars.length <= 10) {
                return new String(chars);
            }
            return new String(chars).substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String buildPackageCertString(String pkg, String cert) {
        return pkg + " " + cert;
    }

    public static boolean isOppoForceApp(String string) {
        if (sOppoForcePkgList.size() < 1) {
            return (isExpROM() ? EXP_SYSTEM_FORCE_PACKAGES : SYSTEM_FORCE_PACKAGES).contains(string);
        }
        if (mAllowSetLauncher && sOppoForcePkgList.contains("com.oppo.launcher")) {
            sOppoForcePkgList.remove("com.oppo.launcher");
        }
        return sOppoForcePkgList.contains(string);
    }

    public static boolean moveOdexToOatDir(Context context) {
        int i;
        String[] pkgList;
        File dataAppDir;
        boolean isArm;
        boolean isArm2;
        File dataAppDir2 = new File(DATA_APP_LOCATION);
        int i2 = 0;
        if (!dataAppDir2.exists()) {
            return false;
        }
        String[] pkgList2 = dataAppDir2.list();
        if (pkgList2 == null) {
            return false;
        }
        if (pkgList2.length <= 0) {
            return false;
        }
        int length = pkgList2.length;
        int i3 = 0;
        while (i3 < length) {
            String pkg = pkgList2[i3];
            String appPkg = pkg.substring(i2, pkg.length() - 2);
            if (isOppoApkList(appPkg)) {
                Slog.d(TAG, "skip oppo pkg = " + appPkg);
                dataAppDir = dataAppDir2;
                pkgList = pkgList2;
                i = length;
            } else {
                File armOdexFile = new File(DATA_APP_LOCATION + pkg + ODEX_ARM_FILE);
                File arm64OdexFile = new File(DATA_APP_LOCATION + pkg + ODEX_ARM64_FILE);
                if (armOdexFile.exists() || arm64OdexFile.exists()) {
                    if (!armOdexFile.exists() || arm64OdexFile.exists()) {
                        dataAppDir = dataAppDir2;
                        if (armOdexFile.exists() || !arm64OdexFile.exists()) {
                            pkgList = pkgList2;
                            i = length;
                            Slog.d(TAG, "Pkg = " + appPkg + " exist arm and arm64 odex!");
                        } else {
                            Slog.d(TAG, "Pkg = " + appPkg + " exist arm64 odex!");
                            isArm = false;
                        }
                    } else {
                        dataAppDir = dataAppDir2;
                        Slog.d(TAG, "Pkg = " + appPkg + " exist arm odex!");
                        isArm = true;
                    }
                    File armOatFile = new File(DATA_APP_LOCATION + pkg + "/oat" + ODEX_ARM_FILE);
                    File arm64OatFile = new File(DATA_APP_LOCATION + pkg + "/oat" + ODEX_ARM64_FILE);
                    pkgList = pkgList2;
                    if (armOatFile.exists()) {
                        Slog.d(TAG, "delete oat " + armOatFile);
                        armOatFile.delete();
                    }
                    if (arm64OatFile.exists()) {
                        Slog.d(TAG, "delete oat " + arm64OatFile);
                        arm64OatFile.delete();
                    }
                    if (getAvaiDataSize() < ODEX_DATA_MIN_SIZE) {
                        return false;
                    }
                    if (isArm) {
                        isArm2 = isArm;
                        StringBuilder sb = new StringBuilder();
                        sb.append(DATA_APP_LOCATION);
                        sb.append(pkg);
                        i = length;
                        sb.append("/oat/arm");
                        File oatFile = new File(sb.toString());
                        if (!oatFile.exists() && !oatFile.isDirectory()) {
                            Slog.d(TAG, "mkdir odex file : " + oatFile);
                            if (oatFile.mkdir()) {
                                oatFile.setLastModified(new File(DATA_APP_LOCATION + pkg + "/base.apk").lastModified());
                            }
                        }
                        if (oatFile.exists()) {
                            moveOdex(armOdexFile, armOatFile);
                        }
                    } else {
                        isArm2 = isArm;
                        i = length;
                        File oatFile2 = new File(DATA_APP_LOCATION + pkg + "/oat/arm64");
                        if (!oatFile2.exists() && !oatFile2.isDirectory()) {
                            Slog.d(TAG, "mkdir odex file : " + oatFile2);
                            if (oatFile2.mkdir()) {
                                oatFile2.setLastModified(new File(DATA_APP_LOCATION + pkg + "/base.apk").lastModified());
                            }
                        }
                        if (oatFile2.exists()) {
                            moveOdex(arm64OdexFile, arm64OatFile);
                        }
                    }
                } else {
                    Slog.d(TAG, "Pkg = " + appPkg + " not exist new odex!");
                    dataAppDir = dataAppDir2;
                    pkgList = pkgList2;
                    i = length;
                }
            }
            i3++;
            dataAppDir2 = dataAppDir;
            pkgList2 = pkgList;
            length = i;
            i2 = 0;
        }
        return true;
    }

    private static void moveOdex(File fromFile, File toFile) {
        if (!fromFile.renameTo(toFile)) {
            Slog.e(TAG, "Unable to rename odex file : " + fromFile);
            return;
        }
        Slog.d(TAG, "delete file : " + fromFile);
        fromFile.delete();
    }

    public static int readEncryptFiles() {
        int result1 = readEncryptFile(OPPO_SYSTEM_APP_PATH);
        int result2 = readEncryptFile(OPPO_VENDOR_APP_PATH);
        if (result1 == -1 || result2 == -1) {
            return -1;
        }
        return 0;
    }

    private static int readEncryptFile(String filePath) {
        String[] line;
        FileInputStream fis = null;
        try {
            Slog.d(TAG, "readEncryptFile!!!");
            File path = new File(filePath);
            if (!path.exists()) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return -1;
            }
            int len = (int) path.length();
            byte[] buf = new byte[len];
            byte[] b = OPPO_SYSTEM_APP_PWD.getBytes("UTF-8");
            FileInputStream fis2 = new FileInputStream(path);
            fis2.read(buf);
            for (int i = 0; i < len; i++) {
                buf[i] = (byte) (b[0] ^ buf[i]);
                buf[i] = (byte) (~buf[i]);
            }
            for (String str : new String(buf, 0, len).split("\n")) {
                mOppoApkList.add(str);
            }
            try {
                fis2.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return 0;
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            return -1;
        } catch (IOException e5) {
            e5.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            return -1;
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Multiple debug info for r4v2 java.util.zip.ZipInputStream: [D('zin' java.util.zip.ZipInputStream), D('files' java.io.File[])] */
    public static void oppoCheckSuApp(String codePath, Context ct) throws PackageManagerException {
        File codeFile;
        StringBuilder sb;
        if (!SystemProperties.getBoolean("debug.restrict.install", true)) {
            Slog.d(TAG, "debug allow app install");
            return;
        }
        File appFile = new File(codePath);
        if (!appFile.exists()) {
            Slog.e(TAG, "appFile is not exist!!");
            return;
        }
        if (PackageParser.isApkFile(appFile)) {
            codeFile = appFile;
        } else {
            File[] files = appFile.listFiles();
            File codeFile2 = null;
            for (File file : files) {
                if (PackageParser.isApkFile(file)) {
                    Slog.d(TAG, "apk file == " + file);
                    codeFile2 = file;
                }
            }
            codeFile = codeFile2;
        }
        ZipInputStream zin = null;
        if (codeFile == null) {
            try {
                Slog.e(TAG, "codeFile is null!!");
                if (zin != null) {
                    try {
                        Slog.d(TAG, "check finish!!");
                        zin.close();
                        return;
                    } catch (IOException e) {
                        Slog.e(TAG, "oppoCheckSuApp fatal error:" + e);
                        return;
                    }
                } else {
                    return;
                }
            } catch (IOException e2) {
                Slog.e(TAG, "oppoCheckSuApp fatal error:" + e2);
                if (zin != null) {
                    try {
                        Slog.d(TAG, "check finish!!");
                        zin.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (zin != null) {
                    try {
                        Slog.d(TAG, "check finish!!");
                        zin.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "oppoCheckSuApp fatal error:" + e4);
                    }
                }
                throw th;
            }
        } else {
            Slog.d(TAG, "code path == " + codeFile);
            ZipInputStream zin2 = new ZipInputStream(new FileInputStream(codeFile));
            while (true) {
                ZipEntry entry = zin2.getNextEntry();
                if (entry != null) {
                    String[] splitName = entry.getName().split("/");
                    int i = 0;
                    while (i < splitName.length) {
                        if (!isRootFile(splitName[i])) {
                            i++;
                        } else {
                            showMessageToUSer(ct);
                            Slog.d(TAG, "This dangerous app!!");
                            throw new PackageManagerException(-2, "This dangerous app, forbid it");
                        }
                    }
                    zin2.closeEntry();
                } else {
                    try {
                        Slog.d(TAG, "check finish!!");
                        zin2.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                }
            }
        }
        sb.append("oppoCheckSuApp fatal error:");
        sb.append(e);
        Slog.e(TAG, sb.toString());
    }

    private static void showMessageToUSer(Context context) {
        context.sendBroadcast(new Intent(OPPO_FORBID_INSTALL_ACTION));
    }

    private static boolean isRootFile(String file) {
        return file.equalsIgnoreCase("su") || file.equalsIgnoreCase("root") || file.equalsIgnoreCase("superuser.apk") || file.equalsIgnoreCase("kinguser.apk") || file.equalsIgnoreCase("libsu.so") || file.equalsIgnoreCase("libroot.so");
    }

    private static String getDataFromProvider(Context mContext, String filterName) {
        Cursor cursor = null;
        String xmlValue = null;
        String[] projection = {COLUMN_NAME_XML};
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            Uri uri = CONTENT_URI;
            Cursor cursor2 = contentResolver.query(uri, projection, "filtername=\"" + filterName + "\"", null, null);
            if (cursor2 == null || cursor2.getCount() <= 0) {
                Slog.w(TAG, "The Filtrate app cursor is null !!!");
            } else {
                int xmlColumnIndex = cursor2.getColumnIndex(COLUMN_NAME_XML);
                cursor2.moveToNext();
                xmlValue = cursor2.getString(xmlColumnIndex);
            }
            if (cursor2 != null) {
                cursor2.close();
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

    public static void readPermKeyBlackList() {
        File xmlFile = new File(OPPO_DEFAULT_PKG_CONFIG);
        if (!xmlFile.exists()) {
            Slog.w(TAG, "Couldn't find sys_pms_defaultpackage_list file");
            return;
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0 && eventType == 2 && parser.getName().equals(TAG_PERM_KEY_BLACKLIST)) {
                        addBlackPermKey(parser.nextText());
                    }
                }
                try {
                    xmlReader2.close();
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e) {
                    Slog.w(TAG, "Got execption close permReader.", e);
                }
            } catch (FileNotFoundException e2) {
                Slog.w(TAG, "Couldn't find or open sys_pms_defaultpackage_list file for perm-key" + xmlFile);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.w(TAG, "Got execption close permReader.", e3);
                        return;
                    }
                }
                if (strReader != null) {
                    strReader.close();
                }
            }
        } catch (Exception e4) {
            Slog.w(TAG, "Got execption parsing permissions.", e4);
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e5) {
                    Slog.w(TAG, "Got execption close permReader.", e5);
                    throw th;
                }
            }
            if (strReader != null) {
                strReader.close();
            }
            throw th;
        }
    }

    private static void addBlackPermKey(String value) {
        if (value != null) {
            Slog.d(TAG, "TAG_PERM_KEY_BLACKLIST: " + value);
            String[] segments = value.split(" ");
            if (segments.length > 0) {
                String sigCert = segments[0];
                ArraySet<String> previous = sPermKeyBlackList.containsKey(sigCert) ? sPermKeyBlackList.get(sigCert) : new ArraySet<>();
                if (previous != null) {
                    if (segments.length > 1) {
                        previous.addAll(Arrays.asList(segments).subList(1, segments.length));
                        sPermKeyBlackList.put(sigCert, previous);
                        return;
                    }
                    sPermKeyBlackList.put(sigCert, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void readConfigFile() {
        File xmlFile = new File(OPPO_DEFAULT_PKG_CONFIG);
        if (xmlFile.exists()) {
            FileReader xmlReader = null;
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader2 = new FileReader(xmlFile);
                    parser.setInput(xmlReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0 && eventType == 2) {
                            if (parser.getName().equals(TAG_OPPO_DEFAULT_APP)) {
                                String value = parser.nextText();
                                if (value != null) {
                                    Slog.d(TAG, "TAG_OPPO_DEFAULT_APP : " + value);
                                    sOppoDefaultPkgList.add(value);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_FORCE_APP)) {
                                String value2 = parser.nextText();
                                if (value2 != null) {
                                    Slog.d(TAG, "TAG_OPPO_FORCE_APP : " + value2);
                                    sOppoForcePkgList.add(value2);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_CTS_APP)) {
                                String value3 = parser.nextText();
                                if (value3 != null) {
                                    Slog.d(TAG, "TAG_OPPO_CTS_APP : " + value3);
                                    sOppoCtsPkgList.add(value3);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_CTS_PREFIX)) {
                                String value4 = parser.nextText();
                                if (value4 != null) {
                                    Slog.d(TAG, "TAG_OPPO_CTS_PREFIX : " + value4);
                                    sOppoCtsPrefixList.add(value4);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_SANDBOX_SWITCH)) {
                                String value5 = parser.nextText();
                                if (value5 != null) {
                                    Slog.d(TAG, "TAG_OPPO_SANDBOX_SWITCH : " + value5);
                                    setSandboxSwitchState(value5);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_SECUREPAY_SWITCH)) {
                                String value6 = parser.nextText();
                                if (value6 != null) {
                                    Slog.d(TAG, "TAG_OPPO_SECUREPAY_SWITCH : " + value6);
                                    sSecurePaySwitch = value6;
                                }
                            } else if (parser.getName().equals(TAG_SPECIAL_SECURE_APP)) {
                                String value7 = parser.nextText();
                                if (value7 != null) {
                                    Slog.d(TAG, "TAG_SPECIAL_SECURE_APP : " + value7);
                                    sSpecialSecureAppList.add(value7);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_ICON_CACHE_MAX_NUM)) {
                                String value8 = parser.nextText().trim();
                                if (value8 != null) {
                                    Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MAX_NUM : " + value8);
                                    if (Integer.parseInt(value8) >= 0) {
                                        sIconCacheMaxNum = Integer.parseInt(value8);
                                    }
                                }
                            } else if (parser.getName().equals(TAG_OPPO_ICON_CACHE_MIN_MEM)) {
                                String value9 = parser.nextText().trim();
                                if (value9 != null) {
                                    Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MIN_MEM : " + value9);
                                    if (Float.parseFloat(value9) >= 0.0f) {
                                        sIconCacheMinMemory = Float.parseFloat(value9);
                                    }
                                }
                            } else if (parser.getName().equals(TAG_SYSTEM_DATA_APP)) {
                                String value10 = parser.nextText().trim();
                                if (value10 != null) {
                                    Slog.d(TAG, "TAG_SYSTEM_DATA_APP : " + value10);
                                    sSystemDataPkgList.add(value10);
                                }
                            } else if (parser.getName().equals(TAG_OPPO_HIDE_APP)) {
                                String value11 = parser.nextText().trim();
                                if (value11 != null) {
                                    Slog.d(TAG, "TAG_OPPO_HIDE_APP : " + value11);
                                    sOppoHidePkgList.add(value11);
                                }
                            } else if (parser.getName().equals(TAG_MULTI_USER_INSTALL)) {
                                String value12 = parser.nextText().trim();
                                if (value12 != null) {
                                    Slog.d(TAG, "TAG_MULTI_USER_INSTALL : " + value12);
                                    sMultiUserInstallList.add(value12);
                                }
                            } else if (parser.getName().equals(TAG_MULTI_USER_NOT_INSTALL)) {
                                String value13 = parser.nextText().trim();
                                if (value13 != null) {
                                    Slog.d(TAG, "TAG_MULTI_USER_NOT_INSTALL : " + value13);
                                    sMultiUserNotInstallList.add(value13);
                                }
                            } else if (parser.getName().equals(TAG_PERM_KEY_BLACKLIST)) {
                                addBlackPermKey(parser.nextText());
                            } else {
                                char[] typeChar = parser.getName().toCharArray();
                                if (typeChar.length <= 3) {
                                    int type = char2int(typeChar);
                                    parser.next();
                                    if (type >= 0) {
                                        ArrayList<String> tmp = sPmsWhiteList.get(type);
                                        if (tmp == null) {
                                            ArrayList<String> tmp2 = new ArrayList<>();
                                            tmp2.add(parser.getText());
                                            sPmsWhiteList.put(type, tmp2);
                                        } else {
                                            tmp.add(parser.getText());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    try {
                        xmlReader2.close();
                        if (strReader != null) {
                            strReader.close();
                        }
                    } catch (IOException e) {
                        Slog.w(TAG, "Got execption close permReader.", e);
                    }
                    if (sHasDefaultLauncherFeature) {
                        replaceLauncher();
                    }
                } catch (FileNotFoundException e2) {
                    Slog.w(TAG, "Couldn't find or open sys_pms_defaultpackage_list file " + xmlFile);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Slog.w(TAG, "Got execption close permReader.", e3);
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                }
            } catch (Exception e4) {
                Slog.w(TAG, "Got execption parsing permissions.", e4);
                if (xmlReader != null) {
                    xmlReader.close();
                }
                if (strReader != null) {
                    strReader.close();
                }
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e5) {
                        Slog.w(TAG, "Got execption close permReader.", e5);
                        throw th;
                    }
                }
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
        }
    }

    private static int char2int(char[] in) {
        int out = 0;
        if (in.length < 1) {
            return -1;
        }
        for (int n = 0; n < in.length; n++) {
            out = (int) (((double) out) + (((double) (in[n] - 'a')) * Math.pow(26.0d, (double) ((in.length - n) - 1))));
        }
        return out;
    }

    public static boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        if (sPmsWhiteList.indexOfKey(type) >= 0) {
            if (sPmsWhiteList.get(type).contains(verifyStr)) {
                return true;
            }
            return false;
        } else if (defaultList == null || !defaultList.contains(verifyStr)) {
            return false;
        } else {
            return true;
        }
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
                ColorPackageManagerHelper.sSystemDataPkgList.clear();
                ColorPackageManagerHelper.sOppoHidePkgList.clear();
                ColorPackageManagerHelper.sMultiUserInstallList.clear();
                ColorPackageManagerHelper.sMultiUserNotInstallList.clear();
                ColorPackageManagerHelper.sPmsWhiteList.clear();
                ColorPackageManagerHelper.sPermKeyBlackList.clear();
                ColorPackageManagerHelper.readConfigFile();
            }
        }
    }

    public static int getAvaiDataSize() {
        StatFs sf = new StatFs("/data");
        return (int) ((sf.getBlockSizeLong() * sf.getAvailableBlocksLong()) / ColorDeviceStorageMonitorService.MB_BYTES);
    }

    public static boolean isSetContainsOppoDefaultPkg(ComponentName[] set, ComponentName activity) {
        String pkg;
        if (set == null || activity == null || activity.getPackageName() == null) {
            return false;
        }
        if ((isExpROM() ? EXP_SYSTEM_DEFAULT_PACKAGES : SYSTEM_DEFAULT_PACKAGES).contains(activity.getPackageName())) {
            return false;
        }
        for (ComponentName comp : set) {
            if (!(comp == null || (pkg = comp.getPackageName()) == null)) {
                if (isExpROM()) {
                    if (EXP_SYSTEM_DEFAULT_PACKAGES.contains(pkg) && !"com.oppo.launcher".equals(pkg)) {
                    }
                } else if (SYSTEM_DEFAULT_PACKAGES.contains(pkg)) {
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isQueryListContainsOppoDefaultPkg(List<ResolveInfo> query) {
        String pkg;
        if (query == null) {
            return false;
        }
        int len = query.size();
        for (int i = 0; i < len; i++) {
            ResolveInfo ri = query.get(i);
            if (!(ri == null || ri.activityInfo == null || (pkg = ri.activityInfo.packageName) == null)) {
                if (isExpROM()) {
                    if (EXP_SYSTEM_DEFAULT_PACKAGES.contains(pkg)) {
                    }
                } else if (SYSTEM_DEFAULT_PACKAGES.contains(pkg)) {
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isCttlApp(String pkg) {
        if (pkg != null && CTTL_PACKAGE_LIST.contains(pkg)) {
            return true;
        }
        return false;
    }

    public static void sendDcsSilentInstallBroadcast(String packageName, Bundle extras, String installerPackageName, int userId) {
    }

    public static int getIconCacheMaxNum() {
        if (sIconCacheMaxNum == 0) {
            return 0;
        }
        if (getTotalMemorySize() <= sIconCacheMinMemory) {
            sIconCacheMaxNum = 0;
        }
        Slog.d(TAG, "sIconCacheMaxNum:" + sIconCacheMaxNum + ",sIconCacheMinMemory:" + sIconCacheMinMemory);
        return sIconCacheMaxNum;
    }

    public static void sendDcsNonSilentInstallBroadcastExp(String packageName, boolean updateState, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            try {
                Intent intent = new Intent();
                intent.setAction("oppo.intent.action.oppo.dcs.caller.info");
                intent.setPackage(PACKAGE_NEARME_STATISTICS);
                intent.putExtra("oppo_extra_pid", -99);
                intent.putExtra("oppo_extra_uid", -99);
                intent.putExtra(OPPO_EXTRA_PKG_NAME, installerPackageName);
                intent.putExtra("android.intent.extra.REPLACING", updateState);
                intent.putExtra("oppo_extra_install_package", packageName);
                try {
                    am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, true, false, userId);
                } catch (RemoteException e) {
                    ex = e;
                }
            } catch (RemoteException e2) {
                ex = e2;
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
        List<String> enterpriseAppList = loadGovEnterpriseAppList("/system/etc/oppo_customize_whitelist.xml");
        if (enterpriseAppList != null && enterpriseAppList.size() > 0) {
            SKIP_APP_PACKAGE_LIST.addAll(enterpriseAppList);
        }
    }

    public static ArrayList<String> getIgnoreAppList() {
        return SKIP_APP_PACKAGE_LIST;
    }

    public static boolean isSafeCenterApp(String callerName, String packageName) {
        if (packageName == null || isSafeCenterWhitePackageName(callerName)) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(687) < 0) {
            return SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName);
        }
        ArrayList<String> hideAppList = sPmsWhiteList.get(687);
        if (SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName) || hideAppList.contains(packageName)) {
            return true;
        }
        return false;
    }

    public static boolean isForbiddenUninstallApp(IPackageManager pm, String packageName, int callingUid) {
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
            if (isSystemCaller || !sOppoHidePkgList.contains(packageName)) {
                return false;
            }
            return true;
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
        if (sTotalMemorySize < 0.0f) {
            MemInfoReader reader = new MemInfoReader();
            reader.readMemInfo();
            sTotalMemorySize = ((float) reader.getTotalSize()) / 1.07374182E9f;
        }
        return sTotalMemorySize;
    }

    public static void initNotLaunchedList() {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
                }
                Runtime.getRuntime().exec("chmod 774 /data/oppo/common//notLaunchedPkgs.xml");
            } catch (IOException e) {
            }
        } else {
            sNotLaunchedPkgs = readNotLaunchedListFromFile();
            Iterator<String> it = sNotLaunchedPkgs.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "init not launched ~~~~~ " + it.next());
            }
        }
    }

    public static void initNotLaunchedList(PackageManagerService service, boolean isFirstBoot) {
        List<String> buildinDataList;
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
                Iterator<String> it = sNotLaunchedPkgs.iterator();
                while (it.hasNext()) {
                    Slog.d(TAG, "init not launched " + it.next());
                }
            } else if (service != null && service.mPackages != null) {
                File buildinThirdpartFile = new File(BUILDIN_NOT_LAUNCHED_PATH);
                if (buildinThirdpartFile.exists() && (buildinDataList = readFromFileLocked(buildinThirdpartFile)) != null && !buildinDataList.isEmpty()) {
                    for (String name : buildinDataList) {
                        if (!TextUtils.isEmpty(name) && service.mPackages.get(name) != null) {
                            Slog.d(TAG, "first-boot find not-Launch installed app " + name);
                            sNotLaunchedPkgs.add(name);
                        }
                    }
                    ArrayList<String> arrayList = sNotLaunchedPkgs;
                    if (arrayList != null && !arrayList.isEmpty()) {
                        writeNotLaunchedListToFile(sNotLaunchedPkgs);
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
                    return new ArrayList<>();
                }
            } catch (IOException e) {
            }
        }
        return (ArrayList) readFromFileLocked(file);
    }

    /* access modifiers changed from: private */
    public static void writeNotLaunchedListToFile(ArrayList<String> list) {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "failed create file /data/oppo/common//notLaunchedPkgs.xml");
                    return;
                }
                Runtime.getRuntime().exec("chmod 774 /data/oppo/common//notLaunchedPkgs.xml");
            } catch (IOException e) {
            }
        }
        writeDataToFile(file, list);
    }

    private static List<String> readFromFileLocked(File file) {
        StringBuilder sb;
        int type;
        String pkg;
        FileInputStream stream = null;
        List<String> list = new ArrayList<>();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "att")) != null) {
                    list.add(pkg);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Slog.i(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return list;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
        return list;
    }

    private static void writeDataToFile(File file, List<String> list) {
        StringBuilder sb;
        if (file != null) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos2, "UTF-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "gs");
                for (int i = 0; i < list.size(); i++) {
                    String pkg = list.get(i);
                    if (pkg != null) {
                        serializer.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                        serializer.attribute(null, "att", pkg);
                        serializer.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                serializer.endTag(null, "gs");
                serializer.endDocument();
                serializer.flush();
                try {
                    fileos2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } catch (IllegalArgumentException e2) {
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IllegalStateException e4) {
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e6) {
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Exception e8) {
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e9) {
                        e = e9;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e10) {
                        Slog.i(TAG, "failed close stream " + e10);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
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

    public static void sendDcsPreventUninstallSystemApp(Context context, String callingPackage, String packageName) {
        if (context != null) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put("caller_pkg", callingPackage);
                map.put("app_pkg", packageName);
                OppoStatistics.onCommon(context, APP_CODE, ACTION_EVENTID_PREVENT_UNINSTALL, map, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean forbiddenSetPreferredActivity(PackageManagerService pms, IntentFilter filter) {
        if (!(pms == null || filter == null || !pms.hasSystemFeature("oppo.childspace.support", 0))) {
            String callingPkg = OppoPackageManagerHelper.getProcessNameByPid(Binder.getCallingPid());
            if (Settings.Global.getInt(pms.mContext.getContentResolver(), "children_mode_on", 0) == 1 && !"com.coloros.childrenspace".equals(callingPkg) && filter.hasCategory("android.intent.category.HOME")) {
                Slog.d(TAG, "forbidden set launcher in children mode, skip!");
                return true;
            }
        }
        return false;
    }

    public static void filterBlackList(Context context, Intent intent, List<ResolveInfo> query) {
        if (query != 0 && !query.isEmpty()) {
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
            if (!TextUtils.isEmpty(pkgName)) {
                Slog.i(TAG, "filterBlackList, skip for intent package=" + pkgName);
                return;
            }
            List<String> blackList = ColorResolveInfoHelper.getInstance(context).getCloudBlackList(intent);
            if (blackList != null && !blackList.isEmpty()) {
                List<ResolveInfo> black = null;
                for (ResolveInfo ri : query) {
                    if (blackList.contains(ri.activityInfo.packageName)) {
                        if (black == null) {
                            black = new ArrayList<>();
                        }
                        black.add(ri);
                    }
                }
                if (black != null) {
                    query.removeAll(black);
                }
            }
        }
    }

    public static boolean hasFileManagerOpenFlag(Intent intent, String callingPackage) {
        if (!"com.coloros.filemanager".equals(callingPackage)) {
            return false;
        }
        try {
            return intent.getBooleanExtra("oppo_filemanager_openflag", false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean inDefaultAppWhiteList(String callerName, Intent intent) {
        ArrayList<String> whiteList;
        if (intent == null) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(DEFAULT_APP_WHITE_LIST_INDEX) >= 0) {
            whiteList = sPmsWhiteList.get(DEFAULT_APP_WHITE_LIST_INDEX);
            if (whiteList == null) {
                return false;
            }
        } else {
            whiteList = DEFAULT_APP_WHITE_LIST;
        }
        Iterator<String> it = whiteList.iterator();
        while (it.hasNext()) {
            String[] splitRuleArray = it.next().split(",");
            if (splitRuleArray.length >= 4 && isStrEqual(splitRuleArray[0], callerName) && isStrEqual(splitRuleArray[1], intent.getAction()) && isStrEqual(splitRuleArray[2], intent.getType()) && isStrEqual(splitRuleArray[3], intent.getDataString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStrEqual(String pattern, String src) {
        if (TextUtils.isEmpty(pattern) || pattern.equals(src)) {
            return true;
        }
        if (TextUtils.isEmpty(src)) {
            src = " ";
        }
        try {
            return Pattern.compile(pattern).matcher(src).matches();
        } catch (Exception e) {
            return pattern.equals(src);
        }
    }

    public static boolean isDefaultAppEnabled(String callerName, boolean isCtsAppInstall, Intent intent) {
        if (inDefaultAppWhiteList(callerName, intent)) {
            Slog.d(TAG, "inDefaultAppWhiteList:" + callerName);
            return false;
        } else if (callerName == null || callerName.contains("android.uid.nfc") || callerName.equals("com.android.cts.stub") || callerName.equals("com.android.cts.normalapp") || callerName.equals("com.android.cts.ephemeralapp1") || callerName.equals("com.android.cts.ephemeralapp2") || callerName.equals("android.voiceinteraction.cts") || callerName.equals("com.android.cts.deviceandprofileowner")) {
            return false;
        } else {
            if (!callerName.contains("android.uid.system") || !isCtsAppInstall) {
                return true;
            }
            return false;
        }
    }

    private static List<String> loadGovEnterpriseAppList(String path) {
        int type;
        String value;
        ArrayList<String> emptyList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            Slog.w(TAG, path + " file don't exist!");
            return emptyList;
        }
        ArrayList<String> ret = new ArrayList<>();
        FileInputStream stream = null;
        boolean success = false;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (value = parser.getAttributeValue(null, "att")) != null) {
                    ret.add(value);
                }
            } while (type != 1);
            success = true;
            try {
                stream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e2) {
            Slog.w(TAG, "failed parsing ", e2);
            if (stream != null) {
                stream.close();
            }
        } catch (NumberFormatException e3) {
            Slog.w(TAG, "failed parsing ", e3);
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "failed parsing ", e4);
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e5) {
            Slog.w(TAG, "failed parsing ", e5);
            if (stream != null) {
                stream.close();
            }
        } catch (IndexOutOfBoundsException e6) {
            Slog.w(TAG, "failed parsing ", e6);
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e7) {
            Slog.w(TAG, "failed parsing ", e7);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
        if (success) {
            Slog.i(TAG, "loadGovEnterpriseAppList sucess!");
            return ret;
        }
        Slog.e(TAG, path + " file failed parsing!");
        return emptyList;
    }

    private static boolean inOppoApkWhiteList(String packageName) {
        if (sPmsWhiteList.indexOfKey(OPPO_PACKAGE_WHITE_LIST_INDEX) >= 0) {
            ArrayList<String> oppoAppWhiteList = sPmsWhiteList.get(OPPO_PACKAGE_WHITE_LIST_INDEX);
            if (oppoAppWhiteList == null) {
                return false;
            }
            return oppoAppWhiteList.contains(packageName);
        } else if (DEFAULT_SYSTEM_APP_IN_DATA.contains(packageName) || DEFAULT_OPPO_APP_NAME_WHITE_LIST.contains(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOppoApkListEmpty() {
        return mOppoApkList.isEmpty();
    }

    public static boolean isOppoApkSignature(PackageParser.Package pkg) {
        String signatureHash;
        ArrayList<String> oppoAppSignatureList;
        if (pkg == null || pkg.packageName == null || (signatureHash = computePackageCertDigest(pkg)) == null) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(687) >= 0) {
            oppoAppSignatureList = sPmsWhiteList.get(687);
            if (oppoAppSignatureList == null) {
                return false;
            }
        } else {
            oppoAppSignatureList = DEFAULT_OPPO_APP_SIGNATURE_LIST;
        }
        boolean result = oppoAppSignatureList.contains(signatureHash);
        if (!result) {
            Slog.d(TAG, "isOppoApkSignature:" + signatureHash + ",pkgName:" + pkg.packageName);
        }
        return result;
    }

    public static boolean isOppoPackageName(String packageName) {
        ArrayList<String> oppoAppNameList;
        if (TextUtils.isEmpty(packageName) || inOppoApkWhiteList(packageName)) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(OPPO_PACKAGE_NAME_LIST_INDEX) >= 0) {
            oppoAppNameList = sPmsWhiteList.get(OPPO_PACKAGE_NAME_LIST_INDEX);
            if (oppoAppNameList == null) {
                return false;
            }
        } else {
            oppoAppNameList = DEFAULT_OPPO_APP_NAME_LIST;
        }
        Iterator<String> it = oppoAppNameList.iterator();
        while (it.hasNext()) {
            if (isStrEqual(it.next(), packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isShopPackageName(String packageName) {
        ArrayList<String> oppoShopAppNameList;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(OPPO_SHOP_LIST_INDEX) >= 0) {
            oppoShopAppNameList = sPmsWhiteList.get(OPPO_SHOP_LIST_INDEX);
            if (oppoShopAppNameList == null) {
                return false;
            }
        } else {
            oppoShopAppNameList = DEFAULT_SHOP_PACKAGE_NAME_LIST;
        }
        return oppoShopAppNameList.contains(packageName);
    }

    public static void uploadForbiddenInstallDcs(Context context, PackageParser.Package pkg) {
        String signatureHash = computePackageCertDigest(pkg);
        if (signatureHash != null) {
            Map<String, String> eventMap = new HashMap<>();
            eventMap.put("pkgName", pkg.packageName);
            eventMap.put("hash", signatureHash);
            OppoStatistics.onCommon(context, APP_CODE, "forbidInstall_oppo_app", eventMap, false);
        }
    }

    public static boolean needVerifyInstall(Context context, String installerPackageName) {
        ArrayList<String> oppoAppNameList;
        if (isExpROM() || installerPackageName == null) {
            Slog.d(TAG, "needVerifyInstall installerPackageName," + installerPackageName);
            return false;
        }
        if (sPmsWhiteList.indexOfKey(OPPO_SHOP_VERIFY_LIST_INDEX) >= 0) {
            oppoAppNameList = sPmsWhiteList.get(OPPO_SHOP_VERIFY_LIST_INDEX);
            if (oppoAppNameList == null) {
                return false;
            }
        } else {
            oppoAppNameList = DEFAULT_SHOP_TO_VERFITY_LIST;
        }
        try {
            int userId = ActivityManager.getCurrentUser();
            if (oppoAppNameList.contains(installerPackageName)) {
                boolean frequencyAlways = true;
                if (Settings.Secure.getIntForUser(context.getContentResolver(), "settings_install_authentication", 0, userId) == 1) {
                    if (Settings.Secure.getIntForUser(context.getContentResolver(), "settings_install_authentication_frequency", 0, userId) != 0) {
                        frequencyAlways = false;
                    }
                    long lastVerifyTime = Settings.Secure.getLongForUser(context.getContentResolver(), "account_verify_time", -1, userId);
                    long interval = SystemClock.elapsedRealtime() - lastVerifyTime;
                    if (frequencyAlways || lastVerifyTime == -1 || interval > 900000) {
                        return true;
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to read Settings ", e);
        }
        return false;
    }

    public static boolean isForbidInstallAppByCert(PackageParser.Package pkg) {
        String cert;
        ArrayList<String> forbidInstallList;
        new ArrayList();
        String appMd5 = "";
        if (pkg == null || pkg.packageName == null || (cert = computePackageCertDigest(pkg)) == null) {
            return false;
        }
        String buildCert = buildPackageCertString(pkg.packageName, cert);
        if (sPmsWhiteList.indexOfKey(OPPO_FORBID_INSTALL_LIST_INDEX) >= 0) {
            forbidInstallList = sPmsWhiteList.get(OPPO_FORBID_INSTALL_LIST_INDEX);
            if (forbidInstallList == null) {
                return false;
            }
        } else {
            forbidInstallList = DEFAULT_FORBID_INSTALL_LIST;
        }
        if (forbidInstallList == null || forbidInstallList.size() == 0) {
            return false;
        }
        Iterator<String> it = forbidInstallList.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (!(str == null || str == "")) {
                if (str.equals(cert) || str.equals(buildCert)) {
                    return true;
                }
                String[] subStr = str.split(" ");
                String targetMd5 = "";
                if (subStr.length > 2) {
                    targetMd5 = subStr[2];
                }
                if (str.startsWith(buildCert) && appMd5 == "") {
                    appMd5 = FileUtil.getInstance().getFileMd5(new File(pkg.baseCodePath));
                    if (PackageManagerService.DEBUG_INSTALL) {
                        Slog.d(TAG, "isForbidInstallAppByCert appMd5:" + appMd5);
                    }
                }
                if (str.startsWith(buildCert) && targetMd5 != "" && targetMd5.equals(appMd5)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
        ArrayList<String> skipUploadInstallList;
        new ArrayList();
        if (context != null && pkg != null && pkg.packageName != null) {
            if (sPmsWhiteList.indexOfKey(OPPO_UPLOAD_INSTALL_WHITE_LIST_INDEX) >= 0) {
                skipUploadInstallList = sPmsWhiteList.get(OPPO_UPLOAD_INSTALL_WHITE_LIST_INDEX);
                if (skipUploadInstallList == null) {
                    return;
                }
            } else {
                skipUploadInstallList = DEFAULT_SKIP_INSTALL_UPLOAD_LIST;
            }
            if (!skipUploadInstallList.contains(installerPackageName)) {
                StringBuilder builder = new StringBuilder();
                Signature[] signatures = null;
                if (pkg.mSigningDetails != null) {
                    if (pkg.mSigningDetails.hasPastSigningCertificates()) {
                        signatures = pkg.mSigningDetails.pastSigningCertificates;
                    } else if (pkg.mSigningDetails.hasSignatures()) {
                        signatures = pkg.mSigningDetails.signatures;
                    }
                }
                if (signatures != null) {
                    for (Signature signature : signatures) {
                        builder.append(computeSignatureCertDigest(signature, true));
                        builder.append(";");
                    }
                    String cert = builder.toString();
                    if (cert.length() > CERT_RESULT_MAX_SIZE) {
                        cert = cert.substring(0, CERT_RESULT_MAX_SIZE);
                    }
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("app_pkg", pkg.packageName);
                        map.put("code", String.valueOf(pkg.mVersionCode));
                        map.put(ColorStartingWindowRUSHelper.VERSION_NAME, String.valueOf(pkg.mVersionName));
                        if (pkg.baseCodePath != null) {
                            map.put("md5", FileUtil.getInstance().getFileMd5(new File(pkg.baseCodePath)));
                        }
                        map.put("cert", cert);
                        if (pkg.applicationInfo != null) {
                            map.put("targetSdkVersion", String.valueOf(pkg.applicationInfo.targetSdkVersion));
                        }
                        OppoStatistics.onCommon(context, APP_CODE, ACTION_EVENTID_NEW_INSTALL_INFO, map, false);
                        if (PackageManagerService.DEBUG_INSTALL) {
                            Slog.d(TAG, "uploadInstallAppInfos:" + map + ",path=" + pkg.baseCodePath + ",installerPackageName:" + installerPackageName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void removeForceLauncher() {
        mAllowSetLauncher = true;
        ArrayList<String> arrayList = SYSTEM_FORCE_PACKAGES;
        if (arrayList != null && arrayList.contains("com.oppo.launcher")) {
            SYSTEM_FORCE_PACKAGES.remove("com.oppo.launcher");
        }
    }

    public static boolean isOppoSignature(PackageParser.Package pkg) {
        for (String signature : oppoSigList) {
            if (isSha256CertMatchPackage(pkg, signature)) {
                return true;
            }
        }
        return false;
    }

    public static void filterHideLauncherIconList(Context context, List<ResolveInfo> query) {
        ArrayList<String> hideBlackAppIconList;
        new ArrayList();
        if (query != 0 && !query.isEmpty()) {
            if (sPmsWhiteList.indexOfKey(OPPO_HIDE_DESKTOP_ICON_LIST_INDEX) >= 0) {
                hideBlackAppIconList = sPmsWhiteList.get(OPPO_HIDE_DESKTOP_ICON_LIST_INDEX);
                if (hideBlackAppIconList == null) {
                    return;
                }
            } else {
                hideBlackAppIconList = DEFAULT_OPPO_HIDE_DESKTOP_ICON_LIST;
            }
            List<ResolveInfo> black = null;
            for (ResolveInfo ri : query) {
                if (hideBlackAppIconList.contains(ri.activityInfo.packageName)) {
                    if (black == null) {
                        black = new ArrayList<>();
                    }
                    black.add(ri);
                }
            }
            if (black != null) {
                query.removeAll(black);
            }
        }
    }

    public static void oppoReadForbidUninstallPkg() {
        Slog.i(TAG, "init pkgs for forbidUninstallListg");
        initForbidUninstallPath();
        initForbidUninstallObserver();
        readForbidUninstallFile();
    }

    private static void initForbidUninstallPath() {
        Slog.i(TAG, "init path for forbidUninstallFile");
        File oppoForbidUninstallFile = new File(OPPO_FORBID_UNINSTALL_DATA_PKG_FILE);
        File parent = oppoForbidUninstallFile.getParentFile();
        if (!oppoForbidUninstallFile.exists()) {
            if (!parent.exists()) {
                parent.mkdirs();
            }
            try {
                oppoForbidUninstallFile.createNewFile();
            } catch (IOException e) {
                Slog.w(TAG, "create forbid uninstall file error");
            }
        }
    }

    private static void initForbidUninstallObserver() {
        Slog.i(TAG, "init observer for forbidUninstallFile");
        sForbidUninstallLisObserver = new FileObserverForbidUninstallFile(OPPO_FORBID_UNINSTALL_DATA_PKG_FILE);
        sForbidUninstallLisObserver.startWatching();
    }

    private static class FileObserverForbidUninstallFile extends FileObserver {
        private String mFocusPath;

        public FileObserverForbidUninstallFile(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && ColorPackageManagerHelper.OPPO_FORBID_UNINSTALL_DATA_PKG_FILE.equals(this.mFocusPath)) {
                Slog.i(ColorPackageManagerHelper.TAG, "onEvent: focusPath = OPPO_FORBID_UNINSTALL_DATA_PKG_FILE");
                ColorPackageManagerHelper.sForbidUninstallDataAppList.clear();
                ColorPackageManagerHelper.readForbidUninstallFile();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fe A[LOOP:1: B:50:0x00f8->B:52:0x00fe, LOOP_END] */
    public static void readForbidUninstallFile() {
        Iterator<String> it;
        StringBuilder sb;
        String value;
        File xmlFile = new File(OPPO_FORBID_UNINSTALL_DATA_PKG_FILE);
        if (xmlFile.exists()) {
            FileReader xmlReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader2 = new FileReader(xmlFile);
                    parser.setInput(xmlReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0 && eventType == 2) {
                            if (parser.getName().equals(TAG_FORBID_UNINSTALL_APP_VER)) {
                                String value2 = parser.nextText();
                                if (value2 != null) {
                                    sForbidUninstallDataAppList.add(value2);
                                }
                            } else if (parser.getName().equals(TAG_FORBID_UNINSTALL_APP_VER_SWITCH) && (value = parser.nextText()) != null) {
                                if (value.equals("0")) {
                                    sForbidUninstallDataAppSwitch = false;
                                } else {
                                    sForbidUninstallDataAppSwitch = true;
                                }
                            }
                        }
                    }
                    try {
                        xmlReader2.close();
                    } catch (IOException e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                } catch (FileNotFoundException e2) {
                    Slog.w(TAG, "Couldn't find or open sys_pms_defaultpackage_list file " + xmlFile);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                            return;
                        } catch (IOException e3) {
                            Slog.w(TAG, "Got exception close xml." + e3);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } catch (Exception e4) {
                Slog.w(TAG, "Got exception parsing xml" + e4);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e6) {
                        Slog.w(TAG, "Got exception close xml." + e6);
                    }
                }
                throw th;
            }
            Slog.d(TAG, "forbid uninstall switch " + sForbidUninstallDataAppSwitch);
            it = sForbidUninstallDataAppList.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "forbid uninstall" + it.next());
            }
        }
        return;
        sb.append("Got exception close xml.");
        sb.append(e);
        Slog.w(TAG, sb.toString());
        Slog.d(TAG, "forbid uninstall switch " + sForbidUninstallDataAppSwitch);
        it = sForbidUninstallDataAppList.iterator();
        while (it.hasNext()) {
        }
    }

    public static boolean isForbidUninstallSystemUpdateApp(PackageParser.Package pkg) {
        if (pkg == null || pkg.packageName == null) {
            return false;
        }
        String pkgVer = pkg.packageName + "&" + pkg.mVersionCode;
        if (!pkg.isUpdatedSystemApp() || !sForbidUninstallDataAppSwitch || !sForbidUninstallDataAppList.contains(pkgVer)) {
            return false;
        }
        return true;
    }

    public static boolean isNewUserInstallPkg(String pkgName) {
        if (pkgName != null) {
            return sMultiUserInstallList.contains(pkgName);
        }
        return false;
    }

    public static boolean isNewUserNotInstallPkg(String pkgName) {
        if (pkgName == null || sMultiUserNotInstallList.contains(pkgName) || DEFAULT_NOT_MULTI_APP_INSTALL_LIST.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public static void replaceDefaultLauncherCustom(PackageManagerService pms) {
        String customLauncher;
        if (pms != null) {
            sHasDefaultLauncherFeature = pms.hasSystemFeature("oppo.customize.function.default_launcher", 0);
            if (sHasDefaultLauncherFeature && (customLauncher = getDefaultLauncherAppList()) != null && customLauncher.length() > 0) {
                SYSTEM_DEFAULT_PACKAGES.remove("com.oppo.launcher");
                SYSTEM_DEFAULT_PACKAGES.add(customLauncher);
                SYSTEM_FORCE_PACKAGES.remove("com.oppo.launcher");
                SYSTEM_FORCE_PACKAGES.add(customLauncher);
                Slog.d(TAG, "custom launcher:" + customLauncher);
            }
        }
    }

    private static void replaceLauncher() {
        String customLauncher = getDefaultLauncherAppList();
        if (customLauncher != null && customLauncher.length() > 0) {
            if (sOppoDefaultPkgList.size() > 0) {
                sOppoDefaultPkgList.remove("com.oppo.launcher");
                sOppoDefaultPkgList.add(customLauncher);
            }
            if (sOppoForcePkgList.size() > 0) {
                sOppoForcePkgList.remove("com.oppo.launcher");
                sOppoForcePkgList.add(customLauncher);
            }
            Slog.d(TAG, "custom launcher:" + customLauncher);
        }
    }

    /*  JADX ERROR: IF instruction can be used only in fallback mode
        jadx.core.utils.exceptions.CodegenException: IF instruction can be used only in fallback mode
        	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:600)
        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:490)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:253)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:110)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:56)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:194)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:67)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:306)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:69)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:244)
        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:237)
        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:347)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:300)
        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:269)
        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:390)
        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
        */
    private static java.lang.String getDefaultLauncherAppList() {
        /*
            java.lang.String r0 = "failed parsing "
            java.lang.String r1 = "/system/etc/oppo_customize_whitelist.xml"
            java.lang.String r2 = ""
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.io.File r4 = new java.io.File
            java.lang.String r5 = "/system/etc/oppo_customize_whitelist.xml"
            r4.<init>(r5)
            boolean r5 = r4.exists()
            java.lang.String r6 = "ColorPackageManager"
            if (r5 != 0) goto L_0x0020
            java.lang.String r0 = "/system/etc/oppo_customize_whitelist.xml file don't exist!"
            android.util.Slog.w(r6, r0)
            return r2
        L_0x0020:
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r7 = 0
            java.io.FileInputStream r8 = new java.io.FileInputStream     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            r8.<init>(r4)     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            r7 = r8
            org.xmlpull.v1.XmlPullParser r8 = android.util.Xml.newPullParser()     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            r9 = 0
            r8.setInput(r7, r9)     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            r10 = 0
        L_0x0035:
            int r11 = r8.next()     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            r10 = r11
            r11 = 2
            if (r10 != r11) goto L_0x0054
            java.lang.String r11 = r8.getName()     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            java.lang.String r12 = "launcher"
            boolean r12 = r12.equals(r11)     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            if (r12 == 0) goto L_0x0054
            java.lang.String r12 = "att"
            java.lang.String r12 = r8.getAttributeValue(r9, r12)     // Catch:{ NullPointerException -> 0x0095, NumberFormatException -> 0x008b, XmlPullParserException -> 0x0081, IOException -> 0x0077, IndexOutOfBoundsException -> 0x006d, Exception -> 0x0063 }
            if (r12 == 0) goto L_0x0054
            r0 = r12
            r2 = r0
            goto L_0x0057
        L_0x0054:
            r11 = 1
            if (r10 != r11) goto L_0x0035
        L_0x0057:
            r7.close()     // Catch:{ IOException -> 0x005c }
        L_0x005b:
            goto L_0x009f
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x009f
        L_0x0061:
            r0 = move-exception
            goto L_0x00a0
        L_0x0063:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)     // Catch:{ all -> 0x0061 }
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x006d:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x0077:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x0081:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x008b:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x0095:
            r8 = move-exception
            android.util.Slog.w(r6, r0, r8)
            if (r7 == 0) goto L_0x005b
            r7.close()
            goto L_0x005b
        L_0x009f:
            return r2
        L_0x00a0:
            if (r7 == 0) goto L_0x00ab
            r7.close()     // Catch:{ IOException -> 0x00a6 }
            goto L_0x00ab
        L_0x00a6:
            r6 = move-exception
            r6.printStackTrace()
            goto L_0x00ac
        L_0x00ab:
        L_0x00ac:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ColorPackageManagerHelper.getDefaultLauncherAppList():java.lang.String");
    }

    public static boolean isSafeCenterWhitePackageName(String packageName) {
        ArrayList<String> safecenterWhiteList;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (sPmsWhiteList.indexOfKey(OPPO_SAFECENTER_WHITE_LIST_INDEX) >= 0) {
            safecenterWhiteList = sPmsWhiteList.get(OPPO_SAFECENTER_WHITE_LIST_INDEX);
            if (safecenterWhiteList == null) {
                return false;
            }
        } else {
            safecenterWhiteList = DEFAULT_SAFECENTER_WHITE_NAME_LIST;
        }
        return safecenterWhiteList.contains(packageName);
    }

    public static boolean isPermKeyInBlackList(String certDigest, String packageName) {
        if (sPermKeyBlackList.isEmpty()) {
            if (DEFAULT_PERM_KEY_BLACKLIST.containsKey(certDigest)) {
                return DEFAULT_PERM_KEY_BLACKLIST.get(certDigest) == null || DEFAULT_PERM_KEY_BLACKLIST.get(certDigest).contains(packageName);
            }
        } else if (sPermKeyBlackList.containsKey(certDigest)) {
            return sPermKeyBlackList.get(certDigest) == null || sPermKeyBlackList.get(certDigest).contains(packageName);
        }
        return false;
    }
}
