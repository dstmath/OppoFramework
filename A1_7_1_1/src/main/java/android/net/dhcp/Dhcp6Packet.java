package android.net.dhcp;

import android.net.DhcpResults;
import android.net.LinkAddress;
import android.util.Log;
import com.android.internal.util.HexDump;
import com.android.server.oppo.IElsaManager;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
abstract class Dhcp6Packet {
    protected static final short CLIENT_ID_ETHER = (short) 3;
    protected static final Boolean DBG = null;
    static final short DHCP_CLIENT = (short) 546;
    protected static final byte DHCP_MESSAGE_TYPE = (byte) 53;
    protected static final byte DHCP_MESSAGE_TYPE_ADVERTISE = (byte) 2;
    protected static final byte DHCP_MESSAGE_TYPE_CONFIRM = (byte) 4;
    protected static final byte DHCP_MESSAGE_TYPE_DECLINE = (byte) 9;
    protected static final byte DHCP_MESSAGE_TYPE_INFO_REQUEST = (byte) 11;
    protected static final byte DHCP_MESSAGE_TYPE_REBIND = (byte) 6;
    protected static final byte DHCP_MESSAGE_TYPE_RELEASE = (byte) 8;
    protected static final byte DHCP_MESSAGE_TYPE_RENEW = (byte) 5;
    protected static final byte DHCP_MESSAGE_TYPE_REPLY = (byte) 7;
    protected static final byte DHCP_MESSAGE_TYPE_REQUEST = (byte) 3;
    protected static final byte DHCP_MESSAGE_TYPE_SOLICIT = (byte) 1;
    protected static final short DHCP_OPTION_END = (short) 255;
    protected static final short DHCP_OPTION_PAD = (short) 0;
    static final short DHCP_SERVER = (short) 547;
    protected static final byte DUID_EN_TYPE = (byte) 2;
    protected static final byte DUID_LLT_TYPE = (byte) 1;
    protected static final byte DUID_LL_TYPE = (byte) 3;
    public static final byte[] ETHER_BROADCAST = null;
    public static final int HWADDR_LEN = 16;
    public static final Inet6Address INADDR_ANY = null;
    public static final Inet6Address INADDR_BROADCAST_ROUTER = null;
    public static final int INFINITE_LEASE = -1;
    private static final byte IPV6_HOT_LIMIT = (byte) 1;
    private static final byte[] IPV6_VERSION_HEADER = null;
    private static final byte IP_TYPE_UDP = (byte) 17;
    protected static final int MAX_LENGTH = 1500;
    public static final int MAX_OPTION_LEN = 65025;
    public static final int MINIMUM_LEASE = 60;
    public static final int MIN_PACKET_LENGTH_L2 = 54;
    public static final int MIN_PACKET_LENGTH_L3 = 40;
    protected static final short OPTION_CLIENTID = (short) 1;
    protected static final short OPTION_DNS_SERVERS = (short) 23;
    protected static final short OPTION_DOMAIN_LIST = (short) 24;
    protected static final short OPTION_ELAPSED_TIME = (short) 8;
    protected static final short OPTION_IAADDR = (short) 5;
    protected static final short OPTION_IA_NA = (short) 3;
    protected static final short OPTION_IA_TA = (short) 4;
    protected static final short OPTION_ORO = (short) 6;
    protected static final short OPTION_PREFERENCE = (short) 7;
    protected static final short OPTION_SERVERID = (short) 2;
    protected static final String TAG = "Dhcp6Packet";
    protected final byte[] mClientMac;
    protected List<Inet6Address> mDnsServers;
    protected String mDomainName;
    protected Inet6Address mGateway;
    protected byte[] mIana;
    protected Integer mLeaseTime;
    protected Short mMtu;
    private final Inet6Address mNextIp;
    private final Inet6Address mRelayIp;
    protected Inet6Address mRequestedIp;
    protected short[] mRequestedParams;
    protected Inet6Address mServerAddress;
    protected byte[] mServerIdentifier;
    protected final Inet6Address mServerIp;
    protected Inet6Address mSubnetMask;
    protected Integer mT1;
    protected Integer mT2;
    protected final byte[] mTransId;
    protected String mVendorId;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.Dhcp6Packet.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.dhcp.Dhcp6Packet.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.Dhcp6Packet.<clinit>():void");
    }

    public abstract ByteBuffer buildPacket(short s, short s2);

    abstract void finishPacket(ByteBuffer byteBuffer);

    protected Dhcp6Packet(byte[] transId, Inet6Address sourceIP, Inet6Address nextIp, Inet6Address relayIp, byte[] clientMac) {
        this.mTransId = transId;
        this.mServerIp = sourceIP;
        this.mNextIp = nextIp;
        this.mRelayIp = relayIp;
        this.mClientMac = clientMac;
    }

    public byte[] getTransactionId() {
        return this.mTransId;
    }

    public byte[] getClientMac() {
        if (this.mClientMac != null) {
            return this.mClientMac;
        }
        return null;
    }

    public byte[] getClientId() {
        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) 1);
        buffer.putShort((short) 1);
        buffer.put(Dhcp6Client.getTimeStamp());
        buffer.put(this.mClientMac);
        return buffer.array();
    }

    private byte[] getIaNa() {
        return new byte[]{(byte) 14, (byte) 0, DHCP_MESSAGE_TYPE_RELEASE, (byte) -54, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    }

    protected void fillInPacket(Inet6Address destIp, Inet6Address srcIp, short destUdp, short srcUdp, ByteBuffer buf, byte requestCode) {
        byte[] destIpArray = destIp.getAddress();
        byte[] srcIpArray = srcIp.getAddress();
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(requestCode);
        buf.put(this.mTransId);
        finishPacket(buf);
        if (DBG.booleanValue()) {
            Log.d(TAG, HexDump.toHexString(buf.array()));
        }
    }

    private static int intAbs(short v) {
        return 65535 & v;
    }

    private int checksum(ByteBuffer buf, int seed, int start, int end) {
        int sum = seed;
        int bufPosition = buf.position();
        buf.position(start);
        ShortBuffer shortBuf = buf.asShortBuffer();
        buf.position(bufPosition);
        short[] shortArray = new short[((end - start) / 2)];
        shortBuf.get(shortArray);
        for (short s : shortArray) {
            sum += intAbs(s);
        }
        start += shortArray.length * 2;
        if (end != start) {
            short b = (short) buf.get(start);
            if (b < (short) 0) {
                b = (short) (b + 256);
            }
            sum += b * 256;
        }
        sum = ((sum >> 16) & 65535) + (sum & 65535);
        return intAbs((short) (~((((sum >> 16) & 65535) + sum) & 65535)));
    }

    protected static void addTlv(ByteBuffer buf, short type, byte value) {
        buf.putShort(type);
        buf.putShort((short) 1);
        buf.put(value);
    }

    protected static void addTlv(ByteBuffer buf, short type, byte[] payload) {
        if (payload == null) {
            return;
        }
        if (payload.length > MAX_OPTION_LEN) {
            throw new IllegalArgumentException("DHCP option too long: " + payload.length + " vs. " + MAX_OPTION_LEN);
        }
        buf.putShort(type);
        buf.putShort((short) payload.length);
        buf.put(payload);
    }

    protected static void addTlv(ByteBuffer buf, short type, short[] payload) {
        if (payload == null) {
            return;
        }
        if (payload.length > MAX_OPTION_LEN) {
            throw new IllegalArgumentException("DHCP option too long: " + payload.length + " vs. " + MAX_OPTION_LEN);
        }
        byte[] rawBtyes = new byte[(payload.length * 2)];
        ByteBuffer.wrap(rawBtyes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(payload);
        addTlv(buf, type, rawBtyes);
    }

    protected static void addTlv(ByteBuffer buf, short type, Inet6Address addr) {
        if (addr != null) {
            addTlv(buf, type, addr.getAddress());
        }
    }

    protected static void addTlv(ByteBuffer buf, short type, List<Inet6Address> addrs) {
        if (addrs != null && addrs.size() != 0) {
            int optionLen = addrs.size() * 4;
            if (optionLen > MAX_OPTION_LEN) {
                throw new IllegalArgumentException("DHCP option too long: " + optionLen + " vs. " + MAX_OPTION_LEN);
            }
            buf.putShort(type);
            buf.put((byte) optionLen);
            for (Inet6Address addr : addrs) {
                buf.put(addr.getAddress());
            }
        }
    }

    protected static void addTlv(ByteBuffer buf, short type, Short value) {
        if (value != null) {
            buf.putShort(type);
            buf.putShort((short) 2);
            buf.putShort(value.shortValue());
        }
    }

    protected static void addTlv(ByteBuffer buf, short type, Integer value) {
        if (value != null) {
            buf.putShort(type);
            buf.putShort((short) 4);
            buf.putInt(value.intValue());
        }
    }

    protected static void addTlv(ByteBuffer buf, short type, String str) {
        try {
            addTlv(buf, type, str.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("String is not US-ASCII: " + str);
        }
    }

    protected void addCommonClientTlvs(ByteBuffer buf) {
        addTlv(buf, (short) 3, getIaNa());
        addTlv(buf, (short) 8, Short.valueOf((short) 0));
    }

    public static String macToString(byte[] mac) {
        String macAddr = IElsaManager.EMPTY_PACKAGE;
        if (mac == null) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        for (int i = 0; i < mac.length; i++) {
            String hexString = "0" + Integer.toHexString(mac[i]);
            macAddr = macAddr + hexString.substring(hexString.length() - 2);
            if (i != mac.length - 1) {
                macAddr = macAddr + ":";
            }
        }
        return macAddr;
    }

    public String toString() {
        return macToString(this.mClientMac);
    }

    private static Inet6Address readIpAddress(ByteBuffer packet) {
        Inet6Address inet6Address;
        byte[] ipAddr = new byte[16];
        packet.get(ipAddr);
        try {
            inet6Address = (Inet6Address) Inet6Address.getByAddress(ipAddr);
        } catch (UnknownHostException e) {
            inet6Address = null;
        }
        if (DBG.booleanValue()) {
            Log.i(TAG, "readIpAddress:" + inet6Address);
        }
        return inet6Address;
    }

    private static String readAsciiString(ByteBuffer buf, int byteCount, boolean nullOk) {
        byte[] bytes = new byte[byteCount];
        buf.get(bytes);
        int length = bytes.length;
        if (!nullOk) {
            length = 0;
            while (length < bytes.length && bytes[length] != (byte) 0) {
                length++;
            }
        }
        return new String(bytes, 0, length, StandardCharsets.US_ASCII);
    }

    public static Dhcp6Packet decodeFullPacket(ByteBuffer packet) {
        byte[] clientMac = null;
        List<Inet6Address> dnsServers = new ArrayList();
        byte[] serverIdentifier = null;
        Inet6Address requestedAddress = null;
        Integer T1 = null;
        Integer T2 = null;
        packet.order(ByteOrder.BIG_ENDIAN);
        byte dhcpType = packet.get();
        byte[] transactionId = new byte[3];
        packet.get(transactionId);
        boolean notFinishedOptions = true;
        while (packet.position() < packet.limit() && notFinishedOptions) {
            try {
                short optionType = packet.getShort();
                if (DBG.booleanValue()) {
                    Log.d(TAG, "optionType:" + optionType);
                }
                if (optionType == (short) 255) {
                    notFinishedOptions = false;
                } else if (optionType != (short) 0) {
                    int optionLen = packet.getShort() & 65535;
                    int expectedLen = 0;
                    ByteBuffer buf;
                    switch (optionType) {
                        case (short) 1:
                            byte[] id = new byte[optionLen];
                            packet.get(id);
                            expectedLen = optionLen;
                            buf = ByteBuffer.wrap(id);
                            short duidType = buf.getShort();
                            if ((duidType == (short) 1 || duidType == (short) 3) && buf.getShort() == (short) 1) {
                                if (duidType == (short) 1) {
                                    buf.getInt();
                                }
                                clientMac = new byte[6];
                                buf.get(clientMac);
                                break;
                            }
                        case (short) 2:
                            serverIdentifier = new byte[optionLen];
                            packet.get(serverIdentifier);
                            expectedLen = optionLen;
                            break;
                        case (short) 3:
                            byte[] iana = new byte[optionLen];
                            packet.get(iana);
                            buf = ByteBuffer.wrap(iana);
                            T1 = Integer.valueOf(buf.getInt(4));
                            T2 = Integer.valueOf(buf.getInt(8));
                            Log.d(TAG, "T1:" + T1);
                            Log.d(TAG, "T2:" + T2);
                            if (optionLen > 12) {
                                buf.position(12);
                                if (buf.getShort() == (short) 5) {
                                    int iaLen = buf.getShort() & 65535;
                                    requestedAddress = readIpAddress(buf);
                                    expectedLen = optionLen;
                                    break;
                                }
                            }
                            break;
                        case (short) 23:
                            expectedLen = 0;
                            while (expectedLen < optionLen) {
                                dnsServers.add(readIpAddress(packet));
                                expectedLen += 16;
                            }
                            break;
                        default:
                            for (int i = 0; i < optionLen; i++) {
                                expectedLen++;
                                byte throwaway = packet.get();
                            }
                            break;
                    }
                    if (DBG.booleanValue()) {
                        Log.d(TAG, "expectedLen:" + expectedLen);
                        Log.d(TAG, "optionLen:" + optionLen);
                    }
                    if (expectedLen != optionLen) {
                        Log.e(TAG, "optionType:" + optionType);
                        return null;
                    }
                } else {
                    continue;
                }
            } catch (BufferUnderflowException e) {
                e.printStackTrace();
                return null;
            } catch (Exception ee) {
                ee.printStackTrace();
                return null;
            }
        }
        Dhcp6Packet dhcp6AdvertisePacket;
        switch (dhcpType) {
            case (byte) -1:
                return null;
            case (byte) 2:
                dhcp6AdvertisePacket = new Dhcp6AdvertisePacket(transactionId, null, requestedAddress, clientMac);
                break;
            case (byte) 7:
                dhcp6AdvertisePacket = new Dhcp6ReplyPacket(transactionId, null, requestedAddress, clientMac);
                break;
            default:
                Log.e(TAG, "Unimplemented type: " + dhcpType);
                return null;
        }
        newPacket.mRequestedIp = requestedAddress;
        newPacket.mDnsServers = dnsServers;
        newPacket.mServerIdentifier = serverIdentifier;
        newPacket.mT1 = T1;
        newPacket.mT2 = T2;
        newPacket.mLeaseTime = T1;
        return newPacket;
    }

    public static Dhcp6Packet decodeFullPacket(byte[] packet, int length) {
        return decodeFullPacket(ByteBuffer.wrap(packet, 0, length).order(ByteOrder.BIG_ENDIAN));
    }

    public DhcpResults toDhcpResults() {
        Inet6Address ipAddress = this.mRequestedIp;
        if (ipAddress == null || ipAddress.equals(Inet6Address.ANY)) {
            return null;
        }
        DhcpResults results = new DhcpResults();
        try {
            results.ipAddress = new LinkAddress(ipAddress, 64);
            results.gateway = this.mGateway;
            results.dnsServers.addAll(this.mDnsServers);
            results.domains = this.mDomainName;
            results.serverAddress = null;
            results.leaseDuration = this.mLeaseTime != null ? this.mLeaseTime.intValue() : -1;
            return results;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getLeaseTimeMillis() {
        if (this.mLeaseTime == null || this.mLeaseTime.intValue() == -1) {
            return 0;
        }
        if (this.mLeaseTime.intValue() < 0 || this.mLeaseTime.intValue() >= 60) {
            return (((long) this.mLeaseTime.intValue()) & 4294967295L) * 1000;
        }
        return 60000;
    }

    public static ByteBuffer buildSolicitPacket(byte[] transactionId, short secs, byte[] clientMac, short[] expectedParams) {
        Dhcp6Packet pkt = new Dhcp6SolicitPacket(transactionId, clientMac);
        pkt.mRequestedParams = expectedParams;
        return pkt.buildPacket(DHCP_SERVER, DHCP_CLIENT);
    }

    public static ByteBuffer buildRequestPacket(byte[] transactionId, short secs, Inet6Address clientIp, byte[] clientMac, Inet6Address requestedIpAddress, byte[] serverIdentifier, short[] requestedParams) {
        Dhcp6Packet pkt = new Dhcp6RequestPacket(transactionId, clientMac);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        pkt.mRequestedParams = requestedParams;
        return pkt.buildPacket(DHCP_SERVER, DHCP_CLIENT);
    }

    public static ByteBuffer buildInfoRequestPacket(byte[] transactionId, short secs, byte[] clientMac, short[] expectedParams) {
        Dhcp6Packet pkt = new Dhcp6InfoRequestPacket(transactionId, clientMac);
        pkt.mRequestedParams = expectedParams;
        return pkt.buildPacket(DHCP_SERVER, DHCP_CLIENT);
    }

    public static ByteBuffer buildRenewPacket(byte[] transactionId, short secs, Inet6Address clientIp, boolean broadcast, byte[] clientMac, Inet6Address requestedIpAddress, byte[] serverIdentifier, byte[] requestedParams) {
        Dhcp6Packet pkt = new Dhcp6RenewPacket(transactionId, clientMac);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        return pkt.buildPacket(DHCP_SERVER, DHCP_CLIENT);
    }

    public static ByteBuffer buildRebindPacket(byte[] transactionId, short secs, Inet6Address clientIp, boolean broadcast, byte[] clientMac, Inet6Address requestedIpAddress, byte[] serverIdentifier, byte[] requestedParams) {
        Dhcp6Packet pkt = new Dhcp6RebindPacket(transactionId, clientMac);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        return pkt.buildPacket(DHCP_SERVER, DHCP_CLIENT);
    }
}
