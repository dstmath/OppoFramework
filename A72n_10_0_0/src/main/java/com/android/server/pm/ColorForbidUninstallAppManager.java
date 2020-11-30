package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageParser;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.app.ColorAccessControlManager;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorForbidUninstallAppManager implements IColorForbidUninstallAppManager {
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    protected static final String PROPERTY_OPPO_DEX_THREAD_NUMBER = "oppo.dex.thread.number";
    public static final String TAG = "ColorForbidUninstallAppManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    private static ColorForbidUninstallAppManager sForbidUninstallAppManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private OppoBasePackageManagerService mBasePms = null;
    Context mContext;
    boolean mDynamicDebug = EXP_VERSION;
    private PackageManagerService mPms = null;

    public static ColorForbidUninstallAppManager getInstance() {
        if (sForbidUninstallAppManager == null) {
            sForbidUninstallAppManager = new ColorForbidUninstallAppManager();
        }
        return sForbidUninstallAppManager;
    }

    private ColorForbidUninstallAppManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
        this.mContext = pmsEx.getContext();
        this.mBasePms = typeCasting(this.mPms);
    }

    /* JADX INFO: finally extract failed */
    public boolean isForbidDeletePackage(int uid, int userId, String packageName, IPackageDeleteObserver2 observer) {
        PackageParser.Package toDeletePkg;
        boolean forbidden = EXP_VERSION;
        if (ColorPackageManagerHelper.isOppoHideApp(packageName) && ("com.coloros.lockassistant".equals(packageName) || uid == 2000)) {
            Slog.d(TAG, "forbidden to remove Oem HideApp app:" + packageName);
            ColorPackageManagerHelper.sendDcsPreventUninstallSystemApp(this.mContext, "pc", packageName);
            forbidden = true;
        }
        if (uid != 0) {
            synchronized (this.mPms.mPackages) {
                toDeletePkg = (PackageParser.Package) this.mPms.mPackages.get(packageName);
            }
            if (ColorPackageManagerHelper.isForbidUninstallDataApp(toDeletePkg)) {
                Slog.d(TAG, "forbid delete " + packageName + " from normal user");
                forbidden = true;
            }
            if (ColorPackageManagerHelper.isForbidUninstallSystemUpdateApp(toDeletePkg)) {
                Slog.d(TAG, "forbid delete " + packageName + " due to system updated app in data");
                forbidden = true;
            }
            if (ColorPackageManagerHelper.isForbidUninstallByBindSecurityEvent(this.mContext, toDeletePkg)) {
                Slog.d(TAG, "in security event bind, forbid to uninstall " + packageName);
                forbidden = true;
            }
            OppoBasePackageManagerService oppoBasePackageManagerService = this.mBasePms;
            if (oppoBasePackageManagerService != null && oppoBasePackageManagerService.interceptUninstallSellModeIfNeeded(toDeletePkg, observer)) {
                Slog.d(TAG, "forbidden to remove app in sell mode" + packageName);
                return true;
            }
        }
        if (!forbidden && ((userId == 0 || userId == 999) && Settings.Secure.getInt(this.mContext.getContentResolver(), "access_control_lock_enabled", 0) == 1)) {
            boolean appEncypt = ColorAccessControlManager.getInstance().getApplicationAccessControlEnabledAsUser(packageName, userId);
            if (!appEncypt && userId == 0 && ColorAccessControlManager.getInstance().getApplicationAccessControlEnabledAsUser(packageName, (int) ColorMultiAppManagerService.USER_ID)) {
                appEncypt = true;
            }
            if (appEncypt) {
                long identity = Binder.clearCallingIdentity();
                try {
                    Slog.d(TAG, "prevent uninstall protect apps:" + packageName + ",userId:" + userId);
                    Intent intent = new Intent("oppo.intent.privacy.action.FORBID_DELETE_PACKAGES");
                    intent.setPackage("com.coloros.safecenter");
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.OWNER, "oppo.permission.OPPO_COMPONENT_SAFE");
                    Binder.restoreCallingIdentity(identity);
                    forbidden = true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }
        if (forbidden) {
            try {
                observer.onPackageDeleted(packageName, -1, (String) null);
            } catch (RemoteException e) {
            }
        }
        return forbidden;
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
            m.invoke(cls.newInstance(), ColorForbidUninstallAppManager.class.getName());
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

    private static OppoBasePackageManagerService typeCasting(PackageManagerService pms) {
        if (pms != null) {
            return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
        }
        return null;
    }
}
