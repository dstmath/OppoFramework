package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceContactManager;
import java.util.List;

public class DeviceContactManager extends DeviceBaseManager {
    private static final String TAG = "DeviceContactManager";
    private static final Object mLock = new Object();
    private static volatile DeviceContactManager sInstance;

    public static final DeviceContactManager getInstance(Context context) {
        DeviceContactManager deviceContactManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DeviceContactManager(context);
            }
            deviceContactManager = sInstance;
        }
        return deviceContactManager;
    }

    private DeviceContactManager(Context context) {
        super(context);
    }

    public boolean setContactBlockPattern(ComponentName componentName, int blockPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactBlockPattern(componentName, blockPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactBlockPattern error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getContactBlockPattern(ComponentName componentName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactBlockPattern(componentName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactBlockPattern error!");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean setContactMatchPattern(ComponentName componentName, int matchPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactMatchPattern(componentName, matchPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactMatchPattern error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getContactMatchPattern(ComponentName componentName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactMatchPattern(componentName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactMatchPattern error!");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean setContactOutgoOrIncomePattern(ComponentName componentName, int outgoOrIncomePattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactOutgoOrIncomePattern(componentName, outgoOrIncomePattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactOutgoOrIncomePattern error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getContactOutgoOrIncomePattern(ComponentName componentName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactOutgoOrIncomePattern(componentName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactOutgoOrIncomePattern error!");
            e.printStackTrace();
            return -1;
        }
    }

    public int addContactBlockNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.addContactBlockNumberList(componentName, numbers, blockPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "addContactBlockNumberList error!");
            e.printStackTrace();
            return -1;
        }
    }

    public int removeContactBlockNumberList(ComponentName componentName, List<String> numbers, int blockPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.removeContactBlockNumberList(componentName, numbers, blockPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return -1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "removeContactBlockNumberList error!");
            e.printStackTrace();
            return -1;
        }
    }

    public List<String> getContactBlockNumberList(ComponentName componentName, int blockPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactBlockNumberList(componentName, blockPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return null;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactBlockNumberList error!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeContactBlockAllNumber(ComponentName componentName, int blockPattern) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.removeContactBlockAllNumber(componentName, blockPattern);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "removeContactBlockAllNumber error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setContactsProviderWhiteList(ComponentName componentName, String packageName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactsProviderWhiteList(componentName, packageName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactsProviderWhiteList error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setContactNumberHideMode(ComponentName componentName, int mode) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactNumberHideMode(componentName, mode);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactNumberHideMode error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getContactNumberHideMode(ComponentName componentName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactNumberHideMode(componentName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return 1;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactNumberHideMode error!");
            e.printStackTrace();
            return 1;
        }
    }

    public boolean setContactNumberMaskEnable(ComponentName componentName, int switcher) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.setContactNumberMaskEnable(componentName, switcher);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "setContactNumberMaskEnable error!");
            e.printStackTrace();
            return false;
        }
    }

    public int getContactNumberMaskEnable(ComponentName componentName) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.getContactNumberMaskEnable(componentName);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return 0;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "getContactNumberMaskEnable error!");
            e.printStackTrace();
            return 0;
        }
    }

    public boolean forbidCallLog(ComponentName componentName, int forbid) {
        try {
            IDeviceContactManager manager = IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager"));
            if (manager != null) {
                return manager.forbidCallLog(componentName, forbid);
            }
            Log.e("DeviceContactManager", "IDeviceContactManager is null");
            return false;
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "forbidCallLog error!");
            e.printStackTrace();
            return false;
        }
    }

    public void forbidGetContactsPermission(ComponentName admin, String packageName) {
        try {
            IDeviceContactManager.Stub.asInterface(getOppoMdmManager("DeviceContactManager")).forbidGetContactsPermission(admin, packageName);
        } catch (RemoteException e) {
            Log.e("DeviceContactManager", "forbidGetContactsPermission: fail", e);
        }
    }
}
