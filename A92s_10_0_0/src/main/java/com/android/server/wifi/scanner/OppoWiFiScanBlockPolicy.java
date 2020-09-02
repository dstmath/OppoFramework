package com.android.server.wifi.scanner;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/* access modifiers changed from: package-private */
public class OppoWiFiScanBlockPolicy {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    public static final int BACKGROUND_SCAN_RESULTS_INTERVAL = 2000;
    private static final String DEFAULT_GAME_APP = "com.netease.hyxd,com.imangi.templerun,com.wepie.snake,com.ztgame.bob,com.tencent.tmgp,com.kiloo.subwaysurf";
    private static final String DEFAULT_SYSTEM_APP = "com.google,com.android,android.net";
    private static final int NAVIGATION_MODE_FALSE = 2;
    private static final int NAVIGATION_MODE_INVALID = -1;
    private static final int NAVIGATION_MODE_TRUE = 1;
    private static final int SCAN_COUNT_MAX = 3;
    private static final String TAG = "OppoWiFiScanBlockPolicy";
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static boolean sDBG = false;
    private Context mContext;
    private boolean mIsBlockedScanRequest = false;
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
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mRandom = new Random(Calendar.getInstance().getTimeInMillis());
    }

    public void resetStat() {
        this.mScanCount = 0;
    }

    public boolean rejectAppScan(int callingUid) {
        if (callingUid < 0 || callingUid == 0 || callingUid == 1000 || callingUid == 1010) {
            return false;
        }
        int i = this.mScanCount;
        if (i >= 3) {
            return isTopAppGame();
        }
        this.mScanCount = i + 1;
        log("[FN5] it is first scan in Connected State.");
        return false;
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
        for (String name : value.split(",")) {
            if (pkgName.contains(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopApp(int callingUid) {
        return ((ActivityManager) this.mContext.getSystemService("activity")).getUidImportance(callingUid) <= BACKGROUND_IMPORTANCE_CUTOFF;
    }

    public String getTopPkgName() {
        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (!tasks.isEmpty()) {
            return tasks.get(0).topActivity.getPackageName();
        }
        return " ";
    }

    private boolean isTopAppGame() {
        String pkgName = getTopPkgName();
        if (!inPkglist(pkgName, "NETWORK_GAME_APP", DEFAULT_GAME_APP)) {
            return false;
        }
        log("[FN5] Top APP is game. " + pkgName);
        return true;
    }

    public void spoofScanResults(ScanResult[] scanList) {
        log("[FN5] spoofScanResults() ");
        for (ScanResult result : scanList) {
            result.level += this.mRandom.nextInt(3) - 1;
            result.timestamp = SystemClock.elapsedRealtime() * 1000;
        }
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private void log(String s) {
        if (sDBG) {
            Log.d(TAG, s);
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDBG = true;
        } else {
            sDBG = false;
        }
    }

    public boolean blockScanRequest(WorkSource workSource, List<ScanResult> CachedScanResults) {
        if (workSource != null && workSource.size() > 0) {
            int callingUid = workSource.get(0);
            if (isWifiConnected(this.mContext) && CachedScanResults != null && CachedScanResults.size() > 0) {
                return rejectAppScan(callingUid);
            }
        }
        return false;
    }

    private boolean isWifiConnected(Context context) {
        NetworkInfo wifiNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
        if (wifiNetworkInfo == null || !wifiNetworkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public boolean getIsBlockedScanRequest() {
        return this.mIsBlockedScanRequest;
    }

    public void setBlockedScanRequest(boolean isBlockedScanRequest) {
        this.mIsBlockedScanRequest = isBlockedScanRequest;
    }

    public void resetStatOnConnected(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).getState().equals(NetworkInfo.State.CONNECTED)) {
                    OppoWiFiScanBlockPolicy.this.resetStat();
                }
            }
        }, new IntentFilter("android.net.wifi.STATE_CHANGE"));
    }
}
