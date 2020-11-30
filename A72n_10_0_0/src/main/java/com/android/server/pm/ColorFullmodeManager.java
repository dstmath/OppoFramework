package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorFullmodeManager implements IColorFullmodeManager {
    public static final String TAG = "ColorFullmodeManager";
    private static ColorFullmodeManager sColorFullmodeManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    boolean mDynamicDebug = false;
    private boolean mFullMode = false;
    private PackageManagerService mPms = null;

    public static ColorFullmodeManager getInstance() {
        if (sColorFullmodeManager == null) {
            sColorFullmodeManager = new ColorFullmodeManager();
        }
        return sColorFullmodeManager;
    }

    private ColorFullmodeManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        registerLogModule();
    }

    public boolean isClosedSuperFirewall() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isClosedSuperFirewall is " + this.mFullMode);
        }
        return this.mFullMode;
    }

    public void setClosedSuperFirewall(boolean mode) {
        if (this.DEBUG_SWITCH) {
            Slog.i(TAG, "setClosedSuperFirewall is " + mode);
        }
        if (this.mPms.mContext != null) {
            this.mPms.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setClosedSuperFirewall");
            this.mFullMode = mode;
        }
    }

    public void trySetClosedSuperFirewall(PackageParser.Package pkg) {
        if (!this.mFullMode && ColorPackageManagerHelper.isCtsPkgBySig(pkg)) {
            setClosedSuperFirewall(true);
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
            m.invoke(cls.newInstance(), ColorFullmodeManager.class.getName());
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
