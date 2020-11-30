package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InetAddresses;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientUtil;
import android.net.shared.ProvisioningConfiguration;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.display.OppoWifiDisplayUsageHelper;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.OppoWifiP2pStats;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiController;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.WifiAsyncChannel;
import com.android.server.wifi.util.WifiHandler;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.mediatek.server.wifi.MtkSoftApUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WifiP2pServiceImpl extends IWifiP2pManager.Stub {
    private static final String ANONYMIZED_DEVICE_ADDRESS = "02:00:00:00:00:00";
    public static final int AUTO_ACCEPT_INVITATION_TIME_OUT = 143379;
    private static final int BASE = 143360;
    public static final int BLOCK_DISCOVERY = 143375;
    private static boolean DBG = false;
    public static final int DISABLED = 0;
    public static final int DISABLE_P2P = 143377;
    public static final int DISABLE_P2P_TIMED_OUT = 143366;
    private static final int DISABLE_P2P_WAIT_TIME_MS = 5000;
    public static final int DISCONNECT_WIFI_REQUEST = 143372;
    public static final int DISCONNECT_WIFI_RESPONSE = 143373;
    private static final int DISCOVER_TIMEOUT_S = 120;
    private static final int DROP_WIFI_USER_ACCEPT = 143364;
    private static final int DROP_WIFI_USER_REJECT = 143365;
    private static final String EMPTY_DEVICE_ADDRESS = "00:00:00:00:00:00";
    public static final int ENABLED = 1;
    public static final int ENABLE_P2P = 143376;
    private static final Boolean FORM_GROUP = false;
    public static final int GROUP_CREATING_TIMED_OUT = 143361;
    private static final int GROUP_CREATING_WAIT_TIME_MS = 120000;
    private static final int GROUP_IDLE_TIME_S = 10;
    private static final int IPC_DHCP_RESULTS = 143392;
    private static final int IPC_POST_DHCP_ACTION = 143391;
    private static final int IPC_PRE_DHCP_ACTION = 143390;
    private static final int IPC_PROVISIONING_FAILURE = 143394;
    private static final int IPC_PROVISIONING_SUCCESS = 143393;
    private static final Boolean JOIN_GROUP = true;
    private static final String NETWORKTYPE = "WIFI_P2P";
    private static final Boolean NO_RELOAD = false;
    public static final int P2P_CONNECTION_CHANGED = 143371;
    private static final int P2P_COUNTRYCODE_CHNAGED = 143395;
    public static final int P2P_FIND_SPECIAL_FREQ_TIME_OUT = 143380;
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    public static final int PEER_CONNECTION_USER_CONFIRM = 143367;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    private static final String[] RECEIVER_PERMISSIONS_FOR_BROADCAST = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"};
    private static final Boolean RELOAD = true;
    public static final int REMOVE_CLIENT_INFO = 143378;
    private static final String SERVER_ADDRESS = "192.168.49.1";
    public static final int SET_MIRACAST_MODE = 143374;
    private static final String TAG = "WifiP2pService";
    private static int sDisableP2pTimeoutIndex = 0;
    private static int sGroupCreatingTimeoutIndex = 0;
    private boolean mAutonomousGroup;
    private Map<IBinder, Messenger> mClientChannelList = new HashMap();
    private ClientHandler mClientHandler;
    private HashMap<Messenger, ClientInfo> mClientInfoList = new HashMap<>();
    private Context mContext;
    private final Map<IBinder, DeathHandlerData> mDeathDataByBinder = new HashMap();
    private DhcpResults mDhcpResults;
    private boolean mDiscoveryBlocked;
    private boolean mDiscoveryPostponed = false;
    private boolean mDiscoveryStarted;
    private String mExternalAddress = EMPTY_DEVICE_ADDRESS;
    private int mFastConTriggeredNum = 0;
    private int mFindTimes = 0;
    private boolean mFoundTargetDevice = false;
    private FrameworkFacade mFrameworkFacade;
    private String mIntentToConnectPeer = "";
    private IIpClient mIpClient;
    private int mIpClientStartIndex = 0;
    private boolean mIsExternalShareConnect = false;
    private boolean mJoinExistingGroup;
    private LocationManager mLocationManager;
    private Object mLock = new Object();
    private NetworkInfo mNetworkInfo;
    INetworkManagementService mNwService;
    private OppoWifiP2pConnectionHelper mOppoWifiP2pConnectionHelper;
    private OppoWifiP2pNotifyService mOppoWifiP2pNotifyService;
    private OppoWifiP2pStats mOppoWifiP2pStats;
    private int mP2pConnectFreq = 0;
    private P2pStateMachine mP2pStateMachine;
    private final boolean mP2pSupported;
    private AsyncChannel mReplyChannel = new WifiAsyncChannel(TAG);
    private String mServiceDiscReqId;
    private byte mServiceTransactionId = 0;
    private boolean mTemporarilyDisconnectedWifi = false;
    private WifiP2pDevice mThisDevice = new WifiP2pDevice();
    private boolean mVerboseLoggingEnabled = false;
    private AsyncChannel mWifiChannel;
    private OppoWifiDisplayUsageHelper mWifiDisplayUsageHelper = new OppoWifiDisplayUsageHelper();
    private WifiInjector mWifiInjector;
    private WifiP2pMetrics mWifiP2pMetrics;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private long mp2pStartedTime = 0;

    static /* synthetic */ byte access$12104(WifiP2pServiceImpl x0) {
        byte b = (byte) (x0.mServiceTransactionId + 1);
        x0.mServiceTransactionId = b;
        return b;
    }

    static /* synthetic */ int access$4204() {
        int i = sDisableP2pTimeoutIndex + 1;
        sDisableP2pTimeoutIndex = i;
        return i;
    }

    static /* synthetic */ int access$6108(WifiP2pServiceImpl x0) {
        int i = x0.mFindTimes;
        x0.mFindTimes = i + 1;
        return i;
    }

    static /* synthetic */ int access$9104() {
        int i = sGroupCreatingTimeoutIndex + 1;
        sGroupCreatingTimeoutIndex = i;
        return i;
    }

    public enum P2pStatus {
        SUCCESS,
        INFORMATION_IS_CURRENTLY_UNAVAILABLE,
        INCOMPATIBLE_PARAMETERS,
        LIMIT_REACHED,
        INVALID_PARAMETER,
        UNABLE_TO_ACCOMMODATE_REQUEST,
        PREVIOUS_PROTOCOL_ERROR,
        NO_COMMON_CHANNEL,
        UNKNOWN_P2P_GROUP,
        BOTH_GO_INTENT_15,
        INCOMPATIBLE_PROVISIONING_METHOD,
        REJECTED_BY_USER,
        UNKNOWN;

        public static P2pStatus valueOf(int error) {
            switch (error) {
                case 0:
                    return SUCCESS;
                case 1:
                    return INFORMATION_IS_CURRENTLY_UNAVAILABLE;
                case 2:
                    return INCOMPATIBLE_PARAMETERS;
                case 3:
                    return LIMIT_REACHED;
                case 4:
                    return INVALID_PARAMETER;
                case 5:
                    return UNABLE_TO_ACCOMMODATE_REQUEST;
                case 6:
                    return PREVIOUS_PROTOCOL_ERROR;
                case 7:
                    return NO_COMMON_CHANNEL;
                case 8:
                    return UNKNOWN_P2P_GROUP;
                case 9:
                    return BOTH_GO_INTENT_15;
                case 10:
                    return INCOMPATIBLE_PROVISIONING_METHOD;
                case 11:
                    return REJECTED_BY_USER;
                default:
                    return UNKNOWN;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        @Override // com.android.server.wifi.util.WifiHandler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 139265:
                case 139268:
                case 139274:
                case 139280:
                case 139283:
                case 139285:
                case 139287:
                case 139292:
                case 139295:
                case 139298:
                case 139301:
                case 139304:
                case 139307:
                case 139310:
                case 139315:
                case 139318:
                case 139321:
                case 139323:
                case 139326:
                case 139329:
                case 139332:
                case 139335:
                case 139346:
                case 139349:
                case 139351:
                case 139354:
                case 139356:
                case 139358:
                case 139360:
                case 139361:
                case 139366:
                case 139374:
                    break;
                default:
                    Slog.d(WifiP2pServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
                case 139271:
                case 139277:
                    if (WifiP2pServiceImpl.this.mWifiInjector == null) {
                        WifiP2pServiceImpl.this.mWifiInjector = WifiInjector.getInstance();
                    }
                    WifiP2pServiceImpl.this.mWifiInjector.getOppoWifiSharingManager().stopWifiSharingTetheringIfNeed();
                    break;
            }
            if (1 == SystemProperties.getInt("persist.sys.p2p_forbidden", 0)) {
                Log.d(WifiP2pServiceImpl.TAG, "forbidden p2p feature for customized.");
            } else {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(Message.obtain(msg));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setWifiLogForReplyChannel(WifiLog log) {
        this.mReplyChannel.setWifiLog(log);
    }

    /* access modifiers changed from: private */
    public class DeathHandlerData {
        IBinder.DeathRecipient mDeathRecipient;
        Messenger mMessenger;

        DeathHandlerData(IBinder.DeathRecipient dr, Messenger m) {
            this.mDeathRecipient = dr;
            this.mMessenger = m;
        }

        public String toString() {
            return "deathRecipient=" + this.mDeathRecipient + ", messenger=" + this.mMessenger;
        }
    }

    public WifiP2pServiceImpl(Context context, WifiInjector wifiInjector) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mFrameworkFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiP2pMetrics = this.mWifiInjector.getWifiP2pMetrics();
        this.mNetworkInfo = new NetworkInfo(13, 0, NETWORKTYPE, "");
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mThisDevice.primaryDeviceType = this.mContext.getResources().getString(17039776);
        HandlerThread wifiP2pThread = this.mWifiInjector.getWifiP2pServiceHandlerThread();
        this.mClientHandler = new ClientHandler(TAG, wifiP2pThread.getLooper());
        this.mOppoWifiP2pStats = new OppoWifiP2pStats(this.mContext, wifiP2pThread.getLooper());
        this.mP2pStateMachine = new P2pStateMachine(TAG, wifiP2pThread.getLooper(), this.mP2pSupported);
        this.mP2pStateMachine.start();
        this.mOppoWifiP2pNotifyService = new OppoWifiP2pNotifyService(this.mP2pStateMachine);
        this.mOppoWifiP2pConnectionHelper = new OppoWifiP2pConnectionHelper();
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
        this.mP2pStateMachine.enableVerboseLogging(verbose);
    }

    public void connectivityServiceReady() {
        this.mNwService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
    }

    private int checkConnectivityInternalPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL");
    }

    private int checkLocationHardwarePermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.LOCATION_HARDWARE");
    }

    private void enforceConnectivityInternalOrLocationHardwarePermission() {
        if (checkConnectivityInternalPermission() != 0 && checkLocationHardwarePermission() != 0) {
            enforceConnectivityInternalPermission();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopIpClient() {
        this.mIpClientStartIndex++;
        IIpClient iIpClient = this.mIpClient;
        if (iIpClient != null) {
            try {
                iIpClient.shutdown();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
            this.mIpClient = null;
        }
        this.mDhcpResults = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startIpClient(String ifname, Handler smHandler) {
        stopIpClient();
        this.mIpClientStartIndex++;
        IpClientUtil.makeIpClient(this.mContext, ifname, new IpClientCallbacksImpl(this.mIpClientStartIndex, smHandler));
    }

    /* access modifiers changed from: private */
    public class IpClientCallbacksImpl extends IpClientCallbacks {
        private final Handler mHandler;
        private final int mStartIndex;

        private IpClientCallbacksImpl(int startIndex, Handler handler) {
            this.mStartIndex = startIndex;
            this.mHandler = handler;
        }

        public void onIpClientCreated(IIpClient ipClient) {
            this.mHandler.post(new Runnable(ipClient) {
                /* class com.android.server.wifi.p2p.$$Lambda$WifiP2pServiceImpl$IpClientCallbacksImpl$nEQLAd0qG_Owp3P744olhYEPtk */
                private final /* synthetic */ IIpClient f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiP2pServiceImpl.IpClientCallbacksImpl.this.lambda$onIpClientCreated$0$WifiP2pServiceImpl$IpClientCallbacksImpl(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onIpClientCreated$0$WifiP2pServiceImpl$IpClientCallbacksImpl(IIpClient ipClient) {
            ArrayList<InetAddress> dnsServers;
            if (WifiP2pServiceImpl.this.mIpClientStartIndex == this.mStartIndex) {
                WifiP2pServiceImpl.this.mIpClient = ipClient;
                if (!OppoWifiP2pConnectionHelper.isNfcTriggered() || WifiP2pServiceImpl.this.mIsExternalShareConnect) {
                    Slog.d(WifiP2pServiceImpl.TAG, "startIpManager aquire dynamic ip for client");
                    dnsServers = new ProvisioningConfiguration.Builder().withoutIPv6().withoutIpReachabilityMonitor().withPreDhcpAction(30000).withProvisioningTimeoutMs(36000).build();
                } else {
                    Slog.d(WifiP2pServiceImpl.TAG, "startIpManager set static ip to 192.168.49.100");
                    ArrayList<InetAddress> dnsServers2 = new ArrayList<>();
                    dnsServers2.add(InetAddress.parseNumericAddress(WifiP2pServiceImpl.SERVER_ADDRESS));
                    dnsServers = new ProvisioningConfiguration.Builder().withoutIPv6().withoutIpReachabilityMonitor().withStaticConfiguration(new StaticIpConfiguration.Builder().setIpAddress(new LinkAddress("192.168.49.100/24")).setGateway(InetAddress.parseNumericAddress(WifiP2pServiceImpl.SERVER_ADDRESS)).setDnsServers(dnsServers2).build()).build();
                }
                try {
                    WifiP2pServiceImpl.this.mIpClient.startProvisioning(dnsServers.toStableParcelable());
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            }
        }

        public void onPreDhcpAction() {
            WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION);
        }

        public void onPostDhcpAction() {
            WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_POST_DHCP_ACTION);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_DHCP_RESULTS, dhcpResults);
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE);
        }
    }

    public Messenger getMessenger(IBinder binder) {
        Messenger messenger;
        enforceAccessPermission();
        enforceChangePermission();
        synchronized (this.mLock) {
            messenger = new Messenger(this.mClientHandler);
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "getMessenger: uid=" + getCallingUid() + ", binder=" + binder + ", messenger=" + messenger);
            }
            IBinder.DeathRecipient dr = new IBinder.DeathRecipient(binder) {
                /* class com.android.server.wifi.p2p.$$Lambda$WifiP2pServiceImpl$LwceCrSRIRY_Lp9TjCEZZ62jls */
                private final /* synthetic */ IBinder f$1;

                {
                    this.f$1 = r2;
                }

                public final void binderDied() {
                    WifiP2pServiceImpl.this.lambda$getMessenger$0$WifiP2pServiceImpl(this.f$1);
                }
            };
            try {
                binder.linkToDeath(dr, 0);
                this.mDeathDataByBinder.put(binder, new DeathHandlerData(dr, messenger));
            } catch (RemoteException e) {
                Log.e(TAG, "Error on linkToDeath: e=" + e);
            }
            this.mP2pStateMachine.sendMessage(ENABLE_P2P);
        }
        return messenger;
    }

    public /* synthetic */ void lambda$getMessenger$0$WifiP2pServiceImpl(IBinder binder) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "binderDied: binder=" + binder);
        }
        close(binder);
    }

    public Messenger getP2pStateMachineMessenger() {
        enforceConnectivityInternalOrLocationHardwarePermission();
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mP2pStateMachine.getHandler());
    }

    public void close(IBinder binder) {
        enforceAccessPermission();
        enforceChangePermission();
        synchronized (this.mLock) {
            DeathHandlerData dhd = this.mDeathDataByBinder.get(binder);
            if (dhd == null) {
                Log.w(TAG, "close(): no death recipient for binder");
                return;
            }
            this.mP2pStateMachine.sendMessage(REMOVE_CLIENT_INFO, 0, 0, binder);
            binder.unlinkToDeath(dhd.mDeathRecipient, 0);
            this.mDeathDataByBinder.remove(binder);
            if (dhd.mMessenger != null && this.mDeathDataByBinder.isEmpty()) {
                try {
                    dhd.mMessenger.send(this.mClientHandler.obtainMessage(139268));
                    dhd.mMessenger.send(this.mClientHandler.obtainMessage(139280));
                } catch (RemoteException e) {
                    Log.e(TAG, "close: Failed sending clean-up commands: e=" + e);
                }
                this.mP2pStateMachine.sendMessage(DISABLE_P2P);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setNfcTriggered(boolean enable) {
        this.mOppoWifiP2pStats.setOshareEnable(enable);
        if (OppoWifiP2pConnectionHelper.isNfcTriggered() != enable) {
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            WifiP2pNative wifiP2pNative = WifiInjector.getInstance().getWifiP2pNative();
            String ifaceName = wifiNative.getClientInterfaceName();
            if (this.mVerboseLoggingEnabled) {
                Slog.d(TAG, "set isNfcTriggered(" + OppoWifiP2pConnectionHelper.isNfcTriggered() + ") to " + enable);
            }
            if (enable) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (this.mWifiInjector == null) {
                        this.mWifiInjector = WifiInjector.getInstance();
                    }
                    this.mWifiInjector.getOppoWifiSharingManager().stopWifiSharingTetheringIfNeed();
                    Binder.restoreCallingIdentity(token);
                    P2pStateMachine p2pStateMachine = this.mP2pStateMachine;
                    int i = this.mFastConTriggeredNum + 1;
                    this.mFastConTriggeredNum = i;
                    p2pStateMachine.sendMessageDelayed(AUTO_ACCEPT_INVITATION_TIME_OUT, i, 60000);
                    WifiConnectivityManager.setPeriodicScanIntervalMs(enable);
                    wifiNative.setOshareMod(ifaceName, 1);
                    wifiP2pNative.setP2pSsidPostfix("-fastCon");
                    if (this.mWifiInjector == null) {
                        this.mWifiInjector = WifiInjector.getInstance();
                    }
                    this.mWifiInjector.getOppoWifiSharingManager().stopWifiSharingTetheringIfNeed();
                    this.mWifiInjector.getOppoWifiSharingManager().setWifiClosedByUser(true);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } else {
                this.mFastConTriggeredNum = 0;
                WifiConnectivityManager.setPeriodicScanIntervalMs(enable);
                wifiNative.setOshareMod(ifaceName, 0);
                wifiP2pNative.setP2pSsidPostfix("-" + OppoWifiP2pConnectionHelper.getSsidPostfix(this.mThisDevice.deviceName));
            }
            OppoWifiP2pConnectionHelper.setNfcTriggered(enable);
        } else if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "isNfcTriggered already " + enable);
        }
    }

    public void setMiracastMode(int mode) {
        enforceConnectivityInternalPermission();
        checkConfigureWifiDisplayPermission();
        this.mP2pStateMachine.sendMessage(SET_MIRACAST_MODE, mode);
        AsyncChannel asyncChannel = this.mWifiChannel;
        if (asyncChannel != null) {
            asyncChannel.sendMessage((int) SET_MIRACAST_MODE, mode);
        } else {
            Log.e(TAG, "setMiracastMode(): WifiChannel is null");
        }
    }

    public void checkConfigureWifiDisplayPermission() {
        if (!getWfdPermission(Binder.getCallingUid())) {
            throw new SecurityException("Wifi Display Permission denied for uid = " + Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getWfdPermission(int uid) {
        return this.mWifiInjector.getWifiPermissionsWrapper().getUidPermission("android.permission.CONFIGURE_WIFI_DISPLAY", uid) != -1;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiP2pService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        this.mP2pStateMachine.dump(fd, pw, args);
        pw.println("mAutonomousGroup " + this.mAutonomousGroup);
        pw.println("mJoinExistingGroup " + this.mJoinExistingGroup);
        pw.println("mDiscoveryStarted " + this.mDiscoveryStarted);
        pw.println("mNetworkInfo " + this.mNetworkInfo);
        pw.println("mTemporarilyDisconnectedWifi " + this.mTemporarilyDisconnectedWifi);
        pw.println("mServiceDiscReqId " + this.mServiceDiscReqId);
        pw.println("mDeathDataByBinder " + this.mDeathDataByBinder);
        pw.println("mClientInfoList " + this.mClientInfoList.size());
        pw.println();
        IIpClient ipClient = this.mIpClient;
        if (ipClient != null) {
            pw.println("mIpClient:");
            IpClientUtil.dumpIpClient(ipClient, fd, pw, args);
        }
    }

    public boolean isP2pGroupNegotiationState() {
        P2pStateMachine p2pStateMachine = this.mP2pStateMachine;
        if (p2pStateMachine != null) {
            return p2pStateMachine.isP2pGroupNegotiationState();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class P2pStateMachine extends StateMachine {
        private DefaultState mDefaultState = new DefaultState();
        private FrequencyConflictState mFrequencyConflictState = new FrequencyConflictState();
        private WifiP2pGroup mGroup;
        private GroupCreatedState mGroupCreatedState = new GroupCreatedState();
        private GroupCreatingState mGroupCreatingState = new GroupCreatingState();
        private GroupNegotiationState mGroupNegotiationState = new GroupNegotiationState();
        private final WifiP2pGroupList mGroups = new WifiP2pGroupList((WifiP2pGroupList) null, new WifiP2pGroupList.GroupDeleteListener() {
            /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.AnonymousClass1 */

            public void onDeleteGroup(int netId) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd("called onDeleteGroup() netId=" + netId);
                }
                P2pStateMachine.this.mWifiNative.removeP2pNetwork(netId);
                P2pStateMachine.this.mWifiNative.saveConfig();
                P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
            }
        });
        private InactiveState mInactiveState = new InactiveState();
        private String mInterfaceName;
        private boolean mIsHalInterfaceAvailable = false;
        private boolean mIsWifiEnabled = false;
        private OngoingGroupRemovalState mOngoingGroupRemovalState = new OngoingGroupRemovalState();
        private P2pDisabledState mP2pDisabledState = new P2pDisabledState();
        private P2pDisablingState mP2pDisablingState = new P2pDisablingState();
        private P2pEnabledState mP2pEnabledState = new P2pEnabledState();
        private P2pNotSupportedState mP2pNotSupportedState = new P2pNotSupportedState();
        private final WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
        private final WifiP2pDeviceList mPeersLostDuringConnection = new WifiP2pDeviceList();
        private ProvisionDiscoveryState mProvisionDiscoveryState = new ProvisionDiscoveryState();
        private WifiP2pConfig mSavedPeerConfig = new WifiP2pConfig();
        private UserAuthorizingInviteRequestState mUserAuthorizingInviteRequestState = new UserAuthorizingInviteRequestState();
        private UserAuthorizingJoinState mUserAuthorizingJoinState = new UserAuthorizingJoinState();
        private UserAuthorizingNegotiationRequestState mUserAuthorizingNegotiationRequestState = new UserAuthorizingNegotiationRequestState();
        private WifiController mWifiController = WifiInjector.getInstance().getWifiController();
        private WifiP2pMonitor mWifiMonitor = WifiP2pServiceImpl.this.mWifiInjector.getWifiP2pMonitor();
        private WifiP2pNative mWifiNative = WifiP2pServiceImpl.this.mWifiInjector.getWifiP2pNative();
        private final WifiP2pInfo mWifiP2pInfo = new WifiP2pInfo();

        P2pStateMachine(String name, Looper looper, boolean p2pSupported) {
            super(name, looper);
            addState(this.mDefaultState);
            addState(this.mP2pNotSupportedState, this.mDefaultState);
            addState(this.mP2pDisablingState, this.mDefaultState);
            addState(this.mP2pDisabledState, this.mDefaultState);
            addState(this.mP2pEnabledState, this.mDefaultState);
            addState(this.mInactiveState, this.mP2pEnabledState);
            addState(this.mGroupCreatingState, this.mP2pEnabledState);
            addState(this.mUserAuthorizingInviteRequestState, this.mGroupCreatingState);
            addState(this.mUserAuthorizingNegotiationRequestState, this.mGroupCreatingState);
            addState(this.mProvisionDiscoveryState, this.mGroupCreatingState);
            addState(this.mGroupNegotiationState, this.mGroupCreatingState);
            addState(this.mFrequencyConflictState, this.mGroupCreatingState);
            addState(this.mGroupCreatedState, this.mP2pEnabledState);
            addState(this.mUserAuthorizingJoinState, this.mGroupCreatedState);
            addState(this.mOngoingGroupRemovalState, this.mGroupCreatedState);
            if (p2pSupported) {
                setInitialState(this.mP2pDisabledState);
            } else {
                setInitialState(this.mP2pNotSupportedState);
            }
            setLogRecSize(50);
            setLogOnlyTransitions(true);
            if (p2pSupported) {
                WifiP2pServiceImpl.this.mContext.registerReceiver(new BroadcastReceiver(WifiP2pServiceImpl.this) {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.AnonymousClass2 */

                    public void onReceive(Context context, Intent intent) {
                        if (intent.getIntExtra("wifi_state", 4) == 3) {
                            P2pStateMachine.this.mIsWifiEnabled = true;
                            P2pStateMachine.this.checkAndReEnableP2p();
                        } else {
                            P2pStateMachine.this.mIsWifiEnabled = false;
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DISABLE_P2P);
                        }
                        P2pStateMachine.this.checkAndSendP2pStateChangedBroadcast();
                    }
                }, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
                WifiP2pServiceImpl.this.mContext.registerReceiver(new BroadcastReceiver(WifiP2pServiceImpl.this) {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.AnonymousClass3 */

                    public void onReceive(Context context, Intent intent) {
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.isLocationModeEnabled()) {
                            P2pStateMachine.this.sendMessage(139268);
                        }
                    }
                }, new IntentFilter("android.location.MODE_CHANGED"));
                this.mWifiNative.registerInterfaceAvailableListener(new HalDeviceManager.InterfaceAvailableForRequestListener() {
                    /* class com.android.server.wifi.p2p.$$Lambda$WifiP2pServiceImpl$P2pStateMachine$zMDJmVHxNOQccRUsy4cDbijFDbc */

                    @Override // com.android.server.wifi.HalDeviceManager.InterfaceAvailableForRequestListener
                    public final void onAvailabilityChanged(boolean z) {
                        WifiP2pServiceImpl.P2pStateMachine.this.lambda$new$0$WifiP2pServiceImpl$P2pStateMachine(z);
                    }
                }, getHandler());
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.wifi.COUNTRY_CODE_CHANGED");
                WifiP2pServiceImpl.this.mContext.registerReceiver(new BroadcastReceiver(WifiP2pServiceImpl.this) {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.AnonymousClass4 */

                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals("android.net.wifi.COUNTRY_CODE_CHANGED")) {
                            P2pStateMachine.this.sendMessageDelayed(WifiP2pServiceImpl.P2P_COUNTRYCODE_CHNAGED, 1500);
                        }
                    }
                }, filter);
            }
        }

        public /* synthetic */ void lambda$new$0$WifiP2pServiceImpl$P2pStateMachine(boolean isAvailable) {
            this.mIsHalInterfaceAvailable = isAvailable;
            if (isAvailable) {
                checkAndReEnableP2p();
            }
            checkAndSendP2pStateChangedBroadcast();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void enableVerboseLogging(int verbose) {
            WifiP2pServiceImpl.this.mVerboseLoggingEnabled = verbose > 0;
            this.mWifiNative.enableVerboseLogging(verbose);
            this.mWifiMonitor.enableVerboseLogging(verbose);
            if (WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService != null) {
                WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService.enableVerboseLogging(verbose);
            }
        }

        public void registerForWifiMonitorEvents() {
            this.mWifiMonitor.registerHandler(this.mInterfaceName, 147498, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, 147497, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_DEVICE_LOST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_FIND_STOPPED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_STARTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, 147457, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, 147458, getHandler());
            this.mWifiMonitor.startMonitoring(this.mInterfaceName);
        }

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName() + message.toString());
                }
                int i = 2;
                switch (message.what) {
                    case 69632:
                        if (message.arg1 != 0) {
                            P2pStateMachine.this.loge("Full connection failure, error = " + message.arg1);
                            WifiP2pServiceImpl.this.mWifiChannel = null;
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            p2pStateMachine.transitionTo(p2pStateMachine.mP2pDisabledState);
                            break;
                        } else {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine.this.logd("Full connection with ClientModeImpl established");
                            }
                            WifiP2pServiceImpl.this.mWifiChannel = (AsyncChannel) message.obj;
                            break;
                        }
                    case 69633:
                        new WifiAsyncChannel(WifiP2pServiceImpl.TAG).connect(WifiP2pServiceImpl.this.mContext, P2pStateMachine.this.getHandler(), message.replyTo);
                        break;
                    case 69636:
                        if (message.arg1 == 2) {
                            P2pStateMachine.this.loge("Send failed, client connection lost");
                        } else {
                            P2pStateMachine.this.loge("Client connection lost with reason: " + message.arg1);
                        }
                        WifiP2pServiceImpl.this.mWifiChannel = null;
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.transitionTo(p2pStateMachine2.mP2pDisabledState);
                        break;
                    case 139265:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 2);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139269, 2);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139272, 2);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139275, 2);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139278, 2);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139281, 2);
                        break;
                    case 139283:
                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                        p2pStateMachine3.replyToMessage((P2pStateMachine) message, (Message) 139284, (int) p2pStateMachine3.getPeers(p2pStateMachine3.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid));
                        break;
                    case 139285:
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        p2pStateMachine4.replyToMessage((P2pStateMachine) message, (Message) 139286, (int) new WifiP2pInfo(p2pStateMachine4.mWifiP2pInfo));
                        break;
                    case 139287:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            p2pStateMachine5.replyToMessage((P2pStateMachine) message, (Message) 139288, (int) p2pStateMachine5.maybeEraseOwnDeviceAddress((P2pStateMachine) p2pStateMachine5.mGroup, (WifiP2pGroup) message.sendingUid));
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139288, (int) null);
                            break;
                        }
                    case 139292:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139293, 2);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139296, 2);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139299, 2);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139302, 2);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139305, 2);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139308, 2);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 2);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139316, 2);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139319, 2);
                        break;
                    case 139321:
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.replyToMessage((P2pStateMachine) message, (Message) 139322, (int) new WifiP2pGroupList(p2pStateMachine6.maybeEraseOwnDeviceAddress((P2pStateMachine) p2pStateMachine6.mGroups, (WifiP2pGroupList) message.sendingUid), (WifiP2pGroupList.GroupDeleteListener) null));
                        break;
                    case 139323:
                        if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 2);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 0);
                            break;
                        }
                    case 139326:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139327, 2);
                        break;
                    case 139329:
                    case 139332:
                    case 139335:
                    case 139374:
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /* 143361 */:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /* 143362 */:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /* 143363 */:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /* 143364 */:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /* 143365 */:
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /* 143366 */:
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /* 143373 */:
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /* 143374 */:
                    case WifiP2pServiceImpl.ENABLE_P2P /* 143376 */:
                    case WifiP2pServiceImpl.DISABLE_P2P /* 143377 */:
                    case WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION /* 143390 */:
                    case WifiP2pServiceImpl.IPC_POST_DHCP_ACTION /* 143391 */:
                    case WifiP2pServiceImpl.IPC_DHCP_RESULTS /* 143392 */:
                    case WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS /* 143393 */:
                    case WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE /* 143394 */:
                    case 147457:
                    case 147458:
                    case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT /* 147477 */:
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /* 147478 */:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /* 147484 */:
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /* 147486 */:
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /* 147488 */:
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT /* 147493 */:
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT /* 147494 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT /* 147495 */:
                        break;
                    case 139339:
                    case 139340:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139341, (int) null);
                        break;
                    case 139342:
                    case 139343:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139345, 2);
                        break;
                    case 139346:
                        if (!P2pStateMachine.this.factoryReset(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139347, 0);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139348);
                            break;
                        }
                    case 139349:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkStackPermission(message.sendingUid)) {
                            P2pStateMachine.this.loge("Permission violation - no NETWORK_STACK permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139350, (int) null);
                            break;
                        } else {
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            p2pStateMachine7.replyToMessage((P2pStateMachine) message, (Message) 139350, (int) p2pStateMachine7.mSavedPeerConfig);
                            break;
                        }
                    case 139351:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkStackPermission(message.sendingUid)) {
                            P2pStateMachine.this.loge("Permission violation - no NETWORK_STACK permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage(message, 139352);
                            break;
                        } else {
                            WifiP2pConfig peerConfig = (WifiP2pConfig) message.obj;
                            if (!P2pStateMachine.this.isConfigInvalid(peerConfig)) {
                                P2pStateMachine.this.logd("setSavedPeerConfig to " + peerConfig);
                                P2pStateMachine.this.mSavedPeerConfig = peerConfig;
                                P2pStateMachine.this.replyToMessage(message, 139353);
                                break;
                            } else {
                                P2pStateMachine.this.loge("Dropping set mSavedPeerConfig requeset" + peerConfig);
                                P2pStateMachine.this.replyToMessage(message, 139352);
                                break;
                            }
                        }
                    case 139354:
                        P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                        if (!p2pStateMachine8.mIsWifiEnabled || !P2pStateMachine.this.isHalInterfaceAvailable()) {
                            i = 1;
                        }
                        p2pStateMachine8.replyToMessage((P2pStateMachine) message, (Message) 139355, i);
                        break;
                    case 139356:
                        P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                        if (!WifiP2pServiceImpl.this.mDiscoveryStarted) {
                            i = 1;
                        }
                        p2pStateMachine9.replyToMessage((P2pStateMachine) message, (Message) 139357, i);
                        break;
                    case 139358:
                        P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                        p2pStateMachine10.replyToMessage((P2pStateMachine) message, (Message) 139359, (int) WifiP2pServiceImpl.this.mNetworkInfo);
                        break;
                    case 139360:
                        if (message.obj instanceof Bundle) {
                            Bundle bundle = (Bundle) message.obj;
                            String pkgName = bundle.getString("android.net.wifi.p2p.CALLING_PACKAGE");
                            IBinder binder = bundle.getBinder("android.net.wifi.p2p.CALLING_BINDER");
                            try {
                                WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkPackage(message.sendingUid, pkgName);
                                if (!(binder == null || message.replyTo == null)) {
                                    WifiP2pServiceImpl.this.mClientChannelList.put(binder, message.replyTo);
                                    P2pStateMachine.this.getClientInfo(message.replyTo, true).mPackageName = pkgName;
                                    break;
                                }
                            } catch (SecurityException se) {
                                P2pStateMachine.this.loge("Unable to update calling package, " + se);
                                break;
                            }
                        }
                        break;
                    case 139361:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                            p2pStateMachine11.replyToMessage((P2pStateMachine) message, (Message) 139362, (int) p2pStateMachine11.maybeEraseOwnDeviceAddress((P2pStateMachine) WifiP2pServiceImpl.this.mThisDevice, (WifiP2pDevice) message.sendingUid));
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139362, (int) null);
                            break;
                        }
                    case 139366:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139366, 2);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /* 143375 */:
                        WifiP2pServiceImpl.this.mDiscoveryBlocked = message.arg1 == 1;
                        WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            if (message.obj != null) {
                                try {
                                    ((StateMachine) message.obj).sendMessage(message.arg2);
                                    break;
                                } catch (Exception e) {
                                    P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                    break;
                                }
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                        }
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /* 147485 */:
                        if (message.obj != null) {
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                            break;
                        }
                    default:
                        P2pStateMachine.this.loge("Unhandled message " + message);
                        return false;
                }
                return true;
            }
        }

        class P2pNotSupportedState extends State {
            P2pNotSupportedState() {
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 1);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139269, 1);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139272, 1);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139275, 1);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139278, 1);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139281, 1);
                        break;
                    case 139292:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139293, 1);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139296, 1);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139299, 1);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139302, 1);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139305, 1);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139308, 1);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 1);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139316, 1);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139319, 1);
                        break;
                    case 139323:
                        if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 1);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 0);
                            break;
                        }
                    case 139326:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139327, 1);
                        break;
                    case 139329:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139330, 1);
                        break;
                    case 139332:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139333, 1);
                        break;
                    case 139346:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139347, 1);
                        break;
                    case 139366:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139366, 1);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class P2pDisablingState extends State {
            P2pDisablingState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.sendMessageDelayed(p2pStateMachine.obtainMessage(WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT, WifiP2pServiceImpl.access$4204(), 0), RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                int i = message.what;
                if (i != 143366) {
                    if (i != 147458) {
                        switch (i) {
                            case WifiP2pServiceImpl.ENABLE_P2P /* 143376 */:
                            case WifiP2pServiceImpl.DISABLE_P2P /* 143377 */:
                            case WifiP2pServiceImpl.REMOVE_CLIENT_INFO /* 143378 */:
                                P2pStateMachine.this.deferMessage(message);
                                return true;
                            default:
                                return false;
                        }
                    } else {
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine.this.logd("p2p socket connection lost");
                        }
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.transitionTo(p2pStateMachine2.mP2pDisabledState);
                        return true;
                    }
                } else if (WifiP2pServiceImpl.sDisableP2pTimeoutIndex != message.arg1) {
                    return true;
                } else {
                    P2pStateMachine.this.loge("P2p disable timed out");
                    P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                    p2pStateMachine3.transitionTo(p2pStateMachine3.mP2pDisabledState);
                    return true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class P2pDisabledState extends State {
            P2pDisabledState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(false);
            }

            private void setupInterfaceFeatures(String interfaceName) {
                if (WifiP2pServiceImpl.this.mContext.getResources().getBoolean(17891590)) {
                    Log.i(WifiP2pServiceImpl.TAG, "Supported feature: P2P MAC randomization");
                    P2pStateMachine.this.mWifiNative.setMacRandomization(true);
                    return;
                }
                P2pStateMachine.this.mWifiNative.setMacRandomization(false);
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                int i = message.what;
                if (i != 143376) {
                    if (i != 143378) {
                        return false;
                    }
                    if (!(message.obj instanceof IBinder)) {
                        P2pStateMachine.this.loge("Invalid obj when REMOVE_CLIENT_INFO");
                        return true;
                    }
                    Map map = WifiP2pServiceImpl.this.mClientChannelList;
                    ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.remove((Messenger) map.remove((IBinder) message.obj));
                    if (clientInfo == null) {
                        return true;
                    }
                    P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                    p2pStateMachine2.logd("Remove client - " + clientInfo.mPackageName);
                    return true;
                } else if (!P2pStateMachine.this.mIsWifiEnabled) {
                    Log.e(WifiP2pServiceImpl.TAG, "Ignore P2P enable since wifi is " + P2pStateMachine.this.mIsWifiEnabled);
                    return true;
                } else {
                    P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                    p2pStateMachine3.mInterfaceName = p2pStateMachine3.mWifiNative.setupInterface(new HalDeviceManager.InterfaceDestroyedListener() {
                        /* class com.android.server.wifi.p2p.$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2gPM */

                        @Override // com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener
                        public final void onDestroyed(String str) {
                            WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this.lambda$processMessage$0$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState(str);
                        }
                    }, P2pStateMachine.this.getHandler());
                    if (P2pStateMachine.this.mInterfaceName == null) {
                        Log.e(WifiP2pServiceImpl.TAG, "Failed to setup interface for P2P");
                        return true;
                    }
                    setupInterfaceFeatures(P2pStateMachine.this.mInterfaceName);
                    try {
                        WifiP2pServiceImpl.this.mNwService.setInterfaceUp(P2pStateMachine.this.mInterfaceName);
                    } catch (RemoteException re) {
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        p2pStateMachine4.loge("Unable to change interface settings: " + re);
                    } catch (IllegalStateException ie) {
                        P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                        p2pStateMachine5.loge("Unable to change interface settings: " + ie);
                    }
                    P2pStateMachine.this.registerForWifiMonitorEvents();
                    P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                    p2pStateMachine6.transitionTo(p2pStateMachine6.mInactiveState);
                    return true;
                }
            }

            public /* synthetic */ void lambda$processMessage$0$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState(String ifaceName) {
                P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DISABLE_P2P);
            }
        }

        class P2pEnabledState extends State {
            P2pEnabledState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(true);
                if (P2pStateMachine.this.isPendingFactoryReset()) {
                    P2pStateMachine.this.factoryReset(1000);
                }
                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                P2pStateMachine.this.initializeP2pSettings();
            }

            public boolean processMessage(Message message) {
                boolean p2pFindSetDown;
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case 139265:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, true)) {
                            if (!WifiP2pServiceImpl.this.mDiscoveryBlocked || OppoWifiP2pConnectionHelper.isNfcTriggered()) {
                                P2pStateMachine.this.clearSupplicantServiceRequest();
                                P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                p2pStateMachine2.logd(getName() + ",DISCOVER_PEERS on freq=" + message.arg1);
                                boolean specifyFreq = false;
                                WifiP2pServiceImpl.this.mFoundTargetDevice = false;
                                if (message.arg1 == 0) {
                                    p2pFindSetDown = P2pStateMachine.this.mWifiNative.p2pFind(120);
                                } else if (message.arg1 == -1) {
                                    specifyFreq = true;
                                    p2pFindSetDown = P2pStateMachine.this.mWifiNative.p2pFind(30, 5745);
                                } else {
                                    specifyFreq = true;
                                    p2pFindSetDown = P2pStateMachine.this.mWifiNative.p2pFind(30, message.arg1);
                                }
                                if (p2pFindSetDown) {
                                    if (specifyFreq && !WifiP2pServiceImpl.this.mIntentToConnectPeer.isEmpty()) {
                                        WifiP2pServiceImpl.access$6108(WifiP2pServiceImpl.this);
                                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                        p2pStateMachine3.sendMessageDelayed(p2pStateMachine3.obtainMessage(WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT, message.arg1, 0), WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
                                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                            P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                            p2pStateMachine4.logd("mIntentToConnectPeer=" + WifiP2pServiceImpl.this.mIntentToConnectPeer + " mFindTimes=" + WifiP2pServiceImpl.this.mFindTimes);
                                        }
                                    }
                                    P2pStateMachine.this.sendThisDeviceChangedBroadcast();
                                    P2pStateMachine.this.replyToMessage(message, 139267);
                                    P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                                    break;
                                } else {
                                    P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 0);
                                    break;
                                }
                            } else {
                                P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 2);
                                break;
                            }
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 0);
                            break;
                        }
                        break;
                    case 139268:
                        if (P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139270);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139269, 0);
                            break;
                        }
                    case 139292:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            P2pStateMachine.this.replyToMessage(message, 139293);
                            break;
                        } else {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                p2pStateMachine5.logd(getName() + " add service");
                            }
                            if (P2pStateMachine.this.addLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj)) {
                                P2pStateMachine.this.replyToMessage(message, 139294);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139293);
                                break;
                            }
                        }
                    case 139295:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.logd(getName() + " remove service");
                        }
                        P2pStateMachine.this.removeLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139297);
                        break;
                    case 139298:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            p2pStateMachine7.logd(getName() + " clear service");
                        }
                        P2pStateMachine.this.clearLocalServices(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139300);
                        break;
                    case 139301:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                            p2pStateMachine8.logd(getName() + " add service request");
                        }
                        if (!P2pStateMachine.this.addServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj)) {
                            P2pStateMachine.this.replyToMessage(message, 139302);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139303);
                            break;
                        }
                    case 139304:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                            p2pStateMachine9.logd(getName() + " remove service request");
                        }
                        P2pStateMachine.this.removeServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139306);
                        break;
                    case 139307:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                            p2pStateMachine10.logd(getName() + " clear service request");
                        }
                        P2pStateMachine.this.clearServiceRequests(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139309);
                        break;
                    case 139310:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, true)) {
                            if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                                P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 2);
                                break;
                            } else {
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                                    p2pStateMachine11.logd(getName() + " discover services");
                                }
                                if (P2pStateMachine.this.updateSupplicantServiceRequest()) {
                                    if (P2pStateMachine.this.mWifiNative.p2pFind(120)) {
                                        WifiP2pServiceImpl.this.mWifiP2pMetrics.incrementServiceScans();
                                        P2pStateMachine.this.replyToMessage(message, 139312);
                                        break;
                                    } else {
                                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 0);
                                        break;
                                    }
                                } else {
                                    P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 3);
                                    break;
                                }
                            }
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139311, 0);
                            break;
                        }
                    case 139315:
                        WifiP2pDevice d = (WifiP2pDevice) message.obj;
                        if (d == null || !P2pStateMachine.this.setAndPersistDeviceName(d.deviceName)) {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139316, 0);
                            break;
                        } else {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                                p2pStateMachine12.logd("set device name " + d.deviceName);
                            }
                            P2pStateMachine.this.replyToMessage(message, 139317);
                            break;
                        }
                    case 139318:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                            p2pStateMachine13.logd(getName() + " delete persistent group");
                        }
                        P2pStateMachine.this.mGroups.remove(message.arg1);
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.updatePersistentGroup(P2pStateMachine.this.mGroups);
                        P2pStateMachine.this.replyToMessage(message, 139320);
                        break;
                    case 139323:
                        WifiP2pWfdInfo d2 = (WifiP2pWfdInfo) message.obj;
                        if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            if (d2 == null || !P2pStateMachine.this.setWfdInfo(d2)) {
                                P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 0);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139325);
                                break;
                            }
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139324, 0);
                            break;
                        }
                        break;
                    case 139329:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(message.sendingUid)) {
                            P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                            p2pStateMachine14.loge("Permission violation - no NETWORK_SETTING permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        } else {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine15 = P2pStateMachine.this;
                                p2pStateMachine15.logd(getName() + " start listen mode");
                            }
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            if (P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                                P2pStateMachine.this.replyToMessage(message, 139331);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139330);
                                break;
                            }
                        }
                    case 139332:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(message.sendingUid)) {
                            P2pStateMachine p2pStateMachine16 = P2pStateMachine.this;
                            p2pStateMachine16.loge("Permission violation - no NETWORK_SETTING permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage(message, 139333);
                            break;
                        } else {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine17 = P2pStateMachine.this;
                                p2pStateMachine17.logd(getName() + " stop listen mode");
                            }
                            if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                                P2pStateMachine.this.replyToMessage(message, 139334);
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139333);
                            }
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            break;
                        }
                    case 139335:
                        Bundle p2pChannels = (Bundle) message.obj;
                        int lc = p2pChannels.getInt("lc", 0);
                        int oc = p2pChannels.getInt("oc", 0);
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine18 = P2pStateMachine.this;
                            p2pStateMachine18.logd(getName() + " set listen and operating channel");
                        }
                        if (P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                            P2pStateMachine.this.replyToMessage(message, 139337);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139336);
                            break;
                        }
                    case 139339:
                        Bundle requestBundle = new Bundle();
                        requestBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverRequest());
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139341, (int) requestBundle);
                        break;
                    case 139340:
                        Bundle selectBundle = new Bundle();
                        selectBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverSelect());
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139341, (int) selectBundle);
                        break;
                    case 139366:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine19 = P2pStateMachine.this;
                            p2pStateMachine19.logd(getName() + " ADD_PERSISTENT_GROUP");
                        }
                        HashMap<String, String> hVariables = (HashMap) ((Bundle) message.obj).getSerializable("variables");
                        if (hVariables != null && WifiP2pServiceImpl.this.mOppoWifiP2pConnectionHelper != null) {
                            WifiP2pGroup group = WifiP2pServiceImpl.this.mOppoWifiP2pConnectionHelper.addPersistentGroup(hVariables, P2pStateMachine.this.mWifiNative, P2pStateMachine.this.mGroups);
                            P2pStateMachine.this.updatePersistentNetworks(true);
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139369, (int) new WifiP2pGroup(group));
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139367, 0);
                            break;
                        }
                    case 139374:
                        int p2pbandtype = ((Bundle) message.obj).getInt("p2pbandtype", 0);
                        if (WifiP2pServiceImpl.this.mOppoWifiP2pConnectionHelper != null) {
                            if (WifiP2pServiceImpl.this.mOppoWifiP2pConnectionHelper.setP2pBandList(p2pbandtype, P2pStateMachine.this.mWifiNative)) {
                                P2pStateMachine.this.replyToMessage(message, 139375);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139376);
                                break;
                            }
                        }
                        break;
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /* 143374 */:
                        P2pStateMachine.this.mWifiNative.setMiracastMode(message.arg1);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /* 143375 */:
                        boolean blocked = message.arg1 == 1;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked != blocked) {
                            WifiP2pServiceImpl.this.mDiscoveryBlocked = blocked;
                            if (blocked && WifiP2pServiceImpl.this.mDiscoveryStarted) {
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = true;
                            }
                            if (!blocked && WifiP2pServiceImpl.this.mDiscoveryPostponed) {
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                                P2pStateMachine.this.mWifiNative.p2pFind(120);
                            }
                        }
                        if (blocked) {
                            if (message.obj == null) {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            } else {
                                try {
                                    ((StateMachine) message.obj).sendMessage(message.arg2);
                                    break;
                                } catch (Exception e) {
                                    P2pStateMachine p2pStateMachine20 = P2pStateMachine.this;
                                    p2pStateMachine20.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiP2pServiceImpl.ENABLE_P2P /* 143376 */:
                        break;
                    case WifiP2pServiceImpl.DISABLE_P2P /* 143377 */:
                        if (P2pStateMachine.this.mPeers.clear()) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                        }
                        if (P2pStateMachine.this.mGroups.clear()) {
                            P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
                        }
                        P2pStateMachine.this.clearServicesForAllClients();
                        P2pStateMachine.this.mWifiMonitor.stopMonitoring(P2pStateMachine.this.mInterfaceName);
                        P2pStateMachine.this.mWifiNative.teardownInterface();
                        P2pStateMachine p2pStateMachine21 = P2pStateMachine.this;
                        p2pStateMachine21.transitionTo(p2pStateMachine21.mP2pDisablingState);
                        break;
                    case WifiP2pServiceImpl.REMOVE_CLIENT_INFO /* 143378 */:
                        if (message.obj instanceof IBinder) {
                            IBinder b = (IBinder) message.obj;
                            P2pStateMachine p2pStateMachine22 = P2pStateMachine.this;
                            p2pStateMachine22.clearClientInfo((Messenger) WifiP2pServiceImpl.this.mClientChannelList.get(b));
                            WifiP2pServiceImpl.this.mClientChannelList.remove(b);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT /* 143379 */:
                        if (WifiP2pServiceImpl.this.mFastConTriggeredNum == message.arg1) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine23 = P2pStateMachine.this;
                                p2pStateMachine23.logd("FastConTrigger: " + message.arg1 + "/" + WifiP2pServiceImpl.this.mFastConTriggeredNum);
                            }
                            WifiP2pServiceImpl.this.setNfcTriggered(false);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT /* 143380 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine24 = P2pStateMachine.this;
                            p2pStateMachine24.logd("P2P_FIND_SPECIAL_FREQ_TIME_OUT:" + WifiP2pServiceImpl.this.mFoundTargetDevice + ", mFindTimes=" + WifiP2pServiceImpl.this.mFindTimes);
                        }
                        if (WifiP2pServiceImpl.this.mFoundTargetDevice || WifiP2pServiceImpl.this.mFindTimes >= 5) {
                            WifiP2pServiceImpl.this.mFindTimes = 0;
                            WifiP2pServiceImpl.this.mFoundTargetDevice = false;
                            break;
                        } else {
                            P2pStateMachine.this.sendMessage(139265, message.arg1);
                            break;
                        }
                    case WifiP2pServiceImpl.P2P_COUNTRYCODE_CHNAGED /* 143395 */:
                        Log.d(WifiP2pServiceImpl.TAG, "P2P_COUNTRYCODE_CHNAGED");
                        WifiInjector.getInstance().getOppoSoftapP2pBandControl().p2pChangeBand();
                        break;
                    case 147458:
                        P2pStateMachine.this.loge("Unexpected loss of p2p socket connection");
                        P2pStateMachine p2pStateMachine25 = P2pStateMachine.this;
                        p2pStateMachine25.transitionTo(p2pStateMachine25.mP2pDisabledState);
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT /* 147477 */:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        } else {
                            WifiP2pDevice device = (WifiP2pDevice) message.obj;
                            if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine26 = P2pStateMachine.this;
                                    p2pStateMachine26.logd("Found a Device=" + device.deviceAddress + ", mIntentToConnectPeer=" + WifiP2pServiceImpl.this.mIntentToConnectPeer);
                                }
                                if (!WifiP2pServiceImpl.this.mIntentToConnectPeer.isEmpty() && WifiP2pServiceImpl.this.mIntentToConnectPeer.equalsIgnoreCase(device.deviceAddress)) {
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine.this.logd("mFoundTargetDevice true!");
                                    }
                                    WifiP2pServiceImpl.this.mFoundTargetDevice = true;
                                    WifiP2pServiceImpl.this.mIntentToConnectPeer = "";
                                    P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT);
                                }
                                P2pStateMachine.this.mPeers.updateSupplicantDetails(device);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                break;
                            }
                        }
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /* 147478 */:
                        if (message.obj != null) {
                            if (P2pStateMachine.this.mPeers.remove(((WifiP2pDevice) message.obj).deviceAddress) != null) {
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT /* 147493 */:
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                        break;
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT /* 147494 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine27 = P2pStateMachine.this;
                            p2pStateMachine27.logd(getName() + " receive service response");
                        }
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        } else {
                            for (WifiP2pServiceResponse resp : (List) message.obj) {
                                resp.setSrcDevice(P2pStateMachine.this.mPeers.get(resp.getSrcDevice().deviceAddress));
                                P2pStateMachine.this.sendServiceResponse(resp);
                            }
                            break;
                        }
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(false);
            }
        }

        /* access modifiers changed from: package-private */
        public class InactiveState extends State {
            InactiveState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
                WifiP2pServiceImpl.this.setNfcTriggered(false);
                P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(false);
            }

            public boolean processMessage(Message message) {
                boolean ret;
                WifiP2pDevice owner;
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139269, 0);
                            break;
                        } else {
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                            P2pStateMachine.this.replyToMessage(message, 139270);
                            break;
                        }
                    case 139271:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                            WifiP2pConfig config = (WifiP2pConfig) message.obj;
                            WifiP2pServiceImpl.this.mP2pConnectFreq = message.arg1;
                            if (WifiP2pServiceImpl.this.mP2pConnectFreq != 0) {
                                WifiP2pServiceImpl.this.mFindTimes = 0;
                                if (config != null) {
                                    WifiP2pServiceImpl.this.mIntentToConnectPeer = config.deviceAddress;
                                    if (config.wps.setup != 0) {
                                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                        p2pStateMachine2.logd("Force wps=" + config.wps.setup + " to pbc ");
                                        config.wps.setup = 0;
                                    }
                                }
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                    p2pStateMachine3.logd(getName() + "connect on freq=" + WifiP2pServiceImpl.this.mP2pConnectFreq);
                                }
                            }
                            boolean isConnectFailed = false;
                            if (P2pStateMachine.this.isConfigValidAsGroup(config)) {
                                WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                                if (P2pStateMachine.this.mWifiNative.p2pGroupAdd(config, true)) {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(3, config);
                                    P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                    p2pStateMachine4.transitionTo(p2pStateMachine4.mGroupNegotiationState);
                                } else {
                                    P2pStateMachine.this.loge("Cannot join a group with config.");
                                    isConnectFailed = true;
                                    P2pStateMachine.this.replyToMessage(message, 139272);
                                    P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(false);
                                }
                            } else if (P2pStateMachine.this.isConfigInvalid(config)) {
                                P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                p2pStateMachine5.loge("Dropping connect request " + config);
                                isConnectFailed = true;
                                P2pStateMachine.this.replyToMessage(message, 139272);
                                P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(false);
                            } else {
                                WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                                if (P2pStateMachine.this.reinvokePersistentGroup(config)) {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(1, config);
                                    P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                                    p2pStateMachine6.transitionTo(p2pStateMachine6.mGroupNegotiationState);
                                } else {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(0, config);
                                    P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                                    p2pStateMachine7.transitionTo(p2pStateMachine7.mProvisionDiscoveryState);
                                }
                                WifiP2pServiceImpl.this.mWifiDisplayUsageHelper.reportP2pConnectionCallerName(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), WifiP2pServiceImpl.this.mWifiDisplayUsageHelper.getFormatTime(), WifiP2pServiceImpl.this.mContext);
                                WifiP2pServiceImpl.this.mp2pStartedTime = System.currentTimeMillis();
                            }
                            if (!isConnectFailed) {
                                P2pStateMachine.this.mSavedPeerConfig = config;
                                P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine.this.replyToMessage(message, 139273);
                                break;
                            }
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139272);
                            break;
                        }
                        break;
                    case 139277:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            WifiP2pServiceImpl.this.mAutonomousGroup = true;
                            int netId = message.arg1;
                            WifiP2pConfig config2 = (WifiP2pConfig) message.obj;
                            P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                            if (config2 != null) {
                                if (P2pStateMachine.this.isConfigValidAsGroup(config2)) {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(3, config2);
                                    ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(config2, false);
                                } else {
                                    ret = false;
                                }
                            } else if (netId == -2) {
                                int netId2 = P2pStateMachine.this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
                                if (netId2 != -1) {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(1, null);
                                    ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(netId2);
                                } else {
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(2, null);
                                    ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(true);
                                }
                            } else {
                                WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(2, null);
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(false);
                            }
                            if (!ret) {
                                P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139278, 0);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139279);
                                P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                                p2pStateMachine8.transitionTo(p2pStateMachine8.mGroupNegotiationState);
                                break;
                            }
                        } else {
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139278, 0);
                            break;
                        }
                    case 139329:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(message.sendingUid)) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                                p2pStateMachine9.logd(getName() + " start listen mode");
                            }
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                                P2pStateMachine.this.replyToMessage(message, 139330);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139331);
                                break;
                            }
                        } else {
                            P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                            p2pStateMachine10.loge("Permission violation - no NETWORK_SETTING permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                    case 139332:
                        if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(message.sendingUid)) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                                p2pStateMachine11.logd(getName() + " stop listen mode");
                            }
                            if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                                P2pStateMachine.this.replyToMessage(message, 139334);
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139333);
                            }
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            break;
                        } else {
                            P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                            p2pStateMachine12.loge("Permission violation - no NETWORK_SETTING permission, uid = " + message.sendingUid);
                            P2pStateMachine.this.replyToMessage(message, 139333);
                            break;
                        }
                    case 139335:
                        if (message.obj != null) {
                            Bundle p2pChannels = (Bundle) message.obj;
                            int lc = p2pChannels.getInt("lc", 0);
                            int oc = p2pChannels.getInt("oc", 0);
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                                p2pStateMachine13.logd(getName() + " set listen and operating channel");
                            }
                            if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                                P2pStateMachine.this.replyToMessage(message, 139336);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139337);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments(s)");
                            break;
                        }
                    case 139342:
                        String handoverSelect = null;
                        if (message.obj != null) {
                            handoverSelect = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverSelect != null && P2pStateMachine.this.mWifiNative.initiatorReportNfcHandover(handoverSelect)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                            p2pStateMachine14.transitionTo(p2pStateMachine14.mGroupCreatingState);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139345);
                            break;
                        }
                        break;
                    case 139343:
                        String handoverRequest = null;
                        if (message.obj != null) {
                            handoverRequest = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverRequest != null && P2pStateMachine.this.mWifiNative.responderReportNfcHandover(handoverRequest)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine p2pStateMachine15 = P2pStateMachine.this;
                            p2pStateMachine15.transitionTo(p2pStateMachine15.mGroupCreatingState);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139345);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT /* 147479 */:
                        WifiP2pConfig config3 = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config3)) {
                            P2pStateMachine.this.mSavedPeerConfig = config3;
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                            WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(0, config3);
                            P2pStateMachine p2pStateMachine16 = P2pStateMachine.this;
                            p2pStateMachine16.transitionTo(p2pStateMachine16.mUserAuthorizingNegotiationRequestState);
                            break;
                        } else {
                            P2pStateMachine p2pStateMachine17 = P2pStateMachine.this;
                            p2pStateMachine17.loge("Dropping GO neg request " + config3);
                            break;
                        }
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /* 147485 */:
                        if (message.obj != null) {
                            P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine18 = P2pStateMachine.this;
                                p2pStateMachine18.logd(getName() + " group started");
                            }
                            if (P2pStateMachine.this.mGroup.isGroupOwner() && WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(P2pStateMachine.this.mGroup.getOwner().deviceAddress)) {
                                P2pStateMachine.this.mGroup.getOwner().deviceAddress = WifiP2pServiceImpl.this.mThisDevice.deviceAddress;
                            }
                            if (P2pStateMachine.this.mGroup.getNetworkId() != -2) {
                                P2pStateMachine p2pStateMachine19 = P2pStateMachine.this;
                                p2pStateMachine19.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                                P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                break;
                            } else {
                                WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                P2pStateMachine.this.deferMessage(message);
                                P2pStateMachine p2pStateMachine20 = P2pStateMachine.this;
                                p2pStateMachine20.transitionTo(p2pStateMachine20.mGroupNegotiationState);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT /* 147487 */:
                        if (message.obj != null) {
                            WifiP2pGroup group = (WifiP2pGroup) message.obj;
                            WifiP2pDevice owner2 = group.getOwner();
                            if (owner2 == null) {
                                int id = group.getNetworkId();
                                if (id >= 0) {
                                    String addr = P2pStateMachine.this.mGroups.getOwnerAddr(id);
                                    if (addr == null) {
                                        P2pStateMachine.this.loge("Ignored invitation from null owner");
                                        break;
                                    } else {
                                        group.setOwner(new WifiP2pDevice(addr));
                                        owner2 = group.getOwner();
                                    }
                                } else {
                                    P2pStateMachine.this.loge("Ignored invitation from null owner");
                                    break;
                                }
                            }
                            WifiP2pConfig config4 = new WifiP2pConfig();
                            config4.deviceAddress = group.getOwner().deviceAddress;
                            if (!P2pStateMachine.this.isConfigInvalid(config4)) {
                                P2pStateMachine.this.mSavedPeerConfig = config4;
                                if (!(owner2 == null || (owner = P2pStateMachine.this.mPeers.get(owner2.deviceAddress)) == null)) {
                                    if (owner.wpsPbcSupported()) {
                                        P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                                    } else if (owner.wpsKeypadSupported()) {
                                        P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                                    } else if (owner.wpsDisplaySupported()) {
                                        P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                                    }
                                }
                                WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                WifiP2pServiceImpl.this.mJoinExistingGroup = true;
                                WifiP2pServiceImpl.this.mWifiP2pMetrics.startConnectionEvent(0, config4);
                                P2pStateMachine p2pStateMachine21 = P2pStateMachine.this;
                                p2pStateMachine21.transitionTo(p2pStateMachine21.mUserAuthorizingInviteRequestState);
                                break;
                            } else {
                                P2pStateMachine p2pStateMachine22 = P2pStateMachine.this;
                                p2pStateMachine22.loge("Dropping invitation request " + config4);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /* 147489 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /* 147491 */:
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /* 147492 */:
                        if (message.obj != null) {
                            WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                            WifiP2pDevice device = provDisc.device;
                            if (device != null) {
                                P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                                P2pStateMachine.this.mSavedPeerConfig.deviceAddress = device.deviceAddress;
                                P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                                P2pStateMachine.this.notifyP2pProvDiscShowPinRequest(provDisc.pin, device.deviceAddress);
                                P2pStateMachine.this.mPeers.updateStatus(device.deviceAddress, 1);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine p2pStateMachine23 = P2pStateMachine.this;
                                p2pStateMachine23.transitionTo(p2pStateMachine23.mUserAuthorizingNegotiationRequestState);
                                break;
                            } else {
                                P2pStateMachine.this.loge("Device entry is null");
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    default:
                        return false;
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class GroupCreatingState extends State {
            GroupCreatingState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.sendMessageDelayed(p2pStateMachine.obtainMessage(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT, WifiP2pServiceImpl.access$9104(), 0), 120000);
                MtkSoftApUtils.stopSoftAp(WifiP2pServiceImpl.this.mContext);
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139266, 2);
                        return true;
                    case 139274:
                        P2pStateMachine.this.mWifiNative.p2pCancelConnect();
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(3);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.transitionTo(p2pStateMachine2.mInactiveState);
                        P2pStateMachine.this.replyToMessage(message, 139276);
                        return true;
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /* 143361 */:
                        if (WifiP2pServiceImpl.sGroupCreatingTimeoutIndex != message.arg1) {
                            return true;
                        }
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine.this.logd("Group negotiation timed out");
                        }
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(2);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                        p2pStateMachine3.transitionTo(p2pStateMachine3.mInactiveState);
                        return true;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /* 147478 */:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            return true;
                        }
                        WifiP2pDevice device = (WifiP2pDevice) message.obj;
                        if (!P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                p2pStateMachine4.logd("mSavedPeerConfig " + P2pStateMachine.this.mSavedPeerConfig.deviceAddress + "device " + device.deviceAddress);
                            }
                            return false;
                        }
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            p2pStateMachine5.logd("Add device to lost list " + device);
                        }
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return true;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /* 147481 */:
                        WifiP2pServiceImpl.this.mAutonomousGroup = false;
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.transitionTo(p2pStateMachine6.mGroupNegotiationState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class UserAuthorizingNegotiationRequestState extends State {
            UserAuthorizingNegotiationRequestState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0 || TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                    P2pStateMachine.this.notifyInvitationReceived();
                }
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /* 143362 */:
                        P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.p2pConnectWithPinDisplay(p2pStateMachine2.mSavedPeerConfig);
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                        p2pStateMachine3.transitionTo(p2pStateMachine3.mGroupNegotiationState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /* 143363 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                            p2pStateMachine4.logd("User rejected negotiation " + P2pStateMachine.this.mSavedPeerConfig);
                        }
                        P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                        p2pStateMachine5.transitionTo(p2pStateMachine5.mInactiveState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_CONFIRM /* 143367 */:
                        P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                        P2pStateMachine.this.mWifiNative.p2pConnect(P2pStateMachine.this.mSavedPeerConfig, WifiP2pServiceImpl.FORM_GROUP.booleanValue());
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.transitionTo(p2pStateMachine6.mGroupNegotiationState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        /* access modifiers changed from: package-private */
        public class UserAuthorizingInviteRequestState extends State {
            UserAuthorizingInviteRequestState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.notifyInvitationReceived();
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /* 143362 */:
                        P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        if (!p2pStateMachine2.reinvokePersistentGroup(p2pStateMachine2.mSavedPeerConfig)) {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.p2pConnectWithPinDisplay(p2pStateMachine3.mSavedPeerConfig);
                        }
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        p2pStateMachine4.transitionTo(p2pStateMachine4.mGroupNegotiationState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /* 143363 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            p2pStateMachine5.logd("User rejected invitation " + P2pStateMachine.this.mSavedPeerConfig);
                        }
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.transitionTo(p2pStateMachine6.mInactiveState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        /* access modifiers changed from: package-private */
        public class ProvisionDiscoveryState extends State {
            ProvisionDiscoveryState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.mWifiNative.p2pProvisionDiscovery(P2pStateMachine.this.mSavedPeerConfig);
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT /* 147490 */:
                        if (message.obj != null) {
                            WifiP2pDevice device = ((WifiP2pProvDiscEvent) message.obj).device;
                            if ((device == null || device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                    p2pStateMachine2.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                }
                                P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                p2pStateMachine3.p2pConnectWithPinDisplay(p2pStateMachine3.mSavedPeerConfig);
                                P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                p2pStateMachine4.transitionTo(p2pStateMachine4.mGroupNegotiationState);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /* 147491 */:
                        if (message.obj != null) {
                            WifiP2pDevice device2 = ((WifiP2pProvDiscEvent) message.obj).device;
                            if ((device2 == null || device2.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 2) {
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                    p2pStateMachine5.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                }
                                if (TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                                    WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                                    P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                                    p2pStateMachine6.transitionTo(p2pStateMachine6.mUserAuthorizingNegotiationRequestState);
                                    break;
                                } else {
                                    P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                                    p2pStateMachine7.p2pConnectWithPinDisplay(p2pStateMachine7.mSavedPeerConfig);
                                    P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                                    p2pStateMachine8.transitionTo(p2pStateMachine8.mGroupNegotiationState);
                                    break;
                                }
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /* 147492 */:
                        if (message.obj != null) {
                            WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                            WifiP2pDevice device3 = provDisc.device;
                            if (device3 != null) {
                                if (device3.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 1) {
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                                        p2pStateMachine9.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                    }
                                    P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                                    P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                                    p2pStateMachine10.p2pConnectWithPinDisplay(p2pStateMachine10.mSavedPeerConfig);
                                    P2pStateMachine.this.notifyInvitationSent(provDisc.pin, device3.deviceAddress);
                                    P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                                    p2pStateMachine11.transitionTo(p2pStateMachine11.mGroupNegotiationState);
                                    break;
                                }
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT /* 147493 */:
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT /* 147494 */:
                    default:
                        return false;
                    case WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT /* 147495 */:
                        if (WifiP2pServiceImpl.this.mOppoWifiP2pStats != null) {
                            WifiP2pServiceImpl.this.mOppoWifiP2pStats.setP2pFailStats(P2pStateMachine.this.mGroup, OppoWifiP2pStats.P2P_PROV_DISC_FAILURE_STATS);
                        }
                        P2pStateMachine.this.loge("provision discovery failed");
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(4);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                        p2pStateMachine12.transitionTo(p2pStateMachine12.mInactiveState);
                        break;
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class GroupNegotiationState extends State {
            GroupNegotiationState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
            }

            /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
            /* JADX WARNING: Removed duplicated region for block: B:76:0x0332  */
            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /* 147481 */:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /* 147483 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.logd(getName() + " go success");
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /* 147482 */:
                        if (WifiP2pServiceImpl.this.mOppoWifiP2pStats != null) {
                            WifiP2pServiceImpl.this.mOppoWifiP2pStats.setP2pFailStats(P2pStateMachine.this.mGroup, OppoWifiP2pStats.P2P_GO_NEGOTIATION_FAILURE_STATS);
                        }
                        if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.transitionTo(p2pStateMachine3.mFrequencyConflictState);
                            break;
                        }
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                            p2pStateMachine4.logd(getName() + " go failure");
                        }
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(0);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                        p2pStateMachine5.transitionTo(p2pStateMachine5.mInactiveState);
                        break;
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /* 147484 */:
                        if (WifiP2pServiceImpl.this.mOppoWifiP2pStats != null) {
                            WifiP2pServiceImpl.this.mOppoWifiP2pStats.setP2pFailStats(P2pStateMachine.this.mGroup, OppoWifiP2pStats.P2P_GROUP_FORMATION_FAILURE_STATS);
                        }
                        if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.transitionTo(p2pStateMachine6.mFrequencyConflictState);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /* 147485 */:
                        if (message.obj != null) {
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                                p2pStateMachine7.logd(getName() + " group started");
                            }
                            if (P2pStateMachine.this.mGroup.isGroupOwner() && WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(P2pStateMachine.this.mGroup.getOwner().deviceAddress)) {
                                P2pStateMachine.this.mGroup.getOwner().deviceAddress = WifiP2pServiceImpl.this.mThisDevice.deviceAddress;
                            }
                            if (P2pStateMachine.this.mGroup.getNetworkId() == -2) {
                                P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                                P2pStateMachine.this.mGroup.setNetworkId(P2pStateMachine.this.mGroups.getNetworkId(P2pStateMachine.this.mGroup.getOwner().deviceAddress, P2pStateMachine.this.mGroup.getNetworkName()));
                            }
                            if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                                if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                    P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                }
                                P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                                p2pStateMachine8.startDhcpServer(p2pStateMachine8.mGroup.getInterface());
                            } else {
                                P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                WifiP2pServiceImpl.this.mIsExternalShareConnect = WifiP2pServiceImpl.this.isExternalShareConnect(P2pStateMachine.this.mGroup.getOwner().deviceAddress);
                                WifiP2pServiceImpl.this.startIpClient(P2pStateMachine.this.mGroup.getInterface(), P2pStateMachine.this.getHandler());
                                WifiP2pDevice groupOwner = P2pStateMachine.this.mGroup.getOwner();
                                WifiP2pDevice peer = P2pStateMachine.this.mPeers.get(groupOwner.deviceAddress);
                                if (peer != null) {
                                    groupOwner.updateSupplicantDetails(peer);
                                    P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                } else if (groupOwner.deviceAddress == null || WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(groupOwner.deviceAddress)) {
                                    P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                                    p2pStateMachine9.logw("Unknown group owner " + groupOwner);
                                } else {
                                    String ssid = P2pStateMachine.this.mGroup.getNetworkName();
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                                        p2pStateMachine10.logd("Derive device name from ssid= " + ssid);
                                    }
                                    if (ssid != null) {
                                        groupOwner.deviceName = ssid.replaceFirst("DIRECT-([A-Za-z0-9]{2})-", "");
                                    }
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                                        p2pStateMachine11.logd("Unknown peer, updating to groupOwner: " + groupOwner);
                                    }
                                    P2pStateMachine.this.mPeers.updateSupplicantDetails(groupOwner);
                                    P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                }
                            }
                            P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                            p2pStateMachine12.transitionTo(p2pStateMachine12.mGroupCreatedState);
                            break;
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /* 147486 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                        }
                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(0);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine p2pStateMachine52 = P2pStateMachine.this;
                        p2pStateMachine52.transitionTo(p2pStateMachine52.mInactiveState);
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT /* 147487 */:
                    default:
                        return false;
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /* 147488 */:
                        P2pStatus status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                            p2pStateMachine13.loge("Invitation result " + status);
                            if (status != P2pStatus.UNKNOWN_P2P_GROUP) {
                                if (status != P2pStatus.INFORMATION_IS_CURRENTLY_UNAVAILABLE) {
                                    if (status != P2pStatus.NO_COMMON_CHANNEL) {
                                        WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(5);
                                        P2pStateMachine.this.handleGroupCreationFailure();
                                        P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                                        p2pStateMachine14.transitionTo(p2pStateMachine14.mInactiveState);
                                        break;
                                    } else {
                                        P2pStateMachine p2pStateMachine15 = P2pStateMachine.this;
                                        p2pStateMachine15.transitionTo(p2pStateMachine15.mFrequencyConflictState);
                                        break;
                                    }
                                } else {
                                    P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                    P2pStateMachine p2pStateMachine16 = P2pStateMachine.this;
                                    p2pStateMachine16.p2pConnectWithPinDisplay(p2pStateMachine16.mSavedPeerConfig);
                                    break;
                                }
                            } else {
                                int netId = P2pStateMachine.this.mSavedPeerConfig.netId;
                                if (netId >= 0) {
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine.this.logd("Remove unknown client from the list");
                                    }
                                    P2pStateMachine p2pStateMachine17 = P2pStateMachine.this;
                                    p2pStateMachine17.removeClientFromList(netId, p2pStateMachine17.mSavedPeerConfig.deviceAddress, true);
                                }
                                P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                P2pStateMachine p2pStateMachine18 = P2pStateMachine.this;
                                p2pStateMachine18.p2pConnectWithPinDisplay(p2pStateMachine18.mSavedPeerConfig);
                                break;
                            }
                        }
                        break;
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class FrequencyConflictState extends State {
            private AlertDialog mFrequencyConflictDialog;

            FrequencyConflictState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                if (WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService != null) {
                    OppoWifiP2pNotifyService oppoWifiP2pNotifyService = WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService;
                    Context context = WifiP2pServiceImpl.this.mContext;
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    this.mFrequencyConflictDialog = oppoWifiP2pNotifyService.notifyFrequencyConflict(context, p2pStateMachine.getDeviceName(p2pStateMachine.mSavedPeerConfig.deviceAddress));
                }
            }

            private void notifyFrequencyConflict() {
                P2pStateMachine.this.logd("Notify frequency conflict");
                Resources r = Resources.getSystem();
                AlertDialog.Builder builder = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext);
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                AlertDialog dialog = builder.setMessage(r.getString(17041292, p2pStateMachine.getDeviceName(p2pStateMachine.mSavedPeerConfig.deviceAddress))).setPositiveButton(r.getString(17039872), new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.FrequencyConflictState.AnonymousClass3 */

                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17039837), new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.FrequencyConflictState.AnonymousClass2 */

                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.FrequencyConflictState.AnonymousClass1 */

                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
                this.mFrequencyConflictDialog = dialog;
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                int i = message.what;
                if (i != 143373) {
                    switch (i) {
                        case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /* 143364 */:
                            if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                                WifiP2pServiceImpl.this.mWifiChannel.sendMessage((int) WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 1);
                            } else {
                                P2pStateMachine.this.loge("DROP_WIFI_USER_ACCEPT message received when WifiChannel is null");
                            }
                            WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = true;
                            break;
                        case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /* 143365 */:
                            WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(6);
                            P2pStateMachine.this.handleGroupCreationFailure();
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.transitionTo(p2pStateMachine2.mInactiveState);
                            break;
                        default:
                            switch (i) {
                                case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /* 147481 */:
                                case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /* 147483 */:
                                    P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                    p2pStateMachine3.loge(getName() + "group sucess during freq conflict!");
                                    break;
                                case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /* 147482 */:
                                case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /* 147484 */:
                                case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /* 147486 */:
                                    break;
                                case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /* 147485 */:
                                    P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                    p2pStateMachine4.loge(getName() + "group started after freq conflict, handle anyway");
                                    P2pStateMachine.this.deferMessage(message);
                                    P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                    p2pStateMachine5.transitionTo(p2pStateMachine5.mGroupNegotiationState);
                                    break;
                                default:
                                    return false;
                            }
                    }
                } else {
                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.logd(getName() + "Wifi disconnected, retry p2p");
                    }
                    P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                    p2pStateMachine7.transitionTo(p2pStateMachine7.mInactiveState);
                    P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                    p2pStateMachine8.sendMessage(139271, p2pStateMachine8.mSavedPeerConfig);
                }
                return true;
            }

            public void exit() {
                AlertDialog alertDialog = this.mFrequencyConflictDialog;
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class GroupCreatedState extends State {
            GroupCreatedState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mOppoWifiP2pStats != null) {
                    WifiP2pServiceImpl.this.mOppoWifiP2pStats.setP2pSuccessStats(P2pStateMachine.this.mGroup);
                }
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, null);
                P2pStateMachine.this.updateThisDevice(0);
                if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                    P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.SERVER_ADDRESS));
                }
                if (WifiP2pServiceImpl.this.mAutonomousGroup) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                }
                WifiP2pServiceImpl.this.mWifiP2pMetrics.endConnectionEvent(1);
                WifiP2pServiceImpl.this.mWifiP2pMetrics.startGroupEvent(P2pStateMachine.this.mGroup);
            }

            public boolean processMessage(Message message) {
                int netId;
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case 139271:
                        if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(P2pStateMachine.this.getCallingPkgName(message.sendingUid, message.replyTo), message.sendingUid, false)) {
                            P2pStateMachine.this.replyToMessage(message, 139272);
                            break;
                        } else {
                            WifiP2pConfig config = (WifiP2pConfig) message.obj;
                            if (P2pStateMachine.this.isConfigInvalid(config)) {
                                P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                p2pStateMachine2.loge("Dropping connect request " + config);
                                P2pStateMachine.this.replyToMessage(message, 139272);
                                break;
                            } else {
                                P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                p2pStateMachine3.logd("Inviting device : " + config.deviceAddress);
                                P2pStateMachine.this.mSavedPeerConfig = config;
                                if (P2pStateMachine.this.mWifiNative.p2pInvite(P2pStateMachine.this.mGroup, config.deviceAddress)) {
                                    P2pStateMachine.this.mPeers.updateStatus(config.deviceAddress, 1);
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                    P2pStateMachine.this.replyToMessage(message, 139273);
                                    break;
                                } else {
                                    P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139272, 0);
                                    break;
                                }
                            }
                        }
                    case 139280:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                            p2pStateMachine4.logd(getName() + " remove group");
                        }
                        if (P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            p2pStateMachine5.transitionTo(p2pStateMachine5.mOngoingGroupRemovalState);
                            P2pStateMachine.this.replyToMessage(message, 139282);
                            break;
                        } else {
                            P2pStateMachine.this.handleGroupRemoved();
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.transitionTo(p2pStateMachine6.mInactiveState);
                            P2pStateMachine.this.replyToMessage((P2pStateMachine) message, (Message) 139281, 0);
                            break;
                        }
                    case 139326:
                        WpsInfo wps = (WpsInfo) message.obj;
                        int i = 139327;
                        if (wps == null) {
                            P2pStateMachine.this.replyToMessage(message, 139327);
                            break;
                        } else {
                            boolean ret = true;
                            if (wps.setup == 0) {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                            } else if (wps.pin == null) {
                                String pin = P2pStateMachine.this.mWifiNative.startWpsPinDisplay(P2pStateMachine.this.mGroup.getInterface(), null);
                                try {
                                    Integer.parseInt(pin);
                                    P2pStateMachine.this.notifyInvitationSent(pin, "any");
                                } catch (NumberFormatException e) {
                                    ret = false;
                                }
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), wps.pin);
                            }
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            if (ret) {
                                i = 139328;
                            }
                            p2pStateMachine7.replyToMessage(message, i);
                            break;
                        }
                    case WifiP2pServiceImpl.DISABLE_P2P /* 143377 */:
                        P2pStateMachine.this.sendMessage(139280);
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION /* 143390 */:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), false);
                        try {
                            WifiP2pServiceImpl.this.mIpClient.completedPreDhcpAction();
                            break;
                        } catch (RemoteException e2) {
                            e2.rethrowFromSystemServer();
                            break;
                        }
                    case WifiP2pServiceImpl.IPC_POST_DHCP_ACTION /* 143391 */:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), true);
                        break;
                    case WifiP2pServiceImpl.IPC_DHCP_RESULTS /* 143392 */:
                        WifiP2pServiceImpl.this.mDhcpResults = (DhcpResults) message.obj;
                        break;
                    case WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS /* 143393 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                            p2pStateMachine8.logd("mDhcpResults: " + WifiP2pServiceImpl.this.mDhcpResults);
                        }
                        if (WifiP2pServiceImpl.this.mDhcpResults.serverAddress == null) {
                            WifiP2pServiceImpl.this.mDhcpResults.setServerAddress((Inet4Address) InetAddresses.parseNumericAddress(WifiP2pServiceImpl.SERVER_ADDRESS));
                        }
                        P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT);
                        if (WifiP2pServiceImpl.this.mDhcpResults != null) {
                            P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                            p2pStateMachine9.setWifiP2pInfoOnGroupFormation(WifiP2pServiceImpl.this.mDhcpResults.serverAddress);
                        }
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        try {
                            String ifname = P2pStateMachine.this.mGroup.getInterface();
                            if (WifiP2pServiceImpl.this.mDhcpResults != null) {
                                WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(ifname, WifiP2pServiceImpl.this.mDhcpResults.getRoutes(ifname));
                                break;
                            }
                        } catch (Exception e3) {
                            P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                            p2pStateMachine10.loge("Failed to add iface to local network " + e3);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE /* 143394 */:
                        if (WifiP2pServiceImpl.this.mOppoWifiP2pStats != null) {
                            WifiP2pServiceImpl.this.mOppoWifiP2pStats.setP2pFailStats(P2pStateMachine.this.mGroup, OppoWifiP2pStats.IPC_PROVISIONING_FAILURE_STATS);
                        }
                        P2pStateMachine.this.loge("IP provisioning failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /* 147478 */:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            return false;
                        }
                        WifiP2pDevice device = (WifiP2pDevice) message.obj;
                        if (!P2pStateMachine.this.mGroup.contains(device)) {
                            return false;
                        }
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                            p2pStateMachine11.logd("Add device to lost list " + device);
                        }
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return true;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /* 147485 */:
                        P2pStateMachine.this.loge("Duplicate group creation event notice, ignore");
                        break;
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /* 147486 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                            p2pStateMachine12.logd(getName() + " group removed");
                        }
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                        p2pStateMachine13.transitionTo(p2pStateMachine13.mInactiveState);
                        WifiP2pServiceImpl.this.mWifiDisplayUsageHelper.reportP2pConnectionTime(Long.valueOf(System.currentTimeMillis() - WifiP2pServiceImpl.this.mp2pStartedTime), WifiP2pServiceImpl.this.mContext);
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /* 147488 */:
                        P2pStatus status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                            p2pStateMachine14.loge("Invitation result " + status);
                            if (status == P2pStatus.UNKNOWN_P2P_GROUP && (netId = P2pStateMachine.this.mGroup.getNetworkId()) >= 0) {
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine.this.logd("Remove unknown client from the list");
                                }
                                P2pStateMachine p2pStateMachine15 = P2pStateMachine.this;
                                p2pStateMachine15.removeClientFromList(netId, p2pStateMachine15.mSavedPeerConfig.deviceAddress, false);
                                P2pStateMachine p2pStateMachine16 = P2pStateMachine.this;
                                p2pStateMachine16.sendMessage(139271, p2pStateMachine16.mSavedPeerConfig);
                                break;
                            }
                        }
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /* 147489 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /* 147491 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /* 147492 */:
                        WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                        P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                        if (!(provDisc == null || provDisc.device == null)) {
                            P2pStateMachine.this.mSavedPeerConfig.deviceAddress = provDisc.device.deviceAddress;
                        }
                        if (message.what == 147491) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                        } else if (message.what == 147492) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                        } else {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                        }
                        if (!P2pStateMachine.this.mGroup.isGroupOwner()) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                P2pStateMachine.this.logd("Ignore provision discovery for GC");
                                break;
                            }
                        } else {
                            P2pStateMachine p2pStateMachine17 = P2pStateMachine.this;
                            p2pStateMachine17.transitionTo(p2pStateMachine17.mUserAuthorizingJoinState);
                            break;
                        }
                        break;
                    case 147497:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        } else {
                            WifiP2pDevice device2 = (WifiP2pDevice) message.obj;
                            String deviceAddress = device2.deviceAddress;
                            if (deviceAddress != null) {
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 3);
                                if (P2pStateMachine.this.mGroup.removeClient(deviceAddress)) {
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine p2pStateMachine18 = P2pStateMachine.this;
                                        p2pStateMachine18.logd("Removed client " + deviceAddress);
                                    }
                                    if (WifiP2pServiceImpl.this.mAutonomousGroup || !P2pStateMachine.this.mGroup.isClientListEmpty()) {
                                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                                    } else {
                                        P2pStateMachine.this.logd("Client list empty, remove non-persistent p2p group");
                                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                    }
                                    WifiP2pServiceImpl.this.mWifiP2pMetrics.updateGroupEvent(P2pStateMachine.this.mGroup);
                                } else {
                                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                        P2pStateMachine p2pStateMachine19 = P2pStateMachine.this;
                                        p2pStateMachine19.logd("Failed to remove client " + deviceAddress);
                                    }
                                    for (WifiP2pDevice c : P2pStateMachine.this.mGroup.getClientList()) {
                                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                            P2pStateMachine p2pStateMachine20 = P2pStateMachine.this;
                                            p2pStateMachine20.logd("client " + c.deviceAddress);
                                        }
                                    }
                                }
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine21 = P2pStateMachine.this;
                                    p2pStateMachine21.logd(getName() + " ap sta disconnected");
                                    break;
                                }
                            } else {
                                P2pStateMachine p2pStateMachine22 = P2pStateMachine.this;
                                p2pStateMachine22.loge("Disconnect on unknown device: " + device2);
                                break;
                            }
                        }
                        break;
                    case 147498:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        } else {
                            String deviceAddress2 = ((WifiP2pDevice) message.obj).deviceAddress;
                            P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 0);
                            if (deviceAddress2 != null) {
                                if (P2pStateMachine.this.mPeers.get(deviceAddress2) != null) {
                                    P2pStateMachine.this.mGroup.addClient(P2pStateMachine.this.mPeers.get(deviceAddress2));
                                } else {
                                    P2pStateMachine.this.mGroup.addClient(deviceAddress2);
                                }
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress2, 0);
                                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                    P2pStateMachine p2pStateMachine23 = P2pStateMachine.this;
                                    p2pStateMachine23.logd(getName() + " ap sta connected");
                                }
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                WifiP2pServiceImpl.this.mWifiP2pMetrics.updateGroupEvent(P2pStateMachine.this.mGroup);
                                P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT);
                            } else {
                                P2pStateMachine.this.loge("Connect on null device address, ignore");
                            }
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            break;
                        }
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                WifiP2pServiceImpl.this.mWifiP2pMetrics.endGroupEvent();
                P2pStateMachine.this.updateThisDevice(3);
                P2pStateMachine.this.resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
            }
        }

        /* access modifiers changed from: package-private */
        public class UserAuthorizingJoinState extends State {
            UserAuthorizingJoinState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
                P2pStateMachine.this.notifyInvitationReceived();
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                switch (message.what) {
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /* 143362 */:
                        P2pStateMachine.this.mWifiController.setP2pIsConnectingFlag(true);
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        if (WifiP2pServiceImpl.this.mWifiInjector == null) {
                            WifiP2pServiceImpl.this.mWifiInjector = WifiInjector.getInstance();
                        }
                        WifiP2pServiceImpl.this.mWifiInjector.getOppoWifiSharingManager().stopWifiSharingTetheringIfNeed();
                        if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                            P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                        } else {
                            P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), P2pStateMachine.this.mSavedPeerConfig.wps.pin);
                        }
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.transitionTo(p2pStateMachine2.mGroupCreatedState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /* 143363 */:
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            P2pStateMachine.this.logd("User rejected incoming request");
                        }
                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                        p2pStateMachine3.transitionTo(p2pStateMachine3.mGroupCreatedState);
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /* 147489 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /* 147491 */:
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /* 147492 */:
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        /* access modifiers changed from: package-private */
        public class OngoingGroupRemovalState extends State {
            OngoingGroupRemovalState() {
            }

            public void enter() {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine.this.logd(getName());
                }
            }

            public boolean processMessage(Message message) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                }
                if (message.what != 139280) {
                    return false;
                }
                P2pStateMachine.this.replyToMessage(message, 139282);
                return true;
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            WifiP2pServiceImpl.super.dump(fd, pw, args);
            pw.println("mWifiP2pInfo " + this.mWifiP2pInfo);
            pw.println("mGroup " + this.mGroup);
            pw.println("mSavedPeerConfig " + this.mSavedPeerConfig);
            pw.println("mGroups" + this.mGroups);
            pw.println();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void checkAndReEnableP2p() {
            boolean isHalInterfaceAvailable = isHalInterfaceAvailable();
            Log.d(WifiP2pServiceImpl.TAG, "Wifi enabled=" + this.mIsWifiEnabled + ", P2P Interface availability=" + isHalInterfaceAvailable + ", Number of clients=" + WifiP2pServiceImpl.this.mDeathDataByBinder.size());
            if (this.mIsWifiEnabled && isHalInterfaceAvailable && !WifiP2pServiceImpl.this.mDeathDataByBinder.isEmpty()) {
                sendMessage(WifiP2pServiceImpl.ENABLE_P2P);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isHalInterfaceAvailable() {
            if (this.mWifiNative.isHalInterfaceSupported()) {
                return this.mIsHalInterfaceAvailable;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void checkAndSendP2pStateChangedBroadcast() {
            boolean isHalInterfaceAvailable = isHalInterfaceAvailable();
            Log.d(WifiP2pServiceImpl.TAG, "Wifi enabled=" + this.mIsWifiEnabled + ", P2P Interface availability=" + isHalInterfaceAvailable);
            sendP2pStateChangedBroadcast(this.mIsWifiEnabled && isHalInterfaceAvailable);
        }

        private void sendP2pStateChangedBroadcast(boolean enabled) {
            Intent intent = new Intent("android.net.wifi.p2p.STATE_CHANGED");
            intent.addFlags(67108864);
            if (enabled) {
                intent.putExtra("wifi_p2p_state", 2);
            } else {
                intent.putExtra("wifi_p2p_state", 1);
            }
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendP2pDiscoveryChangedBroadcast(boolean started) {
            int i;
            if (WifiP2pServiceImpl.this.mDiscoveryStarted != started) {
                WifiP2pServiceImpl.this.mDiscoveryStarted = started;
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    logd("discovery change broadcast " + started);
                }
                Intent intent = new Intent("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
                intent.addFlags(67108864);
                if (started) {
                    i = 2;
                } else {
                    i = 1;
                }
                intent.putExtra("discoveryState", i);
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendThisDeviceChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("wifiP2pDevice", eraseOwnDeviceAddress(WifiP2pServiceImpl.this.mThisDevice));
            WifiP2pServiceImpl.this.mContext.sendBroadcastAsUserMultiplePermissions(intent, UserHandle.ALL, WifiP2pServiceImpl.RECEIVER_PERMISSIONS_FOR_BROADCAST);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendPeersChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
            intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendBroadcastAsUserMultiplePermissions(intent, UserHandle.ALL, WifiP2pServiceImpl.RECEIVER_PERMISSIONS_FOR_BROADCAST);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendP2pConnectionChangedBroadcast() {
            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                logd("sending p2p connection changed broadcast");
            }
            Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
            intent.putExtra("networkInfo", new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            intent.putExtra("p2pGroupInfo", eraseOwnDeviceAddress(this.mGroup));
            WifiP2pServiceImpl.this.mContext.sendBroadcastAsUserMultiplePermissions(intent, UserHandle.ALL, WifiP2pServiceImpl.RECEIVER_PERMISSIONS_FOR_BROADCAST);
            if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage((int) WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            } else {
                loge("sendP2pConnectionChangedBroadcast(): WifiChannel is null");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendP2pPersistentGroupsChangedBroadcast() {
            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                logd("sending p2p persistent groups changed broadcast");
            }
            Intent intent = new Intent("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startDhcpServer(String intf) {
            try {
                InterfaceConfiguration ifcg = WifiP2pServiceImpl.this.mNwService.getInterfaceConfig(intf);
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.SERVER_ADDRESS), 24));
                ifcg.setInterfaceUp();
                WifiP2pServiceImpl.this.mNwService.setInterfaceConfig(intf, ifcg);
                String[] tetheringDhcpRanges = ((ConnectivityManager) WifiP2pServiceImpl.this.mContext.getSystemService("connectivity")).getTetheredDhcpRanges();
                if (WifiP2pServiceImpl.this.mNwService.isTetheringStarted()) {
                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                        logd("Stop existing tethering and restart it");
                    }
                    WifiP2pServiceImpl.this.mNwService.stopTethering();
                }
                WifiP2pServiceImpl.this.mNwService.tetherInterface(intf);
                WifiP2pServiceImpl.this.mNwService.startTethering(tetheringDhcpRanges);
                logd("Started Dhcp server on " + intf);
            } catch (Exception e) {
                loge("Error configuring interface " + intf + ", :" + e);
            }
        }

        private void stopDhcpServer(String intf) {
            String str = "Stopped Dhcp server";
            try {
                WifiP2pServiceImpl.this.mNwService.untetherInterface(intf);
                String[] listTetheredInterfaces = WifiP2pServiceImpl.this.mNwService.listTetheredInterfaces();
                for (String temp : listTetheredInterfaces) {
                    logd("List all interfaces " + temp);
                    if (temp.compareTo(intf) != 0) {
                        str = "Found other tethering interfaces, so keep tethering alive";
                        logd(str);
                        return;
                    }
                }
                WifiP2pServiceImpl.this.mNwService.stopTethering();
                logd(str);
            } catch (Exception e) {
                loge("Error stopping Dhcp server" + e);
            } finally {
                logd(str);
            }
        }

        private void notifyP2pEnableFailure() {
            Resources r = Resources.getSystem();
            AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041287)).setMessage(r.getString(17041291)).setPositiveButton(r.getString(17039370), (DialogInterface.OnClickListener) null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setType(2003);
            WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void addRowToDialog(ViewGroup group, int stringId, String value) {
            Resources r = Resources.getSystem();
            View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367340, group, false);
            ((TextView) row.findViewById(16909136)).setText(r.getString(stringId));
            ((TextView) row.findViewById(16909539)).setText(value);
            group.addView(row);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyInvitationSent(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367339, (ViewGroup) null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909018);
            addRowToDialog(group, 17041297, getDeviceName(peerAddress));
            addRowToDialog(group, 17041296, pin);
            AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041294)).setView(textEntryView).setPositiveButton(r.getString(17039370), (DialogInterface.OnClickListener) null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setType(2003);
            WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyP2pProvDiscShowPinRequest(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367339, (ViewGroup) null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909018);
            addRowToDialog(group, 17041297, getDeviceName(peerAddress));
            addRowToDialog(group, 17041296, pin);
            AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041294)).setView(textEntryView).setPositiveButton(r.getString(17039430), new DialogInterface.OnClickListener() {
                /* class com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.AnonymousClass5 */

                public void onClick(DialogInterface dialog, int which) {
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_CONFIRM);
                }
            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setType(2003);
            WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyInvitationReceived() {
            boolean isExternalShareConnect = WifiP2pServiceImpl.this.isExternalShareConnect(this.mSavedPeerConfig.deviceAddress);
            if (WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService != null) {
                OppoWifiP2pNotifyService oppoWifiP2pNotifyService = WifiP2pServiceImpl.this.mOppoWifiP2pNotifyService;
                Context context = WifiP2pServiceImpl.this.mContext;
                WifiP2pConfig wifiP2pConfig = this.mSavedPeerConfig;
                oppoWifiP2pNotifyService.notifyInvitationReceived(context, wifiP2pConfig, getDeviceName(wifiP2pConfig.deviceAddress), getName(), isExternalShareConnect);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updatePersistentNetworks(boolean reload) {
            if (reload) {
                this.mGroups.clear();
            }
            if (WifiP2pServiceImpl.this.mThisDevice == null || WifiP2pServiceImpl.this.mThisDevice.deviceAddress == null || WifiP2pServiceImpl.this.mThisDevice.deviceAddress == "") {
                Log.e(WifiP2pServiceImpl.TAG, "Invalid deviceAddress ");
                return;
            }
            String deviceAddress = WifiP2pServiceImpl.this.mThisDevice.deviceAddress;
            if (this.mWifiNative.p2pListNetworks(this.mGroups) || reload) {
                for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                    if (group == null || group.getOwner() == null) {
                        Log.e(WifiP2pServiceImpl.TAG, "Group owner information is empty");
                    } else if (deviceAddress.equals(group.getOwner().deviceAddress)) {
                        group.setOwner(WifiP2pServiceImpl.this.mThisDevice);
                    }
                }
                this.mWifiNative.saveConfig();
                WifiP2pServiceImpl.this.mWifiP2pMetrics.updatePersistentGroup(this.mGroups);
                sendP2pPersistentGroupsChangedBroadcast();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isConfigInvalid(WifiP2pConfig config) {
            if (config == null || TextUtils.isEmpty(config.deviceAddress) || this.mPeers.get(config.deviceAddress) == null) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isConfigValidAsGroup(WifiP2pConfig config) {
            if (config != null && !TextUtils.isEmpty(config.deviceAddress) && !TextUtils.isEmpty(config.networkName) && !TextUtils.isEmpty(config.passphrase)) {
                return true;
            }
            return false;
        }

        private WifiP2pDevice fetchCurrentDeviceDetails(WifiP2pConfig config) {
            if (config == null) {
                return null;
            }
            this.mPeers.updateGroupCapability(config.deviceAddress, this.mWifiNative.getGroupCapability(config.deviceAddress));
            return this.mPeers.get(config.deviceAddress);
        }

        private WifiP2pDevice eraseOwnDeviceAddress(WifiP2pDevice device) {
            if (device == null) {
                return null;
            }
            WifiP2pDevice result = new WifiP2pDevice(device);
            if (device.deviceAddress != null && WifiP2pServiceImpl.this.mThisDevice.deviceAddress != null && device.deviceAddress.length() > 0 && WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                result.deviceAddress = WifiP2pServiceImpl.ANONYMIZED_DEVICE_ADDRESS;
            }
            return result;
        }

        private WifiP2pGroup eraseOwnDeviceAddress(WifiP2pGroup group) {
            if (group == null) {
                return null;
            }
            WifiP2pGroup result = new WifiP2pGroup(group);
            for (WifiP2pDevice originalDevice : group.getClientList()) {
                result.removeClient(originalDevice);
                result.addClient(eraseOwnDeviceAddress(originalDevice));
            }
            result.setOwner(eraseOwnDeviceAddress(group.getOwner()));
            return result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private WifiP2pDevice maybeEraseOwnDeviceAddress(WifiP2pDevice device, int uid) {
            if (device == null) {
                return null;
            }
            if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkLocalMacAddressPermission(uid)) {
                return new WifiP2pDevice(device);
            }
            return eraseOwnDeviceAddress(device);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private WifiP2pGroup maybeEraseOwnDeviceAddress(WifiP2pGroup group, int uid) {
            if (group == null) {
                return null;
            }
            if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkLocalMacAddressPermission(uid)) {
                return new WifiP2pGroup(group);
            }
            return eraseOwnDeviceAddress(group);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private WifiP2pGroupList maybeEraseOwnDeviceAddress(WifiP2pGroupList groupList, int uid) {
            if (groupList == null) {
                return null;
            }
            WifiP2pGroupList result = new WifiP2pGroupList();
            for (WifiP2pGroup group : groupList.getGroupList()) {
                result.add(maybeEraseOwnDeviceAddress(group, uid));
            }
            return result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void p2pConnectWithPinDisplay(WifiP2pConfig config) {
            String pin;
            if (config == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return;
            }
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            if (dev == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                return;
            }
            if (!OppoWifiP2pConnectionHelper.isNfcTriggered() || WifiP2pServiceImpl.this.mP2pConnectFreq == 0) {
                pin = this.mWifiNative.p2pConnect(config, dev.isGroupOwner());
            } else {
                pin = this.mWifiNative.p2pConnect(config, dev.isGroupOwner(), WifiP2pServiceImpl.this.mP2pConnectFreq);
            }
            try {
                Integer.parseInt(pin);
                notifyInvitationSent(pin, config.deviceAddress);
            } catch (NumberFormatException e) {
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean reinvokePersistentGroup(WifiP2pConfig config) {
            int netId;
            if (config == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return false;
            }
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            if (dev == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                return false;
            }
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                logd("target ssid is " + ssid + " join:" + join);
            }
            if (join && dev.isGroupLimit()) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    logd("target device reaches group limit.");
                }
                join = false;
            } else if (join && (netId = this.mGroups.getNetworkId(dev.deviceAddress, ssid)) >= 0) {
                if (!this.mWifiNative.p2pGroupAdd(netId)) {
                    return false;
                }
                return true;
            }
            if (join || !dev.isDeviceLimit()) {
                if (!join && dev.isInvitationCapable()) {
                    int netId2 = -2;
                    if (config.netId < 0) {
                        netId2 = this.mGroups.getNetworkId(dev.deviceAddress);
                    } else if (config.deviceAddress.equals(this.mGroups.getOwnerAddr(config.netId))) {
                        netId2 = config.netId;
                    }
                    if (netId2 < 0) {
                        netId2 = getNetworkIdFromClientList(dev.deviceAddress);
                    }
                    if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                        logd("netId related with " + dev.deviceAddress + " = " + netId2);
                    }
                    if (netId2 >= 0) {
                        if (this.mWifiNative.p2pReinvoke(netId2, dev.deviceAddress)) {
                            config.netId = netId2;
                            return true;
                        }
                        loge("p2pReinvoke() failed, update networks");
                        updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                        return false;
                    }
                }
                return false;
            }
            loge("target device reaches the device limit.");
            return false;
        }

        private int getNetworkIdFromClientList(String deviceAddress) {
            if (deviceAddress == null) {
                return -1;
            }
            for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                int netId = group.getNetworkId();
                String[] p2pClientList = getClientList(netId);
                if (p2pClientList != null) {
                    for (String client : p2pClientList) {
                        if (deviceAddress.equalsIgnoreCase(client)) {
                            return netId;
                        }
                    }
                    continue;
                }
            }
            return -1;
        }

        private String[] getClientList(int netId) {
            String p2pClients = this.mWifiNative.getP2pClientList(netId);
            if (p2pClients == null) {
                return null;
            }
            return p2pClients.split(" ");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean removeClientFromList(int netId, String addr, boolean isRemovable) {
            StringBuilder modifiedClientList = new StringBuilder();
            String[] currentClientList = getClientList(netId);
            boolean isClientRemoved = false;
            if (currentClientList != null) {
                boolean isClientRemoved2 = false;
                for (String client : currentClientList) {
                    if (!client.equalsIgnoreCase(addr)) {
                        modifiedClientList.append(" ");
                        modifiedClientList.append(client);
                    } else {
                        isClientRemoved2 = true;
                    }
                }
                isClientRemoved = isClientRemoved2;
            }
            if (modifiedClientList.length() == 0 && isRemovable) {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    logd("Remove unknown network");
                }
                this.mGroups.remove(netId);
                WifiP2pServiceImpl.this.mWifiP2pMetrics.updatePersistentGroup(this.mGroups);
                return true;
            } else if (!isClientRemoved) {
                return false;
            } else {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    logd("Modified client list: " + ((Object) modifiedClientList));
                }
                if (modifiedClientList.length() == 0) {
                    modifiedClientList.append("\"\"");
                }
                this.mWifiNative.setP2pClientList(netId, modifiedClientList.toString());
                this.mWifiNative.saveConfig();
                return true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setWifiP2pInfoOnGroupFormation(InetAddress serverInetAddress) {
            WifiP2pInfo wifiP2pInfo = this.mWifiP2pInfo;
            wifiP2pInfo.groupFormed = true;
            wifiP2pInfo.isGroupOwner = this.mGroup.isGroupOwner();
            this.mWifiP2pInfo.groupOwnerAddress = serverInetAddress;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetWifiP2pInfo() {
            WifiP2pInfo wifiP2pInfo = this.mWifiP2pInfo;
            wifiP2pInfo.groupFormed = false;
            wifiP2pInfo.isGroupOwner = false;
            wifiP2pInfo.groupOwnerAddress = null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getDeviceName(String deviceAddress) {
            WifiP2pDevice d = this.mPeers.get(deviceAddress);
            if (d != null) {
                return d.deviceName;
            }
            return deviceAddress;
        }

        private String getPersistedDeviceName() {
            String deviceName = WifiP2pServiceImpl.this.mFrameworkFacade.getStringSetting(WifiP2pServiceImpl.this.mContext, "wifi_p2p_device_name");
            if (deviceName == null) {
                return SystemProperties.get("ro.oppo.market.name", "OPPO");
            }
            return deviceName;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setAndPersistDeviceName(String devName) {
            if (devName == null) {
                return false;
            }
            if (!this.mWifiNative.setDeviceName(devName)) {
                loge("Failed to set device name " + devName);
                return false;
            }
            WifiP2pServiceImpl.this.mThisDevice.deviceName = devName;
            WifiP2pNative wifiP2pNative = this.mWifiNative;
            wifiP2pNative.setP2pSsidPostfix("-" + WifiP2pServiceImpl.this.mThisDevice.deviceName);
            WifiP2pServiceImpl.this.mFrameworkFacade.setStringSetting(WifiP2pServiceImpl.this.mContext, "wifi_p2p_device_name", devName);
            sendThisDeviceChangedBroadcast();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setWfdInfo(WifiP2pWfdInfo wfdInfo) {
            boolean success;
            if (!wfdInfo.isWfdEnabled()) {
                success = this.mWifiNative.setWfdEnable(false);
            } else {
                success = this.mWifiNative.setWfdEnable(true) && this.mWifiNative.setWfdDeviceInfo(wfdInfo.getDeviceInfoHex());
            }
            if (!success) {
                loge("Failed to set wfd properties");
                return false;
            }
            WifiP2pServiceImpl.this.mThisDevice.wfdInfo = wfdInfo;
            sendThisDeviceChangedBroadcast();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initializeP2pSettings() {
            WifiP2pServiceImpl.this.mThisDevice.deviceName = getPersistedDeviceName();
            this.mWifiNative.setP2pDeviceName(WifiP2pServiceImpl.this.mThisDevice.deviceName);
            WifiP2pNative wifiP2pNative = this.mWifiNative;
            wifiP2pNative.setP2pSsidPostfix("-" + WifiP2pServiceImpl.this.mThisDevice.deviceName);
            this.mWifiNative.setP2pDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
            this.mWifiNative.setConfigMethods("virtual_push_button physical_display keypad");
            WifiP2pServiceImpl.this.mThisDevice.deviceAddress = this.mWifiNative.p2pGetDeviceAddress();
            updateThisDevice(3);
            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                logd("DeviceAddress: " + WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
            }
            this.mWifiNative.p2pFlush();
            this.mWifiNative.p2pServiceFlush();
            WifiP2pServiceImpl.this.mServiceTransactionId = (byte) 0;
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            WifiInjector.getInstance().getOppoSoftapP2pBandControl().p2pChangeBand();
            updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
            enableVerboseLogging(WifiP2pServiceImpl.this.mVerboseLoggingEnabled ? 1 : 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateThisDevice(int status) {
            WifiP2pServiceImpl.this.mThisDevice.status = status;
            sendThisDeviceChangedBroadcast();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleGroupCreationFailure() {
            resetWifiP2pInfo();
            WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.FAILED, null, null);
            sendP2pConnectionChangedBroadcast();
            boolean peersChanged = this.mPeers.remove(this.mPeersLostDuringConnection);
            if (!TextUtils.isEmpty(this.mSavedPeerConfig.deviceAddress) && this.mPeers.remove(this.mSavedPeerConfig.deviceAddress) != null) {
                peersChanged = true;
            }
            if (peersChanged) {
                sendPeersChangedBroadcast();
            }
            this.mPeersLostDuringConnection.clear();
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            sendMessage(139265);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleGroupRemoved() {
            if (this.mGroup.isGroupOwner()) {
                stopDhcpServer(this.mGroup.getInterface());
            } else {
                if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                    logd("stop IpClient");
                }
                WifiP2pServiceImpl.this.stopIpClient();
                try {
                    WifiP2pServiceImpl.this.mNwService.removeInterfaceFromLocalNetwork(this.mGroup.getInterface());
                } catch (RemoteException e) {
                    loge("Failed to remove iface from local network " + e);
                }
            }
            try {
                WifiP2pServiceImpl.this.mNwService.clearInterfaceAddresses(this.mGroup.getInterface());
            } catch (Exception e2) {
                loge("Failed to clear addresses " + e2);
            }
            this.mWifiNative.setP2pGroupIdle(this.mGroup.getInterface(), 0);
            boolean peersChanged = false;
            for (WifiP2pDevice d : this.mGroup.getClientList()) {
                if (this.mPeers.remove(d)) {
                    peersChanged = true;
                }
            }
            if (this.mPeers.remove(this.mGroup.getOwner())) {
                peersChanged = true;
            }
            if (this.mPeers.remove(this.mPeersLostDuringConnection)) {
                peersChanged = true;
            }
            if (peersChanged) {
                sendPeersChangedBroadcast();
            }
            this.mGroup = null;
            this.mPeersLostDuringConnection.clear();
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                    WifiP2pServiceImpl.this.mWifiChannel.sendMessage((int) WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 0);
                } else {
                    loge("handleGroupRemoved(): WifiChannel is null");
                }
                WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void replyToMessage(Message msg, int what) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void replyToMessage(Message msg, int what, int arg1) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                dstMsg.arg1 = arg1;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void replyToMessage(Message msg, int what, Object obj) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                dstMsg.obj = obj;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        private Message obtainMessage(Message srcMsg) {
            Message msg = Message.obtain();
            msg.arg2 = srcMsg.arg2;
            return msg;
        }

        /* access modifiers changed from: protected */
        public void logd(String s) {
            Slog.d(WifiP2pServiceImpl.TAG, s);
        }

        /* access modifiers changed from: protected */
        public void loge(String s) {
            Slog.e(WifiP2pServiceImpl.TAG, s);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean updateSupplicantServiceRequest() {
            clearSupplicantServiceRequest();
            StringBuffer sb = new StringBuffer();
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                for (int i = 0; i < c.mReqList.size(); i++) {
                    WifiP2pServiceRequest req = (WifiP2pServiceRequest) c.mReqList.valueAt(i);
                    if (req != null) {
                        sb.append(req.getSupplicantQuery());
                    }
                }
            }
            if (sb.length() == 0) {
                return false;
            }
            WifiP2pServiceImpl.this.mServiceDiscReqId = this.mWifiNative.p2pServDiscReq(WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS, sb.toString());
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSupplicantServiceRequest() {
            if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                this.mWifiNative.p2pServDiscCancelReq(WifiP2pServiceImpl.this.mServiceDiscReqId);
                WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean addServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            if (m == null || req == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return false;
            }
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo == null) {
                return false;
            }
            WifiP2pServiceImpl.access$12104(WifiP2pServiceImpl.this);
            if (WifiP2pServiceImpl.this.mServiceTransactionId == 0) {
                WifiP2pServiceImpl.access$12104(WifiP2pServiceImpl.this);
            }
            req.setTransactionId(WifiP2pServiceImpl.this.mServiceTransactionId);
            clientInfo.mReqList.put(WifiP2pServiceImpl.this.mServiceTransactionId, req);
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return true;
            }
            return updateSupplicantServiceRequest();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            if (m == null || req == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
            }
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                boolean removed = false;
                int i = 0;
                while (true) {
                    if (i >= clientInfo.mReqList.size()) {
                        break;
                    } else if (req.equals(clientInfo.mReqList.valueAt(i))) {
                        removed = true;
                        clientInfo.mReqList.removeAt(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (removed && WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                    updateSupplicantServiceRequest();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearServiceRequests(Messenger m) {
            if (m == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return;
            }
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null && clientInfo.mReqList.size() != 0) {
                clientInfo.mReqList.clear();
                if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                    updateSupplicantServiceRequest();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean addLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
            if (m == null || servInfo == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                return false;
            }
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo == null || !clientInfo.mServList.add(servInfo)) {
                return false;
            }
            if (this.mWifiNative.p2pServiceAdd(servInfo)) {
                return true;
            }
            clientInfo.mServList.remove(servInfo);
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
            if (m == null || servInfo == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                return;
            }
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                this.mWifiNative.p2pServiceDel(servInfo);
                clientInfo.mServList.remove(servInfo);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearLocalServices(Messenger m) {
            if (m == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return;
            }
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                for (WifiP2pServiceInfo servInfo : clientInfo.mServList) {
                    this.mWifiNative.p2pServiceDel(servInfo);
                }
                clientInfo.mServList.clear();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearClientInfo(Messenger m) {
            clearLocalServices(m);
            clearServiceRequests(m);
            ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.remove(m);
            if (clientInfo != null) {
                logd("Client:" + clientInfo.mPackageName + " is removed");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendServiceResponse(WifiP2pServiceResponse resp) {
            if (resp == null) {
                Log.e(WifiP2pServiceImpl.TAG, "sendServiceResponse with null response");
                return;
            }
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                if (((WifiP2pServiceRequest) c.mReqList.get(resp.getTransactionId())) != null) {
                    Message msg = Message.obtain();
                    msg.what = 139314;
                    msg.arg1 = 0;
                    msg.arg2 = 0;
                    msg.obj = resp;
                    if (c.mMessenger != null) {
                        try {
                            c.mMessenger.send(msg);
                        } catch (RemoteException e) {
                            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                                logd("detect dead channel");
                            }
                            clearClientInfo(c.mMessenger);
                            return;
                        }
                    }
                }
            }
        }

        private void clearClientDeadChannels() {
            ArrayList<Messenger> deadClients = new ArrayList<>();
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                Message msg = Message.obtain();
                msg.what = 139313;
                msg.arg1 = 0;
                msg.arg2 = 0;
                msg.obj = null;
                if (c.mMessenger != null) {
                    try {
                        c.mMessenger.send(msg);
                    } catch (RemoteException e) {
                        if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                            logd("detect dead channel");
                        }
                        deadClients.add(c.mMessenger);
                    }
                }
            }
            Iterator<Messenger> it = deadClients.iterator();
            while (it.hasNext()) {
                clearClientInfo(it.next());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private ClientInfo getClientInfo(Messenger m, boolean createIfNotExist) {
            ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.get(m);
            if (clientInfo != null || !createIfNotExist) {
                return clientInfo;
            }
            if (WifiP2pServiceImpl.this.mVerboseLoggingEnabled) {
                logd("add a new client");
            }
            ClientInfo clientInfo2 = new ClientInfo(m);
            WifiP2pServiceImpl.this.mClientInfoList.put(m, clientInfo2);
            return clientInfo2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private WifiP2pDeviceList getPeers(String pkgName, int uid) {
            if (WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkCanAccessWifiDirect(pkgName, uid, true)) {
                return new WifiP2pDeviceList(this.mPeers);
            }
            return new WifiP2pDeviceList();
        }

        public boolean isP2pGroupNegotiationState() {
            if (getCurrentState() == this.mGroupCreatingState || getCurrentState() == this.mGroupNegotiationState || WifiP2pServiceImpl.this.mDiscoveryStarted) {
                return true;
            }
            return false;
        }

        private void setPendingFactoryReset(boolean pending) {
            WifiP2pServiceImpl.this.mFrameworkFacade.setIntegerSetting(WifiP2pServiceImpl.this.mContext, "wifi_p2p_pending_factory_reset", pending ? 1 : 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isPendingFactoryReset() {
            if (WifiP2pServiceImpl.this.mFrameworkFacade.getIntegerSetting(WifiP2pServiceImpl.this.mContext, "wifi_p2p_pending_factory_reset", 0) != 0) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean factoryReset(int uid) {
            String pkgName = WifiP2pServiceImpl.this.mContext.getPackageManager().getNameForUid(uid);
            UserManager userManager = WifiP2pServiceImpl.this.mWifiInjector.getUserManager();
            if (!WifiP2pServiceImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) || userManager.hasUserRestriction("no_network_reset") || userManager.hasUserRestriction("no_config_wifi")) {
                return false;
            }
            Log.i(WifiP2pServiceImpl.TAG, "factoryReset uid=" + uid + " pkg=" + pkgName);
            if (WifiP2pServiceImpl.this.mNetworkInfo.isAvailable()) {
                if (this.mWifiNative.p2pListNetworks(this.mGroups)) {
                    for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                        this.mWifiNative.removeP2pNetwork(group.getNetworkId());
                    }
                }
                updatePersistentNetworks(true);
                setPendingFactoryReset(false);
            } else {
                setPendingFactoryReset(true);
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getCallingPkgName(int uid, Messenger replyMessenger) {
            ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.get(replyMessenger);
            if (clientInfo != null) {
                return clientInfo.mPackageName;
            }
            if (uid == 1000) {
                return WifiP2pServiceImpl.this.mContext.getOpPackageName();
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearServicesForAllClients() {
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                clearLocalServices(c.mMessenger);
                clearServiceRequests(c.mMessenger);
            }
        }
    }

    public void saveExternalPeerAddress(String address) {
        if (!address.equals("")) {
            this.mExternalAddress = address;
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "peer external peer address is " + this.mExternalAddress);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isExternalShareConnect(String targetaddress) {
        if (this.mExternalAddress.equals("") || this.mExternalAddress.equalsIgnoreCase(EMPTY_DEVICE_ADDRESS) || targetaddress.equals("") || !targetaddress.equalsIgnoreCase(this.mExternalAddress)) {
            return false;
        }
        if (!this.mVerboseLoggingEnabled) {
            return true;
        }
        Log.d(TAG, "is external share connect keep accept wps");
        return true;
    }

    /* access modifiers changed from: private */
    public class ClientInfo {
        private Messenger mMessenger;
        private String mPackageName;
        private SparseArray<WifiP2pServiceRequest> mReqList;
        private List<WifiP2pServiceInfo> mServList;

        private ClientInfo(Messenger m) {
            this.mMessenger = m;
            this.mPackageName = null;
            this.mReqList = new SparseArray<>();
            this.mServList = new ArrayList();
        }
    }
}
