package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.WifiKey;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.mediatek.common.wifi.IWifiFwkExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WifiQualifiedNetworkSelector {
    public static final int BAND_AWARD_5GHz = 40;
    public static final int BSSID_BLACKLIST_EXPIRE_TIME = 300000;
    public static final int BSSID_BLACKLIST_THRESHOLD = 3;
    private static final boolean FORCE_DEBUG = true;
    private static final int INVALID_TIME_STAMP = -1;
    public static final int LAST_SELECTION_AWARD = 480;
    public static final int MINIMUM_2G_ACCEPT_RSSI = -85;
    public static final int MINIMUM_5G_ACCEPT_RSSI = -82;
    private static final int MINIMUM_QUALIFIED_NETWORK_SELECTION_INTERVAL = 10000;
    public static final int PASSPOINT_SECURITY_AWARD = 40;
    public static final int QUALIFIED_RSSI_24G_BAND = -73;
    public static final int QUALIFIED_RSSI_5G_BAND = -70;
    public static final int RSSI_SATURATION_2G_BAND = -60;
    public static final int RSSI_SATURATION_5G_BAND = -57;
    public static final int RSSI_SCORE_OFFSET = 85;
    public static final int RSSI_SCORE_SLOPE = 4;
    public static final int SAME_BSSID_AWARD = 24;
    public static final int SAME_NETWORK_AWARD = 16;
    public static final int SCAN_RESULT_MAXIMUNM_AGE = 40000;
    public static final int SECURITY_AWARD = 80;
    private static final String TAG = "WifiQualifiedNetworkSelector:";
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new HashMap();
    private Clock mClock;
    private String mCurrentBssid = null;
    private WifiConfiguration mCurrentConnectedNetwork = null;
    private boolean mDbg = true;
    private volatile List<Pair<ScanDetail, WifiConfiguration>> mFilteredScanDetails = null;
    private long mLastQualifiedNetworkSelectionTimeStamp = -1;
    private int mLastSelectionAward = LAST_SELECTION_AWARD;
    private WifiConfiguration mLastUserSelectSecuredNetworkForOP03 = null;
    private final LocalLog mLocalLog = new LocalLog(512);
    private WifiNetworkScoreCache mNetworkScoreCache;
    private final int mNoIntnetPenalty;
    private int mPasspointSecurityAward = 40;
    private int mRssiScoreOffset = 85;
    private int mRssiScoreSlope = 4;
    private int mSameBssidAward = 24;
    private List<ScanDetail> mScanDetails = null;
    private NetworkScoreManager mScoreManager;
    private int mSecurityAward = 80;
    private int mUserPreferedBand = 0;
    private WifiConfigManager mWifiConfigManager;
    private IWifiFwkExt mWifiFwkExt;
    private WifiInfo mWifiInfo;
    private boolean skipQualifiedNetworkSelectionForAutoConnect = true;

    private static class BssidBlacklistStatus {
        long mBlacklistedTimeStamp;
        int mCounter;
        boolean mIsBlacklisted;

        /* synthetic */ BssidBlacklistStatus(BssidBlacklistStatus bssidBlacklistStatus) {
            this();
        }

        private BssidBlacklistStatus() {
            this.mBlacklistedTimeStamp = -1;
        }
    }

    static class ExternalScoreEvaluator {
        private int mBestCandidateType = 0;
        private final boolean mDbg;
        private int mHighScore = WifiNetworkScoreCache.INVALID_NETWORK_SCORE;
        private final LocalLog mLocalLog;
        private WifiConfiguration mSavedConfig;
        private ScanResult mScanResultCandidate;

        @Retention(RetentionPolicy.SOURCE)
        @interface BestCandidateType {
            public static final int NONE = 0;
            public static final int SAVED_NETWORK = 1;
            public static final int UNTRUSTED_NETWORK = 2;
        }

        ExternalScoreEvaluator(LocalLog localLog, boolean dbg) {
            this.mLocalLog = localLog;
            this.mDbg = dbg;
        }

        void evalUntrustedCandidate(Integer score, ScanResult scanResult) {
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 2;
                localLog(WifiQualifiedNetworkSelector.toScanId(scanResult) + " become the new untrusted candidate");
            }
        }

        void evalSavedCandidate(Integer score, WifiConfiguration config, ScanResult scanResult) {
            if (score == null) {
                return;
            }
            if (score.intValue() > this.mHighScore || (this.mBestCandidateType == 2 && score.intValue() == this.mHighScore)) {
                this.mHighScore = score.intValue();
                this.mSavedConfig = config;
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 1;
                localLog(WifiQualifiedNetworkSelector.toScanId(scanResult) + " become the new externally scored saved network " + "candidate");
            }
        }

        int getBestCandidateType() {
            return this.mBestCandidateType;
        }

        int getHighScore() {
            return this.mHighScore;
        }

        public ScanResult getScanResultCandidate() {
            return this.mScanResultCandidate;
        }

        WifiConfiguration getSavedConfig() {
            return this.mSavedConfig;
        }

        private void localLog(String log) {
            if (this.mDbg) {
                this.mLocalLog.log(log);
            }
        }
    }

    public void setWifiFwkExt(IWifiFwkExt ext) {
        this.mWifiFwkExt = ext;
    }

    private void localLog(String log) {
        if (this.mDbg) {
            this.mLocalLog.log(log);
            Log.d(TAG, log);
        }
    }

    private void localLoge(String log) {
        this.mLocalLog.log(log);
        Log.d(TAG, log);
    }

    void setWifiNetworkScoreCache(WifiNetworkScoreCache cache) {
        this.mNetworkScoreCache = cache;
    }

    public WifiConfiguration getConnetionTargetNetwork() {
        return this.mCurrentConnectedNetwork;
    }

    public List<Pair<ScanDetail, WifiConfiguration>> getFilteredScanDetails() {
        return this.mFilteredScanDetails;
    }

    public void setUserPreferredBand(int band) {
        this.mUserPreferedBand = band;
    }

    WifiQualifiedNetworkSelector(WifiConfigManager configureStore, Context context, WifiInfo wifiInfo, Clock clock) {
        this.mWifiConfigManager = configureStore;
        this.mWifiInfo = wifiInfo;
        this.mClock = clock;
        this.mScoreManager = (NetworkScoreManager) context.getSystemService("network_score");
        if (this.mScoreManager != null) {
            this.mNetworkScoreCache = new WifiNetworkScoreCache(context);
            this.mScoreManager.registerNetworkScoreCache(1, this.mNetworkScoreCache);
        } else {
            localLoge("No network score service: Couldn't register as a WiFi score Manager, type=1 service= network_score");
            this.mNetworkScoreCache = null;
        }
        this.mRssiScoreSlope = context.getResources().getInteger(17694746);
        this.mRssiScoreOffset = context.getResources().getInteger(17694745);
        this.mSameBssidAward = context.getResources().getInteger(17694747);
        this.mLastSelectionAward = context.getResources().getInteger(17694748);
        this.mPasspointSecurityAward = context.getResources().getInteger(17694749);
        this.mSecurityAward = context.getResources().getInteger(17694750);
        this.mNoIntnetPenalty = (((((this.mWifiConfigManager.mThresholdSaturatedRssi24.get() + this.mRssiScoreOffset) * this.mRssiScoreSlope) + this.mWifiConfigManager.mBandAward5Ghz.get()) + this.mWifiConfigManager.mCurrentNetworkBoost.get()) + this.mSameBssidAward) + this.mSecurityAward;
    }

    void enableVerboseLogging(int verbose) {
        this.mDbg = false;
    }

    private String getNetworkString(WifiConfiguration network) {
        if (network == null) {
            return null;
        }
        return network.SSID + ":" + network.networkId;
    }

    private boolean isNetworkQualified(WifiConfiguration currentNetwork) {
        if (currentNetwork == null) {
            localLog("Disconnected");
            return false;
        }
        localLog("Current network is: " + currentNetwork.SSID + " ,ID is: " + currentNetwork.networkId);
        if (currentNetwork.ephemeral) {
            localLog("Current is ephemeral. Start reselect");
            return false;
        } else if (this.mWifiConfigManager.isOpenNetwork(currentNetwork)) {
            localLog("Current network is open network");
            return false;
        } else if (!this.mWifiInfo.is24GHz() || this.mUserPreferedBand == 2) {
            int currentRssi = this.mWifiInfo.getRssi();
            if ((!this.mWifiInfo.is24GHz() || currentRssi >= this.mWifiConfigManager.mThresholdQualifiedRssi24.get()) && (!this.mWifiInfo.is5GHz() || currentRssi >= this.mWifiConfigManager.mThresholdQualifiedRssi5.get())) {
                return true;
            }
            localLog("Current band = " + (this.mWifiInfo.is24GHz() ? "2.4GHz band" : "5GHz band") + "current RSSI is: " + currentRssi);
            return false;
        } else {
            String str;
            StringBuilder append = new StringBuilder().append("Current band dose not match user preference. Start Qualified Network Selection Current band = ");
            if (this.mWifiInfo.is24GHz()) {
                str = "2.4GHz band";
            } else {
                str = "5GHz band";
            }
            localLog(append.append(str).append("UserPreference band = ").append(this.mUserPreferedBand).toString());
            return false;
        }
    }

    private boolean needQualifiedNetworkSelection(boolean isLinkDebouncing, boolean isConnected, boolean isDisconnected, boolean isSupplicantTransientState) {
        if (this.mScanDetails.size() == 0) {
            localLog("empty scan result");
            return false;
        } else if (isLinkDebouncing) {
            localLog("Need not Qualified Network Selection during L2 debouncing");
            return false;
        } else if (this.skipQualifiedNetworkSelectionForAutoConnect) {
            localLog("Skip network selction, since auto connection disabled");
            return false;
        } else if (isConnected) {
            if (this.mWifiConfigManager.getEnableAutoJoinWhenAssociated()) {
                if (this.mLastQualifiedNetworkSelectionTimeStamp != -1) {
                    long gap = this.mClock.elapsedRealtime() - this.mLastQualifiedNetworkSelectionTimeStamp;
                    if (gap < 10000) {
                        localLog("Too short to last successful Qualified Network Selection Gap is:" + gap + " ms!");
                        return false;
                    }
                }
                if (this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId()) == null || isNetworkQualified(this.mCurrentConnectedNetwork)) {
                    return false;
                }
                localLog("Current network is not qualified");
                return true;
            }
            localLog("Switch network under connection is not allowed");
            return false;
        } else if (isDisconnected) {
            this.mCurrentConnectedNetwork = null;
            this.mCurrentBssid = null;
            return !isSupplicantTransientState;
        } else {
            localLog("WifiStateMachine is not on connected or disconnected state");
            return false;
        }
    }

    int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, boolean sameBssid, boolean sameSelect, StringBuffer sbuf) {
        int score = ((this.mRssiScoreOffset + (scanResult.level <= this.mWifiConfigManager.mThresholdSaturatedRssi24.get() ? scanResult.level : this.mWifiConfigManager.mThresholdSaturatedRssi24.get())) * this.mRssiScoreSlope) + 0;
        sbuf.append(" RSSI score: " + score);
        if (scanResult.is5GHz()) {
            score += this.mWifiConfigManager.mBandAward5Ghz.get();
            sbuf.append(" 5GHz bonus: " + this.mWifiConfigManager.mBandAward5Ghz.get());
        }
        if (sameSelect) {
            long timeDifference = this.mClock.elapsedRealtime() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = this.mLastSelectionAward - ((int) ((timeDifference / 1000) / 60));
                score += bonus > 0 ? bonus : 0;
                sbuf.append(" User selected it last time " + ((timeDifference / 1000) / 60) + " minutes ago, bonus:" + bonus);
            }
        }
        if (network == currentNetwork || network.isLinked(currentNetwork)) {
            score += this.mWifiConfigManager.mCurrentNetworkBoost.get();
            sbuf.append(" Same network with current associated. Bonus: " + this.mWifiConfigManager.mCurrentNetworkBoost.get());
        }
        if (sameBssid) {
            score += this.mSameBssidAward;
            sbuf.append(" Same BSSID with current association. Bonus: " + this.mSameBssidAward);
        }
        if (network.isPasspoint()) {
            score += this.mPasspointSecurityAward;
            sbuf.append(" Passpoint Bonus:" + this.mPasspointSecurityAward);
        } else if (!this.mWifiConfigManager.isOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network Bonus:" + this.mSecurityAward);
        }
        if (network.numNoInternetAccessReports > 0 && !network.validatedInternetAccess) {
            score -= this.mNoIntnetPenalty;
            sbuf.append(" No internet Penalty:-" + this.mNoIntnetPenalty);
        }
        sbuf.append(" Score for scanResult: " + scanResult + " and Network ID: " + network.networkId + " final score:" + score + "\n\n");
        return score;
    }

    private void updateSavedNetworkSelectionStatus() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks();
        if (savedNetworks.size() == 0) {
            localLog("no saved network");
            return;
        }
        StringBuffer sbuf = new StringBuffer("Saved Network List\n");
        for (WifiConfiguration network : savedNetworks) {
            NetworkSelectionStatus status = this.mWifiConfigManager.getWifiConfiguration(network.networkId).getNetworkSelectionStatus();
            if (status.isNetworkTemporaryDisabled()) {
                this.mWifiConfigManager.tryEnableQualifiedNetwork(network.networkId);
            }
            status.setCandidate(null);
            status.setCandidateScore(Integer.MIN_VALUE);
            status.setSeenInLastQualifiedNetworkSelection(false);
            sbuf.append("    " + getNetworkString(network) + " " + " User Preferred BSSID:" + network.BSSID + " FQDN:" + network.FQDN + " " + status.getNetworkStatusString() + " Disable account: ");
            for (int index = 0; index < 11; index++) {
                sbuf.append(status.getDisableReasonCounter(index) + " ");
            }
            sbuf.append("Connect Choice:" + status.getConnectChoice() + " set time:" + status.getConnectChoiceTimestamp());
            sbuf.append("\n");
        }
        localLog(sbuf.toString());
    }

    public boolean userSelectNetwork(int netId, boolean persist) {
        WifiConfiguration selected = this.mWifiConfigManager.getWifiConfiguration(netId);
        localLog("userSelectNetwork:" + netId + " persist:" + persist);
        if (selected == null || selected.SSID == null) {
            localLoge("userSelectNetwork: Bad configuration with nid=" + netId);
            return false;
        }
        if (!selected.getNetworkSelectionStatus().isNetworkEnabled()) {
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, 0);
        }
        if (persist) {
            boolean change = false;
            String key = selected.configKey();
            long currentTime = this.mClock.currentTimeMillis();
            for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
                WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(network.networkId);
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                if (config.networkId == selected.networkId) {
                    if (status.getConnectChoice() != null) {
                        localLog("Remove user selection preference of " + status.getConnectChoice() + " Set Time: " + status.getConnectChoiceTimestamp() + " from " + config.SSID + " : " + config.networkId);
                        status.setConnectChoice(null);
                        status.setConnectChoiceTimestamp(-1);
                        change = true;
                    }
                } else if (status.getSeenInLastQualifiedNetworkSelection() && (status.getConnectChoice() == null || !status.getConnectChoice().equals(key))) {
                    localLog("Add key:" + key + " Set Time: " + currentTime + " to " + getNetworkString(config));
                    status.setConnectChoice(key);
                    status.setConnectChoiceTimestamp(currentTime);
                    change = true;
                }
            }
            if (this.mWifiFwkExt != null && this.mWifiFwkExt.hasNetworkSelection() == 3) {
                if (this.mLastUserSelectSecuredNetworkForOP03 == null) {
                    this.mLastUserSelectSecuredNetworkForOP03 = selected;
                } else {
                    if (compareSecurity(selected, this.mLastUserSelectSecuredNetworkForOP03) <= 0) {
                        this.mLastUserSelectSecuredNetworkForOP03 = selected;
                    }
                }
                localLog("OP_03 mLastUserSelectSecuredNetworkForOP03:" + this.mLastUserSelectSecuredNetworkForOP03.SSID);
            }
            if (!change) {
                return false;
            }
            this.mWifiConfigManager.writeKnownNetworkHistory();
            return true;
        }
        localLog("User has no privilege to overwrite the current priority");
        return false;
    }

    public boolean enableBssidForQualityNetworkSelection(String bssid, boolean enable) {
        boolean z = true;
        if (enable) {
            if (this.mBssidBlacklist.remove(bssid) == null) {
                z = false;
            }
            return z;
        }
        if (bssid != null) {
            BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
            if (status == null) {
                BssidBlacklistStatus newStatus = new BssidBlacklistStatus();
                newStatus.mCounter++;
                this.mBssidBlacklist.put(bssid, newStatus);
            } else if (!status.mIsBlacklisted) {
                status.mCounter++;
                if (status.mCounter >= 3) {
                    status.mIsBlacklisted = true;
                    status.mBlacklistedTimeStamp = this.mClock.elapsedRealtime();
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBssidBlacklist() {
        Iterator<BssidBlacklistStatus> iter = this.mBssidBlacklist.values().iterator();
        while (iter.hasNext()) {
            BssidBlacklistStatus status = (BssidBlacklistStatus) iter.next();
            if (status != null && status.mIsBlacklisted && this.mClock.elapsedRealtime() - status.mBlacklistedTimeStamp >= 300000) {
                iter.remove();
            }
        }
    }

    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
        return status == null ? false : status.mIsBlacklisted;
    }

    public WifiConfiguration selectQualifiedNetwork(boolean forceSelectNetwork, boolean isUntrustedConnectionsAllowed, List<ScanDetail> scanDetails, boolean isLinkDebouncing, boolean isConnected, boolean isDisconnected, boolean isSupplicantTransient) {
        localLog("==========start qualified Network Selection==========");
        this.mScanDetails = scanDetails;
        if (this.mDbg) {
            StringBuffer dumpScanResult = new StringBuffer();
            for (ScanDetail scanDetail : this.mScanDetails) {
                dumpScanResult.append(toScanId(scanDetail.getScanResult()) + " / ");
            }
            localLog(dumpScanResult + " dump for scanDetails at begin of selectQualifiedNetwork()\n");
        }
        List<Pair<ScanDetail, WifiConfiguration>> filteredScanDetails = new ArrayList();
        if (this.mCurrentConnectedNetwork == null) {
            this.mCurrentConnectedNetwork = this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId());
        }
        this.mCurrentBssid = this.mWifiInfo.getBSSID();
        if (forceSelectNetwork || needQualifiedNetworkSelection(isLinkDebouncing, isConnected, isDisconnected, isSupplicantTransient)) {
            int currentHighestScore = Integer.MIN_VALUE;
            ScanResult scanResultCandidate = null;
            WifiConfiguration networkCandidate = null;
            ExternalScoreEvaluator externalScoreEvaluator = new ExternalScoreEvaluator(this.mLocalLog, this.mDbg);
            WifiConfiguration lastUserSelectedNetwork = this.mWifiConfigManager.getWifiConfiguration(this.mWifiConfigManager.getLastSelectedConfiguration());
            if (lastUserSelectedNetwork != null) {
                localLog("Last selection is " + lastUserSelectedNetwork.SSID + " Time to now: " + (((this.mClock.elapsedRealtime() - this.mWifiConfigManager.getLastSelectedTimeStamp()) / 1000) / 60) + " minutes");
            }
            updateSavedNetworkSelectionStatus();
            updateBssidBlacklist();
            StringBuffer lowSignalScan = new StringBuffer();
            StringBuffer notSavedScan = new StringBuffer();
            StringBuffer noValidSsid = new StringBuffer();
            StringBuffer scoreHistory = new StringBuffer();
            ArrayList<NetworkKey> unscoredNetworks = new ArrayList();
            for (ScanDetail scanDetail2 : this.mScanDetails) {
                ScanResult scanResult = scanDetail2.getScanResult();
                if (scanResult.SSID != null && !TextUtils.isEmpty(scanResult.SSID)) {
                    String scanId = toScanId(scanResult);
                    if (!this.mWifiConfigManager.isBssidBlacklisted(scanResult.BSSID)) {
                        if (!isBssidDisabled(scanResult.BSSID)) {
                            if ((!scanResult.is24GHz() || scanResult.level >= this.mWifiConfigManager.mThresholdMinimumRssi24.get()) && (!scanResult.is5GHz() || scanResult.level >= this.mWifiConfigManager.mThresholdMinimumRssi5.get())) {
                                WifiConfiguration network;
                                if (!(this.mNetworkScoreCache == null || this.mNetworkScoreCache.isScoredNetwork(scanResult))) {
                                    try {
                                        unscoredNetworks.add(new NetworkKey(new WifiKey("\"" + scanResult.SSID + "\"", scanResult.BSSID)));
                                    } catch (IllegalArgumentException e) {
                                        Log.w(TAG, "Invalid SSID=" + scanResult.SSID + " BSSID=" + scanResult.BSSID + " for network score. Skip.");
                                    }
                                }
                                boolean potentiallyEphemeral = false;
                                WifiConfiguration potentialEphemeralCandidate = null;
                                WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
                                boolean z = (isSupplicantTransient || isConnected) ? true : isLinkDebouncing;
                                List<WifiConfiguration> associatedWifiConfigurations = wifiConfigManager.updateSavedNetworkWithNewScanDetail(scanDetail2, z);
                                if (associatedWifiConfigurations == null) {
                                    potentiallyEphemeral = true;
                                    if (this.mDbg) {
                                        notSavedScan.append(scanId + " / ");
                                    }
                                } else if (associatedWifiConfigurations.size() == 1) {
                                    network = (WifiConfiguration) associatedWifiConfigurations.get(0);
                                    if (network.ephemeral) {
                                        potentialEphemeralCandidate = network;
                                        potentiallyEphemeral = true;
                                    }
                                }
                                if (!potentiallyEphemeral) {
                                    int highestScore = Integer.MIN_VALUE;
                                    WifiConfiguration configurationCandidateForThisScan = null;
                                    WifiConfiguration potentialCandidate = null;
                                    for (WifiConfiguration network2 : associatedWifiConfigurations) {
                                        NetworkSelectionStatus status = network2.getNetworkSelectionStatus();
                                        status.setSeenInLastQualifiedNetworkSelection(true);
                                        if (potentialCandidate == null) {
                                            potentialCandidate = network2;
                                        }
                                        if (!status.isNetworkEnabled()) {
                                            localLog("SSID[" + network2.SSID + "] skips calculateBssidScore process due to" + " network is not enabled(" + status.getNetworkDisableReasonString() + ")");
                                        } else if (network2.BSSID == null || network2.BSSID.equals(WifiLastResortWatchdog.BSSID_ANY) || network2.BSSID.equals(scanResult.BSSID)) {
                                            int order;
                                            if (!(this.mWifiFwkExt == null || this.mWifiFwkExt.hasNetworkSelection() == 0 || this.mWifiFwkExt.hasNetworkSelection() == 3)) {
                                                order = 0;
                                                if (this.mWifiFwkExt.hasNetworkSelection() == 1) {
                                                    order = 0;
                                                }
                                                localLog(" hasNetworkSelection order = " + order);
                                                if (null == null) {
                                                    if (potentialCandidate.priority > network2.priority) {
                                                        localLog("network select -1" + potentialCandidate.configKey() + " over " + network2.configKey() + " due to priority");
                                                        order = -1;
                                                    } else if (potentialCandidate.priority < network2.priority) {
                                                        localLog("network select +1" + network2.configKey() + " over " + potentialCandidate.configKey() + " due to priority");
                                                        order = 1;
                                                    }
                                                }
                                                if (order > 0) {
                                                    configurationCandidateForThisScan = network2;
                                                    potentialCandidate = network2;
                                                    status.setCandidate(scanResult);
                                                }
                                            }
                                            if (network2.useExternalScores) {
                                                localLog("Skip calculateBssidScore process due to use ExternalScores.");
                                                externalScoreEvaluator.evalSavedCandidate(getNetworkScore(scanResult, false), network2, scanResult);
                                            } else {
                                                boolean z2;
                                                WifiConfiguration wifiConfiguration = this.mCurrentConnectedNetwork;
                                                boolean equals = this.mCurrentBssid == null ? false : this.mCurrentBssid.equals(scanResult.BSSID);
                                                if (lastUserSelectedNetwork != null && lastUserSelectedNetwork.networkId == network2.networkId) {
                                                    z2 = true;
                                                } else {
                                                    z2 = false;
                                                }
                                                int score = calculateBssidScore(scanResult, network2, wifiConfiguration, equals, z2, scoreHistory);
                                                if (!(this.mWifiFwkExt == null || this.mWifiFwkExt.hasNetworkSelection() != 3 || networkCandidate == null)) {
                                                    if (this.mLastUserSelectSecuredNetworkForOP03 == null || !this.mLastUserSelectSecuredNetworkForOP03.SSID.equals(networkCandidate.SSID)) {
                                                        order = compareSecurity(network2, networkCandidate);
                                                        localLog("OP_03:compareSecurity(" + network2.SSID + "," + networkCandidate.SSID + "):" + order);
                                                        if (order < 0) {
                                                            localLog(network2.SSID + " security priority higher than networkCandidate" + ", reset the highestScore!");
                                                            highestScore = Integer.MIN_VALUE;
                                                            status.setCandidateScore(Integer.MIN_VALUE);
                                                            currentHighestScore = Integer.MIN_VALUE;
                                                        } else if (order > 0) {
                                                            localLog(network2.SSID + " security priority lower than networkCandidate" + ", skip the highestScore update process!");
                                                        } else if (this.mLastUserSelectSecuredNetworkForOP03 != null && this.mLastUserSelectSecuredNetworkForOP03.SSID.equals(network2.SSID)) {
                                                            localLog(network2.SSID + " is the last user select secured network" + ", reset the highestScore forcely!");
                                                            highestScore = Integer.MIN_VALUE;
                                                            status.setCandidateScore(Integer.MIN_VALUE);
                                                            currentHighestScore = Integer.MIN_VALUE;
                                                        }
                                                    } else {
                                                        localLog(networkCandidate.SSID + " is the last user select secured network" + ", skip all the highestScore update process!");
                                                    }
                                                }
                                                if (score > highestScore) {
                                                    highestScore = score;
                                                    configurationCandidateForThisScan = network2;
                                                    potentialCandidate = network2;
                                                }
                                                if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                                                    status.setCandidate(scanResult);
                                                    status.setCandidateScore(score);
                                                }
                                            }
                                        } else {
                                            localLog("Network: " + getNetworkString(network2) + " has specified" + "BSSID:" + network2.BSSID + ". Skip " + scanResult.BSSID);
                                        }
                                    }
                                    filteredScanDetails.add(Pair.create(scanDetail2, potentialCandidate));
                                    if (highestScore > currentHighestScore || (highestScore == currentHighestScore && scanResultCandidate != null && scanResult.level > scanResultCandidate.level)) {
                                        currentHighestScore = highestScore;
                                        scanResultCandidate = scanResult;
                                        networkCandidate = configurationCandidateForThisScan;
                                        localLog("networkCandidate updated to " + networkCandidate.SSID);
                                        networkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                                    }
                                } else if (isUntrustedConnectionsAllowed) {
                                    Integer netScore = getNetworkScore(scanResult, false);
                                    if (!(netScore == null || this.mWifiConfigManager.wasEphemeralNetworkDeleted(scanResult.SSID))) {
                                        externalScoreEvaluator.evalUntrustedCandidate(netScore, scanResult);
                                        filteredScanDetails.add(Pair.create(scanDetail2, potentialEphemeralCandidate));
                                    }
                                }
                            } else if (this.mDbg) {
                                lowSignalScan.append(scanId + "(" + (scanResult.is24GHz() ? "2.4GHz" : "5GHz") + ")" + scanResult.level + " / ");
                            }
                        }
                    }
                    Log.e(TAG, scanId + " is in blacklist.");
                } else if (this.mDbg) {
                    noValidSsid.append(scanResult.BSSID + " / ");
                }
            }
            this.mFilteredScanDetails = filteredScanDetails;
            if (!(this.mScoreManager == null || unscoredNetworks.size() == 0)) {
                this.mScoreManager.requestScores((NetworkKey[]) unscoredNetworks.toArray(new NetworkKey[unscoredNetworks.size()]));
            }
            if (this.mDbg) {
                localLog(lowSignalScan + " skipped due to low signal\n");
                localLog(notSavedScan + " skipped due to not saved\n ");
                localLog(noValidSsid + " skipped due to not valid SSID\n");
                localLog(scoreHistory.toString());
            }
            if (scanResultCandidate != null) {
                localLog("HighestScoreCandate:" + networkCandidate.SSID + " ConnectChoice:" + networkCandidate.getNetworkSelectionStatus().getConnectChoice());
                WifiConfiguration tempConfig = networkCandidate;
                while (tempConfig.getNetworkSelectionStatus().getConnectChoice() != null) {
                    String key = tempConfig.getNetworkSelectionStatus().getConnectChoice();
                    tempConfig = this.mWifiConfigManager.getWifiConfiguration(key);
                    if (tempConfig == null) {
                        localLoge("Connect choice: " + key + " has no corresponding saved config");
                        break;
                    }
                    NetworkSelectionStatus tempStatus = tempConfig.getNetworkSelectionStatus();
                    if (tempStatus.getCandidate() != null && tempStatus.isNetworkEnabled()) {
                        scanResultCandidate = tempStatus.getCandidate();
                        networkCandidate = tempConfig;
                    }
                }
                localLog("After user choice adjust, the final candidate is:" + getNetworkString(networkCandidate) + " : " + scanResultCandidate.BSSID);
            }
            if (scanResultCandidate == null) {
                localLog("Checking the externalScoreEvaluator for candidates...");
                networkCandidate = getExternalScoreCandidate(externalScoreEvaluator);
                if (networkCandidate != null) {
                    scanResultCandidate = networkCandidate.getNetworkSelectionStatus().getCandidate();
                }
            }
            if (scanResultCandidate == null) {
                localLog("Can not find any suitable candidates");
                return null;
            }
            String currentAssociationId;
            if (this.mCurrentConnectedNetwork == null) {
                currentAssociationId = "Disconnected";
            } else {
                currentAssociationId = getNetworkString(this.mCurrentConnectedNetwork);
            }
            String targetAssociationId = getNetworkString(networkCandidate);
            if (networkCandidate.isPasspoint()) {
                networkCandidate.SSID = "\"" + scanResultCandidate.SSID + "\"";
            }
            if (scanResultCandidate.BSSID.equals(this.mCurrentBssid)) {
                localLog(currentAssociationId + " is already the best choice!");
            } else if (this.mCurrentConnectedNetwork == null || !(this.mCurrentConnectedNetwork.networkId == networkCandidate.networkId || this.mCurrentConnectedNetwork.isLinked(networkCandidate))) {
                localLog("reconnect from " + currentAssociationId + " to " + targetAssociationId);
            } else {
                localLog("Roaming from " + currentAssociationId + " to " + targetAssociationId);
            }
            this.mCurrentBssid = scanResultCandidate.BSSID;
            this.mCurrentConnectedNetwork = networkCandidate;
            this.mLastQualifiedNetworkSelectionTimeStamp = this.mClock.elapsedRealtime();
            return networkCandidate;
        }
        localLog("Quit qualified Network Selection since it is not forced and current network is qualified already");
        this.mFilteredScanDetails = filteredScanDetails;
        return null;
    }

    private int compareSecurity(WifiConfiguration candidate1, WifiConfiguration candidate2) {
        int candidate1Security = 0;
        int candidate2Security = 0;
        if (candidate1 == null || candidate2 == null) {
            return 0;
        }
        if (candidate1.allowedKeyManagement.get(1) || candidate1.allowedKeyManagement.get(2) || candidate1.allowedKeyManagement.get(3) || candidate1.allowedKeyManagement.get(6) || candidate1.allowedKeyManagement.get(6) || candidate1.allowedKeyManagement.get(4)) {
            candidate1Security = 2;
        } else if (candidate1.wepTxKeyIndex >= 0 && candidate1.wepTxKeyIndex < candidate1.wepKeys.length && candidate1.wepKeys[candidate1.wepTxKeyIndex] != null) {
            candidate1Security = 1;
        }
        if (candidate2.allowedKeyManagement.get(1) || candidate2.allowedKeyManagement.get(2) || candidate2.allowedKeyManagement.get(3) || candidate2.allowedKeyManagement.get(6) || candidate2.allowedKeyManagement.get(6) || candidate2.allowedKeyManagement.get(4)) {
            candidate2Security = 2;
        } else if (candidate2.wepTxKeyIndex >= 0 && candidate2.wepTxKeyIndex < candidate2.wepKeys.length && candidate2.wepKeys[candidate2.wepTxKeyIndex] != null) {
            candidate2Security = 1;
        }
        if (candidate1Security > candidate2Security) {
            return -1;
        }
        return candidate1Security < candidate2Security ? 1 : 0;
    }

    WifiConfiguration getExternalScoreCandidate(ExternalScoreEvaluator scoreEvaluator) {
        switch (scoreEvaluator.getBestCandidateType()) {
            case 0:
                localLog("ExternalScoreEvaluator did not see any good candidates.");
                return null;
            case 1:
                ScanResult scanResultCandidate = scoreEvaluator.getScanResultCandidate();
                WifiConfiguration networkCandidate = scoreEvaluator.getSavedConfig();
                networkCandidate.getNetworkSelectionStatus().setCandidate(scanResultCandidate);
                localLog(String.format("new scored candidate %s network ID:%d", new Object[]{toScanId(scanResultCandidate), Integer.valueOf(networkCandidate.networkId)}));
                return networkCandidate;
            case 2:
                ScanResult untrustedScanResultCandidate = scoreEvaluator.getScanResultCandidate();
                WifiConfiguration unTrustedNetworkCandidate = this.mWifiConfigManager.wifiConfigurationFromScanResult(untrustedScanResultCandidate);
                unTrustedNetworkCandidate.ephemeral = true;
                if (this.mNetworkScoreCache != null) {
                    unTrustedNetworkCandidate.meteredHint = this.mNetworkScoreCache.getMeteredHint(untrustedScanResultCandidate);
                }
                this.mWifiConfigManager.saveNetwork(unTrustedNetworkCandidate, -1);
                localLog(String.format("new ephemeral candidate %s network ID:%d, meteredHint=%b", new Object[]{toScanId(untrustedScanResultCandidate), Integer.valueOf(unTrustedNetworkCandidate.networkId), Boolean.valueOf(unTrustedNetworkCandidate.meteredHint)}));
                unTrustedNetworkCandidate.getNetworkSelectionStatus().setCandidate(untrustedScanResultCandidate);
                return unTrustedNetworkCandidate;
            default:
                localLoge("Unhandled ExternalScoreEvaluator case. No candidate selected.");
                return null;
        }
    }

    Integer getNetworkScore(ScanResult scanResult, boolean isActiveNetwork) {
        if (this.mNetworkScoreCache == null || !this.mNetworkScoreCache.isScoredNetwork(scanResult)) {
            return null;
        }
        int networkScore = this.mNetworkScoreCache.getNetworkScore(scanResult, isActiveNetwork);
        localLog(toScanId(scanResult) + " has score: " + networkScore);
        return Integer.valueOf(networkScore);
    }

    private static String toScanId(ScanResult scanResult) {
        if (scanResult == null) {
            return "NULL";
        }
        return String.format("%s:%s", new Object[]{scanResult.SSID, scanResult.BSSID});
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiQualifiedNetworkSelector");
        pw.println("WifiQualifiedNetworkSelector - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiQualifiedNetworkSelector - Log End ----");
    }
}
