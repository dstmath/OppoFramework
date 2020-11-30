package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.wifi.OppoSlaManager;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy;
import com.color.dialog.ColorListDialog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import oppo.util.OppoStatistics;

/* access modifiers changed from: package-private */
public class OppoWifiAssistantStateTraker extends StateMachine {
    private static final String ACTION_DETECT_INTERNET = "adnroid.net.wifi.DETECT_INTER";
    private static final String ACTION_NOTIFY_GATEWAY_MAC = "com.oppo.wifi.NOTIFY_GATEWAY_MAC";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_INTERNET_INVAILD = "android.net.wifi.OPPO_WIFI_NET_INTERNET_INVAILD";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final String ANT_STR = "ant=";
    private static final boolean ASSISTANT_FOUR_VERSION_ENABLE = true;
    private static final int AUTO_CONN_ERR_THRESHOLD = 180000;
    private static final int AUTO_SWITCH_DATA_COUNT = 5;
    private static final long AUTO_SWITCH_DATA_DISBALE_TIME = 10800000;
    private static final int AUTO_SWITCH_DATA_THRESHOLD_COUNT = 5;
    private static final long AUTO_SWITCH_DATA_THRESHOLD_TIME = 1800000;
    private static final int AVAILABLE_STAT_COUNT = 3;
    public static final int BASE_WIFI_NETWORK_TRAKER = 200704;
    private static final String BRAOD_WIFI_INFO = "com.oppo.BROAD_WIFI_INFO";
    private static final String BRS_STR = "brs=";
    private static final String BSSID_STR = "bssid=";
    private static final String CAP_STR = "cap=";
    private static final String CE_STR = "ce=";
    private static final String CFC_STR = "cfc=";
    public static final int CMD_INTERNET_MONITOR = 200710;
    public static final int CMD_RSSI_FETCH = 200708;
    private static final int CMD_SEND_LOW_QUALITY = 200725;
    public static final int CMD_TCP_MONITOR = 200711;
    public static final int CMD_TRAFFIC_MONITOR = 200709;
    private static final String CONKEY_STR = "conkey=";
    private static final String CSC_STR = "csc=";
    public static final int DATA_INVALID_SCORE = 10;
    private static final int DATA_NETWORK_VALID = 50;
    private static final String DATA_SCORE_CHANGE = "android.net.wifi.OPPO_DATA_NET_CHANGE";
    public static final int DATA_VALID_SCORE = 50;
    private static final String DEFAULT_CONTROL_APP_LIST = "com.stkj.android.wifishare,cn.andouya,com.dewmobile.kuaiya,com.lenovo.anyshare,com.coloros.backuprestore,com.tencent.mobileqq";
    private static final String DEFAULT_HTTPS_URL = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_NETWORK_CHANGED = "android.net.wifi.DEFAULT_NETWORK_CHANGED";
    private static final int DEFAULT_SCORE = 79;
    private static final String DEFAULT_SPECIAL_URL = "360.cn";
    private static final int DETAIL_CAPTIVE = 1;
    private static final int DETAIL_CONNECTED = 2;
    private static final int DETAIL_DISCONNECTED = 0;
    private static final int DETAIL_IDLE = -1;
    private static final int DIFF_RSSI_THRESHOLD = 5;
    private static final int DISABLE_INTERFACE = -1;
    private static final int EVENT_CAPTIVE_PORTAL = 200723;
    private static final int EVENT_CONNECT_NETWORK = 200715;
    private static final int EVENT_DETECT_ALTERNATIVE = 200721;
    private static final int EVENT_DETECT_RSSI = 200717;
    private static final int EVENT_DETECT_SCAN_RESULT = 200718;
    private static final int EVENT_DISMISS_ALERT = 200720;
    private static final int EVENT_INTERNET_CHANGE = 200722;
    public static final int EVENT_NETWORK_MONITOR_CHANGE = 200707;
    private static final int EVENT_NETWORK_NOT_AVAILABLE = 200716;
    public static final int EVENT_NETWORK_STATE_CHANGE = 200705;
    private static final int EVENT_SCAN_TIMEOUT = 200724;
    private static final int EVENT_SCREEN_OFF = 200713;
    private static final int EVENT_SCREEN_ON = 200712;
    private static final int EVENT_SHOW_ALERT = 200719;
    private static final int EVENT_UPDATE_NETWORK_STATE = 200714;
    public static final int EVENT_WIFI_DISABLED = 200706;
    private static final double EXP_COEFFICIENT_MONITOR = 0.5d;
    private static final String EXTRA_DATA_CORE = "data_score";
    private static final String EXTRA_DEFAULT_NET_CELL = "default_net_cell";
    private static final String EXTRA_ENALE_DATA = "enableData";
    private static final String EXTRA_IFACE_NAME = "iface_name";
    private static final String EXTRA_NETWORK_STATE = "netState";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_VERIFY_CONFIG = "verify_config";
    private static final String EXTRA_WIFI_INVALID = "wifi_network_invalid";
    private static final String EXTRA_WIFI_LINK = "linkProperties";
    private static final String EXTRA_WIFI_MANUAL = "manualConnect";
    private static final String EXTRA_WIFI_NETINFO = "wifi_netinfo";
    private static final String EXTRA_WIFI_NETWORK = "network";
    private static final String EXTRA_WIFI_SSID = "ssid";
    private static final String EXTRA_WIFI_TO_DATA = "wifi_to_data";
    private static final String EXTRA_WIFI_VALID = "wifi_valid";
    private static final String GOOD_LOSS_RSSI = "good_loss_rssi";
    private static final String HF_STR = "hf=";
    private static final String HLS_STR = "hls=";
    private static final int HTTP_AVAIL = 4;
    private static final int HTTP_CAPTIVE_CODE_END = 399;
    private static final int HTTP_CAPTIVE_CODE_MID = 300;
    private static final int HTTP_CAPTIVE_CODE_START = 200;
    private static final int HTTP_NORMAL_CODE = 204;
    private static final int HTTP_UNAVAIL = 8;
    private static final int INTERNET_INTERVAL_DELTA = 60000;
    private static final int INTERNET_INTERVAL_SCREENOFF = 300000;
    private static final int INTERNET_INTERVAL_SCREENON = 300000;
    private static final int INTERNET_POLL = 5000;
    private static final int INTERNET_STANDOFF_TIME = 10000;
    private static final int INTERNET_TO_DATA_INTERVAL = 120000;
    private static final int INTER_DETECT_REQUEST_CODE = 0;
    private static final String INT_STR = "int=";
    private static final int INVALID_INFO = -127;
    private static final int ITEM_NO_REMIND = 0;
    private static final int ITEM_REMIND_EVERYTIME = 1;
    private static final String KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT = "keep_celluar_network_wifi_assistant";
    private static final String KEY_CURRENT_WLAN = "key_current_wlan";
    private static final String KEY_NETWORK_MONITOR_AVAILABLE = "oppo.comm.network.monitor.available";
    private static final String KEY_NETWORK_MONITOR_PORTAL = "oppo.comm.network.monitor.portal";
    private static final String KEY_NETWORK_MONITOR_SSID = "oppo.comm.network.monitor.ssid";
    private static final String KEY_SELECT_WLAN = "key_select_wlan";
    private static final String LOW_LOSS_RSSI = "low_loss_rssi";
    private static final String LST_STR = "lst=";
    public static final long MILLIS_OF_A_DAY = 86400000;
    private static final String MS_STR = "ms=";
    private static final int NETQUALITY_HISTORY_COUNTS = 3;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final int NET_DETECT_TIMEOUT = 40000;
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
    private static final int NOTINTER_LOW_TRAFFIC_TRIGGER_COUNT = 10;
    private static final String NOT_REMIND_WIFI_ASSISTANT = "not_remind_wifi_assistant";
    private static final String NQL_STR = "nql=";
    private static final String NV_STR = "nv=";
    private static final int PER_ROAM_THRESHOLD = -83;
    private static final int POOR_STAT_COUNT = 5;
    private static final int PORTAL_STAT_COUNT = 20;
    private static final int ROAM_RSSI_DEALTA = 5;
    private static final int SCORE_NETWORK_VALID = 20;
    private static final String SCORE_STR = "sc=";
    private static final String SECURITY_EAP = "WPA_EAP";
    private static final String SECURITY_NONE = "NONE";
    private static final String SECURITY_PSK = "WPA_PSK";
    private static final String SECURITY_WAPI_CERT = "WAPI_CERT";
    private static final String SECURITY_WAPI_PSK = "WAPI_PSK";
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_WPA2_PSK = "WPA2_PSK";
    public static final String SLA_CANCEL_COUNT = "SLA_CANCEL_COUNT";
    public static final String SLA_DIALOG_COUNT = "SLA_DIALOG_COUNT";
    public static final int SLA_DIALOG_COUNT_MAX = 3;
    public static final String SLA_LAST_DIALOG_TIMESTAMP = "SLA_LAST_DIALOG_TIMESTAMP";
    private static final String SLA_STATUS = "sla_status";
    private static final int SLA_WLAN_POOR_RSSI = -80;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final long STAIC_SCAN_RESULT_AGE = 15000;
    private static final int STAITIC_MANUAL_DIALOG_TIME_THRESHOLD = 30000;
    private static final double STATIC_BAD_LINK_LOSS_THRESHOLD = 0.7d;
    private static final int STATIC_BAD_LINK_SAMPL_INTERVAL = 3000;
    private static final int STATIC_BAD_LINK_SCORE_THRESHOLD = 35;
    private static final int STATIC_BAD_RSSI_24 = -83;
    private static final int STATIC_BAD_RSSI_5 = -80;
    private static final int STATIC_BAD_RSSI_SCORE_THRESHOLD = 30;
    private static final int STATIC_BAD_TCP_SCORE_THRESHOLD = 10;
    private static final double STATIC_DIFF_CONNRATE_THRESHOLD = 0.2d;
    private static final int STATIC_DIFF_SCORE_THRESHOLD = 10;
    private static final int STATIC_DIFF_TCP_NOT_CALCULATE = 5;
    private static final int STATIC_GOOD_LINK_COUNT_THRESHOLD = 3;
    private static final double STATIC_GOOD_LINK_LOSS_THRESHOLD = 0.25d;
    private static final int STATIC_GOOD_LINK_SAMPL_INTERVAL = 5000;
    private static final int STATIC_GOOD_LINK_SCORE_THRESHOLD = 20;
    private static final int STATIC_GOOD_RSSI_24 = -65;
    private static final int STATIC_GOOD_RSSI_5 = -65;
    private static final int STATIC_GOOD_RSSI_SCORE_THRESHOLD = 5;
    private static final int STATIC_GOOD_TCP_SCORE_THRESHOLD = 0;
    private static final int STATIC_HISTORY_RECORD_TRIGGER_THRESHOLD = 10;
    private static final int STATIC_HISTORY_RECORD_VALID_THRESHOLD = 10;
    private static final double STATIC_LOW_LINK_LOSS_THRESHOLD = 0.5d;
    private static final int STATIC_LOW_LINK_SCORE_THRESHOLD = 30;
    private static final int STATIC_LOW_RSSI_24 = -75;
    private static final int STATIC_LOW_RSSI_5 = -72;
    private static final int STATIC_LOW_RSSI_SCORE_THRESHOLD = 20;
    private static final int STATIC_LOW_TCP_SCORE_THRESHOLD = 5;
    private static final int STATIC_NETINVALID_COUNT = 1;
    private static final int STATIC_NO_TRIFFIC_SCORE_THRESHOLD = 5;
    private static final int STATIC_POOR_RSSI_TO_WLAN_24 = -75;
    private static final int STATIC_POOR_RSSI_TO_WLAN_5 = -72;
    private static final int STATIC_ROAM_DETECT_TIMES = 6;
    private static final int STATIC_RSSI_TO_WLAN_THRESHOLD = -73;
    private static final int STATIC_SCAN_TIMEOUT = 6000;
    private static final int STATIC_SCORE_NETWORK_GOOD = 64;
    private static final int STATIC_SCORE_NETWORK_POOR = 54;
    private static final int STATIC_TRAFFIC_SAMPL_INTERVAL_INVALID = 2000;
    private static final int STATIC_TRIGGER_SCAN_COUNT = 3;
    private static final int STATIC_VALID_LINK_LOSS_NUM = 0;
    private static final int STATIC_WLAN_INVALID_THRESHOLD = 40;
    private static final int STATIC_WLAN_NETWORK_INVALID = 10;
    private static final int STATIC_WLAN_POLL_THRESHOLD = 15;
    private static final int STATIC_WLAN_TRIGGER_DATA_THRESHOLD = 20;
    private static final int STATIC_WLAN_TRIGGER_WLAN_THRESHOLD = 25;
    private static final String STATISTIC_AUTO_CONN = "event_auto_conn";
    private static final String STATISTIC_MANUAL_LIMIT = "event_manual_limit";
    private static final String STATISTIC_MANUAL_OPERATE = "event_manual_operate";
    private static final int SWITCH_TO_WIFI_TIME_INTERVAL = 6000;
    private static final String TAG = "WN_S";
    private static final int TCP_AVAIL = 1;
    private static final int TCP_SAMPLE_INTERVAL_LONG = 5000;
    private static final int TCP_SAMPLE_INTERVAL_SHORT = 2000;
    private static final int TCP_SHORT_INTERVAL_LIMIT = 50;
    private static final int TCP_STAT_BASE_COUNT = 1000;
    private static final int TCP_STAT_POOR_COUNT = 1020;
    private static final int TCP_UNAVAIL = 2;
    private static final int TRAFFIC_DIFF_BURST = 20;
    private static final int TRAFFIC_DIFF_LOWEST = 1;
    private static final int TRAFFIC_LOW_COUNT = 2;
    private static final String TYPE_CONN_FOR_DATA = "conn_for_data";
    private static final String TYPE_CONN_FOR_DATA_TO_WLAN = "conn_for_data_to_wlan";
    private static final String TYPE_CONN_NET_WLAN = "conn_net_wlan";
    private static final String TYPE_CONN_NEW_FOR_DATA = "conn_new_fordata";
    private static final String TYPE_CONN_SAVE_FOR_WLAN = "conn_save_forwlan";
    private static final String TYPE_DATA_TO_WLAN = "data_to_wlan";
    private static final String TYPE_DIABLE_DATA = "disable_data";
    private static final String TYPE_DISABLE_DATA_SWITCH_FOR_DATA = "disable_data_switch_fordata";
    private static final String TYPE_DISABLE_SWITCH_FOR_DATA = "disable_switch_fordata";
    private static final String TYPE_DISABLE_WIFI_FOR_DATA = "diable_wifi_fordata";
    private static final String TYPE_DISABLE_WIFI_FOR_WLAN = "diable_wifi_forwlan";
    private static final String TYPE_ENABLE_WIFI_FOR_DATA = "enable_wifi_fordata";
    private static final String TYPE_MONITOR_EXP = "monitor_exp";
    private static final String TYPE_MONITOR_WLAN = "monitor_wlan";
    private static final int TYPE_NET_DISCONNECT = 0;
    private static final int TYPE_NET_STATE_CHANGE = 2;
    private static final int TYPE_NET_SWITCH = 1;
    private static final String TYPE_REMOVE_NETWORK_FOR_DATA = "remove_network_fordata";
    private static final String TYPE_REMOVE_NETWORK_FOR_WLAN = "remove_network_forwlan";
    private static final String TYPE_WLAN_TO_DATA = "wlan_to_data";
    private static final String TYPE_WLAN_TO_WLAN = "wlan_to_wlan";
    private static final int UNAVAILABLE_STAT_COUNT = 10;
    private static final int VALID_SCORE_THRESHOLD = 20;
    private static final String WIFI_ASSISTANT = "wifi_assistant";
    private static final String WIFI_ASSISTANT_FILE = "/data/misc/wifi/wifi_assistant";
    private static final String WIFI_ASSISTANT_VERIFY = "android.net.wifi.OPPO_VERIFY_WIFI";
    private static final String WIFI_AUTO_CHANGE_NETWORK = "wifi_auto_change_network";
    private static final String WIFI_AVAILABLE_FILE = "/data/misc/wifi/network_available";
    private static final String WIFI_NETWORK_CHANGE = "android.net.wifi.WIFI_NETWORK_INVALID";
    private static final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    private static final String WIFI_SCROE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final String WIFI_TO_DATA = "android.net.wifi.WIFI_TO_DATA";
    private static final int WLAN_NETWORK_INVALID = 10;
    private static AlertDialog mAlertDialog = null;
    private static ColorListDialog mDataAlertDialog = null;
    private static Object mDialogLock = new Object();
    private static AlertDialog mSlaDialog = null;
    private static boolean sDebug = false;
    private int DATA_STALL_LOW_COUNT = 6;
    private long mAccessNetTime;
    private AlarmManager mAlarmManager;
    private OppoWifiAssistantUtils mAssistantUtils;
    private long mAutoConnDataTime;
    private long mAutoConnWlanTime;
    private long mAutoDataToWlanTime;
    private boolean mAutoSwitch;
    private int mAutoSwitchDataCount;
    private long mAutoSwitchDataDisableTime;
    private int mAutoSwitchDataIndex;
    private long mAutoSwitchDataTime;
    private long[] mAutoSwitchDataTimes;
    private boolean mAutoSwithToData;
    private CharSequence mAvailableAP;
    private Handler mBroadHandle;
    private String mBroadInfo;
    private BroadcastReceiver mBroadcastReceiver;
    private ConnectivityManager mCM;
    private boolean mCanTriggerData;
    private String mCandidateKey;
    private boolean mCaptivePortal;
    private int mChangeRssi;
    private int mChangeScore;
    private boolean mChangedNetwork;
    private boolean mChangedToData;
    private boolean mClickDialogSwitch;
    private State mCompletedState;
    private int mConnFail;
    private int mConnectId;
    private String mConnectKey;
    private String mConnectSSID;
    private long mConnectTime;
    private int mConnectedId;
    private State mConnectedState;
    private int mConnectingId;
    private String mConnectingkey;
    private Context mContext;
    private Network mCurNetwork;
    private VolumeWeightedEMA mCurrentLoss;
    private long mDTxPkts;
    private boolean mDataAutoSwitch;
    private int mDataScore = 10;
    private boolean mDataState;
    private DataStateObserver mDataStateObserver;
    private State mDefaultState;
    private int mDetectInterCount;
    private PendingIntent mDetectInterIntent;
    private boolean mDetectNet;
    private State mDisconnectedState;
    private String[] mFallbackHttpServers;
    private boolean mFeatureState;
    private CharSequence mGoodAvailableAP;
    private int mGotInternetResult;
    private Handler mHandler;
    private State mHandshakeState;
    private int mIndex;
    private boolean mInitAutoConnect;
    private State mInitState;
    private boolean mInterChangeToInvalid;
    private int mInterInteval;
    private boolean mInterResult;
    private Handler mInterThread;
    private String mInterfaceName;
    private String[] mInternalServers;
    private boolean mInternetDetecting;
    private int mInternetInvalidCount;
    private int mInternetStandoffTime;
    private boolean mIsFirstPktCntFetchSucceed;
    private int mIsNewConfig;
    private boolean mIsScreenOn;
    private boolean mIsSoftAP;
    private String mLastBssid;
    private String mLastConfigkey;
    private long mLastDetectInter;
    private boolean mLastInternetResult;
    private int mLastNetId;
    private RssiPacketCountInfo mLastPktInfo;
    private long mLastRxPkts;
    private long mLastScanTime;
    private long mLastSwitchedToWifiTime;
    private boolean mLastToData;
    private long mLastTrigDataTime;
    private int mLastTxBad;
    private int mLastTxGood;
    private long mLastTxPkts;
    private long mLastuseTime;
    private int mLinkDetectTimes;
    private int mLinkInterval;
    private double[] mLossArray;
    private double mLossInit;
    private int mLowTrafficeCount;
    private int mLowTrafficeThreshold;
    private boolean mManualConnect;
    private int[] mNetQualityArray;
    private int mNetQulityGoodCount;
    private State mNetworkMonitorState;
    private String mNewBssid;
    private String mNewSsid;
    private String mOldBssid;
    private String mOldSsid;
    private int mOldTcpStatus;
    private OppoSlaManager mOppoSlaManager;
    private OppoTcpInfoMonitor mOppoTcpInfoMonitor;
    private String[] mPublicHttpsServers;
    private boolean mResponseGotFromGateway;
    private int mRoamdetectCount;
    private int mRssiFetchToken;
    private int mRxPktsLowCount;
    private ScanRequestProxy mScanRequestProxy;
    private boolean mScreenOn;
    private SupplicantStateTracker mSupplicantTracker;
    private int mTcpInterval;
    private int mTcpLinkStatus;
    private int mTcpShortIntervalCount;
    private int mTcpStatistics;
    private int[] mTcpstateArray;
    private TelephonyManager mTelephonyManager;
    private String mTestApk = "com.oppo.wifiassistant";
    private int mTrafficInteval;
    private int mTrigScanCount;
    private boolean mTriggerInter;
    private boolean mTriggerScan;
    private String mUnavailableKey;
    private State mVerifyInternetState;
    private WifiConfigManager mWifiConfigManager;
    private WifiConfigStore mWifiConfigStore;
    private boolean mWifiConnected;
    private WifiNative mWifiNative;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;
    private OppoWifiSmartSwitcher mWifiSmartSwitcher;
    private int mWifiState;
    private ClientModeImpl mWifiStateMachine;
    private int mWlanIfIndex;
    private int mWlanInvalidThreshold;
    protected final DelayedDiskWrite mWriter;
    private AsyncChannel mWsmChannel;
    private long mdRxPkts;

    static /* synthetic */ int access$10804(OppoWifiAssistantStateTraker x0) {
        int i = x0.mRssiFetchToken + 1;
        x0.mRssiFetchToken = i;
        return i;
    }

    static /* synthetic */ int access$3972(OppoWifiAssistantStateTraker x0, int x1) {
        int i = x0.mGotInternetResult & x1;
        x0.mGotInternetResult = i;
        return i;
    }

    static /* synthetic */ int access$3976(OppoWifiAssistantStateTraker x0, int x1) {
        int i = x0.mGotInternetResult | x1;
        x0.mGotInternetResult = i;
        return i;
    }

    static /* synthetic */ int access$7108(OppoWifiAssistantStateTraker x0) {
        int i = x0.mInternetInvalidCount;
        x0.mInternetInvalidCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$9808(OppoWifiAssistantStateTraker x0) {
        int i = x0.mDetectInterCount;
        x0.mDetectInterCount = i + 1;
        return i;
    }

    public OppoWifiAssistantStateTraker(Context c, ClientModeImpl wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst, WifiRomUpdateHelper wruh, Handler t, ScanRequestProxy mSrp) {
        super(TAG, t.getLooper());
        boolean z = false;
        this.mClickDialogSwitch = false;
        this.mLastScanTime = 0;
        this.mWifiState = 1;
        this.mConnectedId = -1;
        this.mConnectingId = -1;
        this.mConnectId = -1;
        this.mLastNetId = -1;
        this.mLastBssid = " ";
        this.mIsSoftAP = false;
        this.mCaptivePortal = false;
        this.mResponseGotFromGateway = false;
        this.mConnectSSID = null;
        this.mIsScreenOn = true;
        this.mNewSsid = " ";
        this.mOldSsid = " ";
        this.mNewBssid = " ";
        this.mOldBssid = " ";
        this.mUnavailableKey = " ";
        this.mManualConnect = false;
        this.mAutoSwitch = true;
        this.mDataAutoSwitch = true;
        this.mFeatureState = true;
        this.mInitAutoConnect = true;
        this.mWlanIfIndex = 0;
        this.mWlanInvalidThreshold = 40;
        this.mLinkDetectTimes = 0;
        this.mRssiFetchToken = 0;
        this.mLastTxGood = 0;
        this.mLastTxBad = 0;
        this.mRxPktsLowCount = 0;
        this.mLowTrafficeCount = 0;
        this.mLowTrafficeThreshold = 0;
        this.mChangeRssi = 0;
        this.mChangeScore = 0;
        this.mWifiConnected = false;
        this.mCanTriggerData = true;
        this.mTriggerInter = false;
        this.mTriggerScan = false;
        this.mChangedNetwork = false;
        this.mNetQualityArray = new int[]{79, 79, 79, 79};
        this.mTcpstateArray = new int[]{0, 0, 0, 0};
        this.mLossInit = 0.0d;
        double d = this.mLossInit;
        this.mLossArray = new double[]{d, d, d, d};
        this.mOldTcpStatus = 0;
        this.mIndex = 0;
        this.mRoamdetectCount = 0;
        this.mTrigScanCount = 0;
        this.mNetQulityGoodCount = 0;
        this.mInternalServers = new String[]{"conn1.oppomobile.com", "conn2.oppomobile.com"};
        this.mPublicHttpsServers = new String[]{"https://m.baidu.com", "https://sina.cn", "https://m.sohu.com"};
        this.mFallbackHttpServers = new String[]{"http://www.google.cn/generate_204", "http://developers.google.cn/generate_204"};
        this.mDetectInterCount = 0;
        this.mInterResult = false;
        this.mLastInternetResult = false;
        this.mInternetDetecting = false;
        this.mDetectNet = false;
        this.mChangedToData = false;
        this.mLastToData = false;
        this.mScreenOn = true;
        this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
        this.mInternetStandoffTime = 0;
        this.mInternetInvalidCount = 0;
        this.mInterChangeToInvalid = false;
        this.mCandidateKey = "";
        this.mDataStateObserver = null;
        this.mDataState = false;
        this.mOppoTcpInfoMonitor = null;
        this.mTcpLinkStatus = 0;
        this.mTcpStatistics = 0;
        this.mGotInternetResult = 0;
        this.mTcpShortIntervalCount = 0;
        this.mAutoSwitchDataCount = 0;
        this.mAutoSwitchDataTime = 0;
        this.mAutoSwitchDataTimes = new long[]{0, 0, 0, 0, 0};
        this.mAutoSwitchDataDisableTime = 0;
        this.mAutoSwitchDataIndex = 0;
        this.mLastSwitchedToWifiTime = 0;
        this.mAutoSwithToData = false;
        this.mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
        this.mWsmChannel = new AsyncChannel();
        this.mDefaultState = new DefaultState();
        this.mInitState = new InitState();
        this.mDisconnectedState = new DisconnectedState();
        this.mHandshakeState = new HandshakeState();
        this.mCompletedState = new CompletedState();
        this.mVerifyInternetState = new VerifyInternetState();
        this.mConnectedState = new ConnectedState();
        this.mNetworkMonitorState = new NetworkMonitorState();
        this.mIsFirstPktCntFetchSucceed = true;
        this.mWifiRomUpdateHelper = null;
        this.mContext = c;
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mWifiStateMachine = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mWifiRomUpdateHelper = wruh;
        this.mHandler = new NetHandler(t.getLooper());
        this.mWriter = new DelayedDiskWrite();
        this.mScanRequestProxy = mSrp;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDetectInterIntent = getPrivateBroadcast(ACTION_DETECT_INTERNET);
        this.mWifiConfigManager.setWifiNetworkAvailable(this);
        this.mWsmChannel.connectSync(this.mContext, getHandler(), wsm.getMessenger());
        setupNetworkReceiver();
        HandlerThread mTread = new HandlerThread("WifiNetCheckInter");
        mTread.start();
        this.mInterThread = new Handler(mTread.getLooper());
        HandlerThread mBroadTread = new HandlerThread("WifiNetBroad");
        mBroadTread.start();
        this.mBroadHandle = new Handler(mBroadTread.getLooper());
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mDataStateObserver = new DataStateObserver(this.mBroadHandle);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mDataStateObserver);
        this.mDataState = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
        initWifiAssistantData();
        this.mDataAutoSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_NETWORK, 1) == 1 ? true : z;
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_NETWORK), true, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass1 */

            public void onChange(boolean selfChange) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                boolean z = true;
                if (Settings.Global.getInt(oppoWifiAssistantStateTraker.mContext.getContentResolver(), OppoWifiAssistantStateTraker.WIFI_AUTO_CHANGE_NETWORK, 1) != 1) {
                    z = false;
                }
                oppoWifiAssistantStateTraker.mDataAutoSwitch = z;
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker2.setDataAutoSwitch(oppoWifiAssistantStateTraker2.mDataAutoSwitch);
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker3.logD(" mdas= " + OppoWifiAssistantStateTraker.this.mDataAutoSwitch);
            }
        });
        this.mFeatureState = getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue();
        addState(this.mDefaultState);
        addState(this.mInitState, this.mDefaultState);
        addState(this.mDisconnectedState, this.mDefaultState);
        addState(this.mHandshakeState, this.mDefaultState);
        addState(this.mCompletedState, this.mDefaultState);
        addState(this.mVerifyInternetState, this.mCompletedState);
        addState(this.mConnectedState, this.mCompletedState);
        addState(this.mNetworkMonitorState, this.mConnectedState);
        setInitialState(this.mInitState);
        start();
        this.mOppoTcpInfoMonitor = new OppoTcpInfoMonitor(this.mContext);
        this.mAssistantUtils = OppoWifiAssistantUtils.getInstance(this.mContext);
        OppoSlaManager.setSlaCallback(new OppoSlaManager.OppoSlaCallback() {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass2 */

            @Override // com.android.server.wifi.OppoSlaManager.OppoSlaCallback
            public void enableSla() {
                Log.d(OppoWifiAssistantStateTraker.TAG, "enableSla mInterResult=" + OppoWifiAssistantStateTraker.this.mInterResult);
                if (OppoWifiAssistantStateTraker.this.mInterResult) {
                    Log.d(OppoWifiAssistantStateTraker.TAG, "enableSla()");
                    OppoWifiAssistantStateTraker.this.mAssistantUtils.setupDataNetwork();
                    OppoWifiAssistantStateTraker.this.mCanTriggerData = false;
                }
            }

            @Override // com.android.server.wifi.OppoSlaManager.OppoSlaCallback
            public void disableSla() {
                Log.d(OppoWifiAssistantStateTraker.TAG, "disableSla() mChangedNetwork=" + OppoWifiAssistantStateTraker.this.mChangedNetwork);
                if (!OppoWifiAssistantStateTraker.this.mChangedNetwork) {
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker.changeNetworkToWlan(oppoWifiAssistantStateTraker.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker.this.mLastConfigkey), 79, true, false);
                }
            }

            @Override // com.android.server.wifi.OppoSlaManager.OppoSlaCallback
            public void showSlaDialog() {
                Log.d(OppoWifiAssistantStateTraker.TAG, "showSlaDialog() mInterResult=" + OppoWifiAssistantStateTraker.this.mInterResult);
                if (OppoWifiAssistantStateTraker.this.mInterResult) {
                    OppoWifiAssistantStateTraker.this.showDialogForSla();
                }
            }

            @Override // com.android.server.wifi.OppoSlaManager.OppoSlaCallback
            public boolean getWifiInterResult() {
                Log.d(OppoWifiAssistantStateTraker.TAG, "getInterResult() mInterResult=" + OppoWifiAssistantStateTraker.this.mInterResult);
                return OppoWifiAssistantStateTraker.this.mInterResult;
            }
        });
        this.mOppoSlaManager = OppoSlaManager.getInstance(this.mContext);
        this.mOppoSlaManager.setCellState(this.mDataState);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDebug = true;
        } else {
            sDebug = false;
        }
        OppoWifiSmartSwitcher oppoWifiSmartSwitcher = this.mWifiSmartSwitcher;
        if (oppoWifiSmartSwitcher != null) {
            oppoWifiSmartSwitcher.enableVerboseLogging(verbose);
        }
        OppoTcpInfoMonitor oppoTcpInfoMonitor = this.mOppoTcpInfoMonitor;
        if (oppoTcpInfoMonitor != null) {
            oppoTcpInfoMonitor.enableVerboseLogging(verbose);
        }
        OppoSlaManager oppoSlaManager = this.mOppoSlaManager;
        if (oppoSlaManager != null) {
            oppoSlaManager.enableVerboseLogging(verbose);
        }
        OppoWifiAssistantUtils oppoWifiAssistantUtils = this.mAssistantUtils;
        if (oppoWifiAssistantUtils != null) {
            oppoWifiAssistantUtils.enableVerboseLogging(verbose);
            OppoWifiAssistantRecord lastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
            if (lastRecord != null && lastRecord.mHistoryRecordQueue != null) {
                lastRecord.mHistoryRecordQueue.enableVerboseLogging(verbose);
            }
        }
    }

    private void setupNetworkReceiver() {
        IntentFilter netWorkFilter = new IntentFilter();
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_CONNECT);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_STATE);
        netWorkFilter.addAction("android.net.wifi.STATE_CHANGE");
        netWorkFilter.addAction("android.intent.action.SCREEN_ON");
        netWorkFilter.addAction("android.intent.action.SCREEN_OFF");
        netWorkFilter.addAction(ACTION_DETECT_INTERNET);
        netWorkFilter.addAction(ACTION_NOTIFY_GATEWAY_MAC);
        netWorkFilter.addAction(WIFI_TO_DATA);
        netWorkFilter.addAction(DEFAULT_NETWORK_CHANGED);
        netWorkFilter.addAction("android.net.conn.NETWORK_CONDITIONS_MEASURED");
        netWorkFilter.addAction(DATA_SCORE_CHANGE);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                WifiConfiguration netConf;
                String action = intent.getAction();
                if (!OppoWifiAssistantStateTraker.this.mFeatureState || !OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                    OppoWifiAssistantStateTraker.this.logD("mfs dis");
                    return;
                }
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker.logD("AssistReceiver event:" + action);
                if (action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_CONNECT) || action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_STATE)) {
                    return;
                }
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        String ifName = intent.getStringExtra(OppoWifiAssistantStateTraker.EXTRA_IFACE_NAME);
                        if (!OppoWifiAssistantStateTraker.this.mInterfaceName.equals(ifName)) {
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker2.logD("Received NETWORK_STATE_CHANGED_ACTION, mInterfaceName=" + OppoWifiAssistantStateTraker.this.mInterfaceName + " iface_name=" + ifName);
                            return;
                        }
                        OppoWifiAssistantStateTraker.this.mWifiConnected = true;
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker.ACTION_DETECT_INTERNET)) {
                    String ifName2 = intent.getStringExtra(OppoWifiAssistantStateTraker.EXTRA_IFACE_NAME);
                    if (ifName2 == null || !ifName2.equals(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker3.logD("Received ACTION_DETECT_INTERNET , mInterfaceName=" + OppoWifiAssistantStateTraker.this.mInterfaceName + " iface_name=" + ifName2);
                        return;
                    }
                    OppoWifiAssistantStateTraker.this.sendMessage(200710);
                } else if (action.equals(OppoWifiAssistantStateTraker.ACTION_NOTIFY_GATEWAY_MAC)) {
                    String ifName3 = intent.getStringExtra(OppoWifiAssistantStateTraker.EXTRA_IFACE_NAME);
                    if (ifName3 != null && ifName3.equals(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                        String gatewayIp = intent.getStringExtra("gateway_ip");
                        String gatewayMac = intent.getStringExtra("gateway_mac");
                        if (!TextUtils.isEmpty(gatewayMac) && !TextUtils.isEmpty(gatewayIp)) {
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker4.logD(" setIfGatewayIP=" + gatewayIp + " setIfGatewayMac=" + gatewayMac);
                            OppoWifiAssistantStateTraker.this.mAssistantUtils.setIfGatewayInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, gatewayIp, gatewayMac);
                        }
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    if (OppoWifiAssistantStateTraker.this.mChangedToData) {
                        OppoWifiAssistantStateTraker.this.mInterInteval = 120000;
                    } else {
                        OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                    }
                    OppoWifiAssistantStateTraker.this.mScreenOn = true;
                    if (OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mNetworkMonitorState && OppoWifiAssistantStateTraker.this.mLastDetectInter != 0 && System.currentTimeMillis() - OppoWifiAssistantStateTraker.this.mLastDetectInter > ((long) OppoWifiAssistantStateTraker.this.mInterInteval)) {
                        OppoWifiAssistantStateTraker.this.setInternetDetectAlarm(1, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                    OppoWifiAssistantStateTraker.this.mScreenOn = false;
                    OppoWifiAssistantStateTraker.this.mLowTrafficeCount = 0;
                } else if (action.equals(OppoWifiAssistantStateTraker.WIFI_TO_DATA)) {
                    OppoWifiAssistantStateTraker.this.mChangedToData = intent.getBooleanExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_TO_DATA, false);
                    if (OppoWifiAssistantStateTraker.this.mChangedToData != OppoWifiAssistantStateTraker.this.mLastToData) {
                        if (!OppoWifiAssistantStateTraker.this.mChangedToData) {
                            OppoWifiAssistantStateTraker.this.mAssistantUtils.resetTriggerData();
                            if (!OppoWifiAssistantStateTraker.this.mInterResult) {
                                if (OppoWifiAssistantStateTraker.this.mDataAutoSwitch && OppoWifiAssistantStateTraker.this.mAssistantUtils.getWifiBestScore() <= 10) {
                                    OppoWifiAssistantStateTraker.this.mAssistantUtils.setupDataNetwork();
                                }
                                if (OppoWifiAssistantStateTraker.this.mWlanInvalidThreshold == 70 && OppoWifiAssistantStateTraker.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                                    OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                                }
                            } else if (OppoWifiAssistantStateTraker.this.mWifiStateMachine != null && OppoWifiAssistantStateTraker.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                                OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                            }
                            OppoWifiAssistantStateTraker.this.mCanTriggerData = true;
                            OppoWifiAssistantStateTraker.this.mChangedNetwork = false;
                            OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker5.sendMessage(OppoWifiAssistantStateTraker.EVENT_DISMISS_ALERT, oppoWifiAssistantStateTraker5.mLastConfigkey);
                        } else {
                            OppoWifiAssistantStateTraker.this.mInterInteval = 120000;
                            OppoWifiAssistantStateTraker.this.mTcpInterval = 5000;
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker6.setInternetDetectAlarm(1, (long) oppoWifiAssistantStateTraker6.mInterInteval);
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker7 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker7.logD("mcn=" + OppoWifiAssistantStateTraker.this.mChangedNetwork + ",mcds=" + OppoWifiAssistantStateTraker.this.mClickDialogSwitch + ",micti=" + OppoWifiAssistantStateTraker.this.mInterChangeToInvalid + ",mir=" + OppoWifiAssistantStateTraker.this.mInterResult);
                            if (OppoWifiAssistantStateTraker.this.mChangedNetwork && !OppoWifiAssistantStateTraker.this.mClickDialogSwitch && ((!OppoWifiAssistantStateTraker.this.mInterChangeToInvalid && OppoWifiAssistantStateTraker.this.mInterResult) || OppoWifiAssistantStateTraker.this.mInterChangeToInvalid)) {
                                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker8 = OppoWifiAssistantStateTraker.this;
                                oppoWifiAssistantStateTraker8.sendMessage(OppoWifiAssistantStateTraker.EVENT_SHOW_ALERT, oppoWifiAssistantStateTraker8.mLastConfigkey);
                            }
                            if (OppoWifiAssistantStateTraker.this.mClickDialogSwitch) {
                                OppoWifiAssistantStateTraker.this.mAutoSwithToData = false;
                            } else {
                                OppoWifiAssistantStateTraker.this.mAutoSwithToData = true;
                            }
                            OppoWifiAssistantStateTraker.this.mClickDialogSwitch = false;
                            OppoWifiAssistantStateTraker.this.detectSwitchDataFrequence();
                            if (OppoWifiAssistantStateTraker.this.mChangedNetwork) {
                                OppoWifiAssistantStateTraker.this.mAutoConnDataTime = System.currentTimeMillis();
                                OppoWifiAssistantStateTraker.this.mAutoConnWlanTime = 0;
                                OppoWifiAssistantStateTraker.this.mAutoDataToWlanTime = 0;
                                OppoWifiAssistantRecord curTodataRecord = OppoWifiAssistantStateTraker.this.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker.this.mLastConfigkey);
                                int validRecordCount = OppoWifiAssistantStateTraker.this.mAssistantUtils.getSortNetworkRecords().size();
                                OppoWifiAssistantStateTraker.this.logD("stc wda");
                                OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_WLAN_TO_DATA, curTodataRecord, null, validRecordCount);
                            }
                        }
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker9 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker9.mLastToData = oppoWifiAssistantStateTraker9.mChangedToData;
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker.DEFAULT_NETWORK_CHANGED)) {
                    OppoWifiAssistantStateTraker.this.mOppoSlaManager.notifyDefaultNetwork(intent.getBooleanExtra(OppoWifiAssistantStateTraker.EXTRA_DEFAULT_NET_CELL, false));
                } else if (action.equals("android.net.conn.NETWORK_CONDITIONS_MEASURED")) {
                    if (intent.getIntExtra("extra_connectivity_type", -1) == 1 && intent.getBooleanExtra("extra_is_captive_portal", false)) {
                        String ifName4 = intent.getStringExtra(OppoWifiAssistantStateTraker.EXTRA_IFACE_NAME);
                        if (!OppoWifiAssistantStateTraker.this.mInterfaceName.equals(ifName4)) {
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker10 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker10.logD("Received ACTION_NETWORK_CONDITIONS_MEASURED, mInterfaceName=" + OppoWifiAssistantStateTraker.this.mInterfaceName + " iface_name=" + ifName4);
                            return;
                        }
                        OppoWifiAssistantStateTraker.this.logD("Received ACTION_NETWORK_CONDITIONS_MEASURED, wlan is captive portal.");
                        if (OppoWifiAssistantStateTraker.this.mIsSoftAP && OppoWifiAssistantStateTraker.this.isThirdAppOperate()) {
                            return;
                        }
                        if (OppoWifiAssistantStateTraker.this.mCaptivePortal) {
                            OppoWifiAssistantStateTraker.this.logD("is cp.");
                            return;
                        }
                        WifiInfo curWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                        String netStateSsid = intent.getStringExtra("extra_ssid");
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker11 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker11.logD("cpnss: " + netStateSsid + ", info: " + curWifiInfo);
                        if (curWifiInfo != null && netStateSsid != null && curWifiInfo.getSSID().equals(netStateSsid) && (netConf = OppoWifiAssistantStateTraker.this.getWifiConfig(netStateSsid, curWifiInfo.getBSSID())) != null && netConf.networkId == OppoWifiAssistantStateTraker.this.mConnectedId) {
                            OppoWifiAssistantStateTraker.this.mCaptivePortal = true;
                            if ((OppoWifiAssistantStateTraker.this.mGotInternetResult & 4) != 4) {
                                OppoWifiAssistantStateTraker.this.sendVerifyBroadcast(netConf.configKey(false));
                                if (OppoWifiAssistantStateTraker.this.mAutoSwitch && OppoWifiAssistantStateTraker.this.mFeatureState && OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                                    OppoWifiAssistantStateTraker.this.sendNetworkStateBroadcast(netConf.configKey(false), false);
                                }
                                int newConfig = 0;
                                if (OppoWifiAssistantStateTraker.this.getIsOppoManuConnect() && OppoWifiAssistantStateTraker.this.mConnectId == netConf.networkId && OppoWifiAssistantStateTraker.this.mConnectId != -1) {
                                    newConfig = OppoWifiAssistantStateTraker.this.mIsNewConfig;
                                }
                                OppoWifiAssistantStateTraker.this.mHandler.sendMessage(OppoWifiAssistantStateTraker.this.mHandler.obtainMessage(OppoWifiAssistantStateTraker.EVENT_CAPTIVE_PORTAL, newConfig, 0, netConf.configKey(false)));
                                Settings.Global.putString(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_SSID, netStateSsid);
                                Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_AVAILABLE, 0);
                                Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_PORTAL, 1);
                            }
                        }
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker.DATA_SCORE_CHANGE)) {
                    int dataScore = intent.getIntExtra(OppoWifiAssistantStateTraker.EXTRA_DATA_CORE, 10);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker12 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker12.logD("new dataScore=" + dataScore);
                    OppoWifiAssistantStateTraker.this.mDataScore = dataScore;
                    OppoWifiAssistantStateTraker.this.mOppoSlaManager.setCellScore(dataScore);
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, netWorkFilter);
    }

    private PendingIntent getPrivateBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(67108864);
        intent.setPackage("android");
        intent.putExtra(EXTRA_IFACE_NAME, this.mInterfaceName);
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
    }

    private void initWifiAssistantData() {
        File oldfile = new File(WIFI_AVAILABLE_FILE);
        if (oldfile.exists()) {
            logD("exists remove");
            oldfile.delete();
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            String str = null;
            switch (msg.what) {
                case 131126:
                case 151553:
                    OppoWifiAssistantStateTraker.this.mConnectId = Integer.valueOf(msg.arg1).intValue();
                    OppoWifiAssistantStateTraker.this.mIsNewConfig = Integer.valueOf(msg.arg2).intValue();
                    WifiConfiguration config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mConnectId);
                    OppoWifiAssistantStateTraker.this.mConnectKey = config != null ? config.configKey(false) : null;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    oppoWifiAssistantStateTraker2.mConnectSSID = str;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker3.logD("mConnectId= " + OppoWifiAssistantStateTraker.this.mConnectId + ", minc= " + OppoWifiAssistantStateTraker.this.mIsNewConfig);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker4.addOrUpdateRecord(oppoWifiAssistantStateTraker4.mConnectKey);
                    break;
                case 131211:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                case WifiMonitor.WRONG_KEY_EVENT /* 147648 */:
                    WifiConfiguration conFail = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mConnectingId);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                    if (conFail != null) {
                        str = conFail.configKey(false);
                    }
                    oppoWifiAssistantStateTraker5.mConnectingkey = str;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker6.updateRecordConnectFail(oppoWifiAssistantStateTraker6.mConnectingkey);
                    if (OppoWifiAssistantStateTraker.this.getIsOppoManuConnect()) {
                        OppoWifiAssistantStateTraker.this.mConnectId = -1;
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    WifiConfiguration config2 = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker7 = OppoWifiAssistantStateTraker.this;
                    if (config2 != null) {
                        str = config2.configKey(false);
                    }
                    oppoWifiAssistantStateTraker7.mLastConfigkey = str;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker8 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker8.transitionTo(oppoWifiAssistantStateTraker8.mCompletedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateReult = (StateChangeResult) msg.obj;
                    OppoWifiAssistantStateTraker.this.mConnectingId = stateReult.networkId;
                    WifiConfiguration config3 = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mConnectingId);
                    OppoWifiAssistantStateTraker.this.mConnectingkey = config3 != null ? config3.configKey(false) : null;
                    WifiInfo curWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                    if (stateReult.state == SupplicantState.DISCONNECTED) {
                        OppoWifiAssistantStateTraker.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, null);
                    } else if (stateReult.state == SupplicantState.COMPLETED) {
                        OppoWifiAssistantStateTraker.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, curWifiInfo);
                    }
                    WifiInfo otherWifiInfo = OppoWifiAssistantStateTraker.this.mAssistantUtils.getOtherIfWifiInfo(OppoWifiAssistantStateTraker.this.mInterfaceName);
                    if (otherWifiInfo != null && stateReult.state == SupplicantState.COMPLETED && (((curWifiInfo.is24GHz() && otherWifiInfo.is24GHz()) || (curWifiInfo.is5GHz() && otherWifiInfo.is5GHz())) && curWifiInfo.getFrequency() != otherWifiInfo.getFrequency())) {
                        Log.w(OppoWifiAssistantStateTraker.TAG, "wlan0 roamed to the same band with wlan1, disableDualSta..");
                        OppoWifiAssistantStateTraker.this.mAssistantUtils.disableDualSta();
                        break;
                    }
                case 200705:
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    WifiConfiguration config4 = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    OppoWifiAssistantStateTraker.this.mLastConfigkey = config4 != null ? config4.configKey(false) : null;
                    if (msg.arg2 != 2) {
                        if (msg.arg2 == 0) {
                            if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                                OppoWifiAssistantStateTraker.this.dismissDialog(0);
                            }
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker9 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker9.updateRecordDisableState(oppoWifiAssistantStateTraker9.mLastConfigkey);
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker10 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker10.transitionTo(oppoWifiAssistantStateTraker10.mDisconnectedState);
                            break;
                        }
                    } else {
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker11 = OppoWifiAssistantStateTraker.this;
                        if (config4 != null) {
                            str = config4.SSID;
                        }
                        oppoWifiAssistantStateTraker11.mConnectSSID = str;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker12 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker12.transitionTo(oppoWifiAssistantStateTraker12.mConnectedState);
                        break;
                    }
                    break;
                case 200706:
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker13 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker13.transitionTo(oppoWifiAssistantStateTraker13.mInitState);
                    break;
                case 200712:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = true;
                    break;
                case 200713:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = false;
                    break;
            }
            return true;
        }
    }

    class InitState extends State {
        InitState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.mWifiState = oppoWifiAssistantStateTraker.mWifiStateMachine.syncGetWifiState();
            OppoWifiAssistantStateTraker.this.mLastTxBad = 0;
            OppoWifiAssistantStateTraker.this.mLastTxGood = 0;
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            int i = msg.what;
            if (i == 200706) {
                OppoWifiAssistantStateTraker.this.logD("it is Initstate, do not handle!");
                return true;
            } else if (i != 200712) {
                return false;
            } else {
                return true;
            }
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD("mLastNetId = " + OppoWifiAssistantStateTraker.this.mLastNetId + ", mConnectedId = " + OppoWifiAssistantStateTraker.this.mConnectedId + ", mLastConfigkey = " + OppoWifiAssistantStateTraker.this.mLastConfigkey);
            OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
            if (OppoWifiAssistantStateTraker.this.mLastNetId == OppoWifiAssistantStateTraker.this.mConnectedId && OppoWifiAssistantStateTraker.this.mLastNetId != -1 && OppoWifiAssistantStateTraker.this.mConnectingId == OppoWifiAssistantStateTraker.this.mConnectedId) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker2.updateRecordUseTime(oppoWifiAssistantStateTraker2.mLastConfigkey);
            }
            OppoWifiAssistantStateTraker.this.mLastNetId = -1;
            OppoWifiAssistantStateTraker.this.mLastBssid = " ";
            OppoWifiAssistantStateTraker.this.mUnavailableKey = " ";
            OppoWifiAssistantStateTraker.this.mAutoSwithToData = false;
            OppoWifiAssistantStateTraker.this.mInterResult = false;
            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
            OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = false;
            OppoWifiAssistantStateTraker.this.mLastPktInfo = null;
            OppoWifiAssistantStateTraker.this.mGotInternetResult = 0;
            OppoWifiAssistantStateTraker.this.mLastDetectInter = 0;
            OppoWifiAssistantStateTraker.this.mTrigScanCount = 0;
            OppoWifiAssistantStateTraker.this.mDTxPkts = 0;
            OppoWifiAssistantStateTraker.this.mdRxPkts = 0;
            OppoWifiAssistantStateTraker.this.mIsSoftAP = false;
            OppoWifiAssistantStateTraker.this.mCaptivePortal = false;
            OppoWifiAssistantStateTraker.this.mResponseGotFromGateway = false;
            OppoWifiAssistantStateTraker.this.mWifiConnected = false;
            OppoWifiAssistantStateTraker.this.mIsFirstPktCntFetchSucceed = true;
            OppoWifiAssistantStateTraker.this.resetAutoSwitchDataDetect();
            if (OppoWifiAssistantStateTraker.this.mAssistantUtils.getEnabledState()) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker3.sendNetworkStateBroadcast(oppoWifiAssistantStateTraker3.mLastConfigkey, true);
                OppoWifiAssistantStateTraker.this.sendWifiToDataBroadcast(false, 79);
            }
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setCanChangeToCell(OppoWifiAssistantStateTraker.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setCanShowDialog(OppoWifiAssistantStateTraker.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setWifiScore(OppoWifiAssistantStateTraker.this.mInterfaceName, -1);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setIfGatewayInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, "", "");
            OppoWifiAssistantStateTraker.this.mOppoSlaManager.setWifiConnectionState(OppoWifiAssistantStateTraker.this.mWlanIfIndex, false);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, null);
            Log.d(OppoWifiAssistantStateTraker.TAG, "wlan0 disconnected, disableDualStaWithDelay..");
            OppoWifiAssistantStateTraker.this.mAssistantUtils.disableDualStaWithDelay(5000);
            Settings.System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT, 0);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            if (msg.what != 200712) {
                return false;
            }
            return true;
        }
    }

    class HandshakeState extends State {
        HandshakeState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            if (msg.what != 200712) {
                return false;
            }
            return true;
        }
    }

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mGotInternetResult = 0;
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setCanChangeToCell(OppoWifiAssistantStateTraker.this.mInterfaceName, false);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setCanShowDialog(OppoWifiAssistantStateTraker.this.mInterfaceName, false);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            int i = msg.what;
            if (i == 147459) {
                OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
                OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                if (OppoWifiAssistantStateTraker.this.mLastNetId != msg.arg1) {
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    WifiConfiguration config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    OppoWifiAssistantStateTraker.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                }
            } else if (i != 200705) {
                return false;
            } else {
                OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                int completestate = msg.arg2;
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker2.logD("cptst = " + completestate + ",msg.arg1= " + msg.arg1);
                WifiConfiguration config2 = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                if (config2 != null) {
                    OppoWifiAssistantStateTraker.this.mLastConfigkey = config2.configKey(false);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker3.logD("cptst = " + completestate + ",mLastConfigkey= " + OppoWifiAssistantStateTraker.this.mLastConfigkey + ",mLastNetId= " + OppoWifiAssistantStateTraker.this.mLastNetId + ",mConnectSSID= " + OppoWifiAssistantStateTraker.this.mConnectSSID);
                    if (completestate == 0) {
                        if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                            OppoWifiAssistantStateTraker.this.dismissDialog(0);
                        }
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker4.updateRecordDisableState(oppoWifiAssistantStateTraker4.mLastConfigkey);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker5.transitionTo(oppoWifiAssistantStateTraker5.mDisconnectedState);
                    } else if (completestate == 1) {
                        OppoWifiAssistantStateTraker.this.mConnectSSID = config2.SSID;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker6.transitionTo(oppoWifiAssistantStateTraker6.mVerifyInternetState);
                    } else if (completestate == 2) {
                        OppoWifiAssistantStateTraker.this.mConnectSSID = config2.SSID;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker7 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker7.transitionTo(oppoWifiAssistantStateTraker7.mConnectedState);
                    }
                }
            }
            return true;
        }
    }

    class VerifyInternetState extends State {
        VerifyInternetState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.mConnectedId = oppoWifiAssistantStateTraker.mLastNetId;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker2.logD("VerifyInternetState mConnectedId= " + OppoWifiAssistantStateTraker.this.mConnectedId);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            if (msg.what != 200707) {
                return false;
            }
            if (!OppoWifiAssistantStateTraker.this.getIsOppoManuConnect() && !OppoWifiAssistantStateTraker.this.mInterResult) {
                return true;
            }
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker2.transitionTo(oppoWifiAssistantStateTraker2.mNetworkMonitorState);
            return true;
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mWifiConnected = true;
            OppoWifiAssistantStateTraker.this.mConnectTime = System.currentTimeMillis();
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.mConnectedId = oppoWifiAssistantStateTraker.mLastNetId;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker2.logD("ConnectedState mLastConfigkey= " + OppoWifiAssistantStateTraker.this.mLastConfigkey + ", mConnectedId= " + OppoWifiAssistantStateTraker.this.mConnectedId);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker3.updateRecordConCount(oppoWifiAssistantStateTraker3.mLastConfigkey);
            if (OppoWifiAssistantStateTraker.this.mCandidateKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker4.mAutoConnWlanTime = oppoWifiAssistantStateTraker4.mConnectTime;
                OppoWifiAssistantStateTraker.this.mAutoConnDataTime = 0;
                OppoWifiAssistantStateTraker.this.mAutoDataToWlanTime = 0;
                OppoWifiAssistantStateTraker.this.logD("stc wwa");
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker5.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_WLAN_TO_WLAN, oppoWifiAssistantStateTraker5.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker.this.mLastConfigkey), null, -127);
                OppoWifiAssistantStateTraker.this.mCandidateKey = "";
            }
            OppoWifiAssistantStateTraker.this.mTcpStatistics = 0;
            OppoWifiAssistantStateTraker.this.mTcpInterval = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
            OppoWifiAssistantStateTraker.this.mOppoTcpInfoMonitor.resetTcpLinkStatus();
            OppoWifiAssistantStateTraker.this.removeMessages(200711);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker6.sendMessageDelayed(oppoWifiAssistantStateTraker6.obtainMessage(200711), (long) OppoWifiAssistantStateTraker.this.mTcpInterval);
            OppoWifiAssistantStateTraker.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker.this.mInterfaceName, OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo());
            OppoWifiAssistantStateTraker.this.mOppoSlaManager.setWifiConnectionState(OppoWifiAssistantStateTraker.this.mWlanIfIndex, true);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case 200705:
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    int connectedstate = msg.arg2;
                    WifiConfiguration config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    OppoWifiAssistantStateTraker.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker2.logD("cctst= " + connectedstate);
                    if (connectedstate == 0) {
                        if (config != null) {
                            Settings.Global.putString(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_SSID, config.SSID);
                            Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_AVAILABLE, 0);
                            Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_PORTAL, 0);
                        }
                        if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                            OppoWifiAssistantStateTraker.this.dismissDialog(0);
                        }
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker3.updateRecordDisableState(oppoWifiAssistantStateTraker3.mLastConfigkey);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker4.transitionTo(oppoWifiAssistantStateTraker4.mDisconnectedState);
                        break;
                    }
                    break;
                case 200707:
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker5.transitionTo(oppoWifiAssistantStateTraker5.mNetworkMonitorState);
                    break;
                case 200711:
                    if (OppoWifiAssistantStateTraker.this.needToDetectTcpStatus()) {
                        OppoWifiAssistantStateTraker.this.detectTcpStatus();
                        break;
                    }
                    break;
                case 200712:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = true;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class NetworkMonitorState extends State {
        NetworkMonitorState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.logD(getName() + ",mLastConfigkey=" + OppoWifiAssistantStateTraker.this.mLastConfigkey);
            OppoWifiAssistantStateTraker.this.mLastTrigDataTime = System.currentTimeMillis();
            if (OppoWifiAssistantStateTraker.this.mInterfaceName != null) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker2.mLastTxPkts = oppoWifiAssistantStateTraker2.mOppoSlaManager.getWlanTcpTxPackets(0);
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker3.mLastRxPkts = oppoWifiAssistantStateTraker3.mOppoSlaManager.getWlanTcpRxPackets(0);
            } else {
                OppoWifiAssistantStateTraker.this.mLastTxPkts = 0;
                OppoWifiAssistantStateTraker.this.mLastRxPkts = 0;
            }
            OppoWifiAssistantStateTraker.this.mRxPktsLowCount = 0;
            OppoWifiAssistantStateTraker.this.mDTxPkts = 0;
            OppoWifiAssistantStateTraker.this.mdRxPkts = 0;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker4.mLinkInterval = oppoWifiAssistantStateTraker4.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", 5000).intValue();
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker5.mTrafficInteval = oppoWifiAssistantStateTraker5.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL", Integer.valueOf((int) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL)).intValue();
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker6.mLowTrafficeThreshold = oppoWifiAssistantStateTraker6.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_TRAFFICE_THRESHOLD", 10).intValue();
            OppoWifiAssistantStateTraker.this.mTcpInterval = 5000;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker7 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker7.mWlanInvalidThreshold = oppoWifiAssistantStateTraker7.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", 40).intValue();
            OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
            OppoWifiAssistantStateTraker.this.mCanTriggerData = true;
            OppoWifiAssistantStateTraker.this.mAssistantUtils.resetTriggerData();
            OppoWifiAssistantStateTraker.this.mTriggerInter = false;
            OppoWifiAssistantStateTraker.this.mChangedNetwork = false;
            OppoWifiAssistantStateTraker.this.mClickDialogSwitch = false;
            OppoWifiAssistantStateTraker.this.mDetectInterCount = 0;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker8 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker8.mLastInternetResult = oppoWifiAssistantStateTraker8.mInterResult;
            OppoWifiAssistantStateTraker.this.mLinkDetectTimes = 0;
            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
            OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = false;
            OppoWifiAssistantStateTraker.this.mLastDetectInter = 0;
            OppoWifiAssistantStateTraker.this.mInternetStandoffTime = 0;
            OppoWifiAssistantStateTraker.this.mTrigScanCount = 0;
            OppoWifiAssistantStateTraker.this.mNetQulityGoodCount = 0;
            OppoWifiAssistantStateTraker.this.mLowTrafficeCount = 0;
            OppoWifiAssistantStateTraker.this.mOldTcpStatus = 0;
            for (int lossInt = 0; lossInt < OppoWifiAssistantStateTraker.this.mLossArray.length; lossInt++) {
                OppoWifiAssistantStateTraker.this.mLossArray[lossInt] = OppoWifiAssistantStateTraker.this.mLossInit;
            }
            OppoWifiAssistantStateTraker.this.mIndex = 0;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker9 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker9.mCurrentLoss = new VolumeWeightedEMA(0.5d);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker10 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker10.sendMessage(oppoWifiAssistantStateTraker10.obtainMessage(200708, OppoWifiAssistantStateTraker.access$10804(oppoWifiAssistantStateTraker10), 0));
            OppoWifiAssistantStateTraker.this.removeMessages(200709);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker11 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker11.sendMessageDelayed(oppoWifiAssistantStateTraker11.obtainMessage(200709), RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker12 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker12.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_WLAN, oppoWifiAssistantStateTraker12.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker.this.mLastConfigkey), null, -127);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151573:
                    RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
                    if (info != null) {
                        if (OppoWifiAssistantStateTraker.this.mWifiConnected) {
                            OppoWifiAssistantStateTraker.this.updateRecordLinkQuality(info);
                            OppoWifiAssistantStateTraker.this.mLastPktInfo = info;
                            break;
                        } else {
                            OppoWifiAssistantStateTraker.this.logD("noconnect");
                            break;
                        }
                    }
                    break;
                case 151574:
                    OppoWifiAssistantStateTraker.this.logD("RSSI_FETCH_FAILED");
                    break;
                case 200707:
                    break;
                case 200708:
                    if (msg.arg1 == OppoWifiAssistantStateTraker.this.mRssiFetchToken) {
                        OppoWifiAssistantStateTraker.this.mWsmChannel.sendMessage(151572, 1);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.sendMessageDelayed(oppoWifiAssistantStateTraker.obtainMessage(200708, OppoWifiAssistantStateTraker.access$10804(oppoWifiAssistantStateTraker), 0), (long) OppoWifiAssistantStateTraker.this.mLinkInterval);
                        break;
                    }
                    break;
                case 200709:
                    OppoWifiAssistantStateTraker.this.detectTraffic();
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker2.sendMessageDelayed(oppoWifiAssistantStateTraker2.obtainMessage(200709), (long) OppoWifiAssistantStateTraker.this.mTrafficInteval);
                    break;
                case 200710:
                    OppoWifiAssistantStateTraker.this.detectInternet();
                    if (OppoWifiAssistantStateTraker.this.mChangedToData || !OppoWifiAssistantStateTraker.this.mInterResult) {
                        if (!OppoWifiAssistantStateTraker.this.mAutoSwitch || !OppoWifiAssistantStateTraker.this.mFeatureState || !OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                            OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                        }
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker3.logD("CMD_INTERNET_MONITOR mInterInteval = " + OppoWifiAssistantStateTraker.this.mInterInteval);
                        if (!OppoWifiAssistantStateTraker.this.mScreenOn) {
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker4 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker4.setInternetDetectAlarm(1, (long) oppoWifiAssistantStateTraker4.mInterInteval);
                            break;
                        } else {
                            OppoWifiAssistantStateTraker.this.removeMessages(200710);
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker5 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker5.sendMessageDelayed(oppoWifiAssistantStateTraker5.obtainMessage(200710), (long) OppoWifiAssistantStateTraker.this.mInterInteval);
                            break;
                        }
                    }
                case OppoWifiAssistantStateTraker.EVENT_SHOW_ALERT /* 200719 */:
                    String showConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker6 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker6.logD("shcon:" + showConfig);
                    if (showConfig != null && !OppoWifiAssistantStateTraker.this.hasCheckNoRemind()) {
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker7 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker7.showDialogForData(oppoWifiAssistantStateTraker7.mAssistantUtils.getNetworkRecord(showConfig));
                        break;
                    }
                case OppoWifiAssistantStateTraker.EVENT_DISMISS_ALERT /* 200720 */:
                    String dismissConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker8 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker8.logD("dicon:" + dismissConfig);
                    if (dismissConfig != null) {
                        OppoWifiAssistantStateTraker.this.dismissDialog(1);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_DETECT_ALTERNATIVE /* 200721 */:
                    String detectConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker9 = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker9.logD("decon:" + detectConfig);
                    OppoWifiAssistantStateTraker.this.findAvailableCandidate(OppoWifiAssistantStateTraker.this.mAssistantUtils.getNetworkRecord(detectConfig), 10, -127, false);
                    break;
                case OppoWifiAssistantStateTraker.CMD_SEND_LOW_QUALITY /* 200725 */:
                    OppoWifiAssistantStateTraker.this.mLastTrigDataTime = System.currentTimeMillis();
                    OppoWifiAssistantStateTraker.this.mAssistantUtils.setupDataNetwork();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class VolumeWeightedEMA {
        private double mAlpha;
        private double mProduct = 0.0d;
        private double mValue = 0.0d;
        private double mVolume = 0.0d;

        public VolumeWeightedEMA(double coefficient) {
            this.mAlpha = coefficient;
        }

        public void update(double newValue, int newVolume) {
            if (newVolume > 0) {
                double d = this.mAlpha;
                this.mProduct = (d * ((double) newVolume) * newValue) + ((1.0d - d) * this.mProduct);
                this.mVolume = (((double) newVolume) * d) + ((1.0d - d) * this.mVolume);
                this.mValue = this.mProduct / this.mVolume;
            }
        }
    }

    public void updateWifiState(int state) {
        if (state == -1) {
            transitionTo(this.mInitState);
            logD("updateWifiState return");
            return;
        }
        if (state == 3) {
            transitionTo(this.mDisconnectedState);
        } else if (state == 1) {
            this.mAssistantUtils.saveWifiNetworkRecord();
            sendMessage(200706);
            this.mInitAutoConnect = true;
            this.mIsSoftAP = false;
            this.mCurNetwork = null;
            if (this.mWifiState != state) {
                long disableWlanTime = System.currentTimeMillis();
                long j = this.mAutoConnWlanTime;
                long disableForWlan = disableWlanTime - j;
                if (j > 0 && disableForWlan > 0 && disableForWlan < 180000) {
                    logD("stc wwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_WLAN);
                    this.mAutoConnWlanTime = 0;
                }
                long j2 = this.mAutoDataToWlanTime;
                long disableForData = disableWlanTime - j2;
                if (!this.mChangedToData && j2 > 0 && disableForData > 0 && disableForData < 180000) {
                    logD("stc dwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_DATA);
                    this.mAutoDataToWlanTime = 0;
                }
                if (!this.mChangedToData) {
                    logD("stc wdc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_WIFI_FOR_DATA);
                }
            } else {
                return;
            }
        }
        this.mWifiState = state;
    }

    public void updateNetworkConnectResult(int netid, boolean valid) {
        WifiConfiguration updateConfig;
        logD("urncr mGotInternetResult= " + this.mGotInternetResult + ",valid= " + valid + ",mInterResult= " + this.mInterResult);
        if (this.mWifiConfigManager == null) {
            return;
        }
        if ((this.mIsSoftAP && isThirdAppOperate()) || (updateConfig = this.mWifiConfigManager.getConfiguredNetwork(netid)) == null) {
            return;
        }
        if (this.mGotInternetResult < 4 || valid != this.mInterResult) {
            updateRecordInternetStateAndTime(updateConfig.configKey(false), valid, true);
            this.mInterResult = valid;
            if (valid) {
                this.mGotInternetResult |= 4;
                this.mGotInternetResult &= -9;
                return;
            }
            this.mGotInternetResult |= 8;
            this.mGotInternetResult &= -5;
        }
    }

    public void setNetworkDetailState(int netid, NetworkInfo.DetailedState dst, String bssid) {
        int detailState = -1;
        logD("sds: id=" + netid + ",dst = " + dst);
        if (dst == NetworkInfo.DetailedState.DISCONNECTED) {
            detailState = 0;
            this.mDetectNet = false;
        } else if (dst == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
            detailState = 1;
            this.mDetectNet = true;
        } else if (dst == NetworkInfo.DetailedState.CONNECTED) {
            detailState = 2;
            this.mDetectNet = true;
        }
        if (detailState != -1) {
            sendMessage(obtainMessage(200705, netid, detailState, bssid));
        }
    }

    public void reportRssi() {
    }

    public void handleManualConnect(boolean updateState) {
        ClientModeImpl clientModeImpl;
        if (updateState && (clientModeImpl = this.mWifiStateMachine) != null) {
            clientModeImpl.sendWifiNetworkScore(79, true);
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            this.mAssistantUtils.resetTriggerData();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r25v0, resolved type: com.android.server.wifi.OppoWifiAssistantStateTraker */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0, types: [int, boolean] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setManualConnTime(long time, boolean save, WifiConfiguration config) {
        OppoWifiAssistantRecord curRecord;
        OppoWifiAssistantRecord newRecord;
        if (time > 0) {
            if (getCurrentState() == this.mNetworkMonitorState) {
                curRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
            } else {
                curRecord = null;
            }
            if (config != null) {
                String newConfigkey = config.configKey();
                OppoWifiAssistantRecord newRecord2 = this.mAssistantUtils.getNetworkRecord(newConfigkey);
                if (newRecord2 == null) {
                    OppoWifiAssistantRecord newRecord3 = new OppoWifiAssistantRecord();
                    newRecord3.mConfigkey = newConfigkey;
                    newRecord = newRecord3;
                } else {
                    newRecord = newRecord2;
                }
            } else {
                newRecord = null;
            }
            ?? isOppoManuConnect = getIsOppoManuConnect();
            if (save) {
                long j = this.mAutoConnWlanTime;
                long connSaveForWlan = time - j;
                long connForWlanToData = time - this.mAutoConnDataTime;
                if (j > 0 && connSaveForWlan > 0 && connSaveForWlan < 180000) {
                    logD("stc wwb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_SAVE_FOR_WLAN, curRecord, newRecord, isOppoManuConnect == true ? 1 : 0);
                    this.mAutoConnWlanTime = 0;
                } else if (!this.mChangedNetwork || !this.mChangedToData || this.mAutoConnDataTime <= 0 || connForWlanToData <= 0 || connForWlanToData >= 180000) {
                    logD("stc wwc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_CONN_SAVE_FOR_WLAN, curRecord, newRecord, isOppoManuConnect);
                } else {
                    logD("stc wdb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA, curRecord, newRecord, isOppoManuConnect);
                    this.mAutoConnDataTime = 0;
                }
            }
            long j2 = this.mAutoDataToWlanTime;
            long connForDataToWlan = time - j2;
            if (!this.mChangedToData && j2 > 0 && connForDataToWlan > 0 && connForDataToWlan < 180000) {
                logD("stc dwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA_TO_WLAN, curRecord, newRecord, isOppoManuConnect);
                this.mAutoDataToWlanTime = 0;
            }
        }
    }

    public boolean getIsOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    private int getOppoManuConnectUid() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().getManuConnectUid();
        }
        return 1000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().reset();
        }
    }

    private boolean shouldIgnoreSwitch() {
        String phoneCloneStatus = SystemProperties.get("oppo.service.phone.clone.status", "0");
        String privateDnsStatus = SystemProperties.get("oppo.privatedns_status", "0");
        logD("phone clone status: " + phoneCloneStatus);
        if (getCurrentState() == this.mDisconnectedState) {
            logD("disconnected, do not ignore switch!!");
            return false;
        } else if ("2".equals(phoneCloneStatus) || "3".equals(phoneCloneStatus)) {
            return true;
        } else {
            if (!"1".equals(privateDnsStatus)) {
                return false;
            }
            logD("privateDns status is error,just ignore switch");
            return true;
        }
    }

    public boolean getAutoSwitch() {
        return this.mAutoSwitch;
    }

    public void setAutoSwitch(boolean isAutoSwitch) {
        if (this.mAutoSwitch == isAutoSwitch) {
            logD("autoswitch state is same.");
            return;
        }
        logD("setAutoSwitch: " + isAutoSwitch);
        this.mWifiConfigManager.setWifiAutoSwitch(isAutoSwitch);
        this.mAutoSwitch = isAutoSwitch;
        if (!isAutoSwitch) {
            this.mDataAutoSwitch = false;
        } else {
            this.mDataAutoSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_NETWORK, 1) == 1;
        }
        OppoWifiSmartSwitcher oppoWifiSmartSwitcher = this.mWifiSmartSwitcher;
        if (oppoWifiSmartSwitcher != null) {
            oppoWifiSmartSwitcher.switchOnOff(isAutoSwitch);
        }
        if (this.mWifiStateMachine == null) {
            return;
        }
        if (getCurrentState() != this.mConnectedState && getCurrentState() != this.mVerifyInternetState && getCurrentState() != this.mNetworkMonitorState) {
            return;
        }
        if (!isAutoSwitch) {
            long autoSwitchChangeTime = System.currentTimeMillis();
            long j = this.mAutoConnDataTime;
            long autoSwitchFoData = autoSwitchChangeTime - j;
            if (this.mChangedNetwork && this.mChangedToData && j > 0 && autoSwitchFoData > 0 && autoSwitchFoData < 180000) {
                logD("stc wdb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_SWITCH_FOR_DATA);
                this.mAutoConnDataTime = 0;
            }
            if (this.mChangedNetwork && this.mChangedToData) {
                logD("stc dwc1 0");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_SWITCH_FOR_DATA);
            }
            boolean isGameMode = this.mWifiStateMachine.isGameMode();
            logD("isGameMode =" + isGameMode);
            if (this.mInterResult || !isGameMode) {
                if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                    this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                }
                this.mChangedNetwork = false;
                this.mCanTriggerData = true;
                this.mWifiStateMachine.setNetworkStatus(true);
                sendNetworkStateBroadcast(this.mLastConfigkey, true);
                this.mAssistantUtils.releaseDataNetwork();
            }
        } else if (!this.mIsSoftAP || !isThirdAppOperate()) {
            OppoWifiAssistantRecord lastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
            if (lastRecord != null && !lastRecord.mNetworkValid) {
                sendNetworkStateBroadcast(this.mLastConfigkey, false);
            }
            if (this.mInterResult) {
                return;
            }
            if (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) {
                triggerInternetDetect(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDataAutoSwitch(boolean state) {
        if (this.mWifiStateMachine == null) {
            return;
        }
        if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) && !state) {
            long disableDataSwitchTime = System.currentTimeMillis();
            long j = this.mAutoConnDataTime;
            long disableDataSwitchDistanceTime = disableDataSwitchTime - j;
            if (this.mChangedNetwork && this.mChangedToData && j > 0 && disableDataSwitchDistanceTime > 0 && disableDataSwitchDistanceTime < 180000) {
                logD("stc wdb4");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
                this.mAutoConnDataTime = 0;
            }
            if (this.mChangedNetwork && this.mChangedToData) {
                logD("stc dwc1 1");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
            }
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            if (!this.mWifiStateMachine.isConnected() || this.mInterResult) {
                this.mWifiStateMachine.setNetworkStatus(true);
            } else {
                logD("wifi is connected and mInterResult is false, so do not setNetworkStatus(true)");
            }
            this.mAssistantUtils.releaseDataNetwork();
        }
    }

    public void setFeatureState(boolean state) {
        logD("setFS: " + state);
        this.mFeatureState = state;
        OppoWifiSmartSwitcher oppoWifiSmartSwitcher = this.mWifiSmartSwitcher;
        if (oppoWifiSmartSwitcher != null) {
            oppoWifiSmartSwitcher.featureState(state);
        }
        if (this.mWifiStateMachine != null && !this.mFeatureState && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            this.mAssistantUtils.releaseDataNetwork();
            sendNetworkStateBroadcast(this.mLastConfigkey, true);
        }
        if (!this.mFeatureState) {
            BroadcastReceiver broadcastReceiver = this.mBroadcastReceiver;
            if (broadcastReceiver != null) {
                this.mContext.unregisterReceiver(broadcastReceiver);
            }
            this.mWifiConfigManager.setWifiNetworkAvailable(null);
        } else {
            this.mWifiConfigManager.setWifiNetworkAvailable(this);
        }
        this.mOppoSlaManager.setCellScore(50);
    }

    public void setInterfaceName(String interfaceName) {
        if (interfaceName == null) {
            this.mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
        } else {
            this.mInterfaceName = interfaceName;
        }
    }

    private boolean isRusEnableWifiAssistantFourVersion() {
        return getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FOUR_VERSION_ENABLE", true).booleanValue();
    }

    public void detectScanResult(long time) {
        this.mAssistantUtils.sortNetworkRecords();
        this.mLastScanTime = time;
        this.mTriggerScan = false;
        this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
        WifiInfo currentInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
        if (currentInfo != null) {
            currentInfo.getSSID();
        }
        if (this.mWifiState != 3) {
            logD("wifi is not enable.");
        } else if (this.mIsSoftAP && isThirdAppOperate()) {
        } else {
            if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) && currentInfo != null && ssidHasNetworkRecord(this.mConnectingkey)) {
                logD("current state: " + getCurrentState());
                return;
            }
            findAvailableCandidate(null, 0, -127, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addOrUpdateRecord(String configKey) {
        logD("addOrUpdateRecord: configKey= " + configKey);
        if (configKey != null) {
            OppoWifiAssistantRecord record = this.mAssistantUtils.getNetworkRecord(configKey);
            if (record != null) {
                record.mBssid = this.mLastBssid;
                record.mConnExp = false;
                record.mNetFailCount = 0;
                logD("addOrUpdateRecord: contain and count = " + record.mConnSuccCout);
            } else {
                logD("addOrUpdateRecord: no contain");
                record = new OppoWifiAssistantRecord();
                record.mConfigkey = configKey;
                WifiInfo aourWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
                if (aourWifiInfo == null) {
                    record.mRssi = WifiConfiguration.INVALID_RSSI;
                    record.mBestRssi = WifiConfiguration.INVALID_RSSI;
                    record.mIs5G = false;
                } else {
                    record.mRssi = aourWifiInfo.getRssi();
                    record.mBestRssi = aourWifiInfo.getRssi();
                    record.mIs5G = aourWifiInfo.is5GHz();
                }
            }
            record.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
            this.mAssistantUtils.addNetworkRecord(configKey, record, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void disableNetworkWithoutInternet() {
        List<OppoWifiAssistantRecord> wifiAssistRecord = this.mAssistantUtils.getWifiNetworkRecords();
        if (wifiAssistRecord != null && wifiAssistRecord.size() > 0) {
            for (OppoWifiAssistantRecord wnr : wifiAssistRecord) {
                if (wnr.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", 1).intValue() || wnr.mIsCaptive) {
                    this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                }
            }
        }
    }

    private boolean isDualStaSupportConnectCaptiveAp() {
        return getRomUpdateBooleanValue("OPPO_DUAL_STA_CONNECT_CAPTIVE_AP", true).booleanValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordCaptiveState(String configKey, boolean captive, boolean save) {
        OppoWifiAssistantRecord record;
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            record.mIsCaptive = captive;
            if (record.mIsCaptive && !isDualStaSupportConnectCaptiveAp()) {
                record.mIsCaptive2 = true;
                logD("Dual-Sta does not support captive portal AP!");
            }
            this.mAssistantUtils.addNetworkRecord(configKey, record, true);
            if (save && !getIsOppoManuConnect() && this.mCaptivePortal && (this.mGotInternetResult & 8) != 8) {
                updateRecordInternetStateAndTime(configKey, false, false);
                this.mInterResult = false;
                this.mGotInternetResult |= 8;
                this.mGotInternetResult &= -5;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordDisableState(String configKey) {
        int disableId;
        logD("updateRecordDisableState: " + configKey);
        if (configKey != null) {
            if (!this.mAssistantUtils.getEnabledState()) {
                logD("switch is off,no need to disable with no-internet");
                return;
            }
            OppoWifiAssistantRecord record = this.mAssistantUtils.getNetworkRecord(configKey);
            if (record != null) {
                if (record.mWifiConfiguration != null) {
                    disableId = record.mWifiConfiguration.networkId;
                } else {
                    WifiConfiguration disableConfig = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                    if (disableConfig != null) {
                        disableId = disableConfig.networkId;
                    } else {
                        return;
                    }
                }
                logD("updateRecordDisableState: mNetFailCount=" + record.mNetFailCount + ",mIsCaptive=" + record.mIsCaptive);
                if (disableId != -1) {
                    if (record.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", 1).intValue() || record.mIsCaptive) {
                        ClientModeImpl clientModeImpl = this.mWifiStateMachine;
                        if (clientModeImpl == null || clientModeImpl.isSupplicantAvailable()) {
                            this.mWifiConfigManager.disableNetwork(disableId, 1000, 10);
                        } else {
                            logD("wifi is in disable or disable pending state,cancel disable!!");
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordConCount(String configKey) {
        OppoWifiAssistantRecord record;
        logD("uRcc key:" + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            record.mConnSuccCout++;
            record.mNetid = this.mLastNetId;
            record.mBssid = this.mLastBssid;
            record.mConnExp = false;
            WifiInfo ccWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
            if (ccWifiInfo == null) {
                record.mRssi = WifiConfiguration.INVALID_RSSI;
                record.mBestRssi = WifiConfiguration.INVALID_RSSI;
                record.mIs5G = false;
            } else {
                record.mRssi = ccWifiInfo.getRssi();
                record.mBestRssi = ccWifiInfo.getRssi();
                record.mIs5G = ccWifiInfo.is5GHz();
                logD("updateRecordConCount mRssi:" + record.mRssi);
            }
            record.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
            this.mAssistantUtils.addNetworkRecord(configKey, record, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordConnectFail(String configKey) {
        OppoWifiAssistantRecord record;
        logD("uRcf key:" + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            logD("updateRecordConnectFail failCount = " + record.mConnFailCount);
            record.mConnFailCount = record.mConnFailCount + 1;
            record.mBssid = this.mLastBssid;
            this.mAssistantUtils.addNetworkRecord(configKey, record, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordUseTime(String configKey) {
        OppoWifiAssistantRecord record;
        logD("updateRecordUseTime key= " + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            long time = System.currentTimeMillis();
            long useTime = record.mAccessNetTime != 0 ? time - record.mAccessNetTime : 0;
            logD("updateRecordUseTime record= " + record.mConfigkey + ", mAccessNetTime = " + record.mAccessNetTime + ", mInternetTime = " + record.mInternetTime + ", useTime = " + useTime);
            record.mInternetTime = useTime;
            record.mLastuseTime = time;
            record.mAccessNetTime = 0;
            if (this.mWifiConfigManager.getConfiguredNetwork(configKey) == null) {
                record.mWifiConfiguration = null;
            }
            this.mAssistantUtils.addNetworkRecord(configKey, record, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecordInternetStateAndTime(String configKey, boolean valid, boolean transiTo) {
        logD("updateRecordInternetStateAndTime key= " + configKey + ", valid = " + valid);
        if (configKey == null) {
            return;
        }
        if (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) {
            this.mLastConfigkey = configKey;
            this.mAccessNetTime = valid ? System.currentTimeMillis() : 0;
            OppoWifiAssistantRecord record = this.mAssistantUtils.getNetworkRecord(configKey);
            if (record != null) {
                logD("updateRecordInternetStateAndTime mAccessNetTime= " + record.mAccessNetTime + ", time = " + this.mAccessNetTime);
                record.mAccessNetTime = this.mAccessNetTime;
                record.mNetworkValid = valid;
            } else {
                record = new OppoWifiAssistantRecord();
                record.mConfigkey = configKey;
                record.mBssid = this.mLastBssid;
                record.mAccessNetTime = this.mAccessNetTime;
                record.mNetworkValid = valid;
                record.mConnSuccCout++;
            }
            if (valid) {
                record.mNetworkValid2 = valid;
            }
            record.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
            WifiInfo uritWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
            if (uritWifiInfo != null) {
                record.mRssi = uritWifiInfo.getRssi();
                record.mIs5G = uritWifiInfo.is5GHz();
            }
            int index = getQualityIndex(record.mIs5G, record.mRssi);
            int curScore = 79;
            if (index == 0) {
                curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", 30).intValue();
            } else if (index == 1) {
                curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", 20).intValue();
            } else if (index == 2) {
                curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD", 5).intValue();
            }
            if (valid) {
                int i = 0;
                while (true) {
                    int[] iArr = this.mNetQualityArray;
                    if (i >= iArr.length) {
                        break;
                    }
                    iArr[i] = curScore;
                    i++;
                }
                int tcpIndex = 0;
                while (true) {
                    int[] iArr2 = this.mTcpstateArray;
                    if (tcpIndex >= iArr2.length) {
                        break;
                    }
                    iArr2[tcpIndex] = 16;
                    tcpIndex++;
                }
                record.mNetQualitys[index] = curScore;
                record.mNetFailCount = 0;
                record.mIsCaptive = false;
                if (isDualStaSupportConnectCaptiveAp()) {
                    record.mIsCaptive2 = false;
                }
            } else {
                int i2 = 0;
                while (true) {
                    int[] iArr3 = this.mNetQualityArray;
                    if (i2 >= iArr3.length) {
                        break;
                    }
                    iArr3[i2] = 10;
                    i2++;
                }
                int tcpIndex2 = 0;
                while (true) {
                    int[] iArr4 = this.mTcpstateArray;
                    if (tcpIndex2 >= iArr4.length) {
                        break;
                    }
                    iArr4[tcpIndex2] = 0;
                    tcpIndex2++;
                }
                record.mNetQualitys[index] = 10;
                if (transiTo) {
                    record.mNetFailCount++;
                }
            }
            this.mAssistantUtils.addNetworkRecord(configKey, record, true);
            this.mAssistantUtils.sortNetworkRecords();
            if (transiTo) {
                sendMessage(200707);
            }
            if (this.mAssistantUtils.getEnabledState()) {
                sendNetworkStateBroadcast(configKey, valid);
            }
        }
    }

    private void clearRecordWifiConfig(String configKey) {
        OppoWifiAssistantRecord record;
        logD("clearRecordWifiConfig key:" + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            if (record.mWifiConfiguration != null) {
                logD("clearRecordWifiConfig record.config= " + record.mWifiConfiguration.SSID);
                record.mWifiConfiguration = null;
            }
            record.mNetworkValid = false;
            this.mAssistantUtils.addNetworkRecord(configKey, record, false);
        }
    }

    /* JADX INFO: Multiple debug info for r5v27 int: [D('i' int), D('currentQuality' int)] */
    /* JADX INFO: Multiple debug info for r13v51 'now'  long: [D('now' long), D('dtotal' int)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x0904, code lost:
        if (r7 > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_24", -75).intValue()) goto L_0x0906;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x012b  */
    private synchronized void updateRecordLinkQuality(RssiPacketCountInfo info) {
        boolean willRoam;
        OppoWifiAssistantRecord mCandidate;
        int newTcpStatus;
        int txbad;
        int dbad;
        int validLinklossNum;
        double loss;
        long now;
        int slaScore;
        int dtotal;
        int roamDetectCount;
        int rtt_1;
        int speedDecrease;
        int rttSum;
        int rtt_2;
        int rtt_12;
        int speed_2;
        int speed_1;
        int avgRtt;
        OppoWifiAssistantRecord mLastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
        if (mLastRecord != null && info != null) {
            if (info.rssi > WifiConfiguration.INVALID_RSSI) {
                int score = 79;
                int rssi = info.rssi;
                int txbad2 = info.txbad;
                int txgood = info.txgood;
                mLastRecord.mRssi = rssi;
                if (rssi > mLastRecord.mBestRssi) {
                    mLastRecord.mBestRssi = rssi;
                }
                int count = this.mLinkInterval / 1000;
                int[] rttAndSpeed = this.mOppoSlaManager.getRttAndSpeed(count);
                if (this.mOppoSlaManager.isGameInFront() || rttAndSpeed == null || rttAndSpeed.length != count * 6) {
                    mCandidate = null;
                    willRoam = false;
                } else {
                    int rttSum2 = 0;
                    int speedSum = 0;
                    for (int i = 0; i < count; i++) {
                        speedSum += rttAndSpeed[(i * 6) + (this.mWlanIfIndex * 2)];
                        rttSum2 += rttAndSpeed[(i * 6) + (this.mWlanIfIndex * 2) + 1];
                    }
                    int avgRtt2 = rttSum2 / count;
                    int avgSpeed = speedSum / count;
                    int speed_score_1 = 5;
                    int speed_score_2 = 10;
                    int rtt_score_1 = 15;
                    int rtt_score_2 = 10;
                    int rtt_score_3 = 5;
                    mCandidate = null;
                    if (this.mWifiRomUpdateHelper != null) {
                        int[] params = this.mWifiRomUpdateHelper.getSpeedRttParams();
                        if (params != null) {
                            willRoam = false;
                            if (params.length == 11) {
                                int speed_12 = params[0];
                                int speed_22 = params[1];
                                int rtt_13 = params[2];
                                int rtt_22 = params[3];
                                int rtt_3 = params[4];
                                speed_score_1 = params[5];
                                speed_score_2 = params[6];
                                rtt_score_1 = params[7];
                                rtt_score_2 = params[8];
                                rtt_score_3 = params[9];
                                int cellDecreaseMax = params[10];
                                speed_1 = speed_12;
                                speed_2 = speed_22;
                                rtt_12 = rtt_13;
                                rtt_1 = 0;
                                rtt_2 = rtt_22;
                                rttSum = rtt_3;
                                speedDecrease = cellDecreaseMax;
                                if (avgSpeed >= speed_1 && avgSpeed >= speed_2) {
                                    rtt_1 = speed_score_1;
                                } else if (avgSpeed < speed_2) {
                                    rtt_1 = speed_score_2;
                                }
                                if (avgRtt2 < rtt_12) {
                                    avgRtt = rtt_score_1 + rtt_1;
                                } else if (avgRtt2 >= rtt_2 && avgRtt2 < rtt_12) {
                                    avgRtt = rtt_score_2 + rtt_1;
                                } else if (avgRtt2 < rttSum || avgRtt2 >= rtt_2) {
                                    avgRtt = 0;
                                } else {
                                    avgRtt = rtt_score_3 + rtt_1;
                                }
                                if (this.mChangedToData || avgRtt <= speedDecrease) {
                                    score = 79 - avgRtt;
                                } else {
                                    score = 79 - speedDecrease;
                                }
                                logD("speed and rtt decreased " + (79 - score) + " points.");
                            }
                        } else {
                            willRoam = false;
                        }
                    } else {
                        willRoam = false;
                    }
                    speed_1 = 150;
                    speed_2 = 100;
                    rtt_12 = 250;
                    rtt_1 = 0;
                    rtt_2 = 200;
                    rttSum = 150;
                    speedDecrease = 5;
                    if (avgSpeed >= speed_1) {
                    }
                    if (avgSpeed < speed_2) {
                    }
                    if (avgRtt2 < rtt_12) {
                    }
                    if (this.mChangedToData) {
                    }
                    score = 79 - avgRtt;
                    logD("speed and rtt decreased " + (79 - score) + " points.");
                }
                int netQualityIndex = getQualityIndex(mLastRecord.mIs5G, rssi);
                if (netQualityIndex == 0) {
                    int badRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", 30).intValue();
                    if (this.mChangedToData) {
                        score -= badRssiScore + 5;
                    } else {
                        score -= badRssiScore;
                    }
                } else if (netQualityIndex == 1) {
                    int lowRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", 20).intValue();
                    if (this.mChangedToData) {
                        score -= lowRssiScore + 5;
                    } else {
                        score -= lowRssiScore;
                    }
                } else if (netQualityIndex == 2) {
                    score -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD", 5).intValue();
                }
                int dbad2 = txbad2 - this.mLastTxBad;
                int dgood = txgood - this.mLastTxGood;
                int dtotal2 = dbad2 + dgood;
                this.mLastTxBad = txbad2;
                this.mLastTxGood = txgood;
                if (this.mIsFirstPktCntFetchSucceed) {
                    dbad2 = 0;
                    dgood = 0;
                    dtotal2 = 0;
                    this.mIsFirstPktCntFetchSucceed = false;
                }
                boolean isEnableWifiAssistantFourVersion = isRusEnableWifiAssistantFourVersion();
                if (isEnableWifiAssistantFourVersion) {
                    if (mLastRecord.mHistoryRecordQueue == null) {
                        newTcpStatus = 0;
                        mLastRecord.mHistoryRecordQueue = new OppoHistoryRecordQueue(this.mContext, mLastRecord.mBssid, mLastRecord.mIs5G);
                    } else {
                        newTcpStatus = 0;
                    }
                    if (!this.mOppoSlaManager.isNotUpdateRecordBySla()) {
                        mLastRecord.mHistoryRecordQueue.recordTxWithRssi(rssi, dgood, dbad2);
                    }
                    int historyRecordTriggerThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_HISTORY_RECORD_TRIGGER_THRESHOLD", 10).intValue();
                    int historyRecordValidThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_HISTORY_RECORD_VALID_THRESHOLD", 10).intValue();
                    mLastRecord.mIsHistoryLoss = false;
                    if (dtotal2 < historyRecordTriggerThreshold && mLastRecord.mHistoryRecordQueue.getTxTotalWithRssi(rssi) > historyRecordValidThreshold) {
                        dbad2 = mLastRecord.mHistoryRecordQueue.getTxBadWithRssi(rssi);
                        dtotal2 = mLastRecord.mHistoryRecordQueue.getTxTotalWithRssi(rssi);
                        dgood = dtotal2 - dbad2;
                        mLastRecord.mIsHistoryLoss = true;
                    }
                } else {
                    newTcpStatus = 0;
                }
                DecimalFormat df = new DecimalFormat("#.##");
                double badLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD", Double.valueOf((double) STATIC_BAD_LINK_LOSS_THRESHOLD)).doubleValue();
                double lowLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD", Double.valueOf(0.5d)).doubleValue();
                double goodLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD", Double.valueOf((double) STATIC_GOOD_LINK_LOSS_THRESHOLD)).doubleValue();
                int noTrafficScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD", 5).intValue();
                int validLinklossNum2 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_VALID_LINK_LOSS_NUM", 0).intValue();
                if (dtotal2 > validLinklossNum2) {
                    validLinklossNum = validLinklossNum2;
                    dbad = dbad2;
                    txbad = txbad2;
                    loss = ((double) dbad2) / ((double) dtotal2);
                    if ((!isEnableWifiAssistantFourVersion || loss >= goodLinkLossThreshold) && (isEnableWifiAssistantFourVersion || loss != 0.0d)) {
                        this.mLinkDetectTimes = 0;
                        if (loss >= badLinkLossThreshold) {
                            int badLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD", 35).intValue();
                            if (this.mChangedToData) {
                                score -= badLinkScoreThreshold + 5;
                            } else {
                                score -= badLinkScoreThreshold;
                            }
                        } else if (loss >= lowLinkLossThreshold && loss < badLinkLossThreshold) {
                            int lowLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD", 30).intValue();
                            if (this.mChangedToData) {
                                score -= lowLinkScoreThreshold + 5;
                            } else {
                                score -= lowLinkScoreThreshold;
                            }
                        } else if (loss >= goodLinkLossThreshold && loss < lowLinkLossThreshold) {
                            score -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD", 20).intValue();
                        }
                    } else {
                        this.mLinkDetectTimes++;
                    }
                } else {
                    txbad = txbad2;
                    validLinklossNum = validLinklossNum2;
                    dbad = dbad2;
                    this.mLinkDetectTimes = 0;
                    if (this.mChangedToData) {
                        score -= noTrafficScore + 5;
                        loss = 0.0d;
                    } else {
                        score -= noTrafficScore;
                        loss = 0.0d;
                    }
                }
                this.mLossArray[this.mIndex] = loss;
                int badLossCount = 0;
                for (int lossIndex = 0; lossIndex < this.mLossArray.length; lossIndex++) {
                    if (this.mLossArray[lossIndex] >= lowLinkLossThreshold) {
                        badLossCount++;
                    }
                }
                if (badLossCount == this.mLossArray.length) {
                    score -= noTrafficScore * 3;
                } else if (badLossCount == this.mLossArray.length - 1 && loss >= lowLinkLossThreshold) {
                    score -= noTrafficScore * 2;
                } else if (badLossCount == this.mLossArray.length - 2 && loss >= lowLinkLossThreshold) {
                    score -= noTrafficScore;
                }
                if (!mLastRecord.mNetworkValid) {
                    score = 10;
                }
                if (score < 10) {
                    score = 10;
                }
                for (int i2 = netQualityIndex; i2 < 4; i2++) {
                    if (score > mLastRecord.mNetQualitys[i2]) {
                        mLastRecord.mNetQualitys[i2] = score;
                        mLastRecord.mScore = score;
                    }
                }
                logD("updateRecordLinkQuality key:" + this.mLastConfigkey + ",rssi=" + rssi + " txbad=" + txbad + " txgood=" + txgood + ",dbad = " + dbad + ", dgood = " + dgood + ",dtotal= " + dtotal2 + ",badLossCount=" + badLossCount + ",loss=" + df.format(loss * 100.0d) + ",mLinkDetectTimes= " + this.mLinkDetectTimes + ",newTcpStatus:" + newTcpStatus + ",mIndex =" + this.mIndex + ",score= " + score + ",hls=" + mLastRecord.mIsHistoryLoss + ",mInterResult=" + this.mInterResult + ",mChangeScore=" + this.mChangeScore + ",mNetQulityGoodCount=" + this.mNetQulityGoodCount);
                this.mNetQualityArray[this.mIndex] = score;
                this.mIndex = this.mIndex + 1;
                this.mIndex = this.mIndex % 3;
                int sumQuality = 0;
                int i3 = 0;
                for (int i4 = 3; i3 < i4; i4 = 3) {
                    sumQuality += this.mNetQualityArray[i3];
                    i3++;
                }
                int currentQuality = sumQuality / 3;
                int comparedScore = currentQuality > score ? currentQuality : score;
                logD("currentQuality=" + currentQuality + ",comparedScore=" + comparedScore + ",mWlanInvalidThreshold=" + this.mWlanInvalidThreshold + ",mChangedNetwork=" + this.mChangedNetwork + ",mCanTriggerData=" + this.mCanTriggerData + ",mChangedToData=" + this.mChangedToData);
                broadcastInfotoTestapk("ssid:" + mLastRecord.mConfigkey + ",net:" + mLastRecord.mNetworkValid + ",currScore:" + score + ",score:" + currentQuality + ", rssi:" + rssi + ",loss:" + df.format(loss * 100.0d) + ",total:" + dtotal2 + ",tcp:" + newTcpStatus);
                int slaScore2 = currentQuality;
                if (rssi <= -80 && !this.mOppoSlaManager.isGameInFront()) {
                    slaScore2 = 10;
                }
                this.mOppoSlaManager.sendWifiScoreToKernel(this.mWlanIfIndex, slaScore2);
                this.mAssistantUtils.setWifiScore(this.mInterfaceName, currentQuality);
                if (!this.mAssistantUtils.getEnabledState()) {
                    logD("smart switch disable");
                    return;
                }
                if (currentQuality <= 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD", 15).intValue() || this.mChangedNetwork) {
                    this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SAMPL_INTERVAL", 3000).intValue();
                } else {
                    this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", 5000).intValue();
                }
                long now2 = System.currentTimeMillis();
                if (currentQuality > 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD", 20).intValue() || !this.mCanTriggerData) {
                    dtotal = dtotal2;
                    slaScore = slaScore2;
                    now = now2;
                } else {
                    if (!getIsOppoManuConnect() || !this.mIsSoftAP) {
                        dtotal = dtotal2;
                        slaScore = slaScore2;
                        now = now2;
                    } else {
                        dtotal = dtotal2;
                        slaScore = slaScore2;
                        now = now2;
                        if (now - this.mLastSwitchedToWifiTime < 6000) {
                            sendMessageDelayed(CMD_SEND_LOW_QUALITY, currentQuality, (this.mLastSwitchedToWifiTime + 6000) - now);
                            this.mCanTriggerData = false;
                        }
                    }
                    this.mLastTrigDataTime = now;
                    this.mAssistantUtils.setupDataNetwork();
                    this.mCanTriggerData = false;
                }
                int trigWlanSetectScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD", 25).intValue();
                int roamDetectCount2 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_ROAM_DETECT", 6).intValue();
                if (currentQuality <= 79 - trigWlanSetectScore) {
                    if (score <= 79 - trigWlanSetectScore && this.mLastScanTime != 0 && now - this.mLastScanTime > STAIC_SCAN_RESULT_AGE && this.mTrigScanCount < 3 && !this.mTriggerScan) {
                        if (this.mWifiStateMachine != null && this.mScanRequestProxy != null) {
                            this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                            this.mTrigScanCount++;
                            this.mTriggerScan = true;
                            this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SCAN_TIMEOUT), 6000);
                        }
                    }
                    if (!this.mTriggerScan) {
                        if (mLastRecord.mNetworkValid) {
                            willRoam = detectMaybeRoam(mLastRecord, comparedScore);
                        }
                        if (willRoam) {
                            roamDetectCount = roamDetectCount2;
                            if (this.mRoamdetectCount < roamDetectCount) {
                                this.mRoamdetectCount++;
                                if (!this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                                    mCandidate = findAvailableCandidate(mLastRecord, comparedScore, mLastRecord.mRssi, true);
                                }
                            }
                        } else {
                            roamDetectCount = roamDetectCount2;
                        }
                        if (score <= 79 - trigWlanSetectScore) {
                            if (this.mAssistantUtils.getWifiScore(OppoWifiAssistantUtils.IFACE_NAME_WLAN1) - currentQuality >= 10 && !this.mOppoSlaManager.isDualStaAppOnFocus() && !this.mOppoSlaManager.isSystemUidAppOnFocus()) {
                                logD("disableDualSta() when wlan0 is poor and wlan1 is good...");
                                this.mAssistantUtils.disableDualSta();
                            }
                            mCandidate = findAvailableCandidate(mLastRecord, comparedScore, mLastRecord.mRssi, false);
                        }
                    } else {
                        roamDetectCount = roamDetectCount2;
                    }
                } else {
                    roamDetectCount = roamDetectCount2;
                }
                if (mAlertDialog != null && getIsOppoManuConnect() && !this.mChangedToData && mLastRecord.mNetworkValid) {
                    if (currentQuality > 79 - trigWlanSetectScore) {
                        this.mNetQulityGoodCount++;
                        if (this.mNetQulityGoodCount > roamDetectCount) {
                            dismissDialog(2);
                            this.mNetQulityGoodCount = 0;
                        }
                    } else {
                        this.mNetQulityGoodCount = 0;
                    }
                }
                if (!this.mChangedToData && this.mChangedNetwork && currentQuality > 50) {
                    changeNetworkToWlan(mLastRecord, currentQuality, false, false);
                }
                if (this.mWlanInvalidThreshold == 70 && currentQuality <= 39 && this.mAutoSwitchDataDisableTime > 0) {
                    long napTime = System.currentTimeMillis() - this.mAutoSwitchDataDisableTime;
                    logD("nt= " + napTime);
                    if (napTime > getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME", Long.valueOf((long) AUTO_SWITCH_DATA_DISBALE_TIME)).longValue()) {
                        resetAutoSwitchDataDetect();
                    }
                }
                int wlanBadThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD", Integer.valueOf(this.mWlanInvalidThreshold)).intValue();
                this.mAssistantUtils.setCanChangeToCell(this.mInterfaceName, false);
                if (this.mChangedNetwork || currentQuality > 79 - wlanBadThreshold || score > 79 - wlanBadThreshold) {
                    if (this.mChangedNetwork && this.mChangedToData) {
                        int goodLinkCountThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT", 3).intValue();
                        boolean canEnableSla = this.mOppoSlaManager.getSlaEnableStateByWlanAssistant();
                        logD("SlaToWlan:cSla=" + canEnableSla);
                        if (mLastRecord.mNetworkValid && !this.mLastInternetResult && this.mChangeRssi != 0 && rssi >= -72 && currentQuality >= 50 && score >= 50 && this.mChangeScore == 10) {
                            changeNetworkToWlan(mLastRecord, score, true, false);
                        } else if ((((!isEnableWifiAssistantFourVersion || canEnableSla) && isEnableWifiAssistantFourVersion) || rssi <= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD", Integer.valueOf((int) STATIC_RSSI_TO_WLAN_THRESHOLD)).intValue() || this.mLinkDetectTimes < goodLinkCountThreshold) && (rssi < -65 || this.mLinkDetectTimes < goodLinkCountThreshold - 2)) {
                            if (isEnableWifiAssistantFourVersion && canEnableSla) {
                                if (!mLastRecord.mIs5G || rssi < getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_5", -72).intValue()) {
                                    if (!mLastRecord.mIs5G) {
                                    }
                                }
                                if (this.mLinkDetectTimes >= goodLinkCountThreshold - 2) {
                                    if (this.mChangedNetwork && mLastRecord.mNetworkValid) {
                                        if (currentQuality >= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_SCORE_POOR", 54).intValue() && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                            logD("SlaOnReToWlan:cQ=" + currentQuality + ",rssi=" + rssi + ",mLinkDetectTimes=" + this.mLinkDetectTimes);
                                            this.mOppoSlaManager.enableSlaByWlanAssistant();
                                            Settings.System.putInt(this.mContext.getContentResolver(), KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT, 1);
                                            changeNetworkToWlan(mLastRecord, currentQuality, true, true);
                                        }
                                    }
                                }
                            }
                            if (rssi >= -65 && this.mTriggerInter && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                logD("dt inter for strong rssi");
                                this.mTriggerInter = false;
                                setInternetDetectAlarm(0, 0);
                            }
                        } else if (this.mChangedNetwork && mLastRecord.mNetworkValid) {
                            if (currentQuality >= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD", 64).intValue() && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                logD("SlaOffReToWlan:cQ=" + currentQuality + ",rssi=" + rssi + ",mLinkDetectTimes=" + this.mLinkDetectTimes);
                                changeNetworkToWlan(mLastRecord, currentQuality, true, false);
                            }
                        }
                    }
                } else if ((!willRoam || (willRoam && this.mRoamdetectCount >= roamDetectCount)) && mCandidate == null && !this.mTriggerScan) {
                    if (!mLastRecord.mNetworkValid) {
                        currentQuality = 10;
                    } else if (currentQuality < 20) {
                        currentQuality = 20;
                    }
                    if (!getIsOppoManuConnect() && this.mDataAutoSwitch && !shouldIgnoreSwitch()) {
                        this.mAssistantUtils.setCanChangeToCell(this.mInterfaceName, true);
                        if (this.mAssistantUtils.getOtherIfCanChangeToCell(this.mInterfaceName)) {
                            this.mChangedNetwork = true;
                            this.mTriggerInter = true;
                            this.mChangeRssi = rssi;
                            this.mChangeScore = currentQuality;
                            this.mWifiStateMachine.sendWifiNetworkScore(currentQuality, false);
                        }
                    }
                }
                this.mAssistantUtils.addNetworkRecord(this.mLastConfigkey, mLastRecord, false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeNetworkToWlan(OppoWifiAssistantRecord record, int quality, boolean dataToWlan, boolean keep) {
        if (this.mWifiStateMachine != null) {
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine.sendWifiNetworkScore(quality, true);
            }
            this.mChangedNetwork = false;
            this.mTriggerInter = false;
            this.mRoamdetectCount = 0;
            this.mChangeRssi = 0;
            this.mChangeScore = 0;
            this.mTrigScanCount = 0;
            boolean isEnableWifiAssistantFourVersion = isRusEnableWifiAssistantFourVersion();
            if (!keep || !isEnableWifiAssistantFourVersion) {
                this.mAssistantUtils.releaseDataNetwork();
            }
            this.mCanTriggerData = true;
            dismissDialog(1);
            if (dataToWlan) {
                this.mAutoDataToWlanTime = System.currentTimeMillis();
                this.mAutoConnWlanTime = 0;
                this.mAutoConnDataTime = 0;
                logD("stc dwa");
                setAssistantStatistics(STATISTIC_AUTO_CONN, TYPE_DATA_TO_WLAN, record, null, quality);
            }
        }
    }

    private void broadcastInfotoTestapk(String info) {
        Handler handler;
        this.mBroadInfo = info;
        if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_TEST", false).booleanValue() && (handler = this.mBroadHandle) != null) {
            handler.post(new Runnable() {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass4 */

                public void run() {
                    Intent broadIntent = new Intent(OppoWifiAssistantStateTraker.BRAOD_WIFI_INFO);
                    Log.d("testFeature", "mBroadInfo = " + OppoWifiAssistantStateTraker.this.mBroadInfo);
                    broadIntent.putExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_NETINFO, OppoWifiAssistantStateTraker.this.mBroadInfo);
                    OppoWifiAssistantStateTraker.this.mContext.sendStickyBroadcastAsUser(broadIntent, UserHandle.ALL);
                }
            });
        }
    }

    public void updateWifiNetworkConfig(String name, String value) {
        if (value != null && name != null) {
            char c = 65535;
            if (name.hashCode() == 1322731883 && name.equals("SmartWlanFeature")) {
                c = 0;
            }
            if (c != 0) {
                logD("default/");
            } else if (value.equals("false")) {
                setFeatureState(false);
            } else {
                setFeatureState(true);
            }
        }
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getFloatValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getLongValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    /* access modifiers changed from: package-private */
    public void readWifiNetworkRecord() {
        this.mAssistantUtils.readWifiNetworkRecord();
    }

    private void pintRecord() {
        Iterator<OppoWifiAssistantRecord> it = getWifiNetworkRecords().iterator();
        while (it.hasNext()) {
            logD("pintRecord wnr: " + it.next().toString());
        }
    }

    public List<OppoWifiAssistantRecord> getWifiNetworkRecords() {
        return this.mAssistantUtils.getWifiNetworkRecords();
    }

    private int getQualityIndex(boolean is5G, int rssi) {
        if (is5G) {
            int goodRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_5", -65).intValue();
            int lowRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_5", -72).intValue();
            int badRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_5", -80).intValue();
            if (rssi >= goodRssi5) {
                return 3;
            }
            if (rssi >= lowRssi5 && rssi < goodRssi5) {
                return 2;
            }
            if (rssi <= badRssi5 || rssi >= lowRssi5) {
                return 0;
            }
            return 1;
        }
        int goodRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_24", -65).intValue();
        int lowRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_24", -75).intValue();
        int badRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_24", -83).intValue();
        if (rssi >= goodRssi24) {
            return 3;
        }
        if (rssi >= lowRssi24 && rssi < goodRssi24) {
            return 2;
        }
        if (rssi <= badRssi24 || rssi >= lowRssi24) {
            return 0;
        }
        return 1;
    }

    private void selectCandidateNetwork(OppoWifiAssistantRecord lastRecord) {
        WifiConfiguration selectConf = this.mWifiConfigManager.getConfiguredNetwork(lastRecord.mConfigkey);
        if (selectConf == null) {
            logD("select config is null");
            return;
        }
        logD("candidate network: " + selectConf.SSID);
        ClientModeImpl clientModeImpl = this.mWifiStateMachine;
        if (clientModeImpl == null || clientModeImpl.isSupplicantAvailable()) {
            ClientModeImpl clientModeImpl2 = this.mWifiStateMachine;
            if (clientModeImpl2 != null) {
                if (clientModeImpl2.isNetworkAutoConnectingOrConnected(selectConf.networkId)) {
                    logD("network: " + selectConf.SSID + " is connecting or connected, do nothing!");
                    return;
                }
                this.mWifiStateMachine.setTargetNetworkId(lastRecord.mNetid);
                this.mWifiStateMachine.clearTargetBssid(TAG);
                this.mWifiStateMachine.prepareForForcedConnection(selectConf.networkId);
                if (this.mWifiConnected) {
                    this.mWifiStateMachine.notifyDisconnectPktName(null, -1, 5);
                }
                this.mWifiStateMachine.startConnectToNetwork(selectConf.networkId, 1000, "any");
            }
            SupplicantStateTracker supplicantStateTracker = this.mSupplicantTracker;
            if (supplicantStateTracker != null) {
                supplicantStateTracker.sendMessage(131372);
                return;
            }
            return;
        }
        logD("wifi is in disable or disable pending state,cancel reconnect!!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiConfiguration getWifiConfig(String ssid, String bssid) {
        if (ssid == null || bssid == null) {
            return null;
        }
        WifiConfiguration connectedConfig = this.mWifiStateMachine.getCurrentWifiConfiguration();
        String connectedKey = "null";
        if (connectedConfig == null) {
            logD("ccf is null");
        } else {
            connectedKey = connectedConfig.configKey();
        }
        String currentKey = connectedKey;
        Iterator<ScanResult> it = this.mScanRequestProxy.syncGetScanResultsList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult result = it.next();
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String str = result.capabilities;
            if (scanSsid.equals(ssid)) {
                if (scanBssid.equals(bssid)) {
                    currentKey = WifiConfiguration.configKey(result);
                    break;
                }
                String scanKey = WifiConfiguration.configKey(result);
                if (connectedKey != null && connectedKey.equals(scanKey)) {
                    currentKey = scanKey;
                }
            }
        }
        logD("gc cf= " + currentKey);
        WifiConfiguration currentconfig = this.mWifiConfigManager.getConfiguredNetwork(currentKey);
        if (currentconfig != null) {
            logD("gc currentconfig: " + currentconfig.networkId + ",SSID:" + currentconfig.SSID + ",BSSID:" + currentconfig.BSSID);
            if (currentconfig.BSSID == null || currentconfig.BSSID.equals("any")) {
                currentconfig.BSSID = bssid;
            }
        }
        return currentconfig;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detectSwitchDataFrequence() {
        if (this.mWifiStateMachine != null) {
            this.mAutoSwitchDataCount++;
            long curTime = System.currentTimeMillis();
            int thresholdCount = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_COUNT", 5).intValue();
            long thresholdTime = getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_TIME", Long.valueOf((long) AUTO_SWITCH_DATA_THRESHOLD_TIME)).longValue();
            long[] jArr = this.mAutoSwitchDataTimes;
            int i = this.mAutoSwitchDataIndex;
            jArr[i] = curTime;
            int i2 = this.mAutoSwitchDataCount;
            if (i2 >= thresholdCount) {
                this.mAutoSwitchDataCount = i2 - 1;
                int oldestIndex = (i + 1) % 5;
                if (jArr[oldestIndex] > 0 && curTime - jArr[oldestIndex] > 0 && curTime - jArr[oldestIndex] <= thresholdTime) {
                    this.mWlanInvalidThreshold = 70;
                    this.mAutoSwitchDataDisableTime = curTime;
                    logD("dsdf time= " + curTime);
                }
            }
            this.mAutoSwitchDataIndex++;
            this.mAutoSwitchDataIndex %= 5;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetAutoSwitchDataDetect() {
        if (this.mWifiStateMachine != null) {
            int mAsdtIndex = 0;
            while (true) {
                long[] jArr = this.mAutoSwitchDataTimes;
                if (mAsdtIndex < jArr.length) {
                    jArr[mAsdtIndex] = 0;
                    mAsdtIndex++;
                } else {
                    this.mAutoSwitchDataDisableTime = 0;
                    this.mAutoSwitchDataCount = 0;
                    this.mAutoSwitchDataIndex = 0;
                    this.mWlanInvalidThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", 40).intValue();
                    return;
                }
            }
        }
    }

    private boolean ssidHasNetworkRecord(String key) {
        boolean contain = false;
        if (key == null) {
            return false;
        }
        Iterator<OppoWifiAssistantRecord> it = this.mAssistantUtils.getSortNetworkRecords().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().mConfigkey.equals(key)) {
                    contain = true;
                    break;
                }
            } else {
                break;
            }
        }
        logD("ssidHasNetworkRecord: return = " + contain);
        return contain;
    }

    private boolean detectMaybeRoam(OppoWifiAssistantRecord lastRecord, int score) {
        boolean maybeRoam = false;
        List<ScanResult> roamScan = this.mScanRequestProxy.syncGetScanResultsList();
        if (lastRecord == null) {
            logD("detectMaybeRoam lastRecord is null!");
            return false;
        }
        WifiInfo curWifiInfo = this.mWifiStateMachine.getWifiInfo();
        int curFreq = 2412;
        if (curWifiInfo != null) {
            curFreq = curWifiInfo.getFrequency();
        }
        Iterator<ScanResult> it = roamScan.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult result = it.next();
            String str = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String scanConfKey = WifiConfiguration.configKey(result);
            int scanRssi = result.level;
            int scanFreq = result.frequency;
            if (scanConfKey.equals(lastRecord.mConfigkey) && scanRssi > -83 && lastRecord.mBssid != null && !scanBssid.equals(lastRecord.mBssid)) {
                if (curFreq != scanFreq || (curFreq == scanFreq && scanRssi - lastRecord.mRssi > 5)) {
                    maybeRoam = true;
                }
            }
        }
        maybeRoam = true;
        logD("detectMaybeRoam maybeRoam=" + maybeRoam);
        return maybeRoam;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x013a  */
    private void detectTraffic() {
        long rxPkts;
        long txPkts;
        long txPkts2;
        boolean z;
        int i;
        long lastDetlaRxPkts = this.mdRxPkts;
        if (this.mInterfaceName != null) {
            txPkts = this.mOppoSlaManager.getWlanTcpTxPackets(0);
            rxPkts = this.mOppoSlaManager.getWlanTcpRxPackets(0);
        } else {
            txPkts = 0;
            rxPkts = 0;
        }
        if (txPkts < this.mLastTxPkts) {
            this.mLastTxPkts = txPkts;
        }
        if (rxPkts < this.mLastRxPkts) {
            this.mLastRxPkts = rxPkts;
        }
        this.mDTxPkts = txPkts - this.mLastTxPkts;
        this.mdRxPkts = rxPkts - this.mLastRxPkts;
        this.mLastTxPkts = txPkts;
        this.mLastRxPkts = rxPkts;
        if (this.mdRxPkts == 0 && this.mDTxPkts == 0 && this.mInterResult) {
            logD("Didn't send and receive any pkt.");
            return;
        }
        if (this.mdRxPkts < 1) {
            txPkts2 = txPkts;
            long j = this.mDTxPkts;
            if (j > 0) {
                if (this.mScreenOn || j != 1) {
                    this.mRxPktsLowCount++;
                } else {
                    logD("Did Send One But Not Receive Any pkt.");
                }
                logD("DT,tP=" + txPkts2 + ", dTP=" + this.mDTxPkts + ", rP=" + rxPkts + ", dRP=" + this.mdRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
                if (this.mInterResult) {
                    this.mLowTrafficeCount = 0;
                }
                z = this.mInterResult;
                if (z) {
                    if (this.mCaptivePortal || this.mResponseGotFromGateway) {
                        if (lastDetlaRxPkts > 20 && this.mdRxPkts > 20 && this.mDTxPkts > 20) {
                            triggerInternetDetect(true);
                            return;
                        } else if (this.mdRxPkts > 2 && this.mDTxPkts > 2) {
                            triggerInternetDetect(false);
                            return;
                        } else {
                            return;
                        }
                    } else if (this.mdRxPkts > 2 && this.mDTxPkts > 2) {
                        triggerInternetDetect(true);
                        this.mLowTrafficeCount = 0;
                        return;
                    } else if (this.mScreenOn) {
                        this.mLowTrafficeCount++;
                        logD("DT,mLowTrafficeCount=" + this.mLowTrafficeCount);
                        if (this.mLowTrafficeCount >= this.mLowTrafficeThreshold) {
                            triggerInternetDetect(true);
                            this.mLowTrafficeCount = 0;
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                } else if (z && (i = this.mRxPktsLowCount) > 0 && i % 2 == 0) {
                    triggerInternetDetect(true);
                    this.mRxPktsLowCount = 0;
                    return;
                } else {
                    return;
                }
            }
        } else {
            txPkts2 = txPkts;
        }
        this.mRxPktsLowCount = 0;
        logD("DT,tP=" + txPkts2 + ", dTP=" + this.mDTxPkts + ", rP=" + rxPkts + ", dRP=" + this.mdRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
        if (this.mInterResult) {
        }
        z = this.mInterResult;
        if (z) {
        }
    }

    private void triggerInternetDetect(boolean forceTrigger) {
        long standoffTime = System.currentTimeMillis() - this.mLastDetectInter;
        this.mTcpInterval = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
        logD("triggerInternetDetect, forceTrigger=" + forceTrigger + ", mLastInternetResult=" + this.mLastInternetResult + ", standoffTime=" + standoffTime + ", mInternetStandoffTime=" + this.mInternetStandoffTime);
        if (standoffTime >= RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS) {
            if (forceTrigger || this.mLastInternetResult != this.mInterResult) {
                sendMessage(200710);
                this.mInternetStandoffTime = 60000;
            } else if (standoffTime > ((long) this.mInternetStandoffTime)) {
                sendMessage(200710);
                int i = this.mInternetStandoffTime;
                if (i < 300000) {
                    this.mInternetStandoffTime = i + 60000;
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r13v25 double: [D('refSpeed' int), D('diffConsuccRate' double)] */
    /* JADX INFO: Multiple debug info for r0v56 int: [D('refSpeed' int), D('refRssi' int)] */
    /* JADX INFO: Multiple debug info for r0v57 boolean: [D('refSpeed' int), D('refIs5G' boolean)] */
    /* JADX INFO: Multiple debug info for r0v61 int: [D('refSpeed' int), D('refRssi' int)] */
    /* JADX INFO: Multiple debug info for r0v62 boolean: [D('refSpeed' int), D('refIs5G' boolean)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0255, code lost:
        if ((r1.mNetQualitys[r9] - r36) < r12) goto L_0x028a;
     */
    private OppoWifiAssistantRecord findAvailableCandidate(OppoWifiAssistantRecord lastRecord, int curScore, int curRssi, boolean isRoaming) {
        int refScore;
        OppoWifiAssistantRecord candidate;
        double diffConnrateThreshold;
        List<ScanResult> scanList;
        List<OppoWifiAssistantRecord> netRecords;
        Iterator<OppoWifiAssistantRecord> it;
        int refRssi;
        int diffScore;
        if (this.mWifiStateMachine.isDupDhcp()) {
            logD("[bug#1131400] dupDhcp, wait DHCP retry.");
            return null;
        }
        List<OppoWifiAssistantRecord> netRecords2 = this.mAssistantUtils.getSortNetworkRecords();
        List<ScanResult> scanList2 = this.mScanRequestProxy.syncGetScanResultsList();
        WifiInfo otherWifiInfo = this.mAssistantUtils.getOtherIfWifiInfo(this.mInterfaceName);
        int diffScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD", 10).intValue();
        double diffConnrateThreshold2 = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD", Double.valueOf((double) STATIC_DIFF_CONNRATE_THRESHOLD)).doubleValue();
        if (lastRecord == null) {
            candidate = null;
            StringBuilder sb = new StringBuilder();
            refScore = curScore;
            sb.append("findAvailableCandidate scan detect,");
            sb.append(getIsOppoManuConnect());
            logD(sb.toString());
        } else {
            candidate = null;
            refScore = curScore;
        }
        Iterator<OppoWifiAssistantRecord> it2 = netRecords2.iterator();
        double refConsuccRate = 0.0d;
        int refSpeed = 0;
        boolean refIs5G = false;
        OppoWifiAssistantRecord candidate2 = candidate;
        int refRssi2 = curRssi;
        while (it2.hasNext()) {
            OppoWifiAssistantRecord wnr = it2.next();
            if (!wnr.mNetworkValid) {
                it = it2;
                netRecords = netRecords2;
                scanList = scanList2;
                diffConnrateThreshold = diffConnrateThreshold2;
            } else if (wnr.mConfigkey == null) {
                it = it2;
                netRecords = netRecords2;
                scanList = scanList2;
                diffConnrateThreshold = diffConnrateThreshold2;
            } else {
                if (wnr.mConnExp) {
                    logD("findAvailableCandidate record config co exp");
                    it = it2;
                    netRecords = netRecords2;
                    scanList = scanList2;
                    diffConnrateThreshold = diffConnrateThreshold2;
                } else if (lastRecord != null && lastRecord.mConfigkey.equals(wnr.mConfigkey) && !isRoaming) {
                    logD("findAvailableCandidate same configKey and not roaming");
                    it = it2;
                    netRecords = netRecords2;
                    scanList = scanList2;
                    diffConnrateThreshold = diffConnrateThreshold2;
                } else if (lastRecord == null || lastRecord.mConfigkey.equals(wnr.mConfigkey) || !isRoaming) {
                    WifiConfiguration wcf = this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                    if (wcf == null) {
                        logD("findAvailableCandidate config is null");
                        it = it2;
                        netRecords = netRecords2;
                        scanList = scanList2;
                        diffConnrateThreshold = diffConnrateThreshold2;
                    } else if (wcf.status == 1) {
                        logD("findAvailableCandidate config is disabled");
                        it = it2;
                        netRecords = netRecords2;
                        scanList = scanList2;
                        diffConnrateThreshold = diffConnrateThreshold2;
                    } else {
                        double consuccRate = ((double) wnr.mConnSuccCout) / ((double) (wnr.mConnFailCount + wnr.mConnSuccCout));
                        Iterator<ScanResult> it3 = scanList2.iterator();
                        int refRssi3 = refRssi2;
                        int refScore2 = refScore;
                        while (it3.hasNext()) {
                            ScanResult result = it3.next();
                            String str = "\"" + result.SSID + "\"";
                            String scanBssid = result.BSSID;
                            int scanRssi = result.level;
                            if (WifiConfiguration.configKey(result).equals(wnr.mConfigkey)) {
                                if (scanBssid == null || otherWifiInfo == null || !scanBssid.equals(otherWifiInfo.getBSSID())) {
                                    if (otherWifiInfo != null) {
                                        if ((otherWifiInfo.is5GHz() && result.is5GHz()) || (otherWifiInfo.is24GHz() && result.is24GHz())) {
                                            logD("findAvailableCandidate skip BSS of the same band, bssid:" + result.BSSID + " freq:" + result.frequency);
                                        }
                                    }
                                    int index = getQualityIndex(result.is5GHz(), result.level);
                                    logD("findAvailableCandidate*** " + wnr.mConfigkey + "bssid:" + result.BSSID + " freq:" + result.frequency + ",nq[" + index + "]:" + wnr.mNetQualitys[index] + ",refScore:" + refScore2 + ",refRssi:" + refRssi3 + ",scanRssi:" + scanRssi + ",consuccRate:" + consuccRate);
                                    if ((lastRecord == null || scanRssi > -83) && (otherWifiInfo == null || scanRssi > -75)) {
                                        if (wnr.mNetQualitys[index] == -1 || refScore2 == -1) {
                                            diffScore = scanRssi - refRssi3;
                                        } else {
                                            diffScore = wnr.mNetQualitys[index] - refScore2;
                                        }
                                        if (lastRecord == null || !lastRecord.mNetworkValid || scanRssi - curRssi >= 5) {
                                            if (diffScore >= diffScoreThreshold) {
                                                refScore2 = wnr.mNetQualitys[index];
                                                refRssi3 = scanRssi;
                                                refConsuccRate = consuccRate;
                                                refSpeed = wnr.mMaxSpeed;
                                                refIs5G = wnr.mIs5G;
                                                candidate2 = wnr;
                                            } else if (diffScore >= diffScoreThreshold || diffScore < 0) {
                                                refRssi = refRssi3;
                                            } else if (!refIs5G || result.is5GHz()) {
                                                if (refIs5G || !result.is5GHz()) {
                                                    double diffConsuccRate = consuccRate - refConsuccRate;
                                                    if (diffConsuccRate >= diffConnrateThreshold2) {
                                                        refScore2 = wnr.mNetQualitys[index];
                                                        refConsuccRate = consuccRate;
                                                        refSpeed = wnr.mMaxSpeed;
                                                        refIs5G = wnr.mIs5G;
                                                        candidate2 = wnr;
                                                        refRssi3 = scanRssi;
                                                    } else {
                                                        refRssi = refRssi3;
                                                        if (diffConsuccRate < diffConnrateThreshold2 && diffConsuccRate > 0.0d) {
                                                            if (wnr.mMaxSpeed - refSpeed > 0) {
                                                                refScore2 = wnr.mNetQualitys[index];
                                                                refConsuccRate = consuccRate;
                                                                refSpeed = wnr.mMaxSpeed;
                                                                refIs5G = wnr.mIs5G;
                                                                candidate2 = wnr;
                                                                refRssi3 = scanRssi;
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    refScore2 = wnr.mNetQualitys[index];
                                                    refRssi3 = scanRssi;
                                                    refConsuccRate = consuccRate;
                                                    refSpeed = wnr.mMaxSpeed;
                                                    refIs5G = wnr.mIs5G;
                                                    candidate2 = wnr;
                                                }
                                            }
                                            it3 = it3;
                                            netRecords2 = netRecords2;
                                            scanList2 = scanList2;
                                            diffConnrateThreshold2 = diffConnrateThreshold2;
                                        }
                                    }
                                }
                                candidate2 = candidate2;
                                it3 = it3;
                                netRecords2 = netRecords2;
                                scanList2 = scanList2;
                                diffConnrateThreshold2 = diffConnrateThreshold2;
                            } else {
                                refRssi = refRssi3;
                            }
                            candidate2 = candidate2;
                            refRssi3 = refRssi;
                            it3 = it3;
                            netRecords2 = netRecords2;
                            scanList2 = scanList2;
                            diffConnrateThreshold2 = diffConnrateThreshold2;
                        }
                        refScore = refScore2;
                        it2 = it2;
                        refRssi2 = refRssi3;
                    }
                } else {
                    logD("findAvailableCandidate different configKey in roaming");
                    it = it2;
                    netRecords = netRecords2;
                    scanList = scanList2;
                    diffConnrateThreshold = diffConnrateThreshold2;
                }
                it2 = it;
                netRecords2 = netRecords;
                scanList2 = scanList;
                diffConnrateThreshold2 = diffConnrateThreshold;
            }
            logD("findAvailableCandidate record config key is null or invalid");
            it2 = it;
            netRecords2 = netRecords;
            scanList2 = scanList;
            diffConnrateThreshold2 = diffConnrateThreshold;
        }
        if (candidate2 == null) {
            logD("findAvailableCandidate candidate = null, mDataScore=" + this.mDataScore + ",manual=" + getIsOppoManuConnect() + ",mDataAutoSwitch= " + this.mDataAutoSwitch + ",mDataState=" + this.mDataState);
        } else {
            logD("findAvailableCandidate mConnectingId=" + this.mConnectingId + ",candidate = " + candidate2.toString());
        }
        if (this.mAssistantUtils.getEnabledState() && !shouldIgnoreSwitch()) {
            if (lastRecord == null || lastRecord.mWifiConfiguration == null) {
                this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, true);
            } else {
                this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, false);
            }
            if (getIsOppoManuConnect()) {
                if (!(lastRecord == null || lastRecord.mWifiConfiguration == null || candidate2 == null) || (this.mDataAutoSwitch && this.mDataScore == 50 && this.mDataState && curScore <= 79 - this.mWlanInvalidThreshold)) {
                    this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, true);
                    if (this.mAssistantUtils.getOtherIfCanShowDialog(this.mInterfaceName)) {
                        showDialog(lastRecord);
                    }
                }
            } else if (candidate2 != null) {
                selectCandidateNetwork(candidate2);
                if (getCurrentState() == this.mNetworkMonitorState || getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState) {
                    this.mCandidateKey = candidate2.mConfigkey;
                    setAssistantStatistics(STATISTIC_AUTO_CONN, TYPE_CONN_NET_WLAN, lastRecord, candidate2, curScore);
                }
            }
        } else if ((this.mAutoSwitch && !this.mFeatureState) || (lastRecord == null && candidate2 == null && this.mInitAutoConnect)) {
            if (!TextUtils.isEmpty(this.mInterfaceName)) {
                this.mWifiNative.reconnect(this.mInterfaceName);
            }
            this.mInitAutoConnect = false;
        }
        return candidate2;
    }

    public boolean inSpecialUrlList(String url) {
        if (url == null) {
            logD("url is null.");
            return false;
        }
        String value = getRomUpdateValue("NETWORK_SPECIAL_REDIRECT_URL", DEFAULT_SPECIAL_URL);
        if (value == null) {
            logD("Fail to getRomUpdateValue.");
            return false;
        }
        logD("inSpecialUrlList(), url list: " + value);
        String[] split = value.split(",");
        int length = split.length;
        for (int i = 0; i < length; i++) {
            if (url.contains(split[i])) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPublicHttpsServers() {
        List<String> defaultServers = Arrays.asList(this.mPublicHttpsServers);
        String value = getRomUpdateValue("NETWORK_PUBLIC_HTTPS_SERVERS_URL", null);
        if (value == null) {
            logD("Fail to getRomUpdateValue, using default servers!");
            return defaultServers;
        }
        logD("getPublicHttpsServers, updated servers: " + value);
        List<String> updatedServers = new ArrayList<>();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        logD("updated Servers less than 2, using default servers!");
        return defaultServers;
    }

    private List<String> getFallbackServers() {
        List<String> defaultServers = Arrays.asList(this.mFallbackHttpServers);
        String value = getRomUpdateValue("NETWORK_FALLBACK_HTTP_SERVERS_URL", null);
        if (value == null) {
            return defaultServers;
        }
        List<String> updatedServers = new ArrayList<>();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        return defaultServers;
    }

    private List<String> getInternalServers() {
        List<String> originalServers = Arrays.asList(this.mInternalServers);
        String value = getRomUpdateValue("OPPO_WIFI_ASSISTANT_NETSERVER", null);
        if (value == null) {
            logD("default is null, using original servers!");
            return originalServers;
        }
        List<String> updatedServers = new ArrayList<>();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        logD("updated Servers less than 2, using original servers!");
        return originalServers;
    }

    private String getExpHttpUrl() {
        return getRomUpdateValue("NETWORK_EXP_CAPTIVE_SERVER_HTTP_URL", DEFAULT_HTTP_URL);
    }

    private String getExpHttpsUrl() {
        return getRomUpdateValue("NETWORK_EXP_CAPTIVE_SERVER_HTTPS_URL", DEFAULT_HTTPS_URL);
    }

    private URL makeURL(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logE("Bad URL: " + url);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01c8, code lost:
        if (r7 != null) goto L_0x01ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0243, code lost:
        if (r7 == null) goto L_0x0246;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01f9 A[Catch:{ IOException -> 0x0220, RuntimeException -> 0x01fa, Exception -> 0x01d4, all -> 0x01cf, all -> 0x0247 }] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x021f A[Catch:{ IOException -> 0x0220, RuntimeException -> 0x01fa, Exception -> 0x01d4, all -> 0x01cf, all -> 0x0247 }] */
    private int sendHttpProbe(URL url) {
        Throwable th;
        IOException e;
        RuntimeException runtimeException;
        Exception ee;
        HttpURLConnection urlConnection = null;
        boolean isGenerate204 = false;
        int httpResponseCode = 599;
        String redirectUrl = null;
        try {
            String hostToResolve = url.getHost();
            String host = " ";
            logD("SHP: " + url + ", host= " + hostToResolve + ", mCurNetwork: " + this.mCurNetwork);
            if (!TextUtils.isEmpty(hostToResolve) && this.mCurNetwork != null) {
                if (hostToResolve.contains("generate_204")) {
                    isGenerate204 = true;
                }
                if (getCurrentState() != this.mDisconnectedState) {
                    if (this.mCurNetwork != null) {
                        InetAddress[] addresses = this.mCurNetwork.getAllByName(hostToResolve);
                        InetAddress gateway = this.mWifiStateMachine.syncGetDhcpResults().gateway;
                        try {
                            if (addresses[0] != null) {
                                host = "/" + addresses[0].getHostAddress();
                                logD("SHP ht:" + host + ",gw:" + gateway);
                            }
                            if (addresses.length != 1 || gateway == null || !gateway.toString().equals(host)) {
                                this.mResponseGotFromGateway = false;
                                if (getCurrentState() == this.mDisconnectedState || this.mCurNetwork == null) {
                                    if (0 != 0) {
                                        urlConnection.disconnect();
                                    }
                                    return 599;
                                }
                                urlConnection = (HttpURLConnection) this.mCurNetwork.openConnection(url);
                                urlConnection.setInstanceFollowRedirects(false);
                                urlConnection.setConnectTimeout(10000);
                                urlConnection.setReadTimeout(10000);
                                urlConnection.setRequestMethod("GET");
                                urlConnection.setUseCaches(false);
                                urlConnection.getInputStream();
                                httpResponseCode = urlConnection.getResponseCode();
                                redirectUrl = urlConnection.getHeaderField("location");
                                try {
                                    logD("SHP: " + url + ", code = " + httpResponseCode + ", conn = " + urlConnection.getHeaderField("Connection"));
                                    if (httpResponseCode == 200) {
                                        if (!isGenerate204) {
                                            if (urlConnection.getHeaderField("Connection") != null && urlConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive")) {
                                                logD("SHP: !isGenerate204, Keep-Alive 200 response treated as 204.");
                                                httpResponseCode = HTTP_NORMAL_CODE;
                                            }
                                        } else if (urlConnection.getContentLength() == 0 || (urlConnection.getHeaderField("Connection") != null && urlConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive"))) {
                                            logD("SHP: empty and Keep-Alive 200 response interpreted as 204 response.");
                                            httpResponseCode = HTTP_NORMAL_CODE;
                                        }
                                    }
                                    if (httpResponseCode >= HTTP_CAPTIVE_CODE_MID && httpResponseCode <= HTTP_CAPTIVE_CODE_END && redirectUrl != null && inSpecialUrlList(redirectUrl)) {
                                        logD("SHP: response 302 with special redirect url: " + redirectUrl);
                                        httpResponseCode = HTTP_NORMAL_CODE;
                                    }
                                } catch (IOException e2) {
                                    e = e2;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
                                } catch (RuntimeException e3) {
                                    runtimeException = e3;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
                                    if (urlConnection != null) {
                                        urlConnection.disconnect();
                                    }
                                    return httpResponseCode;
                                } catch (Exception e4) {
                                    ee = e4;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
                                    if (urlConnection != null) {
                                        urlConnection.disconnect();
                                    }
                                    return httpResponseCode;
                                }
                            } else {
                                this.mResponseGotFromGateway = true;
                                logD("SHP fgw !!");
                                if (0 != 0) {
                                    urlConnection.disconnect();
                                }
                                return 599;
                            }
                        } catch (IOException e5) {
                            e = e5;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
                        } catch (RuntimeException e6) {
                            runtimeException = e6;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
                            if (urlConnection != null) {
                            }
                            return httpResponseCode;
                        } catch (Exception e7) {
                            ee = e7;
                            redirectUrl = null;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
                            if (urlConnection != null) {
                            }
                            return httpResponseCode;
                        } catch (Throwable th2) {
                            th = th2;
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            throw th;
                        }
                    }
                }
                if (0 != 0) {
                    urlConnection.disconnect();
                }
                return 599;
            }
        } catch (IOException e8) {
            e = e8;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
        } catch (RuntimeException e9) {
            runtimeException = e9;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
            if (urlConnection != null) {
            }
            return httpResponseCode;
        } catch (Exception e10) {
            ee = e10;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
            if (urlConnection != null) {
            }
            return httpResponseCode;
        } catch (Throwable th3) {
            th = th3;
            if (urlConnection != null) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendParallelHttpProbes() {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicReference<Boolean> finalResult = new AtomicReference<>();
        List<String> publicHttpsServers = getPublicHttpsServers();
        Collections.shuffle(publicHttpsServers);
        List<String> intenalServers = getInternalServers();
        List<String> fallbackServers = getFallbackServers();
        Collections.shuffle(fallbackServers);
        try {
            URL url1 = new URL(publicHttpsServers.get(0));
            URL url2 = new URL(fallbackServers.get(0));
            URL url3 = new URL("http://" + intenalServers.get(this.mDetectInterCount % intenalServers.size()) + "/generate_204");
            AnonymousClass1ProbeThread httpProbe1 = new Thread(url1, finalResult, latch) {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* JADX WARN: Incorrect args count in method signature: (Ljava/net/URL;)V */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                        }
                        this.val$latch.countDown();
                    }
                    this.val$latch.countDown();
                }
            };
            AnonymousClass1ProbeThread httpProbe2 = new Thread(url2, finalResult, latch) {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* JADX WARN: Incorrect args count in method signature: (Ljava/net/URL;)V */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                        }
                        this.val$latch.countDown();
                    }
                    this.val$latch.countDown();
                }
            };
            AnonymousClass1ProbeThread httpProbe3 = new Thread(url3, finalResult, latch) {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* JADX WARN: Incorrect args count in method signature: (Ljava/net/URL;)V */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                        }
                        this.val$latch.countDown();
                    }
                    this.val$latch.countDown();
                }
            };
            httpProbe1.start();
            httpProbe2.start();
            httpProbe3.start();
            try {
                latch.await(40000, TimeUnit.MILLISECONDS);
                finalResult.compareAndSet(null, false);
                return finalResult.get().booleanValue();
            } catch (InterruptedException e) {
                logD("Error: probe wait interrupted!");
                return false;
            }
        } catch (MalformedURLException e2) {
            logD("Bad validation URL.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendParallelHttpProbesExp() {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<Boolean> finalResult = new AtomicReference<>();
        URL httpUrl = makeURL(getExpHttpUrl());
        URL httpsUrl = makeURL(getExpHttpsUrl());
        AnonymousClass2ProbeThread httpProbe1 = new Thread(httpUrl, finalResult, latch) {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass2ProbeThread */
            private volatile Boolean mResult;
            private final URL mUrl;
            final /* synthetic */ AtomicReference val$finalResult;
            final /* synthetic */ CountDownLatch val$latch;

            /* JADX WARN: Incorrect args count in method signature: (Ljava/net/URL;)V */
            {
                this.val$finalResult = r3;
                this.val$latch = r4;
                this.mUrl = url;
            }

            public Boolean getResult() {
                return this.mResult;
            }

            public void run() {
                int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
                if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                    this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                    this.val$finalResult.compareAndSet(null, this.mResult);
                    this.val$finalResult.compareAndSet(false, this.mResult);
                    this.val$latch.countDown();
                    if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                        try {
                            sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                        } catch (InterruptedException e) {
                            OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                        }
                        OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                    }
                }
                this.val$latch.countDown();
            }
        };
        AnonymousClass2ProbeThread httpsProbe2 = new Thread(httpsUrl, finalResult, latch) {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass2ProbeThread */
            private volatile Boolean mResult;
            private final URL mUrl;
            final /* synthetic */ AtomicReference val$finalResult;
            final /* synthetic */ CountDownLatch val$latch;

            /* JADX WARN: Incorrect args count in method signature: (Ljava/net/URL;)V */
            {
                this.val$finalResult = r3;
                this.val$latch = r4;
                this.mUrl = url;
            }

            public Boolean getResult() {
                return this.mResult;
            }

            public void run() {
                int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
                if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                    this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    oppoWifiAssistantStateTraker.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                    this.val$finalResult.compareAndSet(null, this.mResult);
                    this.val$finalResult.compareAndSet(false, this.mResult);
                    this.val$latch.countDown();
                    if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                        try {
                            sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                        } catch (InterruptedException e) {
                            OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                        }
                        OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                    }
                }
                this.val$latch.countDown();
            }
        };
        httpProbe1.start();
        httpsProbe2.start();
        try {
            latch.await(20000, TimeUnit.MILLISECONDS);
            finalResult.compareAndSet(null, false);
            return finalResult.get().booleanValue();
        } catch (InterruptedException e) {
            logD("Error: probe wait interrupted!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detectInternet() {
        if (!this.mDetectNet || this.mInternetDetecting) {
            logD("DI, no need check");
        } else if (this.mWifiStateMachine.isRoaming()) {
            logD("DI, ring");
        } else {
            logD("detectInternet...");
            this.mLastInternetResult = this.mInterResult;
            Handler handler = this.mInterThread;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass5 */

                    public void run() {
                        boolean probeResult;
                        String dectConfig = OppoWifiAssistantStateTraker.this.mLastConfigkey;
                        OppoWifiAssistantStateTraker.access$9808(OppoWifiAssistantStateTraker.this);
                        OppoWifiAssistantStateTraker.this.mLastDetectInter = System.currentTimeMillis();
                        OppoWifiAssistantStateTraker.this.mInternetDetecting = true;
                        if (OppoWifiAssistantStateTraker.this.mWifiStateMachine.isChineseOperator()) {
                            probeResult = OppoWifiAssistantStateTraker.this.sendParallelHttpProbes();
                        } else {
                            probeResult = OppoWifiAssistantStateTraker.this.sendParallelHttpProbesExp();
                        }
                        OppoWifiAssistantStateTraker.this.mInternetDetecting = false;
                        if (dectConfig != null && !dectConfig.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                            return;
                        }
                        if (probeResult) {
                            OppoWifiAssistantStateTraker.this.mCaptivePortal = false;
                            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                            if (!OppoWifiAssistantStateTraker.this.mInterResult) {
                                OppoWifiAssistantStateTraker.this.sendMessageForNetChange(true);
                            }
                        } else if (!OppoWifiAssistantStateTraker.this.mInterResult || OppoWifiAssistantStateTraker.this.mWifiStateMachine == null || OppoWifiAssistantStateTraker.this.mWifiStateMachine.getWifiInfo() == null || OppoWifiAssistantStateTraker.this.mWifiStateMachine.getWifiInfo().getRssi() < -75) {
                            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                        } else {
                            if (OppoWifiAssistantStateTraker.this.mInternetInvalidCount == 0) {
                                OppoWifiAssistantStateTraker.this.logD("DI, change to unvailable, detect again before set");
                                if (OppoWifiAssistantStateTraker.this.mWifiStateMachine.isChineseOperator()) {
                                    Intent dnsRecoveryIntent = new Intent(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_INTERNET_INVAILD);
                                    dnsRecoveryIntent.addFlags(67108864);
                                    OppoWifiAssistantStateTraker.this.mContext.sendBroadcastAsUser(dnsRecoveryIntent, UserHandle.ALL);
                                }
                                OppoWifiAssistantStateTraker.access$7108(OppoWifiAssistantStateTraker.this);
                            } else {
                                OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                                OppoWifiAssistantStateTraker.this.sendMessageForNetChange(false);
                            }
                            OppoWifiAssistantStateTraker.this.setInternetDetectAlarm(0, RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setInternetDetectAlarm(int type, long delay) {
        this.mAlarmManager.cancel(this.mDetectInterIntent);
        this.mDetectInterIntent = getPrivateBroadcast(ACTION_DETECT_INTERNET);
        this.mAlarmManager.set(type, System.currentTimeMillis() + delay, this.mDetectInterIntent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageForNetChange(boolean valid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_INTERNET_CHANGE, valid ? 1 : 0, 0, this.mLastConfigkey));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needToDetectTcpStatus() {
        if (!this.mAssistantUtils.getEnabledState()) {
            logD("needToDetectInternet, switch is off");
            return false;
        }
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        NetworkInfo mni = this.mCM.getActiveNetworkInfo();
        if (mni != null && mni.getType() == 0) {
            logD("needToDetectInternet, data");
            return false;
        } else if (this.mDetectNet && !this.mChangedToData) {
            return true;
        } else {
            logD("needToDetectInternet, no need to check");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0155  */
    private boolean detectTcpStatus() {
        int i;
        boolean isAvailable = false;
        OppoTcpInfoMonitor oppoTcpInfoMonitor = this.mOppoTcpInfoMonitor;
        if (oppoTcpInfoMonitor != null) {
            this.mTcpLinkStatus = oppoTcpInfoMonitor.getCurrentTcpLinkStatus();
        }
        logD("Before adjustment, mTcpLinkStatus = " + this.mTcpLinkStatus + " mTcpStatistics = " + this.mTcpStatistics);
        int i2 = this.mTcpLinkStatus;
        if (i2 == 16) {
            int i3 = this.mTcpStatistics;
            if (i3 < 0 || i3 >= 1000) {
                this.mTcpStatistics = 1;
            } else {
                this.mTcpStatistics = i3 + 1;
            }
            if (!this.mInterResult && (this.mGotInternetResult & 2) == 2 && this.mTcpStatistics == 2) {
                triggerInternetDetect(true);
            }
        } else if (i2 == 18) {
            int i4 = this.mTcpStatistics;
            if (i4 < 1000 || i4 >= TCP_STAT_POOR_COUNT) {
                this.mTcpStatistics = 1000;
            } else {
                this.mTcpStatistics = i4 + 1;
            }
            if (this.mTcpStatistics == 1005) {
                triggerInternetDetect(false);
            }
        } else if (i2 == 2 || i2 == 3) {
            int i5 = this.mTcpStatistics;
            if (i5 > -1000 || i5 <= -2000) {
                this.mTcpStatistics = -1000;
            } else {
                this.mTcpStatistics = i5 - 1;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == -1003) {
                triggerInternetDetect(true);
            }
        } else if (i2 == 1) {
            int i6 = this.mTcpStatistics;
            if (i6 > 0 || i6 <= -1000) {
                this.mTcpStatistics = -1;
            } else {
                this.mTcpStatistics = i6 - 1;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == -3) {
                triggerInternetDetect(true);
            }
        } else if (i2 == 0) {
            int i7 = this.mTcpStatistics;
            if (i7 > 0) {
                this.mTcpStatistics = i7 - 1;
            } else if (i7 < 0) {
                this.mTcpStatistics = i7 + 1;
            }
        } else {
            this.mTcpStatistics = 0;
        }
        logD("After adjustment, mTcpStatistics = " + this.mTcpStatistics);
        if (this.mTcpStatistics == 3) {
            int i8 = this.mGotInternetResult;
            if ((i8 & 4) == 4 || ((i8 & 8) == 8 && (i8 & 2) == 2)) {
                int i9 = this.mGotInternetResult;
                if ((i9 & 1) == 1) {
                    this.mTcpStatistics = 0;
                } else {
                    isAvailable = true;
                    this.mGotInternetResult = i9 | 1;
                    this.mGotInternetResult &= -3;
                }
                if (this.mTcpInterval == 2000) {
                    this.mTcpShortIntervalCount++;
                } else {
                    this.mTcpShortIntervalCount = 0;
                }
                if (this.mTcpShortIntervalCount > 50 || (i = this.mTcpStatistics) >= 3 || i == -10 || i == -1020) {
                    this.mTcpInterval = 5000;
                }
                logD("mGotInternetResult = " + this.mGotInternetResult + " isAvailable = " + isAvailable);
                return isAvailable;
            }
        }
        if (this.mTcpStatistics == -10) {
            int i10 = this.mGotInternetResult;
            if ((i10 & 4) != 4 || (i10 & 1) == 1) {
                int i11 = this.mGotInternetResult;
                if ((i11 & 2) == 2) {
                    this.mTcpStatistics = 0;
                } else {
                    this.mGotInternetResult = i11 | 2;
                    this.mGotInternetResult &= -2;
                }
                if (this.mTcpInterval == 2000) {
                }
                this.mTcpInterval = 5000;
                logD("mGotInternetResult = " + this.mGotInternetResult + " isAvailable = " + isAvailable);
                return isAvailable;
            }
        }
        if (this.mTcpStatistics == -1020) {
            int i12 = this.mGotInternetResult;
            if ((i12 & 8) == 8 || (!this.mCaptivePortal && i12 < 4)) {
                int i13 = this.mGotInternetResult;
                if ((i13 & 2) == 2) {
                    this.mTcpStatistics = 0;
                } else {
                    this.mGotInternetResult = i13 | 2;
                    this.mGotInternetResult &= -2;
                }
            }
        }
        if (this.mTcpInterval == 2000) {
        }
        this.mTcpInterval = 5000;
        logD("mGotInternetResult = " + this.mGotInternetResult + " isAvailable = " + isAvailable);
        return isAvailable;
    }

    public void rmConfUpdateRecord(int netId) {
        WifiConfiguration rmconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (rmconfig == null) {
            logD("removeNetworkAvailable rmconfig == null");
            return;
        }
        String str = this.mLastConfigkey;
        if (str != null && str.equals(rmconfig.configKey(false)) && (getCurrentState() == this.mNetworkMonitorState || getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState)) {
            long rmNetworkTime = System.currentTimeMillis();
            long j = this.mAutoConnWlanTime;
            long rmForWlan = rmNetworkTime - j;
            if (j > 0 && rmForWlan > 0 && rmForWlan < 180000) {
                logD("stc wwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_WLAN);
                this.mAutoConnWlanTime = 0;
            }
            long j2 = this.mAutoDataToWlanTime;
            long rmForData = rmNetworkTime - j2;
            if (!this.mChangedToData && j2 > 0 && rmForData > 0 && rmForData < 180000) {
                logD("stc dwb2");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_DATA);
                this.mAutoDataToWlanTime = 0;
            }
            if (!this.mChangedToData) {
                logD("stc wdc2");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_REMOVE_NETWORK_FOR_DATA);
            }
        }
        clearRecordWifiConfig(rmconfig.configKey(false));
    }

    public void rmOrupdateRecordStatus(String key, boolean remove) {
        OppoWifiAssistantRecord record;
        logD("rmOrupdateRecordStatus key:" + key);
        if (key != null && (record = this.mAssistantUtils.getNetworkRecord(key)) != null) {
            if (remove) {
                this.mAssistantUtils.removeNetworkRecord(key);
                return;
            }
            record.mConnExp = true;
            this.mAssistantUtils.addNetworkRecord(key, record, false);
        }
    }

    public void resetConnExp(String key) {
        OppoWifiAssistantRecord reSetRecord;
        logD("rtce key:" + key);
        if (key != null && (reSetRecord = this.mAssistantUtils.getNetworkRecord(key)) != null) {
            reSetRecord.mConnExp = false;
            this.mAssistantUtils.addNetworkRecord(key, reSetRecord, false);
        }
    }

    public boolean isSoftAp(LinkProperties lp) {
        if (lp == null) {
            logD("LinkProperties is null, return");
            return false;
        }
        InetAddress mCurrentGateway = null;
        for (RouteInfo route : lp.getRoutes()) {
            if (route.hasGateway()) {
                mCurrentGateway = route.getGateway();
            }
        }
        if (mCurrentGateway == null) {
            logD("InetAddress getGateway is null, return");
            return false;
        }
        logD("mCurrentGateway : " + mCurrentGateway.toString());
        if (mCurrentGateway.toString().contains("/192.168.43") || mCurrentGateway.toString().equals("/192.168.49.1") || mCurrentGateway.toString().equals("/172.20.10.1")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isThirdAppOperate() {
        if (!getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP", true).booleanValue()) {
            logD("rd close it.");
            return true;
        }
        int operatorUid = getOppoManuConnectUid();
        logD("ita = " + operatorUid);
        if (operatorUid == 1000 || operatorUid == 1010 || !inWhiteList(operatorUid)) {
            return false;
        }
        return true;
    }

    private boolean inWhiteList(int uid) {
        String pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        if (pkgName == null) {
            logD("[1730153] Fail to get package name from UID.");
            return false;
        }
        String pkgList = getRomUpdateValue("OPPO_WIFI_ASSISTANT_CONTROL_APP_LIST", DEFAULT_CONTROL_APP_LIST);
        if (pkgList == null) {
            logD("[1730153] OPPO_WIFI_ASSISTANT_CONTROL_APP_LIST is null.");
            return false;
        }
        logD("[1730153] ThirdApp=" + pkgName + " WhiteList=" + pkgList);
        String[] split = pkgList.split(",");
        int length = split.length;
        for (int i = 0; i < length; i++) {
            if (pkgName.contains(split[i].trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGameScene() {
        return false;
    }

    private String inStream2String(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int len = is.read(buf);
            if (len == -1) {
                return new String(baos.toByteArray());
            }
            baos.write(buf, 0, len);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissDialog(int type) {
        AlertDialog alertDialog = mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        ColorListDialog colorListDialog = mDataAlertDialog;
        if (colorListDialog != null) {
            colorListDialog.dismiss();
        }
    }

    private void showDialog(OppoWifiAssistantRecord lastRecord) {
        String message;
        String message2;
        logD("showDialog mUnavailableKey:" + this.mUnavailableKey);
        if (mAlertDialog != null) {
            logD("showDialog repeated");
        } else if (lastRecord == null) {
            logD("showDialog record null");
        } else if (lastRecord.mWifiConfiguration == null) {
            logD("showDialog record parameter null");
        } else if (getCurrentState() == this.mConnectedState || getCurrentState() == this.mNetworkMonitorState) {
            String str = this.mUnavailableKey;
            if (str != null && str.equals(lastRecord.mConfigkey)) {
                logD("showDialog mUnavailableKey is same");
            } else if (mSlaDialog != null) {
                logD("showDialog sla dialog showing");
            } else {
                this.mUnavailableKey = lastRecord.mConfigkey;
                String ssid = convert2noQuoteString(lastRecord.mWifiConfiguration.SSID);
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
                String title = "" + ((Object) this.mContext.getText(201653623));
                if (getIsOppoManuConnect()) {
                    if (lastRecord.mNetworkValid) {
                        message2 = this.mContext.getResources().getString(201653641);
                    } else {
                        message2 = this.mContext.getResources().getString(201653642);
                    }
                    message = String.format(message2, ssid);
                    builder.setPositiveButton(201653542, new DialogInterface.OnClickListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass6 */

                        public void onClick(DialogInterface d, int w) {
                            if (OppoWifiAssistantStateTraker.mAlertDialog != null) {
                                OppoWifiAssistantStateTraker.mAlertDialog.dismiss();
                            }
                            OppoWifiAssistantStateTraker.this.resetOppoManuConnect();
                            OppoWifiAssistantStateTraker.this.mClickDialogSwitch = true;
                            OppoWifiAssistantStateTraker.this.mAssistantUtils.disableDualStaWithDelay(5000);
                            OppoWifiAssistantStateTraker.this.findAvailableCandidate(null, 0, -127, false);
                        }
                    });
                    builder.setNegativeButton(201653543, new DialogInterface.OnClickListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass7 */

                        public void onClick(DialogInterface d, int w) {
                            if (OppoWifiAssistantStateTraker.mAlertDialog != null) {
                                OppoWifiAssistantStateTraker.mAlertDialog.dismiss();
                            }
                        }
                    });
                } else {
                    message = String.format(this.mContext.getResources().getString(201653644), ssid);
                    builder.setPositiveButton(201653541, new DialogInterface.OnClickListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass8 */

                        public void onClick(DialogInterface d, int w) {
                            if (OppoWifiAssistantStateTraker.mAlertDialog != null) {
                                OppoWifiAssistantStateTraker.mAlertDialog.dismiss();
                            }
                        }
                    });
                }
                builder.setTitle(title);
                builder.setMessage(message);
                synchronized (mDialogLock) {
                    if (mAlertDialog != null) {
                        logD("showDialog multi-thread access!!");
                        return;
                    }
                    mAlertDialog = builder.create();
                    mAlertDialog.setCanceledOnTouchOutside(false);
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass9 */

                        public void onDismiss(DialogInterface dialog) {
                            AlertDialog unused = OppoWifiAssistantStateTraker.mAlertDialog = null;
                            OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker.this.mContext, false);
                        }
                    });
                    OppoWifiCommonUtil.disableStatusBar(this.mContext, true);
                    WindowManager.LayoutParams p = mAlertDialog.getWindow().getAttributes();
                    p.ignoreHomeMenuKey = 1;
                    p.privateFlags = 16;
                    mAlertDialog.getWindow().setAttributes(p);
                    mAlertDialog.getWindow().setType(2003);
                    mAlertDialog.getWindow().addFlags(2);
                    mAlertDialog.show();
                    TextView msg = (TextView) mAlertDialog.findViewById(16908299);
                    if (msg != null) {
                        msg.setGravity(17);
                    } else {
                        logD("textview is null");
                    }
                }
            }
        } else {
            logD("showDialog incorrect state");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDialogForData(OppoWifiAssistantRecord lastRecord) {
        String message;
        if (lastRecord == null) {
            logD("showDialogForData record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            logD("showDialogForData record parameter maybe null");
        } else if (getCurrentState() != this.mNetworkMonitorState) {
            logD("showDialogForData state error");
        } else if (mSlaDialog != null) {
            logD("showDialogForData sla dialog showing");
        } else {
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = convert2noQuoteString(lastRecord.mWifiConfiguration.SSID);
            AlertDialog alertDialog = mAlertDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            ColorListDialog colorListDialog = mDataAlertDialog;
            if (colorListDialog != null) {
                colorListDialog.dismiss();
            }
            mDataAlertDialog = new ColorListDialog(this.mContext, 201523207);
            String title = "" + ((Object) this.mContext.getText(201653623));
            CharSequence[] items = {this.mContext.getText(201653539), this.mContext.getText(201653541)};
            if (lastRecord.mNetworkValid) {
                message = this.mContext.getResources().getString(201653645);
            } else {
                message = this.mContext.getResources().getString(201653643);
            }
            mDataAlertDialog.setTitle(title);
            mDataAlertDialog.setMessage((String.format(message, ssid) + "\n") + ((Object) this.mContext.getText(201653538)));
            mDataAlertDialog.setItems(items, (int[]) null, new DialogInterface.OnClickListener() {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass10 */

                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        OppoWifiAssistantStateTraker.this.logD("sdfn no remind");
                        Settings.System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.NOT_REMIND_WIFI_ASSISTANT, 1);
                        if (OppoWifiAssistantStateTraker.mDataAlertDialog != null) {
                            OppoWifiAssistantStateTraker.mDataAlertDialog.dismiss();
                        }
                    } else if (which == 1 && OppoWifiAssistantStateTraker.mDataAlertDialog != null) {
                        OppoWifiAssistantStateTraker.mDataAlertDialog.dismiss();
                    }
                }
            });
            synchronized (mDialogLock) {
                if (mAlertDialog != null) {
                    logD("showDialogForData multi-thread access!!");
                    return;
                }
                if (mDataAlertDialog.getDialog() != null) {
                    mDataAlertDialog.getDialog().setCanceledOnTouchOutside(false);
                    mDataAlertDialog.getDialog().setCancelable(false);
                    mDataAlertDialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass11 */

                        public void onDismiss(DialogInterface dialog) {
                            ColorListDialog unused = OppoWifiAssistantStateTraker.mDataAlertDialog = null;
                            OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker.this.mContext, false);
                        }
                    });
                    OppoWifiCommonUtil.disableStatusBar(this.mContext, true);
                    WindowManager.LayoutParams p = mDataAlertDialog.getDialog().getWindow().getAttributes();
                    p.ignoreHomeMenuKey = 1;
                    p.privateFlags = 16;
                    mDataAlertDialog.getDialog().getWindow().setAttributes(p);
                    mDataAlertDialog.getDialog().getWindow().setType(2003);
                    mDataAlertDialog.getDialog().getWindow().addFlags(2);
                    mDataAlertDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDialogForSla() {
        if (mAlertDialog != null || mDataAlertDialog != null) {
            logD("showDialogForSla ignore.");
        } else if (getCurrentState() != this.mConnectedState && getCurrentState() != this.mNetworkMonitorState) {
            logD("showDialogForSla do not show dialog when wifi disconnected.");
        } else if (mSlaDialog != null) {
            logD("showDialogForSla already showing.");
        } else if (this.mChangedToData) {
            logD("showDialogForSla do not show dialog when mChangedToData is true.");
        } else {
            int slaDialogCount = Settings.System.getInt(this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", 0);
            long lastDialogTimestamp = Settings.System.getLong(this.mContext.getContentResolver(), "SLA_LAST_DIALOG_TIMESTAMP", 0);
            long currentMillis = System.currentTimeMillis();
            if (slaDialogCount >= 3) {
                logD("showDialogForSla do not show dialog, slaDialogCount=" + slaDialogCount);
                this.mOppoSlaManager.setShowDialog(false);
            } else if (currentMillis - lastDialogTimestamp < 86400000) {
                logD("showDialogForSla do not show dialog, too frequent");
            } else {
                Settings.System.putInt(this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", slaDialogCount + 1);
                Settings.System.putLong(this.mContext.getContentResolver(), "SLA_LAST_DIALOG_TIMESTAMP", currentMillis);
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
                builder.setPositiveButton(201653624, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass12 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker.mSlaDialog != null) {
                            OppoWifiAssistantStateTraker.mSlaDialog.dismiss();
                            Settings.System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", 3);
                            OppoWifiAssistantStateTraker.this.mOppoSlaManager.setShowDialog(false);
                            Log.d(OppoWifiAssistantStateTraker.TAG, "showDialogForSla enable SLA switch..");
                            Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoSlaManager.KEY_SLA_SWITCH, 1);
                        }
                    }
                });
                builder.setNegativeButton(201653622, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass13 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker.mSlaDialog != null) {
                            OppoWifiAssistantStateTraker.mSlaDialog.dismiss();
                            int slaCancelCount = Settings.System.getInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), "SLA_CANCEL_COUNT", 0);
                            Settings.System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), "SLA_CANCEL_COUNT", slaCancelCount + 1);
                            Log.d(OppoWifiAssistantStateTraker.TAG, "showDialogForSla Cancel clicked..cancelCount=" + (slaCancelCount + 1));
                        }
                    }
                });
                builder.setTitle("" + ((Object) this.mContext.getText(201653623)));
                builder.setMessage("" + ((Object) this.mContext.getText(201653620)));
                synchronized (mDialogLock) {
                    if (mAlertDialog != null) {
                        logD("showDialogForSla multi-thread access!!");
                        return;
                    }
                    mSlaDialog = builder.create();
                    mSlaDialog.setCanceledOnTouchOutside(false);
                    mSlaDialog.setCancelable(false);
                    mSlaDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker.AnonymousClass14 */

                        public void onDismiss(DialogInterface dialog) {
                            AlertDialog unused = OppoWifiAssistantStateTraker.mSlaDialog = null;
                            OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker.this.mContext, false);
                        }
                    });
                    WindowManager.LayoutParams p = mSlaDialog.getWindow().getAttributes();
                    p.ignoreHomeMenuKey = 1;
                    p.privateFlags = 16;
                    p.isDisableStatusBar = 1;
                    mSlaDialog.getWindow().setAttributes(p);
                    mSlaDialog.getWindow().setType(2003);
                    mSlaDialog.getWindow().addFlags(2);
                    mSlaDialog.show();
                    TextView textView = (TextView) mSlaDialog.findViewById(16908299);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasCheckNoRemind() {
        boolean hasCheck = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), NOT_REMIND_WIFI_ASSISTANT, 0) == 1) {
            hasCheck = true;
        }
        logD("hasCheck = " + hasCheck);
        return hasCheck;
    }

    private boolean matchKeymgmt(String validKey, String scanKey) {
        if (validKey == null || scanKey == null) {
            return false;
        }
        char c = 65535;
        switch (validKey.hashCode()) {
            case -2038298883:
                if (validKey.equals(SECURITY_EAP)) {
                    c = 2;
                    break;
                }
                break;
            case -2038287759:
                if (validKey.equals(SECURITY_PSK)) {
                    c = 0;
                    break;
                }
                break;
            case -850615648:
                if (validKey.equals(SECURITY_WAPI_CERT)) {
                    c = 5;
                    break;
                }
                break;
            case 85826:
                if (validKey.equals(SECURITY_WEP)) {
                    c = 6;
                    break;
                }
                break;
            case 2402104:
                if (validKey.equals(SECURITY_NONE)) {
                    c = 7;
                    break;
                }
                break;
            case 36491973:
                if (validKey.equals("IEEE8021X")) {
                    c = 3;
                    break;
                }
                break;
            case 1196474771:
                if (validKey.equals(SECURITY_WPA2_PSK)) {
                    c = 1;
                    break;
                }
                break;
            case 1219499692:
                if (validKey.equals(SECURITY_WAPI_PSK)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
                if (scanKey.contains("WPA-PSK") || scanKey.contains("WPA2-PSK")) {
                    return true;
                }
                return false;
            case 2:
                if (scanKey.contains("EAP")) {
                    return true;
                }
                return false;
            case 3:
                if (scanKey.contains("IEEE8021X")) {
                    return true;
                }
                return false;
            case 4:
                if (scanKey.contains("WAPI-KEY") || scanKey.contains("WAPI-PSK")) {
                    return true;
                }
                return false;
            case 5:
                if (scanKey.contains("WAPI-CERT")) {
                    return true;
                }
                return false;
            case 6:
                if (scanKey.contains(SECURITY_WEP)) {
                    return true;
                }
                return false;
            case 7:
                if (scanKey.contains("PSK") || scanKey.contains("EAP") || scanKey.contains(SECURITY_WEP) || scanKey.contains("WAPI") || scanKey.contains("IEEE8021X")) {
                    return false;
                }
                return true;
            default:
                logD("matchKeymgmt default");
                return false;
        }
    }

    private String parseKeymgmt(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(13)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(14)) {
            return SECURITY_WAPI_CERT;
        }
        if (config.wepTxKeyIndex >= 0 && config.wepTxKeyIndex < config.wepKeys.length && config.wepKeys[config.wepTxKeyIndex] != null) {
            return SECURITY_WEP;
        }
        if (config.allowedKeyManagement.get(4)) {
            return SECURITY_WPA2_PSK;
        }
        return SECURITY_NONE;
    }

    private final class NetHandler extends Handler {
        public NetHandler(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            boolean newConf = false;
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_UPDATE_NETWORK_STATE /* 200714 */:
                    WifiInfo updateWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                    if (!OppoWifiAssistantStateTraker.this.mIsSoftAP || !OppoWifiAssistantStateTraker.this.isThirdAppOperate()) {
                        String netStateSsid = (String) msg.obj;
                        OppoWifiAssistantStateTraker.this.logD("EVENT_UPDATE_NETWORK_STATE: " + netStateSsid + ",info: " + updateWifiInfo);
                        if (updateWifiInfo != null && netStateSsid != null && updateWifiInfo.getSSID().equals(netStateSsid)) {
                            boolean netValid = msg.arg1 == 0;
                            OppoWifiAssistantStateTraker.this.logD("EVENT_UPDATE_NETWORK_STATE: " + netValid);
                            if (netValid || !OppoWifiAssistantStateTraker.this.mWifiStateMachine.isDupDhcp()) {
                                if (OppoWifiAssistantStateTraker.this.mInterResult && !netValid) {
                                    OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = true;
                                }
                                WifiConfiguration netConf = OppoWifiAssistantStateTraker.this.getWifiConfig(netStateSsid, updateWifiInfo.getBSSID());
                                if (netConf != null && netConf.networkId == OppoWifiAssistantStateTraker.this.mConnectedId) {
                                    Settings.Global.putString(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_SSID, netStateSsid);
                                    Settings.Global.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.KEY_NETWORK_MONITOR_AVAILABLE, netValid ? 1 : 0);
                                    if (OppoWifiAssistantStateTraker.this.mGotInternetResult < 4 || OppoWifiAssistantStateTraker.this.mCaptivePortal || netValid != OppoWifiAssistantStateTraker.this.mInterResult) {
                                        OppoWifiAssistantStateTraker.this.updateRecordInternetStateAndTime(netConf.configKey(false), netValid, true);
                                    }
                                    if (netValid && netValid != OppoWifiAssistantStateTraker.this.mInterResult && OppoWifiAssistantStateTraker.this.mWifiStateMachine != null && OppoWifiAssistantStateTraker.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                                        OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                                    }
                                    OppoWifiAssistantStateTraker.this.mInterResult = netValid;
                                    if (netValid) {
                                        OppoWifiAssistantStateTraker.access$3976(OppoWifiAssistantStateTraker.this, 4);
                                        OppoWifiAssistantStateTraker.access$3972(OppoWifiAssistantStateTraker.this, -9);
                                    } else {
                                        OppoWifiAssistantStateTraker.access$3976(OppoWifiAssistantStateTraker.this, 8);
                                        OppoWifiAssistantStateTraker.access$3972(OppoWifiAssistantStateTraker.this, -5);
                                    }
                                } else if (OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mConnectedState || OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mVerifyInternetState) {
                                    if (netConf == null) {
                                        OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_EXP);
                                    } else {
                                        OppoWifiAssistantRecord expInfo = new OppoWifiAssistantRecord();
                                        expInfo.mNetid = netConf.networkId;
                                        expInfo.mConfigkey = netConf.configKey();
                                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                                        oppoWifiAssistantStateTraker.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_EXP, expInfo, null, oppoWifiAssistantStateTraker.mConnectedId);
                                    }
                                }
                                if (OppoWifiAssistantStateTraker.this.mInterChangeToInvalid) {
                                    OppoWifiAssistantStateTraker.this.startNoInternetStatistics(netValid, 3);
                                    return;
                                } else {
                                    OppoWifiAssistantStateTraker.this.startNoInternetStatistics(netValid, 4);
                                    return;
                                }
                            } else {
                                OppoWifiAssistantStateTraker.this.logD("[bug#1131400] dupDhcp, wait DHCP retry.");
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case OppoWifiAssistantStateTraker.EVENT_CONNECT_NETWORK /* 200715 */:
                    if (OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo() != null) {
                        int netId = msg.arg1;
                        LinkProperties curLink = (LinkProperties) msg.obj;
                        Network network = OppoWifiAssistantStateTraker.this.mWifiStateMachine.getCurrentNetwork();
                        if (curLink != null && network != null && network.netId == netId) {
                            OppoWifiAssistantStateTraker.this.mLastSwitchedToWifiTime = System.currentTimeMillis();
                            removeMessages(OppoWifiAssistantStateTraker.CMD_SEND_LOW_QUALITY);
                            OppoWifiAssistantStateTraker.this.mCurNetwork = network;
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker2.mIsSoftAP = oppoWifiAssistantStateTraker2.isSoftAp(curLink);
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiAssistantStateTraker.EVENT_INTERNET_CHANGE /* 200722 */:
                    boolean interResult = msg.arg1 == 1;
                    String interConf = (String) msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("ir=" + interResult + "ic=" + interConf);
                    if (interResult) {
                        OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = false;
                        if (OppoWifiAssistantStateTraker.this.mLastConfigkey != null && OppoWifiAssistantStateTraker.this.mLastConfigkey.equals(OppoWifiAssistantStateTraker.this.mUnavailableKey)) {
                            OppoWifiAssistantStateTraker.this.dismissDialog(2);
                        }
                    } else if (!OppoWifiAssistantStateTraker.this.getIsOppoManuConnect()) {
                        OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = true;
                    }
                    OppoWifiAssistantStateTraker.this.updateRecordInternetStateAndTime(interConf, interResult, false);
                    OppoWifiAssistantStateTraker.this.mInterResult = interResult;
                    if (!OppoWifiAssistantStateTraker.this.mChangedToData && OppoWifiAssistantStateTraker.this.mInterResult && !OppoWifiAssistantStateTraker.this.mLastInternetResult && OppoWifiAssistantStateTraker.this.mWifiStateMachine != null && OppoWifiAssistantStateTraker.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker.this.mInterfaceName)) {
                        OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                    }
                    if (OppoWifiAssistantStateTraker.this.mLastPktInfo != null) {
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker3.updateRecordLinkQuality(oppoWifiAssistantStateTraker3.mLastPktInfo);
                    }
                    if (interResult && !OppoWifiAssistantStateTraker.this.mChangedNetwork && OppoWifiAssistantStateTraker.this.mWifiStateMachine != null) {
                        OppoWifiAssistantStateTraker.this.mWifiStateMachine.setNetworkStatus(true);
                    }
                    OppoWifiAssistantStateTraker.this.startNoInternetStatistics(interResult, 3);
                    return;
                case OppoWifiAssistantStateTraker.EVENT_CAPTIVE_PORTAL /* 200723 */:
                    if (Integer.valueOf(msg.arg1).intValue() == 1) {
                        newConf = true;
                    }
                    String captiveConf = (String) msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("nc=" + newConf + ",cc=" + captiveConf);
                    OppoWifiAssistantStateTraker.this.updateRecordCaptiveState(captiveConf, true, true);
                    return;
                case OppoWifiAssistantStateTraker.EVENT_SCAN_TIMEOUT /* 200724 */:
                    OppoWifiAssistantStateTraker.this.mTriggerScan = false;
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startNoInternetStatistics(boolean interResult, int reason) {
        if (OppoDataStallHelper.getInstance() != null) {
            OppoDataStallHelper.getInstance().setHasInetAccess(interResult);
            if (!interResult) {
                OppoDataStallHelper.getInstance().networkStatistic(reason, (ExtendedWifiInfo) this.mWifiStateMachine.getWifiInfo(), this.mLastNetId, this.mInterfaceName);
            }
        }
    }

    private final class InterThread extends Handler {
        private InterThread() {
        }

        public void handleMessage(Message msg) {
        }
    }

    private void sendLowQualityBroadcast(int score, boolean detect) {
        IState iState;
        logD("slqb for ds:" + score + ", dt:" + detect);
        if (!detect || (iState = this.mDisconnectedState) == null || iState != getCurrentState()) {
            Intent qualityIntent = new Intent(WIFI_SCROE_CHANGE);
            qualityIntent.addFlags(67108864);
            qualityIntent.putExtra(EXTRA_ENALE_DATA, detect);
            qualityIntent.putExtra(EXTRA_SCORE, score);
            this.mContext.sendStickyBroadcastAsUser(qualityIntent, UserHandle.ALL);
            return;
        }
        logD("already disconnected, ignore start data detect");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkStateBroadcast(String configkey, boolean valid) {
        if (valid || !this.mWifiStateMachine.isDupDhcp()) {
            Intent netIntent = new Intent(WIFI_NETWORK_CHANGE);
            netIntent.addFlags(67108864);
            netIntent.putExtra(EXTRA_WIFI_INVALID, !valid);
            netIntent.putExtra(EXTRA_IFACE_NAME, this.mInterfaceName);
            logD("sendNetworkStateBroadcast valid=" + valid + " iface=" + this.mInterfaceName);
            this.mContext.sendStickyBroadcastAsUser(netIntent, UserHandle.ALL);
            return;
        }
        logD("[bug#1131400] dupDhcp, wait DHCP retry.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendWifiToDataBroadcast(boolean toData, int score) {
        Intent netIntent = new Intent(WIFI_TO_DATA);
        netIntent.addFlags(67108864);
        netIntent.putExtra(EXTRA_WIFI_TO_DATA, toData);
        boolean wifiValid = false;
        if (score >= 20) {
            wifiValid = true;
        }
        netIntent.putExtra(EXTRA_WIFI_VALID, wifiValid);
        this.mContext.sendStickyBroadcastAsUser(netIntent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendVerifyBroadcast(String configKey) {
        Intent verifyIntent = new Intent(WIFI_ASSISTANT_VERIFY);
        verifyIntent.addFlags(67108864);
        verifyIntent.putExtra(EXTRA_VERIFY_CONFIG, configKey);
        logD("svb");
        this.mContext.sendStickyBroadcastAsUser(verifyIntent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAssistantStatistics(String eventId, String type) {
        setAssistantStatistics(eventId, type, null, null, -127);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAssistantStatistics(String eventId, String type, OppoWifiAssistantRecord cw, OppoWifiAssistantRecord sw, int extra1) {
        if (this.mAutoSwitch) {
            HashMap<String, String> map = new HashMap<>();
            map.put(eventId, type);
            if (cw != null) {
                String currentInfo = makeDumpInfo(cw);
                if (!TextUtils.isEmpty(currentInfo)) {
                    if (extra1 > -127) {
                        currentInfo = currentInfo + NAIRealmData.NAI_REALM_STRING_SEPARATOR + Integer.toString(extra1);
                    }
                    map.put(KEY_CURRENT_WLAN, currentInfo);
                }
            }
            if (sw != null) {
                String selectInfo = makeDumpInfo(sw);
                if (!TextUtils.isEmpty(selectInfo)) {
                    map.put(KEY_SELECT_WLAN, selectInfo);
                }
            }
            OppoSlaManager oppoSlaManager = this.mOppoSlaManager;
            if (oppoSlaManager != null) {
                map.put(SLA_STATUS, String.valueOf(oppoSlaManager.isSlaRuning()));
            }
            OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_ASSISTANT, map, false);
        }
    }

    private String makeDumpInfo(OppoWifiAssistantRecord info) {
        if (info == null) {
            return null;
        }
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(info.mNetid + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mConfigkey + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mRssi + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mScore + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mIs5G + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mIsHistoryLoss + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mNetworkValid + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        for (int i = 0; i < info.mNetQualitys.length; i++) {
            sbuf.append(info.mNetQualitys[i]);
            if (i + 1 != info.mNetQualitys.length) {
                sbuf.append(",");
            }
        }
        return sbuf.toString();
    }

    private class DataStateObserver extends ContentObserver {
        public DataStateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (OppoWifiAssistantStateTraker.this.mTelephonyManager != null) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                oppoWifiAssistantStateTraker.mDataState = oppoWifiAssistantStateTraker.mTelephonyManager.getDataEnabled();
                OppoWifiAssistantStateTraker.this.mOppoSlaManager.setCellState(OppoWifiAssistantStateTraker.this.mDataState);
                if (!OppoWifiAssistantStateTraker.this.mDataState) {
                    long disableData = System.currentTimeMillis() - OppoWifiAssistantStateTraker.this.mAutoConnDataTime;
                    if (OppoWifiAssistantStateTraker.this.mChangedNetwork && OppoWifiAssistantStateTraker.this.mChangedToData) {
                        if (OppoWifiAssistantStateTraker.this.mAutoConnDataTime > 0 && disableData > 0 && disableData < 180000) {
                            OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_MANUAL_LIMIT, OppoWifiAssistantStateTraker.TYPE_DIABLE_DATA);
                            OppoWifiAssistantStateTraker.this.mAutoConnDataTime = 0;
                        }
                        OppoWifiAssistantStateTraker.this.logD("stc dwc2");
                        OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_MANUAL_OPERATE, OppoWifiAssistantStateTraker.TYPE_DIABLE_DATA);
                    }
                    if (!OppoWifiAssistantStateTraker.this.mCanTriggerData) {
                        OppoWifiAssistantStateTraker.this.mAssistantUtils.releaseDataNetwork();
                        OppoWifiAssistantStateTraker.this.mCanTriggerData = true;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(String log) {
        if (sDebug) {
            Log.d(TAG, "" + log);
        }
    }

    private void logE(String log) {
        Log.e(TAG, "" + log);
    }

    public String getSlaAppsTraffic() {
        return this.mOppoSlaManager.getSlaAppsTraffic();
    }

    public String getAvgSpeedAndRtt() {
        return this.mOppoSlaManager.getAvgSpeedAndRtt();
    }

    public void updateNetworkState(int result, String ssid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_UPDATE_NETWORK_STATE, result, 0, ssid));
    }

    public void updateNetworkInfo(int netId, LinkProperties linkProperties) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_CONNECT_NETWORK, netId, 0, linkProperties));
    }

    private String convert2noQuoteString(String str) {
        if (str == null || !str.startsWith("\"") || !str.endsWith("\"")) {
            return str;
        }
        return str.substring(1, str.length() - 1);
    }
}
