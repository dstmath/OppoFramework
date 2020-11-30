package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
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
import android.net.TcpKeepalivePacketData;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientManager;
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
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.OppoAssertTip;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
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
import com.android.server.net.OppoClientModeManagerHelper;
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
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.mediatek.server.wifi.MtkWifiServiceAdapter;
import java.io.BufferedReader;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

public class OppoClientModeImpl2 extends StateMachine {
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    static final int BASE = 131072;
    private static final int BASE_OPPO = 131472;
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF = 131281;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_ASSOCIATED_BSSID = 131219;
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
    private static final int CMD_FIRMWARE_ALERT = 131481;
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
    public static final int DATA_STALL_OFFSET_REASON_CODE = 256;
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    @VisibleForTesting
    public static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    public static final int DISABLED_MODE = 4;
    private static final int DISABLE_INTERFACE = -1;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    public static final int EVENT_FIND_DUP_SERVER = 131473;
    public static final int EVENT_FIX_SERVER_FAILURE = 131476;
    public static final int EVENT_SWITCH_SERVER_FAILURE = 131475;
    public static final int EVENT_UPDATE_LEASE_EXPRIY = 131474;
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
    private static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_NORMAL = 100;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE = 3000;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static final String OTA_VERSION = "ro.build.version.ota";
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
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
    private static final String TAG = "WifiOppoClientModeImpl2";
    private static final int TRIGGER_RESTORE_DELAY_TIME = 2000;
    private static final float WEIGHT_DISCONN = 0.6f;
    private static final float WEIGHT_GROUP = 0.8f;
    private static final float WEIGHT_RENEW = 0.5f;
    private static final float WEIGHT_SCAN = 0.28f;
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final boolean WIFI_DBG = SystemProperties.getBoolean("persist.wifi.dbg", false);
    public static final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final SparseArray<String> sGetWhatToString = MessageUtils.findMessageNames(sMessageClasses);
    private static final Class[] sMessageClasses = {AsyncChannel.class, OppoClientModeImpl2.class};
    private static int sScanAlarmIntentCount = 0;
    private int DISCONN_FLAG = 4096;
    private int GROUP_FLAG = 16;
    private int RENEW_FLAG = 1;
    private int SCAN_FLAG = 256;
    private ActiveModeWarden mActiveModeWarden = null;
    private HashMap<String, Integer> mApTriggerDisableCount = new HashMap<>();
    private OppoAssertTip mAssertProxy = null;
    private Handler mAsyncHandler;
    private boolean mAutoSwitch;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive = false;
    private final BuildProperties mBuildProperties;
    private State mCaptiveState = new CaptiveState();
    private int mCheckInetAccessSeq = 0;
    private ClientModeManager.Listener mClientModeCallback = null;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    private int mConnectedId = -1;
    private boolean mConnectedMacRandomzationSupported;
    private State mConnectedState = new ConnectedState();
    private long mConnectionTimeStamp = 0;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    private State mDefaultState = new DefaultState();
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private boolean mDidBlackListBSSID = false;
    private HashMap<String, Long> mDisableConnectDuring = new HashMap<>();
    private State mDisconnectedState = new DisconnectedState();
    private State mDisconnectingState = new DisconnectingState();
    int mDisconnectingWatchdogCount = 0;
    private boolean mEnableRssiPolling = false;
    private FrameworkFacade mFacade;
    private boolean mFromKeylogVerbose = false;
    private boolean mHasInternetAccess = false;
    private int mIdleDisConnTimes = 0;
    private int mIdleGroupTimes = 0;
    private int mIdleRenewTimes = 0;
    private int mIdleScanTimes = 0;
    private int mIndex = 0;
    private String mInterfaceName;
    private volatile IpClientManager mIpClient;
    private IpClientCallbacksImpl mIpClientCallbacks;
    private boolean mIpReachabilityDisconnectEnabled = true;
    private boolean mIsAutoRoaming = false;
    private boolean mIsRunning = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private String mLastBssid;
    private long mLastConnectAttemptTimestamp = 0;
    private NetworkInfo.DetailedState mLastDetailedState = NetworkInfo.DetailedState.IDLE;
    private long mLastDriverRoamAttempt = 0;
    private Pair<String, String> mLastL2KeyAndGroupHint = null;
    private WifiLinkLayerStats mLastLinkLayerStats;
    private long mLastLinkLayerStatsUpdate = 0;
    private int mLastNetworkId;
    private long mLastOntimeReportTimeStamp = 0;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private long mLastScanTime = 0;
    private long mLastScreenStateChangeTimeStamp = 0;
    private long mLastSelectEvtTimeStamp = 0;
    private int mLastSignalLevel = -1;
    private SupplicantState mLastSupplicantState = SupplicantState.DISCONNECTED;
    private final LinkProbeManager mLinkProbeManager;
    private LinkProperties mLinkProperties;
    private final McastLockManagerFilterController mMcastLockManagerFilterController;
    private int mMessageHandlingStatus = 0;
    private boolean mModeChange = false;
    @GuardedBy({"mNetworkAgentLock"})
    private WifiNetworkAgent mNetworkAgent;
    private final Object mNetworkAgentLock = new Object();
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private boolean mNetworkDetectValid = false;
    private OppoWifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private AtomicInteger mNullMessageCounter = new AtomicInteger(0);
    private State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    private int mOperationalMode = 4;
    private OppoClientModeImplUtil2 mOppoClientModeImplUtil;
    private OppoDhcpRecord mOppoDhcpRecord = null;
    private OppoMtuProber mOppoMtuProber;
    private OppoScanResultsProxy mOppoScanResultsProxy;
    private WifiInfo mOtherWifiInfo;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    private final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    private volatile int mPollRssiIntervalMsecs = 3000;
    private SupplicantState mPowerState = SupplicantState.UNINITIALIZED;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    int mRoamWatchdogCount = 0;
    private State mRoamingState = new RoamingState();
    private int[] mRssiArray = {0, 0, 0, 0, 0};
    private int mRssiCount = 5;
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    private final SarManager mSarManager;
    private ScanRequestProxy mScanRequestProxy;
    private long mScreenOffTime = 0;
    private boolean mScreenOn = false;
    private long mScreenOnTime = 0;
    private SubscriptionManager mSubscriptionManager;
    private long mSupplicantScanIntervalMs;
    private SupplicantStateTracker mSupplicantStateTracker;
    private int mSuspendOptNeedsDisabled = 0;
    private PowerManager.WakeLock mSuspendWakeLock;
    private int mTargetNetworkId = -1;
    private String mTargetRoamBSSID = "any";
    private WifiConfiguration mTargetWifiConfiguration = null;
    private final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    private boolean mTemporarilyDisconnectWifi = false;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private boolean mVerboseLoggingEnabled = false;
    private PowerManager.WakeLock mWakeLock;
    private final WifiConfigManager mWifiConfigManager;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private final ExtendedWifiInfo mWifiInfo;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    private final WifiNative mWifiNative;
    private OppoWifiAssistantStateTraker2 mWifiNetworkStateTraker2;
    private WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private AsyncChannel mWifiP2pChannel;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private final WifiScoreCard mWifiScoreCard;
    private final WifiScoreReport mWifiScoreReport;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    private WifiStateTracker mWifiStateTracker;
    private final WifiTrafficPoller mWifiTrafficPoller;
    private final String mapKey = "mapKey-";
    private int originRssi = -127;
    private String sLastConfigKey = null;
    private WifiP2pServiceImpl wifiP2pServiceImpl;

    static /* synthetic */ int access$12008(OppoClientModeImpl2 x0) {
        int i = x0.mRoamFailCount;
        x0.mRoamFailCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$12408(OppoClientModeImpl2 x0) {
        int i = x0.mCheckInetAccessSeq;
        x0.mCheckInetAccessSeq = i + 1;
        return i;
    }

    static /* synthetic */ int access$8408(OppoClientModeImpl2 x0) {
        int i = x0.mRssiPollToken;
        x0.mRssiPollToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$908(OppoClientModeImpl2 x0) {
        int i = x0.mIdleRenewTimes;
        x0.mIdleRenewTimes = i + 1;
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
    /* access modifiers changed from: public */
    private void processRssiThreshold(byte curRssi, int reason, WifiNative.WifiRssiEventHandler rssiHandler) {
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
                Log.d(TAG, "Re-program RSSI thresholds for " + getWhatToString(reason) + ": [" + ((int) minRssi) + ", " + ((int) maxRssi) + "], curRssi=" + ((int) curRssi) + " ret=" + ret);
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
            logd(dbg + " clearTargetBssid any key=" + config.configKey());
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
            logd(dbg + " clearCurrentConfigBSSID any key=" + config.configKey());
        }
        this.mTargetRoamBSSID = "any";
        String str = config.BSSID;
        return true;
    }

    public boolean isThirdApp(int uid) {
        if (uid < 10000 || uid > 19999) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SubscriptionManager getSubscriptionManager() {
        if (this.mSubscriptionManager == null) {
            this.mSubscriptionManager = this.mWifiInjector.makeSubscriptionManager();
        }
        return this.mSubscriptionManager;
    }

    public OppoClientModeImpl2(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
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
        this.mSarManager = sarManager;
        this.mWifiTrafficPoller = wifiTrafficPoller;
        this.mLinkProbeManager = linkProbeManager;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mOppoClientModeImplUtil = new OppoClientModeImplUtil2(this.mContext, this.mWifiNative, this);
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
        this.mWifiInfo = new ExtendedWifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mLinkProperties = new LinkProperties();
        this.mMcastLockManagerFilterController = new McastLockManagerFilterController();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mCountryCode = countryCode;
        this.mWifiScoreReport = new WifiScoreReport(this.mWifiInjector.getScoringParams(), this.mClock);
        this.mNetworkCapabilitiesFilter.addTransportType(8);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
        this.mNetworkCapabilitiesFilter.addCapability(18);
        this.mNetworkCapabilitiesFilter.addCapability(20);
        this.mNetworkCapabilitiesFilter.addCapability(13);
        this.mNetworkCapabilitiesFilter.addCapability(30);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        this.mNetworkFactory = this.mWifiInjector.makeWifiNetworkFactory2(this.mNetworkCapabilitiesFilter, null);
        this.mUntrustedNetworkFactory = this.mWifiInjector.makeUntrustedWifiNetworkFactory2(this.mNetworkCapabilitiesFilter, null);
        this.mWifiNetworkSuggestionsManager = this.mWifiInjector.getWifiNetworkSuggestionsManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoClientModeImpl2.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_SCREEN_STATE_CHANGED, 1);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_SCREEN_STATE_CHANGED, 0);
                }
            }
        }, filter);
        this.mFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.OppoClientModeImpl2.AnonymousClass2 */

            public void onChange(boolean selfChange) {
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), true, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.OppoClientModeImpl2.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                boolean z = true;
                if (Settings.Global.getInt(oppoClientModeImpl2.mContext.getContentResolver(), OppoClientModeImpl2.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                    z = false;
                }
                oppoClientModeImpl2.mAutoSwitch = z;
                if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                    OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setAutoSwitch(OppoClientModeImpl2.this.mAutoSwitch);
                }
                Log.d(OppoClientModeImpl2.TAG, "onChange mAutoSwitch= " + OppoClientModeImpl2.this.mAutoSwitch);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), true, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.OppoClientModeImpl2.AnonymousClass4 */

            public void onChange(boolean selfChange) {
                if (OppoClientModeImpl2.this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant")) {
                    boolean z = true;
                    boolean isWlanAssistant = Settings.Global.getInt(OppoClientModeImpl2.this.mContext.getContentResolver(), OppoClientModeImpl2.WIFI_ASSISTANT_ROMUPDATE, 1) == 1;
                    Log.d(OppoClientModeImpl2.TAG, "onChange wa= " + isWlanAssistant);
                    if (isWlanAssistant) {
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 == null) {
                            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                            oppoClientModeImpl2.mWifiNetworkStateTraker2 = oppoClientModeImpl2.makeWifiNetworkStateTracker();
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setFeatureState(true);
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.enableVerboseLogging(OppoClientModeImpl2.this.mVerboseLoggingEnabled ? 1 : 0);
                            OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                            if (Settings.Global.getInt(oppoClientModeImpl22.mContext.getContentResolver(), OppoClientModeImpl2.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
                                z = false;
                            }
                            oppoClientModeImpl22.mAutoSwitch = z;
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setAutoSwitch(OppoClientModeImpl2.this.mAutoSwitch);
                        }
                    } else if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setFeatureState(false);
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateWifiState(-1);
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2 = null;
                    }
                }
            }
        });
        this.mAssertProxy = OppoAssertTip.getInstance();
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
        setInitialState(this.mDefaultState);
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        HandlerThread handlerThread = new HandlerThread("CheckInternetAccess");
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper());
        if (isWlanAssistantEnable()) {
            this.mWifiNetworkStateTraker2 = new OppoWifiAssistantStateTraker2(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler(), this.mScanRequestProxy);
            this.mAutoSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1 ? false : true;
            this.mWifiNetworkStateTraker2.setAutoSwitch(this.mAutoSwitch);
        }
    }

    public void start() {
        OppoClientModeImpl2.super.start();
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
    /* access modifiers changed from: public */
    private void setMulticastFilter(boolean enabled) {
        if (this.mIpClient != null) {
            this.mIpClient.setMulticastFilter(enabled);
        }
    }

    class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        McastLockManagerFilterController() {
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void startFilteringMulticastPackets() {
            OppoClientModeImpl2.this.setMulticastFilter(true);
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void stopFilteringMulticastPackets() {
            OppoClientModeImpl2.this.setMulticastFilter(false);
        }
    }

    /* access modifiers changed from: package-private */
    public class IpClientCallbacksImpl extends IpClientCallbacks {
        private final ConditionVariable mWaitForCreationCv = new ConditionVariable(false);
        private final ConditionVariable mWaitForStopCv = new ConditionVariable(false);

        IpClientCallbacksImpl() {
        }

        public void onIpClientCreated(IIpClient ipClient) {
            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
            oppoClientModeImpl2.mIpClient = new IpClientManager(ipClient, oppoClientModeImpl2.getName());
            this.mWaitForCreationCv.open();
        }

        public void onPreDhcpAction() {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_PRE_DHCP_ACTION);
        }

        public void onPostDhcpAction() {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_POST_DHCP_ACTION);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults != null) {
                OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            } else {
                OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_IPV4_PROVISIONING_FAILURE);
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(7);
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_UPDATE_LINKPROPERTIES, newLp);
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(8);
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(9);
            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
            oppoClientModeImpl2.logd("onReachabilityLost received:" + logMsg);
        }

        public void installPacketFilter(byte[] filter) {
        }

        public void startReadPacketFilter() {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_READ_PACKET_FILTER);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }

        public void onQuit() {
            this.mWaitForStopCv.open();
        }

        public void onFindDupServer(String server) {
            Log.e("IpManagerCallback", "[1131400] onFindDupServer");
            OppoClientModeImpl2.this.sendMessage(131473, server);
        }

        public void onUpdateLeaseExpriy(long time) {
            Log.e("IpManagerCallback", "[1131400] dhcpLeaseExpiry");
            OppoClientModeImpl2.this.sendMessage(131474, new Long(time));
        }

        public void onSwitchServerFailure(String server) {
            Log.e("IpManagerCallback", "[1131400] onSwitchDhcpFailure.");
            OppoClientModeImpl2.this.sendMessage(131475, server);
        }

        public void onFixServerFailure(String server) {
            Log.e("IpManagerCallback", "[1131400] onSwitchDhcpFailure.");
            OppoClientModeImpl2.this.sendMessage(131476, server);
        }

        public void onDhcpRenewCount() {
            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
            oppoClientModeImpl2.logd("handleDhcpRenew mScreenOn=" + OppoClientModeImpl2.this.mScreenOn + ",mIdleRenewTimes :" + OppoClientModeImpl2.this.mIdleRenewTimes);
            if (!OppoClientModeImpl2.this.mScreenOn) {
                OppoClientModeImpl2.access$908(OppoClientModeImpl2.this);
            }
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
    /* access modifiers changed from: public */
    private void stopIpClient() {
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
        WifiP2pServiceImpl wifiP2pServiceImpl2 = this.wifiP2pServiceImpl;
        if (wifiP2pServiceImpl2 != null) {
            wifiP2pServiceImpl2.enableVerboseLogging(verbose);
        }
        this.mWifiMonitor.enableVerboseLogging(verbose);
        this.mWifiNative.enableVerboseLogging(verbose);
        this.mWifiConfigManager.enableVerboseLogging(verbose);
        this.mSupplicantStateTracker.enableVerboseLogging(verbose);
        this.mPasspointManager.enableVerboseLogging(verbose);
        this.mNetworkFactory.enableVerboseLogging(verbose);
        this.mLinkProbeManager.enableVerboseLogging(this.mVerboseLoggingEnabled);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(verbose);
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().enableVerboseLogging(verbose);
        }
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.enableVerboseLogging(verbose);
        }
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    private boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039777);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = {(byte) (Integer.parseInt(ouiParts[0], 16) & 255), (byte) (Integer.parseInt(ouiParts[1], 16) & 255), (byte) (Integer.parseInt(ouiParts[2], 16) & 255)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, ouiBytes);
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
    /* access modifiers changed from: public */
    private int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
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
    /* access modifiers changed from: public */
    private int stopWifiIPPacketOffload(int slot) {
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
    /* access modifiers changed from: public */
    private int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring(this.mInterfaceName);
    }

    public void setWifiStateForApiCalls(int newState) {
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.updateWifiState(newState);
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
            Iterator<ScanResult> it = this.mScanRequestProxy.getScanResults().iterator();
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
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2;
        if (this.mVerboseLoggingEnabled) {
            log("setting operational mode to " + String.valueOf(mode) + " for iface: " + ifaceName);
        }
        this.mModeChange = true;
        if (mode != 1) {
            clearDisable();
            transitionTo(this.mDefaultState);
        } else if (ifaceName != null) {
            this.mInterfaceName = ifaceName;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker22 = this.mWifiNetworkStateTraker2;
            if (oppoWifiAssistantStateTraker22 != null) {
                oppoWifiAssistantStateTraker22.setInterfaceName(ifaceName);
            } else if (oppoWifiAssistantStateTraker22 != null) {
                oppoWifiAssistantStateTraker22.readWifiNetworkRecord();
                if (this.mAutoSwitch) {
                    this.mWifiNetworkStateTraker2.disableNetworkWithoutInternet();
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - this.mLastScanTime <= 5000 && (oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2) != null) {
                        oppoWifiAssistantStateTraker2.detectScanResult(currentTime);
                    }
                }
            }
            this.mWifiConfigManager.enableAllNetworks();
            transitionTo(this.mDisconnectedState);
        } else {
            Log.e(TAG, "supposed to enter connect mode, but iface is null -> DefaultState");
            clearDisable();
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
            return new Network(this.mNetworkAgent.netId);
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
        OppoClientModeImpl2.super.dump(fd, pw, args);
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
    /* access modifiers changed from: public */
    private void logStateAndMessage(Message message, State state) {
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
            case CMD_ADD_OR_UPDATE_NETWORK /* 131124 */:
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
            case CMD_ENABLE_NETWORK /* 131126 */:
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
            case CMD_GET_CONFIGURED_NETWORKS /* 131131 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=");
                sb.append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_RSSI_POLL /* 131155 */:
            case CMD_ONESHOT_RSSI_POLL /* 131156 */:
            case CMD_UNWANTED_NETWORK /* 131216 */:
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
            case CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mRoamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mDisconnectingWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /* 131211 */:
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
            case CMD_UPDATE_LINKPROPERTIES /* 131212 */:
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
            case CMD_TARGET_BSSID /* 131213 */:
            case CMD_ASSOCIATED_BSSID /* 131219 */:
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
            case CMD_START_CONNECT /* 131215 */:
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
            case CMD_START_ROAM /* 131217 */:
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
            case CMD_IP_REACHABILITY_LOST /* 131221 */:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
            case CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                sb.append(" ");
                sb.append(msg.obj);
                break;
            case CMD_INSTALL_PACKET_FILTER /* 131274 */:
                sb.append(" len=" + ((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                sb.append(" enabled=" + ((Boolean) msg.obj).booleanValue());
                break;
            case CMD_USER_SWITCH /* 131277 */:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_PRE_DHCP_ACTION /* 131327 */:
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
            case CMD_POST_DHCP_ACTION /* 131329 */:
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
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
            case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
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
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
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
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
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
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
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
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /* 143361 */:
                return "GROUP_CREATING_TIMED_OUT";
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                return "P2P_CONNECTION_CHANGED";
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                return "DISCONNECT_WIFI_REQUEST";
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /* 143373 */:
                return "DISCONNECT_WIFI_RESPONSE";
            case WifiP2pServiceImpl.SET_MIRACAST_MODE /* 143374 */:
                return "SET_MIRACAST_MODE";
            case WifiP2pServiceImpl.BLOCK_DISCOVERY /* 143375 */:
                return "BLOCK_DISCOVERY";
            case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                return "NETWORK_CONNECTION_EVENT";
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                return "NETWORK_DISCONNECTION_EVENT";
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                return "SUPPLICANT_STATE_CHANGE_EVENT";
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                return "AUTHENTICATION_FAILURE_EVENT";
            case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                return "SUP_REQUEST_IDENTITY";
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                return "ASSOCIATION_REJECTION_EVENT";
            case WifiMonitor.ANQP_DONE_EVENT /* 147500 */:
                return "ANQP_DONE_EVENT";
            case WifiMonitor.GAS_QUERY_START_EVENT /* 147507 */:
                return "GAS_QUERY_START_EVENT";
            case WifiMonitor.GAS_QUERY_DONE_EVENT /* 147508 */:
                return "GAS_QUERY_DONE_EVENT";
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /* 147509 */:
                return "RX_HS20_ANQP_ICON_EVENT";
            case WifiMonitor.HS20_REMEDIATION_EVENT /* 147517 */:
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (this.mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
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
    /* access modifiers changed from: public */
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
                this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
    /* access modifiers changed from: public */
    private void fetchRssiLinkSpeedAndFrequencyNative() {
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
                        }
                    } else {
                        this.mRssiCount = 0;
                        sendRssiChangeBroadcast(newRssi);
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
    /* access modifiers changed from: public */
    private void cleanWifiScore() {
        ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
        extendedWifiInfo.txBadRate = 0.0d;
        extendedWifiInfo.txSuccessRate = 0.0d;
        extendedWifiInfo.txRetriesRate = 0.0d;
        extendedWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
        this.mLastLinkLayerStats = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLinkProperties(LinkProperties newLp) {
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
        getNetworkDetailedState();
        NetworkInfo.DetailedState detailedState = NetworkInfo.DetailedState.CONNECTED;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        StatsLog.write(38, WifiManager.calculateSignalLevel(newRssi, 5));
        Intent intent = new Intent("android.net.wifi2.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkStateChangeBroadcast(String bssid) {
        if (this.mOppoDhcpRecord.isDoingSwitch()) {
            if (this.mVerboseLoggingEnabled) {
                log("[1131400] Doing Dhcp Retry. not bc. new state" + this.mNetworkInfo.getDetailedState());
            }
        } else if (this.mWifiNetworkStateTraker2 == null || !this.mAutoSwitch || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.IDLE || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.SCANNING || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.FAILED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.BLOCKED || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            Intent intent = new Intent("android.net.wifi2.STATE_CHANGE");
            boolean wlanAssistAutoConnect = false;
            boolean replacePending = false;
            OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
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
            intent.putExtra("iface_name", this.mInterfaceName);
            OppoClientModeManagerHelper.getInstance().intentPutExtraForDeepSleep(intent);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } else {
            log("state is " + this.mNetworkInfo.getDetailedState() + ",not bc.");
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
    /* access modifiers changed from: public */
    private boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
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
    /* access modifiers changed from: public */
    private SupplicantState handleSupplicantStateChange(Message message) {
        String tmpSsid;
        ScanDetail scanDetail;
        ExtendedWifiInfo extendedWifiInfo;
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        if (this.mPowerState == SupplicantState.COMPLETED && state == SupplicantState.GROUP_HANDSHAKE && !this.mScreenOn) {
            this.mIdleGroupTimes++;
        }
        this.mPowerState = state;
        SupplicantState supplicantState = this.mLastSupplicantState;
        if (supplicantState != state) {
            if (supplicantState == SupplicantState.COMPLETED || state == SupplicantState.COMPLETED) {
                removeMessages(CMD_TRIGGER_RESTORE_DELAY);
            }
            this.mLastSupplicantState = state;
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
        String ssidStr = null;
        ExtendedWifiInfo extendedWifiInfo2 = this.mWifiInfo;
        if (extendedWifiInfo2 != null) {
            ssidStr = extendedWifiInfo2.getSSID();
        }
        if (SupplicantState.isConnecting(state) && (ssidStr == null || ssidStr.equals("<unknown ssid>"))) {
            WifiConfiguration wc = null;
            WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
            if (!(wifiConfigManager == null || (extendedWifiInfo = this.mWifiInfo) == null)) {
                wc = wifiConfigManager.getConfiguredNetwork(extendedWifiInfo.getNetworkId());
            }
            if (!(wc == null || wc.SSID == null)) {
                WifiSsid ssid = null;
                String configSsidStr = wc.SSID;
                if (configSsidStr != null && configSsidStr.startsWith("\"") && configSsidStr.endsWith("\"")) {
                    configSsidStr = configSsidStr.substring(1, configSsidStr.length() - 1);
                }
                if (configSsidStr != null) {
                    ssid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(NativeUtil.stringToByteArrayList(configSsidStr)));
                }
                if (ssid != null) {
                    logd("reset wifissid to" + ssid.toString());
                    this.mWifiInfo.setSSID(ssid);
                }
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
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.sendMessage(message.what, message.arg1, message.arg2, message.obj);
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
    /* access modifiers changed from: public */
    private void handleNetworkDisconnect() {
        if (this.mVerboseLoggingEnabled) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (wifiConfig != null) {
            this.mWifiInjector.getWakeupController().setLastDisconnectInfo(ScanResultMatchInfo.fromWifiConfiguration(wifiConfig));
            this.mWifiNetworkSuggestionsManager.handleDisconnect(wifiConfig, getCurrentBSSID());
        }
        if (!(this.mWifiNetworkStateTraker2 == null || this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED)) {
            this.mWifiNetworkStateTraker2.setNetworkDetailState(this.mLastNetworkId, NetworkInfo.DetailedState.DISCONNECTED, this.mLastBssid);
        }
        this.mNetworkDetectValid = false;
        stopRssiMonitoringOffload();
        clearTargetBssid("handleNetworkDisconnect");
        if (!this.mScreenOn) {
            this.mIdleDisConnTimes++;
        }
        stopIpClient();
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mIsAutoRoaming = false;
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
        updateL2KeyAndGroupHint();
        this.mConnectedId = -1;
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
    /* access modifiers changed from: public */
    private void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mWifiDiagnostics.reportConnectionEvent((byte) 0);
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
    /* access modifiers changed from: public */
    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode, int level2FailureReason) {
        WifiConfiguration configuration = getCurrentWifiConfiguration();
        if (configuration == null) {
            configuration = getTargetWifiConfiguration();
        }
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode, level2FailureReason);
        if (configuration != null) {
            this.mNetworkFactory.handleConnectionAttemptEnded(level2FailureCode, configuration);
            this.mWifiNetworkSuggestionsManager.handleConnectionAttemptEnded(level2FailureCode, configuration, getCurrentBSSID());
        }
        handleConnectionAttemptEndForDiagnostics(level2FailureCode);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIPv4Success(DhcpResults dhcpResults) {
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
    /* access modifiers changed from: public */
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
    /* access modifiers changed from: public */
    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.sendMessage(CMD_IP_CONFIGURATION_LOST);
        }
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIpReachabilityLost() {
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
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e3) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
        return macAddress;
    }

    private boolean isPermanentWrongPasswordFailure(int networkId, int reasonCode) {
        if (reasonCode != 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void registerNetworkFactory() {
        if (checkAndSetConnectivityInstance()) {
            this.mNetworkFactory.setScoreFilter(5);
            this.mNetworkFactory.register();
            this.mUntrustedNetworkFactory.register();
        }
    }

    private void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.wifiP2pServiceImpl = IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.wifiP2pServiceImpl != null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.wifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void configureRandomizedMacAddress(WifiConfiguration config) {
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
    /* access modifiers changed from: public */
    private void setCurrentMacToFactoryMac(WifiConfiguration config) {
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

        public void enter() {
            Log.d(OppoClientModeImpl2.TAG, "entering DefaultState: ifaceName = " + OppoClientModeImpl2.this.mInterfaceName);
            OppoClientModeImpl2.this.clearDisable();
        }

        public boolean processMessage(Message message) {
            int removeResult = -1;
            boolean disableOthers = false;
            switch (message.what) {
                case 0:
                    Log.wtf(OppoClientModeImpl2.TAG, "Error! empty message encountered");
                    break;
                case 69632:
                    if (((AsyncChannel) message.obj) == OppoClientModeImpl2.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            OppoClientModeImpl2.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        } else {
                            OppoClientModeImpl2.this.p2pSendMessage(69633);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.loge("got HALF_CONNECTED for unknown channel");
                        break;
                    }
                case 69636:
                    if (((AsyncChannel) message.obj) == OppoClientModeImpl2.this.mWifiP2pChannel) {
                        OppoClientModeImpl2.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    oppoClientModeImpl2.mBluetoothConnectionActive = disableOthers;
                    break;
                case OppoClientModeImpl2.CMD_ENABLE_NETWORK /* 131126 */:
                    if (message.arg2 == 1) {
                        disableOthers = true;
                    }
                    boolean ok = OppoClientModeImpl2.this.mWifiConfigManager.enableNetwork(message.arg1, disableOthers, message.sendingUid);
                    if (!ok) {
                        OppoClientModeImpl2.this.mMessageHandlingStatus = -2;
                    }
                    OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                    int i = message.what;
                    if (ok) {
                        removeResult = 1;
                    }
                    oppoClientModeImpl22.replyToMessage((OppoClientModeImpl2) message, (Message) i, removeResult);
                    break;
                case OppoClientModeImpl2.CMD_GET_CONFIGURED_NETWORKS /* 131131 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mWifiConfigManager.getSavedNetworks(message.arg2));
                    break;
                case OppoClientModeImpl2.CMD_GET_SUPPORTED_FEATURES /* 131133 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) Long.valueOf(OppoClientModeImpl2.this.mWifiNative.getSupportedFeatureSet(OppoClientModeImpl2.this.mInterfaceName)));
                    break;
                case OppoClientModeImpl2.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /* 131134 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case OppoClientModeImpl2.CMD_GET_LINK_LAYER_STATS /* 131135 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) null);
                    break;
                case OppoClientModeImpl2.CMD_SET_OPERATIONAL_MODE /* 131144 */:
                    break;
                case OppoClientModeImpl2.CMD_DISCONNECT /* 131145 */:
                case OppoClientModeImpl2.CMD_RECONNECT /* 131146 */:
                case OppoClientModeImpl2.CMD_REASSOCIATE /* 131147 */:
                case OppoClientModeImpl2.CMD_RSSI_POLL /* 131155 */:
                case OppoClientModeImpl2.CMD_ONESHOT_RSSI_POLL /* 131156 */:
                case OppoClientModeImpl2.CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                case OppoClientModeImpl2.CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                case OppoClientModeImpl2.CMD_DISABLE_EPHEMERAL_NETWORK /* 131170 */:
                case OppoClientModeImpl2.CMD_TARGET_BSSID /* 131213 */:
                case OppoClientModeImpl2.CMD_START_CONNECT /* 131215 */:
                case OppoClientModeImpl2.CMD_UNWANTED_NETWORK /* 131216 */:
                case OppoClientModeImpl2.CMD_START_ROAM /* 131217 */:
                case OppoClientModeImpl2.CMD_ASSOCIATED_BSSID /* 131219 */:
                case OppoClientModeImpl2.CMD_PRE_DHCP_ACTION /* 131327 */:
                case OppoClientModeImpl2.CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                case OppoClientModeImpl2.CMD_POST_DHCP_ACTION /* 131329 */:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /* 147472 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.setSuspendOptimizations(2, true);
                        break;
                    } else {
                        OppoClientModeImpl2.this.setSuspendOptimizations(2, false);
                        break;
                    }
                case OppoClientModeImpl2.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    oppoClientModeImpl23.mEnableRssiPolling = disableOthers;
                    break;
                case OppoClientModeImpl2.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.setSuspendOptimizations(4, false);
                        break;
                    } else {
                        if (message.arg2 == 1) {
                            OppoClientModeImpl2.this.mSuspendWakeLock.release();
                        }
                        OppoClientModeImpl2.this.setSuspendOptimizations(4, true);
                        break;
                    }
                case OppoClientModeImpl2.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    oppoClientModeImpl24.handleScreenStateChanged(disableOthers);
                    break;
                case OppoClientModeImpl2.CMD_REMOVE_APP_CONFIGURATIONS /* 131169 */:
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                case OppoClientModeImpl2.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = OppoClientModeImpl2.MESSAGE_HANDLING_STATUS_DEFERRED;
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                case OppoClientModeImpl2.CMD_QUERY_OSU_ICON /* 131176 */:
                case OppoClientModeImpl2.CMD_MATCH_PROVIDER_NETWORK /* 131177 */:
                    OppoClientModeImpl2.this.replyToMessage(message, message.what);
                    break;
                case OppoClientModeImpl2.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /* 131178 */:
                    Bundle bundle = (Bundle) message.obj;
                    if (OppoClientModeImpl2.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) bundle.getParcelable(OppoClientModeImpl2.EXTRA_PASSPOINT_CONFIGURATION), bundle.getInt(OppoClientModeImpl2.EXTRA_UID), bundle.getString(OppoClientModeImpl2.EXTRA_PACKAGE_NAME))) {
                        removeResult = 1;
                    }
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, removeResult);
                    break;
                case OppoClientModeImpl2.CMD_REMOVE_PASSPOINT_CONFIG /* 131179 */:
                    if (OppoClientModeImpl2.this.mPasspointManager.removeProvider((String) message.obj)) {
                        removeResult = 1;
                    }
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, removeResult);
                    break;
                case OppoClientModeImpl2.CMD_GET_PASSPOINT_CONFIGS /* 131180 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mPasspointManager.getProviderConfigs());
                    break;
                case OppoClientModeImpl2.CMD_GET_MATCHING_OSU_PROVIDERS /* 131181 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) new HashMap());
                    break;
                case OppoClientModeImpl2.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /* 131182 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) new HashMap());
                    break;
                case OppoClientModeImpl2.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /* 131184 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) new ArrayList());
                    break;
                case OppoClientModeImpl2.CMD_BOOT_COMPLETED /* 131206 */:
                    OppoClientModeImpl2.this.registerNetworkFactory();
                    break;
                case OppoClientModeImpl2.CMD_INITIALIZE /* 131207 */:
                    boolean ok2 = OppoClientModeImpl2.this.mWifiNative.initialize();
                    OppoClientModeImpl2.this.mPasspointManager.initializeProvisioner(OppoClientModeImpl2.this.mWifiInjector.getWifiServiceHandlerThread().getLooper());
                    OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                    int i2 = message.what;
                    if (ok2) {
                        removeResult = 1;
                    }
                    oppoClientModeImpl25.replyToMessage((OppoClientModeImpl2) message, (Message) i2, removeResult);
                    break;
                case OppoClientModeImpl2.CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                case OppoClientModeImpl2.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                case OppoClientModeImpl2.CMD_IP_REACHABILITY_LOST /* 131221 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_UPDATE_LINKPROPERTIES /* 131212 */:
                    OppoClientModeImpl2.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case OppoClientModeImpl2.CMD_REMOVE_USER_CONFIGURATIONS /* 131224 */:
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                case OppoClientModeImpl2.CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                case OppoClientModeImpl2.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                case OppoClientModeImpl2.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /* 131281 */:
                case OppoClientModeImpl2.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /* 131282 */:
                    if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                        OppoClientModeImpl2.this.mNetworkAgent.onSocketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /* 131240 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) new HashMap());
                    break;
                case OppoClientModeImpl2.CMD_INSTALL_PACKET_FILTER /* 131274 */:
                    OppoClientModeImpl2.this.mWifiNative.installPacketFilter(OppoClientModeImpl2.this.mInterfaceName, (byte[]) message.obj);
                    break;
                case OppoClientModeImpl2.CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                    if (!((Boolean) message.obj).booleanValue()) {
                        OppoClientModeImpl2.this.mWifiNative.stopFilteringMulticastV4Packets(OppoClientModeImpl2.this.mInterfaceName);
                        break;
                    } else {
                        OppoClientModeImpl2.this.mWifiNative.startFilteringMulticastV4Packets(OppoClientModeImpl2.this.mInterfaceName);
                        break;
                    }
                case OppoClientModeImpl2.CMD_USER_SWITCH /* 131277 */:
                    Set<Integer> removedNetworkIds = OppoClientModeImpl2.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(Integer.valueOf(OppoClientModeImpl2.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(OppoClientModeImpl2.this.mLastNetworkId))) {
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        break;
                    }
                case OppoClientModeImpl2.CMD_USER_UNLOCK /* 131278 */:
                    OppoClientModeImpl2.this.mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case OppoClientModeImpl2.CMD_USER_STOP /* 131279 */:
                    OppoClientModeImpl2.this.mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case OppoClientModeImpl2.CMD_READ_PACKET_FILTER /* 131280 */:
                    byte[] data = OppoClientModeImpl2.this.mWifiNative.readPacketFilter(OppoClientModeImpl2.this.mInterfaceName);
                    if (OppoClientModeImpl2.this.mIpClient != null) {
                        OppoClientModeImpl2.this.mIpClient.readPacketFilterComplete(data);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_DIAGS_CONNECT_TIMEOUT /* 131324 */:
                    OppoClientModeImpl2.this.mWifiDiagnostics.reportConnectionEvent((byte) 3);
                    break;
                case OppoClientModeImpl2.CMD_START_SUBSCRIPTION_PROVISIONING /* 131326 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, 0);
                    break;
                case OppoClientModeImpl2.CMD_START_SCAN /* 131479 */:
                    String str = (String) message.obj;
                    if (OppoClientModeImpl2.this.isBootCompleted()) {
                        OppoClientModeImpl2.this.mScanRequestProxy.startScan(message.arg1, (String) message.obj);
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                    OppoClientModeImpl2.this.mP2pConnected.set(((NetworkInfo) message.obj).isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    oppoClientModeImpl26.mTemporarilyDisconnectWifi = disableOthers;
                    OppoClientModeImpl2.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case 151569:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151570, 2);
                    break;
                case 151572:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151574, 2);
                    break;
                default:
                    OppoClientModeImpl2.this.loge("Error! unhandled message" + message);
                    break;
            }
            if (1 == 1) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setupClientMode() {
        Log.d(TAG, "setupClientMode() ifacename = " + this.mInterfaceName);
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
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get());
        setPowerSave(true);
        this.mWifiNative.enableStaAutoReconnect(this.mInterfaceName, false);
        this.mWifiNative.setConcurrencyPriority(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopClientMode() {
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
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.setInterfaceName(null);
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
            Log.d(OppoClientModeImpl2.TAG, "entering ConnectModeState: ifaceName = " + OppoClientModeImpl2.this.mInterfaceName);
            if (OppoClientModeImpl2.this.mInterfaceName == null) {
                OppoClientModeImpl2.this.mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN1;
                if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                    OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setInterfaceName(OppoClientModeImpl2.this.mInterfaceName);
                }
            }
            OppoClientModeImpl2.this.mOperationalMode = 1;
            OppoClientModeImpl2.this.setupClientMode();
            if (!OppoClientModeImpl2.this.mWifiNative.removeAllNetworks(OppoClientModeImpl2.this.mInterfaceName)) {
                OppoClientModeImpl2.this.loge("Failed to remove networks on entering connect mode");
            }
            OppoClientModeImpl2.this.mWifiInfo.reset();
            OppoClientModeImpl2.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            OppoClientModeImpl2.this.mWifiInjector.getWakeupController().reset();
            OppoClientModeImpl2.this.mNetworkInfo.setIsAvailable(true);
            if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                OppoClientModeImpl2.this.mNetworkAgent.sendNetworkInfo(OppoClientModeImpl2.this.mNetworkInfo);
            }
            OppoClientModeImpl2.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            OppoClientModeImpl2.this.mNetworkFactory.setWifiState(true);
            OppoClientModeImpl2.this.mWifiMetrics.setWifiState(2);
            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(18);
            OppoClientModeImpl2.this.mSarManager.setClientWifiState(3);
            OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            OppoClientModeImpl2.this.logd("Enter ConnectModeState - setSwitchState to SwitchState.INIT");
        }

        public void exit() {
            OppoClientModeImpl2.this.mOperationalMode = 4;
            OppoClientModeImpl2.this.mNetworkInfo.setIsAvailable(false);
            if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                OppoClientModeImpl2.this.mNetworkAgent.sendNetworkInfo(OppoClientModeImpl2.this.mNetworkInfo);
            }
            OppoClientModeImpl2.this.mNetworkFactory.setWifiState(false);
            OppoClientModeImpl2.this.mWifiMetrics.setWifiState(1);
            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(19);
            OppoClientModeImpl2.this.mSarManager.setClientWifiState(1);
            if (!OppoClientModeImpl2.this.mWifiNative.removeAllNetworks(OppoClientModeImpl2.this.mInterfaceName)) {
                OppoClientModeImpl2.this.loge("Failed to remove networks on exiting connect mode");
            }
            OppoClientModeImpl2.this.mWifiInfo.reset();
            OppoClientModeImpl2.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            OppoClientModeImpl2.this.stopClientMode();
        }

        /* JADX INFO: Multiple debug info for r5v6 int: [D('stats' com.android.server.wifi.WifiLinkLayerStats), D('netId' int)] */
        /* JADX INFO: Multiple debug info for r5v90 int: [D('requestData' com.android.server.wifi.util.TelephonyUtil$SimAuthRequestData), D('netId' int)] */
        /* JADX WARNING: Code restructure failed: missing block: B:245:0x08da, code lost:
            if (r6.isProviderOwnedNetwork(r6.mLastNetworkId, r5) != false) goto L_0x08dc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:254:0x0946, code lost:
            if (r8.isProviderOwnedNetwork(r8.mLastNetworkId, r7) != false) goto L_0x0948;
         */
        public boolean processMessage(Message message) {
            int uid;
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            int level2FailureReason;
            int i;
            boolean handleStatus = true;
            if (MtkWifiServiceAdapter.preProcessMessage(this, message)) {
                return true;
            }
            switch (message.what) {
                case OppoClientModeImpl2.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    OppoClientModeImpl2.this.mBluetoothConnectionActive = message.arg1 != 0;
                    OppoClientModeImpl2.this.mWifiNative.setBluetoothCoexistenceScanMode(OppoClientModeImpl2.this.mInterfaceName, OppoClientModeImpl2.this.mBluetoothConnectionActive);
                    break;
                case OppoClientModeImpl2.CMD_REMOVE_NETWORK /* 131125 */:
                    int netId = message.arg1;
                    if (netId == OppoClientModeImpl2.this.mTargetNetworkId || netId == OppoClientModeImpl2.this.mLastNetworkId) {
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        break;
                    }
                case OppoClientModeImpl2.CMD_GET_LINK_LAYER_STATS /* 131135 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.getWifiLinkLayerStats());
                    break;
                case OppoClientModeImpl2.CMD_REASSOCIATE /* 131147 */:
                    OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                    oppoClientModeImpl2.mLastConnectAttemptTimestamp = oppoClientModeImpl2.mClock.getWallClockMillis();
                    OppoClientModeImpl2.this.mWifiNative.reassociate(OppoClientModeImpl2.this.mInterfaceName);
                    break;
                case OppoClientModeImpl2.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.setSuspendOptimizationsNative(2, true);
                        break;
                    } else {
                        OppoClientModeImpl2.this.setSuspendOptimizationsNative(2, false);
                        break;
                    }
                case OppoClientModeImpl2.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.setSuspendOptimizationsNative(4, false);
                        break;
                    } else {
                        OppoClientModeImpl2.this.setSuspendOptimizationsNative(4, true);
                        if (message.arg2 == 1) {
                            OppoClientModeImpl2.this.mSuspendWakeLock.release();
                            break;
                        }
                    }
                    break;
                case OppoClientModeImpl2.CMD_ENABLE_TDLS /* 131164 */:
                    if (message.obj != null) {
                        OppoClientModeImpl2.this.mWifiNative.startTdls(OppoClientModeImpl2.this.mInterfaceName, (String) message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_DISABLE_EPHEMERAL_NETWORK /* 131170 */:
                    WifiConfiguration config = OppoClientModeImpl2.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null && (config.networkId == OppoClientModeImpl2.this.mTargetNetworkId || config.networkId == OppoClientModeImpl2.this.mLastNetworkId)) {
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        break;
                    }
                case OppoClientModeImpl2.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    OppoClientModeImpl2.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    boolean simPresent = message.arg1 == 1;
                    if (!simPresent) {
                        OppoClientModeImpl2.this.mPasspointManager.removeEphemeralProviders();
                    }
                    OppoClientModeImpl2.this.mWifiConfigManager.resetSimNetworks(simPresent, message.arg2);
                    break;
                case OppoClientModeImpl2.CMD_QUERY_OSU_ICON /* 131176 */:
                    OppoClientModeImpl2.this.mPasspointManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString(OppoClientModeImpl2.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case OppoClientModeImpl2.CMD_MATCH_PROVIDER_NETWORK /* 131177 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, 0);
                    break;
                case OppoClientModeImpl2.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /* 131178 */:
                    Bundle bundle = (Bundle) message.obj;
                    PasspointConfiguration passpointConfig = (PasspointConfiguration) bundle.getParcelable(OppoClientModeImpl2.EXTRA_PASSPOINT_CONFIGURATION);
                    if (!OppoClientModeImpl2.this.mPasspointManager.addOrUpdateProvider(passpointConfig, bundle.getInt(OppoClientModeImpl2.EXTRA_UID), bundle.getString(OppoClientModeImpl2.EXTRA_PACKAGE_NAME))) {
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, -1);
                        break;
                    } else {
                        String fqdn = passpointConfig.getHomeSp().getFqdn();
                        OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                        if (!oppoClientModeImpl22.isProviderOwnedNetwork(oppoClientModeImpl22.mTargetNetworkId, fqdn)) {
                            OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                            break;
                        }
                        OppoClientModeImpl2.this.logd("Disconnect from current network since its provider is updated");
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, 1);
                        break;
                    }
                case OppoClientModeImpl2.CMD_REMOVE_PASSPOINT_CONFIG /* 131179 */:
                    String fqdn2 = (String) message.obj;
                    if (!OppoClientModeImpl2.this.mPasspointManager.removeProvider(fqdn2)) {
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, -1);
                        break;
                    } else {
                        OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                        if (!oppoClientModeImpl24.isProviderOwnedNetwork(oppoClientModeImpl24.mTargetNetworkId, fqdn2)) {
                            OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                            break;
                        }
                        OppoClientModeImpl2.this.logd("Disconnect from current network since its provider is removed");
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        OppoClientModeImpl2.this.mWifiConfigManager.removePasspointConfiguredNetwork(fqdn2);
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, 1);
                        break;
                    }
                case OppoClientModeImpl2.CMD_GET_MATCHING_OSU_PROVIDERS /* 131181 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mPasspointManager.getMatchingOsuProviders((List) message.obj));
                    break;
                case OppoClientModeImpl2.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /* 131182 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mPasspointManager.getMatchingPasspointConfigsForOsuProviders((List) message.obj));
                    break;
                case OppoClientModeImpl2.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /* 131184 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mPasspointManager.getWifiConfigsForPasspointProfiles((List) message.obj));
                    break;
                case OppoClientModeImpl2.CMD_TARGET_BSSID /* 131213 */:
                    if (message.obj != null) {
                        OppoClientModeImpl2.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_START_CONNECT /* 131215 */:
                    int netId2 = message.arg1;
                    int uid2 = message.arg2;
                    String bssid = (String) message.obj;
                    WifiConfiguration config2 = OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId2);
                    OppoClientModeImpl2.this.logd("CMD_START_CONNECT sup state " + OppoClientModeImpl2.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + OppoClientModeImpl2.this.getCurrentState().getName() + " nid=" + Integer.toString(netId2) + " roam=" + Boolean.toString(OppoClientModeImpl2.this.mIsAutoRoaming));
                    if (config2 != null) {
                        Long disableconnectduring = (Long) OppoClientModeImpl2.this.mDisableConnectDuring.get(config2.configKey());
                        if (disableconnectduring != null && disableconnectduring.longValue() > 0) {
                            uid = uid2;
                            if (System.currentTimeMillis() - disableconnectduring.longValue() < ((long) OppoClientModeImpl2.this.getRomUpdateIntegerValue("OPPO_DUAL_STA_DISABLE_DURING", 3600000).intValue())) {
                                Log.d(OppoClientModeImpl2.TAG, "wlan1 is disable connect,just return");
                                break;
                            }
                        } else {
                            uid = uid2;
                        }
                        if (disableconnectduring != null && disableconnectduring.longValue() > 0 && System.currentTimeMillis() - disableconnectduring.longValue() >= ((long) OppoClientModeImpl2.this.getRomUpdateIntegerValue("OPPO_DUAL_STA_DISABLE_DURING", 3600000).intValue())) {
                            OppoClientModeImpl2.this.clearDisable();
                        }
                        ClientModeImpl.convertToQuotedSSID(config2);
                        OppoClientModeImpl2.this.mTargetNetworkId = netId2;
                        String tmpBssid = OppoClientModeImpl2.this.getBestBssidForNetId(netId2);
                        if (tmpBssid != null) {
                            Boolean bssidTmpReset = false;
                            if ("any".equals(bssid)) {
                                if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                                    OppoClientModeImpl2.this.logd("config bssid = " + config2.BSSID);
                                }
                                if (config2.BSSID == null || "any".equals(config2.BSSID)) {
                                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                                        OppoClientModeImpl2.this.logd("reset config bssid to " + tmpBssid + "  temp");
                                    }
                                    config2.BSSID = tmpBssid;
                                    bssidTmpReset = true;
                                }
                            }
                            if (bssidTmpReset.booleanValue()) {
                                OppoClientModeImpl2.this.setTargetBssid(config2, tmpBssid);
                            } else {
                                OppoClientModeImpl2.this.setTargetBssid(config2, bssid);
                            }
                            OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                            oppoClientModeImpl26.reportConnectionAttemptStart(config2, oppoClientModeImpl26.mTargetRoamBSSID, 5);
                            if (config2.macRandomizationSetting != 1 || !OppoClientModeImpl2.this.mConnectedMacRandomzationSupported) {
                                OppoClientModeImpl2.this.setCurrentMacToFactoryMac(config2);
                            } else {
                                OppoClientModeImpl2.this.configureRandomizedMacAddress(config2);
                            }
                            String currentMacAddress = OppoClientModeImpl2.this.mWifiNative.getMacAddress(OppoClientModeImpl2.this.mInterfaceName);
                            OppoClientModeImpl2.this.mWifiInfo.setMacAddress(currentMacAddress);
                            Log.i(OppoClientModeImpl2.TAG, "Connecting with " + currentMacAddress + " as the mac address");
                            if (config2.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config2.enterpriseConfig.getEapMethod()) && OppoClientModeImpl2.this.mWifiInjector.getCarrierNetworkConfig().isCarrierEncryptionInfoAvailable() && TextUtils.isEmpty(config2.enterpriseConfig.getAnonymousIdentity())) {
                                config2.enterpriseConfig.setAnonymousIdentity(TelephonyUtil.getAnonymousIdentityWith3GppRealm(OppoClientModeImpl2.this.getTelephonyManager()));
                            }
                            OppoClientModeImpl2.this.mOppoClientModeImplUtil.resetSaeNetworkConsecutiveAssocRejectCounter();
                            if (!OppoClientModeImpl2.this.mWifiNative.connectToNetwork(OppoClientModeImpl2.this.mInterfaceName, config2)) {
                                OppoClientModeImpl2.this.loge("CMD_START_CONNECT Failed to start connection to network " + config2);
                                OppoClientModeImpl2.this.reportConnectionAttemptEnd(5, 1, 0);
                                OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151554, 0);
                                break;
                            } else {
                                if (!OppoClientModeImpl2.this.isThirdApp(uid)) {
                                    OppoClientModeImpl2.this.clearTargetBssid("AfterConnect");
                                }
                                OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(11, config2);
                                OppoClientModeImpl2 oppoClientModeImpl27 = OppoClientModeImpl2.this;
                                oppoClientModeImpl27.mLastConnectAttemptTimestamp = oppoClientModeImpl27.mClock.getWallClockMillis();
                                OppoClientModeImpl2.this.mTargetWifiConfiguration = config2;
                                OppoClientModeImpl2.this.mIsAutoRoaming = false;
                                if (OppoClientModeImpl2.this.getCurrentState() != OppoClientModeImpl2.this.mDisconnectedState) {
                                    OppoClientModeImpl2 oppoClientModeImpl28 = OppoClientModeImpl2.this;
                                    oppoClientModeImpl28.transitionTo(oppoClientModeImpl28.mDisconnectingState);
                                    break;
                                }
                            }
                        } else {
                            OppoClientModeImpl2.this.logd("There is no different band bssid for wlan1");
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.loge("CMD_START_CONNECT and no config, bail out...");
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_START_ROAM /* 131217 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_ASSOCIATED_BSSID /* 131219 */:
                    String someBssid = (String) message.obj;
                    if (!(someBssid == null || (scanDetailCache = OppoClientModeImpl2.this.mWifiConfigManager.getScanDetailCacheForNetwork(OppoClientModeImpl2.this.mTargetNetworkId)) == null)) {
                        OppoClientModeImpl2.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                    }
                    handleStatus = false;
                    break;
                case OppoClientModeImpl2.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                    int slot = message.arg1;
                    int ret = OppoClientModeImpl2.this.stopWifiIPPacketOffload(slot);
                    if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                        OppoClientModeImpl2.this.mNetworkAgent.onSocketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /* 131240 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, (int) OppoClientModeImpl2.this.mPasspointManager.getAllMatchingFqdnsForScanResults((List) message.obj));
                    break;
                case OppoClientModeImpl2.CMD_CONFIG_ND_OFFLOAD /* 131276 */:
                    OppoClientModeImpl2.this.mWifiNative.configureNeighborDiscoveryOffload(OppoClientModeImpl2.this.mInterfaceName, message.arg1 > 0);
                    break;
                case OppoClientModeImpl2.CMD_START_SUBSCRIPTION_PROVISIONING /* 131326 */:
                    OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) message.what, OppoClientModeImpl2.this.mPasspointManager.startSubscriptionProvisioning(message.arg1, message.getData().getParcelable(OppoClientModeImpl2.EXTRA_OSU_PROVIDER), (IProvisioningCallback) message.obj) ? 1 : 0);
                    break;
                case OppoClientModeImpl2.CMD_TRIGGER_RESTORE_DELAY /* 131477 */:
                    int restoreNetid = message.arg1;
                    if (OppoClientModeImpl2.this.getCurrentState() != OppoClientModeImpl2.this.mConnectedState && OppoClientModeImpl2.this.getCurrentState() != OppoClientModeImpl2.this.mObtainingIpState) {
                        OppoClientModeImpl2.this.reportFoolProofException();
                        OppoClientModeImpl2.this.setStatistics("state_inconsistent", "wifi_state_inconsistent_cant_connect");
                        if (!OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName)) {
                            OppoClientModeImpl2.this.loge("fool-proof,Disconnect cmd reject by wpa,so restart");
                            OppoClientModeImpl2.this.sheduleRestartWifi(restoreNetid);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.loge("fool-proof, already connected,ignore");
                        break;
                    }
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.mWifiNative.reconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2.this.mTemporarilyDisconnectWifi = false;
                        break;
                    } else {
                        OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 5);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2.this.mTemporarilyDisconnectWifi = true;
                        break;
                    }
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2.this.log("Network connection established");
                    }
                    if (OppoClientModeImpl2.this.hasConfigKeyChanged(message.arg1)) {
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                    }
                    OppoClientModeImpl2.this.mLastNetworkId = message.arg1;
                    OppoClientModeImpl2.this.mWifiConfigManager.clearRecentFailureReason(OppoClientModeImpl2.this.mLastNetworkId);
                    OppoClientModeImpl2.this.mLastBssid = (String) message.obj;
                    int i2 = message.arg2;
                    if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, OppoClientModeImpl2.this.mLastNetworkId, message.arg2, OppoClientModeImpl2.this.mLastBssid);
                    }
                    WifiConfiguration currentConfig = OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetwork(OppoClientModeImpl2.this.mLastNetworkId);
                    if (OppoClientModeImpl2.this.mLastNetworkId == OppoClientModeImpl2.this.getManuConnectNetId() && OppoClientModeImpl2.this.isManuConnect() && currentConfig != null) {
                        OppoClientModeImpl2.this.log("Network connection established currentConfig.ssid" + currentConfig.SSID);
                        currentConfig.BSSID = OppoClientModeImpl2.this.mLastBssid;
                    }
                    WifiConfiguration config3 = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                    if (config3 == null) {
                        OppoClientModeImpl2.this.logw("Connected to unknown networkId " + OppoClientModeImpl2.this.mLastNetworkId + ", disconnecting...");
                        OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_DISCONNECT);
                        break;
                    } else {
                        OppoClientModeImpl2.this.mWifiInfo.setBSSID(OppoClientModeImpl2.this.mLastBssid);
                        OppoClientModeImpl2.this.mWifiInfo.setNetworkId(OppoClientModeImpl2.this.mLastNetworkId);
                        OppoClientModeImpl2.this.mWifiInfo.setMacAddress(OppoClientModeImpl2.this.mWifiNative.getMacAddress(OppoClientModeImpl2.this.mInterfaceName));
                        ScanDetailCache scanDetailCache2 = OppoClientModeImpl2.this.mWifiConfigManager.getScanDetailCacheForNetwork(config3.networkId);
                        if (!(scanDetailCache2 == null || OppoClientModeImpl2.this.mLastBssid == null || (scanResult = scanDetailCache2.getScanResult(OppoClientModeImpl2.this.mLastBssid)) == null)) {
                            OppoClientModeImpl2.this.mWifiInfo.setFrequency(scanResult.frequency);
                        }
                        if (config3.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config3.enterpriseConfig.getEapMethod()) && !TelephonyUtil.isAnonymousAtRealmIdentity(config3.enterpriseConfig.getAnonymousIdentity())) {
                            String anonymousIdentity = OppoClientModeImpl2.this.mWifiNative.getEapAnonymousIdentity(OppoClientModeImpl2.this.mInterfaceName);
                            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                                OppoClientModeImpl2.this.log("EAP Pseudonym: " + anonymousIdentity);
                            }
                            config3.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            OppoClientModeImpl2.this.mWifiConfigManager.addOrUpdateNetwork(config3, 1010);
                        }
                        OppoClientModeImpl2.this.mConnectionTimeStamp = System.currentTimeMillis();
                        OppoClientModeImpl2 oppoClientModeImpl29 = OppoClientModeImpl2.this;
                        oppoClientModeImpl29.sendNetworkStateChangeBroadcast(oppoClientModeImpl29.mLastBssid);
                        OppoClientModeImpl2 oppoClientModeImpl210 = OppoClientModeImpl2.this;
                        oppoClientModeImpl210.transitionTo(oppoClientModeImpl210.mObtainingIpState);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2.this.log("ConnectModeState: Network connection lost ");
                    }
                    OppoClientModeImpl2.this.handleNetworkDisconnect();
                    OppoClientModeImpl2 oppoClientModeImpl211 = OppoClientModeImpl2.this;
                    oppoClientModeImpl211.transitionTo(oppoClientModeImpl211.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    if (((StateChangeResult) message.obj) != null) {
                        SupplicantState state = OppoClientModeImpl2.this.handleSupplicantStateChange(message);
                        if (state == SupplicantState.DISCONNECTED && OppoClientModeImpl2.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                                OppoClientModeImpl2.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                            }
                            OppoClientModeImpl2.this.handleNetworkDisconnect();
                            OppoClientModeImpl2 oppoClientModeImpl212 = OppoClientModeImpl2.this;
                            oppoClientModeImpl212.transitionTo(oppoClientModeImpl212.mDisconnectedState);
                        }
                        if (state == SupplicantState.COMPLETED) {
                            if (OppoClientModeImpl2.this.mIpClient != null) {
                                OppoClientModeImpl2.this.mIpClient.confirmConfiguration();
                            }
                            OppoClientModeImpl2.this.mWifiScoreReport.noteIpCheck();
                            break;
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                    OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(2);
                    OppoClientModeImpl2.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int reasonCode = message.arg1;
                    String bssid2 = (String) message.obj;
                    if (bssid2 == null || TextUtils.isEmpty(bssid2)) {
                        bssid2 = OppoClientModeImpl2.this.mTargetRoamBSSID;
                    }
                    if (!OppoClientModeImpl2.this.attemptWpa2FallbackConnectionIfRequired(bssid2)) {
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                        }
                        if (reasonCode == 0) {
                            level2FailureReason = 1;
                        } else if (reasonCode == 1) {
                            level2FailureReason = 2;
                        } else if (reasonCode == 2) {
                            level2FailureReason = 3;
                        } else if (reasonCode != 3) {
                            level2FailureReason = 0;
                        } else {
                            level2FailureReason = 4;
                        }
                        OppoClientModeImpl2.this.reportConnectionAttemptEnd(3, 1, level2FailureReason);
                        if (reasonCode != 2) {
                            OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(OppoClientModeImpl2.this.getTargetSsid(), OppoClientModeImpl2.this.mTargetRoamBSSID, 2);
                            break;
                        }
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                    int netId3 = message.arg2;
                    boolean identitySent = false;
                    if (OppoClientModeImpl2.this.mTargetWifiConfiguration != null && OppoClientModeImpl2.this.mTargetWifiConfiguration.networkId == netId3 && TelephonyUtil.isSimConfig(OppoClientModeImpl2.this.mTargetWifiConfiguration)) {
                        Pair<String, String> identityPair = TelephonyUtil.getSimIdentity(OppoClientModeImpl2.this.getTelephonyManager(), OppoClientModeImpl2.this.getSubscriptionManager(), new TelephonyUtil(), OppoClientModeImpl2.this.mTargetWifiConfiguration, OppoClientModeImpl2.this.mWifiInjector.getCarrierNetworkConfig());
                        Log.i(OppoClientModeImpl2.TAG, "SUP_REQUEST_IDENTITY: identityPair=" + identityPair);
                        if (identityPair == null || identityPair.first == null) {
                            Log.e(OppoClientModeImpl2.TAG, "Unable to retrieve identity from Telephony");
                        } else {
                            OppoClientModeImpl2.this.mWifiNative.simIdentityResponse(OppoClientModeImpl2.this.mInterfaceName, netId3, (String) identityPair.first, (String) identityPair.second);
                            identitySent = true;
                        }
                    }
                    if (!identitySent) {
                        String ssid = (String) message.obj;
                        String quotedSsid = ClientModeImpl.convertToQuotedSSID(ssid);
                        if (!(OppoClientModeImpl2.this.mTargetWifiConfiguration == null || ssid == null || OppoClientModeImpl2.this.mTargetWifiConfiguration.SSID == null || !OppoClientModeImpl2.this.mTargetWifiConfiguration.SSID.equals(quotedSsid))) {
                            OppoClientModeImpl2.this.mWifiConfigManager.updateNetworkSelectionStatus(OppoClientModeImpl2.this.mTargetWifiConfiguration.networkId, 9);
                        }
                        OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /* 147472 */:
                    OppoClientModeImpl2.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    TelephonyUtil.SimAuthRequestData requestData = (TelephonyUtil.SimAuthRequestData) message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                OppoClientModeImpl2.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        } else {
                            OppoClientModeImpl2.this.handleGsmAuthRequest(requestData);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.loge("Invalid SIM auth request");
                        break;
                    }
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                    OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(1);
                    OppoClientModeImpl2.this.mDidBlackListBSSID = false;
                    String bssid3 = (String) message.obj;
                    boolean timedOut = message.arg1 > 0;
                    Log.d(OppoClientModeImpl2.TAG, "Association Rejection event: bssid=" + bssid3 + " reason code=" + message.arg2 + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid3 == null || TextUtils.isEmpty(bssid3)) {
                        bssid3 = OppoClientModeImpl2.this.mTargetRoamBSSID;
                    }
                    if (!OppoClientModeImpl2.this.attemptWpa2FallbackConnectionIfRequired(bssid3)) {
                        OppoClientModeImpl2.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT, message.arg1, message.arg2, message.obj);
                        }
                        OppoClientModeImpl2 oppoClientModeImpl213 = OppoClientModeImpl2.this;
                        if (timedOut) {
                            i = 11;
                        } else {
                            i = 2;
                        }
                        oppoClientModeImpl213.reportConnectionAttemptEnd(i, 1, 0);
                        OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(OppoClientModeImpl2.this.getTargetSsid(), bssid3, 1);
                        break;
                    }
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /* 147500 */:
                    OppoClientModeImpl2.this.mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /* 147509 */:
                    OppoClientModeImpl2.this.mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /* 147517 */:
                    OppoClientModeImpl2.this.mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            MtkWifiServiceAdapter.postProcessMessage(this, message, false, null);
            return handleStatus;
        }
    }

    private WifiNetworkAgentSpecifier createNetworkAgentSpecifier(WifiConfiguration currentWifiConfiguration, String currentBssid, int specificRequestUid, String specificRequestPackageName) {
        currentWifiConfiguration.BSSID = currentBssid;
        return new WifiNetworkAgentSpecifier(currentWifiConfiguration, specificRequestUid, specificRequestPackageName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkCapabilities getCapabilities(WifiConfiguration currentWifiConfiguration) {
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
    /* access modifiers changed from: public */
    private boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        WifiConfiguration config;
        if (networkId == -1 || (config = this.mWifiConfigManager.getConfiguredNetwork(networkId)) == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    private void handleEapAuthFailure(int networkId, int errorCode) {
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

        WifiNetworkAgent(Looper l, Context c, String tag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, tag, ni, nc, lp, score, misc);
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this == OppoClientModeImpl2.this.mNetworkAgent) {
                if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(OppoClientModeImpl2.this.mWifiInfo.score));
                }
                if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 == null) {
                    OppoClientModeImpl2.this.unwantedNetwork(0);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (this == OppoClientModeImpl2.this.mNetworkAgent && status != this.mLastNetworkStatus && TextUtils.isEmpty(redirectUrl)) {
                this.mLastNetworkStatus = status;
                if (status == 2) {
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(OppoClientModeImpl2.this.mWifiInfo.score));
                    }
                    OppoClientModeImpl2.this.mNetworkDetectValid = false;
                    OppoClientModeImpl2.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(OppoClientModeImpl2.this.mWifiInfo.score));
                    }
                    OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(14);
                    OppoClientModeImpl2.this.mNetworkDetectValid = true;
                    OppoClientModeImpl2.this.doNetworkStatus(status);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void saveAcceptUnvalidated(boolean accept) {
            if (this == OppoClientModeImpl2.this.mNetworkAgent) {
                OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        /* access modifiers changed from: protected */
        public void startSocketKeepalive(Message msg) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void stopSocketKeepalive(Message msg) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void addKeepalivePacketFilter(Message msg) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void removeKeepalivePacketFilter(Message msg) {
            OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.sendMessage(OppoClientModeImpl2.CMD_STOP_RSSI_MONITORING_OFFLOAD, oppoClientModeImpl2.mWifiInfo.getRssi());
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
                    Log.e(OppoClientModeImpl2.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                    oppoClientModeImpl22.sendMessage(OppoClientModeImpl2.CMD_STOP_RSSI_MONITORING_OFFLOAD, oppoClientModeImpl22.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            OppoClientModeImpl2.this.mRssiRanges = rssiRange;
            OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
            oppoClientModeImpl23.sendMessage(OppoClientModeImpl2.CMD_START_RSSI_MONITORING_OFFLOAD, oppoClientModeImpl23.mWifiInfo.getRssi());
        }

        /* access modifiers changed from: protected */
        public void preventAutomaticReconnect() {
            if (this == OppoClientModeImpl2.this.mNetworkAgent) {
                OppoClientModeImpl2.this.unwantedNetwork(2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    /* access modifiers changed from: package-private */
    public void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
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
                if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                    Log.e(OppoClientModeImpl2.TAG, "onRssiThresholdBreach event. Cur Rssi = " + ((int) curRssi));
                }
                OppoClientModeImpl2.this.sendMessage(OppoClientModeImpl2.CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        L2ConnectedState() {
        }

        public void enter() {
            OppoClientModeImpl2.this.mOppoClientModeImplUtil.resetSaeNetworkConsecutiveAssocRejectCounter();
            OppoClientModeImpl2.access$8408(OppoClientModeImpl2.this);
            if (OppoClientModeImpl2.this.mEnableRssiPolling) {
                OppoClientModeImpl2.this.mLinkProbeManager.resetOnNewConnection();
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.sendMessage(OppoClientModeImpl2.CMD_RSSI_POLL, oppoClientModeImpl2.mRssiPollToken, 0);
            }
            if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                OppoClientModeImpl2.this.loge("Have NetworkAgent when entering L2Connected");
                OppoClientModeImpl2.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            }
            OppoClientModeImpl2.this.setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
            OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
            NetworkCapabilities nc = oppoClientModeImpl22.getCapabilities(oppoClientModeImpl22.getCurrentWifiConfiguration());
            synchronized (OppoClientModeImpl2.this.mNetworkAgentLock) {
                OppoClientModeImpl2.this.mNetworkAgent = new WifiNetworkAgent(OppoClientModeImpl2.this.getHandler().getLooper(), OppoClientModeImpl2.this.mContext, "WifiNetworkAgent", OppoClientModeImpl2.this.mNetworkInfo, nc, OppoClientModeImpl2.this.mLinkProperties, 5, OppoClientModeImpl2.this.mNetworkMisc);
            }
            if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                Log.d(OppoClientModeImpl2.TAG, "network agent netid is " + Integer.toString(OppoClientModeImpl2.this.mNetworkAgent.netId));
            }
            OppoClientModeImpl2.this.clearTargetBssid("L2ConnectedState");
            OppoClientModeImpl2.this.mCountryCode.setReadyForChange(false);
            OppoClientModeImpl2.this.mWifiMetrics.setWifiState(3);
            if (OppoClientModeImpl2.this.mOppoDhcpRecord.isDoingSwitch()) {
                OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            }
            OppoClientModeImpl2.this.fetchRssiLinkSpeedAndFrequencyNative();
        }

        public void exit() {
            if (OppoClientModeImpl2.this.mIpClient != null) {
                OppoClientModeImpl2.this.mIpClient.stop();
            }
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(OppoClientModeImpl2.this.mLastNetworkId));
                if (OppoClientModeImpl2.this.mLastBssid != null) {
                    sb.append(" ");
                    sb.append(OppoClientModeImpl2.this.mLastBssid);
                }
            }
            if (!(OppoClientModeImpl2.this.mLastBssid == null && OppoClientModeImpl2.this.mLastNetworkId == -1)) {
                OppoClientModeImpl2.this.handleNetworkDisconnect();
            }
            OppoClientModeImpl2.this.mCountryCode.setReadyForChange(true);
            OppoClientModeImpl2.this.mWifiMetrics.setWifiState(2);
            OppoClientModeImpl2.this.mWifiStateTracker.updateState(2);
            OppoClientModeImpl2.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(false);
            OppoClientModeImpl2.this.mIsAutoRoaming = false;
        }

        /* JADX INFO: Multiple debug info for r1v109 int: [D('slot' int), D('currRssi' byte)] */
        /* JADX INFO: Multiple debug info for r1v186 int: [D('netId' int), D('info' android.net.wifi.RssiPacketCountInfo)] */
        public boolean processMessage(Message message) {
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            WifiConfiguration wConf;
            boolean handleStatus = true;
            switch (message.what) {
                case OppoClientModeImpl2.CMD_DISCONNECT /* 131145 */:
                    OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 2);
                    OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                    OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                    oppoClientModeImpl2.transitionTo(oppoClientModeImpl2.mDisconnectingState);
                    break;
                case OppoClientModeImpl2.CMD_RECONNECT /* 131146 */:
                    OppoClientModeImpl2.this.log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case OppoClientModeImpl2.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    OppoClientModeImpl2.this.cleanWifiScore();
                    OppoClientModeImpl2.this.mEnableRssiPolling = message.arg1 == 1;
                    OppoClientModeImpl2.access$8408(OppoClientModeImpl2.this);
                    if (OppoClientModeImpl2.this.mEnableRssiPolling) {
                        OppoClientModeImpl2.this.mLastSignalLevel = -1;
                        OppoClientModeImpl2.this.mLinkProbeManager.resetOnScreenTurnedOn();
                        OppoClientModeImpl2.this.fetchRssiLinkSpeedAndFrequencyNative();
                        OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                        oppoClientModeImpl22.sendMessageDelayed(oppoClientModeImpl22.obtainMessage(OppoClientModeImpl2.CMD_RSSI_POLL, oppoClientModeImpl22.mRssiPollToken, 0), (long) OppoClientModeImpl2.this.mPollRssiIntervalMsecs);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_RSSI_POLL /* 131155 */:
                    if (message.arg1 == OppoClientModeImpl2.this.mRssiPollToken) {
                        OppoClientModeImpl2.this.mWifiMetrics.updateWifiUsabilityStatsEntries(OppoClientModeImpl2.this.mWifiInfo, updateLinkLayerStatsRssiAndScoreReportInternal());
                        if (OppoClientModeImpl2.this.mWifiScoreReport.shouldCheckIpLayer()) {
                            if (OppoClientModeImpl2.this.mIpClient != null) {
                                OppoClientModeImpl2.this.mIpClient.confirmConfiguration();
                            }
                            OppoClientModeImpl2.this.mWifiScoreReport.noteIpCheck();
                        }
                        OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                        oppoClientModeImpl23.sendMessageDelayed(oppoClientModeImpl23.obtainMessage(OppoClientModeImpl2.CMD_RSSI_POLL, oppoClientModeImpl23.mRssiPollToken, 0), (long) OppoClientModeImpl2.this.mPollRssiIntervalMsecs);
                        if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                            OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                            oppoClientModeImpl24.sendRssiChangeBroadcast(oppoClientModeImpl24.mWifiInfo.getRssi());
                        }
                        OppoClientModeImpl2.this.mWifiTrafficPoller.notifyOnDataActivity(OppoClientModeImpl2.this.mWifiInfo.txSuccess, OppoClientModeImpl2.this.mWifiInfo.rxSuccess);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_ONESHOT_RSSI_POLL /* 131156 */:
                    if (!OppoClientModeImpl2.this.mEnableRssiPolling) {
                        updateLinkLayerStatsRssiAndScoreReportInternal();
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    if (message.arg1 == 0 && OppoClientModeImpl2.this.mLastNetworkId != -1) {
                        WifiConfiguration config = OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetwork(OppoClientModeImpl2.this.mLastNetworkId);
                        if (TelephonyUtil.isSimConfig(config) && message.arg2 == TelephonyUtil.getSimSlot(config)) {
                            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 6);
                            OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                            OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                            oppoClientModeImpl25.transitionTo(oppoClientModeImpl25.mDisconnectingState);
                        }
                    }
                    handleStatus = false;
                    break;
                case OppoClientModeImpl2.CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                    OppoClientModeImpl2.this.mIsAutoRoaming = false;
                    if (OppoClientModeImpl2.this.getCurrentWifiConfiguration() != null) {
                        OppoClientModeImpl2.this.handleSuccessfulIpConfiguration();
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null && OppoClientModeImpl2.this.wifiAssistantForSoftAP() && OppoClientModeImpl2.this.mAutoSwitch && OppoClientModeImpl2.this.getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_DETECT_CONNECT", true).booleanValue() && OppoClientModeImpl2.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED && !OppoClientModeImpl2.this.mNetworkDetectValid) {
                            OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                            oppoClientModeImpl26.transitionTo(oppoClientModeImpl26.mCaptiveState);
                            break;
                        } else {
                            if (OppoClientModeImpl2.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
                                OppoClientModeImpl2.this.mNetworkAgent.explicitlySelected(true);
                                OppoClientModeImpl2.this.mNetworkAgent.sendNetworkScore(79);
                            }
                            OppoClientModeImpl2.this.sendConnectedState();
                            OppoClientModeImpl2 oppoClientModeImpl27 = OppoClientModeImpl2.this;
                            oppoClientModeImpl27.transitionTo(oppoClientModeImpl27.mConnectedState);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.reportConnectionAttemptEnd(6, 1, 0);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2 oppoClientModeImpl28 = OppoClientModeImpl2.this;
                        oppoClientModeImpl28.transitionTo(oppoClientModeImpl28.mDisconnectingState);
                        break;
                    }
                case OppoClientModeImpl2.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    OppoClientModeImpl2.this.getWifiLinkLayerStats();
                    OppoClientModeImpl2.this.handleIpConfigurationLost();
                    OppoClientModeImpl2.this.reportConnectionAttemptEnd(10, 1, 0);
                    OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(OppoClientModeImpl2.this.getTargetSsid(), OppoClientModeImpl2.this.mTargetRoamBSSID, 3);
                    OppoClientModeImpl2 oppoClientModeImpl29 = OppoClientModeImpl2.this;
                    oppoClientModeImpl29.transitionTo(oppoClientModeImpl29.mDisconnectingState);
                    break;
                case OppoClientModeImpl2.CMD_START_CONNECT /* 131215 */:
                    int startConnectId = message.arg1;
                    WifiConfiguration connectConfig = OppoClientModeImpl2.this.getConnectConfig(startConnectId, (String) message.obj);
                    if (OppoClientModeImpl2.this.mWifiInfo == null || OppoClientModeImpl2.this.mWifiInfo.getNetworkId() != startConnectId || connectConfig == null || !OppoClientModeImpl2.this.mWifiNative.isSameNetwork(OppoClientModeImpl2.this.mInterfaceName, connectConfig)) {
                        return false;
                    }
                case OppoClientModeImpl2.CMD_ASSOCIATED_BSSID /* 131219 */:
                    if (((String) message.obj) != null) {
                        OppoClientModeImpl2.this.mLastBssid = (String) message.obj;
                        if (OppoClientModeImpl2.this.mLastBssid != null && (OppoClientModeImpl2.this.mWifiInfo.getBSSID() == null || !OppoClientModeImpl2.this.mLastBssid.equals(OppoClientModeImpl2.this.mWifiInfo.getBSSID()))) {
                            if (OppoClientModeImpl2.this.getCurrentState() == OppoClientModeImpl2.this.mConnectedState) {
                                OppoClientModeImpl2.this.mIsAutoRoaming = true;
                            }
                            OppoClientModeImpl2.this.mWifiInfo.setBSSID(OppoClientModeImpl2.this.mLastBssid);
                            WifiConfiguration config2 = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                            if (!(config2 == null || (scanDetailCache = OppoClientModeImpl2.this.mWifiConfigManager.getScanDetailCacheForNetwork(config2.networkId)) == null || (scanResult = scanDetailCache.getScanResult(OppoClientModeImpl2.this.mLastBssid)) == null)) {
                                OppoClientModeImpl2.this.mWifiInfo.setFrequency(scanResult.frequency);
                            }
                            OppoClientModeImpl2 oppoClientModeImpl210 = OppoClientModeImpl2.this;
                            oppoClientModeImpl210.sendNetworkStateChangeBroadcast(oppoClientModeImpl210.mLastBssid);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.logw("Associated command w/o BSSID");
                        break;
                    }
                case OppoClientModeImpl2.CMD_NETWORK_STATUS /* 131220 */:
                    if (message.arg1 == 1 && (OppoClientModeImpl2.this.getCurrentState() == OppoClientModeImpl2.this.mObtainingIpState || OppoClientModeImpl2.this.getCurrentState() == OppoClientModeImpl2.this.mRoamingState)) {
                        OppoClientModeImpl2.this.sendConnectedState();
                        OppoClientModeImpl2 oppoClientModeImpl211 = OppoClientModeImpl2.this;
                        oppoClientModeImpl211.transitionTo(oppoClientModeImpl211.mConnectedState);
                        break;
                    }
                case OppoClientModeImpl2.CMD_IP_REACHABILITY_LOST /* 131221 */:
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled && message.obj != null) {
                        OppoClientModeImpl2.this.log((String) message.obj);
                    }
                    OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(9);
                    OppoClientModeImpl2.this.mWifiMetrics.logWifiIsUnusableEvent(5);
                    OppoClientModeImpl2.this.mWifiMetrics.addToWifiUsabilityStatsList(2, 5, -1);
                    if (!OppoClientModeImpl2.this.mIpReachabilityDisconnectEnabled) {
                        OppoClientModeImpl2.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        break;
                    } else {
                        OppoClientModeImpl2.this.handleIpReachabilityLost();
                        OppoClientModeImpl2 oppoClientModeImpl212 = OppoClientModeImpl2.this;
                        oppoClientModeImpl212.transitionTo(oppoClientModeImpl212.mDisconnectingState);
                        break;
                    }
                case OppoClientModeImpl2.CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                    int slot = message.arg1;
                    int result = OppoClientModeImpl2.this.startWifiIPPacketOffload(slot, (KeepalivePacketData) message.obj, message.arg2);
                    if (OppoClientModeImpl2.this.mNetworkAgent != null) {
                        OppoClientModeImpl2.this.mNetworkAgent.onSocketKeepaliveEvent(slot, result);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                case OppoClientModeImpl2.CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                    OppoClientModeImpl2.this.processRssiThreshold((byte) message.arg1, message.what, this.mRssiEventHandler);
                    break;
                case OppoClientModeImpl2.CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                    OppoClientModeImpl2.this.stopRssiMonitoringOffload();
                    break;
                case OppoClientModeImpl2.CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                    if (OppoClientModeImpl2.this.mOppoDhcpRecord.isDoingSwitch()) {
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                    }
                    OppoClientModeImpl2.this.handleIPv4Success((DhcpResults) message.obj);
                    OppoClientModeImpl2 oppoClientModeImpl213 = OppoClientModeImpl2.this;
                    oppoClientModeImpl213.sendNetworkStateChangeBroadcast(oppoClientModeImpl213.mLastBssid);
                    break;
                case OppoClientModeImpl2.CMD_IPV4_PROVISIONING_FAILURE /* 131273 */:
                    if (OppoClientModeImpl2.this.mOppoDhcpRecord.isDoingSwitch()) {
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                    }
                    OppoClientModeImpl2.this.handleIPv4Failure();
                    OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(OppoClientModeImpl2.this.getTargetSsid(), OppoClientModeImpl2.this.mTargetRoamBSSID, 3);
                    break;
                case OppoClientModeImpl2.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /* 131281 */:
                    if (OppoClientModeImpl2.this.mIpClient != null) {
                        int slot2 = message.arg1;
                        if (!(message.obj instanceof NattKeepalivePacketData)) {
                            if (message.obj instanceof TcpKeepalivePacketData) {
                                OppoClientModeImpl2.this.mIpClient.addKeepalivePacketFilter(slot2, (TcpKeepalivePacketData) message.obj);
                                break;
                            }
                        } else {
                            OppoClientModeImpl2.this.mIpClient.addKeepalivePacketFilter(slot2, (NattKeepalivePacketData) message.obj);
                            break;
                        }
                    }
                    break;
                case OppoClientModeImpl2.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /* 131282 */:
                    if (OppoClientModeImpl2.this.mIpClient != null) {
                        OppoClientModeImpl2.this.mIpClient.removeKeepalivePacketFilter(message.arg1);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_PRE_DHCP_ACTION /* 131327 */:
                    OppoClientModeImpl2.this.handlePreDhcpSetup();
                    break;
                case OppoClientModeImpl2.CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                    if (OppoClientModeImpl2.this.mIpClient != null) {
                        OppoClientModeImpl2.this.mIpClient.completedPreDhcpAction();
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_POST_DHCP_ACTION /* 131329 */:
                    OppoClientModeImpl2.this.handlePostDhcpSetup();
                    break;
                case 131473:
                    OppoClientModeImpl2.this.log("[1131400] receive DhcpClient.EVENT_FIND_DUP_SERVER");
                    OppoClientModeImpl2.this.mOppoDhcpRecord.handleFindDupDhcpServer((String) message.obj);
                    break;
                case 131475:
                    OppoClientModeImpl2.this.log("[1131400] receive DhcpClient.EVENT_SWITCH_SERVER_FAILURE");
                    OppoClientModeImpl2.this.mOppoDhcpRecord.handleSwitchDhcpServerFailure((String) message.obj);
                    break;
                case 131476:
                    OppoClientModeImpl2.this.log("[1131400] receive DhcpClient.EVENT_SWITCH_SERVER_FAILURE");
                    OppoClientModeImpl2.this.mOppoDhcpRecord.handleFixDhcpServerFailure((String) message.obj);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    if (message.arg1 == 1) {
                        OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 5);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2.this.mTemporarilyDisconnectWifi = true;
                        OppoClientModeImpl2 oppoClientModeImpl214 = OppoClientModeImpl2.this;
                        oppoClientModeImpl214.transitionTo(oppoClientModeImpl214.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    OppoClientModeImpl2.this.mWifiInfo.setBSSID((String) message.obj);
                    if (OppoClientModeImpl2.this.hasConfigKeyChanged(message.arg1)) {
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                    }
                    if (message.arg1 != -1) {
                        OppoClientModeImpl2.this.mLastNetworkId = message.arg1;
                    }
                    OppoClientModeImpl2.this.mLastNetworkId = message.arg1;
                    OppoClientModeImpl2.this.mWifiInfo.setNetworkId(OppoClientModeImpl2.this.mLastNetworkId);
                    OppoClientModeImpl2.this.mWifiInfo.setMacAddress(OppoClientModeImpl2.this.mWifiNative.getMacAddress(OppoClientModeImpl2.this.mInterfaceName));
                    if (OppoClientModeImpl2.this.mLastBssid != null && !OppoClientModeImpl2.this.mLastBssid.equals((String) message.obj) && OppoClientModeImpl2.this.getCurrentState() == OppoClientModeImpl2.this.mConnectedState) {
                        OppoClientModeImpl2.this.mIsAutoRoaming = true;
                    }
                    if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.sendMessage(WifiMonitor.NETWORK_CONNECTION_EVENT, OppoClientModeImpl2.this.mLastNetworkId, message.arg2, OppoClientModeImpl2.this.mLastBssid);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_FREQUENCY_CHANGED /* 147468 */:
                    OppoClientModeImpl2.this.mWifiInfo.setFrequency(message.arg1);
                    OppoClientModeImpl2 oppoClientModeImpl215 = OppoClientModeImpl2.this;
                    oppoClientModeImpl215.sendNetworkStateChangeBroadcast(oppoClientModeImpl215.mLastBssid);
                    break;
                case 151553:
                    int netId = message.arg1;
                    if (netId == -1 && (wConf = (WifiConfiguration) message.obj) != null) {
                        netId = wConf.networkId;
                    }
                    if (!(OppoClientModeImpl2.this.mWifiInfo == null || OppoClientModeImpl2.this.mWifiInfo.getNetworkId() != netId || OppoManuConnectManager.getInstance() == null)) {
                        OppoManuConnectManager.getInstance().handleManuConnect(netId, message.sendingUid);
                        if (OppoClientModeImpl2.this.getCurrentState() == OppoClientModeImpl2.this.mConnectedState) {
                            if (OppoClientModeImpl2.this.mWifiConfigManager != null) {
                                OppoClientModeImpl2.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetwork(netId), -1, SupplicantState.COMPLETED);
                            }
                            if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                                OppoClientModeImpl2.this.mWifiNetworkStateTraker2.handleManualConnect(true);
                            }
                            OppoClientModeImpl2.this.sendConnectedState();
                            OppoClientModeImpl2.this.replyToMessage(message, 151555);
                            break;
                        } else if (OppoClientModeImpl2.this.mWifiNative != null) {
                            OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        }
                    }
                    return false;
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    OppoClientModeImpl2.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = OppoClientModeImpl2.this.mWifiInfo.getRssi();
                    WifiNative.TxPacketCounters counters = OppoClientModeImpl2.this.mWifiNative.getTxPacketCounters(OppoClientModeImpl2.this.mInterfaceName);
                    if (counters == null) {
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151574, 0);
                        break;
                    } else {
                        info.txgood = counters.txSucceeded;
                        info.txbad = counters.txFailed;
                        OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151573, (int) info);
                        break;
                    }
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        private WifiLinkLayerStats updateLinkLayerStatsRssiAndScoreReportInternal() {
            WifiLinkLayerStats stats = OppoClientModeImpl2.this.getWifiLinkLayerStats();
            OppoClientModeImpl2.this.fetchRssiLinkSpeedAndFrequencyNative();
            return stats;
        }
    }

    public void updateLinkLayerStatsRssiAndScoreReport() {
        sendMessage(CMD_ONESHOT_RSSI_POLL);
    }

    private static int convertToUsabilityStatsTriggerType(int unusableEventTriggerType) {
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
            WifiConfiguration currentConfig = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
            if (currentConfig == null) {
                OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.transitionTo(oppoClientModeImpl2.mDisconnectingState);
            } else if (SystemProperties.getInt("persist.sys.wifi_secure", 0) != 1 || currentConfig.checkWifiSecureLevel()) {
                if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                    String key = currentConfig.configKey();
                    OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                    oppoClientModeImpl22.log("enter ObtainingIpState netId=" + Integer.toString(OppoClientModeImpl2.this.mLastNetworkId) + " " + key + "  roam=" + OppoClientModeImpl2.this.mIsAutoRoaming + " static=false");
                }
                OppoClientModeImpl2.this.setNetworkDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR);
                OppoClientModeImpl2.this.clearTargetBssid("ObtainingIpAddress");
                DhcpResults dhcpResult = null;
                int dhcpStartType = 0;
                if (!OppoClientModeImpl2.this.mIsAutoRoaming) {
                    OppoClientModeImpl2.this.stopIpClient();
                    dhcpResult = OppoClientModeImpl2.this.mOppoDhcpRecord.getDhcpRecord(OppoClientModeImpl2.this.getCurrentConfigKey());
                    dhcpStartType = OppoClientModeImpl2.this.mOppoDhcpRecord.getStartType(dhcpResult);
                }
                if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                    OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                    oppoClientModeImpl23.logd("[bug#1131400] ObtainingIpState enter: StartType=" + dhcpStartType + "init dhcpResult" + dhcpResult);
                }
                if (OppoClientModeImpl2.this.mIpClient != null) {
                    OppoClientModeImpl2.this.mIpClient.setHttpProxy(currentConfig.getHttpProxy());
                    if (!TextUtils.isEmpty(OppoClientModeImpl2.this.mTcpBufferSizes)) {
                        OppoClientModeImpl2.this.mIpClient.setTcpBufferSizes(OppoClientModeImpl2.this.mTcpBufferSizes);
                    }
                }
                ProvisioningConfiguration prov = new ProvisioningConfiguration.Builder().withPreDhcpAction().withApfCapabilities(OppoClientModeImpl2.this.mWifiNative.getApfCapabilities(OppoClientModeImpl2.this.mInterfaceName)).withNetwork(OppoClientModeImpl2.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).withRandomMacAddress().withDhcpResult(dhcpResult).withStartType(dhcpStartType).build();
                if (!(OppoClientModeImpl2.this.mIpClient == null || prov == null)) {
                    OppoClientModeImpl2.this.mIpClient.startProvisioning(prov);
                }
                OppoClientModeImpl2.this.getWifiLinkLayerStats();
            } else {
                OppoClientModeImpl2.this.logd("checkWifiSecureLevel is false");
                OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                oppoClientModeImpl24.transitionTo(oppoClientModeImpl24.mDisconnectingState);
            }
        }

        public void exit() {
            if (OppoClientModeImpl2.this.mOppoDhcpRecord.isDoingSwitch()) {
                OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
            }
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case OppoClientModeImpl2.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = OppoClientModeImpl2.MESSAGE_HANDLING_STATUS_DEFERRED;
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                case OppoClientModeImpl2.CMD_START_ROAM /* 131217 */:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                    break;
                case OppoClientModeImpl2.CMD_START_SCAN /* 131479 */:
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2.this.logd("[1716726] Defer scan reques in mObtainingIpState");
                    }
                    OppoClientModeImpl2.this.mMessageHandlingStatus = OppoClientModeImpl2.MESSAGE_HANDLING_STATUS_DEFERRED;
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    OppoClientModeImpl2.this.reportConnectionAttemptEnd(6, 1, 0);
                    handleStatus = false;
                    break;
                case 151559:
                    OppoClientModeImpl2.this.mMessageHandlingStatus = OppoClientModeImpl2.MESSAGE_HANDLING_STATUS_DEFERRED;
                    OppoClientModeImpl2.this.deferMessage(message);
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class CaptiveState extends State {
        CaptiveState() {
        }

        public void enter() {
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                String key = "";
                if (OppoClientModeImpl2.this.getCurrentWifiConfiguration() != null) {
                    key = OppoClientModeImpl2.this.getCurrentWifiConfiguration().configKey();
                }
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.log("enter cpt netId=" + Integer.toString(OppoClientModeImpl2.this.mLastNetworkId) + " " + key);
            }
            OppoClientModeImpl2.this.setCaptivePortalMode(2);
            OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
            oppoClientModeImpl22.mConnectedId = oppoClientModeImpl22.mLastNetworkId;
            if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                OppoClientModeImpl2.this.mWifiNetworkStateTraker2.setNetworkDetailState(OppoClientModeImpl2.this.mLastNetworkId, NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK, OppoClientModeImpl2.this.mLastBssid);
            }
            OppoClientModeImpl2.this.setNetworkDetailedState(NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK);
            OppoClientModeImpl2.this.clearTargetBssid("CaptiveState");
            if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkInfo(OppoClientModeImpl2.this.mNetworkAgent.netId, OppoClientModeImpl2.this.mLinkProperties);
            }
        }

        public boolean processMessage(Message message) {
            String str;
            if (message == null) {
                OppoClientModeImpl2.this.logd("message is null,ignore!!");
                return true;
            }
            switch (message.what) {
                case OppoClientModeImpl2.CMD_SET_OPERATIONAL_MODE /* 131144 */:
                    if (message.arg1 != 1) {
                        OppoClientModeImpl2.this.mOperationalMode = message.arg1;
                    }
                    return false;
                case OppoClientModeImpl2.CMD_START_CONNECT /* 131215 */:
                    int startConId = message.arg1;
                    boolean ignoreDisconnect = false;
                    OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                    oppoClientModeImpl2.logd("startConId=" + startConId + ", getid= " + OppoClientModeImpl2.this.mWifiInfo.getNetworkId());
                    if (OppoClientModeImpl2.this.mWifiInfo.getNetworkId() == startConId) {
                        ignoreDisconnect = true;
                    }
                    if (ignoreDisconnect) {
                        OppoClientModeImpl2.this.logd("same netid no need to disconnect,just set manu connect flag and send broadcast!");
                        if (OppoManuConnectManager.getInstance() != null) {
                            OppoManuConnectManager.getInstance().handleManuConnect(startConId, message.sendingUid);
                            OppoManuConnectManager.getInstance().sendConnectModeChangeBroadcast(true);
                        }
                        OppoClientModeImpl2.this.mWifiConfigManager.oppoSetLastSelectedNetwork(startConId);
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.handleManualConnect(true);
                        }
                        OppoClientModeImpl2.this.sendConnectedState();
                        OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                        oppoClientModeImpl22.transitionTo(oppoClientModeImpl22.mConnectedState);
                        break;
                    } else {
                        return false;
                    }
                case OppoClientModeImpl2.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (message.arg1 == 1) {
                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkConnectResult(OppoClientModeImpl2.this.mLastNetworkId, false);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                        oppoClientModeImpl23.transitionTo(oppoClientModeImpl23.mDisconnectingState);
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setNetworkValidated(false);
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkState(1, OppoClientModeImpl2.this.mWifiInfo.getSSID());
                            break;
                        }
                    }
                    break;
                case OppoClientModeImpl2.CMD_START_ROAM /* 131217 */:
                    OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config = OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    if (config != null) {
                        OppoClientModeImpl2.this.setTargetBssid(config, bssid);
                        OppoClientModeImpl2.this.mTargetNetworkId = netId;
                        OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                        oppoClientModeImpl24.logd("CMD_START_ROAM sup state " + OppoClientModeImpl2.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + OppoClientModeImpl2.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config =" + config.configKey() + " targetRoamBSSID " + OppoClientModeImpl2.this.mTargetRoamBSSID);
                        OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                        oppoClientModeImpl25.reportConnectionAttemptStart(config, oppoClientModeImpl25.mTargetRoamBSSID, 3);
                        if (OppoClientModeImpl2.this.mWifiInfo != null) {
                            OppoClientModeImpl2.this.mWifiInfo.getNetworkId();
                        }
                        if (!OppoClientModeImpl2.this.mWifiNative.roamToNetwork(OppoClientModeImpl2.this.mInterfaceName, config)) {
                            OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                            oppoClientModeImpl26.loge("CMD_START_ROAM Failed to start roaming to network " + config);
                            OppoClientModeImpl2.this.reportConnectionAttemptEnd(5, 1, 0);
                            OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151554, 0);
                            OppoClientModeImpl2.this.mMessageHandlingStatus = -2;
                            break;
                        } else {
                            OppoClientModeImpl2 oppoClientModeImpl27 = OppoClientModeImpl2.this;
                            oppoClientModeImpl27.mLastConnectAttemptTimestamp = oppoClientModeImpl27.mClock.getWallClockMillis();
                            OppoClientModeImpl2.this.mTargetWifiConfiguration = config;
                            OppoClientModeImpl2.this.mIsAutoRoaming = true;
                            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(12, config);
                            OppoClientModeImpl2 oppoClientModeImpl28 = OppoClientModeImpl2.this;
                            oppoClientModeImpl28.transitionTo(oppoClientModeImpl28.mRoamingState);
                            break;
                        }
                    } else {
                        OppoClientModeImpl2.this.loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                case OppoClientModeImpl2.CMD_NETWORK_STATUS /* 131220 */:
                    if (message.arg1 == 1) {
                        OppoClientModeImpl2.this.sendConnectedState();
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setNetworkValidated(true);
                        OppoClientModeImpl2.this.mOppoMtuProber.StartMtuProber();
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkState(0, OppoClientModeImpl2.this.mWifiInfo.getSSID());
                        }
                        OppoClientModeImpl2 oppoClientModeImpl29 = OppoClientModeImpl2.this;
                        oppoClientModeImpl29.transitionTo(oppoClientModeImpl29.mConnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    OppoClientModeImpl2.this.reportConnectionAttemptEnd(5, 1, 0);
                    if (OppoClientModeImpl2.this.mLastDriverRoamAttempt != 0) {
                        long lastRoam = OppoClientModeImpl2.this.mClock.getWallClockMillis() - OppoClientModeImpl2.this.mLastDriverRoamAttempt;
                        OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
                    }
                    if (OppoClientModeImpl2.unexpectedDisconnectedReason(message.arg2)) {
                        OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config2 = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2 oppoClientModeImpl210 = OppoClientModeImpl2.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getBSSID());
                        sb.append(" RSSI=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config2 == null) {
                            str = "Unavailable";
                        } else {
                            str = config2.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str);
                        oppoClientModeImpl210.log(sb.toString());
                        break;
                    }
                    break;
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
    /* access modifiers changed from: public */
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
        int i = this.mLastNetworkId;
        this.mConnectedId = i;
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.setNetworkDetailState(i, NetworkInfo.DetailedState.CONNECTED, this.mLastBssid);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                OppoClientModeImpl2.this.log("RoamingState Enter mScreenOn=" + OppoClientModeImpl2.this.mScreenOn);
            }
            OppoClientModeImpl2.this.mRoamWatchdogCount++;
            OppoClientModeImpl2.this.logd("Start Roam Watchdog " + OppoClientModeImpl2.this.mRoamWatchdogCount);
            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
            oppoClientModeImpl2.sendMessageDelayed(oppoClientModeImpl2.obtainMessage(OppoClientModeImpl2.CMD_ROAM_WATCHDOG_TIMER, oppoClientModeImpl2.mRoamWatchdogCount, 0), 15000);
            this.mAssociated = false;
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case OppoClientModeImpl2.CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                    if (OppoClientModeImpl2.this.mRoamWatchdogCount == message.arg1) {
                        if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                            OppoClientModeImpl2.this.log("roaming watchdog! -> disconnect");
                        }
                        OppoClientModeImpl2.this.mWifiMetrics.endConnectionEvent(9, 1, 0);
                        OppoClientModeImpl2.access$12008(OppoClientModeImpl2.this);
                        OppoClientModeImpl2.this.handleNetworkDisconnect();
                        OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 4);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                        oppoClientModeImpl2.transitionTo(oppoClientModeImpl2.mDisconnectedState);
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    if (OppoClientModeImpl2.this.getCurrentWifiConfiguration() != null) {
                        OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(3);
                    }
                    handleStatus = false;
                    break;
                case OppoClientModeImpl2.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2.this.log("Roaming and CS doesn't want the network -> ignore");
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    if (!this.mAssociated) {
                        OppoClientModeImpl2.this.mMessageHandlingStatus = -5;
                        break;
                    } else {
                        if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                            OppoClientModeImpl2.this.log("roaming and Network connection established");
                        }
                        if (OppoClientModeImpl2.this.hasConfigKeyChanged(message.arg1)) {
                            OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.INIT);
                        }
                        OppoClientModeImpl2.this.mLastNetworkId = message.arg1;
                        OppoClientModeImpl2.this.mLastBssid = (String) message.obj;
                        OppoClientModeImpl2.this.mWifiInfo.setBSSID(OppoClientModeImpl2.this.mLastBssid);
                        OppoClientModeImpl2.this.mWifiInfo.setNetworkId(OppoClientModeImpl2.this.mLastNetworkId);
                        int i = message.arg2;
                        OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                        oppoClientModeImpl22.sendNetworkStateChangeBroadcast(oppoClientModeImpl22.mLastBssid);
                        OppoClientModeImpl2.this.reportConnectionAttemptEnd(1, 1, 0);
                        OppoClientModeImpl2.this.clearTargetBssid("RoamingCompleted");
                        if (!OppoClientModeImpl2.this.isUsingDHCP()) {
                            OppoClientModeImpl2.this.sendConnectedState();
                            OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                            oppoClientModeImpl23.transitionTo(oppoClientModeImpl23.mConnectedState);
                            break;
                        } else {
                            OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                            oppoClientModeImpl24.transitionTo(oppoClientModeImpl24.mObtainingIpState);
                            break;
                        }
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    String bssid = (String) message.obj;
                    String target = "";
                    if (OppoClientModeImpl2.this.mTargetRoamBSSID != null) {
                        target = OppoClientModeImpl2.this.mTargetRoamBSSID;
                    }
                    OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                    oppoClientModeImpl25.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + bssid + " target=" + target);
                    if (bssid != null && bssid.equals(OppoClientModeImpl2.this.mTargetRoamBSSID)) {
                        OppoClientModeImpl2.this.handleNetworkDisconnect();
                        OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                        oppoClientModeImpl26.transitionTo(oppoClientModeImpl26.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                            OppoClientModeImpl2 oppoClientModeImpl27 = OppoClientModeImpl2.this;
                            oppoClientModeImpl27.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(OppoClientModeImpl2.this.mTargetRoamBSSID)) {
                            OppoClientModeImpl2.this.handleNetworkDisconnect();
                            OppoClientModeImpl2 oppoClientModeImpl28 = OppoClientModeImpl2.this;
                            oppoClientModeImpl28.transitionTo(oppoClientModeImpl28.mDisconnectedState);
                        }
                    }
                    if (OppoClientModeImpl2.this.mLastSupplicantState != stateChangeResult.state) {
                        OppoClientModeImpl2.this.mLastSupplicantState = stateChangeResult.state;
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED || stateChangeResult.state == SupplicantState.FOUR_WAY_HANDSHAKE || stateChangeResult.state == SupplicantState.GROUP_HANDSHAKE || stateChangeResult.state == SupplicantState.COMPLETED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            OppoClientModeImpl2.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                            break;
                        }
                    }
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            OppoClientModeImpl2.this.logd("ClientModeImpl: Leaving Roaming state");
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.log("Enter ConnectedState  mScreenOn=" + OppoClientModeImpl2.this.mScreenOn);
            }
            if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkInfo(OppoClientModeImpl2.this.mNetworkAgent.netId, OppoClientModeImpl2.this.mLinkProperties);
            }
            OppoClientModeImpl2.this.setCaptivePortalMode(1);
            OppoClientModeImpl2.this.reportConnectionAttemptEnd(1, 1, 0);
            OppoClientModeImpl2.this.registerConnected();
            OppoClientModeImpl2.this.mLastConnectAttemptTimestamp = 0;
            OppoClientModeImpl2.this.mTargetWifiConfiguration = null;
            OppoClientModeImpl2.this.mWifiScoreReport.reset();
            OppoClientModeImpl2.this.mLastSignalLevel = -1;
            if (SystemProperties.get("debug.wifi.prdebug", "0").equals("1")) {
                SystemProperties.set("debug.wifi.prdebug", "0");
                if (OppoClientModeImpl2.this.mFromKeylogVerbose) {
                    OppoClientModeImpl2.this.enableVerboseLogging(0);
                }
            }
            OppoClientModeImpl2.this.removeMessages(OppoClientModeImpl2.CMD_CHECK_INTERNET_ACCESS);
            OppoClientModeImpl2.access$12408(OppoClientModeImpl2.this);
            if (OppoClientModeImpl2.this.mIsAutoRoaming) {
                OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                oppoClientModeImpl22.loge("Dhcp successful after driver roaming, check internet access, seq=" + OppoClientModeImpl2.this.mCheckInetAccessSeq);
                OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                oppoClientModeImpl23.sendMessageDelayed(oppoClientModeImpl23.obtainMessage(OppoClientModeImpl2.CMD_CHECK_INTERNET_ACCESS, 1, oppoClientModeImpl23.mCheckInetAccessSeq), 2000);
            } else {
                OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                oppoClientModeImpl24.loge("Dhcp successful, check internet access, seq=" + OppoClientModeImpl2.this.mCheckInetAccessSeq);
                OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                oppoClientModeImpl25.sendMessageDelayed(oppoClientModeImpl25.obtainMessage(OppoClientModeImpl2.CMD_CHECK_INTERNET_ACCESS, -1, oppoClientModeImpl25.mCheckInetAccessSeq), 2000);
            }
            OppoClientModeImpl2.this.mIsAutoRoaming = false;
            OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
            OppoClientModeImpl2.this.mTargetNetworkId = -1;
            OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            OppoClientModeImpl2.this.mWifiStateTracker.updateState(3);
            OppoClientModeImpl2.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(true);
        }

        public boolean processMessage(Message message) {
            boolean z;
            String str;
            String str2;
            boolean handleStatus = true;
            boolean accept = false;
            switch (message.what) {
                case OppoClientModeImpl2.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (message.arg1 != 0) {
                        if (message.arg1 == 2 || message.arg1 == 1) {
                            if (message.arg1 == 2) {
                                str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                            } else {
                                str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                            }
                            Log.d(OppoClientModeImpl2.TAG, str);
                            WifiConfiguration config = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                            if (config == null) {
                                z = true;
                                break;
                            } else {
                                if (message.arg1 == 2) {
                                    OppoClientModeImpl2.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                    OppoClientModeImpl2.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 10);
                                }
                                OppoClientModeImpl2.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                                if (message.arg1 != 1 || !OppoClientModeImpl2.this.mOppoDhcpRecord.needSwitchDhcpServer()) {
                                    if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 == null) {
                                        z = true;
                                        break;
                                    } else {
                                        z = true;
                                        OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkState(1, OppoClientModeImpl2.this.mWifiInfo.getSSID());
                                        break;
                                    }
                                } else {
                                    OppoClientModeImpl2.this.resetNetworkAngent();
                                    OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                                    oppoClientModeImpl2.transitionTo(oppoClientModeImpl2.mObtainingIpState);
                                    z = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 3);
                        OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                        OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                        oppoClientModeImpl22.transitionTo(oppoClientModeImpl22.mDisconnectingState);
                    }
                    z = true;
                    break;
                case OppoClientModeImpl2.CMD_START_ROAM /* 131217 */:
                    OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config2 = OppoClientModeImpl2.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config2 == null) {
                        OppoClientModeImpl2.this.loge("CMD_START_ROAM and no config, bail out...");
                    } else {
                        OppoClientModeImpl2.this.setTargetBssid(config2, bssid);
                        OppoClientModeImpl2.this.mTargetNetworkId = netId;
                        OppoClientModeImpl2.this.logd("CMD_START_ROAM sup state " + OppoClientModeImpl2.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + OppoClientModeImpl2.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config2.configKey() + " targetRoamBSSID " + OppoClientModeImpl2.this.mTargetRoamBSSID);
                        OppoClientModeImpl2 oppoClientModeImpl23 = OppoClientModeImpl2.this;
                        oppoClientModeImpl23.reportConnectionAttemptStart(config2, oppoClientModeImpl23.mTargetRoamBSSID, 3);
                        if (OppoClientModeImpl2.this.mWifiNative.roamToNetwork(OppoClientModeImpl2.this.mInterfaceName, config2)) {
                            OppoClientModeImpl2 oppoClientModeImpl24 = OppoClientModeImpl2.this;
                            oppoClientModeImpl24.mLastConnectAttemptTimestamp = oppoClientModeImpl24.mClock.getWallClockMillis();
                            OppoClientModeImpl2.this.mTargetWifiConfiguration = config2;
                            OppoClientModeImpl2.this.mIsAutoRoaming = true;
                            OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(12, config2);
                            OppoClientModeImpl2 oppoClientModeImpl25 = OppoClientModeImpl2.this;
                            oppoClientModeImpl25.transitionTo(oppoClientModeImpl25.mRoamingState);
                        } else {
                            OppoClientModeImpl2.this.loge("CMD_START_ROAM Failed to start roaming to network " + config2);
                            OppoClientModeImpl2.this.reportConnectionAttemptEnd(5, 1, 0);
                            OppoClientModeImpl2.this.replyToMessage((OppoClientModeImpl2) message, (Message) 151554, 0);
                            OppoClientModeImpl2.this.mMessageHandlingStatus = -2;
                        }
                    }
                    z = true;
                    break;
                case OppoClientModeImpl2.CMD_ASSOCIATED_BSSID /* 131219 */:
                    OppoClientModeImpl2 oppoClientModeImpl26 = OppoClientModeImpl2.this;
                    oppoClientModeImpl26.mLastDriverRoamAttempt = oppoClientModeImpl26.mClock.getWallClockMillis();
                    handleStatus = false;
                    z = true;
                    break;
                case OppoClientModeImpl2.CMD_NETWORK_STATUS /* 131220 */:
                    if (message.arg1 == 1) {
                        OppoClientModeImpl2.this.removeMessages(OppoClientModeImpl2.CMD_DIAGS_CONNECT_TIMEOUT);
                        OppoClientModeImpl2.this.mWifiDiagnostics.reportConnectionEvent((byte) 1);
                        WifiConfiguration config3 = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                        if (config3 != null) {
                            OppoClientModeImpl2.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config3.networkId, true);
                        }
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setSwitchState(OppoDhcpRecord.SwitchState.DONE);
                        OppoClientModeImpl2.this.mOppoDhcpRecord.setNetworkValidated(true);
                        OppoClientModeImpl2.this.mOppoMtuProber.StartMtuProber();
                        if (OppoClientModeImpl2.this.mWifiNetworkStateTraker2 != null) {
                            OppoClientModeImpl2.this.mWifiNetworkStateTraker2.updateNetworkState(0, OppoClientModeImpl2.this.mWifiInfo.getSSID());
                        }
                    }
                    z = true;
                    break;
                case OppoClientModeImpl2.CMD_ACCEPT_UNVALIDATED /* 131225 */:
                    if (message.arg1 != 0) {
                        accept = true;
                    }
                    OppoClientModeImpl2.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(OppoClientModeImpl2.this.mLastNetworkId, accept);
                    z = true;
                    break;
                case OppoClientModeImpl2.CMD_CHECK_INTERNET_ACCESS /* 131480 */:
                    OppoClientModeImpl2.this.loge("Checking internet access, SSID=" + OppoClientModeImpl2.this.mWifiInfo.getSSID() + " BSSID=" + OppoClientModeImpl2.this.mWifiInfo.getBSSID() + " checkSequence=" + message.arg2);
                    if (OppoDataStallHelper.getInstance() != null) {
                        OppoDataStallHelper.getInstance().checkInternetAccess(message.arg1, message.arg2, -1, OppoClientModeImpl2.this.mWifiInfo, OppoClientModeImpl2.this.mLastNetworkId, OppoClientModeImpl2.this.mInterfaceName);
                    }
                    z = true;
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    long lastRoam = 0;
                    OppoClientModeImpl2.this.reportConnectionAttemptEnd(6, 1, 0);
                    if (OppoClientModeImpl2.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = OppoClientModeImpl2.this.mClock.getWallClockMillis() - OppoClientModeImpl2.this.mLastDriverRoamAttempt;
                        OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
                    }
                    if (OppoClientModeImpl2.unexpectedDisconnectedReason(message.arg2)) {
                        OppoClientModeImpl2.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config4 = OppoClientModeImpl2.this.getCurrentWifiConfiguration();
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2 oppoClientModeImpl27 = OppoClientModeImpl2.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getBSSID());
                        sb.append(" RSSI=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(OppoClientModeImpl2.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config4 == null) {
                            str2 = "Unavailable";
                        } else {
                            str2 = config4.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str2);
                        oppoClientModeImpl27.log(sb.toString());
                    }
                    if (config4 != null) {
                        if (config4.configKey() != null) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - OppoClientModeImpl2.this.mConnectionTimeStamp < ((long) OppoClientModeImpl2.this.getRomUpdateIntegerValue("OPPO_DUAL_STA_DISCONNECT_FREQ_THROTTLED", 360000).intValue()) && message.arg2 != 3) {
                                Log.d(OppoClientModeImpl2.TAG, "wlan1 dis in 6min");
                                if (OppoClientModeImpl2.this.mApTriggerDisableCount.get(config4.configKey()) == null) {
                                    OppoClientModeImpl2.this.mApTriggerDisableCount.put(config4.configKey(), 1);
                                } else {
                                    OppoClientModeImpl2.this.mApTriggerDisableCount.put(config4.configKey(), Integer.valueOf(((Integer) OppoClientModeImpl2.this.mApTriggerDisableCount.get(config4.configKey())).intValue() + 1));
                                }
                            } else if (currentTime - OppoClientModeImpl2.this.mConnectionTimeStamp >= ((long) OppoClientModeImpl2.this.getRomUpdateIntegerValue("OPPO_DUAL_STA_DISCONNECT_FREQ_THROTTLED", 360000).intValue())) {
                                OppoClientModeImpl2.this.clearDisable();
                            }
                            if (OppoClientModeImpl2.this.mApTriggerDisableCount.get(config4.configKey()) != null && ((Integer) OppoClientModeImpl2.this.mApTriggerDisableCount.get(config4.configKey())).intValue() >= OppoClientModeImpl2.this.getRomUpdateIntegerValue("OPPO_DUAL_STA_TRIGGER_DISABLE_COUNT", 6).intValue()) {
                                OppoClientModeImpl2.this.mDisableConnectDuring.put(config4.configKey(), Long.valueOf(System.currentTimeMillis()));
                            }
                        }
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    handleStatus = false;
                    break;
            }
            if (handleStatus == z) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            OppoClientModeImpl2.this.logd("ClientModeImpl: Leaving Connected state");
            OppoClientModeImpl2.this.mLastDriverRoamAttempt = 0;
            OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                OppoClientModeImpl2.this.logd(" Enter DisconnectingState State screenOn=" + OppoClientModeImpl2.this.mScreenOn);
            }
            OppoClientModeImpl2.this.mDisconnectingWatchdogCount++;
            OppoClientModeImpl2.this.logd("Start Disconnecting Watchdog " + OppoClientModeImpl2.this.mDisconnectingWatchdogCount);
            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
            oppoClientModeImpl2.sendMessageDelayed(oppoClientModeImpl2.obtainMessage(OppoClientModeImpl2.CMD_DISCONNECTING_WATCHDOG_TIMER, oppoClientModeImpl2.mDisconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            int i = message.what;
            if (i != OppoClientModeImpl2.CMD_DISCONNECT) {
                if (i != OppoClientModeImpl2.CMD_DISCONNECTING_WATCHDOG_TIMER) {
                    if (i != 147462) {
                        handleStatus = false;
                    } else {
                        OppoClientModeImpl2.this.deferMessage(message);
                        OppoClientModeImpl2.this.handleNetworkDisconnect();
                        OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                        oppoClientModeImpl2.transitionTo(oppoClientModeImpl2.mDisconnectedState);
                    }
                } else if (OppoClientModeImpl2.this.mDisconnectingWatchdogCount == message.arg1) {
                    if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                        OppoClientModeImpl2.this.log("disconnecting watchdog! -> disconnect");
                    }
                    OppoClientModeImpl2.this.handleNetworkDisconnect();
                    OppoClientModeImpl2 oppoClientModeImpl22 = OppoClientModeImpl2.this;
                    oppoClientModeImpl22.transitionTo(oppoClientModeImpl22.mDisconnectedState);
                }
            } else if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                OppoClientModeImpl2.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.i(OppoClientModeImpl2.TAG, "disconnectedstate enter");
            if (OppoClientModeImpl2.this.mTemporarilyDisconnectWifi) {
                OppoClientModeImpl2.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                oppoClientModeImpl2.logd(" Enter DisconnectedState screenOn=" + OppoClientModeImpl2.this.mScreenOn);
            }
            OppoClientModeImpl2.this.mIsAutoRoaming = false;
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case OppoClientModeImpl2.CMD_DISCONNECT /* 131145 */:
                    OppoClientModeImpl2.this.mWifiMetrics.logStaEvent(15, 2);
                    OppoClientModeImpl2.this.mWifiNative.disconnect(OppoClientModeImpl2.this.mInterfaceName);
                    break;
                case OppoClientModeImpl2.CMD_RECONNECT /* 131146 */:
                case OppoClientModeImpl2.CMD_REASSOCIATE /* 131147 */:
                    if (!OppoClientModeImpl2.this.mTemporarilyDisconnectWifi) {
                        handleStatus = false;
                        break;
                    }
                    break;
                case OppoClientModeImpl2.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    OppoClientModeImpl2.this.handleScreenStateChanged(message.arg1 != 0);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                    OppoClientModeImpl2.this.mP2pConnected.set(((NetworkInfo) message.obj).isConnected());
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    if (message.arg2 == 15) {
                        OppoClientModeImpl2.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(OppoClientModeImpl2.this.getTargetSsid(), message.obj == null ? OppoClientModeImpl2.this.mTargetRoamBSSID : (String) message.obj, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult != null && stateChangeResult.state != null) {
                        if (OppoClientModeImpl2.this.mVerboseLoggingEnabled) {
                            OppoClientModeImpl2 oppoClientModeImpl2 = OppoClientModeImpl2.this;
                            oppoClientModeImpl2.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                        }
                        OppoClientModeImpl2.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                        handleStatus = false;
                        break;
                    } else {
                        return true;
                    }
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                OppoClientModeImpl2.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    private boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(message.arg1);
        String curConfigKey = getCurrentConfigKey();
        boolean success = this.mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid);
        if (!success) {
            loge("Failed to remove network");
        }
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

    private NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = (WifiConfiguration) message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration " + this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + getCurrentState().getName());
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
        ClientModeImpl.convertToQuotedSSID(config);
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
    /* access modifiers changed from: public */
    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean p2pSendMessage(int what) {
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

    private void checkAndSetSsidForConfig(WifiConfiguration config) {
        String configSSID;
        String configBssid;
        List<ScanResult> srList;
        if (!(config == null || (configSSID = config.SSID) == null || configSSID.equals("") || (configBssid = config.BSSID) == null || configBssid.equals("00:00:00:00:00:00") || (srList = this.mScanRequestProxy.getScanResults()) == null)) {
            Long newestTimeStamp = 0L;
            String targetSsid = null;
            int count = 0;
            boolean isExists = false;
            for (ScanResult sr : srList) {
                if (sr != null) {
                    String ssid = "\"" + sr.SSID + "\"";
                    if (configBssid.equals(sr.BSSID)) {
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
            if (targetSsid == null || targetSsid.equals("\"\"")) {
                Log.d(TAG, "target = " + targetSsid);
                return;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "set manu connect from " + configSSID + " ssid to " + targetSsid);
            }
            config.SSID = targetSsid;
        }
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
    }

    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        this.mWifiMetrics.incrementWifiUsabilityScoreCount(seqNum, score, predictionHorizonSec);
    }

    @VisibleForTesting
    public void probeLink(WifiNative.SendMgmtFrameCallback callback, int mcs) {
        this.mWifiNative.probeLink(this.mInterfaceName, MacAddress.fromString(this.mWifiInfo.getBSSID()), callback, mcs);
    }

    public void setTargetNetworkId(int id) {
        this.mOppoClientModeImplUtil.setTargetNetworkId(id, this.mTargetNetworkId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isManuConnect() {
        return this.mOppoClientModeImplUtil.isManuConnect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getManuConnectNetId() {
        return this.mOppoClientModeImplUtil.getManuConnectNetId();
    }

    public void handleSSIDStateChangedCB(int netId, int reason) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "handleSSIDStateChangedCB netId:" + netId + " reason:" + reason);
        }
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public WifiConfiguration getmTargetWifiConfiguration() {
        return this.mTargetWifiConfiguration;
    }

    public boolean isSupplicantStateDisconnected() {
        return this.mOppoClientModeImplUtil.isSupplicantStateDisconnected(this.mSupplicantStateTracker);
    }

    public boolean isSupplicantAvailable() {
        if (!this.mOppoClientModeImplUtil.isSupplicantAvailable(this.mOperationalMode)) {
            return false;
        }
        if (!"SupplicantStoppingState".equalsIgnoreCase(getCurrentState().getName())) {
            return true;
        }
        logd("supplicant in stoppong state!");
        return false;
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
        NetworkInfo networkInfo;
        if (this.mTargetNetworkId == netId && (networkInfo = this.mNetworkInfo) != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public boolean isNetworkConnected(int netId) {
        if (this.mWifiInfo.getNetworkId() == netId) {
            return true;
        }
        return false;
    }

    public void prepareForForcedConnection(int netId) {
    }

    public boolean isBootCompleted() {
        if (((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getBestBssidForNetId(int netId) {
        String srConfigKey;
        List<ScanResult> srList = this.mScanRequestProxy.syncGetScanResultsList();
        if (srList == null || srList.size() <= 0) {
            if (this.mVerboseLoggingEnabled) {
                logd("getBestBssidForNetId:srList is null or empty!!");
            }
            return null;
        }
        WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
        if (wifiConfigManager == null) {
            if (this.mVerboseLoggingEnabled) {
                logd("getBestBssidForNetId:mWifiConfigManager is null!!");
            }
            return null;
        }
        WifiConfiguration wConf = wifiConfigManager.getWifiConfigurationForAll(netId);
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
            if (sr != null && (srConfigKey = WifiConfiguration.configKey(sr)) != null && srConfigKey.equals(configKey) && sr.level > bestLevel) {
                bssid = sr.BSSID;
                bestLevel = sr.level;
            }
        }
        if (bssid != null) {
            int sameBssidCount = 0;
            for (ScanResult sr2 : srList) {
                if (sr2 != null && bssid.equals(sr2.BSSID) && sr2.SSID != null && !sr2.SSID.isEmpty()) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetNetworkAngent() {
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
    /* access modifiers changed from: public */
    private boolean hasConfigKeyChanged(int newNetId) {
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
    /* access modifiers changed from: public */
    private OppoWifiAssistantStateTraker2 makeWifiNetworkStateTracker() {
        return new OppoWifiAssistantStateTraker2(this.mContext, this, this.mWifiConfigManager, this.mWifiNative, this.mSupplicantStateTracker, this.mWifiRomUpdateHelper, getHandler(), this.mScanRequestProxy);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean wifiAssistantForSoftAP() {
        boolean isSoftAP = this.mWifiNetworkStateTraker2.isSoftAp(this.mLinkProperties);
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
    /* access modifiers changed from: public */
    private void setCaptivePortalMode(int mode) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "captive_portal_mode", mode);
    }

    public boolean isRoaming() {
        return this.mIsAutoRoaming;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiConfiguration getConnectConfig(int netId, String bssid) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
        if (config == null) {
            return null;
        }
        ClientModeImpl.convertToQuotedSSID(config);
        String tmpBssid = getBestBssidForNetId(netId);
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
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.detectScanResult(this.mLastScanTime);
        }
    }

    public void setStatistics(String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sheduleRestartWifi(int netId) {
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
            log("[1616660] syncScanResults size " + scanResults.size());
        }
        this.mLastScanTime = System.currentTimeMillis();
        OppoWifiAssistantStateTraker2 oppoWifiAssistantStateTraker2 = this.mWifiNetworkStateTraker2;
        if (oppoWifiAssistantStateTraker2 != null) {
            oppoWifiAssistantStateTraker2.detectScanResult(this.mLastScanTime);
        }
        if (this.mOppoScanResultsProxy == null) {
            this.mOppoScanResultsProxy = this.mWifiInjector.getOppoScanResultsProxy();
        }
        if (this.mOppoScanResultsProxy != null && getCurrentState() != null) {
            this.mOppoScanResultsProxy.checkScanFewAP(scanResults, getCurrentState().getName());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean unexpectedDisconnectedDisable(int reason) {
        String allreasonstr = getRomUpdateValue("UNEXPECTED_DISCONNECT_DISABLE_REASON", "");
        if (allreasonstr == null || allreasonstr.equals("")) {
            return unexpectedDisconnectedReason(reason);
        }
        try {
            for (String romupdatereason : allreasonstr.split(",")) {
                if (Integer.valueOf(romupdatereason).intValue() == reason) {
                    return true;
                }
            }
        } catch (Exception e) {
            loge("transfer unexpectedDisconnectedDisable reason to int failed");
        }
        return false;
    }

    public boolean startScan(int callingUid, String packageName) {
        if (getCurrentState() != this.mObtainingIpState && isBootCompleted()) {
            return this.mScanRequestProxy.startScan(callingUid, packageName);
        }
        if (!hasMessages(CMD_START_SCAN)) {
            sendMessage(CMD_START_SCAN, 1000, 0, packageName);
            return true;
        }
        logd("has CMD_START_SCAN in queue, Just return.");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUsingDHCP() {
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig == null || currentConfig.getIpAssignment() != IpConfiguration.IpAssignment.DHCP) {
            return false;
        }
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults == null || this.mDhcpResults.ipAddress == null || this.mDhcpResults.ipAddress.getAddress() == null || !this.mDhcpResults.ipAddress.isIpv4()) {
                return false;
            }
            return true;
        }
    }

    public void notifyDataStallEvent(int errCode) {
        sendMessage(CMD_FIRMWARE_ALERT, errCode + 256);
    }

    public int getCurrentCheckInetAccessSeq() {
        return this.mCheckInetAccessSeq;
    }

    public String getOppoSta2CurConfigKey() {
        return getCurrentConfigKey();
    }

    public boolean notifyRemoveNetwork(int networkId) {
        sendMessage(CMD_REMOVE_NETWORK, networkId);
        return true;
    }

    public void clearDisable() {
        this.mApTriggerDisableCount.clear();
        this.mDisableConnectDuring.clear();
        this.mConnectionTimeStamp = 0;
    }

    public void removeDisable(String configKey) {
        Log.d(TAG, "wlan1 remove disable network: " + configKey);
        if (this.mDisableConnectDuring.containsKey(configKey)) {
            this.mDisableConnectDuring.remove(configKey);
        }
        if (this.mApTriggerDisableCount.containsKey(configKey)) {
            this.mApTriggerDisableCount.remove(configKey);
        }
    }

    public boolean isTempDisabled(String configKey) {
        Long disableconnectduring = this.mDisableConnectDuring.get(configKey);
        if (disableconnectduring != null && disableconnectduring.longValue() > 0 && System.currentTimeMillis() - disableconnectduring.longValue() < ((long) getRomUpdateIntegerValue("OPPO_DUAL_STA_DISABLE_DURING", 3600000).intValue())) {
            Log.d(TAG, "wlan1 is disable connect,just return");
            return true;
        } else if (disableconnectduring == null || disableconnectduring.longValue() <= 0 || System.currentTimeMillis() - disableconnectduring.longValue() < ((long) getRomUpdateIntegerValue("OPPO_DUAL_STA_DISABLE_DURING", 3600000).intValue())) {
            return false;
        } else {
            removeDisable(configKey);
            return false;
        }
    }

    public void setOtherWifiInfo(WifiInfo otherWifiInfo) {
        this.mOtherWifiInfo = otherWifiInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean attemptWpa2FallbackConnectionIfRequired(String bssid) {
        if (this.mOppoScanResultsProxy == null) {
            this.mOppoScanResultsProxy = this.mWifiInjector.getOppoScanResultsProxy();
        }
        WifiConfiguration config = this.mOppoClientModeImplUtil.attemptWpa2FallbackConnectionIfRequired(this.mTargetWifiConfiguration, this.mWifiConfigManager, this.mTargetNetworkId, this.mInterfaceName, this.mWifiInfo, this.mOppoScanResultsProxy);
        if (config != null) {
            this.mTargetWifiConfiguration = config;
            Log.d(TAG, "try to attemptWpa2FallbackConnectionIfRequired");
            return true;
        }
        Log.d(TAG, "do not attemptWpa2FallbackConnectionIfRequired");
        return false;
    }
}
