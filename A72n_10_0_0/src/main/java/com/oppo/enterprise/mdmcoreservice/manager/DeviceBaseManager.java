package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService;
import com.oppo.enterprise.mdmcoreservice.utils.ConstantUtil;

public class DeviceBaseManager {
    public static final String MDM_DEVICEC_NETWORK_MANAGER_NAME = "DeviceNetworkManager";
    public static final String MDM_DEVICE_APPLICATION_MANAGER_NAME = "DeviceApplicationManager";
    public static final String MDM_DEVICE_CONNECTIVITY_MANAGER_NAME = "DeviceConnectivityManager";
    public static final String MDM_DEVICE_CONTACT_MANAGER_NAME = "DeviceContactManager";
    public static final String MDM_DEVICE_CONTROLER_MANAGER_NAME = "DeviceControlerManager";
    public static final String MDM_DEVICE_PACKAGE_MANAGER_NAME = "DevicePackageManager";
    public static final String MDM_DEVICE_PHONE_MANAGER_NAME = "DevicePhoneManager";
    public static final String MDM_DEVICE_RESTRICTION_MANAGER_NAME = "DeviceRestrictionManager";
    public static final String MDM_DEVICE_SECURITY_MANAGER_NAME = "DeviceSecurityManager";
    public static final String MDM_DEVICE_SETTINGS_MANAGER_NAME = "DeviceSettingsManager";
    public static final String MDM_DEVICE_STATE_MANAGER_NAME = "DeviceStateManager";
    public static final String MDM_DEVICE_VPN_MANAGER_NAME = "DeviceVpnManager";
    private static final String TAG = "DeviceControlerManager";
    private static final int TIMES = 3;
    private static final Object mLock = new Object();
    private static final ArrayMap<String, IBinder> mManagerCache = new ArrayMap<>();
    private static IOppoMdmService mOppoMdmService;
    private static DeviceControlerManager sInstance;
    protected Context mContext;

    public DeviceBaseManager(Context context) {
        this.mContext = context;
        getOppoMdmService();
    }

    private final IOppoMdmService getOppoMdmService() {
        synchronized (mManagerCache) {
            if (mOppoMdmService != null) {
                return mOppoMdmService;
            }
            int times = 3;
            while (true) {
                int times2 = times - 1;
                if (times <= 0) {
                    return null;
                }
                Log.d("DeviceControlerManager", "try to get oppomdmservice the " + times2 + " times");
                mOppoMdmService = IOppoMdmService.Stub.asInterface(ServiceManager.getService(ConstantUtil.SERVICE_NAME));
                if (mOppoMdmService != null) {
                    try {
                        mOppoMdmService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                            /* class com.oppo.enterprise.mdmcoreservice.manager.DeviceBaseManager.AnonymousClass1 */

                            public void binderDied() {
                                synchronized (DeviceBaseManager.mManagerCache) {
                                    Log.i("DeviceControlerManager", "getOppoMdmManager IOppoMdmService binderDied, clear cache");
                                    IOppoMdmService unused = DeviceBaseManager.mOppoMdmService = null;
                                    DeviceBaseManager.mManagerCache.clear();
                                }
                            }
                        }, 0);
                        return mOppoMdmService;
                    } catch (RemoteException e) {
                        Log.i("DeviceControlerManager", "getOppoMdmService linkToDeath error!");
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    times = times2;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public final IBinder getOppoMdmManager(String strManagerName) {
        synchronized (mManagerCache) {
            IOppoMdmService mdmService = getOppoMdmService();
            if (mdmService != null) {
                try {
                    if (mManagerCache.containsKey(strManagerName)) {
                        return mManagerCache.get(strManagerName);
                    }
                    IBinder manager = mdmService.getManager(strManagerName);
                    if (manager != null) {
                        mManagerCache.put(strManagerName, manager);
                    }
                    return manager;
                } catch (RemoteException e) {
                    Log.i("DeviceControlerManager", "getOppoMdmManager error!");
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
