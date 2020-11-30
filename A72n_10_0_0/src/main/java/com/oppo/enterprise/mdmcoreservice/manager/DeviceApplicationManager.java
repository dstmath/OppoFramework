package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager;
import java.util.ArrayList;
import java.util.List;

public class DeviceApplicationManager extends DeviceBaseManager {
    private static final String TAG = "DeviceApplicationManager";
    private static final Object mLock = new Object();
    private static volatile DeviceApplicationManager sInstance;
    private final int SOFTWARE_INFO_NUMBER = 7;

    public static final DeviceApplicationManager getInstance(Context context) {
        DeviceApplicationManager deviceApplicationManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceApplicationManager(context);
            }
            deviceApplicationManager = sInstance;
        }
        return deviceApplicationManager;
    }

    private DeviceApplicationManager(Context context) {
        super(context);
    }

    public void addPersistentApp(ComponentName admin, List<String> packageNames) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.addPersistentApp(admin, packageNames);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "addPersistentApp error!");
            e.printStackTrace();
        }
    }

    public void removePersistentApp(ComponentName admin, List<String> packageNames) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.removePersistentApp(admin, packageNames);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "removePersistentApp error!");
            e.printStackTrace();
        }
    }

    public List<String> getPersistentApp(ComponentName admin) {
        List<String> listApps = new ArrayList<>();
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getPersistentApp(admin);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return listApps;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "getPersistentApp error!");
            e.printStackTrace();
            return listApps;
        }
    }

    public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.addDisallowedRunningApp(admin, packageNames);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "addDisallowedRunningApp error!");
            e.printStackTrace();
        }
    }

    public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.removeDisallowedRunningApp(admin, packageNames);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "removeDisallowedRunningApp error!");
            e.printStackTrace();
        }
    }

    public void removeAllDisallowedRunningApp(ComponentName admin) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.removeAllDisallowedRunningApp(admin);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "removeDisallowedRunningApp error!");
            e.printStackTrace();
        }
    }

    public List<String> getDisallowedRunningApp(ComponentName admin) {
        List<String> listApps = new ArrayList<>();
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getDisallowedRunningApp(admin);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return listApps;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "getDisallowedRunningApp error!");
            e.printStackTrace();
            return listApps;
        }
    }

    public void addTrustedAppStore(ComponentName compName, String appStorePkgName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.addTrustedAppStore(compName, appStorePkgName);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "addTrustedAppStore error!");
            e.printStackTrace();
        }
    }

    public void deleteTrustedAppStore(ComponentName compName, String appStorePkgName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.deleteTrustedAppStore(compName, appStorePkgName);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "deleteTrustedAppStore error!");
            e.printStackTrace();
        }
    }

    public void enableTrustedAppStore(ComponentName compName, boolean enable) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.enableTrustedAppStore(compName, enable);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "enableTrustedAppStore error!");
            e.printStackTrace();
        }
    }

    public boolean isTrustedAppStoreEnabled(ComponentName compName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.isTrustedAppStoreEnabled(compName);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "isTrustedAppStoreEnabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getTrustedAppStore(ComponentName compName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getTrustedAppStore(compName);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "getTrustedAppStore error!");
            e.printStackTrace();
            return null;
        }
    }

    public Bundle getApplicationSettings(ComponentName compName, String tag, String cmdName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getApplicationSettings(compName, tag, cmdName);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "getApplicationSettings error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setApplicationSettings(ComponentName compName, String tag, Bundle cmdBundle) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.setApplicationSettings(compName, tag, cmdBundle);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "setApplicationSettings error!");
            e.printStackTrace();
        }
    }

    public void setComponentSettings(ComponentName compName, int newState) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                manager.setComponentSettings(compName, newState);
                return;
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "setComponentSettings error!");
        }
    }

    public int getComponentSettings(ComponentName compName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager:" + manager);
                return manager.getComponentSettings(compName);
            }
            Log.d("DeviceApplicationManager", "mdm service IOppoMdmDeviceApplication manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DeviceApplicationManager", "getComponentSettings error!");
            return -1;
        }
    }

    public boolean setDisabledAppList(ComponentName componentName, List<String> pkgs, int mode) {
        boolean ret = false;
        if (pkgs == null) {
            return false;
        }
        try {
            ret = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).setDisabledAppList(componentName, pkgs, mode);
            Log.d("DeviceApplicationManager", "setDisabledAppList: succeeded");
            return ret;
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "disallowDrawOverlays fail!", e);
            return ret;
        }
    }

    public List<String> getDisabledAppList(ComponentName componentName) {
        List<String> listApps = null;
        try {
            listApps = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).getDisabledAppList(componentName);
            Log.d("DeviceApplicationManager", "getDisabledAppList: succeeded");
            return listApps;
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "getDisabledAppList fail!", e);
            return listApps;
        }
    }

    public boolean forceStopPackage(ComponentName admin, List<String> pkgs) {
        boolean ret = false;
        if (pkgs == null) {
            return false;
        }
        try {
            ret = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).forceStopPackage(admin, pkgs);
            Log.d("DeviceApplicationManager", "forceStopPackage: succeeded");
            return ret;
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "forceStopPackage fail!", e);
            return ret;
        }
    }

    public void killApplicationProcess(ComponentName admin, String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).killApplicationProcess(admin, packageName);
            } catch (RemoteException e) {
                Log.e("DeviceApplicationManager", "killApplicationProcess fail!", e);
            }
        }
    }

    public String getTopAppPackageName(ComponentName admin) {
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).getTopAppPackageName(admin);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "getTopAppPackageName fail!", e);
            return "";
        }
    }

    public void cleanBackgroundProcess(ComponentName compName) {
        try {
            IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).cleanBackgroundProcess(compName);
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "cleanBackgroundProcess fail!", e);
        }
    }

    public boolean setDrawOverlays(ComponentName admin, String packageName, int mode) {
        if (TextUtils.isEmpty(packageName) || mode < 0 || mode > 2) {
            return false;
        }
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).setDrawOverlays(admin, packageName, mode);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "setDrawOverlays fail");
            return false;
        }
    }

    public int getDrawOverlays(ComponentName admin, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).getDrawOverlays(admin, packageName);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "getDrawOverlays fail");
            return -1;
        }
    }

    public void addAppAlarmWhiteList(ComponentName admin, List<String> packageNames) {
        try {
            IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).addAppAlarmWhiteList(admin, packageNames);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "addAppAlarmWhiteList fail!", e);
        }
    }

    public List<String> getAppAlarmWhiteList(ComponentName admin) {
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).getAppAlarmWhiteList(admin);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "getAppAlarmWhiteList fail!", e);
            return null;
        }
    }

    public boolean removeAppAlarmWhiteList(ComponentName admin, List<String> packageNames) {
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).removeAppAlarmWhiteList(admin, packageNames);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "removeAppAlarmWhiteList fail!", e);
            return false;
        }
    }

    public boolean removeAllAppAlarmWhiteList(ComponentName admin) {
        try {
            return IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager")).removeAllAppAlarmWhiteList(admin);
        } catch (Exception e) {
            Log.e("DeviceApplicationManager", "removeAllAppAlarmWhiteList fail!", e);
            return false;
        }
    }

    public void addScreenPinningApp(ComponentName componentName, String packageName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                manager.addScreenPinningApp(componentName, packageName);
                Log.d("DeviceApplicationManager", "addScreenPinningApp: succeeded");
            }
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "addScreenPinningApp fail!", e);
        }
    }

    public void clearScreenPinningApp(ComponentName componentName, String packageName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager != null) {
                manager.clearScreenPinningApp(componentName, packageName);
                Log.d("DeviceApplicationManager", "clearScreenPinningApp: succeeded");
            }
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "clearScreenPinningApp fail!", e);
        }
    }

    public String getScreenPinningApp(ComponentName componentName) {
        try {
            IDeviceApplicationManager manager = IDeviceApplicationManager.Stub.asInterface(getOppoMdmManager("DeviceApplicationManager"));
            if (manager == null) {
                return "";
            }
            String app = manager.getScreenPinningApp(componentName);
            Log.d("DeviceApplicationManager", "getScreenPinningApp: succeeded");
            return app;
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManager", "getScreenPinningApp fail!", e);
            return "";
        }
    }
}
