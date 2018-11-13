package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.arp.ArpPeer;
import android.net.dhcp.DhcpPacket.ParseException;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.IpConnectivityLog;
import android.net.netlink.StructNlMsgHdr;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.display.OppoBrightUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoBridge;

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
public class DhcpClient extends StateMachine {
    public static final int CMD_CLEAR_LINKADDRESS = 196615;
    public static final int CMD_CONFIGURE_LINKADDRESS = 196616;
    private static final int CMD_EXPIRE_DHCP = 196714;
    private static final int CMD_KICK = 196709;
    public static final int CMD_ON_QUIT = 196613;
    public static final int CMD_POST_DHCP_ACTION = 196612;
    public static final int CMD_PRE_DHCP_ACTION = 196611;
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE = 196614;
    private static final int CMD_REBIND_DHCP = 196713;
    private static final int CMD_RECEIVED_PACKET = 196710;
    public static final int CMD_RENEW_AFTER_ROAMING = 196629;
    private static final int CMD_RENEW_DHCP = 196712;
    public static final int CMD_RENEW_TIMES_POST = 196628;
    public static final int CMD_START_DHCP = 196609;
    public static final int CMD_STOP_DHCP = 196610;
    private static final int CMD_TIMEOUT = 196711;
    private static final boolean DBG = true;
    public static final int DHCP_FAILURE = 2;
    private static final String DHCP_LEASE_FILE = "/data/misc/dhcp/dhcp_lease.conf";
    private static final int DHCP_RENEW_TIMEOUT_MS = 8000;
    public static final int DHCP_SUCCESS = 1;
    private static final int DHCP_TIMEOUT_MS = 36000;
    private static final boolean DO_UNICAST = false;
    public static final int EVENT_LINKADDRESS_CONFIGURED = 196617;
    private static final int FIRST_TIMEOUT_MS = 250;
    private static final int MAX_TIMEOUT_MS = 128000;
    private static final boolean MSG_DBG = true;
    private static final boolean PACKET_DBG = true;
    private static final int PRIVATE_BASE = 196708;
    private static final int PUBLIC_BASE = 196608;
    static final byte[] REQUESTED_PARAMS = null;
    private static final int SECONDS = 1000;
    private static final boolean STATE_DBG = true;
    private static final String TAG = "DhcpClient";
    private static long mDhcpLeaseExpiry;
    private static boolean mFastRequest;
    private static DhcpResults mOffer;
    private static AtomicBoolean mOfferCleared;
    private static final Class[] sMessageClasses = null;
    private static final SparseArray<String> sMessageNames = null;
    private State mConfiguringInterfaceState;
    private final Context mContext;
    private final StateMachine mController;
    private State mDhcpBoundState;
    private State mDhcpHaveLeaseState;
    private State mDhcpInitRebootState;
    private State mDhcpInitState;
    private DhcpResults mDhcpLease;
    private State mDhcpRebindingState;
    private State mDhcpRebootingState;
    private State mDhcpRenewingState;
    private State mDhcpRequestingState;
    private State mDhcpSelectingState;
    private State mDhcpState;
    private final WakeupMessage mExpiryAlarm;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private final String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private boolean mIsAutoIpEnabled;
    private boolean mIsIpRecoverEnabled;
    private final WakeupMessage mKickAlarm;
    private long mLastBoundExitTime;
    private long mLastInitEnterTime;
    private final IpConnectivityLog mMetricsLog;
    private FileDescriptor mPacketSock;
    private DhcpResults mPastDhcpLease;
    private final Random mRandom;
    private final WakeupMessage mRebindAlarm;
    private ReceiveThread mReceiveThread;
    private boolean mRegisteredForPreDhcpNotification;
    private final WakeupMessage mRenewAlarm;
    private boolean mRewnewForRoaming;
    private State mStoppedState;
    private final WakeupMessage mTimeoutAlarm;
    private int mTransactionId;
    private long mTransactionStartMillis;
    private FileDescriptor mUdpSock;
    private State mWaitBeforeRenewalState;
    private State mWaitBeforeStartState;

    abstract class LoggingState extends State {
        private long mEnterTimeMs;

        LoggingState() {
        }

        public void enter() {
            Log.d(DhcpClient.TAG, "Entering state " + getName());
            this.mEnterTimeMs = SystemClock.elapsedRealtime();
        }

        public void exit() {
            DhcpClient.this.logState(getName(), (int) (SystemClock.elapsedRealtime() - this.mEnterTimeMs));
        }

        private String messageName(int what) {
            return (String) DhcpClient.sMessageNames.get(what, Integer.toString(what));
        }

        private String messageToString(Message message) {
            long now = SystemClock.uptimeMillis();
            StringBuilder b = new StringBuilder(" ");
            TimeUtils.formatDuration(message.getWhen() - now, b);
            b.append(" ").append(messageName(message.what)).append(" ").append(message.arg1).append(" ").append(message.arg2).append(" ").append(message.obj);
            return b.toString();
        }

        public boolean processMessage(Message message) {
            Log.d(DhcpClient.TAG, getName() + messageToString(message));
            return false;
        }

        public String getName() {
            return getClass().getSimpleName();
        }
    }

    class ConfiguringInterfaceState extends LoggingState {
        ConfiguringInterfaceState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_CONFIGURE_LINKADDRESS, DhcpClient.this.mDhcpLease.ipAddress);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.EVENT_LINKADDRESS_CONFIGURED /*196617*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class DhcpBoundState extends LoggingState {
        DhcpBoundState() {
            super();
        }

        public void enter() {
            super.enter();
            if (!(DhcpClient.this.mDhcpLease.serverAddress == null || DhcpClient.this.connectUdpSock(DhcpClient.this.mDhcpLease.serverAddress))) {
                DhcpClient.this.notifyFailure();
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
            }
            DhcpClient.this.scheduleLeaseTimers();
            logTimeToBoundState();
        }

        public void exit() {
            super.exit();
            DhcpClient.this.mLastBoundExitTime = SystemClock.elapsedRealtime();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_RENEW_AFTER_ROAMING /*196629*/:
                    DhcpClient.this.mRewnewForRoaming = true;
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeRenewalState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRenewingState);
                    }
                    return true;
                case DhcpClient.CMD_RENEW_DHCP /*196712*/:
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeRenewalState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRenewingState);
                    }
                    return true;
                default:
                    return false;
            }
        }

        private void logTimeToBoundState() {
            long now = SystemClock.elapsedRealtime();
            if (DhcpClient.this.mLastBoundExitTime > DhcpClient.this.mLastInitEnterTime) {
                DhcpClient.this.logState("RenewingBoundState", (int) (now - DhcpClient.this.mLastBoundExitTime));
            } else {
                DhcpClient.this.logState("InitialBoundState", (int) (now - DhcpClient.this.mLastInitEnterTime));
            }
        }
    }

    class DhcpHaveLeaseState extends State {
        DhcpHaveLeaseState() {
        }

        public void enter() {
            super.enter();
            putLeaseToFile();
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x017e A:{SYNTHETIC, Splitter: B:35:0x017e} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x01a1 A:{SYNTHETIC, Splitter: B:41:0x01a1} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void putLeaseToFile() {
            IOException e;
            Throwable th;
            BufferedWriter writer = null;
            try {
                BufferedWriter writer2 = new BufferedWriter(new FileWriter(DhcpClient.DHCP_LEASE_FILE));
                try {
                    if (!(DhcpClient.this.mDhcpLease.ipAddress == null || DhcpClient.this.mDhcpLease.ipAddress.getAddress() == null)) {
                        writer2.write("IP=" + DhcpClient.this.mDhcpLease.ipAddress.getAddress().getHostAddress() + "\n");
                    }
                    if (!(DhcpClient.this.mDhcpLease.gateway == null || DhcpClient.this.mDhcpLease.gateway.getAddress() == null)) {
                        writer2.write("Gateway=" + DhcpClient.this.mDhcpLease.gateway.getHostAddress() + "\n");
                    }
                    if (DhcpClient.this.mDhcpLease.dnsServers != null) {
                        Iterator dns$iterator = DhcpClient.this.mDhcpLease.dnsServers.iterator();
                        if (dns$iterator.hasNext()) {
                            writer2.write("DNS=" + ((InetAddress) dns$iterator.next()).getHostAddress() + "\n");
                        }
                    }
                    if (DhcpClient.this.mDhcpLease.domains != null) {
                        writer2.write("Domain=" + DhcpClient.this.mDhcpLease.domains + "\n");
                    }
                    if (DhcpClient.this.mDhcpLease.serverAddress != null) {
                        writer2.write("Server=" + DhcpClient.this.mDhcpLease.serverAddress.getHostAddress() + "\n");
                    }
                    if (writer2 != null) {
                        try {
                            writer2.close();
                        } catch (IOException e2) {
                            Log.e(DhcpClient.TAG, "putLeaseToFile()-02: " + e2);
                        }
                    }
                    writer = writer2;
                } catch (IOException e3) {
                    e2 = e3;
                    writer = writer2;
                    try {
                        Log.e(DhcpClient.TAG, "putLeaseToFile()-01: " + e2);
                        if (writer == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e22) {
                                Log.e(DhcpClient.TAG, "putLeaseToFile()-02: " + e22);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    writer = writer2;
                    if (writer != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e22 = e4;
                Log.e(DhcpClient.TAG, "putLeaseToFile()-01: " + e22);
                if (writer == null) {
                    try {
                        writer.close();
                    } catch (IOException e222) {
                        Log.e(DhcpClient.TAG, "putLeaseToFile()-02: " + e222);
                    }
                }
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case DhcpClient.CMD_EXPIRE_DHCP /*196714*/:
                    Log.d(DhcpClient.TAG, "Lease expired!");
                    DhcpClient.this.notifyFailure();
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            DhcpClient.this.mRenewAlarm.cancel();
            DhcpClient.this.mRebindAlarm.cancel();
            DhcpClient.this.mExpiryAlarm.cancel();
            DhcpClient.this.clearDhcpState();
        }
    }

    class DhcpInitRebootState extends LoggingState {
        DhcpInitRebootState() {
            super();
        }
    }

    abstract class PacketRetransmittingState extends LoggingState {
        protected int mTimeout = 0;
        protected int mTimer;

        protected abstract void receivePacket(DhcpPacket dhcpPacket);

        protected abstract boolean sendPacket();

        PacketRetransmittingState() {
            super();
        }

        public void enter() {
            super.enter();
            initTimer();
            maybeInitTimeout();
            DhcpClient.this.sendMessage(DhcpClient.CMD_KICK);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_KICK /*196709*/:
                    sendPacket();
                    scheduleKick();
                    return true;
                case DhcpClient.CMD_RECEIVED_PACKET /*196710*/:
                    receivePacket((DhcpPacket) message.obj);
                    return true;
                case DhcpClient.CMD_TIMEOUT /*196711*/:
                    timeout();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            super.exit();
            DhcpClient.this.mKickAlarm.cancel();
            DhcpClient.this.mTimeoutAlarm.cancel();
        }

        protected void timeout() {
        }

        protected void initTimer() {
            this.mTimer = 250;
        }

        protected int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            return baseTimer + (DhcpClient.this.mRandom.nextInt(maxJitter * 2) - maxJitter);
        }

        protected void scheduleKick() {
            DhcpClient.this.mKickAlarm.schedule(SystemClock.elapsedRealtime() + ((long) jitterTimer(this.mTimer)));
            this.mTimer *= 2;
            if (this.mTimer > DhcpClient.MAX_TIMEOUT_MS) {
                this.mTimer = DhcpClient.MAX_TIMEOUT_MS;
            }
        }

        protected void maybeInitTimeout() {
            if (this.mTimeout > 0) {
                DhcpClient.this.mTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeout));
            }
        }
    }

    class DhcpInitState extends PacketRetransmittingState {
        public DhcpInitState() {
            super();
        }

        public void enter() {
            DhcpClient.this.mLastInitEnterTime = SystemClock.elapsedRealtime();
            if (DhcpClient.mOffer == null || DhcpClient.mOfferCleared.get() || DhcpClient.this.mLastInitEnterTime >= DhcpClient.mDhcpLeaseExpiry) {
                DhcpClient.mFastRequest = false;
                DhcpClient.mOfferCleared.set(false);
                super.enter();
                DhcpClient.this.startNewTransaction();
                return;
            }
            Log.d(DhcpClient.TAG, "DhcpInitState mOffer != null, go and Request.");
            this.mTimer = 250;
            DhcpClient.mFastRequest = true;
            DhcpClient.this.startNewTransaction();
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
        }

        protected boolean sendPacket() {
            return DhcpClient.this.sendDiscoverPacket();
        }

        protected void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet) && (packet instanceof DhcpOfferPacket)) {
                DhcpClient.mOffer = packet.toDhcpResults();
                if (DhcpClient.mOffer != null) {
                    Log.d(DhcpClient.TAG, "Got pending lease: " + DhcpClient.mOffer);
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
                }
            }
        }

        protected void timeout() {
            if (DhcpClient.this.doIpRecover()) {
                DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
            }
        }

        protected void scheduleKick() {
            long timeout = (long) jitterTimer(this.mTimer);
            Log.d(DhcpClient.TAG, "scheduleKick()@DhcpInitState timeout=" + timeout);
            DhcpClient.this.sendMessageDelayed(DhcpClient.CMD_KICK, timeout);
            this.mTimer *= 2;
            if (this.mTimer > DhcpClient.MAX_TIMEOUT_MS) {
                this.mTimer = DhcpClient.MAX_TIMEOUT_MS;
            }
        }
    }

    abstract class DhcpReacquiringState extends PacketRetransmittingState {
        protected String mLeaseMsg;

        protected abstract Inet4Address packetDestination();

        DhcpReacquiringState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.startNewTransaction();
        }

        protected boolean sendPacket() {
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_RENEW_TIMES_POST);
            return DhcpClient.this.sendRequestPacket((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress(), DhcpPacket.INADDR_ANY, null, packetDestination());
        }

        protected void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet)) {
                if (packet instanceof DhcpAckPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        if (!DhcpClient.this.mDhcpLease.ipAddress.equals(results.ipAddress)) {
                            Log.d(DhcpClient.TAG, "Renewed lease not for our current IP address!");
                            DhcpClient.this.notifyFailure();
                            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                        }
                        DhcpClient.this.setDhcpLeaseExpiry(packet);
                        DhcpClient.this.acceptDhcpResults(results, this.mLeaseMsg);
                        if (DhcpClient.this.mRewnewForRoaming) {
                            DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                        } else {
                            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                        }
                    }
                } else if (packet instanceof DhcpNakPacket) {
                    Log.d(DhcpClient.TAG, "Received NAK, returning to INIT");
                    DhcpClient.mOffer = null;
                    if (!DhcpClient.this.mRewnewForRoaming) {
                        DhcpClient.this.notifyFailure();
                    }
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }

        protected void timeout() {
            Log.d(DhcpClient.TAG, this.mLeaseMsg + " timeout");
            DhcpClient.mOffer = null;
            DhcpClient.this.mKickAlarm.cancel();
        }
    }

    class DhcpRebindingState extends DhcpReacquiringState {
        public DhcpRebindingState() {
            super();
            this.mLeaseMsg = "Rebound";
        }

        public void enter() {
            this.mTimeout = DhcpClient.DHCP_TIMEOUT_MS;
            super.enter();
            DhcpClient.closeQuietly(DhcpClient.this.mUdpSock);
            if (!DhcpClient.this.initUdpSocket()) {
                Log.e(DhcpClient.TAG, "Failed to recreate UDP socket");
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
        }

        protected Inet4Address packetDestination() {
            return DhcpPacket.INADDR_BROADCAST;
        }
    }

    class DhcpRebootingState extends LoggingState {
        DhcpRebootingState() {
            super();
        }
    }

    class DhcpRenewingState extends DhcpReacquiringState {
        public DhcpRenewingState() {
            super();
            this.mLeaseMsg = "Renewed";
        }

        public void enter() {
            if (DhcpClient.this.mRewnewForRoaming) {
                this.mTimeout = DhcpClient.DHCP_RENEW_TIMEOUT_MS;
            } else {
                this.mTimeout = DhcpClient.DHCP_TIMEOUT_MS;
            }
            super.enter();
        }

        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            switch (message.what) {
                case DhcpClient.CMD_REBIND_DHCP /*196713*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRebindingState);
                    return true;
                default:
                    return false;
            }
        }

        protected Inet4Address packetDestination() {
            return DhcpClient.this.mDhcpLease.serverAddress != null ? DhcpClient.this.mDhcpLease.serverAddress : DhcpPacket.INADDR_BROADCAST;
        }

        protected void timeout() {
            super.timeout();
            if (DhcpClient.this.mRewnewForRoaming) {
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
        }

        public void exit() {
            super.exit();
            DhcpClient.this.mRewnewForRoaming = false;
        }
    }

    class DhcpRequestingState extends PacketRetransmittingState {
        public DhcpRequestingState() {
            super();
            this.mTimeout = OppoBrightUtils.HIGH_BRIGHTNESS_DEBOUNCE_LUX;
        }

        protected boolean sendPacket() {
            return DhcpClient.this.sendRequestPacket(DhcpPacket.INADDR_ANY, (Inet4Address) DhcpClient.mOffer.ipAddress.getAddress(), DhcpClient.mFastRequest ? null : DhcpClient.mOffer.serverAddress, DhcpPacket.INADDR_BROADCAST);
        }

        protected void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet)) {
                if (packet instanceof DhcpAckPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        DhcpClient.this.setDhcpLeaseExpiry(packet);
                        DhcpClient.this.acceptDhcpResults(results, "Confirmed");
                        DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                    }
                } else if (packet instanceof DhcpNakPacket) {
                    Log.d(DhcpClient.TAG, "Received NAK, returning to INIT");
                    DhcpClient.mOffer = null;
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }

        protected void timeout() {
            if (DhcpClient.this.mIsAutoIpEnabled && DhcpClient.this.performAutoIP()) {
                DhcpClient.mOffer = null;
                DhcpClient.this.notifySuccess();
                DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                return;
            }
            DhcpClient.mOffer = null;
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
        }

        protected void scheduleKick() {
            long timeout = (long) jitterTimer(this.mTimer);
            Log.d(DhcpClient.TAG, "scheduleKick()@DhcpRequestingState timeout=" + timeout);
            DhcpClient.this.sendMessageDelayed(DhcpClient.CMD_KICK, timeout);
            this.mTimer *= 2;
            if (this.mTimer > DhcpClient.MAX_TIMEOUT_MS) {
                this.mTimer = DhcpClient.MAX_TIMEOUT_MS;
            }
        }
    }

    class DhcpSelectingState extends LoggingState {
        DhcpSelectingState() {
            super();
        }
    }

    class DhcpState extends State {
        DhcpState() {
        }

        public void enter() {
            DhcpClient.this.clearDhcpState();
            if (DhcpClient.this.initInterface() && DhcpClient.this.initSockets()) {
                DhcpClient.this.mReceiveThread = new ReceiveThread();
                DhcpClient.this.mReceiveThread.start();
                return;
            }
            DhcpClient.this.notifyFailure();
            DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
        }

        public void exit() {
            if (DhcpClient.this.mReceiveThread != null) {
                DhcpClient.this.mReceiveThread.halt();
                DhcpClient.this.mReceiveThread = null;
            }
            DhcpClient.this.clearDhcpState();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_STOP_DHCP /*196610*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class ReceiveThread extends Thread {
        private final byte[] mPacket = new byte[1500];
        private volatile boolean mStopped = false;

        ReceiveThread() {
        }

        public void halt() {
            this.mStopped = true;
            DhcpClient.this.closeSockets();
        }

        /* JADX WARNING: Removed duplicated region for block: B:7:0x004a A:{Splitter: B:4:0x000f, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:7:0x004a, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:9:0x004d, code:
            if (r13.mStopped == false) goto L_0x004f;
     */
        /* JADX WARNING: Missing block: B:10:0x004f, code:
            android.util.Log.e(android.net.dhcp.DhcpClient.TAG, "Read error", r3);
            android.net.dhcp.DhcpClient.-wrap12(r13.this$0, android.net.metrics.DhcpErrorEvent.RECEIVE_ERROR);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Log.d(DhcpClient.TAG, "Receive thread started");
            while (!this.mStopped) {
                int length = 0;
                try {
                    length = Os.read(DhcpClient.this.mPacketSock, this.mPacket, 0, this.mPacket.length);
                    DhcpPacket packet = DhcpPacket.decodeFullPacket(this.mPacket, length, 0);
                    Log.d(DhcpClient.TAG, "Received packet: " + packet);
                    DhcpClient.this.sendMessage(DhcpClient.CMD_RECEIVED_PACKET, packet);
                } catch (Exception e) {
                } catch (ParseException e2) {
                    Log.e(DhcpClient.TAG, "Can't parse packet: " + e2.getMessage());
                    Log.d(DhcpClient.TAG, HexDump.dumpHexString(this.mPacket, 0, length));
                    if (e2.errorCode == DhcpErrorEvent.DHCP_NO_COOKIE) {
                        String data = ParseException.class.getName();
                        Object[] objArr = new Object[3];
                        objArr[0] = "31850211";
                        objArr[1] = Integer.valueOf(-1);
                        objArr[2] = data;
                        EventLog.writeEvent(1397638484, objArr);
                    }
                    DhcpClient.this.logError(e2.errorCode);
                }
            }
            Log.d(DhcpClient.TAG, "Receive thread stopped");
        }
    }

    class StoppedState extends State {
        String reqDNS = null;
        String reqDomain = null;
        String reqGW = null;
        String reqIP = null;
        String srvIP = null;

        StoppedState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case DhcpClient.CMD_START_DHCP /*196609*/:
                    DhcpClient.this.mPastDhcpLease = (DhcpResults) message.obj;
                    DhcpClient.this.mIsIpRecoverEnabled = true;
                    Log.d(DhcpClient.TAG, "IP recover: past lease from caller:\n\t" + DhcpClient.this.mPastDhcpLease + ", DhcpClient Protocol.BASE_DHCP = " + DhcpClient.PUBLIC_BASE);
                    checkPastLease();
                    Log.d(DhcpClient.TAG, "IP recover: past lease after check:\n\t" + DhcpClient.this.mPastDhcpLease);
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    }
                    return true;
                default:
                    return false;
            }
        }

        private void checkPastLease() {
            if (DhcpClient.this.mPastDhcpLease == null) {
                getLeaseFromFile();
                if (this.reqIP == null || this.reqGW == null || this.reqDNS == null || this.srvIP == null) {
                    Log.e(DhcpClient.TAG, "checkPastLease(): past dhcp lease was not valid, request IP = " + this.reqIP + ", request Gateway = " + this.reqGW + ", request DNS = " + this.reqDNS + ", server IP = " + this.srvIP);
                    return;
                }
                DhcpClient.this.mPastDhcpLease = new DhcpResults();
                try {
                    int prefixLength = NetworkUtils.getImplicitNetmask((Inet4Address) InetAddress.getByName(this.reqIP));
                    DhcpClient.this.mPastDhcpLease.ipAddress = new LinkAddress((Inet4Address) InetAddress.getByName(this.reqIP), prefixLength);
                    DhcpClient.this.mPastDhcpLease.gateway = InetAddress.getByName(this.reqGW);
                    DhcpClient.this.mPastDhcpLease.dnsServers.add((Inet4Address) InetAddress.getByName(this.reqDNS));
                    DhcpClient.this.mPastDhcpLease.domains = this.reqDomain;
                } catch (UnknownHostException e) {
                    DhcpClient.this.mPastDhcpLease = null;
                    Log.e(DhcpClient.TAG, "checkPastLease(): past dhcp lease some IP was not valid, " + e);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0083 A:{SYNTHETIC, Splitter: B:28:0x0083} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x00c3 A:{SYNTHETIC, Splitter: B:44:0x00c3} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void getLeaseFromFile() {
            IOException e;
            Throwable th;
            if (new File(DhcpClient.DHCP_LEASE_FILE).exists()) {
                BufferedReader bufferedReader = null;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(DhcpClient.DHCP_LEASE_FILE));
                    try {
                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            String[] nameValue;
                            String str;
                            if (line.startsWith("IP")) {
                                nameValue = line.split("=");
                                this.reqIP = nameValue.length != 2 ? null : nameValue[1];
                            } else if (line.startsWith("Gateway")) {
                                nameValue = line.split("=");
                                if (nameValue.length != 2) {
                                    str = null;
                                } else {
                                    str = nameValue[1];
                                }
                                this.reqGW = str;
                            } else if (line.startsWith("DNS")) {
                                nameValue = line.split("=");
                                if (nameValue.length != 2) {
                                    str = null;
                                } else {
                                    str = nameValue[1];
                                }
                                this.reqDNS = str;
                            } else if (line.startsWith("Domain")) {
                                nameValue = line.split("=");
                                this.reqDomain = nameValue.length != 2 ? null : nameValue[1];
                            } else if (line.startsWith("Server")) {
                                nameValue = line.split("=");
                                this.srvIP = nameValue.length != 2 ? null : nameValue[1];
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                                return;
                            } catch (IOException e2) {
                                Log.e(DhcpClient.TAG, "getLeaseFromFile()-02: " + e2);
                                return;
                            }
                        }
                        return;
                    } catch (IOException e3) {
                        e2 = e3;
                        bufferedReader = reader;
                        try {
                            Log.e(DhcpClient.TAG, "getLeaseFromFile()-01: " + e2);
                            if (bufferedReader == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e22) {
                                    Log.e(DhcpClient.TAG, "getLeaseFromFile()-02: " + e22);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = reader;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e22 = e4;
                    Log.e(DhcpClient.TAG, "getLeaseFromFile()-01: " + e22);
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e222) {
                            Log.e(DhcpClient.TAG, "getLeaseFromFile()-02: " + e222);
                            return;
                        }
                    }
                    return;
                }
            }
            Log.e(DhcpClient.TAG, "getLeaseFromFile(): file not existed");
        }
    }

    abstract class WaitBeforeOtherState extends LoggingState {
        protected State mOtherState;

        WaitBeforeOtherState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE /*196614*/:
                    DhcpClient.this.mIsIpRecoverEnabled = true;
                    if (message.obj != null) {
                        WifiConfiguration wifiConfig = message.obj;
                        if (wifiConfig.allowedKeyManagement.get(0) && wifiConfig.wepTxKeyIndex >= 0 && wifiConfig.wepTxKeyIndex < wifiConfig.wepKeys.length && wifiConfig.wepKeys[wifiConfig.wepTxKeyIndex] != null) {
                            DhcpClient.this.mIsIpRecoverEnabled = false;
                        }
                    }
                    Log.d(DhcpClient.TAG, "IP recover: mIsIpRecoverEnabled@CMD_PRE_DHCP_ACTION_COMPLETE = " + DhcpClient.this.mIsIpRecoverEnabled);
                    DhcpClient.this.transitionTo(this.mOtherState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class WaitBeforeRenewalState extends WaitBeforeOtherState {
        public WaitBeforeRenewalState(State otherState) {
            super();
            this.mOtherState = otherState;
        }
    }

    class WaitBeforeStartState extends WaitBeforeOtherState {
        public WaitBeforeStartState(State otherState) {
            super();
            this.mOtherState = otherState;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.DhcpClient.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.DhcpClient.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.DhcpClient.<clinit>():void");
    }

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        return new WakeupMessage(this.mContext, getHandler(), DhcpClient.class.getSimpleName() + "." + this.mIfaceName + "." + cmdName, cmd);
    }

    private DhcpClient(Context context, StateMachine controller, String iface) {
        super(TAG);
        this.mRewnewForRoaming = false;
        this.mMetricsLog = new IpConnectivityLog();
        this.mIsAutoIpEnabled = false;
        this.mIsIpRecoverEnabled = true;
        this.mStoppedState = new StoppedState();
        this.mDhcpState = new DhcpState();
        this.mDhcpInitState = new DhcpInitState();
        this.mDhcpSelectingState = new DhcpSelectingState();
        this.mDhcpRequestingState = new DhcpRequestingState();
        this.mDhcpHaveLeaseState = new DhcpHaveLeaseState();
        this.mConfiguringInterfaceState = new ConfiguringInterfaceState();
        this.mDhcpBoundState = new DhcpBoundState();
        this.mDhcpRenewingState = new DhcpRenewingState();
        this.mDhcpRebindingState = new DhcpRebindingState();
        this.mDhcpInitRebootState = new DhcpInitRebootState();
        this.mDhcpRebootingState = new DhcpRebootingState();
        this.mWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpInitState);
        this.mWaitBeforeRenewalState = new WaitBeforeRenewalState(this.mDhcpRenewingState);
        this.mContext = context;
        this.mController = controller;
        this.mIfaceName = iface;
        addState(this.mStoppedState);
        addState(this.mDhcpState);
        addState(this.mDhcpInitState, this.mDhcpState);
        addState(this.mWaitBeforeStartState, this.mDhcpState);
        addState(this.mDhcpSelectingState, this.mDhcpState);
        addState(this.mDhcpRequestingState, this.mDhcpState);
        addState(this.mDhcpHaveLeaseState, this.mDhcpState);
        addState(this.mConfiguringInterfaceState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpBoundState, this.mDhcpHaveLeaseState);
        addState(this.mWaitBeforeRenewalState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRenewingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRebindingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpInitRebootState, this.mDhcpState);
        addState(this.mDhcpRebootingState, this.mDhcpState);
        setInitialState(this.mStoppedState);
        this.mRandom = new Random();
        this.mKickAlarm = makeWakeupMessage("KICK", CMD_KICK);
        this.mTimeoutAlarm = makeWakeupMessage("TIMEOUT", CMD_TIMEOUT);
        this.mRenewAlarm = makeWakeupMessage("RENEW", CMD_RENEW_DHCP);
        this.mRebindAlarm = makeWakeupMessage("REBIND", CMD_REBIND_DHCP);
        this.mExpiryAlarm = makeWakeupMessage("EXPIRY", CMD_EXPIRE_DHCP);
    }

    public void registerForPreDhcpNotification() {
        this.mRegisteredForPreDhcpNotification = true;
    }

    public static DhcpClient makeDhcpClient(Context context, StateMachine controller, String intf) {
        DhcpClient client = new DhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0027 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0028, code:
            android.util.Log.e(TAG, "Can't determine ifindex or MAC address for " + r4.mIfaceName, r0);
            android.util.Log.e(TAG, "mIface = " + r4.mIface);
     */
    /* JADX WARNING: Missing block: B:6:0x0061, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.getIndex(), DhcpPacket.ETHER_BROADCAST);
            this.mInterfaceBroadcastAddr.sll_protocol = StructNlMsgHdr.NLM_F_APPEND;
            return true;
        } catch (Exception e) {
        }
    }

    private void startNewTransaction() {
        this.mTransactionId = this.mRandom.nextInt();
        this.mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    private boolean initSockets() {
        return initPacketSocket() ? initUdpSocket() : false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0026 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0026, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0027, code:
            android.util.Log.e(TAG, "Error creating packet socket", r1);
     */
    /* JADX WARNING: Missing block: B:6:0x0031, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initPacketSocket() {
        try {
            this.mPacketSock = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IP);
            Os.bind(this.mPacketSock, new PacketSocketAddress((short) OsConstants.ETH_P_IP, this.mIface.getIndex()));
            NetworkUtils.attachDhcpFilter(this.mPacketSock);
            return true;
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0046 A:{Splitter: B:1:0x0002, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0046, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0047, code:
            android.util.Log.e(TAG, "Error creating UDP socket", r0);
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initUdpSocket() {
        try {
            this.mUdpSock = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_REUSEADDR, 1);
            Os.setsockoptIfreq(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfaceName);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BROADCAST, 1);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 0);
            Os.bind(this.mUdpSock, Inet4Address.ANY, 68);
            NetworkUtils.protectFromVpn(this.mUdpSock);
            return true;
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0009 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x000a, code:
            android.util.Log.e(TAG, "Error connecting UDP socket", r0);
     */
    /* JADX WARNING: Missing block: B:6:0x0014, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean connectUdpSock(Inet4Address to) {
        try {
            Os.connect(this.mUdpSock, to, 67);
            return true;
        } catch (Exception e) {
        }
    }

    private static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    private void closeSockets() {
        closeQuietly(this.mUdpSock);
        closeQuietly(this.mPacketSock);
    }

    private short getSecs() {
        return (short) ((int) ((SystemClock.elapsedRealtime() - this.mTransactionStartMillis) / 1000));
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x005e A:{Splitter: B:2:0x0005, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x005e, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x005f, code:
            android.util.Log.e(TAG, "Can't send packet: ", r6);
     */
    /* JADX WARNING: Missing block: B:11:0x0068, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean transmitPacket(ByteBuffer buf, String description, int encap, Inet4Address to) {
        if (encap == 0) {
            try {
                Log.d(TAG, "Broadcasting " + description);
                Os.sendto(this.mPacketSock, buf.array(), 0, buf.limit(), 0, this.mInterfaceBroadcastAddr);
            } catch (Exception e) {
            }
        } else if (encap == 2 && to.equals(DhcpPacket.INADDR_BROADCAST)) {
            Log.d(TAG, "Broadcasting " + description);
            Os.sendto(this.mUdpSock, buf, 0, to, 67);
        } else {
            String str = TAG;
            Object[] objArr = new Object[2];
            objArr[0] = description;
            objArr[1] = Os.getpeername(this.mUdpSock);
            Log.d(str, String.format("Unicasting %s to %s", objArr));
            Os.write(this.mUdpSock, buf);
        }
        return true;
    }

    private boolean sendDiscoverPacket() {
        return transmitPacket(DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, REQUESTED_PARAMS), "DHCPDISCOVER", 0, DhcpPacket.INADDR_BROADCAST);
    }

    private boolean sendRequestPacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        return transmitPacket(DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null), "DHCPREQUEST ciaddr=" + clientAddress.getHostAddress() + " request=" + requestedAddress.getHostAddress() + " serverid=" + (serverAddress != null ? serverAddress.getHostAddress() : null), encap, to);
    }

    private void scheduleLeaseTimers() {
        if (mDhcpLeaseExpiry == 0) {
            Log.d(TAG, "Infinite lease, no timer scheduling needed");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long remainingDelay = mDhcpLeaseExpiry - now;
        long renewDelay = remainingDelay / 2;
        long rebindDelay = (7 * remainingDelay) / 8;
        this.mRenewAlarm.schedule(now + renewDelay);
        this.mRebindAlarm.schedule(now + rebindDelay);
        this.mExpiryAlarm.schedule(now + remainingDelay);
        Log.d(TAG, "Scheduling renewal in " + (renewDelay / 1000) + "s");
        Log.d(TAG, "Scheduling rebind in " + (rebindDelay / 1000) + "s");
        Log.d(TAG, "Scheduling expiry in " + (remainingDelay / 1000) + "s");
    }

    private void notifySuccess() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, 1, 0, new DhcpResults(this.mDhcpLease));
    }

    private void notifyFailure() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, 2, 0, null);
    }

    private void acceptDhcpResults(DhcpResults results, String msg) {
        results.setSystemExpiredTime(mDhcpLeaseExpiry);
        this.mDhcpLease = results;
        Log.d(TAG, msg + " lease: " + this.mDhcpLease);
        notifySuccess();
    }

    private void clearDhcpState() {
        this.mDhcpLease = null;
    }

    public void doQuit() {
        Log.d(TAG, "doQuit");
        quit();
    }

    protected void onQuitting() {
        Log.d(TAG, "onQuitting");
        this.mController.sendMessage(CMD_ON_QUIT);
    }

    public boolean isValidPacket(DhcpPacket packet) {
        if (packet == null) {
            Log.d(TAG, "Received null packet!!");
            return false;
        }
        int xid = packet.getTransactionId();
        if (xid != this.mTransactionId) {
            Log.d(TAG, "Unexpected transaction ID " + xid + ", expected " + this.mTransactionId);
            return false;
        } else if (Arrays.equals(packet.getClientMac(), this.mHwAddr)) {
            return true;
        } else {
            Log.d(TAG, "MAC addr mismatch: got " + HexDump.toHexString(packet.getClientMac()) + ", expected " + HexDump.toHexString(this.mHwAddr));
            return false;
        }
    }

    public void setDhcpLeaseExpiry(DhcpPacket packet) {
        long j = 0;
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        if (leaseTimeMillis > 0) {
            j = SystemClock.elapsedRealtime() + leaseTimeMillis;
        }
        mDhcpLeaseExpiry = j;
    }

    private void logError(int errorCode) {
        this.mMetricsLog.log(new DhcpErrorEvent(this.mIfaceName, errorCode));
    }

    private void logState(String name, int durationMs) {
        this.mMetricsLog.log(new DhcpClientEvent(this.mIfaceName, name, durationMs));
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x00f2 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00f2 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0168  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00f2 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0144  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean performAutoIP() {
        UnknownHostException ue;
        ErrnoException ee;
        IllegalArgumentException ie;
        SocketException se;
        Throwable th;
        Random random = new Random();
        int i = 4;
        byte[] autoIp = new byte[]{(byte) -87, (byte) -2, (byte) 10, (byte) 10};
        ArpPeer ap = null;
        boolean retVal = false;
        int i2 = 0;
        while (true) {
            ArpPeer ap2 = ap;
            if (i2 >= 5) {
                return retVal;
            }
            autoIp[2] = new Integer(random.nextInt(256)).byteValue();
            autoIp[3] = new Integer(random.nextInt(254) + 1).byteValue();
            try {
                InetAddress ipAddress = InetAddress.getByAddress(autoIp);
                Log.d(TAG, "performAutoIP(" + i2 + ") = " + "oooKxxxK" + logDumpIpv4(2, ipAddress.getHostAddress()));
                ap = new ArpPeer(this.mIfaceName, Inet4Address.ANY, ipAddress);
                try {
                    if (ap.doArp(5000) == null) {
                        this.mDhcpLease = new DhcpResults();
                        this.mDhcpLease.ipAddress = new LinkAddress(ipAddress, 16);
                        this.mDhcpLease.leaseDuration = -1;
                        setIpAddress(this.mDhcpLease.ipAddress);
                        Log.d(TAG, "performAutoIP done");
                        retVal = true;
                    } else {
                        Log.d(TAG, "DAD detected!!");
                    }
                    if (ap != null) {
                        ap.close();
                    }
                } catch (UnknownHostException e) {
                    ue = e;
                } catch (ErrnoException e2) {
                    ee = e2;
                    Log.d(TAG, "err :" + ee);
                    if (ap == null) {
                    }
                    i2++;
                } catch (IllegalArgumentException e3) {
                    ie = e3;
                    Log.d(TAG, "err :" + ie);
                    if (ap == null) {
                    }
                    i2++;
                } catch (SocketException e4) {
                    se = e4;
                    Log.d(TAG, "err :" + se);
                    if (ap == null) {
                    }
                    i2++;
                }
            } catch (UnknownHostException e5) {
                ue = e5;
                ap = ap2;
                try {
                    Log.d(TAG, "err :" + ue);
                    if (ap != null) {
                        ap.close();
                    }
                    i2++;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (ErrnoException e6) {
                ee = e6;
                ap = ap2;
                Log.d(TAG, "err :" + ee);
                if (ap == null) {
                    ap.close();
                }
                i2++;
            } catch (IllegalArgumentException e7) {
                ie = e7;
                ap = ap2;
                Log.d(TAG, "err :" + ie);
                if (ap == null) {
                    ap.close();
                }
                i2++;
            } catch (SocketException e8) {
                se = e8;
                ap = ap2;
                Log.d(TAG, "err :" + se);
                if (ap == null) {
                    ap.close();
                }
                i2++;
            } catch (Throwable th3) {
                th = th3;
                ap = ap2;
            }
            i2++;
        }
        if (ap != null) {
            ap.close();
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x001a A:{Splitter: B:1:0x0008, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x001a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x001b, code:
            android.util.Log.e(TAG, "Error configuring IP address " + r8 + ": ", r1);
     */
    /* JADX WARNING: Missing block: B:7:0x003d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setIpAddress(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            Stub.asInterface(ServiceManager.getService("network_management")).setInterfaceConfig(this.mIfaceName, ifcg);
            return true;
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x018f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doIpRecover() {
        ErrnoException ee;
        IllegalArgumentException ie;
        SocketException se;
        Throwable th;
        Log.d(TAG, "IP recover: disable it!");
        this.mIsIpRecoverEnabled = false;
        if (!this.mIsIpRecoverEnabled) {
            Log.d(TAG, "IP recover: it was disabled");
            return false;
        } else if (this.mPastDhcpLease == null) {
            Log.d(TAG, "IP recover: mPastDhcpLease is empty");
            return false;
        } else {
            Log.d(TAG, "IP recover: mPastDhcpLease = " + this.mPastDhcpLease);
            long reCaculatedLeaseMillis = this.mPastDhcpLease.systemExpiredTime - SystemClock.elapsedRealtime();
            Log.d(TAG, "IP recover: reCaculatedLeaseMillis = " + reCaculatedLeaseMillis);
            if (reCaculatedLeaseMillis < 0) {
                mDhcpLeaseExpiry = 0;
                Log.e(TAG, "IP recover: lease had been expired! configure to infinite lease");
            } else {
                mDhcpLeaseExpiry = SystemClock.elapsedRealtime() + reCaculatedLeaseMillis;
                Log.d(TAG, "IP recover: mDhcpLeaseExpiry = " + mDhcpLeaseExpiry);
            }
            ArpPeer ap = null;
            boolean retVal = false;
            try {
                InetAddress ipAddress = this.mPastDhcpLease.ipAddress.getAddress();
                Log.d(TAG, "IP recover: arp address = #$%K" + logDumpIpv4(3, ipAddress.getHostAddress()));
                ArpPeer ap2 = new ArpPeer(this.mIfaceName, Inet4Address.ANY, ipAddress);
                try {
                    if (ap2.doArp(5000) == null) {
                        this.mPastDhcpLease.setLeaseDuration(((int) reCaculatedLeaseMillis) / 1000);
                        acceptDhcpResults(this.mPastDhcpLease, "Confirmed");
                        Log.d(TAG, "doIpRecover no arp response, IP can be reused");
                        retVal = true;
                    } else {
                        Log.d(TAG, "doIpRecover DAD detected!!");
                    }
                    if (ap2 != null) {
                        ap2.close();
                    }
                    ap = ap2;
                } catch (ErrnoException e) {
                    ee = e;
                    ap = ap2;
                } catch (IllegalArgumentException e2) {
                    ie = e2;
                    ap = ap2;
                    Log.d(TAG, "err :" + ie);
                    if (ap != null) {
                        ap.close();
                    }
                    return retVal;
                } catch (SocketException e3) {
                    se = e3;
                    ap = ap2;
                    Log.d(TAG, "err :" + se);
                    if (ap != null) {
                        ap.close();
                    }
                    return retVal;
                } catch (Throwable th2) {
                    th = th2;
                    ap = ap2;
                    if (ap != null) {
                        ap.close();
                    }
                    throw th;
                }
            } catch (ErrnoException e4) {
                ee = e4;
                try {
                    Log.d(TAG, "err :" + ee);
                    if (ap != null) {
                        ap.close();
                    }
                    return retVal;
                } catch (Throwable th3) {
                    th = th3;
                    if (ap != null) {
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e5) {
                ie = e5;
                Log.d(TAG, "err :" + ie);
                if (ap != null) {
                }
                return retVal;
            } catch (SocketException e6) {
                se = e6;
                Log.d(TAG, "err :" + se);
                if (ap != null) {
                }
                return retVal;
            }
            return retVal;
        }
    }

    private String logDumpIpv4(int postAmount, String ipv4) {
        if (ipv4 == null) {
            return null;
        }
        String[] octets = ipv4.split("\\.");
        if (octets.length != 4) {
            return ipv4;
        }
        StringBuilder builder = new StringBuilder(16);
        int i = 4 - postAmount;
        while (i < 4) {
            try {
                if (octets[i].length() > 3) {
                    return ipv4;
                }
                builder.append(Integer.parseInt(octets[i]));
                if (i < 3) {
                    builder.append('K');
                }
                i++;
            } catch (NumberFormatException e) {
                return ipv4;
            }
        }
        return builder.toString();
    }

    public static void clearOffer() {
        mOfferCleared.set(true);
    }
}
