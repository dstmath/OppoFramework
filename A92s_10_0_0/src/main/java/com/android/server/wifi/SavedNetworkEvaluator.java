package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.util.TelephonyUtil;
import java.util.List;

public class SavedNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    @VisibleForTesting
    public static final int LAST_SELECTION_AWARD_DECAY_MSEC = 60000;
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
    private final ScoringParams mScoringParams;
    private final int mSecurityAward;
    private final SubscriptionManager mSubscriptionManager;
    private final WifiConfigManager mWifiConfigManager;

    SavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiConnectivityHelper connectivityHelper, SubscriptionManager subscriptionManager) {
        this.mScoringParams = scoringParams;
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mConnectivityHelper = connectivityHelper;
        this.mSubscriptionManager = subscriptionManager;
        this.mRssiScoreSlope = context.getResources().getInteger(17694921);
        this.mRssiScoreOffset = context.getResources().getInteger(17694920);
        this.mSameBssidAward = context.getResources().getInteger(17694922);
        this.mSameNetworkAward = context.getResources().getInteger(17694932);
        this.mLastSelectionAward = context.getResources().getInteger(17694918);
        this.mSecurityAward = context.getResources().getInteger(17694923);
        this.mBand5GHzAward = context.getResources().getInteger(17694915);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
        Log.d(NAME, log);
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public int getId() {
        return 0;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public String getName() {
        return NAME;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public void update(List<ScanDetail> list) {
    }

    private int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, String currentBssid, StringBuffer sbuf) {
        boolean is5GHz = scanResult.is5GHz();
        sbuf.append("[ ");
        sbuf.append(scanResult.SSID);
        sbuf.append(" ");
        sbuf.append(scanResult.BSSID);
        sbuf.append(" RSSI:");
        sbuf.append(scanResult.level);
        sbuf.append(" ] ");
        int score = 0 + ((this.mRssiScoreOffset + Math.min(scanResult.level, this.mScoringParams.getGoodRssi(scanResult.frequency))) * this.mRssiScoreSlope);
        sbuf.append(" RSSI score: ");
        sbuf.append(score);
        sbuf.append(",");
        if (is5GHz) {
            score += this.mBand5GHzAward;
            sbuf.append(" 5GHz bonus: ");
            sbuf.append(this.mBand5GHzAward);
            sbuf.append(",");
        }
        int lastUserSelectedNetworkId = this.mWifiConfigManager.getLastSelectedNetwork();
        if (lastUserSelectedNetworkId != -1 && lastUserSelectedNetworkId == network.networkId) {
            long timeDifference = this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = Math.max(this.mLastSelectionAward - ((int) (timeDifference / 60000)), 0);
                score += bonus;
                sbuf.append(" User selection ");
                sbuf.append(timeDifference);
                sbuf.append(" ms ago, bonus: ");
                sbuf.append(bonus);
                sbuf.append(",");
            }
        }
        if (currentNetwork != null && network.networkId == currentNetwork.networkId) {
            score += this.mSameNetworkAward;
            sbuf.append(" Same network bonus: ");
            sbuf.append(this.mSameNetworkAward);
            sbuf.append(",");
            if (this.mConnectivityHelper.isFirmwareRoamingSupported() && currentBssid != null && !currentBssid.equals(scanResult.BSSID)) {
                score += this.mSameBssidAward;
                sbuf.append(" Equivalent BSSID bonus: ");
                sbuf.append(this.mSameBssidAward);
                sbuf.append(",");
            }
        }
        if (currentBssid != null && currentBssid.equals(scanResult.BSSID)) {
            score += this.mSameBssidAward;
            sbuf.append(" Same BSSID bonus: ");
            sbuf.append(this.mSameBssidAward);
            sbuf.append(",");
        }
        if (!WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network bonus: ");
            sbuf.append(this.mSecurityAward);
            sbuf.append(",");
        }
        sbuf.append(" ## Total score: ");
        sbuf.append(score);
        sbuf.append("\n");
        return score;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        StringBuffer scoreHistory = new StringBuffer();
        int highestScore = Integer.MIN_VALUE;
        ScanResult scanResultCandidate = null;
        WifiConfiguration candidate = null;
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            WifiConfiguration network = this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail);
            if (network != null) {
                if (!network.isPasspoint()) {
                    if (!network.isEphemeral()) {
                        WifiConfiguration.NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                        status.setSeenInLastQualifiedNetworkSelection(true);
                        if (status.isNetworkEnabled()) {
                            if (network.BSSID != null && !network.BSSID.equals("any") && !network.BSSID.equals(scanResult.BSSID)) {
                                localLog("Network " + WifiNetworkSelector.toNetworkString(network) + " has specified BSSID " + network.BSSID + ". Skip " + scanResult.BSSID);
                            } else if (!TelephonyUtil.isSimConfig(network) || this.mWifiConfigManager.isSimPresent(TelephonyUtil.getSimSlot(network))) {
                                int score = calculateBssidScore(scanResult, network, currentNetwork, currentBssid, scoreHistory);
                                if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                                    this.mWifiConfigManager.setNetworkCandidateScanResult(network.networkId, scanResult, score);
                                }
                                if (network.useExternalScores) {
                                    localLog("Network " + WifiNetworkSelector.toNetworkString(network) + " has external score.");
                                } else {
                                    onConnectableListener.onConnectable(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(network.networkId), score);
                                    if (score > highestScore || (score == highestScore && scanResultCandidate != null && scanResult.level > scanResultCandidate.level)) {
                                        this.mWifiConfigManager.setNetworkCandidateScanResult(network.networkId, scanResult, score);
                                        highestScore = score;
                                        scanResultCandidate = scanResult;
                                        candidate = this.mWifiConfigManager.getConfiguredNetwork(network.networkId);
                                    }
                                }
                            } else {
                                localLog("Network " + WifiNetworkSelector.toNetworkString(network) + " is skipped due to sim card absent");
                            }
                        }
                    }
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
