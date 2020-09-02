package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiRomUpdateHelper;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ScanRequestProxy {
    public static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_INTERVAL_BG_APPS_MS = 1800000;
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW_FG_APPS = 4;
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_TIME_WINDOW_FG_APPS_MS = 120000;
    private static final int SCREEN_TIME = 20000;
    private static final String TAG = "WifiScanRequestProxy";
    private int THIRD_APP_SCAN_COUNT = 10;
    private int THIRD_APP_SCAN_FREQ = 1;
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private int mAppScanTimes = 0;
    private final Clock mClock;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public final List<ScanResult> mLastScanResults = new ArrayList();
    private long mLastScanTimestampForBgApps = 0;
    private final ArrayMap<Pair<Integer, String>, LinkedList<Long>> mLastScanTimestampsForFgApps = new ArrayMap<>();
    private OppoScanResultsProxy mOppoScanResultsProxy;
    private boolean mScanningEnabled = false;
    private boolean mScanningForHiddenNetworksEnabled = false;
    private long mScreenOffTime = 0;
    private boolean mScreenOn = false;
    private long mScreenOnTime = 0;
    private final ThrottleEnabledSettingObserver mThrottleEnabledSettingObserver;
    ArrayList<Integer> mUidList = new ArrayList<>();
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private WifiScanner mWifiScanner;

    private class GlobalScanListener implements WifiScanner.ScanListener {
        private GlobalScanListener() {
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Scan results received");
            }
            if (scanDatas.length != 1) {
                Log.wtf(ScanRequestProxy.TAG, "Found more than 1 batch of scan results, Failing...");
                ScanRequestProxy.this.sendScanResultBroadcast(false);
                return;
            }
            WifiScanner.ScanData scanData = scanDatas[0];
            ScanResult[] scanResults = scanData.getResults();
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Received " + scanResults.length + " scan results");
            }
            if (scanData.getBandScanned() == 7) {
                ScanRequestProxy.this.mLastScanResults.clear();
                ScanRequestProxy.this.mLastScanResults.addAll(Arrays.asList(scanResults));
                ScanRequestProxy.this.sendScanResultBroadcast(true);
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    private class ScanRequestProxyScanListener implements WifiScanner.ScanListener {
        private ScanRequestProxyScanListener() {
        }

        public void onSuccess() {
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Scan request succeeded");
            }
        }

        public void onFailure(int reason, String description) {
            Log.e(ScanRequestProxy.TAG, "Scan failure received. reason: " + reason + ",description: " + description);
            ScanRequestProxy.this.sendScanResultBroadcast(false);
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    private class ThrottleEnabledSettingObserver extends ContentObserver {
        private boolean mThrottleEnabled = true;

        ThrottleEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void initialize() {
            ScanRequestProxy.this.mFrameworkFacade.registerContentObserver(ScanRequestProxy.this.mContext, Settings.Global.getUriFor("wifi_scan_throttle_enabled"), true, this);
            this.mThrottleEnabled = getValue();
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.v(ScanRequestProxy.TAG, "Scan throttle enabled " + this.mThrottleEnabled);
            }
        }

        public boolean isEnabled() {
            return this.mThrottleEnabled;
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            this.mThrottleEnabled = getValue();
            Log.i(ScanRequestProxy.TAG, "Scan throttle enabled " + this.mThrottleEnabled);
        }

        private boolean getValue() {
            return ScanRequestProxy.this.mFrameworkFacade.getIntegerSetting(ScanRequestProxy.this.mContext, "wifi_scan_throttle_enabled", 1) == 1;
        }
    }

    ScanRequestProxy(Context context, AppOpsManager appOpsManager, ActivityManager activityManager, WifiInjector wifiInjector, WifiConfigManager configManager, WifiPermissionsUtil wifiPermissionUtil, WifiMetrics wifiMetrics, Clock clock, FrameworkFacade frameworkFacade, Handler handler) {
        this.mContext = context;
        this.mAppOps = appOpsManager;
        this.mActivityManager = activityManager;
        this.mWifiInjector = wifiInjector;
        this.mOppoScanResultsProxy = this.mWifiInjector.getOppoScanResultsProxy();
        this.mWifiConfigManager = configManager;
        this.mWifiPermissionsUtil = wifiPermissionUtil;
        this.mWifiMetrics = wifiMetrics;
        this.mClock = clock;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mFrameworkFacade = frameworkFacade;
        this.mThrottleEnabledSettingObserver = new ThrottleEnabledSettingObserver(handler);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        WifiScanner wifiScanner = this.mWifiScanner;
        if (wifiScanner != null) {
            wifiScanner.enableVerboseLogging(verbose);
        }
    }

    private boolean retrieveWifiScannerIfNecessary() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = this.mWifiInjector.getWifiScanner();
            this.mThrottleEnabledSettingObserver.initialize();
            WifiScanner wifiScanner = this.mWifiScanner;
            if (wifiScanner != null) {
                wifiScanner.registerScanListener(new GlobalScanListener());
            }
        }
        return this.mWifiScanner != null;
    }

    private void sendScanAvailableBroadcast(Context context, boolean available) {
        Log.d(TAG, "Sending scan available broadcast: " + available);
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        if (available) {
            intent.putExtra("scan_enabled", 3);
        } else {
            intent.putExtra("scan_enabled", 1);
        }
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void enableScanningInternal(boolean enable) {
        if (!retrieveWifiScannerIfNecessary()) {
            Log.e(TAG, "Failed to retrieve wifiscanner");
            return;
        }
        this.mWifiScanner.setScanningEnabled(enable);
        sendScanAvailableBroadcast(this.mContext, enable);
        clearScanResults();
        StringBuilder sb = new StringBuilder();
        sb.append("Scanning is ");
        sb.append(enable ? "enabled" : "disabled");
        Log.i(TAG, sb.toString());
    }

    public void enableScanning(boolean enable, boolean enableScanningForHiddenNetworks) {
        if (enable) {
            enableScanningInternal(true);
            this.mScanningForHiddenNetworksEnabled = enableScanningForHiddenNetworks;
            StringBuilder sb = new StringBuilder();
            sb.append("Scanning for hidden networks is ");
            sb.append(enableScanningForHiddenNetworks ? "enabled" : "disabled");
            Log.i(TAG, sb.toString());
        } else {
            enableScanningInternal(false);
        }
        this.mScanningEnabled = enable;
    }

    /* access modifiers changed from: private */
    public void sendScanResultBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    private void sendScanResultFailureBroadcastToPackage(String packageName) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", false);
        intent.setPackage(packageName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void trimPastScanRequestTimesForForegroundApp(List<Long> scanRequestTimestamps, long currentTimeMillis) {
        Iterator<Long> timestampsIter = scanRequestTimestamps.iterator();
        while (timestampsIter.hasNext() && currentTimeMillis - timestampsIter.next().longValue() > 120000) {
            timestampsIter.remove();
        }
    }

    private LinkedList<Long> getOrCreateScanRequestTimestampsForForegroundApp(int callingUid, String packageName) {
        Pair<Integer, String> uidAndPackageNamePair = Pair.create(Integer.valueOf(callingUid), packageName);
        LinkedList<Long> scanRequestTimestamps = this.mLastScanTimestampsForFgApps.get(uidAndPackageNamePair);
        if (scanRequestTimestamps != null) {
            return scanRequestTimestamps;
        }
        LinkedList<Long> scanRequestTimestamps2 = new LinkedList<>();
        this.mLastScanTimestampsForFgApps.put(uidAndPackageNamePair, scanRequestTimestamps2);
        return scanRequestTimestamps2;
    }

    private boolean shouldScanRequestBeThrottledForForegroundApp(int callingUid, String packageName) {
        OppoScanResultsProxy oppoScanResultsProxy = this.mOppoScanResultsProxy;
        if (oppoScanResultsProxy != null && !oppoScanResultsProxy.inScanBlackList(packageName)) {
            return false;
        }
        LinkedList<Long> scanRequestTimestamps = getOrCreateScanRequestTimestampsForForegroundApp(callingUid, packageName);
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        trimPastScanRequestTimesForForegroundApp(scanRequestTimestamps, currentTimeMillis);
        if (scanRequestTimestamps.size() >= 4) {
            return true;
        }
        scanRequestTimestamps.addLast(Long.valueOf(currentTimeMillis));
        return false;
    }

    private boolean shouldScanRequestBeThrottledForBackgroundApp(int callingUid, String packageName) {
        long lastScanMs = this.mLastScanTimestampForBgApps;
        long elapsedRealtime = this.mClock.getElapsedSinceBootMillis();
        OppoScanResultsProxy oppoScanResultsProxy = this.mOppoScanResultsProxy;
        if (oppoScanResultsProxy != null && oppoScanResultsProxy.inNavigationWhiteList(packageName)) {
            this.mLastScanTimestampForBgApps = elapsedRealtime;
            return false;
        } else if (lastScanMs != 0 && elapsedRealtime - lastScanMs < 1800000) {
            return true;
        } else {
            this.mLastScanTimestampForBgApps = elapsedRealtime;
            return false;
        }
    }

    private boolean isRequestFromBackground(int callingUid, String packageName) {
        this.mAppOps.checkPackage(callingUid, packageName);
        try {
            return this.mActivityManager.getPackageImportance(packageName) > 125;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to check the app state", e);
            return true;
        }
    }

    private boolean shouldScanRequestBeThrottledForApp(int callingUid, String packageName) {
        boolean isThrottled;
        if (isRequestFromBackground(callingUid, packageName)) {
            isThrottled = shouldScanRequestBeThrottledForBackgroundApp(callingUid, packageName);
            if (isThrottled) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Background scan app request [" + callingUid + ", " + packageName + "]");
                }
                this.mWifiMetrics.incrementExternalBackgroundAppOneshotScanRequestsThrottledCount();
            }
        } else {
            isThrottled = shouldScanRequestBeThrottledForForegroundApp(callingUid, packageName);
            if (isThrottled) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Foreground scan app request [" + callingUid + ", " + packageName + "]");
                }
                this.mWifiMetrics.incrementExternalForegroundAppOneshotScanRequestsThrottledCount();
            }
        }
        this.mWifiMetrics.incrementExternalAppOneshotScanRequestsCount();
        return isThrottled;
    }

    public boolean startScan(int callingUid, String packageName) {
        LocationManager mLocationManager;
        if (!retrieveWifiScannerIfNecessary()) {
            Log.e(TAG, "Failed to retrieve wifiscanner");
            sendScanResultFailureBroadcastToPackage(packageName);
            return false;
        }
        OppoScanResultsProxy oppoScanResultsProxy = this.mOppoScanResultsProxy;
        if (oppoScanResultsProxy != null) {
            oppoScanResultsProxy.scanTriggerStatistics(callingUid, packageName);
        }
        boolean fromSettingsOrSetupWizard = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(callingUid) || this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(callingUid);
        if (fromSettingsOrSetupWizard || !this.mThrottleEnabledSettingObserver.isEnabled() || !shouldScanRequestBeThrottledForApp(callingUid, packageName)) {
            Log.i(TAG, "Scan request from uid= " + callingUid + ", " + packageName);
            WorkSource workSource = new WorkSource(callingUid, packageName);
            WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
            if (fromSettingsOrSetupWizard) {
                settings.type = 2;
            }
            settings.band = 7;
            settings.reportEvents = 3;
            if (this.mScanningForHiddenNetworksEnabled) {
                List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mWifiConfigManager.retrieveHiddenNetworkList();
                settings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
            }
            this.mWifiScanner.startScan(settings, new ScanRequestProxyScanListener(), workSource);
            if (!this.mScreenOn && callingUid > 0 && callingUid != 1010 && callingUid != 1000) {
                List<String> navigateAppList = new ArrayList<>();
                String pkg = this.mContext.getPackageManager().getNameForUid(callingUid);
                Context context = this.mContext;
                if (!(context == null || (mLocationManager = (LocationManager) context.getSystemService("location")) == null)) {
                    navigateAppList = mLocationManager.getInUsePackagesList();
                }
                if (navigateAppList != null && navigateAppList.size() > 0 && navigateAppList.contains(pkg)) {
                    return true;
                }
            }
            if (!this.mScreenOn) {
                detectAndreportOPPOGuard(callingUid);
            }
            return true;
        }
        Log.i(TAG, "Scan request from " + packageName + " throttled");
        sendScanResultFailureBroadcastToPackage(packageName);
        return false;
    }

    private void detectAndreportOPPOGuard(int uid) {
        ArrayList<String> pskList = new ArrayList<>();
        ArrayList<Integer> updateUidlist = new ArrayList<>();
        boolean found = false;
        synchronized (this.mUidList) {
            if (!(uid <= 0 || uid == 1010 || uid == 1000)) {
                this.mAppScanTimes++;
                Iterator<Integer> it = this.mUidList.iterator();
                while (it.hasNext()) {
                    if (it.next().intValue() == uid) {
                        found = true;
                    }
                }
                if (!found) {
                    updateUidlist.add(Integer.valueOf(uid));
                }
            }
            Iterator<Integer> it2 = updateUidlist.iterator();
            while (it2.hasNext()) {
                this.mUidList.add(it2.next());
            }
            updateUidlist.clear();
            int AppScanCount = getRomUpdateIntegerValue("POWER_APP_SCAN_COUNT", Integer.valueOf(this.THIRD_APP_SCAN_COUNT)).intValue();
            int AppScanFreq = getRomUpdateIntegerValue("POWER_APP_SCAN_FREQ", Integer.valueOf(this.THIRD_APP_SCAN_FREQ)).intValue();
            if (this.mAppScanTimes > AppScanCount) {
                float deltime = ((float) (System.currentTimeMillis() - this.mScreenOffTime)) / 60000.0f;
                float scanFreq = 0.0f;
                if (deltime != 0.0f) {
                    scanFreq = deltime / ((float) this.mAppScanTimes);
                }
                if (scanFreq <= ((float) AppScanFreq) && scanFreq > 0.0f) {
                    Iterator<Integer> it3 = this.mUidList.iterator();
                    while (it3.hasNext()) {
                        Integer mUid = it3.next();
                        StringBuilder sbpkg = new StringBuilder();
                        String pkg = this.mContext.getPackageManager().getNameForUid(mUid.intValue());
                        Log.i(TAG, "mUid= " + mUid + "pkg = " + pkg);
                        sbpkg.append("[ ");
                        sbpkg.append(pkg);
                        sbpkg.append(" ] ");
                        pskList.add(sbpkg.toString());
                    }
                }
                boolean IsKillApp = getRomUpdateBooleanValue("POWER_APP_DETECT_AND_KILL", true).booleanValue();
                if (pskList.size() > 0) {
                    Intent wifiIntent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                    wifiIntent.putStringArrayListExtra("data", pskList);
                    wifiIntent.putExtra("type", "wifiscan");
                    Log.i(TAG, "oppo guard kill(" + IsKillApp + ") process");
                    if (IsKillApp) {
                        this.mContext.sendBroadcast(wifiIntent);
                    }
                    this.mAppScanTimes = 0;
                    this.mUidList.clear();
                }
            }
        }
    }

    public List<ScanResult> getScanResults() {
        return this.mLastScanResults;
    }

    private void clearScanResults() {
        this.mLastScanResults.clear();
        this.mLastScanTimestampForBgApps = 0;
        this.mLastScanTimestampsForFgApps.clear();
    }

    public void clearScanRequestTimestampsForApp(String packageName, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clearing scan request timestamps for uid=" + uid + ", packageName=" + packageName);
        }
        this.mLastScanTimestampsForFgApps.remove(Pair.create(Integer.valueOf(uid), packageName));
    }

    public List<ScanResult> syncGetScanResultsList() {
        return this.mOppoScanResultsProxy.syncGetScanResultsList();
    }
}
