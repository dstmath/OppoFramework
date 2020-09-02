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
import android.net.TrafficStats;
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
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.DelayedDiskWrite;
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
public class OppoWifiAssistantStateTraker2 extends StateMachine {
    private static final String ACTION_DETECT_INTERNET = "adnroid.net.wifi.DETECT_INTER";
    private static final String ACTION_DETECT_TRAFFIC = "adnroid.net.wifi.DETECT_TRAFFIC";
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
    public static final int CMD_GATEWAY_CONFLICT = 200713;
    public static final int CMD_INTERNET_MONITOR = 200710;
    public static final int CMD_RSSI_FETCH = 200708;
    public static final int CMD_SPEED_DETECT = 200712;
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
    private static final int EVENT_START_SCAN = 200725;
    private static final int EVENT_UPDATE_NETWORK_STATE = 200714;
    public static final int EVENT_WIFI_DISABLED = 200706;
    private static final double EXP_COEFFICIENT_MONITOR = 0.5d;
    private static final String EXTRA_BLACK_LIST_CAPTIVE_URL = "blacklist_captive_url";
    private static final String EXTRA_DATA_CORE = "data_score";
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
    private static final int INTER_DETECT_REQUEST_CODE = 1;
    private static final String INT_STR = "int=";
    private static final int INVALID_INFO = -127;
    private static final int ITEM_NO_REMIND = 0;
    private static final int ITEM_REMIND_EVERYTIME = 1;
    private static final String KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT = "keep_celluar_network_wifi_assistant";
    private static final String KEY_CURRENT_WLAN = "key_current_wlan";
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
    private static final long SPEED_DETECT_INTERVAL = 300000;
    private static final int SPEED_LOW_MAX_COUNT = 2;
    private static final int SPEED_LOW_THRESHOLD = 10240;
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
    private static final String TAG = "WN_S_2";
    private static final int TCP_AVAIL = 1;
    private static final int TCP_SAMPLE_INTERVAL_LONG = 5000;
    private static final int TCP_SAMPLE_INTERVAL_SHORT = 2000;
    private static final int TCP_SHORT_INTERVAL_LIMIT = 50;
    private static final int TCP_STAT_BASE_COUNT = 1000;
    private static final int TCP_STAT_POOR_COUNT = 1020;
    private static final int TCP_UNAVAIL = 2;
    private static final long TOAST_STAND_OFF_TIME = 14400000;
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
    private static final String WIFI_NETWORK_CHANGE = "android.net.wifi2.WIFI_NETWORK_INVALID";
    private static final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    private static final String WIFI_SCROE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final String WIFI_TO_DATA = "android.net.wifi.WIFI_TO_DATA";
    private static final int WLAN_NETWORK_INVALID = 10;
    /* access modifiers changed from: private */
    public static AlertDialog mAlertDialog = null;
    /* access modifiers changed from: private */
    public static ColorListDialog mDataAlertDialog = null;
    private static Object mDialogLock = new Object();
    /* access modifiers changed from: private */
    public static AlertDialog mSlaDialog = null;
    private static boolean sDebug = true;
    private int DATA_STALL_LOW_COUNT = 6;
    private long mAccessNetTime;
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public OppoWifiAssistantUtils mAssistantUtils;
    /* access modifiers changed from: private */
    public long mAutoConnDataTime;
    /* access modifiers changed from: private */
    public long mAutoConnWlanTime;
    /* access modifiers changed from: private */
    public long mAutoDataToWlanTime;
    /* access modifiers changed from: private */
    public boolean mAutoSwitch = true;
    private int mAutoSwitchDataCount;
    private long mAutoSwitchDataDisableTime;
    private int mAutoSwitchDataIndex;
    private long mAutoSwitchDataTime;
    private long[] mAutoSwitchDataTimes;
    /* access modifiers changed from: private */
    public boolean mAutoSwithToData;
    private CharSequence mAvailableAP;
    private Handler mBroadHandle;
    /* access modifiers changed from: private */
    public String mBroadInfo;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public ConnectivityManager mCM;
    /* access modifiers changed from: private */
    public boolean mCanTriggerData = true;
    /* access modifiers changed from: private */
    public String mCandidateKey;
    /* access modifiers changed from: private */
    public boolean mCaptivePortal = false;
    private int mChangeRssi = 0;
    private int mChangeScore = 0;
    /* access modifiers changed from: private */
    public boolean mChangedNetwork = false;
    /* access modifiers changed from: private */
    public boolean mChangedToData;
    /* access modifiers changed from: private */
    public boolean mClickDialogSwitch = false;
    /* access modifiers changed from: private */
    public State mCompletedState;
    private int mConnFail;
    /* access modifiers changed from: private */
    public int mConnectId = -1;
    /* access modifiers changed from: private */
    public String mConnectKey;
    /* access modifiers changed from: private */
    public String mConnectSSID = null;
    /* access modifiers changed from: private */
    public long mConnectTime;
    /* access modifiers changed from: private */
    public int mConnectedId = -1;
    /* access modifiers changed from: private */
    public State mConnectedState;
    /* access modifiers changed from: private */
    public int mConnectingId = -1;
    /* access modifiers changed from: private */
    public String mConnectingkey;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Network mCurNetwork;
    /* access modifiers changed from: private */
    public VolumeWeightedEMA mCurrentLoss;
    /* access modifiers changed from: private */
    public long mDTxPkts;
    /* access modifiers changed from: private */
    public boolean mDataAutoSwitch = true;
    /* access modifiers changed from: private */
    public int mDataScore = 10;
    /* access modifiers changed from: private */
    public boolean mDataState;
    private DataStateObserver mDataStateObserver;
    private State mDefaultState;
    /* access modifiers changed from: private */
    public int mDetectInterCount;
    /* access modifiers changed from: private */
    public PendingIntent mDetectInterIntent;
    private boolean mDetectNet;
    /* access modifiers changed from: private */
    public int mDisconnectedScanCount = 0;
    /* access modifiers changed from: private */
    public State mDisconnectedState;
    /* access modifiers changed from: private */
    public OppoDualStaNotification mDualStaNotify;
    private String[] mFallbackHttpServers;
    /* access modifiers changed from: private */
    public boolean mFeatureState = true;
    /* access modifiers changed from: private */
    public ArrayList<String> mGatewayConflictBlackList;
    private CharSequence mGoodAvailableAP;
    /* access modifiers changed from: private */
    public int mGotInternetResult;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private State mHandshakeState;
    /* access modifiers changed from: private */
    public int mIndex;
    private boolean mInitAutoConnect = true;
    /* access modifiers changed from: private */
    public State mInitState;
    /* access modifiers changed from: private */
    public boolean mInterChangeToInvalid;
    /* access modifiers changed from: private */
    public int mInterInteval;
    /* access modifiers changed from: private */
    public boolean mInterResult;
    private Handler mInterThread;
    /* access modifiers changed from: private */
    public String mInterfaceName;
    private String[] mInternalServers;
    /* access modifiers changed from: private */
    public boolean mInternetDetecting;
    /* access modifiers changed from: private */
    public int mInternetInvalidCount;
    /* access modifiers changed from: private */
    public int mInternetStandoffTime;
    /* access modifiers changed from: private */
    public boolean mIsFirstPktCntFetchSucceed;
    /* access modifiers changed from: private */
    public int mIsNewConfig;
    /* access modifiers changed from: private */
    public boolean mIsSoftAP = false;
    /* access modifiers changed from: private */
    public String mLastBssid = " ";
    /* access modifiers changed from: private */
    public String mLastConfigkey;
    /* access modifiers changed from: private */
    public long mLastDetectInter;
    /* access modifiers changed from: private */
    public boolean mLastInternetResult;
    /* access modifiers changed from: private */
    public int mLastNetId = -1;
    /* access modifiers changed from: private */
    public RssiPacketCountInfo mLastPktInfo;
    /* access modifiers changed from: private */
    public long mLastRxPkts;
    private long mLastScanTime = 0;
    /* access modifiers changed from: private */
    public long mLastSpeedTime;
    /* access modifiers changed from: private */
    public boolean mLastToData;
    /* access modifiers changed from: private */
    public String mLastToastConfigKey;
    /* access modifiers changed from: private */
    public long mLastToastTime;
    /* access modifiers changed from: private */
    public long mLastTrafficBytes;
    /* access modifiers changed from: private */
    public long mLastTrigDataTime;
    /* access modifiers changed from: private */
    public int mLastTxBad = 0;
    /* access modifiers changed from: private */
    public int mLastTxGood = 0;
    /* access modifiers changed from: private */
    public long mLastTxPkts;
    private long mLastuseTime;
    /* access modifiers changed from: private */
    public int mLinkDetectTimes = 0;
    /* access modifiers changed from: private */
    public int mLinkInterval;
    /* access modifiers changed from: private */
    public double[] mLossArray;
    /* access modifiers changed from: private */
    public double mLossInit = 0.0d;
    /* access modifiers changed from: private */
    public int mLowTrafficeCount = 0;
    /* access modifiers changed from: private */
    public int mLowTrafficeThreshold = 0;
    private boolean mManualConnect = false;
    private int[] mNetQualityArray = {79, 79, 79, 79};
    /* access modifiers changed from: private */
    public int mNetQulityGoodCount;
    /* access modifiers changed from: private */
    public State mNetworkMonitorState;
    private String mNewBssid = " ";
    private String mNewSsid = " ";
    private String mOldBssid = " ";
    private String mOldSsid = " ";
    /* access modifiers changed from: private */
    public int mOldTcpStatus;
    /* access modifiers changed from: private */
    public OppoSlaManager mOppoSlaManager;
    /* access modifiers changed from: private */
    public OppoTcpInfoMonitor mOppoTcpInfoMonitor;
    private String[] mPublicHttpsServers;
    /* access modifiers changed from: private */
    public boolean mResponseGotFromGateway = false;
    /* access modifiers changed from: private */
    public int mRoamdetectCount;
    /* access modifiers changed from: private */
    public int mRssiFetchToken = 0;
    /* access modifiers changed from: private */
    public int mRxPktsLowCount = 0;
    /* access modifiers changed from: private */
    public ScanRequestProxy mScanRequestProxy;
    /* access modifiers changed from: private */
    public boolean mScreenOn;
    /* access modifiers changed from: private */
    public PendingIntent mSpeedDetectIntent;
    /* access modifiers changed from: private */
    public int mSpeedLowCount;
    private SupplicantStateTracker mSupplicantTracker;
    /* access modifiers changed from: private */
    public int mTcpInterval;
    private int mTcpLinkStatus;
    private int mTcpShortIntervalCount;
    /* access modifiers changed from: private */
    public int mTcpStatistics;
    private int[] mTcpstateArray = {0, 0, 0, 0};
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    private String mTestApk = "com.oppo.wifiassistant";
    /* access modifiers changed from: private */
    public int mTrafficInteval;
    /* access modifiers changed from: private */
    public int mTrigScanCount;
    /* access modifiers changed from: private */
    public boolean mTriggerInter = false;
    /* access modifiers changed from: private */
    public boolean mTriggerScan = false;
    /* access modifiers changed from: private */
    public String mUnavailableKey = " ";
    /* access modifiers changed from: private */
    public State mVerifyInternetState;
    /* access modifiers changed from: private */
    public WifiConfigManager mWifiConfigManager;
    private WifiConfigStore mWifiConfigStore;
    /* access modifiers changed from: private */
    public boolean mWifiConnected = false;
    private WifiNative mWifiNative;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;
    private OppoWifiSmartSwitcher mWifiSmartSwitcher;
    /* access modifiers changed from: private */
    public int mWifiState = 1;
    /* access modifiers changed from: private */
    public OppoClientModeImpl2 mWifiStateMachine2;
    /* access modifiers changed from: private */
    public int mWlanIfIndex = 1;
    /* access modifiers changed from: private */
    public int mWlanInvalidThreshold = 40;
    protected final DelayedDiskWrite mWriter;
    /* access modifiers changed from: private */
    public AsyncChannel mWsmChannel;
    /* access modifiers changed from: private */
    public long mdRxPkts;

    static /* synthetic */ int access$10608(OppoWifiAssistantStateTraker2 x0) {
        int i = x0.mDetectInterCount;
        x0.mDetectInterCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$11604(OppoWifiAssistantStateTraker2 x0) {
        int i = x0.mRssiFetchToken + 1;
        x0.mRssiFetchToken = i;
        return i;
    }

    static /* synthetic */ int access$1608(OppoWifiAssistantStateTraker2 x0) {
        int i = x0.mSpeedLowCount;
        x0.mSpeedLowCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$4172(OppoWifiAssistantStateTraker2 x0, int x1) {
        int i = x0.mGotInternetResult & x1;
        x0.mGotInternetResult = i;
        return i;
    }

    static /* synthetic */ int access$4176(OppoWifiAssistantStateTraker2 x0, int x1) {
        int i = x0.mGotInternetResult | x1;
        x0.mGotInternetResult = i;
        return i;
    }

    static /* synthetic */ int access$7008(OppoWifiAssistantStateTraker2 x0) {
        int i = x0.mDisconnectedScanCount;
        x0.mDisconnectedScanCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$7308(OppoWifiAssistantStateTraker2 x0) {
        int i = x0.mInternetInvalidCount;
        x0.mInternetInvalidCount = i + 1;
        return i;
    }

    public OppoWifiAssistantStateTraker2(Context c, OppoClientModeImpl2 wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst, WifiRomUpdateHelper wruh, Handler t, ScanRequestProxy mSrp) {
        super(TAG, t.getLooper());
        boolean z = false;
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
        this.mAutoSwithToData = false;
        this.mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN1;
        this.mSpeedLowCount = 0;
        this.mLastTrafficBytes = 0;
        this.mLastSpeedTime = 0;
        this.mLastToastTime = 0;
        this.mLastToastConfigKey = "";
        this.mGatewayConflictBlackList = new ArrayList<>();
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
        this.mWifiStateMachine2 = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mWifiRomUpdateHelper = wruh;
        this.mHandler = new NetHandler(t.getLooper());
        this.mWriter = new DelayedDiskWrite();
        this.mScanRequestProxy = mSrp;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDetectInterIntent = getPrivateBroadcast(ACTION_DETECT_INTERNET);
        this.mSpeedDetectIntent = getPrivateBroadcast(ACTION_DETECT_TRAFFIC);
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
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass1 */

            public void onChange(boolean selfChange) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                boolean z = true;
                if (Settings.Global.getInt(oppoWifiAssistantStateTraker2.mContext.getContentResolver(), OppoWifiAssistantStateTraker2.WIFI_AUTO_CHANGE_NETWORK, 1) != 1) {
                    z = false;
                }
                boolean unused = oppoWifiAssistantStateTraker2.mDataAutoSwitch = z;
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker22.setDataAutoSwitch(oppoWifiAssistantStateTraker22.mDataAutoSwitch);
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker23.logD(" mdas= " + OppoWifiAssistantStateTraker2.this.mDataAutoSwitch);
            }
        });
        this.mFeatureState = getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue();
        this.mOppoTcpInfoMonitor = new OppoTcpInfoMonitor(this.mContext);
        this.mOppoSlaManager = OppoSlaManager.getInstance(this.mContext);
        this.mAssistantUtils = OppoWifiAssistantUtils.getInstance(this.mContext);
        this.mDualStaNotify = new OppoDualStaNotification(this.mContext);
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
    }

    private void setupNetworkReceiver() {
        IntentFilter netWorkFilter = new IntentFilter();
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_CONNECT);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_STATE);
        netWorkFilter.addAction("android.net.wifi.STATE_CHANGE");
        netWorkFilter.addAction("android.intent.action.SCREEN_ON");
        netWorkFilter.addAction("android.intent.action.SCREEN_OFF");
        netWorkFilter.addAction(ACTION_DETECT_INTERNET);
        netWorkFilter.addAction(ACTION_DETECT_TRAFFIC);
        netWorkFilter.addAction(ACTION_NOTIFY_GATEWAY_MAC);
        netWorkFilter.addAction(WIFI_TO_DATA);
        netWorkFilter.addAction("android.net.conn.NETWORK_CONDITIONS_MEASURED");
        netWorkFilter.addAction(DATA_SCORE_CHANGE);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                WifiConfiguration netConf;
                String action = intent.getAction();
                if (!OppoWifiAssistantStateTraker2.this.mFeatureState || !OppoWifiAssistantStateTraker2.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                    OppoWifiAssistantStateTraker2.this.logD("mfs dis");
                    return;
                }
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker2.logD("AssistReceiver event:" + action);
                if (action.equals(OppoWifiAssistantStateTraker2.ACTION_WIFI_NETWORK_CONNECT) || action.equals(OppoWifiAssistantStateTraker2.ACTION_WIFI_NETWORK_STATE)) {
                    return;
                }
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        String ifName = intent.getStringExtra(OppoWifiAssistantStateTraker2.EXTRA_IFACE_NAME);
                        if (!OppoWifiAssistantStateTraker2.this.mInterfaceName.equals(ifName)) {
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker22.logD("Received NETWORK_STATE_CHANGED_ACTION, mInterfaceName=" + OppoWifiAssistantStateTraker2.this.mInterfaceName + " iface_name=" + ifName);
                            return;
                        }
                        boolean unused = OppoWifiAssistantStateTraker2.this.mWifiConnected = true;
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker2.ACTION_DETECT_INTERNET)) {
                    String ifName2 = intent.getStringExtra(OppoWifiAssistantStateTraker2.EXTRA_IFACE_NAME);
                    if (ifName2 == null || !ifName2.equals(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker23.logD("Received ACTION_DETECT_INTERNET , mInterfaceName=" + OppoWifiAssistantStateTraker2.this.mInterfaceName + " iface_name=" + ifName2);
                        return;
                    }
                    OppoWifiAssistantStateTraker2.this.sendMessage(200710);
                } else if (action.equals(OppoWifiAssistantStateTraker2.ACTION_DETECT_TRAFFIC)) {
                    OppoWifiAssistantStateTraker2.this.sendMessage(200712);
                } else if (action.equals(OppoWifiAssistantStateTraker2.ACTION_NOTIFY_GATEWAY_MAC)) {
                    String ifName3 = intent.getStringExtra(OppoWifiAssistantStateTraker2.EXTRA_IFACE_NAME);
                    if (ifName3 != null && ifName3.equals(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                        String gatewayIp = intent.getStringExtra("gateway_ip");
                        String gatewayMac = intent.getStringExtra("gateway_mac");
                        if (!TextUtils.isEmpty(gatewayMac) && !TextUtils.isEmpty(gatewayIp)) {
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker24.logD(" setIfGatewayIP=" + gatewayIp + " setIfGatewayMac=" + gatewayMac);
                            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setIfGatewayInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, gatewayIp, gatewayMac);
                        }
                        if (OppoWifiAssistantStateTraker2.this.mAssistantUtils.isConflictGatewayIpAndDiffMac()) {
                            OppoWifiAssistantStateTraker2.this.sendMessage(200713);
                        }
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    if (OppoWifiAssistantStateTraker2.this.mChangedToData) {
                        int unused2 = OppoWifiAssistantStateTraker2.this.mInterInteval = 120000;
                    } else {
                        int unused3 = OppoWifiAssistantStateTraker2.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                    }
                    boolean unused4 = OppoWifiAssistantStateTraker2.this.mScreenOn = true;
                    if (OppoWifiAssistantStateTraker2.this.getCurrentState() == OppoWifiAssistantStateTraker2.this.mNetworkMonitorState && OppoWifiAssistantStateTraker2.this.mLastDetectInter != 0 && System.currentTimeMillis() - OppoWifiAssistantStateTraker2.this.mLastDetectInter > ((long) OppoWifiAssistantStateTraker2.this.mInterInteval)) {
                        OppoWifiAssistantStateTraker2.this.setInternetDetectAlarm(1, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    }
                    OppoWifiAssistantStateTraker2.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
                    int unused5 = OppoWifiAssistantStateTraker2.this.mSpeedLowCount = 0;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    int unused6 = OppoWifiAssistantStateTraker2.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                    boolean unused7 = OppoWifiAssistantStateTraker2.this.mScreenOn = false;
                    int unused8 = OppoWifiAssistantStateTraker2.this.mLowTrafficeCount = 0;
                    if (OppoWifiAssistantStateTraker2.this.mWifiConnected) {
                        int unused9 = OppoWifiAssistantStateTraker2.this.mSpeedLowCount = 0;
                        long unused10 = OppoWifiAssistantStateTraker2.this.mLastSpeedTime = System.currentTimeMillis();
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                        long unused11 = oppoWifiAssistantStateTraker25.mLastTrafficBytes = TrafficStats.getRxBytes(oppoWifiAssistantStateTraker25.mInterfaceName) + TrafficStats.getTxBytes(OppoWifiAssistantStateTraker2.this.mInterfaceName);
                        long alarmTime = OppoWifiAssistantStateTraker2.this.mLastSpeedTime + OppoWifiAssistantStateTraker2.SPEED_DETECT_INTERVAL;
                        OppoWifiAssistantStateTraker2.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
                        OppoWifiAssistantStateTraker2.this.mAlarmManager.set(0, alarmTime, OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
                        OppoWifiAssistantStateTraker2.this.logD("ACTION_SCREEN_OFF set alarm for speed detect.");
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker2.WIFI_TO_DATA)) {
                    boolean unused12 = OppoWifiAssistantStateTraker2.this.mChangedToData = intent.getBooleanExtra(OppoWifiAssistantStateTraker2.EXTRA_WIFI_TO_DATA, false);
                    if (OppoWifiAssistantStateTraker2.this.mChangedToData != OppoWifiAssistantStateTraker2.this.mLastToData) {
                        if (!OppoWifiAssistantStateTraker2.this.mChangedToData) {
                            if (!OppoWifiAssistantStateTraker2.this.mInterResult) {
                                if (OppoWifiAssistantStateTraker2.this.mDataAutoSwitch && OppoWifiAssistantStateTraker2.this.mAssistantUtils.getWifiBestScore() <= 10) {
                                    OppoWifiAssistantStateTraker2.this.mAssistantUtils.setupDataNetwork();
                                }
                                if (OppoWifiAssistantStateTraker2.this.mWlanInvalidThreshold == 70 && OppoWifiAssistantStateTraker2.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                                    OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
                                }
                            } else if (OppoWifiAssistantStateTraker2.this.mWifiStateMachine2 != null && OppoWifiAssistantStateTraker2.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                                OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
                            }
                            boolean unused13 = OppoWifiAssistantStateTraker2.this.mCanTriggerData = true;
                            boolean unused14 = OppoWifiAssistantStateTraker2.this.mChangedNetwork = false;
                            int unused15 = OppoWifiAssistantStateTraker2.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker26.sendMessage(OppoWifiAssistantStateTraker2.EVENT_DISMISS_ALERT, oppoWifiAssistantStateTraker26.mLastConfigkey);
                        } else {
                            int unused16 = OppoWifiAssistantStateTraker2.this.mInterInteval = 120000;
                            int unused17 = OppoWifiAssistantStateTraker2.this.mTcpInterval = 5000;
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker27.setInternetDetectAlarm(1, (long) oppoWifiAssistantStateTraker27.mInterInteval);
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker28 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker28.logD("mcn=" + OppoWifiAssistantStateTraker2.this.mChangedNetwork + ",mcds=" + OppoWifiAssistantStateTraker2.this.mClickDialogSwitch + ",micti=" + OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid + ",mir=" + OppoWifiAssistantStateTraker2.this.mInterResult);
                            if (OppoWifiAssistantStateTraker2.this.mChangedNetwork && !OppoWifiAssistantStateTraker2.this.mClickDialogSwitch && ((!OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid && OppoWifiAssistantStateTraker2.this.mInterResult) || OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid)) {
                                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker29 = OppoWifiAssistantStateTraker2.this;
                                oppoWifiAssistantStateTraker29.sendMessage(OppoWifiAssistantStateTraker2.EVENT_SHOW_ALERT, oppoWifiAssistantStateTraker29.mLastConfigkey);
                            }
                            if (OppoWifiAssistantStateTraker2.this.mClickDialogSwitch) {
                                boolean unused18 = OppoWifiAssistantStateTraker2.this.mAutoSwithToData = false;
                            } else {
                                boolean unused19 = OppoWifiAssistantStateTraker2.this.mAutoSwithToData = true;
                            }
                            boolean unused20 = OppoWifiAssistantStateTraker2.this.mClickDialogSwitch = false;
                            OppoWifiAssistantStateTraker2.this.detectSwitchDataFrequence();
                            if (OppoWifiAssistantStateTraker2.this.mChangedNetwork) {
                                long unused21 = OppoWifiAssistantStateTraker2.this.mAutoConnDataTime = System.currentTimeMillis();
                                long unused22 = OppoWifiAssistantStateTraker2.this.mAutoConnWlanTime = 0;
                                long unused23 = OppoWifiAssistantStateTraker2.this.mAutoDataToWlanTime = 0;
                                OppoWifiAssistantRecord curTodataRecord = OppoWifiAssistantStateTraker2.this.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker2.this.mLastConfigkey);
                                int validRecordCount = OppoWifiAssistantStateTraker2.this.mAssistantUtils.getSortNetworkRecords().size();
                                OppoWifiAssistantStateTraker2.this.logD("stc wda");
                                OppoWifiAssistantStateTraker2.this.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker2.TYPE_WLAN_TO_DATA, curTodataRecord, null, validRecordCount);
                            }
                        }
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker210 = OppoWifiAssistantStateTraker2.this;
                        boolean unused24 = oppoWifiAssistantStateTraker210.mLastToData = oppoWifiAssistantStateTraker210.mChangedToData;
                    }
                } else if (action.equals("android.net.conn.NETWORK_CONDITIONS_MEASURED")) {
                    if (intent.getIntExtra("extra_connectivity_type", -1) == 1 && intent.getBooleanExtra("extra_is_captive_portal", false)) {
                        String ifName4 = intent.getStringExtra(OppoWifiAssistantStateTraker2.EXTRA_IFACE_NAME);
                        if (!OppoWifiAssistantStateTraker2.this.mInterfaceName.equals(ifName4)) {
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker211 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker211.logD("Received ACTION_NETWORK_CONDITIONS_MEASURED, mInterfaceName=" + OppoWifiAssistantStateTraker2.this.mInterfaceName + " iface_name=" + ifName4);
                            return;
                        }
                        OppoWifiAssistantStateTraker2.this.logD("Received ACTION_NETWORK_CONDITIONS_MEASURED, wlan is captive portal.");
                        if (OppoWifiAssistantStateTraker2.this.mIsSoftAP && OppoWifiAssistantStateTraker2.this.isThirdAppOperate()) {
                            return;
                        }
                        if (OppoWifiAssistantStateTraker2.this.mCaptivePortal) {
                            OppoWifiAssistantStateTraker2.this.logD("is cp.");
                            return;
                        }
                        WifiInfo curWifiInfo = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.syncRequestConnectionInfo();
                        String netStateSsid = intent.getStringExtra("extra_ssid");
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker212 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker212.logD("cpnss: " + netStateSsid + ", info: " + curWifiInfo);
                        if (curWifiInfo != null && netStateSsid != null && curWifiInfo.getSSID().equals(netStateSsid) && (netConf = OppoWifiAssistantStateTraker2.this.getWifiConfig(netStateSsid, curWifiInfo.getBSSID())) != null && netConf.networkId == OppoWifiAssistantStateTraker2.this.mConnectedId) {
                            boolean unused25 = OppoWifiAssistantStateTraker2.this.mCaptivePortal = true;
                            if ((OppoWifiAssistantStateTraker2.this.mGotInternetResult & 4) != 4) {
                                OppoWifiAssistantStateTraker2.this.sendVerifyBroadcast(netConf.configKey(false));
                                if (OppoWifiAssistantStateTraker2.this.mAutoSwitch && OppoWifiAssistantStateTraker2.this.mFeatureState && OppoWifiAssistantStateTraker2.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                                    OppoWifiAssistantStateTraker2.this.sendNetworkStateBroadcast(netConf.configKey(false), false);
                                }
                                int blackListCapUrl = 0;
                                if (intent.getBooleanExtra(OppoWifiAssistantStateTraker2.EXTRA_BLACK_LIST_CAPTIVE_URL, false)) {
                                    blackListCapUrl = 1;
                                }
                                OppoWifiAssistantStateTraker2.this.mHandler.sendMessage(OppoWifiAssistantStateTraker2.this.mHandler.obtainMessage(OppoWifiAssistantStateTraker2.EVENT_CAPTIVE_PORTAL, 0, blackListCapUrl, netConf.configKey(false)));
                            }
                        }
                    }
                } else if (action.equals(OppoWifiAssistantStateTraker2.DATA_SCORE_CHANGE)) {
                    int dataScore = intent.getIntExtra(OppoWifiAssistantStateTraker2.EXTRA_DATA_CORE, 10);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker213 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker213.logD("new dataScore=" + dataScore);
                    int unused26 = OppoWifiAssistantStateTraker2.this.mDataScore = dataScore;
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
        return PendingIntent.getBroadcast(this.mContext, 1, intent, 134217728);
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
            OppoWifiAssistantStateTraker2.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            String str = null;
            switch (msg.what) {
                case 131126:
                case 151553:
                    int unused = OppoWifiAssistantStateTraker2.this.mConnectId = Integer.valueOf(msg.arg1).intValue();
                    int unused2 = OppoWifiAssistantStateTraker2.this.mIsNewConfig = Integer.valueOf(msg.arg2).intValue();
                    WifiConfiguration config = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mConnectId);
                    String unused3 = OppoWifiAssistantStateTraker2.this.mConnectKey = config != null ? config.configKey(false) : null;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    String unused4 = oppoWifiAssistantStateTraker22.mConnectSSID = str;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker23.logD("mConnectId= " + OppoWifiAssistantStateTraker2.this.mConnectId + ", minc= " + OppoWifiAssistantStateTraker2.this.mIsNewConfig);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker24.addOrUpdateRecord(oppoWifiAssistantStateTraker24.mConnectKey);
                    break;
                case 131211:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*{ENCODED_INT: 147463}*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                    WifiConfiguration conFail = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mConnectingId);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                    if (conFail != null) {
                        str = conFail.configKey(false);
                    }
                    String unused5 = oppoWifiAssistantStateTraker25.mConnectingkey = str;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker26.updateRecordConnectFail(oppoWifiAssistantStateTraker26.mConnectingkey);
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                    int unused6 = OppoWifiAssistantStateTraker2.this.mRoamdetectCount = 0;
                    int unused7 = OppoWifiAssistantStateTraker2.this.mLastNetId = msg.arg1;
                    String unused8 = OppoWifiAssistantStateTraker2.this.mLastBssid = (String) msg.obj;
                    WifiConfiguration config2 = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mLastNetId);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
                    if (config2 != null) {
                        str = config2.configKey(false);
                    }
                    String unused9 = oppoWifiAssistantStateTraker27.mLastConfigkey = str;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker28 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker28.transitionTo(oppoWifiAssistantStateTraker28.mCompletedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    StateChangeResult stateReult = (StateChangeResult) msg.obj;
                    int unused10 = OppoWifiAssistantStateTraker2.this.mConnectingId = stateReult.networkId;
                    WifiConfiguration config3 = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mConnectingId);
                    String unused11 = OppoWifiAssistantStateTraker2.this.mConnectingkey = config3 != null ? config3.configKey(false) : null;
                    if (stateReult.state != SupplicantState.DISCONNECTED) {
                        if (stateReult.state == SupplicantState.COMPLETED) {
                            OppoWifiAssistantStateTraker2.this.mAssistantUtils.updateArpParams(true);
                            WifiInfo curWifiInfo = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.syncRequestConnectionInfo();
                            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, curWifiInfo);
                            if (curWifiInfo != null && !TextUtils.isEmpty(curWifiInfo.getSSID())) {
                                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker29 = OppoWifiAssistantStateTraker2.this;
                                oppoWifiAssistantStateTraker29.logD("set SSID of wlan1:" + curWifiInfo.getSSID());
                                Settings.System.putString(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), "slave_wifi_ssid", curWifiInfo.getSSID());
                                break;
                            }
                        }
                    } else {
                        OppoWifiAssistantStateTraker2.this.mAssistantUtils.updateArpParams(false);
                        OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, null);
                        OppoWifiAssistantStateTraker2.this.logD("clear SSID of wlan1");
                        Settings.System.putString(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), "slave_wifi_ssid", "");
                        break;
                    }
                    break;
                case 200705:
                    int unused12 = OppoWifiAssistantStateTraker2.this.mLastNetId = msg.arg1;
                    String unused13 = OppoWifiAssistantStateTraker2.this.mLastBssid = (String) msg.obj;
                    WifiConfiguration config4 = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mLastNetId);
                    String unused14 = OppoWifiAssistantStateTraker2.this.mLastConfigkey = config4 != null ? config4.configKey(false) : null;
                    if (msg.arg2 != 2) {
                        if (msg.arg2 == 0) {
                            if (OppoWifiAssistantStateTraker2.this.mUnavailableKey != null && OppoWifiAssistantStateTraker2.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey)) {
                                OppoWifiAssistantStateTraker2.this.dismissDialog(0);
                            }
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker210 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker210.updateRecordDisableState(oppoWifiAssistantStateTraker210.mLastConfigkey);
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker211 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker211.transitionTo(oppoWifiAssistantStateTraker211.mDisconnectedState);
                            break;
                        }
                    } else {
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker212 = OppoWifiAssistantStateTraker2.this;
                        if (config4 != null) {
                            str = config4.SSID;
                        }
                        String unused15 = oppoWifiAssistantStateTraker212.mConnectSSID = str;
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker213 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker213.transitionTo(oppoWifiAssistantStateTraker213.mConnectedState);
                        break;
                    }
                    break;
                case 200706:
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker214 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker214.transitionTo(oppoWifiAssistantStateTraker214.mInitState);
                    break;
            }
            return true;
        }
    }

    class InitState extends State {
        InitState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2.this.logD(getName());
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            int unused = oppoWifiAssistantStateTraker2.mWifiState = oppoWifiAssistantStateTraker2.mWifiStateMachine2.syncGetWifiState();
            int unused2 = OppoWifiAssistantStateTraker2.this.mLastTxBad = 0;
            int unused3 = OppoWifiAssistantStateTraker2.this.mLastTxGood = 0;
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanChangeToCell(OppoWifiAssistantStateTraker2.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanShowDialog(OppoWifiAssistantStateTraker2.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiScore(OppoWifiAssistantStateTraker2.this.mInterfaceName, -1);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            int i = msg.what;
            if (i == 200706) {
                OppoWifiAssistantStateTraker2.this.logD("it is Initstate, do not handle!");
                return true;
            } else if (i == 200712) {
                return true;
            } else {
                if (i != OppoWifiAssistantStateTraker2.EVENT_START_SCAN) {
                    return false;
                }
                OppoWifiAssistantStateTraker2.this.logD("InitState Dual STA startScan...");
                OppoWifiAssistantStateTraker2.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                OppoWifiAssistantStateTraker2.access$7008(OppoWifiAssistantStateTraker2.this);
                if (OppoWifiAssistantStateTraker2.this.mDisconnectedScanCount > 3) {
                    return false;
                }
                OppoWifiAssistantStateTraker2.this.sendMessageDelayed(OppoWifiAssistantStateTraker2.EVENT_START_SCAN, OppoWifiAssistantStateTraker2.STAIC_SCAN_RESULT_AGE);
                return false;
            }
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2.this.logD(getName());
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD("mLastNetId = " + OppoWifiAssistantStateTraker2.this.mLastNetId + ", mConnectedId = " + OppoWifiAssistantStateTraker2.this.mConnectedId + ", mLastConfigkey = " + OppoWifiAssistantStateTraker2.this.mLastConfigkey);
            OppoWifiAssistantStateTraker2.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker2.this.mDetectInterIntent);
            OppoWifiAssistantStateTraker2.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
            if (OppoWifiAssistantStateTraker2.this.mLastNetId == OppoWifiAssistantStateTraker2.this.mConnectedId && OppoWifiAssistantStateTraker2.this.mLastNetId != -1 && OppoWifiAssistantStateTraker2.this.mConnectingId == OppoWifiAssistantStateTraker2.this.mConnectedId) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker22.updateRecordUseTime(oppoWifiAssistantStateTraker22.mLastConfigkey);
            }
            int unused = OppoWifiAssistantStateTraker2.this.mLastNetId = -1;
            String unused2 = OppoWifiAssistantStateTraker2.this.mLastBssid = " ";
            String unused3 = OppoWifiAssistantStateTraker2.this.mUnavailableKey = " ";
            boolean unused4 = OppoWifiAssistantStateTraker2.this.mAutoSwithToData = false;
            boolean unused5 = OppoWifiAssistantStateTraker2.this.mInterResult = false;
            int unused6 = OppoWifiAssistantStateTraker2.this.mInternetInvalidCount = 0;
            boolean unused7 = OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid = false;
            RssiPacketCountInfo unused8 = OppoWifiAssistantStateTraker2.this.mLastPktInfo = null;
            int unused9 = OppoWifiAssistantStateTraker2.this.mGotInternetResult = 0;
            long unused10 = OppoWifiAssistantStateTraker2.this.mLastDetectInter = 0;
            int unused11 = OppoWifiAssistantStateTraker2.this.mTrigScanCount = 0;
            long unused12 = OppoWifiAssistantStateTraker2.this.mDTxPkts = 0;
            long unused13 = OppoWifiAssistantStateTraker2.this.mdRxPkts = 0;
            boolean unused14 = OppoWifiAssistantStateTraker2.this.mIsSoftAP = false;
            boolean unused15 = OppoWifiAssistantStateTraker2.this.mCaptivePortal = false;
            boolean unused16 = OppoWifiAssistantStateTraker2.this.mResponseGotFromGateway = false;
            boolean unused17 = OppoWifiAssistantStateTraker2.this.mWifiConnected = false;
            boolean unused18 = OppoWifiAssistantStateTraker2.this.mIsFirstPktCntFetchSucceed = true;
            OppoWifiAssistantStateTraker2.this.resetAutoSwitchDataDetect();
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanChangeToCell(OppoWifiAssistantStateTraker2.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanShowDialog(OppoWifiAssistantStateTraker2.this.mInterfaceName, true);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiScore(OppoWifiAssistantStateTraker2.this.mInterfaceName, -1);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setIfGatewayInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, "", "");
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.updateArpParams(false);
            OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setWifiConnectionState(OppoWifiAssistantStateTraker2.this.mWlanIfIndex, false);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, null);
            if (!OppoWifiAssistantStateTraker2.this.mScreenOn) {
                OppoWifiAssistantStateTraker2.this.logD("disableDualSta when wlan1 disconnected and screen is off");
                OppoWifiAssistantStateTraker2.this.mAssistantUtils.disableDualSta();
            }
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker23.sendNetworkStateBroadcast(oppoWifiAssistantStateTraker23.mLastConfigkey, false);
            if (OppoWifiAssistantStateTraker2.this.mDualStaNotify != null) {
                OppoWifiAssistantStateTraker2.this.mDualStaNotify.clearAllNotifying();
            }
            if (OppoWifiAssistantStateTraker2.this.mScreenOn) {
                OppoWifiAssistantStateTraker2.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
            }
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            int i = msg.what;
            if (i == 200712) {
                return true;
            }
            if (i != OppoWifiAssistantStateTraker2.EVENT_START_SCAN) {
                return false;
            }
            OppoWifiAssistantStateTraker2.this.logD("DisconnectedState Dual STA startScan...");
            OppoWifiAssistantStateTraker2.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
            OppoWifiAssistantStateTraker2.access$7008(OppoWifiAssistantStateTraker2.this);
            if (OppoWifiAssistantStateTraker2.this.mDisconnectedScanCount > 3) {
                return true;
            }
            OppoWifiAssistantStateTraker2.this.sendMessageDelayed(OppoWifiAssistantStateTraker2.EVENT_START_SCAN, OppoWifiAssistantStateTraker2.STAIC_SCAN_RESULT_AGE);
            return true;
        }
    }

    class HandshakeState extends State {
        HandshakeState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
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
            OppoWifiAssistantStateTraker2.this.logD(getName());
            int unused = OppoWifiAssistantStateTraker2.this.mGotInternetResult = 0;
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanChangeToCell(OppoWifiAssistantStateTraker2.this.mInterfaceName, false);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setCanShowDialog(OppoWifiAssistantStateTraker2.this.mInterfaceName, false);
            int unused2 = OppoWifiAssistantStateTraker2.this.mDisconnectedScanCount = 0;
            OppoWifiAssistantStateTraker2.this.removeMessages(OppoWifiAssistantStateTraker2.EVENT_START_SCAN);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            int i = msg.what;
            if (i == 147459) {
                int unused = OppoWifiAssistantStateTraker2.this.mRoamdetectCount = 0;
                String unused2 = OppoWifiAssistantStateTraker2.this.mLastBssid = (String) msg.obj;
                if (OppoWifiAssistantStateTraker2.this.mLastNetId != msg.arg1) {
                    int unused3 = OppoWifiAssistantStateTraker2.this.mLastNetId = msg.arg1;
                    WifiConfiguration config = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mLastNetId);
                    String unused4 = OppoWifiAssistantStateTraker2.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                }
            } else if (i == 200705) {
                String unused5 = OppoWifiAssistantStateTraker2.this.mLastBssid = (String) msg.obj;
                int unused6 = OppoWifiAssistantStateTraker2.this.mLastNetId = msg.arg1;
                int completestate = msg.arg2;
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker22.logD("cptst = " + completestate + ",msg.arg1= " + msg.arg1);
                WifiConfiguration config2 = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mLastNetId);
                if (config2 != null) {
                    String unused7 = OppoWifiAssistantStateTraker2.this.mLastConfigkey = config2.configKey(false);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker23.logD("cptst = " + completestate + ",mLastConfigkey= " + OppoWifiAssistantStateTraker2.this.mLastConfigkey + ",mLastNetId= " + OppoWifiAssistantStateTraker2.this.mLastNetId + ",mConnectSSID= " + OppoWifiAssistantStateTraker2.this.mConnectSSID);
                    if (completestate == 0) {
                        if (OppoWifiAssistantStateTraker2.this.mUnavailableKey != null && OppoWifiAssistantStateTraker2.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey)) {
                            OppoWifiAssistantStateTraker2.this.dismissDialog(0);
                        }
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker24.updateRecordDisableState(oppoWifiAssistantStateTraker24.mLastConfigkey);
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker25.transitionTo(oppoWifiAssistantStateTraker25.mDisconnectedState);
                    } else if (completestate == 1) {
                        String unused8 = OppoWifiAssistantStateTraker2.this.mConnectSSID = config2.SSID;
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker26.transitionTo(oppoWifiAssistantStateTraker26.mVerifyInternetState);
                    } else if (completestate == 2) {
                        String unused9 = OppoWifiAssistantStateTraker2.this.mConnectSSID = config2.SSID;
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker27.transitionTo(oppoWifiAssistantStateTraker27.mConnectedState);
                    }
                }
            } else if (i != 200713) {
                return false;
            } else {
                OppoWifiAssistantStateTraker2.this.logD("CMD_GATEWAY_CONFLICT");
                OppoWifiAssistantStateTraker2.this.handleGatewayConflict();
            }
            return true;
        }
    }

    class VerifyInternetState extends State {
        VerifyInternetState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2.this.logD(getName());
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            int unused = oppoWifiAssistantStateTraker2.mConnectedId = oppoWifiAssistantStateTraker2.mLastNetId;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker22.logD("VerifyInternetState mConnectedId= " + OppoWifiAssistantStateTraker2.this.mConnectedId);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            if (msg.what != 200707) {
                return false;
            }
            if (!OppoWifiAssistantStateTraker2.this.mInterResult) {
                return true;
            }
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker22.transitionTo(oppoWifiAssistantStateTraker22.mNetworkMonitorState);
            return true;
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2.this.logD(getName());
            boolean unused = OppoWifiAssistantStateTraker2.this.mWifiConnected = true;
            long unused2 = OppoWifiAssistantStateTraker2.this.mConnectTime = System.currentTimeMillis();
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            int unused3 = oppoWifiAssistantStateTraker2.mConnectedId = oppoWifiAssistantStateTraker2.mLastNetId;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker22.logD("ConnectedState mLastConfigkey= " + OppoWifiAssistantStateTraker2.this.mLastConfigkey + ", mConnectedId= " + OppoWifiAssistantStateTraker2.this.mConnectedId);
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker23.updateRecordConCount(oppoWifiAssistantStateTraker23.mLastConfigkey);
            if (OppoWifiAssistantStateTraker2.this.mCandidateKey.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey)) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                long unused4 = oppoWifiAssistantStateTraker24.mAutoConnWlanTime = oppoWifiAssistantStateTraker24.mConnectTime;
                long unused5 = OppoWifiAssistantStateTraker2.this.mAutoConnDataTime = 0;
                long unused6 = OppoWifiAssistantStateTraker2.this.mAutoDataToWlanTime = 0;
                OppoWifiAssistantStateTraker2.this.logD("stc wwa");
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker25.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker2.TYPE_WLAN_TO_WLAN, oppoWifiAssistantStateTraker25.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker2.this.mLastConfigkey), null, -127);
                String unused7 = OppoWifiAssistantStateTraker2.this.mCandidateKey = "";
            }
            int unused8 = OppoWifiAssistantStateTraker2.this.mTcpStatistics = 0;
            int unused9 = OppoWifiAssistantStateTraker2.this.mTcpInterval = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
            OppoWifiAssistantStateTraker2.this.mOppoTcpInfoMonitor.resetTcpLinkStatus();
            OppoWifiAssistantStateTraker2.this.removeMessages(200711);
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker26.sendMessageDelayed(oppoWifiAssistantStateTraker26.obtainMessage(200711), (long) OppoWifiAssistantStateTraker2.this.mTcpInterval);
            WifiInfo curWifiInfo = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.syncRequestConnectionInfo();
            WifiInfo otherWifiInfo = OppoWifiAssistantStateTraker2.this.mAssistantUtils.getOtherIfWifiInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName);
            OppoWifiAssistantStateTraker2.this.mAssistantUtils.setWifiInfo(OppoWifiAssistantStateTraker2.this.mInterfaceName, curWifiInfo);
            synchronized (OppoWifiAssistantStateTraker2.this.mGatewayConflictBlackList) {
                if (otherWifiInfo != null) {
                    if (!OppoWifiAssistantStateTraker2.this.mGatewayConflictBlackList.contains(otherWifiInfo.getBSSID())) {
                        OppoWifiAssistantStateTraker2.this.mGatewayConflictBlackList.clear();
                    }
                }
            }
            if (OppoWifiAssistantStateTraker2.this.mAssistantUtils.isConflictGatewayIpAndDiffMac()) {
                OppoWifiAssistantStateTraker2.this.handleGatewayConflict();
            }
            OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setWifiConnectionState(OppoWifiAssistantStateTraker2.this.mWlanIfIndex, true);
            if (OppoWifiAssistantStateTraker2.this.mLastToastConfigKey == null || !OppoWifiAssistantStateTraker2.this.mLastToastConfigKey.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey) || System.currentTimeMillis() - OppoWifiAssistantStateTraker2.this.mLastToastTime >= OppoWifiAssistantStateTraker2.TOAST_STAND_OFF_TIME) {
                Toast.makeText(OppoWifiAssistantStateTraker2.this.mContext, "" + ((Object) OppoWifiAssistantStateTraker2.this.mContext.getText(201653634)), 1).show();
                long unused10 = OppoWifiAssistantStateTraker2.this.mLastToastTime = System.currentTimeMillis();
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
                String unused11 = oppoWifiAssistantStateTraker27.mLastToastConfigKey = oppoWifiAssistantStateTraker27.mLastConfigkey;
            }
            if (!(otherWifiInfo == null || otherWifiInfo.getFrequency() == curWifiInfo.getFrequency() || ((!otherWifiInfo.is24GHz() || !curWifiInfo.is24GHz()) && (!otherWifiInfo.is5GHz() || !curWifiInfo.is5GHz())))) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker28 = OppoWifiAssistantStateTraker2.this;
                oppoWifiAssistantStateTraker28.logE("wlan1 connected to the same band and different channel!! curWifiInfo:[" + curWifiInfo + "] otherWifiInfo:[" + otherWifiInfo + "]");
            }
            if (!(curWifiInfo == null || otherWifiInfo == null)) {
                if (curWifiInfo.is24GHz() && otherWifiInfo.is24GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq24gAnd24gCount();
                } else if (curWifiInfo.is24GHz() && otherWifiInfo.is5GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq24gAnd5gCount();
                } else if (!curWifiInfo.is5GHz() || !otherWifiInfo.is24GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq5gAnd5gCount();
                } else {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq5gAnd24gCount();
                }
            }
            if (!(curWifiInfo == null || otherWifiInfo == null)) {
                if (curWifiInfo.is24GHz() && otherWifiInfo.is24GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq24gAnd24gCount();
                } else if (curWifiInfo.is24GHz() && otherWifiInfo.is5GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq24gAnd5gCount();
                } else if (!curWifiInfo.is5GHz() || !otherWifiInfo.is24GHz()) {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq5gAnd5gCount();
                } else {
                    OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setFreq5gAnd24gCount();
                }
            }
            if (OppoWifiAssistantStateTraker2.this.mDualStaNotify != null && OppoWifiAssistantStateTraker2.this.mDualStaNotify.isDualStaEnabled() && OppoWifiAssistantStateTraker2.this.mDualStaNotify.isDualStaFirstTakeEffect()) {
                OppoWifiAssistantStateTraker2.this.mDualStaNotify.showFirstTakeEffectNotification();
                OppoWifiAssistantStateTraker2.this.mDualStaNotify.setDualStaFirstTakeEffectFlag();
            }
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case 200705:
                    String unused = OppoWifiAssistantStateTraker2.this.mLastBssid = (String) msg.obj;
                    int unused2 = OppoWifiAssistantStateTraker2.this.mLastNetId = msg.arg1;
                    int connectedstate = msg.arg2;
                    WifiConfiguration config = OppoWifiAssistantStateTraker2.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker2.this.mLastNetId);
                    String unused3 = OppoWifiAssistantStateTraker2.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker22.logD("cctst= " + connectedstate);
                    if (connectedstate != 0) {
                        return true;
                    }
                    if (OppoWifiAssistantStateTraker2.this.mUnavailableKey != null && OppoWifiAssistantStateTraker2.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey)) {
                        OppoWifiAssistantStateTraker2.this.dismissDialog(0);
                    }
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker23.updateRecordDisableState(oppoWifiAssistantStateTraker23.mLastConfigkey);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker24.transitionTo(oppoWifiAssistantStateTraker24.mDisconnectedState);
                    return true;
                case 200707:
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker25.transitionTo(oppoWifiAssistantStateTraker25.mNetworkMonitorState);
                    return true;
                case 200711:
                    if (!OppoWifiAssistantStateTraker2.this.needToDetectTcpStatus()) {
                        return true;
                    }
                    boolean unused4 = OppoWifiAssistantStateTraker2.this.detectTcpStatus();
                    return true;
                case 200712:
                    return true;
                default:
                    return false;
            }
        }
    }

    class NetworkMonitorState extends State {
        NetworkMonitorState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker2.logD(getName() + ",mLastConfigkey=" + OppoWifiAssistantStateTraker2.this.mLastConfigkey);
            long unused = OppoWifiAssistantStateTraker2.this.mLastTrigDataTime = System.currentTimeMillis();
            if (OppoWifiAssistantStateTraker2.this.mInterfaceName != null) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                long unused2 = oppoWifiAssistantStateTraker22.mLastTxPkts = oppoWifiAssistantStateTraker22.mOppoSlaManager.getWlanTcpTxPackets(1);
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                long unused3 = oppoWifiAssistantStateTraker23.mLastRxPkts = oppoWifiAssistantStateTraker23.mOppoSlaManager.getWlanTcpRxPackets(1);
            } else {
                long unused4 = OppoWifiAssistantStateTraker2.this.mLastTxPkts = 0;
                long unused5 = OppoWifiAssistantStateTraker2.this.mLastRxPkts = 0;
            }
            int unused6 = OppoWifiAssistantStateTraker2.this.mRxPktsLowCount = 0;
            long unused7 = OppoWifiAssistantStateTraker2.this.mDTxPkts = 0;
            long unused8 = OppoWifiAssistantStateTraker2.this.mdRxPkts = 0;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
            int unused9 = oppoWifiAssistantStateTraker24.mLinkInterval = oppoWifiAssistantStateTraker24.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", 5000).intValue();
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
            int unused10 = oppoWifiAssistantStateTraker25.mTrafficInteval = oppoWifiAssistantStateTraker25.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL", Integer.valueOf((int) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL)).intValue();
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
            int unused11 = oppoWifiAssistantStateTraker26.mLowTrafficeThreshold = oppoWifiAssistantStateTraker26.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_TRAFFICE_THRESHOLD", 10).intValue();
            int unused12 = OppoWifiAssistantStateTraker2.this.mTcpInterval = 5000;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
            int unused13 = oppoWifiAssistantStateTraker27.mWlanInvalidThreshold = oppoWifiAssistantStateTraker27.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", 40).intValue();
            int unused14 = OppoWifiAssistantStateTraker2.this.mRoamdetectCount = 0;
            boolean unused15 = OppoWifiAssistantStateTraker2.this.mCanTriggerData = true;
            boolean unused16 = OppoWifiAssistantStateTraker2.this.mTriggerInter = false;
            boolean unused17 = OppoWifiAssistantStateTraker2.this.mChangedNetwork = false;
            boolean unused18 = OppoWifiAssistantStateTraker2.this.mClickDialogSwitch = false;
            int unused19 = OppoWifiAssistantStateTraker2.this.mDetectInterCount = 0;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker28 = OppoWifiAssistantStateTraker2.this;
            boolean unused20 = oppoWifiAssistantStateTraker28.mLastInternetResult = oppoWifiAssistantStateTraker28.mInterResult;
            int unused21 = OppoWifiAssistantStateTraker2.this.mLinkDetectTimes = 0;
            int unused22 = OppoWifiAssistantStateTraker2.this.mInternetInvalidCount = 0;
            boolean unused23 = OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid = false;
            long unused24 = OppoWifiAssistantStateTraker2.this.mLastDetectInter = 0;
            int unused25 = OppoWifiAssistantStateTraker2.this.mInternetStandoffTime = 0;
            int unused26 = OppoWifiAssistantStateTraker2.this.mTrigScanCount = 0;
            int unused27 = OppoWifiAssistantStateTraker2.this.mNetQulityGoodCount = 0;
            int unused28 = OppoWifiAssistantStateTraker2.this.mLowTrafficeCount = 0;
            int unused29 = OppoWifiAssistantStateTraker2.this.mOldTcpStatus = 0;
            for (int lossInt = 0; lossInt < OppoWifiAssistantStateTraker2.this.mLossArray.length; lossInt++) {
                OppoWifiAssistantStateTraker2.this.mLossArray[lossInt] = OppoWifiAssistantStateTraker2.this.mLossInit;
            }
            int unused30 = OppoWifiAssistantStateTraker2.this.mIndex = 0;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker29 = OppoWifiAssistantStateTraker2.this;
            VolumeWeightedEMA unused31 = oppoWifiAssistantStateTraker29.mCurrentLoss = new VolumeWeightedEMA(0.5d);
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker210 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker210.sendMessage(oppoWifiAssistantStateTraker210.obtainMessage(200708, OppoWifiAssistantStateTraker2.access$11604(oppoWifiAssistantStateTraker210), 0));
            OppoWifiAssistantStateTraker2.this.removeMessages(200709);
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker211 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker211.sendMessageDelayed(oppoWifiAssistantStateTraker211.obtainMessage(200709), RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker212 = OppoWifiAssistantStateTraker2.this;
            oppoWifiAssistantStateTraker212.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker2.TYPE_MONITOR_WLAN, oppoWifiAssistantStateTraker212.mAssistantUtils.getNetworkRecord(OppoWifiAssistantStateTraker2.this.mLastConfigkey), null, -127);
        }

        public boolean processMessage(Message msg) {
            long speed;
            switch (msg.what) {
                case 151573:
                    RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
                    if (info != null) {
                        if (OppoWifiAssistantStateTraker2.this.mWifiConnected) {
                            OppoWifiAssistantStateTraker2.this.updateRecordLinkQuality(info);
                            RssiPacketCountInfo unused = OppoWifiAssistantStateTraker2.this.mLastPktInfo = info;
                            break;
                        } else {
                            OppoWifiAssistantStateTraker2.this.logD("noconnect");
                            break;
                        }
                    }
                    break;
                case 151574:
                    OppoWifiAssistantStateTraker2.this.logD("RSSI_FETCH_FAILED");
                    break;
                case 200707:
                    break;
                case 200708:
                    if (msg.arg1 == OppoWifiAssistantStateTraker2.this.mRssiFetchToken) {
                        OppoWifiAssistantStateTraker2.this.mWsmChannel.sendMessage(151572, 1);
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker2.sendMessageDelayed(oppoWifiAssistantStateTraker2.obtainMessage(200708, OppoWifiAssistantStateTraker2.access$11604(oppoWifiAssistantStateTraker2), 0), (long) OppoWifiAssistantStateTraker2.this.mLinkInterval);
                        break;
                    }
                    break;
                case 200709:
                    OppoWifiAssistantStateTraker2.this.detectTraffic();
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker22.sendMessageDelayed(oppoWifiAssistantStateTraker22.obtainMessage(200709), (long) OppoWifiAssistantStateTraker2.this.mTrafficInteval);
                    break;
                case 200710:
                    OppoWifiAssistantStateTraker2.this.detectInternet();
                    if (OppoWifiAssistantStateTraker2.this.mChangedToData || !OppoWifiAssistantStateTraker2.this.mInterResult) {
                        if (!OppoWifiAssistantStateTraker2.this.mAutoSwitch || !OppoWifiAssistantStateTraker2.this.mFeatureState || !OppoWifiAssistantStateTraker2.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue()) {
                            int unused2 = OppoWifiAssistantStateTraker2.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                        }
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker23.logD("CMD_INTERNET_MONITOR mInterInteval = " + OppoWifiAssistantStateTraker2.this.mInterInteval);
                        if (!OppoWifiAssistantStateTraker2.this.mScreenOn) {
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker24.setInternetDetectAlarm(1, (long) oppoWifiAssistantStateTraker24.mInterInteval);
                            break;
                        } else {
                            OppoWifiAssistantStateTraker2.this.removeMessages(200710);
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker25 = OppoWifiAssistantStateTraker2.this;
                            oppoWifiAssistantStateTraker25.sendMessageDelayed(oppoWifiAssistantStateTraker25.obtainMessage(200710), (long) OppoWifiAssistantStateTraker2.this.mInterInteval);
                            break;
                        }
                    }
                case 200712:
                    if (OppoWifiAssistantStateTraker2.this.mWifiConnected) {
                        long currentBytes = TrafficStats.getRxBytes(OppoWifiAssistantStateTraker2.this.mInterfaceName) + TrafficStats.getTxBytes(OppoWifiAssistantStateTraker2.this.mInterfaceName);
                        long currentTime = System.currentTimeMillis();
                        long interval = (currentTime - OppoWifiAssistantStateTraker2.this.mLastSpeedTime) / 1000;
                        if (interval <= 0) {
                            speed = 10240;
                            OppoWifiAssistantStateTraker2.this.logE("CMD_SPEED_DETECT interval <= 0 !!!");
                        } else {
                            speed = (currentBytes - OppoWifiAssistantStateTraker2.this.mLastTrafficBytes) / interval;
                        }
                        if (speed >= 10240 || speed < 0) {
                            int unused3 = OppoWifiAssistantStateTraker2.this.mSpeedLowCount = 0;
                        } else {
                            OppoWifiAssistantStateTraker2.access$1608(OppoWifiAssistantStateTraker2.this);
                        }
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker26 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker26.logD("CMD_SPEED_DETECT  speed=" + speed + "Bytes/s mSpeedLowCount=" + OppoWifiAssistantStateTraker2.this.mSpeedLowCount + " currentBytes=" + currentBytes + " mLastTrafficBytes=" + OppoWifiAssistantStateTraker2.this.mLastTrafficBytes + " interval=" + interval + "s");
                        long unused4 = OppoWifiAssistantStateTraker2.this.mLastTrafficBytes = currentBytes;
                        long unused5 = OppoWifiAssistantStateTraker2.this.mLastSpeedTime = currentTime;
                        if (OppoWifiAssistantStateTraker2.this.mSpeedLowCount >= 2 && !OppoWifiAssistantStateTraker2.this.mScreenOn) {
                            OppoWifiAssistantStateTraker2.this.logD("CMD_SPEED_DETECT disableDualSta when screen off and speed low.");
                            OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setScreenOffDisableCount();
                            OppoWifiAssistantStateTraker2.this.mAssistantUtils.disableDualSta();
                            break;
                        } else {
                            long alarmTime = System.currentTimeMillis() + OppoWifiAssistantStateTraker2.SPEED_DETECT_INTERVAL;
                            OppoWifiAssistantStateTraker2.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
                            OppoWifiAssistantStateTraker2.this.mAlarmManager.set(0, alarmTime, OppoWifiAssistantStateTraker2.this.mSpeedDetectIntent);
                            break;
                        }
                    } else {
                        OppoWifiAssistantStateTraker2.this.logD("CMD_SPEED_DETECT wlan1 disconnected, skip");
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker2.EVENT_SHOW_ALERT /*{ENCODED_INT: 200719}*/:
                    String showConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker27 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker27.logD("shcon:" + showConfig);
                    if (showConfig != null && !OppoWifiAssistantStateTraker2.this.hasCheckNoRemind()) {
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker28 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker28.showDialogForData(oppoWifiAssistantStateTraker28.mAssistantUtils.getNetworkRecord(showConfig));
                        break;
                    }
                case OppoWifiAssistantStateTraker2.EVENT_DISMISS_ALERT /*{ENCODED_INT: 200720}*/:
                    String dismissConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker29 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker29.logD("dicon:" + dismissConfig);
                    if (dismissConfig != null) {
                        OppoWifiAssistantStateTraker2.this.dismissDialog(1);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker2.EVENT_DETECT_ALTERNATIVE /*{ENCODED_INT: 200721}*/:
                    String detectConfig = (String) msg.obj;
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker210 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker210.logD("decon:" + detectConfig);
                    OppoWifiAssistantRecord unused6 = OppoWifiAssistantStateTraker2.this.findAvailableCandidate(OppoWifiAssistantStateTraker2.this.mAssistantUtils.getNetworkRecord(detectConfig), 10, -127, false);
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
        logD("updateWifiState2 -> " + state);
        if (state == -1) {
            transitionTo(this.mInitState);
            logD("updateWifiState return");
            return;
        }
        if (state == 3) {
            transitionTo(this.mDisconnectedState);
            logD("wlan1 enabled, startScan...");
            this.mDisconnectedScanCount = 0;
            sendMessage(EVENT_START_SCAN);
        } else if (state == 1) {
            this.mAssistantUtils.saveWifiNetworkRecord();
            this.mDisconnectedScanCount = 0;
            removeMessages(EVENT_START_SCAN);
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
        if (updateState && this.mWifiStateMachine2 != null) {
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
        }
    }

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
            if (save) {
                long j = this.mAutoConnWlanTime;
                long connSaveForWlan = time - j;
                long connForWlanToData = time - this.mAutoConnDataTime;
                if (j > 0 && connSaveForWlan > 0 && connSaveForWlan < 180000) {
                    logD("stc wwb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_SAVE_FOR_WLAN, curRecord, newRecord, 0);
                    this.mAutoConnWlanTime = 0;
                } else if (!this.mChangedNetwork || !this.mChangedToData || this.mAutoConnDataTime <= 0 || connForWlanToData <= 0 || connForWlanToData >= 180000) {
                    logD("stc wwc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_CONN_SAVE_FOR_WLAN, curRecord, newRecord, 0);
                } else {
                    logD("stc wdb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA, curRecord, newRecord, 0);
                    this.mAutoConnDataTime = 0;
                }
            }
            long j2 = this.mAutoDataToWlanTime;
            long connForDataToWlan = time - j2;
            if (!this.mChangedToData && j2 > 0 && connForDataToWlan > 0 && connForDataToWlan < 180000) {
                logD("stc dwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA_TO_WLAN, curRecord, newRecord, 0);
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
        if (this.mWifiStateMachine2 == null) {
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
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            this.mWifiStateMachine2.setNetworkStatus(true);
            sendNetworkStateBroadcast(this.mLastConfigkey, true);
            this.mAssistantUtils.releaseDataNetwork();
        } else if (!this.mIsSoftAP || !isThirdAppOperate()) {
            OppoWifiAssistantRecord lastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
            if (lastRecord != null && !lastRecord.mNetworkValid2) {
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
    public void setDataAutoSwitch(boolean state) {
        if (this.mWifiStateMachine2 == null) {
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
                this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            this.mWifiStateMachine2.setNetworkStatus(true);
            this.mAssistantUtils.releaseDataNetwork();
        }
    }

    public void setFeatureState(boolean state) {
        BroadcastReceiver broadcastReceiver;
        logD("setFS: " + state);
        this.mFeatureState = state;
        OppoWifiSmartSwitcher oppoWifiSmartSwitcher = this.mWifiSmartSwitcher;
        if (oppoWifiSmartSwitcher != null) {
            oppoWifiSmartSwitcher.featureState(state);
        }
        if (this.mWifiStateMachine2 != null && !this.mFeatureState && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
            }
            this.mChangedNetwork = false;
            this.mCanTriggerData = true;
            this.mAssistantUtils.releaseDataNetwork();
            sendNetworkStateBroadcast(this.mLastConfigkey, true);
        }
        if (!this.mFeatureState && (broadcastReceiver = this.mBroadcastReceiver) != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    public void setInterfaceName(String interfaceName) {
        if (interfaceName == null) {
            this.mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN1;
        } else {
            this.mInterfaceName = interfaceName;
        }
    }

    private boolean isRusEnableWifiAssistantFourVersion() {
        return getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FOUR_VERSION_ENABLE", true).booleanValue();
    }

    public void detectScanResult(long time) {
        logD("detectScanResult current state: " + getCurrentState());
        this.mAssistantUtils.sortNetworkRecords();
        this.mLastScanTime = time;
        this.mTriggerScan = false;
        this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
        WifiInfo currentInfo = this.mWifiStateMachine2.syncRequestConnectionInfo();
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
    public void addOrUpdateRecord(String configKey) {
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
                WifiInfo aourWifiInfo = this.mWifiStateMachine2.syncRequestConnectionInfo();
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
                if (wnr.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", 1).intValue() || wnr.mIsCaptive2) {
                    this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRecordCaptiveState(String configKey, boolean captive, boolean save, boolean blacklistCapUrl) {
        OppoWifiAssistantRecord record;
        OppoClientModeImpl2 oppoClientModeImpl2;
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            record.mIsCaptive2 = captive;
            record.mIsBlacklistCap2 = blacklistCapUrl;
            this.mAssistantUtils.addNetworkRecord(configKey, record, true);
            if (save && this.mCaptivePortal && (this.mGotInternetResult & 8) != 8) {
                updateRecordInternetStateAndTime(configKey, false, true);
                this.mInterResult = false;
                this.mGotInternetResult |= 8;
                this.mGotInternetResult &= -5;
                if (blacklistCapUrl && (oppoClientModeImpl2 = this.mWifiStateMachine2) != null) {
                    oppoClientModeImpl2.disconnectCommand();
                }
                OppoWifiAssistantRecord lastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
                if (lastRecord != null && lastRecord.mWifiConfiguration != null) {
                    logD("updateRecordCaptiveState wlan1 connected to captive network:" + configKey + ", try other candidate...");
                    findAvailableCandidate(lastRecord, lastRecord.mScore, lastRecord.mRssi, false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRecordDisableState(String configKey) {
        OppoWifiAssistantRecord record;
        int disableId;
        logD("updateRecordDisableState: " + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
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
            logD("updateRecordDisableState: mNetFailCount=" + record.mNetFailCount + ",mIsCaptive2=" + record.mIsCaptive2);
            if (disableId != -1 && record.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", 1).intValue()) {
                OppoClientModeImpl2 oppoClientModeImpl2 = this.mWifiStateMachine2;
                if (oppoClientModeImpl2 == null || oppoClientModeImpl2.isSupplicantAvailable()) {
                    this.mWifiConfigManager.disableNetwork(disableId, 1000, 10);
                } else {
                    logD("wifi is in disable or disable pending state,cancel disable!!");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRecordConCount(String configKey) {
        OppoWifiAssistantRecord record;
        logD("uRcc key:" + configKey);
        if (configKey != null && (record = this.mAssistantUtils.getNetworkRecord(configKey)) != null) {
            record.mConnSuccCout++;
            record.mNetid = this.mLastNetId;
            record.mBssid = this.mLastBssid;
            record.mConnExp = false;
            WifiInfo ccWifiInfo = this.mWifiStateMachine2.syncRequestConnectionInfo();
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
    public void updateRecordConnectFail(String configKey) {
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
    public void updateRecordUseTime(String configKey) {
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
    public void updateRecordInternetStateAndTime(String configKey, boolean valid, boolean transiTo) {
        logD("updateRecordInternetStateAndTime key= " + configKey + ", valid = " + valid + ", transiTo=" + transiTo);
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
                record.mNetworkValid2 = valid;
            } else {
                record = new OppoWifiAssistantRecord();
                record.mConfigkey = configKey;
                record.mBssid = this.mLastBssid;
                record.mAccessNetTime = this.mAccessNetTime;
                record.mNetworkValid2 = valid;
                record.mConnSuccCout++;
            }
            record.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
            WifiInfo uritWifiInfo = this.mWifiStateMachine2.syncRequestConnectionInfo();
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
                record.mIsCaptive2 = false;
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
                if (transiTo && !record.mIsCaptive2) {
                    record.mNetFailCount++;
                }
            }
            this.mAssistantUtils.addNetworkRecord(configKey, record, true);
            this.mAssistantUtils.sortNetworkRecords();
            if (transiTo) {
                sendMessage(200707);
            }
            OppoDualStaNotification oppoDualStaNotification = this.mDualStaNotify;
            if (oppoDualStaNotification != null && !valid) {
                oppoDualStaNotification.clearAllNotifying();
            }
            sendNetworkStateBroadcast(configKey, valid);
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
            record.mNetworkValid2 = false;
            this.mAssistantUtils.addNetworkRecord(configKey, record, false);
        }
    }

    /* JADX INFO: Multiple debug info for r4v27 int: [D('currentQuality' int), D('i' int)] */
    /* JADX INFO: Multiple debug info for r7v67 'now'  long: [D('now' long), D('apkinfo' java.lang.String)] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x05d7  */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x066b  */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x06c4  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0126  */
    public synchronized void updateRecordLinkQuality(RssiPacketCountInfo info) {
        boolean willRoam;
        OppoWifiAssistantRecord mCandidate;
        int dbad;
        double loss;
        long now;
        long now2;
        int slaScore;
        String apkinfo;
        int trigWlanSetectScore;
        long napTime;
        int score;
        int score2;
        int score3;
        int rttSum;
        int rtt_3;
        int count;
        int rtt_1;
        int speed_2;
        int speed_1;
        int speed_12;
        OppoWifiAssistantRecord mLastRecord = this.mAssistantUtils.getNetworkRecord(this.mLastConfigkey);
        if (mLastRecord != null && info != null) {
            if (info.rssi > WifiConfiguration.INVALID_RSSI) {
                int score4 = 79;
                int rssi = info.rssi;
                int txbad = info.txbad;
                int txgood = info.txgood;
                mLastRecord.mRssi = rssi;
                if (rssi > mLastRecord.mBestRssi) {
                    mLastRecord.mBestRssi = rssi;
                }
                int count2 = this.mLinkInterval / 1000;
                int[] rttAndSpeed = this.mOppoSlaManager.getRttAndSpeed(count2);
                int i = 2;
                if (this.mOppoSlaManager.isGameInFront() || rttAndSpeed == null || rttAndSpeed.length != count2 * 6) {
                    mCandidate = null;
                    willRoam = false;
                } else {
                    int rttSum2 = 0;
                    int i2 = 0;
                    int speedSum = 0;
                    while (i2 < count2) {
                        speedSum += rttAndSpeed[(i2 * 6) + (this.mWlanIfIndex * i)];
                        rttSum2 += rttAndSpeed[(i2 * 6) + (this.mWlanIfIndex * 2) + 1];
                        i2++;
                        i = 2;
                    }
                    int avgRtt = rttSum2 / count2;
                    int avgSpeed = speedSum / count2;
                    int speedDecrease = 0;
                    int speed_score_1 = 5;
                    int speed_score_2 = 10;
                    int rtt_score_1 = 15;
                    int rtt_score_2 = 10;
                    int rtt_score_3 = 5;
                    if (this.mWifiRomUpdateHelper != null) {
                        int[] params = this.mWifiRomUpdateHelper.getSpeedRttParams();
                        if (params != null) {
                            mCandidate = null;
                            willRoam = false;
                            if (params.length == 11) {
                                int speed_13 = params[0];
                                int speed_22 = params[1];
                                int rtt_12 = params[2];
                                int rtt_2 = params[3];
                                int rtt_32 = params[4];
                                speed_score_1 = params[5];
                                speed_score_2 = params[6];
                                rtt_score_1 = params[7];
                                rtt_score_2 = params[8];
                                rtt_score_3 = params[9];
                                int cellDecreaseMax = params[10];
                                speed_1 = speed_13;
                                speed_2 = speed_22;
                                rtt_1 = rtt_12;
                                count = rtt_2;
                                rtt_3 = rtt_32;
                                rttSum = cellDecreaseMax;
                                if (avgSpeed >= speed_1 && avgSpeed >= speed_2) {
                                    speedDecrease = speed_score_1;
                                } else if (avgSpeed < speed_2) {
                                    speedDecrease = speed_score_2;
                                }
                                if (avgRtt < rtt_1) {
                                    speed_12 = rtt_score_1 + speedDecrease;
                                } else if (avgRtt >= count && avgRtt < rtt_1) {
                                    speed_12 = rtt_score_2 + speedDecrease;
                                } else if (avgRtt < rtt_3 || avgRtt >= count) {
                                    speed_12 = 0;
                                } else {
                                    speed_12 = rtt_score_3 + speedDecrease;
                                }
                                if (this.mChangedToData || speed_12 <= rttSum) {
                                    score4 = 79 - speed_12;
                                } else {
                                    score4 = 79 - rttSum;
                                }
                                logD("speed and rtt decreased " + (79 - score4) + " points.");
                            }
                        } else {
                            mCandidate = null;
                            willRoam = false;
                        }
                    } else {
                        mCandidate = null;
                        willRoam = false;
                    }
                    speed_1 = 150;
                    speed_2 = 100;
                    rtt_1 = 250;
                    count = 200;
                    rtt_3 = 150;
                    rttSum = 5;
                    if (avgSpeed >= speed_1) {
                    }
                    if (avgSpeed < speed_2) {
                    }
                    if (avgRtt < rtt_1) {
                    }
                    if (this.mChangedToData) {
                    }
                    score4 = 79 - speed_12;
                    logD("speed and rtt decreased " + (79 - score4) + " points.");
                }
                int netQualityIndex = getQualityIndex(mLastRecord.mIs5G, rssi);
                if (netQualityIndex == 0) {
                    int badRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", 30).intValue();
                    if (this.mChangedToData) {
                        score3 = score4 - (badRssiScore + 5);
                    } else {
                        score3 = score4 - badRssiScore;
                    }
                } else if (netQualityIndex == 1) {
                    int lowRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", 20).intValue();
                    if (this.mChangedToData) {
                        score4 -= lowRssiScore + 5;
                    } else {
                        score4 -= lowRssiScore;
                    }
                } else if (netQualityIndex == 2) {
                    score4 -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD", 5).intValue();
                }
                int dbad2 = txbad - this.mLastTxBad;
                int dgood = txgood - this.mLastTxGood;
                int dtotal = dbad2 + dgood;
                this.mLastTxBad = txbad;
                this.mLastTxGood = txgood;
                if (this.mIsFirstPktCntFetchSucceed) {
                    dbad2 = 0;
                    dgood = 0;
                    dtotal = 0;
                    this.mIsFirstPktCntFetchSucceed = false;
                }
                boolean isEnableWifiAssistantFourVersion = isRusEnableWifiAssistantFourVersion();
                if (isEnableWifiAssistantFourVersion) {
                    if (mLastRecord.mHistoryRecordQueue == null) {
                        mLastRecord.mHistoryRecordQueue = new OppoHistoryRecordQueue(this.mContext, mLastRecord.mBssid, mLastRecord.mIs5G);
                    }
                    if (!this.mOppoSlaManager.isNotUpdateRecordBySla()) {
                        mLastRecord.mHistoryRecordQueue.recordTxWithRssi(rssi, dgood, dbad2);
                    }
                    int historyRecordTriggerThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_HISTORY_RECORD_TRIGGER_THRESHOLD", 10).intValue();
                    int historyRecordValidThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_HISTORY_RECORD_VALID_THRESHOLD", 10).intValue();
                    mLastRecord.mIsHistoryLoss = false;
                    if (dtotal < historyRecordTriggerThreshold && mLastRecord.mHistoryRecordQueue.getTxTotalWithRssi(rssi) > historyRecordValidThreshold) {
                        dbad2 = mLastRecord.mHistoryRecordQueue.getTxBadWithRssi(rssi);
                        dtotal = mLastRecord.mHistoryRecordQueue.getTxTotalWithRssi(rssi);
                        dgood = dtotal - dbad2;
                        mLastRecord.mIsHistoryLoss = true;
                    }
                }
                DecimalFormat df = new DecimalFormat("#.##");
                double badLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD", Double.valueOf((double) STATIC_BAD_LINK_LOSS_THRESHOLD)).doubleValue();
                double lowLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD", Double.valueOf(0.5d)).doubleValue();
                double goodLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD", Double.valueOf((double) STATIC_GOOD_LINK_LOSS_THRESHOLD)).doubleValue();
                int noTrafficScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD", 5).intValue();
                if (dtotal > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_VALID_LINK_LOSS_NUM", 0).intValue()) {
                    dbad = dbad2;
                    loss = ((double) dbad2) / ((double) dtotal);
                    if ((!isEnableWifiAssistantFourVersion || loss >= goodLinkLossThreshold) && (isEnableWifiAssistantFourVersion || loss != 0.0d)) {
                        this.mLinkDetectTimes = 0;
                        if (loss >= badLinkLossThreshold) {
                            int badLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD", 35).intValue();
                            if (this.mChangedToData) {
                                score2 = score4 - (badLinkScoreThreshold + 5);
                            } else {
                                score2 = score4 - badLinkScoreThreshold;
                            }
                        } else if (loss >= lowLinkLossThreshold && loss < badLinkLossThreshold) {
                            int lowLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD", 30).intValue();
                            if (this.mChangedToData) {
                                score = score4 - (lowLinkScoreThreshold + 5);
                            } else {
                                score = score4 - lowLinkScoreThreshold;
                            }
                        } else if (loss >= goodLinkLossThreshold && loss < lowLinkLossThreshold) {
                            score4 -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD", 20).intValue();
                        }
                    } else {
                        this.mLinkDetectTimes++;
                    }
                } else {
                    dbad = dbad2;
                    this.mLinkDetectTimes = 0;
                    if (this.mChangedToData) {
                        score4 -= noTrafficScore + 5;
                        loss = 0.0d;
                    } else {
                        score4 -= noTrafficScore;
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
                    score4 -= noTrafficScore * 3;
                } else if (badLossCount == this.mLossArray.length - 1 && loss >= lowLinkLossThreshold) {
                    score4 -= noTrafficScore * 2;
                } else if (badLossCount == this.mLossArray.length - 2 && loss >= lowLinkLossThreshold) {
                    score4 -= noTrafficScore;
                }
                if (!mLastRecord.mNetworkValid2) {
                    score4 = 10;
                }
                if (score4 < 10) {
                    score4 = 10;
                }
                for (int i3 = netQualityIndex; i3 < 4; i3++) {
                    if (score4 > mLastRecord.mNetQualitys[i3]) {
                        mLastRecord.mNetQualitys[i3] = score4;
                        mLastRecord.mScore = score4;
                    }
                }
                logD("updateRecordLinkQuality key:" + this.mLastConfigkey + ",rssi=" + rssi + " txbad=" + txbad + " txgood=" + txgood + ",dbad = " + dbad + ", dgood = " + dgood + ",dtotal= " + dtotal + ",badLossCount=" + badLossCount + ",loss=" + df.format(loss * 100.0d) + ",mLinkDetectTimes= " + this.mLinkDetectTimes + ",newTcpStatus:" + 0 + ",mIndex =" + this.mIndex + ",score= " + score4 + ",hls=" + mLastRecord.mIsHistoryLoss + ",mInterResult=" + this.mInterResult + ",mChangeScore=" + this.mChangeScore + ",mNetQulityGoodCount=" + this.mNetQulityGoodCount);
                this.mNetQualityArray[this.mIndex] = score4;
                this.mIndex = this.mIndex + 1;
                this.mIndex = this.mIndex % 3;
                int sumQuality = 0;
                int i4 = 0;
                for (int i5 = 3; i4 < i5; i5 = 3) {
                    sumQuality += this.mNetQualityArray[i4];
                    i4++;
                }
                int currentQuality = sumQuality / 3;
                int comparedScore = currentQuality > score4 ? currentQuality : score4;
                logD("currentQuality=" + currentQuality + ",comparedScore=" + comparedScore + ",mWlanInvalidThreshold=" + this.mWlanInvalidThreshold + ",mChangedNetwork=" + this.mChangedNetwork + ",mCanTriggerData=" + this.mCanTriggerData + ",mChangedToData=" + this.mChangedToData);
                String apkinfo2 = "ssid:" + mLastRecord.mConfigkey + ",net:" + mLastRecord.mNetworkValid + ",currScore:" + score4 + ",score:" + currentQuality + ", rssi:" + rssi + ",loss:" + df.format(loss * 100.0d) + ",total:" + dtotal + ",tcp:" + 0;
                broadcastInfotoTestapk(apkinfo2);
                int slaScore2 = currentQuality;
                if (rssi <= -80) {
                    slaScore2 = 10;
                }
                this.mOppoSlaManager.sendWifiScoreToKernel(this.mWlanIfIndex, slaScore2);
                this.mAssistantUtils.setWifiScore(this.mInterfaceName, currentQuality);
                if (currentQuality > 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD", 15).intValue()) {
                    if (!this.mChangedNetwork) {
                        this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", 5000).intValue();
                        now = System.currentTimeMillis();
                        if (currentQuality <= 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD", 20).intValue() || !this.mCanTriggerData) {
                            apkinfo = apkinfo2;
                            slaScore = slaScore2;
                            now2 = now;
                        } else {
                            apkinfo = apkinfo2;
                            slaScore = slaScore2;
                            now2 = now;
                            this.mLastTrigDataTime = now2;
                            this.mAssistantUtils.setupDataNetwork();
                            this.mCanTriggerData = false;
                        }
                        trigWlanSetectScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD", 25).intValue();
                        int roamDetectCount = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_ROAM_DETECT", 6).intValue();
                        if (currentQuality > 79 - trigWlanSetectScore) {
                            if (score4 <= 79 - trigWlanSetectScore && this.mLastScanTime != 0 && now2 - this.mLastScanTime > STAIC_SCAN_RESULT_AGE && this.mTrigScanCount < 3 && !this.mTriggerScan) {
                                if (this.mWifiStateMachine2 != null && this.mScanRequestProxy != null) {
                                    this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                                    this.mTrigScanCount++;
                                    this.mTriggerScan = true;
                                    this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
                                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SCAN_TIMEOUT), 6000);
                                }
                            }
                            if (!this.mTriggerScan) {
                                if (mLastRecord.mNetworkValid2) {
                                    willRoam = detectMaybeRoam(mLastRecord, comparedScore);
                                }
                                if (willRoam && this.mRoamdetectCount < roamDetectCount) {
                                    this.mRoamdetectCount++;
                                    if (!this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                                        mCandidate = findAvailableCandidate(mLastRecord, comparedScore, mLastRecord.mRssi, true);
                                    }
                                } else if (score4 <= 79 - trigWlanSetectScore) {
                                    mCandidate = findAvailableCandidate(mLastRecord, comparedScore, mLastRecord.mRssi, false);
                                }
                            }
                        }
                        AlertDialog alertDialog = mAlertDialog;
                        if (!this.mChangedToData && this.mChangedNetwork && currentQuality > 50) {
                            changeNetworkToWlan(mLastRecord, currentQuality, false);
                        }
                        if (this.mWlanInvalidThreshold == 70 && currentQuality <= 39 && this.mAutoSwitchDataDisableTime > 0) {
                            napTime = System.currentTimeMillis() - this.mAutoSwitchDataDisableTime;
                            logD("nt= " + napTime);
                            if (napTime > getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME", Long.valueOf((long) AUTO_SWITCH_DATA_DISBALE_TIME)).longValue()) {
                                resetAutoSwitchDataDetect();
                            }
                        }
                        int wlanBadThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD", Integer.valueOf(this.mWlanInvalidThreshold)).intValue();
                        this.mAssistantUtils.setCanChangeToCell(this.mInterfaceName, false);
                        if (!this.mChangedNetwork || currentQuality > 79 - wlanBadThreshold || score4 > 79 - wlanBadThreshold) {
                            if (this.mChangedNetwork && this.mChangedToData) {
                                int goodLinkCountThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT", 3).intValue();
                                if (!mLastRecord.mNetworkValid2 && !this.mLastInternetResult && this.mChangeRssi != 0 && rssi >= -72 && currentQuality >= 50 && score4 >= 50 && this.mChangeScore == 10) {
                                    changeNetworkToWlan(mLastRecord, score4, true);
                                } else if ((rssi > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD", Integer.valueOf((int) STATIC_RSSI_TO_WLAN_THRESHOLD)).intValue() || this.mLinkDetectTimes < goodLinkCountThreshold) && (rssi < -65 || this.mLinkDetectTimes < goodLinkCountThreshold - 2)) {
                                    if (rssi >= -65 && this.mTriggerInter && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                        logD("dt inter for strong rssi");
                                        this.mTriggerInter = false;
                                        setInternetDetectAlarm(0, 0);
                                    }
                                } else if (this.mChangedNetwork && mLastRecord.mNetworkValid2 && currentQuality >= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD", 64).intValue() && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                    changeNetworkToWlan(mLastRecord, currentQuality, true);
                                }
                            }
                        } else if ((!willRoam || (willRoam && this.mRoamdetectCount >= roamDetectCount)) && mCandidate == null && !this.mTriggerScan) {
                            if (!mLastRecord.mNetworkValid2) {
                                currentQuality = 10;
                            } else if (currentQuality < 20) {
                                currentQuality = 20;
                            }
                            if (this.mDataAutoSwitch && !shouldIgnoreSwitch()) {
                                this.mAssistantUtils.setCanChangeToCell(this.mInterfaceName, true);
                                if (this.mAssistantUtils.getOtherIfCanChangeToCell(this.mInterfaceName)) {
                                    this.mChangedNetwork = true;
                                    this.mTriggerInter = true;
                                    this.mChangeRssi = rssi;
                                    this.mChangeScore = currentQuality;
                                    if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                                        this.mWifiStateMachine2.sendWifiNetworkScore(currentQuality, false);
                                    }
                                }
                            }
                        }
                        this.mAssistantUtils.addNetworkRecord(this.mLastConfigkey, mLastRecord, false);
                        return;
                    }
                }
                this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SAMPL_INTERVAL", 3000).intValue();
                now = System.currentTimeMillis();
                if (currentQuality <= 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD", 20).intValue()) {
                }
                apkinfo = apkinfo2;
                slaScore = slaScore2;
                now2 = now;
                trigWlanSetectScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD", 25).intValue();
                int roamDetectCount2 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_ROAM_DETECT", 6).intValue();
                if (currentQuality > 79 - trigWlanSetectScore) {
                }
                AlertDialog alertDialog2 = mAlertDialog;
                changeNetworkToWlan(mLastRecord, currentQuality, false);
                napTime = System.currentTimeMillis() - this.mAutoSwitchDataDisableTime;
                logD("nt= " + napTime);
                if (napTime > getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME", Long.valueOf((long) AUTO_SWITCH_DATA_DISBALE_TIME)).longValue()) {
                }
                int wlanBadThreshold2 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD", Integer.valueOf(this.mWlanInvalidThreshold)).intValue();
                this.mAssistantUtils.setCanChangeToCell(this.mInterfaceName, false);
                if (!this.mChangedNetwork) {
                }
                int goodLinkCountThreshold2 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT", 3).intValue();
                if (!mLastRecord.mNetworkValid2) {
                }
                if (rssi > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD", Integer.valueOf((int) STATIC_RSSI_TO_WLAN_THRESHOLD)).intValue()) {
                }
                logD("dt inter for strong rssi");
                this.mTriggerInter = false;
                setInternetDetectAlarm(0, 0);
                this.mAssistantUtils.addNetworkRecord(this.mLastConfigkey, mLastRecord, false);
                return;
            }
        }
        logD("updateRecordLinkQuality no current record or invalid RssiPacketCountInfo.");
    }

    private void changeNetworkToWlan(OppoWifiAssistantRecord record, int quality, boolean dataToWlan) {
        if (this.mWifiStateMachine2 != null) {
            if (this.mAssistantUtils.isPrimaryWifi(this.mInterfaceName)) {
                this.mWifiStateMachine2.sendWifiNetworkScore(quality, true);
            }
            this.mChangedNetwork = false;
            this.mTriggerInter = false;
            this.mRoamdetectCount = 0;
            this.mChangeRssi = 0;
            this.mChangeScore = 0;
            this.mTrigScanCount = 0;
            this.mAssistantUtils.releaseDataNetwork();
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
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass3 */

                public void run() {
                    Intent broadIntent = new Intent(OppoWifiAssistantStateTraker2.BRAOD_WIFI_INFO);
                    Log.d("testFeature", "mBroadInfo = " + OppoWifiAssistantStateTraker2.this.mBroadInfo);
                    broadIntent.putExtra(OppoWifiAssistantStateTraker2.EXTRA_WIFI_NETINFO, OppoWifiAssistantStateTraker2.this.mBroadInfo);
                    OppoWifiAssistantStateTraker2.this.mContext.sendStickyBroadcastAsUser(broadIntent, UserHandle.ALL);
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
        OppoClientModeImpl2 oppoClientModeImpl2 = this.mWifiStateMachine2;
        if (oppoClientModeImpl2 == null || oppoClientModeImpl2.isSupplicantAvailable()) {
            OppoClientModeImpl2 oppoClientModeImpl22 = this.mWifiStateMachine2;
            if (oppoClientModeImpl22 != null) {
                if (oppoClientModeImpl22.isNetworkAutoConnecting(selectConf.networkId)) {
                    logD("network: " + selectConf.SSID + " is connecting, do nothing!");
                    return;
                } else if (this.mWifiStateMachine2.isNetworkConnected(selectConf.networkId)) {
                    logD("network: " + selectConf.SSID + " is connected, reassociate.");
                    this.mWifiStateMachine2.reassociateCommand();
                    return;
                } else {
                    this.mWifiStateMachine2.setTargetNetworkId(lastRecord.mNetid);
                    this.mWifiStateMachine2.clearTargetBssid(TAG);
                    this.mWifiStateMachine2.prepareForForcedConnection(selectConf.networkId);
                    this.mWifiStateMachine2.startConnectToNetwork(selectConf.networkId, 1000, "any");
                }
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
    public WifiConfiguration getWifiConfig(String ssid, String bssid) {
        if (ssid == null || bssid == null) {
            return null;
        }
        WifiConfiguration connectedConfig = this.mWifiStateMachine2.getCurrentWifiConfiguration();
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
    public void detectSwitchDataFrequence() {
        if (this.mWifiStateMachine2 != null) {
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
    public void resetAutoSwitchDataDetect() {
        if (this.mWifiStateMachine2 != null) {
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
        WifiInfo curWifiInfo = this.mWifiStateMachine2.getWifiInfo();
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
            WifiInfo otherWifiInfo = this.mAssistantUtils.getOtherIfWifiInfo(this.mInterfaceName);
            if (otherWifiInfo != null && otherWifiInfo.getBSSID().equals(result.BSSID)) {
                logD("can't roam to bssid of primary wifi");
            } else if (scanConfKey.equals(lastRecord.mConfigkey) && scanRssi > -83 && lastRecord.mBssid != null && !scanBssid.equals(lastRecord.mBssid)) {
                if (curFreq != scanFreq || (curFreq == scanFreq && scanRssi - lastRecord.mRssi > 5)) {
                    maybeRoam = true;
                }
            }
        }
        logD("detectMaybeRoam maybeRoam=" + maybeRoam);
        return maybeRoam;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0136  */
    public void detectTraffic() {
        long rxPkts;
        long txPkts;
        boolean z;
        int i;
        long lastDetlaRxPkts = this.mdRxPkts;
        if (this.mInterfaceName != null) {
            txPkts = this.mOppoSlaManager.getWlanTcpTxPackets(1);
            rxPkts = this.mOppoSlaManager.getWlanTcpRxPackets(1);
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
            long j = this.mDTxPkts;
            if (j > 0) {
                if (this.mScreenOn || j != 1) {
                    this.mRxPktsLowCount++;
                } else {
                    logD("Did Send One But Not Receive Any pkt.");
                }
                logD("DT,tP=" + txPkts + ", dTP=" + this.mDTxPkts + ", rP=" + rxPkts + ", dRP=" + this.mdRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
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
        }
        this.mRxPktsLowCount = 0;
        logD("DT,tP=" + txPkts + ", dTP=" + this.mDTxPkts + ", rP=" + rxPkts + ", dRP=" + this.mdRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
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

    /* JADX INFO: Multiple debug info for r7v9 java.lang.String: [D('scanSsid' java.lang.String), D('scanBssid' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r13v9 int: [D('scanRssi' int), D('diffConnrateThreshold' double)] */
    /* JADX INFO: Multiple debug info for r0v63 boolean: [D('refIs5G' boolean), D('index' int)] */
    /* JADX INFO: Multiple debug info for r7v26 boolean: [D('scanBssid' java.lang.String), D('refIs5G' boolean)] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x033d, code lost:
        if ((r4.mNetQualitys[r0] - r41) >= r12) goto L_0x0342;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0340, code lost:
        r5 = r13 - r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0342, code lost:
        if (r40 == null) goto L_0x034e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0346, code lost:
        if (r40.mNetworkValid2 == false) goto L_0x034e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x034b, code lost:
        if ((r13 - r42) >= 5) goto L_0x034e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x034e, code lost:
        if (r5 < r12) goto L_0x0380;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0350, code lost:
        r10 = r4.mNetQualitys[r0];
        r11 = r13;
        r15 = r8;
        r14 = r4.mMaxSpeed;
        r23 = r4.mIs5G;
        logD("findAvailableCandidate-->diffScore found candidate:" + r6);
        r34 = r4;
        r24 = r14;
        r7 = r4;
        r10 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0382, code lost:
        if (r5 >= r12) goto L_0x044e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0384, code lost:
        if (r5 < 0) goto L_0x044e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0386, code lost:
        if (r23 == false) goto L_0x039d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x038c, code lost:
        if (r6.is5GHz() != false) goto L_0x039d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x039d, code lost:
        if (r23 != false) goto L_0x03d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x03a3, code lost:
        if (r6.is5GHz() == false) goto L_0x03d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x03a5, code lost:
        r10 = r4.mNetQualitys[r0];
        r11 = r13;
        r15 = r8;
        r7 = r4.mMaxSpeed;
        r14 = r4.mIs5G;
        r5 = new java.lang.StringBuilder();
        r24 = r7;
        r5.append("findAvailableCandidate-->5G found candidate:");
        r5.append(r6);
        logD(r5.toString());
        r34 = r4;
        r23 = r14;
        r7 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x03d1, code lost:
        r14 = r8 - r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x03d7, code lost:
        if (r14 < r13) goto L_0x0406;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x03d9, code lost:
        r10 = r4.mNetQualitys[r0];
        r11 = r13;
        r5 = r4.mMaxSpeed;
        r7 = r4.mIs5G;
        r24 = r5;
        r5 = new java.lang.StringBuilder();
        r23 = r7;
        r5.append("findAvailableCandidate-->diffConsuccRate found candidate:");
        r5.append(r6);
        logD(r5.toString());
        r34 = r4;
        r7 = r4;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x0408, code lost:
        if (r14 >= r13) goto L_0x0449;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x040e, code lost:
        if (r14 <= 0.0d) goto L_0x0449;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0414, code lost:
        if ((r4.mMaxSpeed - r24) <= 0) goto L_0x0444;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x0416, code lost:
        r10 = r4.mNetQualitys[r0];
        r11 = r13;
        r7 = r4.mMaxSpeed;
        r23 = r4.mIs5G;
        r0 = new java.lang.StringBuilder();
        r34 = r4;
        r0.append("findAvailableCandidate-->diffSpeed found candidate:");
        r0.append(r6);
        logD(r0.toString());
        r24 = r7;
        r7 = r4;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0444, code lost:
        r34 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0449, code lost:
        r34 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x044e, code lost:
        r34 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x02a2, code lost:
        r31 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x02b0, code lost:
        r0 = getQualityIndex(r6.is5GHz(), r6.level);
        r15 = new java.lang.StringBuilder();
        r31 = r5;
        r15.append("findAvailableCandidate*** ");
        r15.append(r4.mConfigkey);
        r15.append("bssid:");
        r15.append(r6.BSSID);
        r15.append(" freq:");
        r15.append(r6.frequency);
        r15.append(",nq[");
        r15.append(r0);
        r15.append("]:");
        r15.append(r4.mNetQualitys[r0]);
        r15.append(",refScore:");
        r15.append(r10);
        r15.append(",refRssi:");
        r15.append(r11);
        r15.append(",scanRssi:");
        r15.append(r13);
        r15.append(",consuccRate:");
        r15.append(r8);
        logD(r15.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x031b, code lost:
        if (r40 == null) goto L_0x0323;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x031f, code lost:
        if (r13 > -83) goto L_0x0323;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0325, code lost:
        if (r13 > -75) goto L_0x0329;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x032e, code lost:
        if (r4.mNetQualitys[r0] == -1) goto L_0x0340;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0330, code lost:
        if (r10 != -1) goto L_0x0333;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0333, code lost:
        r5 = r4.mNetQualitys[r0] - r10;
     */
    public OppoWifiAssistantRecord findAvailableCandidate(OppoWifiAssistantRecord lastRecord, int curScore, int curRssi, boolean isRoaming) {
        OppoWifiAssistantRecord candidate;
        WifiInfo otherWifiInfo;
        double diffConnrateThreshold;
        List<ScanResult> scanList;
        int refSpeed;
        boolean refIs5G;
        int refRssi;
        int refScore;
        OppoWifiAssistantRecord wnr;
        WifiInfo otherWifiInfo2;
        double refConsuccRate;
        if (this.mWifiStateMachine2.isDupDhcp()) {
            logD("[bug#1131400] dupDhcp, wait DHCP retry.");
            return null;
        } else if (this.mAssistantUtils.isDualStaDisablingWithDelay()) {
            logD("disableDualStaWithDelay is in progress, wlan1 should not connect any ap !");
            return null;
        } else {
            WifiInfo otherWifiInfo3 = this.mAssistantUtils.getOtherIfWifiInfo(this.mInterfaceName);
            if (otherWifiInfo3 == null) {
                logD("findAvailableCandidate wlan0 is not connected, do not connect wlan1.");
                return null;
            }
            logD("findAvailableCandidate otherWifiInfo:" + otherWifiInfo3);
            if (this.mAssistantUtils.is1x1IotRouter()) {
                Log.d(TAG, "can't enable oppo wifi sta2 !, because router is in 1x1 blackList.");
                return null;
            }
            this.mWifiStateMachine2.setOtherWifiInfo(otherWifiInfo3);
            List<OppoWifiAssistantRecord> netRecords = this.mAssistantUtils.getSortNetworkRecords();
            List<ScanResult> scanList2 = this.mScanRequestProxy.syncGetScanResultsList();
            int refRssi2 = 0;
            int diffScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD", 10).intValue();
            double diffConnrateThreshold2 = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD", Double.valueOf((double) STATIC_DIFF_CONNRATE_THRESHOLD)).doubleValue();
            if (lastRecord == null) {
                candidate = null;
                logD("findAvailableCandidate lastRecord == null");
            } else {
                candidate = null;
            }
            Iterator<OppoWifiAssistantRecord> it = netRecords.iterator();
            int refRssi3 = curScore;
            OppoWifiAssistantRecord candidate2 = candidate;
            int refRssi4 = curRssi;
            boolean refIs5G2 = false;
            double refConsuccRate2 = 0.0d;
            while (it.hasNext()) {
                OppoWifiAssistantRecord wnr2 = it.next();
                if (!wnr2.mNetworkValid2 || wnr2.mConfigkey == null || wnr2.mIsCaptive2) {
                    otherWifiInfo = otherWifiInfo3;
                    scanList = scanList2;
                    refScore = refRssi3;
                    refRssi = refRssi4;
                    refIs5G = refIs5G2;
                    refSpeed = refRssi2;
                    diffConnrateThreshold = diffConnrateThreshold2;
                } else if (wnr2.mIsBlacklistCap2) {
                    otherWifiInfo = otherWifiInfo3;
                    scanList = scanList2;
                    refScore = refRssi3;
                    refRssi = refRssi4;
                    refIs5G = refIs5G2;
                    refSpeed = refRssi2;
                    diffConnrateThreshold = diffConnrateThreshold2;
                } else {
                    if (wnr2.mConnExp) {
                        logD("findAvailableCandidate record config co exp");
                        otherWifiInfo = otherWifiInfo3;
                        scanList = scanList2;
                        refScore = refRssi3;
                        refRssi = refRssi4;
                        refIs5G = refIs5G2;
                        refSpeed = refRssi2;
                        diffConnrateThreshold = diffConnrateThreshold2;
                    } else {
                        if (lastRecord != null) {
                            refScore = refRssi3;
                            if (lastRecord.mConfigkey.equals(wnr2.mConfigkey) && !isRoaming) {
                                logD("findAvailableCandidate same configKey and not roaming");
                                otherWifiInfo = otherWifiInfo3;
                                scanList = scanList2;
                                refRssi = refRssi4;
                                refIs5G = refIs5G2;
                                refSpeed = refRssi2;
                                diffConnrateThreshold = diffConnrateThreshold2;
                            }
                        } else {
                            refScore = refRssi3;
                        }
                        if (lastRecord == null || lastRecord.mConfigkey.equals(wnr2.mConfigkey) || !isRoaming) {
                            WifiConfiguration wcf = this.mWifiConfigManager.getConfiguredNetwork(wnr2.mConfigkey);
                            if (wcf == null) {
                                logD("findAvailableCandidate config is null");
                                otherWifiInfo = otherWifiInfo3;
                                scanList = scanList2;
                                refRssi = refRssi4;
                                refIs5G = refIs5G2;
                                refSpeed = refRssi2;
                                diffConnrateThreshold = diffConnrateThreshold2;
                            } else if (wcf.status == 1) {
                                logD("findAvailableCandidate config is disabled");
                                otherWifiInfo = otherWifiInfo3;
                                scanList = scanList2;
                                refRssi = refRssi4;
                                refIs5G = refIs5G2;
                                refSpeed = refRssi2;
                                diffConnrateThreshold = diffConnrateThreshold2;
                            } else if (this.mWifiStateMachine2.isTempDisabled(wnr2.mConfigkey)) {
                                logD(wnr2.mConfigkey + "is temp disabled by wlan1");
                                otherWifiInfo = otherWifiInfo3;
                                scanList = scanList2;
                                refRssi = refRssi4;
                                refIs5G = refIs5G2;
                                refSpeed = refRssi2;
                                diffConnrateThreshold = diffConnrateThreshold2;
                            } else {
                                logD("==> findAvailableCandidate begin matching for candidate:" + wnr2);
                                boolean refIs5G3 = refIs5G2;
                                int refSpeed2 = refRssi2;
                                double consuccRate = ((double) wnr2.mConnSuccCout) / ((double) (wnr2.mConnFailCount + wnr2.mConnSuccCout));
                                Iterator<ScanResult> it2 = scanList2.iterator();
                                int refScore2 = refScore;
                                int refRssi5 = refRssi4;
                                while (it2.hasNext()) {
                                    ScanResult result = it2.next();
                                    String str = "\"" + result.SSID + "\"";
                                    String scanBssid = result.BSSID;
                                    int scanRssi = result.level;
                                    if (WifiConfiguration.configKey(result).equals(wnr2.mConfigkey)) {
                                        if (scanBssid != null && scanBssid.equals(otherWifiInfo3.getBSSID())) {
                                            otherWifiInfo2 = otherWifiInfo3;
                                            refConsuccRate = refConsuccRate2;
                                        } else if ((!otherWifiInfo3.is5GHz() || !result.is5GHz()) && (!otherWifiInfo3.is24GHz() || !result.is24GHz())) {
                                            synchronized (this.mGatewayConflictBlackList) {
                                                try {
                                                    refConsuccRate = refConsuccRate2;
                                                    try {
                                                        if (this.mGatewayConflictBlackList.contains(otherWifiInfo3.getBSSID())) {
                                                            try {
                                                                if (this.mGatewayConflictBlackList.contains(scanBssid)) {
                                                                    logD("findAvailableCandidate skip gateway conflict BSS, wlan0:" + otherWifiInfo3.getBSSID() + " wlan1:" + scanBssid);
                                                                }
                                                            } catch (Throwable th) {
                                                                th = th;
                                                                while (true) {
                                                                    try {
                                                                        break;
                                                                    } catch (Throwable th2) {
                                                                        th = th2;
                                                                    }
                                                                }
                                                                throw th;
                                                            }
                                                        }
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } else {
                                            logD("findAvailableCandidate skip BSS of the same band, bssid:" + result.BSSID + " freq:" + result.frequency);
                                            otherWifiInfo2 = otherWifiInfo3;
                                            refConsuccRate = refConsuccRate2;
                                        }
                                        it2 = it2;
                                        candidate2 = candidate2;
                                        scanList2 = scanList2;
                                        diffConnrateThreshold2 = diffConnrateThreshold2;
                                        refConsuccRate2 = refConsuccRate;
                                        otherWifiInfo3 = otherWifiInfo2;
                                    } else {
                                        wnr = wnr2;
                                        otherWifiInfo2 = otherWifiInfo3;
                                        refConsuccRate = refConsuccRate2;
                                    }
                                    candidate2 = candidate2;
                                    refConsuccRate2 = refConsuccRate;
                                    it2 = it2;
                                    scanList2 = scanList2;
                                    diffConnrateThreshold2 = diffConnrateThreshold2;
                                    otherWifiInfo3 = otherWifiInfo2;
                                    wnr2 = wnr;
                                }
                                refRssi3 = refScore2;
                                refRssi4 = refRssi5;
                                it = it;
                                netRecords = netRecords;
                                refIs5G2 = refIs5G3;
                                refRssi2 = refSpeed2;
                            }
                        } else {
                            logD("findAvailableCandidate different configKey in roaming");
                            otherWifiInfo = otherWifiInfo3;
                            scanList = scanList2;
                            refRssi = refRssi4;
                            refIs5G = refIs5G2;
                            refSpeed = refRssi2;
                            diffConnrateThreshold = diffConnrateThreshold2;
                        }
                    }
                    it = it;
                    netRecords = netRecords;
                    refRssi3 = refScore;
                    refRssi4 = refRssi;
                    refIs5G2 = refIs5G;
                    refRssi2 = refSpeed;
                    scanList2 = scanList;
                    diffConnrateThreshold2 = diffConnrateThreshold;
                    otherWifiInfo3 = otherWifiInfo;
                }
                logD("findAvailableCandidate record config key is null or invalid or captive or blacklistCap2");
                it = it;
                netRecords = netRecords;
                refRssi3 = refScore;
                refRssi4 = refRssi;
                refIs5G2 = refIs5G;
                refRssi2 = refSpeed;
                scanList2 = scanList;
                diffConnrateThreshold2 = diffConnrateThreshold;
                otherWifiInfo3 = otherWifiInfo;
            }
            if (candidate2 == null) {
                logD("findAvailableCandidate candidate = null, mDataScore=" + this.mDataScore + ",manual=" + false + ",mDataAutoSwitch= " + this.mDataAutoSwitch + ",mDataState=" + this.mDataState);
            } else {
                logD("findAvailableCandidate mConnectingId=" + this.mConnectingId + ",candidate = " + candidate2.toString());
            }
            if (this.mAssistantUtils.getEnabledState() && !shouldIgnoreSwitch()) {
                if (lastRecord == null || lastRecord.mWifiConfiguration == null) {
                    this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, true);
                } else {
                    this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, false);
                }
                if (!(lastRecord == null || lastRecord.mWifiConfiguration == null || candidate2 == null) || (this.mDataAutoSwitch && this.mDataScore == 50 && this.mDataState && curScore <= 79 - this.mWlanInvalidThreshold)) {
                    this.mAssistantUtils.setCanShowDialog(this.mInterfaceName, true);
                }
            }
            if (candidate2 != null) {
                selectCandidateNetwork(candidate2);
            }
            return candidate2;
        }
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
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01c8, code lost:
        if (r7 != null) goto L_0x01ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0243, code lost:
        if (r7 == null) goto L_0x0246;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01f9 A[Catch:{ IOException -> 0x0220, RuntimeException -> 0x01fa, Exception -> 0x01d4, all -> 0x01cf, all -> 0x0247 }] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x021f A[Catch:{ IOException -> 0x0220, RuntimeException -> 0x01fa, Exception -> 0x01d4, all -> 0x01cf, all -> 0x0247 }] */
    public int sendHttpProbe(URL url) {
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
                        InetAddress gateway = this.mWifiStateMachine2.syncGetDhcpResults().gateway;
                        try {
                            if (addresses[0] != null) {
                                host = "/" + addresses[0].getHostAddress();
                                logD("SHP ht:" + host + ",gw:" + gateway);
                            }
                            if (addresses.length != 1 || gateway == null || !gateway.toString().equals(host)) {
                                this.mResponseGotFromGateway = false;
                                if (getCurrentState() == this.mDisconnectedState || this.mCurNetwork == null) {
                                    if (urlConnection != null) {
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
                                } catch (IOException e) {
                                    e = e;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
                                } catch (RuntimeException e2) {
                                    runtimeException = e2;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
                                    if (urlConnection != null) {
                                        urlConnection.disconnect();
                                    }
                                    return httpResponseCode;
                                } catch (Exception e3) {
                                    ee = e3;
                                    logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
                                    if (urlConnection != null) {
                                        urlConnection.disconnect();
                                    }
                                    return httpResponseCode;
                                }
                            } else {
                                this.mResponseGotFromGateway = true;
                                logD("SHP fgw !!");
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                return 599;
                            }
                        } catch (IOException e4) {
                            e = e4;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
                        } catch (RuntimeException e5) {
                            runtimeException = e5;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
                            if (urlConnection != null) {
                            }
                            return httpResponseCode;
                        } catch (Exception e6) {
                            ee = e6;
                            redirectUrl = null;
                            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
                            if (urlConnection != null) {
                            }
                            return httpResponseCode;
                        } catch (Throwable th) {
                            th = th;
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            throw th;
                        }
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return 599;
            }
        } catch (IOException e7) {
            e = e7;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
        } catch (RuntimeException e8) {
            runtimeException = e8;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
            if (urlConnection != null) {
            }
            return httpResponseCode;
        } catch (Exception e9) {
            ee = e9;
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
            if (urlConnection != null) {
            }
            return httpResponseCode;
        } catch (Throwable th2) {
            th = th2;
            if (urlConnection != null) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean sendParallelHttpProbes() {
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
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* Incorrect method signature, types: com.android.server.wifi.OppoWifiAssistantStateTraker2, java.net.URL */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker2.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker2.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker2.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker2.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker2.this.logD("Probe sleep finished!");
                        }
                        this.val$latch.countDown();
                    }
                    this.val$latch.countDown();
                }
            };
            AnonymousClass1ProbeThread httpProbe2 = new Thread(url2, finalResult, latch) {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* Incorrect method signature, types: com.android.server.wifi.OppoWifiAssistantStateTraker2, java.net.URL */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker2.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker2.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker2.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker2.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker2.this.logD("Probe sleep finished!");
                        }
                        this.val$latch.countDown();
                    }
                    this.val$latch.countDown();
                }
            };
            AnonymousClass1ProbeThread httpProbe3 = new Thread(url3, finalResult, latch) {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass1ProbeThread */
                private volatile Boolean mResult;
                private final URL mUrl;
                final /* synthetic */ AtomicReference val$finalResult;
                final /* synthetic */ CountDownLatch val$latch;

                /* Incorrect method signature, types: com.android.server.wifi.OppoWifiAssistantStateTraker2, java.net.URL */
                {
                    this.val$finalResult = r3;
                    this.val$latch = r4;
                    this.mUrl = url;
                }

                public Boolean getResult() {
                    return this.mResult;
                }

                public void run() {
                    int respCode = OppoWifiAssistantStateTraker2.this.sendHttpProbe(this.mUrl);
                    if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker2.HTTP_CAPTIVE_CODE_END) {
                        this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE);
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker2.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                        this.val$finalResult.compareAndSet(null, this.mResult);
                        this.val$finalResult.compareAndSet(false, this.mResult);
                        this.val$latch.countDown();
                        if (respCode != OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE) {
                            try {
                                sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                            } catch (InterruptedException e) {
                                OppoWifiAssistantStateTraker2.this.logD("Probe sleep interrupted!");
                            }
                            OppoWifiAssistantStateTraker2.this.logD("Probe sleep finished!");
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
    public boolean sendParallelHttpProbesExp() {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<Boolean> finalResult = new AtomicReference<>();
        URL httpUrl = makeURL(getExpHttpUrl());
        URL httpsUrl = makeURL(getExpHttpsUrl());
        AnonymousClass2ProbeThread httpProbe1 = new Thread(httpUrl, finalResult, latch) {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass2ProbeThread */
            private volatile Boolean mResult;
            private final URL mUrl;
            final /* synthetic */ AtomicReference val$finalResult;
            final /* synthetic */ CountDownLatch val$latch;

            /* Incorrect method signature, types: com.android.server.wifi.OppoWifiAssistantStateTraker2, java.net.URL */
            {
                this.val$finalResult = r3;
                this.val$latch = r4;
                this.mUrl = url;
            }

            public Boolean getResult() {
                return this.mResult;
            }

            public void run() {
                int respCode = OppoWifiAssistantStateTraker2.this.sendHttpProbe(this.mUrl);
                if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker2.HTTP_CAPTIVE_CODE_END) {
                    this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker2.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                    this.val$finalResult.compareAndSet(null, this.mResult);
                    this.val$finalResult.compareAndSet(false, this.mResult);
                    this.val$latch.countDown();
                    if (respCode != OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE) {
                        try {
                            sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                        } catch (InterruptedException e) {
                            OppoWifiAssistantStateTraker2.this.logD("Probe sleep interrupted!");
                        }
                        OppoWifiAssistantStateTraker2.this.logD("Probe sleep finished!");
                    }
                }
                this.val$latch.countDown();
            }
        };
        AnonymousClass2ProbeThread httpsProbe2 = new Thread(httpsUrl, finalResult, latch) {
            /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass2ProbeThread */
            private volatile Boolean mResult;
            private final URL mUrl;
            final /* synthetic */ AtomicReference val$finalResult;
            final /* synthetic */ CountDownLatch val$latch;

            /* Incorrect method signature, types: com.android.server.wifi.OppoWifiAssistantStateTraker2, java.net.URL */
            {
                this.val$finalResult = r3;
                this.val$latch = r4;
                this.mUrl = url;
            }

            public Boolean getResult() {
                return this.mResult;
            }

            public void run() {
                int respCode = OppoWifiAssistantStateTraker2.this.sendHttpProbe(this.mUrl);
                if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker2.HTTP_CAPTIVE_CODE_END) {
                    this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE);
                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                    oppoWifiAssistantStateTraker2.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                    this.val$finalResult.compareAndSet(null, this.mResult);
                    this.val$finalResult.compareAndSet(false, this.mResult);
                    this.val$latch.countDown();
                    if (respCode != OppoWifiAssistantStateTraker2.HTTP_NORMAL_CODE) {
                        try {
                            sleep((long) OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL);
                        } catch (InterruptedException e) {
                            OppoWifiAssistantStateTraker2.this.logD("Probe sleep interrupted!");
                        }
                        OppoWifiAssistantStateTraker2.this.logD("Probe sleep finished!");
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
    public void detectInternet() {
        if (!this.mDetectNet || this.mInternetDetecting) {
            logD("DI, no need check");
        } else if (this.mWifiStateMachine2.isRoaming()) {
            logD("DI, ring");
        } else {
            logD("detectInternet...");
            this.mLastInternetResult = this.mInterResult;
            Handler handler = this.mInterThread;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass4 */

                    public void run() {
                        boolean probeResult;
                        String dectConfig = OppoWifiAssistantStateTraker2.this.mLastConfigkey;
                        OppoWifiAssistantStateTraker2.access$10608(OppoWifiAssistantStateTraker2.this);
                        long unused = OppoWifiAssistantStateTraker2.this.mLastDetectInter = System.currentTimeMillis();
                        boolean unused2 = OppoWifiAssistantStateTraker2.this.mInternetDetecting = true;
                        if (OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.isChineseOperator()) {
                            probeResult = OppoWifiAssistantStateTraker2.this.sendParallelHttpProbes();
                        } else {
                            probeResult = OppoWifiAssistantStateTraker2.this.sendParallelHttpProbesExp();
                        }
                        boolean unused3 = OppoWifiAssistantStateTraker2.this.mInternetDetecting = false;
                        if (dectConfig != null && !dectConfig.equals(OppoWifiAssistantStateTraker2.this.mLastConfigkey)) {
                            return;
                        }
                        if (probeResult) {
                            boolean unused4 = OppoWifiAssistantStateTraker2.this.mCaptivePortal = false;
                            int unused5 = OppoWifiAssistantStateTraker2.this.mInternetInvalidCount = 0;
                            if (!OppoWifiAssistantStateTraker2.this.mInterResult) {
                                OppoWifiAssistantStateTraker2.this.sendMessageForNetChange(true);
                            }
                        } else if (!OppoWifiAssistantStateTraker2.this.mInterResult || OppoWifiAssistantStateTraker2.this.mLastPktInfo == null || OppoWifiAssistantStateTraker2.this.mLastPktInfo.rssi < -75) {
                            int unused6 = OppoWifiAssistantStateTraker2.this.mInternetInvalidCount = 0;
                        } else {
                            if (OppoWifiAssistantStateTraker2.this.mInternetInvalidCount == 0) {
                                OppoWifiAssistantStateTraker2.this.logD("DI, change to unvailable, detect again before set");
                                if (OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.isChineseOperator()) {
                                    Intent dnsRecoveryIntent = new Intent(OppoWifiAssistantStateTraker2.ACTION_WIFI_NETWORK_INTERNET_INVAILD);
                                    dnsRecoveryIntent.addFlags(67108864);
                                    OppoWifiAssistantStateTraker2.this.mContext.sendBroadcastAsUser(dnsRecoveryIntent, UserHandle.ALL);
                                }
                                OppoWifiAssistantStateTraker2.access$7308(OppoWifiAssistantStateTraker2.this);
                            } else {
                                int unused7 = OppoWifiAssistantStateTraker2.this.mInternetInvalidCount = 0;
                                OppoWifiAssistantStateTraker2.this.sendMessageForNetChange(false);
                            }
                            OppoWifiAssistantStateTraker2.this.setInternetDetectAlarm(0, RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void setInternetDetectAlarm(int type, long delay) {
        this.mAlarmManager.cancel(this.mDetectInterIntent);
        this.mDetectInterIntent = getPrivateBroadcast(ACTION_DETECT_INTERNET);
        this.mAlarmManager.set(type, System.currentTimeMillis() + delay, this.mDetectInterIntent);
    }

    /* access modifiers changed from: private */
    public void sendMessageForNetChange(boolean valid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_INTERNET_CHANGE, valid ? 1 : 0, 0, this.mLastConfigkey));
    }

    /* access modifiers changed from: private */
    public boolean needToDetectTcpStatus() {
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0155  */
    public boolean detectTcpStatus() {
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
    public boolean isThirdAppOperate() {
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
    public void dismissDialog(int type) {
        AlertDialog alertDialog = mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        ColorListDialog colorListDialog = mDataAlertDialog;
        if (colorListDialog != null) {
            colorListDialog.dismiss();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0131, code lost:
        return;
     */
    private void showDialog(OppoWifiAssistantRecord lastRecord) {
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
                String ssid = lastRecord.mWifiConfiguration.SSID;
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
                builder.setPositiveButton(201653541, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass7 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker2.mAlertDialog != null) {
                            OppoWifiAssistantStateTraker2.mAlertDialog.dismiss();
                        }
                    }
                });
                builder.setTitle((ssid + ((Object) this.mContext.getText(201653534))) + ((Object) this.mContext.getText(201653540)));
                synchronized (mDialogLock) {
                    if (mAlertDialog != null) {
                        logD("showDialog multi-thread access!!");
                        return;
                    }
                    mAlertDialog = builder.create();
                    mAlertDialog.setCanceledOnTouchOutside(false);
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass8 */

                        public void onDismiss(DialogInterface dialog) {
                            AlertDialog unused = OppoWifiAssistantStateTraker2.mAlertDialog = null;
                            OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker2.this.mContext, false);
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
    public void showDialogForData(OppoWifiAssistantRecord lastRecord) {
        String title;
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
            String ssid = lastRecord.mWifiConfiguration.SSID;
            AlertDialog alertDialog = mAlertDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            ColorListDialog colorListDialog = mDataAlertDialog;
            if (colorListDialog != null) {
                colorListDialog.dismiss();
            }
            mDataAlertDialog = new ColorListDialog(this.mContext, 201523207);
            CharSequence[] items = {this.mContext.getText(201653539), this.mContext.getText(201653541)};
            if (lastRecord.mNetworkValid2) {
                title = ssid + ((Object) this.mContext.getText(201653535));
            } else {
                title = ssid + ((Object) this.mContext.getText(201653534));
            }
            mDataAlertDialog.setTitle(title + ((Object) this.mContext.getText(201653537)));
            mDataAlertDialog.setMessage(this.mContext.getText(201653538));
            mDataAlertDialog.setItems(items, (int[]) null, new DialogInterface.OnClickListener() {
                /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass9 */

                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        OppoWifiAssistantStateTraker2.this.logD("sdfn no remind");
                        Settings.System.putInt(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker2.NOT_REMIND_WIFI_ASSISTANT, 1);
                        if (OppoWifiAssistantStateTraker2.mDataAlertDialog != null) {
                            OppoWifiAssistantStateTraker2.mDataAlertDialog.dismiss();
                        }
                    } else if (which == 1 && OppoWifiAssistantStateTraker2.mDataAlertDialog != null) {
                        OppoWifiAssistantStateTraker2.mDataAlertDialog.dismiss();
                    }
                }
            });
            synchronized (mDialogLock) {
                if (mAlertDialog != null) {
                    logD("showDialogForData multi-thread access!!");
                    return;
                }
                mDataAlertDialog.getDialog().setCanceledOnTouchOutside(false);
                mDataAlertDialog.getDialog().setCancelable(false);
                mDataAlertDialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass10 */

                    public void onDismiss(DialogInterface dialog) {
                        ColorListDialog unused = OppoWifiAssistantStateTraker2.mDataAlertDialog = null;
                        OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker2.this.mContext, false);
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
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass11 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker2.mSlaDialog != null) {
                            OppoWifiAssistantStateTraker2.mSlaDialog.dismiss();
                            Settings.System.putInt(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", 3);
                            OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setShowDialog(false);
                            Log.d(OppoWifiAssistantStateTraker2.TAG, "showDialogForSla enable SLA switch..");
                            Settings.Global.putInt(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), OppoSlaManager.KEY_SLA_SWITCH, 1);
                        }
                    }
                });
                builder.setNegativeButton(201653622, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass12 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker2.mSlaDialog != null) {
                            OppoWifiAssistantStateTraker2.mSlaDialog.dismiss();
                            int slaCancelCount = Settings.System.getInt(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), "SLA_CANCEL_COUNT", 0);
                            Settings.System.putInt(OppoWifiAssistantStateTraker2.this.mContext.getContentResolver(), "SLA_CANCEL_COUNT", slaCancelCount + 1);
                            Log.d(OppoWifiAssistantStateTraker2.TAG, "showDialogForSla Cancel clicked..cancelCount=" + (slaCancelCount + 1));
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
                        /* class com.android.server.wifi.OppoWifiAssistantStateTraker2.AnonymousClass13 */

                        public void onDismiss(DialogInterface dialog) {
                            AlertDialog unused = OppoWifiAssistantStateTraker2.mSlaDialog = null;
                            OppoWifiCommonUtil.disableStatusBar(OppoWifiAssistantStateTraker2.this.mContext, false);
                        }
                    });
                    OppoWifiCommonUtil.disableStatusBar(this.mContext, true);
                    WindowManager.LayoutParams p = mSlaDialog.getWindow().getAttributes();
                    p.ignoreHomeMenuKey = 1;
                    p.privateFlags = 16;
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
    public boolean hasCheckNoRemind() {
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
            boolean blacklistCapUrl = false;
            switch (msg.what) {
                case OppoWifiAssistantStateTraker2.EVENT_UPDATE_NETWORK_STATE /*{ENCODED_INT: 200714}*/:
                    WifiInfo updateWifiInfo = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.syncRequestConnectionInfo();
                    if (!OppoWifiAssistantStateTraker2.this.mIsSoftAP || !OppoWifiAssistantStateTraker2.this.isThirdAppOperate()) {
                        String netStateSsid = (String) msg.obj;
                        OppoWifiAssistantStateTraker2.this.logD("nss: " + netStateSsid + ",info: " + updateWifiInfo);
                        if (updateWifiInfo != null && netStateSsid != null && updateWifiInfo.getSSID().equals(netStateSsid)) {
                            boolean netValid = msg.arg1 == 0;
                            OppoWifiAssistantStateTraker2.this.logD("nst: " + netValid);
                            if (netValid || !OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.isDupDhcp()) {
                                if (OppoWifiAssistantStateTraker2.this.mInterResult && !netValid) {
                                    boolean unused = OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid = true;
                                }
                                WifiConfiguration netConf = OppoWifiAssistantStateTraker2.this.getWifiConfig(netStateSsid, updateWifiInfo.getBSSID());
                                if (netConf != null && netConf.networkId == OppoWifiAssistantStateTraker2.this.mConnectedId) {
                                    if (OppoWifiAssistantStateTraker2.this.mGotInternetResult < 4 || OppoWifiAssistantStateTraker2.this.mCaptivePortal || netValid != OppoWifiAssistantStateTraker2.this.mInterResult) {
                                        OppoWifiAssistantStateTraker2.this.updateRecordInternetStateAndTime(netConf.configKey(false), netValid, true);
                                    }
                                    if (netValid && netValid != OppoWifiAssistantStateTraker2.this.mInterResult && OppoWifiAssistantStateTraker2.this.mWifiStateMachine2 != null && OppoWifiAssistantStateTraker2.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                                        OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
                                    }
                                    boolean unused2 = OppoWifiAssistantStateTraker2.this.mInterResult = netValid;
                                    if (netValid) {
                                        OppoWifiAssistantStateTraker2.access$4176(OppoWifiAssistantStateTraker2.this, 4);
                                        OppoWifiAssistantStateTraker2.access$4172(OppoWifiAssistantStateTraker2.this, -9);
                                        return;
                                    }
                                    OppoWifiAssistantStateTraker2.access$4176(OppoWifiAssistantStateTraker2.this, 8);
                                    OppoWifiAssistantStateTraker2.access$4172(OppoWifiAssistantStateTraker2.this, -5);
                                    return;
                                } else if (OppoWifiAssistantStateTraker2.this.getCurrentState() != OppoWifiAssistantStateTraker2.this.mConnectedState && OppoWifiAssistantStateTraker2.this.getCurrentState() != OppoWifiAssistantStateTraker2.this.mVerifyInternetState) {
                                    return;
                                } else {
                                    if (netConf == null) {
                                        OppoWifiAssistantStateTraker2.this.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker2.TYPE_MONITOR_EXP);
                                        return;
                                    }
                                    OppoWifiAssistantRecord expInfo = new OppoWifiAssistantRecord();
                                    expInfo.mNetid = netConf.networkId;
                                    expInfo.mConfigkey = netConf.configKey();
                                    OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                                    oppoWifiAssistantStateTraker2.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker2.TYPE_MONITOR_EXP, expInfo, null, oppoWifiAssistantStateTraker2.mConnectedId);
                                    return;
                                }
                            } else {
                                OppoWifiAssistantStateTraker2.this.logD("[bug#1131400] dupDhcp, wait DHCP retry.");
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case OppoWifiAssistantStateTraker2.EVENT_CONNECT_NETWORK /*{ENCODED_INT: 200715}*/:
                    WifiInfo curWifiInfo = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.syncRequestConnectionInfo();
                    if (curWifiInfo != null) {
                        int netId = msg.arg1;
                        LinkProperties curLink = (LinkProperties) msg.obj;
                        if (OppoWifiAssistantStateTraker2.this.mCM == null) {
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = OppoWifiAssistantStateTraker2.this;
                            ConnectivityManager unused3 = oppoWifiAssistantStateTraker22.mCM = (ConnectivityManager) oppoWifiAssistantStateTraker22.mContext.getSystemService("connectivity");
                        }
                        Network network = OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.getCurrentNetwork();
                        if (OppoWifiAssistantStateTraker2.this.mCM != null && network != null && network.netId == netId) {
                            if (OppoWifiAssistantStateTraker2.this.mCM.getNetworkInfo(network) == null) {
                                OppoWifiAssistantStateTraker2.this.log("network isn't connected. old netId = " + netId + ", curSsid:" + curWifiInfo.getSSID());
                                return;
                            }
                            Network unused4 = OppoWifiAssistantStateTraker2.this.mCurNetwork = network;
                            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker23 = OppoWifiAssistantStateTraker2.this;
                            boolean unused5 = oppoWifiAssistantStateTraker23.mIsSoftAP = oppoWifiAssistantStateTraker23.isSoftAp(curLink);
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiAssistantStateTraker2.EVENT_INTERNET_CHANGE /*{ENCODED_INT: 200722}*/:
                    boolean interResult = msg.arg1 == 1;
                    String interConf = (String) msg.obj;
                    OppoWifiAssistantStateTraker2.this.logD("ir=" + interResult + "ic=" + interConf);
                    if (interResult) {
                        boolean unused6 = OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid = false;
                        if (OppoWifiAssistantStateTraker2.this.mLastConfigkey != null && OppoWifiAssistantStateTraker2.this.mLastConfigkey.equals(OppoWifiAssistantStateTraker2.this.mUnavailableKey)) {
                            OppoWifiAssistantStateTraker2.this.dismissDialog(2);
                        }
                    } else {
                        boolean unused7 = OppoWifiAssistantStateTraker2.this.mInterChangeToInvalid = true;
                    }
                    OppoWifiAssistantStateTraker2.this.updateRecordInternetStateAndTime(interConf, interResult, false);
                    boolean unused8 = OppoWifiAssistantStateTraker2.this.mInterResult = interResult;
                    if (!OppoWifiAssistantStateTraker2.this.mChangedToData && OppoWifiAssistantStateTraker2.this.mInterResult && !OppoWifiAssistantStateTraker2.this.mLastInternetResult && OppoWifiAssistantStateTraker2.this.mWifiStateMachine2 != null && OppoWifiAssistantStateTraker2.this.mAssistantUtils.isPrimaryWifi(OppoWifiAssistantStateTraker2.this.mInterfaceName)) {
                        OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.sendWifiNetworkScore(79, true);
                    }
                    if (OppoWifiAssistantStateTraker2.this.mLastPktInfo != null) {
                        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker24 = OppoWifiAssistantStateTraker2.this;
                        oppoWifiAssistantStateTraker24.updateRecordLinkQuality(oppoWifiAssistantStateTraker24.mLastPktInfo);
                    }
                    if (interResult && !OppoWifiAssistantStateTraker2.this.mChangedNetwork && OppoWifiAssistantStateTraker2.this.mWifiStateMachine2 != null) {
                        OppoWifiAssistantStateTraker2.this.mWifiStateMachine2.setNetworkStatus(true);
                        return;
                    }
                    return;
                case OppoWifiAssistantStateTraker2.EVENT_CAPTIVE_PORTAL /*{ENCODED_INT: 200723}*/:
                    boolean newConf = Integer.valueOf(msg.arg1).intValue() == 1;
                    String captiveConf = (String) msg.obj;
                    if (Integer.valueOf(msg.arg2).intValue() == 1) {
                        blacklistCapUrl = true;
                    }
                    OppoWifiAssistantStateTraker2.this.logD("nc=" + newConf + ",cc=" + captiveConf + ",blcu=" + blacklistCapUrl);
                    OppoWifiAssistantStateTraker2.this.updateRecordCaptiveState(captiveConf, true, true, blacklistCapUrl);
                    return;
                case OppoWifiAssistantStateTraker2.EVENT_SCAN_TIMEOUT /*{ENCODED_INT: 200724}*/:
                    boolean unused9 = OppoWifiAssistantStateTraker2.this.mTriggerScan = false;
                    return;
                default:
                    return;
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
    public void sendNetworkStateBroadcast(String configkey, boolean valid) {
        if (valid || !this.mWifiStateMachine2.isDupDhcp()) {
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
    public void sendVerifyBroadcast(String configKey) {
        Intent verifyIntent = new Intent(WIFI_ASSISTANT_VERIFY);
        verifyIntent.addFlags(67108864);
        verifyIntent.putExtra(EXTRA_VERIFY_CONFIG, configKey);
        logD("svb");
        this.mContext.sendStickyBroadcastAsUser(verifyIntent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public void setAssistantStatistics(String eventId, String type) {
        setAssistantStatistics(eventId, type, null, null, -127);
    }

    /* access modifiers changed from: private */
    public void setAssistantStatistics(String eventId, String type, OppoWifiAssistantRecord cw, OppoWifiAssistantRecord sw, int extra1) {
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
            if (OppoWifiAssistantStateTraker2.this.mTelephonyManager != null) {
                OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker2.this;
                boolean unused = oppoWifiAssistantStateTraker2.mDataState = oppoWifiAssistantStateTraker2.mTelephonyManager.getDataEnabled();
                OppoWifiAssistantStateTraker2.this.mOppoSlaManager.setCellState(OppoWifiAssistantStateTraker2.this.mDataState);
                if (!OppoWifiAssistantStateTraker2.this.mDataState) {
                    long disableData = System.currentTimeMillis() - OppoWifiAssistantStateTraker2.this.mAutoConnDataTime;
                    if (OppoWifiAssistantStateTraker2.this.mChangedNetwork && OppoWifiAssistantStateTraker2.this.mChangedToData) {
                        if (OppoWifiAssistantStateTraker2.this.mAutoConnDataTime > 0 && disableData > 0 && disableData < 180000) {
                            OppoWifiAssistantStateTraker2.this.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_MANUAL_LIMIT, OppoWifiAssistantStateTraker2.TYPE_DIABLE_DATA);
                            long unused2 = OppoWifiAssistantStateTraker2.this.mAutoConnDataTime = 0;
                        }
                        OppoWifiAssistantStateTraker2.this.logD("stc dwc2");
                        OppoWifiAssistantStateTraker2.this.setAssistantStatistics(OppoWifiAssistantStateTraker2.STATISTIC_MANUAL_OPERATE, OppoWifiAssistantStateTraker2.TYPE_DIABLE_DATA);
                    }
                    if (!OppoWifiAssistantStateTraker2.this.mCanTriggerData) {
                        OppoWifiAssistantStateTraker2.this.mAssistantUtils.releaseDataNetwork();
                        boolean unused3 = OppoWifiAssistantStateTraker2.this.mCanTriggerData = true;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void logD(String log) {
        if (sDebug) {
            Log.d(TAG, "" + log);
        }
    }

    /* access modifiers changed from: private */
    public void logE(String log) {
        Log.e(TAG, "" + log);
    }

    public void updateNetworkState(int result, String ssid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_UPDATE_NETWORK_STATE, result, 0, ssid));
    }

    public void updateNetworkInfo(int netId, LinkProperties linkProperties) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_CONNECT_NETWORK, netId, 0, linkProperties));
    }

    public void handleGatewayConflict() {
        logD("handleGatewayConflict currentState=" + getCurrentState());
        WifiInfo curWifiInfo = this.mWifiStateMachine2.syncRequestConnectionInfo();
        WifiInfo otherWifiInfo = this.mAssistantUtils.getOtherIfWifiInfo(this.mInterfaceName);
        if (curWifiInfo != null && otherWifiInfo != null) {
            synchronized (this.mGatewayConflictBlackList) {
                this.mGatewayConflictBlackList.add(curWifiInfo.getBSSID());
                this.mGatewayConflictBlackList.add(otherWifiInfo.getBSSID());
            }
            logE("dulplicate gateway IP addr and different MAC addr, disconnect wlan1 now!");
            this.mWifiStateMachine2.disconnectCommand();
            sendMessage(EVENT_START_SCAN);
        }
    }
}
