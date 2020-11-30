package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorPackageInstallInterceptManager implements IColorPackageInstallInterceptManager {
    private static final String ADB_INSTALLER_STATUS_PATH = "/data/oppo/coloros/config/adb_installer_status.xml";
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String SYSTEM_CONFIG_PATH = "/data/oppo/coloros/config";
    public static final String TAG = "ColorPackageInstallInterceptManager";
    private static ColorPackageInstallInterceptManager sAdbInstallInterceptManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private FileObserverPolicy mAdbInstallerFileObserver = null;
    Context mContext;
    boolean mDynamicDebug = EXP_VERSION;
    public ArrayList<OppoAdbInstallerEntry> mOppoPackageInstallerList = new ArrayList<>();
    private PackageManagerService mPms = null;
    private boolean mSwitch = true;
    private boolean mSystemReady = EXP_VERSION;

    public static ColorPackageInstallInterceptManager getInstance() {
        if (sAdbInstallInterceptManager == null) {
            sAdbInstallInterceptManager = new ColorPackageInstallInterceptManager();
        }
        return sAdbInstallInterceptManager;
    }

    private ColorPackageInstallInterceptManager() {
    }

    public void systemReady() {
        initFile();
        readAdbInstallerFile();
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
        this.mContext = pmsEx.getContext();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x007a, code lost:
        if (handleForAdbSessionInstaller(r10, r2, r11, r8.installFlags) == false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x007c, code lost:
        return true;
     */
    public boolean allowInterceptAdbInstallInInstallStage(int installerUid, PackageInstaller.SessionParams sessionParams, File stagedDir, String packageName, IPackageInstallObserver2 observer) {
        boolean allowRootSilentInstall = SystemProperties.getBoolean("oppo.root.silent.install", true);
        if (!((sessionParams.installFlags & 32) == 0 || ((installerUid == 0 && (installerUid != 0 || allowRootSilentInstall)) || stagedDir == null || packageName == null))) {
            String path = stagedDir.getAbsolutePath() + "/base.apk";
            Slog.d(TAG, "installStage send adb install pkg:" + packageName + "   path: " + path + " installFlags:" + sessionParams.installFlags);
            synchronized (this.mPms.mPackages) {
                if ((sessionParams.installFlags & 2) != 0 && this.mPms.mPackages.containsKey(packageName)) {
                    Slog.d(TAG, "installStage send adb replace install, silent");
                    return EXP_VERSION;
                }
            }
        }
        if ((sessionParams.installFlags & 268435456) != 0) {
            Slog.d(TAG, "installStage from oppo adb installer, set INSTALL_FROM_ADB flag");
            sessionParams.installFlags |= 32;
            sessionParams.installFlags &= -268435457;
        }
        return EXP_VERSION;
    }

    public boolean allowInterceptSilentInstallerInStallStage(String installerPackageName, PackageInstaller.SessionParams sessionParams, File stagedDir, String packageName, IPackageInstallObserver2 observer, String installerPackageNameFromUid) {
        if (packageName == null || this.mPms.mSettings == null || this.mPms.mSettings.mPackages.get(packageName) != null || !ColorPackageManagerHelper.needVerifyInstall(this.mContext, installerPackageName) || "com.android.packageinstaller".equals(installerPackageNameFromUid) || stagedDir == null) {
            return EXP_VERSION;
        }
        String path = stagedDir.getAbsolutePath() + "/base.apk";
        Slog.d(TAG, "installStage handleForShopInstaller pkg:" + packageName + " path: " + path);
        if (handleForShopInstaller(this.mContext, installerPackageName, packageName, path, observer, sessionParams.installFlags)) {
            return true;
        }
        return EXP_VERSION;
    }

    private boolean handleForShopInstaller(Context ctx, String callerPackageName, String packageName, String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (ctx != null && packageName != null && apkPath != null) {
            return allowSendBroadcastForAdbInstall(ctx, callerPackageName, packageName, apkPath, observer, installFlags);
        }
        Slog.d(TAG, "handleForShopInstaller pms or packageName or apkPath = null!");
        return EXP_VERSION;
    }

    public void handForAdbSessionInstallerCancel(String packageName) {
        if (sDebugfDetail) {
            Slog.d(TAG, "handForAdbSessionInstallerCancel!!!");
        }
        if (this.mPms == null) {
            Slog.e(TAG, "handForAdbSessionInstallerCancel mPms = null !");
        } else if (packageName == null) {
            Slog.e(TAG, "handForAdbSessionInstallerCancel packageName = null !");
        } else {
            synchronized (this.mOppoPackageInstallerList) {
                int i = 0;
                while (i < this.mOppoPackageInstallerList.size()) {
                    if (this.mOppoPackageInstallerList.get(i).mPackageName.equals(packageName)) {
                        if (sDebugfDetail) {
                            Slog.d(TAG, "handAdbInstallCancel packageName == " + packageName);
                        }
                        try {
                            if (this.mOppoPackageInstallerList.get(i).mObserver != null) {
                                this.mOppoPackageInstallerList.get(i).mObserver.onPackageInstalled(packageName, -99, (String) null, (Bundle) null);
                            }
                        } catch (RemoteException e) {
                        }
                        this.mOppoPackageInstallerList.remove(i);
                        i--;
                    }
                    i++;
                }
            }
        }
    }

    public boolean handleForAdbSessionInstaller(String packageName, String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (EXP_VERSION) {
            Slog.d(TAG, "the version isn't CN!");
            return EXP_VERSION;
        } else if (!this.mSwitch) {
            Slog.d(TAG, "handleForAdbSessionInstaller mSwitch = false !");
            return EXP_VERSION;
        } else {
            if (!this.mSystemReady) {
                this.mSystemReady = ActivityManagerNative.isSystemReady();
                if (!this.mSystemReady) {
                    Slog.d(TAG, "System is not ready!");
                    return EXP_VERSION;
                }
            }
            if (this.mPms == null) {
                Slog.d(TAG, "handleForAdbSessionInstaller mPms = null !");
                return EXP_VERSION;
            } else if (packageName == null || apkPath == null) {
                Slog.d(TAG, "handleForAdbSessionInstaller packageName or apkPath = null !");
                return EXP_VERSION;
            } else {
                if (sDebugfDetail) {
                    Slog.d(TAG, "installStage INSTALL_FROM_ADB !");
                }
                if ("com.example.helloworld".equals(packageName) && ColorPackageManagerHelper.isCtsAppFileBySig(packageName, apkPath)) {
                    if (sDebugfDetail) {
                        Slog.d(TAG, "skip adb intercept for " + packageName);
                    }
                    return EXP_VERSION;
                } else if (ColorPackageManagerHelper.isCtsAppFileByPkgName(packageName) && !"com.example.helloworld".equals(packageName)) {
                    if (sDebugfDetail) {
                        Slog.d(TAG, "skip adb intercept for " + packageName);
                    }
                    return EXP_VERSION;
                } else if (packageName.equals("com.android.cts.priv.ctsshim") || packageName.equals("com.android.cts.ctsshim")) {
                    if (sDebugfDetail) {
                        Slog.d(TAG, "skip adb intercept for " + packageName);
                    }
                    return EXP_VERSION;
                } else {
                    if (sDebugfDetail) {
                        Slog.d(TAG, "call installer for " + packageName);
                    }
                    return allowSendBroadcastForAdbInstall(this.mContext, null, packageName, apkPath, observer, installFlags);
                }
            }
        }
    }

    private boolean allowSendBroadcastForAdbInstall(Context ctx, String callerPackageName, String packageName, String apkPath, IPackageInstallObserver2 observer, int installFlags) {
        if (!new File(apkPath).exists() && !apkPath.startsWith("/storage") && !apkPath.startsWith("/sdcard")) {
            return EXP_VERSION;
        }
        Intent intent = new Intent("oppo.intent.action.OPPO_INSTALL_FROM_ADB");
        intent.addFlags(16777216);
        intent.putExtra("apkPath", apkPath);
        if (callerPackageName != null) {
            intent.putExtra("callerpkg", callerPackageName);
        }
        intent.putExtra("installFlags", installFlags);
        ctx.sendBroadcastAsUser(intent, new UserHandle(ActivityManager.getCurrentUser()));
        OppoAdbInstallerEntry oaie = OppoAdbInstallerEntry.Builder(apkPath, observer, packageName);
        synchronized (this.mOppoPackageInstallerList) {
            this.mOppoPackageInstallerList.add(oaie);
        }
        return true;
    }

    public void handleForAdbSessionInstallerObserver(String packageName, int ret) {
        if (this.mPms == null) {
            Slog.d(TAG, "handleForAdbSessionInstallerObserver mPms = null !");
        } else if (packageName == null) {
            Slog.d(TAG, "handleForAdbSessionInstallerObserver packageName = null !");
        } else {
            if (sDebugfDetail) {
                Slog.d(TAG, "handleForAdbInstallerObserver packageName = " + packageName);
            }
            synchronized (this.mOppoPackageInstallerList) {
                int i = 0;
                while (i < this.mOppoPackageInstallerList.size()) {
                    if (this.mOppoPackageInstallerList.get(i).mPackageName.equals(packageName)) {
                        try {
                            if (this.mOppoPackageInstallerList.get(i).mObserver != null) {
                                this.mOppoPackageInstallerList.get(i).mObserver.onPackageInstalled(packageName, ret, (String) null, (Bundle) null);
                            }
                        } catch (RemoteException e) {
                        }
                        this.mOppoPackageInstallerList.remove(i);
                        i--;
                    }
                    i++;
                }
            }
        }
    }

    private void initFile() {
        Slog.i(TAG, "initFile start");
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
            Slog.e(TAG, "initFile failed!!!");
            e.printStackTrace();
        }
        this.mAdbInstallerFileObserver = new FileObserverPolicy(ADB_INSTALLER_STATUS_PATH);
        this.mAdbInstallerFileObserver.startWatching();
    }

    public void readAdbInstallerFile() {
        File adbInstallerStatusFile = new File(ADB_INSTALLER_STATUS_PATH);
        if (!adbInstallerStatusFile.exists()) {
            this.mSwitch = true;
            initFile();
            return;
        }
        this.mSwitch = readFromStatusFileLocked(adbInstallerStatusFile);
    }

    private boolean readFromStatusFileLocked(File adbInstallerStatusFile) {
        StringBuilder sb;
        int type;
        String str;
        Slog.i(TAG, "readFromStatusFileLocked!!!");
        FileInputStream stream = null;
        boolean status = true;
        try {
            FileInputStream stream2 = new FileInputStream(adbInstallerStatusFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (str = parser.getAttributeValue(null, "att")) != null) {
                    Slog.i(TAG, "readFromStatusFileLocked  status == " + str);
                    status = Boolean.parseBoolean(str);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            Slog.e(TAG, "failed parsing ", e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Slog.e(TAG, "failed parsing ", e4);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Slog.e(TAG, "failed parsing ", e6);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Slog.e(TAG, "failed IOException ", e8);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Slog.e(TAG, "failed parsing ", e10);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return status;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
        return status;
    }

    public void saveAdbInstallerStatusFile(boolean status) {
        if (sDebugfDetail) {
            Slog.i(TAG, "saveAdbInstallerStatusFile start");
        }
        writeToStatusFileLocked(new File(ADB_INSTALLER_STATUS_PATH), status);
    }

    private void writeToStatusFileLocked(File adbInstallerStatusFile, boolean status) {
        StringBuilder sb;
        if (sDebugfDetail) {
            Slog.i(TAG, "writeToStatusFileLocked!!!");
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(adbInstallerStatusFile);
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream2, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "gs");
            String str = String.valueOf(status);
            if (str != null) {
                out.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                out.attribute(null, "att", str);
                out.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
            }
            out.endTag(null, "gs");
            out.endDocument();
            try {
                stream2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Slog.e(TAG, sb.toString());
        } catch (IOException e2) {
            Slog.e(TAG, "Failed to write IOException: " + e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e4);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorPackageInstallInterceptManager.ADB_INSTALLER_STATUS_PATH)) {
                Slog.i(ColorPackageInstallInterceptManager.TAG, "onEvent: mFocusPath = ADB_INSTALLER_STATUS_PATH");
                ColorPackageInstallInterceptManager.this.readAdbInstallerFile();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class OppoAdbInstallerEntry {
        private static final String TAG = "OppoAdbInstallerEntry";
        public String mApkPath = "";
        public IPackageInstallObserver2 mObserver = null;
        public String mPackageName = "";

        private OppoAdbInstallerEntry() {
        }

        public static OppoAdbInstallerEntry Builder(String apkPath, IPackageInstallObserver2 obs) {
            OppoAdbInstallerEntry oaie = new OppoAdbInstallerEntry();
            oaie.mApkPath = apkPath;
            oaie.mObserver = obs;
            return oaie;
        }

        public static OppoAdbInstallerEntry Builder(String apkPath, IPackageInstallObserver2 obs, String packageName) {
            OppoAdbInstallerEntry oaie = new OppoAdbInstallerEntry();
            oaie.mApkPath = apkPath;
            oaie.mObserver = obs;
            oaie.mPackageName = packageName;
            return oaie;
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorPackageInstallInterceptManager.class.getName());
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
