package com.android.server.wifi.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.location.LocationManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.wifi.OppoScanResultsProxy;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;
import java.util.ArrayList;
import java.util.List;

public class WifiPermissionsUtil {
    private static final boolean DEFAULT_CANACCESS_SCANRESULT_WITHOUT_LOCCATIONON = true;
    private static final String DEFAULT_RMNETWORK_APP = "com.coloros.wifisecuredetect,com.coloros.oshare,";
    private static final String TAG = "WifiPermissionsUtil";
    public static final List<String> mRmNetworkAppList = new ArrayList();
    private final AppOpsManager mAppOps;
    private final Context mContext;
    @GuardedBy({"mLock"})
    private LocationManager mLocationManager;
    private final Object mLock = new Object();
    private WifiLog mLog;
    private final UserManager mUserManager;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

    public WifiPermissionsUtil(WifiPermissionsWrapper wifiPermissionsWrapper, Context context, UserManager userManager, WifiInjector wifiInjector) {
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mContext = context;
        this.mUserManager = userManager;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mLog = wifiInjector.makeLog(TAG);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        initRmNetworkAppList();
    }

    public boolean checkConfigOverridePermission(int uid) {
        try {
            if (this.mWifiPermissionsWrapper.getOverrideWifiConfigPermission(uid) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            this.mLog.err("Error checking for permission: %").r(e.getMessage()).flush();
            return false;
        }
    }

    public boolean checkChangePermission(int uid) {
        try {
            if (this.mWifiPermissionsWrapper.getChangeWifiConfigPermission(uid) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            this.mLog.err("Error checking for permission: %").r(e.getMessage()).flush();
            return false;
        }
    }

    public boolean checkWifiAccessPermission(int uid) {
        try {
            if (this.mWifiPermissionsWrapper.getAccessWifiStatePermission(uid) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            this.mLog.err("Error checking for permission: %").r(e.getMessage()).flush();
            return false;
        }
    }

    public void enforceLocationPermission(String pkgName, int uid) {
        if (!checkCallersLocationPermission(pkgName, uid, true)) {
            throw new SecurityException("UID " + uid + " does not have Coarse/Fine Location permission");
        }
    }

    public boolean isTargetSdkLessThan(String packageName, int versionCode) {
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid)).targetSdkVersion < versionCode) {
                Binder.restoreCallingIdentity(ident);
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        return false;
    }

    public boolean checkCallersLocationPermission(String pkgName, int uid, boolean coarseForTargetSdkLessThanQ) {
        boolean isTargetSdkLessThanQ = isTargetSdkLessThan(pkgName, 29);
        String permissionType = "android.permission.ACCESS_FINE_LOCATION";
        if (coarseForTargetSdkLessThanQ && isTargetSdkLessThanQ) {
            permissionType = "android.permission.ACCESS_COARSE_LOCATION";
        }
        if (this.mWifiPermissionsWrapper.getUidPermission(permissionType, uid) == -1) {
            return false;
        }
        boolean isAppOpAllowed = noteAppOpAllowed(1, pkgName, uid);
        if (isAppOpAllowed || !coarseForTargetSdkLessThanQ || !isTargetSdkLessThanQ) {
            return isAppOpAllowed;
        }
        return noteAppOpAllowed(0, pkgName, uid);
    }

    public void enforceFineLocationPermission(String pkgName, int uid) {
        if (!checkCallersFineLocationPermission(pkgName, uid, false)) {
            throw new SecurityException("UID " + uid + " does not have Fine Location permission");
        }
    }

    private boolean checkCallersFineLocationPermission(String pkgName, int uid, boolean hideFromAppOps) {
        if (this.mWifiPermissionsWrapper.getUidPermission("android.permission.ACCESS_FINE_LOCATION", uid) == -1) {
            return false;
        }
        if (hideFromAppOps) {
            if (!checkAppOpAllowed(1, pkgName, uid)) {
                return false;
            }
        } else if (!noteAppOpAllowed(1, pkgName, uid)) {
            return false;
        }
        return true;
    }

    private boolean checkCallersHardwareLocationPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.LOCATION_HARDWARE", uid) == 0;
    }

    public void enforceCanAccessScanResults(String pkgName, int uid) throws SecurityException {
        checkPackage(uid, pkgName);
        if (!checkNetworkSettingsPermission(uid) && !checkNetworkSetupWizardPermission(uid) && !checkNetworkManagedProvisioningPermission(uid) && !checkNetworkStackPermission(uid) && !checkMainlineNetworkStackPermission(uid) && !OppoScanResultsProxy.isPackageCanGetScanResults(pkgName)) {
            boolean canAppPackageUseLocation = true;
            if (isLocationModeEnabled() || getRomUpdateBooleanValue("CANACCESS_SCANRESULT_WITHOUT_LOCCATIONON", true).booleanValue()) {
                boolean canCallingUidAccessLocation = checkCallerHasPeersMacAddressPermission(uid);
                if (!checkCallersLocationPermission(pkgName, uid, true) && !OppoScanResultsProxy.isNLP(pkgName)) {
                    canAppPackageUseLocation = false;
                }
                if (!canCallingUidAccessLocation && !canAppPackageUseLocation) {
                    throw new SecurityException("UID " + uid + " has no location permission");
                } else if (!isScanAllowedbyApps(pkgName, uid)) {
                    throw new SecurityException("UID " + uid + " has no wifi scan permission");
                } else if (!isCurrentProfile(uid) && !checkInteractAcrossUsersFull(uid)) {
                    throw new SecurityException("UID " + uid + " profile not permitted");
                }
            } else {
                throw new SecurityException("Location mode is disabled for the device");
            }
        }
    }

    public void enforceCanAccessScanResultsForWifiScanner(String pkgName, int uid, boolean ignoreLocationSettings, boolean hideFromAppOps) throws SecurityException {
        checkPackage(uid, pkgName);
        if (!isLocationModeEnabled()) {
            if (ignoreLocationSettings) {
                WifiLog wifiLog = this.mLog;
                wifiLog.w("Request from " + pkgName + " violated location settings");
            } else {
                throw new SecurityException("Location mode is disabled for the device");
            }
        }
        if (!checkCallersFineLocationPermission(pkgName, uid, hideFromAppOps) || !checkCallersHardwareLocationPermission(uid)) {
            throw new SecurityException("UID " + uid + " has no location permission");
        } else if (!isScanAllowedbyApps(pkgName, uid)) {
            throw new SecurityException("UID " + uid + " has no wifi scan permission");
        }
    }

    public boolean checkCanAccessWifiDirect(String pkgName, int uid, boolean needLocationModeEnabled) {
        try {
            checkPackage(uid, pkgName);
            if (checkNetworkSettingsPermission(uid)) {
                return true;
            }
            if (needLocationModeEnabled && !isLocationModeEnabled()) {
                Slog.e(TAG, "Location mode is disabled for the device");
                return false;
            } else if (checkCallersLocationPermission(pkgName, uid, false)) {
                return true;
            } else {
                Slog.e(TAG, "UID " + uid + " has no location permission");
                return false;
            }
        } catch (SecurityException se) {
            Slog.e(TAG, "Package check exception - " + se);
            return false;
        }
    }

    public void checkPackage(int uid, String pkgName) throws SecurityException {
        if (pkgName != null) {
            this.mAppOps.checkPackage(uid, pkgName);
            return;
        }
        throw new SecurityException("Checking UID " + uid + " but Package Name is Null");
    }

    private boolean checkCallerHasPeersMacAddressPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.PEERS_MAC_ADDRESS", uid) == 0;
    }

    private boolean isScanAllowedbyApps(String pkgName, int uid) {
        return noteAppOpAllowed(10, pkgName, uid);
    }

    private boolean checkInteractAcrossUsersFull(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid) == 0;
    }

    private boolean isCurrentProfile(int uid) {
        int currentUser = this.mWifiPermissionsWrapper.getCurrentUser();
        int callingUserId = this.mWifiPermissionsWrapper.getCallingUserId(uid);
        if (callingUserId == currentUser || callingUserId == 999) {
            return true;
        }
        for (UserInfo user : this.mUserManager.getProfiles(currentUser)) {
            if (user.id == callingUserId) {
                return true;
            }
        }
        return false;
    }

    private boolean noteAppOpAllowed(int op, String pkgName, int uid) {
        return this.mAppOps.noteOp(op, uid, pkgName) == 0;
    }

    private boolean checkAppOpAllowed(int op, String pkgName, int uid) {
        return this.mAppOps.checkOp(op, uid, pkgName) == 0;
    }

    private boolean retrieveLocationManagerIfNecessary() {
        synchronized (this.mLock) {
            if (this.mLocationManager == null) {
                this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            }
        }
        return this.mLocationManager != null;
    }

    public boolean isLocationModeEnabled() {
        if (!retrieveLocationManagerIfNecessary()) {
            return false;
        }
        return this.mLocationManager.isLocationEnabledForUser(UserHandle.of(this.mWifiPermissionsWrapper.getCurrentUser()));
    }

    public boolean checkNetworkSettingsPermission(int uid) {
        String pkgName = null;
        Context context = this.mContext;
        if (context != null) {
            pkgName = context.getPackageManager().getNameForUid(uid);
        }
        if (!inRmNetworkWhiteList(pkgName) && this.mWifiPermissionsWrapper.getUidPermission("android.permission.NETWORK_SETTINGS", uid) != 0) {
            return false;
        }
        return true;
    }

    public boolean checkLocalMacAddressPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.LOCAL_MAC_ADDRESS", uid) == 0;
    }

    public boolean checkNetworkSetupWizardPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.NETWORK_SETUP_WIZARD", uid) == 0;
    }

    public boolean checkNetworkStackPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.NETWORK_STACK", uid) == 0;
    }

    public boolean checkMainlineNetworkStackPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.MAINLINE_NETWORK_STACK", uid) == 0;
    }

    public boolean checkNetworkManagedProvisioningPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.NETWORK_MANAGED_PROVISIONING", uid) == 0;
    }

    public boolean checkNetworkCarrierProvisioningPermission(int uid) {
        return this.mWifiPermissionsWrapper.getUidPermission("android.permission.NETWORK_CARRIER_PROVISIONING", uid) == 0;
    }

    public boolean checkSystemAlertWindowPermission(int callingUid, String callingPackage) {
        int mode = this.mAppOps.noteOp(24, callingUid, callingPackage);
        return mode == 3 ? this.mWifiPermissionsWrapper.getUidPermission("android.permission.SYSTEM_ALERT_WINDOW", callingUid) == 0 : mode == 0;
    }

    private Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private void initRmNetworkAppList() {
        String value = getRomUpdateValue("NETWORK_REMOVE_APP", DEFAULT_RMNETWORK_APP);
        synchronized (mRmNetworkAppList) {
            if (!mRmNetworkAppList.isEmpty()) {
                mRmNetworkAppList.clear();
            }
            for (String name : value.split(",")) {
                mRmNetworkAppList.add(name.trim());
            }
        }
    }

    private boolean inList(String pkgName, List<String> appList) {
        if (pkgName == null || appList == null) {
            Slog.e(TAG, "pkgName = null");
            return false;
        } else if (appList.size() <= 0) {
            return false;
        } else {
            synchronized (appList) {
                for (String name : appList) {
                    if (pkgName.contains(name)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public boolean inRmNetworkWhiteList(String pkgName) {
        return inList(pkgName, mRmNetworkAppList);
    }
}
