package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
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
import com.android.server.am.OppoShellPermissionUtils;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.wm.AlertWindowNotification;
import com.color.util.ColorTypeCastingHelper;
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
import java.util.Iterator;
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
    private static final ArrayList<String> COPY_APP_PATH = new ArrayList<>();
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
    private static final String[] SYSTEM_DEFAULT_PACKAGES = {"com.android.browser", "com.oppo.music", "com.oppo.launcher", "com.android.contacts", "com.oppo.camera", "com.oppo.gallery3d", "com.oppo.video", "com.android.email", "com.android.dialer", "com.coloros.browser", "com.heytap.browser"};
    private static final String TAG = "OppoPackageManager";
    private static final String TAG_LOCAL_VERSION = "LocalVersion";
    private static final String TAG_NOT_UNINSTALL = "NotUninstall";
    private static final int UID = 10000;
    private static final String mNetLock = SystemProperties.get("ro.oppo.region.netlock", "");
    private static final String mOperator = SystemProperties.get("ro.oppo.operator", "");
    static final ArrayList<String> mOppoShareUid = new ArrayList<>();
    private static final String mRegionMark = SystemProperties.get("ro.oppo.regionmark", "");
    private static ArrayList<String> sBlackList = new ArrayList<>();
    private static int sCalledPid = 0;
    private static File sDataAppDir = null;
    static PackageInfo sDeleteInfo = null;
    private static ArrayList<String> sFiltrateAppNameList = new ArrayList<>();
    private static boolean sFiltrateAppSwitch;
    private static ArrayList<String> sFiltrateMarketNameList = new ArrayList<>();
    public static boolean sForceToSD = false;
    private static Map<String, String> sKnownMarketMap = new HashMap();
    private static ReentrantLock sMarketLock = new ReentrantLock();
    private static ArrayList<String> sNewMarketList = new ArrayList<>();
    private static ArrayList<String> sNotUninstallList = new ArrayList<>();
    static int sPid = -1;
    static int sUid = -1;
    private static ArrayList<String> sWhiteList = new ArrayList<>();

    static {
        COPY_APP_PATH.add("/system/reserve/");
        COPY_APP_PATH.add("/data/reserve/");
        ArrayList<String> arrayList = COPY_APP_PATH;
        arrayList.add("/system/data_app_" + mOperator + SliceClientPermissions.SliceAuthority.DELIMITER);
        ArrayList<String> arrayList2 = COPY_APP_PATH;
        arrayList2.add("/system/data_app_" + mNetLock + SliceClientPermissions.SliceAuthority.DELIMITER);
        ArrayList<String> arrayList3 = COPY_APP_PATH;
        arrayList3.add("/data/reserve/data_app_" + mOperator + SliceClientPermissions.SliceAuthority.DELIMITER);
        ArrayList<String> arrayList4 = COPY_APP_PATH;
        arrayList4.add("/data/reserve/data_app_" + mNetLock + SliceClientPermissions.SliceAuthority.DELIMITER);
        ArrayList<String> arrayList5 = COPY_APP_PATH;
        arrayList5.add("/data/reserve/data_app_" + mRegionMark + SliceClientPermissions.SliceAuthority.DELIMITER);
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
        if (!isFirst) {
            SystemProperties.set("oppo.device.firstboot", STATE_CLOSE);
            return;
        }
        Log.d(TAG, "packages.xml NOT exists, firstboot!!!");
        SystemProperties.set("oppo.device.firstboot", STATE_OPEN);
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
        Iterator<String> it = COPY_APP_PATH.iterator();
        while (it.hasNext()) {
            File oppoReserveApkPath = new File(it.next());
            if (oppoReserveApkPath.exists()) {
                if (SystemProperties.getBoolean("oppo.device.firstboot", false) && oppoReserveApkPath.listFiles() != null) {
                    File[] listFiles = oppoReserveApkPath.listFiles();
                    int length = listFiles.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        File apkFile = listFiles[i];
                        try {
                            Log.d(TAG, "copy apk to /data/app:" + apkFile);
                            long val = getAvailableDataSize();
                            if (val < DATA_MIN_SIZE) {
                                Log.i(TAG, "data size less than 50M, so break! val = " + val);
                                break;
                            }
                            PackageParser.PackageLite pkg = PackageParser.parsePackageLite(apkFile, Integer.MIN_VALUE);
                            if (pkg == null) {
                                Log.i(TAG, "reserve package null, error!!!");
                                break;
                            }
                            if (isExists(pkg.packageName)) {
                                Log.i(TAG, "apk:" + pkg.packageName + " has been installed, skip");
                            } else {
                                try {
                                    File destFile = new File(appInstallDir, apkFile.getName());
                                    Log.i(TAG, "apk:" + pkg.packageName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                                    FileUtils.copyFile(apkFile, destFile);
                                    FileUtils.setPermissions(destFile.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
                                } catch (PackageParser.PackageParserException e) {
                                }
                            }
                            i++;
                        } catch (PackageParser.PackageParserException e2) {
                            Log.e(TAG, "create firstboot flag file error!!!");
                            i++;
                        }
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
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (parser.getName().equals("OppoShareUid")) {
                                parser.next();
                                mOppoShareUid.add(parser.getText());
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
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (parser.getName().equals("OppoShareUid")) {
                                parser.next();
                                mOppoShareUid.add(parser.getText());
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
            Slog.w(TAG, "Couldn't find or open oppo_package file " + permFile);
        }
    }

    public static boolean parsePackagesXml(File xmlFile) {
        if (!xmlFile.exists()) {
            return false;
        }
        try {
            InputStream input = new FileInputStream(xmlFile);
            SAXParserFactory.newInstance().newSAXParser().parse(input, new DefaultHandler());
            input.close();
            return true;
        } catch (Exception ex) {
            Log.i(TAG, "parse " + xmlFile + " error!!!!!!!");
            ex.printStackTrace();
            long t = System.currentTimeMillis();
            Time time = new Time();
            time.set(t);
            String errFile = "/data/packages-error_" + time.format2445() + ".xml";
            Log.i(TAG, "copyFile:" + xmlFile + StringUtils.SPACE + errFile);
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
    }

    public static void sendDataClearBroadcast() {
    }

    public static String getProcessNameByPid(int pid) {
        try {
            for (ActivityManager.RunningAppProcessInfo proc : ActivityManagerNative.getDefault().getRunningAppProcesses()) {
                if (pid == proc.pid) {
                    return proc.processName;
                }
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Exception had happen!!!");
            return "";
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
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (parser.getName().equals(TAG_NOT_UNINSTALL)) {
                                parser.next();
                                String value = parser.getText();
                                if (!(value == null || value == "")) {
                                    sNotUninstallList.add(parser.getText());
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
            Slog.w(TAG, "Couldn't find or open oppo_package file " + file);
        }
    }

    private static int getVersionNumber(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }
        int versionNumber = -1;
        FileInputStream stream = null;
        boolean success = false;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            for (int eventType = parser.getEventType(); eventType != 1 && !success; eventType = parser.next()) {
                if (eventType == 2) {
                    if (parser.getName().equals(TAG_LOCAL_VERSION)) {
                        parser.next();
                        String value = parser.getText();
                        if (!(value == null || value == "")) {
                            versionNumber = Integer.parseInt(value);
                            success = true;
                        }
                    }
                }
            }
            try {
                stream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            versionNumber = -1;
            Slog.w(TAG, "Couldn't getVersionNumber from file " + file);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
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
        int total = SYSTEM_DEFAULT_PACKAGES.length;
        for (int i = 0; i < len; i++) {
            int j = 0;
            while (true) {
                if (j >= total) {
                    break;
                } else if (cp[i].contains(SYSTEM_DEFAULT_PACKAGES[j])) {
                    mComponent = cp[i];
                    break;
                } else {
                    j++;
                }
            }
        }
        return mComponent;
    }

    public static ComponentName[] findCompSet(String[] cp, PreferredActivity pa) {
        int num = cp.length - 1;
        ComponentName[] setNew = new ComponentName[num];
        ComponentName rmCompName = pa.mPref.mComponent;
        int n = 0;
        int m = 0;
        for (String str : cp) {
            String[] comp = str.split(SliceClientPermissions.SliceAuthority.DELIMITER);
            if (!comp[0].equals(rmCompName.getPackageName())) {
                m++;
                String pkgName = comp[0];
                String clsName = comp[0] + comp[1];
                if (m <= num) {
                    setNew[n] = new ComponentName(pkgName, clsName);
                    n++;
                }
            }
        }
        return setNew;
    }

    public static void removeActiveAdmin(PackageManagerService pms, String packageName, int userId) {
        IDevicePolicyManager dpm;
        if (!checkSystemApp(pms, packageName) && (dpm = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"))) != null) {
            Intent intent = new Intent("android.app.action.DEVICE_ADMIN_ENABLED");
            String resolveType = intent.resolveTypeIfNeeded(pms.mContext.getContentResolver());
            OppoBasePackageManagerService oppoBasePackageManagerService = (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
            List<ResolveInfo> avail = null;
            if (!(oppoBasePackageManagerService == null || oppoBasePackageManagerService.mColorPmsInner == null)) {
                avail = oppoBasePackageManagerService.mColorPmsInner.queryIntentReceiversInternal(intent, resolveType, 128, userId, false);
            }
            if (avail != null && avail.size() != 0) {
                int i = 0;
                while (i < avail.size()) {
                    try {
                        ResolveInfo ri = avail.get(i);
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
                        return;
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

    public static String setAppNativeLibraryPath(PackageParser.Package pkg, PackageSetting pkgSetting, File mAppLibInstallDir) {
        String nativeLibraryPath = new File(mAppLibInstallDir, PackageManagerService.deriveCodePathName(pkg.baseCodePath)).getPath();
        if (nativeLibraryPath == null || (pkg.applicationInfo.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0) {
            return pkgSetting.legacyNativeLibraryPathString;
        }
        return nativeLibraryPath;
    }

    private static long getAvailableDataSize() {
        StatFs mStatFs = new StatFs(Environment.getDataDirectory().getPath());
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
                inputStream.close();
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
    }

    /* JADX WARNING: Removed duplicated region for block: B:117:0x01e2 A[Catch:{ Exception -> 0x022a }, RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01e3 A[Catch:{ Exception -> 0x022a }] */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x011b A[EDGE_INSN: B:139:0x011b->B:73:0x011b ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00d6 A[Catch:{ Exception -> 0x0137 }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00e7 A[Catch:{ Exception -> 0x0137 }] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00f8 A[Catch:{ Exception -> 0x0137 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0107 A[LOOP:0: B:23:0x005e->B:69:0x0107, LOOP_END] */
    public static void filterThirdMarket(final Context context, PackageManagerService pms, int flags, List<PackageInfo> packageInfos, Handler handler) {
        boolean z;
        String str;
        String str2;
        int DELAY_TIME;
        int DELAY_TIME2 = 50;
        if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, false)) {
            boolean z2 = true;
            if (pms.hasSystemFeature(FILTRATE_APP_FEATURE_NAME, 0)) {
                sFiltrateAppSwitch = true;
            } else {
                sFiltrateAppSwitch = false;
            }
            try {
                if (sFiltrateAppSwitch) {
                    int callingUid = Binder.getCallingUid();
                    if (!UserHandle.isCore(callingUid)) {
                        initData();
                        boolean isSystem = false;
                        boolean isBlack = false;
                        boolean isWhite = false;
                        boolean isManualOpen = false;
                        if (sKnownMarketMap == null) {
                            try {
                                sKnownMarketMap = new HashMap();
                            } catch (Exception e) {
                                e = e;
                            }
                        }
                        String[] MarketName = pms.getPackagesForUid(callingUid);
                        if (MarketName.length > 0) {
                            int j = 0;
                            boolean isBadMarket = false;
                            boolean isAddNew = false;
                            while (true) {
                                if (j >= MarketName.length) {
                                    z = z2;
                                    break;
                                }
                                try {
                                    if (sKnownMarketMap.isEmpty() || !sKnownMarketMap.containsKey(MarketName[j])) {
                                        try {
                                            DELAY_TIME = DELAY_TIME2;
                                            try {
                                                PackageInfo pInfo = context.getPackageManager().getPackageInfoAsUser(MarketName[j], 0, UserHandle.myUserId());
                                                if (pInfo.applicationInfo != null) {
                                                    z = true;
                                                    if ((pInfo.applicationInfo.flags & 1) == 0) {
                                                        try {
                                                            if ((pInfo.applicationInfo.flags & 128) == 0) {
                                                                sNewMarketList.add(MarketName[j]);
                                                                isAddNew = true;
                                                            }
                                                        } catch (PackageManager.NameNotFoundException e2) {
                                                            e = e2;
                                                            try {
                                                                e.printStackTrace();
                                                                if (!sBlackList.isEmpty()) {
                                                                }
                                                                if (!sWhiteList.isEmpty()) {
                                                                }
                                                                if (!sFiltrateMarketNameList.isEmpty()) {
                                                                }
                                                                if (!isSystem && !isBlack) {
                                                                }
                                                            } catch (Exception e3) {
                                                                e = e3;
                                                                Log.e(TAG, "filterThirdMarket() Exception: " + e);
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    z = true;
                                                }
                                                isSystem = true;
                                            } catch (PackageManager.NameNotFoundException e4) {
                                                e = e4;
                                                z = true;
                                                e.printStackTrace();
                                                if (!sBlackList.isEmpty()) {
                                                }
                                                if (!sWhiteList.isEmpty()) {
                                                }
                                                if (!sFiltrateMarketNameList.isEmpty()) {
                                                }
                                                if (!isSystem && !isBlack) {
                                                }
                                            }
                                        } catch (PackageManager.NameNotFoundException e5) {
                                            e = e5;
                                            DELAY_TIME = DELAY_TIME2;
                                            z = true;
                                            e.printStackTrace();
                                            if (!sBlackList.isEmpty()) {
                                            }
                                            if (!sWhiteList.isEmpty()) {
                                            }
                                            if (!sFiltrateMarketNameList.isEmpty()) {
                                            }
                                            if (!isSystem && !isBlack) {
                                            }
                                        }
                                    } else if (STATE_OPEN.equals(sKnownMarketMap.get(MarketName[j]))) {
                                        DELAY_TIME = DELAY_TIME2;
                                        isManualOpen = true;
                                        z = true;
                                    } else {
                                        DELAY_TIME = DELAY_TIME2;
                                        z = true;
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
                                    if (!isSystem && !isBlack) {
                                        break;
                                    }
                                    j++;
                                    z2 = z;
                                    DELAY_TIME2 = DELAY_TIME;
                                } catch (Exception e6) {
                                    e = e6;
                                    Log.e(TAG, "filterThirdMarket() Exception: " + e);
                                }
                            }
                            if (!isSystem) {
                                String str3 = "";
                                if (isBlack) {
                                    packageInfos.clear();
                                    if (MarketName.length > 0) {
                                        str3 = MarketName[0];
                                    }
                                    packageInfos.add(pms.getPackageInfo(str3, flags, UserHandle.myUserId()));
                                } else if (!isWhite) {
                                    int calledPid = Binder.getCallingPid();
                                    if (sCalledPid != calledPid) {
                                        if (isManualOpen) {
                                            z = false;
                                        }
                                        if (z || isAddNew) {
                                            sCalledPid = calledPid;
                                            try {
                                                ActivityManagerNative.getDefault();
                                                final Intent intent = new Intent(MARKET_ACTION, (Uri) null);
                                                if (MarketName.length > 0) {
                                                    try {
                                                        str2 = MarketName[0];
                                                    } catch (Exception e7) {
                                                        ex = e7;
                                                        str = str3;
                                                    }
                                                } else {
                                                    str2 = null;
                                                }
                                                intent.putExtra(MARKET_EXTRA_PACKAGE_NAME, str2);
                                                intent.putExtra(MARKET_EXTRA_IS_BLACK, isBlack);
                                                intent.putExtra(MARKET_EXTRA_IS_WHITE, isWhite);
                                                intent.putExtra(MARKET_EXTRA_IS_ADDNEW, isAddNew);
                                                intent.putExtra(MARKET_EXTRA_IS_MANUALOPEN, isManualOpen);
                                                intent.putStringArrayListExtra(MARKET_EXTRA_NEW_LIST, sNewMarketList);
                                                try {
                                                    str = str3;
                                                    try {
                                                        handler.postDelayed(new Runnable() {
                                                            /* class com.android.server.pm.OppoPackageManagerHelper.AnonymousClass1 */

                                                            public void run() {
                                                                context.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                                                            }
                                                        }, 50);
                                                    } catch (Exception e8) {
                                                        ex = e8;
                                                    }
                                                } catch (Exception e9) {
                                                    ex = e9;
                                                    str = str3;
                                                    try {
                                                        Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                                        ex.printStackTrace();
                                                        if (isAddNew) {
                                                        }
                                                    } catch (Exception e10) {
                                                        e = e10;
                                                        Log.e(TAG, "filterThirdMarket() Exception: " + e);
                                                    }
                                                }
                                            } catch (Exception e11) {
                                                ex = e11;
                                                str = str3;
                                                Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                                ex.printStackTrace();
                                                if (isAddNew) {
                                                }
                                            }
                                            if (isAddNew) {
                                                if (!isManualOpen) {
                                                    packageInfos.clear();
                                                    packageInfos.add(pms.getPackageInfo(MarketName.length > 0 ? MarketName[0] : str, flags, UserHandle.myUserId()));
                                                    return;
                                                }
                                                if (isBadMarket) {
                                                    for (int i = 0; i < packageInfos.size(); i++) {
                                                        if (sFiltrateAppNameList.contains(packageInfos.get(i).packageName)) {
                                                            packageInfos.remove(i);
                                                        }
                                                    }
                                                }
                                                return;
                                            }
                                            return;
                                        }
                                    }
                                    str = str3;
                                    if (isAddNew) {
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "OppoPackageManagerHelper do not hasSystemFeature oppo.filtrated.app !!!");
                }
            } catch (Exception e12) {
                e = e12;
                Log.e(TAG, "filterThirdMarket() Exception: " + e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x01e7 A[Catch:{ Exception -> 0x0229 }, RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01e8 A[Catch:{ Exception -> 0x0229 }] */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0102 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0105 A[LOOP:0: B:23:0x005b->B:57:0x0105, LOOP_END] */
    public static void filterThirdMarketAsUser(final Context context, PackageManagerService pms, int flags, List<PackageInfo> packageInfos, Handler handler, int userId) {
        boolean z;
        boolean isAddNew;
        String str;
        String str2;
        boolean isAddNew2;
        boolean isAddNew3;
        boolean isAddNew4;
        if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, false)) {
            if (pms.hasSystemFeature(FILTRATE_APP_FEATURE_NAME, 0)) {
                sFiltrateAppSwitch = true;
            } else {
                sFiltrateAppSwitch = false;
            }
            try {
                if (sFiltrateAppSwitch) {
                    int callingUid = Binder.getCallingUid();
                    if (!UserHandle.isCore(callingUid)) {
                        initData();
                        boolean isAddNew5 = false;
                        boolean isSystem = false;
                        boolean isBlack = false;
                        boolean isWhite = false;
                        boolean isManualOpen = false;
                        boolean isBadMarket = false;
                        if (sKnownMarketMap == null) {
                            try {
                                sKnownMarketMap = new HashMap();
                            } catch (Exception e) {
                                ex = e;
                            }
                        }
                        String[] MarketName = pms.getPackagesForUid(callingUid);
                        if (MarketName.length > 0) {
                            int j = 0;
                            while (true) {
                                if (j >= MarketName.length) {
                                    z = true;
                                    isAddNew = isAddNew5;
                                    break;
                                }
                                if (!sKnownMarketMap.isEmpty()) {
                                    isAddNew4 = isAddNew5;
                                    if (sKnownMarketMap.containsKey(MarketName[j])) {
                                        if (STATE_OPEN.equals(sKnownMarketMap.get(MarketName[j]))) {
                                            isManualOpen = true;
                                            isAddNew2 = isAddNew4;
                                            z = true;
                                        } else {
                                            isAddNew2 = isAddNew4;
                                            z = true;
                                        }
                                        if (!sBlackList.isEmpty()) {
                                            isAddNew3 = isAddNew2;
                                            isBlack = sBlackList.contains(MarketName[j]);
                                        } else {
                                            isAddNew3 = isAddNew2;
                                        }
                                        if (!sWhiteList.isEmpty()) {
                                            isWhite = sWhiteList.contains(MarketName[j]);
                                        }
                                        if (!sFiltrateMarketNameList.isEmpty()) {
                                            isBadMarket = sFiltrateMarketNameList.contains(MarketName[j]);
                                        }
                                        if (isSystem || isBlack) {
                                            isAddNew = isAddNew3;
                                            break;
                                        } else {
                                            j++;
                                            isAddNew5 = isAddNew3;
                                        }
                                    }
                                } else {
                                    isAddNew4 = isAddNew5;
                                }
                                PackageInfo pInfo = pms.getPackageInfo(MarketName[j], 0, userId);
                                if (pInfo.applicationInfo != null) {
                                    z = true;
                                    if ((pInfo.applicationInfo.flags & 1) == 0 && (pInfo.applicationInfo.flags & 128) == 0) {
                                        sNewMarketList.add(MarketName[j]);
                                        isAddNew2 = true;
                                        if (!sBlackList.isEmpty()) {
                                        }
                                        if (!sWhiteList.isEmpty()) {
                                        }
                                        if (!sFiltrateMarketNameList.isEmpty()) {
                                        }
                                        if (isSystem || isBlack) {
                                        }
                                    }
                                } else {
                                    z = true;
                                }
                                isSystem = true;
                                isAddNew2 = isAddNew4;
                                if (!sBlackList.isEmpty()) {
                                }
                                if (!sWhiteList.isEmpty()) {
                                }
                                if (!sFiltrateMarketNameList.isEmpty()) {
                                }
                                if (isSystem || isBlack) {
                                }
                            }
                            if (!isSystem) {
                                String str3 = "";
                                if (isBlack) {
                                    packageInfos.clear();
                                    if (MarketName.length > 0) {
                                        str3 = MarketName[0];
                                    }
                                    packageInfos.add(pms.getPackageInfo(str3, flags, userId));
                                } else if (!isWhite) {
                                    int calledPid = Binder.getCallingPid();
                                    if (sCalledPid != calledPid) {
                                        if (isManualOpen) {
                                            z = false;
                                        }
                                        if (z || isAddNew) {
                                            try {
                                                sCalledPid = calledPid;
                                                try {
                                                    ActivityManagerNative.getDefault();
                                                    str = str3;
                                                    try {
                                                        final Intent intent = new Intent(MARKET_ACTION, (Uri) null);
                                                        if (MarketName.length > 0) {
                                                            try {
                                                                str2 = MarketName[0];
                                                            } catch (Exception e2) {
                                                                ex = e2;
                                                                try {
                                                                    Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                                                    ex.printStackTrace();
                                                                    if (!isAddNew) {
                                                                    }
                                                                } catch (Exception e3) {
                                                                    ex = e3;
                                                                    Log.e(TAG, "filterThirdMarket() Exception: " + ex);
                                                                }
                                                            }
                                                        } else {
                                                            str2 = null;
                                                        }
                                                        intent.putExtra(MARKET_EXTRA_PACKAGE_NAME, str2);
                                                        intent.putExtra(MARKET_EXTRA_IS_BLACK, isBlack);
                                                        intent.putExtra(MARKET_EXTRA_IS_WHITE, isWhite);
                                                        intent.putExtra(MARKET_EXTRA_IS_ADDNEW, isAddNew);
                                                        intent.putExtra(MARKET_EXTRA_IS_MANUALOPEN, isManualOpen);
                                                        intent.putStringArrayListExtra(MARKET_EXTRA_NEW_LIST, sNewMarketList);
                                                        try {
                                                            handler.postDelayed(new Runnable() {
                                                                /* class com.android.server.pm.OppoPackageManagerHelper.AnonymousClass2 */

                                                                public void run() {
                                                                    context.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                                                                }
                                                            }, 50);
                                                        } catch (Exception e4) {
                                                            ex = e4;
                                                        }
                                                    } catch (Exception e5) {
                                                        ex = e5;
                                                        Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                                        ex.printStackTrace();
                                                        if (!isAddNew) {
                                                        }
                                                    }
                                                } catch (Exception e6) {
                                                    ex = e6;
                                                    str = str3;
                                                    Log.e(TAG, "sendBroadcastMarketFilted() Exception: " + ex);
                                                    ex.printStackTrace();
                                                    if (!isAddNew) {
                                                    }
                                                }
                                                if (!isAddNew) {
                                                    if (!isManualOpen) {
                                                        packageInfos.clear();
                                                        packageInfos.add(pms.getPackageInfo(MarketName.length > 0 ? MarketName[0] : str, flags, userId));
                                                        return;
                                                    }
                                                    if (isBadMarket) {
                                                        for (int i = 0; i < packageInfos.size(); i++) {
                                                            if (sFiltrateAppNameList.contains(packageInfos.get(i).packageName)) {
                                                                packageInfos.remove(i);
                                                            }
                                                        }
                                                    }
                                                    return;
                                                }
                                                return;
                                            } catch (Exception e7) {
                                                ex = e7;
                                                Log.e(TAG, "filterThirdMarket() Exception: " + ex);
                                            }
                                        }
                                    }
                                    str = str3;
                                    if (!isAddNew) {
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "OppoPackageManagerHelper do not hasSystemFeature oppo.filtrated.app !!!");
                }
            } catch (Exception e8) {
                ex = e8;
                Log.e(TAG, "filterThirdMarket() Exception: " + ex);
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
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getKnownMarketMap(), Exception: " + e);
            e.printStackTrace();
        } catch (Throwable th) {
            sMarketLock.unlock();
            throw th;
        }
        sMarketLock.unlock();
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
            outputStream.close();
        } catch (Exception e2) {
            Log.e(TAG, "setKnownMarketMap(), Exception: " + e2);
            e2.printStackTrace();
        } catch (Throwable th) {
            sMarketLock.unlock();
            throw th;
        }
        sMarketLock.unlock();
    }

    private static Map<String, String> readMarketsFromXML(FileInputStream stream) {
        int type;
        Map<String, String> marketMap = new HashMap<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            do {
                type = parser.next();
                if (type == 2 && "market".equals(parser.getName())) {
                    String name = null;
                    String text = null;
                    try {
                        name = parser.getAttributeValue(null, "packagename");
                        text = parser.nextText();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!(name == null || text == null)) {
                        marketMap.put(name, text);
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
            out.startDocument(null, true);
            out.text("\r\n");
            out.startTag(null, "marketlist");
            for (String name : marketMap.keySet()) {
                out.text("\r\n");
                out.text("\t");
                out.startTag(null, "market");
                out.attribute(null, "packagename", name);
                out.text(marketMap.get(name));
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
