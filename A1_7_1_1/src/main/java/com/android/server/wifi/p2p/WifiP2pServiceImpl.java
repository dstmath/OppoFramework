package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.IWifiP2pManager.Stub;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pGroupList.GroupDeleteListener;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.link.WifiP2pLinkInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiLastResortWatchdog;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.mediatek.server.wifi.WifiNvRamAgent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class WifiP2pServiceImpl extends Stub {
    public static final int AUTO_ACCEPT_INVITATION_TIME_OUT = 143376;
    private static final int BASE = 143360;
    public static final int BLOCK_DISCOVERY = 143375;
    private static final int CONNECTED_DISCOVER_TIMEOUT_S = 25;
    private static final boolean DBG = true;
    private static final String DHCP_INFO_FILE = "/data/misc/dhcp/dnsmasq.p2p0.leases";
    public static final int DISABLED = 0;
    public static final int DISABLE_P2P_TIMED_OUT = 143366;
    private static final int DISABLE_P2P_WAIT_TIME_MS = 5000;
    public static final int DISCONNECT_WIFI_REQUEST = 143372;
    public static final int DISCONNECT_WIFI_RESPONSE = 143373;
    private static final int DISCOVER_TIMEOUT_S = 120;
    private static final int DROP_WIFI_USER_ACCEPT = 143364;
    private static final int DROP_WIFI_USER_REJECT = 143365;
    public static final int ENABLED = 1;
    private static final Boolean FORM_GROUP = null;
    public static final int GROUP_CREATING_TIMED_OUT = 143361;
    private static final int GROUP_CREATING_WAIT_TIME_MS = 120000;
    private static final int GROUP_IDLE_TIME_S = 10;
    private static final int IPM_DHCP_RESULTS = 143392;
    private static final int IPM_POST_DHCP_ACTION = 143391;
    private static final int IPM_PRE_DHCP_ACTION = 143390;
    private static final int IPM_PROVISIONING_FAILURE = 143394;
    private static final int IPM_PROVISIONING_SUCCESS = 143393;
    private static final Boolean JOIN_GROUP = null;
    private static final int M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE = 143381;
    private static final int M_P2P_DEVICE_FOUND_INVITATION = 143380;
    private static final String NETWORKTYPE = "WIFI_P2P";
    private static final Boolean NO_RELOAD = null;
    public static final int P2P_ACTIVE = 0;
    public static final int P2P_CONNECTION_CHANGED = 143371;
    public static final int P2P_FAST_PS = 2;
    public static final int P2P_FIND_SPECIAL_FREQ_TIME_OUT = 143377;
    public static final int P2P_MAX_PS = 1;
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    private static final int RECONN_FOR_INVITE_RES_INFO_UNAVAILABLE_TIME_MS = 120000;
    private static final Boolean RELOAD = null;
    private static final String SERVER_ADDRESS = "192.168.49.1";
    private static final int SET_BEAM_MODE = 143382;
    public static final int SET_MIRACAST_MODE = 143374;
    private static final String STATIC_CLIENT_ADDRESS = "192.168.49.2";
    private static final int STOP_P2P_MONITOR_WAIT_TIME_MS = 5000;
    private static final String TAG = "WifiP2pService";
    private static final String UNKNOWN_COMMAND = "UNKNOWN COMMAND";
    private static final int VENDOR_IE_ALL_FRAME_TAG = 99;
    private static final int VENDOR_IE_FRAME_ID_AMOUNTS = 12;
    private static final String VENDOR_IE_MTK_OUI = "000ce7";
    private static final String VENDOR_IE_OUI_TYPE__CROSSMOUNT = "33";
    private static final String VENDOR_IE_TAG = "dd";
    private static int mDisableP2pTimeoutIndex;
    private static int mGroupCreatingTimeoutIndex;
    private boolean WFD_DONGLE_USE_P2P_INVITE;
    private boolean isNfcTriggered;
    private boolean mAutonomousGroup;
    private ClientHandler mClientHandler;
    private HashMap<Messenger, ClientInfo> mClientInfoList;
    private boolean mConnectToPeer;
    private Context mContext;
    private boolean mCrossmountEventReceived;
    private boolean mCrossmountIEAdded;
    private String mCrossmountSessionInfo;
    private boolean mDelayReconnectForInfoUnavailable;
    private int mDeviceCapa;
    private DhcpResults mDhcpResults;
    private boolean mDiscoveryBlocked;
    private boolean mDiscoveryPostponed;
    private boolean mDiscoveryStarted;
    private int mFastConTriggeredNum;
    private int mFindTimes;
    private boolean mFoundTargetDevice;
    private boolean mGcIgnoresDhcpReq;
    private P2pStatus mGroupRemoveReason;
    private String mIntentToConnectPeer;
    private String mInterface;
    private IpManager mIpManager;
    private boolean mJoinExistingGroup;
    private boolean mMccSupport;
    private int mMiracastMode;
    boolean mNegoChannelConflict;
    private NetworkInfo mNetworkInfo;
    private WifiNative mNfcWifiNative;
    INetworkManagementService mNwService;
    private int mP2pConnectFreq;
    private int mP2pOperFreq;
    private P2pStateMachine mP2pStateMachine;
    private final boolean mP2pSupported;
    private AsyncChannel mReplyChannel;
    private String mServiceDiscReqId;
    private byte mServiceTransactionId;
    private int mStopP2pMonitorTimeoutIndex;
    private boolean mTemporarilyDisconnectedWifi;
    private WifiP2pDevice mThisDevice;
    private boolean mUpdatePeerForInvited;
    private String mWfdSourceAddr;
    private AsyncChannel mWifiChannel;
    private WifiManager mWifiManager;

    private class ClientHandler extends Handler {
        ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 139265:
                case 139268:
                case 139271:
                case 139274:
                case 139277:
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
                case 139339:
                case 139340:
                case 139342:
                case 139343:
                case 139349:
                case 139351:
                case 139354:
                case 139355:
                case 139356:
                case 139357:
                case 139360:
                case 139361:
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(Message.obtain(msg));
                    return;
                default:
                    Slog.d(WifiP2pServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }
    }

    private class ClientInfo {
        private Messenger mMessenger;
        private SparseArray<WifiP2pServiceRequest> mReqList;
        private List<WifiP2pServiceInfo> mServList;

        /* synthetic */ ClientInfo(WifiP2pServiceImpl this$0, Messenger m, ClientInfo clientInfo) {
            this(m);
        }

        private ClientInfo(Messenger m) {
            this.mMessenger = m;
            this.mReqList = new SparseArray();
            this.mServList = new ArrayList();
        }
    }

    private class P2pStateMachine extends StateMachine {
        private DefaultState mDefaultState = new DefaultState();
        private FrequencyConflictState mFrequencyConflictState = new FrequencyConflictState();
        private WifiP2pGroup mGroup;
        private GroupCreatedState mGroupCreatedState = new GroupCreatedState();
        private GroupCreatingState mGroupCreatingState = new GroupCreatingState();
        private GroupNegotiationState mGroupNegotiationState = new GroupNegotiationState();
        private final WifiP2pGroupList mGroups = new WifiP2pGroupList(null, new GroupDeleteListener() {
            public void onDeleteGroup(int netId) {
                P2pStateMachine.this.logd("called onDeleteGroup() netId=" + netId);
                P2pStateMachine.this.mWifiNative.removeNetwork(netId);
                P2pStateMachine.this.mWifiNative.saveConfig();
                P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
            }
        });
        private InactiveState mInactiveState = new InactiveState();
        private OngoingGroupRemovalState mOngoingGroupRemovalState = new OngoingGroupRemovalState();
        private P2pDisabledState mP2pDisabledState = new P2pDisabledState();
        private P2pDisablingState mP2pDisablingState = new P2pDisablingState();
        private P2pEnabledState mP2pEnabledState = new P2pEnabledState();
        private P2pEnablingState mP2pEnablingState = new P2pEnablingState();
        private P2pNotSupportedState mP2pNotSupportedState = new P2pNotSupportedState();
        private final WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
        private final WifiP2pDeviceList mPeersLostDuringConnection = new WifiP2pDeviceList();
        private ProvisionDiscoveryState mProvisionDiscoveryState = new ProvisionDiscoveryState();
        private WifiP2pConfig mSavedPeerConfig = new WifiP2pConfig();
        private UserAuthorizingInviteRequestState mUserAuthorizingInviteRequestState = new UserAuthorizingInviteRequestState();
        private UserAuthorizingJoinState mUserAuthorizingJoinState = new UserAuthorizingJoinState();
        private UserAuthorizingNegotiationRequestState mUserAuthorizingNegotiationRequestState = new UserAuthorizingNegotiationRequestState();
        private WifiMonitor mWifiMonitor = WifiMonitor.getInstance();
        private WifiNative mWifiNative = WifiNative.getP2pNativeInterface();
        private final WifiP2pInfo mWifiP2pInfo = new WifiP2pInfo();

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message message) {
                Object obj = null;
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 69632:
                        if (message.arg1 != 0) {
                            P2pStateMachine.this.loge("Full connection failure, error = " + message.arg1);
                            WifiP2pServiceImpl.this.mWifiChannel = null;
                            break;
                        }
                        P2pStateMachine.this.logd("Full connection with WifiStateMachine established");
                        WifiP2pServiceImpl.this.mWifiChannel = (AsyncChannel) message.obj;
                        break;
                    case 69633:
                        new AsyncChannel().connect(WifiP2pServiceImpl.this.mContext, P2pStateMachine.this.getHandler(), message.replyTo);
                        break;
                    case 69636:
                        if (message.arg1 == 2) {
                            P2pStateMachine.this.loge("Send failed, client connection lost");
                        } else {
                            P2pStateMachine.this.loge("Client connection lost with reason: " + message.arg1);
                        }
                        WifiP2pServiceImpl.this.mWifiChannel = null;
                        break;
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case 139329:
                    case 139332:
                    case 139335:
                    case 139354:
                    case 139355:
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /*143364*/:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /*143365*/:
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                    case WifiP2pServiceImpl.SET_BEAM_MODE /*143382*/:
                    case WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION /*143390*/:
                    case WifiP2pServiceImpl.IPM_POST_DHCP_ACTION /*143391*/:
                    case WifiP2pServiceImpl.IPM_DHCP_RESULTS /*143392*/:
                    case WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS /*143393*/:
                    case WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE /*143394*/:
                    case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                    case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                    case WifiMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                    case WifiMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                    case WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        break;
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
                        break;
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, 2);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage(message, 139269, 2);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage(message, 139272, 2);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage(message, 139275, 2);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage(message, 139278, 2);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139281, 2);
                        break;
                    case 139283:
                        P2pStateMachine.this.replyToMessage(message, 139284, (Object) new WifiP2pDeviceList(P2pStateMachine.this.mPeers));
                        break;
                    case 139285:
                        P2pStateMachine.this.replyToMessage(message, 139286, (Object) new WifiP2pInfo(P2pStateMachine.this.mWifiP2pInfo));
                        break;
                    case 139287:
                        P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                        if (P2pStateMachine.this.mGroup != null) {
                            obj = new WifiP2pGroup(P2pStateMachine.this.mGroup);
                        }
                        p2pStateMachine.replyToMessage(message, 139288, obj);
                        break;
                    case 139292:
                        P2pStateMachine.this.replyToMessage(message, 139293, 2);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage(message, 139296, 2);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage(message, 139299, 2);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage(message, 139302, 2);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage(message, 139305, 2);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage(message, 139308, 2);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage(message, 139311, 2);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage(message, 139316, 2);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage(message, 139318, 2);
                        break;
                    case 139321:
                        P2pStateMachine.this.replyToMessage(message, 139322, (Object) new WifiP2pGroupList(P2pStateMachine.this.mGroups, null));
                        break;
                    case 139323:
                        P2pStateMachine.this.replyToMessage(message, 139324, 2);
                        break;
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, 2);
                        break;
                    case 139339:
                    case 139340:
                        P2pStateMachine.this.replyToMessage(message, 139341, null);
                        break;
                    case 139342:
                    case 139343:
                        P2pStateMachine.this.replyToMessage(message, 139345, 2);
                        break;
                    case 139357:
                        P2pStateMachine.this.replyToMessage(message, 139358, 2);
                        break;
                    case 139360:
                        P2pStateMachine.this.replyToMessage(message, 139269, 2);
                        break;
                    case 139361:
                        P2pStateMachine.this.replyToMessage(message, 139361, 2);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        boolean z;
                        WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                        if (message.arg1 == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        wifiP2pServiceImpl.mDiscoveryBlocked = z;
                        WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            try {
                                message.obj.sendMessage(message.arg2);
                                break;
                            } catch (Exception e) {
                                P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                break;
                            }
                        }
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    default:
                        P2pStateMachine.this.loge("Unhandled message " + message);
                        return false;
                }
                return true;
            }
        }

        class FrequencyConflictState extends State {
            private AlertDialog mFrequencyConflictDialog;

            FrequencyConflictState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                if (WifiP2pServiceImpl.this.mMccSupport) {
                    P2pStateMachine.this.p2pSetCCMode(1);
                }
                if (!WifiP2pServiceImpl.this.mMccSupport && WifiP2pServiceImpl.this.mMiracastMode == 1) {
                    P2pStateMachine.this.sendP2pOPChannelBroadcast();
                } else if (WifiP2pServiceImpl.this.mMiracastMode == 2) {
                    P2pStateMachine.this.logd("[sink] channel conflict, disconnecting wifi by app layer");
                    P2pStateMachine.this.sendMessage(139356, 1);
                } else {
                    if (WifiP2pServiceImpl.this.mMccSupport) {
                        if (WifiP2pServiceImpl.this.mConnectToPeer) {
                            P2pStateMachine.this.logd(getName() + " SCC->MCC, mConnectToPeer=" + WifiP2pServiceImpl.this.mConnectToPeer + "\tP2pOperFreq=" + WifiP2pServiceImpl.this.mP2pOperFreq);
                            P2pStateMachine.this.mSavedPeerConfig.setPreferOperFreq(WifiP2pServiceImpl.this.mP2pOperFreq);
                            if (!P2pStateMachine.this.reinvokePersistentGroup(P2pStateMachine.this.mSavedPeerConfig)) {
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            }
                        } else {
                            P2pStateMachine.this.logd(getName() + " SCC->MCC, mConnectToPeer=" + WifiP2pServiceImpl.this.mConnectToPeer + "\tdo p2p_connect/p2p_invite again!");
                            WifiP2pServiceImpl.this.mP2pOperFreq = -1;
                            P2pStateMachine.this.mSavedPeerConfig.setPreferOperFreq(WifiP2pServiceImpl.this.mP2pOperFreq);
                            if (!P2pStateMachine.this.reinvokePersistentGroup(P2pStateMachine.this.mSavedPeerConfig)) {
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            }
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                    } else {
                        notifyFrequencyConflict();
                    }
                }
            }

            private void notifyFrequencyConflict() {
                P2pStateMachine.this.logd("Notify frequency conflict");
                Resources r = Resources.getSystem();
                Builder builder = new Builder(WifiP2pServiceImpl.this.mContext, 201523207);
                Object[] objArr = new Object[1];
                objArr[0] = P2pStateMachine.this.getDeviceName(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                AlertDialog dialog = builder.setTitle(r.getString(17040382, objArr)).setMessage(r.getString(17040380)).setPositiveButton(r.getString(17040383), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                LayoutParams p = dialog.getWindow().getAttributes();
                p.ignoreHomeMenuKey = 1;
                dialog.getWindow().setAttributes(p);
                dialog.getWindow().setType(2003);
                LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
                this.mFrequencyConflictDialog = dialog;
                TextView msg = (TextView) dialog.findViewById(16908299);
                if (msg != null) {
                    msg.setGravity(17);
                } else {
                    P2pStateMachine.this.loge("textview is null");
                }
            }

            private void notifyFrequencyConflictEx() {
                P2pStateMachine.this.logd("Notify frequency conflict enhancement! mP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                Resources r = Resources.getSystem();
                String localFreq = "";
                if (WifiP2pServiceImpl.this.mP2pOperFreq <= 0) {
                    P2pStateMachine.this.loge(getName() + " in-valid OP channel: " + WifiP2pServiceImpl.this.mP2pOperFreq);
                } else if (WifiP2pServiceImpl.this.mP2pOperFreq < 5000) {
                    localFreq = "2.4G band-" + new String("" + WifiP2pServiceImpl.this.mP2pOperFreq) + " MHz";
                } else {
                    localFreq = "5G band-" + new String("" + WifiP2pServiceImpl.this.mP2pOperFreq) + " MHz";
                }
                Builder builder = new Builder(WifiP2pServiceImpl.this.mContext);
                Object[] objArr = new Object[2];
                objArr[0] = P2pStateMachine.this.getDeviceName(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                objArr[1] = localFreq;
                AlertDialog dialog = builder.setMessage(r.getString(134545665, objArr)).setPositiveButton(r.getString(17040419), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17040373), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
                dialog.getWindow().setType(2003);
                dialog.show();
                this.mFrequencyConflictDialog = dialog;
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139356:
                        int accept = message.arg1;
                        P2pStateMachine.this.logd(getName() + " frequency confliect enhancement decision: " + accept + ", and mP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        if (1 != accept) {
                            notifyFrequencyConflictEx();
                            break;
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.mSavedPeerConfig.setPreferOperFreq(WifiP2pServiceImpl.this.mP2pOperFreq);
                        P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                        break;
                    case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /*143364*/:
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 1);
                        WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = true;
                        break;
                    case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /*143365*/:
                        WifiP2pServiceImpl.this.mGroupRemoveReason = P2pStatus.MTK_EXPAND_02;
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                        P2pStateMachine.this.logd(getName() + "Wifi disconnected, retry p2p");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        WifiP2pServiceImpl.this.mP2pOperFreq = -1;
                        P2pStateMachine.this.mSavedPeerConfig.setPreferOperFreq(WifiP2pServiceImpl.this.mP2pOperFreq);
                        P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.loge(getName() + "group sucess during freq conflict!");
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge(getName() + "group started after freq conflict, handle anyway");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                if (this.mFrequencyConflictDialog != null) {
                    this.mFrequencyConflictDialog.dismiss();
                }
            }
        }

        class GroupCreatedState extends State {
            GroupCreatedState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, null);
                P2pStateMachine.this.updateThisDevice(0);
                if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                    P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.SERVER_ADDRESS));
                } else if (P2pStateMachine.this.isWfdSinkConnected()) {
                    P2pStateMachine.this.logd(getName() + " [wfd sink] stop scan@GC, to avoid packet lost");
                    P2pStateMachine.this.mWifiNative.p2pStopFind();
                }
                if (WifiP2pServiceImpl.this.mAutonomousGroup) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                }
                if (WifiP2pServiceImpl.this.mGcIgnoresDhcpReq) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                }
                WifiP2pServiceImpl.this.resumeReconnectAndScan();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                WifiP2pDevice device;
                String deviceAddress;
                switch (message.what) {
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.sendMessage(139280);
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case 139265:
                        P2pStateMachine.this.clearSupplicantServiceRequest();
                        if (!P2pStateMachine.this.mWifiNative.p2pFind(25)) {
                            P2pStateMachine.this.replyToMessage(message, 139266, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139267);
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                        break;
                    case 139271:
                        WifiP2pConfig config = message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.logd("Inviting device : " + config.deviceAddress);
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            WifiP2pServiceImpl.this.mConnectToPeer = true;
                            if (!P2pStateMachine.this.mWifiNative.p2pInvite(P2pStateMachine.this.mGroup, config.deviceAddress)) {
                                P2pStateMachine.this.replyToMessage(message, 139272, 0);
                                break;
                            }
                            P2pStateMachine.this.mPeers.updateStatus(config.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping connect requeset " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139280:
                        P2pStateMachine.this.logd(getName() + " remove group");
                        if (!P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                            P2pStateMachine.this.handleGroupRemoved();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            P2pStateMachine.this.replyToMessage(message, 139281, 0);
                            break;
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                        P2pStateMachine.this.replyToMessage(message, 139282);
                        break;
                    case 139326:
                        WpsInfo wps = message.obj;
                        if (wps != null) {
                            int i;
                            boolean ret = true;
                            if (wps.setup == 0) {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                            } else if (wps.pin == null) {
                                String pin = P2pStateMachine.this.mWifiNative.startWpsPinDisplay(P2pStateMachine.this.mGroup.getInterface());
                                try {
                                    Integer.parseInt(pin);
                                    P2pStateMachine.this.notifyInvitationSent(pin, WifiLastResortWatchdog.BSSID_ANY);
                                } catch (NumberFormatException e) {
                                    ret = false;
                                }
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), wps.pin);
                            }
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            if (ret) {
                                i = 139328;
                            } else {
                                i = 139327;
                            }
                            p2pStateMachine.replyToMessage(message, i);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139327);
                        break;
                    case 139357:
                        String mac = null;
                        if (message.obj != null) {
                            mac = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_CLIENT_MESSAGE");
                        }
                        P2pStateMachine.this.logd("remove client, am I GO? " + P2pStateMachine.this.mGroup.getOwner().deviceAddress.equals(WifiP2pServiceImpl.this.mThisDevice.deviceAddress) + ", ths client is " + mac);
                        if (!P2pStateMachine.this.mGroup.getOwner().deviceAddress.equals(WifiP2pServiceImpl.this.mThisDevice.deviceAddress)) {
                            P2pStateMachine.this.replyToMessage(message, 139358, 0);
                            break;
                        }
                        if (!P2pStateMachine.this.p2pRemoveClient(P2pStateMachine.this.mGroup.getInterface(), mac)) {
                            P2pStateMachine.this.replyToMessage(message, 139358, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139359);
                        break;
                    case WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION /*143390*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), false);
                        WifiP2pServiceImpl.this.mIpManager.completedPreDhcpAction();
                        break;
                    case WifiP2pServiceImpl.IPM_POST_DHCP_ACTION /*143391*/:
                        break;
                    case WifiP2pServiceImpl.IPM_DHCP_RESULTS /*143392*/:
                        WifiP2pServiceImpl.this.mDhcpResults = (DhcpResults) message.obj;
                        break;
                    case WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS /*143393*/:
                        P2pStateMachine.this.logd("mDhcpResults: " + WifiP2pServiceImpl.this.mDhcpResults);
                        if (WifiP2pServiceImpl.this.mDhcpResults.serverAddress == null) {
                            WifiP2pServiceImpl.this.mDhcpResults.setServerAddress(WifiP2pServiceImpl.SERVER_ADDRESS);
                        }
                        P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT);
                        P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(WifiP2pServiceImpl.this.mDhcpResults.serverAddress);
                        try {
                            String ifname = P2pStateMachine.this.mGroup.getInterface();
                            WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(ifname, WifiP2pServiceImpl.this.mDhcpResults.getRoutes(ifname));
                        } catch (RemoteException e2) {
                            P2pStateMachine.this.loge("Failed to add iface to local network " + e2);
                        } catch (IllegalStateException ie) {
                            P2pStateMachine.this.loge("Failed to add iface to local network: IllegalStateException=" + ie);
                        }
                        if (WifiP2pServiceImpl.this.mDhcpResults != null) {
                            if (WifiP2pServiceImpl.this.mDhcpResults.serverAddress == null || !WifiP2pServiceImpl.this.mDhcpResults.serverAddress.toString().startsWith("/")) {
                                P2pStateMachine.this.mGroup.getOwner().deviceIP = "" + WifiP2pServiceImpl.this.mDhcpResults.serverAddress;
                            } else {
                                P2pStateMachine.this.mGroup.getOwner().deviceIP = WifiP2pServiceImpl.this.mDhcpResults.serverAddress.toString().substring(1);
                            }
                        }
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        break;
                    case WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE /*143394*/:
                        P2pStateMachine.this.loge("IP provisioning failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.loge("Supplicant close unexpected, send fake Group Remove event");
                        P2pStateMachine.this.sendMessage(WifiMonitor.P2P_GROUP_REMOVED_EVENT);
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                        WifiP2pDevice peerDevice = message.obj;
                        if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(peerDevice.deviceAddress)) {
                            if (P2pStateMachine.this.mGroup.contains(peerDevice)) {
                                peerDevice.status = 0;
                            }
                            P2pStateMachine.this.mPeers.update(peerDevice);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        device = (WifiP2pDevice) message.obj;
                        if (!P2pStateMachine.this.mGroup.contains(device)) {
                            return false;
                        }
                        P2pStateMachine.this.logd("Add device to lost list " + device);
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return true;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge("Duplicate group creation event notice, ignore");
                        break;
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        if (message.arg1 != 0) {
                            WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                        }
                        WifiP2pServiceImpl.this.mGroupRemoveReason = (P2pStatus) message.obj;
                        P2pStateMachine.this.logd(getName() + " group removed, reason: " + WifiP2pServiceImpl.this.mGroupRemoveReason + ", mP2pOperFreq: " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                        P2pStatus status = message.obj;
                        P2pStateMachine.this.logd("===> INVITATION RESULT EVENT : " + status + ",\tis GO ? : " + P2pStateMachine.this.mGroup.getOwner().deviceAddress.equals(WifiP2pServiceImpl.this.mThisDevice.deviceAddress));
                        boolean inviteDone = false;
                        if (status == P2pStatus.SUCCESS) {
                            inviteDone = true;
                        }
                        P2pStateMachine.this.loge("Invitation result " + status + ",\tis GO ? : " + P2pStateMachine.this.mGroup.getOwner().deviceAddress.equals(WifiP2pServiceImpl.this.mThisDevice.deviceAddress));
                        if (status == P2pStatus.UNKNOWN_P2P_GROUP) {
                            int netId = P2pStateMachine.this.mGroup.getNetworkId();
                            if (netId >= 0) {
                                P2pStateMachine.this.logd("Remove unknown client from the list");
                                if (!P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, false)) {
                                    P2pStateMachine.this.loge("Already removed the client, ignore");
                                    break;
                                }
                                P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                            }
                        } else if (status == P2pStatus.NO_COMMON_CHANNEL) {
                            if (WifiP2pServiceImpl.this.mMccSupport) {
                                P2pStateMachine.this.p2pSetCCMode(1);
                            }
                            inviteDone = true;
                        } else {
                            inviteDone = true;
                        }
                        if (inviteDone && !P2pStateMachine.this.mGroup.getOwner().deviceAddress.equals(WifiP2pServiceImpl.this.mThisDevice.deviceAddress) && P2pStateMachine.this.mPeers.remove(P2pStateMachine.this.mPeers.get(P2pStateMachine.this.mSavedPeerConfig.deviceAddress))) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            break;
                        }
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
                        if (!TextUtils.isEmpty(provDisc.device.deviceName)) {
                            P2pStateMachine.this.mPeers.update(provDisc.device);
                        }
                        P2pStateMachine.this.updateCrossMountInfo(provDisc.device.deviceAddress);
                        P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                        P2pStateMachine.this.mSavedPeerConfig.deviceAddress = provDisc.device.deviceAddress;
                        if (message.what == 147491) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                        } else if (message.what == 147492) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                        } else {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingJoinState);
                        break;
                    case WifiMonitor.P2P_PEER_DISCONNECT_EVENT /*147496*/:
                        int IEEE802_11_ReasonCode = -1;
                        if (message.obj != null) {
                            try {
                                IEEE802_11_ReasonCode = Integer.valueOf((String) message.obj).intValue();
                                if (IEEE802_11_ReasonCode == WifiP2pServiceImpl.VENDOR_IE_ALL_FRAME_TAG) {
                                    WifiP2pServiceImpl.this.mGroupRemoveReason = P2pStatus.NO_COMMON_CHANNEL;
                                }
                            } catch (NumberFormatException e3) {
                                P2pStateMachine.this.loge("Error! Format unexpected");
                            }
                        }
                        if (message.arg1 != 0) {
                            WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                        }
                        P2pStateMachine.this.loge(getName() + " I'm GC and has been disconnected by GO. IEEE 802.11 reason code: " + IEEE802_11_ReasonCode + ", mP2pOperFreq: " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.AP_STA_DISCONNECTED_EVENT /*147497*/:
                        device = (WifiP2pDevice) message.obj;
                        deviceAddress = device.deviceAddress;
                        if (deviceAddress == null) {
                            P2pStateMachine.this.loge("Disconnect on unknown device: " + device);
                            break;
                        }
                        P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 3);
                        if (P2pStateMachine.this.mGroup.removeClient(deviceAddress)) {
                            P2pStateMachine.this.logd("Removed client " + deviceAddress);
                            if (WifiP2pServiceImpl.this.mAutonomousGroup || !P2pStateMachine.this.mGroup.isClientListEmpty()) {
                                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            } else {
                                P2pStateMachine.this.logd("Client list empty, remove non-persistent p2p group");
                                P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            }
                        } else {
                            P2pStateMachine.this.logd("Failed to remove client " + deviceAddress);
                            for (WifiP2pDevice c : P2pStateMachine.this.mGroup.getClientList()) {
                                P2pStateMachine.this.logd("client " + c.deviceAddress);
                            }
                        }
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine.this.logd(getName() + " ap sta disconnected");
                        break;
                    case WifiMonitor.AP_STA_CONNECTED_EVENT /*147498*/:
                        device = message.obj;
                        deviceAddress = device.deviceAddress;
                        P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 0);
                        if (deviceAddress != null) {
                            if (P2pStateMachine.this.mPeers.get(deviceAddress) != null) {
                                P2pStateMachine.this.mGroup.addClient(P2pStateMachine.this.mPeers.get(deviceAddress));
                            } else {
                                device = P2pStateMachine.this.p2pGoGetSta(device, deviceAddress);
                                P2pStateMachine.this.mGroup.addClient(device);
                                P2pStateMachine.this.mPeers.update(device);
                            }
                            WifiP2pDevice gcLocal = P2pStateMachine.this.mPeers.get(deviceAddress);
                            gcLocal.interfaceAddress = device.interfaceAddress;
                            P2pStateMachine.this.mPeers.update(gcLocal);
                            P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 0);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            if (P2pStateMachine.this.isWfdSinkConnected()) {
                                P2pStateMachine.this.logd(getName() + " [wfd sink] stop scan@GO, to avoid packet lost");
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                            }
                            P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT);
                        } else {
                            P2pStateMachine.this.loge("Connect on null device address, ignore");
                        }
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.updateThisDevice(3);
                P2pStateMachine.this.resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
                if (P2pStateMachine.this.mGroup != null) {
                    P2pStateMachine.this.logd("[wfd sink/source] [crossmount]  {1} isGroupOwner: " + P2pStateMachine.this.mGroup.isGroupOwner() + " {2} getClientAmount: " + P2pStateMachine.this.mGroup.getClientAmount() + " {3} isGroupRemoved(): " + P2pStateMachine.this.isGroupRemoved() + " {4} mCrossmountIEAdded: " + WifiP2pServiceImpl.this.mCrossmountIEAdded);
                }
                if (P2pStateMachine.this.isWfdSinkConnected()) {
                    P2pStateMachine.this.logd("[wfd sink/source] don't bother wfd framework, case 1");
                } else if (P2pStateMachine.this.isWfdSourceConnected()) {
                    P2pStateMachine.this.logd("[wfd sink/source] don't bother wfd framework, case 2");
                } else if (P2pStateMachine.this.isCrossMountGOwithMultiGC()) {
                    P2pStateMachine.this.logd("[crossmount] don't bother crossmount framework, case 3");
                } else {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast(WifiP2pServiceImpl.this.mGroupRemoveReason);
                }
            }
        }

        class GroupCreatingState extends State {
            GroupCreatingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT, WifiP2pServiceImpl.mGroupCreatingTimeoutIndex = WifiP2pServiceImpl.mGroupCreatingTimeoutIndex + 1, 0), 120000);
                P2pStateMachine.this.sendP2pTxBroadcast(true);
                WifiP2pServiceImpl.this.mP2pOperFreq = -1;
                WifiP2pServiceImpl.this.stopReconnectAndScan();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, 2);
                        return true;
                    case 139268:
                        P2pStateMachine.this.logd("defer STOP_DISCOVERY@GroupCreatingState");
                        P2pStateMachine.this.deferMessage(message);
                        return true;
                    case 139274:
                        boolean success = false;
                        if (P2pStateMachine.this.mWifiNative.p2pCancelConnect()) {
                            success = true;
                        } else if (P2pStateMachine.this.mWifiNative.p2pGroupRemove(WifiP2pServiceImpl.this.mInterface)) {
                            success = true;
                        }
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        if (success) {
                            P2pStateMachine.this.replyToMessage(message, 139276);
                            return true;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139275);
                        return true;
                    case 139360:
                        P2pStateMachine.this.logd("defer STOP_P2P_FIND_ONLY@GroupCreatingState");
                        P2pStateMachine.this.deferMessage(message);
                        return true;
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                        if (WifiP2pServiceImpl.mGroupCreatingTimeoutIndex != message.arg1) {
                            return true;
                        }
                        P2pStateMachine.this.logd("Group negotiation timed out");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        return true;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        P2pStateMachine.this.logd("defer BLOCK_DISCOVERY@GroupCreatingState");
                        P2pStateMachine.this.deferMessage(message);
                        return true;
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                        WifiP2pDevice peerDevice = message.obj;
                        if (WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(peerDevice.deviceAddress)) {
                            return true;
                        }
                        if (P2pStateMachine.this.mSavedPeerConfig != null && P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(peerDevice.deviceAddress)) {
                            peerDevice.status = 1;
                        }
                        P2pStateMachine.this.mPeers.update(peerDevice);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        return true;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        WifiP2pDevice device = message.obj;
                        if (P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                            P2pStateMachine.this.logd("Add device to lost list " + device);
                            P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                            return true;
                        }
                        P2pStateMachine.this.logd("mSavedPeerConfig " + P2pStateMachine.this.mSavedPeerConfig.deviceAddress + "device " + device.deviceAddress);
                        return false;
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                        WifiP2pServiceImpl.this.mAutonomousGroup = false;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        return true;
                    case WifiMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                        P2pStateMachine.this.logd("defer P2P_FIND_STOPPED_EVENT@GroupCreatingState");
                        P2pStateMachine.this.deferMessage(message);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class GroupNegotiationState extends State {
            GroupNegotiationState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                P2pStatus status;
                switch (message.what) {
                    case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                        Toast.makeText(WifiP2pServiceImpl.this.mContext, 134545543, 0).show();
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.logd(getName() + " go success");
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                        if (message.arg1 != 0) {
                            WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                        }
                        status = message.obj;
                        P2pStateMachine.this.loge("go negotiation failed, status = " + status + "\tmP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        if (status == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                        if (message.arg1 != 0) {
                            WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                        }
                        status = (P2pStatus) message.obj;
                        P2pStateMachine.this.loge("group formation failed, status = " + status + "\tmP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        if (status == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.logd(getName() + " group started");
                        if (P2pStateMachine.this.mGroup.getNetworkId() == -2) {
                            P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.NO_RELOAD.booleanValue());
                            P2pStateMachine.this.mGroup.setNetworkId(P2pStateMachine.this.mGroups.getNetworkId(P2pStateMachine.this.mGroup.getOwner().deviceAddress, P2pStateMachine.this.mGroup.getNetworkName()));
                            if (WifiP2pServiceImpl.this.mMiracastMode == 1 && !WifiP2pServiceImpl.this.WFD_DONGLE_USE_P2P_INVITE) {
                                WifiP2pServiceImpl.this.mWfdSourceAddr = P2pStateMachine.this.mGroup.getOwner().deviceAddress;
                                P2pStateMachine.this.logd("wfd source case: mWfdSourceAddr = " + WifiP2pServiceImpl.this.mWfdSourceAddr);
                            }
                        }
                        if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                            if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                            }
                            P2pStateMachine.this.startDhcpServer(P2pStateMachine.this.mGroup.getInterface());
                        } else {
                            if (WifiP2pServiceImpl.this.mGcIgnoresDhcpReq) {
                                P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.SERVER_ADDRESS));
                                String gcIp = WifiP2pServiceImpl.STATIC_CLIENT_ADDRESS;
                                String intf = P2pStateMachine.this.mGroup.getInterface();
                                try {
                                    InterfaceConfiguration ifcg = WifiP2pServiceImpl.this.mNwService.getInterfaceConfig(intf);
                                    ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(gcIp), 24));
                                    ifcg.setInterfaceUp();
                                    WifiP2pServiceImpl.this.mNwService.setInterfaceConfig(intf, ifcg);
                                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                                    staticIpConfiguration.ipAddress = new LinkAddress(NetworkUtils.numericToInetAddress(gcIp), 24);
                                    WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(intf, staticIpConfiguration.getRoutes(intf));
                                } catch (RemoteException re) {
                                    P2pStateMachine.this.loge("Error! Configuring static IP to " + intf + ", :" + re);
                                }
                            } else {
                                P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                WifiP2pServiceImpl.this.startIpManager(P2pStateMachine.this.mGroup.getInterface());
                            }
                            P2pStateMachine.this.setP2pPowerSaveMtk(P2pStateMachine.this.mGroup.getInterface(), 2);
                            WifiP2pDevice groupOwner = P2pStateMachine.this.mGroup.getOwner();
                            WifiP2pDevice peer = P2pStateMachine.this.mPeers.get(groupOwner.deviceAddress);
                            if (peer != null) {
                                groupOwner.updateSupplicantDetails(peer);
                                P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                            } else {
                                P2pStateMachine.this.logw("Unknown group owner " + groupOwner);
                            }
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                        break;
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        P2pStateMachine.this.logd(getName() + " go failure");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                        status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine.this.loge("Invitation result " + status);
                            if (status != P2pStatus.UNKNOWN_P2P_GROUP) {
                                if (status != P2pStatus.INFORMATION_IS_CURRENTLY_UNAVAILABLE) {
                                    if (status != P2pStatus.NO_COMMON_CHANNEL) {
                                        P2pStateMachine.this.handleGroupCreationFailure();
                                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                                        break;
                                    }
                                    if (message.arg1 != 0) {
                                        WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                                    }
                                    P2pStateMachine.this.logd("Invitation mP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                                    break;
                                }
                                if (WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable) {
                                    P2pStateMachine.this.logd(getName() + " mDelayReconnectForInfoUnavailable:" + WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable);
                                    if ((P2pStateMachine.this.fetchCurrentDeviceDetails(P2pStateMachine.this.mSavedPeerConfig).groupCapability & 32) == 0) {
                                        P2pStateMachine.this.logd(getName() + "Persistent Reconnect=0, " + "wait for peer re-invite or " + "reconnect peer 120s later");
                                        P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE, new WifiP2pConfig(P2pStateMachine.this.mSavedPeerConfig)), 120000);
                                        if (P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S)) {
                                            P2pStateMachine.this.logd(getName() + "Sart p2pFind for waiting peer invitation");
                                            P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                                        }
                                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                                        break;
                                    }
                                    P2pStateMachine.this.logd(getName() + "Persistent Reconnect=1, " + "connect to peer directly");
                                }
                                P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                break;
                            }
                            int netId = P2pStateMachine.this.mSavedPeerConfig.netId;
                            if (netId >= 0) {
                                P2pStateMachine.this.logd("Remove unknown client from the list");
                                P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, true);
                            }
                            P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class InactiveState extends State {
            InactiveState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                WifiP2pServiceImpl.this.setNfcTriggered(false);
                WifiP2pServiceImpl.this.resumeReconnectAndScan();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                WifiP2pConfig config;
                switch (message.what) {
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                            break;
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        P2pStateMachine.this.clearSupplicantServiceRequest();
                        P2pStateMachine.this.replyToMessage(message, 139270);
                        break;
                    case 139271:
                        config = message.obj;
                        WifiP2pServiceImpl.this.mP2pConnectFreq = message.arg1;
                        if (WifiP2pServiceImpl.this.mP2pConnectFreq != 0) {
                            WifiP2pServiceImpl.this.mFindTimes = 0;
                            if (config != null) {
                                WifiP2pServiceImpl.this.mIntentToConnectPeer = config.deviceAddress;
                            }
                            P2pStateMachine.this.logd(getName() + " sending connect on freq=" + WifiP2pServiceImpl.this.mP2pConnectFreq);
                        } else {
                            P2pStateMachine.this.logd(getName() + " sending connect");
                        }
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            WifiP2pServiceImpl.this.mConnectToPeer = true;
                            if (WifiP2pServiceImpl.this.mMiracastMode == 1 && !WifiP2pServiceImpl.this.WFD_DONGLE_USE_P2P_INVITE) {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mProvisionDiscoveryState);
                            } else if (P2pStateMachine.this.reinvokePersistentGroup(config)) {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            } else {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mProvisionDiscoveryState);
                            }
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping connect request " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139277:
                        WifiP2pServiceImpl.this.mAutonomousGroup = true;
                        int netId = message.arg1;
                        boolean ret = false;
                        if (netId == -2) {
                            netId = P2pStateMachine.this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
                            if (netId != -1) {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(netId);
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(true);
                            }
                        } else if (netId <= -1 || !P2pStateMachine.this.mGroups.contains(netId)) {
                            ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(false);
                        } else if (WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(P2pStateMachine.this.mGroups.getOwnerAddr(netId))) {
                            ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(netId);
                        }
                        if (!ret) {
                            P2pStateMachine.this.replyToMessage(message, 139278, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139279);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                        break;
                    case 139335:
                        Bundle p2pChannels = message.obj;
                        int lc = p2pChannels.getInt("lc", 0);
                        int oc = p2pChannels.getInt("oc", 0);
                        P2pStateMachine.this.logd(getName() + " set listen and operating channel");
                        if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                            P2pStateMachine.this.replyToMessage(message, 139336);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139337);
                        break;
                    case 139342:
                        String handoverSelect = null;
                        if (message.obj != null) {
                            handoverSelect = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverSelect != null && P2pStateMachine.this.mWifiNative.initiatorReportNfcHandover(handoverSelect)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                        break;
                    case 139343:
                        String handoverRequest = null;
                        if (message.obj != null) {
                            handoverRequest = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverRequest != null && P2pStateMachine.this.mWifiNative.responderReportNfcHandover(handoverRequest)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                        break;
                    case WifiP2pServiceImpl.M_P2P_DEVICE_FOUND_INVITATION /*143380*/:
                        WifiP2pDevice owner02 = P2pStateMachine.this.mPeers.get(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                        if (owner02 != null) {
                            if (owner02.wpsPbcSupported()) {
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                            } else if (owner02.wpsKeypadSupported()) {
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                            } else if (owner02.wpsDisplaySupported()) {
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                            }
                        }
                        P2pStateMachine.this.updateCrossMountInfo(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                        WifiP2pServiceImpl.this.mAutonomousGroup = false;
                        WifiP2pServiceImpl.this.mJoinExistingGroup = true;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingInviteRequestState);
                        break;
                    case WifiP2pServiceImpl.M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE /*143381*/:
                        if (WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable) {
                            P2pStateMachine.this.logd(getName() + " mDelayReconnectForInfoUnavailable:" + WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable + " M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE:" + ((WifiP2pConfig) message.obj));
                            config = (WifiP2pConfig) message.obj;
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            if (!P2pStateMachine.this.mPeers.containsPeer(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) {
                                P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE);
                                break;
                            }
                            P2pStateMachine.this.p2pConnectWithPinDisplay(config);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT /*147479*/:
                        config = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping GO neg request " + config);
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.logd(getName() + " group started");
                        if (P2pStateMachine.this.mGroup.getNetworkId() != -2) {
                            P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        }
                        WifiP2pServiceImpl.this.mAutonomousGroup = false;
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case WifiMonitor.P2P_INVITATION_RECEIVED_EVENT /*147487*/:
                        WifiP2pGroup group = message.obj;
                        if (group.getOwner() == null) {
                            int id = group.getNetworkId();
                            if (id >= 0) {
                                String addr = P2pStateMachine.this.mGroups.getOwnerAddr(id);
                                if (addr == null) {
                                    P2pStateMachine.this.loge("Ignored invitation from null owner");
                                    break;
                                }
                                group.setOwner(new WifiP2pDevice(addr));
                                WifiP2pDevice owner = group.getOwner();
                            } else {
                                P2pStateMachine.this.loge("Ignored invitation from null owner");
                                break;
                            }
                        }
                        config = new WifiP2pConfig();
                        config.deviceAddress = group.getOwner().deviceAddress;
                        if (WifiP2pServiceImpl.this.mCrossmountIEAdded) {
                            P2pStateMachine.this.mWifiNative.doCustomSupplicantCommand("P2P_FIND 120 type=progressive dev_id=" + config.deviceAddress);
                            WifiP2pServiceImpl.this.mUpdatePeerForInvited = true;
                        } else if (P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.loge("Dropping invitation request " + config);
                            break;
                        } else {
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.M_P2P_DEVICE_FOUND_INVITATION);
                        }
                        P2pStateMachine.this.mSavedPeerConfig = config;
                        break;
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
                        P2pStateMachine.this.updateCrossMountInfo(provDisc.device.deviceAddress);
                        if (message.what == 147492) {
                            P2pStateMachine.this.logd("Show PIN passively");
                            config = new WifiP2pConfig();
                            config.deviceAddress = provDisc.device.deviceAddress;
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                            if (!P2pStateMachine.this.isAppHandledConnection()) {
                                if (P2pStateMachine.this.mPeers.get(config.deviceAddress) != null) {
                                    P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.notifyInvitationSent(provDisc.pin, P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                }
                                P2pStateMachine.this.loge("peer device is not in our scan result, drop this pd. " + config.deviceAddress);
                                break;
                            }
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                if (WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable) {
                    P2pStateMachine.this.logd(getName() + " mDelayReconnectForInfoUnavailable:" + WifiP2pServiceImpl.this.mDelayReconnectForInfoUnavailable + ", remove M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE");
                    P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.M_P2P_CONN_FOR_INVITE_RES_INFO_UNAVAILABLE);
                }
            }
        }

        class OngoingGroupRemovalState extends State {
            OngoingGroupRemovalState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139282);
                        break;
                    case 139357:
                        P2pStateMachine.this.replyToMessage(message, 139359);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class P2pDisabledState extends State {
            P2pDisabledState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                        try {
                            WifiP2pServiceImpl.this.mNwService.setInterfaceUp(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        } catch (RemoteException re) {
                            P2pStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            P2pStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        P2pStateMachine.this.mWifiMonitor.startMonitoring(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pEnablingState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class P2pDisablingState extends State {
            P2pDisablingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT, WifiP2pServiceImpl.mDisableP2pTimeoutIndex = WifiP2pServiceImpl.mDisableP2pTimeoutIndex + 1, 0), 5000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                        if (WifiP2pServiceImpl.mDisableP2pTimeoutIndex == message.arg1) {
                            P2pStateMachine.this.loge("P2p disable timed out");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        }
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.logd("p2p socket connection lost");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
            }
        }

        class P2pEnabledState extends State {
            P2pEnabledState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendP2pStateChangedBroadcast(true);
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(true);
                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                P2pStateMachine.this.initializeP2pSettings();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                        break;
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        if (P2pStateMachine.this.mPeers.clear()) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                        }
                        if (P2pStateMachine.this.mGroups.clear()) {
                            P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
                        }
                        P2pStateMachine.this.mWifiMonitor.stopMonitoring(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        P2pStateMachine.this.mWifiNative.stopDriver();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisablingState);
                        break;
                    case 139265:
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked && !WifiP2pServiceImpl.this.isNfcTriggered) {
                            P2pStateMachine.this.logd("DiscoveryBlocked");
                            P2pStateMachine.this.replyToMessage(message, 139266, 2);
                            break;
                        }
                        boolean retP2pFind;
                        P2pStateMachine.this.clearSupplicantServiceRequest();
                        boolean specifyFreq = false;
                        int timeout = message.arg1;
                        if (P2pStateMachine.this.isWfdSinkEnabled()) {
                            P2pStateMachine.this.p2pConfigWfdSink();
                            retP2pFind = P2pStateMachine.this.mWifiNative.p2pFind();
                        } else if (timeout == 123) {
                            retP2pFind = P2pStateMachine.this.mWifiNative.p2pFind(123);
                        } else {
                            P2pStateMachine.this.logd(getName() + ",DISCOVER_PEERS on freq=" + message.arg1);
                            specifyFreq = false;
                            WifiP2pServiceImpl.this.mFoundTargetDevice = false;
                            if (message.arg1 == 0) {
                                retP2pFind = P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S);
                            } else if (message.arg1 == -1) {
                                specifyFreq = true;
                                retP2pFind = P2pStateMachine.this.mWifiNative.p2pFind(30, 5745);
                            } else {
                                specifyFreq = true;
                                retP2pFind = P2pStateMachine.this.mWifiNative.p2pFind(30, message.arg1);
                            }
                        }
                        if (!retP2pFind) {
                            P2pStateMachine.this.replyToMessage(message, 139266, 0);
                            break;
                        }
                        if (specifyFreq && !WifiP2pServiceImpl.this.mIntentToConnectPeer.isEmpty()) {
                            WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                            wifiP2pServiceImpl.mFindTimes = wifiP2pServiceImpl.mFindTimes + 1;
                            P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT, message.arg1, 0), 3000);
                            P2pStateMachine.this.logd("mIntentToConnectPeer=" + WifiP2pServiceImpl.this.mIntentToConnectPeer + " mFindTimes=" + WifiP2pServiceImpl.this.mFindTimes);
                        }
                        P2pStateMachine.this.replyToMessage(message, 139267);
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                        break;
                        break;
                    case 139268:
                        if (P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139270);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                        }
                        if (P2pStateMachine.this.isWfdSinkEnabled()) {
                            P2pStateMachine.this.p2pUnconfigWfdSink();
                            break;
                        }
                        break;
                    case 139292:
                        P2pStateMachine.this.logd(getName() + " add service");
                        if (!P2pStateMachine.this.addLocalService(message.replyTo, message.obj)) {
                            P2pStateMachine.this.replyToMessage(message, 139293);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139294);
                        break;
                    case 139295:
                        P2pStateMachine.this.logd(getName() + " remove service");
                        P2pStateMachine.this.removeLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139297);
                        break;
                    case 139298:
                        P2pStateMachine.this.logd(getName() + " clear service");
                        P2pStateMachine.this.clearLocalServices(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139300);
                        break;
                    case 139301:
                        P2pStateMachine.this.logd(getName() + " add service request");
                        if (!P2pStateMachine.this.addServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj)) {
                            P2pStateMachine.this.replyToMessage(message, 139302);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139303);
                        break;
                    case 139304:
                        P2pStateMachine.this.logd(getName() + " remove service request");
                        P2pStateMachine.this.removeServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139306);
                        break;
                    case 139307:
                        P2pStateMachine.this.logd(getName() + " clear service request");
                        P2pStateMachine.this.clearServiceRequests(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139309);
                        break;
                    case 139310:
                        if (!WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            P2pStateMachine.this.logd(getName() + " discover services");
                            if (P2pStateMachine.this.updateSupplicantServiceRequest()) {
                                if (!P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S)) {
                                    P2pStateMachine.this.replyToMessage(message, 139311, 0);
                                    break;
                                }
                                P2pStateMachine.this.replyToMessage(message, 139312);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139311, 3);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139311, 2);
                        break;
                    case 139315:
                        WifiP2pDevice d = message.obj;
                        if (d != null && P2pStateMachine.this.setAndPersistDeviceName(d.deviceName)) {
                            P2pStateMachine.this.logd("set device name " + d.deviceName);
                            P2pStateMachine.this.replyToMessage(message, 139317);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139316, 0);
                        break;
                        break;
                    case 139318:
                        P2pStateMachine.this.logd(getName() + " delete persistent group");
                        P2pStateMachine.this.mGroups.remove(message.arg1);
                        P2pStateMachine.this.replyToMessage(message, 139320);
                        break;
                    case 139323:
                        WifiP2pWfdInfo d2 = message.obj;
                        if (d2 == null || !P2pStateMachine.this.setWfdInfo(d2)) {
                            P2pStateMachine.this.replyToMessage(message, 139324, 0);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139325);
                        }
                        if (WifiP2pServiceImpl.this.mThisDevice.wfdInfo != null && WifiP2pServiceImpl.this.mThisDevice.wfdInfo.mCrossmountLoaned) {
                            WifiP2pServiceImpl.this.mThisDevice.wfdInfo = null;
                            P2pStateMachine.this.logd("[crossmount] reset wfd info in wifi p2p framework");
                            break;
                        }
                        break;
                    case 139329:
                        P2pStateMachine.this.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139331);
                        break;
                    case 139332:
                        P2pStateMachine.this.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        Bundle p2pChannels = message.obj;
                        int lc = p2pChannels.getInt("lc", 0);
                        int oc = p2pChannels.getInt("oc", 0);
                        P2pStateMachine.this.logd(getName() + " set listen and operating channel");
                        if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                            P2pStateMachine.this.replyToMessage(message, 139336);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139337);
                        break;
                    case 139339:
                        Bundle requestBundle = new Bundle();
                        requestBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverRequest());
                        P2pStateMachine.this.replyToMessage(message, 139341, (Object) requestBundle);
                        break;
                    case 139340:
                        Bundle selectBundle = new Bundle();
                        selectBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverSelect());
                        P2pStateMachine.this.replyToMessage(message, 139341, (Object) selectBundle);
                        break;
                    case 139349:
                        WifiP2pLinkInfo info = message.obj;
                        info.linkInfo = P2pStateMachine.this.p2pLinkStatics(info.interfaceAddress);
                        P2pStateMachine.this.logd("Wifi P2p link info is " + info.toString());
                        P2pStateMachine.this.replyToMessage(message, 139350, (Object) new WifiP2pLinkInfo(info));
                        break;
                    case 139351:
                        P2pStateMachine.this.p2pAutoChannel(message.arg1);
                        P2pStateMachine.this.replyToMessage(message, 139353);
                        break;
                    case 139360:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139270);
                        break;
                    case 139361:
                        P2pStateMachine.this.logd(getName() + " ADD_PERSISTENT_GROUP");
                        HashMap<String, String> hVariables = (HashMap) message.obj.getSerializable("variables");
                        if (hVariables == null) {
                            P2pStateMachine.this.replyToMessage(message, 139362, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139364, (Object) new WifiP2pGroup(P2pStateMachine.this.addPersistentGroup(hVariables)));
                        break;
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                        if (message.arg2 != 0) {
                            P2pStateMachine.this.mWifiNative.setMiracastMode(message.arg1, message.arg2);
                        } else {
                            P2pStateMachine.this.mWifiNative.setMiracastMode(message.arg1);
                        }
                        WifiP2pServiceImpl.this.mMiracastMode = message.arg1;
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        boolean blocked = message.arg1 == 1;
                        P2pStateMachine.this.logd("blocked:" + blocked + ", mDiscoveryBlocked:" + WifiP2pServiceImpl.this.mDiscoveryBlocked);
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked != blocked) {
                            WifiP2pServiceImpl.this.mDiscoveryBlocked = blocked;
                            if (blocked && WifiP2pServiceImpl.this.mDiscoveryStarted) {
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = true;
                            }
                            if (!blocked && WifiP2pServiceImpl.this.mDiscoveryPostponed) {
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                                P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S);
                            }
                        }
                        if (blocked) {
                            try {
                                message.obj.sendMessage(message.arg2);
                                break;
                            } catch (Exception e) {
                                P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                break;
                            }
                        }
                        break;
                    case WifiP2pServiceImpl.AUTO_ACCEPT_INVITATION_TIME_OUT /*143376*/:
                        if (WifiP2pServiceImpl.this.mFastConTriggeredNum == message.arg1) {
                            P2pStateMachine.this.logd("FastConTrigger: " + message.arg1 + "/" + WifiP2pServiceImpl.this.mFastConTriggeredNum);
                            WifiP2pServiceImpl.this.setNfcTriggered(false);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT /*143377*/:
                        P2pStateMachine.this.logd("P2P_FIND_SPECIAL_FREQ_TIME_OUT:" + WifiP2pServiceImpl.this.mFoundTargetDevice + ", mFindTimes=" + WifiP2pServiceImpl.this.mFindTimes);
                        if (!WifiP2pServiceImpl.this.mFoundTargetDevice && WifiP2pServiceImpl.this.mFindTimes < 5) {
                            P2pStateMachine.this.sendMessage(139265, message.arg1);
                            break;
                        }
                        WifiP2pServiceImpl.this.mFindTimes = 0;
                        WifiP2pServiceImpl.this.mFoundTargetDevice = false;
                        break;
                        break;
                    case WifiP2pServiceImpl.SET_BEAM_MODE /*143382*/:
                        P2pStateMachine.this.logd(getName() + " SET_BEAM_MODE");
                        P2pStateMachine.this.setBeamMode(message.arg1);
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.loge("Unexpected loss of p2p socket connection");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                        WifiP2pDevice device = message.obj;
                        if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                            P2pStateMachine.this.logd("Found a Device=" + device.deviceAddress + ", mIntentToConnectPeer=" + WifiP2pServiceImpl.this.mIntentToConnectPeer);
                            if (!WifiP2pServiceImpl.this.mIntentToConnectPeer.isEmpty() && WifiP2pServiceImpl.this.mIntentToConnectPeer.equalsIgnoreCase(device.deviceAddress)) {
                                P2pStateMachine.this.logd("mFoundTargetDevice true!");
                                WifiP2pServiceImpl.this.mFoundTargetDevice = true;
                                WifiP2pServiceImpl.this.mIntentToConnectPeer = "";
                                P2pStateMachine.this.removeMessages(WifiP2pServiceImpl.P2P_FIND_SPECIAL_FREQ_TIME_OUT);
                            }
                            P2pStateMachine.this.mPeers.updateSupplicantDetails(device);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            if (WifiP2pServiceImpl.this.mUpdatePeerForInvited && P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                                WifiP2pServiceImpl.this.mUpdatePeerForInvited = false;
                                P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.M_P2P_DEVICE_FOUND_INVITATION);
                                break;
                            }
                        }
                        break;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        if (P2pStateMachine.this.mPeers.remove(((WifiP2pDevice) message.obj).deviceAddress) != null) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                        break;
                    case WifiMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                        P2pStateMachine.this.logd(getName() + " receive service response");
                        for (WifiP2pServiceResponse resp : message.obj) {
                            resp.setSrcDevice(P2pStateMachine.this.mPeers.get(resp.getSrcDevice().deviceAddress));
                            P2pStateMachine.this.sendServiceResponse(resp);
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                P2pStateMachine.this.sendP2pStateChangedBroadcast(false);
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(false);
            }
        }

        class P2pEnablingState extends State {
            P2pEnablingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                        P2pStateMachine.this.logd("P2p socket connection successful");
                        P2pStateMachine.this.mWifiNative.startDriver();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.loge("P2p socket connection failed");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class P2pNotSupportedState extends State {
            P2pNotSupportedState() {
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, 1);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage(message, 139269, 1);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage(message, 139272, 1);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage(message, 139275, 1);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage(message, 139278, 1);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139281, 1);
                        break;
                    case 139292:
                        P2pStateMachine.this.replyToMessage(message, 139293, 1);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage(message, 139296, 1);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage(message, 139299, 1);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage(message, 139302, 1);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage(message, 139305, 1);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage(message, 139308, 1);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage(message, 139311, 1);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage(message, 139316, 1);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage(message, 139318, 1);
                        break;
                    case 139323:
                        P2pStateMachine.this.replyToMessage(message, 139324, 1);
                        break;
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, 1);
                        break;
                    case 139329:
                        P2pStateMachine.this.replyToMessage(message, 139330, 1);
                        break;
                    case 139332:
                        P2pStateMachine.this.replyToMessage(message, 139333, 1);
                        break;
                    case 139354:
                    case 139355:
                        break;
                    case 139357:
                        P2pStateMachine.this.replyToMessage(message, 139358, 1);
                        break;
                    case 139361:
                        P2pStateMachine.this.replyToMessage(message, 139361, 1);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class ProvisionDiscoveryState extends State {
            ProvisionDiscoveryState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                if (P2pStateMachine.this.mSavedPeerConfig.wps.setup != 0) {
                    P2pStateMachine.this.logd("Force wps=" + P2pStateMachine.this.mSavedPeerConfig.wps.setup + " to pbc ");
                    P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                }
                P2pStateMachine.this.mWifiNative.p2pProvisionDiscovery(P2pStateMachine.this.mSavedPeerConfig);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                WifiP2pProvDiscEvent provDisc;
                switch (message.what) {
                    case 139274:
                        boolean success = P2pStateMachine.this.mWifiNative.p2pGroupRemove(WifiP2pServiceImpl.this.mInterface);
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        if (!success) {
                            P2pStateMachine.this.replyToMessage(message, 139275);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139276);
                        break;
                    case WifiMonitor.P2P_PROV_DISC_PBC_RSP_EVENT /*147490*/:
                        provDisc = message.obj;
                        if (provDisc.device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) {
                            P2pStateMachine.this.updateCrossMountInfo(provDisc.device.deviceAddress);
                            if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                                P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                break;
                            }
                        }
                        break;
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                        provDisc = (WifiP2pProvDiscEvent) message.obj;
                        if (provDisc.device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) {
                            P2pStateMachine.this.updateCrossMountInfo(provDisc.device.deviceAddress);
                            if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 2) {
                                P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                if (!TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                                    P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                }
                                WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                                break;
                            }
                        }
                        break;
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        provDisc = (WifiP2pProvDiscEvent) message.obj;
                        WifiP2pDevice device = provDisc.device;
                        if (device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) {
                            P2pStateMachine.this.updateCrossMountInfo(provDisc.device.deviceAddress);
                            if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 1) {
                                P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                if (!WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                                    P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                                    P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.notifyInvitationSent(provDisc.pin, device.deviceAddress);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                }
                                P2pStateMachine.this.logd("[crossmount] PD rsp: SHOW_PIN, move process to UserAuthorizingNegotiationRequestState");
                                WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                                break;
                            }
                        }
                        break;
                    case WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        P2pStateMachine.this.loge("provision discovery failed");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class UserAuthorizingInviteRequestState extends State {
            UserAuthorizingInviteRequestState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                if (P2pStateMachine.this.isWfdSinkEnabled()) {
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                } else if (WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                    P2pStateMachine.this.sendP2pCrossmountIntentionBroadcast();
                } else {
                    P2pStateMachine.this.notifyInvitationReceived();
                }
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139354:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                        if (message.what == 139354 && WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                            P2pStateMachine.this.p2pUserAuthPreprocess(message);
                            P2pStateMachine.this.p2pOverwriteWpsPin("[crossmount] USER_ACCEPT@UserAuthorizingInviteRequestState", message.obj);
                        }
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        if (!P2pStateMachine.this.reinvokePersistentGroup(P2pStateMachine.this.mSavedPeerConfig)) {
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                        }
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case 139355:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                        P2pStateMachine.this.logd("User rejected invitation " + P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        class UserAuthorizingJoinState extends State {
            UserAuthorizingJoinState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                if (P2pStateMachine.this.isWfdSinkEnabled()) {
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                } else if (WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                    P2pStateMachine.this.sendP2pCrossmountIntentionBroadcast();
                } else {
                    P2pStateMachine.this.notifyInvitationReceived();
                }
                WifiP2pServiceImpl.this.stopReconnectAndScan();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139354:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                        if (message.what == 139354) {
                            P2pStateMachine.this.p2pOverwriteWpsPin("[crossmount] USER_ACCEPT@UserAuthorizingJoinState", message.obj);
                        }
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        if (P2pStateMachine.this.mSavedPeerConfig.wps.setup != 0) {
                            P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), P2pStateMachine.this.mSavedPeerConfig.wps.pin);
                            break;
                        }
                        P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                        break;
                    case 139355:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                        P2pStateMachine.this.logd("User rejected incoming request");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                        break;
                    case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                        P2pStateMachine.this.logd("incoming request connect failed!");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                        break;
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        break;
                    case WifiMonitor.AP_STA_CONNECTED_EVENT /*147498*/:
                        P2pStateMachine.this.logd("incoming request is connected!");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        class UserAuthorizingNegotiationRequestState extends State {
            UserAuthorizingNegotiationRequestState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                if (P2pStateMachine.this.isWfdSinkEnabled()) {
                    P2pStateMachine.this.sendP2pGOandGCRequestConnectBroadcast();
                } else if (WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                    P2pStateMachine.this.sendP2pCrossmountIntentionBroadcast();
                } else {
                    P2pStateMachine.this.notifyInvitationReceived();
                }
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logStateAndMessage(message, this);
                switch (message.what) {
                    case 139354:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                        if (message.what == 139354 && P2pStateMachine.this.isAppHandledConnection()) {
                            P2pStateMachine.this.p2pUserAuthPreprocess(message);
                            P2pStateMachine.this.p2pOverwriteWpsPin("[crossmount] USER_ACCEPT@UserAuthorizingNegotiationRequestState", message.obj);
                        }
                        P2pStateMachine.this.logd("User accept negotiation: mSavedPeerConfig = " + P2pStateMachine.this.mSavedPeerConfig);
                        if (!WifiP2pServiceImpl.this.mNegoChannelConflict) {
                            P2pStateMachine.this.logd("isWfdSinkEnabled()=" + P2pStateMachine.this.isWfdSinkEnabled());
                            if (P2pStateMachine.this.isWfdSinkEnabled()) {
                                WifiInfo wifiInfo = P2pStateMachine.this.getWifiConnectionInfo();
                                if (wifiInfo != null) {
                                    P2pStateMachine.this.logd("wifiInfo=" + wifiInfo);
                                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                                        P2pStateMachine.this.logd("wifiInfo.getSupplicantState() == SupplicantState.COMPLETED");
                                        P2pStateMachine.this.logd("wifiInfo.getFrequency()=" + wifiInfo.getFrequency());
                                        P2pStateMachine.this.mSavedPeerConfig.setPreferOperFreq(wifiInfo.getFrequency());
                                    }
                                }
                            }
                            P2pStateMachine.this.p2pUpdateScanList(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                        WifiP2pServiceImpl.this.mNegoChannelConflict = false;
                        P2pStateMachine.this.logd("PEER_CONNECTION_USER_ACCEPT_FROM_OUTER,switch to FrequencyConflictState");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                        break;
                    case 139355:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                        P2pStateMachine.this.logd("User rejected negotiation " + P2pStateMachine.this.mSavedPeerConfig);
                        if (P2pStateMachine.this.mSavedPeerConfig != null) {
                            WifiP2pDevice peerDevice02 = P2pStateMachine.this.mPeers.get(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                            if (peerDevice02 != null && peerDevice02.status == 1) {
                                P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 3);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                            }
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                        if (message.arg1 != 0) {
                            WifiP2pServiceImpl.this.mP2pOperFreq = message.arg1;
                        }
                        P2pStatus status = message.obj;
                        P2pStateMachine.this.loge("go negotiation failed@UserAuthorizingNegotiationRequestState, status = " + status + "\tmP2pOperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
                        if (status != P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.loge("other kinds of negotiation errors");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        }
                        WifiP2pServiceImpl.this.mNegoChannelConflict = true;
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
            }
        }

        private void logStateAndMessage(Message message, State state) {
            StringBuilder b = new StringBuilder();
            if (message != null) {
                b.append("{ what=");
                b.append(message.what);
                if (message.arg1 != 0) {
                    b.append(" arg1=");
                    b.append(message.arg1);
                }
                if (message.arg2 != 0) {
                    b.append(" arg2=");
                    b.append(message.arg2);
                }
                if (message.obj != null) {
                    b.append(" obj=");
                    b.append(message.obj.getClass().getSimpleName());
                }
                b.append(" }");
            }
            logd(" " + state.getClass().getSimpleName() + " " + b.toString());
        }

        P2pStateMachine(String name, Looper looper, boolean p2pSupported) {
            super(name, looper);
            addState(this.mDefaultState);
            addState(this.mP2pNotSupportedState, this.mDefaultState);
            addState(this.mP2pDisablingState, this.mDefaultState);
            addState(this.mP2pDisabledState, this.mDefaultState);
            addState(this.mP2pEnablingState, this.mDefaultState);
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
            String interfaceName = this.mWifiNative.getInterfaceName();
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AP_STA_CONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AP_STA_DISCONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_DEVICE_FOUND_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_DEVICE_LOST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_FIND_STOPPED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_REMOVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_STARTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_INVITATION_RECEIVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_INVITATION_RESULT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_PBC_RSP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_SERV_DISC_RESP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUP_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUP_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            super.dump(fd, pw, args);
            pw.println("mWifiP2pInfo " + this.mWifiP2pInfo);
            pw.println("mGroup " + this.mGroup);
            pw.println("mSavedPeerConfig " + this.mSavedPeerConfig);
            pw.println();
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

        private void sendP2pDiscoveryChangedBroadcast(boolean started) {
            if (WifiP2pServiceImpl.this.mDiscoveryStarted != started) {
                int i;
                WifiP2pServiceImpl.this.mDiscoveryStarted = started;
                logd("discovery change broadcast " + started);
                Intent intent = new Intent("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
                intent.addFlags(67108864);
                String str = "discoveryState";
                if (started) {
                    i = 2;
                } else {
                    i = 1;
                }
                intent.putExtra(str, i);
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }
        }

        private void sendThisDeviceChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("wifiP2pDevice", new WifiP2pDevice(WifiP2pServiceImpl.this.mThisDevice));
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendPeersChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
            intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pConnectionChangedBroadcast() {
            logd("sending p2p connection changed broadcast, mGroup: " + this.mGroup);
            Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
            intent.putExtra("networkInfo", new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            intent.putExtra("p2pGroupInfo", new WifiP2pGroup(this.mGroup));
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
        }

        private void sendP2pPersistentGroupsChangedBroadcast() {
            logd("sending p2p persistent groups changed broadcast");
            Intent intent = new Intent("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void startDhcpServer(String intf) {
            try {
                InterfaceConfiguration ifcg = WifiP2pServiceImpl.this.mNwService.getInterfaceConfig(intf);
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.SERVER_ADDRESS), 24));
                ifcg.setInterfaceUp();
                WifiP2pServiceImpl.this.mNwService.setInterfaceConfig(intf, ifcg);
                String[] tetheringDhcpRanges = ((ConnectivityManager) WifiP2pServiceImpl.this.mContext.getSystemService("connectivity")).getTetheredDhcpRanges();
                if (WifiP2pServiceImpl.this.mNwService.isTetheringStarted()) {
                    logd("Stop existing tethering and restart it");
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
            try {
                WifiP2pServiceImpl.this.mNwService.untetherInterface(intf);
                for (String temp : WifiP2pServiceImpl.this.mNwService.listTetheredInterfaces()) {
                    logd("List all interfaces " + temp);
                    if (temp.compareTo(intf) != 0) {
                        logd("Found other tethering interfaces, so keep tethering alive");
                        return;
                    }
                }
                WifiP2pServiceImpl.this.mNwService.stopTethering();
                logd("Stopped Dhcp server");
            } catch (Exception e) {
                loge("Error stopping Dhcp server" + e);
            } finally {
                logd("Stopped Dhcp server");
            }
        }

        private void notifyP2pEnableFailure() {
            Resources r = Resources.getSystem();
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext, 201523207).setTitle(r.getString(17040384)).setPositiveButton(r.getString(17040385), null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            LayoutParams p = dialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            dialog.getWindow().setAttributes(p);
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
            TextView msg = (TextView) dialog.findViewById(16908299);
            if (msg != null) {
                msg.setGravity(17);
            } else {
                loge("textview is null");
            }
        }

        private void addRowToDialog(ViewGroup group, int stringId, String value) {
            Resources r = Resources.getSystem();
            View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367307, group, false);
            ((TextView) row.findViewById(16909396)).setText(r.getString(stringId));
            ((TextView) row.findViewById(16909197)).setText(value);
            group.addView(row);
        }

        private void notifyInvitationSent(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367306, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909393);
            addRowToDialog(group, 17040377, getDeviceName(peerAddress));
            addRowToDialog(group, 17040379, pin);
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext, 201523207).setTitle(r.getString(17040374)).setView(textEntryView).setPositiveButton(r.getString(17039370), null).create();
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void notifyInvitationReceived() {
            Resources r = Resources.getSystem();
            final WpsInfo wps = this.mSavedPeerConfig.wps;
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367306, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909393);
            addRowToDialog(group, 17040376, getDeviceName(this.mSavedPeerConfig.deviceAddress));
            final EditText pin = (EditText) textEntryView.findViewById(16909395);
            if (WifiP2pServiceImpl.this.isNfcTriggered) {
                if (wps.setup == 2) {
                    this.mSavedPeerConfig.wps.pin = pin.getText().toString();
                }
                logd(getName() + "NFC mode accept invitation " + this.mSavedPeerConfig);
                sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                switch (wps.setup) {
                    case 1:
                        logd("Shown pin section visible");
                        addRowToDialog(group, 17040379, wps.pin);
                        return;
                    case 2:
                        logd("Enter pin section visible");
                        textEntryView.findViewById(16909394).setVisibility(0);
                        return;
                    default:
                        return;
                }
            }
            Builder builder = new Builder(WifiP2pServiceImpl.this.mContext);
            if (wps.setup == 2) {
                builder.setTitle(r.getString(17040375));
                builder.setView(textEntryView);
            } else {
                Object[] objArr = new Object[1];
                objArr[0] = getDeviceName(this.mSavedPeerConfig.deviceAddress);
                builder.setTitle(r.getString(17040381, objArr));
            }
            AlertDialog dialog = builder.setPositiveButton(r.getString(17040372), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (wps.setup == 2) {
                        P2pStateMachine.this.mSavedPeerConfig.wps.pin = pin.getText().toString();
                    }
                    P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " accept invitation " + P2pStateMachine.this.mSavedPeerConfig);
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                }
            }).setNegativeButton(r.getString(17039360), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                    P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                }
            }).create();
            switch (wps.setup) {
                case 1:
                    logd("Shown pin section visible");
                    addRowToDialog(group, 17040379, wps.pin);
                    break;
                case 2:
                    logd("Enter pin section visible");
                    textEntryView.findViewById(16909394).setVisibility(0);
                    break;
            }
            if ((r.getConfiguration().uiMode & 5) == 5) {
                dialog.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode != 164) {
                            return false;
                        }
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                        dialog.dismiss();
                        return true;
                    }
                });
            }
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            LayoutParams p = dialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            dialog.getWindow().setAttributes(p);
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void updatePersistentNetworks(boolean reload) {
            String listStr = this.mWifiNative.listNetworks();
            if (listStr != null) {
                boolean isSaveRequired = false;
                String[] lines = listStr.split("\n");
                if (lines != null) {
                    if (reload) {
                        this.mGroups.clear();
                    }
                    for (int i = 1; i < lines.length; i++) {
                        String[] result = lines[i].split("\t");
                        if (result != null && result.length >= 4) {
                            String ssid = result[1];
                            String bssid = result[2];
                            String flags = result[3];
                            try {
                                int netId = Integer.parseInt(result[0]);
                                if (flags.indexOf("[CURRENT]") == -1) {
                                    if (flags.indexOf("[P2P-PERSISTENT]") == -1) {
                                        logd("clean up the unused persistent group. netId=" + netId);
                                        this.mWifiNative.removeNetwork(netId);
                                        isSaveRequired = true;
                                    } else if (!this.mGroups.contains(netId)) {
                                        WifiP2pGroup group = new WifiP2pGroup();
                                        group.setNetworkId(netId);
                                        group.setNetworkName(WifiSsid.createFromAsciiEncoded(ssid).toString());
                                        String mode = this.mWifiNative.getNetworkVariable(netId, "mode");
                                        if (mode != null && mode.equals("3")) {
                                            group.setIsGroupOwner(true);
                                        }
                                        if (bssid.equalsIgnoreCase(WifiP2pServiceImpl.this.mThisDevice.deviceAddress)) {
                                            group.setOwner(WifiP2pServiceImpl.this.mThisDevice);
                                        } else {
                                            WifiP2pDevice device = new WifiP2pDevice();
                                            device.deviceAddress = bssid;
                                            group.setOwner(device);
                                        }
                                        this.mGroups.add(group);
                                        isSaveRequired = true;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (reload || isSaveRequired) {
                        this.mWifiNative.saveConfig();
                        sendP2pPersistentGroupsChangedBroadcast();
                    }
                }
            }
        }

        private boolean isConfigInvalid(WifiP2pConfig config) {
            if (config == null || TextUtils.isEmpty(config.deviceAddress) || this.mPeers.get(config.deviceAddress) == null) {
                return true;
            }
            return false;
        }

        private WifiP2pDevice fetchCurrentDeviceDetails(WifiP2pConfig config) {
            int gc = this.mWifiNative.getGroupCapability(config.deviceAddress);
            if (getCurrentState() instanceof UserAuthorizingInviteRequestState) {
                gc |= 1;
            }
            this.mPeers.updateGroupCapability(config.deviceAddress, gc);
            return this.mPeers.get(config.deviceAddress);
        }

        private void p2pConnectWithPinDisplay(WifiP2pConfig config) {
            String pin;
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            if (!WifiP2pServiceImpl.this.isNfcTriggered || WifiP2pServiceImpl.this.mP2pConnectFreq == 0) {
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

        private boolean reinvokePersistentGroup(WifiP2pConfig config) {
            if (config.netId == -1) {
                return false;
            }
            int netId;
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            logd("target ssid is " + ssid + " join:" + join);
            if (join && dev.isGroupLimit()) {
                logd("target device reaches group limit.");
                join = false;
            } else if (join) {
                netId = this.mGroups.getNetworkId(dev.deviceAddress, ssid);
                if (netId >= 0) {
                    return this.mWifiNative.p2pReinvoke(netId, dev.deviceAddress);
                }
            }
            if (join || !dev.isDeviceLimit()) {
                if (!join && dev.isInvitationCapable()) {
                    netId = -2;
                    if (config.netId < 0) {
                        netId = this.mGroups.getNetworkId(dev.deviceAddress);
                    } else if (config.deviceAddress.equals(this.mGroups.getOwnerAddr(config.netId))) {
                        netId = config.netId;
                    }
                    if (netId < 0) {
                        netId = getNetworkIdFromClientList(dev.deviceAddress);
                    }
                    logd("netId related with " + dev.deviceAddress + " = " + netId);
                    if (netId >= 0) {
                        if (this.mWifiNative.p2pReinvoke(netId, dev.deviceAddress)) {
                            config.netId = netId;
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
            String p2pClients = this.mWifiNative.getNetworkVariable(netId, "p2p_client_list");
            if (p2pClients == null) {
                return null;
            }
            return p2pClients.split(" ");
        }

        private boolean removeClientFromList(int netId, String addr, boolean isRemovable) {
            StringBuilder modifiedClientList = new StringBuilder();
            String[] currentClientList = getClientList(netId);
            boolean isClientRemoved = false;
            if (currentClientList != null) {
                for (String client : currentClientList) {
                    if (client.equalsIgnoreCase(addr)) {
                        isClientRemoved = true;
                    } else {
                        modifiedClientList.append(" ");
                        modifiedClientList.append(client);
                    }
                }
            }
            if (modifiedClientList.length() == 0 && isRemovable) {
                logd("Remove unknown network");
                this.mGroups.remove(netId);
                return true;
            } else if (!isClientRemoved) {
                return false;
            } else {
                logd("Modified client list: " + modifiedClientList);
                if (modifiedClientList.length() == 0) {
                    modifiedClientList.append("\"\"");
                }
                this.mWifiNative.setNetworkVariable(netId, "p2p_client_list", modifiedClientList.toString());
                this.mWifiNative.saveConfig();
                return true;
            }
        }

        private void setWifiP2pInfoOnGroupFormation(InetAddress serverInetAddress) {
            this.mWifiP2pInfo.groupFormed = true;
            this.mWifiP2pInfo.isGroupOwner = this.mGroup.isGroupOwner();
            this.mWifiP2pInfo.groupOwnerAddress = serverInetAddress;
        }

        private void resetWifiP2pInfo() {
            WifiP2pServiceImpl.this.mGcIgnoresDhcpReq = false;
            this.mWifiP2pInfo.groupFormed = false;
            this.mWifiP2pInfo.isGroupOwner = false;
            this.mWifiP2pInfo.groupOwnerAddress = null;
            sendP2pTxBroadcast(false);
            WifiP2pServiceImpl.this.mNegoChannelConflict = false;
            if (WifiP2pServiceImpl.this.mMccSupport) {
                p2pSetCCMode(0);
            }
            WifiP2pServiceImpl.this.mConnectToPeer = false;
            WifiP2pServiceImpl.this.mCrossmountEventReceived = false;
            WifiP2pServiceImpl.this.mCrossmountSessionInfo = "";
            WifiP2pServiceImpl.this.mUpdatePeerForInvited = false;
        }

        private String getDeviceName(String deviceAddress) {
            WifiP2pDevice d = this.mPeers.get(deviceAddress);
            if (d != null) {
                return d.deviceName;
            }
            return deviceAddress;
        }

        private String getPersistedDeviceName() {
            String deviceName = Global.getString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name");
            if (deviceName != null) {
                return deviceName;
            }
            if (WifiP2pServiceImpl.this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                return SystemProperties.get("ro.oppo.market.name", "OPPO");
            }
            return SystemProperties.get("ro.product.model", "OPPO");
        }

        private boolean setAndPersistDeviceName(String devName) {
            if (devName == null) {
                return false;
            }
            if (this.mWifiNative.setDeviceName(devName)) {
                WifiP2pServiceImpl.this.mThisDevice.deviceName = devName;
                this.mWifiNative.setP2pSsidPostfix("-" + WifiP2pServiceImpl.this.getSsidPostfix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
                Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", devName);
                sendThisDeviceChangedBroadcast();
                return true;
            }
            loge("Failed to set device name " + devName);
            return false;
        }

        private boolean setWfdInfo(WifiP2pWfdInfo wfdInfo) {
            boolean success;
            if (!wfdInfo.isWfdEnabled()) {
                success = this.mWifiNative.setWfdEnable(false);
            } else if (this.mWifiNative.setWfdEnable(true)) {
                success = this.mWifiNative.setWfdDeviceInfo(wfdInfo.getDeviceInfoHex());
            } else {
                success = false;
            }
            if (success) {
                if (wfdInfo.getExtendedCapability() != 0) {
                    setWfdExtCapability(wfdInfo.getExtCapaHex());
                }
                WifiP2pServiceImpl.this.mThisDevice.wfdInfo = wfdInfo;
                sendThisDeviceChangedBroadcast();
                return true;
            }
            loge("Failed to set wfd properties, Device Info part");
            return false;
        }

        private void initializeP2pSettings() {
            this.mWifiNative.setPersistentReconnect(true);
            WifiP2pServiceImpl.this.mThisDevice.deviceName = getPersistedDeviceName();
            this.mWifiNative.setDeviceName(WifiP2pServiceImpl.this.mThisDevice.deviceName);
            this.mWifiNative.setP2pSsidPostfix("-" + WifiP2pServiceImpl.this.getSsidPostfix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
            this.mWifiNative.setDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
            this.mWifiNative.setConfigMethods("virtual_push_button physical_display keypad");
            this.mWifiNative.setConcurrencyPriority("sta");
            logd("old DeviceAddress: " + WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
            WifiP2pServiceImpl.this.mThisDevice.deviceAddress = this.mWifiNative.p2pGetDeviceAddress();
            logd("new DeviceAddress: " + WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
            updateThisDevice(3);
            logd("DeviceAddress: " + WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
            WifiP2pServiceImpl.this.mClientInfoList.clear();
            this.mWifiNative.p2pFlush();
            this.mWifiNative.p2pServiceFlush();
            WifiP2pServiceImpl.this.mServiceTransactionId = (byte) 0;
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
            WifiP2pServiceImpl.this.mMccSupport = SystemProperties.get("ro.mtk_wifi_mcc_support").equals("1");
            logd("is Mcc Supported: " + WifiP2pServiceImpl.this.mMccSupport);
            if (WifiP2pServiceImpl.this.mMccSupport) {
                p2pSetCCMode(0);
            }
        }

        private void updateThisDevice(int status) {
            WifiP2pServiceImpl.this.mThisDevice.status = status;
            sendThisDeviceChangedBroadcast();
        }

        private void handleGroupCreationFailure() {
            resetWifiP2pInfo();
            WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.FAILED, null, null);
            if (WifiP2pServiceImpl.this.mGroupRemoveReason == P2pStatus.UNKNOWN) {
                sendP2pConnectionChangedBroadcast();
            } else {
                sendP2pConnectionChangedBroadcast(WifiP2pServiceImpl.this.mGroupRemoveReason);
            }
            boolean peersChanged = this.mPeers.remove(this.mPeersLostDuringConnection);
            if (!(TextUtils.isEmpty(this.mSavedPeerConfig.deviceAddress) || this.mPeers.remove(this.mSavedPeerConfig.deviceAddress) == null)) {
                peersChanged = true;
            }
            if (peersChanged) {
                sendPeersChangedBroadcast();
            }
            this.mPeersLostDuringConnection.clear();
            clearSupplicantServiceRequest();
            if (!isWfdSinkEnabled()) {
                sendMessage(139265);
            }
            if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 0);
                WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = false;
            }
        }

        private void handleGroupRemoved() {
            if (this.mGroup.isGroupOwner()) {
                stopDhcpServer(this.mGroup.getInterface());
                File dhcpFile = new File(WifiP2pServiceImpl.DHCP_INFO_FILE);
                logd("DHCP file exists=" + dhcpFile.exists());
                if (dhcpFile.exists() && dhcpFile.delete()) {
                    logd("Delete p2p0 dhcp info file OK!");
                }
            } else {
                logd("stop IpManager");
                WifiP2pServiceImpl.this.stopIpManager();
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
            if (!TextUtils.isEmpty(WifiP2pServiceImpl.this.mWfdSourceAddr)) {
                logd("wfd source case: mWfdSourceAddr = " + WifiP2pServiceImpl.this.mWfdSourceAddr);
                while (this.mGroups.contains(this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mWfdSourceAddr))) {
                    this.mGroups.remove(this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mWfdSourceAddr));
                }
                WifiP2pServiceImpl.this.mWfdSourceAddr = null;
            }
            boolean peersChanged = false;
            for (WifiP2pDevice d : this.mGroup.getClientList()) {
                if (d != null) {
                    logd("handleGroupRemoved, call mPeers.remove - d.deviceName = " + d.deviceName + " d.deviceAddress = " + d.deviceAddress);
                }
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
            clearSupplicantServiceRequest();
            if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 0);
                WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = false;
            }
            this.mWifiNative.p2pFlush();
        }

        private void replyToMessage(Message msg, int what) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        private void replyToMessage(Message msg, int what, int arg1) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                dstMsg.arg1 = arg1;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

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

        protected void logd(String s) {
            Log.d(WifiP2pServiceImpl.TAG, s);
        }

        protected void loge(String s) {
            Log.e(WifiP2pServiceImpl.TAG, s);
        }

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
            WifiP2pServiceImpl.this.mServiceDiscReqId = this.mWifiNative.p2pServDiscReq("00:00:00:00:00:00", sb.toString());
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return false;
            }
            return true;
        }

        private void clearSupplicantServiceRequest() {
            if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                this.mWifiNative.p2pServDiscCancelReq(WifiP2pServiceImpl.this.mServiceDiscReqId);
                WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            }
        }

        private boolean addServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, true);
            if (clientInfo == null) {
                return false;
            }
            WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
            wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + 1);
            if (WifiP2pServiceImpl.this.mServiceTransactionId == (byte) 0) {
                wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + 1);
            }
            int localSevID = WifiP2pServiceImpl.this.mServiceTransactionId & 255;
            req.setTransactionId(localSevID);
            clientInfo.mReqList.put(localSevID, req);
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return true;
            }
            return updateSupplicantServiceRequest();
        }

        private void removeServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                boolean removed = false;
                for (int i = 0; i < clientInfo.mReqList.size(); i++) {
                    if (req.equals(clientInfo.mReqList.valueAt(i))) {
                        removed = true;
                        clientInfo.mReqList.removeAt(i);
                        break;
                    }
                }
                if (removed) {
                    if (clientInfo.mReqList.size() == 0 && clientInfo.mServList.size() == 0) {
                        logd("remove client information from framework");
                        WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                    }
                    if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                        updateSupplicantServiceRequest();
                    }
                }
            }
        }

        private void clearServiceRequests(Messenger m) {
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null && clientInfo.mReqList.size() != 0) {
                clientInfo.mReqList.clear();
                if (clientInfo.mServList.size() == 0) {
                    logd("remove channel information from framework");
                    WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                }
                if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                    updateSupplicantServiceRequest();
                }
            }
        }

        private boolean addLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, true);
            if (clientInfo == null || !clientInfo.mServList.add(servInfo)) {
                return false;
            }
            if (this.mWifiNative.p2pServiceAdd(servInfo)) {
                return true;
            }
            clientInfo.mServList.remove(servInfo);
            return false;
        }

        private void removeLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                this.mWifiNative.p2pServiceDel(servInfo);
                clientInfo.mServList.remove(servInfo);
                if (clientInfo.mReqList.size() == 0 && clientInfo.mServList.size() == 0) {
                    logd("remove client information from framework");
                    WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                }
            }
        }

        private void clearLocalServices(Messenger m) {
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                for (WifiP2pServiceInfo servInfo : clientInfo.mServList) {
                    this.mWifiNative.p2pServiceDel(servInfo);
                }
                clientInfo.mServList.clear();
                if (clientInfo.mReqList.size() == 0) {
                    logd("remove client information from framework");
                    WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                }
            }
        }

        private void clearClientInfo(Messenger m) {
            clearLocalServices(m);
            clearServiceRequests(m);
        }

        private void sendServiceResponse(WifiP2pServiceResponse resp) {
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                if (((WifiP2pServiceRequest) c.mReqList.get(resp.getTransactionId())) != null) {
                    Message msg = Message.obtain();
                    msg.what = 139314;
                    msg.arg1 = 0;
                    msg.arg2 = 0;
                    msg.obj = resp;
                    try {
                        c.mMessenger.send(msg);
                    } catch (RemoteException e) {
                        logd("detect dead channel");
                        clearClientInfo(c.mMessenger);
                        return;
                    }
                }
            }
        }

        private void clearClientDeadChannels() {
            ArrayList<Messenger> deadClients = new ArrayList();
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                Message msg = Message.obtain();
                msg.what = 139313;
                msg.arg1 = 0;
                msg.arg2 = 0;
                msg.obj = null;
                try {
                    c.mMessenger.send(msg);
                } catch (RemoteException e) {
                    logd("detect dead channel");
                    deadClients.add(c.mMessenger);
                }
            }
            for (Messenger m : deadClients) {
                clearClientInfo(m);
            }
        }

        private ClientInfo getClientInfo(Messenger m, boolean createIfNotExist) {
            ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.get(m);
            if (clientInfo != null || !createIfNotExist) {
                return clientInfo;
            }
            logd("add a new client");
            clientInfo = new ClientInfo(WifiP2pServiceImpl.this, m, null);
            WifiP2pServiceImpl.this.mClientInfoList.put(m, clientInfo);
            return clientInfo;
        }

        public boolean isP2pGroupNegotiationState() {
            if (getCurrentState() == this.mGroupNegotiationState || WifiP2pServiceImpl.this.mDiscoveryStarted) {
                return true;
            }
            return false;
        }

        private void sendP2pConnectionChangedBroadcast(P2pStatus reason) {
            logd("sending p2p connection changed broadcast, reason = " + reason + ", mGroup: " + this.mGroup + ", mP2pOperFreq: " + WifiP2pServiceImpl.this.mP2pOperFreq);
            Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
            intent.putExtra("networkInfo", new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            intent.putExtra("p2pGroupInfo", new WifiP2pGroup(this.mGroup));
            intent.putExtra("p2pOperFreq", WifiP2pServiceImpl.this.mP2pOperFreq);
            if (reason == P2pStatus.NO_COMMON_CHANNEL) {
                intent.putExtra("reason=", 7);
            } else if (reason == P2pStatus.MTK_EXPAND_02) {
                logd("channel conflict, user decline, broadcast with reason=-3");
                intent.putExtra("reason=", -3);
            } else if (reason == P2pStatus.MTK_EXPAND_01) {
                logd("[wfd sink/source] broadcast with reason=-2");
                intent.putExtra("reason=", -2);
            } else {
                intent.putExtra("reason=", -1);
            }
            WifiP2pServiceImpl.this.mGroupRemoveReason = P2pStatus.UNKNOWN;
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
        }

        private void sendP2pTxBroadcast(boolean bStart) {
            logd("sending p2p Tx broadcast: " + bStart);
            Intent intent = new Intent("com.mediatek.wifi.p2p.Tx");
            intent.addFlags(603979776);
            intent.putExtra("start", bStart);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pGOandGCRequestConnectBroadcast() {
            logd("sendP2pGOandGCRequestConnectBroadcast");
            Intent intent = new Intent("com.mediatek.wifi.p2p.GO.GCrequest.connect");
            intent.addFlags(603979776);
            WifiP2pDevice dev = this.mPeers.get(this.mSavedPeerConfig.deviceAddress);
            if (dev == null || dev.deviceName == null) {
                intent.putExtra("deviceName", "wifidisplay source");
            } else {
                intent.putExtra("deviceName", dev.deviceName);
            }
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pOPChannelBroadcast() {
            logd("sendP2pOPChannelBroadcast: OperFreq = " + WifiP2pServiceImpl.this.mP2pOperFreq);
            Intent intent = new Intent("com.mediatek.wifi.p2p.OP.channel");
            intent.addFlags(603979776);
            intent.putExtra("p2pOperFreq", WifiP2pServiceImpl.this.mP2pOperFreq);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pFreqConflictBroadcast() {
            logd("sendP2pFreqConflictBroadcast");
            Intent intent = new Intent("com.mediatek.wifi.p2p.freq.conflict");
            intent.addFlags(603979776);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pCrossmountIntentionBroadcast() {
            logd("sendP2pCrossmountIntentionBroadcast, session info:" + WifiP2pServiceImpl.this.mCrossmountSessionInfo + ", wps method=" + this.mSavedPeerConfig.wps.setup);
            Intent intent = new Intent("com.mediatek.wifi.p2p.crossmount.intention");
            intent.addFlags(603979776);
            WifiP2pDevice dev = this.mPeers.get(this.mSavedPeerConfig.deviceAddress);
            if (dev == null || dev.deviceName == null) {
                intent.putExtra("deviceName", "crossmount source");
                intent.putExtra("deviceAddress", "crossmount source");
                intent.putExtra("sessionInfo", "63726f73736d6f756e7420736f75726365");
            } else {
                intent.putExtra("deviceName", dev.deviceName);
                intent.putExtra("deviceAddress", dev.deviceAddress);
                intent.putExtra("sessionInfo", WifiP2pServiceImpl.this.mCrossmountSessionInfo);
            }
            intent.putExtra("wpsMethod", Integer.toString(this.mSavedPeerConfig.wps.setup));
            WifiP2pServiceImpl.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }

        private WifiP2pDevice p2pGoGetSta(WifiP2pDevice p2pDev, String p2pMAC) {
            if (p2pMAC == null || p2pDev == null) {
                loge("gc or gc mac is null");
                return null;
            }
            p2pDev.deviceAddress = p2pMAC;
            String p2pSta = p2pGoGetSta(p2pMAC);
            if (p2pSta == null) {
                return p2pDev;
            }
            logd("p2pGoGetSta() return: " + p2pSta);
            for (String token : p2pSta.split("\n")) {
                if (token.startsWith("p2p_device_name=")) {
                    p2pDev.deviceName = nameValueAssign(token.split("="), p2pDev.deviceName);
                } else if (token.startsWith("p2p_primary_device_type=")) {
                    p2pDev.primaryDeviceType = nameValueAssign(token.split("="), p2pDev.primaryDeviceType);
                } else if (token.startsWith("p2p_group_capab=")) {
                    p2pDev.groupCapability = nameValueAssign(token.split("="), p2pDev.groupCapability);
                } else if (token.startsWith("p2p_dev_capab=")) {
                    p2pDev.deviceCapability = nameValueAssign(token.split("="), p2pDev.deviceCapability);
                } else if (token.startsWith("p2p_config_methods=")) {
                    p2pDev.wpsConfigMethodsSupported = nameValueAssign(token.split("="), p2pDev.wpsConfigMethodsSupported);
                }
            }
            return p2pDev;
        }

        private String nameValueAssign(String[] nameValue, String string) {
            if (nameValue == null || nameValue.length != 2) {
                return null;
            }
            return nameValue[1];
        }

        /* JADX WARNING: Missing block: B:4:0x0008, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int nameValueAssign(String[] nameValue, int integer) {
            if (nameValue == null || nameValue.length != 2 || nameValue[1] == null) {
                return 0;
            }
            return WifiP2pDevice.parseHex(nameValue[1]);
        }

        /* JADX WARNING: Missing block: B:4:0x0008, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int nameValueAssign(String[] nameValue, int integer, int base) {
            if (nameValue == null || nameValue.length != 2 || nameValue[1] == null || base == 0) {
                return 0;
            }
            return Integer.parseInt(nameValue[1], base);
        }

        private void setWifiOn_WifiAPOff() {
            if (WifiP2pServiceImpl.this.mWifiManager == null) {
                WifiP2pServiceImpl.this.mWifiManager = (WifiManager) WifiP2pServiceImpl.this.mContext.getSystemService("wifi");
            }
            int wifiApState = WifiP2pServiceImpl.this.mWifiManager.getWifiApState();
            if (wifiApState == 12 || wifiApState == 13) {
                WifiP2pServiceImpl.this.mWifiManager.setWifiApEnabled(null, false);
            }
            logd("call WifiManager.stopReconnectAndScan() and WifiManager.setWifiEnabled()");
            WifiP2pServiceImpl.this.mWifiManager.stopReconnectAndScan(true, 0);
            WifiP2pServiceImpl.this.mWifiManager.setWifiEnabled(true);
        }

        public WifiInfo getWifiConnectionInfo() {
            if (WifiP2pServiceImpl.this.mWifiManager == null) {
                WifiP2pServiceImpl.this.mWifiManager = (WifiManager) WifiP2pServiceImpl.this.mContext.getSystemService("wifi");
            }
            return WifiP2pServiceImpl.this.mWifiManager.getConnectionInfo();
        }

        private String getInterfaceAddress(String deviceAddress) {
            logd("getInterfaceAddress(): deviceAddress=" + deviceAddress);
            WifiP2pDevice d = this.mPeers.get(deviceAddress);
            if (d == null || deviceAddress.equals(d.interfaceAddress)) {
                return deviceAddress;
            }
            logd("getInterfaceAddress(): interfaceAddress=" + d.interfaceAddress);
            return d.interfaceAddress;
        }

        /* JADX WARNING: Removed duplicated region for block: B:61:0x0192 A:{SYNTHETIC, Splitter: B:61:0x0192} */
        /* JADX WARNING: Removed duplicated region for block: B:69:0x01b8 A:{SYNTHETIC, Splitter: B:69:0x01b8} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String getPeerIpAddress(String inputAddress) {
            IOException e;
            Throwable th;
            logd("getPeerIpAddress(): input address=" + inputAddress);
            if (inputAddress == null) {
                return null;
            }
            if (this.mGroup == null) {
                loge("getPeerIpAddress(): mGroup is null!");
                return null;
            } else if (this.mGroup.isGroupOwner()) {
                String intrerfaceAddress = getInterfaceAddress(inputAddress);
                FileInputStream fileStream = null;
                try {
                    FileInputStream fileStream2 = new FileInputStream(WifiP2pServiceImpl.DHCP_INFO_FILE);
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fileStream2)));
                        String str = br.readLine();
                        while (str != null && str.length() != 0) {
                            String[] fields = str.split(" ");
                            if (fields.length > 3) {
                                str = fields[2];
                            } else {
                                str = null;
                            }
                            if (str == null || fields[1] == null || fields[1].indexOf(intrerfaceAddress) == -1) {
                                String lastOneIP = str;
                                str = br.readLine();
                            } else {
                                logd("getPeerIpAddress(): getClientIp() mac matched, get IP address = " + str);
                                if (fileStream2 != null) {
                                    try {
                                        fileStream2.close();
                                    } catch (IOException e2) {
                                        loge("getPeerIpAddress(): getClientIp() close file met IOException: " + e2);
                                    }
                                }
                                return str;
                            }
                        }
                        loge("getPeerIpAddress(): getClientIp() dhcp client " + intrerfaceAddress + " had not connected up!");
                        if (fileStream2 != null) {
                            try {
                                fileStream2.close();
                            } catch (IOException e22) {
                                loge("getPeerIpAddress(): getClientIp() close file met IOException: " + e22);
                            }
                        }
                        return null;
                    } catch (IOException e3) {
                        e22 = e3;
                        fileStream = fileStream2;
                        try {
                            loge("getPeerIpAddress(): getClientIp(): " + e22);
                            if (fileStream != null) {
                            }
                            loge("getPeerIpAddress(): found nothing");
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileStream != null) {
                                try {
                                    fileStream.close();
                                } catch (IOException e222) {
                                    loge("getPeerIpAddress(): getClientIp() close file met IOException: " + e222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileStream = fileStream2;
                        if (fileStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e222 = e4;
                    loge("getPeerIpAddress(): getClientIp(): " + e222);
                    if (fileStream != null) {
                        try {
                            fileStream.close();
                        } catch (IOException e2222) {
                            loge("getPeerIpAddress(): getClientIp() close file met IOException: " + e2222);
                        }
                    }
                    loge("getPeerIpAddress(): found nothing");
                    return null;
                }
            } else if (this.mGroup.getOwner().deviceAddress != null && inputAddress.equals(this.mGroup.getOwner().deviceAddress)) {
                logd("getPeerIpAddress(): GO device address case, goIpAddress=" + this.mGroup.getOwner().deviceIP);
                return this.mGroup.getOwner().deviceIP;
            } else if (this.mGroup.getOwner().interfaceAddress == null || !inputAddress.equals(this.mGroup.getOwner().interfaceAddress)) {
                loge("getPeerIpAddress(): no match GO address case, goIpAddress is null");
                return null;
            } else {
                logd("getPeerIpAddress(): GO interface address case, goIpAddress=" + this.mGroup.getOwner().deviceIP);
                return this.mGroup.getOwner().deviceIP;
            }
        }

        public void setCrossMountIE(boolean isAdd, String hexData) {
            WifiP2pServiceImpl.this.mCrossmountIEAdded = isAdd;
            setVendorElemIE(isAdd, WifiP2pServiceImpl.VENDOR_IE_ALL_FRAME_TAG, hexData);
        }

        public String getCrossMountIE(String hexData) {
            int indexCrossMountTag = hexData.indexOf("000ce733");
            if (indexCrossMountTag < WifiP2pServiceImpl.VENDOR_IE_TAG.length() + 2) {
                loge("getCrossMountIE(): bad index: indexCrossMountTag=" + indexCrossMountTag);
                return "";
            }
            String strLenIE = hexData.substring(indexCrossMountTag - 2, indexCrossMountTag);
            if (TextUtils.isEmpty(strLenIE)) {
                loge("getCrossMountIE(): bad strLenIE: " + strLenIE);
                return "";
            }
            String hexCrossMountIE = hexData.substring(indexCrossMountTag + 8, (indexCrossMountTag + 8) + ((Integer.valueOf(strLenIE, 16).intValue() - 4) * 2));
            loge("getCrossMountIE(): hexCrossMountIE=" + hexCrossMountIE);
            return hexCrossMountIE;
        }

        private boolean isGroupRemoved() {
            boolean removed = true;
            for (WifiP2pDevice d : this.mPeers.getDeviceList()) {
                if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(d.deviceAddress) && d.status == 0) {
                    removed = false;
                }
            }
            logd("isGroupRemoved(): " + removed);
            return removed;
        }

        private void resetWifiP2pConn() {
            if (this.mGroup != null) {
                this.mWifiNative.p2pGroupRemove(WifiP2pServiceImpl.this.mInterface);
            } else if (getHandler().hasMessages(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT)) {
                sendMessage(139274);
            }
        }

        private void p2pConfigWfdSink() {
            resetWifiP2pConn();
            this.mWifiNative.setDeviceType("8-0050F204-2");
            String result = p2pGetDeviceCapa();
            if (result.startsWith("p2p_dev_capa=")) {
                WifiP2pServiceImpl.this.mDeviceCapa = nameValueAssign(result.split("="), WifiP2pServiceImpl.this.mDeviceCapa, 10);
            } else {
                WifiP2pServiceImpl.this.mDeviceCapa = -1;
            }
            logd("[wfd sink] p2pConfigWfdSink() ori deviceCapa = " + WifiP2pServiceImpl.this.mDeviceCapa);
            if (WifiP2pServiceImpl.this.mDeviceCapa > 0) {
                p2pSetDeviceCapa((Integer.valueOf(WifiP2pServiceImpl.this.mDeviceCapa).intValue() & 223) + "");
                logd("[wfd sink] p2pConfigWfdSink() after: " + p2pGetDeviceCapa());
            }
        }

        private void p2pUnconfigWfdSink() {
            resetWifiP2pConn();
            this.mWifiNative.setDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
            if (WifiP2pServiceImpl.this.mDeviceCapa > 0) {
                p2pSetDeviceCapa(WifiP2pServiceImpl.this.mDeviceCapa + "");
                logd("[wfd sink] p2pUnconfigWfdSink(): " + p2pGetDeviceCapa());
            }
        }

        private boolean isWfdSinkEnabled() {
            if (!SystemProperties.get("ro.mtk_wfd_sink_support").equals("1")) {
                logd("[wfd sink] isWfdSinkEnabled, property unset");
            } else if (WifiP2pServiceImpl.this.mThisDevice.wfdInfo == null) {
                logd("[wfd sink] isWfdSinkEnabled, device wfdInfo unset");
            } else if (WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType() == 1 || WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType() == 3) {
                return true;
            } else {
                logd("[wfd sink] isWfdSinkEnabled, type :" + WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType());
            }
            return false;
        }

        private boolean isWfdSinkConnected() {
            boolean basicCondition;
            if (!isWfdSinkEnabled() || this.mGroup == null) {
                basicCondition = false;
            } else {
                basicCondition = true;
            }
            if (basicCondition) {
                return !this.mGroup.isGroupOwner() || this.mGroup.getClientAmount() == 1;
            } else {
                return false;
            }
        }

        private boolean isCrossMountGOwithMultiGC() {
            boolean basicCondition;
            if (!WifiP2pServiceImpl.this.mCrossmountIEAdded || this.mGroup == null) {
                basicCondition = false;
            } else {
                basicCondition = true;
            }
            return basicCondition && this.mGroup.isGroupOwner() && this.mGroup.getClientAmount() > 0;
        }

        private boolean isWfdSourceConnected() {
            if (WifiP2pServiceImpl.this.mThisDevice.wfdInfo == null) {
                logd("[wfd source] isWfdSourceConnected, device wfdInfo unset");
            } else if (WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType() != 0 && WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType() != 3) {
                logd("[wfd source] isWfdSourceConnected, type :" + WifiP2pServiceImpl.this.mThisDevice.wfdInfo.getDeviceType());
            } else if (!isGroupRemoved()) {
                return true;
            } else {
                logd("[wfd source] isWfdSourceConnected, GroupRemoved");
            }
            return false;
        }

        private void setVendorElemIE(boolean isAdd, int frameId, String hexData) {
            logd("setVendorElemIE(): isAdd=" + isAdd + ", frameId=" + frameId + ", hexData=" + hexData);
            String ieBuf = "000ce733" + hexData;
            int len = ieBuf.length() / 2;
            StringBuilder append = new StringBuilder().append(WifiP2pServiceImpl.VENDOR_IE_TAG);
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(len & 255);
            ieBuf = append.append(String.format("%02x", objArr)).append(ieBuf).toString();
            int i;
            if (isAdd) {
                if (frameId == WifiP2pServiceImpl.VENDOR_IE_ALL_FRAME_TAG) {
                    for (i = 0; i <= 12; i++) {
                        vendorIEAdd(i, ieBuf);
                    }
                    return;
                }
                vendorIEAdd(frameId, ieBuf);
            } else if (frameId == WifiP2pServiceImpl.VENDOR_IE_ALL_FRAME_TAG) {
                for (i = 0; i <= 12; i++) {
                    vendorIERemove(i, null);
                }
            } else {
                vendorIERemove(frameId, null);
            }
        }

        private void updateCrossMountInfo(String peerAddress) {
            logd("updateCrossMountInfo(), peerAddress=" + peerAddress);
            String peerVendorIE = this.mWifiNative.p2pGetVendorElems(peerAddress);
            WifiP2pServiceImpl.this.mCrossmountEventReceived = false;
            if (TextUtils.isEmpty(peerVendorIE) || peerVendorIE.equals(WifiP2pServiceImpl.UNKNOWN_COMMAND) || !peerVendorIE.contains("000ce733")) {
                logd("updateCrossMountInfo(): check crossmount IE myself!");
                int i = 0;
                while (i <= 12) {
                    String myVendorIE = vendorIEGet(i);
                    if (TextUtils.isEmpty(myVendorIE) || myVendorIE.equals(WifiP2pServiceImpl.UNKNOWN_COMMAND) || !myVendorIE.contains("000ce733")) {
                        i++;
                    } else {
                        WifiP2pServiceImpl.this.mCrossmountEventReceived = true;
                        WifiP2pServiceImpl.this.mCrossmountSessionInfo = "";
                        return;
                    }
                }
            }
            WifiP2pServiceImpl.this.mCrossmountEventReceived = true;
            WifiP2pServiceImpl.this.mCrossmountSessionInfo = getCrossMountIE(peerVendorIE);
        }

        private void p2pUpdateScanList(String peerAddress) {
            if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(peerAddress) && this.mPeers.get(peerAddress) == null) {
                String peerInfo = this.mWifiNative.p2pPeer(peerAddress);
                if (!TextUtils.isEmpty(peerInfo)) {
                    logd("p2pUpdateScanList(): " + peerAddress + "  isn't in framework scan list but existed in supplicant's list");
                    WifiP2pDevice device = new WifiP2pDevice();
                    device.deviceAddress = peerAddress;
                    for (String token : peerInfo.split("\n")) {
                        if (token.startsWith("device_name=")) {
                            device.deviceName = nameValueAssign(token.split("="), device.deviceName);
                        } else if (token.startsWith("pri_dev_type=")) {
                            device.primaryDeviceType = nameValueAssign(token.split("="), device.primaryDeviceType);
                        } else if (token.startsWith("config_methods=")) {
                            device.wpsConfigMethodsSupported = nameValueAssign(token.split("="), device.wpsConfigMethodsSupported);
                        } else if (token.startsWith("dev_capab=")) {
                            device.deviceCapability = nameValueAssign(token.split("="), device.deviceCapability);
                        } else if (token.startsWith("group_capab=")) {
                            device.groupCapability = nameValueAssign(token.split("="), device.groupCapability);
                        }
                    }
                    this.mPeers.updateSupplicantDetails(device);
                }
            }
        }

        private void p2pOverwriteWpsPin(String caller, Object obj) {
            if (obj != null) {
                int pinMethod = ((Bundle) obj).getInt("android.net.wifi.p2p.EXTRA_PIN_METHOD");
                String pinCode = ((Bundle) obj).getString("android.net.wifi.p2p.EXTRA_PIN_CODE");
                this.mSavedPeerConfig.wps.setup = pinMethod;
                this.mSavedPeerConfig.wps.pin = pinCode;
                logd("p2pOverwriteWpsPin(): " + caller + ", wps pin code: " + pinCode + ", pin method: " + pinMethod);
            }
        }

        private void p2pUserAuthPreprocess(Message message) {
            if (message.arg1 >= 0 && message.arg1 <= 15) {
                this.mSavedPeerConfig.groupOwnerIntent = message.arg1;
            }
            this.mSavedPeerConfig.netId = -1;
            if (this.mSavedPeerConfig.wps.setup == 1 && !WifiP2pServiceImpl.this.mCrossmountEventReceived) {
                notifyInvitationSent(this.mSavedPeerConfig.wps.pin, this.mSavedPeerConfig.deviceAddress);
            }
        }

        private boolean isAppHandledConnection() {
            return !isWfdSinkEnabled() ? WifiP2pServiceImpl.this.mCrossmountEventReceived : true;
        }

        private boolean p2pRemoveClient(String iface, String mac) {
            String ret = this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + iface + " P2P_REMOVE_CLIENT " + mac);
            return !TextUtils.isEmpty(ret) ? ret.startsWith("OK") : false;
        }

        private String p2pSetCCMode(int ccMode) {
            return this.mWifiNative.doCustomSupplicantCommand("DRIVER p2p_use_mcc=" + ccMode);
        }

        private String p2pGetDeviceCapa() {
            return this.mWifiNative.doCustomSupplicantCommand("DRIVER p2p_get_cap p2p_dev_capa");
        }

        private String p2pSetDeviceCapa(String strDecimal) {
            return this.mWifiNative.doCustomSupplicantCommand("DRIVER p2p_set_cap p2p_dev_capa " + strDecimal);
        }

        private void setP2pPowerSaveMtk(String iface, int mode) {
            this.mWifiNative.doCustomSupplicantCommand("DRIVER p2p_set_power_save " + mode);
        }

        private void setWfdExtCapability(String hex) {
            this.mWifiNative.doCustomSupplicantCommand("WFD_SUBELEM_SET 7 " + hex);
        }

        private void p2pBeamPlusGO(int reserve) {
            if (reserve == 0) {
                this.mWifiNative.doCustomSupplicantCommand("DRIVER BEAMPLUS_GO_RESERVE_END");
            } else if (1 == reserve) {
                this.mWifiNative.doCustomSupplicantCommand("DRIVER BEAMPLUS_GO_RESERVE_START");
            }
        }

        private void p2pBeamPlus(int state) {
            if (state == 0) {
                this.mWifiNative.doCustomSupplicantCommand("DRIVER BEAMPLUS_STOP");
            } else if (1 == state) {
                this.mWifiNative.doCustomSupplicantCommand("DRIVER BEAMPLUS_START");
            }
        }

        private void p2pSetBssid(int id, String bssid) {
            this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " SET_NETWORK " + id + " bssid " + bssid);
        }

        private String p2pLinkStatics(String interfaceAddress) {
            return this.mWifiNative.doCustomSupplicantCommand("DRIVER GET_STA_STATISTICS " + interfaceAddress);
        }

        private String p2pGoGetSta(String deviceAddress) {
            return this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " " + "STA " + deviceAddress);
        }

        public void p2pAutoChannel(int enable) {
            this.mWifiNative.doCustomSupplicantCommand("enable_channel_selection " + enable);
        }

        private void vendorIEAdd(int frameId, String hex) {
            this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " VENDOR_ELEM_ADD " + frameId + " " + hex);
        }

        private String vendorIEGet(int frameId) {
            return this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " VENDOR_ELEM_GET " + frameId);
        }

        private void vendorIERemove(int frameId, String hex) {
            if (hex == null) {
                this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " VENDOR_ELEM_REMOVE " + frameId + " *");
            } else {
                this.mWifiNative.doCustomSupplicantCommand("IFNAME=" + this.mWifiNative.getInterfaceName() + " VENDOR_ELEM_REMOVE " + frameId + " " + hex);
            }
        }

        private WifiP2pGroup addPersistentGroup(HashMap<String, String> variables) {
            logd("addPersistentGroup");
            int netId = this.mWifiNative.addNetwork();
            for (String key : variables.keySet()) {
                logd("addPersistentGroup variable=" + key + " : " + ((String) variables.get(key)));
                this.mWifiNative.setNetworkVariable(netId, key, (String) variables.get(key));
            }
            updatePersistentNetworks(true);
            for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                if (netId == group.getNetworkId()) {
                    return group;
                }
            }
            logd("addPersistentGroup failed.");
            return null;
        }

        public void setBeamMode(int mode) {
            logd("setBeamMode mode=" + mode);
            switch (mode) {
                case 0:
                    p2pBeamPlus(1);
                    WifiP2pServiceImpl.this.mGcIgnoresDhcpReq = true;
                    return;
                case 1:
                    p2pBeamPlusGO(1);
                    return;
                case 2:
                    p2pBeamPlus(0);
                    WifiP2pServiceImpl.this.mGcIgnoresDhcpReq = false;
                    return;
                case 3:
                    p2pBeamPlusGO(0);
                    return;
                default:
                    return;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum P2pStatus {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void");
        }

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
                case 12:
                    return MTK_EXPAND_01;
                case 13:
                    return MTK_EXPAND_02;
                default:
                    return UNKNOWN;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void");
    }

    public WifiP2pServiceImpl(Context context) {
        this.mReplyChannel = new AsyncChannel();
        this.mThisDevice = new WifiP2pDevice();
        this.mDiscoveryPostponed = false;
        this.mTemporarilyDisconnectedWifi = false;
        this.mServiceTransactionId = (byte) 0;
        this.mClientInfoList = new HashMap();
        this.isNfcTriggered = false;
        this.mP2pConnectFreq = 0;
        this.mFastConTriggeredNum = 0;
        this.mFoundTargetDevice = false;
        this.mFindTimes = 0;
        this.mIntentToConnectPeer = "";
        this.mGcIgnoresDhcpReq = false;
        this.mGroupRemoveReason = P2pStatus.UNKNOWN;
        this.mMiracastMode = 0;
        this.mStopP2pMonitorTimeoutIndex = 0;
        this.WFD_DONGLE_USE_P2P_INVITE = SystemProperties.getBoolean("persist.p2p.wfd.invitedongle", true);
        this.mP2pOperFreq = -1;
        this.mNegoChannelConflict = false;
        this.mConnectToPeer = false;
        this.mMccSupport = false;
        this.mDelayReconnectForInfoUnavailable = true;
        this.mUpdatePeerForInvited = false;
        this.mCrossmountIEAdded = false;
        this.mCrossmountEventReceived = false;
        this.mCrossmountSessionInfo = "";
        this.mNfcWifiNative = WifiNative.getP2pNativeInterface();
        this.mContext = context;
        this.mNetworkInfo = new NetworkInfo(13, 0, NETWORKTYPE, "");
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mThisDevice.primaryDeviceType = this.mContext.getResources().getString(17039416);
        HandlerThread wifiP2pThread = new HandlerThread(TAG);
        wifiP2pThread.start();
        this.mClientHandler = new ClientHandler(wifiP2pThread.getLooper());
        this.mP2pStateMachine = new P2pStateMachine(TAG, wifiP2pThread.getLooper(), this.mP2pSupported);
        this.mP2pStateMachine.start();
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

    private void stopIpManager() {
        if (this.mIpManager != null) {
            this.mIpManager.stop();
            this.mIpManager = null;
        }
        this.mDhcpResults = null;
    }

    private void startIpManager(String ifname) {
        ProvisioningConfiguration config;
        stopIpManager();
        this.mIpManager = new IpManager(this.mContext, ifname, new Callback() {
            public void onPreDhcpAction() {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION);
            }

            public void onPostDhcpAction() {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_POST_DHCP_ACTION);
            }

            public void onNewDhcpResults(DhcpResults dhcpResults) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_DHCP_RESULTS, dhcpResults);
            }

            public void onProvisioningSuccess(LinkProperties newLp) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS);
            }

            public void onProvisioningFailure(LinkProperties newLp) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE);
            }
        }, this.mNwService);
        IpManager ipManager;
        if (this.isNfcTriggered) {
            Slog.d(TAG, "startIpManager set static ip to 192.168.49.100");
            DhcpResults dhcpResults = new DhcpResults();
            dhcpResults.setIpAddress("192.168.49.100", 24);
            dhcpResults.setGateway(SERVER_ADDRESS);
            dhcpResults.addDns(SERVER_ADDRESS);
            ipManager = this.mIpManager;
            config = IpManager.buildProvisioningConfiguration().withoutIPv6().withoutIpReachabilityMonitor().withStaticConfiguration(dhcpResults).build();
        } else {
            Slog.d(TAG, "startIpManager aquire dynamic ip for client");
            ipManager = this.mIpManager;
            config = IpManager.buildProvisioningConfiguration().withoutIPv6().withoutIpReachabilityMonitor().withPreDhcpAction(30000).withProvisioningTimeoutMs(36000).build();
        }
        this.mIpManager.startProvisioning(config);
    }

    public Messenger getMessenger() {
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mClientHandler);
    }

    public Messenger getP2pStateMachineMessenger() {
        enforceConnectivityInternalOrLocationHardwarePermission();
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mP2pStateMachine.getHandler());
    }

    public void setNfcTriggered(boolean enable) {
        if (this.isNfcTriggered == enable) {
            Slog.d(TAG, "isNfcTriggered already " + enable);
            return;
        }
        Slog.d(TAG, "set isNfcTriggered to " + enable);
        if (enable) {
            P2pStateMachine p2pStateMachine = this.mP2pStateMachine;
            int i = this.mFastConTriggeredNum + 1;
            this.mFastConTriggeredNum = i;
            p2pStateMachine.sendMessageDelayed(AUTO_ACCEPT_INVITATION_TIME_OUT, i, PasspointManagementObjectManager.IntervalFactor);
            WifiConnectivityManager.setPeriodicScanIntervalMs(enable);
            this.mNfcWifiNative.setOshareMod("1");
            this.mNfcWifiNative.setP2pSsidPostfix("-fastCon");
        } else {
            this.mFastConTriggeredNum = 0;
            WifiConnectivityManager.setPeriodicScanIntervalMs(enable);
            this.mNfcWifiNative.setOshareMod("0");
            this.mNfcWifiNative.setP2pSsidPostfix("-" + getSsidPostfix(this.mThisDevice.deviceName));
        }
        this.isNfcTriggered = enable;
    }

    public void setMiracastMode(int mode) {
        enforceConnectivityInternalPermission();
        this.mP2pStateMachine.sendMessage(SET_MIRACAST_MODE, mode);
    }

    public void setMiracastModeEx(int mode, int freq) {
        this.mP2pStateMachine.sendMessage(SET_MIRACAST_MODE, mode, freq);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
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
        pw.println();
        IpManager ipManager = this.mIpManager;
        if (ipManager != null) {
            pw.println("mIpManager:");
            ipManager.dump(fd, pw, args);
        }
    }

    public boolean isP2pGroupNegotiationState() {
        if (this.mP2pStateMachine != null) {
            return this.mP2pStateMachine.isP2pGroupNegotiationState();
        }
        return false;
    }

    public String getMacAddress() {
        Log.d(TAG, "getMacAddress(): before retriving from NVRAM = " + this.mThisDevice.deviceAddress);
        String MAC_ADDRESS_FILENAME = "/data/nvram/APCFG/APRDEB/WIFI";
        String NVRAM_AGENT_SERVICE = "NvRAMAgent";
        try {
            byte[] buff = WifiNvRamAgent.Stub.asInterface(ServiceManager.getService("NvRAMAgent")).readFileByName("/data/nvram/APCFG/APRDEB/WIFI");
            String macFromNVRam = "";
            if (buff != null) {
                Object[] objArr = new Object[6];
                objArr[0] = Integer.valueOf(buff[4] | 2);
                objArr[1] = Byte.valueOf(buff[5]);
                objArr[2] = Byte.valueOf(buff[6]);
                objArr[3] = Byte.valueOf(buff[7]);
                objArr[4] = Byte.valueOf(buff[8]);
                objArr[5] = Byte.valueOf(buff[9]);
                macFromNVRam = String.format("%02x:%02x:%02x:%02x:%02x:%02x", objArr);
                if (!TextUtils.isEmpty(macFromNVRam)) {
                    this.mThisDevice.deviceAddress = macFromNVRam;
                }
            }
            Log.d(TAG, "getMacAddress(): after retriving from NVRAM = " + this.mThisDevice.deviceAddress);
        } catch (RemoteException re) {
            re.printStackTrace();
            Log.d(TAG, "getMacAddress(): after retriving from NVRAM = " + this.mThisDevice.deviceAddress);
        } catch (IndexOutOfBoundsException iobe) {
            iobe.printStackTrace();
            Log.d(TAG, "getMacAddress(): after retriving from NVRAM = " + this.mThisDevice.deviceAddress);
        } catch (Throwable th) {
            Log.d(TAG, "getMacAddress(): after retriving from NVRAM = " + this.mThisDevice.deviceAddress);
            throw th;
        }
        return this.mThisDevice.deviceAddress;
    }

    public String getPeerIpAddress(String peerMacAddress) {
        return this.mP2pStateMachine.getPeerIpAddress(peerMacAddress);
    }

    public void setCrossMountIE(boolean isAdd, String hexData) {
        this.mP2pStateMachine.setCrossMountIE(isAdd, hexData);
    }

    public void setBeamMode(int mode) {
        enforceConnectivityInternalPermission();
        this.mP2pStateMachine.sendMessage(SET_BEAM_MODE, mode);
    }

    private String getSsidPostfix(String deviceName) {
        int utfCount = 0;
        int strLen = 0;
        byte[] bChar = deviceName.getBytes();
        if (TextUtils.isEmpty(deviceName) || bChar.length <= 22) {
            return deviceName;
        }
        for (int i = 0; i <= deviceName.length(); i++) {
            byte b0 = bChar[utfCount];
            Log.d(TAG, "b0=" + b0 + ", i=" + i + ", utfCount=" + utfCount);
            if ((b0 & 128) == 0) {
                utfCount++;
            } else if (b0 >= (byte) -4 && b0 <= (byte) -3) {
                utfCount += 6;
            } else if (b0 >= (byte) -8) {
                utfCount += 5;
            } else if (b0 >= (byte) -16) {
                utfCount += 4;
            } else if (b0 >= (byte) -32) {
                utfCount += 3;
            } else if (b0 >= (byte) -64) {
                utfCount += 2;
            }
            if (utfCount > 22) {
                strLen = i;
                Log.d(TAG, "break: utfCount=" + utfCount + ", strLen=" + strLen);
                break;
            }
        }
        return deviceName.substring(0, strLen);
    }

    void stopReconnectAndScan() {
        Log.d(TAG, "stopReconnectAndScan() is called");
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
    }

    void resumeReconnectAndScan() {
        Log.d(TAG, "resumeReconnectAndScan() is called");
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
    }
}
