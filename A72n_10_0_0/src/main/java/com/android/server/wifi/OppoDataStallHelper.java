package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IDnsResolver;
import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import com.android.server.connectivity.gatewayconflict.OppoIPConflictDetector;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoDataStallHelper {
    private static final int ARP_RESPONSE_TIMEOUT = 5000;
    public static final int CHECK_INTERNET_DATA_STALL = 2;
    public static final int CHECK_INTERNET_DRIVER_ROAMING = 1;
    public static final int CHECK_INTERNET_UNSPECIFIED = -1;
    private static final String CMD_NAME = "cmd_name";
    private static final String CMD_TARGET = "cmd_target";
    public static final int DATA_STALL_LOG_UNSPECIFIC = -1;
    public static final int DATA_STALL_MTK_ARP_NO_RESPONSE = 513;
    public static final int DATA_STALL_MTK_BASE = 512;
    public static final int DATA_STALL_MTK_PER_HIGH = 514;
    public static final int DATA_STALL_MTK_RX_LOW_COUNT = 523;
    public static final int DATA_STALL_MTK_RX_LOW_RATE = 516;
    public static final int DATA_STALL_MTK_TEST_MODE = 512;
    public static final int DATA_STALL_MTK_TX_LOW_RATE = 515;
    public static final int DATA_STALL_QCOM_BB_WDOG_ERROR = 7;
    public static final int DATA_STALL_QCOM_FW_RX_FCS_LEN_ERROR = 5;
    public static final int DATA_STALL_QCOM_FW_RX_REFILL_FAILED = 4;
    public static final int DATA_STALL_QCOM_FW_VDEV_PAUSE = 1;
    public static final int DATA_STALL_QCOM_FW_WDOG_ERRORS = 6;
    public static final int DATA_STALL_QCOM_HOST_SOFTAP_TX_TIMEOUT = 257;
    public static final int DATA_STALL_QCOM_HOST_STA_TX_TIMEOUT = 256;
    public static final int DATA_STALL_QCOM_HWSCHED_CMD_FILTER = 2;
    public static final int DATA_STALL_QCOM_HWSCHED_CMD_FLUSH = 3;
    public static final int DATA_STALL_QCOM_NUD_FAILURE = 258;
    public static final int DATA_STALL_QCOM_POST_TIM_NO_TXRX_ERROR = 8;
    public static final int DATA_STALL_QCOM_RX_LOW_COUNT = 11;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static boolean DEBUG = false;
    private static final String DEFAULT_DNS_LOOKUP_URL = "";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final int GATEWAY_LIST_SIZE = 1;
    private static final int IDLE_SLOT_PERCENT_THRESOLD = 3;
    private static final String LOG_CMD = "com.mediatek.mtklogger.ADB_CMD";
    private static final int MAXNS = 4;
    private static final long MILLISECONDS_OF_AN_HOUR = 3600000;
    private static final long MILLISECONDS_OF_A_DAY = 86400000;
    private static final long MILLISECONDS_OF_A_MINUTE = 60000;
    private static final long MILLISECONDS_OF_A_WEEK = 604800000;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_FIX_LENGTH = 2;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_IDLESLOT_ID = 11;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_RXCURRATE_ID = 7;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_RXDUPCNT_ID = 9;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_RXERRCNT_ID = 10;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_RXTOTALCNT_ID = 8;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXACKFAILCNT_ID = 6;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXCURRATE_ID = 1;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXFAILCNT_ID = 4;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXRETRYCNT_ID = 3;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXRTSFAILCNT_ID = 5;
    private static final int MTK_WIFI_LINK_QUALITY_TAG_TXTOTALCNT_ID = 2;
    private static final int MTK_WIFI_NATIVE_LINK_QUALITY_LENGTH = 108;
    public static final int NETWORK_STATISTICS_DATA_STALL = 2;
    public static final int NETWORK_STATISTICS_DRIVER_ROAMING = 1;
    public static final int NETWORK_STATISTICS_FOR_ADD_NO_INTERNET = 4;
    public static final int NETWORK_STATISTICS_FOR_INTERNET_CHANGE = 3;
    public static final int NETWORK_STATISTICS_FOR_UNSPECIFIED = -1;
    private static final int NUD_FAILURE_MAX_PER_HOUR = 20;
    private static final int NUD_FAILURE_WEAK_RSSI = -75;
    private static final String OPPO_WIFI_LAST_REPORT_SEQ = "persist.sys.wifi.last_report_seq";
    private static final String OPPO_WIFI_LAST_REPORT_TIME = "persist.sys.wifi.last_report_time";
    private static final int PER_IDLE_SLOT_TIME = 9;
    private static final int RESOLVER_PARAMS_COUNT = 6;
    private static final int RX_PER_SECOND_ERR_CNT_THRESOLD = 5000;
    private static final int SMARTGEAR_1x1_MODE = 518;
    private static final int SMARTGEAR_2x2_MODE = 519;
    private static final int SMARTGEAR_DISABLED = 517;
    private static final int SMARTGEAR_DISCONNECT_MODE = 612;
    private static final int SMARTGEAR_SCREEN_OFF = 613;
    private static final int SMARTGEAR_SCREEN_ON = 614;
    private static final int STATS_COUNT = 7;
    private static final int STATS_ERRORS = 1;
    private static final int STATS_INTERNAL_ERRORS = 3;
    private static final int STATS_LAST_SAMPLE_TIME = 5;
    private static final int STATS_RTT_AVG = 4;
    private static final int STATS_SUCCESSES = 0;
    private static final int STATS_TIMEOUTS = 2;
    private static final int STATS_USABLE = 6;
    private static final String TAG = "OppoDataStallHelper";
    private static final int TX_DIVIDE_RX_MULTIPLE_THRESOLD = 10;
    private static final int TX_RETRY_RATIO_THRESOLD = 70;
    private static ClientModeImpl sClientModeImpl;
    private static Context sContext;
    private static OppoDataStallHelper sInstance;
    private static OppoNfHooksHelper sOppoNfHooksHelper;
    private static OppoSlaManager sOppoSlaManager;
    private static SimpleDateFormat sSimpleDateFormat;
    private static WifiConfigManager sWifiConfigManager;
    private static WifiInfo sWifiInfo;
    private static WifiNative sWifiNative;
    private static OppoWifiAssistantStateTraker sWifiNetworkStateTraker;
    private static WifiRomUpdateHelper sWifiRomUpdateHelper;
    private boolean isQcomPlatform = false;
    private int mApFaultCnt = 0;
    private int mAssistantApFaultCnt = 0;
    private int mAssistantDnsFaultCnt = 0;
    private int mAssistantDutFaultCnt = 0;
    private int mAssistantOtherFaultCnt = 0;
    private HashMap<String, Long> mDataStallMap;
    private String mDefaultHttpAddr = null;
    private int[] mDnsErrors = new int[4];
    private int[] mDnsSuccesses = new int[4];
    private int[] mDnsUsable = new int[4];
    private String[] mDnses = new String[4];
    private boolean[] mDnsesAccess = new boolean[4];
    private int mDutFaultCnt = 0;
    private int mEnvFaultCnt = 0;
    private long mFirstNudFailureTime = 0;
    private boolean mGWArpResponse = false;
    private OppoArpPeer.ArpPeerChangeCallback mGWDCallback = new OppoArpPeer.ArpPeerChangeCallback() {
        /* class com.android.server.wifi.OppoDataStallHelper.AnonymousClass2 */

        public void onArpReponseChanged(int arpResponseReceieved, Network network) {
            OppoDataStallHelper oppoDataStallHelper = OppoDataStallHelper.this;
            oppoDataStallHelper.logd("mGWDCallback onArpReponseChanged:" + arpResponseReceieved);
            OppoDataStallHelper.this.mGWArpResponse = true;
        }
    };
    private OppoIPConflictDetector mGWDetector = null;
    private boolean mIsPrivDnsAccess = false;
    private boolean mIsPrivDnsActive = false;
    private int mNudFailureCount = 0;
    private String mPrivDns = null;
    private int mSG1To2Counter = 0;
    private int mSG1x1Above300To1800SecCount = 0;
    private int mSG1x1Below300SecCount = 0;
    private long mSG1x1MinimumDuration = 86400000;
    private long mSG1x1ScreenOnWorkTimeCounter = 0;
    private long mSG1x1Time = 0;
    private long mSG1x1WorkTimeCounter = 0;
    private int mSG1x1above1800SecCount = 0;
    private int mSG2To1Counter = 0;
    private long mSG2x2MinimumDuration = 86400000;
    private long mSG2x2ScreenOnWorkTimeCounter = 0;
    private long mSG2x2Time = 0;
    private long mSG2x2WorkTimeCounter = 0;
    private final Object mSGResultLock = new Object();
    private long mSGScreenOn1x1Time = 0;
    private long mSGScreenOn2x2Time = 0;
    private boolean mScreenOn = false;
    private int mSmartGearCurrentMode = SMARTGEAR_DISCONNECT_MODE;
    private boolean mhadGwAccess = false;
    private boolean mhadInetAccess = false;
    private boolean mhasInetAccess = false;
    private Handler sAsyncHandler;
    private int sLastNetworkId;

    static /* synthetic */ int access$1308(OppoDataStallHelper x0) {
        int i = x0.mAssistantOtherFaultCnt;
        x0.mAssistantOtherFaultCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$1608(OppoDataStallHelper x0) {
        int i = x0.mAssistantDnsFaultCnt;
        x0.mAssistantDnsFaultCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$1708(OppoDataStallHelper x0) {
        int i = x0.mAssistantApFaultCnt;
        x0.mAssistantApFaultCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$2108(OppoDataStallHelper x0) {
        int i = x0.mDutFaultCnt;
        x0.mDutFaultCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$2208(OppoDataStallHelper x0) {
        int i = x0.mNudFailureCount;
        x0.mNudFailureCount = i + 1;
        return i;
    }

    private OppoDataStallHelper(Context mCtxt) {
        sContext = mCtxt;
        this.mhasInetAccess = false;
        this.mhadInetAccess = false;
        this.mhadGwAccess = false;
        HandlerThread handlerThread = new HandlerThread("DatastallCheckInternetAccess");
        handlerThread.start();
        this.sAsyncHandler = new Handler(handlerThread.getLooper());
        this.mDataStallMap = new HashMap<>();
        if (WifiRomUpdateHelper.getInstance(mCtxt).getBooleanValue("OPPO_WIFI_SMARTGEAR_FEATURE", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            sContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.wifi.OppoDataStallHelper.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        OppoDataStallHelper.this.mScreenOn = true;
                        OppoDataStallHelper.this.onSmartGearStateChange(OppoDataStallHelper.SMARTGEAR_SCREEN_ON);
                    } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                        OppoDataStallHelper.this.mScreenOn = false;
                        OppoDataStallHelper.this.onSmartGearStateChange(OppoDataStallHelper.SMARTGEAR_SCREEN_OFF);
                    }
                }
            }, filter);
        }
    }

    public static void init(Context mCtxt, ClientModeImpl mWsm, OppoWifiAssistantStateTraker mWns, WifiConfigManager mWcs, WifiRomUpdateHelper mWruh, WifiNative mWnt, OppoNfHooksHelper mOppoNfHookh) {
        sContext = mCtxt;
        sClientModeImpl = mWsm;
        sWifiNetworkStateTraker = mWns;
        sWifiConfigManager = mWcs;
        sWifiNative = mWnt;
        sWifiRomUpdateHelper = mWruh;
        sOppoNfHooksHelper = mOppoNfHookh;
        sOppoSlaManager = OppoSlaManager.getInstance(sContext);
        sSimpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    }

    public static OppoDataStallHelper getInstance() {
        if (sContext == null) {
            Log.d(TAG, "sContext is null");
            return null;
        } else if (sClientModeImpl == null) {
            Log.d(TAG, "sClientModeImpl is null");
            return null;
        } else if (sWifiConfigManager == null) {
            Log.d(TAG, "sWifiConfigManager is null");
            return null;
        } else if (sWifiNative == null) {
            Log.d(TAG, "sWifiNative is null");
            return null;
        } else {
            synchronized (OppoDataStallHelper.class) {
                if (sInstance == null) {
                    sInstance = new OppoDataStallHelper(sContext);
                }
            }
            return sInstance;
        }
    }

    /* access modifiers changed from: private */
    public class WifiDataStallInfo {
        public DhcpResults dhcpResults = new DhcpResults();
        public boolean hadGwAccess = false;
        public boolean hadInetAccess = false;
        public boolean hasGwAccess = false;
        public boolean hasInetAccess = false;
        public boolean mNeedLog = true;
        public int reason = -1;
        public String sInterfaceName;
        public WifiInfo wifiInfo = new WifiInfo();

        public WifiDataStallInfo() {
        }
    }

    /* access modifiers changed from: private */
    public class MtkWifiLinkLayerStats {
        public int curRxRate = 0;
        public long curTime = 0;
        public int curTxRate = 0;
        public long idleSlotCount = 0;
        public int rxDupCount = 0;
        public long rxErrCount = 0;
        public long rxTotalCount = 0;
        public long txAckFailCount = 0;
        public long txFailCount = 0;
        public long txRetryCount = 0;
        public long txRtsFailCount;
        public long txTotalCount = 0;

        public MtkWifiLinkLayerStats() {
        }

        public String toString() {
            return " MtkWifiLinkLayerStats: \n curTxRate: " + Integer.toString(this.curTxRate) + "\n TX Status:  txTotalCount=" + Long.toString(this.txTotalCount) + " txRetryCount=" + Long.toString(this.txRetryCount) + " txFailCount=" + Long.toString(this.txFailCount) + " txRtsFailCount=" + Long.toString(this.txRtsFailCount) + " txAckFailCount=" + Long.toString(this.txAckFailCount) + "\n curRxRate: " + Integer.toString(this.curRxRate) + "\n RX Status:  rxTotalCount=" + Long.toString(this.rxTotalCount) + " rxDupCount=" + Integer.toString(this.rxDupCount) + " rxErrCount=" + Long.toString(this.rxErrCount) + "\n idleSlot: " + Long.toString(this.idleSlotCount) + "\n curTime: " + Long.toString(this.curTime) + '\n';
        }
    }

    private void wifiPeriodicReport(int sequence, long timestamp) {
        HashMap<String, String> map = new HashMap<>();
        Date date = new Date(timestamp);
        StringBuilder value = new StringBuilder();
        value.append("Seq=" + sequence + "  " + sSimpleDateFormat.format(date));
        StringBuilder sb = new StringBuilder();
        sb.append("  SlaEnabled:");
        boolean z = true;
        sb.append(Settings.Global.getInt(sContext.getContentResolver(), OppoSlaManager.KEY_SLA_SWITCH, 0) == 1);
        value.append(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  SlaAutoEnabled:");
        if (Settings.Global.getInt(sContext.getContentResolver(), "SLA_AUTO_ENABLED", 0) != 1) {
            z = false;
        }
        sb2.append(z);
        value.append(sb2.toString());
        value.append("  SlaCancelCount:" + Settings.System.getInt(sContext.getContentResolver(), "SLA_CANCEL_COUNT", 0));
        String slaTraffic = SystemProperties.get("persist.sys.sla.traffic", DEFAULT_DNS_LOOKUP_URL);
        if (!TextUtils.isEmpty(slaTraffic)) {
            value.append("  SlaTraffic:" + Arrays.toString(slaTraffic.split("-")));
        }
        value.append("  " + sOppoSlaManager.getSlaAppsTraffic());
        value.append("  " + sOppoSlaManager.getAvgSpeedAndRtt());
        value.append("  " + sOppoSlaManager.getAllGameStats());
        value.append("  LM_Count:" + sOppoNfHooksHelper.getLuckyMoneyCount());
        map.put("wifi_periodic_report_sla", value.toString());
        if (sWifiRomUpdateHelper.getBooleanValue("OPPO_WIFI_SMARTGEAR_FEATURE", false)) {
            smartGearPeriodReport(map);
        }
        logd("wifiPeriodicReport map:" + map);
        OppoStatistics.onCommon(sContext, "wifi_fool_proof", "wifi_periodic_report", map, false);
    }

    private void checkQcomTxRxBasedOnL2(WifiLinkLayerStats old, WifiLinkLayerStats current) {
        if (old == null || current == null) {
            logd("WiFi L2 log - Collect L2 status error.");
            this.mAssistantOtherFaultCnt++;
            return;
        }
        logd("WiFi L2 log - beacon rx  :" + (current.beacon_rx - old.beacon_rx));
        logd("WiFi L2 log - mgmt rssi  :" + current.rssi_mgmt);
        logd("WiFi L2 log - tx time    :" + (current.tx_time - old.tx_time));
        logd("WiFi L2 log - rx time    :" + (current.rx_time - old.rx_time));
        logd("WiFi L2 log - TX :");
        logd("WiFi L2 log - BE txmpdu  :" + (current.txmpdu_be - old.txmpdu_be));
        logd("WiFi L2 log - BK txmpdu  :" + (current.txmpdu_bk - old.txmpdu_bk));
        logd("WiFi L2 log - VO txmpdu  :" + (current.txmpdu_vo - old.txmpdu_vo));
        logd("WiFi L2 log - VI txmpdu  :" + (current.txmpdu_vi - old.txmpdu_vi));
        logd("WiFi L2 log - TX lost:");
        logd("WiFi L2 log - BE tx lost :" + (current.rxmpdu_be - old.rxmpdu_be));
        logd("WiFi L2 log - BK tx lost :" + (current.rxmpdu_bk - old.rxmpdu_bk));
        logd("WiFi L2 log - VO tx lost :" + (current.rxmpdu_vo - old.rxmpdu_vo));
        logd("WiFi L2 log - VI tx lost :" + (current.rxmpdu_vi - old.rxmpdu_vi));
        logd("WiFi L2 log - RX :");
        logd("WiFi L2 log - BE rxmpdu  :" + (current.rxmpdu_be - old.rxmpdu_be));
        logd("WiFi L2 log - BK rxmpdu  :" + (current.rxmpdu_bk - old.rxmpdu_bk));
        logd("WiFi L2 log - VO rxmpdu  :" + (current.rxmpdu_vo - old.rxmpdu_vo));
        logd("WiFi L2 log - VI rxmpdu  :" + (current.rxmpdu_vi - old.rxmpdu_vi));
        int totalRxedBeacon = current.beacon_rx - old.beacon_rx;
        int totalTxTime = current.tx_time - old.tx_time;
        int totalRxTime = current.rx_time - old.rx_time;
        long totalAcTxCnt = (current.txmpdu_be - old.txmpdu_be) + (current.txmpdu_bk - old.txmpdu_bk) + (current.txmpdu_vo - old.txmpdu_vo) + (current.txmpdu_vi - old.txmpdu_vi);
        long totalAcRxCnt = (current.rxmpdu_be - old.rxmpdu_be) + (current.rxmpdu_bk - old.rxmpdu_bk) + (current.rxmpdu_vo - old.rxmpdu_vo) + (current.rxmpdu_vi - old.rxmpdu_vi);
        long totalAcTxFailedCnt = (current.lostmpdu_be - old.lostmpdu_be) + (current.lostmpdu_bk - old.lostmpdu_bk) + (current.lostmpdu_vo - old.lostmpdu_vo) + (current.lostmpdu_vi - old.lostmpdu_vi);
        long totalAcTxRetriedCnt = (current.retries_be - old.retries_be) + (current.retries_bk - old.retries_bk) + (current.retries_vo - old.retries_vo) + (current.retries_vi - old.retries_vi);
        if (current.rssi_mgmt < NUD_FAILURE_WEAK_RSSI) {
            logd("WiFi L2 log - RSSI is too weak : " + current.rssi_mgmt);
            this.mAssistantOtherFaultCnt = this.mAssistantOtherFaultCnt + 1;
        } else if (totalAcTxCnt == 0 || totalAcRxCnt != 0 || totalRxedBeacon == 0) {
            int rusTxRetryRatio = sWifiRomUpdateHelper.getIntegerValue("DATA_STALL_TX_RETRY_RATIO_THRESOLD", Integer.valueOf((int) TX_RETRY_RATIO_THRESOLD)).intValue();
            if (totalAcTxCnt + totalAcTxRetriedCnt + totalAcTxFailedCnt != 0) {
                float retryTxRatio = (float) (((totalAcTxRetriedCnt + totalAcTxFailedCnt) * 100) / ((totalAcTxCnt + totalAcTxRetriedCnt) + totalAcTxFailedCnt));
                if (retryTxRatio >= ((float) rusTxRetryRatio)) {
                    logd("WiFi L2 log - Air was too busy. Retry ratio = " + retryTxRatio);
                    this.mAssistantOtherFaultCnt = this.mAssistantOtherFaultCnt + 1;
                    return;
                }
            }
            int rusTxDvideRx = sWifiRomUpdateHelper.getIntegerValue("DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD", 10).intValue();
            if (totalAcTxCnt != 0 && totalAcRxCnt != 0 && totalRxedBeacon != 0) {
                if (totalAcTxCnt / totalAcRxCnt > ((long) rusTxDvideRx)) {
                    logd("WiFi L2 log - Possible AP's L3 issue. DUT TXed packets outnumbered RXed packets");
                    this.mAssistantApFaultCnt++;
                    return;
                }
            }
            if ((totalAcTxCnt == 0 && totalAcTxFailedCnt != 0) || totalTxTime == 0) {
                logd("WiFi L2 log - DUT didn't tx suucess at all. Could be a problem");
                this.mAssistantDutFaultCnt++;
            } else if (totalRxTime == 0) {
                logd("WiFi L2 log - DUT didn't rx at all. Could be a problem");
                this.mAssistantDutFaultCnt++;
            } else {
                logd("WiFi L2 log - None of above");
                this.mAssistantDutFaultCnt++;
            }
        } else {
            logd("WiFi L2 log - Possible AP's L3 issue");
            this.mAssistantApFaultCnt++;
        }
    }

    private void checkMtkTxRxBasedOnL2(MtkWifiLinkLayerStats old, MtkWifiLinkLayerStats current) {
        int perSecondRxErr;
        if (old == null || current == null) {
            logd("WiFi L2 log - Collect L2 status error.");
            this.mAssistantOtherFaultCnt++;
            return;
        }
        logd("WiFi L2 log - TX :");
        logd("WiFi L2 log - curTxRate           :" + current.curTxRate);
        logd("WiFi L2 log - txTotalCount    diff:" + (current.txTotalCount - old.txTotalCount));
        logd("WiFi L2 log - txRetryCount    diff:" + (current.txRetryCount - old.txRetryCount));
        logd("WiFi L2 log - txFailCount     diff:" + (current.txFailCount - old.txFailCount));
        logd("WiFi L2 log - txRtsFailCount  diff:" + (current.txRtsFailCount - old.txRtsFailCount));
        logd("WiFi L2 log - txAckFailCount  diff:" + (current.txAckFailCount - old.txAckFailCount));
        logd("WiFi L2 log - RX :");
        logd("WiFi L2 log - curRxRate           :" + current.curRxRate);
        logd("WiFi L2 log - rxTotalCount    diff:" + (current.rxTotalCount - old.rxTotalCount));
        logd("WiFi L2 log - rxDupCount      diff:" + (current.rxDupCount - old.rxDupCount));
        logd("WiFi L2 log - rxErrCount      diff:" + (current.rxErrCount - old.rxErrCount));
        logd("WiFi L2 log - idleSlotCount   diff:" + (current.idleSlotCount - old.idleSlotCount));
        logd("WiFi L2 log - difftime        diff:" + (current.curTime - old.curTime));
        long txTotalDataAckCnt = ((current.txTotalCount - old.txTotalCount) - (current.txRetryCount - old.txRetryCount)) - (current.txFailCount - old.txFailCount);
        long rxTotalDataCnt = current.rxTotalCount - old.rxTotalCount;
        long rxTotalErrCnt = current.rxErrCount - old.rxErrCount;
        long idleSlotCnt = current.idleSlotCount - old.idleSlotCount;
        long diffTime = current.curTime - old.curTime;
        if (txTotalDataAckCnt == 0 || rxTotalDataCnt != 0) {
            int rusRxErr = sWifiRomUpdateHelper.getIntegerValue("DATA_STALL_RX_PER_SECOND_ERR_CNT_THRESOLD", 5000).intValue();
            if (rxTotalDataCnt == 0 || rxTotalErrCnt == 0 || diffTime <= 0 || (perSecondRxErr = (int) ((rxTotalErrCnt * 1000) / diffTime)) < rusRxErr) {
                int rusIdlePercent = sWifiRomUpdateHelper.getIntegerValue("DATA_STALL_IDLE_SLOT_PERCENT_THRESOLD", 3).intValue();
                if (idleSlotCnt > 0 && diffTime > 0) {
                    int idlepercent = (int) (((9 * idleSlotCnt) * 100) / (1000 * diffTime));
                    if (idlepercent < rusIdlePercent) {
                        logd("WiFi L2 log - Air was too busy. diff idlepercent = " + idlepercent);
                        this.mAssistantOtherFaultCnt = this.mAssistantOtherFaultCnt + 1;
                        return;
                    }
                }
                int rusTxDvideRx = sWifiRomUpdateHelper.getIntegerValue("DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD", 10).intValue();
                if (txTotalDataAckCnt != 0 && rxTotalDataCnt != 0) {
                    if (txTotalDataAckCnt / rxTotalDataCnt > ((long) rusTxDvideRx)) {
                        logd("WiFi L2 log - Possible AP's L3 issue. DUT TXed packets outnumbered RXed packets");
                        this.mAssistantApFaultCnt++;
                        return;
                    }
                }
                logd("WiFi L2 log - None of above");
                this.mAssistantDutFaultCnt++;
                return;
            }
            logd("WiFi L2 log - Air was too busy. RX Err threshold = " + perSecondRxErr);
            this.mAssistantOtherFaultCnt = this.mAssistantOtherFaultCnt + 1;
            return;
        }
        logd("WiFi L2 log - Possible AP's L3 issue");
        this.mAssistantApFaultCnt++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkTxRxBasedOnL2(boolean isQcomPlatform2, WifiLinkLayerStats old, MtkWifiLinkLayerStats mtkOld) {
        if (isQcomPlatform2) {
            checkQcomTxRxBasedOnL2(old, getQcomLinkLayerStats());
        } else {
            checkMtkTxRxBasedOnL2(mtkOld, getMtkLinkLayerStats());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String lookup(String name) {
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(name)) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(name);
                for (int i = 0; i < addresses.length; i++) {
                    sb.append(addresses[i].getHostAddress() + ",");
                }
            } catch (UnknownHostException e) {
                logd("Unable to resolve: " + name);
            }
        }
        logd("Resolve:" + name + " results: " + sb.toString());
        return sb.toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearDnsParam() {
        this.mIsPrivDnsActive = false;
        this.mIsPrivDnsAccess = false;
        this.mPrivDns = null;
        this.mDefaultHttpAddr = null;
        for (int i = 0; i < 4; i++) {
            this.mDnses[i] = null;
            this.mDnsSuccesses[i] = 0;
            this.mDnsErrors[i] = 0;
            this.mDnsUsable[i] = 0;
            this.mDnsesAccess[i] = false;
        }
    }

    private void syncPrivateDns(LinkProperties lp) {
        this.mIsPrivDnsActive = lp.isPrivateDnsActive();
        this.mPrivDns = lp.getPrivateDnsServerName();
        if (!this.mIsPrivDnsActive || TextUtils.isEmpty(this.mPrivDns)) {
            this.mIsPrivDnsAccess = false;
            return;
        }
        try {
            InetAddress dnsAddress = InetAddress.getByName(this.mPrivDns);
            if (dnsAddress != null) {
                this.mIsPrivDnsAccess = dnsAddress.isReachable(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
            }
        } catch (IOException e) {
            this.mIsPrivDnsAccess = false;
        }
    }

    /* JADX INFO: Multiple debug info for r2v3 int[]: [D('cm' android.net.ConnectivityManager), D('params' int[])] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void syncResolverInfo() {
        InetAddress dnsAddress;
        LinkProperties lp = ((ConnectivityManager) sContext.getSystemService("connectivity")).getLinkProperties(1);
        if (lp != null) {
            syncPrivateDns(lp);
            Network nw = sClientModeImpl.getCurrentNetwork();
            IDnsResolver dnsResolver = IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
            Collection<InetAddress> curDnses = lp.getDnsServers();
            String curDomains = lp.getDomains();
            if (nw != null && dnsResolver != null) {
                if (curDnses != null) {
                    int netId = nw.netId;
                    int dnsNum = curDnses.size() > 4 ? 4 : curDnses.size();
                    String[] dnses = new String[dnsNum];
                    int[] stats = new int[(dnsNum * 7)];
                    InetAddress dnsAddress2 = null;
                    try {
                        dnsResolver.getResolverInfo(netId, dnses, curDomains == null ? new String[0] : curDomains.split(" "), new String[dnsNum], new int[6], stats, new int[4]);
                        for (int i = 0; i < dnses.length; i++) {
                            if (dnses[i] != null) {
                                this.mDnses[i] = new String(dnses[i]);
                                this.mDnsSuccesses[i] = stats[(i * 7) + 0];
                                this.mDnsErrors[i] = stats[(i * 7) + 1] + stats[(i * 7) + 2] + stats[(i * 7) + 3];
                                this.mDnsUsable[i] = stats[(i * 7) + 6];
                                try {
                                    dnsAddress = InetAddress.getByName(this.mDnses[i]);
                                    if (dnsAddress != null) {
                                        try {
                                            this.mDnsesAccess[i] = dnsAddress.isReachable(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                                        } catch (IOException e) {
                                        }
                                    }
                                } catch (IOException e2) {
                                    dnsAddress = dnsAddress2;
                                    this.mDnsesAccess[i] = false;
                                    logd("dns" + i + ": " + this.mDnses[i] + " suc: " + this.mDnsSuccesses[i] + " err: " + this.mDnsErrors[i] + " usable: " + this.mDnsUsable[i] + " access: " + this.mDnsesAccess[i]);
                                    dnsAddress2 = dnsAddress;
                                }
                                logd("dns" + i + ": " + this.mDnses[i] + " suc: " + this.mDnsSuccesses[i] + " err: " + this.mDnsErrors[i] + " usable: " + this.mDnsUsable[i] + " access: " + this.mDnsesAccess[i]);
                                dnsAddress2 = dnsAddress;
                            }
                        }
                    } catch (Exception e3) {
                        logd("getResolverInfo error:" + e3);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x005c  */
    private void startGWDetector(WifiDataStallInfo dataStallInfo) {
        this.mGWArpResponse = false;
        if (dataStallInfo != null && dataStallInfo.dhcpResults != null) {
            DhcpResults dhcp = dataStallInfo.dhcpResults;
            if (dhcp.ipAddress == null || dhcp.gateway == null || dhcp.ipAddress.getAddress() == null) {
                logd("null dhcp");
                return;
            }
            OppoIPConflictDetector oppoIPConflictDetector = this.mGWDetector;
            if (oppoIPConflictDetector != null) {
                oppoIPConflictDetector.close();
            }
            this.mGWDetector = new OppoIPConflictDetector(sContext, (NetworkAgentInfo) null, this.mGWDCallback);
            if (this.mGWDetector != null) {
                logd("doDupArp");
                this.mGWDetector.doDupArp(dataStallInfo.sInterfaceName, (Inet4Address) dhcp.ipAddress.getAddress(), (Inet4Address) dhcp.gateway);
                long timeout = SystemClock.elapsedRealtime() + RttServiceImpl.HAL_RANGING_TIMEOUT_MS;
                while (SystemClock.elapsedRealtime() < timeout && !this.mGWArpResponse) {
                    while (SystemClock.elapsedRealtime() < timeout) {
                        while (SystemClock.elapsedRealtime() < timeout) {
                        }
                    }
                }
                this.mGWDetector.close();
                this.mGWDetector = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveNoInternetAccessInfo(WifiDataStallInfo dataStallInfo, int triggerSource) {
        float sucRate;
        HashMap<String, String> map = new HashMap<>();
        boolean isStaticIp = false;
        WifiConfiguration config = sWifiConfigManager.getConfiguredNetwork(dataStallInfo.wifiInfo.getNetworkId());
        if (config != null) {
            map.put("config_key", config.configKey());
            isStaticIp = IpConfiguration.IpAssignment.STATIC == config.getIpAssignment();
        }
        map.put("channel", String.valueOf(dataStallInfo.wifiInfo.getFrequency()));
        map.put("rssi", String.valueOf(dataStallInfo.wifiInfo.getRssi()));
        map.put("vendor_spec", sWifiNative.getApVendorSpec(dataStallInfo.sInterfaceName));
        map.put("static_ip", String.valueOf(isStaticIp));
        map.put("inet_access_before", String.valueOf(dataStallInfo.hadInetAccess));
        map.put("inet_access_now", String.valueOf(dataStallInfo.hasInetAccess));
        map.put("gw_access_before", String.valueOf(dataStallInfo.hadGwAccess));
        map.put("gw_access_now", String.valueOf(dataStallInfo.hasGwAccess));
        if (dataStallInfo.dhcpResults != null) {
            map.put("vendor_info", dataStallInfo.dhcpResults.vendorInfo);
            map.put("dupServer", String.valueOf(dataStallInfo.dhcpResults.dupServer));
        }
        map.put("dns_resolve", this.mDefaultHttpAddr);
        for (int i = 0; i < 4; i++) {
            if (this.mDnses[i] != null) {
                int[] iArr = this.mDnsSuccesses;
                int dnsSum = iArr[i] + this.mDnsErrors[i];
                if (dnsSum > 0) {
                    sucRate = (float) (iArr[i] / dnsSum);
                } else {
                    sucRate = -1.0f;
                }
                DecimalFormat dFormat = new DecimalFormat("#.##");
                map.put("dns" + i, this.mDnses[i]);
                map.put("dns_srate" + i, dFormat.format((double) sucRate));
                map.put("dns_sum" + i, String.valueOf(dnsSum));
                map.put("dns_access" + i, String.valueOf(this.mDnsesAccess[i]));
            }
        }
        map.put("priv_dns", String.valueOf(this.mIsPrivDnsActive));
        if (this.mIsPrivDnsActive) {
            map.put("priv_dns_server", this.mPrivDns);
            map.put("priv_dns_access", String.valueOf(this.mIsPrivDnsAccess));
        }
        map.put("source", String.valueOf(triggerSource));
        OppoStatistics.onCommon(sContext, "wifi_fool_proof", "wifi_no_internet", map, false);
    }

    public void networkStatistic(final int checkReason, ExtendedWifiInfo wifiInfo, int lastNetworkId, String interfaceName) {
        ClientModeImpl clientModeImpl = sClientModeImpl;
        if (!ClientModeImpl.isNotChineseOperator()) {
            if (sWifiRomUpdateHelper.getBooleanValue("NETWORK_STATISTICS_FOR_NO_INTERNET", true) || !(checkReason == 4 || checkReason == 3)) {
                this.isQcomPlatform = true ^ isMtkPlatform();
                final WifiDataStallInfo dataStallInfo = new WifiDataStallInfo();
                dataStallInfo.hadInetAccess = this.mhadInetAccess;
                dataStallInfo.hadGwAccess = this.mhadGwAccess;
                dataStallInfo.reason = 0;
                dataStallInfo.wifiInfo = new WifiInfo(wifiInfo);
                dataStallInfo.dhcpResults = sClientModeImpl.syncGetDhcpResults();
                dataStallInfo.sInterfaceName = interfaceName;
                dataStallInfo.mNeedLog = false;
                this.sLastNetworkId = lastNetworkId;
                Handler handler = this.sAsyncHandler;
                if (handler != null) {
                    handler.post(new Runnable() {
                        /* class com.android.server.wifi.OppoDataStallHelper.AnonymousClass3 */

                        public void run() {
                            WifiLinkLayerStats qcomOldL2Stats = null;
                            MtkWifiLinkLayerStats mtkOldL2Stats = null;
                            if (OppoDataStallHelper.this.isQcomPlatform) {
                                qcomOldL2Stats = OppoDataStallHelper.this.getQcomLinkLayerStats();
                            } else {
                                mtkOldL2Stats = OppoDataStallHelper.this.getMtkLinkLayerStats();
                                OppoDataStallHelper.this.logd("MTK WiFi L2 log - get before check internet L2 status ");
                            }
                            OppoDataStallHelper.this.startGWDetector(dataStallInfo);
                            dataStallInfo.hasGwAccess = OppoDataStallHelper.this.mGWArpResponse;
                            OppoDataStallHelper.this.mhadGwAccess = dataStallInfo.hasGwAccess ? true : OppoDataStallHelper.this.mhadGwAccess;
                            OppoDataStallHelper.this.clearDnsParam();
                            OppoDataStallHelper oppoDataStallHelper = OppoDataStallHelper.this;
                            oppoDataStallHelper.mDefaultHttpAddr = oppoDataStallHelper.lookup(OppoDataStallHelper.DEFAULT_DNS_LOOKUP_URL);
                            OppoDataStallHelper.this.syncResolverInfo();
                            if (checkReason == 3) {
                                if (dataStallInfo.wifiInfo.getRssi() < OppoDataStallHelper.NUD_FAILURE_WEAK_RSSI) {
                                    OppoDataStallHelper.access$1308(OppoDataStallHelper.this);
                                    OppoDataStallHelper.this.logd("fault for low rssi");
                                } else if (!dataStallInfo.hasGwAccess) {
                                    OppoDataStallHelper oppoDataStallHelper2 = OppoDataStallHelper.this;
                                    oppoDataStallHelper2.checkTxRxBasedOnL2(oppoDataStallHelper2.isQcomPlatform, qcomOldL2Stats, mtkOldL2Stats);
                                } else if (!OppoDataStallHelper.this.mIsPrivDnsActive || OppoDataStallHelper.this.mIsPrivDnsAccess) {
                                    OppoDataStallHelper.access$1708(OppoDataStallHelper.this);
                                } else {
                                    OppoDataStallHelper.access$1608(OppoDataStallHelper.this);
                                    OppoDataStallHelper oppoDataStallHelper3 = OppoDataStallHelper.this;
                                    oppoDataStallHelper3.logd("Dns Fault Cnt: " + OppoDataStallHelper.this.mAssistantDnsFaultCnt);
                                }
                            }
                            if (OppoDataStallHelper.sClientModeImpl.isConnected() && OppoDataStallHelper.sClientModeImpl.getNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                                OppoDataStallHelper.this.saveNoInternetAccessInfo(dataStallInfo, checkReason);
                            }
                        }
                    });
                    return;
                }
                return;
            }
            loge("Network statistics for no Internet is disabled");
        }
    }

    private void dualStaBuriedPiontInfoReport(int sequence, long timestamp) {
        if (sOppoSlaManager.isDualStaSupported()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("date", sSimpleDateFormat.format(new Date(timestamp)));
            map.put("settingSwitchState", sOppoSlaManager.getSettingSwitchEnableState());
            map.put("settingEnableCount", sOppoSlaManager.getSettingSwitchEnableCount());
            map.put("rusSwitchState", sOppoSlaManager.getRusSwitchEnableState());
            map.put("rusEnableCount", sOppoSlaManager.getRusSwitchEnableCount());
            map.put("totalAcTime", sOppoSlaManager.getTotalActiveTime());
            map.put("failAcCount", sOppoSlaManager.getFailAcCount());
            map.put("successAcCount", sOppoSlaManager.getSuccessActiveCount());
            map.put("downloadAcCount", sOppoSlaManager.getDownloadActiveCount());
            map.put("lowSpeedAcCount", sOppoSlaManager.getLowSpeedActiveCount());
            map.put("lowScoreAcCount", sOppoSlaManager.getLowScoreActiveCount());
            map.put("reuqestAcCount", sOppoSlaManager.getRequestActiveCount());
            map.put("manualAcCount", sOppoSlaManager.getManualActiveCount());
            map.put("otherDisableCount", sOppoSlaManager.getOtherDisableCount());
            map.put("kernelDisableCount", sOppoSlaManager.getKernelDisableCount());
            map.put("reuqestDisableCount", sOppoSlaManager.getRequestDisableCount());
            map.put("manualDisableCount", sOppoSlaManager.getManualDisableCount());
            map.put("screenOffDisCount", sOppoSlaManager.getScreenOffDisableCount());
            map.put("freq24gAnd24gCount", sOppoSlaManager.getFreq24gAnd24gCount());
            map.put("freq24gAnd5gCount", sOppoSlaManager.getFreq24gAnd5gCount());
            map.put("freq5gAnd24gCount", sOppoSlaManager.getFreq5gAnd24gCount());
            map.put("freq5gAnd5gCount", sOppoSlaManager.getFreq5gAnd5gCount());
            logd("dualStaBuriedPiont map:" + map);
            OppoStatistics.onCommon(sContext, "wifi_fool_proof", "wifi_dual_sta", map, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveDataStallInfo(WifiDataStallInfo dataStallInfo) {
        Bundle extras;
        Parcelable p;
        HashMap<String, String> map = new HashMap<>();
        getImei();
        LocationManager locationManager = (LocationManager) sContext.getSystemService("location");
        if (locationManager != null) {
            Location loc = null;
            try {
                loc = locationManager.getLastKnownLocation("network");
            } catch (SecurityException se) {
                loge("saveDataStallInfo failed to get location:" + se);
            } catch (IllegalArgumentException ie) {
                loge("saveDataStallInfo failed to get location:" + ie);
            }
            if (!(loc == null || (extras = loc.getExtras()) == null || (p = extras.getParcelable("address")) == null || !(p instanceof Address))) {
                Address address = (Address) p;
                String location = address.getAdminArea() + address.getLocality() + address.getSubLocality();
            }
        }
        if (this.isQcomPlatform) {
            map.put("platform", "Qcom");
            map.put("reason", String.valueOf(dataStallInfo.reason));
        } else {
            map.put("platform", "Mtk");
            map.put("reason", String.valueOf(dataStallInfo.reason - 512));
        }
        map.put("inet_access_before", String.valueOf(dataStallInfo.hadInetAccess));
        map.put("inet_access_now", String.valueOf(dataStallInfo.hasInetAccess));
        map.put("gw_access_before", String.valueOf(dataStallInfo.hadGwAccess));
        map.put("gw_access_now", String.valueOf(dataStallInfo.hasGwAccess));
        map.put("mac_addr", sWifiNative.getMacAddress(dataStallInfo.sInterfaceName));
        map.put(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_SSID, dataStallInfo.wifiInfo.getSSID());
        map.put("bssid", dataStallInfo.wifiInfo.getBSSID());
        map.put("channel", String.valueOf(dataStallInfo.wifiInfo.getFrequency()));
        map.put("rssi", String.valueOf(dataStallInfo.wifiInfo.getRssi()));
        map.put("vendor_spec", sWifiNative.getApVendorSpec(dataStallInfo.sInterfaceName));
        map.put("DUTs_fault", String.valueOf(dataStallInfo.mNeedLog));
        logd("saveDataStallInfo map:" + map);
        OppoStatistics.onCommon(sContext, "wifi_fool_proof", "wifi_data_stall", map, false);
    }

    private void startCaptureLogs(int reason, int sequence, boolean isQcom) {
        if (!shouldCaptureLogsForReason(reason)) {
            logd("don't startCaptureLogs reason=" + reason);
        } else if (isQcom) {
            Intent intent = new Intent("oppo.intent.log.customer");
            intent.addFlags(536870912);
            intent.putExtra("logtype", 63);
            intent.putExtra("duration", 60);
            String product = SystemProperties.get("ro.separate.soft", DEFAULT_DNS_LOOKUP_URL);
            intent.putExtra("name", "WDS_" + product + "_" + reason);
            intent.putExtra("sequence", sequence);
            intent.addFlags(16777216);
            intent.setPackage("com.oppo.logkit");
            sContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            logd("QC startCaptureLogs reason=" + reason + " seq=" + sequence);
        } else {
            logd("MTK startCaptureLogs reason=" + reason + " seq=" + sequence);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerLogUpload(int reason, int sequence, boolean isQcom) {
        if (!shouldCaptureLogsForReason(reason)) {
            logd("don't triggerLogUpload reason=" + reason);
        } else if (isQcom) {
            Intent intent = new Intent("oppo.intent.log.customer.retain");
            intent.addFlags(536870912);
            intent.putExtra("sequence", sequence);
            intent.addFlags(16777216);
            intent.setPackage("com.oppo.logkit");
            sContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            logd("QC triggerLogUpload seq=" + sequence);
        } else {
            logd("MTK triggerLogUpload " + sequence);
        }
    }

    private boolean shouldCaptureLogsForReason(int reason) {
        String reasonStr = getRomUpdateValue("DATA_STALL_DONT_UPLOAD_LOG_REASON", null);
        if (reasonStr == null) {
            return true;
        }
        String[] reasonList = reasonStr.split(",");
        for (int i = 0; i < reasonList.length; i++) {
            try {
                if (reason == Integer.parseInt(reasonList[i])) {
                    logd("don't startCaptureLogs reason=" + reason);
                    return false;
                }
            } catch (NumberFormatException e) {
                loge("notCaptureLogsForReason " + reason);
            }
        }
        return true;
    }

    private String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService("phone");
        if (telephonyManager == null) {
            return "null";
        }
        String imei = telephonyManager.getImei();
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        return "null";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkImei() {
        getImei();
        logd("checkImei passed!");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doNudFailureRecover() {
        if (sClientModeImpl.isConnected() && sClientModeImpl.getNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            logd("doNudFailureRecover mNudFailureCount = " + this.mNudFailureCount);
            sClientModeImpl.startRoamToNetwork(this.sLastNetworkId, null);
        }
    }

    public void setEverHadInetAccess(boolean mEverHadInetAccess) {
        this.mhadInetAccess = mEverHadInetAccess;
    }

    public void setEverHadGwAccess(boolean mEverHadGwAccess) {
        this.mhadGwAccess = mEverHadGwAccess;
    }

    public void setHasInetAccess(boolean mNowhasInetAccess) {
        this.mhasInetAccess = mNowhasInetAccess;
        this.mhadInetAccess = this.mhasInetAccess ? true : this.mhadInetAccess;
    }

    private void smartGearPeriodReport(HashMap<String, String> map) {
        synchronized (this.mSGResultLock) {
            if (map != null) {
                map.put("SG1x1WorkTimeCount", getSG1x1WorkTimeCount());
                map.put("SG2x2WorkTimeCount", getSG2x2WorkTimeCount());
                map.put("SG1x1ScreenOnWorkTimeCount", getSGScreenOn1x1WorkTimeCount());
                map.put("SG2x2ScreenOnWorkTimeCount", getSGScreenOn2x2WorkTimeCount());
                map.put("SG1To2Count", getSG1To2Count());
                map.put("SG2To1Count", getSG2To1Count());
                map.put("SG1x1MinimumDuration", getSG1x1MinimumDuration());
                map.put("SG2x2MinimumDuration", getSG2x2MinimumDuration());
                map.put("SGThreeLevel", getTreeLeveCount());
                logd("smartGearPeriodReport map:" + map);
            }
        }
    }

    private String getTreeLeveCount() {
        String tmpStr = DEFAULT_DNS_LOOKUP_URL + this.mSG1x1Below300SecCount + ":" + this.mSG1x1Above300To1800SecCount + ":" + this.mSG1x1above1800SecCount;
        this.mSG1x1Below300SecCount = 0;
        this.mSG1x1Above300To1800SecCount = 0;
        this.mSG1x1above1800SecCount = 0;
        return tmpStr;
    }

    private String getSG1x1MinimumDuration() {
        long count = this.mSG1x1MinimumDuration;
        if (86400000 == count) {
            return "0";
        }
        this.mSG1x1MinimumDuration = 86400000;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSG2x2MinimumDuration() {
        long count = this.mSG2x2MinimumDuration;
        if (86400000 == count) {
            return "0";
        }
        this.mSG2x2MinimumDuration = 86400000;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSG1To2Count() {
        int count = this.mSG1To2Counter;
        this.mSG1To2Counter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSG2To1Count() {
        int count = this.mSG2To1Counter;
        this.mSG2To1Counter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSG1x1WorkTimeCount() {
        long count = this.mSG1x1WorkTimeCounter;
        this.mSG1x1WorkTimeCounter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    public String getSG2x2WorkTimeCount() {
        long count = this.mSG2x2WorkTimeCounter;
        this.mSG2x2WorkTimeCounter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSGScreenOn1x1WorkTimeCount() {
        long count = this.mSG1x1ScreenOnWorkTimeCounter;
        this.mSG1x1ScreenOnWorkTimeCounter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    private String getSGScreenOn2x2WorkTimeCount() {
        long count = this.mSG2x2ScreenOnWorkTimeCounter;
        this.mSG2x2ScreenOnWorkTimeCounter = 0;
        return DEFAULT_DNS_LOOKUP_URL + count;
    }

    public void setSmartGearDisconnect() {
        logd("setSmartGearDisconnect");
        onSmartGearStateChange(SMARTGEAR_DISCONNECT_MODE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x032e A[Catch:{ all -> 0x034c, all -> 0x0403 }] */
    private boolean onSmartGearStateChange(int mode) {
        Object obj;
        if (sWifiRomUpdateHelper.getBooleanValue("OPPO_WIFI_SMARTGEAR_FEATURE", false)) {
            if (SMARTGEAR_1x1_MODE == mode || SMARTGEAR_2x2_MODE == mode || SMARTGEAR_DISABLED == mode || SMARTGEAR_DISCONNECT_MODE == mode || SMARTGEAR_SCREEN_OFF == mode || SMARTGEAR_SCREEN_ON == mode) {
                logd("onSmartGearStateChange mode = " + mode + "mSmartGearCurrentMode = " + this.mSmartGearCurrentMode);
                if (SMARTGEAR_DISABLED == mode) {
                    logd("SmartGear CurrentState is OFF");
                    return true;
                }
                Object obj2 = this.mSGResultLock;
                synchronized (obj2) {
                    try {
                        if (this.mSmartGearCurrentMode != mode) {
                            long currentTime = System.currentTimeMillis() / 1000;
                            if (mode == SMARTGEAR_1x1_MODE) {
                                obj = obj2;
                                this.mSG1x1Time = currentTime;
                                this.mSGScreenOn1x1Time = currentTime;
                                if (this.mSG2x2Time != 0 && currentTime > this.mSG2x2Time) {
                                    long time = currentTime - this.mSG2x2Time;
                                    this.mSG2x2WorkTimeCounter += time;
                                    logd("SG2x2Work for[" + time + "] seconds all2x2time:" + this.mSG2x2WorkTimeCounter);
                                    this.mSG2x2Time = 0;
                                    if (time < this.mSG2x2MinimumDuration) {
                                        this.mSG2x2MinimumDuration = time;
                                    }
                                }
                                if (this.mSGScreenOn2x2Time != 0 && currentTime > this.mSGScreenOn2x2Time && this.mScreenOn) {
                                    long time2 = currentTime - this.mSGScreenOn2x2Time;
                                    logd("SG2x2Work for[" + time2 + "] seconds allscreenon2x2time:" + this.mSG2x2ScreenOnWorkTimeCounter);
                                    this.mSG2x2ScreenOnWorkTimeCounter = this.mSG2x2ScreenOnWorkTimeCounter + time2;
                                    this.mSGScreenOn2x2Time = 0;
                                }
                                if (SMARTGEAR_DISCONNECT_MODE != this.mSmartGearCurrentMode) {
                                    this.mSG2To1Counter++;
                                }
                                logd("mSG2To1Counter = " + this.mSG2To1Counter);
                            } else if (mode != SMARTGEAR_2x2_MODE) {
                                switch (mode) {
                                    case SMARTGEAR_DISCONNECT_MODE /* 612 */:
                                        if (SMARTGEAR_2x2_MODE == this.mSmartGearCurrentMode) {
                                            if (this.mSG2x2Time != 0 && currentTime > this.mSG2x2Time) {
                                                long time3 = currentTime - this.mSG2x2Time;
                                                this.mSG2x2WorkTimeCounter += time3;
                                                logd("SG2x2Work mode: [" + mode + "]for [" + time3 + "] seconds all2x2time:" + this.mSG2x2WorkTimeCounter + "allScreenOn2x2time: " + this.mSG2x2ScreenOnWorkTimeCounter);
                                            }
                                            if (this.mSGScreenOn2x2Time != 0 && currentTime > this.mSGScreenOn2x2Time) {
                                                long time4 = currentTime - this.mSGScreenOn2x2Time;
                                                this.mSG2x2ScreenOnWorkTimeCounter += time4;
                                                logd("SG2x2Work mode: [" + mode + "]for [" + time4 + "] seconds all2x2time:" + this.mSG2x2WorkTimeCounter + "allScreenOn2x2time: " + this.mSG2x2ScreenOnWorkTimeCounter);
                                            }
                                        } else if (SMARTGEAR_1x1_MODE == this.mSmartGearCurrentMode) {
                                            if (this.mSG1x1Time != 0 && currentTime > this.mSG1x1Time) {
                                                long time5 = currentTime - this.mSG1x1Time;
                                                if (time5 < 300) {
                                                    this.mSG1x1Below300SecCount++;
                                                } else if (time5 >= 300 && time5 < 1800) {
                                                    this.mSG1x1Above300To1800SecCount++;
                                                } else if (time5 > 1800) {
                                                    this.mSG1x1above1800SecCount++;
                                                }
                                                this.mSG1x1WorkTimeCounter += time5;
                                                logd("SG1x1Work mode : [" + mode + "]for[" + time5 + "] seconds all1x1time: " + this.mSG1x1WorkTimeCounter + "allscreenon1x1time: " + this.mSG1x1ScreenOnWorkTimeCounter);
                                            }
                                            if (this.mSGScreenOn1x1Time != 0 && currentTime > this.mSGScreenOn1x1Time) {
                                                long time6 = currentTime - this.mSGScreenOn1x1Time;
                                                this.mSG1x1ScreenOnWorkTimeCounter += time6;
                                                logd("SG1x1Work mode : [" + mode + "]for[" + time6 + "] seconds all1x1time: " + this.mSG1x1WorkTimeCounter + "allscreenon1x1time: " + this.mSG1x1ScreenOnWorkTimeCounter);
                                            }
                                        }
                                        this.mSG1x1Time = 0;
                                        this.mSG2x2Time = 0;
                                        this.mSGScreenOn1x1Time = 0;
                                        this.mSGScreenOn2x2Time = 0;
                                        obj = obj2;
                                        break;
                                    case SMARTGEAR_SCREEN_OFF /* 613 */:
                                        if (SMARTGEAR_2x2_MODE == this.mSmartGearCurrentMode) {
                                            if (this.mSGScreenOn2x2Time != 0 && currentTime > this.mSGScreenOn2x2Time) {
                                                long time7 = currentTime - this.mSGScreenOn2x2Time;
                                                this.mSG2x2ScreenOnWorkTimeCounter += time7;
                                                logd("SG2x2Work mode:" + mode + "for [" + time7 + "] seconds allscreen2x2time:" + this.mSG2x2ScreenOnWorkTimeCounter);
                                            }
                                        } else if (SMARTGEAR_1x1_MODE == this.mSmartGearCurrentMode && this.mSGScreenOn1x1Time != 0 && currentTime > this.mSGScreenOn1x1Time) {
                                            long time8 = currentTime - this.mSGScreenOn1x1Time;
                                            this.mSG1x1ScreenOnWorkTimeCounter += time8;
                                            logd("SG1x1Work mode : " + mode + "for[" + time8 + "] seconds allscreen1x1time:" + this.mSG1x1ScreenOnWorkTimeCounter);
                                        }
                                        this.mSGScreenOn1x1Time = 0;
                                        this.mSGScreenOn2x2Time = 0;
                                        obj = obj2;
                                        break;
                                    case SMARTGEAR_SCREEN_ON /* 614 */:
                                        if (SMARTGEAR_2x2_MODE != this.mSmartGearCurrentMode) {
                                            if (SMARTGEAR_1x1_MODE != this.mSmartGearCurrentMode) {
                                                obj = obj2;
                                                break;
                                            } else {
                                                this.mSGScreenOn1x1Time = currentTime;
                                                this.mSGScreenOn2x2Time = 0;
                                                obj = obj2;
                                                break;
                                            }
                                        } else {
                                            this.mSGScreenOn2x2Time = currentTime;
                                            this.mSGScreenOn1x1Time = 0;
                                            obj = obj2;
                                            break;
                                        }
                                    default:
                                        try {
                                            logd("unknow mode :" + mode);
                                            obj = obj2;
                                            break;
                                        } catch (Throwable th) {
                                            th = th;
                                            obj = obj2;
                                            throw th;
                                        }
                                }
                            } else {
                                this.mSG2x2Time = currentTime;
                                this.mSGScreenOn2x2Time = currentTime;
                                if (this.mSG1x1Time != 0) {
                                    try {
                                        if (currentTime > this.mSG1x1Time) {
                                            long time9 = currentTime - this.mSG1x1Time;
                                            if (time9 < 300) {
                                                this.mSG1x1Below300SecCount++;
                                            } else if (time9 >= 300 && time9 < 1800) {
                                                this.mSG1x1Above300To1800SecCount++;
                                            } else if (time9 > 1800) {
                                                this.mSG1x1above1800SecCount++;
                                            }
                                            this.mSG1x1WorkTimeCounter += time9;
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("SG1x1Work for[");
                                            sb.append(time9);
                                            sb.append("] seconds all1x1time:");
                                            obj = obj2;
                                            try {
                                                sb.append(this.mSG1x1WorkTimeCounter);
                                                logd(sb.toString());
                                                if (time9 < this.mSG1x1MinimumDuration) {
                                                    this.mSG1x1MinimumDuration = time9;
                                                }
                                                this.mSG1x1Time = 0;
                                                if (this.mSGScreenOn1x1Time == 0 && currentTime > this.mSGScreenOn1x1Time && this.mScreenOn) {
                                                    long time10 = currentTime - this.mSGScreenOn1x1Time;
                                                    StringBuilder sb2 = new StringBuilder();
                                                    sb2.append("SG1x1Work for[");
                                                    sb2.append(time10);
                                                    sb2.append("] seconds allscreen1x1time:");
                                                    sb2.append(this.mSG1x1ScreenOnWorkTimeCounter);
                                                    logd(sb2.toString());
                                                    this.mSG1x1ScreenOnWorkTimeCounter += time10;
                                                    this.mSGScreenOn1x1Time = 0;
                                                }
                                                if (SMARTGEAR_DISCONNECT_MODE != this.mSmartGearCurrentMode) {
                                                    this.mSG1To2Counter++;
                                                }
                                                logd("mSG1To2Counter = " + this.mSG1To2Counter);
                                            } catch (Throwable th2) {
                                                th = th2;
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        obj = obj2;
                                        throw th;
                                    }
                                }
                                obj = obj2;
                                try {
                                    if (this.mSGScreenOn1x1Time == 0) {
                                    }
                                    if (SMARTGEAR_DISCONNECT_MODE != this.mSmartGearCurrentMode) {
                                    }
                                    logd("mSG1To2Counter = " + this.mSG1To2Counter);
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                            if (!(SMARTGEAR_SCREEN_OFF == mode || SMARTGEAR_SCREEN_ON == mode)) {
                                this.mSmartGearCurrentMode = mode;
                            }
                        } else {
                            obj = obj2;
                        }
                        return true;
                    } catch (Throwable th5) {
                        th = th5;
                        obj = obj2;
                        throw th;
                    }
                }
            }
        }
        return false;
    }

    public void checkSmartGearState(int dsReason) {
        logd("checkSmartGearState dsReason = " + dsReason);
        onSmartGearStateChange(dsReason);
    }

    private static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
    }

    public void checkInternetAccess(final int checkReason, final int checkSequence, final int dsReason, ExtendedWifiInfo mWifiInfo, int mLastNetworkId, String mInterfaceName) {
        if (!onSmartGearStateChange(dsReason)) {
            if (!sWifiRomUpdateHelper.getBooleanValue("NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING", true) && (checkReason == 1 || checkReason == -1)) {
                loge("checking internet access after driver roaming is disabled");
            } else if (sWifiRomUpdateHelper.getBooleanValue("NETWORK_CHECK_INTERNET_FOR_DATA_STALL", true) || checkReason != 2) {
                if (checkReason == 2) {
                    if (isMtkPlatform()) {
                        this.isQcomPlatform = false;
                    } else {
                        this.isQcomPlatform = true;
                    }
                }
                final WifiDataStallInfo dataStallInfo = new WifiDataStallInfo();
                dataStallInfo.hadInetAccess = this.mhadInetAccess;
                dataStallInfo.hadGwAccess = this.mhadGwAccess;
                dataStallInfo.reason = dsReason;
                dataStallInfo.wifiInfo = new WifiInfo(mWifiInfo);
                dataStallInfo.dhcpResults = sClientModeImpl.syncGetDhcpResults();
                dataStallInfo.sInterfaceName = mInterfaceName;
                this.sLastNetworkId = mLastNetworkId;
                final long currentMillis = System.currentTimeMillis();
                long lastReportMillis = SystemProperties.getLong(OPPO_WIFI_LAST_REPORT_TIME, 0);
                int lastReportSeq = SystemProperties.getInt(OPPO_WIFI_LAST_REPORT_SEQ, 0);
                if (lastReportMillis == 0 || currentMillis - lastReportMillis > 86400000) {
                    wifiPeriodicReport(lastReportSeq + 1, currentMillis);
                    dualStaBuriedPiontInfoReport(lastReportSeq + 1, currentMillis);
                    SystemProperties.set(OPPO_WIFI_LAST_REPORT_TIME, currentMillis + DEFAULT_DNS_LOOKUP_URL);
                    SystemProperties.set(OPPO_WIFI_LAST_REPORT_SEQ, (lastReportSeq + 1) + DEFAULT_DNS_LOOKUP_URL);
                }
                final boolean doRecord = false;
                if (checkReason == 2) {
                    if (dataStallInfo.wifiInfo.getRssi() > NUD_FAILURE_WEAK_RSSI) {
                        WifiConfiguration config = sWifiConfigManager.getConfiguredNetwork(dataStallInfo.wifiInfo.getNetworkId());
                        if (config != null) {
                            String key = config.configKey() + " " + dataStallInfo.reason;
                            doRecord = this.mDataStallMap.get(key) == null || currentMillis - this.mDataStallMap.get(key).longValue() > 86400000;
                            if (doRecord) {
                                this.mDataStallMap.put(key, Long.valueOf(currentMillis));
                            }
                            if (doRecord && checkImei()) {
                                ClientModeImpl clientModeImpl = sClientModeImpl;
                                if (!ClientModeImpl.isNotChineseOperator()) {
                                    startCaptureLogs(dataStallInfo.reason, checkSequence, this.isQcomPlatform);
                                }
                            }
                        }
                    }
                }
                Handler handler = this.sAsyncHandler;
                if (handler != null) {
                    handler.post(new Runnable() {
                        /* class com.android.server.wifi.OppoDataStallHelper.AnonymousClass4 */

                        /* JADX WARNING: Code restructure failed: missing block: B:101:0x02bb, code lost:
                            if (r0 == 523) goto L_0x02c1;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:103:0x02bf, code lost:
                            if (r0 != 512) goto L_0x02f2;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:104:0x02c1, code lost:
                            r4 = r18.this$0.getMtkLinkLayerStats();
                            r18.this$0.logd("MTK WiFi L2 log - get after test L2 status ");
                            r3.mNeedLog = r18.this$0.needMtkLogBasedOnL2(r5, r4);
                            r18.this$0.logd("MTK WiFi L2 log - Possible DUT's fault ? " + r3.mNeedLog);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:106:0x02f6, code lost:
                            if (r3.mNeedLog == false) goto L_0x02fd;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:107:0x02f8, code lost:
                            com.android.server.wifi.OppoDataStallHelper.access$2108(r18.this$0);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:109:0x030d, code lost:
                            if (com.android.server.wifi.OppoDataStallHelper.sWifiConfigManager.getConfiguredNetwork(r3.wifiInfo.getNetworkId()) == null) goto L_0x03c5;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:111:0x0319, code lost:
                            if (r3.wifiInfo.getRssi() <= com.android.server.wifi.OppoDataStallHelper.NUD_FAILURE_WEAK_RSSI) goto L_0x03bd;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:113:0x031f, code lost:
                            if (r3.reason == 11) goto L_0x034a;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:115:0x0327, code lost:
                            if (r3.reason == 523) goto L_0x034a;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:117:0x032f, code lost:
                            if (r3.reason == 514) goto L_0x034a;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:119:0x0337, code lost:
                            if (r3.reason == 516) goto L_0x034a;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:121:0x033f, code lost:
                            if (r3.reason != 515) goto L_0x0342;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:122:0x0342, code lost:
                            r18.this$0.saveDataStallInfo(r3);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:124:0x034e, code lost:
                            if (r3.hadInetAccess == false) goto L_0x0369;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:126:0x0354, code lost:
                            if (r3.hasInetAccess != false) goto L_0x0369;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:128:0x035a, code lost:
                            if (r3.hadGwAccess == false) goto L_0x0362;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:130:0x0360, code lost:
                            if (r3.hasGwAccess != false) goto L_0x0369;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:131:0x0362, code lost:
                            r18.this$0.saveDataStallInfo(r3);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:133:0x036b, code lost:
                            if (r0 == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:135:0x0373, code lost:
                            if (r18.this$0.checkImei() == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:137:0x0379, code lost:
                            if (r3.hadInetAccess == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:139:0x037f, code lost:
                            if (r3.hadGwAccess == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:141:0x0385, code lost:
                            if (r3.hasInetAccess != false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:143:0x038b, code lost:
                            if (r3.hasGwAccess != false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:145:0x0391, code lost:
                            if (r3.mNeedLog == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:146:0x0393, code lost:
                            r18.this$0.logd("triggerLogUpload dataStallInfo" + r3);
                            r18.this$0.triggerLogUpload(r3.reason, r26, r18.this$0.isQcomPlatform);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:147:0x03bd, code lost:
                            r18.this$0.loge("checkInternetAccess ignore data stall in weak RSSI");
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:148:0x03c5, code lost:
                            r18.this$0.loge("checkInternetAccess invalid config");
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:152:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:153:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:154:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:155:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:156:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:157:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:158:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:159:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:160:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:161:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:162:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:163:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:164:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:165:?, code lost:
                            return;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:61:0x0187, code lost:
                            if (r7 != null) goto L_0x0189;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0189, code lost:
                            r7.disconnect();
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0228, code lost:
                            if (0 == 0) goto L_0x022c;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:79:0x022c, code lost:
                            r3.hasInetAccess = r18.this$0.mhasInetAccess;
                            r0 = r18.this$0;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:80:0x023c, code lost:
                            if (r0.mhasInetAccess == false) goto L_0x023f;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:81:0x023f, code lost:
                            r6 = r18.this$0.mhadInetAccess;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:82:0x0245, code lost:
                            r0.mhadInetAccess = r6;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:83:0x024a, code lost:
                            if (r25 != 2) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0254, code lost:
                            if (com.android.server.wifi.OppoDataStallHelper.sClientModeImpl.isConnected() == false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0264, code lost:
                            if (com.android.server.wifi.OppoDataStallHelper.sClientModeImpl.getNetworkInfo().getDetailedState() != android.net.NetworkInfo.DetailedState.CONNECTED) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:88:0x0266, code lost:
                            r0 = com.android.server.wifi.OppoDataStallHelper.sClientModeImpl;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:89:0x026d, code lost:
                            if (com.android.server.wifi.ClientModeImpl.isNotChineseOperator() != false) goto L_?;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:91:0x0275, code lost:
                            if (r18.this$0.isQcomPlatform == false) goto L_0x02b3;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:92:0x0277, code lost:
                            r0 = r27;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:93:0x0279, code lost:
                            if (r0 == 258) goto L_0x0281;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:94:0x027b, code lost:
                            if (r0 == 11) goto L_0x0281;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:96:0x027f, code lost:
                            if (r0 != 8) goto L_0x02f2;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:97:0x0281, code lost:
                            r2 = r18.this$0.getQcomLinkLayerStats();
                            r18.this$0.logd("Qcom WiFi L2 log - get after test L2 status ");
                            r3.mNeedLog = r18.this$0.needQcomLogBasedOnL2(r3, r2);
                            r18.this$0.logd("Qcom WiFi L2 log - Possible DUT's fault ? " + r3.mNeedLog);
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:98:0x02b3, code lost:
                            r0 = r27;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:99:0x02b7, code lost:
                            if (r0 == 513) goto L_0x02c1;
                         */
                        /* JADX WARNING: Removed duplicated region for block: B:27:0x008a  */
                        /* JADX WARNING: Removed duplicated region for block: B:28:0x008c  */
                        /* JADX WARNING: Removed duplicated region for block: B:43:0x00c4  */
                        /* JADX WARNING: Removed duplicated region for block: B:46:0x00d5  */
                        /* JADX WARNING: Removed duplicated region for block: B:47:0x00db  */
                        /* JADX WARNING: Removed duplicated region for block: B:55:0x0110 A[Catch:{ Exception -> 0x0191, all -> 0x018e }] */
                        /* JADX WARNING: Removed duplicated region for block: B:56:0x0118 A[Catch:{ Exception -> 0x0191, all -> 0x018e }] */
                        /* JADX WARNING: Removed duplicated region for block: B:58:0x0126 A[Catch:{ Exception -> 0x0191, all -> 0x018e }] */
                        public void run() {
                            MtkWifiLinkLayerStats sOldL2Stats;
                            WifiLinkLayerStats mOldL2Stats;
                            HttpURLConnection urlConnection;
                            URL url;
                            boolean z = true;
                            dataStallInfo.mNeedLog = true;
                            if (checkReason == 2) {
                                if (OppoDataStallHelper.this.isQcomPlatform) {
                                    int i = dsReason;
                                    if (i == 258 || i == 11 || i == 8) {
                                        WifiLinkLayerStats mOldL2Stats2 = OppoDataStallHelper.this.getQcomLinkLayerStats();
                                        OppoDataStallHelper.this.logd("Qcom WiFi L2 log - get before check internet L2 status ");
                                        sOldL2Stats = null;
                                        mOldL2Stats = mOldL2Stats2;
                                        if (!(dataStallInfo.dhcpResults == null || dataStallInfo.dhcpResults.gateway == null)) {
                                            dataStallInfo.hasGwAccess = dataStallInfo.dhcpResults.gateway.isReachable(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                                        }
                                        OppoDataStallHelper.this.mhadGwAccess = dataStallInfo.hasGwAccess ? true : OppoDataStallHelper.this.mhadGwAccess;
                                        if (dataStallInfo.hadInetAccess && dataStallInfo.hadGwAccess && !dataStallInfo.hasGwAccess && checkReason == 2 && (dataStallInfo.reason == 258 || dataStallInfo.reason == 513)) {
                                            OppoDataStallHelper.access$2208(OppoDataStallHelper.this);
                                            if (OppoDataStallHelper.this.mNudFailureCount == 1) {
                                                OppoDataStallHelper.this.mFirstNudFailureTime = currentMillis;
                                            }
                                            if (OppoDataStallHelper.this.mNudFailureCount <= 20) {
                                                OppoDataStallHelper.this.doNudFailureRecover();
                                            } else if (currentMillis - OppoDataStallHelper.this.mFirstNudFailureTime > 3600000) {
                                                OppoDataStallHelper.this.mFirstNudFailureTime = currentMillis;
                                                OppoDataStallHelper.this.mNudFailureCount = 1;
                                                OppoDataStallHelper.this.doNudFailureRecover();
                                            } else {
                                                OppoDataStallHelper.this.logd("NUD_FAILURE, too frequent...");
                                            }
                                        }
                                        urlConnection = null;
                                        ClientModeImpl unused = OppoDataStallHelper.sClientModeImpl;
                                        if (ClientModeImpl.isNotChineseOperator()) {
                                            url = OppoDataStallHelper.this.getExpCheckInternetHttpUrl();
                                        } else {
                                            url = new URL("http", "conn1.oppomobile.com", "/generate_204");
                                        }
                                        if (url != null) {
                                            OppoDataStallHelper.this.logd("checkInternetAccess Checking " + url.toString() + " seq=" + checkSequence);
                                            urlConnection = (HttpURLConnection) url.openConnection();
                                            urlConnection.setInstanceFollowRedirects(false);
                                            urlConnection.setConnectTimeout(10000);
                                            urlConnection.setReadTimeout(10000);
                                            urlConnection.setUseCaches(false);
                                            urlConnection.getInputStream();
                                            if (urlConnection.getResponseCode() == 204) {
                                                OppoDataStallHelper.this.logd("checkInternetAccess has internet access, seq=" + checkSequence);
                                                OppoDataStallHelper.this.mhasInetAccess = true;
                                            }
                                        }
                                    } else {
                                        OppoDataStallHelper.access$2108(OppoDataStallHelper.this);
                                    }
                                } else {
                                    int i2 = dsReason;
                                    if (i2 == 513 || i2 == 523 || i2 == 512) {
                                        MtkWifiLinkLayerStats sOldL2Stats2 = OppoDataStallHelper.this.getMtkLinkLayerStats();
                                        OppoDataStallHelper.this.logd("MTK WiFi L2 log - get before check internet L2 status ");
                                        sOldL2Stats = sOldL2Stats2;
                                        mOldL2Stats = null;
                                        dataStallInfo.hasGwAccess = dataStallInfo.dhcpResults.gateway.isReachable(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                                        OppoDataStallHelper.this.mhadGwAccess = dataStallInfo.hasGwAccess ? true : OppoDataStallHelper.this.mhadGwAccess;
                                        OppoDataStallHelper.access$2208(OppoDataStallHelper.this);
                                        if (OppoDataStallHelper.this.mNudFailureCount == 1) {
                                        }
                                        if (OppoDataStallHelper.this.mNudFailureCount <= 20) {
                                        }
                                        urlConnection = null;
                                        ClientModeImpl unused2 = OppoDataStallHelper.sClientModeImpl;
                                        if (ClientModeImpl.isNotChineseOperator()) {
                                        }
                                        if (url != null) {
                                        }
                                    }
                                }
                            }
                            sOldL2Stats = null;
                            mOldL2Stats = null;
                            try {
                                dataStallInfo.hasGwAccess = dataStallInfo.dhcpResults.gateway.isReachable(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (IOException e) {
                                dataStallInfo.hasGwAccess = false;
                            }
                            OppoDataStallHelper.this.mhadGwAccess = dataStallInfo.hasGwAccess ? true : OppoDataStallHelper.this.mhadGwAccess;
                            OppoDataStallHelper.access$2208(OppoDataStallHelper.this);
                            if (OppoDataStallHelper.this.mNudFailureCount == 1) {
                            }
                            if (OppoDataStallHelper.this.mNudFailureCount <= 20) {
                            }
                            urlConnection = null;
                            try {
                                ClientModeImpl unused22 = OppoDataStallHelper.sClientModeImpl;
                                if (ClientModeImpl.isNotChineseOperator()) {
                                }
                                if (url != null) {
                                }
                            } catch (Exception e2) {
                                OppoDataStallHelper.this.loge("checkInternetAccess no internet access: exception " + e2 + " checkdatastallReason=" + checkReason + " checkSequence=" + checkSequence + " current seq=" + OppoDataStallHelper.sClientModeImpl.getCurrentCheckInetAccessSeq());
                                if (OppoDataStallHelper.this.mhasInetAccess && checkReason == 1 && OppoDataStallHelper.sClientModeImpl.isConnected() && OppoDataStallHelper.sClientModeImpl.getNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED && checkSequence == OppoDataStallHelper.sClientModeImpl.getCurrentCheckInetAccessSeq()) {
                                    OppoDataStallHelper.this.loge("checkInternetAccess driver roamed and no internet access: exception " + e2);
                                    OppoDataStallHelper.sClientModeImpl.startRoamToNetwork(OppoDataStallHelper.this.sLastNetworkId, null);
                                }
                                OppoDataStallHelper.this.mhasInetAccess = false;
                            } catch (Throwable th) {
                                if (0 != 0) {
                                    urlConnection.disconnect();
                                }
                                throw th;
                            }
                        }
                    });
                }
            } else {
                loge("checking internet access for data stall is disabled");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiLinkLayerStats getQcomLinkLayerStats() {
        return sWifiNative.getWifiLinkLayerStats(OppoWifiAssistantUtils.IFACE_NAME_WLAN0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MtkWifiLinkLayerStats getMtkLinkLayerStats() {
        int tlv_int_value = 0;
        long tlv_long_value = 0;
        MtkWifiLinkLayerStats stats = new MtkWifiLinkLayerStats();
        byte[] nativeStats = sWifiNative.getDriverStateDump();
        if (nativeStats != null) {
            logd("length: " + nativeStats.length);
        }
        if (nativeStats != null && MTK_WIFI_NATIVE_LINK_QUALITY_LENGTH <= nativeStats.length) {
            int tlv_cur_index = 0;
            while (tlv_cur_index < nativeStats.length) {
                int tlv_tag = byteArrayToShort(nativeStats, tlv_cur_index, 2);
                int tlv_cur_index2 = tlv_cur_index + 2;
                int tlv_length = byteArrayToShort(nativeStats, tlv_cur_index2, 2);
                int tlv_cur_index3 = tlv_cur_index2 + 2;
                if (tlv_length == 4) {
                    tlv_int_value = byteArrayToInt(nativeStats, tlv_cur_index3, tlv_length);
                    logd("TLV: TAG[" + tlv_tag + "] LENGTH [" + tlv_length + "] VALUE [" + tlv_int_value + "]~!");
                } else {
                    tlv_long_value = byteArrayToLong(nativeStats, tlv_cur_index3, tlv_length);
                    logd("TLV: TAG[" + tlv_tag + "] LENGTH [" + tlv_length + "] VALUE [" + tlv_long_value + "]~!");
                }
                tlv_cur_index = tlv_cur_index3 + tlv_length;
                switch (tlv_tag) {
                    case 1:
                        stats.curTxRate = tlv_int_value;
                        break;
                    case 2:
                        stats.txTotalCount = tlv_long_value;
                        break;
                    case 3:
                        stats.txRetryCount = tlv_long_value;
                        break;
                    case 4:
                        stats.txFailCount = tlv_long_value;
                        break;
                    case 5:
                        stats.txRtsFailCount = tlv_long_value;
                        break;
                    case 6:
                        stats.txAckFailCount = tlv_long_value;
                        break;
                    case 7:
                        stats.curRxRate = tlv_int_value;
                        break;
                    case 8:
                        stats.rxTotalCount = tlv_long_value;
                        break;
                    case 9:
                        stats.rxDupCount = tlv_int_value;
                        break;
                    case 10:
                        stats.rxErrCount = tlv_long_value;
                        break;
                    case 11:
                        stats.idleSlotCount = tlv_long_value;
                        break;
                }
            }
            stats.curTime = System.currentTimeMillis();
        }
        logd(stats.toString());
        return stats;
    }

    private int byteArrayToShort(byte[] valueBuf, int offset, int length) {
        boolean outOfBound = offset + length > valueBuf.length;
        if (length < 2 || outOfBound) {
            return 0;
        }
        ByteBuffer converter = ByteBuffer.wrap(valueBuf, offset, length);
        converter.order(ByteOrder.nativeOrder());
        return converter.getShort();
    }

    private int byteArrayToInt(byte[] valueBuf, int offset, int length) {
        boolean outOfBound = offset + length > valueBuf.length;
        if (length < 4 || outOfBound) {
            return 0;
        }
        ByteBuffer converter = ByteBuffer.wrap(valueBuf, offset, length);
        converter.order(ByteOrder.nativeOrder());
        return converter.getInt();
    }

    public static long byteArrayToLong(byte[] valueBuf, int offset, int length) {
        boolean outOfBound = offset + length > valueBuf.length;
        if (length < 8 || outOfBound) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.wrap(valueBuf, offset, length);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getLong();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needQcomLogBasedOnL2(WifiLinkLayerStats old, WifiLinkLayerStats current) {
        if (old == null || current == null) {
            logd("WiFi L2 log - Collect L2 status error.");
            return true;
        }
        logd("WiFi L2 log - beacon rx  :" + (current.beacon_rx - old.beacon_rx));
        logd("WiFi L2 log - mgmt rssi  :" + current.rssi_mgmt);
        logd("WiFi L2 log - tx time    :" + (current.tx_time - old.tx_time));
        logd("WiFi L2 log - rx time    :" + (current.rx_time - old.rx_time));
        logd("WiFi L2 log - TX :");
        logd("WiFi L2 log - BE txmpdu  :" + (current.txmpdu_be - old.txmpdu_be));
        logd("WiFi L2 log - BK txmpdu  :" + (current.txmpdu_bk - old.txmpdu_bk));
        logd("WiFi L2 log - VO txmpdu  :" + (current.txmpdu_vo - old.txmpdu_vo));
        logd("WiFi L2 log - VI txmpdu  :" + (current.txmpdu_vi - old.txmpdu_vi));
        logd("WiFi L2 log - TX lost:");
        logd("WiFi L2 log - BE tx lost :" + (current.rxmpdu_be - old.rxmpdu_be));
        logd("WiFi L2 log - BK tx lost :" + (current.rxmpdu_bk - old.rxmpdu_bk));
        logd("WiFi L2 log - VO tx lost :" + (current.rxmpdu_vo - old.rxmpdu_vo));
        logd("WiFi L2 log - VI tx lost :" + (current.rxmpdu_vi - old.rxmpdu_vi));
        logd("WiFi L2 log - RX :");
        logd("WiFi L2 log - BE rxmpdu  :" + (current.rxmpdu_be - old.rxmpdu_be));
        logd("WiFi L2 log - BK rxmpdu  :" + (current.rxmpdu_bk - old.rxmpdu_bk));
        logd("WiFi L2 log - VO rxmpdu  :" + (current.rxmpdu_vo - old.rxmpdu_vo));
        logd("WiFi L2 log - VI rxmpdu  :" + (current.rxmpdu_vi - old.rxmpdu_vi));
        int totalRxedBeacon = current.beacon_rx - old.beacon_rx;
        int totalTxTime = current.tx_time - old.tx_time;
        int totalRxTime = current.rx_time - old.rx_time;
        long totalAcTxCnt = (current.txmpdu_be - old.txmpdu_be) + (current.txmpdu_bk - old.txmpdu_bk) + (current.txmpdu_vo - old.txmpdu_vo) + (current.txmpdu_vi - old.txmpdu_vi);
        long totalAcRxCnt = (current.rxmpdu_be - old.rxmpdu_be) + (current.rxmpdu_bk - old.rxmpdu_bk) + (current.rxmpdu_vo - old.rxmpdu_vo) + (current.rxmpdu_vi - old.rxmpdu_vi);
        long totalAcTxFailedCnt = (current.lostmpdu_be - old.lostmpdu_be) + (current.lostmpdu_bk - old.lostmpdu_bk) + (current.lostmpdu_vo - old.lostmpdu_vo) + (current.lostmpdu_vi - old.lostmpdu_vi);
        long totalAcTxRetriedCnt = (current.retries_be - old.retries_be) + (current.retries_bk - old.retries_bk) + (current.retries_vo - old.retries_vo) + (current.retries_vi - old.retries_vi);
        if (current.rssi_mgmt < NUD_FAILURE_WEAK_RSSI) {
            logd("WiFi L2 log - RSSI is too weak : " + current.rssi_mgmt);
            this.mEnvFaultCnt = this.mEnvFaultCnt + 1;
            return false;
        } else if (totalAcTxCnt == 0 || totalAcRxCnt != 0 || totalRxedBeacon == 0) {
            int rusTxRetryRatio = getRomUpdateIntegerValue("DATA_STALL_TX_RETRY_RATIO_THRESOLD", Integer.valueOf((int) TX_RETRY_RATIO_THRESOLD)).intValue();
            if (totalAcTxCnt + totalAcTxRetriedCnt + totalAcTxFailedCnt != 0) {
                float retryTxRatio = (float) (((totalAcTxRetriedCnt + totalAcTxFailedCnt) * 100) / ((totalAcTxCnt + totalAcTxRetriedCnt) + totalAcTxFailedCnt));
                if (retryTxRatio >= ((float) rusTxRetryRatio)) {
                    logd("WiFi L2 log - Air was too busy. Retry ratio = " + retryTxRatio);
                    this.mEnvFaultCnt = this.mEnvFaultCnt + 1;
                    return false;
                }
            }
            int rusTxDvideRx = getRomUpdateIntegerValue("DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD", 10).intValue();
            if (totalAcTxCnt != 0 && totalAcRxCnt != 0 && totalRxedBeacon != 0 && totalAcTxCnt / totalAcRxCnt > ((long) rusTxDvideRx)) {
                logd("WiFi L2 log - Possible AP's L3 issue. DUT TXed packets outnumbered RXed packets");
                this.mApFaultCnt++;
                return false;
            } else if ((totalAcTxCnt == 0 && totalAcTxFailedCnt != 0) || totalTxTime == 0) {
                logd("WiFi L2 log - DUT didn't tx suucess at all. Could be a problem");
                return true;
            } else if (totalRxTime == 0) {
                logd("WiFi L2 log - DUT didn't rx at all. Could be a problem");
                return true;
            } else {
                logd("WiFi L2 log - None of above");
                return true;
            }
        } else {
            logd("WiFi L2 log - Possible AP's L3 issue");
            this.mApFaultCnt++;
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needMtkLogBasedOnL2(MtkWifiLinkLayerStats old, MtkWifiLinkLayerStats current) {
        int perSecondRxErr;
        if (old == null || current == null) {
            logd("WiFi L2 log - Collect L2 status error.");
            return true;
        }
        logd("WiFi L2 log - TX :");
        logd("WiFi L2 log - curTxRate           :" + current.curTxRate);
        logd("WiFi L2 log - txTotalCount    diff:" + (current.txTotalCount - old.txTotalCount));
        logd("WiFi L2 log - txRetryCount    diff:" + (current.txRetryCount - old.txRetryCount));
        logd("WiFi L2 log - txFailCount     diff:" + (current.txFailCount - old.txFailCount));
        logd("WiFi L2 log - txRtsFailCount  diff:" + (current.txRtsFailCount - old.txRtsFailCount));
        logd("WiFi L2 log - txAckFailCount  diff:" + (current.txAckFailCount - old.txAckFailCount));
        logd("WiFi L2 log - RX :");
        logd("WiFi L2 log - curRxRate           :" + current.curRxRate);
        logd("WiFi L2 log - rxTotalCount    diff:" + (current.rxTotalCount - old.rxTotalCount));
        logd("WiFi L2 log - rxDupCount      diff:" + (current.rxDupCount - old.rxDupCount));
        logd("WiFi L2 log - rxErrCount      diff:" + (current.rxErrCount - old.rxErrCount));
        logd("WiFi L2 log - idleSlotCount   diff:" + (current.idleSlotCount - old.idleSlotCount));
        logd("WiFi L2 log - difftime        diff:" + (current.curTime - old.curTime));
        long txTotalDataAckCnt = ((current.txTotalCount - old.txTotalCount) - (current.txRetryCount - old.txRetryCount)) - (current.txFailCount - old.txFailCount);
        long rxTotalDataCnt = current.rxTotalCount - old.rxTotalCount;
        long rxTotalErrCnt = current.rxErrCount - old.rxErrCount;
        long idleSlotCnt = current.idleSlotCount - old.idleSlotCount;
        long diffTime = current.curTime - old.curTime;
        if (txTotalDataAckCnt == 0 || rxTotalDataCnt != 0) {
            int rusRxErr = getRomUpdateIntegerValue("DATA_STALL_RX_PER_SECOND_ERR_CNT_THRESOLD", 5000).intValue();
            if (rxTotalDataCnt == 0 || rxTotalErrCnt == 0 || diffTime <= 0 || (perSecondRxErr = (int) ((rxTotalErrCnt * 1000) / diffTime)) < rusRxErr) {
                int rusIdlePercent = getRomUpdateIntegerValue("DATA_STALL_IDLE_SLOT_PERCENT_THRESOLD", 3).intValue();
                if (idleSlotCnt > 0 && diffTime > 0) {
                    int idlepercent = (int) (((9 * idleSlotCnt) * 100) / (1000 * diffTime));
                    if (idlepercent < rusIdlePercent) {
                        logd("WiFi L2 log - Air was too busy. diff idlepercent = " + idlepercent);
                        this.mEnvFaultCnt = this.mEnvFaultCnt + 1;
                        return false;
                    }
                }
                int rusTxDvideRx = getRomUpdateIntegerValue("DATA_STALL_TX_DIVIDE_RX_MULTIPLE_THRESOLD", 10).intValue();
                if (txTotalDataAckCnt != 0 && rxTotalDataCnt != 0) {
                    if (txTotalDataAckCnt / rxTotalDataCnt > ((long) rusTxDvideRx)) {
                        logd("WiFi L2 log - Possible AP's L3 issue. DUT TXed packets outnumbered RXed packets");
                        this.mApFaultCnt++;
                        return false;
                    }
                }
                logd("WiFi L2 log - None of above");
                return true;
            }
            logd("WiFi L2 log - Air was too busy. RX Err threshold = " + perSecondRxErr);
            this.mEnvFaultCnt = this.mEnvFaultCnt + 1;
            return false;
        }
        logd("WiFi L2 log - Possible AP's L3 issue");
        this.mApFaultCnt++;
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private URL getExpCheckInternetHttpUrl() {
        String url = getRomUpdateValue("NETWORK_EXP_CAPTIVE_SERVER_HTTP_URL", DEFAULT_HTTP_URL);
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            loge("Bad URL: " + url);
            return null;
        }
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, "debug:" + str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String str) {
        Log.d(TAG, "error:" + str);
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "enableVerboseLogging verbose = " + verbose);
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    public void clearDataStallCounter() {
        this.mDutFaultCnt = 0;
        this.mEnvFaultCnt = 0;
        this.mApFaultCnt = 0;
        this.mAssistantDnsFaultCnt = 0;
        this.mAssistantDutFaultCnt = 0;
        this.mAssistantApFaultCnt = 0;
        this.mAssistantOtherFaultCnt = 0;
    }

    public int getDataStallDutFaultCount() {
        return this.mDutFaultCnt;
    }

    public int getDataStallEnvFaultCount() {
        return this.mEnvFaultCnt;
    }

    public int getDataStallApFaultCount() {
        return this.mApFaultCnt;
    }

    public int getDataStallAssistantDutFaultCount() {
        return this.mAssistantDutFaultCnt;
    }

    public int getDataStallAssistantApFaultCount() {
        return this.mAssistantApFaultCnt;
    }

    public int getDataStallAssistantDnsFaultCount() {
        return this.mAssistantDnsFaultCnt;
    }

    public int getDataStallAssistantOtherFaultCount() {
        return this.mAssistantOtherFaultCnt;
    }
}
