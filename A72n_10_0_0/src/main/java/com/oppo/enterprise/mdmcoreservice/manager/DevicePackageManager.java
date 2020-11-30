package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager;
import com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.PackageDeleteObserver;
import com.oppo.enterprise.mdmcoreservice.utils.PackageInstallObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DevicePackageManager extends DeviceBaseManager {
    private static final String ACTION_INSTALL_COMMIT = "com.android.cts.deviceowner.INTENT_PACKAGE_INSTALL_COMMIT";
    private static final String ACTION_UNINSTALL_COMPLETE = "com.android.cts.deviceowner.INTENT_PACKAGE_UNINSTALL_COMPLETE";
    private static final String DIALER_SYSTEM_PKG = "com.android.contacts";
    private static final String MESSAGE_SYSTEM_PKG = "com.android.mms";
    private static final String TAG = "DevicePackageManager";
    private static final Object mLock = new Object();
    private static volatile DevicePackageManager sInstance;
    private final int INSERT_UNINSTALL_LIST_FAIL = -1;
    private final int INSERT_UNINSTALL_LIST_SUCCEEDED = 0;
    private final String JSON_APP_CERTIFY_KEY = "CertificateHash";
    private final String JSON_APP_NAME_KEY = "AppPackageName";
    private final int REMOVE_UNINSTALL_LIST_FAIL = -1;
    private final int REMOVE_UNINSTALL_LIST_SUCCEEDED = 0;

    public static final DevicePackageManager getInstance(Context context) {
        DevicePackageManager devicePackageManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DevicePackageManager(context);
            }
            devicePackageManager = sInstance;
        }
        return devicePackageManager;
    }

    private DevicePackageManager(Context context) {
        super(context);
    }

    private IDevicePackageManager getIDevicePackageManager() {
        return IDevicePackageManager.Stub.asInterface(getOppoMdmManager("DevicePackageManager"));
    }

    public void uninstallPackage(ComponentName admin, String packageName, boolean keepData) {
        if (packageName == null || packageName.isEmpty()) {
            Log.e("DevicePackageManager", "uninstallPackage: invalid parammeter");
            return;
        }
        try {
            getIDevicePackageManager().uninstallPackage(admin, packageName, keepData ? 1 : 0);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "uninstallPackage: fail", e);
        }
    }

    public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        try {
            getIDevicePackageManager().addDisallowedUninstallPackages(admin, packageNames);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "addDisallowedUninstallPackages: fail", e);
        }
    }

    public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        if (packageNames == null || packageNames.size() <= 0) {
            Log.e("DevicePackageManager", "removeDisallowedUninstallPackages: invalid parammeter");
            return;
        }
        try {
            getIDevicePackageManager().removeDisallowedUninstallPackages(admin, packageNames);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "removeDisallowedUninstallPackages: fail", e);
        }
    }

    public void removeAllDisallowedUninstallPackages(ComponentName admin) {
        try {
            getIDevicePackageManager().removeAllDisallowedUninstallPackages(admin);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "removeDisallowedUninstallPackages: fail", e);
        }
    }

    public List<String> getDisallowUninstallPackageList(ComponentName admin) {
        try {
            return getIDevicePackageManager().getDisallowUninstallPackageList(admin);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "getDisallowUninstallPackageList: fail", e);
            return Collections.emptyList();
        }
    }

    public void clearApplicationUserData(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.e("DevicePackageManager", "clearApplicationUserData: invalid packageName");
            return;
        }
        try {
            getIDevicePackageManager().clearApplicationUserData(packageName);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "clearApplicationUserData: ", e);
        }
    }

    public List<String> getClearAppName() {
        try {
            return getIDevicePackageManager().getClearAppName();
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "getClearAppName: fail", e);
            return null;
        }
    }

    public boolean setAdbInstallUninstallPolicies(int mode) {
        if (mode == 0 || 1 == mode) {
            try {
                getIDevicePackageManager().setAdbInstallUninstallDisabled(null, mode == 1);
                return true;
            } catch (RemoteException e) {
                Log.w("DevicePackageManager", "setAdbInstallUninstallPolicies: fail", e);
                return false;
            }
        } else {
            Log.w("DevicePackageManager", "setAdbInstallUninstallPolicies: invalid mode");
            return false;
        }
    }

    public int getAdbInstallUninstallPolicies() {
        try {
            if (getIDevicePackageManager().getAdbInstallUninstallDisabled(null)) {
                return 1;
            }
            return 0;
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getAdbInstallUninstallDisabled: fail", e);
            return 0;
        }
    }

    public void installPackage(ComponentName admin, String packagePath) {
        if (packagePath == null || packagePath.isEmpty()) {
            Log.e("DevicePackageManager", "installPackage: invalid parammeter");
            return;
        }
        try {
            getIDevicePackageManager().installPackage(admin, packagePath, 0);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "installPackage: fail", e);
        }
    }

    public void deleteApplicationCacheFiles(ComponentName admin, String packageName) {
        try {
            getIDevicePackageManager().deleteApplicationCacheFiles(admin, packageName);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "deleteApplicationCacheFiles: fail", e);
        }
    }

    private class InstallBroadcastReceiver extends BroadcastReceiver {
        private Handler mInstallHandler;
        private PackageInstallObserver mPackageInstallObserver;

        public InstallBroadcastReceiver(PackageInstallObserver mPackageInstallObserver2, Handler mInstallHandler2) {
            this.mPackageInstallObserver = mPackageInstallObserver2;
            this.mInstallHandler = mInstallHandler2;
        }

        public void onReceive(Context context, Intent intent) {
            int mResult = intent.getIntExtra("android.content.pm.extra.STATUS", 1);
            String mPackageName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
            Log.d("DevicePackageManager", "DevicePackageManager receive install broadcast");
            if (this.mPackageInstallObserver != null) {
                this.mPackageInstallObserver.packageInstalled(mPackageName, DevicePackageManager.this.installStatusToPMStatus(mResult));
                this.mPackageInstallObserver = null;
            }
            if (this.mInstallHandler != null) {
                Message message = this.mInstallHandler.obtainMessage();
                message.arg1 = mResult == 0 ? 0 : -1;
                this.mInstallHandler.sendMessage(message);
                this.mInstallHandler = null;
            }
            DevicePackageManager.this.mContext.unregisterReceiver(this);
        }
    }

    private class DeleteBroadcastReceiver extends BroadcastReceiver {
        private PackageDeleteObserver mPackageDeleteObserver;
        private Handler mUninstallHandler;

        public DeleteBroadcastReceiver(PackageDeleteObserver mPackageDeleteObserver2, Handler mUninstallHandler2) {
            this.mPackageDeleteObserver = mPackageDeleteObserver2;
            this.mUninstallHandler = mUninstallHandler2;
        }

        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getStringExtra("name");
            int i = -1;
            int status = intent.getIntExtra("status", -1);
            if (this.mPackageDeleteObserver != null) {
                this.mPackageDeleteObserver.packageDeleted(packageName, status);
                this.mPackageDeleteObserver = null;
            }
            if (this.mUninstallHandler != null) {
                Message message = this.mUninstallHandler.obtainMessage();
                if (status == 1) {
                    i = 0;
                }
                message.arg1 = i;
                this.mUninstallHandler.sendMessage(message);
                this.mUninstallHandler = null;
            }
        }
    }

    private void registerInstallBroadcast(String apkPath, PackageInstallObserver observer, Handler handler) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.cts.deviceowner.INTENT_PACKAGE_INSTALL_COMMIT." + apkPath.hashCode());
        this.mContext.registerReceiver(new InstallBroadcastReceiver(observer, handler), intentFilter);
    }

    private void registerUninstallBroadcast(String packageName, PackageDeleteObserver observer, Handler handler) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.cts.deviceowner.INTENT_PACKAGE_UNINSTALL_COMPLETE." + packageName);
        this.mContext.registerReceiver(new DeleteBroadcastReceiver(observer, handler), intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int installStatusToPMStatus(int status) {
        switch (status) {
            case 0:
                return 1;
            case 1:
                return -110;
            case 2:
            default:
                return -115;
            case 3:
                return -115;
            case 4:
                return -3;
            case 5:
                return -7;
            case 6:
                return -4;
            case 7:
                return -111;
        }
    }

    public boolean setAppPermission(String appPackageName, String permissions, boolean fixed) {
        if (appPackageName == null || appPackageName.isEmpty()) {
            Log.e("DevicePackageManager", "setAppPermission: invalid packageName.");
            return false;
        } else if (permissions == null || permissions.isEmpty()) {
            Log.e("DevicePackageManager", "setAppPermission: invalid permissions.");
            return false;
        } else {
            try {
                return getIDevicePackageManager().setAppPermission(appPackageName, permissions, fixed);
            } catch (RemoteException e) {
                Log.e("DevicePackageManager", "setAppPermission: " + e);
                return false;
            }
        }
    }

    public String getAppPermission(String appPackageName) {
        try {
            return getIDevicePackageManager().getAppPermission(appPackageName);
        } catch (RemoteException e) {
            Log.e("DevicePackageManager", "setAppPermission: " + e);
            return "";
        }
    }

    public void setDefaultDialer(String packageName) {
        if (packageName != null) {
            try {
                getIDevicePackageManager().setDefaultDialer(packageName);
            } catch (RemoteException e) {
                Log.d("DevicePackageManager", "setDefaultDialer: failed");
            }
        } else {
            Log.d("DevicePackageManager", "setDefaultDialer: invalid packageName");
        }
    }

    public String getDefaultDialerPackage(ComponentName admin) {
        try {
            return getIDevicePackageManager().getDefaultDialerPackage(admin);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getDefaultDialerPackage: failed");
            return null;
        }
    }

    public String getSystemDialerPackage(ComponentName admin) {
        try {
            return getIDevicePackageManager().getSystemDialerPackage(admin);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getSystemDialerPackage: failed");
            return null;
        }
    }

    public void clearDefaultDialerPackage() {
        try {
            Log.d("DevicePackageManager", "clearDefaultDialerPackage");
            getIDevicePackageManager().setDefaultDialer(DIALER_SYSTEM_PKG);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "clearDefaultDialerPackage: failed");
        }
    }

    public boolean setDefaultMessage(String packageName) {
        try {
            Log.d("DevicePackageManager", "setDefaultMessage");
            return getIDevicePackageManager().setDefaultMessage(packageName);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "setDefaultMessage: failed");
            return false;
        }
    }

    public boolean clearDefaultMessage() {
        try {
            Log.d("DevicePackageManager", "clearDefaultMessage");
            return getIDevicePackageManager().setDefaultMessage(MESSAGE_SYSTEM_PKG);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "clearDefaultMessage: failed");
            return false;
        }
    }

    public String getDefaultMessage() {
        try {
            Log.d("DevicePackageManager", "getDefaultMessage");
            return getIDevicePackageManager().getDefaultMessage();
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getDefaultMessage: failed");
            return null;
        }
    }

    public boolean setDefaultBrowser(String packageName) {
        try {
            Log.d("DevicePackageManager", "setDefaultBrowser");
            return getIDevicePackageManager().setDefaultBrowser(packageName);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "setDefaultBrowser: failed");
            return false;
        }
    }

    public boolean clearDefaultBrowser() {
        try {
            Log.d("DevicePackageManager", "clearDefaultBrowser");
            return getIDevicePackageManager().clearDefaultBrowser();
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "clearDefaultBrowser: failed");
            return false;
        }
    }

    public String getDefaultBrowser() {
        try {
            Log.d("DevicePackageManager", "getDefaultBrowser");
            return getIDevicePackageManager().getDefaultBrowser();
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getDefaultBrowser: failed");
            return null;
        }
    }

    public void addDisabledDeactivateMdmPackages(List<String> packageNames) {
        try {
            getIDevicePackageManager().addDisabledDeactivateMdmPackages(packageNames);
            Log.d("DevicePackageManager", "addDisabledDeactivateMdmPackages manager: succeed");
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "addDisabledDeactivateMdmPackages: fail");
        }
    }

    public void removeDisabledDeactivateMdmPackages(List<String> packageNames) {
        try {
            getIDevicePackageManager().removeDisabledDeactivateMdmPackages(packageNames);
            Log.d("DevicePackageManager", "removeDisabledDeactivateMdmPackages manager: succeed");
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "removeDisabledDeactivateMdmPackages: fail");
        }
    }

    public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) {
        List<String> disabledDeactivateMdmPackagesList = new ArrayList<>();
        try {
            disabledDeactivateMdmPackagesList = getIDevicePackageManager().getDisabledDeactivateMdmPackages(admin);
            Log.d("DevicePackageManager", "getDisabledDeactivateMdmPackages manager: succeed");
            return disabledDeactivateMdmPackagesList;
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getDisabledDeactivateMdmPackages: fail");
            return disabledDeactivateMdmPackagesList;
        }
    }

    public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) {
        try {
            getIDevicePackageManager().removeAllDisabledDeactivateMdmPackages(admin);
            Log.d("DevicePackageManager", "removeAllDisabledDeactivateMdmPackages manager: succeed");
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "removeAllDisabledDeactivateMdmPackages: fail");
        }
    }

    public void setDefaultApplication(ComponentName admin, ComponentName componentName, String type) {
        char c = 65535;
        try {
            int hashCode = type.hashCode();
            if (hashCode != -1407250528) {
                if (hashCode != -1332085731) {
                    if (hashCode == 150940456) {
                        if (type.equals(AppTypeUtil.DEFAULT_BROWSER)) {
                            c = 2;
                        }
                    }
                } else if (type.equals(AppTypeUtil.DEFAULT_DIALER)) {
                    c = 0;
                }
            } else if (type.equals(AppTypeUtil.DEFAULT_LAUNCHER)) {
                c = 1;
            }
            switch (c) {
                case 0:
                    getIDevicePackageManager().setDefaultDialer(componentName.getPackageName());
                    return;
                case 1:
                    IDeviceControlerManager.Stub.asInterface(getOppoMdmManager(DeviceBaseManager.MDM_DEVICE_CONTROLER_MANAGER_NAME)).setDefaultLauncher(admin, componentName);
                    return;
                case 2:
                    getIDevicePackageManager().setDefaultApplication(admin, componentName, type);
                    return;
                default:
                    return;
            }
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "setDefaultApplication: fail");
        }
    }

    public void setSysAppList(ComponentName admin, Map<String, String> maps, Bundle bundle) {
        try {
            getIDevicePackageManager().setSysAppList(admin, maps, bundle);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "setDefaultApplication: fail");
        }
    }

    public List<String> getSysAppList(ComponentName admin, List<String> pkgNames) {
        if (pkgNames == null || pkgNames.isEmpty()) {
            return null;
        }
        try {
            return getIDevicePackageManager().getSysAppList(admin, pkgNames);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "setDefaultApplication: fail");
            return null;
        }
    }

    public boolean setSuperWhiteList(ComponentName componentName, List<String> list) {
        if (componentName == null) {
            Log.d("DevicePackageManager", "setSuperWhiteList componentName is null");
            return false;
        } else if (list == null || list.size() == 0) {
            Log.d("DevicePackageManager", "setSuperWhiteList list is null");
            return false;
        } else {
            try {
                Log.d("DevicePackageManager", "setSuperWhiteList try");
                return getIDevicePackageManager().setSuperWhiteList(componentName, list);
            } catch (RemoteException e) {
                Log.d("DevicePackageManager", "setSuperWhiteList: fail");
                return false;
            }
        }
    }

    public List<String> getSuperWhiteList() {
        try {
            Log.d("DevicePackageManager", "getSuperWhiteList try");
            return getIDevicePackageManager().getSuperWhiteList();
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "getSuperWhiteList: fail");
            return null;
        }
    }

    public boolean clearSuperWhiteList(ComponentName componentName, List<String> clearList) {
        if (componentName == null) {
            Log.d("DevicePackageManager", "clearSuperWhiteList componentName is null");
            return false;
        } else if (clearList == null || clearList.size() == 0) {
            Log.d("DevicePackageManager", "clearSuperWhiteList is null");
            return false;
        } else {
            try {
                Log.d("DevicePackageManager", "clearSuperWhiteList try");
                return getIDevicePackageManager().clearSuperWhiteList(componentName, clearList);
            } catch (RemoteException e) {
                Log.d("DevicePackageManager", "clearSuperWhiteList: fail");
                return false;
            }
        }
    }

    public boolean clearAllSuperWhiteList(ComponentName componentName) {
        if (componentName == null) {
            Log.d("DevicePackageManager", "clearAllSuperWhiteList componentName is null");
            return false;
        }
        try {
            Log.d("DevicePackageManager", "clearAllSuperWhiteList try");
            return getIDevicePackageManager().clearAllSuperWhiteList(componentName);
        } catch (RemoteException e) {
            Log.d("DevicePackageManager", "clearAllSuperWhiteList: fail");
            return false;
        }
    }
}
