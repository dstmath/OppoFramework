package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoWifiLockHelper {
    private static boolean DEBUG = false;
    public static final String HIGH_PERFORMANCE_DEFAULT_BLACK_LIST = null;
    public static final String LOW_LATENCY_DEFAULT_BLACK_LIST = null;
    private static final String TAG = "OppoWifiLockHelper";
    public static final List<String> mHighPerformanceBlackList = new ArrayList();
    public static final List<String> mLowLatencyBlackList = new ArrayList();
    private static BroadcastReceiver mReceiver = null;
    private static Context sContext;
    private static OppoWifiLockHelper sInstance = null;

    private String getPackageName(int uid) {
        PackageManager mPackageManager = sContext.getPackageManager();
        if (mPackageManager != null) {
            return mPackageManager.getNameForUid(uid);
        }
        Log.e(TAG, "mPackageManager is null!");
        return null;
    }

    public boolean isInBlackList(int uid, int lockMode) {
        if (lockMode == 3) {
            return isInHighPerformanceBlackList(uid);
        }
        if (lockMode == 4) {
            return isInLowLatencyBlackList(uid);
        }
        return false;
    }

    private boolean isInHighPerformanceBlackList(int uid) {
        return isInList(uid, mHighPerformanceBlackList);
    }

    private boolean isInLowLatencyBlackList(int uid) {
        return isInList(uid, mLowLatencyBlackList);
    }

    private boolean isInList(int uid, List<String> appList) {
        String pkgName = getPackageName(uid);
        if (pkgName == null || appList == null) {
            Log.d(TAG, "pkgName = null");
            return false;
        } else if (appList.size() <= 0) {
            return false;
        } else {
            synchronized (appList) {
                for (String name : appList) {
                    if (pkgName.contains(name)) {
                        logd("pkgName:" + pkgName + " in blacklist!");
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void initList() {
        String value;
        int i;
        Log.d(TAG, "initList!");
        if (WifiRomUpdateHelper.getInstance(sContext) != null && (value = WifiRomUpdateHelper.getInstance(sContext).getValue("WIFILOCK_HIGH_PERFORMANCE_BLACKLIST", HIGH_PERFORMANCE_DEFAULT_BLACK_LIST)) != null) {
            synchronized (mHighPerformanceBlackList) {
                if (!mHighPerformanceBlackList.isEmpty()) {
                    mHighPerformanceBlackList.clear();
                }
                for (String name : value.split(",")) {
                    mHighPerformanceBlackList.add(name.trim());
                }
            }
            String value2 = WifiRomUpdateHelper.getInstance(sContext).getValue("WIFILOCK_LOW_LATENCY_BLACKLIST", LOW_LATENCY_DEFAULT_BLACK_LIST);
            if (value2 != null) {
                synchronized (mLowLatencyBlackList) {
                    if (!mLowLatencyBlackList.isEmpty()) {
                        mLowLatencyBlackList.clear();
                    }
                    for (String name2 : value2.split(",")) {
                        mLowLatencyBlackList.add(name2.trim());
                    }
                }
            }
        }
    }

    private void registerForBroadcasts() {
        mReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiLockHelper.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null && "oppo.intent.action.WIFI_ROM_UPDATE_CHANGED".equals(intent.getAction())) {
                    OppoWifiLockHelper.this.initList();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        new IntentFilter();
        sContext.registerReceiver(mReceiver, filter);
    }

    public void reportLockModeUsageTime(int uid, long startTime, int lockMode) {
        HashMap<String, String> map = new HashMap<>();
        long usageTime = System.currentTimeMillis() - startTime;
        String lockModeStr = null;
        map.put("pkgName", getPackageName(uid));
        if (lockMode == 3) {
            lockModeStr = "high_performance_lock";
        } else if (lockMode == 4) {
            lockModeStr = "low_latency_lock";
        }
        if (lockModeStr != null) {
            map.put("lockMode", lockModeStr);
            map.put("usageTime", Long.toString(usageTime));
            logd(map.toString());
            OppoStatistics.onCommon(sContext, "wifi_fool_proof", "wifi_lock_usage", map, false);
        }
    }

    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, str);
        }
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "====verbose= " + verbose);
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    public OppoWifiLockHelper(Context contxt) {
        sContext = contxt;
    }

    public static synchronized OppoWifiLockHelper getInstance(Context context) {
        OppoWifiLockHelper oppoWifiLockHelper;
        synchronized (OppoWifiLockHelper.class) {
            if (sInstance == null) {
                synchronized (OppoWifiLockHelper.class) {
                    if (sInstance == null) {
                        sInstance = new OppoWifiLockHelper(context);
                    }
                }
            }
            oppoWifiLockHelper = sInstance;
        }
        return oppoWifiLockHelper;
    }
}
