package android.os;

import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import java.util.List;

public interface IOppoCustomizeService extends IInterface {
    void activateSubId(int i) throws RemoteException;

    void addAccessibilityServiceToWhiteList(List<String> list) throws RemoteException;

    void addAppAlarmWhiteList(List<String> list) throws RemoteException;

    void addAppMeteredDataBlackList(List<String> list) throws RemoteException;

    void addAppWlanDataBlackList(List<String> list) throws RemoteException;

    void addBluetoothDevicesToBlackList(List<String> list) throws RemoteException;

    void addBluetoothDevicesToWhiteList(List<String> list) throws RemoteException;

    void addDisabledDeactivateMdmPackages(List<String> list) throws RemoteException;

    void addDisallowedRunningApp(List<String> list) throws RemoteException;

    void addDisallowedUninstallPackages(List<String> list) throws RemoteException;

    void addInstallPackageBlacklist(int i, List<String> list) throws RemoteException;

    void addInstallPackageWhitelist(int i, List<String> list) throws RemoteException;

    void addInstallSource(String str) throws RemoteException;

    void addNetworkRestriction(int i, List<String> list) throws RemoteException;

    void addProtectApplication(String str) throws RemoteException;

    void allowGetUsageStats(String str) throws RemoteException;

    void backupAppData(String str, String str2, String str3, int i) throws RemoteException;

    Bitmap captureFullScreen() throws RemoteException;

    boolean clearAllSuperWhiteList() throws RemoteException;

    void clearAppData(String str) throws RemoteException;

    void clearDefaultInputMethod() throws RemoteException;

    boolean clearSuperWhiteList(List<String> list) throws RemoteException;

    void deactivateSubId(int i) throws RemoteException;

    void deleteAccessibilityServiceWhiteList() throws RemoteException;

    void deleteInstallSource(String str) throws RemoteException;

    void deviceReboot() throws RemoteException;

    void deviceShutDown() throws RemoteException;

    void disconnectAllVpn() throws RemoteException;

    void enableInstallSource(boolean z) throws RemoteException;

    String executeShellToSetIptables(String str) throws RemoteException;

    boolean forceStopPackage(List<String> list, int i) throws RemoteException;

    List<String> getAccessibilityServiceWhiteList() throws RemoteException;

    int getAccessibilityStatusFromSettings() throws RemoteException;

    List<ComponentName> getAccessiblityEnabledListFromSettings() throws RemoteException;

    boolean getAdbInstallUninstallDisabled() throws RemoteException;

    List<String> getAppAlarmWhiteList() throws RemoteException;

    List<String> getAppInstallationPolicies(int i) throws RemoteException;

    List<String> getAppMeteredDataBlackList() throws RemoteException;

    List<String> getAppRuntimeExceptionInfo() throws RemoteException;

    List<String> getAppUninstallationPolicies(int i) throws RemoteException;

    List<String> getAppWlanDataBlackList() throws RemoteException;

    List<String> getBluetoothDevicesFromBlackLists() throws RemoteException;

    List<String> getBluetoothDevicesFromWhiteLists() throws RemoteException;

    List<String> getClearAppName() throws RemoteException;

    String getDefaultInputMethod() throws RemoteException;

    List<String> getDisabledDeactivateMdmPackages(ComponentName componentName) throws RemoteException;

    List<String> getDisallowUninstallPackageList() throws RemoteException;

    List<String> getDisallowedRunningApp() throws RemoteException;

    int getDrawOverlays(String str) throws RemoteException;

    List<String> getInstallSourceList() throws RemoteException;

    Bundle getInstallSysAppBundle() throws RemoteException;

    List<String> getNetworkRestrictionList(int i) throws RemoteException;

    String getPhoneNumber(int i) throws RemoteException;

    List<String> getProtectApplicationList() throws RemoteException;

    Uri getSimContactsUri(int i) throws RemoteException;

    List<String> getSuperWhiteList() throws RemoteException;

    int getVpnServiceState() throws RemoteException;

    List<String> getWifiBssidBlackList() throws RemoteException;

    List<String> getWifiBssidWhiteList() throws RemoteException;

    List<String> getWifiSsidBlackList() throws RemoteException;

    List<String> getWifiSsidWhiteList() throws RemoteException;

    List<String> getWlanApClientBlackList() throws RemoteException;

    List<String> getWlanApClientWhiteList() throws RemoteException;

    boolean isBlackListedBSSID(String str) throws RemoteException;

    boolean isBlackListedSSID(String str) throws RemoteException;

    boolean isDeviceRoot() throws RemoteException;

    boolean isInstallSourceEnable() throws RemoteException;

    boolean isWhiteListedBSSID(String str) throws RemoteException;

    boolean isWhiteListedSSID(String str) throws RemoteException;

    void killAppProcess(String str) throws RemoteException;

    void openCloseGps(boolean z) throws RemoteException;

    void openCloseNFC(boolean z) throws RemoteException;

    void removeAccessibilityServiceFromWhiteList(List<String> list) throws RemoteException;

    boolean removeAllAppAlarmWhiteList() throws RemoteException;

    void removeAllDisabledDeactivateMdmPackages(ComponentName componentName) throws RemoteException;

    void removeAllDisallowedUninstallPackages() throws RemoteException;

    boolean removeAppAlarmWhiteList(List<String> list) throws RemoteException;

    void removeAppMeteredDataBlackList(List<String> list) throws RemoteException;

    void removeAppWlanDataBlackList(List<String> list) throws RemoteException;

    boolean removeBSSIDFromBlackList(List<String> list) throws RemoteException;

    boolean removeBSSIDFromWhiteList(List<String> list) throws RemoteException;

    void removeBluetoothDevicesFromBlackList(List<String> list) throws RemoteException;

    void removeBluetoothDevicesFromWhiteList(List<String> list) throws RemoteException;

    void removeDisabledDeactivateMdmPackages(List<String> list) throws RemoteException;

    void removeDisallowedRunningApp(List<String> list) throws RemoteException;

    void removeDisallowedUninstallPackages(List<String> list) throws RemoteException;

    void removeNetworkRestriction(int i, List<String> list) throws RemoteException;

    void removeNetworkRestrictionAll(int i) throws RemoteException;

    void removeProtectApplication(String str) throws RemoteException;

    boolean removeSSIDFromBlackList(List<String> list) throws RemoteException;

    boolean removeSSIDFromWhiteList(List<String> list) throws RemoteException;

    void removeWlanApClientBlackList(List<String> list) throws RemoteException;

    void removeWlanApClientWhiteList(List<String> list) throws RemoteException;

    void resetFactory() throws RemoteException;

    void setAccessibilityEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setAdbInstallUninstallDisabled(boolean z) throws RemoteException;

    void setAirplaneMode(boolean z) throws RemoteException;

    void setAppInstallationPolicies(int i, List<String> list) throws RemoteException;

    void setAppUninstallationPolicies(int i, List<String> list) throws RemoteException;

    void setBLBlackList(ComponentName componentName, List<String> list, boolean z) throws RemoteException;

    void setBLWhiteList(ComponentName componentName, List<String> list, boolean z) throws RemoteException;

    void setDB(String str, int i) throws RemoteException;

    void setDataEnabled(boolean z) throws RemoteException;

    void setDefaultInputMethod(String str) throws RemoteException;

    void setDevelopmentEnabled(boolean z) throws RemoteException;

    boolean setDeviceOwner(ComponentName componentName) throws RemoteException;

    boolean setDrawOverlays(String str, int i) throws RemoteException;

    void setEmmAdmin(ComponentName componentName, boolean z) throws RemoteException;

    void setInstallSysAppBundle(Bundle bundle) throws RemoteException;

    void setNetworkRestriction(int i) throws RemoteException;

    void setProp(String str, String str2) throws RemoteException;

    void setSDCardFormatted() throws RemoteException;

    void setSettingsRestriction(String str, boolean z) throws RemoteException;

    boolean setSuperWhiteList(List<String> list) throws RemoteException;

    boolean setWifiBssidBlackList(List<String> list) throws RemoteException;

    boolean setWifiBssidWhiteList(List<String> list) throws RemoteException;

    boolean setWifiSsidBlackList(List<String> list) throws RemoteException;

    boolean setWifiSsidWhiteList(List<String> list) throws RemoteException;

    void setWlanApClientBlackList(List<String> list) throws RemoteException;

    void setWlanApClientWhiteList(List<String> list) throws RemoteException;

    void updateConfiguration(Configuration configuration) throws RemoteException;

    public static class Default implements IOppoCustomizeService {
        @Override // android.os.IOppoCustomizeService
        public void deviceReboot() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void deviceShutDown() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setSDCardFormatted() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isDeviceRoot() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void clearAppData(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getClearAppName() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setProp(String prop, String value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setDB(String key, int value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setSettingsRestriction(String key, boolean value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void openCloseGps(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void openCloseNFC(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setAccessibilityEnabled(ComponentName cn2, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public int getAccessibilityStatusFromSettings() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoCustomizeService
        public List<ComponentName> getAccessiblityEnabledListFromSettings() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setDataEnabled(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setDeviceOwner(ComponentName cn2) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void setEmmAdmin(ComponentName cn2, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void killAppProcess(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addProtectApplication(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeProtectApplication(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getProtectApplicationList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public Bitmap captureFullScreen() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void resetFactory() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void allowGetUsageStats(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setDrawOverlays(String packageName, int mode) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public int getDrawOverlays(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoCustomizeService
        public void setAdbInstallUninstallDisabled(boolean disabled) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean getAdbInstallUninstallDisabled() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void updateConfiguration(Configuration config) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setAirplaneMode(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setDevelopmentEnabled(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisallowedUninstallPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeDisallowedUninstallPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAllDisallowedUninstallPackages() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getDisallowUninstallPackageList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setAppInstallationPolicies(int mode, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppInstallationPolicies(int mode) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setAppUninstallationPolicies(int mode, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppUninstallationPolicies(int mode) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppRuntimeExceptionInfo() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisabledDeactivateMdmPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeDisabledDeactivateMdmPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addAppAlarmWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppAlarmWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeAppAlarmWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeAllAppAlarmWhiteList() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public Uri getSimContactsUri(int slotId) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void addBluetoothDevicesToBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addBluetoothDevicesToWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeBluetoothDevicesFromBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeBluetoothDevicesFromWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getBluetoothDevicesFromBlackLists() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getBluetoothDevicesFromWhiteLists() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setBLBlackList(ComponentName admin, List<String> list, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setBLWhiteList(ComponentName admin, List<String> list, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setWlanApClientWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWlanApClientWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void removeWlanApClientWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setWlanApClientBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWlanApClientBlackList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void removeWlanApClientBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public String executeShellToSetIptables(String commandline) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setNetworkRestriction(int pattern) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addNetworkRestriction(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeNetworkRestriction(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeNetworkRestrictionAll(int pattern) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getNetworkRestrictionList(int pattern) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public int getVpnServiceState() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoCustomizeService
        public String getPhoneNumber(int subId) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void disconnectAllVpn() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void activateSubId(int subId) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void deactivateSubId(int subId) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setWifiSsidWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setWifiBssidWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setWifiSsidBlackList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setWifiBssidBlackList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWifiSsidWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWifiSsidBlackList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWifiBssidWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getWifiBssidBlackList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isWhiteListedSSID(String ssid) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isBlackListedSSID(String ssid) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isWhiteListedBSSID(String bssid) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isBlackListedBSSID(String bssid) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeSSIDFromWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeSSIDFromBlackList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeBSSIDFromWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean removeBSSIDFromBlackList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void addInstallSource(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void enableInstallSource(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getInstallSourceList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isInstallSourceEnable() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void deleteInstallSource(String pkgName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisallowedRunningApp(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeDisallowedRunningApp(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getDisallowedRunningApp() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void addInstallPackageBlacklist(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addInstallPackageWhitelist(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean forceStopPackage(List<String> list, int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void setDefaultInputMethod(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public String getDefaultInputMethod() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void clearDefaultInputMethod() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addAppMeteredDataBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addAppWlanDataBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAppMeteredDataBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAppWlanDataBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppMeteredDataBlackList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppWlanDataBlackList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public Bundle getInstallSysAppBundle() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setInstallSysAppBundle(Bundle bundle) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addAccessibilityServiceToWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAccessibilityServiceFromWhiteList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAccessibilityServiceWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void deleteAccessibilityServiceWhiteList() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setSuperWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getSuperWhiteList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean clearSuperWhiteList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public boolean clearAllSuperWhiteList() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void backupAppData(String src, String packageName, String dest, int requestId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoCustomizeService {
        private static final String DESCRIPTOR = "android.os.IOppoCustomizeService";
        static final int TRANSACTION_activateSubId = 73;
        static final int TRANSACTION_addAccessibilityServiceToWhiteList = 113;
        static final int TRANSACTION_addAppAlarmWhiteList = 43;
        static final int TRANSACTION_addAppMeteredDataBlackList = 105;
        static final int TRANSACTION_addAppWlanDataBlackList = 106;
        static final int TRANSACTION_addBluetoothDevicesToBlackList = 50;
        static final int TRANSACTION_addBluetoothDevicesToWhiteList = 51;
        static final int TRANSACTION_addDisabledDeactivateMdmPackages = 41;
        static final int TRANSACTION_addDisallowedRunningApp = 96;
        static final int TRANSACTION_addDisallowedUninstallPackages = 32;
        static final int TRANSACTION_addInstallPackageBlacklist = 99;
        static final int TRANSACTION_addInstallPackageWhitelist = 100;
        static final int TRANSACTION_addInstallSource = 91;
        static final int TRANSACTION_addNetworkRestriction = 66;
        static final int TRANSACTION_addProtectApplication = 19;
        static final int TRANSACTION_allowGetUsageStats = 24;
        static final int TRANSACTION_backupAppData = 121;
        static final int TRANSACTION_captureFullScreen = 22;
        static final int TRANSACTION_clearAllSuperWhiteList = 120;
        static final int TRANSACTION_clearAppData = 5;
        static final int TRANSACTION_clearDefaultInputMethod = 104;
        static final int TRANSACTION_clearSuperWhiteList = 119;
        static final int TRANSACTION_deactivateSubId = 74;
        static final int TRANSACTION_deleteAccessibilityServiceWhiteList = 116;
        static final int TRANSACTION_deleteInstallSource = 95;
        static final int TRANSACTION_deviceReboot = 1;
        static final int TRANSACTION_deviceShutDown = 2;
        static final int TRANSACTION_disconnectAllVpn = 72;
        static final int TRANSACTION_enableInstallSource = 92;
        static final int TRANSACTION_executeShellToSetIptables = 64;
        static final int TRANSACTION_forceStopPackage = 101;
        static final int TRANSACTION_getAccessibilityServiceWhiteList = 115;
        static final int TRANSACTION_getAccessibilityStatusFromSettings = 13;
        static final int TRANSACTION_getAccessiblityEnabledListFromSettings = 14;
        static final int TRANSACTION_getAdbInstallUninstallDisabled = 28;
        static final int TRANSACTION_getAppAlarmWhiteList = 44;
        static final int TRANSACTION_getAppInstallationPolicies = 37;
        static final int TRANSACTION_getAppMeteredDataBlackList = 109;
        static final int TRANSACTION_getAppRuntimeExceptionInfo = 40;
        static final int TRANSACTION_getAppUninstallationPolicies = 39;
        static final int TRANSACTION_getAppWlanDataBlackList = 110;
        static final int TRANSACTION_getBluetoothDevicesFromBlackLists = 54;
        static final int TRANSACTION_getBluetoothDevicesFromWhiteLists = 55;
        static final int TRANSACTION_getClearAppName = 6;
        static final int TRANSACTION_getDefaultInputMethod = 103;
        static final int TRANSACTION_getDisabledDeactivateMdmPackages = 48;
        static final int TRANSACTION_getDisallowUninstallPackageList = 35;
        static final int TRANSACTION_getDisallowedRunningApp = 98;
        static final int TRANSACTION_getDrawOverlays = 26;
        static final int TRANSACTION_getInstallSourceList = 93;
        static final int TRANSACTION_getInstallSysAppBundle = 111;
        static final int TRANSACTION_getNetworkRestrictionList = 69;
        static final int TRANSACTION_getPhoneNumber = 71;
        static final int TRANSACTION_getProtectApplicationList = 21;
        static final int TRANSACTION_getSimContactsUri = 49;
        static final int TRANSACTION_getSuperWhiteList = 118;
        static final int TRANSACTION_getVpnServiceState = 70;
        static final int TRANSACTION_getWifiBssidBlackList = 82;
        static final int TRANSACTION_getWifiBssidWhiteList = 81;
        static final int TRANSACTION_getWifiSsidBlackList = 80;
        static final int TRANSACTION_getWifiSsidWhiteList = 79;
        static final int TRANSACTION_getWlanApClientBlackList = 62;
        static final int TRANSACTION_getWlanApClientWhiteList = 59;
        static final int TRANSACTION_isBlackListedBSSID = 86;
        static final int TRANSACTION_isBlackListedSSID = 84;
        static final int TRANSACTION_isDeviceRoot = 4;
        static final int TRANSACTION_isInstallSourceEnable = 94;
        static final int TRANSACTION_isWhiteListedBSSID = 85;
        static final int TRANSACTION_isWhiteListedSSID = 83;
        static final int TRANSACTION_killAppProcess = 18;
        static final int TRANSACTION_openCloseGps = 10;
        static final int TRANSACTION_openCloseNFC = 11;
        static final int TRANSACTION_removeAccessibilityServiceFromWhiteList = 114;
        static final int TRANSACTION_removeAllAppAlarmWhiteList = 46;
        static final int TRANSACTION_removeAllDisabledDeactivateMdmPackages = 47;
        static final int TRANSACTION_removeAllDisallowedUninstallPackages = 34;
        static final int TRANSACTION_removeAppAlarmWhiteList = 45;
        static final int TRANSACTION_removeAppMeteredDataBlackList = 107;
        static final int TRANSACTION_removeAppWlanDataBlackList = 108;
        static final int TRANSACTION_removeBSSIDFromBlackList = 90;
        static final int TRANSACTION_removeBSSIDFromWhiteList = 89;
        static final int TRANSACTION_removeBluetoothDevicesFromBlackList = 52;
        static final int TRANSACTION_removeBluetoothDevicesFromWhiteList = 53;
        static final int TRANSACTION_removeDisabledDeactivateMdmPackages = 42;
        static final int TRANSACTION_removeDisallowedRunningApp = 97;
        static final int TRANSACTION_removeDisallowedUninstallPackages = 33;
        static final int TRANSACTION_removeNetworkRestriction = 67;
        static final int TRANSACTION_removeNetworkRestrictionAll = 68;
        static final int TRANSACTION_removeProtectApplication = 20;
        static final int TRANSACTION_removeSSIDFromBlackList = 88;
        static final int TRANSACTION_removeSSIDFromWhiteList = 87;
        static final int TRANSACTION_removeWlanApClientBlackList = 63;
        static final int TRANSACTION_removeWlanApClientWhiteList = 60;
        static final int TRANSACTION_resetFactory = 23;
        static final int TRANSACTION_setAccessibilityEnabled = 12;
        static final int TRANSACTION_setAdbInstallUninstallDisabled = 27;
        static final int TRANSACTION_setAirplaneMode = 30;
        static final int TRANSACTION_setAppInstallationPolicies = 36;
        static final int TRANSACTION_setAppUninstallationPolicies = 38;
        static final int TRANSACTION_setBLBlackList = 56;
        static final int TRANSACTION_setBLWhiteList = 57;
        static final int TRANSACTION_setDB = 8;
        static final int TRANSACTION_setDataEnabled = 15;
        static final int TRANSACTION_setDefaultInputMethod = 102;
        static final int TRANSACTION_setDevelopmentEnabled = 31;
        static final int TRANSACTION_setDeviceOwner = 16;
        static final int TRANSACTION_setDrawOverlays = 25;
        static final int TRANSACTION_setEmmAdmin = 17;
        static final int TRANSACTION_setInstallSysAppBundle = 112;
        static final int TRANSACTION_setNetworkRestriction = 65;
        static final int TRANSACTION_setProp = 7;
        static final int TRANSACTION_setSDCardFormatted = 3;
        static final int TRANSACTION_setSettingsRestriction = 9;
        static final int TRANSACTION_setSuperWhiteList = 117;
        static final int TRANSACTION_setWifiBssidBlackList = 78;
        static final int TRANSACTION_setWifiBssidWhiteList = 76;
        static final int TRANSACTION_setWifiSsidBlackList = 77;
        static final int TRANSACTION_setWifiSsidWhiteList = 75;
        static final int TRANSACTION_setWlanApClientBlackList = 61;
        static final int TRANSACTION_setWlanApClientWhiteList = 58;
        static final int TRANSACTION_updateConfiguration = 29;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoCustomizeService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoCustomizeService)) {
                return new Proxy(obj);
            }
            return (IOppoCustomizeService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "deviceReboot";
                case 2:
                    return "deviceShutDown";
                case 3:
                    return "setSDCardFormatted";
                case 4:
                    return "isDeviceRoot";
                case 5:
                    return "clearAppData";
                case 6:
                    return "getClearAppName";
                case 7:
                    return "setProp";
                case 8:
                    return "setDB";
                case 9:
                    return "setSettingsRestriction";
                case 10:
                    return "openCloseGps";
                case 11:
                    return "openCloseNFC";
                case 12:
                    return "setAccessibilityEnabled";
                case 13:
                    return "getAccessibilityStatusFromSettings";
                case 14:
                    return "getAccessiblityEnabledListFromSettings";
                case 15:
                    return "setDataEnabled";
                case 16:
                    return "setDeviceOwner";
                case 17:
                    return "setEmmAdmin";
                case 18:
                    return "killAppProcess";
                case 19:
                    return "addProtectApplication";
                case 20:
                    return "removeProtectApplication";
                case 21:
                    return "getProtectApplicationList";
                case 22:
                    return "captureFullScreen";
                case 23:
                    return "resetFactory";
                case 24:
                    return "allowGetUsageStats";
                case 25:
                    return "setDrawOverlays";
                case 26:
                    return "getDrawOverlays";
                case 27:
                    return "setAdbInstallUninstallDisabled";
                case 28:
                    return "getAdbInstallUninstallDisabled";
                case 29:
                    return "updateConfiguration";
                case 30:
                    return "setAirplaneMode";
                case 31:
                    return "setDevelopmentEnabled";
                case 32:
                    return "addDisallowedUninstallPackages";
                case 33:
                    return "removeDisallowedUninstallPackages";
                case 34:
                    return "removeAllDisallowedUninstallPackages";
                case 35:
                    return "getDisallowUninstallPackageList";
                case 36:
                    return "setAppInstallationPolicies";
                case 37:
                    return "getAppInstallationPolicies";
                case 38:
                    return "setAppUninstallationPolicies";
                case 39:
                    return "getAppUninstallationPolicies";
                case 40:
                    return "getAppRuntimeExceptionInfo";
                case 41:
                    return "addDisabledDeactivateMdmPackages";
                case 42:
                    return "removeDisabledDeactivateMdmPackages";
                case 43:
                    return "addAppAlarmWhiteList";
                case 44:
                    return "getAppAlarmWhiteList";
                case 45:
                    return "removeAppAlarmWhiteList";
                case 46:
                    return "removeAllAppAlarmWhiteList";
                case 47:
                    return "removeAllDisabledDeactivateMdmPackages";
                case 48:
                    return "getDisabledDeactivateMdmPackages";
                case 49:
                    return "getSimContactsUri";
                case 50:
                    return "addBluetoothDevicesToBlackList";
                case 51:
                    return "addBluetoothDevicesToWhiteList";
                case 52:
                    return "removeBluetoothDevicesFromBlackList";
                case 53:
                    return "removeBluetoothDevicesFromWhiteList";
                case 54:
                    return "getBluetoothDevicesFromBlackLists";
                case 55:
                    return "getBluetoothDevicesFromWhiteLists";
                case 56:
                    return "setBLBlackList";
                case 57:
                    return "setBLWhiteList";
                case 58:
                    return "setWlanApClientWhiteList";
                case 59:
                    return "getWlanApClientWhiteList";
                case 60:
                    return "removeWlanApClientWhiteList";
                case 61:
                    return "setWlanApClientBlackList";
                case 62:
                    return "getWlanApClientBlackList";
                case 63:
                    return "removeWlanApClientBlackList";
                case 64:
                    return "executeShellToSetIptables";
                case 65:
                    return "setNetworkRestriction";
                case 66:
                    return "addNetworkRestriction";
                case 67:
                    return "removeNetworkRestriction";
                case 68:
                    return "removeNetworkRestrictionAll";
                case 69:
                    return "getNetworkRestrictionList";
                case 70:
                    return "getVpnServiceState";
                case 71:
                    return "getPhoneNumber";
                case 72:
                    return "disconnectAllVpn";
                case 73:
                    return "activateSubId";
                case 74:
                    return "deactivateSubId";
                case 75:
                    return "setWifiSsidWhiteList";
                case 76:
                    return "setWifiBssidWhiteList";
                case 77:
                    return "setWifiSsidBlackList";
                case 78:
                    return "setWifiBssidBlackList";
                case 79:
                    return "getWifiSsidWhiteList";
                case 80:
                    return "getWifiSsidBlackList";
                case 81:
                    return "getWifiBssidWhiteList";
                case 82:
                    return "getWifiBssidBlackList";
                case 83:
                    return "isWhiteListedSSID";
                case 84:
                    return "isBlackListedSSID";
                case 85:
                    return "isWhiteListedBSSID";
                case 86:
                    return "isBlackListedBSSID";
                case 87:
                    return "removeSSIDFromWhiteList";
                case 88:
                    return "removeSSIDFromBlackList";
                case 89:
                    return "removeBSSIDFromWhiteList";
                case 90:
                    return "removeBSSIDFromBlackList";
                case 91:
                    return "addInstallSource";
                case 92:
                    return "enableInstallSource";
                case 93:
                    return "getInstallSourceList";
                case 94:
                    return "isInstallSourceEnable";
                case 95:
                    return "deleteInstallSource";
                case 96:
                    return "addDisallowedRunningApp";
                case 97:
                    return "removeDisallowedRunningApp";
                case 98:
                    return "getDisallowedRunningApp";
                case 99:
                    return "addInstallPackageBlacklist";
                case 100:
                    return "addInstallPackageWhitelist";
                case 101:
                    return "forceStopPackage";
                case 102:
                    return "setDefaultInputMethod";
                case 103:
                    return "getDefaultInputMethod";
                case 104:
                    return "clearDefaultInputMethod";
                case 105:
                    return "addAppMeteredDataBlackList";
                case 106:
                    return "addAppWlanDataBlackList";
                case 107:
                    return "removeAppMeteredDataBlackList";
                case 108:
                    return "removeAppWlanDataBlackList";
                case 109:
                    return "getAppMeteredDataBlackList";
                case 110:
                    return "getAppWlanDataBlackList";
                case 111:
                    return "getInstallSysAppBundle";
                case 112:
                    return "setInstallSysAppBundle";
                case 113:
                    return "addAccessibilityServiceToWhiteList";
                case 114:
                    return "removeAccessibilityServiceFromWhiteList";
                case 115:
                    return "getAccessibilityServiceWhiteList";
                case 116:
                    return "deleteAccessibilityServiceWhiteList";
                case 117:
                    return "setSuperWhiteList";
                case 118:
                    return "getSuperWhiteList";
                case 119:
                    return "clearSuperWhiteList";
                case 120:
                    return "clearAllSuperWhiteList";
                case 121:
                    return "backupAppData";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            Configuration _arg04;
            ComponentName _arg05;
            ComponentName _arg06;
            ComponentName _arg07;
            ComponentName _arg08;
            Bundle _arg09;
            if (code != 1598968902) {
                boolean _arg010 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        deviceReboot();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        deviceShutDown();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setSDCardFormatted();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceRoot = isDeviceRoot();
                        reply.writeNoException();
                        reply.writeInt(isDeviceRoot ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        clearAppData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getClearAppName();
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setProp(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setDB(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setSettingsRestriction(_arg011, _arg010);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        openCloseGps(_arg010);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        openCloseNFC(_arg010);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setAccessibilityEnabled(_arg0, _arg010);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getAccessibilityStatusFromSettings();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        List<ComponentName> _result3 = getAccessiblityEnabledListFromSettings();
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setDataEnabled(_arg010);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean deviceOwner = setDeviceOwner(_arg02);
                        reply.writeNoException();
                        reply.writeInt(deviceOwner ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setEmmAdmin(_arg03, _arg010);
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        killAppProcess(data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        addProtectApplication(data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        removeProtectApplication(data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getProtectApplicationList();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result5 = captureFullScreen();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        resetFactory();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        allowGetUsageStats(data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean drawOverlays = setDrawOverlays(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(drawOverlays ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getDrawOverlays(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setAdbInstallUninstallDisabled(_arg010);
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean adbInstallUninstallDisabled = getAdbInstallUninstallDisabled();
                        reply.writeNoException();
                        reply.writeInt(adbInstallUninstallDisabled ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        updateConfiguration(_arg04);
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setAirplaneMode(_arg010);
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setDevelopmentEnabled(_arg010);
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        addDisallowedUninstallPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisallowedUninstallPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDisallowedUninstallPackages();
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result7 = getDisallowUninstallPackageList();
                        reply.writeNoException();
                        reply.writeStringList(_result7);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        setAppInstallationPolicies(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getAppInstallationPolicies(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        setAppUninstallationPolicies(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result9 = getAppUninstallationPolicies(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getAppRuntimeExceptionInfo();
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        addDisabledDeactivateMdmPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisabledDeactivateMdmPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        addAppAlarmWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result11 = getAppAlarmWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result11);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeAppAlarmWhiteList = removeAppAlarmWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeAppAlarmWhiteList ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeAllAppAlarmWhiteList = removeAllAppAlarmWhiteList();
                        reply.writeNoException();
                        reply.writeInt(removeAllAppAlarmWhiteList ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        removeAllDisabledDeactivateMdmPackages(_arg05);
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        List<String> _result12 = getDisabledDeactivateMdmPackages(_arg06);
                        reply.writeNoException();
                        reply.writeStringList(_result12);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result13 = getSimContactsUri(data.readInt());
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        addBluetoothDevicesToBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        addBluetoothDevicesToWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        removeBluetoothDevicesFromBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        removeBluetoothDevicesFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result14 = getBluetoothDevicesFromBlackLists();
                        reply.writeNoException();
                        reply.writeStringList(_result14);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result15 = getBluetoothDevicesFromWhiteLists();
                        reply.writeNoException();
                        reply.writeStringList(_result15);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        List<String> _arg1 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setBLBlackList(_arg07, _arg1, _arg010);
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        List<String> _arg12 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setBLWhiteList(_arg08, _arg12, _arg010);
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        setWlanApClientWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = getWlanApClientWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result16);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        removeWlanApClientWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        setWlanApClientBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getWlanApClientBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result17);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        removeWlanApClientBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = executeShellToSetIptables(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        setNetworkRestriction(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        addNetworkRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        removeNetworkRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        removeNetworkRestrictionAll(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result19 = getNetworkRestrictionList(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result19);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getVpnServiceState();
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getPhoneNumber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        disconnectAllVpn();
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        activateSubId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        deactivateSubId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wifiSsidWhiteList = setWifiSsidWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(wifiSsidWhiteList ? 1 : 0);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wifiBssidWhiteList = setWifiBssidWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(wifiBssidWhiteList ? 1 : 0);
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wifiSsidBlackList = setWifiSsidBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(wifiSsidBlackList ? 1 : 0);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wifiBssidBlackList = setWifiBssidBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(wifiBssidBlackList ? 1 : 0);
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result22 = getWifiSsidWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result22);
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result23 = getWifiSsidBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result23);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result24 = getWifiBssidWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result24);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result25 = getWifiBssidBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result25);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWhiteListedSSID = isWhiteListedSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWhiteListedSSID ? 1 : 0);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBlackListedSSID = isBlackListedSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackListedSSID ? 1 : 0);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWhiteListedBSSID = isWhiteListedBSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWhiteListedBSSID ? 1 : 0);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBlackListedBSSID = isBlackListedBSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackListedBSSID ? 1 : 0);
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeSSIDFromWhiteList = removeSSIDFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeSSIDFromWhiteList ? 1 : 0);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeSSIDFromBlackList = removeSSIDFromBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeSSIDFromBlackList ? 1 : 0);
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeBSSIDFromWhiteList = removeBSSIDFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBSSIDFromWhiteList ? 1 : 0);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeBSSIDFromBlackList = removeBSSIDFromBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBSSIDFromBlackList ? 1 : 0);
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        addInstallSource(data.readString());
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        enableInstallSource(_arg010);
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result26 = getInstallSourceList();
                        reply.writeNoException();
                        reply.writeStringList(_result26);
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInstallSourceEnable = isInstallSourceEnable();
                        reply.writeNoException();
                        reply.writeInt(isInstallSourceEnable ? 1 : 0);
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        deleteInstallSource(data.readString());
                        reply.writeNoException();
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        addDisallowedRunningApp(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisallowedRunningApp(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result27 = getDisallowedRunningApp();
                        reply.writeNoException();
                        reply.writeStringList(_result27);
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        addInstallPackageBlacklist(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        addInstallPackageWhitelist(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        boolean forceStopPackage = forceStopPackage(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(forceStopPackage ? 1 : 0);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        setDefaultInputMethod(data.readString());
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        String _result28 = getDefaultInputMethod();
                        reply.writeNoException();
                        reply.writeString(_result28);
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        clearDefaultInputMethod();
                        reply.writeNoException();
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        addAppMeteredDataBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        addAppWlanDataBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        removeAppMeteredDataBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        removeAppWlanDataBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result29 = getAppMeteredDataBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result29);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result30 = getAppWlanDataBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result30);
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result31 = getInstallSysAppBundle();
                        reply.writeNoException();
                        if (_result31 != null) {
                            reply.writeInt(1);
                            _result31.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        setInstallSysAppBundle(_arg09);
                        reply.writeNoException();
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        addAccessibilityServiceToWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        removeAccessibilityServiceFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result32 = getAccessibilityServiceWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result32);
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        deleteAccessibilityServiceWhiteList();
                        reply.writeNoException();
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        boolean superWhiteList = setSuperWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(superWhiteList ? 1 : 0);
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result33 = getSuperWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result33);
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        boolean clearSuperWhiteList = clearSuperWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(clearSuperWhiteList ? 1 : 0);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        boolean clearAllSuperWhiteList = clearAllSuperWhiteList();
                        reply.writeNoException();
                        reply.writeInt(clearAllSuperWhiteList ? 1 : 0);
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        backupAppData(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoCustomizeService {
            public static IOppoCustomizeService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.os.IOppoCustomizeService
            public void deviceReboot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceReboot();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void deviceShutDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceShutDown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setSDCardFormatted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSDCardFormatted();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isDeviceRoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceRoot();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void clearAppData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearAppData(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getClearAppName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClearAppName();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setProp(String prop, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(prop);
                    _data.writeString(value);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProp(prop, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDB(String key, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDB(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setSettingsRestriction(String key, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSettingsRestriction(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void openCloseGps(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openCloseGps(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void openCloseNFC(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openCloseNFC(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAccessibilityEnabled(ComponentName cn2, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAccessibilityEnabled(cn2, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public int getAccessibilityStatusFromSettings() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAccessibilityStatusFromSettings();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<ComponentName> getAccessiblityEnabledListFromSettings() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAccessiblityEnabledListFromSettings();
                    }
                    _reply.readException();
                    List<ComponentName> _result = _reply.createTypedArrayList(ComponentName.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDataEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setDeviceOwner(ComponentName cn2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceOwner(cn2);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setEmmAdmin(ComponentName cn2, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setEmmAdmin(cn2, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void killAppProcess(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killAppProcess(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addProtectApplication(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addProtectApplication(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeProtectApplication(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeProtectApplication(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getProtectApplicationList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProtectApplicationList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public Bitmap captureFullScreen() throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureFullScreen();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void resetFactory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetFactory();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void allowGetUsageStats(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowGetUsageStats(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setDrawOverlays(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDrawOverlays(packageName, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public int getDrawOverlays(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDrawOverlays(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAdbInstallUninstallDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disabled ? 1 : 0);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAdbInstallUninstallDisabled(disabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean getAdbInstallUninstallDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdbInstallUninstallDisabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void updateConfiguration(Configuration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateConfiguration(config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAirplaneMode(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAirplaneMode(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDevelopmentEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDevelopmentEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addDisallowedUninstallPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisallowedUninstallPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeDisallowedUninstallPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDisallowedUninstallPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAllDisallowedUninstallPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDisallowedUninstallPackages();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getDisallowUninstallPackageList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisallowUninstallPackageList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAppInstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStringList(appPackageNames);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppInstallationPolicies(mode, appPackageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppInstallationPolicies(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppInstallationPolicies(mode);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAppUninstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStringList(appPackageNames);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppUninstallationPolicies(mode, appPackageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppUninstallationPolicies(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUninstallationPolicies(mode);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppRuntimeExceptionInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppRuntimeExceptionInfo();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addDisabledDeactivateMdmPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisabledDeactivateMdmPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeDisabledDeactivateMdmPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDisabledDeactivateMdmPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addAppAlarmWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAppAlarmWhiteList(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppAlarmWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppAlarmWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeAppAlarmWhiteList(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    boolean _result = false;
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAppAlarmWhiteList(packageNames);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeAllAppAlarmWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAllAppAlarmWhiteList();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDisabledDeactivateMdmPackages(admin);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisabledDeactivateMdmPackages(admin);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public Uri getSimContactsUri(int slotId) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimContactsUri(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addBluetoothDevicesToBlackList(List<String> addressList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(addressList);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addBluetoothDevicesToBlackList(addressList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addBluetoothDevicesToWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addBluetoothDevicesToWhiteList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeBluetoothDevicesFromBlackList(List<String> addressList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(addressList);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeBluetoothDevicesFromBlackList(addressList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeBluetoothDevicesFromWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeBluetoothDevicesFromWhiteList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getBluetoothDevicesFromBlackLists() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBluetoothDevicesFromBlackLists();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getBluetoothDevicesFromWhiteLists() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBluetoothDevicesFromWhiteLists();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setBLBlackList(ComponentName admin, List<String> list, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(list);
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBLBlackList(admin, list, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setBLWhiteList(ComponentName admin, List<String> list, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(list);
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBLWhiteList(admin, list, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setWlanApClientWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWlanApClientWhiteList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWlanApClientWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWlanApClientWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeWlanApClientWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeWlanApClientWhiteList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setWlanApClientBlackList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWlanApClientBlackList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWlanApClientBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWlanApClientBlackList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeWlanApClientBlackList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeWlanApClientBlackList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public String executeShellToSetIptables(String commandline) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(commandline);
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().executeShellToSetIptables(commandline);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setNetworkRestriction(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetworkRestriction(pattern);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addNetworkRestriction(int pattern, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addNetworkRestriction(pattern, list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeNetworkRestriction(int pattern, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNetworkRestriction(pattern, list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeNetworkRestrictionAll(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNetworkRestrictionAll(pattern);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getNetworkRestrictionList(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkRestrictionList(pattern);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public int getVpnServiceState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVpnServiceState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public String getPhoneNumber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPhoneNumber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void disconnectAllVpn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disconnectAllVpn();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void activateSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().activateSubId(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void deactivateSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(74, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deactivateSubId(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setWifiSsidWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    boolean _result = false;
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiSsidWhiteList(list);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setWifiBssidWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    boolean _result = false;
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiBssidWhiteList(list);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setWifiSsidBlackList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    boolean _result = false;
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiSsidBlackList(list);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setWifiBssidBlackList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    boolean _result = false;
                    if (!this.mRemote.transact(78, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiBssidBlackList(list);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWifiSsidWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiSsidWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWifiSsidBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiSsidBlackList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWifiBssidWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiBssidWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getWifiBssidBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiBssidBlackList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isWhiteListedSSID(String ssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ssid);
                    boolean _result = false;
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWhiteListedSSID(ssid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isBlackListedSSID(String ssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ssid);
                    boolean _result = false;
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBlackListedSSID(ssid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isWhiteListedBSSID(String bssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(bssid);
                    boolean _result = false;
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWhiteListedBSSID(bssid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isBlackListedBSSID(String bssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(bssid);
                    boolean _result = false;
                    if (!this.mRemote.transact(86, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBlackListedBSSID(bssid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeSSIDFromWhiteList(List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(ssids);
                    boolean _result = false;
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeSSIDFromWhiteList(ssids);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeSSIDFromBlackList(List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(ssids);
                    boolean _result = false;
                    if (!this.mRemote.transact(88, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeSSIDFromBlackList(ssids);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeBSSIDFromWhiteList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeBSSIDFromWhiteList(bssids);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean removeBSSIDFromBlackList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeBSSIDFromBlackList(bssids);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addInstallSource(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstallSource(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void enableInstallSource(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableInstallSource(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getInstallSourceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(93, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallSourceList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isInstallSourceEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInstallSourceEnable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void deleteInstallSource(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(95, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteInstallSource(pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addDisallowedRunningApp(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(96, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisallowedRunningApp(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeDisallowedRunningApp(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(97, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDisallowedRunningApp(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getDisallowedRunningApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisallowedRunningApp();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addInstallPackageBlacklist(int pattern, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(99, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstallPackageBlacklist(pattern, packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addInstallPackageWhitelist(int pattern, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstallPackageWhitelist(pattern, packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean forceStopPackage(List<String> pkgs, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceStopPackage(pkgs, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDefaultInputMethod(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultInputMethod(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public String getDefaultInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultInputMethod();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void clearDefaultInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(104, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearDefaultInputMethod();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addAppMeteredDataBlackList(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (this.mRemote.transact(105, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAppMeteredDataBlackList(pkgs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addAppWlanDataBlackList(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (this.mRemote.transact(106, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAppWlanDataBlackList(pkgs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAppMeteredDataBlackList(List<String> addressList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(addressList);
                    if (this.mRemote.transact(107, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAppMeteredDataBlackList(addressList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAppWlanDataBlackList(List<String> addressList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(addressList);
                    if (this.mRemote.transact(108, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAppWlanDataBlackList(addressList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppMeteredDataBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppMeteredDataBlackList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppWlanDataBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(110, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppWlanDataBlackList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public Bundle getInstallSysAppBundle() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(111, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallSysAppBundle();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setInstallSysAppBundle(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(112, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInstallSysAppBundle(bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addAccessibilityServiceToWhiteList(List<String> serviceList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(serviceList);
                    if (this.mRemote.transact(113, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAccessibilityServiceToWhiteList(serviceList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAccessibilityServiceFromWhiteList(List<String> serviceList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(serviceList);
                    if (this.mRemote.transact(114, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAccessibilityServiceFromWhiteList(serviceList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAccessibilityServiceWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(115, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAccessibilityServiceWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void deleteAccessibilityServiceWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(116, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteAccessibilityServiceWhiteList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setSuperWhiteList(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    boolean _result = false;
                    if (!this.mRemote.transact(117, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSuperWhiteList(list);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getSuperWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(118, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSuperWhiteList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean clearSuperWhiteList(List<String> clearList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(clearList);
                    boolean _result = false;
                    if (!this.mRemote.transact(119, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearSuperWhiteList(clearList);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean clearAllSuperWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(120, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearAllSuperWhiteList();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void backupAppData(String src, String packageName, String dest, int requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(src);
                    _data.writeString(packageName);
                    _data.writeString(dest);
                    _data.writeInt(requestId);
                    if (this.mRemote.transact(121, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().backupAppData(src, packageName, dest, requestId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoCustomizeService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoCustomizeService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
