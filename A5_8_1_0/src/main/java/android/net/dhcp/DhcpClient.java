package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.dhcp.DhcpPacket.ParseException;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.IpConnectivityLog;
import android.net.netlink.StructNlMsgHdr;
import android.net.util.NetworkConstants;
import android.os.Build.VERSION;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.display.OppoBrightUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoBridge;

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
    public static final int CMD_START_DHCP_RAPID_COMMIT = 196618;
    public static final int CMD_STOP_DHCP = 196610;
    private static final int CMD_TIMEOUT = 196711;
    private static final boolean DBG = true;
    public static final int DHCP_FAILURE = 2;
    private static final int DHCP_RENEW_TIMEOUT_MS = 8000;
    public static final int DHCP_SUCCESS = 1;
    private static final int DHCP_TIMEOUT_MS = 36000;
    private static final boolean DO_UNICAST = false;
    public static final int EVENT_LINKADDRESS_CONFIGURED = 196617;
    private static final int FIRST_TIMEOUT_MS = 250;
    private static final int MAX_TIMEOUT_MS = 128000;
    private static final boolean MSG_DBG = true;
    private static final boolean PACKET_DBG = false;
    private static final int PRIVATE_BASE = 196708;
    private static final int PUBLIC_BASE = 196608;
    static final byte[] REQUESTED_PARAMS = new byte[]{(byte) 1, (byte) 3, (byte) 6, (byte) 15, (byte) 26, (byte) 28, (byte) 51, (byte) 58, (byte) 59, (byte) 43};
    private static final int SECONDS = 1000;
    private static final boolean STATE_DBG = true;
    private static final String TAG = "DhcpClient";
    private static long mDhcpLeaseExpiry;
    private static boolean mFastRequest = false;
    private static DhcpResults mOffer;
    private static AtomicBoolean mOfferCleared = new AtomicBoolean(false);
    private static final Class[] sMessageClasses = new Class[]{DhcpClient.class};
    private static final SparseArray<String> sMessageNames = MessageUtils.findMessageNames(sMessageClasses);
    private byte[] OPPO_REQUESTED_PARAMS;
    private State mConfiguringInterfaceState = new ConfiguringInterfaceState();
    private final Context mContext;
    private final StateMachine mController;
    private State mDhcpBoundState = new DhcpBoundState();
    private State mDhcpHaveLeaseState = new DhcpHaveLeaseState();
    private State mDhcpInitRebootState = new DhcpInitRebootState();
    private State mDhcpInitState = new DhcpInitState();
    private DhcpResults mDhcpLease;
    private HashMap<String, String> mDhcpOptional = new HashMap();
    private State mDhcpRapidCommitInitState = new DhcpRapidCommitInitState();
    private State mDhcpRebindingState = new DhcpRebindingState();
    private State mDhcpRebootingState = new DhcpRebootingState();
    private State mDhcpRenewingState = new DhcpRenewingState();
    private State mDhcpRequestingState = new DhcpRequestingState();
    private State mDhcpSelectingState = new DhcpSelectingState();
    private State mDhcpState = new DhcpState();
    public boolean mDiscoverSent;
    private final WakeupMessage mExpiryAlarm;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private final String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private final WakeupMessage mKickAlarm;
    private long mLastBoundExitTime;
    private long mLastInitEnterTime;
    private final IpConnectivityLog mMetricsLog = new IpConnectivityLog();
    private boolean mOppoDhcpOptionalOn = true;
    private FileDescriptor mPacketSock;
    private final Random mRandom;
    public boolean mRapidCommit;
    private State mRapidCommitWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpRapidCommitInitState);
    private final WakeupMessage mRebindAlarm;
    private ReceiveThread mReceiveThread;
    private boolean mRegisteredForPreDhcpNotification;
    private final WakeupMessage mRenewAlarm;
    private boolean mRewnewForRoaming = false;
    private State mStoppedState = new StoppedState();
    private final WakeupMessage mTimeoutAlarm;
    private int mTransactionId;
    private long mTransactionStartMillis;
    private FileDescriptor mUdpSock;
    private State mWaitBeforeRenewalState = new WaitBeforeRenewalState(this.mDhcpRenewingState);
    private State mWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpInitState);
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

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
            if (!(DhcpClient.this.mDhcpLease.serverAddress == null || (DhcpClient.this.connectUdpSock(DhcpClient.this.mDhcpLease.serverAddress) ^ 1) == 0)) {
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
            this.mTimer = DhcpClient.FIRST_TIMEOUT_MS;
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
            this.mTimeout = DhcpClient.DHCP_TIMEOUT_MS;
            if (DhcpClient.mOffer == null || (DhcpClient.mOfferCleared.get() ^ 1) == 0 || SystemClock.elapsedRealtime() >= DhcpClient.mDhcpLeaseExpiry) {
                DhcpClient.mFastRequest = false;
                synchronized (DhcpClient.mOfferCleared) {
                    DhcpClient.mOfferCleared.set(false);
                }
                super.enter();
                DhcpClient.this.startNewTransaction();
            } else {
                Log.d(DhcpClient.TAG, "DhcpInitState mOffer != null, go and Request.");
                this.mTimer = DhcpClient.FIRST_TIMEOUT_MS;
                DhcpClient.mFastRequest = true;
                DhcpClient.this.startNewTransaction();
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
            }
            DhcpClient.this.mLastInitEnterTime = SystemClock.elapsedRealtime();
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
            DhcpClient.this.notifyFailure();
            DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
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

    class DhcpRapidCommitInitState extends PacketRetransmittingState {
        public DhcpRapidCommitInitState() {
            super();
        }

        public void enter() {
            super.enter();
            if (!DhcpClient.this.mDiscoverSent) {
                DhcpClient.this.startNewTransaction();
            }
            DhcpClient.this.mLastInitEnterTime = SystemClock.elapsedRealtime();
        }

        protected boolean sendPacket() {
            if (!DhcpClient.this.mDiscoverSent) {
                return DhcpClient.this.sendDiscoverPacket();
            }
            DhcpClient.this.mDiscoverSent = false;
            return true;
        }

        protected void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet)) {
                if (packet instanceof DhcpOfferPacket) {
                    DhcpClient.mOffer = packet.toDhcpResults();
                    if (DhcpClient.mOffer != null) {
                        Log.d(DhcpClient.TAG, "DhcpRapidCommitInitState:Got pending lease: " + DhcpClient.mOffer);
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
                    }
                } else if (packet instanceof DhcpAckPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    Log.d(DhcpClient.TAG, "Received ACK in DhcpRapidCommitInitState");
                    if (results != null) {
                        DhcpClient.this.setDhcpLeaseExpiry(packet);
                        DhcpClient.this.acceptDhcpResults(results, "Confirmed");
                        DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                    }
                } else if (packet instanceof DhcpNakPacket) {
                    Log.d(DhcpClient.TAG, "Received NAK in DhcpRapidCommitInitState, returning to INIT");
                    DhcpClient.mOffer = null;
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
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
                    DhcpClient.this.notifyFailure();
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }

        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            switch (message.what) {
                case DhcpClient.CMD_RENEW_AFTER_ROAMING /*196629*/:
                    DhcpClient.this.deferMessage(message);
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                    return true;
                default:
                    return false;
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
                DhcpClient.mOffer = null;
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
        private final byte[] mPacket = new byte[NetworkConstants.ETHER_MTU];
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
            android.net.dhcp.DhcpClient.-wrap10(r13.this$0, android.net.metrics.DhcpErrorEvent.RECEIVE_ERROR);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Log.d(DhcpClient.TAG, "Receive thread started");
            while (!this.mStopped) {
                try {
                    DhcpPacket packet = DhcpPacket.decodeFullPacket(this.mPacket, Os.read(DhcpClient.this.mPacketSock, this.mPacket, 0, this.mPacket.length), 0);
                    Log.d(DhcpClient.TAG, "Received packet: " + packet);
                    DhcpClient.this.sendMessage(DhcpClient.CMD_RECEIVED_PACKET, packet);
                } catch (Exception e) {
                } catch (ParseException e2) {
                    Log.e(DhcpClient.TAG, "Can't parse packet: " + e2.getMessage());
                    if (e2.errorCode == DhcpErrorEvent.DHCP_NO_COOKIE) {
                        String data = ParseException.class.getName();
                        EventLog.writeEvent(1397638484, new Object[]{"31850211", Integer.valueOf(-1), data});
                    }
                    DhcpClient.this.logError(e2.errorCode);
                }
            }
            Log.d(DhcpClient.TAG, "Receive thread stopped");
        }
    }

    class StoppedState extends State {
        StoppedState() {
        }

        public boolean processMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case DhcpClient.CMD_START_DHCP /*196609*/:
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    }
                    return true;
                case DhcpClient.CMD_START_DHCP_RAPID_COMMIT /*196618*/:
                    DhcpClient.this.mRapidCommit = message.arg1 == 1;
                    DhcpClient dhcpClient = DhcpClient.this;
                    if (message.arg2 == 1) {
                        z = true;
                    }
                    dhcpClient.mDiscoverSent = z;
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        if (DhcpClient.this.mRapidCommit) {
                            DhcpClient.this.transitionTo(DhcpClient.this.mRapidCommitWaitBeforeStartState);
                        } else {
                            DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
                        }
                    } else if (DhcpClient.this.mRapidCommit) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRapidCommitInitState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
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
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE /*196614*/:
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

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        return new WakeupMessage(this.mContext, getHandler(), DhcpClient.class.getSimpleName() + "." + this.mIfaceName + "." + cmdName, cmd);
    }

    private DhcpClient(Context context, StateMachine controller, String iface) {
        super(TAG);
        this.mContext = context;
        this.mController = controller;
        this.mIfaceName = iface;
        addState(this.mStoppedState);
        addState(this.mDhcpState);
        addState(this.mDhcpInitState, this.mDhcpState);
        addState(this.mDhcpRapidCommitInitState, this.mDhcpState);
        addState(this.mWaitBeforeStartState, this.mDhcpState);
        addState(this.mRapidCommitWaitBeforeStartState, this.mDhcpState);
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
        this.mWifiRomUpdateHelper = new WifiRomUpdateHelper(this.mContext);
        if (this.mWifiRomUpdateHelper != null) {
            getOppoDhcpOptional();
        }
    }

    private void getOppoDhcpOptional() {
        Object oppoDhcpHostName;
        Object oppoDhcpVendorClassId;
        ArrayList<Byte> tmpDhcpParameterList = new ArrayList();
        this.mOppoDhcpOptionalOn = this.mWifiRomUpdateHelper.getBooleanValue(WifiRomUpdateHelper.OPPO_DHCP_OPTIONAL_ON, true);
        this.mDhcpOptional.put(WifiRomUpdateHelper.OPPO_DHCP_OPTIONAL_ON, this.mWifiRomUpdateHelper.getValue(WifiRomUpdateHelper.OPPO_DHCP_OPTIONAL_ON, "true"));
        String oppoDhcpHostNameRU = this.mWifiRomUpdateHelper.getValue(WifiRomUpdateHelper.OPPO_DHCP_HOST_NAME, "default");
        if ("default".equals(oppoDhcpHostNameRU)) {
            oppoDhcpHostName = SystemProperties.get("ro.oppo.market.enname", SystemProperties.get("ro.oppo.market.name", "OPPO"));
            if (!TextUtils.isEmpty(oppoDhcpHostName)) {
                oppoDhcpHostName = oppoDhcpHostName.replace(" ", "-").replace("\"", "");
            }
        } else if ("close".equals(oppoDhcpHostNameRU)) {
            oppoDhcpHostName = null;
        } else {
            String oppoDhcpHostName2 = oppoDhcpHostNameRU;
        }
        this.mDhcpOptional.put(WifiRomUpdateHelper.OPPO_DHCP_HOST_NAME, oppoDhcpHostName2);
        String oppoDhcpParameterListRU = this.mWifiRomUpdateHelper.getValue(WifiRomUpdateHelper.OPPO_DHCP_PARAMETER_LIST, "default");
        if ("default".equals(oppoDhcpParameterListRU)) {
            this.OPPO_REQUESTED_PARAMS = REQUESTED_PARAMS;
        } else if (TextUtils.isEmpty(oppoDhcpParameterListRU)) {
            this.OPPO_REQUESTED_PARAMS = REQUESTED_PARAMS;
        } else {
            for (String dhcpParameter : oppoDhcpParameterListRU.split(",")) {
                try {
                    tmpDhcpParameterList.add(Byte.valueOf(Byte.parseByte(dhcpParameter)));
                } catch (NumberFormatException ex) {
                    Log.d(TAG, "parse exception:" + ex);
                }
            }
            if (tmpDhcpParameterList.size() != 0) {
                Iterator<Byte> iterator = tmpDhcpParameterList.iterator();
                byte[] tmpBytes = new byte[tmpDhcpParameterList.size()];
                int i = 0;
                while (iterator.hasNext()) {
                    tmpBytes[i] = ((Byte) iterator.next()).byteValue();
                    i++;
                }
                this.OPPO_REQUESTED_PARAMS = tmpBytes;
            } else {
                this.OPPO_REQUESTED_PARAMS = REQUESTED_PARAMS;
            }
        }
        String oppoDhcpVendorClassIdRU = this.mWifiRomUpdateHelper.getValue(WifiRomUpdateHelper.OPPO_DHCP_VENDOR_CLASS_ID, "default");
        if ("default".equals(oppoDhcpVendorClassIdRU)) {
            oppoDhcpVendorClassId = "android-dhcp-" + VERSION.RELEASE;
        } else if ("close".equals(oppoDhcpVendorClassIdRU)) {
            oppoDhcpVendorClassId = null;
        } else {
            String oppoDhcpVendorClassId2 = oppoDhcpVendorClassIdRU;
        }
        this.mDhcpOptional.put(WifiRomUpdateHelper.OPPO_DHCP_VENDOR_CLASS_ID, oppoDhcpVendorClassId2);
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
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
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

    /* JADX WARNING: Removed duplicated region for block: B:4:0x004f A:{Splitter: B:1:0x0008, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:?, code:
            android.util.Log.e(TAG, "Error creating UDP socket", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initUdpSocket() {
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-192);
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
        } finally {
            TrafficStats.setThreadStatsTag(oldTag);
        }
        return false;
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
            Log.d(TAG, String.format("Unicasting %s to %s", new Object[]{description, Os.getpeername(this.mUdpSock)}));
            Os.write(this.mUdpSock, buf);
        }
        return true;
    }

    public ByteBuffer buildDiscoverWithRapidCommitPacket() {
        startNewTransaction();
        if (this.mOppoDhcpOptionalOn) {
            return DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, this.OPPO_REQUESTED_PARAMS, this.mRapidCommit, this.mDhcpOptional);
        }
        return DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, REQUESTED_PARAMS, this.mRapidCommit);
    }

    private boolean sendDiscoverPacket() {
        ByteBuffer packet;
        if (this.mOppoDhcpOptionalOn) {
            packet = DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, this.OPPO_REQUESTED_PARAMS, this.mRapidCommit, this.mDhcpOptional);
        } else {
            packet = DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, REQUESTED_PARAMS, this.mRapidCommit);
        }
        return transmitPacket(packet, "DHCPDISCOVER", 0, DhcpPacket.INADDR_BROADCAST);
    }

    private boolean sendRequestPacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        ByteBuffer packet;
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        if (this.mOppoDhcpOptionalOn) {
            packet = DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, this.OPPO_REQUESTED_PARAMS, null, this.mDhcpOptional);
        } else {
            packet = DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null);
        }
        return transmitPacket(packet, "DHCPREQUEST ciaddr=" + clientAddress.getHostAddress() + " request=" + requestedAddress.getHostAddress() + " serverid=" + (serverAddress != null ? serverAddress.getHostAddress() : null), encap, to);
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
            Log.d(TAG, "MAC addr mismatch: got " + HexDump.toHexString(packet.getClientMac()) + ", expected " + HexDump.toHexString(packet.getClientMac()));
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

    public static void clearOffer() {
        synchronized (mOfferCleared) {
            mOfferCleared.set(true);
        }
    }

    private void logError(int errorCode) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpErrorEvent(errorCode));
    }

    private void logState(String name, int durationMs) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpClientEvent(name, durationMs));
    }
}
