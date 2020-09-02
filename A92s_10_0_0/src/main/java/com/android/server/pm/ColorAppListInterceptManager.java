package com.android.server.pm;

import android.content.Intent;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorAppListInterceptManager implements IColorAppListInterceptManager {
    private static final String FEATURE_NAME_HIDE_APP = "oppo.customize.function.hide_app";
    private static final String TAG = "ColorAppListInterceptManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorAppListInterceptManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    public static ColorAppListInterceptManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAppListInterceptManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAppListInterceptManager();
                }
            }
        }
        return sInstance;
    }

    private ColorAppListInterceptManager() {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "Constructor");
        }
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "init");
        }
        this.mPmsEx = pmsEx;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
    }

    public boolean loadHideAppConfigurations() {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "loadHideAppConfigurations");
        }
        if (!this.mPms.hasSystemFeature(FEATURE_NAME_HIDE_APP, 0)) {
            return false;
        }
        ColorPackageManagerHelper.addPrivilegedHideApp();
        return true;
    }

    public boolean shouldFilterTask(Intent intent) {
        Slog.d(TAG, "shouldFilterTask");
        if (intent == null || intent.getComponent() == null) {
            return false;
        }
        String packageName = intent.getComponent().getPackageName();
        Slog.d(TAG, "shouldFilterTask::packageName = " + packageName);
        if (TextUtils.isEmpty(packageName) || !ColorPackageManagerHelper.isPrivilegedHideApp(packageName)) {
            return false;
        }
        if (!this.DEBUG_SWITCH) {
            return true;
        }
        Slog.d(TAG, "shouldFilterTask::package " + packageName + " is hide app");
        return true;
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
            m.invoke(cls.newInstance(), ColorAppListInterceptManager.class.getName());
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
