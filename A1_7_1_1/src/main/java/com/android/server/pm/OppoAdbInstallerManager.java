package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver2;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
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
public class OppoAdbInstallerManager {
    private static final String ADB_INSTALLER_STATUS_PATH = "/data/system/config/adb_installer_status.xml";
    public static boolean DEBUG_DETAIL = false;
    private static final String SYSTEM_CONFIG_PATH = "/data/system/config";
    public static final String TAG = "OppoAdbInstallerManager";
    private static OppoAdbInstallerManager mOppoAdbInstallerManager;
    private String[] CTS_APKNAME_KEY;
    private FileObserverPolicy mAdbInstallerFileObserver;
    private PackageManagerService mPms;
    private boolean mSwitch;
    private boolean mSystemReady;
    public String mVersion;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoAdbInstallerManager.ADB_INSTALLER_STATUS_PATH)) {
                Log.i("OppoAdbInstallerManager", "onEvent: focusPath = ADB_INSTALLER_STATUS_PATH");
                OppoAdbInstallerManager.this.readAdbInstallerFile();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OppoAdbInstallerManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OppoAdbInstallerManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.OppoAdbInstallerManager.<clinit>():void");
    }

    public OppoAdbInstallerManager() {
        this.mPms = null;
        this.mSwitch = true;
        this.mSystemReady = false;
        this.mAdbInstallerFileObserver = null;
        this.mVersion = SystemProperties.get("ro.oppo.version", "CN");
        String[] strArr = new String[8];
        strArr[0] = "android.core.tests";
        strArr[1] = "Cts";
        strArr[2] = "com.drawelements.deqp";
        strArr[3] = "com.replica.replicaisland";
        strArr[4] = "TestDeviceSetup";
        strArr[5] = "Xts";
        strArr[6] = "NotificationBot";
        strArr[7] = "Gts";
        this.CTS_APKNAME_KEY = strArr;
    }

    public static final OppoAdbInstallerManager getInstance() {
        if (mOppoAdbInstallerManager == null) {
            mOppoAdbInstallerManager = new OppoAdbInstallerManager();
        }
        return mOppoAdbInstallerManager;
    }

    public void init(PackageManagerService pms) {
        this.mPms = pms;
        initFile();
        readAdbInstallerFile();
    }

    public void handForAdbInstallerCancel(String apkPath) {
        if (DEBUG_DETAIL) {
            Log.d("OppoAdbInstallerManager", "handAdbInstallCancel!!!");
        }
        if (this.mPms == null) {
            Log.e("OppoAdbInstallerManager", "handAdbInstallCancel mPms = null !");
            return;
        }
        synchronized (this.mPms.mOppoPackageInstallerList) {
            int i = 0;
            while (i < this.mPms.mOppoPackageInstallerList.size()) {
                if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mApkPath.equals(apkPath)) {
                    try {
                        if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver != null) {
                            ((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver.onPackageInstalled(IElsaManager.EMPTY_PACKAGE, -99, null, null);
                        }
                    } catch (RemoteException e) {
                    }
                    this.mPms.mOppoPackageInstallerList.remove(i);
                    i--;
                }
                i++;
            }
        }
    }

    public boolean handleForAdbInstaller(String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (!this.mVersion.equals("CN")) {
            Log.d("OppoAdbInstallerManager", "the version isn't CN!");
            return false;
        } else if (this.mSwitch) {
            if (!this.mSystemReady) {
                this.mSystemReady = ActivityManagerNative.isSystemReady();
                if (!this.mSystemReady) {
                    Log.d("OppoAdbInstallerManager", "System is not ready!");
                    return false;
                }
            }
            if (this.mPms == null) {
                Log.d("OppoAdbInstallerManager", "handleForAdbInstaller mPms = null !");
                return false;
            }
            if (DEBUG_DETAIL) {
                Log.d("OppoAdbInstallerManager", "installPackageAsUser INSTALL_FROM_ADB !");
            }
            if (!new File(apkPath).exists() && !apkPath.startsWith("/storage") && !apkPath.startsWith("/sdcard")) {
                Log.d("OppoAdbInstallerManager", apkPath + "  file is not exists!");
                return false;
            } else if (isCtsApk(apkPath)) {
                return false;
            } else {
                Intent intent = new Intent("android.intent.action.OPPO_INSTALL_FROM_ADB");
                intent.putExtra("apkPath", apkPath);
                intent.putExtra("installFlags", installFlags);
                this.mPms.mContext.sendBroadcast(intent);
                OppoAdbInstallerEntry oaie = OppoAdbInstallerEntry.Builder(apkPath, observer);
                synchronized (this.mPms.mOppoPackageInstallerList) {
                    this.mPms.mOppoPackageInstallerList.add(oaie);
                }
                return true;
            }
        } else {
            Log.d("OppoAdbInstallerManager", "handleForAdbInstaller mSwitch = false !");
            return false;
        }
    }

    public void handleForAdbInstallerObserver(String apkPath, int ret) {
        if (this.mPms == null) {
            Log.d("OppoAdbInstallerManager", "handleForAdbInstallerObserver mPms = null !");
            return;
        }
        if (DEBUG_DETAIL) {
            Log.d("OppoAdbInstallerManager", "handleForAdbInstallerObserver apkPath = " + apkPath);
        }
        synchronized (this.mPms.mOppoPackageInstallerList) {
            int i = 0;
            while (i < this.mPms.mOppoPackageInstallerList.size()) {
                if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mApkPath.equals(apkPath)) {
                    try {
                        if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver != null) {
                            ((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver.onPackageInstalled(IElsaManager.EMPTY_PACKAGE, ret, null, null);
                        }
                    } catch (RemoteException e) {
                    }
                    this.mPms.mOppoPackageInstallerList.remove(i);
                    i--;
                }
                i++;
            }
        }
    }

    public void handForAdbSessionInstallerCancel(String packageName) {
        if (DEBUG_DETAIL) {
            Log.d("OppoAdbInstallerManager", "handForAdbSessionInstallerCancel!!!");
        }
        if (this.mPms == null) {
            Log.e("OppoAdbInstallerManager", "handForAdbSessionInstallerCancel mPms = null !");
            return;
        }
        synchronized (this.mPms.mOppoPackageInstallerList) {
            int i = 0;
            while (i < this.mPms.mOppoPackageInstallerList.size()) {
                if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mPackageName.equals(packageName)) {
                    if (DEBUG_DETAIL) {
                        Log.d("OppoAdbInstallerManager", "handAdbInstallCancel packageName == " + packageName);
                    }
                    try {
                        if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver != null) {
                            ((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver.onPackageInstalled(packageName, -99, null, null);
                        }
                    } catch (RemoteException e) {
                    }
                    this.mPms.mOppoPackageInstallerList.remove(i);
                    i--;
                }
                i++;
            }
        }
    }

    public boolean handleForAdbSessionInstaller(String packageName, String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (!this.mVersion.equals("CN")) {
            Log.d("OppoAdbInstallerManager", "the version isn't CN!");
            return false;
        } else if (this.mSwitch) {
            if (!this.mSystemReady) {
                this.mSystemReady = ActivityManagerNative.isSystemReady();
                if (!this.mSystemReady) {
                    Log.d("OppoAdbInstallerManager", "System is not ready!");
                    return false;
                }
            }
            if (this.mPms == null) {
                Log.d("OppoAdbInstallerManager", "handleForAdbSessionInstaller mPms = null !");
                return false;
            } else if (packageName == null || apkPath == null) {
                Log.d("OppoAdbInstallerManager", "handleForAdbSessionInstaller packageName or apkPath = null !");
                return false;
            } else {
                if (DEBUG_DETAIL) {
                    Log.d("OppoAdbInstallerManager", "installStage INSTALL_FROM_ADB !");
                }
                if (!new File(apkPath).exists() && !apkPath.startsWith("/storage") && !apkPath.startsWith("/sdcard")) {
                    Log.d("OppoAdbInstallerManager", apkPath + "  file is not exists!");
                    return false;
                } else if (ColorPackageManagerHelper.IsCtsApp(packageName)) {
                    if (DEBUG_DETAIL) {
                        Log.d("OppoAdbInstallerManager", "skip adb intercept for ctsapp " + packageName);
                    }
                    return false;
                } else if (packageName.equals("com.android.cts.priv.ctsshim") || packageName.equals("com.android.cts.ctsshim")) {
                    if (DEBUG_DETAIL) {
                        Log.d("OppoAdbInstallerManager", "skip adb intercept for ctsapp " + packageName);
                    }
                    return false;
                } else {
                    Intent intent = new Intent("android.intent.action.OPPO_INSTALL_FROM_ADB");
                    intent.putExtra("apkPath", apkPath);
                    intent.putExtra("installFlags", installFlags);
                    this.mPms.mContext.sendBroadcast(intent);
                    OppoAdbInstallerEntry oaie = OppoAdbInstallerEntry.Builder(apkPath, observer, packageName);
                    synchronized (this.mPms.mOppoPackageInstallerList) {
                        this.mPms.mOppoPackageInstallerList.add(oaie);
                    }
                    return true;
                }
            }
        } else {
            Log.d("OppoAdbInstallerManager", "handleForAdbSessionInstaller mSwitch = false !");
            return false;
        }
    }

    public void handleForAdbSessionInstallerObserver(String packageName, int ret) {
        if (this.mPms == null) {
            Log.d("OppoAdbInstallerManager", "handleForAdbSessionInstallerObserver mPms = null !");
        } else if (packageName == null) {
            Log.d("OppoAdbInstallerManager", "handleForAdbSessionInstallerObserver packageName = null !");
        } else {
            if (DEBUG_DETAIL) {
                Log.d("OppoAdbInstallerManager", "handleForAdbInstallerObserver packageName = " + packageName);
            }
            synchronized (this.mPms.mOppoPackageInstallerList) {
                int i = 0;
                while (i < this.mPms.mOppoPackageInstallerList.size()) {
                    if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mPackageName.equals(packageName)) {
                        try {
                            if (((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver != null) {
                                ((OppoAdbInstallerEntry) this.mPms.mOppoPackageInstallerList.get(i)).mObserver.onPackageInstalled(packageName, ret, null, null);
                            }
                        } catch (RemoteException e) {
                        }
                        this.mPms.mOppoPackageInstallerList.remove(i);
                        i--;
                    }
                    i++;
                }
            }
        }
    }

    public boolean isCtsApk(String apkPath) {
        int i = 0;
        boolean result = false;
        String apkName = IElsaManager.EMPTY_PACKAGE;
        if (apkPath == null || apkPath.length() == 0) {
            return false;
        }
        try {
            int index = apkPath.lastIndexOf("/");
            if (index >= 0) {
                apkName = apkPath.substring(index + 1);
                String[] strArr = this.CTS_APKNAME_KEY;
                int length = strArr.length;
                while (i < length) {
                    if (apkName.startsWith(strArr[i])) {
                        if (DEBUG_DETAIL) {
                            Log.d("OppoAdbInstallerManager", "isCtsApk apkName == " + apkName);
                        }
                        result = true;
                    } else {
                        i++;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d("OppoAdbInstallerManager", "isCtsApk apkName == " + apkName);
            e.printStackTrace();
        }
        return result;
    }

    private void initFile() {
        Log.i("OppoAdbInstallerManager", "initFile start");
        File systemConfigPath = new File(SYSTEM_CONFIG_PATH);
        File adbInstallerPath = new File(ADB_INSTALLER_STATUS_PATH);
        try {
            if (!systemConfigPath.exists()) {
                systemConfigPath.mkdirs();
            }
            if (!adbInstallerPath.exists()) {
                adbInstallerPath.createNewFile();
                saveAdbInstallerStatusFile(true);
            }
        } catch (IOException e) {
            Log.e("OppoAdbInstallerManager", "initFile failed!!!");
            e.printStackTrace();
        }
        this.mAdbInstallerFileObserver = new FileObserverPolicy(ADB_INSTALLER_STATUS_PATH);
        this.mAdbInstallerFileObserver.startWatching();
    }

    public void readAdbInstallerFile() {
        File adbInstallerStatusFile = new File(ADB_INSTALLER_STATUS_PATH);
        if (adbInstallerStatusFile.exists()) {
            this.mSwitch = readFromStatusFileLocked(adbInstallerStatusFile);
            return;
        }
        this.mSwitch = true;
        initFile();
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x015a A:{SYNTHETIC, Splitter: B:67:0x015a} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0137 A:{SYNTHETIC, Splitter: B:60:0x0137} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x010b A:{SYNTHETIC, Splitter: B:51:0x010b} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00df A:{SYNTHETIC, Splitter: B:42:0x00df} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b3 A:{SYNTHETIC, Splitter: B:33:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0087 A:{SYNTHETIC, Splitter: B:24:0x0087} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readFromStatusFileLocked(File adbInstallerStatusFile) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Log.i("OppoAdbInstallerManager", "readFromStatusFileLocked!!!");
        FileInputStream stream = null;
        boolean z = true;
        try {
            FileInputStream stream2 = new FileInputStream(adbInstallerStatusFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String str = parser.getAttributeValue(null, "att");
                            if (str != null) {
                                Log.i("OppoAdbInstallerManager", "readFromStatusFileLocked  status == " + str);
                                z = Boolean.parseBoolean(str);
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e6);
                    }
                }
                return z;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                Log.e("OppoAdbInstallerManager", "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e62);
                    }
                }
                return true;
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.e("OppoAdbInstallerManager", "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e622);
                    }
                }
                return true;
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.e("OppoAdbInstallerManager", "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e6222);
                    }
                }
                return true;
            } catch (IOException e10) {
                e6222 = e10;
                stream = stream2;
                Log.e("OppoAdbInstallerManager", "failed IOException ", e6222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e62222);
                    }
                }
                return true;
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                try {
                    Log.e("OppoAdbInstallerManager", "failed parsing ", e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e622222) {
                            Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e622222);
                        }
                    }
                    return true;
                } catch (Throwable th) {
                    if (stream != null) {
                    }
                    return true;
                }
            } catch (Throwable th2) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e6222222);
                    }
                }
                return true;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.e("OppoAdbInstallerManager", "failed parsing ", e2);
            if (stream != null) {
            }
            return true;
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.e("OppoAdbInstallerManager", "failed parsing ", e3);
            if (stream != null) {
            }
            return true;
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.e("OppoAdbInstallerManager", "failed parsing ", e4);
            if (stream != null) {
            }
            return true;
        } catch (IOException e15) {
            e6222222 = e15;
            Log.e("OppoAdbInstallerManager", "failed IOException ", e6222222);
            if (stream != null) {
            }
            return true;
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.e("OppoAdbInstallerManager", "failed parsing ", e5);
            if (stream != null) {
            }
            return true;
        }
    }

    public void saveAdbInstallerStatusFile(boolean status) {
        if (DEBUG_DETAIL) {
            Log.i("OppoAdbInstallerManager", "saveAdbInstallerStatusFile start");
        }
        writeToStatusFileLocked(new File(ADB_INSTALLER_STATUS_PATH), status);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0092 A:{SYNTHETIC, Splitter: B:21:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b5 A:{SYNTHETIC, Splitter: B:27:0x00b5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeToStatusFileLocked(File adbInstallerStatusFile, boolean status) {
        IOException e;
        Throwable th;
        if (DEBUG_DETAIL) {
            Log.i("OppoAdbInstallerManager", "writeToStatusFileLocked!!!");
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(adbInstallerStatusFile);
            try {
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "gs");
                String str = String.valueOf(status);
                if (str != null) {
                    out.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                    out.attribute(null, "att", str);
                    out.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                }
                out.endTag(null, "gs");
                out.endDocument();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e2);
                    }
                }
                stream = stream2;
            } catch (IOException e3) {
                e2 = e3;
                stream = stream2;
                try {
                    Log.e("OppoAdbInstallerManager", "Failed to write IOException: " + e2);
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e22);
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
        } catch (IOException e4) {
            e22 = e4;
            Log.e("OppoAdbInstallerManager", "Failed to write IOException: " + e22);
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e222) {
                    Log.e("OppoAdbInstallerManager", "Failed to close state FileInputStream " + e222);
                }
            }
        }
    }
}
