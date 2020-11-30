package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeviceStateManager extends DeviceBaseManager {
    private static final String TAG = "DeviceStateManager";
    private static final Object mLock = new Object();
    private static volatile DeviceStateManager sInstance;
    private final int SOFTWARE_INFO_NUMBER = 7;

    public static final DeviceStateManager getInstance(Context context) {
        DeviceStateManager deviceStateManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceStateManager(context);
            }
            deviceStateManager = sInstance;
        }
        return deviceStateManager;
    }

    private DeviceStateManager(Context context) {
        super(context);
    }

    private IDeviceStateManager getIDeviceStateManager() {
        return IDeviceStateManager.Stub.asInterface(getOppoMdmManager("DeviceStateManager"));
    }

    public String[] getDeviceState() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                return manager.getDeviceState();
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "getDeviceState error!");
            e.printStackTrace();
            return null;
        }
    }

    public List<String[]> getAppRuntimeExceptionInfo() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                List<String> appExceps = manager.getAppRuntimeExceptionInfo();
                List<String[]> retAppExceps = new ArrayList<>();
                if (appExceps != null && appExceps.size() > 0) {
                    for (String packageName : appExceps) {
                        Log.d("DeviceStateManager", "getAppRuntimeExceptionInfo: " + packageName);
                        retAppExceps.add(packageName.split(":"));
                    }
                }
                return retAppExceps;
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "getAppRuntimeExceptionInfo error!");
            e.printStackTrace();
            return null;
        }
    }

    public void keepSrceenOn() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                manager.keepSrceenOn();
                return;
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "keepSrceenOn error!");
            e.printStackTrace();
        }
    }

    public void cancelSrceenOn() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                manager.cancelSrceenOn();
                return;
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "cancelSrceenOn error!");
            e.printStackTrace();
        }
    }

    public boolean getScreenState() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                return manager.getScreenState();
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "getScreenState error!");
            e.printStackTrace();
            return false;
        }
    }

    public List getAppRunInfo() {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getAppRunInfo();
            }
            Log.d("DeviceStateManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return new ArrayList();
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "setApplicationSettings error!");
            e.printStackTrace();
        }
    }

    public void ignoringBatteryOptimizations(ComponentName admin, String packageName) {
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager:" + manager);
                manager.ignoringBatteryOptimizations(admin, packageName);
                return;
            }
            Log.d("DeviceStateManager", "mdm service IDeviceStateManager manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceStateManager", "ignoringBatteryOptimizations fail!", e);
        }
    }

    public List<String[]> getAppPowerUsage() {
        try {
            Map<String, String> appPowerUsages = getIDeviceStateManager().getAppPowerUsage(null);
            if (appPowerUsages != null) {
                if (appPowerUsages.size() > 0) {
                    List<String[]> appPoserUsageResult = new ArrayList<>();
                    for (Map.Entry<String, String> entry : appPowerUsages.entrySet()) {
                        appPoserUsageResult.add(new String[]{entry.getKey(), entry.getValue()});
                    }
                    return appPoserUsageResult;
                }
            }
            return null;
        } catch (RemoteException e) {
            Log.e("DeviceStateManager", "getAppPowerUsage error!", e);
            return null;
        }
    }

    public List<String[]> getRunningApplication() {
        try {
            List<String> runningApps = getIDeviceStateManager().getRunningApplication(null);
            List<String[]> runningAppresult = new ArrayList<>();
            if (runningApps != null && runningApps.size() > 0) {
                Iterator<String> it = runningApps.iterator();
                while (it.hasNext()) {
                    runningAppresult.add(new String[]{it.next()});
                }
            }
            return runningAppresult;
        } catch (RemoteException e) {
            Log.e("DeviceStateManager", "getRunningApplication fail!", e);
            return Collections.emptyList();
        }
    }

    public void allowGetUsageStats(String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                getIDeviceStateManager().allowGetUsageStats(null, packageName);
            } catch (RemoteException e) {
                Log.d("DeviceStateManager", "allowGetUsageStats: fail", e);
            }
        }
    }

    public String[][] getSoftwareInfo() {
        try {
            Map<String, List<String>> softwareInfosMap = getIDeviceStateManager().getSoftwareInfo(null);
            if (softwareInfosMap == null || softwareInfosMap.size() <= 0) {
                return (String[][]) Array.newInstance(String.class, 0, 0);
            }
            String[][] softwareInfoResult = (String[][]) Array.newInstance(String.class, softwareInfosMap.size(), 7);
            int count = 0;
            for (List<String> singleInfo : softwareInfosMap.values()) {
                for (int j = 0; j < 7; j++) {
                    softwareInfoResult[count][j] = singleInfo.get(j);
                }
                count++;
            }
            return softwareInfoResult;
        } catch (RemoteException e) {
            Log.e("DeviceStateManager", "getSoftwareInfo error!", e);
            return (String[][]) Array.newInstance(String.class, 0, 0);
        }
    }

    public boolean getSystemIntegrity(ComponentName admin) {
        try {
            return getIDeviceStateManager().getSystemIntegrity(admin);
        } catch (RemoteException e) {
            Log.e("DeviceStateManager", "getSystemIntegrity fail!", e);
            return false;
        }
    }

    public long[] getAppTrafficInfo(ComponentName componentName, int uid) {
        long[] l = {0, 0};
        try {
            IDeviceStateManager manager = getIDeviceStateManager();
            if (manager != null) {
                Log.d("DeviceStateManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                return manager.getAppTrafficInfo(componentName, uid);
            }
            Log.d("DeviceStateManager", "mdm service IDeviceRestrictionManager manager is null");
            return l;
        } catch (RemoteException e) {
            Log.i("DeviceStateManager", "getAppTrafficInfo1 error!");
            e.printStackTrace();
        }
    }
}
