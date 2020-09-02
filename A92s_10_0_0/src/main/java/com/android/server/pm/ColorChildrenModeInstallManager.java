package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorChildrenModeInstallManager implements IColorChildrenModeInstallManager {
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final String TAG = "ColorChildrenModeInstallManager";
    private static ColorChildrenModeInstallManager sColorChildrenModeInstallManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private Context mContext = null;
    boolean mDynamicDebug = EXP_VERSION;

    public static ColorChildrenModeInstallManager getInstance() {
        if (sColorChildrenModeInstallManager == null) {
            sColorChildrenModeInstallManager = new ColorChildrenModeInstallManager();
        }
        return sColorChildrenModeInstallManager;
    }

    private ColorChildrenModeInstallManager() {
    }

    public void init(Context ctx) {
        this.mContext = ctx;
        registerLogModule();
    }

    public boolean prohibitChildInstallation(int userId, boolean isInstall) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "prohibitChildInstallation called with userId " + userId + "; isInstall " + isInstall);
        }
        if (!EXP_VERSION || Settings.Global.getInt(this.mContext.getContentResolver(), "children_mode_on", 0) != 1) {
            return EXP_VERSION;
        }
        UserHandle user = new UserHandle(userId);
        long identity = Binder.clearCallingIdentity();
        if (isInstall) {
            try {
                Slog.w(TAG, "prevent installation in children mode");
                this.mContext.sendBroadcastAsUser(new Intent("com.coloros.childrenspace.action.FORBID_INSTALL_PACKAGES"), user, "oppo.permission.OPPO_COMPONENT_SAFE");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            Slog.w(TAG, "prevent uninstallation in children mode");
            this.mContext.sendBroadcastAsUser(new Intent("com.coloros.childrenspace.action.FORBID_DELETE_PACKAGES"), user, "oppo.permission.OPPO_COMPONENT_SAFE");
        }
        Binder.restoreCallingIdentity(identity);
        return true;
    }

    public boolean prohibitDeleteInChildMode(int userId, String packageName, IPackageDeleteObserver2 observer, boolean isInstall) {
        if (!prohibitChildInstallation(userId, EXP_VERSION)) {
            return EXP_VERSION;
        }
        try {
            observer.onPackageDeleted(packageName, -3, (String) null);
            return true;
        } catch (RemoteException e) {
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
            m.invoke(cls.newInstance(), ColorChildrenModeInstallManager.class.getName());
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
