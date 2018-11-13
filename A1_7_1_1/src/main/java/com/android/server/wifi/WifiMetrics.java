package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Base64;
import android.util.SparseIntArray;
import com.android.server.wifi.WifiMetricsProto.AlertReasonCount;
import com.android.server.wifi.WifiMetricsProto.RssiPollCount;
import com.android.server.wifi.WifiMetricsProto.WifiLog;
import com.android.server.wifi.WifiMetricsProto.WifiLog.ScanReturnEntry;
import com.android.server.wifi.WifiMetricsProto.WifiLog.WifiSystemStateEntry;
import com.android.server.wifi.WifiMetricsProto.WifiScoreCount;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.google.protobuf.nano.MessageNano;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WifiMetrics {
    public static final String CLEAN_DUMP_ARG = "clean";
    private static final boolean DBG = false;
    private static final int MAX_CONNECTION_EVENTS = 256;
    private static final int MAX_RSSI_POLL = 0;
    private static final int MAX_WIFI_SCORE = 5;
    private static final int MIN_RSSI_POLL = -127;
    private static final int MIN_WIFI_SCORE = 0;
    public static final String PROTO_DUMP_ARG = "wifiMetricsProto";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final String TAG = "WifiMetrics";
    private Clock mClock;
    private final List<ConnectionEvent> mConnectionEventList = new ArrayList();
    private ConnectionEvent mCurrentConnectionEvent;
    private final Object mLock = new Object();
    private long mRecordStartTimeSec;
    private final SparseIntArray mRssiPollCounts = new SparseIntArray();
    private final SparseIntArray mScanReturnEntries = new SparseIntArray();
    private boolean mScreenOn;
    private final SparseIntArray mWifiAlertReasonCounts = new SparseIntArray();
    private final WifiLog mWifiLogProto = new WifiLog();
    private final SparseIntArray mWifiScoreCounts = new SparseIntArray();
    private int mWifiState;
    private final SparseIntArray mWifiSystemStateEntries = new SparseIntArray();

    class ConnectionEvent {
        public static final int FAILURE_ASSOCIATION_REJECTION = 2;
        public static final int FAILURE_AUTHENTICATION_FAILURE = 3;
        public static final int FAILURE_CONNECT_NETWORK_FAILED = 5;
        public static final int FAILURE_DHCP = 10;
        public static final int FAILURE_NETWORK_DISCONNECTION = 6;
        public static final int FAILURE_NEW_CONNECTION_ATTEMPT = 7;
        public static final int FAILURE_NONE = 1;
        public static final int FAILURE_REDUNDANT_CONNECTION_ATTEMPT = 8;
        public static final int FAILURE_ROAM_TIMEOUT = 9;
        public static final int FAILURE_SSID_TEMP_DISABLED = 4;
        public static final int FAILURE_UNKNOWN = 0;
        private String mConfigBssid;
        private String mConfigSsid;
        com.android.server.wifi.WifiMetricsProto.ConnectionEvent mConnectionEvent;
        private long mRealEndTime;
        private long mRealStartTime;
        RouterFingerPrint mRouterFingerPrint;
        private boolean mScreenOn;
        private int mWifiState;

        /* synthetic */ ConnectionEvent(WifiMetrics this$0, ConnectionEvent connectionEvent) {
            this();
        }

        private ConnectionEvent() {
            this.mConnectionEvent = new com.android.server.wifi.WifiMetricsProto.ConnectionEvent();
            this.mRealEndTime = 0;
            this.mRealStartTime = 0;
            this.mRouterFingerPrint = new RouterFingerPrint();
            this.mConnectionEvent.routerFingerprint = this.mRouterFingerPrint.mRouterFingerPrintProto;
            this.mConfigSsid = "<NULL>";
            this.mConfigBssid = "<NULL>";
            this.mWifiState = 0;
            this.mScreenOn = false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("startTime=");
            Calendar c = Calendar.getInstance();
            synchronized (WifiMetrics.this.mLock) {
                String str;
                c.setTimeInMillis(this.mConnectionEvent.startTimeMillis);
                if (this.mConnectionEvent.startTimeMillis == 0) {
                    str = "            <null>";
                } else {
                    str = String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
                }
                sb.append(str);
                sb.append(", SSID=");
                sb.append(this.mConfigSsid);
                sb.append(", BSSID=");
                sb.append(this.mConfigBssid);
                sb.append(", durationMillis=");
                sb.append(this.mConnectionEvent.durationTakenToConnectMillis);
                sb.append(", roamType=");
                switch (this.mConnectionEvent.roamType) {
                    case 1:
                        sb.append("ROAM_NONE");
                        break;
                    case 2:
                        sb.append("ROAM_DBDC");
                        break;
                    case 3:
                        sb.append("ROAM_ENTERPRISE");
                        break;
                    case 4:
                        sb.append("ROAM_USER_SELECTED");
                        break;
                    case 5:
                        sb.append("ROAM_UNRELATED");
                        break;
                    default:
                        sb.append("ROAM_UNKNOWN");
                        break;
                }
                sb.append(", connectionResult=");
                sb.append(this.mConnectionEvent.connectionResult);
                sb.append(", level2FailureCode=");
                switch (this.mConnectionEvent.level2FailureCode) {
                    case 1:
                        sb.append("NONE");
                        break;
                    case 2:
                        sb.append("ASSOCIATION_REJECTION");
                        break;
                    case 3:
                        sb.append("AUTHENTICATION_FAILURE");
                        break;
                    case 4:
                        sb.append("SSID_TEMP_DISABLED");
                        break;
                    case 5:
                        sb.append("CONNECT_NETWORK_FAILED");
                        break;
                    case 6:
                        sb.append("NETWORK_DISCONNECTION");
                        break;
                    case 7:
                        sb.append("NEW_CONNECTION_ATTEMPT");
                        break;
                    case 8:
                        sb.append("REDUNDANT_CONNECTION_ATTEMPT");
                        break;
                    case 9:
                        sb.append("ROAM_TIMEOUT");
                        break;
                    case 10:
                        sb.append("DHCP");
                        break;
                }
                sb.append("UNKNOWN");
                sb.append(", connectivityLevelFailureCode=");
                switch (this.mConnectionEvent.connectivityLevelFailureCode) {
                    case 1:
                        sb.append("NONE");
                        break;
                    case 2:
                        sb.append("DHCP");
                        break;
                    case 3:
                        sb.append("NO_INTERNET");
                        break;
                    case 4:
                        sb.append("UNWANTED");
                        break;
                    default:
                        sb.append("UNKNOWN");
                        break;
                }
                sb.append(", signalStrength=");
                sb.append(this.mConnectionEvent.signalStrength);
                sb.append(", wifiState=");
                switch (this.mWifiState) {
                    case 1:
                        sb.append("WIFI_DISABLED");
                        break;
                    case 2:
                        sb.append("WIFI_DISCONNECTED");
                        break;
                    case 3:
                        sb.append("WIFI_ASSOCIATED");
                        break;
                    default:
                        sb.append("WIFI_UNKNOWN");
                        break;
                }
                sb.append(", screenOn=");
                sb.append(this.mScreenOn);
                sb.append(". mRouterFingerprint: ");
                sb.append(this.mRouterFingerPrint.toString());
            }
            return sb.toString();
        }
    }

    class RouterFingerPrint {
        private com.android.server.wifi.WifiMetricsProto.RouterFingerPrint mRouterFingerPrintProto = new com.android.server.wifi.WifiMetricsProto.RouterFingerPrint();

        RouterFingerPrint() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            synchronized (WifiMetrics.this.mLock) {
                sb.append("mConnectionEvent.roamType=").append(this.mRouterFingerPrintProto.roamType);
                sb.append(", mChannelInfo=").append(this.mRouterFingerPrintProto.channelInfo);
                sb.append(", mDtim=").append(this.mRouterFingerPrintProto.dtim);
                sb.append(", mAuthentication=").append(this.mRouterFingerPrintProto.authentication);
                sb.append(", mHidden=").append(this.mRouterFingerPrintProto.hidden);
                sb.append(", mRouterTechnology=").append(this.mRouterFingerPrintProto.routerTechnology);
                sb.append(", mSupportsIpv6=").append(this.mRouterFingerPrintProto.supportsIpv6);
            }
            return sb.toString();
        }

        public void updateFromWifiConfiguration(WifiConfiguration config) {
            synchronized (WifiMetrics.this.mLock) {
                if (config != null) {
                    this.mRouterFingerPrintProto.hidden = config.hiddenSSID;
                    if (config.dtimInterval > 0) {
                        this.mRouterFingerPrintProto.dtim = config.dtimInterval;
                    }
                    WifiMetrics.this.mCurrentConnectionEvent.mConfigSsid = config.SSID;
                    if (config.allowedKeyManagement != null && config.allowedKeyManagement.get(0)) {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 1;
                    } else if (config.isEnterprise()) {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
                    } else {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
                    }
                    WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.passpoint = config.isPasspoint();
                    ScanResult candidate = config.getNetworkSelectionStatus().getCandidate();
                    if (candidate != null) {
                        WifiMetrics.this.updateMetricsFromScanResult(candidate);
                    }
                }
            }
        }
    }

    public WifiMetrics(Clock clock) {
        this.mClock = clock;
        this.mCurrentConnectionEvent = null;
        this.mScreenOn = true;
        this.mWifiState = 1;
        this.mRecordStartTimeSec = this.mClock.elapsedRealtime() / 1000;
    }

    public void startConnectionEvent(WifiConfiguration config, String targetBSSID, int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                if (this.mCurrentConnectionEvent.mConfigSsid == null || this.mCurrentConnectionEvent.mConfigBssid == null || config == null || !this.mCurrentConnectionEvent.mConfigSsid.equals(config.SSID) || !(this.mCurrentConnectionEvent.mConfigBssid.equals(WifiLastResortWatchdog.BSSID_ANY) || this.mCurrentConnectionEvent.mConfigBssid.equals(targetBSSID))) {
                    endConnectionEvent(7, 1);
                } else {
                    this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
                    endConnectionEvent(8, 1);
                }
            }
            while (this.mConnectionEventList.size() >= 256) {
                this.mConnectionEventList.remove(0);
            }
            this.mCurrentConnectionEvent = new ConnectionEvent(this, null);
            this.mCurrentConnectionEvent.mConnectionEvent.startTimeMillis = this.mClock.currentTimeMillis();
            this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
            this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            this.mCurrentConnectionEvent.mRouterFingerPrint.updateFromWifiConfiguration(config);
            this.mCurrentConnectionEvent.mConfigBssid = WifiLastResortWatchdog.BSSID_ANY;
            this.mCurrentConnectionEvent.mRealStartTime = this.mClock.elapsedRealtime();
            this.mCurrentConnectionEvent.mWifiState = this.mWifiState;
            this.mCurrentConnectionEvent.mScreenOn = this.mScreenOn;
            this.mConnectionEventList.add(this.mCurrentConnectionEvent);
        }
    }

    public void setConnectionEventRoamType(int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            }
        }
    }

    public void setConnectionScanDetail(ScanDetail scanDetail) {
        synchronized (this.mLock) {
            if (!(this.mCurrentConnectionEvent == null || scanDetail == null)) {
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                ScanResult scanResult = scanDetail.getScanResult();
                if (!(networkDetail == null || scanResult == null || this.mCurrentConnectionEvent.mConfigSsid == null || !this.mCurrentConnectionEvent.mConfigSsid.equals("\"" + networkDetail.getSSID() + "\""))) {
                    updateMetricsFromNetworkDetail(networkDetail);
                    updateMetricsFromScanResult(scanResult);
                }
            }
        }
    }

    public void endConnectionEvent(int level2FailureCode, int connectivityFailureCode) {
        int i = 1;
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                boolean result = level2FailureCode == 1 ? connectivityFailureCode == 1 : false;
                com.android.server.wifi.WifiMetricsProto.ConnectionEvent connectionEvent = this.mCurrentConnectionEvent.mConnectionEvent;
                if (!result) {
                    i = 0;
                }
                connectionEvent.connectionResult = i;
                this.mCurrentConnectionEvent.mRealEndTime = this.mClock.elapsedRealtime();
                this.mCurrentConnectionEvent.mConnectionEvent.durationTakenToConnectMillis = (int) (this.mCurrentConnectionEvent.mRealEndTime - this.mCurrentConnectionEvent.mRealStartTime);
                this.mCurrentConnectionEvent.mConnectionEvent.level2FailureCode = level2FailureCode;
                this.mCurrentConnectionEvent.mConnectionEvent.connectivityLevelFailureCode = connectivityFailureCode;
                this.mCurrentConnectionEvent = null;
            }
        }
    }

    private void updateMetricsFromNetworkDetail(NetworkDetail networkDetail) {
        int connectionWifiMode;
        int dtimInterval = networkDetail.getDtimInterval();
        if (dtimInterval > 0) {
            this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.dtim = dtimInterval;
        }
        switch (networkDetail.getWifiMode()) {
            case 0:
                connectionWifiMode = 0;
                break;
            case 1:
                connectionWifiMode = 1;
                break;
            case 2:
                connectionWifiMode = 2;
                break;
            case 3:
                connectionWifiMode = 3;
                break;
            case 4:
                connectionWifiMode = 4;
                break;
            case 5:
                connectionWifiMode = 5;
                break;
            default:
                connectionWifiMode = 6;
                break;
        }
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.routerTechnology = connectionWifiMode;
    }

    private void updateMetricsFromScanResult(ScanResult scanResult) {
        this.mCurrentConnectionEvent.mConnectionEvent.signalStrength = scanResult.level;
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 1;
        this.mCurrentConnectionEvent.mConfigBssid = scanResult.BSSID;
        if (scanResult.capabilities != null) {
            if (scanResult.capabilities.contains("WEP")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (scanResult.capabilities.contains("PSK")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (scanResult.capabilities.contains("EAP")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
            }
        }
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.channelInfo = scanResult.frequency;
    }

    void setNumSavedNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSavedNetworks = num;
        }
    }

    void setNumOpenNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numOpenNetworks = num;
        }
    }

    void setNumPersonalNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPersonalNetworks = num;
        }
    }

    void setNumEnterpriseNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numEnterpriseNetworks = num;
        }
    }

    void setNumHiddenNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numHiddenNetworks = num;
        }
    }

    void setNumPasspointNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointNetworks = num;
        }
    }

    void setNumNetworksAddedByUser(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numNetworksAddedByUser = num;
        }
    }

    void setNumNetworksAddedByApps(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numNetworksAddedByApps = num;
        }
    }

    void setIsLocationEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isLocationEnabled = enabled;
        }
    }

    void setIsScanningAlwaysEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isScanningAlwaysEnabled = enabled;
        }
    }

    public void incrementNonEmptyScanResultCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numNonEmptyScanResults++;
        }
    }

    public void incrementEmptyScanResultCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numEmptyScanResults++;
        }
    }

    public void incrementBackgroundScanCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numBackgroundScans++;
        }
    }

    public int getBackgroundScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numBackgroundScans;
        }
        return i;
    }

    public void incrementOneshotScanCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numOneshotScans++;
        }
        incrementWifiSystemScanStateCount(this.mWifiState, this.mScreenOn);
    }

    public int getOneshotScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numOneshotScans;
        }
        return i;
    }

    private String returnCodeToString(int scanReturnCode) {
        switch (scanReturnCode) {
            case 0:
                return "SCAN_UNKNOWN";
            case 1:
                return "SCAN_SUCCESS";
            case 2:
                return "SCAN_FAILURE_INTERRUPTED";
            case 3:
                return "SCAN_FAILURE_INVALID_CONFIGURATION";
            case 4:
                return "FAILURE_WIFI_DISABLED";
            default:
                return "<UNKNOWN>";
        }
    }

    public void incrementScanReturnEntry(int scanReturnCode, int countToAdd) {
        synchronized (this.mLock) {
            this.mScanReturnEntries.put(scanReturnCode, this.mScanReturnEntries.get(scanReturnCode) + countToAdd);
        }
    }

    public int getScanReturnEntry(int scanReturnCode) {
        int i;
        synchronized (this.mLock) {
            i = this.mScanReturnEntries.get(scanReturnCode);
        }
        return i;
    }

    private String wifiSystemStateToString(int state) {
        switch (state) {
            case 0:
                return "WIFI_UNKNOWN";
            case 1:
                return "WIFI_DISABLED";
            case 2:
                return "WIFI_DISCONNECTED";
            case 3:
                return "WIFI_ASSOCIATED";
            default:
                return "default";
        }
    }

    public void incrementWifiSystemScanStateCount(int state, boolean screenOn) {
        synchronized (this.mLock) {
            int index = (state * 2) + (screenOn ? 1 : 0);
            this.mWifiSystemStateEntries.put(index, this.mWifiSystemStateEntries.get(index) + 1);
        }
    }

    public int getSystemStateCount(int state, boolean screenOn) {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiSystemStateEntries.get((state * 2) + (screenOn ? 1 : 0));
        }
        return i;
    }

    public void incrementNumLastResortWatchdogTriggers() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggers++;
        }
    }

    public void addCountToNumLastResortWatchdogBadAssociationNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadAssociationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadAuthenticationNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadAuthenticationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadDhcpNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadDhcpNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadOtherNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadOtherNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogAvailableNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogAvailableNetworksTotal += count;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAssociation() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadAssociation++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAuthentication() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadAuthentication++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadDhcp() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadDhcp++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadOther() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadOther++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoGood++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoBad++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundGood++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundBad++;
        }
    }

    public void incrementRssiPollRssiCount(int rssi) {
        if (rssi >= MIN_RSSI_POLL && rssi <= 0) {
            synchronized (this.mLock) {
                this.mRssiPollCounts.put(rssi, this.mRssiPollCounts.get(rssi) + 1);
            }
        }
    }

    public void incrementNumLastResortWatchdogSuccesses() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogSuccesses++;
        }
    }

    public void incrementAlertReasonCount(int reason) {
        if (reason > 64 || reason < 0) {
            reason = 0;
        }
        synchronized (this.mLock) {
            this.mWifiAlertReasonCounts.put(reason, this.mWifiAlertReasonCounts.get(reason) + 1);
        }
    }

    public void countScanResults(List<ScanDetail> scanDetails) {
        if (scanDetails != null) {
            int totalResults = 0;
            int openNetworks = 0;
            int personalNetworks = 0;
            int enterpriseNetworks = 0;
            int hiddenNetworks = 0;
            int hotspot2r1Networks = 0;
            int hotspot2r2Networks = 0;
            for (ScanDetail scanDetail : scanDetails) {
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                ScanResult scanResult = scanDetail.getScanResult();
                totalResults++;
                if (networkDetail != null) {
                    if (networkDetail.isHiddenBeaconFrame()) {
                        hiddenNetworks++;
                    }
                    if (networkDetail.getHSRelease() != null) {
                        if (networkDetail.getHSRelease() == HSRelease.R1) {
                            hotspot2r1Networks++;
                        } else if (networkDetail.getHSRelease() == HSRelease.R2) {
                            hotspot2r2Networks++;
                        }
                    }
                }
                if (!(scanResult == null || scanResult.capabilities == null)) {
                    if (scanResult.capabilities.contains("EAP")) {
                        enterpriseNetworks++;
                    } else if (scanResult.capabilities.contains("PSK") || scanResult.capabilities.contains("WEP")) {
                        personalNetworks++;
                    } else {
                        openNetworks++;
                    }
                }
            }
            synchronized (this.mLock) {
                WifiLog wifiLog = this.mWifiLogProto;
                wifiLog.numTotalScanResults += totalResults;
                wifiLog = this.mWifiLogProto;
                wifiLog.numOpenNetworkScanResults += openNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numPersonalNetworkScanResults += personalNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numEnterpriseNetworkScanResults += enterpriseNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHiddenNetworkScanResults += hiddenNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHotspot2R1NetworkScanResults += hotspot2r1Networks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHotspot2R2NetworkScanResults += hotspot2r2Networks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numScans++;
            }
        }
    }

    public void incrementWifiScoreCount(int score) {
        if (score >= 0 && score <= 5) {
            synchronized (this.mLock) {
                this.mWifiScoreCounts.put(score, this.mWifiScoreCounts.get(score) + 1);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            int i;
            if (args != null) {
                if (args.length > 0 && PROTO_DUMP_ARG.equals(args[0])) {
                    consolidateProto(true);
                    for (ConnectionEvent event : this.mConnectionEventList) {
                        if (this.mCurrentConnectionEvent != event) {
                            event.mConnectionEvent.automaticBugReportTaken = true;
                        }
                    }
                    String metricsProtoDump = Base64.encodeToString(MessageNano.toByteArray(this.mWifiLogProto), 0);
                    if (args.length <= 1 || !CLEAN_DUMP_ARG.equals(args[1])) {
                        pw.println("WifiMetrics:");
                        pw.println(metricsProtoDump);
                        pw.println("EndWifiMetrics");
                    } else {
                        pw.print(metricsProtoDump);
                    }
                    clear();
                }
            }
            pw.println("WifiMetrics:");
            pw.println("mConnectionEvents:");
            for (ConnectionEvent event2 : this.mConnectionEventList) {
                String eventLine = event2.toString();
                if (event2 == this.mCurrentConnectionEvent) {
                    eventLine = eventLine + "CURRENTLY OPEN EVENT";
                }
                pw.println(eventLine);
            }
            pw.println("mWifiLogProto.numSavedNetworks=" + this.mWifiLogProto.numSavedNetworks);
            pw.println("mWifiLogProto.numOpenNetworks=" + this.mWifiLogProto.numOpenNetworks);
            pw.println("mWifiLogProto.numPersonalNetworks=" + this.mWifiLogProto.numPersonalNetworks);
            pw.println("mWifiLogProto.numEnterpriseNetworks=" + this.mWifiLogProto.numEnterpriseNetworks);
            pw.println("mWifiLogProto.numHiddenNetworks=" + this.mWifiLogProto.numHiddenNetworks);
            pw.println("mWifiLogProto.numPasspointNetworks=" + this.mWifiLogProto.numPasspointNetworks);
            pw.println("mWifiLogProto.isLocationEnabled=" + this.mWifiLogProto.isLocationEnabled);
            pw.println("mWifiLogProto.isScanningAlwaysEnabled=" + this.mWifiLogProto.isScanningAlwaysEnabled);
            pw.println("mWifiLogProto.numNetworksAddedByUser=" + this.mWifiLogProto.numNetworksAddedByUser);
            pw.println("mWifiLogProto.numNetworksAddedByApps=" + this.mWifiLogProto.numNetworksAddedByApps);
            pw.println("mWifiLogProto.numNonEmptyScanResults=" + this.mWifiLogProto.numNonEmptyScanResults);
            pw.println("mWifiLogProto.numEmptyScanResults=" + this.mWifiLogProto.numEmptyScanResults);
            pw.println("mWifiLogProto.numOneshotScans=" + this.mWifiLogProto.numOneshotScans);
            pw.println("mWifiLogProto.numBackgroundScans=" + this.mWifiLogProto.numBackgroundScans);
            pw.println("mScanReturnEntries:");
            pw.println("  SCAN_UNKNOWN: " + getScanReturnEntry(0));
            pw.println("  SCAN_SUCCESS: " + getScanReturnEntry(1));
            pw.println("  SCAN_FAILURE_INTERRUPTED: " + getScanReturnEntry(2));
            pw.println("  SCAN_FAILURE_INVALID_CONFIGURATION: " + getScanReturnEntry(3));
            pw.println("  FAILURE_WIFI_DISABLED: " + getScanReturnEntry(4));
            pw.println("mSystemStateEntries: <state><screenOn> : <scansInitiated>");
            pw.println("  WIFI_UNKNOWN       ON: " + getSystemStateCount(0, true));
            pw.println("  WIFI_DISABLED      ON: " + getSystemStateCount(1, true));
            pw.println("  WIFI_DISCONNECTED  ON: " + getSystemStateCount(2, true));
            pw.println("  WIFI_ASSOCIATED    ON: " + getSystemStateCount(3, true));
            pw.println("  WIFI_UNKNOWN      OFF: " + getSystemStateCount(0, false));
            pw.println("  WIFI_DISABLED     OFF: " + getSystemStateCount(1, false));
            pw.println("  WIFI_DISCONNECTED OFF: " + getSystemStateCount(2, false));
            pw.println("  WIFI_ASSOCIATED   OFF: " + getSystemStateCount(3, false));
            pw.println("mWifiLogProto.numConnectivityWatchdogPnoGood=" + this.mWifiLogProto.numConnectivityWatchdogPnoGood);
            pw.println("mWifiLogProto.numConnectivityWatchdogPnoBad=" + this.mWifiLogProto.numConnectivityWatchdogPnoBad);
            pw.println("mWifiLogProto.numConnectivityWatchdogBackgroundGood=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundGood);
            pw.println("mWifiLogProto.numConnectivityWatchdogBackgroundBad=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundBad);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggers=" + this.mWifiLogProto.numLastResortWatchdogTriggers);
            pw.println("mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadOther=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadOther);
            pw.println("mWifiLogProto.numLastResortWatchdogSuccesses=" + this.mWifiLogProto.numLastResortWatchdogSuccesses);
            pw.println("mWifiLogProto.recordDurationSec=" + ((this.mClock.elapsedRealtime() / 1000) - this.mRecordStartTimeSec));
            pw.println("mWifiLogProto.rssiPollRssiCount: Printing counts for [-127, 0]");
            StringBuilder sb = new StringBuilder();
            for (i = MIN_RSSI_POLL; i <= 0; i++) {
                sb.append(this.mRssiPollCounts.get(i)).append(" ");
            }
            pw.println("  " + sb.toString());
            pw.print("mWifiLogProto.alertReasonCounts=");
            sb.setLength(0);
            for (i = 0; i <= 64; i++) {
                int count = this.mWifiAlertReasonCounts.get(i);
                if (count > 0) {
                    sb.append("(").append(i).append(",").append(count).append("),");
                }
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
                pw.println(sb.toString());
            } else {
                pw.println("()");
            }
            pw.println("mWifiLogProto.numTotalScanResults=" + this.mWifiLogProto.numTotalScanResults);
            pw.println("mWifiLogProto.numOpenNetworkScanResults=" + this.mWifiLogProto.numOpenNetworkScanResults);
            pw.println("mWifiLogProto.numPersonalNetworkScanResults=" + this.mWifiLogProto.numPersonalNetworkScanResults);
            pw.println("mWifiLogProto.numEnterpriseNetworkScanResults=" + this.mWifiLogProto.numEnterpriseNetworkScanResults);
            pw.println("mWifiLogProto.numHiddenNetworkScanResults=" + this.mWifiLogProto.numHiddenNetworkScanResults);
            pw.println("mWifiLogProto.numHotspot2R1NetworkScanResults=" + this.mWifiLogProto.numHotspot2R1NetworkScanResults);
            pw.println("mWifiLogProto.numHotspot2R2NetworkScanResults=" + this.mWifiLogProto.numHotspot2R2NetworkScanResults);
            pw.println("mWifiLogProto.numScans=" + this.mWifiLogProto.numScans);
            pw.println("mWifiLogProto.WifiScoreCount: [0, 5]");
            for (i = 0; i <= 5; i++) {
                pw.print(this.mWifiScoreCounts.get(i) + " ");
            }
            pw.print("\n");
        }
    }

    private void consolidateProto(boolean incremental) {
        List<com.android.server.wifi.WifiMetricsProto.ConnectionEvent> events = new ArrayList();
        List<RssiPollCount> rssis = new ArrayList();
        List<AlertReasonCount> alertReasons = new ArrayList();
        List<WifiScoreCount> scores = new ArrayList();
        synchronized (this.mLock) {
            int i;
            for (ConnectionEvent event : this.mConnectionEventList) {
                if (!(incremental && (this.mCurrentConnectionEvent == event || event.mConnectionEvent.automaticBugReportTaken))) {
                    events.add(event.mConnectionEvent);
                    if (incremental) {
                        event.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
            }
            if (events.size() > 0) {
                this.mWifiLogProto.connectionEvent = (com.android.server.wifi.WifiMetricsProto.ConnectionEvent[]) events.toArray(this.mWifiLogProto.connectionEvent);
            }
            this.mWifiLogProto.scanReturnEntries = new ScanReturnEntry[this.mScanReturnEntries.size()];
            for (i = 0; i < this.mScanReturnEntries.size(); i++) {
                this.mWifiLogProto.scanReturnEntries[i] = new ScanReturnEntry();
                this.mWifiLogProto.scanReturnEntries[i].scanReturnCode = this.mScanReturnEntries.keyAt(i);
                this.mWifiLogProto.scanReturnEntries[i].scanResultsCount = this.mScanReturnEntries.valueAt(i);
            }
            this.mWifiLogProto.wifiSystemStateEntries = new WifiSystemStateEntry[this.mWifiSystemStateEntries.size()];
            for (i = 0; i < this.mWifiSystemStateEntries.size(); i++) {
                this.mWifiLogProto.wifiSystemStateEntries[i] = new WifiSystemStateEntry();
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiState = this.mWifiSystemStateEntries.keyAt(i) / 2;
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiStateCount = this.mWifiSystemStateEntries.valueAt(i);
                this.mWifiLogProto.wifiSystemStateEntries[i].isScreenOn = this.mWifiSystemStateEntries.keyAt(i) % 2 > 0;
            }
            this.mWifiLogProto.recordDurationSec = (int) ((this.mClock.elapsedRealtime() / 1000) - this.mRecordStartTimeSec);
            for (i = 0; i < this.mRssiPollCounts.size(); i++) {
                RssiPollCount keyVal = new RssiPollCount();
                keyVal.rssi = this.mRssiPollCounts.keyAt(i);
                keyVal.count = this.mRssiPollCounts.valueAt(i);
                rssis.add(keyVal);
            }
            this.mWifiLogProto.rssiPollRssiCount = (RssiPollCount[]) rssis.toArray(this.mWifiLogProto.rssiPollRssiCount);
            for (i = 0; i < this.mWifiAlertReasonCounts.size(); i++) {
                AlertReasonCount keyVal2 = new AlertReasonCount();
                keyVal2.reason = this.mWifiAlertReasonCounts.keyAt(i);
                keyVal2.count = this.mWifiAlertReasonCounts.valueAt(i);
                alertReasons.add(keyVal2);
            }
            this.mWifiLogProto.alertReasonCount = (AlertReasonCount[]) alertReasons.toArray(this.mWifiLogProto.alertReasonCount);
            for (int score = 0; score < this.mWifiScoreCounts.size(); score++) {
                WifiScoreCount keyVal3 = new WifiScoreCount();
                keyVal3.score = this.mWifiScoreCounts.keyAt(score);
                keyVal3.count = this.mWifiScoreCounts.valueAt(score);
                scores.add(keyVal3);
            }
            this.mWifiLogProto.wifiScoreCount = (WifiScoreCount[]) scores.toArray(this.mWifiLogProto.wifiScoreCount);
        }
    }

    private void clear() {
        synchronized (this.mLock) {
            this.mConnectionEventList.clear();
            if (this.mCurrentConnectionEvent != null) {
                this.mConnectionEventList.add(this.mCurrentConnectionEvent);
            }
            this.mScanReturnEntries.clear();
            this.mWifiSystemStateEntries.clear();
            this.mRecordStartTimeSec = this.mClock.elapsedRealtime() / 1000;
            this.mRssiPollCounts.clear();
            this.mWifiAlertReasonCounts.clear();
            this.mWifiScoreCounts.clear();
            this.mWifiLogProto.clear();
        }
    }

    public void setScreenState(boolean screenOn) {
        synchronized (this.mLock) {
            this.mScreenOn = screenOn;
        }
    }

    public void setWifiState(int wifiState) {
        synchronized (this.mLock) {
            this.mWifiState = wifiState;
        }
    }
}
