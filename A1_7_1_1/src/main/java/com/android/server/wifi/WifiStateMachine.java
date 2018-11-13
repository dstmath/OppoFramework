package com.android.server.wifi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.dhcp.DhcpClient;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.PPPOEInfo.Status;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.OppoAssertTip;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.location.GpsMonitor;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiNative.WifiRssiEventHandler;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import com.android.server.wifi.util.TelephonyUtil;
import com.google.protobuf.nano.Extension;
import com.mediatek.aee.ExceptionLog;
import com.mediatek.common.MPlugin;
import com.mediatek.common.wifi.IWifiFwkExt;
import com.oppo.RomUpdateHelper;
import com.oppo.oiface.OifaceUtil;
import com.oppo.oiface.OifaceUtil.NetType;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiStateMachine extends StateMachine implements WifiRssiEventHandler {
    public static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String ACTION_RESTART_WIFI = "com.android.server.WifiManager.action.RESTART_WIFI";
    private static final String ACTION_START_WIFI = "com.android.server.WifiManager.action.START_WIFI";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final int ADD_OR_UPDATE_SOURCE = -3;
    private static final int BACKGROUND_SCAN_RESULTS_INTERVAL = 2000;
    static final int BASE = 131072;
    static final int BASE_OPPO = 131352;
    private static final String BOUNDARY = "-----------hello word-----------\r\n";
    private static final int BUFFER_LENGTH = 40;
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_PASSPOINT_MO = 131174;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    static final int CMD_AUTO_CONNECT = 131215;
    static final int CMD_AUTO_JOIN_OPTIMIAZE = 131372;
    static final int CMD_AUTO_ROAM = 131217;
    static final int CMD_AUTO_SAVE_NETWORK = 131218;
    static final int CMD_BLACKLIST_NETWORK = 131128;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    public static final int CMD_BOOT_COMPLETED = 131206;
    private static final int CMD_CHECK_FOR_EXPAPAUTO = 131358;
    private static final int CMD_CHECK_INTERNET_ACCESS = 131353;
    static final int CMD_CLEAR_BLACKLIST = 131129;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DELAYED_NETWORK_DISCONNECT = 131159;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    public static final int CMD_DISABLE_P2P_REQ = 131204;
    public static final int CMD_DISABLE_P2P_RSP = 131205;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_DRIVER_START_TIMED_OUT = 131091;
    static final int CMD_ENABLE_ALL_NETWORKS = 131127;
    static final int CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED = 131239;
    static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_P2P = 131203;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_FIRMWARE_ALERT = 131172;
    static final int CMD_GET_CAPABILITY_FREQ = 131132;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_CONNECTION_STATISTICS = 131148;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    static final int CMD_MODIFY_PASSPOINT_MO = 131175;
    private static final int CMD_MTU_PROBER = 131357;
    static final int CMD_NETWORK_STATUS = 131220;
    static final int CMD_NO_NETWORKS_PERIODIC_SCAN = 131160;
    static final int CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER = 131165;
    static final int CMD_PING_SUPPLICANT = 131123;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_RELOAD_TLS_AND_RECONNECT = 131214;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_NETWORK = 131125;
    static final int CMD_REMOVE_USER_CONFIGURATIONS = 131224;
    static final int CMD_RESET_SIM_NETWORKS = 131173;
    static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    static final int CMD_RSSI_POLL = 131155;
    static final int CMD_RSSI_THRESHOLD_BREACH = 131236;
    static final int CMD_SAVE_CONFIG = 131130;
    static final int CMD_SCREEN_STATE_CHANGED = 131167;
    static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    static final int CMD_SET_FREQUENCY_BAND = 131162;
    static final int CMD_SET_HIGH_PERF_MODE = 131149;
    static final int CMD_SET_OPERATIONAL_MODE = 131144;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_DRIVER = 131085;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    static final int CMD_START_SCAN = 131143;
    static final int CMD_START_SUPPLICANT = 131083;
    static final int CMD_STATIC_IP_FAILURE = 131088;
    static final int CMD_STATIC_IP_SUCCESS = 131087;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_DRIVER = 131086;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    static final int CMD_STOP_SUPPLICANT = 131084;
    static final int CMD_STOP_SUPPLICANT_FAILED = 131089;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    private static final int CMD_TRIGGER_RESTORE_DELAY = 131361;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION = 131230;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    static final int CMD_USER_SWITCH = 131237;
    private static final int CMD_WAIT_SCAN_RESULTS = 131354;
    public static final int CONNECT_MODE = 1;
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    private static final int CONNECT_TIMEOUT_MSEC = 3000;
    private static final String CUSTOMIZED_SCAN_SETTING = "customized_scan_settings";
    private static final String CUSTOMIZED_SCAN_WORKSOURCE = "customized_scan_worksource";
    private static final int DATA_HEAD_LEN = 110;
    private static boolean DBG = false;
    private static final boolean DEBUG_PARSE = false;
    public static final String DEBUG_PROPERTY = "persist.sys.assert.panic";
    private static final String DEFAULT_BLACK_LIST = "fn5.blacklist.reserve";
    private static final String DEFAULT_LOCATION_APP = "map,navi";
    private static final String DEFAULT_SYSTEM_APP = "com.google,com.android,android.net";
    public static final int DFS_RESTRICTED_SCAN_REQUEST = -6;
    private static final int DISABLE_INTERFACE = -1;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int DOUBLE_SIM = 3;
    private static final int DRIVER_START_TIME_OUT_MSECS = 10000;
    private static final int ENABLE_WIFI = -5;
    private static final int EVENT_PPPOE_SUCCEEDED = 131323;
    private static final int EVENT_START_PPPOE = 131322;
    private static final int EVENT_UPDATE_DNS = 131324;
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    private static final String EXTRA_NETWORK_STATE = "netState";
    private static final int FAILURE = -1;
    private static final String GOOGLE_OUI = "DA-A1-19";
    private static final int IDLE_DISCONN_FREQ = 35;
    private static final int IDLE_GROUP_FREQ = 20;
    private static final int IDLE_RENEW_FREQ = 40;
    private static final int IDLE_SCAN_FREQ = 3;
    private static final int INVALID_NETWORK_ID = -1;
    static final int IP_REACHABILITY_MONITOR_TIMER_MSEC = 10000;
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    private static final int MAX_RSSI = 256;
    private static final int MCC_SUB_BEG = 0;
    private static boolean MDBG = false;
    private static int MESSAGE_HANDLING_STATUS_DEFERRED = 0;
    private static int MESSAGE_HANDLING_STATUS_DISCARD = 0;
    private static int MESSAGE_HANDLING_STATUS_FAIL = 0;
    private static int MESSAGE_HANDLING_STATUS_HANDLING_ERROR = 0;
    private static int MESSAGE_HANDLING_STATUS_LOOPED = 0;
    private static int MESSAGE_HANDLING_STATUS_OBSOLETE = 0;
    private static int MESSAGE_HANDLING_STATUS_OK = 0;
    private static int MESSAGE_HANDLING_STATUS_PROCESSED = 0;
    private static int MESSAGE_HANDLING_STATUS_REFUSED = 0;
    private static int MESSAGE_HANDLING_STATUS_UNKNOWN = 0;
    private static final int MIN_INTERVAL_ENABLE_ALL_NETWORKS_MS = 600000;
    private static final int MIN_RSSI = -200;
    private static final int MNC_SUB_BEG = 3;
    private static final int MNC_SUB_END = 5;
    private static final int M_CMD_DO_CTIA_TEST_OFF = 131283;
    private static final int M_CMD_DO_CTIA_TEST_ON = 131282;
    private static final int M_CMD_DO_CTIA_TEST_RATE = 131284;
    private static final int M_CMD_ENABLE_EAP_SIM_CONFIG_NETWORK = 131297;
    private static final int M_CMD_FACTORY_RESET = 131294;
    private static final int M_CMD_FLUSH_BSS = 131292;
    private static final int M_CMD_GET_CONNECTING_NETWORK_ID = 131248;
    private static final int M_CMD_GET_DISCONNECT_FLAG = 131260;
    private static final int M_CMD_GET_TEST_ENV = 131293;
    private static final int M_CMD_GET_WIFI_STATUS = 131288;
    private static final int M_CMD_IP_REACHABILITY_MONITOR_TIMER = 131307;
    private static final int M_CMD_NOTIFY_CONNECTION_FAILURE = 131262;
    private static final int M_CMD_SET_POWER_SAVING_MODE = 131289;
    private static final int M_CMD_SET_RSSI = 131308;
    private static final int M_CMD_SET_TDLS_POWER_SAVE = 131295;
    private static final int M_CMD_SET_TX_POWER = 131287;
    private static final int M_CMD_SET_TX_POWER_ENABLED = 131286;
    private static final int M_CMD_SET_WOWLAN_MAGIC_MODE = 131291;
    private static final int M_CMD_SET_WOWLAN_NORMAL_MODE = 131290;
    private static final int M_CMD_UPDATE_BGSCAN = 131285;
    private static final int M_CMD_UPDATE_COUNTRY_CODE = 131244;
    private static final int M_CMD_UPDATE_RSSI = 131249;
    private static final int M_CMD_UPDATE_SCAN_INTERVAL = 131243;
    private static final int M_CMD_UPDATE_SCAN_STRATEGY = 131296;
    private static final int M_CMD_UPDATE_SETTINGS = 131242;
    private static final int NAVIGATION_MODE_FALSE = 2;
    private static final int NAVIGATION_MODE_INVALID = -1;
    private static final int NAVIGATION_MODE_TRUE = 1;
    private static final String NETWORKTYPE = "WIFI";
    private static final String NETWORKTYPE_UNTRUSTED = "WIFI_UT";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    private static final int NONE_SIM = 0;
    public static final short NUM_LOG_RECS_NORMAL = (short) 100;
    public static final short NUM_LOG_RECS_VERBOSE = (short) 3000;
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = (short) 200;
    static final int OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC = 40000;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static final int POLL_RSSI_INTERVAL_MSECS = 3000;
    private static final int PPPOE_NETID = 65500;
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    private static final int SCAN_COUNT_MAX = 3;
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    static final long SCAN_PERMISSION_UPDATE_THROTTLE_MILLI = 20000;
    private static final int SCAN_REQUEST = 0;
    private static final int SCAN_REQUEST_BUFFER_MAX_SIZE = 10;
    private static final String SCAN_REQUEST_TIME = "scan_request_time";
    private static final int SCREEN_TIME = 20000;
    private static final int SET_ALLOW_UNTRUSTED_SOURCE = -4;
    private static final int SIGNAL_HISTORY_COUNTS = 5;
    private static final int SINGLE_SIM_1 = 1;
    private static final int SINGLE_SIM_2 = 2;
    private static final long SMART_SCAN_INTERVAL = 5000;
    private static final int SUCCESS = 1;
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;
    private static final int SUPPLICANT_RESTART_TRIES = 5;
    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    private static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String TAG = "WifiStateMachine";
    private static final int TETHER_NOTIFICATION_TIME_OUT_MSECS = 5000;
    private static final int TRIGGER_RESTORE_DELAY_TIME = 2000;
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static final int UPDATE_DNS_DELAY_MS = 500;
    private static boolean USE_PAUSE_SCANS = false;
    private static final float WEIGHT_DISCONN = 0.0f;
    private static final float WEIGHT_GROUP = 0.03f;
    private static final float WEIGHT_RENEW = 0.5f;
    private static final float WEIGHT_SCAN = 0.0f;
    private static final String WIFI_5G_BAND_SUPPORT = "wifi_5g_band_support";
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final boolean WIFI_DBG = false;
    public static final WorkSource WIFI_WORK_SOURCE = null;
    private static boolean bManualConnect = false;
    private static HashMap<String, DhcpResults> mDhcpResultMap = null;
    private static boolean mIsManuConnect = false;
    private static WifiConfiguration mManuConnectConfiguration = null;
    private static int mManuConnectNetId = 0;
    private static Random mRandom = null;
    private static int nNetId = 0;
    private static final int sFrameworkMinScanIntervalSaneValue = 10000;
    private static final Class[] sMessageClasses = null;
    private static int sScanAlarmIntentCount;
    private static final SparseArray<String> sSmToString = null;
    private static boolean wConnected;
    private static boolean wConnection;
    private static boolean wStartWps;
    private int DISCONN_FLAG;
    private int GROUP_FLAG;
    private int RENEW_FLAG;
    private int SCAN_FLAG;
    private final int SILENCE_RESTART_SOURCE;
    public String SingtelWIFI;
    private int THIRD_APP_SCAN_COUNT;
    private int THIRD_APP_SCAN_FREQ;
    public String WirelessSGx;
    private boolean autoSwitch;
    private boolean changesim;
    private boolean closedByRestartInSilence;
    private int currentSim;
    private boolean didBlackListBSSID;
    int disconnectingWatchdogCount;
    public AtomicBoolean enableIpReachabilityMonitor;
    public AtomicBoolean enableIpReachabilityMonitorEnhancement;
    private boolean enableSpoofScanResults;
    private int fetchPKTCount;
    int ipReachabilityMonitorCount;
    private boolean isSingtelConnecting;
    private long lastConnectAttemptTimestamp;
    private WifiConfiguration lastForgetConfigurationAttempt;
    private long lastLinkLayerStatsUpdate;
    private long lastOntimeReportTimeStamp;
    private String lastRecord;
    private WifiConfiguration lastSavedConfigurationAttempt;
    private Set<Integer> lastScanFreqs;
    private long lastScreenStateChangeTimeStamp;
    private boolean linkDebouncing;
    private boolean mAffectRoaming;
    private int mAggressiveHandover;
    private AlarmManager mAlarmManager;
    private ValueAnimator mAnimator;
    private int mAppScanTimes;
    private OppoAssertTip mAssertProxy;
    private Handler mAsyncHandler;
    private boolean mAutoRoaming;
    private final boolean mBackgroundScanSupported;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive;
    private final Queue<Message> mBufferedScanMsg;
    private final BuildProperties mBuildProperties;
    private State mCaptiveState;
    private int mCheckInetAccessSeq;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState;
    private boolean mConnectNetwork;
    private int mConnectedId;
    boolean mConnectedModeGScanOffloadStarted;
    private State mConnectedState;
    @GuardedBy("mWifiReqCountLock")
    private int mConnectionReqCount;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    private int mCurSmoothRssi;
    private final int mDefaultFrameworkScanIntervalMs;
    private State mDefaultState;
    private final NetworkCapabilities mDfltNetworkCapabilities;
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock;
    private int mDisconnectNetworkId;
    private boolean mDisconnectOperation;
    private State mDisconnectedState;
    private long mDisconnectedTimeStamp;
    private State mDisconnectingState;
    private final AtomicBoolean mDontReconnect;
    private final AtomicBoolean mDontReconnectAndScan;
    private boolean mDriverRoaming;
    private int mDriverStartToken;
    private State mDriverStartedState;
    private State mDriverStartingState;
    private State mDriverStoppedState;
    private State mDriverStoppingState;
    private boolean mEnableRssiPolling;
    private boolean mEnableRssiSmoothing;
    private FrameworkFacade mFacade;
    private AtomicInteger mFrequencyBand;
    private boolean mFromKeylogVerbose;
    private long mGScanPeriodMilli;
    private long mGScanStartTimeMilli;
    private Handler mHandler;
    private boolean mHasInternetAccess;
    private boolean mHotspotOptimization;
    private int mIdleDisConnTimes;
    private int mIdleGroupTimes;
    private int mIdleRenewTimes;
    private int mIdleScanTimes;
    private int mIndex;
    private State mInitialState;
    private final String mInterfaceName;
    private boolean mIpConfigLost;
    private final IpManager mIpManager;
    private boolean mIsFullScanOngoing;
    boolean mIsListeningIpReachabilityLost;
    private boolean mIsNewAssociatedBssid;
    private boolean mIsRunning;
    private boolean mIsScanOngoing;
    private State mL2ConnectedState;
    private String mLastBssid;
    private long mLastCheckWeakSignalTime;
    private DetailedState mLastDetailedState;
    private long mLastDriverRoamAttempt;
    private long mLastEnableAllNetworksTime;
    private int mLastExplicitNetworkId;
    private int mLastNetworkId;
    private final WorkSource mLastRunningWifiUids;
    long mLastScanPermissionUpdate;
    private long mLastScanTime;
    private long mLastSelectEvtTimeStamp;
    private int mLastSignalLevel;
    private SupplicantState mLastSupplicantState;
    private LinkProperties mLinkProperties;
    private boolean mMtkCtpppoe;
    private WifiNetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilitiesFilter;
    private boolean mNetworkDetectValid;
    private WifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc;
    private long mNextAnimate;
    private final int mNoNetworksPeriodicScan;
    private int mNumScanResultsKnown;
    private int mNumScanResultsReturned;
    private INetworkManagementService mNwService;
    private State mObtainingIpState;
    private int mOnTime;
    private int mOnTimeLastReport;
    private int mOnTimeScreenStateChange;
    private long mOnlineStartTime;
    private PendingIntent mOpenWifiIntent;
    private int mOperationalMode;
    private final AtomicBoolean mP2pConnected;
    private final boolean mP2pSupported;
    private int mPeriodicScanToken;
    private SupplicantState mPowerState;
    private PPPOEConfig mPppoeConfig;
    private PppoeHandler mPppoeHandler;
    private PPPOEInfo mPppoeInfo;
    private LinkProperties mPppoeLinkProperties;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel;
    private boolean mReportedRunning;
    private PendingIntent mRestartIntent;
    private boolean mRetry;
    private int mRetryCount;
    private int mRoamFailCount;
    private State mRoamingState;
    private int[] mRssiArray;
    private int mRssiCount;
    private int mRssiPollToken;
    private byte[] mRssiRanges;
    private int mRssiSmoothingThreshold;
    int mRunningBeaconCount;
    private final WorkSource mRunningWifiUids;
    private int mRxTime;
    private int mRxTimeLastReport;
    private int mScanCount;
    private boolean mScanForWeakSignal;
    private PendingIntent mScanIntent;
    private State mScanModeState;
    private List<ScanDetail> mScanResults;
    private final Object mScanResultsLock;
    private WorkSource mScanWorkSource;
    private AtomicBoolean mScreenBroadcastReceived;
    private long mScreenOffTime;
    private boolean mScreenOn;
    private long mScreenOnTime;
    private boolean mSendScanResultsBroadcast;
    private boolean mShowReselectDialog;
    private String mSim1IccState;
    private String mSim2IccState;
    private State mSoftApState;
    private final AtomicBoolean mStopScanStarted;
    private int mSupplicantRestartCount;
    private long mSupplicantScanIntervalMs;
    private State mSupplicantStartedState;
    private State mSupplicantStartingState;
    private SupplicantStateTracker mSupplicantStateTracker;
    private int mSupplicantStopFailureToken;
    private State mSupplicantStoppingState;
    private int mSuspendOptNeedsDisabled;
    private WakeLock mSuspendWakeLock;
    private int mSystemUiUid;
    private int mTagetRssi;
    private int mTargetNetworkId;
    private String mTargetRoamBSSID;
    private final String mTcpBufferSizes;
    private boolean mTemporarilyDisconnectWifi;
    private String mTetherInterfaceName;
    private int mTetherToken;
    private int mTxTime;
    private int mTxTimeLastReport;
    ArrayList<Integer> mUidList;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    @GuardedBy("mWifiReqCountLock")
    private int mUntrustedReqCount;
    private AtomicBoolean mUserWantsSuspendOpt;
    private boolean mUsingPppoe;
    private int mVerboseLoggingLevel;
    private State mWaitForP2pDisableState;
    private WakeLock mWakeLock;
    private String[] mWhiteListedSsids;
    private WifiApConfigStore mWifiApConfigStore;
    private final AtomicInteger mWifiApState;
    private WifiCfgUpdateHelper mWifiCfgUpdateHelper;
    private WifiConfigManager mWifiConfigManager;
    private WifiConnectionStatistics mWifiConnectionStatistics;
    private WifiConnectivityManager mWifiConnectivityManager;
    private IWifiFwkExt mWifiFwkExt;
    private final WifiInfo mWifiInfo;
    private WifiInjector mWifiInjector;
    private WifiLastResortWatchdog mWifiLastResortWatchdog;
    private int mWifiLinkLayerStatsSupported;
    private BaseWifiLogger mWifiLogger;
    private WifiManager mWifiManager;
    private WifiMetrics mWifiMetrics;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private WifiNetworkAvailable mWifiNetworkAvailable;
    private WifiNetworkStateTraker mWifiNetworkStateTraker;
    private int mWifiOnScanCount;
    private AsyncChannel mWifiP2pChannel;
    private WifiP2pServiceImpl mWifiP2pServiceImpl;
    private WifiQualifiedNetworkSelector mWifiQualifiedNetworkSelector;
    private final Object mWifiReqCountLock;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;
    private WifiScanner mWifiScanner;
    WifiScoreReport mWifiScoreReport;
    private final AtomicInteger mWifiState;
    private State mWpsRunningState;
    private final String mapKey;
    private int messageHandlingStatus;
    int obtainingIpWatchdogCount;
    private RssiPacketCountInfo pktInfo;
    private int resetAlarmCount;
    int roamWatchdogCount;
    private int sLastNetworkId;
    private boolean scanResultsAvailable;
    private WifiConfiguration targetWificonfiguration;
    private boolean testNetworkDisconnect;
    private int testNetworkDisconnectCounter;

    class CaptiveState extends State {
        CaptiveState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                String key = "";
                if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                    key = WifiStateMachine.this.getCurrentWifiConfiguration().configKey();
                }
                WifiStateMachine.this.log("enter cpt netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key);
            }
            WifiStateMachine.this.mConnectedId = WifiStateMachine.this.mLastNetworkId;
            if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                WifiStateMachine.this.mWifiNetworkStateTraker.setNetworkDetailState(WifiStateMachine.this.mLastNetworkId, DetailedState.CAPTIVE_PORTAL_CHECK, WifiStateMachine.this.mLastBssid);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.CAPTIVE_PORTAL_CHECK);
            WifiStateMachine.this.clearCurrentConfigBSSID("CaptiveState");
        }

        /* JADX WARNING: Missing block: B:6:0x002c, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:26:0x00f9, code:
            return true;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            if (message == null) {
                WifiStateMachine.this.logd("message is null,ignore!!");
                return true;
            }
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    int enableNetId = message.arg1;
                    boolean disableOthers = 1 == message.arg2;
                    int curNetId = -1;
                    if (WifiStateMachine.this.mWifiInfo != null) {
                        curNetId = WifiStateMachine.this.mWifiInfo.getNetworkId();
                    }
                    WifiStateMachine.this.logd("enableNetId = " + enableNetId + " disableOthers = " + disableOthers + " curNetId = " + curNetId);
                    if (disableOthers && enableNetId != -1 && enableNetId == curNetId) {
                        WifiStateMachine.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(true, true, 1000);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(enableNetId);
                        }
                        WifiStateMachine.nNetId = enableNetId;
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    return false;
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.updateNetworkConnectResult(WifiStateMachine.this.mLastNetworkId, false);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    ScanResult candidate = message.obj;
                    String bssid = WifiLastResortWatchdog.BSSID_ANY;
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    int netId = message.arg1;
                    if (netId != -1) {
                        config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                        WifiStateMachine.this.setTargetBssid(config, bssid);
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        WifiStateMachine.this.logd("CMD_AUTO_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config.configKey() + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                        WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (currentConfig == null || !currentConfig.isLinked(config)) {
                            WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                        } else {
                            WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 2);
                        }
                        if (!WifiStateMachine.this.deferForUserInput(message, netId, false)) {
                            if (WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId).userApproved != 2) {
                                boolean ret = false;
                                int netIdFromWifiInfo = -1;
                                if (WifiStateMachine.this.mWifiInfo != null) {
                                    netIdFromWifiInfo = WifiStateMachine.this.mWifiInfo.getNetworkId();
                                }
                                if (WifiStateMachine.this.linkDebouncing && netIdFromWifiInfo != -1 && netIdFromWifiInfo != WifiStateMachine.this.mLastNetworkId) {
                                    WifiStateMachine.this.handleNetworkDisconnect();
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                                    WifiStateMachine.this.mWifiNative.reassociate();
                                    break;
                                }
                                if (WifiStateMachine.this.mLastNetworkId == netId) {
                                    ret = WifiStateMachine.this.mWifiNative.reassociate();
                                } else if (WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, false, -1) && WifiStateMachine.this.mWifiNative.reconnect()) {
                                    ret = true;
                                }
                                if (!ret) {
                                    WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                                    WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                                    WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                    break;
                                }
                                WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                WifiStateMachine.this.mAutoRoaming = true;
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                                break;
                            }
                            WifiStateMachine.this.replyToMessage(message, 151554, 9);
                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                            break;
                        }
                        WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                        break;
                    }
                    WifiStateMachine.this.loge("AUTO_ROAM and no config, bail out...");
                    break;
                    break;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    long lastRoam = 0;
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = System.currentTimeMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiLogger.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (!WifiStateMachine.this.mScreenOn || WifiStateMachine.this.linkDebouncing || config == null || !config.getNetworkSelectionStatus().isNetworkEnabled() || WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config) || ((message.arg2 == 3 && (lastRoam <= 0 || lastRoam >= 2000)) || ((!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= -73) && (!ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mWifiConfigManager.mThresholdQualifiedRssi5.get())))) {
                        if (WifiStateMachine.DBG) {
                            String str;
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in simulate connected state BSSID=").append(WifiStateMachine.this.mWifiInfo.getBSSID()).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.linkDebouncing).append(" reason=").append(message.arg2).append(" Network Selection Status=");
                            if (config == null) {
                                str = "Unavailable";
                            } else {
                                str = config.getNetworkSelectionStatus().getNetworkStatusString();
                            }
                            wifiStateMachine.log(append.append(str).toString());
                            break;
                        }
                    }
                    WifiStateMachine.this.startScanForConfiguration(WifiStateMachine.this.getCurrentWifiConfiguration());
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC", Integer.valueOf(WifiStateMachine.LINK_FLAPPING_DEBOUNCE_MSEC)).intValue());
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in simulate connected state BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " RSSI=" + WifiStateMachine.this.mWifiInfo.getRssi() + " freq=" + WifiStateMachine.this.mWifiInfo.getFrequency() + " reason=" + message.arg2 + " -> debounce");
                        break;
                    }
                    break;
                case 151553:
                    int conId = message.arg1;
                    boolean ignoreDisconnect = false;
                    WifiConfiguration conConfig = message.obj;
                    WifiStateMachine.this.logd("conid=" + conId + ", getid= " + WifiStateMachine.this.mWifiInfo.getNetworkId());
                    if (conConfig != null) {
                        WifiStateMachine.this.logd("conConfig = " + conConfig.networkId);
                        if (conId == -1) {
                            conId = conConfig.networkId;
                        }
                    }
                    if (WifiStateMachine.this.mWifiInfo.getNetworkId() == conId || (conConfig != null && conConfig.networkId == WifiStateMachine.this.mWifiInfo.getNetworkId())) {
                        ignoreDisconnect = true;
                    }
                    WifiConfiguration savedConf = null;
                    if (!(WifiStateMachine.this.mWifiConfigManager == null || conConfig == null)) {
                        savedConf = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(conConfig.configKey());
                    }
                    if (!(savedConf == null || WifiStateMachine.this.mWifiInfo == null || savedConf.networkId != WifiStateMachine.this.mWifiInfo.getNetworkId())) {
                        ignoreDisconnect = true;
                    }
                    if (ignoreDisconnect) {
                        WifiStateMachine.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(true, true, 1000);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(conId);
                        }
                        WifiStateMachine.nNetId = conId;
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    return false;
            }
        }
    }

    class ConnectModeState extends State {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.wifi.WifiStateMachine.ConnectModeState.processMessage(android.os.Message):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean processMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.wifi.WifiStateMachine.ConnectModeState.processMessage(android.os.Message):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiStateMachine.ConnectModeState.processMessage(android.os.Message):boolean");
        }

        ConnectModeState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(true);
            }
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
        }

        public void exit() {
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(false);
            }
            WifiStateMachine.this.mWifiMetrics.setWifiState(1);
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            WifiStateMachine.this.scanResultsAvailable = false;
            WifiStateMachine.this.mScanCount = 0;
            if (WifiStateMachine.this.isSingtel()) {
                WifiStateMachine.this.mRetry = false;
                WifiStateMachine.this.isSingtelConnecting = false;
                WifiStateMachine.this.updatelastRecord(WifiStateMachine.this.mLastNetworkId);
            }
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log("Enter ConnectedState  mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
            }
            if (WifiStateMachine.this.mMtkCtpppoe) {
                Log.d(WifiStateMachine.TAG, "Enter ConnectedState, mPppoeInfo.status:" + WifiStateMachine.this.mPppoeInfo.status);
                if (WifiStateMachine.this.mPppoeInfo.status == Status.ONLINE) {
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.EVENT_UPDATE_DNS, 500);
                }
            }
            WifiStateMachine.this.registerConnected();
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.targetWificonfiguration = null;
            WifiStateMachine.this.linkDebouncing = false;
            WifiStateMachine.this.resetVerbose();
            WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS);
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mCheckInetAccessSeq = wifiStateMachine.mCheckInetAccessSeq + 1;
            if (WifiStateMachine.this.mDriverRoaming && WifiStateMachine.this.mAutoRoaming) {
                WifiStateMachine.this.loge("Dhcp successful after driver roaming, check internet access, seq=" + WifiStateMachine.this.mCheckInetAccessSeq);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS, 1, WifiStateMachine.this.mCheckInetAccessSeq), 2000);
            } else {
                WifiStateMachine.this.loge("Dhcp successful, check internet access, seq=" + WifiStateMachine.this.mCheckInetAccessSeq);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS, -1, WifiStateMachine.this.mCheckInetAccessSeq), 2000);
            }
            WifiStateMachine.this.mDriverRoaming = false;
            WifiStateMachine.this.mAutoRoaming = false;
            if (WifiStateMachine.this.testNetworkDisconnect) {
                wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.testNetworkDisconnectCounter = wifiStateMachine.testNetworkDisconnectCounter + 1;
                WifiStateMachine.this.logd("ConnectedState Enter start disconnect test " + WifiStateMachine.this.testNetworkDisconnectCounter);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT, WifiStateMachine.this.testNetworkDisconnectCounter, 0), 15000);
            }
            WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
            WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetworkRecord(WifiStateMachine.this.mWifiInfo.getNetworkId(), WifiStateMachine.this.mWifiInfo.getSSID());
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mTargetNetworkId = -1;
            WifiStateMachine.this.mWifiLastResortWatchdog.connectedStateTransition(true);
            if (OifaceUtil.isEnable()) {
                OifaceUtil.getInstance().currentNetwork(NetType.OIFACE_NETWORK_DATA_ON_WLAN.ordinal());
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:173:0x0933 A:{ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), Splitter: B:170:0x08d2} */
        /* JADX WARNING: Missing block: B:174:0x0934, code:
            r33.this$0.loge("Can't find MAC address for next hop to " + r22.dstAddress);
            com.android.server.wifi.WifiStateMachine.-get61(r33.this$0).onPacketKeepaliveEvent(r26, -21);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            String str;
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                    }
                    return false;
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                    if (message.arg1 == WifiStateMachine.this.testNetworkDisconnectCounter) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                            WifiStateMachine.this.mDisconnectOperation = true;
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 0) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                            WifiStateMachine.this.mDisconnectOperation = true;
                        }
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    } else if (message.arg1 == 2 || message.arg1 == 1) {
                        String str2 = WifiStateMachine.TAG;
                        if (message.arg1 == 2) {
                            str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                        } else {
                            str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                        }
                        Log.d(str2, str);
                        if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                            WifiStateMachine.this.log("Skip unwanted operation because of customization!");
                        } else {
                            config = WifiStateMachine.this.getCurrentWifiConfiguration();
                            if (config != null) {
                                if (message.arg1 == 2) {
                                    config.validatedInternetAccess = false;
                                    if (WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config)) {
                                        WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                                    }
                                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config, 8);
                                }
                                config.numNoInternetAccessReports++;
                                WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                            }
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    if (!WifiStateMachine.this.hasCustomizedAutoConnect() || WifiStateMachine.this.mWifiFwkExt.shouldAutoConnect()) {
                        ScanResult candidate = message.obj;
                        String bssid = WifiLastResortWatchdog.BSSID_ANY;
                        if (candidate != null) {
                            bssid = candidate.BSSID;
                        }
                        int netId = message.arg1;
                        if (netId != -1) {
                            config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                            if (config != null) {
                                WifiStateMachine.this.setTargetBssid(config, bssid);
                                WifiStateMachine.this.mTargetNetworkId = netId;
                                WifiStateMachine.this.logd("CMD_AUTO_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config.configKey() + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                                WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                                if (currentConfig == null || !currentConfig.isLinked(config)) {
                                    WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                                } else {
                                    WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 2);
                                }
                                if (!WifiStateMachine.this.deferForUserInput(message, netId, false)) {
                                    if (WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId).userApproved != 2) {
                                        boolean ret = false;
                                        int netIdFromWifiInfo = -1;
                                        if (WifiStateMachine.this.mWifiInfo != null) {
                                            netIdFromWifiInfo = WifiStateMachine.this.mWifiInfo.getNetworkId();
                                        }
                                        if (WifiStateMachine.this.linkDebouncing && netIdFromWifiInfo != -1 && netIdFromWifiInfo != WifiStateMachine.this.mLastNetworkId) {
                                            WifiStateMachine.this.handleNetworkDisconnect();
                                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                                            WifiStateMachine.this.mWifiNative.reassociate();
                                            break;
                                        }
                                        if (WifiStateMachine.this.mLastNetworkId != netId) {
                                            boolean tmpResult;
                                            if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                                                tmpResult = WifiStateMachine.this.mWifiConfigManager.enableNetwork(config, true, -1);
                                            } else {
                                                tmpResult = WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, false, -1);
                                            }
                                            if (tmpResult && WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, false, -1) && WifiStateMachine.this.mWifiNative.reconnect()) {
                                                ret = true;
                                            }
                                        } else {
                                            ret = WifiStateMachine.this.mWifiNative.reassociate();
                                        }
                                        if (!ret) {
                                            WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                                            WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                            WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                            break;
                                        }
                                        WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                        WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                        WifiStateMachine.this.mAutoRoaming = true;
                                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                                        break;
                                    }
                                    WifiStateMachine.this.replyToMessage(message, 151554, 9);
                                    WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                    break;
                                }
                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                break;
                            }
                            WifiStateMachine.this.loge("AUTO_ROAM nid=" + Integer.toString(netId) + " and config is null, bail out...");
                            break;
                        }
                        WifiStateMachine.this.loge("AUTO_ROAM and no config, bail out...");
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "Skip CMD_AUTO_ROAM for customization!");
                    return true;
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (WifiStateMachine.this.mLastBssid == null || !WifiStateMachine.this.mLastBssid.equals(message.obj)) {
                        WifiStateMachine.this.mLastDriverRoamAttempt = System.currentTimeMillis();
                    } else {
                        WifiStateMachine.this.log("bssid is the same, it isn't roaming");
                    }
                    return false;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.updateDefaultRouteMacAddress(1000);
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            config.numNoInternetAccessReports = 0;
                            config.validatedInternetAccess = true;
                            WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_ACCEPT_UNVALIDATED /*131225*/:
                    boolean accept = message.arg1 != 0;
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config != null) {
                        config.noInternetAccessExpected = accept;
                        WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                    }
                    return true;
                case WifiStateMachine.CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                    WifiStateMachine.this.updateAssociatedScanPermission();
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = message.obj;
                    try {
                        pkt.dstMac = WifiStateMachine.this.macAddressFromString(WifiStateMachine.this.macAddressFromRoute(RouteInfo.selectBestRoute(WifiStateMachine.this.mLinkProperties.getRoutes(), pkt.dstAddress).getGateway().getHostAddress()));
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, WifiStateMachine.this.startWifiIPPacketOffload(slot, pkt, intervalSeconds));
                        break;
                    } catch (NullPointerException e) {
                    }
                case WifiStateMachine.EVENT_UPDATE_DNS /*131324*/:
                    Log.d(WifiStateMachine.TAG, "Update DNS for pppoe!");
                    if (WifiStateMachine.this.mPppoeLinkProperties == null) {
                        Log.e(WifiStateMachine.TAG, "mPppoeLinkProperties is null");
                        break;
                    }
                    Collection<InetAddress> dnses = WifiStateMachine.this.mPppoeLinkProperties.getDnsServers();
                    ArrayList<String> pppoeDnses = new ArrayList();
                    for (InetAddress dns : dnses) {
                        pppoeDnses.add(dns.getHostAddress());
                    }
                    for (int i = 0; i < pppoeDnses.size(); i++) {
                        Log.d(WifiStateMachine.TAG, "Set net.dns" + (i + 1) + " to " + ((String) pppoeDnses.get(i)));
                        WifiStateMachine.this.mPropertyService.set("net.dns" + (i + 1), (String) pppoeDnses.get(i));
                    }
                    break;
                case WifiStateMachine.CMD_CHECK_INTERNET_ACCESS /*131353*/:
                    WifiStateMachine.this.loge("Checking internet access, SSID=" + WifiStateMachine.this.mWifiInfo.getSSID() + " BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " checkSequence=" + message.arg2);
                    WifiStateMachine.this.checkInternetAccess(message.arg1 == 1, message.arg2);
                    break;
                case WifiStateMachine.CMD_MTU_PROBER /*131357*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("[N18] CMD_MTU_PROBER ");
                    }
                    WifiStateMachine.this.mtuProber();
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    long lastRoam = 0;
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = System.currentTimeMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiLogger.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (!WifiStateMachine.this.mScreenOn || WifiStateMachine.this.linkDebouncing || config == null || !config.getNetworkSelectionStatus().isNetworkEnabled() || WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config) || (((message.arg2 == 3 || message.arg2 == 100) && (lastRoam <= 0 || lastRoam >= 2000)) || (((!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= -73) && (!ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mWifiConfigManager.mThresholdQualifiedRssi5.get())) || WifiStateMachine.this.hasCustomizedAutoConnect()))) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=").append(WifiStateMachine.this.mWifiInfo.getBSSID()).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.linkDebouncing).append(" reason=").append(message.arg2).append(" Network Selection Status=");
                            if (config == null) {
                                str = "Unavailable";
                            } else {
                                str = config.getNetworkSelectionStatus().getNetworkStatusString();
                            }
                            wifiStateMachine.log(append.append(str).toString());
                            break;
                        }
                    }
                    WifiStateMachine.this.startScanForConfiguration(WifiStateMachine.this.getCurrentWifiConfiguration());
                    if (WifiStateMachine.this.mWifiNetworkStateTraker == null || !(WifiStateMachine.this.mWifiNetworkStateTraker == null || WifiStateMachine.this.autoSwitch)) {
                        WifiStateMachine.this.linkDebouncing = true;
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC", Integer.valueOf(WifiStateMachine.LINK_FLAPPING_DEBOUNCE_MSEC)).intValue());
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in connected state BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " RSSI=" + WifiStateMachine.this.mWifiInfo.getRssi() + " freq=" + WifiStateMachine.this.mWifiInfo.getFrequency() + " reason=" + message.arg2 + " -> debounce");
                    }
                    return true;
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Connected state");
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            }
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mWhiteListedSsids = null;
            WifiStateMachine.this.mWifiLastResortWatchdog.connectedStateTransition(false);
            if (OifaceUtil.isEnable()) {
                OifaceUtil.getInstance().currentNetwork(NetType.OIFACE_NETWORK_DATA_OFF_WLAN.ordinal());
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case 69632:
                    if (message.obj == WifiStateMachine.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            WifiStateMachine.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        }
                        WifiStateMachine.this.mWifiP2pChannel.sendMessage(69633);
                        break;
                    }
                    WifiStateMachine.this.loge("got HALF_CONNECTED for unknown channel");
                    break;
                case 69636:
                    if (((AsyncChannel) message.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        WifiStateMachine.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED /*131089*/:
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                case WifiStateMachine.CMD_ENABLE_ALL_NETWORKS /*131127*/:
                case WifiStateMachine.CMD_BLACKLIST_NETWORK /*131128*/:
                case WifiStateMachine.CMD_CLEAR_BLACKLIST /*131129*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER /*131165*/:
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                case WifiStateMachine.CMD_AUTO_SAVE_NETWORK /*131218*/:
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                case WifiStateMachine.CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case 196611:
                case 196612:
                case 196614:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    break;
                case WifiStateMachine.CMD_PING_SUPPLICANT /*131123*/:
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) (List) null);
                    break;
                case WifiStateMachine.CMD_GET_CAPABILITY_FREQ /*131132*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_GET_SUPPORTED_FEATURES /*131133*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.getSupportedFeatureSet());
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) (List) null);
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_GET_CONNECTION_STATISTICS /*131148*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConnectionStatistics);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizations(2, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(4, false);
                        break;
                    }
                    if (message.arg2 == 1) {
                        WifiStateMachine.this.mSuspendWakeLock.release();
                    }
                    WifiStateMachine.this.setSuspendOptimizations(4, true);
                    break;
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    WifiStateMachine.this.handleScreenStateChanged(message.arg1 != 0);
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_FIRMWARE_ALERT /*131172*/:
                    if (WifiStateMachine.this.mWifiLogger != null) {
                        byte[] buffer = message.obj;
                        int alertReason = message.arg1;
                        WifiStateMachine.this.mWifiLogger.captureAlertData(alertReason, buffer);
                        WifiStateMachine.this.mWifiMetrics.incrementAlertReasonCount(alertReason);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_ADD_PASSPOINT_MO /*131174*/:
                case WifiStateMachine.CMD_MODIFY_PASSPOINT_MO /*131175*/:
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_BOOT_COMPLETED /*131206*/:
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_UPDATE_LINKPROPERTIES /*131212*/:
                    WifiStateMachine.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_USER_SWITCH /*131237*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    break;
                case WifiStateMachine.CMD_INSTALL_PACKET_FILTER /*131274*/:
                    WifiStateMachine.this.mWifiNative.installPacketFilter((byte[]) message.obj);
                    break;
                case WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                    if (!((Boolean) message.obj).booleanValue()) {
                        WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
                        break;
                    }
                    WifiStateMachine.this.mWifiNative.startFilteringMulticastV4Packets();
                    break;
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_ON /*131282*/:
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_OFF /*131283*/:
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_RATE /*131284*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                case WifiStateMachine.M_CMD_SET_TX_POWER_ENABLED /*131286*/:
                    WifiStateMachine.this.mWifiNative;
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiNative.setTxPowerEnabled(message.arg1 == 1) ? 1 : -1);
                    break;
                case WifiStateMachine.M_CMD_SET_TX_POWER /*131287*/:
                    WifiStateMachine.this.mWifiNative;
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiNative.setTxPower(message.arg1) ? 1 : -1);
                    break;
                case WifiStateMachine.M_CMD_GET_WIFI_STATUS /*131288*/:
                case WifiStateMachine.M_CMD_GET_TEST_ENV /*131293*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.M_CMD_SET_POWER_SAVING_MODE /*131289*/:
                case WifiStateMachine.M_CMD_FLUSH_BSS /*131292*/:
                case WifiStateMachine.M_CMD_SET_TDLS_POWER_SAVE /*131295*/:
                case WifiStateMachine.CMD_CHECK_FOR_EXPAPAUTO /*131358*/:
                    break;
                case WifiStateMachine.M_CMD_SET_WOWLAN_NORMAL_MODE /*131290*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.setWoWlanNormalModeCommand() ? 1 : -1);
                    break;
                case WifiStateMachine.M_CMD_SET_WOWLAN_MAGIC_MODE /*131291*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.setWoWlanMagicModeCommand() ? 1 : -1);
                    break;
                case WifiStateMachine.M_CMD_FACTORY_RESET /*131294*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.M_CMD_ENABLE_EAP_SIM_CONFIG_NETWORK /*131297*/:
                    List<WifiConfiguration> networks = WifiStateMachine.this.mWifiConfigManager.getSavedNetworks();
                    if (networks == null) {
                        WifiStateMachine.this.log("Check for EAP_SIM_AKA, networks is null!");
                        break;
                    }
                    boolean isSimConfigExisted = false;
                    for (WifiConfiguration network : networks) {
                        if (TelephonyUtil.isSimConfig(network) && network.getNetworkSelectionStatus().getNetworkSelectionDisableReason() == 10) {
                            WifiConfiguration eapSimConfig = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(network.networkId);
                            if (WifiStateMachine.this.isConfigSimCardLoaded(eapSimConfig)) {
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(eapSimConfig, 0);
                                isSimConfigExisted = true;
                            }
                        }
                    }
                    if (isSimConfigExisted && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.handleScanStrategyChanged();
                        break;
                    }
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = message.arg1 == 1;
                    WifiStateMachine.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("handle SUPPLICANT_STATE_CHANGE_EVENT in default state");
                    }
                    StateChangeResult stateChangeResult = message.obj;
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.loge("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state) + " debouncing=" + WifiStateMachine.this.linkDebouncing);
                    }
                    SupplicantState state = stateChangeResult.state;
                    if (WifiStateMachine.this.mLastSupplicantState != state) {
                        WifiStateMachine.this.mLastSupplicantState = state;
                    }
                    WifiStateMachine.this.mWifiInfo.setSupplicantState(state);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(-1);
                    WifiStateMachine.this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
                    WifiStateMachine.this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
                    break;
                case WifiMonitor.DRIVER_HUNG_EVENT /*147468*/:
                    WifiStateMachine.this.setSupplicantRunning(false);
                    WifiStateMachine.this.setSupplicantRunning(true);
                    break;
                case 151553:
                    WifiStateMachine.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    WifiStateMachine.this.replyToMessage(message, 151557, 2);
                    break;
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151560, 2);
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 2);
                    break;
                case 151566:
                    WifiStateMachine.this.replyToMessage(message, 151567, 2);
                    break;
                case 151569:
                    WifiStateMachine.this.replyToMessage(message, 151570, 2);
                    break;
                case 151572:
                    WifiStateMachine.this.replyToMessage(message, 151574, 2);
                    break;
                case 151575:
                    if (!WifiStateMachine.this.mMtkCtpppoe) {
                        WifiStateMachine.this.replyToMessage(message, 151577, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151577, 2);
                    break;
                case 151578:
                    if (!WifiStateMachine.this.mMtkCtpppoe) {
                        WifiStateMachine.this.replyToMessage(message, 151580, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151580, 2);
                    break;
                case 151612:
                    WifiStateMachine.this.loge("SET_WIFI_NOT_RECONNECT_AND_SCAN " + message);
                    if (message.arg1 != 1 && message.arg1 != 2) {
                        WifiStateMachine.this.loge("reset dont_reconnect_scan flag");
                        WifiStateMachine.this.removeMessages(151612);
                        if (WifiStateMachine.this.isTemporarilyDontReconnectWifi()) {
                            WifiStateMachine.this.mDontReconnect.set(false);
                            WifiStateMachine.this.mDontReconnectAndScan.set(false);
                            WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY);
                            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                                WifiStateMachine.this.mWifiConnectivityManager.handleScanStrategyChanged();
                                break;
                            }
                        }
                    }
                    WifiStateMachine.this.loge("set dont_reconnect_scan flag");
                    WifiStateMachine.this.removeMessages(151612);
                    if (message.arg2 > 0) {
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(151612, 0, -1), (long) (message.arg2 * 1000));
                    }
                    WifiStateMachine.this.loge("message.arg1: " + message.arg1);
                    if (message.arg1 == 2) {
                        WifiStateMachine.this.loge("isAllowReconnect is false");
                        WifiStateMachine.this.mDontReconnect.set(true);
                    }
                    if (!WifiStateMachine.this.isTemporarilyDontReconnectWifi()) {
                        WifiStateMachine.this.mDontReconnectAndScan.set(true);
                        WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY);
                        if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                            WifiStateMachine.this.mWifiConnectivityManager.handleScanStrategyChanged();
                            break;
                        }
                    }
                    break;
                default:
                    WifiStateMachine.this.loge("Error! unhandled message" + message);
                    break;
            }
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            if (WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd(" Enter DisconnectedState screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            if (!WifiStateMachine.this.hasCustomizedAutoConnect()) {
                WifiStateMachine.this.startScan(-1, 0, null, null);
            }
            WifiStateMachine.this.mAutoRoaming = false;
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
            }
            if (!(WifiStateMachine.this.isTemporarilyDontReconnectWifi() || WifiStateMachine.this.mNoNetworksPeriodicScan == 0 || WifiStateMachine.this.mP2pConnected.get() || WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() != 0)) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
            }
            WifiStateMachine.this.mDisconnectedTimeStamp = System.currentTimeMillis();
            if (WifiStateMachine.this.fetchPKTCount > 0) {
                WifiStateMachine.this.fetchPKTCount = 0;
                WifiStateMachine.this.resetRestartAlarm();
            }
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            boolean ret = true;
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiStateMachine wifiStateMachine;
            WifiStateMachine wifiStateMachine2;
            WifiStateMachine wifiStateMachine3;
            switch (message.what) {
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                case 151556:
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine2 = WifiStateMachine.this;
                    wifiStateMachine3 = WifiStateMachine.this;
                    wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                    ret = false;
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    if (WifiStateMachine.this.checkOrDeferScanAllowed(message)) {
                        ret = false;
                        break;
                    }
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_REFUSED;
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                        WifiStateMachine.this.mWifiNative.disconnect();
                        if (WifiStateMachine.this.mOperationalMode == 3) {
                            WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                            WifiStateMachine.this.setWifiState(1);
                        }
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    if (!SupplicantState.isConnecting(WifiStateMachine.this.mWifiInfo.getSupplicantState())) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("Ignore CMD_DISCONNECT when already disconnected.");
                            break;
                        }
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("CMD_DISCONNECT when supplicant is connecting - do not ignore");
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    break;
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    if (!WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                        ret = false;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                    if (!(WifiStateMachine.this.mP2pConnected.get() || WifiStateMachine.this.isTemporarilyDontReconnectWifi() || WifiStateMachine.this.mNoNetworksPeriodicScan == 0 || message.arg1 != WifiStateMachine.this.mPeriodicScanToken || WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() != 0)) {
                        WifiStateMachine.this.startScan(-1, -1, null, WifiStateMachine.WIFI_WORK_SOURCE);
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine2 = WifiStateMachine.this;
                        wifiStateMachine3 = WifiStateMachine.this;
                        wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                        break;
                    }
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    wifiStateMachine2 = WifiStateMachine.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    wifiStateMachine2.handleScreenStateChanged(z);
                    break;
                case WifiStateMachine.M_CMD_UPDATE_BGSCAN /*131285*/:
                    if (WifiStateMachine.this.isTemporarilyDontReconnectWifi()) {
                        Log.d(WifiStateMachine.TAG, "isNetworksDisabledDuringConnect:" + WifiStateMachine.this.mSupplicantStateTracker.isNetworksDisabledDuringConnect() + ", mConnectNetwork:" + WifiStateMachine.this.mConnectNetwork);
                        if (WifiStateMachine.this.mConnectNetwork) {
                            WifiStateMachine.this.mConnectNetwork = false;
                        } else if (!WifiStateMachine.this.mSupplicantStateTracker.isNetworksDisabledDuringConnect()) {
                            Log.d(WifiStateMachine.TAG, "Disable supplicant auto scan!");
                            WifiStateMachine.this.mWifiNative.disconnect();
                        }
                    } else if (!WifiStateMachine.this.mTemporarilyDisconnectWifi && WifiStateMachine.this.hasCustomizedAutoConnect()) {
                        WifiStateMachine.this.mWifiNative.reconnect();
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY /*131296*/:
                    if (WifiStateMachine.this.isTemporarilyDontReconnectWifi()) {
                        if (WifiStateMachine.this.mConnectNetwork) {
                            WifiStateMachine.this.mConnectNetwork = false;
                        } else {
                            Log.d(WifiStateMachine.TAG, "Disable supplicant auto scan!");
                            WifiStateMachine.this.mWifiNative.disconnect();
                        }
                    }
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.handleScanStrategyChanged();
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    if (!WifiStateMachine.this.mP2pConnected.get()) {
                        if (WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log("Turn on scanning after p2p disconnected");
                            }
                            wifiStateMachine = WifiStateMachine.this;
                            wifiStateMachine2 = WifiStateMachine.this;
                            wifiStateMachine3 = WifiStateMachine.this;
                            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                            break;
                        }
                    }
                    WifiStateMachine.this.mWifiNative.setScanInterval(((int) WifiStateMachine.this.mFacade.getLongSetting(WifiStateMachine.this.mContext, "wifi_scan_interval_p2p_connected_ms", (long) WifiStateMachine.this.mContext.getResources().getInteger(17694769))) / 1000);
                    break;
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.isSingtelConnecting = false;
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (stateChangeResult != null && stateChangeResult.state != null) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state) + " debouncing=" + WifiStateMachine.this.linkDebouncing);
                        }
                        WifiStateMachine.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                        ret = false;
                        break;
                    }
                    return false;
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
        }

        public void exit() {
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            }
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd(" Enter DisconnectingState State screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.disconnectingWatchdogCount++;
            WifiStateMachine.this.logd("Start Disconnecting Watchdog " + WifiStateMachine.this.disconnectingWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER, WifiStateMachine.this.disconnectingWatchdogCount, 0), WifiStateMachine.SMART_SCAN_INTERVAL);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                    if (WifiStateMachine.this.disconnectingWatchdogCount == message.arg1) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("disconnecting watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY /*131296*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStartedState extends State {
        DriverStartedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd("DriverStartedState enter");
            }
            if (WifiStateMachine.this.mWifiScanner == null) {
                WifiStateMachine.this.mWifiScanner = WifiStateMachine.this.mFacade.makeWifiScanner(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler().getLooper());
                WifiStateMachine.this.mWifiScanner.enableVerboseLogging(WifiStateMachine.this.mVerboseLoggingLevel);
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine.this.mWifiConnectivityManager = new WifiConnectivityManager(WifiStateMachine.this.mContext, WifiStateMachine.this, WifiStateMachine.this.mWifiScanner, WifiStateMachine.this.mWifiConfigManager, WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mWifiQualifiedNetworkSelector, WifiStateMachine.this.mWifiInjector, WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.hasConnectionRequests());
                    WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(WifiStateMachine.this.mUntrustedReqCount > 0);
                }
            }
            WifiStateMachine.this.mWifiLogger.startLogging(WifiStateMachine.DBG);
            WifiStateMachine.this.mIsRunning = true;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV6Packets();
            if (WifiStateMachine.this.mOperationalMode != 1) {
                WifiStateMachine.this.mWifiNative.disconnect();
                if (WifiStateMachine.this.mOperationalMode == 3) {
                    WifiStateMachine.this.setWifiState(1);
                }
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
            } else {
                WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                WifiStateMachine.this.mWifiNative.status();
                WifiStateMachine.this.mWifiOnScanCount = 0;
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
            }
            if (WifiStateMachine.this.mScreenBroadcastReceived.get()) {
                boolean z;
                WifiNative -get128 = WifiStateMachine.this.mWifiNative;
                if (WifiStateMachine.this.mSuspendOptNeedsDisabled == 0) {
                    z = WifiStateMachine.this.mUserWantsSuspendOpt.get();
                } else {
                    z = false;
                }
                -get128.setSuspendOptimizations(z);
                WifiStateMachine.this.mWifiConnectivityManager.handleScreenStateChanged(WifiStateMachine.this.mScreenOn);
            } else {
                WifiStateMachine.this.handleScreenStateChanged(((PowerManager) WifiStateMachine.this.mContext.getSystemService("power")).isScreenOn());
            }
            WifiStateMachine.this.mWifiNative.setPowerSave(true);
            if (WifiStateMachine.this.mP2pSupported && WifiStateMachine.this.mOperationalMode == 1) {
                WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            }
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 3);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.resetAlarmCount = 0;
            WifiStateMachine.this.mAlarmManager.set(0, WifiStateMachine.this.caculateTimeIntoMillis(3, WifiStateMachine.this.getRandomTime(1, 5), WifiStateMachine.this.getRandomTime(0, 60)), WifiStateMachine.this.mRestartIntent);
            WifiStateMachine.this.mWifiNative.setWifiLinkLayerStats("wlan0", 1);
            if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_5G_BAND_SUPPORT, 0) == 0 && 2 == (WifiStateMachine.this.mWifiNative.getSupportedFeatureSet() & 2)) {
                Global.putInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_5G_BAND_SUPPORT, 1);
            }
        }

        public boolean processMessage(Message message) {
            boolean z = false;
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiNative -get128;
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                    if (WifiStateMachine.this.mOperationalMode == 1) {
                        WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                    int mode = message.arg1;
                    WifiStateMachine.this.log("stop driver");
                    WifiStateMachine.this.mWifiConfigManager.disableAllNetworksNative();
                    if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                        WifiStateMachine.this.mDisconnectOperation = true;
                    }
                    if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.handleNetworkDisconnect();
                    }
                    WifiStateMachine.this.mWakeLock.acquire();
                    WifiStateMachine.this.mWifiNative.stopDriver();
                    WifiStateMachine.this.mWakeLock.release();
                    if (!WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStoppingState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    boolean z2;
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (message.arg1 != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    wifiStateMachine.mBluetoothConnectionActive = z2;
                    WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    WifiStateMachine.this.mSendScanResultsBroadcast = true;
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(2, false);
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(4, false);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(4, true);
                    if (message.arg2 == 1) {
                        WifiStateMachine.this.mSuspendWakeLock.release();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    int band = message.arg1;
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("set frequency band " + band);
                    }
                    if (!WifiStateMachine.this.mWifiNative.setBand(band)) {
                        WifiStateMachine.this.loge("Failed to set frequency band " + band);
                        break;
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("did set frequency band " + band);
                    }
                    WifiStateMachine.this.mFrequencyBand.set(band);
                    WifiStateMachine.this.mWifiNative.bssFlush();
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("done set frequency band " + band);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_TDLS /*131164*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mWifiNative.startTdls(message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    int slot = message.arg1;
                    int ret = WifiStateMachine.this.stopWifiIPPacketOffload(slot);
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /*131238*/:
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiConnectivityManager -get119 = WifiStateMachine.this.mWifiConnectivityManager;
                        if (message.arg1 == 1) {
                            z = true;
                        }
                        -get119.enable(z);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED /*131239*/:
                    boolean allowed = message.arg1 > 0;
                    boolean old_state = WifiStateMachine.this.mWifiConfigManager.getEnableAutoJoinWhenAssociated();
                    WifiStateMachine.this.mWifiConfigManager.setEnableAutoJoinWhenAssociated(allowed);
                    if (!old_state && allowed && WifiStateMachine.this.mScreenOn && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan();
                        break;
                    }
                case WifiStateMachine.CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    WifiStateMachine.this.mWifiNative.configureNeighborDiscoveryOffload(message.arg1 > 0);
                    break;
                case WifiStateMachine.M_CMD_SET_POWER_SAVING_MODE /*131289*/:
                    -get128 = WifiStateMachine.this.mWifiNative;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    -get128.setPowerSave(z);
                    break;
                case WifiStateMachine.M_CMD_SET_TDLS_POWER_SAVE /*131295*/:
                    -get128 = WifiStateMachine.this.mWifiNative;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    -get128.setTdlsPowerSave(z);
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                    WifiConfigManager -get117 = WifiStateMachine.this.mWifiConfigManager;
                    Long l = (Long) message.obj;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    -get117.notifyANQPDone(l, z);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                    WifiStateMachine.this.mWifiConfigManager.notifyIconReceived((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                    WifiStateMachine.this.wnmFrameReceived((WnmData) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + " exit\n");
            }
            WifiStateMachine.this.mWifiLogger.stopLogging();
            WifiStateMachine.this.mIsRunning = false;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mScanResults = new ArrayList();
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mBufferedScanMsg.clear();
        }
    }

    class DriverStartingState extends State {
        private int mTries;

        DriverStartingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            this.mTries = 1;
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_DRIVER_START_TIMED_OUT, wifiStateMachine3.mDriverStartToken = wifiStateMachine3.mDriverStartToken + 1, 0), 10000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                    if (message.arg1 == WifiStateMachine.this.mDriverStartToken) {
                        if (this.mTries < 2) {
                            WifiStateMachine.this.loge("Driver start failed, retrying");
                            WifiStateMachine.this.mWakeLock.acquire();
                            WifiStateMachine.this.mWifiNative.startDriver();
                            WifiStateMachine.this.mWakeLock.release();
                            this.mTries++;
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_DRIVER_START_TIMED_OUT, wifiStateMachine3.mDriverStartToken = wifiStateMachine3.mDriverStartToken + 1, 0), 10000);
                            break;
                        }
                        WifiStateMachine.this.loge("Failed to start driver after " + this.mTries);
                        WifiStateMachine.this.setSupplicantRunning(false);
                        WifiStateMachine.this.setSupplicantRunning(true);
                        break;
                    }
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (SupplicantState.isDriverActive(WifiStateMachine.this.handleSupplicantStateChange(message))) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStoppedState extends State {
        DriverStoppedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                    WifiStateMachine.this.mWakeLock.acquire();
                    WifiStateMachine.this.mWifiNative.startDriver();
                    WifiStateMachine.this.mWakeLock.release();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartingState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = message.obj.state;
                    if (WifiStateMachine.this.mLastSupplicantState != state) {
                        WifiStateMachine.this.mLastSupplicantState = state;
                    }
                    if (SupplicantState.isDriverActive(state)) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStoppingState extends State {
        DriverStoppingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (WifiStateMachine.this.handleSupplicantStateChange(message) == SupplicantState.INTERFACE_DISABLED) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStoppedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            WifiStateMachine.this.mWifiNative.stopHal();
            WifiStateMachine.this.mWifiNative.unloadDriver();
            if (WifiStateMachine.this.mWifiP2pChannel == null) {
                WifiStateMachine.this.mWifiP2pChannel = new AsyncChannel();
                WifiStateMachine.this.mWifiP2pChannel.connect(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler(), WifiStateMachine.this.mWifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
            if (WifiStateMachine.this.mWifiApConfigStore == null) {
                WifiStateMachine.this.mWifiApConfigStore = WifiStateMachine.this.mFacade.makeApConfigStore(WifiStateMachine.this.mContext, WifiStateMachine.this.mBackupManagerProxy);
            }
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.mIsFullScanOngoing = true;
            WifiStateMachine.this.log("fool-proof,cancel restart alarm. ");
            WifiStateMachine.this.mAlarmManager.cancel(WifiStateMachine.this.mRestartIntent);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                    ExceptionLog exceptionLog;
                    if (!WifiStateMachine.this.mWifiNative.loadDriver()) {
                        WifiStateMachine.this.loge("Failed to load driver");
                        exceptionLog = null;
                        try {
                            if (WifiStateMachine.this.mPropertyService.get("ro.have_aee_feature", "").equals("1")) {
                                exceptionLog = new ExceptionLog();
                            }
                            if (exceptionLog != null) {
                                exceptionLog.systemreport((byte) 1, "CRDISPATCH_KEY:WifiStateMachine", "loadDriver fails", "/data/cursorleak/traces.txt");
                            }
                        } catch (Exception e) {
                        }
                        WifiStateMachine.this.setWifiState(4);
                        break;
                    }
                    try {
                        WifiStateMachine.this.mNwService.wifiFirmwareReload(WifiStateMachine.this.mInterfaceName, "STA");
                        try {
                            WifiStateMachine.this.mNwService.setInterfaceDown(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.mNwService.clearInterfaceAddresses(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.mNwService.setInterfaceIpv6PrivacyExtensions(WifiStateMachine.this.mInterfaceName, true);
                            WifiStateMachine.this.mNwService.disableIpv6(WifiStateMachine.this.mInterfaceName);
                        } catch (RemoteException re) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        WifiStateMachine.this.mWifiMonitor.killSupplicant(WifiStateMachine.this.mP2pSupported);
                        if (!WifiStateMachine.this.mWifiNative.startHal()) {
                            WifiStateMachine.this.loge("Failed to start HAL");
                        }
                        if (WifiStateMachine.this.mWifiNative.startSupplicant(WifiStateMachine.this.mP2pSupported)) {
                            WifiStateMachine.this.setSupplicantLogLevel();
                            if (WifiStateMachine.this.mOperationalMode == 1) {
                                WifiStateMachine.this.setWifiState(2);
                            }
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log("Supplicant start successful");
                            }
                            WifiStateMachine.this.mWifiMonitor.startMonitoring(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartingState);
                            break;
                        }
                        WifiStateMachine.this.loge("Failed to start supplicant!");
                        WifiStateMachine.this.setWifiState(4);
                        exceptionLog = null;
                        try {
                            if (WifiStateMachine.this.mPropertyService.get("ro.have_aee_feature", "").equals("1")) {
                                exceptionLog = new ExceptionLog();
                            }
                            if (exceptionLog != null) {
                                exceptionLog.systemreport((byte) 1, "CRDISPATCH_KEY:WifiStateMachine", "Failed to start supplicant!", "/data/cursorleak/traces.txt");
                            }
                        } catch (Exception e2) {
                        }
                        return true;
                    } catch (Exception e3) {
                        WifiStateMachine.this.loge("Failed to reload STA firmware " + e3);
                        WifiStateMachine.this.loge("fwreload fail, unloadDriver");
                        WifiStateMachine.this.mWifiNative.unloadDriver();
                        exceptionLog = null;
                        try {
                            if (WifiStateMachine.this.mPropertyService.get("ro.have_aee_feature", "").equals("1")) {
                                exceptionLog = new ExceptionLog();
                            }
                            if (exceptionLog != null) {
                                exceptionLog.systemreport((byte) 1, "CRDISPATCH_KEY:WifiStateMachine", "fwreload fails", "/data/cursorleak/traces.txt");
                            }
                        } catch (Exception e4) {
                        }
                        WifiStateMachine.this.setWifiState(4);
                        return true;
                    }
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    if (!WifiStateMachine.this.setupDriverForSoftAp()) {
                        WifiStateMachine.this.setWifiApState(14, 0);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSoftApState);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class IpManagerCallback extends Callback {
        IpManagerCallback() {
        }

        public void onPreDhcpAction() {
            WifiStateMachine.this.sendMessage(196611);
        }

        public void onPostDhcpAction() {
            WifiStateMachine.this.sendMessage(196612);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults != null) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
                return;
            }
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
            WifiStateMachine.this.mWifiLastResortWatchdog.noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 3);
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            WifiStateMachine.this.logd("onReachabilityLost received:" + logMsg);
        }

        public void installPacketFilter(byte[] filter) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }
    }

    class L2ConnectedState extends State {
        L2ConnectedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
            if (WifiStateMachine.this.mEnableRssiPolling) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0);
            }
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.loge("Have NetworkAgent when entering L2Connected");
                WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.CONNECTING);
            WifiStateMachine.this.mNetworkAgent = new WifiNetworkAgent(WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 5, WifiStateMachine.this.mNetworkMisc);
            WifiStateMachine.this.clearCurrentConfigBSSID("L2ConnectedState");
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(3);
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log("Reset mIsListeningIpReachabilityLost");
            }
            WifiStateMachine.this.mIsListeningIpReachabilityLost = false;
        }

        public void exit() {
            WifiStateMachine.this.log("Leaving L2ConnctedState");
            WifiStateMachine.this.mIpManager.stop();
            if (WifiStateMachine.DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=").append(Integer.toString(WifiStateMachine.this.mLastNetworkId));
                if (WifiStateMachine.this.mLastBssid != null) {
                    sb.append(" ").append(WifiStateMachine.this.mLastBssid);
                }
            }
            if (!(WifiStateMachine.this.mLastBssid == null && WifiStateMachine.this.mLastNetworkId == -1)) {
                WifiStateMachine.this.handleNetworkDisconnect();
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
            WifiStateMachine.this.mIsNewAssociatedBssid = false;
            WifiStateMachine.this.mDriverRoaming = false;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiStateMachine wifiStateMachine;
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        WifiStateMachine.this.deferMessage(message);
                        if (message.arg1 == 3) {
                            WifiStateMachine.this.noteWifiDisabledWhileAssociated();
                        }
                    } else {
                        WifiStateMachine.this.deferMessage(message);
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.cleanWifiScore();
                    if (WifiStateMachine.this.mWifiConfigManager.mEnableRssiPollWhenAssociated.get()) {
                        WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    } else {
                        WifiStateMachine.this.mEnableRssiPolling = false;
                    }
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
                    if (WifiStateMachine.this.mEnableRssiPolling) {
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), 3000);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                    if (message.arg1 == WifiStateMachine.this.mRssiPollToken) {
                        if (WifiStateMachine.this.mWifiConfigManager.mEnableChipWakeUpWhenAssociated.get()) {
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log(" get link layer stats " + WifiStateMachine.this.mWifiLinkLayerStatsSupported);
                            }
                            WifiLinkLayerStats stats = WifiStateMachine.this.getWifiLinkLayerStats(WifiStateMachine.DBG);
                            if (!(stats == null || WifiStateMachine.this.mWifiInfo.getRssi() == -127 || (stats.rssi_mgmt != 0 && stats.beacon_rx != 0))) {
                            }
                            WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        }
                        if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.reportRssi();
                        }
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), 3000);
                        if (WifiStateMachine.MDBG) {
                            WifiStateMachine.this.sendRssiChangeBroadcast(WifiStateMachine.this.mWifiInfo.getRssi());
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT /*131159*/:
                    if (WifiStateMachine.this.linkDebouncing || !WifiStateMachine.this.mWifiConfigManager.mEnableLinkDebouncing) {
                        WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and debouncing - disconnect " + message.arg1);
                        WifiStateMachine.this.linkDebouncing = false;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and not debouncing - ignore " + message.arg1);
                    return true;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message.arg2 == 0 && WifiStateMachine.this.mLastNetworkId != -1) {
                        WifiConfiguration config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(WifiStateMachine.this.mLastNetworkId);
                        int removedSimSlot = message.arg1;
                        int configSimSlot = WifiConfigurationUtil.getIntSimSlot(config);
                        if (TelephonyUtil.isSimConfig(config) && configSimSlot == removedSimSlot) {
                            WifiStateMachine.this.log("config.simSlot: " + config.simSlot + "," + configSimSlot + " equals removedSimSlot: " + removedSimSlot);
                            WifiStateMachine.this.mWifiNative.disconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.mAutoRoaming = false;
                    WifiStateMachine.this.handleSuccessfulIpConfiguration();
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null && !WifiStateMachine.this.mWifiNetworkStateTraker.getManualConnect() && WifiStateMachine.this.wifiAssistantForSoftAP() && WifiStateMachine.this.autoSwitch && WifiStateMachine.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_DETECT_CONNECT", Boolean.valueOf(true)).booleanValue() && WifiStateMachine.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED && !WifiStateMachine.this.mNetworkDetectValid) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mCaptiveState);
                        break;
                    }
                    if (WifiStateMachine.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        WifiStateMachine.this.mNetworkAgent.explicitlySelected(true);
                        WifiStateMachine.this.mNetworkAgent.sendNetworkScore(79);
                    }
                    WifiStateMachine.this.sendConnectedState();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    WifiStateMachine.this.getWifiLinkLayerStats(true);
                    WifiStateMachine.this.handleIpConfigurationLost();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (((String) message.obj) != null) {
                        if (!(WifiStateMachine.this.mLastBssid == null || WifiStateMachine.this.mLastBssid.equals(message.obj))) {
                            WifiStateMachine.this.mIsNewAssociatedBssid = true;
                        }
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastBssid != null && (WifiStateMachine.this.mWifiInfo.getBSSID() == null || !WifiStateMachine.this.mLastBssid.equals(WifiStateMachine.this.mWifiInfo.getBSSID()))) {
                            WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                            break;
                        }
                    }
                    WifiStateMachine.this.logw("Associated command w/o BSSID");
                    break;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1 && (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mObtainingIpState || WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mRoamingState)) {
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    if (!WifiStateMachine.this.isTemporarilyDontReconnectWifi()) {
                        if (!WifiStateMachine.this.enableIpReachabilityMonitor()) {
                            Log.d(WifiStateMachine.TAG, "Ignore CMD_IP_REACHABILITY_LOST due to enableIpReachabilityMonitor is off");
                            break;
                        }
                        Log.d(WifiStateMachine.TAG, "mIsListeningIpReachabilityLost: " + WifiStateMachine.this.mIsListeningIpReachabilityLost);
                        if (WifiStateMachine.this.enableIpReachabilityMonitorEnhancement() && !WifiStateMachine.this.mIsListeningIpReachabilityLost) {
                            Log.d(WifiStateMachine.TAG, "mIsListeningIpReachabilityLost: " + WifiStateMachine.this.mIsListeningIpReachabilityLost);
                            if (!WifiStateMachine.this.mIsListeningIpReachabilityLost) {
                                Log.d(WifiStateMachine.TAG, "Ignore CMD_IP_REACHABILITY_LOST");
                                break;
                            }
                            if (WifiStateMachine.DBG && message.obj != null) {
                                WifiStateMachine.this.log((String) message.obj);
                            }
                            WifiStateMachine.this.handleIpReachabilityLost();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                            break;
                        }
                        if (WifiStateMachine.DBG && message.obj != null) {
                            WifiStateMachine.this.log((String) message.obj);
                        }
                        WifiStateMachine.this.handleIpReachabilityLost();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    WifiStateMachine.this.log("isTemporarilyDontReconnectWifi is true, ignore CMD_IP_REACHABILITY_LOST");
                    break;
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                case WifiStateMachine.CMD_RSSI_THRESHOLD_BREACH /*131236*/:
                    WifiStateMachine.this.processRssiThreshold((byte) message.arg1, message.what);
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.stopRssiMonitoringOffload();
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                    WifiStateMachine.this.handleIPv4Success((DhcpResults) message.obj);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                    WifiStateMachine.this.handleIPv4Failure();
                    break;
                case WifiStateMachine.M_CMD_GET_WIFI_STATUS /*131288*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiNative.status());
                    break;
                case WifiStateMachine.M_CMD_IP_REACHABILITY_MONITOR_TIMER /*131307*/:
                    if (message.arg1 != WifiStateMachine.this.ipReachabilityMonitorCount) {
                        Log.d(WifiStateMachine.TAG, "IpReachabilityMonitor count mismatch, count: " + WifiStateMachine.this.ipReachabilityMonitorCount + ", arg1: " + message.arg1);
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "IpReachabilityMonitor timer time out, count: " + WifiStateMachine.this.ipReachabilityMonitorCount);
                    WifiStateMachine.this.mIsListeningIpReachabilityLost = false;
                    break;
                case WifiStateMachine.M_CMD_SET_RSSI /*131308*/:
                    if (WifiStateMachine.this.mEnableRssiSmoothing && (WifiStateMachine.this.mAnimator.isRunning() || (WifiStateMachine.this.mCurSmoothRssi == WifiStateMachine.this.mWifiInfo.getRssi(WifiStateMachine.this.mAffectRoaming) && message.arg1 == WifiStateMachine.this.mTagetRssi))) {
                        Log.d(WifiStateMachine.TAG, "M_CMD_SET_RSSI mAffectRoaming = " + WifiStateMachine.this.mAffectRoaming);
                        int curRssi = message.arg1;
                        if (WifiStateMachine.this.mAffectRoaming) {
                            WifiStateMachine.this.mWifiInfo.setRssi(curRssi);
                        } else {
                            WifiStateMachine.this.mWifiInfo.mSmoothRssi = curRssi;
                        }
                        int newSignalLevel = WifiManager.calculateSignalLevel(curRssi, 5);
                        if (newSignalLevel != WifiStateMachine.this.mLastSignalLevel) {
                            WifiStateMachine.this.updateCapabilities(WifiStateMachine.this.getCurrentWifiConfiguration());
                            WifiStateMachine.this.sendRssiChangeBroadcast(curRssi);
                        }
                        Log.d(WifiStateMachine.TAG, "M_CMD_SET_RSSI mLastSignalLevel:" + WifiStateMachine.this.mLastSignalLevel + ", newSignalLevel:" + newSignalLevel);
                        WifiStateMachine.this.mLastSignalLevel = newSignalLevel;
                        WifiStateMachine.this.mWifiConfigManager.updateConfiguration(WifiStateMachine.this.mWifiInfo);
                        break;
                    }
                case WifiStateMachine.EVENT_PPPOE_SUCCEEDED /*131323*/:
                    WifiStateMachine.this.handleSuccessfulPppoeConfiguration((DhcpResults) message.obj);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                            WifiStateMachine.this.mDisconnectOperation = true;
                        }
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    Log.d(WifiStateMachine.TAG, "mLastBssid:" + WifiStateMachine.this.mLastBssid + ", newBssid:" + ((String) message.obj) + ", mIsNewAssociatedBssid:" + WifiStateMachine.this.mIsNewAssociatedBssid);
                    if (WifiStateMachine.this.mLastBssid == null || message.obj == null || !WifiStateMachine.this.mLastBssid.equals(message.obj) || WifiStateMachine.this.mIsNewAssociatedBssid) {
                        WifiStateMachine.this.mIsNewAssociatedBssid = false;
                        WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                        if (WifiStateMachine.this.sLastNetworkId != message.arg1) {
                            DhcpClient.clearOffer();
                            WifiStateMachine.this.sLastNetworkId = message.arg1;
                        }
                        WifiStateMachine.this.mLastNetworkId = message.arg1;
                        WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                        if (WifiStateMachine.this.mLastBssid != null) {
                            if (!WifiStateMachine.this.mLastBssid.equals((String) message.obj)) {
                                WifiStateMachine.this.mLastBssid = (String) message.obj;
                                WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                            }
                        } else if (message.obj != null) {
                            WifiStateMachine.this.mLastBssid = (String) message.obj;
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                        }
                        if (WifiStateMachine.this.enableIpReachabilityMonitorEnhancement()) {
                            WifiStateMachine.this.startListenToIpReachabilityLost();
                            Log.d(WifiStateMachine.TAG, "driver roaming, start to listen ip reachability lost for 10 sec, counter: " + WifiStateMachine.this.ipReachabilityMonitorCount);
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, WifiStateMachine.this.mLastNetworkId, message.arg2, WifiStateMachine.this.mLastBssid);
                        }
                        WifiStateMachine.this.mDriverRoaming = true;
                        if (WifiStateMachine.this.isUsingDHCP()) {
                            WifiStateMachine.this.mAutoRoaming = true;
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                            break;
                        }
                    }
                    break;
                case 151553:
                    int netId = message.arg1;
                    if (netId == -1) {
                        WifiConfiguration wConf = message.obj;
                        if (wConf != null) {
                            netId = wConf.networkId;
                        }
                    }
                    if (WifiStateMachine.this.mWifiNetworkStateTraker == null) {
                        if (WifiStateMachine.this.mWifiInfo.getNetworkId() == netId) {
                            WifiStateMachine.this.replyToMessage(message, 151555);
                            break;
                        }
                    } else if (WifiStateMachine.this.mWifiInfo.getNetworkId() == netId && !WifiStateMachine.this.mWifiNetworkStateTraker.getManualConnect()) {
                        if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(true, true, 1000);
                            break;
                        } else if (WifiStateMachine.this.mWifiNative != null) {
                            WifiStateMachine.this.mWifiNative.disconnect();
                        }
                    }
                    return false;
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = WifiStateMachine.this.mWifiInfo.getRssi();
                    WifiStateMachine.this.fetchPktcntNative(info);
                    if (message.arg1 != 1000) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("RSSI_PKTCNT_FETCH_SUCCEEDED");
                        }
                        WifiStateMachine.this.replyToMessage(message, 151573, (Object) info);
                        break;
                    }
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.fetchPKTCount = wifiStateMachine.fetchPKTCount + 1;
                    if (!WifiStateMachine.this.mScreenOn) {
                        if (!WifiStateMachine.this.hasNetworkAccessing(info, WifiStateMachine.this.fetchPKTCount)) {
                            if (WifiStateMachine.this.fetchPKTCount >= 60) {
                                if (WifiStateMachine.DBG) {
                                    WifiStateMachine.this.log("fool-proof,restart in silence although AP connected");
                                }
                                WifiStateMachine.this.setStatistics("silence_connected", "wifi_restart_in_silence_connected");
                                WifiStateMachine.this.sheduleRestartWifi(WifiStateMachine.this.mLastNetworkId);
                                WifiStateMachine.this.fetchPKTCount = 0;
                                break;
                            }
                            WifiStateMachine.this.sendMessageDelayed(151572, 1000, 1000);
                            break;
                        }
                    }
                    WifiStateMachine.this.fetchPKTCount = 0;
                    WifiStateMachine.this.resetRestartAlarm();
                    break;
                case 151575:
                    if (!WifiStateMachine.this.mMtkCtpppoe) {
                        WifiStateMachine.this.replyToMessage(message, 151577, 0);
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "mPppoeInfo.status:" + WifiStateMachine.this.mPppoeInfo.status + ", config:" + ((PPPOEConfig) message.obj));
                    if (WifiStateMachine.this.mPppoeInfo.status != Status.ONLINE) {
                        WifiStateMachine.this.mPppoeConfig = (PPPOEConfig) message.obj;
                        WifiStateMachine.this.mUsingPppoe = true;
                        if (WifiStateMachine.this.mPppoeHandler == null) {
                            HandlerThread pppoeThread = new HandlerThread("PPPoE Handler Thread");
                            pppoeThread.start();
                            WifiStateMachine.this.mPppoeHandler = new PppoeHandler(pppoeThread.getLooper(), WifiStateMachine.this);
                        }
                        WifiStateMachine.this.mPppoeHandler.sendEmptyMessage(WifiStateMachine.EVENT_START_PPPOE);
                        WifiStateMachine.this.replyToMessage(message, 151576);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151576);
                    WifiStateMachine.this.sendPppoeCompletedBroadcast("ALREADY_ONLINE", -1);
                    break;
                case 196611:
                    WifiStateMachine.this.handlePreDhcpSetup();
                    break;
                case 196612:
                    WifiStateMachine.this.handlePostDhcpSetup();
                    break;
                case 196614:
                    WifiStateMachine.this.mIpManager.completedPreDhcpAction(WifiStateMachine.this.getCurrentWifiConfiguration());
                    break;
                case 196628:
                    WifiStateMachine.this.loge("L2ConnectedState CMD_RENEW_TIMES_POST");
                    if (!WifiStateMachine.this.mScreenOn) {
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.mIdleRenewTimes = wifiStateMachine.mIdleRenewTimes + 1;
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                String key = "";
                if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                    key = WifiStateMachine.this.getCurrentWifiConfiguration().configKey();
                }
                WifiStateMachine.this.log("enter ObtainingIpState netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key + " " + " roam=" + WifiStateMachine.this.mAutoRoaming + " static=" + WifiStateMachine.this.mWifiConfigManager.isUsingStaticIp(WifiStateMachine.this.mLastNetworkId) + " watchdog= " + WifiStateMachine.this.obtainingIpWatchdogCount);
            }
            WifiStateMachine.this.linkDebouncing = false;
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.OBTAINING_IPADDR);
            WifiStateMachine.this.clearCurrentConfigBSSID("ObtainingIpAddress");
            WifiStateMachine.this.mIpManager.setDhcp6ClientFeature(WifiStateMachine.this.getRomUpdateBooleanValue("OPPO_WIFI_DHCP6CLIENT", Boolean.valueOf(false)).booleanValue());
            if (!WifiStateMachine.this.mDriverRoaming) {
                WifiStateMachine.this.stopIpManager();
            }
            WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.mWifiConfigManager.getProxyProperties(WifiStateMachine.this.mLastNetworkId));
            if (!TextUtils.isEmpty(WifiStateMachine.this.mTcpBufferSizes)) {
                WifiStateMachine.this.mIpManager.setTcpBufferSizes(WifiStateMachine.this.mTcpBufferSizes);
            }
            if (WifiStateMachine.this.isSingtel()) {
                WifiStateMachine.this.mRetry = false;
                WifiStateMachine.this.isSingtelConnecting = false;
            }
            if (WifiStateMachine.this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_HANDLE_GATEWAY_CONFLICT", true)) {
                WifiStateMachine.this.mIpManager.enableHandleGatewayConflict();
            }
            ProvisioningConfiguration prov;
            if (WifiStateMachine.this.mWifiConfigManager.isUsingStaticIp(WifiStateMachine.this.mLastNetworkId)) {
                StaticIpConfiguration config = WifiStateMachine.this.mWifiConfigManager.getStaticIpConfiguration(WifiStateMachine.this.mLastNetworkId);
                if (config == null || config.ipAddress == null) {
                    WifiStateMachine.this.logd("Static IP lacks address");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
                    return;
                }
                if (WifiStateMachine.this.enableIpReachabilityMonitor()) {
                    WifiStateMachine.this.mIpManager;
                    prov = IpManager.buildProvisioningConfiguration().withStaticConfiguration(config).withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
                } else {
                    WifiStateMachine.this.mIpManager;
                    prov = IpManager.buildProvisioningConfiguration().withStaticConfiguration(config).withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).withoutIpReachabilityMonitor().build();
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                WifiStateMachine.this.mIpManager.startProvisioning(prov);
                return;
            }
            if (WifiStateMachine.this.enableIpReachabilityMonitor()) {
                WifiStateMachine.this.mIpManager;
                prov = IpManager.buildProvisioningConfiguration().withPreDhcpAction().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
            } else {
                WifiStateMachine.this.mIpManager;
                prov = IpManager.buildProvisioningConfiguration().withPreDhcpAction().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).withoutIpReachabilityMonitor().build();
            }
            WifiConfiguration wifiCfg = WifiStateMachine.this.getCurrentWifiConfiguration();
            if (wifiCfg != null) {
                String ssid = wifiCfg.getPrintableSsid();
                DhcpResults record = (DhcpResults) WifiStateMachine.mDhcpResultMap.get(ssid);
                WifiStateMachine.this.logd("IP recover: get DhcpResult for ssid-->" + ssid + ", record-->" + record);
                WifiStateMachine.this.mIpManager.updatePastSuccessedDhcpResult(record);
            }
            WifiStateMachine.this.mIpManager.startProvisioning(prov);
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.obtainingIpWatchdogCount++;
            WifiStateMachine.this.logd("Start Dhcp Watchdog " + WifiStateMachine.this.obtainingIpWatchdogCount);
            WifiStateMachine.this.getWifiLinkLayerStats(true);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER, WifiStateMachine.this.obtainingIpWatchdogCount, 0), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC", Integer.valueOf(40000)).intValue());
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER /*131165*/:
                    if (message.arg1 != WifiStateMachine.this.obtainingIpWatchdogCount) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                    WifiStateMachine.this.logd("ObtainingIpAddress: Watchdog Triggered, count=" + WifiStateMachine.this.obtainingIpWatchdogCount);
                    WifiStateMachine.this.handleIpConfigurationLost();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_AUTO_SAVE_NETWORK /*131218*/:
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    return false;
                default:
                    return false;
            }
            return true;
        }
    }

    private class PppoeHandler extends Handler {
        private boolean mCancelCallback;
        private StateMachine mController;

        public PppoeHandler(Looper looper, StateMachine target) {
            super(looper);
            this.mController = target;
        }

        public void handleMessage(Message msg) {
            Log.d(WifiStateMachine.TAG, "Handle start PPPOE message!");
            DhcpResults pppoeResult = new DhcpResults();
            synchronized (this) {
                this.mCancelCallback = false;
            }
            WifiStateMachine.this.mPppoeInfo.status = Status.CONNECTING;
            WifiStateMachine.this.sendPppoeStateChangedBroadcast("PPPOE_STATE_CONNECTING");
            int result = NetworkUtils.runPPPOE(WifiStateMachine.this.mInterfaceName, WifiStateMachine.this.mPppoeConfig.timeout, WifiStateMachine.this.mPppoeConfig.username, WifiStateMachine.this.mPppoeConfig.password, WifiStateMachine.this.mPppoeConfig.lcp_echo_interval, WifiStateMachine.this.mPppoeConfig.lcp_echo_failure, WifiStateMachine.this.mPppoeConfig.mtu, WifiStateMachine.this.mPppoeConfig.mru, WifiStateMachine.this.mPppoeConfig.MSS, pppoeResult);
            Log.d(WifiStateMachine.TAG, "runPPPOE result:" + result);
            if (result == 0) {
                Log.d(WifiStateMachine.TAG, "PPPoE succeeded, pppoeResult:" + pppoeResult);
                synchronized (this) {
                    if (!this.mCancelCallback) {
                        this.mController.sendMessage(WifiStateMachine.EVENT_PPPOE_SUCCEEDED, pppoeResult);
                    }
                }
                return;
            }
            WifiStateMachine.this.stopPPPoE();
            WifiStateMachine.this.sendPppoeCompletedBroadcast("FAILURE", result);
            Log.d(WifiStateMachine.TAG, "PPPoE failed, error:" + NetworkUtils.getPPPOEError());
        }

        public synchronized void setCancelCallback(boolean cancelCallback) {
            this.mCancelCallback = cancelCallback;
        }
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log("RoamingState Enter mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.roamWatchdogCount++;
            WifiStateMachine.this.logd("Start Roam Watchdog " + WifiStateMachine.this.roamWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER, WifiStateMachine.this.roamWatchdogCount, 0), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_ROAM_GUARD_TIMER_MSEC", Integer.valueOf(WifiStateMachine.ROAM_GUARD_TIMER_MSEC)).intValue());
            this.mAssociated = false;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                    if (WifiStateMachine.this.roamWatchdogCount == message.arg1) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("roaming watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.mWifiMetrics.endConnectionEvent(9, 1);
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.mRoamFailCount = wifiStateMachine.mRoamFailCount + 1;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config != null) {
                        WifiStateMachine.this.mWifiLogger.captureBugReportData(3);
                        WifiStateMachine.this.mWifiConfigManager.noteRoamingFailure(config, WifiConfiguration.ROAMING_FAILURE_IP_CONFIG);
                    }
                    return false;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Roaming and CS doesnt want the network -> ignore");
                    }
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (!this.mAssociated) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("roaming and Network connection established");
                    }
                    if (WifiStateMachine.this.sLastNetworkId != message.arg1) {
                        DhcpClient.clearOffer();
                        WifiStateMachine.this.sLastNetworkId = message.arg1;
                    }
                    WifiStateMachine.this.mLastNetworkId = message.arg1;
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true);
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    WifiStateMachine.this.clearCurrentConfigBSSID("RoamingCompleted");
                    if (WifiStateMachine.this.isUsingDHCP()) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    } else {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                    }
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, WifiStateMachine.this.mLastNetworkId, message.arg2, WifiStateMachine.this.mLastBssid);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    String bssid = message.obj;
                    String target = "";
                    if (WifiStateMachine.this.mTargetRoamBSSID != null) {
                        target = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + bssid + " target=" + target);
                    if (bssid != null && bssid.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                            WifiStateMachine.this.handleNetworkDisconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        }
                    }
                    if (!(stateChangeResult == null || WifiStateMachine.this.mLastSupplicantState == stateChangeResult.state)) {
                        WifiStateMachine.this.mLastSupplicantState = stateChangeResult.state;
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED || stateChangeResult.state == SupplicantState.FOUR_WAY_HANDSHAKE || stateChangeResult.state == SupplicantState.GROUP_HANDSHAKE || stateChangeResult.state == SupplicantState.COMPLETED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            WifiStateMachine.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                        }
                        if (!(stateChangeResult.wifiSsid == null || stateChangeResult.wifiSsid.toString().isEmpty())) {
                            WifiStateMachine.this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
                            break;
                        }
                    }
                case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
                    WifiStateMachine.this.logd("SSID_TEMP_DISABLED nid=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " id=" + Integer.toString(message.arg1) + " isRoaming=" + WifiStateMachine.this.isRoaming() + " roam=" + WifiStateMachine.this.mAutoRoaming);
                    if (message.arg1 == WifiStateMachine.this.mLastNetworkId) {
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            WifiStateMachine.this.mWifiLogger.captureBugReportData(3);
                            WifiStateMachine.this.mWifiConfigManager.noteRoamingFailure(config, WifiConfiguration.ROAMING_FAILURE_AUTH_FAILURE);
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    }
                    return false;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Roaming state");
        }
    }

    class ScanModeState extends State {
        private int mLastOperationMode;

        ScanModeState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            this.mLastOperationMode = WifiStateMachine.this.mOperationalMode;
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.mWifiNative.setAlwaysScanState(1);
        }

        public void exit() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "exit\n");
            }
            WifiStateMachine.this.mWifiNative.setAlwaysScanState(0);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        if (message.arg1 == 3 && this.mLastOperationMode == 2) {
                            WifiStateMachine.this.setWifiState(1);
                            break;
                        }
                        return true;
                    }
                    if (this.mLastOperationMode == 3) {
                        WifiStateMachine.this.setWifiState(3);
                        WifiStateMachine.this.mWifiConfigManager.loadAndEnableAllNetworks();
                        WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                        WifiStateMachine.this.mSendScanResultsBroadcast = true;
                        if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.readConfigAndUpdate();
                        } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.readWifiNetworkRecord();
                            if (WifiStateMachine.this.autoSwitch) {
                                WifiStateMachine.this.mWifiNetworkStateTraker.disableNetworkWithoutInternet();
                            }
                        }
                    } else {
                        WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                    }
                    if (WifiStateMachine.this.autoSwitch) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - WifiStateMachine.this.mLastScanTime > WifiStateMachine.SMART_SCAN_INTERVAL) {
                            WifiStateMachine.this.startScan(WifiStateMachine.ENABLE_WIFI, 0, null, null);
                        } else if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.detectScanResult(currentTime);
                        } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.detectScanResult(currentTime);
                        }
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    WifiStateMachine.this.mOperationalMode = 1;
                    WifiStateMachine.this.mWifiOnScanCount = 0;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("SupplicantState= " + state);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    public static class SimAuthRequestData {
        String[] data;
        int networkId;
        int protocol;
        String ssid;
    }

    class SoftApState extends State {
        private SoftApManager mSoftApManager;

        private class SoftApListener implements Listener {
            /* synthetic */ SoftApListener(SoftApState this$1, SoftApListener softApListener) {
                this();
            }

            private SoftApListener() {
            }

            public void onStateChanged(int state, int reason) {
                if (state == 11) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_AP_STOPPED);
                } else if (state == 14) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_AP_FAILURE);
                }
                WifiStateMachine.this.setWifiApState(state, reason);
            }
        }

        SoftApState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            Message message = WifiStateMachine.this.getCurrentMessage();
            if (message.what == WifiStateMachine.CMD_START_AP) {
                WifiConfiguration config = message.obj;
                if (config == null) {
                    config = WifiStateMachine.this.mWifiApConfigStore.getApConfiguration();
                } else {
                    WifiStateMachine.this.mWifiApConfigStore.setApConfiguration(config);
                }
                WifiStateMachine.this.checkAndSetConnectivityInstance();
                this.mSoftApManager = WifiStateMachine.this.mFacade.makeSoftApManager(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mWifiNative, WifiStateMachine.this.mNwService, WifiStateMachine.this.mCm, WifiStateMachine.this.mCountryCode.getCountryCode(), WifiStateMachine.this.mWifiApConfigStore.getAllowed2GChannel(), new SoftApListener(this, null));
                this.mSoftApManager.start(config);
                return;
            }
            throw new RuntimeException("Illegal transition to SoftApState: " + message);
        }

        public void exit() {
            this.mSoftApManager = null;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    break;
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                    this.mSoftApManager.stop();
                    break;
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStartedState extends State {
        SupplicantStartedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(true);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            int defaultInterval = WifiStateMachine.this.mContext.getResources().getInteger(17694767);
            if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                ContentResolver contentResolver = WifiStateMachine.this.mContext.getContentResolver();
                String str = "wifi_supplicant_scan_interval_ms";
                if (!WifiStateMachine.this.mScreenOn) {
                    defaultInterval = WifiStateMachine.this.mContext.getResources().getInteger(17694770);
                }
                wifiStateMachine.mSupplicantScanIntervalMs = Global.getLong(contentResolver, str, (long) defaultInterval);
            } else {
                WifiStateMachine.this.mSupplicantScanIntervalMs = WifiStateMachine.this.mFacade.getLongSetting(WifiStateMachine.this.mContext, "wifi_supplicant_scan_interval_ms", (long) defaultInterval);
            }
            WifiStateMachine.this.mWifiNative.setScanInterval(((int) WifiStateMachine.this.mSupplicantScanIntervalMs) / 1000);
            WifiStateMachine.this.mWifiNative.setExternalSim(true);
            WifiStateMachine.this.mWifiNative.setDfsFlag(true);
            WifiStateMachine.this.setRandomMacOui();
            if (WifiStateMachine.this.getEnableAutoJoinWhenAssociated()) {
                WifiStateMachine.this.mWifiNative.enableAutoConnect(false);
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            if (WifiStateMachine.this.mWifiFwkExt != null && WifiStateMachine.this.mWifiFwkExt.hasNetworkSelection() != 0) {
                WifiStateMachine.this.mWifiNative.disconnect();
            }
        }

        public boolean processMessage(Message message) {
            int i = -1;
            WifiStateMachine.this.logStateAndMessage(message, this);
            boolean ok;
            WifiStateMachine wifiStateMachine;
            int i2;
            switch (message.what) {
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                    if (!WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    WifiStateMachine.this.loge("Failed to start soft AP with a running supplicant");
                    WifiStateMachine.this.setWifiApState(14, 0);
                    break;
                case WifiStateMachine.CMD_PING_SUPPLICANT /*131123*/:
                    ok = WifiStateMachine.this.mWifiNative.ping();
                    wifiStateMachine = WifiStateMachine.this;
                    i2 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_GET_CAPABILITY_FREQ /*131132*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiNative.getFreqCapability());
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.getWifiLinkLayerStats(WifiStateMachine.DBG));
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed, simSlot: " + message.arg1 + ", present: " + message.arg2);
                    }
                    WifiStateMachine.this.mWifiConfigManager.resetSimNetworks(message.arg1);
                    break;
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case WifiStateMachine.M_CMD_UPDATE_SETTINGS /*131242*/:
                    WifiStateMachine.this.updateAutoConnectSettings();
                    break;
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_ON /*131282*/:
                    ok = WifiStateMachine.this.mWifiNative.doCtiaTestOn();
                    wifiStateMachine = WifiStateMachine.this;
                    i2 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_OFF /*131283*/:
                    ok = WifiStateMachine.this.mWifiNative.doCtiaTestOff();
                    wifiStateMachine = WifiStateMachine.this;
                    i2 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.M_CMD_DO_CTIA_TEST_RATE /*131284*/:
                    ok = WifiStateMachine.this.mWifiNative.doCtiaTestRate(message.arg1);
                    wifiStateMachine = WifiStateMachine.this;
                    i2 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.M_CMD_FLUSH_BSS /*131292*/:
                    WifiStateMachine.this.mWifiNative.bssFlush();
                    break;
                case WifiStateMachine.M_CMD_GET_TEST_ENV /*131293*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiNative.getTestEnv(message.arg1));
                    break;
                case WifiStateMachine.CMD_WAIT_SCAN_RESULTS /*131354*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Send old scan result to user.");
                    }
                    WifiStateMachine.this.enableSpoofScanResults = true;
                    WifiStateMachine.this.sendScanResultsAvailableBroadcast(true);
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    WifiStateMachine.this.loge("Connection lost, restart supplicant");
                    WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    if (WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    } else {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, WifiStateMachine.SMART_SCAN_INTERVAL);
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                    boolean scanSucceeded;
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    if (message.what == WifiMonitor.SCAN_RESULTS_EVENT) {
                        scanSucceeded = true;
                    } else {
                        scanSucceeded = false;
                    }
                    if (scanSucceeded) {
                        WifiStateMachine.this.setScanResults();
                    }
                    if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                        WifiStateMachine.this.mShowReselectDialog = false;
                        Log.d(WifiStateMachine.TAG, "SCAN_RESULTS_EVENT, mScanForWeakSignal:" + WifiStateMachine.this.mScanForWeakSignal);
                        if (WifiStateMachine.this.mScanForWeakSignal) {
                            WifiStateMachine.this.showReselectionDialog();
                        }
                        WifiStateMachine.this.mDisconnectNetworkId = -1;
                    }
                    WifiStateMachine.this.loge("mIsFullScanOngoing: " + WifiStateMachine.this.mIsFullScanOngoing + ", mSendScanResultsBroadcast: " + WifiStateMachine.this.mSendScanResultsBroadcast);
                    if (WifiStateMachine.this.mIsFullScanOngoing || WifiStateMachine.this.mSendScanResultsBroadcast || WifiStateMachine.this.mWifiOnScanCount < 2 || (WifiStateMachine.this.getHandler() != null && WifiStateMachine.this.getHandler().hasMessages(WifiStateMachine.CMD_WAIT_SCAN_RESULTS))) {
                        WifiStateMachine.this.loge("mWifiOnScanCount: " + WifiStateMachine.this.mWifiOnScanCount);
                        if (scanSucceeded) {
                            WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WAIT_SCAN_RESULTS);
                            WifiStateMachine.this.scanResultsAvailable = true;
                            WifiStateMachine.this.enableSpoofScanResults = false;
                            WifiStateMachine.this.sendScanResultsAvailableBroadcast(scanSucceeded);
                        } else {
                            Log.d(WifiStateMachine.TAG, "SCAN_FAILED_EVENT, and not broadcast scan results available!");
                        }
                    }
                    WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                    wifiStateMachine2.mWifiOnScanCount = wifiStateMachine2.mWifiOnScanCount + 1;
                    WifiStateMachine.this.mSendScanResultsBroadcast = false;
                    WifiStateMachine.this.mIsScanOngoing = false;
                    WifiStateMachine.this.mIsFullScanOngoing = false;
                    if (WifiStateMachine.this.mBufferedScanMsg.size() > 0) {
                        WifiStateMachine.this.sendMessage((Message) WifiStateMachine.this.mBufferedScanMsg.remove());
                        break;
                    }
                    break;
                case WifiMonitor.WHOLE_CHIP_RESET_FAIL_EVENT /*147538*/:
                    Log.e(WifiStateMachine.TAG, "Receive whole chip reset fail, disable wifi!");
                    WifiStateMachine.this.setWifiState(4);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
        }
    }

    class SupplicantStartingState extends State {
        SupplicantStartingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
        }

        private void initializeWpsDetails() {
            String detail = WifiStateMachine.this.mPropertyService.get("ro.product.name", "");
            if (!WifiStateMachine.this.mWifiNative.setDeviceName(detail)) {
                WifiStateMachine.this.loge("Failed to set device name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.manufacturer", "");
            if (!WifiStateMachine.this.mWifiNative.setManufacturer(detail)) {
                WifiStateMachine.this.loge("Failed to set manufacturer " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelName(detail)) {
                WifiStateMachine.this.loge("Failed to set model name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set model number " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.serialno", "");
            if (!WifiStateMachine.this.mWifiNative.setSerialNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set serial number " + detail);
            }
            if (!WifiStateMachine.this.mWifiNative.setConfigMethods("physical_display virtual_push_button")) {
                WifiStateMachine.this.loge("Failed to set WPS config methods");
            }
            if (!WifiStateMachine.this.mWifiNative.setDeviceType(WifiStateMachine.this.mPrimaryDeviceType)) {
                WifiStateMachine.this.loge("Failed to set primary device type " + WifiStateMachine.this.mPrimaryDeviceType);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Supplicant connection established");
                    }
                    int wifi_setting_on = 0;
                    try {
                        wifi_setting_on = Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), "wifi_on");
                    } catch (SettingNotFoundException e) {
                        Global.putInt(WifiStateMachine.this.mContext.getContentResolver(), "wifi_on", 0);
                    }
                    if (WifiStateMachine.this.mOperationalMode != 3 || wifi_setting_on == 1) {
                        WifiStateMachine.this.setWifiState(3);
                    }
                    WifiStateMachine.this.mSupplicantRestartCount = 0;
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    WifiStateMachine.this.mLastBssid = null;
                    WifiStateMachine.this.mLastNetworkId = -1;
                    WifiStateMachine.this.mLastSignalLevel = -1;
                    WifiStateMachine.this.mRssiCount = 5;
                    for (int i = 0; i < 5; i++) {
                        WifiStateMachine.this.mRssiArray[i] = 0;
                    }
                    WifiStateMachine.this.log("Set debug level for wpa_supplicant.");
                    WifiStateMachine.this.setSupplicantLogLevel();
                    if (WifiStateMachine.this.mWifiNative != null) {
                        WifiStateMachine.this.mWifiNative.scan(null, null);
                    }
                    WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress());
                    WifiStateMachine.this.setFrequencyBand();
                    WifiStateMachine.this.mWifiNative.enableSaveConfig();
                    WifiStateMachine.this.mWifiConfigManager.loadAndEnableAllNetworks();
                    if (WifiStateMachine.this.mWifiConfigManager.mEnableVerboseLogging.get() > 0) {
                        WifiStateMachine.this.enableVerboseLogging(WifiStateMachine.this.mWifiConfigManager.mEnableVerboseLogging.get());
                    }
                    initializeWpsDetails();
                    WifiStateMachine.this.mConnectNetwork = false;
                    WifiStateMachine.this.mLastExplicitNetworkId = -1;
                    WifiStateMachine.this.mOnlineStartTime = 0;
                    WifiStateMachine.this.mUsingPppoe = false;
                    WifiStateMachine.this.mConnectNetwork = false;
                    if (WifiStateMachine.this.hasCustomizedAutoConnect()) {
                        WifiStateMachine.this.mWifiNative.setBssExpireAge(10);
                        WifiStateMachine.this.mWifiNative.setBssExpireCount(1);
                        WifiStateMachine.this.mDisconnectOperation = false;
                        WifiStateMachine.this.mScanForWeakSignal = false;
                        WifiStateMachine.this.mShowReselectDialog = false;
                        WifiStateMachine.this.mIpConfigLost = false;
                        WifiStateMachine.this.mLastCheckWeakSignalTime = 0;
                        if (!WifiStateMachine.this.mWifiFwkExt.shouldAutoConnect()) {
                            WifiStateMachine.this.disableAllNetworks(false);
                        }
                    }
                    if (!(WifiStateMachine.this.mSim1IccState.equals("LOADED") && WifiStateMachine.this.mSim2IccState.equals("LOADED"))) {
                        WifiStateMachine.this.log("iccState: (" + WifiStateMachine.this.mSim1IccState + "," + WifiStateMachine.this.mSim2IccState + "), check EAP SIM/AKA networks");
                        List<WifiConfiguration> networks = WifiStateMachine.this.mWifiConfigManager.getSavedNetworks();
                        if (networks != null) {
                            for (WifiConfiguration network : networks) {
                                if (TelephonyUtil.isSimConfig(network) && !WifiStateMachine.this.isConfigSimCardLoaded(network)) {
                                    WifiStateMachine.this.log("diable EAP SIM/AKA network let supplicant cannot auto connect, netId: " + network.networkId);
                                    WifiStateMachine.this.mWifiConfigManager.disableNetwork(network.networkId);
                                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(network.networkId), 10);
                                }
                            }
                        } else {
                            WifiStateMachine.this.log("Check for EAP_SIM_AKA, networks is null!");
                        }
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.readConfigAndUpdate();
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.readWifiNetworkRecord();
                        if (WifiStateMachine.this.autoSwitch) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.disableNetworkWithoutInternet();
                        }
                    }
                    WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(true);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mSupplicantRestartCount = wifiStateMachine.mSupplicantRestartCount + 1 > 5) {
                        WifiStateMachine.this.loge("Failed " + WifiStateMachine.this.mSupplicantRestartCount + " times to start supplicant, unload driver");
                        WifiStateMachine.this.mSupplicantRestartCount = 0;
                        WifiStateMachine.this.setWifiState(4);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.loge("Failed to setup control channel, restart supplicant");
                    WifiStateMachine.this.mWifiMonitor.killSupplicant(WifiStateMachine.this.mP2pSupported);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, WifiStateMachine.SMART_SCAN_INTERVAL);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStoppingState extends State {
        SupplicantStoppingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            WifiStateMachine.this.handleNetworkDisconnect();
            String suppState = System.getProperty("init.svc.wpa_supplicant");
            if (suppState == null) {
                suppState = "unknown";
            }
            String p2pSuppState = System.getProperty("init.svc.p2p_supplicant");
            if (p2pSuppState == null) {
                p2pSuppState = "unknown";
            }
            WifiStateMachine.this.logd("SupplicantStoppingState: stopSupplicant  init.svc.wpa_supplicant=" + suppState + " init.svc.p2p_supplicant=" + p2pSuppState);
            WifiStateMachine.this.mWifiMonitor.stopSupplicant();
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED, wifiStateMachine3.mSupplicantStopFailureToken = wifiStateMachine3.mSupplicantStopFailureToken + 1, 0), WifiStateMachine.SMART_SCAN_INTERVAL);
            if (WifiStateMachine.this.mOperationalMode != 3) {
                WifiStateMachine.this.setWifiState(0);
            }
            WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED /*131089*/:
                    if (message.arg1 == WifiStateMachine.this.mSupplicantStopFailureToken) {
                        WifiStateMachine.this.loge("Timed out on a supplicant stop, kill and proceed");
                        WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    WifiStateMachine.this.loge("Supplicant connection received while stopping");
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Supplicant connection lost");
                    }
                    WifiStateMachine.this.handleSupplicantConnectionLoss(false);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class UntrustedWifiNetworkFactory extends NetworkFactory {
        public UntrustedWifiNetworkFactory(Looper l, Context c, String tag, NetworkCapabilities f) {
            super(l, c, tag, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mUntrustedReqCount = wifiStateMachine.mUntrustedReqCount + 1 == 1 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        if (WifiStateMachine.this.mConnectionReqCount == 0) {
                            WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                        }
                        WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
                    }
                }
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mUntrustedReqCount = wifiStateMachine.mUntrustedReqCount - 1 == 0 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(false);
                        if (WifiStateMachine.this.mConnectionReqCount == 0) {
                            WifiStateMachine.this.loge("UntrustedWifiNetworkFactory:releaseNetworkFor");
                        }
                    }
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mUntrustedReqCount " + WifiStateMachine.this.mUntrustedReqCount);
        }
    }

    class WaitForP2pDisableState extends State {
        private State mTransitionToState;

        WaitForP2pDisableState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            switch (WifiStateMachine.this.getCurrentMessage().what) {
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                    this.mTransitionToState = WifiStateMachine.this.mSupplicantStoppingState;
                    break;
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                    this.mTransitionToState = WifiStateMachine.this.mDriverStoppingState;
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    this.mTransitionToState = WifiStateMachine.this.mInitialState;
                    break;
                default:
                    this.mTransitionToState = WifiStateMachine.this.mDriverStoppingState;
                    break;
            }
            WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                    WifiStateMachine.this.transitionTo(this.mTransitionToState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class WifiCfgUpdateHelper extends RomUpdateHelper {
        private static final String DATA_FILE_DIR = "/data/misc/wifi/WCNSS_qcom_cfg_new.ini";
        public static final String FILTER_NAME = "qcom_wifi_cfg";
        private static final String SYS_FILE_DIR = "/system/etc/wifi/WCNSS_qcom_cfg.ini";
        private static final String TAG = "WifiCfgUpdateHelper";

        public WifiCfgUpdateHelper(Context context) {
            super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
            setUpdateInfo(null, null);
        }
    }

    private class WifiNetworkAgent extends NetworkAgent {
        public WifiNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (WifiStateMachine.DBG) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                }
                if (WifiStateMachine.this.mWifiNetworkAvailable == null && WifiStateMachine.this.mWifiNetworkStateTraker == null) {
                    WifiStateMachine.this.unwantedNetwork(0);
                }
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (status == 2) {
                    if (WifiStateMachine.DBG) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.mNetworkDetectValid = false;
                    WifiStateMachine.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (WifiStateMachine.DBG) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.mNetworkDetectValid = true;
                    WifiStateMachine.this.doNetworkStatus(status);
                }
            }
        }

        protected void saveAcceptUnvalidated(boolean accept) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        protected void startPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void stopPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
                return;
            }
            int[] rssiVals = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiVals[rssiVals.length - 2] = WifiNetworkScoreCache.INVALID_NETWORK_SCORE;
            rssiVals[rssiVals.length - 1] = SupportedRates.MASK;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val > SupportedRates.MASK || val < WifiNetworkScoreCache.INVALID_NETWORK_SCORE) {
                    Log.e(WifiStateMachine.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            WifiStateMachine.this.mRssiRanges = rssiRange;
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
        }

        protected void preventAutomaticReconnect() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.unwantedNetwork(2);
            }
        }
    }

    private class WifiNetworkFactory extends NetworkFactory {
        public WifiNetworkFactory(Looper l, Context c, String TAG, NetworkCapabilities f) {
            super(l, c, TAG, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (wifiStateMachine.mConnectionReqCount = wifiStateMachine.mConnectionReqCount + 1 == 1 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
                    WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                }
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (wifiStateMachine.mConnectionReqCount = wifiStateMachine.mConnectionReqCount - 1 == 0 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
                    WifiStateMachine.this.loge("WifiNetworkFactory:releaseNetworkFor");
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mConnectionReqCount " + WifiStateMachine.this.mConnectionReqCount);
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.loge(getName() + "\n");
            }
            this.mSourceMessage = Message.obtain(WifiStateMachine.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_ENABLE_ALL_NETWORKS /*131127*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case 151553:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151565);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.wConnection = true;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Network connection lost");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.WPS_SUCCESS_EVENT);
                    WifiStateMachine.this.mConnectNetwork = true;
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    }
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.setManualConnect(false);
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                    }
                    WifiStateMachine.wConnection = false;
                    WifiStateMachine.wConnected = false;
                    WifiStateMachine.wStartWps = false;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.setManualConnect(false);
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                    }
                    WifiStateMachine.wConnection = false;
                    WifiStateMachine.wConnected = false;
                    WifiStateMachine.wStartWps = false;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.setManualConnect(false);
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                    }
                    WifiStateMachine.wConnection = false;
                    WifiStateMachine.wConnected = false;
                    WifiStateMachine.wStartWps = false;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Ignore Assoc reject event during WPS Connection");
                        break;
                    }
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 1);
                    break;
                case 151566:
                    if (WifiStateMachine.this.mWifiNative.cancelWps()) {
                        WifiStateMachine.this.replyToMessage(message, 151568);
                    } else {
                        WifiStateMachine.this.replyToMessage(message, 151567, 0);
                    }
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.setManualConnect(false);
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
                    }
                    WifiStateMachine.wConnection = false;
                    WifiStateMachine.wConnected = false;
                    WifiStateMachine.wStartWps = false;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
            WifiStateMachine.this.mWifiConfigManager.loadConfiguredNetworks();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiStateMachine.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiStateMachine.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiStateMachine.<clinit>():void");
    }

    protected void loge(String s) {
        Log.e(getName(), s);
    }

    protected void logd(String s) {
        Log.d(getName(), s);
    }

    protected void log(String s) {
        Log.d(getName(), s);
    }

    public void onRssiThresholdBreached(byte curRssi) {
        if (DBG) {
            Log.e(TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
        }
        sendMessage(CMD_RSSI_THRESHOLD_BREACH, curRssi);
    }

    public void processRssiThreshold(byte curRssi, int reason) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + curRssi);
            return;
        }
        for (int i = 0; i < this.mRssiRanges.length; i++) {
            if (curRssi < this.mRssiRanges[i]) {
                byte maxRssi = this.mRssiRanges[i];
                byte minRssi = this.mRssiRanges[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities(getCurrentWifiConfiguration());
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + startRssiMonitoringOffload(maxRssi, minRssi));
                break;
            }
        }
    }

    boolean isRoaming() {
        return this.mAutoRoaming;
    }

    public void autoRoamSetBSSID(int netId, String bssid) {
        autoRoamSetBSSID(this.mWifiConfigManager.getWifiConfiguration(netId), bssid);
    }

    public boolean autoRoamSetBSSID(WifiConfiguration config, String bssid) {
        boolean ret = true;
        if (this.mTargetRoamBSSID == null) {
            this.mTargetRoamBSSID = WifiLastResortWatchdog.BSSID_ANY;
        }
        if (bssid == null) {
            bssid = WifiLastResortWatchdog.BSSID_ANY;
        }
        if (config == null) {
            return false;
        }
        if (this.mTargetRoamBSSID != null && bssid.equals(this.mTargetRoamBSSID) && bssid.equals(config.BSSID)) {
            return false;
        }
        if (!this.mTargetRoamBSSID.equals(WifiLastResortWatchdog.BSSID_ANY) && bssid.equals(WifiLastResortWatchdog.BSSID_ANY)) {
            ret = false;
        }
        if (DBG) {
            logd("autoRoamSetBSSID " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        this.mWifiConfigManager.saveWifiConfigBSSID(config, bssid);
        return ret;
    }

    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (DBG) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (bssid == null) {
            bssid = WifiLastResortWatchdog.BSSID_ANY;
        }
        String networkSelectionBSSID = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
        if (networkSelectionBSSID == null || !networkSelectionBSSID.equals(bssid)) {
            if (DBG) {
                Log.d(TAG, "target set to " + config.SSID + ":" + bssid);
            }
            this.mTargetRoamBSSID = bssid;
            this.mWifiConfigManager.saveWifiConfigBSSID(config, bssid);
            return true;
        }
        if (DBG) {
            Log.d(TAG, "Current preferred BSSID is the same as the target one");
        }
        return false;
    }

    boolean recordUidIfAuthorized(WifiConfiguration config, int uid, boolean onlyAnnotate) {
        if (!this.mWifiConfigManager.isNetworkConfigured(config)) {
            config.creatorUid = uid;
            config.creatorName = this.mContext.getPackageManager().getNameForUid(uid);
        } else if (!this.mWifiConfigManager.canModifyNetwork(uid, config, onlyAnnotate)) {
            return false;
        }
        config.lastUpdateUid = uid;
        config.lastUpdateName = this.mContext.getPackageManager().getNameForUid(uid);
        return true;
    }

    boolean deferForUserInput(Message message, int netId, boolean allowOverride) {
        WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(netId);
        if (config == null) {
            logd("deferForUserInput: configuration for netId=" + netId + " not stored");
            return true;
        }
        switch (config.userApproved) {
            case 1:
            case 2:
                return false;
            default:
                config.userApproved = 1;
                return false;
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

    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode) {
        super(TAG, looper);
        this.mVerboseLoggingLevel = 0;
        this.didBlackListBSSID = false;
        this.mP2pConnected = new AtomicBoolean(false);
        this.mTemporarilyDisconnectWifi = false;
        this.mScanResults = new ArrayList();
        this.mScanResultsLock = new Object();
        this.mScreenOn = false;
        this.mLastSignalLevel = -1;
        this.mRssiCount = 5;
        this.mRssiArray = new int[]{0, 0, 0, 0, 0};
        this.mIndex = 0;
        this.linkDebouncing = false;
        this.testNetworkDisconnect = false;
        this.mEnableRssiPolling = false;
        this.mRssiPollToken = 0;
        this.mOperationalMode = 1;
        this.mIsScanOngoing = false;
        this.mIsFullScanOngoing = true;
        this.mSendScanResultsBroadcast = false;
        this.mBufferedScanMsg = new LinkedList();
        this.mScanWorkSource = null;
        this.mScreenBroadcastReceived = new AtomicBoolean(false);
        this.mBluetoothConnectionActive = false;
        this.mSupplicantRestartCount = 0;
        this.mSupplicantStopFailureToken = 0;
        this.mTetherToken = 0;
        this.mDriverStartToken = 0;
        this.mPeriodicScanToken = 0;
        this.mDhcpResultsLock = new Object();
        this.mWifiLinkLayerStatsSupported = 4;
        this.mAutoRoaming = false;
        this.mRoamFailCount = 0;
        this.sLastNetworkId = -1;
        this.mTargetRoamBSSID = WifiLastResortWatchdog.BSSID_ANY;
        this.mTargetNetworkId = -1;
        this.mLastDriverRoamAttempt = 0;
        this.targetWificonfiguration = null;
        this.lastSavedConfigurationAttempt = null;
        this.lastForgetConfigurationAttempt = null;
        this.mFrequencyBand = new AtomicInteger(0);
        this.mReplyChannel = new AsyncChannel();
        this.mConnectionReqCount = 0;
        this.mUntrustedReqCount = 0;
        this.mWifiReqCountLock = new Object();
        this.mWhiteListedSsids = null;
        this.mWifiConnectionStatistics = new WifiConnectionStatistics();
        this.mNetworkCapabilitiesFilter = new NetworkCapabilities();
        this.mNetworkMisc = new NetworkMisc();
        this.testNetworkDisconnectCounter = 0;
        this.roamWatchdogCount = 0;
        this.obtainingIpWatchdogCount = 0;
        this.disconnectingWatchdogCount = 0;
        this.ipReachabilityMonitorCount = 0;
        this.mIsListeningIpReachabilityLost = false;
        this.enableIpReachabilityMonitor = new AtomicBoolean(true);
        this.enableIpReachabilityMonitorEnhancement = new AtomicBoolean(true);
        this.mLastSupplicantState = SupplicantState.DISCONNECTED;
        this.mSuspendOptNeedsDisabled = 0;
        this.mUserWantsSuspendOpt = new AtomicBoolean(true);
        this.mRunningBeaconCount = 0;
        this.mDefaultState = new DefaultState();
        this.mInitialState = new InitialState();
        this.mSupplicantStartingState = new SupplicantStartingState();
        this.mSupplicantStartedState = new SupplicantStartedState();
        this.mSupplicantStoppingState = new SupplicantStoppingState();
        this.mDriverStartingState = new DriverStartingState();
        this.mDriverStartedState = new DriverStartedState();
        this.mWaitForP2pDisableState = new WaitForP2pDisableState();
        this.mDriverStoppingState = new DriverStoppingState();
        this.mDriverStoppedState = new DriverStoppedState();
        this.mScanModeState = new ScanModeState();
        this.mConnectModeState = new ConnectModeState();
        this.mL2ConnectedState = new L2ConnectedState();
        this.mObtainingIpState = new ObtainingIpState();
        this.mCaptiveState = new CaptiveState();
        this.mConnectedState = new ConnectedState();
        this.mRoamingState = new RoamingState();
        this.mDisconnectingState = new DisconnectingState();
        this.mDisconnectedState = new DisconnectedState();
        this.mWpsRunningState = new WpsRunningState();
        this.mSoftApState = new SoftApState();
        this.mHasInternetAccess = false;
        this.mCheckInetAccessSeq = 0;
        this.mDriverRoaming = false;
        this.mWifiCfgUpdateHelper = null;
        this.mWifiRomUpdateHelper = null;
        this.mWifiState = new AtomicInteger(1);
        this.mWifiApState = new AtomicInteger(11);
        this.mIsRunning = false;
        this.mReportedRunning = false;
        this.mRunningWifiUids = new WorkSource();
        this.mLastRunningWifiUids = new WorkSource();
        this.mConnectedId = -1;
        this.mLastScanTime = 0;
        this.mNetworkDetectValid = false;
        this.mFromKeylogVerbose = false;
        this.mScreenOnTime = 0;
        this.mScreenOffTime = 0;
        this.mIdleScanTimes = 0;
        this.mAppScanTimes = 0;
        this.mIdleRenewTimes = 0;
        this.mIdleGroupTimes = 0;
        this.mIdleDisConnTimes = 0;
        this.RENEW_FLAG = 1;
        this.GROUP_FLAG = 16;
        this.SCAN_FLAG = 256;
        this.DISCONN_FLAG = 4096;
        this.THIRD_APP_SCAN_FREQ = 1;
        this.THIRD_APP_SCAN_COUNT = 10;
        this.mPowerState = SupplicantState.UNINITIALIZED;
        this.mUidList = new ArrayList();
        this.mSystemUiUid = -1;
        this.mDisconnectOperation = false;
        this.mScanForWeakSignal = false;
        this.mShowReselectDialog = false;
        this.mIpConfigLost = false;
        this.mDisconnectNetworkId = -1;
        this.mLastExplicitNetworkId = -1;
        this.mLastCheckWeakSignalTime = 0;
        this.mWifiOnScanCount = 0;
        this.mStopScanStarted = new AtomicBoolean(false);
        this.mConnectNetwork = false;
        this.mUsingPppoe = false;
        this.mOnlineStartTime = 0;
        this.mMtkCtpppoe = false;
        this.mDontReconnectAndScan = new AtomicBoolean(false);
        this.mDontReconnect = new AtomicBoolean(false);
        this.mHotspotOptimization = false;
        this.mIsNewAssociatedBssid = false;
        this.mSim1IccState = "UNKNOWN";
        this.mSim2IccState = "UNKNOWN";
        this.mHandler = getHandler();
        this.mEnableRssiSmoothing = false;
        this.mAffectRoaming = false;
        this.mRssiSmoothingThreshold = 11;
        this.mCurSmoothRssi = -127;
        this.mTagetRssi = -127;
        this.mAnimator = new ValueAnimator();
        this.mNextAnimate = 0;
        this.closedByRestartInSilence = false;
        this.fetchPKTCount = 0;
        this.SILENCE_RESTART_SOURCE = 1000;
        this.mapKey = "mapKey-";
        this.mAssertProxy = null;
        this.mLastSelectEvtTimeStamp = 0;
        this.mLastScanPermissionUpdate = 0;
        this.mConnectedModeGScanOffloadStarted = false;
        this.mAggressiveHandover = 0;
        this.mDisconnectedTimeStamp = 0;
        this.lastConnectAttemptTimestamp = 0;
        this.lastScanFreqs = null;
        this.messageHandlingStatus = 0;
        this.mOnTime = 0;
        this.mTxTime = 0;
        this.mRxTime = 0;
        this.mOnTimeScreenStateChange = 0;
        this.lastOntimeReportTimeStamp = 0;
        this.lastScreenStateChangeTimeStamp = 0;
        this.mOnTimeLastReport = 0;
        this.mTxTimeLastReport = 0;
        this.mRxTimeLastReport = 0;
        this.lastLinkLayerStatsUpdate = 0;
        this.mWifiScoreReport = null;
        this.mLastDetailedState = DetailedState.IDLE;
        this.scanResultsAvailable = false;
        this.mScanCount = 0;
        this.enableSpoofScanResults = false;
        this.currentSim = 1;
        this.mRetry = false;
        this.changesim = false;
        this.mRetryCount = 0;
        this.isSingtelConnecting = false;
        this.lastRecord = "Singtel WIFI";
        this.SingtelWIFI = "Singtel WIFI";
        this.WirelessSGx = "Wireless@SGx";
        this.mWifiFwkExt = (IWifiFwkExt) MPlugin.createInstance(IWifiFwkExt.class.getName(), context);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mWifiLastResortWatchdog = wifiInjector.getWifiLastResortWatchdog();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = WifiNative.getWlanNativeInterface();
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWifiNative.initContext(this.mContext);
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
        this.mWifiRomUpdateHelper = new WifiRomUpdateHelper(this.mContext);
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.enableVerboseLogging(getVerboseLoggingLevel());
        }
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mNwService = INetworkManagementService.Stub.asInterface(this.mFacade.getService("network_management"));
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiConfigManager = this.mFacade.makeWifiConfigManager(context, this.mWifiNative, facade, this.mWifiInjector.getClock(), userManager, this.mWifiInjector.getKeyStore());
        this.mWifiMonitor = WifiMonitor.getInstance();
        if (this.mContext.getResources().getBoolean(17956891)) {
            this.mWifiLogger = facade.makeRealLogger(this.mContext, this, this.mWifiNative, this.mBuildProperties);
        } else {
            this.mWifiLogger = facade.makeBaseLogger();
        }
        this.mWifiInfo = new WifiInfo();
        this.mWifiQualifiedNetworkSelector = new WifiQualifiedNetworkSelector(this.mWifiConfigManager, this.mContext, this.mWifiInfo, this.mWifiInjector.getClock());
        this.mWifiQualifiedNetworkSelector.setWifiFwkExt(this.mWifiFwkExt);
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mFacade.setWifiConfigManagerStatemachine(this.mWifiConfigManager, this);
        this.mFacade.makeSupplicantWifiStatemachine(this.mSupplicantStateTracker, this);
        this.mLinkProperties = new LinkProperties();
        this.mWifiP2pServiceImpl = (WifiP2pServiceImpl) IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mIpManager = this.mFacade.makeIpManager(this.mContext, this.mInterfaceName, new IpManagerCallback());
        this.mIpManager.setMulticastFilter(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mWifiFwkExt != null) {
            this.mDefaultFrameworkScanIntervalMs = this.mWifiFwkExt.defaultFrameworkScanIntervalMs();
        } else {
            int period = this.mContext.getResources().getInteger(17694770);
            if (period < 10000) {
                period = 10000;
            }
            this.mDefaultFrameworkScanIntervalMs = period;
        }
        this.mNoNetworksPeriodicScan = this.mContext.getResources().getInteger(17694771);
        this.mBackgroundScanSupported = this.mContext.getResources().getBoolean(17956888);
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039416);
        this.mCountryCode = countryCode;
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
        this.mNetworkCapabilitiesFilter.addCapability(13);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1048576);
        this.mDfltNetworkCapabilities = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(WifiStateMachine.TAG, "onReceive, action:" + action);
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 1);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 0);
                }
            }
        }, filter);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_BOOT_COMPLETED);
            }
        }, new IntentFilter("android.intent.action.LOCKED_BOOT_COMPLETED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean netValid = intent.getBooleanExtra(WifiStateMachine.EXTRA_NETWORK_STATE, false);
                if (WifiStateMachine.DBG) {
                    WifiStateMachine.this.logd("[N18] onReceive OPPO_WIFI_NET_STATE change, netState = " + netValid);
                }
                if (netValid) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_MTU_PROBER);
                }
            }
        }, new IntentFilter(ACTION_WIFI_NETWORK_STATE));
        this.mAssertProxy = OppoAssertTip.getInstance();
        this.mRestartIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_RESTART_WIFI, null), 0);
        this.mOpenWifiIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_START_WIFI, null), 0);
        IntentFilter restartfilter = new IntentFilter();
        restartfilter.addAction(ACTION_RESTART_WIFI);
        restartfilter.addAction(ACTION_START_WIFI);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                WifiStateMachine.this.log("receive action = " + action);
                if (action.equals(WifiStateMachine.ACTION_RESTART_WIFI)) {
                    if (!WifiStateMachine.this.checkTimeInMorning()) {
                        WifiStateMachine.this.log("fool-proof,now is daytime so restart next day at 1 o'clock");
                        WifiStateMachine.this.mAlarmManager.set(0, WifiStateMachine.this.caculateTimeIntoMillis(1, WifiStateMachine.this.getRandomTime(1, 5), WifiStateMachine.this.getRandomTime(0, 60)), WifiStateMachine.this.mRestartIntent);
                    } else if (WifiStateMachine.this.mScreenOn) {
                        WifiStateMachine.this.resetRestartAlarm();
                    } else if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState) {
                        WifiStateMachine.this.sendMessageDelayed(151572, 1000, 1000);
                    } else {
                        WifiStateMachine.this.log("fool-proof,close wifi in silence and open after screen on or at 6 AM");
                        WifiStateMachine.this.setStatistics("silence", "wifi_restart_in_silence");
                        WifiStateMachine.this.resetAlarmCount = 0;
                        WifiStateMachine.this.closedByRestartInSilence = true;
                        WifiStateMachine.this.setSupplicantRunning(false);
                        WifiStateMachine.this.mAlarmManager.setExact(0, WifiStateMachine.this.caculateTimeIntoMillis(0, 5, 0), WifiStateMachine.this.mOpenWifiIntent);
                    }
                } else if (action.equals(WifiStateMachine.ACTION_START_WIFI) && WifiStateMachine.this.closedByRestartInSilence && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mInitialState) {
                    WifiStateMachine.this.log("fool-proof,doing start wifi at 5 o'clock");
                    WifiStateMachine.this.setSupplicantRunning(true);
                    WifiStateMachine.this.closedByRestartInSilence = false;
                }
            }
        }, restartfilter);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                wifiStateMachine.autoSwitch = z;
                if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                    WifiStateMachine.this.mWifiNetworkAvailable.setAutoSwitch(WifiStateMachine.this.autoSwitch);
                } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                    WifiStateMachine.this.mWifiNetworkStateTraker.setAutoSwitch(WifiStateMachine.this.autoSwitch);
                }
                Log.d(WifiStateMachine.TAG, "onChange autoSwitch= " + WifiStateMachine.this.autoSwitch);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                if (WifiStateMachine.this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant")) {
                    boolean isWlanAssistant = Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_ASSISTANT_ROMUPDATE, 1) == 1;
                    Log.d(WifiStateMachine.TAG, "onChange wa= " + isWlanAssistant);
                    WifiStateMachine wifiStateMachine;
                    if (isWlanAssistant) {
                        if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.setFeature(false);
                            WifiStateMachine.this.mWifiNetworkAvailable = null;
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker == null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker = WifiStateMachine.this.makeWifiNetworkStateTracker();
                            WifiStateMachine.this.mWifiNetworkStateTraker.setFeatureState(true);
                            WifiStateMachine.this.mWifiNetworkStateTraker.enableVerboseLogging(WifiStateMachine.this.getVerboseLoggingLevel());
                            wifiStateMachine = WifiStateMachine.this;
                            if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                                z = false;
                            }
                            wifiStateMachine.autoSwitch = z;
                            WifiStateMachine.this.mWifiNetworkStateTraker.setAutoSwitch(WifiStateMachine.this.autoSwitch);
                            return;
                        }
                        return;
                    }
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.setFeatureState(false);
                        WifiStateMachine.this.mWifiNetworkStateTraker.updateWifiState(-1);
                        WifiStateMachine.this.mWifiNetworkStateTraker = null;
                    }
                    if (WifiStateMachine.this.mWifiNetworkAvailable == null) {
                        WifiStateMachine.this.mWifiNetworkAvailable = WifiStateMachine.this.makeWifiNetworkAvailable();
                        WifiStateMachine.this.mWifiNetworkAvailable.setFeature(true);
                        WifiStateMachine.this.mWifiNetworkAvailable.enableVerboseLogging(WifiStateMachine.this.getVerboseLoggingLevel());
                        wifiStateMachine = WifiStateMachine.this;
                        if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                            z = false;
                        }
                        wifiStateMachine.autoSwitch = z;
                        WifiStateMachine.this.mWifiNetworkAvailable.setAutoSwitch(WifiStateMachine.this.autoSwitch);
                    }
                }
            }
        });
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039454);
        this.enableIpReachabilityMonitor.set(this.mContext.getResources().getBoolean(135004165));
        log("enableIpReachabilityMonitor: " + this.enableIpReachabilityMonitor.get());
        this.enableIpReachabilityMonitorEnhancement.set(this.mContext.getResources().getBoolean(135004166));
        log("enableIpReachabilityMonitorEnhancement: " + this.enableIpReachabilityMonitorEnhancement.get());
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSupplicantStartingState, this.mDefaultState);
        addState(this.mSupplicantStartedState, this.mDefaultState);
        addState(this.mDriverStartingState, this.mSupplicantStartedState);
        addState(this.mDriverStartedState, this.mSupplicantStartedState);
        addState(this.mScanModeState, this.mDriverStartedState);
        addState(this.mConnectModeState, this.mDriverStartedState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mCaptiveState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        addState(this.mWaitForP2pDisableState, this.mSupplicantStartedState);
        addState(this.mDriverStoppingState, this.mSupplicantStartedState);
        addState(this.mDriverStoppedState, this.mSupplicantStartedState);
        addState(this.mSupplicantStoppingState, this.mDefaultState);
        addState(this.mSoftApState, this.mDefaultState);
        setInitialState(this.mInitialState);
        initializeExtra();
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        HandlerThread handlerThread = new HandlerThread("CheckInternetAccess");
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper());
        start();
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.DRIVER_HUNG_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_REENABLED, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_TEMP_DISABLED, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_CONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_DISCONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WAPI_NO_CERTIFICATION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NEW_PAC_UPDATED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WHOLE_CHIP_RESET_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.TDLS_CONNECTED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.TDLS_DISCONNECTED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WRONG_KEY_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SELECT_NETWORK_EVENT, getHandler());
        if (isWlanAssistantEnable()) {
            this.mWifiNetworkStateTraker = new WifiNetworkStateTraker(context, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler());
            this.autoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
            this.mWifiNetworkStateTraker.setAutoSwitch(this.autoSwitch);
        } else {
            this.mWifiNetworkAvailable = new WifiNetworkAvailable(context, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker);
            this.autoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
            this.mWifiNetworkAvailable.setAutoSwitch(this.autoSwitch);
        }
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        intent.putExtra("scan_enabled", 1);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", 1048576, 0);
        } catch (NameNotFoundException e) {
            loge("Unable to resolve SystemUI's UID.");
        }
        this.mVerboseLoggingLevel = SystemProperties.getBoolean("persist.sys.assert.panic", false) ? 1 : 0;
        updateLoggingLevel();
        this.mWifiCfgUpdateHelper = new WifiCfgUpdateHelper(this.mContext);
        OppoAutoConnectManager.init(this.mContext, this, this.mWifiConfigManager, this.mWifiNetworkStateTraker, this.mWifiNative, this.mWifiRomUpdateHelper);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(getVerboseLoggingLevel());
        }
    }

    private void stopIpManager() {
        handlePostDhcpSetup();
        this.mIpManager.stop();
    }

    PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return this.mFacade.getBroadcast(this.mContext, requestCode, intent, 0);
    }

    int getVerboseLoggingLevel() {
        int i = 0;
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            i = 1;
        }
        this.mVerboseLoggingLevel = i;
        return this.mVerboseLoggingLevel;
    }

    void enableVerboseLogging(int verbose) {
        this.mFromKeylogVerbose = false;
        if (this.mVerboseLoggingLevel == verbose) {
            setSupplicantLogLevel();
            return;
        }
        this.mVerboseLoggingLevel = verbose;
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        updateLoggingLevel();
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.enableVerboseLogging(verbose);
        }
    }

    void setSupplicantLogLevel() {
        if (this.mVerboseLoggingLevel > 0) {
            this.mWifiNative.setSupplicantLogLevel("DEBUG");
        } else {
            this.mWifiNative.setSupplicantLogLevel("INFO");
        }
    }

    void updateLoggingLevel() {
        boolean z;
        if (this.mVerboseLoggingLevel > 0) {
            DBG = true;
            MDBG = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS : 3000);
        } else {
            DBG = false;
            MDBG = false;
            setLogRecSize(100);
        }
        if (this.mVerboseLoggingLevel > 0) {
            z = true;
        } else {
            z = false;
        }
        configureVerboseHalLogging(z);
        setSupplicantLogLevel();
        this.mCountryCode.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiLogger.startLogging(DBG);
        this.mWifiMonitor.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiNative.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiConfigManager.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mSupplicantStateTracker.enableVerboseLogging(this.mVerboseLoggingLevel);
        if (this.mWifiNetworkAvailable != null) {
            this.mWifiNetworkAvailable.enableVerboseLogging(this.mVerboseLoggingLevel);
        } else if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.enableVerboseLogging(this.mVerboseLoggingLevel);
        }
        this.mWifiQualifiedNetworkSelector.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiLastResortWatchdog.enableVerboseLogging(this.mVerboseLoggingLevel);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.enableVerboseLogging(this.mVerboseLoggingLevel);
        }
        if (this.mWifiScanner != null) {
            this.mWifiScanner.enableVerboseLogging(this.mVerboseLoggingLevel);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(this.mVerboseLoggingLevel);
        }
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    void updateAssociatedScanPermission() {
    }

    int getAggressiveHandover() {
        return this.mAggressiveHandover;
    }

    void enableAggressiveHandover(int enabled) {
        this.mAggressiveHandover = enabled;
    }

    public void clearANQPCache() {
        this.mWifiConfigManager.trimANQPCache(true);
    }

    public void setAllowScansWithTraffic(int enabled) {
        this.mWifiConfigManager.mAlwaysEnableScansWhileAssociated.set(enabled);
    }

    public int getAllowScansWithTraffic() {
        return this.mWifiConfigManager.mAlwaysEnableScansWhileAssociated.get();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        sendMessage(CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED, enabled ? 1 : 0);
        return true;
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mWifiConfigManager.getEnableAutoJoinWhenAssociated();
    }

    private boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039417);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[3];
        ouiBytes[0] = (byte) (Integer.parseInt(ouiParts[0], 16) & 255);
        ouiBytes[1] = (byte) (Integer.parseInt(ouiParts[1], 16) & 255);
        ouiBytes[2] = (byte) (Integer.parseInt(ouiParts[2], 16) & 255);
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    public boolean syncPingSupplicant(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_PING_SUPPLICANT);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public void startScan(int callingUid, int scanCounter, ScanSettings settings, WorkSource workSource) {
        if (getCurrentState() == this.mConnectedState && rejectAppScan(callingUid)) {
            if (getHandler() == null || !getHandler().hasMessages(CMD_WAIT_SCAN_RESULTS)) {
                if (DBG) {
                    loge("[FN5] Delay to broadcast old scan results.");
                }
                sendMessageDelayed(CMD_WAIT_SCAN_RESULTS, 2000);
            } else if (DBG) {
                loge("[FN5] waiting for scan results.");
            }
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, settings);
        bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, workSource);
        bundle.putLong(SCAN_REQUEST_TIME, System.currentTimeMillis());
        sendMessage(CMD_START_SCAN, callingUid, scanCounter, bundle);
        if (!(this.mScreenOn || callingUid <= 0 || callingUid == 1010 || callingUid == 1000)) {
            List<String> navigateAppList = new ArrayList();
            String pkg = this.mContext.getPackageManager().getNameForUid(callingUid);
            if (this.mContext != null) {
                LocationManager mLocationManager = (LocationManager) this.mContext.getSystemService("location");
                if (mLocationManager != null) {
                    navigateAppList = mLocationManager.getInUsePackagesList();
                }
            }
            if (navigateAppList != null && navigateAppList.size() > 0 && navigateAppList.contains(pkg)) {
                return;
            }
        }
        if (!this.mScreenOn) {
            detectAndreportOPPOGuard(callingUid);
        }
    }

    public long getDisconnectedTimeMilli() {
        if (getCurrentState() != this.mDisconnectedState || this.mDisconnectedTimeStamp == 0) {
            return 0;
        }
        return System.currentTimeMillis() - this.mDisconnectedTimeStamp;
    }

    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = System.currentTimeMillis();
        if (this.lastConnectAttemptTimestamp == 0 || now - this.lastConnectAttemptTimestamp >= 10000) {
            return true;
        }
        sendMessageDelayed(Message.obtain(msg), 11000 - (now - this.lastConnectAttemptTimestamp));
        return false;
    }

    String reportOnTime() {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        int on = this.mOnTime - this.mOnTimeLastReport;
        this.mOnTimeLastReport = this.mOnTime;
        int tx = this.mTxTime - this.mTxTimeLastReport;
        this.mTxTimeLastReport = this.mTxTime;
        int rx = this.mRxTime - this.mRxTimeLastReport;
        this.mRxTimeLastReport = this.mRxTime;
        int period = (int) (now - this.lastOntimeReportTimeStamp);
        this.lastOntimeReportTimeStamp = now;
        try {
            Object[] objArr = new Object[4];
            objArr[0] = Integer.valueOf(on);
            objArr[1] = Integer.valueOf(tx);
            objArr[2] = Integer.valueOf(rx);
            objArr[3] = Integer.valueOf(period);
            sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", objArr));
            on = this.mOnTime - this.mOnTimeScreenStateChange;
            period = (int) (now - this.lastScreenStateChangeTimeStamp);
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(on);
            objArr[1] = Integer.valueOf(period);
            sb.append(String.format(" from screen [on:%d period:%d]", objArr));
            return sb.toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "reportOnTime fatal exception, on=" + on + " tx=" + tx + " rx=" + rx + " period=" + period);
            log(e.toString());
            return "";
        }
    }

    WifiLinkLayerStats getWifiLinkLayerStats(boolean dbg) {
        WifiLinkLayerStats stats = null;
        if (this.mWifiLinkLayerStatsSupported > 0) {
            String name = "wlan0";
            stats = this.mWifiNative.getWifiLinkLayerStats(name);
            if (name != null && stats == null && this.mWifiLinkLayerStatsSupported > 0) {
                this.mWifiLinkLayerStatsSupported--;
            } else if (stats != null) {
                this.lastLinkLayerStatsUpdate = System.currentTimeMillis();
                this.mOnTime = stats.on_time;
                this.mTxTime = stats.tx_time;
                this.mRxTime = stats.rx_time;
                this.mRunningBeaconCount = stats.beacon_rx;
            }
        }
        if (stats == null || this.mWifiLinkLayerStatsSupported <= 0) {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mInterfaceName), this.mFacade.getRxPackets(this.mInterfaceName));
        } else {
            this.mWifiInfo.updatePacketRates(stats);
        }
        return stats;
    }

    int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        int ret = this.mWifiNative.startSendingOffloadedPacket(slot, packetData, intervalSeconds * 1000);
        if (ret == 0) {
            return 0;
        }
        loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
        return -31;
    }

    int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    int startRssiMonitoringOffload(byte maxRssi, byte minRssi) {
        return this.mWifiNative.startRssiMonitoring(maxRssi, minRssi, this);
    }

    int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring();
    }

    private void handleScanRequest(Message message) {
        ScanSettings settings = null;
        Parcelable parcelable = null;
        Bundle bundle = message.obj;
        if (bundle != null) {
            settings = (ScanSettings) bundle.getParcelable(CUSTOMIZED_SCAN_SETTING);
            parcelable = (WorkSource) bundle.getParcelable(CUSTOMIZED_SCAN_WORKSOURCE);
        }
        Set freqs = null;
        if (!(settings == null || settings.channelSet == null)) {
            freqs = new HashSet();
            for (WifiChannel channel : settings.channelSet) {
                freqs.add(Integer.valueOf(channel.freqMHz));
            }
        }
        if (startScanNative(freqs, this.mWifiConfigManager.getHiddenConfiguredNetworkIds(), parcelable)) {
            if (freqs == null) {
                this.mBufferedScanMsg.clear();
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_OK;
            if (parcelable != null) {
                this.mSendScanResultsBroadcast = true;
            }
            return;
        }
        if (!this.mIsScanOngoing) {
            if (this.mBufferedScanMsg.size() > 0) {
                sendMessage((Message) this.mBufferedScanMsg.remove());
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
        } else if (this.mIsFullScanOngoing) {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
        } else {
            if (freqs == null) {
                this.mBufferedScanMsg.clear();
            }
            if (this.mBufferedScanMsg.size() < 10) {
                this.mBufferedScanMsg.add(obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle));
            } else {
                bundle = new Bundle();
                bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, null);
                bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, parcelable);
                Message msg = obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle);
                this.mBufferedScanMsg.clear();
                this.mBufferedScanMsg.add(msg);
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_LOOPED;
        }
    }

    private boolean startScanNative(Set<Integer> freqs, Set<Integer> hiddenNetworkIds, WorkSource workSource) {
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        if (freqs == null) {
            settings.band = 7;
        } else {
            settings.band = 0;
            int index = 0;
            settings.channels = new ChannelSpec[freqs.size()];
            for (Integer freq : freqs) {
                int index2 = index + 1;
                settings.channels[index] = new ChannelSpec(freq.intValue());
                index = index2;
            }
        }
        settings.reportEvents = 3;
        if (hiddenNetworkIds != null && hiddenNetworkIds.size() > 0) {
            int i = 0;
            settings.hiddenNetworkIds = new int[hiddenNetworkIds.size()];
            for (Integer netId : hiddenNetworkIds) {
                int i2 = i + 1;
                settings.hiddenNetworkIds[i] = netId.intValue();
                i = i2;
            }
        }
        this.mWifiScanner.startScan(settings, new ScanListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason, String description) {
                WifiStateMachine.this.mIsScanOngoing = false;
                WifiStateMachine.this.mIsFullScanOngoing = false;
            }

            public void onResults(ScanData[] results) {
            }

            public void onFullResult(ScanResult fullScanResult) {
            }

            public void onPeriodChanged(int periodInMs) {
            }
        }, workSource);
        this.mIsScanOngoing = true;
        this.mIsFullScanOngoing = freqs == null;
        this.lastScanFreqs = freqs;
        return true;
    }

    public void setSupplicantRunning(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_SUPPLICANT);
        } else {
            sendMessage(CMD_STOP_SUPPLICANT);
        }
    }

    public void setHostApRunning(WifiConfiguration wifiConfig, boolean enable) {
        if (enable) {
            sendMessage(CMD_START_AP, wifiConfig);
        } else {
            sendMessage(CMD_STOP_AP);
        }
    }

    public void setWifiApConfiguration(WifiConfiguration config) {
        this.mWifiApConfigStore.setApConfiguration(config);
    }

    public WifiConfiguration syncGetWifiApConfiguration() {
        return this.mWifiApConfigStore.getApConfiguration();
    }

    public int syncGetWifiState() {
        return this.mWifiState.get();
    }

    public String syncGetWifiStateByName() {
        switch (this.mWifiState.get()) {
            case 0:
                return "disabling";
            case 1:
                return "disabled";
            case 2:
                return "enabling";
            case 3:
                return "enabled";
            case 4:
                return "unknown state";
            default:
                return "[invalid state]";
        }
    }

    public int syncGetWifiApState() {
        return this.mWifiApState.get();
    }

    public String syncGetWifiApStateByName() {
        switch (this.mWifiApState.get()) {
            case 10:
                return "disabling";
            case 11:
                return "disabled";
            case 12:
                return "enabling";
            case 13:
                return "enabled";
            case Extension.TYPE_ENUM /*14*/:
                return "failed";
            default:
                return "[invalid state]";
        }
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public boolean isDisconnected() {
        return getCurrentState() == this.mDisconnectedState;
    }

    public boolean isSupplicantTransientState() {
        SupplicantState supplicantState = this.mWifiInfo.getSupplicantState();
        if (SupplicantState.isHandshakeState(supplicantState)) {
            if (DBG) {
                Log.d(TAG, "Supplicant is under transient state: " + supplicantState);
            }
            return true;
        }
        if (DBG) {
            Log.d(TAG, "Supplicant is under steady state: " + supplicantState);
        }
        return false;
    }

    public boolean isLinkDebouncing() {
        return this.linkDebouncing;
    }

    public WifiInfo syncRequestConnectionInfo() {
        return getWiFiInfoForUid(Binder.getCallingUid());
    }

    public WifiInfo getWifiInfo() {
        return this.mWifiInfo;
    }

    public DhcpResults syncGetDhcpResults() {
        DhcpResults dhcpResults;
        synchronized (this.mDhcpResultsLock) {
            dhcpResults = new DhcpResults(this.mDhcpResults);
        }
        return dhcpResults;
    }

    public void setDriverStart(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_DRIVER);
        } else {
            sendMessage(CMD_STOP_DRIVER);
        }
    }

    public int getOperationalMode() {
        return this.mOperationalMode;
    }

    public void setOperationalMode(int mode) {
        if (DBG) {
            log("setting operational mode to " + String.valueOf(mode));
        }
        sendMessage(CMD_SET_OPERATIONAL_MODE, mode, 0);
    }

    protected int getOperationalModeForTest() {
        return this.mOperationalMode;
    }

    public List<ScanResult> syncGetScanResultsList() {
        List<ScanResult> scanList;
        synchronized (this.mScanResultsLock) {
            scanList = new ArrayList();
            if (this.enableSpoofScanResults) {
                spoofScanResults(scanList);
            } else {
                for (ScanDetail result : this.mScanResults) {
                    scanList.add(new ScanResult(result.getScanResult()));
                }
            }
        }
        return scanList;
    }

    public int syncAddPasspointManagementObject(AsyncChannel channel, String managementObject) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_PASSPOINT_MO, managementObject);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public int syncModifyPasspointManagementObject(AsyncChannel channel, String fqdn, List<PasspointManagementObjectDefinition> managementObjectDefinitions) {
        Bundle bundle = new Bundle();
        bundle.putString(PasspointManagementObjectManager.TAG_FQDN, fqdn);
        bundle.putParcelableList("MOS", managementObjectDefinitions);
        Message resultMsg = channel.sendMessageSynchronously(CMD_MODIFY_PASSPOINT_MO, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString("FILENAME", fileName);
        Message resultMsg = channel.sendMessageSynchronously(CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }

    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_MATCH_PROVIDER_NETWORK, fqdn);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
    }

    public void disableEphemeralNetwork(String SSID) {
        if (SSID != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, SSID);
        }
    }

    public void disconnectCommand() {
        if (hasCustomizedAutoConnect()) {
            this.mDisconnectOperation = true;
        }
        sendMessage(CMD_DISCONNECT);
        if (!hasCustomizedAutoConnect()) {
            sendMessage(CMD_RECONNECT);
        }
    }

    public void disconnectCommand(int uid, int reason) {
        if (hasCustomizedAutoConnect()) {
            this.mDisconnectOperation = true;
        }
        sendMessage(CMD_DISCONNECT, uid, reason);
        if (!hasCustomizedAutoConnect()) {
            sendMessage(CMD_RECONNECT);
        }
    }

    public void reconnectCommand() {
        sendMessage(CMD_RECONNECT);
    }

    public void reassociateCommand() {
        sendMessage(CMD_REASSOCIATE);
    }

    public void reloadTlsNetworksAndReconnect() {
        sendMessage(CMD_RELOAD_TLS_AND_RECONNECT);
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_NETWORK, config);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONFIGURED_NETWORKS, uuid);
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConfiguration syncGetMatchingWifiConfig(ScanResult scanResult, AsyncChannel channel) {
        return (WifiConfiguration) channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult).obj;
    }

    public WifiConnectionStatistics syncGetConnectionStatistics(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONNECTION_STATISTICS);
        WifiConnectionStatistics result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public int syncGetSupportedFeatures(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SUPPORTED_FEATURES);
        int supportedFeatureSet = resultMsg.arg1;
        resultMsg.recycle();
        return supportedFeatureSet;
    }

    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_LINK_LAYER_STATS);
        WifiLinkLayerStats result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_NETWORK, networkId);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ENABLE_NETWORK, netId, disableOthers ? 1 : 0);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        Message resultMsg = channel.sendMessageSynchronously(151569, netId);
        boolean result = resultMsg.arg1 != 151570;
        resultMsg.recycle();
        return result;
    }

    public String syncGetWpsNfcConfigurationToken(int netId) {
        return this.mWifiNative.getNfcWpsConfigurationToken(netId);
    }

    public void addToBlacklist(String bssid) {
        sendMessage(CMD_BLACKLIST_NETWORK, bssid);
    }

    public void clearBlacklist() {
        sendMessage(CMD_CLEAR_BLACKLIST);
    }

    public void enableRssiPolling(boolean enabled) {
        int i;
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_ENABLE_RSSI_POLL, i, 0);
    }

    public void enableAllNetworks() {
        sendMessage(CMD_ENABLE_ALL_NETWORKS);
    }

    public void startFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(true);
    }

    public void stopFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(false);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        int i;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_SET_HIGH_PERF_MODE, i, 0);
    }

    public synchronized void resetSimAuthNetworks(int simSlot, boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simSlot, simPresent ? 1 : 0);
    }

    public Network getCurrentNetwork() {
        if (this.mNetworkAgent != null) {
            return new Network(this.mNetworkAgent.netId);
        }
        return null;
    }

    public void setFrequencyBand(int band, boolean persist) {
        if (persist) {
            Global.putInt(this.mContext.getContentResolver(), "wifi_frequency_band", band);
        }
        sendMessage(CMD_SET_FREQUENCY_BAND, 0, 0);
    }

    public void enableTdls(String remoteMacAddress, boolean enable) {
        int enabler;
        if (enable) {
            enabler = 1;
        } else {
            enabler = 0;
        }
        sendMessage(CMD_ENABLE_TDLS, enabler, 0, remoteMacAddress);
    }

    public int getFrequencyBand() {
        return this.mFrequencyBand.get();
    }

    public String getConfigFile() {
        return this.mWifiConfigManager.getConfigFile();
    }

    public void sendBluetoothAdapterStateChange(int state) {
        sendMessage(CMD_BLUETOOTH_ADAPTER_STATE_CHANGE, state, 0);
    }

    public void removeAppConfigs(String packageName, int uid) {
        ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.uid = uid;
        sendMessage(CMD_REMOVE_APP_CONFIGURATIONS, ai);
    }

    public void removeUserConfigs(int userId) {
        sendMessage(CMD_REMOVE_USER_CONFIGURATIONS, userId);
    }

    public boolean syncSaveConfig(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_SAVE_CONFIG);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public void updateBatteryWorkSource(WorkSource newSource) {
        synchronized (this.mRunningWifiUids) {
            if (newSource != null) {
                try {
                    this.mRunningWifiUids.set(newSource);
                } catch (RemoteException e) {
                }
            }
            if (this.mIsRunning) {
                if (!this.mReportedRunning) {
                    this.mBatteryStats.noteWifiRunning(this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                    this.mReportedRunning = true;
                } else if (this.mLastRunningWifiUids.diff(this.mRunningWifiUids)) {
                    this.mBatteryStats.noteWifiRunningChanged(this.mLastRunningWifiUids, this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                }
            } else if (this.mReportedRunning) {
                this.mBatteryStats.noteWifiStopped(this.mLastRunningWifiUids);
                this.mLastRunningWifiUids.clear();
                this.mReportedRunning = false;
            }
            this.mWakeLock.setWorkSource(newSource);
        }
    }

    public void dumpIpManager(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mIpManager.dump(fd, pw, args);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        try {
            if (args.length > 1 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0]) && WifiMetrics.CLEAN_DUMP_ARG.equals(args[1])) {
                updateWifiMetrics();
                this.mWifiMetrics.dump(fd, pw, args);
                return;
            }
            super.dump(fd, pw, args);
            this.mSupplicantStateTracker.dump(fd, pw, args);
            pw.println("mLinkProperties " + this.mLinkProperties);
            pw.println("mWifiInfo " + this.mWifiInfo);
            pw.println("mDhcpResults " + this.mDhcpResults);
            pw.println("mNetworkInfo " + this.mNetworkInfo);
            pw.println("mLastSignalLevel " + this.mLastSignalLevel);
            pw.println("mLastBssid " + this.mLastBssid);
            pw.println("mLastNetworkId " + this.mLastNetworkId);
            pw.println("mOperationalMode " + this.mOperationalMode);
            pw.println("mUserWantsSuspendOpt " + this.mUserWantsSuspendOpt);
            pw.println("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
            pw.println("Supplicant status " + this.mWifiNative.status(true));
            if (this.mCountryCode.getCountryCodeSentToDriver() != null) {
                pw.println("CountryCode sent to driver " + this.mCountryCode.getCountryCodeSentToDriver());
            } else if (this.mCountryCode.getCountryCode() != null) {
                pw.println("CountryCode: " + this.mCountryCode.getCountryCode() + " was not sent to driver");
            } else {
                pw.println("CountryCode was not initialized");
            }
            pw.println("mConnectedModeGScanOffloadStarted " + this.mConnectedModeGScanOffloadStarted);
            pw.println("mGScanPeriodMilli " + this.mGScanPeriodMilli);
            if (this.mWhiteListedSsids != null && this.mWhiteListedSsids.length > 0) {
                pw.println("SSID whitelist :");
                for (String str : this.mWhiteListedSsids) {
                    pw.println("       " + str);
                }
            }
            if (this.mNetworkFactory != null) {
                this.mNetworkFactory.dump(fd, pw, args);
            } else {
                pw.println("mNetworkFactory is not initialized");
            }
            if (this.mUntrustedNetworkFactory != null) {
                this.mUntrustedNetworkFactory.dump(fd, pw, args);
            } else {
                pw.println("mUntrustedNetworkFactory is not initialized");
            }
            pw.println("Wlan Wake Reasons:" + this.mWifiNative.getWlanWakeReasonCount());
            pw.println();
            updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiConfigManager.dump(fd, pw, args);
            pw.println();
            this.mWifiLogger.captureBugReportData(7);
            this.mWifiLogger.dump(fd, pw, args);
            this.mWifiQualifiedNetworkSelector.dump(fd, pw, args);
            dumpIpManager(fd, pw, args);
            if (this.mWifiConnectivityManager != null) {
                this.mWifiConnectivityManager.dump(fd, pw, args);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    private void logStateAndMessage(Message message, State state) {
        this.messageHandlingStatus = 0;
        if (DBG) {
            switch (message.what) {
                case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                case CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                case CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    if (!MDBG) {
                        return;
                    }
                    break;
            }
            logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
        }
    }

    String printTime() {
        StringBuilder sb = new StringBuilder();
        sb.append(" rt=").append(SystemClock.uptimeMillis());
        sb.append("/").append(SystemClock.elapsedRealtime());
        return sb.toString();
    }

    protected String getLogRecString(Message msg) {
        if (!DBG) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (this.mScreenOn) {
            sb.append("!");
        }
        if (this.messageHandlingStatus != MESSAGE_HANDLING_STATUS_UNKNOWN) {
            sb.append("(").append(this.messageHandlingStatus).append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=").append(msg.sendingUid);
        }
        sb.append(" ").append(printTime());
        WifiConfiguration config;
        String key;
        Long now;
        Object[] objArr;
        String report;
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    config = (WifiConfiguration) msg.obj;
                    sb.append(" ").append(config.configKey());
                    sb.append(" prio=").append(config.priority);
                    sb.append(" status=").append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ").append(config.BSSID);
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=").append(curConfig.configKey());
                            sb.append(" prio=").append(curConfig.priority);
                            sb.append(" status=").append(curConfig.status);
                            break;
                        }
                        sb.append(" is current");
                        break;
                    }
                }
                break;
            case CMD_ENABLE_NETWORK /*131126*/:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                config = this.mWifiConfigManager.getWifiConfiguration(msg.arg1);
                if (config != null && (key == null || !config.configKey().equals(key))) {
                    sb.append(" target=").append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=").append(this.mWifiConfigManager.getConfiguredNetworksSize());
                break;
            case CMD_START_SCAN /*131143*/:
                now = Long.valueOf(System.currentTimeMillis());
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ic=");
                sb.append(Integer.toString(sScanAlarmIntentCount));
                if (msg.obj != null) {
                    Long request = Long.valueOf(msg.obj.getLong(SCAN_REQUEST_TIME, 0));
                    if (request.longValue() != 0) {
                        sb.append(" proc(ms):").append(now.longValue() - request.longValue());
                    }
                }
                if (this.mIsScanOngoing) {
                    sb.append(" onGoing");
                }
                if (this.mIsFullScanOngoing) {
                    sb.append(" full");
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txSuccessRate);
                sb.append(String.format(" tx=%.1f,", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txRetriesRate);
                sb.append(String.format(" %.1f,", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txBadRate);
                sb.append(String.format(" %.1f ", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.rxSuccessRate);
                sb.append(String.format(" rx=%.1f", objArr));
                if (this.lastScanFreqs != null) {
                    sb.append(" list=");
                    for (Integer intValue : this.lastScanFreqs) {
                        sb.append(intValue.intValue()).append(",");
                    }
                }
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                    break;
                }
                break;
            case CMD_RSSI_POLL /*131155*/:
            case CMD_UNWANTED_NETWORK /*131216*/:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ").append(this.mWifiInfo.getSSID());
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txSuccessRate);
                sb.append(String.format(" tx=%.1f,", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txRetriesRate);
                sb.append(String.format(" %.1f,", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.txBadRate);
                sb.append(String.format(" %.1f ", objArr));
                objArr = new Object[1];
                objArr[0] = Double.valueOf(this.mWifiInfo.rxSuccessRate);
                sb.append(String.format(" rx=%.1f", objArr));
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(this.mRunningBeaconCount);
                sb.append(String.format(" bcn=%d", objArr));
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                }
                if (this.mWifiScoreReport != null) {
                    sb.append(this.mWifiScoreReport.getReport());
                }
                if (!this.mConnectedModeGScanOffloadStarted) {
                    sb.append(" offload-stopped");
                    break;
                }
                sb.append(" offload-started periodMilli ").append(this.mGScanPeriodMilli);
                break;
            case CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.roamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.disconnectingWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /*131211*/:
                int count = -1;
                WifiConfiguration c = getCurrentWifiConfiguration();
                if (c != null) {
                    count = c.getNetworkSelectionStatus().getDisableReasonCounter(4);
                }
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" failures: ");
                sb.append(Integer.toString(count));
                sb.append("/");
                sb.append(Integer.toString(this.mWifiConfigManager.getMaxDhcpRetries()));
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(this.mRunningBeaconCount);
                sb.append(String.format(" bcn=%d", objArr));
                break;
            case CMD_UPDATE_LINKPROPERTIES /*131212*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case CMD_TARGET_BSSID /*131213*/:
            case CMD_ASSOCIATED_BSSID /*131219*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=").append((String) msg.obj);
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                break;
            case CMD_AUTO_CONNECT /*131215*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = this.mWifiConfigManager.getWifiConfiguration(msg.arg1);
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                        break;
                    }
                }
                break;
            case CMD_AUTO_ROAM /*131217*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = msg.obj;
                if (result != null) {
                    now = Long.valueOf(System.currentTimeMillis());
                    sb.append(" bssid=").append(result.BSSID);
                    sb.append(" rssi=").append(result.level);
                    sb.append(" freq=").append(result.frequency);
                    if (result.seen <= 0 || result.seen >= now.longValue()) {
                        sb.append(" !seen=").append(result.seen);
                    } else {
                        sb.append(" seen=").append(now.longValue() - result.seen);
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                sb.append(" fail count=").append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_AUTO_SAVE_NETWORK /*131218*/:
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.lastSavedConfigurationAttempt != null) {
                    sb.append(" ").append(this.lastSavedConfigurationAttempt.configKey());
                    sb.append(" nid=").append(this.lastSavedConfigurationAttempt.networkId);
                    if (this.lastSavedConfigurationAttempt.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (!(this.lastSavedConfigurationAttempt.preSharedKey == null || this.lastSavedConfigurationAttempt.preSharedKey.equals("*"))) {
                        sb.append(" hasPSK");
                    }
                    if (this.lastSavedConfigurationAttempt.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (this.lastSavedConfigurationAttempt.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(this.lastSavedConfigurationAttempt.creatorUid);
                    sb.append(" suid=").append(this.lastSavedConfigurationAttempt.lastUpdateUid);
                    break;
                }
                break;
            case CMD_IP_REACHABILITY_LOST /*131221*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                    break;
                }
                break;
            case CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" autojoinAllowed=");
                sb.append(this.mWifiConfigManager.getEnableAutoJoinWhenAssociated());
                sb.append(" withTraffic=").append(getAllowScansWithTraffic());
                sb.append(" tx=").append(this.mWifiInfo.txSuccessRate);
                sb.append("/").append(8);
                sb.append(" rx=").append(this.mWifiInfo.rxSuccessRate);
                sb.append("/").append(16);
                sb.append(" -> ").append(this.mConnectedModeGScanOffloadStarted);
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
            case CMD_RSSI_THRESHOLD_BREACH /*131236*/:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_USER_SWITCH /*131237*/:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                sb.append(" ");
                if (msg.arg1 != 1) {
                    if (msg.arg1 != CMD_STATIC_IP_SUCCESS) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    }
                    sb.append("STATIC_OK");
                    break;
                }
                sb.append("DHCP_OK");
                break;
            case CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                sb.append(" ");
                if (msg.arg1 != 2) {
                    if (msg.arg1 != CMD_STATIC_IP_FAILURE) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    }
                    sb.append("STATIC_FAIL");
                    break;
                }
                sb.append("DHCP_FAIL");
                break;
            case CMD_INSTALL_PACKET_FILTER /*131274*/:
                sb.append(" len=").append(((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                sb.append(" enabled=").append(((Boolean) msg.obj).booleanValue());
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    NetworkInfo info = msg.obj;
                    NetworkInfo.State state = info.getState();
                    DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=").append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/").append(detailedState);
                        break;
                    }
                }
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ").append(this.mLastBssid);
                sb.append(" nid=").append(this.mLastNetworkId);
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                }
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                sb.append(" nid=").append(msg.arg1);
                sb.append(" reason=").append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=").append(this.mLastBssid);
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=").append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                }
                if (this.linkDebouncing) {
                    sb.append(" debounce");
                    break;
                }
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mScanResults != null) {
                    sb.append(" found=");
                    sb.append(this.mScanResults.size());
                }
                sb.append(" known=").append(this.mNumScanResultsKnown);
                sb.append(" got=").append(this.mNumScanResultsReturned);
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(this.mRunningBeaconCount);
                sb.append(String.format(" bcn=%d", objArr));
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(this.mConnectionReqCount);
                sb.append(String.format(" con=%d", objArr));
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(this.mUntrustedReqCount);
                sb.append(String.format(" untrustedcn=%d", objArr));
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                    break;
                }
                break;
            case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
            case WifiMonitor.SSID_REENABLED /*147470*/:
                sb.append(" nid=").append(msg.arg1);
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    NetworkSelectionStatus netWorkSelectionStatus = config.getNetworkSelectionStatus();
                    sb.append(" cur=").append(config.configKey());
                    sb.append(" ajst=").append(netWorkSelectionStatus.getNetworkStatusString());
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    if (config.status != 0) {
                        sb.append(" st=").append(config.status);
                        sb.append(" rs=").append(netWorkSelectionStatus.getNetworkDisableReasonString());
                    }
                    if (config.lastConnected != 0) {
                        sb.append(" lastconn=").append(Long.valueOf(System.currentTimeMillis()).longValue() - config.lastConnected).append("(ms)");
                    }
                    if (this.mLastBssid != null) {
                        sb.append(" lastbssid=").append(this.mLastBssid);
                    }
                    if (this.mWifiInfo.getFrequency() != -1) {
                        sb.append(" freq=").append(this.mWifiInfo.getFrequency());
                        sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                        sb.append(" bssid=").append(this.mWifiInfo.getBSSID());
                        break;
                    }
                }
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(bssid);
                }
                sb.append(" blacklist=").append(Boolean.toString(this.didBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.lastForgetConfigurationAttempt != null) {
                    sb.append(" ").append(this.lastForgetConfigurationAttempt.configKey());
                    sb.append(" nid=").append(this.lastForgetConfigurationAttempt.networkId);
                    if (this.lastForgetConfigurationAttempt.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (this.lastForgetConfigurationAttempt.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (this.lastForgetConfigurationAttempt.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (this.lastForgetConfigurationAttempt.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(this.lastForgetConfigurationAttempt.creatorUid);
                    sb.append(" suid=").append(this.lastForgetConfigurationAttempt.lastUpdateUid);
                    sb.append(" ajst=").append(this.lastForgetConfigurationAttempt.getNetworkSelectionStatus().getNetworkStatusString());
                    break;
                }
                break;
            case 196611:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=").append(this.mWifiInfo.txSuccess);
                sb.append(",").append(this.mWifiInfo.txBad);
                sb.append(",").append(this.mWifiInfo.txRetries);
                break;
            case 196612:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.arg1 == 1) {
                    sb.append(" OK ");
                } else if (msg.arg1 == 2) {
                    sb.append(" FAIL ");
                }
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            default:
                sb.append(msg.toString());
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }
        return sb.toString();
    }

    private void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (DBG) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        if (screenOn) {
            if (DBG) {
                log("force to scan when screen is on---");
            }
            startScan(-1, -1, null, null);
        }
        if (screenOn && this.closedByRestartInSilence && getCurrentState() == this.mInitialState) {
            log("fool-proof,start wifi when screen is on if closedByRestartInSilence");
            setSupplicantRunning(true);
            this.closedByRestartInSilence = false;
            this.mAlarmManager.cancel(this.mOpenWifiIntent);
        }
        enableRssiPolling(screenOn);
        if (this.mUserWantsSuspendOpt.get()) {
            int shouldReleaseWakeLock = 0;
            if (screenOn) {
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 0, 0);
            } else {
                if (isConnected()) {
                    this.mSuspendWakeLock.acquire(2000);
                    shouldReleaseWakeLock = 1;
                }
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 1, shouldReleaseWakeLock);
            }
        }
        if (screenOn) {
            this.mScreenOnTime = System.currentTimeMillis();
        } else {
            this.mScreenOffTime = System.currentTimeMillis();
            if (this.mScreenOffTime - this.mScreenOnTime > SCAN_PERMISSION_UPDATE_THROTTLE_MILLI) {
                this.mIdleScanTimes = 0;
                this.mAppScanTimes = 0;
                this.mIdleRenewTimes = 0;
                this.mIdleGroupTimes = 0;
                this.mIdleDisConnTimes = 0;
                if (this.mUidList != null) {
                    synchronized (this.mUidList) {
                        this.mUidList.clear();
                    }
                }
            }
        }
        this.mScreenBroadcastReceived.set(true);
        if (hasCustomizedAutoConnect()) {
            sendMessage(M_CMD_UPDATE_SCAN_INTERVAL);
        }
        getWifiLinkLayerStats(false);
        this.mOnTimeScreenStateChange = this.mOnTime;
        this.lastScreenStateChangeTimeStamp = this.lastLinkLayerStatsUpdate;
        this.mWifiMetrics.setScreenState(screenOn);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        }
        if (DBG) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private void setFrequencyBand() {
        if (this.mWifiNative.setBand(0)) {
            this.mFrequencyBand.set(0);
            if (this.mWifiConnectivityManager != null) {
                this.mWifiConnectivityManager.setUserPreferredBand(0);
            }
            if (DBG) {
                logd("done set frequency band " + 0);
                return;
            }
            return;
        }
        loge("Failed to set frequency band " + 0);
    }

    private void setSuspendOptimizationsNative(int reason, boolean enabled) {
        if (DBG) {
            log("setSuspendOptimizationsNative: " + reason + " " + enabled + " -want " + this.mUserWantsSuspendOpt.get() + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
            if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
                if (DBG) {
                    log("setSuspendOptimizationsNative do it " + reason + " " + enabled + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
                }
                this.mWifiNative.setSuspendOptimizations(true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(false);
    }

    private void setSuspendOptimizations(int reason, boolean enabled) {
        if (DBG) {
            log("setSuspendOptimizations: " + reason + " " + enabled);
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
        } else {
            this.mSuspendOptNeedsDisabled |= reason;
        }
        if (DBG) {
            log("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        }
    }

    private void setWifiState(int wifiState) {
        int previousWifiState = this.mWifiState.get();
        if (previousWifiState == wifiState) {
            if (DBG) {
                log("Don't set same state " + wifiState);
            }
            return;
        }
        if (wifiState == 3) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiState == 1) {
            this.mBatteryStats.noteWifiOff();
        }
        this.mWifiState.set(wifiState);
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.updateWifiState(wifiState);
        }
        if (wifiState == 1) {
            setManuConnect(false);
        }
        this.mSupplicantStateTracker.setWifiState(wifiState);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiStateChangedEvt(wifiState);
        }
        if (DBG) {
            log("setWifiState: " + syncGetWifiStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiState);
        intent.putExtra("previous_wifi_state", previousWifiState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setWifiApState(int wifiApState, int reason) {
        int previousWifiApState = this.mWifiApState.get();
        if (wifiApState == 13) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiApState == 11) {
            this.mBatteryStats.noteWifiOff();
        }
        this.mWifiApState.set(wifiApState);
        if (DBG) {
            log("setWifiApState: " + syncGetWifiApStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        if (wifiApState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* JADX WARNING: Missing block: B:47:0x00e6, code:
            if (r8.getNetworkDetail().getANQPElements() == null) goto L_0x009e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setScanResults() {
        this.mNumScanResultsKnown = 0;
        this.mNumScanResultsReturned = 0;
        ArrayList<ScanDetail> scanResults = this.mWifiNative.getScanResults();
        if (scanResults.isEmpty()) {
            this.mScanResults = new ArrayList();
            return;
        }
        this.mWifiConfigManager.trimANQPCache(false);
        boolean connected = this.mLastBssid != null;
        long activeBssid = 0;
        if (connected) {
            try {
                activeBssid = Utils.parseMac(this.mLastBssid);
            } catch (IllegalArgumentException e) {
                connected = false;
            }
        }
        synchronized (this.mScanResultsLock) {
            ScanDetail activeScanDetail = null;
            this.mScanResults = scanResults;
            this.mNumScanResultsReturned = this.mScanResults.size();
            for (ScanDetail resultDetail : this.mScanResults) {
                if (connected && resultDetail.getNetworkDetail().getBSSID() == activeBssid) {
                    if (activeScanDetail != null && activeScanDetail.getNetworkDetail().getBSSID() == activeBssid) {
                    }
                    activeScanDetail = resultDetail;
                }
                NetworkDetail networkDetail = resultDetail.getNetworkDetail();
                if (networkDetail != null && networkDetail.getDtimInterval() > 0) {
                    List<WifiConfiguration> associatedWifiConfigurations = this.mWifiConfigManager.getSavedNetworkFromScanDetail(resultDetail);
                    if (associatedWifiConfigurations != null) {
                        for (WifiConfiguration associatedConf : associatedWifiConfigurations) {
                            if (associatedConf != null) {
                                associatedConf.dtimInterval = networkDetail.getDtimInterval();
                            }
                        }
                    }
                }
            }
            if (OppoAutoConnectManager.getInstance() != null) {
                OppoAutoConnectManager.getInstance().sendScanResultEvt();
            }
            this.mLastScanTime = System.currentTimeMillis();
            if (this.mWifiNetworkAvailable != null) {
                this.mWifiNetworkAvailable.detectScanResult(this.mLastScanTime);
            } else if (this.mWifiNetworkStateTraker != null) {
                this.mWifiNetworkStateTraker.detectScanResult(this.mLastScanTime);
            }
            if (!this.mScreenOn) {
                this.mIdleScanTimes++;
            }
            this.mWifiConfigManager.setActiveScanDetail(activeScanDetail);
        }
        if (this.linkDebouncing) {
            sendMessage(CMD_AUTO_ROAM, this.mLastNetworkId, 1, null);
        }
    }

    private int getFilterRssi(int rssi) {
        int sumRssi = 0;
        int tempCount = 0;
        for (int i = 0; i < 5; i++) {
            if (WIFI_DBG) {
                log("mRssiArray[" + i + "] = " + this.mRssiArray[i]);
            }
            if (this.mRssiArray[i] < 0) {
                sumRssi += this.mRssiArray[i];
                tempCount++;
            }
        }
        int[] iArr = this.mRssiArray;
        int i2 = this.mIndex;
        this.mIndex = i2 + 1;
        iArr[i2] = rssi;
        this.mIndex %= 5;
        int result = (sumRssi + rssi) / (tempCount + 1);
        if (result > rssi) {
            return result;
        }
        return rssi;
    }

    private void fetchRssiLinkSpeedAndFrequencyNative() {
        Integer newRssi = null;
        Integer newLinkSpeed = null;
        Integer newFrequency = null;
        String signalPoll = this.mWifiNative.signalPoll();
        if (signalPoll != null) {
            for (String line : signalPoll.split("\n")) {
                String[] prop = line.split("=");
                if (prop.length >= 2) {
                    try {
                        if (prop[0].equals("RSSI")) {
                            newRssi = Integer.valueOf(Integer.parseInt(prop[1]));
                        } else if (prop[0].equals("LINKSPEED")) {
                            newLinkSpeed = Integer.valueOf(Integer.parseInt(prop[1]));
                        } else if (prop[0].equals("FREQUENCY")) {
                            newFrequency = Integer.valueOf(Integer.parseInt(prop[1]));
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        if (DBG) {
            logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " linkspeed=" + newLinkSpeed + " freq=" + newFrequency + ", mEnableRssiSmoothing = " + this.mEnableRssiSmoothing + ", mAffectRoaming = " + this.mAffectRoaming + ", mAnimator.isRunning() = " + this.mAnimator.isRunning() + ", mWifiInfo.mSmoothRssi = " + this.mWifiInfo.mSmoothRssi + ", mWifiInfo.getRssi() = " + this.mWifiInfo.getRssi());
        }
        if (newRssi == null || newRssi.intValue() <= -127 || newRssi.intValue() >= 200) {
            this.mWifiInfo.setRssi(-127);
            updateCapabilities(getCurrentWifiConfiguration());
        } else {
            int newSignalLevel;
            if (newRssi.intValue() > 0) {
                newRssi = Integer.valueOf(newRssi.intValue() - 256);
            }
            newRssi = Integer.valueOf(getFilterRssi(newRssi.intValue()));
            if (this.mEnableRssiSmoothing) {
                if (!(!this.mEnableRssiSmoothing || this.mAffectRoaming || this.mAnimator.isRunning())) {
                    this.mWifiInfo.mSmoothRssi = newRssi.intValue();
                }
                if (this.mAnimator.isRunning()) {
                    Log.d(TAG, "[RSSI smoothing] animation is running, newRssi = " + newRssi + ", mAffectRoaming = " + this.mAffectRoaming + ", mWifiInfo.getRssi() = " + this.mWifiInfo.getRssi(this.mAffectRoaming) + ", mTagetRssi = " + this.mTagetRssi);
                    if (newRssi.intValue() > this.mWifiInfo.getRssi(this.mAffectRoaming) || (newRssi.intValue() < this.mWifiInfo.getRssi(this.mAffectRoaming) && newRssi.intValue() < this.mTagetRssi && newRssi.intValue() - this.mTagetRssi < this.mRssiSmoothingThreshold)) {
                        Log.d(TAG, "[RSSI smoothing] cancel animator#1");
                        this.mAnimator.cancel();
                        if (!this.mAffectRoaming) {
                            this.mWifiInfo.mSmoothRssi = newRssi.intValue();
                        }
                    } else {
                        if (newRssi.intValue() == this.mTagetRssi) {
                            this.mWifiMetrics.incrementRssiPollRssiCount(this.mWifiInfo.getRssi(this.mAffectRoaming));
                            return;
                        }
                        Log.d(TAG, "[RSSI smoothing] cancel animator#2");
                        this.mAnimator.cancel();
                        createAnimation(newRssi.intValue());
                        newRssi = Integer.valueOf(this.mWifiInfo.getRssi(this.mAffectRoaming));
                        if (!this.mAffectRoaming) {
                            this.mWifiInfo.mSmoothRssi = newRssi.intValue();
                        }
                    }
                } else {
                    if (this.mWifiInfo.getRssi() - newRssi.intValue() >= this.mRssiSmoothingThreshold) {
                        createAnimation(newRssi.intValue());
                        newRssi = Integer.valueOf(this.mWifiInfo.getRssi(this.mAffectRoaming));
                    }
                }
            }
            this.mWifiInfo.setRssi(newRssi.intValue());
            this.mWifiMetrics.incrementRssiPollRssiCount(newRssi.intValue());
            if (this.mAffectRoaming) {
                newSignalLevel = WifiManager.calculateSignalLevel(newRssi.intValue(), 5);
            } else {
                newSignalLevel = WifiManager.calculateSignalLevel(this.mWifiInfo.mSmoothRssi, 5);
            }
            if (newSignalLevel != this.mLastSignalLevel) {
                updateCapabilities(getCurrentWifiConfiguration());
                if ((newSignalLevel - this.mLastSignalLevel > 1 || this.mLastSignalLevel - newSignalLevel > 1) && this.mRssiCount < 3) {
                    this.mRssiCount++;
                    log("Rssi change too fast: " + this.mLastSignalLevel + " to " + newSignalLevel + "; Count = " + this.mRssiCount);
                    newSignalLevel = (this.mLastSignalLevel + newSignalLevel) / 2;
                    log("Rssi change too fast: so average level to " + newSignalLevel);
                    int sumRssi = 0;
                    int tempCount = 0;
                    for (int i = 0; i < 5; i++) {
                        if (this.mRssiArray[i] < 0) {
                            sumRssi += this.mRssiArray[i];
                            tempCount++;
                        }
                    }
                    if (tempCount > 0) {
                        sendRssiChangeBroadcast(sumRssi / tempCount);
                    }
                } else {
                    this.mRssiCount = 0;
                    sendRssiChangeBroadcast(newRssi.intValue());
                }
            }
            Log.d(TAG, "mLastSignalLevel:" + this.mLastSignalLevel + ", newSignalLevel:" + newSignalLevel);
            this.mLastSignalLevel = newSignalLevel;
        }
        if (hasCustomizedAutoConnect() && newRssi != null && newRssi.intValue() < -85) {
            int ipAddr = this.mWifiInfo.getIpAddress();
            long time = SystemClock.elapsedRealtime();
            boolean autoConnect = this.mWifiFwkExt.shouldAutoConnect();
            Log.d(TAG, "fetchRssi, ip:" + ipAddr + ", mDisconnectOperation:" + this.mDisconnectOperation + ", time:" + time + ", lasttime:" + this.mLastCheckWeakSignalTime);
            if (!(ipAddr == 0 || this.mDisconnectOperation || (time - this.mLastCheckWeakSignalTime <= PasspointManagementObjectManager.IntervalFactor && !autoConnect))) {
                Log.d(TAG, "Rssi < -85, scan for checking signal!");
                if (!autoConnect) {
                    this.mLastCheckWeakSignalTime = time;
                }
                this.mDisconnectNetworkId = this.mLastNetworkId;
                this.mScanForWeakSignal = true;
                this.mWifiNative.bssFlush();
                startScan(-1, 0, null, null);
            }
        }
        if (newLinkSpeed != null) {
            this.mWifiInfo.setLinkSpeed(newLinkSpeed.intValue());
        }
        if (newFrequency != null && newFrequency.intValue() > 0) {
            WifiConnectionStatistics wifiConnectionStatistics;
            if (ScanResult.is5GHz(newFrequency.intValue())) {
                wifiConnectionStatistics = this.mWifiConnectionStatistics;
                wifiConnectionStatistics.num5GhzConnected++;
            }
            if (ScanResult.is24GHz(newFrequency.intValue())) {
                wifiConnectionStatistics = this.mWifiConnectionStatistics;
                wifiConnectionStatistics.num24GhzConnected++;
            }
            int oldFreq = this.mWifiInfo.getFrequency();
            this.mWifiInfo.setFrequency(newFrequency.intValue());
            if (oldFreq != newFrequency.intValue()) {
                Log.d(TAG, "Old frequency: " + oldFreq + " & new frequency: " + newFrequency + " is not equal, send broadcast");
                sendNetworkStateChangeBroadcast(this.mLastBssid);
            }
        }
        this.mWifiConfigManager.updateConfiguration(this.mWifiInfo);
    }

    private void createAnimation(int newRssi) {
        Log.d(TAG, "createAnimation, mWifiInfo.getRssi(mAffectRoaming) = " + this.mWifiInfo.getRssi(this.mAffectRoaming) + ", newRssi = " + newRssi + ", mAffectRoaming = " + this.mAffectRoaming);
        int[] iArr = new int[2];
        iArr[0] = this.mWifiInfo.getRssi();
        iArr[1] = newRssi;
        this.mAnimator = ValueAnimator.ofInt(iArr);
        this.mAnimator.setDuration(SMART_SCAN_INTERVAL);
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (System.currentTimeMillis() >= WifiStateMachine.this.mNextAnimate) {
                    int curRssi = ((Integer) animation.getAnimatedValue()).intValue();
                    WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_SET_RSSI, curRssi);
                    WifiStateMachine.this.mNextAnimate = System.currentTimeMillis() + 1000;
                    Log.d(WifiStateMachine.TAG, "onAnimationUpdate, curRssi = " + curRssi + ", mNextAnimate = " + WifiStateMachine.this.mNextAnimate);
                    WifiStateMachine.this.mCurSmoothRssi = curRssi;
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Log.d(WifiStateMachine.TAG, "onAnimationEnd");
                WifiStateMachine.this.mNextAnimate = 0;
                WifiStateMachine.this.mCurSmoothRssi = -127;
                WifiStateMachine.this.mTagetRssi = -127;
                ((ValueAnimator) animation).removeAllUpdateListeners();
                animation.removeAllListeners();
            }

            public void onAnimationCancel(Animator animation) {
                Log.d(WifiStateMachine.TAG, "onAnimationCancel");
                WifiStateMachine.this.mNextAnimate = 0;
                WifiStateMachine.this.mCurSmoothRssi = -127;
                WifiStateMachine.this.mTagetRssi = -127;
                ((ValueAnimator) animation).removeAllUpdateListeners();
                animation.removeAllListeners();
            }
        });
        this.mAnimator.start();
        this.mNextAnimate = System.currentTimeMillis() + 1000;
        this.mTagetRssi = newRssi;
        this.mCurSmoothRssi = this.mWifiInfo.getRssi();
    }

    private void cleanWifiScore() {
        this.mWifiInfo.txBadRate = 0.0d;
        this.mWifiInfo.txSuccessRate = 0.0d;
        this.mWifiInfo.txRetriesRate = 0.0d;
        this.mWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport = null;
    }

    public double getTxPacketRate() {
        return this.mWifiInfo.txSuccessRate;
    }

    public double getRxPacketRate() {
        return this.mWifiInfo.rxSuccessRate;
    }

    private void fetchPktcntNative(RssiPacketCountInfo info) {
        String pktcntPoll = this.mWifiNative.pktcntPoll();
        if (pktcntPoll != null) {
            for (String line : pktcntPoll.split("\n")) {
                String[] prop = line.split("=");
                if (prop.length >= 2) {
                    try {
                        if (prop[0].equals("TXGOOD")) {
                            info.txgood = Integer.parseInt(prop[1]);
                        } else if (prop[0].equals("TXBAD")) {
                            info.txbad = Integer.parseInt(prop[1]);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
    }

    private void updateLinkProperties(LinkProperties newLp) {
        if (DBG) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        if (this.mLinkProperties != null) {
            this.mLinkProperties.setMtu(getRomUpdateIntegerValue("NETWORK_MTU", Integer.valueOf(1500)).intValue());
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: ").append(this.mLastNetworkId);
            sb.append(" state: ").append(getNetworkDetailedState());
            if (this.mLinkProperties != null) {
                sb.append(" ");
                sb.append(getLinkPropertiesSummary(this.mLinkProperties));
            }
            logd(sb.toString());
        }
    }

    private void clearLinkProperties() {
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        this.mLinkProperties.clear();
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private String updateDefaultRouteMacAddress(int timeout) {
        String address = null;
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route.isDefaultRoute() && route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                if (gateway instanceof Inet4Address) {
                    if (DBG) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :" + gateway.getHostAddress());
                    }
                    address = macAddressFromRoute(gateway.getHostAddress());
                    if (address == null && timeout > 0) {
                        try {
                            if (gateway.isReachable(timeout)) {
                                address = macAddressFromRoute(gateway.getHostAddress());
                                if (DBG) {
                                    logd("updateDefaultRouteMacAddress reachable (tried again) :" + gateway.getHostAddress() + " found " + address);
                                }
                            }
                        } catch (Exception e) {
                            loge("updateDefaultRouteMacAddress exception reaching :" + gateway.getHostAddress());
                        }
                    }
                    if (address != null) {
                        this.mWifiConfigManager.setDefaultGwMacAddress(this.mLastNetworkId, address);
                    }
                } else {
                    continue;
                }
            }
        }
        return address;
    }

    void sendScanResultsAvailableBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        intent.putExtra("SHOW_RESELECT_DIALOG", this.mShowReselectDialog);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (isSingtel() && !this.mRetry && !this.isSingtelConnecting && !getManuConnect()) {
            checkForExpApauto();
        }
    }

    private void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendNetworkStateChangeBroadcast(String bssid) {
        if (this.mWifiNetworkStateTraker == null || this.mWifiNetworkStateTraker.getManualConnect() || !this.autoSwitch || this.mNetworkInfo.getDetailedState() == DetailedState.IDLE || this.mNetworkInfo.getDetailedState() == DetailedState.SCANNING || this.mNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED || this.mNetworkInfo.getDetailedState() == DetailedState.FAILED || this.mNetworkInfo.getDetailedState() == DetailedState.BLOCKED || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            sendNetworkStateChangeBroadcast(bssid, true);
        } else {
            log("state is " + this.mNetworkInfo.getDetailedState() + ",not bc.");
        }
    }

    private void sendNetworkStateChangeBroadcast(String bssid, boolean triggerFetchRssi) {
        Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
        boolean wlanAssistAutoConnect = false;
        boolean replacePending = false;
        if (!(this.mWifiNetworkStateTraker == null || this.mWifiNetworkStateTraker.getManualConnect() || !this.autoSwitch)) {
            wlanAssistAutoConnect = true;
        }
        if (!wlanAssistAutoConnect) {
            replacePending = true;
        } else if (this.mLastDetailedState == this.mNetworkInfo.getDetailedState()) {
            replacePending = true;
        }
        if (replacePending) {
            intent.addFlags(536870912);
        }
        this.mLastDetailedState = this.mNetworkInfo.getDetailedState();
        intent.addFlags(67108864);
        intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        if (bssid != null) {
            intent.putExtra("bssid", bssid);
        }
        if (this.mNetworkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK || this.mNetworkInfo.getDetailedState() == DetailedState.CAPTIVE_PORTAL_CHECK || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            if (triggerFetchRssi) {
                fetchRssiLinkSpeedAndFrequencyNative();
            }
            WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
            sentWifiInfo.setMacAddress("02:00:00:00:00:00");
            intent.putExtra("wifiInfo", sentWifiInfo);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendNetworkStateChangedEvt(intent);
        }
    }

    private WifiInfo getWiFiInfoForUid(int uid) {
        WifiInfo result;
        if (Binder.getCallingUid() != Process.myUid()) {
            result = new WifiInfo(this.mWifiInfo);
            result.setMacAddress("02:00:00:00:00:00");
            if (this.mEnableRssiSmoothing && !this.mAffectRoaming) {
                result.setRssi(this.mWifiInfo.mSmoothRssi);
            }
            try {
                if (IPackageManager.Stub.asInterface(this.mFacade.getService("package")).checkUidPermission("android.permission.LOCAL_MAC_ADDRESS", uid) == 0) {
                    result.setMacAddress(this.mWifiInfo.getMacAddress());
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error checking receiver permission", e);
            }
            if ((!this.mEnableRssiPolling || result.getRssi() == -127) && result.getBSSID() != null) {
                for (ScanDetail scanResult : this.mScanResults) {
                    if (result.getBSSID().equals(scanResult.getScanResult().BSSID)) {
                        int level = scanResult.getScanResult().level;
                        Log.d(TAG, "Adjust rssi from " + result.getRssi() + " to " + level);
                        result.setRssi(level);
                        break;
                    }
                }
            }
            return result;
        } else if (!this.mEnableRssiSmoothing) {
            return this.mWifiInfo;
        } else {
            if (this.mAffectRoaming) {
                return this.mWifiInfo;
            }
            result = new WifiInfo(this.mWifiInfo);
            result.setRssi(this.mWifiInfo.mSmoothRssi);
            return result;
        }
    }

    private void sendLinkConfigurationChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendSupplicantConnectionChangedBroadcast(boolean connected) {
        Intent intent = new Intent("android.net.wifi.supplicant.CONNECTION_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("connected", connected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean setNetworkDetailedState(DetailedState state) {
        boolean hidden = false;
        if (this.linkDebouncing || isRoaming()) {
            hidden = true;
        }
        if (DBG) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (!(this.mNetworkInfo.getExtraInfo() == null || this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID().equals("<unknown ssid>") || this.mNetworkInfo.getExtraInfo().equals(this.mWifiInfo.getSSID()))) {
            if (DBG) {
                log("setDetailed state send new extra info" + this.mWifiInfo.getSSID());
            }
            this.mNetworkInfo.setExtraInfo(this.mWifiInfo.getSSID());
            sendNetworkStateChangeBroadcast(null);
        }
        if (hidden || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, this.mWifiInfo.getSSID());
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    public void setManuConnect(boolean manu) {
        if (DBG) {
            Log.d(TAG, "setManuConnect=" + manu);
        }
        mIsManuConnect = manu;
    }

    public boolean getManuConnect() {
        if (DBG) {
            Log.d(TAG, "getManuConnect=" + mIsManuConnect);
        }
        return mIsManuConnect;
    }

    public void setManuConnectNetId(int id) {
        if (DBG) {
            Log.d(TAG, "setManuConnectNetId=" + id);
        }
        mManuConnectNetId = id;
    }

    public int getManuConnectNetId() {
        if (DBG) {
            Log.d(TAG, "getManuConnectNetId=" + mManuConnectNetId);
        }
        return mManuConnectNetId;
    }

    public void setManuConnectConfiguration(WifiConfiguration config) {
        if (DBG) {
            Log.d(TAG, "setManuConnectConfiguration");
        }
        if (config != null && DBG) {
            Log.d(TAG, "netId=" + config.networkId + " ssid=" + config.SSID + " bssid=" + config.BSSID);
        }
        mManuConnectConfiguration = config;
    }

    public WifiConfiguration getManuConnectConfiguration() {
        if (mManuConnectConfiguration != null) {
            if (DBG) {
                Log.d(TAG, "getManuConnectConfiguration netId=" + mManuConnectConfiguration.networkId + " ssid=" + mManuConnectConfiguration.SSID + " bssid=" + mManuConnectConfiguration.BSSID);
            }
        } else if (DBG) {
            Log.d(TAG, "getManuConnectConfiguration = null");
        }
        return mManuConnectConfiguration;
    }

    public WifiConfiguration getLastManuConnectConfiguration() {
        return getManuConnectConfiguration();
    }

    boolean isOtherNetworksEnabledDuringManuConnect() {
        boolean val = false;
        if (this.mWifiConfigManager != null) {
            for (WifiConfiguration wc : this.mWifiConfigManager.getSavedNetworks()) {
                if (wc.status != 1 && wc.networkId != getManuConnectNetId()) {
                    val = true;
                    break;
                }
            }
        } else {
            val = false;
        }
        if (DBG) {
            Log.d(TAG, "isOtherNetworksEnabledDuringManuConnect=" + val);
        }
        return val;
    }

    public void handleSSIDStateChangedCB(int netId, int reason) {
        if (DBG) {
            Log.d(TAG, "handleSSIDStateChangedCB netId:" + netId + " reason:" + reason);
        }
        this.mSupplicantStateTracker.handleSSIDStateChangedCB(netId, reason);
    }

    private SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = message.obj;
        if (stateChangeResult == null) {
            if (DBG) {
                Log.d(TAG, "invalid supplicant state change message!");
            }
            return SupplicantState.INVALID;
        }
        SupplicantState state = stateChangeResult.state;
        if (this.mLastSupplicantState != state) {
            if (this.mLastSupplicantState == SupplicantState.COMPLETED) {
                removeMessages(CMD_TRIGGER_RESTORE_DELAY);
            }
            this.mLastSupplicantState = state;
        }
        if (this.mPowerState == SupplicantState.COMPLETED && state == SupplicantState.GROUP_HANDSHAKE && !this.mScreenOn) {
            this.mIdleGroupTimes++;
        }
        this.mPowerState = state;
        this.mWifiInfo.setSupplicantState(state);
        if ((stateChangeResult.wifiSsid == null || stateChangeResult.wifiSsid.toString().isEmpty()) && this.linkDebouncing) {
            return state;
        }
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
        } else {
            this.mWifiInfo.setNetworkId(-1);
        }
        this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
        if (!(this.mWhiteListedSsids == null || this.mWhiteListedSsids.length <= 0 || stateChangeResult.wifiSsid == null)) {
            String SSID = stateChangeResult.wifiSsid.toString();
            String currentSSID = this.mWifiInfo.getSSID();
            if (!(SSID == null || currentSSID == null || SSID.equals("<unknown ssid>"))) {
                if (SSID.length() >= 2 && SSID.charAt(0) == '\"' && SSID.charAt(SSID.length() - 1) == '\"') {
                    SSID = SSID.substring(1, SSID.length() - 1);
                }
                if (currentSSID.length() >= 2 && currentSSID.charAt(0) == '\"' && currentSSID.charAt(currentSSID.length() - 1) == '\"') {
                    currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
                }
                if (!SSID.equals(currentSSID) && getCurrentState() == this.mConnectedState) {
                    this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                    this.targetWificonfiguration = this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId());
                    transitionTo(this.mRoamingState);
                }
            }
        }
        this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        this.mWifiInfo.setEphemeral(this.mWifiConfigManager.isEphemeral(this.mWifiInfo.getNetworkId()));
        if (!this.mWifiInfo.getMeteredHint()) {
            this.mWifiInfo.setMeteredHint(this.mWifiConfigManager.getMeteredHint(this.mWifiInfo.getNetworkId()));
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.sendMessage(message.what, message.arg1, message.arg2, message.obj);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiSupplicantConnectStateChangedEvt(Message.obtain(message));
        }
        if (1 == syncGetWifiState() && SupplicantState.isConnecting(state)) {
            if (DBG) {
                Log.d(TAG, "wrong supplicant action, disconnect supplicant!!");
            }
            if (this.mWifiNative != null) {
                this.mWifiNative.disconnect();
            }
        }
        return state;
    }

    private void handleNetworkDisconnect() {
        if (DBG) {
            log("Stopping DHCP and clearing IP");
        }
        if (bManualConnect && wConnected) {
            if (DBG) {
                Log.d(TAG, "handleNetworkDisconnect set wConnected false ");
            }
            wConnected = false;
            if (OppoAutoConnectManager.getInstance() != null) {
                OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
            }
        }
        if ((nNetId == this.mLastNetworkId && this.mConnectedId == this.mLastNetworkId && !bManualConnect && !wStartWps) || wConnected) {
            if (this.mWifiNetworkAvailable != null) {
                this.mWifiNetworkAvailable.setManualConnect(false);
            } else if (this.mWifiNetworkStateTraker != null) {
                this.mWifiNetworkStateTraker.setManualConnect(false, false, 1000);
            }
            if (wConnected) {
                wConnected = false;
                if (OppoAutoConnectManager.getInstance() != null) {
                    OppoAutoConnectManager.getInstance().handleWpsConnect(false, true);
                }
            }
        }
        if (!(this.mWifiNetworkStateTraker == null || this.mNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED)) {
            this.mWifiNetworkStateTraker.setNetworkDetailState(this.mLastNetworkId, DetailedState.DISCONNECTED, this.mLastBssid);
        }
        this.mNetworkDetectValid = false;
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(false, this.mLastNetworkId);
        }
        if (hasCustomizedAutoConnect()) {
            DetailedState state = getNetworkDetailedState();
            Log.d(TAG, "handleNetworkDisconnect, state:" + state + ", mDisconnectOperation:" + this.mDisconnectOperation);
            if (state == DetailedState.CONNECTED) {
                this.mDisconnectNetworkId = this.mLastNetworkId;
                if (!this.mDisconnectOperation) {
                    this.mScanForWeakSignal = true;
                    this.mWifiNative.bssFlush();
                    startScan(-1, 0, null, null);
                }
            }
            if (!this.mWifiFwkExt.shouldAutoConnect()) {
                disableLastNetwork();
            }
            this.mDisconnectOperation = false;
            this.mLastCheckWeakSignalTime = 0;
        }
        if (DBG) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        stopRssiMonitoringOffload();
        clearCurrentConfigBSSID("handleNetworkDisconnect");
        stopIpManager();
        if (!this.mScreenOn) {
            this.mIdleDisConnTimes++;
        }
        if (this.mMtkCtpppoe && this.mUsingPppoe) {
            stopPPPoE();
        }
        this.mWifiScoreReport = null;
        this.mWifiInfo.reset();
        this.linkDebouncing = false;
        this.mAutoRoaming = false;
        setNetworkDetailedState(DetailedState.DISCONNECTED);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent = null;
        }
        this.mWifiConfigManager.updateStatus(this.mLastNetworkId, DetailedState.DISCONNECTED);
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        autoRoamSetBSSID(this.mLastNetworkId, WifiLastResortWatchdog.BSSID_ANY);
        this.mLastBssid = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
    }

    private void handleSupplicantConnectionLoss(boolean killSupplicant) {
        if (killSupplicant) {
            this.mWifiMonitor.killSupplicant(this.mP2pSupported);
        }
        this.mWifiNative.closeSupplicantConnection();
        sendSupplicantConnectionChangedBroadcast(false);
        setWifiState(1);
    }

    void handlePreDhcpSetup() {
        if (!this.mBluetoothConnectionActive) {
            this.mWifiNative.setBluetoothCoexistenceMode(1);
        }
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(false);
        getWifiLinkLayerStats(false);
        Message msg = new Message();
        msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
        msg.arg1 = 1;
        msg.arg2 = 196614;
        msg.obj = this;
        this.mWifiP2pChannel.sendMessage(msg);
    }

    void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        this.mWifiNative.setPowerSave(true);
        this.mWifiP2pChannel.sendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(2);
    }

    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        switch (level2FailureCode) {
            case 1:
            case 8:
                return;
            default:
                this.mWifiLogger.reportConnectionFailure();
                return;
        }
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (DBG) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            logd("link address " + dhcpResults.ipAddress);
        }
        synchronized (this.mDhcpResultsLock) {
            WifiConfiguration wifiCfg = getCurrentWifiConfiguration();
            if (wifiCfg != null) {
                String ssid = wifiCfg.getPrintableSsid();
                Log.d(TAG, "IP recover: record put-->" + ssid);
                mDhcpResultMap.put(ssid, new DhcpResults(dhcpResults));
            }
            this.mDhcpResults = dhcpResults;
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }
        if (isRoaming() && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            logd("handleIPv4Success, roaming and address changed" + this.mWifiInfo + " got: " + addr);
        }
        this.mWifiInfo.setInetAddress(addr);
        if (!this.mWifiInfo.getMeteredHint()) {
            this.mWifiInfo.setMeteredHint(dhcpResults.hasMeteredHint());
            updateCapabilities(getCurrentWifiConfiguration());
        }
    }

    private void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        this.mRssiCount = 5;
        for (int i = 0; i < 5; i++) {
            this.mRssiArray[i] = 0;
        }
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            c.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(c);
        }
        if (c != null) {
            ScanResult result = getCurrentScanResult();
            if (result == null) {
                logd("WifiStateMachine: handleSuccessfulIpConfiguration and no scan results" + c.configKey());
                return;
            }
            result.numIpConfigFailures = 0;
            this.mWifiConfigManager.clearBssidBlacklist();
        }
    }

    private void handleIPv4Failure() {
        this.mWifiLogger.captureBugReportData(4);
        if (DBG) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(10, 2);
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        if (DBG) {
            logd("handleIPv4Failure");
        }
    }

    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.sendMessage(CMD_IP_CONFIGURATION_LOST);
        }
        this.mWifiNative.disconnect();
        if (hasCustomizedAutoConnect()) {
            this.mIpConfigLost = true;
            this.mDisconnectOperation = true;
        } else if (!getEnableAutoJoinWhenAssociated()) {
            this.mWifiNative.reconnect();
        }
    }

    private void handleIpReachabilityLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect();
    }

    private int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < 5170 || frequency > 5825) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    private int chooseApChannel(int apBand) {
        int apChannel;
        if (apBand == 0) {
            ArrayList<Integer> allowed2GChannel = this.mWifiApConfigStore.getAllowed2GChannel();
            if (allowed2GChannel == null || allowed2GChannel.size() == 0) {
                if (DBG) {
                    Log.d(TAG, "No specified 2G allowed channel list");
                }
                apChannel = 6;
            } else {
                apChannel = ((Integer) allowed2GChannel.get(mRandom.nextInt(allowed2GChannel.size()))).intValue();
            }
        } else {
            int[] channel = this.mWifiNative.getChannelsForBand(2);
            if (channel == null || channel.length <= 0) {
                Log.e(TAG, "SoftAp do not get available channel list");
                apChannel = 0;
            } else {
                apChannel = convertFrequencyToChannelNumber(channel[mRandom.nextInt(channel.length)]);
            }
        }
        if (DBG) {
            Log.d(TAG, "SoftAp set on channel " + apChannel);
        }
        return apChannel;
    }

    private boolean setupDriverForSoftAp() {
        if (this.mWifiNative.loadDriver()) {
            if (this.mWifiNative.queryInterfaceIndex(this.mInterfaceName) != -1) {
                if (!this.mWifiNative.setInterfaceUp(false)) {
                    Log.e(TAG, "toggleInterface failed");
                    return false;
                }
            } else if (DBG) {
                Log.d(TAG, "No interfaces to bring down");
            }
            try {
                this.mNwService.wifiFirmwareReload(this.mInterfaceName, "AP");
                if (DBG) {
                    Log.d(TAG, "Firmware reloaded in AP mode");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to reload AP firmware " + e);
            }
            if (!this.mWifiNative.startHal()) {
                Log.e(TAG, "Failed to start HAL");
            }
            return true;
        }
        Log.e(TAG, "Failed to load driver for softap");
        return false;
    }

    private byte[] macAddressFromString(String macString) {
        String[] macBytes = macString.split(":");
        if (macBytes.length != 6) {
            throw new IllegalArgumentException("MAC address should be 6 bytes long!");
        }
        byte[] mac = new byte[6];
        for (int i = 0; i < macBytes.length; i++) {
            mac[i] = Integer.valueOf(Integer.parseInt(macBytes[i], 16)).byteValue();
        }
        return mac;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0079 A:{SYNTHETIC, Splitter: B:31:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006a A:{SYNTHETIC, Splitter: B:24:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0082 A:{SYNTHETIC, Splitter: B:36:0x0082} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String macAddressFromRoute(String ipAddress) {
        Throwable th;
        String macAddress = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            try {
                String readLine = reader2.readLine();
                while (true) {
                    readLine = reader2.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] tokens = readLine.split("[ ]+");
                    if (tokens.length >= 6) {
                        String ip = tokens[0];
                        String mac = tokens[3];
                        if (ipAddress.equals(ip)) {
                            macAddress = mac;
                            break;
                        }
                    }
                }
                if (macAddress == null) {
                    loge("Did not find remoteAddress {" + ipAddress + "} in " + "/proc/net/arp");
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e2) {
                reader = reader2;
                loge("Could not open /proc/net/arp to lookup mac address");
                if (reader != null) {
                }
                return macAddress;
            } catch (IOException e3) {
                reader = reader2;
                try {
                    loge("Could not read /proc/net/arp to lookup mac address");
                    if (reader != null) {
                    }
                    return macAddress;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            return macAddress;
        } catch (IOException e7) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            return macAddress;
        }
        return macAddress;
    }

    void maybeRegisterNetworkFactory() {
        if (this.mNetworkFactory == null) {
            checkAndSetConnectivityInstance();
            if (this.mCm != null) {
                this.mNetworkFactory = new WifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE, this.mNetworkCapabilitiesFilter);
                this.mNetworkFactory.setScoreFilter(5);
                this.mNetworkFactory.register();
                this.mUntrustedNetworkFactory = new UntrustedWifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE_UNTRUSTED, this.mNetworkCapabilitiesFilter);
                this.mUntrustedNetworkFactory.setScoreFilter(Integer.MAX_VALUE);
                this.mUntrustedNetworkFactory.register();
            }
        }
    }

    String smToString(Message message) {
        return smToString(message.what);
    }

    String smToString(int what) {
        String s = (String) sSmToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case 69632:
                s = "AsyncChannel.CMD_CHANNEL_HALF_CONNECTED";
                break;
            case 69636:
                s = "AsyncChannel.CMD_CHANNEL_DISCONNECTED";
                break;
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                s = "GROUP_CREATING_TIMED_OUT";
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                s = "P2P_CONNECTION_CHANGED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                s = "WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                s = "P2P.DISCONNECT_WIFI_RESPONSE";
                break;
            case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                s = "P2P.SET_MIRACAST_MODE";
                break;
            case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                s = "P2P.BLOCK_DISCOVERY";
                break;
            case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                s = "SUP_CONNECTION_EVENT";
                break;
            case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                s = "SUP_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                s = "NETWORK_CONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                s = "NETWORK_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                s = "SCAN_RESULTS_EVENT";
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                s = "SUPPLICANT_STATE_CHANGE_EVENT";
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                s = "AUTHENTICATION_FAILURE_EVENT";
                break;
            case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                s = "WPS_SUCCESS_EVENT";
                break;
            case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                s = "WPS_FAIL_EVENT";
                break;
            case WifiMonitor.DRIVER_HUNG_EVENT /*147468*/:
                s = "DRIVER_HUNG_EVENT";
                break;
            case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
                s = "SSID_TEMP_DISABLED";
                break;
            case WifiMonitor.SSID_REENABLED /*147470*/:
                s = "SSID_REENABLED";
                break;
            case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                s = "SUP_REQUEST_IDENTITY";
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                s = "SCAN_FAILED_EVENT";
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                s = "ASSOCIATION_REJECTION_EVENT";
                break;
            case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                s = "WifiMonitor.ANQP_DONE_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_START_EVENT /*147507*/:
                s = "WifiMonitor.GAS_QUERY_START_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_DONE_EVENT /*147508*/:
                s = "WifiMonitor.GAS_QUERY_DONE_EVENT";
                break;
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                s = "WifiMonitor.RX_HS20_ANQP_ICON_EVENT";
                break;
            case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                s = "WifiMonitor.HS20_REMEDIATION_EVENT";
                break;
            case WifiMonitor.WAPI_NO_CERTIFICATION_EVENT /*147536*/:
                s = "WAPI_NO_CERTIFICATION_EVENT";
                break;
            case WifiMonitor.TDLS_CONNECTED_EVENT /*147539*/:
                s = "TDLS_CONNECTED_EVENT";
                break;
            case WifiMonitor.TDLS_DISCONNECTED_EVENT /*147540*/:
                s = "TDLS_DISCONNECTED_EVENT";
                break;
            case 151553:
                s = "CONNECT_NETWORK";
                break;
            case 151556:
                s = "FORGET_NETWORK";
                break;
            case 151559:
                s = "SAVE_NETWORK";
                break;
            case 151562:
                s = "START_WPS";
                break;
            case 151563:
                s = "START_WPS_SUCCEEDED";
                break;
            case 151564:
                s = "WPS_FAILED";
                break;
            case 151565:
                s = "WPS_COMPLETED";
                break;
            case 151566:
                s = "CANCEL_WPS";
                break;
            case 151567:
                s = "CANCEL_WPS_FAILED";
                break;
            case 151568:
                s = "CANCEL_WPS_SUCCEDED";
                break;
            case 151569:
                s = "WifiManager.DISABLE_NETWORK";
                break;
            case 151572:
                s = "RSSI_PKTCNT_FETCH";
                break;
            default:
                s = "what:" + Integer.toString(what);
                break;
        }
        return s;
    }

    void registerConnected() {
        if (this.mLastNetworkId != -1) {
            WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
            if (config != null) {
                config.lastConnected = System.currentTimeMillis();
                config.numAssociation++;
                NetworkSelectionStatus networkSelectionStatus = config.getNetworkSelectionStatus();
                networkSelectionStatus.clearDisableReasonCounter();
                networkSelectionStatus.setHasEverConnected(true);
            }
            this.mWifiScoreReport = null;
        }
    }

    void registerDisconnected() {
        if (this.mLastNetworkId != -1) {
            WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
            if (config != null) {
                config.lastDisconnected = System.currentTimeMillis();
                if (config.ephemeral) {
                    this.mWifiConfigManager.forgetNetwork(this.mLastNetworkId);
                }
            }
        }
    }

    void noteWifiDisabledWhileAssociated() {
        int rssi = this.mWifiInfo.getRssi();
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (getCurrentState() == this.mConnectedState && rssi != -127 && config != null) {
            boolean is24GHz = this.mWifiInfo.is24GHz();
            boolean isBadRSSI = (!is24GHz || rssi >= this.mWifiConfigManager.mThresholdMinimumRssi24.get()) ? !is24GHz && rssi < this.mWifiConfigManager.mThresholdMinimumRssi5.get() : true;
            boolean isLowRSSI = (!is24GHz || rssi >= this.mWifiConfigManager.mThresholdQualifiedRssi24.get()) ? !is24GHz && this.mWifiInfo.getRssi() < this.mWifiConfigManager.mThresholdQualifiedRssi5.get() : true;
            boolean isHighRSSI = (!is24GHz || rssi < this.mWifiConfigManager.mThresholdSaturatedRssi24.get()) ? !is24GHz && this.mWifiInfo.getRssi() >= this.mWifiConfigManager.mThresholdSaturatedRssi5.get() : true;
            if (isBadRSSI) {
                config.numUserTriggeredWifiDisableLowRSSI++;
            } else if (isLowRSSI) {
                config.numUserTriggeredWifiDisableBadRSSI++;
            } else if (!isHighRSSI) {
                config.numUserTriggeredWifiDisableNotHighRSSI++;
            }
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        if (this.mLastNetworkId == -1) {
            return null;
        }
        return this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
    }

    ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String BSSID = this.mWifiInfo.getBSSID();
        if (BSSID == null) {
            BSSID = this.mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCache(config);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.get(BSSID);
    }

    String getCurrentBSSID() {
        if (this.linkDebouncing) {
            return null;
        }
        return this.mLastBssid;
    }

    private void updateCapabilities(WifiConfiguration config) {
        NetworkCapabilities networkCapabilities = new NetworkCapabilities(this.mDfltNetworkCapabilities);
        if (config != null) {
            int rssi;
            if (config.ephemeral) {
                networkCapabilities.removeCapability(14);
            } else {
                networkCapabilities.addCapability(14);
            }
            if (this.mWifiInfo.getRssi() != -127) {
                rssi = this.mWifiInfo.getRssi();
            } else {
                rssi = Integer.MIN_VALUE;
            }
            networkCapabilities.setSignalStrength(rssi);
        }
        if (this.mWifiInfo.getMeteredHint()) {
            networkCapabilities.removeCapability(11);
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkCapabilities(networkCapabilities);
        }
    }

    void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
    }

    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        if (imsi == null || imsi.isEmpty()) {
            return "";
        }
        String prefix;
        String mcc;
        String mnc;
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = "0";
        } else if (eapMethod != 6) {
            return "";
        } else {
            prefix = "6";
        }
        if (mccMnc == null || mccMnc.isEmpty()) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    boolean startScanForConfiguration(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCache(config);
        if (scanDetailCache == null || !config.allowedKeyManagement.get(1) || scanDetailCache.size() > 6) {
            return true;
        }
        HashSet<Integer> freqs = this.mWifiConfigManager.makeChannelList(config, ONE_HOUR_MILLI);
        if (freqs == null || freqs.size() == 0) {
            if (DBG) {
                logd("no channels for " + config.configKey());
            }
            return false;
        }
        logd("starting scan for " + config.configKey() + " with " + freqs);
        Set<Integer> hiddenNetworkIds = new HashSet();
        if (config.hiddenSSID) {
            hiddenNetworkIds.add(Integer.valueOf(config.networkId));
        }
        if (startScanNative(freqs, hiddenNetworkIds, WIFI_WORK_SOURCE)) {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_OK;
        } else {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_HANDLING_ERROR;
        }
        return true;
    }

    void clearCurrentConfigBSSID(String dbg) {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            clearConfigBSSID(config, dbg);
        }
    }

    void clearConfigBSSID(WifiConfiguration config, String dbg) {
        if (config != null) {
            if (DBG) {
                logd(dbg + " " + this.mTargetRoamBSSID + " config " + config.configKey() + " config.NetworkSelectionStatus.mNetworkSelectionBSSID " + config.getNetworkSelectionStatus().getNetworkSelectionBSSID());
            }
            if (DBG) {
                logd(dbg + " " + config.SSID + " nid=" + Integer.toString(config.networkId));
            }
            config.BSSID = WifiLastResortWatchdog.BSSID_ANY;
            this.mWifiConfigManager.saveWifiConfigBSSID(config, WifiLastResortWatchdog.BSSID_ANY);
        }
    }

    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (this.mWifiConfigManager.isLastSelectedConfiguration(config)) {
            boolean prompt = this.mWifiConfigManager.checkConfigOverridePermission(config.lastConnectUid);
            if (DBG) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt && DBG) {
                log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
            }
        }
        this.mConnectedId = this.mLastNetworkId;
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.setNetworkDetailState(this.mLastNetworkId, DetailedState.CONNECTED, this.mLastBssid);
        }
        if (wConnection) {
            wConnected = true;
            wConnection = false;
            wStartWps = false;
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(true, this.mLastNetworkId);
            WifiConfiguration mLastConfig = getCurrentWifiConfiguration();
            if (mLastConfig != null) {
                OppoAutoConnectManager.getInstance().removeAutoConnectConfiguration(mLastConfig.configKey());
            }
        }
        setNetworkDetailedState(DetailedState.CONNECTED);
        this.mWifiConfigManager.updateStatus(this.mLastNetworkId, DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private Message obtainMessageWithWhatAndArg2(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    private void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent("android.net.wifi.WIFI_CREDENTIAL_CHANGED");
            intent.putExtra("ssid", config.SSID);
            intent.putExtra("et", wifiCredentialEventType);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE");
        }
    }

    private static int parseHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - 48;
        }
        if ('a' <= ch && ch <= 'f') {
            return (ch - 97) + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return (ch - 65) + 10;
        }
        throw new NumberFormatException("" + ch + " is not a valid hex digit");
    }

    private byte[] parseHex(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        if (hex.length() % 2 != 0) {
            throw new NumberFormatException(hex + " is not a valid hex string");
        }
        byte[] result = new byte[((hex.length() / 2) + 1)];
        result[0] = (byte) (hex.length() / 2);
        int i = 0;
        int j = 1;
        while (i < hex.length()) {
            result[j] = (byte) (((parseHex(hex.charAt(i)) * 16) + parseHex(hex.charAt(i + 1))) & 255);
            i += 2;
            j++;
        }
        return result;
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            Object[] objArr = new Object[1];
            objArr[0] = Byte.valueOf(b);
            sb.append(String.format("%02x", objArr));
        }
        return sb.toString();
    }

    private static String makeHex(byte[] bytes, int from, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            Object[] objArr = new Object[1];
            objArr[0] = Byte.valueOf(bytes[from + i]);
            sb.append(String.format("%02x", objArr));
        }
        return sb.toString();
    }

    private static byte[] concat(byte[] array1, byte[] array2, byte[] array3) {
        int length;
        int i = 0;
        int len = (array1.length + array2.length) + array3.length;
        if (array1.length != 0) {
            len++;
        }
        if (array2.length != 0) {
            len++;
        }
        if (array3.length != 0) {
            len++;
        }
        byte[] result = new byte[len];
        int index = 0;
        if (array1.length != 0) {
            result[0] = (byte) (array1.length & 255);
            index = 1;
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }
        if (array2.length != 0) {
            result[index] = (byte) (array2.length & 255);
            index++;
            for (byte b2 : array2) {
                result[index] = b2;
                index++;
            }
        }
        if (array3.length != 0) {
            result[index] = (byte) (array3.length & 255);
            index++;
            length = array3.length;
            while (i < length) {
                result[index] = array3[i];
                index++;
                i++;
            }
        }
        return result;
    }

    private static byte[] concatHex(byte[] array1, byte[] array2) {
        int length;
        int i = 0;
        byte[] result = new byte[(array1.length + array2.length)];
        int index = 0;
        if (array1.length != 0) {
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }
        if (array2.length != 0) {
            length = array2.length;
            while (i < length) {
                result[index] = array2[i];
                index++;
                i++;
            }
        }
        return result;
    }

    String getGsmSimAuthResponse(String[] requestData, TelephonyManager tm) {
        return getGsmSimAuthResponse(requestData, tm, -1);
    }

    String getGsmSimAuthResponse(String[] requestData, TelephonyManager tm, int netId) {
        String tmResponse;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int length = requestData.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return sb.toString();
            }
            String challenge = requestData[i2];
            if (!(challenge == null || challenge.isEmpty())) {
                logd("RAND = " + challenge);
                try {
                    String base64Challenge = Base64.encodeToString(parseHex(challenge), 2);
                    int appType = 2;
                    tmResponse = getIccAuthentication(2, 128, base64Challenge, tm, netId);
                    if (tmResponse == null) {
                        appType = 1;
                        tmResponse = getIccAuthentication(1, 128, base64Challenge, tm, netId);
                    }
                    logv("Raw Response - " + tmResponse);
                    if (tmResponse == null || tmResponse.length() <= 4) {
                        loge("bad response - " + tmResponse);
                    } else {
                        byte[] result = Base64.decode(tmResponse, 0);
                        logv("Hex Response -" + makeHex(result));
                        String sres = null;
                        String kc = null;
                        if (appType == 2) {
                            int sres_len = result[0];
                            if (sres_len >= result.length) {
                                loge("malfomed response - " + tmResponse);
                                return null;
                            }
                            sres = makeHex(result, 1, sres_len);
                            int kc_offset = sres_len + 1;
                            if (kc_offset >= result.length) {
                                loge("malfomed response - " + tmResponse);
                                return null;
                            }
                            int kc_len = result[kc_offset];
                            if (kc_offset + kc_len > result.length) {
                                loge("malfomed response - " + tmResponse);
                                return null;
                            }
                            kc = makeHex(result, kc_offset + 1, kc_len);
                        } else if (appType == 1) {
                            if (result.length < 12) {
                                loge("malfomed response - " + tmResponse);
                                return null;
                            }
                            sres = makeHex(result, 0, 4);
                            kc = makeHex(result, 4, 8);
                        }
                        sb.append(":").append(kc).append(":").append(sres);
                        logv("kc:" + kc + " sres:" + sres);
                    }
                } catch (NumberFormatException e) {
                    loge("malformed challenge");
                }
            }
            i = i2 + 1;
        }
        loge("bad response - " + tmResponse);
        return null;
    }

    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            if (tm == null) {
                loge("could not get telephony manager");
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
                return;
            }
            String response = getGsmSimAuthResponse(requestData.data, tm, requestData.networkId);
            if (response == null) {
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
            } else {
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, "GSM-AUTH", response);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    void handle3GAuthRequest(SimAuthRequestData requestData) {
        StringBuilder sb = new StringBuilder();
        byte[] rand = null;
        byte[] authn = null;
        String res_type = "UMTS-AUTH";
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            if (requestData.data.length == 2) {
                try {
                    rand = parseHex(requestData.data[0]);
                    authn = parseHex(requestData.data[1]);
                } catch (NumberFormatException e) {
                    loge("malformed challenge");
                }
            } else {
                loge("malformed challenge");
            }
            String tmResponse = "";
            if (!(rand == null || authn == null)) {
                String base64Challenge = Base64.encodeToString(concatHex(rand, authn), 2);
                TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
                if (tm != null) {
                    tmResponse = getIccAuthentication(2, 129, base64Challenge, tm, requestData.networkId);
                    logv("Raw Response - " + tmResponse);
                } else {
                    loge("could not get telephony manager");
                }
            }
            boolean good_response = false;
            if (tmResponse == null || tmResponse.length() <= 4) {
                loge("bad response - " + tmResponse);
            } else {
                byte[] result = Base64.decode(tmResponse, 0);
                loge("Hex Response - " + makeHex(result));
                byte tag = result[0];
                if (tag == (byte) -37) {
                    logv("successful 3G authentication ");
                    int res_len = result[1];
                    String res = makeHex(result, 2, res_len);
                    int ck_len = result[res_len + 2];
                    String ck = makeHex(result, res_len + 3, ck_len);
                    byte[] bArr = result;
                    String ik = makeHex(bArr, (res_len + ck_len) + 4, result[(res_len + ck_len) + 3]);
                    sb.append(":").append(ik).append(":").append(ck).append(":").append(res);
                    logv("ik:" + ik + "ck:" + ck + " res:" + res);
                    good_response = true;
                } else if (tag == (byte) -36) {
                    loge("synchronisation failure");
                    String auts = makeHex(result, 2, result[1]);
                    res_type = "UMTS-AUTS";
                    sb.append(":").append(auts);
                    logv("auts:" + auts);
                    good_response = true;
                } else {
                    loge("bad response - unknown tag = " + tag);
                }
            }
            if (good_response) {
                String response = sb.toString();
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, res_type, response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(requestData.networkId);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    public void autoConnectToNetwork(int networkId, String bssid) {
        synchronized (this.mWifiReqCountLock) {
            if (hasConnectionRequests()) {
                sendMessage(CMD_AUTO_CONNECT, networkId, 0, bssid);
            }
        }
    }

    public void autoRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_AUTO_ROAM, networkId, 0, scanResult);
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled ? 1 : 0);
    }

    static boolean unexpectedDisconnectedReason(int reason) {
        if (reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34) {
            return true;
        }
        return false;
    }

    void updateWifiMetrics() {
        int numSavedNetworks = this.mWifiConfigManager.getConfiguredNetworksSize();
        int numOpenNetworks = 0;
        int numPersonalNetworks = 0;
        int numEnterpriseNetworks = 0;
        int numNetworksAddedByUser = 0;
        int numNetworksAddedByApps = 0;
        int numHiddenNetworks = 0;
        int numPasspoint = 0;
        for (WifiConfiguration config : this.mWifiConfigManager.getSavedNetworks()) {
            if (config.allowedKeyManagement.get(0)) {
                numOpenNetworks++;
            } else if (config.isEnterprise()) {
                numEnterpriseNetworks++;
            } else {
                numPersonalNetworks++;
            }
            if (config.selfAdded) {
                numNetworksAddedByUser++;
            } else {
                numNetworksAddedByApps++;
            }
            if (config.hiddenSSID) {
                numHiddenNetworks++;
            }
            if (config.isPasspoint()) {
                numPasspoint++;
            }
        }
        this.mWifiMetrics.setNumSavedNetworks(numSavedNetworks);
        this.mWifiMetrics.setNumOpenNetworks(numOpenNetworks);
        this.mWifiMetrics.setNumPersonalNetworks(numPersonalNetworks);
        this.mWifiMetrics.setNumEnterpriseNetworks(numEnterpriseNetworks);
        this.mWifiMetrics.setNumNetworksAddedByUser(numNetworksAddedByUser);
        this.mWifiMetrics.setNumNetworksAddedByApps(numNetworksAddedByApps);
        this.mWifiMetrics.setNumHiddenNetworks(numHiddenNetworks);
        this.mWifiMetrics.setNumPasspointNetworks(numPasspoint);
    }

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList(6);
        if (lp.hasIPv4Address()) {
            attributes.add("v4");
        }
        if (lp.hasIPv4DefaultRoute()) {
            attributes.add("v4r");
        }
        if (lp.hasIPv4DnsServer()) {
            attributes.add("v4dns");
        }
        if (lp.hasGlobalIPv6Address()) {
            attributes.add("v6");
        }
        if (lp.hasIPv6DefaultRoute()) {
            attributes.add("v6r");
        }
        if (lp.hasIPv6DnsServer()) {
            attributes.add("v6dns");
        }
        return TextUtils.join(" ", attributes);
    }

    private void wnmFrameReceived(WnmData event) {
        Intent intent = new Intent("android.net.wifi.PASSPOINT_WNM_FRAME_RECEIVED");
        intent.addFlags(67108864);
        intent.putExtra("bssid", event.getBssid());
        intent.putExtra("url", event.getUrl());
        if (event.isDeauthEvent()) {
            intent.putExtra("ess", event.isEss());
            intent.putExtra("delay", event.getDelay());
        } else {
            intent.putExtra("method", event.getMethod());
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (!(config == null || config.FQDN == null)) {
                intent.putExtra("match", this.mWifiConfigManager.matchProviderWithCurrentNetwork(config.FQDN));
            }
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getWifiConfiguration(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    public void setTargetNetworkId(int id) {
        if (id != -1) {
            this.mTargetNetworkId = id;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkAndSetSsidForConfig(WifiConfiguration config) {
        if (config != null) {
            String configSSID = config.SSID;
            if (configSSID != null && !configSSID.equals("")) {
                String configBssid = config.BSSID;
                if (configBssid != null && !configBssid.equals("00:00:00:00:00:00") && this.mScanResults != null) {
                    List<ScanResult> srList = syncGetScanResultsList();
                    Long newestTimeStamp = Long.valueOf(0);
                    String targetSsid = null;
                    int count = 0;
                    for (ScanResult sr : srList) {
                        if (sr != null) {
                            String bssid = sr.BSSID;
                            String ssid = sr.SSID;
                            if (configBssid.equals(bssid)) {
                                count++;
                                if (sr.timestamp > newestTimeStamp.longValue()) {
                                    targetSsid = ssid;
                                    newestTimeStamp = Long.valueOf(sr.timestamp);
                                }
                            }
                        }
                    }
                    if (DBG) {
                        Log.d(TAG, "same bssid count = " + count);
                    }
                    if (count > 1) {
                        if (targetSsid == null || targetSsid.equals("")) {
                            Log.d(TAG, "target = " + targetSsid);
                            return;
                        }
                        if (DBG) {
                            Log.d(TAG, "set manu connect from " + configSSID + " ssid to " + targetSsid);
                        }
                        config.SSID = targetSsid;
                    }
                }
            }
        }
    }

    private boolean hasConnectionRequests() {
        return this.mConnectionReqCount > 0 || this.mUntrustedReqCount > 0;
    }

    public boolean syncDoCtiaTestOn(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_DO_CTIA_TEST_ON);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDoCtiaTestOff(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_DO_CTIA_TEST_OFF);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDoCtiaTestRate(AsyncChannel channel, int rate) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_DO_CTIA_TEST_RATE, rate);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncSetTxPowerEnabled(AsyncChannel channel, boolean enable) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_SET_TX_POWER_ENABLED, enable ? 1 : 0);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncSetTxPower(AsyncChannel channel, int offset) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_SET_TX_POWER, offset);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public PPPOEInfo syncGetPppoeInfo() {
        if (!this.mMtkCtpppoe) {
            return null;
        }
        this.mPppoeInfo.online_time = (System.currentTimeMillis() / 1000) - this.mOnlineStartTime;
        return this.mPppoeInfo;
    }

    public int syncGetConnectingNetworkId(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_GET_CONNECTING_NETWORK_ID);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<Integer> syncGetDisconnectNetworks() {
        return this.mWifiConfigManager.getDisconnectNetworks();
    }

    public boolean isNetworksDisabledDuringConnect() {
        if ((this.mSupplicantStateTracker.isNetworksDisabledDuringConnect() && isExplicitNetworkExist()) || getCurrentState() == this.mWpsRunningState) {
            return true;
        }
        return false;
    }

    public boolean hasConnectableAp() {
        sendMessage(M_CMD_FLUSH_BSS);
        return this.mWifiFwkExt != null ? this.mWifiFwkExt.hasConnectableAp() : false;
    }

    public void suspendNotification(int type) {
        if (this.mWifiFwkExt != null) {
            this.mWifiFwkExt.suspendNotification(type);
        }
    }

    private void initializeExtra() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.mtk.stopscan.activated");
        intentFilter.addAction("com.mtk.stopscan.deactivated");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("com.mediatek.common.wifi.AUTOCONNECT_SETTINGS_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(WifiStateMachine.TAG, "onReceive, action:" + action);
                if (action.equals("com.mtk.stopscan.activated")) {
                    WifiStateMachine.this.mStopScanStarted.set(true);
                    WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY);
                } else if (action.equals("com.mtk.stopscan.deactivated")) {
                    WifiStateMachine.this.mStopScanStarted.set(false);
                    WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_UPDATE_SCAN_STRATEGY);
                } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    String iccState = intent.getStringExtra("ss");
                    int simSlot = intent.getIntExtra("slot", -1);
                    WifiStateMachine.this.log("iccState:" + iccState + ", simSlot: " + simSlot);
                    if (simSlot == 0 || -1 == simSlot) {
                        WifiStateMachine.this.mSim1IccState = iccState;
                    } else {
                        WifiStateMachine.this.mSim2IccState = iccState;
                    }
                    if (iccState.equals("LOADED")) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_ENABLE_EAP_SIM_CONFIG_NETWORK);
                    }
                } else if (action.equals("com.mediatek.common.wifi.AUTOCONNECT_SETTINGS_CHANGE")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.M_CMD_UPDATE_SETTINGS);
                }
            }
        }, intentFilter);
        if (this.mWifiFwkExt != null) {
            this.mMtkCtpppoe = this.mWifiFwkExt.isPppoeSupported();
        }
        if (this.mMtkCtpppoe) {
            this.mPppoeInfo = new PPPOEInfo();
            this.mPppoeLinkProperties = new LinkProperties();
        }
        this.mEnableRssiSmoothing = this.mContext.getResources().getBoolean(135004168);
        this.mAffectRoaming = this.mContext.getResources().getBoolean(135004169);
        this.mRssiSmoothingThreshold = this.mContext.getResources().getInteger(134938627);
    }

    private void sendPppoeCompletedBroadcast(String status, int errorCode) {
        Intent intent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
        intent.addFlags(67108864);
        intent.putExtra("pppoe_result_status", status);
        if (status.equals("FAILURE")) {
            intent.putExtra("pppoe_result_error_code", Integer.toString(errorCode));
        }
        Log.d(TAG, "sendPppoeCompletedBroadcast, status:" + status + ", errorCode:" + errorCode);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendPppoeStateChangedBroadcast(String state) {
        Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("pppoe_state", state);
        Log.d(TAG, "sendPppoeStateChangedBroadcast, state:" + state);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public String getWifiStatus(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_GET_WIFI_STATUS);
        String result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public void setPowerSavingMode(boolean mode) {
        int i;
        if (mode) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(obtainMessage(M_CMD_SET_POWER_SAVING_MODE, i, 0));
    }

    public void setTdlsPowerSave(boolean enable) {
        int i;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(obtainMessage(M_CMD_SET_TDLS_POWER_SAVE, i, 0));
    }

    private void stopPPPoE() {
        Log.d(TAG, "stopPPPoE, mPppoeInfo:" + this.mPppoeInfo);
        this.mUsingPppoe = false;
        if (this.mPppoeHandler != null) {
            this.mPppoeHandler.setCancelCallback(true);
            if (this.mPppoeHandler.hasMessages(EVENT_START_PPPOE)) {
                Log.e(TAG, "hasMessages EVENT_START_PPPOE!");
                this.mPppoeHandler.removeMessages(EVENT_START_PPPOE);
            }
        } else {
            Log.e(TAG, "mPppoeHandler is null!");
        }
        sendPppoeStateChangedBroadcast("PPPOE_STATE_DISCONNECTING");
        try {
            this.mNwService.removeInterfaceFromNetwork(this.mPppoeLinkProperties.getInterfaceName(), PPPOE_NETID);
            this.mNwService.removeNetwork(PPPOE_NETID);
            Log.d(TAG, "removeNetwork successfully!");
        } catch (Exception e) {
            Log.e(TAG, "Exception in removeNetwork:" + e.toString());
        }
        try {
            this.mNwService.disablePPPOE();
            Log.d(TAG, "Stop PPPOE successfully!");
        } catch (Exception e2) {
            Log.e(TAG, "Exception in disablePPPOE:" + e2.toString());
        }
        this.mPppoeConfig = null;
        this.mPppoeInfo.status = Status.OFFLINE;
        this.mPppoeInfo.online_time = 0;
        this.mOnlineStartTime = 0;
        this.mPppoeLinkProperties.clear();
        sendPppoeStateChangedBroadcast("PPPOE_STATE_DISCONNECTED");
        if (this.mPppoeHandler != null) {
            this.mPppoeHandler.getLooper().quit();
            this.mPppoeHandler = null;
            return;
        }
        Log.e(TAG, "mPppoeHandler is null!");
    }

    private void handleSuccessfulPppoeConfiguration(DhcpResults pppoeResult) {
        if (pppoeResult == null) {
            Log.e(TAG, "pppoeResult is null");
            return;
        }
        this.mPppoeLinkProperties = pppoeResult.toLinkProperties("wlan0");
        Log.d(TAG, "handleSuccessfulPppoeConfiguration, mPppoeLinkProperties:" + this.mPppoeLinkProperties);
        Collection<RouteInfo> oldRouteInfos = this.mLinkProperties.getRoutes();
        for (RouteInfo route : oldRouteInfos) {
            Log.d(TAG, "RouteInfo of wlan0:" + route);
        }
        int wifiNetId = -1;
        Network[] networks = this.mCm.getAllNetworks();
        if (networks != null && networks.length > 0) {
            for (Network net : networks) {
                NetworkInfo info = this.mCm.getNetworkInfo(net);
                if (info != null && info.getType() == 1) {
                    wifiNetId = net.netId;
                    break;
                }
            }
        }
        Log.d(TAG, "wifiNetId:" + wifiNetId);
        if (wifiNetId != -1) {
            for (RouteInfo route2 : oldRouteInfos) {
                if (route2.isDefaultRoute()) {
                    try {
                        this.mNwService.removeRoute(wifiNetId, route2);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in removeRoute:" + e.toString());
                    }
                }
            }
        }
        Collection<InetAddress> dnses = this.mPppoeLinkProperties.getDnsServers();
        ArrayList<String> pppoeDnses = new ArrayList();
        for (InetAddress dns : dnses) {
            pppoeDnses.add(dns.getHostAddress());
        }
        String[] dnsArr = new String[pppoeDnses.size()];
        pppoeDnses.toArray(dnsArr);
        for (int i = 0; i < dnsArr.length; i++) {
            Log.d(TAG, "Set net.dns" + (i + 1) + " to " + dnsArr[i]);
            this.mPropertyService.set("net.dns" + (i + 1), dnsArr[i]);
        }
        try {
            this.mNwService.createPhysicalNetwork(PPPOE_NETID, null);
            this.mNwService.addInterfaceToNetwork("wlan0", PPPOE_NETID);
            this.mNwService.setDnsServersForNetwork(PPPOE_NETID, dnsArr, null);
            this.mNwService.setDefaultNetId(PPPOE_NETID);
            for (RouteInfo route22 : this.mPppoeLinkProperties.getRoutes()) {
                if (route22.isDefaultRoute()) {
                    this.mNwService.addRoute(PPPOE_NETID, route22);
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "Exception in config pppoe:" + e2.toString());
        }
        this.mPppoeInfo.status = Status.ONLINE;
        this.mOnlineStartTime = System.currentTimeMillis() / 1000;
        sendPppoeStateChangedBroadcast("PPPOE_STATE_CONNECTED");
        sendPppoeCompletedBroadcast("SUCCESS", 0);
    }

    public boolean syncSetWoWlanNormalMode(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_SET_WOWLAN_NORMAL_MODE);
        if (resultMsg == null) {
            log("syncSetWoWlanNormalMode fail, resultMsg == null");
            return false;
        }
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncSetWoWlanMagicMode(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_SET_WOWLAN_MAGIC_MODE);
        if (resultMsg == null) {
            log("syncSetWoWlanMagicMode fail, resultMsg == null");
            return false;
        }
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean shouldSwitchNetwork() {
        if (isTemporarilyDontReconnectWifi()) {
            loge("mDontReconnect: " + this.mDontReconnect.get());
            if (this.mDontReconnect.get()) {
                Log.d(TAG, "shouldSwitchNetwork don't switch due to mDontReconnect");
                return false;
            }
            Log.d(TAG, "shouldSwitchNetwork  switch! Even isTemporarilyDontReconnectWifi");
        }
        if (this.mTemporarilyDisconnectWifi) {
            Log.d(TAG, "shouldSwitchNetwork don't switch due to mTemporarilyDisconnectWifi");
            return false;
        } else if (this.mWifiFwkExt == null || this.mWifiFwkExt.hasNetworkSelection() == 0 || this.mWifiInfo.getNetworkId() == -1) {
            return true;
        } else {
            Log.d(TAG, "hasNetworkSelection Don't");
            return false;
        }
    }

    private void convertToQuotedSSID(WifiConfiguration config) {
        if (config != null && !TextUtils.isEmpty(config.SSID)) {
            if (config.SSID.charAt(0) != '\"' || (config.SSID.charAt(0) == '\"' && config.SSID.charAt(config.SSID.length() - 1) != '\"')) {
                String str = "";
                config.SSID = "\"" + config.SSID + "\"";
            }
        }
    }

    public boolean isTemporarilyDontReconnectWifi() {
        log("stopReconnectWifi StopScan=" + this.mStopScanStarted.get() + " mDontReconnectAndScan=" + this.mDontReconnectAndScan.get());
        if (this.mStopScanStarted.get() || this.mDontReconnectAndScan.get()) {
            return true;
        }
        return false;
    }

    public void setHotspotOptimization(boolean enable) {
        log("setHotspotOptimization " + enable);
        this.mHotspotOptimization = enable;
    }

    public String syncGetTestEnv(AsyncChannel channel, int wifiChannel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_GET_TEST_ENV, wifiChannel);
        String result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public String getIccAuthentication(int appType, int authType, String base64Challenge, TelephonyManager tm, int netId) {
        if (netId != -1) {
            WifiConfiguration config = getConfiguredNetworkByNetId(netId);
            if (TelephonyManager.getDefault().getPhoneCount() >= 2 && config != null) {
                int subId = TelephonyUtil.getSubId(WifiConfigurationUtil.getIntSimSlot(config));
                if (subId != -1) {
                    return tm.getIccAuthentication(subId, appType, authType, base64Challenge);
                }
            }
        }
        return tm.getIccAuthentication(appType, authType, base64Challenge);
    }

    private WifiConfiguration getConfiguredNetworkByNetId(int netId) {
        List<WifiConfiguration> networks = this.mWifiConfigManager.getSavedNetworks();
        if (networks != null) {
            for (WifiConfiguration config : networks) {
                if (config.networkId == netId) {
                    return config;
                }
            }
        }
        log("getConfiguredNetworkByNetId don't found config");
        return null;
    }

    private boolean setSimSlotNative(WifiConfiguration config) {
        if (config.simSlot == null || this.mWifiNative.setNetworkVariable(config.networkId, "sim_num", removeDoubleQuotes(config.simSlot))) {
            this.mWifiNative.saveConfig();
            return true;
        }
        Log.e(TAG, "failed to set simSlot: " + removeDoubleQuotes(config.simSlot));
        return false;
    }

    private String removeDoubleQuotes(String string) {
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public void factoryReset(int uid) {
        sendMessage(M_CMD_FACTORY_RESET, uid);
    }

    private void sendTdlsEventBroadcast(boolean isConnectedEvent, String macAddress) {
        Intent intent;
        logd("sendTdlsEventBroadcast peer: " + macAddress);
        if (isConnectedEvent) {
            intent = new Intent("android.net.wifi.TDLS_CONNECTED");
        } else {
            intent = new Intent("android.net.wifi.TDLS_DISCONNECTED");
        }
        if (macAddress != null) {
            intent.putExtra("tdls_bssid", macAddress);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void startListenToIpReachabilityLost() {
        this.mIsListeningIpReachabilityLost = true;
        this.ipReachabilityMonitorCount++;
        sendMessageDelayed(obtainMessage(M_CMD_IP_REACHABILITY_MONITOR_TIMER, this.ipReachabilityMonitorCount, 0), 10000);
    }

    private boolean enableIpReachabilityMonitor() {
        boolean enable = this.mPropertyService.get("persist.wifi.IRM.enable", "1").equals("1");
        log("enable IpReachabilityMonitor Enhancement config: " + this.enableIpReachabilityMonitor.get() + ", SystemProperty: " + enable);
        if (this.enableIpReachabilityMonitor.get() || enable) {
            return true;
        }
        Log.d(TAG, "No enable IpReachabilityMonitor");
        return false;
    }

    private boolean enableIpReachabilityMonitorEnhancement() {
        boolean enable = this.mPropertyService.get("persist.wifi.IRM.enhancement", "1").equals("1");
        log("enable IpReachabilityMonitor Enhancement config: " + this.enableIpReachabilityMonitorEnhancement.get() + ", SystemProperty: " + enable);
        if (this.enableIpReachabilityMonitorEnhancement.get() || enable) {
            return true;
        }
        Log.d(TAG, "No enable IpReachabilityMonitor Enhancement");
        return false;
    }

    private boolean checkOrCleanIdentity(WifiConfiguration config) {
        if (config == null || config.enterpriseConfig == null) {
            loge("config or enterpriseConfig is null");
            return false;
        }
        int eapMethod = config.enterpriseConfig.getEapMethod();
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            String imsi;
            log("TelephonyManager != null");
            String mccMnc = "";
            if (TelephonyManager.getDefault().getPhoneCount() >= 2) {
                int slotId = WifiConfigurationUtil.getIntSimSlot(config);
                log("simSlot: " + config.simSlot + " " + slotId);
                int subId = TelephonyUtil.getSubId(slotId);
                log("subId: " + subId);
                imsi = tm.getSubscriberId(subId);
                if (tm.getSimState(slotId) == 5) {
                    mccMnc = tm.getSimOperator(subId);
                }
            } else {
                imsi = tm.getSubscriberId();
                if (tm.getSimState() == 5) {
                    mccMnc = tm.getSimOperator();
                }
            }
            log("imsi: " + imsi);
            log("mccMnc: " + mccMnc);
            String identity = buildIdentity(eapMethod, imsi, mccMnc);
            if (identity.isEmpty()) {
                loge("identity is empty");
                return false;
            }
            if (config.enterpriseConfig.getIdentity().equals(identity)) {
                log("same card");
            } else {
                log("different identity: " + config.enterpriseConfig.getIdentity() + "new identity: " + identity);
                this.mWifiConfigManager.resetSimNetwork(config);
                resetIdentityAndAnonymousId(config);
            }
            return true;
        }
        loge("TelephonyManager is null");
        return false;
    }

    private void resetIdentityAndAnonymousId(WifiConfiguration config) {
        log("reset identity and anonymous_identity to NULL");
        config.enterpriseConfig.setIdentity("");
        config.enterpriseConfig.setAnonymousIdentity("");
    }

    private boolean isAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean isConfigSimCardLoaded(WifiConfiguration config) {
        int simSlot = WifiConfigurationUtil.getIntSimSlot(config);
        if (simSlot == -1) {
            log("simSlot: " + simSlot + " is unspecified, assume sim card is loaded");
            return true;
        }
        return (simSlot == 0 ? this.mSim1IccState : this.mSim2IccState).equals("LOADED");
    }

    private boolean isConfigSimCardAbsent(WifiConfiguration config) {
        int simSlot = WifiConfigurationUtil.getIntSimSlot(config);
        if (simSlot == -1) {
            log("simSlot: " + simSlot + " is unspecified, assume sim card isn't absent");
            return false;
        }
        boolean z;
        String iccState = simSlot == 0 ? this.mSim1IccState : this.mSim2IccState;
        if (iccState.equals("ABSENT") || iccState.equals("LOCKED")) {
            z = true;
        } else {
            z = iccState.equals("UNKNOWN");
        }
        return z;
    }

    public void autoConnectInit() {
        if (this.mWifiFwkExt != null) {
            this.mWifiFwkExt.init();
        }
        this.mWifiConfigManager.setWifiFwkExt(this.mWifiFwkExt);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public boolean hasCustomizedAutoConnect() {
        return this.mWifiFwkExt != null ? this.mWifiFwkExt.hasCustomizedAutoConnect() : false;
    }

    public boolean isWifiConnecting(int connectingNetworkId) {
        if ((this.mWifiFwkExt == null || !this.mWifiFwkExt.isWifiConnecting(connectingNetworkId, this.mWifiConfigManager.getDisconnectNetworks())) && getCurrentState() != this.mWpsRunningState) {
            return false;
        }
        return true;
    }

    private void showReselectionDialog() {
        this.mScanForWeakSignal = false;
        Log.d(TAG, "showReselectionDialog, mLastNetworkId:" + this.mLastNetworkId + ", mDisconnectNetworkId:" + this.mDisconnectNetworkId);
        int networkId = getHighPriorityNetworkId();
        if (networkId != -1) {
            if (this.mWifiFwkExt.shouldAutoConnect()) {
                Log.d(TAG, "Supplicant state is " + this.mWifiInfo.getSupplicantState() + " when try to connect network " + networkId);
                if (isNetworksDisabledDuringConnect()) {
                    Log.d(TAG, "WiFi is connecting!");
                } else {
                    sendMessage(obtainMessage(CMD_ENABLE_NETWORK, networkId, 1));
                }
            } else {
                this.mShowReselectDialog = this.mWifiFwkExt.handleNetworkReselection();
            }
        }
    }

    private int getHighPriorityNetworkId() {
        int networkId = -1;
        int rssi = MIN_RSSI;
        String ssid = null;
        List<WifiConfiguration> networks = this.mWifiConfigManager.getConfiguredNetworks();
        if (networks == null || networks.size() == 0) {
            Log.d(TAG, "No configured networks, ignore!");
            return -1;
        }
        HashMap<Integer, Integer> foundNetworks = new HashMap();
        if (this.mScanResults != null) {
            for (WifiConfiguration network : networks) {
                if (network.networkId != this.mDisconnectNetworkId) {
                    for (ScanDetail scanresult : this.mScanResults) {
                        if (network.SSID != null && scanresult.getSSID() != null && network.SSID.equals("\"" + scanresult.getSSID() + "\"") && getSecurity(network) == getSecurity(scanresult.getScanResult()) && scanresult.getScanResult().level > -79) {
                            foundNetworks.put(Integer.valueOf(network.priority), Integer.valueOf(scanresult.getScanResult().level));
                        }
                    }
                }
            }
        }
        if (foundNetworks.size() < 2) {
            Log.d(TAG, "Configured networks number less than two, ignore!");
            return -1;
        }
        Object[] keys = foundNetworks.keySet().toArray();
        Arrays.sort(keys, new Comparator<Object>() {
            public int compare(Object obj1, Object obj2) {
                return ((Integer) obj2).intValue() - ((Integer) obj1).intValue();
            }
        });
        int priority = ((Integer) keys[0]).intValue();
        for (WifiConfiguration network2 : networks) {
            if (network2.priority == priority) {
                networkId = network2.networkId;
                ssid = network2.SSID;
                rssi = ((Integer) foundNetworks.get(Integer.valueOf(priority))).intValue();
                break;
            }
        }
        Log.d(TAG, "Found the highest priority AP, networkId:" + networkId + ", priority:" + priority + ", rssi:" + rssi + ", ssid:" + ssid);
        return networkId;
    }

    private void disableAllNetworks(boolean except) {
        Log.d(TAG, "disableAllNetworks, except:" + except);
        List<WifiConfiguration> networks = this.mWifiConfigManager.getConfiguredNetworks();
        if (except) {
            if (networks != null) {
                for (WifiConfiguration network : networks) {
                    if (!(network.networkId == this.mLastNetworkId || network.status == 1)) {
                        this.mWifiConfigManager.disableNetwork(network.networkId);
                    }
                }
            }
        } else if (networks != null) {
            for (WifiConfiguration network2 : networks) {
                if (network2.status != 1) {
                    this.mWifiConfigManager.disableNetwork(network2.networkId);
                }
            }
        }
    }

    private void checkIfEapNetworkChanged(WifiConfiguration newConfig) {
        Log.d(TAG, "checkIfEapNetworkChanged, mLastNetworkId:" + this.mLastNetworkId + ", newConfig:" + newConfig);
        if (newConfig != null && this.mLastNetworkId != -1 && this.mLastNetworkId == newConfig.networkId && (newConfig.allowedKeyManagement.get(2) || newConfig.allowedKeyManagement.get(3))) {
            this.mDisconnectOperation = true;
            this.mScanForWeakSignal = false;
        }
    }

    private boolean isExplicitNetworkExist() {
        List<WifiConfiguration> networks = this.mWifiConfigManager.getConfiguredNetworks();
        if (!(this.mScanResults == null || networks == null)) {
            for (WifiConfiguration network : networks) {
                if (network.networkId == this.mLastExplicitNetworkId) {
                    for (ScanDetail scanresult : this.mScanResults) {
                        if (network.SSID != null && scanresult.getSSID() != null && network.SSID.equals("\"" + scanresult.getSSID() + "\"") && getSecurity(network) == getSecurity(scanresult.getScanResult())) {
                            Log.d(TAG, "Explicit network " + this.mLastExplicitNetworkId + " exists!");
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        Log.d(TAG, "Explicit network " + this.mLastExplicitNetworkId + " doesn't exist!");
        return false;
    }

    private void disableLastNetwork() {
        Log.d(TAG, "disableLastNetwork, currentState:" + getCurrentState() + ", mLastNetworkId:" + this.mLastNetworkId + ", mLastBssid:" + this.mLastBssid);
        if (getCurrentState() != this.mSupplicantStoppingState && this.mLastNetworkId != -1) {
            this.mWifiConfigManager.disableNetwork(this.mLastNetworkId);
        }
    }

    private void updateAutoConnectSettings() {
        boolean isConnecting = isNetworksDisabledDuringConnect();
        Log.d(TAG, "updateAutoConnectSettings, isConnecting:" + isConnecting);
        List<WifiConfiguration> networks = this.mWifiConfigManager.getConfiguredNetworks();
        if (networks == null) {
            return;
        }
        if (this.mWifiFwkExt.shouldAutoConnect()) {
            if (!isConnecting) {
                Collections.sort(networks, new Comparator<WifiConfiguration>() {
                    public int compare(WifiConfiguration obj1, WifiConfiguration obj2) {
                        return obj2.priority - obj1.priority;
                    }
                });
                List<Integer> disconnectNetworks = this.mWifiConfigManager.getDisconnectNetworks();
                for (WifiConfiguration network : networks) {
                    if (!(network.networkId == this.mLastNetworkId || disconnectNetworks.contains(Integer.valueOf(network.networkId)))) {
                        this.mWifiConfigManager.enableNetwork(network, false, -1);
                    }
                }
            }
        } else if (!isConnecting) {
            for (WifiConfiguration network2 : networks) {
                if (!(network2.networkId == this.mLastNetworkId || network2.status == 1)) {
                    this.mWifiConfigManager.disableNetwork(network2.networkId);
                }
            }
        }
    }

    public int getSecurity(WifiConfiguration config) {
        return this.mWifiFwkExt.getSecurity(config);
    }

    public int getSecurity(ScanResult result) {
        return this.mWifiFwkExt.getSecurity(result);
    }

    public boolean syncGetDisconnectFlag(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(M_CMD_GET_DISCONNECT_FLAG);
        boolean result = ((Boolean) resultMsg.obj).booleanValue();
        Log.d(TAG, "syncGetDisconnectFlag:" + result);
        resultMsg.recycle();
        return result;
    }

    private boolean isUsingDHCP() {
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig != null && currentConfig.getIpAssignment() == IpAssignment.DHCP) {
            synchronized (this.mDhcpResultsLock) {
                if (this.mDhcpResults == null || this.mDhcpResults.ipAddress == null || this.mDhcpResults.ipAddress.getAddress() == null || !(this.mDhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkInternetAccess(final boolean driverRoaming, final int checkSequence) {
        if (this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_CHECK_INTERNET_AFTER_DRIVER_ROAMING", true)) {
            if (this.mAsyncHandler != null) {
                this.mAsyncHandler.post(new Runnable() {
                    /* JADX WARNING: Missing block: B:35:?, code:
            return;
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        HttpURLConnection httpURLConnection = null;
                        try {
                            URL url = new URL("http", "conn1.oppomobile.com", "/generate_204");
                            WifiStateMachine.this.log("checkInternetAccess Checking " + url.toString());
                            httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setInstanceFollowRedirects(false);
                            httpURLConnection.setConnectTimeout(10000);
                            httpURLConnection.setReadTimeout(10000);
                            httpURLConnection.setUseCaches(false);
                            httpURLConnection.getInputStream();
                            int rspCode = httpURLConnection.getResponseCode();
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log("checkInternetAccess: ret=" + rspCode + " headers=" + httpURLConnection.getHeaderFields() + " content=" + httpURLConnection.getContent());
                            }
                            if (rspCode == 204) {
                                WifiStateMachine.this.log("checkInternetAccess has internet access, seq=" + checkSequence);
                                WifiStateMachine.this.mHasInternetAccess = true;
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        } catch (IOException e) {
                            WifiStateMachine.this.log("checkInternetAccess no internet access: exception " + e + " driverRoaming=" + driverRoaming + " checkSequence=" + checkSequence + " current seq=" + WifiStateMachine.this.mCheckInetAccessSeq);
                            if (WifiStateMachine.this.mHasInternetAccess && driverRoaming && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState && WifiStateMachine.this.getNetworkDetailedState() == DetailedState.CONNECTED && checkSequence == WifiStateMachine.this.mCheckInetAccessSeq) {
                                WifiStateMachine.this.log("checkInternetAccess driver roamed and no internet access: exception " + e);
                                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_AUTO_ROAM, WifiStateMachine.this.mLastNetworkId, 0, null);
                            }
                            if (checkSequence == WifiStateMachine.this.mCheckInetAccessSeq) {
                                WifiStateMachine.this.mHasInternetAccess = false;
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        } catch (Throwable th) {
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        }
                    }
                });
            }
            return;
        }
        log("checkInternetAccess feature is disabled");
    }

    private void sendConnectModeChangeBroadcast(boolean bManualConnect) {
        Intent intent = new Intent(CONNECT_MODE_CHANGE_ACTION);
        intent.addFlags(67108864);
        intent.putExtra(EXTRA_CONNECT_MODE, bManualConnect);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void initRomupdateHelperBroadcastReceiver() {
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.initUpdateBroadcastReceiver();
        }
        if (this.mWifiCfgUpdateHelper != null) {
            this.mWifiCfgUpdateHelper.initUpdateBroadcastReceiver();
        }
    }

    private boolean rejectAppScan(int callingUid) {
        if (callingUid < 0 || callingUid == 0 || callingUid == 1000 || callingUid == 1010) {
            if (DBG) {
                log("[FN5] System triger scan. UID = " + callingUid);
            }
            return false;
        }
        String pkgName = this.mContext.getPackageManager().getNameForUid(callingUid);
        if (pkgName == null) {
            if (DBG) {
                log("[FN5] Fail to get package name from UID.");
            }
            return false;
        }
        if (DBG) {
            log("[FN5] " + pkgName + " request trigger scan , UID = " + callingUid);
        }
        if (!this.scanResultsAvailable && this.mScanCount < 3) {
            this.mScanCount++;
            if (DBG) {
                log("[FN5] it is first scan in Connected State.");
            }
            return false;
        } else if (inPkglist(pkgName, "NETWORK_BLACK_LIST", DEFAULT_BLACK_LIST)) {
            if (DBG) {
                log("[FN5] Reject black list app to trigger scan. " + pkgName);
            }
            return true;
        } else if (inPkglist(pkgName, "NETWORK_SYSTEM_APP", DEFAULT_SYSTEM_APP)) {
            if (DBG) {
                log("[FN5] System app triger scan. " + pkgName);
            }
            return false;
        } else {
            if (inPkglist(pkgName, "NETWORK_LOCATION_APP", DEFAULT_LOCATION_APP)) {
                int mode = GpsMonitor.getNavigateMode();
                if (DBG) {
                    log("[FN5] navigation mode is " + mode);
                }
                if (!(2 == mode || -1 == mode)) {
                    if (DBG) {
                        log("[FN5] GPS triger scan in navigation mode. " + pkgName);
                    }
                    return false;
                }
            }
            if (isTopApp(callingUid)) {
                if (DBG) {
                    log("[FN5] Foreground App triger scan. " + pkgName);
                }
                return false;
            }
            if (DBG) {
                loge("[FN5] Reject background App triger scan. " + pkgName);
            }
            return true;
        }
    }

    public boolean inPkglist(String pkgName, String key, String defaultList) {
        if (pkgName == null || key == null) {
            loge("[FN5] key or pkgName is null.");
            return false;
        }
        String value = getRomUpdateValue(key, defaultList);
        if (value == null) {
            loge("[FN5] Fail to getRomUpdateValue. " + key);
            return false;
        }
        if (DBG) {
            log("[FN5] getRomUpdateValue() " + key + " is " + value);
        }
        for (String name : value.split(",")) {
            if (pkgName.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopApp(int callingUid) {
        boolean z = true;
        ComponentName topCpn = null;
        ActivityManager mAms = (ActivityManager) this.mContext.getSystemService("activity");
        if (mAms != null) {
            topCpn = mAms.getTopAppName();
        }
        if (topCpn == null) {
            loge("[FN5] Fail to getTopAppName.");
            return true;
        }
        String topPkg = topCpn.getPackageName();
        try {
            int topUid = this.mContext.getPackageManager().getPackageUid(topPkg, 0);
            if (DBG) {
                log("[FN5] TopAppName is " + topPkg + ", TopUID = " + topUid);
            }
            if (topUid != callingUid) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            loge("[FN5] Fail to get uid of " + topPkg);
            return true;
        }
    }

    private void spoofScanResults(List<ScanResult> scanList) {
        if (DBG) {
            log("[FN5] spoofScanResults() ");
        }
        for (ScanDetail result : this.mScanResults) {
            ScanResult scanResult = new ScanResult(result.getScanResult());
            long timestamp = SystemClock.elapsedRealtime() * 1000;
            scanResult.level += mRandom.nextInt(3) - 1;
            scanResult.timestamp = timestamp;
            scanList.add(scanResult);
        }
    }

    public String getWifiPowerEventCode() {
        StringBuilder eventPower = new StringBuilder();
        float idleTime = ((float) (System.currentTimeMillis() - this.mScreenOffTime)) / 60000.0f;
        if (idleTime == 0.0f) {
            return null;
        }
        float scanFrequency = 0.0f;
        float renewFrequency = 0.0f;
        float keyChangeFrequency = 0.0f;
        float disconnFrequency = 0.0f;
        eventPower.append("event=");
        if (this.mIdleScanTimes != 0) {
            scanFrequency = idleTime / ((float) this.mIdleScanTimes);
        }
        if (this.mIdleRenewTimes != 0) {
            renewFrequency = idleTime / ((float) this.mIdleRenewTimes);
        }
        if (this.mIdleDisConnTimes != 0) {
            disconnFrequency = idleTime / ((float) this.mIdleDisConnTimes);
        }
        if (this.mIdleGroupTimes != 0) {
            keyChangeFrequency = idleTime / ((float) this.mIdleGroupTimes);
        }
        if (scanFrequency < 3.0f && scanFrequency > 0.0f) {
            eventPower.append("SCAN_FREQUENT");
        }
        if (renewFrequency < 40.0f && renewFrequency > 0.0f) {
            eventPower.append("|RENEW_FREQUENT");
        }
        if (keyChangeFrequency < 20.0f && keyChangeFrequency > 0.0f) {
            eventPower.append("|GROUP_FREQUENT");
        }
        if (disconnFrequency < 35.0f && disconnFrequency > 0.0f) {
            eventPower.append("|DISCONN_FREQUENT");
        }
        float powerValue = (((((float) this.mIdleScanTimes) * 0.0f) + (((float) this.mIdleRenewTimes) * WEIGHT_RENEW)) + (((float) this.mIdleDisConnTimes) * 0.0f)) + (((float) this.mIdleGroupTimes) * WEIGHT_GROUP);
        eventPower.append("\n");
        eventPower.append("powerValue=").append(Float.toString(powerValue));
        return eventPower.toString();
    }

    private void detectAndreportOPPOGuard(int uid) {
        ArrayList<String> pskList = new ArrayList();
        ArrayList<Integer> updateUidlist = new ArrayList();
        String pkg = "";
        boolean found = false;
        synchronized (this.mUidList) {
            if (!(uid <= 0 || uid == 1010 || uid == 1000)) {
                this.mAppScanTimes++;
                for (Integer mUid : this.mUidList) {
                    if (mUid.intValue() == uid) {
                        found = true;
                    }
                }
                if (!found) {
                    updateUidlist.add(Integer.valueOf(uid));
                }
            }
            for (Integer mUid2 : updateUidlist) {
                this.mUidList.add(mUid2);
            }
            updateUidlist.clear();
            int AppScanCount = getRomUpdateIntegerValue("POWER_APP_SCAN_COUNT", Integer.valueOf(this.THIRD_APP_SCAN_COUNT)).intValue();
            int AppScanFreq = getRomUpdateIntegerValue("POWER_APP_SCAN_FREQ", Integer.valueOf(this.THIRD_APP_SCAN_FREQ)).intValue();
            if (this.mAppScanTimes > AppScanCount) {
                float deltime = ((float) (System.currentTimeMillis() - this.mScreenOffTime)) / 60000.0f;
                float scanFreq = 0.0f;
                if (deltime != 0.0f) {
                    scanFreq = deltime / ((float) this.mAppScanTimes);
                }
                if (scanFreq <= ((float) AppScanFreq) && scanFreq > 0.0f) {
                    for (Integer mUid22 : this.mUidList) {
                        StringBuilder sbpkg = new StringBuilder();
                        pkg = this.mContext.getPackageManager().getNameForUid(mUid22.intValue());
                        log("mUid= " + mUid22 + "pkg = " + pkg);
                        sbpkg.append("[ ").append(pkg).append(" ] ");
                        pskList.add(sbpkg.toString());
                    }
                }
                boolean IsKillApp = getRomUpdateBooleanValue("POWER_APP_DETECT_AND_KILL", Boolean.valueOf(true)).booleanValue();
                if (pskList.size() > 0) {
                    Intent wifiIntent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                    wifiIntent.putStringArrayListExtra("data", pskList);
                    wifiIntent.putExtra("type", "wifiscan");
                    log("oppo guard kill(" + IsKillApp + ") process");
                    if (IsKillApp) {
                        this.mContext.sendBroadcast(wifiIntent);
                    }
                    this.mAppScanTimes = 0;
                    this.mUidList.clear();
                }
            }
        }
    }

    public void sendWifiNetworkScore(int score, boolean explicitlySelected) {
        logd("sws score= " + score + ",mNetworkAgent =" + this.mNetworkAgent);
        if (this.mNetworkAgent != null) {
            if (!explicitlySelected) {
                this.mNetworkAgent.explicitlySelected(explicitlySelected);
            }
            this.mNetworkAgent.sendNetworkScore(score);
        }
    }

    public void setNetworkStatus(boolean valid) {
        logd("setNetworkStatus " + valid);
        if (valid) {
            doNetworkStatus(1);
        }
    }

    private boolean isWlanAssistantEnable() {
        return this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant") ? Global.getInt(this.mContext.getContentResolver(), WIFI_ASSISTANT_ROMUPDATE, 1) == 1 : false;
    }

    private WifiNetworkStateTraker makeWifiNetworkStateTracker() {
        return new WifiNetworkStateTraker(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler());
    }

    private WifiNetworkAvailable makeWifiNetworkAvailable() {
        return new WifiNetworkAvailable(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker);
    }

    private boolean wifiAssistantForSoftAP() {
        boolean isSoftAP = this.mWifiNetworkStateTraker.isSoftAp(this.mLinkProperties);
        boolean romupdateValue = getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP", Boolean.valueOf(true)).booleanValue();
        if (isSoftAP) {
            return isSoftAP ? romupdateValue : false;
        } else {
            return true;
        }
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public void resetVerbose() {
        if (SystemProperties.get("debug.wifi.prdebug", "1").equals("1")) {
            SystemProperties.set("debug.wifi.prdebug", "0");
            if (this.mFromKeylogVerbose) {
                enableVerboseLogging(0);
            }
        }
    }

    private boolean checkTimeInMorning() {
        int hour = Calendar.getInstance().get(10);
        if (hour >= 6 || hour <= 0) {
            return false;
        }
        return true;
    }

    private void sheduleRestartWifi() {
        sheduleRestartWifi(-1);
    }

    private void sheduleRestartWifi(int netId) {
        if (getRomUpdateIntegerValue("BASIC_FOOL_PROOF_ON", Integer.valueOf(1)).intValue() != 1) {
            log("fool-proof,foolProofOn != 1, don't restart!");
            return;
        }
        setSupplicantRunning(false);
        log("fool-proof,sheduleRestartWifi!");
        sendMessageDelayed(CMD_START_SUPPLICANT, SMART_SCAN_INTERVAL);
        if (netId != -1) {
            sendMessageDelayed(obtainMessage(151553, netId, -1), SMART_SCAN_INTERVAL);
        }
    }

    private void resetRestartAlarm() {
        if (DBG) {
            log("fool-proof,reset alarm count = " + this.resetAlarmCount);
        }
        int i = this.resetAlarmCount;
        this.resetAlarmCount = i + 1;
        if (i >= 3) {
            if (DBG) {
                log("fool-proof,reset alarm next night!");
            }
            this.resetAlarmCount = 0;
            this.mAlarmManager.set(0, caculateTimeIntoMillis(1, getRandomTime(1, 5), getRandomTime(0, 60)), this.mRestartIntent);
            return;
        }
        this.mAlarmManager.set(0, System.currentTimeMillis() + 3600000, this.mRestartIntent);
    }

    private boolean hasNetworkAccessing(RssiPacketCountInfo info, int count) {
        if (DBG) {
            log("fool-proof,hasNetworkAccessing count = " + count + " rssi = " + info.rssi + " txbad = " + info.txbad + " txgood = " + info.txgood + " rxgood = " + info.rxgood);
        }
        if (count != 1) {
            return info.rxgood - this.pktInfo.rxgood >= 80 || info.txgood - this.pktInfo.txgood >= 50;
        } else {
            this.pktInfo = new RssiPacketCountInfo();
            this.pktInfo.txbad = info.txbad;
            this.pktInfo.txgood = info.txgood;
            this.pktInfo.rxgood = info.rxgood;
            return false;
        }
    }

    public void setStatistics(String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap();
        map.put("mapKey-", mapValue);
        log("fool-proof, onCommon eventId = " + eventId);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, map, false);
    }

    public void reportFoolProofException() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support")) {
            log("fool-proof, CTA version don't reportFoolProofException");
            return;
        }
        if (getVerboseLoggingLevel() == 0) {
            enableVerboseLogging(1);
        }
        RuntimeException excp = new RuntimeException("Please send this log to Yuanliu.Tang of wifi team,thank you!");
        excp.fillInStackTrace();
        this.mAssertProxy.requestShowAssertMessage(Log.getStackTraceString(excp));
    }

    public String getCurrentStateName() {
        return getCurrentState().getName();
    }

    private long caculateTimeIntoMillis(int days, int hours, int mins) {
        Calendar Cal = Calendar.getInstance();
        if (days < 0) {
            Cal.add(5, 1);
        } else {
            Cal.add(5, days);
        }
        if (hours < 0 || hours > 6) {
            Cal.set(11, 1);
        } else {
            Cal.set(11, hours);
        }
        Cal.set(12, mins);
        Cal.set(13, 0);
        if (DBG) {
            log("caculateTimeIntoMillis: " + Cal.getTime());
        }
        return Cal.getTimeInMillis();
    }

    private int getRandomTime(int start, int end) {
        if (end <= 0) {
            end = 1;
        }
        if (start >= end) {
            end = start + 1;
        }
        int ret = new Random().nextInt(end - start) + start;
        if (DBG) {
            log("fool-proof, start=" + start + " end=" + end + " random=" + ret);
        }
        return ret;
    }

    private void mtuProber() {
        if (DBG) {
            logd("[N18] Enter mtuProber() ");
        }
        if (this.mWifiRomUpdateHelper == null || this.mAsyncHandler == null) {
            loge("[N18] mWifiRomUpdateHelper or mAsyncHandler is null.");
            return;
        }
        final String[] mtuServer = this.mWifiRomUpdateHelper.getMtuServer();
        if (mtuServer == null || mtuServer.length == 0) {
            loge("[N18] mtuServer is null.");
            return;
        }
        this.mAsyncHandler.post(new Runnable() {
            public void run() {
                int index = WifiStateMachine.mRandom.nextInt(mtuServer.length - 1);
                if (WifiStateMachine.DBG) {
                    WifiStateMachine.this.logd("[N18] try connect to mtu server : " + mtuServer[index] + ", index = " + index);
                }
                WifiStateMachine.this.connectToMtuServer(mtuServer[index]);
            }
        });
    }

    private String buildContent() {
        return BOUNDARY + new String(new char[(getRomUpdateIntegerValue("NETWORK_MTU", Integer.valueOf(1500)).intValue() - 110)]) + "\r\n" + BOUNDARY;
    }

    private void connectToMtuServer(String mtuServer) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL("http://" + mtuServer).openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=-----------hello word-----------\r\n");
            urlConnection.setRequestProperty("Charsert", "UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            OutputStream outStream = urlConnection.getOutputStream();
            outStream.write(buildContent().getBytes("utf-8"));
            outStream.close();
            InputStream inStream = urlConnection.getInputStream();
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    if (DBG) {
                        loge("[N18] ignore mtu excetion");
                        return;
                    }
                    return;
                }
            }
            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e2) {
            if (DBG) {
                loge("[N18] ignore mtu excetion");
            }
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e3) {
                    if (DBG) {
                        loge("[N18] ignore mtu excetion");
                    }
                }
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e4) {
                    if (DBG) {
                        loge("[N18] ignore mtu excetion");
                    }
                }
            }
        }
    }

    public boolean isSupplicantStateDisconnected() {
        if (this.mSupplicantStateTracker != null) {
            String supplicantState = this.mSupplicantStateTracker.getSupplicantStateName();
            if ("DisconnectedState".equals(supplicantState) || "ScanState".equals(supplicantState)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupplicantAvailable() {
        int wifiState = syncGetWifiState();
        if (wifiState == 0 || 1 == wifiState) {
            logd("wifi in disabled or disabling state!");
            return false;
        } else if (3 == getOperationalMode()) {
            logd("wifi in disable pending state!");
            return false;
        } else {
            if (!"SupplicantStoppingState".equalsIgnoreCase(getCurrentStateName())) {
                return true;
            }
            logd("supplicant in stoppong state!");
            return false;
        }
    }

    void checkGbkEncoding(WifiConfiguration config) {
        if (config != null && config.SSID != null) {
            Log.d(TAG, "checkGbkEncoding for " + config.configKey());
            synchronized (this.mScanResultsLock) {
                for (ScanDetail sd : this.mScanResults) {
                    if (sd.getScanResult().wifiSsid.isGBK()) {
                        if (config.SSID.equals("\"" + sd.getScanResult().SSID + "\"") && isSameEncryption(config, sd.getScanResult())) {
                            Log.d(TAG, "found GBK config:" + config.networkId + " " + config.configKey());
                            config.isGbkEncoding = true;
                        }
                    }
                }
            }
        }
    }

    private boolean isSameEncryption(WifiConfiguration config, ScanResult scanResult) {
        String configEncrypt = config.configKey();
        String scanResultEncrypt = scanResult.capabilities;
        if (scanResultEncrypt.contains("WEP") && configEncrypt.contains("WEP")) {
            return true;
        }
        if (scanResultEncrypt.contains("PSK") && configEncrypt.contains("PSK")) {
            return true;
        }
        if (scanResultEncrypt.contains("EAP") && configEncrypt.contains("EAP")) {
            return true;
        }
        if ((this.mWifiConfigManager.isOpenNetwork(scanResultEncrypt) && this.mWifiConfigManager.isOpenNetwork(configEncrypt)) || this.mWifiFwkExt.getSecurity(config) == this.mWifiFwkExt.getSecurity(scanResult)) {
            return true;
        }
        return false;
    }

    boolean isTargetNetworkWapi() {
        WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mTargetNetworkId);
        if (config != null) {
            return config.isWapi();
        }
        return false;
    }

    public boolean checkForExpApauto() {
        sendMessage(CMD_CHECK_FOR_EXPAPAUTO);
        return true;
    }

    private boolean handleCheckForExpApauto() {
        if (this.mWifiInfo == null) {
            Log.e(TAG, "Singtel: mWifiInfo == null!");
            return false;
        }
        int result;
        Log.d(TAG, "Singtel: checkForExpApauto CUR SSID is " + this.mWifiInfo.getSSID() + ",status=" + this.mNetworkInfo.getState());
        WifiConfiguration config_sg = null;
        WifiConfiguration config_wl = null;
        for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
            if (isSingtelWIFI(network.SSID)) {
                config_sg = network;
            }
            if (network.SSID.equals("Wireless@SGx") || network.SSID.equals("\"Wireless@SGx\"")) {
                config_wl = network;
            }
        }
        String defaultSSID = this.SingtelWIFI;
        boolean usewl = false;
        if (!(config_wl == null || config_sg == null)) {
            this.lastRecord = this.mWifiConfigManager.getlastRecord();
            if (this.lastRecord.equalsIgnoreCase("NONE")) {
                if (config_sg.priority >= config_wl.priority) {
                    this.mWifiConfigManager.setlastRecord(this.SingtelWIFI);
                } else {
                    this.mWifiConfigManager.setlastRecord(this.WirelessSGx);
                    result = checkScanResult();
                    Log.d(TAG, "Singtel: check wireless ---- checkscanresult=" + result);
                    if (result == 3) {
                        usewl = true;
                    }
                }
            } else if (checkScanResult() == 3) {
                defaultSSID = this.lastRecord;
            }
        }
        ScanResult destresult = null;
        if (usewl) {
            defaultSSID = this.WirelessSGx;
        }
        if (!(defaultSSID.equals(this.SingtelWIFI) || defaultSSID.equals(this.WirelessSGx))) {
            defaultSSID = this.SingtelWIFI;
        }
        result = checkScanResult();
        if (result == 1) {
            defaultSSID = this.SingtelWIFI;
        } else if (result == 2) {
            defaultSSID = this.WirelessSGx;
        }
        for (ScanDetail scandetail : this.mScanResults) {
            ScanResult scanresult = scandetail.getScanResult();
            if (scanresult.SSID.equals(defaultSSID)) {
                destresult = scanresult;
                break;
            }
        }
        Log.d(TAG, "Singtel: targetSSID=" + defaultSSID + ",result=" + result);
        if (!(destresult == null || this.mNetworkInfo.getState() == NetworkInfo.State.CONNECTED)) {
            Log.d(TAG, "Singtel: -handleCheckForExpApauto-mNetworkInfo.getState()=" + this.mNetworkInfo.getState());
            List<WifiConfiguration> networks = this.mWifiConfigManager.getSavedNetworks();
            if (this.mScanResults == null || networks == null || networks.size() < 1) {
                Log.d(TAG, "Singtel: No scan result or configured networks");
                return false;
            }
            for (WifiConfiguration network2 : networks) {
                if (!network2.SSID.equals("\"" + destresult.SSID + "\"")) {
                    if (network2.SSID.equals(destresult.SSID)) {
                    }
                }
                Log.d(TAG, "Singtel: find singtel ap, connect to " + network2.networkId + " for Singtel requirement!");
                int sim_num = getSimnum();
                Log.d(TAG, "Singtel: connect status--changesim=" + this.changesim + "|currentsim=" + this.currentSim + ",simnum=" + sim_num);
                if (sim_num == 1 || sim_num == 2) {
                    network2.imsi = makeNAI(ColorOSTelephonyManager.getDefault(this.mContext).getSubscriberIdGemini(sim_num - 1), PasspointManagementObjectManager.TAG_SIM);
                    network2.simSlot = addQuote(String.valueOf(sim_num - 1));
                    network2.pcsc = addQuote("rild");
                    sendMessage(obtainMessage(151553, network2.networkId, -1, network2));
                } else if (sim_num != 3) {
                    Log.d(TAG, "Singtel: insert double no singtel sim or have no sim," + sim_num);
                    sendMessage(obtainMessage(151553, network2.networkId, -1));
                } else if (this.changesim) {
                    this.changesim = false;
                    Log.d(TAG, "Singtel: change simcard to connect!");
                    if (this.currentSim == 1) {
                        this.currentSim = 2;
                    } else {
                        this.currentSim = 1;
                    }
                    network2.imsi = makeNAI(ColorOSTelephonyManager.getDefault(this.mContext).getSubscriberIdGemini(this.currentSim - 1), PasspointManagementObjectManager.TAG_SIM);
                    network2.simSlot = addQuote(String.valueOf(this.currentSim - 1));
                    network2.pcsc = addQuote("rild");
                    sendMessage(obtainMessage(151553, network2.networkId, -1, network2));
                } else if (network2.simSlot == null) {
                    this.currentSim = 1;
                    network2.imsi = makeNAI(ColorOSTelephonyManager.getDefault(this.mContext).getSubscriberIdGemini(this.currentSim - 1), PasspointManagementObjectManager.TAG_SIM);
                    network2.simSlot = addQuote(String.valueOf(this.currentSim - 1));
                    network2.pcsc = addQuote("rild");
                    sendMessage(obtainMessage(151553, network2.networkId, -1, network2));
                } else {
                    try {
                        this.currentSim = Integer.parseInt(delQuote(network2.simSlot)) + 1;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "NumberFormatException:" + e.toString());
                    }
                    sendMessage(obtainMessage(151553, network2.networkId, -1));
                }
                return true;
            }
        }
        return false;
    }

    private void updatelastRecord(int netId) {
        this.mWifiConfigManager.updatelastRecord(netId);
    }

    private int checkScanResult() {
        boolean ap_sg = false;
        boolean ap_wl = false;
        for (ScanDetail scandetail : this.mScanResults) {
            ScanResult scanresult = scandetail.getScanResult();
            if (isSingtelWIFI(scanresult.SSID)) {
                ap_sg = true;
            } else if (scanresult.SSID.equals(this.WirelessSGx)) {
                ap_wl = true;
            }
        }
        if (ap_sg && ap_wl) {
            return 3;
        }
        if (!ap_sg && !ap_wl) {
            return 0;
        }
        if (ap_sg) {
            return 1;
        }
        return 2;
    }

    private int checkConfig() {
        int configFlag = 0;
        for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
            if (isSingtelWIFI(network.SSID)) {
                configFlag++;
            }
            if (network.SSID.equals("Wireless@SGx") || network.SSID.equals("\"Wireless@SGx\"")) {
                configFlag += 2;
            }
        }
        return configFlag;
    }

    public boolean isQcomGeminiSupport(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.qualcomm.gemini.support");
    }

    private int getSimnum() {
        ColorOSTelephonyManager mm = ColorOSTelephonyManager.getDefault(this.mContext);
        int sim = 0;
        if (mm.hasIccCardGemini(0)) {
            sim = 1;
        }
        if (mm.hasIccCardGemini(1)) {
            sim += 2;
        }
        if (sim != 3 || !isSingtel()) {
            return sim;
        }
        String operator1 = mm.getSimOperatorGemini(0);
        String operator2 = mm.getSimOperatorGemini(1);
        int sim_sg = 0;
        if (operator1.equals("52501") || operator1.equals("52502") || operator1.equals("52507")) {
            sim_sg = 1;
        }
        if (operator2.equals("52501") || operator2.equals("52502") || operator2.equals("52507")) {
            sim_sg += 2;
        }
        return sim_sg;
    }

    boolean isSingtel() {
        if (SystemProperties.get("persist.sys.oppo.region", "CN").equals("SG") && SystemProperties.get("ro.oppo.operator", "NULL").equals("SINGTEL")) {
            return true;
        }
        return false;
    }

    boolean isSingtelWIFI(int netId) {
        if (netId < 0) {
            return false;
        }
        WifiConfiguration network = this.mWifiConfigManager.getWifiConfiguration(netId);
        if (network != null) {
            return isSingtelWIFI(network.getPrintableSsid());
        }
        return false;
    }

    boolean isSingtelWIFI(String name) {
        if ("Singtel WIFI".equals(name) || "\"Singtel WIFI\"".equals(name)) {
            return true;
        }
        return false;
    }

    public static String addQuote(String s) {
        return "\"" + s + "\"";
    }

    public static String delQuote(String s) {
        if (s != null) {
            return s.replace("\"", "");
        }
        return s;
    }

    public static String makeNAI(String imsi, String eapMethod) {
        if (imsi == null) {
            return addQuote("error");
        }
        StringBuffer NAI = new StringBuffer(40);
        System.out.println("".length());
        if (eapMethod.equals(PasspointManagementObjectManager.TAG_SIM)) {
            NAI.append("1");
        } else if (eapMethod.equals("AKA")) {
            NAI.append("0");
        }
        NAI.append(imsi);
        NAI.append("@wlan.mnc");
        NAI.append("0");
        NAI.append(imsi.substring(3, 5));
        NAI.append(".mcc");
        NAI.append(imsi.substring(0, 3));
        NAI.append(".3gppnetwork.org");
        if (DBG) {
            Log.d(TAG, NAI.toString());
        }
        if (DBG) {
            Log.d(TAG, "\"" + NAI.toString() + "\"");
        }
        return addQuote(NAI.toString());
    }
}
