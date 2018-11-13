package com.android.server.wifi.scanner;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.wifi.OppoManuConnectManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

class OppoWiFiScanBlockPolicy {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    public static final int BACKGROUND_SCAN_RESULTS_INTERVAL = 2000;
    private static final String DEFAULT_BLACK_LIST = "fn5.blacklist.reserve";
    private static final String DEFAULT_LOCATION_APP = "map,navi";
    private static final String DEFAULT_SYSTEM_APP = "com.google,com.android,android.net";
    private static final int NAVIGATION_MODE_FALSE = 2;
    private static final int NAVIGATION_MODE_INVALID = -1;
    private static final int NAVIGATION_MODE_TRUE = 1;
    private static final int SCAN_COUNT_MAX = 3;
    private static final String TAG = "OppoWiFiScanBlockPolicy";
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static boolean sDBG = false;
    private Context mContext;
    private Random mRandom;
    private int mScanCount = 0;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    public OppoWiFiScanBlockPolicy(Context context, WifiRomUpdateHelper wifiromupdate) {
        this.mContext = context;
        this.mWifiRomUpdateHelper = wifiromupdate;
        this.mRandom = new Random(Calendar.getInstance().getTimeInMillis());
    }

    public OppoWiFiScanBlockPolicy(Context context) {
        this.mContext = context;
        this.mWifiRomUpdateHelper = new WifiRomUpdateHelper(this.mContext);
        this.mRandom = new Random(Calendar.getInstance().getTimeInMillis());
    }

    public void resetStat() {
        this.mScanCount = 0;
    }

    public boolean rejectAppScan(int callingUid) {
        if (callingUid < 0 || callingUid == 0 || callingUid == OppoManuConnectManager.UID_DEFAULT || callingUid == 1010) {
            log("[FN5] System triger scan. UID = " + callingUid);
            return false;
        }
        String pkgName = this.mContext.getPackageManager().getNameForUid(callingUid);
        if (pkgName == null) {
            log("[FN5] Fail to get package name from UID.");
            return false;
        }
        log("[FN5] " + pkgName + " request trigger scan , UID = " + callingUid);
        if (this.mScanCount < 3) {
            this.mScanCount++;
            log("[FN5] it is first scan in Connected State.");
            return false;
        } else if (inPkglist(pkgName, "NETWORK_BLACK_LIST", DEFAULT_BLACK_LIST)) {
            log("[FN5] Reject black list app to trigger scan. " + pkgName);
            return true;
        } else if (inPkglist(pkgName, "NETWORK_SYSTEM_APP", DEFAULT_SYSTEM_APP)) {
            log("[FN5] System app triger scan. " + pkgName);
            return false;
        } else {
            List<String> navigateAppList = new ArrayList();
            LocationManager mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            if (mLocationManager != null) {
                navigateAppList = mLocationManager.getInUsePackagesList();
            }
            if (navigateAppList != null && navigateAppList.size() > 0 && navigateAppList.contains(pkgName)) {
                log("[FN5] navigateApp App triger scan. " + pkgName);
                return false;
            } else if (isTopApp(callingUid)) {
                log("[FN5] Foreground App triger scan. " + pkgName);
                return false;
            } else {
                log("[FN5] Reject background App triger scan. " + pkgName);
                return true;
            }
        }
    }

    private boolean inPkglist(String pkgName, String key, String defaultList) {
        if (pkgName == null || key == null) {
            log("[FN5] key or pkgName is null.");
            return false;
        }
        String value = getRomUpdateValue(key, defaultList);
        if (value == null) {
            log("[FN5] Fail to getRomUpdateValue. " + key);
            return false;
        }
        log("[FN5] getRomUpdateValue() " + key + " is " + value);
        for (String name : value.split(",")) {
            if (pkgName.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopApp(int callingUid) {
        return ((ActivityManager) this.mContext.getSystemService("activity")).getUidImportance(callingUid) <= BACKGROUND_IMPORTANCE_CUTOFF;
    }

    public void spoofScanResults(ScanResult[] scanList) {
        log("[FN5] spoofScanResults() ");
        for (ScanResult result : scanList) {
            result.level += this.mRandom.nextInt(3) - 1;
            result.timestamp = SystemClock.elapsedRealtime() * 1000;
        }
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private void log(String s) {
        if (sDBG) {
            Log.d(TAG, s);
        }
    }
}
