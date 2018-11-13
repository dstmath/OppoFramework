package com.android.server;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkStats;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Handler;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.am.OppoProcessManager;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.oppo.IElsaManager;
import com.android.server.secrecy.policy.DecryptTool;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.google.android.collect.Maps;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

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
public class NetworkManagementService extends Stub implements Monitor {
    static final int DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO = 1;
    private static final boolean DBG = false;
    public static final int DNS_RESOLVER_DEFAULT_MAX_SAMPLES = 64;
    public static final int DNS_RESOLVER_DEFAULT_MIN_SAMPLES = 8;
    public static final int DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS = 1800;
    public static final int DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT = 25;
    public static final String LIMIT_GLOBAL_ALERT = "globalAlert";
    private static final int MAX_UID_RANGES_PER_COMMAND = 10;
    private static final String NETD_SERVICE_NAME = "netd";
    private static final String NETD_TAG = "NetdConnector";
    public static final String PERMISSION_NETWORK = "NETWORK";
    public static final String PERMISSION_SYSTEM = "SYSTEM";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final boolean SDBG = false;
    static final String SOFT_AP_COMMAND = "softap";
    static final String SOFT_AP_COMMAND_SUCCESS = "Ok";
    private static final String TAG = "NetworkManagement";
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveAlerts;
    private HashMap<String, IdleTimerParams> mActiveIdleTimers;
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveQuotas;
    private volatile boolean mBandwidthControlEnabled;
    private IBatteryStats mBatteryStats;
    private CountDownLatch mConnectedSignal;
    private final NativeDaemonConnector mConnector;
    private final Context mContext;
    private final Handler mDaemonHandler;
    @GuardedBy("mQuotaLock")
    private boolean mDataSaverMode;
    private final Handler mFgHandler;
    @GuardedBy("mQuotaLock")
    final SparseBooleanArray mFirewallChainStates;
    private volatile boolean mFirewallEnabled;
    private Object mIdleTimerLock;
    private int mLastPowerStateFromRadio;
    private int mLastPowerStateFromWifi;
    private boolean mMobileActivityFromRadio;
    private INetd mNetdService;
    private boolean mNetworkActive;
    private final RemoteCallbackList<INetworkActivityListener> mNetworkActivityListeners;
    private final RemoteCallbackList<INetworkManagementEventObserver> mObservers;
    private Object mQuotaLock;
    private final NetworkStatsFactory mStatsFactory;
    private volatile boolean mStrictEnabled;
    private final Thread mThread;
    @GuardedBy("mQuotaLock")
    private SparseBooleanArray mUidAllowOnMetered;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidCleartextPolicy;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallDozableRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallStandbyRules;
    @GuardedBy("mQuotaLock")
    private SparseBooleanArray mUidRejectOnMetered;
    private SparseLongArray mUidStrictRules;

    private static class IdleTimerParams {
        public int networkCount = 1;
        public final int timeout;
        public final int type;

        IdleTimerParams(int timeout, int type) {
            this.timeout = timeout;
            this.type = type;
        }
    }

    private class NetdCallbackReceiver implements INativeDaemonConnectorCallbacks {
        /* synthetic */ NetdCallbackReceiver(NetworkManagementService this$0, NetdCallbackReceiver netdCallbackReceiver) {
            this();
        }

        private NetdCallbackReceiver() {
        }

        public void onDaemonConnected() {
            Slog.i(NetworkManagementService.TAG, "onDaemonConnected()");
            if (NetworkManagementService.this.mConnectedSignal != null) {
                NetworkManagementService.this.mConnectedSignal.countDown();
                NetworkManagementService.this.mConnectedSignal = null;
                return;
            }
            NetworkManagementService.this.mFgHandler.post(new Runnable() {
                public void run() {
                    NetworkManagementService.this.connectNativeNetdService();
                    NetworkManagementService.this.prepareNativeDaemon();
                }
            });
        }

        public boolean onCheckHoldWakeLock(int code) {
            return code == NetdResponseCode.InterfaceClassActivity;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            if (code != 699) {
                Slog.d(NetworkManagementService.TAG, "onEvent:" + raw + ":" + cooked.length);
            }
            Object[] objArr = new Object[1];
            objArr[0] = raw;
            String errorMessage = String.format("Invalid event from daemon (%s)", objArr);
            switch (code) {
                case 600:
                    if (cooked.length < 4 || !cooked[1].equals("Iface")) {
                        throw new IllegalStateException(errorMessage);
                    } else if (cooked[2].equals("added")) {
                        NetworkManagementService.this.notifyInterfaceAdded(cooked[3]);
                        return true;
                    } else if (cooked[2].equals("removed")) {
                        NetworkManagementService.this.notifyInterfaceRemoved(cooked[3]);
                        return true;
                    } else if (cooked[2].equals("changed") && cooked.length == 5) {
                        NetworkManagementService.this.notifyInterfaceStatusChanged(cooked[3], cooked[4].equals("up"));
                        return true;
                    } else if (cooked[2].equals("linkstate") && cooked.length == 5) {
                        NetworkManagementService.this.notifyInterfaceLinkStateChanged(cooked[3], cooked[4].equals("up"));
                        return true;
                    } else {
                        throw new IllegalStateException(errorMessage);
                    }
                case NetdResponseCode.BandwidthControl /*601*/:
                    if (cooked.length < 5 || !cooked[1].equals("limit")) {
                        throw new IllegalStateException(errorMessage);
                    } else if (cooked[2].equals("alert")) {
                        NetworkManagementService.this.notifyLimitReached(cooked[3], cooked[4]);
                        return true;
                    } else {
                        throw new IllegalStateException(errorMessage);
                    }
                case NetdResponseCode.InterfaceClassActivity /*613*/:
                    if (cooked.length < 4 || !cooked[1].equals("IfaceClass")) {
                        throw new IllegalStateException(errorMessage);
                    }
                    int i;
                    long timestampNanos = 0;
                    int processUid = -1;
                    if (cooked.length >= 5) {
                        try {
                            timestampNanos = Long.parseLong(cooked[4]);
                            if (cooked.length == 6) {
                                processUid = Integer.parseInt(cooked[5]);
                            }
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        timestampNanos = SystemClock.elapsedRealtimeNanos();
                    }
                    boolean isActive = cooked[2].equals("active");
                    NetworkManagementService networkManagementService = NetworkManagementService.this;
                    int parseInt = Integer.parseInt(cooked[3]);
                    if (isActive) {
                        i = 3;
                    } else {
                        i = 1;
                    }
                    networkManagementService.notifyInterfaceClassActivity(parseInt, i, timestampNanos, processUid, true);
                    return true;
                case NetdResponseCode.InterfaceAddressChange /*614*/:
                    if (cooked.length < 7 || !cooked[1].equals("Address")) {
                        throw new IllegalStateException(errorMessage);
                    }
                    String iface = cooked[4];
                    try {
                        LinkAddress address;
                        int flags = Integer.parseInt(cooked[5]);
                        int scope = Integer.parseInt(cooked[6]);
                        if (cooked.length > 7) {
                            long valid = Long.parseLong(cooked[7]);
                            Slog.d(NetworkManagementService.TAG, "InterfaceAddressChange valid=" + valid);
                            address = new LinkAddress(cooked[3], flags, scope, valid);
                        } else {
                            Slog.d(NetworkManagementService.TAG, "InterfaceAddressChange no valid field");
                            address = new LinkAddress(cooked[3], flags, scope);
                        }
                        if (cooked[2].equals("updated")) {
                            NetworkManagementService.this.notifyAddressUpdated(iface, address);
                        } else {
                            NetworkManagementService.this.notifyAddressRemoved(iface, address);
                        }
                        return true;
                    } catch (Throwable e2) {
                        Slog.d(NetworkManagementService.TAG, "NumberFormatException");
                        throw new IllegalStateException(errorMessage, e2);
                    } catch (Throwable e3) {
                        Slog.d(NetworkManagementService.TAG, "IllegalArgumentException");
                        throw new IllegalStateException(errorMessage, e3);
                    }
                case NetdResponseCode.InterfaceDnsServerInfo /*615*/:
                    if (cooked.length == 6 && cooked[1].equals("DnsInfo") && cooked[2].equals("servers")) {
                        try {
                            long lifetime = Long.parseLong(cooked[4]);
                            NetworkManagementService.this.notifyInterfaceDnsServerInfo(cooked[3], lifetime, cooked[5].split(","));
                        } catch (NumberFormatException e4) {
                            throw new IllegalStateException(errorMessage);
                        }
                    }
                    return true;
                case NetdResponseCode.RouteChange /*616*/:
                    if (!cooked[1].equals("Route") || cooked.length < 6) {
                        throw new IllegalStateException(errorMessage);
                    }
                    String via = null;
                    String dev = null;
                    boolean valid2 = true;
                    for (int i2 = 4; i2 + 1 < cooked.length && valid2; i2 += 2) {
                        if (cooked[i2].equals("dev")) {
                            if (dev == null) {
                                dev = cooked[i2 + 1];
                            } else {
                                valid2 = false;
                            }
                        } else if (!cooked[i2].equals("via")) {
                            valid2 = false;
                        } else if (via == null) {
                            via = cooked[i2 + 1];
                        } else {
                            valid2 = false;
                        }
                    }
                    if (valid2) {
                        InetAddress gateway = null;
                        if (via != null) {
                            try {
                                gateway = InetAddress.parseNumericAddress(via);
                            } catch (IllegalArgumentException e5) {
                            }
                        }
                        NetworkManagementService.this.notifyRouteChange(cooked[2], new RouteInfo(new IpPrefix(cooked[3]), gateway, dev));
                        return true;
                    }
                    throw new IllegalStateException(errorMessage);
                case NetdResponseCode.StrictCleartext /*617*/:
                    try {
                        ActivityManagerNative.getDefault().notifyCleartextNetwork(Integer.parseInt(cooked[1]), HexDump.hexStringToByteArray(cooked[2]));
                        break;
                    } catch (RemoteException e6) {
                        break;
                    }
                case NetdResponseCode.InterfaceMessage /*619*/:
                    if (cooked.length < 3 || !cooked[2].equals("IfaceMessage")) {
                        throw new IllegalStateException(errorMessage);
                    }
                    Slog.d(NetworkManagementService.TAG, "onEvent: " + raw);
                    if (cooked[5] != null) {
                        NetworkManagementService.this.notifyInterfaceMessage(cooked[4] + " " + cooked[5]);
                    } else {
                        NetworkManagementService.this.notifyInterfaceMessage(cooked[4]);
                    }
                    return true;
                case NetdResponseCode.StrictSocketConn /*699*/:
                    int uidApp = Integer.parseInt(cooked[1]);
                    long oldTime = NetworkManagementService.this.mUidStrictRules.get(uidApp);
                    Intent intent = new Intent("com.mediatek.network.socketconn");
                    long newTime = SystemClock.uptimeMillis();
                    if (oldTime != 0) {
                        if (newTime - oldTime > 500) {
                            intent.putExtra(PackageProcessList.KEY_UID, uidApp);
                            NetworkManagementService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                            NetworkManagementService.this.mUidStrictRules.put(uidApp, newTime);
                            break;
                        }
                    }
                    intent.putExtra(PackageProcessList.KEY_UID, uidApp);
                    NetworkManagementService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    if (NetworkManagementService.this.mUidStrictRules.size() > 20) {
                        NetworkManagementService.this.mUidStrictRules.clear();
                    }
                    NetworkManagementService.this.mUidStrictRules.put(uidApp, newTime);
                    break;
                    break;
            }
            return false;
        }
    }

    class NetdResponseCode {
        public static final int BandwidthControl = 601;
        public static final int ClatdStatusResult = 223;
        public static final int DnsProxyQueryResult = 222;
        public static final int InterfaceAddressChange = 614;
        public static final int InterfaceChange = 600;
        public static final int InterfaceClassActivity = 613;
        public static final int InterfaceDnsServerInfo = 615;
        public static final int InterfaceGetCfgResult = 213;
        public static final int InterfaceListResult = 110;
        public static final int InterfaceMessage = 619;
        public static final int InterfaceRxCounterResult = 216;
        public static final int InterfaceRxThrottleResult = 218;
        public static final int InterfaceTxCounterResult = 217;
        public static final int InterfaceTxThrottleResult = 219;
        public static final int IpFwdStatusResult = 211;
        public static final int NetInfoSipError = 251;
        public static final int NetInfoSipResult = 250;
        public static final int QuotaCounterResult = 220;
        public static final int RouteChange = 616;
        public static final int SoftapStatusResult = 214;
        public static final int StrictCleartext = 617;
        public static final int StrictSocketConn = 699;
        public static final int TetherDnsFwdTgtListResult = 112;
        public static final int TetherInterfaceListResult = 111;
        public static final int TetherStatusResult = 210;
        public static final int TetheringStatsListResult = 114;
        public static final int TetheringStatsResult = 221;
        public static final int TtyListResult = 113;

        NetdResponseCode() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.NetworkManagementService.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.NetworkManagementService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkManagementService.<clinit>():void");
    }

    private NetworkManagementService(Context context, String socket) {
        this.mConnectedSignal = new CountDownLatch(1);
        this.mObservers = new RemoteCallbackList();
        this.mStatsFactory = new NetworkStatsFactory();
        this.mQuotaLock = new Object();
        this.mActiveQuotas = Maps.newHashMap();
        this.mActiveAlerts = Maps.newHashMap();
        this.mUidRejectOnMetered = new SparseBooleanArray();
        this.mUidAllowOnMetered = new SparseBooleanArray();
        this.mUidCleartextPolicy = new SparseIntArray();
        this.mUidFirewallRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mIdleTimerLock = new Object();
        this.mActiveIdleTimers = Maps.newHashMap();
        this.mMobileActivityFromRadio = false;
        this.mLastPowerStateFromRadio = 1;
        this.mLastPowerStateFromWifi = 1;
        this.mNetworkActivityListeners = new RemoteCallbackList();
        this.mUidStrictRules = new SparseLongArray();
        this.mContext = context;
        this.mFgHandler = new Handler(FgThread.get().getLooper());
        this.mConnector = new NativeDaemonConnector(new NetdCallbackReceiver(this, null), socket, 10, NETD_TAG, 160, null, FgThread.get().getLooper());
        this.mThread = new Thread(this.mConnector, NETD_TAG);
        this.mDaemonHandler = new Handler(FgThread.get().getLooper());
        Watchdog.getInstance().addMonitor(this);
    }

    static NetworkManagementService create(Context context, String socket) throws InterruptedException {
        NetworkManagementService service = new NetworkManagementService(context, socket);
        CountDownLatch connectedSignal = service.mConnectedSignal;
        if (DBG) {
            Slog.d(TAG, "Creating NetworkManagementService");
        }
        service.mThread.start();
        if (DBG) {
            Slog.d(TAG, "Awaiting socket connection");
        }
        connectedSignal.await();
        if (DBG) {
            Slog.d(TAG, "Connected");
        }
        service.connectNativeNetdService();
        return service;
    }

    public static NetworkManagementService create(Context context) throws InterruptedException {
        return create(context, NETD_SERVICE_NAME);
    }

    public void systemReady() {
        if (DBG) {
            long start = System.currentTimeMillis();
            prepareNativeDaemon();
            Slog.d(TAG, "Prepared in " + (System.currentTimeMillis() - start) + "ms");
            return;
        }
        prepareNativeDaemon();
    }

    private IBatteryStats getBatteryStats() {
        synchronized (this) {
            IBatteryStats iBatteryStats;
            if (this.mBatteryStats != null) {
                iBatteryStats = this.mBatteryStats;
                return iBatteryStats;
            }
            this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
            iBatteryStats = this.mBatteryStats;
            return iBatteryStats;
        }
    }

    public void registerObserver(INetworkManagementEventObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mObservers.register(observer);
    }

    public void unregisterObserver(INetworkManagementEventObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mObservers.unregister(observer);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceStatusChanged(String iface, boolean up) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceStatusChanged(iface, up);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceLinkStateChanged(String iface, boolean up) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceLinkStateChanged(iface, up);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceAdded(String iface) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceAdded(iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0048 A:{Splitter: B:2:0x002d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceRemoved(String iface) {
        Slog.d(TAG, "notifyInterfaceRemoved, iface=" + iface);
        this.mActiveAlerts.remove(iface);
        this.mActiveQuotas.remove(iface);
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceRemoved(iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyLimitReached(String limitName, String iface) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).limitReached(limitName, iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x008b A:{Splitter: B:45:0x0069, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceClassActivity(int type, int powerState, long tsNanos, int uid, boolean fromRadio) {
        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(type);
        if (isMobile) {
            if (fromRadio) {
                this.mMobileActivityFromRadio = true;
            } else if (this.mMobileActivityFromRadio) {
                powerState = this.mLastPowerStateFromRadio;
            }
            if (this.mLastPowerStateFromRadio != powerState) {
                this.mLastPowerStateFromRadio = powerState;
                try {
                    getBatteryStats().noteMobileRadioPowerState(powerState, tsNanos, uid);
                } catch (RemoteException e) {
                }
            }
        }
        if (ConnectivityManager.isNetworkTypeWifi(type) && this.mLastPowerStateFromWifi != powerState) {
            this.mLastPowerStateFromWifi = powerState;
            try {
                getBatteryStats().noteWifiRadioPowerState(powerState, tsNanos, uid);
            } catch (RemoteException e2) {
            }
        }
        boolean isActive = powerState != 2 ? powerState == 3 : true;
        if (!(isMobile && !fromRadio && this.mMobileActivityFromRadio)) {
            int length = this.mObservers.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceClassDataActivityChanged(Integer.toString(type), isActive, tsNanos);
                } catch (RemoteException e3) {
                } catch (Throwable th) {
                    this.mObservers.finishBroadcast();
                }
            }
            this.mObservers.finishBroadcast();
        }
        boolean report = false;
        synchronized (this.mIdleTimerLock) {
            if (this.mActiveIdleTimers.isEmpty()) {
                isActive = true;
            }
            if (this.mNetworkActive != isActive) {
                this.mNetworkActive = isActive;
                report = isActive;
            }
        }
        if (report) {
            reportNetworkActive();
        }
    }

    private void syncFirewallChainLocked(int chain, SparseIntArray uidFirewallRules, String name) {
        int size = uidFirewallRules.size();
        if (size > 0) {
            SparseIntArray rules = uidFirewallRules.clone();
            uidFirewallRules.clear();
            if (DBG) {
                Slog.d(TAG, "Pushing " + size + " active firewall " + name + "UID rules");
            }
            for (int i = 0; i < rules.size(); i++) {
                setFirewallUidRuleLocked(chain, rules.keyAt(i), rules.valueAt(i));
            }
        }
    }

    private void connectNativeNetdService() {
        boolean nativeServiceAvailable = false;
        try {
            this.mNetdService = INetd.Stub.asInterface(ServiceManager.getService(NETD_SERVICE_NAME));
            nativeServiceAvailable = this.mNetdService.isAlive();
        } catch (RemoteException e) {
        }
        if (!nativeServiceAvailable) {
            Slog.wtf(TAG, "Can't connect to NativeNetdService netd");
        }
    }

    private void notifyInterfaceMessage(String message) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceMessageRecevied(message);
            } catch (RemoteException e) {
            } catch (RuntimeException e2) {
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void prepareNativeDaemon() {
        Object[] objArr;
        if (SDBG) {
            this.mConnector.setDebug(true);
        }
        this.mBandwidthControlEnabled = false;
        if (new File("/proc/net/xt_qtaguid/ctrl").exists()) {
            Slog.d(TAG, "enabling bandwidth control");
            try {
                objArr = new Object[1];
                objArr[0] = "enable";
                this.mConnector.execute("bandwidth", objArr);
                this.mBandwidthControlEnabled = true;
            } catch (NativeDaemonConnectorException e) {
                Slog.e(TAG, "problem enabling bandwidth controls");
            }
        } else {
            Slog.i(TAG, "not enabling bandwidth control");
        }
        SystemProperties.set("net.qtaguid_enabled", this.mBandwidthControlEnabled ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
        if (this.mBandwidthControlEnabled) {
            try {
                getBatteryStats().noteNetworkStatsEnabled();
            } catch (RemoteException e2) {
            }
        }
        try {
            objArr = new Object[1];
            objArr[0] = "enable";
            this.mConnector.execute("strict", objArr);
            this.mStrictEnabled = true;
        } catch (NativeDaemonConnectorException e3) {
            Log.wtf(TAG, "Failed strict enable", e3);
        }
        synchronized (this.mQuotaLock) {
            int i;
            setDataSaverModeEnabled(this.mDataSaverMode);
            int size = this.mActiveQuotas.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active quota rules");
                }
                HashMap<String, Long> activeQuotas = this.mActiveQuotas;
                this.mActiveQuotas = Maps.newHashMap();
                for (Entry<String, Long> entry : activeQuotas.entrySet()) {
                    setInterfaceQuota((String) entry.getKey(), ((Long) entry.getValue()).longValue());
                }
            }
            size = this.mActiveAlerts.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active alert rules");
                }
                HashMap<String, Long> activeAlerts = this.mActiveAlerts;
                this.mActiveAlerts = Maps.newHashMap();
                for (Entry<String, Long> entry2 : activeAlerts.entrySet()) {
                    setInterfaceAlert((String) entry2.getKey(), ((Long) entry2.getValue()).longValue());
                }
            }
            size = this.mUidRejectOnMetered.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " UIDs to metered whitelist rules");
                }
                SparseBooleanArray uidRejectOnQuota = this.mUidRejectOnMetered;
                this.mUidRejectOnMetered = new SparseBooleanArray();
                for (i = 0; i < uidRejectOnQuota.size(); i++) {
                    setUidMeteredNetworkBlacklist(uidRejectOnQuota.keyAt(i), uidRejectOnQuota.valueAt(i));
                }
            }
            size = this.mUidAllowOnMetered.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " UIDs to metered blacklist rules");
                }
                SparseBooleanArray uidAcceptOnQuota = this.mUidAllowOnMetered;
                this.mUidAllowOnMetered = new SparseBooleanArray();
                for (i = 0; i < uidAcceptOnQuota.size(); i++) {
                    setUidMeteredNetworkWhitelist(uidAcceptOnQuota.keyAt(i), uidAcceptOnQuota.valueAt(i));
                }
            }
            size = this.mUidCleartextPolicy.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active UID cleartext policies");
                }
                SparseIntArray local = this.mUidCleartextPolicy;
                this.mUidCleartextPolicy = new SparseIntArray();
                for (i = 0; i < local.size(); i++) {
                    setUidCleartextNetworkPolicy(local.keyAt(i), local.valueAt(i));
                }
            }
            setFirewallEnabled(!this.mFirewallEnabled ? LockdownVpnTracker.isEnabled() : true);
            syncFirewallChainLocked(0, this.mUidFirewallRules, IElsaManager.EMPTY_PACKAGE);
            syncFirewallChainLocked(2, this.mUidFirewallStandbyRules, "standby ");
            syncFirewallChainLocked(1, this.mUidFirewallDozableRules, "dozable ");
            syncFirewallChainLocked(3, this.mUidFirewallPowerSaveRules, "powersave ");
            if (this.mFirewallChainStates.get(2)) {
                setFirewallChainEnabled(2, true);
            }
            if (this.mFirewallChainStates.get(1)) {
                setFirewallChainEnabled(1, true);
            }
            if (this.mFirewallChainStates.get(3)) {
                setFirewallChainEnabled(3, true);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyAddressUpdated(String iface, LinkAddress address) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).addressUpdated(iface, address);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyAddressRemoved(String iface, LinkAddress address) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).addressRemoved(iface, address);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyInterfaceDnsServerInfo(String iface, long lifetime, String[] addresses) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceDnsServerInfo(iface, lifetime, addresses);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x002c A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyRouteChange(String action, RouteInfo route) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                if (action.equals("updated")) {
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).routeUpdated(route);
                } else {
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).routeRemoved(route);
                }
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    public INetd getNetdService() throws RemoteException {
        CountDownLatch connectedSignal = this.mConnectedSignal;
        if (connectedSignal != null) {
            try {
                connectedSignal.await();
            } catch (InterruptedException e) {
            }
        }
        return this.mNetdService;
    }

    public String[] listInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "list";
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("interface", objArr), 110);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:26:0x0091, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x0092, code:
            android.util.Slog.e(TAG, "Failed to parse prefixLength", r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InterfaceConfiguration getInterfaceConfig(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "getcfg";
            objArr[1] = iface;
            NativeDaemonEvent event = this.mConnector.execute("interface", objArr);
            event.checkCode(NetdResponseCode.InterfaceGetCfgResult);
            StringTokenizer st = new StringTokenizer(event.getMessage());
            InterfaceConfiguration cfg = new InterfaceConfiguration();
            cfg.setHardwareAddress(st.nextToken(" "));
            InetAddress addr = null;
            int prefixLength = 0;
            try {
                addr = NetworkUtils.numericToInetAddress(st.nextToken());
            } catch (IllegalArgumentException iae) {
                Slog.e(TAG, "Failed to parse ipaddr", iae);
            }
            try {
                prefixLength = Integer.parseInt(st.nextToken());
                cfg.setLinkAddress(new LinkAddress(addr, prefixLength));
                while (st.hasMoreTokens()) {
                    cfg.setFlag(st.nextToken());
                }
                return cfg;
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Invalid response from daemon: " + event);
            }
        } catch (NativeDaemonConnectorException e2) {
            throw e2.rethrowAsParcelableException();
        }
    }

    public void setInterfaceConfig(String iface, InterfaceConfiguration cfg) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Slog.d(TAG, "Enter setInterfaceConfig, iface=" + iface);
        LinkAddress linkAddr = cfg.getLinkAddress();
        if (linkAddr == null || linkAddr.getAddress() == null) {
            throw new IllegalStateException("Null LinkAddress given");
        }
        Object[] objArr = new Object[4];
        objArr[0] = "setcfg";
        objArr[1] = iface;
        objArr[2] = linkAddr.getAddress().getHostAddress();
        objArr[3] = Integer.valueOf(linkAddr.getPrefixLength());
        Command cmd = new Command("interface", objArr);
        for (String flag : cfg.getFlags()) {
            cmd.appendArg(flag);
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "setInterfaceConfig Error");
        }
    }

    public void setInterfaceDown(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        InterfaceConfiguration ifcg = getInterfaceConfig(iface);
        ifcg.setInterfaceDown();
        setInterfaceConfig(iface, ifcg);
    }

    public void setInterfaceUp(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        InterfaceConfiguration ifcg = getInterfaceConfig(iface);
        ifcg.setInterfaceUp();
        setInterfaceConfig(iface, ifcg);
    }

    public void setInterfaceIpv6PrivacyExtensions(String iface, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "interface";
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6privacyextensions";
            objArr[1] = iface;
            objArr[2] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearInterfaceAddresses(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "clearaddrs";
            objArr[1] = iface;
            this.mConnector.execute("interface", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableIpv6(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6";
            objArr[1] = iface;
            objArr[2] = "enable";
            this.mConnector.execute("interface", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void disableIpv6(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6";
            objArr[1] = iface;
            objArr[2] = "disable";
            this.mConnector.execute("interface", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setInterfaceIpv6NdOffload(String iface, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "interface";
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6ndoffload";
            objArr[1] = iface;
            objArr[2] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addRoute(int netId, RouteInfo route) {
        modifyRoute("add", IElsaManager.EMPTY_PACKAGE + netId, route);
    }

    public void removeRoute(int netId, RouteInfo route) {
        modifyRoute("remove", IElsaManager.EMPTY_PACKAGE + netId, route);
    }

    private void modifyRoute(String action, String netId, RouteInfo route) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr = new Object[3];
        objArr[0] = "route";
        objArr[1] = action;
        objArr[2] = netId;
        Command cmd = new Command("network", objArr);
        cmd.appendArg(route.getInterface());
        cmd.appendArg(route.getDestination().toString());
        switch (route.getType()) {
            case 1:
                if (route.hasGateway()) {
                    cmd.appendArg(route.getGateway().getHostAddress());
                    break;
                }
                break;
            case 7:
                cmd.appendArg("unreachable");
                break;
            case 9:
                cmd.appendArg("throw");
                break;
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0034 A:{SYNTHETIC, Splitter: B:18:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002e A:{SYNTHETIC, Splitter: B:14:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0040 A:{SYNTHETIC, Splitter: B:25:0x0040} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> readRouteList(String filename) {
        Throwable th;
        FileInputStream fstream = null;
        ArrayList<String> list = new ArrayList();
        try {
            FileInputStream fstream2 = new FileInputStream(filename);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String s = br.readLine();
                    if (s != null && s.length() != 0) {
                        list.add(s);
                    } else if (fstream2 != null) {
                        try {
                            fstream2.close();
                        } catch (IOException e) {
                        }
                    }
                }
                if (fstream2 != null) {
                }
            } catch (IOException e2) {
                fstream = fstream2;
                if (fstream != null) {
                }
                return list;
            } catch (Throwable th2) {
                th = th2;
                fstream = fstream2;
                if (fstream != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e4) {
                }
            }
            return list;
        } catch (Throwable th3) {
            th = th3;
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return list;
    }

    public void setMtu(String iface, int mtu) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "setmtu";
            objArr[1] = iface;
            objArr[2] = Integer.valueOf(mtu);
            NativeDaemonEvent event = this.mConnector.execute("interface", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void shutdown() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", TAG);
        Slog.i(TAG, "Shutting down");
    }

    public boolean getIpForwardingEnabled() throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "status";
            NativeDaemonEvent event = this.mConnector.execute("ipfwd", objArr);
            event.checkCode(211);
            return event.getMessage().endsWith("enabled");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setIpForwardingEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "ipfwd";
            Object[] objArr = new Object[2];
            objArr[0] = enable ? "enable" : "disable";
            objArr[1] = "tethering";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void startTethering(String[] dhcpRange) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr = new Object[1];
        objArr[0] = "start";
        Command cmd = new Command("tether", objArr);
        int length = dhcpRange.length;
        while (i < length) {
            cmd.appendArg(dhcpRange[i]);
            i++;
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopTethering() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "stop";
            this.mConnector.execute("tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isTetheringStarted() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "status";
            NativeDaemonEvent event = this.mConnector.execute("tether", objArr);
            event.checkCode(210);
            return event.getMessage().endsWith("started");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void tetherInterface(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "interface";
            objArr[1] = "add";
            objArr[2] = iface;
            this.mConnector.execute("tether", objArr);
            List<RouteInfo> routes = new ArrayList();
            routes.add(new RouteInfo(getInterfaceConfig(iface).getLinkAddress(), null, iface));
            addInterfaceToLocalNetwork(iface, routes);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void untetherInterface(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "interface";
            objArr[1] = "remove";
            objArr[2] = iface;
            this.mConnector.execute("tether", objArr);
            removeInterfaceFromLocalNetwork(iface);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (Throwable th) {
            removeInterfaceFromLocalNetwork(iface);
        }
    }

    public String[] listTetheredInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "interface";
            objArr[1] = "list";
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", objArr), 111);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDnsForwarders(Network network, String[] dns) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        int netId = network != null ? network.netId : 0;
        Object[] objArr = new Object[3];
        objArr[0] = "dns";
        objArr[1] = "set";
        objArr[2] = Integer.valueOf(netId);
        Command cmd = new Command("tether", objArr);
        int length = dns.length;
        while (i < length) {
            cmd.appendArg(NetworkUtils.numericToInetAddress(dns[i]).getHostAddress());
            i++;
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String[] getDnsForwarders() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "dns";
            objArr[1] = "list";
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", objArr), 112);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private List<InterfaceAddress> excludeLinkLocal(List<InterfaceAddress> addresses) {
        ArrayList<InterfaceAddress> filtered = new ArrayList(addresses.size());
        for (InterfaceAddress ia : addresses) {
            if (!ia.getAddress().isLinkLocalAddress()) {
                filtered.add(ia);
            }
        }
        return filtered;
    }

    private void modifyInterfaceForward(boolean add, String fromIface, String toIface) {
        String str = "ipfwd";
        Object[] objArr = new Object[3];
        objArr[0] = add ? "add" : "remove";
        objArr[1] = fromIface;
        objArr[2] = toIface;
        try {
            this.mConnector.execute(new Command(str, objArr));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void startInterfaceForwarding(String fromIface, String toIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        modifyInterfaceForward(true, fromIface, toIface);
    }

    public void stopInterfaceForwarding(String fromIface, String toIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        modifyInterfaceForward(false, fromIface, toIface);
    }

    private void modifyNat(String action, String internalInterface, String externalInterface) throws SocketException {
        Object[] objArr = new Object[4];
        objArr[0] = action;
        objArr[1] = internalInterface;
        objArr[2] = externalInterface;
        objArr[3] = Integer.valueOf(0);
        try {
            this.mConnector.execute(new Command("nat", objArr));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableNat(String internalInterface, String externalInterface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            modifyNat("enable", internalInterface, externalInterface);
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
    }

    public void disableNat(String internalInterface, String externalInterface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            modifyNat("disable", internalInterface, externalInterface);
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
    }

    public String[] listTtys() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("list_ttys", new Object[0]), 113);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void attachPppd(String tty, String localAddr, String remoteAddr, String dns1Addr, String dns2Addr) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[6];
            objArr[0] = "attach";
            objArr[1] = tty;
            objArr[2] = NetworkUtils.numericToInetAddress(localAddr).getHostAddress();
            objArr[3] = NetworkUtils.numericToInetAddress(remoteAddr).getHostAddress();
            objArr[4] = NetworkUtils.numericToInetAddress(dns1Addr).getHostAddress();
            objArr[5] = NetworkUtils.numericToInetAddress(dns2Addr).getHostAddress();
            this.mConnector.execute("pppd", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void detachPppd(String tty) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "detach";
            objArr[1] = tty;
            this.mConnector.execute("pppd", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private void executeOrLogWithMessage(String command, Object[] args, int expectedResponseCode, String expectedResponseMessage, String logMsg) throws NativeDaemonConnectorException {
        NativeDaemonEvent event = this.mConnector.execute(command, args);
        if (event.getCode() != expectedResponseCode || !event.getMessage().equals(expectedResponseMessage)) {
            Log.e(TAG, logMsg + ": event = " + event);
        }
    }

    public void startAccessPoint(WifiConfiguration wifiConfig, String wlanIface) {
        Object[] args;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String logMsg = "startAccessPoint Error setting up softap";
        if (wifiConfig == null) {
            try {
                args = new Object[2];
                args[0] = "set";
                args[1] = wlanIface;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        args = new Object[7];
        args[0] = "set";
        args[1] = wlanIface;
        args[2] = wifiConfig.SSID;
        args[3] = OppoProcessManager.RESUME_REASON_BROADCAST_STR;
        args[4] = Integer.toString(wifiConfig.apChannel);
        args[5] = getSecurityType(wifiConfig);
        args[6] = new SensitiveArg(wifiConfig.preSharedKey);
        executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, logMsg);
        args = new Object[1];
        args[0] = "startap";
        executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, "startAccessPoint Error starting softap");
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        switch (wifiConfig.getAuthType()) {
            case 1:
                return "wpa-psk";
            case 4:
                return "wpa2-psk";
            default:
                return "open";
        }
    }

    public void wifiFirmwareReload(String wlanIface, String mode) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] args = new Object[3];
        args[0] = "fwreload";
        args[1] = wlanIface;
        args[2] = mode;
        String logMsg = "wifiFirmwareReload Error reloading " + wlanIface + " fw in " + mode + " mode";
        try {
            executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, logMsg);
            this.mConnector.waitForCallbacks();
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopAccessPoint(String wlanIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] args = new Object[1];
        args[0] = "stopap";
        try {
            executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, "stopAccessPoint Error stopping softap");
            wifiFirmwareReload(wlanIface, "STA");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setAccessPoint(WifiConfiguration wifiConfig, String wlanIface) {
        Object[] args;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String logMsg = "startAccessPoint Error setting up softap";
        if (wifiConfig == null) {
            try {
                args = new Object[2];
                args[0] = "set";
                args[1] = wlanIface;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        int clientNum = System.getInt(this.mContext.getContentResolver(), "wifi_hotspot_max_client_num", 8);
        String hiddenSSid = wifiConfig.hiddenSSID ? "hidden" : OppoProcessManager.RESUME_REASON_BROADCAST_STR;
        args = new Object[9];
        args[0] = "set";
        args[1] = wlanIface;
        args[2] = wifiConfig.SSID;
        args[3] = hiddenSSid;
        args[4] = Integer.valueOf(wifiConfig.channel);
        args[5] = getSecurityType(wifiConfig);
        args[6] = new SensitiveArg(wifiConfig.preSharedKey);
        args[7] = Integer.valueOf(wifiConfig.channelWidth);
        args[8] = Integer.valueOf(clientNum);
        executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, logMsg);
    }

    public void addIdleTimer(String iface, int timeout, final int type) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Adding idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            IdleTimerParams params = (IdleTimerParams) this.mActiveIdleTimers.get(iface);
            if (params != null) {
                params.networkCount++;
                return;
            }
            try {
                Object[] objArr = new Object[4];
                objArr[0] = "add";
                objArr[1] = iface;
                objArr[2] = Integer.toString(timeout);
                objArr[3] = Integer.toString(type);
                this.mConnector.execute("idletimer", objArr);
                this.mActiveIdleTimers.put(iface, new IdleTimerParams(timeout, type));
                if (ConnectivityManager.isNetworkTypeMobile(type)) {
                    this.mNetworkActive = false;
                }
                this.mDaemonHandler.post(new Runnable() {
                    public void run() {
                        NetworkManagementService.this.notifyInterfaceClassActivity(type, 3, SystemClock.elapsedRealtimeNanos(), -1, false);
                    }
                });
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeIdleTimer(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Removing idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            final IdleTimerParams params = (IdleTimerParams) this.mActiveIdleTimers.get(iface);
            if (params != null) {
                int i = params.networkCount - 1;
                params.networkCount = i;
                if (i <= 0) {
                    try {
                        Object[] objArr = new Object[4];
                        objArr[0] = "remove";
                        objArr[1] = iface;
                        objArr[2] = Integer.toString(params.timeout);
                        objArr[3] = Integer.toString(params.type);
                        this.mConnector.execute("idletimer", objArr);
                        this.mActiveIdleTimers.remove(iface);
                        this.mDaemonHandler.post(new Runnable() {
                            public void run() {
                                NetworkManagementService.this.notifyInterfaceClassActivity(params.type, 1, SystemClock.elapsedRealtimeNanos(), -1, false);
                            }
                        });
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public NetworkStats getNetworkStatsSummaryDev() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsSummaryDev();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsSummaryXt() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsSummaryXt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsDetail() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetail(-1, null, -1, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setInterfaceQuota(String iface, long quotaBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                try {
                    Object[] objArr = new Object[3];
                    objArr[0] = "setiquota";
                    objArr[1] = iface;
                    objArr[2] = Long.valueOf(quotaBytes);
                    this.mConnector.execute("bandwidth", objArr);
                    this.mActiveQuotas.put(iface, Long.valueOf(quotaBytes));
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
        }
    }

    public void removeInterfaceQuota(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveQuotas.containsKey(iface)) {
                    this.mActiveQuotas.remove(iface);
                    this.mActiveAlerts.remove(iface);
                    try {
                        Object[] objArr = new Object[2];
                        objArr[0] = "removeiquota";
                        objArr[1] = iface;
                        this.mConnector.execute("bandwidth", objArr);
                        return;
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public void setInterfaceAlert(String iface, long alertBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mBandwidthControlEnabled) {
            return;
        }
        if (this.mActiveQuotas.containsKey(iface)) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveAlerts.containsKey(iface)) {
                    throw new IllegalStateException("iface " + iface + " already has alert");
                }
                try {
                    Object[] objArr = new Object[3];
                    objArr[0] = "setinterfacealert";
                    objArr[1] = iface;
                    objArr[2] = Long.valueOf(alertBytes);
                    this.mConnector.execute("bandwidth", objArr);
                    this.mActiveAlerts.put(iface, Long.valueOf(alertBytes));
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            return;
        }
        throw new IllegalStateException("setting alert requires existing quota on iface");
    }

    public void removeInterfaceAlert(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveAlerts.containsKey(iface)) {
                    try {
                        Object[] objArr = new Object[2];
                        objArr[0] = "removeinterfacealert";
                        objArr[1] = iface;
                        this.mConnector.execute("bandwidth", objArr);
                        this.mActiveAlerts.remove(iface);
                        return;
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public void setGlobalAlert(long alertBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            try {
                Object[] objArr = new Object[2];
                objArr[0] = "setglobalalert";
                objArr[1] = Long.valueOf(alertBytes);
                this.mConnector.execute("bandwidth", objArr);
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x005b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setUidOnMeteredNetworkList(SparseBooleanArray quotaList, int uid, boolean blacklist, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            String chain = blacklist ? "naughtyapps" : "niceapps";
            String suffix = enable ? "add" : "remove";
            synchronized (this.mQuotaLock) {
                if (quotaList.get(uid, false) == enable) {
                    return;
                }
                try {
                    Object[] objArr = new Object[2];
                    objArr[0] = suffix + chain;
                    objArr[1] = Integer.valueOf(uid);
                    this.mConnector.execute("bandwidth", objArr);
                    if (enable) {
                        quotaList.put(uid, true);
                    } else {
                        quotaList.delete(uid);
                    }
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
        }
    }

    public void setUidMeteredNetworkBlacklist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(this.mUidRejectOnMetered, uid, true, enable);
    }

    public void setUidMeteredNetworkWhitelist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(this.mUidAllowOnMetered, uid, false, enable);
    }

    /* JADX WARNING: Missing block: B:17:0x004f, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setDataSaverModeEnabled(boolean enable) {
        if (DBG) {
            Log.d(TAG, "setDataSaverMode: " + enable);
        }
        synchronized (this.mQuotaLock) {
            if (this.mDataSaverMode == enable) {
                Log.w(TAG, "setDataSaverMode(): already " + this.mDataSaverMode);
                return true;
            }
            try {
                boolean changed = this.mNetdService.bandwidthEnableDataSaver(enable);
                if (changed) {
                    this.mDataSaverMode = enable;
                } else {
                    Log.w(TAG, "setDataSaverMode(" + enable + "): netd command silently failed");
                }
            } catch (RemoteException e) {
                Log.w(TAG, "setDataSaverMode(" + enable + "): netd command failed", e);
                return false;
            }
        }
    }

    public void setAllowOnlyVpnForUids(boolean add, UidRange[] uidRanges) throws ServiceSpecificException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mNetdService.networkRejectNonSecureVpn(add, uidRanges);
        } catch (ServiceSpecificException e) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + ")" + ": netd command failed", e);
            throw e;
        } catch (RemoteException e2) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + ")" + ": netd command failed", e2);
            throw e2.rethrowAsRuntimeException();
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUidCleartextNetworkPolicy(int uid, int policy) {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        }
        synchronized (this.mQuotaLock) {
            if (this.mUidCleartextPolicy.get(uid, 0) == policy) {
            } else if (this.mStrictEnabled) {
                String policyString;
                Object[] objArr;
                switch (policy) {
                    case 0:
                        policyString = "accept";
                    case 1:
                        policyString = "log";
                    case 2:
                        policyString = "reject";
                        objArr = new Object[3];
                        objArr[0] = "set_uid_cleartext_policy";
                        objArr[1] = Integer.valueOf(uid);
                        objArr[2] = policyString;
                        this.mConnector.execute("strict", objArr);
                        this.mUidCleartextPolicy.put(uid, policy);
                    default:
                        throw new IllegalArgumentException("Unknown policy " + policy);
                }
                try {
                    objArr = new Object[3];
                    objArr[0] = "set_uid_cleartext_policy";
                    objArr[1] = Integer.valueOf(uid);
                    objArr[2] = policyString;
                    this.mConnector.execute("strict", objArr);
                    this.mUidCleartextPolicy.put(uid, policy);
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                this.mUidCleartextPolicy.put(uid, policy);
            }
        }
    }

    public boolean isBandwidthControlEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return this.mBandwidthControlEnabled;
    }

    public NetworkStats getNetworkStatsUidDetail(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetail(uid, null, -1, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsTethering() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "gettetherstats";
            for (NativeDaemonEvent event : this.mConnector.executeForList("bandwidth", objArr)) {
                if (event.getCode() == 114) {
                    StringTokenizer tok = new StringTokenizer(event.getMessage());
                    String ifaceIn = tok.nextToken();
                    String ifaceOut = tok.nextToken();
                    NetworkStats.Entry entry = new NetworkStats.Entry();
                    entry.iface = ifaceOut;
                    entry.uid = -5;
                    entry.set = 0;
                    entry.tag = 0;
                    entry.rxBytes = Long.parseLong(tok.nextToken());
                    entry.rxPackets = Long.parseLong(tok.nextToken());
                    entry.txBytes = Long.parseLong(tok.nextToken());
                    entry.txPackets = Long.parseLong(tok.nextToken());
                    stats.combineValues(entry);
                }
            }
            return stats;
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("problem parsing tethering stats: " + event);
        } catch (NumberFormatException e2) {
            throw new IllegalStateException("problem parsing tethering stats: " + event);
        } catch (NativeDaemonConnectorException e3) {
            throw e3.rethrowAsParcelableException();
        }
    }

    public void setDnsConfigurationForNetwork(int netId, String[] servers, String domains) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        ContentResolver resolver = this.mContext.getContentResolver();
        int sampleValidity = Global.getInt(resolver, "dns_resolver_sample_validity_seconds", DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS);
        if (sampleValidity < 0 || sampleValidity > 65535) {
            Slog.w(TAG, "Invalid sampleValidity=" + sampleValidity + ", using default=" + DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS);
            sampleValidity = DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS;
        }
        int successThreshold = Global.getInt(resolver, "dns_resolver_success_threshold_percent", 25);
        if (successThreshold < 0 || successThreshold > 100) {
            Slog.w(TAG, "Invalid successThreshold=" + successThreshold + ", using default=" + 25);
            successThreshold = 25;
        }
        int minSamples = Global.getInt(resolver, "dns_resolver_min_samples", 8);
        int maxSamples = Global.getInt(resolver, "dns_resolver_max_samples", 64);
        if (minSamples < 0 || minSamples > maxSamples || maxSamples > 64) {
            Slog.w(TAG, "Invalid sample count (min, max)=(" + minSamples + ", " + maxSamples + "), using default=(" + 8 + ", " + 64 + ")");
            minSamples = 8;
            maxSamples = 64;
        }
        String[] domainStrs = domains == null ? new String[0] : domains.split(" ");
        int[] params = new int[4];
        params[0] = sampleValidity;
        params[1] = successThreshold;
        params[2] = minSamples;
        params[3] = maxSamples;
        try {
            this.mNetdService.setResolverConfiguration(netId, servers, domainStrs, params);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDnsServersForNetwork(int netId, String[] servers, String domains) {
        Command cmd;
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr;
        if (servers.length > 0) {
            String str = "resolver";
            objArr = new Object[3];
            objArr[0] = "setnetdns";
            objArr[1] = Integer.valueOf(netId);
            if (domains == null) {
                domains = IElsaManager.EMPTY_PACKAGE;
            }
            objArr[2] = domains;
            cmd = new Command(str, objArr);
            int length = servers.length;
            while (i < length) {
                InetAddress a = NetworkUtils.numericToInetAddress(servers[i]);
                if (!a.isAnyLocalAddress()) {
                    cmd.appendArg(a.getHostAddress());
                }
                i++;
            }
        } else {
            objArr = new Object[2];
            objArr[0] = "clearnetdns";
            objArr[1] = Integer.valueOf(netId);
            cmd = new Command("resolver", objArr);
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = SoundModelContract.KEY_USERS;
        argv[1] = "add";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void removeVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = SoundModelContract.KEY_USERS;
        argv[1] = "remove";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void setFirewallEnabled(boolean enabled) {
        enforceSystemUid();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "firewall";
            Object[] objArr = new Object[2];
            objArr[0] = "enable";
            objArr[1] = enabled ? "whitelist" : "blacklist";
            nativeDaemonConnector.execute(str, objArr);
            this.mFirewallEnabled = enabled;
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isFirewallEnabled() {
        enforceSystemUid();
        return this.mFirewallEnabled;
    }

    public void setFirewallInterfaceRule(String iface, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "set_interface_rule";
            objArr[1] = iface;
            objArr[2] = rule;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setFirewallEgressSourceRule(String addr, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "set_egress_source_rule";
            objArr[1] = addr;
            objArr[2] = rule;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setFirewallEgressDestRule(String addr, int port, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "set_egress_dest_rule";
            objArr[1] = addr;
            objArr[2] = Integer.valueOf(port);
            objArr[3] = rule;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0070 A:{Splitter: B:12:0x003d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:25:0x0070, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x0071, code:
            android.util.Slog.e(TAG, "Error closing sockets after enabling chain " + r14 + ": " + r0);
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void closeSocketsForFirewallChainLocked(int chain, String chainName) {
        UidRange[] ranges;
        int[] exemptUids;
        SparseIntArray rules = getUidFirewallRules(chain);
        int numUids = 0;
        int i;
        if (getFirewallType(chain) == 0) {
            ranges = new UidRange[1];
            ranges[0] = new UidRange(10000, Integer.MAX_VALUE);
            exemptUids = new int[rules.size()];
            for (i = 0; i < exemptUids.length; i++) {
                if (rules.valueAt(i) == 1) {
                    exemptUids[numUids] = rules.keyAt(i);
                    numUids++;
                }
            }
            if (numUids != exemptUids.length) {
                exemptUids = Arrays.copyOf(exemptUids, numUids);
            }
        } else {
            ranges = new UidRange[rules.size()];
            for (i = 0; i < ranges.length; i++) {
                if (rules.valueAt(i) == 2) {
                    int uid = rules.keyAt(i);
                    ranges[numUids] = new UidRange(uid, uid);
                    numUids++;
                }
            }
            if (numUids != ranges.length) {
                ranges = (UidRange[]) Arrays.copyOf(ranges, numUids);
            }
            exemptUids = new int[0];
        }
        try {
            this.mNetdService.socketDestroy(ranges, exemptUids);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0060 A:{SYNTHETIC, Splitter: B:29:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0060 A:{SYNTHETIC, Splitter: B:29:0x0060} */
    /* JADX WARNING: Missing block: B:35:0x0082, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFirewallChainEnabled(int chain, boolean enable) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            if (this.mFirewallChainStates.get(chain) == enable) {
            } else if (this.mFirewallChainStates.indexOfKey(chain) >= 0 || enable) {
                String chainName;
                Object[] objArr;
                this.mFirewallChainStates.put(chain, enable);
                String operation = enable ? "enable_chain" : "disable_chain";
                switch (chain) {
                    case 1:
                        chainName = "dozable";
                    case 2:
                        chainName = "standby";
                        objArr = new Object[2];
                        objArr[0] = operation;
                        objArr[1] = chainName;
                        this.mConnector.execute("firewall", objArr);
                        if (enable) {
                            if (DBG) {
                                Slog.d(TAG, "Closing sockets after enabling chain " + chainName);
                            }
                            closeSocketsForFirewallChainLocked(chain, chainName);
                            break;
                        }
                        break;
                    case 3:
                        chainName = "powersave";
                        objArr = new Object[2];
                        objArr[0] = operation;
                        objArr[1] = chainName;
                        this.mConnector.execute("firewall", objArr);
                        if (enable) {
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Bad child chain: " + chain);
                }
                try {
                    objArr = new Object[2];
                    objArr[0] = operation;
                    objArr[1] = chainName;
                    this.mConnector.execute("firewall", objArr);
                    if (enable) {
                    }
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
        }
    }

    private int getFirewallType(int chain) {
        int i = 0;
        switch (chain) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 0;
            default:
                if (!isFirewallEnabled()) {
                    i = 1;
                }
                return i;
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFirewallUidRules(int chain, int[] uids, int[] rules) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            int index;
            int uid;
            SparseIntArray uidFirewallRules = getUidFirewallRules(chain);
            SparseIntArray newRules = new SparseIntArray();
            for (index = uids.length - 1; index >= 0; index--) {
                uid = uids[index];
                int rule = rules[index];
                updateFirewallUidRuleLocked(chain, uid, rule);
                newRules.put(uid, rule);
            }
            SparseIntArray rulesToRemove = new SparseIntArray();
            for (index = uidFirewallRules.size() - 1; index >= 0; index--) {
                uid = uidFirewallRules.keyAt(index);
                if (newRules.indexOfKey(uid) < 0) {
                    rulesToRemove.put(uid, 0);
                }
            }
            for (index = rulesToRemove.size() - 1; index >= 0; index--) {
                updateFirewallUidRuleLocked(chain, rulesToRemove.keyAt(index), 0);
            }
            switch (chain) {
                case 1:
                    this.mNetdService.firewallReplaceUidChain("fw_dozable", true, uids);
                    break;
                case 2:
                    this.mNetdService.firewallReplaceUidChain("fw_standby", false, uids);
                    break;
                case 3:
                    this.mNetdService.firewallReplaceUidChain("fw_powersave", true, uids);
                    break;
                default:
                    try {
                        Slog.d(TAG, "setFirewallUidRules() called on invalid chain: " + chain);
                        break;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Error flushing firewall chain " + chain, e);
                        break;
                    }
            }
        }
    }

    public void setFirewallUidRule(int chain, int uid, int rule) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            setFirewallUidRuleLocked(chain, uid, rule);
        }
    }

    private void setFirewallUidRuleLocked(int chain, int uid, int rule) {
        if (updateFirewallUidRuleLocked(chain, uid, rule)) {
            try {
                Object[] objArr = new Object[4];
                objArr[0] = "set_uid_rule";
                objArr[1] = getFirewallChainName(chain);
                objArr[2] = Integer.valueOf(uid);
                objArr[3] = getFirewallRuleName(chain, rule);
                this.mConnector.execute("firewall", objArr);
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    private boolean updateFirewallUidRuleLocked(int chain, int uid, int rule) {
        boolean z = false;
        SparseIntArray uidFirewallRules = getUidFirewallRules(chain);
        int oldUidFirewallRule = uidFirewallRules.get(uid, 0);
        if (DBG) {
            Slog.d(TAG, "oldRule = " + oldUidFirewallRule + ", newRule=" + rule + " for uid=" + uid + " on chain " + chain);
        }
        if (oldUidFirewallRule == rule) {
            if (DBG) {
                Slog.d(TAG, "!!!!! Skipping change");
            }
            return false;
        }
        String ruleName = getFirewallRuleName(chain, rule);
        String oldRuleName = getFirewallRuleName(chain, oldUidFirewallRule);
        if (rule == 0) {
            uidFirewallRules.delete(uid);
        } else {
            uidFirewallRules.put(uid, rule);
        }
        if (!ruleName.equals(oldRuleName)) {
            z = true;
        }
        return z;
    }

    private String getFirewallRuleName(int chain, int rule) {
        if (getFirewallType(chain) == 0) {
            if (rule == 1) {
                return "allow";
            }
            return "deny";
        } else if (rule == 2) {
            return "deny";
        } else {
            return "allow";
        }
    }

    private SparseIntArray getUidFirewallRules(int chain) {
        switch (chain) {
            case 0:
                return this.mUidFirewallRules;
            case 1:
                return this.mUidFirewallDozableRules;
            case 2:
                return this.mUidFirewallStandbyRules;
            case 3:
                return this.mUidFirewallPowerSaveRules;
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    public String getFirewallChainName(int chain) {
        switch (chain) {
            case 0:
                return "none";
            case 1:
                return "dozable";
            case 2:
                return "standby";
            case 3:
                return "powersave";
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    private static void enforceSystemUid() {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only available to AID_SYSTEM");
        }
    }

    public void startClatd(String interfaceName) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "start";
            objArr[1] = interfaceName;
            this.mConnector.execute("clatd", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopClatd(String interfaceName) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "stop";
            objArr[1] = interfaceName;
            this.mConnector.execute("clatd", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isClatdStarted(String interfaceName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "status";
            objArr[1] = interfaceName;
            NativeDaemonEvent event = this.mConnector.execute("clatd", objArr);
            event.checkCode(NetdResponseCode.ClatdStatusResult);
            return event.getMessage().endsWith("started");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void registerNetworkActivityListener(INetworkActivityListener listener) {
        this.mNetworkActivityListeners.register(listener);
    }

    public void unregisterNetworkActivityListener(INetworkActivityListener listener) {
        this.mNetworkActivityListeners.unregister(listener);
    }

    public boolean isNetworkActive() {
        boolean isEmpty;
        synchronized (this.mNetworkActivityListeners) {
            isEmpty = !this.mNetworkActive ? this.mActiveIdleTimers.isEmpty() : true;
        }
        return isEmpty;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A:{Splitter: B:2:0x0009, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportNetworkActive() {
        int length = this.mNetworkActivityListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                ((INetworkActivityListener) this.mNetworkActivityListeners.getBroadcastItem(i)).onNetworkActive();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mNetworkActivityListeners.finishBroadcast();
            }
        }
        this.mNetworkActivityListeners.finishBroadcast();
    }

    public void monitor() {
        if (this.mConnector != null) {
            this.mConnector.monitor();
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.println("NetworkManagementService NativeDaemonConnector Log:");
        this.mConnector.dump(fd, pw, args);
        pw.println();
        pw.print("Bandwidth control enabled: ");
        pw.println(this.mBandwidthControlEnabled);
        pw.print("mMobileActivityFromRadio=");
        pw.print(this.mMobileActivityFromRadio);
        pw.print(" mLastPowerStateFromRadio=");
        pw.println(this.mLastPowerStateFromRadio);
        pw.print("mNetworkActive=");
        pw.println(this.mNetworkActive);
        synchronized (this.mQuotaLock) {
            pw.print("Active quota ifaces: ");
            pw.println(this.mActiveQuotas.toString());
            pw.print("Active alert ifaces: ");
            pw.println(this.mActiveAlerts.toString());
            pw.print("Data saver mode: ");
            pw.println(this.mDataSaverMode);
            dumpUidRuleOnQuotaLocked(pw, "blacklist", this.mUidRejectOnMetered);
            dumpUidRuleOnQuotaLocked(pw, "whitelist", this.mUidAllowOnMetered);
        }
        synchronized (this.mUidFirewallRules) {
            dumpUidFirewallRule(pw, IElsaManager.EMPTY_PACKAGE, this.mUidFirewallRules);
        }
        pw.print("UID firewall standby chain enabled: ");
        pw.println(this.mFirewallChainStates.get(2));
        synchronized (this.mUidFirewallStandbyRules) {
            dumpUidFirewallRule(pw, "standby", this.mUidFirewallStandbyRules);
        }
        pw.print("UID firewall dozable chain enabled: ");
        pw.println(this.mFirewallChainStates.get(1));
        synchronized (this.mUidFirewallDozableRules) {
            dumpUidFirewallRule(pw, "dozable", this.mUidFirewallDozableRules);
        }
        pw.println("UID firewall powersave chain enabled: " + this.mFirewallChainStates.get(3));
        synchronized (this.mUidFirewallPowerSaveRules) {
            dumpUidFirewallRule(pw, "powersave", this.mUidFirewallPowerSaveRules);
        }
        synchronized (this.mIdleTimerLock) {
            pw.println("Idle timers:");
            for (Entry<String, IdleTimerParams> ent : this.mActiveIdleTimers.entrySet()) {
                pw.print("  ");
                pw.print((String) ent.getKey());
                pw.println(":");
                IdleTimerParams params = (IdleTimerParams) ent.getValue();
                pw.print("    timeout=");
                pw.print(params.timeout);
                pw.print(" type=");
                pw.print(params.type);
                pw.print(" networkCount=");
                pw.println(params.networkCount);
            }
        }
        pw.print("Firewall enabled: ");
        pw.println(this.mFirewallEnabled);
        pw.print("Netd service status: ");
        if (this.mNetdService == null) {
            pw.println("disconnected");
            return;
        }
        try {
            pw.println(this.mNetdService.isAlive() ? "alive" : "dead");
        } catch (RemoteException e) {
            pw.println("unreachable");
        }
    }

    private void dumpUidRuleOnQuotaLocked(PrintWriter pw, String name, SparseBooleanArray list) {
        pw.print("UID bandwith control ");
        pw.print(name);
        pw.print(" rule: [");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            pw.print(list.keyAt(i));
            if (i < size - 1) {
                pw.print(",");
            }
        }
        pw.println("]");
    }

    private void dumpUidFirewallRule(PrintWriter pw, String name, SparseIntArray rules) {
        pw.print("UID firewall ");
        pw.print(name);
        pw.print(" rule: [");
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            pw.print(rules.keyAt(i));
            pw.print(":");
            pw.print(rules.valueAt(i));
            if (i < size - 1) {
                pw.print(",");
            }
        }
        pw.println("]");
    }

    public void createPhysicalNetwork(int netId, String permission) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr;
        if (permission != null) {
            try {
                objArr = new Object[3];
                objArr[0] = "create";
                objArr[1] = Integer.valueOf(netId);
                objArr[2] = permission;
                this.mConnector.execute("network", objArr);
                return;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        objArr = new Object[2];
        objArr[0] = "create";
        objArr[1] = Integer.valueOf(netId);
        this.mConnector.execute("network", objArr);
    }

    public void createVirtualNetwork(int netId, boolean hasDNS, boolean secure) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "network";
            Object[] objArr = new Object[5];
            objArr[0] = "create";
            objArr[1] = Integer.valueOf(netId);
            objArr[2] = "vpn";
            objArr[3] = hasDNS ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
            objArr[4] = secure ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void removeNetwork(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "destroy";
            objArr[1] = Integer.valueOf(netId);
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addInterfaceToNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("add", IElsaManager.EMPTY_PACKAGE + netId, iface);
    }

    public void removeInterfaceFromNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("remove", IElsaManager.EMPTY_PACKAGE + netId, iface);
    }

    private void modifyInterfaceInNetwork(String action, String netId, String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "interface";
            objArr[1] = action;
            objArr[2] = netId;
            objArr[3] = iface;
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr = new Object[5];
        objArr[0] = "route";
        objArr[1] = "legacy";
        objArr[2] = Integer.valueOf(uid);
        objArr[3] = "add";
        objArr[4] = Integer.valueOf(netId);
        Command cmd = new Command("network", objArr);
        LinkAddress la = routeInfo.getDestinationLinkAddress();
        cmd.appendArg(routeInfo.getInterface());
        cmd.appendArg(la.getAddress().getHostAddress() + "/" + la.getPrefixLength());
        if (routeInfo.hasGateway()) {
            cmd.appendArg(routeInfo.getGateway().getHostAddress());
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDefaultNetId(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "default";
            objArr[1] = "set";
            objArr[2] = Integer.valueOf(netId);
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public NetworkStats getNetworkStatsUidDetailWithPids(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetailWithPids(-1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clearDefaultNetId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "default";
            objArr[1] = "clear";
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setNetworkPermission(int netId, String permission) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr;
        if (permission != null) {
            try {
                objArr = new Object[5];
                objArr[0] = "permission";
                objArr[1] = "network";
                objArr[2] = "set";
                objArr[3] = permission;
                objArr[4] = Integer.valueOf(netId);
                this.mConnector.execute("network", objArr);
                return;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        objArr = new Object[4];
        objArr[0] = "permission";
        objArr[1] = "network";
        objArr[2] = "clear";
        objArr[3] = Integer.valueOf(netId);
        this.mConnector.execute("network", objArr);
    }

    public void setPermission(String permission, int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[14];
        argv[0] = "permission";
        argv[1] = "user";
        argv[2] = "set";
        argv[3] = permission;
        int argc = 4;
        for (int i = 0; i < uids.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 4;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void clearPermission(int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = "permission";
        argv[1] = "user";
        argv[2] = "clear";
        int argc = 3;
        for (int i = 0; i < uids.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void allowProtect(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "protect";
            objArr[1] = "allow";
            objArr[2] = Integer.valueOf(uid);
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void denyProtect(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "protect";
            objArr[1] = "deny";
            objArr[2] = Integer.valueOf(uid);
            this.mConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addInterfaceToLocalNetwork(String iface, List<RouteInfo> routes) {
        modifyInterfaceInNetwork("add", "local", iface);
        for (RouteInfo route : routes) {
            if (!route.isDefaultRoute()) {
                modifyRoute("add", "local", route);
            }
        }
    }

    public void removeInterfaceFromLocalNetwork(String iface) {
        modifyInterfaceInNetwork("remove", "local", iface);
    }

    public boolean getIpv6ForwardingEnabled() throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "status";
            NativeDaemonEvent event = this.mConnector.execute("ipv6fwd", objArr);
            event.checkCode(211);
            return event.getMessage().endsWith("enabled");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setIpv6ForwardingEnabled(boolean enable) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "ipv6fwd";
            Object[] objArr = new Object[1];
            String str2 = "%sable";
            Object[] objArr2 = new Object[1];
            objArr2[0] = enable ? "en" : "dis";
            objArr[0] = String.format(str2, objArr2);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private void modifyNatIpv6(String action, String internalInterface, String externalInterface) throws SocketException {
        Object[] objArr = new Object[4];
        objArr[0] = action;
        objArr[1] = internalInterface;
        objArr[2] = externalInterface;
        objArr[3] = Integer.valueOf(0);
        try {
            this.mConnector.execute(new Command("IPv6Tether", objArr));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableNatIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "enableNatIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            modifyNatIpv6("enable", internalInterface, externalInterface);
        } catch (SocketException e) {
            Log.e(TAG, "enableNatIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for enabling Ipv6 NAT interface");
        }
    }

    public void setRouteIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "setRouteIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "setroute";
            objArr[1] = internalInterface;
            objArr[2] = externalInterface;
            this.mConnector.execute("IPv6Tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            Log.e(TAG, "setRouteIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for setRouteIpv6");
        }
    }

    public void clearRouteIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "clearRouteIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "clearroute";
            objArr[1] = internalInterface;
            objArr[2] = externalInterface;
            this.mConnector.execute("IPv6Tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            Log.e(TAG, "clearRouteIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for clearRouteIpv6");
        }
    }

    public void setSourceRouteIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "setSourceRouteIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "setsroute";
            objArr[1] = internalInterface;
            objArr[2] = externalInterface;
            this.mConnector.execute("IPv6Tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            Log.e(TAG, "setSourceRouteIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for setSourceRouteIpv6");
        }
    }

    public void clearSourceRouteIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "clearSourceRouteIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "clearsroute";
            objArr[1] = internalInterface;
            objArr[2] = externalInterface;
            this.mConnector.execute("IPv6Tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            Log.e(TAG, "clearSourceRouteIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for clearSourceRouteIpv6");
        }
    }

    public void disableNatIpv6(String internalInterface, String externalInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Log.d(TAG, "disableNatIpv6(" + internalInterface + ", " + externalInterface + ")");
        }
        try {
            modifyNatIpv6("disable", internalInterface, externalInterface);
        } catch (SocketException e) {
            Log.e(TAG, "disableNatIpv6 got Exception " + e.toString());
            throw new IllegalStateException("Unable to communicate to native daemon for disabling Ipv6 NAT interface");
        }
    }

    public void setFirewallEgressProtoRule(String proto, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "set_egress_proto_rule";
            objArr[1] = proto;
            objArr[2] = rule;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDhcpv6Enabled(boolean enable, String ifc) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_GPRS, "NetworkManagementService");
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "IPv6Tether";
            Object[] objArr = new Object[2];
            String str2 = "%s";
            Object[] objArr2 = new Object[1];
            objArr2[0] = enable ? "add" : "remove";
            objArr[0] = String.format(str2, objArr2);
            objArr[1] = ifc;
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableUdpForwarding(boolean enabled, String internalInterface, String externalInterface, String ipAddr) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr;
        if (enabled) {
            try {
                objArr = new Object[4];
                objArr[0] = "set_udp_forwarding";
                objArr[1] = internalInterface;
                objArr[2] = externalInterface;
                objArr[3] = ipAddr;
                this.mConnector.execute("firewall", objArr);
                return;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        objArr = new Object[3];
        objArr[0] = "clear_udp_forwarding";
        objArr[1] = internalInterface;
        objArr[2] = externalInterface;
        this.mConnector.execute("firewall", objArr);
    }

    public void getUsbClient(String iface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "get_usb_client";
            objArr[1] = iface;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private boolean isWifi(String iface) {
        for (String regex : this.mContext.getResources().getStringArray(17235991)) {
            if (iface.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public void setInterfaceThrottle(String iface, int rxKbps, int txKbps) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "setthrottle";
            objArr[1] = iface;
            objArr[2] = Integer.valueOf(rxKbps);
            objArr[3] = Integer.valueOf(txKbps);
            this.mConnector.execute("interface", objArr);
            if (isWifi(iface)) {
                Secure.putInt(this.mContext.getContentResolver(), "interface_throttle_rx_value", rxKbps);
                Secure.putInt(this.mContext.getContentResolver(), "interface_throttle_tx_value", txKbps);
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private int getInterfaceThrottle(String iface, boolean rx) {
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "interface";
            Object[] objArr = new Object[3];
            objArr[0] = "getthrottle";
            objArr[1] = iface;
            objArr[2] = rx ? "rx" : "tx";
            NativeDaemonEvent event = nativeDaemonConnector.execute(str, objArr);
            if (rx) {
                event.checkCode(NetdResponseCode.InterfaceRxThrottleResult);
            } else {
                event.checkCode(NetdResponseCode.InterfaceTxThrottleResult);
            }
            try {
                return Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("unexpected response:" + event);
            }
        } catch (NativeDaemonConnectorException e2) {
            throw e2.rethrowAsParcelableException();
        }
    }

    public int getInterfaceRxThrottle(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return getInterfaceThrottle(iface, true);
    }

    public int getInterfaceTxThrottle(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return getInterfaceThrottle(iface, false);
    }

    public void setFirewallUidChainRule(int uid, int networkType, boolean allow) {
        String MOBILE = "mobile";
        String WIFI = "wifi";
        String rule = allow ? "allow" : "deny";
        String chain = networkType == 1 ? "wifi" : "mobile";
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "set_uid_fw_rule";
            objArr[1] = Integer.valueOf(uid);
            objArr[2] = chain;
            objArr[3] = rule;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearFirewallChain(String chain) {
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "clear_fw_chain";
            objArr[1] = chain;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void disablePPPOE() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "stop";
            this.mConnector.execute("pppoectl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String[] getSipInfo(String iface, String service, String protocol) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Log.e(TAG, "getSipInfo:" + iface + " " + service + " " + protocol);
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "getsip";
            objArr[1] = iface;
            objArr[2] = service;
            objArr[3] = protocol;
            NativeDaemonEvent event = this.mConnector.execute("NetInfo", objArr);
            event.checkCode(NetdResponseCode.NetInfoSipResult);
            ArrayList<String> result = new ArrayList();
            StringTokenizer st = new StringTokenizer(event.getMessage());
            while (st.hasMoreTokens()) {
                result.add(st.nextToken(" "));
            }
            if (result.isEmpty()) {
                throw new IllegalStateException("Got an empty sipinfo response");
            }
            Log.e(TAG, "getSipInfo result:" + result);
            return (String[]) result.toArray(new String[result.size()]);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearSipInfo(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "clearsip";
            objArr[1] = iface;
            this.mConnector.execute("NetInfo", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearIotFirewall() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "clear_nsiot_firewall";
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setIotFirewall() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "set_nsiot_firewall";
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearVolteIotFirewall(String ifc) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "clear_volte_nsiot_firewall";
            objArr[1] = ifc;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setVolteIotFirewall(String ifc) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "set_volte_nsiot_firewall";
            objArr[1] = ifc;
            this.mConnector.execute("firewall", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addBridge(String bridgeInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "addbr";
            objArr[1] = bridgeInterface;
            this.mConnector.execute("brctl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void deleteBridge(String bridgeInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "delbr";
            objArr[1] = bridgeInterface;
            this.mConnector.execute("brctl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addBridgeInterface(String bridgeInterface, String portInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "addif";
            objArr[1] = bridgeInterface;
            objArr[2] = portInterface;
            this.mConnector.execute("brctl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void deleteBridgeInterface(String bridgeInterface, String portInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "delif";
            objArr[1] = bridgeInterface;
            objArr[2] = portInterface;
            this.mConnector.execute("brctl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearBridgeMac(String bridgeInterface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "clear";
            objArr[1] = DecryptTool.UNLOCK_TYPE_MAC;
            objArr[2] = bridgeInterface;
            this.mConnector.execute("brctl", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public int removeRoutesFromLocalNetwork(List<RouteInfo> routes) {
        int failures = 0;
        for (RouteInfo route : routes) {
            try {
                modifyRoute("remove", "local", route);
            } catch (IllegalStateException e) {
                failures++;
            }
        }
        return failures;
    }
}
