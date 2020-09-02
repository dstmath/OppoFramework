package com.android.server.wifi;

import android.app.AlarmManager;
import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.ScanResultUtil;
import com.mediatek.server.wifi.MtkWifiServiceAdapter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.ksoap2.transport.ServiceConnection;

public class WifiConnectivityManager {
    @VisibleForTesting
    public static final int BSSID_BLACKLIST_EXPIRE_TIME_MS = 300000;
    @VisibleForTesting
    public static final int BSSID_BLACKLIST_THRESHOLD = 3;
    private static final int CHANNEL_LIST_AGE_MS = 3600000;
    private static final int CONNECTED_PNO_SCAN_INTERVAL_MS = 160000;
    private static final int LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS = 80000;
    private static final int LOW_RSSI_NETWORK_RETRY_START_DELAY_MS = 20000;
    public static final int MAX_CONNECTION_ATTEMPTS_RATE = 6;
    public static final int MAX_CONNECTION_ATTEMPTS_TIME_INTERVAL_MS = 240000;
    @VisibleForTesting
    public static final int MAX_PERIODIC_SCAN_INTERVAL_MS = 160000;
    @VisibleForTesting
    public static final int MAX_SCAN_RESTART_ALLOWED = 5;
    @VisibleForTesting
    static final int MOVING_PNO_SCAN_INTERVAL_MS = 20000;
    private static final int P2P_HIGH_TRAFFIC_MODE = 2;
    private static final int P2P_HIGH_TRAFFIC_PERIODIC_SCAN_INTERVAL = 44000;
    private static final int P2P_LOW_TRAFFIC_MODE = 1;
    private static final int P2P_OSHARE_TRAFFIC_MODE = 3;
    private static final int P2P_OSHARE_TRAFFIC_PERIODIC_SCAN_INTERVAL = 89000;
    private static final int P2P_TRAFFIC_DETECT_INTERVAL = 5000;
    private static final int P2P_TRAFFIC_RX_LOWEST = 250;
    private static final int P2P_TRAFFIC_TX_LOWEST = 250;
    private static final int P2P_WFD_TRAFFIC_MODE = 4;
    private static final int P2P_ZERO_TRAFFIC_MODE = 0;
    @VisibleForTesting
    public static final int PERIODIC_SCAN_INTERVAL_MS = 15000;
    public static final String PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Periodic Scan Timer";
    @VisibleForTesting
    public static final int REASON_CODE_AP_UNABLE_TO_HANDLE_NEW_STA = 17;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    public static final String RESTART_CONNECTIVITY_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Scan";
    private static final int RESTART_SCAN_DELAY_MS = 2000;
    public static final String RESTART_SINGLE_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Single Scan";
    private static final boolean SCAN_IMMEDIATELY = true;
    private static final boolean SCAN_ON_SCHEDULE = false;
    @VisibleForTesting
    static final int STATIONARY_PNO_SCAN_INTERVAL_MS = 60000;
    private static final String TAG = "WifiConnectivityManager";
    private static final int WATCHDOG_INTERVAL_MS = 1200000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    /* access modifiers changed from: private */
    public static boolean mOshareTrafficEnable = false;
    private static int mPeriodicSingleScanInterval;
    private final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener = new AllSingleScanListener();
    private int mBand5GHzBonus;
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new ConcurrentHashMap();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final CarrierNetworkNotifier mCarrierNetworkNotifier;
    /* access modifiers changed from: private */
    public final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    /* access modifiers changed from: private */
    public final WifiConnectivityHelper mConnectivityHelper;
    private int mCurrentConnectionBonus;
    boolean mDbg = false;
    private boolean mEnableAutoJoinWhenAssociated;
    private final Handler mEventHandler;
    private int mFullScanMaxRxRate;
    private int mFullScanMaxTxRate;
    private String mLastConnectionAttemptBssid = null;
    private long mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private final LocalLog mLocalLog;
    /* access modifiers changed from: private */
    public int mMiracastMode = 0;
    private final WifiNetworkSelector mNetworkSelector;
    private final OpenNetworkNotifier mOpenNetworkNotifier;
    public boolean mP2pConnectedStatus = false;
    public P2pTrafficModeInfo mP2pCurTrafficModeInfo = new P2pTrafficModeInfo();
    public long mP2pHighTrafficPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    public long mP2pOshareTrafficPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    public P2pTrafficModeInfo mP2pPreTrafficModeInfo = new P2pTrafficModeInfo();
    public boolean mP2pTrafficModeDetectFlag = false;
    private final AlarmManager.OnAlarmListener mPeriodicScanTimerListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass4 */

        public void onAlarm() {
            WifiConnectivityManager.this.periodicScanTimerHandler();
        }
    };
    private boolean mPeriodicScanTimerSet = false;
    private int mPnoScanIntervalMs;
    private final PnoScanListener mPnoScanListener = new PnoScanListener();
    /* access modifiers changed from: private */
    public boolean mPnoScanStarted = false;
    private final AlarmManager.OnAlarmListener mRestartScanListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass2 */

        public void onAlarm() {
            WifiConnectivityManager.this.startConnectivityScan(true);
        }
    };
    private int mRssiScoreOffset;
    private int mRssiScoreSlope;
    private boolean mRunning = false;
    private int mSameNetworkBonus;
    /* access modifiers changed from: private */
    public int mScanRestartCount = 0;
    private WifiScanner mScanner;
    private final ScoringParams mScoringParams;
    /* access modifiers changed from: private */
    public boolean mScreenOn = false;
    private int mSecureBonus;
    /* access modifiers changed from: private */
    public int mSingleScanRestartCount = 0;
    private boolean mSpecificNetworkRequestInProgress = false;
    /* access modifiers changed from: private */
    public final ClientModeImpl mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited = 0;
    private boolean mTrustedConnectionAllowed = false;
    private boolean mUntrustedConnectionAllowed = false;
    /* access modifiers changed from: private */
    public boolean mUseSingleRadioChainScanResults = false;
    /* access modifiers changed from: private */
    public boolean mWaitForFullBandScanResults = false;
    private final AlarmManager.OnAlarmListener mWatchdogListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass3 */

        public void onAlarm() {
            WifiConnectivityManager.this.watchdogHandler();
        }
    };
    /* access modifiers changed from: private */
    public boolean mWifiConnectivityManagerEnabled = false;
    /* access modifiers changed from: private */
    public boolean mWifiEnabled = false;
    private final WifiInfo mWifiInfo;
    private final WifiInjector mWifiInjector;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;
    /* access modifiers changed from: private */
    public int mWifiState = 0;

    static /* synthetic */ int access$1708(WifiConnectivityManager x0) {
        int i = x0.mSingleScanRestartCount;
        x0.mSingleScanRestartCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1908(WifiConnectivityManager x0) {
        int i = x0.mScanRestartCount;
        x0.mScanRestartCount = i + 1;
        return i;
    }

    private static class BssidBlacklistStatus {
        public long blacklistedTimeStamp;
        public int counter;
        public boolean isBlacklisted;

        private BssidBlacklistStatus() {
            this.blacklistedTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
        }
    }

    public class P2pTrafficModeInfo {
        public long mRxPkts = 0;
        public int mTrafficMode = 1;
        public long mTxPkts = 0;

        public P2pTrafficModeInfo() {
        }
    }

    private void P2pTrafficModeDetectThread() {
        this.mP2pTrafficModeDetectFlag = true;
        new Thread() {
            /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass1 */

            public void run() {
                super.run();
                while (WifiConnectivityManager.this.mP2pTrafficModeDetectFlag) {
                    long currentTimeStamp = WifiConnectivityManager.this.mClock.getElapsedSinceBootMillis();
                    WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mTrafficMode = WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode;
                    if (WifiConnectivityManager.this.mMiracastMode == 1 || WifiConnectivityManager.this.mMiracastMode == 2) {
                        WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode = 4;
                    } else if (WifiConnectivityManager.mOshareTrafficEnable) {
                        WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode = 3;
                    } else {
                        P2pTrafficModeInfo trafficdiff = new P2pTrafficModeInfo();
                        String p2pIfaceName = WifiInjector.getInstance().getWifiP2pNative().getP2pInterfaceName();
                        if (TextUtils.isEmpty(p2pIfaceName)) {
                            Log.d(WifiConnectivityManager.TAG, "could not get client interface name for checking network access!");
                            WifiConnectivityManager.this.mP2pTrafficModeDetectFlag = false;
                            return;
                        }
                        WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTxPkts = TrafficStats.getTxPackets(p2pIfaceName);
                        WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mRxPkts = TrafficStats.getRxPackets(p2pIfaceName);
                        trafficdiff.mTxPkts = WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTxPkts - WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mTxPkts;
                        trafficdiff.mRxPkts = WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mRxPkts - WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mRxPkts;
                        WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mTxPkts = WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTxPkts;
                        WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mRxPkts = WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mRxPkts;
                        if (trafficdiff.mTxPkts > 250 || trafficdiff.mRxPkts > 250) {
                            WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode = 2;
                        } else {
                            WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode = 1;
                        }
                        Log.d(WifiConnectivityManager.TAG, " txPkts = " + trafficdiff.mTxPkts + " rxPkts = " + trafficdiff.mRxPkts);
                    }
                    Log.d(WifiConnectivityManager.TAG, "P2pCurTrafficMode = " + WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode);
                    if (WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode == 3) {
                        if (WifiConnectivityManager.this.mP2pOshareTrafficPeriodicSingleScanTimeStamp == WifiConnectivityManager.RESET_TIME_STAMP) {
                            Log.d(WifiConnectivityManager.TAG, "oshare traffic mode, init mP2pOshareTrafficPeriodicSingleScanTimeStamp!");
                            WifiConnectivityManager.this.mP2pOshareTrafficPeriodicSingleScanTimeStamp = currentTimeStamp;
                        }
                    } else if (WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode != 2) {
                        WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                        wifiConnectivityManager.mP2pOshareTrafficPeriodicSingleScanTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
                        wifiConnectivityManager.mP2pHighTrafficPeriodicSingleScanTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
                    } else if (WifiConnectivityManager.this.mP2pHighTrafficPeriodicSingleScanTimeStamp == WifiConnectivityManager.RESET_TIME_STAMP) {
                        Log.d(WifiConnectivityManager.TAG, "p2p high traffic mode, init mP2pHighTrafficPeriodicSingleScanTimeStamp!");
                        WifiConnectivityManager.this.mP2pHighTrafficPeriodicSingleScanTimeStamp = currentTimeStamp;
                    }
                    try {
                        sleep(RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                WifiConnectivityManager wifiConnectivityManager2 = WifiConnectivityManager.this;
                wifiConnectivityManager2.mP2pOshareTrafficPeriodicSingleScanTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
                wifiConnectivityManager2.mP2pHighTrafficPeriodicSingleScanTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
                wifiConnectivityManager2.mP2pPreTrafficModeInfo.mTrafficMode = 1;
                WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTrafficMode = 1;
                WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mTxPkts = 0;
                WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mTxPkts = 0;
                WifiConnectivityManager.this.mP2pPreTrafficModeInfo.mRxPkts = 0;
                WifiConnectivityManager.this.mP2pCurTrafficModeInfo.mRxPkts = 0;
                Log.d(WifiConnectivityManager.TAG, "P2pTrafficModeDetectThread break.");
            }
        }.start();
    }

    public void saveP2pConnectedStatus(boolean status) {
        Log.d(TAG, "saveP2pConnectedStatus: mP2pConnectedStatus=" + status);
        this.mP2pConnectedStatus = status;
        if (!this.mP2pConnectedStatus) {
            this.mP2pTrafficModeDetectFlag = false;
            this.mP2pOshareTrafficPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
            this.mP2pHighTrafficPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
            P2pTrafficModeInfo p2pTrafficModeInfo = this.mP2pPreTrafficModeInfo;
            p2pTrafficModeInfo.mTrafficMode = 1;
            P2pTrafficModeInfo p2pTrafficModeInfo2 = this.mP2pCurTrafficModeInfo;
            p2pTrafficModeInfo2.mTrafficMode = 1;
            p2pTrafficModeInfo.mTxPkts = 0;
            p2pTrafficModeInfo2.mTxPkts = 0;
            p2pTrafficModeInfo.mRxPkts = 0;
            p2pTrafficModeInfo2.mRxPkts = 0;
        } else if (!this.mP2pTrafficModeDetectFlag) {
            P2pTrafficModeDetectThread();
        }
    }

    public static void setPeriodicScanIntervalMs(boolean enable) {
        mOshareTrafficEnable = enable;
    }

    /* access modifiers changed from: private */
    public void localLog(String log) {
        if (this.mDbg) {
            this.mLocalLog.log(log);
            Log.d(TAG, log);
        }
    }

    private class RestartSingleScanListener implements AlarmManager.OnAlarmListener {
        private final boolean mIsFullBandScan;

        RestartSingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onAlarm() {
            if (WifiConnectivityManager.this.mWifiState == 1 || WifiConnectivityManager.this.mWifiState == 2) {
                WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan, ClientModeImpl.WIFI_WORK_SOURCE);
            } else {
                Log.d(WifiConnectivityManager.TAG, "wifi connecting don't scan!");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName) {
        refreshBssidBlacklist();
        if (this.mStateMachine.isSupplicantTransientState()) {
            localLog(listenerName + " onResults: No network selection because supplicantTransientState is " + this.mStateMachine.isSupplicantTransientState());
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
            if (this.mCarrierNetworkConfig.isCarrierEncryptionInfoAvailable()) {
                this.mCarrierNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForCarrierUnsavedNetworks(this.mCarrierNetworkConfig));
            }
            MtkWifiServiceAdapter.handleScanResults(scanDetails, this.mNetworkSelector.getFilteredScanDetailsForOpenUnsavedNetworks());
        }
        return false;
    }

    private class AllSingleScanListener implements WifiScanner.ScanListener {
        private int mNumScanResultsIgnoredDueToSingleRadioChain;
        private List<ScanDetail> mScanDetails;

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog("registerScanListener onFailure: reason: " + reason + " description: " + description);
        }

        public void onPeriodChanged(int periodInMs) {
        }

        public void onResults(WifiScanner.ScanData[] results) {
            if (!WifiConnectivityManager.this.mWifiEnabled || !WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                clearScanDetails();
                boolean unused = WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                return;
            }
            boolean isFullBandScanResults = results[0].getBandScanned() == 7 || results[0].getBandScanned() == 3;
            if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                if (!isFullBandScanResults) {
                    WifiConnectivityManager.this.localLog("AllSingleScanListener waiting for full band scan results.");
                    clearScanDetails();
                    return;
                }
                boolean unused2 = WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
            }
            if (results.length > 0) {
                WifiConnectivityManager.this.mWifiMetrics.incrementAvailableNetworksHistograms(this.mScanDetails, isFullBandScanResults);
            }
            if (this.mNumScanResultsIgnoredDueToSingleRadioChain > 0) {
                Log.i(WifiConnectivityManager.TAG, "Number of scan results ignored due to single radio chain scan: " + this.mNumScanResultsIgnoredDueToSingleRadioChain);
            }
            boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "AllSingleScanListener");
            clearScanDetails();
            if (!WifiConnectivityManager.this.mPnoScanStarted) {
                return;
            }
            if (wasConnectAttempted) {
                WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoBad();
            } else {
                WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoGood();
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mWifiEnabled && WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                if (WifiConnectivityManager.this.mUseSingleRadioChainScanResults || fullScanResult.radioChainInfos == null || fullScanResult.radioChainInfos.length != 1) {
                    this.mScanDetails.add(ScanResultUtil.toScanDetail(fullScanResult));
                } else {
                    this.mNumScanResultsIgnoredDueToSingleRadioChain++;
                }
            }
        }
    }

    private class SingleScanListener implements WifiScanner.ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog("SingleScanListener onFailure: reason: " + reason + " description: " + description);
            if (WifiConnectivityManager.access$1708(WifiConnectivityManager.this) < 5) {
                WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
                return;
            }
            int unused = WifiConnectivityManager.this.mSingleScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start single scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog("SingleScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(WifiScanner.ScanData[] results) {
            int unused = WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }

        public void onFullResult(ScanResult fullScanResult) {
        }
    }

    private class PnoScanListener implements WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        private PnoScanListener() {
            this.mScanDetails = new ArrayList();
            this.mLowRssiNetworkRetryDelay = ServiceConnection.DEFAULT_TIMEOUT;
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
        }

        public void resetLowRssiNetworkRetryDelay() {
            this.mLowRssiNetworkRetryDelay = ServiceConnection.DEFAULT_TIMEOUT;
        }

        @VisibleForTesting
        public int getLowRssiNetworkRetryDelay() {
            return this.mLowRssiNetworkRetryDelay;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog("PnoScanListener onFailure: reason: " + reason + " description: " + description);
            if (WifiConnectivityManager.access$1908(WifiConnectivityManager.this) < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(2000);
                return;
            }
            int unused = WifiConnectivityManager.this.mScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog("PnoScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(WifiScanner.ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            for (ScanResult result : results) {
                if (result.informationElements == null) {
                    WifiConnectivityManager.this.localLog("Skipping scan result with null information elements");
                } else {
                    this.mScanDetails.add(ScanResultUtil.toScanDetail(result));
                }
            }
            boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "PnoScanListener");
            clearScanDetails();
            int unused = WifiConnectivityManager.this.mScanRestartCount = 0;
            if (!wasConnectAttempted) {
                if (this.mLowRssiNetworkRetryDelay > WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS) {
                    this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS;
                }
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(this.mLowRssiNetworkRetryDelay);
                this.mLowRssiNetworkRetryDelay *= 2;
                return;
            }
            resetLowRssiNetworkRetryDelay();
        }
    }

    private class OnSavedNetworkUpdateListener implements WifiConfigManager.OnSavedNetworkUpdateListener {
        private OnSavedNetworkUpdateListener() {
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkAdded(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkEnabled(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkRemoved(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkUpdated(int networkId) {
            WifiConnectivityManager.this.mStateMachine.updateCapabilities();
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkTemporarilyDisabled(int networkId, int disableReason) {
            if (disableReason != 6) {
                WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
            }
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkPermanentlyDisabled(int networkId, int disableReason) {
            if (disableReason != 10) {
                WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
                updatePnoScan();
            }
        }

        private void updatePnoScan() {
            if (!WifiConnectivityManager.this.mScreenOn) {
                WifiConnectivityManager.this.localLog("Saved networks updated");
                WifiConnectivityManager.this.startConnectivityScan(false);
            }
        }
    }

    WifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl stateMachine, WifiInjector injector, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog) {
        this.mStateMachine = stateMachine;
        this.mWifiInjector = injector;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mNetworkSelector = networkSelector;
        this.mConnectivityHelper = connectivityHelper;
        this.mLocalLog = localLog;
        this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
        this.mOpenNetworkNotifier = openNetworkNotifier;
        this.mCarrierNetworkNotifier = carrierNetworkNotifier;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
        this.mWifiMetrics = wifiMetrics;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        this.mScoringParams = scoringParams;
        this.mConnectionAttemptTimeStamps = new LinkedList<>();
        this.mBand5GHzBonus = context.getResources().getInteger(17694915);
        this.mCurrentConnectionBonus = context.getResources().getInteger(17694932);
        this.mSameNetworkBonus = context.getResources().getInteger(17694922);
        this.mSecureBonus = context.getResources().getInteger(17694923);
        this.mRssiScoreOffset = context.getResources().getInteger(17694920);
        this.mRssiScoreSlope = context.getResources().getInteger(17694921);
        this.mEnableAutoJoinWhenAssociated = false;
        this.mUseSingleRadioChainScanResults = context.getResources().getBoolean(17891586);
        this.mFullScanMaxTxRate = context.getResources().getInteger(17694935);
        this.mFullScanMaxRxRate = context.getResources().getInteger(17694934);
        this.mPnoScanIntervalMs = ServiceConnection.DEFAULT_TIMEOUT;
        localLog("PNO settings: min5GHzRssi " + this.mScoringParams.getEntryRssi(5000) + " min24GHzRssi " + this.mScoringParams.getEntryRssi(ScoringParams.BAND2) + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + initialScoreMax());
        this.mConfigManager.setOnSavedNetworkUpdateListener(new OnSavedNetworkUpdateListener());
    }

    private int initialScoreMax() {
        return this.mRssiScoreSlope * (Math.max(this.mScoringParams.getGoodRssi(ScoringParams.BAND2), this.mScoringParams.getGoodRssi(5000)) + this.mRssiScoreOffset);
    }

    private boolean shouldSkipConnectionAttempt(Long timeMillis) {
        Iterator<Long> attemptIter = this.mConnectionAttemptTimeStamps.iterator();
        while (attemptIter.hasNext() && timeMillis.longValue() - attemptIter.next().longValue() > 240000) {
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
        String currentAssociationId;
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        if (scanResultCandidate == null) {
            localLog("connectToNetwork: bad candidate - " + candidate + " scanResult: " + scanResultCandidate);
            return;
        }
        String targetBssid = scanResultCandidate.BSSID;
        String targetAssociationId = candidate.SSID + " : " + targetBssid;
        if (targetBssid != null && ((targetBssid.equals(this.mLastConnectionAttemptBssid) || targetBssid.equals(this.mWifiInfo.getBSSID())) && SupplicantState.isConnecting(this.mWifiInfo.getSupplicantState()))) {
            localLog("connectToNetwork: Either already connected or is connecting to " + targetAssociationId);
        } else if (candidate.BSSID == null || candidate.BSSID.equals("any") || candidate.BSSID.equals(targetBssid)) {
            long elapsedTimeMillis = this.mClock.getElapsedSinceBootMillis();
            if (this.mScreenOn || !shouldSkipConnectionAttempt(Long.valueOf(elapsedTimeMillis))) {
                noteConnectionAttempt(Long.valueOf(elapsedTimeMillis));
                this.mLastConnectionAttemptBssid = targetBssid;
                WifiConfiguration currentConnectedNetwork = this.mConfigManager.getConfiguredNetwork(this.mWifiInfo.getNetworkId());
                if (currentConnectedNetwork == null) {
                    currentAssociationId = "Disconnected";
                } else {
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + this.mWifiInfo.getBSSID();
                }
                if (currentConnectedNetwork == null || currentConnectedNetwork.networkId != candidate.networkId) {
                    if (!this.mConnectivityHelper.isFirmwareRoamingSupported() || (candidate.BSSID != null && !candidate.BSSID.equals("any"))) {
                        localLog("connectToNetwork: Connect to " + targetAssociationId + " from " + currentAssociationId);
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().setNetworkSuggestionConnect(candidate.BSSID, candidate.creatorUid);
                        }
                    } else {
                        targetBssid = "any";
                        localLog("connectToNetwork: Connect to " + candidate.SSID + ":" + targetBssid + " from " + currentAssociationId);
                    }
                    this.mStateMachine.startConnectToNetwork(candidate.networkId, candidate.creatorUid, targetBssid);
                } else if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                    localLog("connectToNetwork: Roaming candidate - " + targetAssociationId + ". The actual roaming target is up to the firmware.");
                } else {
                    localLog("connectToNetwork: Roaming to " + targetAssociationId + " from " + currentAssociationId);
                    this.mStateMachine.startRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
            } else {
                localLog("connectToNetwork: Too many connection attempts. Skipping this attempt!");
                this.mTotalConnectivityAttemptsRateLimited++;
            }
        } else {
            localLog("connecToNetwork: target BSSID " + targetBssid + " does not match the config specified BSSID " + candidate.BSSID + ". Drop it!");
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

    private boolean setScanChannels(WifiScanner.ScanSettings settings) {
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
        settings.channels = new WifiScanner.ChannelSpec[freqs.size()];
        for (Integer freq : freqs) {
            settings.channels[index] = new WifiScanner.ChannelSpec(freq.intValue());
            index++;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void watchdogHandler() {
        if (this.mWifiState == 2) {
            localLog("start a single scan from watchdogHandler");
            scheduleWatchdogTimer();
            startSingleScan(true, ClientModeImpl.WIFI_WORK_SOURCE);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
        long j = this.mLastPeriodicSingleScanTimeStamp;
        if (j != RESET_TIME_STAMP) {
            long msSinceLastScan = currentTimeStamp - j;
            if (msSinceLastScan < 15000) {
                localLog("Last periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                schedulePeriodicScanTimer(mPeriodicSingleScanInterval - ((int) msSinceLastScan));
                return;
            }
        }
        boolean isScanNeeded = true;
        boolean isFullBandScan = true;
        boolean isTrafficOverThreshold = this.mWifiInfo.txSuccessRate > ((double) this.mFullScanMaxTxRate) || this.mWifiInfo.rxSuccessRate > ((double) this.mFullScanMaxRxRate);
        if (this.mWifiState == 1 && isTrafficOverThreshold) {
            if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                localLog("No partial scan because firmware roaming is supported.");
                isScanNeeded = false;
            } else {
                localLog("No full band scan due to ongoing traffic");
                isFullBandScan = false;
            }
        }
        if (isScanNeeded) {
            this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
            startSingleScan(isFullBandScan, ClientModeImpl.WIFI_WORK_SOURCE);
            schedulePeriodicScanTimer(mPeriodicSingleScanInterval);
            if (mPeriodicSingleScanInterval > 160000) {
                mPeriodicSingleScanInterval = 160000;
                return;
            }
            return;
        }
        schedulePeriodicScanTimer(mPeriodicSingleScanInterval);
    }

    private void resetLastPeriodicSingleScanTimeStamp() {
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    }

    /* access modifiers changed from: private */
    public void periodicScanTimerHandler() {
        localLog("periodicScanTimerHandler");
        if (this.mScreenOn) {
            startPeriodicSingleScan();
        }
    }

    public void saveMiracastMode(int mode) {
        Log.d(TAG, "saveMiracastMode: mode=" + mode);
        this.mMiracastMode = mode;
    }

    /* access modifiers changed from: protected */
    public int getMiracastMode() {
        return this.mMiracastMode;
    }

    /* access modifiers changed from: private */
    public void startSingleScan(boolean isFullBandScan, WorkSource workSource) {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            if (this.mP2pConnectedStatus) {
                long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
                Log.d(TAG, "p2p is connected, start startSingleScan handle.");
                if (this.mP2pCurTrafficModeInfo.mTrafficMode == 4) {
                    Log.d(TAG, "ignore connectivity Single scan, wfd traffic mode, MiracastMode:" + this.mMiracastMode);
                    return;
                }
                if (this.mP2pCurTrafficModeInfo.mTrafficMode == 3) {
                    long j = this.mP2pOshareTrafficPeriodicSingleScanTimeStamp;
                    if (j != RESET_TIME_STAMP) {
                        long msSinceLastScan = currentTimeStamp - j;
                        if (msSinceLastScan < 89000) {
                            Log.d(TAG, "ignore connectivity Single scan, oshare traffic mode.");
                            return;
                        }
                        this.mP2pOshareTrafficPeriodicSingleScanTimeStamp = currentTimeStamp;
                        Log.d(TAG, "accept connectivity Single scan, oshare traffic mode, scan interval = " + msSinceLastScan);
                    }
                }
                if (this.mP2pCurTrafficModeInfo.mTrafficMode == 2) {
                    long j2 = this.mP2pHighTrafficPeriodicSingleScanTimeStamp;
                    if (j2 != RESET_TIME_STAMP) {
                        long msSinceLastScan2 = currentTimeStamp - j2;
                        if (msSinceLastScan2 < 44000) {
                            Log.d(TAG, "ignore connectivity Single scan, p2p high traffic mode");
                            return;
                        }
                        this.mP2pHighTrafficPeriodicSingleScanTimeStamp = currentTimeStamp;
                        Log.d(TAG, "accept connectivity Single scan, p2p high traffic mode, scan interval = " + msSinceLastScan2);
                    }
                }
                Log.d(TAG, "accept connectivity Single scan, p2p low traffic mode");
            }
            this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
            WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
            if (!isFullBandScan && !setScanChannels(settings)) {
                isFullBandScan = true;
            }
            settings.type = 2;
            settings.band = getScanBand(isFullBandScan);
            settings.reportEvents = 3;
            settings.numBssidsPerScan = 0;
            List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mConfigManager.retrieveHiddenNetworkList();
            settings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
            this.mScanner.startScan(settings, new SingleScanListener(isFullBandScan), workSource);
            this.mWifiMetrics.incrementConnectivityOneshotScanCount();
        }
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || this.mEnableAutoJoinWhenAssociated) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            startPeriodicSingleScan();
        }
    }

    private static int deviceMobilityStateToPnoScanIntervalMs(int state) {
        if (state == 0 || state == 1 || state == 2) {
            return ServiceConnection.DEFAULT_TIMEOUT;
        }
        if (state != 3) {
            return -1;
        }
        return 60000;
    }

    public void setDeviceMobilityState(int newState) {
        int newPnoScanIntervalMs = deviceMobilityStateToPnoScanIntervalMs(newState);
        if (newPnoScanIntervalMs < 0) {
            Log.e(TAG, "Invalid device mobility state: " + newState);
        } else if (newPnoScanIntervalMs != this.mPnoScanIntervalMs) {
            this.mPnoScanIntervalMs = newPnoScanIntervalMs;
            Log.d(TAG, "PNO Scan Interval changed to " + this.mPnoScanIntervalMs + " ms.");
            if (this.mPnoScanStarted) {
                Log.d(TAG, "Restarting PNO Scan with new scan interval");
                stopPnoScan();
                this.mWifiMetrics.enterDeviceMobilityState(newState);
                startDisconnectedPnoScan();
                return;
            }
            this.mWifiMetrics.enterDeviceMobilityState(newState);
        } else if (this.mPnoScanStarted) {
            this.mWifiMetrics.logPnoScanStop();
            this.mWifiMetrics.enterDeviceMobilityState(newState);
            this.mWifiMetrics.logPnoScanStart();
        } else {
            this.mWifiMetrics.enterDeviceMobilityState(newState);
        }
    }

    private void startDisconnectedPnoScan() {
        WifiScanner.PnoSettings pnoSettings = new WifiScanner.PnoSettings();
        List<WifiScanner.PnoSettings.PnoNetwork> pnoNetworkList = this.mConfigManager.retrievePnoNetworkList();
        int listSize = pnoNetworkList.size();
        if (listSize == 0) {
            localLog("No saved network for starting disconnected PNO.");
            return;
        }
        pnoSettings.networkList = new WifiScanner.PnoSettings.PnoNetwork[listSize];
        pnoSettings.networkList = (WifiScanner.PnoSettings.PnoNetwork[]) pnoNetworkList.toArray(pnoSettings.networkList);
        pnoSettings.min5GHzRssi = this.mScoringParams.getEntryRssi(5000);
        pnoSettings.min24GHzRssi = this.mScoringParams.getEntryRssi(ScoringParams.BAND2);
        pnoSettings.initialScoreMax = initialScoreMax();
        pnoSettings.currentConnectionBonus = this.mCurrentConnectionBonus;
        pnoSettings.sameNetworkBonus = this.mSameNetworkBonus;
        pnoSettings.secureBonus = this.mSecureBonus;
        pnoSettings.band5GHzBonus = this.mBand5GHzBonus;
        WifiScanner.ScanSettings scanSettings = new WifiScanner.ScanSettings();
        scanSettings.band = getScanBand();
        scanSettings.reportEvents = 4;
        scanSettings.numBssidsPerScan = 0;
        scanSettings.periodInMs = this.mPnoScanIntervalMs;
        this.mPnoScanListener.clearScanDetails();
        this.mScanner.startDisconnectedPnoScan(scanSettings, pnoSettings, this.mPnoScanListener);
        this.mPnoScanStarted = true;
        this.mWifiMetrics.logPnoScanStart();
    }

    public void stopPnoScan() {
        if (this.mPnoScanStarted) {
            this.mScanner.stopPnoScan(this.mPnoScanListener);
            this.mPnoScanStarted = false;
            this.mWifiMetrics.logPnoScanStop();
        }
    }

    private void scheduleWatchdogTimer() {
        localLog("scheduleWatchdogTimer");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 1200000, WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
    }

    private void schedulePeriodicScanTimer(int intervalMs) {
        localLog("schedulePeriodicScanTimer, intervalMs: " + intervalMs);
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) intervalMs), PERIODIC_SCAN_TIMER_TAG, this.mPeriodicScanTimerListener, this.mEventHandler);
        this.mPeriodicScanTimerSet = true;
    }

    private void cancelPeriodicScanTimer() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleDelayedSingleScan(boolean isFullBandScan) {
        localLog("scheduleDelayedSingleScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 2000, RESTART_SINGLE_SCAN_TIMER_TAG, new RestartSingleScanListener(isFullBandScan), this.mEventHandler);
    }

    /* access modifiers changed from: private */
    public void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) msFromNow), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
    }

    /* access modifiers changed from: private */
    public void startConnectivityScan(boolean scanImmediately) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + stateToString(this.mWifiState) + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            stopConnectivityScan();
            int i = this.mWifiState;
            if (i != 1 && i != 2) {
                return;
            }
            if (this.mScreenOn) {
                startPeriodicScan(scanImmediately);
            } else if (this.mWifiState == 2 && !this.mPnoScanStarted && !this.mStateMachine.isNetworkConnecting()) {
                startDisconnectedPnoScan();
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
        this.mCarrierNetworkNotifier.handleScreenStateChanged(screenOn);
        startConnectivityScan(false);
    }

    private static String stateToString(int state) {
        if (state == 1) {
            return "connected";
        }
        if (state == 2) {
            return "disconnected";
        }
        if (state != 3) {
            return "unknown";
        }
        return "transitioning";
    }

    public void handleConnectionStateChanged(int state) {
        localLog("handleConnectionStateChanged: state=" + stateToString(state));
        this.mWifiState = state;
        if (this.mWifiState == 2) {
            this.mLastConnectionAttemptBssid = null;
            scheduleWatchdogTimer();
            startConnectivityScan(true);
            return;
        }
        startConnectivityScan(false);
    }

    public void handleConnectionAttemptEnded(int failureCode) {
        String ssid;
        if (failureCode == 1) {
            if (this.mWifiInfo.getWifiSsid() == null) {
                ssid = null;
            } else {
                ssid = this.mWifiInfo.getWifiSsid().toString();
            }
            this.mOpenNetworkNotifier.handleWifiConnected(ssid);
            this.mCarrierNetworkNotifier.handleWifiConnected(ssid);
            return;
        }
        this.mOpenNetworkNotifier.handleConnectionFailure();
        this.mCarrierNetworkNotifier.handleConnectionFailure();
    }

    private void checkStateAndEnable() {
        enable(!this.mSpecificNetworkRequestInProgress && (this.mUntrustedConnectionAllowed || this.mTrustedConnectionAllowed));
        startConnectivityScan(true);
    }

    public void setTrustedConnectionAllowed(boolean allowed) {
        localLog("setTrustedConnectionAllowed: allowed=" + allowed);
        if (this.mTrustedConnectionAllowed != allowed) {
            this.mTrustedConnectionAllowed = allowed;
            checkStateAndEnable();
        }
    }

    public void setUntrustedConnectionAllowed(boolean allowed) {
        localLog("setUntrustedConnectionAllowed: allowed=" + allowed);
        if (this.mUntrustedConnectionAllowed != allowed) {
            this.mUntrustedConnectionAllowed = allowed;
            checkStateAndEnable();
        }
    }

    public void setSpecificNetworkRequestInProgress(boolean inProgress) {
        localLog("setsetSpecificNetworkRequestInProgress : inProgress=" + inProgress);
        if (this.mSpecificNetworkRequestInProgress != inProgress) {
            this.mSpecificNetworkRequestInProgress = inProgress;
            checkStateAndEnable();
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
        if (enable) {
            return this.mBssidBlacklist.remove(bssid) != null;
        }
        if (this.mWifiLastResortWatchdog.shouldIgnoreBssidUpdate(bssid)) {
            localLog("Ignore update Bssid Blacklist since Watchdog trigger is activated");
            return false;
        }
        BssidBlacklistStatus status = this.mBssidBlacklist.get(bssid);
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
        StringBuilder sb = new StringBuilder();
        sb.append("trackBssid: ");
        sb.append(enable ? "enable " : "disable ");
        sb.append(bssid);
        sb.append(" reason code ");
        sb.append(reasonCode);
        localLog(sb.toString());
        if (bssid == null || !updateBssidBlacklist(bssid, enable, reasonCode)) {
            return false;
        }
        updateFirmwareRoamingConfiguration();
        if (!enable) {
            startConnectivityScan(true);
        }
        return true;
    }

    @VisibleForTesting
    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = this.mBssidBlacklist.get(bssid);
        if (status == null) {
            return false;
        }
        return status.isBlacklisted;
    }

    private HashSet<String> buildBssidBlacklist() {
        HashSet<String> blacklistedBssids = new HashSet<>();
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
            ArrayList<String> blacklistedBssids = new ArrayList<>(buildBssidBlacklist());
            int blacklistSize = blacklistedBssids.size();
            if (blacklistSize > maxBlacklistSize) {
                Log.wtf(TAG, "Attempt to write " + blacklistSize + " blacklisted BSSIDs, max size is " + maxBlacklistSize);
                blacklistedBssids = new ArrayList<>(blacklistedBssids.subList(0, maxBlacklistSize));
                localLog("Trim down BSSID blacklist size from " + blacklistSize + " to " + blacklistedBssids.size());
            }
            if (blacklistSize == 0) {
                localLog("Set a invaild bssid to blacklist");
                blacklistedBssids.add("00:00:00:00:00:00");
            }
            if (!this.mConnectivityHelper.setFirmwareRoamingConfiguration(blacklistedBssids, new ArrayList<>())) {
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
                BssidBlacklistStatus status = iter.next();
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

    private void retrieveWifiScanner() {
        if (this.mScanner == null) {
            this.mScanner = this.mWifiInjector.getWifiScanner();
            Preconditions.checkNotNull(this.mScanner);
            this.mScanner.registerScanListener(this.mAllSingleScanListener);
        }
    }

    private void clearBssidBlacklist() {
        this.mBssidBlacklist.clear();
        updateFirmwareRoamingConfiguration();
    }

    private void start() {
        if (!this.mRunning) {
            retrieveWifiScanner();
            this.mConnectivityHelper.getFirmwareRoamingInfo();
            clearBssidBlacklist();
            this.mRunning = true;
        }
    }

    private void stop() {
        if (this.mRunning) {
            this.mRunning = false;
            stopConnectivityScan();
            clearBssidBlacklist();
            resetLastPeriodicSingleScanTimeStamp();
            this.mOpenNetworkNotifier.clearPendingNotification(true);
            this.mCarrierNetworkNotifier.clearPendingNotification(true);
            this.mLastConnectionAttemptBssid = null;
            this.mWaitForFullBandScanResults = false;
        }
    }

    private void updateRunningState() {
        if (!this.mWifiEnabled || !this.mWifiConnectivityManagerEnabled) {
            localLog("Stopping WifiConnectivityManager");
            stop();
            return;
        }
        localLog("Starting up WifiConnectivityManager");
        start();
    }

    public void setWifiEnabled(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Set WiFi ");
        sb.append(enable ? "enabled" : "disabled");
        localLog(sb.toString());
        this.mWifiEnabled = enable;
        updateRunningState();
    }

    public void enable(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Set WiFiConnectivityManager ");
        sb.append(enable ? "enabled" : "disabled");
        localLog(sb.toString());
        this.mWifiConnectivityManagerEnabled = enable;
        updateRunningState();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getLowRssiNetworkRetryDelay() {
        return this.mPnoScanListener.getLowRssiNetworkRetryDelay();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getLastPeriodicSingleScanTimeStamp() {
        return this.mLastPeriodicSingleScanTimeStamp;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConnectivityManager");
        pw.println("WifiConnectivityManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConnectivityManager - Log End ----");
        this.mOpenNetworkNotifier.dump(fd, pw, args);
        this.mCarrierNetworkNotifier.dump(fd, pw, args);
        this.mCarrierNetworkConfig.dump(fd, pw, args);
    }

    public boolean isPnoStarted() {
        return this.mPnoScanStarted;
    }

    public void tryStartConnectivityScan() {
        startConnectivityScan(false);
    }
}
