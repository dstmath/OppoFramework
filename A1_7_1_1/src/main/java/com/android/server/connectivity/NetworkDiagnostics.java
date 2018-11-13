package com.android.server.connectivity;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.oppo.IElsaManager;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;

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
public class NetworkDiagnostics {
    private static final String TAG = "NetworkDiagnostics";
    private static final InetAddress TEST_DNS4 = null;
    private static final InetAddress TEST_DNS6 = null;
    private final CountDownLatch mCountDownLatch;
    private final long mDeadlineTime;
    private final String mDescription;
    private final Map<InetAddress, Measurement> mDnsUdpChecks;
    private final Map<Pair<InetAddress, InetAddress>, Measurement> mExplicitSourceIcmpChecks;
    private final Map<InetAddress, Measurement> mIcmpChecks;
    private final Integer mInterfaceIndex;
    private final LinkProperties mLinkProperties;
    private final Network mNetwork;
    private final long mStartTime;
    private final long mTimeoutMs;

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
    public enum DnsResponseCode {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void");
        }
    }

    private class SimpleSocketCheck implements Closeable {
        protected final int mAddressFamily;
        protected FileDescriptor mFileDescriptor;
        protected final Measurement mMeasurement;
        protected SocketAddress mSocketAddress;
        protected final InetAddress mSource;
        protected final InetAddress mTarget;

        protected SimpleSocketCheck(InetAddress source, InetAddress target, Measurement measurement) {
            this.mMeasurement = measurement;
            if (target instanceof Inet6Address) {
                InetAddress targetWithScopeId = null;
                if (target.isLinkLocalAddress() && NetworkDiagnostics.this.mInterfaceIndex != null) {
                    try {
                        targetWithScopeId = Inet6Address.getByAddress(null, target.getAddress(), NetworkDiagnostics.this.mInterfaceIndex.intValue());
                    } catch (UnknownHostException e) {
                        this.mMeasurement.recordFailure(e.toString());
                    }
                }
                if (targetWithScopeId == null) {
                    targetWithScopeId = target;
                }
                this.mTarget = targetWithScopeId;
                this.mAddressFamily = OsConstants.AF_INET6;
            } else {
                this.mTarget = target;
                this.mAddressFamily = OsConstants.AF_INET;
            }
            this.mSource = source;
        }

        protected SimpleSocketCheck(NetworkDiagnostics this$0, InetAddress target, Measurement measurement) {
            this(null, target, measurement);
        }

        protected void setupSocket(int sockType, int protocol, long writeTimeout, long readTimeout, int dstPort) throws ErrnoException, IOException {
            this.mFileDescriptor = Os.socket(this.mAddressFamily, sockType, protocol);
            Os.setsockoptTimeval(this.mFileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(writeTimeout));
            Os.setsockoptTimeval(this.mFileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(readTimeout));
            NetworkDiagnostics.this.mNetwork.bindSocket(this.mFileDescriptor);
            if (this.mSource != null) {
                Os.bind(this.mFileDescriptor, this.mSource, 0);
            }
            Os.connect(this.mFileDescriptor, this.mTarget, dstPort);
            this.mSocketAddress = Os.getsockname(this.mFileDescriptor);
        }

        protected String getSocketAddressString() {
            InetSocketAddress inetSockAddr = this.mSocketAddress;
            InetAddress localAddr = inetSockAddr.getAddress();
            String str = localAddr instanceof Inet6Address ? "[%s]:%d" : "%s:%d";
            Object[] objArr = new Object[2];
            objArr[0] = localAddr.getHostAddress();
            objArr[1] = Integer.valueOf(inetSockAddr.getPort());
            return String.format(str, objArr);
        }

        public void close() {
            IoUtils.closeQuietly(this.mFileDescriptor);
        }
    }

    private class DnsUdpCheck extends SimpleSocketCheck implements Runnable {
        private static final int DNS_SERVER_PORT = 53;
        private static final int PACKET_BUFSIZE = 512;
        private static final int RR_TYPE_A = 1;
        private static final int RR_TYPE_AAAA = 28;
        private static final int TIMEOUT_RECV = 500;
        private static final int TIMEOUT_SEND = 100;
        private final int mQueryType;
        private final Random mRandom = new Random();

        private String responseCodeStr(int rcode) {
            try {
                return DnsResponseCode.values()[rcode].toString();
            } catch (IndexOutOfBoundsException e) {
                return String.valueOf(rcode);
            }
        }

        public DnsUdpCheck(InetAddress target, Measurement measurement) {
            super(NetworkDiagnostics.this, target, measurement);
            if (this.mAddressFamily == OsConstants.AF_INET6) {
                this.mQueryType = 28;
            } else {
                this.mQueryType = 1;
            }
            this.mMeasurement.description = "DNS UDP dst{" + this.mTarget.getHostAddress() + "}";
        }

        /* JADX WARNING: Removed duplicated region for block: B:23:0x0131 A:{Splitter: B:4:0x0014, ExcHandler: android.system.ErrnoException (r10_2 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x013c A:{Splitter: B:10:0x00b5, ExcHandler: android.system.ErrnoException (r10_1 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x014b A:{Splitter: B:13:0x00be, ExcHandler: android.system.ErrnoException (e android.system.ErrnoException)} */
        /* JADX WARNING: Missing block: B:23:0x0131, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:24:0x0132, code:
            r14.mMeasurement.recordFailure(r10.toString());
     */
        /* JADX WARNING: Missing block: B:25:0x013b, code:
            return;
     */
        /* JADX WARNING: Missing block: B:26:0x013c, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:27:0x013d, code:
            r14.mMeasurement.recordFailure(r10.toString());
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            if (this.mMeasurement.finishTime > 0) {
                NetworkDiagnostics.this.mCountDownLatch.countDown();
                return;
            }
            try {
                setupSocket(OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP, 100, 500, 53);
                Measurement measurement = this.mMeasurement;
                measurement.description += " src{" + getSocketAddressString() + "}";
                String sixRandomDigits = String.valueOf(this.mRandom.nextInt(900000) + 100000);
                measurement = this.mMeasurement;
                measurement.description += " qtype{" + this.mQueryType + "}" + " qname{" + sixRandomDigits + "-android-ds.metric.gstatic.com}";
                byte[] dnsPacket = getDnsQueryPacket(sixRandomDigits);
                int count = 0;
                this.mMeasurement.startTime = NetworkDiagnostics.now();
                while (NetworkDiagnostics.now() < NetworkDiagnostics.this.mDeadlineTime - 1000) {
                    count++;
                    try {
                        Os.write(this.mFileDescriptor, dnsPacket, 0, dnsPacket.length);
                        try {
                            String rcodeStr;
                            ByteBuffer reply = ByteBuffer.allocate(512);
                            Os.read(this.mFileDescriptor, reply);
                            if (reply.limit() > 3) {
                                rcodeStr = " " + responseCodeStr(reply.get(3) & 15);
                            } else {
                                rcodeStr = IElsaManager.EMPTY_PACKAGE;
                            }
                            this.mMeasurement.recordSuccess("1/" + count + rcodeStr);
                            break;
                        } catch (ErrnoException e) {
                        }
                    } catch (Exception e2) {
                    }
                }
                if (this.mMeasurement.finishTime == 0) {
                    this.mMeasurement.recordFailure("0/" + count);
                }
                close();
            } catch (Exception e3) {
            }
        }

        private byte[] getDnsQueryPacket(String sixRandomDigits) {
            byte[] rnd = sixRandomDigits.getBytes(StandardCharsets.US_ASCII);
            byte[] bArr = new byte[54];
            bArr[0] = (byte) this.mRandom.nextInt();
            bArr[1] = (byte) this.mRandom.nextInt();
            bArr[2] = (byte) 1;
            bArr[3] = (byte) 0;
            bArr[4] = (byte) 0;
            bArr[5] = (byte) 1;
            bArr[6] = (byte) 0;
            bArr[7] = (byte) 0;
            bArr[8] = (byte) 0;
            bArr[9] = (byte) 0;
            bArr[10] = (byte) 0;
            bArr[11] = (byte) 0;
            bArr[12] = (byte) 17;
            bArr[13] = rnd[0];
            bArr[14] = rnd[1];
            bArr[15] = rnd[2];
            bArr[16] = rnd[3];
            bArr[17] = rnd[4];
            bArr[18] = rnd[5];
            bArr[19] = (byte) 45;
            bArr[20] = (byte) 97;
            bArr[21] = (byte) 110;
            bArr[22] = (byte) 100;
            bArr[23] = (byte) 114;
            bArr[24] = (byte) 111;
            bArr[25] = (byte) 105;
            bArr[26] = (byte) 100;
            bArr[27] = (byte) 45;
            bArr[28] = (byte) 100;
            bArr[29] = (byte) 115;
            bArr[30] = (byte) 6;
            bArr[31] = (byte) 109;
            bArr[32] = (byte) 101;
            bArr[33] = (byte) 116;
            bArr[34] = (byte) 114;
            bArr[35] = (byte) 105;
            bArr[36] = (byte) 99;
            bArr[37] = (byte) 7;
            bArr[38] = (byte) 103;
            bArr[39] = (byte) 115;
            bArr[40] = (byte) 116;
            bArr[41] = (byte) 97;
            bArr[42] = (byte) 116;
            bArr[43] = (byte) 105;
            bArr[44] = (byte) 99;
            bArr[45] = (byte) 3;
            bArr[46] = (byte) 99;
            bArr[47] = (byte) 111;
            bArr[48] = (byte) 109;
            bArr[49] = (byte) 0;
            bArr[50] = (byte) 0;
            bArr[51] = (byte) this.mQueryType;
            bArr[52] = (byte) 0;
            bArr[53] = (byte) 1;
            return bArr;
        }
    }

    private class IcmpCheck extends SimpleSocketCheck implements Runnable {
        private static final int ICMPV4_ECHO_REQUEST = 8;
        private static final int ICMPV6_ECHO_REQUEST = 128;
        private static final int PACKET_BUFSIZE = 512;
        private static final int TIMEOUT_RECV = 300;
        private static final int TIMEOUT_SEND = 100;
        private final int mIcmpType;
        private final int mProtocol;

        public IcmpCheck(InetAddress source, InetAddress target, Measurement measurement) {
            super(source, target, measurement);
            if (this.mAddressFamily == OsConstants.AF_INET6) {
                this.mProtocol = OsConstants.IPPROTO_ICMPV6;
                this.mIcmpType = 128;
                this.mMeasurement.description = "ICMPv6";
            } else {
                this.mProtocol = OsConstants.IPPROTO_ICMP;
                this.mIcmpType = 8;
                this.mMeasurement.description = "ICMPv4";
            }
            Measurement measurement2 = this.mMeasurement;
            measurement2.description += " dst{" + this.mTarget.getHostAddress() + "}";
        }

        public IcmpCheck(NetworkDiagnostics this$0, InetAddress target, Measurement measurement) {
            this(null, target, measurement);
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x00e4 A:{Splitter: B:4:0x0014, ExcHandler: android.system.ErrnoException (r9_2 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x00ef A:{Splitter: B:10:0x0092, ExcHandler: android.system.ErrnoException (r9_1 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x00fa A:{Splitter: B:13:0x009b, ExcHandler: android.system.ErrnoException (e android.system.ErrnoException)} */
        /* JADX WARNING: Missing block: B:20:0x00e4, code:
            r9 = move-exception;
     */
        /* JADX WARNING: Missing block: B:21:0x00e5, code:
            r12.mMeasurement.recordFailure(r9.toString());
     */
        /* JADX WARNING: Missing block: B:22:0x00ee, code:
            return;
     */
        /* JADX WARNING: Missing block: B:23:0x00ef, code:
            r9 = move-exception;
     */
        /* JADX WARNING: Missing block: B:24:0x00f0, code:
            r12.mMeasurement.recordFailure(r9.toString());
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            if (this.mMeasurement.finishTime > 0) {
                NetworkDiagnostics.this.mCountDownLatch.countDown();
                return;
            }
            try {
                setupSocket(OsConstants.SOCK_DGRAM, this.mProtocol, 100, 300, 0);
                Measurement measurement = this.mMeasurement;
                measurement.description += " src{" + getSocketAddressString() + "}";
                byte[] icmpPacket = new byte[8];
                icmpPacket[0] = (byte) this.mIcmpType;
                icmpPacket[1] = (byte) 0;
                icmpPacket[2] = (byte) 0;
                icmpPacket[3] = (byte) 0;
                icmpPacket[4] = (byte) 0;
                icmpPacket[5] = (byte) 0;
                icmpPacket[6] = (byte) 0;
                icmpPacket[7] = (byte) 0;
                int count = 0;
                this.mMeasurement.startTime = NetworkDiagnostics.now();
                while (NetworkDiagnostics.now() < NetworkDiagnostics.this.mDeadlineTime - 400) {
                    count++;
                    icmpPacket[icmpPacket.length - 1] = (byte) count;
                    try {
                        Os.write(this.mFileDescriptor, icmpPacket, 0, icmpPacket.length);
                        try {
                            Os.read(this.mFileDescriptor, ByteBuffer.allocate(512));
                            this.mMeasurement.recordSuccess("1/" + count);
                            break;
                        } catch (ErrnoException e) {
                        }
                    } catch (Exception e2) {
                    }
                }
                if (this.mMeasurement.finishTime == 0) {
                    this.mMeasurement.recordFailure("0/" + count);
                }
                close();
            } catch (Exception e3) {
            }
        }
    }

    public class Measurement {
        private static final String FAILED = "FAILED";
        private static final String SUCCEEDED = "SUCCEEDED";
        String description = IElsaManager.EMPTY_PACKAGE;
        long finishTime;
        String result = IElsaManager.EMPTY_PACKAGE;
        long startTime;
        private boolean succeeded;
        Thread thread;

        public boolean checkSucceeded() {
            return this.succeeded;
        }

        void recordSuccess(String msg) {
            maybeFixupTimes();
            this.succeeded = true;
            this.result = "SUCCEEDED: " + msg;
            if (NetworkDiagnostics.this.mCountDownLatch != null) {
                NetworkDiagnostics.this.mCountDownLatch.countDown();
            }
        }

        void recordFailure(String msg) {
            maybeFixupTimes();
            this.succeeded = false;
            this.result = "FAILED: " + msg;
            if (NetworkDiagnostics.this.mCountDownLatch != null) {
                NetworkDiagnostics.this.mCountDownLatch.countDown();
            }
        }

        private void maybeFixupTimes() {
            if (this.finishTime == 0) {
                this.finishTime = NetworkDiagnostics.now();
            }
            if (this.startTime == 0) {
                this.startTime = this.finishTime;
            }
        }

        public String toString() {
            return this.description + ": " + this.result + " (" + (this.finishTime - this.startTime) + "ms)";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void");
    }

    private static final long now() {
        return SystemClock.elapsedRealtime();
    }

    public NetworkDiagnostics(Network network, LinkProperties lp, long timeoutMs) {
        this.mIcmpChecks = new HashMap();
        this.mExplicitSourceIcmpChecks = new HashMap();
        this.mDnsUdpChecks = new HashMap();
        this.mNetwork = network;
        this.mLinkProperties = lp;
        this.mInterfaceIndex = getInterfaceIndex(this.mLinkProperties.getInterfaceName());
        this.mTimeoutMs = timeoutMs;
        this.mStartTime = now();
        this.mDeadlineTime = this.mStartTime + this.mTimeoutMs;
        if (this.mLinkProperties.isReachable(TEST_DNS4)) {
            this.mLinkProperties.addDnsServer(TEST_DNS4);
        }
        if (this.mLinkProperties.hasGlobalIPv6Address() || this.mLinkProperties.hasIPv6DefaultRoute()) {
            this.mLinkProperties.addDnsServer(TEST_DNS6);
        }
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                prepareIcmpMeasurement(gateway);
                if (route.isIPv6Default()) {
                    prepareExplicitSourceIcmpMeasurements(gateway);
                }
            }
        }
        for (InetAddress nameserver : this.mLinkProperties.getDnsServers()) {
            prepareIcmpMeasurement(nameserver);
            prepareDnsMeasurement(nameserver);
        }
        this.mCountDownLatch = new CountDownLatch(totalMeasurementCount());
        startMeasurements();
        this.mDescription = "ifaces{" + TextUtils.join(",", this.mLinkProperties.getAllInterfaceNames()) + "}" + " index{" + this.mInterfaceIndex + "}" + " network{" + this.mNetwork + "}" + " nethandle{" + this.mNetwork.getNetworkHandle() + "}";
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000d A:{Splitter: B:0:0x0000, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Missing block: B:5:0x000f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Integer getInterfaceIndex(String ifname) {
        try {
            return Integer.valueOf(NetworkInterface.getByName(ifname).getIndex());
        } catch (NullPointerException e) {
        }
    }

    private void prepareIcmpMeasurement(InetAddress target) {
        if (!this.mIcmpChecks.containsKey(target)) {
            Measurement measurement = new Measurement();
            measurement.thread = new Thread(new IcmpCheck(this, target, measurement));
            this.mIcmpChecks.put(target, measurement);
        }
    }

    private void prepareExplicitSourceIcmpMeasurements(InetAddress target) {
        for (LinkAddress l : this.mLinkProperties.getLinkAddresses()) {
            InetAddress source = l.getAddress();
            if ((source instanceof Inet6Address) && l.isGlobalPreferred()) {
                Pair<InetAddress, InetAddress> srcTarget = new Pair(source, target);
                if (!this.mExplicitSourceIcmpChecks.containsKey(srcTarget)) {
                    Measurement measurement = new Measurement();
                    measurement.thread = new Thread(new IcmpCheck(source, target, measurement));
                    this.mExplicitSourceIcmpChecks.put(srcTarget, measurement);
                }
            }
        }
    }

    private void prepareDnsMeasurement(InetAddress target) {
        if (!this.mDnsUdpChecks.containsKey(target)) {
            Measurement measurement = new Measurement();
            measurement.thread = new Thread(new DnsUdpCheck(target, measurement));
            this.mDnsUdpChecks.put(target, measurement);
        }
    }

    private int totalMeasurementCount() {
        return (this.mIcmpChecks.size() + this.mExplicitSourceIcmpChecks.size()) + this.mDnsUdpChecks.size();
    }

    private void startMeasurements() {
        for (Measurement measurement : this.mIcmpChecks.values()) {
            measurement.thread.start();
        }
        for (Measurement measurement2 : this.mExplicitSourceIcmpChecks.values()) {
            measurement2.thread.start();
        }
        for (Measurement measurement22 : this.mDnsUdpChecks.values()) {
            measurement22.thread.start();
        }
    }

    public void waitForMeasurements() {
        try {
            this.mCountDownLatch.await(this.mDeadlineTime - now(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
    }

    public List<Measurement> getMeasurements() {
        ArrayList<Measurement> measurements = new ArrayList(totalMeasurementCount());
        for (Entry<InetAddress, Measurement> entry : this.mIcmpChecks.entrySet()) {
            if (entry.getKey() instanceof Inet4Address) {
                measurements.add((Measurement) entry.getValue());
            }
        }
        for (Entry<Pair<InetAddress, InetAddress>, Measurement> entry2 : this.mExplicitSourceIcmpChecks.entrySet()) {
            if (((Pair) entry2.getKey()).first instanceof Inet4Address) {
                measurements.add((Measurement) entry2.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry3 : this.mDnsUdpChecks.entrySet()) {
            if (entry3.getKey() instanceof Inet4Address) {
                measurements.add((Measurement) entry3.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry32 : this.mIcmpChecks.entrySet()) {
            if (entry32.getKey() instanceof Inet6Address) {
                measurements.add((Measurement) entry32.getValue());
            }
        }
        for (Entry<Pair<InetAddress, InetAddress>, Measurement> entry22 : this.mExplicitSourceIcmpChecks.entrySet()) {
            if (((Pair) entry22.getKey()).first instanceof Inet6Address) {
                measurements.add((Measurement) entry22.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry322 : this.mDnsUdpChecks.entrySet()) {
            if (entry322.getKey() instanceof Inet6Address) {
                measurements.add((Measurement) entry322.getValue());
            }
        }
        return measurements;
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("NetworkDiagnostics:" + this.mDescription);
        long unfinished = this.mCountDownLatch.getCount();
        if (unfinished > 0) {
            pw.println("WARNING: countdown wait incomplete: " + unfinished + " unfinished measurements");
        }
        pw.increaseIndent();
        for (Measurement m : getMeasurements()) {
            pw.println((m.checkSucceeded() ? "." : "F") + "  " + m.toString());
        }
        pw.decreaseIndent();
    }
}
