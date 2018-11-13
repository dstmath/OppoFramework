package android.net.ip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LinkProperties.ProvisioningChange;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.apf.ApfCapabilities;
import android.net.apf.ApfFilter;
import android.net.dhcp.Dhcp6Client;
import android.net.dhcp.DhcpClient;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.IpManagerEvent;
import android.net.util.AvoidBadWifiTracker;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.IState;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.net.NetlinkTracker;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringJoiner;
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
public class IpManager extends StateMachine {
    /* renamed from: -android-net-LinkProperties$ProvisioningChangeSwitchesValues */
    private static final /* synthetic */ int[] f1-android-net-LinkProperties$ProvisioningChangeSwitchesValues = null;
    private static final String CLAT_PREFIX = "v4-";
    private static final int CMD_CONFIRM = 3;
    private static final int CMD_SET_MULTICAST_FILTER = 8;
    private static final int CMD_START = 2;
    private static final int CMD_STOP = 1;
    private static final int CMD_UPDATE_HTTP_PROXY = 7;
    private static final int CMD_UPDATE_TCP_BUFFER_SIZES = 6;
    private static final boolean DBG = true;
    public static final String DUMP_ARG = "ipmanager";
    public static final String DUMP_ARG_CONFIRM = "confirm";
    private static final int EVENT_DHCPACTION_TIMEOUT = 10;
    private static final int EVENT_NETLINK_LINKPROPERTIES_CHANGED = 5;
    private static final int EVENT_PRE_DHCP_ACTION_COMPLETE = 4;
    private static final int EVENT_PROVISIONING_TIMEOUT = 9;
    private static final int MAX_LOG_RECORDS = 500;
    private static final boolean NO_CALLBACKS = false;
    private static final boolean SDBG = false;
    private static final boolean SEND_CALLBACKS = true;
    private static final boolean VDBG = true;
    private static final String WIFI_DIFFERENT_IP = "wifi_different_ip";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final Class[] sMessageClasses = null;
    private static final boolean sMtkDhcpv6cWifi = false;
    private static final SparseArray<String> sWhatToString = null;
    private ApfFilter mApfFilter;
    private final AvoidBadWifiTracker mAvoidBadWifiTracker;
    protected final Callback mCallback;
    private final String mClatInterfaceName;
    private ProvisioningConfiguration mConfiguration;
    private final Context mContext;
    private Dhcp6Client mDhcp6Client;
    private final WakeupMessage mDhcpActionTimeoutAlarm;
    private DhcpClient mDhcpClient;
    private DhcpResults mDhcpResults;
    private ArrayList<InetAddress> mDnsV6Servers;
    private boolean mHandleGateConflict;
    private ProxyInfo mHttpProxy;
    private final String mInterfaceName;
    private IpReachabilityMonitor mIpReachabilityMonitor;
    private boolean mIsStartCalled;
    private boolean mIsWifiSMStarted;
    private LinkProperties mLinkProperties;
    private final LocalLog mLocalLog;
    private final IpConnectivityLog mMetricsLog;
    private final MessageHandlingLogger mMsgStateLogger;
    private boolean mMulticastFiltering;
    private final NetlinkTracker mNetlinkTracker;
    private NetworkInterface mNetworkInterface;
    private final INetworkManagementService mNwService;
    private DhcpResults mPastSuccessedDhcpResult;
    private final WakeupMessage mProvisioningTimeoutAlarm;
    private boolean mRomupdateDhcp6Client;
    private final State mRunningState;
    private long mStartTimeMillis;
    private final State mStartedState;
    private final State mStoppedState;
    private final State mStoppingState;
    private final String mTag;
    private String mTcpBufferSizes;

    public static class Callback {
        public void onPreDhcpAction() {
        }

        public void onPostDhcpAction() {
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
        }

        public void onProvisioningFailure(LinkProperties newLp) {
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
        }

        public void onReachabilityLost(String logMsg) {
        }

        public void onQuit() {
        }

        public void installPacketFilter(byte[] filter) {
        }

        public void setFallbackMulticastFilter(boolean enabled) {
        }

        public void setNeighborDiscoveryOffload(boolean enable) {
        }
    }

    private class LoggingCallbackWrapper extends Callback {
        private static final String PREFIX = "INVOKE ";
        private Callback mCallback;

        public LoggingCallbackWrapper(Callback callback) {
            this.mCallback = callback;
        }

        private void log(String msg) {
            IpManager.this.mLocalLog.log(PREFIX + msg);
        }

        public void onPreDhcpAction() {
            this.mCallback.onPreDhcpAction();
            log("onPreDhcpAction()");
        }

        public void onPostDhcpAction() {
            this.mCallback.onPostDhcpAction();
            log("onPostDhcpAction()");
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            this.mCallback.onNewDhcpResults(dhcpResults);
            log("onNewDhcpResults({" + dhcpResults + "})");
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            this.mCallback.onProvisioningSuccess(newLp);
            log("onProvisioningSuccess({" + newLp + "})");
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            this.mCallback.onProvisioningFailure(newLp);
            log("onProvisioningFailure({" + newLp + "})");
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            this.mCallback.onLinkPropertiesChange(newLp);
            log("onLinkPropertiesChange({" + newLp + "})");
        }

        public void onReachabilityLost(String logMsg) {
            this.mCallback.onReachabilityLost(logMsg);
            log("onReachabilityLost(" + logMsg + ")");
        }

        public void onQuit() {
            this.mCallback.onQuit();
            log("onQuit()");
        }

        public void installPacketFilter(byte[] filter) {
            this.mCallback.installPacketFilter(filter);
            log("installPacketFilter(byte[" + filter.length + "])");
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            this.mCallback.setFallbackMulticastFilter(enabled);
            log("setFallbackMulticastFilter(" + enabled + ")");
        }

        public void setNeighborDiscoveryOffload(boolean enable) {
            this.mCallback.setNeighborDiscoveryOffload(enable);
            log("setNeighborDiscoveryOffload(" + enable + ")");
        }
    }

    private static class MessageHandlingLogger {
        public String processedInState;
        public String receivedInState;

        /* synthetic */ MessageHandlingLogger(MessageHandlingLogger messageHandlingLogger) {
            this();
        }

        private MessageHandlingLogger() {
        }

        public void reset() {
            this.processedInState = null;
            this.receivedInState = null;
        }

        public void handled(State processedIn, IState receivedIn) {
            this.processedInState = processedIn.getClass().getSimpleName();
            this.receivedInState = receivedIn.getName();
        }

        public String toString() {
            Object[] objArr = new Object[2];
            objArr[0] = this.receivedInState;
            objArr[1] = this.processedInState;
            return String.format("rcvd_in=%s, proc_in=%s", objArr);
        }
    }

    public static class ProvisioningConfiguration {
        private static final int DEFAULT_TIMEOUT_MS = 36000;
        ApfCapabilities mApfCapabilities;
        boolean mEnableIPv4 = true;
        boolean mEnableIPv6 = true;
        int mProvisioningTimeoutMs = DEFAULT_TIMEOUT_MS;
        int mRequestedPreDhcpActionMs;
        StaticIpConfiguration mStaticIpConfig;
        boolean mUsingIpReachabilityMonitor = true;

        public static class Builder {
            private ProvisioningConfiguration mConfig = new ProvisioningConfiguration();

            public Builder withoutIPv4() {
                this.mConfig.mEnableIPv4 = false;
                return this;
            }

            public Builder withoutIPv6() {
                this.mConfig.mEnableIPv6 = false;
                return this;
            }

            public Builder withoutIpReachabilityMonitor() {
                this.mConfig.mUsingIpReachabilityMonitor = false;
                return this;
            }

            public Builder withPreDhcpAction() {
                this.mConfig.mRequestedPreDhcpActionMs = ProvisioningConfiguration.DEFAULT_TIMEOUT_MS;
                return this;
            }

            public Builder withPreDhcpAction(int dhcpActionTimeoutMs) {
                this.mConfig.mRequestedPreDhcpActionMs = dhcpActionTimeoutMs;
                return this;
            }

            public Builder withStaticConfiguration(StaticIpConfiguration staticConfig) {
                this.mConfig.mStaticIpConfig = staticConfig;
                return this;
            }

            public Builder withApfCapabilities(ApfCapabilities apfCapabilities) {
                this.mConfig.mApfCapabilities = apfCapabilities;
                return this;
            }

            public Builder withProvisioningTimeoutMs(int timeoutMs) {
                this.mConfig.mProvisioningTimeoutMs = timeoutMs;
                return this;
            }

            public ProvisioningConfiguration build() {
                return new ProvisioningConfiguration(this.mConfig);
            }
        }

        public ProvisioningConfiguration(ProvisioningConfiguration other) {
            this.mEnableIPv4 = other.mEnableIPv4;
            this.mEnableIPv6 = other.mEnableIPv6;
            this.mUsingIpReachabilityMonitor = other.mUsingIpReachabilityMonitor;
            this.mRequestedPreDhcpActionMs = other.mRequestedPreDhcpActionMs;
            this.mStaticIpConfig = other.mStaticIpConfig;
            this.mApfCapabilities = other.mApfCapabilities;
            this.mProvisioningTimeoutMs = other.mProvisioningTimeoutMs;
        }

        public String toString() {
            return new StringJoiner(", ", getClass().getSimpleName() + "{", "}").add("mEnableIPv4: " + this.mEnableIPv4).add("mEnableIPv6: " + this.mEnableIPv6).add("mUsingIpReachabilityMonitor: " + this.mUsingIpReachabilityMonitor).add("mRequestedPreDhcpActionMs: " + this.mRequestedPreDhcpActionMs).add("mStaticIpConfig: " + this.mStaticIpConfig).add("mApfCapabilities: " + this.mApfCapabilities).add("mProvisioningTimeoutMs: " + this.mProvisioningTimeoutMs).toString();
        }
    }

    class RunningState extends State {
        private boolean mDhcpActionInFlight;

        RunningState() {
        }

        public void enter() {
            IpManager.this.mApfFilter = ApfFilter.maybeCreate(IpManager.this.mConfiguration.mApfCapabilities, IpManager.this.mNetworkInterface, IpManager.this.mCallback, IpManager.this.mMulticastFiltering);
            if (IpManager.this.mApfFilter == null) {
                IpManager.this.mCallback.setFallbackMulticastFilter(IpManager.this.mMulticastFiltering);
            }
            if (IpManager.this.mConfiguration.mEnableIPv6) {
                IpManager.this.startIPv6();
            }
            if (!IpManager.this.mConfiguration.mEnableIPv4 || IpManager.this.startIPv4()) {
                if (IpManager.this.mConfiguration.mUsingIpReachabilityMonitor) {
                    try {
                        IpManager.this.mIpReachabilityMonitor = new IpReachabilityMonitor(IpManager.this.mContext, IpManager.this.mInterfaceName, new android.net.ip.IpReachabilityMonitor.Callback() {
                            public void notifyLost(InetAddress ip, String logMsg) {
                                IpManager.this.mCallback.onReachabilityLost(logMsg);
                            }
                        }, IpManager.this.mAvoidBadWifiTracker);
                    } catch (IllegalArgumentException e) {
                        Log.e(IpManager.this.mTag, "star IRM fail: " + e);
                        IpManager.this.transitionTo(IpManager.this.mStoppingState);
                    }
                }
                if (!(IpManager.this.mConfiguration.mStaticIpConfig == null || !IpManager.this.mHandleGateConflict || IpManager.this.mDhcpResults == null || IpManager.this.mDhcpResults.gateway == null || IpManager.this.mIpReachabilityMonitor == null)) {
                    IpManager.this.mIpReachabilityMonitor.probeGateway((Inet4Address) IpManager.this.mDhcpResults.gateway, IpManager.this.mInterfaceName);
                }
                return;
            }
            IpManager.this.transitionTo(IpManager.this.mStoppingState);
        }

        public void exit() {
            stopDhcpAction();
            if (IpManager.this.mIpReachabilityMonitor != null) {
                IpManager.this.mIpReachabilityMonitor.stop();
                IpManager.this.mIpReachabilityMonitor = null;
            }
            IpManager.this.checkActiveImsPdn();
            if (IpManager.this.mDhcpClient != null) {
                IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_STOP_DHCP);
                IpManager.this.mDhcpClient.doQuit();
            }
            if (IpManager.this.isDhcp6Support(IpManager.this.mDhcp6Client)) {
                IpManager.this.mDhcp6Client.sendMessage(Dhcp6Client.CMD_STOP_DHCP);
                IpManager.this.mDhcp6Client.doQuit();
            }
            if (IpManager.this.mApfFilter != null) {
                IpManager.this.mApfFilter.shutdown();
                IpManager.this.mApfFilter = null;
            }
            IpManager.this.resetLinkProperties();
        }

        private void ensureDhcpAction() {
            if (!this.mDhcpActionInFlight) {
                IpManager.this.mCallback.onPreDhcpAction();
                this.mDhcpActionInFlight = true;
                IpManager.this.mDhcpActionTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) IpManager.this.mConfiguration.mRequestedPreDhcpActionMs));
            }
        }

        private void stopDhcpAction() {
            IpManager.this.mDhcpActionTimeoutAlarm.cancel();
            if (this.mDhcpActionInFlight) {
                IpManager.this.mCallback.onPostDhcpAction();
                this.mDhcpActionInFlight = false;
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    IpManager.this.transitionTo(IpManager.this.mStoppingState);
                    break;
                case 2:
                    if (IpManager.this.mDhcpClient != null) {
                        IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_RENEW_AFTER_ROAMING);
                        break;
                    }
                    break;
                case 3:
                    if (IpManager.this.mIpReachabilityMonitor != null) {
                        IpManager.this.mIpReachabilityMonitor.probeAll();
                        break;
                    }
                    break;
                case 4:
                    if (IpManager.this.mDhcpClient != null) {
                        if (msg.obj != null) {
                            IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE, msg.obj);
                        } else {
                            IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE);
                        }
                    }
                    if (IpManager.this.isDhcp6Support(IpManager.this.mDhcp6Client)) {
                        IpManager.this.mDhcp6Client.sendMessage(Dhcp6Client.CMD_PRE_DHCP_ACTION_COMPLETE);
                        break;
                    }
                    break;
                case 5:
                    if (!IpManager.this.handleLinkPropertiesUpdate(true)) {
                        IpManager.this.transitionTo(IpManager.this.mStoppingState);
                        break;
                    }
                    break;
                case 6:
                    IpManager.this.mTcpBufferSizes = (String) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(true);
                    break;
                case 7:
                    IpManager.this.mHttpProxy = (ProxyInfo) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(true);
                    break;
                case 8:
                    IpManager.this.mMulticastFiltering = ((Boolean) msg.obj).booleanValue();
                    if (IpManager.this.mApfFilter == null) {
                        IpManager.this.mCallback.setFallbackMulticastFilter(IpManager.this.mMulticastFiltering);
                        break;
                    }
                    IpManager.this.mApfFilter.setMulticastFilter(IpManager.this.mMulticastFiltering);
                    break;
                case 10:
                    stopDhcpAction();
                    break;
                case DhcpClient.CMD_PRE_DHCP_ACTION /*196611*/:
                    if (IpManager.this.mConfiguration.mRequestedPreDhcpActionMs <= 0) {
                        IpManager.this.sendMessage(4);
                        break;
                    }
                    ensureDhcpAction();
                    break;
                case DhcpClient.CMD_POST_DHCP_ACTION /*196612*/:
                    stopDhcpAction();
                    switch (msg.arg1) {
                        case 1:
                            DhcpResults newDhcpResult = msg.obj;
                            if (IpManager.this.mDhcpResults != null && IpManager.this.mDhcpResults.ipAddress != null && !IpManager.this.mDhcpResults.ipAddress.equals(newDhcpResult.ipAddress)) {
                                IpManager.this.setDifferentIPStatics(IpManager.this.mDhcpResults, newDhcpResult);
                                IpManager.this.handleIPv4Failure();
                                break;
                            }
                            IpManager.this.handleIPv4Success((DhcpResults) msg.obj);
                            break;
                            break;
                        case 2:
                            IpManager.this.handleIPv4Failure();
                            break;
                        default:
                            Log.e(IpManager.this.mTag, "Unknown CMD_POST_DHCP_ACTION status:" + msg.arg1);
                            break;
                    }
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    Log.e(IpManager.this.mTag, "Unexpected CMD_ON_QUIT.");
                    IpManager.this.mDhcpClient = null;
                    break;
                case DhcpClient.CMD_CLEAR_LINKADDRESS /*196615*/:
                    if (IpManager.this.mConfiguration.mStaticIpConfig == null) {
                        IpManager.this.clearIPv4Address();
                        break;
                    }
                    Log.e(IpManager.this.mTag, "static Ip is configured, ignore clearIPv4Address");
                    return true;
                case DhcpClient.CMD_CONFIGURE_LINKADDRESS /*196616*/:
                    LinkAddress ipAddress = msg.obj;
                    InterfaceConfiguration orginIfcg = null;
                    try {
                        orginIfcg = IpManager.this.mNwService.getInterfaceConfig(IpManager.this.mInterfaceName);
                    } catch (RemoteException e) {
                        Log.d(IpManager.this.mTag, "fail to get interfaceConfig");
                    }
                    if (orginIfcg != null) {
                        LinkAddress orginalAddress = orginIfcg.getLinkAddress();
                        if (orginalAddress != null && orginalAddress.equals(ipAddress)) {
                            Log.d(IpManager.this.mTag, "configAddress  address " + ipAddress + " same as old one");
                            IpManager.this.mCallback.onProvisioningSuccess(IpManager.this.assembleLinkProperties());
                            IpManager.this.mDhcpClient.sendMessage(DhcpClient.EVENT_LINKADDRESS_CONFIGURED);
                            break;
                        }
                    }
                    if (!IpManager.this.setIPv4Address(ipAddress)) {
                        Log.e(IpManager.this.mTag, "Failed to set IPv4 address!");
                        IpManager.this.dispatchCallback(ProvisioningChange.LOST_PROVISIONING, new LinkProperties(IpManager.this.mLinkProperties));
                        IpManager.this.transitionTo(IpManager.this.mStoppingState);
                        break;
                    }
                    IpManager.this.mDhcpClient.sendMessage(DhcpClient.EVENT_LINKADDRESS_CONFIGURED);
                    break;
                case Dhcp6Client.CMD_ON_QUIT /*196813*/:
                    Log.e(IpManager.this.mTag, "Unexpected v6 CMD_ON_QUIT.");
                    IpManager.this.mDhcp6Client = null;
                    IpManager.this.mDnsV6Servers = null;
                    break;
                case Dhcp6Client.CMD_CONFIGURE_DNSV6 /*196816*/:
                    IpManager.this.mDnsV6Servers = (ArrayList) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(true);
                    break;
                default:
                    return false;
            }
            IpManager.this.mMsgStateLogger.handled(this, IpManager.this.getCurrentState());
            return true;
        }
    }

    class StartedState extends State {
        StartedState() {
        }

        public void enter() {
            Log.d(IpManager.this.mTag, "[StartedState] Enter");
            IpManager.this.mStartTimeMillis = SystemClock.elapsedRealtime();
            if (IpManager.this.mConfiguration.mProvisioningTimeoutMs > 0) {
                IpManager.this.mProvisioningTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) IpManager.this.mConfiguration.mProvisioningTimeoutMs));
            }
            if (readyToProceed()) {
                IpManager.this.transitionTo(IpManager.this.mRunningState);
            } else {
                IpManager.this.stopAllIP();
            }
        }

        public void exit() {
            IpManager.this.mProvisioningTimeoutAlarm.cancel();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    IpManager.this.transitionTo(IpManager.this.mStoppingState);
                    break;
                case 5:
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    if (readyToProceed()) {
                        IpManager.this.transitionTo(IpManager.this.mRunningState);
                        break;
                    }
                    break;
                case 9:
                    IpManager.this.handleProvisioningFailure();
                    break;
                default:
                    IpManager.this.deferMessage(msg);
                    break;
            }
            IpManager.this.mMsgStateLogger.handled(this, IpManager.this.getCurrentState());
            return true;
        }

        boolean readyToProceed() {
            if (IpManager.this.mLinkProperties.hasIPv4Address() || IpManager.this.mLinkProperties.hasGlobalIPv6Address()) {
                return false;
            }
            return true;
        }
    }

    class StoppedState extends State {
        StoppedState() {
        }

        public void enter() {
            Log.d(IpManager.this.mTag, "[StoppedState] Enter");
            IpManager.this.stopAllIP();
            IpManager.this.resetLinkProperties();
            Log.d(IpManager.this.mTag, "resetLinkProperties");
            if (IpManager.this.mIsWifiSMStarted) {
                IpManager.this.mCallback.onLinkPropertiesChange(IpManager.this.mLinkProperties);
            }
            if (IpManager.this.mStartTimeMillis > 0) {
                IpManager.this.recordMetric(3);
                IpManager.this.mStartTimeMillis = 0;
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    IpManager.this.mIsWifiSMStarted = true;
                    IpManager.this.mConfiguration = (ProvisioningConfiguration) msg.obj;
                    IpManager.this.transitionTo(IpManager.this.mStartedState);
                    break;
                case 5:
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 6:
                    IpManager.this.mTcpBufferSizes = (String) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 7:
                    IpManager.this.mHttpProxy = (ProxyInfo) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 8:
                    IpManager.this.mMulticastFiltering = ((Boolean) msg.obj).booleanValue();
                    break;
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    Log.e(IpManager.this.mTag, "Unexpected CMD_ON_QUIT (already stopped).");
                    break;
                default:
                    Log.d(IpManager.this.mTag, "StoppedState NOT_HANDLED what = " + msg.what);
                    return false;
            }
            IpManager.this.mMsgStateLogger.handled(this, IpManager.this.getCurrentState());
            return true;
        }
    }

    class StoppingState extends State {
        StoppingState() {
        }

        public void enter() {
            Log.d(IpManager.this.mTag, "[StoppingState] Enter");
            if (IpManager.this.mDhcpClient == null) {
                IpManager.this.transitionTo(IpManager.this.mStoppedState);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    IpManager.this.mDhcpClient = null;
                    IpManager.this.transitionTo(IpManager.this.mStoppedState);
                    break;
                case DhcpClient.CMD_CLEAR_LINKADDRESS /*196615*/:
                    IpManager.this.clearIPv4Address();
                    break;
                default:
                    Log.d(IpManager.this.mTag, "deferMessage what = " + msg.what);
                    IpManager.this.deferMessage(msg);
                    break;
            }
            IpManager.this.mMsgStateLogger.handled(this, IpManager.this.getCurrentState());
            return true;
        }
    }

    public static class WaitForProvisioningCallback extends Callback {
        private LinkProperties mCallbackLinkProperties;

        public LinkProperties waitForProvisioning() {
            LinkProperties linkProperties;
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                linkProperties = this.mCallbackLinkProperties;
            }
            return linkProperties;
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            synchronized (this) {
                this.mCallbackLinkProperties = newLp;
                notify();
            }
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            synchronized (this) {
                this.mCallbackLinkProperties = null;
                notify();
            }
        }
    }

    /* renamed from: -getandroid-net-LinkProperties$ProvisioningChangeSwitchesValues */
    private static /* synthetic */ int[] m2-getandroid-net-LinkProperties$ProvisioningChangeSwitchesValues() {
        if (f1-android-net-LinkProperties$ProvisioningChangeSwitchesValues != null) {
            return f1-android-net-LinkProperties$ProvisioningChangeSwitchesValues;
        }
        int[] iArr = new int[ProvisioningChange.values().length];
        try {
            iArr[ProvisioningChange.GAINED_PROVISIONING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProvisioningChange.LOST_PROVISIONING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProvisioningChange.STILL_NOT_PROVISIONED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProvisioningChange.STILL_PROVISIONED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f1-android-net-LinkProperties$ProvisioningChangeSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.ip.IpManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.ip.IpManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ip.IpManager.<clinit>():void");
    }

    public IpManager(Context context, String ifName, Callback callback) throws IllegalArgumentException {
        this(context, ifName, callback, Stub.asInterface(ServiceManager.getService("network_management")));
    }

    public IpManager(Context context, String ifName, Callback callback, INetworkManagementService nwService) throws IllegalArgumentException {
        super(IpManager.class.getSimpleName() + "." + ifName);
        this.mStoppedState = new StoppedState();
        this.mStoppingState = new StoppingState();
        this.mStartedState = new StartedState();
        this.mRunningState = new RunningState();
        this.mMetricsLog = new IpConnectivityLog();
        this.mIsWifiSMStarted = false;
        this.mIsStartCalled = false;
        this.mHandleGateConflict = false;
        this.mRomupdateDhcp6Client = false;
        this.mTag = getName();
        this.mContext = context;
        this.mInterfaceName = ifName;
        this.mClatInterfaceName = CLAT_PREFIX + ifName;
        this.mCallback = new LoggingCallbackWrapper(callback);
        this.mNwService = nwService;
        this.mNetlinkTracker = new NetlinkTracker(this.mInterfaceName, new com.android.server.net.NetlinkTracker.Callback() {
            public void update() {
                if (IpManager.this.mIsStartCalled) {
                    IpManager.this.sendMessage(5);
                }
            }
        }) {
            public void interfaceAdded(String iface) {
                super.interfaceAdded(iface);
                if (IpManager.this.mClatInterfaceName.equals(iface)) {
                    IpManager.this.mCallback.setNeighborDiscoveryOffload(false);
                }
            }

            public void interfaceRemoved(String iface) {
                super.interfaceRemoved(iface);
                if (IpManager.this.mClatInterfaceName.equals(iface)) {
                    IpManager.this.mCallback.setNeighborDiscoveryOffload(true);
                }
            }
        };
        this.mAvoidBadWifiTracker = new AvoidBadWifiTracker(this.mContext, getHandler());
        resetLinkProperties();
        this.mProvisioningTimeoutAlarm = new WakeupMessage(this.mContext, getHandler(), this.mTag + ".EVENT_PROVISIONING_TIMEOUT", 9);
        this.mDhcpActionTimeoutAlarm = new WakeupMessage(this.mContext, getHandler(), this.mTag + ".EVENT_DHCPACTION_TIMEOUT", 10);
        addState(this.mStoppedState);
        addState(this.mStartedState);
        addState(this.mRunningState, this.mStartedState);
        addState(this.mStoppingState);
        setInitialState(this.mStoppedState);
        this.mLocalLog = new LocalLog(500);
        this.mMsgStateLogger = new MessageHandlingLogger();
        super.start();
        this.mIsStartCalled = true;
        try {
            this.mNwService.registerObserver(this.mNetlinkTracker);
        } catch (RemoteException e) {
            Log.e(this.mTag, "Couldn't register NetlinkTracker: " + e.toString());
        }
    }

    protected void onQuitting() {
        this.mCallback.onQuit();
    }

    public void shutdown() {
        this.mAvoidBadWifiTracker.unregisterIntentReceiver();
        stop();
        quit();
    }

    public static Builder buildProvisioningConfiguration() {
        return new Builder();
    }

    public void enableHandleGatewayConflict() {
        this.mHandleGateConflict = true;
    }

    public void setDhcp6ClientFeature(boolean support) {
        this.mRomupdateDhcp6Client = support;
    }

    public void startProvisioning(ProvisioningConfiguration req) {
        getNetworkInterface();
        if (this.mNetworkInterface == null) {
            Log.e(this.mTag, "Invalid interface " + this.mInterfaceName);
            this.mCallback.onProvisioningFailure(new LinkProperties(this.mLinkProperties));
            return;
        }
        this.mCallback.setNeighborDiscoveryOffload(true);
        sendMessage(2, new ProvisioningConfiguration(req));
    }

    public void startProvisioning(StaticIpConfiguration staticIpConfig) {
        startProvisioning(buildProvisioningConfiguration().withStaticConfiguration(staticIpConfig).build());
    }

    public void startProvisioning() {
        startProvisioning(new ProvisioningConfiguration());
    }

    public void stop() {
        sendMessage(1);
    }

    public void confirmConfiguration() {
        sendMessage(3);
    }

    public void completedPreDhcpAction() {
        sendMessage(4);
    }

    public void completedPreDhcpAction(WifiConfiguration wifiConfig) {
        sendMessage(4, wifiConfig);
    }

    public void setTcpBufferSizes(String tcpBufferSizes) {
        sendMessage(6, tcpBufferSizes);
    }

    public void setHttpProxy(ProxyInfo proxyInfo) {
        sendMessage(7, proxyInfo);
    }

    public void setMulticastFilter(boolean enabled) {
        sendMessage(8, Boolean.valueOf(enabled));
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (args.length <= 0 || !DUMP_ARG_CONFIRM.equals(args[0])) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            pw.println("APF dump:");
            pw.increaseIndent();
            ApfFilter apfFilter = this.mApfFilter;
            if (apfFilter != null) {
                apfFilter.dump(pw);
            } else {
                pw.println("No apf support");
            }
            pw.decreaseIndent();
            pw.println();
            pw.println(this.mTag + " StateMachine dump:");
            pw.increaseIndent();
            this.mLocalLog.readOnlyLocalLog().dump(fd, pw, args);
            pw.decreaseIndent();
            return;
        }
        confirmConfiguration();
    }

    protected String getWhatToString(int what) {
        return (String) sWhatToString.get(what, "UNKNOWN: " + Integer.toString(what));
    }

    protected String getLogRecString(Message msg) {
        String str = "%s/%d %d %d %s [%s]";
        Object[] objArr = new Object[6];
        objArr[0] = this.mInterfaceName;
        objArr[1] = Integer.valueOf(this.mNetworkInterface == null ? -1 : this.mNetworkInterface.getIndex());
        objArr[2] = Integer.valueOf(msg.arg1);
        objArr[3] = Integer.valueOf(msg.arg2);
        objArr[4] = Objects.toString(msg.obj);
        objArr[5] = this.mMsgStateLogger;
        String logLine = String.format(str, objArr);
        String richerLogLine = getWhatToString(msg.what) + " " + logLine;
        this.mLocalLog.log(richerLogLine);
        Log.d(this.mTag, richerLogLine);
        this.mMsgStateLogger.reset();
        return logLine;
    }

    protected boolean recordLogRec(Message msg) {
        boolean shouldLog = msg.what != 5;
        if (!shouldLog) {
            this.mMsgStateLogger.reset();
        }
        return shouldLog;
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x0009 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x0009, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            android.util.Log.e(r3.mTag, "ALERT: Failed to get interface object: ", r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getNetworkInterface() {
        try {
            this.mNetworkInterface = NetworkInterface.getByName(this.mInterfaceName);
        } catch (Exception e) {
        }
    }

    private void resetLinkProperties() {
        this.mNetlinkTracker.clearLinkProperties();
        this.mConfiguration = null;
        this.mDhcpResults = null;
        this.mTcpBufferSizes = IElsaManager.EMPTY_PACKAGE;
        this.mHttpProxy = null;
        this.mDnsV6Servers = null;
        this.mLinkProperties = new LinkProperties();
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
    }

    private void recordMetric(int type) {
        if (this.mStartTimeMillis <= 0) {
            Log.e(this.mTag, "Start time undefined!");
        }
        this.mMetricsLog.log(new IpManagerEvent(this.mInterfaceName, type, SystemClock.elapsedRealtime() - this.mStartTimeMillis));
    }

    private static boolean isProvisioned(LinkProperties lp) {
        return !lp.isProvisioned() ? lp.hasIPv4Address() : true;
    }

    private ProvisioningChange compareProvisioning(LinkProperties oldLp, LinkProperties newLp) {
        ProvisioningChange delta;
        boolean wasProvisioned = isProvisioned(oldLp);
        boolean isProvisioned = isProvisioned(newLp);
        if (!wasProvisioned && isProvisioned) {
            delta = ProvisioningChange.GAINED_PROVISIONING;
        } else if (wasProvisioned && isProvisioned) {
            delta = ProvisioningChange.STILL_PROVISIONED;
        } else if (wasProvisioned || isProvisioned) {
            delta = ProvisioningChange.LOST_PROVISIONING;
        } else {
            delta = ProvisioningChange.STILL_NOT_PROVISIONED;
        }
        Log.d("IpManager", "compareProvisioning: " + delta);
        boolean lostIPv6 = oldLp.isIPv6Provisioned() && !newLp.isIPv6Provisioned();
        boolean lostIPv4Address = oldLp.hasIPv4Address() && !newLp.hasIPv4Address();
        boolean lostIPv6Router;
        if (!oldLp.hasIPv6DefaultRoute() || newLp.hasIPv6DefaultRoute()) {
            lostIPv6Router = false;
        } else {
            lostIPv6Router = true;
        }
        boolean ignoreIPv6ProvisioningLoss;
        if (this.mAvoidBadWifiTracker.currentValue()) {
            ignoreIPv6ProvisioningLoss = false;
        } else {
            ignoreIPv6ProvisioningLoss = true;
        }
        if (lostIPv4Address || (lostIPv6 && !ignoreIPv6ProvisioningLoss)) {
            delta = ProvisioningChange.LOST_PROVISIONING;
            Log.d("IpManager", "compareProvisioning: " + delta + " due to hasIPv4Address/isIPv6Provisioned lost");
        }
        if (!oldLp.hasGlobalIPv6Address() || !lostIPv6Router || ignoreIPv6ProvisioningLoss) {
            return delta;
        }
        delta = ProvisioningChange.LOST_PROVISIONING;
        Log.d("IpManager", "compareProvisioning: " + delta + " due to IPv6DefaultRoute lost");
        return delta;
    }

    private void dispatchCallback(ProvisioningChange delta, LinkProperties newLp) {
        switch (m2-getandroid-net-LinkProperties$ProvisioningChangeSwitchesValues()[delta.ordinal()]) {
            case 1:
                Log.d(this.mTag, "onProvisioningSuccess()");
                recordMetric(1);
                this.mCallback.onProvisioningSuccess(newLp);
                return;
            case 2:
                Log.d(this.mTag, "onProvisioningFailure()");
                recordMetric(2);
                this.mCallback.onProvisioningFailure(newLp);
                return;
            default:
                Log.d(this.mTag, "onLinkPropertiesChange()");
                this.mCallback.onLinkPropertiesChange(newLp);
                return;
        }
    }

    private ProvisioningChange setLinkProperties(LinkProperties newLp) {
        if (SDBG) {
            Log.d(this.mTag, "setLinkProperties newLp = " + newLp);
        }
        if (this.mApfFilter != null) {
            this.mApfFilter.setLinkProperties(newLp);
        }
        if (this.mIpReachabilityMonitor != null) {
            this.mIpReachabilityMonitor.updateLinkProperties(newLp);
        }
        ProvisioningChange delta = compareProvisioning(this.mLinkProperties, newLp);
        this.mLinkProperties = new LinkProperties(newLp);
        if (delta == ProvisioningChange.GAINED_PROVISIONING) {
            this.mProvisioningTimeoutAlarm.cancel();
        }
        return delta;
    }

    private boolean linkPropertiesUnchanged(LinkProperties newLp) {
        return Objects.equals(newLp, this.mLinkProperties);
    }

    private LinkProperties assembleLinkProperties() {
        LinkProperties newLp = new LinkProperties();
        newLp.setInterfaceName(this.mInterfaceName);
        LinkProperties netlinkLinkProperties = this.mNetlinkTracker.getLinkProperties();
        newLp.setLinkAddresses(netlinkLinkProperties.getLinkAddresses());
        for (RouteInfo route : netlinkLinkProperties.getRoutes()) {
            newLp.addRoute(route);
        }
        for (InetAddress dns : netlinkLinkProperties.getDnsServers()) {
            if (newLp.isReachable(dns)) {
                newLp.addDnsServer(dns);
            }
        }
        if (this.mDhcpResults != null) {
            for (RouteInfo route2 : this.mDhcpResults.getRoutes(this.mInterfaceName)) {
                newLp.addRoute(route2);
            }
            for (InetAddress dns2 : this.mDhcpResults.dnsServers) {
                newLp.addDnsServer(dns2);
            }
            newLp.setDomains(this.mDhcpResults.domains);
            if (this.mDhcpResults.mtu != 0) {
                newLp.setMtu(this.mDhcpResults.mtu);
            }
        }
        if (!TextUtils.isEmpty(this.mTcpBufferSizes)) {
            newLp.setTcpBufferSizes(this.mTcpBufferSizes);
        }
        if (this.mHttpProxy != null) {
            newLp.setHttpProxy(this.mHttpProxy);
        }
        if (this.mDnsV6Servers != null) {
            for (InetAddress dnsV6 : this.mDnsV6Servers) {
                if (newLp.isReachable(dnsV6)) {
                    newLp.addDnsServer(dnsV6);
                }
            }
        }
        Log.d(this.mTag, "newLp{" + newLp + "}");
        return newLp;
    }

    private boolean handleLinkPropertiesUpdate(boolean sendCallbacks) {
        boolean z = true;
        LinkProperties newLp = assembleLinkProperties();
        if (linkPropertiesUnchanged(newLp)) {
            Log.d(this.mTag, "linkPropertiesUnchanged");
            return true;
        }
        ProvisioningChange delta = setLinkProperties(newLp);
        Log.d(this.mTag, "handleLinkPropertiesUpdate delta = " + delta);
        if (sendCallbacks) {
            dispatchCallback(delta, newLp);
        }
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0019 A:{Splitter: B:1:0x0008, ExcHandler: java.lang.IllegalStateException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0019, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x001a, code:
            android.util.Log.e(r4.mTag, "IPv4 configuration failed: ", r0);
     */
    /* JADX WARNING: Missing block: B:7:0x0023, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setIPv4Address(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            this.mNwService.setInterfaceConfig(this.mInterfaceName, ifcg);
            Log.d(this.mTag, "IPv4 configuration succeeded");
            return true;
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x0018 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.IllegalStateException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x0018, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x0019, code:
            android.util.Log.e(r5.mTag, "ALERT: Failed to clear IPv4 address on interface " + r5.mInterfaceName, r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void clearIPv4Address() {
        try {
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress("0.0.0.0/0"));
            this.mNwService.setInterfaceConfig(this.mInterfaceName, ifcg);
        } catch (Exception e) {
        }
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        this.mDhcpResults = new DhcpResults(dhcpResults);
        LinkProperties newLp = assembleLinkProperties();
        ProvisioningChange delta = setLinkProperties(newLp);
        Log.d(this.mTag, "handleIPv4Success delta = " + delta);
        Log.d(this.mTag, "onNewDhcpResults(" + Objects.toString(dhcpResults) + ")");
        if (!(!this.mHandleGateConflict || this.mDhcpResults.gateway == null || this.mIpReachabilityMonitor == null)) {
            this.mIpReachabilityMonitor.probeGateway((Inet4Address) this.mDhcpResults.gateway, newLp.getInterfaceName());
        }
        this.mCallback.onNewDhcpResults(dhcpResults);
        dispatchCallback(delta, newLp);
    }

    private void handleIPv4Failure() {
        clearIPv4Address();
        this.mDhcpResults = null;
        Log.d(this.mTag, "onNewDhcpResults(null)");
        this.mCallback.onNewDhcpResults(null);
        handleProvisioningFailure();
    }

    private void handleProvisioningFailure() {
        LinkProperties newLp = assembleLinkProperties();
        ProvisioningChange delta = setLinkProperties(newLp);
        if (delta == ProvisioningChange.STILL_NOT_PROVISIONED) {
            delta = ProvisioningChange.LOST_PROVISIONING;
        }
        dispatchCallback(delta, newLp);
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            transitionTo(this.mStoppingState);
        }
    }

    private boolean startIPv4() {
        if (this.mConfiguration.mStaticIpConfig == null) {
            this.mDhcpClient = DhcpClient.makeDhcpClient(this.mContext, this, this.mInterfaceName);
            this.mDhcpClient.registerForPreDhcpNotification();
            this.mDhcpClient.sendMessage(DhcpClient.CMD_START_DHCP, this.mPastSuccessedDhcpResult);
            if (sMtkDhcpv6cWifi && this.mRomupdateDhcp6Client) {
                this.mDhcp6Client = Dhcp6Client.makeDhcp6Client(this.mContext, this, this.mInterfaceName);
                this.mDhcp6Client.registerForPreDhcpNotification();
                this.mDhcp6Client.sendMessage(Dhcp6Client.CMD_START_DHCP);
            }
        } else if (setIPv4Address(this.mConfiguration.mStaticIpConfig.ipAddress)) {
            handleIPv4Success(new DhcpResults(this.mConfiguration.mStaticIpConfig));
        } else {
            Log.d(this.mTag, "onProvisioningFailure()");
            recordMetric(2);
            this.mCallback.onProvisioningFailure(new LinkProperties(this.mLinkProperties));
            return false;
        }
        return true;
    }

    private boolean isDhcp6Support(Dhcp6Client client) {
        boolean z = false;
        if (!sMtkDhcpv6cWifi || !this.mRomupdateDhcp6Client) {
            return false;
        }
        if (client != null) {
            z = true;
        }
        return z;
    }

    private boolean startIPv6() {
        try {
            this.mNwService.setInterfaceIpv6PrivacyExtensions(this.mInterfaceName, true);
            this.mNwService.enableIpv6(this.mInterfaceName);
            return true;
        } catch (RemoteException re) {
            Log.e(this.mTag, "Unable to change interface settings: " + re);
            return false;
        } catch (IllegalStateException ie) {
            Log.e(this.mTag, "Unable to change interface settings: " + ie);
            return false;
        }
    }

    private void stopAllIP() {
        try {
            this.mNwService.disableIpv6(this.mInterfaceName);
        } catch (Exception e) {
            Log.e(this.mTag, "Failed to disable IPv6" + e);
        }
        try {
            this.mNwService.clearInterfaceAddresses(this.mInterfaceName);
        } catch (Exception e2) {
            Log.e(this.mTag, "Failed to clear addresses " + e2);
        }
    }

    private void setDifferentIPStatics(DhcpResults oldDhcpResults, DhcpResults newDchpResults) {
        HashMap<String, String> map = new HashMap();
        if (oldDhcpResults.gateway != null) {
            map.put("oldGatway", oldDhcpResults.gateway.toString());
        }
        if (newDchpResults.gateway != null) {
            map.put("newGatway", newDchpResults.gateway.toString());
        }
        if (oldDhcpResults.ipAddress != null) {
            map.put("oldIp", oldDhcpResults.ipAddress.toString());
        }
        if (newDchpResults.ipAddress != null) {
            map.put("newIp", newDchpResults.ipAddress.toString());
        }
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_DIFFERENT_IP, map, false);
    }

    public void updatePastSuccessedDhcpResult(DhcpResults result) {
        this.mPastSuccessedDhcpResult = result;
    }

    private void checkActiveImsPdn() {
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Network[] allNetworks = connMgr.getAllNetworks();
        int i = 0;
        int length = allNetworks.length;
        while (i < length) {
            Network network = allNetworks[i];
            NetworkCapabilities nc = connMgr.getNetworkCapabilities(network);
            if (nc == null || !nc.hasCapability(4)) {
                i++;
            } else {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                if (networkInfo != null && "IWLAN".equals(networkInfo.getSubtypeName())) {
                    log("Wait for IWLAN teardown");
                    try {
                        Thread.sleep(600);
                        return;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                return;
            }
        }
    }
}
