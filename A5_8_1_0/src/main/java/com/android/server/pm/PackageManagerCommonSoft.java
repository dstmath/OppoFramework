package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.telephony.OppoTelephonyFunction;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.LocationManagerService;
import com.android.server.engineer.OppoEngineerNative;
import com.android.server.pm.Installer.InstallerException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PackageManagerCommonSoft {
    private static final ArrayList<String> COPY_APP_PATH = new ArrayList();
    private static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    static final String TAG = "PackageManagerCommonSoft";
    private static final String mAppChannelDir = "/data/etc/appchannel";
    private static ArrayList<String> mAppChannelNameList = new ArrayList();
    private static final File mBlackMediaResourceFile = new File("/data/engineermode/MediaResourceConfig/blacklist.txt");
    private static ArrayList<String> mBlackMediaResourceList = new ArrayList();
    private static final String mDataAppDir = "/data/app";
    private static final String mDataEngineermodeDir = "/data/engineermode";
    private static ArrayList<String> mDataPackageNameList = new ArrayList();
    private static ArrayList<String> mDataPathNameList = new ArrayList();
    private static final String mDataReserveDir = "/data/reserve";
    private static ArrayList<String> mGboardInputMethodList = new ArrayList();
    private static final String mGboardResourceDir = "/data/etc/GBoard";
    private static Installer mInstaller = null;
    private static final String mMediaResourceConfigFileName = "/data/engineermode/MediaResourceConfig/config.xml";
    private static final String mNetLock = SystemProperties.get("ro.oppo.region.netlock", "");
    private static final String mOperator = SystemProperties.get("ro.oppo.operator", "");
    private static boolean mOppoDeriveAbiNeed = false;
    private static PackageManagerService mPms = null;
    private static final String mProject = SystemProperties.get("ro.separate.soft", "");
    private static final String mRegionMark = SystemProperties.get("ro.oppo.regionmark", "");
    private static ArrayList<String> mSystemAppNameList = new ArrayList();
    private static ArrayList<String> mSystemAppPathList = new ArrayList();
    private static final String mSystemBlacklistDir = "/system/etc/blacklist";

    enum FileMatchCategory {
        MISMATCH,
        MATCH,
        MUST_BE_REMOVED
    }

    static {
        COPY_APP_PATH.add("/system/reserve/");
        COPY_APP_PATH.add("/data/reserve/");
        COPY_APP_PATH.add("/system/data_app_" + mOperator + "/");
        COPY_APP_PATH.add("/system/data_app_" + mNetLock + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mOperator + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mNetLock + "/");
        COPY_APP_PATH.add("/data/reserve/data_app_" + mRegionMark + "/");
    }

    static void commonSoftInit(Installer installer, PackageManagerService pms) {
        Slog.d(TAG, "commonSoftInit");
        mInstaller = installer;
        mPms = pms;
        mSystemAppNameList.addAll(getNameListFromPathFile(mSystemBlacklistDir, "systemapp"));
        mSystemAppPathList.addAll(getNameListFromPathFile(mSystemBlacklistDir, "systempath"));
        mDataPathNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "persistpath"));
        mDataPackageNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "persistname"));
        mAppChannelNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "appchannel"));
        mGboardInputMethodList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "gboard"));
        printArrayListInfo(mSystemAppNameList, "systemapp");
        printArrayListInfo(mSystemAppPathList, "systempath");
        printArrayListInfo(mDataPackageNameList, "persistname");
        printArrayListInfo(mDataPathNameList, "persistpath");
        printArrayListInfo(mAppChannelNameList, "appchannel");
        printArrayListInfo(mGboardInputMethodList, "gboard");
    }

    static void resetOppoDeriveAbi() {
        mOppoDeriveAbiNeed = false;
    }

    static void enableOppoDeriveAbi() {
        mOppoDeriveAbiNeed = true;
    }

    static boolean getOppoDeriveAbiStatus() {
        return mOppoDeriveAbiNeed;
    }

    static boolean ShouldOppoDerivePackageAbi(String pkgName) {
        if (!getOppoDeriveAbiStatus() || pkgName == null || !isDataAppNameInWhiteList(pkgName)) {
            return false;
        }
        Slog.d(TAG, pkgName + " ShouldOppoDerivePackageAbi!");
        return true;
    }

    static FileMatchCategory isFileNameMatchDevice(String filename) {
        String project = "";
        String region = "";
        String operator = "";
        FileMatchCategory projectMatchType = FileMatchCategory.MISMATCH;
        FileMatchCategory regionMatchType = FileMatchCategory.MISMATCH;
        FileMatchCategory operatorMatchType = FileMatchCategory.MISMATCH;
        String filenameSubString = filename;
        int startTagIndex = filename.indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        if (startTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        filenameSubString = filename.substring(startTagIndex + 1, filename.length());
        int endTagIndex = filenameSubString.indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        if (endTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        project = filenameSubString.substring(0, endTagIndex);
        startTagIndex = filenameSubString.indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        if (startTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        filenameSubString = filenameSubString.substring(startTagIndex + 1, filenameSubString.length());
        endTagIndex = filenameSubString.indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        if (endTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        region = filenameSubString.substring(0, endTagIndex);
        startTagIndex = filenameSubString.indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        if (startTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        filenameSubString = filenameSubString.substring(startTagIndex + 1, filenameSubString.length());
        endTagIndex = filenameSubString.indexOf(".");
        if (endTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        operator = filenameSubString.substring(0, endTagIndex);
        if ("common".equals(project) || mProject.equals(project) || (project.startsWith("-") && (project.substring(project.indexOf("-") + 1, project.length()).equals(mProject) ^ 1) != 0)) {
            projectMatchType = FileMatchCategory.MATCH;
        }
        if (project.startsWith("-") && project.substring(project.indexOf("-") + 1, project.length()).equals(mProject)) {
            projectMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if ("common".equals(region) || mRegionMark.equals(region) || (region.startsWith("-") && (region.substring(region.indexOf("-") + 1, region.length()).equals(mRegionMark) ^ 1) != 0)) {
            regionMatchType = FileMatchCategory.MATCH;
        }
        if (region.startsWith("-") && region.substring(region.indexOf("-") + 1, region.length()).equals(mRegionMark)) {
            regionMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if ("common".equals(operator) || mOperator.equals(operator) || (operator.startsWith("-") && (operator.substring(operator.indexOf("-") + 1, operator.length()).equals(mOperator) ^ 1) != 0)) {
            operatorMatchType = FileMatchCategory.MATCH;
        }
        if (operator.startsWith("-") && operator.substring(operator.indexOf("-") + 1, operator.length()).equals(mOperator)) {
            operatorMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if (projectMatchType == FileMatchCategory.MATCH && regionMatchType == FileMatchCategory.MATCH && operatorMatchType == FileMatchCategory.MATCH) {
            return FileMatchCategory.MATCH;
        }
        if (projectMatchType == FileMatchCategory.MUST_BE_REMOVED || regionMatchType == FileMatchCategory.MUST_BE_REMOVED || operatorMatchType == FileMatchCategory.MUST_BE_REMOVED) {
            return FileMatchCategory.MUST_BE_REMOVED;
        }
        return FileMatchCategory.MISMATCH;
    }

    static Set<String> getNameListFromPathFile(String path, String fileStartTag) {
        File fileDir = new File(path);
        File[] files = fileDir.listFiles();
        Set<String> addSet = new HashSet();
        Set<String> removeSet = new HashSet();
        if (ArrayUtils.isEmpty(files)) {
            Slog.d(TAG, "No files in dir:" + fileDir);
            return addSet;
        }
        for (File file : files) {
            if (file.getName().startsWith(fileStartTag)) {
                FileMatchCategory matchType = isFileNameMatchDevice(file.getName());
                if (matchType == FileMatchCategory.MATCH) {
                    Slog.d(TAG, "load " + fileStartTag + " from file=" + file);
                    addSet.addAll(getNameListFromFile(file));
                }
                if (matchType == FileMatchCategory.MUST_BE_REMOVED) {
                    Slog.d(TAG, "load " + fileStartTag + " from remove file=" + file);
                    removeSet.addAll(getNameListFromFile(file));
                }
            }
        }
        for (Object obj : removeSet) {
            if (addSet.contains(obj)) {
                addSet.remove(obj);
            }
        }
        return addSet;
    }

    static ArrayList<String> getNameListFromFile(File file) {
        IOException e;
        Throwable th;
        ArrayList<String> list = new ArrayList();
        if (!file.exists()) {
            return list;
        }
        AutoCloseable in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(file));
            while (true) {
                try {
                    String line = in2.readLine();
                    if (line == null) {
                        break;
                    }
                    list.add(line);
                } catch (IOException e2) {
                    e = e2;
                    in = in2;
                    try {
                        Slog.e(TAG, "readApkWhiteList IOException " + e);
                        IoUtils.closeQuietly(in);
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(in);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Object in3 = in2;
                    IoUtils.closeQuietly(in);
                    throw th;
                }
            }
            IoUtils.closeQuietly(in2);
        } catch (IOException e3) {
            e = e3;
            Slog.e(TAG, "readApkWhiteList IOException " + e);
            IoUtils.closeQuietly(in);
            return list;
        }
        return list;
    }

    static void writeNameListToFile(ArrayList<String> list, File file) {
        IOException e;
        Throwable th;
        if (file.exists()) {
            file.delete();
        }
        AutoCloseable out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new FileWriter(file));
            try {
                for (String line : list) {
                    out2.write(line);
                    out2.newLine();
                }
                IoUtils.closeQuietly(out2);
            } catch (IOException e2) {
                e = e2;
                out = out2;
                try {
                    Slog.e(TAG, "writeNameListToFile IOException " + e);
                    IoUtils.closeQuietly(out);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(out);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Object out3 = out2;
                IoUtils.closeQuietly(out);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Slog.e(TAG, "writeNameListToFile IOException " + e);
            IoUtils.closeQuietly(out);
        }
    }

    static void printArrayListInfo(ArrayList<String> list, String name) {
        if (list.size() > 0) {
            for (String signlelist : list) {
                Slog.d(TAG, "name=" + name + ",signlelist=" + signlelist);
            }
        }
    }

    static ArraySet<String> getDataPackageNameList() {
        ArraySet<String> result = new ArraySet();
        result.addAll(mDataPackageNameList);
        return result;
    }

    static boolean isDisableSoftsimPackage(String packageName) {
        if (EXP_VERSION) {
            return (packageName.equals("com.redteamobile.roaming") || packageName.equals("com.redteamobile.roaming.deamon")) && (!mPms.hasSystemFeature("oppo.softsim.exp.support", 0) || OppoTelephonyFunction.colorIsSimLockedEnabledTH() || OppoTelephonyFunction.colorIsSimLockedEnabled());
        } else {
            return false;
        }
    }

    static boolean isSystemAppNameInBlackList(String packageName) {
        if (mSystemAppNameList.contains(packageName) || isDisableSoftsimPackage(packageName)) {
            return true;
        }
        return false;
    }

    static boolean isSystemAppPathInBlackList(String appPath) {
        if (mSystemAppPathList.size() != 0 && mSystemAppPathList.contains(appPath)) {
            return true;
        }
        return false;
    }

    static boolean isDataAppNameInWhiteList(String packageName) {
        if (mDataPackageNameList.size() != 0 && mDataPackageNameList.contains(packageName)) {
            return true;
        }
        return false;
    }

    static void deleteDataFileForDifferentDevice() {
        if (isBootFromMasterClear()) {
            deleteDataAppForMasterClear();
        } else {
            deleteDataAppForFirstBoot();
        }
        if (isInternetDownload() || isBootFromAdbClear()) {
            if (!(isOperatorNormalize() || (mRegionMark.equals("TW") ^ 1) == 0)) {
                deleteDataReserveAppsForAdbClear();
                deleteAppChannelForAdbClear();
            }
            deleteMediaResourceForAdbClear();
            deleteGboardResourceForMasterClear();
            SystemProperties.set("persist.sys.oppo.fromadbclear", "false");
        }
    }

    static boolean isBootFromMasterClear() {
        File packagexmlFile = new File(mDataAppDir, "packages.xml");
        if (packagexmlFile == null || !packagexmlFile.exists()) {
            return false;
        }
        return true;
    }

    static boolean isBootFromAdbClear() {
        return SystemProperties.get("persist.sys.oppo.fromadbclear", "false").equalsIgnoreCase("true");
    }

    static boolean isOperatorNormalize() {
        return SystemProperties.get("ro.oppo.operator_normalize", "false").equalsIgnoreCase("true");
    }

    static boolean isTochpalPackage(String packagename) {
        if (packagename.contains("com.cootek.smartinputv5") || packagename.contains("com.emoji.keyboard.touchpal")) {
            return true;
        }
        return false;
    }

    static String getDownloadStatusInternal() {
        byte[] status = OppoEngineerNative.native_getDownloadStatus();
        if (status == null || status.length <= 0) {
            return null;
        }
        return new String(status, StandardCharsets.UTF_8);
    }

    static boolean isInternetDownload() {
        String downloadString = getDownloadStatusInternal();
        if (downloadString != null && downloadString.contains("Status:internet")) {
            return true;
        }
        return false;
    }

    static void deleteDataAppForFirstBoot() {
        File dataAppDir = new File(mDataAppDir);
        if (dataAppDir.exists() && mDataPathNameList.size() > 0) {
            File[] files = dataAppDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + dataAppDir);
                return;
            }
            for (File apkFile : files) {
                if (!mDataPathNameList.contains("data/app/" + apkFile.getName())) {
                    removeCodePath(apkFile);
                }
            }
        }
    }

    static boolean isMasterClearFilterApp(String pkgName) {
        String[] filterPackages = new String[]{"com.coloros.accegamesdk"};
        for (String equals : filterPackages) {
            if (equals.equals(pkgName)) {
                Slog.d(TAG, "MasterClear Filter pkgName=" + pkgName);
                return true;
            }
        }
        return false;
    }

    static void deleteDataAppForMasterClear() {
        File dataAppDir = new File(mDataAppDir);
        if (dataAppDir.exists()) {
            File[] files = dataAppDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + dataAppDir);
                return;
            }
            for (File apkFile : files) {
                if (!apkFile.getName().startsWith("packages.xml")) {
                    String pkgName = getPkgName(apkFile);
                    if (!(pkgName == null || (mDataPackageNameList.contains(pkgName) ^ 1) == 0 || (isMasterClearFilterApp(pkgName) ^ 1) == 0)) {
                        removeCodePath(apkFile);
                    }
                }
            }
        }
    }

    static void deleteAppChannelForAdbClear() {
        File appChannelDir = new File(mAppChannelDir);
        if (appChannelDir.exists() && mAppChannelNameList.size() > 0) {
            File[] files = appChannelDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + appChannelDir);
                return;
            }
            for (File file : files) {
                if (!mAppChannelNameList.contains(file.getName())) {
                    deleteFiles(file);
                }
            }
        }
    }

    static void removeCodePath(File codePath) {
        if (codePath.isDirectory()) {
            try {
                mInstaller.rmPackageDir(codePath.getAbsolutePath());
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to remove code path", e);
            }
        } else {
            codePath.delete();
        }
        Slog.d(TAG, "delete file " + codePath);
    }

    static void deleteGboardResourceForMasterClear() {
        File gboardResourceDir = new File(mGboardResourceDir);
        if (gboardResourceDir.exists() && mGboardInputMethodList.size() > 0) {
            File[] files = gboardResourceDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + gboardResourceDir);
                return;
            }
            for (File file : files) {
                if (!mGboardInputMethodList.contains(file.getName())) {
                    deleteFiles(file);
                }
            }
        }
    }

    static void deleteDataReserveAppsForAdbClear() {
        File dataReserveDir = new File(mDataReserveDir);
        if (dataReserveDir.exists() && mDataPackageNameList.size() > 0) {
            File[] files = dataReserveDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + dataReserveDir);
                return;
            }
            for (File apkFile : files) {
                String pkgName = getPkgName(apkFile);
                if (!(pkgName == null || (mDataPackageNameList.contains(pkgName) ^ 1) == 0)) {
                    deleteFiles(apkFile);
                }
            }
        }
    }

    static void CopyDataReserveAppsForSimOperatorSwitch() {
        int i = 0;
        boolean mOPInstalled = SystemProperties.getBoolean("persist.sys.oppo.opinstalled", false);
        boolean mSimOperatorSwitchSupport = SystemProperties.getBoolean("ro.oppo.sim_operator_switch", false);
        if (mOPInstalled || (mSimOperatorSwitchSupport ^ 1) != 0) {
            Slog.d(TAG, "skip CopyDataReserveAppsForSimSwitch for :mOPInstalled = " + mOPInstalled + " mSimOperatorSwitchSupport = " + mSimOperatorSwitchSupport);
            return;
        }
        Slog.d(TAG, "start CopyDataReserveAppsForSimOperatorSwitch");
        File dataReserveDir = new File(mDataReserveDir);
        if (dataReserveDir.exists() && mDataPackageNameList.size() > 0) {
            File[] files = dataReserveDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir" + dataReserveDir);
                return;
            }
            int length = files.length;
            while (i < length) {
                File apkFile = files[i];
                String pkgName = getPkgName(apkFile);
                if (pkgName != null && mDataPackageNameList.contains(pkgName)) {
                    if (isPackageExists(pkgName)) {
                        Slog.i(TAG, "CopyDataReserveAppsForSimOperatorSwitch apk:" + pkgName + " has been installed, skip");
                    } else {
                        File packageDir = new File(mDataAppDir, pkgName);
                        if (!packageDir.exists()) {
                            packageDir.mkdir();
                        }
                        FileUtils.setPermissions(packageDir.getPath(), 505, -1, -1);
                        File destFile = new File(packageDir, apkFile.getName());
                        Slog.i(TAG, " CopyDataReserveAppsForSimOperatorSwitch apk:" + pkgName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                        FileUtils.copyFile(apkFile, destFile);
                        FileUtils.setPermissions(destFile.getPath(), 420, -1, -1);
                    }
                }
                i++;
            }
        }
        if (!mRegionMark.equals("TW")) {
            deleteDataReserveAppsForAdbClear();
            deleteAppChannelForAdbClear();
        }
        SystemProperties.set("persist.sys.oppo.opinstalled", "true");
        enableOppoDeriveAbi();
    }

    public static void copyReserveApk() {
        for (String appCopyPath : COPY_APP_PATH) {
            File oppoReserveApkPath = new File(appCopyPath);
            if (oppoReserveApkPath.exists() && oppoReserveApkPath.listFiles() != null) {
                for (File apkFile : oppoReserveApkPath.listFiles()) {
                    String packageName = parsePkg(apkFile);
                    String apkFileName = apkFile.getName();
                    if (apkFileName.contains("-")) {
                        int startIndex = apkFileName.indexOf("-");
                        int fileNameLength = apkFileName.length();
                        if (startIndex != -1 && fileNameLength > startIndex + 4) {
                            if (!mProject.equals(apkFileName.substring(startIndex + 1, fileNameLength - 4))) {
                                Slog.i(TAG, "ignore reserve file=" + apkFileName);
                            }
                        }
                    }
                    if (packageName == null) {
                        Slog.i(TAG, "reserve package null, error!!!");
                    } else if (isPackageExists(packageName)) {
                        Slog.i(TAG, "apk:" + packageName + " has been installed, skip it!");
                    } else {
                        File packageDir = new File(mDataAppDir, packageName);
                        if (!packageDir.exists()) {
                            packageDir.mkdir();
                        }
                        FileUtils.setPermissions(packageDir.getPath(), 505, -1, -1);
                        File destFile = new File(packageDir, apkFile.getName());
                        Slog.i(TAG, "apk:" + packageName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                        FileUtils.copyFile(apkFile, destFile);
                        FileUtils.setPermissions(destFile.getPath(), 420, -1, -1);
                    }
                }
            }
        }
    }

    public static void deleteMediaResourceForAdbClear() {
        Slog.i(TAG, "delete MediaResource For AdbClear!");
        parseMediaResourceXml();
        if (mBlackMediaResourceList.size() > 0) {
            writeNameListToFile(mBlackMediaResourceList, mBlackMediaResourceFile);
            SystemProperties.set("ro.oppo.reconcile_media_resource", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:92:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0076 A:{SYNTHETIC, Splitter: B:21:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:93:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0165 A:{SYNTHETIC, Splitter: B:63:0x0165} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0187 A:{SYNTHETIC, Splitter: B:77:0x0187} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void parseMediaResourceXml() {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        InputStream input = null;
        try {
            InputStream input2 = new FileInputStream(new File(mMediaResourceConfigFileName));
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(input2, "UTF-8");
                while (parser.getEventType() != 1) {
                    if (parser.getEventType() == 2 && parser.getName().equals("black-resource")) {
                        boolean isProjectHit = false;
                        boolean isCountryHit = false;
                        boolean isOperatorHit = false;
                        String project = parser.getAttributeValue(null, "project");
                        String country = parser.getAttributeValue(null, "country");
                        String operator = parser.getAttributeValue(null, "operator");
                        String path = parser.getAttributeValue(null, "path");
                        if (project == null || country == null || operator == null || path == null) {
                            Slog.e(TAG, "mBlackMediaResourceList xml may config wrong in this line!");
                            parser.next();
                        } else {
                            if (project.equals(mProject) || project.equals("common")) {
                                isProjectHit = true;
                            }
                            if (country.equals(mRegionMark) || country.equals("common")) {
                                isCountryHit = true;
                            }
                            if (operator.equals(mOperator) || operator.equals("common")) {
                                isOperatorHit = true;
                            }
                            if ((mOperator.isEmpty() && operator.equals("EX")) || (!mOperator.isEmpty() && operator.equals("ALLOPERATOR"))) {
                                isOperatorHit = true;
                            }
                            Slog.i(TAG, "parseMediaResourceXml project = " + project + ",country = " + country + ",operator = " + operator + ",path = " + path + ",isProjectHit = " + isProjectHit + ",isCountryHit = " + isCountryHit + ",isOperatorHit = " + isOperatorHit);
                            if (isProjectHit && isCountryHit && isOperatorHit) {
                                Slog.i(TAG, "mBlackMediaResourceList add " + path);
                                mBlackMediaResourceList.add(path);
                            }
                        }
                    }
                    parser.next();
                }
                if (input2 != null) {
                    try {
                        input2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (XmlPullParserException e4) {
                e2 = e4;
                input = input2;
                try {
                    e2.printStackTrace();
                    if (input == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e32 = e5;
                input = input2;
                e32.printStackTrace();
                if (input == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                input = input2;
                if (input != null) {
                }
                throw th;
            }
        } catch (XmlPullParserException e6) {
            e2 = e6;
            e2.printStackTrace();
            if (input == null) {
                try {
                    input.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (input == null) {
                try {
                    input.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
        }
    }

    public static boolean isPackageExists(String pkgName) {
        File dataAppDir = new File(mDataAppDir);
        if (!dataAppDir.exists()) {
            return false;
        }
        File[] files = dataAppDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Slog.d(TAG, "No files in app dir " + dataAppDir);
            return false;
        }
        for (File apkFile : files) {
            if (apkFile.getName().startsWith(pkgName)) {
                return true;
            }
        }
        return false;
    }

    static void deleteFiles(File file) {
        if (file.getName().startsWith("com.cootek.smartinputv5.language") && mNetLock.equals("") && mRegionMark.equals("")) {
            Slog.d(TAG, "ignore file" + file);
            return;
        }
        if (file.exists()) {
            if (file.isFile()) {
                Slog.d(TAG, "delete file : " + file);
                file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File deleteFiles : files) {
                    deleteFiles(deleteFiles);
                }
                Slog.d(TAG, "delete dir : " + file);
                file.delete();
            }
        }
    }

    static String getPkgName(File file) {
        if (file.isDirectory()) {
            for (File apkFile : file.listFiles()) {
                if (apkFile.getName().endsWith(".apk")) {
                    return parsePkg(file);
                }
            }
        } else if (file.getName().endsWith(".apk")) {
            return parsePkg(file);
        }
        return null;
    }

    static String parsePkg(File file) {
        try {
            PackageLite pkg = PackageParser.parsePackageLite(file, 2);
            if (pkg != null) {
                return pkg.packageName;
            }
        } catch (PackageParserException ex) {
            Slog.e(TAG, "parser pkg fail !!!" + ex);
        }
        return null;
    }
}
