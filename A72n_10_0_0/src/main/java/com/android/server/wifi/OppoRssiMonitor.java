package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.PowerManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoRssiMonitor {
    private static final int LAST_RSSI_TIMESTAMP_COUNT = 5;
    private static final String RSSI_MONITOR_DETECTION_THR = "RSSI_MONITOR_DETECTION_THR";
    private static final int RSSI_MONITOR_DETECTION_THR_DEFAULT = 2;
    private static final String RSSI_MONITOR_LOW_RSSI = "RSSI_MONITOR_LOW_RSSI";
    private static final int RSSI_MONITOR_LOW_RSSI_DEFAULT = -70;
    private static final String RSSI_MONITOR_SWITCH = "RSSI_MONITOR_SWITCH";
    private static final boolean RSSI_MONITOR_SWITCH_DEFAULT = true;
    private static final String RSSI_MONITOR_WAIT_TIME_MS = "RSSI_MONITOR_WAIT_TIME_MS";
    private static final int RSSI_MONITOR_WAIT_TIME_MS_DEFAULT = 3500;
    private static final String TAG = "OppoRssiMonitor";
    private ClientModeImpl mClientModeImpl;
    private WifiConfiguration mConnectedConfig = null;
    private long mConnectedTimeStamp;
    private final Context mContext;
    private long mLastRssiDistributionTimestamp = 0;
    private int mLastRssiLevel = 0;
    private RssiTimestamp[] mLastRssiTimestamp = new RssiTimestamp[5];
    private int mLastRssiValue = 0;
    private List<ScanResult> mLastScanResults = new ArrayList();
    private WifiInfo mLastWifiInfo = null;
    private int mRssiBestScreenOff = WifiConfiguration.INVALID_RSSI;
    private int mRssiBestScreenOn = WifiConfiguration.INVALID_RSSI;
    private long mRssiCountScreenOff = 0;
    private long mRssiCountScreenOn = 0;
    private long[] mRssiDistribution = new long[5];
    private int mRssiRapidChangeTimes = 0;
    private long mRssiSumScreenOff = 0;
    private long mRssiSumScreenOn = 0;
    private int mRssiTimestampInedx = 0;
    private int mRssiWorstScreenOff = 0;
    private int mRssiWorstScreenOn = 0;
    private long mScreenOffLastTimestmap = 0;
    private long mScreenOffTime = 0;
    private boolean mScreenOn = false;
    private boolean mVerboseLoggingEnabled = false;
    private boolean mWifiConnected = false;
    private final WifiInjector mWifiInjector;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    OppoRssiMonitor(Context context) {
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mClientModeImpl = WifiInjector.getInstance().getClientModeImpl();
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mScreenOn = ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
        registerReceiver();
        clearRssiDistribution();
        clearRssiTimeStamp();
    }

    public void rssiChange(int rssi) {
        if (this.mWifiConnected && getRssiMonitorSwitch()) {
            long currTime = getElapsedSinceBootMillis();
            log("update rssi:" + rssi);
            handleRssiDistribution(currTime, rssi);
            if (!this.mScreenOn || !isRssiRapidChange(rssi, currTime)) {
                updateRssiParam(rssi, currTime);
                updateScanResult(getScanResults());
                return;
            }
            updateRssiParam(rssi, currTime);
            triggerStatistic(rssi, currTime);
        }
    }

    public void sendConnectedEvent(int rssi, WifiConfiguration config) {
        log("wifi connected");
        this.mWifiConnected = true;
        this.mConnectedTimeStamp = getElapsedSinceBootMillis();
        this.mConnectedConfig = config;
        updateRssiParam(rssi, this.mConnectedTimeStamp);
        this.mScreenOffTime = 0;
        long j = this.mConnectedTimeStamp;
        this.mScreenOffLastTimestmap = j;
        updateRssiDistributionParam(rssi, j);
    }

    public void sendDisconnectedEvent() {
        log("wifi disconnected");
        this.mWifiConnected = false;
        clearRssiDistribution();
        clearRssiTimeStamp();
    }

    public void enableVerbose(boolean verbose) {
        this.mVerboseLoggingEnabled = verbose;
    }

    public void generateRssiDistribution(HashMap<String, String> map) {
        if (map != null) {
            handleRssiDistribution(getElapsedSinceBootMillis(), this.mLastRssiValue);
            for (int i = 0; i < 5; i++) {
                map.put("level_" + i + "_ms", String.valueOf(this.mRssiDistribution[i]));
            }
            map.put("screen_off_ms", String.valueOf(this.mScreenOffTime));
            map.put("rssi_best_screenOn", String.valueOf(this.mRssiBestScreenOn));
            map.put("rssi_worst_screenOn", String.valueOf(this.mRssiWorstScreenOn));
            long j = this.mRssiCountScreenOn;
            String str = "0";
            map.put("rssi_aver_screenOn", j == 0 ? str : String.valueOf(this.mRssiSumScreenOn / j));
            map.put("rssi_best_screenOff", String.valueOf(this.mRssiBestScreenOff));
            map.put("rssi_worst_screenOff", String.valueOf(this.mRssiWorstScreenOff));
            long j2 = this.mRssiCountScreenOff;
            map.put("rssi_aver_screenOff", j2 == 0 ? str : String.valueOf(this.mRssiSumScreenOff / j2));
            long j3 = this.mRssiCountScreenOff;
            long j4 = this.mRssiCountScreenOn;
            if (j3 + j4 != 0) {
                str = String.valueOf((this.mRssiSumScreenOff + this.mRssiSumScreenOn) / (j3 + j4));
            }
            map.put("rssi_aver", str);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoRssiMonitor.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        OppoRssiMonitor.this.handleScreenState(true);
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        OppoRssiMonitor.this.handleScreenState(false);
                    }
                }
            }
        }, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenState(boolean screenState) {
        if (this.mWifiConnected && getRssiMonitorSwitch()) {
            long currTime = getElapsedSinceBootMillis();
            if (!screenState) {
                this.mScreenOffLastTimestmap = currTime;
                handleRssiDistribution(currTime, this.mLastRssiValue);
            } else {
                this.mScreenOffTime += currTime - this.mScreenOffLastTimestmap;
                this.mLastRssiDistributionTimestamp = currTime;
            }
            log("screen on:" + screenState + "  screen off time:" + this.mScreenOffTime);
        }
        this.mScreenOn = screenState;
    }

    private void clearRssiDistribution() {
        for (int i = 0; i < 5; i++) {
            this.mRssiDistribution[i] = 0;
        }
        long currTime = getElapsedSinceBootMillis();
        this.mScreenOffTime = 0;
        this.mScreenOffLastTimestmap = currTime;
        this.mLastRssiDistributionTimestamp = currTime;
        this.mLastRssiValue = 0;
        this.mLastRssiLevel = 0;
        this.mRssiRapidChangeTimes = 0;
        this.mRssiBestScreenOn = WifiConfiguration.INVALID_RSSI;
        this.mRssiWorstScreenOn = 0;
        this.mRssiSumScreenOn = 0;
        this.mRssiCountScreenOn = 0;
        this.mRssiBestScreenOff = WifiConfiguration.INVALID_RSSI;
        this.mRssiWorstScreenOff = 0;
        this.mRssiSumScreenOff = 0;
        this.mRssiCountScreenOff = 0;
    }

    private void updateRssiDistributionParam(int rssi, long currTime) {
        this.mLastRssiLevel = WifiManager.calculateSignalLevel(rssi, 5);
        this.mLastRssiDistributionTimestamp = currTime;
        this.mLastRssiValue = rssi;
    }

    private void handleRssiDistribution(long currTime, int rssi) {
        if (this.mScreenOn) {
            long[] jArr = this.mRssiDistribution;
            int i = this.mLastRssiLevel;
            jArr[i] = jArr[i] + (currTime - this.mLastRssiDistributionTimestamp);
            updateRssiDistributionParam(rssi, currTime);
            if (this.mRssiBestScreenOn < rssi) {
                this.mRssiBestScreenOn = rssi;
            }
            if (this.mRssiWorstScreenOn > rssi && rssi != WifiConfiguration.INVALID_RSSI) {
                this.mRssiWorstScreenOn = rssi;
            }
            if (rssi != WifiConfiguration.INVALID_RSSI) {
                this.mRssiSumScreenOn += (long) rssi;
                this.mRssiCountScreenOn++;
                return;
            }
            return;
        }
        if (this.mRssiBestScreenOff < rssi) {
            this.mRssiBestScreenOff = rssi;
        }
        if (this.mRssiWorstScreenOff > rssi && rssi != WifiConfiguration.INVALID_RSSI) {
            this.mRssiWorstScreenOff = rssi;
        }
        if (rssi != WifiConfiguration.INVALID_RSSI) {
            this.mRssiSumScreenOff += (long) rssi;
            this.mRssiCountScreenOff++;
        }
    }

    private boolean isRssiRapidChange(int rssi, long currTime) {
        if (!isSameBssid()) {
            log("bssid changed, no need trigger statistic");
            return false;
        }
        int deltaTime = getRssiMonitorWaitTime();
        if (currTime - this.mConnectedTimeStamp < ((long) deltaTime)) {
            log("new connect, no need trigger statistic");
            return false;
        }
        int levelDeltaThr = getRssiMonitorDetectionThr();
        int currLevel = WifiManager.calculateSignalLevel(rssi, 5);
        for (int i = 0; i < 5; i++) {
            int j = ((this.mRssiTimestampInedx + 5) - i) % 5;
            if (this.mLastRssiTimestamp[j].timestamp != 0 && currTime - this.mLastRssiTimestamp[j].timestamp <= ((long) deltaTime) && this.mLastRssiTimestamp[j].level - currLevel > levelDeltaThr) {
                this.mRssiRapidChangeTimes++;
                return true;
            }
        }
        return false;
    }

    private void updateRssiParam(int rssi, long timeStamp) {
        this.mRssiTimestampInedx++;
        this.mRssiTimestampInedx %= 5;
        this.mLastRssiTimestamp[this.mRssiTimestampInedx] = new RssiTimestamp(rssi, timeStamp);
        if (!isSameBssid()) {
            log("bssid changed, init connected or roaming time");
            this.mConnectedTimeStamp = getElapsedSinceBootMillis();
        }
        updateWifiInfo();
    }

    private void updateWifiInfo() {
        if (this.mClientModeImpl == null) {
            this.mClientModeImpl = WifiInjector.getInstance().getClientModeImpl();
        }
        ClientModeImpl clientModeImpl = this.mClientModeImpl;
        if (clientModeImpl != null) {
            this.mLastWifiInfo = new WifiInfo(clientModeImpl.getWifiInfo());
        }
    }

    private void clearRssiTimeStamp() {
        for (int i = 0; i < 5; i++) {
            this.mLastRssiTimestamp[i] = new RssiTimestamp(WifiConfiguration.INVALID_RSSI, 0);
        }
    }

    private String getRssiTimestamp() {
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int j = ((this.mRssiTimestampInedx + 5) - i) % 5;
            if (this.mLastRssiTimestamp[j].timestamp == 0) {
                break;
            }
            sbuf.append("[" + this.mLastRssiTimestamp[j].rssi + "," + this.mLastRssiTimestamp[j].timestamp + "]");
        }
        return sbuf.toString();
    }

    private boolean isSameBssid() {
        WifiInfo wifiInfo = null;
        if (this.mClientModeImpl == null) {
            this.mClientModeImpl = WifiInjector.getInstance().getClientModeImpl();
        }
        ClientModeImpl clientModeImpl = this.mClientModeImpl;
        if (clientModeImpl != null) {
            wifiInfo = clientModeImpl.getWifiInfo();
        }
        WifiInfo wifiInfo2 = this.mLastWifiInfo;
        if (wifiInfo2 == null || wifiInfo == null) {
            return false;
        }
        String lastBssid = wifiInfo2.getBSSID();
        String currBssid = wifiInfo.getBSSID();
        if (currBssid == null || !currBssid.equals(lastBssid)) {
            return false;
        }
        return true;
    }

    private void updateScanResult(List<ScanResult> scanResults) {
        this.mLastScanResults = scanResults;
    }

    private List<ScanResult> getScanResults() {
        return WifiInjector.getInstance().getScanRequestProxy().syncGetScanResultsList();
    }

    private long getElapsedSinceBootMillis() {
        return System.currentTimeMillis();
    }

    private ScanResult findApFromScanResults(WifiInfo wifiInfo, List<ScanResult> scanResults) {
        String bssid;
        if (wifiInfo == null || scanResults.size() <= 0 || (bssid = wifiInfo.getBSSID()) == null) {
            return null;
        }
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            if (bssid.equals(scanResult.BSSID)) {
                return scanResult;
            }
        }
        return null;
    }

    private void triggerStatistic(int rssi, long currTime) {
        List<ScanResult> scanResults = getScanResults();
        HashMap<String, String> map = new HashMap<>();
        generateApInformation(map, scanResults);
        map.put("rssi_stamp", getRssiTimestamp());
        map.put("curr_scan", getApChannelStatistic(scanResults));
        log("rssi_rapid_change");
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "rssi_rapid_change", map, false);
        updateScanResult(scanResults);
    }

    private void generateApInformation(HashMap<String, String> map, List<ScanResult> scanResults) {
        WifiInfo wifiInfo;
        if (map != null) {
            ScanResult sr = null;
            if (this.mClientModeImpl == null) {
                this.mClientModeImpl = WifiInjector.getInstance().getClientModeImpl();
            }
            ClientModeImpl clientModeImpl = this.mClientModeImpl;
            String str = "";
            if (!(clientModeImpl == null || (wifiInfo = clientModeImpl.getWifiInfo()) == null)) {
                sr = findApFromScanResults(wifiInfo, scanResults);
                map.put("ap_freq", String.valueOf(wifiInfo.getFrequency()));
                map.put("ap_merterd", String.valueOf(wifiInfo.getMeteredHint()));
                map.put("link_tx", String.valueOf(wifiInfo.getTxLinkSpeedMbps()));
                map.put("link_rx", String.valueOf(wifiInfo.getRxLinkSpeedMbps()));
                map.put("link_sp", String.valueOf(wifiInfo.getLinkSpeed()));
                map.put("ap_bw", sr != null ? String.valueOf(sr.channelWidth) : str);
            }
            WifiConfiguration wifiConfiguration = this.mConnectedConfig;
            if (wifiConfiguration != null) {
                str = wifiConfiguration.configKey();
            }
            map.put("config_key", str);
            if (WifiInjector.getInstance().getWifiDisconStat() != null) {
                map.put("ap_name", WifiInjector.getInstance().getWifiDisconStat().getApName());
            }
            map.put("wifi_standard", OppoInformationElementUtil.getWifiStandard(sr));
            map.put("vendor_spec", OppoInformationElementUtil.getVendorSpec(sr));
        }
    }

    private String getApChannelStatistic(List<ScanResult> scanResults) {
        if (scanResults == null || scanResults.size() <= 0) {
            log("null scan result");
            return "";
        }
        HashMap<Integer, ChannelStatistic> map = new HashMap<>();
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult sr = scanResults.get(i);
            if (sr != null) {
                Integer channel = Integer.valueOf(sr.frequency);
                int level = sr.level;
                if (!map.containsKey(channel)) {
                    map.put(channel, new ChannelStatistic(sr.frequency, 1, level));
                } else {
                    ChannelStatistic cs = map.get(channel);
                    cs.count++;
                    if (cs.bestRssi < level) {
                        cs.bestRssi = level;
                    }
                }
            }
        }
        Iterator iterator = null;
        if (map.keySet() != null) {
            iterator = map.keySet().iterator();
        }
        if (iterator == null) {
            log("null iterator");
            return "";
        }
        StringBuilder sbuf = new StringBuilder();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            if (key == null) {
                log("null key");
            } else {
                ChannelStatistic cs2 = map.get(key);
                if (cs2 == null) {
                    log("null cs");
                } else {
                    sbuf.append(cs2.toString());
                }
            }
        }
        return sbuf.toString();
    }

    private boolean getRssiMonitorSwitch() {
        return this.mWifiRomUpdateHelper.getBooleanValue(RSSI_MONITOR_SWITCH, true);
    }

    private int getRssiMonitorWaitTime() {
        return this.mWifiRomUpdateHelper.getIntegerValue(RSSI_MONITOR_WAIT_TIME_MS, Integer.valueOf((int) RSSI_MONITOR_WAIT_TIME_MS_DEFAULT)).intValue();
    }

    private int getRssiMonitorDetectionThr() {
        return this.mWifiRomUpdateHelper.getIntegerValue(RSSI_MONITOR_DETECTION_THR, 2).intValue();
    }

    private int getRssiMonitorLowRssi() {
        return this.mWifiRomUpdateHelper.getIntegerValue(RSSI_MONITOR_LOW_RSSI, Integer.valueOf((int) RSSI_MONITOR_LOW_RSSI_DEFAULT)).intValue();
    }

    /* access modifiers changed from: private */
    public static class RssiTimestamp {
        public int level = 0;
        public int rssi = WifiConfiguration.INVALID_RSSI;
        public long timestamp = 0;

        public RssiTimestamp(int rssi2, long timestamp2) {
            this.rssi = rssi2;
            this.timestamp = timestamp2;
            this.level = WifiManager.calculateSignalLevel(rssi2, 5);
        }
    }

    /* access modifiers changed from: private */
    public static class ChannelStatistic {
        public int bestRssi;
        public int channel;
        public int count;

        public ChannelStatistic(int channel2, int count2, int bestRssi2) {
            this.channel = channel2;
            this.count = count2;
            this.bestRssi = bestRssi2;
        }

        public String toString() {
            return "[" + this.channel + ":" + this.count + ":" + this.bestRssi + "]";
        }
    }

    private void log(String s) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, s);
        }
    }
}
