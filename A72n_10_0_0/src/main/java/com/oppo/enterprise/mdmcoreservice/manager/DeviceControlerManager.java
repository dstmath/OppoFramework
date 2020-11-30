package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager;
import java.util.ArrayList;
import java.util.List;

public class DeviceControlerManager extends DeviceBaseManager {
    private static final String TAG = "DeviceControlerManager";
    private static final Object mLock = new Object();
    private static volatile DeviceControlerManager sInstance;

    public static final DeviceControlerManager getInstance(Context context) {
        DeviceControlerManager deviceControlerManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceControlerManager(context);
            }
            deviceControlerManager = sInstance;
        }
        return deviceControlerManager;
    }

    private DeviceControlerManager(Context context) {
        super(context);
    }

    private IDeviceControlerManager getIDeviceControlerManager() {
        return IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
    }

    public void shutdownDevice(ComponentName componentName) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.shutdownDevice(null);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "shutdownDevice error!");
            e.printStackTrace();
        }
    }

    public void rebootDevice(ComponentName componentName) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.rebootDevice(null);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "rebootDevice error!");
            e.printStackTrace();
        }
    }

    public boolean formatSDCard(ComponentName componentName, String diskId) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.formatSDCard(null, diskId);
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "formatSDCard error!");
            e.printStackTrace();
            return false;
        }
    }

    public void enableAccessibilityService(ComponentName admin, ComponentName componentName) {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.enableAccessibilityService(admin, componentName);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "enableAccessibilityService error!");
            e.printStackTrace();
        }
    }

    public void disableAccessibilityService(ComponentName admin, ComponentName componentName) {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.disableAccessibilityService(admin, componentName);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "disableAccessibilityService error!");
        }
    }

    public List<ComponentName> getAccessibilityService(ComponentName admin) {
        List<ComponentName> enabledServicesList = new ArrayList<>();
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.getAccessibilityService(admin);
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
            return new ArrayList();
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "getAccessibilityService error!");
            return enabledServicesList;
        }
    }

    public boolean isAccessibilityServiceEnabled(ComponentName admin) {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.isAccessibilityServiceEnabled(admin);
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "isAccessibilityServiceEnabled error!");
            return false;
        }
    }

    public void addAccessibilityServiceToWhiteList(List<String> pkgList) {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.addAccessibilityServiceToWhiteList(pkgList);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "addAccessibilityServiceToWhiteList fail!", e);
        }
    }

    public void removeAccessibilityServiceFromWhiteList(List<String> pkgList) {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.removeAccessibilityServiceFromWhiteList(pkgList);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "removeAccessibilityServiceFromWhiteList fail!", e);
        }
    }

    public List<String> getAccessibilityServiceWhiteList() {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                return manager.getAccessibilityServiceWhiteList();
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
            return null;
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "getAccessibilityServiceWhiteList fail!", e);
            return null;
        }
    }

    public void deleteAccessibilityServiceWhiteList() {
        try {
            IDeviceControlerManager manager = IDeviceControlerManager.Stub.asInterface(getOppoMdmManager("DeviceControlerManager"));
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.deleteAccessibilityServiceWhiteList();
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "deleteAccessibilityServiceWhiteList fail!", e);
        }
    }

    public boolean setSysTime(ComponentName admin, long mills) {
        try {
            return getIDeviceControlerManager().setSysTime(admin, mills);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setSysTime fail!", e);
            return false;
        }
    }

    public boolean setDefaultLauncher(ComponentName admin, ComponentName home) {
        try {
            return getIDeviceControlerManager().setDefaultLauncher(admin, home);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setDefaultLauncher fail!", e);
            return false;
        }
    }

    public String getDefaultLauncher() {
        try {
            return getIDeviceControlerManager().getDefaultLauncher();
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "getDefaultLauncher fail!", e);
            return null;
        }
    }

    public void clearDefaultLauncher(ComponentName admin) {
        try {
            getIDeviceControlerManager().clearDefaultLauncher(admin);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setDefaultLauncher fail!", e);
        }
    }

    public boolean wipeDeviceData() {
        try {
            return getIDeviceControlerManager().wipeDeviceData();
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "wipeDeviceData fail!", e);
            return false;
        }
    }

    public void setCustomSettingsMenu(ComponentName admin, List<String> deleteMenus) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager:" + manager);
                manager.setCustomSettingsMenu(admin, deleteMenus);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IOppoMdmDeviceControl manager is null");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setAirplaneMode(ComponentName admin, boolean on) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setAirplaneMode(admin, on);
                return;
            }
            Log.d("DeviceControlerManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "setAirplaneMode error!");
            e.printStackTrace();
        }
    }

    public boolean getAirplaneMode(ComponentName admin) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            if (manager != null) {
                Log.d("DeviceControlerManager", "mdm service IDeviceRestrictionManager isAirplaneEnabled admin = " + admin);
                return manager.getAirplaneMode(admin);
            }
            Log.d("DeviceControlerManager", "mdm service IDeviceRestrictionManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceControlerManager", "getAirplaneMode error! e=" + e);
            return false;
        }
    }

    public void getCertInstaller() {
        try {
            getIDeviceControlerManager().getCertInstaller();
            Log.d("DeviceControlerManager", "getCertInstaller: succeeded");
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "getCertInstaller fail!", e);
        }
    }

    public boolean setDisableKeyguardForgetPassword(ComponentName cn, boolean disable) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "setDisableKeyguardForgetPassword: succeeded");
            return manager.setDisableKeyguardForgetPassword(cn, disable);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setDisableKeyguardForgetPassword fail!", e);
            return false;
        }
    }

    public boolean isDisableKeyguardForgetPassword(ComponentName cn) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "isDisableKeyguardForgetPassword: succeeded");
            return manager.isDisableKeyguardForgetPassword(cn);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "isDisableKeyguardForgetPassword fail!", e);
            return false;
        }
    }

    public void setKeyguardPolicy(ComponentName cn, int[] policies) {
        try {
            getIDeviceControlerManager().setKeyguardPolicy(cn, policies);
            Log.d("DeviceControlerManager", "setKeyguardPolicy: succeeded");
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setKeyguardPolicy fail!", e);
        }
    }

    public int[] getKeyguardPolicy(ComponentName cn) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "getKeyguardPolicy: succeeded");
            return manager.getKeyguardPolicy(cn);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "getKeyguardPolicy fail!", e);
            return null;
        }
    }

    public boolean setDefaultInputMethod(String packagename) {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "setDefaultInputMethod: succeeded");
            return manager.setDefaultInputMethod(packagename);
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "setDefaultInputMethod fail!", e);
            return false;
        }
    }

    public String getDefaultInputMethod() {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "getDefaultInputMethod: succeeded");
            return manager.getDefaultInputMethod();
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "getDefaultInputMethod fail!", e);
            return "";
        }
    }

    public void clearDefaultInputMethod() {
        try {
            IDeviceControlerManager manager = getIDeviceControlerManager();
            Log.d("DeviceControlerManager", "clearDefaultInputMethod: succeeded");
            manager.clearDefaultInputMethod();
        } catch (RemoteException e) {
            Log.e("DeviceControlerManager", "clearDefaultInputMethod fail!", e);
        }
    }
}
