package android.net.apf;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.apf.ApfGenerator.IllegalInstructionException;
import android.net.apf.ApfGenerator.Register;
import android.net.ip.IpManager.Callback;
import android.net.metrics.ApfProgramEvent;
import android.net.metrics.ApfStats;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.RaEvent.Builder;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ApfFilter {
    private static final int ARP_HEADER_OFFSET = 14;
    private static final byte[] ARP_IPV4_HEADER = null;
    private static final int ARP_OPCODE_OFFSET = 20;
    private static final short ARP_OPCODE_REPLY = (short) 2;
    private static final short ARP_OPCODE_REQUEST = (short) 1;
    private static final int ARP_TARGET_IP_ADDRESS_OFFSET = 38;
    private static final boolean DBG = true;
    private static final int DHCP_CLIENT_MAC_OFFSET = 50;
    private static final int DHCP_CLIENT_PORT = 68;
    private static final byte[] ETH_BROADCAST_MAC_ADDRESS = null;
    private static final int ETH_DEST_ADDR_OFFSET = 0;
    private static final int ETH_ETHERTYPE_OFFSET = 12;
    private static final int ETH_HEADER_LEN = 14;
    private static final int FRACTION_OF_LIFETIME_TO_FILTER = 6;
    private static final int ICMP6_NEIGHBOR_ANNOUNCEMENT = 136;
    private static final int ICMP6_ROUTER_ADVERTISEMENT = 134;
    private static final int ICMP6_TYPE_OFFSET = 54;
    private static final int IPV4_ANY_HOST_ADDRESS = 0;
    private static final int IPV4_BROADCAST_ADDRESS = -1;
    private static final int IPV4_DEST_ADDR_OFFSET = 30;
    private static final int IPV4_FRAGMENT_OFFSET_MASK = 8191;
    private static final int IPV4_FRAGMENT_OFFSET_OFFSET = 20;
    private static final int IPV4_PROTOCOL_OFFSET = 23;
    private static final byte[] IPV6_ALL_NODES_ADDRESS = null;
    private static final int IPV6_DEST_ADDR_OFFSET = 38;
    private static final int IPV6_HEADER_LEN = 40;
    private static final int IPV6_NEXT_HEADER_OFFSET = 20;
    private static final int IPV6_SRC_ADDR_OFFSET = 22;
    private static final long MAX_PROGRAM_LIFETIME_WORTH_REFRESHING = 30;
    private static final int MAX_RAS = 10;
    private static final String TAG = "ApfFilter";
    private static final int UDP_DESTINATION_PORT_OFFSET = 16;
    private static final int UDP_HEADER_LEN = 8;
    private static final boolean VDBG = true;
    private final ApfCapabilities mApfCapabilities;
    byte[] mHardwareAddress;
    @GuardedBy("this")
    private byte[] mIPv4Address;
    @GuardedBy("this")
    private int mIPv4PrefixLength;
    private final Callback mIpManagerCallback;
    @GuardedBy("this")
    private byte[] mLastInstalledProgram;
    @GuardedBy("this")
    private long mLastInstalledProgramMinLifetime;
    @GuardedBy("this")
    private long mLastTimeInstalledProgram;
    private final IpConnectivityLog mMetricsLog;
    @GuardedBy("this")
    private boolean mMulticastFilter;
    private final NetworkInterface mNetworkInterface;
    @GuardedBy("this")
    private int mNumProgramUpdates;
    @GuardedBy("this")
    private ArrayList<Ra> mRas;
    ReceiveThread mReceiveThread;
    @GuardedBy("this")
    private long mUniqueCounter;

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
    private enum ProcessRaResult {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.apf.ApfFilter.ProcessRaResult.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.apf.ApfFilter.ProcessRaResult.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfFilter.ProcessRaResult.<clinit>():void");
        }
    }

    private class Ra {
        private static final int ICMP6_4_BYTE_LIFETIME_LEN = 4;
        private static final int ICMP6_4_BYTE_LIFETIME_OFFSET = 4;
        private static final int ICMP6_DNSSL_OPTION_TYPE = 31;
        private static final int ICMP6_PREFIX_OPTION_LEN = 32;
        private static final int ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_LEN = 4;
        private static final int ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET = 8;
        private static final int ICMP6_PREFIX_OPTION_TYPE = 3;
        private static final int ICMP6_PREFIX_OPTION_VALID_LIFETIME_LEN = 4;
        private static final int ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET = 4;
        private static final int ICMP6_RA_CHECKSUM_LEN = 2;
        private static final int ICMP6_RA_CHECKSUM_OFFSET = 56;
        private static final int ICMP6_RA_HEADER_LEN = 16;
        private static final int ICMP6_RA_OPTION_OFFSET = 70;
        private static final int ICMP6_RA_ROUTER_LIFETIME_LEN = 2;
        private static final int ICMP6_RA_ROUTER_LIFETIME_OFFSET = 60;
        private static final int ICMP6_RDNSS_OPTION_TYPE = 25;
        private static final int ICMP6_ROUTE_INFO_OPTION_TYPE = 24;
        long mLastSeen;
        long mMinLifetime;
        private final ArrayList<Pair<Integer, Integer>> mNonLifetimes = new ArrayList();
        private final ByteBuffer mPacket;
        private final ArrayList<Integer> mPrefixOptionOffsets = new ArrayList();
        private final ArrayList<Integer> mRdnssOptionOffsets = new ArrayList();
        int seenCount = 0;

        String getLastMatchingPacket() {
            return HexDump.toHexString(this.mPacket.array(), 0, this.mPacket.capacity(), false);
        }

        /* JADX WARNING: Removed duplicated region for block: B:11:0x0026 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassCastException (e java.lang.ClassCastException)} */
        /* JADX WARNING: Missing block: B:13:0x002a, code:
            return "???";
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String IPv6AddresstoString(int pos) {
            try {
                byte[] array = this.mPacket.array();
                if (pos < 0 || pos + 16 > array.length || pos + 16 < pos) {
                    return "???";
                }
                return ((Inet6Address) InetAddress.getByAddress(Arrays.copyOfRange(array, pos, pos + 16))).getHostAddress();
            } catch (UnsupportedOperationException e) {
                return "???";
            } catch (ClassCastException e2) {
            }
        }

        private void prefixOptionToString(StringBuffer sb, int offset) {
            String prefix = IPv6AddresstoString(offset + 16);
            int length = ApfFilter.uint8(this.mPacket.get(offset + 2));
            long valid = (long) this.mPacket.getInt(offset + 4);
            long preferred = (long) this.mPacket.getInt(offset + 8);
            Object[] objArr = new Object[4];
            objArr[0] = prefix;
            objArr[1] = Integer.valueOf(length);
            objArr[2] = Long.valueOf(valid);
            objArr[3] = Long.valueOf(preferred);
            sb.append(String.format("%s/%d %ds/%ds ", objArr));
        }

        private void rdnssOptionToString(StringBuffer sb, int offset) {
            int optLen = ApfFilter.uint8(this.mPacket.get(offset + 1)) * 8;
            if (optLen >= 24) {
                int numServers = (optLen - 8) / 16;
                sb.append("DNS ").append(ApfFilter.uint32(this.mPacket.getInt(offset + 4))).append("s");
                for (int server = 0; server < numServers; server++) {
                    sb.append(" ").append(IPv6AddresstoString((offset + 8) + (server * 16)));
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x0051 A:{Splitter: B:0:0x0000, ExcHandler: java.nio.BufferUnderflowException (e java.nio.BufferUnderflowException)} */
        /* JADX WARNING: Missing block: B:8:0x0055, code:
            return "<Malformed RA>";
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String toString() {
            try {
                StringBuffer sb = new StringBuffer();
                Object[] objArr = new Object[3];
                objArr[0] = IPv6AddresstoString(22);
                objArr[1] = IPv6AddresstoString(38);
                objArr[2] = Integer.valueOf(ApfFilter.uint16(this.mPacket.getShort(60)));
                sb.append(String.format("RA %s -> %s %ds ", objArr));
                for (Integer intValue : this.mPrefixOptionOffsets) {
                    prefixOptionToString(sb, intValue.intValue());
                }
                for (Integer intValue2 : this.mRdnssOptionOffsets) {
                    rdnssOptionToString(sb, intValue2.intValue());
                }
                return sb.toString();
            } catch (BufferUnderflowException e) {
            }
        }

        private int addNonLifetime(int lastNonLifetimeStart, int lifetimeOffset, int lifetimeLength) {
            lifetimeOffset += this.mPacket.position();
            this.mNonLifetimes.add(new Pair(Integer.valueOf(lastNonLifetimeStart), Integer.valueOf(lifetimeOffset - lastNonLifetimeStart)));
            return lifetimeOffset + lifetimeLength;
        }

        private int addNonLifetimeU32(int lastNonLifetimeStart) {
            return addNonLifetime(lastNonLifetimeStart, 4, 4);
        }

        Ra(byte[] packet, int length) {
            this.mPacket = ByteBuffer.wrap(Arrays.copyOf(packet, length));
            this.mLastSeen = ApfFilter.curTime();
            if (ApfFilter.getUint16(this.mPacket, 12) == ((long) OsConstants.ETH_P_IPV6) && ApfFilter.uint8(this.mPacket.get(20)) == OsConstants.IPPROTO_ICMPV6 && ApfFilter.uint8(this.mPacket.get(54)) == ApfFilter.ICMP6_ROUTER_ADVERTISEMENT) {
                Builder builder = new Builder();
                int lastNonLifetimeStart = addNonLifetime(addNonLifetime(0, 56, 2), 60, 2);
                builder.updateRouterLifetime(ApfFilter.getUint16(this.mPacket, 60));
                this.mPacket.position(70);
                while (this.mPacket.hasRemaining()) {
                    int position = this.mPacket.position();
                    int optionType = ApfFilter.uint8(this.mPacket.get(position));
                    int optionLength = ApfFilter.uint8(this.mPacket.get(position + 1)) * 8;
                    switch (optionType) {
                        case 3:
                            lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart, 4, 4);
                            builder.updatePrefixValidLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart, 8, 4);
                            builder.updatePrefixPreferredLifetime(ApfFilter.getUint32(this.mPacket, position + 8));
                            this.mPrefixOptionOffsets.add(Integer.valueOf(position));
                            break;
                        case 24:
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateRouteInfoLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                        case 25:
                            this.mRdnssOptionOffsets.add(Integer.valueOf(position));
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateRdnssLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                        case 31:
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateDnsslLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                    }
                    if (optionLength <= 0) {
                        Object[] objArr = new Object[2];
                        objArr[0] = Integer.valueOf(optionType);
                        objArr[1] = Integer.valueOf(optionLength);
                        throw new IllegalArgumentException(String.format("Invalid option length opt=%d len=%d", objArr));
                    }
                    this.mPacket.position(position + optionLength);
                }
                addNonLifetime(lastNonLifetimeStart, 0, 0);
                this.mMinLifetime = minLifetime(packet, length);
                ApfFilter.this.mMetricsLog.log(builder.build());
                return;
            }
            throw new IllegalArgumentException("Not an ICMP6 router advertisement");
        }

        boolean matches(byte[] packet, int length) {
            if (length != this.mPacket.capacity()) {
                return false;
            }
            byte[] referencePacket = this.mPacket.array();
            for (Pair<Integer, Integer> nonLifetime : this.mNonLifetimes) {
                int i = ((Integer) nonLifetime.first).intValue();
                while (true) {
                    if (i < ((Integer) nonLifetime.second).intValue() + ((Integer) nonLifetime.first).intValue()) {
                        if (packet[i] != referencePacket[i]) {
                            return false;
                        }
                        i++;
                    }
                }
            }
            return true;
        }

        long minLifetime(byte[] packet, int length) {
            long minLifetime = JobStatus.NO_LATEST_RUNTIME;
            ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
            for (int i = 0; i + 1 < this.mNonLifetimes.size(); i++) {
                int offset = ((Integer) ((Pair) this.mNonLifetimes.get(i)).first).intValue() + ((Integer) ((Pair) this.mNonLifetimes.get(i)).second).intValue();
                if (offset != 56) {
                    long optionLifetime;
                    int lifetimeLength = ((Integer) ((Pair) this.mNonLifetimes.get(i + 1)).first).intValue() - offset;
                    switch (lifetimeLength) {
                        case 2:
                            optionLifetime = (long) ApfFilter.uint16(byteBuffer.getShort(offset));
                            break;
                        case 4:
                            optionLifetime = ApfFilter.uint32(byteBuffer.getInt(offset));
                            break;
                        default:
                            throw new IllegalStateException("bogus lifetime size " + lifetimeLength);
                    }
                    minLifetime = Math.min(minLifetime, optionLifetime);
                }
            }
            return minLifetime;
        }

        long currentLifetime() {
            return this.mMinLifetime - (ApfFilter.curTime() - this.mLastSeen);
        }

        boolean isExpired() {
            return currentLifetime() <= 0;
        }

        @GuardedBy("ApfFilter.this")
        long generateFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
            String nextFilterLabel = "Ra" + ApfFilter.this.getUniqueNumberLocked();
            gen.addLoadFromMemory(Register.R0, 14);
            gen.addJumpIfR0NotEquals(this.mPacket.capacity(), nextFilterLabel);
            int filterLifetime = (int) (currentLifetime() / 6);
            gen.addLoadFromMemory(Register.R0, 15);
            gen.addJumpIfR0GreaterThan(filterLifetime, nextFilterLabel);
            for (int i = 0; i < this.mNonLifetimes.size(); i++) {
                Pair<Integer, Integer> nonLifetime = (Pair) this.mNonLifetimes.get(i);
                if (((Integer) nonLifetime.second).intValue() != 0) {
                    gen.addLoadImmediate(Register.R0, ((Integer) nonLifetime.first).intValue());
                    gen.addJumpIfBytesNotEqual(Register.R0, Arrays.copyOfRange(this.mPacket.array(), ((Integer) nonLifetime.first).intValue(), ((Integer) nonLifetime.second).intValue() + ((Integer) nonLifetime.first).intValue()), nextFilterLabel);
                }
                if (i + 1 < this.mNonLifetimes.size()) {
                    Pair<Integer, Integer> nextNonLifetime = (Pair) this.mNonLifetimes.get(i + 1);
                    int offset = ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue();
                    if (offset == 56) {
                        continue;
                    } else {
                        int length = ((Integer) nextNonLifetime.first).intValue() - offset;
                        switch (length) {
                            case 2:
                                gen.addLoad16(Register.R0, offset);
                                break;
                            case 4:
                                gen.addLoad32(Register.R0, offset);
                                break;
                            default:
                                throw new IllegalStateException("bogus lifetime size " + length);
                        }
                        gen.addJumpIfR0LessThan(filterLifetime, nextFilterLabel);
                    }
                }
            }
            gen.addJump(ApfGenerator.DROP_LABEL);
            gen.defineLabel(nextFilterLabel);
            return (long) filterLifetime;
        }
    }

    class ReceiveThread extends Thread {
        /* renamed from: -android-net-apf-ApfFilter$ProcessRaResultSwitchesValues */
        private static final /* synthetic */ int[] f0-android-net-apf-ApfFilter$ProcessRaResultSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$apf$ApfFilter$ProcessRaResult;
        private int mDroppedRas;
        private int mMatchingRas;
        private final byte[] mPacket = new byte[1514];
        private int mParseErrors;
        private int mProgramUpdates;
        private int mReceivedRas;
        private final FileDescriptor mSocket;
        private final long mStart = SystemClock.elapsedRealtime();
        private volatile boolean mStopped;
        private int mZeroLifetimeRas;

        /* renamed from: -getandroid-net-apf-ApfFilter$ProcessRaResultSwitchesValues */
        private static /* synthetic */ int[] m0-getandroid-net-apf-ApfFilter$ProcessRaResultSwitchesValues() {
            if (f0-android-net-apf-ApfFilter$ProcessRaResultSwitchesValues != null) {
                return f0-android-net-apf-ApfFilter$ProcessRaResultSwitchesValues;
            }
            int[] iArr = new int[ProcessRaResult.values().length];
            try {
                iArr[ProcessRaResult.DROPPED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ProcessRaResult.MATCH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ProcessRaResult.PARSE_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ProcessRaResult.UPDATE_EXPIRY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ProcessRaResult.UPDATE_NEW_RA.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ProcessRaResult.ZERO_LIFETIME.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            f0-android-net-apf-ApfFilter$ProcessRaResultSwitchesValues = iArr;
            return iArr;
        }

        public ReceiveThread(FileDescriptor socket) {
            this.mSocket = socket;
        }

        public void halt() {
            this.mStopped = true;
            try {
                IoBridge.closeAndSignalBlockedThreads(this.mSocket);
            } catch (IOException e) {
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x0024 A:{Splitter: B:3:0x000c, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:6:0x0024, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:8:0x0027, code:
            if (r6.mStopped == false) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:9:0x0029, code:
            android.util.Log.e(android.net.apf.ApfFilter.TAG, "Read error", r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            ApfFilter.this.log("begin monitoring");
            while (!this.mStopped) {
                try {
                    updateStats(ApfFilter.this.processRa(this.mPacket, Os.read(this.mSocket, this.mPacket, 0, this.mPacket.length)));
                } catch (Exception e) {
                }
            }
            logStats();
        }

        private void updateStats(ProcessRaResult result) {
            this.mReceivedRas++;
            switch (m0-getandroid-net-apf-ApfFilter$ProcessRaResultSwitchesValues()[result.ordinal()]) {
                case 1:
                    this.mDroppedRas++;
                    return;
                case 2:
                    this.mMatchingRas++;
                    return;
                case 3:
                    this.mParseErrors++;
                    return;
                case 4:
                    this.mMatchingRas++;
                    this.mProgramUpdates++;
                    return;
                case 5:
                    this.mProgramUpdates++;
                    return;
                case 6:
                    this.mZeroLifetimeRas++;
                    return;
                default:
                    return;
            }
        }

        private void logStats() {
            ApfFilter.this.mMetricsLog.log(new ApfStats(SystemClock.elapsedRealtime() - this.mStart, this.mReceivedRas, this.mMatchingRas, this.mDroppedRas, this.mZeroLifetimeRas, this.mParseErrors, this.mProgramUpdates, ApfFilter.this.mApfCapabilities.maximumApfProgramSize));
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.apf.ApfFilter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.apf.ApfFilter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfFilter.<clinit>():void");
    }

    ApfFilter(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter, IpConnectivityLog log) {
        this.mRas = new ArrayList();
        this.mApfCapabilities = apfCapabilities;
        this.mIpManagerCallback = ipManagerCallback;
        this.mNetworkInterface = networkInterface;
        this.mMulticastFilter = multicastFilter;
        this.mMetricsLog = log;
        maybeStartFilter();
    }

    private void log(String s) {
        Log.d(TAG, "(" + this.mNetworkInterface.getName() + "): " + s);
    }

    @GuardedBy("this")
    private long getUniqueNumberLocked() {
        long j = this.mUniqueCounter;
        this.mUniqueCounter = 1 + j;
        return j;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x003f, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0040, code:
            android.util.Log.e(TAG, "Error starting filter", r1);
     */
    /* JADX WARNING: Missing block: B:16:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void maybeStartFilter() {
        try {
            this.mHardwareAddress = this.mNetworkInterface.getHardwareAddress();
            synchronized (this) {
                installNewProgramLocked();
            }
            FileDescriptor socket = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IPV6);
            Os.bind(socket, new PacketSocketAddress((short) OsConstants.ETH_P_IPV6, this.mNetworkInterface.getIndex()));
            NetworkUtils.attachRaFilter(socket, this.mApfCapabilities.apfPacketFormat);
            this.mReceiveThread = new ReceiveThread(socket);
            this.mReceiveThread.start();
        } catch (Exception e) {
        }
    }

    private static long curTime() {
        return System.currentTimeMillis() / 1000;
    }

    @GuardedBy("this")
    private void generateArpFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        String checkTargetIPv4 = "checkTargetIPv4";
        gen.addLoadImmediate(Register.R0, 14);
        gen.addJumpIfBytesNotEqual(Register.R0, ARP_IPV4_HEADER, ApfGenerator.PASS_LABEL);
        gen.addLoad16(Register.R0, 20);
        gen.addJumpIfR0Equals(1, "checkTargetIPv4");
        gen.addJumpIfR0NotEquals(2, ApfGenerator.PASS_LABEL);
        gen.addLoadImmediate(Register.R0, 0);
        gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
        gen.defineLabel("checkTargetIPv4");
        if (this.mIPv4Address == null) {
            gen.addLoad32(Register.R0, 38);
            gen.addJumpIfR0Equals(0, ApfGenerator.DROP_LABEL);
        } else {
            gen.addLoadImmediate(Register.R0, 38);
            gen.addJumpIfBytesNotEqual(Register.R0, this.mIPv4Address, ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv4FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        if (this.mMulticastFilter) {
            String skipDhcpv4Filter = "skip_dhcp_v4_filter";
            gen.addLoad8(Register.R0, 23);
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_UDP, "skip_dhcp_v4_filter");
            gen.addLoad16(Register.R0, 20);
            gen.addJumpIfR0AnyBitsSet(IPV4_FRAGMENT_OFFSET_MASK, "skip_dhcp_v4_filter");
            gen.addLoadFromMemory(Register.R1, 13);
            gen.addLoad16Indexed(Register.R0, 16);
            gen.addJumpIfR0NotEquals(68, "skip_dhcp_v4_filter");
            gen.addLoadImmediate(Register.R0, 50);
            gen.addAddR1();
            gen.addJumpIfBytesNotEqual(Register.R0, this.mHardwareAddress, "skip_dhcp_v4_filter");
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel("skip_dhcp_v4_filter");
            gen.addLoad8(Register.R0, 30);
            gen.addAnd(240);
            gen.addJumpIfR0Equals(224, ApfGenerator.DROP_LABEL);
            gen.addLoad32(Register.R0, 30);
            gen.addJumpIfR0Equals(-1, ApfGenerator.DROP_LABEL);
            if (this.mIPv4Address != null && this.mIPv4PrefixLength < 31) {
                gen.addJumpIfR0Equals(ipv4BroadcastAddress(this.mIPv4Address, this.mIPv4PrefixLength), ApfGenerator.DROP_LABEL);
            }
            gen.addLoadImmediate(Register.R0, 0);
            gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
            gen.addJump(ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv6FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        gen.addLoad8(Register.R0, 20);
        if (this.mMulticastFilter) {
            String skipIpv6MulticastFilterLabel = "skipIPv6MulticastFilter";
            gen.addJumpIfR0Equals(OsConstants.IPPROTO_ICMPV6, skipIpv6MulticastFilterLabel);
            gen.addLoad8(Register.R0, 38);
            gen.addJumpIfR0Equals(255, ApfGenerator.DROP_LABEL);
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel(skipIpv6MulticastFilterLabel);
        } else {
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_ICMPV6, ApfGenerator.PASS_LABEL);
        }
        String skipUnsolicitedMulticastNALabel = "skipUnsolicitedMulticastNA";
        gen.addLoad8(Register.R0, 54);
        gen.addJumpIfR0NotEquals(ICMP6_NEIGHBOR_ANNOUNCEMENT, skipUnsolicitedMulticastNALabel);
        gen.addLoadImmediate(Register.R0, 38);
        gen.addJumpIfBytesNotEqual(Register.R0, IPV6_ALL_NODES_ADDRESS, skipUnsolicitedMulticastNALabel);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(skipUnsolicitedMulticastNALabel);
    }

    @GuardedBy("this")
    private ApfGenerator beginProgramLocked() throws IllegalInstructionException {
        ApfGenerator gen = new ApfGenerator();
        gen.setApfVersion(this.mApfCapabilities.apfVersionSupported);
        String skipArpFiltersLabel = "skipArpFilters";
        gen.addLoad16(Register.R0, 12);
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_ARP, skipArpFiltersLabel);
        generateArpFilterLocked(gen);
        gen.defineLabel(skipArpFiltersLabel);
        String skipIPv4FiltersLabel = "skipIPv4Filters";
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_IP, skipIPv4FiltersLabel);
        generateIPv4FilterLocked(gen);
        gen.defineLabel(skipIPv4FiltersLabel);
        String ipv6FilterLabel = "IPv6Filters";
        gen.addJumpIfR0Equals(OsConstants.ETH_P_IPV6, ipv6FilterLabel);
        gen.addLoadImmediate(Register.R0, 0);
        gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(ipv6FilterLabel);
        generateIPv6FilterLocked(gen);
        return gen;
    }

    @GuardedBy("this")
    void installNewProgramLocked() {
        purgeExpiredRasLocked();
        ArrayList<Ra> rasToFilter = new ArrayList();
        long programMinLifetime = JobStatus.NO_LATEST_RUNTIME;
        try {
            ApfGenerator gen = beginProgramLocked();
            for (Ra ra : this.mRas) {
                ra.generateFilterLocked(gen);
                if (gen.programLengthOverEstimate() > this.mApfCapabilities.maximumApfProgramSize) {
                    break;
                }
                rasToFilter.add(ra);
            }
            gen = beginProgramLocked();
            for (Ra ra2 : rasToFilter) {
                programMinLifetime = Math.min(programMinLifetime, ra2.generateFilterLocked(gen));
            }
            byte[] program = gen.generate();
            this.mLastTimeInstalledProgram = curTime();
            this.mLastInstalledProgramMinLifetime = programMinLifetime;
            this.mLastInstalledProgram = program;
            this.mNumProgramUpdates++;
            hexDump("Installing filter: ", program, program.length);
            this.mIpManagerCallback.installPacketFilter(program);
            this.mMetricsLog.log(new ApfProgramEvent(programMinLifetime, rasToFilter.size(), this.mRas.size(), program.length, ApfProgramEvent.flagsFor(this.mIPv4Address != null, this.mMulticastFilter)));
        } catch (IllegalInstructionException e) {
            Log.e(TAG, "Program failed to generate: ", e);
        }
    }

    private boolean shouldInstallnewProgram() {
        return this.mLastTimeInstalledProgram + this.mLastInstalledProgramMinLifetime < curTime() + MAX_PROGRAM_LIFETIME_WORTH_REFRESHING;
    }

    private void hexDump(String msg, byte[] packet, int length) {
        log(msg + HexDump.toHexString(packet, 0, length, false));
    }

    @GuardedBy("this")
    private void purgeExpiredRasLocked() {
        int i = 0;
        while (i < this.mRas.size()) {
            if (((Ra) this.mRas.get(i)).isExpired()) {
                log("Expiring " + this.mRas.get(i));
                this.mRas.remove(i);
            } else {
                i++;
            }
        }
    }

    private synchronized ProcessRaResult processRa(byte[] packet, int length) {
        Ra ra;
        hexDump("Read packet = ", packet, length);
        for (int i = 0; i < this.mRas.size(); i++) {
            ra = (Ra) this.mRas.get(i);
            if (ra.matches(packet, length)) {
                log("matched RA " + ra);
                ra.mLastSeen = curTime();
                ra.mMinLifetime = ra.minLifetime(packet, length);
                ra.seenCount++;
                this.mRas.add(0, (Ra) this.mRas.remove(i));
                if (shouldInstallnewProgram()) {
                    installNewProgramLocked();
                    return ProcessRaResult.UPDATE_EXPIRY;
                }
                return ProcessRaResult.MATCH;
            }
        }
        purgeExpiredRasLocked();
        if (this.mRas.size() >= 10) {
            return ProcessRaResult.DROPPED;
        }
        try {
            ra = new Ra(packet, length);
            if (ra.isExpired()) {
                return ProcessRaResult.ZERO_LIFETIME;
            }
            log("Adding " + ra);
            this.mRas.add(ra);
            installNewProgramLocked();
            return ProcessRaResult.UPDATE_NEW_RA;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RA: " + e);
            return ProcessRaResult.PARSE_ERROR;
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ApfFilter maybeCreate(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter) {
        if (apfCapabilities == null || networkInterface == null || apfCapabilities.apfVersionSupported == 0) {
            return null;
        }
        if (apfCapabilities.maximumApfProgramSize < 512) {
            Log.e(TAG, "Unacceptably small APF limit: " + apfCapabilities.maximumApfProgramSize);
            return null;
        } else if (apfCapabilities.apfPacketFormat != OsConstants.ARPHRD_ETHER) {
            return null;
        } else {
            if (new ApfGenerator().setApfVersion(apfCapabilities.apfVersionSupported)) {
                return new ApfFilter(apfCapabilities, networkInterface, ipManagerCallback, multicastFilter, new IpConnectivityLog());
            }
            Log.e(TAG, "Unsupported APF version: " + apfCapabilities.apfVersionSupported);
            return null;
        }
    }

    public synchronized void shutdown() {
        if (this.mReceiveThread != null) {
            log("shutting down");
            this.mReceiveThread.halt();
            this.mReceiveThread = null;
        }
        this.mRas.clear();
    }

    public synchronized void setMulticastFilter(boolean enabled) {
        if (this.mMulticastFilter != enabled) {
            this.mMulticastFilter = enabled;
            installNewProgramLocked();
        }
    }

    private static LinkAddress findIPv4LinkAddress(LinkProperties lp) {
        LinkAddress ipv4Address = null;
        for (LinkAddress address : lp.getLinkAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
                if (ipv4Address != null && !ipv4Address.isSameAddressAs(address)) {
                    return null;
                }
                ipv4Address = address;
            }
        }
        return ipv4Address;
    }

    public synchronized void setLinkProperties(LinkProperties lp) {
        LinkAddress ipv4Address = findIPv4LinkAddress(lp);
        byte[] addr = ipv4Address != null ? ipv4Address.getAddress().getAddress() : null;
        int prefix = ipv4Address != null ? ipv4Address.getPrefixLength() : 0;
        if (prefix != this.mIPv4PrefixLength || !Arrays.equals(addr, this.mIPv4Address)) {
            this.mIPv4Address = addr;
            this.mIPv4PrefixLength = prefix;
            installNewProgramLocked();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0150 A:{Splitter: B:9:0x0056, ExcHandler: java.net.UnknownHostException (e java.net.UnknownHostException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void dump(IndentingPrintWriter pw) {
        pw.println("Capabilities: " + this.mApfCapabilities);
        pw.println("Receive thread: " + (this.mReceiveThread != null ? "RUNNING" : "STOPPED"));
        pw.println("Multicast: " + (this.mMulticastFilter ? "DROP" : "ALLOW"));
        try {
            pw.println("IPv4 address: " + InetAddress.getByAddress(this.mIPv4Address).getHostAddress());
        } catch (UnknownHostException e) {
        }
        if (this.mLastTimeInstalledProgram == 0) {
            pw.println("No program installed.");
            return;
        }
        pw.println("Program updates: " + this.mNumProgramUpdates);
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mLastInstalledProgram.length);
        objArr[1] = Long.valueOf(curTime() - this.mLastTimeInstalledProgram);
        objArr[2] = Long.valueOf(this.mLastInstalledProgramMinLifetime);
        pw.println(String.format("Last program length %d, installed %ds ago, lifetime %ds", objArr));
        pw.println("RA filters:");
        pw.increaseIndent();
        for (Ra ra : this.mRas) {
            pw.println(ra);
            pw.increaseIndent();
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(ra.seenCount);
            objArr[1] = Long.valueOf(curTime() - ra.mLastSeen);
            pw.println(String.format("Seen: %d, last %ds ago", objArr));
            pw.println("Last match:");
            pw.increaseIndent();
            pw.println(ra.getLastMatchingPacket());
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
        pw.println("Last program:");
        pw.increaseIndent();
        pw.println(HexDump.toHexString(this.mLastInstalledProgram, false));
        pw.decreaseIndent();
    }

    private static int uint8(byte b) {
        return b & 255;
    }

    private static int uint16(short s) {
        return 65535 & s;
    }

    private static long uint32(int i) {
        return ((long) i) & 4294967295L;
    }

    private static long getUint16(ByteBuffer buffer, int position) {
        return (long) uint16(buffer.getShort(position));
    }

    private static long getUint32(ByteBuffer buffer, int position) {
        return uint32(buffer.getInt(position));
    }

    public static int ipv4BroadcastAddress(byte[] addrBytes, int prefixLength) {
        return bytesToInt(addrBytes) | ((int) (uint32(-1) >>> prefixLength));
    }

    public static int bytesToInt(byte[] addrBytes) {
        return (((uint8(addrBytes[0]) << 24) + (uint8(addrBytes[1]) << 16)) + (uint8(addrBytes[2]) << 8)) + uint8(addrBytes[3]);
    }
}
