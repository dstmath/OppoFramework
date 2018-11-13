package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkAgent;
import android.net.wifi.WifiInfo;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class WifiScoreReport {
    private static final int DUMPSYS_ENTRY_COUNT_LIMIT = 14400;
    public static final String DUMP_ARG = "WifiScoreReport";
    private static final long FIRST_REASONABLE_WALL_CLOCK = 1490000000000L;
    private static final String TAG = "WifiScoreReport";
    ConnectedScore mAggressiveConnectedScore;
    private final Clock mClock;
    ConnectedScore mConnectedScore;
    private LinkedList<String> mLinkMetricsHistory = new LinkedList();
    private String mReport;
    private boolean mReportValid = false;
    private int mSessionNumber = 0;
    private boolean mVerboseLoggingEnabled = false;

    WifiScoreReport(Context context, WifiConfigManager wifiConfigManager, Clock clock) {
        this.mClock = clock;
        this.mConnectedScore = new LegacyConnectedScore(context, wifiConfigManager, clock);
        this.mAggressiveConnectedScore = new AggressiveConnectedScore(context, clock);
    }

    public String getLastReport() {
        return this.mReport;
    }

    public void reset() {
        this.mReport = "";
        if (this.mReportValid) {
            this.mSessionNumber++;
            this.mReportValid = false;
        }
        this.mConnectedScore.reset();
        this.mAggressiveConnectedScore.reset();
        if (this.mVerboseLoggingEnabled) {
            Log.d("WifiScoreReport", "reset");
        }
    }

    public boolean isLastReportValid() {
        return this.mReportValid;
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public void calculateAndReportScore(WifiInfo wifiInfo, NetworkAgent networkAgent, int aggressiveHandover, WifiMetrics wifiMetrics) {
        int score;
        long millis = this.mConnectedScore.getMillis();
        this.mConnectedScore.updateUsingWifiInfo(wifiInfo, millis);
        this.mAggressiveConnectedScore.updateUsingWifiInfo(wifiInfo, millis);
        int s0 = this.mConnectedScore.generateScore();
        int s1 = this.mAggressiveConnectedScore.generateScore();
        if (aggressiveHandover == 0) {
            score = s0;
        } else {
            score = s1;
        }
        if (score > 5) {
            score = 5;
        }
        if (score < 0) {
            score = 0;
        }
        logLinkMetrics(wifiInfo, s0, s1);
        if (score != wifiInfo.score) {
            if (this.mVerboseLoggingEnabled) {
                Log.d("WifiScoreReport", " report new wifi score " + score);
            }
            wifiInfo.score = score;
            if (networkAgent != null) {
                networkAgent.sendNetworkScore(score);
            }
        }
        this.mReport = String.format(Locale.US, " score=%d", new Object[]{Integer.valueOf(score)});
        this.mReportValid = true;
        wifiMetrics.incrementWifiScoreCount(score);
    }

    private void logLinkMetrics(WifiInfo wifiInfo, int s0, int s1) {
        long now = this.mClock.getWallClockMillis();
        if (now >= FIRST_REASONABLE_WALL_CLOCK) {
            double rssi = (double) wifiInfo.getRssi();
            int freq = wifiInfo.getFrequency();
            int linkSpeed = wifiInfo.getLinkSpeed();
            double txSuccessRate = wifiInfo.txSuccessRate;
            double txRetriesRate = wifiInfo.txRetriesRate;
            double txBadRate = wifiInfo.txBadRate;
            double rxSuccessRate = wifiInfo.rxSuccessRate;
            try {
                String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(now));
                this.mLinkMetricsHistory.add(String.format(Locale.US, "%s,%d,%.1f,%d,%d,%.2f,%.2f,%.2f,%.2f,%d,%d", new Object[]{timestamp, Integer.valueOf(this.mSessionNumber), Double.valueOf(rssi), Integer.valueOf(freq), Integer.valueOf(linkSpeed), Double.valueOf(txSuccessRate), Double.valueOf(txRetriesRate), Double.valueOf(txBadRate), Double.valueOf(rxSuccessRate), Integer.valueOf(s0), Integer.valueOf(s1)}));
            } catch (Exception e) {
                Log.e("WifiScoreReport", "format problem", e);
            }
            while (this.mLinkMetricsHistory.size() > DUMPSYS_ENTRY_COUNT_LIMIT) {
                this.mLinkMetricsHistory.removeFirst();
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("time,session,rssi,freq,linkspeed,tx_good,tx_retry,tx_bad,rx,s0,s1");
        for (String line : this.mLinkMetricsHistory) {
            pw.println(line);
        }
    }
}
