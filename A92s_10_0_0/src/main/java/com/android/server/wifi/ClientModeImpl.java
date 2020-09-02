package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.MatchAllNetworkSpecifier;
import android.net.NattKeepalivePacketData;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.SocketKeepalive;
import android.net.StaticIpConfiguration;
import android.net.TcpKeepalivePacketData;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientManager;
import android.net.shared.Inet4AddressUtils;
import android.net.shared.ProvisioningConfiguration;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkAgentSpecifier;
import android.net.wifi.WifiRomUpdateHelper;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.system.OsConstants;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.SystemServiceManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import com.android.server.connectivity.gatewayconflict.OppoIPConflictDetector;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.OppoDhcpRecord;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.WifiMulticastLockManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.GbkUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.mediatek.server.wifi.MtkWifiServiceAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientModeImpl extends StateMachine {
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    private static final String ACTION_TRY_START_CAPTIVE_ACTIVITY = "oppo.intent.action.ACTION_TRY_START_CAPTIVE_ACTIVITY";
    private static final int ARP_HAS_RESPONSE = 1;
    static final int BASE = 131072;
    private static final int BASE_OPPO = 131472;
    private static final int CAPTIVE_DIRECT_TO_BROWSER = 1;
    private static final int CAPTIVE_DIRECT_TO_NOTHING = 3;
    private static final int CAPTIVE_DIRECT_TO_NOTIFY = 2;
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF = 131281;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    private static final int CMD_AUTO_CON_CAPTIVE = 131482;
    static final int CMD_AUTO_JOIN_OPTIMIAZE = 131372;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    static final int CMD_BOOT_COMPLETED = 131206;
    private static final int CMD_CHECK_INTERNET_ACCESS = 131480;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_ENABLE_NETWORK = 131126;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_FIRMWARE_ALERT = 131172;
    static final int CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS = 131240;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_OSU_PROVIDERS = 131181;
    static final int CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS = 131182;
    static final int CMD_GET_PASSPOINT_CONFIGS = 131180;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES = 131184;
    static final int CMD_INITIALIZE = 131207;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    static final int CMD_NETWORK_STATUS = 131220;
    static final int CMD_ONESHOT_RSSI_POLL = 131156;
    private static final int CMD_POST_DHCP_ACTION = 131329;
    @VisibleForTesting
    static final int CMD_PRE_DHCP_ACTION = 131327;
    private static final int CMD_PRE_DHCP_ACTION_COMPLETE = 131328;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_READ_PACKET_FILTER = 131280;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF = 131282;
    static final int CMD_REMOVE_NETWORK = 131125;
    static final int CMD_REMOVE_PASSPOINT_CONFIG = 131179;
    static final int CMD_REMOVE_USER_CONFIGURATIONS = 131224;
    static final int CMD_RESET_SIM_NETWORKS = 131173;
    static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    static final int CMD_RSSI_POLL = 131155;
    static final int CMD_RSSI_THRESHOLD_BREACHED = 131236;
    static final int CMD_SCREEN_STATE_CHANGED = 131167;
    static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    static final int CMD_SET_HIGH_PERF_MODE = 131149;
    static final int CMD_SET_OPERATIONAL_MODE = 131144;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_CONNECT = 131215;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_ROAM = 131217;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    static final int CMD_START_SCAN = 131479;
    private static final int CMD_START_SUBSCRIPTION_PROVISIONING = 131326;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    static final int CMD_TARGET_BSSID = 131213;
    private static final int CMD_TRIGGER_MINIDUMP_ENABLE = 131478;
    private static final int CMD_TRIGGER_RESTORE_DELAY = 131477;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    static final int CMD_USER_STOP = 131279;
    static final int CMD_USER_SWITCH = 131277;
    static final int CMD_USER_UNLOCK = 131278;
    public static final int CONNECT_MODE = 1;
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    public static final int DATA_STALL_OFFSET_REASON_CODE = 256;
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    @VisibleForTesting
    public static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    private static final String DIRECT_TO_BROWSER = "direct_to_browser";
    public static final int DISABLED_MODE = 4;
    private static final int DISABLE_INTERFACE = -1;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int EVENT_DHCP_STATE_CHANGE = 131488;
    private static final int EVENT_DO_DUP_ARP = 131489;
    private static final int EVENT_DO_GATEWAY_DETECT = 131490;
    public static final int EVENT_FIND_DUP_SERVER = 131473;
    public static final int EVENT_FIX_SERVER_FAILURE = 131476;
    public static final int EVENT_SWITCH_SERVER_FAILURE = 131475;
    public static final int EVENT_UPDATE_LEASE_EXPRIY = 131474;
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final String EXTRA_OSU_PROVIDER = "OsuProvider";
    private static final String EXTRA_PACKAGE_NAME = "PackageName";
    private static final String EXTRA_PASSPOINT_CONFIGURATION = "PasspointConfiguration";
    private static final String EXTRA_UID = "uid";
    private static final int FAILURE = -1;
    private static final String GOOGLE_OUI = "DA-A1-19";
    private static final int IDLE_DISCONN_FREQ = 35;
    private static final int IDLE_GROUP_FREQ = 20;
    private static final int IDLE_RENEW_FREQ = 40;
    private static final int IDLE_SCAN_FREQ = 3;
    private static final int INVALID_NETWORK_ID = -1;
    private static final int IPCLIENT_TIMEOUT_MS = 10000;
    @VisibleForTesting
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
    private static final int M_CMD_SET_POWER_SAVING_MODE = 131487;
    private static final String NETWORKTYPE = "WIFI";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    private static final int NETWORK_TCP_TS_ERROR_THRESHOLD = 10;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_NORMAL = 100;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE = 3000;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static final String OTA_VERSION = "ro.build.version.ota";
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    private static final String RSSI_LEVEL = "rssi_level";
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
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
    private static final String TAG = "WifiClientModeImpl";
    private static final int TRIGGER_RESTORE_DELAY_TIME = 2000;
    private static final float WEIGHT_DISCONN = 0.6f;
    private static final float WEIGHT_GROUP = 0.8f;
    private static final float WEIGHT_RENEW = 0.5f;
    private static final float WEIGHT_SCAN = 0.28f;
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final boolean WIFI_DBG = SystemProperties.getBoolean("persist.wifi.dbg", false);
    public static String WIFI_PACKEG_NAME = "android";
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final SparseArray<String> sGetWhatToString = MessageUtils.findMessageNames(sMessageClasses);
    private static final Class[] sMessageClasses = {AsyncChannel.class, ClientModeImpl.class};
    private static int sNetId = -1;
    private static int sScanAlarmIntentCount = 0;
    private int DISCONN_FLAG = 4096;
    private int GROUP_FLAG = 16;
    private int RENEW_FLAG = 1;
    private int SCAN_FLAG = 256;
    /* access modifiers changed from: private */
    public boolean hasLoadStore = false;
    /* access modifiers changed from: private */
    public ActiveModeWarden mActiveModeWarden = null;
    private Handler mAsyncHandler;
    /* access modifiers changed from: private */
    public boolean mAutoSwitch;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public boolean mBluetoothConnectionActive = false;
    private final BuildProperties mBuildProperties;
    /* access modifiers changed from: private */
    public State mCaptiveState = new CaptiveState();
    /* access modifiers changed from: private */
    public int mCheckInetAccessSeq = 0;
    private ClientModeManager.Listener mClientModeCallback = null;
    /* access modifiers changed from: private */
    public final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    /* access modifiers changed from: private */
    public int mConnectedId = -1;
    /* access modifiers changed from: private */
    public boolean mConnectedMacRandomzationSupported;
    /* access modifiers changed from: private */
    public State mConnectedState = new ConnectedState();
    /* access modifiers changed from: private */
    public long mConnectionTimeStamp = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public final WifiCountryCode mCountryCode;
    /* access modifiers changed from: private */
    public OppoArpPeer.ArpPeerChangeCallback mDADCallback = new OppoArpPeer.ArpPeerChangeCallback() {
        /* class com.android.server.wifi.ClientModeImpl.AnonymousClass1 */

        public void onArpReponseChanged(int arpResponseReceieved, Network network) {
            Log.d(ClientModeImpl.TAG, "IP Recovery, mDADCallback onArpReponseChanged:" + arpResponseReceieved);
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.onDADetectorResult(1);
            }
        }
    };
    /* access modifiers changed from: private */
    public OppoIPConflictDetector mDADetector = null;
    private State mDefaultState = new DefaultState();
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    /* access modifiers changed from: private */
    public boolean mDidBlackListBSSID = false;
    /* access modifiers changed from: private */
    public State mDisconnectedState = new DisconnectedState();
    /* access modifiers changed from: private */
    public State mDisconnectingState = new DisconnectingState();
    int mDisconnectingWatchdogCount = 0;
    /* access modifiers changed from: private */
    public boolean mEnableRssiPolling = false;
    private FrameworkFacade mFacade;
    /* access modifiers changed from: private */
    public boolean mFromKeylogVerbose = false;
    /* access modifiers changed from: private */
    public OppoArpPeer.ArpPeerChangeCallback mGWDCallback = new OppoArpPeer.ArpPeerChangeCallback() {
        /* class com.android.server.wifi.ClientModeImpl.AnonymousClass2 */

        public void onArpReponseChanged(int arpResponseReceieved, Network network) {
            Log.d(ClientModeImpl.TAG, "IP Recovery, mGWDCallback onArpReponseChanged:" + arpResponseReceieved);
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.onGWDetectorResult(1);
            }
        }
    };
    /* access modifiers changed from: private */
    public OppoIPConflictDetector mGWDetector = null;
    private boolean mHasInternetAccess = false;
    private int mIdleDisConnTimes = 0;
    private int mIdleGroupTimes = 0;
    /* access modifiers changed from: private */
    public int mIdleRenewTimes = 0;
    /* access modifiers changed from: private */
    public int mIdleScanTimes = 0;
    private int mIndex = 0;
    /* access modifiers changed from: private */
    public String mInterfaceName;
    /* access modifiers changed from: private */
    public volatile IpClientManager mIpClient;
    private IpClientCallbacksImpl mIpClientCallbacks;
    /* access modifiers changed from: private */
    public boolean mIpReachabilityDisconnectEnabled = true;
    /* access modifiers changed from: private */
    public int mIs1x1IotRouter = -1;
    /* access modifiers changed from: private */
    public boolean mIsAutoRoaming = false;
    /* access modifiers changed from: private */
    public boolean mIsRandomMacCleared = false;
    private boolean mIsRunning = false;
    private State mL2ConnectedState = new L2ConnectedState();
    /* access modifiers changed from: private */
    public String mLastBssid;
    /* access modifiers changed from: private */
    public long mLastConnectAttemptTimestamp = 0;
    private NetworkInfo.DetailedState mLastDetailedState = NetworkInfo.DetailedState.IDLE;
    /* access modifiers changed from: private */
    public long mLastDriverRoamAttempt = 0;
    private Pair<String, String> mLastL2KeyAndGroupHint = null;
    /* access modifiers changed from: private */
    public WifiLinkLayerStats mLastLinkLayerStats;
    private long mLastLinkLayerStatsUpdate = 0;
    /* access modifiers changed from: private */
    public int mLastNetworkId;
    private long mLastOntimeReportTimeStamp = 0;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private long mLastScanTime = 0;
    private long mLastScreenStateChangeTimeStamp = 0;
    /* access modifiers changed from: private */
    public long mLastSelectEvtTimeStamp = 0;
    /* access modifiers changed from: private */
    public int mLastSignalLevel = -1;
    /* access modifiers changed from: private */
    public SupplicantState mLastSupplicantState = SupplicantState.DISCONNECTED;
    /* access modifiers changed from: private */
    public final LinkProbeManager mLinkProbeManager;
    /* access modifiers changed from: private */
    public LinkProperties mLinkProperties;
    private final McastLockManagerFilterController mMcastLockManagerFilterController;
    /* access modifiers changed from: private */
    public int mMessageHandlingStatus = 0;
    private boolean mModeChange = false;
    /* access modifiers changed from: private */
    @GuardedBy({"mNetworkAgentLock"})
    public WifiNetworkAgent mNetworkAgent;
    /* access modifiers changed from: private */
    public final Object mNetworkAgentLock = new Object();
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    /* access modifiers changed from: private */
    public boolean mNetworkDetectValid = false;
    /* access modifiers changed from: private */
    public WifiNetworkFactory mNetworkFactory;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public final NetworkMisc mNetworkMisc = new NetworkMisc();
    private AtomicInteger mNullMessageCounter = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    /* access modifiers changed from: private */
    public int mOperationalMode = 4;
    /* access modifiers changed from: private */
    public OppoClientModeImplUtil mOppoClientModeImplUtil;
    /* access modifiers changed from: private */
    public OppoDhcpRecord mOppoDhcpRecord = null;
    /* access modifiers changed from: private */
    public OppoMtuProber mOppoMtuProber;
    /* access modifiers changed from: private */
    public OppoNfHooksHelper mOppoNfHooksHelper;
    /* access modifiers changed from: private */
    public OppoScanResultsProxy mOppoScanResultsProxy;
    /* access modifiers changed from: private */
    public final OppoWifiConnectionAlert mOppoWifiConnectionAlert;
    /* access modifiers changed from: private */
    public final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    /* access modifiers changed from: private */
    public final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    /* access modifiers changed from: private */
    public volatile int mPollRssiIntervalMsecs = 3000;
    private SupplicantState mPowerState = SupplicantState.UNINITIALIZED;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    int mRoamWatchdogCount = 0;
    /* access modifiers changed from: private */
    public State mRoamingState = new RoamingState();
    private int[] mRssiArray = {0, 0, 0, 0, 0};
    private int mRssiCount = 5;
    /* access modifiers changed from: private */
    public int mRssiPollToken = 0;
    /* access modifiers changed from: private */
    public byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    /* access modifiers changed from: private */
    public final SarManager mSarManager;
    /* access modifiers changed from: private */
    public ScanRequestProxy mScanRequestProxy;
    private long mScreenOffTime = 0;
    /* access modifiers changed from: private */
    public boolean mScreenOn = false;
    private long mScreenOnTime = 0;
    /* access modifiers changed from: private */
    public boolean mSmartGearFeature;
    private SubscriptionManager mSubscriptionManager;
    private long mSupplicantScanIntervalMs;
    /* access modifiers changed from: private */
    public SupplicantStateTracker mSupplicantStateTracker;
    private int mSuspendOptNeedsDisabled = 0;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mSuspendWakeLock;
    /* access modifiers changed from: private */
    public int mTargetNetworkId = -1;
    /* access modifiers changed from: private */
    public String mTargetRoamBSSID = "any";
    /* access modifiers changed from: private */
    public WifiConfiguration mTargetWifiConfiguration = null;
    /* access modifiers changed from: private */
    public final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public boolean mTemporarilyDisconnectWifi = false;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    private PowerManager.WakeLock mWakeLock;
    /* access modifiers changed from: private */
    public final WifiConfigManager mWifiConfigManager;
    /* access modifiers changed from: private */
    public final WifiConnectivityManager mWifiConnectivityManager;
    /* access modifiers changed from: private */
    public final WifiDataStall mWifiDataStall;
    /* access modifiers changed from: private */
    public BaseWifiDiagnostics mWifiDiagnostics;
    /* access modifiers changed from: private */
    public final ExtendedWifiInfo mWifiInfo;
    /* access modifiers changed from: private */
    public final WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    /* access modifiers changed from: private */
    public final WifiNative mWifiNative;
    /* access modifiers changed from: private */
    public OppoWifiNetworkSwitchEnhance mWifiNetworkAvailable;
    /* access modifiers changed from: private */
    public OppoWifiAssistantStateTraker mWifiNetworkStateTraker;
    private WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    /* access modifiers changed from: private */
    public AsyncChannel mWifiP2pChannel;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    /* access modifiers changed from: private */
    public final WifiScoreCard mWifiScoreCard;
    /* access modifiers changed from: private */
    public final WifiScoreReport mWifiScoreReport;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    /* access modifiers changed from: private */
    public WifiStateTracker mWifiStateTracker;
    /* access modifiers changed from: private */
    public final WifiTrafficPoller mWifiTrafficPoller;
    /* access modifiers changed from: private */
    public State mWpsRunningState = new WpsRunningState();
    private final WrongPasswordNotifier mWrongPasswordNotifier;
    private int originRssi = -127;
    private String sLastConfigKey = null;
    WifiP2pServiceImpl wifiP2pServiceImpl;

    static /* synthetic */ int access$11008(ClientModeImpl x0) {
        int i = x0.mRssiPollToken;
        x0.mRssiPollToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$1208(ClientModeImpl x0) {
        int i = x0.mIdleRenewTimes;
        x0.mIdleRenewTimes = i + 1;
        return i;
    }

    static /* synthetic */ int access$15708(ClientModeImpl x0) {
        int i = x0.mRoamFailCount;
        x0.mRoamFailCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$15908(ClientModeImpl x0) {
        int i = x0.mCheckInetAccessSeq;
        x0.mCheckInetAccessSeq = i + 1;
        return i;
    }

    static /* synthetic */ int access$5008(ClientModeImpl x0) {
        int i = x0.mIdleScanTimes;
        x0.mIdleScanTimes = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Log.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Log.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(getName(), s);
    }

    public WifiScoreReport getWifiScoreReport() {
        return this.mWifiScoreReport;
    }

    /* access modifiers changed from: private */
    public void processRssiThreshold(byte curRssi, int reason, WifiNative.WifiRssiEventHandler rssiHandler) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + ((int) curRssi));
            return;
        }
        int i = 0;
        while (true) {
            byte[] bArr = this.mRssiRanges;
            if (i >= bArr.length) {
                return;
            }
            if (curRssi < bArr[i]) {
                byte maxRssi = bArr[i];
                byte minRssi = bArr[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                int ret = startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler);
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "Re-program RSSI thresholds for " + getWhatToString(reason) + ": [" + ((int) minRssi) + ", " + ((int) maxRssi) + "], curRssi=" + ((int) curRssi) + " ret=" + ret);
                    return;
                }
                return;
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public int getPollRssiIntervalMsecs() {
        return this.mPollRssiIntervalMsecs;
    }

    /* access modifiers changed from: package-private */
    public void setPollRssiIntervalMsecs(int newPollIntervalMsecs) {
        this.mPollRssiIntervalMsecs = newPollIntervalMsecs;
    }

    public boolean clearTargetBssid(String dbg) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (config == null) {
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + "any" + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = "any";
        if (config.BSSID != "any") {
            this.mWifiConfigManager.clearConfiguredNetworkBssid(config.networkId);
        }
        return this.mWifiNative.setConfiguredNetworkBSSID(this.mInterfaceName, "any");
    }

    public boolean clearCurrentConfigBSSID(String dbg) {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearCurrentConfigBSSID " + "any" + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = "any";
        if (config.BSSID == "any") {
            return true;
        }
        this.mWifiConfigManager.clearConfiguredNetworkBssid(config.networkId);
        return true;
    }

    public boolean isThirdApp(int uid) {
        if (uid < 10000 || uid > 19999) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean setTargetBssid(WifiConfiguration config, String bssid) {
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

    /* access modifiers changed from: private */
    public TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    /* access modifiers changed from: private */
    public SubscriptionManager getSubscriptionManager() {
        if (this.mSubscriptionManager == null) {
            this.mSubscriptionManager = this.mWifiInjector.makeSubscriptionManager();
        }
        return this.mSubscriptionManager;
    }

    public ClientModeImpl(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mWifiScoreCard = wifiInjector.getWifiScoreCard();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = wifiNative;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWrongPasswordNotifier = wrongPasswordNotifier;
        this.mSarManager = sarManager;
        this.mWifiTrafficPoller = wifiTrafficPoller;
        this.mLinkProbeManager = linkProbeManager;
        WIFI_PACKEG_NAME = this.mContext.getOpPackageName();
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mOppoClientModeImplUtil = new OppoClientModeImplUtil(this.mContext, this.mWifiNative, this);
        this.mOppoDhcpRecord = new OppoDhcpRecord(this.mContext);
        this.mOppoMtuProber = new OppoMtuProber(this.mContext);
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = IBatteryStats.Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mWifiStateTracker = wifiInjector.getWifiStateTracker();
        this.mFacade.getService("network_management");
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mPasspointManager = this.mWifiInjector.getPasspointManager();
        this.mWifiMonitor = this.mWifiInjector.getWifiMonitor();
        this.mWifiDiagnostics = this.mWifiInjector.getWifiDiagnostics();
        this.mWifiPermissionsWrapper = this.mWifiInjector.getWifiPermissionsWrapper();
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mOppoWifiConnectionAlert = new OppoWifiConnectionAlert(this.mContext, this, this.mWifiConfigManager, this.mScanRequestProxy);
        this.mWifiDataStall = this.mWifiInjector.getWifiDataStall();
        this.mWifiInfo = new ExtendedWifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mWifiConnectivityManager = this.mWifiInjector.makeWifiConnectivityManager(this);
        this.mFacade.setWifiConfigManagerStatemachine(this.mWifiConfigManager, this);
        this.mLinkProperties = new LinkProperties();
        this.mMcastLockManagerFilterController = new McastLockManagerFilterController();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039776);
        this.mCountryCode = countryCode;
        this.mWifiScoreReport = new WifiScoreReport(this.mWifiInjector.getScoringParams(), this.mClock);
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
        this.mNetworkCapabilitiesFilter.addCapability(18);
        this.mNetworkCapabilitiesFilter.addCapability(20);
        this.mNetworkCapabilitiesFilter.addCapability(13);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        this.mNetworkFactory = this.mWifiInjector.makeWifiNetworkFactory(this.mNetworkCapabilitiesFilter, this.mWifiConnectivityManager);
        this.mUntrustedNetworkFactory = this.mWifiInjector.makeUntrustedWifiNetworkFactory(this.mNetworkCapabilitiesFilter, this.mWifiConnectivityManager);
        this.mWifiNetworkSuggestionsManager = this.mWifiInjector.getWifiNetworkSuggestionsManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SCREEN_STATE_CHANGED, 1);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SCREEN_STATE_CHANGED, 0);
                }
            }
        }, filter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_TRIGGER_MINIDUMP_ENABLE);
            }
        }, new IntentFilter("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED"));
        this.mFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass5 */

            public void onChange(boolean selfChange) {
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass6 */

            public void onChange(boolean selfChange) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                boolean z = true;
                if (Settings.Global.getInt(clientModeImpl.mContext.getContentResolver(), ClientModeImpl.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                boolean unused = clientModeImpl.mAutoSwitch = z;
                if (ClientModeImpl.this.mWifiNetworkAvailable != null) {
                    ClientModeImpl.this.mWifiNetworkAvailable.setAutoSwitch(ClientModeImpl.this.mAutoSwitch);
                } else if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                    ClientModeImpl.this.mWifiNetworkStateTraker.setAutoSwitch(ClientModeImpl.this.mAutoSwitch);
                }
                Log.d(ClientModeImpl.TAG, "onChange mAutoSwitch= " + ClientModeImpl.this.mAutoSwitch);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass7 */

            public void onChange(boolean selfChange) {
                if (ClientModeImpl.this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant")) {
                    boolean z = true;
                    boolean z2 = true;
                    boolean isWlanAssistant = Settings.Global.getInt(ClientModeImpl.this.mContext.getContentResolver(), ClientModeImpl.WIFI_ASSISTANT_ROMUPDATE, 1) == 1;
                    Log.d(ClientModeImpl.TAG, "onChange wa= " + isWlanAssistant);
                    if (isWlanAssistant) {
                        if (ClientModeImpl.this.mWifiNetworkAvailable != null) {
                            ClientModeImpl.this.mWifiNetworkAvailable.setFeature(false);
                            OppoWifiNetworkSwitchEnhance unused = ClientModeImpl.this.mWifiNetworkAvailable = null;
                        }
                        if (ClientModeImpl.this.mWifiNetworkStateTraker == null) {
                            ClientModeImpl clientModeImpl = ClientModeImpl.this;
                            OppoWifiAssistantStateTraker unused2 = clientModeImpl.mWifiNetworkStateTraker = clientModeImpl.makeWifiNetworkStateTracker();
                            ClientModeImpl.this.mWifiNetworkStateTraker.setFeatureState(true);
                            ClientModeImpl.this.mWifiNetworkStateTraker.enableVerboseLogging(ClientModeImpl.this.mVerboseLoggingEnabled ? 1 : 0);
                            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                            if (Settings.Global.getInt(clientModeImpl2.mContext.getContentResolver(), ClientModeImpl.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                                z2 = false;
                            }
                            boolean unused3 = clientModeImpl2.mAutoSwitch = z2;
                            ClientModeImpl.this.mWifiNetworkStateTraker.setAutoSwitch(ClientModeImpl.this.mAutoSwitch);
                            return;
                        }
                        return;
                    }
                    if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                        ClientModeImpl.this.mWifiNetworkStateTraker.setFeatureState(false);
                        ClientModeImpl.this.mWifiNetworkStateTraker.updateWifiState(-1);
                        OppoWifiAssistantStateTraker unused4 = ClientModeImpl.this.mWifiNetworkStateTraker = null;
                    }
                    if (ClientModeImpl.this.mWifiNetworkAvailable == null) {
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        OppoWifiNetworkSwitchEnhance unused5 = clientModeImpl3.mWifiNetworkAvailable = clientModeImpl3.makeWifiNetworkAvailable();
                        ClientModeImpl.this.mWifiNetworkAvailable.setFeature(true);
                        ClientModeImpl.this.mWifiNetworkAvailable.enableVerboseLogging(ClientModeImpl.this.mVerboseLoggingEnabled ? 1 : 0);
                        ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                        if (Settings.Global.getInt(clientModeImpl4.mContext.getContentResolver(), ClientModeImpl.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                            z = false;
                        }
                        boolean unused6 = clientModeImpl4.mAutoSwitch = z;
                        ClientModeImpl.this.mWifiNetworkAvailable.setAutoSwitch(ClientModeImpl.this.mAutoSwitch);
                    }
                }
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass8 */

            public void onReceive(Context context, Intent intent) {
                Log.d(ClientModeImpl.TAG, "receive loadFromStore broadcast!!!");
                ClientModeImpl.this.mWifiConfigManager.loadFromStore();
            }
        }, new IntentFilter(ACTION_LOAD_FROM_STORE));
        this.mUserWantsSuspendOpt.set(this.mFacade.getIntegerSetting(this.mContext, "wifi_suspend_optimizations_enabled", 1) == 1);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mConnectedMacRandomzationSupported = this.mContext.getResources().getBoolean(17891576);
        this.mWifiInfo.setEnableConnectedMacRandomization(this.mConnectedMacRandomzationSupported);
        this.mWifiMetrics.setIsMacRandomizationOn(this.mConnectedMacRandomzationSupported);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039780);
        addState(this.mDefaultState);
        addState(this.mConnectModeState, this.mDefaultState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mCaptiveState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        setInitialState(this.mDefaultState);
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        HandlerThread handlerThread = new HandlerThread("CheckInternetAccess");
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper());
        if (!isWlanAssistantEnable()) {
            this.mWifiNetworkAvailable = new OppoWifiNetworkSwitchEnhance(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mScanRequestProxy);
            this.mAutoSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
            this.mWifiNetworkAvailable.setAutoSwitch(this.mAutoSwitch);
        } else {
            this.mWifiNetworkStateTraker = new OppoWifiAssistantStateTraker(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler(), this.mScanRequestProxy);
            this.mAutoSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1 ? false : true;
            this.mWifiNetworkStateTraker.setAutoSwitch(this.mAutoSwitch);
        }
        OppoManuConnectManager.init(this.mContext, this, this.mWifiConfigManager, this.mWifiRomUpdateHelper);
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mOppoNfHooksHelper = new OppoNfHooksHelper(this.mContext);
        this.mOppoNfHooksHelper.sendPidAndListen();
        this.mOppoClientModeImplUtil.initForbiddenEnNetworkApplist();
        this.mOppoClientModeImplUtil.initForbiddenDisNetworkApplist();
        OppoDataStallHelper.init(this.mContext, this, this.mWifiNetworkStateTraker, this.mWifiConfigManager, this.mWifiRomUpdateHelper, this.mWifiNative, this.mOppoNfHooksHelper);
        if (OppoDataStallHelper.getInstance() != null) {
            OppoDataStallHelper.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mOppoDhcpRecord = new OppoDhcpRecord(this.mContext);
        OppoDhcpRecord oppoDhcpRecord = this.mOppoDhcpRecord;
        if (oppoDhcpRecord != null) {
            oppoDhcpRecord.enableVerboseLogging(this.mVerboseLoggingEnabled);
        }
        OppoAutoConnectManager.init(this.mContext, this, this.mWifiConfigManager, this.mWifiNetworkStateTraker, this.mWifiNative, this.mWifiRomUpdateHelper, this.mScanRequestProxy);
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        handleWifiMinidumpSwitch();
    }

    public void start() {
        ClientModeImpl.super.start();
        handleScreenStateChanged(((PowerManager) this.mContext.getSystemService("power")).isInteractive());
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        return this.mOppoClientModeImplUtil.getRomUpdateValue(key, defaultVal);
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        return this.mOppoClientModeImplUtil.getRomUpdateIntegerValue(key, defaultVal);
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        return this.mOppoClientModeImplUtil.getRomUpdateFloatValue(key, defaultVal);
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        return this.mOppoClientModeImplUtil.getRomUpdateLongValue(key, defaultVal);
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        return this.mOppoClientModeImplUtil.getRomUpdateBooleanValue(key, defaultVal);
    }

    public void initRomupdateHelperBroadcastReceiver() {
        this.mOppoClientModeImplUtil.initRomupdateHelperBroadcastReceiver();
    }

    private void registerForWifiMonitorEvents() {
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
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SELECT_NETWORK_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SAVE_CONFIG_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_TEMP_DISABLED, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.PNO_SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_FREQUENCY_CHANGED, getHandler());
    }

    /* access modifiers changed from: private */
    public void setMulticastFilter(boolean enabled) {
        if (this.mIpClient != null) {
            this.mIpClient.setMulticastFilter(enabled);
        }
    }

    class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        McastLockManagerFilterController() {
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void startFilteringMulticastPackets() {
            ClientModeImpl.this.setMulticastFilter(true);
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void stopFilteringMulticastPackets() {
            ClientModeImpl.this.setMulticastFilter(false);
        }
    }

    class IpClientCallbacksImpl extends IpClientCallbacks {
        private final ConditionVariable mWaitForCreationCv = new ConditionVariable(false);
        private final ConditionVariable mWaitForStopCv = new ConditionVariable(false);

        IpClientCallbacksImpl() {
        }

        public void onIpClientCreated(IIpClient ipClient) {
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            IpClientManager unused = clientModeImpl.mIpClient = new IpClientManager(ipClient, clientModeImpl.getName());
            this.mWaitForCreationCv.open();
        }

        public void onPreDhcpAction() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_PRE_DHCP_ACTION);
        }

        public void onPostDhcpAction() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_POST_DHCP_ACTION);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults != null) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            } else {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IPV4_PROVISIONING_FAILURE);
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(7);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_UPDATE_LINKPROPERTIES, newLp);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(8);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(9);
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.logd("onReachabilityLost received:" + logMsg);
            }
        }

        public void installPacketFilter(byte[] filter) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void startReadPacketFilter() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_READ_PACKET_FILTER);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void onDhcpRenewCount() {
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.logd("handleDhcpRenew mScreenOn=" + ClientModeImpl.this.mScreenOn + ",mIdleRenewTimes :" + ClientModeImpl.this.mIdleRenewTimes);
            if (!ClientModeImpl.this.mScreenOn) {
                ClientModeImpl.access$1208(ClientModeImpl.this);
            }
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }

        public void onQuit() {
            this.mWaitForStopCv.open();
        }

        public void onFindDupServer(String server) {
            Log.e("IpManagerCallback", "[1131400] onFindDupServer");
            ClientModeImpl.this.sendMessage(131473, server);
        }

        public void onUpdateLeaseExpriy(long time) {
            Log.e("IpManagerCallback", "[1131400] dhcpLeaseExpiry");
            ClientModeImpl.this.sendMessage(131474, new Long(time));
        }

        public void onSwitchServerFailure(String server) {
            Log.e("IpManagerCallback", "[1131400] onSwitchDhcpFailure.");
            ClientModeImpl.this.sendMessage(131475, server);
        }

        public void onFixServerFailure(String server) {
            Log.e("IpManagerCallback", "[1131400] onSwitchDhcpFailure.");
            ClientModeImpl.this.sendMessage(131476, server);
        }

        public void onDoDupArp(String ifaceName, int myAddress, int target) {
            ClientModeImpl.this.logd("onDoDupArp");
            ClientModeImpl.this.sendMessage(ClientModeImpl.EVENT_DO_DUP_ARP, myAddress, target, ifaceName);
        }

        public void onDoGatewayDetect(String ifaceName, int myAddress, int target) {
            ClientModeImpl.this.logd("onDoGatewayDetect");
            ClientModeImpl.this.sendMessage(ClientModeImpl.EVENT_DO_GATEWAY_DETECT, myAddress, target, ifaceName);
        }

        public void onDhcpStateChange(DhcpResults dhcpResults) {
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.logd("onDhcpStateChange dhcpResults = " + dhcpResults);
            ClientModeImpl.this.sendMessage(ClientModeImpl.EVENT_DHCP_STATE_CHANGE, dhcpResults);
        }

        /* access modifiers changed from: package-private */
        public boolean awaitCreation() {
            return this.mWaitForCreationCv.block(RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
        }

        /* access modifiers changed from: package-private */
        public boolean awaitShutdown() {
            return this.mWaitForStopCv.block(RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
        }
    }

    /* access modifiers changed from: private */
    public void stopIpClient() {
        handlePostDhcpSetup();
        if (this.mIpClient != null) {
            this.mIpClient.stop();
        }
    }

    /* access modifiers changed from: package-private */
    public void setSupplicantLogLevel() {
        this.mWifiNative.setSupplicantLogLevel(this.mVerboseLoggingEnabled);
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = false;
        this.mFromKeylogVerbose = false;
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS : 3000);
        } else {
            this.mVerboseLoggingEnabled = false;
            setLogRecSize(100);
        }
        configureVerboseHalLogging(this.mVerboseLoggingEnabled);
        setSupplicantLogLevel();
        this.mCountryCode.enableVerboseLogging(verbose);
        this.mWifiScoreReport.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mWifiMonitor.enableVerboseLogging(verbose);
        this.mWifiNative.enableVerboseLogging(verbose);
        this.mWifiConfigManager.enableVerboseLogging(verbose);
        this.mSupplicantStateTracker.enableVerboseLogging(verbose);
        this.mPasspointManager.enableVerboseLogging(verbose);
        this.mNetworkFactory.enableVerboseLogging(verbose);
        WifiConnectivityManager wifiConnectivityManager = this.mWifiConnectivityManager;
        if (verbose > 0) {
            z = true;
        }
        wifiConnectivityManager.mDbg = z;
        this.mLinkProbeManager.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mOppoWifiConnectionAlert.enableVerboseLogging(verbose);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(verbose);
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().enableVerboseLogging(verbose);
        }
        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiNetworkAvailable;
        if (oppoWifiNetworkSwitchEnhance != null) {
            oppoWifiNetworkSwitchEnhance.enableVerboseLogging(verbose);
        } else {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
            if (oppoWifiAssistantStateTraker != null) {
                oppoWifiAssistantStateTraker.enableVerboseLogging(verbose);
            }
        }
        if (this.mWifiInjector.getWifiDisconStat() != null) {
            this.mWifiInjector.getWifiDisconStat().enableVerboseLogging(verbose);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().enableVerboseLogging(verbose);
        }
        if (OppoDataStallHelper.getInstance() != null) {
            OppoDataStallHelper.getInstance().enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        WifiP2pServiceImpl wifiP2pServiceImpl2 = this.wifiP2pServiceImpl;
        if (wifiP2pServiceImpl2 != null) {
            wifiP2pServiceImpl2.enableVerboseLogging(verbose);
        }
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    /* access modifiers changed from: private */
    public boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039777);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = {(byte) (Integer.parseInt(ouiParts[0], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[1], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[2], 16) & Constants.BYTE_MASK)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, ouiBytes);
    }

    /* access modifiers changed from: private */
    public boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
        logd("connectToUserSelectNetwork netId " + netId + ", uid " + uid + ", forceReconnect = " + forceReconnect);
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (config == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (!this.mWifiConfigManager.enableNetwork(netId, true, uid) || !this.mWifiConfigManager.updateLastConnectUid(netId, uid)) {
            logi("connectToUserSelectNetwork Allowing uid " + uid + " with insufficient permissions to connect=" + netId);
        } else if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
            this.mWifiConnectivityManager.setUserConnectChoice(netId);
        }
        if (forceReconnect || this.mWifiInfo.getNetworkId() != netId) {
            this.mWifiConnectivityManager.prepareForForcedConnection(netId);
            if (uid == 1000) {
                this.mWifiMetrics.setNominatorForNetwork(config.networkId, 1);
            }
            startConnectToNetwork(netId, uid, "any");
            this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            logd("connectToUserSelectNetwork - setSwitchState to SwitchState.INIT");
        } else {
            logi("connectToUserSelectNetwork already connecting/connected=" + netId);
            if (getCurrentState() == this.mConnectedState || getCurrentState() == this.mObtainingIpState) {
                WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
                wifiConfigManager.sendAlertNetworksChangedBroadcast(netId, wifiConfigManager.getConfiguredNetwork(netId), -1, SupplicantState.COMPLETED);
            }
        }
        return true;
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    /* access modifiers changed from: package-private */
    public String reportOnTime() {
        long now = this.mClock.getWallClockMillis();
        StringBuilder sb = new StringBuilder();
        int i = this.mOnTime;
        int on = i - this.mOnTimeLastReport;
        this.mOnTimeLastReport = i;
        int i2 = this.mTxTime;
        int tx = i2 - this.mTxTimeLastReport;
        this.mTxTimeLastReport = i2;
        int i3 = this.mRxTime;
        int rx = i3 - this.mRxTimeLastReport;
        this.mRxTimeLastReport = i3;
        int period = (int) (now - this.mLastOntimeReportTimeStamp);
        this.mLastOntimeReportTimeStamp = now;
        try {
            sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf(period)));
            on = this.mOnTime - this.mOnTimeScreenStateChange;
            period = (int) (now - this.mLastScreenStateChangeTimeStamp);
            sb.append(String.format(" from screen [on:%d period:%d]", Integer.valueOf(on), Integer.valueOf(period)));
            return sb.toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "reportOnTime fatal exception, on=" + on + " tx=" + tx + " rx=" + rx + " period=" + period);
            log(e.toString());
            return "";
        }
    }

    /* access modifiers changed from: package-private */
    public WifiLinkLayerStats getWifiLinkLayerStats() {
        if (this.mInterfaceName == null) {
            loge("getWifiLinkLayerStats called without an interface");
            return null;
        }
        this.mLastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
        WifiLinkLayerStats stats = this.mWifiNative.getWifiLinkLayerStats(this.mInterfaceName);
        if (stats != null) {
            this.mOnTime = stats.on_time;
            this.mTxTime = stats.tx_time;
            this.mRxTime = stats.rx_time;
            this.mRunningBeaconCount = stats.beacon_rx;
            this.mWifiInfo.updatePacketRates(stats, this.mLastLinkLayerStatsUpdate);
        } else {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mInterfaceName), this.mFacade.getRxPackets(this.mInterfaceName), this.mLastLinkLayerStatsUpdate);
        }
        return stats;
    }

    private byte[] getDstMacForKeepalive(KeepalivePacketData packetData) throws SocketKeepalive.InvalidPacketException {
        try {
            return NativeUtil.macAddressToByteArray(macAddressFromRoute(RouteInfo.selectBestRoute(this.mLinkProperties.getRoutes(), packetData.dstAddress).getGateway().getHostAddress()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new SocketKeepalive.InvalidPacketException(-21);
        }
    }

    private static int getEtherProtoForKeepalive(KeepalivePacketData packetData) throws SocketKeepalive.InvalidPacketException {
        if (packetData.dstAddress instanceof Inet4Address) {
            return OsConstants.ETH_P_IP;
        }
        if (packetData.dstAddress instanceof Inet6Address) {
            return OsConstants.ETH_P_IPV6;
        }
        throw new SocketKeepalive.InvalidPacketException(-21);
    }

    /* access modifiers changed from: private */
    public int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        SocketKeepalive.InvalidPacketException e;
        try {
            byte[] packet = packetData.getPacket();
            try {
                try {
                    int ret = this.mWifiNative.startSendingOffloadedPacket(this.mInterfaceName, slot, getDstMacForKeepalive(packetData), packet, getEtherProtoForKeepalive(packetData), intervalSeconds * 1000);
                    if (ret == 0) {
                        return 0;
                    }
                    loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
                    return -31;
                } catch (SocketKeepalive.InvalidPacketException e2) {
                    e = e2;
                    return e.error;
                }
            } catch (SocketKeepalive.InvalidPacketException e3) {
                e = e3;
                return e.error;
            }
        } catch (SocketKeepalive.InvalidPacketException e4) {
            e = e4;
            return e.error;
        }
    }

    /* access modifiers changed from: private */
    public int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(this.mInterfaceName, slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    private int startRssiMonitoringOffload(byte maxRssi, byte minRssi, WifiNative.WifiRssiEventHandler rssiHandler) {
        return this.mWifiNative.startRssiMonitoring(this.mInterfaceName, maxRssi, minRssi, rssiHandler);
    }

    /* access modifiers changed from: private */
    public int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring(this.mInterfaceName);
    }

    public void setWifiStateForApiCalls(int newState) {
        this.mOppoWifiConnectionAlert.setWifiState(newState);
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker != null) {
            oppoWifiAssistantStateTraker.updateWifiState(newState);
        }
        if (newState == 0 || newState == 1 || newState == 2 || newState == 3 || newState == 4) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "setting wifi state to: " + newState);
            }
            if (newState == 1) {
                this.mSupplicantStateTracker.sendMessage(CMD_RESET_SUPPLICANT_STATE);
            }
            this.mWifiState.set(newState);
            return;
        }
        Log.d(TAG, "attempted to set an invalid state: " + newState);
    }

    public int syncGetWifiState() {
        return this.mWifiState.get();
    }

    public String syncGetWifiStateByName() {
        int i = this.mWifiState.get();
        if (i == 0) {
            return "disabling";
        }
        if (i == 1) {
            return "disabled";
        }
        if (i == 2) {
            return "enabling";
        }
        if (i == 3) {
            return "enabled";
        }
        if (i != 4) {
            return "[invalid state]";
        }
        return "unknown state";
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public boolean isWifiConnected() {
        return "CompletedState".equalsIgnoreCase(this.mSupplicantStateTracker.getSupplicantStateName());
    }

    public boolean isDisconnected() {
        return getCurrentState() == this.mDisconnectedState;
    }

    public boolean isSupplicantTransientState() {
        SupplicantState supplicantState = this.mWifiInfo.getSupplicantState();
        if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.AUTHENTICATING || supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
            if (!this.mVerboseLoggingEnabled) {
                return true;
            }
            Log.d(TAG, "Supplicant is under transient state: " + supplicantState);
            return true;
        } else if (!this.mVerboseLoggingEnabled) {
            return false;
        } else {
            Log.d(TAG, "Supplicant is under steady state: " + supplicantState);
            return false;
        }
    }

    public WifiInfo syncRequestConnectionInfo() {
        WifiInfo result = new WifiInfo(this.mWifiInfo);
        if ((!this.mEnableRssiPolling || result.getRssi() == -127) && result.getBSSID() != null) {
            Iterator<ScanResult> it = this.mScanRequestProxy.syncGetScanResultsList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ScanResult scanResult = it.next();
                if (result.getBSSID().equals(scanResult.BSSID)) {
                    int level = scanResult.level;
                    Log.d(TAG, "Adjust rssi from " + result.getRssi() + " to " + level);
                    result.setRssi(level);
                    break;
                }
            }
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

    public void handleIfaceDestroyed() {
        handleNetworkDisconnect();
    }

    public void setOperationalMode(int mode, String ifaceName) {
        if (this.mVerboseLoggingEnabled) {
            log("setting operational mode to " + String.valueOf(mode) + " for iface: " + ifaceName);
        }
        this.mModeChange = true;
        if (mode != 1) {
            if (this.mWifiInjector.getWifiDisconStat() != null) {
                this.mWifiInjector.getWifiDisconStat().updateDisconnectionReason(1, 3);
            }
            transitionTo(this.mDefaultState);
        } else if (ifaceName != null) {
            this.mInterfaceName = ifaceName;
            if (this.mWifiInjector.getWifiDisconStat() != null) {
                this.mWifiInjector.getWifiDisconStat().setInterfaceName(ifaceName);
            }
            if (OppoAutoConnectManager.getInstance() != null) {
                OppoAutoConnectManager.getInstance().setInterfaceName(ifaceName);
            }
            OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiNetworkAvailable;
            if (oppoWifiNetworkSwitchEnhance != null) {
                oppoWifiNetworkSwitchEnhance.setInterfaceName(ifaceName);
            }
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
            if (oppoWifiAssistantStateTraker != null) {
                oppoWifiAssistantStateTraker.setInterfaceName(ifaceName);
            }
            OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance2 = this.mWifiNetworkAvailable;
            if (oppoWifiNetworkSwitchEnhance2 != null) {
                oppoWifiNetworkSwitchEnhance2.readConfigAndUpdate();
            } else {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker;
                if (oppoWifiAssistantStateTraker2 != null) {
                    oppoWifiAssistantStateTraker2.readWifiNetworkRecord();
                    if (this.mAutoSwitch) {
                        this.mWifiNetworkStateTraker.disableNetworkWithoutInternet();
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - this.mLastScanTime <= 5000) {
                            OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance3 = this.mWifiNetworkAvailable;
                            if (oppoWifiNetworkSwitchEnhance3 != null) {
                                oppoWifiNetworkSwitchEnhance3.detectScanResult(currentTime);
                            } else {
                                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker3 = this.mWifiNetworkStateTraker;
                                if (oppoWifiAssistantStateTraker3 != null) {
                                    oppoWifiAssistantStateTraker3.detectScanResult(currentTime);
                                }
                            }
                        }
                    }
                }
            }
            this.mWifiConfigManager.enableAllNetworks();
            transitionTo(this.mDisconnectedState);
        } else {
            Log.e(TAG, "supposed to enter connect mode, but iface is null -> DefaultState");
            transitionTo(this.mDefaultState);
        }
        sendMessageAtFrontOfQueue(CMD_SET_OPERATIONAL_MODE);
    }

    public void takeBugReport(String bugTitle, String bugDetail) {
        this.mWifiDiagnostics.takeBugReport(bugTitle, bugDetail);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getOperationalModeForTest() {
        return this.mOperationalMode;
    }

    /* access modifiers changed from: protected */
    public WifiMulticastLockManager.FilterController getMcastLockManagerFilterController() {
        return this.mMcastLockManagerFilterController;
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString(EXTRA_OSU_ICON_QUERY_FILENAME, fileName);
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result == 1;
    }

    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_MATCH_PROVIDER_NETWORK, fqdn);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
    }

    public void disableEphemeralNetwork(String ssid) {
        if (ssid != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, ssid);
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

    private boolean messageIsNull(Message resultMsg) {
        if (resultMsg != null) {
            return false;
        }
        if (this.mNullMessageCounter.getAndIncrement() <= 0) {
            return true;
        }
        Log.wtf(TAG, "Persistent null Message", new RuntimeException());
        return true;
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ADD_OR_UPDATE_NETWORK, config);
        if (messageIsNull(resultMsg)) {
            return -1;
        }
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel, int targetUid) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_CONFIGURED_NETWORKS, uuid, targetUid);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /* access modifiers changed from: package-private */
    public Map<String, Map<Integer, List<ScanResult>>> syncGetAllMatchingFqdnsForScanResults(List<ScanResult> scanResults, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS, scanResults);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<String, Map<Integer, List<ScanResult>>> configs = (Map) resultMsg.obj;
        resultMsg.recycle();
        return configs;
    }

    public Map<OsuProvider, List<ScanResult>> syncGetMatchingOsuProviders(List<ScanResult> scanResults, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_MATCHING_OSU_PROVIDERS, scanResults);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<OsuProvider, List<ScanResult>> providers = (Map) resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    public Map<OsuProvider, PasspointConfiguration> syncGetMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS, osuProviders);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<OsuProvider, PasspointConfiguration> result = (Map) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetWifiConfigsForPasspointProfiles(List<String> fqdnList, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES, fqdnList);
        if (messageIsNull(resultMsg)) {
            return new ArrayList();
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        bundle.putString(EXTRA_PACKAGE_NAME, packageName);
        bundle.putParcelable(EXTRA_PASSPOINT_CONFIGURATION, config);
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG, bundle);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 == 1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_REMOVE_PASSPOINT_CONFIG, fqdn);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 == 1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_PASSPOINT_CONFIGS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        List<PasspointConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncStartSubscriptionProvisioning(int callingUid, OsuProvider provider, IProvisioningCallback callback, AsyncChannel channel) {
        Message msg = Message.obtain();
        msg.what = CMD_START_SUBSCRIPTION_PROVISIONING;
        msg.arg1 = callingUid;
        msg.obj = callback;
        msg.getData().putParcelable(EXTRA_OSU_PROVIDER, provider);
        Message resultMsg = channel.sendMessageSynchronously(msg);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != 0) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public long syncGetSupportedFeatures(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_SUPPORTED_FEATURES);
        if (messageIsNull(resultMsg)) {
            return 0;
        }
        long supportedFeatureSet = ((Long) resultMsg.obj).longValue();
        resultMsg.recycle();
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
            return supportedFeatureSet & -385;
        }
        return supportedFeatureSet;
    }

    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_LINK_LAYER_STATS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        WifiLinkLayerStats result = (WifiLinkLayerStats) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_REMOVE_NETWORK, networkId);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != -1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ENABLE_NETWORK, netId, disableOthers ? 1 : 0);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != -1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        Message resultMsg = channel.sendMessageSynchronously(151569, netId);
        boolean result = resultMsg.what != 151570;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        resultMsg.recycle();
        return result;
    }

    public void enableRssiPolling(boolean enabled) {
        sendMessage(CMD_ENABLE_RSSI_POLL, enabled ? 1 : 0, 0);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        sendMessage(CMD_SET_HIGH_PERF_MODE, enable ? 1 : 0, 0);
    }

    public synchronized void resetSimAuthNetworks(boolean simPresent, int simSlot) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent ? 1 : 0, simSlot);
    }

    public Network getCurrentNetwork() {
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent == null) {
                return null;
            }
            Network network = new Network(this.mNetworkAgent.netId);
            return network;
        }
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
                } else if (!this.mLastRunningWifiUids.equals(this.mRunningWifiUids)) {
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

    public void dumpIpClient(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mIpClient != null) {
            pw.println("IpClient logs have moved to dumpsys network_stack");
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        ClientModeImpl.super.dump(fd, pw, args);
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
        this.mCountryCode.dump(fd, pw, args);
        this.mNetworkFactory.dump(fd, pw, args);
        this.mUntrustedNetworkFactory.dump(fd, pw, args);
        pw.println("Wlan Wake Reasons:" + this.mWifiNative.getWlanWakeReasonCount());
        pw.println();
        this.mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        this.mPasspointManager.dump(pw);
        pw.println();
        this.mWifiDiagnostics.captureBugReportData(7);
        this.mWifiDiagnostics.dump(fd, pw, args);
        dumpIpClient(fd, pw, args);
        this.mWifiConnectivityManager.dump(fd, pw, args);
        this.mWifiInjector.getWakeupController().dump(fd, pw, args);
        this.mLinkProbeManager.dump(fd, pw, args);
        this.mWifiInjector.getWifiLastResortWatchdog().dump(fd, pw, args);
    }

    public void handleBootCompleted() {
        sendMessage(CMD_BOOT_COMPLETED);
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

    /* access modifiers changed from: private */
    public void logStateAndMessage(Message message, State state) {
        this.mMessageHandlingStatus = 0;
        if (this.mVerboseLoggingEnabled) {
            logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
        }
    }

    /* access modifiers changed from: protected */
    public boolean recordLogRec(Message msg) {
        if (msg.what != CMD_RSSI_POLL) {
            return true;
        }
        return this.mVerboseLoggingEnabled;
    }

    /* access modifiers changed from: protected */
    public String getLogRecString(Message msg) {
        if (!this.mVerboseLoggingEnabled) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getWhatToString(msg.what));
        sb.append(" ");
        sb.append("screen=");
        sb.append(this.mScreenOn ? "on" : "off");
        if (this.mMessageHandlingStatus != 0) {
            sb.append("(");
            sb.append(this.mMessageHandlingStatus);
            sb.append(")");
        }
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=" + msg.sendingUid);
        }
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /*{ENCODED_INT: 131124}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    WifiConfiguration config = (WifiConfiguration) msg.obj;
                    sb.append(" ");
                    sb.append(config.configKey());
                    sb.append(" prio=");
                    sb.append(config.priority);
                    sb.append(" status=");
                    sb.append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ");
                        sb.append(config.BSSID);
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=");
                            sb.append(curConfig.configKey());
                            sb.append(" prio=");
                            sb.append(curConfig.priority);
                            sb.append(" status=");
                            sb.append(curConfig.status);
                            break;
                        } else {
                            sb.append(" is current");
                            break;
                        }
                    }
                }
                break;
            case CMD_ENABLE_NETWORK /*{ENCODED_INT: 131126}*/:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=");
                    sb.append(key);
                }
                WifiConfiguration config2 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config2 != null && (key == null || !config2.configKey().equals(key))) {
                    sb.append(" target=");
                    sb.append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*{ENCODED_INT: 131131}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=");
                sb.append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_RSSI_POLL /*{ENCODED_INT: 131155}*/:
            case CMD_ONESHOT_RSSI_POLL /*{ENCODED_INT: 131156}*/:
            case CMD_UNWANTED_NETWORK /*{ENCODED_INT: 131216}*/:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ");
                    sb.append(this.mWifiInfo.getSSID());
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ");
                    sb.append(this.mWifiInfo.getBSSID());
                }
                sb.append(" rssi=");
                sb.append(this.mWifiInfo.getRssi());
                sb.append(" f=");
                sb.append(this.mWifiInfo.getFrequency());
                sb.append(" sc=");
                sb.append(this.mWifiInfo.score);
                sb.append(" link=");
                sb.append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", Double.valueOf(this.mWifiInfo.txSuccessRate)));
                sb.append(String.format(" %.1f,", Double.valueOf(this.mWifiInfo.txRetriesRate)));
                sb.append(String.format(" %.1f ", Double.valueOf(this.mWifiInfo.txBadRate)));
                sb.append(String.format(" rx=%.1f", Double.valueOf(this.mWifiInfo.rxSuccessRate)));
                sb.append(String.format(" bcn=%d", Integer.valueOf(this.mRunningBeaconCount)));
                String report = reportOnTime();
                if (report != null) {
                    sb.append(" ");
                    sb.append(report);
                }
                sb.append(String.format(" score=%d", Integer.valueOf(this.mWifiInfo.score)));
                break;
            case CMD_ROAM_WATCHDOG_TIMER /*{ENCODED_INT: 131166}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mRoamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /*{ENCODED_INT: 131168}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mDisconnectingWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /*{ENCODED_INT: 131211}*/:
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
                    sb.append(" ");
                    sb.append(this.mWifiInfo.getBSSID());
                }
                sb.append(String.format(" bcn=%d", Integer.valueOf(this.mRunningBeaconCount)));
                break;
            case CMD_UPDATE_LINKPROPERTIES /*{ENCODED_INT: 131212}*/:
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
            case CMD_TARGET_BSSID /*{ENCODED_INT: 131213}*/:
            case CMD_ASSOCIATED_BSSID /*{ENCODED_INT: 131219}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=");
                    sb.append((String) msg.obj);
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=");
                    sb.append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                break;
            case CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config3 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config3 != null) {
                    sb.append(" ");
                    sb.append(config3.configKey());
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ");
                    sb.append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                WifiConfiguration config4 = getCurrentWifiConfiguration();
                if (config4 != null) {
                    sb.append(config4.configKey());
                    break;
                }
                break;
            case CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = (ScanResult) msg.obj;
                if (result != null) {
                    Long now = Long.valueOf(this.mClock.getWallClockMillis());
                    sb.append(" bssid=");
                    sb.append(result.BSSID);
                    sb.append(" rssi=");
                    sb.append(result.level);
                    sb.append(" freq=");
                    sb.append(result.frequency);
                    if (result.seen <= 0 || result.seen >= now.longValue()) {
                        sb.append(" !seen=");
                        sb.append(result.seen);
                    } else {
                        sb.append(" seen=");
                        sb.append(now.longValue() - result.seen);
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ");
                    sb.append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                sb.append(" fail count=");
                sb.append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_IP_REACHABILITY_LOST /*{ENCODED_INT: 131221}*/:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131234}*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131235}*/:
            case CMD_RSSI_THRESHOLD_BREACHED /*{ENCODED_INT: 131236}*/:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /*{ENCODED_INT: 131272}*/:
                sb.append(" ");
                sb.append(msg.obj);
                break;
            case CMD_INSTALL_PACKET_FILTER /*{ENCODED_INT: 131274}*/:
                sb.append(" len=" + ((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /*{ENCODED_INT: 131275}*/:
                sb.append(" enabled=" + ((Boolean) msg.obj).booleanValue());
                break;
            case CMD_USER_SWITCH /*{ENCODED_INT: 131277}*/:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_PRE_DHCP_ACTION /*{ENCODED_INT: 131327}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=");
                sb.append(this.mWifiInfo.txSuccess);
                sb.append(",");
                sb.append(this.mWifiInfo.txBad);
                sb.append(",");
                sb.append(this.mWifiInfo.txRetries);
                break;
            case CMD_POST_DHCP_ACTION /*{ENCODED_INT: 131329}*/:
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*{ENCODED_INT: 143371}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    NetworkInfo info = (NetworkInfo) msg.obj;
                    NetworkInfo.State state = info.getState();
                    NetworkInfo.DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=");
                        sb.append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/");
                        sb.append(detailedState);
                        break;
                    }
                }
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ");
                sb.append(this.mLastBssid);
                sb.append(" nid=");
                sb.append(this.mLastNetworkId);
                WifiConfiguration config5 = getCurrentWifiConfiguration();
                if (config5 != null) {
                    sb.append(" ");
                    sb.append(config5.configKey());
                }
                String key2 = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key2 != null) {
                    sb.append(" last=");
                    sb.append(key2);
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                }
                sb.append(" nid=");
                sb.append(msg.arg1);
                sb.append(" reason=");
                sb.append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=");
                    sb.append(this.mLastBssid);
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=");
                    sb.append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=");
                    sb.append(this.mWifiInfo.getRssi());
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = (StateChangeResult) msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                    break;
                }
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                sb.append(" ");
                sb.append(" timedOut=" + Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = (String) msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(bssid);
                }
                sb.append(" blacklist=" + Boolean.toString(this.mDidBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config6 = (WifiConfiguration) msg.obj;
                if (config6 != null) {
                    sb.append(" ");
                    sb.append(config6.configKey());
                    sb.append(" nid=");
                    sb.append(config6.networkId);
                    if (config6.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config6.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (config6.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config6.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=");
                    sb.append(config6.creatorUid);
                    sb.append(" suid=");
                    sb.append(config6.lastUpdateUid);
                    WifiConfiguration.NetworkSelectionStatus netWorkSelectionStatus = config6.getNetworkSelectionStatus();
                    sb.append(" ajst=");
                    sb.append(netWorkSelectionStatus.getNetworkStatusString());
                    break;
                }
                break;
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config7 = (WifiConfiguration) msg.obj;
                if (config7 != null) {
                    sb.append(" ");
                    sb.append(config7.configKey());
                    sb.append(" nid=");
                    sb.append(config7.networkId);
                    if (config7.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config7.preSharedKey != null && !config7.preSharedKey.equals("*")) {
                        sb.append(" hasPSK");
                    }
                    if (config7.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config7.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=");
                    sb.append(config7.creatorUid);
                    sb.append(" suid=");
                    sb.append(config7.lastUpdateUid);
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

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        String s = sGetWhatToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case 69632:
                return "CMD_CHANNEL_HALF_CONNECTED";
            case 69636:
                return "CMD_CHANNEL_DISCONNECTED";
            case M_CMD_SET_POWER_SAVING_MODE /*{ENCODED_INT: 131487}*/:
                return "M_CMD_SET_POWER_SAVING_MODE";
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*{ENCODED_INT: 143361}*/:
                return "GROUP_CREATING_TIMED_OUT";
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*{ENCODED_INT: 143371}*/:
                return "P2P_CONNECTION_CHANGED";
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*{ENCODED_INT: 143372}*/:
                return "DISCONNECT_WIFI_REQUEST";
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*{ENCODED_INT: 143373}*/:
                return "DISCONNECT_WIFI_RESPONSE";
            case WifiP2pServiceImpl.SET_MIRACAST_MODE /*{ENCODED_INT: 143374}*/:
                return "SET_MIRACAST_MODE";
            case WifiP2pServiceImpl.BLOCK_DISCOVERY /*{ENCODED_INT: 143375}*/:
                return "BLOCK_DISCOVERY";
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                return "NETWORK_CONNECTION_EVENT";
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                return "NETWORK_DISCONNECTION_EVENT";
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                return "SUPPLICANT_STATE_CHANGE_EVENT";
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*{ENCODED_INT: 147463}*/:
                return "AUTHENTICATION_FAILURE_EVENT";
            case WifiMonitor.SUP_REQUEST_IDENTITY /*{ENCODED_INT: 147471}*/:
                return "SUP_REQUEST_IDENTITY";
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                return "ASSOCIATION_REJECTION_EVENT";
            case WifiMonitor.ANQP_DONE_EVENT /*{ENCODED_INT: 147500}*/:
                return "ANQP_DONE_EVENT";
            case WifiMonitor.GAS_QUERY_START_EVENT /*{ENCODED_INT: 147507}*/:
                return "GAS_QUERY_START_EVENT";
            case WifiMonitor.GAS_QUERY_DONE_EVENT /*{ENCODED_INT: 147508}*/:
                return "GAS_QUERY_DONE_EVENT";
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*{ENCODED_INT: 147509}*/:
                return "RX_HS20_ANQP_ICON_EVENT";
            case WifiMonitor.HS20_REMEDIATION_EVENT /*{ENCODED_INT: 147517}*/:
                return "HS20_REMEDIATION_EVENT";
            case 151553:
                return "CONNECT_NETWORK";
            case 151556:
                return "FORGET_NETWORK";
            case 151559:
                return "SAVE_NETWORK";
            case 151569:
                return "DISABLE_NETWORK";
            case 151572:
                return "RSSI_PKTCNT_FETCH";
            default:
                return "what:" + Integer.toString(what);
        }
    }

    private boolean isWfdMiracasting() {
        WifiConnectivityManager wifiConnectivityManager = this.mWifiConnectivityManager;
        if (wifiConnectivityManager == null) {
            return false;
        }
        int currentMiracastMode = wifiConnectivityManager.getMiracastMode();
        if (currentMiracastMode == 1 || currentMiracastMode == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (this.mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        if (this.mWifiInjector.getWifiDisconStat() != null) {
            this.mWifiInjector.getWifiDisconStat().syncScreenState(screenOn);
        }
        enableRssiPolling(screenOn);
        if (screenOn) {
            if (this.mVerboseLoggingEnabled) {
                log("force to scan when screen is on.");
            }
            if (isBootCompleted() && this.mScanRequestProxy != null && !isWfdMiracasting()) {
                this.mScanRequestProxy.startScan(1000, WIFI_PACKEG_NAME);
            }
        }
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
                this.mIdleRenewTimes = 0;
                this.mIdleGroupTimes = 0;
                this.mIdleDisConnTimes = 0;
                this.mIdleScanTimes = 0;
            }
        }
        getWifiLinkLayerStats();
        this.mOnTimeScreenStateChange = this.mOnTime;
        this.mLastScreenStateChangeTimeStamp = this.mLastLinkLayerStatsUpdate;
        this.mWifiMetrics.setScreenState(screenOn);
        this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        this.mNetworkFactory.handleScreenStateChanged(screenOn);
        WifiLockManager wifiLockManager = this.mWifiInjector.getWifiLockManager();
        if (wifiLockManager == null) {
            Log.w(TAG, "WifiLockManager not initialized, skipping screen state notification");
        } else {
            wifiLockManager.handleScreenStateChanged(screenOn);
        }
        this.mSarManager.handleScreenStateChanged(screenOn);
        if (this.mVerboseLoggingEnabled) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private boolean checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        if (this.mCm != null) {
            return true;
        }
        Log.e(TAG, "Cannot retrieve connectivity service");
        return false;
    }

    /* access modifiers changed from: private */
    public void setSuspendOptimizationsNative(int reason, boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            log("setSuspendOptimizationsNative: " + reason + " " + enabled + " -want " + this.mUserWantsSuspendOpt.get() + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
            if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
                if (this.mVerboseLoggingEnabled) {
                    log("setSuspendOptimizationsNative do it " + reason + " " + enabled + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
                }
                this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, false);
    }

    /* access modifiers changed from: private */
    public void setSuspendOptimizations(int reason, boolean enabled) {
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

    private int getFilterRssi(int rssi) {
        int sumRssi = 0;
        int tempCount = 0;
        for (int i = 0; i < 5; i++) {
            if (WIFI_DBG) {
                log("mRssiArray[" + i + "] = " + this.mRssiArray[i]);
            }
            int[] iArr = this.mRssiArray;
            if (iArr[i] < 0) {
                sumRssi += iArr[i];
                tempCount++;
            }
        }
        int[] iArr2 = this.mRssiArray;
        int i2 = this.mIndex;
        this.mIndex = i2 + 1;
        iArr2[i2] = rssi;
        this.mIndex %= 5;
        int result = (sumRssi + rssi) / (tempCount + 1);
        if (result > rssi) {
            return result;
        }
        return rssi;
    }

    /* access modifiers changed from: private */
    public void fetchRssiLinkSpeedAndFrequencyNative() {
        int i;
        WifiNative.SignalPollResult pollResult = this.mWifiNative.signalPoll(this.mInterfaceName);
        if (pollResult != null) {
            int newRssi = pollResult.currentRssi;
            int newTxLinkSpeed = pollResult.txBitrate;
            int newFrequency = pollResult.associationFrequency;
            int newRxLinkSpeed = pollResult.rxBitrate;
            if (this.mVerboseLoggingEnabled) {
                logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " TxLinkspeed=" + newTxLinkSpeed + " freq=" + newFrequency + " RxLinkSpeed=" + newRxLinkSpeed);
            }
            if (newRssi <= -127 || newRssi >= 200) {
                this.mWifiInfo.setRssi(-127);
                updateCapabilities();
            } else {
                if (newRssi > 0) {
                    Log.wtf(TAG, "Error! +ve value RSSI: " + newRssi);
                    newRssi += -256;
                }
                this.originRssi = newRssi;
                if (this.mWifiInjector.getWifiDisconStat() != null) {
                    this.mWifiInjector.getWifiDisconStat().syncRssi(newRssi);
                }
                newRssi = getFilterRssi(newRssi);
                this.mWifiInfo.setRssi(newRssi);
                int newSignalLevel = WifiManager.calculateSignalLevel(newRssi, 5);
                if (newSignalLevel != this.mLastSignalLevel) {
                    updateCapabilities();
                    int i2 = this.mLastSignalLevel;
                    if ((newSignalLevel - i2 > 1 || i2 - newSignalLevel > 1) && (i = this.mRssiCount) < 3) {
                        this.mRssiCount = i + 1;
                        log("Rssi change too fast: " + this.mLastSignalLevel + " to " + newSignalLevel + "; Count = " + this.mRssiCount);
                        newSignalLevel = (this.mLastSignalLevel + newSignalLevel) / 2;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Rssi change too fast: so average level to ");
                        sb.append(newSignalLevel);
                        log(sb.toString());
                        int sumRssi = 0;
                        int tempCount = 0;
                        for (int i3 = 0; i3 < 5; i3++) {
                            int[] iArr = this.mRssiArray;
                            if (iArr[i3] < 0) {
                                sumRssi += iArr[i3];
                                tempCount++;
                            }
                        }
                        if (tempCount > 0) {
                            sendRssiChangeBroadcast(sumRssi / tempCount);
                            setRssiLevel(sumRssi / tempCount);
                        }
                    } else {
                        this.mRssiCount = 0;
                        sendRssiChangeBroadcast(newRssi);
                        setRssiLevel(newRssi);
                    }
                }
                this.mLastSignalLevel = newSignalLevel;
            }
            MtkWifiServiceAdapter.updateRSSI(Integer.valueOf(newRssi), this.mWifiInfo.getIpAddress(), this.mLastNetworkId);
            if (newTxLinkSpeed > 0) {
                this.mWifiInfo.setLinkSpeed(newTxLinkSpeed);
                this.mWifiInfo.setTxLinkSpeedMbps(newTxLinkSpeed);
            }
            if (newRxLinkSpeed > 0) {
                this.mWifiInfo.setRxLinkSpeedMbps(newRxLinkSpeed);
            }
            if (newFrequency > 0) {
                this.mWifiInfo.setFrequency(newFrequency);
            }
            this.mWifiConfigManager.updateScanDetailCacheFromWifiInfo(this.mWifiInfo);
            this.mWifiMetrics.handlePollResult(this.mWifiInfo);
        }
    }

    /* access modifiers changed from: private */
    public void cleanWifiScore() {
        ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
        extendedWifiInfo.txBadRate = 0.0d;
        extendedWifiInfo.txSuccessRate = 0.0d;
        extendedWifiInfo.txRetriesRate = 0.0d;
        extendedWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
        this.mLastLinkLayerStats = null;
    }

    /* access modifiers changed from: private */
    public void updateLinkProperties(LinkProperties newLp) {
        if (this.mVerboseLoggingEnabled) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        LinkProperties linkProperties = this.mLinkProperties;
        if (linkProperties != null) {
            linkProperties.setMtu(getRomUpdateIntegerValue("NETWORK_MTU", 1500).intValue());
        }
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: " + this.mLastNetworkId);
            sb.append(" state: " + getNetworkDetailedState());
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
        this.mOppoDhcpRecord.saveDhcpRecord();
        this.mOppoDhcpRecord.clearDhcpResults();
        this.mLinkProperties.clear();
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private void setRssiLevel(int newRssi) {
        int newLevel = WifiManager.calculateSignalLevel(newRssi, 5);
        if (this.mVerboseLoggingEnabled) {
            log("rssi changes to: " + newRssi + " level: " + newLevel);
        }
        try {
            Settings.System.putInt(this.mContext.getContentResolver(), RSSI_LEVEL, newLevel);
        } catch (Exception e) {
            log("rssi level set exception");
        }
    }

    /* access modifiers changed from: private */
    public void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        StatsLog.write(38, WifiManager.calculateSignalLevel(newRssi, 5));
        if (!OppoWifiAssistantUtils.getInstance(this.mContext).isPrimaryWifi(this.mInterfaceName)) {
            logd("mInterfaceName isn't PrimaryWifi " + this.mInterfaceName);
            return;
        }
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
    }

    /* access modifiers changed from: private */
    public void sendNetworkStateChangeBroadcast(String bssid) {
        if (!this.mOppoDhcpRecord.isDoingSwitch()) {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
            if (oppoWifiAssistantStateTraker == null || oppoWifiAssistantStateTraker.getIsOppoManuConnect() || !this.mAutoSwitch || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.IDLE || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.SCANNING || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.FAILED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.BLOCKED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
                boolean wlanAssistAutoConnect = false;
                boolean replacePending = false;
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker;
                if (oppoWifiAssistantStateTraker2 != null && !oppoWifiAssistantStateTraker2.getIsOppoManuConnect() && this.mAutoSwitch) {
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
                NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
                networkInfo.setExtraInfo(null);
                intent.putExtra("networkInfo", networkInfo);
                WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
                sentWifiInfo.setMacAddress("02:00:00:00:00:00");
                sentWifiInfo.setBSSID("02:00:00:00:00:00");
                intent.putExtra("wifiInfo", sentWifiInfo);
                intent.putExtra("iface_name", this.mInterfaceName);
                if (OppoWifiAssistantUtils.getInstance(this.mContext).isPrimaryWifi(this.mInterfaceName)) {
                    this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                }
                if (OppoAutoConnectManager.getInstance() != null) {
                    OppoAutoConnectManager.getInstance().sendNetworkStateChangedEvt(intent);
                    return;
                }
                return;
            }
            log("state is " + this.mNetworkInfo.getDetailedState() + ",not bc.");
        } else if (this.mVerboseLoggingEnabled) {
            log("[1131400] Doing Dhcp Retry. not bc. new state" + this.mNetworkInfo.getDetailedState());
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

    /* access modifiers changed from: private */
    public boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
        boolean hidden = false;
        if (this.mIsAutoRoaming) {
            hidden = true;
        }
        if (this.mVerboseLoggingEnabled) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (this.mNetworkInfo.getExtraInfo() != null && this.mWifiInfo.getSSID() != null && !this.mWifiInfo.getSSID().equals("<unknown ssid>") && !this.mNetworkInfo.getExtraInfo().equals(this.mWifiInfo.getSSID())) {
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
        if (state == NetworkInfo.DetailedState.DISCONNECTED) {
            tryStartPnoScan();
        }
        if (NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK == state) {
            if (this.mWifiConfigManager.getNetworkEverCaptiveState()) {
                if (this.mVerboseLoggingEnabled) {
                    log("[1785291] set CAPTIVE_DIRECT_TO_NOTIFY for CaptiveState.");
                }
                DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, Integer.toString(2), false);
            } else {
                if (this.mVerboseLoggingEnabled) {
                    log("[1785291] set CAPTIVE_DIRECT_TO_NOTHING for CaptiveState");
                }
                DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, Integer.toString(3), false);
            }
        }
        if (NetworkInfo.DetailedState.CONNECTED == state) {
            if (OppoManuConnectManager.getInstance() == null || !OppoManuConnectManager.getInstance().isManuConnect()) {
                if (this.mVerboseLoggingEnabled) {
                    log("[1785291] set CAPTIVE_DIRECT_TO_NOTIFYs for ConnectedState.");
                }
                DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, Integer.toString(2), false);
            } else if (this.mOppoClientModeImplUtil.noNeedLoginByPkgname(OppoManuConnectManager.getInstance().getManuConnectUid())) {
                if (this.mVerboseLoggingEnabled) {
                    log("[1785291] set CAPTIVE_DIRECT_TO_NOTHING for ConnectedState.");
                }
                DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, Integer.toString(3), false);
            } else {
                if (this.mVerboseLoggingEnabled) {
                    log("[1785291] set CAPTIVE_DIRECT_TO_BROWSER for ConnectedState.");
                }
                DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, Integer.toString(1), false);
                broadcastCaptiveTypeChanged();
            }
        }
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private NetworkInfo.DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    /* access modifiers changed from: private */
    public SupplicantState handleSupplicantStateChange(Message message) {
        String tmpSsid;
        ScanDetail scanDetail;
        WifiConfiguration wc;
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        if (this.mPowerState == SupplicantState.COMPLETED && state == SupplicantState.GROUP_HANDSHAKE && !this.mScreenOn) {
            this.mIdleGroupTimes++;
        }
        this.mPowerState = state;
        this.mWifiScoreCard.noteSupplicantStateChanging(this.mWifiInfo, state);
        SupplicantState supplicantState = this.mLastSupplicantState;
        if (supplicantState != state && (supplicantState == SupplicantState.COMPLETED || state == SupplicantState.COMPLETED)) {
            removeMessages(CMD_TRIGGER_RESTORE_DELAY);
        }
        this.mWifiInfo.setSupplicantState(state);
        if (SupplicantState.isConnecting(state)) {
            if (stateChangeResult.networkId == -1) {
                Log.d(TAG, "invalid netid for wps connect,ignore!!");
            } else {
                this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
            }
            this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
            this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        } else {
            this.mWifiInfo.setNetworkId(-1);
            this.mWifiInfo.setBSSID(null);
            this.mWifiInfo.setSSID(null);
        }
        String ssidStr = this.mWifiInfo.getSSID();
        if (SupplicantState.isConnecting(state) && !((ssidStr != null && !ssidStr.equals("<unknown ssid>")) || (wc = this.mWifiConfigManager.getConfiguredNetwork(this.mWifiInfo.getNetworkId())) == null || wc.SSID == null)) {
            WifiSsid ssid = null;
            String configSsidStr = wc.SSID;
            if (configSsidStr != null && configSsidStr.startsWith("\"") && configSsidStr.endsWith("\"")) {
                configSsidStr = configSsidStr.substring(1, configSsidStr.length() - 1);
            }
            if (configSsidStr != null) {
                ssid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(NativeUtil.stringToByteArrayList(configSsidStr)));
                if (GbkUtil.isGbkSsid(configSsidStr)) {
                    ssid.mIsGbkEncoding = true;
                }
            }
            if (ssid != null) {
                logd("reset wifissid to" + ssid.toString());
                this.mWifiInfo.setSSID(ssid);
            }
        }
        updateL2KeyAndGroupHint();
        updateCapabilities();
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
            this.mWifiInfo.setTrusted(config.trusted);
            this.mWifiInfo.setOsuAp(config.osu);
            if (config.fromWifiNetworkSpecifier || config.fromWifiNetworkSuggestion) {
                this.mWifiInfo.setNetworkSuggestionOrSpecifierPackageName(config.creatorName);
            }
            ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
            if (!(scanDetailCache == null || (scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID)) == null)) {
                this.mWifiInfo.setFrequency(scanDetail.getScanResult().frequency);
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                if (networkDetail != null && networkDetail.getAnt() == NetworkDetail.Ant.ChargeablePublic) {
                    this.mWifiInfo.setMeteredHint(true);
                }
            }
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleSupplicantStateChanged(Message.obtain(message));
        }
        this.mOppoWifiConnectionAlert.sendSupplicantStateChangeEvent(stateChangeResult);
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker != null) {
            oppoWifiAssistantStateTraker.sendMessage(message.what, message.arg1, message.arg2, message.obj);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiSupplicantConnectStateChangedEvt(Message.obtain(message));
        }
        if (1 == syncGetWifiState() && SupplicantState.isConnecting(state)) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "wrong supplicant action, disconnect supplicant!!");
            }
            WifiNative wifiNative = this.mWifiNative;
            if (wifiNative != null) {
                wifiNative.disconnect(this.mInterfaceName);
            }
        }
        if (!(config == null || this.mWifiInfo == null || this.mWifiNative == null || !SupplicantState.isConnecting(state) || (tmpSsid = this.mWifiInfo.getSSID()) == null || tmpSsid.equals(config.SSID))) {
            Log.d(TAG, "ap ssid may changed ,disconnect");
            this.mWifiNative.disconnect(this.mInterfaceName);
        }
        this.mWifiScoreCard.noteSupplicantStateChanged(this.mWifiInfo);
        if (this.mWifiConfigManager.isAutoConnectSwitchEnable() && this.mLastSupplicantState == SupplicantState.COMPLETED && state == SupplicantState.DISCONNECTED && config != null && this.mWifiNative != null && config.userApproved == 1001) {
            this.mWifiNative.removeNetworkIfCurrent(this.mInterfaceName, config.networkId);
        }
        if (this.mLastSupplicantState != state) {
            this.mLastSupplicantState = state;
        }
        return state;
    }

    private void updateL2KeyAndGroupHint() {
        if (this.mIpClient != null) {
            Pair<String, String> p = this.mWifiScoreCard.getL2KeyAndGroupHint(this.mWifiInfo);
            if (p.equals(this.mLastL2KeyAndGroupHint)) {
                return;
            }
            if (this.mIpClient.setL2KeyAndGroupHint((String) p.first, (String) p.second)) {
                this.mLastL2KeyAndGroupHint = p;
            } else {
                this.mLastL2KeyAndGroupHint = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkDisconnect() {
        if (this.mVerboseLoggingEnabled) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (wifiConfig != null) {
            this.mWifiInjector.getWakeupController().setLastDisconnectInfo(ScanResultMatchInfo.fromWifiConfiguration(wifiConfig));
            this.mWifiNetworkSuggestionsManager.handleDisconnect(wifiConfig, getCurrentBSSID());
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleConnectStateChanged(false, this.mLastNetworkId);
        }
        if (!(this.mWifiNetworkStateTraker == null || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED)) {
            this.mWifiNetworkStateTraker.setNetworkDetailState(this.mLastNetworkId, NetworkInfo.DetailedState.DISCONNECTED, this.mLastBssid);
        }
        this.mNetworkDetectValid = false;
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(false, this.mLastNetworkId);
        }
        if (this.mWifiInjector.getWifiDisconStat() != null) {
            this.mWifiInjector.getWifiDisconStat().startDisconnectionDetect();
        }
        stopRssiMonitoringOffload();
        clearCurrentConfigBSSID("handleNetworkDisconnect");
        if (!this.mScreenOn) {
            this.mIdleDisConnTimes++;
        }
        stopIpClient();
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mIsAutoRoaming = false;
        if (OppoDataStallHelper.getInstance() != null) {
            OppoDataStallHelper.getInstance().setHasInetAccess(false);
            OppoDataStallHelper.getInstance().setEverHadInetAccess(false);
            OppoDataStallHelper.getInstance().setEverHadGwAccess(false);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent != null) {
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent = null;
            }
        }
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        this.mLastLinkLayerStats = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
        this.mWifiScoreCard.resetConnectionState();
        updateL2KeyAndGroupHint();
        this.mConnectedId = -1;
        if (this.mSmartGearFeature && OppoDataStallHelper.getInstance() != null) {
            OppoDataStallHelper.getInstance().setSmartGearDisconnect();
        }
    }

    /* access modifiers changed from: package-private */
    public void handlePreDhcpSetup() {
        if (!this.mBluetoothConnectionActive) {
            this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 1);
        }
        setSuspendOptimizationsNative(1, false);
        setPowerSave(false);
        getWifiLinkLayerStats();
        if (this.mWifiP2pChannel != null) {
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = 1;
            msg.arg2 = CMD_PRE_DHCP_ACTION_COMPLETE;
            msg.obj = this;
            this.mWifiP2pChannel.sendMessage(msg);
            return;
        }
        sendMessage(CMD_PRE_DHCP_ACTION_COMPLETE);
    }

    /* access modifiers changed from: package-private */
    public void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        setPowerSave(true);
        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 2);
    }

    public boolean setPowerSave(boolean ps) {
        if (this.mInterfaceName != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Setting power save for: " + this.mInterfaceName + " to: " + ps);
            }
            this.mWifiNative.setPowerSave(this.mInterfaceName, ps);
            return true;
        }
        Log.e(TAG, "Failed to setPowerSave, interfaceName is null");
        return false;
    }

    public boolean setLowLatencyMode(boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Setting low latency mode to " + enabled);
        }
        if (this.mWifiNative.setLowLatencyMode(enabled)) {
            return true;
        }
        Log.e(TAG, "Failed to setLowLatencyMode");
        return false;
    }

    /* access modifiers changed from: private */
    public void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mWifiDiagnostics.reportConnectionEvent((byte) 0);
        this.mWrongPasswordNotifier.onNewConnectionAttempt();
        removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, 60000);
    }

    private void handleConnectionAttemptEndForDiagnostics(int level2FailureCode) {
        if (level2FailureCode != 1 && level2FailureCode != 5) {
            removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
            this.mWifiDiagnostics.reportConnectionEvent((byte) 2);
        }
    }

    /* access modifiers changed from: private */
    public void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode, int level2FailureReason) {
        if (level2FailureCode != 1) {
            this.mWifiScoreCard.noteConnectionFailure(this.mWifiInfo, level2FailureCode, connectivityFailureCode);
        }
        WifiConfiguration configuration = getCurrentWifiConfiguration();
        if (configuration == null) {
            configuration = getTargetWifiConfiguration();
        }
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode, level2FailureReason);
        this.mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        if (configuration != null) {
            this.mNetworkFactory.handleConnectionAttemptEnded(level2FailureCode, configuration);
            this.mWifiNetworkSuggestionsManager.handleConnectionAttemptEnded(level2FailureCode, configuration, getCurrentBSSID());
        }
        handleConnectionAttemptEndForDiagnostics(level2FailureCode);
    }

    /* access modifiers changed from: private */
    public void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            StringBuilder sb = new StringBuilder();
            sb.append("link address ");
            sb.append(dhcpResults.ipAddress);
            logd(sb.toString());
        }
        this.mOppoDhcpRecord.syncDhcpResults(dhcpResults, getCurrentConfigKey());
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
            this.mWifiInfo.setTrusted(config.trusted);
        }
        if (dhcpResults.hasMeteredHint()) {
            this.mWifiInfo.setMeteredHint(true);
        }
        updateCapabilities(config);
    }

    /* access modifiers changed from: private */
    public void handleSuccessfulIpConfiguration() {
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
        this.mWifiScoreCard.noteIpConfiguration(this.mWifiInfo);
    }

    /* access modifiers changed from: private */
    public void handleIPv4Failure() {
        this.mWifiDiagnostics.captureBugReportData(4);
        if (this.mVerboseLoggingEnabled) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(10, 2, 0);
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Failure");
        }
        this.mOppoDhcpRecord.rmDhcpRecord(getCurrentConfigKey());
        this.mOppoDhcpRecord.clearDhcpResults();
        if (this.mOppoDhcpRecord.isDoingSwitch()) {
            this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
        }
    }

    /* access modifiers changed from: private */
    public void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker != null) {
            oppoWifiAssistantStateTraker.sendMessage(CMD_IP_CONFIGURATION_LOST);
        }
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    /* access modifiers changed from: private */
    public void handleIpReachabilityLost() {
        this.mWifiScoreCard.noteIpReachabilityLost(this.mWifiInfo);
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    private String macAddressFromRoute(String ipAddress) {
        String macAddress = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            reader2.readLine();
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split("[ ]+");
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
                loge("Did not find remoteAddress {" + ipAddress + "} in /proc/net/arp");
            }
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
        return macAddress;
    }

    /* access modifiers changed from: private */
    public boolean isPermanentWrongPasswordFailure(int networkId, int reasonCode) {
        if (reasonCode != 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void registerNetworkFactory() {
        if (checkAndSetConnectivityInstance()) {
            this.mNetworkFactory.setScoreFilter(79);
            this.mNetworkFactory.register();
            this.mUntrustedNetworkFactory.register();
        }
    }

    /* access modifiers changed from: private */
    public void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.wifiP2pServiceImpl = IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.wifiP2pServiceImpl != null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.wifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
        }
    }

    /* access modifiers changed from: private */
    public void configureRandomizedMacAddress(WifiConfiguration config) {
        if (config == null) {
            Log.e(TAG, "No config to change MAC address to");
            return;
        }
        MacAddress currentMac = MacAddress.fromString(this.mWifiNative.getMacAddress(this.mInterfaceName));
        MacAddress newMac = config.getOrCreateRandomizedMacAddress();
        this.mWifiConfigManager.setNetworkRandomizedMacAddress(config.networkId, newMac);
        if (!WifiConfiguration.isValidMacAddressForRandomization(newMac)) {
            Log.wtf(TAG, "Config generated an invalid MAC address");
        } else if (currentMac.equals(newMac)) {
            Log.d(TAG, "No changes in MAC address");
        } else {
            this.mWifiMetrics.logStaEvent(17, config);
            boolean setMacSuccess = this.mWifiNative.setMacAddress(this.mInterfaceName, newMac);
            Log.d(TAG, "ConnectedMacRandomization SSID(" + config.getPrintableSsid() + "). setMacAddress(" + newMac.toString() + ") from " + currentMac.toString() + " = " + setMacSuccess);
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentMacToFactoryMac(WifiConfiguration config) {
        MacAddress factoryMac = this.mWifiNative.getFactoryMacAddress(this.mInterfaceName);
        if (factoryMac == null) {
            Log.e(TAG, "Fail to set factory MAC address. Factory MAC is null.");
        } else if (TextUtils.equals(this.mWifiNative.getMacAddress(this.mInterfaceName), factoryMac.toString())) {
        } else {
            if (this.mWifiNative.setMacAddress(this.mInterfaceName, factoryMac)) {
                this.mWifiMetrics.logStaEvent(17, config);
                return;
            }
            Log.e(TAG, "Failed to set MAC address to '" + factoryMac.toString() + "'");
        }
    }

    public boolean isConnectedMacRandomizationEnabled() {
        return this.mConnectedMacRandomzationSupported;
    }

    public void failureDetected(int reason) {
        this.mWifiInjector.getSelfRecovery().trigger(2);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            SupplicantState state;
            int removeResult = -1;
            boolean disableOthers = false;
            switch (message.what) {
                case 0:
                    Log.wtf(ClientModeImpl.TAG, "Error! empty message encountered");
                    break;
                case 69632:
                    if (((AsyncChannel) message.obj) == ClientModeImpl.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            ClientModeImpl.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        } else {
                            boolean unused = ClientModeImpl.this.p2pSendMessage(69633);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("got HALF_CONNECTED for unknown channel");
                        break;
                    }
                case 69636:
                    if (((AsyncChannel) message.obj) == ClientModeImpl.this.mWifiP2pChannel) {
                        ClientModeImpl.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*{ENCODED_INT: 131103}*/:
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    boolean unused2 = clientModeImpl.mBluetoothConnectionActive = disableOthers;
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_NETWORK /*{ENCODED_INT: 131124}*/:
                    WifiConfiguration config = (WifiConfiguration) message.obj;
                    ClientModeImpl.this.convertToQuotedSSID(config);
                    WifiConfiguration currConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mWifiInfo.getNetworkId());
                    WifiConfiguration saveConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(config.configKey(true));
                    if (saveConfig != null && config.hiddenSSID) {
                        if (currConfig != null && ClientModeImpl.this.mWifiInfo.getNetworkId() == saveConfig.networkId && "ConnectedState".equalsIgnoreCase(ClientModeImpl.this.getCurrentState().getName()) && config.macRandomizationSetting == currConfig.macRandomizationSetting) {
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                Log.d(ClientModeImpl.TAG, "trying to add a already connected network,ignore");
                            }
                            ClientModeImpl.this.replyToMessage(message, ClientModeImpl.CMD_ADD_OR_UPDATE_NETWORK, saveConfig.networkId);
                            break;
                        } else {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.setLastUpdatedWifiConfiguration(ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithPassword(saveConfig.networkId));
                        }
                    }
                    NetworkUpdateResult result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                    if (!result.isSuccess()) {
                        int unused3 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                    }
                    if ((WifiConfigurationUtil.hasMacRandomizationSettingsChanged(currConfig, config) || result.hasCredentialChanged()) && ClientModeImpl.this.mWifiInfo != null && ClientModeImpl.this.mWifiInfo.getNetworkId() == config.networkId && ClientModeImpl.this.isNetworkConnecting()) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            Log.d(ClientModeImpl.TAG, "trying to disable conecting ap for Credential Changed or MacRandomizationSettingsChanged");
                        }
                        ClientModeImpl.this.mWifiConfigManager.disableNetwork(config.networkId, message.sendingUid);
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case ClientModeImpl.CMD_REMOVE_NETWORK /*{ENCODED_INT: 131125}*/:
                    boolean unused4 = ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, false);
                    break;
                case ClientModeImpl.CMD_ENABLE_NETWORK /*{ENCODED_INT: 131126}*/:
                    if (message.arg2 == 1) {
                        disableOthers = true;
                    }
                    boolean ok = ClientModeImpl.this.mWifiConfigManager.enableNetwork(message.arg1, disableOthers, message.sendingUid);
                    if (!ok) {
                        int unused5 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                    }
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    int i = message.what;
                    if (ok) {
                        removeResult = 1;
                    }
                    clientModeImpl2.replyToMessage(message, i, removeResult);
                    break;
                case ClientModeImpl.CMD_GET_CONFIGURED_NETWORKS /*{ENCODED_INT: 131131}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mWifiConfigManager.getSavedNetworks(message.arg2));
                    break;
                case ClientModeImpl.CMD_GET_SUPPORTED_FEATURES /*{ENCODED_INT: 131133}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, Long.valueOf(ClientModeImpl.this.mWifiNative.getSupportedFeatureSet(ClientModeImpl.this.mInterfaceName)));
                    break;
                case ClientModeImpl.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*{ENCODED_INT: 131134}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case ClientModeImpl.CMD_GET_LINK_LAYER_STATS /*{ENCODED_INT: 131135}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, null);
                    break;
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /*{ENCODED_INT: 131144}*/:
                case ClientModeImpl.M_CMD_SET_POWER_SAVING_MODE /*{ENCODED_INT: 131487}*/:
                    break;
                case ClientModeImpl.CMD_DISCONNECT /*{ENCODED_INT: 131145}*/:
                case ClientModeImpl.CMD_RECONNECT /*{ENCODED_INT: 131146}*/:
                case ClientModeImpl.CMD_REASSOCIATE /*{ENCODED_INT: 131147}*/:
                case ClientModeImpl.CMD_RSSI_POLL /*{ENCODED_INT: 131155}*/:
                case ClientModeImpl.CMD_ONESHOT_RSSI_POLL /*{ENCODED_INT: 131156}*/:
                case ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER /*{ENCODED_INT: 131166}*/:
                case ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER /*{ENCODED_INT: 131168}*/:
                case ClientModeImpl.CMD_DISABLE_EPHEMERAL_NETWORK /*{ENCODED_INT: 131170}*/:
                case ClientModeImpl.CMD_TARGET_BSSID /*{ENCODED_INT: 131213}*/:
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                case ClientModeImpl.CMD_UNWANTED_NETWORK /*{ENCODED_INT: 131216}*/:
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /*{ENCODED_INT: 131219}*/:
                case ClientModeImpl.CMD_PRE_DHCP_ACTION /*{ENCODED_INT: 131327}*/:
                case ClientModeImpl.CMD_PRE_DHCP_ACTION_COMPLETE /*{ENCODED_INT: 131328}*/:
                case ClientModeImpl.CMD_POST_DHCP_ACTION /*{ENCODED_INT: 131329}*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*{ENCODED_INT: 147463}*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*{ENCODED_INT: 147466}*/:
                case WifiMonitor.SUP_REQUEST_IDENTITY /*{ENCODED_INT: 147471}*/:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*{ENCODED_INT: 147472}*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                    int unused6 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /*{ENCODED_INT: 131149}*/:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizations(2, true);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizations(2, false);
                        break;
                    }
                case ClientModeImpl.CMD_ENABLE_RSSI_POLL /*{ENCODED_INT: 131154}*/:
                    ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    boolean unused7 = clientModeImpl3.mEnableRssiPolling = disableOthers;
                    break;
                case ClientModeImpl.CMD_SET_SUSPEND_OPT_ENABLED /*{ENCODED_INT: 131158}*/:
                    boolean isScanOnlyMode = false;
                    if (ClientModeImpl.this.mActiveModeWarden == null) {
                        ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                        ActiveModeWarden unused8 = clientModeImpl4.mActiveModeWarden = clientModeImpl4.mWifiInjector.getActiveModeWarden();
                    }
                    if (ClientModeImpl.this.mActiveModeWarden != null) {
                        isScanOnlyMode = Objects.equals("ScanOnlyModeActiveState", ClientModeImpl.this.mActiveModeWarden.getCurrentMode());
                    }
                    Log.d(ClientModeImpl.TAG, "DefaultState isScanOnlyMode: " + isScanOnlyMode);
                    if (!isScanOnlyMode) {
                        if (message.arg1 != 1) {
                            ClientModeImpl.this.setSuspendOptimizations(4, false);
                            break;
                        } else {
                            if (message.arg2 == 1) {
                                ClientModeImpl.this.mSuspendWakeLock.release();
                            }
                            ClientModeImpl.this.setSuspendOptimizations(4, true);
                            break;
                        }
                    } else if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, false);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, true);
                        if (message.arg2 == 1) {
                            ClientModeImpl.this.mSuspendWakeLock.release();
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_SCREEN_STATE_CHANGED /*{ENCODED_INT: 131167}*/:
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    clientModeImpl5.handleScreenStateChanged(disableOthers);
                    break;
                case ClientModeImpl.CMD_REMOVE_APP_CONFIGURATIONS /*{ENCODED_INT: 131169}*/:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_FIRMWARE_ALERT /*{ENCODED_INT: 131172}*/:
                    if (ClientModeImpl.this.mSmartGearFeature) {
                        if (message.arg1 > 256) {
                            int reason = message.arg1 - 256;
                            ClientModeImpl.this.loge("Received DATA_STALL, reason=" + reason);
                            if (OppoDataStallHelper.getInstance() != null) {
                                OppoDataStallHelper.getInstance().checkSmartGearState(reason);
                                break;
                            }
                        }
                    } else {
                        ClientModeImpl.this.loge("Error! default state unhandled message" + message);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /*{ENCODED_INT: 131173}*/:
                    int unused9 = ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_QUERY_OSU_ICON /*{ENCODED_INT: 131176}*/:
                case ClientModeImpl.CMD_MATCH_PROVIDER_NETWORK /*{ENCODED_INT: 131177}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what);
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*{ENCODED_INT: 131178}*/:
                    Bundle bundle = (Bundle) message.obj;
                    if (ClientModeImpl.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) bundle.getParcelable(ClientModeImpl.EXTRA_PASSPOINT_CONFIGURATION), bundle.getInt(ClientModeImpl.EXTRA_UID), bundle.getString(ClientModeImpl.EXTRA_PACKAGE_NAME))) {
                        removeResult = 1;
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, removeResult);
                    break;
                case ClientModeImpl.CMD_REMOVE_PASSPOINT_CONFIG /*{ENCODED_INT: 131179}*/:
                    if (ClientModeImpl.this.mPasspointManager.removeProvider((String) message.obj)) {
                        removeResult = 1;
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, removeResult);
                    break;
                case ClientModeImpl.CMD_GET_PASSPOINT_CONFIGS /*{ENCODED_INT: 131180}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getProviderConfigs());
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_OSU_PROVIDERS /*{ENCODED_INT: 131181}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /*{ENCODED_INT: 131182}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /*{ENCODED_INT: 131184}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, new ArrayList());
                    break;
                case ClientModeImpl.CMD_BOOT_COMPLETED /*{ENCODED_INT: 131206}*/:
                    ClientModeImpl.this.getAdditionalWifiServiceInterfaces();
                    new MemoryStoreImpl(ClientModeImpl.this.mContext, ClientModeImpl.this.mWifiInjector, ClientModeImpl.this.mWifiScoreCard).start();
                    if (!ClientModeImpl.this.hasLoadStore) {
                        if (!ClientModeImpl.this.mWifiConfigManager.loadFromStore()) {
                            Log.e(ClientModeImpl.TAG, "Failed to load from config store");
                        } else {
                            boolean unused10 = ClientModeImpl.this.hasLoadStore = true;
                        }
                    }
                    ClientModeImpl.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                    ClientModeImpl.this.registerNetworkFactory();
                    break;
                case ClientModeImpl.CMD_INITIALIZE /*{ENCODED_INT: 131207}*/:
                    boolean ok2 = ClientModeImpl.this.mWifiNative.initialize();
                    ClientModeImpl.this.mPasspointManager.initializeProvisioner(ClientModeImpl.this.mWifiInjector.getWifiServiceHandlerThread().getLooper());
                    ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                    int i2 = message.what;
                    if (ok2) {
                        removeResult = 1;
                    }
                    clientModeImpl6.replyToMessage(message, i2, removeResult);
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL /*{ENCODED_INT: 131210}*/:
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /*{ENCODED_INT: 131211}*/:
                case ClientModeImpl.CMD_IP_REACHABILITY_LOST /*{ENCODED_INT: 131221}*/:
                    int unused11 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_UPDATE_LINKPROPERTIES /*{ENCODED_INT: 131212}*/:
                    ClientModeImpl.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case ClientModeImpl.CMD_REMOVE_USER_CONFIGURATIONS /*{ENCODED_INT: 131224}*/:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD /*{ENCODED_INT: 131232}*/:
                case ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD /*{ENCODED_INT: 131233}*/:
                case ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /*{ENCODED_INT: 131281}*/:
                case ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /*{ENCODED_INT: 131282}*/:
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131234}*/:
                    int unused12 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131235}*/:
                    int unused13 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /*{ENCODED_INT: 131240}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_INSTALL_PACKET_FILTER /*{ENCODED_INT: 131274}*/:
                    ClientModeImpl.this.mWifiNative.installPacketFilter(ClientModeImpl.this.mInterfaceName, (byte[]) message.obj);
                    break;
                case ClientModeImpl.CMD_SET_FALLBACK_PACKET_FILTERING /*{ENCODED_INT: 131275}*/:
                    if (!((Boolean) message.obj).booleanValue()) {
                        ClientModeImpl.this.mWifiNative.stopFilteringMulticastV4Packets(ClientModeImpl.this.mInterfaceName);
                        break;
                    } else {
                        ClientModeImpl.this.mWifiNative.startFilteringMulticastV4Packets(ClientModeImpl.this.mInterfaceName);
                        break;
                    }
                case ClientModeImpl.CMD_USER_SWITCH /*{ENCODED_INT: 131277}*/:
                    Set<Integer> removedNetworkIds = ClientModeImpl.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_USER_UNLOCK /*{ENCODED_INT: 131278}*/:
                    ClientModeImpl.this.mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case ClientModeImpl.CMD_USER_STOP /*{ENCODED_INT: 131279}*/:
                    ClientModeImpl.this.mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case ClientModeImpl.CMD_READ_PACKET_FILTER /*{ENCODED_INT: 131280}*/:
                    byte[] data = ClientModeImpl.this.mWifiNative.readPacketFilter(ClientModeImpl.this.mInterfaceName);
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.readPacketFilterComplete(data);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_DIAGS_CONNECT_TIMEOUT /*{ENCODED_INT: 131324}*/:
                    ClientModeImpl.this.mWifiDiagnostics.reportConnectionEvent((byte) 3);
                    break;
                case ClientModeImpl.CMD_START_SUBSCRIPTION_PROVISIONING /*{ENCODED_INT: 131326}*/:
                    ClientModeImpl.this.replyToMessage(message, message.what, 0);
                    break;
                case ClientModeImpl.CMD_TRIGGER_MINIDUMP_ENABLE /*{ENCODED_INT: 131478}*/:
                    ClientModeImpl.this.handleWifiMinidumpSwitch();
                    break;
                case ClientModeImpl.CMD_START_SCAN /*{ENCODED_INT: 131479}*/:
                    String str = (String) message.obj;
                    if (ClientModeImpl.this.isBootCompleted()) {
                        ClientModeImpl.this.mScanRequestProxy.startScan(message.arg1, (String) message.obj);
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*{ENCODED_INT: 143371}*/:
                    NetworkInfo info = (NetworkInfo) message.obj;
                    ClientModeImpl.this.mP2pConnected.set(info.isConnected());
                    if (ClientModeImpl.this.mWifiConnectivityManager != null) {
                        ClientModeImpl.this.mWifiConnectivityManager.saveP2pConnectedStatus(info.isConnected());
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*{ENCODED_INT: 143372}*/:
                    ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    boolean unused14 = clientModeImpl7.mTemporarilyDisconnectWifi = disableOthers;
                    ClientModeImpl.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiP2pServiceImpl.SET_MIRACAST_MODE /*{ENCODED_INT: 143374}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.logd("SET_MIRACAST_MODE: " + message.arg1);
                    }
                    if (ClientModeImpl.this.mWifiConnectivityManager != null) {
                        ClientModeImpl.this.mWifiConnectivityManager.saveMiracastMode(message.arg1);
                        break;
                    }
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*{ENCODED_INT: 147461}*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*{ENCODED_INT: 147473}*/:
                case WifiMonitor.PNO_SCAN_RESULTS_EVENT /*{ENCODED_INT: 147474}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        Log.d(ClientModeImpl.TAG, "receive scan result message mIdleScanTimes=" + ClientModeImpl.this.mIdleScanTimes);
                    }
                    if (!ClientModeImpl.this.mScreenOn) {
                        ClientModeImpl.access$5008(ClientModeImpl.this);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (!(stateChangeResult == null || ClientModeImpl.this.mLastSupplicantState == (state = stateChangeResult.state))) {
                        SupplicantState unused15 = ClientModeImpl.this.mLastSupplicantState = state;
                        break;
                    }
                case 151553:
                    ClientModeImpl.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    boolean unused16 = ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, true);
                    break;
                case 151559:
                    NetworkUpdateResult unused17 = ClientModeImpl.this.saveNetworkConfigAndSendReply(message);
                    break;
                case 151562:
                    ClientModeImpl.this.replyToMessage(message, 151564, 2);
                    break;
                case 151566:
                    ClientModeImpl.this.replyToMessage(message, 151567, 2);
                    break;
                case 151569:
                    ClientModeImpl.this.replyToMessage(message, 151570, 2);
                    break;
                case 151572:
                    ClientModeImpl.this.replyToMessage(message, 151574, 2);
                    break;
                default:
                    ClientModeImpl.this.loge("Error! unhandled message" + message);
                    break;
            }
            if (1 == 1) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void setupClientMode() {
        Log.d(TAG, "setupClientMode() ifacename = " + this.mInterfaceName);
        boolean z = false;
        setHighPerfModeEnabled(false);
        this.mWifiStateTracker.updateState(0);
        this.mIpClientCallbacks = new IpClientCallbacksImpl();
        this.mFacade.makeIpClient(this.mContext, this.mInterfaceName, this.mIpClientCallbacks);
        if (!this.mIpClientCallbacks.awaitCreation()) {
            loge("Timeout waiting for IpClient");
        }
        setMulticastFilter(true);
        registerForWifiMonitorEvents();
        this.mWifiInjector.getWifiLastResortWatchdog().clearAllFailureCounts();
        setSupplicantLogLevel();
        this.mSupplicantStateTracker.sendMessage(CMD_RESET_SUPPLICANT_STATE);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mRssiCount = 5;
        for (int i = 0; i < 5; i++) {
            this.mRssiArray[i] = 0;
        }
        this.mWifiInfo.setMacAddress(this.mWifiNative.getMacAddress(this.mInterfaceName));
        initializeWpsDetails();
        sendSupplicantConnectionChangedBroadcast(true);
        this.mWifiNative.setExternalSim(this.mInterfaceName, true);
        setRandomMacOui();
        this.mCountryCode.setReadyForChange(true);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mIsRunning = true;
        updateBatteryWorkSource(null);
        this.mWifiNative.setBluetoothCoexistenceScanMode(this.mInterfaceName, this.mBluetoothConnectionActive);
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        this.mWifiNative.stopFilteringMulticastV4Packets(this.mInterfaceName);
        this.mWifiNative.stopFilteringMulticastV6Packets(this.mInterfaceName);
        WifiNative wifiNative = this.mWifiNative;
        String str = this.mInterfaceName;
        if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
            z = true;
        }
        wifiNative.setSuspendOptimizations(str, z);
        setPowerSave(true);
        WifiNative wifiNative2 = this.mWifiNative;
        if (wifiNative2 != null) {
            wifiNative2.enableStaAutoReconnect(this.mInterfaceName, true);
        }
        this.mWifiNative.setConcurrencyPriority(true);
    }

    /* access modifiers changed from: private */
    public void stopClientMode() {
        this.mWifiDiagnostics.stopLogging();
        this.mIsRunning = false;
        updateBatteryWorkSource(null);
        if (this.mIpClient != null && this.mIpClient.shutdown()) {
            this.mIpClientCallbacks.awaitShutdown();
        }
        this.mNetworkInfo.setIsAvailable(false);
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        this.mCountryCode.setReadyForChange(false);
        this.mInterfaceName = null;
        if (this.mWifiInjector.getWifiDisconStat() != null) {
            this.mWifiInjector.getWifiDisconStat().setInterfaceName(null);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().setInterfaceName(null);
        }
        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiNetworkAvailable;
        if (oppoWifiNetworkSwitchEnhance != null) {
            oppoWifiNetworkSwitchEnhance.setInterfaceName(null);
        }
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker != null) {
            oppoWifiAssistantStateTraker.setInterfaceName(null);
        }
        sendSupplicantConnectionChangedBroadcast(false);
        this.mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
    }

    /* access modifiers changed from: package-private */
    public void registerConnected() {
        int i = this.mLastNetworkId;
        if (i != -1) {
            this.mWifiConfigManager.updateNetworkAfterConnect(i);
            WifiConfiguration currentNetwork = getCurrentWifiConfiguration();
            if (currentNetwork != null && currentNetwork.isPasspoint()) {
                this.mPasspointManager.onPasspointNetworkConnected(currentNetwork.FQDN);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerDisconnected() {
        int i = this.mLastNetworkId;
        if (i != -1) {
            this.mWifiConfigManager.updateNetworkAfterDisconnect(i);
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        int i = this.mLastNetworkId;
        if (i == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(i);
    }

    private WifiConfiguration getTargetWifiConfiguration() {
        int i = this.mTargetNetworkId;
        if (i == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(i);
    }

    /* access modifiers changed from: package-private */
    public ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String bssid = this.mWifiInfo.getBSSID();
        if (bssid == null) {
            bssid = this.mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.getScanResult(bssid);
    }

    /* access modifiers changed from: package-private */
    public String getCurrentBSSID() {
        return this.mLastBssid;
    }

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            Log.d(ClientModeImpl.TAG, "entering ConnectModeState: ifaceName = " + ClientModeImpl.this.mInterfaceName);
            int unused = ClientModeImpl.this.mOperationalMode = 1;
            ClientModeImpl.this.setupClientMode();
            if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                ClientModeImpl.this.loge("Failed to remove networks on entering connect mode");
            }
            ClientModeImpl.this.mWifiInfo.reset();
            ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            ClientModeImpl.this.mWifiInjector.getWakeupController().reset();
            ClientModeImpl.this.mNetworkInfo.setIsAvailable(true);
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(ClientModeImpl.this.mNetworkInfo);
            }
            boolean unused2 = ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            ClientModeImpl.this.mWifiConnectivityManager.setWifiEnabled(true);
            ClientModeImpl.this.mNetworkFactory.setWifiState(true);
            ClientModeImpl.this.mWifiMetrics.setWifiState(2);
            ClientModeImpl.this.mWifiMetrics.logStaEvent(18);
            ClientModeImpl.this.mSarManager.setClientWifiState(3);
            ClientModeImpl.this.mWifiScoreCard.noteSupplicantStateChanged(ClientModeImpl.this.mWifiInfo);
            ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            ClientModeImpl.this.logd("Enter ConnectModeState - setSwitchState to SwitchState.INIT");
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            boolean unused3 = clientModeImpl.mSmartGearFeature = clientModeImpl.mOppoClientModeImplUtil.isSmartGearEnable();
            ClientModeImpl.this.mOppoClientModeImplUtil.setSmartGearFeature();
            if (Settings.Global.getInt(ClientModeImpl.this.mContext.getContentResolver(), "wifi_5g_band_support", 0) == 0 && 2 == (ClientModeImpl.this.mWifiNative.getSupportedFeatureSet(ClientModeImpl.this.mInterfaceName) & 2)) {
                Settings.Global.putInt(ClientModeImpl.this.mContext.getContentResolver(), "wifi_5g_band_support", 1);
            }
        }

        public void exit() {
            int unused = ClientModeImpl.this.mOperationalMode = 4;
            ClientModeImpl.this.mNetworkInfo.setIsAvailable(false);
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(ClientModeImpl.this.mNetworkInfo);
            }
            ClientModeImpl.this.mWifiConnectivityManager.setWifiEnabled(false);
            ClientModeImpl.this.mNetworkFactory.setWifiState(false);
            ClientModeImpl.this.mWifiMetrics.setWifiState(1);
            ClientModeImpl.this.mWifiMetrics.logStaEvent(19);
            ClientModeImpl.this.mWifiScoreCard.noteWifiDisabled(ClientModeImpl.this.mWifiInfo);
            ClientModeImpl.this.mSarManager.setClientWifiState(1);
            if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                ClientModeImpl.this.loge("Failed to remove networks on exiting connect mode");
            }
            ClientModeImpl.this.mWifiInfo.reset();
            ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            ClientModeImpl.this.mWifiScoreCard.noteSupplicantStateChanged(ClientModeImpl.this.mWifiInfo);
            ClientModeImpl.this.stopClientMode();
        }

        /* JADX INFO: Multiple debug info for r2v59 int: [D('ok' boolean), D('netId' int)] */
        /* JADX INFO: Multiple debug info for r2v66 int: [D('ok' boolean), D('slot' int)] */
        /* JADX INFO: Multiple debug info for r2v76 int: [D('ok' boolean), D('restoreNetid' int)] */
        /* JADX INFO: Multiple debug info for r2v120 int: [D('ok' boolean), D('netId' int)] */
        /* JADX WARNING: Code restructure failed: missing block: B:520:0x119e, code lost:
            if (com.android.server.wifi.ClientModeImpl.access$10500(r5, com.android.server.wifi.ClientModeImpl.access$4500(r5), r2) != false) goto L_0x11a0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:529:0x120d, code lost:
            if (com.android.server.wifi.ClientModeImpl.access$10500(r7, com.android.server.wifi.ClientModeImpl.access$4500(r7), r6) != false) goto L_0x120f;
         */
        public boolean processMessage(Message message) {
            boolean ok;
            boolean ok2;
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            int level2FailureReason;
            int i;
            int sendingUid;
            PackageManager pm;
            String sendingPktName;
            WifiConfiguration currentWifiConfig;
            NetworkUpdateResult result = null;
            boolean handleStatus = true;
            if (MtkWifiServiceAdapter.preProcessMessage(this, message)) {
                return true;
            }
            switch (message.what) {
                case ClientModeImpl.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*{ENCODED_INT: 131103}*/:
                    ok = false;
                    boolean unused = ClientModeImpl.this.mBluetoothConnectionActive = message.arg1 != 0;
                    ClientModeImpl.this.mWifiNative.setBluetoothCoexistenceScanMode(ClientModeImpl.this.mInterfaceName, ClientModeImpl.this.mBluetoothConnectionActive);
                    break;
                case ClientModeImpl.CMD_REMOVE_NETWORK /*{ENCODED_INT: 131125}*/:
                    ok = false;
                    if (ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, false)) {
                        int netId = message.arg1;
                        if (netId == ClientModeImpl.this.mTargetNetworkId || netId == ClientModeImpl.this.mLastNetworkId) {
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            break;
                        }
                    } else {
                        int unused2 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                        break;
                    }
                case ClientModeImpl.CMD_ENABLE_NETWORK /*{ENCODED_INT: 131126}*/:
                    int i2 = -1;
                    boolean disableOthers = message.arg2 == 1;
                    int netId2 = message.arg1;
                    String pkgName = null;
                    if (ClientModeImpl.this.mContext != null) {
                        pkgName = ClientModeImpl.this.mContext.getPackageManager().getNameForUid(message.sendingUid);
                    }
                    if (ClientModeImpl.this.mOppoClientModeImplUtil.inForbiddenEnNetworkApplist(pkgName) && ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mDisconnectedState) {
                        disableOthers = false;
                    }
                    if (disableOthers) {
                        ok2 = ClientModeImpl.this.connectToUserSelectNetwork(netId2, message.sendingUid, false);
                    } else {
                        ok2 = ClientModeImpl.this.mWifiConfigManager.enableNetwork(netId2, false, message.sendingUid);
                    }
                    if (!ok2) {
                        int unused3 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                    } else if (ok2 && disableOthers) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.logd("select network :" + message.arg1 + " from third app");
                        }
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleThirdAPKConnect(message.arg1, message.sendingUid);
                            OppoManuConnectManager.getInstance().setManuConnectBssid(netId2, null);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendThirdAPKConnectEvt(message.arg1);
                        }
                        if (netId2 == ClientModeImpl.this.mWifiInfo.getNetworkId() && (ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mObtainingIpState)) {
                            if (OppoManuConnectManager.getInstance() != null && ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState) {
                                OppoManuConnectManager.getInstance().handleConnectStateChanged(true, ClientModeImpl.this.mLastNetworkId);
                            }
                            ClientModeImpl.this.logd("the network has already established, no need mOppoWifiConnectionAlert.sendEnableNetworkEvent");
                        } else {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.sendEnableNetworkEvent(message.arg1);
                        }
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(ClientModeImpl.CMD_ENABLE_NETWORK, message.arg1);
                        }
                        if (ClientModeImpl.this.mWifiConfigManager != null) {
                            ClientModeImpl.this.mWifiConfigManager.statisticsStartConPkgName(message.sendingUid);
                        }
                    }
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    int i3 = message.what;
                    if (ok2) {
                        i2 = 1;
                    }
                    clientModeImpl.replyToMessage(message, i3, i2);
                    ok = ok2;
                    break;
                case ClientModeImpl.CMD_GET_LINK_LAYER_STATS /*{ENCODED_INT: 131135}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.getWifiLinkLayerStats());
                    break;
                case ClientModeImpl.CMD_RECONNECT /*{ENCODED_INT: 131146}*/:
                    ok = false;
                    ClientModeImpl.this.mWifiConnectivityManager.forceConnectivityScan((WorkSource) message.obj);
                    break;
                case ClientModeImpl.CMD_REASSOCIATE /*{ENCODED_INT: 131147}*/:
                    ok = false;
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    long unused4 = clientModeImpl2.mLastConnectAttemptTimestamp = clientModeImpl2.mClock.getWallClockMillis();
                    ClientModeImpl.this.mWifiNative.reassociate(ClientModeImpl.this.mInterfaceName);
                    break;
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /*{ENCODED_INT: 131149}*/:
                    ok = false;
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizationsNative(2, true);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizationsNative(2, false);
                        break;
                    }
                case ClientModeImpl.CMD_SET_SUSPEND_OPT_ENABLED /*{ENCODED_INT: 131158}*/:
                    ok = false;
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, false);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, true);
                        if (message.arg2 == 1) {
                            ClientModeImpl.this.mSuspendWakeLock.release();
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_ENABLE_TDLS /*{ENCODED_INT: 131164}*/:
                    ok = false;
                    if (message.obj != null) {
                        ClientModeImpl.this.mWifiNative.startTdls(ClientModeImpl.this.mInterfaceName, (String) message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_APP_CONFIGURATIONS /*{ENCODED_INT: 131169}*/:
                    ok = false;
                    Set<Integer> removedNetworkIds = ClientModeImpl.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_DISABLE_EPHEMERAL_NETWORK /*{ENCODED_INT: 131170}*/:
                    ok = false;
                    WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null && (config.networkId == ClientModeImpl.this.mTargetNetworkId || config.networkId == ClientModeImpl.this.mLastNetworkId)) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /*{ENCODED_INT: 131173}*/:
                    ok = false;
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    }
                    boolean simPresent = message.arg1 == 1;
                    if (!simPresent) {
                        ClientModeImpl.this.mPasspointManager.removeEphemeralProviders();
                    }
                    ClientModeImpl.this.mWifiConfigManager.resetSimNetworks(simPresent, message.arg2);
                    break;
                case ClientModeImpl.CMD_QUERY_OSU_ICON /*{ENCODED_INT: 131176}*/:
                    ok = false;
                    ClientModeImpl.this.mPasspointManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString(ClientModeImpl.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case ClientModeImpl.CMD_MATCH_PROVIDER_NETWORK /*{ENCODED_INT: 131177}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, 0);
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*{ENCODED_INT: 131178}*/:
                    ok = false;
                    Bundle bundle = (Bundle) message.obj;
                    PasspointConfiguration passpointConfig = (PasspointConfiguration) bundle.getParcelable(ClientModeImpl.EXTRA_PASSPOINT_CONFIGURATION);
                    if (!ClientModeImpl.this.mPasspointManager.addOrUpdateProvider(passpointConfig, bundle.getInt(ClientModeImpl.EXTRA_UID), bundle.getString(ClientModeImpl.EXTRA_PACKAGE_NAME))) {
                        ClientModeImpl.this.replyToMessage(message, message.what, -1);
                        break;
                    } else {
                        String fqdn = passpointConfig.getHomeSp().getFqdn();
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        if (!clientModeImpl3.isProviderOwnedNetwork(clientModeImpl3.mTargetNetworkId, fqdn)) {
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            break;
                        }
                        ClientModeImpl.this.logd("Disconnect from current network since its provider is updated");
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        ClientModeImpl.this.replyToMessage(message, message.what, 1);
                        break;
                    }
                case ClientModeImpl.CMD_REMOVE_PASSPOINT_CONFIG /*{ENCODED_INT: 131179}*/:
                    ok = false;
                    String fqdn2 = (String) message.obj;
                    if (!ClientModeImpl.this.mPasspointManager.removeProvider(fqdn2)) {
                        ClientModeImpl.this.replyToMessage(message, message.what, -1);
                        break;
                    } else {
                        ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                        if (!clientModeImpl5.isProviderOwnedNetwork(clientModeImpl5.mTargetNetworkId, fqdn2)) {
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            break;
                        }
                        ClientModeImpl.this.logd("Disconnect from current network since its provider is removed");
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        ClientModeImpl.this.mWifiConfigManager.removePasspointConfiguredNetwork(fqdn2);
                        ClientModeImpl.this.replyToMessage(message, message.what, 1);
                        break;
                    }
                case ClientModeImpl.CMD_GET_MATCHING_OSU_PROVIDERS /*{ENCODED_INT: 131181}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getMatchingOsuProviders((List) message.obj));
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /*{ENCODED_INT: 131182}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getMatchingPasspointConfigsForOsuProviders((List) message.obj));
                    break;
                case ClientModeImpl.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /*{ENCODED_INT: 131184}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getWifiConfigsForPasspointProfiles((List) message.obj));
                    break;
                case ClientModeImpl.CMD_TARGET_BSSID /*{ENCODED_INT: 131213}*/:
                    ok = false;
                    if (message.obj != null) {
                        String unused5 = ClientModeImpl.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                    ok = false;
                    int netId3 = message.arg1;
                    int uid = message.arg2;
                    String bssid = (String) message.obj;
                    WifiConfiguration config2 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId3);
                    ClientModeImpl.this.logd("CMD_START_CONNECT sup state " + ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + ClientModeImpl.this.getCurrentState().getName() + " nid=" + Integer.toString(netId3) + " roam=" + Boolean.toString(ClientModeImpl.this.mIsAutoRoaming));
                    if (config2 != null) {
                        ClientModeImpl.this.convertToQuotedSSID(config2);
                        ClientModeImpl.this.mWifiScoreCard.noteConnectionAttempt(ClientModeImpl.this.mWifiInfo);
                        int unused6 = ClientModeImpl.this.mTargetNetworkId = netId3;
                        String tmpBssid = ClientModeImpl.this.mOppoClientModeImplUtil.getBestBssidForNetId(netId3, ClientModeImpl.this.mScanRequestProxy, ClientModeImpl.this.mWifiConfigManager);
                        Boolean bssidTmpReset = false;
                        if ("any".equals(bssid) && tmpBssid != null) {
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.logd("config bssid = " + config2.BSSID);
                            }
                            if (config2.BSSID == null || "any".equals(config2.BSSID)) {
                                if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                    ClientModeImpl.this.logd("reset config bssid to " + tmpBssid + "  temp");
                                }
                                config2.BSSID = tmpBssid;
                                bssidTmpReset = true;
                            }
                        }
                        if (bssidTmpReset.booleanValue()) {
                            boolean unused7 = ClientModeImpl.this.setTargetBssid(config2, tmpBssid);
                        } else {
                            boolean unused8 = ClientModeImpl.this.setTargetBssid(config2, bssid);
                        }
                        ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                        clientModeImpl7.reportConnectionAttemptStart(config2, clientModeImpl7.mTargetRoamBSSID, 5);
                        if (config2.macRandomizationSetting != 1 || !ClientModeImpl.this.mConnectedMacRandomzationSupported) {
                            ClientModeImpl.this.setCurrentMacToFactoryMac(config2);
                        } else {
                            ClientModeImpl.this.configureRandomizedMacAddress(config2);
                        }
                        String currentMacAddress = ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl.this.mWifiInfo.setMacAddress(currentMacAddress);
                        Log.i(ClientModeImpl.TAG, "Connecting with " + currentMacAddress + " as the mac address");
                        if (config2.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config2.enterpriseConfig.getEapMethod()) && ClientModeImpl.this.mWifiInjector.getCarrierNetworkConfig().isCarrierEncryptionInfoAvailable() && TextUtils.isEmpty(config2.enterpriseConfig.getAnonymousIdentity())) {
                            config2.enterpriseConfig.setAnonymousIdentity(TelephonyUtil.getAnonymousIdentityWith3GppRealm(ClientModeImpl.this.getTelephonyManager()));
                        }
                        if (!ClientModeImpl.this.mScreenOn && ClientModeImpl.this.mWifiConnectivityManager != null && ClientModeImpl.this.mWifiConnectivityManager.isPnoStarted()) {
                            ClientModeImpl.this.mWifiConnectivityManager.stopPnoScan();
                        }
                        ClientModeImpl.this.mOppoClientModeImplUtil.resetSaeNetworkConsecutiveAssocRejectCounter();
                        if (!ClientModeImpl.this.mWifiNative.connectToNetwork(ClientModeImpl.this.mInterfaceName, config2)) {
                            ClientModeImpl.this.loge("CMD_START_CONNECT Failed to start connection to network " + config2);
                            ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            break;
                        } else {
                            if (!ClientModeImpl.this.isThirdApp(uid)) {
                                ClientModeImpl.this.clearTargetBssid("AfterConnect");
                            }
                            if (ClientModeImpl.this.mOppoWifiConnectionAlert != null) {
                                ClientModeImpl.this.mOppoWifiConnectionAlert.setIsSelectingNetwork(true);
                            }
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(11, config2);
                            ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                            long unused9 = clientModeImpl8.mLastConnectAttemptTimestamp = clientModeImpl8.mClock.getWallClockMillis();
                            WifiConfiguration unused10 = ClientModeImpl.this.mTargetWifiConfiguration = config2;
                            boolean unused11 = ClientModeImpl.this.mIsAutoRoaming = false;
                            if (ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mDisconnectedState) {
                                ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                                clientModeImpl9.transitionTo(clientModeImpl9.mDisconnectingState);
                                break;
                            }
                        }
                    } else {
                        ClientModeImpl.this.loge("CMD_START_CONNECT and no config, bail out...");
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                    ok = false;
                    int unused12 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /*{ENCODED_INT: 131219}*/:
                    ok = false;
                    String someBssid = (String) message.obj;
                    if (!(someBssid == null || (scanDetailCache = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(ClientModeImpl.this.mTargetNetworkId)) == null)) {
                        ClientModeImpl.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_REMOVE_USER_CONFIGURATIONS /*{ENCODED_INT: 131224}*/:
                    ok = false;
                    Set<Integer> removedNetworkIds2 = ClientModeImpl.this.mWifiConfigManager.removeNetworksForUser(Integer.valueOf(message.arg1).intValue());
                    if (removedNetworkIds2.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds2.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD /*{ENCODED_INT: 131233}*/:
                    ok = false;
                    int slot = message.arg1;
                    int ret = ClientModeImpl.this.stopWifiIPPacketOffload(slot);
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /*{ENCODED_INT: 131238}*/:
                    ok = false;
                    ClientModeImpl.this.mWifiConnectivityManager.enable(message.arg1 == 1);
                    break;
                case ClientModeImpl.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /*{ENCODED_INT: 131240}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getAllMatchingFqdnsForScanResults((List) message.obj));
                    break;
                case ClientModeImpl.CMD_CONFIG_ND_OFFLOAD /*{ENCODED_INT: 131276}*/:
                    ok = false;
                    ClientModeImpl.this.mWifiNative.configureNeighborDiscoveryOffload(ClientModeImpl.this.mInterfaceName, message.arg1 > 0);
                    break;
                case ClientModeImpl.CMD_START_SUBSCRIPTION_PROVISIONING /*{ENCODED_INT: 131326}*/:
                    ok = false;
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.startSubscriptionProvisioning(message.arg1, message.getData().getParcelable(ClientModeImpl.EXTRA_OSU_PROVIDER), (IProvisioningCallback) message.obj) ? 1 : 0);
                    break;
                case ClientModeImpl.CMD_TRIGGER_RESTORE_DELAY /*{ENCODED_INT: 131477}*/:
                    ok = false;
                    int restoreNetid = message.arg1;
                    if (ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mConnectedState && ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mObtainingIpState) {
                        ClientModeImpl.this.reportFoolProofException();
                        ClientModeImpl.this.setStatistics("state_inconsistent", "wifi_state_inconsistent_cant_connect");
                        if (!ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName)) {
                            ClientModeImpl.this.loge("fool-proof,Disconnect cmd reject by wpa,so restart");
                            ClientModeImpl.this.sheduleRestartWifi(restoreNetid);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("fool-proof, already connected,ignore");
                        break;
                    }
                    break;
                case ClientModeImpl.M_CMD_SET_POWER_SAVING_MODE /*{ENCODED_INT: 131487}*/:
                    ok = false;
                    if (ClientModeImpl.this.mInterfaceName != null) {
                        ClientModeImpl.this.mWifiNative.setPowerSave(ClientModeImpl.this.mInterfaceName, message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*{ENCODED_INT: 143372}*/:
                    ok = false;
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.mWifiNative.reconnect(ClientModeImpl.this.mInterfaceName);
                        boolean unused13 = ClientModeImpl.this.mTemporarilyDisconnectWifi = false;
                        break;
                    } else {
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 5);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        boolean unused14 = ClientModeImpl.this.mTemporarilyDisconnectWifi = true;
                        break;
                    }
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                    ok = false;
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Network connection established");
                    }
                    if (ClientModeImpl.this.hasConfigKeyChanged(message.arg1)) {
                        if (OppoDataStallHelper.getInstance() != null) {
                            OppoDataStallHelper.getInstance().setHasInetAccess(false);
                            OppoDataStallHelper.getInstance().setEverHadInetAccess(false);
                            OppoDataStallHelper.getInstance().setEverHadGwAccess(false);
                        }
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                    }
                    int unused15 = ClientModeImpl.this.mLastNetworkId = message.arg1;
                    ClientModeImpl.this.mWifiConfigManager.clearRecentFailureReason(ClientModeImpl.this.mLastNetworkId);
                    String unused16 = ClientModeImpl.this.mLastBssid = (String) message.obj;
                    int reasonCode = message.arg2;
                    if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                        ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, ClientModeImpl.this.mLastNetworkId, message.arg2, ClientModeImpl.this.mLastBssid);
                    }
                    WifiConfiguration currentConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mLastNetworkId);
                    if (ClientModeImpl.this.mLastNetworkId == ClientModeImpl.this.getManuConnectNetId() && ClientModeImpl.this.isManuConnect() && currentConfig != null) {
                        ClientModeImpl.this.log("Network connection established currentConfig.ssid" + currentConfig.SSID);
                        currentConfig.BSSID = ClientModeImpl.this.mLastBssid;
                        ClientModeImpl.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(ClientModeImpl.this.mLastNetworkId, currentConfig, -1, SupplicantState.COMPLETED);
                    }
                    WifiConfiguration config3 = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (config3 == null) {
                        ClientModeImpl.this.logw("Connected to unknown networkId " + ClientModeImpl.this.mLastNetworkId + ", disconnecting...");
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    } else {
                        ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                        ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                        ClientModeImpl.this.mWifiInfo.setMacAddress(ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName));
                        ScanDetailCache scanDetailCache2 = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(config3.networkId);
                        if (!(scanDetailCache2 == null || ClientModeImpl.this.mLastBssid == null || (scanResult = scanDetailCache2.getScanResult(ClientModeImpl.this.mLastBssid)) == null)) {
                            ClientModeImpl.this.mWifiInfo.setFrequency(scanResult.frequency);
                        }
                        ClientModeImpl.this.mWifiConnectivityManager.trackBssid(ClientModeImpl.this.mLastBssid, true, reasonCode);
                        if (config3.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config3.enterpriseConfig.getEapMethod()) && !TelephonyUtil.isAnonymousAtRealmIdentity(config3.enterpriseConfig.getAnonymousIdentity())) {
                            String anonymousIdentity = ClientModeImpl.this.mWifiNative.getEapAnonymousIdentity(ClientModeImpl.this.mInterfaceName);
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.log("EAP Pseudonym: " + anonymousIdentity);
                            }
                            config3.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config3, 1010);
                        }
                        if (config3.hiddenSSID) {
                            ClientModeImpl.this.mOppoClientModeImplUtil.updateWifiConfigToSaeOrOweIfRequired(config3, ClientModeImpl.this.mWifiConfigManager, ClientModeImpl.this.mOppoScanResultsProxy, ClientModeImpl.this.mLastBssid);
                        }
                        long unused17 = ClientModeImpl.this.mConnectionTimeStamp = System.currentTimeMillis();
                        ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                        clientModeImpl10.sendNetworkStateChangeBroadcast(clientModeImpl10.mLastBssid);
                        ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                        clientModeImpl11.transitionTo(clientModeImpl11.mObtainingIpState);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    ok = false;
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("ConnectModeState: Network connection lost ");
                    }
                    ClientModeImpl.this.handleNetworkDisconnect();
                    ClientModeImpl clientModeImpl12 = ClientModeImpl.this;
                    clientModeImpl12.transitionTo(clientModeImpl12.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    ok = false;
                    if (((StateChangeResult) message.obj) != null) {
                        SupplicantState state = ClientModeImpl.this.handleSupplicantStateChange(message);
                        if (state == SupplicantState.DISCONNECTED && ClientModeImpl.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                            }
                            ClientModeImpl.this.handleNetworkDisconnect();
                            ClientModeImpl clientModeImpl13 = ClientModeImpl.this;
                            clientModeImpl13.transitionTo(clientModeImpl13.mDisconnectedState);
                        }
                        if (state == SupplicantState.COMPLETED) {
                            if (ClientModeImpl.this.mIpClient != null) {
                                ClientModeImpl.this.mIpClient.confirmConfiguration();
                            }
                            ClientModeImpl.this.mWifiScoreReport.noteIpCheck();
                            break;
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*{ENCODED_INT: 147463}*/:
                    ok = false;
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(2);
                    ClientModeImpl.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int disableReason = 3;
                    int reasonCode2 = message.arg1;
                    String bssid2 = (String) message.obj;
                    if (bssid2 == null || TextUtils.isEmpty(bssid2)) {
                        bssid2 = ClientModeImpl.this.mTargetRoamBSSID;
                    }
                    if (!ClientModeImpl.this.attemptWpa2FallbackConnectionIfRequired(bssid2)) {
                        ClientModeImpl clientModeImpl14 = ClientModeImpl.this;
                        if (clientModeImpl14.isPermanentWrongPasswordFailure(clientModeImpl14.mTargetNetworkId, reasonCode2)) {
                            disableReason = 13;
                            ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mTargetNetworkId);
                            ClientModeImpl.this.mOppoWifiConnectionAlert.sendWrongKeyEvent();
                        } else {
                            if (reasonCode2 == 3) {
                                ClientModeImpl clientModeImpl15 = ClientModeImpl.this;
                                clientModeImpl15.handleEapAuthFailure(clientModeImpl15.mTargetNetworkId, message.arg2);
                            }
                            ClientModeImpl.this.mOppoWifiConnectionAlert.sendAuthFailedEvent(message);
                            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                                ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                            }
                        }
                        ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetNetworkId, disableReason);
                        ClientModeImpl.this.mWifiConfigManager.clearRecentFailureReason(ClientModeImpl.this.mTargetNetworkId);
                        if (reasonCode2 == 0) {
                            level2FailureReason = 1;
                        } else if (reasonCode2 == 1) {
                            level2FailureReason = 2;
                        } else if (reasonCode2 == 2) {
                            level2FailureReason = 3;
                        } else if (reasonCode2 != 3) {
                            level2FailureReason = 0;
                        } else {
                            level2FailureReason = 4;
                        }
                        ClientModeImpl.this.reportConnectionAttemptEnd(3, 1, level2FailureReason);
                        if (reasonCode2 != 2) {
                            ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 2);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().switchConfigurationSimSlot(ClientModeImpl.this.mTargetNetworkId);
                            break;
                        }
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /*{ENCODED_INT: 147471}*/:
                    ok = false;
                    int netId4 = message.arg2;
                    boolean identitySent = false;
                    if (ClientModeImpl.this.mTargetWifiConfiguration != null && ClientModeImpl.this.mTargetWifiConfiguration.networkId == netId4 && TelephonyUtil.isSimConfig(ClientModeImpl.this.mTargetWifiConfiguration)) {
                        Pair<String, String> identityPair = TelephonyUtil.getSimIdentity(ClientModeImpl.this.getTelephonyManager(), ClientModeImpl.this.getSubscriptionManager(), new TelephonyUtil(), ClientModeImpl.this.mTargetWifiConfiguration, ClientModeImpl.this.mWifiInjector.getCarrierNetworkConfig());
                        Log.i(ClientModeImpl.TAG, "SUP_REQUEST_IDENTITY: identityPair=" + identityPair);
                        if (identityPair == null || identityPair.first == null) {
                            Log.e(ClientModeImpl.TAG, "Unable to retrieve identity from Telephony");
                        } else {
                            ClientModeImpl.this.mWifiNative.simIdentityResponse(ClientModeImpl.this.mInterfaceName, netId4, (String) identityPair.first, (String) identityPair.second);
                            identitySent = true;
                        }
                    }
                    if (identitySent) {
                        break;
                    } else {
                        String ssid = (String) message.obj;
                        String quotedSsid = ClientModeImpl.this.convertToQuotedSSID(ssid);
                        if (!(ClientModeImpl.this.mTargetWifiConfiguration == null || ssid == null || ClientModeImpl.this.mTargetWifiConfiguration.SSID == null || !ClientModeImpl.this.mTargetWifiConfiguration.SSID.equals(quotedSsid))) {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.sendSupRequestIdentityEvent(ClientModeImpl.this.mTargetWifiConfiguration.networkId);
                            ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetWifiConfiguration.networkId, 9);
                        }
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*{ENCODED_INT: 147472}*/:
                    ok = false;
                    ClientModeImpl.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    TelephonyUtil.SimAuthRequestData requestData = (TelephonyUtil.SimAuthRequestData) message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                ClientModeImpl.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        } else {
                            ClientModeImpl.this.handleGsmAuthRequest(requestData);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("Invalid SIM auth request");
                        break;
                    }
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                    ok = false;
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(1);
                    boolean unused18 = ClientModeImpl.this.mDidBlackListBSSID = false;
                    String bssid3 = (String) message.obj;
                    boolean timedOut = message.arg1 > 0;
                    int reasonCode3 = message.arg2;
                    Log.d(ClientModeImpl.TAG, "Association Rejection event: bssid=" + bssid3 + " reason code=" + reasonCode3 + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid3 == null || TextUtils.isEmpty(bssid3)) {
                        bssid3 = ClientModeImpl.this.mTargetRoamBSSID;
                    }
                    if (!ClientModeImpl.this.attemptWpa2FallbackConnectionIfRequired(bssid3)) {
                        if (bssid3 != null) {
                            ClientModeImpl clientModeImpl16 = ClientModeImpl.this;
                            boolean unused19 = clientModeImpl16.mDidBlackListBSSID = clientModeImpl16.mWifiConnectivityManager.trackBssid(bssid3, false, reasonCode3);
                        }
                        ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetNetworkId, 2);
                        ClientModeImpl.this.mWifiConfigManager.setRecentFailureAssociationStatus(ClientModeImpl.this.mTargetNetworkId, reasonCode3);
                        ClientModeImpl.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                        ClientModeImpl.this.mOppoWifiConnectionAlert.sendAssociationRejectionEvent(message);
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT, message.arg1, message.arg2, message.obj);
                        }
                        if (ClientModeImpl.this.mScreenOn) {
                            ClientModeImpl.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                        }
                        ClientModeImpl.this.mWifiConfigManager.setReasonCode(message.arg2);
                        ClientModeImpl clientModeImpl17 = ClientModeImpl.this;
                        if (timedOut) {
                            i = 11;
                        } else {
                            i = 2;
                        }
                        clientModeImpl17.reportConnectionAttemptEnd(i, 1, 0);
                        ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), bssid3, 1);
                        if (timedOut) {
                            boolean unused20 = ClientModeImpl.this.attemptWpa2FallbackConnectionIfRequired(bssid3);
                            break;
                        }
                    }
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /*{ENCODED_INT: 147500}*/:
                    ok = false;
                    ClientModeImpl.this.mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*{ENCODED_INT: 147509}*/:
                    ok = false;
                    ClientModeImpl.this.mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /*{ENCODED_INT: 147517}*/:
                    ok = false;
                    ClientModeImpl.this.mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                case WifiMonitor.SELECT_NETWORK_EVENT /*{ENCODED_INT: 147649}*/:
                    ok = false;
                    ClientModeImpl.this.mOppoWifiConnectionAlert.sendSelectNetworkEvent(message);
                    break;
                case WifiMonitor.SAVE_CONFIG_FAILED_EVENT /*{ENCODED_INT: 147650}*/:
                    ok = false;
                    ClientModeImpl.this.mOppoWifiConnectionAlert.sendSaveConfigFailed(message.arg1);
                    break;
                case WifiMonitor.SSID_TEMP_DISABLED /*{ENCODED_INT: 147651}*/:
                    ok = false;
                    int reason = 3;
                    String msgStr = (String) message.obj;
                    if (msgStr != null) {
                        if (msgStr.contains("WRONG_KEY")) {
                            reason = 13;
                        } else if (msgStr.contains("DHCP FAILURE")) {
                            reason = 4;
                        } else if (msgStr.contains("CONN_FAILED")) {
                            reason = 2;
                        } else {
                            reason = 3;
                            if (msgStr.contains("AUTH_FAILED")) {
                                ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(message.arg1, 3);
                            }
                        }
                    }
                    ClientModeImpl.this.handleSSIDStateChangedCB(message.arg1, reason);
                    break;
                case 151553:
                    int netId5 = message.arg1;
                    WifiConfiguration config4 = (WifiConfiguration) message.obj;
                    if (netId5 == -1 && config4 != null && config4.hiddenSSID) {
                        ClientModeImpl.this.mOppoWifiConnectionAlert.setAddAndConnectHiddenAp(true);
                    } else {
                        ClientModeImpl.this.mOppoWifiConnectionAlert.setAddAndConnectHiddenAp(false);
                    }
                    ClientModeImpl.this.checkAndSetSsidForConfig(config4);
                    ClientModeImpl.this.convertToQuotedSSID(config4);
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().setManuConnectBssid(netId5, config4);
                    }
                    ClientModeImpl.this.mWifiConfigManager.setIsNewNetwork(false);
                    boolean hasCredentialChanged = false;
                    if (config4 != null) {
                        WifiConfiguration currConfig = ClientModeImpl.this.getCurrentWifiConfiguration();
                        WifiConfiguration saveConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(config4.configKey(true));
                        if (saveConfig != null && config4.hiddenSSID && currConfig != null && ClientModeImpl.this.mLastNetworkId == saveConfig.networkId && "ConnectedState".equalsIgnoreCase(ClientModeImpl.this.getCurrentState().getName())) {
                            config4 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithPassword(saveConfig.networkId);
                        }
                        if (netId5 != -1 || config4 == null || config4.networkId == -1) {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.setConnectAlreadyExistConfigByAdd(false);
                        } else {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.setConnectAlreadyExistConfigByAdd(true);
                        }
                        if (ClientModeImpl.this.mContext != null) {
                            sendingUid = message.sendingUid;
                            pm = ClientModeImpl.this.mContext.getPackageManager();
                        } else {
                            sendingUid = -1;
                            pm = null;
                        }
                        if (pm != null) {
                            sendingPktName = pm.getNameForUid(sendingUid);
                        } else {
                            sendingPktName = null;
                        }
                        if (!"com.coloros.backuprestore".equals(sendingPktName)) {
                            ok = false;
                            if (ClientModeImpl.this.mOppoWifiConnectionAlert.needSaveAsHiddenAP((WifiConfiguration) message.obj)) {
                                config4.hiddenSSID = true;
                            }
                        } else {
                            ok = false;
                        }
                        result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config4, message.sendingUid);
                        if (!result.isSuccess()) {
                            ClientModeImpl.this.loge("CONNECT_NETWORK adding/updating config=" + config4 + " failed");
                            int unused21 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            break;
                        } else {
                            netId5 = result.getNetworkId();
                            hasCredentialChanged = result.hasCredentialChanged();
                        }
                    } else {
                        ok = false;
                    }
                    if ("CompletedState".equalsIgnoreCase(ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName()) && "DisconnectedState".equalsIgnoreCase(ClientModeImpl.this.getCurrentState().getName())) {
                        boolean mMsgPending = ClientModeImpl.this.mSupplicantStateTracker.getHandler().hasMessages(WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT);
                        if (System.currentTimeMillis() - ClientModeImpl.this.mLastSelectEvtTimeStamp < ((long) 5000)) {
                            Log.d(ClientModeImpl.TAG, "fool-proof,Supplicant state goes wrong now,but connect too frequence!");
                        } else if (mMsgPending) {
                            Log.d(ClientModeImpl.TAG, "fool-proof,Supplicant state goes wrong now,but still has unhandled supplicant msg!!");
                        } else {
                            ClientModeImpl.this.loge("fool-proof,Supplicant state goes wrong!");
                            ClientModeImpl clientModeImpl18 = ClientModeImpl.this;
                            clientModeImpl18.sendMessageDelayed(clientModeImpl18.obtainMessage(ClientModeImpl.CMD_TRIGGER_RESTORE_DELAY, netId5, 0), 2000);
                        }
                    }
                    long unused22 = ClientModeImpl.this.mLastSelectEvtTimeStamp = System.currentTimeMillis();
                    ClientModeImpl.this.logd("disableDualStaWithDelay when mannual connect.");
                    OppoWifiAssistantUtils.getInstance(ClientModeImpl.this.mContext).disableDualStaWithDelay(5000);
                    if (ClientModeImpl.this.connectToUserSelectNetwork(netId5, message.sendingUid, hasCredentialChanged)) {
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(netId5, message.sendingUid);
                        }
                        if (!(ClientModeImpl.this.mWifiConfigManager == null || message.sendingUid == 1000 || message.sendingUid == 1010)) {
                            if (!("android.uid.systemui:" + message.sendingUid).equals(ClientModeImpl.this.mContext.getPackageManager().getNameForUid(message.sendingUid))) {
                                ClientModeImpl.this.logd("Third Wifi Apk connect by CONNECT_NETWORK");
                                ClientModeImpl.this.mWifiConfigManager.statisticsStartConPkgName(message.sendingUid);
                            }
                        }
                        ClientModeImpl.this.mOppoWifiConnectionAlert.sendConnectNetworkEvent(netId5);
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            boolean isSaveConfig = message.arg1 != -1;
                            int newConfig = isSaveConfig ? 0 : 1;
                            ClientModeImpl.this.mWifiNetworkStateTraker.setManualConnTime(System.currentTimeMillis(), isSaveConfig, config4);
                            ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(151553, netId5, newConfig);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(netId5);
                        }
                        if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.getCurrentWifiConfiguration() != null && ClientModeImpl.this.getCurrentWifiConfiguration().networkId == netId5 && OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(true, netId5);
                        }
                        if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.getCurrentWifiConfiguration() != null && ClientModeImpl.this.getCurrentWifiConfiguration().networkId == netId5 && ClientModeImpl.this.mOppoWifiConnectionAlert != null) {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.setManuConnect(false);
                        }
                        if (SystemProperties.get("ro.secure", "0").equals("1") && SystemProperties.get("debug.wifi.prdebug", "0").equals("0")) {
                            SystemProperties.set("debug.wifi.prdebug", "1");
                            if (!ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.enableVerboseLogging(1);
                                boolean unused23 = ClientModeImpl.this.mFromKeylogVerbose = true;
                            }
                        }
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(13, config4);
                        ClientModeImpl.this.broadcastWifiCredentialChanged(0, config4);
                        ClientModeImpl.this.replyToMessage(message, 151555);
                        break;
                    } else {
                        int unused24 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                        ClientModeImpl.this.replyToMessage(message, 151554, 9);
                        break;
                    }
                case 151556:
                    if (ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, true)) {
                        if (ClientModeImpl.this.mOppoWifiConnectionAlert != null && ClientModeImpl.this.mOppoWifiConnectionAlert.isManuConnect() && ClientModeImpl.this.mOppoWifiConnectionAlert.getManuConnectNetId() == message.arg1 && message.arg1 != -1) {
                            ClientModeImpl.this.mOppoWifiConnectionAlert.setManuConnect(false);
                        }
                        int netId6 = message.arg1;
                        if (netId6 != ClientModeImpl.this.mTargetNetworkId && netId6 != ClientModeImpl.this.mLastNetworkId) {
                            ok = false;
                            break;
                        } else {
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            ok = false;
                            break;
                        }
                    } else {
                        ok = false;
                        break;
                    }
                    break;
                case 151559:
                    WifiConfiguration config5 = (WifiConfiguration) message.obj;
                    if (config5 != null && config5.SSID == null && config5.networkId != -1 && ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(config5.networkId) == null) {
                        ClientModeImpl.this.logd("break because ssid is null,but cant find saved ap by netid");
                        ok = false;
                        break;
                    } else {
                        result = ClientModeImpl.this.saveNetworkConfigAndSendReply(message);
                        int netId7 = result.getNetworkId();
                        if (!(!ClientModeImpl.this.mWifiConfigManager.isAutoConnectSwitchEnable() || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState || (currentWifiConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(netId7)) == null || ClientModeImpl.this.mWifiNative == null)) {
                            Log.d(ClientModeImpl.TAG, "config.userApproved:" + currentWifiConfig.userApproved);
                            if (currentWifiConfig.userApproved == 1001) {
                                ClientModeImpl.this.mWifiNative.removeNetworkIfCurrent(ClientModeImpl.this.mInterfaceName, netId7);
                            }
                        }
                        if (result.isSuccess() && ClientModeImpl.this.mWifiInfo.getNetworkId() == netId7) {
                            if (result.hasCredentialChanged()) {
                                WifiConfiguration config6 = (WifiConfiguration) message.obj;
                                ClientModeImpl.this.logi("SAVE_NETWORK credential changed for config=" + config6.configKey() + ", Reconnecting.");
                                ClientModeImpl.this.startConnectToNetwork(netId7, message.sendingUid, "any");
                            } else {
                                if (result.hasProxyChanged() && ClientModeImpl.this.mIpClient != null) {
                                    ClientModeImpl.this.log("Reconfiguring proxy on connection");
                                    if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                                        ClientModeImpl.this.mIpClient.setHttpProxy(ClientModeImpl.this.getCurrentWifiConfiguration().getHttpProxy());
                                    }
                                }
                                if (!(!result.hasIpChanged() || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mDisconnectedState || ClientModeImpl.this.getCurrentWifiConfiguration() == null)) {
                                    ClientModeImpl.this.log("Reconfiguring IP on connection");
                                    ClientModeImpl clientModeImpl19 = ClientModeImpl.this;
                                    clientModeImpl19.transitionTo(clientModeImpl19.mObtainingIpState);
                                }
                            }
                        }
                        ok = false;
                        break;
                    }
                    break;
                case 151562:
                    WpsInfo wpsInfo = (WpsInfo) message.obj;
                    if (wpsInfo != null) {
                        WpsResult wpsResult = new WpsResult();
                        if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                            ClientModeImpl.this.loge("Failed to remove networks before WPS");
                        }
                        int i4 = wpsInfo.setup;
                        if (i4 == 0) {
                            boolean unused25 = ClientModeImpl.this.clearRandomMacOui();
                            boolean unused26 = ClientModeImpl.this.mIsRandomMacCleared = true;
                            if (ClientModeImpl.this.mWifiNative.startWpsPbc(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID)) {
                                wpsResult.status = WpsResult.Status.SUCCESS;
                            } else {
                                Log.e(ClientModeImpl.TAG, "Failed to start WPS push button configuration");
                                wpsResult.status = WpsResult.Status.FAILURE;
                            }
                        } else if (i4 == 1) {
                            wpsResult.pin = ClientModeImpl.this.mWifiNative.startWpsPinDisplay(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID);
                            if (!TextUtils.isEmpty(wpsResult.pin)) {
                                wpsResult.status = WpsResult.Status.SUCCESS;
                            } else {
                                Log.e(ClientModeImpl.TAG, "Failed to start WPS pin method configuration");
                                wpsResult.status = WpsResult.Status.FAILURE;
                            }
                        } else if (i4 != 2) {
                            wpsResult = new WpsResult(WpsResult.Status.FAILURE);
                            ClientModeImpl.this.loge("Invalid setup for WPS");
                        } else if (ClientModeImpl.this.mWifiNative.startWpsRegistrar(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID, wpsInfo.pin)) {
                            wpsResult.status = WpsResult.Status.SUCCESS;
                        } else {
                            Log.e(ClientModeImpl.TAG, "Failed to start WPS push button configuration");
                            wpsResult.status = WpsResult.Status.FAILURE;
                        }
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleWpsConnect(message.sendingUid);
                        }
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(151553, -1, 0);
                        }
                        if (wpsResult.status != WpsResult.Status.SUCCESS) {
                            ClientModeImpl.this.loge("Failed to start WPS with config " + wpsInfo.toString());
                            ClientModeImpl.this.replyToMessage(message, 151564, 0);
                            if (OppoManuConnectManager.getInstance() == null) {
                                ok = false;
                                break;
                            } else {
                                OppoManuConnectManager.getInstance().reset();
                                ok = false;
                                break;
                            }
                        } else {
                            ClientModeImpl.this.replyToMessage(message, 151563, wpsResult);
                            ClientModeImpl clientModeImpl20 = ClientModeImpl.this;
                            clientModeImpl20.transitionTo(clientModeImpl20.mWpsRunningState);
                            ok = false;
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("Cannot start WPS with null WpsInfo object");
                        ClientModeImpl.this.replyToMessage(message, 151564, 0);
                        ok = false;
                        break;
                    }
                case 151569:
                    int netId8 = message.arg1;
                    String pkgName2 = null;
                    if (ClientModeImpl.this.mContext != null) {
                        pkgName2 = ClientModeImpl.this.mContext.getPackageManager().getNameForUid(message.sendingUid);
                    }
                    if (!ClientModeImpl.this.mOppoClientModeImplUtil.inForbiddenDisNetworkApplist(pkgName2) || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mDisconnectedState) {
                        if (!ClientModeImpl.this.mWifiConfigManager.disableNetwork(netId8, message.sendingUid)) {
                            ClientModeImpl.this.loge("Failed to disable network");
                            int unused27 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                            ClientModeImpl.this.replyToMessage(message, 151570, 0);
                            ok = false;
                            break;
                        } else {
                            ClientModeImpl.this.replyToMessage(message, 151571);
                            if (netId8 != ClientModeImpl.this.mTargetNetworkId && netId8 != ClientModeImpl.this.mLastNetworkId) {
                                ok = false;
                                break;
                            } else {
                                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                                ok = false;
                                break;
                            }
                        }
                    } else {
                        ClientModeImpl.this.loge("This apk can is forbidden,Failed to disable network");
                        int unused28 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                        ClientModeImpl.this.replyToMessage(message, 151570, 0);
                        ok = false;
                        break;
                    }
                    break;
                default:
                    ok = false;
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            MtkWifiServiceAdapter.postProcessMessage(this, message, Boolean.valueOf(ok), result);
            return handleStatus;
        }
    }

    private WifiNetworkAgentSpecifier createNetworkAgentSpecifier(WifiConfiguration currentWifiConfiguration, String currentBssid, int specificRequestUid, String specificRequestPackageName) {
        currentWifiConfiguration.BSSID = currentBssid;
        return new WifiNetworkAgentSpecifier(currentWifiConfiguration, specificRequestUid, specificRequestPackageName);
    }

    /* access modifiers changed from: private */
    public NetworkCapabilities getCapabilities(WifiConfiguration currentWifiConfiguration) {
        NetworkCapabilities result = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        result.setNetworkSpecifier(null);
        if (currentWifiConfiguration == null) {
            return result;
        }
        if (!this.mWifiInfo.isTrusted()) {
            result.removeCapability(14);
        } else {
            result.addCapability(14);
        }
        if (!WifiConfiguration.isMetered(currentWifiConfiguration, this.mWifiInfo)) {
            result.addCapability(11);
        } else {
            result.removeCapability(11);
        }
        if (this.mWifiInfo.getRssi() != -127) {
            result.setSignalStrength(this.mWifiInfo.getRssi());
        } else {
            result.setSignalStrength(Integer.MIN_VALUE);
        }
        if (currentWifiConfiguration.osu) {
            result.removeCapability(12);
        }
        if (!this.mWifiInfo.getSSID().equals("<unknown ssid>")) {
            result.setSSID(this.mWifiInfo.getSSID());
        } else {
            result.setSSID(null);
        }
        Pair<Integer, String> specificRequestUidAndPackageName = this.mNetworkFactory.getSpecificNetworkRequestUidAndPackageName(currentWifiConfiguration);
        if (((Integer) specificRequestUidAndPackageName.first).intValue() != -1) {
            result.removeCapability(12);
        }
        result.setNetworkSpecifier(createNetworkAgentSpecifier(currentWifiConfiguration, getCurrentBSSID(), ((Integer) specificRequestUidAndPackageName.first).intValue(), (String) specificRequestUidAndPackageName.second));
        return result;
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration currentWifiConfiguration) {
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkCapabilities(getCapabilities(currentWifiConfiguration));
        }
    }

    /* access modifiers changed from: private */
    public boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        WifiConfiguration config;
        if (networkId == -1 || (config = this.mWifiConfigManager.getConfiguredNetwork(networkId)) == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    /* access modifiers changed from: private */
    public void handleEapAuthFailure(int networkId, int errorCode) {
        WifiConfiguration targetedNetwork = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (targetedNetwork != null) {
            int eapMethod = targetedNetwork.enterpriseConfig.getEapMethod();
            if ((eapMethod == 4 || eapMethod == 5 || eapMethod == 6) && errorCode == 16385) {
                getTelephonyManager().createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).resetCarrierKeysForImsiEncryption();
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiNetworkAgent extends NetworkAgent {
        private int mLastNetworkStatus = -1;
        private String mLastRedirectUrl = null;

        WifiNetworkAgent(Looper l, Context c, String tag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, tag, ni, nc, lp, score, misc);
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                }
                if (ClientModeImpl.this.mWifiNetworkAvailable == null && ClientModeImpl.this.mWifiNetworkStateTraker == null) {
                    ClientModeImpl.this.unwantedNetwork(0);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                if (status != this.mLastNetworkStatus || (status == 2 && !TextUtils.isEmpty(this.mLastRedirectUrl) && TextUtils.isEmpty(redirectUrl))) {
                    this.mLastRedirectUrl = redirectUrl;
                    this.mLastNetworkStatus = status;
                    if (status == 2) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                        }
                        boolean unused = ClientModeImpl.this.mNetworkDetectValid = false;
                        ClientModeImpl.this.unwantedNetwork(1, redirectUrl);
                    } else if (status == 1) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                        }
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(14);
                        boolean unused2 = ClientModeImpl.this.mNetworkDetectValid = true;
                        ClientModeImpl.this.doNetworkStatus(status);
                    } else if (status == 3) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            log("WifiNetworkAgent -> Wifi networkStatus auto captive, score= " + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                        }
                        boolean unused3 = ClientModeImpl.this.mNetworkDetectValid = false;
                        ClientModeImpl.this.autoCaptiveNetworkStatus(status);
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void saveAcceptUnvalidated(boolean accept) {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        /* access modifiers changed from: protected */
        public void startSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void stopSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void addKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void removeKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.sendMessage(ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD, clientModeImpl.mWifiInfo.getRssi());
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
                    Log.e(ClientModeImpl.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    clientModeImpl2.sendMessage(ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD, clientModeImpl2.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            byte[] unused = ClientModeImpl.this.mRssiRanges = rssiRange;
            ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
            clientModeImpl3.sendMessage(ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD, clientModeImpl3.mWifiInfo.getRssi());
        }

        /* access modifiers changed from: protected */
        public void preventAutomaticReconnect() {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                ClientModeImpl.this.unwantedNetwork(2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
        this.mIpClient.setNetworkValidState(2);
    }

    /* access modifiers changed from: package-private */
    public void unwantedNetwork(int reason, String redirectUrl) {
        sendMessage(CMD_UNWANTED_NETWORK, reason, 0, redirectUrl);
        if (TextUtils.isEmpty(redirectUrl)) {
            this.mIpClient.setNetworkValidState(2);
        }
    }

    /* access modifiers changed from: package-private */
    public void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
        this.mIpClient.setNetworkValidState(1);
    }

    /* access modifiers changed from: package-private */
    public void autoCaptiveNetworkStatus(int status) {
        sendMessage(CMD_AUTO_CON_CAPTIVE, status);
    }

    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        String prefix;
        String mnc;
        String mcc;
        if (imsi == null || imsi.isEmpty()) {
            return "";
        }
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

    class L2ConnectedState extends State {
        RssiEventHandler mRssiEventHandler = new RssiEventHandler();

        class RssiEventHandler implements WifiNative.WifiRssiEventHandler {
            RssiEventHandler() {
            }

            @Override // com.android.server.wifi.WifiNative.WifiRssiEventHandler
            public void onRssiThresholdBreached(byte curRssi) {
                if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                    Log.e(ClientModeImpl.TAG, "onRssiThresholdBreach event. Cur Rssi = " + ((int) curRssi));
                }
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        L2ConnectedState() {
        }

        public void enter() {
            ClientModeImpl.this.mOppoClientModeImplUtil.resetSaeNetworkConsecutiveAssocRejectCounter();
            ClientModeImpl.access$11008(ClientModeImpl.this);
            if (ClientModeImpl.this.mEnableRssiPolling) {
                ClientModeImpl.this.mLinkProbeManager.resetOnNewConnection();
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.sendMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl.mRssiPollToken, 0);
            }
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.loge("Have NetworkAgent when entering L2Connected");
                boolean unused = ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            }
            boolean unused2 = ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
            NetworkCapabilities nc = clientModeImpl2.getCapabilities(clientModeImpl2.getCurrentWifiConfiguration());
            synchronized (ClientModeImpl.this.mNetworkAgentLock) {
                WifiNetworkAgent unused3 = ClientModeImpl.this.mNetworkAgent = new WifiNetworkAgent(ClientModeImpl.this.getHandler().getLooper(), ClientModeImpl.this.mContext, "WifiNetworkAgent", ClientModeImpl.this.mNetworkInfo, nc, ClientModeImpl.this.mLinkProperties, 5, ClientModeImpl.this.mNetworkMisc);
            }
            ClientModeImpl.this.clearTargetBssid("L2ConnectedState");
            ClientModeImpl.this.mCountryCode.setReadyForChange(false);
            ClientModeImpl.this.mWifiMetrics.setWifiState(3);
            ClientModeImpl.this.mWifiScoreCard.noteNetworkAgentCreated(ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mNetworkAgent.netId);
            if (ClientModeImpl.this.mOppoDhcpRecord.isDoingSwitch()) {
                ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            }
            ClientModeImpl.this.enableTcpTimestamps(true, ClientModeImpl.this.getRomUpdateIntegerValue("NETWORK_TCP_TS_ERROR_THRESHOLD", 10).intValue());
            ClientModeImpl.this.changeTcpRandomTS(true);
            ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
            ClientModeImpl.this.is1x1IotRouter();
        }

        public void exit() {
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.stop();
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(ClientModeImpl.this.mLastNetworkId));
                if (ClientModeImpl.this.mLastBssid != null) {
                    sb.append(" ");
                    sb.append(ClientModeImpl.this.mLastBssid);
                }
            }
            if (!(ClientModeImpl.this.mLastBssid == null && ClientModeImpl.this.mLastNetworkId == -1)) {
                ClientModeImpl.this.handleNetworkDisconnect();
            }
            ClientModeImpl.this.mCountryCode.setReadyForChange(true);
            ClientModeImpl.this.mWifiMetrics.setWifiState(2);
            ClientModeImpl.this.mWifiStateTracker.updateState(2);
            ClientModeImpl.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(false);
            boolean unused = ClientModeImpl.this.mIsAutoRoaming = false;
            ClientModeImpl.this.enableTcpTimestamps(true, 0);
            ClientModeImpl.this.changeTcpRandomTS(false);
            int unused2 = ClientModeImpl.this.mIs1x1IotRouter = -1;
        }

        /* JADX INFO: Multiple debug info for r1v121 int: [D('currRssi' byte), D('slot' int)] */
        /* JADX INFO: Multiple debug info for r1v224 int: [D('netId' int), D('info' android.net.wifi.RssiPacketCountInfo)] */
        public boolean processMessage(Message message) {
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            WifiConfiguration wConf;
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_DISCONNECT /*{ENCODED_INT: 131145}*/:
                    ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                    ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.transitionTo(clientModeImpl.mDisconnectingState);
                    break;
                case ClientModeImpl.CMD_RECONNECT /*{ENCODED_INT: 131146}*/:
                    ClientModeImpl.this.log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case ClientModeImpl.CMD_ENABLE_RSSI_POLL /*{ENCODED_INT: 131154}*/:
                    ClientModeImpl.this.cleanWifiScore();
                    boolean unused = ClientModeImpl.this.mEnableRssiPolling = message.arg1 == 1;
                    ClientModeImpl.access$11008(ClientModeImpl.this);
                    if (ClientModeImpl.this.mEnableRssiPolling) {
                        int unused2 = ClientModeImpl.this.mLastSignalLevel = -1;
                        ClientModeImpl.this.mLinkProbeManager.resetOnScreenTurnedOn();
                        ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.sendMessageDelayed(clientModeImpl2.obtainMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl2.mRssiPollToken, 0), (long) ClientModeImpl.this.mPollRssiIntervalMsecs);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_RSSI_POLL /*{ENCODED_INT: 131155}*/:
                    if (message.arg1 == ClientModeImpl.this.mRssiPollToken) {
                        WifiLinkLayerStats stats = updateLinkLayerStatsRssiAndScoreReportInternal();
                        ClientModeImpl.this.mWifiMetrics.updateWifiUsabilityStatsEntries(ClientModeImpl.this.mWifiInfo, stats);
                        if (ClientModeImpl.this.mWifiScoreReport.shouldCheckIpLayer()) {
                            if (ClientModeImpl.this.mIpClient != null) {
                                ClientModeImpl.this.mIpClient.confirmConfiguration();
                            }
                            ClientModeImpl.this.mWifiScoreReport.noteIpCheck();
                        }
                        if (ClientModeImpl.this.mWifiNetworkAvailable != null) {
                            ClientModeImpl.this.mWifiNetworkAvailable.reportRssi();
                        }
                        int statusDataStall = ClientModeImpl.this.mWifiDataStall.checkForDataStall(ClientModeImpl.this.mLastLinkLayerStats, stats);
                        if (statusDataStall != 0) {
                            ClientModeImpl.this.mWifiMetrics.addToWifiUsabilityStatsList(2, ClientModeImpl.convertToUsabilityStatsTriggerType(statusDataStall), -1);
                        }
                        ClientModeImpl.this.mWifiMetrics.incrementWifiLinkLayerUsageStats(stats);
                        WifiLinkLayerStats unused3 = ClientModeImpl.this.mLastLinkLayerStats = stats;
                        ClientModeImpl.this.mWifiScoreCard.noteSignalPoll(ClientModeImpl.this.mWifiInfo);
                        ClientModeImpl.this.mLinkProbeManager.updateConnectionStats(ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.sendMessageDelayed(clientModeImpl3.obtainMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl3.mRssiPollToken, 0), (long) ClientModeImpl.this.mPollRssiIntervalMsecs);
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            clientModeImpl4.sendRssiChangeBroadcast(clientModeImpl4.mWifiInfo.getRssi());
                        }
                        ClientModeImpl.this.mWifiTrafficPoller.notifyOnDataActivity(ClientModeImpl.this.mWifiInfo.txSuccess, ClientModeImpl.this.mWifiInfo.rxSuccess);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_ONESHOT_RSSI_POLL /*{ENCODED_INT: 131156}*/:
                    if (!ClientModeImpl.this.mEnableRssiPolling) {
                        updateLinkLayerStatsRssiAndScoreReportInternal();
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /*{ENCODED_INT: 131173}*/:
                    if (message.arg1 == 0 && ClientModeImpl.this.mLastNetworkId != -1) {
                        WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mLastNetworkId);
                        if (TelephonyUtil.isSimConfig(config) && message.arg2 == TelephonyUtil.getSimSlot(config)) {
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 6);
                            ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                            ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                            clientModeImpl5.transitionTo(clientModeImpl5.mDisconnectingState);
                        }
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL /*{ENCODED_INT: 131210}*/:
                    boolean unused4 = ClientModeImpl.this.mIsAutoRoaming = false;
                    if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                        ClientModeImpl.this.handleSuccessfulIpConfiguration();
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null && !ClientModeImpl.this.mWifiNetworkStateTraker.getIsOppoManuConnect() && ClientModeImpl.this.wifiAssistantForSoftAP() && ClientModeImpl.this.mAutoSwitch && ClientModeImpl.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_DETECT_CONNECT", true).booleanValue() && ClientModeImpl.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED && !ClientModeImpl.this.mNetworkDetectValid) {
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            clientModeImpl6.transitionTo(clientModeImpl6.mCaptiveState);
                            break;
                        } else {
                            if (ClientModeImpl.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
                                ClientModeImpl.this.mNetworkAgent.explicitlySelected(true);
                                ClientModeImpl.this.mNetworkAgent.sendNetworkScore(79);
                            }
                            ClientModeImpl.this.sendConnectedState();
                            ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                            clientModeImpl7.transitionTo(clientModeImpl7.mConnectedState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                        clientModeImpl8.transitionTo(clientModeImpl8.mDisconnectingState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /*{ENCODED_INT: 131211}*/:
                    if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.mWifiInjector.getWifiDisconStat() != null) {
                        ClientModeImpl.this.mWifiInjector.getWifiDisconStat().handleDhcpRenewFailed();
                    }
                    ClientModeImpl.this.getWifiLinkLayerStats();
                    ClientModeImpl.this.handleIpConfigurationLost();
                    ClientModeImpl.this.reportConnectionAttemptEnd(10, 1, 0);
                    ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 3);
                    ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                    clientModeImpl9.transitionTo(clientModeImpl9.mDisconnectingState);
                    break;
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                    int startConnectId = message.arg1;
                    WifiConfiguration connectConfig = ClientModeImpl.this.getConnectConfig(startConnectId, (String) message.obj);
                    if (ClientModeImpl.this.mWifiInfo == null || ClientModeImpl.this.mWifiInfo.getNetworkId() != startConnectId || connectConfig == null || !ClientModeImpl.this.mWifiNative.isSameNetwork(ClientModeImpl.this.mInterfaceName, connectConfig)) {
                        if (ClientModeImpl.this.mWifiNative != null) {
                            ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        }
                        return false;
                    }
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /*{ENCODED_INT: 131219}*/:
                    if (((String) message.obj) != null) {
                        String unused5 = ClientModeImpl.this.mLastBssid = (String) message.obj;
                        ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                        if (ClientModeImpl.this.mLastBssid != null && (ClientModeImpl.this.mWifiInfo.getBSSID() == null || !ClientModeImpl.this.mLastBssid.equals(ClientModeImpl.this.mWifiInfo.getBSSID()))) {
                            if (ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState) {
                                boolean unused6 = ClientModeImpl.this.mIsAutoRoaming = true;
                            }
                            ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                            WifiConfiguration config2 = ClientModeImpl.this.getCurrentWifiConfiguration();
                            if (!(config2 == null || (scanDetailCache = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(config2.networkId)) == null || (scanResult = scanDetailCache.getScanResult(ClientModeImpl.this.mLastBssid)) == null)) {
                                ClientModeImpl.this.mWifiInfo.setFrequency(scanResult.frequency);
                            }
                            ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                            clientModeImpl10.sendNetworkStateChangeBroadcast(clientModeImpl10.mLastBssid);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.logw("Associated command w/o BSSID");
                        break;
                    }
                case ClientModeImpl.CMD_NETWORK_STATUS /*{ENCODED_INT: 131220}*/:
                    if (message.arg1 == 1 && (ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mObtainingIpState || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mRoamingState)) {
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                        clientModeImpl11.transitionTo(clientModeImpl11.mConnectedState);
                        break;
                    }
                case ClientModeImpl.CMD_IP_REACHABILITY_LOST /*{ENCODED_INT: 131221}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled && message.obj != null) {
                        ClientModeImpl.this.log((String) message.obj);
                    }
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(9);
                    ClientModeImpl.this.mWifiMetrics.logWifiIsUnusableEvent(5);
                    ClientModeImpl.this.mWifiMetrics.addToWifiUsabilityStatsList(2, 5, -1);
                    if (!ClientModeImpl.this.mIpReachabilityDisconnectEnabled) {
                        ClientModeImpl.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        break;
                    } else {
                        ClientModeImpl.this.handleIpReachabilityLost();
                        ClientModeImpl clientModeImpl12 = ClientModeImpl.this;
                        clientModeImpl12.transitionTo(clientModeImpl12.mDisconnectingState);
                        break;
                    }
                case ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD /*{ENCODED_INT: 131232}*/:
                    int slot = message.arg1;
                    int result = ClientModeImpl.this.startWifiIPPacketOffload(slot, (KeepalivePacketData) message.obj, message.arg2);
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(slot, result);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131234}*/:
                case ClientModeImpl.CMD_RSSI_THRESHOLD_BREACHED /*{ENCODED_INT: 131236}*/:
                    ClientModeImpl.this.processRssiThreshold((byte) message.arg1, message.what, this.mRssiEventHandler);
                    break;
                case ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD /*{ENCODED_INT: 131235}*/:
                    int unused7 = ClientModeImpl.this.stopRssiMonitoringOffload();
                    break;
                case ClientModeImpl.CMD_IPV4_PROVISIONING_SUCCESS /*{ENCODED_INT: 131272}*/:
                    if (ClientModeImpl.this.mOppoDhcpRecord.isDoingSwitch()) {
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                    }
                    ClientModeImpl.this.handleIPv4Success((DhcpResults) message.obj);
                    ClientModeImpl clientModeImpl13 = ClientModeImpl.this;
                    clientModeImpl13.sendNetworkStateChangeBroadcast(clientModeImpl13.mLastBssid);
                    break;
                case ClientModeImpl.CMD_IPV4_PROVISIONING_FAILURE /*{ENCODED_INT: 131273}*/:
                    if (ClientModeImpl.this.mOppoDhcpRecord.isDoingSwitch()) {
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                    }
                    if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.mWifiInjector.getWifiDisconStat() != null) {
                        ClientModeImpl.this.mWifiInjector.getWifiDisconStat().handleDhcpRenewFailed();
                    }
                    ClientModeImpl.this.handleIPv4Failure();
                    ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 3);
                    break;
                case ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /*{ENCODED_INT: 131281}*/:
                    if (ClientModeImpl.this.mIpClient != null) {
                        int slot2 = message.arg1;
                        if (!(message.obj instanceof NattKeepalivePacketData)) {
                            if (message.obj instanceof TcpKeepalivePacketData) {
                                ClientModeImpl.this.mIpClient.addKeepalivePacketFilter(slot2, (TcpKeepalivePacketData) message.obj);
                                break;
                            }
                        } else {
                            ClientModeImpl.this.mIpClient.addKeepalivePacketFilter(slot2, (NattKeepalivePacketData) message.obj);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /*{ENCODED_INT: 131282}*/:
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.removeKeepalivePacketFilter(message.arg1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_PRE_DHCP_ACTION /*{ENCODED_INT: 131327}*/:
                    ClientModeImpl.this.handlePreDhcpSetup();
                    break;
                case ClientModeImpl.CMD_PRE_DHCP_ACTION_COMPLETE /*{ENCODED_INT: 131328}*/:
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.completedPreDhcpAction();
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_POST_DHCP_ACTION /*{ENCODED_INT: 131329}*/:
                    ClientModeImpl.this.handlePostDhcpSetup();
                    break;
                case 131473:
                    ClientModeImpl.this.log("[1131400] receive DhcpClient.EVENT_FIND_DUP_SERVER");
                    ClientModeImpl.this.mOppoDhcpRecord.handleFindDupDhcpServer((String) message.obj);
                    break;
                case 131475:
                    ClientModeImpl.this.log("[1131400] receive DhcpClient.EVENT_SWITCH_SERVER_FAILURE");
                    ClientModeImpl.this.mOppoDhcpRecord.handleSwitchDhcpServerFailure((String) message.obj);
                    break;
                case 131476:
                    ClientModeImpl.this.log("[1131400] receive DhcpClient.EVENT_SWITCH_SERVER_FAILURE");
                    ClientModeImpl.this.mOppoDhcpRecord.handleFixDhcpServerFailure((String) message.obj);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*{ENCODED_INT: 143372}*/:
                    if (message.arg1 == 1) {
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 5);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        boolean unused8 = ClientModeImpl.this.mTemporarilyDisconnectWifi = true;
                        ClientModeImpl clientModeImpl14 = ClientModeImpl.this;
                        clientModeImpl14.transitionTo(clientModeImpl14.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                    ClientModeImpl.this.mWifiInfo.setBSSID((String) message.obj);
                    if (ClientModeImpl.this.hasConfigKeyChanged(message.arg1)) {
                        if (OppoDataStallHelper.getInstance() != null) {
                            OppoDataStallHelper.getInstance().setHasInetAccess(false);
                            OppoDataStallHelper.getInstance().setEverHadInetAccess(false);
                            OppoDataStallHelper.getInstance().setEverHadGwAccess(false);
                        }
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                    }
                    if (message.arg1 != -1) {
                        int unused9 = ClientModeImpl.this.mLastNetworkId = message.arg1;
                    }
                    ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                    ClientModeImpl.this.mWifiInfo.setMacAddress(ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName));
                    if (ClientModeImpl.this.mLastBssid != null) {
                        if (!ClientModeImpl.this.mLastBssid.equals((String) message.obj)) {
                            String unused10 = ClientModeImpl.this.mLastBssid = (String) message.obj;
                            ClientModeImpl clientModeImpl15 = ClientModeImpl.this;
                            clientModeImpl15.sendNetworkStateChangeBroadcast(clientModeImpl15.mLastBssid);
                            if (ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState) {
                                boolean unused11 = ClientModeImpl.this.mIsAutoRoaming = true;
                            }
                        }
                    } else if (message.obj != null) {
                        String unused12 = ClientModeImpl.this.mLastBssid = (String) message.obj;
                        ClientModeImpl clientModeImpl16 = ClientModeImpl.this;
                        clientModeImpl16.sendNetworkStateChangeBroadcast(clientModeImpl16.mLastBssid);
                    }
                    if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                        ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, ClientModeImpl.this.mLastNetworkId, message.arg2, ClientModeImpl.this.mLastBssid);
                    }
                    if (!ClientModeImpl.this.isUsingDHCP() || ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mConnectedState || !ClientModeImpl.this.mIsAutoRoaming) {
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleConnectStateChanged(true, ClientModeImpl.this.mLastNetworkId);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.log("roam to other BSSID.");
                        ClientModeImpl clientModeImpl17 = ClientModeImpl.this;
                        clientModeImpl17.transitionTo(clientModeImpl17.mObtainingIpState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_FREQUENCY_CHANGED /*{ENCODED_INT: 147468}*/:
                    ClientModeImpl.this.mWifiInfo.setFrequency(message.arg1);
                    ClientModeImpl clientModeImpl18 = ClientModeImpl.this;
                    clientModeImpl18.sendNetworkStateChangeBroadcast(clientModeImpl18.mLastBssid);
                    break;
                case 151553:
                    int netId = message.arg1;
                    if (netId == -1 && (wConf = (WifiConfiguration) message.obj) != null) {
                        netId = wConf.networkId;
                    }
                    if (!(ClientModeImpl.this.mWifiInfo == null || ClientModeImpl.this.mWifiInfo.getNetworkId() != netId || OppoManuConnectManager.getInstance() == null)) {
                        OppoManuConnectManager.getInstance().handleManuConnect(netId, message.sendingUid);
                        ClientModeImpl.this.mWifiConfigManager.oppoSetLastSelectedNetwork(netId);
                        if (ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mConnectedState) {
                            if (ClientModeImpl.this.mWifiConfigManager != null) {
                                ClientModeImpl.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(netId), -1, SupplicantState.COMPLETED);
                            }
                            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                                ClientModeImpl.this.mWifiNetworkStateTraker.handleManualConnect(true);
                            }
                            ClientModeImpl.this.sendConnectedState();
                            ClientModeImpl.this.replyToMessage(message, 151555);
                            break;
                        } else if (ClientModeImpl.this.mWifiNative != null) {
                            ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        }
                    }
                    return false;
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = ClientModeImpl.this.mWifiInfo.getRssi();
                    WifiNative.TxPacketCounters counters = ClientModeImpl.this.mWifiNative.getTxPacketCounters(ClientModeImpl.this.mInterfaceName);
                    if (counters == null) {
                        ClientModeImpl.this.replyToMessage(message, 151574, 0);
                        break;
                    } else {
                        info.txgood = counters.txSucceeded;
                        info.txbad = counters.txFailed;
                        ClientModeImpl.this.replyToMessage(message, 151573, info);
                        break;
                    }
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        private WifiLinkLayerStats updateLinkLayerStatsRssiAndScoreReportInternal() {
            WifiLinkLayerStats stats = ClientModeImpl.this.getWifiLinkLayerStats();
            ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
            return stats;
        }
    }

    public void updateLinkLayerStatsRssiAndScoreReport() {
        sendMessage(CMD_ONESHOT_RSSI_POLL);
    }

    /* access modifiers changed from: private */
    public static int convertToUsabilityStatsTriggerType(int unusableEventTriggerType) {
        if (unusableEventTriggerType == 1) {
            return 1;
        }
        if (unusableEventTriggerType == 2) {
            return 2;
        }
        if (unusableEventTriggerType == 3) {
            return 3;
        }
        if (unusableEventTriggerType == 4) {
            return 4;
        }
        if (unusableEventTriggerType == 5) {
            return 5;
        }
        Log.e(TAG, "Unknown WifiIsUnusableEvent: " + unusableEventTriggerType);
        return 0;
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            StaticIpConfiguration staticIpConfig;
            WifiConfiguration currentConfig = ClientModeImpl.this.getCurrentWifiConfiguration();
            if (currentConfig == null) {
                ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.transitionTo(clientModeImpl.mDisconnectingState);
                return;
            }
            boolean isUsingStaticIp = currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC;
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                String key = currentConfig.configKey();
                ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                clientModeImpl2.log("enter ObtainingIpState netId=" + Integer.toString(ClientModeImpl.this.mLastNetworkId) + " " + key + "  roam=" + ClientModeImpl.this.mIsAutoRoaming + " static=" + isUsingStaticIp);
            }
            boolean unused = ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR);
            ClientModeImpl.this.clearTargetBssid("ObtainingIpAddress");
            DhcpResults dhcpResult = null;
            int dhcpStartType = 0;
            if (!ClientModeImpl.this.mIsAutoRoaming) {
                ClientModeImpl.this.stopIpClient();
                dhcpResult = ClientModeImpl.this.mOppoDhcpRecord.getDhcpRecord(ClientModeImpl.this.getCurrentConfigKey());
                dhcpStartType = ClientModeImpl.this.mOppoDhcpRecord.getStartType(dhcpResult);
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                clientModeImpl3.logd("[bug#1131400] ObtainingIpState enter: StartType=" + dhcpStartType + "init dhcpResult" + dhcpResult);
            }
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.setHttpProxy(currentConfig.getHttpProxy());
                if (!TextUtils.isEmpty(ClientModeImpl.this.mTcpBufferSizes)) {
                    ClientModeImpl.this.mIpClient.setTcpBufferSizes(ClientModeImpl.this.mTcpBufferSizes);
                }
            }
            if (!isUsingStaticIp) {
                staticIpConfig = new ProvisioningConfiguration.Builder().withPreDhcpAction().withApfCapabilities(ClientModeImpl.this.mWifiNative.getApfCapabilities(ClientModeImpl.this.mInterfaceName)).withNetwork(ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).withRandomMacAddress().withDhcpResult(dhcpResult).withStartType(dhcpStartType).build();
            } else {
                StaticIpConfiguration staticIpConfig2 = currentConfig.getStaticIpConfiguration();
                if (staticIpConfig2 != null) {
                    staticIpConfig = new ProvisioningConfiguration.Builder().withStaticConfiguration(staticIpConfig2).withApfCapabilities(ClientModeImpl.this.mWifiNative.getApfCapabilities(ClientModeImpl.this.mInterfaceName)).withNetwork(ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).build();
                } else {
                    staticIpConfig = null;
                }
            }
            if (!(ClientModeImpl.this.mIpClient == null || staticIpConfig == null)) {
                ClientModeImpl.this.mIpClient.startProvisioning(staticIpConfig);
            }
            ClientModeImpl.this.getWifiLinkLayerStats();
        }

        public void exit() {
            if (ClientModeImpl.this.mOppoDhcpRecord.isDoingSwitch()) {
                ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            }
            if (ClientModeImpl.this.mDADetector != null) {
                ClientModeImpl.this.mDADetector.close();
            }
            if (ClientModeImpl.this.mGWDetector != null) {
                ClientModeImpl.this.mGWDetector.close();
            }
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /*{ENCODED_INT: 131149}*/:
                    int unused = ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                    if (message.arg1 != ClientModeImpl.this.mLastNetworkId) {
                        Log.d(ClientModeImpl.TAG, "Allow different connection attempt in ObtainingIpState");
                        handleStatus = false;
                        break;
                    }
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                    int unused2 = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    break;
                case ClientModeImpl.CMD_START_SCAN /*{ENCODED_INT: 131479}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.logd("[1716726] Defer scan reques in mObtainingIpState");
                    }
                    int unused3 = ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.EVENT_DHCP_STATE_CHANGE /*{ENCODED_INT: 131488}*/:
                    ClientModeImpl.this.logd("IP Recover: receive event CMD_DHCP_RECOVER_STATE");
                    DhcpResults results = (DhcpResults) message.obj;
                    if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.mOppoDhcpRecord != null) {
                        ClientModeImpl.this.mOppoDhcpRecord.syncDhcpResults(results, ClientModeImpl.this.getCurrentConfigKey());
                    }
                    ClientModeImpl.this.resetNetworkAngent();
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.transitionTo(clientModeImpl.mObtainingIpState);
                    break;
                case ClientModeImpl.EVENT_DO_DUP_ARP /*{ENCODED_INT: 131489}*/:
                    ClientModeImpl.this.logd("IP Recover: receive event EVENT_DO_DUP_ARP");
                    String ifaceName = (String) message.obj;
                    Inet4Address myAddr = Inet4AddressUtils.intToInet4AddressHTL(message.arg1);
                    Inet4Address target = Inet4AddressUtils.intToInet4AddressHTL(message.arg2);
                    Log.d(ClientModeImpl.TAG, "IP Recover: receive event EVENT_DO_DUP_ARP myAddr = " + myAddr + " target = " + target + " ifaceName= " + ifaceName);
                    if (ClientModeImpl.this.mDADetector != null) {
                        ClientModeImpl.this.mDADetector.close();
                    }
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    OppoIPConflictDetector unused4 = clientModeImpl2.mDADetector = new OppoIPConflictDetector(clientModeImpl2.mContext, (NetworkAgentInfo) null, ClientModeImpl.this.mDADCallback);
                    if (ClientModeImpl.this.mDADetector != null) {
                        ClientModeImpl.this.mDADetector.doDupArp(ifaceName, myAddr, target);
                        break;
                    }
                    break;
                case ClientModeImpl.EVENT_DO_GATEWAY_DETECT /*{ENCODED_INT: 131490}*/:
                    ClientModeImpl.this.logd("IP Recover: receive event EVENT_DO_GATEWAY_DETECT");
                    String ifaceName1 = (String) message.obj;
                    Inet4Address myAddr1 = Inet4AddressUtils.intToInet4AddressHTL(message.arg1);
                    Inet4Address target1 = Inet4AddressUtils.intToInet4AddressHTL(message.arg2);
                    Log.d(ClientModeImpl.TAG, "IP Recover: receive event EVENT_DO_GATEWAY_DETECT myAddr = " + myAddr1 + " target = " + target1 + " ifaceName= " + ifaceName1);
                    if (ClientModeImpl.this.mGWDetector != null) {
                        ClientModeImpl.this.mGWDetector.close();
                    }
                    ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                    OppoIPConflictDetector unused5 = clientModeImpl3.mGWDetector = new OppoIPConflictDetector(clientModeImpl3.mContext, (NetworkAgentInfo) null, ClientModeImpl.this.mGWDCallback);
                    if (ClientModeImpl.this.mGWDetector != null) {
                        ClientModeImpl.this.mGWDetector.doDupArp(ifaceName1, myAddr1, target1);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                    WifiConfiguration config = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (!(config == null || config.networkId == -1 || OppoAutoConnectManager.getInstance() == null)) {
                        OppoAutoConnectManager.getInstance().handleunexpectedDiconnectDisabled(ClientModeImpl.this.mConnectionTimeStamp, config.networkId, message.arg2);
                    }
                    handleStatus = false;
                    break;
                case 151559:
                    int unused6 = ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class CaptiveState extends State {
        CaptiveState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                String key = "";
                if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                    key = ClientModeImpl.this.getCurrentWifiConfiguration().configKey();
                }
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.log("enter cpt netId=" + Integer.toString(ClientModeImpl.this.mLastNetworkId) + " " + key);
            }
            ClientModeImpl.this.setCaptivePortalMode(2);
            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
            int unused = clientModeImpl2.mConnectedId = clientModeImpl2.mLastNetworkId;
            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                ClientModeImpl.this.mWifiNetworkStateTraker.setNetworkDetailState(ClientModeImpl.this.mLastNetworkId, NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK, ClientModeImpl.this.mLastBssid);
            }
            boolean unused2 = ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK);
            ClientModeImpl.this.clearTargetBssid("CaptiveState");
            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkInfo(ClientModeImpl.this.mNetworkAgent.netId, ClientModeImpl.this.mLinkProperties);
            }
        }

        public boolean processMessage(Message message) {
            String str;
            if (message == null) {
                ClientModeImpl.this.logd("message is null,ignore!!");
                return true;
            }
            switch (message.what) {
                case ClientModeImpl.CMD_ENABLE_NETWORK /*{ENCODED_INT: 131126}*/:
                    int enableNetId = message.arg1;
                    boolean disableOthers = 1 == message.arg2;
                    int curNetId = -1;
                    if (ClientModeImpl.this.mWifiInfo != null) {
                        curNetId = ClientModeImpl.this.mWifiInfo.getNetworkId();
                    }
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.logd("enableNetId = " + enableNetId + " disableOthers = " + disableOthers + " curNetId = " + curNetId);
                    if (disableOthers && enableNetId != -1 && enableNetId == curNetId) {
                        ClientModeImpl.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        ClientModeImpl.this.replyToMessage(message, message.what, 1);
                        ClientModeImpl.this.mWifiConfigManager.oppoSetLastSelectedNetwork(enableNetId);
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(enableNetId, message.sendingUid);
                        }
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(enableNetId);
                        }
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.transitionTo(clientModeImpl2.mConnectedState);
                        break;
                    } else {
                        return false;
                    }
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /*{ENCODED_INT: 131144}*/:
                    if (message.arg1 != 1) {
                        int unused = ClientModeImpl.this.mOperationalMode = message.arg1;
                    }
                    return false;
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                    int startConId = message.arg1;
                    boolean ignoreDisconnect = false;
                    ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                    clientModeImpl3.logd("startConId=" + startConId + ", getid= " + ClientModeImpl.this.mWifiInfo.getNetworkId());
                    if (ClientModeImpl.this.mWifiInfo.getNetworkId() == startConId) {
                        ignoreDisconnect = true;
                    }
                    if (ignoreDisconnect) {
                        ClientModeImpl.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(startConId, message.sendingUid);
                            OppoManuConnectManager.getInstance().sendConnectModeChangeBroadcast(true);
                        }
                        ClientModeImpl.this.mWifiConfigManager.oppoSetLastSelectedNetwork(startConId);
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(startConId);
                        }
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                        clientModeImpl4.transitionTo(clientModeImpl4.mConnectedState);
                        break;
                    } else {
                        return false;
                    }
                case ClientModeImpl.CMD_UNWANTED_NETWORK /*{ENCODED_INT: 131216}*/:
                    if (message.arg1 == 1) {
                        if (TextUtils.isEmpty((String) message.obj)) {
                            ClientModeImpl.this.mOppoDhcpRecord.setNetworkValidated(false);
                        }
                        if (!ClientModeImpl.this.mOppoDhcpRecord.needSwitchDhcpServer()) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkConnectResult(ClientModeImpl.this.mLastNetworkId, false);
                            ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                            ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                            clientModeImpl5.transitionTo(clientModeImpl5.mDisconnectingState);
                            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                                ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkState(1, ClientModeImpl.this.mWifiInfo.getSSID());
                                break;
                            }
                        } else {
                            ClientModeImpl.this.resetNetworkAngent();
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            clientModeImpl6.transitionTo(clientModeImpl6.mObtainingIpState);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                    long unused2 = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    if (config != null) {
                        boolean unused3 = ClientModeImpl.this.setTargetBssid(config, bssid);
                        int unused4 = ClientModeImpl.this.mTargetNetworkId = netId;
                        ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                        clientModeImpl7.logd("CMD_START_ROAM sup state " + ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + ClientModeImpl.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config =" + config.configKey() + " targetRoamBSSID " + ClientModeImpl.this.mTargetRoamBSSID);
                        ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                        clientModeImpl8.reportConnectionAttemptStart(config, clientModeImpl8.mTargetRoamBSSID, 3);
                        if (ClientModeImpl.this.mWifiInfo != null) {
                            ClientModeImpl.this.mWifiInfo.getNetworkId();
                        }
                        if (!ClientModeImpl.this.mWifiNative.roamToNetwork(ClientModeImpl.this.mInterfaceName, config)) {
                            ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                            clientModeImpl9.loge("CMD_START_ROAM Failed to start roaming to network " + config);
                            ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            int unused5 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                            break;
                        } else {
                            ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                            long unused6 = clientModeImpl10.mLastConnectAttemptTimestamp = clientModeImpl10.mClock.getWallClockMillis();
                            WifiConfiguration unused7 = ClientModeImpl.this.mTargetWifiConfiguration = config;
                            boolean unused8 = ClientModeImpl.this.mIsAutoRoaming = true;
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(12, config);
                            ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                            clientModeImpl11.transitionTo(clientModeImpl11.mRoamingState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                case ClientModeImpl.CMD_NETWORK_STATUS /*{ENCODED_INT: 131220}*/:
                    if (message.arg1 == 1) {
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                        ClientModeImpl.this.mOppoDhcpRecord.setNetworkValidated(true);
                        ClientModeImpl.this.mOppoMtuProber.StartMtuProber();
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkState(0, ClientModeImpl.this.mWifiInfo.getSSID());
                        }
                        ClientModeImpl clientModeImpl12 = ClientModeImpl.this;
                        clientModeImpl12.transitionTo(clientModeImpl12.mConnectedState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_AUTO_CON_CAPTIVE /*{ENCODED_INT: 131482}*/:
                    if (message.arg1 == 3) {
                        ClientModeImpl.this.loge("CMD_AUTO_CON_CAPTIVE");
                        ClientModeImpl.this.mNetworkAgent.explicitlySelected(true);
                        ClientModeImpl.this.mNetworkAgent.sendNetworkScore(79);
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl clientModeImpl13 = ClientModeImpl.this;
                        clientModeImpl13.transitionTo(clientModeImpl13.mConnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                    if (ClientModeImpl.this.mLastDriverRoamAttempt != 0) {
                        long lastRoam = ClientModeImpl.this.mClock.getWallClockMillis() - ClientModeImpl.this.mLastDriverRoamAttempt;
                        long unused9 = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    }
                    if (ClientModeImpl.unexpectedDisconnectedReason(message.arg2)) {
                        ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config2 = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (!(config2 == null || config2.networkId == -1 || OppoAutoConnectManager.getInstance() == null)) {
                        OppoAutoConnectManager.getInstance().handleunexpectedDiconnectDisabled(ClientModeImpl.this.mConnectionTimeStamp, config2.networkId, message.arg2);
                    }
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl clientModeImpl14 = ClientModeImpl.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getBSSID());
                        sb.append(" RSSI=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config2 == null) {
                            str = "Unavailable";
                        } else {
                            str = config2.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str);
                        clientModeImpl14.log(sb.toString());
                        break;
                    }
                    break;
                case 151553:
                    int conId = message.arg1;
                    boolean ignoreDisconnect2 = false;
                    WifiConfiguration conConfig = (WifiConfiguration) message.obj;
                    ClientModeImpl clientModeImpl15 = ClientModeImpl.this;
                    clientModeImpl15.logd("conid=" + conId + ", getid= " + ClientModeImpl.this.mWifiInfo.getNetworkId());
                    if (conConfig != null) {
                        ClientModeImpl clientModeImpl16 = ClientModeImpl.this;
                        clientModeImpl16.logd("conConfig = " + conConfig.networkId);
                        if (conId == -1) {
                            conId = conConfig.networkId;
                        }
                    }
                    if (ClientModeImpl.this.mWifiInfo.getNetworkId() == conId || (conConfig != null && conConfig.networkId == ClientModeImpl.this.mWifiInfo.getNetworkId())) {
                        ignoreDisconnect2 = true;
                    }
                    WifiConfiguration savedConf = null;
                    if (!(ClientModeImpl.this.mWifiConfigManager == null || conConfig == null)) {
                        savedConf = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(conConfig.configKey());
                    }
                    if (!(savedConf == null || ClientModeImpl.this.mWifiInfo == null || savedConf.networkId != ClientModeImpl.this.mWifiInfo.getNetworkId())) {
                        ignoreDisconnect2 = true;
                    }
                    if (ignoreDisconnect2) {
                        ClientModeImpl.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (!(ClientModeImpl.this.mWifiInfo == null || ClientModeImpl.this.mWifiConfigManager == null)) {
                            int ignoreNetId = ClientModeImpl.this.mWifiInfo.getNetworkId();
                            ClientModeImpl.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(ignoreNetId, ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ignoreNetId), -1, SupplicantState.COMPLETED);
                        }
                        ClientModeImpl.this.mWifiConfigManager.oppoSetLastSelectedNetwork(conId);
                        ClientModeImpl.this.replyToMessage(message, message.what, 1);
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(conId, message.sendingUid);
                            OppoManuConnectManager.getInstance().sendConnectModeChangeBroadcast(true);
                        }
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.handleManualConnect(true);
                        }
                        if (OppoAutoConnectManager.getInstance() != null) {
                            OppoAutoConnectManager.getInstance().sendManuConnectEvt(conId);
                        }
                        ClientModeImpl.this.sendConnectedState();
                        ClientModeImpl clientModeImpl17 = ClientModeImpl.this;
                        clientModeImpl17.transitionTo(clientModeImpl17.mConnectedState);
                        break;
                    } else {
                        return false;
                    }
                default:
                    return false;
            }
            return true;
        }
    }

    @VisibleForTesting
    public boolean shouldEvaluateWhetherToSendExplicitlySelected(WifiConfiguration currentConfig) {
        if (currentConfig == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
            return false;
        }
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        if (this.mWifiConfigManager.getLastSelectedNetwork() != currentConfig.networkId || currentTimeMillis - this.mWifiConfigManager.getLastSelectedTimeStamp() >= 30000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        boolean explicitlySelected = false;
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            explicitlySelected = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(config.lastConnectUid);
            if (this.mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " explicitlySelected=" + explicitlySelected);
            }
        }
        if (this.mVerboseLoggingEnabled) {
            log("explictlySelected=" + explicitlySelected + " acceptUnvalidated=" + config.noInternetAccessExpected);
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleConnectStateChanged(true, this.mLastNetworkId);
        }
        int i = this.mLastNetworkId;
        this.mConnectedId = i;
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker != null) {
            oppoWifiAssistantStateTraker.setNetworkDetailState(i, NetworkInfo.DetailedState.CONNECTED, this.mLastBssid);
        }
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendWifiConnectStateChangedEvt(true, this.mLastNetworkId);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl.this.log("RoamingState Enter mScreenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl.this.mRoamWatchdogCount++;
            ClientModeImpl.this.logd("Start Roam Watchdog " + ClientModeImpl.this.mRoamWatchdogCount);
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.sendMessageDelayed(clientModeImpl.obtainMessage(ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER, clientModeImpl.mRoamWatchdogCount, 0), (long) ClientModeImpl.this.getRomUpdateIntegerValue("CONNECT_ROAM_GUARD_TIMER_MSEC", 15000).intValue());
            this.mAssociated = false;
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER /*{ENCODED_INT: 131166}*/:
                    if (ClientModeImpl.this.mRoamWatchdogCount == message.arg1) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("roaming watchdog! -> disconnect");
                        }
                        ClientModeImpl.this.mWifiMetrics.endConnectionEvent(9, 1, 0);
                        ClientModeImpl.access$15708(ClientModeImpl.this);
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 4);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDisconnectedState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /*{ENCODED_INT: 131211}*/:
                    if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                        ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(3);
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_UNWANTED_NETWORK /*{ENCODED_INT: 131216}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Roaming and CS doesn't want the network -> ignore");
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                    if (!this.mAssociated) {
                        int unused = ClientModeImpl.this.mMessageHandlingStatus = -5;
                        break;
                    } else {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("roaming and Network connection established");
                        }
                        if (ClientModeImpl.this.hasConfigKeyChanged(message.arg1)) {
                            if (OppoDataStallHelper.getInstance() != null) {
                                OppoDataStallHelper.getInstance().setHasInetAccess(false);
                                OppoDataStallHelper.getInstance().setEverHadInetAccess(false);
                                OppoDataStallHelper.getInstance().setEverHadGwAccess(false);
                            }
                            ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                        }
                        int unused2 = ClientModeImpl.this.mLastNetworkId = message.arg1;
                        String unused3 = ClientModeImpl.this.mLastBssid = (String) message.obj;
                        ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                        ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                        ClientModeImpl.this.mWifiConnectivityManager.trackBssid(ClientModeImpl.this.mLastBssid, true, message.arg2);
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.sendNetworkStateChangeBroadcast(clientModeImpl2.mLastBssid);
                        ClientModeImpl.this.reportConnectionAttemptEnd(1, 1, 0);
                        ClientModeImpl.this.clearTargetBssid("RoamingCompleted");
                        if (!ClientModeImpl.this.isUsingDHCP()) {
                            ClientModeImpl.this.sendConnectedState();
                            ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                            clientModeImpl3.transitionTo(clientModeImpl3.mConnectedState);
                            break;
                        } else {
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            clientModeImpl4.transitionTo(clientModeImpl4.mObtainingIpState);
                            break;
                        }
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    String bssid = (String) message.obj;
                    String target = "";
                    if (ClientModeImpl.this.mTargetRoamBSSID != null) {
                        target = ClientModeImpl.this.mTargetRoamBSSID;
                    }
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    clientModeImpl5.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + bssid + " target=" + target);
                    WifiConfiguration config = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (!(config == null || config.networkId == -1 || OppoAutoConnectManager.getInstance() == null)) {
                        OppoAutoConnectManager.getInstance().handleunexpectedDiconnectDisabled(ClientModeImpl.this.mConnectionTimeStamp, config.networkId, message.arg2);
                    }
                    if (bssid != null && bssid.equals(ClientModeImpl.this.mTargetRoamBSSID)) {
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                        clientModeImpl6.transitionTo(clientModeImpl6.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                            clientModeImpl7.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(ClientModeImpl.this.mTargetRoamBSSID)) {
                            ClientModeImpl.this.handleNetworkDisconnect();
                            ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                            clientModeImpl8.transitionTo(clientModeImpl8.mDisconnectedState);
                        }
                    }
                    if (ClientModeImpl.this.mLastSupplicantState != stateChangeResult.state) {
                        SupplicantState unused4 = ClientModeImpl.this.mLastSupplicantState = stateChangeResult.state;
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED || stateChangeResult.state == SupplicantState.FOUR_WAY_HANDSHAKE || stateChangeResult.state == SupplicantState.GROUP_HANDSHAKE || stateChangeResult.state == SupplicantState.COMPLETED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            String unused5 = ClientModeImpl.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                            break;
                        }
                    }
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.logd("ClientModeImpl: Leaving Roaming state");
        }
    }

    private void broadcastCaptiveTypeChanged() {
        log("broadcastCaptiveTypeChanged");
        Intent intent = new Intent(ACTION_TRY_START_CAPTIVE_ACTIVITY);
        intent.addFlags(67108864);
        intent.addFlags(536870912);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.log("Enter ConnectedState  mScreenOn=" + ClientModeImpl.this.mScreenOn);
            }
            if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkInfo(ClientModeImpl.this.mNetworkAgent.netId, ClientModeImpl.this.mLinkProperties);
            }
            ClientModeImpl.this.setCaptivePortalMode(1);
            ClientModeImpl.this.reportConnectionAttemptEnd(1, 1, 0);
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
            ClientModeImpl.this.registerConnected();
            long unused = ClientModeImpl.this.mLastConnectAttemptTimestamp = 0;
            WifiConfiguration unused2 = ClientModeImpl.this.mTargetWifiConfiguration = null;
            ClientModeImpl.this.mWifiScoreReport.reset();
            int unused3 = ClientModeImpl.this.mLastSignalLevel = -1;
            if (SystemProperties.get("debug.wifi.prdebug", "0").equals("1")) {
                SystemProperties.set("debug.wifi.prdebug", "0");
                if (ClientModeImpl.this.mFromKeylogVerbose) {
                    ClientModeImpl.this.enableVerboseLogging(0);
                }
            }
            ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_CHECK_INTERNET_ACCESS);
            ClientModeImpl.access$15908(ClientModeImpl.this);
            if (ClientModeImpl.this.mIsAutoRoaming) {
                ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                clientModeImpl2.loge("Dhcp successful after driver roaming, check internet access, seq=" + ClientModeImpl.this.mCheckInetAccessSeq);
                ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                clientModeImpl3.sendMessageDelayed(clientModeImpl3.obtainMessage(ClientModeImpl.CMD_CHECK_INTERNET_ACCESS, 1, clientModeImpl3.mCheckInetAccessSeq), 2000);
            } else {
                ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                clientModeImpl4.loge("Dhcp successful, check internet access, seq=" + ClientModeImpl.this.mCheckInetAccessSeq);
                ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                clientModeImpl5.sendMessageDelayed(clientModeImpl5.obtainMessage(ClientModeImpl.CMD_CHECK_INTERNET_ACCESS, -1, clientModeImpl5.mCheckInetAccessSeq), 2000);
            }
            boolean unused4 = ClientModeImpl.this.mIsAutoRoaming = false;
            long unused5 = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            int unused6 = ClientModeImpl.this.mTargetNetworkId = -1;
            ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            ClientModeImpl.this.mWifiStateTracker.updateState(3);
            ClientModeImpl.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(true);
            ClientModeImpl.this.mOppoNfHooksHelper.sendPidAndListen();
            if (ClientModeImpl.this.mWifiInjector.getWifiDisconStat() != null) {
                ClientModeImpl.this.mWifiInjector.getWifiDisconStat().connectedInit(ClientModeImpl.this.getCurrentWifiConfiguration(), ClientModeImpl.this.mWifiInfo);
            }
            if (ClientModeImpl.this.mWifiConfigManager != null) {
                ClientModeImpl.this.mWifiConfigManager.storethirdAPKWifiDiagnosis(true, 0);
            }
            if (OppoDataStallHelper.getInstance() != null) {
                OppoDataStallHelper.getInstance().clearDataStallCounter();
            }
            ClientModeImpl.this.mOppoNfHooksHelper.sendPidAndListen();
        }

        public boolean processMessage(Message message) {
            String str;
            String str2;
            boolean handleStatus = true;
            boolean accept = false;
            switch (message.what) {
                case ClientModeImpl.CMD_FIRMWARE_ALERT /*{ENCODED_INT: 131172}*/:
                    if (message.arg1 > 256) {
                        int reason = message.arg1 - 256;
                        ClientModeImpl.access$15908(ClientModeImpl.this);
                        String otaVersion = SystemProperties.get(ClientModeImpl.OTA_VERSION);
                        ClientModeImpl.this.loge("Received DATA_STALL, reason=" + reason + " seq=" + ClientModeImpl.this.mCheckInetAccessSeq + " OTAversion = " + otaVersion);
                        if (OppoDataStallHelper.getInstance() != null) {
                            OppoDataStallHelper.getInstance().checkInternetAccess(2, ClientModeImpl.this.mCheckInetAccessSeq, reason, ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mLastNetworkId, ClientModeImpl.this.mInterfaceName);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_UNWANTED_NETWORK /*{ENCODED_INT: 131216}*/:
                    if (message.arg1 != 0) {
                        if (message.arg1 == 2 || message.arg1 == 1) {
                            if (message.arg1 == 2) {
                                str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                            } else {
                                str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                            }
                            Log.d(ClientModeImpl.TAG, str);
                            WifiConfiguration config = ClientModeImpl.this.getCurrentWifiConfiguration();
                            if (config != null) {
                                if (message.arg1 == 2) {
                                    ClientModeImpl.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                    ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 10);
                                }
                                ClientModeImpl.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                                ClientModeImpl.this.mOppoDhcpRecord.setNetworkValidated(false);
                                if (!TextUtils.isEmpty((String) message.obj) || message.arg1 != 1 || !ClientModeImpl.this.mOppoDhcpRecord.needSwitchDhcpServer()) {
                                    if (ClientModeImpl.this.mWifiNetworkStateTraker != null && TextUtils.isEmpty((String) message.obj)) {
                                        ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkState(1, ClientModeImpl.this.mWifiInfo.getSSID());
                                        break;
                                    }
                                } else {
                                    ClientModeImpl.this.resetNetworkAngent();
                                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                                    clientModeImpl.transitionTo(clientModeImpl.mObtainingIpState);
                                    break;
                                }
                            }
                        }
                    } else {
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 3);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectingState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                    long unused = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config2 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config2 != null) {
                        ClientModeImpl.this.mWifiScoreCard.noteConnectionAttempt(ClientModeImpl.this.mWifiInfo);
                        boolean unused2 = ClientModeImpl.this.setTargetBssid(config2, bssid);
                        int unused3 = ClientModeImpl.this.mTargetNetworkId = netId;
                        ClientModeImpl.this.logd("CMD_START_ROAM sup state " + ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + ClientModeImpl.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config2.configKey() + " targetRoamBSSID " + ClientModeImpl.this.mTargetRoamBSSID);
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.reportConnectionAttemptStart(config2, clientModeImpl3.mTargetRoamBSSID, 3);
                        if (!ClientModeImpl.this.mWifiNative.roamToNetwork(ClientModeImpl.this.mInterfaceName, config2)) {
                            ClientModeImpl.this.loge("CMD_START_ROAM Failed to start roaming to network " + config2);
                            ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            int unused4 = ClientModeImpl.this.mMessageHandlingStatus = -2;
                            break;
                        } else {
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            long unused5 = clientModeImpl4.mLastConnectAttemptTimestamp = clientModeImpl4.mClock.getWallClockMillis();
                            WifiConfiguration unused6 = ClientModeImpl.this.mTargetWifiConfiguration = config2;
                            boolean unused7 = ClientModeImpl.this.mIsAutoRoaming = true;
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(12, config2);
                            ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                            clientModeImpl5.transitionTo(clientModeImpl5.mRoamingState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /*{ENCODED_INT: 131219}*/:
                    ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                    long unused8 = clientModeImpl6.mLastDriverRoamAttempt = clientModeImpl6.mClock.getWallClockMillis();
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_NETWORK_STATUS /*{ENCODED_INT: 131220}*/:
                    if (message.arg1 == 1) {
                        ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_DIAGS_CONNECT_TIMEOUT);
                        ClientModeImpl.this.mWifiDiagnostics.reportConnectionEvent((byte) 1);
                        ClientModeImpl.this.mWifiScoreCard.noteValidationSuccess(ClientModeImpl.this.mWifiInfo);
                        WifiConfiguration config3 = ClientModeImpl.this.getCurrentWifiConfiguration();
                        if (config3 != null) {
                            ClientModeImpl.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config3.networkId, true);
                        }
                        ClientModeImpl.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                        ClientModeImpl.this.mOppoDhcpRecord.setNetworkValidated(true);
                        ClientModeImpl.this.mOppoMtuProber.StartMtuProber();
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.updateNetworkState(0, ClientModeImpl.this.mWifiInfo.getSSID());
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_ACCEPT_UNVALIDATED /*{ENCODED_INT: 131225}*/:
                    if (message.arg1 != 0) {
                        accept = true;
                    }
                    ClientModeImpl.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(ClientModeImpl.this.mLastNetworkId, accept);
                    break;
                case ClientModeImpl.CMD_CHECK_INTERNET_ACCESS /*{ENCODED_INT: 131480}*/:
                    ClientModeImpl.this.loge("Checking internet access, SSID=" + ClientModeImpl.this.mWifiInfo.getSSID() + " BSSID=" + ClientModeImpl.this.mWifiInfo.getBSSID() + " checkSequence=" + message.arg2);
                    if (OppoDataStallHelper.getInstance() != null) {
                        OppoDataStallHelper.getInstance().checkInternetAccess(message.arg1, message.arg2, -1, ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mLastNetworkId, ClientModeImpl.this.mInterfaceName);
                    }
                    if (ClientModeImpl.this.mWifiInjector.getWifiDisconStat() != null) {
                        ClientModeImpl.this.log("start getApProductVersion");
                        ClientModeImpl.this.mWifiInjector.getWifiDisconStat().getApProductVersion(ClientModeImpl.this.syncGetDhcpResults());
                        break;
                    }
                    break;
                case ClientModeImpl.EVENT_DHCP_STATE_CHANGE /*{ENCODED_INT: 131488}*/:
                    ClientModeImpl.this.logd("IP Recover: receive event CMD_DHCP_RECOVER_STATE");
                    DhcpResults results = (DhcpResults) message.obj;
                    if (ClientModeImpl.this.mConnectedState == ClientModeImpl.this.getCurrentState() && ClientModeImpl.this.mOppoDhcpRecord != null) {
                        ClientModeImpl.this.mOppoDhcpRecord.syncDhcpResults(results, ClientModeImpl.this.getCurrentConfigKey());
                    }
                    ClientModeImpl.this.resetNetworkAngent();
                    ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                    clientModeImpl7.transitionTo(clientModeImpl7.mObtainingIpState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                    if (ClientModeImpl.this.mLastDriverRoamAttempt != 0) {
                        long lastRoam = ClientModeImpl.this.mClock.getWallClockMillis() - ClientModeImpl.this.mLastDriverRoamAttempt;
                        long unused9 = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    }
                    if (ClientModeImpl.unexpectedDisconnectedReason(message.arg2)) {
                        ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config4 = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (ClientModeImpl.this.mWifiInjector.getWifiDisconStat() != null) {
                        ClientModeImpl.this.mWifiInjector.getWifiDisconStat().handleNetworkDisconnectionEvent(message);
                    }
                    if (!(config4 == null || config4.networkId == -1 || OppoAutoConnectManager.getInstance() == null)) {
                        OppoAutoConnectManager.getInstance().handleunexpectedDiconnectDisabled(ClientModeImpl.this.mConnectionTimeStamp, config4.networkId, message.arg2);
                    }
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getBSSID());
                        sb.append(" RSSI=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config4 == null) {
                            str2 = "Unavailable";
                        } else {
                            str2 = config4.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str2);
                        clientModeImpl8.log(sb.toString());
                        break;
                    }
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.logd("ClientModeImpl: Leaving Connected state");
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            long unused = ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl.this.logd(" Enter DisconnectingState State screenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl.this.mDisconnectingWatchdogCount++;
            ClientModeImpl.this.logd("Start Disconnecting Watchdog " + ClientModeImpl.this.mDisconnectingWatchdogCount);
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.sendMessageDelayed(clientModeImpl.obtainMessage(ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER, clientModeImpl.mDisconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            int i = message.what;
            if (i != ClientModeImpl.CMD_DISCONNECT) {
                if (i != ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER) {
                    if (i != 147462) {
                        handleStatus = false;
                    } else {
                        ClientModeImpl.this.deferMessage(message);
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDisconnectedState);
                    }
                } else if (ClientModeImpl.this.mDisconnectingWatchdogCount == message.arg1) {
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("disconnecting watchdog! -> disconnect");
                    }
                    ClientModeImpl.this.handleNetworkDisconnect();
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectedState);
                }
            } else if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.i(ClientModeImpl.TAG, "disconnectedstate enter");
            if (ClientModeImpl.this.mTemporarilyDisconnectWifi) {
                boolean unused = ClientModeImpl.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.logd(" Enter DisconnectedState screenOn=" + ClientModeImpl.this.mScreenOn);
            }
            boolean unused2 = ClientModeImpl.this.mIsAutoRoaming = false;
            if (ClientModeImpl.this.isBootCompleted()) {
                ClientModeImpl.this.mScanRequestProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
            }
            if (!ClientModeImpl.this.hasLoadStore) {
                if (!ClientModeImpl.this.mWifiConfigManager.loadFromStore()) {
                    Log.e(ClientModeImpl.TAG, "Failed to load from config store");
                } else {
                    boolean unused3 = ClientModeImpl.this.hasLoadStore = true;
                }
            }
            if (OppoManuConnectManager.getInstance() != null && !OppoManuConnectManager.getInstance().isManuConnect()) {
                DeviceConfig.setProperty("connectivity", ClientModeImpl.DIRECT_TO_BROWSER, Integer.toString(3), false);
            }
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            boolean z = false;
            switch (message.what) {
                case ClientModeImpl.CMD_DISCONNECT /*{ENCODED_INT: 131145}*/:
                    ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                    ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                    break;
                case ClientModeImpl.CMD_RECONNECT /*{ENCODED_INT: 131146}*/:
                case ClientModeImpl.CMD_REASSOCIATE /*{ENCODED_INT: 131147}*/:
                    if (!ClientModeImpl.this.mTemporarilyDisconnectWifi) {
                        handleStatus = false;
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_SCREEN_STATE_CHANGED /*{ENCODED_INT: 131167}*/:
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    clientModeImpl.handleScreenStateChanged(z);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*{ENCODED_INT: 143371}*/:
                    NetworkInfo info = (NetworkInfo) message.obj;
                    ClientModeImpl.this.mP2pConnected.set(info.isConnected());
                    if (ClientModeImpl.this.mWifiConnectivityManager != null) {
                        ClientModeImpl.this.mWifiConnectivityManager.saveP2pConnectedStatus(info.isConnected());
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    if (message.arg2 == 15) {
                        ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), message.obj == null ? ClientModeImpl.this.mTargetRoamBSSID : (String) message.obj, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult != null && stateChangeResult.state != null) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                        }
                        if (SupplicantState.isConnecting(stateChangeResult.state)) {
                            WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(stateChangeResult.networkId);
                            ClientModeImpl.this.mWifiInfo.setFQDN(null);
                            ClientModeImpl.this.mWifiInfo.setOsuAp(false);
                            ClientModeImpl.this.mWifiInfo.setProviderFriendlyName(null);
                            if (config != null && (config.isPasspoint() || config.osu)) {
                                if (config.isPasspoint()) {
                                    ClientModeImpl.this.mWifiInfo.setFQDN(config.FQDN);
                                } else {
                                    ClientModeImpl.this.mWifiInfo.setOsuAp(true);
                                }
                                ClientModeImpl.this.mWifiInfo.setProviderFriendlyName(config.providerFriendlyName);
                            }
                        }
                        boolean unused = ClientModeImpl.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                        handleStatus = false;
                        break;
                    } else {
                        return true;
                    }
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            this.mSourceMessage = Message.obtain(ClientModeImpl.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case ClientModeImpl.CMD_ENABLE_NETWORK /*{ENCODED_INT: 131126}*/:
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /*{ENCODED_INT: 131144}*/:
                case ClientModeImpl.CMD_RECONNECT /*{ENCODED_INT: 131146}*/:
                case 151553:
                    ClientModeImpl.this.log(" Ignore CMD_RECONNECT request because wps is running");
                    return true;
                case ClientModeImpl.CMD_REASSOCIATE /*{ENCODED_INT: 131147}*/:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_CONNECT /*{ENCODED_INT: 131215}*/:
                case ClientModeImpl.CMD_START_ROAM /*{ENCODED_INT: 131217}*/:
                    int unused = ClientModeImpl.this.mMessageHandlingStatus = -5;
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*{ENCODED_INT: 147459}*/:
                    Pair<Boolean, Integer> loadResult = loadNetworksFromSupplicantAfterWps();
                    boolean success = ((Boolean) loadResult.first).booleanValue();
                    int netId = ((Integer) loadResult.second).intValue();
                    if (!success) {
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 0);
                    } else if (netId == -1) {
                        Log.d(ClientModeImpl.TAG, "WPS NETWORK_CONNECTION_EVENT from other connect, ignore!");
                        break;
                    } else {
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleWpsCompleted(netId);
                        }
                        if (ClientModeImpl.this.mWifiNative != null) {
                            ClientModeImpl.this.mWifiNative.updateCurrentConfigPairByWps(ClientModeImpl.this.mInterfaceName, netId);
                        }
                        message.arg1 = netId;
                        int unused2 = ClientModeImpl.this.mTargetNetworkId = netId;
                        if (ClientModeImpl.this.mWifiNetworkStateTraker != null) {
                            ClientModeImpl.this.mWifiNetworkStateTraker.sendMessage(151553, netId, 0);
                        }
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151565);
                    }
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    ClientModeImpl.this.deferMessage(message);
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.transitionTo(clientModeImpl.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*{ENCODED_INT: 147460}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Network connection lost");
                    }
                    ClientModeImpl.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*{ENCODED_INT: 147462}*/:
                    if (OppoAutoConnectManager.getInstance() != null) {
                        OppoAutoConnectManager.getInstance().handleWpsSupplicantStateChanged(message);
                        break;
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*{ENCODED_INT: 147463}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /*{ENCODED_INT: 147464}*/:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /*{ENCODED_INT: 147465}*/:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    } else {
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                        this.mSourceMessage.recycle();
                        this.mSourceMessage = null;
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().reset();
                        }
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /*{ENCODED_INT: 147466}*/:
                    ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                    clientModeImpl3.transitionTo(clientModeImpl3.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /*{ENCODED_INT: 147467}*/:
                    ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                    clientModeImpl4.transitionTo(clientModeImpl4.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*{ENCODED_INT: 147499}*/:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Ignore Assoc reject event during WPS Connection");
                        break;
                    }
                    break;
                case 151562:
                    ClientModeImpl.this.replyToMessage(message, 151564, 1);
                    break;
                case 151566:
                    if (ClientModeImpl.this.mWifiNative.cancelWps(ClientModeImpl.this.mInterfaceName)) {
                        ClientModeImpl.this.replyToMessage(message, 151568);
                    } else {
                        ClientModeImpl.this.replyToMessage(message, 151567, 0);
                    }
                    if (OppoManuConnectManager.getInstance() != null) {
                        OppoManuConnectManager.getInstance().reset();
                    }
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    clientModeImpl5.transitionTo(clientModeImpl5.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private Pair<Boolean, Integer> loadNetworksFromSupplicantAfterWps() {
            Map<String, WifiConfiguration> configs = new HashMap<>();
            int netId = -1;
            int i = -1;
            if (!ClientModeImpl.this.mWifiNative.migrateNetworksFromSupplicant(ClientModeImpl.this.mInterfaceName, configs, new SparseArray<>())) {
                ClientModeImpl.this.loge("Failed to load networks from wpa_supplicant after Wps");
                return Pair.create(false, -1);
            }
            for (Map.Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                WifiConfiguration config = entry.getValue();
                config.networkId = -1;
                NetworkUpdateResult result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config, this.mSourceMessage.sendingUid);
                if (!result.isSuccess()) {
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.loge("Failed to add network after WPS: " + entry.getValue());
                    return Pair.create(false, -1);
                } else if (!ClientModeImpl.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), true, this.mSourceMessage.sendingUid)) {
                    Log.wtf(ClientModeImpl.TAG, "Failed to enable network after WPS: " + entry.getValue());
                    return Pair.create(false, -1);
                } else {
                    netId = result.getNetworkId();
                }
            }
            if (configs.size() == 1) {
                i = netId;
            }
            return Pair.create(true, Integer.valueOf(i));
        }

        public void exit() {
            if (ClientModeImpl.this.mIsRandomMacCleared) {
                boolean unused = ClientModeImpl.this.setRandomMacOui();
                boolean unused2 = ClientModeImpl.this.mIsRandomMacCleared = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what, Object obj) {
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

    /* access modifiers changed from: private */
    public void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent("android.net.wifi.WIFI_CREDENTIAL_CHANGED");
            intent.putExtra(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_SSID, config.SSID);
            intent.putExtra("et", wifiCredentialEventType);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleGsmAuthRequest(TelephonyUtil.SimAuthRequestData requestData) {
        WifiConfiguration wifiConfiguration = this.mTargetWifiConfiguration;
        if (wifiConfiguration == null || wifiConfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            int subId = TelephonyUtil.getSubId(getSubscriptionManager(), TelephonyUtil.getSimSlot(this.mWifiConfigManager.getConfiguredNetwork(requestData.networkId)));
            String response = TelephonyUtil.getGsmSimAuthResponse(requestData.data, subId, getTelephonyManager());
            if (response == null && (response = TelephonyUtil.getGsmSimpleSimAuthResponse(requestData.data, subId, getTelephonyManager())) == null) {
                response = TelephonyUtil.getGsmSimpleSimNoLengthAuthResponse(requestData.data, subId, getTelephonyManager());
            }
            if (response == null || response.length() == 0) {
                this.mWifiNative.simAuthFailedResponse(this.mInterfaceName, requestData.networkId);
                return;
            }
            logv("Supplicant Response -" + response);
            this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    /* access modifiers changed from: package-private */
    public void handle3GAuthRequest(TelephonyUtil.SimAuthRequestData requestData) {
        WifiConfiguration wifiConfiguration = this.mTargetWifiConfiguration;
        if (wifiConfiguration == null || wifiConfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            TelephonyUtil.SimAuthResponseData response = TelephonyUtil.get3GAuthResponse(requestData, TelephonyUtil.getSubId(getSubscriptionManager(), TelephonyUtil.getSimSlot(this.mWifiConfigManager.getConfiguredNetwork(requestData.networkId))), getTelephonyManager());
            if (response != null) {
                this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, response.type, response.response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(this.mInterfaceName, requestData.networkId);
            }
        } else {
            logd("id does not match targetWifiConfiguration");
        }
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
        return reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34;
    }

    public void updateWifiMetrics() {
        this.mWifiMetrics.updateSavedNetworks(this.mWifiConfigManager.getSavedNetworks(1010));
        this.mPasspointManager.updateMetrics();
    }

    /* access modifiers changed from: private */
    public boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(message.arg1);
        String curConfigKey = getCurrentConfigKey();
        boolean success = this.mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid);
        if (!success) {
            loge("Failed to remove network");
        }
        OppoDualWsmChannel.getInstance().notifyRemoveNetwork(message.arg1);
        if (config != null) {
            this.mOppoDhcpRecord.rmDhcpRecord(config.configKey());
            if (curConfigKey != null && curConfigKey.equals(config.configKey())) {
                this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                logd("deleteNetworkConfigAndSendReply - setSwitchState to SwitchState.INIT");
            }
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
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, message.what, -1);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = (WifiConfiguration) message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration " + this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + getCurrentState().getName());
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
        convertToQuotedSSID(config);
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return result;
        } else if (!this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, message.sendingUid)) {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        } else {
            broadcastWifiCredentialChanged(0, config);
            replyToMessage(message, 151561);
            return result;
        }
    }

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList<>(6);
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

    /* access modifiers changed from: private */
    public String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean p2pSendMessage(int what) {
        AsyncChannel asyncChannel = this.mWifiP2pChannel;
        if (asyncChannel == null) {
            return false;
        }
        asyncChannel.sendMessage(what);
        return true;
    }

    private boolean p2pSendMessage(int what, int arg1) {
        AsyncChannel asyncChannel = this.mWifiP2pChannel;
        if (asyncChannel == null) {
            return false;
        }
        asyncChannel.sendMessage(what, arg1);
        return true;
    }

    private boolean hasConnectionRequests() {
        return this.mNetworkFactory.hasConnectionRequests() || this.mUntrustedNetworkFactory.hasConnectionRequests();
    }

    public boolean getIpReachabilityDisconnectEnabled() {
        return this.mIpReachabilityDisconnectEnabled;
    }

    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        this.mIpReachabilityDisconnectEnabled = enabled;
    }

    public boolean syncInitialize(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_INITIALIZE);
        MtkWifiServiceAdapter.initialize(this.mContext);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public void addNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        this.mNetworkFactory.addCallback(binder, callback, callbackIdentifier);
    }

    public void removeNetworkRequestMatchCallback(int callbackIdentifier) {
        this.mNetworkFactory.removeCallback(callbackIdentifier);
    }

    public void removeNetworkRequestUserApprovedAccessPointsForApp(String packageName) {
        this.mNetworkFactory.removeUserApprovedAccessPointsForApp(packageName);
    }

    public void clearNetworkRequestUserApprovedAccessPoints() {
        this.mNetworkFactory.clear();
    }

    public String getFactoryMacAddress() {
        MacAddress macAddress = this.mWifiNative.getFactoryMacAddress(this.mInterfaceName);
        if (macAddress != null) {
            return macAddress.toString();
        }
        if (!this.mConnectedMacRandomzationSupported) {
            return this.mWifiNative.getMacAddress(this.mInterfaceName);
        }
        return null;
    }

    public void setDeviceMobilityState(int state) {
        this.mWifiConnectivityManager.setDeviceMobilityState(state);
    }

    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        this.mWifiMetrics.incrementWifiUsabilityScoreCount(seqNum, score, predictionHorizonSec);
    }

    @VisibleForTesting
    public void probeLink(WifiNative.SendMgmtFrameCallback callback, int mcs) {
        this.mWifiNative.probeLink(this.mInterfaceName, MacAddress.fromString(this.mWifiInfo.getBSSID()), callback, mcs);
    }

    public void setPowerSavingMode(boolean mode) {
        sendMessage(obtainMessage(M_CMD_SET_POWER_SAVING_MODE, mode ? 1 : 0, 0));
    }

    /* access modifiers changed from: private */
    public void handleWifiMinidumpSwitch() {
        boolean enable = getRomUpdateBooleanValue("OPPO_BASIC_WIFI_COLLECT_WIFI_COREDUMP", true).booleanValue();
        Log.d(TAG, "handleWifiMinidumpSwitch enable = " + enable);
        if (enable) {
            logd("set wifi coredump enable");
        } else {
            logd("set wifi coredump disable");
        }
    }

    public void stopP2pConnectInternal() {
        p2pSendMessage(139280);
    }

    /* access modifiers changed from: private */
    public String convertToQuotedSSID(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        if (ssid.charAt(0) == '\"' && (ssid.charAt(0) != '\"' || ssid.charAt(ssid.length() - 1) == '\"')) {
            return ssid;
        }
        return "\"" + ssid + "\"";
    }

    public void setTargetNetworkId(int id) {
        if (id != -1) {
            this.mTargetNetworkId = id;
        }
    }

    /* access modifiers changed from: private */
    public boolean isManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getManuConnectNetId() {
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

    public boolean isNetworkConnecting() {
        return isNetworkConnecting(this.mTargetNetworkId);
    }

    public boolean isNetworkConnecting(int netId) {
        return isNetworkManuConnecting(netId) || isNetworkAutoConnecting(netId);
    }

    public boolean isNetworkAutoConnectingOrConnected(int netId) {
        return isNetworkAutoConnecting(netId) || isNetworkConnected(netId);
    }

    public boolean isNetworkManuConnecting(int netId) {
        if (this.mTargetNetworkId == netId && OppoAutoConnectManager.getInstance() != null && OppoAutoConnectManager.getInstance().isManuConnect()) {
            return true;
        }
        return false;
    }

    public boolean isNetworkAutoConnecting(int netId) {
        if (this.mTargetNetworkId != netId) {
            return false;
        }
        NetworkInfo networkInfo = this.mNetworkInfo;
        if ((networkInfo == null || !networkInfo.isConnectedOrConnecting()) && !this.mOppoWifiConnectionAlert.isSelectingNetwork()) {
            return false;
        }
        return true;
    }

    public boolean isNetworkConnected(int netId) {
        if (this.mWifiInfo.getNetworkId() == netId) {
            return true;
        }
        return false;
    }

    public void prepareForForcedConnection(int netId) {
        WifiConnectivityManager wifiConnectivityManager = this.mWifiConnectivityManager;
        if (wifiConnectivityManager != null) {
            wifiConnectivityManager.prepareForForcedConnection(netId);
        }
    }

    public boolean isBootCompleted() {
        if (((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted()) {
            return true;
        }
        return false;
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public WifiConfiguration getmTargetWifiConfiguration() {
        return this.mTargetWifiConfiguration;
    }

    public boolean isSupplicantStateDisconnected() {
        SupplicantStateTracker supplicantStateTracker = this.mSupplicantStateTracker;
        if (supplicantStateTracker == null) {
            return false;
        }
        String supplicantState = supplicantStateTracker.getSupplicantStateName();
        if ("DisconnectedState".equals(supplicantState) || "ScanState".equals(supplicantState) || "InactiveState".equals(supplicantState)) {
            return true;
        }
        return false;
    }

    public boolean startScan(int callingUid, String packageName) {
        if (getCurrentState() != this.mObtainingIpState) {
            return this.mScanRequestProxy.startScan(callingUid, packageName);
        }
        if (!hasMessages(CMD_START_SCAN)) {
            sendMessage(CMD_START_SCAN, 1000, 0, packageName);
            return true;
        }
        logd("has CMD_START_SCAN in queue, Just return.");
        return true;
    }

    public boolean isSupplicantAvailable() {
        int wifiState = syncGetWifiState();
        if (wifiState == 0 || 1 == wifiState) {
            logd("wifi in disabled or disabling state!");
            return false;
        } else if (3 == getOperationalModeForTest()) {
            logd("wifi in disable pending state!");
            return false;
        } else if (!"SupplicantStoppingState".equalsIgnoreCase(getCurrentState().getName())) {
            return true;
        } else {
            logd("supplicant in stoppong state!");
            return false;
        }
    }

    public boolean isPnoStarted() {
        WifiConnectivityManager wifiConnectivityManager = this.mWifiConnectivityManager;
        if (wifiConnectivityManager != null) {
            return wifiConnectivityManager.isPnoStarted();
        }
        return false;
    }

    public void tryStartPnoScan() {
        WifiConnectivityManager wifiConnectivityManager;
        if (!this.mScreenOn && (wifiConnectivityManager = this.mWifiConnectivityManager) != null) {
            wifiConnectivityManager.tryStartConnectivityScan();
        }
    }

    /* access modifiers changed from: private */
    public void convertToQuotedSSID(WifiConfiguration config) {
        if (config != null && !TextUtils.isEmpty(config.SSID)) {
            if (config.SSID.charAt(0) != '\"' || (config.SSID.charAt(0) == '\"' && config.SSID.charAt(config.SSID.length() - 1) != '\"')) {
                config.SSID = "\"" + config.SSID + "\"";
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetNetworkAngent() {
        log("[bug#1131400] resetNetworkAngent");
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        clearLinkProperties();
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
        NetworkCapabilities nc = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        nc.setNetworkSpecifier(null);
        this.mNetworkAgent = new WifiNetworkAgent(getHandler().getLooper(), this.mContext, "WifiNetworkAgent", this.mNetworkInfo, nc, this.mLinkProperties, 5, this.mNetworkMisc);
    }

    public String getCurrentConfigKey() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        return config.configKey();
    }

    /* access modifiers changed from: private */
    public boolean hasConfigKeyChanged(int newNetId) {
        boolean cfgKeyChanged;
        WifiConfiguration newCfg;
        String newCfgKey = null;
        if (!(newNetId == -1 || (newCfg = this.mWifiConfigManager.getConfiguredNetwork(newNetId)) == null)) {
            newCfgKey = newCfg.configKey();
        }
        String str = this.sLastConfigKey;
        boolean z = true;
        if (str == null || newCfgKey == null) {
            if (this.sLastConfigKey == newCfgKey) {
                z = false;
            }
            cfgKeyChanged = z;
        } else {
            cfgKeyChanged = !str.equals(newCfgKey);
        }
        if (cfgKeyChanged) {
            this.sLastConfigKey = newCfgKey;
        }
        return cfgKeyChanged;
    }

    public boolean isChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (!TextUtils.isEmpty(mcc) || !TextUtils.isEmpty(mcc2)) {
            return mcc.equals("460") || mcc2.equals("460");
        }
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    public static boolean isNotChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (TextUtils.isEmpty(mcc) && TextUtils.isEmpty(mcc2)) {
            return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        }
        if ("460".equals(mcc) || "460".equals(mcc2)) {
            return false;
        }
        return true;
    }

    public boolean isDupDhcp() {
        return this.mOppoDhcpRecord.isDupDhcp();
    }

    private boolean isWlanAssistantEnable() {
        int romUpdate = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_ASSISTANT_ROMUPDATE, 1);
        boolean wlanAssistantFeature = this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        boolean romUpdateWlanAssistant = romUpdate == 1;
        if (!wlanAssistantFeature || !romUpdateWlanAssistant) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public OppoWifiAssistantStateTraker makeWifiNetworkStateTracker() {
        return new OppoWifiAssistantStateTraker(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler(), this.mScanRequestProxy);
    }

    /* access modifiers changed from: private */
    public OppoWifiNetworkSwitchEnhance makeWifiNetworkAvailable() {
        return new OppoWifiNetworkSwitchEnhance(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mScanRequestProxy);
    }

    /* access modifiers changed from: private */
    public boolean wifiAssistantForSoftAP() {
        boolean isSoftAP = this.mWifiNetworkStateTraker.isSoftAp(this.mLinkProperties);
        boolean romupdateValue = getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_CONTROL_SOFTAP", true).booleanValue();
        if (!isSoftAP) {
            return true;
        }
        if (!isSoftAP || !romupdateValue) {
            return false;
        }
        return true;
    }

    public void sendWifiNetworkScore(int score, boolean explicitlySelected) {
        logd("sws score= " + score + ",mNetworkAgent =" + this.mNetworkAgent);
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            if (!explicitlySelected) {
                wifiNetworkAgent.explicitlySelected(explicitlySelected);
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

    /* access modifiers changed from: private */
    public void setCaptivePortalMode(int mode) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "captive_portal_mode", mode);
    }

    public boolean isRoaming() {
        return this.mIsAutoRoaming;
    }

    /* access modifiers changed from: private */
    public WifiConfiguration getConnectConfig(int netId, String bssid) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
        if (config == null) {
            return null;
        }
        convertToQuotedSSID(config);
        String tmpBssid = this.mOppoClientModeImplUtil.getBestBssidForNetId(netId, this.mScanRequestProxy, this.mWifiConfigManager);
        Boolean bssidTmpReset = false;
        if ("any".equals(bssid) && tmpBssid != null) {
            if (this.mVerboseLoggingEnabled) {
                logd("config bssid = " + config.BSSID);
            }
            if (config.BSSID == null || "any".equals(config.BSSID)) {
                if (this.mVerboseLoggingEnabled) {
                    logd("reset config bssid to " + tmpBssid + "  temp");
                }
                config.BSSID = tmpBssid;
                bssidTmpReset = true;
            }
        }
        if (bssidTmpReset.booleanValue()) {
            setTargetBssid(config, tmpBssid);
        } else {
            setTargetBssid(config, bssid);
        }
        return config;
    }

    /* access modifiers changed from: private */
    public boolean clearRandomMacOui() {
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, new byte[]{0, 0, 0});
    }

    public String syncGetCurrentNetworkWpsNfcConfigurationToken() {
        return this.mWifiNative.getCurrentNetworkWpsNfcConfigurationToken(this.mInterfaceName);
    }

    private String getPersistedDeviceName() {
        String deviceName = this.mFacade.getStringSetting(this.mContext, "wifi_p2p_device_name");
        if (deviceName == null) {
            return SystemProperties.get("ro.oppo.market.name", "OPPO");
        }
        return deviceName;
    }

    private void initializeWpsDetails() {
        String detail = getPersistedDeviceName();
        if (!this.mWifiNative.setDeviceName(this.mInterfaceName, detail)) {
            loge("Failed to set device name " + detail);
        }
        String detail2 = this.mPropertyService.get("ro.product.manufacturer", "");
        if (!this.mWifiNative.setManufacturer(this.mInterfaceName, detail2)) {
            loge("Failed to set manufacturer " + detail2);
        }
        String detail3 = this.mPropertyService.get("ro.product.model", "");
        if (!this.mWifiNative.setModelName(this.mInterfaceName, detail3)) {
            loge("Failed to set model name " + detail3);
        }
        String detail4 = this.mPropertyService.get("ro.product.model", "");
        if (!this.mWifiNative.setModelNumber(this.mInterfaceName, detail4)) {
            loge("Failed to set model number " + detail4);
        }
        String detail5 = this.mPropertyService.get("ro.serialno", "");
        if (!this.mWifiNative.setSerialNumber(this.mInterfaceName, detail5)) {
            loge("Failed to set serial number " + detail5);
        }
        if (!this.mWifiNative.setConfigMethods(this.mInterfaceName, "physical_display virtual_push_button")) {
            loge("Failed to set WPS config methods");
        }
        if (!this.mWifiNative.setDeviceType(this.mInterfaceName, this.mPrimaryDeviceType)) {
            loge("Failed to set primary device type " + this.mPrimaryDeviceType);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return false;
     */
    public boolean isUsingDHCP() {
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig == null || currentConfig.getIpAssignment() != IpConfiguration.IpAssignment.DHCP) {
            return false;
        }
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null && this.mDhcpResults.ipAddress != null && this.mDhcpResults.ipAddress.getAddress() != null && this.mDhcpResults.ipAddress.isIpv4()) {
                return true;
            }
        }
    }

    public void notifyDataStallEvent(int errCode) {
        sendMessage(CMD_FIRMWARE_ALERT, errCode + 256);
    }

    public int getCurrentCheckInetAccessSeq() {
        return this.mCheckInetAccessSeq;
    }

    public String getWifiPowerEventCode() {
        StringBuilder eventPower = new StringBuilder();
        float idleTime = ((float) (System.currentTimeMillis() - this.mScreenOffTime)) / 60000.0f;
        if (idleTime == 0.0f) {
            return null;
        }
        eventPower.append("event=");
        int i = this.mIdleScanTimes;
        if (i != 0) {
            float scanFrequency = idleTime / ((float) i);
        }
        int i2 = this.mIdleRenewTimes;
        if (i2 != 0) {
            float renewFrequency = idleTime / ((float) i2);
        }
        int i3 = this.mIdleDisConnTimes;
        if (i3 != 0) {
            float disconnFrequency = idleTime / ((float) i3);
        }
        int i4 = this.mIdleGroupTimes;
        if (i4 != 0) {
            float keyChangeFrequency = idleTime / ((float) i4);
        }
        float powerValue = (((float) this.mIdleScanTimes) * WEIGHT_SCAN) + (((float) this.mIdleRenewTimes) * WEIGHT_RENEW) + (((float) this.mIdleDisConnTimes) * WEIGHT_DISCONN) + (((float) this.mIdleGroupTimes) * WEIGHT_GROUP);
        eventPower.append(compareMaxPower());
        eventPower.append("\n");
        eventPower.append("powerValue=" + Float.toString(powerValue));
        log("wifieventpower" + eventPower.toString());
        return eventPower.toString();
    }

    private String compareMaxPower() {
        String maxValue = "SCAN_FREQUENT";
        float powerValueMax = ((float) this.mIdleScanTimes) * WEIGHT_SCAN;
        if (this.mVerboseLoggingEnabled) {
            log("compareMaxPower mIdleScanTimes=" + this.mIdleScanTimes + ",mIdleRenewTimes=" + this.mIdleRenewTimes + ",mIdleDisConnTimes=" + this.mIdleDisConnTimes + ",mIdleGroupTimes=" + this.mIdleGroupTimes);
        }
        int i = this.mIdleRenewTimes;
        if (((float) i) * WEIGHT_RENEW > powerValueMax) {
            maxValue = "RENEW_FREQUENT";
            powerValueMax = ((float) i) * WEIGHT_RENEW;
        }
        int i2 = this.mIdleDisConnTimes;
        if (((float) i2) * WEIGHT_DISCONN > powerValueMax) {
            maxValue = "DISCONN_FREQUENT";
            powerValueMax = ((float) i2) * WEIGHT_SCAN;
        }
        int i3 = this.mIdleGroupTimes;
        if (((float) i3) * WEIGHT_GROUP <= powerValueMax) {
            return maxValue;
        }
        float powerValueMax2 = ((float) i3) * WEIGHT_SCAN;
        return "GROUP_FREQUENT";
    }

    public List<ScanResult> getScanResults() {
        return this.mScanRequestProxy.syncGetScanResultsList();
    }

    public void resetVerbose() {
        if (SystemProperties.get("debug.wifi.prdebug", "0").equals("1")) {
            SystemProperties.set("debug.wifi.prdebug", "0");
            if (this.mFromKeylogVerbose) {
                enableVerboseLogging(0);
            }
        }
    }

    public void handleScanResultEvent() {
        log("handleScanResultEvent2");
        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiNetworkAvailable;
        if (oppoWifiNetworkSwitchEnhance != null) {
            oppoWifiNetworkSwitchEnhance.detectScanResult(this.mLastScanTime);
        }
    }

    public void setStatistics(String mapValue, String eventId) {
        this.mOppoClientModeImplUtil.setStatistics(mapValue, eventId);
    }

    public void reportFoolProofException() {
        this.mOppoClientModeImplUtil.reportFoolProofException();
    }

    private void sheduleRestartWifi() {
        sheduleRestartWifi(-1);
    }

    /* access modifiers changed from: private */
    public void sheduleRestartWifi(int netId) {
        if (getRomUpdateIntegerValue("BASIC_FOOL_PROOF_ON", 1).intValue() != 1) {
            log("fool-proof,foolProofOn != 1, don't restart!");
            return;
        }
        if (this.mActiveModeWarden == null) {
            this.mActiveModeWarden = this.mWifiInjector.getActiveModeWarden();
        }
        if (this.mActiveModeWarden != null) {
            this.mWifiInjector.getSelfRecovery().trigger(2);
            if (netId != -1) {
                sendMessageDelayed(obtainMessage(151553, netId, -1), 5000);
            }
        }
    }

    public void handleScanResultEvent(List<ScanResult> scanResults) {
        if (this.mVerboseLoggingEnabled) {
            log("[1616660] syncScanResults");
        }
        this.mLastScanTime = System.currentTimeMillis();
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendScanResultEvt();
        }
        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiNetworkAvailable;
        if (oppoWifiNetworkSwitchEnhance != null) {
            oppoWifiNetworkSwitchEnhance.detectScanResult(this.mLastScanTime);
        } else {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
            if (oppoWifiAssistantStateTraker != null) {
                oppoWifiAssistantStateTraker.detectScanResult(this.mLastScanTime);
            }
        }
        if (this.mOppoScanResultsProxy == null) {
            this.mOppoScanResultsProxy = this.mWifiInjector.getOppoScanResultsProxy();
        }
        if (this.mOppoScanResultsProxy != null && getCurrentState() != null) {
            this.mOppoScanResultsProxy.checkScanFewAP(scanResults, getCurrentState().getName());
        }
    }

    /* access modifiers changed from: private */
    public void enableTcpTimestamps(boolean enable, int errorThreshold) {
        try {
            FileUtils.stringToFile("/proc/sys/net/ipv4/tcp_timestamps", enable ? "1" : "0");
            FileUtils.stringToFile("/proc/sys/net/ipv4/tcp_timestamps_control", Integer.toString(errorThreshold) + " 1");
        } catch (IOException e) {
            loge("Failed to wirte file" + e);
        }
    }

    /* access modifiers changed from: private */
    public void checkAndSetSsidForConfig(WifiConfiguration config) {
        this.mOppoClientModeImplUtil.checkAndSetSsidForConfig(config, this.mScanRequestProxy);
    }

    /* access modifiers changed from: private */
    public void changeTcpRandomTS(boolean onConnected) {
        if (!getRomUpdateBooleanValue("CHANGE_TCP_RANDOM_TIMESTAMP", true).booleanValue()) {
            return;
        }
        if (onConnected) {
            try {
                FileUtils.stringToFile("/proc/sys/net/ipv4/tcp_random_timestamp", "0");
            } catch (Exception e) {
                loge("Unable to write file" + e);
            }
        } else {
            FileUtils.stringToFile("/proc/sys/net/ipv4/tcp_random_timestamp", "1");
        }
    }

    private static String readStringFromFile(String fileName) {
        StringBuilder sb;
        BufferedReader reader = null;
        String tempString = "";
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            tempString = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            Log.e(TAG, "readFileByLines io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    Log.e(TAG, "readFileByLines io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        return tempString;
        sb.append("readFileByLines io close exception :");
        sb.append(e1.getMessage());
        Log.e(TAG, sb.toString());
        return tempString;
    }

    public boolean is1x1IotRouter() {
        if (this.mIs1x1IotRouter != -1) {
            log("0 bssid: current is1x1Iot: " + this.mIs1x1IotRouter);
            return this.mIs1x1IotRouter == 1;
        }
        String buf = readStringFromFile("/proc/oppo_dualSta/is1x1IOTRouter");
        if (buf != null) {
            String[] temp = buf.split("=");
            if (temp.length == 2) {
                String routerBssid = temp[0];
                String isIotRouter = temp[1];
                log("1 get is1x1Iot bssid: " + routerBssid + " result: " + isIotRouter);
                if (routerBssid.length() > 0) {
                    this.mIs1x1IotRouter = isIotRouter.contains("1") ? 1 : 0;
                    return this.mIs1x1IotRouter == 1;
                }
            }
        }
        this.mIs1x1IotRouter = 0;
        log("2 bssid: current is1x1Iot: " + this.mIs1x1IotRouter);
        return false;
    }

    /* access modifiers changed from: private */
    public boolean attemptWpa2FallbackConnectionIfRequired(String bssid) {
        WifiConfiguration config = this.mOppoClientModeImplUtil.attemptWpa2FallbackConnectionIfRequired(this.mTargetWifiConfiguration, this.mWifiConfigManager, this.mTargetNetworkId, this.mInterfaceName, this.mWifiInfo, this.mOppoScanResultsProxy);
        if (config != null) {
            if (bssid != null) {
                this.mWifiConnectivityManager.trackBssid(bssid, true, 0);
            }
            this.mTargetWifiConfiguration = config;
            Log.d(TAG, "try to attemptWpa2FallbackConnectionIfRequired");
            OppoWifiConnectionAlert oppoWifiConnectionAlert = this.mOppoWifiConnectionAlert;
            if (oppoWifiConnectionAlert != null) {
                oppoWifiConnectionAlert.setIsSelectingNetworkTrueForcely();
            }
            return true;
        }
        Log.d(TAG, "do not attemptWpa2FallbackConnectionIfRequired");
        return false;
    }
}
