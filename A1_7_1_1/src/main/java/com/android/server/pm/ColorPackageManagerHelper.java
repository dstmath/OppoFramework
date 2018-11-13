package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ColorPackageManagerHelper {
    private static final String ACTION_EVENTID_PREVENT_UNINSTALL = "PMS_prevent_uninstall";
    private static final String ACTION_OPPO_INSTALL_FAILED = "oppo.intent.action.OPPO_INSTALL_FAILED";
    private static final String ACTION_OPPO_START_INSTALL = "oppo.intent.action.OPPO_START_INSTALL";
    private static final String ACTION_OPPO_THIRD_APP_LAUNCH = "oppo.intent.action.OPPO_THIRD_APP_FIRST_LAUNCH";
    private static final String APP_CODE = "20120";
    private static final ArrayList<String> CAMERA_DEFAULT_GRANT_PEMISSION = null;
    private static final String CAMERA_SHARE_UID_NEW = "oppo.uid.camera";
    private static final String CAMERA_SHARE_UID_OLD = "com.oppo.camera.Camera";
    public static final int CLOSE_RESTORE_SANDBOX_STATE = 2;
    public static final int CLOSE_SANDBOX_SWITCH = 0;
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI = null;
    private static final ArrayList<String> CTTL_PACKAGE_LIST = null;
    private static final String DATA_APP_LOCATION = "/data/app/";
    public static final ArrayList<String> DEFAULT_APP_WHITE_LIST = null;
    public static final int DEFAULT_APP_WHITE_LIST_INDEX = 677;
    private static final ArrayList<String> DEFAULT_OPPO_HIDE_APP_LIST = null;
    public static final List<String> DEFAULT_PROTECT_HIDE_APP = null;
    private static final List<String> DEFAULT_SYSTEM_APP_IN_DATA = null;
    private static final ArrayList<String> EXP_SYSTEM_DEFAULT_PACKAGES = null;
    private static final ArrayList<String> EXP_SYSTEM_FORCE_PACKAGES = null;
    private static final String FILE_NOT_LAUNCHED_LIST = "notLaunchedPkgs.xml";
    public static final List<String> FILTER_RUNTIME_PERM_GROUPS = null;
    private static final ArrayList<String> GAMECENTER_DEFAULT_GRANT_PEMISSION = null;
    private static final String GAMECENTER_SHARE_UID = "oppo.uid.gc";
    private static final String LAUNCHER_SHARE_UID = "oppo.uid.launcher";
    private static final ArrayList<String> NEARME_DEFAULT_GRANT_PEMISSION = null;
    private static final String NEARME_SHARE_UID = "oppo.uid.nearme";
    private static final String ODEX_ARM64_FILE = "/arm64/base.odex/";
    private static final String ODEX_ARM_FILE = "/arm/base.odex/";
    private static final int ODEX_DATA_MIN_SIZE = 200;
    public static final int OPEN_SANDBOX_SWITCH = 1;
    private static final String OPPO_DEFAULT_PACKAGE_XML = "sys_pms_defaultpackage_list";
    private static final String OPPO_DEFAULT_PKG_CONFIG = "/data/system/config/sys_pms_defaultpackage_list.xml";
    private static final String OPPO_DEFAULT_PKG_PATH = "/data/system/config";
    public static final String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
    private static final ArrayList<String> OPPO_FIXED_PERM_LIST = null;
    public static final String OPPO_FORBID_INSTALL_ACTION = "oppo.android.intent.action.FORBID_INSTALL";
    private static final ArrayList<String> OPPO_OVERSEA_FIXED_PERM_LIST = null;
    private static final ArrayList<String> OPPO_OVERSEA_NONFIXED_PERM_LIST = null;
    private static final String OPPO_RUNTIME_PERM_FILTER_FILE = "/data/system/config/sys_pms_runtimeperm_filter_list.xml";
    private static final String OPPO_SYSTEM_APP_PATH = "/system/etc/security/pl.fs";
    private static final String OPPO_SYSTEM_APP_PWD = "a";
    private static final String PATH_NOT_LAUNCHED_LIST = "/data/system/";
    public static final int PROTECT_HIDE_APP_INDEX = 676;
    private static final ArrayList<String> SEPCIAL_DEFAULT_SECURE_APP = null;
    private static final ArrayList<String> SKIP_APP_PACKAGE_LIST = null;
    private static final ArrayList<String> SKIP_SAFECENTER_PACKAGE_LIST = null;
    private static final ArrayList<String> SYSTEM_DEFAULT_PACKAGES = null;
    private static final ArrayList<String> SYSTEM_FORCE_PACKAGES = null;
    static final String TAG = "ColorPackageManager";
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
    private static FileObserverPolicy mConfigFileObserver;
    private static int mIconCacheMaxNum;
    private static float mIconCacheMinMemory;
    private static ArrayList<String> mLocalCtsPkgList;
    public static ArrayList<String> mNotLaunchedPkgs;
    private static final ArrayList<String> mOppoApkList = null;
    private static ArrayList<String> mOppoCtsPkgList;
    private static ArrayList<String> mOppoCtsPrefixList;
    private static ArrayList<String> mOppoDefaultPkgList;
    private static ArrayList<RuntimePermFilterInfo> mOppoFixedPermInfos;
    private static ArrayList<String> mOppoForcePkgList;
    private static ArrayList<String> mOppoHidePkgList;
    private static ArrayList<RuntimePermFilterInfo> mOppoOverseaFixedPermInfos;
    private static ArrayList<RuntimePermFilterInfo> mOppoOverseaNonFixedPermInfos;
    public static ArrayList<PackageSetting> mOppoSystemToDataList;
    private static String mSecurePaySwitch;
    private static ArrayList<String> mSpecialSecureAppList;
    private static ArrayList<String> mSystemDataPkgList;
    private static SparseArray<ArrayList<String>> sPmsWhiteList;
    private static float sTotalMemorySize;

    private static class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorPackageManagerHelper.OPPO_DEFAULT_PKG_CONFIG)) {
                Slog.i(ColorPackageManagerHelper.TAG, "onEvent: focusPath = OPPO_CRASH_CLEAR_CONFIG_PATH");
                ColorPackageManagerHelper.mOppoDefaultPkgList.clear();
                ColorPackageManagerHelper.mOppoForcePkgList.clear();
                ColorPackageManagerHelper.mOppoCtsPkgList.clear();
                ColorPackageManagerHelper.mOppoCtsPrefixList.clear();
                ColorPackageManagerHelper.mSpecialSecureAppList.clear();
                ColorPackageManagerHelper.mOppoHidePkgList.clear();
                ColorPackageManagerHelper.sPmsWhiteList.clear();
                ColorPackageManagerHelper.readConfigFile();
            }
        }
    }

    public static class RuntimePermFilterInfo {
        public boolean addAll;
        public int fixType;
        public ArrayList<String> groups;
        public boolean overSea;
        public String packageName;
    }

    private static class SyncNotLaunchedPkgsToFileRunnable implements Runnable {
        boolean mNotify;
        String mPkg;

        public SyncNotLaunchedPkgsToFileRunnable(String pkg, boolean needNotify) {
            this.mPkg = pkg;
            this.mNotify = needNotify;
        }

        public void run() {
            synchronized (ColorPackageManagerHelper.mNotLaunchedPkgs) {
                ColorPackageManagerHelper.writeNotLaunchedListToFile(ColorPackageManagerHelper.mNotLaunchedPkgs);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.ColorPackageManagerHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.ColorPackageManagerHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ColorPackageManagerHelper.<clinit>():void");
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
        return false;
    }

    public static boolean isGrantedPermissionForGameCenter(String shareUid, String permission) {
        if (GAMECENTER_SHARE_UID.equals(shareUid)) {
            return GAMECENTER_DEFAULT_GRANT_PEMISSION.contains(permission);
        }
        return false;
    }

    public static boolean isSupportSecurePay() {
        return "true".equals(mSecurePaySwitch);
    }

    private static void setSandboxSwitchState(String value) {
        int valueState = Integer.parseInt(value);
        if (valueState == 0) {
            Slog.d(TAG, "setSandboxSwitchState valueState = " + valueState);
            SystemProperties.set("persist.sys.coloros.sandbox", String.valueOf(2));
        }
    }

    public static boolean IsOppoApkList(String string) {
        return mOppoApkList.contains(string);
    }

    public static boolean IsOppoDefaultApp(String string) {
        if (mOppoDefaultPkgList.size() >= 1) {
            return mOppoDefaultPkgList.contains(string);
        }
        return isExpROM() ? EXP_SYSTEM_DEFAULT_PACKAGES.contains(string) : SYSTEM_DEFAULT_PACKAGES.contains(string);
    }

    private static boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    public static boolean isSystemDataApp(String pkg) {
        if (pkg == null) {
            return false;
        }
        if (mSystemDataPkgList.isEmpty()) {
            return DEFAULT_SYSTEM_APP_IN_DATA.contains(pkg);
        }
        return mSystemDataPkgList.contains(pkg);
    }

    public static boolean isSpecialSecureApp(String pkg) {
        if (SEPCIAL_DEFAULT_SECURE_APP.contains(pkg)) {
            return true;
        }
        return mSpecialSecureAppList.size() > 0 && mSpecialSecureAppList.contains(pkg);
    }

    public static boolean IsCtsApp(String pkg) {
        if (mLocalCtsPkgList.contains(pkg)) {
            return true;
        }
        if (mOppoCtsPkgList.size() > 0 && mOppoCtsPkgList.contains(pkg)) {
            return true;
        }
        if (mOppoCtsPrefixList.size() > 0) {
            for (String ctsPkg : mOppoCtsPrefixList) {
                if (pkg.contains(ctsPkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void initCtsToolList() {
        File permFile = new File(Environment.getRootDirectory(), "etc/oppo_cts_list.xml");
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equals("CtsToolList")) {
                                eventType = parser.next();
                                mLocalCtsPkgList.add(parser.getText());
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
            Slog.d(TAG, "initCtsToolList size=" + mLocalCtsPkgList.size());
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
            if (IsCtsApp(pkg.substring(0, pkg.length() - 2))) {
                return true;
            }
        }
        return false;
    }

    public static boolean IsOppoForceApp(String string) {
        if (mOppoForcePkgList.size() >= 1) {
            return mOppoForcePkgList.contains(string);
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
            if (IsOppoApkList(appPkg)) {
                Slog.d(TAG, "skip oppo pkg = " + appPkg);
            } else {
                String arm64OdexStr = DATA_APP_LOCATION + pkg + ODEX_ARM64_FILE;
                File armOdexFile = new File(DATA_APP_LOCATION + pkg + ODEX_ARM_FILE);
                File arm64OdexFile = new File(arm64OdexStr);
                if (armOdexFile.exists() || arm64OdexFile.exists()) {
                    boolean isArm;
                    if (armOdexFile.exists() && !arm64OdexFile.exists()) {
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
                        if (!(oatFile.exists() || oatFile.isDirectory())) {
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
                        if (!(oatFile.exists() || oatFile.isDirectory())) {
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

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0084 A:{SYNTHETIC, Splitter: B:34:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0075 A:{SYNTHETIC, Splitter: B:25:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0090 A:{SYNTHETIC, Splitter: B:41:0x0090} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int ReadEncryptFile() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        FileInputStream fis = null;
        try {
            Slog.d(TAG, "ReadEncryptFile!!!");
            File path = new File(OPPO_SYSTEM_APP_PATH);
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
                    try {
                        fis.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return -1;
            } catch (IOException e5) {
                e32 = e5;
                fis = fis2;
                try {
                    e32.printStackTrace();
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
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
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            e2.printStackTrace();
            if (fis != null) {
            }
            return -1;
        } catch (IOException e7) {
            e3222 = e7;
            e3222.printStackTrace();
            if (fis != null) {
            }
            return -1;
        }
    }

    public static void OppoCheckSuApp(String codePath, Context ct) throws PackageManagerException {
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
                            Slog.e(TAG, "OppoCheckSuApp fatal error:" + e);
                            if (zipInputStream != null) {
                                try {
                                    Slog.d(TAG, "check finish!!");
                                    zipInputStream.close();
                                } catch (IOException e3) {
                                    Slog.e(TAG, "OppoCheckSuApp fatal error:" + e3);
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
                                    Slog.e(TAG, "OppoCheckSuApp fatal error:" + e32);
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
                            for (String IsRootFile : splitName) {
                                if (IsRootFile(IsRootFile)) {
                                    ShowMessageToUSer(ct);
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
                                    Slog.e(TAG, "OppoCheckSuApp fatal error:" + e322);
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

    private static void ShowMessageToUSer(Context context) {
        context.sendBroadcast(new Intent(OPPO_FORBID_INSTALL_ACTION));
    }

    private static boolean IsRootFile(String file) {
        if (file.equalsIgnoreCase("su") || file.equalsIgnoreCase("root") || file.equalsIgnoreCase("superuser.apk") || file.equalsIgnoreCase("kinguser.apk") || file.equalsIgnoreCase("libsu.so")) {
            return true;
        }
        return file.equalsIgnoreCase("libroot.so");
    }

    private static String getDataFromProvider(Context mContext, String filterName) {
        Cursor cursor = null;
        String xmlValue = null;
        String[] projection = new String[1];
        projection[0] = COLUMN_NAME_XML;
        try {
            cursor = mContext.getContentResolver().query(CONTENT_URI, projection, "filtername=\"" + filterName + "\"", null, null);
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
                                                                                mOppoHidePkgList.add(value);
                                                                                break;
                                                                            }
                                                                        }
                                                                        value = parser.nextText().trim();
                                                                        if (value != null) {
                                                                            Slog.d(TAG, "TAG_SYSTEM_DATA_APP : " + value);
                                                                            mSystemDataPkgList.add(value);
                                                                            break;
                                                                        }
                                                                    }
                                                                    value = parser.nextText().trim();
                                                                    if (value != null) {
                                                                        Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MIN_MEM : " + value);
                                                                        if (Float.parseFloat(value) >= OppoBrightUtils.MIN_LUX_LIMITI) {
                                                                            mIconCacheMinMemory = Float.parseFloat(value);
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                value = parser.nextText().trim();
                                                                if (value != null) {
                                                                    Slog.d(TAG, "TAG_OPPO_ICON_CACHE_MAX_NUM : " + value);
                                                                    if (Integer.parseInt(value) >= 0) {
                                                                        mIconCacheMaxNum = Integer.parseInt(value);
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            value = parser.nextText();
                                                            if (value != null) {
                                                                Slog.d(TAG, "TAG_SPECIAL_SECURE_APP : " + value);
                                                                mSpecialSecureAppList.add(value);
                                                                break;
                                                            }
                                                        }
                                                        value = parser.nextText();
                                                        if (value != null) {
                                                            Slog.d(TAG, "TAG_OPPO_SECUREPAY_SWITCH : " + value);
                                                            mSecurePaySwitch = value;
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
                                                    mOppoCtsPrefixList.add(value);
                                                    break;
                                                }
                                            }
                                            value = parser.nextText();
                                            if (value != null) {
                                                Slog.d(TAG, "TAG_OPPO_CTS_APP : " + value);
                                                mOppoCtsPkgList.add(value);
                                                break;
                                            }
                                        }
                                        value = parser.nextText();
                                        if (value != null) {
                                            Slog.d(TAG, "TAG_OPPO_FORCE_APP : " + value);
                                            mOppoForcePkgList.add(value);
                                            break;
                                        }
                                    }
                                    value = parser.nextText();
                                    if (value != null) {
                                        Slog.d(TAG, "TAG_OPPO_DEFAULT_APP : " + value);
                                        mOppoDefaultPkgList.add(value);
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
                            mOppoDefaultPkgList.add(value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_OPPO_FORCE_APP.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            mOppoForcePkgList.add(value);
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
        mConfigFileObserver = new FileObserverPolicy(OPPO_DEFAULT_PKG_CONFIG);
        mConfigFileObserver.startWatching();
    }

    public static int getAvaiDataSize() {
        StatFs sf = new StatFs("/data");
        return (int) ((sf.getBlockSizeLong() * sf.getAvailableBlocksLong()) / 1048576);
    }

    public static boolean isSetContainsOppoDefaultPkg(ComponentName[] set, ComponentName activity) {
        String PKG_OPPO_LAUNCHER = ActivityManagerService.OPPO_LAUNCHER;
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
                if (pkg != null) {
                    if (isExpROM()) {
                        if (EXP_SYSTEM_DEFAULT_PACKAGES.contains(pkg) && !ActivityManagerService.OPPO_LAUNCHER.equals(pkg)) {
                        }
                    } else if (SYSTEM_DEFAULT_PACKAGES.contains(pkg)) {
                    }
                    result = true;
                    break;
                }
                continue;
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

    public static boolean IsCttlApp(String pkg) {
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
        File systemConfigPah = new File(OPPO_DEFAULT_PKG_PATH);
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
                    info.packageName = splits[0];
                    info.addAll = false;
                    ArrayList<String> groups = new ArrayList();
                    for (int i = 1; i < splits.length; i++) {
                        String group = splits[i];
                        if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                            groups.add(group);
                        }
                    }
                    info.groups = groups;
                } else {
                    info.packageName = splits[0];
                    info.addAll = true;
                }
            } else {
                info.packageName = value;
                info.addAll = true;
            }
            tempList.add(info);
        }
        return tempList;
    }

    public static ArrayList<RuntimePermFilterInfo> getFixedRuntimePermInfos(boolean overSea) {
        if (overSea) {
            if (mOppoOverseaFixedPermInfos == null || mOppoOverseaFixedPermInfos.isEmpty()) {
                return getDefaultPermFilterInfosFromStr(OPPO_OVERSEA_FIXED_PERM_LIST);
            }
            return mOppoOverseaFixedPermInfos;
        } else if (mOppoFixedPermInfos == null || mOppoFixedPermInfos.isEmpty()) {
            return getDefaultPermFilterInfosFromStr(OPPO_FIXED_PERM_LIST);
        } else {
            return mOppoFixedPermInfos;
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
                                                info.overSea = true;
                                                if (value.contains("#")) {
                                                    splits = value.split("#");
                                                    if (splits.length >= 2) {
                                                        info.packageName = splits[0];
                                                        info.addAll = false;
                                                        groups = new ArrayList();
                                                        for (i = 1; i < splits.length; i++) {
                                                            group = splits[i];
                                                            if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                                                                groups.add(group);
                                                            }
                                                        }
                                                        info.groups = groups;
                                                    } else {
                                                        info.packageName = splits[0];
                                                        info.addAll = true;
                                                    }
                                                } else {
                                                    info.packageName = value;
                                                    info.addAll = true;
                                                }
                                                mOppoOverseaFixedPermInfos.add(info);
                                                break;
                                            }
                                        }
                                    }
                                    value = parser.nextText();
                                    if (value != null) {
                                        info = new RuntimePermFilterInfo();
                                        info.overSea = false;
                                        if (value.contains("#")) {
                                            splits = value.split("#");
                                            if (splits.length >= 2) {
                                                info.packageName = splits[0];
                                                info.addAll = false;
                                                groups = new ArrayList();
                                                for (i = 1; i < splits.length; i++) {
                                                    group = splits[i];
                                                    if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                                                        groups.add(group);
                                                    }
                                                }
                                                info.groups = groups;
                                            } else {
                                                info.packageName = splits[0];
                                                info.addAll = true;
                                            }
                                        } else {
                                            info.packageName = value;
                                            info.addAll = true;
                                        }
                                        mOppoFixedPermInfos.add(info);
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
                        String str = "android.intent.action.OPPO_PACKAGE_ADDED";
                        if (packageName != null) {
                            fromParts = Uri.fromParts("package", packageName, null);
                        } else {
                            fromParts = null;
                        }
                        Intent intent = new Intent(str, fromParts);
                        if (extras != null) {
                            intent.putExtras(extras);
                        }
                        String str2 = "oppo_extra_pkg_name";
                        if (installerPackageName == null) {
                            installerPackageName = IElsaManager.EMPTY_PACKAGE;
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
                    path = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str, path);
                str = "installerPackageName";
                if (installerPackageName == null) {
                    installerPackageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str, installerPackageName);
                str = "packageName";
                if (packageName == null) {
                    packageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str, packageName);
                String[] requiredPermissions = new String[1];
                requiredPermissions[0] = "oppo.permission.OPPO_COMPONENT_SAFE";
                am.broadcastIntent(null, intent, null, null, 0, null, null, requiredPermissions, -1, null, true, false, userId);
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
                    installerPackageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str2, installerPackageName);
                str2 = "packageName";
                if (packageName == null) {
                    packageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str2, packageName);
                String[] requiredPermissions = new String[1];
                requiredPermissions[0] = "oppo.permission.OPPO_COMPONENT_SAFE";
                am.broadcastIntent(null, intent, null, null, 0, null, null, requiredPermissions, -1, null, true, false, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void sendThirdAppLaunchBro(String packageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            try {
                Uri fromParts;
                String str = ACTION_OPPO_THIRD_APP_LAUNCH;
                if (packageName != null) {
                    fromParts = Uri.fromParts("package", packageName, null);
                } else {
                    fromParts = null;
                }
                Intent intent = new Intent(str, fromParts);
                String str2 = "packageName";
                if (packageName == null) {
                    packageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(str2, packageName);
                String[] requiredPermissions = new String[1];
                requiredPermissions[0] = "oppo.permission.OPPO_COMPONENT_SAFE";
                am.broadcastIntent(null, intent, null, null, 0, null, null, requiredPermissions, -1, null, true, false, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int getIconCacheMaxNum() {
        if (getTotalMemorySize() <= mIconCacheMinMemory) {
            mIconCacheMaxNum = 0;
        }
        Slog.d(TAG, "mIconCacheMaxNum:" + mIconCacheMaxNum + ",mIconCacheMinMemory:" + mIconCacheMinMemory);
        return mIconCacheMaxNum;
    }

    public static void sendDcsNonSilentInstallBroadcastExp(String packageName, boolean updateState, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            String OPPO_EXTRA_PID = OppoPackageManagerHelper.OPPO_EXTRA_PID;
            String OPPO_EXTRA_UID = OppoPackageManagerHelper.OPPO_EXTRA_UID;
            String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
            String OPPO_EXTRA_INSTALL_PACKAGE = "oppo_extra_install_package";
            String ACTION_OPPO_DCS_CALLER_INFO = "android.intent.action.oppo.dcs.caller.info";
            try {
                Intent intent = new Intent();
                intent.setAction(ACTION_OPPO_DCS_CALLER_INFO);
                intent.putExtra(OPPO_EXTRA_PID, -99);
                intent.putExtra(OPPO_EXTRA_UID, -99);
                intent.putExtra(OPPO_EXTRA_PKG_NAME, installerPackageName);
                intent.putExtra("android.intent.extra.REPLACING", updateState);
                if (packageName == null) {
                    packageName = IElsaManager.EMPTY_PACKAGE;
                }
                intent.putExtra(OPPO_EXTRA_INSTALL_PACKAGE, packageName);
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

    public static ArrayList<String> getIgnoreAppList() {
        return SKIP_APP_PACKAGE_LIST;
    }

    public static boolean isSafeCenterApp(String packageName) {
        boolean z = true;
        if (packageName == null) {
            return false;
        }
        if (mOppoHidePkgList.size() < 1) {
            if (!SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName)) {
                z = DEFAULT_OPPO_HIDE_APP_LIST.contains(packageName);
            }
            return z;
        }
        if (!SKIP_SAFECENTER_PACKAGE_LIST.contains(packageName)) {
            z = mOppoHidePkgList.contains(packageName);
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
                        if (OppoPackageManagerHelper.IsShareUid(shareName[0])) {
                            isSystemCaller = true;
                        }
                        String[] shareUidPkg = pm.getPackagesForUid(Integer.parseInt(shareName[1]));
                        if (!(shareUidPkg == null || shareUidPkg[0] == null)) {
                            isSystemCaller = IsOppoApkList(shareUidPkg[0]);
                        }
                    }
                } else {
                    isSystemCaller = IsOppoApkList(callerName);
                }
            }
            if (!isSystemCaller) {
                z = mOppoHidePkgList.contains(packageName);
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
        if (mOppoHidePkgList.size() < 1) {
            return DEFAULT_OPPO_HIDE_APP_LIST.contains(packageName);
        }
        return mOppoHidePkgList.contains(packageName);
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
            mNotLaunchedPkgs = readNotLaunchedListFromFile();
            for (String str : mNotLaunchedPkgs) {
                Slog.d(TAG, "init not launched ~~~~~ " + str);
            }
            return;
        }
        try {
            if (!file.createNewFile()) {
                Slog.i(TAG, "failed create file /data/system//notLaunchedPkgs.xml");
            }
            Runtime.getRuntime().exec("chmod 774 /data/system//notLaunchedPkgs.xml");
        } catch (IOException e) {
        }
    }

    private static ArrayList<String> readNotLaunchedListFromFile() {
        File file = new File(PATH_NOT_LAUNCHED_LIST, FILE_NOT_LAUNCHED_LIST);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "failed create file /data/system//notLaunchedPkgs.xml");
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
                    Runtime.getRuntime().exec("chmod 774 /data/system//notLaunchedPkgs.xml");
                } else {
                    Slog.i(TAG, "failed create file /data/system//notLaunchedPkgs.xml");
                    return;
                }
            } catch (IOException e) {
            }
        }
        writeDataToFile(file, list);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00ee A:{SYNTHETIC, Splitter: B:47:0x00ee} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c9 A:{SYNTHETIC, Splitter: B:41:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a5 A:{SYNTHETIC, Splitter: B:35:0x00a5} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0082 A:{SYNTHETIC, Splitter: B:29:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005f A:{SYNTHETIC, Splitter: B:23:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0113 A:{SYNTHETIC, Splitter: B:53:0x0113} */
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
        synchronized (mNotLaunchedPkgs) {
            if (!mNotLaunchedPkgs.contains(pkg)) {
                Slog.d(TAG, "addPkgToNotLaunchedList " + pkg);
                mNotLaunchedPkgs.add(pkg);
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
        synchronized (mNotLaunchedPkgs) {
            if (mNotLaunchedPkgs.contains(pkg)) {
                Slog.d(TAG, "removePkgFromNotLaunchedList " + pkg);
                mNotLaunchedPkgs.remove(pkg);
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
            if (Global.getInt(pms.mContext.getContentResolver(), "children_mode_on", 0) == 1 && !"com.coloros.childrenspace".equals(callingPkg) && filter.hasCategory("android.intent.category.HOME")) {
                Slog.d(TAG, "forbidden set launcher in children mode, skip!");
                return true;
            }
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
        if (!(callerName == null || callerName.contains("android.uid.nfc") || callerName.equals("com.android.cts.stub") || callerName.equals("com.android.cts.normalapp") || callerName.equals("com.android.cts.deviceandprofileowner") || (callerName.contains("android.uid.system") && isCtsAppInstall))) {
            z = true;
        }
        return z;
    }
}
