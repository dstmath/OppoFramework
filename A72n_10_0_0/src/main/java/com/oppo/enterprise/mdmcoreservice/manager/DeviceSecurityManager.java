package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager;
import com.oppo.enterprise.mdmcoreservice.aidl.OppoSimContactEntry;
import java.util.ArrayList;
import java.util.List;

public class DeviceSecurityManager extends DeviceBaseManager {
    private static final String TAG = "DeviceSecurityManager";
    private static final Object mLock = new Object();
    private static volatile DeviceSecurityManager sInstance;

    public static final DeviceSecurityManager getInstance(Context context) {
        DeviceSecurityManager deviceSecurityManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceSecurityManager(context);
            }
            deviceSecurityManager = sInstance;
        }
        return deviceSecurityManager;
    }

    private DeviceSecurityManager(Context context) {
        super(context);
    }

    private IDeviceSecurityManager getIDeviceSecurityManager() {
        return IDeviceSecurityManager.Stub.asInterface(getOppoMdmManager("DeviceSecurityManager"));
    }

    public void setEmmAdmin(ComponentName cn, boolean enable) {
        try {
            getIDeviceSecurityManager().setEmmAdmin(cn, enable);
        } catch (RemoteException e) {
            Log.e("DeviceSecurityManager", "setEmmAdmin: fail", e);
        }
    }

    public List<ComponentName> getEmmAdmin(ComponentName admin) {
        List<ComponentName> emmAdminList = new ArrayList<>();
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                return manager.getEmmAdmin(admin);
            }
            Log.e("DeviceSecurityManager", "IDeviceSecurityManager is null");
            return emmAdminList;
        } catch (RemoteException e) {
            Log.e("DeviceSecurityManager", "getEmmAdmin error!");
            return emmAdminList;
        }
    }

    public boolean setDeviceOwner(ComponentName componentName) {
        if (componentName == null) {
            return false;
        }
        try {
            return getIDeviceSecurityManager().setDeviceOwner(componentName);
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "setDeviceOwner fail!", e);
            return false;
        }
    }

    public ComponentName getDeviceOwner() {
        try {
            return getIDeviceSecurityManager().getDeviceOwner();
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "getDeviceOwner fail!", e);
            return null;
        }
    }

    public void clearDeviceOwner(String packageName) {
        try {
            getIDeviceSecurityManager().clearDeviceOwner(packageName);
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "clearDeviceOwner fail!", e);
        }
    }

    public List<OppoSimContactEntry> getSimContacts(ComponentName componentName, int slotId) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                return manager.getSimContacts(componentName, slotId);
            }
            Log.e("DeviceSecurityManager", "IDeviceSecurityManager is null");
            return new ArrayList();
        } catch (RemoteException e) {
            Log.e("DeviceSecurityManager", "getSimContacts error!");
            e.printStackTrace();
            return new ArrayList();
        }
    }

    public void startRecordPolicy(String dirPath) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                manager.startRecordPolicy(dirPath);
                return;
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "startRecordPolicy error!");
            e.printStackTrace();
        }
    }

    public void stopRecordPolicy() {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                manager.stopRecordPolicy();
                return;
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "startRecordPolicy error!");
            e.printStackTrace();
        }
    }

    public void startRecord(String filePath) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                manager.startRecord(filePath);
                return;
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "startRecord error!");
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                manager.stopRecord();
                return;
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "stopRecord error!");
            e.printStackTrace();
        }
    }

    public void grantAllRuntimePermission(ComponentName admin, String packageName) {
        try {
            getIDeviceSecurityManager().grantAllRuntimePermission(admin, packageName);
        } catch (RemoteException e) {
            Log.e("DeviceSecurityManager", "grantAllRuntimePermission: fail", e);
        }
    }

    public boolean setDeviceLocked(ComponentName cn) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.setDeviceLocked(cn);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "stopRecord error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDeviceUnLocked(ComponentName cn) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.setDeviceUnLocked(cn);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "stopRecord error!");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getDeviceInfo(ComponentName admin) {
        List<String> devInfo = new ArrayList<>();
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:getDeviceInfo()");
                return manager.getDeviceInfo(admin);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return devInfo;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "getDeviceInfo error!");
            e.printStackTrace();
            return devInfo;
        }
    }

    public String getPhoneNumber(ComponentName admin, int slot) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.getPhoneNumber(admin, slot);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "getPhoneNumber error!");
            e.printStackTrace();
            return null;
        }
    }

    public String executeShellToSetIptables(ComponentName componentName, String commandline) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.executeShellToSetIptables(componentName, commandline);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "executeShellToSetIptables error!");
            e.printStackTrace();
            return null;
        }
    }

    public String[] listIccid(ComponentName componentName) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.listIccid(componentName);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "listIccid error!");
            e.printStackTrace();
            return null;
        }
    }

    public String[] listImei(ComponentName componentName) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.listImei(componentName);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return null;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "listImei error!");
            e.printStackTrace();
            return null;
        }
    }

    public Bundle getMobilePerpheralSettings(ComponentName admin, String business, String setting) {
        Bundle bundle = new Bundle();
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager getMobilePerpheralSettings admin=" + admin);
                bundle = manager.getMobilePerpheralSettings(admin, business, setting);
            } else {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "getMobilePerpheralSettings error! e=" + e);
        }
        Log.d("DeviceSecurityManager", "getMobilePerpheralSettings " + business + "::" + setting);
        return bundle;
    }

    public void setMobilePerpheralSettings(ComponentName admin, String business, Bundle settingbundle) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager setMobilePerpheralSettings admin=" + admin);
                manager.setMobilePerpheralSettings(admin, business, settingbundle);
            } else {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "setMobilePerpheralSettings error! e=" + e);
        }
        Log.d("DeviceSecurityManager", "setMobilePerpheralSettings business :: " + business);
    }

    public Bundle getMobileCommSettings(ComponentName admin, String business, String setting) {
        Bundle bundle = new Bundle();
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                return manager.getMobileCommSettings(admin, business, setting);
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
            return bundle;
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "getMobileCommSettings error!");
            e.printStackTrace();
            return bundle;
        }
    }

    public void setMobileCommSettings(ComponentName admin, String business, Bundle setting) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager:" + manager);
                manager.setMobileCommSettings(admin, business, setting);
                return;
            }
            Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
        } catch (RemoteException e) {
            Log.i("DeviceSecurityManager", "setMobileCommSettings error!");
            e.printStackTrace();
        }
    }

    public Bitmap captureScreen(ComponentName componentName) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                manager.captureScreen(null);
            } else {
                Log.d("DeviceSecurityManager", "mdm service IOppoMdmDeviceControl manager is null");
            }
            return null;
        } catch (Exception e) {
            Log.i("DeviceSecurityManager", "captureScreen error!");
            e.printStackTrace();
            return null;
        }
    }

    public void setTestServerState(boolean enable) {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                manager.setTestServerState(enable);
            } else {
                Log.d("DeviceSecurityManager", "mdm service IOppoMdmDeviceControl manager is null");
            }
        } catch (Exception e) {
            Log.i("DeviceSecurityManager", "setTestServerState error!");
            e.printStackTrace();
        }
    }

    public boolean getTestServerState() {
        try {
            IDeviceSecurityManager manager = getIDeviceSecurityManager();
            if (manager != null) {
                return manager.getTestServerState();
            }
            Log.d("DeviceSecurityManager", "mdm service IOppoMdmDeviceControl manager is null");
            return false;
        } catch (Exception e) {
            Log.i("DeviceSecurityManager", "getTestServerState error!");
            e.printStackTrace();
        } catch (Throwable th) {
        }
        return false;
    }

    public void backupAppData(String src, String packageName, String dest, int requestId) {
        if (packageName != null && !packageName.isEmpty() && src != null && !src.isEmpty() && dest != null && !dest.isEmpty() && requestId > 0) {
            try {
                IDeviceSecurityManager manager = getIDeviceSecurityManager();
                if (manager != null) {
                    manager.backupAppData(src, packageName, dest, requestId);
                } else {
                    Log.d("DeviceSecurityManager", "mdm service IDeviceSecurityManager manager is null");
                }
            } catch (RemoteException e) {
                Log.d("DeviceSecurityManager", "backupAppData: fail");
            }
        }
    }
}
