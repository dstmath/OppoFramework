package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager;
import java.util.ArrayList;
import java.util.List;

public class DeviceRestrictionManager extends DeviceBaseManager {
    public static final int AIRPLANE_POLICY_NO_RESTRICTIONS = 2;
    public static final int AIRPLANE_POLICY_OFF = 3;
    public static final int AIRPLANE_POLICY_OFF_FORCE = 0;
    public static final int AIRPLANE_POLICY_ON = 4;
    public static final int AIRPLANE_POLICY_ON_FORCE = 1;
    public static final int DISABLE_APP_IN_LAUNCHER_FLAG = 4097;
    public static final int DISABLE_APP_IN_RECENT_TASK_FLAG = 268500992;
    public static final int DISABLE_NONE = 0;
    public static final int ENABLE_APP_IN_LAUNCHER_FLAG = 4096;
    public static final int ENABLE_APP_IN_RECENT_TASK_FLAG = 268435456;
    public static final int GPS_FORCE_CLOSE = 0;
    public static final int GPS_FORCE_OPEN = 1;
    public static final int GPS_NO_CONTROLLER = 2;
    public static final int GPS_STATUS_CLOSE = 3;
    public static final int GPS_STATUS_OPEN = 4;
    public static final int SETTINGS_POLICY_USER_PASSWORD_COMPLEX = 0;
    public static final int SETTINGS_POLICY_USER_PASSWORD_NO_RESTRICTIONS = 3;
    public static final int SETTINGS_POLICY_USER_PASSWORD_ONLY_BIOMETRICS = 2;
    public static final int SETTINGS_POLICY_USER_PASSWORD_SIMPLLE = 1;
    private static final String TAG = "DeviceRestrictionManager";
    public static final int UNLCOK_POLICY_NO_RESTRICTIONS_BY_FACE = 2;
    public static final int UNLCOK_POLICY_NO_RESTRICTIONS_BY_FINGER = 2;
    public static final int UNLCOK_POLICY_OFF_BY_FACE = 3;
    public static final int UNLCOK_POLICY_OFF_BY_FINGER = 3;
    public static final int UNLCOK_POLICY_OFF_FORCE_BY_FACE = 0;
    public static final int UNLCOK_POLICY_OFF_FORCE_BY_FINGER = 0;
    public static final int UNLCOK_POLICY_ON_BY_FACE = 4;
    public static final int UNLCOK_POLICY_ON_BY_FINGER = 4;
    public static final int UNLCOK_POLICY_ON_FORCE_BY_FACE = 1;
    public static final int UNLCOK_POLICY_ON_FORCE_BY_FINGER = 1;
    public static final int VALUE_EXCEPTION = -100;
    private static final Object mLock = new Object();
    private static volatile DeviceRestrictionManager sInstance;

    public static final DeviceRestrictionManager getInstance(Context context) {
        DeviceRestrictionManager deviceRestrictionManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceRestrictionManager(context);
            }
            deviceRestrictionManager = sInstance;
        }
        return deviceRestrictionManager;
    }

    private DeviceRestrictionManager(Context context) {
        super(context);
    }

    private IDeviceRestrictionManager getIDeviceRestrictionManager() {
        return IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
    }

    public void setStatusBarExpandPanelDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setStatusBarExpandPanelDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setStatusBarExpandPanelDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isStatusBarExpandPanelDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isStatusBarExpandPanelDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setWifiDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setWifiDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setWifiDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isWifiDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isWifiDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isWifiDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setWifiP2pDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setWifiP2pDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setWifiP2pDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isWifiP2pDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isWifiP2pDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isWifiP2pDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setWifiApDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setWifiApDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setWifiApDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isWifiApDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isWifiApDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setWifiSharingDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setWifiSharingDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setWifiSharingDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isWifiSharingDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isWifiSharingDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isWifiSharingDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setWifiInBackground(ComponentName admin, boolean enable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.setWifiInBackground(admin, enable);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setWifiInBackground error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isWifiOpen(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isWifiOpen(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isWifiOpen error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setBluetoothDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setBluetoothDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isBluetoothDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isBluetoothDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setBluetoothEnabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothEnabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setBluetoothEnabled error!");
            e.printStackTrace();
        }
    }

    public boolean isBluetoothEnabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothEnabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isBluetoothEnabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setBluetoothTetheringDisabled(ComponentName admin, boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothTetheringDisabled(admin, disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setBluetoothTetheringDisabled error", e);
        }
    }

    public boolean isBluetoothTetheringDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothTetheringDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isBluetoothTetheringDisabled error", e);
            return false;
        }
    }

    public void setBluetoothDataTransferDisabled(boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothDataTransferDisabled(disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setBluetoothDataTransferDisabled error", e);
        }
    }

    public boolean isBluetoothDataTransferDisabled() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothDataTransferDisabled();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isBluetoothDataTransferDisabled error", e);
            return false;
        }
    }

    public void setBluetoothPairingDisabled(boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothPairingDisabled(disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setBluetoothPairingDisabled error", e);
        }
    }

    public boolean isBluetoothPairingDisabled() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothPairingDisabled();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isBluetoothPairingDisabled error", e);
            return false;
        }
    }

    public void setBluetoothOutGoingCallDisabled(boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setBluetoothOutGoingCallDisabled(disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setBluetoothOutGoingCallDisabled error", e);
        }
    }

    public boolean isBluetoothOutGoingCallDisabled() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isBluetoothOutGoingCallDisabled();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isBluetoothOutGoingCallDisabled error", e);
            return false;
        }
    }

    public void setNFCDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setNFCDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setNFCDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isNFCDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isNFCDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isNFCDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void openCloseNFC(ComponentName admin, boolean enable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.openCloseNFC(admin, enable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "openCloseNFC error!");
            e.printStackTrace();
        }
    }

    public boolean isNFCTurnOn(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isNFCTurnOn(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isNFCTurnOn error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setAndroidBeamDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceSettingsManager manager : " + manager);
                return manager.setAndroidBeamDisabled(admin, disabled);
            }
        } catch (Exception e) {
            Log.i("DeviceRestrictionManager", "setAndroidBeamDisabled error!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAndroidBeamDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceSettingsManager manager : " + manager);
                return manager.isAndroidBeamDisabled(admin);
            }
        } catch (Exception e) {
            Log.i("DeviceRestrictionManager", "isAndroidBeamDisabled error!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean setRecordDisabled(ComponentName componentName, boolean enable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setRecordDisabled(" + enable + "+)");
                return manager.setRecordDisabled(componentName, enable);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setRecordDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRecordDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isMicrophoneDisabled()");
                return manager.isRecordDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isRecordDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setChangeWallpaperDisable(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.setChangeWallpaperDisable(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "rebootDevice error! e = " + e.getMessage());
        }
    }

    public boolean isChangeWallpaperDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.isChangeWallpaperDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "rebootDevice error! e= " + e.getMessage());
            return false;
        }
    }

    public void setExternalStorageDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setExternalStorageDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setExternalStorageDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.isExternalStorageDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "rebootDevice error! e= " + e.getMessage());
            return false;
        }
    }

    public void setUSBDataDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setUSBDataDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUSBDataDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isUSBDataDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.isUSBDataDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isUSBDataDisabled error! e= " + e.getMessage());
            return false;
        }
    }

    public void setUSBOtgDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setUSBOtgDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUSBOtgDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isUSBOtgDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.isUSBOtgDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "rebootDevice error! e= " + e.getMessage());
            return false;
        }
    }

    public boolean setCameraPolicies(int mode) {
        try {
            return getIDeviceRestrictionManager().setCameraPolicies(mode);
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setCameraPolicies fail!", e);
            return false;
        }
    }

    public int getCameraPolicies() {
        try {
            return getIDeviceRestrictionManager().getCameraPolicies();
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "getCameraPolicies fail!", e);
            return -1;
        }
    }

    public void setSafeModeDisabled(boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.setSafeModeDisabled(disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isSafeModeDisabled() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestriction manager:" + manager);
                return manager.isSafeModeDisabled();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestriction manager is null");
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addAppRestriction(ComponentName admin, int pattern, List<String> pkgs) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.addAppRestriction(admin, pattern, pkgs);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "addAppRestriction error!");
            e.printStackTrace();
        }
    }

    public void removeAppRestriction(ComponentName admin, int pattern, List<String> pkgs) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.removeAppRestriction(admin, pattern, pkgs);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "removeAppRestriction error!");
            e.printStackTrace();
        }
    }

    public void removeAllAppRestriction(ComponentName admin, int pattern) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.removeAllAppRestriction(admin, pattern);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "removeAllAppRestriction error!");
            e.printStackTrace();
        }
    }

    public void setAppRestrictionPolicies(ComponentName admin, int pattern) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.setAppRestrictionPolicies(admin, pattern);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setAppRestriction error!" + e);
        }
    }

    public int getAppRestrictionPolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getAppRestrictionPolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setAppRestriction error!" + e);
            return -1;
        }
    }

    public List<String> getAppRestriction(ComponentName admin, int pattern) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getAppRestriction(admin, pattern);
            }
            Log.d("DeviceRestrictionManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return null;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setAppRestriction error!" + e);
        }
    }

    public void setMmsDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setMmsDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setMmsDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isMmsDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = IDeviceRestrictionManager.Stub.asInterface(getOppoMdmManager("DeviceRestrictionManager"));
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isMmsDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isMmsDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setVoiceDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setVoiceDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setVoiceDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isVoiceDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isVoiceDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isVoiceDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDefaultVoiceCard(ComponentName componentName, int slotId, Message response) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                Bundle result = manager.setDefaultVoiceCard(componentName, slotId);
                if (result != null) {
                    response.setData(result);
                    boolean value = result.getBoolean("RESULT");
                    Log.d("DeviceRestrictionManager", "mdm setDefaultVoiceCard result: " + result.getBoolean("RESULT") + ", exception : " + result.getString("EXCEPTION"));
                    return value;
                }
                Bundle result2 = new Bundle();
                result2.putBoolean("RESULT", false);
                result2.putString("EXCEPTION", "GENERIC_FAILURE");
                response.setData(result2);
                return false;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setDefaultVoiceCard RemoteException error!");
            Bundle result3 = new Bundle();
            result3.putBoolean("RESULT", false);
            result3.putString("EXCEPTION", "GENERIC_FAILURE");
            response.setData(result3);
            return false;
        }
    }

    public int getDefaultVoiceCard(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getDefaultVoiceCard(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getDefaultVoiceCard RemoteException error!");
            return -1;
        }
    }

    public void setMobileDataMode(ComponentName componentName, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setMobileDataMode(componentName, mode);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setMobileDataMode error!");
            e.printStackTrace();
        }
    }

    public int getMobileDataMode(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isMicrophoneDisabled()");
                return manager.getMobileDataMode(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isMobileDataDisabled error!");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean setGpsPolicies(ComponentName admin, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.setGpsPolicies(admin, mode);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setGpsPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getGpsPolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager getGpsPolicies admin=" + admin);
                return manager.getGpsPolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -100;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getGpsPolicies error! e=" + e);
            return -100;
        }
    }

    public boolean setPowerDisable(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setPowerDisable(admin, disabled);
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setPowerDisable error!", e);
            return false;
        }
    }

    public boolean getPowerDisable(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().getPowerDisable(admin);
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "getPowerDisable error!", e);
            return false;
        }
    }

    public boolean setSplitScreenDisable(ComponentName admin, boolean disable) {
        try {
            return getIDeviceRestrictionManager().setSplitScreenDisable(admin, disable);
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setSplitScreenDisable error!", e);
            return false;
        }
    }

    public boolean getSplitScreenDisable(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().getSplitScreenDisable(admin);
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setSplitScreenDisable error!", e);
            return false;
        }
    }

    public boolean setUnknownSourceAppInstallDisabled(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setUnknownSourceAppInstallDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.d("DeviceRestrictionManager", "setUnknownSourceAppInstallDisabled fail", e);
            return false;
        }
    }

    public boolean isUnknownSourceAppInstallDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isUnknownSourceAppInstallDisabled(admin);
        } catch (RemoteException e) {
            Log.d("DeviceRestrictionManager", "isUnknownSourceAppInstallDisabled fail", e);
            return false;
        }
    }

    public boolean setMicrophonePolicies(ComponentName admin, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setMicrophonePolicies(" + mode + "+)");
                return manager.setMicrophonePolicies(admin, mode);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setMicrophonePolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getMicrophonePolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getMicrophonePolicies()");
                return manager.getMicrophonePolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getMicrophonePolicies error!");
            e.printStackTrace();
            return 1;
        }
    }

    public boolean setSpeakerPolicies(ComponentName admin, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setSpeakerPolicies(" + mode + "+)");
                return manager.setSpeakerPolicies(admin, mode);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSpeakerPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getSpeakerPolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getSpeakerPolicies()");
                return manager.getSpeakerPolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getSpeakerPolicies error!");
            e.printStackTrace();
            return 1;
        }
    }

    public boolean setAppUninstallationPolicies(int mode, String[] appPackageNames) {
        List<String> packagelist = new ArrayList<>();
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                if (appPackageNames != null) {
                    if (appPackageNames.length != 0) {
                        for (int i = 0; i < appPackageNames.length; i++) {
                            String name = appPackageNames[i];
                            if (name != null) {
                                if (!name.equals("")) {
                                    packagelist.add(appPackageNames[i]);
                                }
                            }
                            Log.d("DeviceRestrictionManager", "setAppUninstallationPolicies:there is a wrong packagename from the list" + name);
                        }
                        return manager.setAppUninstallationPolicies(mode, packagelist);
                    }
                }
                packagelist = null;
                return manager.setAppUninstallationPolicies(mode, packagelist);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
            throw new NullPointerException("manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setAppInstallationPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public String[] getAppUninstallationPolicies() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                List<String> packagelist = manager.getAppUninstallationPolicies();
                String[] list = new String[packagelist.size()];
                for (int i = 0; i < list.length; i++) {
                    list[i] = packagelist.get(i);
                }
                return list;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getAppUninstallationPolicies error!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean setScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager == null) {
                return false;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
            return manager.setScreenCaptureDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setScreenCaptureDisabled error!");
            return false;
        }
    }

    public boolean isScreenCaptureDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager == null) {
                return false;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
            return manager.isScreenCaptureDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isScreenCaptureDisabled error!");
            return false;
        }
    }

    public void setLanguageChangeDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setLanguageChangeDisabled()");
                manager.setLanguageChangeDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setLanguageChangeDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isLanguageChangeDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setLanguageChangeDisabled()");
                return manager.isLanguageChangeDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setLanguageChangeDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setAdbDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setAdbDisabled()");
                manager.setAdbDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setAdbDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isAdbDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isAdbDisabled()");
                return manager.isAdbDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isAdbDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setUnlockByFingerprintDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUnlockByFingerprintDisabled()");
                return manager.setUnlockByFingerprintDisabled(admin, disabled);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUnlockByFingerprintDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUnlockByFingerprintDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isUnlockByFingerprintDisabled()");
                return manager.isUnlockByFingerprintDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isUnlockByFingerprintDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setUnlockByFaceDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUnlockByFaceDisabled()");
                return manager.setUnlockByFaceDisabled(admin, disabled);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUnlockByFaceDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUnlockByFaceDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isUnlockByFaceDisabled()");
                return manager.isUnlockByFaceDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isUnlockByFaceDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setUnlockByFingerprintPolicies(ComponentName admin, int policy) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUnlockByFingerprintPolicies()");
                return manager.setUnlockByFingerprintPolicies(admin, policy);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUnlockByFingerprintPolicies error!");
            return false;
        }
    }

    public int getUnlockByFingerprintPolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getUnlockByFingerprintPolicies()");
                return manager.getUnlockByFingerprintPolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getUnlockByFingerprintPolicies error!");
            return 2;
        }
    }

    public boolean setUnlockByFacePolicies(ComponentName admin, int policy) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUnlockByFacePolicies()");
                return manager.setUnlockByFacePolicies(admin, policy);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUnlockByFacePolicies error!");
            return false;
        }
    }

    public int getUnlockByFacePolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getUnlockByFacePolicies()");
                return manager.getUnlockByFacePolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getUnlockByFacePolicies error!");
            return 2;
        }
    }

    public boolean setUserPasswordPolicies(ComponentName admin, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUserPasswordPolicies()");
                return manager.setUserPasswordPolicies(admin, mode);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUserPasswordPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getUserPasswordPolicies(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getUserPasswordPolicies()");
                return manager.getUserPasswordPolicies(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getUserPasswordPolicies error!");
            e.printStackTrace();
            return -1;
        }
    }

    public void setBrowserRestriction(int pattern) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setBrowserRestriction");
                manager.setBrowserRestriction(pattern);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager setBrowserRestriction is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setBrowserRestriction error!");
            e.printStackTrace();
        }
    }

    public void clearBrowserRestriction(int pattern) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:clearBrowserRestriction");
                manager.clearBrowserRestriction(pattern);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager clearBrowserRestriction is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "clearBrowserRestriction error!");
            e.printStackTrace();
        }
    }

    public void addBrowserRestriction(int pattern, List<String> urls) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service addBrowserRestriction manager:addBrowserRestriction");
                manager.addBrowserRestriction(pattern, urls);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service addBrowserRestriction addBrowserRestriction is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "addBrowserRestriction error!");
            e.printStackTrace();
        }
    }

    public void removeBrowserRestriction(int pattern, List<String> urls) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:removeBrowserRestriction");
                manager.removeBrowserRestriction(pattern, urls);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager removeBrowserRestriction is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "removeBrowserRestriction error!");
            e.printStackTrace();
        }
    }

    public List<String> queryBrowserHistory(int position, int pageSize) {
        List<String> data = new ArrayList<>();
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:queryBrowserHistory");
                return manager.queryBrowserHistory(position, pageSize);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager queryBrowserHistory is null");
            return data;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "removeBrowserRestriction error!");
            e.printStackTrace();
            return data;
        }
    }

    public List<String> getBrowserRestrictionUrls(int pattern) {
        List<String> data = new ArrayList<>();
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getBrowserRestrictionUrls");
                return manager.getBrowserRestrictionUrls(pattern);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager getBrowserRestrictionUrls is null");
            return data;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getBrowserRestrictionUrls error!");
            e.printStackTrace();
            return data;
        }
    }

    public boolean setTaskButtonDisabled(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setTaskButtonDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setTaskButtonDisabled error!", e);
            return false;
        }
    }

    public boolean isTaskButtonDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isTaskButtonDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isTaskButtonDisabled error!", e);
            return false;
        }
    }

    public boolean setFloatTaskDisabled(ComponentName admin, boolean disable) {
        try {
            return getIDeviceRestrictionManager().setFloatTaskDisabled(admin, disable);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setTaskButtonDisabled error!", e);
            return false;
        }
    }

    public boolean isFloatTaskDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isFloatTaskDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isTaskButtonDisabled error!", e);
            return false;
        }
    }

    public boolean setAirplanePolices(ComponentName admin, int policy) {
        try {
            return getIDeviceRestrictionManager().setAirplanePolices(admin, policy);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setAirplanePolices error!", e);
            return false;
        }
    }

    public int getAirplanePolices(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().getAirplanePolices(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getAirplanePolices error!", e);
            return -100;
        }
    }

    public boolean setHomeButtonDisabled(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setHomeButtonDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setHomeButtonDisabled error!", e);
            return false;
        }
    }

    public boolean isHomeButtonDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isHomeButtonDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isHomeButtonDisabled error!", e);
            return false;
        }
    }

    public boolean setBackButtonDisabled(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setBackButtonDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setBackButtonDisabled error!", e);
            return false;
        }
    }

    public boolean isBackButtonDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isBackButtonDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isBackButtonDisabled error!", e);
            return false;
        }
    }

    public boolean setSystemBrowserDisabled(ComponentName admin, boolean disabled) {
        try {
            return getIDeviceRestrictionManager().setSystemBrowserDisabled(admin, disabled);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSystemBrowserDisabled error!", e);
            return false;
        }
    }

    public boolean isSystemBrowserDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isSystemBrowserDisabled(admin);
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isSystemBrowserDisabled error!", e);
            return false;
        }
    }

    public boolean disableClipboard(ComponentName admin, boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.disableClipboard(admin, disable);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "disableClipboard error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isClipboardDisabled() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isClipboardDisabled();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isClipboardDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setRequiredStrongAuthTime(ComponentName admin, long timeoutMs) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setRequiredStrongAuthTime(admin, timeoutMs);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setRequiredStrongAuth error!");
        }
    }

    public long getRequiredStrongAuthTime(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getRequiredStrongAuthTime(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 0;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getRequiredStrongAuth error!");
            return 0;
        }
    }

    public boolean allowWifiCellularNetwork(ComponentName compName, String packageName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.allowWifiCellularNetwork(compName, packageName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "allowWifiCellularNetwork error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getDefaultDataCard(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getDefaultDataCard(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return 0;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getDefaultDataCard error!");
            return 0;
        }
    }

    public boolean setDefaultDataCard(ComponentName admin, int slot, Message response) {
        Bundle result = new Bundle();
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                Bundle result2 = manager.setDefaultDataCard(admin, slot);
                if (result2 == null) {
                    return false;
                }
                response.setData(result2);
                return result2.getBoolean("RESULT");
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setDefaultDataCard error!");
            result.putBoolean("RESULT", false);
            result.putString("EXCEPTION", "GENERIC_FAILURE");
            response.setData(result);
            return false;
        }
    }

    public boolean setDataRoamingDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.setDataRoamingDisabled(admin, disabled);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setIncomingThirdCallDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDataRoamingDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isDataRoamingDisabled(admin);
            }
            Log.e("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isDataRoamingDisabled error! e = " + e.getMessage());
            return false;
        }
    }

    public void setSlot1DataConnectivityDisabled(ComponentName admin, String value) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlot1DataConnectivityDisabled(admin, value);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSlot1DataConnectivityDisabled error!");
            e.printStackTrace();
        }
    }

    public void setSlot2DataConnectivityDisabled(ComponentName admin, String value) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlot2DataConnectivityDisabled(admin, value);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSlot2DataConnectivityDisabled error!");
            e.printStackTrace();
        }
    }

    public int getSlot1DataConnectivityDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getSlot1DataConnectivityDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getSlot1DataConnectivityDisabled e=" + e);
            return -1;
        }
    }

    public int getSlot2DataConnectivityDisabled(ComponentName admin) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getSlot2DataConnectivityDisabled(admin);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getSlot2DataConnectivityDisabled e=" + e);
            return -1;
        }
    }

    public void setVoiceOutgoingDisable(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setVoiceOutgoingDisable(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setVoiceOutgoingDisable error!");
            e.printStackTrace();
        }
    }

    public void setVoiceIncomingDisable(ComponentName admin, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setVoiceIncomingDisable(admin, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setVoiceIncomingDisable error!");
            e.printStackTrace();
        }
    }

    public boolean isVoiceOutgoingDisabled(ComponentName admin, int slotId) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager isVoiceOutgoingDisabled admin=" + admin);
                return manager.isVoiceOutgoingDisabled(admin, slotId);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isVoiceOutgoingDisabled error! e=" + e);
            return false;
        }
    }

    public boolean isVoiceIncomingDisabled(ComponentName admin, int slotId) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager isVoiceIncomingDisabled admin=" + admin);
                return manager.isVoiceIncomingDisabled(admin, slotId);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isVoiceIncomingDisabled error! e=" + e);
            return false;
        }
    }

    public void setUsbTetheringDisable(ComponentName componentName, boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setUsbTetheringDisable()");
                manager.setUsbTetheringDisable(componentName, disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setUsbTetheringDisable error!");
            e.printStackTrace();
        }
    }

    public boolean isUsbTetheringDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isUsbTetheringDisabled()");
                return manager.isUsbTetheringDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isUsbTetheringDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setSystemUpdatePolicies(ComponentName componentName, int mode) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setSystemUpdatePolicies()");
                return manager.setSystemUpdatePolicies(componentName, mode);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSystemUpdatePolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getSystemUpdatePolicies(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getSystemUpdatePolicies()");
                return manager.getSystemUpdatePolicies(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getSystemUpdatePolicies() error!");
            e.printStackTrace();
            return -1;
        }
    }

    public void setSettingsApplicationDisabled(ComponentName componentName, boolean disable) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setSettingsApplicationDisabled()");
                manager.setSettingsApplicationDisabled(componentName, disable);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setSettingsApplicationDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isSettingsApplicationDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:isSettingsApplicationDisabled()");
                return manager.isSettingsApplicationDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "isSettingsApplicationDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setNavigationBarDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setNavigationBarDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setNavigationBarDisabled error!", e);
        }
    }

    public boolean isNavigationBarDisabled(ComponentName componentName) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isNavigationBarDisabled(componentName);
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isNavigationBarDisabled error!", e);
            return false;
        }
    }

    public boolean isMultiAppSupport() {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.isMultiAppSupport();
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
            return true;
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "isMultiAppSupport error!", e);
            return true;
        }
    }

    public void setMultiAppSupport(boolean support) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setMultiAppSupport(support);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceRestrictionManager", "setMultiAppSupport error!", e);
        }
    }

    public void setPowerSavingModeDisabled(ComponentName admin, boolean disabled) {
        try {
            getIDeviceRestrictionManager().setPowerSavingModeDisabled(admin, disabled);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManager", "setPowerSavingModeDisabled fail!", e);
        }
    }

    public boolean isPowerSavingModeDisabled(ComponentName admin) {
        try {
            return getIDeviceRestrictionManager().isPowerSavingModeDisabled(admin);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManager", "isPowerSavingModeDisabled fail!", e);
            return false;
        }
    }

    public void setApplicationDisabledInLauncherOrRecentTask(List<String> list, int flag) {
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:setApplicationDisabledInLauncherOrRecentTask()");
                manager.setApplicationDisabledInLauncherOrRecentTask(list, flag);
                return;
            }
            Log.d("DeviceRestrictionManager", "mdm service setApplicationDisabledInLauncherOrRecentTask manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "setApplicationDisabledInLauncherOrRecentTask error!");
            e.printStackTrace();
        }
    }

    public List<String> getApplicationDisabledInLauncherOrRecentTask(int flag) {
        List<String> result = new ArrayList<>();
        try {
            IDeviceRestrictionManager manager = getIDeviceRestrictionManager();
            if (manager != null) {
                Log.d("DeviceRestrictionManager", "mdm service IDeviceRestrictionManager manager:getApplicationDisabledInLauncherOrRecentTask()");
                return manager.getApplicationDisabledInLauncherOrRecentTask(flag);
            }
            Log.d("DeviceRestrictionManager", "mdm service getApplicationDisabledInLauncherOrRecentTask manager is null");
            return result;
        } catch (RemoteException e) {
            Log.i("DeviceRestrictionManager", "getApplicationDisabledInLauncherOrRecentTask error!");
            e.printStackTrace();
            return result;
        }
    }
}
