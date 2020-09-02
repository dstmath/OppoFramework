package cm.android.mdm.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class CustomizeServiceManager {
    private static final int ADD_APP_LIST = 1;
    private static final int BLACK_LIST = 1;
    private static final boolean DEBUG = true;
    private static final int DELETE_APP_LIST = 2;
    private static final int INIT_TRY_TIMES = 3;
    private static final String SERVICE_NAME = "oppocustomize";
    private static String TAG = "CustomizeServiceManager";
    private static final int WHITE_LIST = 2;
    private static IOppoCustomizeService sService;

    public static final boolean init() {
        if (sService != null) {
            return DEBUG;
        }
        int times = INIT_TRY_TIMES;
        do {
            Log.w(TAG, "Try to OppoCustomizeService Instance! times = " + times);
            sService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
            if (sService != null) {
                return DEBUG;
            }
            times--;
        } while (times > 0);
        return false;
    }

    public static final void deviceReboot() {
        if (sService != null || init()) {
            try {
                sService.deviceReboot();
            } catch (RemoteException e) {
                Log.d(TAG, "servive reboot device failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static final void deviceShutDown() {
        if (sService != null || init()) {
            try {
                sService.deviceShutDown();
            } catch (RemoteException e) {
                Log.d(TAG, "servive showdown device failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static final void setSDCardFormatted() {
        if (sService != null || init()) {
            try {
                sService.setSDCardFormatted();
            } catch (RemoteException e) {
                Log.d(TAG, "set SDCard Formatted failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static final boolean isDeviceRoot() {
        if (sService != null || init()) {
            try {
                return sService.isDeviceRoot();
            } catch (RemoteException e) {
                Log.d(TAG, "check device root failed!");
                return false;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return false;
        }
    }

    public static final void clearAppData(String pkgName) {
        if (sService != null || init()) {
            try {
                sService.clearAppData(pkgName);
            } catch (RemoteException e) {
                Log.d(TAG, "clear App Data failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static final List<String> getClearAppName() {
        if (sService != null || init()) {
            try {
                return sService.getClearAppName();
            } catch (RemoteException e) {
                Log.d(TAG, "get Clear App Name failed!");
                return null;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return null;
        }
    }

    public static void setProp(String prop, String value) {
        if (sService != null || init()) {
            try {
                sService.setProp(prop, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set prop failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setDB(String key, int value) {
        if (sService != null || init()) {
            try {
                sService.setDB(key, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set DB failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void openCloseGps(boolean enable) {
        if (sService != null || init()) {
            try {
                sService.openCloseGps(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "open close gps failed");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void openCloseNFC(boolean enable) {
        if (sService != null || init()) {
            try {
                sService.openCloseNFC(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "open close nfc failed");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setSettingsRestriction(String key, boolean value) {
        if (sService != null || init()) {
            try {
                sService.setSettingsRestriction(key, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set settingsRestriction failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setEmmAdmin(ComponentName cn, boolean enable) {
        if (sService != null || init()) {
            try {
                sService.setEmmAdmin(cn, enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set emm admin failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setDataEnabled(boolean enable) {
        if (sService != null || init()) {
            try {
                sService.setDataEnabled(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set data enable failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setAccessibilityEnabled(ComponentName cn, boolean enable) {
        if (sService != null || init()) {
            try {
                sService.setAccessibilityEnabled(cn, enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set Accessibility Enabled failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static boolean setDeviceOwner(ComponentName cn) {
        if (sService != null || init()) {
            try {
                return sService.setDeviceOwner(cn);
            } catch (RemoteException e) {
                Log.d(TAG, "set device owner failed!");
                return false;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return false;
        }
    }

    public static void killProcess(String packageName) {
        if (sService != null || init()) {
            try {
                sService.killAppProcess(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "Kill process failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void addProtectApplication(String packageName) {
        if (sService != null || init()) {
            try {
                sService.addProtectApplication(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "add Protect Application failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void removeProtectApplication(String packageName) {
        if (sService != null || init()) {
            try {
                sService.removeProtectApplication(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "remove Protect Application failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static List<String> getProtectApplicationList() {
        if (sService != null || init()) {
            try {
                return sService.getProtectApplicationList();
            } catch (RemoteException e) {
                Log.d(TAG, "get Protect ApplicationList failed!");
                return null;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return null;
        }
    }

    public static Bitmap captureScreen() {
        if (sService != null || init()) {
            try {
                return sService.captureFullScreen();
            } catch (RemoteException e) {
                Log.d(TAG, "capture Screen failed!");
                return null;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return null;
        }
    }

    public static void allowGetUsageStats(String packageName) {
        if (sService != null || init()) {
            try {
                sService.allowGetUsageStats(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "allow GetUsageStats failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void updateConfiguration(Configuration config) {
        if (sService != null || init()) {
            try {
                sService.updateConfiguration(config);
            } catch (RemoteException e) {
                Log.d(TAG, "update Configuration failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setDevelopmentEnabled(boolean enable) {
        if (sService != null || init()) {
            try {
                sService.setDevelopmentEnabled(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "update Configuration failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setNfcEnabled(boolean enable) {
        if (sService != null || init()) {
            try {
                sService.openCloseNFC(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "update Configuration failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setSendNotificationDisabled(boolean disable) {
        setDB("third.app.disable.notification", disable ? 1 : 0);
    }

    public static boolean isSendNotificationDisabled(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "third.app.disable.notification", 0) == 1) {
            return DEBUG;
        }
        return false;
    }

    public static void setStatusBarExpandPanelDisabled(boolean disable) {
        if (sService != null || init()) {
            try {
                sService.setStatusBarExpandPanelDisabled(disable);
            } catch (RemoteException e) {
                Log.d(TAG, "setStatusBarExpandPanelDisabled failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static boolean isStatusBarExpandPanelDisabled() {
        if (sService != null || init()) {
            try {
                return sService.isStatusBarExpandPanelDisabled();
            } catch (RemoteException e) {
                Log.d(TAG, "isStatusBarExpandPanelDisabled failed!");
                return false;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return false;
        }
    }

    public static boolean setMdmBlockPattern(ComponentName componentName, int blockPattern) {
        if (sService != null || init()) {
            return false;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return false;
    }

    public static int getMdmBlockPattern(ComponentName componentName) {
        if (sService != null || init()) {
            return -1;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return -1;
    }

    public static boolean setMdmMatchPattern(ComponentName componentName, int matchPattern) {
        if (sService != null || init()) {
            return false;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return false;
    }

    public static int getMdmMatchPattern(ComponentName componentName) {
        if (sService != null || init()) {
            return -1;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return -1;
    }

    public static boolean setMdmOutgoOrIncomePattern(ComponentName componentName, int outgoOrIncomePattern) {
        if (sService != null || init()) {
            return false;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return false;
    }

    public static int getMdmOutgoOrIncomePattern(ComponentName componentName) {
        if (sService != null || init()) {
            return -1;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return -1;
    }

    public static int addMdmNumberList(ComponentName componentName, List<String> list, int blockPattern) {
        if (sService != null || init()) {
            return -1;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return -1;
    }

    public static int removeMdmNumberList(ComponentName componentName, List<String> list, int blockPattern) {
        if (sService != null || init()) {
            return -1;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return -1;
    }

    public static List<String> getMdmNumberList(ComponentName componentName, int blockPattern) {
        List<String> numberList = new ArrayList<>();
        if (sService != null || init()) {
            return numberList;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return numberList;
    }

    public static boolean removeMdmAllNumber(ComponentName componentName, int blockPattern) {
        if (sService != null || init()) {
            return false;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return false;
    }

    public static void addDisallowedRunningApp(List<String> packageNames) {
        if (sService != null || init()) {
            try {
                sService.addDisallowedRunningApp(packageNames);
            } catch (RemoteException e) {
                Log.d(TAG, "addDisallowedRunningApp error");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void setNetworkRestriction(int pattern) {
        if (sService != null || init()) {
            try {
                sService.setNetworkRestriction(pattern);
            } catch (RemoteException e) {
                Log.d(TAG, "setNetworkRestriction failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void addNetworkRestriction(int pattern, List<String> list) {
        if (sService != null || init()) {
            try {
                sService.addNetworkRestriction(pattern, list);
            } catch (RemoteException e) {
                Log.d(TAG, "addNetworkRestriction failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void removeNetworkRestriction(int pattern, List<String> list) {
        if (sService != null || init()) {
            try {
                sService.removeNetworkRestriction(pattern, list);
            } catch (RemoteException e) {
                Log.d(TAG, "removeNetworkRestriction failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void removeNetworkRestrictionAll(int pattern) {
        if (sService != null || init()) {
            try {
                sService.removeNetworkRestrictionAll(pattern);
            } catch (RemoteException e) {
                Log.d(TAG, "removeNetworkRestrictionAll failed!");
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void addDisallowUninstallApps(List<String> packageName) {
        if (sService != null || init()) {
            try {
                sService.addDisallowUninstallApps(packageName);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "enableInstallSource failed!" + e);
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static void removeDisallowUninstallApps(List<String> packageName) {
        if (sService != null || init()) {
            try {
                sService.removeDisallowUninstallApps(packageName);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "enableInstallSource failed!" + e);
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
        }
    }

    public static List<String> getDisallowUninstallApps() {
        if (sService != null || init()) {
            try {
                return sService.getallDisallowUninstallApps();
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "enableInstallSource failed!" + e);
                return null;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return null;
        }
    }

    public static List<String> getAppRestriction(int pattern) {
        if (sService != null || init()) {
            try {
                return sService.getAppInstallationPolicies(pattern);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "getAppInstallationPolicies failed!" + e);
                return null;
            }
        } else {
            Log.d(TAG, "OppoCustomizeService init failed!");
            return null;
        }
    }

    public static void addAppRestriction(int pattern, List<String> pkgs) {
        if (sService == null && !init()) {
            Log.d(TAG, "OppoCustomizeService init failed!");
        } else if (pattern == 1) {
            try {
                sService.addInstallPackageBlacklist(1, pkgs);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "addAppRestriction failed!" + e);
            }
        } else if (pattern == 2) {
            sService.addInstallPackageWhitelist(1, pkgs);
        }
    }

    public static void removeAppRestriction(int pattern, List<String> pkgs) {
        if (sService == null && !init()) {
            Log.d(TAG, "OppoCustomizeService init failed!");
        } else if (pattern == 1) {
            try {
                sService.addInstallPackageBlacklist(2, pkgs);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "removeAppRestriction failed!" + e);
            }
        } else if (pattern == 2) {
            sService.addInstallPackageWhitelist(2, pkgs);
        }
    }

    public static void removeAllAppRestriction(int pattern) {
        if (sService == null && !init()) {
            Log.d(TAG, "OppoCustomizeService init failed!");
        } else if (pattern == 1) {
            try {
                sService.addInstallPackageBlacklist(2, (List) null);
            } catch (RemoteException e) {
                String str = TAG;
                Log.d(str, "removeAllAppRestriction failed!" + e);
            }
        } else if (pattern == 2) {
            sService.addInstallPackageWhitelist(2, (List) null);
        }
    }
}
