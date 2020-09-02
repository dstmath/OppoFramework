package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorPkgStartInfoManager implements IColorPkgStartInfoManager {
    private static final String TAG = "ColorPkgStartInfoManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorPkgStartInfoManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = false;

    public static ColorPkgStartInfoManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorPkgStartInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorPkgStartInfoManager();
                }
            }
        }
        return sInstance;
    }

    private ColorPkgStartInfoManager() {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "Constructor");
        }
    }

    public boolean removePkgFromNotLaunchedList(String pkg, boolean notify) {
        return ColorPackageManagerHelper.removePkgFromNotLaunchedList(pkg, notify);
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        ColorPackageManagerHelper.initNotLaunchedList(pmsEx.getPackageManagerService(), pmsEx.getPackageManagerService().isFirstBoot());
    }

    public boolean addPkgToNotLaunchedList(String pkg) {
        return ColorPackageManagerHelper.addPkgToNotLaunchedList(pkg);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        getInstance().setDynamicDebugSwitch(on);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorPkgStartInfoManager.class.getName());
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
