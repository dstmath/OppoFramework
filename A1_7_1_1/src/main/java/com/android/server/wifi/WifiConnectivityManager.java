package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
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
import android.os.Handler;
import android.os.Looper;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.util.ScanDetailUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiConnectivityManager {
    private static final int CHANNEL_LIST_AGE_MS = 3600000;
    private static final int CONNECTED_PNO_SCAN_INTERVAL_MS = 160000;
    private static final int DISCONNECTED_PNO_SCAN_INTERVAL_MS = 20000;
    private static final boolean ENABLE_BACKGROUND_SCAN = false;
    private static final boolean ENABLE_CONNECTED_PNO_SCAN = false;
    private static final int LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS = 80000;
    private static final int LOW_RSSI_NETWORK_RETRY_START_DELAY_MS = 20000;
    public static final int MAX_CONNECTION_ATTEMPTS_RATE = 6;
    public static final int MAX_CONNECTION_ATTEMPTS_TIME_INTERVAL_MS = 240000;
    public static final int MAX_PERIODIC_SCAN_INTERVAL_MS = 160000;
    public static final int MAX_SCAN_RESTART_ALLOWED = 5;
    public static int PERIODIC_SCAN_INTERVAL_MS = 0;
    public static final String PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Periodic Scan Timer";
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    public static final String RESTART_CONNECTIVITY_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Scan";
    private static final int RESTART_SCAN_DELAY_MS = 2000;
    public static final String RESTART_SINGLE_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Single Scan";
    private static final boolean SCAN_IMMEDIATELY = true;
    private static final boolean SCAN_ON_SCHEDULE = false;
    private static final String TAG = "WifiConnectivityManager";
    public static final String T_PUT_MONITOR_TIMER_TAG = "WifiConnectivityManager Schedule T-put monitor Timer";
    private static final int WATCHDOG_INTERVAL_MS = 1200000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    private final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener;
    private boolean mBackoffScanCheckTputSupport;
    private int mBackoffScanInterval;
    private int mBand5GHzBonus;
    private final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    private int mCurrentConnectionBonus;
    private boolean mDbg;
    private final Handler mEventHandler;
    private int mInitialScoreMax;
    private String mLastConnectionAttemptBssid;
    private long mLastPeriodicSingleScanTimeStamp;
    private boolean mLinkQualityBad;
    private final LocalLog mLocalLog;
    private int mMin24GHzRssi;
    private int mMin5GHzRssi;
    private final PeriodicScanListener mPeriodicScanListener;
    private final OnAlarmListener mPeriodicScanTimerListener;
    private boolean mPeriodicScanTimerSet;
    private int mPeriodicSingleScanInterval;
    private final PnoScanListener mPnoScanListener;
    private boolean mPnoScanStarted;
    private final WifiQualifiedNetworkSelector mQualifiedNetworkSelector;
    private final OnAlarmListener mRestartScanListener;
    private int mSameNetworkBonus;
    private int mScanRestartCount;
    private final WifiScanner mScanner;
    private boolean mScreenOn;
    private boolean mScreenOnConnectedBackoffScanSupport;
    private boolean mScreenOnConnectedScanEnabled;
    private int mSecureBonus;
    private int mSingleScanRestartCount;
    private final WifiStateMachine mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited;
    private final OnAlarmListener mTputMonitorTimerListener;
    private boolean mUntrustedConnectionAllowed;
    private boolean mWaitForFullBandScanResults;
    private final OnAlarmListener mWatchdogListener;
    private boolean mWifiConnectivityManagerEnabled;
    private boolean mWifiEnabled;
    private final WifiInfo mWifiInfo;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private int mWifiState;

    private class AllSingleScanListener implements ScanListener {
        private List<ScanDetail> mScanDetails;

        /* synthetic */ AllSingleScanListener(WifiConnectivityManager this$0, AllSingleScanListener allSingleScanListener) {
            this();
        }

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog("registerScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.e(WifiConnectivityManager.TAG, "registerScanListener onFailure: reason: " + reason + " description: " + description);
        }

        public void onPeriodChanged(int periodInMs) {
        }

        public void onResults(ScanData[] results) {
            if (WifiConnectivityManager.this.mWifiEnabled && WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                    if (results[0].isAllChannelsScanned()) {
                        WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                    } else {
                        WifiConnectivityManager.this.localLog("AllSingleScanListener waiting for full band scan results.");
                        clearScanDetails();
                        return;
                    }
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
            if (WifiConnectivityManager.this.mWifiEnabled && WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                this.mScanDetails.add(ScanDetailUtil.toScanDetail(fullScanResult));
            }
        }
    }

    private class PeriodicScanListener implements ScanListener {
        private List<ScanDetail> mScanDetails;

        /* synthetic */ PeriodicScanListener(WifiConnectivityManager this$0, PeriodicScanListener periodicScanListener) {
            this();
        }

        private PeriodicScanListener() {
            this.mScanDetails = new ArrayList();
        }

        public void clearScanDetails() {
            this.mScanDetails.clear();
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog("PeriodicScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.e(WifiConnectivityManager.TAG, "PeriodicScanListener onFailure: reason: " + reason + " description: " + description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get1 = wifiConnectivityManager.mScanRestartCount;
            wifiConnectivityManager.mScanRestartCount = -get1 + 1;
            if (-get1 < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(WifiConnectivityManager.RESTART_SCAN_DELAY_MS);
                return;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
            Log.e(WifiConnectivityManager.TAG, "Failed to successfully start periodic scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog("PeriodicScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(ScanData[] results) {
            WifiConnectivityManager.this.localLog("PeriodicScanListener onResults: " + results);
            WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "PeriodicScanListener");
            clearScanDetails();
            WifiConnectivityManager.this.mScanRestartCount = 0;
        }

        public void onFullResult(ScanResult fullScanResult) {
            this.mScanDetails.add(ScanDetailUtil.toScanDetail(fullScanResult));
        }
    }

    private class PnoScanListener implements android.net.wifi.WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        /* synthetic */ PnoScanListener(WifiConnectivityManager this$0, PnoScanListener pnoScanListener) {
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
            WifiConnectivityManager.this.localLog("PnoScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.e(WifiConnectivityManager.TAG, "PnoScanListener onFailure: reason: " + reason + " description: " + description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get1 = wifiConnectivityManager.mScanRestartCount;
            wifiConnectivityManager.mScanRestartCount = -get1 + 1;
            if (-get1 < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(WifiConnectivityManager.RESTART_SCAN_DELAY_MS);
                return;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
            Log.e(WifiConnectivityManager.TAG, "Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog("PnoScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            WifiConnectivityManager.this.localLog("PnoScanListener: onPnoNetworkFound: results len = " + results.length);
            for (ScanResult result : results) {
                this.mScanDetails.add(ScanDetailUtil.toScanDetail(result));
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
            WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan);
        }
    }

    private class SingleScanListener implements ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog("SingleScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.e(WifiConnectivityManager.TAG, "SingleScanListener onFailure: reason: " + reason + " description: " + description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get2 = wifiConnectivityManager.mSingleScanRestartCount;
            wifiConnectivityManager.mSingleScanRestartCount = -get2 + 1;
            if (-get2 < 5) {
                WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
                return;
            }
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
            Log.e(WifiConnectivityManager.TAG, "Failed to successfully start single scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog("SingleScanListener onPeriodChanged: actual scan period " + periodInMs + "ms");
        }

        public void onResults(ScanData[] results) {
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }

        public void onFullResult(ScanResult fullScanResult) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConnectivityManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConnectivityManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiConnectivityManager.<clinit>():void");
    }

    public static void setPeriodicScanIntervalMs(boolean enable) {
        if (enable) {
            PERIODIC_SCAN_INTERVAL_MS = 90000;
            Log.d(TAG, "Set PERIODIC_SCAN_INTERVAL_MS = 90 seconds!");
            return;
        }
        PERIODIC_SCAN_INTERVAL_MS = 15000;
        Log.d(TAG, "Set PERIODIC_SCAN_INTERVAL_MS = 15 seconds!");
    }

    private void localLog(String log) {
        if (this.mDbg) {
            this.mLocalLog.log(log);
            Log.d(TAG, log);
        }
    }

    private boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName) {
        localLog(listenerName + " onResults: start QNS");
        WifiConfiguration candidate = this.mQualifiedNetworkSelector.selectQualifiedNetwork(false, this.mUntrustedConnectionAllowed, scanDetails, this.mStateMachine.isLinkDebouncing(), this.mStateMachine.isConnected(), this.mStateMachine.isDisconnected(), this.mStateMachine.isSupplicantTransientState());
        this.mWifiLastResortWatchdog.updateAvailableNetworks(this.mQualifiedNetworkSelector.getFilteredScanDetails());
        this.mWifiMetrics.countScanResults(scanDetails);
        if (candidate == null) {
            return false;
        }
        localLog(listenerName + ": QNS candidate-" + candidate.SSID);
        connectToNetwork(candidate);
        return true;
    }

    public WifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiQualifiedNetworkSelector qualifiedNetworkSelector, WifiInjector wifiInjector, Looper looper, boolean enable) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : Constants.ANQP_QUERY_LIST);
        this.mDbg = false;
        this.mWifiEnabled = false;
        this.mWifiConnectivityManagerEnabled = true;
        this.mScreenOn = false;
        this.mWifiState = 0;
        this.mUntrustedConnectionAllowed = false;
        this.mScanRestartCount = 0;
        this.mSingleScanRestartCount = 0;
        this.mTotalConnectivityAttemptsRateLimited = 0;
        this.mLastConnectionAttemptBssid = null;
        this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
        this.mPnoScanStarted = false;
        this.mPeriodicScanTimerSet = false;
        this.mWaitForFullBandScanResults = false;
        this.mLinkQualityBad = false;
        this.mScreenOnConnectedScanEnabled = false;
        this.mScreenOnConnectedBackoffScanSupport = false;
        this.mBackoffScanCheckTputSupport = false;
        this.mBackoffScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        this.mRestartScanListener = new OnAlarmListener() {
            public void onAlarm() {
                WifiConnectivityManager.this.startConnectivityScan(true);
            }
        };
        this.mWatchdogListener = new OnAlarmListener() {
            public void onAlarm() {
                WifiConnectivityManager.this.watchdogHandler();
            }
        };
        this.mPeriodicScanTimerListener = new OnAlarmListener() {
            public void onAlarm() {
                WifiConnectivityManager.this.periodicScanTimerHandler();
            }
        };
        this.mPeriodicScanListener = new PeriodicScanListener(this, null);
        this.mAllSingleScanListener = new AllSingleScanListener(this, null);
        this.mPnoScanListener = new PnoScanListener(this, null);
        this.mTputMonitorTimerListener = new OnAlarmListener() {
            public void onAlarm() {
                WifiConnectivityManager.this.tputMonitorTimerHandler();
            }
        };
        this.mStateMachine = stateMachine;
        this.mScanner = scanner;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mQualifiedNetworkSelector = qualifiedNetworkSelector;
        this.mWifiLastResortWatchdog = wifiInjector.getWifiLastResortWatchdog();
        this.mWifiMetrics = wifiInjector.getWifiMetrics();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = wifiInjector.getClock();
        this.mConnectionAttemptTimeStamps = new LinkedList();
        this.mMin5GHzRssi = -82;
        this.mMin24GHzRssi = -85;
        this.mBand5GHzBonus = 40;
        this.mCurrentConnectionBonus = this.mConfigManager.mCurrentNetworkBoost.get();
        this.mSameNetworkBonus = context.getResources().getInteger(17694747);
        this.mSecureBonus = context.getResources().getInteger(17694750);
        this.mInitialScoreMax = (this.mConfigManager.mThresholdSaturatedRssi24.get() + 85) * 4;
        this.mScreenOnConnectedBackoffScanSupport = context.getResources().getBoolean(135004163);
        this.mBackoffScanCheckTputSupport = context.getResources().getBoolean(135004164);
        if (this.mDbg) {
            Log.i(TAG, "PNO settings: min5GHzRssi " + this.mMin5GHzRssi + " min24GHzRssi " + this.mMin24GHzRssi + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + this.mInitialScoreMax + " backoffscan " + this.mScreenOnConnectedBackoffScanSupport + " backoffscanTput " + this.mBackoffScanCheckTputSupport);
        }
        this.mScanner.registerScanListener(this.mAllSingleScanListener);
        this.mWifiConnectivityManagerEnabled = enable;
        Log.i(TAG, "ConnectivityScanManager initialized and " + (enable ? "enabled" : "disabled"));
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
            Log.e(TAG, "connectToNetwork: bad candidate - " + candidate + " scanResult: " + scanResultCandidate);
            return;
        }
        String targetBssid = scanResultCandidate.BSSID;
        String targetAssociationId = candidate.SSID + " : " + targetBssid;
        if ((targetBssid != null && targetBssid.equals(this.mWifiInfo.getBSSID()) && SupplicantState.isConnecting(this.mWifiInfo.getSupplicantState())) || (targetBssid.equals(this.mLastConnectionAttemptBssid) && SupplicantState.isHandshakeState(this.mWifiInfo.getSupplicantState()))) {
            localLog("connectToNetwork: Either already connected or is connecting to " + targetAssociationId);
        } else if (this.mStateMachine.shouldSwitchNetwork()) {
            Long elapsedTimeMillis = Long.valueOf(this.mClock.elapsedRealtime());
            if (this.mScreenOn || !shouldSkipConnectionAttempt(elapsedTimeMillis)) {
                String currentAssociationId;
                noteConnectionAttempt(elapsedTimeMillis);
                this.mLastConnectionAttemptBssid = targetBssid;
                WifiConfiguration currentConnectedNetwork = this.mConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId());
                if (currentConnectedNetwork == null) {
                    currentAssociationId = "Disconnected";
                } else {
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + this.mWifiInfo.getBSSID();
                }
                if (currentConnectedNetwork == null || !(currentConnectedNetwork.networkId == candidate.networkId || currentConnectedNetwork.isLinked(candidate))) {
                    localLog("connectToNetwork: Reconnect from " + currentAssociationId + " to " + targetAssociationId);
                    this.mStateMachine.autoConnectToNetwork(candidate.networkId, scanResultCandidate.BSSID);
                } else {
                    localLog("connectToNetwork: Roaming from " + currentAssociationId + " to " + targetAssociationId);
                    this.mStateMachine.autoRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
                return;
            }
            localLog("connectToNetwork: Too many connection attempts. Skipping this attempt!");
            this.mTotalConnectivityAttemptsRateLimited++;
        }
    }

    private int getScanBand() {
        return getScanBand(true);
    }

    private int getScanBand(boolean isFullBandScan) {
        if (!isFullBandScan) {
            return 0;
        }
        int freqBand = this.mStateMachine.getFrequencyBand();
        if (freqBand == 1) {
            return 6;
        }
        if (freqBand == 2) {
            return 1;
        }
        return 7;
    }

    private boolean setScanChannels(ScanSettings settings) {
        WifiConfiguration config = this.mStateMachine.getCurrentWifiConfiguration();
        if (config == null) {
            return false;
        }
        HashSet<Integer> freqs = this.mConfigManager.makeChannelList(config, CHANNEL_LIST_AGE_MS);
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
        localLog("watchdogHandler");
        if (this.mWifiState == 2) {
            if (this.mDbg) {
                Log.i(TAG, "start a single scan from watchdogHandler");
            }
            scheduleWatchdogTimer();
            startSingleScan(true);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.elapsedRealtime();
        if (this.mLastPeriodicSingleScanTimeStamp != RESET_TIME_STAMP) {
            long msSinceLastScan = currentTimeStamp - this.mLastPeriodicSingleScanTimeStamp;
            if (msSinceLastScan < ((long) PERIODIC_SCAN_INTERVAL_MS)) {
                localLog("Last periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                schedulePeriodicScanTimer(PERIODIC_SCAN_INTERVAL_MS - ((int) msSinceLastScan));
                return;
            }
        }
        boolean isFullBandScan = true;
        boolean scanEnable = true;
        if (this.mWifiState == 1 && (this.mWifiInfo.txSuccessRate > 8.0d || this.mWifiInfo.rxSuccessRate > 16.0d)) {
            localLog("No full band scan due to heavy traffic, txSuccessRate=" + this.mWifiInfo.txSuccessRate + " rxSuccessRate=" + this.mWifiInfo.rxSuccessRate);
            isFullBandScan = false;
        }
        if (this.mWifiState == 1 && (this.mWifiInfo.txSuccessRate > 40.0d || this.mWifiInfo.rxSuccessRate > 80.0d)) {
            localLog("No scan due to heavy traffic, txSuccessRate=" + this.mWifiInfo.txSuccessRate + " rxSuccessRate=" + this.mWifiInfo.rxSuccessRate);
            scanEnable = false;
        }
        if ((this.mWifiState == 1 && this.mWifiInfo.is24GHz() && this.mWifiInfo.getLinkSpeed() < 6 && this.mWifiInfo.getRssi() < -70) || (this.mWifiInfo.is5GHz() && this.mWifiInfo.getLinkSpeed() <= 9 && this.mWifiInfo.getRssi() < -70)) {
            scanEnable = false;
        }
        this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
        if (scanEnable) {
            startSingleScan(isFullBandScan);
        }
        schedulePeriodicScanTimer(this.mPeriodicSingleScanInterval);
        this.mPeriodicSingleScanInterval *= 2;
        if (this.mPeriodicSingleScanInterval > 160000) {
            this.mPeriodicSingleScanInterval = 160000;
        }
        if (this.mWifiState == 2) {
            this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
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

    private void startSingleScan(boolean isFullBandScan) {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
            ScanSettings settings = new ScanSettings();
            if (!(isFullBandScan || setScanChannels(settings))) {
                isFullBandScan = true;
            }
            settings.band = getScanBand(isFullBandScan);
            settings.reportEvents = 3;
            settings.numBssidsPerScan = 0;
            Set<Integer> hiddenNetworkIds = this.mConfigManager.getHiddenConfiguredNetworkIds();
            if (hiddenNetworkIds != null && hiddenNetworkIds.size() > 0) {
                int i = 0;
                settings.hiddenNetworkIds = new int[hiddenNetworkIds.size()];
                for (Integer netId : hiddenNetworkIds) {
                    int i2 = i + 1;
                    settings.hiddenNetworkIds[i] = netId.intValue();
                    i = i2;
                }
            }
            SingleScanListener singleScanListener = new SingleScanListener(isFullBandScan);
            localLog("startSingleScan: " + settings);
            this.mScanner.startScan(settings, singleScanListener, WifiStateMachine.WIFI_WORK_SOURCE);
        }
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || this.mConfigManager.getEnableAutoJoinWhenAssociated()) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            Log.d(TAG, "startPeriodicScan(): mPeriodicSingleScanInterval = " + this.mPeriodicSingleScanInterval);
            startPeriodicSingleScan();
        }
    }

    private void startDisconnectedPnoScan() {
        PnoSettings pnoSettings = new PnoSettings();
        ArrayList<PnoNetwork> pnoNetworkList = this.mConfigManager.retrieveDisconnectedPnoNetworkList();
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
        localLog("startDisconnectedPnoScan: " + scanSettings);
        this.mScanner.startDisconnectedPnoScan(scanSettings, pnoSettings, this.mPnoScanListener);
        this.mPnoScanStarted = true;
    }

    private void startConnectedPnoScan() {
    }

    private void stopPnoScan() {
        if (this.mPnoScanStarted) {
            this.mScanner.stopPnoScan(this.mPnoScanListener);
        }
        this.mPnoScanStarted = false;
    }

    private void scheduleWatchdogTimer() {
        if (this.mDbg) {
            Log.i(TAG, "scheduleWatchdogTimer 1200000");
        }
        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + 1200000, WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
    }

    private void schedulePeriodicScanTimer(int intervalMs) {
        if (this.mDbg) {
            Log.i(TAG, "schedulePeriodicScanTimer: " + intervalMs);
        }
        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + ((long) intervalMs), PERIODIC_SCAN_TIMER_TAG, this.mPeriodicScanTimerListener, this.mEventHandler);
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
        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + 2000, RESTART_SINGLE_SCAN_TIMER_TAG, new RestartSingleScanListener(isFullBandScan), this.mEventHandler);
    }

    private void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + ((long) msFromNow), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
    }

    private void startConnectivityScan(boolean scanImmediately) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + this.mWifiState + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            stopConnectivityScan();
            if ((this.mWifiState == 1 || this.mWifiState == 2) && !this.mStateMachine.isTemporarilyDontReconnectWifi()) {
                if (this.mScreenOn) {
                    startPeriodicScan(scanImmediately);
                } else if (this.mWifiState == 1) {
                    startConnectedPnoScan();
                } else {
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
        startConnectivityScan(false);
    }

    public void handleConnectionStateChanged(int state) {
        localLog("handleConnectionStateChanged: state=" + state);
        this.mWifiState = state;
        if (this.mWifiState == 2) {
            this.mLastConnectionAttemptBssid = null;
            scheduleWatchdogTimer();
        }
        startConnectivityScan(false);
    }

    public void setUntrustedConnectionAllowed(boolean allowed) {
        if (this.mDbg) {
            Log.i(TAG, "setUntrustedConnectionAllowed: allowed=" + allowed);
        }
        if (this.mUntrustedConnectionAllowed != allowed) {
            this.mUntrustedConnectionAllowed = allowed;
            startConnectivityScan(true);
        }
    }

    public void connectToUserSelectNetwork(int netId, boolean persistent) {
        if (this.mDbg) {
            Log.i(TAG, "connectToUserSelectNetwork: netId=" + netId + " persist=" + persistent);
        }
        this.mQualifiedNetworkSelector.userSelectNetwork(netId, persistent);
        clearConnectionAttemptTimeStamps();
    }

    public void forceConnectivityScan() {
        if (this.mDbg) {
            Log.i(TAG, "forceConnectivityScan");
        }
        this.mWaitForFullBandScanResults = true;
        startSingleScan(true);
    }

    public boolean trackBssid(String bssid, boolean enable) {
        if (this.mDbg) {
            Log.i(TAG, "trackBssid: " + (enable ? "enable " : "disable ") + bssid);
        }
        boolean ret = this.mQualifiedNetworkSelector.enableBssidForQualityNetworkSelection(bssid, enable);
        if (ret && !enable) {
            startConnectivityScan(true);
        }
        return ret;
    }

    public void setUserPreferredBand(int band) {
        if (this.mDbg) {
            Log.i(TAG, "User band preference: " + band);
        }
        this.mQualifiedNetworkSelector.setUserPreferredBand(band);
        startConnectivityScan(true);
    }

    public void setWifiEnabled(boolean enable) {
        if (this.mDbg) {
            Log.i(TAG, "Set WiFi " + (enable ? "enabled" : "disabled"));
        }
        this.mWifiEnabled = enable;
        if (!this.mWifiEnabled) {
            stopConnectivityScan();
            resetLastPeriodicSingleScanTimeStamp();
            this.mLastConnectionAttemptBssid = null;
            this.mWaitForFullBandScanResults = false;
        } else if (this.mWifiConnectivityManagerEnabled) {
            startConnectivityScan(true);
        }
    }

    public void enable(boolean enable) {
        if (this.mDbg) {
            Log.i(TAG, "Set WiFiConnectivityManager " + (enable ? "enabled" : "disabled"));
        }
        this.mWifiConnectivityManagerEnabled = enable;
        if (!this.mWifiConnectivityManagerEnabled) {
            stopConnectivityScan();
            resetLastPeriodicSingleScanTimeStamp();
            this.mLastConnectionAttemptBssid = null;
            this.mWaitForFullBandScanResults = false;
        } else if (this.mWifiEnabled) {
            startConnectivityScan(true);
        }
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = false;
        if (verbose > 0) {
            z = true;
        }
        this.mDbg = z;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConnectivityManager");
        pw.println("WifiConnectivityManager - Log Begin ----");
        pw.println("WifiConnectivityManager - Number of connectivity attempts rate limited: " + this.mTotalConnectivityAttemptsRateLimited);
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConnectivityManager - Log End ----");
    }

    int getLowRssiNetworkRetryDelay() {
        return this.mPnoScanListener.getLowRssiNetworkRetryDelay();
    }

    long getLastPeriodicSingleScanTimeStamp() {
        return this.mLastPeriodicSingleScanTimeStamp;
    }

    public void handleScanStrategyChanged() {
        localLog("handleScanStrategyChanged");
        startConnectivityScan(false);
    }

    private void startBackOffPeriodicScan(boolean scanImmediately, boolean isFullBandScan) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (scanImmediately) {
            resetLastPeriodicSingleScanTimeStamp();
        }
        this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
        Log.d(TAG, "startBackOffPeriodicScan(): mPeriodicSingleScanInterval = " + this.mPeriodicSingleScanInterval);
        startPeriodicSingleScan();
    }

    private void scheduleTputCheckTimer(int intervalMs) {
        if (this.mDbg) {
            Log.i(TAG, "scheduleTputCheckTimer: " + intervalMs);
        }
        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + ((long) intervalMs), T_PUT_MONITOR_TIMER_TAG, this.mTputMonitorTimerListener, this.mEventHandler);
    }

    private void tputMonitorTimerHandler() {
        localLog("tputMonitorTimerHandler");
        if (this.mScreenOn) {
            checkTputForScan();
        }
    }

    private void checkTputForScan() {
        boolean scanEnable = true;
        this.mBackoffScanInterval *= 2;
        if (this.mBackoffScanInterval > 160000) {
            this.mBackoffScanInterval = 160000;
        }
        if (this.mWifiInfo == null) {
            localLog("error: no mWifiInfo return");
            return;
        }
        double txSuccessRate = this.mWifiInfo.txSuccessRate;
        double rxSuccessRate = this.mWifiInfo.rxSuccessRate;
        boolean is24G = this.mWifiInfo.is24GHz();
        boolean is5G = this.mWifiInfo.is5GHz();
        localLog("txSuccessRate=" + txSuccessRate + " rxSuccessRate=" + rxSuccessRate + " linkspeed=" + this.mWifiInfo.getLinkSpeed());
        if (this.mWifiState == 1 && (txSuccessRate > 40.0d || rxSuccessRate > 80.0d)) {
            scanEnable = false;
        }
        if ((this.mWifiInfo.is24GHz() && this.mWifiInfo.getLinkSpeed() < 6 && this.mWifiInfo.getRssi() < -70) || (this.mWifiInfo.is5GHz() && this.mWifiInfo.getLinkSpeed() <= 9 && this.mWifiInfo.getRssi() < -70)) {
            scanEnable = false;
        }
        if (this.mScreenOnConnectedScanEnabled == scanEnable) {
            scheduleTputCheckTimer(this.mBackoffScanInterval);
        } else if (scanEnable) {
            boolean isFullBandScan = shouldFullBandScan();
            localLog("restart startBackOffPeriodicScan");
            startBackOffPeriodicScan(true, isFullBandScan);
        } else {
            localLog("stop BackOffPeriodicScan due to low link quality or tput high");
            this.mScanner.stopBackgroundScan(this.mPeriodicScanListener);
            this.mScreenOnConnectedScanEnabled = false;
            scheduleTputCheckTimer(this.mBackoffScanInterval);
        }
    }

    boolean shouldFullBandScan() {
        if (this.mWifiState != 1) {
            return true;
        }
        if (this.mWifiInfo.txSuccessRate <= 8.0d && this.mWifiInfo.rxSuccessRate <= 16.0d) {
            return true;
        }
        localLog("No full band scan due to heavy traffic, txSuccessRate=" + this.mWifiInfo.txSuccessRate + " rxSuccessRate=" + this.mWifiInfo.rxSuccessRate);
        return false;
    }
}
