package com.android.server.pm;

import android.app.ActivityManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.am.ColorAppStartupManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorInstallThreadsControlManager implements IColorInstallThreadsControlManager {
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    protected static final String PROPERTY_OPPO_DEX_THREAD_NUMBER = "oppo.dex.thread.number";
    public static final String TAG = "ColorInstallThreadsControlManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    private static ColorInstallThreadsControlManager sInstallThreadsControlManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = EXP_VERSION;
    private OppoActivityManager mOppoAm = new OppoActivityManager();
    private final String[] mPackageInstaller = {"com.android.vending", "com.coloros.backuprestore", "com.google.android.packageinstaller"};

    public static ColorInstallThreadsControlManager getInstance() {
        if (sInstallThreadsControlManager == null) {
            sInstallThreadsControlManager = new ColorInstallThreadsControlManager();
        }
        return sInstallThreadsControlManager;
    }

    private ColorInstallThreadsControlManager() {
    }

    public void updateOdexThreads(String installerPackageName, int installFlags) {
        SystemProperties.set(PROPERTY_OPPO_DEX_THREAD_NUMBER, ColorAppStartupManager.RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE);
    }

    private int adjustInstallerParameterCore(String installerPackageName, int installFlags) {
        if (!EXP_VERSION || installerPackageName == null) {
            return installFlags;
        }
        String pkgName = null;
        if (ActivityManager.getService() == null) {
            return installFlags;
        }
        try {
            ComponentName cn = this.mOppoAm.getTopActivityComponentName();
            if (cn != null) {
                pkgName = cn.getPackageName();
            }
        } catch (RemoteException e) {
        }
        if (pkgName == null || installerPackageName.equals(pkgName)) {
            return installFlags;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.mPackageInstaller;
            if (i >= strArr.length) {
                return installFlags;
            }
            if (strArr[i].equals(installerPackageName)) {
                int installFlags2 = installFlags | Integer.MIN_VALUE;
                Slog.i(TAG, "" + this.mPackageInstaller[i] + " installFlags with INSTALL_SPEED_BACKGROUND");
                return installFlags2;
            }
            i++;
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
            m.invoke(cls.newInstance(), ColorInstallThreadsControlManager.class.getName());
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
