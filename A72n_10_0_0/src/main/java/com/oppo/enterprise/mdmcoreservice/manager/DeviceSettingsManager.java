package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager;

public class DeviceSettingsManager extends DeviceBaseManager {
    private static final String TAG = "DeviceSettingsManager";
    private static final Object mLock = new Object();
    private static volatile DeviceSettingsManager sInstance;

    public static final DeviceSettingsManager getInstance(Context context) {
        DeviceSettingsManager deviceSettingsManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceSettingsManager(context);
            }
            deviceSettingsManager = sInstance;
        }
        return deviceSettingsManager;
    }

    private DeviceSettingsManager(Context context) {
        super(context);
    }

    private IDeviceSettingsManager getIDeviceSettingsManager() {
        return IDeviceSettingsManager.Stub.asInterface(getOppoMdmManager("DeviceSettingsManager"));
    }

    public boolean setTimeAndDateSetDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.setTimeAndDateSetDisabled(componentName, disabled);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "setTimeAndDateSetDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTimeAndDateSetDisabled(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.isTimeAndDateSetDisabled(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "isTimeAndDateSetDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setRestoreFactoryDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.setRestoreFactoryDisabled(componentName, disabled);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "setRestoreFactoryDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRestoreFactoryDisabled(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.isRestoreFactoryDisabled(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "isRestoreFactoryDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public String getAPIVersion(ComponentName componentName) throws SecurityException {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.getAPIVersion(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "getAPIVersion error!");
            e.printStackTrace();
            return null;
        }
    }

    public String getRomVersion(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.getRomVersion(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "getRomVersion error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setTetherEnable(boolean isAllow) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                manager.setTetherEnable(isAllow);
                return;
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "setTetherEnable error: " + e.getMessage());
        }
    }

    public void setTetherEnable(Context context, ComponentName componentName, boolean isAllow) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                DeviceRestrictionManager deviceRestrictionManager = DeviceRestrictionManager.getInstance(context);
                if (isAllow) {
                    if (deviceRestrictionManager.isWifiApDisabled(componentName)) {
                        deviceRestrictionManager.setWifiApDisabled(componentName, false);
                    }
                    if (deviceRestrictionManager.isBluetoothTetheringDisabled(componentName)) {
                        deviceRestrictionManager.setBluetoothTetheringDisabled(componentName, false);
                    }
                    if (deviceRestrictionManager.isUsbTetheringDisabled(componentName)) {
                        deviceRestrictionManager.setUsbTetheringDisable(componentName, false);
                    }
                    if (deviceRestrictionManager.isWifiSharingDisabled(componentName)) {
                        deviceRestrictionManager.setWifiSharingDisabled(componentName, false);
                    }
                } else {
                    if (!deviceRestrictionManager.isWifiApDisabled(componentName)) {
                        deviceRestrictionManager.setWifiApDisabled(componentName, true);
                    }
                    if (!deviceRestrictionManager.isBluetoothTetheringDisabled(componentName)) {
                        deviceRestrictionManager.setBluetoothTetheringDisabled(componentName, true);
                    }
                    if (!deviceRestrictionManager.isUsbTetheringDisabled(componentName)) {
                        deviceRestrictionManager.setUsbTetheringDisable(componentName, true);
                    }
                    if (!deviceRestrictionManager.isWifiSharingDisabled(componentName)) {
                        deviceRestrictionManager.setWifiSharingDisabled(componentName, true);
                    }
                }
                manager.setTetherEnable(isAllow);
                return;
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "setTetherEnable error: " + e.getMessage());
        }
    }

    public boolean getTetherEnable(Context context) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.getTetherEnable();
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "getTetherEnable error: " + e.getMessage());
            return false;
        }
    }

    public boolean setDevelopmentOptionsDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.setDevelopmentOptionsDisabled(componentName, disabled);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "setDevelopmentOptionsDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDeveloperOptionsDisabled(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.isDeveloperOptionsDisabled(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "isDeveloperOptionsDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setVolumeMuted(ComponentName componentName, boolean isMuted) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setVolumeMuted(componentName, isMuted);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setVolumeMuted error!", e);
            return false;
        }
    }

    public boolean isVolumeMuted(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.isVolumeMuted(componentName);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "isVolumeMuted error!", e);
            return false;
        }
    }

    public boolean setFontSize(ComponentName componentName, int size) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setFontSize(componentName, size);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setFontSize error!", e);
            return false;
        }
    }

    public boolean setSearchIndexDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setSearchIndexDisabled(admin, disabled);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setSearchIndexDisabled error!", e);
            return false;
        }
    }

    public boolean isSearchIndexDisabled(ComponentName admin) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.isSearchIndexDisabled(admin);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "isSearchIndexDisabled error!", e);
            return false;
        }
    }

    public boolean enableAllNotificationChannel(String packageName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.enableAllNotificationChannel(packageName);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "enableAllNotificationChannel error!", e);
            return false;
        }
    }

    public boolean disableAllNotificationChannel(String packageName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.disableAllNotificationChannel(packageName);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "disableAllNotificationChannel error!", e);
            return false;
        }
    }

    public boolean switchNotificationChannel(String packageName, String channelID, String manualType, boolean enabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.switchNotificationChannel(packageName, channelID, manualType, enabled);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "switchNotificationChannel error!", e);
            return false;
        }
    }

    public boolean setVolumeChangeActionState(ComponentName admin, int mode) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setVolumeChangeActionState(admin, mode);
            }
            Log.e("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null!");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setVolumeChangeActionState error!", e);
            e.printStackTrace();
            return false;
        }
    }

    public int getVolumeChangeActionState(ComponentName admin) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:getVolumeChangeActionState()");
                return manager.getVolumeChangeActionState(admin);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return 0;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "getVolumeChangeActionState error!");
            e.printStackTrace();
            return 0;
        }
    }

    public boolean turnOnProtectEyes(ComponentName componentName, boolean on) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.turnOnProtectEyes(componentName, on);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "turnOnProtectEyes error!");
            return false;
        }
    }

    public boolean isProtectEyesOn(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.isProtectEyesOn(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "isProtectEyesOn error!");
            return false;
        }
    }

    public boolean setAutoScreenOffTime(ComponentName componentName, long millis) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.setAutoScreenOffTime(componentName, millis);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "setAutoScreenOffTime error!");
            return false;
        }
    }

    public long getAutoScreenOffTime(ComponentName componentName) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager:" + manager);
                return manager.getAutoScreenOffTime(componentName);
            }
            Log.d("DeviceSettingsManager", "mdm service IDeviceSettingsManager manager is null");
            return 0;
        } catch (RemoteException e) {
            Log.i("DeviceSettingsManager", "getAutoScreenOffTime error!");
            return 0;
        }
    }

    public boolean setSIMLockDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setSIMLockDisabled(admin, disabled);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setSIMLockDisabled error!", e);
            return false;
        }
    }

    public boolean isSIMLockDisabled(ComponentName admin) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.isSIMLockDisabled(admin);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "isSIMLockDisabled error!", e);
            return false;
        }
    }

    public boolean setBackupRestoreDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setBackupRestoreDisabled(admin, disabled);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "setBackupRestoreDisabled error!", e);
            return false;
        }
    }

    public boolean isBackupRestoreDisabled(ComponentName admin) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.isBackupRestoreDisabled(admin);
            }
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceSettingsManager", "isBackupRestoreDisabled error!", e);
            return false;
        }
    }

    public boolean setInterceptAllNotifications(boolean intercepted) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setInterceptAllNotifications(intercepted);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "setInterceptAllNotifications error!", e);
            return false;
        }
    }

    public boolean setInterceptNonSystemNotifications(boolean intercepted) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setInterceptNonSystemNotifications(intercepted);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "setInterceptNonSystemNotifications error!", e);
            return false;
        }
    }

    public boolean shouldInterceptAllNotifications() {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.shouldInterceptAllNotifications();
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "shouldInterceptAllNotifications error!", e);
            return false;
        }
    }

    public boolean shouldInterceptNonSystemNotifications() {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.shouldInterceptNonSystemNotifications();
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "shouldInterceptNonSystemNotifications error!", e);
            return false;
        }
    }

    public boolean setPackageNotificationEnable(String pkgName, boolean isMultiApp, boolean enabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.setPackageNotificationEnable(pkgName, isMultiApp, enabled);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "setPackageNotificationEnable error!", e);
            return false;
        }
    }

    public boolean isPackageNotificationEnable(String pkgName, boolean isMultiApp) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.isPackageNotificationEnable(pkgName, isMultiApp);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "isPackageNotificationEnable error!", e);
            return false;
        }
    }

    public boolean updateNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType, boolean enabled) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.updateNotificationChannel(pkgName, isMultiApp, channelId, switchType, enabled);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "updateNotificationChannel error!", e);
            return false;
        }
    }

    public boolean queryNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType) {
        try {
            IDeviceSettingsManager manager = getIDeviceSettingsManager();
            if (manager != null) {
                return manager.queryNotificationChannel(pkgName, isMultiApp, channelId, switchType);
            }
            return false;
        } catch (Exception e) {
            Log.e("DeviceSettingsManager", "queryNotificationChannel error!", e);
            return false;
        }
    }
}
