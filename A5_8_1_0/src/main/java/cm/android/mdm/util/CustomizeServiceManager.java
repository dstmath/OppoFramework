package cm.android.mdm.util;

import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.IOppoCustomizeService;
import android.os.IOppoCustomizeService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.List;

public final class CustomizeServiceManager {
    private static final boolean DEBUG = true;
    private static final int INIT_TRY_TIMES = 3;
    private static final String SERVICE_NAME = "oppocustomize";
    private static String TAG = "CustomizeServiceManager";
    private static IOppoCustomizeService sService;

    public static final boolean init() {
        if (sService != null) {
            return DEBUG;
        }
        int times = INIT_TRY_TIMES;
        do {
            Log.w(TAG, "Try to OppoCustomizeService Instance! times = " + times);
            sService = Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
            if (sService != null) {
                return DEBUG;
            }
            times--;
        } while (times > 0);
        return false;
    }

    public static final void deviceReboot() {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.deviceReboot();
            } catch (RemoteException e) {
                Log.d(TAG, "servive reboot device failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static final void deviceShutDown() {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.deviceShutDown();
            } catch (RemoteException e) {
                Log.d(TAG, "servive showdown device failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static final void setSDCardFormatted() {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setSDCardFormatted();
            } catch (RemoteException e) {
                Log.d(TAG, "set SDCard Formatted failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static final boolean isDeviceRoot() {
        boolean ret = false;
        if (sService != null || (init() ^ 1) == 0) {
            try {
                ret = sService.isDeviceRoot();
            } catch (RemoteException e) {
                Log.d(TAG, "check device root failed!");
            }
            return ret;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return ret;
    }

    public static final void clearAppData(String pkgName) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.clearAppData(pkgName);
            } catch (RemoteException e) {
                Log.d(TAG, "clear App Data failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static final List<String> getClearAppName() {
        List<String> ret = null;
        if (sService != null || (init() ^ 1) == 0) {
            try {
                ret = sService.getClearAppName();
            } catch (RemoteException e) {
                Log.d(TAG, "get Clear App Name failed!");
            }
            return ret;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return ret;
    }

    public static void setProp(String prop, String value) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setProp(prop, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set prop failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void setDB(String key, int value) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setDB(key, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set DB failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void openCloseGps(boolean enable) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.openCloseGps(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "open close gps failed");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void openCloseNFC(boolean enable) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.openCloseNFC(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "open close nfc failed");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void setSettingsRestriction(String key, boolean value) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setSettingsRestriction(key, value);
            } catch (RemoteException e) {
                Log.d(TAG, "set settingsRestriction failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void setEmmAdmin(ComponentName cn, boolean enable) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setEmmAdmin(cn, enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set emm admin failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void setDataEnabled(boolean enable) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setDataEnabled(enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set data enable failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void setAccessibilityEnabled(ComponentName cn, boolean enable) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.setAccessibilityEnabled(cn, enable);
            } catch (RemoteException e) {
                Log.d(TAG, "set Accessibility Enabled failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static boolean setDeviceOwner(ComponentName cn) {
        boolean result = false;
        if (sService != null || (init() ^ 1) == 0) {
            try {
                result = sService.setDeviceOwner(cn);
            } catch (RemoteException e) {
                Log.d(TAG, "set device owner failed!");
            }
            return result;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return result;
    }

    public static void killProcess(String packageName) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.killAppProcess(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "Kill process failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void addProtectApplication(String packageName) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.addProtectApplication(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "add Protect Application failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void removeProtectApplication(String packageName) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.removeProtectApplication(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "remove Protect Application failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static List<String> getProtectApplicationList() {
        List<String> ret = null;
        if (sService != null || (init() ^ 1) == 0) {
            try {
                ret = sService.getProtectApplicationList();
            } catch (RemoteException e) {
                Log.d(TAG, "get Protect ApplicationList failed!");
            }
            return ret;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return ret;
    }

    public static Bitmap captureScreen() {
        Bitmap ret = null;
        if (sService != null || (init() ^ 1) == 0) {
            try {
                ret = sService.captureFullScreen();
            } catch (RemoteException e) {
                Log.d(TAG, "capture Screen failed!");
            }
            return ret;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
        return ret;
    }

    public static void allowGetUsageStats(String packageName) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.allowGetUsageStats(packageName);
            } catch (RemoteException e) {
                Log.d(TAG, "allow GetUsageStats failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }

    public static void updateConfiguration(Configuration config) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.updateConfiguration(config);
            } catch (RemoteException e) {
                Log.d(TAG, "update Configuration failed!");
            }
            return;
        }
        Log.d(TAG, "OppoCustomizeService init failed!");
    }
}
