package com.android.server.pm;

import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageParser;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorSellModeManager implements IColorSellModeManager {
    private static final boolean DEBUG = true;
    private static final String FEATURE_NAME_EXP_SELLMODE = "oppo.specialversion.exp.sellmode";
    private static final String PACKAGE_NAME_DAYDREAMVIDEO = "com.oppo.daydreamvideo";
    private static final String TAG = "ColorSellModeManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorSellModeManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    public static ColorSellModeManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorSellModeManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorSellModeManager();
                }
            }
        }
        return sInstance;
    }

    private ColorSellModeManager() {
        Slog.d(TAG, "Constructor");
    }

    public void init(IColorPackageManagerServiceEx ex) {
        Slog.d(TAG, "init");
        this.mPmsEx = ex;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
    }

    public void clearSellModeIfNeeded() {
        PackageSetting ps;
        if (!this.mPms.hasSystemFeature(FEATURE_NAME_EXP_SELLMODE, 0) && (ps = this.mPms.mSettings.getPackageLPr(PACKAGE_NAME_DAYDREAMVIDEO)) != null && !ps.isSystem()) {
            Slog.d(TAG, "no sellmode feature, delete setting for com.oppo.daydreamvideo");
            synchronized (this.mPms.mPackages) {
                this.mPms.mSettings.removePackageLPw(PACKAGE_NAME_DAYDREAMVIDEO);
            }
        }
    }

    public void interceptScanSellModeIfNeeded(String packageName) throws PackageManagerException {
        if (PACKAGE_NAME_DAYDREAMVIDEO.equals(packageName) && !this.mPms.hasSystemFeature(FEATURE_NAME_EXP_SELLMODE, 0)) {
            PackageSetting ps = this.mPms.mSettings.getPackageLPr(packageName);
            if (ps == null || !ps.isSystem()) {
                Slog.d(TAG, "no sellmode feature, skip scan :" + packageName);
                throw new PackageManagerException(-2, "no sellmode feature for com.oppo.daydreamvideo");
            }
        }
    }

    public boolean interceptUninstallSellModeIfNeeded(PackageParser.Package pkg, IPackageDeleteObserver2 observer) {
        if (pkg == null || !PACKAGE_NAME_DAYDREAMVIDEO.equals(pkg.packageName) || !this.mPms.hasSystemFeature(FEATURE_NAME_EXP_SELLMODE, 0)) {
            return false;
        }
        Slog.d(TAG, "forbid delete com.oppo.daydreamvideo in sellmode mode");
        try {
            observer.onPackageDeleted(PACKAGE_NAME_DAYDREAMVIDEO, -1, (String) null);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
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
            m.invoke(cls.newInstance(), ColorSellModeManager.class.getName());
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
