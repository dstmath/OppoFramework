package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager;
import com.oppo.enterprise.mdmcoreservice.help.LogHelper;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceStateManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceApplicationManagerImpl extends IDeviceApplicationManager.Stub {
    private ActivityManager mAm = ((ActivityManager) this.mContext.getSystemService("activity"));
    private Context mContext;
    private String mCurrentScreenPinedApp;
    private HashMap<String, Integer> mNavBarTypeState = new HashMap<>();
    private PackageManager mPm = this.mContext.getPackageManager();
    private IOppoCustomizeService mService;

    public DeviceApplicationManagerImpl(Context context) {
        this.mContext = context;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void addPersistentApp(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        if (packageNames == null || packageNames.isEmpty()) {
            Log.d("DeviceApplicationManagerImpl", "addPersistentApp: packageNames is null or empty.");
            return;
        }
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceApplicationManagerImpl", "addPersistentApp: customService is null.");
            return;
        }
        try {
            for (String packageName : packageNames) {
                Log.d("DeviceApplicationManagerImpl", "addPersistentApp: " + packageName);
                customService.addProtectApplication(packageName);
            }
        } catch (Throwable th) {
            Log.d("DeviceApplicationManagerImpl", "addPersistentApp Throwable!");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void removePersistentApp(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        if (packageNames == null || packageNames.isEmpty()) {
            Log.d("DeviceApplicationManagerImpl", "removePersistentApp: packageNames is null or empty.");
            return;
        }
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceApplicationManagerImpl", "removePersistentApp: customService is null.");
            return;
        }
        try {
            for (String packageName : packageNames) {
                Log.d("DeviceApplicationManagerImpl", "removePersistentApp: " + packageName);
                customService.removeProtectApplication(packageName);
            }
        } catch (Throwable th) {
            Log.d("DeviceApplicationManagerImpl", "removePersistentApp Throwable!");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public List<String> getPersistentApp(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceApplicationManagerImpl", "getPersistentApp: customService is null.");
            return new ArrayList();
        }
        List<String> listApps = new ArrayList<>();
        try {
            listApps = customService.getProtectApplicationList();
        } catch (Throwable th) {
            Log.d("DeviceApplicationManagerImpl", "get Protect ApplicationList failed!");
        }
        Log.d("DeviceApplicationManagerImpl", "getPersistentApp listApps=" + listApps);
        return listApps;
    }

    private IOppoCustomizeService getCustomizeService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mService;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().addDisallowedRunningApp(packageNames);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().removeDisallowedRunningApp(packageNames);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void removeAllDisallowedRunningApp(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<String> currentlist = getCustomizeService().getDisallowedRunningApp();
            if (currentlist != null) {
                getCustomizeService().removeDisallowedRunningApp(currentlist);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public List<String> getDisallowedRunningApp(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getDisallowedRunningApp();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void addTrustedAppStore(ComponentName compName, String appStorePkgName) throws SecurityException {
        PermissionManager.getInstance().checkPermission();
        if (compName == null || appStorePkgName == null) {
            Log.w("DeviceApplicationManagerImpl", "addTrustedAppStore param is null.");
            return;
        }
        Log.d("DeviceApplicationManagerImpl", "addTrustedAppStore: " + appStorePkgName);
        try {
            getCustomizeService().addInstallSource(appStorePkgName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void deleteTrustedAppStore(ComponentName compName, String appStorePkgName) throws SecurityException {
        PermissionManager.getInstance().checkPermission();
        if (compName == null || appStorePkgName == null) {
            Log.w("DeviceApplicationManagerImpl", "deleteTrustedAppStore param is null.");
            return;
        }
        Log.d("DeviceApplicationManagerImpl", "deleteTrustedAppStore: " + appStorePkgName);
        try {
            getCustomizeService().deleteInstallSource(appStorePkgName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void enableTrustedAppStore(ComponentName compName, boolean enable) throws SecurityException {
        PermissionManager.getInstance().checkPermission();
        if (compName == null) {
            Log.w("DeviceApplicationManagerImpl", "enableTrustedAppStore param is null.");
            return;
        }
        Log.d("DeviceApplicationManagerImpl", "enableTrustedAppStore: " + enable);
        try {
            getCustomizeService().enableInstallSource(enable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean isTrustedAppStoreEnabled(ComponentName compName) throws SecurityException {
        PermissionManager.getInstance().checkPermission();
        boolean ret = false;
        if (compName == null) {
            Log.w("DeviceApplicationManagerImpl", "isTrustedAppStoreEnabled param is null.");
            return false;
        }
        try {
            ret = getCustomizeService().isInstallSourceEnable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d("DeviceApplicationManagerImpl", "isTrustedAppStoreEnabled: " + ret);
        return ret;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public List<String> getTrustedAppStore(ComponentName compName) throws SecurityException {
        PermissionManager.getInstance().checkPermission();
        if (compName == null) {
            Log.w("DeviceApplicationManagerImpl", "getTrustedAppStore param is null.");
            return null;
        }
        Log.d("DeviceApplicationManagerImpl", "getTrustedAppStore.");
        try {
            return getCustomizeService().getInstallSourceList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public Bundle getApplicationSettings(ComponentName compName, String tag, String cmdName) throws RuntimeException, RemoteException {
        List<String> list3;
        PermissionManager.getInstance().checkPermission();
        if (compName == null || tag == null || cmdName == null) {
            Log.w("DeviceApplicationManagerImpl", "getApplicationSettings param is null.");
            return null;
        } else if (tag.equalsIgnoreCase("APPLICATION")) {
            Bundle bundle = new Bundle();
            if (cmdName.equalsIgnoreCase("CLEARCACHE")) {
                List<String> list1 = getCustomizeService().getClearAppName();
                if (list1 != null) {
                    bundle.putStringArrayList("CLEARCACHE", listToArrayList(list1));
                }
            } else if (cmdName.equalsIgnoreCase("RUNNING")) {
                throw new RuntimeException("get app running is not available!");
            } else if (cmdName.equalsIgnoreCase("STOP")) {
                List<String> list2 = getCustomizeService().getDisallowedRunningApp();
                if (list2 != null) {
                    bundle.putStringArrayList("STOP", listToArrayList(list2));
                }
            } else if (cmdName.equalsIgnoreCase("UNINSTALL") && (list3 = getCustomizeService().getDisallowUninstallPackageList()) != null) {
                bundle.putStringArrayList("UNINSTALL", listToArrayList(list3));
            }
            return bundle;
        } else {
            throw new RuntimeException("business is not available!");
        }
    }

    private ArrayList<String> listToArrayList(List<String> list) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String item : list) {
            arrayList.add(item);
        }
        return arrayList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void setApplicationSettings(ComponentName compName, String tag, Bundle cmdBundle) throws RuntimeException, RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (compName == null || tag == null || cmdBundle == null) {
            Log.w("DeviceApplicationManagerImpl", "setApplicationSettings param is null.");
        } else if (tag.equalsIgnoreCase("APPLICATION")) {
            String clearcache_value = cmdBundle.getString("CLEARCACHE", null);
            String running_value = cmdBundle.getString("RUNNING", null);
            String stop_value = cmdBundle.getString("STOP", null);
            String uninstall_value = cmdBundle.getString("UNINSTALL", null);
            if (clearcache_value != null) {
                getCustomizeService().clearAppData(clearcache_value);
            } else if (running_value != null) {
                throw new RuntimeException("set app running is not available!");
            } else if (stop_value != null) {
                List<String> list = new ArrayList<>();
                list.add(stop_value);
                getCustomizeService().addDisallowedRunningApp(list);
            } else if (uninstall_value != null) {
                Log.d("DeviceApplicationManagerImpl", "addDisallowUninstallApp successed!");
                List<String> list2 = new ArrayList<>();
                list2.add(uninstall_value);
                getCustomizeService().addDisallowedUninstallPackages(list2);
            }
        } else {
            throw new RuntimeException("business is not available!");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void setComponentSettings(ComponentName componentName, int newState) throws RuntimeException, RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (componentName == null || newState < 0 || newState > 4) {
            Log.w("DeviceApplicationManagerImpl", "setComponentSettings param is invaild.");
            return;
        }
        try {
            this.mPm.setComponentEnabledSetting(componentName, newState, 1);
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "setComponentSettings error", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public int getComponentSettings(ComponentName componentName) throws RuntimeException, RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (componentName == null) {
            Log.w("DeviceApplicationManagerImpl", "getComponentSettings param is invaild.");
        }
        try {
            return this.mPm.getComponentEnabledSetting(componentName);
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "setComponentSettings error", e);
            return -1;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean setDisabledAppList(ComponentName componentName, List<String> pkgs, int mode) {
        PermissionManager.getInstance().checkPermission();
        try {
            for (String pkg : pkgs) {
                this.mPm.setApplicationEnabledSetting(pkg, mode, 0);
            }
            return true;
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "setDisabledAppList error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public List<String> getDisabledAppList(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        List<String> listApps = new ArrayList<>();
        try {
            List<PackageInfo> packageInfos = this.mPm.getInstalledPackages(0);
            if (packageInfos != null) {
                if (packageInfos.size() > 0) {
                    for (PackageInfo pkg : packageInfos) {
                        int mode = this.mPm.getApplicationEnabledSetting(pkg.packageName);
                        if (mode == 2 || mode == 4) {
                            listApps.add(pkg.packageName);
                        }
                    }
                    return listApps;
                }
            }
            return listApps;
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "getDisabledAppList error", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean forceStopPackage(ComponentName admin, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceApplicationManagerImpl", "getDisabledAppList: customService is null.");
            return false;
        }
        try {
            customService.forceStopPackage(pkgs, -2);
            return true;
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "getDisabledAppList error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void killApplicationProcess(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().killAppProcess(packageName);
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "killApplicationProcess fail", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public String getTopAppPackageName(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = this.mAm.getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.isEmpty()) {
            return "";
        }
        return runningTaskInfos.get(0).baseActivity.getPackageName();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void cleanBackgroundProcess(ComponentName compName) {
        PermissionManager.getInstance().checkPermission();
        Intent explicitIntent = getExplicitIntent(this.mContext, new Intent("oppo.intent.action.REQUEST_APP_CLEAN_RUNNING"));
        if (explicitIntent != null) {
            explicitIntent.putExtra("caller_package", "com.coloros.recents");
            explicitIntent.putExtra("IsShowCleanFinishToast", true);
            explicitIntent.putExtra("clear_task", true);
            explicitIntent.putExtra("clear_system", true);
            explicitIntent.putExtra("clean_trash", true);
            Log.i("DeviceApplicationManagerImpl", "start to call app clean service");
            try {
                this.mContext.startService(explicitIntent);
            } catch (Exception e) {
                Log.e("DeviceApplicationManagerImpl", "cleanBackgroundProcess Exception:" + e);
            }
        }
    }

    private Intent getExplicitIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = this.mPm.queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            Log.i("DeviceApplicationManagerImpl", "getExplicitIntent, resolveInfo == null or more than one service have same intent action, return null");
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        explicitIntent.setPackage(context.getPackageName());
        return explicitIntent;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean setDrawOverlays(ComponentName admin, String packageName, int mode) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().setDrawOverlays(packageName, mode);
        } catch (Exception e) {
            Log.d("DeviceApplicationManagerImpl", "setDrawOverlays fail!");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public int getDrawOverlays(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getDrawOverlays(packageName);
        } catch (Exception e) {
            Log.d("DeviceApplicationManagerImpl", "getDrawOverlays fail!");
            return -1;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void addAppAlarmWhiteList(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().addAppAlarmWhiteList(packageNames);
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "addAppAlarmWhiteList error", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public List<String> getAppAlarmWhiteList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getAppAlarmWhiteList();
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "getAppAlarmWhiteList error", e);
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean removeAppAlarmWhiteList(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().removeAppAlarmWhiteList(packageNames);
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "removeAppAlarmWhiteList error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public boolean removeAllAppAlarmWhiteList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().removeAllAppAlarmWhiteList();
        } catch (Exception e) {
            Log.e("DeviceApplicationManagerImpl", "removeAllAppAlarmWhiteList error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void addScreenPinningApp(ComponentName compName, String packageName) {
        PermissionManager.getInstance().checkPermission();
        Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            this.mContext.startActivity(launchIntent);
            try {
                Thread.sleep(150);
                List<ActivityManager.RunningTaskInfo> taskInfo = this.mAm.getRunningTasks(1);
                if (taskInfo != null && taskInfo.size() > 0) {
                    LogHelper.d("DeviceApplicationManagerImpl", "topActivity::" + taskInfo.get(0).topActivity.getClassName());
                    if (taskInfo.get(0).topActivity.getPackageName().equals(packageName)) {
                        setNavBackOrPowerStatus(true, packageName);
                        ActivityManager.getService().startSystemLockTaskMode(taskInfo.get(0).id);
                    }
                }
            } catch (RemoteException | InterruptedException e) {
                Log.e("DeviceApplicationManagerImpl", "addScreenPinningApp error", e);
                if (0 != 0) {
                    setNavBackOrPowerStatus(false, packageName);
                }
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public void clearScreenPinningApp(ComponentName compName, String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (!this.mNavBarTypeState.containsKey(packageName)) {
            LogHelper.d("DeviceApplicationManagerImpl", "No app ScreenPinning");
            return;
        }
        setNavBackOrPowerStatus(false, packageName);
        if (Build.VERSION.SDK_INT > 28) {
            try {
                Object iActivityTaskManager = Class.forName("android.app.ActivityTaskManager").getMethod("getService", new Class[0]).invoke(null, new Object[0]);
                iActivityTaskManager.getClass().getMethod("stopSystemLockTaskMode", new Class[0]).invoke(iActivityTaskManager, new Object[0]);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Log.e("DeviceApplicationManagerImpl", "clearScreenPinningApp error::", e);
            }
        } else {
            try {
                ActivityManager.getService().stopSystemLockTaskMode();
            } catch (RemoteException e2) {
                Log.e("DeviceApplicationManagerImpl", "clearScreenPinningApp error::", e2);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
    public String getScreenPinningApp(ComponentName compName) {
        PermissionManager.getInstance().checkPermission();
        try {
            if (ActivityManager.getService().isInLockTaskMode()) {
                return this.mCurrentScreenPinedApp;
            }
            return "";
        } catch (RemoteException e) {
            Log.e("DeviceApplicationManagerImpl", "getScreenPinningApp error::", e);
            return "";
        }
    }

    private void setNavBackOrPowerStatus(boolean disable, String packageName) {
        LogHelper.d("DeviceApplicationManagerImpl", "disable=" + disable + " packageName=" + packageName);
        if (disable) {
            this.mCurrentScreenPinedApp = packageName;
            this.mNavBarTypeState.put(packageName, Integer.valueOf(Settings.Secure.getInt(this.mContext.getContentResolver(), "hide_navigationbar_enable", -1)));
            DeviceRestrictionManager.getInstance(this.mContext).setNavigationBarDisabled(null, true);
            DeviceRestrictionManager.getInstance(this.mContext).setBackButtonDisabled(null, true);
            DeviceRestrictionManager.getInstance(this.mContext).setPowerDisable(null, true);
            DeviceStateManager.getInstance(this.mContext).keepSrceenOn();
            return;
        }
        this.mCurrentScreenPinedApp = "";
        DeviceRestrictionManager.getInstance(this.mContext).setNavigationBarDisabled(null, false);
        Integer navState = this.mNavBarTypeState.get(packageName);
        if (navState != null) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "hide_navigationbar_enable", navState.intValue());
        }
        DeviceRestrictionManager.getInstance(this.mContext).setBackButtonDisabled(null, false);
        DeviceRestrictionManager.getInstance(this.mContext).setPowerDisable(null, false);
        DeviceStateManager.getInstance(this.mContext).cancelSrceenOn();
    }
}
