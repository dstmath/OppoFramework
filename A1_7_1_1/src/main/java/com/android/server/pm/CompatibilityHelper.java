package com.android.server.pm;

import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.server.LocationManagerService;
import com.android.server.oppo.IElsaManager;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
public class CompatibilityHelper extends RomUpdateHelper {
    private static final Map<String, Integer> ABI_TO_INT_MAP = null;
    private static final Map<Integer, String> ABI_TO_STRING_MAP = null;
    private static final String DATA_FILE_DIR = "data/system/oppo_cpt_list.xml";
    public static final String FILTER_NAME = "compatibility_whitelist_values";
    public static final int FORCE_CHOOSING_OLD_WEBVIEW_M = 17;
    public static final int FORCE_CHOOSING_OLD_WEBVIEW_N = 681;
    public static final int FORCE_CHOOSING_TARGETSDK_L = 25;
    public static final int FORCE_CHOOSING_TARGETSDK_M = 689;
    public static final int FORCE_CHOOSING_WEBVIEW = 11;
    public static final int FORCE_DELAY_DEXOPT = 18;
    public static final int FORCE_DELAY_DEXOPT_M = 27;
    public static final int FORCE_DEX2OAT_ROLLBACK = 684;
    public static final int FORCE_DEXOPT_IN_SPEED = 688;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_FOR_ACTIVITIES = 22;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_MTK = 16;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_QCOM = 15;
    public static final int FORCE_DISABLE_HYPNUS = 20;
    public static final int FORCE_DISABLE_OPENSSL = 683;
    public static final int FORCE_EFFECT_LIB_BY_OPENSSL = 24;
    public static final int FORCE_ENABLE_DEBUGGER = 682;
    public static final int FORCE_ENABLE_HARDWAREACCELERATE = 14;
    public static final int FORCE_ENABLE_HARDWAREACCELERATE_FOR_ACTIVITIES = 21;
    public static final int FORCE_FILTER_EXPLICIT_SERVICEINTENTCHECK = 3;
    public static final int FORCE_FILTER_INVALID_WIN_TYPE = 9;
    public static final int FORCE_FILTER_MESSAGE = 7;
    public static final int FORCE_FILTER_SERIALIZABLE_IMPLEMENT = 4;
    public static final int FORCE_FILTER_UNBIND_SERVICE = 5;
    public static final int FORCE_FILTER_WALLPAPER = 10;
    public static final int FORCE_IGNORE_DEXOPT = 8;
    public static final int FORCE_IGNORE_GSF = 6;
    public static final int FORCE_IN_SAFEMODE_DEX = 12;
    public static final int FORCE_IN_SAFEMODE_DEX_MTK = 13;
    public static final int FORCE_MINI_TRIMMEMORY = 678;
    public static final int FORCE_NEED_SPECIAL_LIBRARIES = 23;
    public static final int FORCE_NEED_SPECIAL_LIBRARIES_IN = 677;
    public static final int FORCE_RUNNING_IN_32_BIT_V5 = 2;
    public static final int FORCE_RUNNING_IN_32_BIT_V7 = 0;
    public static final int FORCE_RUNNING_IN_64_BIT = 1;
    public static final int FORCE_SKIP_OPENNDK_CHECK = 687;
    public static final int FORCE_SKIP_TOAST_CHECK = 685;
    public static final int GR_BLACK_LIST = 679;
    public static final int GR_WHITE_LIST = 680;
    public static final int IME_SKIP_TMP_DETACH = 686;
    private static final int LENGTH_OF_WHITELIST = 36;
    public static final int MM_MONEY_LUCKY_CHECK = 691;
    public static final int RUN_SCORE_BLACK_LIST = 690;
    private static final String SYS_FILE_DIR = "system/etc/oppo_cpt_list.xml";
    public static final String TAG_CP = "CompatibilityHelper";
    public static final String VERSION_NAME = "version";

    private class CompatibilityUpdateInfo extends UpdateInfo {
        private SparseArray<ArrayList<String>> mCmpWhiteList = new SparseArray();

        public CompatibilityUpdateInfo() {
            super(CompatibilityHelper.this);
        }

        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                StringReader stringReader = null;
                this.mCmpWhiteList.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader = new StringReader(content);
                    try {
                        parser.setInput(strReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    char[] typeChar = parser.getName().toCharArray();
                                    if (typeChar.length > 3) {
                                        eventType = parser.next();
                                        updateConfigVersion(String.valueOf(typeChar), parser.getText());
                                        break;
                                    }
                                    int type = char2int(typeChar);
                                    eventType = parser.next();
                                    if (type >= 0) {
                                        ArrayList<String> tmp = (ArrayList) this.mCmpWhiteList.get(type);
                                        if (tmp != null) {
                                            tmp.add(parser.getText());
                                            break;
                                        }
                                        tmp = new ArrayList();
                                        tmp.add(parser.getText());
                                        this.mCmpWhiteList.put(type, tmp);
                                        break;
                                    }
                                    continue;
                                default:
                                    break;
                            }
                        }
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e3) {
                                CompatibilityHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                        dealUpdate();
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        stringReader = strReader;
                    } catch (IOException e5) {
                        e3 = e5;
                        stringReader = strReader;
                    } catch (Throwable th2) {
                        th = th2;
                        stringReader = strReader;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    try {
                        CompatibilityHelper.this.log("Got execption parsing permissions.", e2);
                        if (stringReader != null) {
                            try {
                                stringReader.close();
                            } catch (IOException e32) {
                                CompatibilityHelper.this.log("Got execption close permReader.", e32);
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (stringReader != null) {
                            try {
                                stringReader.close();
                            } catch (IOException e322) {
                                CompatibilityHelper.this.log("Got execption close permReader.", e322);
                            }
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    e322 = e7;
                    CompatibilityHelper.this.log("Got execption parsing permissions.", e322);
                    if (stringReader != null) {
                        try {
                            stringReader.close();
                        } catch (IOException e3222) {
                            CompatibilityHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:15:0x0026 A:{SYNTHETIC, Splitter: B:15:0x0026} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x00b4 A:{SYNTHETIC, Splitter: B:41:0x00b4} */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x008f A:{SYNTHETIC, Splitter: B:31:0x008f} */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x00cd A:{SYNTHETIC, Splitter: B:48:0x00cd} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private long getContentVersion(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            long version = -1;
            if (content == null) {
                return -1;
            }
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                try {
                    parser.setInput(strReader2);
                    boolean found = false;
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                if (CompatibilityHelper.VERSION_NAME.equals(parser.getName())) {
                                    eventType = parser.next();
                                    Slog.d(CompatibilityHelper.TAG_CP, "eventType = " + eventType + ", text = " + parser.getText());
                                    version = (long) Integer.parseInt(parser.getText());
                                    found = true;
                                    break;
                                }
                                break;
                        }
                        if (found) {
                            if (strReader2 != null) {
                                try {
                                    strReader2.close();
                                } catch (IOException e3) {
                                    CompatibilityHelper.this.log("Got execption close permReader.", e3);
                                }
                            }
                            return version;
                        }
                    }
                    if (strReader2 != null) {
                    }
                    return version;
                } catch (XmlPullParserException e4) {
                    e2 = e4;
                    strReader = strReader2;
                    CompatibilityHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                    }
                    return -1;
                } catch (IOException e5) {
                    e3 = e5;
                    strReader = strReader2;
                    try {
                        CompatibilityHelper.this.log("Got execption parsing permissions.", e3);
                        if (strReader != null) {
                        }
                        return -1;
                    } catch (Throwable th2) {
                        th = th2;
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e32) {
                                CompatibilityHelper.this.log("Got execption close permReader.", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    strReader = strReader2;
                    if (strReader != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                CompatibilityHelper.this.log("Got execption parsing permissions.", e2);
                if (strReader != null) {
                    try {
                        strReader.close();
                    } catch (IOException e322) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e322);
                    }
                }
                return -1;
            } catch (IOException e7) {
                e322 = e7;
                CompatibilityHelper.this.log("Got execption parsing permissions.", e322);
                if (strReader != null) {
                    try {
                        strReader.close();
                    } catch (IOException e3222) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e3222);
                    }
                }
                return -1;
            }
        }

        private void updateConfigVersion(String type, String value) {
            Slog.d(CompatibilityHelper.TAG_CP, hashCode() + " updateConfigVersion, type = " + type + ", value = " + value);
            if (CompatibilityHelper.VERSION_NAME.equals(type)) {
                this.mVersion = (long) Integer.parseInt(value);
            }
        }

        public boolean updateToLowerVersion(String content) {
            long newVersion = getContentVersion(content);
            Slog.d(CompatibilityHelper.TAG_CP, "upateToLowerVersion, newVersion = " + newVersion + ", mVersion = " + this.mVersion);
            return newVersion < this.mVersion;
        }

        public boolean clone(UpdateInfo input) {
            SparseArray<ArrayList<String>> other = ((CompatibilityUpdateInfo) input).getAllList();
            if (other == null || other.size() == 0) {
                CompatibilityHelper.this.log("Source object is empty");
                return false;
            }
            this.mCmpWhiteList.clear();
            for (int i = 0; i < other.size(); i++) {
                int key = other.keyAt(i);
                this.mCmpWhiteList.put(key, (ArrayList) ((ArrayList) other.get(key)).clone());
            }
            return true;
        }

        public boolean insert(int type, String verifyStr) {
            ArrayList<String> tmp = (ArrayList) this.mCmpWhiteList.get(type);
            if (tmp == null) {
                return false;
            }
            tmp.add(verifyStr);
            return true;
        }

        public void clear() {
            this.mCmpWhiteList.clear();
        }

        int char2int(char[] in) {
            int out = 0;
            if (in.length < 1) {
                return -1;
            }
            for (int n = 0; n < in.length; n++) {
                out = (int) (((double) out) + (((double) (in[n] - 97)) * Math.pow(26.0d, (double) ((in.length - n) - 1))));
            }
            return out;
        }

        void dealUpdate() {
            enableDisableHypnus();
        }

        void enableDisableHypnus() {
            if (this.mCmpWhiteList.indexOfKey(20) < 0) {
                return;
            }
            if (((ArrayList) this.mCmpWhiteList.get(20)).contains("disable")) {
                SystemProperties.set("sys.enable.hypnus", "0");
            } else {
                SystemProperties.set("sys.enable.hypnus", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("CompatibilityInfo [").append(hashCode()).append(", version = ").append(getVersion()).append("]\n");
            for (int i = 0; i < this.mCmpWhiteList.size(); i++) {
                int key = this.mCmpWhiteList.keyAt(i);
                strBuilder.append("type = ").append(key);
                strBuilder.append(", value = ").append((ArrayList) this.mCmpWhiteList.get(key)).append("\n");
            }
            return strBuilder.toString();
        }

        public boolean isInWhiteList(int type, String verifyStr) {
            if (this.mCmpWhiteList.indexOfKey(type) < 0 || !((ArrayList) this.mCmpWhiteList.get(type)).contains(verifyStr)) {
                return false;
            }
            return true;
        }

        public ArrayList<String> getOneList(int type) {
            return (ArrayList) this.mCmpWhiteList.get(type);
        }

        public SparseArray<ArrayList<String>> getAllList() {
            return this.mCmpWhiteList;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.CompatibilityHelper.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.CompatibilityHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.CompatibilityHelper.<clinit>():void");
    }

    public CompatibilityHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new CompatibilityUpdateInfo(), new CompatibilityUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String dumpToString() {
        return ((CompatibilityUpdateInfo) getUpdateInfo(true)).dumpToString();
    }

    public void dump(PrintWriter pw, String[] args, int opti) {
        String cmd = args[1];
        boolean isConfVersion = "true".equals(SystemProperties.get("persist.version.confidential"));
        ComponentName cn;
        if ("insert".equals(cmd) && isConfVersion) {
            if (args.length != 4) {
                pw.println("Invalid arguements!");
                return;
            }
            try {
                if (insertValueInList(Integer.parseInt(args[2]), args[3])) {
                    pw.println("Success!");
                }
            } catch (NumberFormatException e) {
                pw.println("Invalid arguements!");
            }
        } else if ("webview".equals(cmd)) {
            String pkg = null;
            try {
                cn = new OppoActivityManager().getTopActivityComponentName();
            } catch (RemoteException e2) {
                log("getTopActivityComponentName RemoteException");
                cn = null;
            }
            if (cn != null) {
                pkg = cn.getPackageName();
                pw.println("Current package is : " + pkg);
            }
            if (isInWhiteList(FORCE_CHOOSING_OLD_WEBVIEW_N, pkg)) {
                pw.println("Package already in the list!");
            } else {
                insertValueInList(FORCE_CHOOSING_OLD_WEBVIEW_N, pkg);
            }
        } else if ("top".equals(cmd)) {
            try {
                cn = new OppoActivityManager().getTopActivityComponentName();
            } catch (RemoteException e3) {
                log("getTopActivityComponentName RemoteException");
                cn = null;
            }
            if (cn != null) {
                pw.println("PackageName:[" + cn.getPackageName() + "]");
            }
        } else {
            pw.println("I know nothing\n");
        }
    }

    public boolean isInWhiteList(int type, String verifyStr) {
        return isInWhiteList(type, verifyStr, false);
    }

    private boolean isInWhiteList(int type, String verifyStr, boolean b) {
        if (verifyStr == null) {
            return false;
        }
        CompatibilityUpdateInfo tempInfo = (CompatibilityUpdateInfo) getUpdateInfo(true);
        if (tempInfo == null) {
            return false;
        }
        if (type == 5) {
            verifyStr = extraServiceName(verifyStr);
        }
        if (type == 18 || type == 27) {
            b = true;
        }
        if (type == MM_MONEY_LUCKY_CHECK) {
            b = true;
        }
        if (!b) {
            return tempInfo.isInWhiteList(type, verifyStr);
        }
        ArrayList<String> tmp = tempInfo.getOneList(type);
        if (tmp != null) {
            return isContained(tmp, verifyStr);
        }
        return false;
    }

    public void customizePackageIfNeeded(Package pkg) {
        try {
            pkg.cpuAbiOverride = abiOverride(pkg.cpuAbiOverride, pkg.packageName);
            customizeHardwareAccelerateIfNeeded(pkg);
            customizeHardwareAccelerateForActivityIfNeeded(pkg);
            customizeVMSafeModeIfNeeded(pkg);
            customizeTargetSdkIfNeeded(pkg);
            customizePrivateFlagsIfNeeded(pkg);
        } catch (RuntimeException e) {
        }
    }

    private void customizePrivateFlagsIfNeeded(Package pkg) {
        ApplicationInfo applicationInfo;
        if (isInWhiteList(FORCE_MINI_TRIMMEMORY, pkg.packageName)) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= DumpState.DUMP_PREFERRED_XML;
        }
        if (isInWhiteList(FORCE_ENABLE_DEBUGGER, pkg.packageName)) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= 16384;
        }
        if (isInWhiteList(FORCE_DEX2OAT_ROLLBACK, pkg.packageName)) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= 32768;
        }
        if (isInWhiteList(FORCE_SKIP_TOAST_CHECK, pkg.packageName)) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= DumpState.DUMP_INSTALLS;
        }
        if (isInWhiteList(FORCE_SKIP_OPENNDK_CHECK, pkg.packageName)) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= DumpState.DUMP_INTENT_FILTER_VERIFIERS;
        }
    }

    private boolean isBaiduProtectedApk(long length, String abiString) {
        int index = 0;
        if (abiString != null) {
            index = ((Integer) ABI_TO_INT_MAP.get(abiString)).intValue();
        }
        switch (index) {
            case 0:
                if (length == 610128) {
                    return true;
                }
                break;
            case 1:
                if (length == 367076) {
                    return true;
                }
                break;
            case 2:
                if (length == 408028 || length == 412124 || length == 416220) {
                    return true;
                }
        }
        return false;
    }

    public void customizeNativeLibrariesIfNeeded(Package pkg) {
        ArrayList tmpList = new ArrayList();
        boolean bOpenssl = false;
        if (isInWhiteList(23, pkg.packageName)) {
            tmpList.add("openssl");
        }
        File dir = new File(pkg.applicationInfo.nativeLibraryDir);
        if (dir.isDirectory()) {
            for (File tmp : dir.listFiles()) {
                String libName = tmp.getName();
                if (libName != null) {
                    ApplicationInfo applicationInfo;
                    if (bOpenssl || !isInWhiteList(24, libName)) {
                        if ("libssl.so".equals(libName) || "libcrypto.so".equals(libName)) {
                            bOpenssl = true;
                        }
                    } else if (!tmpList.contains("openssl")) {
                        tmpList.add("openssl");
                    }
                    if ("libmg20pbase.so".equals(libName) && tmp.length() == 42156) {
                        applicationInfo = pkg.applicationInfo;
                        applicationInfo.privateFlags |= 16384;
                    }
                    if ("libbaiduprotect.so".equals(libName) && isBaiduProtectedApk(tmp.length(), pkg.applicationInfo.primaryCpuAbi)) {
                        applicationInfo = pkg.applicationInfo;
                        applicationInfo.privateFlags |= 32768;
                    }
                }
            }
            if (bOpenssl || isInWhiteList(FORCE_DISABLE_OPENSSL, pkg.packageName)) {
                tmpList.remove("openssl");
            }
        }
        if (tmpList.size() > 0) {
            pkg.applicationInfo.specialNativeLibraryDirs = (String[]) tmpList.toArray(new String[tmpList.size()]);
        }
        if (isInWhiteList(677, pkg.packageName)) {
            String opensslLibraryDir;
            if (VMRuntime.is64BitInstructionSet(InstructionSets.getPrimaryInstructionSet(pkg.applicationInfo))) {
                opensslLibraryDir = "/vendor/lib64/openssl";
            } else {
                opensslLibraryDir = "/vendor/lib/openssl";
            }
            pkg.applicationInfo.nativeLibraryDir = opensslLibraryDir + File.pathSeparator + pkg.applicationInfo.nativeLibraryDir;
        }
    }

    private void customizeHardwareAccelerateIfNeeded(Package pkg) {
        if (isInWhiteList(14, pkg.packageName)) {
            pkg.baseHardwareAccelerated = true;
        } else if (isInWhiteList(15, pkg.packageName)) {
            pkg.baseHardwareAccelerated = false;
        } else {
            return;
        }
        changeActivitiesHW(pkg.activities, pkg.baseHardwareAccelerated);
    }

    private void customizeHardwareAccelerateForActivityIfNeeded(Package pkg) {
        if (isInWhiteList(21, pkg.packageName, true)) {
            changeActivityHW(pkg.activities, pkg.packageName, true);
        } else if (isInWhiteList(22, pkg.packageName, true)) {
            changeActivityHW(pkg.activities, pkg.packageName, false);
        }
    }

    private void customizeVMSafeModeIfNeeded(Package pkg) {
        if (isInWhiteList(12, pkg.packageName)) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.flags |= 16384;
        }
    }

    private void customizeSpecialLibraryIfNeeded(Package pkg) {
        if (isInWhiteList(23, pkg.packageName)) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            String[] strArr = new String[1];
            strArr[0] = "openssl";
            applicationInfo.specialNativeLibraryDirs = strArr;
        }
    }

    private void customizeTargetSdkIfNeeded(Package pkg) {
        if (isInWhiteList(25, pkg.packageName)) {
            pkg.applicationInfo.targetSdkVersion = 22;
        }
        if (isInWhiteList(FORCE_CHOOSING_TARGETSDK_M, pkg.packageName)) {
            pkg.applicationInfo.targetSdkVersion = 23;
        }
    }

    private void changeActivitiesHW(ArrayList<Activity> activities, boolean enable) {
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityInfo activityInfo;
            if (enable) {
                activityInfo = ((Activity) activities.get(i)).info;
                activityInfo.flags |= 512;
            } else {
                activityInfo = ((Activity) activities.get(i)).info;
                activityInfo.flags &= -513;
            }
        }
    }

    private void changeActivityHW(ArrayList<Activity> activities, String pkgName, boolean enable) {
        String cmp = IElsaManager.EMPTY_PACKAGE;
        for (int i = activities.size() - 1; i >= 0; i--) {
            int i2;
            cmp = pkgName + "/" + ((Activity) activities.get(i)).className;
            if (enable) {
                i2 = 21;
            } else {
                i2 = 22;
            }
            if (isInWhiteList(i2, cmp)) {
                ActivityInfo activityInfo;
                if (enable) {
                    activityInfo = ((Activity) activities.get(i)).info;
                    activityInfo.flags |= 512;
                } else {
                    activityInfo = ((Activity) activities.get(i)).info;
                    activityInfo.flags &= -513;
                }
            }
        }
    }

    public String abiOverride(String packageAbiOverride, String pkgName) {
        if (pkgName == null) {
            return packageAbiOverride;
        }
        if (isInWhiteList(0, pkgName) && Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            return Build.SUPPORTED_32_BIT_ABIS[0];
        }
        if (isInWhiteList(1, pkgName) && Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            return Build.SUPPORTED_64_BIT_ABIS[0];
        }
        if (!isInWhiteList(2, pkgName) || Build.SUPPORTED_32_BIT_ABIS.length <= 1) {
            return packageAbiOverride;
        }
        return Build.SUPPORTED_32_BIT_ABIS[1];
    }

    public int convertAbi2Int(String abiString) {
        return ((Integer) ABI_TO_INT_MAP.get(abiString)).intValue();
    }

    public String convertAbi2String(int abiInt) {
        return (String) ABI_TO_STRING_MAP.get(Integer.valueOf(abiInt));
    }

    private String extraServiceName(String fullName) {
        if (fullName == null) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        String[] temp = fullName.split("\\$");
        if (temp[0] != null) {
            return temp[0].split("\\@")[0];
        }
        return IElsaManager.EMPTY_PACKAGE;
    }

    private boolean isContained(ArrayList<String> tmpList, String verifyStr) {
        int i = 0;
        while (i < tmpList.size()) {
            if (verifyStr != null && (verifyStr.contains((CharSequence) tmpList.get(i)) || ((String) tmpList.get(i)).contains(verifyStr))) {
                return true;
            }
            i++;
        }
        return false;
    }
}
