package com.android.server.wifi;

import android.net.NetworkAgent;
import android.net.wifi.WifiInfo;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public class WifiScoreReport {
    private static final int DUMPSYS_ENTRY_COUNT_LIMIT = 3600;
    public static final String DUMP_ARG = "WifiScoreReport";
    private static final long FIRST_REASONABLE_WALL_CLOCK = 1490000000000L;
    private static final long MIN_TIME_TO_KEEP_BELOW_TRANSITION_SCORE_MILLIS = 9000;
    private static final long NUD_THROTTLE_MILLIS = 5000;
    private static final String TAG = "WifiScoreReport";
    private static final double TIME_CONSTANT_MILLIS = 30000.0d;
    ConnectedScore mAggressiveConnectedScore;
    private final Clock mClock;
    private long mLastDownwardBreachTimeMillis = 0;
    private int mLastKnownNudCheckScore = -5;
    private long mLastKnownNudCheckTimeMillis = 0;
    private LinkedList<String> mLinkMetricsHistory = new LinkedList<>();
    private int mNudCount = 0;
    private int mNudYes = 0;
    private int mScore = 5;
    private final ScoringParams mScoringParams;
    private int mSessionNumber = 0;
    VelocityBasedConnectedScore mVelocityBasedConnectedScore;
    private boolean mVerboseLoggingEnabled = false;

    WifiScoreReport(ScoringParams scoringParams, Clock clock) {
        this.mScoringParams = scoringParams;
        this.mClock = clock;
        this.mAggressiveConnectedScore = new AggressiveConnectedScore(scoringParams, clock);
        this.mVelocityBasedConnectedScore = new VelocityBasedConnectedScore(scoringParams, clock);
    }

    public void reset() {
        this.mSessionNumber++;
        this.mScore = 5;
        this.mLastKnownNudCheckScore = -5;
        this.mAggressiveConnectedScore.reset();
        this.mVelocityBasedConnectedScore.reset();
        this.mLastDownwardBreachTimeMillis = 0;
        if (this.mVerboseLoggingEnabled) {
            Log.d("WifiScoreReport", "reset");
        }
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public void calculateAndReportScore(WifiInfo wifiInfo, NetworkAgent networkAgent, WifiMetrics wifiMetrics) {
        int netId;
        int score;
        if (wifiInfo.getRssi() == -127) {
            Log.d("WifiScoreReport", "Not reporting score because RSSI is invalid");
            return;
        }
        long millis = this.mClock.getWallClockMillis();
        if (networkAgent != null) {
            netId = networkAgent.netId;
        } else {
            netId = 0;
        }
        this.mAggressiveConnectedScore.updateUsingWifiInfo(wifiInfo, millis);
        this.mVelocityBasedConnectedScore.updateUsingWifiInfo(wifiInfo, millis);
        int s1 = this.mAggressiveConnectedScore.generateScore();
        int s2 = this.mVelocityBasedConnectedScore.generateScore();
        int score2 = s2;
        if (wifiInfo.score > -5 && score2 <= -5 && wifiInfo.txSuccessRate >= ((double) this.mScoringParams.getYippeeSkippyPacketsPerSecond()) && wifiInfo.rxSuccessRate >= ((double) this.mScoringParams.getYippeeSkippyPacketsPerSecond())) {
            score2 = -4;
        }
        if (wifiInfo.score > -5 && score2 <= -5) {
            int entry = this.mScoringParams.getEntryRssi(wifiInfo.getFrequency());
            if (this.mVelocityBasedConnectedScore.getFilteredRssi() >= ((double) entry) || wifiInfo.getRssi() >= entry) {
                score2 = -4;
            }
        }
        if (wifiInfo.score >= -5 && score2 < -5) {
            this.mLastDownwardBreachTimeMillis = millis;
        } else if (wifiInfo.score < -5 && score2 >= -5 && millis - this.mLastDownwardBreachTimeMillis < MIN_TIME_TO_KEEP_BELOW_TRANSITION_SCORE_MILLIS) {
            score2 = wifiInfo.score;
        }
        if (score2 > 5) {
            score2 = 5;
        }
        if (score2 < 0) {
            score = 0;
        } else {
            score = score2;
        }
        logLinkMetrics(wifiInfo, millis, netId, s1, s2, score);
        if (score != wifiInfo.score) {
            if (this.mVerboseLoggingEnabled) {
                Log.d("WifiScoreReport", "report new wifi score " + score);
            }
            wifiInfo.score = score;
            if (networkAgent != null) {
                networkAgent.sendNetworkScore(score);
            }
        }
        wifiMetrics.incrementWifiScoreCount(score);
        this.mScore = score;
    }

    public boolean shouldCheckIpLayer() {
        int nud = this.mScoringParams.getNudKnob();
        if (nud == 0) {
            return false;
        }
        long deltaMillis = this.mClock.getWallClockMillis() - this.mLastKnownNudCheckTimeMillis;
        if (deltaMillis < 5000) {
            return false;
        }
        double deltaLevel = (double) (11 - nud);
        double nextNudBreach = -5.0d;
        if (this.mLastKnownNudCheckScore < -5 && ((double) deltaMillis) < 150000.0d) {
            double a = Math.exp(((double) (-deltaMillis)) / TIME_CONSTANT_MILLIS);
            nextNudBreach = ((((double) this.mLastKnownNudCheckScore) - deltaLevel) * a) + ((1.0d - a) * -5.0d);
        }
        if (((double) this.mScore) >= nextNudBreach) {
            return false;
        }
        this.mNudYes++;
        return true;
    }

    public void noteIpCheck() {
        this.mLastKnownNudCheckTimeMillis = this.mClock.getWallClockMillis();
        this.mLastKnownNudCheckScore = this.mScore;
        this.mNudCount++;
    }

    /* JADX INFO: Multiple debug info for r13v1 double: [D('txRetriesRate' double), D('txSuccessRate' double)] */
    /* JADX INFO: Multiple debug info for r13v2 double: [D('txBadRate' double), D('txRetriesRate' double)] */
    /* JADX INFO: Multiple debug info for r13v3 double: [D('txBadRate' double), D('rxSuccessRate' double)] */
    private void logLinkMetrics(WifiInfo wifiInfo, long now, int netId, int s1, int s2, int score) {
        if (now >= FIRST_REASONABLE_WALL_CLOCK) {
            double rssi = (double) wifiInfo.getRssi();
            double filteredRssi = this.mVelocityBasedConnectedScore.getFilteredRssi();
            double rssiThreshold = this.mVelocityBasedConnectedScore.getAdjustedRssiThreshold();
            int freq = wifiInfo.getFrequency();
            int linkSpeed = wifiInfo.getLinkSpeed();
            double txSuccessRate = wifiInfo.txSuccessRate;
            double txSuccessRate2 = wifiInfo.txRetriesRate;
            double txRetriesRate = wifiInfo.txBadRate;
            double rxSuccessRate = wifiInfo.rxSuccessRate;
            try {
                String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(now));
                String s = String.format(Locale.US, "%s,%d,%d,%.1f,%.1f,%.1f,%d,%d,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,%d,%d", timestamp, Integer.valueOf(this.mSessionNumber), Integer.valueOf(netId), Double.valueOf(rssi), Double.valueOf(filteredRssi), Double.valueOf(rssiThreshold), Integer.valueOf(freq), Integer.valueOf(linkSpeed), Double.valueOf(txSuccessRate), Double.valueOf(txSuccessRate2), Double.valueOf(txRetriesRate), Double.valueOf(rxSuccessRate), Integer.valueOf(this.mNudYes), Integer.valueOf(this.mNudCount), Integer.valueOf(s1), Integer.valueOf(s2), Integer.valueOf(score));
                synchronized (this.mLinkMetricsHistory) {
                    this.mLinkMetricsHistory.add(s);
                    while (this.mLinkMetricsHistory.size() > DUMPSYS_ENTRY_COUNT_LIMIT) {
                        this.mLinkMetricsHistory.removeFirst();
                    }
                }
            } catch (Exception e) {
                Log.e("WifiScoreReport", "format problem", e);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        LinkedList<String> history;
        synchronized (this.mLinkMetricsHistory) {
            history = new LinkedList<>(this.mLinkMetricsHistory);
        }
        pw.println("time,session,netid,rssi,filtered_rssi,rssi_threshold,freq,linkspeed,tx_good,tx_retry,tx_bad,rx_pps,nudrq,nuds,s1,s2,score");
        Iterator<String> it = history.iterator();
        while (it.hasNext()) {
            pw.println(it.next());
        }
        history.clear();
    }
}
