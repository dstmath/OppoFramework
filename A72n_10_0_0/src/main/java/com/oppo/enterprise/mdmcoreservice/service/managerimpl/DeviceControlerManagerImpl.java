package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IOppoCustomizeService;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype.Desktop;
import java.util.ArrayList;
import java.util.List;

public class DeviceControlerManagerImpl extends IDeviceControlerManager.Stub {
    static final TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
    private Context mContext;
    private DevicePolicyManager mDpm = ((DevicePolicyManager) this.mContext.getSystemService("device_policy"));
    private IOppoCustomizeService mService;

    public DeviceControlerManagerImpl(Context context) {
        this.mContext = context;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void shutdownDevice(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call deviceShutDown!!!");
        long identify = Binder.clearCallingIdentity();
        try {
            ((PowerManager) this.mContext.getSystemService("power")).shutdown(false, null, false);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void rebootDevice(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call deviceReboot!!!");
        long identify = Binder.clearCallingIdentity();
        try {
            ((PowerManager) this.mContext.getSystemService("power")).reboot(null);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean formatSDCard(ComponentName componentName, String diskId) {
        String uuid;
        VolumeInfo volumeInfo;
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call setSDCardFormatted!!!");
        long identify = Binder.clearCallingIdentity();
        try {
            StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
            try {
                if (storageManager.getVolumeState("/storage/sdcard1") == null) {
                    Log.d("DeviceControlerManagerImpl", "no sdcard!!!");
                    return false;
                }
                StorageVolume[] volumes = storageManager.getVolumeList();
                for (int i = 0; i < volumes.length; i++) {
                    if (!(volumes[i] == null || (uuid = volumes[i].getUuid()) == null || (volumeInfo = storageManager.findVolumeByUuid(uuid)) == null || !volumeInfo.disk.isSd())) {
                        String validDiskId = volumeInfo.getDiskId();
                        if (validDiskId.equals(diskId)) {
                            try {
                                storageManager.partitionPublic(validDiskId);
                                Binder.restoreCallingIdentity(identify);
                                return true;
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                                Binder.restoreCallingIdentity(identify);
                                return false;
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(identify);
                return false;
            } catch (IllegalArgumentException e2) {
                Binder.restoreCallingIdentity(identify);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void enableAccessibilityService(ComponentName admin, ComponentName componentName) {
        StringBuilder addBuilder;
        PermissionManager.getInstance().checkPermission();
        if (componentName == null) {
            Log.d("DeviceControlerManagerImpl", "enableAccessibilityService: componentName is null.");
            return;
        }
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "removePersistentApp: customService is null.");
            return;
        }
        try {
            customService.setAccessibilityEnabled(componentName, true);
        } catch (Throwable th) {
            Log.d("DeviceControlerManagerImpl", "enableAccessibilityService Throwable!");
        }
        synchronized (this) {
            String pkg = componentName.getPackageName();
            ContentResolver resolver = this.mContext.getContentResolver();
            String strPath = Settings.Secure.getString(resolver, "custom_settings_enable_access_service_list");
            if (strPath == null) {
                addBuilder = new StringBuilder();
                addBuilder.append(pkg);
            } else if (!strPath.contains(pkg)) {
                addBuilder = new StringBuilder(strPath);
                addBuilder.append(':');
                addBuilder.append(pkg);
            } else {
                return;
            }
            Settings.Secure.putString(resolver, "custom_settings_enable_access_service_list", addBuilder.toString());
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void disableAccessibilityService(ComponentName admin, ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "disableAccessibilityService: customService is null.");
            return;
        }
        try {
            customService.setAccessibilityEnabled(componentName, false);
        } catch (Throwable th) {
            Log.d("DeviceControlerManagerImpl", "disableAccessibilityService Throwable!");
        }
        synchronized (this) {
            String pkg = componentName.getPackageName();
            ContentResolver resolver = this.mContext.getContentResolver();
            String strPath = Settings.Secure.getString(resolver, "custom_settings_enable_access_service_list");
            if (strPath != null) {
                if (strPath.contains(pkg)) {
                    Settings.Secure.putString(resolver, "custom_settings_enable_access_service_list", strPath.replace(pkg, ""));
                }
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean isAccessibilityServiceEnabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "isAccessibilityServiceEnabled: customService is null.");
            return false;
        }
        try {
            if (customService.getAccessibilityStatusFromSettings() == 1) {
                return true;
            }
            return false;
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "isAccessibilityServiceEnabled Throwable!");
            e.printStackTrace();
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public List<ComponentName> getAccessibilityService(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<ComponentName> enabledServicesList = new ArrayList<>();
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "getAccessibilityService: customService is null.");
            return enabledServicesList;
        }
        try {
            return customService.getAccessiblityEnabledListFromSettings();
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "getAccessibilityService Throwable!");
            e.printStackTrace();
            return enabledServicesList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void addAccessibilityServiceToWhiteList(List<String> pkgList) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call addAccessibilityServiceToWhiteList!!!");
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "addAccessibilityServiceToWhiteList: customService is null.");
            return;
        }
        try {
            customService.addAccessibilityServiceToWhiteList(pkgList);
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "addAccessibilityServiceToWhiteList Throwable!");
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void removeAccessibilityServiceFromWhiteList(List<String> pkgList) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call removeAccessibilityServiceFromWhiteList!!!");
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "removeAccessibilityServiceFromWhiteList: customService is null.");
            return;
        }
        try {
            customService.removeAccessibilityServiceFromWhiteList(pkgList);
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "removeAccessibilityServiceFromWhiteList Throwable!");
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public List<String> getAccessibilityServiceWhiteList() {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call getAccessibilityServiceWhiteList!!!");
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "removeAccessibilityServiceFromWhiteList: customService is null.");
            return null;
        }
        try {
            return customService.getAccessibilityServiceWhiteList();
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "getAccessibilityServiceWhiteList Throwable!");
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void deleteAccessibilityServiceWhiteList() {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceControlerManagerImpl", "call deleteAccessibilityServiceWhiteList!!!");
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceControlerManagerImpl", "removeAccessibilityServiceFromWhiteList: customService is null.");
            return;
        }
        try {
            customService.deleteAccessibilityServiceWhiteList();
        } catch (Throwable e) {
            Log.d("DeviceControlerManagerImpl", "deleteAccessibilityServiceWhiteList Throwable!");
            e.printStackTrace();
        }
    }

    private IOppoCustomizeService getCustomizeService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mService;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean setSysTime(ComponentName admin, long mills) {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null || mills / 1000 >= 2147483647L) {
            return false;
        }
        ((AlarmManager) this.mContext.getSystemService("alarm")).setTime(mills);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean setDefaultLauncher(ComponentName admin, ComponentName home) {
        PermissionManager.getInstance().checkPermission();
        boolean retVal = false;
        try {
            retVal = new Desktop(this.mContext).setDefaultApp(home);
            backHome(this.mContext);
            return retVal;
        } catch (Exception e) {
            Log.d("DeviceControlerManagerImpl", "set setDefaultLauncher failed," + e);
            e.printStackTrace();
            return retVal;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public String getDefaultLauncher() {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Desktop(this.mContext).getDefaultPackage(this.mContext.getPackageManager());
        } catch (Exception e) {
            Log.d("DeviceControlerManagerImpl", "get setDefaultLauncher failed," + e);
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void clearDefaultLauncher(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            new Desktop(this.mContext).setDefaultApp("com.oppo.launcher");
            backHome(this.mContext);
        } catch (Exception e) {
            Log.d("DeviceControlerManagerImpl", "set clearDefaultLauncher failed," + e);
        }
    }

    private void backHome(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(268435456);
        context.startActivity(intent);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean wipeDeviceData() {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().resetFactory();
            return true;
        } catch (Exception e) {
            Log.e("DeviceControlerManagerImpl", "wipeDeviceData fail!");
            e.printStackTrace();
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void setCustomSettingsMenu(ComponentName admin, List<String> deleteMenus) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        try {
            setCustomSettingsMenuByUserId(this.mContext, deleteMenus, -2);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setCustomSettingsMenuByUserId(Context context, List<String> deleteMenus, int userId) {
        if (deleteMenus == null || deleteMenus.isEmpty()) {
            deleteMenus = new ArrayList(1);
        }
        StringBuilder deleteBuilder = new StringBuilder();
        for (String delete : deleteMenus) {
            deleteBuilder.append(delete);
            deleteBuilder.append(':');
        }
        int deleteBuilderLength = deleteBuilder.length();
        if (deleteBuilderLength > 0) {
            deleteBuilder.deleteCharAt(deleteBuilderLength - 1);
        }
        Settings.Secure.putStringForUser(context.getContentResolver(), "oppo_settings_main_menu_delete_list", deleteBuilder.toString(), userId);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void setAirplaneMode(ComponentName admin, boolean on) {
        PermissionManager.getInstance().checkPermission();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            Log.d("DeviceControlerManagerImpl", "setAirplaneMode : on = " + on);
            SystemProperties.set("persist.sys.airplane_disable", "0");
            connectivityManager.setAirplaneMode(on);
            SystemProperties.set("persist.sys.airplane_clickable", "1");
            SystemProperties.set("persist.sys.airplane_grey", "0");
            SystemProperties.set("persist.sys.airplane_on", on ? "1" : "0");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean getAirplaneMode(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        boolean isOn = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            isOn = true;
        }
        Log.d("DeviceControlerManagerImpl", "getAirplaneMode : isOn = " + isOn);
        return isOn;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void getCertInstaller() {
        PermissionManager.getInstance().checkPermission();
        Intent mIntent = new Intent("android.credentials.INSTALL");
        if (this.mContext == null) {
            Log.d("DeviceControlerManagerImpl", "getCertInstaller: mContext == null");
            return;
        }
        try {
            if (!(this.mContext instanceof Activity)) {
                mIntent.addFlags(268435456);
            }
            this.mContext.startActivity(mIntent);
        } catch (ActivityNotFoundException e) {
            Log.e("DeviceControlerManagerImpl", e.toString());
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean setDisableKeyguardForgetPassword(ComponentName cn, boolean disable) {
        try {
            PermissionManager.getInstance().checkPermission();
            Log.d("DeviceControlerManagerImpl", "setDisableKeyguardForgetPassword");
            return KeyguardPolicyManager.getInstance().setKeyguardDB(this.mContext, disable, "disable_keyguard_forget_password");
        } catch (Exception e) {
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean isDisableKeyguardForgetPassword(ComponentName cn) {
        try {
            PermissionManager.getInstance().checkPermission();
            Log.d("DeviceControlerManagerImpl", "isDisableKeyguardForgetPassword");
            if (1 == KeyguardPolicyManager.getInstance().getKeyguardDB(this.mContext, "disable_keyguard_forget_password")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void setKeyguardPolicy(ComponentName cn, int[] policies) {
        try {
            PermissionManager.getInstance().checkPermission();
            Log.d("DeviceControlerManagerImpl", "setKeyguardPolicy");
            KeyguardPolicyManager.getInstance().setKeyguardPolicy(cn, this.mContext, policies);
        } catch (Exception e) {
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public int[] getKeyguardPolicy(ComponentName cn) {
        try {
            PermissionManager.getInstance().checkPermission();
            Log.d("DeviceControlerManagerImpl", "getKeyguardPolicy");
            return KeyguardPolicyManager.getInstance().getKeyguardPolicy(cn, this.mContext);
        } catch (Exception e) {
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public boolean setDefaultInputMethod(String methodId) {
        try {
            PermissionManager.getInstance().checkPermission();
            InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService("input_method");
            String availableInputMethod = null;
            if (imm != null) {
                List<InputMethodInfo> imis = imm.getInputMethodList();
                int N = imis == null ? 0 : imis.size();
                int i = 0;
                while (true) {
                    if (i < N) {
                        InputMethodInfo imi = imis.get(i);
                        if (imi != null && imi.getPackageName().equals(methodId)) {
                            availableInputMethod = imi.getId();
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
                if (availableInputMethod != null) {
                    getCustomizeService().setDefaultInputMethod(availableInputMethod);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public String getDefaultInputMethod() {
        try {
            PermissionManager.getInstance().checkPermission();
            return getCustomizeService().getDefaultInputMethod();
        } catch (Exception e) {
            Log.d("DeviceControlerManagerImpl", "getDefaultInputMethod err");
            return "";
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
    public void clearDefaultInputMethod() {
        try {
            PermissionManager.getInstance().checkPermission();
            getCustomizeService().clearDefaultInputMethod();
        } catch (Exception e) {
            Log.d("DeviceControlerManagerImpl", "clearDefaultInputMethod err");
        }
    }
}
