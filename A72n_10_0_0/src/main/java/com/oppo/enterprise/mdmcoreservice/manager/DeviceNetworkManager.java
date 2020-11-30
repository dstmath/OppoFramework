package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceNetworkManager extends DeviceBaseManager {
    private static final String TAG = "DeviceNetworkManager";
    private static final Object mLock = new Object();
    private static volatile DeviceNetworkManager sInstance;

    public static final DeviceNetworkManager getInstance(Context context) {
        DeviceNetworkManager deviceNetworkManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceNetworkManager(context);
            }
            deviceNetworkManager = sInstance;
        }
        return deviceNetworkManager;
    }

    private DeviceNetworkManager(Context context) {
        super(context);
    }

    private IDeviceNetworkManager getIDeviceNetworkManager() {
        return IDeviceNetworkManager.Stub.asInterface(getOppoMdmManager("DeviceNetworkManager"));
    }

    public void addApn(ComponentName componentName, Map apnInfo) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.addApn(componentName, apnInfo);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "addApn error!");
            e.printStackTrace();
        }
    }

    public void removeApn(ComponentName componentName, String apnId) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.removeApn(componentName, apnId);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "removeApn error!");
            e.printStackTrace();
        }
    }

    public void updateApn(ComponentName componentName, Map apnInfo, String apnId) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.updateApn(componentName, apnInfo, apnId);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "updateApn error!");
            e.printStackTrace();
        }
    }

    public List queryApn(ComponentName componentName, Map apnInfo) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:queryApn()");
                return manager.queryApn(componentName, apnInfo);
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "queryApn error!");
            e.printStackTrace();
            return null;
        }
    }

    public Map getApnInfo(ComponentName componentName, String apnId) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:queryApn()");
                return manager.getApnInfo(componentName, apnId);
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "getApnInfo error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setPreferApn(ComponentName componentName, String apnId) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.setPreferApn(componentName, apnId);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "setPreferApn error!");
            e.printStackTrace();
        }
    }

    public void setUserApnMgrPolicies(ComponentName componentName, int disabled) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.setAccessPointNameDisabled(componentName, disabled);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "setAccessPointNameDisabled error!");
            e.printStackTrace();
        }
    }

    public int getUserApnMgrPolicies() {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager:" + manager);
                return manager.getUserApnMgrPolicies();
            }
            Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "getUserApnMgrPolicies error!");
            e.printStackTrace();
            return -1;
        }
    }

    public int getPreferApn() {
        String apnId = "";
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager:" + manager);
                apnId = manager.getCurrentApn();
            } else {
                Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "getPreferApn error!");
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(apnId)) {
            return -1;
        }
        return Integer.parseInt(apnId);
    }

    public void restoreDefaultApnOnSystem() {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager:" + manager);
                manager.restoreDefaultApnOnSystem();
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service getUserApnMgrPolicies manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "restoreDefaultApnOnSystem error!");
            e.printStackTrace();
        }
    }

    public String getOperatorNumeric() {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                return manager.getOperatorNumeric();
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "getOperatorNumeric error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setNetworkRestriction(ComponentName componentName, int pattern) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.setNetworkRestriction(componentName, pattern);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "setNetworkRestriction error!");
            e.printStackTrace();
        }
    }

    public void addNetworkRestriction(ComponentName componentName, int pattern, List<String> list) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.addNetworkRestriction(componentName, pattern, list);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "addNetworkRestriction error!");
            e.printStackTrace();
        }
    }

    public void removeNetworkRestriction(ComponentName componentName, int pattern, List<String> list) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.removeNetworkRestriction(componentName, pattern, list);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "removeNetworkRestriction error!");
            e.printStackTrace();
        }
    }

    public void removeNetworkRestrictionAll(ComponentName componentName, int pattern) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager:" + manager);
                manager.removeNetworkRestrictionAll(componentName, pattern);
                return;
            }
            Log.d("DeviceNetworkManager", "mdm service IDeviceNetworkManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceNetworkManager", "removeNetworkRestriction error!");
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNetworkRestriction(ComponentName admin, int pattern) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                list = manager.getNetworkRestriction(admin, pattern);
            } else {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            }
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "getNetworkRestriction error!");
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public boolean addAppMeteredDataBlackList(ComponentName admin, List<String> pkgs) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                return manager.addAppMeteredDataBlackList(admin, pkgs);
            }
            Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "addAppMeteredDataBlackList error!");
            return false;
        }
    }

    public boolean removeAppMeteredDataBlackList(ComponentName admin, List<String> pkgs) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                return manager.removeAppMeteredDataBlackList(admin, pkgs);
            }
            Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "removeAppMeteredDataBlackList error!");
            return false;
        }
    }

    public ArrayList<String> getAppMeteredDataBlackList(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                list = manager.getAppMeteredDataBlackList(admin);
            } else {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            }
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "getAppMeteredDataBlackList error!");
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }

    public boolean addAppWlanDataBlackList(ComponentName admin, List<String> pkgs) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                return manager.addAppWlanDataBlackList(admin, pkgs);
            }
            Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "addAppWlanDataBlackList error!");
            return false;
        }
    }

    public boolean removeAppWlanDataBlackList(ComponentName admin, List<String> pkgs) {
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                return manager.removeAppWlanDataBlackList(admin, pkgs);
            }
            Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "removeAppWlanDataBlackList error!");
            return false;
        }
    }

    public ArrayList<String> getAppWlanDataBlackList(ComponentName admin) {
        List<String> list = new ArrayList<>();
        try {
            IDeviceNetworkManager manager = getIDeviceNetworkManager();
            if (manager != null) {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager:" + manager);
                list = manager.getAppWlanDataBlackList(admin);
            } else {
                Log.d("DeviceNetworkManager", "mdmservice IDeviceNetworkManager manager is null");
            }
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManager", "getAppWlanDataBlackList error!");
        }
        if (list == null) {
            return null;
        }
        return new ArrayList<>(list);
    }
}
