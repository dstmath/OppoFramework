package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.util.LocalLog;
import android.util.Pair;
import com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator;
import com.android.server.wifi.util.TelephonyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedNetworkEvaluator implements NetworkEvaluator {
    private static final String NAME = "SavedNetworkEvaluator";
    private final int mBand5GHzAward;
    private final Clock mClock;
    private final WifiConnectivityHelper mConnectivityHelper;
    private final int mLastSelectionAward;
    private final LocalLog mLocalLog;
    private final int mRssiScoreOffset;
    private final int mRssiScoreSlope;
    private final int mSameBssidAward;
    private final int mSameNetworkAward;
    private final int mSecurityAward;
    private final int mThresholdSaturatedRssi24;
    private final int mThresholdSaturatedRssi5;
    private final WifiConfigManager mWifiConfigManager;

    SavedNetworkEvaluator(Context context, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiConnectivityHelper connectivityHelper) {
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mConnectivityHelper = connectivityHelper;
        this.mRssiScoreSlope = context.getResources().getInteger(17694881);
        this.mRssiScoreOffset = context.getResources().getInteger(17694880);
        this.mSameBssidAward = context.getResources().getInteger(17694882);
        this.mSameNetworkAward = context.getResources().getInteger(17694892);
        this.mLastSelectionAward = context.getResources().getInteger(17694878);
        this.mSecurityAward = context.getResources().getInteger(17694883);
        this.mBand5GHzAward = context.getResources().getInteger(17694875);
        this.mThresholdSaturatedRssi24 = context.getResources().getInteger(17694910);
        this.mThresholdSaturatedRssi5 = context.getResources().getInteger(17694911);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    public String getName() {
        return NAME;
    }

    private void updateSavedNetworkSelectionStatus() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks();
        if (savedNetworks.size() == 0) {
            localLog("No saved networks.");
            return;
        }
        StringBuffer sbuf = new StringBuffer();
        for (WifiConfiguration network : savedNetworks) {
            if (!network.isPasspoint()) {
                this.mWifiConfigManager.tryEnableNetwork(network.networkId);
                this.mWifiConfigManager.clearNetworkCandidateScanResult(network.networkId);
                NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                if (!status.isNetworkEnabled()) {
                    sbuf.append("  ").append(WifiNetworkSelector.toNetworkString(network)).append(" ");
                    for (int index = 1; index < 13; index++) {
                        int count = status.getDisableReasonCounter(index);
                        if (count > 0) {
                            sbuf.append("reason=").append(NetworkSelectionStatus.getNetworkDisableReasonString(index)).append(", count=").append(count).append("; ");
                        }
                    }
                    sbuf.append("\n");
                }
            }
        }
        if (sbuf.length() > 0) {
            localLog("Disabled saved networks:");
            localLog(sbuf.toString());
        }
    }

    public void update(List<ScanDetail> list) {
        updateSavedNetworkSelectionStatus();
    }

    private int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, String currentBssid, StringBuffer sbuf) {
        int rssi;
        boolean is5GHz = scanResult.is5GHz();
        sbuf.append("[ ").append(scanResult.SSID).append(" ").append(scanResult.BSSID).append(" RSSI:").append(scanResult.level).append(" ] ");
        int rssiSaturationThreshold = is5GHz ? this.mThresholdSaturatedRssi5 : this.mThresholdSaturatedRssi24;
        if (scanResult.level < rssiSaturationThreshold) {
            rssi = scanResult.level;
        } else {
            rssi = rssiSaturationThreshold;
        }
        int score = ((this.mRssiScoreOffset + rssi) * this.mRssiScoreSlope) + 0;
        sbuf.append(" RSSI score: ").append(score).append(",");
        if (is5GHz) {
            score += this.mBand5GHzAward;
            sbuf.append(" 5GHz bonus: ").append(this.mBand5GHzAward).append(",");
        }
        int lastUserSelectedNetworkId = this.mWifiConfigManager.getLastSelectedNetwork();
        if (lastUserSelectedNetworkId != -1 && lastUserSelectedNetworkId == network.networkId) {
            long timeDifference = this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = this.mLastSelectionAward - ((int) ((timeDifference / 1000) / 60));
                score += bonus > 0 ? bonus : 0;
                sbuf.append(" User selection ").append((timeDifference / 1000) / 60).append(" minutes ago, bonus: ").append(bonus).append(",");
            }
        }
        if (currentNetwork != null && network.networkId == currentNetwork.networkId) {
            score += this.mSameNetworkAward;
            sbuf.append(" Same network bonus: ").append(this.mSameNetworkAward).append(",");
            if (this.mConnectivityHelper.isFirmwareRoamingSupported() && currentBssid != null) {
                if ((currentBssid.equals(scanResult.BSSID) ^ 1) != 0) {
                    score += this.mSameBssidAward;
                    sbuf.append(" Equivalent BSSID bonus: ").append(this.mSameBssidAward).append(",");
                }
            }
        }
        if (currentBssid != null) {
            if (currentBssid.equals(scanResult.BSSID)) {
                score += this.mSameBssidAward;
                sbuf.append(" Same BSSID bonus: ").append(this.mSameBssidAward).append(",");
            }
        }
        if (!WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network bonus: ").append(this.mSecurityAward).append(",");
        }
        sbuf.append(" ## Total score: ").append(score).append("\n");
        return score;
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> connectableNetworks) {
        int highestScore = Integer.MIN_VALUE;
        ScanResult scanResultCandidate = null;
        WifiConfiguration candidate = null;
        StringBuffer scoreHistory = new StringBuffer();
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            int highestScoreOfScanResult = Integer.MIN_VALUE;
            int candidateIdOfScanResult = -1;
            if (this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) != null) {
                for (WifiConfiguration network : new ArrayList(Arrays.asList(new WifiConfiguration[]{this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail)}))) {
                    if (!(network.isPasspoint() || network.isEphemeral())) {
                        NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                        status.setSeenInLastQualifiedNetworkSelection(true);
                        if (status.isNetworkEnabled()) {
                            if (network.BSSID != null && (network.BSSID.equals("any") ^ 1) != 0 && (network.BSSID.equals(scanResult.BSSID) ^ 1) != 0) {
                                localLog("Network " + WifiNetworkSelector.toNetworkString(network) + " has specified BSSID " + network.BSSID + ". Skip " + scanResult.BSSID);
                            } else if (!TelephonyUtil.isSimConfig(network) || (this.mWifiConfigManager.isSimPresent() ^ 1) == 0) {
                                int score = calculateBssidScore(scanResult, network, currentNetwork, currentBssid, scoreHistory);
                                if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                                    this.mWifiConfigManager.setNetworkCandidateScanResult(network.networkId, scanResult, score);
                                }
                                if (network.useExternalScores) {
                                    localLog("Network " + WifiNetworkSelector.toNetworkString(network) + " has external score.");
                                } else if (score > highestScoreOfScanResult) {
                                    highestScoreOfScanResult = score;
                                    candidateIdOfScanResult = network.networkId;
                                }
                            }
                        }
                    }
                }
                if (connectableNetworks != null) {
                    connectableNetworks.add(Pair.create(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult)));
                }
                if (highestScoreOfScanResult > highestScore || (highestScoreOfScanResult == highestScore && scanResultCandidate != null && scanResult.level > scanResultCandidate.level)) {
                    highestScore = highestScoreOfScanResult;
                    scanResultCandidate = scanResult;
                    this.mWifiConfigManager.setNetworkCandidateScanResult(candidateIdOfScanResult, scanResult, highestScore);
                    candidate = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                }
            }
        }
        if (scoreHistory.length() > 0) {
            localLog("\n" + scoreHistory.toString());
        }
        if (scanResultCandidate == null) {
            localLog("did not see any good candidates.");
        }
        return candidate;
    }
}
