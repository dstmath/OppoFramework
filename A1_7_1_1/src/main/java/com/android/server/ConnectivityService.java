package com.android.server;

import android.app.BroadcastOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.LinkProperties.CompareResult;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkMisc;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.NetworkRequest.Type;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.DefaultNetworkEvent;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.util.AvoidBadWifiTracker;
import android.net.wifi.WifiDevice;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.NtpTrustedTime;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.WakeupMessage;
import com.android.internal.util.XmlUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.connectivity.DataConnectionStats;
import com.android.server.connectivity.KeepaliveTracker;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.connectivity.Nat464Xlat;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkDiagnostics;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.NetworkNotificationManager;
import com.android.server.connectivity.NetworkNotificationManager.NotificationType;
import com.android.server.connectivity.PacManager;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.net.NetworkHttpMonitor;
import com.android.server.oppo.IElsaManager;
import com.android.server.policy.PhoneWindowManager;
import com.google.android.collect.Lists;
import com.mediatek.common.MPlugin;
import com.mediatek.common.net.IConnectivityServiceExt;
import com.oppo.oiface.OifaceUtil;
import com.oppo.oiface.OifaceUtil.NetType;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.SocketFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ConnectivityService extends Stub implements OnFinished {
    /* renamed from: -com-android-server-ConnectivityService$UnneededForSwitchesValues */
    private static final /* synthetic */ int[] f2x78a82206 = null;
    /* renamed from: -com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues */
    private static final /* synthetic */ int[] f3x6e7dafb6 = null;
    private static final String ATTR_MCC = "mcc";
    private static final String ATTR_MNC = "mnc";
    private static final String DATA_SCORE_CHANGE = "android.net.wifi.OPPO_DATA_NET_CHANGE";
    private static final boolean DBG = true;
    private static final int DEFAULT_LINGER_DELAY_MS = 3000;
    private static final String DEFAULT_TCP_BUFFER_SIZES = "4096,87380,110208,4096,16384,110208";
    private static final String DEFAULT_TCP_RWND_KEY = "net.tcp.default_init_rwnd";
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
    private static final int EVENT_APPLY_GLOBAL_HTTP_PROXY = 9;
    private static final int EVENT_CHANGE_MOBILE_DATA_ENABLED = 2;
    private static final int EVENT_CLEAR_NET_TRANSITION_WAKELOCK = 8;
    private static final int EVENT_CONFIGURE_MOBILE_DATA_ALWAYS_ON = 30;
    private static final int EVENT_ENABLE_MOBILE_DATA_FOR_TETHERING = 100;
    private static final int EVENT_EXPIRE_NET_TRANSITION_WAKELOCK = 24;
    private static final int EVENT_PROMPT_UNVALIDATED = 29;
    private static final int EVENT_PROXY_HAS_CHANGED = 16;
    private static final int EVENT_REGISTER_NETWORK_AGENT = 18;
    private static final int EVENT_REGISTER_NETWORK_FACTORY = 17;
    private static final int EVENT_REGISTER_NETWORK_LISTENER = 21;
    private static final int EVENT_REGISTER_NETWORK_LISTENER_WITH_INTENT = 31;
    private static final int EVENT_REGISTER_NETWORK_REQUEST = 19;
    private static final int EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT = 26;
    private static final int EVENT_RELEASE_NETWORK_REQUEST = 22;
    private static final int EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT = 27;
    private static final int EVENT_REQUEST_LINKPROPERTIES = 32;
    private static final int EVENT_REQUEST_NETCAPABILITIES = 33;
    private static final int EVENT_SET_ACCEPT_UNVALIDATED = 28;
    private static final int EVENT_SET_AVOID_UNVALIDATED = 35;
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final String EXTRA_DATA_CORE = "data_score";
    private static final String EXTRA_WIFI_TO_DATA = "wifi_to_data";
    private static final String EXTRA_WIFI_VALID = "wifi_valid";
    private static final int INET_CONDITION_LOG_MAX_SIZE = 15;
    private static final int INVALID_SCORE = 10;
    private static final boolean IS_LAB_TEST = false;
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final boolean LOGD_BLOCKED_NETWORKINFO = false;
    private static final boolean LOGD_RULES = false;
    private static final int MAX_NETWORK_INFO_LOGS = 100;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 65535;
    private static final int MAX_VALIDATION_LOGS = 10;
    private static final int MIN_NET_ID = 100;
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    private static final boolean SDBG = false;
    private static final String TAG = null;
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    private static final int VALID_SCORE = 50;
    private static final int VALID_SCORE_THRESHOLD = 20;
    private static final boolean VDBG = false;
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final String WIFI_NETWORK_CHANGE = "android.net.wifi.WIFI_TO_DATA";
    private static final boolean WLAN_ASSIST_DBG = false;
    private static boolean mAlreadyUpdated;
    private static NetworkHttpMonitor mNetworkHttpMonitor;
    private static double mPowerLost;
    private static String mPowerState;
    private static boolean sIsAutoTethering;
    private static final SparseArray<String> sMagicDecoderRing = null;
    private static ConnectivityService sServiceInstance;
    private int ENABLE_WLAN_ASSISTANT;
    private long RECONNECT_TIMER;
    private long REMEASURE_TIMER;
    private long REMEASURE_TIMER_FINE;
    private int RESPONSE_TIMEOUT;
    private String[] SERVERS;
    private int TOTAL_MEASURE_TIME;
    private boolean hasWifiAssistant;
    final AvoidBadWifiTracker mAvoidBadWifiTracker;
    private boolean mBlock;
    @GuardedBy("mBlockedAppUids")
    private final HashSet<Integer> mBlockedAppUids;
    private final Context mContext;
    private String mCurrentTcpBufferSizes;
    private INetworkManagementEventObserver mDataActivityObserver;
    private DataConnectionStats mDataConnectionStats;
    private InetAddress mDefaultDns;
    private int mDefaultInetConditionPublished;
    private final NetworkRequest mDefaultMobileDataRequest;
    private volatile ProxyInfo mDefaultProxy;
    private boolean mDefaultProxyDisabled;
    private final NetworkRequest mDefaultRequest;
    private ProxyInfo mGlobalProxy;
    private final InternalHandler mHandler;
    protected final HandlerThread mHandlerThread;
    IConnectivityServiceExt mIcsExt;
    private ArrayList mInetLog;
    private Intent mInitialBroadcast;
    private KeepaliveTracker mKeepaliveTracker;
    private KeyStore mKeyStore;
    private long mLastMeasureTime;
    private long mLastRequestNetworkTime;
    private LegacyTypeTracker mLegacyTypeTracker;
    protected int mLingerDelayMs;
    private LingerMonitor mLingerMonitor;
    private boolean mLockdownEnabled;
    private LockdownVpnTracker mLockdownTracker;
    private int mMeasureTime;
    @GuardedBy("mRulesLock")
    private ArraySet<String> mMeteredIfaces;
    private final IpConnectivityLog mMetricsLog;
    NetworkConfig[] mNetConfigs;
    @GuardedBy("mNetworkForNetId")
    private final SparseBooleanArray mNetIdInUse;
    private WakeLock mNetTransitionWakeLock;
    private String mNetTransitionWakeLockCausedBy;
    private int mNetTransitionWakeLockSerialNumber;
    private int mNetTransitionWakeLockTimeout;
    private INetworkManagementService mNetd;
    private final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    private final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy("mNetworkForNetId")
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    private final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private final LocalLog mNetworkInfoBlockingLogs;
    private int mNetworkPreference;
    private final LocalLog mNetworkRequestInfoLogs;
    private final HashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    private NetworkNotificationManager mNotifier;
    private int mNumDnsEntries;
    private PacManager mPacManager;
    private final WakeLock mPendingIntentWakeLock;
    private final PermissionMonitor mPermissionMonitor;
    private INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    List mProtectedNetworks;
    private final File mProvisioningUrlFile;
    private Object mProxyLock;
    private BroadcastReceiver mReceiver;
    private final int mReleasePendingIntentDelayMs;
    private Object mRequestLock;
    @GuardedBy("mRulesLock")
    private boolean mRestrictBackground;
    private Object mRulesLock;
    private final SettingsObserver mSettingsObserver;
    private boolean mShouldKeepCelluarNetwork;
    private INetworkStatsService mStatsService;
    private Object mSynchronizedObject;
    private boolean mSystemReady;
    TelephonyManager mTelephonyManager;
    private boolean mTestMode;
    private Tethering mTethering;
    private NtpTrustedTime mTimeOem;
    private final NetworkStateTrackerHandler mTrackerHandler;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidRules;
    @GuardedBy("mUidToNetworkRequestCount")
    private final SparseIntArray mUidToNetworkRequestCount;
    private BroadcastReceiver mUserIntentReceiver;
    private UserManager mUserManager;
    private final ArrayDeque<ValidationLog> mValidationLogs;
    @GuardedBy("mVpns")
    private final SparseArray<Vpn> mVpns;

    final /* synthetic */ class -void__init__android_content_Context_context_android_os_INetworkManagementService_netManager_android_net_INetworkStatsService_statsService_android_net_INetworkPolicyManager_policyManager_android_net_metrics_IpConnectivityLog_logger_LambdaImpl0 implements Runnable {
        public void run() {
            ConnectivityService.this.-com_android_server_ConnectivityService_lambda$1();
        }
    }

    private class ConnectivityServiceReceiver extends BroadcastReceiver {
        /* synthetic */ ConnectivityServiceReceiver(ConnectivityService this$0, ConnectivityServiceReceiver connectivityServiceReceiver) {
            this();
        }

        private ConnectivityServiceReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Slog.d(ConnectivityService.TAG, "received intent ==> " + action);
                synchronized (ConnectivityService.this.mSynchronizedObject) {
                    if ("android.intent.action.TETHERING_CHANGED".equals(action)) {
                        ConnectivityService.this.setUsbTethering(intent.getBooleanExtra("tethering_isconnected", false));
                    } else if (("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.ACTION_BOOT_IPO".equals(action)) && ConnectivityService.sIsAutoTethering) {
                        ConnectivityService.this.setUsbTethering(true);
                        ConnectivityService.this.mHandler.sendMessageDelayed(ConnectivityService.this.mHandler.obtainMessage(100), 3000);
                    }
                }
            }
        }
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:12:0x0037, code:
            if (com.android.server.ConnectivityService.-get2() == false) goto L_?;
     */
        /* JADX WARNING: Missing block: B:14:0x003d, code:
            if (r12.what != 24) goto L_0x004b;
     */
        /* JADX WARNING: Missing block: B:15:0x003f, code:
            com.android.server.ConnectivityService.-wrap24("Failed to find a new network - expiring NetTransition Wakelock");
     */
        /* JADX WARNING: Missing block: B:20:0x004b, code:
            r5 = new java.lang.StringBuilder().append("NetTransition Wakelock (");
     */
        /* JADX WARNING: Missing block: B:21:0x0057, code:
            if (r0 != null) goto L_0x005c;
     */
        /* JADX WARNING: Missing block: B:22:0x0059, code:
            r0 = "unknown";
     */
        /* JADX WARNING: Missing block: B:23:0x005c, code:
            com.android.server.ConnectivityService.-wrap24(r5.append(r0).append(" cleared because we found a replacement network").toString());
     */
        /* JADX WARNING: Missing block: B:55:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:56:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:57:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:76:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 8:
                case 24:
                    synchronized (ConnectivityService.this) {
                        if (msg.arg1 == ConnectivityService.this.mNetTransitionWakeLockSerialNumber && ConnectivityService.this.mNetTransitionWakeLock.isHeld()) {
                            ConnectivityService.this.mNetTransitionWakeLock.release();
                            String causedBy = ConnectivityService.this.mNetTransitionWakeLockCausedBy;
                            break;
                        }
                    }
                    break;
                case 9:
                    ConnectivityService.this.handleDeprecatedGlobalHttpProxy();
                    return;
                case 16:
                    ConnectivityService.this.handleApplyDefaultProxy((ProxyInfo) msg.obj);
                    return;
                case 17:
                    ConnectivityService.this.handleRegisterNetworkFactory((NetworkFactoryInfo) msg.obj);
                    return;
                case 18:
                    ConnectivityService.this.handleRegisterNetworkAgent((NetworkAgentInfo) msg.obj);
                    return;
                case 19:
                case 21:
                    ConnectivityService.this.handleRegisterNetworkRequest((NetworkRequestInfo) msg.obj);
                    return;
                case 22:
                    ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1);
                    return;
                case 23:
                    ConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                    return;
                case 25:
                    for (NetworkAgentInfo nai : ConnectivityService.this.mNetworkAgentInfos.values()) {
                        nai.networkMonitor.systemReady = true;
                    }
                    return;
                case 26:
                case 31:
                    ConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                    return;
                case 27:
                    ConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                    return;
                case 28:
                    ConnectivityService connectivityService = ConnectivityService.this;
                    Network network = (Network) msg.obj;
                    boolean z2 = msg.arg1 != 0;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    connectivityService.handleSetAcceptUnvalidated(network, z2, z);
                    return;
                case 29:
                    ConnectivityService.this.handlePromptUnvalidated((Network) msg.obj);
                    return;
                case 30:
                    ConnectivityService.this.handleMobileDataAlwaysOn();
                    return;
                case 32:
                    ConnectivityService.this.handleRequestLinkProperties((NetworkRequest) msg.obj, msg.arg1);
                    return;
                case 33:
                    ConnectivityService.this.handleRequestNetworkCapabilities((NetworkRequest) msg.obj, msg.arg1);
                    return;
                case 35:
                    ConnectivityService.this.handleSetAvoidUnvalidated((Network) msg.obj);
                    return;
                case 100:
                    ConnectivityService.this.handleRegisterNetworkRequest(new NetworkRequestInfo(null, ConnectivityService.this.mDefaultRequest, new Binder()));
                    return;
                case 528395:
                    ConnectivityService.this.mKeepaliveTracker.handleStartKeepalive(msg);
                    return;
                case 528396:
                    ConnectivityService.this.mKeepaliveTracker.handleStopKeepalive(ConnectivityService.this.getNetworkAgentInfoForNetwork((Network) msg.obj), msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }
    }

    private class LegacyTypeTracker {
        private static final boolean DBG = true;
        private final ArrayList<NetworkAgentInfo>[] mTypeLists = new ArrayList[50];

        public void addSupportedType(int type) {
            if (this.mTypeLists[type] != null) {
                throw new IllegalStateException("legacy list for type " + type + "already initialized");
            }
            this.mTypeLists[type] = new ArrayList();
        }

        public boolean isTypeSupported(int type) {
            return ConnectivityManager.isNetworkTypeValid(type) && this.mTypeLists[type] != null;
        }

        /* JADX WARNING: Missing block: B:9:0x0015, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public NetworkAgentInfo getNetworkForType(int type) {
            synchronized (this.mTypeLists) {
                if (!isTypeSupported(type) || this.mTypeLists[type].isEmpty()) {
                } else {
                    NetworkAgentInfo networkAgentInfo = (NetworkAgentInfo) this.mTypeLists[type].get(0);
                    return networkAgentInfo;
                }
            }
        }

        private void maybeLogBroadcast(NetworkAgentInfo nai, DetailedState state, int type, boolean isDefaultNetwork) {
            ConnectivityService.log("Sending " + state + " broadcast for type " + type + " " + nai.name() + " isDefaultNetwork=" + isDefaultNetwork);
        }

        public void add(int type, NetworkAgentInfo nai) {
            if (isTypeSupported(type)) {
                if (ConnectivityService.VDBG) {
                    ConnectivityService.log("Adding agent " + nai + " for legacy network type " + type);
                }
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                if (!list.contains(nai)) {
                    synchronized (this.mTypeLists) {
                        list.add(nai);
                    }
                    boolean isDefaultNetwork = ConnectivityService.this.isDefaultNetwork(nai);
                    if (list.size() == 1 || isDefaultNetwork) {
                        maybeLogBroadcast(nai, DetailedState.CONNECTED, type, isDefaultNetwork);
                        ConnectivityService.this.sendLegacyNetworkBroadcast(nai, DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:13:0x0024, code:
            r2 = android.net.NetworkInfo.DetailedState.DISCONNECTED;
     */
        /* JADX WARNING: Missing block: B:14:0x0026, code:
            if (r3 != false) goto L_0x002a;
     */
        /* JADX WARNING: Missing block: B:15:0x0028, code:
            if (r10 == false) goto L_0x0032;
     */
        /* JADX WARNING: Missing block: B:16:0x002a, code:
            maybeLogBroadcast(r9, r2, r8, r10);
            com.android.server.ConnectivityService.-wrap32(r7.this$0, r9, r2, r8);
     */
        /* JADX WARNING: Missing block: B:18:0x0036, code:
            if (r0.isEmpty() != false) goto L_0x006c;
     */
        /* JADX WARNING: Missing block: B:19:0x0038, code:
            if (r3 == false) goto L_0x006c;
     */
        /* JADX WARNING: Missing block: B:20:0x003a, code:
            com.android.server.ConnectivityService.-wrap24("Other network available for type " + r8 + ", sending connected broadcast");
            r1 = (com.android.server.connectivity.NetworkAgentInfo) r0.get(0);
            maybeLogBroadcast(r1, r2, r8, com.android.server.ConnectivityService.-wrap0(r7.this$0, r1));
            com.android.server.ConnectivityService.-wrap32(r7.this$0, r1, r2, r8);
     */
        /* JADX WARNING: Missing block: B:21:0x006c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = ((NetworkAgentInfo) list.get(0)).equals(nai);
                synchronized (this.mTypeLists) {
                    if (!list.remove(nai)) {
                    }
                }
            }
        }

        public void remove(NetworkAgentInfo nai, boolean wasDefault) {
            if (ConnectivityService.VDBG) {
                ConnectivityService.log("Removing agent " + nai + " wasDefault=" + wasDefault);
            }
            for (int type = 0; type < this.mTypeLists.length; type++) {
                remove(type, nai, wasDefault);
            }
        }

        public void update(NetworkAgentInfo nai) {
            boolean isDefault = ConnectivityService.this.isDefaultNetwork(nai);
            DetailedState state = nai.networkInfo.getDetailedState();
            for (int type = 0; type < this.mTypeLists.length; type++) {
                boolean contains;
                boolean isFirst;
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                if (list != null) {
                    contains = list.contains(nai);
                } else {
                    contains = false;
                }
                if (contains && nai == list.get(0)) {
                    isFirst = true;
                } else {
                    isFirst = false;
                }
                if (isFirst || (contains && isDefault)) {
                    maybeLogBroadcast(nai, state, type, isDefault);
                    ConnectivityService.this.sendLegacyNetworkBroadcast(nai, state, type);
                }
            }
        }

        private String naiToString(NetworkAgentInfo nai) {
            String state;
            String name = nai != null ? nai.name() : "null";
            if (nai.networkInfo != null) {
                state = nai.networkInfo.getState() + "/" + nai.networkInfo.getDetailedState();
            } else {
                state = "???/???";
            }
            return name + " " + state;
        }

        public void dump(IndentingPrintWriter pw) {
            int type;
            pw.println("mLegacyTypeTracker:");
            pw.increaseIndent();
            pw.print("Supported types:");
            for (type = 0; type < this.mTypeLists.length; type++) {
                if (this.mTypeLists[type] != null) {
                    pw.print(" " + type);
                }
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            synchronized (this.mTypeLists) {
                type = 0;
                while (type < this.mTypeLists.length) {
                    if (!(this.mTypeLists[type] == null || this.mTypeLists[type].isEmpty())) {
                        for (NetworkAgentInfo nai : this.mTypeLists[type]) {
                            pw.println(type + " " + naiToString(nai));
                        }
                    }
                    type++;
                }
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println();
        }
    }

    private static class NetworkFactoryInfo {
        public final AsyncChannel asyncChannel;
        public final Messenger messenger;
        public final String name;

        public NetworkFactoryInfo(String name, Messenger messenger, AsyncChannel asyncChannel) {
            this.name = name;
            this.messenger = messenger;
            this.asyncChannel = asyncChannel;
        }
    }

    private class NetworkRequestInfo implements DeathRecipient {
        private final IBinder mBinder;
        final PendingIntent mPendingIntent;
        boolean mPendingIntentSent;
        final int mPid;
        final int mUid;
        final Messenger messenger;
        final NetworkRequest request;

        NetworkRequestInfo(NetworkRequest r, PendingIntent pi) {
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mPendingIntent = pi;
            this.messenger = null;
            this.mBinder = null;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
            enforceRequestCountLimit();
        }

        NetworkRequestInfo(Messenger m, NetworkRequest r, IBinder binder) {
            this.messenger = m;
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mBinder = binder;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
            this.mPendingIntent = null;
            enforceRequestCountLimit();
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        private void enforceRequestCountLimit() {
            synchronized (ConnectivityService.this.mUidToNetworkRequestCount) {
                int networkRequests = ConnectivityService.this.mUidToNetworkRequestCount.get(this.mUid, 0) + 1;
                if (networkRequests >= 100) {
                    throw new IllegalArgumentException("Too many NetworkRequests filed");
                }
                ConnectivityService.this.mUidToNetworkRequestCount.put(this.mUid, networkRequests);
            }
        }

        void unlinkDeathRecipient() {
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            ConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            ConnectivityService.this.releaseNetworkRequest(this.request);
        }

        public String toString() {
            return "uid/pid:" + this.mUid + "/" + this.mPid + " " + this.request + (this.mPendingIntent == null ? IElsaManager.EMPTY_PACKAGE : " to trigger " + this.mPendingIntent);
        }
    }

    private class NetworkStateTrackerHandler extends Handler {
        public NetworkStateTrackerHandler(Looper looper) {
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    ConnectivityService.this.handleAsyncChannelHalfConnect(msg);
                    break;
                case 69635:
                    NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai != null) {
                        nai.asyncChannel.disconnect();
                        break;
                    }
                    break;
                case 69636:
                    ConnectivityService.this.handleAsyncChannelDisconnected(msg);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
            if (nai == null) {
                if (ConnectivityService.VDBG) {
                    Object[] objArr = new Object[1];
                    objArr[0] = (String) ConnectivityService.sMagicDecoderRing.get(msg.what, Integer.toString(msg.what));
                    ConnectivityService.log(String.format("%s from unknown NetworkAgent", objArr));
                }
                return;
            }
            switch (msg.what) {
                case 528385:
                    ConnectivityService.this.updateNetworkInfo(nai, msg.obj);
                    break;
                case 528386:
                    NetworkCapabilities networkCapabilities = msg.obj;
                    if (networkCapabilities.hasCapability(17) || networkCapabilities.hasCapability(16) || networkCapabilities.hasCapability(18)) {
                        Slog.e(ConnectivityService.TAG, "BUG: " + nai + " has CS-managed capability.");
                    }
                    ConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, networkCapabilities);
                    break;
                case 528387:
                    if (ConnectivityService.VDBG) {
                        ConnectivityService.log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
                    }
                    LinkProperties oldLp = nai.linkProperties;
                    synchronized (nai) {
                        nai.linkProperties = (LinkProperties) msg.obj;
                    }
                    if (nai.everConnected) {
                        ConnectivityService.this.updateLinkProperties(nai, oldLp);
                        break;
                    }
                    break;
                case 528388:
                    Integer score = msg.obj;
                    if (score != null) {
                        ConnectivityService.this.updateNetworkScore(nai, score.intValue());
                        break;
                    }
                    break;
                case 528389:
                    try {
                        ConnectivityService.this.mNetd.addVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e) {
                        ConnectivityService.loge("Exception in addVpnUidRanges: " + e);
                        break;
                    }
                case 528390:
                    try {
                        ConnectivityService.this.mNetd.removeVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e2) {
                        ConnectivityService.loge("Exception in removeVpnUidRanges: " + e2);
                        break;
                    }
                case 528392:
                    if (nai.everConnected && !nai.networkMisc.explicitlySelected) {
                        ConnectivityService.loge("ERROR: already-connected network explicitly selected.");
                    }
                    nai.networkMisc.explicitlySelected = true;
                    nai.networkMisc.acceptUnvalidated = ((Boolean) msg.obj).booleanValue();
                    break;
                case 528397:
                    ConnectivityService.this.mKeepaliveTracker.handleEventPacketKeepalive(nai, msg);
                    break;
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            NetworkAgentInfo nai;
            int oldScore;
            switch (msg.what) {
                case NetworkMonitor.EVENT_NETWORK_TESTED /*532482*/:
                    synchronized (ConnectivityService.this.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkForNetId.get(msg.arg2);
                    }
                    if (nai != null) {
                        String str;
                        boolean valid = msg.arg1 == 0;
                        boolean wasValidated = nai.lastValidated;
                        StringBuilder append = new StringBuilder().append(nai.name()).append(" validation ").append(valid ? "passed" : "failed");
                        if (msg.obj == null) {
                            str = IElsaManager.EMPTY_PACKAGE;
                        } else {
                            str = " with redirect to " + ((String) msg.obj);
                        }
                        ConnectivityService.log(append.append(str).toString());
                        if (valid && nai.networkCapabilities.hasTransport(1)) {
                            ConnectivityService.log("wifi valid");
                            ConnectivityService.this.updateNetworkScore(nai, 79);
                        }
                        if (valid != nai.lastValidated) {
                            oldScore = nai.getCurrentScore();
                            nai.lastValidated = valid;
                            nai.everValidated |= valid;
                            ConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                            if (oldScore != nai.getCurrentScore()) {
                                ConnectivityService.this.sendUpdatedScoreToFactories(nai);
                            }
                        }
                        ConnectivityService.this.updateInetCondition(nai);
                        Bundle redirectUrlBundle = new Bundle();
                        redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, (String) msg.obj);
                        nai.asyncChannel.sendMessage(528391, valid ? 1 : 2, 0, redirectUrlBundle);
                        if (wasValidated && !nai.lastValidated) {
                            ConnectivityService.this.handleNetworkUnvalidated(nai);
                            break;
                        }
                    }
                    break;
                case NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION /*532490*/:
                    int netId = msg.arg2;
                    boolean visible = msg.arg1 != 0;
                    synchronized (ConnectivityService.this.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkForNetId.get(netId);
                    }
                    if (!(nai == null || visible == nai.lastCaptivePortalDetected)) {
                        oldScore = nai.getCurrentScore();
                        nai.lastCaptivePortalDetected = visible;
                        nai.everCaptivePortalDetected |= visible;
                        ConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                    }
                    if (visible) {
                        if (nai != null) {
                            if (!nai.networkMisc.provisioningNotificationDisabled) {
                                ConnectivityService.this.mNotifier.showNotification(netId, NotificationType.SIGN_IN, nai, null, (PendingIntent) msg.obj, nai.networkMisc.explicitlySelected);
                                break;
                            }
                        }
                        ConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                        break;
                    }
                    ConnectivityService.this.mNotifier.clearNotification(netId);
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean maybeHandleNetworkAgentInfoMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    NetworkAgentInfo nai = msg.obj;
                    if (nai != null && ConnectivityService.this.isLiveNetworkAgent(nai, msg.what)) {
                        if (ConnectivityService.this.hasWifiAssistant && nai != null && nai.networkInfo != null && nai.networkInfo.getType() == 1 && ConnectivityService.this.isSwitchEnable()) {
                            ConnectivityService.log("wifi type,no linger");
                        } else {
                            ConnectivityService.this.handleLingerComplete(nai);
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }

        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && !maybeHandleNetworkMonitorMessage(msg) && !maybeHandleNetworkAgentInfoMessage(msg)) {
                maybeHandleNetworkAgentMessage(msg);
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private enum ReapUnvalidatedNetworks {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void");
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            Slog.e(ConnectivityService.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                ConnectivityService.loge("No matching event to send for URI=" + uri);
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private enum UnneededFor {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.ConnectivityService.UnneededFor.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.ConnectivityService.UnneededFor.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.UnneededFor.<clinit>():void");
        }
    }

    private static class ValidationLog {
        final ReadOnlyLocalLog mLog;
        final Network mNetwork;
        final String mNetworkExtraInfo;

        ValidationLog(Network network, String networkExtraInfo, ReadOnlyLocalLog log) {
            this.mNetwork = network;
            this.mNetworkExtraInfo = networkExtraInfo;
            this.mLog = log;
        }
    }

    /* renamed from: -getcom-android-server-ConnectivityService$UnneededForSwitchesValues */
    private static /* synthetic */ int[] m8xd7b145e2() {
        if (f2x78a82206 != null) {
            return f2x78a82206;
        }
        int[] iArr = new int[UnneededFor.values().length];
        try {
            iArr[UnneededFor.LINGER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[UnneededFor.TEARDOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        f2x78a82206 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues */
    private static /* synthetic */ int[] m9x28b55b5a() {
        if (f3x6e7dafb6 != null) {
            return f3x6e7dafb6;
        }
        int[] iArr = new int[NotificationType.values().length];
        try {
            iArr[NotificationType.LOST_INTERNET.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NotificationType.NETWORK_SWITCH.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NotificationType.NO_INTERNET.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[NotificationType.SIGN_IN.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        f3x6e7dafb6 = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.ConnectivityService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.ConnectivityService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.<clinit>():void");
    }

    private void addValidationLogs(ReadOnlyLocalLog log, Network network, String networkExtraInfo) {
        synchronized (this.mValidationLogs) {
            while (this.mValidationLogs.size() >= 10) {
                this.mValidationLogs.removeLast();
            }
            this.mValidationLogs.addFirst(new ValidationLog(network, networkExtraInfo, log));
        }
    }

    protected HandlerThread createHandlerThread() {
        return new HandlerThread("ConnectivityServiceThread");
    }

    public ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this(context, netManager, statsService, policyManager, new IpConnectivityLog());
    }

    protected ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager, IpConnectivityLog logger) {
        boolean equals;
        this.mBlock = false;
        this.mMeasureTime = 0;
        this.mShouldKeepCelluarNetwork = false;
        this.TOTAL_MEASURE_TIME = 3;
        this.RESPONSE_TIMEOUT = 5000;
        String[] strArr = new String[3];
        strArr[0] = "conn1.oppomobile.com";
        strArr[1] = "conn2.oppomobile.com";
        strArr[2] = "www.baidu.com";
        this.SERVERS = strArr;
        this.RECONNECT_TIMER = 30000;
        this.REMEASURE_TIMER = 1000;
        this.REMEASURE_TIMER_FINE = 300000;
        this.ENABLE_WLAN_ASSISTANT = 1;
        this.mVpns = new SparseArray();
        this.mRulesLock = new Object();
        this.mUidRules = new SparseIntArray();
        this.mMeteredIfaces = new ArraySet();
        this.mDefaultInetConditionPublished = 0;
        this.mNetTransitionWakeLockCausedBy = IElsaManager.EMPTY_PACKAGE;
        this.mDefaultProxy = null;
        this.mProxyLock = new Object();
        this.mDefaultProxyDisabled = false;
        this.mGlobalProxy = null;
        this.mPacManager = null;
        this.mNextNetId = 100;
        this.mNextNetworkRequestId = 1;
        this.mRequestLock = new Object();
        this.mIcsExt = null;
        this.mNetworkRequestInfoLogs = new LocalLog(20);
        this.mNetworkInfoBlockingLogs = new LocalLog(100);
        this.mValidationLogs = new ArrayDeque(10);
        this.mLegacyTypeTracker = new LegacyTypeTracker();
        this.hasWifiAssistant = false;
        this.mDataActivityObserver = new BaseNetworkObserver() {
            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                ConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mPolicyListener = new INetworkPolicyListener.Stub() {
            /* JADX WARNING: Missing block: B:11:0x0022, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onUidRulesChanged(int uid, int uidRules) {
                synchronized (ConnectivityService.this.mRulesLock) {
                    if (ConnectivityService.this.mUidRules.get(uid, 0) == uidRules) {
                    } else if (uidRules == 0) {
                        ConnectivityService.this.mUidRules.delete(uid);
                    } else {
                        ConnectivityService.this.mUidRules.put(uid, uidRules);
                    }
                }
            }

            public void onMeteredIfacesChanged(String[] meteredIfaces) {
                if (ConnectivityService.LOGD_RULES) {
                    ConnectivityService.log("onMeteredIfacesChanged(ifaces=" + Arrays.toString(meteredIfaces) + ")");
                }
                synchronized (ConnectivityService.this.mRulesLock) {
                    ConnectivityService.this.mMeteredIfaces.clear();
                    for (String iface : meteredIfaces) {
                        ConnectivityService.this.mMeteredIfaces.add(iface);
                    }
                }
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                if (ConnectivityService.LOGD_RULES) {
                    ConnectivityService.log("onRestrictBackgroundChanged(restrictBackground=" + restrictBackground + ")");
                }
                synchronized (ConnectivityService.this.mRulesLock) {
                    ConnectivityService.this.mRestrictBackground = restrictBackground;
                }
                if (restrictBackground) {
                    ConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    ConnectivityService.this.mTethering.untetherAll();
                }
            }

            public void onRestrictBackgroundWhitelistChanged(int uid, boolean whitelisted) {
                if (ConnectivityService.LOGD_RULES) {
                    ConnectivityService.log("onRestrictBackgroundWhitelistChanged(uid=" + uid + ", whitelisted=" + whitelisted + ")");
                }
            }

            public void onRestrictBackgroundBlacklistChanged(int uid, boolean blacklisted) {
                if (ConnectivityService.LOGD_RULES) {
                    ConnectivityService.log("onRestrictBackgroundBlacklistChanged(uid=" + uid + ", blacklisted=" + blacklisted + ")");
                }
            }
        };
        this.mProvisioningUrlFile = new File(PROVISIONING_URL_PATH);
        this.mUserIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId != -10000) {
                    if ("android.intent.action.USER_STARTED".equals(action)) {
                        ConnectivityService.this.onUserStart(userId);
                    } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                        ConnectivityService.this.onUserStop(userId);
                    } else if ("android.intent.action.USER_ADDED".equals(action)) {
                        ConnectivityService.this.onUserAdded(userId);
                    } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                        ConnectivityService.this.onUserRemoved(userId);
                    } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                        ConnectivityService.this.onUserUnlocked(userId);
                    }
                }
            }
        };
        this.mNetworkFactoryInfos = new HashMap();
        this.mNetworkRequests = new HashMap();
        this.mUidToNetworkRequestCount = new SparseIntArray();
        this.mNetworkForRequestId = new SparseArray();
        this.mNetworkForNetId = new SparseArray();
        this.mNetIdInUse = new SparseBooleanArray();
        this.mNetworkAgentInfos = new HashMap();
        this.mBlockedAppUids = new HashSet();
        log("ConnectivityService starting up");
        this.mMetricsLog = logger;
        this.mDefaultRequest = createInternetRequestForTransport(-1, Type.REQUEST);
        NetworkRequestInfo defaultNRI = new NetworkRequestInfo(null, this.mDefaultRequest, new Binder());
        if (SystemProperties.getBoolean("persist.op12.ccp.mode", false)) {
            log("isCcpMode enabled, don't assign default networkRequest");
        } else if (sIsAutoTethering) {
            log("Delay default network request until boot is completed");
        } else {
            this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI);
            this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        }
        this.mDefaultMobileDataRequest = createInternetRequestForTransport(0, Type.BACKGROUND_REQUEST);
        this.mHandlerThread = createHandlerThread();
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this.mHandlerThread.getLooper());
        if (TextUtils.isEmpty(SystemProperties.get("net.hostname"))) {
            String id = Secure.getString(context.getContentResolver(), "android_id");
            if (id != null && id.length() > 0) {
                SystemProperties.set("net.hostname", new String("android-").concat(id));
            }
        }
        this.mReleasePendingIntentDelayMs = Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mLingerDelayMs = SystemProperties.getInt(LINGER_DELAY_PROPERTY, 3000);
        this.mContext = (Context) checkNotNull(context, "missing Context");
        this.mNetd = (INetworkManagementService) checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.setConnectivityListener(this.mPolicyListener);
            this.mRestrictBackground = this.mPolicyManager.getRestrictBackground();
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694735);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetConfigs = new NetworkConfig[50];
        boolean wifiOnly = SystemProperties.getBoolean("ro.radio.noril", false);
        log("wifiOnly=" + wifiOnly);
        for (String naString : context.getResources().getStringArray(17235985)) {
            try {
                NetworkConfig n = new NetworkConfig(naString);
                if (VDBG) {
                    log("naString=" + naString + " config=" + n);
                }
                if (n.type > 49) {
                    loge("Error in networkAttributes - ignoring attempt to define type " + n.type);
                } else {
                    if (wifiOnly) {
                        if (ConnectivityManager.isNetworkTypeMobile(n.type)) {
                            log("networkAttributes - ignoring mobile as this dev is wifiOnly " + n.type);
                        }
                    }
                    if (this.mNetConfigs[n.type] != null) {
                        loge("Error in networkAttributes - ignoring attempt to redefine type " + n.type);
                    } else {
                        this.mLegacyTypeTracker.addSupportedType(n.type);
                        this.mNetConfigs[n.type] = n;
                        this.mNetworksDefined++;
                    }
                }
            } catch (Exception e2) {
            }
        }
        if (this.mNetConfigs[17] == null) {
            this.mLegacyTypeTracker.addSupportedType(17);
            this.mNetworksDefined++;
        }
        if (VDBG) {
            log("mNetworksDefined=" + this.mNetworksDefined);
        }
        this.mProtectedNetworks = new ArrayList();
        for (int p : context.getResources().getIntArray(17235986)) {
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        if (SystemProperties.get("cm.test.mode").equals("true")) {
            equals = SystemProperties.get("ro.build.type").equals("eng");
        } else {
            equals = false;
        }
        this.mTestMode = equals;
        this.mTethering = new Tethering(this.mContext, this.mNetd, statsService, this.mPolicyManager);
        this.mPermissionMonitor = new PermissionMonitor(this.mContext, this.mNetd);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        try {
            this.mNetd.registerObserver(this.mTethering);
            this.mNetd.registerObserver(this.mDataActivityObserver);
        } catch (RemoteException e3) {
            loge("Error registering observer :" + e3);
        }
        this.mInetLog = new ArrayList();
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerSettingsCallbacks();
        this.mDataConnectionStats = new DataConnectionStats(this.mContext);
        this.mDataConnectionStats.startMonitoring();
        this.mPacManager = new PacManager(this.mContext, this.mHandler, 16);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mKeepaliveTracker = new KeepaliveTracker(this.mHandler);
        IntentFilter filterC = new IntentFilter();
        filterC.addAction("android.intent.action.TETHERING_CHANGED");
        if (sIsAutoTethering) {
            filterC.addAction("android.intent.action.BOOT_COMPLETED");
            filterC.addAction("android.intent.action.ACTION_BOOT_IPO");
        }
        this.mReceiver = new ConnectivityServiceReceiver(this, null);
        this.mContext.registerReceiver(this.mReceiver, filterC);
        this.mSynchronizedObject = new Object();
        String dns = this.mContext.getResources().getString(17039433);
        try {
            this.mDefaultDns = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e4) {
            loge("Error setting defaultDns using " + dns);
        }
        this.mNotifier = new NetworkNotificationManager(this.mContext, this.mTelephonyManager, (NotificationManager) this.mContext.getSystemService(NotificationManager.class));
        this.mLingerMonitor = new LingerMonitor(this.mContext, this.mNotifier, Global.getInt(this.mContext.getContentResolver(), "network_switch_notification_daily_limit", 3), Global.getLong(this.mContext.getContentResolver(), "network_switch_notification_rate_limit_millis", 60000));
        this.mAvoidBadWifiTracker = createAvoidBadWifiTracker(this.mContext, this.mHandler, new -void__init__android_content_Context_context_android_os_INetworkManagementService_netManager_android_net_INetworkStatsService_statsService_android_net_INetworkPolicyManager_policyManager_android_net_metrics_IpConnectivityLog_logger_LambdaImpl0());
        this.hasWifiAssistant = isWlanAssistantEnable();
    }

    private NetworkRequest createInternetRequestForTransport(int transportType, Type type) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        if (transportType > -1) {
            netCap.addTransportType(transportType);
        }
        return new NetworkRequest(netCap, -1, nextNetworkRequestId(), type);
    }

    void updateMobileDataAlwaysOn() {
        this.mHandler.sendEmptyMessage(30);
    }

    private void handleMobileDataAlwaysOn() {
        boolean isEnabled = true;
        boolean enable = Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", 0) == 1;
        if (this.mNetworkRequests.get(this.mDefaultMobileDataRequest) == null) {
            isEnabled = false;
        }
        if (enable != isEnabled) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, this.mDefaultMobileDataRequest, new Binder()));
            } else {
                handleReleaseNetworkRequest(this.mDefaultMobileDataRequest, 1000);
            }
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Global.getUriFor("http_proxy"), 9);
        this.mSettingsObserver.observe(Global.getUriFor("mobile_data_always_on"), 30);
    }

    private synchronized int nextNetworkRequestId() {
        int i;
        i = this.mNextNetworkRequestId;
        this.mNextNetworkRequestId = i + 1;
        return i;
    }

    protected int reserveNetId() {
        synchronized (this.mNetworkForNetId) {
            int i = 100;
            while (i <= MAX_NET_ID) {
                int netId = this.mNextNetId;
                int i2 = this.mNextNetId + 1;
                this.mNextNetId = i2;
                if (i2 > MAX_NET_ID) {
                    this.mNextNetId = 100;
                }
                if (this.mNetIdInUse.get(netId)) {
                    i++;
                } else {
                    this.mNetIdInUse.put(netId, true);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid, boolean ignoreBlocked) {
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkState state;
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            NetworkInfo info = new NetworkInfo(networkType, 0, ConnectivityManager.getNetworkTypeName(networkType), IElsaManager.EMPTY_PACKAGE);
            info.setDetailedState(DetailedState.DISCONNECTED, null, null);
            info.setIsAvailable(true);
            state = new NetworkState(info, new LinkProperties(), new NetworkCapabilities(), null, null, null);
        }
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state;
    }

    private NetworkAgentInfo getNetworkAgentInfoForNetwork(Network network) {
        if (network == null) {
            return null;
        }
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForNetId) {
            networkAgentInfo = (NetworkAgentInfo) this.mNetworkForNetId.get(network.netId);
        }
        return networkAgentInfo;
    }

    private Network[] getVpnUnderlyingNetworks(int uid) {
        if (!this.mLockdownEnabled) {
            int user = UserHandle.getUserId(uid);
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(user);
                if (vpn == null || !vpn.appliesToUid(uid)) {
                } else {
                    Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
                    return underlyingNetworks;
                }
            }
        }
        return null;
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

    /* JADX WARNING: Missing block: B:16:0x0026, code:
            if (r11 != null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:17:0x0028, code:
            r1 = com.android.server.oppo.IElsaManager.EMPTY_PACKAGE;
     */
    /* JADX WARNING: Missing block: B:18:0x002b, code:
            r8 = r10.mRulesLock;
     */
    /* JADX WARNING: Missing block: B:19:0x002d, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r2 = r10.mMeteredIfaces.contains(r1);
            r3 = r10.mUidRules.get(r12, 0);
     */
    /* JADX WARNING: Missing block: B:22:0x003b, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:23:0x003c, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:24:0x003d, code:
            if (r2 == false) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:26:0x0041, code:
            if ((r3 & 4) == 0) goto L_0x007d;
     */
    /* JADX WARNING: Missing block: B:28:0x0045, code:
            if (LOGD_RULES == false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:29:0x0047, code:
            android.util.Log.d(TAG, "uid " + r12 + " is blacklisted");
     */
    /* JADX WARNING: Missing block: B:30:0x0067, code:
            r0 = false;
     */
    /* JADX WARNING: Missing block: B:31:0x0068, code:
            if (r0 == false) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:33:0x006c, code:
            if ((r3 & 64) != 0) goto L_0x00e4;
     */
    /* JADX WARNING: Missing block: B:34:0x006e, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:35:0x006f, code:
            if (r0 == false) goto L_0x00e6;
     */
    /* JADX WARNING: Missing block: B:36:0x0071, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:40:0x0075, code:
            r1 = r11.getInterfaceName();
     */
    /* JADX WARNING: Missing block: B:45:0x007f, code:
            if (r10.mRestrictBackground == false) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:47:0x0083, code:
            if ((r3 & 1) == 0) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:48:0x0085, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:50:0x0088, code:
            if (LOGD_RULES == false) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:51:0x008a, code:
            r8 = TAG;
            r9 = new java.lang.StringBuilder().append("allowed status for uid ").append(r12).append(" when").append(" mRestrictBackground=").append(r10.mRestrictBackground).append(", whitelisted=");
     */
    /* JADX WARNING: Missing block: B:52:0x00b9, code:
            if ((r3 & 1) == 0) goto L_0x00e2;
     */
    /* JADX WARNING: Missing block: B:53:0x00bb, code:
            r5 = true;
     */
    /* JADX WARNING: Missing block: B:54:0x00bc, code:
            android.util.Log.d(r8, r9.append(r5).append(", tempWhitelist= + ((uidRules & RULE_TEMPORARY_ALLOW_METERED) != 0)").append(": ").append(r0).toString());
     */
    /* JADX WARNING: Missing block: B:56:0x00dc, code:
            if ((r3 & 2) == 0) goto L_0x00e0;
     */
    /* JADX WARNING: Missing block: B:57:0x00de, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:58:0x00e0, code:
            r0 = false;
     */
    /* JADX WARNING: Missing block: B:59:0x00e2, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:60:0x00e4, code:
            r0 = false;
     */
    /* JADX WARNING: Missing block: B:61:0x00e6, code:
            r7 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isNetworkWithLinkPropertiesBlocked(LinkProperties lp, int uid, boolean ignoreBlocked) {
        boolean z = false;
        if (ignoreBlocked || isSystem(uid)) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn == null || !vpn.isBlockingUid(uid)) {
            } else {
                return true;
            }
        }
    }

    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        if (ni != null && LOGD_BLOCKED_NETWORKINFO) {
            boolean removed = false;
            boolean added = false;
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    added = true;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    removed = true;
                }
            }
            if (added) {
                log("Returning blocked NetworkInfo to uid=" + uid);
                this.mNetworkInfoBlockingLogs.log("BLOCKED " + uid);
            } else if (removed) {
                log("Returning unblocked NetworkInfo to uid=" + uid);
                this.mNetworkInfoBlockingLogs.log("UNBLOCKED " + uid);
            }
        }
    }

    private void filterNetworkStateForUid(NetworkState state, int uid, boolean ignoreBlocked) {
        if (state != null && state.networkInfo != null && state.linkProperties != null) {
            if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, ignoreBlocked)) {
                state.networkInfo.setDetailedState(DetailedState.BLOCKED, null, null);
            }
            if (this.mLockdownTracker != null) {
                this.mLockdownTracker.augmentNetworkInfo(state.networkInfo);
            }
            long token = Binder.clearCallingIdentity();
            try {
                state.networkInfo.setMetered(this.mPolicyManager.isNetworkMetered(state));
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(token);
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
        Network network;
        int user = UserHandle.getUserId(uid);
        int vpnNetId = 0;
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(user);
            if (vpn != null && vpn.appliesToUid(uid)) {
                vpnNetId = vpn.getNetId();
            }
        }
        if (vpnNetId != 0) {
            synchronized (this.mNetworkForNetId) {
                nai = (NetworkAgentInfo) this.mNetworkForNetId.get(vpnNetId);
            }
            if (nai != null) {
                return nai.network;
            }
        }
        nai = getDefaultNetwork();
        if (nai != null && isNetworkWithLinkPropertiesBlocked(nai.linkProperties, uid, ignoreBlocked)) {
            nai = null;
        }
        log("getActiveNetworkForUidInternal nai = " + (nai == null ? "null" : nai.name()));
        if (nai != null) {
            network = nai.network;
        } else {
            network = null;
        }
        return network;
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
        return getFilteredNetworkState(networkType, uid, false).networkInfo;
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
        for (int networkType = 0; networkType <= 49; networkType++) {
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
        NetworkState state = getFilteredNetworkState(networkType, uid, false);
        if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, false)) {
            return null;
        }
        return state.network;
    }

    public Network[] getAllNetworks() {
        Network[] result;
        enforceAccessPermission();
        synchronized (this.mNetworkForNetId) {
            result = new Network[this.mNetworkForNetId.size()];
            for (int i = 0; i < this.mNetworkForNetId.size(); i++) {
                result[i] = ((NetworkAgentInfo) this.mNetworkForNetId.valueAt(i)).network;
            }
        }
        return result;
    }

    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        enforceAccessPermission();
        HashMap<Network, NetworkCapabilities> result = new HashMap();
        NetworkAgentInfo nai = getDefaultNetwork();
        NetworkCapabilities nc = getNetworkCapabilitiesInternal(nai);
        if (nc != null) {
            result.put(nai.network, nc);
        }
        if (!this.mLockdownEnabled) {
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(userId);
                if (vpn != null) {
                    Network[] networks = vpn.getUnderlyingNetworks();
                    if (networks != null) {
                        for (Network network : networks) {
                            nc = getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
                            if (nc != null) {
                                result.put(network, nc);
                            }
                        }
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
        enforceAccessPermission();
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai == null) {
            return null;
        }
        LinkProperties linkProperties;
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    public LinkProperties getLinkProperties(Network network) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null) {
            return null;
        }
        LinkProperties linkProperties;
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    private NetworkCapabilities getNetworkCapabilitiesInternal(NetworkAgentInfo nai) {
        if (nai != null) {
            synchronized (nai) {
                if (nai.networkCapabilities != null) {
                    NetworkCapabilities networkCapabilities = new NetworkCapabilities(nai.networkCapabilities);
                    return networkCapabilities;
                }
            }
        }
        return null;
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        enforceAccessPermission();
        return getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
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

    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long token = Binder.clearCallingIdentity();
        try {
            NetworkState state = getUnfilteredActiveNetworkState(uid);
            if (state.networkInfo != null) {
                try {
                    NetworkQuotaInfo networkQuotaInfo = this.mPolicyManager.getNetworkQuotaInfo(state);
                    return networkQuotaInfo;
                } catch (RemoteException e) {
                }
            }
            Binder.restoreCallingIdentity(token);
            return null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean isActiveNetworkMetered() {
        enforceAccessPermission();
        NetworkInfo info = getActiveNetworkInfo();
        return info != null ? info.isMetered() : false;
    }

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        enforceChangePermission();
        if (this.mProtectedNetworks.contains(Integer.valueOf(networkType))) {
            enforceConnectivityInternalPermission();
        }
        try {
            InetAddress addr = InetAddress.getByAddress(hostAddress);
            if (ConnectivityManager.isNetworkTypeValid(networkType)) {
                NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
                if (nai == null) {
                    if (this.mLegacyTypeTracker.isTypeSupported(networkType)) {
                        log("requestRouteToHostAddress on down network: " + networkType);
                    } else {
                        log("requestRouteToHostAddress on unsupported network: " + networkType);
                    }
                    return false;
                }
                DetailedState netState;
                synchronized (nai) {
                    netState = nai.networkInfo.getDetailedState();
                }
                if (netState == DetailedState.CONNECTED || netState == DetailedState.CAPTIVE_PORTAL_CHECK) {
                    int uid = Binder.getCallingUid();
                    long token = Binder.clearCallingIdentity();
                    try {
                        LinkProperties lp;
                        int netId;
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
                        log("requestRouteToHostAddress on down network (" + networkType + ") - dropped" + " netState=" + netState);
                    }
                    return false;
                }
            }
            log("requestRouteToHostAddress on invalid network: " + networkType);
            return false;
        } catch (UnknownHostException e) {
            log("requestRouteToHostAddress got " + e.toString());
            return false;
        }
    }

    private boolean addLegacyRouteToHost(LinkProperties lp, InetAddress addr, int netId, int uid) {
        RouteInfo bestRoute = RouteInfo.selectBestRoute(lp.getAllRoutes(), addr);
        if (bestRoute == null) {
            bestRoute = RouteInfo.makeHostRoute(addr, lp.getInterfaceName());
        } else {
            String iface = bestRoute.getInterface();
            if (bestRoute.getGateway().equals(addr)) {
                bestRoute = RouteInfo.makeHostRoute(addr, iface);
            } else {
                bestRoute = RouteInfo.makeHostRoute(addr, bestRoute.getGateway(), iface);
            }
        }
        log("Adding legacy route " + bestRoute + " for UID/PID " + uid + "/" + Binder.getCallingPid());
        try {
            this.mNetd.addLegacyRouteForNetId(netId, bestRoute, uid);
            return true;
        } catch (Exception e) {
            loge("Exception trying to add a route: " + e);
            return false;
        }
    }

    private void enforceCrossUserPermission(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
    }

    private void enforceInternetPermission() {
        this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_SEND_MMS_INTERNET, "ConnectivityService");
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceChangePermission() {
        ConnectivityManager.enforceChangePermission(this.mContext);
    }

    private void enforceTetherAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    private void enforceConnectivityRestrictedNetworksPermission() {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS", "ConnectivityService");
        } catch (SecurityException e) {
            enforceConnectivityInternalPermission();
        }
    }

    private void enforceKeepalivePermission() {
        this.mContext.enforceCallingOrSelfPermission(KeepaliveTracker.PERMISSION, "ConnectivityService");
    }

    public void sendConnectedBroadcast(NetworkInfo info) {
        enforceConnectivityInternalPermission();
        sendGeneralBroadcast(info, "android.net.conn.CONNECTIVITY_CHANGE");
    }

    private void sendInetConditionBroadcast(NetworkInfo info) {
        sendGeneralBroadcast(info, "android.net.conn.INET_CONDITION_ACTION");
    }

    private Intent makeGeneralIntent(NetworkInfo info, String bcastType) {
        if (this.mLockdownTracker != null) {
            NetworkInfo info2 = new NetworkInfo(info);
            this.mLockdownTracker.augmentNetworkInfo(info2);
            info = info2;
        }
        Intent intent = new Intent(bcastType);
        intent.putExtra("networkInfo", new NetworkInfo(info));
        intent.putExtra("networkType", info.getType());
        if (info.isFailover()) {
            intent.putExtra("isFailover", true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, info.getReason());
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

    private void sendDataActivityBroadcast(int deviceType, boolean active, long tsNanos) {
        Intent intent = new Intent("android.net.conn.DATA_ACTIVITY_CHANGE");
        intent.putExtra("deviceType", deviceType);
        intent.putExtra("isActive", active);
        intent.putExtra("tsNanos", tsNanos);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_DATA_ACTIVITY_CHANGE", null, null, 0, null, null);
            Intent intentCt = new Intent("android.net.conn.DATA_ACTIVITY_CHANGE_CT");
            intentCt.putExtra("deviceType", deviceType);
            intentCt.putExtra("isActive", active);
            intentCt.putExtra("tsNanos", tsNanos);
            long identCt = Binder.clearCallingIdentity();
            try {
                this.mContext.sendStickyBroadcastAsUser(intentCt, UserHandle.ALL);
            } finally {
                Binder.restoreCallingIdentity(identCt);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendStickyBroadcast(Intent intent) {
        synchronized (this) {
            if (!this.mSystemReady) {
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
                    opts.setMaxManifestReceiverApiLevel(23);
                    options = opts.toBundle();
                }
                try {
                    BatteryStatsService.getService().noteConnectivityChanged(intent.getIntExtra("networkType", -1), ni != null ? ni.getState().toString() : "?");
                } catch (RemoteException e) {
                }
            }
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL, options);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void systemReady() {
        loadGlobalProxy();
        synchronized (this) {
            this.mSystemReady = true;
            if (this.mInitialBroadcast != null) {
                this.mContext.sendStickyBroadcastAsUser(this.mInitialBroadcast, UserHandle.ALL);
                this.mInitialBroadcast = null;
            }
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
        updateLockdownVpn();
        log("Init IConnectivityServiceExt class");
        this.mIcsExt = (IConnectivityServiceExt) MPlugin.createInstance(IConnectivityServiceExt.class.getName(), this.mContext);
        if (this.mIcsExt == null) {
            log("Get IConnectivityServiceExt fail");
        } else {
            this.mIcsExt.init(this.mContext);
        }
        log("End MPlugin createInstance");
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_pre_sim_wo_bal_support", "0"))) {
            try {
                log("[NetworkHttpMonitor] allocating memory for new variable");
                mNetworkHttpMonitor = new NetworkHttpMonitor(this.mContext, this.mNetd);
            } catch (Exception e) {
                log("[NetworkHttpMonitor] unable to create the new variable");
                e.printStackTrace();
            }
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(30));
        this.mHandler.sendMessage(this.mHandler.obtainMessage(25));
        this.mPermissionMonitor.startMonitoring();
    }

    private void setupDataActivityTracking(NetworkAgentInfo networkAgent) {
        int timeout;
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        if (networkAgent.networkCapabilities.hasTransport(0)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", 10);
            type = 0;
        } else if (networkAgent.networkCapabilities.hasTransport(1)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", 15);
            type = 1;
        } else {
            timeout = 0;
        }
        if (timeout > 0 && iface != null && type != -1) {
            try {
                this.mNetd.addIdleTimer(iface, timeout, type);
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
        if (caps.hasTransport(0) || caps.hasTransport(1)) {
            try {
                this.mNetd.removeIdleTimer(iface);
            } catch (Exception e) {
                loge("Exception in removeDataActivityTracking " + e);
            }
        }
    }

    private void updateMtu(LinkProperties newLp, LinkProperties oldLp) {
        String iface = newLp.getInterfaceName();
        int mtu = newLp.getMtu();
        if (oldLp != null && newLp.isIdenticalMtu(oldLp)) {
            if (VDBG) {
                log("identical MTU - not setting");
            }
        } else if (!LinkProperties.isValidMtu(mtu, newLp.hasGlobalIPv6Address())) {
            if (mtu != 0) {
                loge("Unexpected mtu value: " + mtu + ", " + iface);
            }
        } else if (TextUtils.isEmpty(iface)) {
            loge("Setting MTU size with null iface.");
        } else {
            try {
                if (VDBG) {
                    log("Setting MTU size: " + iface + ", " + mtu);
                }
                this.mNetd.setMtu(iface, mtu);
            } catch (Exception e) {
                Slog.e(TAG, "exception in setMtu()" + e);
            }
        }
    }

    protected int getDefaultTcpRwnd() {
        return SystemProperties.getInt(DEFAULT_TCP_RWND_KEY, 0);
    }

    private void updateTcpBufferSizes(NetworkAgentInfo nai) {
        if (isDefaultNetwork(nai)) {
            String tcpBufferSizes = nai.linkProperties.getTcpBufferSizes();
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
                    if (VDBG) {
                        Slog.d(TAG, "Setting tx/rx TCP buffers to " + tcpBufferSizes);
                    }
                    String prefix = "/sys/kernel/ipv4/tcp_";
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_min", values[0]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_def", values[1]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_max", values[2]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_min", values[3]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_def", values[4]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_max", values[5]);
                    this.mCurrentTcpBufferSizes = tcpBufferSizes;
                } catch (IOException e) {
                    loge("Can't set TCP buffer sizes:" + e);
                }
                Integer rwndValue = Integer.valueOf(Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", getDefaultTcpRwnd()));
                String sysctlKey = "sys.sysctl.tcp_def_init_rwnd";
                if (rwndValue.intValue() != 0) {
                    SystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
                }
            }
        }
    }

    private void flushVmDnsCache() {
        Intent intent = new Intent("android.intent.action.CLEAR_DNS_CACHE");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int getRestoreDefaultNetworkDelay(int networkType) {
        String restoreDefaultNetworkDelayStr = SystemProperties.get(NETWORK_RESTORE_DELAY_PROP_NAME);
        if (!(restoreDefaultNetworkDelayStr == null || restoreDefaultNetworkDelayStr.length() == 0)) {
            try {
                return Integer.parseInt(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        int ret = 60000;
        if (networkType <= 49 && this.mNetConfigs[networkType] != null) {
            ret = this.mNetConfigs[networkType].restoreTime;
        }
        return ret;
    }

    private boolean argsContain(String[] args, String target) {
        for (String arg : args) {
            if (arg.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private void dumpNetworkDiagnostics(IndentingPrintWriter pw) {
        List<NetworkDiagnostics> netDiags = new ArrayList();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            netDiags.add(new NetworkDiagnostics(nai.network, new LinkProperties(nai.linkProperties), 5000));
        }
        for (NetworkDiagnostics netDiag : netDiags) {
            pw.println();
            netDiag.waitForMeasurements();
            netDiag.dump(pw);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ConnectivityService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (argsContain(args, "--diag")) {
            dumpNetworkDiagnostics(pw);
        } else {
            int i;
            pw.print("NetworkFactories for:");
            for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
                pw.print(" " + nfi.name);
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
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                pw.println(nai.toString());
                pw.increaseIndent();
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(nai.numForegroundNetworkRequests());
                objArr[1] = Integer.valueOf(nai.numNetworkRequests() - nai.numRequestNetworkRequests());
                objArr[2] = Integer.valueOf(nai.numBackgroundNetworkRequests());
                objArr[3] = Integer.valueOf(nai.numNetworkRequests());
                pw.println(String.format("Requests: REQUEST:%d LISTEN:%d BACKGROUND_REQUEST:%d total:%d", objArr));
                pw.increaseIndent();
                for (i = 0; i < nai.numNetworkRequests(); i++) {
                    pw.println(nai.requestAt(i).toString());
                }
                pw.decreaseIndent();
                pw.println("Lingered:");
                pw.increaseIndent();
                nai.dumpLingerTimers(pw);
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Metered Interfaces:");
            pw.increaseIndent();
            for (String value : this.mMeteredIfaces) {
                pw.println(value);
            }
            pw.decreaseIndent();
            pw.println();
            pw.print("Restrict background: ");
            pw.println(this.mRestrictBackground);
            pw.println();
            pw.println("Status for known UIDs:");
            pw.increaseIndent();
            int size = this.mUidRules.size();
            for (i = 0; i < size; i++) {
                int uid = this.mUidRules.keyAt(i);
                pw.print("UID=");
                pw.print(uid);
                int uidRules = this.mUidRules.get(uid, 0);
                pw.print(" rules=");
                pw.print(NetworkPolicyManager.uidRulesToString(uidRules));
                pw.println();
            }
            pw.println();
            pw.decreaseIndent();
            pw.println("Network Requests:");
            pw.increaseIndent();
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                pw.println(nri.toString());
            }
            pw.println();
            pw.decreaseIndent();
            this.mLegacyTypeTracker.dump(pw);
            synchronized (this) {
                pw.print("mNetTransitionWakeLock: currently " + (this.mNetTransitionWakeLock.isHeld() ? IElsaManager.EMPTY_PACKAGE : "not ") + "held");
                if (TextUtils.isEmpty(this.mNetTransitionWakeLockCausedBy)) {
                    pw.println(", last requested never");
                } else {
                    pw.println(", last requested for " + this.mNetTransitionWakeLockCausedBy);
                }
            }
            pw.println();
            this.mTethering.dump(fd, pw, args);
            pw.println();
            this.mKeepaliveTracker.dump(pw);
            pw.println();
            dumpAvoidBadWifiSettings(pw);
            pw.println();
            if (this.mInetLog != null && this.mInetLog.size() > 0) {
                pw.println();
                pw.println("Inet condition reports:");
                pw.increaseIndent();
                for (i = 0; i < this.mInetLog.size(); i++) {
                    pw.println(this.mInetLog.get(i));
                }
                pw.decreaseIndent();
            }
            if (!argsContain(args, "--short")) {
                pw.println();
                synchronized (this.mValidationLogs) {
                    pw.println("mValidationLogs (most recent first):");
                    for (ValidationLog p : this.mValidationLogs) {
                        pw.println(p.mNetwork + " - " + p.mNetworkExtraInfo);
                        pw.increaseIndent();
                        p.mLog.dump(fd, pw, args);
                        pw.decreaseIndent();
                    }
                }
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
            }
        }
    }

    private boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
        if (nai.network == null) {
            return false;
        }
        NetworkAgentInfo officialNai = getNetworkAgentInfoForNetwork(nai.network);
        if (officialNai != null && officialNai.equals(nai)) {
            return true;
        }
        if (officialNai != null || VDBG) {
            loge(((String) sMagicDecoderRing.get(what, Integer.toString(what))) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        }
        return false;
    }

    private void updateLingerState(NetworkAgentInfo nai, long now) {
        nai.updateLingerTimer();
        if (nai.isLingering() && nai.numForegroundNetworkRequests() > 0) {
            log("Unlingering " + nai.name());
            nai.unlinger();
            logNetworkEvent(nai, 6);
        } else if (unneeded(nai, UnneededFor.LINGER) && nai.getLingerExpiry() > 0) {
            int lingerTime = (int) (nai.getLingerExpiry() - now);
            log("Lingering " + nai.name() + " for " + lingerTime + "ms");
            nai.linger();
            logNetworkEvent(nai, 5);
            notifyNetworkCallbacks(nai, 524291, lingerTime);
        }
    }

    private void handleAsyncChannelHalfConnect(Message msg) {
        AsyncChannel ac = msg.obj;
        NetworkAgentInfo nai;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                if (VDBG) {
                    log("NetworkFactory connected");
                }
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (!nri.request.isListen()) {
                        int currentScore;
                        nai = getNetworkByRequestId(nri.request.requestId);
                        if (nai != null) {
                            currentScore = nai.getCurrentScore();
                        } else {
                            currentScore = 0;
                        }
                        ac.sendMessage(536576, currentScore, 0, nri.request);
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
                ((NetworkAgentInfo) this.mNetworkAgentInfos.get(msg.replyTo)).asyncChannel.sendMessage(69633);
                return;
            }
            loge("Error connecting NetworkAgent");
            nai = (NetworkAgentInfo) this.mNetworkAgentInfos.remove(msg.replyTo);
            if (nai != null) {
                boolean wasDefault = isDefaultNetwork(nai);
                synchronized (this.mNetworkForNetId) {
                    this.mNetworkForNetId.remove(nai.network.netId);
                    this.mNetIdInUse.delete(nai.network.netId);
                }
                this.mLegacyTypeTracker.remove(nai, wasDefault);
            }
        }
    }

    private void handleAsyncChannelDisconnected(Message msg) {
        NetworkAgentInfo nai = (NetworkAgentInfo) this.mNetworkAgentInfos.get(msg.replyTo);
        if (nai != null) {
            log(nai.name() + " got DISCONNECTED, was satisfying " + nai.numNetworkRequests());
            if (!(nai == null || nai.networkInfo == null || !this.hasWifiAssistant)) {
                boolean changedNetworkIsCellular = nai.networkCapabilities.hasTransport(0);
                String extraStr = nai.networkInfo.getExtraInfo();
                boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                boolean isMmsApn = nai.networkCapabilities.hasCapability(0);
                if (!(isImsApn || isMmsApn || !changedNetworkIsCellular)) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ handleAsyncChannelDisconnected CELLULAR DISCONNECTED! update DataScore to wifi!");
                    }
                    sendBroadcastDataScore(10);
                }
            }
            if (nai.networkInfo.isConnected()) {
                nai.networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            }
            boolean wasDefault = isDefaultNetwork(nai);
            if (wasDefault) {
                this.mDefaultInetConditionPublished = 0;
                logDefaultNetworkEvent(null, nai);
            }
            notifyIfacesChangedForNetworkStats();
            notifyNetworkCallbacks(nai, 524292);
            this.mKeepaliveTracker.handleStopAllKeepalives(nai, -20);
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_DISCONNECTED);
            this.mNetworkAgentInfos.remove(msg.replyTo);
            updateClat(null, nai.linkProperties, nai);
            synchronized (this.mNetworkForNetId) {
                this.mNetworkForNetId.remove(nai.network.netId);
            }
            if (this.mIcsExt == null || nai.networkInfo.getType() == 1) {
            }
            if (this.mIcsExt == null) {
                log("mIcsExt is null");
            }
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                NetworkRequest request = nai.requestAt(i);
                NetworkAgentInfo currentNetwork = null;
                if (request != null) {
                    currentNetwork = getNetworkByRequestId(request.requestId);
                }
                if (currentNetwork != null && currentNetwork.network.netId == nai.network.netId) {
                    synchronized (this.mNetworkForRequestId) {
                        this.mNetworkForRequestId.remove(request.requestId);
                    }
                    sendUpdatedScoreToFactories(request, 0);
                }
            }
            nai.clearLingerState();
            if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
                removeDataActivityTracking(nai);
                notifyLockdownVpn(nai);
                requestNetworkTransitionWakelock(nai.name());
            }
            this.mLegacyTypeTracker.remove(nai, wasDefault);
            rematchAllNetworksAndRequests(null, 0);
            this.mLingerMonitor.noteDisconnect(nai);
            if (nai.created) {
                try {
                    this.mNetd.removeNetwork(nai.network.netId);
                } catch (Exception e) {
                    loge("Exception removing network: " + e);
                }
            }
            synchronized (this.mNetworkForNetId) {
                this.mNetIdInUse.delete(nai.network.netId);
            }
            return;
        }
        NetworkFactoryInfo nfi = (NetworkFactoryInfo) this.mNetworkFactoryInfos.remove(msg.replyTo);
        if (nfi != null) {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(PendingIntent pendingIntent) {
        Intent intent = pendingIntent.getIntent();
        for (Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            PendingIntent existingPendingIntent = ((NetworkRequestInfo) entry.getValue()).mPendingIntent;
            if (existingPendingIntent != null && existingPendingIntent.getIntent().filterEquals(intent)) {
                return (NetworkRequestInfo) entry.getValue();
            }
        }
        return null;
    }

    private void handleRegisterNetworkRequestWithIntent(Message msg) {
        NetworkRequestInfo nri = msg.obj;
        NetworkRequestInfo existingRequest = findExistingNetworkRequestInfo(nri.mPendingIntent);
        if (existingRequest != null) {
            log("Replacing " + existingRequest.request + " with " + nri.request + " because their intents matched.");
            handleReleaseNetworkRequest(existingRequest.request, getCallingUid());
        }
        handleRegisterNetworkRequest(nri);
    }

    private void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        synchronized (this.mRequestLock) {
            this.mNetworkRequests.put(nri.request, nri);
        }
        this.mNetworkRequestInfoLogs.log("REGISTER " + nri);
        if (nri.request.isListen()) {
            for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && network.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(network, "REGISTER", nri.request);
                }
            }
        }
        rematchAllNetworksAndRequests(null, 0);
        if (nri.request.isRequest() && this.mNetworkForRequestId.get(nri.request.requestId) == null) {
            sendUpdatedScoreToFactories(nri.request, 0);
        }
    }

    private void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid);
        }
    }

    private boolean unneeded(NetworkAgentInfo nai, UnneededFor reason) {
        int numRequests;
        switch (m8xd7b145e2()[reason.ordinal()]) {
            case 1:
                numRequests = nai.numForegroundNetworkRequests();
                break;
            case 2:
                numRequests = nai.numRequestNetworkRequests();
                break;
            default:
                Slog.e(TAG, "Invalid reason. Cannot happen.");
                return true;
        }
        if (!nai.everConnected || nai.isVPN() || nai.isLingering() || numRequests > 0) {
            return false;
        }
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            if (!(reason == UnneededFor.LINGER && nri.request.isBackgroundRequest()) && nri.request.isRequest() && nai.satisfies(nri.request)) {
                if (nai.isSatisfyingRequest(nri.request.requestId) || (getNetworkByRequestId(nri.request.requestId) != null && getNetworkByRequestId(nri.request.requestId).getCurrentScore() < nai.getCurrentScoreAsValidated())) {
                    return false;
                }
            }
        }
        return true;
    }

    private NetworkRequestInfo getNriForAppRequest(NetworkRequest request, int callingUid, String requestedOperation) {
        NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get(request);
        if (nri == null || 1000 == callingUid || nri.mUid == callingUid) {
            return nri;
        }
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(callingUid);
        objArr[1] = requestedOperation;
        objArr[2] = nri;
        log(String.format("UID %d attempted to %s for unowned request %s", objArr));
        return null;
    }

    private void handleRequestCallbackUpdate(NetworkRequest request, int callingUid, String description, int callbackType) {
        NetworkRequestInfo nri = getNriForAppRequest(request, callingUid, description);
        if (nri != null) {
            NetworkAgentInfo nai = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
            if (nai != null) {
                callCallbackForRequest(nri, nai, callbackType, 0);
            }
        }
    }

    private void handleRequestLinkProperties(NetworkRequest request, int callingUid) {
        handleRequestCallbackUpdate(request, callingUid, "request LinkProperties", 524295);
    }

    private void handleRequestNetworkCapabilities(NetworkRequest request, int callingUid) {
        handleRequestCallbackUpdate(request, callingUid, "request NetworkCapabilities", 524294);
    }

    private void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequestInfo nri = getNriForAppRequest(request, callingUid, "release NetworkRequest");
        if (nri != null) {
            if (VDBG || nri.request.isRequest()) {
                log("releasing " + request);
            }
            nri.unlinkDeathRecipient();
            synchronized (this.mRequestLock) {
                this.mNetworkRequests.remove(request);
            }
            synchronized (this.mUidToNetworkRequestCount) {
                int requests = this.mUidToNetworkRequestCount.get(nri.mUid, 0);
                if (requests < 1) {
                    Slog.e(TAG, "BUG: too small request count " + requests + " for UID " + nri.mUid);
                } else if (requests == 1) {
                    this.mUidToNetworkRequestCount.removeAt(this.mUidToNetworkRequestCount.indexOfKey(nri.mUid));
                } else {
                    this.mUidToNetworkRequestCount.put(nri.mUid, requests - 1);
                }
            }
            this.mNetworkRequestInfoLogs.log("RELEASE " + nri);
            NetworkAgentInfo nai;
            if (nri.request.isRequest()) {
                boolean wasKept = false;
                nai = getNetworkByRequestId(nri.request.requestId);
                if (nai != null) {
                    boolean wasBackgroundNetwork = nai.isBackgroundNetwork();
                    nai.removeRequest(nri.request.requestId);
                    if (VDBG) {
                        log(" Removing from current network " + nai.name() + ", leaving " + nai.numNetworkRequests() + " requests.");
                    }
                    updateLingerState(nai, SystemClock.elapsedRealtime());
                    if (unneeded(nai, UnneededFor.TEARDOWN)) {
                        log("no live requests for " + nai.name() + "; disconnecting");
                        teardownUnneededNetwork(nai);
                    } else {
                        wasKept = true;
                    }
                    synchronized (this.mNetworkForRequestId) {
                        this.mNetworkForRequestId.remove(nri.request.requestId);
                    }
                    if (!wasBackgroundNetwork && nai.isBackgroundNetwork()) {
                        updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                    }
                }
                for (NetworkAgentInfo otherNai : this.mNetworkAgentInfos.values()) {
                    if (otherNai.isSatisfyingRequest(nri.request.requestId) && otherNai != nai) {
                        Slog.e(TAG, "Request " + nri.request + " satisfied by " + otherNai.name() + ", but mNetworkAgentInfos says " + (nai != null ? nai.name() : "null"));
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
            } else {
                for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                    nai2.removeRequest(nri.request.requestId);
                    if (nri.request.networkCapabilities.hasSignalStrength() && nai2.satisfiesImmutableCapabilitiesOf(nri.request)) {
                        updateSignalStrengthThresholds(nai2, "RELEASE", nri.request);
                    }
                }
            }
            callCallbackForRequest(nri, null, 524296, 0);
        }
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        int i;
        int i2 = 1;
        enforceConnectivityInternalPermission();
        InternalHandler internalHandler = this.mHandler;
        InternalHandler internalHandler2 = this.mHandler;
        if (accept) {
            i = 1;
        } else {
            i = 0;
        }
        if (!always) {
            i2 = 0;
        }
        internalHandler.sendMessage(internalHandler2.obtainMessage(28, i, i2, network));
    }

    public void setAvoidUnvalidated(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(35, network));
    }

    private void handleSetAcceptUnvalidated(Network network, boolean accept, boolean always) {
        log("handleSetAcceptUnvalidated network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated) {
            if (!nai.networkMisc.explicitlySelected) {
                Slog.e(TAG, "BUG: setAcceptUnvalidated non non-explicitly selected network");
            }
            if (accept != nai.networkMisc.acceptUnvalidated) {
                int oldScore = nai.getCurrentScore();
                nai.networkMisc.acceptUnvalidated = accept;
                rematchAllNetworksAndRequests(nai, oldScore);
                sendUpdatedScoreToFactories(nai);
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, accept ? 1 : 0);
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleSetAvoidUnvalidated(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (!(nai == null || nai.lastValidated || nai.avoidUnvalidated)) {
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
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(29, nai.network), 8000);
    }

    public boolean avoidBadWifi() {
        return this.mAvoidBadWifiTracker.currentValue();
    }

    private void rematchForAvoidBadWifiUpdate() {
        rematchAllNetworksAndRequests(null, 0);
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.networkCapabilities.hasTransport(1)) {
                sendUpdatedScoreToFactories(nai);
            }
        }
    }

    private void dumpAvoidBadWifiSettings(IndentingPrintWriter pw) {
        boolean configRestrict = this.mAvoidBadWifiTracker.configRestrictsAvoidBadWifi();
        if (configRestrict) {
            String description;
            pw.println("Bad Wi-Fi avoidance: " + avoidBadWifi());
            pw.increaseIndent();
            pw.println("Config restrict:   " + configRestrict);
            String value = this.mAvoidBadWifiTracker.getSettingsValue();
            if ("0".equals(value)) {
                description = "get stuck";
            } else if (value == null) {
                description = "prompt";
            } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(value)) {
                description = "avoid";
            } else {
                description = value + " (?)";
            }
            pw.println("User setting:      " + description);
            pw.println("Network overrides:");
            pw.increaseIndent();
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                if (nai.avoidUnvalidated) {
                    pw.println(nai.name());
                }
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
            return;
        }
        pw.println("Bad Wi-Fi avoidance: unrestricted");
    }

    private void showValidationNotification(NetworkAgentInfo nai, NotificationType type) {
        String action;
        switch (m9x28b55b5a()[type.ordinal()]) {
            case 1:
                action = "android.net.conn.PROMPT_LOST_VALIDATION";
                break;
            case 2:
                action = "android.net.conn.PROMPT_UNVALIDATED";
                break;
            default:
                Slog.e(TAG, "Unknown notification type " + type);
                return;
        }
        Intent intent = new Intent(action);
        intent.setData(Uri.fromParts("netId", Integer.toString(nai.network.netId), null));
        intent.addFlags(268435456);
        intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
        this.mNotifier.showNotification(nai.network.netId, type, nai, null, PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, UserHandle.CURRENT), true);
    }

    private void handlePromptUnvalidated(Network network) {
        if (VDBG) {
            log("handlePromptUnvalidated " + network);
        }
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated && !nai.everCaptivePortalDetected && nai.networkMisc.explicitlySelected && !nai.networkMisc.acceptUnvalidated) {
            if (nai.networkInfo == null || nai.networkInfo.getType() != 1) {
                showValidationNotification(nai, NotificationType.NO_INTERNET);
                return;
            }
            if (VDBG) {
                log("handlePromptUnvalidated WiFi do not show NO_INTERNET notification");
            }
        }
    }

    private void handleNetworkUnvalidated(NetworkAgentInfo nai) {
        NetworkCapabilities nc = nai.networkCapabilities;
        log("handleNetworkUnvalidated " + nai.name() + " cap=" + nc);
        if (nc.hasTransport(1) && this.mAvoidBadWifiTracker.shouldNotifyWifiUnvalidated()) {
            showValidationNotification(nai, NotificationType.LOST_INTERNET);
        }
    }

    public List<WifiDevice> getTetherConnectedSta() {
        if (isTetheringSupported()) {
            return this.mTethering.getTetherConnectedSta();
        }
        return null;
    }

    public int tether(String iface) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (isTetheringSupported()) {
            return this.mTethering.tether(iface);
        }
        return 3;
    }

    public int untether(String iface) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
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

    public int setUsbTethering(boolean enable) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
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

    public boolean isTetheringSupported() {
        boolean z = true;
        enforceTetherAccessPermission();
        boolean tetherEnabledInSettings = Global.getInt(this.mContext.getContentResolver(), "tether_supported", SystemProperties.get("ro.tether.denied").equals("true") ? 0 : 1) != 0 ? !this.mUserManager.hasUserRestriction("no_config_tethering") : false;
        if (!tetherEnabledInSettings || !this.mUserManager.isAdminUser()) {
            return false;
        }
        if (this.mTethering.getTetherableUsbRegexs().length == 0 && this.mTethering.getTetherableWifiRegexs().length == 0 && this.mTethering.getTetherableBluetoothRegexs().length == 0) {
            return false;
        }
        if (this.mTethering.getUpstreamIfaceTypes().length == 0) {
            z = false;
        }
        return z;
    }

    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (isTetheringSupported()) {
            this.mTethering.startTethering(type, receiver, showProvisioningUi);
        } else {
            receiver.send(3, null);
        }
    }

    public void stopTethering(int type) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        this.mTethering.stopTethering(type);
    }

    private void requestNetworkTransitionWakelock(String forWhom) {
        Throwable th;
        synchronized (this) {
            try {
                if (this.mNetTransitionWakeLock.isHeld()) {
                    return;
                }
                int serialNum = this.mNetTransitionWakeLockSerialNumber + 1;
                this.mNetTransitionWakeLockSerialNumber = serialNum;
                try {
                    this.mNetTransitionWakeLock.acquire();
                    this.mNetTransitionWakeLockCausedBy = forWhom;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(24, serialNum, 0), (long) this.mNetTransitionWakeLockTimeout);
                } catch (Throwable th2) {
                    th = th2;
                    int i = serialNum;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            reportNetworkConnectivity(nai.network, percentage > 50);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0018, code:
            return;
     */
    /* JADX WARNING: Missing block: B:32:0x008a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        NetworkAgentInfo nai;
        enforceAccessPermission();
        enforceInternetPermission();
        if (network == null) {
            nai = getDefaultNetwork();
        } else {
            nai = getNetworkAgentInfoForNetwork(network);
        }
        if (nai != null && nai.networkInfo.getState() != State.DISCONNECTING && nai.networkInfo.getState() != State.DISCONNECTED && hasConnectivity != nai.lastValidated) {
            int uid = Binder.getCallingUid();
            log("reportNetworkConnectivity(" + nai.network.netId + ", " + hasConnectivity + ") by " + uid);
            synchronized (nai) {
                if (!nai.everConnected) {
                } else if (isNetworkWithLinkPropertiesBlocked(nai.linkProperties, uid, false)) {
                } else if (!(nai.networkInfo == null || nai.networkInfo.getType() == 1)) {
                    nai.networkMonitor.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, uid);
                }
            }
        }
    }

    private ProxyInfo getDefaultProxy() {
        ProxyInfo ret;
        synchronized (this.mProxyLock) {
            ret = this.mGlobalProxy;
            if (ret == null && !this.mDefaultProxyDisabled) {
                ret = this.mDefaultProxy;
            }
        }
        return ret;
    }

    public ProxyInfo getProxyForNetwork(Network network) {
        if (network == null) {
            return getDefaultProxy();
        }
        ProxyInfo globalProxy = getGlobalProxy();
        if (globalProxy != null) {
            return globalProxy;
        }
        if (!NetworkUtils.queryUserAccess(Binder.getCallingUid(), network.netId)) {
            return null;
        }
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            ProxyInfo proxyInfo = nai.linkProperties.getHttpProxy();
            if (proxyInfo == null) {
                return null;
            }
            ProxyInfo proxyInfo2 = new ProxyInfo(proxyInfo);
            return proxyInfo2;
        }
    }

    private ProxyInfo canonicalizeProxyInfo(ProxyInfo proxy) {
        if (proxy == null || !TextUtils.isEmpty(proxy.getHost())) {
            return proxy;
        }
        if (proxy.getPacFileUrl() == null || Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            return null;
        }
        return proxy;
    }

    private boolean proxyInfoEqual(ProxyInfo a, ProxyInfo b) {
        a = canonicalizeProxyInfo(a);
        b = canonicalizeProxyInfo(b);
        if (Objects.equals(a, b)) {
            return a != null ? Objects.equals(a.getHost(), b.getHost()) : true;
        } else {
            return false;
        }
    }

    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        synchronized (this.mProxyLock) {
            if (proxyProperties == this.mGlobalProxy) {
                return;
            }
            if (proxyProperties != null) {
                if (proxyProperties.equals(this.mGlobalProxy)) {
                    return;
                }
            }
            if (this.mGlobalProxy == null || !this.mGlobalProxy.equals(proxyProperties)) {
                String host = IElsaManager.EMPTY_PACKAGE;
                int port = 0;
                String exclList = IElsaManager.EMPTY_PACKAGE;
                String pacFileUrl = IElsaManager.EMPTY_PACKAGE;
                if (proxyProperties == null || (TextUtils.isEmpty(proxyProperties.getHost()) && Uri.EMPTY.equals(proxyProperties.getPacFileUrl()))) {
                    this.mGlobalProxy = null;
                } else if (proxyProperties.isValid()) {
                    this.mGlobalProxy = new ProxyInfo(proxyProperties);
                    host = this.mGlobalProxy.getHost();
                    port = this.mGlobalProxy.getPort();
                    exclList = this.mGlobalProxy.getExclusionListAsString();
                    if (!Uri.EMPTY.equals(proxyProperties.getPacFileUrl())) {
                        pacFileUrl = proxyProperties.getPacFileUrl().toString();
                    }
                } else {
                    log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
                    return;
                }
                ContentResolver res = this.mContext.getContentResolver();
                long token = Binder.clearCallingIdentity();
                try {
                    Global.putString(res, "global_http_proxy_host", host);
                    Global.putInt(res, "global_http_proxy_port", port);
                    Global.putString(res, "global_http_proxy_exclusion_list", exclList);
                    Global.putString(res, "global_proxy_pac_url", pacFileUrl);
                    Binder.restoreCallingIdentity(token);
                    if (this.mGlobalProxy == null) {
                        proxyProperties = this.mDefaultProxy;
                    }
                    sendProxyBroadcast(proxyProperties);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    private void loadGlobalProxy() {
        ContentResolver res = this.mContext.getContentResolver();
        String host = Global.getString(res, "global_http_proxy_host");
        int port = Global.getInt(res, "global_http_proxy_port", 0);
        String exclList = Global.getString(res, "global_http_proxy_exclusion_list");
        String pacFileUrl = Global.getString(res, "global_proxy_pac_url");
        if (!(TextUtils.isEmpty(host) && TextUtils.isEmpty(pacFileUrl))) {
            ProxyInfo proxyProperties;
            if (TextUtils.isEmpty(pacFileUrl)) {
                proxyProperties = new ProxyInfo(host, port, exclList);
            } else {
                proxyProperties = new ProxyInfo(pacFileUrl);
            }
            if (proxyProperties.isValid()) {
                synchronized (this.mProxyLock) {
                    this.mGlobalProxy = proxyProperties;
                }
            } else {
                log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
            }
        }
    }

    public ProxyInfo getGlobalProxy() {
        ProxyInfo proxyInfo;
        synchronized (this.mProxyLock) {
            proxyInfo = this.mGlobalProxy;
        }
        return proxyInfo;
    }

    /* JADX WARNING: Missing block: B:48:0x0090, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        synchronized (this.mProxyLock) {
            if (this.mDefaultProxy != null && this.mDefaultProxy.equals(proxy)) {
            } else if (this.mDefaultProxy == proxy) {
            } else {
                if (proxy != null) {
                    if (!proxy.isValid()) {
                        log("Invalid proxy properties, ignoring: " + proxy.toString());
                        return;
                    }
                }
                if (!(this.mGlobalProxy == null || proxy == null || Uri.EMPTY.equals(proxy.getPacFileUrl()))) {
                    if (proxy.getPacFileUrl().equals(this.mGlobalProxy.getPacFileUrl())) {
                        this.mGlobalProxy = proxy;
                        sendProxyBroadcast(this.mGlobalProxy);
                        return;
                    }
                }
                this.mDefaultProxy = proxy;
                if (this.mGlobalProxy != null) {
                } else if (!this.mDefaultProxyDisabled) {
                    sendProxyBroadcast(proxy);
                }
            }
        }
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        ProxyInfo oldProxyInfo = null;
        ProxyInfo newProxyInfo = newLp == null ? null : newLp.getHttpProxy();
        if (oldLp != null) {
            oldProxyInfo = oldLp.getHttpProxy();
        }
        if (!proxyInfoEqual(newProxyInfo, oldProxyInfo)) {
            sendProxyBroadcast(getDefaultProxy());
        }
    }

    private void handleDeprecatedGlobalHttpProxy() {
        String proxy = Global.getString(this.mContext.getContentResolver(), "http_proxy");
        if (!TextUtils.isEmpty(proxy)) {
            String[] data = proxy.split(":");
            if (data.length != 0) {
                String proxyHost = data[0];
                int proxyPort = 8080;
                if (data.length > 1) {
                    try {
                        proxyPort = Integer.parseInt(data[1]);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                setGlobalProxy(new ProxyInfo(data[0], proxyPort, IElsaManager.EMPTY_PACKAGE));
            }
        }
    }

    private void sendProxyBroadcast(ProxyInfo proxy) {
        if (proxy == null) {
            proxy = new ProxyInfo(IElsaManager.EMPTY_PACKAGE, 0, IElsaManager.EMPTY_PACKAGE);
        }
        if (!this.mPacManager.setCurrentProxyScriptUrl(proxy)) {
            log("sending Proxy Broadcast for " + proxy);
            Intent intent = new Intent("android.intent.action.PROXY_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("android.intent.extra.PROXY_INFO", proxy);
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    private static <T> T checkNotNull(T value, String message) {
        if (value != null) {
            return value;
        }
        throw new NullPointerException(message);
    }

    public boolean prepareVpn(String oldPackage, String newPackage, int userId) {
        enforceCrossUserPermission(userId);
        throwIfLockdownEnabled();
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                boolean prepare = vpn.prepare(oldPackage, newPackage);
                return prepare;
            }
            return false;
        }
    }

    public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                vpn.setPackageAuthorization(packageName, authorized);
            }
        }
    }

    public ParcelFileDescriptor establishVpn(VpnConfig config) {
        ParcelFileDescriptor establish;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            establish = ((Vpn) this.mVpns.get(user)).establish(config);
        }
        return establish;
    }

    public void startLegacyVpn(VpnProfile profile) {
        throwIfLockdownEnabled();
        LinkProperties egress = getActiveLinkProperties();
        if (egress == null) {
            throw new IllegalStateException("Missing active network connection");
        }
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            ((Vpn) this.mVpns.get(user)).startLegacyVpn(profile, this.mKeyStore, egress);
        }
    }

    public LegacyVpnInfo getLegacyVpnInfo(int userId) {
        LegacyVpnInfo legacyVpnInfo;
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            legacyVpnInfo = ((Vpn) this.mVpns.get(userId)).getLegacyVpnInfo();
        }
        return legacyVpnInfo;
    }

    public VpnInfo[] getAllVpnInfo() {
        enforceConnectivityInternalPermission();
        if (this.mLockdownEnabled) {
            return new VpnInfo[0];
        }
        VpnInfo[] vpnInfoArr;
        synchronized (this.mVpns) {
            List<VpnInfo> infoList = new ArrayList();
            for (int i = 0; i < this.mVpns.size(); i++) {
                VpnInfo info = createVpnInfo((Vpn) this.mVpns.valueAt(i));
                if (info != null) {
                    infoList.add(info);
                }
            }
            vpnInfoArr = (VpnInfo[]) infoList.toArray(new VpnInfo[infoList.size()]);
        }
        return vpnInfoArr;
    }

    private VpnInfo createVpnInfo(Vpn vpn) {
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
        } else if (underlyingNetworks.length > 0) {
            LinkProperties linkProperties = getLinkProperties(underlyingNetworks[0]);
            if (linkProperties != null) {
                info.primaryUnderlyingIface = linkProperties.getInterfaceName();
            }
        }
        if (info.primaryUnderlyingIface == null) {
            info = null;
        }
        return info;
    }

    public VpnConfig getVpnConfig(int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                VpnConfig vpnConfig = vpn.getVpnConfig();
                return vpnConfig;
            }
            return null;
        }
    }

    public boolean updateLockdownVpn() {
        if (Binder.getCallingUid() != 1000) {
            Slog.w(TAG, "Lockdown VPN only available to AID_SYSTEM");
            return false;
        }
        this.mLockdownEnabled = LockdownVpnTracker.isEnabled();
        if (this.mLockdownEnabled) {
            String profileName = new String(this.mKeyStore.get("LOCKDOWN_VPN"));
            VpnProfile profile = VpnProfile.decode(profileName, this.mKeyStore.get("VPN_" + profileName));
            if (profile == null) {
                loge("Null profile name:" + profileName);
                this.mKeyStore.delete("LOCKDOWN_VPN");
                setLockdownTracker(null);
                return true;
            }
            int user = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(user);
                if (vpn == null) {
                    Slog.w(TAG, "VPN for user " + user + " not ready yet. Skipping lockdown");
                    return false;
                }
                setLockdownTracker(new LockdownVpnTracker(this.mContext, this.mNetd, this, vpn, profile));
            }
        } else {
            setLockdownTracker(null);
        }
        return true;
    }

    private void setLockdownTracker(LockdownVpnTracker tracker) {
        LockdownVpnTracker existing = this.mLockdownTracker;
        this.mLockdownTracker = null;
        if (existing != null) {
            existing.shutdown();
        }
        if (tracker != null) {
            try {
                this.mNetd.setFirewallEnabled(true);
                this.mNetd.setFirewallInterfaceRule("lo", true);
                this.mLockdownTracker = tracker;
                this.mLockdownTracker.init();
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mNetd.setFirewallEnabled(false);
    }

    private void throwIfLockdownEnabled() {
        if (this.mLockdownEnabled) {
            throw new IllegalStateException("Unavailable in lockdown mode");
        }
    }

    private boolean startAlwaysOnVpn(int userId) {
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.e(TAG, "User " + userId + " has no Vpn configuration");
                return false;
            }
            boolean startAlwaysOnVpn = vpn.startAlwaysOnVpn();
            return startAlwaysOnVpn;
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        if (LockdownVpnTracker.isEnabled()) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.w(TAG, "User " + userId + " has no Vpn configuration");
                return false;
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown)) {
                return false;
            } else if (startAlwaysOnVpn(userId)) {
                vpn.saveAlwaysOnPackage();
                return true;
            } else {
                vpn.setAlwaysOnPackage(null, false);
                return false;
            }
        }
    }

    public String getAlwaysOnVpnPackage(int userId) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.w(TAG, "User " + userId + " has no Vpn configuration");
                return null;
            }
            String alwaysOnPackage = vpn.getAlwaysOnPackage();
            return alwaysOnPackage;
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        return -1;
    }

    private String getProvisioningUrlBaseFromFile() {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        FileReader fileReader = null;
        Configuration config = this.mContext.getResources().getConfiguration();
        try {
            FileReader fileReader2 = new FileReader(this.mProvisioningUrlFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileReader2);
                XmlUtils.beginDocument(parser, TAG_PROVISIONING_URLS);
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        if (fileReader2 != null) {
                            try {
                                fileReader2.close();
                            } catch (IOException e3) {
                            }
                        }
                        return null;
                    } else if (element.equals(TAG_PROVISIONING_URL)) {
                        String mcc = parser.getAttributeValue(null, ATTR_MCC);
                        if (mcc != null) {
                            try {
                                if (Integer.parseInt(mcc) == config.mcc) {
                                    String mnc = parser.getAttributeValue(null, ATTR_MNC);
                                    if (mnc != null && Integer.parseInt(mnc) == config.mnc) {
                                        parser.next();
                                        if (parser.getEventType() == 4) {
                                            String text = parser.getText();
                                            if (fileReader2 != null) {
                                                try {
                                                    fileReader2.close();
                                                } catch (IOException e4) {
                                                }
                                            }
                                            return text;
                                        }
                                    }
                                } else {
                                    continue;
                                }
                            } catch (NumberFormatException e5) {
                                loge("NumberFormatException in getProvisioningUrlBaseFromFile: " + e5);
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } catch (FileNotFoundException e6) {
                fileReader = fileReader2;
            } catch (XmlPullParserException e7) {
                e = e7;
                fileReader = fileReader2;
            } catch (IOException e8) {
                e2 = e8;
                fileReader = fileReader2;
            } catch (Throwable th2) {
                th = th2;
                fileReader = fileReader2;
            }
        } catch (FileNotFoundException e9) {
            try {
                loge("Carrier Provisioning Urls file not found");
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e10) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e11) {
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e12) {
            e = e12;
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e13) {
                }
            }
            return null;
        } catch (IOException e14) {
            e2 = e14;
            loge("I/O exception reading Carrier Provisioning Urls file: " + e2);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e15) {
                }
            }
            return null;
        }
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17039435);
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
        Object[] objArr = new Object[3];
        objArr[0] = this.mTelephonyManager.getSimSerialNumber();
        objArr[1] = this.mTelephonyManager.getDeviceId();
        objArr[2] = phoneNumber;
        return String.format(url, objArr);
    }

    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        enforceConnectivityInternalPermission();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mNotifier.setProvNotificationVisible(visible, DumpState.DUMP_INSTALLS + (networkType + 1), action);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceConnectivityInternalPermission();
        long ident = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", enable ? 1 : 0);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0051, code:
            if (r8.mUserManager.getUserInfo(r9).isPrimary() == false) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:18:0x0057, code:
            if (com.android.server.net.LockdownVpnTracker.isEnabled() == false) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:19:0x0059, code:
            updateLockdownVpn();
     */
    /* JADX WARNING: Missing block: B:20:0x005c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUserStart(int userId) {
        synchronized (this.mVpns) {
            if (((Vpn) this.mVpns.get(userId)) != null) {
                loge("Starting user already has a VPN");
                return;
            }
            Vpn userVpn = new Vpn(this.mHandler.getLooper(), this.mContext, this.mNetd, userId);
            this.mVpns.put(userId, userVpn);
            ContentResolver cr = this.mContext.getContentResolver();
            String alwaysOnPackage = Secure.getStringForUser(cr, "always_on_vpn_app", userId);
            boolean alwaysOnLockdown = Secure.getIntForUser(cr, "always_on_vpn_lockdown", 0, userId) != 0;
            if (alwaysOnPackage != null) {
                userVpn.setAlwaysOnPackage(alwaysOnPackage, alwaysOnLockdown);
            }
        }
    }

    private void onUserStop(int userId) {
        synchronized (this.mVpns) {
            Vpn userVpn = (Vpn) this.mVpns.get(userId);
            if (userVpn == null) {
                loge("Stopped user has no VPN");
                return;
            }
            userVpn.onUserStopped();
            this.mVpns.delete(userId);
        }
    }

    private void onUserAdded(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                ((Vpn) this.mVpns.valueAt(i)).onUserAdded(userId);
            }
        }
    }

    private void onUserRemoved(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                ((Vpn) this.mVpns.valueAt(i)).onUserRemoved(userId);
            }
        }
    }

    private void onUserUnlocked(int userId) {
        if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
            updateLockdownVpn();
        } else {
            startAlwaysOnVpn(userId);
        }
    }

    private void ensureNetworkRequestHasType(NetworkRequest request) {
        if (request.type == Type.NONE) {
            throw new IllegalArgumentException("All NetworkRequests in ConnectivityService must have a type");
        }
    }

    private void ensureRequestableCapabilities(NetworkCapabilities networkCapabilities) {
        String badCapability = networkCapabilities.describeFirstNonRequestableCapability();
        if (badCapability != null) {
            throw new IllegalArgumentException("Cannot request network with " + badCapability);
        }
    }

    private ArrayList<Integer> getSignalStrengthThresholds(NetworkAgentInfo nai) {
        SortedSet<Integer> thresholds = new TreeSet();
        synchronized (nai) {
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && nai.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    thresholds.add(Integer.valueOf(nri.request.networkCapabilities.getSignalStrength()));
                }
            }
        }
        return new ArrayList(thresholds);
    }

    private void updateSignalStrengthThresholds(NetworkAgentInfo nai, String reason, NetworkRequest request) {
        ArrayList<Integer> thresholdsArray = getSignalStrengthThresholds(nai);
        Bundle thresholds = new Bundle();
        thresholds.putIntegerArrayList("thresholds", thresholdsArray);
        if (VDBG || !"CONNECT".equals(reason)) {
            String detail;
            if (request == null || !request.networkCapabilities.hasSignalStrength()) {
                detail = reason;
            } else {
                detail = reason + " " + request.networkCapabilities.getSignalStrength();
            }
            Object[] objArr = new Object[3];
            objArr[0] = detail;
            objArr[1] = Arrays.toString(thresholdsArray.toArray());
            objArr[2] = nai.name();
            log(String.format("updateSignalStrengthThresholds: %s, sending %s to %s", objArr));
        }
        nai.asyncChannel.sendMessage(528398, 0, 0, thresholds);
    }

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        Type type;
        if (networkCapabilities == null) {
            type = Type.TRACK_DEFAULT;
        } else {
            type = Type.REQUEST;
        }
        if (type == Type.TRACK_DEFAULT) {
            networkCapabilities = new NetworkCapabilities(this.mDefaultRequest.networkCapabilities);
            enforceAccessPermission();
        } else {
            NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
            enforceNetworkRequestPermissions(networkCapabilities2);
            enforceMeteredApnPolicy(networkCapabilities2);
            networkCapabilities = networkCapabilities2;
        }
        ensureRequestableCapabilities(networkCapabilities);
        if (timeoutMs < 0 || timeoutMs > 6000000) {
            throw new IllegalArgumentException("Bad timeout specified");
        } else if ("*".equals(networkCapabilities.getNetworkSpecifier())) {
            throw new IllegalArgumentException("Invalid network specifier - must not be '*'");
        } else if (this.mIcsExt == null || !this.mIcsExt.ignoreRequest(networkCapabilities)) {
            NetworkRequest networkRequest = new NetworkRequest(networkCapabilities, legacyType, nextNetworkRequestId(), type);
            NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
            log("requestNetwork for " + nri);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(19, nri));
            if (timeoutMs > 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20, nri), (long) timeoutMs);
            }
            return networkRequest;
        } else {
            log("requestNetwork return null to ignore mms request for OP09");
            return null;
        }
    }

    private void enforceNetworkRequestPermissions(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasCapability(13)) {
            enforceChangePermission();
        } else {
            enforceConnectivityRestrictedNetworksPermission();
        }
    }

    public boolean requestBandwidthUpdate(Network network) {
        enforceAccessPermission();
        if (network == null) {
            return false;
        }
        NetworkAgentInfo nai;
        synchronized (this.mNetworkForNetId) {
            nai = (NetworkAgentInfo) this.mNetworkForNetId.get(network.netId);
        }
        if (nai == null) {
            return false;
        }
        nai.asyncChannel.sendMessage(528394);
        return true;
    }

    private boolean isSystem(int uid) {
        return uid < 10000;
    }

    private void enforceMeteredApnPolicy(NetworkCapabilities networkCapabilities) {
        int uid = Binder.getCallingUid();
        if (!(isSystem(uid) || networkCapabilities.hasCapability(11))) {
            synchronized (this.mRulesLock) {
                int uidRules = this.mUidRules.get(uid, 32);
            }
            if (this.mRestrictBackground && (uidRules & 1) == 0 && (uidRules & 2) == 0) {
                networkCapabilities.addCapability(11);
            }
        }
    }

    public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        enforceNetworkRequestPermissions(networkCapabilities2);
        enforceMeteredApnPolicy(networkCapabilities2);
        ensureRequestableCapabilities(networkCapabilities2);
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, -1, nextNetworkRequestId(), Type.REQUEST);
        NetworkRequestInfo nri = new NetworkRequestInfo(networkRequest, operation);
        log("pendingRequest for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(26, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(27, getCallingUid(), 0, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(27, getCallingUid(), 0, operation));
    }

    private boolean hasWifiNetworkListenPermission(NetworkCapabilities nc) {
        if (nc == null) {
            return false;
        }
        int[] transportTypes = nc.getTransportTypes();
        if (transportTypes.length != 1 || transportTypes[0] != 1) {
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
        if (!ConnectivityManager.checkChangePermission(this.mContext)) {
            nc.addCapability(18);
        }
        NetworkRequest networkRequest = new NetworkRequest(nc, -1, nextNetworkRequestId(), Type.LISTEN);
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
        if (VDBG) {
            log("listenForNetwork for " + nri);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, nri));
        return networkRequest;
    }

    public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        NetworkRequestInfo nri = new NetworkRequestInfo(new NetworkRequest(new NetworkCapabilities(networkCapabilities), -1, nextNetworkRequestId(), Type.LISTEN), operation);
        if (VDBG) {
            log("pendingListenForNetwork for " + nri);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, nri));
    }

    public void requestLinkProperties(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        if (networkRequest.type != Type.LISTEN) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(32, getCallingUid(), 0, networkRequest));
        }
    }

    public void requestNetworkCapabilities(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        if (networkRequest.type != Type.LISTEN) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(33, getCallingUid(), 0, networkRequest));
        }
    }

    public void releaseNetworkRequest(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(22, getCallingUid(), 0, networkRequest));
    }

    public void registerNetworkFactory(Messenger messenger, String name) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17, new NetworkFactoryInfo(name, messenger, new AsyncChannel())));
    }

    private void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, messenger));
    }

    private void handleUnregisterNetworkFactory(Messenger messenger) {
        NetworkFactoryInfo nfi = (NetworkFactoryInfo) this.mNetworkFactoryInfos.remove(messenger);
        if (nfi == null) {
            loge("Failed to find Messenger in unregisterNetworkFactory");
        } else {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private NetworkAgentInfo getDefaultNetwork() {
        return getNetworkByRequestId(this.mDefaultRequest.requestId);
    }

    private NetworkAgentInfo getNetworkByRequestId(int requestId) {
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForRequestId) {
            networkAgentInfo = (NetworkAgentInfo) this.mNetworkForRequestId.get(requestId);
        }
        return networkAgentInfo;
    }

    private boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork();
    }

    private boolean isDefaultRequest(NetworkRequestInfo nri) {
        return nri.request.requestId == this.mDefaultRequest.requestId;
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc) {
        enforceConnectivityInternalPermission();
        NetworkAgentInfo nai = new NetworkAgentInfo(messenger, new AsyncChannel(), new Network(reserveNetId()), new NetworkInfo(networkInfo), new LinkProperties(linkProperties), new NetworkCapabilities(networkCapabilities), currentScore, this.mContext, this.mTrackerHandler, new NetworkMisc(networkMisc), this.mDefaultRequest, this);
        synchronized (this) {
            nai.networkMonitor.systemReady = this.mSystemReady;
        }
        addValidationLogs(nai.networkMonitor.getValidationLogs(), nai.network, networkInfo.getExtraInfo());
        log("registerNetworkAgent " + nai);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, nai));
        return nai.network.netId;
    }

    private void handleRegisterNetworkAgent(NetworkAgentInfo na) {
        if (VDBG) {
            log("Got NetworkAgent Messenger");
        }
        this.mNetworkAgentInfos.put(na.messenger, na);
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.put(na.network.netId, na);
        }
        na.asyncChannel.connect(this.mContext, this.mTrackerHandler, na.messenger);
        NetworkInfo networkInfo = na.networkInfo;
        na.networkInfo = null;
        updateNetworkInfo(na, networkInfo);
    }

    private void updateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        LinkProperties newLp = networkAgent.linkProperties;
        int netId = networkAgent.network.netId;
        if (VDBG) {
            log("updateLinkProperties for " + networkAgent.name());
        }
        if (SDBG) {
            log("LinkProperties:" + newLp);
        }
        if (networkAgent.clatd != null) {
            networkAgent.clatd.fixupLinkProperties(oldLp);
        }
        updateInterfaces(newLp, oldLp, netId);
        updateMtu(newLp, oldLp);
        updateTcpBufferSizes(networkAgent);
        updateRoutes(newLp, oldLp, netId);
        updateDnses(newLp, oldLp, netId, networkAgent);
        updateClat(newLp, oldLp, networkAgent);
        if (isDefaultNetwork(networkAgent)) {
            handleApplyDefaultProxy(newLp.getHttpProxy());
        } else {
            updateProxy(newLp, oldLp, networkAgent);
        }
        if (!Objects.equals(newLp, oldLp)) {
            notifyIfacesChangedForNetworkStats();
            notifyNetworkCallbacks(networkAgent, 524295);
        }
        this.mKeepaliveTracker.handleCheckKeepalivesStillValid(networkAgent);
    }

    private void updateClat(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        boolean wasRunningClat = nai.clatd != null ? nai.clatd.isStarted() : false;
        boolean shouldRunClat = Nat464Xlat.requiresClat(nai);
        if (nai.networkCapabilities.hasCapability(4) || nai.networkCapabilities.hasCapability(10)) {
            shouldRunClat = false;
        }
        if (!wasRunningClat && shouldRunClat) {
            nai.clatd = new Nat464Xlat(this.mContext, this.mNetd, this.mTrackerHandler, nai);
            nai.clatd.start();
        } else if (wasRunningClat && !shouldRunClat) {
            nai.clatd.stop();
        }
    }

    private void updateInterfaces(LinkProperties newLp, LinkProperties oldLp, int netId) {
        CompareResult<String> interfaceDiff = new CompareResult();
        if (oldLp != null) {
            interfaceDiff = oldLp.compareAllInterfaceNames(newLp);
        } else if (newLp != null) {
            interfaceDiff.added = newLp.getAllInterfaceNames();
        }
        for (String iface : interfaceDiff.added) {
            try {
                log("Adding iface " + iface + " to network " + netId);
                this.mNetd.addInterfaceToNetwork(iface, netId);
            } catch (Exception e) {
                loge("Exception adding interface: " + e);
            }
        }
        for (String iface2 : interfaceDiff.removed) {
            try {
                log("Removing iface " + iface2 + " from network " + netId);
                this.mNetd.removeInterfaceFromNetwork(iface2, netId);
            } catch (Exception e2) {
                loge("Exception removing interface: " + e2);
            }
        }
    }

    private boolean updateRoutes(LinkProperties newLp, LinkProperties oldLp, int netId) {
        CompareResult<RouteInfo> routeDiff = new CompareResult();
        if (oldLp != null) {
            routeDiff = oldLp.compareAllRoutes(newLp);
        } else if (newLp != null) {
            routeDiff.added = newLp.getAllRoutes();
        }
        for (RouteInfo route : routeDiff.added) {
            if (!route.hasGateway()) {
                if (VDBG) {
                    log("Adding Route [" + route + "] to network " + netId);
                }
                try {
                    this.mNetd.addRoute(netId, route);
                } catch (Exception e) {
                    if ((route.getDestination().getAddress() instanceof Inet4Address) || VDBG) {
                        loge("Exception in addRoute for non-gateway: " + e);
                    }
                }
            }
        }
        for (RouteInfo route2 : routeDiff.added) {
            if (route2.hasGateway()) {
                if (VDBG) {
                    log("Adding Route [" + route2 + "] to network " + netId);
                }
                try {
                    this.mNetd.addRoute(netId, route2);
                } catch (Exception e2) {
                    if ((route2.getGateway() instanceof Inet4Address) || VDBG) {
                        loge("Exception in addRoute for gateway: " + e2);
                    }
                }
            }
        }
        for (RouteInfo route22 : routeDiff.removed) {
            if (VDBG) {
                log("Removing Route [" + route22 + "] from network " + netId);
            }
            try {
                this.mNetd.removeRoute(netId, route22);
            } catch (Exception e22) {
                loge("Exception in removeRoute: " + e22);
            }
        }
        if (routeDiff.added.isEmpty() && routeDiff.removed.isEmpty()) {
            return false;
        }
        return true;
    }

    private void updateDnses(LinkProperties newLp, LinkProperties oldLp, int netId, NetworkAgentInfo networkAgent) {
        if (oldLp == null || !newLp.isIdenticalDnses(oldLp)) {
            boolean isChineseOper;
            ArrayList<InetAddress> sortedDnses;
            Collection<InetAddress> dnses = newLp.getDnsServers();
            ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
            String mcc = colorOSTelephonyManager.getNetworkOperatorGemini(colorOSTelephonyManager.colorGetDataSubscription());
            if (TextUtils.isEmpty(mcc) || mcc.startsWith("460")) {
                isChineseOper = true;
            } else {
                log("isChineseOper:not Chinese operator!");
                isChineseOper = false;
            }
            if (isChineseOper && !IS_LAB_TEST) {
                InetAddress defaultDns;
                sortedDnses = new ArrayList();
                if (newLp.hasIPv4Address()) {
                    for (InetAddress ia : dnses) {
                        if (ia instanceof Inet4Address) {
                            sortedDnses.add(ia);
                        }
                    }
                    for (InetAddress ia2 : dnses) {
                        if (ia2 instanceof Inet6Address) {
                            sortedDnses.add(ia2);
                        }
                    }
                } else {
                    for (InetAddress ia22 : dnses) {
                        if (ia22 instanceof Inet6Address) {
                            sortedDnses.add(ia22);
                        }
                    }
                    for (InetAddress ia222 : dnses) {
                        if (ia222 instanceof Inet4Address) {
                            sortedDnses.add(ia222);
                        }
                    }
                }
                WifiRomUpdateHelper helper = new WifiRomUpdateHelper(this.mContext);
                String dns = "114.114.114.114";
                if (helper != null) {
                    dns = helper.getValue(WifiRomUpdateHelper.NETWORK_DEFAULT_DNS, "114.114.114.114");
                }
                try {
                    defaultDns = NetworkUtils.numericToInetAddress(dns);
                } catch (IllegalArgumentException e) {
                    loge("Error setting defaultDns using " + dns);
                    defaultDns = null;
                }
                if (!(defaultDns == null || sortedDnses.size() >= 2 || sortedDnses.contains(defaultDns))) {
                    sortedDnses.add(defaultDns);
                    synchronized (networkAgent) {
                        newLp.addDnsServer(defaultDns);
                    }
                }
                Object dnses2 = sortedDnses;
            } else if (!(IS_LAB_TEST || this.mDefaultDns == null || dnses2.size() >= 2 || dnses2.contains(this.mDefaultDns))) {
                Collection<InetAddress> dnses3 = new ArrayList(dnses2);
                dnses3.add(this.mDefaultDns);
                if (SDBG) {
                    log("add default dns: " + this.mDefaultDns.getHostAddress());
                    dnses2 = dnses3;
                } else {
                    dnses2 = dnses3;
                }
            }
            if (isOnlyIpv6Address(newLp.getAddresses())) {
                sortedDnses = new ArrayList();
                for (InetAddress ia2222 : dnses) {
                    if (ia2222 instanceof Inet6Address) {
                        sortedDnses.add(ia2222);
                    }
                }
                for (InetAddress ia22222 : dnses) {
                    if (ia22222 instanceof Inet4Address) {
                        sortedDnses.add(ia22222);
                    }
                }
                dnses2 = sortedDnses;
            }
            log("Setting Dns servers for network " + netId + " to " + dnses2);
            try {
                this.mNetd.setDnsConfigurationForNetwork(netId, NetworkUtils.makeStrings(dnses2), newLp.getDomains());
            } catch (Exception e2) {
                loge("Exception in setDnsConfigurationForNetwork: " + e2);
            }
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            if (defaultNai != null && defaultNai.network.netId == netId) {
                setDefaultDnsSystemProperties(dnses2);
            }
            flushVmDnsCache();
        }
    }

    private void setDefaultDnsSystemProperties(Collection<InetAddress> dnses) {
        int last = 0;
        for (InetAddress dns : dnses) {
            last++;
            SystemProperties.set("net.dns" + last, dns.getHostAddress());
        }
        for (int i = last + 1; i <= this.mNumDnsEntries; i++) {
            SystemProperties.set("net.dns" + i, IElsaManager.EMPTY_PACKAGE);
        }
        this.mNumDnsEntries = last;
    }

    private String getNetworkPermission(NetworkCapabilities nc) {
        if (!nc.hasCapability(13)) {
            return NetworkManagementService.PERMISSION_SYSTEM;
        }
        if (nc.hasCapability(18)) {
            return null;
        }
        return NetworkManagementService.PERMISSION_NETWORK;
    }

    private void updateCapabilities(int oldScore, NetworkAgentInfo nai, NetworkCapabilities networkCapabilities) {
        log("updateCapabilities cap: " + networkCapabilities);
        if (nai.everConnected && !nai.networkCapabilities.equalImmutableCapabilities(networkCapabilities)) {
            Slog.e(TAG, "BUG: " + nai + " changed immutable capabilities: " + nai.networkCapabilities + " -> " + networkCapabilities);
        }
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        if (nai.lastValidated) {
            networkCapabilities2.addCapability(16);
        } else {
            networkCapabilities2.removeCapability(16);
        }
        if (nai.lastCaptivePortalDetected) {
            networkCapabilities2.addCapability(17);
        } else {
            networkCapabilities2.removeCapability(17);
        }
        if (nai.isBackgroundNetwork()) {
            networkCapabilities2.removeCapability(18);
        } else {
            networkCapabilities2.addCapability(18);
        }
        if (!Objects.equals(nai.networkCapabilities, networkCapabilities2)) {
            String oldPermission = getNetworkPermission(nai.networkCapabilities);
            String newPermission = getNetworkPermission(networkCapabilities2);
            if (!(Objects.equals(oldPermission, newPermission) || !nai.created || nai.isVPN())) {
                try {
                    this.mNetd.setNetworkPermission(nai.network.netId, newPermission);
                } catch (RemoteException e) {
                    loge("Exception in setNetworkPermission: " + e);
                }
            }
            NetworkCapabilities prevNc = nai.networkCapabilities;
            synchronized (nai) {
                nai.networkCapabilities = networkCapabilities2;
            }
            if (nai.getCurrentScore() == oldScore && networkCapabilities2.equalRequestableCapabilities(prevNc)) {
                processListenRequests(nai, true);
            } else {
                rematchAllNetworksAndRequests(nai, oldScore);
                notifyNetworkCallbacks(nai, 524294);
            }
        }
    }

    private void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest nr = nai.requestAt(i);
            if (!nr.isListen()) {
                sendUpdatedScoreToFactories(nr, nai.getCurrentScore());
            }
        }
    }

    private void sendUpdatedScoreToFactories(NetworkRequest networkRequest, int score) {
        if (VDBG) {
            log("sending new Min Network Score(" + score + "): " + networkRequest.toString());
        }
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            nfi.asyncChannel.sendMessage(536576, score, 0, networkRequest);
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
        } catch (CanceledException e) {
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
            bundle.putParcelable(NetworkRequest.class.getSimpleName(), new NetworkRequest(nri.request));
            Message msg = Message.obtain();
            if (!(notificationType == 524293 || notificationType == 524296)) {
                bundle.putParcelable(Network.class.getSimpleName(), networkAgent.network);
            }
            switch (notificationType) {
                case 524291:
                    msg.arg1 = arg1;
                    break;
                case 524294:
                    bundle.putParcelable(NetworkCapabilities.class.getSimpleName(), new NetworkCapabilities(networkAgent.networkCapabilities));
                    break;
                case 524295:
                    bundle.putParcelable(LinkProperties.class.getSimpleName(), new LinkProperties(networkAgent.linkProperties));
                    break;
            }
            msg.what = notificationType;
            msg.setData(bundle);
            try {
                if (VDBG && (notificationType == 524290 || notificationType == 524292)) {
                    log("sending notification " + notifyTypeToName(notificationType) + " for " + nri.request);
                }
                nri.messenger.send(msg);
            } catch (RemoteException e) {
                loge("RemoteException caught trying to send a callback msg for " + nri.request);
            }
        }
    }

    private void teardownUnneededNetwork(NetworkAgentInfo nai) {
        if (nai.numRequestNetworkRequests() != 0) {
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                NetworkRequest nr = nai.requestAt(i);
                if (!nr.isListen()) {
                    loge("Dead network still had at least " + nr);
                    break;
                }
            }
        }
        try {
            nai.asyncChannel.disconnect();
        } catch (Exception ex) {
            loge("disconnect error: " + ex);
        }
    }

    private void handleLingerComplete(NetworkAgentInfo oldNetwork) {
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
        setupDataActivityTracking(newNetwork);
        try {
            this.mNetd.setDefaultNetId(newNetwork.network.netId);
        } catch (Exception e) {
            loge("Exception setting default network :" + e);
        }
        notifyLockdownVpn(newNetwork);
        handleApplyDefaultProxy(newNetwork.linkProperties.getHttpProxy());
        updateTcpBufferSizes(newNetwork);
        setDefaultDnsSystemProperties(newNetwork.linkProperties.getDnsServers());
    }

    private void processListenRequests(NetworkAgentInfo nai, boolean capabilitiesChanged) {
        NetworkRequest nr;
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            nr = nri.request;
            if (nr.isListen() && nai.isSatisfyingRequest(nr.requestId) && !nai.satisfies(nr)) {
                nai.removeRequest(nri.request.requestId);
                callCallbackForRequest(nri, nai, 524292, 0);
            }
        }
        if (capabilitiesChanged) {
            notifyNetworkCallbacks(nai, 524294);
        }
        for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
            nr = nri2.request;
            if (nr.isListen() && nai.satisfies(nr) && !nai.isSatisfyingRequest(nr.requestId)) {
                nai.addRequest(nr);
                notifyNetworkCallback(nai, nri2);
            }
        }
    }

    private void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks, long now) {
        if (newNetwork.everConnected) {
            String str;
            Object[] objArr;
            boolean keep = newNetwork.isVPN();
            boolean isNewDefault = false;
            NetworkAgentInfo oldDefaultNetwork = null;
            boolean wasBackgroundNetwork = newNetwork.isBackgroundNetwork();
            int score = newNetwork.getCurrentScore();
            if (VDBG) {
                log("rematching " + newNetwork.name());
            }
            ArrayList<NetworkAgentInfo> affectedNetworks = new ArrayList();
            ArrayList<NetworkRequestInfo> addedRequests = new ArrayList();
            NetworkCapabilities nc = newNetwork.networkCapabilities;
            if (VDBG) {
                log(" network has: " + nc);
            }
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (!nri.request.isListen()) {
                    NetworkAgentInfo currentNetwork = getNetworkByRequestId(nri.request.requestId);
                    boolean satisfies = newNetwork.satisfies(nri.request);
                    if (newNetwork == currentNetwork && satisfies) {
                        if (VDBG) {
                            log("Network " + newNetwork.name() + " was already satisfying" + " request " + nri.request.requestId + ". No change.");
                        }
                        keep = true;
                    } else {
                        if (VDBG) {
                            log("  checking if request is satisfied: " + nri.request);
                        }
                        if (satisfies) {
                            if (VDBG) {
                                log("currentScore = " + (currentNetwork != null ? currentNetwork.getCurrentScore() : 0) + ", newScore = " + score);
                            }
                            if (currentNetwork == null || currentNetwork.getCurrentScore() < score) {
                                if (VDBG) {
                                    log("rematch for " + newNetwork.name());
                                }
                                if (currentNetwork != null) {
                                    if (VDBG) {
                                        log("   accepting network in place of " + currentNetwork.name());
                                    }
                                    currentNetwork.removeRequest(nri.request.requestId);
                                    currentNetwork.lingerRequest(nri.request, now, (long) this.mLingerDelayMs);
                                    affectedNetworks.add(currentNetwork);
                                } else if (VDBG) {
                                    log("   accepting network in place of null");
                                }
                                newNetwork.unlingerRequest(nri.request);
                                synchronized (this.mNetworkForRequestId) {
                                    this.mNetworkForRequestId.put(nri.request.requestId, newNetwork);
                                }
                                if (!newNetwork.addRequest(nri.request)) {
                                    Slog.e(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                                }
                                addedRequests.add(nri);
                                keep = true;
                                sendUpdatedScoreToFactories(nri.request, score);
                                if (isDefaultRequest(nri)) {
                                    isNewDefault = true;
                                    oldDefaultNetwork = currentNetwork;
                                    if (currentNetwork != null) {
                                        this.mLingerMonitor.noteLingerDefaultNetwork(currentNetwork, newNetwork);
                                    }
                                }
                            }
                        } else {
                            if (newNetwork.isSatisfyingRequest(nri.request.requestId)) {
                                log("Network " + newNetwork.name() + " stopped satisfying" + " request " + nri.request.requestId);
                                newNetwork.removeRequest(nri.request.requestId);
                                if (currentNetwork == newNetwork) {
                                    if (isDefaultRequest(nri)) {
                                        try {
                                            this.mNetd.clearDefaultNetId();
                                        } catch (RemoteException e) {
                                            loge("clearDefaultNetId err:" + e);
                                        }
                                        log("clear default network = " + newNetwork.name());
                                        this.mLegacyTypeTracker.remove(newNetwork.networkInfo.getType(), newNetwork, true);
                                        newNetwork.lingerRequest(nri.request, now, (long) this.mLingerDelayMs);
                                    }
                                    synchronized (this.mNetworkForRequestId) {
                                        this.mNetworkForRequestId.remove(nri.request.requestId);
                                    }
                                    sendUpdatedScoreToFactories(nri.request, 0);
                                } else {
                                    Slog.e(TAG, "BUG: Removing request " + nri.request.requestId + " from " + newNetwork.name() + " without updating mNetworkForRequestId or factories!");
                                }
                                callCallbackForRequest(nri, newNetwork, 524292, 0);
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
            if (isNewDefault) {
                if (this.hasWifiAssistant) {
                    if (oldDefaultNetwork == null || oldDefaultNetwork.networkInfo == null || newNetwork.networkInfo == null) {
                        if (oldDefaultNetwork == null && newNetwork.networkInfo != null && newNetwork.networkInfo.getType() == 1) {
                            sendNetworkChangeBroadcast(false, newNetwork.getCurrentScore());
                            releaseCelluarNetworkRequest();
                        }
                    } else if (oldDefaultNetwork.networkInfo.getType() == 1 && newNetwork.networkInfo.getType() == 0) {
                        sendNetworkChangeBroadcast(true, oldDefaultNetwork.getCurrentScore());
                    } else if (oldDefaultNetwork.networkInfo.getType() == 0 && newNetwork.networkInfo.getType() == 1) {
                        sendNetworkChangeBroadcast(false, newNetwork.getCurrentScore());
                        releaseCelluarNetworkRequest();
                    }
                }
                makeDefault(newNetwork);
                logDefaultNetworkEvent(newNetwork, oldDefaultNetwork);
                synchronized (this) {
                    if (this.mNetTransitionWakeLock.isHeld()) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8, this.mNetTransitionWakeLockSerialNumber, 0), 1000);
                    }
                }
            }
            if (!newNetwork.networkCapabilities.equalRequestableCapabilities(nc)) {
                str = TAG;
                objArr = new Object[2];
                objArr[0] = nc;
                objArr[1] = newNetwork.networkCapabilities;
                Slog.e(str, String.format("BUG: changed requestable capabilities during rematch: %s -> %s", objArr));
            }
            if (newNetwork.getCurrentScore() != score) {
                str = TAG;
                objArr = new Object[2];
                objArr[0] = Integer.valueOf(score);
                objArr[1] = Integer.valueOf(newNetwork.getCurrentScore());
                Slog.e(str, String.format("BUG: changed score during rematch: %d -> %d", objArr));
            }
            if (wasBackgroundNetwork != newNetwork.isBackgroundNetwork()) {
                updateCapabilities(score, newNetwork, newNetwork.networkCapabilities);
            } else {
                processListenRequests(newNetwork, false);
            }
            for (NetworkRequestInfo nri2 : addedRequests) {
                notifyNetworkCallback(newNetwork, nri2);
            }
            for (NetworkAgentInfo nai : affectedNetworks) {
                updateLingerState(nai, now);
            }
            updateLingerState(newNetwork, now);
            if (isNewDefault) {
                if (oldDefaultNetwork != null) {
                    this.mLegacyTypeTracker.remove(oldDefaultNetwork.networkInfo.getType(), oldDefaultNetwork, true);
                }
                this.mDefaultInetConditionPublished = newNetwork.lastValidated ? 100 : 0;
                this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                notifyLockdownVpn(newNetwork);
            }
            if (keep) {
                try {
                    IBatteryStats bs = BatteryStatsService.getService();
                    int type = newNetwork.networkInfo.getType();
                    String baseIface = newNetwork.linkProperties.getInterfaceName();
                    bs.noteNetworkInterfaceType(baseIface, type);
                    for (LinkProperties stacked : newNetwork.linkProperties.getStackedLinks()) {
                        String stackedIface = stacked.getInterfaceName();
                        bs.noteNetworkInterfaceType(stackedIface, type);
                        NetworkStatsFactory.noteStackedIface(stackedIface, baseIface);
                    }
                } catch (RemoteException e2) {
                }
                for (int i = 0; i < newNetwork.numNetworkRequests(); i++) {
                    NetworkRequest nr = newNetwork.requestAt(i);
                    if (!(nr == null || nr.legacyType == -1 || !nr.isRequest())) {
                        this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                    }
                }
                if (newNetwork.isVPN()) {
                    this.mLegacyTypeTracker.add(17, newNetwork);
                }
            }
            if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                    if (unneeded(nai2, UnneededFor.TEARDOWN)) {
                        if (nai2.getLingerExpiry() > 0) {
                            updateLingerState(nai2, now);
                        } else {
                            log("Reaping " + nai2.name());
                            if (this.hasWifiAssistant && this.mShouldKeepCelluarNetwork && nai2.networkCapabilities.hasTransport(0)) {
                                if (WLAN_ASSIST_DBG) {
                                    log("WLAN+ Ignore Reaping CelluarNetwork: " + nai2.name());
                                }
                            } else if (this.hasWifiAssistant && nai2.networkCapabilities.hasTransport(1)) {
                                log("wifi_assist type wifi ,no reaping");
                            } else {
                                teardownUnneededNetwork(nai2);
                            }
                        }
                    }
                }
            }
        }
    }

    private void rematchAllNetworksAndRequests(NetworkAgentInfo changed, int oldScore) {
        long now = SystemClock.elapsedRealtime();
        if (changed == null || oldScore >= changed.getCurrentScore()) {
            NetworkAgentInfo[] nais = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[this.mNetworkAgentInfos.size()]);
            Arrays.sort(nais);
            for (NetworkAgentInfo nai : nais) {
                ReapUnvalidatedNetworks reapUnvalidatedNetworks;
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

    private void updateInetCondition(NetworkAgentInfo nai) {
        if (nai.everValidated && isDefaultNetwork(nai)) {
            int newInetCondition = nai.lastValidated ? 100 : 0;
            if (newInetCondition != this.mDefaultInetConditionPublished) {
                this.mDefaultInetConditionPublished = newInetCondition;
                sendInetConditionBroadcast(nai.networkInfo);
            }
        }
    }

    private void notifyLockdownVpn(NetworkAgentInfo nai) {
        if (this.mLockdownTracker == null) {
            return;
        }
        if (nai == null || !nai.isVPN()) {
            this.mLockdownTracker.onNetworkInfoChanged();
        } else {
            this.mLockdownTracker.onVpnStateChanged(nai.networkInfo);
        }
    }

    private void updateNetworkInfo(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        State state = newInfo.getState();
        int oldScore = networkAgent.getCurrentScore();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        notifyLockdownVpn(networkAgent);
        if (oldInfo == null || oldInfo.getState() != state) {
            log(networkAgent.name() + " EVENT_NETWORK_INFO_CHANGED, going from " + (oldInfo == null ? "null" : oldInfo.getState()) + " to " + state);
            if (!networkAgent.created && (state == State.CONNECTED || (state == State.CONNECTING && networkAgent.isVPN()))) {
                networkAgent.networkCapabilities.addCapability(18);
                try {
                    if (networkAgent.isVPN()) {
                        INetworkManagementService iNetworkManagementService = this.mNetd;
                        int i = networkAgent.network.netId;
                        boolean z = !networkAgent.linkProperties.getDnsServers().isEmpty();
                        boolean z2 = networkAgent.networkMisc != null ? !networkAgent.networkMisc.allowBypass : true;
                        iNetworkManagementService.createVirtualNetwork(i, z, z2);
                    } else if ("IWLAN".equals(networkAgent.networkInfo.getSubtypeName())) {
                        this.mNetd.createPhysicalNetwork(networkAgent.network.netId, NetworkManagementService.PERMISSION_SYSTEM);
                    } else {
                        this.mNetd.createPhysicalNetwork(networkAgent.network.netId, getNetworkPermission(networkAgent.networkCapabilities));
                    }
                    networkAgent.created = true;
                } catch (Exception e) {
                    loge("Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
                    return;
                }
            }
            if (!networkAgent.everConnected && state == State.CONNECTED) {
                networkAgent.everConnected = true;
                updateLinkProperties(networkAgent, null);
                notifyIfacesChangedForNetworkStats();
                networkAgent.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_CONNECTED);
                scheduleUnvalidatedPrompt(networkAgent);
                if (networkAgent.isVPN()) {
                    synchronized (this.mProxyLock) {
                        if (!this.mDefaultProxyDisabled) {
                            this.mDefaultProxyDisabled = true;
                            if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                                sendProxyBroadcast(null);
                            }
                        }
                    }
                }
                updateSignalStrengthThresholds(networkAgent, "CONNECT", null);
                rematchNetworkAndRequests(networkAgent, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
                notifyNetworkCallbacks(networkAgent, 524289);
            } else if (state == State.DISCONNECTED) {
                networkAgent.asyncChannel.disconnect();
                if (networkAgent.isVPN()) {
                    synchronized (this.mProxyLock) {
                        if (this.mDefaultProxyDisabled) {
                            this.mDefaultProxyDisabled = false;
                            if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                                sendProxyBroadcast(this.mDefaultProxy);
                            }
                        }
                    }
                }
            } else if ((oldInfo != null && oldInfo.getState() == State.SUSPENDED) || state == State.SUSPENDED) {
                int i2;
                if (networkAgent.getCurrentScore() != oldScore) {
                    rematchAllNetworksAndRequests(networkAgent, oldScore);
                }
                if (state == State.SUSPENDED) {
                    i2 = 524299;
                } else {
                    i2 = 524300;
                }
                notifyNetworkCallbacks(networkAgent, i2);
                this.mLegacyTypeTracker.update(networkAgent);
            }
            if ((state == State.CONNECTED || state == State.DISCONNECTED) && networkAgent.networkCapabilities.hasCapability(0)) {
                sendStickyBroadcast(new Intent("com.mediatek.conn.MMS_CONNECTIVITY"));
            }
            if (this.hasWifiAssistant && networkAgent != null && networkAgent.networkInfo != null) {
                String extraStr = networkAgent.networkInfo.getExtraInfo();
                boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                boolean isMmsApn = networkAgent.networkCapabilities.hasCapability(0);
                boolean isCellular = networkAgent.networkCapabilities.hasTransport(0);
                boolean isConnected = networkAgent.networkInfo.isConnected();
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ mShouldKeepCelluarNetwork:" + this.mShouldKeepCelluarNetwork + " mBlock:" + this.mBlock + " isConnected:" + isConnected + " isCellular:" + isCellular);
                }
                if (!isImsApn && !isMmsApn && isCellular && isConnected && this.mShouldKeepCelluarNetwork && !this.mBlock) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ CELLULAR CONNECTED! request measureDataState with new Thread!");
                    }
                    new Thread() {
                        public void run() {
                            ConnectivityService.this.measureDataState(0);
                        }
                    }.start();
                }
                return;
            }
            return;
        }
        if (oldInfo.isRoaming() != newInfo.isRoaming()) {
            if (VDBG) {
                log("roaming status changed, notifying NetworkStatsService");
            }
            notifyIfacesChangedForNetworkStats();
        } else if (VDBG) {
            log("ignoring duplicate network state non-change");
        }
    }

    private void updateNetworkScore(NetworkAgentInfo nai, int score) {
        if (VDBG) {
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

    protected void notifyNetworkCallback(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        if (nri.mPendingIntent == null) {
            callCallbackForRequest(nri, nai, 524290, 0);
        } else {
            sendPendingIntentForRequest(nri, nai, 524290);
        }
    }

    private void sendLegacyNetworkBroadcast(NetworkAgentInfo nai, DetailedState state, int type) {
        if (!(nai == null || !this.hasWifiAssistant || getActiveNetworkInfo() == null || getDefaultNetwork() == null)) {
            boolean changedNetworkIsCellular = nai.networkCapabilities.hasTransport(0);
            boolean defaultNetworkIsWifi = getDefaultNetwork().networkCapabilities.hasTransport(1);
            boolean activeNetworkIsWifi = getActiveNetworkInfo().getType() == 1;
            boolean isCtsRunning = this.mContext.getPackageManager().isFullFunctionMode();
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ changedNetworkIsCellular = " + changedNetworkIsCellular + " activeNetworkIsWifi = " + activeNetworkIsWifi + " isCtsRunning " + isCtsRunning);
            }
            if (this.mShouldKeepCelluarNetwork && changedNetworkIsCellular && activeNetworkIsWifi && defaultNetworkIsWifi && !isCtsRunning) {
                log("WLAN+ ignore broadcast CONNECTIVITY_ACTION! isAcitiveNetwork = false");
                return;
            }
        }
        NetworkInfo info = new NetworkInfo(nai.networkInfo);
        info.setType(type);
        if (state != DetailedState.DISCONNECTED) {
            info.setDetailedState(state, null, info.getExtraInfo());
            sendConnectedBroadcast(info);
        } else {
            info.setDetailedState(state, info.getReason(), info.getExtraInfo());
            Intent intent = new Intent("android.net.conn.CONNECTIVITY_CHANGE");
            intent.putExtra("networkInfo", info);
            intent.putExtra("networkType", info.getType());
            if (info.isFailover()) {
                intent.putExtra("isFailover", true);
                nai.networkInfo.setFailover(false);
            }
            if (info.getReason() != null) {
                log("broadcast DISCONNECTED reason=" + info.getReason());
                intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, info.getReason());
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
            intent.putExtra("subId", nai.networkCapabilities.getNetworkSpecifier());
            sendStickyBroadcast(intent);
            if (newDefaultAgent != null) {
                sendConnectedBroadcast(newDefaultAgent.networkInfo);
            }
        }
        if (OifaceUtil.isEnable()) {
            OifaceUtil.getInstance().currentNetwork(info.getState() == State.CONNECTED ? NetType.OIFACE_NETWORK_DATA_ON_WWLAN.ordinal() : NetType.OIFACE_NETWORK_DATA_OFF_WWLAN.ordinal());
        }
    }

    protected void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType, int arg1) {
        if (VDBG) {
            log("notifyType " + notifyTypeToName(notifyType) + " for " + networkAgent.name());
        }
        for (int i = 0; i < networkAgent.numNetworkRequests(); i++) {
            NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get(networkAgent.requestAt(i));
            if (nri != null) {
                if (nri.mPendingIntent == null) {
                    callCallbackForRequest(nri, networkAgent, notifyType, arg1);
                } else {
                    sendPendingIntentForRequest(nri, networkAgent, notifyType);
                }
            }
        }
    }

    protected void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        notifyNetworkCallbacks(networkAgent, notifyType, 0);
    }

    private String notifyTypeToName(int notifyType) {
        switch (notifyType) {
            case 524289:
                return "PRECHECK";
            case 524290:
                return "AVAILABLE";
            case 524291:
                return "LOSING";
            case 524292:
                return "LOST";
            case 524293:
                return "UNAVAILABLE";
            case 524294:
                return "CAP_CHANGED";
            case 524295:
                return "IP_CHANGED";
            case 524296:
                return "RELEASED";
            default:
                return "UNKNOWN";
        }
    }

    private void notifyIfacesChangedForNetworkStats() {
        try {
            this.mStatsService.forceUpdateIfaces();
        } catch (Exception e) {
        }
    }

    public boolean addVpnAddress(String address, int prefixLength) {
        boolean addAddress;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            addAddress = ((Vpn) this.mVpns.get(user)).addAddress(address, prefixLength);
        }
        return addAddress;
    }

    public boolean removeVpnAddress(String address, int prefixLength) {
        boolean removeAddress;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            removeAddress = ((Vpn) this.mVpns.get(user)).removeAddress(address, prefixLength);
        }
        return removeAddress;
    }

    public boolean setUnderlyingNetworksForVpn(Network[] networks) {
        boolean success;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            success = ((Vpn) this.mVpns.get(user)).setUnderlyingNetworks(networks);
        }
        if (success) {
            notifyIfacesChangedForNetworkStats();
        }
        return success;
    }

    public String getCaptivePortalServerUrl() {
        return NetworkMonitor.getCaptivePortalServerHttpUrl(this.mContext);
    }

    public void startNattKeepalive(Network network, int intervalSeconds, Messenger messenger, IBinder binder, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), intervalSeconds, messenger, binder, srcAddr, srcPort, dstAddr, 4500);
    }

    public void stopKeepalive(Network network, int slot) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(528396, slot, 0, network));
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
            setAirplaneMode(false);
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                for (String tether : getTetheredIfaces()) {
                    untether(tether);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_vpn")) {
                synchronized (this.mVpns) {
                    String alwaysOnPackage = getAlwaysOnVpnPackage(userId);
                    if (alwaysOnPackage != null) {
                        setAlwaysOnVpnPackage(userId, null, false);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, false);
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
            Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", null);
        }
    }

    public NetworkMonitor createNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new NetworkMonitor(context, handler, nai, defaultRequest);
    }

    AvoidBadWifiTracker createAvoidBadWifiTracker(Context c, Handler h, Runnable r) {
        return new AvoidBadWifiTracker(c, h, r);
    }

    public WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int cmd, Object obj) {
        return new WakeupMessage(c, h, s, cmd, 0, 0, obj);
    }

    private void logDefaultNetworkEvent(NetworkAgentInfo newNai, NetworkAgentInfo prevNai) {
        int newNetid = 0;
        int prevNetid = 0;
        int[] transports = new int[0];
        boolean hadIPv4 = false;
        boolean hadIPv6 = false;
        if (newNai != null) {
            newNetid = newNai.network.netId;
            transports = newNai.networkCapabilities.getTransportTypes();
        }
        if (prevNai != null) {
            prevNetid = prevNai.network.netId;
            LinkProperties lp = prevNai.linkProperties;
            hadIPv4 = lp.hasIPv4Address() ? lp.hasIPv4DefaultRoute() : false;
            hadIPv6 = lp.hasGlobalIPv6Address() ? lp.hasIPv6DefaultRoute() : false;
        }
        this.mMetricsLog.log(new DefaultNetworkEvent(newNetid, transports, prevNetid, hadIPv4, hadIPv6));
    }

    private void logNetworkEvent(NetworkAgentInfo nai, int evtype) {
        this.mMetricsLog.log(new NetworkEvent(nai.network.netId, evtype));
    }

    public boolean isTetheringChangeDone() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.isTetheringChangeDone();
        }
        return true;
    }

    public String[] getTetheredIfacePairs() {
        enforceTetherAccessPermission();
        return this.mTethering.getTetheredIfacePairs();
    }

    public void setTetheringIpv6Enable(boolean enable) {
        enforceTetherAccessPermission();
        this.mTethering.setIpv6FeatureEnable(enable);
    }

    public boolean getTetheringIpv6Enable() {
        enforceTetherAccessPermission();
        return this.mTethering.getIpv6FeatureEnable();
    }

    private boolean isOnlyIpv6Address(List<InetAddress> list) {
        for (InetAddress ia : list) {
            if (ia instanceof Inet4Address) {
                return false;
            }
        }
        return true;
    }

    public Network getNetworkIfCreated(NetworkRequest nr) {
        synchronized (this.mRequestLock) {
            for (NetworkRequestInfo nri : new HashMap(this.mNetworkRequests).values()) {
                if (nri.request.networkCapabilities.equalsNetCapabilities(nr.networkCapabilities)) {
                    NetworkAgentInfo currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                    if (currentNetwork != null && currentNetwork.created) {
                        log("getNetworkIfCreated");
                        Network network = currentNetwork.network;
                        return network;
                    }
                }
            }
            return null;
        }
    }

    public void monitorHttpRedirect(String location) {
        int appUid = Binder.getCallingUid();
        log("[NetworkHttpMonitor] monitorHttpRedirect");
        if (mNetworkHttpMonitor != null) {
            mNetworkHttpMonitor.monitorHttpRedirect(location, appUid);
            log("[NetworkHttpMonitor] calll for monitorHttpRedirect");
            return;
        }
        loge("Null object for mNetworkHttpMonitor");
    }

    public boolean isFirewallEnabled() {
        log("[NetworkHttpMonitor] isFirewallEnabled");
        return mNetworkHttpMonitor.isFirewallEnabled();
    }

    public String getWebLocation() {
        log("[NetworkHttpMonitor] getWebLocation");
        return mNetworkHttpMonitor.getWebLocation();
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
            log("WLAN+ getCelluarNetworkRequest ENABLE_WLAN_ASSISTANT = " + this.ENABLE_WLAN_ASSISTANT + " (now - lastRequestNetworkTime) = " + (now - this.mLastRequestNetworkTime));
        }
        if (now - this.mLastRequestNetworkTime < this.RECONNECT_TIMER || this.ENABLE_WLAN_ASSISTANT <= 0) {
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ ignore getCelluarNetworkRequest!");
            }
            return null;
        }
        boolean isCellular;
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (!(nai == null || nai.networkInfo == null)) {
                String extraStr = nai.networkInfo.getExtraInfo();
                boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                boolean isMmsApn = nai.networkCapabilities.hasCapability(0);
                isCellular = nai.networkCapabilities.hasTransport(0);
                boolean isLingering = nai.isLingering();
                if (!isImsApn && !isMmsApn && isCellular && isLingering) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ CELLULAR isLingering! Ignore!");
                    }
                    return null;
                }
            }
        }
        this.mLastRequestNetworkTime = now;
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ network request: " + nri.request);
            }
            isCellular = nri.request.networkCapabilities.hasTransport(0);
            boolean isInternet = nri.request.networkCapabilities.hasCapability(12);
            boolean isForeground = nri.request.networkCapabilities.hasCapability(18);
            boolean isDefaultNetwork = -1 == nri.request.legacyType;
            if (isInternet && isCellular && isDefaultNetwork && !isForeground) {
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ it's CELLULAR INTERNET! request id:" + nri.request.requestId);
                }
                this.mLastMeasureTime = 0;
                return nri.request;
            }
        }
        NetworkRequest request = new Builder().addCapability(12).addTransportType(0).build();
        this.mLastMeasureTime = 0;
        return request;
    }

    private void releaseCelluarNetworkRequest() {
        try {
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (WLAN_ASSIST_DBG) {
                    log("WLAN+ Network request: " + nri.request);
                }
                boolean isCellular = nri.request.networkCapabilities.hasTransport(0);
                boolean isInternet = nri.request.networkCapabilities.hasCapability(12);
                boolean isRequest = nri.request.type == Type.REQUEST;
                if (isCellular && isInternet && isRequest) {
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ releaseCelluarNetworkRequest request: " + nri.request);
                    }
                    releaseNetworkRequest(nri.request);
                }
            }
        } catch (Exception e) {
            loge("WLAN+ releaseCelluarNetworkRequest Exception: " + e.toString());
        }
        this.mLastRequestNetworkTime = 0;
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ releaseCelluarNetworkRequest end!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:98:0x0337 A:{Catch:{ IOException -> 0x0261, NullPointerException -> 0x03da, Exception -> 0x0400, all -> 0x0438 }} */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0451 A:{LOOP_END, LOOP:1: B:61:0x0226->B:143:0x0451, Catch:{ IOException -> 0x0261, NullPointerException -> 0x03da, Exception -> 0x0400, all -> 0x0438 }} */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0374 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean measureDataState(int siganlLevel) {
        long now = SystemClock.elapsedRealtime();
        if (WLAN_ASSIST_DBG) {
            log("WLAN+ mBlock = " + this.mBlock + " now - mLastMeasureTime = " + (now - this.mLastMeasureTime) + "ENABLE_WLAN_ASSISTANT = " + this.ENABLE_WLAN_ASSISTANT);
        }
        if (now - this.mLastMeasureTime < this.REMEASURE_TIMER || this.mBlock || this.ENABLE_WLAN_ASSISTANT <= 0) {
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ ignore measureDataState!");
            }
            return true;
        }
        NetworkAgentInfo targetNai = null;
        boolean celluarFound = false;
        long RTT = 0;
        long token = Binder.clearCallingIdentity();
        try {
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                if (!(nai == null || nai.networkInfo == null)) {
                    String extraStr = nai.networkInfo.getExtraInfo();
                    boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                    boolean isMmsApn = nai.networkCapabilities.hasCapability(0);
                    boolean isCellular = nai.networkCapabilities.hasTransport(0);
                    boolean isConnected = nai.networkInfo.isConnected();
                    if (WLAN_ASSIST_DBG) {
                        log("WLAN+ connction name:" + nai.name() + ", state: " + nai.networkInfo.getState() + ", Extra:" + extraStr);
                    }
                    if (!(isImsApn || isMmsApn || !isCellular)) {
                        if (isConnected) {
                            boolean shouldReMeasure = nai.getCurrentScore() != 50 || now - this.mLastMeasureTime > this.REMEASURE_TIMER_FINE;
                            if (shouldReMeasure) {
                                this.mBlock = true;
                                this.mLastMeasureTime = now;
                                celluarFound = true;
                                targetNai = nai;
                                int RAT = TelephonyManager.getNetworkClass(nai.networkInfo.getSubtype()) + 1;
                                if (WLAN_ASSIST_DBG) {
                                    log("WLAN+ mMeasureTime:" + this.mMeasureTime + " it's TRANSPORT_CELLULAR, RAT:" + RAT + "G, siganlLevel:" + siganlLevel);
                                }
                                int i = this.mMeasureTime;
                                while (i < this.TOTAL_MEASURE_TIME) {
                                    long requestTimestamp = 0;
                                    SocketFactory socketfactory = nai.network.getSocketFactory();
                                    InetAddress[] hostAddresses = nai.network.getAllByName(this.SERVERS[i]);
                                    if (hostAddresses.length <= 0) {
                                        throw new UnknownHostException(this.SERVERS[i]);
                                    }
                                    long singleRtt;
                                    int j = 0;
                                    while (j < hostAddresses.length) {
                                        Socket socket = null;
                                        try {
                                            socket = socketfactory.createSocket();
                                            requestTimestamp = SystemClock.elapsedRealtime();
                                            socket.connect(new InetSocketAddress(hostAddresses[j], 80), this.RESPONSE_TIMEOUT);
                                            if (socket != null) {
                                                if (!socket.isClosed()) {
                                                    socket.close();
                                                }
                                            }
                                            singleRtt = SystemClock.elapsedRealtime() - requestTimestamp;
                                            if (WLAN_ASSIST_DBG) {
                                                log("WLAN+ " + this.SERVERS[i] + " response time:" + singleRtt);
                                            }
                                            if (singleRtt >= ((long) this.RESPONSE_TIMEOUT)) {
                                                RTT = ((long) (this.TOTAL_MEASURE_TIME - this.mMeasureTime)) * singleRtt;
                                                break;
                                            }
                                            RTT += singleRtt;
                                            i++;
                                        } catch (IOException e) {
                                            if (j == hostAddresses.length - 1) {
                                                throw e;
                                            }
                                            if (socket != null) {
                                                if (!socket.isClosed()) {
                                                    socket.close();
                                                }
                                            }
                                            j++;
                                        } catch (Throwable th) {
                                            if (socket != null) {
                                                if (!socket.isClosed()) {
                                                    socket.close();
                                                }
                                            }
                                            throw th;
                                        }
                                    }
                                    singleRtt = SystemClock.elapsedRealtime() - requestTimestamp;
                                    if (WLAN_ASSIST_DBG) {
                                    }
                                    if (singleRtt >= ((long) this.RESPONSE_TIMEOUT)) {
                                    }
                                }
                                if (nai != null) {
                                    if (RTT <= 0 || RTT >= ((long) ((this.TOTAL_MEASURE_TIME - this.mMeasureTime) * this.RESPONSE_TIMEOUT))) {
                                        if (nai.getCurrentScore() != 10) {
                                            updateNetworkScore(nai, 10);
                                            sendBroadcastDataScore(10);
                                        }
                                    } else if (nai.getCurrentScore() != 50) {
                                        updateNetworkScore(nai, 50);
                                        sendBroadcastDataScore(50);
                                    }
                                }
                                this.mMeasureTime = 0;
                            } else {
                                if (WLAN_ASSIST_DBG) {
                                    log("WLAN+ ignore measureDataState! shouldReMeasure:" + shouldReMeasure);
                                }
                                Binder.restoreCallingIdentity(token);
                                return true;
                            }
                        }
                        if (nai.getCurrentScore() != 10) {
                            updateNetworkScore(nai, 10);
                            sendBroadcastDataScore(10);
                        }
                        if (WLAN_ASSIST_DBG) {
                            log("WLAN+ ignore measureDataState! network state:" + nai.networkInfo.getState());
                        }
                        Binder.restoreCallingIdentity(token);
                        return true;
                    }
                }
            }
            Binder.restoreCallingIdentity(token);
        } catch (IOException e2) {
            loge("WLAN+ mMeasureTime:" + this.mMeasureTime + " IOException:" + e2.toString());
            this.mMeasureTime++;
            if (this.mMeasureTime >= this.TOTAL_MEASURE_TIME) {
                this.mMeasureTime = 0;
                if (targetNai != null) {
                    try {
                        if (targetNai.getCurrentScore() != 10) {
                            updateNetworkScore(targetNai, 10);
                            sendBroadcastDataScore(10);
                        }
                    } catch (NullPointerException ne) {
                        loge("WLAN+ NullPointerException:" + ne.toString());
                    } catch (Exception ee) {
                        loge("WLAN+ Exception:" + ee.toString());
                    }
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    loge("WLAN+ InterruptedException:" + ie.toString());
                }
                this.mBlock = false;
                this.mLastMeasureTime = 0;
                if (!measureDataState(siganlLevel)) {
                    Binder.restoreCallingIdentity(token);
                    return false;
                }
            }
            Binder.restoreCallingIdentity(token);
        } catch (NullPointerException e3) {
            loge("WLAN+ NullPointerException:" + e3.toString());
            Binder.restoreCallingIdentity(token);
        } catch (Exception e4) {
            loge("WLAN+ Exception:" + e4.toString());
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(token);
            throw th2;
        }
        this.mBlock = false;
        if (!celluarFound) {
            this.mLastMeasureTime = 0;
            if (WLAN_ASSIST_DBG) {
                log("WLAN+ warning ! celluar not Found !");
            }
        } else if (WLAN_ASSIST_DBG) {
            log("WLAN+ mMeasureTime:" + this.mMeasureTime + " RTT: " + RTT);
        }
        return celluarFound;
    }

    public boolean shouldKeepCelluarNetwork(boolean keep) {
        this.mShouldKeepCelluarNetwork = keep;
        return true;
    }

    public void updateDataNetworkConfig(String name, String value) {
        if (value != null && name != null) {
            if (name.equals("EnableWlanAssistant")) {
                try {
                    this.ENABLE_WLAN_ASSISTANT = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e);
                }
            } else if (name.equals("TotalMeasureTime")) {
                try {
                    this.TOTAL_MEASURE_TIME = Integer.parseInt(value);
                } catch (NumberFormatException e2) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e2);
                }
            } else if (name.equals("ResponseTimeout")) {
                try {
                    this.RESPONSE_TIMEOUT = Integer.parseInt(value);
                } catch (NumberFormatException e22) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e22);
                }
            } else if (name.equals("ReconnectTimer")) {
                try {
                    this.RECONNECT_TIMER = (long) Integer.parseInt(value);
                } catch (NumberFormatException e222) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e222);
                }
            } else if (name.equals("RemeasureTimer")) {
                try {
                    this.REMEASURE_TIMER = (long) Integer.parseInt(value);
                } catch (NumberFormatException e2222) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e2222);
                }
            } else if (name.equals("RemeasureTimerFine")) {
                try {
                    this.REMEASURE_TIMER_FINE = (long) Integer.parseInt(value);
                } catch (NumberFormatException e22222) {
                    loge("WLAN+ Numberex DataNetworkInvalid e:" + e22222);
                }
            } else if (name.equals("Server1")) {
                this.SERVERS[0] = value;
            } else if (name.equals("Server2")) {
                this.SERVERS[1] = value;
            } else if (name.equals("Server3")) {
                this.SERVERS[2] = value;
            }
        }
    }

    private void sendBroadcastDataScore(int score) {
        Intent dataIntent = new Intent(DATA_SCORE_CHANGE);
        dataIntent.putExtra(EXTRA_DATA_CORE, score);
        this.mContext.sendStickyBroadcastAsUser(dataIntent, UserHandle.ALL);
    }

    private void sendNetworkChangeBroadcast(boolean toData, int score) {
        Intent netIntent = new Intent(WIFI_NETWORK_CHANGE);
        netIntent.putExtra(EXTRA_WIFI_TO_DATA, toData);
        boolean wifiValid = false;
        if (score >= 20) {
            wifiValid = true;
        }
        netIntent.putExtra(EXTRA_WIFI_VALID, wifiValid);
        this.mContext.sendStickyBroadcastAsUser(netIntent, UserHandle.ALL);
    }

    private boolean isSwitchEnable() {
        if (this.mContext != null) {
            return Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
        } else {
            return false;
        }
    }

    private boolean isWlanAssistantEnable() {
        return this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant") ? Global.getInt(this.mContext.getContentResolver(), "rom.update.wifi.assistant", 1) == 1 : false;
    }

    public long getCurrentTimeMillis() {
        if (this.mContext != null && this.mTimeOem == null) {
            this.mTimeOem = NtpTrustedTime.getInstance(this.mContext);
        }
        if (this.mTimeOem != null) {
            return this.mTimeOem.currentTimeMillis();
        }
        return 0;
    }
}
