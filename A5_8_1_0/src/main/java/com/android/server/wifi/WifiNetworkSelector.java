package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkKey;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Pair;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WifiNetworkSelector {
    public static final int EVALUATOR_MIN_PRIORITY = 6;
    private static final long INVALID_TIME_STAMP = Long.MIN_VALUE;
    public static final int MAX_NUM_EVALUATORS = 6;
    public static final int MINIMUM_NETWORK_SELECTION_INTERVAL_MS = 10000;
    private static final String TAG = "WifiNetworkSelector";
    private final Clock mClock;
    private volatile List<Pair<ScanDetail, WifiConfiguration>> mConnectableNetworks = new ArrayList();
    private final boolean mEnableAutoJoinWhenAssociated;
    private final NetworkEvaluator[] mEvaluators = new NetworkEvaluator[6];
    private List<ScanDetail> mFilteredNetworks = new ArrayList();
    private long mLastNetworkSelectionTimeStamp = INVALID_TIME_STAMP;
    private final LocalLog mLocalLog;
    private boolean mSkipQualifiedNetworkSelectionForAutoConnect = true;
    private final int mStayOnNetworkMinimumRxRate;
    private final int mStayOnNetworkMinimumTxRate;
    private final int mThresholdMinimumRssi24;
    private final int mThresholdMinimumRssi5;
    private final int mThresholdQualifiedRssi24;
    private final int mThresholdQualifiedRssi5;
    private final WifiConfigManager mWifiConfigManager;

    public interface NetworkEvaluator {
        WifiConfiguration evaluateNetworks(List<ScanDetail> list, WifiConfiguration wifiConfiguration, String str, boolean z, boolean z2, List<Pair<ScanDetail, WifiConfiguration>> list2);

        String getName();

        void update(List<ScanDetail> list);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    private boolean isCurrentNetworkSufficient(WifiInfo wifiInfo, List<ScanDetail> scanDetails) {
        WifiConfiguration network = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        if (network == null) {
            localLog("No current connected network.");
            return false;
        }
        localLog("Current connected network: " + network.SSID + " , ID: " + network.networkId);
        int currentRssi = wifiInfo.getRssi();
        boolean hasQualifiedRssi = (!wifiInfo.is24GHz() || currentRssi <= this.mThresholdQualifiedRssi24) ? wifiInfo.is5GHz() && currentRssi > this.mThresholdQualifiedRssi5 : true;
        boolean hasActiveStream = wifiInfo.getTxSuccessRatePps() <= ((double) this.mStayOnNetworkMinimumTxRate) ? wifiInfo.getRxSuccessRatePps() > ((double) this.mStayOnNetworkMinimumRxRate) : true;
        if (hasQualifiedRssi && hasActiveStream) {
            localLog("Stay on current network because of good RSSI and ongoing traffic");
            return true;
        } else if (network.ephemeral) {
            localLog("Current network is an ephemeral one.");
            return false;
        } else if (WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            localLog("Current network is a open one.");
            return false;
        } else if (wifiInfo.is24GHz() && is5GHzNetworkAvailable(scanDetails)) {
            localLog("Current network is 2.4GHz. 5GHz networks available.");
            return false;
        } else if (hasQualifiedRssi) {
            return true;
        } else {
            localLog("Current network RSSI[" + currentRssi + "]-acceptable but not qualified.");
            return false;
        }
    }

    private boolean is5GHzNetworkAvailable(List<ScanDetail> scanDetails) {
        for (ScanDetail detail : scanDetails) {
            if (detail.getScanResult().is5GHz()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkSelectionNeeded(List<ScanDetail> scanDetails, WifiInfo wifiInfo, boolean connected, boolean disconnected) {
        if (scanDetails.size() == 0) {
            localLog("Empty connectivity scan results. Skip network selection.");
            return false;
        } else if (connected) {
            if (this.mEnableAutoJoinWhenAssociated) {
                if (this.mLastNetworkSelectionTimeStamp != INVALID_TIME_STAMP) {
                    long gap = this.mClock.getElapsedSinceBootMillis() - this.mLastNetworkSelectionTimeStamp;
                    if (gap < 10000) {
                        localLog("Too short since last network selection: " + gap + " ms." + " Skip network selection.");
                        return false;
                    }
                }
                if (isCurrentNetworkSufficient(wifiInfo, scanDetails)) {
                    localLog("Current connected network already sufficient. Skip network selection.");
                    return false;
                }
                localLog("Current connected network is not sufficient.");
                return true;
            }
            localLog("Switching networks in connected state is not allowed. Skip network selection.");
            return false;
        } else if (disconnected) {
            return true;
        } else {
            localLog("WifiStateMachine is in neither CONNECTED nor DISCONNECTED state. Skip network selection.");
            return false;
        }
    }

    public static String toScanId(ScanResult scanResult) {
        if (scanResult == null) {
            return "NULL";
        }
        return String.format("%s:%s", new Object[]{scanResult.SSID, scanResult.BSSID});
    }

    public static String toNetworkString(WifiConfiguration network) {
        if (network == null) {
            return null;
        }
        return network.SSID + ":" + network.networkId;
    }

    public boolean isSignalTooWeak(ScanResult scanResult) {
        if (scanResult.is24GHz() && scanResult.level < this.mThresholdMinimumRssi24) {
            return true;
        }
        if (!scanResult.is5GHz() || scanResult.level >= this.mThresholdMinimumRssi5) {
            return false;
        }
        return true;
    }

    private List<ScanDetail> filterScanResults(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, boolean isConnected, String currentBssid) {
        ArrayList<NetworkKey> unscoredNetworks = new ArrayList();
        List<ScanDetail> validScanDetails = new ArrayList();
        StringBuffer noValidSsid = new StringBuffer();
        StringBuffer blacklistedBssid = new StringBuffer();
        StringBuffer lowRssi = new StringBuffer();
        boolean scanResultsHaveCurrentBssid = false;
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (TextUtils.isEmpty(scanResult.SSID)) {
                noValidSsid.append(scanResult.BSSID).append(" / ");
            } else {
                if (scanResult.BSSID.equals(currentBssid)) {
                    scanResultsHaveCurrentBssid = true;
                }
                String scanId = toScanId(scanResult);
                if (bssidBlacklist.contains(scanResult.BSSID)) {
                    blacklistedBssid.append(scanId).append(" / ");
                } else if (isSignalTooWeak(scanResult)) {
                    lowRssi.append(scanId).append("(").append(scanResult.is24GHz() ? "2.4GHz" : "5GHz").append(")").append(scanResult.level).append(" / ");
                } else {
                    validScanDetails.add(scanDetail);
                }
            }
        }
        if (!isConnected || (scanResultsHaveCurrentBssid ^ 1) == 0) {
            if (noValidSsid.length() != 0) {
                localLog("Networks filtered out due to invalid SSID: " + noValidSsid);
            }
            if (blacklistedBssid.length() != 0) {
                localLog("Networks filtered out due to blacklist: " + blacklistedBssid);
            }
            if (lowRssi.length() != 0) {
                localLog("Networks filtered out due to low signal strength: " + lowRssi);
            }
            return validScanDetails;
        }
        localLog("Current connected BSSID " + currentBssid + " is not in the scan results." + " Skip network selection.");
        validScanDetails.clear();
        return validScanDetails;
    }

    public List<ScanDetail> getFilteredScanDetailsForOpenUnsavedNetworks() {
        List<ScanDetail> openUnsavedNetworks = new ArrayList();
        for (ScanDetail scanDetail : this.mFilteredNetworks) {
            if (ScanResultUtil.isScanResultForOpenNetwork(scanDetail.getScanResult()) && this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) == null) {
                openUnsavedNetworks.add(scanDetail);
            }
        }
        return openUnsavedNetworks;
    }

    public List<Pair<ScanDetail, WifiConfiguration>> getConnectableScanDetails() {
        return this.mConnectableNetworks;
    }

    public boolean setUserConnectChoice(int netId) {
        localLog("userSelectNetwork: network ID=" + netId);
        WifiConfiguration selected = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (selected == null || selected.SSID == null) {
            localLog("userSelectNetwork: Invalid configuration with nid=" + netId);
            return false;
        }
        if (!selected.getNetworkSelectionStatus().isNetworkEnabled()) {
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, 0);
        }
        boolean change = false;
        String key = selected.configKey();
        long currentTime = this.mClock.getWallClockMillis();
        for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
            NetworkSelectionStatus status = network.getNetworkSelectionStatus();
            if (network.networkId == selected.networkId) {
                if (status.getConnectChoice() != null) {
                    localLog("Remove user selection preference of " + status.getConnectChoice() + " Set Time: " + status.getConnectChoiceTimestamp() + " from " + network.SSID + " : " + network.networkId);
                    this.mWifiConfigManager.clearNetworkConnectChoice(network.networkId);
                    change = true;
                }
            } else if (status.getSeenInLastQualifiedNetworkSelection() && (status.getConnectChoice() == null || (status.getConnectChoice().equals(key) ^ 1) != 0)) {
                localLog("Add key: " + key + " Set Time: " + currentTime + " to " + toNetworkString(network));
                this.mWifiConfigManager.setNetworkConnectChoice(network.networkId, key, currentTime);
                change = true;
            }
        }
        return change;
    }

    private WifiConfiguration overrideCandidateWithUserConnectChoice(WifiConfiguration candidate) {
        WifiConfiguration tempConfig = candidate;
        WifiConfiguration originalCandidate = candidate;
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        while (tempConfig.getNetworkSelectionStatus().getConnectChoice() != null) {
            String key = tempConfig.getNetworkSelectionStatus().getConnectChoice();
            tempConfig = this.mWifiConfigManager.getConfiguredNetwork(key);
            if (tempConfig == null) {
                localLog("Connect choice: " + key + " has no corresponding saved config.");
                break;
            }
            NetworkSelectionStatus tempStatus = tempConfig.getNetworkSelectionStatus();
            if (tempStatus.getCandidate() != null && tempStatus.isNetworkEnabled()) {
                scanResultCandidate = tempStatus.getCandidate();
                candidate = tempConfig;
            }
        }
        if (candidate != originalCandidate) {
            localLog("After user selection adjustment, the final candidate is:" + toNetworkString(candidate) + " : " + scanResultCandidate.BSSID);
        }
        return candidate;
    }

    public WifiConfiguration selectNetwork(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, WifiInfo wifiInfo, boolean connected, boolean disconnected, boolean untrustedNetworkAllowed) {
        if (this.mSkipQualifiedNetworkSelectionForAutoConnect) {
            return null;
        }
        this.mFilteredNetworks.clear();
        this.mConnectableNetworks.clear();
        if (scanDetails.size() == 0) {
            localLog("Empty connectivity scan result");
            return null;
        }
        WifiConfiguration currentNetwork = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        String currentBssid = wifiInfo.getBSSID();
        if (!isNetworkSelectionNeeded(scanDetails, wifiInfo, connected, disconnected)) {
            return null;
        }
        for (NetworkEvaluator registeredEvaluator : this.mEvaluators) {
            if (registeredEvaluator != null) {
                registeredEvaluator.update(scanDetails);
            }
        }
        this.mFilteredNetworks = filterScanResults(scanDetails, bssidBlacklist, connected, currentBssid);
        if (this.mFilteredNetworks.size() == 0) {
            return null;
        }
        WifiConfiguration selectedNetwork = null;
        for (NetworkEvaluator registeredEvaluator2 : this.mEvaluators) {
            if (registeredEvaluator2 != null) {
                localLog("About to run " + registeredEvaluator2.getName() + " :");
                selectedNetwork = registeredEvaluator2.evaluateNetworks(new ArrayList(this.mFilteredNetworks), currentNetwork, currentBssid, connected, untrustedNetworkAllowed, this.mConnectableNetworks);
                if (selectedNetwork != null) {
                    localLog(registeredEvaluator2.getName() + " selects " + toNetworkString(selectedNetwork) + " : " + selectedNetwork.getNetworkSelectionStatus().getCandidate().BSSID);
                    break;
                }
            }
        }
        if (selectedNetwork != null) {
            selectedNetwork = overrideCandidateWithUserConnectChoice(selectedNetwork);
            this.mLastNetworkSelectionTimeStamp = this.mClock.getElapsedSinceBootMillis();
        }
        return selectedNetwork;
    }

    public boolean registerNetworkEvaluator(NetworkEvaluator evaluator, int priority) {
        if (priority < 0 || priority >= 6) {
            localLog("Invalid network evaluator priority: " + priority);
            return false;
        } else if (this.mEvaluators[priority] != null) {
            localLog("Priority " + priority + " is already registered by " + this.mEvaluators[priority].getName());
            return false;
        } else {
            this.mEvaluators[priority] = evaluator;
            return true;
        }
    }

    WifiNetworkSelector(Context context, WifiConfigManager configManager, Clock clock, LocalLog localLog) {
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mThresholdQualifiedRssi24 = context.getResources().getInteger(17694912);
        this.mThresholdQualifiedRssi5 = context.getResources().getInteger(17694913);
        this.mThresholdMinimumRssi24 = context.getResources().getInteger(17694906);
        this.mThresholdMinimumRssi5 = context.getResources().getInteger(17694907);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957070);
        this.mStayOnNetworkMinimumTxRate = context.getResources().getInteger(17694897);
        this.mStayOnNetworkMinimumRxRate = context.getResources().getInteger(17694896);
    }
}
