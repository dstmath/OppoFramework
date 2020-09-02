package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.Environment;
import android.os.FileUtils;
import android.os.OppoBaseEnvironment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.SystemConfig;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.Installer;
import com.android.server.slice.SliceClientPermissions;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PackageManagerCommonSoft {
    private static final int ALLOW_ALL = -1;
    private static final String CLS_SYSTEMCONFIG = "com.android.server.SystemConfig";
    private static final ArrayList<String> COPY_APP_PATH = new ArrayList<>();
    private static final String CUSTOMDELAPPDIR = (DIR_CUSTOM_ROOT.getAbsolutePath() + "/del-app");
    private static final File DIR_CUSTOM_ROOT = OppoBaseEnvironment.getResourceDirectory();
    private static final String ENV_CUSTOM_ROOT = "CUSTOM_ROOT";
    private static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String LABANKEY = "com.vng.inputmethod.labankey";
    private static final String LABANKEY_APK = "com.vng.inputmethod.labankey.apk";
    private static final String METHOD_READPERMISSIONS = "readPermissions";
    private static final ArrayList<String> RESERVE_DELETE = new ArrayList<>();
    private static final File RESERVE_DELETE_FILE = new File("/data/etc/recovery/reserve_delete_apps.txt");
    static final String TAG = "PackageManagerCommonSoft";
    private static final File custom_etc = new File("" + OppoBaseEnvironment.getResourceDirectory().getAbsolutePath() + "/etc/permissions/");
    private static final String dataAppXmlPath = "data/engineermode/data_app_xml";
    private static final String data_flag = "white";
    private static final File engineer_etc = new File(OppoBaseEnvironment.getOppoEngineerDirectory().getAbsolutePath() + "/etc/permissions/");
    private static final String mAfterSaleRegion = SystemProperties.get("ro.oppo.aftersale.region", "");
    private static ArrayList<String> mAfterSaleSystemAppNameList = new ArrayList<>();
    private static final String mAppChannelDir = "/data/etc/appchannel";
    private static ArrayList<String> mAppChannelNameList = new ArrayList<>();
    private static final File mAppFlyerFile = new File(mAppFlyerPath);
    private static final String mAppFlyerPath = SystemProperties.get("ro.appsflyer.preinstall.path", "");
    private static final File mBlackMediaResourceFile = new File("/data/engineermode/MediaResourceConfig/blacklist.txt");
    private static ArrayList<String> mBlackMediaResourceList = new ArrayList<>();
    private static ArrayList<String> mCanUninstallApksList = new ArrayList<>();
    private static final File mCustCanUninstllApksFile = new File("/data/etc/recovery/can_uninstall_apks.txt");
    private static final String mDataAppDir = "/data/app";
    private static final String mDataEngineermodeDir = "/data/engineermode";
    private static ArrayList<String> mDataPackageNameList = new ArrayList<>();
    private static ArrayList<String> mDataPathNameList = new ArrayList<>();
    private static final String mDataReserveDir = "/data/reserve";
    private static final String mDataSpecialPreloadDir = "/data/format_unclear/special_preload";
    private static final String mEuexCountry = SystemProperties.get("ro.oppo.euex.country", "");
    private static ArrayList<String> mGboardInputMethodList = new ArrayList<>();
    private static final String mGboardResourceDir = "/data/etc/GBoard";
    private static Installer mInstaller = null;
    private static boolean mIsAfterSaleRegionDevice = false;
    private static final boolean mIsBootFromSimOperatorSwitch;
    private static boolean mIsSupportRSA3 = false;
    private static String mLightOS = TemperatureProvider.SWITCH_OFF;
    private static final String mMediaResourceConfigFileName = "/data/engineermode/MediaResourceConfig/config.xml";
    private static final String mNetLock = SystemProperties.get("ro.oppo.region.netlock", "");
    private static final String mOperator = SystemProperties.get("ro.oppo.operator", "");
    private static final String mOperatorCN = SystemProperties.get("ro.rom.featrue", "");
    private static ArrayList<String> mOperatorReserveWhiteList = new ArrayList<>();
    private static boolean mOppoDeriveAbiNeed = false;
    private static final File mPackageChannelFile = new File("/data/engineermode/package_channel.txt");
    private static HashMap<String, String> mPackageChannelHashMap = new HashMap<>();
    private static PackageManagerService mPms = null;
    private static final String mProject = SystemProperties.get("ro.separate.soft", "");
    private static final String mRegionMark = SystemProperties.get("ro.oppo.regionmark", "");
    private static HashMap<String, String> mReserveHashMap = new HashMap<>();
    private static ArrayList<String> mReserveWhiteList = new ArrayList<>();
    private static final String mSimOperatorProp = SystemProperties.get("persist.sys.oppo_opta", "");
    private static ArrayList<String> mSystemAppNameList = new ArrayList<>();
    private static ArrayList<String> mSystemAppPathList = new ArrayList<>();
    private static final String mSystemBlacklistDir = "/system/etc/blacklist";
    private static final String mWhiteRecoveryDir = "/data/etc/recovery";
    private static final File mWhiteRecoveryNameFile = new File("/data/etc/recovery/whitelist_recovery_name");
    private static final File mWhiteRecoveryPathFile = new File("/data/etc/recovery/whitelist_recovery_path");
    private static HashMap<String, String> mWhiteSamePackageHashMap = new HashMap<>();
    private static final File product_etc = new File(OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/etc/permissions/");
    private static final String systemAppXmlPath = "system/etc/blacklist/system_app_xml";
    private static final String system_flag = "black";
    private static final File version_etc = new File(OppoBaseEnvironment.getOppoVersionDirectory().getAbsolutePath() + "/etc/permissions/");
    private static final File zalo_channel = new File("/data/etc/appchannel/zalo_appchannel.in");

    enum FileMatchCategory {
        MISMATCH,
        MATCH,
        MUST_BE_REMOVED
    }

    static {
        boolean z = false;
        if (!SystemProperties.getBoolean("persist.sys.oppo.opinstalled", false) && SystemProperties.getBoolean("ro.oppo.sim_operator_switch", false)) {
            z = true;
        }
        mIsBootFromSimOperatorSwitch = z;
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

    static void commonSoftInit(Installer installer, PackageManagerService pms) {
        Slog.d(TAG, "commonSoftInit");
        mInstaller = installer;
        mPms = pms;
        mIsAfterSaleRegionDevice = TextUtils.isEmpty(mRegionMark) && !TextUtils.isEmpty(mAfterSaleRegion);
        mIsSupportRSA3 = SystemProperties.get("ro.oppo.rsa3.support", TemperatureProvider.SWITCH_OFF).equalsIgnoreCase(TemperatureProvider.SWITCH_ON);
        initPackageChannelHashMap();
        mSystemAppNameList.addAll(getNameListFromPathFile(mSystemBlacklistDir, "systemapp", false));
        mSystemAppPathList.addAll(getNameListFromPathFile(mSystemBlacklistDir, "systempath", false));
        mDataPathNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "persistpath", false));
        mDataPackageNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "persistname", false));
        if (mPms.hasSystemFeature("oppo.sys.light.func", 0)) {
            mLightOS = TemperatureProvider.SWITCH_ON;
        }
        PackageManagerXmlParse dataXmlParse = new PackageManagerXmlParse(dataAppXmlPath, data_flag, mProject, mLightOS, mRegionMark, mEuexCountry, mOperator);
        for (int it = 0; it < dataXmlParse.mAddList.size(); it++) {
            if (!mDataPackageNameList.contains(dataXmlParse.mAddList.get(it).getPackage()) && dataXmlParse.mAddList.get(it).getPackage() != null) {
                mDataPackageNameList.add(dataXmlParse.mAddList.get(it).getPackage());
            }
            if (!mDataPathNameList.contains(dataXmlParse.mAddList.get(it).getPathName()) && dataXmlParse.mAddList.get(it).getPathName() != null) {
                mDataPathNameList.add(dataXmlParse.mAddList.get(it).getPathName());
            }
        }
        for (int it2 = 0; it2 < dataXmlParse.mDeleteList.size(); it2++) {
            if (mDataPackageNameList.contains(dataXmlParse.mDeleteList.get(it2).getPackage()) && dataXmlParse.mDeleteList.get(it2).getPackage() != null) {
                Slog.d(TAG, "that is fail, " + dataXmlParse.mDeleteList.get(it2).getPackage() + "should not be contains in white list");
                mDataPackageNameList.remove(dataXmlParse.mDeleteList.get(it2).getPackage());
            }
            if (mDataPathNameList.contains(dataXmlParse.mDeleteList.get(it2).getPathName()) && dataXmlParse.mDeleteList.get(it2).getPathName() != null) {
                Slog.d(TAG, "that is fail, " + dataXmlParse.mDeleteList.get(it2).getPathName() + "should not be contains in white list");
                mDataPathNameList.remove(dataXmlParse.mDeleteList.get(it2).getPathName());
            }
        }
        PackageManagerXmlParse systemXmlParse = new PackageManagerXmlParse(systemAppXmlPath, system_flag, mProject, mLightOS, mRegionMark, mEuexCountry, mOperator);
        for (int it3 = 0; it3 < systemXmlParse.mAddList.size(); it3++) {
            if (!mSystemAppNameList.contains(systemXmlParse.mAddList.get(it3).getPackage()) && systemXmlParse.mAddList.get(it3).getPackage() != null) {
                mSystemAppNameList.add(systemXmlParse.mAddList.get(it3).getPackage());
            }
            if (!mSystemAppPathList.contains(systemXmlParse.mAddList.get(it3).getPathName()) && systemXmlParse.mAddList.get(it3).getPathName() != null) {
                mSystemAppPathList.add(systemXmlParse.mAddList.get(it3).getPathName());
            }
        }
        for (int it4 = 0; it4 < systemXmlParse.mDeleteList.size(); it4++) {
            if (mSystemAppNameList.contains(systemXmlParse.mDeleteList.get(it4).getPackage()) && systemXmlParse.mDeleteList.get(it4).getPackage() != null) {
                Slog.d(TAG, "that is fail, " + systemXmlParse.mDeleteList.get(it4).getPackage() + "should not be contains in black list");
                mSystemAppNameList.remove(systemXmlParse.mDeleteList.get(it4).getPackage());
            }
            if (mSystemAppPathList.contains(systemXmlParse.mDeleteList.get(it4).getPathName()) && systemXmlParse.mDeleteList.get(it4).getPathName() != null) {
                Slog.d(TAG, "that is fail, " + systemXmlParse.mDeleteList.get(it4).getPathName() + "should not be contains in black list");
                mSystemAppPathList.remove(systemXmlParse.mDeleteList.get(it4).getPathName());
            }
        }
        mAppChannelNameList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "appchannel", false));
        mGboardInputMethodList.addAll(getNameListFromPathFile(mDataEngineermodeDir, "gboard", false));
        mCanUninstallApksList.addAll(getNameListFromFile(mCustCanUninstllApksFile));
        mOperatorReserveWhiteList.addAll(getNameListFromOperatorFile());
        if (mIsAfterSaleRegionDevice) {
            Slog.d(TAG, "This is AfterSale Flash Region Phone");
            mAfterSaleSystemAppNameList.addAll(getNameListFromPathFile(mSystemBlacklistDir, "systemapp", true));
            if (mSystemAppNameList.contains("com.heytap.cloud") && !mAfterSaleSystemAppNameList.contains("com.heytap.cloud")) {
                Slog.d(TAG, "Remove com.heytap.cloud from blacklist for AfterSale Flash Region " + mAfterSaleRegion);
                mSystemAppNameList.remove("com.heytap.cloud");
            }
            printArrayListInfo(mAfterSaleSystemAppNameList, "mAfterSaleSystemAppNameList");
        }
        if (mIsSupportRSA3) {
            Slog.d(TAG, "Remove Data's Google lens and Google photos for rsa3.0's regions");
            int index = 0;
            while (index < mDataPackageNameList.size()) {
                if (mDataPackageNameList.get(index).contains("com.google.ar.lens") || mDataPackageNameList.get(index).contains("com.google.android.apps.photos")) {
                    ArrayList<String> arrayList = mDataPackageNameList;
                    arrayList.remove(arrayList.get(index));
                    index--;
                }
                index++;
            }
            int index2 = 0;
            while (index2 < mDataPathNameList.size()) {
                if (mDataPathNameList.get(index2).contains("com.google.ar.lens") || mDataPathNameList.get(index2).contains("com.google.android.apps.photos")) {
                    ArrayList<String> arrayList2 = mDataPathNameList;
                    arrayList2.remove(arrayList2.get(index2));
                    index2--;
                }
                index2++;
            }
        }
        if (mPms.isFirstBoot() || mIsBootFromSimOperatorSwitch) {
            mReserveWhiteList.addAll(initReserveWhiteList());
            printArrayListInfo(mReserveWhiteList, "reservewhitelist");
        }
        initRecoveryWhiteFile();
        printArrayListInfo(mSystemAppNameList, "systemapp");
        printArrayListInfo(mSystemAppPathList, "systempath");
        printArrayListInfo(mDataPackageNameList, "persistname");
        printArrayListInfo(mDataPathNameList, "persistpath");
        printArrayListInfo(mAppChannelNameList, "appchannel");
        printArrayListInfo(mGboardInputMethodList, "gboard");
        printArrayListInfo(mOperatorReserveWhiteList, "operator");
        setChannelProperties();
        dealWithGoogleNews();
    }

    static void setChannelProperties() {
        if (SystemProperties.getInt("ro.oppo.setchannel", 0) != 1) {
            if (hasGoogleNews()) {
                if (hasSimeji() || hasTouchPal() || !hasGboard()) {
                    SystemProperties.set("ro.oppo.rlz_ap_whitelist", "YH,YJ");
                } else {
                    SystemProperties.set("ro.oppo.rlz_ap_whitelist", "YH,YJ,YG");
                }
            } else if (!hasSimeji() && !hasTouchPal() && hasGboard()) {
                SystemProperties.set("ro.oppo.rlz_ap_whitelist", "YG");
            }
            if (!SystemProperties.get("ro.oppo.rlz_ap_whitelist", "").equals("")) {
                SystemProperties.set("ro.oppo.setchannel", "1");
                return;
            }
            return;
        }
        Slog.d(TAG, "The channel properties have been set yet.");
    }

    private static boolean hasTouchPal() {
        return isDataAppNameInWhiteList("com.emoji.keyboard.touchpal");
    }

    private static boolean hasSimeji() {
        return isDataAppNameInWhiteList("com.simeji.android.oppo");
    }

    private static boolean hasGboard() {
        return !isSystemAppNameInBlackList("com.google.android.inputmethod.latin");
    }

    private static boolean hasGoogleNews() {
        return isDataAppNameInWhiteList("com.google.android.apps.magazines");
    }

    static void dealWithGoogleNews() {
        if (mDataPackageNameList.size() > 0 && mDataPackageNameList.contains("com.google.android.apps.magazines")) {
            Slog.d(TAG, "start googlenews-handler!!");
            SystemProperties.set("ctl.start", "googlenews-handler");
        }
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
        if (!getOppoDeriveAbiStatus() || pkgName == null) {
            return false;
        }
        if (isDataAppNameInWhiteList(pkgName) || inOperatorReserveWhiteList(pkgName)) {
            Slog.d(TAG, pkgName + " ShouldOppoDerivePackageAbi!");
            return true;
        }
        return false;
    }

    static void scanCustomDataApp(int scanFlag) {
        File customDelAppDir = new File(DIR_CUSTOM_ROOT, "del-app");
        try {
            customDelAppDir = customDelAppDir.getCanonicalFile();
        } catch (IOException e) {
        }
        if (ArrayUtils.isEmpty(customDelAppDir.listFiles())) {
            Slog.d(TAG, "No files in app dir " + customDelAppDir);
            return;
        }
        scanDirTracedLI(customDelAppDir, 0, scanFlag, 0);
    }

    static void scanDirTracedLI(File scanDir, int parseFlags, int scanFlags, long currentTime) {
        CommonSoftReflectionHelper.callDeclaredMethod(mPms, "com.android.server.pm.PackageManagerService", "scanDirTracedLI", new Class[]{File.class, Integer.TYPE, Integer.TYPE, Long.TYPE}, new Object[]{scanDir, Integer.valueOf(parseFlags), Integer.valueOf(scanFlags), Long.valueOf(currentTime)});
    }

    static boolean needSkipScanning(PackageParser.Package pkg, boolean pkgAlreadyExists) {
        boolean pkgNotExsit = !pkgAlreadyExists;
        if (mPms.isFirstBoot() && isCustomDataApp(pkg.codePath)) {
            mCanUninstallApksList.add(pkg.packageName);
            writeNameListToFile(mCanUninstallApksList, mCustCanUninstllApksFile);
        }
        if (mPms.isFirstBoot() || !isCustomDataApp(pkg.codePath) || !pkgNotExsit) {
            return false;
        }
        if (isNewCustomDataApp(pkg.packageName)) {
            Slog.d(TAG, "New added removable sys app :" + pkg.packageName);
            mCanUninstallApksList.add(pkg.packageName);
            writeNameListToFile(mCanUninstallApksList, mCustCanUninstllApksFile);
            return false;
        }
        Slog.d(TAG, "Skip scanning uninstalled sys package " + pkg.packageName);
        return true;
    }

    static boolean isNewCustomDataApp(String packageName) {
        ArrayList<String> arrayList = mCanUninstallApksList;
        if (arrayList == null || arrayList.size() == 0) {
            return true;
        }
        Iterator<String> it = mCanUninstallApksList.iterator();
        while (it.hasNext()) {
            if (it.next().equals(packageName)) {
                return false;
            }
        }
        return true;
    }

    static boolean isCustomDataApp(String codePath) {
        if (codePath.startsWith(CUSTOMDELAPPDIR)) {
            return true;
        }
        return false;
    }

    static FileMatchCategory isFileNameMatchDevice(String filename, boolean isAfterSaleRegionDevice) {
        String filenameSubString;
        int endTagIndex;
        String euexcountry = "";
        FileMatchCategory projectMatchType = FileMatchCategory.MISMATCH;
        FileMatchCategory regionMatchType = FileMatchCategory.MISMATCH;
        FileMatchCategory operatorMatchType = FileMatchCategory.MISMATCH;
        FileMatchCategory euexcountryMatchType = FileMatchCategory.MISMATCH;
        int startTagIndex = filename.indexOf("_");
        if (startTagIndex == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String filenameSubString2 = filename.substring(startTagIndex + 1, filename.length());
        int endTagIndex2 = filenameSubString2.indexOf("_");
        if (endTagIndex2 == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String project = filenameSubString2.substring(0, endTagIndex2);
        int startTagIndex2 = filenameSubString2.indexOf("_");
        if (startTagIndex2 == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String filenameSubString3 = filenameSubString2.substring(startTagIndex2 + 1, filenameSubString2.length());
        int endTagIndex3 = filenameSubString3.indexOf("_");
        if (endTagIndex3 == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String region = filenameSubString3.substring(0, endTagIndex3);
        int startTagIndex3 = filenameSubString3.indexOf("_");
        if (startTagIndex3 == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String filenameSubString4 = filenameSubString3.substring(startTagIndex3 + 1, filenameSubString3.length());
        int endTagIndex4 = filenameSubString4.indexOf(".");
        if (endTagIndex4 == -1) {
            return FileMatchCategory.MISMATCH;
        }
        String operator = filenameSubString4.substring(0, endTagIndex4);
        int startTagIndex4 = filenameSubString4.indexOf(".");
        if (!(startTagIndex4 == -1 || (endTagIndex = (filenameSubString = filenameSubString4.substring(startTagIndex4 + 1, filenameSubString4.length())).indexOf(".")) == -1)) {
            euexcountry = filenameSubString.substring(0, endTagIndex);
        }
        if ("common".equals(project) || mProject.equals(project) || (project.startsWith("-") && !project.substring(project.indexOf("-") + 1, project.length()).equals(mProject))) {
            projectMatchType = FileMatchCategory.MATCH;
        }
        if (project.startsWith("-") && project.substring(project.indexOf("-") + 1, project.length()).equals(mProject)) {
            projectMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if ("common".equals(region) || mRegionMark.equals(region) || (region.startsWith("-") && !region.substring(region.indexOf("-") + 1, region.length()).equals(mRegionMark))) {
            regionMatchType = FileMatchCategory.MATCH;
        }
        if (region.startsWith("-") && region.substring(region.indexOf("-") + 1, region.length()).equals(mRegionMark)) {
            regionMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if (isAfterSaleRegionDevice) {
            if ("common".equals(region) || mAfterSaleRegion.equals(region) || (region.startsWith("-") && !region.substring(region.indexOf("-") + 1, region.length()).equals(mAfterSaleRegion))) {
                regionMatchType = FileMatchCategory.MATCH;
            }
            if (region.startsWith("-") && region.substring(region.indexOf("-") + 1, region.length()).equals(mAfterSaleRegion)) {
                regionMatchType = FileMatchCategory.MUST_BE_REMOVED;
            }
        }
        if ("common".equals(operator) || mOperator.equals(operator) || (("IGNOREOPERATOR".equals(operator) && isOperatorVersion()) || (operator.startsWith("-") && !operator.substring(operator.indexOf("-") + 1, operator.length()).equals(mOperator)))) {
            operatorMatchType = FileMatchCategory.MATCH;
        }
        if (operator.startsWith("-") && operator.substring(operator.indexOf("-") + 1, operator.length()).equals(mOperator)) {
            operatorMatchType = FileMatchCategory.MUST_BE_REMOVED;
        }
        if (!TextUtils.isEmpty(euexcountry) && !TextUtils.isEmpty(mEuexCountry)) {
            if ("common".equals(euexcountry) || mEuexCountry.equals(euexcountry) || (euexcountry.startsWith("-") && !euexcountry.substring(euexcountry.indexOf("-") + 1, euexcountry.length()).equals(mEuexCountry))) {
                euexcountryMatchType = FileMatchCategory.MATCH;
            }
            if (euexcountry.startsWith("-") && euexcountry.substring(euexcountry.indexOf("-") + 1, euexcountry.length()).equals(mEuexCountry)) {
                euexcountryMatchType = FileMatchCategory.MUST_BE_REMOVED;
            }
        }
        if (projectMatchType == FileMatchCategory.MATCH && regionMatchType == FileMatchCategory.MATCH && operatorMatchType == FileMatchCategory.MATCH && (euexcountryMatchType == FileMatchCategory.MATCH || TextUtils.isEmpty(euexcountry) || TextUtils.isEmpty(mEuexCountry))) {
            return FileMatchCategory.MATCH;
        }
        if (projectMatchType == FileMatchCategory.MUST_BE_REMOVED || regionMatchType == FileMatchCategory.MUST_BE_REMOVED || operatorMatchType == FileMatchCategory.MUST_BE_REMOVED || euexcountryMatchType == FileMatchCategory.MUST_BE_REMOVED) {
            return FileMatchCategory.MUST_BE_REMOVED;
        }
        return FileMatchCategory.MISMATCH;
    }

    static Set<String> getNameListFromPathFile(String path, String fileStartTag, boolean isAfterSaleRegionDevice) {
        File fileDir = new File(path);
        File[] files = fileDir.listFiles();
        Set<String> addSet = new HashSet<>();
        Set<String> removeSet = new HashSet<>();
        if (ArrayUtils.isEmpty(files)) {
            Slog.d(TAG, "No files in dir:" + fileDir);
            return addSet;
        }
        for (File file : files) {
            if (file.getName().startsWith(fileStartTag)) {
                FileMatchCategory matchType = isFileNameMatchDevice(file.getName(), isAfterSaleRegionDevice);
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
        ArrayList<String> list = new ArrayList<>();
        if (!file.exists()) {
            return list;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
            }
        } catch (IOException e) {
            Slog.e(TAG, "readApkWhiteList IOException " + e);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(in);
        return list;
    }

    static void writeNameListToFile(ArrayList<String> list, File file) {
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                out.write(it.next());
                out.newLine();
            }
        } catch (IOException e) {
            Slog.e(TAG, "writeNameListToFile IOException " + e);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(out);
    }

    static void printArrayListInfo(ArrayList<String> list, String name) {
        if (list.size() > 0) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "name=" + name + ",signlelist=" + it.next());
            }
        }
    }

    static ArraySet<String> getDataPackageNameList() {
        ArraySet<String> result = new ArraySet<>();
        result.addAll(mDataPackageNameList);
        return result;
    }

    static boolean isDisableSoftsimPackage(String packageName) {
        if (!EXP_VERSION) {
            return false;
        }
        if (packageName.equals("com.redteamobile.roaming") || packageName.equals("com.redteamobile.roaming.deamon")) {
            if (!mPms.hasSystemFeature("oppo.softsim.exp.support", 0)) {
                return true;
            }
            if (CommonSoftReflectionHelper.oppoTelephonyFunction_colorIsSimLockedEnabledTH() || CommonSoftReflectionHelper.oppoTelephonyFunction_colorIsSimLockedEnabled()) {
                Slog.e(TAG, "disabled by simlock" + packageName);
                return true;
            }
        }
        return false;
    }

    static boolean isDisableCloundInEuex(String pkgName) {
        String[] CloundEnableCountriesInEuex = {"FR", "DE", "IT", "ES", "NL", "PL", "GB"};
        boolean isCloudDisabledInEuex = false;
        if ("com.heytap.cloud".equals(pkgName) && "EUEX".equals(mRegionMark)) {
            isCloudDisabledInEuex = true;
            for (String str : CloundEnableCountriesInEuex) {
                if (str.equals(mEuexCountry)) {
                    isCloudDisabledInEuex = false;
                }
            }
        }
        if (isCloudDisabledInEuex) {
            Slog.d(TAG, "com.heytap.cloud is Disabled in EUEX");
        }
        return isCloudDisabledInEuex;
    }

    static boolean isSystemFilterApp(String pkgName) {
        String[] cmccFilterPackages = {"com.mobiletools.systemhelper", "com.oppo.ctautoregist"};
        String[] allnetFilterPackages = {"com.nativeapp.rcsapp"};
        if (mOperatorCN.equals("allnetcmccdeep")) {
            for (String str : cmccFilterPackages) {
                if (str.equals(pkgName)) {
                    Slog.d(TAG, "allnetcmccdeep Filter pkgName=" + pkgName);
                    return true;
                }
            }
        }
        if (!mOperatorCN.equals("allnet")) {
            return false;
        }
        for (String str2 : allnetFilterPackages) {
            if (str2.equals(pkgName)) {
                Slog.d(TAG, "allnet Filter pkgName=" + pkgName);
                return true;
            }
        }
        return false;
    }

    static boolean isSystemAppNameInBlackList(String packageName) {
        if (!isSystemFilterApp(packageName) && !isDisableCloundInEuex(packageName) && !mSystemAppNameList.contains(packageName) && !isDisableSoftsimPackage(packageName)) {
            return false;
        }
        return true;
    }

    static boolean isSystemAppPathInBlackList(String appPath) {
        String CTAutoRegistPath = SystemProperties.get("ro.oppo.disable_ct_app_path", "");
        if (!CTAutoRegistPath.equals("") && !appPath.equals("") && CTAutoRegistPath.equals(appPath)) {
            return true;
        }
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
            if (EXP_VERSION) {
                deleteDataSpecialPreloadForFirstBoot();
            }
        }
        if (isInternetDownload() || isBootFromAdbClear()) {
            if (!isOperatorNormalize() && !mRegionMark.equals("TW")) {
                deleteDataReserveAppsForAdbClear();
                deleteAppChannelForAdbClear();
            }
            deleteMediaResourceForAdbClear();
            deleteGboardResourceForMasterClear();
            SystemProperties.set("persist.sys.oppo.fromadbclear", TemperatureProvider.SWITCH_OFF);
        }
    }

    static boolean isBootFromMasterClear() {
        if (new File(mDataAppDir, "packages.xml").exists()) {
            return true;
        }
        return false;
    }

    static boolean isBootFromAdbClear() {
        return SystemProperties.get("persist.sys.oppo.fromadbclear", TemperatureProvider.SWITCH_OFF).equalsIgnoreCase(TemperatureProvider.SWITCH_ON);
    }

    static boolean isOperatorNormalize() {
        return SystemProperties.get("ro.oppo.operator_normalize", TemperatureProvider.SWITCH_OFF).equalsIgnoreCase(TemperatureProvider.SWITCH_ON);
    }

    static boolean isTochpalPackage(String packagename) {
        if (packagename.contains("com.cootek.smartinputv5") || packagename.contains("com.emoji.keyboard.touchpal")) {
            return true;
        }
        return false;
    }

    static String getDownloadStatusInternal() {
        byte[] status = CommonSoftReflectionHelper.oppoEngineerNative_getDownloadStatus();
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

    static void deleteDataSpecialPreloadForFirstBoot() {
        File dataSpecialPreloadDir = new File(mDataSpecialPreloadDir);
        if (dataSpecialPreloadDir.exists()) {
            File[] files = dataSpecialPreloadDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + dataSpecialPreloadDir);
                return;
            }
            for (File zipFile : files) {
                if (!zipFile.getName().contains("special_preload_" + mRegionMark + ".zip")) {
                    removeCodePath(zipFile);
                }
            }
        }
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

    static boolean isWhitePathForSamePackage(String pkgName, String apkFileName) {
        String whitePathForSamepackge = mWhiteSamePackageHashMap.get(pkgName);
        if (whitePathForSamepackge == null || apkFileName.equals(whitePathForSamepackge.replace(".apk", "")) || apkFileName.contains(pkgName)) {
            return true;
        }
        return false;
    }

    static boolean isMasterClearFilterApp(String pkgName) {
        String[] filterPackages;
        for (String str : new String[]{"com.coloros.accegamesdk", "com.oppo.daydreamvideo"}) {
            if (str.equals(pkgName)) {
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
                    if (pkgName != null && ((!mDataPackageNameList.contains(pkgName) || isTochpalPackage(pkgName)) && !isMasterClearFilterApp(pkgName))) {
                        removeCodePath(apkFile);
                    }
                    if (pkgName != null && mDataPackageNameList.contains(pkgName) && !isWhitePathForSamePackage(pkgName, apkFile.getName())) {
                        Slog.d(TAG, "is not WhitePath For SamePackage,deltet " + apkFile.getName());
                        removeCodePath(apkFile);
                    }
                }
            }
        }
    }

    static void deleteAppChannelForAdbClear() {
        File appChannelDir = new File(mAppChannelDir);
        if (appChannelDir.exists()) {
            File[] files = appChannelDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + appChannelDir);
                return;
            }
            for (File file : files) {
                if (!mAppChannelNameList.contains(file.getName()) && !mAppFlyerFile.getName().equals(file.getName())) {
                    deleteFiles(file);
                }
            }
        }
        dealWithAppFlyerChannel();
        dealWithZaloChannel();
    }

    static void dealWithAppFlyerChannel() {
        if (!TextUtils.isEmpty(mAppFlyerPath) && mAppFlyerFile.exists()) {
            new ArrayList();
            ArrayList<String> AppFlyerList = getNameListFromFile(mAppFlyerFile);
            Iterator iterator = AppFlyerList.iterator();
            while (iterator.hasNext()) {
                if (!mDataPackageNameList.contains(iterator.next().split("=")[0].trim())) {
                    iterator.remove();
                }
            }
            writeNameListToFile(AppFlyerList, mAppFlyerFile);
            FileUtils.setPermissions(mAppFlyerFile.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
        }
    }

    static void dealWithZaloChannel() {
        if (!TextUtils.isEmpty(mAppChannelDir)) {
            Slog.d(TAG, "dealWithZaloChannel zalo_channel exists: " + zalo_channel.exists());
            if (!zalo_channel.exists()) {
                return;
            }
            if (isDataAppNameInWhiteList(LABANKEY)) {
                Slog.d(TAG, "dealWithZaloChannel LABANKEY is exist in WhiteList, do nothing.");
                return;
            }
            ArrayList<String> preload = new ArrayList<>();
            preload.add("preload:OPPO_zmp3_BM_zalo_2nd");
            writeNameListToFile(preload, zalo_channel);
            FileUtils.setPermissions(zalo_channel.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
        }
    }

    static void removeCodePath(File codePath) {
        if (codePath.isDirectory()) {
            try {
                mInstaller.rmPackageDir(codePath.getAbsolutePath());
            } catch (Installer.InstallerException e) {
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

    static ArrayList<String> initReserveWhiteList() {
        ArrayList<String> list = new ArrayList<>();
        Iterator<String> it = COPY_APP_PATH.iterator();
        while (it.hasNext()) {
            File reserveDir = new File(it.next());
            if (reserveDir.exists() && mDataPackageNameList.size() > 0) {
                File[] files = reserveDir.listFiles();
                if (ArrayUtils.isEmpty(files)) {
                    Slog.d(TAG, "No files in dir:" + reserveDir);
                    return list;
                }
                for (File apkFile : files) {
                    String pkgName = getPkgName(apkFile);
                    if (pkgName != null && mDataPackageNameList.contains(pkgName)) {
                        String newpkgPath = apkFile.getName();
                        String oldpkgPath = mReserveHashMap.get(pkgName);
                        if (oldpkgPath == null) {
                            mReserveHashMap.put(pkgName, newpkgPath);
                        } else {
                            mWhiteSamePackageHashMap.put(pkgName, oldpkgPath);
                            if (mDataPathNameList.contains("data/app/" + newpkgPath.replace(".apk", ""))) {
                                mReserveHashMap.put(pkgName, newpkgPath);
                                mWhiteSamePackageHashMap.put(pkgName, newpkgPath);
                                Slog.d(TAG, "update DataReserveWhiteList pkg = " + pkgName + " newpkgPath:" + newpkgPath + " oldpkgPath:" + oldpkgPath);
                            }
                        }
                    }
                }
            }
        }
        if (!mWhiteSamePackageHashMap.isEmpty()) {
            for (Map.Entry<String, String> entry : mWhiteSamePackageHashMap.entrySet()) {
                Slog.d(TAG, "mWhiteSamePackageHashMap key = " + entry.getKey() + " Value = " + entry.getValue());
            }
        }
        if (!mReserveHashMap.isEmpty()) {
            for (Map.Entry<String, String> entry2 : mReserveHashMap.entrySet()) {
                Slog.d(TAG, "mReserveHashMap key = " + entry2.getKey() + " Value = " + entry2.getValue());
                list.add(entry2.getValue());
            }
        }
        return list;
    }

    static void deleteDataReserveAppsForAdbClear() {
        File dataReserveDir = new File(mDataReserveDir);
        if (dataReserveDir.exists() && mReserveWhiteList.size() > 0) {
            File[] files = dataReserveDir.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                Slog.d(TAG, "No files in dir:" + dataReserveDir);
                return;
            }
            for (File apkFile : files) {
                String apkFileName = apkFile.getName();
                if (apkFileName != null && !mReserveWhiteList.contains(apkFileName) && !inOperatorReserveWhiteList(apkFile)) {
                    RESERVE_DELETE.add(apkFileName + "-->" + getPkgName(apkFile));
                    deleteFiles(apkFile);
                }
            }
            if (RESERVE_DELETE.size() > 0) {
                recordReserveDelApps();
            }
        }
    }

    static void recordReserveDelApps() {
        Slog.d(TAG, "recordReserveDelApps");
        ArrayList<String> record = getNameListFromFile(RESERVE_DELETE_FILE);
        record.add("Record adb clear msg: (mIsBootFromSimOperatorSwitch ? " + mIsBootFromSimOperatorSwitch + ")");
        record.add("Regionmark: " + mRegionMark + " Operator: " + mOperator + " EuexCountry: " + mEuexCountry);
        record.addAll(RESERVE_DELETE);
        RESERVE_DELETE.clear();
        record.add("############################");
        writeNameListToFile(record, RESERVE_DELETE_FILE);
        SystemProperties.set("persist.sys.reserve_app.deleted", TemperatureProvider.SWITCH_ON);
    }

    static void CopyDataReserveAppsForSimOperatorSwitch() {
        if (!mIsBootFromSimOperatorSwitch) {
            Slog.d(TAG, "skip CopyDataReserveAppsForSimSwitch for : persist.sys.oppo.opinstalled = " + SystemProperties.getBoolean("persist.sys.oppo.opinstalled", false) + " ro.oppo.sim_operator_switch = " + SystemProperties.getBoolean("ro.oppo.sim_operator_switch", false));
            return;
        }
        Slog.d(TAG, "start CopyDataReserveAppsForSimOperatorSwitch");
        copyReserveApk();
        if (!mRegionMark.equals("TW")) {
            deleteDataReserveAppsForAdbClear();
            deleteAppChannelForAdbClear();
        }
        SystemProperties.set("persist.sys.oppo.opinstalled", TemperatureProvider.SWITCH_ON);
        enableOppoDeriveAbi();
    }

    public static void copyReserveApk() {
        Iterator<String> it = COPY_APP_PATH.iterator();
        while (it.hasNext()) {
            File oppoReserveApkPath = new File(it.next());
            if (oppoReserveApkPath.exists() && mReserveWhiteList.size() > 0 && oppoReserveApkPath.listFiles() != null) {
                File[] listFiles = oppoReserveApkPath.listFiles();
                for (File apkFile : listFiles) {
                    String apkFileName = apkFile.getName();
                    if (!EXP_VERSION && apkFileName.contains("-")) {
                        int startIndex = apkFileName.lastIndexOf("-");
                        int fileNameLength = apkFileName.length();
                        if (startIndex != -1 && fileNameLength > startIndex + 4) {
                            if (!mProject.equals(apkFileName.substring(startIndex + 1, fileNameLength - 4))) {
                                Slog.i(TAG, "ignore reserve file=" + apkFileName);
                            }
                        }
                    }
                    if (!mReserveWhiteList.contains(apkFileName)) {
                        Slog.i(TAG, apkFileName + " not in mReserveWhiteList, skip it!");
                    } else {
                        String packageName = getKey(mReserveHashMap, apkFileName);
                        if (packageName == null) {
                            Slog.i(TAG, "reserve package null, error!!!");
                        } else if (isPackageExists(packageName, apkFileName.replace(".apk", ""))) {
                            Slog.i(TAG, "apk:" + packageName + " has been installed, skip it!");
                        } else {
                            File packageDir = new File(mDataAppDir, mWhiteSamePackageHashMap.get(packageName) == null ? packageName : apkFileName.replace(".apk", ""));
                            if (!packageDir.exists()) {
                                packageDir.mkdir();
                            }
                            FileUtils.setPermissions(packageDir.getPath(), 505, -1, -1);
                            File destFile = new File(packageDir, apkFile.getName());
                            Slog.i(TAG, "apk:" + packageName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                            FileUtils.copyFile(apkFile, destFile);
                            FileUtils.setPermissions(destFile.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
                        }
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
            if (TextUtils.isEmpty(SystemProperties.get("ro.oppo.reconcile_media_resource", ""))) {
                SystemProperties.set("ro.oppo.reconcile_media_resource", "1");
                SystemProperties.set("ctl.start", "media-resource-reconciliation");
            }
        }
    }

    static void initRecoveryWhiteFile() {
        if (mDataPackageNameList.size() > 0) {
            File baseDir = new File(mWhiteRecoveryDir);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            Slog.i(TAG, "Init recovery white file with mDataPackageNameList");
            writeNameListToFile(mDataPackageNameList, mWhiteRecoveryNameFile);
            writeNameListToFile(mDataPathNameList, mWhiteRecoveryPathFile);
            return;
        }
        Slog.i(TAG, "Skip initRecoveryWhiteFile, isFirstBoot: " + mPms.isFirstBoot() + ", size: " + mDataPackageNameList.size());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:144:?, code lost:
        return;
     */
    public static void parseMediaResourceXml() {
        String str;
        String str2;
        XmlPullParserFactory factory;
        boolean isProjectHit;
        boolean isOperatorHit;
        boolean isCountryHit;
        boolean isOperatorHit2;
        String str3 = ",";
        String str4 = "-";
        InputStream input = null;
        try {
            InputStream input2 = new FileInputStream(new File(mMediaResourceConfigFileName));
            XmlPullParserFactory factory2 = XmlPullParserFactory.newInstance();
            int i = 1;
            factory2.setNamespaceAware(true);
            XmlPullParser parser = factory2.newPullParser();
            parser.setInput(input2, "UTF-8");
            while (parser.getEventType() != i) {
                if (parser.getEventType() == 2) {
                    String eventname = parser.getName();
                    if (eventname.equals("black-resource")) {
                        boolean isProjectHit2 = false;
                        boolean isCountryHit2 = false;
                        boolean isOperatorHit3 = false;
                        String project = parser.getAttributeValue(null, "project");
                        String country = parser.getAttributeValue(null, "country");
                        String operator = parser.getAttributeValue(null, "operator");
                        String path = parser.getAttributeValue(null, "path");
                        if (project == null || country == null || operator == null || path == null) {
                            Slog.e(TAG, "mBlackMediaResourceList xml may config wrong in this line!");
                            parser.next();
                            factory2 = factory2;
                            str3 = str3;
                            str4 = str4;
                            i = 1;
                        } else {
                            String[] projectArray = project.split(str3);
                            if (!project.contains(str4)) {
                                factory = factory2;
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= projectArray.length) {
                                        isProjectHit = false;
                                        break;
                                    } else if (mProject.equals(projectArray[i2].trim()) || projectArray[i2].trim().equals("common")) {
                                        isProjectHit = true;
                                    } else {
                                        i2++;
                                    }
                                }
                            } else {
                                boolean tmpHit = true;
                                factory = factory2;
                                int i3 = 0;
                                while (true) {
                                    if (i3 >= projectArray.length) {
                                        break;
                                    }
                                    if ((str4 + mProject).equals(projectArray[i3].trim())) {
                                        tmpHit = false;
                                        break;
                                    }
                                    i3++;
                                    eventname = eventname;
                                    isProjectHit2 = isProjectHit2;
                                }
                                isProjectHit = tmpHit;
                            }
                            String[] countryArray = country.split(str3);
                            if (!country.contains(str4)) {
                                isOperatorHit = false;
                                int i4 = 0;
                                while (true) {
                                    if (i4 >= countryArray.length) {
                                        isCountryHit = false;
                                        break;
                                    } else if (mRegionMark.equals(countryArray[i4].trim()) || countryArray[i4].trim().equals("common")) {
                                        isCountryHit = true;
                                    } else {
                                        i4++;
                                    }
                                }
                            } else {
                                boolean tmpHit2 = true;
                                int i5 = 0;
                                while (true) {
                                    if (i5 >= countryArray.length) {
                                        isOperatorHit = isOperatorHit3;
                                        break;
                                    }
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(str4);
                                    isOperatorHit = isOperatorHit3;
                                    sb.append(mRegionMark);
                                    if (sb.toString().equals(countryArray[i5].trim())) {
                                        tmpHit2 = false;
                                        break;
                                    }
                                    i5++;
                                    isCountryHit2 = isCountryHit2;
                                    isOperatorHit3 = isOperatorHit;
                                }
                                isCountryHit = tmpHit2;
                            }
                            String[] operatorArray = operator.split(str3);
                            if (operator.contains(str4)) {
                                boolean tmpHit3 = true;
                                int i6 = 0;
                                while (true) {
                                    str2 = str3;
                                    if (i6 >= operatorArray.length) {
                                        str = str4;
                                        break;
                                    }
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append(str4);
                                    str = str4;
                                    sb2.append(mOperator);
                                    if (sb2.toString().equals(operatorArray[i6].trim())) {
                                        tmpHit3 = false;
                                        break;
                                    }
                                    i6++;
                                    str3 = str2;
                                    str4 = str;
                                }
                                isOperatorHit2 = tmpHit3;
                            } else {
                                str2 = str3;
                                str = str4;
                                int i7 = 0;
                                while (true) {
                                    if (i7 >= operatorArray.length) {
                                        isOperatorHit2 = isOperatorHit;
                                        break;
                                    } else if (mOperator.equals(operatorArray[i7].trim()) || operatorArray[i7].trim().equals("common")) {
                                        isOperatorHit2 = true;
                                    } else {
                                        i7++;
                                    }
                                }
                                isOperatorHit2 = true;
                            }
                            if ((mOperator.isEmpty() && operator.equals("EX")) || (!mOperator.isEmpty() && operator.equals("ALLOPERATOR"))) {
                                isOperatorHit2 = true;
                            }
                            Slog.i(TAG, "parseMediaResourceXml project = " + project + ",country = " + country + ",operator = " + operator + ",path = " + path + ",isProjectHit = " + isProjectHit + ",isCountryHit = " + isCountryHit + ",isOperatorHit = " + isOperatorHit2);
                            if (isProjectHit && isCountryHit && isOperatorHit2) {
                                Slog.i(TAG, "mBlackMediaResourceList add " + path);
                                mBlackMediaResourceList.add(path);
                            }
                        }
                    } else {
                        str2 = str3;
                        str = str4;
                        factory = factory2;
                    }
                } else {
                    str2 = str3;
                    str = str4;
                    factory = factory2;
                }
                parser.next();
                factory2 = factory;
                str3 = str2;
                str4 = str;
                i = 1;
            }
            try {
                input2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            if (input != null) {
                input.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (input != null) {
                input.close();
            }
        } catch (Throwable th) {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static boolean isPackageExists(String pkgName, String apkFileName) {
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
            if (apkFile.getName().startsWith(pkgName) || apkFile.getName().equals(apkFileName)) {
                return true;
            }
        }
        return false;
    }

    static void deleteFiles(File file) {
        File[] files;
        if (file.getName().startsWith("com.cootek.smartinputv5.language") && mNetLock.equals("") && mRegionMark.equals("")) {
            Slog.d(TAG, "ignore file" + file);
        } else if (!file.exists()) {
        } else {
            if (file.isFile()) {
                Slog.d(TAG, "delete file : " + file);
                file.delete();
            } else if (file.isDirectory()) {
                for (File file2 : file.listFiles()) {
                    deleteFiles(file2);
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
            return null;
        } else if (file.getName().endsWith(".apk")) {
            return parsePkg(file);
        } else {
            return null;
        }
    }

    static String parsePkg(File file) {
        try {
            PackageParser.PackageLite pkg = PackageParser.parsePackageLite(file, Integer.MIN_VALUE);
            if (pkg != null) {
                return pkg.packageName;
            }
            return null;
        } catch (PackageParser.PackageParserException ex) {
            Slog.e(TAG, "parser pkg fail !!!" + ex);
            return null;
        }
    }

    public static String getKey(HashMap<String, String> map, String value) {
        String key = null;
        if (!(map == null || value == null)) {
            for (String getKey : map.keySet()) {
                if (map.get(getKey).equals(value)) {
                    key = getKey;
                }
            }
        }
        return key;
    }

    static void initPackageChannelHashMap() {
        ArrayList<String> list = getNameListFromFile(mPackageChannelFile);
        if (list == null || list.size() < 1) {
            Slog.d(TAG, "package_channel.txt may be not exist or null!");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            String[] listArray = list.get(i).split(":", 2);
            if (listArray != null && listArray.length == 2) {
                mPackageChannelHashMap.put(listArray[0], listArray[1]);
            }
        }
        if (!mPackageChannelHashMap.isEmpty()) {
            for (Map.Entry<String, String> entry : mPackageChannelHashMap.entrySet()) {
                Slog.d(TAG, "mPackageChannelHashMap key = " + entry.getKey() + " Value = " + entry.getValue());
            }
        }
    }

    static void supportDeleteChannelFile(String packageName) {
        String channelFileName;
        if (!mPms.hasSystemFeature("oppo.user.delete.channel.support", 0)) {
            Slog.d(TAG, "not support delete channel with apk removing!");
        } else if (packageName != null && mPackageChannelHashMap.containsKey(packageName) && (channelFileName = mPackageChannelHashMap.get(packageName)) != null) {
            try {
                File channelFile = new File(mAppChannelDir, channelFileName);
                Slog.d(TAG, "delete channle file " + channelFileName + " for remove package " + packageName);
                deleteFiles(channelFile);
            } catch (Exception e) {
                Slog.e(TAG, "delete channel file exception");
            }
        }
    }

    static void loadCustomFeatures() {
        readPermissions(Environment.buildPath(DIR_CUSTOM_ROOT, new String[]{"feature"}), -1);
        readPermissions(Environment.buildPath(DIR_CUSTOM_ROOT, new String[]{"etc", "sysconfig"}), -1);
        readPermissions(product_etc, -1);
        readPermissions(version_etc, -1);
        readPermissions(engineer_etc, -1);
        readPermissions(custom_etc, -1);
    }

    static ArrayList<String> getNameListFromOperatorFile() {
        String operator;
        if (!TextUtils.isEmpty(mOperator) || !"EUEX".equals(mRegionMark)) {
            operator = mOperator;
        } else {
            operator = "TMOBILE";
        }
        return getNameListFromFile(new File("/data/engineermode/persistname_common_common_##" + operator + ".txt"));
    }

    static boolean inOperatorReserveWhiteList(File file) {
        if (file == null || mOperatorReserveWhiteList.isEmpty() || !mOperatorReserveWhiteList.contains(getPkgName(file))) {
            return false;
        }
        Slog.i(TAG, "inOperatorReserveWhiteList true path " + file.getPath());
        return true;
    }

    static boolean inOperatorReserveWhiteList(String name) {
        if (mOperatorReserveWhiteList.isEmpty() || !mOperatorReserveWhiteList.contains(name)) {
            return false;
        }
        Slog.i(TAG, "inOperatorReserveWhiteList true name " + name);
        return true;
    }

    static void readPermissions(File libraryDir, int permissionFlag) {
        CommonSoftReflectionHelper.callDeclaredMethod(SystemConfig.getInstance(), CLS_SYSTEMCONFIG, METHOD_READPERMISSIONS, new Class[]{File.class, Integer.TYPE}, new Object[]{libraryDir, Integer.valueOf(permissionFlag)});
    }

    static File getDirectory(String variableName, String defaultPath) {
        File file;
        String path = System.getenv(variableName);
        if (path != null) {
            file = new File(path);
        }
        return file;
    }

    static boolean isOperatorVersion() {
        if ((TextUtils.isEmpty(mOperator) || isOperatorNormalizeArea()) && TextUtils.isEmpty(mSimOperatorProp)) {
            return false;
        }
        return true;
    }

    static boolean isOperatorNormalizeArea() {
        String[] operatorNormalizeFilter;
        for (String str : new String[]{"TWOP", "SGOP", "NZOP", "MYOP"}) {
            if (str.equals(mOperator)) {
                Slog.d(TAG, "operatorNormalizeFilter area= " + mOperator);
                return true;
            }
        }
        return false;
    }
}
