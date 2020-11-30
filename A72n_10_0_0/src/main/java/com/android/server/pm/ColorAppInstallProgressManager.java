package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfoLite;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.ColorSmartDozeHelper;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ColorResourcePreloadDatabaseHelper;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorAppInstallProgressManager implements IColorAppInstallProgressManager {
    private static final String ACTION_OPPO_INSTALL_FAILED = "oppo.intent.action.OPPO_INSTALL_FAILED";
    private static final String ACTION_OPPO_START_INSTALL = "oppo.intent.action.OPPO_START_INSTALL";
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String PACKAGE_OPPO_LAUNCHER = "com.oppo.launcher";
    private static final String TAG = "ColorAppInstallProgressManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    private static ColorAppInstallProgressManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    private IColorActivityManagerServiceEx mAmsEx = null;
    boolean mDynamicDebug = EXP_VERSION;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    public static ColorAppInstallProgressManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAppInstallProgressManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAppInstallProgressManager();
                }
            }
        }
        return sInstance;
    }

    private ColorAppInstallProgressManager() {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "Constructor");
        }
    }

    public void init(IColorPackageManagerServiceEx pmsEx, IColorActivityManagerServiceEx amsEx) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "init");
        }
        this.mPmsEx = pmsEx;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
        this.mAmsEx = amsEx;
        IColorActivityManagerServiceEx iColorActivityManagerServiceEx = this.mAmsEx;
        if (iColorActivityManagerServiceEx != null) {
            this.mAms = iColorActivityManagerServiceEx.getActivityManagerService();
        }
    }

    public void sendOppoStartInstallBro(File stagedDir, String packageName, String installerPackageName, int userId, boolean isFromPackageInstaller) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "sendOppoStartInstallBro");
        }
        if (stagedDir == null || !stagedDir.exists()) {
            Slog.e(TAG, "stage directory error");
        } else if (TextUtils.isEmpty(packageName)) {
            Slog.e(TAG, "packageName is empty.");
        } else {
            String apkPath = stagedDir.getAbsolutePath() + "/base.apk";
            long token = Binder.clearCallingIdentity();
            try {
                sendOppoStartInstallBro(apkPath, installerPackageName, packageName, userId, isFromPackageInstaller);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void sendFailBroCauseByNeedVerify(String packageName, String installerPackageName, int userId) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "sendFailBroCauseByNeedVerify::packageName = " + packageName + ", installerPackageName = " + installerPackageName + ", userId = " + userId);
        }
        sendOppoInstallFailBro(packageName, installerPackageName, userId);
    }

    public void sendFailBroInCopyFinishStage(int ret, PackageInfoLite pkgLite, String installerPackageName, int userId) {
        if (this.DEBUG_SWITCH) {
            StringBuilder sb = new StringBuilder();
            sb.append("sendFailBroInCopyFinishStage::ret = ");
            sb.append(ret);
            sb.append(", packageName = ");
            sb.append(pkgLite != null ? pkgLite.packageName : "null");
            sb.append(", installerPackageName = ");
            sb.append(installerPackageName);
            sb.append(", userId = ");
            sb.append(userId);
            Slog.d(TAG, sb.toString());
        }
        if (ret != 1 && pkgLite != null && pkgLite.packageName != null) {
            Slog.d(TAG, "send install failed broadcast for " + pkgLite.packageName + ", ret = " + ret);
            sendOppoInstallFailBro(pkgLite.packageName, installerPackageName, userId);
        }
    }

    public void sendFailBroInInstallFinishStage(int ret, String packageName, String installerPackageName, int userId) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "sendFailBroInInstallFinishStage::ret = " + ret + ", packageName = " + packageName + ", installerPackageName = " + installerPackageName + ", userId = " + userId);
        }
        if (ret != 1) {
            sendOppoInstallFailBro(packageName, installerPackageName, userId);
        }
    }

    private void sendOppoStartInstallBro(String path, String installerPackageName, String packageName, int userId, boolean isFromPackageInstaller) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && path != null) {
            try {
                Intent intent = new Intent(ACTION_OPPO_START_INSTALL);
                intent.putExtra("apkPath", path);
                String str = "";
                intent.putExtra("installerPackageName", installerPackageName != null ? installerPackageName : str);
                if (packageName != null) {
                    str = packageName;
                }
                intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, str);
                intent.putExtra("isFromPackageInstaller", isFromPackageInstaller);
                intent.setPackage("com.oppo.launcher");
                am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, new String[]{"oppo.permission.OPPO_COMPONENT_SAFE"}, -1, (Bundle) null, true, (boolean) EXP_VERSION, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendOppoInstallFailBro(String packageName, String installerPackageName, int userId) {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null && packageName != null) {
            try {
                Intent intent = new Intent(ACTION_OPPO_INSTALL_FAILED, Uri.fromParts(BrightnessConstants.AppSplineXml.TAG_PACKAGE, packageName, null));
                intent.putExtra("installerPackageName", installerPackageName != null ? installerPackageName : "");
                intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, packageName);
                intent.setPackage("com.oppo.launcher");
                am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, new String[]{"oppo.permission.OPPO_COMPONENT_SAFE"}, -1, (Bundle) null, true, (boolean) EXP_VERSION, userId);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public int tryRetstrictPackgeInstall(String installerPackageName, PackageInfoLite pkgLite, IPackageInstallObserver2 observer) {
        try {
            String frontPackage = SystemProperties.get("oppo.dex.front.package");
            if (EXP_VERSION) {
                return -1;
            }
            if ((!"com.oppo.market".equals(installerPackageName) || !"com.oppo.market".equals(frontPackage)) && (!"com.nearme.gamecenter".equals(installerPackageName) || !"com.nearme.gamecenter".equals(frontPackage))) {
                return -1;
            }
            boolean frequencyAlways = true;
            if (Settings.Secure.getInt(this.mPms.mContext.getContentResolver(), "settings_install_authentication", 0) != 1 || pkgLite == null || pkgLite.packageName == null || this.mPms.mSettings.mPackages.get(pkgLite.packageName) != null) {
                return -1;
            }
            if (Settings.Secure.getInt(this.mPms.mContext.getContentResolver(), "settings_install_authentication_frequency", 0) != 0) {
                frequencyAlways = false;
            }
            long lastVerifyTime = Settings.Secure.getLong(this.mPms.mContext.getContentResolver(), "account_verify_time", -1);
            long interval = SystemClock.elapsedRealtime() - lastVerifyTime;
            boolean needVerify = true;
            if (!frequencyAlways && lastVerifyTime != -1 && interval <= ColorSmartDozeHelper.GPS_EXEPTION_TIME) {
                needVerify = EXP_VERSION;
            }
            if (!needVerify) {
                return -1;
            }
            if (observer != null) {
                observer.onPackageInstalled("", -111, (String) null, (Bundle) null);
            }
            OppoFeatureCache.get(IColorAppInstallProgressManager.DEFAULT).sendFailBroCauseByNeedVerify(pkgLite.packageName, installerPackageName, 0);
            return -111;
        } catch (Exception e) {
            Slog.w(TAG, "Failed to restrict oppo market", e);
            return -1;
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorAppInstallProgressManager.class.getName());
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
