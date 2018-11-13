package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkAddress;
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
import android.net.TrafficStats;
import android.net.dhcp.DhcpClient;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.WpsResult.Status;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiBackupRestore.SupplicantBackupMigration;
import com.android.server.wifi.WifiMulticastLockManager.FilterController;
import com.android.server.wifi.WifiNative.SignalPollResult;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.WifiNative.VendorHalDeathEventHandler;
import com.android.server.wifi.WifiNative.WifiRssiEventHandler;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import com.android.server.wifi.util.TelephonyUtil.SimAuthResponseData;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.mediatek.server.wifi.WifiApStateMachine;
import com.oppo.oiface.OifaceProxyUtils;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

public class WifiStateMachine extends StateMachine implements WifiRssiEventHandler, FilterController {
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    public static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final int ADD_OR_UPDATE_SOURCE = -3;
    static final int BASE = 131072;
    private static final int BASE_OPPO = 131572;
    private static final String BOUNDARY = "-----------hello word-----------\r\n";
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    static final int CMD_AUTO_JOIN_OPTIMIAZE = 131372;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    static final int CMD_BOOT_COMPLETED = 131206;
    private static final int CMD_CHECK_INTERNET_ACCESS = 131573;
    private static final int CMD_CLIENT_INTERFACE_BINDER_DEATH = 131322;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DELAYED_NETWORK_DISCONNECT = 131159;
    private static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    public static final int CMD_DISABLE_P2P_REQ = 131204;
    public static final int CMD_DISABLE_P2P_RSP = 131205;
    static final int CMD_DISABLE_P2P_WATCHDOG_TIMER = 131184;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_DRIVER_START_TIMED_OUT = 131091;
    static final int CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED = 131239;
    static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_P2P = 131203;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_FIRMWARE_ALERT = 131172;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_CONNECTION_STATISTICS = 131148;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
    static final int CMD_GET_MATCHING_OSU_PROVIDERS = 131181;
    static final int CMD_GET_PASSPOINT_CONFIGS = 131180;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_INITIALIZE = 131207;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    private static final int CMD_IP_REACHABILITY_SESSION_END = 131326;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    private static final int CMD_MTU_PROBER = 131577;
    static final int CMD_NETWORK_STATUS = 131220;
    static final int CMD_NO_NETWORKS_PERIODIC_SCAN = 131160;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_RELOAD_TLS_AND_RECONNECT = 131214;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_NETWORK = 131125;
    static final int CMD_REMOVE_PASSPOINT_CONFIG = 131179;
    static final int CMD_REMOVE_USER_CONFIGURATIONS = 131224;
    static final int CMD_RESET_SIM_NETWORKS = 131173;
    static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    static final int CMD_RSSI_POLL = 131155;
    static final int CMD_RSSI_THRESHOLD_BREACHED = 131236;
    static final int CMD_SAVE_CONFIG = 131130;
    static final int CMD_SCREEN_STATE_CHANGED = 131167;
    private static final int CMD_SELECT_TX_POWER_SCENARIO = 131325;
    static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    static final int CMD_SET_HIGH_PERF_MODE = 131149;
    static final int CMD_SET_OPERATIONAL_MODE = 131144;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_CONNECT = 131215;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_ROAM = 131217;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    static final int CMD_START_SCAN = 131143;
    static final int CMD_START_SUPPLICANT = 131083;
    static final int CMD_STATIC_IP_FAILURE = 131088;
    static final int CMD_STATIC_IP_SUCCESS = 131087;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    static final int CMD_STOP_SUPPLICANT = 131084;
    static final int CMD_SUPPLICANT_STOPPED = 131090;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    private static final int CMD_TRIGGER_RESTORE_DELAY = 131582;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    static final int CMD_USER_STOP = 131279;
    static final int CMD_USER_SWITCH = 131277;
    static final int CMD_USER_UNLOCK = 131278;
    private static final int CMD_VENDOR_HAL_HWBINDER_DEATH = 131323;
    public static final int CONNECT_MODE = 1;
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    private static final String CUSTOMIZED_SCAN_SETTING = "customized_scan_settings";
    private static final String CUSTOMIZED_SCAN_WORKSOURCE = "customized_scan_worksource";
    private static final int DATA_HEAD_LEN = 110;
    public static final String DEBUG_PROPERTY = "persist.sys.assert.panic";
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    private static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    public static final int DISABLED_MODE = 4;
    private static final int DISABLE_INTERFACE = -1;
    static final int DISABLE_P2P_GUARD_TIMER_MSEC = 2000;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    private static final String EXTRA_NETWORK_STATE = "netState";
    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final int FAILURE = -1;
    private static final String GOOGLE_OUI = "DA-A1-19";
    private static final int IDLE_DISCONN_FREQ = 35;
    private static final int IDLE_GROUP_FREQ = 20;
    private static final int IDLE_RENEW_FREQ = 40;
    private static final int IDLE_SCAN_FREQ = 3;
    private static final int INVALID_NETWORK_ID = -1;
    public static final int LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS = 30000;
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    private static final int MESSAGE_HANDLING_STATUS_DEFERRED = -4;
    private static final int MESSAGE_HANDLING_STATUS_DISCARD = -5;
    private static final int MESSAGE_HANDLING_STATUS_FAIL = -2;
    private static final int MESSAGE_HANDLING_STATUS_HANDLING_ERROR = -7;
    private static final int MESSAGE_HANDLING_STATUS_LOOPED = -6;
    private static final int MESSAGE_HANDLING_STATUS_OBSOLETE = -3;
    private static final int MESSAGE_HANDLING_STATUS_OK = 1;
    private static final int MESSAGE_HANDLING_STATUS_PROCESSED = 2;
    private static final int MESSAGE_HANDLING_STATUS_REFUSED = -1;
    private static final int MESSAGE_HANDLING_STATUS_UNKNOWN = 0;
    private static final int M_CMD_SET_POWER_SAVING_MODE = 131581;
    private static final String NETWORKTYPE = "WIFI";
    private static final String NETWORKTYPE_UNTRUSTED = "WIFI_UT";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    public static final short NUM_LOG_RECS_NORMAL = (short) 100;
    public static final short NUM_LOG_RECS_VERBOSE = (short) 3000;
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = (short) 200;
    private static final int ONE_HOUR_MILLI = 3600000;
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    private static final int SCAN_REQUEST_BUFFER_MAX_SIZE = 10;
    private static final String SCAN_REQUEST_TIME = "scan_request_time";
    private static final int SCREEN_TIME = 20000;
    private static final int SIGNAL_HISTORY_COUNTS = 5;
    private static final long SMART_SCAN_INTERVAL = 5000;
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;
    private static final int SUPPLICANT_RESTART_TRIES = 5;
    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    private static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String TAG = "WifiStateMachine";
    private static final int TRIGGER_RESTORE_DELAY_TIME = 2000;
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static final float WEIGHT_DISCONN = 0.6f;
    private static final float WEIGHT_GROUP = 0.3f;
    private static final float WEIGHT_RENEW = 0.5f;
    private static final float WEIGHT_SCAN = 0.28f;
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final boolean WIFI_DBG = SystemProperties.getBoolean("persist.wifi.dbg", false);
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static Random mRandom = new Random();
    private static boolean sManualConnect = false;
    private static final Class[] sMessageClasses = new Class[]{AsyncChannel.class, WifiStateMachine.class, DhcpClient.class};
    private static int sNetId = -1;
    private static int sScanAlarmIntentCount = 0;
    private static final SparseArray<String> sSmToString = MessageUtils.findMessageNames(sMessageClasses);
    private int DISCONN_FLAG = 4096;
    private int GROUP_FLAG = 16;
    private int RENEW_FLAG = 1;
    private int SCAN_FLAG = 256;
    private int THIRD_APP_SCAN_COUNT = 10;
    private int THIRD_APP_SCAN_FREQ = 1;
    private boolean didBlackListBSSID = false;
    int disconnectingWatchdogCount = 0;
    private boolean hasLoadStore = false;
    private long lastConnectAttemptTimestamp = 0;
    private long lastLinkLayerStatsUpdate = 0;
    private long lastOntimeReportTimeStamp = 0;
    private Set<Integer> lastScanFreqs = null;
    private long lastScreenStateChangeTimeStamp = 0;
    private int mAggressiveHandover = 0;
    private int mAlwaysEnableScansWhileAssociated;
    private int mAppScanTimes = 0;
    private OppoAssertTip mAssertProxy = null;
    private Handler mAsyncHandler;
    private boolean mAutoSwitch;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive = false;
    private final Queue<Message> mBufferedScanMsg = new LinkedList();
    private final BuildProperties mBuildProperties;
    private State mCaptiveState = new CaptiveState();
    private int mCheckInetAccessSeq = 0;
    private IClientInterface mClientInterface;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    private int mConnectedId = -1;
    private State mConnectedState = new ConnectedState();
    @GuardedBy("mWifiReqCountLock")
    private int mConnectionReqCount = 0;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    private String mDataInterfaceName;
    private final StateMachineDeathRecipient mDeathRecipient = new StateMachineDeathRecipient(this, CMD_CLIENT_INTERFACE_BINDER_DEATH);
    private State mDefaultState = new DefaultState();
    private final NetworkCapabilities mDfltNetworkCapabilities;
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private long mDiagsConnectionStartMillis = -1;
    int mDisableP2pWatchdogCount = 0;
    private int mDisconnectDelayDuration;
    private boolean mDisconnectOnlyOnInitialIpReachability = false;
    private State mDisconnectedState = new DisconnectedState();
    private long mDisconnectedTimeStamp = 0;
    private State mDisconnectingState = new DisconnectingState();
    private boolean mDriverRoaming = false;
    private boolean mDualSapMode = false;
    private boolean mEnableAutoJoinWhenAssociated;
    private final boolean mEnableChipWakeUpWhenAssociated;
    private final boolean mEnableLinkDebouncing;
    private final boolean mEnableRssiPollWhenAssociated;
    private boolean mEnableRssiPolling = false;
    private final boolean mEnableVoiceCallSarTxPowerLimit;
    private FrameworkFacade mFacade;
    private WifiConfiguration mFilsConfig;
    private State mFilsState = new FilsState();
    private boolean mHasInternetAccess = false;
    private int mIdleDisConnTimes = 0;
    private int mIdleGroupTimes = 0;
    private int mIdleRenewTimes = 0;
    private int mIdleScanTimes = 0;
    private int mIndex = 0;
    private State mInitialState = new InitialState();
    private final String mInterfaceName;
    private IpManager mIpManager;
    private boolean mIpReachabilityDisconnectEnabled = true;
    private boolean mIsAutoRoaming = false;
    private boolean mIsFilsConnection = false;
    private boolean mIsFullScanOngoing = true;
    private boolean mIsIpManagerStarted = false;
    private boolean mIsLinkDebouncing = false;
    private boolean mIsRandomMacCleared = false;
    private boolean mIsRunning = false;
    private boolean mIsScanOngoing = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private String mLastBssid;
    private DetailedState mLastDetailedState = DetailedState.IDLE;
    private long mLastDriverRoamAttempt = 0;
    private int mLastNetworkId;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private long mLastScanTime = 0;
    private long mLastSelectEvtTimeStamp = 0;
    private int mLastSignalLevel = -1;
    private SupplicantState mLastSupplicantState = SupplicantState.DISCONNECTED;
    private LinkProperties mLinkProperties;
    private WifiNetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private boolean mNetworkDetectValid = false;
    private WifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private final int mNoNetworksPeriodicScan;
    private int mNumScanResultsKnown;
    private int mNumScanResultsReturned;
    private INetworkManagementService mNwService;
    private State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    private int mOperationalMode = 1;
    private final OppoWifiConnectionAlert mOppoWifiConnectionAlert;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    private final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    private final WifiPhoneStateListener mPhoneStateListener;
    private volatile int mPollRssiIntervalMsecs = DEFAULT_POLL_RSSI_INTERVAL_MSECS;
    private SupplicantState mPowerState = SupplicantState.UNINITIALIZED;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private boolean mRejectScanWhenRxSensTest = false;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    private State mRoamingState = new RoamingState();
    private int[] mRssiArray = new int[]{0, 0, 0, 0, 0};
    private int mRssiCount = 5;
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    private String mSapInterfaceName = null;
    private State mScanModeState = new ScanModeState();
    private List<ScanDetail> mScanResults = new ArrayList();
    private final Object mScanResultsLock = new Object();
    private long mScreenOffTime = 0;
    private boolean mScreenOn = false;
    private long mScreenOnTime = 0;
    private State mSoftApState = new SoftApState();
    private SoftApStateMachine mSoftApStateMachine = null;
    private boolean mStaAndAPConcurrency = false;
    private boolean mStartApPending = false;
    private int mSupplicantRestartCount = 0;
    private long mSupplicantScanIntervalMs;
    private State mSupplicantStartedState = new SupplicantStartedState();
    private State mSupplicantStartingState = new SupplicantStartingState();
    private SupplicantStateTracker mSupplicantStateTracker;
    private State mSupplicantStoppingState = new SupplicantStoppingState();
    private int mSuspendOptNeedsDisabled = 0;
    private WakeLock mSuspendWakeLock;
    private int mTargetNetworkId = -1;
    private String mTargetRoamBSSID = "any";
    private final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    private boolean mTemporarilyDisconnectWifi = false;
    private final int mThresholdMinimumRssi24;
    private final int mThresholdMinimumRssi5;
    private final int mThresholdQualifiedRssi24;
    private final int mThresholdQualifiedRssi5;
    private final int mThresholdSaturatedRssi24;
    private final int mThresholdSaturatedRssi5;
    private WifiTrafficPoller mTrafficPoller = null;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    ArrayList<Integer> mUidList = new ArrayList();
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    @GuardedBy("mWifiReqCountLock")
    private int mUntrustedReqCount = 0;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private final VendorHalDeathEventHandler mVendorHalDeathRecipient = new -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs(this);
    private boolean mVerboseLoggingEnabled = false;
    private State mWaitForP2pDisableState = new WaitForP2pDisableState();
    private WakeLock mWakeLock;
    private WifiApConfigStore mWifiApConfigStore;
    private final AtomicInteger mWifiApState = new AtomicInteger(11);
    private OppoWifiCfgUpdateHelper mWifiCfgUpdateHelper = null;
    private WifiConfigManager mWifiConfigManager;
    private WifiConnectionStatistics mWifiConnectionStatistics = new WifiConnectionStatistics();
    private WifiConnectivityManager mWifiConnectivityManager;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private final WifiInfo mWifiInfo;
    private WifiInjector mWifiInjector;
    private int mWifiLinkLayerStatsSupported = 4;
    private WifiMetrics mWifiMetrics;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private OppoWifiNetworkSwitchEnhance mWifiNetworkAvailable;
    private OppoWifiAssistantStateTraker mWifiNetworkStateTraker;
    private AsyncChannel mWifiP2pChannel;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private final Object mWifiReqCountLock = new Object();
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private WifiScanner mWifiScanner;
    private final WifiScoreReport mWifiScoreReport;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    private WifiStateTracker mWifiStateTracker;
    private State mWpsRunningState = new WpsRunningState();
    private final WrongPasswordNotifier mWrongPasswordNotifier;
    private final String mapKey = "mapKey-";
    private int messageHandlingStatus = 0;
    private int originRssi = WifiMetrics.MIN_RSSI_DELTA;
    int roamWatchdogCount = 0;
    private String sLastConfigKey = null;
    private boolean staCleanUpDone = false;
    private WifiConfiguration targetWificonfiguration = null;
    private boolean testNetworkDisconnect = false;
    private int testNetworkDisconnectCounter = 0;
    private WifiP2pServiceImpl wifiP2pServiceImpl;

    class CaptiveState extends State {
        CaptiveState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
            WifiStateMachine.this.clearTargetBssid("CaptiveState");
        }

        public boolean processMessage(Message message) {
            if (message == null) {
                WifiStateMachine.this.logd("message is null,ignore!!");
                return true;
            }
            boolean ignoreDisconnect;
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
                        WifiStateMachine.this.replyToMessage(message, message.what, 1);
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(enableNetId, message.sendingUid);
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(enableNetId);
                        }
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    return false;
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                    }
                    return false;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                    int startConId = message.arg1;
                    ignoreDisconnect = false;
                    WifiStateMachine.this.logd("startConId=" + startConId + ", getid= " + WifiStateMachine.this.mWifiInfo.getNetworkId());
                    if (WifiStateMachine.this.mWifiInfo.getNetworkId() == startConId) {
                        ignoreDisconnect = true;
                    }
                    if (ignoreDisconnect) {
                        WifiStateMachine.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(startConId, message.sendingUid);
                            OppoManuConnectManager.getInstance().sendConnectModeChangeBroadcast(true);
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(startConId);
                        }
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    return false;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.updateNetworkConnectResult(WifiStateMachine.this.mLastNetworkId, false);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    if (config != null) {
                        WifiStateMachine.this.setTargetBssid(config, bssid);
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        WifiStateMachine.this.logd("CMD_START_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config =" + (config == null ? "null " : config.configKey()) + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                        WifiStateMachine.this.reportConnectionAttemptStart(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                        int netIdFromWifiInfo = -1;
                        if (WifiStateMachine.this.mWifiInfo != null) {
                            netIdFromWifiInfo = WifiStateMachine.this.mWifiInfo.getNetworkId();
                        }
                        if (!WifiStateMachine.this.mIsLinkDebouncing || netIdFromWifiInfo == -1 || netIdFromWifiInfo == WifiStateMachine.this.mLastNetworkId) {
                            if (!WifiStateMachine.this.mWifiNative.roamToNetwork(config)) {
                                WifiStateMachine.this.loge("CMD_START_ROAM Failed to start roaming to network " + config);
                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                WifiStateMachine.this.messageHandlingStatus = -2;
                                break;
                            }
                            WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiStateMachine.this.targetWificonfiguration = config;
                            WifiStateMachine.this.mIsAutoRoaming = true;
                            WifiStateMachine.this.mWifiMetrics.logStaEvent(12, config);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                            break;
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        WifiStateMachine.this.mWifiNative.reassociate();
                        break;
                    }
                    WifiStateMachine.this.loge("CMD_START_ROAM and no config, bail out...");
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
                        lastRoam = WifiStateMachine.this.mClock.getWallClockMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (!WifiStateMachine.this.mEnableLinkDebouncing || !WifiStateMachine.this.mScreenOn || (WifiStateMachine.this.isLinkDebouncing() ^ 1) == 0 || config == null || !config.getNetworkSelectionStatus().isNetworkEnabled() || config.networkId == WifiStateMachine.this.mWifiConfigManager.getLastSelectedNetwork() || ((message.arg2 == 3 && (lastRoam <= 0 || lastRoam >= 2000)) || ((!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5) && (!ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5)))) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            String str;
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=").append(WifiStateMachine.this.mWifiInfo.getBSSID()).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.isLinkDebouncing()).append(" reason=").append(message.arg2).append(" Network Selection Status=");
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
                    if (WifiStateMachine.this.mWifiNetworkStateTraker == null || !(WifiStateMachine.this.mWifiNetworkStateTraker == null || (WifiStateMachine.this.mAutoSwitch ^ 1) == 0)) {
                        WifiStateMachine.this.mIsLinkDebouncing = true;
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC", Integer.valueOf(WifiStateMachine.LINK_FLAPPING_DEBOUNCE_MSEC)).intValue());
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in connected state BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " RSSI=" + WifiStateMachine.this.mWifiInfo.getRssi() + " freq=" + WifiStateMachine.this.mWifiInfo.getFrequency() + " reason=" + message.arg2 + " -> debounce");
                    }
                    return true;
                    break;
                case 151553:
                    int conId = message.arg1;
                    ignoreDisconnect = false;
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
                        savedConf = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(conConfig.configKey());
                    }
                    if (!(savedConf == null || WifiStateMachine.this.mWifiInfo == null || savedConf.networkId != WifiStateMachine.this.mWifiInfo.getNetworkId())) {
                        ignoreDisconnect = true;
                    }
                    if (ignoreDisconnect) {
                        WifiStateMachine.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        WifiStateMachine.this.replyToMessage(message, message.what, 1);
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(conId, message.sendingUid);
                            OppoManuConnectManager.getInstance().sendConnectModeChangeBroadcast(true);
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(conId);
                        }
                        WifiStateMachine.this.sendConnectedState();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    return false;
                default:
                    return false;
            }
            return true;
        }
    }

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                WifiStateMachine.this.loge("Failed to remove networks on entering connect mode");
            }
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            WifiStateMachine.this.setWifiState(3);
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(true);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
            WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
        }

        public void exit() {
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(1);
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                WifiStateMachine.this.loge("Failed to remove networks on exiting connect mode");
            }
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
        }

        /* JADX WARNING: Missing block: B:463:0x1a59, code:
            if (com.android.server.wifi.WifiStateMachine.-wrap11(r63.this$0, com.android.server.wifi.WifiStateMachine.-get41(r63.this$0), r17) != false) goto L_0x1a5b;
     */
        /* JADX WARNING: Missing block: B:472:0x1afb, code:
            if (com.android.server.wifi.WifiStateMachine.-wrap11(r63.this$0, com.android.server.wifi.WifiStateMachine.-get41(r63.this$0), r17) != false) goto L_0x1afd;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            int netId;
            Set<Integer> removedNetworkIds;
            WifiConfiguration config;
            String fqdn;
            String bssid;
            ScanDetailCache scanDetailCache;
            int reasonCode;
            WifiConnectionStatistics -get92;
            NetworkUpdateResult result;
            switch (message.what) {
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    if (!WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, false)) {
                        WifiStateMachine.this.messageHandlingStatus = -2;
                        break;
                    }
                    netId = message.arg1;
                    if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    boolean ok;
                    int i;
                    boolean disableOthers = message.arg2 == 1;
                    netId = message.arg1;
                    if (disableOthers) {
                        ok = WifiStateMachine.this.connectToUserSelectNetwork(netId, message.sendingUid, false);
                    } else {
                        ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(netId, false, message.sendingUid);
                    }
                    if (ok && message.arg2 == 1 && OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().handleThirdAPKConnect(message.arg1, message.sendingUid);
                        OppoManuConnectManager.getInstance().setManuConnectBssid(netId, null);
                    }
                    if (ok && message.arg2 == 1 && OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().sendThirdAPKConnectEvt(message.arg1);
                    }
                    if (ok) {
                        if (message.arg2 == 1) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.logd("select network :" + message.arg1 + " from third app");
                            }
                            WifiStateMachine.this.mOppoWifiConnectionAlert.sendEnableNetworkEvent(message.arg1);
                        }
                        if (message.arg2 == 1 && WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiStateMachine.CMD_ENABLE_NETWORK, message.arg1);
                        }
                    } else {
                        WifiStateMachine.this.messageHandlingStatus = -2;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    int i2 = message.what;
                    if (ok) {
                        i = 1;
                    } else {
                        i = -1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, WifiStateMachine.CMD_SAVE_CONFIG, WifiStateMachine.this.mWifiConfigManager.saveToStore(true) ? 1 : -1);
                    WifiStateMachine.this.mBackupManagerProxy.notifyDataChanged();
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan(message.obj);
                    break;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.mWifiNative.reassociate();
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                    config = WifiStateMachine.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null && (config.networkId == WifiStateMachine.this.mTargetNetworkId || config.networkId == WifiStateMachine.this.mLastNetworkId)) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mPasspointManager.getMatchingWifiConfig((ScanResult) message.obj));
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                    WifiStateMachine.this.mPasspointManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString(WifiStateMachine.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, 0);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    PasspointConfiguration passpointConfig = message.obj;
                    if (!WifiStateMachine.this.mPasspointManager.addOrUpdateProvider(passpointConfig, message.arg1)) {
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    fqdn = passpointConfig.getHomeSp().getFqdn();
                    if (!WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn)) {
                        break;
                    }
                    WifiStateMachine.this.logd("Disconnect from current network since its provider is updated");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    WifiStateMachine.this.replyToMessage(message, message.what, 1);
                    break;
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    fqdn = (String) message.obj;
                    if (!WifiStateMachine.this.mPasspointManager.removeProvider(fqdn)) {
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    if (!WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn)) {
                        break;
                    }
                    WifiStateMachine.this.logd("Disconnect from current network since its provider is removed");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    WifiStateMachine.this.replyToMessage(message, message.what, 1);
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_OSU_PROVIDERS /*131181*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mPasspointManager.getMatchingOsuProviders((ScanResult) message.obj));
                    break;
                case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                    break;
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                    if (WifiStateMachine.this.mWifiConfigManager.needsUnlockedKeyStore()) {
                        WifiStateMachine.this.logd("Reconnecting to give a chance to un-connected TLS networks");
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                        WifiStateMachine.this.mWifiNative.reconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                    WifiStateMachine.this.mIsFilsConnection = false;
                    netId = message.arg1;
                    int uid = message.arg2;
                    bssid = (String) message.obj;
                    config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    WifiStateMachine.this.logd("CMD_START_CONNECT sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " roam=" + Boolean.toString(WifiStateMachine.this.mIsAutoRoaming));
                    if (config != null) {
                        WifiStateMachine.this.convertToQuotedSSID(config);
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        String tmpBssid = WifiStateMachine.this.getBestBssidForNetId(netId);
                        Boolean bssidTmpReset = Boolean.valueOf(false);
                        if (!(config == null || !"any".equals(bssid) || tmpBssid == null)) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.logd("config bssid = " + config.BSSID);
                            }
                            if (config.BSSID == null || "any".equals(config.BSSID)) {
                                if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                    WifiStateMachine.this.logd("reset config bssid to " + tmpBssid + "  temp");
                                }
                                config.BSSID = tmpBssid;
                                bssidTmpReset = Boolean.valueOf(true);
                            }
                        }
                        if (bssidTmpReset.booleanValue()) {
                            WifiStateMachine.this.setTargetBssid(config, tmpBssid);
                        } else {
                            WifiStateMachine.this.setTargetBssid(config, bssid);
                        }
                        if (!config.allowedKeyManagement.get(8) && !config.allowedKeyManagement.get(9)) {
                            WifiStateMachine.this.reportConnectionAttemptStart(config, WifiStateMachine.this.mTargetRoamBSSID, 5);
                            if (!WifiStateMachine.this.mWifiNative.connectToNetwork(config)) {
                                WifiStateMachine.this.loge("CMD_START_CONNECT Failed to start connection to network " + config);
                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                break;
                            }
                            if (!WifiStateMachine.this.isThirdApp(uid)) {
                                WifiStateMachine.this.clearTargetBssid("AfterConnect");
                            }
                            WifiStateMachine.this.mWifiMetrics.logStaEvent(11, config);
                            WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiStateMachine.this.targetWificonfiguration = config;
                            WifiStateMachine.this.mIsAutoRoaming = false;
                            if (!WifiStateMachine.this.isLinkDebouncing()) {
                                if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                                    break;
                                }
                            }
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                            break;
                        }
                        WifiStateMachine.this.mFilsConfig = config;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mFilsState);
                        break;
                    }
                    WifiStateMachine.this.loge("CMD_START_CONNECT and no config, bail out...");
                    break;
                    break;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    return true;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    String someBssid = message.obj;
                    if (someBssid != null) {
                        scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(WifiStateMachine.this.mTargetNetworkId);
                        if (scanDetailCache != null) {
                            WifiStateMachine.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.removeNetworksForUser(Integer.valueOf(message.arg1).intValue());
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_TRIGGER_RESTORE_DELAY /*131582*/:
                    int restoreNetid = message.arg1;
                    if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mConnectedState && WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mObtainingIpState) {
                        WifiStateMachine.this.reportFoolProofException();
                        WifiStateMachine.this.setStatistics("state_inconsistent", "wifi_state_inconsistent_cant_connect");
                        if (!WifiStateMachine.this.mWifiNative.disconnect()) {
                            WifiStateMachine.this.loge("fool-proof,Disconnect cmd reject by wpa,so restart");
                            WifiStateMachine.this.sheduleRestartWifi(restoreNetid);
                            break;
                        }
                    }
                    WifiStateMachine.this.loge("fool-proof, already connected,ignore");
                    break;
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mWifiNative.reconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = false;
                        break;
                    }
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection established");
                    }
                    if (WifiStateMachine.this.hasConfigKeyChanged(message.arg1)) {
                        DhcpClient.clearOffer();
                    }
                    WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    WifiStateMachine.this.mWifiConfigManager.clearRecentFailureReason(WifiStateMachine.this.mLastNetworkId);
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    reasonCode = message.arg2;
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, WifiStateMachine.this.mLastNetworkId, message.arg2, WifiStateMachine.this.mLastBssid);
                    }
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection established mLastNetworkId " + WifiStateMachine.this.mLastNetworkId + "sManualConnect" + WifiStateMachine.sManualConnect + "sNetId " + WifiStateMachine.sNetId);
                    }
                    WifiConfiguration current = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mLastNetworkId);
                    if (WifiStateMachine.this.mLastNetworkId == WifiStateMachine.this.getManuConnectNetId() && WifiStateMachine.this.isManuConnect() && current != null) {
                        WifiStateMachine.this.log("Network connection established current.ssid" + current.SSID);
                        current.BSSID = WifiStateMachine.this.mLastBssid;
                        WifiStateMachine.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(WifiStateMachine.this.mLastNetworkId, current, -1, SupplicantState.COMPLETED);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config == null) {
                        WifiStateMachine.this.logw("Connected to unknown networkId " + WifiStateMachine.this.mLastNetworkId + ", disconnecting...");
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
                    if (!(scanDetailCache == null || WifiStateMachine.this.mLastBssid == null)) {
                        ScanResult scanResult = scanDetailCache.getScanResult(WifiStateMachine.this.mLastBssid);
                        if (scanResult != null) {
                            WifiStateMachine.this.mWifiInfo.setFrequency(scanResult.frequency);
                        }
                    }
                    WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, reasonCode);
                    if (config.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                        String anonymousIdentity = WifiStateMachine.this.mWifiNative.getEapAnonymousIdentity();
                        if (anonymousIdentity != null) {
                            config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                        } else {
                            Log.d(WifiStateMachine.TAG, "Failed to get updated anonymous identity from supplicant, reset it in WifiConfiguration.");
                            config.enterpriseConfig.setAnonymousIdentity(null);
                        }
                        WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, 1010);
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    boolean mConnectionInProgress;
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("ConnectModeState: Network connection lost ");
                    }
                    ScanResult scanRes = WifiStateMachine.this.getScanResultForBssid((String) message.obj);
                    if (WifiStateMachine.this.targetWificonfiguration == null || scanRes == null) {
                        mConnectionInProgress = false;
                    } else {
                        mConnectionInProgress = WifiStateMachine.this.targetWificonfiguration.SSID.equals("\"" + scanRes.SSID + "\"") ^ 1;
                    }
                    WifiStateMachine.this.handleNetworkDisconnect(mConnectionInProgress);
                    if (!(WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mFilsState && (mConnectionInProgress ^ 1) == 0)) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (message.obj != null) {
                        SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                        if (!SupplicantState.isDriverActive(state)) {
                            if (WifiStateMachine.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                                WifiStateMachine.this.handleNetworkDisconnect();
                            }
                            WifiStateMachine.this.log("Detected an interface down, restart driver");
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_SUPPLICANT);
                            break;
                        }
                        if (!(WifiStateMachine.this.isLinkDebouncing() || state != SupplicantState.DISCONNECTED || WifiStateMachine.this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED)) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                            }
                            WifiStateMachine.this.handleNetworkDisconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        }
                        if (state == SupplicantState.COMPLETED) {
                            WifiStateMachine.this.mIpManager.confirmConfiguration();
                            break;
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    if (message.arg2 == 2) {
                        WifiStateMachine.this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.AUTH_PASSWORD_WRONG"), UserHandle.ALL);
                    }
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int disableReason = 3;
                    if (WifiStateMachine.this.isPermanentWrongPasswordFailure(WifiStateMachine.this.mTargetNetworkId, message.arg2)) {
                        disableReason = 12;
                        WifiConfiguration targetedNetwork = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mTargetNetworkId);
                        WifiStateMachine.this.mOppoWifiConnectionAlert.sendWrongKeyEvent();
                    } else {
                        WifiStateMachine.this.mOppoWifiConnectionAlert.sendAuthFailedEvent(message);
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                        }
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, disableReason);
                    WifiStateMachine.this.mWifiConfigManager.clearRecentFailureReason(WifiStateMachine.this.mTargetNetworkId);
                    WifiStateMachine.this.reportConnectionAttemptEnd(3, 1);
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 2);
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().switchConfigurationSimSlot(WifiStateMachine.this.mTargetNetworkId);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                    int supplicantNetworkId = message.arg2;
                    netId = WifiStateMachine.this.lookupFrameworkNetworkId(supplicantNetworkId);
                    boolean identitySent = false;
                    if (WifiStateMachine.this.targetWificonfiguration != null && WifiStateMachine.this.targetWificonfiguration.networkId == netId && TelephonyUtil.isSimConfig(WifiStateMachine.this.targetWificonfiguration)) {
                        String identity = TelephonyUtil.getSimIdentity(WifiStateMachine.this.getTelephonyManager(), WifiStateMachine.this.targetWificonfiguration);
                        if (identity != null) {
                            WifiStateMachine.this.mWifiNative.simIdentityResponse(supplicantNetworkId, identity);
                            identitySent = true;
                        } else {
                            Log.e(WifiStateMachine.TAG, "Unable to retrieve identity from Telephony");
                        }
                    }
                    if (!identitySent) {
                        String ssid = message.obj;
                        String quotedSsid = WifiStateMachine.this.convertToQuotedSSID(ssid);
                        if (!(WifiStateMachine.this.targetWificonfiguration == null || ssid == null || WifiStateMachine.this.targetWificonfiguration.SSID == null || !WifiStateMachine.this.targetWificonfiguration.SSID.equals(quotedSsid))) {
                            WifiStateMachine.this.mOppoWifiConnectionAlert.sendSupRequestIdentityEvent(WifiStateMachine.this.targetWificonfiguration.networkId);
                            WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 8);
                        }
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                    WifiStateMachine.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    SimAuthRequestData requestData = message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                WifiStateMachine.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        }
                        WifiStateMachine.this.handleGsmAuthRequest(requestData);
                        break;
                    }
                    WifiStateMachine.this.loge("Invalid sim auth request");
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(1);
                    WifiStateMachine.this.didBlackListBSSID = false;
                    bssid = message.obj;
                    boolean timedOut = message.arg1 > 0;
                    reasonCode = message.arg2;
                    Log.d(WifiStateMachine.TAG, "Assocation Rejection event: bssid=" + bssid + " reason code=" + reasonCode + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        bssid = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    if (bssid != null) {
                        WifiStateMachine.this.didBlackListBSSID = WifiStateMachine.this.mWifiConnectivityManager.trackBssid(bssid, false, reasonCode);
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 2);
                    WifiStateMachine.this.mWifiConfigManager.setRecentFailureAssociationStatus(WifiStateMachine.this.mTargetNetworkId, reasonCode);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    WifiStateMachine.this.mOppoWifiConnectionAlert.sendAssociationRejectionEvent(message);
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT, message.arg1, message.arg2, message.obj);
                    }
                    if (WifiStateMachine.this.mScreenOn) {
                        WifiStateMachine.this.startScan(-1, 0, null, null);
                    }
                    WifiStateMachine.this.reportConnectionAttemptEnd(2, 1);
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), bssid, 1);
                    break;
                case WifiMonitor.SELECT_NETWORK_EVENT /*147649*/:
                    WifiStateMachine.this.mOppoWifiConnectionAlert.sendSelectNetworkEvent(message);
                    break;
                case WifiMonitor.SAVE_CONFIG_FAILED_EVENT /*147650*/:
                    WifiStateMachine.this.mOppoWifiConnectionAlert.sendSaveConfigFailed(message.arg1);
                    break;
                case WifiMonitor.SSID_TEMP_DISABLED /*147651*/:
                    int reason = 3;
                    String msgStr = message.obj;
                    if (msgStr != null) {
                        if (msgStr.contains("WRONG_KEY")) {
                            reason = 12;
                        } else if (msgStr.contains("DHCP FAILURE")) {
                            reason = 4;
                        } else if (msgStr.contains("CONN_FAILED")) {
                            reason = 2;
                        } else {
                            reason = 3;
                            if (msgStr.contains("AUTH_FAILED")) {
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(message.arg1, 3);
                            }
                        }
                    }
                    WifiStateMachine.this.handleSSIDStateChangedCB(message.arg1, reason);
                    break;
                case 151553:
                    netId = message.arg1;
                    config = (WifiConfiguration) message.obj;
                    WifiStateMachine.this.checkAndSetSsidForConfig(config);
                    -get92 = WifiStateMachine.this.mWifiConnectionStatistics;
                    -get92.numWifiManagerJoinAttempt++;
                    WifiStateMachine.this.convertToQuotedSSID(config);
                    if (netId == -1 && config != null && config.hiddenSSID) {
                        WifiStateMachine.this.mOppoWifiConnectionAlert.setAddAndConnectHiddenAp(true);
                    } else {
                        WifiStateMachine.this.mOppoWifiConnectionAlert.setAddAndConnectHiddenAp(false);
                    }
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().setManuConnectBssid(netId, config);
                    }
                    WifiStateMachine.this.mWifiConfigManager.setIsNewNetwork(false);
                    boolean hasCredentialChanged = false;
                    if (config != null) {
                        WifiConfiguration currConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                        WifiConfiguration saveConfig = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(config.configKey(true));
                        if (saveConfig != null && config.hiddenSSID && currConfig != null && WifiStateMachine.this.mLastNetworkId == saveConfig.networkId && "ConnectedState".equalsIgnoreCase(WifiStateMachine.this.getCurrentState().getName())) {
                            config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(saveConfig.networkId);
                        }
                        if (netId != -1 || config == null || config.networkId == -1) {
                            WifiStateMachine.this.mOppoWifiConnectionAlert.setConnectAlreadyExistConfigByAdd(false);
                        } else {
                            WifiStateMachine.this.mOppoWifiConnectionAlert.setConnectAlreadyExistConfigByAdd(true);
                        }
                        int sendingUid = -1;
                        String backupPktName = "com.coloros.backuprestore";
                        String sendingPktName = null;
                        PackageManager pm = null;
                        if (!(message == null || WifiStateMachine.this.mContext == null)) {
                            sendingUid = message.sendingUid;
                            pm = WifiStateMachine.this.mContext.getPackageManager();
                        }
                        if (pm != null) {
                            sendingPktName = pm.getNameForUid(sendingUid);
                        }
                        if (!backupPktName.equals(sendingPktName)) {
                            if (WifiStateMachine.this.mOppoWifiConnectionAlert.needSaveAsHiddenAP((WifiConfiguration) message.obj)) {
                                config.hiddenSSID = true;
                            }
                        }
                        result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                        if (!result.isSuccess()) {
                            WifiStateMachine.this.loge("CONNECT_NETWORK adding/updating config=" + config + " failed");
                            WifiStateMachine.this.messageHandlingStatus = -2;
                            WifiStateMachine.this.replyToMessage(message, 151554, 0);
                            break;
                        }
                        netId = result.getNetworkId();
                        hasCredentialChanged = result.hasCredentialChanged();
                    }
                    if ("CompletedState".equalsIgnoreCase(WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName()) && "DisconnectedState".equalsIgnoreCase(WifiStateMachine.this.getCurrentState().getName())) {
                        boolean mMsgPending = WifiStateMachine.this.mSupplicantStateTracker.getHandler().hasMessages(WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT);
                        if (System.currentTimeMillis() - WifiStateMachine.this.mLastSelectEvtTimeStamp < WifiStateMachine.SMART_SCAN_INTERVAL) {
                            Log.d(WifiStateMachine.TAG, "fool-proof,Supplicant state goes wrong now,but connect too frequence!");
                        } else if (mMsgPending) {
                            Log.d(WifiStateMachine.TAG, "fool-proof,Supplicant state goes wrong now,but still has unhandled supplicant msg!!");
                        } else {
                            WifiStateMachine.this.loge("fool-proof,Supplicant state goes wrong!");
                            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TRIGGER_RESTORE_DELAY, netId, 0), 2000);
                        }
                    }
                    WifiStateMachine.this.mLastSelectEvtTimeStamp = System.currentTimeMillis();
                    if (!WifiStateMachine.this.connectToUserSelectNetwork(netId, message.sendingUid, hasCredentialChanged)) {
                        WifiStateMachine.this.messageHandlingStatus = -2;
                        WifiStateMachine.this.replyToMessage(message, 151554, 9);
                        break;
                    }
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().handleManuConnect(netId, message.sendingUid);
                    }
                    WifiStateMachine.this.mOppoWifiConnectionAlert.sendConnectNetworkEvent(netId);
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        boolean isSaveConfig = message.arg1 != -1;
                        int newConfig = isSaveConfig ? 0 : 1;
                        WifiStateMachine.this.mWifiNetworkStateTraker.setManualConnTime(System.currentTimeMillis(), isSaveConfig, config);
                        WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(151553, netId, newConfig);
                    }
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().sendManuConnectEvt(netId);
                    }
                    if (WifiStateMachine.this.mConnectedState == WifiStateMachine.this.getCurrentState() && WifiStateMachine.this.getCurrentWifiConfiguration() != null && WifiStateMachine.this.getCurrentWifiConfiguration().networkId == netId && OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(true, netId);
                    }
                    if (WifiStateMachine.this.mConnectedState == WifiStateMachine.this.getCurrentState() && WifiStateMachine.this.getCurrentWifiConfiguration() != null && WifiStateMachine.this.getCurrentWifiConfiguration().networkId == netId && WifiStateMachine.this.mOppoWifiConnectionAlert != null) {
                        WifiStateMachine.this.mOppoWifiConnectionAlert.setManuConnect(false);
                    }
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(13, config);
                    WifiStateMachine.this.broadcastWifiCredentialChanged(0, config);
                    WifiStateMachine.this.replyToMessage(message, 151555);
                    break;
                    break;
                case 151556:
                    if (WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, true)) {
                        if (WifiStateMachine.this.mOppoWifiConnectionAlert != null && WifiStateMachine.this.mOppoWifiConnectionAlert.isManuConnect() && WifiStateMachine.this.mOppoWifiConnectionAlert.getManuConnectNetId() == message.arg1 && message.arg1 != -1) {
                            WifiStateMachine.this.mOppoWifiConnectionAlert.setManuConnect(false);
                        }
                        netId = message.arg1;
                        if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            break;
                        }
                    }
                    break;
                case 151559:
                    config = (WifiConfiguration) message.obj;
                    if (config != null && config.SSID == null && config.networkId != -1 && WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(config.networkId) == null) {
                        WifiStateMachine.this.logd("break because ssid is null,but cant find saved ap by netid");
                        break;
                    }
                    result = WifiStateMachine.this.saveNetworkConfigAndSendReply(message);
                    netId = result.getNetworkId();
                    if (result.isSuccess() && WifiStateMachine.this.mWifiInfo.getNetworkId() == netId) {
                        -get92 = WifiStateMachine.this.mWifiConnectionStatistics;
                        -get92.numWifiManagerJoinAttempt++;
                        if (!result.hasCredentialChanged()) {
                            if (result.hasProxyChanged()) {
                                WifiStateMachine.this.log("Reconfiguring proxy on connection");
                                WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.getCurrentWifiConfiguration().getHttpProxy());
                            }
                            if (result.hasIpChanged()) {
                                WifiStateMachine.this.log("Reconfiguring IP on connection");
                                WifiStateMachine.this.mWifiNative.disconnect();
                                WifiStateMachine.this.handleNetworkDisconnect();
                                WifiStateMachine.this.startConnectToNetwork(netId, message.sendingUid, "any");
                                break;
                            }
                        }
                        WifiStateMachine.this.logi("SAVE_NETWORK credential changed for config=" + message.obj.configKey() + ", Reconnecting.");
                        WifiStateMachine.this.startConnectToNetwork(netId, message.sendingUid, "any");
                        break;
                    }
                    break;
                case 151562:
                    WpsInfo wpsInfo = message.obj;
                    if (wpsInfo != null) {
                        WpsResult wpsResult = new WpsResult();
                        if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                            WifiStateMachine.this.loge("Failed to remove networks before WPS");
                        }
                        switch (wpsInfo.setup) {
                            case 0:
                                WifiStateMachine.this.clearRandomMacOui();
                                WifiStateMachine.this.mIsRandomMacCleared = true;
                                if (!WifiStateMachine.this.mWifiNative.startWpsPbc(wpsInfo.BSSID)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = Status.FAILURE;
                                    break;
                                }
                                wpsResult.status = Status.SUCCESS;
                                break;
                            case 1:
                                wpsResult.pin = WifiStateMachine.this.mWifiNative.startWpsPinDisplay(wpsInfo.BSSID);
                                if (!TextUtils.isEmpty(wpsResult.pin)) {
                                    wpsResult.status = Status.SUCCESS;
                                    break;
                                }
                                Log.e(WifiStateMachine.TAG, "Failed to start WPS pin method configuration");
                                wpsResult.status = Status.FAILURE;
                                break;
                            case 2:
                                if (!WifiStateMachine.this.mWifiNative.startWpsRegistrar(wpsInfo.BSSID, wpsInfo.pin)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = Status.FAILURE;
                                    break;
                                }
                                wpsResult.status = Status.SUCCESS;
                                break;
                            default:
                                wpsResult = new WpsResult(Status.FAILURE);
                                WifiStateMachine.this.loge("Invalid setup for WPS");
                                break;
                        }
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleWpsConnect(message.sendingUid);
                        }
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(151553, -1, 0);
                        }
                        if (wpsResult.status != Status.SUCCESS) {
                            WifiStateMachine.this.loge("Failed to start WPS with config " + wpsInfo.toString());
                            WifiStateMachine.this.replyToMessage(message, 151564, 0);
                            if (OppoManuConnectManager.getInstance() != null) {
                                OppoManuConnectManager.getInstance().reset();
                                break;
                            }
                        }
                        WifiStateMachine.this.replyToMessage(message, 151563, (Object) wpsResult);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWpsRunningState);
                        break;
                    }
                    WifiStateMachine.this.loge("Cannot start WPS with null WpsInfo object");
                    WifiStateMachine.this.replyToMessage(message, 151564, 0);
                    break;
                    break;
                case 151569:
                    netId = message.arg1;
                    if (!WifiStateMachine.this.mWifiConfigManager.disableNetwork(netId, message.sendingUid)) {
                        WifiStateMachine.this.loge("Failed to disable network");
                        WifiStateMachine.this.messageHandlingStatus = -2;
                        WifiStateMachine.this.replyToMessage(message, 151570, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151571);
                    if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
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
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.log("Enter ConnectedState  mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
            if (WifiStateMachine.this.mDriverRoaming) {
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_IP_REACHABILITY_SESSION_END, 0, 0), 10000);
            }
            WifiStateMachine.this.registerConnected();
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.targetWificonfiguration = null;
            WifiStateMachine.this.mIsLinkDebouncing = false;
            WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS);
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mCheckInetAccessSeq = wifiStateMachine.mCheckInetAccessSeq + 1;
            if (WifiStateMachine.this.mIsAutoRoaming) {
                WifiStateMachine.this.loge("Dhcp successful after driver roaming, check internet access, seq=" + WifiStateMachine.this.mCheckInetAccessSeq);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS, 1, WifiStateMachine.this.mCheckInetAccessSeq), 2000);
            } else {
                WifiStateMachine.this.loge("Dhcp successful, check internet access, seq=" + WifiStateMachine.this.mCheckInetAccessSeq);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_CHECK_INTERNET_ACCESS, -1, WifiStateMachine.this.mCheckInetAccessSeq), 2000);
            }
            WifiStateMachine.this.mIsAutoRoaming = false;
            if (WifiStateMachine.this.testNetworkDisconnect) {
                wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.testNetworkDisconnectCounter = wifiStateMachine.testNetworkDisconnectCounter + 1;
                WifiStateMachine.this.logd("ConnectedState Enter start disconnect test " + WifiStateMachine.this.testNetworkDisconnectCounter);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT, WifiStateMachine.this.testNetworkDisconnectCounter, 0), 15000);
            }
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mTargetNetworkId = -1;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            WifiStateMachine.this.mWifiStateTracker.updateState(3);
            OifaceProxyUtils.getInstance().currentNetwork(0);
        }

        /* JADX WARNING: Removed duplicated region for block: B:119:0x0726 A:{ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), Splitter: B:114:0x06c3} */
        /* JADX WARNING: Missing block: B:120:0x0727, code:
            r25.this$0.loge("Can't find MAC address for next hop to " + r17.dstAddress);
     */
        /* JADX WARNING: Missing block: B:121:0x0754, code:
            if (com.android.server.wifi.WifiStateMachine.-get46(r25.this$0) != null) goto L_0x0756;
     */
        /* JADX WARNING: Missing block: B:122:0x0756, code:
            com.android.server.wifi.WifiStateMachine.-get46(r25.this$0).onPacketKeepaliveEvent(r19, -21);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            String str;
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                    if (message.arg1 == WifiStateMachine.this.testNetworkDisconnectCounter) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 0) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 3);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    } else if (message.arg1 == 2 || message.arg1 == 1) {
                        String str2 = WifiStateMachine.TAG;
                        if (message.arg1 == 2) {
                            str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                        } else {
                            str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                        }
                        Log.d(str2, str);
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            if (message.arg1 == 2) {
                                WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 9);
                            }
                            WifiStateMachine.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    if (config != null) {
                        WifiStateMachine.this.setTargetBssid(config, bssid);
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        WifiStateMachine.this.logd("CMD_START_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config.configKey() + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                        WifiStateMachine.this.reportConnectionAttemptStart(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                        int netIdFromWifiInfo = -1;
                        if (WifiStateMachine.this.mWifiInfo != null) {
                            netIdFromWifiInfo = WifiStateMachine.this.mWifiInfo.getNetworkId();
                        }
                        if (!WifiStateMachine.this.mIsLinkDebouncing || netIdFromWifiInfo == -1 || netIdFromWifiInfo == WifiStateMachine.this.mLastNetworkId) {
                            if (!WifiStateMachine.this.mWifiNative.roamToNetwork(config)) {
                                WifiStateMachine.this.loge("CMD_START_ROAM Failed to start roaming to network " + config);
                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                WifiStateMachine.this.messageHandlingStatus = -2;
                                break;
                            }
                            WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiStateMachine.this.targetWificonfiguration = config;
                            WifiStateMachine.this.mIsAutoRoaming = true;
                            WifiStateMachine.this.mWifiMetrics.logStaEvent(12, config);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                            break;
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        WifiStateMachine.this.mWifiNative.reassociate();
                        break;
                    }
                    WifiStateMachine.this.loge("CMD_START_ROAM and no config, bail out...");
                    break;
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = WifiStateMachine.this.mClock.getWallClockMillis();
                    return false;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, true);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_ACCEPT_UNVALIDATED /*131225*/:
                    WifiStateMachine.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(WifiStateMachine.this.mLastNetworkId, message.arg1 != 0);
                    return true;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = message.obj;
                    try {
                        pkt.dstMac = NativeUtil.macAddressToByteArray(WifiStateMachine.this.macAddressFromRoute(RouteInfo.selectBestRoute(WifiStateMachine.this.mLinkProperties.getRoutes(), pkt.dstAddress).getGateway().getHostAddress()));
                        int result = WifiStateMachine.this.startWifiIPPacketOffload(slot, pkt, intervalSeconds);
                        if (WifiStateMachine.this.mNetworkAgent != null) {
                            WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, result);
                            break;
                        }
                    } catch (NullPointerException e) {
                    }
                    break;
                case WifiStateMachine.CMD_IP_REACHABILITY_SESSION_END /*131326*/:
                    WifiStateMachine.this.mDriverRoaming = false;
                    break;
                case WifiStateMachine.CMD_CHECK_INTERNET_ACCESS /*131573*/:
                    WifiStateMachine.this.loge("Checking internet access, SSID=" + WifiStateMachine.this.mWifiInfo.getSSID() + " BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " checkSequence=" + message.arg2);
                    WifiStateMachine.this.checkInternetAccess(message.arg1 == 1, message.arg2);
                    break;
                case WifiStateMachine.CMD_MTU_PROBER /*131577*/:
                    WifiStateMachine.this.mtuProber();
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    long lastRoam = 0;
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = WifiStateMachine.this.mClock.getWallClockMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (!WifiStateMachine.this.mEnableLinkDebouncing || !WifiStateMachine.this.mScreenOn || (WifiStateMachine.this.isLinkDebouncing() ^ 1) == 0 || config == null || !config.getNetworkSelectionStatus().isNetworkEnabled() || config.networkId == WifiStateMachine.this.mWifiConfigManager.getLastSelectedNetwork() || ((message.arg2 == 3 && (lastRoam <= 0 || lastRoam >= 2000)) || ((!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5) && (!ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5)))) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=").append(WifiStateMachine.this.mWifiInfo.getBSSID()).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.isLinkDebouncing()).append(" reason=").append(message.arg2).append(" Network Selection Status=");
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
                    if (WifiStateMachine.this.mWifiNetworkStateTraker == null || !(WifiStateMachine.this.mWifiNetworkStateTraker == null || (WifiStateMachine.this.mAutoSwitch ^ 1) == 0)) {
                        WifiStateMachine.this.mIsLinkDebouncing = true;
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_LINK_FLAPPING_DEBOUNCE_MSEC", Integer.valueOf(WifiStateMachine.LINK_FLAPPING_DEBOUNCE_MSEC)).intValue());
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
            OifaceProxyUtils.getInstance().currentNetwork(1);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        /* JADX WARNING: Missing block: B:9:0x0064, code:
            if (com.android.server.wifi.WifiStateMachine.-get72(r26.this$0).processSoftApStateMessage(r27, com.android.server.wifi.WifiStateMachine.-get11(r26.this$0)) != false) goto L_0x0066;
     */
        /* JADX WARNING: Missing block: B:171:0x098a, code:
            if (com.mediatek.server.wifi.WifiApStateMachine.processDefaultStateMessage(r27, com.android.server.wifi.WifiStateMachine.-get11(r26.this$0)) == false) goto L_0x098c;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case 0:
                    Log.wtf(WifiStateMachine.TAG, "Error! empty message encountered");
                    break;
                case 69632:
                    if (message.obj == WifiStateMachine.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            WifiStateMachine.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        }
                        WifiStateMachine.this.p2pSendMessage(69633);
                        if (WifiStateMachine.this.mOperationalMode == 1) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                            break;
                        }
                    }
                    WifiStateMachine.this.loge("got HALF_CONNECTED for unknown channel");
                    break;
                    break;
                case 69636:
                    if (((AsyncChannel) message.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        WifiStateMachine.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                case WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                case WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO /*131325*/:
                case 147457:
                case 147458:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
                case 196611:
                case 196612:
                case 196614:
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                    WifiConfiguration config = message.obj;
                    WifiStateMachine.this.convertToQuotedSSID(config);
                    WifiConfiguration currConfig = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mWifiInfo.getNetworkId());
                    WifiConfiguration saveConfig = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(config.configKey(true));
                    if (saveConfig != null && config.hiddenSSID) {
                        if (currConfig != null && WifiStateMachine.this.mWifiInfo.getNetworkId() == saveConfig.networkId && "ConnectedState".equalsIgnoreCase(WifiStateMachine.this.getCurrentState().getName())) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                Log.d(WifiStateMachine.TAG, "trying to add a already connected network,ignore");
                            }
                            WifiStateMachine.this.replyToMessage(message, WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK, saveConfig.networkId);
                            break;
                        }
                        WifiStateMachine.this.mOppoWifiConnectionAlert.setLastUpdatedWifiConfiguration(WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(saveConfig.networkId));
                    }
                    NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                    if (!result.isSuccess()) {
                        WifiStateMachine.this.messageHandlingStatus = -2;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    int i;
                    boolean ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(message.arg1, message.arg2 == 1, message.sendingUid);
                    if (!ok) {
                        WifiStateMachine.this.messageHandlingStatus = -2;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    int i2 = message.what;
                    if (ok) {
                        i = 1;
                    } else {
                        i = -1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getSavedNetworks());
                    break;
                case WifiStateMachine.CMD_GET_SUPPORTED_FEATURES /*131133*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.getSupportedFeatureSet());
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
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
                    if (WifiStateMachine.this.mWifiDiagnostics != null) {
                        byte[] buffer = message.obj;
                        int alertReason = message.arg1;
                        WifiStateMachine.this.mWifiDiagnostics.captureAlertData(alertReason, buffer);
                        WifiStateMachine.this.mWifiMetrics.incrementAlertReasonCount(alertReason);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) message.obj, message.arg1) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mPasspointManager.removeProvider((String) message.obj) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_GET_PASSPOINT_CONFIGS /*131180*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mPasspointManager.getProviderConfigs());
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_OSU_PROVIDERS /*131181*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) new ArrayList());
                    break;
                case 131206:
                    WifiStateMachine.this.getAdditionalWifiServiceInterfaces();
                    if (!WifiStateMachine.this.hasLoadStore) {
                        if (!WifiStateMachine.this.mWifiConfigManager.loadFromStore()) {
                            Log.e(WifiStateMachine.TAG, "Failed to load from config store");
                        }
                        WifiStateMachine.this.hasLoadStore = true;
                    }
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    WifiStateMachine.this.maybeRegisterPhoneListener();
                    break;
                case WifiStateMachine.CMD_INITIALIZE /*131207*/:
                    Log.d(WifiStateMachine.TAG, "=qcdbg= WifiStateMachine - handle CMD_INITIALIZE");
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.initializeVendorHal(WifiStateMachine.this.mVendorHalDeathRecipient) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_SESSION_END /*131326*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
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
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
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
                case WifiStateMachine.CMD_USER_SWITCH /*131277*/:
                    Set<Integer> removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_USER_UNLOCK /*131278*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case WifiStateMachine.CMD_USER_STOP /*131279*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case WifiStateMachine.CMD_CLIENT_INTERFACE_BINDER_DEATH /*131322*/:
                    Log.e(WifiStateMachine.TAG, "wificond died unexpectedly. Triggering recovery");
                    WifiStateMachine.this.mWifiMetrics.incrementNumWificondCrashes();
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(8);
                    WifiStateMachine.this.mWifiInjector.getSelfRecovery().trigger(2);
                    break;
                case WifiStateMachine.CMD_VENDOR_HAL_HWBINDER_DEATH /*131323*/:
                    Log.e(WifiStateMachine.TAG, "Vendor HAL died unexpectedly. Triggering recovery");
                    WifiStateMachine.this.mWifiMetrics.incrementNumHalCrashes();
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(9);
                    WifiStateMachine.this.mWifiInjector.getSelfRecovery().trigger(1);
                    break;
                case WifiStateMachine.CMD_DIAGS_CONNECT_TIMEOUT /*131324*/:
                    WifiStateMachine.this.mWifiDiagnostics.reportConnectionEvent(((Long) message.obj).longValue(), (byte) 2);
                    break;
                case WifiStateMachine.M_CMD_SET_POWER_SAVING_MODE /*131581*/:
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = message.arg1 == 1;
                    WifiStateMachine.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.logd("SET_MIRACAST_MODE: " + message.arg1);
                    }
                    WifiStateMachine.this.mWifiConnectivityManager.saveMiracastMode(message.arg1);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (message != null) {
                        StateChangeResult stateChangeResult = message.obj;
                        if (stateChangeResult != null) {
                            SupplicantState state = stateChangeResult.state;
                            if (WifiStateMachine.this.mLastSupplicantState != state) {
                                WifiStateMachine.this.mLastSupplicantState = state;
                                break;
                            }
                        }
                    }
                    break;
                case 151553:
                    WifiStateMachine.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, true);
                    break;
                case 151559:
                    WifiStateMachine.this.saveNetworkConfigAndSendReply(message);
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
                default:
                    if (!WifiStateMachine.this.mStaAndAPConcurrency || WifiStateMachine.this.mSoftApStateMachine == null || WifiStateMachine.this.mSoftApStateMachine.syncGetWifiApState() != 13) {
                        break;
                    }
                    break;
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
            Log.i(WifiStateMachine.TAG, "disconnectedstate enter");
            if (WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                WifiStateMachine.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.logd(" Enter DisconnectedState screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.mIsAutoRoaming = false;
            WifiStateMachine.this.mDriverRoaming = false;
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
            if (!(WifiStateMachine.this.mNoNetworksPeriodicScan == 0 || (WifiStateMachine.this.mP2pConnected.get() ^ 1) == 0 || WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() != 0)) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
            }
            WifiStateMachine.this.mDisconnectedTimeStamp = WifiStateMachine.this.mClock.getWallClockMillis();
            WifiStateMachine.this.mWifiStateTracker.updateState(2);
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
                    WifiStateMachine.this.messageHandlingStatus = -1;
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                        WifiStateMachine.this.mWifiNative.disconnect();
                        if (WifiStateMachine.this.mOperationalMode != 4) {
                            if (WifiStateMachine.this.mOperationalMode == 2 || WifiStateMachine.this.mOperationalMode == 3) {
                                WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                                WifiStateMachine.this.setWifiState(1);
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
                                break;
                            }
                        }
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 0);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    if (!WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                        ret = false;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                    if (!WifiStateMachine.this.mP2pConnected.get() && WifiStateMachine.this.mNoNetworksPeriodicScan != 0 && message.arg1 == WifiStateMachine.this.mPeriodicScanToken && WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
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
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    if (!WifiStateMachine.this.mP2pConnected.get() && WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Turn on scanning after p2p disconnected");
                        }
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine2 = WifiStateMachine.this;
                        wifiStateMachine3 = WifiStateMachine.this;
                        wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state) + " debouncing=" + WifiStateMachine.this.isLinkDebouncing());
                    }
                    WifiStateMachine.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    ret = false;
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
        }

        public void exit() {
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                    if (WifiStateMachine.this.disconnectingWatchdogCount == message.arg1) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("disconnecting watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
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

    class FilsState extends State {
        FilsState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                Log.d(WifiStateMachine.TAG, "Filsstate enter");
            }
            WifiStateMachine.this.mIpManager;
            ProvisioningConfiguration prov = IpManager.buildProvisioningConfiguration().withPreDhcpAction().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
            prov.mRapidCommit = true;
            prov.mDiscoverSent = true;
            WifiStateMachine.this.mIpManager.startProvisioning(prov);
            WifiStateMachine.this.mIsIpManagerStarted = true;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                    WifiStateMachine.this.stopIpManager();
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    WifiStateMachine.this.stopIpManager();
                    return false;
                case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
                    WifiStateMachine.this.mIsFilsConnection = true;
                    break;
                case 196611:
                    WifiStateMachine.this.handlePreFilsDhcpSetup();
                    break;
                case 196612:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case 196614:
                    WifiStateMachine.this.mIpManager.completedPreDhcpAction();
                    WifiStateMachine.this.buildDiscoverWithRapidCommitPacket();
                    WifiStateMachine.this.reportConnectionAttemptStart(WifiStateMachine.this.mFilsConfig, WifiStateMachine.this.mTargetRoamBSSID, 5);
                    if (!WifiStateMachine.this.mWifiNative.connectToNetwork(WifiStateMachine.this.mFilsConfig)) {
                        WifiStateMachine.this.loge("Failed to connect to FILS network " + WifiStateMachine.this.mFilsConfig);
                        WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                        WifiStateMachine.this.replyToMessage(message, 151554, 0);
                        break;
                    }
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(11, WifiStateMachine.this.mFilsConfig);
                    WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mFilsConfig;
                    WifiStateMachine.this.mIsAutoRoaming = false;
                    break;
                default:
                    return false;
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.log("Network connection established with FILS " + WifiStateMachine.this.mIsFilsConnection);
            }
            WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
            WifiStateMachine.this.mLastBssid = (String) message.obj;
            int reasonCode = message.arg2;
            WifiConfiguration config = WifiStateMachine.this.getCurrentWifiConfiguration();
            if (config != null) {
                WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, reasonCode);
                if (config.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                    String anonymousIdentity = WifiStateMachine.this.mWifiNative.getEapAnonymousIdentity();
                    if (anonymousIdentity != null) {
                        config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                    } else {
                        Log.d(WifiStateMachine.TAG, "Failed to get updated anonymous identity from supplicant, reset it in WifiConfiguration.");
                        config.enterpriseConfig.setAnonymousIdentity(null);
                    }
                    WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, 1010);
                }
                WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
            }
            return true;
        }

        public void exit() {
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            WifiStateMachine.this.mWifiStateTracker.updateState(0);
            if (!WifiStateMachine.this.staCleanUpDone) {
                WifiStateMachine.this.staCleanup();
            }
            WifiStateMachine.this.staCleanUpDone = false;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                    Pair<Integer, IClientInterface> statusAndInterface = WifiStateMachine.this.mWifiNative.setupForClientMode();
                    if (((Integer) statusAndInterface.first).intValue() == 0) {
                        WifiStateMachine.this.mClientInterface = (IClientInterface) statusAndInterface.second;
                    } else {
                        WifiStateMachine.this.incrementMetricsForSetupFailure(((Integer) statusAndInterface.first).intValue());
                    }
                    if (WifiStateMachine.this.mClientInterface != null && (WifiStateMachine.this.mDeathRecipient.linkToDeath(WifiStateMachine.this.mClientInterface.asBinder()) ^ 1) == 0) {
                        try {
                            WifiStateMachine.this.mNwService.clearInterfaceAddresses(WifiStateMachine.this.mDataInterfaceName);
                            WifiStateMachine.this.mNwService.setInterfaceIpv6PrivacyExtensions(WifiStateMachine.this.mDataInterfaceName, true);
                            WifiStateMachine.this.mNwService.disableIpv6(WifiStateMachine.this.mDataInterfaceName);
                        } catch (RemoteException re) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        WifiStateMachine.this.updateDataInterface();
                        if (!WifiStateMachine.this.mWifiNative.enableSupplicant()) {
                            WifiStateMachine.this.loge("Failed to start supplicant!");
                            WifiStateMachine.this.setWifiState(4);
                            WifiStateMachine.this.staCleanup();
                            break;
                        }
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Supplicant start successful");
                        }
                        WifiStateMachine.this.mWifiMonitor.startMonitoring(WifiStateMachine.this.mInterfaceName, true);
                        WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().clearAllFailureCounts();
                        WifiStateMachine.this.setSupplicantLogLevel();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartingState);
                        break;
                    }
                    WifiStateMachine.this.setWifiState(4);
                    WifiStateMachine.this.staCleanup();
                    break;
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSoftApState);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    if (WifiStateMachine.this.mOperationalMode != 4) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_SUPPLICANT);
                        break;
                    }
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
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 3);
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(7);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(8);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(9);
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
            WifiStateMachine.this.clearTargetBssid("L2ConnectedState");
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(3);
        }

        public void exit() {
            WifiStateMachine.this.mIpManager.stop();
            WifiStateMachine.this.mIsIpManagerStarted = false;
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
            WifiStateMachine.this.mIsAutoRoaming = false;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine wifiStateMachine;
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 0);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    WifiStateMachine.this.log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.cleanWifiScore();
                    if (WifiStateMachine.this.mEnableRssiPollWhenAssociated) {
                        WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    } else {
                        WifiStateMachine.this.mEnableRssiPolling = false;
                    }
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
                    if (WifiStateMachine.this.mEnableRssiPolling) {
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), (long) WifiStateMachine.this.mPollRssiIntervalMsecs);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                    if (message.arg1 == WifiStateMachine.this.mRssiPollToken) {
                        if (WifiStateMachine.this.mEnableChipWakeUpWhenAssociated) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.log(" get link layer stats " + WifiStateMachine.this.mWifiLinkLayerStatsSupported);
                            }
                            WifiLinkLayerStats stats = WifiStateMachine.this.getWifiLinkLayerStats();
                            if (!(stats == null || WifiStateMachine.this.mWifiInfo.getRssi() == WifiMetrics.MIN_RSSI_DELTA || (stats.rssi_mgmt != 0 && stats.beacon_rx != 0))) {
                            }
                            WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        }
                        if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.reportRssi();
                        }
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), (long) WifiStateMachine.this.mPollRssiIntervalMsecs);
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.sendRssiChangeBroadcast(WifiStateMachine.this.mWifiInfo.getRssi());
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT /*131159*/:
                    if (WifiStateMachine.this.isLinkDebouncing()) {
                        WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and debouncing - disconnect " + message.arg1);
                        WifiStateMachine.this.mIsLinkDebouncing = false;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and not debouncing - ignore " + message.arg1);
                    return true;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message.arg1 == 0 && WifiStateMachine.this.mLastNetworkId != -1 && TelephonyUtil.isSimConfig(WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 6);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    }
                    return false;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.mIsAutoRoaming = false;
                    WifiStateMachine.this.handleSuccessfulIpConfiguration();
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null && (WifiStateMachine.this.mWifiNetworkStateTraker.getIsOppoManuConnect() ^ 1) != 0 && WifiStateMachine.this.wifiAssistantForSoftAP() && WifiStateMachine.this.mAutoSwitch && WifiStateMachine.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_DETECT_CONNECT", Boolean.valueOf(true)).booleanValue() && WifiStateMachine.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED && (WifiStateMachine.this.mNetworkDetectValid ^ 1) != 0) {
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
                    }
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    WifiStateMachine.this.getWifiLinkLayerStats();
                    WifiStateMachine.this.handleIpConfigurationLost();
                    WifiStateMachine.this.reportConnectionAttemptEnd(10, 1);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                    int startConnectId = message.arg1;
                    if (WifiStateMachine.this.mWifiInfo != null && WifiStateMachine.this.mWifiInfo.getNetworkId() == startConnectId) {
                        WifiStateMachine.this.replyToMessage(message, 151555);
                        break;
                    }
                    return false;
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (((String) message.obj) != null) {
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastBssid != null && (WifiStateMachine.this.mWifiInfo.getBSSID() == null || (WifiStateMachine.this.mLastBssid.equals(WifiStateMachine.this.mWifiInfo.getBSSID()) ^ 1) != 0)) {
                            WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                            WifiConfiguration config = WifiStateMachine.this.getCurrentWifiConfiguration();
                            if (config != null) {
                                ScanDetailCache scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
                                if (scanDetailCache != null) {
                                    ScanResult scanResult = scanDetailCache.getScanResult(WifiStateMachine.this.mLastBssid);
                                    if (scanResult != null) {
                                        WifiStateMachine.this.mWifiInfo.setFrequency(scanResult.frequency);
                                    }
                                }
                            }
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
                    if (WifiStateMachine.this.mVerboseLoggingEnabled && message.obj != null) {
                        WifiStateMachine.this.log((String) message.obj);
                    }
                    if (WifiStateMachine.this.mIpReachabilityDisconnectEnabled) {
                        if (WifiStateMachine.this.mDisconnectOnlyOnInitialIpReachability && (WifiStateMachine.this.mDriverRoaming ^ 1) != 0) {
                            WifiStateMachine.this.logd("CMD_IP_REACHABILITY_LOST Connect session is over, skip ip reachability lost indication.");
                            break;
                        }
                        WifiStateMachine.this.handleIpReachabilityLost();
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(10);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    WifiStateMachine.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                    break;
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                case WifiStateMachine.CMD_RSSI_THRESHOLD_BREACHED /*131236*/:
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
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
                    WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                    if (WifiStateMachine.this.hasConfigKeyChanged(message.arg1)) {
                        DhcpClient.clearOffer();
                    }
                    if (message.arg1 != -1) {
                        WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    }
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
                    if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, WifiStateMachine.this.mLastNetworkId, message.arg2, WifiStateMachine.this.mLastBssid);
                    }
                    WifiStateMachine.this.mDriverRoaming = true;
                    if (WifiStateMachine.this.isUsingDHCP()) {
                        WifiStateMachine.this.mIsAutoRoaming = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                        break;
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
                    if (!(WifiStateMachine.this.mWifiInfo == null || WifiStateMachine.this.mWifiInfo.getNetworkId() != netId || OppoManuConnectManager.getInstance() == null)) {
                        OppoManuConnectManager.getInstance().handleManuConnect(netId, message.sendingUid);
                        if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.handleManualConnect(true);
                            WifiStateMachine.this.sendConnectedState();
                            WifiStateMachine.this.replyToMessage(message, 151555);
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
                    TxPacketCounters counters = WifiStateMachine.this.mWifiNative.getTxPacketCounters();
                    if (message != null && message.arg1 == 1) {
                        info.rssi = WifiStateMachine.this.originRssi;
                    }
                    if (counters == null) {
                        WifiStateMachine.this.replyToMessage(message, 151574, 0);
                        break;
                    }
                    info.txgood = counters.txSucceeded;
                    info.txbad = counters.txFailed;
                    WifiStateMachine.this.replyToMessage(message, 151573, (Object) info);
                    break;
                case 196611:
                    WifiStateMachine.this.handlePreDhcpSetup();
                    break;
                case 196612:
                    WifiStateMachine.this.handlePostDhcpSetup();
                    break;
                case 196614:
                    WifiStateMachine.this.mIpManager.completedPreDhcpAction();
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
            WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
            boolean isUsingStaticIp = false;
            if (currentConfig != null) {
                IpAssignment ipAssignment = currentConfig.getIpAssignment();
                if (ipAssignment != null) {
                    isUsingStaticIp = ipAssignment == IpAssignment.STATIC;
                }
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                String key = "";
                if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                    key = WifiStateMachine.this.getCurrentWifiConfiguration().configKey();
                }
                WifiStateMachine.this.log("enter ObtainingIpState netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key + " " + " roam=" + WifiStateMachine.this.mIsAutoRoaming + " static=" + isUsingStaticIp);
            }
            WifiStateMachine.this.mIsLinkDebouncing = false;
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.OBTAINING_IPADDR);
            WifiStateMachine.this.clearTargetBssid("ObtainingIpAddress");
            WifiStateMachine.this.mWifiNative.setPowerSave(false);
            if (!(WifiStateMachine.this.mIsFilsConnection || (WifiStateMachine.this.mIsAutoRoaming ^ 1) == 0)) {
                WifiStateMachine.this.stopIpManager();
            }
            if (currentConfig != null) {
                WifiStateMachine.this.mIpManager.setHttpProxy(currentConfig.getHttpProxy());
            }
            if (!TextUtils.isEmpty(WifiStateMachine.this.mTcpBufferSizes)) {
                WifiStateMachine.this.mIpManager.setTcpBufferSizes(WifiStateMachine.this.mTcpBufferSizes);
            }
            if (WifiStateMachine.this.mIsFilsConnection && WifiStateMachine.this.mIsIpManagerStarted) {
                WifiStateMachine.this.setPowerSaveForFilsDhcp();
            } else if (isUsingStaticIp) {
                WifiStateMachine.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withStaticConfiguration(currentConfig.getStaticIpConfiguration()).withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build());
                WifiStateMachine.this.mIsIpManagerStarted = true;
            } else {
                WifiStateMachine.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withPreDhcpAction().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build());
                WifiStateMachine.this.mIsIpManagerStarted = true;
            }
            WifiStateMachine.this.getWifiLinkLayerStats();
            WifiStateMachine.this.mIsFilsConnection = false;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    return false;
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.log("RoamingState Enter mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.roamWatchdogCount++;
            WifiStateMachine.this.logd("Start Roam Watchdog " + WifiStateMachine.this.roamWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER, WifiStateMachine.this.roamWatchdogCount, 0), (long) WifiStateMachine.this.getRomUpdateIntegerValue("CONNECT_ROAM_GUARD_TIMER_MSEC", Integer.valueOf(15000)).intValue());
            this.mAssociated = false;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
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
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("roaming watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.mWifiMetrics.endConnectionEvent(9, 1);
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.mRoamFailCount = wifiStateMachine.mRoamFailCount + 1;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 4);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(3);
                    }
                    return false;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Roaming and CS doesnt want the network -> ignore");
                    }
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (!this.mAssociated) {
                        WifiStateMachine.this.messageHandlingStatus = -5;
                        break;
                    }
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("roaming and Network connection established");
                    }
                    if (WifiStateMachine.this.hasConfigKeyChanged(message.arg1)) {
                        DhcpClient.clearOffer();
                    }
                    WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, message.arg2);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    WifiStateMachine.this.clearTargetBssid("RoamingCompleted");
                    WifiStateMachine.this.mDriverRoaming = true;
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
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
                            break;
                        }
                    }
                    break;
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
            this.mLastOperationMode = WifiStateMachine.this.mOperationalMode;
            WifiStateMachine.this.mWifiStateTracker.updateState(1);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        if (message.arg1 == 4) {
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                            break;
                        }
                    }
                    if (this.mLastOperationMode == 3) {
                        if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.readConfigAndUpdate();
                        } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.readWifiNetworkRecord();
                            if (WifiStateMachine.this.mAutoSwitch) {
                                WifiStateMachine.this.mWifiNetworkStateTraker.disableNetworkWithoutInternet();
                            }
                        }
                    }
                    if (WifiStateMachine.this.mAutoSwitch) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - WifiStateMachine.this.mLastScanTime > WifiStateMachine.SMART_SCAN_INTERVAL) {
                            WifiStateMachine.this.startScan(-1, 0, null, null);
                        } else if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                            WifiStateMachine.this.mWifiNetworkAvailable.detectScanResult(currentTime);
                        } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.detectScanResult(currentTime);
                        }
                    }
                    WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                    WifiStateMachine.this.mOperationalMode = 1;
                    WifiStateMachine.this.setWifiState(2);
                    WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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

    class SoftApState extends State {
        private String mIfaceName;
        private int mMode;
        private SoftApManager mSoftApManager;

        private class SoftApListener implements Listener {
            /* synthetic */ SoftApListener(SoftApState this$1, SoftApListener -this1) {
                this();
            }

            private SoftApListener() {
            }

            public void onStateChanged(int state, int reason) {
                if (state == 11) {
                    WifiStateMachine.this.mWifiNative.addOrRemoveInterface(WifiStateMachine.this.mSapInterfaceName, false, WifiStateMachine.this.mDualSapMode);
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_AP_STOPPED);
                } else if (state == 14) {
                    WifiStateMachine.this.mWifiNative.addOrRemoveInterface(WifiStateMachine.this.mSapInterfaceName, false, WifiStateMachine.this.mDualSapMode);
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_AP_FAILURE);
                }
                WifiStateMachine.this.setWifiApState(state, reason, SoftApState.this.mIfaceName, SoftApState.this.mMode);
            }
        }

        SoftApState() {
        }

        public void enter() {
            Message message = WifiStateMachine.this.getCurrentMessage();
            if (message.what != WifiStateMachine.CMD_START_AP) {
                throw new RuntimeException("Illegal transition to SoftApState: " + message);
            }
            SoftApModeConfiguration config = message.obj;
            this.mMode = config.getTargetMode();
            IApInterface iApInterface = null;
            Pair<Integer, IApInterface> statusAndInterface = WifiStateMachine.this.mWifiNative.setupForSoftApMode(WifiStateMachine.this.mSapInterfaceName, WifiStateMachine.this.mDualSapMode);
            if (((Integer) statusAndInterface.first).intValue() == 0) {
                iApInterface = statusAndInterface.second;
            } else {
                WifiStateMachine.this.incrementMetricsForSetupFailure(((Integer) statusAndInterface.first).intValue());
            }
            if (iApInterface == null) {
                WifiStateMachine.this.mWifiNative.addOrRemoveInterface(WifiStateMachine.this.mSapInterfaceName, false, WifiStateMachine.this.mDualSapMode);
                WifiStateMachine.this.setWifiApState(14, 0, null, this.mMode);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                return;
            }
            try {
                int fstEnabled = SystemProperties.getInt("persist.vendor.fst.softap.en", 0);
                String rateUpgradeDataInterfaceName = SystemProperties.get("persist.vendor.fst.data.interface", "bond0");
                if (fstEnabled != 1) {
                    rateUpgradeDataInterfaceName = iApInterface.getInterfaceName();
                }
                this.mIfaceName = rateUpgradeDataInterfaceName;
                WifiStateMachine.this.logd("softap fst " + (fstEnabled == 1 ? "enabled" : "disabled"));
            } catch (RemoteException e) {
            }
            WifiStateMachine.this.checkAndSetConnectivityInstance();
            this.mSoftApManager = WifiStateMachine.this.mWifiInjector.makeSoftApManager(WifiStateMachine.this.mNwService, new SoftApListener(this, null), iApInterface, config.getWifiConfiguration());
            this.mSoftApManager.setDualSapMode(WifiStateMachine.this.mDualSapMode);
            this.mSoftApManager.start();
            WifiStateMachine.this.mWifiStateTracker.updateState(4);
        }

        public void exit() {
            this.mSoftApManager = null;
            this.mIfaceName = null;
            this.mMode = -1;
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
                    this.mSoftApManager.setDualSapMode(WifiStateMachine.this.mDualSapMode);
                    break;
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                default:
                    if (WifiApStateMachine.processSoftApStateMessage(message, WifiStateMachine.this.mContext, this.mSoftApManager)) {
                        WifiStateMachine.this.loge("WifiApStateMachine handled message: " + message);
                        break;
                    }
                    return false;
            }
            return true;
        }
    }

    class SupplicantStartedState extends State {
        SupplicantStartedState() {
        }

        public void enter() {
            boolean z;
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.logd("SupplicantStartedState enter");
            }
            WifiStateMachine.this.mWifiNative.setExternalSim(true);
            WifiStateMachine.this.setRandomMacOui();
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            if (WifiStateMachine.this.mWifiScanner == null) {
                WifiStateMachine.this.mWifiScanner = WifiStateMachine.this.mWifiInjector.getWifiScanner();
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine.this.mWifiConnectivityManager = WifiStateMachine.this.mWifiInjector.makeWifiConnectivityManager(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.hasConnectionRequests());
                    WifiConnectivityManager -get93 = WifiStateMachine.this.mWifiConnectivityManager;
                    if (WifiStateMachine.this.mUntrustedReqCount > 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    -get93.setUntrustedConnectionAllowed(z);
                    WifiStateMachine.this.mWifiConnectivityManager.handleScreenStateChanged(WifiStateMachine.this.mScreenOn);
                }
            }
            WifiStateMachine.this.mWifiDiagnostics.startLogging(WifiStateMachine.this.mVerboseLoggingEnabled);
            WifiStateMachine.this.mIsRunning = true;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
            if (WifiStateMachine.this.mEnableVoiceCallSarTxPowerLimit) {
                if (WifiStateMachine.this.getTelephonyManager().isOffhook()) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO, 1);
                } else {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO, 0);
                }
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV6Packets();
            if (WifiStateMachine.this.mOperationalMode == 2 || WifiStateMachine.this.mOperationalMode == 3) {
                WifiStateMachine.this.mWifiNative.disconnect();
                WifiStateMachine.this.setWifiState(1);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
            } else if (WifiStateMachine.this.mOperationalMode == 1) {
                WifiStateMachine.this.setWifiState(2);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
            } else if (WifiStateMachine.this.mOperationalMode == 4) {
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
            }
            WifiNative -get100 = WifiStateMachine.this.mWifiNative;
            if (WifiStateMachine.this.mSuspendOptNeedsDisabled == 0) {
                z = WifiStateMachine.this.mUserWantsSuspendOpt.get();
            } else {
                z = false;
            }
            -get100.setSuspendOptimizations(z);
            WifiStateMachine.this.mWifiNative.setPowerSave(true);
            if (WifiStateMachine.this.mP2pSupported && WifiStateMachine.this.mOperationalMode == 1) {
                WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            }
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 3);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (WifiStateMachine.this.mWifiNative != null) {
                WifiStateMachine.this.mWifiNative.enableStaAutoReconnect(true);
            }
            WifiStateMachine.this.mWifiNative.setConcurrencyPriority(true);
            if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), "wifi_5g_band_support", 0) == 0 && 2 == (WifiStateMachine.this.mWifiNative.getSupportedFeatureSet() & 2)) {
                Global.putInt(WifiStateMachine.this.mContext.getContentResolver(), "wifi_5g_band_support", 1);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
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
                    WifiStateMachine.this.setWifiApState(14, 0, null, -1);
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.getWifiLinkLayerStats());
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    if (WifiStateMachine.this.mOperationalMode == 4) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
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
                case WifiStateMachine.CMD_ENABLE_TDLS /*131164*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mWifiNative.startTdls(message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    WifiStateMachine.this.mWifiConfigManager.resetSimNetworks(message.arg1 == 1);
                    break;
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mTargetRoamBSSID = (String) message.obj;
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
                    WifiStateMachine.this.mWifiConnectivityManager.enable(message.arg1 == 1);
                    break;
                case WifiStateMachine.CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED /*131239*/:
                    boolean allowed = message.arg1 > 0;
                    boolean old_state = WifiStateMachine.this.mEnableAutoJoinWhenAssociated;
                    WifiStateMachine.this.mEnableAutoJoinWhenAssociated = allowed;
                    if (!old_state && allowed && WifiStateMachine.this.mScreenOn && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState) {
                        WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan(WifiStateMachine.WIFI_WORK_SOURCE);
                        break;
                    }
                case WifiStateMachine.CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    WifiStateMachine.this.mWifiNative.configureNeighborDiscoveryOffload(message.arg1 > 0);
                    break;
                case WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO /*131325*/:
                    int txPowerScenario = message.arg1;
                    WifiStateMachine.this.logd("Setting Tx power scenario to " + txPowerScenario);
                    if (!WifiStateMachine.this.mWifiNative.selectTxPowerScenario(txPowerScenario)) {
                        WifiStateMachine.this.loge("Failed to set TX power scenario");
                        break;
                    }
                    break;
                case WifiStateMachine.M_CMD_SET_POWER_SAVING_MODE /*131581*/:
                    WifiStateMachine.this.mWifiNative.setPowerSave(message.arg1 == 1);
                    break;
                case 147458:
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
                case WifiMonitor.PNO_SCAN_RESULTS_EVENT /*147474*/:
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    if (!(message.what == WifiMonitor.SCAN_FAILED_EVENT)) {
                        WifiStateMachine.this.setScanResults(message.what);
                    }
                    WifiStateMachine.this.mIsScanOngoing = false;
                    WifiStateMachine.this.mIsFullScanOngoing = false;
                    if (WifiStateMachine.this.mBufferedScanMsg.size() > 0) {
                        WifiStateMachine.this.sendMessage((Message) WifiStateMachine.this.mBufferedScanMsg.remove());
                        break;
                    }
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                    WifiStateMachine.this.mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                    WifiStateMachine.this.mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                    WifiStateMachine.this.mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mWifiDiagnostics.stopLogging();
            WifiStateMachine.this.mIsRunning = false;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mScanResults = new ArrayList();
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mBufferedScanMsg.clear();
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
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case 147457:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Supplicant connection established");
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
                    WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress());
                    if (!WifiStateMachine.this.mWifiConfigManager.migrateFromLegacyStore()) {
                        Log.e(WifiStateMachine.TAG, "Failed to migrate from legacy config store");
                    }
                    if (!WifiStateMachine.this.hasLoadStore) {
                        if (!WifiStateMachine.this.mWifiConfigManager.loadFromStore()) {
                            Log.e(WifiStateMachine.TAG, "Failed to load from config store");
                        }
                        WifiStateMachine.this.hasLoadStore = true;
                    }
                    WifiStateMachine.this.startScan(-1, 0, null, null);
                    WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                    if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                        WifiStateMachine.this.mWifiNetworkAvailable.readConfigAndUpdate();
                    } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                        WifiStateMachine.this.mWifiNetworkStateTraker.readWifiNetworkRecord();
                        if (WifiStateMachine.this.mAutoSwitch) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.disableNetworkWithoutInternet();
                        }
                    }
                    initializeWpsDetails();
                    WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(true);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartedState);
                    break;
                case 147458:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mSupplicantRestartCount = wifiStateMachine.mSupplicantRestartCount + 1 > 5) {
                        WifiStateMachine.this.loge("Failed " + WifiStateMachine.this.mSupplicantRestartCount + " times to start supplicant, unload driver");
                        WifiStateMachine.this.mSupplicantRestartCount = 0;
                        WifiStateMachine.this.setWifiState(4);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.loge("Failed to setup control channel, restart supplicant");
                    WifiStateMachine.this.mWifiMonitor.stopAllMonitoring();
                    WifiStateMachine.this.mWifiNative.disableSupplicant();
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
            WifiStateMachine.this.handleNetworkDisconnect();
            String suppState = System.getProperty("init.svc.wpa_supplicant");
            if (suppState == null) {
                suppState = "unknown";
            }
            if (WifiStateMachine.this.mOperationalMode != 3) {
                WifiStateMachine.this.setWifiState(0);
            }
            WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
            WifiStateMachine.this.logd("SupplicantStoppingState: disableSupplicant  init.svc.wpa_supplicant=" + suppState);
            if (WifiStateMachine.this.mWifiNative.disableSupplicant()) {
                WifiStateMachine.this.mWifiNative.closeSupplicantConnection();
                WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(false);
                WifiStateMachine.this.setWifiState(1);
                WifiStateMachine.this.staCleanup();
                WifiStateMachine.this.staCleanUpDone = true;
            } else {
                WifiStateMachine.this.handleSupplicantConnectionLoss(true);
            }
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SUPPLICANT_STOPPED);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SUPPLICANT_STOPPED /*131090*/:
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
                        int -get10 = WifiStateMachine.this.mConnectionReqCount;
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
            switch (WifiStateMachine.this.getCurrentMessage().what) {
                case 147458:
                    this.mTransitionToState = WifiStateMachine.this.mInitialState;
                    break;
                default:
                    this.mTransitionToState = WifiStateMachine.this.mSupplicantStoppingState;
                    break;
            }
            if (WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ)) {
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER, WifiStateMachine.this.mDisableP2pWatchdogCount, 0), 2000);
            } else {
                WifiStateMachine.this.transitionTo(this.mTransitionToState);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                    if (WifiStateMachine.this.mDisableP2pWatchdogCount == message.arg1) {
                        WifiStateMachine.this.logd("Timeout waiting for CMD_DISABLE_P2P_RSP");
                        WifiStateMachine.this.transitionTo(this.mTransitionToState);
                        break;
                    }
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

    private class WifiNetworkAgent extends NetworkAgent {
        public WifiNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.mNetworkDetectValid = false;
                    WifiStateMachine.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(14);
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
            rssiVals[rssiVals.length - 2] = -128;
            rssiVals[rssiVals.length - 1] = 127;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val > 127 || val < -128) {
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
                if (wifiStateMachine.mConnectionReqCount = wifiStateMachine.mConnectionReqCount - 1 == 0 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                    int -get86 = WifiStateMachine.this.mUntrustedReqCount;
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mConnectionReqCount " + WifiStateMachine.this.mConnectionReqCount);
        }
    }

    private class WifiPhoneStateListener extends PhoneStateListener {
        WifiPhoneStateListener(Looper looper) {
            super(looper);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (!WifiStateMachine.this.mEnableVoiceCallSarTxPowerLimit) {
                return;
            }
            if (state == 2) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO, 1);
            } else if (state == 0) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SELECT_TX_POWER_SCENARIO, 0);
            }
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            this.mSourceMessage = Message.obtain(WifiStateMachine.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case 151553:
                    WifiStateMachine.this.log(" Ignore CMD_RECONNECT request because wps is running");
                    return true;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    return true;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = -5;
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    Pair<Boolean, Integer> loadResult = loadNetworksFromSupplicantAfterWps();
                    boolean success = ((Boolean) loadResult.first).booleanValue();
                    int netId = ((Integer) loadResult.second).intValue();
                    if (!success) {
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 0);
                    } else if (netId == -1) {
                        Log.d(WifiStateMachine.TAG, "WPS NETWORK_CONNECTION_EVENT from other connect, ignore!");
                        break;
                    } else {
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleWpsCompleted(netId);
                        }
                        if (WifiStateMachine.this.mWifiNative != null) {
                            WifiStateMachine.this.mWifiNative.updateCurrentConfigPairByWps(netId);
                        }
                        message.arg1 = netId;
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                            WifiStateMachine.this.mWifiNetworkStateTraker.sendMessage(151553, netId, 0);
                        }
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151565);
                    }
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection lost");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsSupplicantStateChanged(message);
                        break;
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    }
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
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
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private Pair<Boolean, Integer> loadNetworksFromSupplicantAfterWps() {
            Map<String, WifiConfiguration> configs = new HashMap();
            int netId = -1;
            if (WifiStateMachine.this.mWifiNative.migrateNetworksFromSupplicant(configs, new SparseArray())) {
                for (Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                    WifiConfiguration config = (WifiConfiguration) entry.getValue();
                    config.networkId = -1;
                    NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, this.mSourceMessage.sendingUid);
                    if (!result.isSuccess()) {
                        WifiStateMachine.this.loge("Failed to add network after WPS: " + entry.getValue());
                        return Pair.create(Boolean.valueOf(false), Integer.valueOf(-1));
                    } else if (WifiStateMachine.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), true, this.mSourceMessage.sendingUid)) {
                        netId = result.getNetworkId();
                    } else {
                        Log.wtf(WifiStateMachine.TAG, "Failed to enable network after WPS: " + entry.getValue());
                        return Pair.create(Boolean.valueOf(false), Integer.valueOf(-1));
                    }
                }
                Boolean valueOf = Boolean.valueOf(true);
                if (configs.size() != 1) {
                    netId = -1;
                }
                return Pair.create(valueOf, Integer.valueOf(netId));
            }
            WifiStateMachine.this.loge("Failed to load networks from wpa_supplicant after Wps");
            return Pair.create(Boolean.valueOf(false), Integer.valueOf(-1));
        }

        public void exit() {
            if (WifiStateMachine.this.mIsRandomMacCleared) {
                WifiStateMachine.this.setRandomMacOui();
                WifiStateMachine.this.mIsRandomMacCleared = false;
            }
        }
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

    public WifiScoreReport getWifiScoreReport() {
        return this.mWifiScoreReport;
    }

    /* renamed from: lambda$-com_android_server_wifi_WifiStateMachine_13337 */
    /* synthetic */ void m93lambda$-com_android_server_wifi_WifiStateMachine_13337() {
        sendMessage(CMD_VENDOR_HAL_HWBINDER_DEATH);
    }

    public void onRssiThresholdBreached(byte curRssi) {
        if (this.mVerboseLoggingEnabled) {
            Log.e(TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
        }
        sendMessage(CMD_RSSI_THRESHOLD_BREACHED, curRssi);
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
                updateCapabilities();
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + startRssiMonitoringOffload(maxRssi, minRssi));
                break;
            }
        }
    }

    boolean isRoaming() {
        return this.mIsAutoRoaming;
    }

    int getPollRssiIntervalMsecs() {
        return this.mPollRssiIntervalMsecs;
    }

    void setPollRssiIntervalMsecs(int newPollIntervalMsecs) {
        this.mPollRssiIntervalMsecs = newPollIntervalMsecs;
    }

    public boolean isExtendingNetworkCoverage() {
        return this.mSoftApStateMachine != null ? this.mSoftApStateMachine.isExtendingNetworkCoverage() : false;
    }

    public boolean clearTargetBssid(String dbg) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (config == null) {
            return false;
        }
        String bssid = "any";
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        if (config.BSSID != "any") {
            this.mWifiConfigManager.clearConfiguredNetworkBssid(config.networkId);
        }
        return this.mWifiNative.setConfiguredNetworkBSSID(bssid);
    }

    public boolean clearCurrentConfigBSSID(String dbg) {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return false;
        }
        String bssid = "any";
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearCurrentConfigBSSID " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        if (config.BSSID != "any") {
            this.mWifiConfigManager.clearConfiguredNetworkBssid(config.networkId);
        }
        return true;
    }

    public boolean isThirdApp(int uid) {
        if (uid < 10000 || uid > 19999) {
            return false;
        }
        return true;
    }

    private String getBestBssidForNetId(int netId) {
        List<ScanResult> srList = syncGetScanResultsList();
        if (srList == null || srList.size() <= 0) {
            if (this.mVerboseLoggingEnabled) {
                logd("getBestBssidForNetId:srList is null or empty!!");
            }
            return null;
        } else if (this.mWifiConfigManager == null) {
            if (this.mVerboseLoggingEnabled) {
                logd("getBestBssidForNetId:mWifiConfigManager is null!!");
            }
            return null;
        } else {
            WifiConfiguration wConf = this.mWifiConfigManager.getWifiConfigurationForAll(netId);
            if (wConf == null) {
                if (this.mVerboseLoggingEnabled) {
                    logd("getBestBssidForNetId:wConf is null!!");
                }
                return null;
            }
            String configKey = wConf.configKey();
            if (configKey == null) {
                if (this.mVerboseLoggingEnabled) {
                    logd("getBestBssidForNetId:configKey is null!!");
                }
                return null;
            }
            int bestLevel = WifiConfiguration.INVALID_RSSI;
            String bssid = null;
            for (ScanResult sr : srList) {
                if (sr != null) {
                    String srConfigKey = WifiConfiguration.configKey(sr);
                    if (srConfigKey != null && srConfigKey.equals(configKey) && sr.level > bestLevel) {
                        bssid = sr.BSSID;
                        bestLevel = sr.level;
                    }
                }
            }
            if (bssid != null) {
                int sameBssidCount = 0;
                for (ScanResult sr2 : srList) {
                    if (sr2 != null && bssid.equals(sr2.BSSID)) {
                        sameBssidCount++;
                    }
                }
                if (sameBssidCount > 1) {
                    bssid = null;
                }
            }
            if (this.mVerboseLoggingEnabled) {
                logd("getBestBssidForNetId bssid = " + bssid);
            }
            return bssid;
        }
    }

    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "setTargetBssid set to " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
        return true;
    }

    public SoftApStateMachine getSoftApStateMachine() {
        return this.mSoftApStateMachine;
    }

    public void setStaSoftApConcurrency(boolean enable) {
        if (enable && this.mSoftApStateMachine == null) {
            this.mSoftApStateMachine = new SoftApStateMachine(this.mContext, this.mWifiInjector, this.mWifiNative, this.mNwService, this.mBatteryStats);
            logd("mSoftApStateMachine is created");
        }
        this.mStaAndAPConcurrency = enable;
        this.mWifiNative.setStaSoftApConcurrency(enable);
        logd("set StaAndAPConcurrency = " + enable);
    }

    public void setNewSapInterface(String intf) {
        this.mSapInterfaceName = intf;
    }

    public void cleanup() {
        this.mWifiMonitor.stopAllMonitoring();
        this.mDeathRecipient.unlinkToDeath();
        this.mWifiNative.tearDown();
        if (!this.mDataInterfaceName.equals(this.mInterfaceName)) {
            try {
                this.mNwService.setInterfaceDown(this.mDataInterfaceName);
                this.mNwService.clearInterfaceAddresses(this.mDataInterfaceName);
            } catch (RemoteException re) {
                loge("Unable to change interface settings: " + re);
            } catch (IllegalStateException ie) {
                loge("Unable to change interface settings: " + ie);
            }
        }
    }

    private void staCleanup() {
        boolean skipUnload = false;
        if (this.mStaAndAPConcurrency) {
            int wifiApState = this.mSoftApStateMachine.syncGetWifiApState();
            if (wifiApState == 12 || wifiApState == 13) {
                log("Avoid unloading driver, AP_STATE is enabled/enabling");
                skipUnload = true;
            }
        }
        if (skipUnload || (this.mStartApPending ^ 1) == 0) {
            this.mWifiMonitor.stopAllMonitoring();
            this.mDeathRecipient.unlinkToDeath();
            this.mWifiNative.tearDownSta();
        } else {
            cleanup();
        }
        this.mIsFullScanOngoing = true;
    }

    public int getOperationalMode() {
        return this.mOperationalMode;
    }

    public void setDualSapMode(boolean enable) {
        String bridgeInterface;
        this.mDualSapMode = enable;
        if (enable) {
            bridgeInterface = this.mWifiApConfigStore.getBridgeInterface();
        } else {
            bridgeInterface = this.mWifiApConfigStore.getSapInterface();
        }
        setNewSapInterface(bridgeInterface);
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

    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
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
        RuntimeException excp = new RuntimeException("Please send this log to Yuanliu.Tang of wifi team,thank you!");
        excp.fillInStackTrace();
        this.mAssertProxy.requestShowAssertMessage(Log.getStackTraceString(excp));
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

    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = wifiNative;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWrongPasswordNotifier = wrongPasswordNotifier;
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
        updateDataInterface();
        this.mWifiRomUpdateHelper = new WifiRomUpdateHelper(this.mContext);
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mWifiStateTracker = wifiInjector.getWifiStateTracker();
        this.mNwService = INetworkManagementService.Stub.asInterface(this.mFacade.getService("network_management"));
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        this.mPasspointManager = this.mWifiInjector.getPasspointManager();
        this.mWifiMonitor = this.mWifiInjector.getWifiMonitor();
        this.mOppoWifiConnectionAlert = new OppoWifiConnectionAlert(this.mContext, this, this.mWifiConfigManager);
        this.mWifiInfo = new WifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mFacade.setWifiConfigManagerStatemachine(this.mWifiConfigManager, this);
        this.mLinkProperties = new LinkProperties();
        this.mPhoneStateListener = new WifiPhoneStateListener(looper);
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mIpManager = this.mFacade.makeIpManager(this.mContext, this.mDataInterfaceName, new IpManagerCallback());
        this.mIpManager.setMulticastFilter(true);
        this.mNoNetworksPeriodicScan = this.mContext.getResources().getInteger(17694918);
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039716);
        this.mCountryCode = countryCode;
        this.mWifiScoreReport = new WifiScoreReport(this.mContext, this.mWifiConfigManager, this.mClock);
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
                WifiStateMachine.this.sendMessage(131206);
            }
        }, new IntentFilter("android.intent.action.LOCKED_BOOT_COMPLETED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(WifiStateMachine.TAG, "receive loadFromStore broadcast!!!");
                new Thread() {
                    public void run() {
                        WifiStateMachine.this.mWifiConfigManager.loadFromStore();
                    }
                }.start();
            }
        }, new IntentFilter(ACTION_LOAD_FROM_STORE));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra(WifiStateMachine.EXTRA_NETWORK_STATE, false)) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_MTU_PROBER);
                }
            }
        }, new IntentFilter(ACTION_WIFI_NETWORK_STATE));
        this.mAssertProxy = OppoAssertTip.getInstance();
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                wifiStateMachine.mAutoSwitch = z;
                if (WifiStateMachine.this.mWifiNetworkAvailable != null) {
                    WifiStateMachine.this.mWifiNetworkAvailable.setAutoSwitch(WifiStateMachine.this.mAutoSwitch);
                } else if (WifiStateMachine.this.mWifiNetworkStateTraker != null) {
                    WifiStateMachine.this.mWifiNetworkStateTraker.setAutoSwitch(WifiStateMachine.this.mAutoSwitch);
                }
                Log.d(WifiStateMachine.TAG, "onChange mAutoSwitch= " + WifiStateMachine.this.mAutoSwitch);
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
                            WifiStateMachine.this.mWifiNetworkStateTraker.enableVerboseLogging(WifiStateMachine.this.mVerboseLoggingEnabled ? 1 : 0);
                            wifiStateMachine = WifiStateMachine.this;
                            if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                                z = false;
                            }
                            wifiStateMachine.mAutoSwitch = z;
                            WifiStateMachine.this.mWifiNetworkStateTraker.setAutoSwitch(WifiStateMachine.this.mAutoSwitch);
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
                        int i;
                        WifiStateMachine.this.mWifiNetworkAvailable = WifiStateMachine.this.makeWifiNetworkAvailable();
                        WifiStateMachine.this.mWifiNetworkAvailable.setFeature(true);
                        OppoWifiNetworkSwitchEnhance -get101 = WifiStateMachine.this.mWifiNetworkAvailable;
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        -get101.enableVerboseLogging(i);
                        wifiStateMachine = WifiStateMachine.this;
                        if (Global.getInt(WifiStateMachine.this.mContext.getContentResolver(), WifiStateMachine.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                            z = false;
                        }
                        wifiStateMachine.mAutoSwitch = z;
                        WifiStateMachine.this.mWifiNetworkAvailable.setAutoSwitch(WifiStateMachine.this.mAutoSwitch);
                    }
                }
            }
        });
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039718);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957070);
        this.mThresholdQualifiedRssi24 = context.getResources().getInteger(17694912);
        this.mThresholdQualifiedRssi5 = context.getResources().getInteger(17694913);
        this.mThresholdSaturatedRssi24 = context.getResources().getInteger(17694910);
        this.mThresholdSaturatedRssi5 = context.getResources().getInteger(17694911);
        this.mThresholdMinimumRssi5 = context.getResources().getInteger(17694905);
        this.mThresholdMinimumRssi24 = context.getResources().getInteger(17694904);
        this.mEnableLinkDebouncing = this.mContext.getResources().getBoolean(17957066);
        this.mEnableVoiceCallSarTxPowerLimit = this.mContext.getResources().getBoolean(17957071);
        this.mEnableChipWakeUpWhenAssociated = true;
        this.mEnableRssiPollWhenAssociated = true;
        this.mDisconnectOnlyOnInitialIpReachability = this.mContext.getResources().getBoolean(17957063);
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSupplicantStartingState, this.mDefaultState);
        addState(this.mSupplicantStartedState, this.mDefaultState);
        addState(this.mScanModeState, this.mSupplicantStartedState);
        addState(this.mConnectModeState, this.mSupplicantStartedState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mCaptiveState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        addState(this.mFilsState, this.mConnectModeState);
        addState(this.mWaitForP2pDisableState, this.mSupplicantStartedState);
        addState(this.mSupplicantStoppingState, this.mDefaultState);
        addState(this.mSoftApState, this.mDefaultState);
        setInitialState(this.mInitialState);
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        HandlerThread handlerThread = new HandlerThread("CheckInternetAccess");
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper());
        start();
        handleScreenStateChanged(powerManager.isInteractive());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.PNO_SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147457, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147458, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SELECT_NETWORK_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SAVE_CONFIG_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_TEMP_DISABLED, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.FILS_NETWORK_CONNECTION_EVENT, getHandler());
        if (isWlanAssistantEnable()) {
            this.mWifiNetworkStateTraker = new OppoWifiAssistantStateTraker(context, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler());
            this.mAutoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
            this.mWifiNetworkStateTraker.setAutoSwitch(this.mAutoSwitch);
        } else {
            this.mWifiNetworkAvailable = new OppoWifiNetworkSwitchEnhance(context, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker);
            this.mAutoSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
            this.mWifiNetworkAvailable.setAutoSwitch(this.mAutoSwitch);
        }
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        intent.putExtra("scan_enabled", 1);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        int verboseLoggingLevel = SystemProperties.getBoolean(DEBUG_PROPERTY, false) ? 1 : 0;
        if (this.mWifiDiagnostics != null) {
            enableVerboseLogging(verboseLoggingLevel);
        }
        this.mWifiCfgUpdateHelper = new OppoWifiCfgUpdateHelper(this.mContext);
        OppoAutoConnectManager.init(this.mContext, this, this.mWifiConfigManager, this.mWifiNetworkStateTraker, this.mWifiNative, this.mWifiRomUpdateHelper);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        OppoManuConnectManager.init(this.mContext, this, this.mWifiConfigManager, this.mWifiRomUpdateHelper);
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
    }

    private void stopIpManager() {
        handlePostDhcpSetup();
        this.mIpManager.stop();
        this.mIsIpManagerStarted = false;
    }

    public void setWifiDiagnostics(BaseWifiDiagnostics WifiDiagnostics) {
        this.mWifiDiagnostics = WifiDiagnostics;
    }

    public void setTrafficPoller(WifiTrafficPoller trafficPoller) {
        this.mTrafficPoller = trafficPoller;
        if (this.mTrafficPoller != null) {
            this.mTrafficPoller.setInterface(this.mDataInterfaceName);
        }
    }

    PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return this.mFacade.getBroadcast(this.mContext, requestCode, intent, 0);
    }

    void setSupplicantLogLevel() {
        this.mWifiNative.setSupplicantLogLevel(this.mVerboseLoggingEnabled);
    }

    public void enableVerboseLogging(int verbose) {
        int debug = SystemProperties.getInt("vendor.qcom.wifi.debug", 0);
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS : DEFAULT_POLL_RSSI_INTERVAL_MSECS);
        } else {
            this.mVerboseLoggingEnabled = false;
            setLogRecSize(100);
        }
        configureVerboseHalLogging(this.mVerboseLoggingEnabled);
        setSupplicantLogLevel();
        this.mCountryCode.enableVerboseLogging(verbose);
        this.mWifiScoreReport.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        if (this.wifiP2pServiceImpl != null) {
            this.wifiP2pServiceImpl.enableVerboseLogging(verbose);
        }
        this.mWifiMonitor.enableVerboseLogging(verbose);
        this.mWifiNative.enableVerboseLogging(verbose);
        this.mWifiConfigManager.enableVerboseLogging(verbose);
        this.mSupplicantStateTracker.enableVerboseLogging(verbose);
        this.mOppoWifiConnectionAlert.enableVerboseLogging(verbose);
        if (this.mWifiNetworkAvailable != null) {
            this.mWifiNetworkAvailable.enableVerboseLogging(verbose);
        } else if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.enableVerboseLogging(verbose);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(verbose);
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().enableVerboseLogging(verbose);
        }
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.enableVerboseLogging(verbose);
        }
        if (this.mStaAndAPConcurrency) {
            this.mSoftApStateMachine.enableVerboseLogging(verbose);
        }
        if (this.mWifiScanner != null) {
            this.mWifiScanner.enableVerboseLogging(verbose);
        }
        if (this.mWifiConnectivityManager != null) {
            Log.d(TAG, "set debug log for WifiConnectivityManager" + debug);
            this.mWifiConnectivityManager.enableVerboseLogging(debug);
        }
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    int getAggressiveHandover() {
        return this.mAggressiveHandover;
    }

    void enableAggressiveHandover(int enabled) {
        this.mAggressiveHandover = enabled;
    }

    public void clearANQPCache() {
    }

    public void setAllowScansWithTraffic(int enabled) {
        this.mAlwaysEnableScansWhileAssociated = enabled;
    }

    public int getAllowScansWithTraffic() {
        return this.mAlwaysEnableScansWhileAssociated;
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        sendMessage(CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED, enabled ? 1 : 0);
        return true;
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mEnableAutoJoinWhenAssociated;
    }

    private void updateDataInterface() {
        int fstEnabled = SystemProperties.getInt("persist.vendor.fst.rate.upgrade.en", 0);
        String prevDataInterfaceName = this.mDataInterfaceName;
        String rateUpgradeDataInterfaceName = SystemProperties.get("persist.vendor.fst.data.interface", "bond0");
        if (fstEnabled != 1) {
            rateUpgradeDataInterfaceName = this.mInterfaceName;
        }
        this.mDataInterfaceName = rateUpgradeDataInterfaceName;
        if (!this.mDataInterfaceName.equals(prevDataInterfaceName)) {
            logd("fst " + (fstEnabled == 1 ? "enabled" : "disabled"));
            if (this.mIpManager != null) {
                this.mIpManager.shutdown();
                this.mIpManager = this.mFacade.makeIpManager(this.mContext, this.mDataInterfaceName, new IpManagerCallback());
                this.mIpManager.setMulticastFilter(true);
            }
            if (this.mTrafficPoller != null) {
                this.mTrafficPoller.setInterface(this.mDataInterfaceName);
            }
        }
    }

    private boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039717);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[]{(byte) (Integer.parseInt(ouiParts[0], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[1], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[2], 16) & Constants.BYTE_MASK)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    private boolean clearRandomMacOui() {
        byte[] ouiBytes = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    private int lookupFrameworkNetworkId(int supplicantNetworkId) {
        return this.mWifiNative.getFrameworkNetworkId(supplicantNetworkId);
    }

    private boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
        logd("connectToUserSelectNetwork netId " + netId + ", uid " + uid + ", forceReconnect = " + forceReconnect);
        if (this.mWifiConfigManager.getConfiguredNetwork(netId) == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (this.mWifiConfigManager.enableNetwork(netId, true, uid) && (this.mWifiConfigManager.checkAndUpdateLastConnectUid(netId, uid) ^ 1) == 0) {
            this.mWifiConnectivityManager.setUserConnectChoice(netId);
        } else {
            logi("connectToUserSelectNetwork Allowing uid " + uid + " with insufficient permissions to connect=" + netId);
        }
        if (forceReconnect || this.mWifiInfo.getNetworkId() != netId) {
            this.mWifiConnectivityManager.prepareForForcedConnection(netId);
            startConnectToNetwork(netId, uid, "any");
        } else {
            logi("connectToUserSelectNetwork already connecting/connected=" + netId);
            if ((getCurrentState() == this.mConnectedState || getCurrentState() == this.mObtainingIpState) && this.mWifiConfigManager != null) {
                this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, this.mWifiConfigManager.getConfiguredNetwork(netId), -1, SupplicantState.COMPLETED);
            }
        }
        return true;
    }

    public void prepareForForcedConnection(int netId) {
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.prepareForForcedConnection(netId);
        }
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    public void startScan(int callingUid, int scanCounter, ScanSettings settings, WorkSource workSource) {
        if (!this.mRejectScanWhenRxSensTest) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, settings);
            bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, workSource);
            bundle.putLong(SCAN_REQUEST_TIME, this.mClock.getWallClockMillis());
            sendMessage(CMD_START_SCAN, callingUid, scanCounter, bundle);
            if (!(this.mScreenOn || callingUid <= 0 || callingUid == 1010 || callingUid == OppoManuConnectManager.UID_DEFAULT)) {
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
    }

    public long getDisconnectedTimeMilli() {
        if (getCurrentState() != this.mDisconnectedState || this.mDisconnectedTimeStamp == 0) {
            return 0;
        }
        return this.mClock.getWallClockMillis() - this.mDisconnectedTimeStamp;
    }

    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = this.mClock.getWallClockMillis();
        if (this.lastConnectAttemptTimestamp == 0 || now - this.lastConnectAttemptTimestamp >= 10000) {
            return true;
        }
        sendMessageDelayed(Message.obtain(msg), 11000 - (now - this.lastConnectAttemptTimestamp));
        return false;
    }

    String reportOnTime() {
        long now = this.mClock.getWallClockMillis();
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
            sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf(period)}));
            on = this.mOnTime - this.mOnTimeScreenStateChange;
            period = (int) (now - this.lastScreenStateChangeTimeStamp);
            sb.append(String.format(" from screen [on:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(period)}));
            return sb.toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "reportOnTime fatal exception, on=" + on + " tx=" + tx + " rx=" + rx + " period=" + period);
            log(e.toString());
            return "";
        }
    }

    WifiLinkLayerStats getWifiLinkLayerStats() {
        WifiLinkLayerStats stats = null;
        if (this.mWifiLinkLayerStatsSupported > 0) {
            String name = "wlan0";
            stats = this.mWifiNative.getWifiLinkLayerStats(name);
            if (name != null && stats == null && this.mWifiLinkLayerStatsSupported > 0) {
                this.mWifiLinkLayerStatsSupported--;
            } else if (stats != null) {
                this.lastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
                this.mOnTime = stats.on_time;
                this.mTxTime = stats.tx_time;
                this.mRxTime = stats.rx_time;
                this.mRunningBeaconCount = stats.beacon_rx;
            }
        }
        if (stats == null || this.mWifiLinkLayerStatsSupported <= 0) {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mDataInterfaceName), this.mFacade.getRxPackets(this.mDataInterfaceName));
        } else {
            this.mWifiInfo.updatePacketRates(stats, this.lastLinkLayerStatsUpdate);
        }
        return stats;
    }

    int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        int ret = this.mWifiNative.startSendingOffloadedPacket(slot, packetData, intervalSeconds * OppoManuConnectManager.UID_DEFAULT);
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
        if (startScanNative(freqs, this.mWifiConfigManager.retrieveHiddenNetworkList(), parcelable)) {
            if (freqs == null) {
                this.mBufferedScanMsg.clear();
            }
            this.messageHandlingStatus = 1;
            return;
        }
        if (!this.mIsScanOngoing) {
            if (this.mBufferedScanMsg.size() > 0) {
                sendMessage((Message) this.mBufferedScanMsg.remove());
            }
            this.messageHandlingStatus = -5;
        } else if (this.mIsFullScanOngoing) {
            this.messageHandlingStatus = -2;
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

    private boolean startScanNative(Set<Integer> freqs, List<HiddenNetwork> hiddenNetworkList, WorkSource workSource) {
        boolean z;
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
        settings.hiddenNetworks = (HiddenNetwork[]) hiddenNetworkList.toArray(new HiddenNetwork[hiddenNetworkList.size()]);
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
        if (freqs == null) {
            z = true;
        } else {
            z = false;
        }
        this.mIsFullScanOngoing = z;
        this.lastScanFreqs = freqs;
        return true;
    }

    public void setSupplicantRunning(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_SUPPLICANT);
            return;
        }
        this.mDisconnectDelayDuration = -1;
        try {
            this.mDisconnectDelayDuration = Secure.getInt(this.mContext.getContentResolver(), "wifi_disconnect_delay_duration", 0);
        } catch (NumberFormatException e) {
            this.mDisconnectDelayDuration = 0;
            Log.e(TAG, " get mDisconnectDelayDuration caught exception ");
        }
        if (this.mDisconnectDelayDuration <= 0 || this.mNetworkInfo.getState() != NetworkInfo.State.CONNECTED) {
            sendMessage(CMD_STOP_SUPPLICANT);
            return;
        }
        this.mContext.sendBroadcastAsUser(new Intent("wifi_disconnect_in_progress"), UserHandle.ALL);
        Log.e(TAG, " Disconnection delayed by  " + this.mDisconnectDelayDuration + " seconds");
        sendMessageDelayed(CMD_STOP_SUPPLICANT, (long) (this.mDisconnectDelayDuration * OppoManuConnectManager.UID_DEFAULT));
    }

    private void setHostApRunningPre(SoftApModeConfiguration wificonfig, boolean enable) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "setHostApRunningPre:" + enable);
        }
        this.mStartApPending = enable;
    }

    public void setHostApRunning(SoftApModeConfiguration wifiConfig, boolean enable) {
        setHostApRunningPre(wifiConfig, enable);
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
        if (this.mStaAndAPConcurrency) {
            return this.mSoftApStateMachine.syncGetWifiApState();
        }
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
            case 14:
                return "failed";
            default:
                return "[invalid state]";
        }
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public boolean isDisconnected() {
        if (getCurrentState() == this.mDisconnectedState || getCurrentState() == this.mFilsState) {
            return true;
        }
        return false;
    }

    public boolean isSupplicantTransientState() {
        SupplicantState supplicantState = this.mWifiInfo.getSupplicantState();
        if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.AUTHENTICATING || supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Supplicant is under transient state: " + supplicantState);
            }
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Supplicant is under steady state: " + supplicantState);
        }
        return false;
    }

    public boolean isLinkDebouncing() {
        return this.mIsLinkDebouncing;
    }

    public WifiInfo syncRequestConnectionInfo(String callingPackage) {
        int uid = Binder.getCallingUid();
        WifiInfo result = new WifiInfo(this.mWifiInfo);
        if (uid == Process.myUid()) {
            return result;
        }
        boolean hideBssidAndSsid = true;
        result.setMacAddress("02:00:00:00:00:00");
        try {
            if (AppGlobals.getPackageManager().checkUidPermission("android.permission.LOCAL_MAC_ADDRESS", uid) == 0) {
                result.setMacAddress(this.mWifiInfo.getMacAddress());
            }
            if (this.mWifiPermissionsUtil.canAccessFullConnectionInfo(getCurrentWifiConfiguration(), callingPackage, uid, 26)) {
                hideBssidAndSsid = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error checking receiver permission", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Security exception checking receiver permission", e2);
        }
        if ((!this.mEnableRssiPolling || result.getRssi() == WifiMetrics.MIN_RSSI_DELTA) && result.getBSSID() != null) {
            for (ScanDetail scanResult : this.mScanResults) {
                if (result.getBSSID().equals(scanResult.getScanResult().BSSID)) {
                    int level = scanResult.getScanResult().level;
                    Log.d(TAG, "Adjust rssi from " + result.getRssi() + " to " + level);
                    result.setRssi(level);
                    break;
                }
            }
        }
        if (hideBssidAndSsid) {
            result.setBSSID("02:00:00:00:00:00");
            result.setSSID(WifiSsid.createFromHex(null));
        }
        return result;
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

    public void setOperationalMode(int mode) {
        if (this.mVerboseLoggingEnabled) {
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
            for (ScanDetail result : this.mScanResults) {
                scanList.add(new ScanResult(result.getScanResult()));
            }
        }
        return scanList;
    }

    public ScanResult getScanResultForBssid(String bssid) {
        synchronized (this.mScanResultsLock) {
            for (ScanDetail result : this.mScanResults) {
                ScanResult scanRes = result.getScanResult();
                if (scanRes.BSSID.equals(bssid)) {
                    return scanRes;
                }
            }
            return null;
        }
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString(EXTRA_OSU_ICON_QUERY_FILENAME, fileName);
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
        sendMessage(CMD_DISCONNECT);
    }

    public void disconnectCommand(int uid, int reason) {
        sendMessage(CMD_DISCONNECT, uid, reason);
    }

    public void reconnectCommand(WorkSource workSource) {
        sendMessage(CMD_RECONNECT, workSource);
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
        if (resultMsg == null) {
            return null;
        }
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
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult);
        WifiConfiguration config = resultMsg.obj;
        resultMsg.recycle();
        return config;
    }

    public List<OsuProvider> syncGetMatchingOsuProviders(ScanResult scanResult, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_OSU_PROVIDERS, scanResult);
        List<OsuProvider> providers = resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG, uid, 0, config);
        boolean result = resultMsg.arg1 == 1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_PASSPOINT_CONFIG, fqdn);
        boolean result = resultMsg.arg1 == 1;
        resultMsg.recycle();
        return result;
    }

    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PASSPOINT_CONFIGS);
        List<PasspointConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
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
        if (this.mPropertyService.getBoolean("config.disable_rtt", false)) {
            return supportedFeatureSet & -385;
        }
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
        boolean result = resultMsg.what != 151570;
        resultMsg.recycle();
        return result;
    }

    public String syncGetCurrentNetworkWpsNfcConfigurationToken() {
        return this.mWifiNative.getCurrentNetworkWpsNfcConfigurationToken();
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

    public synchronized void resetSimAuthNetworks(boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent ? 1 : 0);
    }

    public Network getCurrentNetwork() {
        if (this.mNetworkAgent != null) {
            return new Network(this.mNetworkAgent.netId);
        }
        return null;
    }

    public void enableTdls(String remoteMacAddress, boolean enable) {
        sendMessage(CMD_ENABLE_TDLS, enable ? 1 : 0, 0, remoteMacAddress);
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
        if (this.mCountryCode.getCountryCodeSentToDriver() != null) {
            pw.println("CountryCode sent to driver " + this.mCountryCode.getCountryCodeSentToDriver());
        } else if (this.mCountryCode.getCountryCode() != null) {
            pw.println("CountryCode: " + this.mCountryCode.getCountryCode() + " was not sent to driver");
        } else {
            pw.println("CountryCode was not initialized");
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
        this.mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        this.mPasspointManager.dump(pw);
        pw.println();
        this.mWifiDiagnostics.captureBugReportData(7);
        this.mWifiDiagnostics.dump(fd, pw, args);
        dumpIpManager(fd, pw, args);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.dump(fd, pw, args);
        } else {
            pw.println("mWifiConnectivityManager is not initialized");
        }
    }

    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    public void handleUserUnlock(int userId) {
        sendMessage(CMD_USER_UNLOCK, userId);
    }

    public void handleUserStop(int userId) {
        sendMessage(CMD_USER_STOP, userId);
    }

    private void logStateAndMessage(Message message, State state) {
        this.messageHandlingStatus = 0;
        if (this.mVerboseLoggingEnabled) {
            logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
        }
    }

    protected boolean recordLogRec(Message msg) {
        switch (msg.what) {
            case CMD_RSSI_POLL /*131155*/:
                return this.mVerboseLoggingEnabled;
            default:
                return true;
        }
    }

    protected String getLogRecString(Message msg) {
        if (!this.mVerboseLoggingEnabled) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (this.mScreenOn) {
            sb.append("!");
        }
        if (this.messageHandlingStatus != 0) {
            sb.append("(").append(this.messageHandlingStatus).append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=").append(msg.sendingUid);
        }
        sb.append(" rt=").append(this.mClock.getUptimeSinceBootMillis());
        sb.append("/").append(this.mClock.getElapsedSinceBootMillis());
        WifiConfiguration config;
        String key;
        Long now;
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
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                config = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null && (key == null || (config.configKey().equals(key) ^ 1) != 0)) {
                    sb.append(" target=").append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=").append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_START_SCAN /*131143*/:
                now = Long.valueOf(this.mClock.getWallClockMillis());
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
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
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
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                }
                if (this.mWifiScoreReport.isLastReportValid()) {
                    sb.append(this.mWifiScoreReport.getLastReport());
                    break;
                }
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
            case CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.mDisableP2pWatchdogCount);
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
                sb.append(Integer.toString(this.mFacade.getIntegerSetting(this.mContext, "wifi_max_dhcp_retry_count", 0)));
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
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
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                break;
            case CMD_START_CONNECT /*131215*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                        break;
                    }
                }
                break;
            case CMD_START_ROAM /*131217*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = msg.obj;
                if (result != null) {
                    now = Long.valueOf(this.mClock.getWallClockMillis());
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
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                sb.append(" fail count=").append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_IP_REACHABILITY_LOST /*131221*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
            case CMD_RSSI_THRESHOLD_BREACHED /*131236*/:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
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
            case CMD_USER_SWITCH /*131277*/:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_IP_REACHABILITY_SESSION_END /*131326*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                    break;
                }
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
            case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
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
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
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
                if (isLinkDebouncing()) {
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
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                sb.append(String.format(" con=%d", new Object[]{Integer.valueOf(this.mConnectionReqCount)}));
                sb.append(String.format(" untrustedcn=%d", new Object[]{Integer.valueOf(this.mUntrustedReqCount)}));
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
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
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                sb.append(" ");
                sb.append(" timedOut=").append(Integer.toString(msg.arg1));
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
                config = (WifiConfiguration) msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
                    sb.append(" ajst=").append(config.getNetworkSelectionStatus().getNetworkStatusString());
                    break;
                }
                break;
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (!(config.preSharedKey == null || (config.preSharedKey.equals("*") ^ 1) == 0)) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
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
        if (this.mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        if (screenOn) {
            if (this.mVerboseLoggingEnabled) {
                log("force to scan when screen is on---");
            }
            startScan(-1, -1, null, null);
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
            if (this.mScreenOffTime - this.mScreenOnTime > 20000) {
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
        getWifiLinkLayerStats();
        this.mOnTimeScreenStateChange = this.mOnTime;
        this.lastScreenStateChangeTimeStamp = this.lastLinkLayerStatsUpdate;
        this.mWifiMetrics.setScreenState(screenOn);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        }
        if (this.mVerboseLoggingEnabled) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private void setSuspendOptimizationsNative(int reason, boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            log("setSuspendOptimizationsNative: " + reason + " " + enabled + " -want " + this.mUserWantsSuspendOpt.get() + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
            if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
                if (this.mVerboseLoggingEnabled) {
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
        if (this.mVerboseLoggingEnabled) {
            log("setSuspendOptimizations: " + reason + " " + enabled);
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
        } else {
            this.mSuspendOptNeedsDisabled |= reason;
        }
        if (this.mVerboseLoggingEnabled) {
            log("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        }
    }

    private void setWifiState(int wifiState) {
        int previousWifiState = this.mWifiState.get();
        if (previousWifiState == wifiState) {
            loge("Don't set same state " + wifiState);
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
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleWifiStateChanged(wifiState);
        }
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.updateWifiState(wifiState);
        }
        this.mOppoWifiConnectionAlert.setWifiState(wifiState);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiStateChangedEvt(wifiState);
        }
        if (this.mVerboseLoggingEnabled) {
            log("setWifiState: " + syncGetWifiStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiState);
        intent.putExtra("previous_wifi_state", previousWifiState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setWifiApState(int wifiApState, int reason, String ifaceName, int mode) {
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
        if (wifiApState == 11 || wifiApState == 14) {
            boolean skipUnload = false;
            int wifiState = syncGetWifiState();
            int operMode = getOperationalMode();
            if (wifiState == 2 || wifiState == 3 || operMode == 3) {
                Log.d(TAG, "Avoid unload driver, WIFI_STATE is enabled/enabling");
                skipUnload = true;
            }
            if (skipUnload) {
                this.mWifiNative.tearDownAp();
            } else {
                cleanup();
            }
        }
        this.mWifiApState.set(wifiApState);
        if (this.mVerboseLoggingEnabled) {
            log("setWifiApState: " + syncGetWifiApStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        if (wifiApState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        if (ifaceName == null) {
            loge("Updating wifiApState with a null iface name");
        }
        intent.putExtra("wifi_ap_interface_name", ifaceName);
        intent.putExtra("wifi_ap_mode", mode);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setScanResults(int message) {
        ArrayList<ScanDetail> scanResults;
        this.mNumScanResultsKnown = 0;
        this.mNumScanResultsReturned = 0;
        if (message == WifiMonitor.PNO_SCAN_RESULTS_EVENT) {
            scanResults = this.mWifiNative.getPnoScanResults();
        } else {
            scanResults = this.mWifiNative.getScanResults();
        }
        if (scanResults.isEmpty()) {
            this.mScanResults = new ArrayList();
            return;
        }
        if (this.mLastBssid != null) {
            try {
                long activeBssid = Utils.parseMac(this.mLastBssid);
            } catch (IllegalArgumentException e) {
            }
        }
        synchronized (this.mScanResultsLock) {
            this.mScanResults = scanResults;
            this.mNumScanResultsReturned = this.mScanResults.size();
            if (OppoAutoConnectManager.getInstance() != null) {
                OppoAutoConnectManager.getInstance().sendScanResultEvt();
            }
            this.mLastScanTime = System.currentTimeMillis();
            if (this.mWifiNetworkAvailable != null) {
                this.mWifiNetworkAvailable.detectScanResult(this.mLastScanTime);
            } else if (this.mWifiNetworkStateTraker != null) {
                this.mWifiNetworkStateTraker.detectScanResult(this.mLastScanTime);
            }
        }
        if (!this.mScreenOn) {
            this.mIdleScanTimes++;
        }
        if (isLinkDebouncing()) {
            sendMessage(CMD_START_ROAM, this.mLastNetworkId, 1, null);
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
        SignalPollResult pollResult = this.mWifiNative.signalPoll();
        if (pollResult != null) {
            Integer newRssi = Integer.valueOf(pollResult.currentRssi);
            Integer newLinkSpeed = Integer.valueOf(pollResult.txBitrate);
            Integer newFrequency = Integer.valueOf(pollResult.associationFrequency);
            if (this.mVerboseLoggingEnabled) {
                logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " linkspeed=" + newLinkSpeed + " freq=" + newFrequency);
            }
            if (newRssi == null || newRssi.intValue() <= WifiMetrics.MIN_RSSI_DELTA || newRssi.intValue() >= ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS) {
                this.mWifiInfo.setRssi(WifiMetrics.MIN_RSSI_DELTA);
                updateCapabilities();
            } else {
                if (newRssi.intValue() > 0) {
                    newRssi = Integer.valueOf(newRssi.intValue() - 256);
                }
                this.originRssi = newRssi.intValue();
                newRssi = Integer.valueOf(getFilterRssi(newRssi.intValue()));
                this.mWifiInfo.setRssi(newRssi.intValue());
                int newSignalLevel = WifiManager.calculateSignalLevel(newRssi.intValue(), 5);
                if (newSignalLevel != this.mLastSignalLevel) {
                    updateCapabilities();
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
                this.mLastSignalLevel = newSignalLevel;
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
                this.mWifiInfo.setFrequency(newFrequency.intValue());
            }
            this.mWifiConfigManager.updateScanDetailCacheFromWifiInfo(this.mWifiInfo);
            if (!(newRssi == null || newLinkSpeed == null || newFrequency == null)) {
                this.mWifiMetrics.handlePollResult(this.mWifiInfo);
            }
        }
    }

    private void cleanWifiScore() {
        this.mWifiInfo.txBadRate = 0.0d;
        this.mWifiInfo.txSuccessRate = 0.0d;
        this.mWifiInfo.txRetriesRate = 0.0d;
        this.mWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
    }

    private void updateLinkProperties(LinkProperties newLp) {
        if (this.mVerboseLoggingEnabled) {
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
        if (this.mVerboseLoggingEnabled) {
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
                    if (this.mVerboseLoggingEnabled) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :" + gateway.getHostAddress());
                    }
                    address = macAddressFromRoute(gateway.getHostAddress());
                    if (address == null && timeout > 0) {
                        boolean reachable = false;
                        TrafficStats.setThreadStatsTag(-190);
                        try {
                            reachable = gateway.isReachable(timeout);
                            if (reachable) {
                                address = macAddressFromRoute(gateway.getHostAddress());
                                if (this.mVerboseLoggingEnabled) {
                                    logd("updateDefaultRouteMacAddress reachable (tried again) :" + gateway.getHostAddress() + " found " + address);
                                }
                            }
                        } catch (Exception e) {
                            loge("updateDefaultRouteMacAddress exception reaching :" + gateway.getHostAddress());
                        } finally {
                            TrafficStats.clearThreadStatsTag();
                        }
                    }
                    if (address != null) {
                        this.mWifiConfigManager.setNetworkDefaultGwMacAddress(this.mLastNetworkId, address);
                    }
                }
            }
        }
        return address;
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
        if (this.mWifiNetworkStateTraker == null || (this.mWifiNetworkStateTraker.getIsOppoManuConnect() ^ 1) == 0 || !this.mAutoSwitch || this.mNetworkInfo.getDetailedState() == DetailedState.IDLE || this.mNetworkInfo.getDetailedState() == DetailedState.SCANNING || this.mNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED || this.mNetworkInfo.getDetailedState() == DetailedState.FAILED || this.mNetworkInfo.getDetailedState() == DetailedState.BLOCKED || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
            boolean wlanAssistAutoConnect = false;
            boolean replacePending = false;
            if (!(this.mWifiNetworkStateTraker == null || (this.mWifiNetworkStateTraker.getIsOppoManuConnect() ^ 1) == 0 || !this.mAutoSwitch)) {
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
                fetchRssiLinkSpeedAndFrequencyNative();
                WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
                sentWifiInfo.setMacAddress("02:00:00:00:00:00");
                intent.putExtra("wifiInfo", sentWifiInfo);
            }
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (OppoAutoConnectManager.getInstance() != null) {
                OppoAutoConnectManager.getInstance().sendNetworkStateChangedEvt(intent);
            }
            return;
        }
        log("state is " + this.mNetworkInfo.getDetailedState() + ",not bc.");
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
        if (isLinkDebouncing() || this.mIsAutoRoaming) {
            hidden = true;
        }
        if (this.mVerboseLoggingEnabled) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (!(this.mNetworkInfo.getExtraInfo() == null || this.mWifiInfo.getSSID() == null || (this.mWifiInfo.getSSID().equals("<unknown ssid>") ^ 1) == 0 || this.mNetworkInfo.getExtraInfo().equals(this.mWifiInfo.getSSID()))) {
            if (this.mVerboseLoggingEnabled) {
                log("setDetailed state send new extra info" + this.mWifiInfo.getSSID());
            }
            this.mNetworkInfo.setExtraInfo(this.mWifiInfo.getSSID());
            sendNetworkStateChangeBroadcast(null);
        }
        if (hidden || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, this.mWifiInfo.getSSID());
        if (state == DetailedState.DISCONNECTED) {
            tryStartPnoScan();
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    private boolean isManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    private int getManuConnectNetId() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().getManuConnectNetId();
        }
        return -1;
    }

    public void handleSSIDStateChangedCB(int netId, int reason) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "handleSSIDStateChangedCB netId:" + netId + " reason:" + reason);
        }
        this.mOppoWifiConnectionAlert.handleSSIDStateChangedCB(netId, reason);
    }

    private SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = message.obj;
        SupplicantState state = stateChangeResult.state;
        if (this.mPowerState == SupplicantState.COMPLETED && state == SupplicantState.GROUP_HANDSHAKE && !this.mScreenOn) {
            this.mIdleGroupTimes++;
        }
        this.mPowerState = state;
        if (this.mLastSupplicantState != state) {
            if (this.mLastSupplicantState == SupplicantState.COMPLETED || state == SupplicantState.COMPLETED) {
                removeMessages(CMD_TRIGGER_RESTORE_DELAY);
            }
            this.mLastSupplicantState = state;
        }
        this.mWifiInfo.setSupplicantState(state);
        if ((stateChangeResult.wifiSsid == null || stateChangeResult.wifiSsid.toString().isEmpty()) && isLinkDebouncing()) {
            return state;
        }
        if (!SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(-1);
        } else if (stateChangeResult.networkId == -1) {
            Log.d(TAG, "invalid netid for wps connect,ignore!!");
        } else {
            this.mWifiInfo.setNetworkId(lookupFrameworkNetworkId(stateChangeResult.networkId));
        }
        this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
        this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
            ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
            if (scanDetailCache != null) {
                ScanDetail scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID);
                if (scanDetail != null) {
                    this.mWifiInfo.setFrequency(scanDetail.getScanResult().frequency);
                    NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                    if (networkDetail != null && networkDetail.getAnt() == Ant.ChargeablePublic) {
                        this.mWifiInfo.setMeteredHint(true);
                    }
                }
            }
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleSupplicantStateChanged(Message.obtain(message));
        }
        this.mOppoWifiConnectionAlert.sendSupplicantStateChangeEvent(stateChangeResult);
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.sendMessage(message.what, message.arg1, message.arg2, message.obj);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiSupplicantConnectStateChangedEvt(Message.obtain(message));
        }
        if (1 == syncGetWifiState() && SupplicantState.isConnecting(state)) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "wrong supplicant action, disconnect supplicant!!");
            }
            if (this.mWifiNative != null) {
                this.mWifiNative.disconnect();
            }
        }
        return state;
    }

    private void handleNetworkDisconnect() {
        handleNetworkDisconnect(false);
    }

    private void handleNetworkDisconnect(boolean connectionInProgress) {
        if (this.mVerboseLoggingEnabled) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        if (!this.mScreenOn) {
            this.mIdleDisConnTimes++;
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleConnectStateChanged(false, this.mLastNetworkId);
        }
        if (!(this.mWifiNetworkStateTraker == null || this.mNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED)) {
            this.mWifiNetworkStateTraker.setNetworkDetailState(this.mLastNetworkId, DetailedState.DISCONNECTED, this.mLastBssid);
        }
        this.mNetworkDetectValid = false;
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(false, this.mLastNetworkId);
        }
        stopRssiMonitoringOffload();
        clearCurrentConfigBSSID("handleNetworkDisconnect");
        if (!(getCurrentState() == this.mFilsState && (connectionInProgress ^ 1) == 0)) {
            stopIpManager();
        }
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mIsLinkDebouncing = false;
        this.mIsAutoRoaming = false;
        setNetworkDetailedState(DetailedState.DISCONNECTED);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent = null;
        }
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
        this.mConnectedId = -1;
    }

    private void handleSupplicantConnectionLoss(boolean killSupplicant) {
        if (killSupplicant) {
            this.mWifiMonitor.stopAllMonitoring();
            if (!this.mWifiNative.disableSupplicant()) {
                loge("Failed to disable supplicant after connection loss");
            }
        }
        this.mWifiNative.closeSupplicantConnection();
        sendSupplicantConnectionChangedBroadcast(false);
        setWifiState(1);
    }

    void handlePreDhcpSetup() {
        this.mWifiNative.setBluetoothCoexistenceMode(1);
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(false);
        getWifiLinkLayerStats();
        if (this.mWifiP2pChannel != null) {
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = 1;
            msg.arg2 = 196614;
            msg.obj = this;
            this.mWifiP2pChannel.sendMessage(msg);
            return;
        }
        sendMessage(196614);
    }

    void buildDiscoverWithRapidCommitPacket() {
        ByteBuffer mDiscoverPacket = this.mIpManager.buildDiscoverWithRapidCommitPacket();
        if (mDiscoverPacket != null) {
            int i;
            byte[] bytes = mDiscoverPacket.array();
            StringBuilder dst = new StringBuilder();
            for (i = 0; i < 5; i++) {
                dst.append(String.format("%02x:", new Object[]{Byte.valueOf(bytes[i])}));
            }
            dst.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[5])}));
            StringBuilder sb = new StringBuilder();
            for (i = 12; i < mDiscoverPacket.limit(); i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
            }
            String mDiscoverPacketBytes = sb.toString();
            this.mWifiNative.flushAllHlp();
            this.mWifiNative.addHlpReq(dst.toString(), mDiscoverPacketBytes);
        }
    }

    void handlePreFilsDhcpSetup() {
        if (this.mWifiP2pChannel != null) {
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = 1;
            msg.arg2 = 196614;
            msg.obj = this;
            this.mWifiP2pChannel.sendMessage(msg);
            return;
        }
        sendMessage(196614);
    }

    void setPowerSaveForFilsDhcp() {
        this.mWifiNative.setBluetoothCoexistenceMode(1);
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(false);
    }

    void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        this.mWifiNative.setPowerSave(true);
        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(2);
    }

    private void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mDiagsConnectionStartMillis = this.mClock.getElapsedSinceBootMillis();
        this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 0);
        this.mWrongPasswordNotifier.onNewConnectionAttempt();
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, Long.valueOf(this.mDiagsConnectionStartMillis), 60000);
    }

    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        this.mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        switch (level2FailureCode) {
            case 1:
                this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 1);
                break;
            case 5:
            case 8:
                break;
            default:
                this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 2);
                break;
        }
        this.mDiagsConnectionStartMillis = -1;
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            logd("link address " + dhcpResults.ipAddress);
        }
        synchronized (this.mDhcpResultsLock) {
            this.mDhcpResults = dhcpResults;
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }
        if (this.mIsAutoRoaming && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            logd("handleIPv4Success, roaming and address changed" + this.mWifiInfo + " got: " + addr);
        }
        this.mWifiInfo.setInetAddress(addr);
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
        }
        if (dhcpResults.hasMeteredHint()) {
            this.mWifiInfo.setMeteredHint(true);
        }
        updateCapabilities(config);
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
            } else {
                result.numIpConfigFailures = 0;
            }
        }
    }

    private void handleIPv4Failure() {
        this.mWifiDiagnostics.captureBugReportData(4);
        if (this.mVerboseLoggingEnabled) {
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
        if (this.mVerboseLoggingEnabled) {
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
        if (!this.mEnableAutoJoinWhenAssociated) {
            this.mWifiNative.reconnect();
        }
    }

    private void handleIpReachabilityLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect();
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

    private boolean isPermanentWrongPasswordFailure(int networkId, int reasonCode) {
        if (reasonCode != 2) {
            return false;
        }
        return true;
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

    private void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.wifiP2pServiceImpl = (WifiP2pServiceImpl) IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.wifiP2pServiceImpl != null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.wifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
        }
    }

    private void incrementMetricsForSetupFailure(int failureReason) {
        if (failureReason == 1) {
            this.mWifiMetrics.incrementNumWifiOnFailureDueToHal();
        } else if (failureReason == 2) {
            this.mWifiMetrics.incrementNumWifiOnFailureDueToWificond();
        }
    }

    private void maybeRegisterPhoneListener() {
        if (this.mEnableVoiceCallSarTxPowerLimit) {
            logd("Registering for telephony call state changes");
            getTelephonyManager().listen(this.mPhoneStateListener, 32);
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
            case M_CMD_SET_POWER_SAVING_MODE /*131581*/:
                s = "M_CMD_SET_POWER_SAVING_MODE";
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
            case 147457:
                s = "SUP_CONNECTION_EVENT";
                break;
            case 147458:
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
            case WifiMonitor.FILS_NETWORK_CONNECTION_EVENT /*147519*/:
                s = "FILS_NETWORK_CONNECTION_EVENT";
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
                if (WifiApStateMachine.smToString(what) == null) {
                    s = "what:" + Integer.toString(what);
                    break;
                }
                break;
        }
        return s;
    }

    void registerConnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
            this.mWifiScoreReport.reset();
            WifiConfiguration currentNetwork = getCurrentWifiConfiguration();
            if (currentNetwork != null && currentNetwork.isPasspoint()) {
                this.mPasspointManager.onPasspointNetworkConnected(currentNetwork.FQDN);
            }
        }
    }

    void registerDisconnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterDisconnect(this.mLastNetworkId);
            this.mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        if (this.mLastNetworkId == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(this.mLastNetworkId);
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
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.getScanResult(BSSID);
    }

    String getCurrentBSSID() {
        if (isLinkDebouncing()) {
            return null;
        }
        return this.mLastBssid;
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration config) {
        NetworkCapabilities result = new NetworkCapabilities(this.mDfltNetworkCapabilities);
        if (this.mWifiInfo == null || (this.mWifiInfo.isEphemeral() ^ 1) == 0) {
            result.removeCapability(14);
        } else {
            result.addCapability(14);
        }
        if (this.mWifiInfo == null || (WifiConfiguration.isMetered(config, this.mWifiInfo) ^ 1) == 0) {
            result.removeCapability(11);
        } else {
            result.addCapability(11);
        }
        if (this.mWifiInfo == null || this.mWifiInfo.getRssi() == WifiMetrics.MIN_RSSI_DELTA) {
            result.setSignalStrength(Integer.MIN_VALUE);
        } else {
            result.setSignalStrength(this.mWifiInfo.getRssi());
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkCapabilities(result);
        }
    }

    private boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        if (networkId == -1) {
            return false;
        }
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
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
        if (mccMnc == null || (mccMnc.isEmpty() ^ 1) == 0) {
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
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null || (config.allowedKeyManagement.get(1) ^ 1) != 0 || scanDetailCache.size() > 6) {
            return true;
        }
        Set<Integer> freqs = this.mWifiConfigManager.fetchChannelSetForNetworkForPartialScan(config.networkId, 3600000, this.mWifiInfo.getFrequency());
        if (freqs == null || freqs.size() == 0) {
            if (this.mVerboseLoggingEnabled) {
                logd("no channels for " + config.configKey());
            }
            return false;
        }
        logd("starting scan for " + config.configKey() + " with " + freqs);
        List<HiddenNetwork> hiddenNetworks = new ArrayList();
        if (config.hiddenSSID) {
            hiddenNetworks.add(new HiddenNetwork(config.SSID));
        }
        if (startScanNative(freqs, hiddenNetworks, WIFI_WORK_SOURCE)) {
            this.messageHandlingStatus = 1;
        } else {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_HANDLING_ERROR;
        }
        return true;
    }

    public boolean shouldEvaluateWhetherToSendExplicitlySelected(WifiConfiguration currentConfig) {
        boolean z = false;
        if (currentConfig == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
            return false;
        }
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        if (this.mWifiConfigManager.getLastSelectedNetwork() == currentConfig.networkId && currentTimeMillis - this.mWifiConfigManager.getLastSelectedTimeStamp() < 30000) {
            z = true;
        }
        return z;
    }

    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            boolean prompt = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(config.lastConnectUid);
            if (this.mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt && this.mVerboseLoggingEnabled) {
                log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
            }
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleConnectStateChanged(true, this.mLastNetworkId);
        }
        this.mConnectedId = this.mLastNetworkId;
        if (this.mWifiNetworkStateTraker != null) {
            this.mWifiNetworkStateTraker.setNetworkDetailState(this.mLastNetworkId, DetailedState.CONNECTED, this.mLastBssid);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(true, this.mLastNetworkId);
        }
        setNetworkDetailedState(DetailedState.CONNECTED);
        this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
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
            intent.putExtra(SupplicantBackupMigration.SUPPLICANT_KEY_SSID, config.SSID);
            intent.putExtra("et", wifiCredentialEventType);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE");
        }
    }

    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == lookupFrameworkNetworkId(requestData.networkId)) {
            logd("id matches targetWifiConfiguration");
            String response = TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
            if (response == null) {
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
            } else {
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    void handle3GAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == lookupFrameworkNetworkId(requestData.networkId)) {
            logd("id matches targetWifiConfiguration");
            SimAuthResponseData response = TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
            if (response != null) {
                this.mWifiNative.simAuthResponse(requestData.networkId, response.type, response.response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(requestData.networkId);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    public void startConnectToNetwork(int networkId, int uid, String bssid) {
        sendMessage(CMD_START_CONNECT, networkId, uid, bssid);
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_START_ROAM, networkId, 0, scanResult);
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

    public void updateWifiMetrics() {
        this.mWifiMetrics.updateSavedNetworks(this.mWifiConfigManager.getSavedNetworks());
        this.mPasspointManager.updateMetrics();
    }

    private boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        boolean success = this.mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid);
        if (!success) {
            loge("Failed to remove network");
        }
        if (calledFromForget) {
            if (success) {
                replyToMessage(message, 151558);
                broadcastWifiCredentialChanged(1, (WifiConfiguration) message.obj);
                return true;
            }
            replyToMessage(message, 151557, 0);
            return false;
        } else if (success) {
            replyToMessage(message, message.what, 1);
            return true;
        } else {
            this.messageHandlingStatus = -2;
            replyToMessage(message, message.what, -1);
            return false;
        }
    }

    private NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration " + this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + getCurrentState().getName());
            this.messageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
        convertToQuotedSSID(config);
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            this.messageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return result;
        } else if (this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, message.sendingUid)) {
            broadcastWifiCredentialChanged(0, config);
            replyToMessage(message, 151561);
            return result;
        } else {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            this.messageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
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

    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
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

    /* JADX WARNING: Missing block: B:11:0x0022, code:
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
                    boolean isExists = false;
                    for (ScanResult sr : srList) {
                        if (sr != null) {
                            String bssid = sr.BSSID;
                            String ssid = sr.SSID;
                            if (configBssid.equals(bssid)) {
                                count++;
                                if (sr.timestamp > newestTimeStamp.longValue()) {
                                    targetSsid = ssid;
                                    newestTimeStamp = Long.valueOf(sr.timestamp);
                                    if (ssid != null && ssid.equals(configSSID)) {
                                        isExists = true;
                                    }
                                }
                            }
                        }
                    }
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "same bssid count = " + count);
                    }
                    if (count <= 1 && isExists) {
                        return;
                    }
                    if (targetSsid == null || targetSsid.equals("")) {
                        Log.d(TAG, "target = " + targetSsid);
                        return;
                    }
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "set manu connect from " + configSSID + " ssid to " + targetSsid);
                    }
                    config.SSID = targetSsid;
                }
            }
        }
    }

    private void convertToQuotedSSID(WifiConfiguration config) {
        if (config != null && (TextUtils.isEmpty(config.SSID) ^ 1) != 0) {
            if (config.SSID.charAt(0) != '\"' || (config.SSID.charAt(0) == '\"' && config.SSID.charAt(config.SSID.length() - 1) != '\"')) {
                String str = "";
                config.SSID = "\"" + config.SSID + "\"";
            }
        }
    }

    private String convertToQuotedSSID(String ssid) {
        String str = "";
        if (TextUtils.isEmpty(ssid)) {
            return str;
        }
        if (ssid.charAt(0) != '\"' || (ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) != '\"')) {
            return "\"" + ssid + "\"";
        }
        return ssid;
    }

    private boolean p2pSendMessage(int what) {
        if (this.mWifiP2pChannel == null) {
            return false;
        }
        this.mWifiP2pChannel.sendMessage(what);
        return true;
    }

    private boolean p2pSendMessage(int what, int arg1) {
        if (this.mWifiP2pChannel == null) {
            return false;
        }
        this.mWifiP2pChannel.sendMessage(what, arg1);
        return true;
    }

    private boolean hasConnectionRequests() {
        return this.mConnectionReqCount > 0 || this.mUntrustedReqCount > 0;
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

    private OppoWifiAssistantStateTraker makeWifiNetworkStateTracker() {
        return new OppoWifiAssistantStateTraker(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler());
    }

    private OppoWifiNetworkSwitchEnhance makeWifiNetworkAvailable() {
        return new OppoWifiNetworkSwitchEnhance(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker);
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

    public boolean getIpReachabilityDisconnectEnabled() {
        return this.mIpReachabilityDisconnectEnabled;
    }

    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        this.mIpReachabilityDisconnectEnabled = enabled;
    }

    public boolean syncInitialize(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_INITIALIZE);
        boolean result = resultMsg.arg1 != -1;
        Log.d(TAG, "=qcdbg= syncInitialize() result=" + result);
        resultMsg.recycle();
        return result;
    }

    private boolean hasConfigKeyChanged(int newNetId) {
        String newCfgKey = null;
        if (newNetId != -1) {
            WifiConfiguration newCfg = this.mWifiConfigManager.getConfiguredNetwork(newNetId);
            if (newCfg != null) {
                newCfgKey = newCfg.configKey();
            }
        }
        boolean cfgKeyChanged = (this.sLastConfigKey == null || newCfgKey == null) ? this.sLastConfigKey != newCfgKey : this.sLastConfigKey.equals(newCfgKey) ^ 1;
        if (cfgKeyChanged) {
            this.sLastConfigKey = newCfgKey;
        }
        return cfgKeyChanged;
    }

    private boolean isUsingDHCP() {
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig != null && currentConfig.getIpAssignment() == IpAssignment.DHCP) {
            synchronized (this.mDhcpResultsLock) {
                if (this.mDhcpResults == null || this.mDhcpResults.ipAddress == null || this.mDhcpResults.ipAddress.getAddress() == null || !this.mDhcpResults.ipAddress.isIPv4()) {
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
                    /* JADX WARNING: Missing block: B:32:?, code:
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
                            if (httpURLConnection.getResponseCode() == 204) {
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
                                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_ROAM, WifiStateMachine.this.mLastNetworkId, 0, null);
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

    public void initRomupdateHelperBroadcastReceiver() {
        if (this.mWifiRomUpdateHelper != null) {
            this.mWifiRomUpdateHelper.initUpdateBroadcastReceiver();
        }
        if (this.mWifiCfgUpdateHelper != null) {
            this.mWifiCfgUpdateHelper.initUpdateBroadcastReceiver();
        }
    }

    private void mtuProber() {
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
                WifiStateMachine.this.connectToMtuServer(mtuServer[WifiStateMachine.mRandom.nextInt(mtuServer.length - 1)]);
            }
        });
    }

    private String buildContent() {
        return BOUNDARY + new String(new char[(getRomUpdateIntegerValue("NETWORK_MTU", Integer.valueOf(1500)).intValue() - 110)]) + "\r\n" + BOUNDARY;
    }

    private void connectToMtuServer(String mtuServer) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL("http://" + mtuServer).openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=-----------hello word-----------\r\n");
            httpURLConnection.setRequestProperty("Charsert", "UTF-8");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            OutputStream outStream = httpURLConnection.getOutputStream();
            outStream.write(buildContent().getBytes("utf-8"));
            outStream.close();
            InputStream inStream = httpURLConnection.getInputStream();
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    return;
                }
            }
            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e2) {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e4) {
                }
            }
        }
    }

    public WifiConfiguration getTargetWifiConfiguration() {
        return this.targetWificonfiguration;
    }

    public boolean isSupplicantStateDisconnected() {
        if (this.mSupplicantStateTracker != null) {
            String supplicantState = this.mSupplicantStateTracker.getSupplicantStateName();
            if ("DisconnectedState".equals(supplicantState) || "ScanState".equals(supplicantState) || "InactiveState".equals(supplicantState)) {
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
        } else if (3 == getOperationalModeForTest()) {
            logd("wifi in disable pending state!");
            return false;
        } else {
            if (!"SupplicantStoppingState".equalsIgnoreCase(getCurrentState().getName())) {
                return true;
            }
            logd("supplicant in stoppong state!");
            return false;
        }
    }

    public boolean isNetworkConnecting() {
        return isNetworkConnecting(this.mTargetNetworkId);
    }

    public boolean isNetworkConnecting(int netId) {
        return !isNetworkManuConnecting(netId) ? isNetworkAutoConnecting(netId) : true;
    }

    public boolean isNetworkAutoConnectingOrConnected(int netId) {
        return !isNetworkAutoConnecting(netId) ? isNetworkConnected(netId) : true;
    }

    public boolean isNetworkManuConnecting(int netId) {
        boolean isConnecting = false;
        if (this.mTargetNetworkId != netId) {
            return false;
        }
        if (OppoAutoConnectManager.getInstance() != null && OppoAutoConnectManager.getInstance().isManuConnect()) {
            isConnecting = true;
        }
        return isConnecting;
    }

    public boolean isNetworkAutoConnecting(int netId) {
        boolean isConnecting = false;
        if (this.mTargetNetworkId != netId) {
            return false;
        }
        if ((this.mNetworkInfo != null && this.mNetworkInfo.isConnectedOrConnecting()) || this.mOppoWifiConnectionAlert.isSelectingNetwork()) {
            isConnecting = true;
        }
        return isConnecting;
    }

    public boolean isNetworkConnected(int netId) {
        if (this.mWifiInfo.getNetworkId() == netId) {
            return true;
        }
        return false;
    }

    public void tryStartPnoScan() {
        if (!this.mScreenOn && this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.tryStartConnectivityScan();
        }
    }

    public boolean startRxSensTest(WifiConfiguration config, String ip) {
        if (getCurrentState() != this.mScanModeState) {
            setSupplicantRunning(true);
            setOperationalMode(3);
        }
        while (getCurrentState() != this.mScanModeState) {
            Log.d(TAG, "startRxSensTest sleep 100");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, OppoManuConnectManager.UID_DEFAULT);
        if (!result.isSuccess()) {
            return false;
        }
        if (!this.mWifiNative.connectToNetwork(this.mWifiConfigManager.getConfiguredNetwork(result.getNetworkId()))) {
            return false;
        }
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        try {
            ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ip), 24));
            try {
                this.mNwService.setInterfaceConfig(this.mInterfaceName, ifcg);
                this.mRejectScanWhenRxSensTest = true;
                return true;
            } catch (RemoteException re) {
                loge("startRxSensTest failed: " + re);
                return false;
            } catch (IllegalStateException e2) {
                loge("startRxSensTest failed2: " + e2);
                return false;
            }
        } catch (IllegalArgumentException e3) {
            loge("startRxSensTest invalid ip: " + ip);
            return false;
        }
    }

    public void stopRxSensTest() {
        this.mWifiNative.disconnect();
        this.mRejectScanWhenRxSensTest = false;
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

    public String getWifiPowerEventCode() {
        StringBuilder eventPower = new StringBuilder();
        float idleTime = ((float) (System.currentTimeMillis() - this.mScreenOffTime)) / 60000.0f;
        if (idleTime == 0.0f) {
            return null;
        }
        eventPower.append("event=");
        if (this.mIdleScanTimes != 0) {
            float scanFrequency = idleTime / ((float) this.mIdleScanTimes);
        }
        if (this.mIdleRenewTimes != 0) {
            float renewFrequency = idleTime / ((float) this.mIdleRenewTimes);
        }
        if (this.mIdleDisConnTimes != 0) {
            float disconnFrequency = idleTime / ((float) this.mIdleDisConnTimes);
        }
        if (this.mIdleGroupTimes != 0) {
            float keyChangeFrequency = idleTime / ((float) this.mIdleGroupTimes);
        }
        float powerValue = (((((float) this.mIdleScanTimes) * WEIGHT_SCAN) + (((float) this.mIdleRenewTimes) * WEIGHT_RENEW)) + (((float) this.mIdleDisConnTimes) * WEIGHT_DISCONN)) + (((float) this.mIdleGroupTimes) * WEIGHT_GROUP);
        eventPower.append(compareMaxPower());
        eventPower.append("\n");
        eventPower.append("powerValue=").append(Float.toString(powerValue));
        log("wifieventpower" + eventPower.toString());
        return eventPower.toString();
    }

    private String compareMaxPower() {
        String maxValue = "SCAN_FREQUENT";
        float powerValueMax = ((float) this.mIdleScanTimes) * WEIGHT_SCAN;
        if (((float) this.mIdleRenewTimes) * WEIGHT_RENEW > powerValueMax) {
            maxValue = "RENEW_FREQUENT";
            powerValueMax = ((float) this.mIdleRenewTimes) * WEIGHT_RENEW;
        }
        if (((float) this.mIdleDisConnTimes) * WEIGHT_DISCONN > powerValueMax) {
            maxValue = "DISCONN_FREQUENT";
            powerValueMax = ((float) this.mIdleDisConnTimes) * WEIGHT_SCAN;
        }
        if (((float) this.mIdleGroupTimes) * WEIGHT_GROUP <= powerValueMax) {
            return maxValue;
        }
        powerValueMax = ((float) this.mIdleGroupTimes) * WEIGHT_SCAN;
        return "GROUP_FREQUENT";
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
}
