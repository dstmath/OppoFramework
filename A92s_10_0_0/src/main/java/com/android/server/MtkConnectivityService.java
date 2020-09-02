package com.android.server;

import android.app.BroadcastOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hidl.base.V1_0.DebugInfo;
import android.net.CaptivePortal;
import android.net.ConnectionInfo;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.IDnsResolver;
import android.net.IIpConnectivityMetrics;
import android.net.INetd;
import android.net.INetdEventCallback;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkMonitor;
import android.net.INetworkMonitorCallbacks;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.ISocketKeepaliveCallback;
import android.net.ITetheringEventCallback;
import android.net.InetAddresses;
import android.net.IpMemoryStore;
import android.net.IpPrefix;
import android.net.LinkProperties;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkMonitorManager;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkStackClient;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.NetworkWatchlistManager;
import android.net.PrivateDnsConfigParcel;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StringNetworkSpecifier;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.netlink.InetDiagMessage;
import android.net.shared.NetworkMonitorUtils;
import android.net.shared.PrivateDnsConfig;
import android.net.util.MultinetworkPolicyTracker;
import android.net.util.NetdService;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.security.KeyStore;
import android.system.OsConstants;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.WakeupMessage;
import com.android.internal.util.XmlUtils;
import com.android.server.MtkConnectivityService;
import com.android.server.am.BatteryStatsService;
import com.android.server.connectivity.AutodestructReference;
import com.android.server.connectivity.DataConnectionStats;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.connectivity.KeepaliveTracker;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.connectivity.MockableSystemProperties;
import com.android.server.connectivity.MultipathPolicyTracker;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkDiagnostics;
import com.android.server.connectivity.NetworkNotificationManager;
import com.android.server.connectivity.OppoPrivateDnsHelper;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.ProxyTracker;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.connectivity.oppo.OppoAutoConCaptivePortalControl;
import com.android.server.connectivity.oppo.OppoDualWifiSta2CaptivePortalControl;
import com.android.server.connectivity.tethering.TetheringDependencies;
import com.android.server.net.BaseNetdEventCallback;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.utils.PriorityDump;
import com.google.android.collect.Lists;
import com.mediatek.internal.telephony.MtkLteDataOnlyController;
import com.mediatek.net.tethering.MtkTethering;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.SocketFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MtkConnectivityService extends ConnectivityService {
    private static final String ACTION_DISABLE_DATA_ALERT = "com.mediatek.intent.action.IGNORE_DATA_USAGE_ALERT";
    private static final String ACTION_OPERATOR_CONFIG_CHANGED = "com.mediatek.common.carrierexpress.operator_config_changed";
    private static final String ATTR_MCC = "mcc";
    private static final String ATTR_MNC = "mnc";
    private static final String AUTO_TETHERING_INTENT = "com.mediatek.intent.action.TETHERING_CHANGED";
    private static final String DATA_SCORE_CHANGE = "android.net.wifi.OPPO_DATA_NET_CHANGE";
    private static final boolean DBG = true;
    private static final boolean DDBG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_CAPTIVE_PORTAL_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_EVALUATION_BLACKLIST = "com.google.android";
    private static final int DEFAULT_LINGER_DELAY_MS = 3000;
    @VisibleForTesting
    protected static final String DEFAULT_TCP_BUFFER_SIZES = "4096,87380,110208,4096,16384,110208";
    private static final String DEFAULT_TCP_RWND_KEY = "net.tcp.default_init_rwnd";
    private static final String DIAG_ARG = "--diag";
    private static final long DISABLE_DATA_ALERT_BYTES = 0;
    private static final int EVENT_APPLY_GLOBAL_HTTP_PROXY = 9;
    private static final int EVENT_CLEAR_NET_TRANSITION_WAKELOCK = 8;
    private static final int EVENT_CONFIGURE_ALWAYS_ON_NETWORKS = 30;
    private static final int EVENT_DATA_SAVER_CHANGED = 40;
    private static final int EVENT_ENABLE_MOBILE_DATA_FOR_TETHERING = 100;
    private static final int EVENT_EXPIRE_NET_TRANSITION_WAKELOCK = 24;
    public static final int EVENT_NETWORK_TESTED = 41;
    private static final int EVENT_NOTIFY_NETWORK_CONNECTED = 102;
    private static final int EVENT_NOTIFY_PRIVATE_DNS_STATUS = 101;
    public static final int EVENT_PRIVATE_DNS_CONFIG_RESOLVED = 42;
    private static final int EVENT_PRIVATE_DNS_SETTINGS_CHANGED = 37;
    private static final int EVENT_PRIVATE_DNS_VALIDATION_UPDATE = 38;
    private static final int EVENT_PROMPT_UNVALIDATED = 29;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 43;
    private static final int EVENT_PROXY_HAS_CHANGED = 16;
    private static final int EVENT_REGISTER_NETWORK_AGENT = 18;
    private static final int EVENT_REGISTER_NETWORK_FACTORY = 17;
    private static final int EVENT_REGISTER_NETWORK_LISTENER = 21;
    private static final int EVENT_REGISTER_NETWORK_LISTENER_WITH_INTENT = 31;
    private static final int EVENT_REGISTER_NETWORK_REQUEST = 19;
    private static final int EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT = 26;
    private static final int EVENT_RELEASE_NETWORK_REQUEST = 22;
    private static final int EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT = 27;
    private static final int EVENT_REVALIDATE_NETWORK = 36;
    private static final int EVENT_SET_ACCEPT_PARTIAL_CONNECTIVITY = 45;
    private static final int EVENT_SET_ACCEPT_UNVALIDATED = 28;
    private static final int EVENT_SET_AVOID_UNVALIDATED = 35;
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    public static final int EVENT_TIMEOUT_NOTIFICATION = 44;
    private static final int EVENT_UID_RULES_CHANGED = 39;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final String EXTRA_DATA_CORE = "data_score";
    private static final String EXTRA_ENALE_DATA = "enableData";
    private static final int INVALID_SCORE = 10;
    private static final boolean IS_LAB_TEST = "1".equalsIgnoreCase(SystemProperties.get("persist.sys.nw_lab_test", "0"));
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final boolean LOGD_BLOCKED_NETWORKINFO = true;
    private static final int MAX_CELLULAR_INTERFACE = 5;
    private static final int MAX_NETWORK_INFO_LOGS = 40;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 64511;
    private static final int MAX_WAKELOCK_LOGS = 20;
    private static final int MIN_NET_ID = 100;
    private static final String NAME_USE_HTTPS = "captive_portal_use_https";
    private static final String NETWORK_ARG = "networks";
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final String OPPO_WIFISECURE_DETECT_PACKAGE_NAME = "com.coloros.wifisecuredetect";
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    public static final int PROVISIONING_NOTIFICATION_HIDE = 0;
    public static final int PROVISIONING_NOTIFICATION_SHOW = 1;
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final String REQUEST_ARG = "requests";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    public static final String SHORT_ARG = "--short";
    private static final String SYSTEM_PROPERTY_OPTR = "persist.vendor.operator.optr";
    /* access modifiers changed from: private */
    public static final String TAG = MtkConnectivityService.class.getSimpleName();
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    private static final boolean TDD_DATA_ONLY_SUPPORT = SystemProperties.get("ro.vendor.mtk_tdd_data_only_support").equals("1");
    private static final String TETHERING_ARG = "tethering";
    private static final int TIMEOUT_NOTIFICATION_DELAY_MS = 20000;
    private static final boolean USP_SUPPORTED = (!SystemProperties.get("ro.vendor.mtk_carrierexpress_pack", "no").equals("no"));
    private static final int VALID_SCORE = 50;
    /* access modifiers changed from: private */
    public static final boolean VDBG = Log.isLoggable(TAG, 2);
    private static final String WIFI_SCROE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    /* access modifiers changed from: private */
    public static final boolean WLAN_ASSIST_DBG = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
    private static boolean mAlreadyUpdated;
    private static MtkTethering mMtkTethering;
    private static double mPowerLost;
    private static String mPowerState;
    /* access modifiers changed from: private */
    public static boolean sIsAutoTethering = SystemProperties.getBoolean("persist.vendor.net.auto.tethering", false);
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(new Class[]{AsyncChannel.class, MtkConnectivityService.class, NetworkAgent.class, NetworkAgentInfo.class});
    private int ENABLE_WLAN_ASSISTANT;
    private String[] EXTERNAL_SERVERS;
    private String[] INTERNAL_SERVERS;
    private long RECONNECT_TIMER;
    private long REMEASURE_TIMER;
    private long REMEASURE_TIMER_FINE;
    private int RESPONSE_TIMEOUT;
    private int TOTAL_MEASURE_TIME;
    private boolean hasWifiAssistant;
    /* access modifiers changed from: private */
    public OppoAutoConCaptivePortalControl mAutoConCaptivePortalControl;
    @GuardedBy({"mBandwidthRequests"})
    private final SparseArray<Integer> mBandwidthRequests;
    private boolean mBlock;
    @GuardedBy({"mBlockedAppUids"})
    private final HashSet<Integer> mBlockedAppUids;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public OppoConnectivityServiceHelper mCsHelper;
    private String mCurrentTcpBufferSizes;
    private INetworkManagementEventObserver mDataActivityObserver;
    private InetAddress mDefaultDns;
    private int mDefaultInetConditionPublished;
    private final NetworkRequest mDefaultMobileDataRequest;
    /* access modifiers changed from: private */
    public final NetworkRequest mDefaultRequest;
    private final NetworkRequest mDefaultWifiRequest;
    /* access modifiers changed from: private */
    public final DnsManager mDnsManager;
    @VisibleForTesting
    protected IDnsResolver mDnsResolver;
    /* access modifiers changed from: private */
    public OppoDualWifiSta2CaptivePortalControl mDualWifiSta2CaptivePortalControl;
    /* access modifiers changed from: private */
    public final InternalHandler mHandler;
    @VisibleForTesting
    protected final HandlerThread mHandlerThread;
    private Intent mInitialBroadcast;
    private BroadcastReceiver mIntentReceiver;
    /* access modifiers changed from: private */
    public KeepaliveTracker mKeepaliveTracker;
    private KeyStore mKeyStore;
    private long mLastMeasureTime;
    private long mLastRequestNetworkTime;
    private long mLastWakeLockAcquireTimestamp;
    private final LegacyTypeTracker mLegacyTypeTracker;
    @VisibleForTesting
    protected int mLingerDelayMs;
    private LingerMonitor mLingerMonitor;
    @GuardedBy({"mVpns"})
    private boolean mLockdownEnabled;
    @GuardedBy({"mVpns"})
    private LockdownVpnTracker mLockdownTracker;
    private long mMaxWakelockDurationMs;
    private int mMeasureTime;
    private final IpConnectivityLog mMetricsLog;
    @VisibleForTesting
    final MultinetworkPolicyTracker mMultinetworkPolicyTracker;
    @VisibleForTesting
    final MultipathPolicyTracker mMultipathPolicyTracker;
    private INetworkManagementService mNMS;
    private NetworkConfig[] mNetConfigs;
    @GuardedBy({"mNetworkForNetId"})
    private final SparseBooleanArray mNetIdInUse;
    private PowerManager.WakeLock mNetTransitionWakeLock;
    private int mNetTransitionWakeLockTimeout;
    @VisibleForTesting
    protected INetd mNetd;
    @VisibleForTesting
    protected final INetdEventCallback mNetdEventCallback;
    /* access modifiers changed from: private */
    public final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    private final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy({"mNetworkForNetId"})
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    @GuardedBy({"mNetworkForRequestId"})
    private final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private final LocalLog mNetworkInfoBlockingLogs;
    private final LocalLog mNetworkRequestInfoLogs;
    private final HashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    private int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    /* access modifiers changed from: private */
    public NetworkNotificationManager mNotifier;
    private final PowerManager.WakeLock mPendingIntentWakeLock;
    @VisibleForTesting
    protected final PermissionMonitor mPermissionMonitor;
    private final INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    private NetworkPolicyManagerInternal mPolicyManagerInternal;
    private final PriorityDump.PriorityDumper mPriorityDumper;
    private List mProtectedNetworks;
    private final File mProvisioningUrlFile;
    @VisibleForTesting
    protected final ProxyTracker mProxyTracker;
    private final int mReleasePendingIntentDelayMs;
    private boolean mRestrictBackground;
    private final SettingsObserver mSettingsObserver;
    private boolean mShouldKeepCelluarNetwork;
    private INetworkStatsService mStatsService;
    /* access modifiers changed from: private */
    public MockableSystemProperties mSystemProperties;
    private boolean mSystemReady;
    @GuardedBy({"mTNSLock"})
    private TestNetworkService mTNS;
    private final Object mTNSLock;
    private TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public Tethering mTethering;
    private BroadcastReceiver mTetheringReceiver;
    private NtpTrustedTime mTimeOem;
    private int mTotalWakelockAcquisitions;
    private long mTotalWakelockDurationMs;
    private int mTotalWakelockReleases;
    /* access modifiers changed from: private */
    public final NetworkStateTrackerHandler mTrackerHandler;
    private SparseIntArray mUidRules;
    /* access modifiers changed from: private */
    @GuardedBy({"mUidToNetworkRequestCount"})
    public final SparseIntArray mUidToNetworkRequestCount;
    private UserManager mUserManager;
    private BroadcastReceiver mUserPresentReceiver;
    @GuardedBy({"mVpns"})
    @VisibleForTesting
    protected final SparseArray<Vpn> mVpns;
    private final LocalLog mWakelockLogs;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    private enum ReapUnvalidatedNetworks {
        REAP,
        DONT_REAP
    }

    private enum UnneededFor {
        LINGER,
        TEARDOWN
    }

    /* access modifiers changed from: private */
    public static String eventName(int what) {
        return sMagicDecoderRing.get(what, Integer.toString(what));
    }

    private static IDnsResolver getDnsResolver() {
        return IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
    }

    @VisibleForTesting
    static class LegacyTypeTracker {
        private static final boolean DBG = true;
        private static final boolean VDBG = false;
        private final MtkConnectivityService mService;
        private final ArrayList<NetworkAgentInfo>[] mTypeLists = new ArrayList[MtkConnectivityService.EVENT_PROMPT_UNVALIDATED];

        LegacyTypeTracker(MtkConnectivityService service) {
            this.mService = service;
        }

        public void addSupportedType(int type) {
            ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
            if (arrayListArr[type] == null) {
                arrayListArr[type] = new ArrayList<>();
                return;
            }
            throw new IllegalStateException("legacy list for type " + type + "already initialized");
        }

        public boolean isTypeSupported(int type) {
            return (!ConnectivityManager.isNetworkTypeValid(type) || this.mTypeLists[type] == null) ? VDBG : DBG;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
            return null;
         */
        public NetworkAgentInfo getNetworkForType(int type) {
            synchronized (this.mTypeLists) {
                if (isTypeSupported(type) && !this.mTypeLists[type].isEmpty()) {
                    NetworkAgentInfo networkAgentInfo = this.mTypeLists[type].get(0);
                    return networkAgentInfo;
                }
            }
        }

        private void maybeLogBroadcast(NetworkAgentInfo nai, NetworkInfo.DetailedState state, int type, boolean isDefaultNetwork) {
            MtkConnectivityService.log("Sending " + state + " broadcast for type " + type + " " + nai.name() + " isDefaultNetwork=" + isDefaultNetwork);
        }

        public void add(int type, NetworkAgentInfo nai) {
            if (isTypeSupported(type)) {
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                if (!list.contains(nai)) {
                    synchronized (this.mTypeLists) {
                        list.add(nai);
                    }
                    boolean isDefaultNetwork = this.mService.isDefaultNetwork(nai);
                    if (list.size() == 1 || isDefaultNetwork) {
                        maybeLogBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type, isDefaultNetwork);
                        this.mService.sendLegacyNetworkBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
            if (r2 != false) goto L_0x0028;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
            if (r8 == false) goto L_0x0034;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
            maybeLogBroadcast(r7, android.net.NetworkInfo.DetailedState.DISCONNECTED, r6, r8);
            r5.mService.sendLegacyNetworkBroadcast(r7, android.net.NetworkInfo.DetailedState.DISCONNECTED, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
            if (r0.isEmpty() != false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
            if (r2 == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003c, code lost:
            com.android.server.MtkConnectivityService.access$000("Other network available for type " + r6 + ", sending connected broadcast");
            r1 = r0.get(0);
            maybeLogBroadcast(r1, android.net.NetworkInfo.DetailedState.CONNECTED, r6, r5.mService.isDefaultNetwork(r1));
            r5.mService.sendLegacyNetworkBroadcast(r1, android.net.NetworkInfo.DetailedState.CONNECTED, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
            return;
         */
        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = list.get(0).equals(nai);
                synchronized (this.mTypeLists) {
                    if (!list.remove(nai)) {
                    }
                }
            }
        }

        public void remove(NetworkAgentInfo nai, boolean wasDefault) {
            for (int type = 0; type < this.mTypeLists.length; type++) {
                remove(type, nai, wasDefault);
            }
        }

        public void update(NetworkAgentInfo nai) {
            boolean isDefault = this.mService.isDefaultNetwork(nai);
            NetworkInfo.DetailedState state = nai.networkInfo.getDetailedState();
            int type = 0;
            while (true) {
                ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
                if (type < arrayListArr.length) {
                    ArrayList<NetworkAgentInfo> list = arrayListArr[type];
                    boolean isFirst = DBG;
                    boolean contains = list != null && list.contains(nai);
                    if (!contains || nai != list.get(0)) {
                        isFirst = false;
                    }
                    if (isFirst || (contains && isDefault)) {
                        maybeLogBroadcast(nai, state, type, isDefault);
                        this.mService.sendLegacyNetworkBroadcast(nai, state, type);
                    }
                    type++;
                } else {
                    return;
                }
            }
        }

        private String naiToString(NetworkAgentInfo nai) {
            String state;
            String name = nai.name();
            if (nai.networkInfo != null) {
                state = nai.networkInfo.getState() + "/" + nai.networkInfo.getDetailedState();
            } else {
                state = "???/???";
            }
            return name + " " + state;
        }

        public void dump(IndentingPrintWriter pw) {
            pw.println("mLegacyTypeTracker:");
            pw.increaseIndent();
            pw.print("Supported types:");
            int type = 0;
            while (true) {
                ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
                if (type >= arrayListArr.length) {
                    break;
                }
                if (arrayListArr[type] != null) {
                    pw.print(" " + type);
                }
                type++;
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            synchronized (this.mTypeLists) {
                for (int type2 = 0; type2 < this.mTypeLists.length; type2++) {
                    if (this.mTypeLists[type2] != null) {
                        if (!this.mTypeLists[type2].isEmpty()) {
                            Iterator<NetworkAgentInfo> it = this.mTypeLists[type2].iterator();
                            while (it.hasNext()) {
                                pw.println(type2 + " " + naiToString(it.next()));
                            }
                        }
                    }
                }
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println();
        }
    }

    public MtkConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this(context, netManager, statsService, policyManager, getDnsResolver(), new IpConnectivityLog(), NetdService.getInstance());
    }

    /* JADX INFO: Multiple debug info for r4v46 java.lang.String: [D('powerManager' android.os.PowerManager), D('naString' java.lang.String)] */
    @VisibleForTesting
    protected MtkConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager, IDnsResolver dnsresolver, IpConnectivityLog logger, INetd netd) {
        String[] naStrings;
        this.mVpns = new SparseArray<>();
        this.mUidRules = new SparseIntArray();
        this.mDefaultInetConditionPublished = 0;
        this.mTNSLock = new Object();
        this.mNextNetId = 100;
        this.mNextNetworkRequestId = 1;
        this.mNetworkRequestInfoLogs = new LocalLog(20);
        this.mNetworkInfoBlockingLogs = new LocalLog(40);
        this.mWakelockLogs = new LocalLog(20);
        this.mTotalWakelockAcquisitions = 0;
        this.mTotalWakelockReleases = 0;
        this.mTotalWakelockDurationMs = DISABLE_DATA_ALERT_BYTES;
        this.mMaxWakelockDurationMs = DISABLE_DATA_ALERT_BYTES;
        this.mLastWakeLockAcquireTimestamp = DISABLE_DATA_ALERT_BYTES;
        this.mBandwidthRequests = new SparseArray<>(INVALID_SCORE);
        this.mLegacyTypeTracker = new LegacyTypeTracker(this);
        this.hasWifiAssistant = false;
        this.mPriorityDumper = new PriorityDump.PriorityDumper() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass1 */

            public void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                MtkConnectivityService.this.doDump(fd, pw, new String[]{MtkConnectivityService.DIAG_ARG}, asProto);
                MtkConnectivityService.this.doDump(fd, pw, new String[]{MtkConnectivityService.SHORT_ARG}, asProto);
            }

            public void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                MtkConnectivityService.this.doDump(fd, pw, args, asProto);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                MtkConnectivityService.this.doDump(fd, pw, args, asProto);
            }
        };
        this.mDataActivityObserver = new BaseNetworkObserver() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass3 */

            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                MtkConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mNetdEventCallback = new BaseNetdEventCallback() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass4 */

            public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) {
                try {
                    MtkConnectivityService.this.mHandler.sendMessage(MtkConnectivityService.this.mHandler.obtainMessage(MtkConnectivityService.EVENT_PRIVATE_DNS_VALIDATION_UPDATE, new DnsManager.PrivateDnsValidationUpdate(netId, InetAddress.parseNumericAddress(ipAddress), hostname, validated)));
                } catch (IllegalArgumentException e) {
                    MtkConnectivityService.loge("Error parsing ip address in validation event");
                }
            }

            public void onDnsEvent(int netId, int eventType, int returnCode, String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
                NetworkAgentInfo nai = MtkConnectivityService.this.getNetworkAgentInfoForNetId(netId);
                if (nai != null && nai.satisfies(MtkConnectivityService.this.mDefaultRequest)) {
                    nai.networkMonitor().notifyDnsResponse(returnCode);
                }
            }

            public /* synthetic */ void lambda$onNat64PrefixEvent$0$MtkConnectivityService$4(int netId, boolean added, String prefixString, int prefixLength) {
                MtkConnectivityService.this.handleNat64PrefixEvent(netId, added, prefixString, prefixLength);
            }

            public void onNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) {
                MtkConnectivityService.this.mHandler.post(new Runnable(netId, added, prefixString, prefixLength) {
                    /* class com.android.server.$$Lambda$MtkConnectivityService$4$WcJvzs3R8ChRgLBRjZCLx6HZes */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ boolean f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ int f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void run() {
                        MtkConnectivityService.AnonymousClass4.this.lambda$onNat64PrefixEvent$0$MtkConnectivityService$4(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        };
        this.mPolicyListener = new NetworkPolicyManager.Listener() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass5 */

            public void onUidRulesChanged(int uid, int uidRules) {
                MtkConnectivityService.this.mHandler.sendMessage(MtkConnectivityService.this.mHandler.obtainMessage(MtkConnectivityService.EVENT_UID_RULES_CHANGED, uid, uidRules));
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                MtkConnectivityService.log("onRestrictBackgroundChanged(restrictBackground=" + restrictBackground + ")");
                MtkConnectivityService.this.mHandler.sendMessage(MtkConnectivityService.this.mHandler.obtainMessage(40, restrictBackground ? 1 : 0, 0));
                if (restrictBackground) {
                    MtkConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    MtkConnectivityService.this.mTethering.untetherAll();
                }
            }
        };
        this.mProvisioningUrlFile = new File(PROVISIONING_URL_PATH);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass6 */

            public void onReceive(Context context, Intent intent) {
                MtkConnectivityService.this.ensureRunningOnConnectivityServiceThread();
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                Uri packageData = intent.getData();
                String packageName = packageData != null ? packageData.getSchemeSpecificPart() : null;
                if (userId != -10000) {
                    if ("android.intent.action.USER_STARTED".equals(action)) {
                        MtkConnectivityService.this.onUserStart(userId);
                    } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                        MtkConnectivityService.this.onUserStop(userId);
                    } else if ("android.intent.action.USER_ADDED".equals(action)) {
                        MtkConnectivityService.this.onUserAdded(userId);
                    } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                        MtkConnectivityService.this.onUserRemoved(userId);
                    } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                        MtkConnectivityService.this.onUserUnlocked(userId);
                    } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        MtkConnectivityService.this.onPackageAdded(packageName, uid);
                    } else if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
                        MtkConnectivityService.this.onPackageReplaced(packageName, uid);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        MtkConnectivityService.this.onPackageRemoved(packageName, uid, intent.getBooleanExtra("android.intent.extra.REPLACING", false));
                    }
                }
            }
        };
        this.mUserPresentReceiver = new BroadcastReceiver() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass7 */

            public void onReceive(Context context, Intent intent) {
                MtkConnectivityService.this.updateLockdownVpn();
                MtkConnectivityService.this.mContext.unregisterReceiver(this);
            }
        };
        this.mNetworkFactoryInfos = new HashMap<>();
        this.mNetworkRequests = new HashMap<>();
        this.mUidToNetworkRequestCount = new SparseIntArray();
        this.mNetworkForRequestId = new SparseArray<>();
        this.mNetworkForNetId = new SparseArray<>();
        this.mNetIdInUse = new SparseBooleanArray();
        this.mNetworkAgentInfos = new HashMap<>();
        this.mBlockedAppUids = new HashSet<>();
        this.mBlock = false;
        this.mMeasureTime = 0;
        this.mShouldKeepCelluarNetwork = false;
        this.TOTAL_MEASURE_TIME = 3;
        this.RESPONSE_TIMEOUT = 5000;
        this.INTERNAL_SERVERS = new String[]{"conn1.oppomobile.com", "conn2.oppomobile.com", "www.baidu.com"};
        this.EXTERNAL_SERVERS = new String[]{"connectivitycheck.gstatic.com", "www.google.cn", "developers.google.cn"};
        this.RECONNECT_TIMER = 30000;
        this.REMEASURE_TIMER = 1000;
        this.REMEASURE_TIMER_FINE = 300000;
        this.ENABLE_WLAN_ASSISTANT = 1;
        log("ConnectivityService starting up");
        this.mSystemProperties = getSystemProperties();
        this.mMetricsLog = logger;
        this.mDefaultRequest = createDefaultInternetRequestForTransport(-1, NetworkRequest.Type.REQUEST);
        NetworkRequestInfo defaultNRI = new NetworkRequestInfo(null, this.mDefaultRequest, new Binder());
        this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI);
        this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        if (this.mSystemProperties.getBoolean("persist.vendor.op12.ccp.mode", false)) {
            log("isCcpMode enabled, don't assign default networkRequest");
        } else if (sIsAutoTethering) {
            log("Delay default network request until boot is completed");
        } else {
            this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI);
            this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        }
        this.mDefaultMobileDataRequest = createDefaultInternetRequestForTransport(0, NetworkRequest.Type.BACKGROUND_REQUEST);
        this.mDefaultWifiRequest = createDefaultInternetRequestForTransport(1, NetworkRequest.Type.BACKGROUND_REQUEST);
        this.mHandlerThread = new HandlerThread("ConnectivityServiceThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this.mHandlerThread.getLooper());
        if (TextUtils.isEmpty(SystemProperties.get("net.hostname"))) {
            String oppoDhcpHostName = SystemProperties.get("ro.oppo.market.enname", SystemProperties.get("ro.oppo.market.name", "OPPO"));
            oppoDhcpHostName = !TextUtils.isEmpty(oppoDhcpHostName) ? oppoDhcpHostName.replace(" ", "-").replace("\"", "") : oppoDhcpHostName;
            if (!TextUtils.isEmpty(oppoDhcpHostName)) {
                SystemProperties.set("net.hostname", oppoDhcpHostName);
            }
        }
        this.mReleasePendingIntentDelayMs = Settings.Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mLingerDelayMs = this.mSystemProperties.getInt(LINGER_DELAY_PROPERTY, (int) DEFAULT_LINGER_DELAY_MS);
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNMS = (INetworkManagementService) Preconditions.checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) Preconditions.checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) Preconditions.checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mPolicyManagerInternal = (NetworkPolicyManagerInternal) Preconditions.checkNotNull((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class), "missing NetworkPolicyManagerInternal");
        this.mDnsResolver = (IDnsResolver) Preconditions.checkNotNull(dnsresolver, "missing IDnsResolver");
        this.mProxyTracker = makeProxyTracker();
        this.mNetd = netd;
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694856);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetConfigs = new NetworkConfig[EVENT_PROMPT_UNVALIDATED];
        boolean wifiOnly = this.mSystemProperties.getBoolean("ro.radio.noril", false);
        log("wifiOnly=" + wifiOnly);
        String[] naStrings2 = context.getResources().getStringArray(17236103);
        int length = naStrings2.length;
        int i = 0;
        while (i < length) {
            String naString = naStrings2[i];
            try {
                NetworkConfig n = new NetworkConfig(naString);
                if (VDBG) {
                    naStrings = naStrings2;
                    try {
                        log("naString=" + naString + " config=" + n);
                    } catch (Exception e2) {
                    }
                } else {
                    naStrings = naStrings2;
                }
                if (n.type > EVENT_SET_ACCEPT_UNVALIDATED) {
                    loge("Error in networkAttributes - ignoring attempt to define type " + n.type);
                } else if (wifiOnly && ConnectivityManager.isNetworkTypeMobile(n.type)) {
                    log("networkAttributes - ignoring mobile as this dev is wifiOnly " + n.type);
                } else if (this.mNetConfigs[n.type] != null) {
                    loge("Error in networkAttributes - ignoring attempt to redefine type " + n.type);
                } else {
                    this.mLegacyTypeTracker.addSupportedType(n.type);
                    this.mNetConfigs[n.type] = n;
                    this.mNetworksDefined++;
                }
            } catch (Exception e3) {
                naStrings = naStrings2;
            }
            i++;
            powerManager = powerManager;
            naStrings2 = naStrings;
        }
        if (this.mNetConfigs[EVENT_REGISTER_NETWORK_FACTORY] == null) {
            this.mLegacyTypeTracker.addSupportedType(EVENT_REGISTER_NETWORK_FACTORY);
            this.mNetworksDefined++;
        }
        if (this.mNetConfigs[EVENT_APPLY_GLOBAL_HTTP_PROXY] == null && hasService("ethernet")) {
            this.mLegacyTypeTracker.addSupportedType(EVENT_APPLY_GLOBAL_HTTP_PROXY);
            this.mNetworksDefined++;
        }
        if (VDBG) {
            log("mNetworksDefined=" + this.mNetworksDefined);
        }
        this.mProtectedNetworks = new ArrayList();
        int[] protectedNetworks = context.getResources().getIntArray(17236057);
        for (int p : protectedNetworks) {
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        this.mTethering = makeTethering();
        this.mPermissionMonitor = new PermissionMonitor(this.mContext, this.mNetd);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, this.mHandler);
        this.mContext.registerReceiverAsUser(this.mUserPresentReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_PRESENT"), null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter2, null, this.mHandler);
        IntentFilter filterC = new IntentFilter();
        filterC.addAction(AUTO_TETHERING_INTENT);
        if (sIsAutoTethering) {
            filterC.addAction("android.intent.action.BOOT_COMPLETED");
        }
        filterC.addAction(ACTION_DISABLE_DATA_ALERT);
        this.mTetheringReceiver = new ConnectivityServiceReceiver();
        this.mContext.registerReceiver(this.mTetheringReceiver, filterC);
        String dns = this.mContext.getResources().getString(17039715);
        try {
            this.mDefaultDns = NetworkUtils.numericToInetAddress(dns);
            if (VDBG) {
                log("mDefaultDns:" + this.mDefaultDns);
            }
        } catch (IllegalArgumentException e4) {
            loge("Error setting defaultDns using " + dns);
        }
        try {
            this.mNMS.registerObserver(this.mTethering);
            this.mNMS.registerObserver(this.mDataActivityObserver);
        } catch (RemoteException e5) {
            loge("Error registering observer :" + e5);
        }
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerSettingsCallbacks();
        new DataConnectionStats(this.mContext).startMonitoring();
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mKeepaliveTracker = new KeepaliveTracker(this.mContext, this.mHandler);
        Context context2 = this.mContext;
        this.mNotifier = new NetworkNotificationManager(context2, this.mTelephonyManager, (NotificationManager) context2.getSystemService(NotificationManager.class));
        this.mLingerMonitor = new LingerMonitor(this.mContext, this.mNotifier, Settings.Global.getInt(this.mContext.getContentResolver(), "network_switch_notification_daily_limit", 3), Settings.Global.getLong(this.mContext.getContentResolver(), "network_switch_notification_rate_limit_millis", 60000));
        this.mMultinetworkPolicyTracker = createMultinetworkPolicyTracker(this.mContext, this.mHandler, new Runnable() {
            /* class com.android.server.$$Lambda$MtkConnectivityService$wkRLcspZsFlF9yMhb21EbvtHd0 */

            public final void run() {
                MtkConnectivityService.this.lambda$new$0$MtkConnectivityService();
            }
        });
        this.mMultinetworkPolicyTracker.start();
        this.mMultipathPolicyTracker = new MultipathPolicyTracker(this.mContext, this.mHandler);
        this.mDnsManager = new DnsManager(this.mContext, this.mDnsResolver, this.mSystemProperties);
        registerPrivateDnsSettingsCallbacks();
        this.mCsHelper = new OppoConnectivityServiceHelper(context, netManager, this.mDnsManager, netd, this);
        this.mAutoConCaptivePortalControl = new OppoAutoConCaptivePortalControl(this.mContext, powerManager, this.mNotifier);
        this.mDualWifiSta2CaptivePortalControl = new OppoDualWifiSta2CaptivePortalControl(this.mContext, powerManager, this.mNotifier);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        ipv6CellulorInit();
        mMtkTethering = new MtkTethering(this.mContext, this.mTethering);
        Context context3 = this.mContext;
        if (context3 != null) {
            this.mTimeOem = NtpTrustedTime.getInstance(context3);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Tethering makeTethering() {
        return new Tethering(this.mContext, this.mNMS, this.mStatsService, this.mPolicyManager, IoThread.get().getLooper(), new MockableSystemProperties(), new TetheringDependencies() {
            /* class com.android.server.MtkConnectivityService.AnonymousClass2 */

            public boolean isTetheringSupported() {
                return MtkConnectivityService.this.isTetheringSupported();
            }

            public NetworkRequest getDefaultNetworkRequest() {
                return MtkConnectivityService.this.mDefaultRequest;
            }
        });
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ProxyTracker makeProxyTracker() {
        return new ProxyTracker(this.mContext, this.mHandler, (int) EVENT_PROXY_HAS_CHANGED);
    }

    private static NetworkCapabilities createDefaultNetworkCapabilitiesForUid(int uid) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        netCap.removeCapability(15);
        netCap.setSingleUid(uid);
        return netCap;
    }

    private NetworkRequest createDefaultInternetRequestForTransport(int transportType, NetworkRequest.Type type) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        if (transportType > -1) {
            netCap.addTransportType(transportType);
        }
        return new NetworkRequest(netCap, -1, nextNetworkRequestId(), type);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateAlwaysOnNetworks() {
        this.mHandler.sendEmptyMessage(EVENT_CONFIGURE_ALWAYS_ON_NETWORKS);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePrivateDnsSettings() {
        this.mHandler.sendEmptyMessage(EVENT_PRIVATE_DNS_SETTINGS_CHANGED);
    }

    private void handleAlwaysOnNetworkRequest(NetworkRequest networkRequest, String settingName, boolean defaultValue) {
        boolean enable = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), settingName, encodeBool(defaultValue)));
        if (enable != (this.mNetworkRequests.get(networkRequest) != null)) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, networkRequest, new Binder()));
            } else {
                handleReleaseNetworkRequest(networkRequest, 1000, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleConfigureAlwaysOnNetworks() {
        handleAlwaysOnNetworkRequest(this.mDefaultMobileDataRequest, "mobile_data_always_on", true);
        handleAlwaysOnNetworkRequest(this.mDefaultWifiRequest, "wifi_always_requested", false);
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Settings.Global.getUriFor("http_proxy"), EVENT_APPLY_GLOBAL_HTTP_PROXY);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("mobile_data_always_on"), EVENT_CONFIGURE_ALWAYS_ON_NETWORKS);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("wifi_always_requested"), EVENT_CONFIGURE_ALWAYS_ON_NETWORKS);
    }

    private void registerPrivateDnsSettingsCallbacks() {
        for (Uri uri : DnsManager.getPrivateDnsSettingsUris()) {
            this.mSettingsObserver.observe(uri, EVENT_PRIVATE_DNS_SETTINGS_CHANGED);
        }
    }

    private synchronized int nextNetworkRequestId() {
        int i;
        i = this.mNextNetworkRequestId;
        this.mNextNetworkRequestId = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int reserveNetId() {
        synchronized (this.mNetworkForNetId) {
            for (int i = 100; i <= MAX_NET_ID; i++) {
                int netId = this.mNextNetId;
                int i2 = this.mNextNetId + 1;
                this.mNextNetId = i2;
                if (i2 > MAX_NET_ID) {
                    this.mNextNetId = 100;
                }
                if (!this.mNetIdInUse.get(netId)) {
                    this.mNetIdInUse.put(netId, true);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid) {
        NetworkState state;
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            NetworkInfo info = new NetworkInfo(networkType, 0, ConnectivityManager.getNetworkTypeName(networkType), "");
            info.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
            info.setIsAvailable(true);
            NetworkCapabilities capabilities = new NetworkCapabilities();
            capabilities.setCapability(EVENT_REGISTER_NETWORK_AGENT, true ^ info.isRoaming());
            state = new NetworkState(info, new LinkProperties(), capabilities, (Network) null, (String) null, (String) null);
        }
        filterNetworkStateForUid(state, uid, false);
        return state;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public NetworkAgentInfo getNetworkAgentInfoForNetwork(Network network) {
        if (network == null) {
            return null;
        }
        return getNetworkAgentInfoForNetId(network.netId);
    }

    /* access modifiers changed from: private */
    public NetworkAgentInfo getNetworkAgentInfoForNetId(int netId) {
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForNetId) {
            networkAgentInfo = this.mNetworkForNetId.get(netId);
        }
        return networkAgentInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        return null;
     */
    private Network[] getVpnUnderlyingNetworks(int uid) {
        Vpn vpn;
        synchronized (this.mVpns) {
            if (!this.mLockdownEnabled && (vpn = this.mVpns.get(UserHandle.getUserId(uid))) != null && vpn.appliesToUid(uid)) {
                Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
                return underlyingNetworks;
            }
        }
    }

    private NetworkState getUnfilteredActiveNetworkState(int uid) {
        NetworkAgentInfo nai = getDefaultNetwork();
        Network[] networks = getVpnUnderlyingNetworks(uid);
        if (networks != null) {
            if (networks.length > 0) {
                nai = getNetworkAgentInfoForNetwork(networks[0]);
            } else {
                nai = null;
            }
        }
        if (nai != null) {
            return nai.getNetworkState();
        }
        return NetworkState.EMPTY;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002c, code lost:
        if (r4 != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
        r0 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
        r0 = r4.getInterfaceName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003b, code lost:
        return r3.mPolicyManagerInternal.isUidNetworkingBlocked(r5, r0);
     */
    private boolean isNetworkWithLinkPropertiesBlocked(LinkProperties lp, int uid, boolean ignoreBlocked) {
        if (ignoreBlocked || isSystem(uid)) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn != null && vpn.getLockdown() && vpn.isBlockingUid(uid)) {
                return true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002f, code lost:
        if (r1 == false) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        r0 = "BLOCKED";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        r0 = "UNBLOCKED";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
        log(java.lang.String.format("Returning %s NetworkInfo to uid=%d", r0, java.lang.Integer.valueOf(r7)));
        r2 = r5.mNetworkInfoBlockingLogs;
        r2.log(r0 + " " + r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0065, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0067, code lost:
        return;
     */
    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        boolean blocked;
        if (ni != null) {
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == NetworkInfo.DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    blocked = true;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    blocked = false;
                }
            }
        }
    }

    private void maybeLogBlockedStatusChanged(NetworkRequestInfo nri, Network net, boolean blocked) {
        if (nri != null && net != null) {
            String action = blocked ? "BLOCKED" : "UNBLOCKED";
            log(String.format("Blocked status changed to %s for %d(%d) on netId %d", Boolean.valueOf(blocked), Integer.valueOf(nri.mUid), Integer.valueOf(nri.request.requestId), Integer.valueOf(net.netId)));
            LocalLog localLog = this.mNetworkInfoBlockingLogs;
            localLog.log(action + " " + nri.mUid);
        }
    }

    private void filterNetworkStateForUid(NetworkState state, int uid, boolean ignoreBlocked) {
        if (state != null && state.networkInfo != null && state.linkProperties != null) {
            if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, ignoreBlocked)) {
                state.networkInfo.setDetailedState(NetworkInfo.DetailedState.BLOCKED, null, null);
            }
            synchronized (this.mVpns) {
                if (this.mLockdownTracker != null) {
                    this.mLockdownTracker.augmentNetworkInfo(state.networkInfo);
                }
            }
        }
    }

    public NetworkInfo getActiveNetworkInfo() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        NetworkState state = getUnfilteredActiveNetworkState(uid);
        filterNetworkStateForUid(state, uid, false);
        maybeLogBlockedNetworkInfo(state.networkInfo, uid);
        return state.networkInfo;
    }

    public Network getActiveNetwork() {
        enforceAccessPermission();
        return getActiveNetworkForUidInternal(Binder.getCallingUid(), false);
    }

    public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) {
        enforceConnectivityInternalPermission();
        return getActiveNetworkForUidInternal(uid, ignoreBlocked);
    }

    private Network getActiveNetworkForUidInternal(int uid, boolean ignoreBlocked) {
        NetworkAgentInfo nai;
        int user = UserHandle.getUserId(uid);
        int vpnNetId = 0;
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(user);
            if (vpn != null && vpn.appliesToUid(uid)) {
                vpnNetId = vpn.getNetId();
            }
        }
        if (vpnNetId != 0 && (nai = getNetworkAgentInfoForNetId(vpnNetId)) != null && createDefaultNetworkCapabilitiesForUid(uid).satisfiedByNetworkCapabilities(nai.networkCapabilities)) {
            return nai.network;
        }
        NetworkAgentInfo nai2 = getDefaultNetwork();
        if (nai2 != null && isNetworkWithLinkPropertiesBlocked(nai2.linkProperties, uid, ignoreBlocked)) {
            nai2 = null;
        }
        if (nai2 != null) {
            return nai2.network;
        }
        return null;
    }

    public NetworkInfo getActiveNetworkInfoUnfiltered() {
        enforceAccessPermission();
        return getUnfilteredActiveNetworkState(Binder.getCallingUid()).networkInfo;
    }

    public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) {
        enforceConnectivityInternalPermission();
        NetworkState state = getUnfilteredActiveNetworkState(uid);
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state.networkInfo;
    }

    public NetworkInfo getNetworkInfo(int networkType) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (getVpnUnderlyingNetworks(uid) != null) {
            NetworkState state = getUnfilteredActiveNetworkState(uid);
            if (state.networkInfo != null && state.networkInfo.getType() == networkType) {
                filterNetworkStateForUid(state, uid, false);
                return state.networkInfo;
            }
        }
        return getFilteredNetworkState(networkType, uid).networkInfo;
    }

    public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null) {
            return null;
        }
        NetworkState state = nai.getNetworkState();
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state.networkInfo;
    }

    public NetworkInfo[] getAllNetworkInfo() {
        enforceAccessPermission();
        ArrayList<NetworkInfo> result = Lists.newArrayList();
        for (int networkType = 0; networkType <= EVENT_SET_ACCEPT_UNVALIDATED; networkType++) {
            NetworkInfo info = getNetworkInfo(networkType);
            if (info != null) {
                result.add(info);
            }
        }
        return (NetworkInfo[]) result.toArray(new NetworkInfo[result.size()]);
    }

    public Network getNetworkForType(int networkType) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        NetworkState state = getFilteredNetworkState(networkType, uid);
        if (!isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, false)) {
            return state.network;
        }
        return null;
    }

    public Network[] getAllNetworks() {
        Network[] result;
        enforceAccessPermission();
        synchronized (this.mNetworkForNetId) {
            result = new Network[this.mNetworkForNetId.size()];
            for (int i = 0; i < this.mNetworkForNetId.size(); i++) {
                result[i] = this.mNetworkForNetId.valueAt(i).network;
            }
        }
        return result;
    }

    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        Vpn vpn;
        Network[] networks;
        enforceAccessPermission();
        HashMap<Network, NetworkCapabilities> result = new HashMap<>();
        NetworkAgentInfo nai = getDefaultNetwork();
        NetworkCapabilities nc = getNetworkCapabilitiesInternal(nai);
        if (nc != null) {
            result.put(nai.network, nc);
        }
        synchronized (this.mVpns) {
            if (!(this.mLockdownEnabled || (vpn = this.mVpns.get(userId)) == null || (networks = vpn.getUnderlyingNetworks()) == null)) {
                for (Network network : networks) {
                    NetworkCapabilities nc2 = getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
                    if (nc2 != null) {
                        result.put(network, nc2);
                    }
                }
            }
        }
        return (NetworkCapabilities[]) result.values().toArray(new NetworkCapabilities[result.size()]);
    }

    public boolean isNetworkSupported(int networkType) {
        enforceAccessPermission();
        return this.mLegacyTypeTracker.isTypeSupported(networkType);
    }

    public LinkProperties getActiveLinkProperties() {
        enforceAccessPermission();
        return getUnfilteredActiveNetworkState(Binder.getCallingUid()).linkProperties;
    }

    public LinkProperties getLinkPropertiesForType(int networkType) {
        LinkProperties linkProperties;
        enforceAccessPermission();
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    public LinkProperties getLinkProperties(Network network) {
        enforceAccessPermission();
        return getLinkProperties(getNetworkAgentInfoForNetwork(network));
    }

    private LinkProperties getLinkProperties(NetworkAgentInfo nai) {
        LinkProperties linkProperties;
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    private NetworkCapabilities getNetworkCapabilitiesInternal(NetworkAgentInfo nai) {
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            if (nai.networkCapabilities == null) {
                return null;
            }
            NetworkCapabilities networkCapabilitiesRestrictedForCallerPermissions = networkCapabilitiesRestrictedForCallerPermissions(nai.networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
            return networkCapabilitiesRestrictedForCallerPermissions;
        }
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        enforceAccessPermission();
        return getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
    }

    private NetworkCapabilities networkCapabilitiesRestrictedForCallerPermissions(NetworkCapabilities nc, int callerPid, int callerUid) {
        NetworkCapabilities newNc = new NetworkCapabilities(nc);
        if (!checkSettingsPermission(callerPid, callerUid)) {
            newNc.setUids(null);
            newNc.setSSID(null);
        }
        if (newNc.getNetworkSpecifier() != null) {
            newNc.setNetworkSpecifier(newNc.getNetworkSpecifier().redact());
        }
        return newNc;
    }

    private void restrictRequestUidsForCaller(NetworkCapabilities nc) {
        if (!checkSettingsPermission()) {
            nc.setSingleUid(Binder.getCallingUid());
        }
    }

    private void restrictBackgroundRequestForCaller(NetworkCapabilities nc) {
        if (!this.mPermissionMonitor.hasUseBackgroundNetworksPermission(Binder.getCallingUid())) {
            nc.addCapability(EVENT_REGISTER_NETWORK_REQUEST);
        }
    }

    public NetworkState[] getAllNetworkState() {
        enforceConnectivityInternalPermission();
        ArrayList<NetworkState> result = Lists.newArrayList();
        for (Network network : getAllNetworks()) {
            NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
            if (nai != null) {
                result.add(nai.getNetworkState());
            }
        }
        return (NetworkState[]) result.toArray(new NetworkState[result.size()]);
    }

    @Deprecated
    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        String str = TAG;
        Log.w(str, "Shame on UID " + Binder.getCallingUid() + " for calling the hidden API getNetworkQuotaInfo(). Shame!");
        return new NetworkQuotaInfo();
    }

    public boolean isActiveNetworkMetered() {
        enforceAccessPermission();
        NetworkCapabilities caps = getNetworkCapabilities(getActiveNetwork());
        if (caps != null) {
            return true ^ caps.hasCapability(11);
        }
        return true;
    }

    private boolean disallowedBecauseSystemCaller() {
        if (!isSystem(Binder.getCallingUid()) || SystemProperties.getInt("ro.product.first_api_level", 0) <= EVENT_SET_ACCEPT_UNVALIDATED) {
            return false;
        }
        log("This method exists only for app backwards compatibility and must not be called by system services.");
        return true;
    }

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        NetworkInfo.DetailedState netState;
        LinkProperties lp;
        int netId;
        if (disallowedBecauseSystemCaller()) {
            return false;
        }
        enforceChangePermission();
        if (this.mProtectedNetworks.contains(Integer.valueOf(networkType))) {
            enforceConnectivityInternalPermission();
        }
        try {
            InetAddress addr = InetAddress.getByAddress(hostAddress);
            if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
                log("requestRouteToHostAddress on invalid network: " + networkType);
                return false;
            }
            NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
            if (nai == null) {
                if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
                    log("requestRouteToHostAddress on unsupported network: " + networkType);
                } else {
                    log("requestRouteToHostAddress on down network: " + networkType);
                }
                return false;
            }
            synchronized (nai) {
                netState = nai.networkInfo.getDetailedState();
            }
            if (netState == NetworkInfo.DetailedState.CONNECTED || netState == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                int uid = Binder.getCallingUid();
                long token = Binder.clearCallingIdentity();
                try {
                    synchronized (nai) {
                        lp = nai.linkProperties;
                        netId = nai.network.netId;
                    }
                    boolean ok = addLegacyRouteToHost(lp, addr, netId, uid);
                    log("requestRouteToHostAddress ok=" + ok);
                    return ok;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                if (VDBG) {
                    log("requestRouteToHostAddress on down network (" + networkType + ") - dropped netState=" + netState);
                }
                return false;
            }
        } catch (UnknownHostException e) {
            log("requestRouteToHostAddress got " + e.toString());
            return false;
        }
    }

    private boolean addLegacyRouteToHost(LinkProperties lp, InetAddress addr, int netId, int uid) {
        RouteInfo bestRoute;
        RouteInfo bestRoute2 = RouteInfo.selectBestRoute(lp.getAllRoutes(), addr);
        if (bestRoute2 == null) {
            bestRoute = RouteInfo.makeHostRoute(addr, lp.getInterfaceName());
        } else {
            String iface = bestRoute2.getInterface();
            if (bestRoute2.getGateway().equals(addr)) {
                bestRoute = RouteInfo.makeHostRoute(addr, iface);
            } else {
                bestRoute = RouteInfo.makeHostRoute(addr, bestRoute2.getGateway(), iface);
            }
        }
        log("Adding legacy route " + bestRoute + " for UID/PID " + uid + "/" + Binder.getCallingPid());
        try {
            this.mNMS.addLegacyRouteForNetId(netId, bestRoute, uid);
            return true;
        } catch (Exception e) {
            loge("Exception trying to add a route: " + e);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void registerNetdEventCallback() {
        IIpConnectivityMetrics ipConnectivityMetrics = IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("connmetrics"));
        if (ipConnectivityMetrics == null) {
            Slog.wtf(TAG, "Missing IIpConnectivityMetrics");
            return;
        }
        try {
            ipConnectivityMetrics.addNetdEventCallback(0, this.mNetdEventCallback);
        } catch (Exception e) {
            loge("Error registering netd callback: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidRulesChanged(int uid, int newRules) {
        if (this.mUidRules.get(uid, 0) != newRules) {
            maybeNotifyNetworkBlockedForNewUidRules(uid, newRules);
            if (newRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newRules);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRestrictBackgroundChanged(boolean restrictBackground) {
        if (this.mRestrictBackground != restrictBackground) {
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                boolean curMetered = nai.networkCapabilities.isMetered();
                maybeNotifyNetworkBlocked(nai, curMetered, curMetered, this.mRestrictBackground, restrictBackground);
            }
            this.mRestrictBackground = restrictBackground;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        r0 = r3.mPolicyManagerInternal;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        return com.android.server.net.NetworkPolicyManagerInternal.isUidNetworkingBlocked(r4, r5, r6, r7);
     */
    private boolean isUidNetworkingWithVpnBlocked(int uid, int uidRules, boolean isNetworkMetered, boolean isBackgroundRestricted) {
        if (isSystem(uid)) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn != null && vpn.getLockdown() && vpn.isBlockingUid(uid)) {
                return true;
            }
        }
    }

    private void enforceCrossUserPermission(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
    }

    private boolean checkAnyPermissionOf(String... permissions) {
        for (String permission : permissions) {
            if (this.mContext.checkCallingOrSelfPermission(permission) == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAnyPermissionOf(int pid, int uid, String... permissions) {
        for (String permission : permissions) {
            if (this.mContext.checkPermission(permission, pid, uid) == 0) {
                return true;
            }
        }
        return false;
    }

    private void enforceAnyPermissionOf(String... permissions) {
        if (!checkAnyPermissionOf(permissions)) {
            throw new SecurityException("Requires one of the following permissions: " + String.join(", ", permissions) + ".");
        }
    }

    private void enforceInternetPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", "ConnectivityService");
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceChangePermission() {
        ConnectivityManager.enforceChangePermission(this.mContext);
    }

    /* access modifiers changed from: private */
    public void enforceSettingsPermission() {
        enforceAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkSettingsPermission() {
        return checkAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0 || this.mContext.checkPermission("android.permission.MAINLINE_NETWORK_STACK", pid, uid) == 0;
    }

    private void enforceTetherAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceConnectivityInternalPermission() {
        enforceAnyPermissionOf("android.permission.CONNECTIVITY_INTERNAL", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private void enforceControlAlwaysOnVpnPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_ALWAYS_ON_VPN", "ConnectivityService");
    }

    private void enforceNetworkStackSettingsOrSetup() {
        enforceAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.NETWORK_SETUP_WIZARD", "android.permission.NETWORK_STACK", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkNetworkStackPermission() {
        return checkAnyPermissionOf("android.permission.NETWORK_STACK", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkNetworkSignalStrengthWakeupPermission(int pid, int uid) {
        return checkAnyPermissionOf(pid, uid, "android.permission.NETWORK_SIGNAL_STRENGTH_WAKEUP", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private void enforceConnectivityRestrictedNetworksPermission() {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS", "ConnectivityService");
        } catch (SecurityException e) {
            enforceConnectivityInternalPermission();
        }
    }

    private void enforceKeepalivePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.PACKET_KEEPALIVE_OFFLOAD", "ConnectivityService");
    }

    public void sendConnectedBroadcast(NetworkInfo info) {
        enforceConnectivityInternalPermission();
        sendGeneralBroadcast(info, "android.net.conn.CONNECTIVITY_CHANGE");
    }

    private void sendInetConditionBroadcast(NetworkInfo info) {
        sendGeneralBroadcast(info, "android.net.conn.INET_CONDITION_ACTION");
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    private Intent makeGeneralIntent(NetworkInfo info, String bcastType) {
        synchronized (this.mVpns) {
            if (this.mLockdownTracker != null) {
                info = new NetworkInfo(info);
                this.mLockdownTracker.augmentNetworkInfo(info);
            }
        }
        Intent intent = new Intent(bcastType);
        intent.putExtra("networkInfo", new NetworkInfo(info));
        intent.putExtra("networkType", info.getType());
        if (info.isFailover()) {
            intent.putExtra("isFailover", true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra("reason", info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra("extraInfo", info.getExtraInfo());
        }
        intent.putExtra("inetCondition", this.mDefaultInetConditionPublished);
        return intent;
    }

    private void sendGeneralBroadcast(NetworkInfo info, String bcastType) {
        sendStickyBroadcast(makeGeneralIntent(info, bcastType));
    }

    /* access modifiers changed from: private */
    public void sendDataActivityBroadcast(int deviceType, boolean active, long tsNanos) {
        Intent intent = new Intent("android.net.conn.DATA_ACTIVITY_CHANGE");
        intent.putExtra("deviceType", deviceType);
        intent.putExtra("isActive", active);
        intent.putExtra("tsNanos", tsNanos);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_DATA_ACTIVITY_CHANGE", null, null, 0, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendStickyBroadcast(Intent intent) {
        synchronized (this) {
            if (!this.mSystemReady && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                this.mInitialBroadcast = new Intent(intent);
            }
            intent.addFlags(67108864);
            if (VDBG) {
                log("sendStickyBroadcast: action=" + intent.getAction());
            }
            Bundle options = null;
            long ident = Binder.clearCallingIdentity();
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (ni.getType() == 3) {
                    intent.setAction("android.net.conn.CONNECTIVITY_CHANGE_SUPL");
                    intent.addFlags(1073741824);
                } else {
                    BroadcastOptions opts = BroadcastOptions.makeBasic();
                    opts.setMaxManifestReceiverApiLevel((int) EVENT_UNREGISTER_NETWORK_FACTORY);
                    options = opts.toBundle();
                }
                try {
                    BatteryStatsService.getService().noteConnectivityChanged(intent.getIntExtra("networkType", -1), ni.getState().toString());
                } catch (RemoteException e) {
                }
                intent.addFlags(2097152);
            }
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL, options);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void systemReady() {
        this.mProxyTracker.loadGlobalProxy();
        registerNetdEventCallback();
        this.mTethering.systemReady();
        synchronized (this) {
            this.mSystemReady = true;
            if (this.mInitialBroadcast != null) {
                this.mContext.sendStickyBroadcastAsUser(this.mInitialBroadcast, UserHandle.ALL);
                this.mInitialBroadcast = null;
            }
        }
        updateLockdownVpn();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_CONFIGURE_ALWAYS_ON_NETWORKS));
        InternalHandler internalHandler2 = this.mHandler;
        internalHandler2.sendMessage(internalHandler2.obtainMessage(EVENT_SYSTEM_READY));
        this.mPermissionMonitor.startMonitoring();
    }

    private void setupDataActivityTracking(NetworkAgentInfo networkAgent) {
        int timeout;
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        if (networkAgent.networkCapabilities.hasTransport(0)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", INVALID_SCORE);
            type = 0;
        } else if (networkAgent.networkCapabilities.hasTransport(1) || networkAgent.networkCapabilities.hasTransport(EVENT_CLEAR_NET_TRANSITION_WAKELOCK)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", 15);
            type = 1;
        } else {
            timeout = 0;
        }
        if (timeout > 0 && iface != null && type != -1) {
            try {
                this.mNMS.addIdleTimer(iface, timeout, type);
            } catch (Exception e) {
                loge("Exception in setupDataActivityTracking " + e);
            }
        }
    }

    private void removeDataActivityTracking(NetworkAgentInfo networkAgent) {
        String iface = networkAgent.linkProperties.getInterfaceName();
        NetworkCapabilities caps = networkAgent.networkCapabilities;
        if (iface == null) {
            return;
        }
        if (caps.hasTransport(0) || caps.hasTransport(1) || caps.hasTransport(EVENT_CLEAR_NET_TRANSITION_WAKELOCK)) {
            try {
                this.mNMS.removeIdleTimer(iface);
            } catch (Exception e) {
                loge("Exception in removeDataActivityTracking " + e);
            }
        }
    }

    private void updateDataActivityTracking(NetworkAgentInfo newNetwork, NetworkAgentInfo oldNetwork) {
        if (newNetwork != null) {
            setupDataActivityTracking(newNetwork);
        }
        if (oldNetwork != null) {
            removeDataActivityTracking(oldNetwork);
        }
    }

    private void updateMtu(LinkProperties newLp, LinkProperties oldLp) {
        String iface = newLp.getInterfaceName();
        int mtu = newLp.getMtu();
        if (oldLp != null || mtu != 0) {
            if (oldLp == null || !newLp.isIdenticalMtu(oldLp)) {
                if (!LinkProperties.isValidMtu(mtu, newLp.hasGlobalIpv6Address())) {
                    if (mtu != 0) {
                        loge("Unexpected mtu value: " + mtu + ", " + iface);
                    }
                } else if (TextUtils.isEmpty(iface)) {
                    loge("Setting MTU size with null iface.");
                } else {
                    try {
                        if (VDBG || DDBG) {
                            log("Setting MTU size: " + iface + ", " + mtu);
                        }
                        this.mNMS.setMtu(iface, mtu);
                    } catch (Exception e) {
                        String str = TAG;
                        Slog.e(str, "exception in setMtu()" + e);
                    }
                }
            } else if (VDBG) {
                log("identical MTU - not setting");
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public MockableSystemProperties getSystemProperties() {
        return new MockableSystemProperties();
    }

    private void updateTcpBufferSizes(String tcpBufferSizes) {
        String[] values = null;
        if (tcpBufferSizes != null) {
            values = tcpBufferSizes.split(",");
        }
        if (values == null || values.length != 6) {
            log("Invalid tcpBufferSizes string: " + tcpBufferSizes + ", using defaults");
            tcpBufferSizes = DEFAULT_TCP_BUFFER_SIZES;
            values = tcpBufferSizes.split(",");
        }
        if (!tcpBufferSizes.equals(this.mCurrentTcpBufferSizes)) {
            try {
                if (VDBG || DDBG) {
                    String str = TAG;
                    Slog.d(str, "Setting tx/rx TCP buffers to " + tcpBufferSizes);
                }
                this.mNetd.setTcpRWmemorySize(String.join(" ", values[0], values[1], values[2]), String.join(" ", values[3], values[4], values[MAX_CELLULAR_INTERFACE]));
                this.mCurrentTcpBufferSizes = tcpBufferSizes;
            } catch (RemoteException | ServiceSpecificException e) {
                loge("Can't set TCP buffer sizes:" + e);
            }
            Integer rwndValue = Integer.valueOf(Settings.Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", this.mSystemProperties.getInt(DEFAULT_TCP_RWND_KEY, 0)));
            if (rwndValue.intValue() != 0) {
                try {
                    this.mSystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
                } catch (RuntimeException runtimeException) {
                    loge("Can't set property  sys.sysctl.tcp_def_init_rwnd:" + runtimeException);
                }
            }
        }
    }

    public int getRestoreDefaultNetworkDelay(int networkType) {
        String restoreDefaultNetworkDelayStr = this.mSystemProperties.get(NETWORK_RESTORE_DELAY_PROP_NAME);
        if (!(restoreDefaultNetworkDelayStr == null || restoreDefaultNetworkDelayStr.length() == 0)) {
            try {
                return Integer.parseInt(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        if (networkType > EVENT_SET_ACCEPT_UNVALIDATED) {
            return RESTORE_DEFAULT_NETWORK_DELAY;
        }
        NetworkConfig[] networkConfigArr = this.mNetConfigs;
        if (networkConfigArr[networkType] != null) {
            return networkConfigArr[networkType].restoreTime;
        }
        return RESTORE_DEFAULT_NETWORK_DELAY;
    }

    private void dumpNetworkDiagnostics(IndentingPrintWriter pw) {
        List<NetworkDiagnostics> netDiags = new ArrayList<>();
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            netDiags.add(new NetworkDiagnostics(nai.network, new LinkProperties(nai.linkProperties), 5000));
        }
        for (NetworkDiagnostics netDiag : netDiags) {
            pw.println();
            netDiag.waitForMeasurements();
            netDiag.dump(pw);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        PriorityDump.dump(this.mPriorityDumper, fd, writer, args);
    }

    /* access modifiers changed from: private */
    public void doDump(FileDescriptor fd, PrintWriter writer, String[] args, boolean asProto) {
        int i;
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (!DumpUtils.checkDumpPermission(this.mContext, TAG, pw) || asProto) {
            return;
        }
        if (ArrayUtils.contains(args, DIAG_ARG)) {
            dumpNetworkDiagnostics(pw);
        } else if (ArrayUtils.contains(args, TETHERING_ARG)) {
            this.mTethering.dump(fd, pw, args);
        } else if (ArrayUtils.contains(args, NETWORK_ARG)) {
            dumpNetworks(pw);
        } else if (ArrayUtils.contains(args, REQUEST_ARG)) {
            dumpNetworkRequests(pw);
        } else {
            pw.print("NetworkFactories for:");
            Iterator<NetworkFactoryInfo> it = this.mNetworkFactoryInfos.values().iterator();
            while (it.hasNext()) {
                pw.print(" " + it.next().name);
            }
            pw.println();
            pw.println();
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            pw.print("Active default network: ");
            if (defaultNai == null) {
                pw.println("none");
            } else {
                pw.println(defaultNai.network.netId);
            }
            pw.println();
            pw.println("Current Networks:");
            pw.increaseIndent();
            dumpNetworks(pw);
            pw.decreaseIndent();
            pw.println();
            pw.print("Restrict background: ");
            pw.println(this.mRestrictBackground);
            pw.println();
            pw.println("Status for known UIDs:");
            pw.increaseIndent();
            int size = this.mUidRules.size();
            int i2 = 0;
            while (true) {
                if (i2 >= size) {
                    break;
                }
                try {
                    int uid = this.mUidRules.keyAt(i2);
                    int uidRules = this.mUidRules.get(uid, 0);
                    pw.println("UID=" + uid + " rules=" + NetworkPolicyManager.uidRulesToString(uidRules));
                } catch (ArrayIndexOutOfBoundsException e) {
                    pw.println("  ArrayIndexOutOfBoundsException");
                } catch (ConcurrentModificationException e2) {
                    pw.println("  ConcurrentModificationException");
                }
                i2++;
            }
            pw.println();
            pw.decreaseIndent();
            pw.println("Network Requests:");
            pw.increaseIndent();
            dumpNetworkRequests(pw);
            pw.decreaseIndent();
            pw.println();
            this.mLegacyTypeTracker.dump(pw);
            pw.println();
            this.mTethering.dump(fd, pw, args);
            pw.println();
            this.mKeepaliveTracker.dump(pw);
            pw.println();
            dumpAvoidBadWifiSettings(pw);
            pw.println();
            this.mMultipathPolicyTracker.dump(pw);
            if (!ArrayUtils.contains(args, SHORT_ARG)) {
                pw.println();
                pw.println("mNetworkRequestInfoLogs (most recent first):");
                pw.increaseIndent();
                this.mNetworkRequestInfoLogs.reverseDump(fd, pw, args);
                pw.decreaseIndent();
                pw.println();
                pw.println("mNetworkInfoBlockingLogs (most recent first):");
                pw.increaseIndent();
                this.mNetworkInfoBlockingLogs.reverseDump(fd, pw, args);
                pw.decreaseIndent();
                pw.println();
                pw.println("NetTransition WakeLock activity (most recent first):");
                pw.increaseIndent();
                pw.println("total acquisitions: " + this.mTotalWakelockAcquisitions);
                pw.println("total releases: " + this.mTotalWakelockReleases);
                pw.println("cumulative duration: " + (this.mTotalWakelockDurationMs / 1000) + "s");
                pw.println("longest duration: " + (this.mMaxWakelockDurationMs / 1000) + "s");
                if (this.mTotalWakelockAcquisitions > this.mTotalWakelockReleases) {
                    pw.println("currently holding WakeLock for: " + ((SystemClock.elapsedRealtime() - this.mLastWakeLockAcquireTimestamp) / 1000) + "s");
                }
                this.mWakelockLogs.reverseDump(fd, pw, args);
                pw.println();
                pw.println("bandwidth update requests (by uid):");
                pw.increaseIndent();
                synchronized (this.mBandwidthRequests) {
                    for (i = 0; i < this.mBandwidthRequests.size(); i++) {
                        pw.println("[" + this.mBandwidthRequests.keyAt(i) + "]: " + this.mBandwidthRequests.valueAt(i));
                    }
                }
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
            pw.println();
            pw.println("NetworkStackClient logs:");
            pw.increaseIndent();
            NetworkStackClient.getInstance().dump(pw);
            pw.decreaseIndent();
            pw.println();
            pw.println("Permission Monitor:");
            pw.increaseIndent();
            this.mPermissionMonitor.dump(pw);
            pw.decreaseIndent();
        }
    }

    private void dumpNetworks(IndentingPrintWriter pw) {
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            pw.println(nai.toString());
            pw.increaseIndent();
            pw.println(String.format("Requests: REQUEST:%d LISTEN:%d BACKGROUND_REQUEST:%d total:%d", Integer.valueOf(nai.numForegroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests() - nai.numRequestNetworkRequests()), Integer.valueOf(nai.numBackgroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests())));
            pw.increaseIndent();
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                pw.println(nai.requestAt(i).toString());
            }
            pw.decreaseIndent();
            pw.println("Lingered:");
            pw.increaseIndent();
            nai.dumpLingerTimers(pw);
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
    }

    private void dumpNetworkRequests(IndentingPrintWriter pw) {
        for (NetworkRequestInfo nri : requestsSortedById()) {
            pw.println(nri.toString());
        }
    }

    private NetworkAgentInfo[] networksSortedById() {
        NetworkAgentInfo[] networks = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[0]);
        Arrays.sort(networks, Comparator.comparingInt($$Lambda$MtkConnectivityService$x2Zo4gL9SJltVqJWmaROPZfJr4.INSTANCE));
        return networks;
    }

    private NetworkRequestInfo[] requestsSortedById() {
        NetworkRequestInfo[] requests = (NetworkRequestInfo[]) this.mNetworkRequests.values().toArray(new NetworkRequestInfo[0]);
        Arrays.sort(requests, Comparator.comparingInt($$Lambda$MtkConnectivityService$MKKdpO7XQK1qJQw2DuuYW6L6E.INSTANCE));
        return requests;
    }

    /* access modifiers changed from: private */
    public boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
        if (nai.network == null) {
            return false;
        }
        NetworkAgentInfo officialNai = getNetworkAgentInfoForNetwork(nai.network);
        if (officialNai != null && officialNai.equals(nai)) {
            return true;
        }
        if (officialNai != null || VDBG) {
            loge(eventName(what) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class NetworkStateTrackerHandler extends Handler {
        public NetworkStateTrackerHandler(Looper looper) {
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    MtkConnectivityService.this.handleAsyncChannelHalfConnect(msg);
                    return true;
                case 69633:
                case 69634:
                default:
                    return false;
                case 69635:
                    NetworkAgentInfo nai = (NetworkAgentInfo) MtkConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai == null) {
                        return true;
                    }
                    nai.asyncChannel.disconnect();
                    return true;
                case 69636:
                    MtkConnectivityService.this.handleAsyncChannelDisconnected(msg);
                    return true;
            }
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) MtkConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
            if (528485 == msg.what && msg.obj != null) {
                nai = (NetworkAgentInfo) msg.obj;
            }
            boolean z = false;
            if (nai != null) {
                int i = msg.what;
                if (i == 528392) {
                    if (nai.everConnected) {
                        MtkConnectivityService.loge("ERROR: cannot call explicitlySelected on already-connected network");
                    }
                    nai.networkMisc.explicitlySelected = msg.arg1 == 1;
                    nai.networkMisc.acceptUnvalidated = msg.arg1 == 1 && msg.arg2 == 1;
                    NetworkMisc networkMisc = nai.networkMisc;
                    if (msg.arg2 == 1) {
                        z = true;
                    }
                    networkMisc.acceptPartialConnectivity = z;
                } else if (i == 528397) {
                    MtkConnectivityService.this.mKeepaliveTracker.handleEventSocketKeepalive(nai, msg);
                } else if (i != 528485) {
                    switch (i) {
                        case 528385:
                            MtkConnectivityService.this.updateNetworkInfo(nai, (NetworkInfo) msg.obj, false);
                            return;
                        case 528386:
                            NetworkCapabilities networkCapabilities = (NetworkCapabilities) msg.obj;
                            if (networkCapabilities.hasConnectivityManagedCapability()) {
                                Slog.e(MtkConnectivityService.TAG, "BUG: " + nai + " has CS-managed capability.");
                            }
                            MtkConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, networkCapabilities);
                            return;
                        case 528387:
                            MtkConnectivityService.this.handleUpdateLinkProperties(nai, (LinkProperties) msg.obj);
                            return;
                        case 528388:
                            MtkConnectivityService.this.updateNetworkScore(nai, msg.arg1);
                            return;
                        default:
                            return;
                    }
                } else {
                    if (MtkConnectivityService.WLAN_ASSIST_DBG) {
                        MtkConnectivityService.log("WLAN+ EVENT_NETWORK_SCORE_CHANGED_WLAN_ASSIST score = " + msg.arg1);
                    }
                    try {
                        MtkConnectivityService.this.updateNetworkScore(nai, msg.arg1);
                    } catch (Exception e) {
                        MtkConnectivityService.loge("Exception in maybeHandleNetworkAgentMessage: " + e);
                    }
                }
            } else if (MtkConnectivityService.VDBG) {
                MtkConnectivityService.log(String.format("%s from unknown NetworkAgent", MtkConnectivityService.eventName(msg.what)));
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            int i = msg.what;
            boolean z = false;
            if (i != MtkConnectivityService.EVENT_NOTIFY_PRIVATE_DNS_STATUS) {
                int i2 = 2;
                switch (i) {
                    case MtkConnectivityService.EVENT_NETWORK_TESTED /*{ENCODED_INT: 41}*/:
                        NetworkAgentInfo nai = MtkConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                        if (nai != null) {
                            boolean wasPartial = nai.partialConnectivity;
                            nai.partialConnectivity = (msg.arg1 & 2) != 0;
                            boolean partialConnectivityChanged = wasPartial != nai.partialConnectivity;
                            boolean valid = msg.arg1 == 0;
                            boolean wasValidated = nai.lastValidated;
                            boolean wasDefault = MtkConnectivityService.this.isDefaultNetwork(nai);
                            MtkConnectivityService.this.mCsHelper.handleNetworkTestedMsg(nai, valid);
                            if (!nai.mGatewayState.needWaitReevaluateNetwork()) {
                                if (MtkConnectivityService.this.mAutoConCaptivePortalControl != null && valid) {
                                    MtkConnectivityService.this.mAutoConCaptivePortalControl.setAutoCaptiveSucStatus(nai.network.netId);
                                }
                                if (nai.captivePortalValidationPending && valid) {
                                    nai.captivePortalValidationPending = false;
                                    MtkConnectivityService.this.showNetworkNotification(nai, NetworkNotificationManager.NotificationType.LOGGED_IN);
                                }
                                String logMsg = "";
                                String redirectUrl = msg.obj instanceof String ? (String) msg.obj : logMsg;
                                if (!TextUtils.isEmpty(redirectUrl)) {
                                    logMsg = " with redirect to " + redirectUrl;
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append(nai.name());
                                sb.append(" validation ");
                                sb.append(valid ? "passed" : "failed");
                                sb.append(logMsg);
                                MtkConnectivityService.log(sb.toString());
                                if (valid && nai.networkCapabilities.hasTransport(1) && nai.networkInfo != null && nai.networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                                    MtkConnectivityService.log("wifi valid");
                                    MtkConnectivityService.this.handleUpdateNetworkScore(nai, 79);
                                }
                                if (valid != nai.lastValidated) {
                                    if (wasDefault) {
                                        MtkConnectivityService.this.metricsLogger().defaultNetworkMetrics().logDefaultNetworkValidity(SystemClock.elapsedRealtime(), valid);
                                    }
                                    int oldScore = nai.getCurrentScore();
                                    nai.lastValidated = valid;
                                    nai.everValidated |= valid;
                                    MtkConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                                    if (oldScore != nai.getCurrentScore()) {
                                        MtkConnectivityService.this.sendUpdatedScoreToFactories(nai);
                                    }
                                    if (valid) {
                                        MtkConnectivityService.this.handleFreshlyValidatedNetwork(nai);
                                        MtkConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.NO_INTERNET);
                                        MtkConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.LOST_INTERNET);
                                        MtkConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY);
                                    }
                                } else if (partialConnectivityChanged) {
                                    MtkConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                                }
                                MtkConnectivityService.this.updateInetCondition(nai);
                                Bundle redirectUrlBundle = new Bundle();
                                redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                                AsyncChannel asyncChannel = nai.asyncChannel;
                                if (valid) {
                                    i2 = 1;
                                }
                                asyncChannel.sendMessage(528391, i2, 0, redirectUrlBundle);
                                if (!wasPartial && nai.partialConnectivity) {
                                    MtkConnectivityService.this.mHandler.removeMessages(MtkConnectivityService.EVENT_PROMPT_UNVALIDATED, nai.network);
                                    MtkConnectivityService.this.handlePromptUnvalidated(nai.network);
                                }
                                if (wasValidated && !nai.lastValidated) {
                                    MtkConnectivityService.this.handleNetworkUnvalidated(nai);
                                    break;
                                }
                            } else {
                                MtkConnectivityService.log("hasDupGateway, switch gateway and Reevaluate Network.");
                                break;
                            }
                        }
                        break;
                    case MtkConnectivityService.EVENT_PRIVATE_DNS_CONFIG_RESOLVED /*{ENCODED_INT: 42}*/:
                        NetworkAgentInfo nai2 = MtkConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                        if (nai2 != null) {
                            MtkConnectivityService.this.updatePrivateDns(nai2, (PrivateDnsConfig) msg.obj);
                            break;
                        }
                        break;
                    case MtkConnectivityService.EVENT_PROVISIONING_NOTIFICATION /*{ENCODED_INT: 43}*/:
                        int netId = msg.arg2;
                        boolean visible = MtkConnectivityService.toBool(msg.arg1);
                        NetworkAgentInfo nai3 = MtkConnectivityService.this.getNetworkAgentInfoForNetId(netId);
                        if (!(nai3 == null || visible == nai3.lastCaptivePortalDetected)) {
                            int oldScore2 = nai3.getCurrentScore();
                            nai3.lastCaptivePortalDetected = visible;
                            nai3.everCaptivePortalDetected |= visible;
                            if (nai3.lastCaptivePortalDetected && 2 == getCaptivePortalMode()) {
                                MtkConnectivityService.log("Avoiding captive portal network: " + nai3.name());
                                nai3.asyncChannel.sendMessage(528399);
                                MtkConnectivityService.this.teardownUnneededNetwork(nai3);
                                break;
                            } else {
                                MtkConnectivityService.this.updateCapabilities(oldScore2, nai3, nai3.networkCapabilities);
                            }
                        }
                        if (visible) {
                            if (nai3 != null) {
                                if (!nai3.networkMisc.provisioningNotificationDisabled) {
                                    if (!OppoConnectivityServiceHelper.isDualWifiSta2Network(nai3) || !MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl.needShowOppoNotify(netId)) {
                                        if (!MtkConnectivityService.this.mAutoConCaptivePortalControl.needShowOppoNotify(netId)) {
                                            MtkConnectivityService.this.mNotifier.showNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN, nai3, (NetworkAgentInfo) null, (PendingIntent) msg.obj, nai3.networkMisc.explicitlySelected);
                                            break;
                                        } else {
                                            MtkConnectivityService.this.mNotifier.showOppoNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN, nai3, (NetworkAgentInfo) null, (PendingIntent) msg.obj, nai3.networkMisc.explicitlySelected);
                                            MtkConnectivityService.this.mAutoConCaptivePortalControl.setAutoCaptiveControlStateToNotify();
                                            break;
                                        }
                                    } else {
                                        MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl.showCaptiveNotify(nai3, (PendingIntent) msg.obj, netId);
                                        break;
                                    }
                                }
                            } else {
                                MtkConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                                break;
                            }
                        } else {
                            MtkConnectivityService.this.mNotifier.clearNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN);
                            MtkConnectivityService.this.mNotifier.clearNotification(netId, NetworkNotificationManager.NotificationType.NETWORK_SWITCH);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
            } else {
                NetworkAgentInfo nai4 = MtkConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg1);
                OppoConnectivityServiceHelper access$2200 = MtkConnectivityService.this.mCsHelper;
                int i3 = msg.arg1;
                if (msg.arg2 > 0) {
                    z = true;
                }
                access$2200.handleNotifyPrivateDnsStatus(i3, z, nai4, MtkConnectivityService.this.mDnsManager);
            }
            return true;
        }

        private int getCaptivePortalMode() {
            return Settings.Global.getInt(MtkConnectivityService.this.mContext.getContentResolver(), "captive_portal_mode", 1);
        }

        private boolean maybeHandleNetworkAgentInfoMessage(Message msg) {
            if (msg.what != 1001) {
                return false;
            }
            NetworkAgentInfo nai = (NetworkAgentInfo) msg.obj;
            if (nai == null || !MtkConnectivityService.this.isLiveNetworkAgent(nai, msg.what) || MtkConnectivityService.this.mCsHelper.noNeedLinger(nai)) {
                return true;
            }
            MtkConnectivityService.this.handleLingerComplete(nai);
            return true;
        }

        private boolean maybeHandleNetworkFactoryMessage(Message msg) {
            if (msg.what != 536580) {
                return false;
            }
            MtkConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.sendingUid, true);
            return true;
        }

        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && !maybeHandleNetworkMonitorMessage(msg) && !maybeHandleNetworkAgentInfoMessage(msg) && !maybeHandleNetworkFactoryMessage(msg)) {
                maybeHandleNetworkAgentMessage(msg);
            }
        }
    }

    private class NetworkMonitorCallbacks extends INetworkMonitorCallbacks.Stub {
        private final AutodestructReference<NetworkAgentInfo> mNai;
        private final int mNetId;

        private NetworkMonitorCallbacks(NetworkAgentInfo nai) {
            this.mNetId = nai.network.netId;
            this.mNai = new AutodestructReference<>(nai);
        }

        public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) {
            MtkConnectivityService.this.mHandler.sendMessage(MtkConnectivityService.this.mHandler.obtainMessage(MtkConnectivityService.EVENT_REGISTER_NETWORK_AGENT, new Pair((NetworkAgentInfo) this.mNai.getAndDestroy(), networkMonitor)));
        }

        public void notifyNetworkTested(int testResult, String redirectUrl) {
            MtkConnectivityService.this.mTrackerHandler.sendMessage(MtkConnectivityService.this.mTrackerHandler.obtainMessage(41, testResult, this.mNetId, redirectUrl));
        }

        public void notifyNetworkPrepareCaptive(int testResult, String redirectUrl, boolean isDualWifiSta2) {
            if (isDualWifiSta2) {
                if (MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl != null) {
                    Bundle redirectUrlBundle = new Bundle();
                    redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                    MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl.checkNetworkTestStatus(MtkConnectivityService.this.getNetworkAgentInfoForNetId(this.mNetId), 1, redirectUrlBundle);
                }
            } else if (MtkConnectivityService.this.mAutoConCaptivePortalControl != null) {
                Bundle redirectUrlBundle2 = new Bundle();
                redirectUrlBundle2.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                OppoAutoConCaptivePortalControl access$2300 = MtkConnectivityService.this.mAutoConCaptivePortalControl;
                NetworkAgentInfo access$800 = MtkConnectivityService.this.getNetworkAgentInfoForNetId(this.mNetId);
                OppoAutoConCaptivePortalControl unused = MtkConnectivityService.this.mAutoConCaptivePortalControl;
                access$2300.checkNetworkTestStatus(access$800, 1, redirectUrlBundle2);
            }
        }

        public void notifyNetworkShowPortal(int testResult, String redirectUrl, boolean isDualWifiSta2) {
            if (isDualWifiSta2) {
                if (MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl != null) {
                    Bundle redirectUrlBundle = new Bundle();
                    redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                    MtkConnectivityService.this.mDualWifiSta2CaptivePortalControl.checkNetworkTestStatus(MtkConnectivityService.this.getNetworkAgentInfoForNetId(this.mNetId), 2, redirectUrlBundle);
                }
            } else if (MtkConnectivityService.this.mAutoConCaptivePortalControl != null) {
                Bundle redirectUrlBundle2 = new Bundle();
                redirectUrlBundle2.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                OppoAutoConCaptivePortalControl access$2300 = MtkConnectivityService.this.mAutoConCaptivePortalControl;
                NetworkAgentInfo access$800 = MtkConnectivityService.this.getNetworkAgentInfoForNetId(this.mNetId);
                OppoAutoConCaptivePortalControl unused = MtkConnectivityService.this.mAutoConCaptivePortalControl;
                access$2300.checkNetworkTestStatus(access$800, 2, redirectUrlBundle2);
            }
        }

        public void notifyPrivateDnsConfigResolved(PrivateDnsConfigParcel config) {
            MtkConnectivityService.this.mTrackerHandler.sendMessage(MtkConnectivityService.this.mTrackerHandler.obtainMessage(42, 0, this.mNetId, PrivateDnsConfig.fromParcel(config)));
        }

        /* JADX INFO: finally extract failed */
        public void showProvisioningNotification(String action, String packageName) {
            Intent intent = new Intent(action);
            intent.setPackage(packageName);
            long token = Binder.clearCallingIdentity();
            try {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MtkConnectivityService.this.mContext, 0, intent, 0);
                Binder.restoreCallingIdentity(token);
                MtkConnectivityService.this.mTrackerHandler.sendMessage(MtkConnectivityService.this.mTrackerHandler.obtainMessage(43, 1, this.mNetId, pendingIntent));
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void hideProvisioningNotification() {
            MtkConnectivityService.this.mTrackerHandler.sendMessage(MtkConnectivityService.this.mTrackerHandler.obtainMessage(43, 0, this.mNetId));
        }

        public int getInterfaceVersion() {
            return 3;
        }

        public void notifyPrivateDnsConfigStatus(boolean isFailure) {
            MtkConnectivityService.this.mTrackerHandler.sendMessage(MtkConnectivityService.this.mTrackerHandler.obtainMessage(MtkConnectivityService.EVENT_NOTIFY_PRIVATE_DNS_STATUS, this.mNetId, isFailure ? 1 : 0));
        }
    }

    private boolean networkRequiresPrivateDnsValidation(NetworkAgentInfo nai) {
        return NetworkMonitorUtils.isPrivateDnsValidationRequired(nai.networkCapabilities);
    }

    /* access modifiers changed from: private */
    public void handleFreshlyValidatedNetwork(NetworkAgentInfo nai) {
        if (nai != null) {
            PrivateDnsConfig cfg = this.mDnsManager.getPrivateDnsConfig();
            if (cfg.useTls && TextUtils.isEmpty(cfg.hostname)) {
                updateDnses(nai.linkProperties, null, nai.network.netId, nai);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePrivateDnsSettingsChanged() {
        PrivateDnsConfig cfg = this.mDnsManager.getPrivateDnsConfig();
        if (this.mNetworkAgentInfos.isEmpty() && cfg.inStrictMode()) {
            log("handlePrivateDnsSettingsChanged no active network set status failure");
            this.mDnsManager.updatePrivateDnsFailureStatus();
        }
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            handlePerNetworkPrivateDnsConfig(nai, cfg);
            if (networkRequiresPrivateDnsValidation(nai)) {
                handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
            }
        }
    }

    private void handlePerNetworkPrivateDnsConfig(NetworkAgentInfo nai, PrivateDnsConfig cfg) {
        if (networkRequiresPrivateDnsValidation(nai)) {
            nai.networkMonitor().notifyPrivateDnsChanged(cfg.toParcel());
            updatePrivateDns(nai, cfg);
        }
    }

    /* access modifiers changed from: private */
    public void updatePrivateDns(NetworkAgentInfo nai, PrivateDnsConfig newCfg) {
        this.mDnsManager.updatePrivateDns(nai.network, newCfg);
        updateDnses(nai.linkProperties, null, nai.network.netId, nai);
    }

    /* access modifiers changed from: private */
    public void handlePrivateDnsValidationUpdate(DnsManager.PrivateDnsValidationUpdate update) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(update.netId);
        if (nai != null) {
            this.mDnsManager.updatePrivateDnsValidation(update);
            handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
        }
    }

    /* access modifiers changed from: private */
    public void handleNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) {
        NetworkAgentInfo nai = this.mNetworkForNetId.get(netId);
        if (nai != null) {
            Object[] objArr = new Object[4];
            objArr[0] = added ? "added" : "removed";
            objArr[1] = Integer.valueOf(netId);
            objArr[2] = prefixString;
            objArr[3] = Integer.valueOf(prefixLength);
            log(String.format("NAT64 prefix %s on netId %d: %s/%d", objArr));
            IpPrefix prefix = null;
            if (added) {
                try {
                    prefix = new IpPrefix(InetAddresses.parseNumericAddress(prefixString), prefixLength);
                } catch (IllegalArgumentException e) {
                    loge("Invalid NAT64 prefix " + prefixString + "/" + prefixLength);
                    return;
                }
            }
            nai.clatd.setNat64Prefix(prefix);
            handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
        }
    }

    private void updateLingerState(NetworkAgentInfo nai, long now) {
        nai.updateLingerTimer();
        if (nai.isLingering() && nai.numForegroundNetworkRequests() > 0) {
            log("Unlingering " + nai.name());
            nai.unlinger();
            logNetworkEvent(nai, 6);
        } else if (unneeded(nai, UnneededFor.LINGER) && nai.getLingerExpiry() > DISABLE_DATA_ALERT_BYTES) {
            int lingerTime = (int) (nai.getLingerExpiry() - now);
            log("Lingering " + nai.name() + " for " + lingerTime + "ms");
            nai.linger();
            logNetworkEvent(nai, MAX_CELLULAR_INTERFACE);
            notifyNetworkCallbacks(nai, 524291, lingerTime);
        }
    }

    /* access modifiers changed from: private */
    public void handleAsyncChannelHalfConnect(Message msg) {
        int serial;
        int score;
        AsyncChannel ac = (AsyncChannel) msg.obj;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                if (VDBG) {
                    log("NetworkFactory connected");
                }
                this.mNetworkFactoryInfos.get(msg.replyTo).asyncChannel.sendMessage(69633);
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (!nri.request.isListen()) {
                        NetworkAgentInfo nai = getNetworkForRequest(nri.request.requestId);
                        if (nai != null) {
                            score = nai.getCurrentScore();
                            serial = nai.factorySerialNumber;
                        } else {
                            score = 0;
                            serial = -1;
                        }
                        ac.sendMessage(536576, score, serial, nri.request);
                    }
                }
                return;
            }
            loge("Error connecting NetworkFactory");
            this.mNetworkFactoryInfos.remove(msg.obj);
        } else if (!this.mNetworkAgentInfos.containsKey(msg.replyTo)) {
        } else {
            if (msg.arg1 == 0) {
                if (VDBG) {
                    log("NetworkAgent connected");
                }
                this.mNetworkAgentInfos.get(msg.replyTo).asyncChannel.sendMessage(69633);
                return;
            }
            loge("Error connecting NetworkAgent");
            NetworkAgentInfo nai2 = this.mNetworkAgentInfos.remove(msg.replyTo);
            if (nai2 != null) {
                boolean wasDefault = isDefaultNetwork(nai2);
                synchronized (this.mNetworkForNetId) {
                    this.mNetworkForNetId.remove(nai2.network.netId);
                    this.mNetIdInUse.delete(nai2.network.netId);
                }
                this.mLegacyTypeTracker.remove(nai2, wasDefault);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleAsyncChannelDisconnected(Message msg) {
        NetworkAgentInfo nai = this.mNetworkAgentInfos.get(msg.replyTo);
        if (nai != null) {
            disconnectAndDestroyNetwork(nai);
            return;
        }
        NetworkFactoryInfo nfi = this.mNetworkFactoryInfos.remove(msg.replyTo);
        if (nfi != null) {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private void disconnectAndDestroyNetwork(NetworkAgentInfo nai) {
        NetworkAgentInfo currentNetwork;
        if (nai == null) {
            loge("disconnectAndDestroyNetwork() NetworkAgentInfo is null");
            return;
        }
        log(nai.name() + " got DISCONNECTED, was satisfying " + nai.numNetworkRequests());
        if (nai.mGatewayState != null) {
            nai.mGatewayState.stopGatewayDetector();
        }
        this.mCsHelper.updateOppoSlaIface(nai, false);
        if (OppoConnectivityServiceHelper.isDualWifiSta2Network(nai)) {
            this.mDualWifiSta2CaptivePortalControl.exitCaptiveControl(nai.network.netId);
        } else if (OppoConnectivityServiceHelper.isWifiNetwork(nai)) {
            this.mAutoConCaptivePortalControl.exitAutoCaptiveControl(nai.network.netId);
        }
        if (nai.networkInfo != null && this.mCsHelper.hasWifiAssistant) {
            boolean changedNetworkIsCellular = nai.networkCapabilities.hasTransport(0);
            String extraStr = nai.networkInfo.getExtraInfo();
            if (!(extraStr == null ? false : "ims".equals(extraStr.toLowerCase())) && changedNetworkIsCellular) {
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ handleAsyncChannelDisconnected CELLULAR DISCONNECTED! update DataScore to wifi!");
                }
                sendBroadcastDataScore(INVALID_SCORE);
            }
        }
        this.mNotifier.clearNotification(nai.network.netId);
        if (nai.networkInfo.isConnected()) {
            nai.networkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
        }
        boolean wasDefault = isDefaultNetwork(nai);
        if (wasDefault) {
            this.mDefaultInetConditionPublished = 0;
            metricsLogger().defaultNetworkMetrics().logDefaultNetworkEvent(SystemClock.elapsedRealtime(), (NetworkAgentInfo) null, nai);
            this.mCsHelper.clearPrivateDns(nai, this.mDnsManager);
        }
        notifyIfacesChangedForNetworkStats();
        notifyNetworkCallbacks(nai, 524292);
        this.mKeepaliveTracker.handleStopAllKeepalives(nai, -20);
        for (String iface : nai.linkProperties.getAllInterfaceNames()) {
            wakeupModifyInterface(iface, nai.networkCapabilities, false);
        }
        nai.networkMonitor().notifyNetworkDisconnected();
        this.mNetworkAgentInfos.remove(nai.messenger);
        nai.clatd.update();
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.remove(nai.network.netId);
        }
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest request = nai.requestAt(i);
            if (!(request == null || (currentNetwork = getNetworkForRequest(request.requestId)) == null || currentNetwork.network.netId != nai.network.netId)) {
                clearNetworkForRequest(request.requestId);
                sendUpdatedScoreToFactories(request, null);
            }
        }
        nai.clearLingerState();
        if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
            updateDataActivityTracking(null, nai);
            notifyLockdownVpn(nai);
            ensureNetworkTransitionWakelock(nai.name());
        }
        this.mLegacyTypeTracker.remove(nai, wasDefault);
        if (!nai.networkCapabilities.hasTransport(4)) {
            updateAllVpnsCapabilities();
        }
        rematchAllNetworksAndRequests(null, 0);
        this.mLingerMonitor.noteDisconnect(nai);
        if (nai.created) {
            destroyNativeNetwork(nai);
            this.mDnsManager.removeNetwork(nai.network);
        }
        synchronized (this.mNetworkForNetId) {
            this.mNetIdInUse.delete(nai.network.netId);
        }
    }

    private boolean createNativeNetwork(NetworkAgentInfo networkAgent) {
        boolean z;
        try {
            if (networkAgent.isVPN()) {
                INetd iNetd = this.mNetd;
                int i = networkAgent.network.netId;
                if (networkAgent.networkMisc != null) {
                    if (networkAgent.networkMisc.allowBypass) {
                        z = false;
                        iNetd.networkCreateVpn(i, z);
                    }
                }
                z = true;
                iNetd.networkCreateVpn(i, z);
            } else {
                this.mNetd.networkCreatePhysical(networkAgent.network.netId, getNetworkPermission(networkAgent.networkCapabilities));
            }
            this.mDnsResolver.createNetworkCache(networkAgent.network.netId);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            loge("Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            return false;
        }
    }

    private void destroyNativeNetwork(NetworkAgentInfo networkAgent) {
        try {
            this.mNetd.networkDestroy(networkAgent.network.netId);
            this.mDnsResolver.destroyNetworkCache(networkAgent.network.netId);
        } catch (RemoteException | ServiceSpecificException e) {
            loge("Exception destroying network: " + e);
        }
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(PendingIntent pendingIntent) {
        Intent intent = pendingIntent.getIntent();
        for (Map.Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            PendingIntent existingPendingIntent = entry.getValue().mPendingIntent;
            if (existingPendingIntent != null && existingPendingIntent.getIntent().filterEquals(intent)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkRequestWithIntent(Message msg) {
        NetworkRequestInfo nri = (NetworkRequestInfo) msg.obj;
        NetworkRequestInfo existingRequest = findExistingNetworkRequestInfo(nri.mPendingIntent);
        if (existingRequest != null) {
            log("Replacing " + existingRequest.request + " with " + nri.request + " because their intents matched.");
            handleReleaseNetworkRequest(existingRequest.request, getCallingUid(), false);
        }
        handleRegisterNetworkRequest(nri);
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        this.mNetworkRequests.put(nri.request, nri);
        LocalLog localLog = this.mNetworkRequestInfoLogs;
        localLog.log("REGISTER " + nri);
        if (nri.request.isListen()) {
            for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && network.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(network, "REGISTER", nri.request);
                }
            }
        }
        rematchAllNetworksAndRequests(null, 0);
        if (nri.request.isRequest() && getNetworkForRequest(nri.request.requestId) == null) {
            sendUpdatedScoreToFactories(nri.request, null);
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid, false);
        }
    }

    private boolean unneeded(NetworkAgentInfo nai, UnneededFor reason) {
        int numRequests;
        int i = AnonymousClass9.$SwitchMap$com$android$server$MtkConnectivityService$UnneededFor[reason.ordinal()];
        if (i == 1) {
            numRequests = nai.numRequestNetworkRequests();
        } else if (i != 2) {
            Slog.e(TAG, "Invalid reason. Cannot happen.");
            return true;
        } else {
            numRequests = nai.numForegroundNetworkRequests();
        }
        if (!nai.everConnected || nai.isVPN() || nai.isLingering() || numRequests > 0) {
            return false;
        }
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            if ((reason != UnneededFor.LINGER || !nri.request.isBackgroundRequest()) && nri.request.isRequest() && nai.satisfies(nri.request)) {
                if (nai.isSatisfyingRequest(nri.request.requestId) || (this.mNetworkForRequestId.get(nri.request.requestId) != null && this.mNetworkForRequestId.get(nri.request.requestId).getCurrentScore() < nai.getCurrentScoreAsValidated())) {
                    return false;
                }
            }
        }
        return true;
    }

    private NetworkRequestInfo getNriForAppRequest(NetworkRequest request, int callingUid, String requestedOperation) {
        NetworkRequestInfo nri = this.mNetworkRequests.get(request);
        if (nri == null || 1000 == callingUid || nri.mUid == callingUid) {
            return nri;
        }
        log(String.format("UID %d attempted to %s for unowned request %s", Integer.valueOf(callingUid), requestedOperation, nri));
        return null;
    }

    /* access modifiers changed from: private */
    public void handleTimedOutNetworkRequest(NetworkRequestInfo nri) {
        if (this.mNetworkRequests.get(nri.request) != null && getNetworkForRequest(nri.request.requestId) == null) {
            if (VDBG || nri.request.isRequest()) {
                log("releasing " + nri.request + " (timeout)");
            }
            handleRemoveNetworkRequest(nri);
            callCallbackForRequest(nri, null, 524293, 0);
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseNetworkRequest(NetworkRequest request, int callingUid, boolean callOnUnavailable) {
        NetworkRequestInfo nri = getNriForAppRequest(request, callingUid, "release NetworkRequest");
        if (nri != null) {
            if (VDBG || nri.request.isRequest()) {
                log("releasing " + nri.request + " (release request)");
            }
            handleRemoveNetworkRequest(nri);
            if (callOnUnavailable) {
                callCallbackForRequest(nri, null, 524293, 0);
            }
        }
    }

    private void handleRemoveNetworkRequest(NetworkRequestInfo nri) {
        nri.unlinkDeathRecipient();
        this.mNetworkRequests.remove(nri.request);
        synchronized (this.mUidToNetworkRequestCount) {
            int requests = this.mUidToNetworkRequestCount.get(nri.mUid, 0);
            if (requests < 1) {
                String str = TAG;
                Slog.e(str, "BUG: too small request count " + requests + " for UID " + nri.mUid);
            } else if (requests == 1) {
                this.mUidToNetworkRequestCount.removeAt(this.mUidToNetworkRequestCount.indexOfKey(nri.mUid));
            } else {
                this.mUidToNetworkRequestCount.put(nri.mUid, requests - 1);
            }
        }
        LocalLog localLog = this.mNetworkRequestInfoLogs;
        localLog.log("RELEASE " + nri);
        if (nri.request.isRequest()) {
            boolean wasKept = false;
            NetworkAgentInfo nai = getNetworkForRequest(nri.request.requestId);
            if (nai != null) {
                boolean wasBackgroundNetwork = nai.isBackgroundNetwork();
                nai.removeRequest(nri.request.requestId);
                if (VDBG || DDBG) {
                    log(" Removing from current network " + nai.name() + ", leaving " + nai.numNetworkRequests() + " requests.");
                }
                updateLingerState(nai, SystemClock.elapsedRealtime());
                if (unneeded(nai, UnneededFor.TEARDOWN)) {
                    log("no live requests for " + nai.name() + "; disconnecting");
                    teardownUnneededNetwork(nai);
                } else {
                    wasKept = true;
                }
                clearNetworkForRequest(nri.request.requestId);
                if (!wasBackgroundNetwork && nai.isBackgroundNetwork()) {
                    updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                }
            }
            if (!(nri.request.legacyType == -1 || nai == null)) {
                boolean doRemove = true;
                if (wasKept) {
                    for (int i = 0; i < nai.numNetworkRequests(); i++) {
                        NetworkRequest otherRequest = nai.requestAt(i);
                        if (otherRequest.legacyType == nri.request.legacyType && otherRequest.isRequest()) {
                            log(" still have other legacy request - leaving");
                            doRemove = false;
                        }
                    }
                }
                if (doRemove) {
                    this.mLegacyTypeTracker.remove(nri.request.legacyType, nai, false);
                }
            }
            for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
                nfi.asyncChannel.sendMessage(536577, nri.request);
            }
            return;
        }
        for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
            nai2.removeRequest(nri.request.requestId);
            if (nri.request.networkCapabilities.hasSignalStrength() && nai2.satisfiesImmutableCapabilitiesOf(nri.request)) {
                updateSignalStrengthThresholds(nai2, "RELEASE", nri.request);
            }
        }
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_SET_ACCEPT_UNVALIDATED, encodeBool(accept), encodeBool(always), network));
    }

    public void setAcceptPartialConnectivity(Network network, boolean accept, boolean always) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_SET_ACCEPT_PARTIAL_CONNECTIVITY, encodeBool(accept), encodeBool(always), network));
    }

    public void setAvoidUnvalidated(Network network) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_SET_AVOID_UNVALIDATED, network));
    }

    /* access modifiers changed from: private */
    public void handleSetAcceptUnvalidated(Network network, boolean accept, boolean always) {
        log("handleSetAcceptUnvalidated network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated) {
            if (!nai.networkMisc.explicitlySelected) {
                Slog.e(TAG, "BUG: setAcceptUnvalidated non non-explicitly selected network");
            }
            if (accept != nai.networkMisc.acceptUnvalidated) {
                int oldScore = nai.getCurrentScore();
                nai.networkMisc.acceptUnvalidated = accept;
                nai.networkMisc.acceptPartialConnectivity = accept;
                rematchAllNetworksAndRequests(nai, oldScore);
                sendUpdatedScoreToFactories(nai);
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, encodeBool(accept));
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSetAcceptPartialConnectivity(Network network, boolean accept, boolean always) {
        log("handleSetAcceptPartialConnectivity network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.lastValidated) {
            if (accept != nai.networkMisc.acceptPartialConnectivity) {
                nai.networkMisc.acceptPartialConnectivity = accept;
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, encodeBool(accept));
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
                return;
            }
            nai.networkMonitor().setAcceptPartialConnectivity();
        }
    }

    /* access modifiers changed from: private */
    public void handleSetAvoidUnvalidated(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.lastValidated && !nai.avoidUnvalidated) {
            int oldScore = nai.getCurrentScore();
            nai.avoidUnvalidated = true;
            rematchAllNetworksAndRequests(nai, oldScore);
            sendUpdatedScoreToFactories(nai);
        }
    }

    private void scheduleUnvalidatedPrompt(NetworkAgentInfo nai) {
        if (VDBG) {
            log("scheduleUnvalidatedPrompt " + nai.network);
        }
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessageDelayed(internalHandler.obtainMessage(EVENT_PROMPT_UNVALIDATED, nai.network), 8000);
    }

    public void startCaptivePortalApp(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.post(new Runnable(network) {
            /* class com.android.server.$$Lambda$MtkConnectivityService$HeDH94sP2JC0cyXsaEf0qHRuAG0 */
            private final /* synthetic */ Network f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MtkConnectivityService.this.lambda$startCaptivePortalApp$3$MtkConnectivityService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$startCaptivePortalApp$3$MtkConnectivityService(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(EVENT_REGISTER_NETWORK_FACTORY)) {
            nai.networkMonitor().launchCaptivePortalApp();
        }
    }

    public void startCaptivePortalAppInternal(Network network, Bundle appExtras) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MAINLINE_NETWORK_STACK", "ConnectivityService");
        Intent appIntent = new Intent("android.net.conn.CAPTIVE_PORTAL");
        appIntent.putExtras(appExtras);
        appIntent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new CaptivePortalImpl(network).asBinder()));
        appIntent.setFlags(272629760);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null) {
            nai.captivePortalValidationPending = true;
        }
        Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(appIntent) {
            /* class com.android.server.$$Lambda$MtkConnectivityService$deSwjDXzD2ywYZyySwnT1ydfsAA */
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                MtkConnectivityService.this.lambda$startCaptivePortalAppInternal$4$MtkConnectivityService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$startCaptivePortalAppInternal$4$MtkConnectivityService(Intent appIntent) throws Exception {
        this.mContext.startActivityAsUser(appIntent, UserHandle.CURRENT);
    }

    private class CaptivePortalImpl extends ICaptivePortal.Stub {
        private final Network mNetwork;

        private CaptivePortalImpl(Network network) {
            this.mNetwork = network;
        }

        public void appResponse(int response) {
            NetworkMonitorManager nm;
            if (response == 2) {
                MtkConnectivityService.this.enforceSettingsPermission();
            }
            NetworkAgentInfo nai = MtkConnectivityService.this.getNetworkAgentInfoForNetwork(this.mNetwork);
            if (nai != null && (nm = nai.networkMonitor()) != null) {
                nm.notifyCaptivePortalAppFinished(response);
            }
        }

        public void logEvent(int eventId, String packageName) {
            MtkConnectivityService.this.enforceSettingsPermission();
            new MetricsLogger().action(eventId, packageName);
        }
    }

    public boolean avoidBadWifi() {
        return this.mMultinetworkPolicyTracker.getAvoidBadWifi();
    }

    public boolean shouldAvoidBadWifi() {
        if (checkNetworkStackPermission()) {
            return avoidBadWifi();
        }
        throw new SecurityException("avoidBadWifi requires NETWORK_STACK permission");
    }

    /* access modifiers changed from: private */
    /* renamed from: rematchForAvoidBadWifiUpdate */
    public void lambda$new$0$MtkConnectivityService() {
        rematchAllNetworksAndRequests(null, 0);
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.networkCapabilities.hasTransport(1) || nai.networkCapabilities.hasTransport(EVENT_CLEAR_NET_TRANSITION_WAKELOCK)) {
                sendUpdatedScoreToFactories(nai);
            }
        }
    }

    private void dumpAvoidBadWifiSettings(IndentingPrintWriter pw) {
        String description;
        boolean configRestrict = this.mMultinetworkPolicyTracker.configRestrictsAvoidBadWifi();
        if (!configRestrict) {
            pw.println("Bad Wi-Fi avoidance: unrestricted");
            return;
        }
        pw.println("Bad Wi-Fi avoidance: " + avoidBadWifi());
        pw.increaseIndent();
        pw.println("Config restrict:   " + configRestrict);
        String value = this.mMultinetworkPolicyTracker.getAvoidBadWifiSetting();
        if ("0".equals(value)) {
            description = "get stuck";
        } else if (value == null) {
            description = "prompt";
        } else if ("1".equals(value)) {
            description = "avoid";
        } else {
            description = value + " (?)";
        }
        pw.println("User setting:      " + description);
        pw.println("Network overrides:");
        pw.increaseIndent();
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            if (nai.avoidUnvalidated) {
                pw.println(nai.name());
            }
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    /* renamed from: com.android.server.MtkConnectivityService$9  reason: invalid class name */
    static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$MtkConnectivityService$UnneededFor = new int[UnneededFor.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType = new int[NetworkNotificationManager.NotificationType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.LOGGED_IN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.NO_INTERNET.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.LOST_INTERNET.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$server$MtkConnectivityService$UnneededFor[UnneededFor.TEARDOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$server$MtkConnectivityService$UnneededFor[UnneededFor.LINGER.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void showNetworkNotification(NetworkAgentInfo nai, NetworkNotificationManager.NotificationType type) {
        boolean highPriority;
        String action;
        int i = AnonymousClass9.$SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[type.ordinal()];
        if (i == 1) {
            action = "android.settings.WIFI_SETTINGS";
            this.mHandler.removeMessages(44);
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessageDelayed(internalHandler.obtainMessage(44, nai.network.netId, 0), 20000);
            highPriority = true;
        } else if (i == 2) {
            action = "android.net.conn.PROMPT_UNVALIDATED";
            highPriority = true;
        } else if (i == 3) {
            action = "android.net.conn.PROMPT_LOST_VALIDATION";
            highPriority = true;
        } else if (i != 4) {
            Slog.e(TAG, "Unknown notification type " + type);
            return;
        } else {
            action = "android.net.conn.PROMPT_PARTIAL_CONNECTIVITY";
            highPriority = nai.networkMisc.explicitlySelected;
        }
        Intent intent = new Intent(action);
        if (type != NetworkNotificationManager.NotificationType.LOGGED_IN) {
            intent.setData(Uri.fromParts("netId", Integer.toString(nai.network.netId), null));
            intent.addFlags(268435456);
            intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
        }
        this.mNotifier.showNotification(nai.network.netId, type, nai, (NetworkAgentInfo) null, PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, UserHandle.CURRENT), highPriority);
    }

    private boolean shouldPromptUnvalidated(NetworkAgentInfo nai) {
        if (nai.everValidated || nai.everCaptivePortalDetected) {
            return false;
        }
        if (nai.partialConnectivity && !nai.networkMisc.acceptPartialConnectivity) {
            return true;
        }
        if (!nai.networkMisc.explicitlySelected || nai.networkMisc.acceptUnvalidated) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handlePromptUnvalidated(Network network) {
        if (VDBG || DDBG) {
            log("handlePromptUnvalidated " + network);
        }
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && shouldPromptUnvalidated(nai)) {
            if (nai.networkInfo == null || nai.networkInfo.getType() != 1) {
                nai.asyncChannel.sendMessage(528399);
                if (nai.partialConnectivity) {
                    showNetworkNotification(nai, NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY);
                } else {
                    showNetworkNotification(nai, NetworkNotificationManager.NotificationType.NO_INTERNET);
                }
            } else if (VDBG) {
                log("handlePromptUnvalidated WiFi do not show NO_INTERNET notification");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkUnvalidated(NetworkAgentInfo nai) {
        NetworkCapabilities nc = nai.networkCapabilities;
        log("handleNetworkUnvalidated " + nai.name() + " cap=" + nc);
        if (nc.hasTransport(1) && this.mMultinetworkPolicyTracker.shouldNotifyWifiUnvalidated()) {
            showNetworkNotification(nai, NetworkNotificationManager.NotificationType.LOST_INTERNET);
        }
    }

    public int getMultipathPreference(Network network) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(11)) {
            return 7;
        }
        Integer networkPreference = this.mMultipathPolicyTracker.getMultipathPreference(network);
        if (networkPreference != null) {
            return networkPreference.intValue();
        }
        return this.mMultinetworkPolicyTracker.getMeteredMultipathPreference();
    }

    public NetworkRequest getDefaultRequest() {
        return this.mDefaultRequest;
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != MtkConnectivityService.EVENT_CLEAR_NET_TRANSITION_WAKELOCK) {
                if (i == MtkConnectivityService.EVENT_APPLY_GLOBAL_HTTP_PROXY) {
                    MtkConnectivityService.this.mProxyTracker.loadDeprecatedGlobalHttpProxy();
                    return;
                } else if (i == 44) {
                    MtkConnectivityService.this.mNotifier.clearNotification(msg.arg1, NetworkNotificationManager.NotificationType.LOGGED_IN);
                    return;
                } else if (i != MtkConnectivityService.EVENT_SET_ACCEPT_PARTIAL_CONNECTIVITY) {
                    if (i == 100) {
                        NetworkAgentInfo networkAgent = MtkConnectivityService.this;
                        networkAgent.handleRegisterNetworkRequest(new NetworkRequestInfo(null, networkAgent.mDefaultRequest, new Binder()));
                    } else if (i != MtkConnectivityService.EVENT_NOTIFY_NETWORK_CONNECTED) {
                        switch (i) {
                            case MtkConnectivityService.EVENT_PROXY_HAS_CHANGED /*{ENCODED_INT: 16}*/:
                                MtkConnectivityService.this.handleApplyDefaultProxy((ProxyInfo) msg.obj);
                                return;
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_FACTORY /*{ENCODED_INT: 17}*/:
                                MtkConnectivityService.this.handleRegisterNetworkFactory((NetworkFactoryInfo) msg.obj);
                                return;
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_AGENT /*{ENCODED_INT: 18}*/:
                                Pair<NetworkAgentInfo, INetworkMonitor> arg = (Pair) msg.obj;
                                MtkConnectivityService.this.handleRegisterNetworkAgent((NetworkAgentInfo) arg.first, (INetworkMonitor) arg.second);
                                return;
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_REQUEST /*{ENCODED_INT: 19}*/:
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_LISTENER /*{ENCODED_INT: 21}*/:
                                MtkConnectivityService.this.handleRegisterNetworkRequest((NetworkRequestInfo) msg.obj);
                                return;
                            case 20:
                                MtkConnectivityService.this.handleTimedOutNetworkRequest((NetworkRequestInfo) msg.obj);
                                return;
                            case MtkConnectivityService.EVENT_RELEASE_NETWORK_REQUEST /*{ENCODED_INT: 22}*/:
                                MtkConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1, false);
                                return;
                            case MtkConnectivityService.EVENT_UNREGISTER_NETWORK_FACTORY /*{ENCODED_INT: 23}*/:
                                MtkConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                                return;
                            case MtkConnectivityService.EVENT_EXPIRE_NET_TRANSITION_WAKELOCK /*{ENCODED_INT: 24}*/:
                                break;
                            case MtkConnectivityService.EVENT_SYSTEM_READY /*{ENCODED_INT: 25}*/:
                                MtkConnectivityService.this.mMultipathPolicyTracker.start();
                                return;
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT /*{ENCODED_INT: 26}*/:
                            case MtkConnectivityService.EVENT_REGISTER_NETWORK_LISTENER_WITH_INTENT /*{ENCODED_INT: 31}*/:
                                MtkConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                                return;
                            case MtkConnectivityService.EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT /*{ENCODED_INT: 27}*/:
                                MtkConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                                return;
                            case MtkConnectivityService.EVENT_SET_ACCEPT_UNVALIDATED /*{ENCODED_INT: 28}*/:
                                MtkConnectivityService.this.handleSetAcceptUnvalidated((Network) msg.obj, MtkConnectivityService.toBool(msg.arg1), MtkConnectivityService.toBool(msg.arg2));
                                return;
                            case MtkConnectivityService.EVENT_PROMPT_UNVALIDATED /*{ENCODED_INT: 29}*/:
                                MtkConnectivityService.this.handlePromptUnvalidated((Network) msg.obj);
                                return;
                            case MtkConnectivityService.EVENT_CONFIGURE_ALWAYS_ON_NETWORKS /*{ENCODED_INT: 30}*/:
                                MtkConnectivityService.this.handleConfigureAlwaysOnNetworks();
                                return;
                            default:
                                switch (i) {
                                    case MtkConnectivityService.EVENT_SET_AVOID_UNVALIDATED /*{ENCODED_INT: 35}*/:
                                        MtkConnectivityService.this.handleSetAvoidUnvalidated((Network) msg.obj);
                                        return;
                                    case MtkConnectivityService.EVENT_REVALIDATE_NETWORK /*{ENCODED_INT: 36}*/:
                                        break;
                                    case MtkConnectivityService.EVENT_PRIVATE_DNS_SETTINGS_CHANGED /*{ENCODED_INT: 37}*/:
                                        MtkConnectivityService.this.handlePrivateDnsSettingsChanged();
                                        return;
                                    case MtkConnectivityService.EVENT_PRIVATE_DNS_VALIDATION_UPDATE /*{ENCODED_INT: 38}*/:
                                        MtkConnectivityService.this.handlePrivateDnsValidationUpdate((DnsManager.PrivateDnsValidationUpdate) msg.obj);
                                        return;
                                    case MtkConnectivityService.EVENT_UID_RULES_CHANGED /*{ENCODED_INT: 39}*/:
                                        MtkConnectivityService.this.handleUidRulesChanged(msg.arg1, msg.arg2);
                                        return;
                                    case 40:
                                        MtkConnectivityService.this.handleRestrictBackgroundChanged(MtkConnectivityService.toBool(msg.arg1));
                                        return;
                                    default:
                                        switch (i) {
                                            case 528395:
                                                MtkConnectivityService.this.mKeepaliveTracker.handleStartKeepalive(msg);
                                                return;
                                            case 528396:
                                                MtkConnectivityService.this.mKeepaliveTracker.handleStopKeepalive(MtkConnectivityService.this.getNetworkAgentInfoForNetwork((Network) msg.obj), msg.arg1, msg.arg2);
                                                return;
                                            default:
                                                return;
                                        }
                                }
                        }
                    } else {
                        MtkConnectivityService.this.mCsHelper.handleNetworkConnected(MtkConnectivityService.this.getNetworkAgentInfoForNetwork((Network) msg.obj));
                        return;
                    }
                    MtkConnectivityService.this.handleReportNetworkConnectivity((Network) msg.obj, msg.arg1, MtkConnectivityService.toBool(msg.arg2));
                    return;
                } else {
                    MtkConnectivityService.this.handleSetAcceptPartialConnectivity((Network) msg.obj, MtkConnectivityService.toBool(msg.arg1), MtkConnectivityService.toBool(msg.arg2));
                    return;
                }
            }
            MtkConnectivityService.this.handleReleaseNetworkTransitionWakelock(msg.what);
        }
    }

    public int tether(String iface, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (isTetheringSupported()) {
            return this.mTethering.tether(iface);
        }
        return 3;
    }

    public int untether(String iface, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (isTetheringSupported()) {
            return this.mTethering.untether(iface);
        }
        return 3;
    }

    public int getLastTetherError(String iface) {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getLastTetherError(iface);
        }
        return 3;
    }

    public String[] getTetherableUsbRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableUsbRegexs();
        }
        return new String[0];
    }

    public String[] getTetherableWifiRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableWifiRegexs();
        }
        return new String[0];
    }

    public String[] getTetherableBluetoothRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableBluetoothRegexs();
        }
        return new String[0];
    }

    public int setUsbTethering(boolean enable, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (isTetheringSupported()) {
            return this.mTethering.setUsbTethering(enable);
        }
        return 3;
    }

    public String[] getTetherableIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getTetherableIfaces();
    }

    public String[] getTetheredIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getTetheredIfaces();
    }

    public String[] getTetheringErroredIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getErroredIfaces();
    }

    public String[] getTetheredDhcpRanges() {
        enforceConnectivityInternalPermission();
        return this.mTethering.getTetheredDhcpRanges();
    }

    public boolean isTetheringSupported(String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        return isTetheringSupported();
    }

    /* access modifiers changed from: private */
    public boolean isTetheringSupported() {
        boolean tetherEnabledInSettings = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), "tether_supported", encodeBool(this.mSystemProperties.get("ro.tether.denied").equals("true") ^ true))) && !this.mUserManager.hasUserRestriction("no_config_tethering");
        long token = Binder.clearCallingIdentity();
        try {
            boolean adminUser = this.mUserManager.isAdminUser();
            if (!tetherEnabledInSettings || !adminUser || !this.mTethering.hasTetherableConfiguration()) {
                return false;
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX INFO: finally extract failed */
    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (!isTetheringSupported()) {
            receiver.send(3, null);
            return;
        }
        long token = Binder.clearCallingIdentity();
        try {
            if ("com.coloros.backuprestore".equals(callerPkg)) {
                Settings.Global.putString(this.mContext.getContentResolver(), "backup_restore_hotspot", "true");
            }
            Binder.restoreCallingIdentity(token);
            this.mTethering.startTethering(type, receiver, showProvisioningUi);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public void stopTethering(int type, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Global.putString(this.mContext.getContentResolver(), "backup_restore_hotspot", "false");
            Binder.restoreCallingIdentity(token);
            this.mTethering.stopTethering(type);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void getLatestTetheringEntitlementResult(int type, ResultReceiver receiver, boolean showEntitlementUi, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.getLatestTetheringEntitlementResult(type, receiver, showEntitlementUi);
    }

    public void registerTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.registerTetheringEventCallback(callback);
    }

    public void unregisterTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.unregisterTetheringEventCallback(callback);
    }

    private void ensureNetworkTransitionWakelock(String forWhom) {
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mNetTransitionWakeLock.acquire();
                this.mLastWakeLockAcquireTimestamp = SystemClock.elapsedRealtime();
                this.mTotalWakelockAcquisitions++;
                this.mWakelockLogs.log("ACQUIRE for " + forWhom);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK), (long) this.mNetTransitionWakeLockTimeout);
            }
        }
    }

    private void scheduleReleaseNetworkTransitionWakelock() {
        synchronized (this) {
            if (this.mNetTransitionWakeLock.isHeld()) {
                this.mHandler.removeMessages(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_CLEAR_NET_TRANSITION_WAKELOCK), 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseNetworkTransitionWakelock(int eventId) {
        String event = eventName(eventId);
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mWakelockLogs.log(String.format("RELEASE: already released (%s)", event));
                Slog.w(TAG, "expected Net Transition WakeLock to be held");
                return;
            }
            this.mNetTransitionWakeLock.release();
            long lockDuration = SystemClock.elapsedRealtime() - this.mLastWakeLockAcquireTimestamp;
            this.mTotalWakelockDurationMs += lockDuration;
            this.mMaxWakelockDurationMs = Math.max(this.mMaxWakelockDurationMs, lockDuration);
            this.mTotalWakelockReleases++;
            this.mWakelockLogs.log(String.format("RELEASE (%s)", event));
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            reportNetworkConnectivity(nai.network, percentage > VALID_SCORE);
        }
    }

    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        enforceAccessPermission();
        enforceInternetPermission();
        int uid = Binder.getCallingUid();
        int connectivityInfo = encodeBool(hasConnectivity);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REVALIDATE_NETWORK, uid, connectivityInfo, network));
    }

    /* access modifiers changed from: private */
    public void handleReportNetworkConnectivity(Network network, int uid, boolean hasConnectivity) {
        NetworkAgentInfo nai;
        if (network == null) {
            nai = getDefaultNetwork();
        } else {
            nai = getNetworkAgentInfoForNetwork(network);
        }
        if (nai != null && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTING && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTED && hasConnectivity != nai.lastValidated) {
            int netid = nai.network.netId;
            log("reportNetworkConnectivity(" + netid + ", " + hasConnectivity + ") by " + uid);
            if (nai.everConnected && !isNetworkWithLinkPropertiesBlocked(getLinkProperties(nai), uid, false) && this.mCsHelper.needForceReevaluation(nai, uid)) {
                nai.networkMonitor().forceReevaluation(uid);
            }
        }
    }

    public ProxyInfo getProxyForNetwork(Network network) {
        ProxyInfo globalProxy = this.mProxyTracker.getGlobalProxy();
        if (globalProxy != null) {
            return globalProxy;
        }
        if (network == null) {
            Network activeNetwork = getActiveNetworkForUidInternal(Binder.getCallingUid(), true);
            if (activeNetwork == null) {
                return null;
            }
            return getLinkPropertiesProxyInfo(activeNetwork);
        } else if (queryUserAccess(Binder.getCallingUid(), network.netId)) {
            return getLinkPropertiesProxyInfo(network);
        } else {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean queryUserAccess(int uid, int netId) {
        return NetworkUtils.queryUserAccess(uid, netId);
    }

    private ProxyInfo getLinkPropertiesProxyInfo(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        ProxyInfo proxyInfo = null;
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            ProxyInfo linkHttpProxy = nai.linkProperties.getHttpProxy();
            if (linkHttpProxy != null) {
                proxyInfo = new ProxyInfo(linkHttpProxy);
            }
        }
        return proxyInfo;
    }

    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        this.mProxyTracker.setGlobalProxy(proxyProperties);
    }

    public ProxyInfo getGlobalProxy() {
        return this.mProxyTracker.getGlobalProxy();
    }

    /* access modifiers changed from: private */
    public void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        this.mProxyTracker.setDefaultProxy(proxy);
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp) {
        ProxyInfo oldProxyInfo = null;
        ProxyInfo newProxyInfo = newLp == null ? null : newLp.getHttpProxy();
        if (oldLp != null) {
            oldProxyInfo = oldLp.getHttpProxy();
        }
        if (!ProxyTracker.proxyInfoEqual(newProxyInfo, oldProxyInfo)) {
            this.mProxyTracker.sendProxyBroadcast();
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap<>();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            Slog.e(MtkConnectivityService.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            MtkConnectivityService.loge("No matching event to send for URI=" + uri);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Slog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Slog.e(TAG, s, t);
    }

    public boolean prepareVpn(String oldPackage, String newPackage, int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                return false;
            }
            boolean prepare = vpn.prepare(oldPackage, newPackage);
            return prepare;
        }
    }

    public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                vpn.setPackageAuthorization(packageName, authorized);
            }
        }
    }

    public ParcelFileDescriptor establishVpn(VpnConfig config) {
        ParcelFileDescriptor establish;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            establish = this.mVpns.get(user).establish(config);
        }
        return establish;
    }

    public void startLegacyVpn(VpnProfile profile) {
        int user = UserHandle.getUserId(Binder.getCallingUid());
        LinkProperties egress = getActiveLinkProperties();
        if (egress != null) {
            synchronized (this.mVpns) {
                throwIfLockdownEnabled();
                this.mVpns.get(user).startLegacyVpn(profile, this.mKeyStore, egress);
            }
            return;
        }
        throw new IllegalStateException("Missing active network connection");
    }

    public LegacyVpnInfo getLegacyVpnInfo(int userId) {
        LegacyVpnInfo legacyVpnInfo;
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            legacyVpnInfo = this.mVpns.get(userId).getLegacyVpnInfo();
        }
        return legacyVpnInfo;
    }

    private VpnInfo[] getAllVpnInfo() {
        ensureRunningOnConnectivityServiceThread();
        synchronized (this.mVpns) {
            if (this.mLockdownEnabled) {
                VpnInfo[] vpnInfoArr = new VpnInfo[0];
                return vpnInfoArr;
            }
            List<VpnInfo> infoList = new ArrayList<>();
            for (int i = 0; i < this.mVpns.size(); i++) {
                VpnInfo info = createVpnInfo(this.mVpns.valueAt(i));
                if (info != null) {
                    infoList.add(info);
                }
            }
            VpnInfo[] vpnInfoArr2 = (VpnInfo[]) infoList.toArray(new VpnInfo[infoList.size()]);
            return vpnInfoArr2;
        }
    }

    private VpnInfo createVpnInfo(Vpn vpn) {
        LinkProperties linkProperties;
        VpnInfo info = vpn.getVpnInfo();
        if (info == null) {
            return null;
        }
        Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
        if (underlyingNetworks == null) {
            NetworkAgentInfo defaultNetwork = getDefaultNetwork();
            if (!(defaultNetwork == null || defaultNetwork.linkProperties == null)) {
                info.primaryUnderlyingIface = getDefaultNetwork().linkProperties.getInterfaceName();
            }
        } else if (underlyingNetworks.length > 0 && (linkProperties = getLinkProperties(underlyingNetworks[0])) != null) {
            info.primaryUnderlyingIface = linkProperties.getInterfaceName();
        }
        if (info.primaryUnderlyingIface == null) {
            return null;
        }
        return info;
    }

    public VpnConfig getVpnConfig(int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                return null;
            }
            VpnConfig vpnConfig = vpn.getVpnConfig();
            return vpnConfig;
        }
    }

    private void updateAllVpnsCapabilities() {
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            for (int i = 0; i < this.mVpns.size(); i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    private void updateVpnCapabilities(Vpn vpn, NetworkCapabilities nc) {
        ensureRunningOnConnectivityServiceThread();
        NetworkAgentInfo vpnNai = getNetworkAgentInfoForNetId(vpn.getNetId());
        if (vpnNai != null && nc != null) {
            updateCapabilities(vpnNai.getCurrentScore(), vpnNai, nc);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b5, code lost:
        return true;
     */
    public boolean updateLockdownVpn() {
        if (Binder.getCallingUid() != 1000) {
            Slog.w(TAG, "Lockdown VPN only available to AID_SYSTEM");
            return false;
        }
        synchronized (this.mVpns) {
            this.mLockdownEnabled = LockdownVpnTracker.isEnabled();
            if (this.mLockdownEnabled) {
                byte[] profileTag = this.mKeyStore.get("LOCKDOWN_VPN");
                if (profileTag == null) {
                    Slog.e(TAG, "Lockdown VPN configured but cannot be read from keystore");
                    return false;
                }
                String profileName = new String(profileTag);
                KeyStore keyStore = this.mKeyStore;
                VpnProfile profile = VpnProfile.decode(profileName, keyStore.get("VPN_" + profileName));
                if (profile == null) {
                    String str = TAG;
                    Slog.e(str, "Lockdown VPN configured invalid profile " + profileName);
                    setLockdownTracker(null);
                    return true;
                }
                int user = UserHandle.getUserId(Binder.getCallingUid());
                Vpn vpn = this.mVpns.get(user);
                if (vpn == null) {
                    String str2 = TAG;
                    Slog.w(str2, "VPN for user " + user + " not ready yet. Skipping lockdown");
                    return false;
                }
                setLockdownTracker(new LockdownVpnTracker(this.mContext, this.mNMS, this, vpn, profile));
            } else {
                setLockdownTracker(null);
            }
        }
    }

    @GuardedBy({"mVpns"})
    private void setLockdownTracker(LockdownVpnTracker tracker) {
        LockdownVpnTracker existing = this.mLockdownTracker;
        this.mLockdownTracker = null;
        if (existing != null) {
            existing.shutdown();
        }
        if (tracker != null) {
            this.mLockdownTracker = tracker;
            this.mLockdownTracker.init();
        }
    }

    @GuardedBy({"mVpns"})
    private void throwIfLockdownEnabled() {
        if (this.mLockdownEnabled) {
            throw new IllegalStateException("Unavailable in lockdown mode");
        }
    }

    private boolean startAlwaysOnVpn(int userId) {
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.e(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            boolean startAlwaysOnVpn = vpn.startAlwaysOnVpn();
            return startAlwaysOnVpn;
        }
    }

    public boolean isAlwaysOnVpnPackageSupported(int userId, String packageName) {
        enforceSettingsPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            boolean isAlwaysOnPackageSupported = vpn.isAlwaysOnPackageSupported(packageName);
            return isAlwaysOnPackageSupported;
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown, List<String> lockdownWhitelist) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            if (LockdownVpnTracker.isEnabled()) {
                return false;
            }
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown, lockdownWhitelist)) {
                return false;
            } else {
                if (startAlwaysOnVpn(userId)) {
                    return true;
                }
                vpn.setAlwaysOnPackage((String) null, false, (List) null);
                return false;
            }
        }
    }

    public String getAlwaysOnVpnPackage(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return null;
            }
            String alwaysOnPackage = vpn.getAlwaysOnPackage();
            return alwaysOnPackage;
        }
    }

    public boolean isVpnLockdownEnabled(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            boolean lockdown = vpn.getLockdown();
            return lockdown;
        }
    }

    public List<String> getVpnLockdownWhitelist(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return null;
            }
            List<String> lockdownWhitelist = vpn.getLockdownWhitelist();
            return lockdownWhitelist;
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        return -1;
    }

    private String getProvisioningUrlBaseFromFile() {
        String mcc;
        String mnc;
        FileReader fileReader = null;
        Configuration config = this.mContext.getResources().getConfiguration();
        try {
            FileReader fileReader2 = new FileReader(this.mProvisioningUrlFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileReader2);
            XmlUtils.beginDocument(parser, TAG_PROVISIONING_URLS);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    try {
                        fileReader2.close();
                    } catch (IOException e) {
                    }
                    return null;
                } else if (element.equals(TAG_PROVISIONING_URL) && (mcc = parser.getAttributeValue(null, ATTR_MCC)) != null) {
                    try {
                        if (Integer.parseInt(mcc) == config.mcc && (mnc = parser.getAttributeValue(null, ATTR_MNC)) != null && Integer.parseInt(mnc) == config.mnc) {
                            parser.next();
                            if (parser.getEventType() == 4) {
                                String text = parser.getText();
                                try {
                                    fileReader2.close();
                                } catch (IOException e2) {
                                }
                                return text;
                            }
                        }
                    } catch (NumberFormatException e3) {
                        loge("NumberFormatException in getProvisioningUrlBaseFromFile: " + e3);
                    }
                }
            }
        } catch (FileNotFoundException e4) {
            loge("Carrier Provisioning Urls file not found");
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (XmlPullParserException e5) {
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e5);
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (IOException e6) {
            loge("I/O exception reading Carrier Provisioning Urls file: " + e6);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
        return null;
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17040449);
            log("getMobileProvisioningUrl: mobile_provisioining_url from resource =" + url);
        } else {
            log("getMobileProvisioningUrl: mobile_provisioning_url from File =" + url);
        }
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        String phoneNumber = this.mTelephonyManager.getLine1Number();
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = "0000000000";
        }
        return String.format(url, this.mTelephonyManager.getSimSerialNumber(), this.mTelephonyManager.getDeviceId(), phoneNumber);
    }

    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        enforceConnectivityInternalPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mNotifier.setProvNotificationVisible(visible, networkType + 1 + 64512, action);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceNetworkStackSettingsOrSetup();
        long ident = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", encodeBool(enable));
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003f, code lost:
        return;
     */
    public void onUserStart(int userId) {
        synchronized (this.mVpns) {
            if (this.mVpns.get(userId) != null) {
                loge("Starting user already has a VPN");
                return;
            }
            this.mVpns.put(userId, new Vpn(this.mHandler.getLooper(), this.mContext, this.mNMS, userId));
            if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserStop(int userId) {
        synchronized (this.mVpns) {
            Vpn userVpn = this.mVpns.get(userId);
            if (userVpn == null) {
                loge("Stopped user has no VPN");
                return;
            }
            userVpn.onUserStopped();
            this.mVpns.delete(userId);
        }
    }

    /* access modifiers changed from: private */
    public void onUserAdded(int userId) {
        this.mPermissionMonitor.onUserAdded(userId);
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                vpn.onUserAdded(userId);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserRemoved(int userId) {
        this.mPermissionMonitor.onUserRemoved(userId);
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                vpn.onUserRemoved(userId);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    /* access modifiers changed from: private */
    public void onPackageAdded(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageAdded: " + packageName + " | " + uid);
            return;
        }
        this.mPermissionMonitor.onPackageAdded(packageName, uid);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0048, code lost:
        return;
     */
    public void onPackageReplaced(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageReplaced: " + packageName + " | " + uid);
            return;
        }
        int userId = UserHandle.getUserId(uid);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                if (TextUtils.equals(vpn.getAlwaysOnPackage(), packageName)) {
                    String str2 = TAG;
                    Slog.d(str2, "Restarting always-on VPN package " + packageName + " for user " + userId);
                    vpn.startAlwaysOnVpn();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        return;
     */
    public void onPackageRemoved(String packageName, int uid, boolean isReplacing) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageRemoved: " + packageName + " | " + uid);
            return;
        }
        this.mPermissionMonitor.onPackageRemoved(uid);
        int userId = UserHandle.getUserId(uid);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                if (TextUtils.equals(vpn.getAlwaysOnPackage(), packageName) && !isReplacing) {
                    String str2 = TAG;
                    Slog.d(str2, "Removing always-on VPN package " + packageName + " for user " + userId);
                    vpn.setAlwaysOnPackage((String) null, false, (List) null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserUnlocked(int userId) {
        synchronized (this.mVpns) {
            if (!this.mUserManager.getUserInfo(userId).isPrimary() || !LockdownVpnTracker.isEnabled()) {
                startAlwaysOnVpn(userId);
            } else {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class NetworkFactoryInfo {
        public final AsyncChannel asyncChannel;
        public final int factorySerialNumber;
        public final Messenger messenger;
        public final String name;

        NetworkFactoryInfo(String name2, Messenger messenger2, AsyncChannel asyncChannel2, int factorySerialNumber2) {
            this.name = name2;
            this.messenger = messenger2;
            this.asyncChannel = asyncChannel2;
            this.factorySerialNumber = factorySerialNumber2;
        }
    }

    /* access modifiers changed from: private */
    public void ensureNetworkRequestHasType(NetworkRequest request) {
        if (request.type == NetworkRequest.Type.NONE) {
            throw new IllegalArgumentException("All NetworkRequests in ConnectivityService must have a type");
        }
    }

    /* access modifiers changed from: private */
    public class NetworkRequestInfo implements IBinder.DeathRecipient {
        private final IBinder mBinder;
        final PendingIntent mPendingIntent;
        boolean mPendingIntentSent;
        final int mPid;
        final int mUid;
        final Messenger messenger;
        final NetworkRequest request;

        NetworkRequestInfo(NetworkRequest r, PendingIntent pi) {
            this.request = r;
            MtkConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mPendingIntent = pi;
            this.messenger = null;
            this.mBinder = null;
            this.mPid = Binder.getCallingPid();
            this.mUid = Binder.getCallingUid();
            enforceRequestCountLimit();
        }

        NetworkRequestInfo(Messenger m, NetworkRequest r, IBinder binder) {
            this.messenger = m;
            this.request = r;
            MtkConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mBinder = binder;
            this.mPid = Binder.getCallingPid();
            this.mUid = Binder.getCallingUid();
            this.mPendingIntent = null;
            enforceRequestCountLimit();
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        private void enforceRequestCountLimit() {
            synchronized (MtkConnectivityService.this.mUidToNetworkRequestCount) {
                int networkRequests = MtkConnectivityService.this.mUidToNetworkRequestCount.get(this.mUid, 0) + 1;
                if (networkRequests < 100) {
                    MtkConnectivityService.this.mUidToNetworkRequestCount.put(this.mUid, networkRequests);
                } else {
                    throw new ServiceSpecificException(1);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeathRecipient() {
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            MtkConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            MtkConnectivityService.this.releaseNetworkRequest(this.request);
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("uid/pid:");
            sb.append(this.mUid);
            sb.append("/");
            sb.append(this.mPid);
            sb.append(" ");
            sb.append(this.request);
            if (this.mPendingIntent == null) {
                str = "";
            } else {
                str = " to trigger " + this.mPendingIntent;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    private void ensureRequestableCapabilities(NetworkCapabilities networkCapabilities) {
        String badCapability = networkCapabilities.describeFirstNonRequestableCapability();
        if (badCapability != null) {
            throw new IllegalArgumentException("Cannot request network with " + badCapability);
        }
    }

    private void ensureSufficientPermissionsForRequest(NetworkCapabilities nc, int callerPid, int callerUid) {
        if (nc.getSSID() != null && !checkSettingsPermission(callerPid, callerUid)) {
            throw new SecurityException("Insufficient permissions to request a specific SSID");
        } else if (nc.hasSignalStrength() && !checkNetworkSignalStrengthWakeupPermission(callerPid, callerUid)) {
            throw new SecurityException("Insufficient permissions to request a specific signal strength");
        }
    }

    private ArrayList<Integer> getSignalStrengthThresholds(NetworkAgentInfo nai) {
        SortedSet<Integer> thresholds = new TreeSet<>();
        synchronized (nai) {
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && nai.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    thresholds.add(Integer.valueOf(nri.request.networkCapabilities.getSignalStrength()));
                }
            }
        }
        return new ArrayList<>(thresholds);
    }

    private void updateSignalStrengthThresholds(NetworkAgentInfo nai, String reason, NetworkRequest request) {
        String detail;
        ArrayList<Integer> thresholdsArray = getSignalStrengthThresholds(nai);
        Bundle thresholds = new Bundle();
        thresholds.putIntegerArrayList("thresholds", thresholdsArray);
        if (VDBG || !"CONNECT".equals(reason)) {
            if (request == null || !request.networkCapabilities.hasSignalStrength()) {
                detail = reason;
            } else {
                detail = reason + " " + request.networkCapabilities.getSignalStrength();
            }
            log(String.format("updateSignalStrengthThresholds: %s, sending %s to %s", detail, Arrays.toString(thresholdsArray.toArray()), nai.name()));
        }
        nai.asyncChannel.sendMessage(528398, 0, 0, thresholds);
    }

    private void ensureValidNetworkSpecifier(NetworkCapabilities nc) {
        NetworkSpecifier ns;
        if (nc != null && (ns = nc.getNetworkSpecifier()) != null) {
            MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(ns);
            ns.assertValidFromUid(Binder.getCallingUid());
        }
    }

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        NetworkRequest.Type type;
        NetworkCapabilities networkCapabilities2;
        if (networkCapabilities == null) {
            type = NetworkRequest.Type.TRACK_DEFAULT;
        } else {
            type = NetworkRequest.Type.REQUEST;
        }
        if (type == NetworkRequest.Type.TRACK_DEFAULT) {
            networkCapabilities2 = createDefaultNetworkCapabilitiesForUid(Binder.getCallingUid());
            enforceAccessPermission();
        } else {
            networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
            enforceNetworkRequestPermissions(networkCapabilities2);
            enforceMeteredApnPolicy(networkCapabilities2);
        }
        ensureRequestableCapabilities(networkCapabilities2);
        ensureSufficientPermissionsForRequest(networkCapabilities2, Binder.getCallingPid(), Binder.getCallingUid());
        restrictRequestUidsForCaller(networkCapabilities2);
        if (timeoutMs >= 0) {
            ensureValidNetworkSpecifier(networkCapabilities2);
            if (ignoreMmsReqInTddOnly(networkCapabilities2)) {
                log("requestNetwork: ignore mms request in TDD Data only network mode");
                return null;
            }
            NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, legacyType, nextNetworkRequestId(), type);
            NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
            log("requestNetwork for " + nri);
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REGISTER_NETWORK_REQUEST, nri));
            if (timeoutMs > 0) {
                InternalHandler internalHandler2 = this.mHandler;
                internalHandler2.sendMessageDelayed(internalHandler2.obtainMessage(20, nri), (long) timeoutMs);
            }
            return networkRequest;
        }
        throw new IllegalArgumentException("Bad timeout specified");
    }

    private void enforceNetworkRequestPermissions(NetworkCapabilities networkCapabilities) {
        if (!networkCapabilities.hasCapability(13)) {
            enforceConnectivityRestrictedNetworksPermission();
        } else {
            enforceChangePermission();
        }
    }

    public boolean requestBandwidthUpdate(Network network) {
        NetworkAgentInfo nai;
        enforceAccessPermission();
        if (network == null) {
            return false;
        }
        synchronized (this.mNetworkForNetId) {
            nai = this.mNetworkForNetId.get(network.netId);
        }
        if (nai == null) {
            return false;
        }
        nai.asyncChannel.sendMessage(528394);
        synchronized (this.mBandwidthRequests) {
            int uid = Binder.getCallingUid();
            Integer uidReqs = this.mBandwidthRequests.get(uid);
            if (uidReqs == null) {
                uidReqs = new Integer(0);
            }
            this.mBandwidthRequests.put(uid, Integer.valueOf(uidReqs.intValue() + 1));
        }
        return true;
    }

    private boolean isSystem(int uid) {
        if (uid < 10000) {
            return true;
        }
        String appName = this.mContext.getPackageManager().getNameForUid(uid);
        if (("android.uid.systemui:" + uid).equals(appName)) {
            return true;
        }
        return false;
    }

    private void enforceMeteredApnPolicy(NetworkCapabilities networkCapabilities) {
        int uid = Binder.getCallingUid();
        if (!isSystem(uid) && !networkCapabilities.hasCapability(11) && this.mPolicyManagerInternal.isUidRestrictedOnMeteredNetworks(uid)) {
            networkCapabilities.addCapability(11);
        }
    }

    public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        enforceNetworkRequestPermissions(networkCapabilities2);
        enforceMeteredApnPolicy(networkCapabilities2);
        ensureRequestableCapabilities(networkCapabilities2);
        ensureSufficientPermissionsForRequest(networkCapabilities2, Binder.getCallingPid(), Binder.getCallingUid());
        ensureValidNetworkSpecifier(networkCapabilities2);
        restrictRequestUidsForCaller(networkCapabilities2);
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, -1, nextNetworkRequestId(), NetworkRequest.Type.REQUEST);
        NetworkRequestInfo nri = new NetworkRequestInfo(networkRequest, operation);
        log("pendingRequest for " + nri);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessageDelayed(internalHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation));
    }

    private boolean hasWifiNetworkListenPermission(NetworkCapabilities nc) {
        if (nc == null) {
            return false;
        }
        int[] transportTypes = nc.getTransportTypes();
        if (transportTypes.length != 1 || (transportTypes[0] != 1 && transportTypes[0] != EVENT_CLEAR_NET_TRANSITION_WAKELOCK)) {
            return false;
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "ConnectivityService");
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) {
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        ensureSufficientPermissionsForRequest(networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
        restrictRequestUidsForCaller(nc);
        restrictBackgroundRequestForCaller(nc);
        ensureValidNetworkSpecifier(nc);
        NetworkRequest networkRequest = new NetworkRequest(nc, -1, nextNetworkRequestId(), NetworkRequest.Type.LISTEN);
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
        if (VDBG) {
            log("listenForNetwork for " + nri);
        }
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REGISTER_NETWORK_LISTENER, nri));
        return networkRequest;
    }

    public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        ensureValidNetworkSpecifier(networkCapabilities);
        ensureSufficientPermissionsForRequest(networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        restrictRequestUidsForCaller(nc);
        NetworkRequestInfo nri = new NetworkRequestInfo(new NetworkRequest(nc, -1, nextNetworkRequestId(), NetworkRequest.Type.LISTEN), operation);
        if (VDBG) {
            log("pendingListenForNetwork for " + nri);
        }
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REGISTER_NETWORK_LISTENER, nri));
    }

    public void releaseNetworkRequest(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST, getCallingUid(), 0, networkRequest));
    }

    public int registerNetworkFactory(Messenger messenger, String name) {
        enforceConnectivityInternalPermission();
        NetworkFactoryInfo nfi = new NetworkFactoryInfo(name, messenger, new AsyncChannel(), NetworkFactory.SerialNumber.nextSerialNumber());
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_REGISTER_NETWORK_FACTORY, nfi));
        return nfi.factorySerialNumber;
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_UNREGISTER_NETWORK_FACTORY, messenger));
    }

    /* access modifiers changed from: private */
    public void handleUnregisterNetworkFactory(Messenger messenger) {
        NetworkFactoryInfo nfi = this.mNetworkFactoryInfos.remove(messenger);
        if (nfi == null) {
            loge("Failed to find Messenger in unregisterNetworkFactory");
            return;
        }
        log("unregisterNetworkFactory for " + nfi.name);
    }

    private NetworkAgentInfo getNetworkForRequest(int requestId) {
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForRequestId) {
            networkAgentInfo = this.mNetworkForRequestId.get(requestId);
        }
        return networkAgentInfo;
    }

    private void clearNetworkForRequest(int requestId) {
        synchronized (this.mNetworkForRequestId) {
            this.mNetworkForRequestId.remove(requestId);
        }
    }

    private void setNetworkForRequest(int requestId, NetworkAgentInfo nai) {
        synchronized (this.mNetworkForRequestId) {
            this.mNetworkForRequestId.put(requestId, nai);
        }
    }

    private NetworkAgentInfo getDefaultNetwork() {
        return getNetworkForRequest(this.mDefaultRequest.requestId);
    }

    private Network getNetwork(NetworkAgentInfo nai) {
        if (nai != null) {
            return nai.network;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void ensureRunningOnConnectivityServiceThread() {
        if (this.mHandler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Not running on ConnectivityService thread: " + Thread.currentThread().getName());
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork();
    }

    private boolean isDefaultRequest(NetworkRequestInfo nri) {
        return nri.request.requestId == this.mDefaultRequest.requestId;
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc) {
        return registerNetworkAgent(messenger, networkInfo, linkProperties, networkCapabilities, currentScore, networkMisc, -1);
    }

    /* JADX INFO: finally extract failed */
    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc, int factorySerialNumber) {
        enforceConnectivityInternalPermission();
        LinkProperties lp = new LinkProperties(linkProperties);
        lp.ensureDirectlyConnectedRoutes();
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        NetworkAgentInfo nai = new NetworkAgentInfo(messenger, new AsyncChannel(), new Network(reserveNetId()), new NetworkInfo(networkInfo), lp, nc, currentScore, this.mContext, this.mTrackerHandler, new NetworkMisc(networkMisc), this, this.mNetd, this.mDnsResolver, this.mNMS, factorySerialNumber);
        nai.setNetworkCapabilities(mixInCapabilities(nai, nc));
        String extraInfo = networkInfo.getExtraInfo();
        String name = TextUtils.isEmpty(extraInfo) ? nai.networkCapabilities.getSSID() : extraInfo;
        log("registerNetworkAgent " + nai);
        long token = Binder.clearCallingIdentity();
        try {
            getNetworkStack().makeNetworkMonitor(nai.network, name, new NetworkMonitorCallbacks(nai));
            Binder.restoreCallingIdentity(token);
            return nai.network.netId;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public NetworkStackClient getNetworkStack() {
        return NetworkStackClient.getInstance();
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkAgent(NetworkAgentInfo nai, INetworkMonitor networkMonitor) {
        nai.onNetworkMonitorCreated(networkMonitor);
        if (VDBG) {
            log("Got NetworkAgent Messenger");
        }
        this.mNetworkAgentInfos.put(nai.messenger, nai);
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.put(nai.network.netId, nai);
        }
        try {
            networkMonitor.start();
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        nai.asyncChannel.connect(this.mContext, this.mTrackerHandler, nai.messenger);
        updateNetworkInfo(nai, nai.networkInfo, true);
        updateUids(nai, null, nai.networkCapabilities);
    }

    private void updateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties newLp, LinkProperties oldLp) {
        int netId = networkAgent.network.netId;
        networkAgent.clatd.fixupLinkProperties(oldLp, newLp);
        updateInterfaces(newLp, oldLp, netId, networkAgent.networkCapabilities);
        updateVpnFiltering(newLp, oldLp, networkAgent);
        updateMtu(newLp, oldLp);
        if (isDefaultNetwork(networkAgent)) {
            updateTcpBufferSizes(newLp.getTcpBufferSizes());
        }
        updateRoutes(newLp, oldLp, netId);
        updateDnses(newLp, oldLp, netId, networkAgent);
        this.mDnsManager.updatePrivateDnsStatus(netId, newLp);
        if (isDefaultNetwork(networkAgent)) {
            handleApplyDefaultProxy(newLp.getHttpProxy());
        } else {
            updateProxy(newLp, oldLp);
        }
        if (!Objects.equals(newLp, oldLp)) {
            synchronized (networkAgent) {
                networkAgent.linkProperties = newLp;
            }
            networkAgent.clatd.update();
            notifyIfacesChangedForNetworkStats();
            networkAgent.networkMonitor().notifyLinkPropertiesChanged(newLp);
            if (networkAgent.everConnected) {
                notifyNetworkCallbacks(networkAgent, 524295);
            }
        }
        if (networkAgent.networkInfo != null && networkAgent.networkInfo.isConnected()) {
            log("oppo_sla:update the LinkProperties");
            this.mCsHelper.updateOppoSlaIface(networkAgent, true);
        }
        this.mKeepaliveTracker.handleCheckKeepalivesStillValid(networkAgent);
    }

    private void wakeupModifyInterface(String iface, NetworkCapabilities caps, boolean add) {
        if (caps.hasTransport(1) || caps.hasTransport(EVENT_CLEAR_NET_TRANSITION_WAKELOCK)) {
            int mark = this.mContext.getResources().getInteger(17694857);
            int mask = this.mContext.getResources().getInteger(17694858);
            if (mark != 0 && mask != 0) {
                String prefix = "iface:" + iface;
                if (add) {
                    try {
                        this.mNetd.wakeupAddInterface(iface, prefix, mark, mask);
                    } catch (Exception e) {
                        loge("Exception modifying wakeup packet monitoring: " + e);
                    }
                } else {
                    this.mNetd.wakeupDelInterface(iface, prefix, mark, mask);
                }
            }
        }
    }

    private void updateInterfaces(LinkProperties newLp, LinkProperties oldLp, int netId, NetworkCapabilities caps) {
        List list = null;
        List allInterfaceNames = oldLp != null ? oldLp.getAllInterfaceNames() : null;
        if (newLp != null) {
            list = newLp.getAllInterfaceNames();
        }
        LinkProperties.CompareResult<String> interfaceDiff = new LinkProperties.CompareResult<>(allInterfaceNames, list);
        for (String iface : interfaceDiff.added) {
            try {
                log("Adding iface " + iface + " to network " + netId);
                this.mNMS.addInterfaceToNetwork(iface, netId);
                wakeupModifyInterface(iface, caps, true);
            } catch (Exception e) {
                loge("Exception adding interface: " + e);
            }
        }
        for (String iface2 : interfaceDiff.removed) {
            try {
                log("Removing iface " + iface2 + " from network " + netId);
                wakeupModifyInterface(iface2, caps, false);
                this.mNMS.removeInterfaceFromNetwork(iface2, netId);
            } catch (Exception e2) {
                loge("Exception removing interface: " + e2);
            }
        }
    }

    private boolean updateRoutes(LinkProperties newLp, LinkProperties oldLp, int netId) {
        List list = null;
        List allRoutes = oldLp != null ? oldLp.getAllRoutes() : null;
        if (newLp != null) {
            list = newLp.getAllRoutes();
        }
        LinkProperties.CompareResult<RouteInfo> routeDiff = new LinkProperties.CompareResult<>(allRoutes, list);
        for (RouteInfo route : routeDiff.added) {
            if (!route.hasGateway()) {
                if (VDBG || DDBG) {
                    log("Adding Route [" + route + "] to network " + netId);
                }
                try {
                    this.mNMS.addRoute(netId, route);
                } catch (Exception e) {
                    if ((route.getDestination().getAddress() instanceof Inet4Address) || VDBG) {
                        loge("Exception in addRoute for non-gateway: " + e);
                    }
                }
            }
        }
        for (RouteInfo route2 : routeDiff.added) {
            if (route2.hasGateway()) {
                if (VDBG || DDBG) {
                    log("Adding Route [" + route2 + "] to network " + netId);
                }
                try {
                    this.mNMS.addRoute(netId, route2);
                } catch (Exception e2) {
                    if ((route2.getGateway() instanceof Inet4Address) || VDBG) {
                        loge("Exception in addRoute for gateway: " + e2);
                    }
                }
            }
        }
        for (RouteInfo route3 : routeDiff.removed) {
            if (VDBG || DDBG) {
                log("Removing Route [" + route3 + "] from network " + netId);
            }
            try {
                this.mNMS.removeRoute(netId, route3);
            } catch (Exception e3) {
                loge("Exception in removeRoute: " + e3);
            }
        }
        return !routeDiff.added.isEmpty() || !routeDiff.removed.isEmpty();
    }

    private void updateDnses(LinkProperties newLp, LinkProperties oldLp, int netId, NetworkAgentInfo networkAgent) {
        if (oldLp == null || !newLp.isIdenticalDnses(oldLp)) {
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            boolean isDefaultNetwork = defaultNai != null && defaultNai.network.netId == netId;
            this.mCsHelper.addDefaultDns(newLp, netId, networkAgent);
            Collection<InetAddress> dnses = newLp.getDnsServers();
            log("Setting DNS servers for network " + netId + " to " + dnses);
            try {
                this.mDnsManager.setDnsConfigurationForNetwork(netId, newLp, isDefaultNetwork);
            } catch (Exception e) {
                loge("Exception in setDnsConfigurationForNetwork: " + e);
            }
            this.mCsHelper.startDnsRecoverEngine(networkAgent);
            this.mCsHelper.setIPv6DnsConfiguation(netId);
        }
    }

    private void updateVpnFiltering(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        String newIface = null;
        String oldIface = oldLp != null ? oldLp.getInterfaceName() : null;
        if (newLp != null) {
            newIface = newLp.getInterfaceName();
        }
        boolean wasFiltering = requiresVpnIsolation(nai, nai.networkCapabilities, oldLp);
        boolean needsFiltering = requiresVpnIsolation(nai, nai.networkCapabilities, newLp);
        if (!wasFiltering && !needsFiltering) {
            return;
        }
        if (!Objects.equals(oldIface, newIface) || wasFiltering != needsFiltering) {
            Set<UidRange> ranges = nai.networkCapabilities.getUids();
            int vpnAppUid = nai.networkCapabilities.getEstablishingVpnAppUid();
            if (wasFiltering) {
                this.mPermissionMonitor.onVpnUidRangesRemoved(oldIface, ranges, vpnAppUid);
            }
            if (needsFiltering) {
                this.mPermissionMonitor.onVpnUidRangesAdded(newIface, ranges, vpnAppUid);
            }
        }
    }

    private boolean isChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (!TextUtils.isEmpty(mcc) || !TextUtils.isEmpty(mcc2)) {
            return mcc.equals("460") || mcc2.equals("460");
        }
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    private int getNetworkPermission(NetworkCapabilities nc) {
        if (!nc.hasCapability(13)) {
            return 2;
        }
        if (!nc.hasCapability(EVENT_REGISTER_NETWORK_REQUEST)) {
            return 1;
        }
        return 0;
    }

    private NetworkCapabilities mixInCapabilities(NetworkAgentInfo nai, NetworkCapabilities nc) {
        if (nai.everConnected && !nai.isVPN() && !nai.networkCapabilities.satisfiedByImmutableNetworkCapabilities(nc)) {
            String diff = nai.networkCapabilities.describeImmutableDifferences(nc);
            if (!TextUtils.isEmpty(diff)) {
                String str = TAG;
                Slog.e(str, "BUG: " + nai + " lost immutable capabilities:" + diff);
            }
        }
        NetworkCapabilities newNc = new NetworkCapabilities(nc);
        if (nai.lastValidated) {
            newNc.addCapability(EVENT_PROXY_HAS_CHANGED);
        } else {
            newNc.removeCapability(EVENT_PROXY_HAS_CHANGED);
        }
        if (nai.lastCaptivePortalDetected) {
            newNc.addCapability(EVENT_REGISTER_NETWORK_FACTORY);
        } else {
            newNc.removeCapability(EVENT_REGISTER_NETWORK_FACTORY);
        }
        if (nai.isBackgroundNetwork()) {
            newNc.removeCapability(EVENT_REGISTER_NETWORK_REQUEST);
        } else {
            newNc.addCapability(EVENT_REGISTER_NETWORK_REQUEST);
        }
        if (nai.isSuspended()) {
            newNc.removeCapability(EVENT_REGISTER_NETWORK_LISTENER);
        } else {
            newNc.addCapability(EVENT_REGISTER_NETWORK_LISTENER);
        }
        if (nai.partialConnectivity) {
            newNc.addCapability(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
        } else {
            newNc.removeCapability(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
        }
        return newNc;
    }

    /* access modifiers changed from: private */
    public void updateCapabilities(int oldScore, NetworkAgentInfo nai, NetworkCapabilities nc) {
        NetworkCapabilities prevNc;
        NetworkCapabilities newNc = mixInCapabilities(nai, nc);
        if (!Objects.equals(nai.networkCapabilities, newNc)) {
            int oldPermission = getNetworkPermission(nai.networkCapabilities);
            int newPermission = getNetworkPermission(newNc);
            if (oldPermission != newPermission && nai.created && !nai.isVPN()) {
                try {
                    this.mNMS.setNetworkPermission(nai.network.netId, newPermission);
                } catch (RemoteException e) {
                    loge("Exception in setNetworkPermission: " + e);
                }
            }
            synchronized (nai) {
                prevNc = nai.networkCapabilities;
                nai.setNetworkCapabilities(newNc);
            }
            updateUids(nai, prevNc, newNc);
            boolean roamingChanged = true;
            if (nai.getCurrentScore() != oldScore || !newNc.equalRequestableCapabilities(prevNc)) {
                rematchAllNetworksAndRequests(nai, oldScore);
                notifyNetworkCallbacks(nai, 524294);
            } else {
                processListenRequests(nai, true);
            }
            if (prevNc != null) {
                boolean oldMetered = prevNc.isMetered();
                boolean newMetered = newNc.isMetered();
                boolean meteredChanged = oldMetered != newMetered;
                if (meteredChanged) {
                    boolean z = this.mRestrictBackground;
                    maybeNotifyNetworkBlocked(nai, oldMetered, newMetered, z, z);
                }
                if (prevNc.hasCapability(EVENT_REGISTER_NETWORK_AGENT) == newNc.hasCapability(EVENT_REGISTER_NETWORK_AGENT)) {
                    roamingChanged = false;
                }
                if (meteredChanged || roamingChanged) {
                    notifyIfacesChangedForNetworkStats();
                }
            }
            if (!newNc.hasTransport(4)) {
                updateAllVpnsCapabilities();
            }
        }
    }

    private boolean requiresVpnIsolation(NetworkAgentInfo nai, NetworkCapabilities nc, LinkProperties lp) {
        if (nc == null || lp == null || !nai.isVPN() || nai.networkMisc.allowBypass || nc.getEstablishingVpnAppUid() == 1000 || lp.getInterfaceName() == null) {
            return false;
        }
        if (lp.hasIPv4DefaultRoute() || lp.hasIPv6DefaultRoute()) {
            return true;
        }
        return false;
    }

    private void updateUids(NetworkAgentInfo nai, NetworkCapabilities prevNc, NetworkCapabilities newNc) {
        Set<UidRange> newRanges = null;
        Set<UidRange> prevRanges = prevNc == null ? null : prevNc.getUids();
        if (newNc != null) {
            newRanges = newNc.getUids();
        }
        if (prevRanges == null) {
            prevRanges = new ArraySet<>();
        }
        if (newRanges == null) {
            newRanges = new ArraySet<>();
        }
        Set<UidRange> prevRangesCopy = new ArraySet<>(prevRanges);
        prevRanges.removeAll(newRanges);
        newRanges.removeAll(prevRangesCopy);
        try {
            if (!newRanges.isEmpty()) {
                UidRange[] addedRangesArray = new UidRange[newRanges.size()];
                newRanges.toArray(addedRangesArray);
                this.mNMS.addVpnUidRanges(nai.network.netId, addedRangesArray);
            }
            if (!prevRanges.isEmpty()) {
                UidRange[] removedRangesArray = new UidRange[prevRanges.size()];
                prevRanges.toArray(removedRangesArray);
                this.mNMS.removeVpnUidRanges(nai.network.netId, removedRangesArray);
            }
            boolean wasFiltering = requiresVpnIsolation(nai, prevNc, nai.linkProperties);
            boolean shouldFilter = requiresVpnIsolation(nai, newNc, nai.linkProperties);
            String iface = nai.linkProperties.getInterfaceName();
            if (wasFiltering && !prevRanges.isEmpty()) {
                this.mPermissionMonitor.onVpnUidRangesRemoved(iface, prevRanges, prevNc.getEstablishingVpnAppUid());
            }
            if (shouldFilter && !newRanges.isEmpty()) {
                this.mPermissionMonitor.onVpnUidRangesAdded(iface, newRanges, newNc.getEstablishingVpnAppUid());
            }
        } catch (Exception e) {
            loge("Exception in updateUids: ", e);
        }
    }

    public void handleUpdateLinkProperties(NetworkAgentInfo nai, LinkProperties newLp) {
        ensureRunningOnConnectivityServiceThread();
        if (getNetworkAgentInfoForNetId(nai.network.netId) == nai) {
            newLp.ensureDirectlyConnectedRoutes();
            if (VDBG || DDBG) {
                log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
            }
            updateLinkProperties(nai, newLp, new LinkProperties(nai.linkProperties));
        }
    }

    /* access modifiers changed from: private */
    public void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest nr = nai.requestAt(i);
            if (!nr.isListen()) {
                sendUpdatedScoreToFactories(nr, nai);
            }
        }
    }

    private void sendUpdatedScoreToFactories(NetworkRequest networkRequest, NetworkAgentInfo nai) {
        int score = 0;
        int serial = 0;
        if (nai != null) {
            score = nai.getCurrentScore();
            serial = nai.factorySerialNumber;
        }
        if (VDBG || DDBG) {
            log("sending new Min Network Score(" + score + "): " + networkRequest.toString());
        }
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            nfi.asyncChannel.sendMessage(536576, score, serial, networkRequest);
        }
    }

    private void sendPendingIntentForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType) {
        if (notificationType == 524290 && !nri.mPendingIntentSent) {
            Intent intent = new Intent();
            intent.putExtra("android.net.extra.NETWORK", networkAgent.network);
            intent.putExtra("android.net.extra.NETWORK_REQUEST", nri.request);
            nri.mPendingIntentSent = true;
            sendIntent(nri.mPendingIntent, intent);
        }
    }

    private void sendIntent(PendingIntent pendingIntent, Intent intent) {
        this.mPendingIntentWakeLock.acquire();
        try {
            log("Sending " + pendingIntent);
            pendingIntent.send(this.mContext, 0, intent, this, null);
        } catch (PendingIntent.CanceledException e) {
            log(pendingIntent + " was not sent, it had been canceled.");
            this.mPendingIntentWakeLock.release();
            releasePendingNetworkRequest(pendingIntent);
        }
    }

    public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
        log("Finished sending " + pendingIntent);
        this.mPendingIntentWakeLock.release();
        releasePendingNetworkRequestWithDelay(pendingIntent);
    }

    private void callCallbackForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType, int arg1) {
        if (nri.messenger != null) {
            Bundle bundle = new Bundle();
            putParcelable(bundle, new NetworkRequest(nri.request));
            Message msg = Message.obtain();
            if (notificationType != 524293) {
                putParcelable(bundle, networkAgent.network);
            }
            switch (notificationType) {
                case 524290:
                    putParcelable(bundle, networkCapabilitiesRestrictedForCallerPermissions(networkAgent.networkCapabilities, nri.mPid, nri.mUid));
                    putParcelable(bundle, new LinkProperties(networkAgent.linkProperties));
                    msg.arg1 = arg1;
                    break;
                case 524291:
                    msg.arg1 = arg1;
                    break;
                case 524294:
                    putParcelable(bundle, networkCapabilitiesRestrictedForCallerPermissions(networkAgent.networkCapabilities, nri.mPid, nri.mUid));
                    break;
                case 524295:
                    putParcelable(bundle, new LinkProperties(networkAgent.linkProperties));
                    break;
                case 524299:
                    maybeLogBlockedStatusChanged(nri, networkAgent.network, arg1 != 0);
                    msg.arg1 = arg1;
                    break;
            }
            msg.what = notificationType;
            msg.setData(bundle);
            try {
                if (VDBG) {
                    String notification = ConnectivityManager.getCallbackName(notificationType);
                    log("sending notification " + notification + " for " + nri.request);
                }
                nri.messenger.send(msg);
            } catch (RemoteException e) {
                loge("RemoteException caught trying to send a callback msg for " + nri.request);
            }
        }
    }

    private static <T extends Parcelable> void putParcelable(Bundle bundle, T t) {
        bundle.putParcelable(t.getClass().getSimpleName(), t);
    }

    /* access modifiers changed from: private */
    public void teardownUnneededNetwork(NetworkAgentInfo nai) {
        if (nai.numRequestNetworkRequests() != 0) {
            int i = 0;
            while (true) {
                if (i < nai.numNetworkRequests()) {
                    NetworkRequest nr = nai.requestAt(i);
                    if (!nr.isListen()) {
                        loge("Dead network still had at least " + nr);
                        break;
                    }
                    i++;
                }
            }
        }
        try {
            nai.asyncChannel.disconnect();
        } catch (Exception ex) {
            loge("disconnect error: " + ex);
        }
    }

    /* access modifiers changed from: private */
    public void handleLingerComplete(NetworkAgentInfo oldNetwork) {
        if (oldNetwork == null) {
            loge("Unknown NetworkAgentInfo in handleLingerComplete");
            return;
        }
        log("handleLingerComplete for " + oldNetwork.name());
        oldNetwork.clearLingerState();
        if (unneeded(oldNetwork, UnneededFor.TEARDOWN)) {
            teardownUnneededNetwork(oldNetwork);
        } else {
            updateCapabilities(oldNetwork.getCurrentScore(), oldNetwork, oldNetwork.networkCapabilities);
        }
    }

    private void makeDefault(NetworkAgentInfo newNetwork) {
        log("Switching to new default network: " + newNetwork);
        try {
            this.mNMS.setDefaultNetId(newNetwork.network.netId);
        } catch (Exception e) {
            loge("Exception setting default network :" + e);
        }
        notifyLockdownVpn(newNetwork);
        handleApplyDefaultProxy(newNetwork.linkProperties.getHttpProxy());
        updateTcpBufferSizes(newNetwork.linkProperties.getTcpBufferSizes());
        this.mDnsManager.setDefaultDnsSystemProperties(newNetwork.linkProperties.getDnsServers());
        if (newNetwork.linkProperties.isPrivateDnsActive()) {
            OppoPrivateDnsHelper.getInstance(this.mContext).onDefaultNetworkChanged();
            List<InetAddress> dnses = newNetwork.linkProperties.getValidatedPrivateDnsServers();
            if (!ArrayUtils.isEmpty(dnses)) {
                log("makeDefault privateDns dnses" + dnses);
                Settings.Global.putInt(this.mContext.getContentResolver(), "private_dns_validating_status", 2);
            } else {
                log("makeDefault set privateDnsStatus fail");
                Settings.Global.putInt(this.mContext.getContentResolver(), "private_dns_validating_status", 0);
            }
        }
        notifyIfacesChangedForNetworkStats();
        updateAllVpnsCapabilities();
    }

    private void processListenRequests(NetworkAgentInfo nai, boolean capabilitiesChanged) {
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            NetworkRequest nr = nri.request;
            if (nr.isListen() && nai.isSatisfyingRequest(nr.requestId) && !nai.satisfies(nr)) {
                nai.removeRequest(nri.request.requestId);
                callCallbackForRequest(nri, nai, 524292, 0);
            }
        }
        if (capabilitiesChanged) {
            notifyNetworkCallbacks(nai, 524294);
        }
        for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
            NetworkRequest nr2 = nri2.request;
            if (nr2.isListen() && nai.satisfies(nr2) && !nai.isSatisfyingRequest(nr2.requestId)) {
                nai.addRequest(nr2);
                notifyNetworkAvailable(nai, nri2);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r12v15 'currentNetwork'  com.android.server.connectivity.NetworkAgentInfo: [D('currentNetwork' com.android.server.connectivity.NetworkAgentInfo), D('score' int)] */
    private void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks, long now) {
        int i;
        int score;
        boolean wasBackgroundNetwork;
        NetworkAgentInfo oldDefaultNetwork;
        boolean wasBackgroundNetwork2;
        int score2;
        NetworkAgentInfo currentNetwork;
        NetworkAgentInfo oldDefaultNetwork2;
        if (newNetwork.everConnected) {
            boolean keep = newNetwork.isVPN();
            boolean wasBackgroundNetwork3 = newNetwork.isBackgroundNetwork();
            int score3 = newNetwork.getCurrentScore();
            if (VDBG || DDBG) {
                log("rematching " + newNetwork.name());
            }
            ArrayList<NetworkAgentInfo> affectedNetworks = new ArrayList<>();
            ArrayList<NetworkRequestInfo> addedRequests = new ArrayList<>();
            NetworkCapabilities nc = newNetwork.networkCapabilities;
            if (VDBG) {
                log(" network has: " + nc);
            }
            Iterator<NetworkRequestInfo> it = this.mNetworkRequests.values().iterator();
            boolean keep2 = keep;
            boolean isNewDefault = false;
            NetworkAgentInfo oldDefaultNetwork3 = null;
            while (true) {
                int i2 = 0;
                if (it.hasNext()) {
                    NetworkRequestInfo nri = it.next();
                    if (!nri.request.isListen()) {
                        NetworkAgentInfo currentNetwork2 = getNetworkForRequest(nri.request.requestId);
                        boolean satisfies = newNetwork.satisfies(nri.request);
                        if (newNetwork != currentNetwork2 || !satisfies) {
                            if (VDBG) {
                                log("  checking if request is satisfied: " + nri.request);
                            }
                            if (satisfies) {
                                if (VDBG || DDBG) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("currentScore = ");
                                    sb.append(currentNetwork2 != null ? currentNetwork2.getCurrentScore() : 0);
                                    sb.append(", newScore = ");
                                    sb.append(score3);
                                    log(sb.toString());
                                }
                                if (currentNetwork2 != null && currentNetwork2.getCurrentScore() >= score3) {
                                    wasBackgroundNetwork2 = wasBackgroundNetwork3;
                                    score2 = score3;
                                    oldDefaultNetwork = oldDefaultNetwork3;
                                } else if (newNetwork.networkInfo == null || newNetwork.networkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                                    if (VDBG) {
                                        log("rematch for " + newNetwork.name());
                                    }
                                    if (currentNetwork2 != null) {
                                        if (VDBG || DDBG) {
                                            log("   accepting network in place of " + currentNetwork2.name());
                                        }
                                        currentNetwork2.removeRequest(nri.request.requestId);
                                        wasBackgroundNetwork = wasBackgroundNetwork3;
                                        score = score3;
                                        oldDefaultNetwork2 = oldDefaultNetwork3;
                                        currentNetwork = currentNetwork2;
                                        currentNetwork2.lingerRequest(nri.request, now, (long) this.mLingerDelayMs);
                                        affectedNetworks.add(currentNetwork);
                                    } else {
                                        wasBackgroundNetwork = wasBackgroundNetwork3;
                                        score = score3;
                                        oldDefaultNetwork2 = oldDefaultNetwork3;
                                        currentNetwork = currentNetwork2;
                                        if (VDBG || DDBG) {
                                            log("   accepting network in place of null");
                                        }
                                    }
                                    newNetwork.unlingerRequest(nri.request);
                                    setNetworkForRequest(nri.request.requestId, newNetwork);
                                    if (!newNetwork.addRequest(nri.request)) {
                                        Slog.e(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                                    }
                                    addedRequests.add(nri);
                                    sendUpdatedScoreToFactories(nri.request, newNetwork);
                                    if (isDefaultRequest(nri)) {
                                        if (currentNetwork != null) {
                                            this.mLingerMonitor.noteLingerDefaultNetwork(currentNetwork, newNetwork);
                                        }
                                        keep2 = true;
                                        isNewDefault = true;
                                        oldDefaultNetwork3 = currentNetwork;
                                    } else {
                                        keep2 = true;
                                        oldDefaultNetwork3 = oldDefaultNetwork2;
                                    }
                                    wasBackgroundNetwork3 = wasBackgroundNetwork;
                                    score3 = score;
                                } else {
                                    log("reset the network score to 0 for the network is DISCONNECTED,newNetwork :" + newNetwork);
                                    newNetwork.setCurrentScore(0);
                                    return;
                                }
                            } else {
                                wasBackgroundNetwork2 = wasBackgroundNetwork3;
                                score2 = score3;
                                oldDefaultNetwork = oldDefaultNetwork3;
                                if (newNetwork.isSatisfyingRequest(nri.request.requestId)) {
                                    log("Network " + newNetwork.name() + " stopped satisfying request " + nri.request.requestId);
                                    newNetwork.removeRequest(nri.request.requestId);
                                    if (currentNetwork2 == newNetwork) {
                                        clearNetworkForRequest(nri.request.requestId);
                                        sendUpdatedScoreToFactories(nri.request, null);
                                    } else {
                                        Slog.e(TAG, "BUG: Removing request " + nri.request.requestId + " from " + newNetwork.name() + " without updating mNetworkForRequestId or factories!");
                                    }
                                    callCallbackForRequest(nri, newNetwork, 524292, 0);
                                }
                            }
                            oldDefaultNetwork3 = oldDefaultNetwork;
                            wasBackgroundNetwork3 = wasBackgroundNetwork;
                            score3 = score;
                        } else {
                            if (VDBG) {
                                log("Network " + newNetwork.name() + " was already satisfying request " + nri.request.requestId + ". No change.");
                            }
                            keep2 = true;
                        }
                    }
                } else {
                    if (isNewDefault) {
                        this.mCsHelper.handleNetworkTypeChange(oldDefaultNetwork3, newNetwork);
                        updateDataActivityTracking(newNetwork, oldDefaultNetwork3);
                        if (newNetwork.networkInfo != null) {
                            if (newNetwork.networkInfo.getType() == 1) {
                                this.mCsHelper.notifyDefaultNetork(false);
                            } else if (newNetwork.networkInfo.getType() == 0) {
                                this.mCsHelper.notifyDefaultNetork(true);
                            }
                        }
                        makeDefault(newNetwork);
                        metricsLogger().defaultNetworkMetrics().logDefaultNetworkEvent(now, newNetwork, oldDefaultNetwork3);
                        scheduleReleaseNetworkTransitionWakelock();
                    }
                    if (!newNetwork.networkCapabilities.equalRequestableCapabilities(nc)) {
                        Slog.e(TAG, String.format("BUG: %s changed requestable capabilities during rematch: %s -> %s", newNetwork.name(), nc, newNetwork.networkCapabilities));
                    }
                    if (newNetwork.getCurrentScore() != score3) {
                        Slog.e(TAG, String.format("BUG: %s changed score during rematch: %d -> %d", newNetwork.name(), Integer.valueOf(score3), Integer.valueOf(newNetwork.getCurrentScore())));
                    }
                    if (wasBackgroundNetwork3 != newNetwork.isBackgroundNetwork()) {
                        updateCapabilities(score3, newNetwork, newNetwork.networkCapabilities);
                    } else {
                        processListenRequests(newNetwork, false);
                    }
                    Iterator<NetworkRequestInfo> it2 = addedRequests.iterator();
                    while (it2.hasNext()) {
                        notifyNetworkAvailable(newNetwork, it2.next());
                    }
                    Iterator<NetworkAgentInfo> it3 = affectedNetworks.iterator();
                    while (it3.hasNext()) {
                        updateLingerState(it3.next(), now);
                    }
                    updateLingerState(newNetwork, now);
                    if (isNewDefault) {
                        if (oldDefaultNetwork3 != null) {
                            this.mLegacyTypeTracker.remove(oldDefaultNetwork3.networkInfo.getType(), oldDefaultNetwork3, true);
                        }
                        this.mDefaultInetConditionPublished = newNetwork.lastValidated ? 100 : 0;
                        this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                        notifyLockdownVpn(newNetwork);
                    }
                    if (keep2) {
                        try {
                            IBatteryStats bs = BatteryStatsService.getService();
                            int type = newNetwork.networkInfo.getType();
                            bs.noteNetworkInterfaceType(newNetwork.linkProperties.getInterfaceName(), type);
                            for (LinkProperties stacked : newNetwork.linkProperties.getStackedLinks()) {
                                bs.noteNetworkInterfaceType(stacked.getInterfaceName(), type);
                            }
                        } catch (RemoteException e) {
                        }
                        for (int i3 = 0; i3 < newNetwork.numNetworkRequests(); i3++) {
                            NetworkRequest nr = newNetwork.requestAt(i3);
                            if (!(nr == null || nr.legacyType == -1 || !nr.isRequest())) {
                                this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                            }
                        }
                        if (newNetwork.isVPN()) {
                            this.mLegacyTypeTracker.add(EVENT_REGISTER_NETWORK_FACTORY, newNetwork);
                        }
                    }
                    if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                            if (!unneeded(nai, UnneededFor.TEARDOWN)) {
                                i = i2;
                            } else if (nai.getLingerExpiry() > DISABLE_DATA_ALERT_BYTES) {
                                updateLingerState(nai, now);
                                i = i2;
                            } else {
                                log("Reaping " + nai.name());
                                if (!this.mCsHelper.hasWifiAssistant || !this.mShouldKeepCelluarNetwork || !nai.networkCapabilities.hasTransport(i2)) {
                                    if (!this.mCsHelper.hasWifiAssistant || (!nai.networkCapabilities.hasTransport(1) && !nai.networkCapabilities.hasTransport(EVENT_CLEAR_NET_TRANSITION_WAKELOCK))) {
                                        if (!this.mCsHelper.hasWifiAssistant || !this.mShouldKeepCelluarNetwork) {
                                            i = 0;
                                        } else {
                                            i = 0;
                                            if (nai.networkCapabilities.hasTransport(0)) {
                                                if (WLAN_ASSIST_DBG) {
                                                    log("WLAN+ Ignore Reaping CelluarNetwork: " + nai.name());
                                                }
                                                i2 = 0;
                                            }
                                        }
                                        teardownUnneededNetwork(nai);
                                    } else {
                                        log("wifi_assist type wifi ,no reaping");
                                        i2 = 0;
                                    }
                                } else if (WLAN_ASSIST_DBG) {
                                    log("WLAN+ Ignore Reaping CelluarNetwork: " + nai.name());
                                }
                            }
                            i2 = i;
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    private void rematchAllNetworksAndRequests(NetworkAgentInfo changed, int oldScore) {
        ReapUnvalidatedNetworks reapUnvalidatedNetworks;
        long now = SystemClock.elapsedRealtime();
        if (changed == null || oldScore >= changed.getCurrentScore()) {
            NetworkAgentInfo[] nais = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[this.mNetworkAgentInfos.size()]);
            Arrays.sort(nais);
            for (NetworkAgentInfo nai : nais) {
                if (nai != nais[nais.length - 1]) {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.DONT_REAP;
                } else {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.REAP;
                }
                rematchNetworkAndRequests(nai, reapUnvalidatedNetworks, now);
            }
            return;
        }
        rematchNetworkAndRequests(changed, ReapUnvalidatedNetworks.REAP, now);
    }

    /* access modifiers changed from: private */
    public void updateInetCondition(NetworkAgentInfo nai) {
        if (nai.everValidated && isDefaultNetwork(nai)) {
            int newInetCondition = nai.lastValidated ? 100 : 0;
            if (newInetCondition != this.mDefaultInetConditionPublished) {
                this.mDefaultInetConditionPublished = newInetCondition;
                sendInetConditionBroadcast(nai.networkInfo);
            }
        }
    }

    private void notifyLockdownVpn(NetworkAgentInfo nai) {
        synchronized (this.mVpns) {
            if (this.mLockdownTracker != null) {
                if (nai == null || !nai.isVPN()) {
                    this.mLockdownTracker.onNetworkInfoChanged();
                } else {
                    this.mLockdownTracker.onVpnStateChanged(nai.networkInfo);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateNetworkInfo(NetworkAgentInfo networkAgent, NetworkInfo newInfo, boolean isNewCreated) {
        NetworkInfo oldInfo;
        int i;
        NetworkInfo.State state = newInfo.getState();
        int oldScore = networkAgent.getCurrentScore();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        notifyLockdownVpn(networkAgent);
        StringBuilder sb = new StringBuilder();
        sb.append(networkAgent.name());
        sb.append(" EVENT_NETWORK_INFO_CHANGED, going from ");
        sb.append(isNewCreated ? "initial" : oldInfo == null ? "null" : oldInfo.getState());
        sb.append(" to ");
        sb.append(state);
        log(sb.toString());
        if (!networkAgent.created && (state == NetworkInfo.State.CONNECTED || (state == NetworkInfo.State.CONNECTING && networkAgent.isVPN()))) {
            networkAgent.networkCapabilities.addCapability(EVENT_REGISTER_NETWORK_REQUEST);
            if (createNativeNetwork(networkAgent)) {
                networkAgent.created = true;
            } else {
                return;
            }
        }
        if (!networkAgent.everConnected && state == NetworkInfo.State.CONNECTED) {
            networkAgent.everConnected = true;
            if (networkAgent.linkProperties == null) {
                String str = TAG;
                Slog.wtf(str, networkAgent.name() + " connected with null LinkProperties");
            }
            this.mCsHelper.updateOppoSlaIface(networkAgent, true);
            synchronized (networkAgent) {
                networkAgent.setNetworkCapabilities(networkAgent.networkCapabilities);
            }
            handlePerNetworkPrivateDnsConfig(networkAgent, this.mDnsManager.getPrivateDnsConfig());
            updateLinkProperties(networkAgent, new LinkProperties(networkAgent.linkProperties), null);
            this.mCsHelper.startGatewayDetector(networkAgent);
            if (networkAgent.mGatewayState.needWaitGatewayDetector()) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_NOTIFY_NETWORK_CONNECTED, networkAgent.network), 2000);
            } else {
                this.mCsHelper.notifyNetworkConnected(networkAgent);
            }
            scheduleUnvalidatedPrompt(networkAgent);
            updateSignalStrengthThresholds(networkAgent, "CONNECT", null);
            if (networkAgent.isVPN()) {
                updateAllVpnsCapabilities();
            }
            rematchNetworkAndRequests(networkAgent, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            notifyNetworkCallbacks(networkAgent, 524289);
        } else if (state == NetworkInfo.State.DISCONNECTED) {
            networkAgent.asyncChannel.disconnect();
            if (networkAgent.isVPN()) {
                updateUids(networkAgent, networkAgent.networkCapabilities, null);
            }
            disconnectAndDestroyNetwork(networkAgent);
            if (networkAgent.isVPN()) {
                this.mProxyTracker.sendProxyBroadcast();
            }
        } else if ((!isNewCreated && oldInfo != null && oldInfo.getState() == NetworkInfo.State.SUSPENDED) || state == NetworkInfo.State.SUSPENDED) {
            if (networkAgent.getCurrentScore() != oldScore) {
                rematchAllNetworksAndRequests(networkAgent, oldScore);
            }
            updateCapabilities(networkAgent.getCurrentScore(), networkAgent, networkAgent.networkCapabilities);
            if (state == NetworkInfo.State.SUSPENDED) {
                i = 524297;
            } else {
                i = 524298;
            }
            notifyNetworkCallbacks(networkAgent, i);
            this.mLegacyTypeTracker.update(networkAgent);
        }
        if (this.mCsHelper.hasWifiAssistant && networkAgent.networkInfo != null) {
            String extraStr = networkAgent.networkInfo.getExtraInfo();
            boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
            boolean isCellular = networkAgent.networkCapabilities.hasTransport(0);
            boolean isConnected = networkAgent.networkInfo.isConnected();
            if (!isImsApn && isCellular) {
                if (isConnected && this.mShouldKeepCelluarNetwork && !this.mBlock) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ CELLULAR CONNECTED! request measureDataState with new Thread!");
                    }
                    new Thread() {
                        /* class com.android.server.MtkConnectivityService.AnonymousClass8 */

                        public void run() {
                            MtkConnectivityService.this.measureDataState(0);
                        }
                    }.start();
                } else if (state == NetworkInfo.State.SUSPENDED && this.mShouldKeepCelluarNetwork) {
                    handleUpdateNetworkScore(networkAgent, INVALID_SCORE);
                    sendBroadcastDataScore(INVALID_SCORE);
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+  CELLULAR not CONNECTED! update DataScore to wifi!");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateNetworkScore(NetworkAgentInfo nai, int score) {
        if (VDBG || DDBG) {
            log("updateNetworkScore for " + nai.name() + " to " + score);
        }
        if (score < 0) {
            loge("updateNetworkScore for " + nai.name() + " got a negative score (" + score + ").  Bumping score to min of 0");
            score = 0;
        }
        int oldScore = nai.getCurrentScore();
        nai.setCurrentScore(score);
        rematchAllNetworksAndRequests(nai, oldScore);
        sendUpdatedScoreToFactories(nai);
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkAvailable(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        if (nri == null) {
            log("Error: notifyNetworkAvailable nri = null !!! ");
            return;
        }
        this.mHandler.removeMessages(20, nri);
        if (nri.mPendingIntent != null) {
            sendPendingIntentForRequest(nri, nai, 524290);
            return;
        }
        callCallbackForRequest(nri, nai, 524290, isUidNetworkingWithVpnBlocked(nri.mUid, this.mUidRules.get(nri.mUid), nai.networkCapabilities.isMetered(), this.mRestrictBackground) ? 1 : 0);
    }

    private void maybeNotifyNetworkBlocked(NetworkAgentInfo nai, boolean oldMetered, boolean newMetered, boolean oldRestrictBackground, boolean newRestrictBackground) {
        boolean oldBlocked;
        boolean newBlocked;
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequestInfo nri = this.mNetworkRequests.get(nai.requestAt(i));
            int uidRules = this.mUidRules.get(nri.mUid);
            synchronized (this.mVpns) {
                oldBlocked = isUidNetworkingWithVpnBlocked(nri.mUid, uidRules, oldMetered, oldRestrictBackground);
                newBlocked = isUidNetworkingWithVpnBlocked(nri.mUid, uidRules, newMetered, newRestrictBackground);
            }
            if (oldBlocked != newBlocked) {
                callCallbackForRequest(nri, nai, 524299, encodeBool(newBlocked));
            }
        }
    }

    private void maybeNotifyNetworkBlockedForNewUidRules(int uid, int newRules) {
        boolean oldBlocked;
        boolean newBlocked;
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            boolean metered = nai.networkCapabilities.isMetered();
            synchronized (this.mVpns) {
                oldBlocked = isUidNetworkingWithVpnBlocked(uid, this.mUidRules.get(uid), metered, this.mRestrictBackground);
                newBlocked = isUidNetworkingWithVpnBlocked(uid, newRules, metered, this.mRestrictBackground);
            }
            if (oldBlocked != newBlocked) {
                int arg = encodeBool(newBlocked);
                for (int i = 0; i < nai.numNetworkRequests(); i++) {
                    NetworkRequestInfo nri = this.mNetworkRequests.get(nai.requestAt(i));
                    if (nri != null && nri.mUid == uid) {
                        callCallbackForRequest(nri, nai, 524299, arg);
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void sendLegacyNetworkBroadcast(NetworkAgentInfo nai, NetworkInfo.DetailedState state, int type) {
        NetworkInfo info = new NetworkInfo(nai.networkInfo);
        info.setType(type);
        if (state != NetworkInfo.DetailedState.DISCONNECTED) {
            info.setDetailedState(state, null, info.getExtraInfo());
            sendConnectedBroadcast(info);
            return;
        }
        info.setDetailedState(state, info.getReason(), info.getExtraInfo());
        Intent intent = new Intent("android.net.conn.CONNECTIVITY_CHANGE");
        intent.putExtra("networkInfo", info);
        intent.putExtra("networkType", info.getType());
        if (info.isFailover()) {
            intent.putExtra("isFailover", true);
            nai.networkInfo.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra("reason", info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra("extraInfo", info.getExtraInfo());
        }
        NetworkAgentInfo newDefaultAgent = null;
        if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
            newDefaultAgent = getDefaultNetwork();
            if (newDefaultAgent != null) {
                intent.putExtra("otherNetwork", newDefaultAgent.networkInfo);
            } else {
                intent.putExtra("noConnectivity", true);
            }
        }
        intent.putExtra("inetCondition", this.mDefaultInetConditionPublished);
        sendStickyBroadcast(intent);
        if (newDefaultAgent != null) {
            sendConnectedBroadcast(newDefaultAgent.networkInfo);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType, int arg1) {
        if (VDBG || DDBG) {
            String notification = ConnectivityManager.getCallbackName(notifyType);
            log("notifyType " + notification + " for " + networkAgent.name());
        }
        for (int i = 0; i < networkAgent.numNetworkRequests(); i++) {
            NetworkRequest nr = networkAgent.requestAt(i);
            NetworkRequestInfo nri = this.mNetworkRequests.get(nr);
            if (VDBG) {
                log(" sending notification for " + nr);
            }
            if (nri != null) {
                if (nri.mPendingIntent == null) {
                    callCallbackForRequest(nri, networkAgent, notifyType, arg1);
                } else {
                    sendPendingIntentForRequest(nri, networkAgent, notifyType);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        notifyNetworkCallbacks(networkAgent, notifyType, 0);
    }

    private Network[] getDefaultNetworks() {
        ensureRunningOnConnectivityServiceThread();
        ArrayList<Network> defaultNetworks = new ArrayList<>();
        NetworkAgentInfo defaultNetwork = getDefaultNetwork();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.everConnected && (nai == defaultNetwork || nai.isVPN())) {
                defaultNetworks.add(nai.network);
            }
        }
        return (Network[]) defaultNetworks.toArray(new Network[0]);
    }

    private void notifyIfacesChangedForNetworkStats() {
        ensureRunningOnConnectivityServiceThread();
        String activeIface = null;
        LinkProperties activeLinkProperties = getActiveLinkProperties();
        if (activeLinkProperties != null) {
            activeIface = activeLinkProperties.getInterfaceName();
        }
        try {
            this.mStatsService.forceUpdateIfaces(getDefaultNetworks(), getAllVpnInfo(), getAllNetworkState(), activeIface);
        } catch (Exception e) {
        }
    }

    public boolean addVpnAddress(String address, int prefixLength) {
        boolean addAddress;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            addAddress = this.mVpns.get(user).addAddress(address, prefixLength);
        }
        return addAddress;
    }

    public boolean removeVpnAddress(String address, int prefixLength) {
        boolean removeAddress;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            removeAddress = this.mVpns.get(user).removeAddress(address, prefixLength);
        }
        return removeAddress;
    }

    public boolean setUnderlyingNetworksForVpn(Network[] networks) {
        boolean success;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            success = this.mVpns.get(user).setUnderlyingNetworks(networks);
        }
        if (success) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.$$Lambda$MtkConnectivityService$sR6i36M5tag13DlqedVqP9sS3mU */

                public final void run() {
                    MtkConnectivityService.this.lambda$setUnderlyingNetworksForVpn$5$MtkConnectivityService();
                }
            });
        }
        return success;
    }

    public /* synthetic */ void lambda$setUnderlyingNetworksForVpn$5$MtkConnectivityService() {
        updateAllVpnsCapabilities();
        notifyIfacesChangedForNetworkStats();
    }

    public String getCaptivePortalServerUrl() {
        enforceConnectivityInternalPermission();
        String settingUrl = this.mContext.getResources().getString(17039758);
        if (!TextUtils.isEmpty(settingUrl)) {
            return settingUrl;
        }
        String settingUrl2 = Settings.Global.getString(this.mContext.getContentResolver(), "captive_portal_http_url");
        if (!TextUtils.isEmpty(settingUrl2)) {
            return settingUrl2;
        }
        return DEFAULT_CAPTIVE_PORTAL_HTTP_URL;
    }

    public void startNattKeepalive(Network network, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), (FileDescriptor) null, intervalSeconds, cb, srcAddr, srcPort, dstAddr, 4500);
    }

    public void startNattKeepaliveWithFd(Network network, FileDescriptor fd, int resourceId, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, String dstAddr) {
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), fd, resourceId, intervalSeconds, cb, srcAddr, dstAddr, 4500);
    }

    public void startTcpKeepalive(Network network, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startTcpKeepalive(getNetworkAgentInfoForNetwork(network), fd, intervalSeconds, cb);
    }

    public void stopKeepalive(Network network, int slot) {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(528396, slot, 0, network));
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
            Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable() {
                /* class com.android.server.$$Lambda$MtkConnectivityService$W50RTpxVaxHAoNdYsDCEbPOxVk */

                public final void runOrThrow() {
                    MtkConnectivityService.this.lambda$factoryReset$6$MtkConnectivityService();
                }
            });
            setAirplaneMode(false);
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                String pkgName = this.mContext.getOpPackageName();
                for (String tether : getTetheredIfaces()) {
                    untether(tether, pkgName);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_vpn")) {
                synchronized (this.mVpns) {
                    String alwaysOnPackage = getAlwaysOnVpnPackage(userId);
                    if (alwaysOnPackage != null) {
                        setAlwaysOnVpnPackage(userId, null, false, null);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, false);
                    }
                    if (this.mLockdownEnabled && userId == 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            this.mKeyStore.delete("LOCKDOWN_VPN");
                            this.mLockdownEnabled = false;
                            setLockdownTracker(null);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    VpnConfig vpnConfig = getVpnConfig(userId);
                    if (vpnConfig != null) {
                        if (vpnConfig.legacy) {
                            prepareVpn("[Legacy VPN]", "[Legacy VPN]", userId);
                        } else {
                            setVpnPackageAuthorization(vpnConfig.user, userId, false);
                            prepareVpn(null, "[Legacy VPN]", userId);
                        }
                    }
                }
            }
            if (!this.mUserManager.hasUserRestriction("disallow_config_private_dns")) {
                Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_mode", "opportunistic");
            }
            Settings.Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", null);
        }
    }

    public /* synthetic */ void lambda$factoryReset$6$MtkConnectivityService() throws Exception {
        IpMemoryStore.getMemoryStore(this.mContext).factoryReset();
    }

    private class ConnectivityServiceReceiver extends BroadcastReceiver {
        private ConnectivityServiceReceiver() {
        }

        public void onReceive(final Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String access$1600 = MtkConnectivityService.TAG;
                Slog.d(access$1600, "received intent ==> " + action);
                if (MtkConnectivityService.AUTO_TETHERING_INTENT.equals(action)) {
                    boolean enable = intent.getBooleanExtra("tethering_isconnected", true);
                    if (MtkConnectivityService.this.isTetheringSupported()) {
                        MtkConnectivityService.this.mTethering.setUsbTethering(enable);
                    }
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    if (MtkConnectivityService.sIsAutoTethering) {
                        if (MtkConnectivityService.this.isTetheringSupported()) {
                            MtkConnectivityService.this.mTethering.setUsbTethering(true);
                        }
                        MtkConnectivityService.this.mHandler.sendMessageDelayed(MtkConnectivityService.this.mHandler.obtainMessage(100), 3000);
                    }
                } else if (MtkConnectivityService.ACTION_DISABLE_DATA_ALERT.equals(action)) {
                    new Thread("IGNORE_DATA_USAGE_ALERT") {
                        /* class com.android.server.MtkConnectivityService.ConnectivityServiceReceiver.AnonymousClass1 */

                        public void run() {
                            Slog.d(MtkConnectivityService.TAG, "Disable data usage alert");
                            Settings.Global.putLong(context.getContentResolver(), "netstats_global_alert_bytes", MtkConnectivityService.DISABLE_DATA_ALERT_BYTES);
                        }
                    }.start();
                }
            }
        }
    }

    private boolean ignoreMmsReqInTddOnly(NetworkCapabilities nc) {
        if (!TDD_DATA_ONLY_SUPPORT || !nc.hasCapability(0) || !nc.hasTransport(0)) {
            return false;
        }
        int subId = -1;
        try {
            StringNetworkSpecifier ns = nc.getNetworkSpecifier();
            if (ns != null) {
                subId = Integer.parseInt(ns.toString());
            }
        } catch (NumberFormatException e) {
            loge("ignoreMmsReqInTddOnly: NumberFormatException");
        }
        MtkLteDataOnlyController ldoc = new MtkLteDataOnlyController(this.mContext);
        if (subId != -1) {
            if (!ldoc.checkPermission(subId)) {
                return true;
            }
            return false;
        } else if (!ldoc.checkPermission()) {
            return true;
        } else {
            return false;
        }
    }

    private InetAddress addDefaultDnsServer(int netId, LinkProperties lp) {
        InetAddress inetAddress;
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(netId);
        boolean hadIPv6 = true;
        boolean isDefaultDnsEnabled = !isTestSim();
        Collection<InetAddress> dnses = lp.getDnsServers();
        if (nai == null || (inetAddress = this.mDefaultDns) == null || dnses.contains(inetAddress) || !nai.networkCapabilities.hasCapability(12)) {
            return null;
        }
        if (!dnses.isEmpty()) {
            log("Not append default DNS for original DNSes not empy:" + dnses);
            return null;
        }
        if (isDefaultDnsEnabled && nai.networkCapabilities.hasTransport(0)) {
            isDefaultDnsEnabled = !TelephonyManager.getDefault().getSimCountryIso().equalsIgnoreCase(Locale.US.getCountry());
            log("isDefaultNetwork:" + TelephonyManager.getDefault().getSimCountryIso() + ";" + Locale.US.getCountry());
        }
        if (isDefaultDnsEnabled) {
            boolean hadIPv4 = lp.hasIpv4Address() && lp.hasIpv4DefaultRoute();
            if (!lp.hasGlobalIpv6Address() || !lp.hasIpv6DefaultRoute()) {
                hadIPv6 = false;
            }
            if ((this.mDefaultDns instanceof Inet4Address) && hadIPv4) {
                log("add default v4 dns" + this.mDefaultDns);
                return this.mDefaultDns;
            } else if ((this.mDefaultDns instanceof Inet6Address) && hadIPv6) {
                log("add default v6 dns" + this.mDefaultDns);
                return this.mDefaultDns;
            }
        }
        return null;
    }

    private boolean isTestSim() {
        return this.mSystemProperties.get("vendor.gsm.sim.ril.testsim").equals("1") || this.mSystemProperties.get("vendor.gsm.sim.ril.testsim.2").equals("1") || this.mSystemProperties.get("vendor.gsm.sim.ril.testsim.3").equals("1") || this.mSystemProperties.get("vendor.gsm.sim.ril.testsim.4").equals("1");
    }

    public byte[] getNetworkWatchlistConfigHash() {
        NetworkWatchlistManager nwm = (NetworkWatchlistManager) this.mContext.getSystemService(NetworkWatchlistManager.class);
        if (nwm != null) {
            return nwm.getWatchlistConfigHash();
        }
        loge("Unable to get NetworkWatchlistManager");
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public MultinetworkPolicyTracker createMultinetworkPolicyTracker(Context c, Handler h, Runnable r) {
        return new MultinetworkPolicyTracker(c, h, r);
    }

    @VisibleForTesting
    public WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int cmd, Object obj) {
        return new WakeupMessage(c, h, s, cmd, 0, 0, obj);
    }

    @VisibleForTesting
    public boolean hasService(String name) {
        return ServiceManager.checkService(name) != null;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public IpConnectivityMetrics.Logger metricsLogger() {
        return (IpConnectivityMetrics.Logger) Preconditions.checkNotNull((IpConnectivityMetrics.Logger) LocalServices.getService(IpConnectivityMetrics.Logger.class), "no IpConnectivityMetrics service");
    }

    private void logNetworkEvent(NetworkAgentInfo nai, int evtype) {
        this.mMetricsLog.log(nai.network.netId, nai.networkCapabilities.getTransportTypes(), new NetworkEvent(evtype));
    }

    /* access modifiers changed from: private */
    public static boolean toBool(int encodedBoolean) {
        return encodedBoolean != 0;
    }

    private static int encodeBool(boolean b) {
        return b ? 1 : 0;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
    }

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                if ((cmd.hashCode() == 144736062 && cmd.equals("airplane-mode")) ? false : true) {
                    return handleDefaultCommands(cmd);
                }
                String action = getNextArg();
                if ("enable".equals(action)) {
                    MtkConnectivityService.this.setAirplaneMode(true);
                    return 0;
                } else if ("disable".equals(action)) {
                    MtkConnectivityService.this.setAirplaneMode(false);
                    return 0;
                } else if (action == null) {
                    pw.println(Settings.Global.getInt(MtkConnectivityService.this.mContext.getContentResolver(), "airplane_mode_on") == 0 ? "disabled" : "enabled");
                    return 0;
                } else {
                    onHelp();
                    return -1;
                }
            } catch (Exception e) {
                pw.println(e);
                return -1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Connectivity service commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  airplane-mode [enable|disable]");
            pw.println("    Turn airplane mode on or off.");
            pw.println("  airplane-mode");
            pw.println("    Get airplane mode.");
        }
    }

    @GuardedBy({"mVpns"})
    private Vpn getVpnIfOwner() {
        VpnInfo info;
        int uid = Binder.getCallingUid();
        Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
        if (vpn == null || (info = vpn.getVpnInfo()) == null || info.ownerUid != uid) {
            return null;
        }
        return vpn;
    }

    private Vpn enforceActiveVpnOrNetworkStackPermission() {
        if (checkNetworkStackPermission()) {
            return null;
        }
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            if (vpn != null) {
                return vpn;
            }
            throw new SecurityException("App must either be an active VPN or have the NETWORK_STACK permission");
        }
    }

    public int getConnectionOwnerUid(ConnectionInfo connectionInfo) {
        Vpn vpn = enforceActiveVpnOrNetworkStackPermission();
        if (connectionInfo.protocol == OsConstants.IPPROTO_TCP || connectionInfo.protocol == OsConstants.IPPROTO_UDP) {
            int uid = InetDiagMessage.getConnectionOwnerUid(connectionInfo.protocol, connectionInfo.local, connectionInfo.remote);
            if (vpn == null || vpn.appliesToUid(uid)) {
                return uid;
            }
            return -1;
        }
        throw new IllegalArgumentException("Unsupported protocol " + connectionInfo.protocol);
    }

    public boolean isCallerCurrentAlwaysOnVpnApp() {
        boolean z;
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            z = vpn != null && vpn.getAlwaysOn();
        }
        return z;
    }

    public boolean isCallerCurrentAlwaysOnVpnLockdownApp() {
        boolean z;
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            z = vpn != null && vpn.getLockdown();
        }
        return z;
    }

    public IBinder startOrGetTestNetworkService() {
        return startOrGetTestNetworkService(this.mContext, this.mNMS);
    }

    private void ipv6CellulorInit() {
        boolean isOp07 = this.mSystemProperties.get(SYSTEM_PROPERTY_OPTR).equals("OP07");
        log("USP_SUPPORTED=" + USP_SUPPORTED + ", isOp07=" + isOp07);
        if (USP_SUPPORTED) {
            IntentFilter operatorIntentFilter = new IntentFilter();
            operatorIntentFilter.addAction(ACTION_OPERATOR_CONFIG_CHANGED);
            this.mContext.registerReceiver(new SimOperatorReceiver(), operatorIntentFilter);
            if (isOp07) {
                enableIpv6PrivacyCellulor(isOp07);
            }
        } else if (isOp07) {
            enableIpv6PrivacyCellulor(true);
        }
    }

    /* access modifiers changed from: private */
    public void enableIpv6PrivacyCellulor(boolean enable) {
        int mode;
        log("enableIpv6PrivacyCellulor: " + enable);
        if (enable) {
            mode = 2;
        } else {
            mode = 0;
        }
        for (int i = 0; i <= MAX_CELLULAR_INTERFACE; i++) {
            try {
                INetd iNetd = this.mNetd;
                iNetd.setIPv6AddrGenMode("ccmni" + i, mode);
            } catch (RemoteException | ServiceSpecificException e) {
                loge("Unable to set IPv6 addrgen mode: %s", e);
            }
        }
    }

    private class SimOperatorReceiver extends BroadcastReceiver {
        private SimOperatorReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MtkConnectivityService.VDBG) {
                MtkConnectivityService.log("received:" + intent);
            }
            if (MtkConnectivityService.ACTION_OPERATOR_CONFIG_CHANGED.equals(action)) {
                handleOperatorConfigChanged();
            }
        }

        private void handleOperatorConfigChanged() {
            String operator = MtkConnectivityService.this.mSystemProperties.get(MtkConnectivityService.SYSTEM_PROPERTY_OPTR);
            MtkConnectivityService.log("operator=" + operator);
            MtkConnectivityService.this.enableIpv6PrivacyCellulor("OP07".equals(operator));
        }
    }

    private void disableCaptivePortalUseHttps(Context context) {
        String useHttps = DeviceConfig.getProperty("connectivity", NAME_USE_HTTPS);
        String str = TAG;
        Log.d(str, "Use https=" + useHttps);
        if (useHttps == null) {
            DeviceConfig.setProperty("connectivity", NAME_USE_HTTPS, "0", false);
            Log.d(TAG, "Set use https as 0!");
        }
    }

    public String getTelephonyPowerState() {
        return mPowerState;
    }

    public boolean isAlreadyUpdated() {
        return mAlreadyUpdated;
    }

    public double getTelephonyPowerLost() {
        return mPowerLost;
    }

    public void setTelephonyPowerState(String powerState) {
        mPowerState = powerState;
    }

    public void setAlreadyUpdated(boolean alreadyUpdated) {
        mAlreadyUpdated = alreadyUpdated;
    }

    public void setTelephonyPowerLost(double powerLost) {
        mPowerLost = powerLost;
    }

    public NetworkRequest getCelluarNetworkRequest() {
        long now = SystemClock.elapsedRealtime();
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ getCelluarNetworkRequest ENABLE_WLAN_ASSISTANT = " + this.ENABLE_WLAN_ASSISTANT + " (now - lastRequestNetworkTime) = " + (now - this.mLastRequestNetworkTime) + " mShouldKeepCelluarNetwork = " + this.mShouldKeepCelluarNetwork);
        }
        if (now - this.mLastRequestNetworkTime < this.RECONNECT_TIMER || this.ENABLE_WLAN_ASSISTANT <= 0 || !this.mShouldKeepCelluarNetwork) {
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ ignore getCelluarNetworkRequest!");
            }
            return null;
        }
        for (NetworkAgentInfo nai : ((HashMap) this.mNetworkAgentInfos.clone()).values()) {
            if (!(nai == null || nai.networkInfo == null)) {
                String extraStr = nai.networkInfo.getExtraInfo();
                boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                boolean isCellular = nai.networkCapabilities.hasTransport(0);
                boolean isLingering = nai.isLingering();
                if (!isImsApn && isCellular && isLingering) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ CELLULAR isLingering! Ignore!");
                    }
                    return null;
                }
            }
        }
        this.mLastRequestNetworkTime = now;
        for (NetworkRequestInfo nri : ((HashMap) this.mNetworkRequests.clone()).values()) {
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ network request: " + nri.request);
            }
            boolean isCellular2 = nri.request.networkCapabilities.hasTransport(0);
            boolean isInternet = nri.request.networkCapabilities.hasCapability(12);
            boolean isForeground = nri.request.networkCapabilities.hasCapability(EVENT_REGISTER_NETWORK_REQUEST);
            boolean isDefaultNetwork = -1 == nri.request.legacyType;
            if (isInternet && isCellular2 && isDefaultNetwork && !isForeground) {
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ it's CELLULAR INTERNET! request id:" + nri.request.requestId);
                }
                this.mLastMeasureTime = DISABLE_DATA_ALERT_BYTES;
                return nri.request;
            }
        }
        NetworkRequest request = new NetworkRequest.Builder().addCapability(12).addTransportType(0).build();
        this.mLastMeasureTime = DISABLE_DATA_ALERT_BYTES;
        this.mBlock = false;
        return request;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public void releaseCelluarNetworkRequest() {
        Intent qualityIntent = new Intent(WIFI_SCROE_CHANGE);
        qualityIntent.addFlags(67108864);
        qualityIntent.putExtra(EXTRA_ENALE_DATA, false);
        this.mContext.sendStickyBroadcastAsUser(qualityIntent, UserHandle.ALL);
        try {
            for (NetworkRequestInfo nri : ((HashMap) this.mNetworkRequests.clone()).values()) {
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ Network request: " + nri.request);
                }
                boolean isCellular = nri.request.networkCapabilities.hasTransport(0);
                boolean isInternet = nri.request.networkCapabilities.hasCapability(12);
                boolean isRequest = nri.request.type == NetworkRequest.Type.REQUEST;
                if (isCellular && isInternet && isRequest) {
                    releaseNetworkRequest(nri.request);
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ releaseCelluarNetworkRequest request: " + nri.request);
                    }
                }
            }
        } catch (Exception e) {
            loge("WLAN+ releaseCelluarNetworkRequest Exception: " + e.toString());
        }
        this.mLastRequestNetworkTime = DISABLE_DATA_ALERT_BYTES;
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ releaseCelluarNetworkRequest end!");
        }
    }

    public void handleUpdateNetworkScore(NetworkAgentInfo nai, int score) {
        Message msg = Message.obtain();
        msg.what = 528485;
        msg.obj = nai;
        msg.arg1 = score;
        this.mTrackerHandler.sendMessage(msg);
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ handleUpdateNetworkScore score = " + score);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02ce, code lost:
        if (r9.isClosed() != false) goto L_0x0332;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02d0, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x03bb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x03bc, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x03bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x03c0, code lost:
        r3 = r0;
        r7 = r18;
        r9 = r22;
        r10 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x03c8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x03c9, code lost:
        r2 = r0;
        r9 = r22;
        r10 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x03d1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x03d2, code lost:
        r5 = r0;
        r7 = r18;
        r9 = r22;
        r10 = r23;
     */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x0306 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0662  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x06a9  */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x06e7  */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x031c A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:339:0x01b7 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01d8  */
    public boolean measureDataState(int siganlLevel) {
        long token;
        Throwable th;
        IOException e;
        boolean z;
        NullPointerException e2;
        Exception e3;
        long now;
        long token2;
        long RTT;
        boolean celluarFound;
        HashMap<Messenger, NetworkAgentInfo> tempNais;
        NetworkAgentInfo targetNai;
        HashMap<Messenger, NetworkAgentInfo> tempNais2;
        NetworkAgentInfo targetNai2;
        boolean isImsApn;
        long RTT2;
        long now2;
        boolean shouldReMeasure;
        boolean celluarFound2;
        boolean celluarFound3;
        boolean isCellular;
        boolean isImsApn2;
        InetAddress[] hostAddresses;
        boolean shouldReMeasure2;
        Throwable th2;
        SocketFactory socketfactory;
        IOException e4;
        StringBuilder sb;
        long now3 = SystemClock.elapsedRealtime();
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ mBlock = " + this.mBlock + " now - mLastMeasureTime = " + (now3 - this.mLastMeasureTime) + "ENABLE_WLAN_ASSISTANT = " + this.ENABLE_WLAN_ASSISTANT + "mShouldKeepCelluarNetwork = " + this.mShouldKeepCelluarNetwork);
        }
        if (now3 - this.mLastMeasureTime >= this.REMEASURE_TIMER && !this.mBlock && this.ENABLE_WLAN_ASSISTANT > 0) {
            if (this.mShouldKeepCelluarNetwork) {
                NetworkAgentInfo targetNai3 = null;
                boolean celluarFound4 = false;
                long RTT3 = DISABLE_DATA_ALERT_BYTES;
                long token3 = Binder.clearCallingIdentity();
                try {
                    HashMap<Messenger, NetworkAgentInfo> tempNais3 = (HashMap) this.mNetworkAgentInfos.clone();
                    Iterator<NetworkAgentInfo> it = tempNais3.values().iterator();
                    while (it.hasNext()) {
                        try {
                            NetworkAgentInfo nai = it.next();
                            if (nai == null) {
                                now = now3;
                                targetNai = targetNai3;
                                tempNais = tempNais3;
                                celluarFound = celluarFound4;
                                RTT = RTT3;
                                token2 = token3;
                            } else if (nai.networkInfo == null) {
                                now = now3;
                                targetNai = targetNai3;
                                tempNais = tempNais3;
                                celluarFound = celluarFound4;
                                RTT = RTT3;
                                token2 = token3;
                            } else {
                                String extraStr = nai.networkInfo.getExtraInfo();
                                if (extraStr == null) {
                                    targetNai2 = targetNai3;
                                    tempNais2 = tempNais3;
                                    isImsApn = false;
                                } else {
                                    targetNai2 = targetNai3;
                                    tempNais2 = tempNais3;
                                    try {
                                        isImsApn = "ims".equals(extraStr.toLowerCase());
                                    } catch (IOException e5) {
                                        token = token3;
                                        e = e5;
                                        targetNai3 = targetNai2;
                                        loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                        this.mMeasureTime = this.mMeasureTime + 1;
                                        if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                        }
                                        Binder.restoreCallingIdentity(token);
                                        this.mBlock = z;
                                        if (celluarFound4) {
                                        }
                                        return celluarFound4;
                                    } catch (NullPointerException e6) {
                                        token = token3;
                                        e2 = e6;
                                        loge("WLAN+ NullPointerException:" + e2.toString());
                                        Binder.restoreCallingIdentity(token);
                                        this.mBlock = false;
                                        if (celluarFound4) {
                                        }
                                        return celluarFound4;
                                    } catch (Exception e7) {
                                        token = token3;
                                        e3 = e7;
                                        targetNai3 = targetNai2;
                                        try {
                                            loge("WLAN+ Exception:" + e3.toString());
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        token = token3;
                                        th = th4;
                                        Binder.restoreCallingIdentity(token);
                                        this.mBlock = false;
                                        throw th;
                                    }
                                }
                                try {
                                    boolean isCellular2 = nai.networkCapabilities.hasTransport(0);
                                    boolean isConnected = nai.networkInfo.isConnected();
                                    if (WLAN_ASSIST_DBG) {
                                        RTT2 = RTT3;
                                        try {
                                            log("WLAN+ connction name:" + nai.name() + ", state: " + nai.networkInfo.getState() + ", Extra:" + extraStr);
                                        } catch (IOException e8) {
                                            token = token3;
                                            targetNai3 = targetNai2;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            e = e8;
                                            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                            this.mMeasureTime = this.mMeasureTime + 1;
                                            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                            }
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = z;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (NullPointerException e9) {
                                            e2 = e9;
                                            token = token3;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            loge("WLAN+ NullPointerException:" + e2.toString());
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (Exception e10) {
                                            e3 = e10;
                                            token = token3;
                                            targetNai3 = targetNai2;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            loge("WLAN+ Exception:" + e3.toString());
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            token = token3;
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            throw th;
                                        }
                                    } else {
                                        RTT2 = RTT3;
                                    }
                                    if (isImsApn || !isCellular2) {
                                        now2 = now3;
                                        token = token3;
                                        targetNai3 = targetNai2;
                                        celluarFound4 = celluarFound4;
                                        RTT3 = RTT2;
                                    } else if (!isConnected) {
                                        if (nai.getCurrentScore() != INVALID_SCORE) {
                                            handleUpdateNetworkScore(nai, INVALID_SCORE);
                                            sendBroadcastDataScore(INVALID_SCORE);
                                        }
                                        if (WLAN_ASSIST_DBG) {
                                            log("WLAN+ ignore measureDataState! network state:" + nai.networkInfo.getState());
                                        }
                                        Binder.restoreCallingIdentity(token3);
                                        this.mBlock = false;
                                        return true;
                                    } else {
                                        try {
                                            if (nai.getCurrentScore() == VALID_SCORE) {
                                                token = token3;
                                                try {
                                                    if (now3 - this.mLastMeasureTime <= this.REMEASURE_TIMER_FINE) {
                                                        shouldReMeasure = false;
                                                        if (shouldReMeasure) {
                                                            if (WLAN_ASSIST_DBG) {
                                                                log("WLAN+ ignore measureDataState! shouldReMeasure:" + shouldReMeasure);
                                                            }
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            return true;
                                                        }
                                                        try {
                                                            this.mBlock = true;
                                                            this.mLastMeasureTime = now3;
                                                            celluarFound2 = true;
                                                            try {
                                                                int RAT = TelephonyManager.getNetworkClass(nai.networkInfo.getSubtype()) + 1;
                                                                if (WLAN_ASSIST_DBG) {
                                                                    now2 = now3;
                                                                    try {
                                                                        sb = new StringBuilder();
                                                                        sb.append("WLAN+ mMeasureTime:");
                                                                        sb.append(this.mMeasureTime);
                                                                        sb.append(" it's TRANSPORT_CELLULAR, RAT:");
                                                                        sb.append(RAT);
                                                                        sb.append("G, siganlLevel:");
                                                                    } catch (IOException e11) {
                                                                        e = e11;
                                                                        celluarFound4 = true;
                                                                        targetNai3 = nai;
                                                                        RTT3 = RTT2;
                                                                        loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                                        this.mMeasureTime = this.mMeasureTime + 1;
                                                                        if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                                        }
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = z;
                                                                        if (celluarFound4) {
                                                                        }
                                                                        return celluarFound4;
                                                                    } catch (NullPointerException e12) {
                                                                        e2 = e12;
                                                                        celluarFound4 = true;
                                                                        RTT3 = RTT2;
                                                                        loge("WLAN+ NullPointerException:" + e2.toString());
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        if (celluarFound4) {
                                                                        }
                                                                        return celluarFound4;
                                                                    } catch (Exception e13) {
                                                                        e3 = e13;
                                                                        celluarFound4 = true;
                                                                        targetNai3 = nai;
                                                                        RTT3 = RTT2;
                                                                        loge("WLAN+ Exception:" + e3.toString());
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        if (celluarFound4) {
                                                                        }
                                                                        return celluarFound4;
                                                                    } catch (Throwable th6) {
                                                                        th = th6;
                                                                        th = th;
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        throw th;
                                                                    }
                                                                    try {
                                                                        sb.append(siganlLevel);
                                                                        log(sb.toString());
                                                                    } catch (IOException e14) {
                                                                        e = e14;
                                                                        celluarFound4 = true;
                                                                        targetNai3 = nai;
                                                                        RTT3 = RTT2;
                                                                    } catch (NullPointerException e15) {
                                                                        e2 = e15;
                                                                        celluarFound4 = true;
                                                                        RTT3 = RTT2;
                                                                        loge("WLAN+ NullPointerException:" + e2.toString());
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        if (celluarFound4) {
                                                                        }
                                                                        return celluarFound4;
                                                                    } catch (Exception e16) {
                                                                        e3 = e16;
                                                                        celluarFound4 = true;
                                                                        targetNai3 = nai;
                                                                        RTT3 = RTT2;
                                                                        loge("WLAN+ Exception:" + e3.toString());
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        if (celluarFound4) {
                                                                        }
                                                                        return celluarFound4;
                                                                    } catch (Throwable th7) {
                                                                        th = th7;
                                                                        th = th;
                                                                        Binder.restoreCallingIdentity(token);
                                                                        this.mBlock = false;
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    now2 = now3;
                                                                }
                                                            } catch (IOException e17) {
                                                                e = e17;
                                                                targetNai3 = nai;
                                                                celluarFound4 = true;
                                                                RTT3 = RTT2;
                                                                loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                                this.mMeasureTime = this.mMeasureTime + 1;
                                                                if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                                }
                                                                Binder.restoreCallingIdentity(token);
                                                                this.mBlock = z;
                                                                if (celluarFound4) {
                                                                }
                                                                return celluarFound4;
                                                            } catch (NullPointerException e18) {
                                                                e2 = e18;
                                                                celluarFound4 = true;
                                                                RTT3 = RTT2;
                                                                loge("WLAN+ NullPointerException:" + e2.toString());
                                                                Binder.restoreCallingIdentity(token);
                                                                this.mBlock = false;
                                                                if (celluarFound4) {
                                                                }
                                                                return celluarFound4;
                                                            } catch (Exception e19) {
                                                                e3 = e19;
                                                                targetNai3 = nai;
                                                                celluarFound4 = true;
                                                                RTT3 = RTT2;
                                                                loge("WLAN+ Exception:" + e3.toString());
                                                                Binder.restoreCallingIdentity(token);
                                                                this.mBlock = false;
                                                                if (celluarFound4) {
                                                                }
                                                                return celluarFound4;
                                                            } catch (Throwable th8) {
                                                                th = th8;
                                                                Binder.restoreCallingIdentity(token);
                                                                this.mBlock = false;
                                                                throw th;
                                                            }
                                                        } catch (IOException e20) {
                                                            e = e20;
                                                            targetNai3 = targetNai2;
                                                            celluarFound4 = celluarFound4;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                            this.mMeasureTime = this.mMeasureTime + 1;
                                                            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                            }
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = z;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (NullPointerException e21) {
                                                            e2 = e21;
                                                            celluarFound4 = celluarFound4;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ NullPointerException:" + e2.toString());
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (Exception e22) {
                                                            e3 = e22;
                                                            targetNai3 = targetNai2;
                                                            celluarFound4 = celluarFound4;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ Exception:" + e3.toString());
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (Throwable th9) {
                                                            th = th9;
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            throw th;
                                                        }
                                                        try {
                                                            int i = this.mMeasureTime;
                                                            while (true) {
                                                                if (i >= this.TOTAL_MEASURE_TIME) {
                                                                    celluarFound3 = celluarFound2;
                                                                    RTT3 = RTT2;
                                                                    break;
                                                                }
                                                                long requestTimestamp = DISABLE_DATA_ALERT_BYTES;
                                                                SocketFactory socketfactory2 = nai.network.getSocketFactory();
                                                                if (!isChineseOperator()) {
                                                                    isImsApn2 = isImsApn;
                                                                    isCellular = isCellular2;
                                                                    hostAddresses = nai.network.getAllByName(this.EXTERNAL_SERVERS[i]);
                                                                    if (hostAddresses.length <= 0) {
                                                                        throw new UnknownHostException(this.EXTERNAL_SERVERS[i]);
                                                                    }
                                                                } else {
                                                                    isImsApn2 = isImsApn;
                                                                    isCellular = isCellular2;
                                                                    hostAddresses = nai.network.getAllByName(this.INTERNAL_SERVERS[i]);
                                                                    if (hostAddresses.length <= 0) {
                                                                        throw new UnknownHostException(this.INTERNAL_SERVERS[i]);
                                                                    }
                                                                }
                                                                int j = 0;
                                                                while (true) {
                                                                    if (j >= hostAddresses.length) {
                                                                        shouldReMeasure2 = shouldReMeasure;
                                                                        celluarFound3 = celluarFound2;
                                                                        break;
                                                                    }
                                                                    Socket socket = null;
                                                                    try {
                                                                        socket = socketfactory2.createSocket();
                                                                        requestTimestamp = SystemClock.elapsedRealtime();
                                                                        socketfactory = socketfactory2;
                                                                        try {
                                                                            shouldReMeasure2 = shouldReMeasure;
                                                                            try {
                                                                                celluarFound3 = celluarFound2;
                                                                            } catch (IOException e23) {
                                                                                celluarFound3 = celluarFound2;
                                                                                e4 = e23;
                                                                                try {
                                                                                    if (j == hostAddresses.length - 1) {
                                                                                    }
                                                                                } catch (Throwable th10) {
                                                                                    th2 = th10;
                                                                                    socket.close();
                                                                                    throw th2;
                                                                                }
                                                                            } catch (Throwable th11) {
                                                                                th2 = th11;
                                                                                socket.close();
                                                                                throw th2;
                                                                            }
                                                                        } catch (IOException e24) {
                                                                            shouldReMeasure2 = shouldReMeasure;
                                                                            celluarFound3 = celluarFound2;
                                                                            e4 = e24;
                                                                            if (j == hostAddresses.length - 1) {
                                                                            }
                                                                        } catch (Throwable th12) {
                                                                            th2 = th12;
                                                                            socket.close();
                                                                            throw th2;
                                                                        }
                                                                        try {
                                                                            socket.connect(new InetSocketAddress(hostAddresses[j], 80), this.RESPONSE_TIMEOUT);
                                                                            break;
                                                                        } catch (IOException e25) {
                                                                            e4 = e25;
                                                                            if (j == hostAddresses.length - 1) {
                                                                            }
                                                                        }
                                                                    } catch (IOException e26) {
                                                                        socketfactory = socketfactory2;
                                                                        shouldReMeasure2 = shouldReMeasure;
                                                                        celluarFound3 = celluarFound2;
                                                                        e4 = e26;
                                                                        if (j == hostAddresses.length - 1) {
                                                                            if (socket != null && !socket.isClosed()) {
                                                                                socket.close();
                                                                            }
                                                                            j++;
                                                                            socketfactory2 = socketfactory;
                                                                            celluarFound2 = celluarFound3;
                                                                            isConnected = isConnected;
                                                                            shouldReMeasure = shouldReMeasure2;
                                                                        } else {
                                                                            throw e4;
                                                                        }
                                                                    } catch (Throwable th13) {
                                                                        th2 = th13;
                                                                        if (socket != null && !socket.isClosed()) {
                                                                            socket.close();
                                                                        }
                                                                        throw th2;
                                                                    }
                                                                    j++;
                                                                    socketfactory2 = socketfactory;
                                                                    celluarFound2 = celluarFound3;
                                                                    isConnected = isConnected;
                                                                    shouldReMeasure = shouldReMeasure2;
                                                                }
                                                                long singleRtt = SystemClock.elapsedRealtime() - requestTimestamp;
                                                                if (!isChineseOperator()) {
                                                                    if (WLAN_ASSIST_DBG) {
                                                                        log("WLAN+ " + this.EXTERNAL_SERVERS[i] + " response time:" + singleRtt);
                                                                    }
                                                                } else if (WLAN_ASSIST_DBG) {
                                                                    log("WLAN+ " + this.INTERNAL_SERVERS[i] + " response time:" + singleRtt);
                                                                }
                                                                if (singleRtt < ((long) this.RESPONSE_TIMEOUT)) {
                                                                    RTT3 = ((long) (this.TOTAL_MEASURE_TIME - this.mMeasureTime)) * singleRtt;
                                                                    break;
                                                                }
                                                                RTT2 += singleRtt;
                                                                i++;
                                                                celluarFound2 = celluarFound3;
                                                                isImsApn = isImsApn2;
                                                                isCellular2 = isCellular;
                                                                isConnected = isConnected;
                                                                shouldReMeasure = shouldReMeasure2;
                                                            }
                                                            if (RTT3 > DISABLE_DATA_ALERT_BYTES) {
                                                                try {
                                                                    if (RTT3 < ((long) ((this.TOTAL_MEASURE_TIME - this.mMeasureTime) * this.RESPONSE_TIMEOUT))) {
                                                                        if (nai.getCurrentScore() != VALID_SCORE) {
                                                                            handleUpdateNetworkScore(nai, VALID_SCORE);
                                                                            sendBroadcastDataScore(VALID_SCORE);
                                                                        }
                                                                        this.mMeasureTime = 0;
                                                                        targetNai3 = nai;
                                                                        celluarFound4 = celluarFound3;
                                                                    }
                                                                } catch (IOException e27) {
                                                                    e = e27;
                                                                    targetNai3 = nai;
                                                                    celluarFound4 = celluarFound3;
                                                                    loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                                    this.mMeasureTime = this.mMeasureTime + 1;
                                                                    if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                                    }
                                                                    Binder.restoreCallingIdentity(token);
                                                                    this.mBlock = z;
                                                                    if (celluarFound4) {
                                                                    }
                                                                    return celluarFound4;
                                                                } catch (NullPointerException e28) {
                                                                    e2 = e28;
                                                                    celluarFound4 = celluarFound3;
                                                                    loge("WLAN+ NullPointerException:" + e2.toString());
                                                                    Binder.restoreCallingIdentity(token);
                                                                    this.mBlock = false;
                                                                    if (celluarFound4) {
                                                                    }
                                                                    return celluarFound4;
                                                                } catch (Exception e29) {
                                                                    e3 = e29;
                                                                    targetNai3 = nai;
                                                                    celluarFound4 = celluarFound3;
                                                                    loge("WLAN+ Exception:" + e3.toString());
                                                                    Binder.restoreCallingIdentity(token);
                                                                    this.mBlock = false;
                                                                    if (celluarFound4) {
                                                                    }
                                                                    return celluarFound4;
                                                                } catch (Throwable th14) {
                                                                    th = th14;
                                                                    Binder.restoreCallingIdentity(token);
                                                                    this.mBlock = false;
                                                                    throw th;
                                                                }
                                                            }
                                                            if (nai.getCurrentScore() != INVALID_SCORE) {
                                                                handleUpdateNetworkScore(nai, INVALID_SCORE);
                                                                sendBroadcastDataScore(INVALID_SCORE);
                                                            }
                                                            this.mMeasureTime = 0;
                                                            targetNai3 = nai;
                                                            celluarFound4 = celluarFound3;
                                                        } catch (IOException e30) {
                                                            e = e30;
                                                            targetNai3 = nai;
                                                            celluarFound4 = true;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                            this.mMeasureTime = this.mMeasureTime + 1;
                                                            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                            }
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = z;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (NullPointerException e31) {
                                                            e2 = e31;
                                                            celluarFound4 = true;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ NullPointerException:" + e2.toString());
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (Exception e32) {
                                                            e3 = e32;
                                                            targetNai3 = nai;
                                                            celluarFound4 = true;
                                                            RTT3 = RTT2;
                                                            loge("WLAN+ Exception:" + e3.toString());
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            if (celluarFound4) {
                                                            }
                                                            return celluarFound4;
                                                        } catch (Throwable th15) {
                                                            th = th15;
                                                            Binder.restoreCallingIdentity(token);
                                                            this.mBlock = false;
                                                            throw th;
                                                        }
                                                    }
                                                } catch (IOException e33) {
                                                    targetNai3 = targetNai2;
                                                    celluarFound4 = celluarFound4;
                                                    RTT3 = RTT2;
                                                    e = e33;
                                                    loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                                    this.mMeasureTime = this.mMeasureTime + 1;
                                                    if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                                    }
                                                    Binder.restoreCallingIdentity(token);
                                                    this.mBlock = z;
                                                    if (celluarFound4) {
                                                    }
                                                    return celluarFound4;
                                                } catch (NullPointerException e34) {
                                                    e2 = e34;
                                                    celluarFound4 = celluarFound4;
                                                    RTT3 = RTT2;
                                                    loge("WLAN+ NullPointerException:" + e2.toString());
                                                    Binder.restoreCallingIdentity(token);
                                                    this.mBlock = false;
                                                    if (celluarFound4) {
                                                    }
                                                    return celluarFound4;
                                                } catch (Exception e35) {
                                                    e3 = e35;
                                                    targetNai3 = targetNai2;
                                                    celluarFound4 = celluarFound4;
                                                    RTT3 = RTT2;
                                                    loge("WLAN+ Exception:" + e3.toString());
                                                    Binder.restoreCallingIdentity(token);
                                                    this.mBlock = false;
                                                    if (celluarFound4) {
                                                    }
                                                    return celluarFound4;
                                                } catch (Throwable th16) {
                                                    th = th16;
                                                    Binder.restoreCallingIdentity(token);
                                                    this.mBlock = false;
                                                    throw th;
                                                }
                                            } else {
                                                token = token3;
                                            }
                                            shouldReMeasure = true;
                                            if (shouldReMeasure) {
                                            }
                                        } catch (IOException e36) {
                                            token = token3;
                                            e = e36;
                                            targetNai3 = targetNai2;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                            this.mMeasureTime = this.mMeasureTime + 1;
                                            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                            }
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = z;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (NullPointerException e37) {
                                            token = token3;
                                            e2 = e37;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            loge("WLAN+ NullPointerException:" + e2.toString());
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (Exception e38) {
                                            token = token3;
                                            e3 = e38;
                                            targetNai3 = targetNai2;
                                            celluarFound4 = celluarFound4;
                                            RTT3 = RTT2;
                                            loge("WLAN+ Exception:" + e3.toString());
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            if (celluarFound4) {
                                            }
                                            return celluarFound4;
                                        } catch (Throwable th17) {
                                            token = token3;
                                            th = th17;
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            throw th;
                                        }
                                    }
                                    tempNais3 = tempNais2;
                                    token3 = token;
                                    now3 = now2;
                                } catch (IOException e39) {
                                    token = token3;
                                    e = e39;
                                    targetNai3 = targetNai2;
                                    celluarFound4 = celluarFound4;
                                    loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                                    this.mMeasureTime = this.mMeasureTime + 1;
                                    if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                                        this.mMeasureTime = 0;
                                        if (targetNai3 != null) {
                                            try {
                                                if (targetNai3.getCurrentScore() != INVALID_SCORE) {
                                                    handleUpdateNetworkScore(targetNai3, INVALID_SCORE);
                                                    sendBroadcastDataScore(INVALID_SCORE);
                                                }
                                            } catch (NullPointerException ne) {
                                                loge("WLAN+ NullPointerException:" + ne.toString());
                                            } catch (Exception ee) {
                                                loge("WLAN+ Exception:" + ee.toString());
                                            }
                                        }
                                        z = false;
                                    } else {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ie) {
                                            loge("WLAN+ InterruptedException:" + ie.toString());
                                        }
                                        this.mBlock = false;
                                        this.mLastMeasureTime = DISABLE_DATA_ALERT_BYTES;
                                        if (!measureDataState(siganlLevel)) {
                                            Binder.restoreCallingIdentity(token);
                                            this.mBlock = false;
                                            return false;
                                        }
                                        z = false;
                                    }
                                    Binder.restoreCallingIdentity(token);
                                    this.mBlock = z;
                                    if (celluarFound4) {
                                    }
                                    return celluarFound4;
                                } catch (NullPointerException e40) {
                                    token = token3;
                                    e2 = e40;
                                    celluarFound4 = celluarFound4;
                                    loge("WLAN+ NullPointerException:" + e2.toString());
                                    Binder.restoreCallingIdentity(token);
                                    this.mBlock = false;
                                    if (celluarFound4) {
                                    }
                                    return celluarFound4;
                                } catch (Exception e41) {
                                    token = token3;
                                    e3 = e41;
                                    targetNai3 = targetNai2;
                                    celluarFound4 = celluarFound4;
                                    loge("WLAN+ Exception:" + e3.toString());
                                    Binder.restoreCallingIdentity(token);
                                    this.mBlock = false;
                                    if (celluarFound4) {
                                    }
                                    return celluarFound4;
                                } catch (Throwable th18) {
                                    token = token3;
                                    th = th18;
                                    Binder.restoreCallingIdentity(token);
                                    this.mBlock = false;
                                    throw th;
                                }
                            }
                            targetNai3 = targetNai;
                            tempNais3 = tempNais;
                            celluarFound4 = celluarFound;
                            RTT3 = RTT;
                            token3 = token2;
                            now3 = now;
                        } catch (IOException e42) {
                            token = token3;
                            e = e42;
                            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                            this.mMeasureTime = this.mMeasureTime + 1;
                            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                            }
                            Binder.restoreCallingIdentity(token);
                            this.mBlock = z;
                            if (celluarFound4) {
                            }
                            return celluarFound4;
                        } catch (NullPointerException e43) {
                            token = token3;
                            e2 = e43;
                            loge("WLAN+ NullPointerException:" + e2.toString());
                            Binder.restoreCallingIdentity(token);
                            this.mBlock = false;
                            if (celluarFound4) {
                            }
                            return celluarFound4;
                        } catch (Exception e44) {
                            token = token3;
                            e3 = e44;
                            loge("WLAN+ Exception:" + e3.toString());
                            Binder.restoreCallingIdentity(token);
                            this.mBlock = false;
                            if (celluarFound4) {
                            }
                            return celluarFound4;
                        } catch (Throwable th19) {
                            token = token3;
                            th = th19;
                            Binder.restoreCallingIdentity(token);
                            this.mBlock = false;
                            throw th;
                        }
                    }
                    Binder.restoreCallingIdentity(token3);
                    this.mBlock = false;
                } catch (IOException e45) {
                    token = token3;
                    e = e45;
                    loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e.toString());
                    this.mMeasureTime = this.mMeasureTime + 1;
                    if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                    }
                    Binder.restoreCallingIdentity(token);
                    this.mBlock = z;
                    if (celluarFound4) {
                    }
                    return celluarFound4;
                } catch (NullPointerException e46) {
                    token = token3;
                    e2 = e46;
                    loge("WLAN+ NullPointerException:" + e2.toString());
                    Binder.restoreCallingIdentity(token);
                    this.mBlock = false;
                    if (celluarFound4) {
                    }
                    return celluarFound4;
                } catch (Exception e47) {
                    token = token3;
                    e3 = e47;
                    loge("WLAN+ Exception:" + e3.toString());
                    Binder.restoreCallingIdentity(token);
                    this.mBlock = false;
                    if (celluarFound4) {
                    }
                    return celluarFound4;
                } catch (Throwable th20) {
                    token = token3;
                    th = th20;
                    Binder.restoreCallingIdentity(token);
                    this.mBlock = false;
                    throw th;
                }
                if (celluarFound4) {
                    this.mLastMeasureTime = DISABLE_DATA_ALERT_BYTES;
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ warning ! celluar not Found !");
                    }
                } else if (WLAN_ASSIST_DBG) {
                    log("WLAN+ mMeasureTime:" + this.mMeasureTime + " RTT: " + RTT3);
                }
                return celluarFound4;
            }
        }
        if (!WLAN_ASSIST_DBG) {
            return true;
        }
        log("WLAN+ ignore measureDataState!");
        return true;
    }

    public boolean shouldKeepCelluarNetwork(boolean keep) {
        this.mShouldKeepCelluarNetwork = keep;
        return true;
    }

    public void updateDataNetworkConfig(String name, String value) {
        if (value != null && name != null) {
            char c = 65535;
            switch (name.hashCode()) {
                case -1718522432:
                    if (name.equals("ResponseTimeout")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1343557836:
                    if (name.equals("RemeasureTimerFine")) {
                        c = 5;
                        break;
                    }
                    break;
                case -466500018:
                    if (name.equals("ReconnectTimer")) {
                        c = 3;
                        break;
                    }
                    break;
                case -395859065:
                    if (name.equals("TotalMeasureTime")) {
                        c = 1;
                        break;
                    }
                    break;
                case 930908588:
                    if (name.equals("ExternalServers1")) {
                        c = 9;
                        break;
                    }
                    break;
                case 930908589:
                    if (name.equals("ExternalServers2")) {
                        c = 10;
                        break;
                    }
                    break;
                case 930908590:
                    if (name.equals("ExternalServers3")) {
                        c = 11;
                        break;
                    }
                    break;
                case 993122974:
                    if (name.equals("InternalServers1")) {
                        c = 6;
                        break;
                    }
                    break;
                case 993122975:
                    if (name.equals("InternalServers2")) {
                        c = 7;
                        break;
                    }
                    break;
                case 993122976:
                    if (name.equals("InternalServers3")) {
                        c = 8;
                        break;
                    }
                    break;
                case 1362874969:
                    if (name.equals("EnableWlanAssistant")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1539719770:
                    if (name.equals("RemeasureTimer")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case PROVISIONING_NOTIFICATION_HIDE /*{ENCODED_INT: 0}*/:
                    try {
                        this.ENABLE_WLAN_ASSISTANT = Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e);
                        return;
                    }
                case 1:
                    try {
                        this.TOTAL_MEASURE_TIME = Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e2) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e2);
                        return;
                    }
                case DebugInfo.Architecture.IS_32BIT:
                    try {
                        this.RESPONSE_TIMEOUT = Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e3) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e3);
                        return;
                    }
                case 3:
                    try {
                        this.RECONNECT_TIMER = (long) Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e4) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e4);
                        return;
                    }
                case 4:
                    try {
                        this.REMEASURE_TIMER = (long) Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e5) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e5);
                        return;
                    }
                case MAX_CELLULAR_INTERFACE /*{ENCODED_INT: 5}*/:
                    try {
                        this.REMEASURE_TIMER_FINE = (long) Integer.parseInt(value);
                        return;
                    } catch (NumberFormatException e6) {
                        loge("WLAN+ Numberex DataNetworkInvalid e:" + e6);
                        return;
                    }
                case 6:
                    this.INTERNAL_SERVERS[0] = value;
                    return;
                case 7:
                    this.INTERNAL_SERVERS[1] = value;
                    return;
                case EVENT_CLEAR_NET_TRANSITION_WAKELOCK /*{ENCODED_INT: 8}*/:
                    this.INTERNAL_SERVERS[2] = value;
                    return;
                case EVENT_APPLY_GLOBAL_HTTP_PROXY /*{ENCODED_INT: 9}*/:
                    this.EXTERNAL_SERVERS[0] = value;
                    return;
                case INVALID_SCORE /*{ENCODED_INT: 10}*/:
                    this.EXTERNAL_SERVERS[1] = value;
                    return;
                case 11:
                    this.EXTERNAL_SERVERS[2] = value;
                    return;
                default:
                    return;
            }
        }
    }

    private void sendBroadcastDataScore(int score) {
        Intent dataIntent = new Intent(DATA_SCORE_CHANGE);
        dataIntent.putExtra(EXTRA_DATA_CORE, score);
        this.mContext.sendStickyBroadcastAsUser(dataIntent, UserHandle.ALL);
    }

    public boolean hasCache() {
        Context context = this.mContext;
        if (context != null && this.mTimeOem == null) {
            this.mTimeOem = NtpTrustedTime.getInstance(context);
        }
        NtpTrustedTime ntpTrustedTime = this.mTimeOem;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.hasCache();
        }
        return false;
    }

    public long getCacheAge() {
        Context context = this.mContext;
        if (context != null && this.mTimeOem == null) {
            this.mTimeOem = NtpTrustedTime.getInstance(context);
        }
        NtpTrustedTime ntpTrustedTime = this.mTimeOem;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.getCacheAge();
        }
        return Long.MAX_VALUE;
    }

    public long getCurrentTimeMillis() {
        Context context = this.mContext;
        if (context != null && this.mTimeOem == null) {
            this.mTimeOem = NtpTrustedTime.getInstance(context);
        }
        NtpTrustedTime ntpTrustedTime = this.mTimeOem;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.currentTimeMillis();
        }
        return Long.MAX_VALUE;
    }

    public void sendNetworkConnectedMsg(Network network) {
        this.mHandler.removeMessages(EVENT_NOTIFY_NETWORK_CONNECTED, network);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_NOTIFY_NETWORK_CONNECTED, network));
    }

    public boolean oppoExecuteIPtableCmd(String cmd, String dstPort) {
        String str = TAG;
        Log.d(str, "executeIPtableCmd " + cmd, new Throwable());
        try {
            this.mNetd.oppoExecuteIPtableCmd(cmd, dstPort);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            loge("Can't set TCP buffer sizes:" + e);
            return true;
        }
    }

    public List<String> readArpFile() {
        List<String> arpFile = new ArrayList<>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String packagename = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        loge("readArpFile start:" + packagename);
        if (!OPPO_WIFISECURE_DETECT_PACKAGE_NAME.equals(packagename)) {
            String str = TAG;
            Log.d(str, "readArpFile:" + packagename + " not allow");
            return null;
        }
        try {
            FileReader fileReader2 = new FileReader("/proc/net/arp");
            BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
            while (true) {
                String line = bufferedReader2.readLine();
                if (line != null) {
                    arpFile.add(line);
                } else {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            bufferedReader2.close();
            try {
                fileReader2.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (IOException e5) {
            e5.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e6) {
                    e6.printStackTrace();
                }
            }
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (Throwable th) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (Exception e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
        loge("readArpFile:" + arpFile);
        return arpFile;
    }
}
