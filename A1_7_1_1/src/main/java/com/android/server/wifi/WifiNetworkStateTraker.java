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
import android.net.TcpInfoMonitor;
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
import android.os.Process;
import android.os.ServiceManager;
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
import com.android.server.wifi.scanner.ChannelHelper;
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

class WifiNetworkStateTraker extends StateMachine {
    /* renamed from: -android-net-wifi-SupplicantStateSwitchesValues */
    private static final /* synthetic */ int[] f4-android-net-wifi-SupplicantStateSwitchesValues = null;
    private static final String ACTION_DETECT_INTERNET = "adnroid.net.wifi.DETECT_INTER";
    private static final String ACTION_WIFI_NETWORK_AVAILABLE = "android.net.wifi.OPPO_WIFI_VALID";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_NOT_AVAILABLE = "android.net.wifi.OPPO_WIFI_INVALID";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final String ANT_STR = "ant=";
    private static final int AUTO_SWITCH_DATA_COUNT = 5;
    private static final long AUTO_SWITCH_DATA_DISBALE_TIME = 10800000;
    private static final int AUTO_SWITCH_DATA_THRESHOLD_COUNT = 5;
    private static final long AUTO_SWITCH_DATA_THRESHOLD_TIME = 1800000;
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
    private static final String DATA_SCORE_CHANGE = "android.net.wifi.OPPO_DATA_NET_CHANGE";
    private static final int DATA_VALID_SCORE = 50;
    private static final int DEFAULT_SCORE = 79;
    private static final String DEFAULT_SPECIAL_URL = "360.cn";
    private static final int DETAIL_CAPTIVE = 1;
    private static final int DETAIL_CONNECTED = 2;
    private static final int DETAIL_DISCONNECTED = 0;
    private static final int DETAIL_IDLE = -1;
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
    private static final int HTTP_UNAVAIL = 8;
    private static final int INTERNET_INTERVAL_SCREENOFF = 300000;
    private static final int INTERNET_INTERVAL_SCREENON = 300000;
    private static final int INTERNET_POLL = 5000;
    private static final int INTERNET_TO_DATA_INTERVAL = 120000;
    private static final String INT_STR = "int=";
    private static final int INVALID_INFO = -127;
    private static final int ITEM_NO_REMIND = 0;
    private static final int ITEM_REMIND_EVERYTIME = 1;
    private static final String KEY_CURRENT_WLAN = "key_current_wlan";
    private static final String KEY_SELECT_WLAN = "key_select_wlan";
    private static final String LST_STR = "lst=";
    private static final String MS_STR = "ms=";
    private static final int NETQUALITY_HISTORY_COUNTS = 4;
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
    private static final String NOT_REMIND_WIFI_ASSISTANT = "not_remind_wifi_assistant";
    private static final String NQL_STR = "nql=";
    private static final String NV_STR = "nv=";
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
    private static final int STATIC_BAD_LINK_SCORE_THRESHOLD = 30;
    private static final int STATIC_BAD_RSSI_24 = -83;
    private static final int STATIC_BAD_RSSI_5 = -83;
    private static final int STATIC_BAD_RSSI_SCORE_THRESHOLD = 15;
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
    private static final int STATIC_LOW_LINK_SCORE_THRESHOLD = 25;
    private static final int STATIC_LOW_RSSI_24 = -77;
    private static final int STATIC_LOW_RSSI_5 = -77;
    private static final int STATIC_LOW_RSSI_SCORE_THRESHOLD = 10;
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
    private static final int TCP_UNAVAIL = 2;
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
    private static final int VALID_SCORE_THRESHOLD = 20;
    private static final String WIFI_ASSISTANT = "wifi_assistant";
    private static final String WIFI_ASSISTANT_FILE = "/data/misc/wifi/wifi_assistant";
    private static final String WIFI_ASSISTANT_VERIFY = "android.net.wifi.OPPO_VERIFY_WIFI";
    private static final String WIFI_AUTO_CHANGE_NETWORK = "wifi_auto_change_network";
    private static final String WIFI_AVAILABLE_FILE = "/data/misc/wifi/network_available";
    private static final String WIFI_NETWORK_CHANGE = "android.net.wifi.WIFI_NETWORK_INVALID";
    private static final String WIFI_SCROE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final String WIFI_TO_DATA = "android.net.wifi.WIFI_TO_DATA";
    private static final String mInterfaceName = "wlan0";
    private int AUTO_CONN_ERR_THRESHOLD = 180000;
    private int AVAILABLE_STAT_COUNT = 3;
    private int DATA_NETWORK_VALID = 50;
    private boolean DBG = false;
    private int DETECT_DELAY_TIME = STATIC_TRAFFIC_SAMPL_INTERVAL_INVALID;
    private int DIFF_RSSI_THRESHOLD = 5;
    private int DISABLE_INTERFACE = -1;
    private double EXP_COEFFICIENT_MONITOR = STATIC_LOW_LINK_LOSS_THRESHOLD;
    private int INTERNET_INTERVAL_DELTA = 60000;
    private int INTERNET_STANDOFF_TIME = 10000;
    private int PER_ROAM_THRESHOLD = -83;
    private int POOR_STAT_COUNT = 5;
    private int PORTAL_STAT_COUNT = 20;
    private int SCORE_NETWORK_VALID = 20;
    private int TCP_SAMPLE_INTERVAL_LONG = 5000;
    private int TCP_SAMPLE_INTERVAL_SHORT = STATIC_TRAFFIC_SAMPL_INTERVAL_INVALID;
    private int TCP_SHORT_INTERVAL_LIMIT = 50;
    private int TRAFFIC_DIFF_BURST = 20;
    private int TRAFFIC_DIFF_LOWEST = 1;
    private int TRAFFIC_LOW_COUNT = 2;
    private int TRAFFIC_SAMPL_INTERVAL_VALID = 5000;
    private int UNAVAILABLE_STAT_COUNT = 10;
    private int WLAN_INVALID_THRESHOLD = 40;
    private int WLAN_NETWORK_INVALID = 10;
    private String broadInfo;
    private boolean captivePortal = false;
    private long dRxPkts;
    private long dTxPkts;
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
    private BroadcastReceiver mBroadcastReceiver;
    private ConnectivityManager mCM;
    private String mCandidateKey = "";
    private boolean mChangeNetwork = false;
    private int mChangeRssi = 0;
    private int mChangeScore = 0;
    private boolean mClickDialogSwitch = false;
    private final State mCompletedState = new CompletedState();
    private int mConnFail;
    private int mConnectId = -1;
    private String mConnectKey;
    private String mConnectSSID = null;
    private long mConnectTime;
    private int mConnectedId = -1;
    private final State mConnectedState = new ConnectedState();
    private int mConnectingId = -1;
    private String mConnectingkey;
    private final Context mContext;
    private Network mCurNetwork;
    private VolumeWeightedEMA mCurrentLoss;
    private ColorSystemUpdateDialog mDataAlertDialog = null;
    private boolean mDataAutoSwitch = true;
    private int mDataScore = 10;
    private boolean mDataState = false;
    private DataStateObserver mDataStateObserver = null;
    private final State mDefaultState = new DefaultState();
    private int mDetectInterCount = 0;
    private PendingIntent mDetectInterIntent;
    private boolean mDetectNet = false;
    private final State mDisconnectState = new DisConnectedState();
    private boolean mFeatureState = true;
    private CharSequence mGoodAvailableAP;
    private int mGotInternetResult = 0;
    private Handler mHandler;
    private final State mHandshakeState = new HandshakeState();
    private int mIndex = 0;
    private boolean mInitAutoConnect = true;
    private final State mInitState = new InitState();
    private boolean mInterChangeToInvalid = false;
    private int mInterInteval = WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME;
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
    private int[] mNetQulityArray = new int[]{DEFAULT_SCORE, DEFAULT_SCORE, DEFAULT_SCORE, DEFAULT_SCORE};
    private int mNetQulityGoodCount = 0;
    private final State mNetworkMonitorState = new NetworkMonitorState();
    private String mNewBssid = " ";
    private String mNewSsid = " ";
    private String mOldBssid = " ";
    private String mOldSsid = " ";
    private int mOldTcpStatus = 0;
    private int mOperateUid = 1000;
    private String[] mPublicServers = new String[]{"https://m.baidu.com", "http://info.3g.qq.com", "https://sina.cn", "https://m.sohu.com"};
    private boolean mResponseGotFromGateway = false;
    private int mRoamdetectCount = 0;
    private int mRssiFetchToken = 0;
    private int mRxPktsLowCount = 0;
    private boolean mScreenOn = true;
    private List<WifiNetworkRecord> mSortNetworkRecord = new ArrayList();
    private List<WifiConfiguration> mSortWifiConfig = new ArrayList();
    private SupplicantStateTracker mSupplicantTracker;
    private TcpInfoMonitor mTcpInfoMonitor = null;
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
    private final State mVerifyInternetState = new VerifyInternetState();
    private WifiAssistantHelper mWah;
    private WifiConfigManager mWifiConfigManager;
    private WifiConfigStore mWifiConfigStore;
    private WifiNative mWifiNative;
    private HashMap<String, WifiNetworkRecord> mWifiNetworkRecord = new HashMap();
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private WifiSmartSwitcher mWifiSmartSwitcher;
    private int mWifiState = 1;
    private WifiStateMachine mWifiStateMachine;
    private boolean mWifiStateMachineConnected = false;
    private final DelayedDiskWrite mWriter;
    private AsyncChannel mWsmChannel = new AsyncChannel();

    /* renamed from: com.android.server.wifi.WifiNetworkStateTraker$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private volatile Boolean mResult;
        private final URL mUrl;
        final /* synthetic */ AtomicReference val$finalResult;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(URL url, AtomicReference val$finalResult, CountDownLatch val$latch) {
            this.val$finalResult = val$finalResult;
            this.val$latch = val$latch;
            this.mUrl = url;
        }

        public Boolean getResult() {
            return this.mResult;
        }

        public void run() {
            int respCode = WifiNetworkStateTraker.this.sendHttpProbe(this.mUrl);
            if (respCode >= ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS && respCode <= 399) {
                boolean z;
                if (respCode == 204) {
                    z = true;
                } else {
                    z = false;
                }
                this.mResult = Boolean.valueOf(z);
                WifiNetworkStateTraker.this.Logd("SPHP: decided result: " + this.mResult + ", from url: " + this.mUrl);
                this.val$finalResult.compareAndSet(null, this.mResult);
                this.val$finalResult.compareAndSet(Boolean.valueOf(false), this.mResult);
                this.val$latch.countDown();
                if (respCode != 204) {
                    try {
                        AnonymousClass1ProbeThread.sleep(2000);
                    } catch (InterruptedException e) {
                        WifiNetworkStateTraker.this.Loge("Probe sleep interrupted!");
                    }
                    WifiNetworkStateTraker.this.Logd("Probe sleep finished!");
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
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
            WifiNetworkStateTraker.this.mGotInternetResult = 0;
        }

        public boolean processMessage(Message msg) {
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            WifiConfiguration config;
            switch (msg.what) {
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiNetworkStateTraker.this.mRoamdetectCount = 0;
                    WifiNetworkStateTraker.this.mLastBssid = (String) msg.obj;
                    if (WifiNetworkStateTraker.this.mLastNetId != msg.arg1) {
                        WifiNetworkStateTraker.this.mLastNetId = msg.arg1;
                        config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mLastNetId);
                        WifiNetworkStateTraker.this.mLastConfigkey = config != null ? config.configKey(false) : null;
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    WifiNetworkStateTraker.this.mLastBssid = (String) msg.obj;
                    WifiNetworkStateTraker.this.mLastNetId = msg.arg1;
                    int completestate = msg.arg2;
                    WifiNetworkStateTraker.this.Logd("cptst = " + completestate + ",msg.arg1= " + msg.arg1);
                    config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mLastNetId);
                    if (config != null) {
                        WifiNetworkStateTraker.this.mLastConfigkey = config.configKey(false);
                        WifiNetworkStateTraker.this.Logd("cptst = " + completestate + ",mLastConfigkey= " + WifiNetworkStateTraker.this.mLastConfigkey + ",mLastNetId= " + WifiNetworkStateTraker.this.mLastNetId + ",mConnectSSID= " + WifiNetworkStateTraker.this.mConnectSSID);
                        if (completestate != 0) {
                            if (completestate != 1) {
                                if (completestate == 2) {
                                    WifiNetworkStateTraker.this.mConnectSSID = config.SSID;
                                    WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mConnectedState);
                                    break;
                                }
                            }
                            WifiNetworkStateTraker.this.mConnectSSID = config.SSID;
                            WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mVerifyInternetState);
                            break;
                        }
                        if (WifiNetworkStateTraker.this.mUnavailableKey != null && WifiNetworkStateTraker.this.mUnavailableKey.equals(WifiNetworkStateTraker.this.mLastConfigkey)) {
                            WifiNetworkStateTraker.this.dismissDialog(0);
                        }
                        WifiNetworkStateTraker.this.updateRecordDisableState(WifiNetworkStateTraker.this.mLastConfigkey);
                        WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mDisconnectState);
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
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
            WifiNetworkStateTraker.this.mConnectTime = System.currentTimeMillis();
            WifiNetworkStateTraker.this.mConnectedId = WifiNetworkStateTraker.this.mLastNetId;
            WifiNetworkStateTraker.this.Logd("ConnectedState mLastConfigkey= " + WifiNetworkStateTraker.this.mLastConfigkey + ", mConnectedId= " + WifiNetworkStateTraker.this.mConnectedId);
            WifiNetworkStateTraker.this.updateRecordConCount(WifiNetworkStateTraker.this.mLastConfigkey);
            if (WifiNetworkStateTraker.this.mCandidateKey.equals(WifiNetworkStateTraker.this.mLastConfigkey)) {
                WifiNetworkStateTraker.this.mAutoConnWlanTime = WifiNetworkStateTraker.this.mConnectTime;
                WifiNetworkStateTraker.this.mAutoConnDataTime = 0;
                WifiNetworkStateTraker.this.mAutoDataToWlanTime = 0;
                WifiNetworkStateTraker.this.Logd("stc wwa");
                WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_AUTO_CONN, WifiNetworkStateTraker.TYPE_WLAN_TO_WLAN, (WifiNetworkRecord) WifiNetworkStateTraker.this.mWifiNetworkRecord.get(WifiNetworkStateTraker.this.mLastConfigkey), null, WifiNetworkStateTraker.INVALID_INFO);
                WifiNetworkStateTraker.this.mCandidateKey = "";
            }
            WifiNetworkStateTraker.this.mTcpStatistics = 0;
            WifiNetworkStateTraker.this.mTcpInterval = WifiNetworkStateTraker.this.TCP_SAMPLE_INTERVAL_SHORT;
            WifiNetworkStateTraker.this.mTcpInfoMonitor.resetTcpLinkStatus();
            WifiNetworkStateTraker.this.removeMessages(WifiNetworkStateTraker.CMD_TCP_MONITOR);
            WifiNetworkStateTraker.this.sendMessageDelayed(WifiNetworkStateTraker.this.obtainMessage(WifiNetworkStateTraker.CMD_TCP_MONITOR), (long) WifiNetworkStateTraker.this.mTcpInterval);
        }

        public boolean processMessage(Message msg) {
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiNetworkStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    String configKey;
                    WifiNetworkStateTraker.this.mLastBssid = (String) msg.obj;
                    WifiNetworkStateTraker.this.mLastNetId = msg.arg1;
                    int connectedstate = msg.arg2;
                    WifiConfiguration config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mLastNetId);
                    WifiNetworkStateTraker wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    wifiNetworkStateTraker.mLastConfigkey = configKey;
                    WifiNetworkStateTraker.this.Logd("cctst= " + connectedstate);
                    if (connectedstate == 0) {
                        if (WifiNetworkStateTraker.this.mUnavailableKey != null && WifiNetworkStateTraker.this.mUnavailableKey.equals(WifiNetworkStateTraker.this.mLastConfigkey)) {
                            WifiNetworkStateTraker.this.dismissDialog(0);
                        }
                        WifiNetworkStateTraker.this.updateRecordDisableState(WifiNetworkStateTraker.this.mLastConfigkey);
                        WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mDisconnectState);
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mNetworkMonitorState);
                    break;
                case WifiNetworkStateTraker.CMD_TCP_MONITOR /*200711*/:
                    if (WifiNetworkStateTraker.this.needToDetectTcpStatus()) {
                        boolean -wrap1 = WifiNetworkStateTraker.this.detectTcpStatus();
                    }
                    WifiNetworkStateTraker.this.sendMessageDelayed(WifiNetworkStateTraker.this.obtainMessage(WifiNetworkStateTraker.CMD_TCP_MONITOR), (long) WifiNetworkStateTraker.this.mTcpInterval);
                    break;
                case WifiNetworkStateTraker.EVENT_SCREEN_ON /*200712*/:
                    WifiNetworkStateTraker.this.mIsScreenOn = true;
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
            if (WifiNetworkStateTraker.this.mTelephonyManager != null) {
                WifiNetworkStateTraker.this.mDataState = WifiNetworkStateTraker.this.mTelephonyManager.getDataEnabled();
                if (!WifiNetworkStateTraker.this.mDataState) {
                    long disableData = System.currentTimeMillis() - WifiNetworkStateTraker.this.mAutoConnDataTime;
                    if (WifiNetworkStateTraker.this.mChangeNetwork && WifiNetworkStateTraker.this.mToData) {
                        if (WifiNetworkStateTraker.this.mAutoConnDataTime > 0 && disableData > 0 && disableData < ((long) WifiNetworkStateTraker.this.AUTO_CONN_ERR_THRESHOLD)) {
                            WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_MANUAL_LIMIT, WifiNetworkStateTraker.TYPE_DIABLE_DATA);
                            WifiNetworkStateTraker.this.mAutoConnDataTime = 0;
                        }
                        WifiNetworkStateTraker.this.Logd("stc dwc2");
                        WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_MANUAL_OPERATE, WifiNetworkStateTraker.TYPE_DIABLE_DATA);
                    }
                }
            }
        }
    }

    class DefaultState extends State {
        Intent intent;

        DefaultState() {
        }

        public void enter() {
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
        }

        public boolean processMessage(Message msg) {
            String str = null;
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            WifiConfiguration config;
            WifiNetworkStateTraker wifiNetworkStateTraker;
            String configKey;
            WifiNetworkStateTraker wifiNetworkStateTraker2;
            switch (msg.what) {
                case 131126:
                case 151553:
                    WifiNetworkStateTraker.this.mConnectId = Integer.valueOf(msg.arg1).intValue();
                    WifiNetworkStateTraker.this.mIsNewConfig = Integer.valueOf(msg.arg2).intValue();
                    config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mConnectId);
                    wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    wifiNetworkStateTraker.mConnectKey = configKey;
                    wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    wifiNetworkStateTraker2.mConnectSSID = str;
                    WifiNetworkStateTraker.this.Logd("mConnectId= " + WifiNetworkStateTraker.this.mConnectId + ", minc= " + WifiNetworkStateTraker.this.mIsNewConfig);
                    WifiNetworkStateTraker.this.addOrUpdateRecord(WifiNetworkStateTraker.this.mConnectKey);
                    break;
                case 131211:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.WRONG_KEY_EVENT /*147648*/:
                    WifiConfiguration conFail = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mConnectingId);
                    wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
                    if (conFail != null) {
                        str = conFail.configKey(false);
                    }
                    wifiNetworkStateTraker2.mConnectingkey = str;
                    WifiNetworkStateTraker.this.updateRecordConnectFail(WifiNetworkStateTraker.this.mConnectingkey);
                    if (WifiNetworkStateTraker.this.mManualConnect) {
                        WifiNetworkStateTraker.this.mConnectId = -1;
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiNetworkStateTraker.this.mRoamdetectCount = 0;
                    WifiNetworkStateTraker.this.mLastNetId = msg.arg1;
                    WifiNetworkStateTraker.this.mLastBssid = (String) msg.obj;
                    config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mLastNetId);
                    wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
                    if (config != null) {
                        str = config.configKey(false);
                    }
                    wifiNetworkStateTraker2.mLastConfigkey = str;
                    WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mCompletedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiNetworkStateTraker.this.transitionSupplicantState(msg.obj);
                    break;
                case WifiNetworkStateTraker.EVENT_NETWORK_STATE_CHANGE /*200705*/:
                    WifiNetworkStateTraker.this.mLastNetId = msg.arg1;
                    WifiNetworkStateTraker.this.mLastBssid = (String) msg.obj;
                    config = WifiNetworkStateTraker.this.mWifiConfigManager.getWifiConfiguration(WifiNetworkStateTraker.this.mLastNetId);
                    wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                    if (config != null) {
                        configKey = config.configKey(false);
                    } else {
                        configKey = null;
                    }
                    wifiNetworkStateTraker.mLastConfigkey = configKey;
                    if (msg.arg2 != 2) {
                        if (msg.arg2 == 0) {
                            if (WifiNetworkStateTraker.this.mUnavailableKey != null && WifiNetworkStateTraker.this.mUnavailableKey.equals(WifiNetworkStateTraker.this.mLastConfigkey)) {
                                WifiNetworkStateTraker.this.dismissDialog(0);
                            }
                            WifiNetworkStateTraker.this.updateRecordDisableState(WifiNetworkStateTraker.this.mLastConfigkey);
                            WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mDisconnectState);
                            break;
                        }
                    }
                    wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
                    if (config != null) {
                        str = config.SSID;
                    }
                    wifiNetworkStateTraker2.mConnectSSID = str;
                    WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mConnectedState);
                    break;
                    break;
                case WifiNetworkStateTraker.EVENT_WIFI_STATE_CHANGE /*200706*/:
                    WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mInitState);
                    break;
                case WifiNetworkStateTraker.EVENT_SCREEN_ON /*200712*/:
                    WifiNetworkStateTraker.this.mIsScreenOn = true;
                    break;
                case WifiNetworkStateTraker.EVENT_SCREEN_OFF /*200713*/:
                    WifiNetworkStateTraker.this.mIsScreenOn = false;
                    break;
            }
            return true;
        }
    }

    class DisConnectedState extends State {
        DisConnectedState() {
        }

        public void enter() {
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
            WifiNetworkStateTraker.this.Logd("mLastNetId = " + WifiNetworkStateTraker.this.mLastNetId + ", mConnectedId = " + WifiNetworkStateTraker.this.mConnectedId + ", mLastConfigkey = " + WifiNetworkStateTraker.this.mLastConfigkey);
            WifiNetworkStateTraker.this.mAlarmManager.cancel(WifiNetworkStateTraker.this.mDetectInterIntent);
            if (WifiNetworkStateTraker.this.mLastNetId == WifiNetworkStateTraker.this.mConnectedId && WifiNetworkStateTraker.this.mLastNetId != -1 && WifiNetworkStateTraker.this.mConnectingId == WifiNetworkStateTraker.this.mConnectedId) {
                WifiNetworkStateTraker.this.updateRecordUseTime(WifiNetworkStateTraker.this.mLastConfigkey);
            }
            WifiNetworkStateTraker.this.mLastNetId = -1;
            WifiNetworkStateTraker.this.mLastBssid = " ";
            WifiNetworkStateTraker.this.mUnavailableKey = " ";
            WifiNetworkStateTraker.this.mAutoSwithToData = false;
            WifiNetworkStateTraker.this.mInterResult = false;
            WifiNetworkStateTraker.this.mInternetInvalidCount = 0;
            WifiNetworkStateTraker.this.mInterChangeToInvalid = false;
            WifiNetworkStateTraker.this.mLastPkgInfo = null;
            WifiNetworkStateTraker.this.mGotInternetResult = 0;
            WifiNetworkStateTraker.this.mLastDetectInter = 0;
            WifiNetworkStateTraker.this.mTrigScanCount = 0;
            WifiNetworkStateTraker.this.dTxPkts = 0;
            WifiNetworkStateTraker.this.dRxPkts = 0;
            WifiNetworkStateTraker.this.mIsSoftAP = false;
            WifiNetworkStateTraker.this.captivePortal = false;
            WifiNetworkStateTraker.this.mResponseGotFromGateway = false;
            WifiNetworkStateTraker.this.mWifiStateMachineConnected = false;
            WifiNetworkStateTraker.this.resetAutoSwitchDataDetect();
            if (WifiNetworkStateTraker.this.mAutoSwitch && WifiNetworkStateTraker.this.mFeatureState && WifiNetworkStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                WifiNetworkStateTraker.this.sendNetworkStateBroadCast(WifiNetworkStateTraker.this.mLastConfigkey, true);
                WifiNetworkStateTraker.this.sendWifiToDataBroadcast(false, WifiNetworkStateTraker.DEFAULT_SCORE);
            }
        }

        public boolean processMessage(Message msg) {
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiNetworkStateTraker.EVENT_SCREEN_ON /*200712*/:
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
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
        }

        public boolean processMessage(Message msg) {
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiNetworkStateTraker.EVENT_SCREEN_ON /*200712*/:
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
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName());
            }
            WifiNetworkStateTraker.this.mWifiState = WifiNetworkStateTraker.this.mWifiStateMachine.syncGetWifiState();
        }

        public boolean processMessage(Message msg) {
            if (WifiNetworkStateTraker.this.DBG) {
                Log.d(WifiNetworkStateTraker.TAG, getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiNetworkStateTraker.EVENT_WIFI_STATE_CHANGE /*200706*/:
                    WifiNetworkStateTraker.this.Logd("it is Initstate, do not handle!");
                    break;
                case WifiNetworkStateTraker.EVENT_SCREEN_ON /*200712*/:
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
                case WifiNetworkStateTraker.EVENT_ADD_UPDATE_NETWORK /*200714*/:
                    WifiInfo CurWifiInfo = WifiNetworkStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                    Intent netMintent = msg.obj;
                    if (!(netMintent == null || (WifiNetworkStateTraker.this.mIsSoftAP && WifiNetworkStateTraker.this.isThirdAppOperate()))) {
                        String netStateSsid = netMintent.getStringExtra(WifiNetworkStateTraker.EXTRA_WIFI_SSID);
                        WifiNetworkStateTraker.this.Logd("nss: " + netStateSsid + ",info: " + CurWifiInfo);
                        if (!(CurWifiInfo == null || netStateSsid == null || !CurWifiInfo.getSSID().equals(netStateSsid))) {
                            boolean netValid = netMintent.getBooleanExtra(WifiNetworkStateTraker.EXTRA_NETWORK_STATE, false);
                            WifiNetworkStateTraker.this.Logd("nst: " + netValid);
                            WifiConfiguration netConf = WifiNetworkStateTraker.this.getWifiConfig(netStateSsid, CurWifiInfo.getBSSID());
                            if (netConf == null || netConf.networkId != WifiNetworkStateTraker.this.mConnectedId) {
                                if (WifiNetworkStateTraker.this.getCurrentState() == WifiNetworkStateTraker.this.mConnectedState || WifiNetworkStateTraker.this.getCurrentState() == WifiNetworkStateTraker.this.mVerifyInternetState) {
                                    if (netConf != null) {
                                        WifiNetworkRecord expInfo = new WifiNetworkRecord();
                                        expInfo.mNetid = netConf.networkId;
                                        expInfo.mConfigkey = netConf.configKey();
                                        WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_AUTO_CONN, WifiNetworkStateTraker.TYPE_MONITOR_EXP, expInfo, null, WifiNetworkStateTraker.this.mConnectedId);
                                        break;
                                    }
                                    WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_AUTO_CONN, WifiNetworkStateTraker.TYPE_MONITOR_EXP);
                                    break;
                                }
                            }
                            if (WifiNetworkStateTraker.this.mGotInternetResult < 4 || netValid != WifiNetworkStateTraker.this.mInterResult) {
                                WifiNetworkStateTraker.this.updateRecordInternetStateAndTime(netConf.configKey(false), netValid, true);
                            }
                            WifiNetworkStateTraker.this.mInterResult = netValid;
                            WifiNetworkStateTraker wifiNetworkStateTraker;
                            if (!netValid) {
                                wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                                wifiNetworkStateTraker.mGotInternetResult = wifiNetworkStateTraker.mGotInternetResult | 8;
                                wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                                wifiNetworkStateTraker.mGotInternetResult = wifiNetworkStateTraker.mGotInternetResult & -5;
                                break;
                            }
                            wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                            wifiNetworkStateTraker.mGotInternetResult = wifiNetworkStateTraker.mGotInternetResult | 4;
                            wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                            wifiNetworkStateTraker.mGotInternetResult = wifiNetworkStateTraker.mGotInternetResult & -9;
                            break;
                        }
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_CONNECT_NETWORK /*200715*/:
                    Intent conIntent = msg.obj;
                    WifiInfo curWifiInfo = WifiNetworkStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                    String curSsid = conIntent.getStringExtra(WifiNetworkStateTraker.EXTRA_WIFI_SSID);
                    if (curWifiInfo != null && curSsid != null && curWifiInfo.getSSID().equals(curSsid)) {
                        WifiNetworkStateTraker.this.Logd("cnsid:" + curSsid + ",ifsid:" + curWifiInfo.getSSID() + ",cisid:" + WifiNetworkStateTraker.this.mConnectSSID);
                        if (curSsid.equals(WifiNetworkStateTraker.this.mConnectSSID)) {
                            WifiNetworkStateTraker.this.mManualConnect = conIntent.getBooleanExtra(WifiNetworkStateTraker.EXTRA_WIFI_MANUAL, true);
                            LinkProperties curLink = (LinkProperties) conIntent.getExtra(WifiNetworkStateTraker.EXTRA_WIFI_LINK);
                            WifiNetworkStateTraker.this.mCurNetwork = (Network) conIntent.getExtra(WifiNetworkStateTraker.EXTRA_WIFI_NETWORK);
                            WifiNetworkStateTraker.this.mIsSoftAP = WifiNetworkStateTraker.this.isSoftAp(curLink);
                            break;
                        }
                    }
                    return;
                    break;
                case WifiNetworkStateTraker.EVENT_INTERNET_CHANGE /*200722*/:
                    boolean inter_result = msg.arg1 == 1;
                    String inter_conf = msg.obj;
                    WifiNetworkStateTraker.this.Logd("ir=" + inter_result + "ic=" + inter_conf);
                    if (inter_result) {
                        WifiNetworkStateTraker.this.mInterChangeToInvalid = false;
                        if (WifiNetworkStateTraker.this.mLastConfigkey != null && WifiNetworkStateTraker.this.mLastConfigkey.equals(WifiNetworkStateTraker.this.mUnavailableKey)) {
                            WifiNetworkStateTraker.this.dismissDialog(2);
                        }
                    } else if (!WifiNetworkStateTraker.this.mManualConnect) {
                        WifiNetworkStateTraker.this.mInterChangeToInvalid = true;
                    }
                    WifiNetworkStateTraker.this.updateRecordInternetStateAndTime(inter_conf, inter_result, false);
                    if (WifiNetworkStateTraker.this.mLastPkgInfo != null) {
                        WifiNetworkStateTraker.this.updateRecordLinkQuality(WifiNetworkStateTraker.this.mLastPkgInfo);
                    }
                    if (!(!inter_result || WifiNetworkStateTraker.this.mChangeNetwork || WifiNetworkStateTraker.this.mWifiStateMachine == null)) {
                        WifiNetworkStateTraker.this.mWifiStateMachine.setNetworkStatus(true);
                    }
                    WifiNetworkStateTraker.this.mInterResult = inter_result;
                    break;
                case WifiNetworkStateTraker.EVENT_CAPTIVE_PORTAL /*200723*/:
                    String captive_conf = msg.obj;
                    WifiNetworkStateTraker.this.Logd("nc=" + (Integer.valueOf(msg.arg1).intValue() == 1) + ",cc=" + captive_conf);
                    WifiNetworkStateTraker.this.updateRecordCaptiveState(captive_conf, true, true);
                    break;
                case WifiNetworkStateTraker.EVENT_SCAN_TIMEOUT /*200724*/:
                    WifiNetworkStateTraker.this.mTriggerScan = false;
                    break;
            }
        }
    }

    class NetworkMonitorState extends State {
        NetworkMonitorState() {
        }

        public void enter() {
            if (WifiNetworkStateTraker.this.DBG) {
                WifiNetworkStateTraker.this.Logd(getName() + ",mLastConfigkey=" + WifiNetworkStateTraker.this.mLastConfigkey);
            }
            WifiNetworkStateTraker.this.mLastTrigDataTime = System.currentTimeMillis();
            WifiNetworkStateTraker.this.mLastTxBad = 0;
            WifiNetworkStateTraker.this.mLastTxGood = 0;
            WifiNetworkStateTraker.this.mLastTxPkts = TrafficStats.getTcpTxPackets(WifiNetworkStateTraker.mInterfaceName);
            WifiNetworkStateTraker.this.mLastRxPkts = TrafficStats.getTcpRxPackets(WifiNetworkStateTraker.mInterfaceName);
            WifiNetworkStateTraker.this.mRxPktsLowCount = 0;
            WifiNetworkStateTraker.this.dTxPkts = 0;
            WifiNetworkStateTraker.this.dRxPkts = 0;
            WifiNetworkStateTraker.this.mLinkInterval = WifiNetworkStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SAMPL_INTERVAL", Integer.valueOf(5000)).intValue();
            WifiNetworkStateTraker.this.mTrafficInteval = WifiNetworkStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_INVALID_TRAFFIC_SAMPL_INTERVAL", Integer.valueOf(WifiNetworkStateTraker.STATIC_TRAFFIC_SAMPL_INTERVAL_INVALID)).intValue();
            WifiNetworkStateTraker.this.mTcpInterval = WifiNetworkStateTraker.this.TCP_SAMPLE_INTERVAL_LONG;
            WifiNetworkStateTraker.this.WLAN_INVALID_THRESHOLD = WifiNetworkStateTraker.this.getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", Integer.valueOf(40)).intValue();
            WifiNetworkStateTraker.this.mRoamdetectCount = 0;
            WifiNetworkStateTraker.this.mTriggerData = true;
            WifiNetworkStateTraker.this.mTriggerInter = false;
            WifiNetworkStateTraker.this.mChangeNetwork = false;
            WifiNetworkStateTraker.this.mClickDialogSwitch = false;
            WifiNetworkStateTraker.this.mDetectInterCount = 0;
            WifiNetworkStateTraker.this.mLastInternetResult = WifiNetworkStateTraker.this.mInterResult;
            WifiNetworkStateTraker.this.mLinkDetectTimes = 0;
            WifiNetworkStateTraker.this.mInternetInvalidCount = 0;
            WifiNetworkStateTraker.this.mInterChangeToInvalid = false;
            WifiNetworkStateTraker.this.mLastDetectInter = 0;
            WifiNetworkStateTraker.this.mInternetStandoffTime = 0;
            WifiNetworkStateTraker.this.mTrigScanCount = 0;
            WifiNetworkStateTraker.this.mNetQulityGoodCount = 0;
            WifiNetworkStateTraker.this.mOldTcpStatus = 0;
            for (int lossInt = 0; lossInt < WifiNetworkStateTraker.this.mLossArray.length; lossInt++) {
                WifiNetworkStateTraker.this.mLossArray[lossInt] = WifiNetworkStateTraker.this.mLossInit;
            }
            WifiNetworkStateTraker.this.mIndex = 0;
            WifiNetworkStateTraker.this.mCurrentLoss = new VolumeWeightedEMA(WifiNetworkStateTraker.this.EXP_COEFFICIENT_MONITOR);
            WifiNetworkStateTraker wifiNetworkStateTraker = WifiNetworkStateTraker.this;
            WifiNetworkStateTraker wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
            WifiNetworkStateTraker wifiNetworkStateTraker3 = WifiNetworkStateTraker.this;
            wifiNetworkStateTraker.sendMessage(wifiNetworkStateTraker2.obtainMessage(WifiNetworkStateTraker.CMD_RSSI_FETCH, wifiNetworkStateTraker3.mRssiFetchToken = wifiNetworkStateTraker3.mRssiFetchToken + 1, 0));
            WifiNetworkStateTraker.this.removeMessages(WifiNetworkStateTraker.CMD_TRAFFIC_MONITOR);
            WifiNetworkStateTraker.this.sendMessageDelayed(WifiNetworkStateTraker.this.obtainMessage(WifiNetworkStateTraker.CMD_TRAFFIC_MONITOR), (long) WifiNetworkStateTraker.this.INTERNET_STANDOFF_TIME);
            WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_AUTO_CONN, WifiNetworkStateTraker.TYPE_MONITOR_WLAN, (WifiNetworkRecord) WifiNetworkStateTraker.this.mWifiNetworkRecord.get(WifiNetworkStateTraker.this.mLastConfigkey), null, WifiNetworkStateTraker.INVALID_INFO);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151573:
                    RssiPacketCountInfo info = msg.obj;
                    if (info != null) {
                        if (!WifiNetworkStateTraker.this.mWifiStateMachineConnected) {
                            WifiNetworkStateTraker.this.Logd("noconnect");
                            break;
                        }
                        WifiNetworkStateTraker.this.updateRecordLinkQuality(info);
                        WifiNetworkStateTraker.this.mLastPkgInfo = info;
                        break;
                    }
                    break;
                case 151574:
                    if (WifiNetworkStateTraker.this.DBG) {
                        WifiNetworkStateTraker.this.Logd("RSSI_FETCH_FAILED");
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    break;
                case WifiNetworkStateTraker.CMD_RSSI_FETCH /*200708*/:
                    if (msg.arg1 == WifiNetworkStateTraker.this.mRssiFetchToken) {
                        WifiNetworkStateTraker.this.mWsmChannel.sendMessage(151572);
                        WifiNetworkStateTraker wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                        WifiNetworkStateTraker wifiNetworkStateTraker2 = WifiNetworkStateTraker.this;
                        WifiNetworkStateTraker wifiNetworkStateTraker3 = WifiNetworkStateTraker.this;
                        wifiNetworkStateTraker.sendMessageDelayed(wifiNetworkStateTraker2.obtainMessage(WifiNetworkStateTraker.CMD_RSSI_FETCH, wifiNetworkStateTraker3.mRssiFetchToken = wifiNetworkStateTraker3.mRssiFetchToken + 1, 0), (long) WifiNetworkStateTraker.this.mLinkInterval);
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.CMD_TRAFFIC_MONITOR /*200709*/:
                    WifiNetworkStateTraker.this.detectTraffic();
                    WifiNetworkStateTraker.this.sendMessageDelayed(WifiNetworkStateTraker.this.obtainMessage(WifiNetworkStateTraker.CMD_TRAFFIC_MONITOR), (long) WifiNetworkStateTraker.this.mTrafficInteval);
                    break;
                case WifiNetworkStateTraker.CMD_INTERNET_MONITOR /*200710*/:
                    WifiNetworkStateTraker.this.detectInternet();
                    if (WifiNetworkStateTraker.this.mToData) {
                        if (!(WifiNetworkStateTraker.this.mAutoSwitch && WifiNetworkStateTraker.this.mFeatureState && WifiNetworkStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue())) {
                            WifiNetworkStateTraker.this.mInterInteval = WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME;
                        }
                        WifiNetworkStateTraker.this.Logd("miit = " + WifiNetworkStateTraker.this.mInterInteval);
                        if (!WifiNetworkStateTraker.this.mScreenOn) {
                            long wakeupTime = System.currentTimeMillis() + ((long) WifiNetworkStateTraker.this.mInterInteval);
                            WifiNetworkStateTraker.this.mAlarmManager.cancel(WifiNetworkStateTraker.this.mDetectInterIntent);
                            WifiNetworkStateTraker.this.mAlarmManager.set(1, wakeupTime, WifiNetworkStateTraker.this.mDetectInterIntent);
                            break;
                        }
                        WifiNetworkStateTraker.this.removeMessages(WifiNetworkStateTraker.CMD_INTERNET_MONITOR);
                        WifiNetworkStateTraker.this.sendMessageDelayed(WifiNetworkStateTraker.this.obtainMessage(WifiNetworkStateTraker.CMD_INTERNET_MONITOR), (long) WifiNetworkStateTraker.this.mInterInteval);
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_SHOW_ALERT /*200719*/:
                    String show_config = msg.obj;
                    WifiNetworkStateTraker.this.Logd("shcon:" + show_config);
                    if (!(show_config == null || WifiNetworkStateTraker.this.hasCheckNoRemind())) {
                        WifiNetworkStateTraker.this.showDialogFordataNewtype((WifiNetworkRecord) WifiNetworkStateTraker.this.mWifiNetworkRecord.get(show_config));
                        break;
                    }
                case WifiNetworkStateTraker.EVENT_DISMISS_ALERT /*200720*/:
                    String dismiss_config = msg.obj;
                    WifiNetworkStateTraker.this.Logd("dicon:" + dismiss_config);
                    if (dismiss_config != null) {
                        WifiNetworkStateTraker.this.dismissDialog(1);
                        break;
                    }
                    break;
                case WifiNetworkStateTraker.EVENT_DETECT_ALTERNATIVE /*200721*/:
                    String detect_config = msg.obj;
                    WifiNetworkStateTraker.this.Logd("decon:" + detect_config);
                    WifiNetworkStateTraker.this.detectNetworkAvailable((WifiNetworkRecord) WifiNetworkStateTraker.this.mWifiNetworkRecord.get(detect_config), WifiNetworkStateTraker.this.WLAN_NETWORK_INVALID, WifiNetworkStateTraker.INVALID_INFO, true);
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
            WifiNetworkStateTraker.this.Logd(getName());
            WifiNetworkStateTraker.this.mConnectedId = WifiNetworkStateTraker.this.mLastNetId;
            WifiNetworkStateTraker.this.Logd("VerifyInternetState mConnectedId= " + WifiNetworkStateTraker.this.mConnectedId);
        }

        public boolean processMessage(Message msg) {
            WifiNetworkStateTraker.this.Logd(getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiNetworkStateTraker.EVENT_NETWORK_MONITOR_CHANGE /*200707*/:
                    if (WifiNetworkStateTraker.this.mManualConnect || WifiNetworkStateTraker.this.mInterResult) {
                        WifiNetworkStateTraker.this.transitionTo(WifiNetworkStateTraker.this.mNetworkMonitorState);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private class VolumeWeightedEMA {
        private final double mAlpha;
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
    private static /* synthetic */ int[] m4-getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (f4-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return f4-android-net-wifi-SupplicantStateSwitchesValues;
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
        f4-android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    public WifiNetworkStateTraker(Context c, WifiStateMachine wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst, WifiRomUpdateHelper wruh, Handler t) {
        super(TAG, t.getLooper());
        this.mContext = c;
        this.mWifiStateMachine = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mWifiRomUpdateHelper = wruh;
        this.mHandler = new NetHandler(t.getLooper());
        this.mWriter = new DelayedDiskWrite();
        this.mWah = new WifiAssistantHelper(this.mContext, this);
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
                WifiNetworkStateTraker wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                if (Global.getInt(WifiNetworkStateTraker.this.mContext.getContentResolver(), WifiNetworkStateTraker.WIFI_AUTO_CHANGE_NETWORK, 1) != 1) {
                    z = false;
                }
                wifiNetworkStateTraker.mDataAutoSwitch = z;
                WifiNetworkStateTraker.this.setDataAutoSwitch(WifiNetworkStateTraker.this.mDataAutoSwitch);
                WifiNetworkStateTraker.this.Logd(" mdas= " + WifiNetworkStateTraker.this.mDataAutoSwitch);
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
        this.mTcpInfoMonitor = new TcpInfoMonitor(this.mContext);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.enableVerboseLogging(verbose);
        }
        if (this.mTcpInfoMonitor != null) {
            this.mTcpInfoMonitor.enableVerboseLogging(verbose);
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
                if (WifiNetworkStateTraker.this.mFeatureState && WifiNetworkStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                    if (WifiNetworkStateTraker.this.DBG) {
                        Log.d("AssistReceiver", "event:" + action);
                    }
                    if (action.equals(WifiNetworkStateTraker.ACTION_WIFI_NETWORK_CONNECT)) {
                        WifiNetworkStateTraker.this.mHandler.sendMessage(WifiNetworkStateTraker.this.mHandler.obtainMessage(WifiNetworkStateTraker.EVENT_CONNECT_NETWORK, intent));
                    } else if (action.equals(WifiNetworkStateTraker.ACTION_WIFI_NETWORK_STATE)) {
                        WifiNetworkStateTraker.this.mHandler.sendMessage(WifiNetworkStateTraker.this.mHandler.obtainMessage(WifiNetworkStateTraker.EVENT_ADD_UPDATE_NETWORK, intent));
                    } else if (!(action.equals(WifiNetworkStateTraker.ACTION_WIFI_NETWORK_AVAILABLE) || action.equals(WifiNetworkStateTraker.ACTION_WIFI_NETWORK_NOT_AVAILABLE))) {
                        if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (networkInfo != null && networkInfo.getDetailedState() == DetailedState.CONNECTED) {
                                WifiNetworkStateTraker.this.mWifiStateMachineConnected = true;
                            }
                        } else if (action.equals("android.net.wifi.CONFIGURED_NETWORKS_CHANGE")) {
                            WifiConfiguration config = (WifiConfiguration) intent.getExtra("wifiConfiguration", null);
                            int changeReason = intent.getIntExtra("changeReason", 0);
                            if (config != null && config.status == 1 && changeReason == 2 && config.networkId != -1 && config.networkId == WifiNetworkStateTraker.this.mConnectId && WifiNetworkStateTraker.this.mManualConnect) {
                                WifiNetworkStateTraker.this.Loge("DISABLED, set mManualConnect false");
                                WifiNetworkStateTraker.this.mManualConnect = false;
                                WifiNetworkStateTraker.this.mOperateUid = 1000;
                            }
                        } else if (action.equals(WifiNetworkStateTraker.ACTION_DETECT_INTERNET)) {
                            WifiNetworkStateTraker.this.sendMessage(WifiNetworkStateTraker.CMD_INTERNET_MONITOR);
                        } else if (action.equals("android.intent.action.SCREEN_ON")) {
                            if (WifiNetworkStateTraker.this.mToData) {
                                WifiNetworkStateTraker.this.mInterInteval = WifiNetworkStateTraker.INTERNET_TO_DATA_INTERVAL;
                            } else {
                                WifiNetworkStateTraker.this.mInterInteval = WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME;
                            }
                            WifiNetworkStateTraker.this.mScreenOn = true;
                            if (WifiNetworkStateTraker.this.getCurrentState() == WifiNetworkStateTraker.this.mNetworkMonitorState && WifiNetworkStateTraker.this.mLastDetectInter != 0 && System.currentTimeMillis() - WifiNetworkStateTraker.this.mLastDetectInter > ((long) WifiNetworkStateTraker.this.mInterInteval)) {
                                WifiNetworkStateTraker.this.mAlarmManager.cancel(WifiNetworkStateTraker.this.mDetectInterIntent);
                                WifiNetworkStateTraker.this.mAlarmManager.set(1, 5000, WifiNetworkStateTraker.this.mDetectInterIntent);
                            }
                        } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                            WifiNetworkStateTraker.this.mInterInteval = WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME;
                            WifiNetworkStateTraker.this.mScreenOn = false;
                        } else if (action.equals(WifiNetworkStateTraker.WIFI_TO_DATA)) {
                            WifiNetworkStateTraker.this.mToData = intent.getBooleanExtra(WifiNetworkStateTraker.EXTRA_WIFI_TO_DATA, false);
                            if (WifiNetworkStateTraker.this.mToData != WifiNetworkStateTraker.this.mLastToData) {
                                if (WifiNetworkStateTraker.this.mToData) {
                                    WifiNetworkStateTraker.this.mInterInteval = WifiNetworkStateTraker.INTERNET_TO_DATA_INTERVAL;
                                    WifiNetworkStateTraker.this.mTcpInterval = WifiNetworkStateTraker.this.TCP_SAMPLE_INTERVAL_LONG;
                                    long delayTime = System.currentTimeMillis() + ((long) WifiNetworkStateTraker.this.mInterInteval);
                                    WifiNetworkStateTraker.this.mAlarmManager.cancel(WifiNetworkStateTraker.this.mDetectInterIntent);
                                    WifiNetworkStateTraker.this.mAlarmManager.set(1, delayTime, WifiNetworkStateTraker.this.mDetectInterIntent);
                                    WifiNetworkStateTraker.this.Logd("mcn=" + WifiNetworkStateTraker.this.mChangeNetwork + ",mcds=" + WifiNetworkStateTraker.this.mClickDialogSwitch + ",micti=" + WifiNetworkStateTraker.this.mInterChangeToInvalid + ",mir=" + WifiNetworkStateTraker.this.mInterResult);
                                    if (WifiNetworkStateTraker.this.mChangeNetwork && !WifiNetworkStateTraker.this.mClickDialogSwitch && ((!WifiNetworkStateTraker.this.mInterChangeToInvalid && WifiNetworkStateTraker.this.mInterResult) || WifiNetworkStateTraker.this.mInterChangeToInvalid)) {
                                        WifiNetworkStateTraker.this.sendMessage(WifiNetworkStateTraker.EVENT_SHOW_ALERT, WifiNetworkStateTraker.this.mLastConfigkey);
                                    }
                                    if (WifiNetworkStateTraker.this.mClickDialogSwitch) {
                                        WifiNetworkStateTraker.this.mAutoSwithToData = false;
                                    } else {
                                        WifiNetworkStateTraker.this.mAutoSwithToData = true;
                                    }
                                    WifiNetworkStateTraker.this.mClickDialogSwitch = false;
                                    WifiNetworkStateTraker.this.detectSwitchDataFrequence();
                                    if (WifiNetworkStateTraker.this.mChangeNetwork) {
                                        WifiNetworkStateTraker.this.mAutoConnDataTime = System.currentTimeMillis();
                                        WifiNetworkStateTraker.this.mAutoConnWlanTime = 0;
                                        WifiNetworkStateTraker.this.mAutoDataToWlanTime = 0;
                                        WifiNetworkRecord curTodataRecord = (WifiNetworkRecord) WifiNetworkStateTraker.this.mWifiNetworkRecord.get(WifiNetworkStateTraker.this.mLastConfigkey);
                                        int validRecordCount = WifiNetworkStateTraker.this.mSortNetworkRecord.size();
                                        WifiNetworkStateTraker.this.Logd("stc wda");
                                        WifiNetworkStateTraker.this.setAssistantStatistics(WifiNetworkStateTraker.STATISTIC_AUTO_CONN, WifiNetworkStateTraker.TYPE_WLAN_TO_DATA, curTodataRecord, null, validRecordCount);
                                    }
                                } else {
                                    WifiNetworkStateTraker.this.mWifiStateMachine.sendWifiNetworkScore(WifiNetworkStateTraker.DEFAULT_SCORE, true);
                                    WifiNetworkStateTraker.this.mTriggerData = true;
                                    WifiNetworkStateTraker.this.mChangeNetwork = false;
                                    WifiNetworkStateTraker.this.mInterInteval = WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME;
                                    WifiNetworkStateTraker.this.sendMessage(WifiNetworkStateTraker.EVENT_DISMISS_ALERT, WifiNetworkStateTraker.this.mLastConfigkey);
                                }
                                WifiNetworkStateTraker.this.mLastToData = WifiNetworkStateTraker.this.mToData;
                            }
                        } else if (action.equals("android.net.conn.NETWORK_CONDITIONS_MEASURED")) {
                            if (intent.getIntExtra("extra_connectivity_type", -1) == 1) {
                                if (intent.getBooleanExtra("extra_is_captive_portal", false)) {
                                    WifiNetworkStateTraker.this.Logd("Received ACTION_NETWORK_CONDITIONS_MEASURED, wlan is captive portal.");
                                    if (!WifiNetworkStateTraker.this.mIsSoftAP || !WifiNetworkStateTraker.this.isThirdAppOperate()) {
                                        if (WifiNetworkStateTraker.this.captivePortal) {
                                            WifiNetworkStateTraker.this.Logd("is cp.");
                                            return;
                                        }
                                        WifiInfo CurWifiInfo = WifiNetworkStateTraker.this.mWifiStateMachine.syncRequestConnectionInfo();
                                        String netStateSsid = intent.getStringExtra("extra_ssid");
                                        WifiNetworkStateTraker.this.Logd("cpnss: " + netStateSsid + ", info: " + CurWifiInfo);
                                        if (CurWifiInfo != null && netStateSsid != null && CurWifiInfo.getSSID().equals(netStateSsid)) {
                                            WifiConfiguration netConf = WifiNetworkStateTraker.this.getWifiConfig(netStateSsid, CurWifiInfo.getBSSID());
                                            if (netConf != null && netConf.networkId == WifiNetworkStateTraker.this.mConnectedId) {
                                                WifiNetworkStateTraker.this.captivePortal = true;
                                                if ((WifiNetworkStateTraker.this.mGotInternetResult & 4) != 4) {
                                                    WifiNetworkStateTraker.this.sendVerifyBroadcast(netConf.configKey(false));
                                                    if (WifiNetworkStateTraker.this.mAutoSwitch && WifiNetworkStateTraker.this.mFeatureState && WifiNetworkStateTraker.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                                                        WifiNetworkStateTraker.this.sendNetworkStateBroadCast(netConf.configKey(false), false);
                                                    }
                                                    int newConfig = 0;
                                                    if (WifiNetworkStateTraker.this.mManualConnect && WifiNetworkStateTraker.this.mConnectId == netConf.networkId && WifiNetworkStateTraker.this.mConnectId != -1) {
                                                        newConfig = WifiNetworkStateTraker.this.mIsNewConfig;
                                                    }
                                                    WifiNetworkStateTraker.this.mHandler.sendMessage(WifiNetworkStateTraker.this.mHandler.obtainMessage(WifiNetworkStateTraker.EVENT_CAPTIVE_PORTAL, newConfig, 0, netConf.configKey(false)));
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
                        } else if (action.equals(WifiNetworkStateTraker.DATA_SCORE_CHANGE)) {
                            int dataScore = intent.getIntExtra(WifiNetworkStateTraker.EXTRA_DATA_CORE, 10);
                            WifiNetworkStateTraker.this.Logd("ds=" + dataScore);
                            WifiNetworkStateTraker.this.mDataScore = dataScore;
                        }
                    }
                    return;
                }
                WifiNetworkStateTraker.this.Logd("mfs dis");
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
            Loge("exists remove");
            oldfile.delete();
        }
    }

    private void transitionSupplicantState(StateChangeResult stateChangeResult) {
        String str = null;
        SupplicantState supState = stateChangeResult.state;
        this.mConnectingId = stateChangeResult.networkId;
        WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mConnectingId);
        if (config != null) {
            str = config.configKey(false);
        }
        this.mConnectingkey = str;
        Logd("Supplicant state: " + supState.toString() + ",mConnectingId= " + this.mConnectingId);
        switch (m4-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()]) {
        }
    }

    public void updateWifiState(int state) {
        if (state == this.DISABLE_INTERFACE) {
            transitionTo(this.mInitState);
            Loge("updateWifiState return");
            return;
        }
        if (state == 3) {
            transitionTo(this.mDisconnectState);
        } else if (state == 1) {
            saveWifiNetworkRecord();
            sendMessage(EVENT_WIFI_STATE_CHANGE);
            this.mManualConnect = false;
            this.mInitAutoConnect = true;
            this.mIsSoftAP = false;
            this.mOperateUid = 1000;
            this.mCurNetwork = null;
            if (this.mWifiState != state) {
                long disableWlanTime = System.currentTimeMillis();
                long disableForWlan = disableWlanTime - this.mAutoConnWlanTime;
                if (this.mAutoConnWlanTime > 0 && disableForWlan > 0 && disableForWlan < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                    Logd("stc wwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_WLAN);
                    this.mAutoConnWlanTime = 0;
                }
                long disableForData = disableWlanTime - this.mAutoDataToWlanTime;
                if (!this.mToData && this.mAutoDataToWlanTime > 0 && disableForData > 0 && disableForData < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                    Logd("stc dwb1");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_WIFI_FOR_DATA);
                    this.mAutoDataToWlanTime = 0;
                }
                if (!this.mToData) {
                    Logd("stc wdc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_WIFI_FOR_DATA);
                }
            } else {
                return;
            }
        }
        this.mWifiState = state;
    }

    public void updateNetworkConnectResult(int netid, boolean valid) {
        Logd("urncr mGotInternetResult= " + this.mGotInternetResult + ",valid= " + valid + ",mInterResult= " + this.mInterResult);
        if (this.mWifiConfigManager != null && (!this.mIsSoftAP || !isThirdAppOperate())) {
            WifiConfiguration updateConfig = this.mWifiConfigManager.getWifiConfiguration(netid);
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
        Logd("sds: id=" + netid + ",dst = " + dst);
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

    public void setManualConnect(boolean isManualConnect, boolean updateState, int uid) {
        this.mManualConnect = isManualConnect;
        if (isManualConnect) {
            this.mOperateUid = uid;
        } else {
            this.mOperateUid = 1000;
        }
        if (updateState && this.mWifiStateMachine != null) {
            this.mWifiStateMachine.sendWifiNetworkScore(DEFAULT_SCORE, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
        }
        Logd("smc" + this.mManualConnect + ",muid=" + this.mOperateUid);
    }

    public void setManualConnTime(long time, boolean save, WifiConfiguration config) {
        if (time > 0) {
            WifiNetworkRecord wifiNetworkRecord = null;
            WifiNetworkRecord newRecord = null;
            if (getCurrentState() == this.mNetworkMonitorState) {
                wifiNetworkRecord = (WifiNetworkRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
            }
            if (config != null) {
                String newConfigkey = config.configKey();
                newRecord = (WifiNetworkRecord) this.mWifiNetworkRecord.get(newConfigkey);
                if (newRecord == null) {
                    newRecord = new WifiNetworkRecord();
                    newRecord.mConfigkey = newConfigkey;
                }
            }
            int isManual = this.mManualConnect ? 1 : 0;
            if (save) {
                long manualConnSaveTime = time;
                long connSaveForWlan = time - this.mAutoConnWlanTime;
                long connForWlanToData = time - this.mAutoConnDataTime;
                if (this.mAutoConnWlanTime > 0 && connSaveForWlan > 0 && connSaveForWlan < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                    Logd("stc wwb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_SAVE_FOR_WLAN, wifiNetworkRecord, newRecord, isManual);
                    this.mAutoConnWlanTime = 0;
                } else if (!this.mChangeNetwork || !this.mToData || this.mAutoConnDataTime <= 0 || connForWlanToData <= 0 || connForWlanToData >= ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                    Logd("stc wwc1");
                    setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_CONN_SAVE_FOR_WLAN, wifiNetworkRecord, newRecord, isManual);
                } else {
                    Logd("stc wdb2");
                    setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA, wifiNetworkRecord, newRecord, isManual);
                    this.mAutoConnDataTime = 0;
                }
            } else {
                long manualConnNewTime = time;
            }
            long connForDataToWlan = time - this.mAutoDataToWlanTime;
            if (!this.mToData && this.mAutoDataToWlanTime > 0 && connForDataToWlan > 0 && connForDataToWlan < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                Logd("stc dwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_CONN_FOR_DATA_TO_WLAN, wifiNetworkRecord, newRecord, isManual);
                this.mAutoDataToWlanTime = 0;
            }
        }
    }

    public boolean getManualConnect() {
        return this.mManualConnect;
    }

    public void setAutoSwitch(boolean isAutoSwitch) {
        Logd("setAutoSwitch: " + isAutoSwitch);
        this.mWifiConfigManager.setWifiAutoSwitch(isAutoSwitch);
        this.mAutoSwitch = isAutoSwitch;
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.switchOnOff(isAutoSwitch);
        }
        if (this.mWifiStateMachine == null) {
            return;
        }
        if (getCurrentState() != this.mConnectedState && getCurrentState() != this.mVerifyInternetState && getCurrentState() != this.mNetworkMonitorState) {
            return;
        }
        if (!isAutoSwitch) {
            long autoSwitchFoData = System.currentTimeMillis() - this.mAutoConnDataTime;
            if (this.mChangeNetwork && this.mToData && this.mAutoConnDataTime > 0 && autoSwitchFoData > 0 && autoSwitchFoData < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                Logd("stc wdb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_SWITCH_FOR_DATA);
                this.mAutoConnDataTime = 0;
            }
            if (this.mChangeNetwork && this.mToData) {
                Logd("stc dwc1 0");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_SWITCH_FOR_DATA);
            }
            this.mWifiStateMachine.sendWifiNetworkScore(DEFAULT_SCORE, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
            this.mWifiStateMachine.setNetworkStatus(true);
            sendNetworkStateBroadCast(this.mLastConfigkey, true);
            sendLowQualityBroadcast(DEFAULT_SCORE, false);
        } else if (this.mWifiNetworkRecord == null) {
        } else {
            if (!this.mIsSoftAP || !isThirdAppOperate()) {
                WifiNetworkRecord lastRecord = (WifiNetworkRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
                if (!(lastRecord == null || lastRecord.mNetworkValid)) {
                    sendNetworkStateBroadCast(this.mLastConfigkey, false);
                }
                if (!this.mInterResult) {
                    if (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) {
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
            if (this.mChangeNetwork && this.mToData && this.mAutoConnDataTime > 0 && disableDataSwitchDistanceTime > 0 && disableDataSwitchDistanceTime < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                Logd("stc wdb4");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
                this.mAutoConnDataTime = 0;
            }
            if (this.mChangeNetwork && this.mToData) {
                Logd("stc dwc1 1");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_DISABLE_DATA_SWITCH_FOR_DATA);
            }
            this.mWifiStateMachine.sendWifiNetworkScore(DEFAULT_SCORE, true);
            this.mChangeNetwork = false;
            this.mTriggerData = true;
            this.mWifiStateMachine.setNetworkStatus(true);
            sendLowQualityBroadcast(DEFAULT_SCORE, false);
        }
    }

    public void setFeatureState(boolean state) {
        Logd("setFS: " + state);
        this.mFeatureState = state;
        if (this.mWifiSmartSwitcher != null) {
            this.mWifiSmartSwitcher.featureState(state);
        }
        if (!(this.mWifiStateMachine == null || this.mFeatureState || (getCurrentState() != this.mConnectedState && getCurrentState() != this.mVerifyInternetState && getCurrentState() != this.mNetworkMonitorState))) {
            this.mWifiStateMachine.sendWifiNetworkScore(DEFAULT_SCORE, true);
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
        WifiInfo currentInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
        String currentSsid;
        if (currentInfo != null) {
            currentSsid = currentInfo.getSSID();
        } else {
            currentSsid = " ";
        }
        if (this.mWifiState != 3) {
            Loge("wifi is not enable.");
        } else if (!this.mIsSoftAP || !isThirdAppOperate()) {
            if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState) && currentInfo != null && detectSsidBelongRecord(this.mConnectingkey)) {
                Logd("current state: " + getCurrentState());
            } else {
                detectNetworkAvailable(null, 0, INVALID_INFO, false);
            }
        }
    }

    private void addOrUpdateRecord(String configKey) {
        WifiInfo aourWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
        Logd("aouR: configKey= " + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                WifiNetworkRecord record;
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                    record.mBssid = this.mLastBssid;
                    record.mConnExp = false;
                    record.mNetFailCount = 0;
                    Logd("aouR: contain and count = " + record.mConnSuccCout);
                } else {
                    Logd("aouR: no contain");
                    record = new WifiNetworkRecord();
                    record.mConfigkey = configKey;
                    if (aourWifiInfo == null) {
                        record.mRssi = WifiConfiguration.INVALID_RSSI;
                        record.mBestRssi = WifiConfiguration.INVALID_RSSI;
                        record.mIs5G = false;
                    } else {
                        record.mRssi = aourWifiInfo.getRssi();
                        record.mBestRssi = aourWifiInfo.getRssi();
                        record.mIs5G = aourWifiInfo.is5GHz();
                    }
                    record.mWifiConfiguration = this.mWifiConfigManager.getWifiConfiguration(configKey);
                    this.mWifiNetworkRecord.put(configKey, record);
                }
            }
            saveWifiNetworkRecord();
        }
    }

    void disableNetworkWithoutInternet() {
        List<WifiNetworkRecord> wifiAssistRecord = getWifiNetworkRecords();
        if (wifiAssistRecord != null && wifiAssistRecord.size() > 0) {
            for (WifiNetworkRecord wnr : wifiAssistRecord) {
                if (wnr.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", Integer.valueOf(1)).intValue() || wnr.mIsCaptive) {
                    WifiConfiguration disableConf = this.mWifiConfigManager.getWifiConfiguration(wnr.mConfigkey);
                    Logd("dnwi: " + (disableConf == null ? "null" : Integer.valueOf(disableConf.networkId)));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0047, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordCaptiveState(String configKey, boolean captive, boolean save) {
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    if (record.mIsCaptive != captive) {
                        record.mIsCaptive = captive;
                    }
                    if (save) {
                        if (this.mManualConnect || !this.captivePortal || (this.mGotInternetResult & 8) == 8) {
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

    /* JADX WARNING: Missing block: B:47:0x00bf, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordDisableState(String configKey) {
        Logd("uRds: " + configKey);
        if (configKey != null) {
            if (this.mAutoSwitch && this.mFeatureState && getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                synchronized (this.mWifiNetworkRecord) {
                    if (this.mWifiNetworkRecord.containsKey(configKey)) {
                        WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                        if (record == null) {
                            return;
                        }
                        int disableId;
                        if (record.mWifiConfiguration != null) {
                            disableId = record.mWifiConfiguration.networkId;
                        } else {
                            WifiConfiguration disableConfig = this.mWifiConfigManager.getWifiConfiguration(configKey);
                            if (disableConfig == null) {
                                return;
                            }
                            disableId = disableConfig.networkId;
                        }
                        Logd("uRds: nfc=" + record.mNetFailCount + ",ic=" + record.mIsCaptive);
                        if (disableId == -1) {
                            return;
                        } else if (record.mNetFailCount > getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NETINVALID_COUNT", Integer.valueOf(1)).intValue() || record.mIsCaptive) {
                            if (this.mWifiStateMachine == null || this.mWifiStateMachine.isSupplicantAvailable()) {
                                this.mWifiConfigManager.disableNetwork(disableId, 8);
                            } else {
                                Logd("wifi is in disable or disable pending state,cancel disable!!");
                                return;
                            }
                        }
                    }
                }
            }
            Logd("switch is off,no need to disable with no-internet");
        }
    }

    /* JADX WARNING: Missing block: B:18:0x005e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordConCount(String configKey) {
        Loge("uRcc key:" + configKey);
        if (configKey != null) {
            WifiInfo ccWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
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
                        Logd("uRcc rs:" + record.mRssi);
                    }
                    record.mWifiConfiguration = this.mWifiConfigManager.getWifiConfiguration(configKey);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordConnectFail(String configKey) {
        Loge("uRcf key:" + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    Logd("uRcf failCount = " + record.mConnFailCount);
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
        Logd("updateRecordUseTime key= " + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    long time = System.currentTimeMillis();
                    long useTime = record.mAccessNetTime != 0 ? time - record.mAccessNetTime : 0;
                    Logd("updateRecordUseTime record= " + record.mConfigkey + ", mant = " + record.mAccessNetTime + ", mint = " + record.mInternetTime + ", useTime = " + useTime);
                    record.mInternetTime = useTime;
                    record.mLastuseTime = time;
                    record.mAccessNetTime = 0;
                    if (this.mWifiConfigManager.getWifiConfiguration(configKey) == null) {
                        record.mWifiConfiguration = null;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:53:0x0185, code:
            sortNetworkRecords();
     */
    /* JADX WARNING: Missing block: B:54:0x0188, code:
            if (r19 == false) goto L_0x0192;
     */
    /* JADX WARNING: Missing block: B:55:0x018a, code:
            sendMessage(EVENT_NETWORK_MONITOR_CHANGE);
     */
    /* JADX WARNING: Missing block: B:57:0x0196, code:
            if (r16.mAutoSwitch == false) goto L_0x01b5;
     */
    /* JADX WARNING: Missing block: B:59:0x019c, code:
            if (r16.mFeatureState == false) goto L_0x01b5;
     */
    /* JADX WARNING: Missing block: B:61:0x01b0, code:
            if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", java.lang.Boolean.valueOf(true)).booleanValue() == false) goto L_0x01b5;
     */
    /* JADX WARNING: Missing block: B:62:0x01b2, code:
            sendNetworkStateBroadCast(r17, r18);
     */
    /* JADX WARNING: Missing block: B:63:0x01b5, code:
            saveWifiNetworkRecord();
     */
    /* JADX WARNING: Missing block: B:64:0x01b8, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRecordInternetStateAndTime(String configKey, boolean valid, boolean transiTo) {
        Throwable th;
        Logd("urit key= " + configKey + ", valid = " + valid);
        if (configKey != null && (getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState || getCurrentState() == this.mNetworkMonitorState)) {
            this.mLastConfigkey = configKey;
            this.mAccessNetTime = valid ? System.currentTimeMillis() : 0;
            WifiInfo uritWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
            synchronized (this.mWifiNetworkRecord) {
                try {
                    WifiNetworkRecord record;
                    if (this.mWifiNetworkRecord.containsKey(configKey)) {
                        record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                        if (record == null) {
                            return;
                        }
                        Logd("urit mant= " + record.mAccessNetTime + ", time = " + this.mAccessNetTime);
                        record.mAccessNetTime = this.mAccessNetTime;
                        record.mNetworkValid = valid;
                    } else {
                        WifiNetworkRecord record2 = new WifiNetworkRecord();
                        try {
                            record2.mConfigkey = configKey;
                            record2.mBssid = this.mLastBssid;
                            record2.mAccessNetTime = this.mAccessNetTime;
                            record2.mNetworkValid = valid;
                            record2.mConnSuccCout++;
                            record2.mWifiConfiguration = this.mWifiConfigManager.getWifiConfiguration(configKey);
                            record = record2;
                        } catch (Throwable th2) {
                            th = th2;
                            record = record2;
                            throw th;
                        }
                    }
                    if (uritWifiInfo != null) {
                        record.mRssi = uritWifiInfo.getRssi();
                        record.mIs5G = uritWifiInfo.is5GHz();
                    }
                    int index = getQulityIndex(record.mIs5G, record.mRssi);
                    int curScore = DEFAULT_SCORE;
                    if (index == 0) {
                        curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", Integer.valueOf(15)).intValue();
                    } else if (index == 1) {
                        curScore = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
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
                            this.mNetQulityArray[i] = this.WLAN_NETWORK_INVALID;
                        }
                        for (tcpIndex = 0; tcpIndex < this.mTcpstateArray.length; tcpIndex++) {
                            this.mTcpstateArray[tcpIndex] = 0;
                        }
                        record.mNetQualitys[index] = this.WLAN_NETWORK_INVALID;
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
        Loge("updateRecordWifiConfig key:" + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(configKey);
                    if (record == null) {
                        return;
                    }
                    if (record.mWifiConfiguration != null) {
                        Logd("updateRecordWifiConfig record.config= " + record.mWifiConfiguration.SSID);
                        record.mWifiConfiguration = null;
                    }
                    record.mNetworkValid = false;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x001c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:208:0x0888, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void updateRecordLinkQuality(RssiPacketCountInfo info) {
        WifiNetworkRecord mCandidate = null;
        boolean willBeRoam = false;
        WifiNetworkRecord mLastRecord = (WifiNetworkRecord) this.mWifiNetworkRecord.get(this.mLastConfigkey);
        if (!(mLastRecord == null || info == null)) {
            if (info.rssi > WifiConfiguration.INVALID_RSSI) {
                int i;
                int score = DEFAULT_SCORE;
                int rssi = info.rssi;
                int txbad = info.txbad;
                int txgood = info.txgood;
                mLastRecord.mRssi = rssi;
                if (rssi > mLastRecord.mBestRssi) {
                    mLastRecord.mBestRssi = rssi;
                }
                int newTcpStatus = 0;
                boolean tcpGood = false;
                if (!(this.mChangeNetwork && this.mToData)) {
                    if (this.mTcpInfoMonitor != null) {
                        newTcpStatus = this.mTcpLinkStatus;
                    }
                    if (newTcpStatus != 0) {
                        this.mTcpstateArray[this.mIndex] = newTcpStatus;
                        for (int j = 0; j < 4; j++) {
                            if (this.mTcpstateArray[j] == 16) {
                                tcpGood = true;
                            }
                        }
                        if (tcpGood) {
                            if (newTcpStatus != 16) {
                                if (this.mOldTcpStatus != 16) {
                                    score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_TCP_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                                }
                            }
                            if (newTcpStatus != 16) {
                                score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_TCP_SCORE_THRESHOLD", Integer.valueOf(0)).intValue();
                            }
                        } else {
                            score = 79 - getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_TCP_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
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
                    int BAD_RSSI_SCORE_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_SCORE_THRESHOLD", Integer.valueOf(15)).intValue();
                    if (this.mToData || !tcpAvailable) {
                        score -= BAD_RSSI_SCORE_THRESHOLD + 5;
                    } else {
                        score -= BAD_RSSI_SCORE_THRESHOLD;
                    }
                } else if (netQualityIndex == 1) {
                    int LOW_RSSI_SCORE_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
                    if (this.mToData || !tcpAvailable) {
                        score -= LOW_RSSI_SCORE_THRESHOLD + 5;
                    } else {
                        score -= LOW_RSSI_SCORE_THRESHOLD;
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
                double LOW_LINK_LOSS_THRESHOLD = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_LOW_LINK_LOSS_THRESHOLD", Double.valueOf(STATIC_LOW_LINK_LOSS_THRESHOLD)).doubleValue();
                int NO_TRIFFIC_SCORE_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_NO_LINK_SCORE_THRESHOLD", Integer.valueOf(5)).intValue();
                if (dtotal > 0) {
                    loss = ((double) dbad) / ((double) dtotal);
                    if (loss == 0.0d) {
                        this.mLinkDetectTimes++;
                    } else {
                        this.mLinkDetectTimes = 0;
                        double BAD_LINK_LOSS_THRESHOLD = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_BAD_LINK_LOSS_THRESHOLD", Double.valueOf(STATIC_BAD_LINK_LOSS_THRESHOLD)).doubleValue();
                        double GOOD_LINK_LOSS_THRESHOLD = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_LOSS_THRESHOLD", Double.valueOf(STATIC_GOOD_LINK_LOSS_THRESHOLD)).doubleValue();
                        if (loss >= BAD_LINK_LOSS_THRESHOLD) {
                            int BadLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_LINK_SCORE_THRESHOLD", Integer.valueOf(30)).intValue();
                            if (this.mToData || !tcpAvailable) {
                                score -= BadLinkScoreThreshold + 5;
                            } else {
                                score -= BadLinkScoreThreshold;
                            }
                        } else if (loss >= LOW_LINK_LOSS_THRESHOLD && loss < BAD_LINK_LOSS_THRESHOLD) {
                            int LowLinkScoreThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_LINK_SCORE_THRESHOLD", Integer.valueOf(25)).intValue();
                            if (this.mToData || !tcpAvailable) {
                                score -= LowLinkScoreThreshold + 5;
                            } else {
                                score -= LowLinkScoreThreshold;
                            }
                        } else if (loss >= GOOD_LINK_LOSS_THRESHOLD && loss < LOW_LINK_LOSS_THRESHOLD) {
                            score -= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_SCORE_THRESHOLD", Integer.valueOf(20)).intValue();
                        }
                    }
                } else {
                    this.mLinkDetectTimes = 0;
                    if (this.mToData || !tcpAvailable) {
                        score -= NO_TRIFFIC_SCORE_THRESHOLD + 5;
                    } else {
                        score -= NO_TRIFFIC_SCORE_THRESHOLD;
                    }
                }
                this.mLossArray[this.mIndex] = loss;
                int badLossCount = 0;
                for (double d : this.mLossArray) {
                    if (d >= LOW_LINK_LOSS_THRESHOLD) {
                        badLossCount++;
                    }
                }
                if (badLossCount == this.mLossArray.length) {
                    score -= NO_TRIFFIC_SCORE_THRESHOLD * 3;
                } else if (badLossCount == this.mLossArray.length - 1 && loss >= LOW_LINK_LOSS_THRESHOLD) {
                    score -= NO_TRIFFIC_SCORE_THRESHOLD * 2;
                } else if (badLossCount == this.mLossArray.length - 2 && loss >= LOW_LINK_LOSS_THRESHOLD) {
                    score -= NO_TRIFFIC_SCORE_THRESHOLD;
                }
                if (!mLastRecord.mNetworkValid) {
                    score = this.WLAN_NETWORK_INVALID;
                }
                if (score < this.WLAN_NETWORK_INVALID) {
                    score = this.WLAN_NETWORK_INVALID;
                }
                for (i = netQualityIndex; i < 4; i++) {
                    if (score > mLastRecord.mNetQualitys[i]) {
                        mLastRecord.mNetQualitys[i] = score;
                        mLastRecord.mScore = score;
                    }
                }
                Logd("urLQ key:" + this.mLastConfigkey + ",ri=" + rssi + " tb=" + txbad + " tg=" + txgood + ",db = " + dbad + ", dg = " + dgood + ",dt= " + dtotal + ",blc=" + badLossCount + ",ls=" + loss + ",mLDTs= " + this.mLinkDetectTimes + ",tstat:" + newTcpStatus + ",dex =" + this.mIndex + ",sc= " + score + ",mis=" + this.mInterResult + ",mcs=" + this.mChangeScore + ",mqgc=" + this.mNetQulityGoodCount);
                this.mNetQulityArray[this.mIndex] = score;
                this.mIndex++;
                this.mIndex %= 4;
                int sumQuality = 0;
                for (i = 0; i < 4; i++) {
                    sumQuality += this.mNetQulityArray[i];
                }
                int currentQuality = sumQuality / 4;
                int comPareScore = currentQuality > score ? currentQuality : score;
                Logd("cQ=" + currentQuality + ",cS=" + comPareScore + ",wit=" + this.WLAN_INVALID_THRESHOLD + ",mcnt=" + this.mChangeNetwork + ",mtd=" + this.mTriggerData + ",mtod=" + this.mToData);
                broadcastInfotoTestapk("ssid:" + mLastRecord.mConfigkey + ",net:" + mLastRecord.mNetworkValid + ",currScore:" + score + ",score:" + currentQuality + ", rssi:" + rssi + ",loss:" + decimalFormat.format(100.0d * loss) + ",total:" + dtotal + ",tcp:" + newTcpStatus);
                if (this.mAutoSwitch && this.mFeatureState && getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
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
                    int WLAN_TRIGGER_WLAN_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_TRIGGER_WLAN_THRESHOLD", Integer.valueOf(25)).intValue();
                    int ROAM_DETECT_TIMES = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_ROAM_DETECT", Integer.valueOf(6)).intValue();
                    if (currentQuality <= 79 - WLAN_TRIGGER_WLAN_THRESHOLD) {
                        if (score <= 79 - WLAN_TRIGGER_WLAN_THRESHOLD && this.mLastScanTime != 0 && now - this.mLastScanTime > STAIC_SCAN_RESULT_AGE && this.mTrigScanCount < 3 && !this.mTriggerScan) {
                            this.mWifiNative.scan(null, null);
                            this.mTrigScanCount++;
                            this.mTriggerScan = true;
                            this.mHandler.removeMessages(EVENT_SCAN_TIMEOUT);
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SCAN_TIMEOUT), 6000);
                        }
                        if (!this.mTriggerScan) {
                            if (mLastRecord.mNetworkValid) {
                                willBeRoam = detectMaybeRoam(mLastRecord, comPareScore);
                            }
                            if (willBeRoam && this.mRoamdetectCount < ROAM_DETECT_TIMES) {
                                this.mRoamdetectCount++;
                            } else if (score <= 79 - WLAN_TRIGGER_WLAN_THRESHOLD) {
                                mCandidate = detectNetworkAvailable(mLastRecord, comPareScore, mLastRecord.mRssi, false);
                            }
                        }
                    }
                    if (this.mAlertDialog != null && this.mManualConnect && !this.mToData && mLastRecord.mNetworkValid) {
                        if (currentQuality > 79 - WLAN_TRIGGER_WLAN_THRESHOLD) {
                            this.mNetQulityGoodCount++;
                            if (this.mNetQulityGoodCount > ROAM_DETECT_TIMES) {
                                dismissDialog(2);
                                this.mNetQulityGoodCount = 0;
                            }
                        } else {
                            this.mNetQulityGoodCount = 0;
                        }
                    }
                    if (!this.mToData && this.mChangeNetwork && currentQuality > this.DATA_NETWORK_VALID) {
                        changeNetworkToWlan(mLastRecord, currentQuality, false);
                    }
                    if (this.WLAN_INVALID_THRESHOLD == 70 && currentQuality <= 39 && this.mAutoSwitchDataDisableTime > 0) {
                        long napTime = System.currentTimeMillis() - this.mAutoSwitchDataDisableTime;
                        Logd("nt= " + napTime);
                        if (napTime > getRomUpdateLongValue("OPPO_WIFI_ASSISTANT_AUTO_SWITCH_DATA_DISABLE_TIME", Long.valueOf(AUTO_SWITCH_DATA_DISBALE_TIME)).longValue()) {
                            resetAutoSwitchDataDetect();
                        }
                    }
                    int WlanBadThreshold = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_BAD_THRESHOLD", Integer.valueOf(this.WLAN_INVALID_THRESHOLD)).intValue();
                    if (this.mChangeNetwork || currentQuality > 79 - WlanBadThreshold || score > 79 - WlanBadThreshold) {
                        if (this.mChangeNetwork && this.mToData) {
                            int GOOD_LINK_COUNT_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_LINK_COUNT", Integer.valueOf(3)).intValue();
                            if (mLastRecord.mNetworkValid && !this.mInterResult && this.mChangeRssi != 0 && rssi >= -77 && currentQuality >= this.DATA_NETWORK_VALID && score >= this.DATA_NETWORK_VALID && this.mChangeScore == this.WLAN_NETWORK_INVALID) {
                                changeNetworkToWlan(mLastRecord, score, true);
                            } else if ((rssi <= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_RSSI_TO_WLAN_THRESHOLD", Integer.valueOf(-73)).intValue() || this.mLinkDetectTimes < GOOD_LINK_COUNT_THRESHOLD) && (rssi < -65 || this.mLinkDetectTimes < GOOD_LINK_COUNT_THRESHOLD - 2)) {
                                if (rssi >= -65 && this.mTriggerInter && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= this.DIFF_RSSI_THRESHOLD) {
                                    Logd("dt inter for strong rssi");
                                    this.mTriggerInter = false;
                                    this.mAlarmManager.cancel(this.mDetectInterIntent);
                                    this.mAlarmManager.set(0, System.currentTimeMillis(), this.mDetectInterIntent);
                                }
                            } else if (this.mChangeNetwork && mLastRecord.mNetworkValid && currentQuality >= getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_SCORE_GOOD", Integer.valueOf(STATIC_SCORE_NETWORK_GOOD)).intValue() && this.mChangeRssi != 0 && rssi - this.mChangeRssi >= this.DIFF_RSSI_THRESHOLD) {
                                changeNetworkToWlan(mLastRecord, currentQuality, true);
                            }
                        }
                    } else if ((!willBeRoam || (willBeRoam && this.mRoamdetectCount >= ROAM_DETECT_TIMES)) && mCandidate == null && !this.mTriggerScan) {
                        if (mLastRecord.mNetworkValid) {
                            if (currentQuality < this.SCORE_NETWORK_VALID) {
                                currentQuality = this.SCORE_NETWORK_VALID;
                            }
                        } else {
                            currentQuality = this.WLAN_NETWORK_INVALID;
                        }
                        if (!this.mManualConnect && this.mDataAutoSwitch) {
                            this.mChangeNetwork = true;
                            this.mTriggerInter = true;
                            this.mChangeRssi = rssi;
                            this.mChangeScore = currentQuality;
                            this.mWifiStateMachine.sendWifiNetworkScore(currentQuality, false);
                        }
                    }
                } else {
                    Logd("smart switch disable");
                }
            }
        }
    }

    private void changeNetworkToWlan(WifiNetworkRecord record, int quality, boolean dataToWlan) {
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
                Logd("stc dwa");
                setAssistantStatistics(STATISTIC_AUTO_CONN, TYPE_DATA_TO_WLAN, record, null, quality);
            }
        }
    }

    private void broadcastInfotoTestapk(String info) {
        this.broadInfo = info;
        if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_TEST", Boolean.valueOf(false)).booleanValue()) {
            PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
            if ((pm == null || pm.getPackageUid(this.mTestApk, 65536, 0) >= 1000) && this.mBroadHandle != null) {
                this.mBroadHandle.post(new Runnable() {
                    public void run() {
                        Intent broadIntent = new Intent(WifiNetworkStateTraker.BRAOD_WIFI_INFO);
                        Log.d("testFeature", "broadInfo = " + WifiNetworkStateTraker.this.broadInfo);
                        broadIntent.putExtra(WifiNetworkStateTraker.EXTRA_WIFI_NETINFO, WifiNetworkStateTraker.this.broadInfo);
                        WifiNetworkStateTraker.this.mContext.sendStickyBroadcastAsUser(broadIntent, UserHandle.ALL);
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
                Logd("default/");
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
        List<WifiNetworkRecord> wifiRecords = getWifiNetworkRecords();
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
                    Loge("FileNotFoundException: " + e);
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
                Loge("IOException: " + e2);
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
                Logd("swR length = " + wifiRecords.size());
                for (WifiNetworkRecord wnr : wifiRecords) {
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
                Loge("FileNotFoundException: " + e);
                if (out != null) {
                }
                if (config != null) {
                }
            } catch (IOException e14) {
                e2 = e14;
                out = out2;
                config = config2;
                Loge("IOException: " + e2);
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
            Loge("FileNotFoundException: " + e);
            if (out != null) {
            }
            if (config != null) {
            }
        } catch (IOException e16) {
            e2 = e16;
            Loge("IOException: " + e2);
            e2.printStackTrace();
            if (out != null) {
            }
            if (config != null) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:146:0x02d5, code:
            throw r39;
     */
    /* JADX WARNING: Missing block: B:254:0x0560, code:
            Loge("readWifiNetworkRecord end");
     */
    /* JADX WARNING: Missing block: B:255:0x056a, code:
            if (r0 == null) goto L_0x056f;
     */
    /* JADX WARNING: Missing block: B:257:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:262:0x057b, code:
            r39 = th;
     */
    /* JADX WARNING: Missing block: B:263:0x057c, code:
            r35 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readWifiNetworkRecord() {
        BufferedReader bufferedReader;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader bufferedReader2 = null;
        synchronized (this.mWifiNetworkRecord) {
            try {
                this.mWifiNetworkRecord.clear();
                try {
                    bufferedReader = new BufferedReader(new FileReader(WIFI_ASSISTANT_FILE));
                    try {
                        Loge("rwR st");
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            WifiNetworkRecord mwr = new WifiNetworkRecord();
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
                                    Logd("NumberFormatException e:" + e3);
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
                                config = this.mWifiConfigManager.getWifiConfiguration(mwr.mConfigkey);
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
                                config = this.mWifiConfigManager.getWifiConfiguration(mwr.mConfigkey);
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
                        Loge("readWifiNetworkRecord: FileNotFoundException: " + e);
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
                    Loge("readWifiNetworkRecord: IOException: " + e2);
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
        for (WifiNetworkRecord wnr : getWifiNetworkRecords()) {
            Loge("pintRecord wnr: " + wnr.toString());
        }
    }

    private void sortNetworkRecords() {
        List<ScanResult> currentScan = this.mWifiStateMachine.syncGetScanResultsList();
        List<WifiNetworkRecord> tempRecords = getWifiNetworkRecords();
        List<WifiNetworkRecord> mSortValidRecords = new ArrayList();
        for (WifiNetworkRecord tRecord : tempRecords) {
            if (tRecord.mNetworkValid && this.mWifiConfigManager.getWifiConfiguration(tRecord.mConfigkey) != null) {
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
                    Logd("snr:" + tRecord.mConfigkey + ",mri:" + refeRssi + ",mnq[" + index + "]=" + tRecord.mNetQualitys[index]);
                    if (tRecord.mNetQualitys[index] > 0) {
                        tRecord.mScore = tRecord.mNetQualitys[index];
                    }
                    mSortValidRecords.add(tRecord);
                }
            }
        }
        Collections.sort(mSortValidRecords, new Comparator<WifiNetworkRecord>() {
            public int compare(WifiNetworkRecord b1, WifiNetworkRecord b2) {
                if (b2.mScore == 0 || b1.mScore == 0 || b2.mScore == b1.mScore) {
                    return b2.mRssi - b1.mRssi;
                }
                return b2.mScore - b1.mScore;
            }
        });
        synchronized (this.mSortNetworkRecord) {
            this.mSortNetworkRecord.clear();
            for (WifiNetworkRecord sRecord : mSortValidRecords) {
                if (this.mWifiConfigManager.getWifiConfiguration(sRecord.mConfigkey) != null) {
                    this.mSortNetworkRecord.add(sRecord);
                }
            }
        }
    }

    public List<WifiNetworkRecord> getWifiNetworkRecords() {
        List<WifiNetworkRecord> wifiRecord = new ArrayList();
        synchronized (this.mWifiNetworkRecord) {
            for (WifiNetworkRecord wnr : this.mWifiNetworkRecord.values()) {
                wifiRecord.add(new WifiNetworkRecord(wnr));
            }
        }
        return wifiRecord;
    }

    private List<WifiNetworkRecord> getSortNetworkRecords() {
        List<WifiNetworkRecord> sortRecord = new ArrayList();
        synchronized (this.mSortNetworkRecord) {
            for (WifiNetworkRecord wnr : this.mSortNetworkRecord) {
                sortRecord.add(new WifiNetworkRecord(wnr));
            }
        }
        return sortRecord;
    }

    public List<WifiConfiguration> getValidSortConfig() {
        List<WifiConfiguration> mSortConfig = new ArrayList();
        synchronized (this.mSortNetworkRecord) {
            for (WifiNetworkRecord wnr : this.mSortNetworkRecord) {
                WifiConfiguration recordConfig = this.mWifiConfigManager.getWifiConfiguration(wnr.mConfigkey);
                if (recordConfig != null) {
                    mSortConfig.add(new WifiConfiguration(recordConfig));
                }
            }
        }
        return mSortConfig;
    }

    private int getQulityIndex(boolean is5G, int rssi) {
        if (is5G) {
            int GOOD_RSSI_5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_5", Integer.valueOf(-65)).intValue();
            int LOW_RSSI_5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_5", Integer.valueOf(-77)).intValue();
            int BAD_RSSI_5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_5", Integer.valueOf(-83)).intValue();
            if (rssi >= GOOD_RSSI_5) {
                return 3;
            }
            if (rssi >= LOW_RSSI_5 && rssi < GOOD_RSSI_5) {
                return 2;
            }
            if (rssi <= BAD_RSSI_5 || rssi >= LOW_RSSI_5) {
                return 0;
            }
            return 1;
        }
        int GOOD_RSSI_24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_24", Integer.valueOf(-65)).intValue();
        int LOW_RSSI_24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_24", Integer.valueOf(-77)).intValue();
        int BAD_RSSI_24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_24", Integer.valueOf(-83)).intValue();
        if (rssi >= GOOD_RSSI_24) {
            return 3;
        }
        if (rssi >= LOW_RSSI_24 && rssi < GOOD_RSSI_24) {
            return 2;
        }
        if (rssi <= BAD_RSSI_24 || rssi >= LOW_RSSI_24) {
            return 0;
        }
        return 1;
    }

    private void selectCandidateNetwork(WifiNetworkRecord lastRecord) {
        WifiConfiguration selectConf = this.mWifiConfigManager.getWifiConfiguration(lastRecord.mConfigkey);
        if (selectConf == null) {
            Loge("select config is null");
            return;
        }
        Logd("candidate network: " + selectConf.SSID);
        if (this.mWifiStateMachine == null || this.mWifiStateMachine.isSupplicantAvailable()) {
            if (this.mWifiStateMachine != null) {
                this.mWifiStateMachine.clearConfigBSSID(selectConf, TAG);
                this.mWifiStateMachine.setTargetNetworkId(lastRecord.mNetid);
            }
            if (this.mWifiConfigManager.selectNetwork(selectConf, true, Process.myUid()) && this.mWifiNative.reconnect()) {
                this.mSupplicantTracker.sendMessage(131372);
            }
            return;
        }
        Logd("wifi is in disable or disable pending state,cancel reconnect!!");
    }

    private WifiConfiguration getWifiConfig(String ssid, String bssid) {
        if (ssid == null || bssid == null) {
            return null;
        }
        WifiConfiguration connectedConfig = this.mWifiStateMachine.getCurrentWifiConfiguration();
        String connectedKey = "null";
        if (connectedConfig == null) {
            Logd("ccf is null");
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
        Loge("gc cf= " + currentKey);
        WifiConfiguration currentconfig = this.mWifiConfigManager.getWifiConfiguration(currentKey);
        if (currentconfig != null) {
            Logd("gc currentconfig: " + currentconfig.networkId + ",SSID:" + currentconfig.SSID + ",BSSID:" + currentconfig.BSSID);
            if (currentconfig.BSSID == null || currentconfig.BSSID.equals(WifiLastResortWatchdog.BSSID_ANY)) {
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
                    this.WLAN_INVALID_THRESHOLD = 70;
                    this.mAutoSwitchDataDisableTime = curTime;
                    Logd("dsdf time= " + curTime);
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
            this.WLAN_INVALID_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_WLAN_INVALID_THRESHOLD", Integer.valueOf(40)).intValue();
        }
    }

    private boolean detectSsidBelongRecord(String key) {
        boolean contain = false;
        if (key == null) {
            return false;
        }
        synchronized (this.mSortNetworkRecord) {
            if (this.mSortNetworkRecord.size() > 0) {
                for (WifiNetworkRecord network : this.mSortNetworkRecord) {
                    if (network.mConfigkey.equals(key)) {
                        contain = true;
                        break;
                    }
                }
            }
        }
        Logd("detectSsidBelongRecord: return = " + contain);
        return contain;
    }

    private boolean detectMaybeRoam(WifiNetworkRecord lastRecord, int score) {
        boolean maybeRoam = false;
        List<ScanResult> roamScan = this.mWifiStateMachine.syncGetScanResultsList();
        if (lastRecord == null) {
            Logd("detectMaybeRoam lastRecord is null!");
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
            if (scanConfKey.equals(lastRecord.mConfigkey) && scanRssi > this.PER_ROAM_THRESHOLD && lastRecord.mBssid != null && !scanBssid.equals(lastRecord.mBssid)) {
                if (curFreq != scanFreq || (curFreq == scanFreq && scanRssi - lastRecord.mRssi > 5)) {
                    maybeRoam = true;
                    break;
                }
            }
        }
        Logd("detectMaybeRoam maybeRoam=" + maybeRoam);
        return maybeRoam;
    }

    private void detectTraffic() {
        long txPkts = TrafficStats.getTcpTxPackets(mInterfaceName);
        long rxPkts = TrafficStats.getTcpRxPackets(mInterfaceName);
        long lastDetlaRxPkts = this.dRxPkts;
        this.dTxPkts = txPkts - this.mLastTxPkts;
        this.dRxPkts = rxPkts - this.mLastRxPkts;
        this.mLastTxPkts = txPkts;
        this.mLastRxPkts = rxPkts;
        if (this.dRxPkts >= ((long) this.TRAFFIC_DIFF_LOWEST) || this.dTxPkts <= 0) {
            this.mRxPktsLowCount = 0;
        } else {
            this.mRxPktsLowCount++;
        }
        Logd("DT,tP=" + txPkts + ", dTP=" + this.dTxPkts + ", rP=" + rxPkts + ", dRP=" + this.dRxPkts + " mIR=" + this.mInterResult + ", mRPLC=" + this.mRxPktsLowCount);
        if (!this.mInterResult) {
            boolean z;
            if (this.captivePortal) {
                z = true;
            } else {
                z = this.mResponseGotFromGateway;
            }
            if (z) {
                if (lastDetlaRxPkts > ((long) this.TRAFFIC_DIFF_BURST) && this.dRxPkts > ((long) this.TRAFFIC_DIFF_BURST) && this.dTxPkts > ((long) this.TRAFFIC_DIFF_BURST)) {
                    triggerInternetDetect(true);
                } else if (this.dRxPkts > ((long) this.TRAFFIC_LOW_COUNT) && this.dTxPkts > ((long) this.TRAFFIC_LOW_COUNT)) {
                    triggerInternetDetect(false);
                }
            } else if (this.dRxPkts > ((long) this.TRAFFIC_LOW_COUNT) && this.dTxPkts > ((long) this.TRAFFIC_LOW_COUNT)) {
                triggerInternetDetect(true);
            }
        } else if (this.mInterResult && this.mRxPktsLowCount >= this.TRAFFIC_LOW_COUNT) {
            triggerInternetDetect(true);
            this.mRxPktsLowCount = 0;
        }
    }

    private void triggerInternetDetect(boolean forceTrigger) {
        long standoffTime = System.currentTimeMillis() - this.mLastDetectInter;
        this.mTcpInterval = this.TCP_SAMPLE_INTERVAL_SHORT;
        Logd("triggerInternetDetect, forceTrigger=" + forceTrigger + ", mLastInternetResult=" + this.mLastInternetResult + ", standoffTime=" + standoffTime + ", mInternetStandoffTime=" + this.mInternetStandoffTime);
        if (standoffTime >= ((long) this.INTERNET_STANDOFF_TIME)) {
            if (forceTrigger || this.mLastInternetResult != this.mInterResult) {
                sendMessage(CMD_INTERNET_MONITOR);
                this.mInternetStandoffTime = this.INTERNET_INTERVAL_DELTA;
            } else if (standoffTime > ((long) this.mInternetStandoffTime)) {
                sendMessage(CMD_INTERNET_MONITOR);
                if (this.mInternetStandoffTime < WifiQualifiedNetworkSelector.BSSID_BLACKLIST_EXPIRE_TIME) {
                    this.mInternetStandoffTime += this.INTERNET_INTERVAL_DELTA;
                }
            }
        }
    }

    private WifiNetworkRecord detectNetworkAvailable(WifiNetworkRecord lastRecord, int curScore, int curRssi, boolean force) {
        List<WifiNetworkRecord> netRecords = getSortNetworkRecords();
        List<ScanResult> scanList = this.mWifiStateMachine.syncGetScanResultsList();
        WifiNetworkRecord candidate = null;
        int refScore = curScore;
        int refRssi = curRssi;
        double refConsuccRate = 0.0d;
        int refSpeed = 0;
        int DIFF_SCORE_THRESHOLD = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_DIFF_SCORE_THRESHOLD", Integer.valueOf(10)).intValue();
        double DIFF_CONNRATE_THRESHOLD = getRomUpdateFloatValue("OPPO_WIFI_ASSISTANT_DIFF_CONNRATE_THRESHOLD", Double.valueOf(STATIC_DIFF_CONNRATE_THRESHOLD)).doubleValue();
        boolean refIs5G = false;
        if (lastRecord == null) {
            Logd("dna scan detect," + this.mManualConnect);
        }
        for (WifiNetworkRecord wnr : netRecords) {
            if (!wnr.mNetworkValid || wnr.mConfigkey == null) {
                Loge("config key is null or invalid");
            } else if (wnr.mConnExp) {
                Loge("config co exp");
            } else if (lastRecord == null || !lastRecord.mConfigkey.equals(wnr.mConfigkey)) {
                WifiConfiguration wcf = this.mWifiConfigManager.getWifiConfiguration(wnr.mConfigkey);
                if (wcf == null) {
                    Loge("config is null");
                } else if (wcf.status == 1) {
                    Loge("config is disable");
                } else {
                    double consuccRate = ((double) wnr.mConnSuccCout) / ((double) (wnr.mConnFailCount + wnr.mConnSuccCout));
                    for (ScanResult result : scanList) {
                        String scanSsid = "\"" + result.SSID + "\"";
                        String scanBssid = result.BSSID;
                        int scanRssi = result.level;
                        if (WifiConfiguration.configKey(result).equals(wnr.mConfigkey)) {
                            int index = getQulityIndex(result.is5GHz(), result.level);
                            Logd("dna " + wnr.mConfigkey + ",nq[" + index + "]:" + wnr.mNetQualitys[index] + ",rfs:" + refScore + ",rfsi:" + refRssi + ",ss:" + scanRssi + ",csr:" + consuccRate);
                            if (lastRecord == null || scanRssi > -83) {
                                int diffScore;
                                if (wnr.mNetQualitys[index] == -1 || refScore == -1) {
                                    diffScore = scanRssi - refRssi;
                                } else {
                                    diffScore = wnr.mNetQualitys[index] - refScore;
                                    if (wnr.mNetQualitys[index] - curScore < DIFF_SCORE_THRESHOLD) {
                                    }
                                }
                                if (lastRecord == null || !lastRecord.mNetworkValid || scanRssi - curRssi >= this.DIFF_RSSI_THRESHOLD) {
                                    if (diffScore >= DIFF_SCORE_THRESHOLD) {
                                        refScore = wnr.mNetQualitys[index];
                                        refRssi = scanRssi;
                                        refConsuccRate = consuccRate;
                                        refSpeed = wnr.mMaxSpeed;
                                        refIs5G = wnr.mIs5G;
                                        candidate = wnr;
                                    } else if (diffScore < DIFF_SCORE_THRESHOLD && diffScore >= 0) {
                                        if (!refIs5G || result.is5GHz()) {
                                            if (refIs5G || !result.is5GHz()) {
                                                double diffConsuccRate = consuccRate - refConsuccRate;
                                                if (diffConsuccRate >= DIFF_CONNRATE_THRESHOLD) {
                                                    refScore = wnr.mNetQualitys[index];
                                                    refRssi = scanRssi;
                                                    refConsuccRate = consuccRate;
                                                    refSpeed = wnr.mMaxSpeed;
                                                    refIs5G = wnr.mIs5G;
                                                    candidate = wnr;
                                                } else if (diffConsuccRate < DIFF_CONNRATE_THRESHOLD && diffConsuccRate > 0.0d && wnr.mMaxSpeed - refSpeed > 0) {
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
                Loge("config is same");
            }
        }
        if (candidate == null) {
            Logd("cdd = null, mds=" + this.mDataScore + ",mmc=" + this.mManualConnect + ",mdas= " + this.mDataAutoSwitch + ",mds=" + this.mDataState);
        } else {
            Logd("mCId=" + this.mConnectingId + ",cdd = " + candidate.toString());
        }
        if (this.mAutoSwitch && this.mFeatureState) {
            if (getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
                if (this.mManualConnect) {
                    if (!(lastRecord == null || lastRecord.mWifiConfiguration == null || candidate == null) || (this.mDataAutoSwitch && this.mDataScore == 50 && this.mDataState && curScore <= 79 - this.WLAN_INVALID_THRESHOLD)) {
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
        if ((this.mAutoSwitch && !this.mFeatureState) || (lastRecord == null && candidate == null && this.mInitAutoConnect)) {
            this.mWifiNative.reconnect();
            this.mInitAutoConnect = false;
        }
        return candidate;
    }

    public boolean inSpecialUrlList(String url) {
        if (url == null) {
            Loge("url is null.");
            return false;
        }
        String value = getRomUpdateValue("NETWORK_SPECIAL_REDIRECT_URL", DEFAULT_SPECIAL_URL);
        if (value == null) {
            Loge("Fail to getRomUpdateValue.");
            return false;
        }
        Logd("inSpecialUrlList(), url list: " + value);
        for (String name : value.split(",")) {
            if (url.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPublicServers() {
        List<String> defaultServers = Arrays.asList(this.mPublicServers);
        String value = getRomUpdateValue("NETWORK_PUBLIC_SERVERS_URL", null);
        if (value == null) {
            Loge("Fail to getRomUpdateValue, using default servers!");
            return defaultServers;
        }
        Logd("getPublicServers, updated servers: " + value);
        List<String> updatedServers = new ArrayList();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        Loge("updated Servers less than 2, using default servers!");
        return defaultServers;
    }

    private List<String> getInternalServers() {
        List<String> originalServers = Arrays.asList(this.mInternalServers);
        String value = getRomUpdateValue("OPPO_WIFI_ASSISTANT_NETSERVER", null);
        if (value == null) {
            Loge("default is null, using original servers!");
            return originalServers;
        }
        List<String> updatedServers = new ArrayList();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        Loge("updated Servers less than 2, using original servers!");
        return originalServers;
    }

    /* JADX WARNING: Missing block: B:28:0x00d7, code:
            return 599;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int sendHttpProbe(URL url) {
        HttpURLConnection urlConnection = null;
        boolean isGenerate204 = false;
        int httpResponseCode = 599;
        try {
            String hostToResolve = url.getHost();
            String host = " ";
            Logd("SHP: " + url + ", host= " + hostToResolve + ", mCurNetwork: " + this.mCurNetwork);
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
                    Logd("SHP ht:" + host + ",gw:" + gateway);
                }
                if (addresses.length == 1 && gateway != null && gateway.toString().equals(host)) {
                    this.mResponseGotFromGateway = true;
                    Logd("SHP fgw !!");
                    return 599;
                }
                this.mResponseGotFromGateway = false;
                if (getCurrentState() == this.mDisconnectState || this.mCurNetwork == null || url == null) {
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
                String redirectUrl = urlConnection.getHeaderField("location");
                Logd("SHP: " + url + ", code = " + httpResponseCode + ", conn = " + urlConnection.getHeaderField("Connection"));
                if (httpResponseCode == ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS) {
                    if (isGenerate204) {
                        if (urlConnection.getContentLength() == 0 || (urlConnection.getHeaderField("Connection") != null && urlConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive"))) {
                            Logd("SHP: empty and Keep-Alive 200 response interpreted as 204 response.");
                            httpResponseCode = 204;
                        }
                    } else if (urlConnection.getHeaderField("Connection") != null && urlConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive")) {
                        Logd("SHP: !isGenerate204, Keep-Alive 200 response treated as 204.");
                        httpResponseCode = 204;
                    }
                }
                if (httpResponseCode >= 300 && httpResponseCode <= 399 && redirectUrl != null && inSpecialUrlList(redirectUrl)) {
                    Logd("SHP: response 302 with special redirect url: " + redirectUrl);
                    httpResponseCode = 204;
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Loge("SHP: mInresult = " + this.mInterResult + ", url = " + url + ", exception: " + e);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return httpResponseCode;
    }

    private boolean sendParallelHttpProbes() {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicReference<Boolean> finalResult = new AtomicReference();
        List<String> publicServers = getPublicServers();
        Collections.shuffle(publicServers);
        List<String> intenalServers = getInternalServers();
        try {
            URL url1 = new URL((String) publicServers.get(0));
            URL url2 = new URL((String) publicServers.get(1));
            URL url3 = new URL("http://" + ((String) intenalServers.get(this.mDetectInterCount % intenalServers.size())) + "/generate_204");
            AnonymousClass1ProbeThread httpProbe1 = new AnonymousClass1ProbeThread(url1, finalResult, latch);
            AnonymousClass1ProbeThread httpProbe2 = new AnonymousClass1ProbeThread(url2, finalResult, latch);
            AnonymousClass1ProbeThread httpProbe3 = new AnonymousClass1ProbeThread(url3, finalResult, latch);
            httpProbe1.start();
            httpProbe2.start();
            httpProbe3.start();
            try {
                latch.await(40000, TimeUnit.MILLISECONDS);
                finalResult.compareAndSet(null, Boolean.valueOf(false));
                return ((Boolean) finalResult.get()).booleanValue();
            } catch (InterruptedException e) {
                Loge("Error: probe wait interrupted!");
                return false;
            }
        } catch (MalformedURLException e2) {
            Loge("Bad validation URL.");
            return false;
        }
    }

    private void detectInternet() {
        if (!this.mAutoSwitch || !this.mFeatureState || !getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
            Logd("DI, switch is off");
        } else if (!this.mDetectNet || this.mInternetDetecting) {
            Logd("DI, no need check");
        } else if (this.mWifiStateMachine.isRoaming()) {
            Logd("DI, ring");
        } else {
            this.mLastInternetResult = this.mInterResult;
            if (this.mInterThread != null) {
                this.mInterThread.post(new Runnable() {
                    public void run() {
                        String dectConfig = WifiNetworkStateTraker.this.mLastConfigkey;
                        WifiNetworkStateTraker wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                        wifiNetworkStateTraker.mDetectInterCount = wifiNetworkStateTraker.mDetectInterCount + 1;
                        WifiNetworkStateTraker.this.mLastDetectInter = System.currentTimeMillis();
                        WifiNetworkStateTraker.this.mInternetDetecting = true;
                        boolean probeResult = WifiNetworkStateTraker.this.sendParallelHttpProbes();
                        WifiNetworkStateTraker.this.mInternetDetecting = false;
                        if (dectConfig == null || dectConfig.equals(WifiNetworkStateTraker.this.mLastConfigkey)) {
                            if (probeResult) {
                                WifiNetworkStateTraker.this.mInternetInvalidCount = 0;
                                if (!WifiNetworkStateTraker.this.mInterResult) {
                                    WifiNetworkStateTraker.this.sendMessageForNetChange(true);
                                }
                            } else if (!WifiNetworkStateTraker.this.mInterResult || WifiNetworkStateTraker.this.mLastPkgInfo == null || WifiNetworkStateTraker.this.mLastPkgInfo.rssi < -77) {
                                WifiNetworkStateTraker.this.mInternetInvalidCount = 0;
                            } else if (WifiNetworkStateTraker.this.mInternetInvalidCount == 0) {
                                WifiNetworkStateTraker.this.Logd("DI, change to unvailable, detect again before set");
                                wifiNetworkStateTraker = WifiNetworkStateTraker.this;
                                wifiNetworkStateTraker.mInternetInvalidCount = wifiNetworkStateTraker.mInternetInvalidCount + 1;
                                long delayTime = System.currentTimeMillis() + ((long) WifiNetworkStateTraker.this.INTERNET_STANDOFF_TIME);
                                WifiNetworkStateTraker.this.mAlarmManager.cancel(WifiNetworkStateTraker.this.mDetectInterIntent);
                                WifiNetworkStateTraker.this.mAlarmManager.set(0, delayTime, WifiNetworkStateTraker.this.mDetectInterIntent);
                            } else {
                                WifiNetworkStateTraker.this.mInternetInvalidCount = 0;
                                WifiNetworkStateTraker.this.sendMessageForNetChange(false);
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
        if (this.mAutoSwitch && this.mFeatureState && getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", Boolean.valueOf(true)).booleanValue()) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            }
            NetworkInfo mni = this.mCM.getActiveNetworkInfo();
            if (mni != null && mni.getType() == 0) {
                Logd("needToDetectInternet, data");
                return false;
            } else if (this.mDetectNet && !this.mToData) {
                return true;
            } else {
                Logd("needToDetectInternet, no need to check");
                return false;
            }
        }
        Logd("needToDetectInternet, switch is off");
        return false;
    }

    private boolean detectTcpStatus() {
        boolean isAvailable = false;
        if (this.mTcpInfoMonitor != null) {
            this.mTcpLinkStatus = this.mTcpInfoMonitor.getCurrentTcpLinkStatus();
        }
        Logd("Before adjustment, mTcpLinkStatus = " + this.mTcpLinkStatus + " mTcpStatistics = " + this.mTcpStatistics);
        if (this.mTcpLinkStatus == 16) {
            if (this.mTcpStatistics < 0 || this.mTcpStatistics >= 1000) {
                this.mTcpStatistics = 1;
            } else {
                this.mTcpStatistics++;
            }
            if (!this.mInterResult && (this.mGotInternetResult & 2) == 2 && this.mTcpStatistics == this.AVAILABLE_STAT_COUNT - 1) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus == 18) {
            if (this.mTcpStatistics < 1000 || this.mTcpStatistics >= 1020) {
                this.mTcpStatistics = 1000;
            } else {
                this.mTcpStatistics++;
            }
            if (this.mTcpStatistics == this.POOR_STAT_COUNT + 1000) {
                triggerInternetDetect(false);
            }
        } else if (this.mTcpLinkStatus == 2 || this.mTcpLinkStatus == 3) {
            if (this.mTcpStatistics > -1000 || this.mTcpStatistics <= -2000) {
                this.mTcpStatistics = -1000;
            } else {
                this.mTcpStatistics--;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == -1000 - this.AVAILABLE_STAT_COUNT) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus == 1) {
            if (this.mTcpStatistics > 0 || this.mTcpStatistics <= -1000) {
                this.mTcpStatistics = -1;
            } else {
                this.mTcpStatistics--;
            }
            if (this.mInterResult && (this.mGotInternetResult & 1) == 1 && this.mTcpStatistics == this.AVAILABLE_STAT_COUNT * -1) {
                triggerInternetDetect(true);
            }
        } else if (this.mTcpLinkStatus != 0) {
            this.mTcpStatistics = 0;
        } else if (this.mTcpStatistics > 0) {
            this.mTcpStatistics--;
        } else if (this.mTcpStatistics < 0) {
            this.mTcpStatistics++;
        }
        Logd("After adjustment, mTcpStatistics = " + this.mTcpStatistics);
        if (this.mTcpStatistics == this.AVAILABLE_STAT_COUNT && ((this.mGotInternetResult & 4) == 4 || ((this.mGotInternetResult & 8) == 8 && (this.mGotInternetResult & 2) == 2))) {
            if ((this.mGotInternetResult & 1) == 1) {
                this.mTcpStatistics = 0;
            } else {
                isAvailable = true;
                this.mGotInternetResult |= 1;
                this.mGotInternetResult &= -3;
            }
        } else if (this.mTcpStatistics != this.UNAVAILABLE_STAT_COUNT * -1 || ((this.mGotInternetResult & 4) == 4 && (this.mGotInternetResult & 1) != 1)) {
            if (this.mTcpStatistics == -1000 - this.PORTAL_STAT_COUNT && ((this.mGotInternetResult & 8) == 8 || (!this.captivePortal && this.mGotInternetResult < 4))) {
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
        if (this.mTcpInterval == this.TCP_SAMPLE_INTERVAL_SHORT) {
            this.mTcpShortIntervalCount++;
        } else {
            this.mTcpShortIntervalCount = 0;
        }
        if (this.mTcpShortIntervalCount > this.TCP_SHORT_INTERVAL_LIMIT || this.mTcpStatistics >= this.AVAILABLE_STAT_COUNT || this.mTcpStatistics == this.UNAVAILABLE_STAT_COUNT * -1 || this.mTcpStatistics == -1000 - this.PORTAL_STAT_COUNT) {
            this.mTcpInterval = this.TCP_SAMPLE_INTERVAL_LONG;
        }
        Logd("mGotInternetResult = " + this.mGotInternetResult + " isAvailable = " + isAvailable);
        return isAvailable;
    }

    private boolean tcpStatSetInternetResult(boolean available) {
        boolean setSuccess = false;
        Logd("tcpStatSetInternetResult, available = " + available);
        WifiInfo tcpWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
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
        WifiConfiguration rmconfig = this.mWifiConfigManager.getWifiConfiguration(netId);
        if (rmconfig == null) {
            Logd("removeNetworkAvailable rmconfig == null");
            return;
        }
        if (this.mLastConfigkey != null && this.mLastConfigkey.equals(rmconfig.configKey(false)) && (getCurrentState() == this.mNetworkMonitorState || getCurrentState() == this.mConnectedState || getCurrentState() == this.mVerifyInternetState)) {
            long rmNetworkTime = System.currentTimeMillis();
            long rmForWlan = rmNetworkTime - this.mAutoConnWlanTime;
            if (this.mAutoConnWlanTime > 0 && rmForWlan > 0 && rmForWlan < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                Logd("stc wwb3");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_WLAN);
                this.mAutoConnWlanTime = 0;
            }
            long rmForData = rmNetworkTime - this.mAutoDataToWlanTime;
            if (!this.mToData && this.mAutoDataToWlanTime > 0 && rmForData > 0 && rmForData < ((long) this.AUTO_CONN_ERR_THRESHOLD)) {
                Logd("stc dwb2");
                setAssistantStatistics(STATISTIC_MANUAL_LIMIT, TYPE_REMOVE_NETWORK_FOR_DATA);
                this.mAutoDataToWlanTime = 0;
            }
            if (!this.mToData) {
                Logd("stc wdc2");
                setAssistantStatistics(STATISTIC_MANUAL_OPERATE, TYPE_REMOVE_NETWORK_FOR_DATA);
            }
        }
        updateRecordWifiConfig(rmconfig.configKey(false));
    }

    /* JADX WARNING: Missing block: B:21:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rmOrupdateRecordStatus(String key, boolean remove) {
        Loge("rmoRst key:" + key);
        if (key != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(key)) {
                    WifiNetworkRecord record = (WifiNetworkRecord) this.mWifiNetworkRecord.get(key);
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

    /* JADX WARNING: Missing block: B:16:0x0035, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resetConnExp(String key) {
        Loge("rtce key:" + key);
        if (key != null) {
            synchronized (this.mWifiNetworkRecord) {
                if (this.mWifiNetworkRecord.containsKey(key)) {
                    WifiNetworkRecord reSetRecord = (WifiNetworkRecord) this.mWifiNetworkRecord.get(key);
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
            Loge("LinkProperties is null, return");
            return false;
        }
        LinkProperties mLp = lp;
        InetAddress mCurrentGateway = null;
        for (RouteInfo route : lp.getRoutes()) {
            if (route.hasGateway()) {
                mCurrentGateway = route.getGateway();
            }
        }
        if (mCurrentGateway == null) {
            Loge("InetAddress getGateway is null, return");
            return false;
        }
        boolean isSoft;
        Logd("mCurrentGateway : " + mCurrentGateway.toString());
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
            Logd("ita = " + this.mOperateUid);
            if (this.mOperateUid == 1000 || this.mOperateUid == 1010) {
                z = false;
            }
            return z;
        }
        Logd("rd close it.");
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

    private void showDialog(WifiNetworkRecord lastRecord) {
        Logd("sd muk:" + this.mUnavailableKey);
        if (this.mAlertDialog != null) {
            Logd("repeated sd");
        } else if (lastRecord == null) {
            Loge("record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            Loge("record parameter maybe null");
        } else if (getCurrentState() != this.mConnectedState && getCurrentState() != this.mNetworkMonitorState) {
            Logd("don not sd");
        } else if (this.mUnavailableKey == null || !this.mUnavailableKey.equals(lastRecord.mConfigkey)) {
            String title;
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = lastRecord.mWifiConfiguration.SSID;
            Builder builder = new Builder(this.mContext, 201523207);
            if (this.mManualConnect) {
                if (lastRecord.mNetworkValid) {
                    title = ssid + this.mContext.getText(17040911);
                } else {
                    title = ssid + this.mContext.getText(17040910);
                }
                title = title + this.mContext.getText(17040912);
                builder.setPositiveButton(17040918, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (WifiNetworkStateTraker.this.mAlertDialog != null) {
                            WifiNetworkStateTraker.this.mAlertDialog.dismiss();
                            WifiNetworkStateTraker.this.mManualDialogAutoDismissTime = System.currentTimeMillis();
                        }
                        WifiNetworkStateTraker.this.mManualConnect = false;
                        WifiNetworkStateTraker.this.mClickDialogSwitch = true;
                        WifiNetworkStateTraker.this.detectNetworkAvailable(null, 0, WifiNetworkStateTraker.INVALID_INFO, false);
                    }
                });
                builder.setNegativeButton(17040919, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (WifiNetworkStateTraker.this.mAlertDialog != null) {
                            WifiNetworkStateTraker.this.mAlertDialog.dismiss();
                            WifiNetworkStateTraker.this.mManualDialogAutoDismissTime = 0;
                        }
                    }
                });
            } else {
                title = (ssid + this.mContext.getText(17040910)) + this.mContext.getText(17040916);
                builder.setPositiveButton(17040917, new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (WifiNetworkStateTraker.this.mAlertDialog != null) {
                            WifiNetworkStateTraker.this.mAlertDialog.dismiss();
                            WifiNetworkStateTraker.this.mManualDialogAutoDismissTime = 0;
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
                    WifiNetworkStateTraker.this.mAlertDialog = null;
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
                Loge("textview is null");
            }
        } else {
            Logd("record is same");
        }
    }

    private void showDialogFordata(WifiNetworkRecord lastRecord) {
        if (lastRecord == null) {
            Loge("sdf record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            Loge("sdf record parameter maybe null");
        } else if (getCurrentState() != this.mNetworkMonitorState) {
            Logd("sdf state error");
        } else {
            String title;
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
            if (lastRecord.mNetworkValid) {
                title = ssid + this.mContext.getText(17040911);
            } else {
                title = ssid + this.mContext.getText(17040910);
            }
            builder.setTitle(title + this.mContext.getText(17040913));
            mMessage.setText(this.mContext.getText(17040914));
            builder.setView(contentView);
            builder.setPositiveButton(17040917, new OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    System.putInt(WifiNetworkStateTraker.this.mContext.getContentResolver(), WifiNetworkStateTraker.NOT_REMIND_WIFI_ASSISTANT, mAlertBox.isChecked() ? 1 : 0);
                    if (WifiNetworkStateTraker.this.mAlertDialog != null) {
                        WifiNetworkStateTraker.this.mAlertDialog.dismiss();
                        WifiNetworkStateTraker.this.mManualDialogAutoDismissTime = 0;
                    }
                }
            });
            this.mAlertDialog = builder.create();
            this.mAlertDialog.setCanceledOnTouchOutside(false);
            this.mAlertDialog.setCancelable(false);
            this.mAlertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    WifiNetworkStateTraker.this.mAlertDialog = null;
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

    private void showDialogFordataNewtype(WifiNetworkRecord lastRecord) {
        if (lastRecord == null) {
            Loge("sdfn record maybe null");
        } else if (lastRecord.mWifiConfiguration == null) {
            Loge("sdfn record parameter maybe null");
        } else if (getCurrentState() != this.mNetworkMonitorState) {
            Logd("sdfn state error");
        } else {
            String title;
            this.mUnavailableKey = lastRecord.mConfigkey;
            String ssid = lastRecord.mWifiConfiguration.SSID;
            if (this.mAlertDialog != null) {
                this.mAlertDialog.dismiss();
            }
            if (this.mDataAlertDialog != null) {
                this.mDataAlertDialog.dismiss();
            }
            ColorSystemUpdateDialog.Builder builder = new ColorSystemUpdateDialog.Builder(this.mContext, 201523207);
            CharSequence[] items = new CharSequence[]{this.mContext.getText(17040915), this.mContext.getText(17040917)};
            if (lastRecord.mNetworkValid) {
                title = ssid + this.mContext.getText(17040911);
            } else {
                title = ssid + this.mContext.getText(17040910);
            }
            builder.setTitle(title + this.mContext.getText(17040913));
            builder.setMessage(this.mContext.getText(17040914));
            builder.setItems(items, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            WifiNetworkStateTraker.this.Logd("sdfn no remind");
                            System.putInt(WifiNetworkStateTraker.this.mContext.getContentResolver(), WifiNetworkStateTraker.NOT_REMIND_WIFI_ASSISTANT, 1);
                            if (WifiNetworkStateTraker.this.mDataAlertDialog != null) {
                                WifiNetworkStateTraker.this.mDataAlertDialog.dismiss();
                                return;
                            }
                            return;
                        case 1:
                            if (WifiNetworkStateTraker.this.mDataAlertDialog != null) {
                                WifiNetworkStateTraker.this.mDataAlertDialog.dismiss();
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
                    WifiNetworkStateTraker.this.mDataAlertDialog = null;
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
        Logd("hasCheck = " + hasCheck);
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
        Logd("dtt: " + distanceTime);
        if (distanceTime > ((long) getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_MANUAL_DIALOG_TIME", Integer.valueOf(STAITIC_MANUAL_DIALOG_TIME_THRESHOLD)).intValue())) {
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
            Loge("matchKeymgmt default");
        } else if (!(scanKey.contains("PSK") || scanKey.contains("EAP") || scanKey.contains(SECURITY_WEP) || scanKey.contains("WAPI") || scanKey.contains("IEEE8021X"))) {
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
        if (config.allowedKeyManagement.get(6)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(7)) {
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
        Logd("slqb for ds:" + score + ", dt:" + detect);
        Intent qualityIntent = new Intent(WIFI_SCROE_CHANGE);
        qualityIntent.addFlags(67108864);
        qualityIntent.putExtra(EXTRA_ENALE_DATA, detect);
        qualityIntent.putExtra(EXTRA_SCORE, score);
        this.mContext.sendStickyBroadcastAsUser(qualityIntent, UserHandle.ALL);
    }

    private void sendNetworkStateBroadCast(String configkey, boolean valid) {
        Intent netIntent = new Intent(WIFI_NETWORK_CHANGE);
        netIntent.addFlags(67108864);
        netIntent.putExtra(EXTRA_WIFI_INVALID, !valid);
        Logd("snb " + valid);
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
        Logd("svb");
        this.mContext.sendStickyBroadcastAsUser(verifyIntent, UserHandle.ALL);
    }

    private void setAssistantStatistics(String eventId, String type) {
        setAssistantStatistics(eventId, type, null, null, INVALID_INFO);
    }

    private void setAssistantStatistics(String eventId, String type, WifiNetworkRecord cw, WifiNetworkRecord sw, int extra1) {
        if (this.mAutoSwitch) {
            HashMap<String, String> map = new HashMap();
            map.put(eventId, type);
            if (cw != null) {
                String currentInfo = makeDumpInfo(cw);
                if (!TextUtils.isEmpty(currentInfo)) {
                    if (extra1 > INVALID_INFO) {
                        currentInfo = currentInfo + ";" + Integer.toString(extra1);
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

    private String makeDumpInfo(WifiNetworkRecord info) {
        if (info == null) {
            return null;
        }
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(info.mNetid).append(";");
        sbuf.append(info.mConfigkey).append(";");
        sbuf.append(info.mRssi).append(";");
        sbuf.append(info.mScore).append(";");
        sbuf.append(info.mNetworkValid).append(";");
        for (int i = 0; i < info.mNetQualitys.length; i++) {
            sbuf.append(info.mNetQualitys[i]);
            if (i + 1 != info.mNetQualitys.length) {
                sbuf.append(",");
            }
        }
        return sbuf.toString();
    }

    private void Logd(String log) {
        if (this.DBG) {
            Log.d(TAG, "" + log);
        }
    }

    private void Loge(String log) {
        Log.e(TAG, "" + log);
    }
}
