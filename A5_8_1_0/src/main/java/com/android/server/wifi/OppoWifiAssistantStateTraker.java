package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ColorSystemUpdateDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.RouteInfo;
import android.net.TrafficStats;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import oppo.util.OppoStatistics;

class OppoWifiAssistantStateTraker extends StateMachine {
    /* renamed from: -android-net-wifi-SupplicantStateSwitchesValues */
    private static final /* synthetic */ int[] f185-android-net-wifi-SupplicantStateSwitchesValues = null;
    private static final String ACTION_DETECT_INTERNET = "adnroid.net.wifi.DETECT_INTER";
    private static final String ACTION_WIFI_NETWORK_AVAILABLE = "android.net.wifi.OPPO_WIFI_VALID";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_NOT_AVAILABLE = "android.net.wifi.OPPO_WIFI_INVALID";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final String ANT_STR = "ant=";
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
    public static final int CMD_TCP_MONITOR = 200711;
    public static final int CMD_TRAFFIC_MONITOR = 200709;
    private static final String CONKEY_STR = "conkey=";
    private static final String CSC_STR = "csc=";
    private static final int DATA_INVALID_SCORE = 10;
    private static final int DATA_NETWORK_VALID = 50;
    private static final String DATA_SCORE_CHANGE = "android.net.wifi.OPPO_DATA_NET_CHANGE";
    private static final int DATA_VALID_SCORE = 50;
    private static final int DEFAULT_SCORE = 79;
    private static final String DEFAULT_SPECIAL_URL = "360.cn";
    private static final int DETAIL_CAPTIVE = 1;
    private static final int DETAIL_CONNECTED = 2;
    private static final int DETAIL_DISCONNECTED = 0;
    private static final int DETAIL_IDLE = -1;
    private static final int DIFF_RSSI_THRESHOLD = 5;
    private static final int DISABLE_INTERFACE = -1;
    private static final int EVENT_ADD_UPDATE_NETWORK = 200714;
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
    public static final int EVENT_WIFI_STATE_CHANGE = 200706;
    private static final double EXP_COEFFICIENT_MONITOR = 0.5d;
    private static final String EXTRA_DATA_CORE = "data_score";
    private static final String EXTRA_ENALE_DATA = "enableData";
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
    private static final String HF_STR = "hf=";
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
    private static final String INT_STR = "int=";
    private static final int INVALID_INFO = -127;
    private static final int ITEM_NO_REMIND = 0;
    private static final int ITEM_REMIND_EVERYTIME = 1;
    private static final String KEY_CURRENT_WLAN = "key_current_wlan";
    private static final String KEY_SELECT_WLAN = "key_select_wlan";
    private static final String LST_STR = "lst=";
    private static final String MS_STR = "ms=";
    private static final int NETQUALITY_HISTORY_COUNTS = 3;
    private static final int NET_DETECT_TIMEOUT = 40000;
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
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
    private static final double STATIC_LOW_LINK_LOSS_THRESHOLD = 0.5d;
    private static final int STATIC_LOW_LINK_SCORE_THRESHOLD = 30;
    private static final int STATIC_LOW_RSSI_24 = -75;
    private static final int STATIC_LOW_RSSI_5 = -72;
    private static final int STATIC_LOW_RSSI_SCORE_THRESHOLD = 20;
    private static final int STATIC_LOW_TCP_SCORE_THRESHOLD = 5;
    private static final int STATIC_NETINVALID_COUNT = 1;
    private static final int STATIC_NO_TRIFFIC_SCORE_THRESHOLD = 5;
    private static final int STATIC_ROAM_DETECT_TIMES = 6;
    private static final int STATIC_RSSI_TO_WLAN_THRESHOLD = -73;
    private static final int STATIC_SCAN_TIMEOUT = 6000;
    private static final int STATIC_SCORE_NETWORK_GOOD = 64;
    private static final int STATIC_TRAFFIC_SAMPL_INTERVAL_INVALID = 2000;
    private static final int STATIC_TRIGGER_SCAN_COUNT = 3;
    private static final int STATIC_WLAN_INVALID_THRESHOLD = 40;
    private static final int STATIC_WLAN_NETWORK_INVALID = 10;
    private static final int STATIC_WLAN_POLL_THRESHOLD = 15;
    private static final int STATIC_WLAN_TRIGGER_DATA_THRESHOLD = 20;
    private static final int STATIC_WLAN_TRIGGER_WLAN_THRESHOLD = 25;
    private static final String STATISTIC_AUTO_CONN = "event_auto_conn";
    private static final String STATISTIC_MANUAL_LIMIT = "event_manual_limit";
    private static final String STATISTIC_MANUAL_OPERATE = "event_manual_operate";
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
    private static final String mInterfaceName = "wlan0";
    private static boolean sDebug = false;
    private long mAccessNetTime;
    private AlarmManager mAlarmManager;
    private AlertDialog mAlertDialog = null;
    private long mAutoConnDataTime;
    private long mAutoConnWlanTime;
    private long mAutoDataToWlanTime;
    private boolean mAutoSwitch = true;
    private int mAutoSwitchDataCount = 0;
    private long mAutoSwitchDataDisableTime = 0;
    private int mAutoSwitchDataIndex = 0;
    private long mAutoSwitchDataTime = 0;
    private long[] mAutoSwitchDataTimes = new long[]{0, 0, 0, 0, 0};
    private boolean mAutoSwithToData = false;
    private CharSequence mAvailableAP;
    private Handler mBroadHandle;
    private String mBroadInfo;
    private BroadcastReceiver mBroadcastReceiver;
    private ConnectivityManager mCM;
    private String mCandidateKey = "";
    private boolean mCaptivePortal = false;
    private boolean mChangeNetwork = false;
    private int mChangeRssi = 0;
    private int mChangeScore = 0;
    private boolean mClickDialogSwitch = false;
    private State mCompletedState = new CompletedState();
    private int mConnFail;
    private int mConnectId = -1;
    private String mConnectKey;
    private String mConnectSSID = null;
    private long mConnectTime;
    private int mConnectedId = -1;
    private State mConnectedState = new ConnectedState();
    private int mConnectingId = -1;
    private String mConnectingkey;
    private Context mContext;
    private Network mCurNetwork;
    private VolumeWeightedEMA mCurrentLoss;
    private long mDTxPkts;
    private ColorSystemUpdateDialog mDataAlertDialog = null;
    private boolean mDataAutoSwitch = true;
    private int mDataScore = 10;
    private boolean mDataState = false;
    private DataStateObserver mDataStateObserver = null;
    private State mDefaultState = new DefaultState();
    private int mDetectInterCount = 0;
    private PendingIntent mDetectInterIntent;
    private boolean mDetectNet = false;
    private State mDisconnectState = new DisConnectedState();
    private String[] mFallbackHttpServers = new String[]{"http://info.3g.qq.com", "http://www.google.cn/generate_204", "http://developers.google.cn/generate_204"};
    private boolean mFeatureState = true;
    private CharSequence mGoodAvailableAP;
    private int mGotInternetResult = 0;
    private Handler mHandler;
    private State mHandshakeState = new HandshakeState();
    private int mIndex = 0;
    private boolean mInitAutoConnect = true;
    private State mInitState = new InitState();
    private boolean mInterChangeToInvalid = false;
    private int mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
    private boolean mInterResult = false;
    private Handler mInterThread;
    private String[] mInternalServers = new String[]{"conn1.oppomobile.com", "conn2.oppomobile.com"};
    private boolean mInternetDetecting = false;
    private int mInternetInvalidCount = 0;
    private int mInternetStandoffTime = 0;
    private int mIsNewConfig;
    private boolean mIsScreenOn = true;
    private boolean mIsSoftAP = false;
    private String mLastBssid = " ";
    private String mLastConfigkey;
    private long mLastDetectInter;
    private boolean mLastInternetResult = false;
    private int mLastNetId = -1;
    private RssiPacketCountInfo mLastPkgInfo;
    private long mLastRxPkts;
    private long mLastScanTime = 0;
    private boolean mLastToData = false;
    private long mLastTrigDataTime;
    private int mLastTxBad;
    private int mLastTxGood;
    private long mLastTxPkts;
    private long mLastuseTime;
    private int mLinkDetectTimes = 0;
    private int mLinkInterval;
    private double[] mLossArray = new double[]{this.mLossInit, this.mLossInit, this.mLossInit, this.mLossInit};
    private double mLossInit = 0.0d;
    private boolean mManualConnect = false;
    private long mManualDialogAutoDismissTime = 0;
    private int[] mNetQulityArray = new int[]{79, 79, 79, 79};
    private int mNetQulityGoodCount = 0;
    private State mNetworkMonitorState = new NetworkMonitorState();
    private String mNewBssid = " ";
    private String mNewSsid = " ";
    private String mOldBssid = " ";
    private String mOldSsid = " ";
    private int mOldTcpStatus = 0;
    private OppoTcpInfoMonitor mOppoTcpInfoMonitor = null;
    private String[] mPublicHttpsServers = new String[]{"https://m.baidu.com", "https://sina.cn", "https://m.sohu.com"};
    private boolean mResponseGotFromGateway = false;
    private int mRoamdetectCount = 0;
    private int mRssiFetchToken = 0;
    private int mRxPktsLowCount = 0;
    private boolean mScreenOn = true;
    private List<OppoWifiAssistantRecord> mSortNetworkRecord = new ArrayList();
    private List<WifiConfiguration> mSortWifiConfig = new ArrayList();
    private SupplicantStateTracker mSupplicantTracker;
    private int mTcpInterval;
    private int mTcpLinkStatus = 0;
    private int mTcpShortIntervalCount = 0;
    private int mTcpStatistics = 0;
    private int[] mTcpstateArray = new int[]{0, 0, 0, 0};
    private TelephonyManager mTelephonyManager;
    private String mTestApk = "com.oppo.wifiassistant";
    private boolean mToData = false;
    private int mTrafficInteval;
    private int mTrigScanCount = 0;
    private boolean mTriggerData = true;
    private boolean mTriggerInter = false;
    private boolean mTriggerScan = false;
    private String mUnavailableKey = " ";
    private State mVerifyInternetState = new VerifyInternetState();
    private OppoWifiAssistantUpdateHelper mWah;
    private WifiConfigManager mWifiConfigManager;
    private WifiConfigStore mWifiConfigStore;
    private WifiNative mWifiNative;
    private HashMap<String, OppoWifiAssistantRecord> mWifiNetworkRecord = new HashMap();
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private OppoWifiSmartSwitcher mWifiSmartSwitcher;
    private int mWifiState = 1;
    private WifiStateMachine mWifiStateMachine;
    private boolean mWifiStateMachineConnected = false;
    private int mWlanInvalidThreshold = 40;
    protected final DelayedDiskWrite mWriter;
    private AsyncChannel mWsmChannel = new AsyncChannel();
    private long mdRxPkts;

    /* renamed from: com.android.server.wifi.OppoWifiAssistantStateTraker$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private volatile Boolean mResult;
        private final URL mUrl;
        final /* synthetic */ AtomicReference val$finalResult;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(URL url, AtomicReference atomicReference, CountDownLatch countDownLatch) {
            this.val$finalResult = atomicReference;
            this.val$latch = countDownLatch;
            this.mUrl = url;
        }

        public Boolean getResult() {
            return this.mResult;
        }

        public void run() {
            int respCode = OppoWifiAssistantStateTraker.this.sendHttpProbe(this.mUrl);
            if (respCode >= 200 && respCode <= OppoWifiAssistantStateTraker.HTTP_CAPTIVE_CODE_END) {
                this.mResult = Boolean.valueOf(respCode == OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE);
                OppoWifiAssistantStateTraker.this.logD("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                this.val$finalResult.compareAndSet(null, this.mResult);
                this.val$finalResult.compareAndSet(Boolean.valueOf(false), this.mResult);
                this.val$latch.countDown();
                if (respCode != OppoWifiAssistantStateTraker.HTTP_NORMAL_CODE) {
                    try {
                        AnonymousClass1ProbeThread.sleep(2000);
                    } catch (InterruptedException e) {
                        OppoWifiAssistantStateTraker.this.logD("Probe sleep interrupted!");
                    }
                    OppoWifiAssistantStateTraker.this.logD("Probe sleep finished!");
                }
                this.val$latch.countDown();
            }
            this.val$latch.countDown();
        }
    }

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mGotInternetResult = 0;
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            WifiConfiguration config;
            switch (msg.what) {
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    if (OppoWifiAssistantStateTraker.this.mLastNetId != msg.arg1) {
                        OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                        config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                        OppoWifiAssistantStateTraker.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    int completestate = msg.arg2;
                    OppoWifiAssistantStateTraker.this.logD("cptst = " + completestate + ",msg.arg1= " + msg.arg1);
                    config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    if (config != null) {
                        OppoWifiAssistantStateTraker.this.mLastConfigkey = config.configKey(false);
                        OppoWifiAssistantStateTraker.this.logD("cptst = " + completestate + ",mLastConfigkey= " + OppoWifiAssistantStateTraker.this.mLastConfigkey + ",mLastNetId= " + OppoWifiAssistantStateTraker.this.mLastNetId + ",mConnectSSID= " + OppoWifiAssistantStateTraker.this.mConnectSSID);
                        if (completestate != 0) {
                            if (completestate != 1) {
                                if (completestate == 2) {
                                    OppoWifiAssistantStateTraker.this.mConnectSSID = config.SSID;
                                    OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mConnectedState);
                                    break;
                                }
                            }
                            OppoWifiAssistantStateTraker.this.mConnectSSID = config.SSID;
                            OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mVerifyInternetState);
                            break;
                        }
                        if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                            OppoWifiAssistantStateTraker.this.dismissDialog(0);
                        }
                        OppoWifiAssistantStateTraker.this.updateRecordDisableState(OppoWifiAssistantStateTraker.this.mLastConfigkey);
                        OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mDisconnectState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mConnectTime = System.currentTimeMillis();
            OppoWifiAssistantStateTraker.this.mConnectedId = OppoWifiAssistantStateTraker.this.mLastNetId;
            OppoWifiAssistantStateTraker.this.logD("ConnectedState mLastConfigkey= " + OppoWifiAssistantStateTraker.this.mLastConfigkey + ", mConnectedId= " + OppoWifiAssistantStateTraker.this.mConnectedId);
            OppoWifiAssistantStateTraker.this.updateRecordConCount(OppoWifiAssistantStateTraker.this.mLastConfigkey);
            if (OppoWifiAssistantStateTraker.this.mCandidateKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                OppoWifiAssistantStateTraker.this.mAutoConnWlanTime = OppoWifiAssistantStateTraker.this.mConnectTime;
                OppoWifiAssistantStateTraker.this.mAutoConnDataTime = 0;
                OppoWifiAssistantStateTraker.this.mAutoDataToWlanTime = 0;
                OppoWifiAssistantStateTraker.this.logD("stc wwa");
                OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_WLAN_TO_WLAN, (OppoWifiAssistantRecord) OppoWifiAssistantStateTraker.this.mWifiNetworkRecord.get(OppoWifiAssistantStateTraker.this.mLastConfigkey), null, -127);
                OppoWifiAssistantStateTraker.this.mCandidateKey = "";
            }
            OppoWifiAssistantStateTraker.this.mTcpStatistics = 0;
            OppoWifiAssistantStateTraker.this.mTcpInterval = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
            OppoWifiAssistantStateTraker.this.mOppoTcpInfoMonitor.resetTcpLinkStatus();
            OppoWifiAssistantStateTraker.this.removeMessages(OppoWifiAssistantStateTraker.CMD_TCP_MONITOR);
            OppoWifiAssistantStateTraker.this.sendMessageDelayed(OppoWifiAssistantStateTraker.this.obtainMessage(OppoWifiAssistantStateTraker.CMD_TCP_MONITOR), (long) OppoWifiAssistantStateTraker.this.mTcpInterval);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    String configKey;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    int connectedstate = msg.arg2;
                    WifiConfiguration config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    oppoWifiAssistantStateTraker.mLastConfigkey = configKey;
                    OppoWifiAssistantStateTraker.this.logD("cctst= " + connectedstate);
                    if (connectedstate == 0) {
                        if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                            OppoWifiAssistantStateTraker.this.dismissDialog(0);
                        }
                        OppoWifiAssistantStateTraker.this.updateRecordDisableState(OppoWifiAssistantStateTraker.this.mLastConfigkey);
                        OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mDisconnectState);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mNetworkMonitorState);
                    break;
                case OppoWifiAssistantStateTraker.CMD_TCP_MONITOR /*200711*/:
                    if (OppoWifiAssistantStateTraker.this.needToDetectTcpStatus()) {
                        boolean -wrap1 = OppoWifiAssistantStateTraker.this.detectTcpStatus();
                    }
                    OppoWifiAssistantStateTraker.this.sendMessageDelayed(OppoWifiAssistantStateTraker.this.obtainMessage(OppoWifiAssistantStateTraker.CMD_TCP_MONITOR), (long) OppoWifiAssistantStateTraker.this.mTcpInterval);
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_ON /*200712*/:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = true;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class DataStateObserver extends ContentObserver {
        public DataStateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (OppoWifiAssistantStateTraker.this.mTelephonyManager != null) {
                OppoWifiAssistantStateTraker.this.mDataState = OppoWifiAssistantStateTraker.this.mTelephonyManager.getDataEnabled();
                if (!OppoWifiAssistantStateTraker.this.mDataState) {
                    long disableData = System.currentTimeMillis() - OppoWifiAssistantStateTraker.this.mAutoConnDataTime;
                    if (OppoWifiAssistantStateTraker.this.mChangeNetwork && OppoWifiAssistantStateTraker.this.mToData) {
                        if (OppoWifiAssistantStateTraker.this.mAutoConnDataTime > 0 && disableData > 0 && disableData < 180000) {
                            OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_MANUAL_LIMIT, OppoWifiAssistantStateTraker.TYPE_DIABLE_DATA);
                            OppoWifiAssistantStateTraker.this.mAutoConnDataTime = 0;
                        }
                        OppoWifiAssistantStateTraker.this.logD("stc dwc2");
                        OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_MANUAL_OPERATE, OppoWifiAssistantStateTraker.TYPE_DIABLE_DATA);
                    }
                }
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            String str = null;
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            WifiConfiguration config;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker;
            String configKey;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2;
            switch (msg.what) {
                case 131126:
                case 151553:
                    OppoWifiAssistantStateTraker.this.mConnectId = Integer.valueOf(msg.arg1).intValue();
                    OppoWifiAssistantStateTraker.this.mIsNewConfig = Integer.valueOf(msg.arg2).intValue();
                    config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mConnectId);
                    oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    oppoWifiAssistantStateTraker.mConnectKey = configKey;
                    oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    oppoWifiAssistantStateTraker2.mConnectSSID = str;
                    OppoWifiAssistantStateTraker.this.logD("mConnectId= " + OppoWifiAssistantStateTraker.this.mConnectId + ", minc= " + OppoWifiAssistantStateTraker.this.mIsNewConfig);
                    OppoWifiAssistantStateTraker.this.addOrUpdateRecord(OppoWifiAssistantStateTraker.this.mConnectKey);
                    break;
                case 131211:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.WRONG_KEY_EVENT /*147648*/:
                    WifiConfiguration conFail = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mConnectingId);
                    oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    if (conFail != null) {
                        str = conFail.configKey(false);
                    }
                    oppoWifiAssistantStateTraker2.mConnectingkey = str;
                    OppoWifiAssistantStateTraker.this.updateRecordConnectFail(OppoWifiAssistantStateTraker.this.mConnectingkey);
                    if (OppoWifiAssistantStateTraker.this.getIsOppoManuConnect()) {
                        OppoWifiAssistantStateTraker.this.mConnectId = -1;
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        str = config.configKey(false);
                    }
                    oppoWifiAssistantStateTraker2.mLastConfigkey = str;
                    OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mCompletedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    OppoWifiAssistantStateTraker.this.transitionSupplicantState(msg.obj);
                    break;
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    OppoWifiAssistantStateTraker.this.mLastNetId = msg.arg1;
                    OppoWifiAssistantStateTraker.this.mLastBssid = (String) msg.obj;
                    config = OppoWifiAssistantStateTraker.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiAssistantStateTraker.this.mLastNetId);
                    oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    oppoWifiAssistantStateTraker.mLastConfigkey = configKey;
                    if (msg.arg2 != 2) {
                        if (msg.arg2 == 0) {
                            if (OppoWifiAssistantStateTraker.this.mUnavailableKey != null && OppoWifiAssistantStateTraker.this.mUnavailableKey.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey)) {
                                OppoWifiAssistantStateTraker.this.dismissDialog(0);
                            }
                            OppoWifiAssistantStateTraker.this.updateRecordDisableState(OppoWifiAssistantStateTraker.this.mLastConfigkey);
                            OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mDisconnectState);
                            break;
                        }
                    }
                    oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    oppoWifiAssistantStateTraker2.mConnectSSID = str;
                    OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mConnectedState);
                    break;
                    break;
                case OppoWifiAssistantStateTraker.EVENT_WIFI_STATE_CHANGE /*200706*/:
                    OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mInitState);
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_ON /*200712*/:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = true;
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_OFF /*200713*/:
                    OppoWifiAssistantStateTraker.this.mIsScreenOn = false;
                    break;
            }
            return true;
        }
    }

    class DisConnectedState extends State {
        DisConnectedState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.logD("mLastNetId = " + OppoWifiAssistantStateTraker.this.mLastNetId + ", mConnectedId = " + OppoWifiAssistantStateTraker.this.mConnectedId + ", mLastConfigkey = " + OppoWifiAssistantStateTraker.this.mLastConfigkey);
            OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
            if (OppoWifiAssistantStateTraker.this.mLastNetId == OppoWifiAssistantStateTraker.this.mConnectedId && OppoWifiAssistantStateTraker.this.mLastNetId != -1 && OppoWifiAssistantStateTraker.this.mConnectingId == OppoWifiAssistantStateTraker.this.mConnectedId) {
                OppoWifiAssistantStateTraker.this.updateRecordUseTime(OppoWifiAssistantStateTraker.this.mLastConfigkey);
            }
            OppoWifiAssistantStateTraker.this.mLastNetId = -1;
            OppoWifiAssistantStateTraker.this.mLastBssid = " ";
            OppoWifiAssistantStateTraker.this.mUnavailableKey = " ";
            OppoWifiAssistantStateTraker.this.mAutoSwithToData = false;
            OppoWifiAssistantStateTraker.this.mInterResult = false;
            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
            OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = false;
            OppoWifiAssistantStateTraker.this.mLastPkgInfo = null;
            OppoWifiAssistantStateTraker.this.mGotInternetResult = 0;
            OppoWifiAssistantStateTraker.this.mLastDetectInter = 0;
            OppoWifiAssistantStateTraker.this.mTrigScanCount = 0;
            OppoWifiAssistantStateTraker.this.mDTxPkts = 0;
            OppoWifiAssistantStateTraker.this.mdRxPkts = 0;
            OppoWifiAssistantStateTraker.this.mIsSoftAP = false;
            OppoWifiAssistantStateTraker.this.mCaptivePortal = false;
            OppoWifiAssistantStateTraker.this.mResponseGotFromGateway = false;
            OppoWifiAssistantStateTraker.this.mWifiStateMachineConnected = false;
            OppoWifiAssistantStateTraker.this.resetAutoSwitchDataDetect();
            if (OppoWifiAssistantStateTraker.this.mAutoSwitch && OppoWifiAssistantStateTraker.this.mFeatureState && OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                OppoWifiAssistantStateTraker.this.sendNetworkStateBroadCast(OppoWifiAssistantStateTraker.this.mLastConfigkey, true);
                OppoWifiAssistantStateTraker.this.sendWifiToDataBroadcast(false, 79);
            }
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_ON /*200712*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    class HandshakeState extends State {
        HandshakeState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_ON /*200712*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    class InitState extends State {
        InitState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mWifiState = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncGetWifiState();
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_WIFI_STATE_CHANGE /*200706*/:
                    OppoWifiAssistantStateTraker.this.logD("it is Initstate, do not handle!");
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SCREEN_ON /*200712*/:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private final class InterThread extends Handler {
        private InterThread() {
        }

        public void handleMessage(Message msg) {
        }
    }

    private final class NetHandler extends Handler {
        public NetHandler(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_ADD_UPDATE_NETWORK /*200714*/:
                    WifiInfo updateWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo(OppoWifiAssistantStateTraker.WIFI_PACKEG_NAME);
                    Intent netMintent = msg.obj;
                    if (!(netMintent == null || (OppoWifiAssistantStateTraker.this.mIsSoftAP && OppoWifiAssistantStateTraker.this.isThirdAppOperate()))) {
                        String netStateSsid = netMintent.getStringExtra("ssid");
                        OppoWifiAssistantStateTraker.this.logD("nss: " + netStateSsid + ",info: " + updateWifiInfo);
                        if (!(updateWifiInfo == null || netStateSsid == null || (updateWifiInfo.getSSID().equals(netStateSsid) ^ 1) != 0)) {
                            boolean netValid = netMintent.getBooleanExtra(OppoWifiAssistantStateTraker.EXTRA_NETWORK_STATE, false);
                            OppoWifiAssistantStateTraker.this.logD("nst: " + netValid);
                            WifiConfiguration netConf = OppoWifiAssistantStateTraker.this.getWifiConfig(netStateSsid, updateWifiInfo.getBSSID());
                            if (netConf == null || netConf.networkId != OppoWifiAssistantStateTraker.this.mConnectedId) {
                                if (OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mConnectedState || OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mVerifyInternetState) {
                                    if (netConf != null) {
                                        OppoWifiAssistantRecord expInfo = new OppoWifiAssistantRecord();
                                        expInfo.mNetid = netConf.networkId;
                                        expInfo.mConfigkey = netConf.configKey();
                                        OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_EXP, expInfo, null, OppoWifiAssistantStateTraker.this.mConnectedId);
                                        break;
                                    }
                                    OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_EXP);
                                    break;
                                }
                            }
                            if (OppoWifiAssistantStateTraker.this.mGotInternetResult < 4 || OppoWifiAssistantStateTraker.this.mCaptivePortal || netValid != OppoWifiAssistantStateTraker.this.mInterResult) {
                                OppoWifiAssistantStateTraker.this.updateRecordInternetStateAndTime(netConf.configKey(false), netValid, true);
                            }
                            if (!(!netValid || netValid == OppoWifiAssistantStateTraker.this.mInterResult || OppoWifiAssistantStateTraker.this.mWifiStateMachine == null)) {
                                OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                            }
                            OppoWifiAssistantStateTraker.this.mInterResult = netValid;
                            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker;
                            if (!netValid) {
                                oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                                oppoWifiAssistantStateTraker.mGotInternetResult = oppoWifiAssistantStateTraker.mGotInternetResult | 8;
                                oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                                oppoWifiAssistantStateTraker.mGotInternetResult = oppoWifiAssistantStateTraker.mGotInternetResult & -5;
                                break;
                            }
                            oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker.mGotInternetResult = oppoWifiAssistantStateTraker.mGotInternetResult | 4;
                            oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                            oppoWifiAssistantStateTraker.mGotInternetResult = oppoWifiAssistantStateTraker.mGotInternetResult & -9;
                            break;
                        }
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_CONNECT_NETWORK /*200715*/:
                    Intent conIntent = msg.obj;
                    WifiInfo curWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo(OppoWifiAssistantStateTraker.WIFI_PACKEG_NAME);
                    String curSsid = conIntent.getStringExtra("ssid");
                    if (curWifiInfo != null && curSsid != null && (curWifiInfo.getSSID().equals(curSsid) ^ 1) == 0) {
                        OppoWifiAssistantStateTraker.this.logD("cnsid:" + curSsid + ",ifsid:" + curWifiInfo.getSSID() + ",cisid:" + OppoWifiAssistantStateTraker.this.mConnectSSID);
                        if (curSsid.equals(OppoWifiAssistantStateTraker.this.mConnectSSID)) {
                            LinkProperties curLink = (LinkProperties) conIntent.getExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_LINK);
                            OppoWifiAssistantStateTraker.this.mCurNetwork = (Network) conIntent.getExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_NETWORK);
                            OppoWifiAssistantStateTraker.this.mIsSoftAP = OppoWifiAssistantStateTraker.this.isSoftAp(curLink);
                            break;
                        }
                    }
                    return;
                    break;
                case OppoWifiAssistantStateTraker.EVENT_INTERNET_CHANGE /*200722*/:
                    boolean interResult = msg.arg1 == 1;
                    String interConf = msg.obj;
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
                    if (OppoWifiAssistantStateTraker.this.mLastPkgInfo != null) {
                        OppoWifiAssistantStateTraker.this.updateRecordLinkQuality(OppoWifiAssistantStateTraker.this.mLastPkgInfo);
                    }
                    if (!(!interResult || (OppoWifiAssistantStateTraker.this.mChangeNetwork ^ 1) == 0 || OppoWifiAssistantStateTraker.this.mWifiStateMachine == null)) {
                        OppoWifiAssistantStateTraker.this.mWifiStateMachine.setNetworkStatus(true);
                        break;
                    }
                case OppoWifiAssistantStateTraker.EVENT_CAPTIVE_PORTAL /*200723*/:
                    String captiveConf = msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("nc=" + (Integer.valueOf(msg.arg1).intValue() == 1) + ",cc=" + captiveConf);
                    OppoWifiAssistantStateTraker.this.updateRecordCaptiveState(captiveConf, true, true);
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SCAN_TIMEOUT /*200724*/:
                    OppoWifiAssistantStateTraker.this.mTriggerScan = false;
                    break;
            }
        }
    }

    class NetworkMonitorState extends State {
        NetworkMonitorState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName() + ",mLastConfigkey=" + OppoWifiAssistantStateTraker.this.mLastConfigkey);
            OppoWifiAssistantStateTraker.this.mLastTrigDataTime = System.currentTimeMillis();
            OppoWifiAssistantStateTraker.this.mLastTxBad = 0;
            OppoWifiAssistantStateTraker.this.mLastTxGood = 0;
            OppoWifiAssistantStateTraker.this.mLastTxPkts = TrafficStats.getTcpTxPackets(OppoWifiAssistantStateTraker.mInterfaceName);
            OppoWifiAssistantStateTraker.this.mLastRxPkts = TrafficStats.getTcpRxPackets(OppoWifiAssistantStateTraker.mInterfaceName);
            OppoWifiAssistantStateTraker.this.mRxPktsLowCount = 0;
            OppoWifiAssistantStateTraker.this.mDTxPkts = 0;
            OppoWifiAssistantStateTraker.this.mdRxPkts = 0;
            OppoWifiAssistantStateTraker.this.mLinkInterval = OppoWifiAssistantStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", Integer.valueOf(5000)).intValue();
            OppoWifiAssistantStateTraker.this.mTrafficInteval = OppoWifiAssistantStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL", Integer.valueOf(OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL)).intValue();
            OppoWifiAssistantStateTraker.this.mTcpInterval = 5000;
            OppoWifiAssistantStateTraker.this.mWlanInvalidThreshold = OppoWifiAssistantStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", Integer.valueOf(40)).intValue();
            OppoWifiAssistantStateTraker.this.mRoamdetectCount = 0;
            OppoWifiAssistantStateTraker.this.mTriggerData = true;
            OppoWifiAssistantStateTraker.this.mTriggerInter = false;
            OppoWifiAssistantStateTraker.this.mChangeNetwork = false;
            OppoWifiAssistantStateTraker.this.mClickDialogSwitch = false;
            OppoWifiAssistantStateTraker.this.mDetectInterCount = 0;
            OppoWifiAssistantStateTraker.this.mLastInternetResult = OppoWifiAssistantStateTraker.this.mInterResult;
            OppoWifiAssistantStateTraker.this.mLinkDetectTimes = 0;
            OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
            OppoWifiAssistantStateTraker.this.mInterChangeToInvalid = false;
            OppoWifiAssistantStateTraker.this.mLastDetectInter = 0;
            OppoWifiAssistantStateTraker.this.mInternetStandoffTime = 0;
            OppoWifiAssistantStateTraker.this.mTrigScanCount = 0;
            OppoWifiAssistantStateTraker.this.mNetQulityGoodCount = 0;
            OppoWifiAssistantStateTraker.this.mOldTcpStatus = 0;
            for (int lossInt = 0; lossInt < OppoWifiAssistantStateTraker.this.mLossArray.length; lossInt++) {
                OppoWifiAssistantStateTraker.this.mLossArray[lossInt] = OppoWifiAssistantStateTraker.this.mLossInit;
            }
            OppoWifiAssistantStateTraker.this.mIndex = 0;
            OppoWifiAssistantStateTraker.this.mCurrentLoss = new VolumeWeightedEMA(0.5d);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
            oppoWifiAssistantStateTraker.sendMessage(oppoWifiAssistantStateTraker2.obtainMessage(OppoWifiAssistantStateTraker.CMD_RSSI_FETCH, oppoWifiAssistantStateTraker3.mRssiFetchToken = oppoWifiAssistantStateTraker3.mRssiFetchToken + 1, 0));
            OppoWifiAssistantStateTraker.this.removeMessages(OppoWifiAssistantStateTraker.CMD_TRAFFIC_MONITOR);
            OppoWifiAssistantStateTraker.this.sendMessageDelayed(OppoWifiAssistantStateTraker.this.obtainMessage(OppoWifiAssistantStateTraker.CMD_TRAFFIC_MONITOR), 10000);
            OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_MONITOR_WLAN, (OppoWifiAssistantRecord) OppoWifiAssistantStateTraker.this.mWifiNetworkRecord.get(OppoWifiAssistantStateTraker.this.mLastConfigkey), null, -127);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151573:
                    RssiPacketCountInfo info = msg.obj;
                    if (info != null) {
                        if (!OppoWifiAssistantStateTraker.this.mWifiStateMachineConnected) {
                            OppoWifiAssistantStateTraker.this.logD("noconnect");
                            break;
                        }
                        OppoWifiAssistantStateTraker.this.updateRecordLinkQuality(info);
                        OppoWifiAssistantStateTraker.this.mLastPkgInfo = info;
                        break;
                    }
                    break;
                case 151574:
                    OppoWifiAssistantStateTraker.this.logD("RSSI_FETCH_FAILED");
                    break;
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    break;
                case OppoWifiAssistantStateTraker.CMD_RSSI_FETCH /*200708*/:
                    if (msg.arg1 == OppoWifiAssistantStateTraker.this.mRssiFetchToken) {
                        OppoWifiAssistantStateTraker.this.mWsmChannel.sendMessage(151572, 1);
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = OppoWifiAssistantStateTraker.this;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.sendMessageDelayed(oppoWifiAssistantStateTraker2.obtainMessage(OppoWifiAssistantStateTraker.CMD_RSSI_FETCH, oppoWifiAssistantStateTraker3.mRssiFetchToken = oppoWifiAssistantStateTraker3.mRssiFetchToken + 1, 0), (long) OppoWifiAssistantStateTraker.this.mLinkInterval);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.CMD_TRAFFIC_MONITOR /*200709*/:
                    OppoWifiAssistantStateTraker.this.detectTraffic();
                    OppoWifiAssistantStateTraker.this.sendMessageDelayed(OppoWifiAssistantStateTraker.this.obtainMessage(OppoWifiAssistantStateTraker.CMD_TRAFFIC_MONITOR), (long) OppoWifiAssistantStateTraker.this.mTrafficInteval);
                    break;
                case OppoWifiAssistantStateTraker.CMD_INTERNET_MONITOR /*200710*/:
                    OppoWifiAssistantStateTraker.this.detectInternet();
                    if (OppoWifiAssistantStateTraker.this.mToData) {
                        if (!(OppoWifiAssistantStateTraker.this.mAutoSwitch && (OppoWifiAssistantStateTraker.this.mFeatureState ^ 1) == 0 && (OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) == 0)) {
                            OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                        }
                        OppoWifiAssistantStateTraker.this.logD("miit = " + OppoWifiAssistantStateTraker.this.mInterInteval);
                        if (!OppoWifiAssistantStateTraker.this.mScreenOn) {
                            long wakeupTime = System.currentTimeMillis() + ((long) OppoWifiAssistantStateTraker.this.mInterInteval);
                            OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                            OppoWifiAssistantStateTraker.this.mAlarmManager.set(1, wakeupTime, OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                            break;
                        }
                        OppoWifiAssistantStateTraker.this.removeMessages(OppoWifiAssistantStateTraker.CMD_INTERNET_MONITOR);
                        OppoWifiAssistantStateTraker.this.sendMessageDelayed(OppoWifiAssistantStateTraker.this.obtainMessage(OppoWifiAssistantStateTraker.CMD_INTERNET_MONITOR), (long) OppoWifiAssistantStateTraker.this.mInterInteval);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_SHOW_ALERT /*200719*/:
                    String showConfig = msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("shcon:" + showConfig);
                    if (!(showConfig == null || OppoWifiAssistantStateTraker.this.hasCheckNoRemind())) {
                        OppoWifiAssistantStateTraker.this.showDialogFordataNewtype((OppoWifiAssistantRecord) OppoWifiAssistantStateTraker.this.mWifiNetworkRecord.get(showConfig));
                        break;
                    }
                case OppoWifiAssistantStateTraker.EVENT_DISMISS_ALERT /*200720*/:
                    String dismissConfig = msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("dicon:" + dismissConfig);
                    if (dismissConfig != null) {
                        OppoWifiAssistantStateTraker.this.dismissDialog(1);
                        break;
                    }
                    break;
                case OppoWifiAssistantStateTraker.EVENT_DETECT_ALTERNATIVE /*200721*/:
                    String detectConfig = msg.obj;
                    OppoWifiAssistantStateTraker.this.logD("decon:" + detectConfig);
                    OppoWifiAssistantStateTraker.this.detectNetworkAvailable((OppoWifiAssistantRecord) OppoWifiAssistantStateTraker.this.mWifiNetworkRecord.get(detectConfig), 10, -127, true);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class VerifyInternetState extends State {
        VerifyInternetState() {
        }

        public void enter() {
            OppoWifiAssistantStateTraker.this.logD(getName());
            OppoWifiAssistantStateTraker.this.mConnectedId = OppoWifiAssistantStateTraker.this.mLastNetId;
            OppoWifiAssistantStateTraker.this.logD("VerifyInternetState mConnectedId= " + OppoWifiAssistantStateTraker.this.mConnectedId);
        }

        public boolean processMessage(Message msg) {
            OppoWifiAssistantStateTraker.this.logD(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case OppoWifiAssistantStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    if (OppoWifiAssistantStateTraker.this.getIsOppoManuConnect() || OppoWifiAssistantStateTraker.this.mInterResult) {
                        OppoWifiAssistantStateTraker.this.transitionTo(OppoWifiAssistantStateTraker.this.mNetworkMonitorState);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private class VolumeWeightedEMA {
        private double mAlpha;
        private double mProduct = 0.0d;
        private double mValue = 0.0d;
        private double mVolume = 0.0d;

        public VolumeWeightedEMA(double coefficient) {
            this.mAlpha = coefficient;
        }

        public void update(double newValue, int newVolume) {
            if (newVolume > 0) {
                this.mProduct = (this.mAlpha * (newValue * ((double) newVolume))) + ((1.0d - this.mAlpha) * this.mProduct);
                this.mVolume = (this.mAlpha * ((double) newVolume)) + ((1.0d - this.mAlpha) * this.mVolume);
                this.mValue = this.mProduct / this.mVolume;
            }
        }
    }

    /* renamed from: -getandroid-net-wifi-SupplicantStateSwitchesValues */
    private static /* synthetic */ int[] m98-getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (f185-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return f185-android-net-wifi-SupplicantStateSwitchesValues;
        }
        int[] iArr = new int[SupplicantState.values().length];
        try {
            iArr[SupplicantState.ASSOCIATED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SupplicantState.ASSOCIATING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SupplicantState.COMPLETED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SupplicantState.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SupplicantState.DORMANT.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SupplicantState.INACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SupplicantState.INTERFACE_DISABLED.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SupplicantState.INVALID.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SupplicantState.SCANNING.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SupplicantState.UNINITIALIZED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        f185-android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    public OppoWifiAssistantStateTraker(Context c, WifiStateMachine wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst, WifiRomUpdateHelper wruh, Handler t) {
        super(TAG, t.getLooper());
        this.mContext = c;
        this.mWifiStateMachine = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mWifiRomUpdateHelper = wruh;
        this.mHandler = new NetHandler(t.getLooper());
        this.mWriter = new DelayedDiskWrite();
        this.mWah = new OppoWifiAssistantUpdateHelper(this.mContext, this);
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
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mDataStateObserver);
        this.mDataState = Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
        initWifiAssistantData();
        this.mDataAutoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_NETWORK, 1) == 1;
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_AUTO_CHANGE_NETWORK), true, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                if (Global.getInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.WIFI_AUTO_CHANGE_NETWORK, 1) != 1) {
                    z = false;
                }
                oppoWifiAssistantStateTraker.mDataAutoSwitch = z;
                OppoWifiAssistantStateTraker.this.setDataAutoSwitch(OppoWifiAssistantStateTraker.this.mDataAutoSwitch);
                OppoWifiAssistantStateTraker.this.logD(" mdas= " + OppoWifiAssistantStateTraker.this.mDataAutoSwitch);
            }
        });
        this.mFeatureState = getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue();
        addState(this.mDefaultState);
        addState(this.mInitState, this.mDefaultState);
        addState(this.mDisconnectState, this.mDefaultState);
        addState(this.mHandshakeState, this.mDefaultState);
        addState(this.mCompletedState, this.mDefaultState);
        addState(this.mVerifyInternetState, this.mCompletedState);
        addState(this.mConnectedState, this.mCompletedState);
        addState(this.mNetworkMonitorState, this.mConnectedState);
        setInitialState(this.mInitState);
        start();
        this.mOppoTcpInfoMonitor = new OppoTcpInfoMonitor(this.mContext);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDebug = true;
        } else {
            sDebug = false;
        }
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.enableVerboseLogging(verbose);
        }
        if (this.mOppoTcpInfoMonitor != null) {
            this.mOppoTcpInfoMonitor.enableVerboseLogging(verbose);
        }
    }

    private void setupNetworkReceiver() {
        IntentFilter netWorkFilter = new IntentFilter();
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_CONNECT);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_STATE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_AVAILABLE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_NOT_AVAILABLE);
        netWorkFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        netWorkFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        netWorkFilter.addAction("android.net.wifi.STATE_CHANGE");
        netWorkFilter.addAction("android.intent.action.SCREEN_ON");
        netWorkFilter.addAction("android.intent.action.SCREEN_OFF");
        netWorkFilter.addAction(ACTION_DETECT_INTERNET);
        netWorkFilter.addAction(WIFI_TO_DATA);
        netWorkFilter.addAction("android.net.conn.NETWORK_CONDITIONS_MEASURED");
        netWorkFilter.addAction(DATA_SCORE_CHANGE);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoWifiAssistantStateTraker.this.mFeatureState && (OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) == 0) {
                    OppoWifiAssistantStateTraker.this.logD("AssistReceiver event:" + action);
                    if (action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_CONNECT)) {
                        OppoWifiAssistantStateTraker.this.mHandler.sendMessage(OppoWifiAssistantStateTraker.this.mHandler.obtainMessage(OppoWifiAssistantStateTraker.EVENT_CONNECT_NETWORK, intent));
                    } else if (action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_STATE)) {
                        OppoWifiAssistantStateTraker.this.mHandler.sendMessage(OppoWifiAssistantStateTraker.this.mHandler.obtainMessage(OppoWifiAssistantStateTraker.EVENT_ADD_UPDATE_NETWORK, intent));
                    } else if (!(action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_AVAILABLE) || action.equals(OppoWifiAssistantStateTraker.ACTION_WIFI_NETWORK_NOT_AVAILABLE))) {
                        if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (networkInfo != null && networkInfo.getDetailedState() == DetailedState.CONNECTED) {
                                OppoWifiAssistantStateTraker.this.mWifiStateMachineConnected = true;
                            }
                        } else if (action.equals(OppoWifiAssistantStateTraker.ACTION_DETECT_INTERNET)) {
                            OppoWifiAssistantStateTraker.this.sendMessage(OppoWifiAssistantStateTraker.CMD_INTERNET_MONITOR);
                        } else if (action.equals("android.intent.action.SCREEN_ON")) {
                            if (OppoWifiAssistantStateTraker.this.mToData) {
                                OppoWifiAssistantStateTraker.this.mInterInteval = OppoWifiAssistantStateTraker.INTERNET_TO_DATA_INTERVAL;
                            } else {
                                OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                            }
                            OppoWifiAssistantStateTraker.this.mScreenOn = true;
                            if (OppoWifiAssistantStateTraker.this.getCurrentState() == OppoWifiAssistantStateTraker.this.mNetworkMonitorState && OppoWifiAssistantStateTraker.this.mLastDetectInter != 0 && System.currentTimeMillis() - OppoWifiAssistantStateTraker.this.mLastDetectInter > ((long) OppoWifiAssistantStateTraker.this.mInterInteval)) {
                                OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                                OppoWifiAssistantStateTraker.this.mAlarmManager.set(1, 5000, OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                            }
                        } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                            OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                            OppoWifiAssistantStateTraker.this.mScreenOn = false;
                        } else if (action.equals(OppoWifiAssistantStateTraker.WIFI_TO_DATA)) {
                            OppoWifiAssistantStateTraker.this.mToData = intent.getBooleanExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_TO_DATA, false);
                            if (OppoWifiAssistantStateTraker.this.mToData != OppoWifiAssistantStateTraker.this.mLastToData) {
                                if (OppoWifiAssistantStateTraker.this.mToData) {
                                    OppoWifiAssistantStateTraker.this.mInterInteval = OppoWifiAssistantStateTraker.INTERNET_TO_DATA_INTERVAL;
                                    OppoWifiAssistantStateTraker.this.mTcpInterval = 5000;
                                    long delayTime = System.currentTimeMillis() + ((long) OppoWifiAssistantStateTraker.this.mInterInteval);
                                    OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                                    OppoWifiAssistantStateTraker.this.mAlarmManager.set(1, delayTime, OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                                    OppoWifiAssistantStateTraker.this.logD("mcn=" + OppoWifiAssistantStateTraker.this.mChangeNetwork + ",mcds=" + OppoWifiAssistantStateTraker.this.mClickDialogSwitch + ",micti=" + OppoWifiAssistantStateTraker.this.mInterChangeToInvalid + ",mir=" + OppoWifiAssistantStateTraker.this.mInterResult);
                                    if (OppoWifiAssistantStateTraker.this.mChangeNetwork && (OppoWifiAssistantStateTraker.this.mClickDialogSwitch ^ 1) != 0 && ((!OppoWifiAssistantStateTraker.this.mInterChangeToInvalid && OppoWifiAssistantStateTraker.this.mInterResult) || OppoWifiAssistantStateTraker.this.mInterChangeToInvalid)) {
                                        OppoWifiAssistantStateTraker.this.sendMessage(OppoWifiAssistantStateTraker.EVENT_SHOW_ALERT, OppoWifiAssistantStateTraker.this.mLastConfigkey);
                                    }
                                    if (OppoWifiAssistantStateTraker.this.mClickDialogSwitch) {
                                        OppoWifiAssistantStateTraker.this.mAutoSwithToData = false;
                                    } else {
                                        OppoWifiAssistantStateTraker.this.mAutoSwithToData = true;
                                    }
                                    OppoWifiAssistantStateTraker.this.mClickDialogSwitch = false;
                                    OppoWifiAssistantStateTraker.this.detectSwitchDataFrequence();
                                    if (OppoWifiAssistantStateTraker.this.mChangeNetwork) {
                                        OppoWifiAssistantStateTraker.this.mAutoConnDataTime = System.currentTimeMillis();
                                        OppoWifiAssistantStateTraker.this.mAutoConnWlanTime = 0;
                                        OppoWifiAssistantStateTraker.this.mAutoDataToWlanTime = 0;
                                        OppoWifiAssistantRecord curTodataRecord = (OppoWifiAssistantRecord) OppoWifiAssistantStateTraker.this.mWifiNetworkRecord.get(OppoWifiAssistantStateTraker.this.mLastConfigkey);
                                        int validRecordCount = OppoWifiAssistantStateTraker.this.mSortNetworkRecord.size();
                                        OppoWifiAssistantStateTraker.this.logD("stc wda");
                                        OppoWifiAssistantStateTraker.this.setAssistantStatistics(OppoWifiAssistantStateTraker.STATISTIC_AUTO_CONN, OppoWifiAssistantStateTraker.TYPE_WLAN_TO_DATA, curTodataRecord, null, validRecordCount);
                                    }
                                } else {
                                    if (!OppoWifiAssistantStateTraker.this.mInterResult) {
                                        if (OppoWifiAssistantStateTraker.this.mDataAutoSwitch) {
                                            OppoWifiAssistantStateTraker.this.sendLowQualityBroadcast(10, true);
                                        }
                                        if (OppoWifiAssistantStateTraker.this.mWlanInvalidThreshold == 70) {
                                            OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                                        }
                                    } else if (OppoWifiAssistantStateTraker.this.mWifiStateMachine != null) {
                                        OppoWifiAssistantStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                                    }
                                    OppoWifiAssistantStateTraker.this.mTriggerData = true;
                                    OppoWifiAssistantStateTraker.this.mChangeNetwork = false;
                                    OppoWifiAssistantStateTraker.this.mInterInteval = WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS;
                                    OppoWifiAssistantStateTraker.this.sendMessage(OppoWifiAssistantStateTraker.EVENT_DISMISS_ALERT, OppoWifiAssistantStateTraker.this.mLastConfigkey);
                                }
                                OppoWifiAssistantStateTraker.this.mLastToData = OppoWifiAssistantStateTraker.this.mToData;
                            }
                        } else if (action.equals("android.net.conn.NETWORK_CONDITIONS_MEASURED")) {
                            if (intent.getIntExtra("extra_connectivity_type", -1) == 1) {
                                if (intent.getBooleanExtra("extra_is_captive_portal", false)) {
                                    OppoWifiAssistantStateTraker.this.logD("Received ACTION_NETWORK_CONDITIONS_MEASURED, wlan is captive portal.");
                                    if (!OppoWifiAssistantStateTraker.this.mIsSoftAP || !OppoWifiAssistantStateTraker.this.isThirdAppOperate()) {
                                        if (OppoWifiAssistantStateTraker.this.mCaptivePortal) {
                                            OppoWifiAssistantStateTraker.this.logD("is cp.");
                                            return;
                                        }
                                        WifiInfo curWifiInfo = OppoWifiAssistantStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo(OppoWifiAssistantStateTraker.WIFI_PACKEG_NAME);
                                        String netStateSsid = intent.getStringExtra("extra_ssid");
                                        OppoWifiAssistantStateTraker.this.logD("cpnss: " + netStateSsid + ", info: " + curWifiInfo);
                                        if (curWifiInfo != null && netStateSsid != null && (curWifiInfo.getSSID().equals(netStateSsid) ^ 1) == 0) {
                                            WifiConfiguration netConf = OppoWifiAssistantStateTraker.this.getWifiConfig(netStateSsid, curWifiInfo.getBSSID());
                                            if (netConf != null && netConf.networkId == OppoWifiAssistantStateTraker.this.mConnectedId) {
                                                OppoWifiAssistantStateTraker.this.mCaptivePortal = true;
                                                if ((OppoWifiAssistantStateTraker.this.mGotInternetResult & 4) != 4) {
                                                    OppoWifiAssistantStateTraker.this.sendVerifyBroadcast(netConf.configKey(false));
                                                    if (OppoWifiAssistantStateTraker.this.mAutoSwitch && OppoWifiAssistantStateTraker.this.mFeatureState && OppoWifiAssistantStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                                                        OppoWifiAssistantStateTraker.this.sendNetworkStateBroadCast(netConf.configKey(false), false);
                                                    }
                                                    int newConfig = 0;
                                                    if (OppoWifiAssistantStateTraker.this.getIsOppoManuConnect() && OppoWifiAssistantStateTraker.this.mConnectId == netConf.networkId && OppoWifiAssistantStateTraker.this.mConnectId != -1) {
                                                        newConfig = OppoWifiAssistantStateTraker.this.mIsNewConfig;
                                                    }
                                                    OppoWifiAssistantStateTraker.this.mHandler.sendMessage(OppoWifiAssistantStateTraker.this.mHandler.obtainMessage(OppoWifiAssistantStateTraker.EVENT_CAPTIVE_PORTAL, newConfig, 0, netConf.configKey(false)));
                                                } else {
                                                    return;
                                                }
                                            }
                                        }
                                        return;
                                    }
                                    return;
                                }
                            }
                        } else if (action.equals(OppoWifiAssistantStateTraker.DATA_SCORE_CHANGE)) {
                            int dataScore = intent.getIntExtra(OppoWifiAssistantStateTraker.EXTRA_DATA_CORE, 10);
                            OppoWifiAssistantStateTraker.this.logD("ds=" + dataScore);
                            OppoWifiAssistantStateTraker.this.mDataScore = dataScore;
                        }
                    }
                    return;
                }
                OppoWifiAssistantStateTraker.this.logD("mfs dis");
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, netWorkFilter);
    }

    private PendingIntent getPrivateBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
    }

    private void initWifiAssistantData() {
        File oldfile = new File(WIFI_AVAILABLE_FILE);
        if (oldfile.exists()) {
            logD("exists remove");
            oldfile.delete();
        }
    }

    private void transitionSupplicantState(StateChangeResult stateChangeResult) {
        String str = null;
        SupplicantState supState = stateChangeResult.state;
        this.mConnectingId = stateChangeResult.networkId;
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mConnectingId);
        if (config != null) {
            str = config.configKey(false);
        }
        this.mConnectingkey = str;
        logD("Supplicant state: " + supState.toString() + ",mConnectingId= " + this.mConnectingId);
        int i = m98-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()];
    }

    public void updateWifiState(int state) {
        if (state == -1) {
            transitionTo(this.mInitState);
            logD("updateWifiState return");
            return;
        }
        if (state == 3) {
            transitionTo(this.mDisconnectState);
        } else if (state == 1) {
            saveWifiNetworkRecord();
            sendMessage(EVENT_WIFI_STATE_CHANGE);
            this.mInitAutoConnect = true;
            this.mIsSoftAP = false;
            this.mCurNetwork = null;
            if (this.mWifiState != state) {
                long disableWlanTime = System.currentTimeMillis();
                long disableForWlan = disableWlanTime - this.mAutoConnWlanTime;
                if (this.mAutoConnWlanTime > 0 && disableForWlan > 0 && disableForWlan < 180000) {
                    logD("stc wwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_WLAN);
                    this.mAutoConnWlanTime = 0;
                }
                long disableForData = disableWlanTime - this.mAutoDataToWlanTime;
                if (!this.mToData && this.mAutoDataToWlanTime > 0 && disableForData > 0 && disableForData < 180000) {
                    logD("stc dwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_DATA);
                    this.mAutoDataToWlanTime = 0;
                }
                if (!this.mToData) {
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
        logD("urncr mGotInternetResult= " + this.mGotInternetResult + ",valid= " + valid + ",mInterResult= " + this.mInterResult);
        if (this.mWifiConfigManager != null && (!this.mIsSoftAP || !isThirdAppOperate())) {
            WifiConfiguration updateConfig = this.mWifiConfigManager.getConfiguredNetwork(netid);
            if (updateConfig != null) {
                if (this.mGotInternetResult < 4 || valid != this.mInterResult) {
                    updateRecordInternetStateAndTime(updateConfig.configKey(false), valid, true);
                    this.mInterResult = valid;
                    if (valid) {
                        this.mGotInternetResult |= 4;
                        this.mGotInternetResult &= -9;
                    } else {
                        this.mGotInternetResult |= 8;
                        this.mGotInternetResult &= -5;
                    }
                }
            }
        }
    }

    public void setNetworkDetailState(int netid, DetailedState dst, String bssid) {
        int detailId = netid;
        int detailState = -1;
        logD("sds: id=" + netid + ",dst = " + dst);
        if (dst == DetailedState.DISCONNECTED) {
            detailState = 0;
            this.mDetectNet = false;
        } else if (dst == DetailedState.CAPTIVE_PORTAL_CHECK) {
            detailState = 1;
            this.mDetectNet = true;
        } else if (dst == DetailedState.CONNECTED) {
            detailState = 2;
            this.mDetectNet = true;
        }
        if (detailState != -1) {
            sendMessage(obtainMessage(EVENT_NETWORK_STATE_CHANGE, netid, detailState, bssid));
        }
    }

    public void reportRssi() {
    }

    public void handleManualConnect(boolean updateState) {
        if (updateState && this.mWifiStateMachine != null) {
            this.mWifiStateMachine.sendWifiNetworkScore(79, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
        }
    }

    public void setManualConnTime(long time, boolean save, WifiConfiguration config) {
        if (time > 0) {
            OppoWifiAssistantRecord oppoWifiAssistantRecord = null;
            OppoWifiAssistantRecord newRecord = null;
            if (getCurrentState() == this.mNetworkMonitorState) {
                oppoWifiAssistantRecord = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
            }
            if (config != null) {
                String newConfigkey = config.configKey();
                newRecord = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(newConfigkey);
                if (newRecord == null) {
                    newRecord = new OppoWifiAssistantRecord();
                    newRecord.mConfigkey = newConfigkey;
                }
            }
            int isManual = getIsOppoManuConnect() ? 1 : 0;
            if (save) {
                long manualConnSaveTime = time;
                long connSaveForWlan = time - this.mAutoConnWlanTime;
                long connForWlanToData = time - this.mAutoConnDataTime;
                if (this.mAutoConnWlanTime > 0 && connSaveForWlan > 0 && connSaveForWlan < 180000) {
                    logD("stc wwb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_SAVE_FOR_WLAN, oppoWifiAssistantRecord, newRecord, isManual);
                    this.mAutoConnWlanTime = 0;
                } else if (!this.mChangeNetwork || !this.mToData || this.mAutoConnDataTime <= 0 || connForWlanToData <= 0 || connForWlanToData >= 180000) {
                    logD("stc wwc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_CONN_SAVE_FOR_WLAN, oppoWifiAssistantRecord, newRecord, isManual);
                } else {
                    logD("stc wdb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA, oppoWifiAssistantRecord, newRecord, isManual);
                    this.mAutoConnDataTime = 0;
                }
            } else {
                long manualConnNewTime = time;
            }
            long connForDataToWlan = time - this.mAutoDataToWlanTime;
            if (!this.mToData && this.mAutoDataToWlanTime > 0 && connForDataToWlan > 0 && connForDataToWlan < 180000) {
                logD("stc dwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA_TO_WLAN, oppoWifiAssistantRecord, newRecord, isManual);
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
        logD("phone clone status: " + phoneCloneStatus);
        if (getCurrentState() == this.mDisconnectState) {
            logD("disconnected, do not ignore switch!!");
            return false;
        } else if ("2".equals(phoneCloneStatus) || "3".equals(phoneCloneStatus)) {
            return true;
        } else {
            return false;
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
        if (isAutoSwitch) {
            this.mDataAutoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_NETWORK, 1) == 1;
        } else {
            this.mDataAutoSwitch = false;
        }
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.switchOnOff(isAutoSwitch);
        }
        if (this.mWifiStateMachine != null && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
            if (!isAutoSwitch) {
                long autoSwitchFoData = System.currentTimeMillis() - this.mAutoConnDataTime;
                if (this.mChangeNetwork && this.mToData && this.mAutoConnDataTime > 0 && autoSwitchFoData > 0 && autoSwitchFoData < 180000) {
                    logD("stc wdb3");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_SWITCH_FOR_DATA);
                    this.mAutoConnDataTime = 0;
                }
                if (this.mChangeNetwork && this.mToData) {
                    logD("stc dwc1 0");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_SWITCH_FOR_DATA);
                }
                this.mWifiStateMachine.sendWifiNetworkScore(79, true);
                this.mChangeNetwork = false;
                this.mTriggerData = true;
                this.mWifiStateMachine.setNetworkStatus(true);
                sendNetworkStateBroadCast(this.mLastConfigkey, true);
                sendLowQualityBroadcast(79, false);
            } else if (this.mWifiNetworkRecord != null) {
                int isThirdAppOperate;
                if (this.mIsSoftAP) {
                    isThirdAppOperate = isThirdAppOperate();
                } else {
                    isThirdAppOperate = 0;
                }
                if ((isThirdAppOperate ^ 1) != 0) {
                    OppoWifiAssistantRecord lastRecord = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
                    if (!(lastRecord == null || (lastRecord.mNetworkValid ^ 1) == 0)) {
                        sendNetworkStateBroadCast(this.mLastConfigkey, false);
                    }
                    if (!this.mInterResult && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
                        triggerInternetDetect(false);
                    }
                }
            }
        }
    }

    private void setDataAutoSwitch(boolean state) {
        if (this.mWifiStateMachine == null) {
            return;
        }
        if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) && !state) {
            long disableDataSwitchDistanceTime = System.currentTimeMillis() - this.mAutoConnDataTime;
            if (this.mChangeNetwork && this.mToData && this.mAutoConnDataTime > 0 && disableDataSwitchDistanceTime > 0 && disableDataSwitchDistanceTime < 180000) {
                logD("stc wdb4");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
                this.mAutoConnDataTime = 0;
            }
            if (this.mChangeNetwork && this.mToData) {
                logD("stc dwc1 1");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
            }
            this.mWifiStateMachine.sendWifiNetworkScore(79, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
            this.mWifiStateMachine.setNetworkStatus(true);
            sendLowQualityBroadcast(79, false);
        }
    }

    public void setFeatureState(boolean state) {
        logD("setFS: " + state);
        this.mFeatureState = state;
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.featureState(state);
        }
        if (!(this.mWifiStateMachine == null || (this.mFeatureState ^ 1) == 0 || (getCurrentState() != this.mConnectedState && getCurrentState() != this.mVerifyInternetState && getCurrentState() != this.mNetworkMonitorState))) {
            this.mWifiStateMachine.sendWifiNetworkScore(79, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
            sendNetworkStateBroadCast(this.mLastConfigkey, true);
        }
        if (this.mFeatureState) {
            this.mWifiConfigManager.setWifiNetworkAvailable(this);
            return;
        }
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
        this.mWifiConfigManager.setWifiNetworkAvailable(null);
    }

    public void detectScanResult(long time) {
        sortNetworkRecords();
        this.mLastScanTime = time;
        this.mTriggerScan = false;
        this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
        WifiInfo currentInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
        String currentSsid;
        if (currentInfo != null) {
            currentSsid = currentInfo.getSSID();
        } else {
            currentSsid = " ";
        }
        if (this.mWifiState != 3) {
            logD("wifi is not enable.");
        } else if (!this.mIsSoftAP || !isThirdAppOperate()) {
            if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) && currentInfo != null && detectSsidBelongRecord(this.mConnectingkey)) {
                logD("current state: " + getCurrentState());
            } else {
                detectNetworkAvailable(null, 0, -127, false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x005d, code:
            saveWifiNetworkRecord();
     */
    /* JADX WARNING: Missing block: B:11:0x0060, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addOrUpdateRecord(String configKey) {
        Throwable th;
        WifiInfo aourWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
        logD("aouR: configKey= " + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                try {
                    OppoWifiAssistantRecord record;
                    if (this.mWifiNetworkRecord.containsKey(configKey)) {
                        record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                        record.mBssid = this.mLastBssid;
                        record.mConnExp = false;
                        record.mNetFailCount = 0;
                        logD("aouR: contain and count = " + record.mConnSuccCout);
                    } else {
                        logD("aouR: no contain");
                        OppoWifiAssistantRecord record2 = new OppoWifiAssistantRecord();
                        try {
                            record2.mConfigkey = configKey;
                            if (aourWifiInfo == null) {
                                record2.mRssi = WifiConfiguration.INVALID_RSSI;
                                record2.mBestRssi = WifiConfiguration.INVALID_RSSI;
                                record2.mIs5G = false;
                            } else {
                                record2.mRssi = aourWifiInfo.getRssi();
                                record2.mBestRssi = aourWifiInfo.getRssi();
                                record2.mIs5G = aourWifiInfo.is5GHz();
                            }
                            record2.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                            this.mWifiNetworkRecord.put(configKey, record2);
                        } catch (Throwable th2) {
                            th = th2;
                            record = record2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    void disableNetworkWithoutInternet() {
        List<OppoWifiAssistantRecord> wifiAssistRecord = getWifiNetworkRecords();
        if (wifiAssistRecord != null && wifiAssistRecord.size() > 0) {
            for (OppoWifiAssistantRecord wnr : wifiAssistRecord) {
                if (wnr.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", Integer.valueOf(1)).intValue() || wnr.mIsCaptive) {
                    WifiConfiguration disableConf = this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                    logD("dnwi: " + (disableConf == null ? "null" : Integer.valueOf(disableConf.networkId)));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordCaptiveState(String configKey, boolean captive, boolean save) {
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    if (record.mIsCaptive != captive) {
                        record.mIsCaptive = captive;
                    }
                    if (save) {
                        if (getIsOppoManuConnect() || !this.mCaptivePortal || (this.mGotInternetResult & 8) == 8) {
                            saveWifiNetworkRecord();
                        } else {
                            updateRecordInternetStateAndTime(configKey, false, true);
                            this.mInterResult = false;
                            this.mGotInternetResult |= 8;
                            this.mGotInternetResult &= -5;
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:51:0x00cf, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordDisableState(String configKey) {
        logD("uRds: " + configKey);
        if (configKey != null) {
            if (this.mAutoSwitch && (this.mFeatureState ^ 1) == 0 && (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) == 0) {
                synchronized (this.mWifiNetworkRecord) {
                    if (this.mWifiNetworkRecord.containsKey(configKey)) {
                        OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                        if (record == null) {
                            return;
                        }
                        int disableId;
                        if (record.mWifiConfiguration != null) {
                            disableId = record.mWifiConfiguration.networkId;
                        } else {
                            WifiConfiguration disableConfig = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                            if (disableConfig == null) {
                                return;
                            }
                            disableId = disableConfig.networkId;
                        }
                        logD("uRds: nfc=" + record.mNetFailCount + ",ic=" + record.mIsCaptive);
                        if (disableId == -1) {
                            return;
                        } else if (record.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", Integer.valueOf(1)).intValue() || record.mIsCaptive) {
                            if (this.mWifiStateMachine == null || (this.mWifiStateMachine.isSupplicantAvailable() ^ 1) == 0) {
                                this.mWifiConfigManager.disableNetwork(disableId, 1000, 9);
                            } else {
                                logD("wifi is in disable or disable pending state,cancel disable!!");
                                return;
                            }
                        }
                    }
                }
            }
            logD("switch is off,no need to disable with no-internet");
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0061, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordConCount(String configKey) {
        logD("uRcc key:" + configKey);
        if (configKey != null) {
            WifiInfo ccWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    record.mConnSuccCout++;
                    record.mNetid = this.mLastNetId;
                    record.mBssid = this.mLastBssid;
                    record.mConnExp = false;
                    if (ccWifiInfo == null) {
                        record.mRssi = WifiConfiguration.INVALID_RSSI;
                        record.mBestRssi = WifiConfiguration.INVALID_RSSI;
                        record.mIs5G = false;
                    } else {
                        record.mRssi = ccWifiInfo.getRssi();
                        record.mBestRssi = ccWifiInfo.getRssi();
                        record.mIs5G = ccWifiInfo.is5GHz();
                        logD("uRcc rs:" + record.mRssi);
                    }
                    record.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordConnectFail(String configKey) {
        logD("uRcf key:" + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    logD("uRcf failCount = " + record.mConnFailCount);
                    record.mConnFailCount++;
                    record.mBssid = this.mLastBssid;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0093, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordUseTime(String configKey) {
        logD("updateRecordUseTime key= " + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    long time = System.currentTimeMillis();
                    long useTime = record.mAccessNetTime != 0 ? time - record.mAccessNetTime : 0;
                    logD("updateRecordUseTime record= " + record.mConfigkey + ", mant = " + record.mAccessNetTime + ", mint = " + record.mInternetTime + ", useTime = " + useTime);
                    record.mInternetTime = useTime;
                    record.mLastuseTime = time;
                    record.mAccessNetTime = 0;
                    if (this.mWifiConfigManager.getConfiguredNetwork(configKey) == null) {
                        record.mWifiConfiguration = null;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:57:0x018c, code:
            sortNetworkRecords();
     */
    /* JADX WARNING: Missing block: B:58:0x018f, code:
            if (r19 == false) goto L_0x0199;
     */
    /* JADX WARNING: Missing block: B:59:0x0191, code:
            sendMessage(EVENT_NETWORK_MONITOR_CHANGE);
     */
    /* JADX WARNING: Missing block: B:61:0x019d, code:
            if (r16.mAutoSwitch == false) goto L_0x01bc;
     */
    /* JADX WARNING: Missing block: B:63:0x01a3, code:
            if (r16.mFeatureState == false) goto L_0x01bc;
     */
    /* JADX WARNING: Missing block: B:65:0x01b7, code:
            if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", java.lang.Boolean.valueOf(true)).booleanValue() == false) goto L_0x01bc;
     */
    /* JADX WARNING: Missing block: B:66:0x01b9, code:
            sendNetworkStateBroadCast(r17, r18);
     */
    /* JADX WARNING: Missing block: B:67:0x01bc, code:
            saveWifiNetworkRecord();
     */
    /* JADX WARNING: Missing block: B:68:0x01bf, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordInternetStateAndTime(String configKey, boolean valid, boolean transiTo) {
        Throwable th;
        logD("urit key= " + configKey + ", valid = " + valid);
        if (configKey != null && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
            this.mLastConfigkey = configKey;
            this.mAccessNetTime = valid ? System.currentTimeMillis() : 0;
            WifiInfo uritWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
            synchronized (this.mWifiNetworkRecord) {
                try {
                    OppoWifiAssistantRecord record;
                    if (this.mWifiNetworkRecord.containsKey(configKey)) {
                        record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                        if (record == null) {
                            return;
                        }
                        logD("urit mant= " + record.mAccessNetTime + ", time = " + this.mAccessNetTime);
                        record.mAccessNetTime = this.mAccessNetTime;
                        record.mNetworkValid = valid;
                    } else {
                        OppoWifiAssistantRecord record2 = new OppoWifiAssistantRecord();
                        try {
                            record2.mConfigkey = configKey;
                            record2.mBssid = this.mLastBssid;
                            record2.mAccessNetTime = this.mAccessNetTime;
                            record2.mNetworkValid = valid;
                            record2.mConnSuccCout++;
                            record2.mWifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                            record = record2;
                        } catch (Throwable th2) {
                            th = th2;
                            record = record2;
                            throw th;
                        }
                    }
                    if (record == null) {
                        return;
                    }
                    if (uritWifiInfo != null) {
                        record.mRssi = uritWifiInfo.getRssi();
                        record.mIs5G = uritWifiInfo.is5GHz();
                    }
                    int index = getQulityIndex(record.mIs5G, record.mRssi);
                    int curScore = 79;
                    if (index == 0) {
                        curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", Integer.valueOf(30)).intValue();
                    } else if (index == 1) {
                        curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", Integer.valueOf(20)).intValue();
                    } else if (index == 2) {
                        curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                    }
                    int i;
                    int tcpIndex;
                    if (valid) {
                        for (i = 0; i < this.mNetQulityArray.length; i++) {
                            this.mNetQulityArray[i] = curScore;
                        }
                        for (tcpIndex = 0; tcpIndex < this.mTcpstateArray.length; tcpIndex++) {
                            this.mTcpstateArray[tcpIndex] = 16;
                        }
                        record.mNetQualitys[index] = curScore;
                        record.mNetFailCount = 0;
                        record.mIsCaptive = false;
                    } else {
                        for (i = 0; i < this.mNetQulityArray.length; i++) {
                            this.mNetQulityArray[i] = 10;
                        }
                        for (tcpIndex = 0; tcpIndex < this.mTcpstateArray.length; tcpIndex++) {
                            this.mTcpstateArray[tcpIndex] = 0;
                        }
                        record.mNetQualitys[index] = 10;
                        if (transiTo) {
                            record.mNetFailCount++;
                        }
                    }
                    this.mWifiNetworkRecord.put(configKey, record);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0057, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordWifiConfig(String configKey) {
        logD("updateRecordWifiConfig key:" + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    if (record.mWifiConfiguration != null) {
                        logD("updateRecordWifiConfig record.config= " + record.mWifiConfiguration.SSID);
                        record.mWifiConfiguration = null;
                    }
                    record.mNetworkValid = false;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:234:0x0952, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void updateRecordLinkQuality(RssiPacketCountInfo info) {
        OppoWifiAssistantRecord mCandidate = null;
        boolean willBeRoam = false;
        OppoWifiAssistantRecord mLastRecord = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
        if (!(mLastRecord == null || info == null)) {
            if (info.rssi > WifiConfiguration.INVALID_RSSI) {
                int i;
                int score = 79;
                int rssi = info.rssi;
                int txbad = info.txbad;
                int txgood = info.txgood;
                mLastRecord.mRssi = rssi;
                if (rssi > mLastRecord.mBestRssi) {
                    mLastRecord.mBestRssi = rssi;
                }
                int newTcpStatus = 0;
                boolean tcpGood = false;
                if (!(this.mChangeNetwork && (this.mToData ^ 1) == 0)) {
                    if (this.mOppoTcpInfoMonitor != null) {
                        newTcpStatus = this.mTcpLinkStatus;
                    }
                    if (newTcpStatus != 0) {
                        this.mTcpstateArray[this.mIndex] = newTcpStatus;
                        for (int j = 0; j < 3; j++) {
                            if (this.mTcpstateArray[j] == 16) {
                                tcpGood = true;
                            }
                        }
                        if (!tcpGood) {
                            score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_TCP_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
                        } else if (newTcpStatus != 16 && this.mOldTcpStatus != 16) {
                            score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                        } else if (newTcpStatus != 16) {
                            score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD", Integer.valueOf(0)).intValue();
                        }
                        this.mOldTcpStatus = newTcpStatus;
                    }
                }
                boolean tcpAvailable;
                if (newTcpStatus == 0) {
                    tcpAvailable = false;
                } else {
                    tcpAvailable = true;
                }
                int netQualityIndex = getQulityIndex(mLastRecord.mIs5G, rssi);
                if (netQualityIndex == 0) {
                    int badRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", Integer.valueOf(30)).intValue();
                    if (this.mToData || (tcpAvailable ^ 1) != 0) {
                        score -= badRssiScore + 5;
                    } else {
                        score -= badRssiScore;
                    }
                } else if (netQualityIndex == 1) {
                    int lowRssiScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", Integer.valueOf(20)).intValue();
                    if (this.mToData || (tcpAvailable ^ 1) != 0) {
                        score -= lowRssiScore + 5;
                    } else {
                        score -= lowRssiScore;
                    }
                } else if (netQualityIndex == 2) {
                    score -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                }
                int dbad = txbad - this.mLastTxBad;
                int dgood = txgood - this.mLastTxGood;
                int dtotal = dbad + dgood;
                this.mLastTxBad = txbad;
                this.mLastTxGood = txgood;
                double loss = 0.0d;
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                double lowLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD", Double.valueOf(0.5d)).doubleValue();
                int noTrafficScore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                if (dtotal > 0) {
                    loss = ((double) dbad) / ((double) dtotal);
                    if (loss == 0.0d) {
                        this.mLinkDetectTimes++;
                    } else {
                        this.mLinkDetectTimes = 0;
                        double badLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD", Double.valueOf(STATIC_BAD_LINK_LOSS_THRESHOLD)).doubleValue();
                        double goodLinkLossThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD", Double.valueOf(STATIC_GOOD_LINK_LOSS_THRESHOLD)).doubleValue();
                        if (loss >= badLinkLossThreshold) {
                            int badLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD", Integer.valueOf(35)).intValue();
                            if (this.mToData || (tcpAvailable ^ 1) != 0) {
                                score -= badLinkScoreThreshold + 5;
                            } else {
                                score -= badLinkScoreThreshold;
                            }
                        } else if (loss >= lowLinkLossThreshold && loss < badLinkLossThreshold) {
                            int lowLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD", Integer.valueOf(30)).intValue();
                            if (this.mToData || (tcpAvailable ^ 1) != 0) {
                                score -= lowLinkScoreThreshold + 5;
                            } else {
                                score -= lowLinkScoreThreshold;
                            }
                        } else if (loss >= goodLinkLossThreshold && loss < lowLinkLossThreshold) {
                            score -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD", Integer.valueOf(20)).intValue();
                        }
                    }
                } else {
                    this.mLinkDetectTimes = 0;
                    if (this.mToData || (tcpAvailable ^ 1) != 0) {
                        score -= noTrafficScore + 5;
                    } else {
                        score -= noTrafficScore;
                    }
                }
                this.mLossArray[this.mIndex] = loss;
                int badLossCount = 0;
                for (double d : this.mLossArray) {
                    if (d >= lowLinkLossThreshold) {
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
                for (i = netQualityIndex; i < 4; i++) {
                    if (score > mLastRecord.mNetQualitys[i]) {
                        mLastRecord.mNetQualitys[i] = score;
                        mLastRecord.mScore = score;
                    }
                }
                logD("urLQ key:" + this.mLastConfigkey + ",ri=" + rssi + " tb=" + txbad + " tg=" + txgood + ",db = " + dbad + ", dg = " + dgood + ",dt= " + dtotal + ",blc=" + badLossCount + ",ls=" + loss + ",mLDTs= " + this.mLinkDetectTimes + ",tstat:" + newTcpStatus + ",dex =" + this.mIndex + ",sc= " + score + ",mis=" + this.mInterResult + ",mcs=" + this.mChangeScore + ",mqgc=" + this.mNetQulityGoodCount);
                this.mNetQulityArray[this.mIndex] = score;
                this.mIndex++;
                this.mIndex %= 3;
                int sumQuality = 0;
                for (i = 0; i < 3; i++) {
                    sumQuality += this.mNetQulityArray[i];
                }
                int currentQuality = sumQuality / 3;
                int comPareScore = currentQuality > score ? currentQuality : score;
                logD("cQ=" + currentQuality + ",cS=" + comPareScore + ",wit=" + this.mWlanInvalidThreshold + ",mcnt=" + this.mChangeNetwork + ",mtd=" + this.mTriggerData + ",mtod=" + this.mToData);
                broadcastInfotoTestapk("ssid:" + mLastRecord.mConfigkey + ",net:" + mLastRecord.mNetworkValid + ",currScore:" + score + ",score:" + currentQuality + ", rssi:" + rssi + ",loss:" + decimalFormat.format(100.0d * loss) + ",total:" + dtotal + ",tcp:" + newTcpStatus);
                if (this.mAutoSwitch && (this.mFeatureState ^ 1) == 0 && (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) == 0) {
                    if (currentQuality <= 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_POLL_THRESHOLD", Integer.valueOf(15)).intValue() || this.mChangeNetwork) {
                        this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SAMPL_INTERVAL", Integer.valueOf(STATIC_BAD_LINK_SAMPL_INTERVAL)).intValue();
                    } else {
                        this.mLinkInterval = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", Integer.valueOf(5000)).intValue();
                    }
                    long now = System.currentTimeMillis();
                    if (currentQuality < 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_DATA_THRESHOLD", Integer.valueOf(20)).intValue() && this.mTriggerData) {
                        this.mLastTrigDataTime = now;
                        sendLowQualityBroadcast(currentQuality, true);
                        this.mTriggerData = false;
                    }
                    int trigWlanDetectCore = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD", Integer.valueOf(25)).intValue();
                    int roamDetectCount = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_ROAM_DETECT", Integer.valueOf(6)).intValue();
                    if (currentQuality <= 79 - trigWlanDetectCore) {
                        if (score <= 79 - trigWlanDetectCore && this.mLastScanTime != 0 && now - this.mLastScanTime > STAIC_SCAN_RESULT_AGE && this.mTrigScanCount < 3 && (this.mTriggerScan ^ 1) != 0 && this.mWifiStateMachine != null) {
                            this.mWifiStateMachine.startScan(-1, 0, null, null);
                            this.mTrigScanCount++;
                            this.mTriggerScan = true;
                            this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SCAN_TIMEOUT), 6000);
                        }
                        if (!this.mTriggerScan) {
                            if (mLastRecord.mNetworkValid) {
                                willBeRoam = detectMaybeRoam(mLastRecord, comPareScore);
                            }
                            if (willBeRoam && this.mRoamdetectCount < roamDetectCount) {
                                this.mRoamdetectCount++;
                            } else if (score <= 79 - trigWlanDetectCore) {
                                mCandidate = detectNetworkAvailable(mLastRecord, comPareScore, mLastRecord.mRssi, false);
                            }
                        }
                    }
                    if (this.mAlertDialog != null && getIsOppoManuConnect() && (this.mToData ^ 1) != 0 && mLastRecord.mNetworkValid) {
                        if (currentQuality > 79 - trigWlanDetectCore) {
                            this.mNetQulityGoodCount++;
                            if (this.mNetQulityGoodCount > roamDetectCount) {
                                dismissDialog(2);
                                this.mNetQulityGoodCount = 0;
                            }
                        } else {
                            this.mNetQulityGoodCount = 0;
                        }
                    }
                    if (!this.mToData && this.mChangeNetwork && currentQuality > 50) {
                        changeNetworkToWlan(mLastRecord, currentQuality, false);
                    }
                    if (this.mWlanInvalidThreshold == 70 && currentQuality <= 39 && this.mAutoSwitchDataDisableTime > 0) {
                        long napTime = System.currentTimeMillis() - this.mAutoSwitchDataDisableTime;
                        logD("nt= " + napTime);
                        if (napTime > getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME", Long.valueOf(AUTO_SWITCH_DATA_DISBALE_TIME)).longValue()) {
                            resetAutoSwitchDataDetect();
                        }
                    }
                    int wlanBadThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD", Integer.valueOf(this.mWlanInvalidThreshold)).intValue();
                    if (this.mChangeNetwork || currentQuality > 79 - wlanBadThreshold || score > 79 - wlanBadThreshold) {
                        if (this.mChangeNetwork && this.mToData) {
                            int goodLinkCountThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT", Integer.valueOf(3)).intValue();
                            if (mLastRecord.mNetworkValid && (this.mLastInternetResult ^ 1) != 0 && this.mChangeRssi != 0 && rssi >= STATIC_LOW_RSSI_5 && currentQuality >= 50 && score >= 50 && this.mChangeScore == 10) {
                                changeNetworkToWlan(mLastRecord, score, true);
                            } else if ((rssi <= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD", Integer.valueOf(STATIC_RSSI_TO_WLAN_THRESHOLD)).intValue() || this.mLinkDetectTimes < goodLinkCountThreshold) && (rssi < -65 || this.mLinkDetectTimes < goodLinkCountThreshold - 2)) {
                                if (rssi >= -65 && this.mTriggerInter && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                    logD("dt inter for strong rssi");
                                    this.mTriggerInter = false;
                                    this.mAlarmManager.cancel(this.mDetectInterIntent);
                                    this.mAlarmManager.set(0, System.currentTimeMillis(), this.mDetectInterIntent);
                                }
                            } else if (this.mChangeNetwork && mLastRecord.mNetworkValid && currentQuality >= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD", Integer.valueOf(64)).intValue() && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= 5) {
                                changeNetworkToWlan(mLastRecord, currentQuality, true);
                            }
                        }
                    } else if ((!willBeRoam || (willBeRoam && this.mRoamdetectCount >= roamDetectCount)) && mCandidate == null && (this.mTriggerScan ^ 1) != 0) {
                        if (!mLastRecord.mNetworkValid) {
                            currentQuality = 10;
                        } else if (currentQuality < 20) {
                            currentQuality = 20;
                        }
                        if (!(getIsOppoManuConnect() || !this.mDataAutoSwitch || (shouldIgnoreSwitch() ^ 1) == 0)) {
                            this.mChangeNetwork = true;
                            this.mTriggerInter = true;
                            this.mChangeRssi = rssi;
                            this.mChangeScore = currentQuality;
                            this.mWifiStateMachine.sendWifiNetworkScore(currentQuality, false);
                        }
                    }
                } else {
                    logD("smart switch disable");
                }
            }
        }
    }

    private void changeNetworkToWlan(OppoWifiAssistantRecord record, int quality, boolean dataToWlan) {
        if (this.mWifiStateMachine != null) {
            this.mWifiStateMachine.sendWifiNetworkScore(quality, true);
            this.mChangeNetwork = false;
            this.mTriggerInter = false;
            this.mRoamdetectCount = 0;
            this.mChangeRssi = 0;
            this.mChangeScore = 0;
            this.mTrigScanCount = 0;
            sendLowQualityBroadcast(quality, false);
            this.mTriggerData = true;
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
        this.mBroadInfo = info;
        if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_TEST", Boolean.valueOf(false)).booleanValue()) {
            PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
            if ((pm == null || pm.getPackageUid(this.mTestApk, 65536, 0) >= 1000) && this.mBroadHandle != null) {
                this.mBroadHandle.post(new Runnable() {
                    public void run() {
                        Intent broadIntent = new Intent(OppoWifiAssistantStateTraker.BRAOD_WIFI_INFO);
                        Log.d("testFeature", "mBroadInfo = " + OppoWifiAssistantStateTraker.this.mBroadInfo);
                        broadIntent.putExtra(OppoWifiAssistantStateTraker.EXTRA_WIFI_NETINFO, OppoWifiAssistantStateTraker.this.mBroadInfo);
                        OppoWifiAssistantStateTraker.this.mContext.sendStickyBroadcastAsUser(broadIntent, UserHandle.ALL);
                    }
                });
            }
        }
    }

    public void updateWifiNetworkConfig(String name, String value) {
        if (value != null && name != null) {
            if (name.equals("SmartWlanFeature")) {
                String featureState = value;
                if (value.equals("false")) {
                    setFeatureState(false);
                } else {
                    setFeatureState(true);
                }
            } else {
                logD("default/");
            }
        }
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getFloatValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getLongValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return Boolean.valueOf(this.mWifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c7 A:{SYNTHETIC, Splitter: B:47:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00cc A:{SYNTHETIC, Splitter: B:50:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0079 A:{SYNTHETIC, Splitter: B:16:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:67:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x007e A:{SYNTHETIC, Splitter: B:19:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3 A:{SYNTHETIC, Splitter: B:36:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b8 A:{SYNTHETIC, Splitter: B:39:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c7 A:{SYNTHETIC, Splitter: B:47:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00cc A:{SYNTHETIC, Splitter: B:50:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0079 A:{SYNTHETIC, Splitter: B:16:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x007e A:{SYNTHETIC, Splitter: B:19:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:67:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3 A:{SYNTHETIC, Splitter: B:36:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b8 A:{SYNTHETIC, Splitter: B:39:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveWifiNetworkRecord() {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        List<OppoWifiAssistantRecord> wifiRecords = getWifiNetworkRecords();
        FileWriter config = null;
        BufferedWriter out = null;
        try {
            BufferedWriter out2;
            FileWriter config2 = new FileWriter(WIFI_ASSISTANT_FILE);
            try {
                out2 = new BufferedWriter(config2);
            } catch (FileNotFoundException e3) {
                e = e3;
                config = config2;
                try {
                    logD("FileNotFoundException: " + e);
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                        }
                    }
                    if (config != null) {
                        try {
                            config.close();
                            return;
                        } catch (IOException e5) {
                            return;
                        }
                    }
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (config != null) {
                        try {
                            config.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e8) {
                e2 = e8;
                config = config2;
                logD("IOException: " + e2);
                e2.printStackTrace();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e9) {
                    }
                }
                if (config != null) {
                    try {
                        config.close();
                        return;
                    } catch (IOException e10) {
                        return;
                    }
                }
                return;
            } catch (Throwable th3) {
                th = th3;
                config = config2;
                if (out != null) {
                }
                if (config != null) {
                }
                throw th;
            }
            try {
                logD("swR length = " + wifiRecords.size());
                for (OppoWifiAssistantRecord wnr : wifiRecords) {
                    out2.write(wnr.toTagString() + "\n");
                    out2.flush();
                }
                out2.close();
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e11) {
                    }
                }
                if (config2 != null) {
                    try {
                        config2.close();
                    } catch (IOException e12) {
                    }
                }
            } catch (FileNotFoundException e13) {
                e = e13;
                out = out2;
                config = config2;
                logD("FileNotFoundException: " + e);
                if (out != null) {
                }
                if (config != null) {
                }
            } catch (IOException e14) {
                e2 = e14;
                out = out2;
                config = config2;
                logD("IOException: " + e2);
                e2.printStackTrace();
                if (out != null) {
                }
                if (config != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                out = out2;
                config = config2;
                if (out != null) {
                }
                if (config != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e15) {
            e = e15;
            logD("FileNotFoundException: " + e);
            if (out != null) {
            }
            if (config != null) {
            }
        } catch (IOException e16) {
            e2 = e16;
            logD("IOException: " + e2);
            e2.printStackTrace();
            if (out != null) {
            }
            if (config != null) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:149:0x02f9, code:
            throw r40;
     */
    /* JADX WARNING: Missing block: B:257:0x0584, code:
            logD("readWifiNetworkRecord end");
     */
    /* JADX WARNING: Missing block: B:258:0x058e, code:
            if (r0 == null) goto L_0x0593;
     */
    /* JADX WARNING: Missing block: B:260:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:265:0x059f, code:
            r40 = th;
     */
    /* JADX WARNING: Missing block: B:266:0x05a0, code:
            r36 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readWifiNetworkRecord() {
        BufferedReader bufferedReader;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader bufferedReader2 = null;
        if (this.mWifiNetworkRecord == null) {
            logD("mWifiNetworkRecord exception, re-init it");
            this.mWifiNetworkRecord = new HashMap();
        }
        synchronized (this.mWifiNetworkRecord) {
            try {
                this.mWifiNetworkRecord.clear();
                try {
                    bufferedReader = new BufferedReader(new FileReader(WIFI_ASSISTANT_FILE));
                    try {
                        String line = "";
                        logD("rwR st");
                        while (true) {
                            line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            OppoWifiAssistantRecord mwr = new OppoWifiAssistantRecord();
                            String[] data = line.split("\t");
                            int netid = -1;
                            int bestRssi = WifiConfiguration.INVALID_RSSI;
                            int connSuccCout = 0;
                            int connFailCount = 0;
                            long accessNetTime = 0;
                            long internetTime = 0;
                            long lastuseTime = 0;
                            int netFailCount = 0;
                            int maxSpeed = 0;
                            int score = 0;
                            int i;
                            WifiConfiguration config;
                            if (data.length == 16) {
                                try {
                                    netid = Integer.parseInt(data[0]);
                                } catch (NumberFormatException e3) {
                                    logD("NumberFormatException e:" + e3);
                                }
                                mwr.mNetid = netid;
                                mwr.mConfigkey = data[1];
                                mwr.mBssid = data[2];
                                try {
                                    bestRssi = Integer.parseInt(data[3]);
                                } catch (NumberFormatException e4) {
                                }
                                mwr.mBestRssi = bestRssi;
                                try {
                                    connSuccCout = Integer.parseInt(data[4]);
                                } catch (NumberFormatException e5) {
                                }
                                mwr.mConnSuccCout = connSuccCout;
                                try {
                                    connFailCount = Integer.parseInt(data[5]);
                                } catch (NumberFormatException e6) {
                                }
                                mwr.mConnFailCount = connFailCount;
                                try {
                                    accessNetTime = (long) Integer.parseInt(data[6]);
                                } catch (NumberFormatException e7) {
                                }
                                mwr.mAccessNetTime = accessNetTime;
                                try {
                                    internetTime = (long) Integer.parseInt(data[7]);
                                } catch (NumberFormatException e8) {
                                }
                                mwr.mInternetTime = internetTime;
                                try {
                                    lastuseTime = (long) Integer.parseInt(data[8]);
                                } catch (NumberFormatException e9) {
                                }
                                mwr.mLastuseTime = lastuseTime;
                                try {
                                    netFailCount = Integer.parseInt(data[9]);
                                } catch (NumberFormatException e10) {
                                }
                                mwr.mNetFailCount = netFailCount;
                                try {
                                    maxSpeed = Integer.parseInt(data[10]);
                                } catch (NumberFormatException e11) {
                                }
                                mwr.mMaxSpeed = maxSpeed;
                                mwr.mIs5G = data[11].equals("true");
                                mwr.mNetworkValid = data[12].equals("true");
                                mwr.mConnExp = data[13].equals("true");
                                if (mwr.mConnExp) {
                                    mwr.mConnExp = false;
                                }
                                try {
                                    score = Integer.parseInt(data[14]);
                                } catch (NumberFormatException e12) {
                                }
                                mwr.mScore = score;
                                String[] netQulity = data[15].split(",");
                                if (netQulity.length == mwr.mNetQualitys.length) {
                                    i = 0;
                                    while (i < mwr.mNetQualitys.length) {
                                        int quality = -1;
                                        try {
                                            quality = Integer.parseInt(netQulity[i]);
                                        } catch (NumberFormatException e13) {
                                        }
                                        mwr.mNetQualitys[i] = quality;
                                        if (i >= 1 && mwr.mNetQualitys[i] < mwr.mNetQualitys[i - 1]) {
                                            mwr.mNetQualitys[i] = mwr.mNetQualitys[i - 1];
                                        }
                                        i++;
                                    }
                                }
                                config = this.mWifiConfigManager.getConfiguredNetwork(mwr.mConfigkey);
                                if (config != null) {
                                    mwr.mWifiConfiguration = config;
                                }
                            } else {
                                int i2 = 0;
                                int length = data.length;
                                while (true) {
                                    int i3 = i2;
                                    if (i3 >= length) {
                                        break;
                                    }
                                    String subData = data[i3];
                                    if (subData.startsWith(NID_STR)) {
                                        try {
                                            netid = Integer.parseInt(subData.substring(NID_STR.length()));
                                        } catch (NumberFormatException e14) {
                                        }
                                        mwr.mNetid = netid;
                                    } else if (subData.startsWith(CONKEY_STR)) {
                                        mwr.mConfigkey = subData.substring(CONKEY_STR.length());
                                    } else if (subData.startsWith(BSSID_STR)) {
                                        mwr.mBssid = subData.substring(BSSID_STR.length());
                                    } else if (subData.startsWith(BRS_STR)) {
                                        try {
                                            bestRssi = Integer.parseInt(subData.substring(BRS_STR.length()));
                                        } catch (NumberFormatException e15) {
                                        }
                                        mwr.mBestRssi = bestRssi;
                                    } else if (subData.startsWith(CSC_STR)) {
                                        try {
                                            connSuccCout = Integer.parseInt(subData.substring(CSC_STR.length()));
                                        } catch (NumberFormatException e16) {
                                        }
                                        mwr.mConnSuccCout = connSuccCout;
                                    } else if (subData.startsWith(CFC_STR)) {
                                        try {
                                            connFailCount = Integer.parseInt(subData.substring(CFC_STR.length()));
                                        } catch (NumberFormatException e17) {
                                        }
                                        mwr.mConnFailCount = connFailCount;
                                    } else if (subData.startsWith(ANT_STR)) {
                                        try {
                                            accessNetTime = Long.parseLong(subData.substring(ANT_STR.length()));
                                        } catch (NumberFormatException e18) {
                                        }
                                        mwr.mAccessNetTime = accessNetTime;
                                    } else if (subData.startsWith(INT_STR)) {
                                        try {
                                            internetTime = Long.parseLong(subData.substring(INT_STR.length()));
                                        } catch (NumberFormatException e19) {
                                        }
                                        mwr.mInternetTime = internetTime;
                                    } else if (subData.startsWith(LST_STR)) {
                                        try {
                                            lastuseTime = Long.parseLong(subData.substring(LST_STR.length()));
                                        } catch (NumberFormatException e20) {
                                        }
                                        mwr.mLastuseTime = lastuseTime;
                                    } else if (subData.startsWith(NFC_STR)) {
                                        try {
                                            netFailCount = Integer.parseInt(subData.substring(NFC_STR.length()));
                                        } catch (NumberFormatException e21) {
                                        }
                                        mwr.mNetFailCount = netFailCount;
                                    } else if (subData.startsWith(MS_STR)) {
                                        try {
                                            maxSpeed = Integer.parseInt(subData.substring(MS_STR.length()));
                                        } catch (NumberFormatException e22) {
                                        }
                                        mwr.mMaxSpeed = maxSpeed;
                                    } else if (subData.startsWith(HF_STR)) {
                                        mwr.mIs5G = subData.substring(HF_STR.length()).equals("true");
                                    } else if (subData.startsWith(NV_STR)) {
                                        mwr.mNetworkValid = subData.substring(NV_STR.length()).equals("true");
                                    } else if (subData.startsWith(CE_STR)) {
                                        mwr.mConnExp = subData.substring(CE_STR.length()).equals("true");
                                    } else if (subData.startsWith(SCORE_STR)) {
                                        try {
                                            score = Integer.parseInt(subData.substring(SCORE_STR.length()));
                                        } catch (NumberFormatException e23) {
                                        }
                                        mwr.mScore = score;
                                    } else if (subData.startsWith(NQL_STR)) {
                                        String[] netQulitys = subData.substring(NQL_STR.length()).split(",");
                                        if (netQulitys.length == mwr.mNetQualitys.length) {
                                            i = 0;
                                            while (i < mwr.mNetQualitys.length) {
                                                int newQuality = -1;
                                                try {
                                                    newQuality = Integer.parseInt(netQulitys[i]);
                                                } catch (NumberFormatException e24) {
                                                }
                                                mwr.mNetQualitys[i] = newQuality;
                                                if (i >= 1 && mwr.mNetQualitys[i] < mwr.mNetQualitys[i - 1]) {
                                                    mwr.mNetQualitys[i] = mwr.mNetQualitys[i - 1];
                                                }
                                                i++;
                                            }
                                        }
                                    } else if (subData.startsWith(CAP_STR)) {
                                        mwr.mIsCaptive = subData.substring(CAP_STR.length()).equals("true");
                                    }
                                    i2 = i3 + 1;
                                }
                                config = this.mWifiConfigManager.getConfiguredNetwork(mwr.mConfigkey);
                                if (config != null) {
                                    mwr.mWifiConfiguration = config;
                                }
                            }
                            if (mwr.mConfigkey != null) {
                                this.mWifiNetworkRecord.put(mwr.mConfigkey, mwr);
                            }
                        }
                    } catch (FileNotFoundException e25) {
                        e = e25;
                        bufferedReader2 = bufferedReader;
                    } catch (IOException e26) {
                        e2 = e26;
                        bufferedReader2 = bufferedReader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader2 = bufferedReader;
                    }
                } catch (FileNotFoundException e27) {
                    e = e27;
                    try {
                        logD("readWifiNetworkRecord: FileNotFoundException: " + e);
                        if (bufferedReader2 != null) {
                            try {
                                bufferedReader2.close();
                            } catch (IOException e28) {
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader2 != null) {
                            try {
                                bufferedReader2.close();
                            } catch (IOException e29) {
                            }
                        }
                        throw th;
                    }
                } catch (IOException e30) {
                    e2 = e30;
                    logD("readWifiNetworkRecord: IOException: " + e2);
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e31) {
                        }
                    }
                }
            } catch (Throwable th4) {
                th = th4;
            }
        }
        bufferedReader2 = bufferedReader;
    }

    private void pintRecord() {
        for (OppoWifiAssistantRecord wnr : getWifiNetworkRecords()) {
            logD("pintRecord wnr: " + wnr.toString());
        }
    }

    private void sortNetworkRecords() {
        List<ScanResult> currentScan = this.mWifiStateMachine.syncGetScanResultsList();
        List<OppoWifiAssistantRecord> tempRecords = getWifiNetworkRecords();
        List<OppoWifiAssistantRecord> mSortValidRecords = new ArrayList();
        for (OppoWifiAssistantRecord tRecord : tempRecords) {
            if (tRecord.mNetworkValid && this.mWifiConfigManager.getConfiguredNetwork(tRecord.mConfigkey) != null) {
                int refeRssi = WifiConfiguration.INVALID_RSSI;
                ScanResult refScan = null;
                for (ScanResult result : currentScan) {
                    String ssid = "\"" + result.SSID + "\"";
                    String bssid = result.BSSID;
                    String capabilitie = result.capabilities;
                    if (WifiConfiguration.configKey(result).equals(tRecord.mConfigkey) && result.level > refeRssi) {
                        refeRssi = result.level;
                        refScan = result;
                    }
                }
                if (!(refeRssi == WifiConfiguration.INVALID_RSSI || refScan == null)) {
                    tRecord.mRssi = refeRssi;
                    int index = getQulityIndex(refScan.is5GHz(), refeRssi);
                    logD("snr:" + tRecord.mConfigkey + ",mri:" + refeRssi + ",mnq[" + index + "]=" + tRecord.mNetQualitys[index]);
                    if (tRecord.mNetQualitys[index] > 0) {
                        tRecord.mScore = tRecord.mNetQualitys[index];
                    }
                    mSortValidRecords.add(tRecord);
                }
            }
        }
        Collections.sort(mSortValidRecords, new Comparator<OppoWifiAssistantRecord>() {
            public int compare(OppoWifiAssistantRecord b1, OppoWifiAssistantRecord b2) {
                if (b2.mScore == 0 || b1.mScore == 0 || b2.mScore == b1.mScore) {
                    return b2.mRssi - b1.mRssi;
                }
                return b2.mScore - b1.mScore;
            }
        });
        synchronized (this.mSortNetworkRecord) {
            this.mSortNetworkRecord.clear();
            for (OppoWifiAssistantRecord sRecord : mSortValidRecords) {
                if (this.mWifiConfigManager.getConfiguredNetwork(sRecord.mConfigkey) != null) {
                    this.mSortNetworkRecord.add(sRecord);
                }
            }
        }
    }

    public List<OppoWifiAssistantRecord> getWifiNetworkRecords() {
        List<OppoWifiAssistantRecord> wifiRecord = new ArrayList();
        synchronized (this.mWifiNetworkRecord) {
            for (OppoWifiAssistantRecord wnr : this.mWifiNetworkRecord.values()) {
                wifiRecord.add(new OppoWifiAssistantRecord(wnr));
            }
        }
        return wifiRecord;
    }

    private List<OppoWifiAssistantRecord> getSortNetworkRecords() {
        List<OppoWifiAssistantRecord> sortRecord = new ArrayList();
        synchronized (this.mSortNetworkRecord) {
            for (OppoWifiAssistantRecord wnr : this.mSortNetworkRecord) {
                sortRecord.add(new OppoWifiAssistantRecord(wnr));
            }
        }
        return sortRecord;
    }

    public List<WifiConfiguration> getValidSortConfig() {
        List<WifiConfiguration> mSortConfig = new ArrayList();
        synchronized (this.mSortNetworkRecord) {
            for (OppoWifiAssistantRecord wnr : this.mSortNetworkRecord) {
                WifiConfiguration recordConfig = this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                if (recordConfig != null) {
                    mSortConfig.add(new WifiConfiguration(recordConfig));
                }
            }
        }
        return mSortConfig;
    }

    private int getQulityIndex(boolean is5G, int rssi) {
        if (is5G) {
            int goodRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_5", Integer.valueOf(-65)).intValue();
            int lowRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_5", Integer.valueOf(STATIC_LOW_RSSI_5)).intValue();
            int badRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_5", Integer.valueOf(STATIC_BAD_RSSI_5)).intValue();
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
        int goodRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_24", Integer.valueOf(-65)).intValue();
        int lowRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_24", Integer.valueOf(STATIC_LOW_RSSI_24)).intValue();
        int badRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_24", Integer.valueOf(-83)).intValue();
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
        if (this.mWifiStateMachine == null || (this.mWifiStateMachine.isSupplicantAvailable() ^ 1) == 0) {
            if (this.mWifiStateMachine != null) {
                if (this.mWifiStateMachine.isNetworkAutoConnectingOrConnected(selectConf.networkId)) {
                    logD("network: " + selectConf.SSID + " is connecting or connected, do nothing!");
                    return;
                }
                this.mWifiStateMachine.setTargetNetworkId(lastRecord.mNetid);
                this.mWifiStateMachine.clearTargetBssid(TAG);
                this.mWifiStateMachine.prepareForForcedConnection(selectConf.networkId);
                this.mWifiStateMachine.startConnectToNetwork(selectConf.networkId, 1000, "any");
            }
            if (this.mSupplicantTracker != null) {
                this.mSupplicantTracker.sendMessage(131372);
            }
            return;
        }
        logD("wifi is in disable or disable pending state,cancel reconnect!!");
    }

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
        String currentKey = "null";
        for (ScanResult result : this.mWifiStateMachine.syncGetScanResultsList()) {
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String capabilitie = result.capabilities;
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

    private void detectSwitchDataFrequence() {
        if (this.mWifiStateMachine != null) {
            this.mAutoSwitchDataCount++;
            long curTime = System.currentTimeMillis();
            int thresholdCount = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_COUNT", Integer.valueOf(5)).intValue();
            long thresholdTime = getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_TIME", Long.valueOf(AUTO_SWITCH_DATA_THRESHOLD_TIME)).longValue();
            this.mAutoSwitchDataTimes[this.mAutoSwitchDataIndex] = curTime;
            if (this.mAutoSwitchDataCount >= thresholdCount) {
                this.mAutoSwitchDataCount--;
                int oldestIndex = (this.mAutoSwitchDataIndex + 1) % 5;
                if (this.mAutoSwitchDataTimes[oldestIndex] > 0 && curTime - this.mAutoSwitchDataTimes[oldestIndex] > 0 && curTime - this.mAutoSwitchDataTimes[oldestIndex] <= thresholdTime) {
                    this.mWlanInvalidThreshold = 70;
                    this.mAutoSwitchDataDisableTime = curTime;
                    logD("dsdf time= " + curTime);
                }
            }
            this.mAutoSwitchDataIndex++;
            this.mAutoSwitchDataIndex %= 5;
        }
    }

    private void resetAutoSwitchDataDetect() {
        if (this.mWifiStateMachine != null) {
            for (int mAsdtIndex = 0; mAsdtIndex < this.mAutoSwitchDataTimes.length; mAsdtIndex++) {
                this.mAutoSwitchDataTimes[mAsdtIndex] = 0;
            }
            this.mAutoSwitchDataDisableTime = 0;
            this.mAutoSwitchDataCount = 0;
            this.mAutoSwitchDataIndex = 0;
            this.mWlanInvalidThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", Integer.valueOf(40)).intValue();
        }
    }

    private boolean detectSsidBelongRecord(String key) {
        boolean contain = false;
        if (key == null) {
            return false;
        }
        synchronized (this.mSortNetworkRecord) {
            if (this.mSortNetworkRecord.size() > 0) {
                for (OppoWifiAssistantRecord network : this.mSortNetworkRecord) {
                    if (network.mConfigkey.equals(key)) {
                        contain = true;
                        break;
                    }
                }
            }
        }
        logD("detectSsidBelongRecord: return = " + contain);
        return contain;
    }

    private boolean detectMaybeRoam(OppoWifiAssistantRecord lastRecord, int score) {
        boolean maybeRoam = false;
        List<ScanResult> roamScan = this.mWifiStateMachine.syncGetScanResultsList();
        if (lastRecord == null) {
            logD("detectMaybeRoam lastRecord is null!");
            return false;
        }
        WifiInfo curWifiInfo = this.mWifiStateMachine.getWifiInfo();
        int curFreq = 2412;
        if (curWifiInfo != null) {
            curFreq = curWifiInfo.getFrequency();
        }
        for (ScanResult result : roamScan) {
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String scanConfKey = WifiConfiguration.configKey(result);
            int scanRssi = result.level;
            int scanFreq = result.frequency;
            if (scanConfKey.equals(lastRecord.mConfigkey) && scanRssi > -83 && lastRecord.mBssid != null && (scanBssid.equals(lastRecord.mBssid) ^ 1) != 0) {
                if (curFreq != scanFreq || (curFreq == scanFreq && scanRssi - lastRecord.mRssi > 5)) {
                    maybeRoam = true;
                    break;
                }
            }
        }
        logD("detectMaybeRoam maybeRoam=" + maybeRoam);
        return maybeRoam;
    }

    private void detectTraffic() {
        long txPkts = TrafficStats.getTcpTxPackets(mInterfaceName);
        long rxPkts = TrafficStats.getTcpRxPackets(mInterfaceName);
        long lastDetlaRxPkts = this.mdRxPkts;
        this.mDTxPkts = txPkts - this.mLastTxPkts;
        this.mdRxPkts = rxPkts - this.mLastRxPkts;
        this.mLastTxPkts = txPkts;
        this.mLastRxPkts = rxPkts;
        if (this.mdRxPkts >= 1 || this.mDTxPkts <= 0) {
            this.mRxPktsLowCount = 0;
        } else {
            this.mRxPktsLowCount++;
        }
        logD("DT,tP=" + txPkts + ", dTP=" + this.mDTxPkts + ", rP=" + rxPkts + ", dRP=" + this.mdRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
        if (!this.mInterResult) {
            if (!this.mCaptivePortal ? this.mResponseGotFromGateway : true) {
                if (lastDetlaRxPkts > 20 && this.mdRxPkts > 20 && this.mDTxPkts > 20) {
                    triggerInternetDetect(true);
                } else if (this.mdRxPkts > 2 && this.mDTxPkts > 2) {
                    triggerInternetDetect(false);
                }
            } else if (this.mdRxPkts > 2 && this.mDTxPkts > 2) {
                triggerInternetDetect(true);
            }
        } else if (this.mInterResult && this.mRxPktsLowCount >= 2) {
            triggerInternetDetect(true);
            this.mRxPktsLowCount = 0;
        }
    }

    private void triggerInternetDetect(boolean forceTrigger) {
        long standoffTime = System.currentTimeMillis() - this.mLastDetectInter;
        this.mTcpInterval = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
        logD("triggerInternetDetect, forceTrigger=" + forceTrigger + ", mLastInternetResult=" + this.mLastInternetResult + ", standoffTime=" + standoffTime + ", mInternetStandoffTime=" + this.mInternetStandoffTime);
        if (standoffTime >= 10000) {
            if (forceTrigger || this.mLastInternetResult != this.mInterResult) {
                sendMessage(CMD_INTERNET_MONITOR);
                this.mInternetStandoffTime = INTERNET_INTERVAL_DELTA;
            } else if (standoffTime > ((long) this.mInternetStandoffTime)) {
                sendMessage(CMD_INTERNET_MONITOR);
                if (this.mInternetStandoffTime < WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS) {
                    this.mInternetStandoffTime += INTERNET_INTERVAL_DELTA;
                }
            }
        }
    }

    private OppoWifiAssistantRecord detectNetworkAvailable(OppoWifiAssistantRecord lastRecord, int curScore, int curRssi, boolean force) {
        List<OppoWifiAssistantRecord> netRecords = getSortNetworkRecords();
        List<ScanResult> scanList = this.mWifiStateMachine.syncGetScanResultsList();
        OppoWifiAssistantRecord candidate = null;
        int refScore = curScore;
        int refRssi = curRssi;
        double refConsuccRate = 0.0d;
        int refSpeed = 0;
        int diffScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
        double diffConnrateThreshold = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD", Double.valueOf(STATIC_DIFF_CONNRATE_THRESHOLD)).doubleValue();
        boolean refIs5G = false;
        if (lastRecord == null) {
            logD("dna scan detect," + getIsOppoManuConnect());
        }
        for (OppoWifiAssistantRecord wnr : netRecords) {
            if (!wnr.mNetworkValid || wnr.mConfigkey == null) {
                logD("config key is null or invalid");
            } else if (wnr.mConnExp) {
                logD("config co exp");
            } else if (lastRecord == null || !lastRecord.mConfigkey.equals(wnr.mConfigkey)) {
                WifiConfiguration wcf = this.mWifiConfigManager.getConfiguredNetwork(wnr.mConfigkey);
                if (wcf == null) {
                    logD("config is null");
                } else if (wcf.status == 1) {
                    logD("config is disable");
                } else {
                    double consuccRate = ((double) wnr.mConnSuccCout) / ((double) (wnr.mConnFailCount + wnr.mConnSuccCout));
                    for (ScanResult result : scanList) {
                        String scanSsid = "\"" + result.SSID + "\"";
                        String scanBssid = result.BSSID;
                        int scanRssi = result.level;
                        if (WifiConfiguration.configKey(result).equals(wnr.mConfigkey)) {
                            int index = getQulityIndex(result.is5GHz(), result.level);
                            logD("dna " + wnr.mConfigkey + ",nq[" + index + "]:" + wnr.mNetQualitys[index] + ",rfs:" + refScore + ",rfsi:" + refRssi + ",ss:" + scanRssi + ",csr:" + consuccRate);
                            if (lastRecord == null || scanRssi > -83) {
                                int diffScore;
                                if (wnr.mNetQualitys[index] == -1 || refScore == -1) {
                                    diffScore = scanRssi - refRssi;
                                } else {
                                    diffScore = wnr.mNetQualitys[index] - refScore;
                                    if (wnr.mNetQualitys[index] - curScore < diffScoreThreshold) {
                                    }
                                }
                                if (lastRecord == null || !lastRecord.mNetworkValid || scanRssi - curRssi >= 5) {
                                    if (diffScore >= diffScoreThreshold) {
                                        refScore = wnr.mNetQualitys[index];
                                        refRssi = scanRssi;
                                        refConsuccRate = consuccRate;
                                        refSpeed = wnr.mMaxSpeed;
                                        refIs5G = wnr.mIs5G;
                                        candidate = wnr;
                                    } else if (diffScore < diffScoreThreshold && diffScore >= 0) {
                                        if (!refIs5G || (result.is5GHz() ^ 1) == 0) {
                                            if (refIs5G || !result.is5GHz()) {
                                                double diffConsuccRate = consuccRate - refConsuccRate;
                                                if (diffConsuccRate >= diffConnrateThreshold) {
                                                    refScore = wnr.mNetQualitys[index];
                                                    refRssi = scanRssi;
                                                    refConsuccRate = consuccRate;
                                                    refSpeed = wnr.mMaxSpeed;
                                                    refIs5G = wnr.mIs5G;
                                                    candidate = wnr;
                                                } else if (diffConsuccRate < diffConnrateThreshold && diffConsuccRate > 0.0d && wnr.mMaxSpeed - refSpeed > 0) {
                                                    refScore = wnr.mNetQualitys[index];
                                                    refRssi = scanRssi;
                                                    refConsuccRate = consuccRate;
                                                    refSpeed = wnr.mMaxSpeed;
                                                    refIs5G = wnr.mIs5G;
                                                    candidate = wnr;
                                                }
                                            } else {
                                                refScore = wnr.mNetQualitys[index];
                                                refRssi = scanRssi;
                                                refConsuccRate = consuccRate;
                                                refSpeed = wnr.mMaxSpeed;
                                                refIs5G = wnr.mIs5G;
                                                candidate = wnr;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logD("config is same");
            }
        }
        if (candidate == null) {
            logD("cdd = null, mds=" + this.mDataScore + ",mmc=" + getIsOppoManuConnect() + ",mdas= " + this.mDataAutoSwitch + ",mds=" + this.mDataState);
        } else {
            logD("mCId=" + this.mConnectingId + ",cdd = " + candidate.toString());
        }
        if (this.mAutoSwitch && this.mFeatureState) {
            if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() && (shouldIgnoreSwitch() ^ 1) != 0) {
                if (getIsOppoManuConnect()) {
                    if (!(lastRecord == null || lastRecord.mWifiConfiguration == null || candidate == null) || (this.mDataAutoSwitch && this.mDataScore == 50 && this.mDataState && curScore <= 79 - this.mWlanInvalidThreshold)) {
                        showDialog(lastRecord);
                    }
                } else if (candidate != null) {
                    selectCandidateNetwork(candidate);
                    if (getCurrentState() == this.mNetworkMonitorState || getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState) {
                        this.mCandidateKey = candidate.mConfigkey;
                        setAssistantStatistics(STATISTIC_AUTO_CONN, TYPE_CONN_NET_WLAN, lastRecord, candidate, curScore);
                    }
                }
                return candidate;
            }
        }
        if ((this.mAutoSwitch && (this.mFeatureState ^ 1) != 0) || (lastRecord == null && candidate == null && this.mInitAutoConnect)) {
            this.mWifiNative.reconnect();
            this.mInitAutoConnect = false;
        }
        return candidate;
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
        for (String name : value.split(",")) {
            if (url.contains(name)) {
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
        List<String> updatedServers = new ArrayList();
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
        List<String> updatedServers = new ArrayList();
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
        List<String> updatedServers = new ArrayList();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        logD("updated Servers less than 2, using original servers!");
        return originalServers;
    }

    private int sendHttpProbe(URL url) {
        HttpURLConnection httpURLConnection = null;
        boolean isGenerate204 = false;
        int httpResponseCode = 599;
        try {
            String hostToResolve = url.getHost();
            String host = " ";
            logD("SHP: " + url + ", host= " + hostToResolve + ", mCurNetwork: " + this.mCurNetwork);
            if (!(TextUtils.isEmpty(hostToResolve) || this.mCurNetwork == null)) {
                if (hostToResolve.contains("generate_204")) {
                    isGenerate204 = true;
                }
                if (getCurrentState() == this.mDisconnectState || this.mCurNetwork == null) {
                    return 599;
                }
                InetAddress[] addresses = this.mCurNetwork.getAllByName(hostToResolve);
                InetAddress gateway = this.mWifiStateMachine.syncGetDhcpResults().gateway;
                if (addresses[0] != null) {
                    host = "/" + addresses[0].getHostAddress();
                    logD("SHP ht:" + host + ",gw:" + gateway);
                }
                if (addresses.length == 1 && gateway != null && gateway.toString().equals(host)) {
                    this.mResponseGotFromGateway = true;
                    logD("SHP fgw !!");
                    return 599;
                }
                this.mResponseGotFromGateway = false;
                if (getCurrentState() == this.mDisconnectState || this.mCurNetwork == null) {
                    return 599;
                }
                if (url == null) {
                    return 599;
                }
                httpURLConnection = (HttpURLConnection) this.mCurNetwork.openConnection(url);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setUseCaches(false);
                httpURLConnection.getInputStream();
                httpResponseCode = httpURLConnection.getResponseCode();
                String redirectUrl = httpURLConnection.getHeaderField("location");
                logD("SHP: " + url + ", code = " + httpResponseCode + ", conn = " + httpURLConnection.getHeaderField("Connection"));
                if (httpResponseCode == 200) {
                    if (isGenerate204) {
                        if (httpURLConnection.getContentLength() == 0 || (httpURLConnection.getHeaderField("Connection") != null && httpURLConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive"))) {
                            logD("SHP: empty and Keep-Alive 200 response interpreted as 204 response.");
                            httpResponseCode = HTTP_NORMAL_CODE;
                        }
                    } else if (httpURLConnection.getHeaderField("Connection") != null && httpURLConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive")) {
                        logD("SHP: !isGenerate204, Keep-Alive 200 response treated as 204.");
                        httpResponseCode = HTTP_NORMAL_CODE;
                    }
                }
                if (httpResponseCode >= HTTP_CAPTIVE_CODE_MID && httpResponseCode <= HTTP_CAPTIVE_CODE_END && redirectUrl != null && inSpecialUrlList(redirectUrl)) {
                    logD("SHP: response 302 with special redirect url: " + redirectUrl);
                    httpResponseCode = HTTP_NORMAL_CODE;
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (RuntimeException runtimeException) {
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + runtimeException);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Exception ee) {
            logD("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + ee);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return httpResponseCode;
    }

    private boolean sendParallelHttpProbes() {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicReference<Boolean> finalResult = new AtomicReference();
        List<String> publicHttpsServers = getPublicHttpsServers();
        Collections.shuffle(publicHttpsServers);
        List<String> intenalServers = getInternalServers();
        List<String> fallbackServers = getFallbackServers();
        Collections.shuffle(fallbackServers);
        try {
            URL url;
            URL url1 = new URL((String) publicHttpsServers.get(0));
            try {
                url = new URL((String) fallbackServers.get(0));
            } catch (MalformedURLException e) {
                logD("Bad validation URL.");
                return false;
            }
            try {
                URL url3 = new URL("http://" + ((String) intenalServers.get(this.mDetectInterCount % intenalServers.size())) + "/generate_204");
                AnonymousClass1ProbeThread httpProbe1 = new AnonymousClass1ProbeThread(url1, finalResult, latch);
                AnonymousClass1ProbeThread httpProbe2 = new AnonymousClass1ProbeThread(url, finalResult, latch);
                AnonymousClass1ProbeThread httpProbe3 = new AnonymousClass1ProbeThread(url3, finalResult, latch);
                httpProbe1.start();
                httpProbe2.start();
                httpProbe3.start();
                try {
                    latch.await(40000, TimeUnit.MILLISECONDS);
                    finalResult.compareAndSet(null, Boolean.valueOf(false));
                    return ((Boolean) finalResult.get()).booleanValue();
                } catch (InterruptedException e2) {
                    logD("Error: probe wait interrupted!");
                    return false;
                }
            } catch (MalformedURLException e3) {
                URL url2 = url;
                URL url4 = url1;
                logD("Bad validation URL.");
                return false;
            }
        } catch (MalformedURLException e4) {
            logD("Bad validation URL.");
            return false;
        }
    }

    private void detectInternet() {
        if (!this.mAutoSwitch || (this.mFeatureState ^ 1) != 0 || (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) != 0) {
            logD("DI, switch is off");
        } else if (!this.mDetectNet || this.mInternetDetecting) {
            logD("DI, no need check");
        } else if (this.mWifiStateMachine.isRoaming()) {
            logD("DI, ring");
        } else {
            this.mLastInternetResult = this.mInterResult;
            if (this.mInterThread != null) {
                this.mInterThread.post(new Runnable() {
                    public void run() {
                        String dectConfig = OppoWifiAssistantStateTraker.this.mLastConfigkey;
                        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                        oppoWifiAssistantStateTraker.mDetectInterCount = oppoWifiAssistantStateTraker.mDetectInterCount + 1;
                        OppoWifiAssistantStateTraker.this.mLastDetectInter = System.currentTimeMillis();
                        OppoWifiAssistantStateTraker.this.mInternetDetecting = true;
                        boolean probeResult = OppoWifiAssistantStateTraker.this.sendParallelHttpProbes();
                        OppoWifiAssistantStateTraker.this.mInternetDetecting = false;
                        if (dectConfig == null || (dectConfig.equals(OppoWifiAssistantStateTraker.this.mLastConfigkey) ^ 1) == 0) {
                            if (probeResult) {
                                OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                                if (!OppoWifiAssistantStateTraker.this.mInterResult) {
                                    OppoWifiAssistantStateTraker.this.sendMessageForNetChange(true);
                                }
                            } else if (!OppoWifiAssistantStateTraker.this.mInterResult || OppoWifiAssistantStateTraker.this.mLastPkgInfo == null || OppoWifiAssistantStateTraker.this.mLastPkgInfo.rssi < OppoWifiAssistantStateTraker.STATIC_LOW_RSSI_24) {
                                OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                            } else if (OppoWifiAssistantStateTraker.this.mInternetInvalidCount == 0) {
                                OppoWifiAssistantStateTraker.this.logD("DI, change to unvailable, detect again before set");
                                oppoWifiAssistantStateTraker = OppoWifiAssistantStateTraker.this;
                                oppoWifiAssistantStateTraker.mInternetInvalidCount = oppoWifiAssistantStateTraker.mInternetInvalidCount + 1;
                                long delayTime = System.currentTimeMillis() + 10000;
                                OppoWifiAssistantStateTraker.this.mAlarmManager.cancel(OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                                OppoWifiAssistantStateTraker.this.mAlarmManager.set(0, delayTime, OppoWifiAssistantStateTraker.this.mDetectInterIntent);
                            } else {
                                OppoWifiAssistantStateTraker.this.mInternetInvalidCount = 0;
                                OppoWifiAssistantStateTraker.this.sendMessageForNetChange(false);
                            }
                        }
                    }
                });
            }
        }
    }

    private void sendMessageForNetChange(boolean valid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_INTERNET_CHANGE, valid ? 1 : 0, 0, this.mLastConfigkey));
    }

    private boolean needToDetectTcpStatus() {
        if (this.mAutoSwitch && (this.mFeatureState ^ 1) == 0 && (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue() ^ 1) == 0) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            }
            NetworkInfo mni = this.mCM.getActiveNetworkInfo();
            if (mni != null && mni.getType() == 0) {
                logD("needToDetectInternet, data");
                return false;
            } else if (this.mDetectNet && !this.mToData) {
                return true;
            } else {
                logD("needToDetectInternet, no need to check");
                return false;
            }
        }
        logD("needToDetectInternet, switch is off");
        return false;
    }

    private boolean detectTcpStatus() {
        boolean isAvailable = false;
        if (this.mOppoTcpInfoMonitor != null) {
            this.mTcpLinkStatus = this.mOppoTcpInfoMonitor.getCurrentTcpLinkStatus();
        }
        logD("Before adjustment, mTcpLinkStatus = " + this.mTcpLinkStatus + " mTcpStatistics = " + this.mTcpStatistics);
        if (this.mTcpLinkStatus == 16) {
            if (this.mTcpStatistics < 0 || this.mTcpStatistics >= 1000) {
                this.mTcpStatistics = 1;
            } else {
                this.mTcpStatistics++;
            }
            if (!this.mInterResult && (this.mGotInternetResult & 2) == 2 && this.mTcpStatistics == 2) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus == 18) {
            if (this.mTcpStatistics < 1000 || this.mTcpStatistics >= TCP_STAT_POOR_COUNT) {
                this.mTcpStatistics = 1000;
            } else {
                this.mTcpStatistics++;
            }
            if (this.mTcpStatistics == 1005) {
                triggerInternetDetect(false);
            }
        } else if (this.mTcpLinkStatus == 2 || this.mTcpLinkStatus == 3) {
            if (this.mTcpStatistics > -1000 || this.mTcpStatistics <= -2000) {
                this.mTcpStatistics = -1000;
            } else {
                this.mTcpStatistics--;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == -1003) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus == 1) {
            if (this.mTcpStatistics > 0 || this.mTcpStatistics <= -1000) {
                this.mTcpStatistics = -1;
            } else {
                this.mTcpStatistics--;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == -3) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus != 0) {
            this.mTcpStatistics = 0;
        } else if (this.mTcpStatistics > 0) {
            this.mTcpStatistics--;
        } else if (this.mTcpStatistics < 0) {
            this.mTcpStatistics++;
        }
        logD("After adjustment, mTcpStatistics = " + this.mTcpStatistics);
        if (this.mTcpStatistics == 3 && ((this.mGotInternetResult & 4) == 4 || ((this.mGotInternetResult & 8) == 8 && (this.mGotInternetResult & 2) == 2))) {
            if ((this.mGotInternetResult & 1) == 1) {
                this.mTcpStatistics = 0;
            } else {
                isAvailable = true;
                this.mGotInternetResult |= 1;
                this.mGotInternetResult &= -3;
            }
        } else if (this.mTcpStatistics != -10 || ((this.mGotInternetResult & 4) == 4 && (this.mGotInternetResult & 1) != 1)) {
            if (this.mTcpStatistics == -1020 && ((this.mGotInternetResult & 8) == 8 || (!this.mCaptivePortal && this.mGotInternetResult < 4))) {
                if ((this.mGotInternetResult & 2) == 2) {
                    this.mTcpStatistics = 0;
                } else {
                    this.mGotInternetResult |= 2;
                    this.mGotInternetResult &= -2;
                }
            }
        } else if ((this.mGotInternetResult & 2) == 2) {
            this.mTcpStatistics = 0;
        } else {
            this.mGotInternetResult |= 2;
            this.mGotInternetResult &= -2;
        }
        if (this.mTcpInterval == OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL) {
            this.mTcpShortIntervalCount++;
        } else {
            this.mTcpShortIntervalCount = 0;
        }
        if (this.mTcpShortIntervalCount > 50 || this.mTcpStatistics >= 3 || this.mTcpStatistics == -10 || this.mTcpStatistics == -1020) {
            this.mTcpInterval = 5000;
        }
        logD("mGotInternetResult = " + this.mGotInternetResult + " isAvailable = " + isAvailable);
        return isAvailable;
    }

    private boolean tcpStatSetInternetResult(boolean available) {
        boolean setSuccess = false;
        logD("tcpStatSetInternetResult, available = " + available);
        WifiInfo tcpWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
        if (tcpWifiInfo == null || (this.mIsSoftAP && isThirdAppOperate())) {
            return false;
        }
        WifiConfiguration avaConf = getWifiConfig(tcpWifiInfo.getSSID(), tcpWifiInfo.getBSSID());
        if (avaConf != null && avaConf.networkId == this.mConnectedId) {
            if (this.mGotInternetResult < 4 || available != this.mInterResult) {
                if (this.mGotInternetResult == 0) {
                    updateRecordInternetStateAndTime(avaConf.configKey(false), available, true);
                } else {
                    updateRecordInternetStateAndTime(avaConf.configKey(false), available, false);
                }
            }
            this.mInterResult = available;
            setSuccess = true;
        }
        return setSuccess;
    }

    public void rmConfUpdateRecord(int netId) {
        WifiConfiguration rmconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (rmconfig == null) {
            logD("removeNetworkAvailable rmconfig == null");
            return;
        }
        if (this.mLastConfigkey != null && this.mLastConfigkey.equals(rmconfig.configKey(false)) && (getCurrentState() == this.mNetworkMonitorState || getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState)) {
            long rmNetworkTime = System.currentTimeMillis();
            long rmForWlan = rmNetworkTime - this.mAutoConnWlanTime;
            if (this.mAutoConnWlanTime > 0 && rmForWlan > 0 && rmForWlan < 180000) {
                logD("stc wwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_WLAN);
                this.mAutoConnWlanTime = 0;
            }
            long rmForData = rmNetworkTime - this.mAutoDataToWlanTime;
            if (!this.mToData && this.mAutoDataToWlanTime > 0 && rmForData > 0 && rmForData < 180000) {
                logD("stc dwb2");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_DATA);
                this.mAutoDataToWlanTime = 0;
            }
            if (!this.mToData) {
                logD("stc wdc2");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_REMOVE_NETWORK_FOR_DATA);
            }
        }
        updateRecordWifiConfig(rmconfig.configKey(false));
    }

    /* JADX WARNING: Missing block: B:21:0x0041, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rmOrupdateRecordStatus(String key, boolean remove) {
        logD("rmoRst key:" + key);
        if (key != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(key)) {
                    OppoWifiAssistantRecord record = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(key);
                    if (record == null) {
                    } else if (remove) {
                        this.mWifiNetworkRecord.remove(key);
                    } else {
                        record.mConnExp = true;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resetConnExp(String key) {
        logD("rtce key:" + key);
        if (key != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(key)) {
                    OppoWifiAssistantRecord reSetRecord = (OppoWifiAssistantRecord) this.mWifiNetworkRecord.get(key);
                    if (reSetRecord == null) {
                        return;
                    }
                    reSetRecord.mConnExp = false;
                }
            }
        }
    }

    public boolean isSoftAp(LinkProperties lp) {
        if (lp == null) {
            logD("LinkProperties is null, return");
            return false;
        }
        LinkProperties mLp = lp;
        String result = "";
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
        boolean isSoft;
        logD("mCurrentGateway : " + mCurrentGateway.toString());
        if (mCurrentGateway.toString().equals("/192.168.43.1") || mCurrentGateway.toString().equals("/192.168.49.1") || mCurrentGateway.toString().equals("/172.20.10.1")) {
            isSoft = true;
        } else {
            isSoft = false;
        }
        return isSoft;
    }

    private boolean isThirdAppOperate() {
        boolean z = true;
        if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP", Boolean.valueOf(true)).booleanValue()) {
            int operatorUid = getOppoManuConnectUid();
            logD("ita = " + operatorUid);
            if (operatorUid == 1000 || operatorUid == 1010) {
                z = false;
            }
            return z;
        }
        logD("rd close it.");
        return true;
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

    private void dismissDialog(int type) {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.dismiss();
            if (type == 2) {
                this.mManualDialogAutoDismissTime = System.currentTimeMillis();
            } else {
                this.mManualDialogAutoDismissTime = 0;
            }
        }
        if (this.mDataAlertDialog != null) {
            this.mDataAlertDialog.dismiss();
        }
    }

    private void showDialog(OppoWifiAssistantRecord lastRecord) {
        logD("sd muk:" + this.mUnavailableKey);
        if (this.mAlertDialog != null) {
            logD("repeated sd");
        } else if (lastRecord == null) {
            logD("record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            logD("record parameter maybe null");
        } else if (getCurrentState() != this.mConnectedState && getCurrentState() != this.mNetworkMonitorState) {
            logD("don not sd");
        } else if (this.mUnavailableKey == null || !this.mUnavailableKey.equals(lastRecord.mConfigkey)) {
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = lastRecord.mWifiConfiguration.SSID;
            Builder builder = new Builder(this.mContext, 201523207);
            String title = "";
            if (getIsOppoManuConnect()) {
                if (lastRecord.mNetworkValid) {
                    title = ssid + this.mContext.getText(17039585);
                } else {
                    title = ssid + this.mContext.getText(17041010);
                }
                title = title + this.mContext.getText(17041141);
                builder.setPositiveButton(17041101, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker.this.mAlertDialog != null) {
                            OppoWifiAssistantStateTraker.this.mAlertDialog.dismiss();
                            OppoWifiAssistantStateTraker.this.mManualDialogAutoDismissTime = System.currentTimeMillis();
                        }
                        OppoWifiAssistantStateTraker.this.resetOppoManuConnect();
                        OppoWifiAssistantStateTraker.this.mClickDialogSwitch = true;
                        OppoWifiAssistantStateTraker.this.detectNetworkAvailable(null, 0, -127, false);
                    }
                });
                builder.setNegativeButton(17041102, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker.this.mAlertDialog != null) {
                            OppoWifiAssistantStateTraker.this.mAlertDialog.dismiss();
                            OppoWifiAssistantStateTraker.this.mManualDialogAutoDismissTime = 0;
                        }
                    }
                });
            } else {
                title = (ssid + this.mContext.getText(17041010)) + this.mContext.getText(17039584);
                builder.setPositiveButton(17040137, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiAssistantStateTraker.this.mAlertDialog != null) {
                            OppoWifiAssistantStateTraker.this.mAlertDialog.dismiss();
                            OppoWifiAssistantStateTraker.this.mManualDialogAutoDismissTime = 0;
                        }
                    }
                });
            }
            builder.setTitle(title);
            this.mAlertDialog = builder.create();
            this.mAlertDialog.setCanceledOnTouchOutside(false);
            this.mAlertDialog.setCancelable(false);
            this.mAlertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    OppoWifiAssistantStateTraker.this.mAlertDialog = null;
                }
            });
            LayoutParams p = this.mAlertDialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            this.mAlertDialog.getWindow().setAttributes(p);
            this.mAlertDialog.getWindow().setType(2003);
            this.mAlertDialog.getWindow().addFlags(2);
            this.mAlertDialog.show();
            TextView msg = (TextView) this.mAlertDialog.findViewById(16908299);
            if (msg != null) {
                msg.setGravity(17);
            } else {
                logD("textview is null");
            }
        } else {
            logD("record is same");
        }
    }

    private void showDialogFordata(OppoWifiAssistantRecord lastRecord) {
        if (lastRecord == null) {
            logD("sdf record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            logD("sdf record parameter maybe null");
        } else if (getCurrentState() != this.mNetworkMonitorState) {
            logD("sdf state error");
        } else {
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = lastRecord.mWifiConfiguration.SSID;
            LinearLayout contentView = (LinearLayout) LayoutInflater.from(this.mContext).inflate(201917514, null);
            final CheckBox mAlertBox = (CheckBox) contentView.findViewById(201458846);
            TextView mMessage = (TextView) contentView.findViewById(201458845);
            mAlertBox.setChecked(false);
            if (this.mAlertDialog != null) {
                this.mAlertDialog.dismiss();
            }
            Builder builder = new Builder(this.mContext, 201523207);
            String title = "";
            if (lastRecord.mNetworkValid) {
                title = ssid + this.mContext.getText(17039585);
            } else {
                title = ssid + this.mContext.getText(17041010);
            }
            builder.setTitle(title + this.mContext.getText(17041142));
            mMessage.setText(this.mContext.getText(17041144));
            builder.setView(contentView);
            builder.setPositiveButton(17040137, new OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.NOT_REMIND_WIFI_ASSISTANT, mAlertBox.isChecked() ? 1 : 0);
                    if (OppoWifiAssistantStateTraker.this.mAlertDialog != null) {
                        OppoWifiAssistantStateTraker.this.mAlertDialog.dismiss();
                        OppoWifiAssistantStateTraker.this.mManualDialogAutoDismissTime = 0;
                    }
                }
            });
            this.mAlertDialog = builder.create();
            this.mAlertDialog.setCanceledOnTouchOutside(false);
            this.mAlertDialog.setCancelable(false);
            this.mAlertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    OppoWifiAssistantStateTraker.this.mAlertDialog = null;
                }
            });
            LayoutParams p = this.mAlertDialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            this.mAlertDialog.getWindow().setAttributes(p);
            this.mAlertDialog.getWindow().setType(2003);
            this.mAlertDialog.getWindow().addFlags(2);
            this.mAlertDialog.show();
        }
    }

    private void showDialogFordataNewtype(OppoWifiAssistantRecord lastRecord) {
        if (lastRecord == null) {
            logD("sdfn record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            logD("sdfn record parameter maybe null");
        } else if (getCurrentState() != this.mNetworkMonitorState) {
            logD("sdfn state error");
        } else {
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = lastRecord.mWifiConfiguration.SSID;
            if (this.mAlertDialog != null) {
                this.mAlertDialog.dismiss();
            }
            if (this.mDataAlertDialog != null) {
                this.mDataAlertDialog.dismiss();
            }
            ColorSystemUpdateDialog.Builder builder = new ColorSystemUpdateDialog.Builder(this.mContext, 201523207);
            String title = "";
            CharSequence[] items = new CharSequence[]{this.mContext.getText(17041143), this.mContext.getText(17040137)};
            if (lastRecord.mNetworkValid) {
                title = ssid + this.mContext.getText(17039585);
            } else {
                title = ssid + this.mContext.getText(17041010);
            }
            builder.setTitle(title + this.mContext.getText(17041142));
            builder.setMessage(this.mContext.getText(17041144));
            builder.setItems(items, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            OppoWifiAssistantStateTraker.this.logD("sdfn no remind");
                            System.putInt(OppoWifiAssistantStateTraker.this.mContext.getContentResolver(), OppoWifiAssistantStateTraker.NOT_REMIND_WIFI_ASSISTANT, 1);
                            if (OppoWifiAssistantStateTraker.this.mDataAlertDialog != null) {
                                OppoWifiAssistantStateTraker.this.mDataAlertDialog.dismiss();
                                return;
                            }
                            return;
                        case 1:
                            if (OppoWifiAssistantStateTraker.this.mDataAlertDialog != null) {
                                OppoWifiAssistantStateTraker.this.mDataAlertDialog.dismiss();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            });
            this.mDataAlertDialog = builder.create();
            this.mDataAlertDialog.setCanceledOnTouchOutside(false);
            this.mDataAlertDialog.setCancelable(false);
            this.mDataAlertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    OppoWifiAssistantStateTraker.this.mDataAlertDialog = null;
                }
            });
            LayoutParams p = this.mDataAlertDialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            this.mDataAlertDialog.getWindow().setAttributes(p);
            this.mDataAlertDialog.getWindow().setType(2003);
            this.mDataAlertDialog.getWindow().addFlags(2);
            this.mDataAlertDialog.show();
        }
    }

    private boolean hasCheckNoRemind() {
        boolean hasCheck = System.getInt(this.mContext.getContentResolver(), NOT_REMIND_WIFI_ASSISTANT, 0) == 1;
        logD("hasCheck = " + hasCheck);
        return hasCheck;
    }

    private boolean checkManualDialogTimer() {
        boolean checkTimer = false;
        if (this.mAutoSwithToData) {
            checkTimer = true;
        }
        if (this.mManualDialogAutoDismissTime <= 0) {
            return checkTimer;
        }
        long distanceTime = System.currentTimeMillis() - this.mManualDialogAutoDismissTime;
        logD("dtt: " + distanceTime);
        if (distanceTime > ((long) getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME", Integer.valueOf(30000)).intValue())) {
            return true;
        }
        return checkTimer;
    }

    private boolean matchKeymgmt(String validKey, String scanKey) {
        boolean match = false;
        if (validKey == null || scanKey == null) {
            return false;
        }
        if (validKey.equals(SECURITY_PSK) || validKey.equals(SECURITY_WPA2_PSK)) {
            if (scanKey.contains("WPA-PSK") || scanKey.contains("WPA2-PSK")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_EAP)) {
            if (scanKey.contains("EAP")) {
                match = true;
            }
        } else if (validKey.equals("IEEE8021X")) {
            if (scanKey.contains("IEEE8021X")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WAPI_PSK)) {
            if (scanKey.contains("WAPI-KEY") || scanKey.contains("WAPI-PSK")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WAPI_CERT)) {
            if (scanKey.contains("WAPI-CERT")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WEP)) {
            if (scanKey.contains(SECURITY_WEP)) {
                match = true;
            }
        } else if (!validKey.equals(SECURITY_NONE)) {
            logD("matchKeymgmt default");
        } else if (!(scanKey.contains("PSK") || (scanKey.contains("EAP") ^ 1) == 0 || (scanKey.contains(SECURITY_WEP) ^ 1) == 0 || (scanKey.contains("WAPI") ^ 1) == 0 || (scanKey.contains("IEEE8021X") ^ 1) == 0)) {
            match = true;
        }
        return match;
    }

    private String parseKeymgmt(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(190)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(191)) {
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

    private void sendLowQualityBroadcast(int score, boolean detect) {
        logD("slqb for ds:" + score + ", dt:" + detect);
        if (detect && this.mDisconnectState != null && this.mDisconnectState == getCurrentState()) {
            logD("already disconnected, ignore start data detect");
            return;
        }
        Intent qualityIntent = new Intent(WIFI_SCROE_CHANGE);
        qualityIntent.addFlags(67108864);
        qualityIntent.putExtra(EXTRA_ENALE_DATA, detect);
        qualityIntent.putExtra(EXTRA_SCORE, score);
        this.mContext.sendStickyBroadcastAsUser(qualityIntent, UserHandle.ALL);
    }

    private void sendNetworkStateBroadCast(String configkey, boolean valid) {
        Intent netIntent = new Intent(WIFI_NETWORK_CHANGE);
        netIntent.addFlags(67108864);
        netIntent.putExtra(EXTRA_WIFI_INVALID, valid ^ 1);
        logD("snb " + valid);
        this.mContext.sendStickyBroadcastAsUser(netIntent, UserHandle.ALL);
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

    private void sendVerifyBroadcast(String configKey) {
        Intent verifyIntent = new Intent(WIFI_ASSISTANT_VERIFY);
        verifyIntent.addFlags(67108864);
        verifyIntent.putExtra(EXTRA_VERIFY_CONFIG, configKey);
        logD("svb");
        this.mContext.sendStickyBroadcastAsUser(verifyIntent, UserHandle.ALL);
    }

    private void setAssistantStatistics(String eventId, String type) {
        setAssistantStatistics(eventId, type, null, null, -127);
    }

    private void setAssistantStatistics(String eventId, String type, OppoWifiAssistantRecord cw, OppoWifiAssistantRecord sw, int extra1) {
        if (this.mAutoSwitch) {
            HashMap<String, String> map = new HashMap();
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
        sbuf.append(info.mNetid).append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mConfigkey).append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mRssi).append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mScore).append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        sbuf.append(info.mNetworkValid).append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        for (int i = 0; i < info.mNetQualitys.length; i++) {
            sbuf.append(info.mNetQualitys[i]);
            if (i + 1 != info.mNetQualitys.length) {
                sbuf.append(",");
            }
        }
        return sbuf.toString();
    }

    private void logD(String log) {
        if (sDebug) {
            Log.d(TAG, "" + log);
        }
    }

    private void logE(String log) {
        Log.e(TAG, "" + log);
    }
}
