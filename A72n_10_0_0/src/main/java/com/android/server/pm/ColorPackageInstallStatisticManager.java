package com.android.server.pm;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.PackageManagerService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorPackageInstallStatisticManager implements IColorPackageInstallStatisticManager {
    private static final String ACTION_OPPO_DCS_CALLER_INFO = "oppo.intent.action.oppo.dcs.caller.info";
    private static final String EXP_INSTALL_COLLECT_PROP = "oppo.exp.install.collect";
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String INSTALLER_INSTALL_ACTIVITY_EXP = "com.android.packageinstaller.InstallStart";
    private static final String INSTALLER_PKG_NAME_EXP = "com.google.android.packageinstaller";
    private static final String OEM_INSTALL_ACTIVITY = "com.android.packageinstaller.PackageInstallerActivity";
    private static final String OEM_UNINSTALL_ACTIVITY = "com.android.packageinstaller.UninstallerActivity";
    private static final String OPPO_EXTRA_DEBUG_INFO = "oppo_extra_debug_info";
    private static final String OPPO_EXTRA_INSTALL_PKG = "oppo_extra_install_package";
    private static final String OPPO_EXTRA_PID = "oppo_extra_pid";
    private static final String OPPO_EXTRA_PKG_NAME = "oppo_extra_pkg_name";
    private static final String OPPO_EXTRA_UID = "oppo_extra_uid";
    private static final String PACKAGE_NEARME_STATISTICS = "com.nearme.statistics.rom";
    public static final String TAG = "ColorPackageInstallStatisticManager";
    private static ColorPackageInstallStatisticManager sColorPackageInstallStatisticManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = EXP_VERSION;
    private PackageManagerService mPms = null;
    private ArrayMap<PackageManagerService.OriginInfo, String> mRunningInstallerPkgName = new ArrayMap<>();

    public static ColorPackageInstallStatisticManager getInstance() {
        if (sColorPackageInstallStatisticManager == null) {
            sColorPackageInstallStatisticManager = new ColorPackageInstallStatisticManager();
        }
        return sColorPackageInstallStatisticManager;
    }

    private ColorPackageInstallStatisticManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
    }

    public void packageInstallInfoCollect(boolean logCallerInfo, boolean logCallerAppInfo, int extraPid, int extraUid, int pid, int uid, String callingPackage, Intent intent) {
        ComponentName realActivity;
        if (sDebugfDetail) {
            Slog.d(TAG, "packageInstallInfoCollect called");
        }
        if (intent != null && intent.getComponent() != null && (realActivity = intent.getComponent()) != null) {
            if (OEM_INSTALL_ACTIVITY.equals(realActivity.getClassName()) || OEM_UNINSTALL_ACTIVITY.equals(realActivity.getClassName())) {
                intent.putExtra(OPPO_EXTRA_PID, extraPid);
                intent.putExtra(OPPO_EXTRA_UID, extraUid);
                intent.putExtra("oppo_extra_pkg_name", callingPackage != null ? callingPackage : "");
                StringBuffer stringBuffer = new StringBuffer("dataCollection debug info ");
                if (logCallerInfo) {
                    stringBuffer.append(" caller is null,");
                }
                if (logCallerAppInfo) {
                    stringBuffer.append(" callerApp is null");
                }
                stringBuffer.append(" pid ");
                stringBuffer.append(pid);
                stringBuffer.append(" uid ");
                stringBuffer.append(uid);
                stringBuffer.append(" callingPackage ");
                stringBuffer.append(callingPackage);
                intent.putExtra(OPPO_EXTRA_DEBUG_INFO, stringBuffer.toString());
            }
        }
    }

    public void packageInstallInfoCollectForExp(String callAppName, String callingPackage, Intent intent) {
        if (sDebugfDetail) {
            Slog.d(TAG, "packageInstallInfoCollect called");
        }
        if (callAppName != null && intent != null && intent.getComponent() != null) {
            ComponentName realActivityComp = intent.getComponent();
            ComponentName googleInstallActivityComp = new ComponentName(INSTALLER_PKG_NAME_EXP, INSTALLER_INSTALL_ACTIVITY_EXP);
            if (realActivityComp != null && googleInstallActivityComp.equals(realActivityComp) && !INSTALLER_PKG_NAME_EXP.equals(callAppName)) {
                Slog.d(TAG, "packageInstallInfoCollectForExp: callAppName" + callAppName + " callingPackage " + callingPackage);
                intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", callAppName);
                try {
                    SystemProperties.set(EXP_INSTALL_COLLECT_PROP, callAppName);
                } catch (Exception e) {
                }
            }
        }
    }

    public void sendNonSilentInstallBroadcastExp(String installerPackageName, PackageManagerService.OriginInfo origin, PackageManagerService.PackageInstalledInfo res, int childPackageCount, int childPackageIndex) {
        String extraInstallerExp;
        if (EXP_VERSION && installerPackageName != null && installerPackageName.equals(INSTALLER_PKG_NAME_EXP)) {
            synchronized (this.mRunningInstallerPkgName) {
                extraInstallerExp = this.mRunningInstallerPkgName.get(origin);
                if (childPackageCount <= 0 || childPackageIndex + 1 >= childPackageCount) {
                    this.mRunningInstallerPkgName.remove(origin);
                }
            }
            Slog.i(TAG, "origin : " + origin.toString() + " packageName: " + res.pkg.applicationInfo.packageName + " extraInstallerExp: " + extraInstallerExp);
            boolean update = (res.removedInfo == null || res.removedInfo.removedPackage == null) ? false : true;
            if (extraInstallerExp != null && extraInstallerExp.length() > 0) {
                sendDcsNonSilentInstallBroadcastCore(res.pkg.applicationInfo.packageName, update, extraInstallerExp, 0);
            }
        }
    }

    private void sendDcsNonSilentInstallBroadcastCore(String packageName, boolean updateState, String installerPackageName, int userId) {
        RemoteException ex;
        IActivityManager am = ActivityManager.getService();
        if (am != null && packageName != null) {
            try {
                Intent intent = new Intent();
                intent.setAction(ACTION_OPPO_DCS_CALLER_INFO);
                intent.setPackage(PACKAGE_NEARME_STATISTICS);
                intent.putExtra(OPPO_EXTRA_PID, -99);
                intent.putExtra(OPPO_EXTRA_UID, -99);
                intent.putExtra("oppo_extra_pkg_name", installerPackageName);
                intent.putExtra("android.intent.extra.REPLACING", updateState);
                intent.putExtra(OPPO_EXTRA_INSTALL_PKG, packageName);
                try {
                    am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, true, (boolean) EXP_VERSION, userId);
                } catch (RemoteException e) {
                    ex = e;
                }
            } catch (RemoteException e2) {
                ex = e2;
                ex.printStackTrace();
            }
        }
    }

    public void sendDcsSilentInstallBroadcast(String packageName, Bundle extras, String installerPackageName, int userId) {
        RemoteException ex;
        IActivityManager am = ActivityManager.getService();
        if (am != null && packageName != null) {
            if (installerPackageName != null && "com.android.packageinstaller".equals(installerPackageName)) {
                return;
            }
            if (installerPackageName == null || !INSTALLER_PKG_NAME_EXP.equals(installerPackageName)) {
                try {
                    Intent intent = new Intent("oppo.intent.action.OPPO_PACKAGE_ADDED", Uri.fromParts(BrightnessConstants.AppSplineXml.TAG_PACKAGE, packageName, null));
                    if (extras != null) {
                        try {
                            intent.putExtras(extras);
                        } catch (RemoteException e) {
                            ex = e;
                        }
                    }
                    intent.setPackage(PACKAGE_NEARME_STATISTICS);
                    intent.putExtra("oppo_extra_pkg_name", installerPackageName != null ? installerPackageName : "");
                    try {
                        am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, true, (boolean) EXP_VERSION, userId);
                    } catch (RemoteException e2) {
                        ex = e2;
                    }
                } catch (RemoteException e3) {
                    ex = e3;
                    ex.printStackTrace();
                }
            }
        }
    }

    public String addRunningInstallerPackageName(PackageManagerService.OriginInfo origin, String installerPackageName, int installerUid, boolean isCtsAppInstall) {
        if (EXP_VERSION && installerPackageName != null && installerPackageName.length() > 0) {
            String curExtraInstallPkgNameExp = SystemProperties.get(EXP_INSTALL_COLLECT_PROP, "");
            if (!"".equals(curExtraInstallPkgNameExp) && installerPackageName.equals(INSTALLER_PKG_NAME_EXP)) {
                Slog.i(TAG, "first origin : " + origin.toString() + " curExtraInstallPkgNameExp: " + curExtraInstallPkgNameExp);
                synchronized (this.mRunningInstallerPkgName) {
                    this.mRunningInstallerPkgName.put(origin, curExtraInstallPkgNameExp);
                }
            }
        }
        if (isCtsAppInstall) {
            return installerPackageName;
        }
        if (installerUid == 2000 || installerUid == 0) {
            installerPackageName = "pc";
        }
        if (installerPackageName == null || installerPackageName.equals("")) {
            return this.mPms.getNameForUid(installerUid);
        }
        return installerPackageName;
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
            m.invoke(cls.newInstance(), ColorPackageInstallStatisticManager.class.getName());
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
