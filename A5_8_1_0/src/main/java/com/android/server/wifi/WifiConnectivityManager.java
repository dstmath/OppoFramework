package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.PnoSettings;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.os.Handler;
import android.os.Looper;
import android.os.WorkSource;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WifiConnectivityManager {
    public static final int BSSID_BLACKLIST_EXPIRE_TIME_MS = 300000;
    public static final int BSSID_BLACKLIST_THRESHOLD = 3;
    private static final int CHANNEL_LIST_AGE_MS = 3600000;
    private static final int CONNECTED_PNO_SCAN_INTERVAL_MS = 160000;
    private static final int DISCONNECTED_PNO_SCAN_INTERVAL_MS = 20000;
    private static final int LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS = 80000;
    private static final int LOW_RSSI_NETWORK_RETRY_START_DELAY_MS = 20000;
    public static final int MAX_CONNECTION_ATTEMPTS_RATE = 6;
    public static final int MAX_CONNECTION_ATTEMPTS_TIME_INTERVAL_MS = 240000;
    public static final int MAX_PERIODIC_SCAN_INTERVAL_MS = 160000;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_RX_PACKET_FOR_PARTIAL_SCANS = 80;
    public static final int MAX_SCAN_RESTART_ALLOWED = 5;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    public static final int MAX_TX_PACKET_FOR_PARTIAL_SCANS = 40;
    private static final int PASSPOINT_NETWORK_EVALUATOR_PRIORITY = 2;
    public static final int PERIODIC_SCAN_INTERVAL_MS = 15000;
    public static final String PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Periodic Scan Timer";
    public static final int REASON_CODE_AP_UNABLE_TO_HANDLE_NEW_STA = 17;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    public static final String RESTART_CONNECTIVITY_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Scan";
    private static final int RESTART_SCAN_DELAY_MS = 2000;
    public static final String RESTART_SINGLE_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Single Scan";
    private static final int SAVED_NETWORK_EVALUATOR_PRIORITY = 1;
    private static final boolean SCAN_IMMEDIATELY = true;
    private static final boolean SCAN_ON_SCHEDULE = false;
    private static final int SCORED_NETWORK_EVALUATOR_PRIORITY = 3;
    private static final String TAG = "WifiConnectivityManager";
    private static final int WATCHDOG_INTERVAL_MS = 1200000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    private static int mPeriodicSingleScanInterval;
    private final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener = new AllSingleScanListener(this, null);
    private int mBand5GHzBonus;
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new HashMap();
    private final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    private final WifiConnectivityHelper mConnectivityHelper;
    private int mCurrentConnectionBonus;
    private boolean mDbg = false;
    private boolean mEnableAutoJoinWhenAssociated;
    private final Handler mEventHandler;
    private int mFullScanMaxRxRate;
    private int mFullScanMaxTxRate;
    private int mInitialScoreMax;
    private String mLastConnectionAttemptBssid = null;
    private long mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private final LocalLog mLocalLog;
    private int mMin24GHzRssi;
    private int mMin5GHzRssi;
    private int mMiracastMode = 0;
    private final WifiNetworkSelector mNetworkSelector;
    private final OpenNetworkNotifier mOpenNetworkNotifier;
    private final OnAlarmListener mPeriodicScanTimerListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.periodicScanTimerHandler();
        }
    };
    private boolean mPeriodicScanTimerSet = false;
    private final PnoScanListener mPnoScanListener = new PnoScanListener(this, null);
    private boolean mPnoScanStarted = false;
    private final OnAlarmListener mRestartScanListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.startConnectivityScan(true);
        }
    };
    private int mSameNetworkBonus;
    private int mScanRestartCount = 0;
    private final WifiScanner mScanner;
    private boolean mScreenOn = false;
    private int mSecureBonus;
    private int mSingleScanRestartCount = 0;
    private final WifiStateMachine mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited = 0;
    private boolean mUntrustedConnectionAllowed = false;
    private boolean mWaitForFullBandScanResults = false;
    private final OnAlarmListener mWatchdogListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.watchdogHandler();
        }
    };
    private boolean mWifiConnectivityManagerEnabled = true;
    private boolean mWifiEnabled = false;
    private final WifiInfo mWifiInfo;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private int mWifiState = 0;
    private boolean skipScan = false;

    private class AllSingleScanListener implements ScanListener {
        private List<ScanDetail> mScanDetails;

        /* synthetic */ AllSingleScanListener(WifiConnectivityManager this$0, AllSingleScanListener -this1) {
            this();
        }

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog("registerScanListener onFailure: reason: " + reason + " description: " + description);
        }

        public void onPeriodChanged(int periodInMs) {
        }

        public void onResults(ScanData[] results) {
            if (WifiConnectivityManager.this.mWifiEnabled && (WifiConnectivityManager.this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
                if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                    if (results[0].isAllChannelsScanned()) {
                        WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                    } else {
                        WifiConnectivityManager.this.localLog("AllSingleScanListener waiting for full band scan results.");
                        clearScanDetails();
                        return;
                    }
                }
                if (results.length > 0) {
                    WifiConnectivityManager.this.mWifiMetrics.incrementAvailableNetworksHistograms(this.mScanDetails, results[0].isAllChannelsScanned());
                }
                boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "AllSingleScanListener");
                clearScanDetails();
                if (WifiConnectivityManager.this.mPnoScanStarted) {
                    if (wasConnectAttempted) {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoBad();
                    } else {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoGood();
                    }
                }
                return;
            }
            clearScanDetails();
            WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mWifiEnabled && (WifiConnectivityManager.this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
                if (WifiConnectivityManager.this.mDbg) {
                    Log.d(WifiConnectivityManager.TAG, "AllSingleScanListener onFullResult: " + fullScanResult.SSID + " capabilities " + fullScanResult.capabilities);
                }
                if (fullScanResult.informationElements != null) {
                    this.mScanDetails.add(ScanResultUtil.toScanDetail(fullScanResult));
                }
            }
        }
    }

    private static class BssidBlacklistStatus {
        public long blacklistedTimeStamp;
        public int counter;
        public boolean isBlacklisted;

        /* synthetic */ BssidBlacklistStatus(BssidBlacklistStatus -this0) {
            this();
        }

        private BssidBlacklistStatus() {
            this.blacklistedTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
        }
    }

    private class OnSavedNetworkUpdateListener implements com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener {
        /* synthetic */ OnSavedNetworkUpdateListener(WifiConnectivityManager this$0, OnSavedNetworkUpdateListener -this1) {
            this();
        }

        private OnSavedNetworkUpdateListener() {
        }

        public void onSavedNetworkAdded(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkEnabled(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkRemoved(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkUpdated(int networkId) {
            WifiConnectivityManager.this.mStateMachine.updateCapabilities();
            updatePnoScan();
        }

        public void onSavedNetworkTemporarilyDisabled(int networkId) {
            WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
        }

        public void onSavedNetworkPermanentlyDisabled(int networkId) {
            WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
            updatePnoScan();
        }

        private void updatePnoScan() {
            if (!WifiConnectivityManager.this.mScreenOn) {
                WifiConnectivityManager.this.localLog("Saved networks updated");
                WifiConnectivityManager.this.startConnectivityScan(false);
            }
        }
    }

    private class PnoScanListener implements android.net.wifi.WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        /* synthetic */ PnoScanListener(WifiConnectivityManager this$0, PnoScanListener -this1) {
            this();
        }

        private PnoScanListener() {
            this.mScanDetails = new ArrayList();
            this.mLowRssiNetworkRetryDelay = 20000;
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
        }

        public void resetLowRssiNetworkRetryDelay() {
            this.mLowRssiNetworkRetryDelay = 20000;
        }

        public int getLowRssiNetworkRetryDelay() {
            return this.mLowRssiNetworkRetryDelay;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog("PnoScanListener onFailure: reason: " + reason + " description: " + description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get3 = wifiConnectivityManager.mScanRestartCount;
            wifiConnectivityManager.mScanRestartCount = -get3 + 1;
            if (-get3 < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(2000);
                return;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog("PnoScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            for (ScanResult result : results) {
                if (result.informationElements == null) {
                    WifiConnectivityManager.this.localLog("Skipping scan result with null information elements");
                } else if (result.informationElements != null) {
                    this.mScanDetails.add(ScanResultUtil.toScanDetail(result));
                }
            }
            boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "PnoScanListener");
            clearScanDetails();
            WifiConnectivityManager.this.mScanRestartCount = 0;
            if (wasConnectAttempted) {
                resetLowRssiNetworkRetryDelay();
                return;
            }
            if (this.mLowRssiNetworkRetryDelay > WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS) {
                this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS;
            }
            WifiConnectivityManager.this.scheduleDelayedConnectivityScan(this.mLowRssiNetworkRetryDelay);
            this.mLowRssiNetworkRetryDelay *= 2;
        }
    }

    private class RestartSingleScanListener implements OnAlarmListener {
        private final boolean mIsFullBandScan;

        RestartSingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onAlarm() {
            if (WifiConnectivityManager.this.mWifiState == 1 || WifiConnectivityManager.this.mWifiState == 2) {
                WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan, WifiStateMachine.WIFI_WORK_SOURCE);
            } else {
                Log.d(WifiConnectivityManager.TAG, "wifi connecting don't scan!");
            }
        }
    }

    private class SingleScanListener implements ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog("SingleScanListener onFailure: reason: " + reason + " description: " + description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get5 = wifiConnectivityManager.mSingleScanRestartCount;
            wifiConnectivityManager.mSingleScanRestartCount = -get5 + 1;
            if (-get5 < 5) {
                WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
                return;
            }
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start single scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog("SingleScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }
    }

    public static void setPeriodicScanIntervalMs(boolean enable) {
        if (enable) {
            mPeriodicSingleScanInterval = 90000;
            Log.d(TAG, "Set mPeriodicSingleScanInterval = " + mPeriodicSingleScanInterval);
            return;
        }
        mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        Log.d(TAG, "Set mPeriodicSingleScanInterval = " + mPeriodicSingleScanInterval);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDbg = true;
        } else {
            this.mDbg = false;
        }
    }

    private boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName) {
        refreshBssidBlacklist();
        if (this.mStateMachine.isLinkDebouncing() || this.mStateMachine.isSupplicantTransientState()) {
            localLog(listenerName + " onResults: No network selection because linkDebouncing is " + this.mStateMachine.isLinkDebouncing() + " and supplicantTransient is " + this.mStateMachine.isSupplicantTransientState());
            return false;
        }
        localLog(listenerName + " onResults: start network selection");
        WifiConfiguration candidate = this.mNetworkSelector.selectNetwork(scanDetails, buildBssidBlacklist(), this.mWifiInfo, this.mStateMachine.isConnected(), this.mStateMachine.isDisconnected(), this.mUntrustedConnectionAllowed);
        this.mWifiLastResortWatchdog.updateAvailableNetworks(this.mNetworkSelector.getConnectableScanDetails());
        this.mWifiMetrics.countScanResults(scanDetails);
        if (candidate != null) {
            localLog(listenerName + ":  WNS candidate-" + candidate.SSID);
            connectToNetwork(candidate);
            return true;
        }
        if (this.mWifiState == 2) {
            this.mOpenNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForOpenUnsavedNetworks());
        }
        return false;
    }

    WifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        this.mStateMachine = stateMachine;
        this.mScanner = scanner;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mNetworkSelector = networkSelector;
        this.mConnectivityHelper = connectivityHelper;
        this.mLocalLog = localLog;
        this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
        this.mOpenNetworkNotifier = openNetworkNotifier;
        this.mWifiMetrics = wifiMetrics;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        this.mConnectionAttemptTimeStamps = new LinkedList();
        this.mMin5GHzRssi = context.getResources().getInteger(17694907);
        this.mMin24GHzRssi = context.getResources().getInteger(17694906);
        this.mBand5GHzBonus = context.getResources().getInteger(17694875);
        this.mCurrentConnectionBonus = context.getResources().getInteger(17694892);
        this.mSameNetworkBonus = context.getResources().getInteger(17694882);
        this.mSecureBonus = context.getResources().getInteger(17694883);
        int thresholdSaturatedRssi24 = context.getResources().getInteger(17694910);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957070);
        this.mInitialScoreMax = (context.getResources().getInteger(17694910) + context.getResources().getInteger(17694880)) * context.getResources().getInteger(17694881);
        this.mFullScanMaxTxRate = context.getResources().getInteger(17694895);
        this.mFullScanMaxRxRate = context.getResources().getInteger(17694894);
        localLog("PNO settings: min5GHzRssi " + this.mMin5GHzRssi + " min24GHzRssi " + this.mMin24GHzRssi + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + this.mInitialScoreMax);
        boolean hs2Enabled = context.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint");
        localLog("Passpoint is: " + (hs2Enabled ? "enabled" : "disabled"));
        this.mNetworkSelector.registerNetworkEvaluator(savedNetworkEvaluator, 1);
        if (hs2Enabled) {
            this.mNetworkSelector.registerNetworkEvaluator(passpointNetworkEvaluator, 2);
        }
        this.mNetworkSelector.registerNetworkEvaluator(scoredNetworkEvaluator, 3);
        this.mScanner.registerScanListener(this.mAllSingleScanListener);
        this.mConfigManager.setOnSavedNetworkUpdateListener(new OnSavedNetworkUpdateListener(this, null));
        this.mWifiConnectivityManagerEnabled = enable;
        localLog("ConnectivityScanManager initialized and " + (enable ? "enabled" : "disabled"));
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiConnectivityManager.this.skipScan = true;
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiConnectivityManager.this.skipScan = false;
            }
        }, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
    }

    private boolean shouldSkipConnectionAttempt(Long timeMillis) {
        Iterator<Long> attemptIter = this.mConnectionAttemptTimeStamps.iterator();
        while (attemptIter.hasNext()) {
            if (timeMillis.longValue() - ((Long) attemptIter.next()).longValue() <= 240000) {
                break;
            }
            attemptIter.remove();
        }
        return this.mConnectionAttemptTimeStamps.size() >= 6;
    }

    private void noteConnectionAttempt(Long timeMillis) {
        this.mConnectionAttemptTimeStamps.addLast(timeMillis);
    }

    private void clearConnectionAttemptTimeStamps() {
        this.mConnectionAttemptTimeStamps.clear();
    }

    private void connectToNetwork(WifiConfiguration candidate) {
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        if (scanResultCandidate == null) {
            localLog("connectToNetwork: bad candidate - " + candidate + " scanResult: " + scanResultCandidate);
            return;
        }
        String targetBssid = scanResultCandidate.BSSID;
        String targetAssociationId = candidate.SSID + " : " + targetBssid;
        if (targetBssid != null && ((targetBssid.equals(this.mLastConnectionAttemptBssid) || targetBssid.equals(this.mWifiInfo.getBSSID())) && SupplicantState.isConnecting(this.mWifiInfo.getSupplicantState()))) {
            localLog("connectToNetwork: Either already connected or is connecting to " + targetAssociationId);
        } else if (candidate.BSSID == null || (candidate.BSSID.equals("any") ^ 1) == 0 || (candidate.BSSID.equals(targetBssid) ^ 1) == 0) {
            long elapsedTimeMillis = this.mClock.getElapsedSinceBootMillis();
            if (this.mScreenOn || !shouldSkipConnectionAttempt(Long.valueOf(elapsedTimeMillis))) {
                String currentAssociationId;
                noteConnectionAttempt(Long.valueOf(elapsedTimeMillis));
                this.mLastConnectionAttemptBssid = targetBssid;
                WifiConfiguration currentConnectedNetwork = this.mConfigManager.getConfiguredNetwork(this.mWifiInfo.getNetworkId());
                if (currentConnectedNetwork == null) {
                    currentAssociationId = "Disconnected";
                } else {
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + this.mWifiInfo.getBSSID();
                }
                if (currentConnectedNetwork == null || currentConnectedNetwork.networkId != candidate.networkId) {
                    if (this.mConnectivityHelper.isFirmwareRoamingSupported() && (candidate.BSSID == null || candidate.BSSID.equals("any"))) {
                        targetBssid = "any";
                        localLog("connectToNetwork: Connect to " + candidate.SSID + ":" + targetBssid + " from " + currentAssociationId);
                    } else {
                        localLog("connectToNetwork: Connect to " + targetAssociationId + " from " + currentAssociationId);
                    }
                    this.mStateMachine.startConnectToNetwork(candidate.networkId, 1010, targetBssid);
                } else if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                    localLog("connectToNetwork: Roaming candidate - " + targetAssociationId + "." + " The actual roaming target is up to the firmware.");
                } else {
                    localLog("connectToNetwork: Roaming to " + targetAssociationId + " from " + currentAssociationId);
                    this.mStateMachine.startRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
                return;
            }
            localLog("connectToNetwork: Too many connection attempts. Skipping this attempt!");
            this.mTotalConnectivityAttemptsRateLimited++;
        } else {
            localLog("connecToNetwork: target BSSID " + targetBssid + " does not match the " + "config specified BSSID " + candidate.BSSID + ". Drop it!");
        }
    }

    private int getScanBand() {
        return getScanBand(true);
    }

    private int getScanBand(boolean isFullBandScan) {
        if (isFullBandScan) {
            return 7;
        }
        return 0;
    }

    private boolean setScanChannels(ScanSettings settings) {
        WifiConfiguration config = this.mStateMachine.getCurrentWifiConfiguration();
        if (config == null) {
            return false;
        }
        Set<Integer> freqs = this.mConfigManager.fetchChannelSetForNetworkForPartialScan(config.networkId, 3600000, this.mWifiInfo.getFrequency());
        if (freqs == null || freqs.size() == 0) {
            localLog("No scan channels for " + config.configKey() + ". Perform full band scan");
            return false;
        }
        int index = 0;
        settings.channels = new ChannelSpec[freqs.size()];
        for (Integer freq : freqs) {
            int index2 = index + 1;
            settings.channels[index] = new ChannelSpec(freq.intValue());
            index = index2;
        }
        return true;
    }

    private void watchdogHandler() {
        if (this.mWifiState == 2) {
            localLog("start a single scan from watchdogHandler");
            scheduleWatchdogTimer();
            startSingleScan(true, WifiStateMachine.WIFI_WORK_SOURCE);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
        if (this.mLastPeriodicSingleScanTimeStamp != RESET_TIME_STAMP) {
            long msSinceLastScan = currentTimeStamp - this.mLastPeriodicSingleScanTimeStamp;
            if (msSinceLastScan < 15000) {
                localLog("Last periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                schedulePeriodicScanTimer(mPeriodicSingleScanInterval - ((int) msSinceLastScan));
                return;
            }
        }
        boolean isFullBandScan = true;
        if (this.mWifiState == 1 && (this.mWifiInfo.txSuccessRate > ((double) this.mFullScanMaxTxRate) || this.mWifiInfo.rxSuccessRate > ((double) this.mFullScanMaxRxRate))) {
            localLog("No full band scan due to ongoing traffic");
            isFullBandScan = false;
        }
        this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
        if (this.mWifiState != 1 || (this.mWifiInfo.txSuccessRate <= 40.0d && this.mWifiInfo.rxSuccessRate <= 80.0d)) {
            startSingleScan(isFullBandScan, WifiStateMachine.WIFI_WORK_SOURCE);
        } else {
            Log.e(TAG, "Ignore scan due to heavy traffic");
        }
        schedulePeriodicScanTimer(mPeriodicSingleScanInterval);
        if (mPeriodicSingleScanInterval > 160000) {
            mPeriodicSingleScanInterval = 160000;
        }
    }

    private void resetLastPeriodicSingleScanTimeStamp() {
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    }

    private void periodicScanTimerHandler() {
        localLog("periodicScanTimerHandler");
        if (this.mScreenOn) {
            startPeriodicSingleScan();
        }
    }

    private void startSingleScan(boolean isFullBandScan, WorkSource workSource) {
        if (!this.mWifiEnabled || (this.mWifiConnectivityManagerEnabled ^ 1) != 0) {
            return;
        }
        if (WifiStateMachine.WIFI_WORK_SOURCE.equals(workSource) && (this.mMiracastMode == 1 || this.mMiracastMode == 2)) {
            Log.d(TAG, "ignore connectivity scan, MiracastMode:" + this.mMiracastMode);
            return;
        }
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        ScanSettings settings = new ScanSettings();
        if (!(isFullBandScan || setScanChannels(settings))) {
            isFullBandScan = true;
        }
        settings.band = getScanBand(isFullBandScan);
        settings.reportEvents = 3;
        settings.numBssidsPerScan = 0;
        List<HiddenNetwork> hiddenNetworkList = this.mConfigManager.retrieveHiddenNetworkList();
        settings.hiddenNetworks = (HiddenNetwork[]) hiddenNetworkList.toArray(new HiddenNetwork[hiddenNetworkList.size()]);
        this.mScanner.startScan(settings, new SingleScanListener(isFullBandScan), workSource);
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || (this.mEnableAutoJoinWhenAssociated ^ 1) == 0) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            startPeriodicSingleScan();
        }
    }

    private void startDisconnectedPnoScan() {
        PnoSettings pnoSettings = new PnoSettings();
        List<PnoNetwork> pnoNetworkList = this.mConfigManager.retrievePnoNetworkList();
        int listSize = pnoNetworkList.size();
        if (listSize == 0) {
            localLog("No saved network for starting disconnected PNO.");
            return;
        }
        pnoSettings.networkList = new PnoNetwork[listSize];
        pnoSettings.networkList = (PnoNetwork[]) pnoNetworkList.toArray(pnoSettings.networkList);
        pnoSettings.min5GHzRssi = this.mMin5GHzRssi;
        pnoSettings.min24GHzRssi = this.mMin24GHzRssi;
        pnoSettings.initialScoreMax = this.mInitialScoreMax;
        pnoSettings.currentConnectionBonus = this.mCurrentConnectionBonus;
        pnoSettings.sameNetworkBonus = this.mSameNetworkBonus;
        pnoSettings.secureBonus = this.mSecureBonus;
        pnoSettings.band5GHzBonus = this.mBand5GHzBonus;
        ScanSettings scanSettings = new ScanSettings();
        scanSettings.band = getScanBand();
        scanSettings.reportEvents = 4;
        scanSettings.numBssidsPerScan = 0;
        scanSettings.periodInMs = 20000;
        this.mPnoScanListener.clearScanDetails();
        this.mScanner.startDisconnectedPnoScan(scanSettings, pnoSettings, this.mPnoScanListener);
        this.mPnoScanStarted = true;
    }

    private void stopPnoScan() {
        if (this.mPnoScanStarted) {
            this.mScanner.stopPnoScan(this.mPnoScanListener);
        }
        this.mPnoScanStarted = false;
    }

    private void scheduleWatchdogTimer() {
        localLog("scheduleWatchdogTimer");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 1200000, WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
    }

    private void schedulePeriodicScanTimer(int intervalMs) {
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) intervalMs), PERIODIC_SCAN_TIMER_TAG, this.mPeriodicScanTimerListener, this.mEventHandler);
        this.mPeriodicScanTimerSet = true;
    }

    private void cancelPeriodicScanTimer() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
    }

    private void scheduleDelayedSingleScan(boolean isFullBandScan) {
        localLog("scheduleDelayedSingleScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 2000, RESTART_SINGLE_SCAN_TIMER_TAG, new RestartSingleScanListener(isFullBandScan), this.mEventHandler);
    }

    private void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) msFromNow), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
    }

    private void startConnectivityScan(boolean scanImmediately) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + stateToString(this.mWifiState) + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && (this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
            stopConnectivityScan();
            if (this.mWifiState == 1 || this.mWifiState == 2) {
                if (this.mScreenOn) {
                    startPeriodicScan(scanImmediately);
                } else if (!(this.mWifiState != 2 || (this.mPnoScanStarted ^ 1) == 0 || (this.mStateMachine.isNetworkConnecting() ^ 1) == 0)) {
                    startDisconnectedPnoScan();
                }
            }
        }
    }

    private void stopConnectivityScan() {
        cancelPeriodicScanTimer();
        stopPnoScan();
        this.mScanRestartCount = 0;
    }

    public void handleScreenStateChanged(boolean screenOn) {
        localLog("handleScreenStateChanged: screenOn=" + screenOn);
        this.mScreenOn = screenOn;
        this.mOpenNetworkNotifier.handleScreenStateChanged(screenOn);
        startConnectivityScan(false);
    }

    public void saveMiracastMode(int mode) {
        Log.d(TAG, "saveMiracastMode: mode=" + mode);
        this.mMiracastMode = mode;
    }

    private static String stateToString(int state) {
        switch (state) {
            case 1:
                return "connected";
            case 2:
                return "disconnected";
            case 3:
                return "transitioning";
            default:
                return "unknown";
        }
    }

    public void handleConnectionStateChanged(int state) {
        localLog("handleConnectionStateChanged: state=" + stateToString(state));
        if (!this.skipScan) {
            this.mWifiState = state;
            if (this.mWifiState == 1) {
                this.mOpenNetworkNotifier.handleWifiConnected();
            }
            if (this.mWifiState == 2) {
                this.mLastConnectionAttemptBssid = null;
                scheduleWatchdogTimer();
                startConnectivityScan(true);
            } else {
                startConnectivityScan(false);
            }
        }
    }

    public void handleConnectionAttemptEnded(int failureCode) {
        if (failureCode != 1) {
            this.mOpenNetworkNotifier.handleConnectionFailure();
        }
    }

    public void setUntrustedConnectionAllowed(boolean allowed) {
        localLog("setUntrustedConnectionAllowed: allowed=" + allowed);
        if (this.mUntrustedConnectionAllowed != allowed) {
            this.mUntrustedConnectionAllowed = allowed;
            startConnectivityScan(true);
        }
    }

    public void setUserConnectChoice(int netId) {
        localLog("setUserConnectChoice: netId=" + netId);
        this.mNetworkSelector.setUserConnectChoice(netId);
    }

    public void prepareForForcedConnection(int netId) {
        localLog("prepareForForcedConnection: netId=" + netId);
        clearConnectionAttemptTimeStamps();
        clearBssidBlacklist();
    }

    public void forceConnectivityScan(WorkSource workSource) {
        localLog("forceConnectivityScan in request of " + workSource);
        this.mWaitForFullBandScanResults = true;
        startSingleScan(true, workSource);
    }

    private boolean updateBssidBlacklist(String bssid, boolean enable, int reasonCode) {
        boolean z = true;
        if (enable) {
            if (this.mBssidBlacklist.remove(bssid) == null) {
                z = false;
            }
            return z;
        }
        BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
        if (status == null) {
            status = new BssidBlacklistStatus();
            this.mBssidBlacklist.put(bssid, status);
        }
        status.blacklistedTimeStamp = this.mClock.getElapsedSinceBootMillis();
        status.counter++;
        if (status.isBlacklisted || (status.counter < 3 && reasonCode != 17)) {
            return false;
        }
        status.isBlacklisted = true;
        return true;
    }

    public boolean trackBssid(String bssid, boolean enable, int reasonCode) {
        localLog("trackBssid: " + (enable ? "enable " : "disable ") + bssid + " reason code " + reasonCode);
        if (bssid == null || !updateBssidBlacklist(bssid, enable, reasonCode)) {
            return false;
        }
        updateFirmwareRoamingConfiguration();
        if (!enable) {
            startConnectivityScan(true);
        }
        return true;
    }

    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
        return status == null ? false : status.isBlacklisted;
    }

    private HashSet<String> buildBssidBlacklist() {
        HashSet<String> blacklistedBssids = new HashSet();
        for (String bssid : this.mBssidBlacklist.keySet()) {
            if (isBssidDisabled(bssid)) {
                blacklistedBssids.add(bssid);
            }
        }
        return blacklistedBssids;
    }

    private void updateFirmwareRoamingConfiguration() {
        if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
            int maxBlacklistSize = this.mConnectivityHelper.getMaxNumBlacklistBssid();
            if (maxBlacklistSize <= 0) {
                Log.wtf(TAG, "Invalid max BSSID blacklist size:  " + maxBlacklistSize);
                return;
            }
            ArrayList<String> blacklistedBssids = new ArrayList(buildBssidBlacklist());
            int blacklistSize = blacklistedBssids.size();
            if (blacklistSize > maxBlacklistSize) {
                Log.wtf(TAG, "Attempt to write " + blacklistSize + " blacklisted BSSIDs, max size is " + maxBlacklistSize);
                ArrayList<String> blacklistedBssids2 = new ArrayList(blacklistedBssids.subList(0, maxBlacklistSize));
                localLog("Trim down BSSID blacklist size from " + blacklistSize + " to " + blacklistedBssids2.size());
                blacklistedBssids = blacklistedBssids2;
            }
            if (!this.mConnectivityHelper.setFirmwareRoamingConfiguration(blacklistedBssids, new ArrayList())) {
                localLog("Failed to set firmware roaming configuration.");
            }
        }
    }

    private void refreshBssidBlacklist() {
        if (!this.mBssidBlacklist.isEmpty()) {
            boolean updated = false;
            Iterator<BssidBlacklistStatus> iter = this.mBssidBlacklist.values().iterator();
            Long currentTimeStamp = Long.valueOf(this.mClock.getElapsedSinceBootMillis());
            while (iter.hasNext()) {
                BssidBlacklistStatus status = (BssidBlacklistStatus) iter.next();
                if (status.isBlacklisted && currentTimeStamp.longValue() - status.blacklistedTimeStamp >= 300000) {
                    iter.remove();
                    updated = true;
                }
            }
            if (updated) {
                updateFirmwareRoamingConfiguration();
            }
        }
    }

    private void clearBssidBlacklist() {
        this.mBssidBlacklist.clear();
        updateFirmwareRoamingConfiguration();
    }

    private void start() {
        this.mConnectivityHelper.getFirmwareRoamingInfo();
        clearBssidBlacklist();
        startConnectivityScan(true);
    }

    private void stop() {
        stopConnectivityScan();
        clearBssidBlacklist();
        resetLastPeriodicSingleScanTimeStamp();
        this.mOpenNetworkNotifier.clearPendingNotification(true);
        this.mLastConnectionAttemptBssid = null;
        this.mWaitForFullBandScanResults = false;
    }

    private void updateRunningState() {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            localLog("Starting up WifiConnectivityManager");
            start();
            return;
        }
        localLog("Stopping WifiConnectivityManager");
        stop();
    }

    public void setWifiEnabled(boolean enable) {
        localLog("Set WiFi " + (enable ? "enabled" : "disabled"));
        this.mWifiEnabled = enable;
        updateRunningState();
    }

    public void enable(boolean enable) {
        localLog("Set WiFiConnectivityManager " + (enable ? "enabled" : "disabled"));
        this.mWifiConnectivityManagerEnabled = enable;
        updateRunningState();
    }

    int getLowRssiNetworkRetryDelay() {
        return this.mPnoScanListener.getLowRssiNetworkRetryDelay();
    }

    long getLastPeriodicSingleScanTimeStamp() {
        return this.mLastPeriodicSingleScanTimeStamp;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConnectivityManager");
        pw.println("WifiConnectivityManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConnectivityManager - Log End ----");
        this.mOpenNetworkNotifier.dump(fd, pw, args);
    }

    public void tryStartConnectivityScan() {
        startConnectivityScan(false);
    }
}
