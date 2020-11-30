package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.am.ColorProcessWhiteListUtils;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorClearDataProtectManager implements IColorClearDataProtectManager {
    private static final String TAG = "ColorClearDataProtectManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorClearDataProtectManager sInstance = null;
    boolean DEBUG_SWITCH;
    boolean mDynamicDebug = false;
    private PackageManagerService mPms;
    private IColorPackageManagerServiceEx mPmsEx;

    public static ColorClearDataProtectManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorClearDataProtectManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorClearDataProtectManager();
                }
            }
        }
        return sInstance;
    }

    private ColorClearDataProtectManager() {
        boolean z = sDebugfDetail;
        boolean z2 = this.mDynamicDebug;
        this.DEBUG_SWITCH = z | z2;
        this.mPmsEx = null;
        this.mPms = null;
        if (z2) {
            Slog.d(TAG, "Constructor");
        }
    }

    public void init(IColorPackageManagerServiceEx ex) {
        if (this.mDynamicDebug) {
            Slog.d(TAG, "init");
        }
        this.mPmsEx = ex;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
    }

    public void interceptClearUserDataIfNeeded(String packageName) throws SecurityException {
        if (Binder.getCallingUid() == 2000) {
            ColorProcessWhiteListUtils mFileUtils = ColorProcessWhiteListUtils.getInstance();
            if (!mFileUtils.isSupportClearSystemAppData()) {
                ApplicationInfo info = this.mPms.getApplicationInfo(packageName, 0, UserHandle.getCallingUserId());
                boolean isCtsRunning = OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
                if (!(info == null || (info.flags & 1) == 0 || isCtsRunning)) {
                    Slog.d(TAG, "this is system app not support adb clear user data!!!");
                    throw new SecurityException("adb clearing user data is forbidden.");
                }
            }
            if (packageName != null && mFileUtils.isClearDataWhiteApp(packageName)) {
                Slog.d(TAG, "this is not support adb clear pkg data: " + packageName);
                throw new SecurityException("adb clearing user data is forbidden.");
            }
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
            m.invoke(cls.newInstance(), ColorClearDataProtectManager.class.getName());
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
