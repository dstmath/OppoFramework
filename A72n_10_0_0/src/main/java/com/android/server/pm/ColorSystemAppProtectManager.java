package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorSystemAppProtectManager implements IColorSystemAppProtectManager {
    public static final String TAG = "ColorSystemAppProtectManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorSystemAppProtectManager sThirdPartyAppSignCheckManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    Context mContext;
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;

    private ColorSystemAppProtectManager() {
    }

    public static ColorSystemAppProtectManager getInstance() {
        if (sThirdPartyAppSignCheckManager == null) {
            sThirdPartyAppSignCheckManager = new ColorSystemAppProtectManager();
        }
        return sThirdPartyAppSignCheckManager;
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
        this.mContext = pmsEx.getContext();
    }

    public boolean skipScanInvalidSystemApp(PackageParser.Package pkg) {
        if (pkg == null || pkg.codePath == null || pkg.packageName == null) {
            Slog.w(TAG, "skipScanInvalidSystemApp because pkg.codePath or pkg.packageName is null");
            return true;
        }
        boolean isOppoApp = ColorPackageManagerHelper.isOppoApkList(pkg.packageName);
        String pkgCodePath = pkg.codePath;
        boolean isSystemAppNotOem = pkgCodePath.contains("/system/app") || pkgCodePath.contains("/system/priv-app") || pkgCodePath.contains("/vendor/app") || pkgCodePath.contains("/vendor/overlay");
        if (isOppoApp || !isSystemAppNotOem) {
            return false;
        }
        return true;
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
            m.invoke(cls.newInstance(), ColorSystemAppProtectManager.class.getName());
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
