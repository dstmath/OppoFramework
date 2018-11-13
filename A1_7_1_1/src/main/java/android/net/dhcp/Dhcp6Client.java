package android.net.dhcp;

import android.app.AlarmManager;
import android.content.Context;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.metrics.DhcpErrorEvent;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.display.OppoBrightUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Random;
import java.util.TimeZone;
import libcore.io.IoBridge;

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
public class Dhcp6Client extends StateMachine {
    private static final int BASE = 196708;
    public static final int CMD_CLEAR_LINKADDRESS = 196815;
    public static final int CMD_CONFIGURE_DNSV6 = 196816;
    private static final int CMD_KICK = 196709;
    public static final int CMD_ON_QUIT = 196813;
    private static final int CMD_POLL_CHECK = 196713;
    public static final int CMD_POST_DHCP_ACTION = 196812;
    public static final int CMD_PRE_DHCP_ACTION = 196811;
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE = 196814;
    private static final int CMD_RECEIVED_PACKET = 196710;
    private static final int CMD_RENEW_DHCP = 196712;
    public static final int CMD_START_DHCP = 196809;
    public static final int CMD_STOP_DHCP = 196810;
    private static final int CMD_TIMEOUT = 196711;
    private static final boolean DBG = true;
    private static final int DHCP_POLL_TOTAL_COUNTER = 8;
    private static final int DHCP_TIMEOUT_MS = 36000;
    private static final boolean DO_UNICAST = false;
    public static final int EVENT_LINKADDRESS_CONFIGURED = 196817;
    private static final int FIRST_TIMEOUT_MS = 2000;
    private static final int MAX_TIMEOUT_MS = 128000;
    private static final boolean MSG_DBG = true;
    private static final boolean PACKET_DBG = true;
    private static final int PUBLIC_BASE = 196808;
    private static final short[] REQUESTED_PARAMS = null;
    private static final int SECONDS = 1000;
    private static final int STATEFUL_DHCPV6 = 2;
    private static final int STATELESS_DHCPV6 = 1;
    private static final boolean STATE_DBG = true;
    private static final String TAG = "Dhcp6Client";
    private static final Class[] sMessageClasses = null;
    private static final SparseArray<String> sMessageNames = null;
    private static byte[] sTimeStamp;
    private final AlarmManager mAlarmManager;
    private final Context mContext;
    private final StateMachine mController;
    private State mDhcpBoundState;
    private State mDhcpCheckState;
    private State mDhcpHaveAddressState;
    private State mDhcpInitRebootState;
    private State mDhcpInitState;
    private DhcpResults mDhcpLease;
    private long mDhcpLeaseExpiry;
    private int mDhcpRaFlagPollCount;
    private State mDhcpRebindingState;
    private State mDhcpRebootingState;
    private State mDhcpRenewingState;
    private State mDhcpRequestingState;
    private State mDhcpSelectingState;
    private int mDhcpServerType;
    private State mDhcpState;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private final String mIfaceName;
    private final WakeupMessage mKickAlarm;
    private final INetworkManagementService mNMService;
    private DhcpResults mOffer;
    private final Random mRandom;
    private ReceiveThread mReceiveThread;
    private boolean mRegisteredForPreDhcpNotification;
    private final WakeupMessage mRenewAlarm;
    private byte[] mServerIdentifier;
    private Inet6Address mServerIpAddress;
    private State mStoppedState;
    private final WakeupMessage mTimeoutAlarm;
    private byte[] mTransactionId;
    private long mTransactionStartMillis;
    private FileDescriptor mUdpSock;
    private State mWaitBeforeRenewalState;
    private State mWaitBeforeStartState;

    abstract class LoggingState extends State {
        LoggingState() {
        }

        public void enter() {
            Log.d(Dhcp6Client.TAG, "Entering state " + getName());
        }

        private String messageName(int what) {
            return (String) Dhcp6Client.sMessageNames.get(what, Integer.toString(what));
        }

        private String messageToString(Message message) {
            long now = SystemClock.uptimeMillis();
            StringBuilder b = new StringBuilder(" ");
            TimeUtils.formatDuration(message.getWhen() - now, b);
            b.append(" ").append(messageName(message.what)).append(" ").append(message.arg1).append(" ").append(message.arg2).append(" ").append(message.obj);
            return b.toString();
        }

        public boolean processMessage(Message message) {
            Log.d(Dhcp6Client.TAG, getName() + messageToString(message));
            return false;
        }
    }

    class DhcpBoundState extends LoggingState {
        DhcpBoundState() {
            super();
        }

        public void enter() {
            super.enter();
            if (Dhcp6Client.this.mDhcpServerType == 2) {
                Dhcp6Client.this.scheduleRenew();
            }
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_RENEW_DHCP /*196712*/:
                    if (Dhcp6Client.this.mRegisteredForPreDhcpNotification) {
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mWaitBeforeRenewalState);
                    } else {
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpRenewingState);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    class DhcpCheckState extends LoggingState {
        boolean mIsPreDhcpComplete;

        DhcpCheckState() {
            super();
        }

        public void enter() {
            super.enter();
            this.mIsPreDhcpComplete = false;
            Dhcp6Client.this.mDhcpRaFlagPollCount = 8;
            Dhcp6Client.this.checkDhcp6Support(this.mIsPreDhcpComplete);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_POLL_CHECK /*196713*/:
                    Dhcp6Client.this.checkDhcp6Support(this.mIsPreDhcpComplete);
                    return true;
                case Dhcp6Client.CMD_STOP_DHCP /*196810*/:
                    Dhcp6Client.this.transitionTo(Dhcp6Client.this.mStoppedState);
                    return true;
                case Dhcp6Client.CMD_PRE_DHCP_ACTION_COMPLETE /*196814*/:
                    this.mIsPreDhcpComplete = true;
                    return true;
                default:
                    return false;
            }
        }
    }

    class DhcpHaveAddressState extends LoggingState {
        DhcpHaveAddressState() {
            super();
        }

        public void enter() {
            super.enter();
            if (Dhcp6Client.this.setIpAddress(Dhcp6Client.this.mDhcpLease.ipAddress)) {
                Log.d(Dhcp6Client.TAG, "Configured IPv6 address " + Dhcp6Client.this.mDhcpLease.ipAddress);
                if (Dhcp6Client.this.mDhcpLease.dnsServers != null) {
                    Dhcp6Client.this.mController.sendMessage(Dhcp6Client.CMD_CONFIGURE_DNSV6, 0, 0, Dhcp6Client.this.mDhcpLease.dnsServers);
                    return;
                }
                return;
            }
            Log.e(Dhcp6Client.TAG, "Failed to configure IPv6 address " + Dhcp6Client.this.mDhcpLease.ipAddress);
            Dhcp6Client.this.transitionTo(Dhcp6Client.this.mStoppedState);
        }

        public void exit() {
            Log.d(Dhcp6Client.TAG, "Clearing IPv6 address");
        }
    }

    class DhcpInitRebootState extends LoggingState {
        DhcpInitRebootState() {
            super();
        }
    }

    abstract class PacketRetransmittingState extends LoggingState {
        protected int mTimeout = 0;
        private int mTimer;

        protected abstract void receivePacket(Dhcp6Packet dhcp6Packet);

        protected abstract boolean sendPacket();

        PacketRetransmittingState() {
            super();
        }

        public void enter() {
            super.enter();
            initTimer();
            maybeInitTimeout();
            Dhcp6Client.this.sendMessage(Dhcp6Client.CMD_KICK);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_KICK /*196709*/:
                    try {
                        sendPacket();
                        scheduleKick();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                case Dhcp6Client.CMD_RECEIVED_PACKET /*196710*/:
                    try {
                        receivePacket((Dhcp6Packet) message.obj);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    return true;
                case Dhcp6Client.CMD_TIMEOUT /*196711*/:
                    try {
                        timeout();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Dhcp6Client.this.mKickAlarm.cancel();
            Dhcp6Client.this.mTimeoutAlarm.cancel();
        }

        protected void timeout() {
        }

        protected void initTimer() {
            this.mTimer = Dhcp6Client.FIRST_TIMEOUT_MS;
        }

        protected int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            return baseTimer + (Dhcp6Client.this.mRandom.nextInt(maxJitter * 2) - maxJitter);
        }

        protected void scheduleKick() {
            Dhcp6Client.this.mKickAlarm.schedule(SystemClock.elapsedRealtime() + ((long) jitterTimer(this.mTimer)));
            this.mTimer *= 2;
            if (this.mTimer > Dhcp6Client.MAX_TIMEOUT_MS) {
                this.mTimer = Dhcp6Client.MAX_TIMEOUT_MS;
            }
        }

        protected void maybeInitTimeout() {
            if (this.mTimeout > 0) {
                Dhcp6Client.this.mTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeout));
            }
        }
    }

    class DhcpInitState extends PacketRetransmittingState {
        public DhcpInitState() {
            super();
        }

        public void enter() {
            super.enter();
            Dhcp6Client.this.startNewTransaction();
        }

        protected boolean sendPacket() {
            if (Dhcp6Client.this.mDhcpServerType == 2) {
                return Dhcp6Client.this.sendSolicitPacket();
            }
            if (Dhcp6Client.this.mDhcpServerType == 2) {
                return Dhcp6Client.this.sendInfoRequestPacket();
            }
            return false;
        }

        protected void receivePacket(Dhcp6Packet packet) {
            if (Dhcp6Client.this.isValidPacket(packet)) {
                if (Dhcp6Client.this.mDhcpServerType == 2) {
                    if (packet instanceof Dhcp6AdvertisePacket) {
                        Dhcp6Client.this.mOffer = packet.toDhcpResults();
                        if (Dhcp6Client.this.mOffer != null) {
                            Dhcp6Client.this.mServerIdentifier = packet.mServerIdentifier;
                            Dhcp6Client.this.mServerIpAddress = packet.mServerAddress;
                            Log.d(Dhcp6Client.TAG, "Got pending lease");
                            if (Dhcp6Client.this.mOffer.dnsServers.size() != 0) {
                                Dhcp6Client.this.mController.sendMessage(Dhcp6Client.CMD_CONFIGURE_DNSV6, 0, 0, Dhcp6Client.this.mOffer.dnsServers);
                            }
                            Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpRequestingState);
                        }
                    }
                } else if (packet instanceof Dhcp6ReplyPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        Dhcp6Client.this.mDhcpLease = results;
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpBoundState);
                    }
                }
            }
        }
    }

    class DhcpRebindingState extends LoggingState {
        DhcpRebindingState() {
            super();
        }
    }

    class DhcpRebootingState extends LoggingState {
        DhcpRebootingState() {
            super();
        }
    }

    class DhcpRenewingState extends PacketRetransmittingState {
        public DhcpRenewingState() {
            super();
            this.mTimeout = Dhcp6Client.DHCP_TIMEOUT_MS;
        }

        public void enter() {
            super.enter();
            Dhcp6Client.this.startNewTransaction();
        }

        protected boolean sendPacket() {
            return Dhcp6Client.this.sendRequestPacket((Inet6Address) Dhcp6Client.this.mDhcpLease.ipAddress.getAddress(), Dhcp6Packet.INADDR_ANY, Dhcp6Packet.INADDR_ANY, Dhcp6Client.this.mServerIpAddress);
        }

        protected void receivePacket(Dhcp6Packet packet) {
            if (Dhcp6Client.this.isValidPacket(packet)) {
                if (packet instanceof Dhcp6ReplyPacket) {
                    Dhcp6Client.this.setDhcpLeaseExpiry(packet);
                    Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpBoundState);
                } else if (packet instanceof Dhcp6NakPacket) {
                    Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpInitState);
                }
            }
        }

        protected void timeout() {
            Dhcp6Client.this.transitionTo(Dhcp6Client.this.mStoppedState);
        }
    }

    class DhcpRequestingState extends PacketRetransmittingState {
        public DhcpRequestingState() {
            super();
            this.mTimeout = OppoBrightUtils.HIGH_BRIGHTNESS_DEBOUNCE_LUX;
        }

        protected boolean sendPacket() {
            return Dhcp6Client.this.sendRequestPacket(Dhcp6Packet.INADDR_ANY, (Inet6Address) Dhcp6Client.this.mOffer.ipAddress.getAddress(), Dhcp6Client.this.mServerIpAddress, Dhcp6Packet.INADDR_BROADCAST_ROUTER);
        }

        protected void receivePacket(Dhcp6Packet packet) {
            if (Dhcp6Client.this.isValidPacket(packet)) {
                if (packet instanceof Dhcp6ReplyPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        Dhcp6Client.this.mDhcpLease = results;
                        if (Dhcp6Client.this.mDhcpLease.dnsServers.size() == 0 && Dhcp6Client.this.mOffer.dnsServers.size() != 0) {
                            Log.d(Dhcp6Client.TAG, "Get DNS server address from Advertise message");
                            Dhcp6Client.this.mDhcpLease.dnsServers.addAll(Dhcp6Client.this.mOffer.dnsServers);
                        }
                        Dhcp6Client.this.mOffer = null;
                        Dhcp6Client.this.mServerIdentifier = packet.mServerIdentifier;
                        Dhcp6Client.this.mServerIpAddress = packet.mServerAddress;
                        Log.d(Dhcp6Client.TAG, "Confirmed lease: " + Dhcp6Client.this.mDhcpLease);
                        Dhcp6Client.this.setDhcpLeaseExpiry(packet);
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpBoundState);
                    }
                } else if (packet instanceof Dhcp6NakPacket) {
                    Log.d(Dhcp6Client.TAG, "Received NAK, returning to INIT");
                    Dhcp6Client.this.mOffer = null;
                    Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpInitState);
                }
            }
        }

        protected void timeout() {
            Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpInitState);
        }
    }

    class DhcpSelectingState extends LoggingState {
        DhcpSelectingState() {
            super();
        }
    }

    class DhcpState extends LoggingState {
        DhcpState() {
            super();
        }

        public void enter() {
            super.enter();
            Dhcp6Client.this.clearDhcpState();
            if (Dhcp6Client.this.initInterface() && Dhcp6Client.this.initSockets()) {
                Dhcp6Client.this.mReceiveThread = new ReceiveThread();
                Dhcp6Client.this.mReceiveThread.start();
                return;
            }
            Dhcp6Client.this.transitionTo(Dhcp6Client.this.mStoppedState);
        }

        public void exit() {
            if (Dhcp6Client.this.mReceiveThread != null) {
                Dhcp6Client.this.mReceiveThread.halt();
                Dhcp6Client.this.mReceiveThread = null;
            }
            Dhcp6Client.this.clearDhcpState();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_STOP_DHCP /*196810*/:
                    Dhcp6Client.this.transitionTo(Dhcp6Client.this.mStoppedState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class ReceiveThread extends Thread {
        private final byte[] mPacket = new byte[1500];
        private boolean stopped = false;

        ReceiveThread() {
        }

        public void halt() {
            this.stopped = true;
            Dhcp6Client.this.closeSockets();
        }

        public void run() {
            Log.d(Dhcp6Client.TAG, "Receive thread started");
            while (!this.stopped) {
                try {
                    int length = Os.read(Dhcp6Client.this.mUdpSock, this.mPacket, 0, this.mPacket.length);
                    Dhcp6Packet packet = Dhcp6Packet.decodeFullPacket(this.mPacket, length);
                    if (packet != null) {
                        Log.d(Dhcp6Client.TAG, "Received packet: " + packet);
                        Dhcp6Client.this.sendMessage(Dhcp6Client.CMD_RECEIVED_PACKET, packet);
                    } else {
                        Log.d(Dhcp6Client.TAG, "Can't parse packet" + HexDump.dumpHexString(this.mPacket, 0, length));
                    }
                } catch (Exception e) {
                    if (!this.stopped) {
                        Log.e(Dhcp6Client.TAG, "Read error", e);
                        DhcpErrorEvent.logReceiveError("v6:" + Dhcp6Client.this.mIfaceName);
                    }
                }
            }
            Log.d(Dhcp6Client.TAG, "Receive thread stopped");
        }
    }

    class StoppedState extends LoggingState {
        StoppedState() {
            super();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_START_DHCP /*196809*/:
                    if (Dhcp6Client.this.mRegisteredForPreDhcpNotification) {
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpCheckState);
                    } else {
                        Dhcp6Client.this.transitionTo(Dhcp6Client.this.mDhcpInitState);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    abstract class WaitBeforeOtherState extends LoggingState {
        protected State mOtherState;

        WaitBeforeOtherState() {
            super();
        }

        public void enter() {
            super.enter();
            Dhcp6Client.this.mController.sendMessage(Dhcp6Client.CMD_PRE_DHCP_ACTION);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case Dhcp6Client.CMD_PRE_DHCP_ACTION_COMPLETE /*196814*/:
                    Dhcp6Client.this.transitionTo(this.mOtherState);
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
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.Dhcp6Client.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.Dhcp6Client.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.Dhcp6Client.<clinit>():void");
    }

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        return new WakeupMessage(this.mContext, getHandler(), DhcpClient.class.getSimpleName() + "." + this.mIfaceName + "." + cmdName, cmd);
    }

    private Dhcp6Client(Context context, StateMachine controller, String iface) {
        super(TAG);
        this.mStoppedState = new StoppedState();
        this.mDhcpCheckState = new DhcpCheckState();
        this.mDhcpState = new DhcpState();
        this.mDhcpInitState = new DhcpInitState();
        this.mDhcpSelectingState = new DhcpSelectingState();
        this.mDhcpRequestingState = new DhcpRequestingState();
        this.mDhcpHaveAddressState = new DhcpHaveAddressState();
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
        addState(this.mDhcpCheckState);
        addState(this.mDhcpState);
        addState(this.mDhcpInitState, this.mDhcpState);
        addState(this.mWaitBeforeStartState, this.mDhcpState);
        addState(this.mDhcpSelectingState, this.mDhcpState);
        addState(this.mDhcpRequestingState, this.mDhcpState);
        addState(this.mDhcpHaveAddressState, this.mDhcpState);
        addState(this.mDhcpBoundState, this.mDhcpHaveAddressState);
        addState(this.mWaitBeforeRenewalState, this.mDhcpHaveAddressState);
        addState(this.mDhcpRenewingState, this.mDhcpHaveAddressState);
        addState(this.mDhcpRebindingState, this.mDhcpHaveAddressState);
        addState(this.mDhcpInitRebootState, this.mDhcpState);
        addState(this.mDhcpRebootingState, this.mDhcpState);
        setInitialState(this.mStoppedState);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mNMService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.mRandom = new Random();
        this.mKickAlarm = makeWakeupMessage("KICK", CMD_KICK);
        this.mTimeoutAlarm = makeWakeupMessage("TIMEOUT", CMD_TIMEOUT);
        this.mRenewAlarm = makeWakeupMessage("RENEW", CMD_RENEW_DHCP);
    }

    public void registerForPreDhcpNotification() {
        this.mRegisteredForPreDhcpNotification = true;
    }

    public static Dhcp6Client makeDhcp6Client(Context context, StateMachine controller, String intf) {
        Dhcp6Client client = new Dhcp6Client(context, controller, intf);
        client.start();
        Log.i(TAG, "makeDhcp6Client");
        return client;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0012 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0012, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            android.util.Log.e(TAG, "Can't determine ifindex or MAC address for " + r4.mIfaceName, r0);
            android.util.Log.e(TAG, "mIface = " + r4.mIface);
     */
    /* JADX WARNING: Missing block: B:6:0x004c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            return true;
        } catch (Exception e) {
        }
    }

    private void startNewTransaction() {
        this.mTransactionId = intToByteArray(this.mRandom.nextInt());
        this.mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    private InetAddress getIpv6LinkLocalAddress(NetworkInterface iface) {
        Enumeration<InetAddress> ipAddres = iface.getInetAddresses();
        while (ipAddres.hasMoreElements()) {
            InetAddress inetAddress = (InetAddress) ipAddres.nextElement();
            if (inetAddress.isLinkLocalAddress()) {
                Log.i(TAG, "Source address:" + inetAddress);
                return inetAddress;
            }
        }
        return Inet6Address.ANY;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x003f A:{Splitter: B:1:0x0001, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x003f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0040, code:
            android.util.Log.e(TAG, "Error creating UDP socket", r0);
     */
    /* JADX WARNING: Missing block: B:6:0x004a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initSockets() {
        try {
            this.mUdpSock = Os.socket(OsConstants.AF_INET6, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_REUSEADDR, 1);
            Os.setsockoptIfreq(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfaceName);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BROADCAST, 1);
            Os.bind(this.mUdpSock, getIpv6LinkLocalAddress(this.mIface), 546);
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
    private boolean connectUdpSock(Inet6Address to) {
        try {
            Os.connect(this.mUdpSock, to, 547);
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
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0011 A:{Splitter: B:1:0x0008, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0011, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0012, code:
            android.util.Log.e(TAG, "Error configuring IP address : " + r0);
     */
    /* JADX WARNING: Missing block: B:7:0x002d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setIpAddress(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            this.mNMService.setInterfaceConfig(this.mIfaceName, ifcg);
            return true;
        } catch (Exception e) {
        }
    }

    private short getSecs() {
        return (short) ((int) ((SystemClock.elapsedRealtime() - this.mTransactionStartMillis) / 1000));
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x003e A:{Splitter: B:1:0x0001, ExcHandler: android.system.ErrnoException (r7_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x003e, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003f, code:
            android.util.Log.e(TAG, "Can't send packet: ", r7);
     */
    /* JADX WARNING: Missing block: B:7:0x0048, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean transmitPacket(ByteBuffer buf, String description, Inet6Address to) {
        try {
            Log.d(TAG, "Sending " + description + " to " + to.getHostAddress());
            Os.sendto(this.mUdpSock, buf.array(), 0, buf.limit(), 0, to, 547);
            return true;
        } catch (Exception e) {
        }
    }

    private boolean sendSolicitPacket() {
        return transmitPacket(Dhcp6Packet.buildSolicitPacket(this.mTransactionId, getSecs(), this.mHwAddr, REQUESTED_PARAMS), "DHCPSOLICIT", Dhcp6Packet.INADDR_BROADCAST_ROUTER);
    }

    private boolean sendInfoRequestPacket() {
        return transmitPacket(Dhcp6Packet.buildInfoRequestPacket(this.mTransactionId, getSecs(), this.mHwAddr, REQUESTED_PARAMS), "DHCP_INFO_REQUEST", Dhcp6Packet.INADDR_BROADCAST_ROUTER);
    }

    private boolean sendRequestPacket(Inet6Address clientAddress, Inet6Address requestedAddress, Inet6Address serverAddress, Inet6Address to) {
        return transmitPacket(Dhcp6Packet.buildRequestPacket(this.mTransactionId, getSecs(), clientAddress, this.mHwAddr, requestedAddress, this.mServerIdentifier, REQUESTED_PARAMS), "DHCPREQUEST  request=" + requestedAddress.getHostAddress(), Dhcp6Packet.INADDR_BROADCAST_ROUTER);
    }

    private void scheduleRenew() {
        if (this.mDhcpLeaseExpiry != 0) {
            long now = SystemClock.elapsedRealtime();
            long alarmTime = now + this.mDhcpLeaseExpiry;
            this.mRenewAlarm.schedule(alarmTime);
            Log.d(TAG, "Scheduling renewal in " + ((alarmTime - now) / 1000) + "s");
            return;
        }
        Log.d(TAG, "Infinite lease, no renewal needed");
    }

    private void clearDhcpState() {
        this.mDhcpLease = null;
        this.mDhcpLeaseExpiry = 0;
        this.mOffer = null;
    }

    public void doQuit() {
        Log.d(TAG, "doQuit");
        quit();
    }

    protected void onQuitting() {
        Log.d(TAG, "onQuitting");
        this.mController.sendMessage(CMD_ON_QUIT);
    }

    private boolean checkDhcp6Support(boolean isPreDhcpComplete) {
        int raFlags = NetworkUtils.getRaFlags(this.mIfaceName);
        if (raFlags == 2) {
            this.mDhcpServerType = raFlags;
            if (isPreDhcpComplete) {
                transitionTo(this.mDhcpInitState);
            } else {
                transitionTo(this.mWaitBeforeStartState);
            }
        } else if (raFlags == 1) {
            return false;
        } else {
            this.mDhcpRaFlagPollCount--;
            if (this.mDhcpRaFlagPollCount > 0) {
                sendMessageDelayed(CMD_POLL_CHECK, 1000);
            } else {
                Log.i(TAG, "No DHCPv6 support:" + raFlags);
                transitionTo(this.mStoppedState);
            }
        }
        return false;
    }

    public boolean isValidPacket(Dhcp6Packet packet) {
        byte[] xid = packet.getTransactionId();
        if (!Arrays.equals(xid, this.mTransactionId)) {
            Log.d(TAG, "Unexpected transaction ID " + HexDump.toHexString(xid) + ", expected " + HexDump.toHexString(this.mTransactionId));
            return false;
        } else if (packet.getClientMac() != null && Arrays.equals(packet.getClientMac(), this.mHwAddr)) {
            return true;
        } else {
            Log.d(TAG, "MAC addr mismatch: got " + HexDump.toHexString(packet.getClientMac()) + ", expected " + HexDump.toHexString(this.mHwAddr));
            return false;
        }
    }

    public void setDhcpLeaseExpiry(Dhcp6Packet packet) {
        long j = 0;
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        if (leaseTimeMillis > 0) {
            j = SystemClock.elapsedRealtime() + leaseTimeMillis;
        }
        this.mDhcpLeaseExpiry = j;
    }

    private static final byte[] intToByteArray(int value) {
        byte[] bArr = new byte[3];
        bArr[0] = (byte) (value >>> 16);
        bArr[1] = (byte) (value >>> 8);
        bArr[2] = (byte) value;
        return bArr;
    }

    public static byte[] getTimeStamp() {
        if (sTimeStamp == null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.set(FIRST_TIMEOUT_MS, 0, 1, 0, 0, 0);
            Long offset = Long.valueOf(((Long.valueOf(Calendar.getInstance().getTimeInMillis()).longValue() - Long.valueOf(cal.getTimeInMillis()).longValue()) / 1000) % 4294967296L);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.clear();
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(offset.intValue());
            sTimeStamp = buffer.array();
        }
        return sTimeStamp;
    }
}
