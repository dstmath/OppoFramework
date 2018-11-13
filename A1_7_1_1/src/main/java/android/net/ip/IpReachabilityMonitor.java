package android.net.ip;

import android.content.Context;
import android.net.LinkProperties;
import android.net.LinkProperties.ProvisioningChange;
import android.net.RouteInfo;
import android.net.arp.ArpPeer;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.IpReachabilityEvent;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.net.util.AvoidBadWifiTracker;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
public class IpReachabilityMonitor {
    private static final boolean DBG = true;
    private static final boolean SDBG = false;
    private static final String TAG = "IpReachabilityMonitor";
    private static final boolean VDBG = true;
    private static Context mContext;
    private static short sArpNudState;
    private ArpPeer mArpPeer;
    private final AvoidBadWifiTracker mAvoidBadWifiTracker;
    private final Callback mCallback;
    private final int mInterfaceIndex;
    private final String mInterfaceName;
    @GuardedBy("mLock")
    private Map<InetAddress, Short> mIpWatchList;
    @GuardedBy("mLock")
    private int mIpWatchListVersion;
    private volatile long mLastProbeTimeMs;
    @GuardedBy("mLock")
    private LinkProperties mLinkProperties;
    private final Object mLock;
    private final IpConnectivityLog mMetricsLog;
    private final NetlinkSocketObserver mNetlinkSocketObserver;
    private final Thread mObserverThread;
    @GuardedBy("mLock")
    private boolean mRunning;
    @GuardedBy("mLock")
    private boolean mStopped;
    private final WakeLock mWakeLock;

    public interface Callback {
        void notifyLost(InetAddress inetAddress, String str);
    }

    private final class NetlinkSocketObserver implements Runnable {
        private NetlinkSocket mSocket;

        /* synthetic */ NetlinkSocketObserver(IpReachabilityMonitor this$0, NetlinkSocketObserver netlinkSocketObserver) {
            this();
        }

        private NetlinkSocketObserver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:21:0x0048 A:{Splitter: B:5:0x0017, ExcHandler: android.system.ErrnoException (r2_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0048 A:{Splitter: B:5:0x0017, ExcHandler: android.system.ErrnoException (r2_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:21:0x0048, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:22:0x0049, code:
            android.util.Log.e(android.net.ip.IpReachabilityMonitor.TAG, "Failed to suitably initialize a netlink socket", r2);
     */
        /* JADX WARNING: Missing block: B:23:0x0058, code:
            monitor-enter(android.net.ip.IpReachabilityMonitor.-get2(r8.this$0));
     */
        /* JADX WARNING: Missing block: B:25:?, code:
            android.net.ip.IpReachabilityMonitor.-set0(r8.this$0, false);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Log.d(IpReachabilityMonitor.TAG, "Starting observing thread.");
            synchronized (IpReachabilityMonitor.this.mLock) {
                IpReachabilityMonitor.this.mRunning = true;
            }
            try {
                setupNetlinkSocket();
            } catch (Exception e) {
            }
            while (IpReachabilityMonitor.this.stillRunning() && !IpReachabilityMonitor.this.isMonitorStopped()) {
                try {
                    ByteBuffer byteBuffer = recvKernelReply();
                    long whenMs = SystemClock.elapsedRealtime();
                    if (byteBuffer != null) {
                        parseNetlinkMessageBuffer(byteBuffer, whenMs);
                    }
                } catch (ErrnoException e2) {
                    if (IpReachabilityMonitor.this.stillRunning()) {
                        Log.w(IpReachabilityMonitor.TAG, "ErrnoException: ", e2);
                    }
                }
            }
            clearNetlinkSocket();
            synchronized (IpReachabilityMonitor.this.mLock) {
                IpReachabilityMonitor.this.mRunning = false;
            }
            Log.d(IpReachabilityMonitor.TAG, "Finishing observing thread.");
        }

        private void clearNetlinkSocket() {
            if (this.mSocket != null) {
                this.mSocket.close();
            }
        }

        private void setupNetlinkSocket() throws ErrnoException, SocketException {
            clearNetlinkSocket();
            this.mSocket = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
            this.mSocket.bind(new NetlinkSocketAddress(0, OsConstants.RTMGRP_NEIGH));
            NetlinkSocketAddress nlAddr = this.mSocket.getLocalAddress();
            Log.d(IpReachabilityMonitor.TAG, "bound to sockaddr_nl{" + ((long) (nlAddr.getPortId() & -1)) + ", " + nlAddr.getGroupsMask() + "}");
        }

        private ByteBuffer recvKernelReply() throws ErrnoException {
            try {
                return this.mSocket.recvMessage(0);
            } catch (InterruptedIOException e) {
            } catch (ErrnoException e2) {
                if (e2.errno != OsConstants.EAGAIN) {
                    throw e2;
                }
            }
            return null;
        }

        private void parseNetlinkMessageBuffer(ByteBuffer byteBuffer, long whenMs) {
            while (byteBuffer.remaining() > 0) {
                int position = byteBuffer.position();
                NetlinkMessage nlMsg = NetlinkMessage.parse(byteBuffer);
                if (nlMsg == null || nlMsg.getHeader() == null) {
                    byteBuffer.position(position);
                    Log.e(IpReachabilityMonitor.TAG, "unparsable netlink msg: " + NetlinkConstants.hexify(byteBuffer));
                    return;
                }
                int srcPortId = nlMsg.getHeader().nlmsg_pid;
                if (srcPortId != 0) {
                    Log.e(IpReachabilityMonitor.TAG, "non-kernel source portId: " + ((long) (srcPortId & -1)));
                    return;
                } else if (nlMsg instanceof NetlinkErrorMessage) {
                    Log.e(IpReachabilityMonitor.TAG, "netlink error: " + nlMsg);
                } else if (nlMsg instanceof RtNetlinkNeighborMessage) {
                    evaluateRtNetlinkNeighborMessage((RtNetlinkNeighborMessage) nlMsg, whenMs);
                } else {
                    Log.d(IpReachabilityMonitor.TAG, "non-rtnetlink neighbor msg: " + nlMsg);
                }
            }
        }

        private void evaluateRtNetlinkNeighborMessage(RtNetlinkNeighborMessage neighMsg, long whenMs) {
            StructNdMsg ndMsg = neighMsg.getNdHeader();
            if (ndMsg != null && ndMsg.ndm_ifindex == IpReachabilityMonitor.this.mInterfaceIndex) {
                InetAddress destination = neighMsg.getDestination();
                if (IpReachabilityMonitor.this.isWatching(destination)) {
                    short msgType = neighMsg.getHeader().nlmsg_type;
                    short nudState = ndMsg.ndm_state;
                    String eventMsg = "NeighborEvent{elapsedMs=" + whenMs + ", " + destination.getHostAddress() + ", " + "[" + NetlinkConstants.hexify(neighMsg.getLinkLayerAddress()) + "], " + NetlinkConstants.stringForNlMsgType(msgType) + ", " + StructNdMsg.stringForNudState(nudState) + "}";
                    Log.d(IpReachabilityMonitor.TAG, neighMsg.toString());
                    synchronized (IpReachabilityMonitor.this.mLock) {
                        if (IpReachabilityMonitor.this.mIpWatchList.containsKey(destination)) {
                            short value;
                            if (msgType == (short) 29) {
                                value = (short) 0;
                            } else {
                                value = nudState;
                            }
                            IpReachabilityMonitor.this.mIpWatchList.put(destination, Short.valueOf(value));
                        }
                    }
                    if (nudState == (short) 32) {
                        Log.w(IpReachabilityMonitor.TAG, "ALERT: " + eventMsg);
                        IpReachabilityMonitor.this.handleNeighborLost(eventMsg);
                    }
                    synchronized (IpReachabilityMonitor.this.mLock) {
                        if (IpReachabilityMonitor.this.mIpWatchList.containsKey(destination) && (destination instanceof Inet4Address)) {
                            IpReachabilityMonitor.sArpNudState = nudState;
                        }
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.ip.IpReachabilityMonitor.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.ip.IpReachabilityMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ip.IpReachabilityMonitor.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x0138 A:{SYNTHETIC, Splitter: B:40:0x0138} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0194 A:{SYNTHETIC, Splitter: B:58:0x0194} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x013d A:{SYNTHETIC, Splitter: B:43:0x013d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int probeNeighbor(int ifIndex, InetAddress ip) {
        Throwable th;
        Throwable th2;
        String msgSnippet = "probing ip=" + ip.getHostAddress() + "%" + ifIndex;
        Log.d(TAG, msgSnippet);
        byte[] msg = RtNetlinkNeighborMessage.newNewNeighborMessage(1, ip, (short) 16, ifIndex, null);
        int errno = -OsConstants.EPROTO;
        if (sArpNudState == StructNdMsg.NUD_PERMANENT && (ip instanceof Inet4Address)) {
            Log.d(TAG, "Skip probeNeighbor");
            return 0;
        }
        Throwable th3 = null;
        NetlinkSocket nlSocket;
        try {
            nlSocket = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
            try {
                nlSocket.connectToKernel();
                nlSocket.sendMessage(msg, 0, msg.length, 300);
                ByteBuffer bytes = nlSocket.recvMessage(300);
                NetlinkMessage response = NetlinkMessage.parse(bytes);
                if (response == null || !(response instanceof NetlinkErrorMessage) || ((NetlinkErrorMessage) response).getNlMsgError() == null) {
                    String errmsg;
                    if (response == null) {
                        bytes.position(0);
                        errmsg = "raw bytes: " + NetlinkConstants.hexify(bytes);
                    } else {
                        errmsg = response.toString();
                    }
                    Log.e(TAG, "Error " + msgSnippet + ", errmsg=" + errmsg);
                } else {
                    errno = ((NetlinkErrorMessage) response).getNlMsgError().error;
                    if (errno != 0) {
                        Log.e(TAG, "Error " + msgSnippet + ", errmsg=" + response.toString());
                    }
                }
                if (nlSocket != null) {
                    try {
                        nlSocket.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (ErrnoException e) {
                        Log.e(TAG, "Error " + msgSnippet, e);
                        errno = -e.errno;
                    } catch (InterruptedIOException e2) {
                        Log.e(TAG, "Error " + msgSnippet, e2);
                        errno = -OsConstants.ETIMEDOUT;
                    } catch (SocketException e3) {
                        Log.e(TAG, "Error " + msgSnippet, e3);
                        errno = -OsConstants.EIO;
                    }
                }
                return errno;
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                if (nlSocket != null) {
                    try {
                        nlSocket.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                } else {
                    throw th;
                }
            }
        } catch (Throwable th7) {
            th = th7;
            th2 = null;
            nlSocket = null;
            if (nlSocket != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public IpReachabilityMonitor(Context context, String ifName, Callback callback) {
        this(context, ifName, callback, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0074 A:{Splitter: B:1:0x0028, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0074, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x0095, code:
            throw new java.lang.IllegalArgumentException("invalid interface '" + r9 + "': ", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IpReachabilityMonitor(Context context, String ifName, Callback callback, AvoidBadWifiTracker tracker) throws IllegalArgumentException {
        this.mLock = new Object();
        this.mMetricsLog = new IpConnectivityLog();
        this.mLinkProperties = new LinkProperties();
        this.mIpWatchList = new HashMap();
        this.mStopped = false;
        this.mArpPeer = null;
        this.mInterfaceName = ifName;
        try {
            this.mInterfaceIndex = NetworkInterface.getByName(ifName).getIndex();
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "IpReachabilityMonitor." + this.mInterfaceName);
            this.mCallback = callback;
            this.mAvoidBadWifiTracker = tracker;
            this.mNetlinkSocketObserver = new NetlinkSocketObserver(this, null);
            this.mObserverThread = new Thread(this.mNetlinkSocketObserver);
            this.mObserverThread.start();
            mContext = context;
        } catch (Exception e) {
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            this.mRunning = false;
        }
        clearLinkProperties();
        this.mNetlinkSocketObserver.clearNetlinkSocket();
        synchronized (this.mLock) {
            this.mStopped = true;
        }
        if (this.mArpPeer != null) {
            this.mArpPeer.close();
            this.mArpPeer = null;
        }
    }

    private String describeWatchList() {
        String delimiter = ", ";
        StringBuilder sb = new StringBuilder();
        synchronized (this.mLock) {
            sb.append("iface{").append(this.mInterfaceName).append("/").append(this.mInterfaceIndex).append("}, ");
            sb.append("v{").append(this.mIpWatchListVersion).append("}, ");
            sb.append("ntable=[");
            boolean firstTime = true;
            for (Entry<InetAddress, Short> entry : this.mIpWatchList.entrySet()) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(", ");
                }
                sb.append(((InetAddress) entry.getKey()).getHostAddress()).append("/").append(StructNdMsg.stringForNudState(((Short) entry.getValue()).shortValue()));
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private boolean isWatching(InetAddress ip) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mRunning ? this.mIpWatchList.containsKey(ip) : false;
        }
        return containsKey;
    }

    private boolean stillRunning() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mRunning;
        }
        return z;
    }

    private boolean isMonitorStopped() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mStopped;
        }
        return z;
    }

    private static boolean isOnLink(List<RouteInfo> routes, InetAddress ip) {
        for (RouteInfo route : routes) {
            if (!route.hasGateway() && route.matches(ip)) {
                return true;
            }
        }
        return false;
    }

    private short getNeighborStateLocked(InetAddress ip) {
        if (this.mIpWatchList.containsKey(ip)) {
            return ((Short) this.mIpWatchList.get(ip)).shortValue();
        }
        return (short) 0;
    }

    public void updateLinkProperties(LinkProperties lp) {
        if (this.mInterfaceName.equals(lp.getInterfaceName())) {
            synchronized (this.mLock) {
                this.mLinkProperties = new LinkProperties(lp);
                Map<InetAddress, Short> newIpWatchList = new HashMap();
                List<RouteInfo> routes = this.mLinkProperties.getRoutes();
                for (RouteInfo route : routes) {
                    if (route.hasGateway()) {
                        InetAddress gw = route.getGateway();
                        if (isOnLink(routes, gw)) {
                            newIpWatchList.put(gw, Short.valueOf(getNeighborStateLocked(gw)));
                        }
                    }
                }
                for (InetAddress nameserver : lp.getDnsServers()) {
                    if (isOnLink(routes, nameserver)) {
                        newIpWatchList.put(nameserver, Short.valueOf(getNeighborStateLocked(nameserver)));
                    }
                }
                this.mIpWatchList = newIpWatchList;
                this.mIpWatchListVersion++;
            }
            Log.d(TAG, "watch: " + describeWatchList());
            return;
        }
        Log.wtf(TAG, "requested LinkProperties interface '" + lp.getInterfaceName() + "' does not match: " + this.mInterfaceName);
    }

    public void clearLinkProperties() {
        synchronized (this.mLock) {
            this.mLinkProperties.clear();
            this.mIpWatchList.clear();
            this.mIpWatchListVersion++;
        }
        Log.d(TAG, "clear: " + describeWatchList());
    }

    private void handleNeighborLost(String msg) {
        ProvisioningChange delta;
        InetAddress ip = null;
        synchronized (this.mLock) {
            LinkProperties whatIfLp = new LinkProperties(this.mLinkProperties);
            for (Entry<InetAddress, Short> entry : this.mIpWatchList.entrySet()) {
                if (((Short) entry.getValue()).shortValue() == (short) 32) {
                    ip = (InetAddress) entry.getKey();
                    for (RouteInfo route : this.mLinkProperties.getRoutes()) {
                        if (ip.equals(route.getGateway())) {
                            whatIfLp.removeRoute(route);
                        }
                    }
                    if (avoidingBadLinks() || !(ip instanceof Inet6Address)) {
                        whatIfLp.removeDnsServer(ip);
                    }
                }
            }
            delta = LinkProperties.compareProvisioning(this.mLinkProperties, whatIfLp);
        }
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            String logMsg = "FAILURE: LOST_PROVISIONING, " + msg;
            Log.w(TAG, logMsg);
            if (this.mCallback != null) {
                this.mCallback.notifyLost(ip, logMsg);
            }
        }
        logNudFailed(delta);
    }

    private boolean avoidingBadLinks() {
        return this.mAvoidBadWifiTracker != null ? this.mAvoidBadWifiTracker.currentValue() : true;
    }

    public void probeAll() {
        Set<InetAddress> ipProbeList = new HashSet();
        synchronized (this.mLock) {
            ipProbeList.addAll(this.mIpWatchList.keySet());
        }
        if (!ipProbeList.isEmpty() && stillRunning()) {
            this.mWakeLock.acquire(getProbeWakeLockDuration());
        }
        for (InetAddress target : ipProbeList) {
            if (!stillRunning()) {
                break;
            }
            int returnValue = probeNeighbor(this.mInterfaceIndex, target);
            logEvent(256, returnValue);
            if (returnValue != 0) {
                logEvent(256, 0);
                sendUdpBroadcast(target);
            }
        }
        this.mLastProbeTimeMs = SystemClock.elapsedRealtime();
    }

    private void sendUdpBroadcast(InetAddress target) {
        if (SDBG) {
            Log.d(TAG, "sendUdpBroadcast with " + target);
        }
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] data = new byte[1];
            data[0] = (byte) -1;
            socket.send(new DatagramPacket(data, data.length, target, 19191));
        } catch (Exception e) {
            Log.e(TAG, "sendUdpBroadcast: " + e);
        }
    }

    private static long getProbeWakeLockDuration() {
        return 3500;
    }

    private void logEvent(int probeType, int errorCode) {
        this.mMetricsLog.log(new IpReachabilityEvent(this.mInterfaceName, probeType | (errorCode & 255)));
    }

    private void logNudFailed(ProvisioningChange delta) {
        this.mMetricsLog.log(new IpReachabilityEvent(this.mInterfaceName, IpReachabilityEvent.nudFailureEventType(SystemClock.elapsedRealtime() - this.mLastProbeTimeMs < getProbeWakeLockDuration(), delta == ProvisioningChange.LOST_PROVISIONING)));
    }

    public void probeGateway(final Inet4Address gwAddress, final String iface) {
        Inet4Address addr = gwAddress;
        Log.d(TAG, "probeGateway:" + gwAddress + ":" + iface);
        new Thread("probeGateway") {
            public void run() {
                Log.d(IpReachabilityMonitor.TAG, "probeGateway");
                IpReachabilityMonitor.this.performCheckDupGw(gwAddress, iface);
            }
        }.start();
    }

    private void performCheckDupGw(Inet4Address target, String iface) {
        if (target instanceof Inet4Address) {
            Log.d(TAG, "performCheckDupGw : " + target);
            try {
                this.mArpPeer = new ArpPeer(mContext, iface, null, target);
                this.mArpPeer.doDupArp(5000);
                if (this.mArpPeer != null) {
                    this.mArpPeer.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "err :" + e);
                if (this.mArpPeer != null) {
                    this.mArpPeer.close();
                }
            } catch (Throwable th) {
                if (this.mArpPeer != null) {
                    this.mArpPeer.close();
                }
            }
        }
        this.mArpPeer = null;
    }
}
