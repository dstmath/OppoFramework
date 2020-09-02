package com.android.server;

import android.net.TrafficStats;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoSlaTrafficRecord {
    private static final String TAG = "OppoSlaTrafficRecord";
    long beginCellRxBytes;
    long beginCellTxBytes;
    long beginRawCellRxBytes;
    long beginRawCellTxBytes;
    long beginWlanRxBytes;
    long beginWlanTxBytes;
    long endCellRxBytes;
    long endCellTxBytes;
    long endRawCellRxBytes;
    long endRawCellTxBytes;
    long endWlanRxBytes;
    long endWlanTxBytes;
    public String mCellIfName = "";
    public String mWlanIfName = "";
    long rawCellRxBytes;
    long rawCellTxBytes;
    long totalCellRxBytes;
    long totalCellTxBytes;
    long totalWlanRxBytes;
    long totalWlanTxBytes;

    public OppoSlaTrafficRecord() {
        String[] params = SystemProperties.get("persist.sys.sla.traffic", "").split("-");
        if (params != null && params.length == 6) {
            this.totalWlanRxBytes = Long.parseLong(params[0]);
            this.totalWlanTxBytes = Long.parseLong(params[1]);
            this.totalCellRxBytes = Long.parseLong(params[2]);
            this.totalCellTxBytes = Long.parseLong(params[3]);
            this.rawCellRxBytes = Long.parseLong(params[4]);
            this.rawCellTxBytes = Long.parseLong(params[5]);
        }
    }

    public void startRecord() {
        this.beginWlanRxBytes = TrafficStats.getRxBytes(this.mWlanIfName);
        this.beginWlanTxBytes = TrafficStats.getTxBytes(this.mWlanIfName);
        this.beginCellRxBytes = TrafficStats.getRxBytes(this.mCellIfName);
        this.beginCellTxBytes = TrafficStats.getTxBytes(this.mCellIfName);
    }

    public void stopRecord() {
        this.endWlanRxBytes = TrafficStats.getRxBytes(this.mWlanIfName);
        this.endWlanTxBytes = TrafficStats.getTxBytes(this.mWlanIfName);
        this.endCellRxBytes = TrafficStats.getRxBytes(this.mCellIfName);
        this.endCellTxBytes = TrafficStats.getTxBytes(this.mCellIfName);
        this.totalWlanRxBytes += this.endWlanRxBytes - this.beginWlanRxBytes;
        this.totalWlanTxBytes += this.endWlanTxBytes - this.beginWlanTxBytes;
        this.totalCellRxBytes += this.endCellRxBytes - this.beginCellRxBytes;
        this.totalCellTxBytes += this.endCellTxBytes - this.beginCellTxBytes;
        updateRecord();
    }

    public void startCellRecord() {
        this.beginRawCellRxBytes = TrafficStats.getRxBytes(this.mCellIfName);
        this.beginRawCellTxBytes = TrafficStats.getTxBytes(this.mCellIfName);
        log("oppo_sla:startCellRecord beginRawCellRxBytes:" + this.beginRawCellRxBytes + " beginRawCellTxBytes:" + this.beginRawCellTxBytes);
    }

    public void stopCellRecord() {
        this.endRawCellRxBytes = TrafficStats.getRxBytes(this.mCellIfName);
        this.endRawCellTxBytes = TrafficStats.getTxBytes(this.mCellIfName);
        this.rawCellRxBytes += this.endRawCellRxBytes - this.beginRawCellRxBytes;
        this.rawCellTxBytes += this.endRawCellTxBytes - this.beginRawCellTxBytes;
        log("oppo_sla:stopCellRecord endRawCellRxBytes:" + this.endRawCellRxBytes + " endRawCellTxBytes:" + this.endRawCellTxBytes + " rawCellRxBytes:" + this.rawCellRxBytes + " rawCellTxBytes:" + this.rawCellTxBytes);
        updateRecord();
    }

    private void updateRecord() {
        SystemProperties.set("persist.sys.sla.traffic", this.totalWlanRxBytes + "-" + this.totalWlanTxBytes + "-" + this.totalCellRxBytes + "-" + this.totalCellTxBytes + "-" + this.rawCellRxBytes + "-" + this.rawCellTxBytes);
    }

    public String toString() {
        return "Wlan_Rx " + ((this.totalWlanRxBytes / 1024) / 1024) + "    Wlan_Tx " + ((this.totalWlanTxBytes / 1024) / 1024) + "    Cell_Rx " + ((this.totalCellRxBytes / 1024) / 1024) + "    Cell_Tx " + ((this.totalCellTxBytes / 1024) / 1024) + "    Raw_Cell_Rx " + ((this.rawCellRxBytes / 1024) / 1024) + "    Raw_Cell_Tx " + ((this.rawCellTxBytes / 1024) / 1024);
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }
}
