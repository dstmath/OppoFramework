package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.DeviceVpnProfile;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager;
import java.util.List;

public class DeviceVpnManager extends DeviceBaseManager {
    private static final String TAG = "DeviceVpnManager";
    private static final Object mLock = new Object();
    private static volatile DeviceVpnManager sInstance;

    public static final DeviceVpnManager getInstance(Context context) {
        DeviceVpnManager deviceVpnManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceVpnManager(context);
            }
            deviceVpnManager = sInstance;
        }
        return deviceVpnManager;
    }

    public DeviceVpnManager(Context context) {
        super(context);
    }

    private IDeviceVpnManager getIDeviceVpnManager() {
        return IDeviceVpnManager.Stub.asInterface(getOppoMdmManager("DeviceVpnManager"));
    }

    public void setVpnProfile(ComponentName componentName, DeviceVpnProfile vpnProfile) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                manager.setVpnProfile(vpnProfile, componentName);
                return;
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "setVpnProfile error!");
            e.printStackTrace();
        }
    }

    public List<String> getVpnList(ComponentName componentName) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                return manager.getVpnList(componentName);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "getVpnList error!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteVpnProfile(ComponentName componentName, String key) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                return manager.deleteVpnProfile(componentName, key);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "deleteVpnProfile error!");
            e.printStackTrace();
            return false;
        }
    }

    public DeviceVpnProfile getVpnProfile(ComponentName componentName, String key) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                return manager.getVpnProfile(componentName, key);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "getVpnProfile error!");
            e.printStackTrace();
            return null;
        }
    }

    public int getVpnServiceState(ComponentName admin) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                return manager.getVpnServiceState(admin);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "getVpnServiceState error!");
            e.printStackTrace();
            return -1;
        }
    }

    public int disestablishVpnConnection(ComponentName admin) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager:" + manager);
                return manager.disestablishVpnConnection(admin);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "disestablishVpnConnection error!");
            e.printStackTrace();
            return -1;
        }
    }

    public void setVpnDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setVpnDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "setVpnDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isVpnDisabled(ComponentName admin) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceRestrictionManager isSystemUpdateDisabled admin=" + admin);
                return manager.isVpnDisabled(admin);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "isVpnDisabled error! e=" + e);
            return false;
        }
    }

    public boolean setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdown) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager setAlwaysOnVpnPackage admin=" + admin);
                return manager.setAlwaysOnVpnPackage(admin, vpnPackage, lockdown);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "setAlwaysOnVpnPackage error! e=" + e);
            return false;
        }
    }

    public String getAlwaysOnVpnPackage(ComponentName admin) {
        try {
            IDeviceVpnManager manager = getIDeviceVpnManager();
            if (manager != null) {
                Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager getAlwaysOnVpnPackage admin=" + admin);
                return manager.getAlwaysOnVpnPackage(admin);
            }
            Log.d("DeviceVpnManager", "mdm service IDeviceVpnManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceVpnManager", "getAlwaysOnVpnPackage error! e=" + e);
            return null;
        }
    }
}
