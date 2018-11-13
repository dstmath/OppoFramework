package com.android.server.pm;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.format.Time;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoShellPermissionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class OppoPackageManagerHelper {
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_FILTRATE_APP_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final ArrayList<String> COPY_APP_PATH = new ArrayList();
    private static final long DATA_MIN_SIZE = 52428800;
    private static final boolean DEBUG = false;
    private static final String FILTRATE_APP_FEATURE_NAME = "oppo.filtrated.app";
    private static final String FILTRATE_APP_NAME = "market-filter";
    private static final int INVALID_ID = -99;
    private static final String KNOWN_MARKET_FILE_PATH = "/data/oppo/coloros/permission/known_markets.xml";
    private static final String MARKET_ACTION = "oppo.intent.action.SafeCenter.FILTER_MARKET";
    private static final String MARKET_EXTRA_IS_ADDNEW = "is_addnew";
    private static final String MARKET_EXTRA_IS_BLACK = "is_black";
    private static final String MARKET_EXTRA_IS_MANUALOPEN = "is_manualopen";
    private static final String MARKET_EXTRA_IS_WHITE = "is_white";
    private static final String MARKET_EXTRA_NEW_LIST = "new_list";
    private static final String MARKET_EXTRA_PACKAGE_NAME = "package_name";
    private static final String MARKET_FILTER_FILE_PATH = "/data/oppo/coloros/permission/market_filter.xml";
    private static final String NEARME_PACAKGE_FLAG = "com.nearme.";
    private static final String NOT_UNINSTALL_DIR = "/data/system";
    private static final String NOT_UNINSTALL_PATH = "/data/system/not_uninstall_packages.xml";
    private static final String NOT_UNINSTALL_SYSTEM_PATH = "/system/etc/not_uninstall_packages.xml";
    public static final String OPPO_EXTRA_DEBUG_INFO = "oppo_extra_debug_info";
    public static final String OPPO_EXTRA_PID = "oppo_extra_pid";
    public static final String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
    public static final String OPPO_EXTRA_UID = "oppo_extra_uid";
    public static final String OPPO_EXTRA_VERSION_CODE = "oppo_extra_version_code";
    public static final String OPPO_EXTRA_VERSION_NAME = "oppo_extra_version_name";
    private static final String OPPO_PACKAGE_FLAG = "com.oppo.";
    private static final String PRESET_APP_FILE_PATH = "/data/oppo/coloros/permission/preset_apps.xml";
    private static final String STATE_CLOSE = "0";
    private static final String STATE_OPEN = "1";
    private static final String[] SYSTEM_DEFAULT_PACKAGES = new String[]{"com.android.browser", "com.oppo.music", ActivityManagerService.OPPO_LAUNCHER, "com.android.contacts", "com.oppo.camera", "com.oppo.gallery3d", "com.oppo.video", "com.android.email", "com.android.dialer"};
    private static final String TAG = "OppoPackageManager";
    private static final String TAG_LOCAL_VERSION = "LocalVersion";
    private static final String TAG_NOT_UNINSTALL = "NotUninstall";
    private static final int UID = 10000;
    private static final String mNetLock = SystemProperties.get("ro.oppo.region.netlock", "");
    private static final String mOperator = SystemProperties.get("ro.oppo.operator", "");
    static final ArrayList<String> mOppoShareUid = new ArrayList();
    private static final String mRegionMark = SystemProperties.get("ro.oppo.regionmark", "");
    private static ArrayList<String> sBlackList = new ArrayList();
    private static int sCalledPid = 0;
    private static File sDataAppDir = null;
    static PackageInfo sDeleteInfo = null;
    private static ArrayList<String> sFiltrateAppNameList = new ArrayList();
    private static boolean sFiltrateAppSwitch;
    private static ArrayList<String> sFiltrateMarketNameList = new ArrayList();
    public static boolean sForceToSD = false;
    private static Map<String, String> sKnownMarketMap = new HashMap();
    private static ReentrantLock sMarketLock = new ReentrantLock();
    private static ArrayList<String> sNewMarketList = new ArrayList();
    private static ArrayList<String> sNotUninstallList = new ArrayList();
    static int sPid = -1;
    static int sUid = -1;
    private static ArrayList<String> sWhiteList = new ArrayList();

    static {
        COPY_APP_PATH.add("/system/reserve/");
        COPY_APP_PATH.add("/data/reserve/");
        COPY_APP_PATH.add("/system/data_app_" + mOperator + "/");
        COPY_APP_PATH.add("/system/data_app_" + mNetLock + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mOperator + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mNetLock + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mRegionMark + "/");
    }

    public static boolean isGoogleMarket(int callUid, int uid) {
        if (callUid == uid) {
            return true;
        }
        return false;
    }

    public static boolean isShareUid(String string) {
        return mOppoShareUid.contains(string);
    }

    public static void isFirstBoot(boolean isFirst) {
        if (isFirst) {
            Log.d(TAG, "packages.xml NOT exists, firstboot!!!");
            SystemProperties.set("oppo.device.firstboot", "1");
            return;
        }
        SystemProperties.set("oppo.device.firstboot", STATE_CLOSE);
    }

    private static boolean isExists(String pkgName) {
        if (sDataAppDir == null) {
            sDataAppDir = new File(Environment.getDataDirectory(), "app");
        }
        if (!sDataAppDir.exists()) {
            return false;
        }
        File[] files = sDataAppDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Log.d(TAG, "No files in app dir " + sDataAppDir);
            return false;
        }
        for (File apkFile : files) {
            if (apkFile.getName().startsWith(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public static void restorePresetApk(Settings settings, File appInstallDir) {
        for (String appCopyPath : COPY_APP_PATH) {
            File oppoReserveApkPath = new File(appCopyPath);
            if (oppoReserveApkPath.exists() && SystemProperties.getBoolean("oppo.device.firstboot", false) && oppoReserveApkPath.listFiles() != null) {
                File[] listFiles = oppoReserveApkPath.listFiles();
                int i = 0;
                int length = listFiles.length;
                while (i < length) {
                    File apkFile = listFiles[i];
                    try {
                        Log.d(TAG, "copy apk to /data/app:" + apkFile);
                        long val = getAvailableDataSize();
                        if (val < DATA_MIN_SIZE) {
                            Log.i(TAG, "data size less than 50M, so break! val = " + val);
                            break;
                        }
                        PackageLite pkg = PackageParser.parsePackageLite(apkFile, 2);
                        if (pkg == null) {
                            Log.i(TAG, "reserve package null, error!!!");
                            break;
                        }
                        if (isExists(pkg.packageName)) {
                            Log.i(TAG, "apk:" + pkg.packageName + " has been installed, skip");
                        } else {
                            File destFile = new File(appInstallDir, apkFile.getName());
                            Log.i(TAG, "apk:" + pkg.packageName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                            FileUtils.copyFile(apkFile, destFile);
                            FileUtils.setPermissions(destFile.getPath(), 420, -1, -1);
                        }
                        i++;
                    } catch (PackageParserException e) {
                        Log.e(TAG, "create firstboot flag file error!!!");
                    }
                }
            }
        }
    }

    public static void parsePackageXml() {
        File permFile = new File(Environment.getRootDirectory(), "oppo/oppo_package.xml");
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equals("OppoShareUid")) {
                                eventType = parser.next();
                                mOppoShareUid.add(parser.getText());
                                break;
                            }
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
            Slog.w(TAG, "Couldn't find or open oppo_package file " + permFile);
        }
    }

    public static void parsePackageXmlForCamera() {
        File permFile = new File(Environment.getRootDirectory(), "etc/oppo_package_camera.xml");
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equals("OppoShareUid")) {
                                eventType = parser.next();
                                mOppoShareUid.add(parser.getText());
                                break;
                            }
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
            Slog.w(TAG, "Couldn't find or open oppo_package file " + permFile);
        }
    }

    public static boolean parsePackagesXml(File xmlFile) {
        Exception ex;
        long t;
        Time time;
        String errFile;
        if (!xmlFile.exists()) {
            return false;
        }
        try {
            InputStream input = new FileInputStream(xmlFile);
            try {
                SAXParserFactory.newInstance().newSAXParser().parse(input, new DefaultHandler());
                input.close();
                return true;
            } catch (Exception e) {
                ex = e;
                Log.i(TAG, "parse " + xmlFile + " error!!!!!!!");
                ex.printStackTrace();
                t = System.currentTimeMillis();
                time = new Time();
                time.set(t);
                errFile = "/data/packages-error_" + time.format2445() + ".xml";
                Log.i(TAG, "copyFile:" + xmlFile + " " + errFile);
                FileUtils.copyFile(xmlFile, new File(errFile));
                return false;
            }
        } catch (Exception e2) {
            ex = e2;
            Log.i(TAG, "parse " + xmlFile + " error!!!!!!!");
            ex.printStackTrace();
            t = System.currentTimeMillis();
            time = new Time();
            time.set(t);
            errFile = "/data/packages-error_" + time.format2445() + ".xml";
            Log.i(TAG, "copyFile:" + xmlFile + " " + errFile);
            FileUtils.copyFile(xmlFile, new File(errFile));
            return false;
        }
    }

    public static void setDataCollection() {
        sUid = Binder.getCallingUid();
        sPid = Binder.getCallingPid();
    }

    public static void resetDataCollection() {
        sUid = INVALID_ID;
        sPid = INVALID_ID;
    }

    public static void restoreDeleteInfo(PackageInfo info) {
        sDeleteInfo = info;
    }

    public static void resetDeleteInfo() {
        sDeleteInfo = null;
    }

    public static void sendPackageInstallBroadcast(ApplicationInfo info, Bundle extras, int userId, UserManagerService sUserManager) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (!(am == null || info == null)) {
            try {
                IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
                if (!"com.android.packageinstaller".equals(pm.getNameForUid(sUid))) {
                    Uri fromParts;
                    userId = UserHandle.myUserId();
                    String str = "oppo.intent.action.OPPO_PACKAGE_ADDED";
                    if (info.packageName != null) {
                        fromParts = Uri.fromParts("package", info.packageName, null);
                    } else {
                        fromParts = null;
                    }
                    Intent intent = new Intent(str, fromParts);
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (uid > 0 && userId > 0) {
                        intent.putExtra("android.intent.extra.UID", UserHandle.getUid(userId, UserHandle.getAppId(uid)));
                    }
                    intent.putExtra("UID", sUid);
                    intent.putExtra("PID", sPid);
                    StringBuffer debugInfo = new StringBuffer("dataCollection debug info ");
                    String mCallingPkgName = getProcessNameByPid(sPid);
                    if (mCallingPkgName == null || mCallingPkgName == "") {
                        debugInfo.append(" get an empty package name by pid");
                        mCallingPkgName = pm.getNameForUid(sUid);
                        if (mCallingPkgName == null || mCallingPkgName == "") {
                            debugInfo.append(" get an empty package name by uid");
                        }
                    }
                    debugInfo.append(" UID ");
                    debugInfo.append(sUid);
                    debugInfo.append(" PID ");
                    debugInfo.append(sPid);
                    debugInfo.append(" mCallingPkgName ");
                    debugInfo.append(mCallingPkgName);
                    String str2 = "oppo_extra_pkg_name";
                    if (mCallingPkgName == null) {
                        mCallingPkgName = "";
                    }
                    intent.putExtra(str2, mCallingPkgName);
                    intent.putExtra(OPPO_EXTRA_DEBUG_INFO, debugInfo.toString());
                    am.broadcastIntent(null, intent, null, null, 0, null, null, null, -1, null, true, false, userId);
                    resetDataCollection();
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void sendDataClearBroadcast() {
        try {
            ActivityManagerNative.getDefault().broadcastIntent(null, new Intent("oppo.intent.action.DATA_COLLECT_CLEAR", null), null, null, 0, null, null, null, -1, null, false, false, 0);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProcessNameByPid(int pid) {
        String processName = "";
        try {
            for (RunningAppProcessInfo proc : ActivityManagerNative.getDefault().getRunningAppProcesses()) {
                if (pid == proc.pid) {
                    return proc.processName;
                }
            }
            return processName;
        } catch (Exception e) {
            Log.e(TAG, "Exception had happen!!!");
            return processName;
        }
    }

    public static void initNotUnintall() {
        initDir(NOT_UNINSTALL_DIR);
        parseNotUninstallXml();
    }

    private static void initDir(String dir) {
        File file = new File(dir);
        try {
            if (!file.exists()) {
                file.mkdirs();
                copyFile(NOT_UNINSTALL_SYSTEM_PATH, NOT_UNINSTALL_PATH);
            } else if (!copyFile(NOT_UNINSTALL_SYSTEM_PATH, NOT_UNINSTALL_PATH) && getVersionNumber(NOT_UNINSTALL_SYSTEM_PATH) > getVersionNumber(NOT_UNINSTALL_PATH)) {
                File targetFile = new File(NOT_UNINSTALL_PATH);
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                copyFile(NOT_UNINSTALL_SYSTEM_PATH, NOT_UNINSTALL_PATH);
            }
        } catch (Exception e) {
            Slog.w(TAG, "mkdir failed " + e);
        }
    }

    public static void parseNotUninstallXml() {
        File file = new File(NOT_UNINSTALL_PATH);
        try {
            FileReader permReader = new FileReader(file);
            sNotUninstallList.clear();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equals(TAG_NOT_UNINSTALL)) {
                                eventType = parser.next();
                                String value = parser.getText();
                                if (!(value == null || value == "")) {
                                    sNotUninstallList.add(parser.getText());
                                    break;
                                }
                            }
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
            Slog.w(TAG, "Couldn't find or open oppo_package file " + file);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x007d A:{SYNTHETIC, Splitter: B:36:0x007d} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x008a A:{SYNTHETIC, Splitter: B:43:0x008a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int getVersionNumber(String path) {
        Throwable th;
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }
        int versionNumber = -1;
        FileInputStream stream = null;
        boolean success = false;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                for (int eventType = parser.getEventType(); eventType != 1 && (success ^ 1) != 0; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (!parser.getName().equals(TAG_LOCAL_VERSION)) {
                                break;
                            }
                            eventType = parser.next();
                            String value = parser.getText();
                            if (!(value == null || value == "")) {
                                versionNumber = Integer.parseInt(value);
                                success = true;
                                break;
                            }
                        default:
                            break;
                    }
                }
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                stream = stream2;
            } catch (Exception e2) {
                stream = stream2;
                versionNumber = -1;
                try {
                    Slog.w(TAG, "Couldn't getVersionNumber from file " + file);
                    if (stream != null) {
                    }
                    return versionNumber;
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
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
        } catch (Exception e4) {
            versionNumber = -1;
            Slog.w(TAG, "Couldn't getVersionNumber from file " + file);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
            return versionNumber;
        }
        return versionNumber;
    }

    public static boolean inNotUninstallList(String packageName) {
        if (packageName == null && packageName == "") {
            return false;
        }
        return sNotUninstallList.contains(packageName);
    }

    private static boolean copyFile(String fromFile, String toFile) throws IOException {
        File targetFile = new File(toFile);
        if (targetFile.exists()) {
            return false;
        }
        FileUtils.copyFile(new File(fromFile), targetFile);
        return true;
    }

    public static ResolveInfo checkIntent(PackageManagerService pms, Intent intent, String resolvedType, int flags, int userId, ResolveInfo defaultResolveInfo) {
        if (!pms.hasSystemFeature("oppo.ct.optr", 0) || intent == null || intent.getAction() == null || intent.getScheme() == null || !intent.getAction().equals("android.intent.action.VIEW") || !intent.getScheme().equals("http")) {
            return defaultResolveInfo;
        }
        intent.setClassName("com.android.browser", "com.android.browser.RealBrowserActivity");
        return pms.resolveIntent(intent, resolvedType, flags, userId);
    }

    public static boolean isSystemDefaultActivities(PreferredActivity pa) {
        for (String name : SYSTEM_DEFAULT_PACKAGES) {
            if (pa.mPref.mComponent.getPackageName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static String findSystemDefaultApp(String[] cp) {
        String mComponent = null;
        int len = cp.length;
        for (int i = 0; i < len; i++) {
            for (CharSequence contains : SYSTEM_DEFAULT_PACKAGES) {
                if (cp[i].contains(contains)) {
                    mComponent = cp[i];
                    break;
                }
            }
        }
        return mComponent;
    }

    public static ComponentName[] findCompSet(String[] cp, PreferredActivity pa) {
        int num = cp.length - 1;
        ComponentName[] setNew = new ComponentName[num];
        ComponentName rmCompName = pa.mPref.mComponent;
        int len = cp.length;
        int m = 0;
        int i = 0;
        int n = 0;
        while (i < len) {
            int n2;
            String[] comp = cp[i].split("/");
            if (comp[0].equals(rmCompName.getPackageName())) {
                n2 = n;
            } else {
                m++;
                String pkgName = comp[0];
                String clsName = comp[0] + comp[1];
                if (m <= num) {
                    n2 = n + 1;
                    setNew[n] = new ComponentName(pkgName, clsName);
                } else {
                    n2 = n;
                }
            }
            i++;
            n = n2;
        }
        return setNew;
    }

    public static void removeActiveAdmin(PackageManagerService pms, String packageName, int userId) {
        if (!checkSystemApp(pms, packageName)) {
            IDevicePolicyManager dpm = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
            if (dpm != null) {
                Intent intent = new Intent("android.app.action.DEVICE_ADMIN_ENABLED");
                List<ResolveInfo> avail = pms.queryIntentReceiversInternal(intent, intent.resolveTypeIfNeeded(pms.mContext.getContentResolver()), 128, userId, false);
                if (!(avail == null || avail.size() == 0)) {
                    int i = 0;
                    while (i < avail.size()) {
                        try {
                            ResolveInfo ri = (ResolveInfo) avail.get(i);
                            if (ri.activityInfo.packageName.equals(packageName)) {
                                try {
                                    dpm.removeActiveAdmin(new ComponentName(packageName, ri.activityInfo.name), userId);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            i++;
                        } catch (Exception e2) {
                            Log.i(TAG, "Exception happened!!!" + e2);
                        }
                    }
                }
            }
        }
    }

    private static boolean checkSystemApp(PackageManagerService pms, String pkgName) {
        PackageInfo pkgInfo = pms.getPackageInfo(pkgName, 0, UserHandle.myUserId());
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            Log.d(TAG, pkgName + " does not exits!");
            return false;
        } else if ((pkgInfo.applicationInfo.flags & 1) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String setAppNativeLibraryPath(Package pkg, PackageSetting pkgSetting, File mAppLibInstallDir) {
        String nativeLibraryPath = new File(mAppLibInstallDir, PackageManagerService.deriveCodePathName(pkg.baseCodePath)).getPath();
        if (nativeLibraryPath == null || (pkg.applicationInfo.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0) {
            return pkgSetting.legacyNativeLibraryPathString;
        }
        return nativeLibraryPath;
    }

    private static long getAvailableDataSize() {
        StatFs mStatFs = new StatFs(Environment.getDataDirectory().getPath());
        if (mStatFs == null) {
            return 0;
        }
        return mStatFs.getBlockSizeLong() * mStatFs.getAvailableBlocksLong();
    }

    private static void initData() {
        if (!sFiltrateMarketNameList.isEmpty()) {
            sFiltrateMarketNameList.clear();
        }
        if (!sFiltrateAppNameList.isEmpty()) {
            sFiltrateAppNameList.clear();
        }
        if (!sBlackList.isEmpty()) {
            sBlackList.clear();
        }
        if (!sWhiteList.isEmpty()) {
            sWhiteList.clear();
        }
        if (!sKnownMarketMap.isEmpty()) {
            sKnownMarketMap.clear();
        }
        if (!sNewMarketList.isEmpty()) {
            sNewMarketList.clear();
        }
        StringBuffer sb = new StringBuffer();
        try {
            File file = new File(MARKET_FILTER_FILE_PATH);
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while (true) {
                    String tempstr = br.readLine();
                    if (tempstr == null) {
                        break;
                    }
                    sb.append(tempstr);
                }
                parserFilterAppList(sb.toString());
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            sKnownMarketMap = getKnownMarketMap();
        } catch (Exception e) {
            Log.e(TAG, "We can not get Filtrate app data from provider, because of " + e);
            e.printStackTrace();
        }
    }

    private static void parserFilterAppList(String xml) throws XmlPullParserException, IOException {
        if (xml == null || xml.length() <= 0) {
            Log.e(TAG, "parserFilterAppList() xml empty, return.");
            return;
        }
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(xml));
            parser.nextTag();
            for (int evenType = parser.getEventType(); evenType != 1; evenType = parser.next()) {
                if (2 == evenType) {
                    String tagName = parser.getName();
                    if ("markets".equals(tagName)) {
                        String marketName = parser.nextText();
                        if (!marketName.equals("")) {
                            sFiltrateMarketNameList.add(marketName);
                        }
                    } else if ("apps".equals(tagName)) {
                        String appName = parser.nextText();
                        if (!appName.equals("")) {
                            sFiltrateAppNameList.add(appName);
                        }
                    } else if ("blacklist".equals(tagName)) {
                        String blackName = parser.nextText();
                        if (!blackName.equals("")) {
                            sBlackList.add(blackName);
                        }
                    } else if ("whitelist".equals(tagName)) {
                        String whiteName = parser.nextText();
                        if (!whiteName.equals("")) {
                            sWhiteList.add(whiteName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parserFilterAppList(), Exception: " + e);
            e.printStackTrace();
        }
        return;
    }

    public static void filterThirdMarket(Context context, PackageManagerService pms, int flags, List<PackageInfo> packageInfos, Handler handler) {
        if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, false)) {
            if (pms.hasSystemFeature(FILTRATE_APP_FEATURE_NAME, 0)) {
                sFiltrateAppSwitch = true;
            } else {
                sFiltrateAppSwitch = false;
            }
            try {
                if (sFiltrateAppSwitch) {
                    int callingUid = Binder.getCallingUid();
                    if (callingUid >= 10000) {
                        initData();
                        boolean isAddNew = false;
                        boolean isSystem = false;
                        boolean isBlack = false;
                        boolean isWhite = false;
                        boolean isManualOpen = false;
                        boolean isBadMarket = false;
                        if (sKnownMarketMap == null) {
                            sKnownMarketMap = new HashMap();
                        }
                        String[] MarketName = pms.getPackagesForUid(callingUid);
                        if (MarketName.length > 0) {
                            int j = 0;
                            while (j < MarketName.length) {
                                if (sKnownMarketMap.isEmpty() || (sKnownMarketMap.containsKey(MarketName[j]) ^ 1) != 0) {
                                    try {
                                        PackageInfo pInfo = context.getPackageManager().getPackageInfo(MarketName[j], 0);
                                        if (pInfo.applicationInfo != null && (pInfo.applicationInfo.flags & 1) == 0 && (pInfo.applicationInfo.flags & 128) == 0) {
                                            sNewMarketList.add(MarketName[j]);
                                            isAddNew = true;
                                        } else {
                                            isSystem = true;
                                        }
                                    } catch (NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else if ("1".equals((String) sKnownMarketMap.get(MarketName[j]))) {
                                    isManualOpen = true;
                                }
                                if (!sBlackList.isEmpty()) {
                                    isBlack = sBlackList.contains(MarketName[j]);
                                }
                                if (!sWhiteList.isEmpty()) {
                                    isWhite = sWhiteList.contains(MarketName[j]);
                                }
                                if (!sFiltrateMarketNameList.isEmpty()) {
                                    isBadMarket = sFiltrateMarketNameList.contains(MarketName[j]);
                                }
                                if ((isSystem | isBlack) != 0) {
                                    break;
                                }
                                j++;
                            }
                            if (!isSystem) {
                                if (isBlack) {
                                    packageInfos.clear();
                                    packageInfos.add(pms.getPackageInfo(MarketName.length > 0 ? MarketName[0] : "", flags, UserHandle.myUserId()));
                                    return;
                                } else if (!isWhite) {
                                    int calledPid = Binder.getCallingPid();
                                    if (!(sCalledPid == calledPid || ((isManualOpen ^ 1) | isAddNew) == 0)) {
                                        sCalledPid = calledPid;
                                        try {
                                            IActivityManager am = ActivityManagerNative.getDefault();
                                            final Intent intent = new Intent(MARKET_ACTION, null);
                                            intent.putExtra(MARKET_EXTRA_PACKAGE_NAME, MarketName.length > 0 ? MarketName[0] : null);
                                            intent.putExtra(MARKET_EXTRA_IS_BLACK, isBlack);
                                            intent.putExtra(MARKET_EXTRA_IS_WHITE, isWhite);
                                            intent.putExtra(MARKET_EXTRA_IS_ADDNEW, isAddNew);
                                            intent.putExtra(MARKET_EXTRA_IS_MANUALOPEN, isManualOpen);
                                            intent.putStringArrayListExtra(MARKET_EXTRA_NEW_LIST, sNewMarketList);
                                            final Context context2 = context;
                                            handler.postDelayed(new Runnable() {
                                                public void run() {
                                                    context2.sendBroadcast(intent);
                                                }
                                            }, 50);
                                        } catch (Exception ex) {
                                            Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                            ex.printStackTrace();
                                        }
                                    }
                                    if (!isAddNew) {
                                        if (!isManualOpen) {
                                            packageInfos.clear();
                                            packageInfos.add(pms.getPackageInfo(MarketName.length > 0 ? MarketName[0] : "", flags, UserHandle.myUserId()));
                                            return;
                                        } else if (isBadMarket) {
                                            for (int i = 0; i < packageInfos.size(); i++) {
                                                if (sFiltrateAppNameList.contains(((PackageInfo) packageInfos.get(i)).packageName)) {
                                                    packageInfos.remove(i);
                                                }
                                            }
                                        }
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                Log.w(TAG, "OppoPackageManagerHelper do not hasSystemFeature oppo.filtrated.app !!!");
            } catch (Exception e2) {
                Log.e(TAG, "filterThirdMarket() Exception: " + e2);
            }
        }
    }

    private static Map<String, String> getKnownMarketMap() {
        Map<String, String> resultMap = null;
        sMarketLock.lock();
        try {
            File file = new File(KNOWN_MARKET_FILE_PATH);
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                resultMap = readMarketsFromXML(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            sMarketLock.unlock();
        } catch (Exception e) {
            Log.e(TAG, "getKnownMarketMap(), Exception: " + e);
            e.printStackTrace();
            sMarketLock.unlock();
        } catch (Throwable th) {
            sMarketLock.unlock();
            throw th;
        }
        return resultMap;
    }

    private static void setKnownMarketMap(Map<String, String> marketMap) {
        if (marketMap.size() <= 0) {
            Log.e(TAG, "setKnownMarketList() empty map, return.");
            return;
        }
        sMarketLock.lock();
        try {
            File file = new File(KNOWN_MARKET_FILE_PATH);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file = new File(KNOWN_MARKET_FILE_PATH);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            writeMarketsToXML(outputStream, marketMap);
            if (outputStream != null) {
                outputStream.close();
            }
            sMarketLock.unlock();
        } catch (Exception e2) {
            Log.e(TAG, "setKnownMarketMap(), Exception: " + e2);
            e2.printStackTrace();
            sMarketLock.unlock();
        } catch (Throwable th) {
            sMarketLock.unlock();
            throw th;
        }
    }

    private static Map<String, String> readMarketsFromXML(FileInputStream stream) {
        Map<String, String> marketMap = new HashMap();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    if ("market".equals(parser.getName())) {
                        Object obj = null;
                        Object text = null;
                        try {
                            obj = parser.getAttributeValue(null, "packagename");
                            text = parser.nextText();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!(obj == null || text == null)) {
                            marketMap.put(obj, text);
                        }
                    }
                }
            } while (type != 1);
        } catch (Exception e2) {
            Log.e(TAG, "readMarketsFromXML() failed parsing: " + e2);
            e2.printStackTrace();
        }
        return marketMap;
    }

    private static boolean writeMarketsToXML(FileOutputStream stream, Map<String, String> marketMap) {
        try {
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, Boolean.valueOf(true));
            out.text("\r\n");
            out.startTag(null, "marketlist");
            for (String name : marketMap.keySet()) {
                String value = (String) marketMap.get(name);
                out.text("\r\n");
                out.text("\t");
                out.startTag(null, "market");
                out.attribute(null, "packagename", name);
                out.text(value);
                out.endTag(null, "market");
            }
            out.text("\r\n");
            out.endTag(null, "marketlist");
            out.text("\r\n");
            out.endDocument();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeMarketsToXML() Failed to write status: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean revokeShellPermission(Context cotnext, String permName, int uid) {
        return OppoShellPermissionUtils.revokeShellPermission(cotnext, permName, uid);
    }
}
