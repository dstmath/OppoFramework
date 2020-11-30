package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorForbidHideOrDisableManager implements IColorForbidHideOrDisableManager {
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final String TAG = "ColorForbidHideOrDisableManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    private static ColorForbidHideOrDisableManager sForbidHideOrDisableManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = EXP_VERSION;
    private PackageManagerService mPms = null;

    private ColorForbidHideOrDisableManager() {
    }

    public static ColorForbidHideOrDisableManager getInstance() {
        if (sForbidHideOrDisableManager == null) {
            sForbidHideOrDisableManager = new ColorForbidHideOrDisableManager();
        }
        return sForbidHideOrDisableManager;
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
    }

    public boolean isPackageForbidHidden(boolean hidden, String packageName) {
        if ((this.mPms.hasSystemFeature("oppo.customize.function.hide_launcher", 0) && "com.oppo.launcher".equals(packageName)) || !hidden || !ColorPackageManagerHelper.inPmsWhiteList(ColorPackageManagerHelper.PROTECT_HIDE_APP_INDEX, packageName, ColorPackageManagerHelper.DEFAULT_PROTECT_HIDE_APP)) {
            return EXP_VERSION;
        }
        Slog.w(TAG, "Cannot hiding protect app " + packageName);
        return true;
    }

    public boolean isPackageForbidDisabled(int callingUid, int newState, String packageName) {
        if ((newState == 2 || newState == 3 || newState == 4) && "com.coloros.lockassistant".equals(packageName)) {
            Slog.w(TAG, "Cannot disable sim lock app " + packageName);
            return true;
        } else if (callingUid != 2000) {
            return EXP_VERSION;
        } else {
            if ((newState != 2 && newState != 3 && newState != 4) || !ColorPackageManagerHelper.inPmsWhiteList(ColorPackageManagerHelper.PROTECT_HIDE_APP_INDEX, packageName, ColorPackageManagerHelper.DEFAULT_PROTECT_HIDE_APP)) {
                return EXP_VERSION;
            }
            Slog.w(TAG, "Cannot disable protect app " + packageName);
            return true;
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
            m.invoke(cls.newInstance(), ColorForbidHideOrDisableManager.class.getName());
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
