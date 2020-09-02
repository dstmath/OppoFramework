package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.OppoDataUpdater;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorOtaDataManager implements IColorOtaDataManager {
    public static final String TAG = "ColorOtaDataManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorOtaDataManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean isBootFromOta = isBootFromOTA();
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    private Context mContext = null;
    boolean mDynamicDebug = false;
    /* access modifiers changed from: private */
    public PackageManagerService mPms = null;

    public static ColorOtaDataManager getInstance() {
        if (sInstance == null) {
            sInstance = new ColorOtaDataManager();
        }
        return sInstance;
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
            this.mContext = this.mPms.mContext;
        }
        registerLogModule();
        OppoDataUpdater.getInstance().setCallback(new DataUpdaterCallBackImpl());
    }

    public void initAppList() {
        OppoDataUpdater.getInstance().init(this.isBootFromOta);
    }

    public void update() {
        OppoDataUpdater.getInstance().update();
    }

    public void systemReady() {
        OppoDataUpdater.getInstance().systemReady(this.mContext);
    }

    public void setDataPackageNameList(ArraySet<String> list) {
        OppoDataUpdater.getInstance().setDataPackageNameList(list);
    }

    public void addManualPackageOperationState(PackageSetting ps, String packageName, boolean install) {
        if (install) {
            OppoDataUpdater.getInstance().addManualPackageOperationState(packageName, install);
            return;
        }
        boolean isAllUninstalled = true;
        boolean isSystemApp = ps != null && (ps.isSystem() || ps.isUpdatedSystem());
        Slog.d(TAG, "isSystemApp = " + isSystemApp);
        if (!isSystemApp) {
            PackageSetting tPs = this.mPms.mSettings.getPackageLPr(packageName);
            int[] installedUsers = null;
            if (tPs != null) {
                PackageManagerService packageManagerService = this.mPms;
                int[] allUsers = PackageManagerService.sUserManager.getUserIds();
                installedUsers = tPs.queryInstalledUsers(allUsers, true);
                if (allUsers != null && allUsers.length > 0) {
                    for (int i = 0; i < allUsers.length; i++) {
                        Slog.d(TAG, "package " + packageName + " in user " + allUsers[i] + " is installed");
                    }
                }
            } else {
                Slog.w(TAG, "tPs is null");
            }
            if (!(tPs == null || installedUsers == null || installedUsers.length == 0)) {
                isAllUninstalled = false;
            }
            Slog.d(TAG, "uninstalled by all users = " + isAllUninstalled);
            if (isAllUninstalled) {
                OppoDataUpdater.getInstance().addManualPackageOperationState(packageName, false);
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        OppoDataUpdater.getInstance().dump(pw, args);
    }

    static boolean isBootFromOTA() {
        File file = new File("/cache/recovery/intent");
        boolean result = false;
        String resultStr = "";
        if (file.exists() && file.canRead()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                resultStr = reader2.readLine();
                result = "0".equals(resultStr) || "2".equals(resultStr);
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
                }
            } catch (IOException e) {
                Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                    }
                }
                throw th;
            }
        } else if (sDebugfDetail) {
            Slog.i(TAG, "OTA file path is no exist,normal boot");
        }
        Slog.d(TAG, "isBootFromOTA::resultStr = " + resultStr + ", result = " + result);
        return result;
    }

    private class DataUpdaterCallBackImpl implements OppoDataUpdater.DataUpdaterCallback {
        private DataUpdaterCallBackImpl() {
        }

        @Override // com.android.server.pm.OppoDataUpdater.DataUpdaterCallback
        public PackageParser.Package getPackage(String name) {
            return (PackageParser.Package) ColorOtaDataManager.this.mPms.mPackages.get(name);
        }

        @Override // com.android.server.pm.OppoDataUpdater.DataUpdaterCallback
        public PackageSetting getPackageSetting(String name) {
            return ColorOtaDataManager.this.mPms.mSettings.getPackageLPr(name);
        }

        @Override // com.android.server.pm.OppoDataUpdater.DataUpdaterCallback
        public void removePackageSetting(String name) {
            ColorOtaDataManager.this.mPms.mSettings.removePackageLPw(name);
        }

        @Override // com.android.server.pm.OppoDataUpdater.DataUpdaterCallback
        public void removeCodePath(File path) {
            ColorOtaDataManager.this.mPms.removeCodePathLI(path);
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
            m.invoke(cls.newInstance(), ColorOtaDataManager.class.getName());
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
