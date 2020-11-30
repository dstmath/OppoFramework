package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorPmsSupportedFunctionManager implements IColorPmsSupportedFunctionManager {
    public static final String TAG = "ColorPmsSupportedFunctionManager";
    private static ColorPmsSupportedFunctionManager instance = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private boolean isSupportSessionWrite = false;
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;

    public static ColorPmsSupportedFunctionManager getInstance() {
        if (instance == null) {
            synchronized (ColorPmsSupportedFunctionManager.class) {
                if (instance == null) {
                    instance = new ColorPmsSupportedFunctionManager();
                }
            }
        }
        return instance;
    }

    private ColorPmsSupportedFunctionManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        registerLogModule();
    }

    public boolean isSupportSessionWrite() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isSupportSessionWrite is " + this.isSupportSessionWrite);
        }
        return this.isSupportSessionWrite;
    }

    public void setSupportSessionWrite(boolean support) {
        if (this.DEBUG_SWITCH) {
            Slog.i(TAG, "setSupportSessionWrite is " + support);
        }
        this.isSupportSessionWrite = support;
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
            m.invoke(cls.newInstance(), ColorPmsSupportedFunctionManager.class.getName());
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
