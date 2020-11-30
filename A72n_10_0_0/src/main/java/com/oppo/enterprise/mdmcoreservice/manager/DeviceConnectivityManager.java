package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager;
import java.util.ArrayList;
import java.util.List;

public class DeviceConnectivityManager extends DeviceBaseManager {
    private static final String TAG = "DeviceConnectivityManager";
    private static final Object mLock = new Object();
    private static volatile DeviceConnectivityManager sInstance;

    public static final DeviceConnectivityManager getInstance(Context context) {
        DeviceConnectivityManager deviceConnectivityManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceConnectivityManager(context);
            }
            deviceConnectivityManager = sInstance;
        }
        return deviceConnectivityManager;
    }

    private DeviceConnectivityManager(Context context) {
        super(context);
    }

    private IDeviceConnectivityManager getIDeviceConnectivityManager() {
        return IDeviceConnectivityManager.Stub.asInterface(getOppoMdmManager("DeviceConnectivityManager"));
    }

    public boolean addBluetoothDevicesToBlackList(ComponentName admin, ArrayList<String> devices) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addBluetoothDevicesToBlackList(admin, devices);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addBluetoothDevicesToBlackList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addBluetoothDevicesToWhiteList(ComponentName admin, ArrayList<String> devices) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addBluetoothDevicesToWhiteList(admin, devices);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addBluetoothDevicesToWhiteList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBluetoothDevicesFromBlackList(ComponentName admin, ArrayList<String> devices) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeBluetoothDevicesFromBlackList(admin, devices);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeBluetoothDevicesFromBlackList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBluetoothDevicesFromWhiteList(ComponentName admin, ArrayList<String> devices) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeBluetoothDevicesFromWhiteList(admin, devices);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeBluetoothDevicesFromWhiteList error!");
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getBluetoothDevicesFromBlackLists(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getBluetoothDevicesFromBlackLists(admin);
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getBluetoothDevicesFromBlackLists error!");
            e.printStackTrace();
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public ArrayList<String> getBluetoothDevicesFromWhiteLists(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getBluetoothDevicesFromWhiteLists(admin);
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getBluetoothDevicesFromWhiteLists error!");
            e.printStackTrace();
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public boolean isBlackListedDevice(ComponentName admin, String device) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isBlackListedDevice(admin, device);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isBlackListedDevice error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isWhiteListedDevice(ComponentName admin, String device) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWhiteListedDevice(admin, device);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWhiteListedDevice error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSSIDToBlackList(ComponentName admin, ArrayList<String> ssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addSSIDToBlackList(admin, ssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addSSIDToBlackList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSSIDFromBlackList(ComponentName admin, ArrayList<String> ssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeSSIDFromBlackList(admin, ssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeSSIDFromBlackList error!");
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getSSIDBlackList(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getSSIDBlackList(admin);
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getSSIDBlackList error!");
            e.printStackTrace();
        }
        return new ArrayList<>(list);
    }

    public boolean isBlackListedSSID(ComponentName admin, String ssid) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isBlackListedSSID(admin, ssid);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isBlackListedSSID error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSSIDToWhiteList(ComponentName admin, ArrayList<String> ssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addSSIDToWhiteList(admin, ssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addSSIDToWhiteList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSSIDFromWhiteList(ComponentName admin, ArrayList<String> ssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeSSIDFromWhiteList(admin, ssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeSSIDFromWhiteList error!");
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getSSIDWhiteList(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getSSIDWhiteList(admin);
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getSSIDWhiteList error!");
            e.printStackTrace();
        }
        return new ArrayList<>(list);
    }

    public boolean isWhiteListedSSID(String ssid) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWhiteListedSSID(ssid);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWhiteListedSSID error!");
            return false;
        }
    }

    public boolean addBSSIDToWhiteList(ArrayList<String> bssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addBSSIDToWhiteList(bssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addBSSIDToWhiteList error!");
            return false;
        }
    }

    public boolean addBSSIDToBlackList(ArrayList<String> bssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.addBSSIDToBlackList(bssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "addBSSIDToBlackList error!");
            return false;
        }
    }

    public boolean removeBSSIDFromWhiteList(ArrayList<String> bssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeBSSIDFromWhiteList(bssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeBSSIDFromWhiteList error!");
            return false;
        }
    }

    public boolean removeBSSIDFromBlackList(ArrayList<String> bssids) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeBSSIDFromBlackList(bssids);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeBSSIDFromBlackList error!");
            return false;
        }
    }

    public ArrayList<String> getBSSIDWhiteList() {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getBSSIDWhiteList();
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getBSSIDWhiteList error!");
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public ArrayList<String> getBSSIDBlackList() {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                list = manager.getBSSIDBlackList();
            } else {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getBSSIDBlackList error!");
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public boolean isWhiteListedBSSID(String bssid) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWhiteListedBSSID(bssid);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWhiteListedBSSID error!");
            return false;
        }
    }

    public boolean isBlackListedBSSID(String bssid) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isBlackListedBSSID(bssid);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isBlackListedBSSID error!");
            return false;
        }
    }

    public boolean setWlanPolicies(ComponentName admin, int mode) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWlanPolicies(admin, mode);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWlanPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getWlanPolicies(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWlanPolicies(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getWlanPolicies error!");
            e.printStackTrace();
            return 2;
        }
    }

    public boolean setWifiApPolicies(ComponentName admin, int mode) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWifiApPolicies(admin, mode);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceConnectivityManager", "setWifiApPolicies error!");
            return false;
        }
    }

    public int getWifiApPolicies(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWifiApPolicies(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.e("DeviceConnectivityManager", "getWifiApPolicies error!");
            return 2;
        }
    }

    public boolean setBluetoothPolicies(ComponentName admin, int mode) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setBluetoothPolicies(admin, mode);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setBluetoothPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getBluetoothPolicies(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getBluetoothPolicies(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getBluetoothPolicies error!");
            e.printStackTrace();
        }
    }

    public boolean setNfcPolicies(ComponentName admin, int mode) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setNfcPolicies(admin, mode);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setNfcPolicies error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getNfcPolicies(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getNfcPolicies(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return 2;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getNfcPolicies error!");
            e.printStackTrace();
            return 2;
        }
    }

    public boolean setSecurityLevel(ComponentName admin, int level) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setSecurityLevel(admin, level);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setSecurityLevel error!");
            e.printStackTrace();
        }
    }

    public int getSecurityLevel(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getSecurityLevel(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getSecurityLevel error!");
            e.printStackTrace();
        }
    }

    public boolean isUnSecureSoftApDisabled(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isUnSecureSoftApDisabled(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isUnSecureSoftApDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean setUnSecureSoftApDisabled(ComponentName admin, boolean disable) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setUnSecureSoftApDisabled(admin, disable);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setUnSecureSoftApDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean setWifiAutoConnectionDisabled(boolean disable) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWifiAutoConnectionDisabled(disable);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWifiAutoConnectionDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isWifiAutoConnectionDisabled() {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWifiAutoConnectionDisabled();
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWifiAutoConnectionDisabled error!");
            e.printStackTrace();
        }
    }

    public String getDevicePosition(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getDevicePosition(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getDevicePosition error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setWlanApClientBlackList(ComponentName admin, List<String> list) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                manager.setWlanApClientBlackList(admin, list);
                return;
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWlanApClientBlackList error!");
            e.printStackTrace();
        }
    }

    public List<String> getWlanApClientBlackList(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWlanApClientBlackList(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getWlanApClientBlackList error!");
            e.printStackTrace();
            return null;
        }
    }

    public void removeWlanApClientBlackList(ComponentName admin, List<String> list) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                manager.removeWlanApClientBlackList(admin, list);
                return;
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "removeWlanApClientBlackList error!");
            e.printStackTrace();
        }
    }

    public boolean setWlanConfiguration(ComponentName admin, List<String> devices) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWlanConfiguration(admin, devices);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWlanConfiguration error!");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getWlanConfiguration(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWlanConfiguration(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "getWlanConfiguration error!");
            e.printStackTrace();
            return null;
        }
    }

    public String getWifiMacAddress(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWifiMacAddress(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.e("DeviceConnectivityManager", "getWifiMacAddress error!");
            return null;
        }
    }

    public boolean isUserProfilesDisabled(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isUserProfilesDisabled(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isUserProfilesDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setUserProfilesDisabled(ComponentName admin, boolean disable) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setUserProfilesDisabled(admin, disable);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setUserProfilesDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setWifiProfile(ComponentName admin, WifiConfiguration config) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWifiProfile(admin, config);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWifiProfile error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeWifiProfile(ComponentName admin, WifiConfiguration config) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.removeWifiProfile(admin, config);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWifiProfile error!");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getWifiProfileList(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.getWifiProfileList(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return list;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWifiProfile error!");
            e.printStackTrace();
            return list;
        }
    }

    public void turnOnGPS(ComponentName admin, boolean on) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                manager.turnOnGPS(admin, on);
                return;
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "turnOnGPS error!");
            e.printStackTrace();
        }
    }

    public boolean isGPSTurnOn(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isGPSTurnOn(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isGPSTurnOn error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setGPSDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                manager.setGPSDisabled(admin, disabled);
                return;
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setGPSDisabled error!");
            e.printStackTrace();
        }
    }

    public boolean isGPSDisabled(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isGPSDisabled(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isGPSDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setWifiEditDisabled(ComponentName admin, boolean disabled) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.setWifiEditDisabled(admin, disabled);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "setWifiEditDisabled error! " + e.toString());
            return false;
        }
    }

    public boolean isWifiEditDisabled(ComponentName admin) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWifiEditDisabled(admin);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWifiEditDisabled error! " + e.toString());
            return false;
        }
    }

    public boolean isWifiProfileSet(ComponentName admin, WifiConfiguration config) {
        try {
            IDeviceConnectivityManager manager = getIDeviceConnectivityManager();
            if (manager != null) {
                Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager:" + manager);
                return manager.isWifiProfileSet(admin, config);
            }
            Log.d("DeviceConnectivityManager", "mdm service IDeviceConnectivityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceConnectivityManager", "isWifiProfileSet error! " + e.toString());
            return false;
        }
    }
}
