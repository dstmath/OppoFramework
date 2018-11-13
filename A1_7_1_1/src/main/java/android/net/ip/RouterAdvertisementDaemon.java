package android.net.ip;

import android.net.IpPrefix;
import android.net.NetworkUtils;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
public class RouterAdvertisementDaemon {
    private static final byte[] ALL_NODES = null;
    private static final int DAY_IN_SECONDS = 86400;
    private static final int DEFAULT_LIFETIME = 3600;
    private static final byte ICMPV6_ND_ROUTER_ADVERT = (byte) 0;
    private static final byte ICMPV6_ND_ROUTER_SOLICIT = (byte) 0;
    private static final int IPV6_MIN_MTU = 1280;
    private static final int MAX_RTR_ADV_INTERVAL_SEC = 600;
    private static final int MAX_URGENT_RTR_ADVERTISEMENTS = 5;
    private static final int MIN_DELAY_BETWEEN_RAS_SEC = 3;
    private static final int MIN_RA_HEADER_SIZE = 16;
    private static final int MIN_RTR_ADV_INTERVAL_SEC = 300;
    private static final String TAG = null;
    private final InetSocketAddress mAllNodes;
    @GuardedBy("mLock")
    private final DeprecatedInfoTracker mDeprecatedInfoTracker;
    private final byte[] mHwAddr;
    private final int mIfIndex;
    private final String mIfName;
    private final Object mLock;
    private volatile MulticastTransmitter mMulticastTransmitter;
    @GuardedBy("mLock")
    private final byte[] mRA;
    @GuardedBy("mLock")
    private int mRaLength;
    @GuardedBy("mLock")
    private RaParams mRaParams;
    private volatile FileDescriptor mSocket;
    private volatile UnicastResponder mUnicastResponder;

    private static class DeprecatedInfoTracker {
        private final HashMap<Inet6Address, Integer> mDnses;
        private final HashMap<IpPrefix, Integer> mPrefixes;

        /* synthetic */ DeprecatedInfoTracker(DeprecatedInfoTracker deprecatedInfoTracker) {
            this();
        }

        private DeprecatedInfoTracker() {
            this.mPrefixes = new HashMap();
            this.mDnses = new HashMap();
        }

        Set<IpPrefix> getPrefixes() {
            return this.mPrefixes.keySet();
        }

        void putPrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.put(ipp, Integer.valueOf(5));
            }
        }

        void removePrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.remove(ipp);
            }
        }

        Set<Inet6Address> getDnses() {
            return this.mDnses.keySet();
        }

        void putDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.put(dns, Integer.valueOf(5));
            }
        }

        void removeDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.remove(dns);
            }
        }

        boolean isEmpty() {
            return this.mPrefixes.isEmpty() ? this.mDnses.isEmpty() : false;
        }

        private boolean decrementCounters() {
            return decrementCounter(this.mPrefixes) | decrementCounter(this.mDnses);
        }

        private <T> boolean decrementCounter(HashMap<T, Integer> map) {
            boolean removed = false;
            Iterator<Entry<T, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<T, Integer> kv = (Entry) it.next();
                if (((Integer) kv.getValue()).intValue() == 0) {
                    it.remove();
                    removed = true;
                } else {
                    kv.setValue(Integer.valueOf(((Integer) kv.getValue()).intValue() - 1));
                }
            }
            return removed;
        }
    }

    private final class MulticastTransmitter extends Thread {
        private final Random mRandom;
        private final AtomicInteger mUrgentAnnouncements;

        /* synthetic */ MulticastTransmitter(RouterAdvertisementDaemon this$0, MulticastTransmitter multicastTransmitter) {
            this();
        }

        private MulticastTransmitter() {
            this.mRandom = new Random();
            this.mUrgentAnnouncements = new AtomicInteger(0);
        }

        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    Thread.sleep(getNextMulticastTransmitDelayMs());
                } catch (InterruptedException e) {
                }
                RouterAdvertisementDaemon.this.maybeSendRA(RouterAdvertisementDaemon.this.mAllNodes);
                synchronized (RouterAdvertisementDaemon.this.mLock) {
                    if (RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.decrementCounters()) {
                        RouterAdvertisementDaemon.this.assembleRaLocked();
                    }
                }
            }
        }

        public void hup() {
            this.mUrgentAnnouncements.set(4);
            interrupt();
        }

        /* JADX WARNING: Missing block: B:15:0x002d, code:
            if (r6.mUrgentAnnouncements.getAndDecrement() > 0) goto L_0x0031;
     */
        /* JADX WARNING: Missing block: B:16:0x002f, code:
            if (r0 == false) goto L_0x0038;
     */
        /* JADX WARNING: Missing block: B:18:0x0032, code:
            return 3;
     */
        /* JADX WARNING: Missing block: B:24:0x0040, code:
            return r6.mRandom.nextInt(300) + 300;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int getNextMulticastTransmitDelaySec() {
            synchronized (RouterAdvertisementDaemon.this.mLock) {
                if (RouterAdvertisementDaemon.this.mRaLength < 16) {
                    return RouterAdvertisementDaemon.DAY_IN_SECONDS;
                }
                boolean deprecationInProgress = !RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.isEmpty();
            }
        }

        private long getNextMulticastTransmitDelayMs() {
            return ((long) getNextMulticastTransmitDelaySec()) * 1000;
        }
    }

    public static class RaParams {
        public HashSet<Inet6Address> dnses;
        public boolean hasDefaultRoute;
        public int mtu;
        public HashSet<IpPrefix> prefixes;

        public RaParams() {
            this.hasDefaultRoute = false;
            this.mtu = RouterAdvertisementDaemon.IPV6_MIN_MTU;
            this.prefixes = new HashSet();
            this.dnses = new HashSet();
        }

        public RaParams(RaParams other) {
            this.hasDefaultRoute = other.hasDefaultRoute;
            this.mtu = other.mtu;
            this.prefixes = (HashSet) other.prefixes.clone();
            this.dnses = (HashSet) other.dnses.clone();
        }

        public static RaParams getDeprecatedRaParams(RaParams oldRa, RaParams newRa) {
            RaParams newlyDeprecated = new RaParams();
            if (oldRa != null) {
                for (IpPrefix ipp : oldRa.prefixes) {
                    if (newRa == null || !newRa.prefixes.contains(ipp)) {
                        newlyDeprecated.prefixes.add(ipp);
                    }
                }
                for (Inet6Address dns : oldRa.dnses) {
                    if (newRa == null || !newRa.dnses.contains(dns)) {
                        newlyDeprecated.dnses.add(dns);
                    }
                }
            }
            return newlyDeprecated;
        }
    }

    private final class UnicastResponder extends Thread {
        private final byte[] mSolication;
        private final InetSocketAddress solicitor;

        /* synthetic */ UnicastResponder(RouterAdvertisementDaemon this$0, UnicastResponder unicastResponder) {
            this();
        }

        private UnicastResponder() {
            this.solicitor = new InetSocketAddress();
            this.mSolication = new byte[RouterAdvertisementDaemon.IPV6_MIN_MTU];
        }

        /* JADX WARNING: Removed duplicated region for block: B:8:0x0031 A:{Splitter: B:2:0x0008, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:8:0x0031, code:
            r6 = move-exception;
     */
        /* JADX WARNING: Missing block: B:10:0x0038, code:
            if (android.net.ip.RouterAdvertisementDaemon.-wrap0(r8.this$0) != false) goto L_0x003a;
     */
        /* JADX WARNING: Missing block: B:11:0x003a, code:
            android.util.Log.e(android.net.ip.RouterAdvertisementDaemon.-get1(), "recvfrom error: " + r6);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    if (Os.recvfrom(RouterAdvertisementDaemon.this.mSocket, this.mSolication, 0, this.mSolication.length, 0, this.solicitor) >= 1 && this.mSolication[0] == RouterAdvertisementDaemon.ICMPV6_ND_ROUTER_SOLICIT) {
                        RouterAdvertisementDaemon.this.maybeSendRA(this.solicitor);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.ip.RouterAdvertisementDaemon.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.ip.RouterAdvertisementDaemon.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ip.RouterAdvertisementDaemon.<clinit>():void");
    }

    public RouterAdvertisementDaemon(String ifname, int ifindex, byte[] hwaddr) {
        this.mLock = new Object();
        this.mRA = new byte[IPV6_MIN_MTU];
        this.mIfName = ifname;
        this.mIfIndex = ifindex;
        this.mHwAddr = hwaddr;
        this.mAllNodes = new InetSocketAddress(getAllNodesForScopeId(this.mIfIndex), 0);
        this.mDeprecatedInfoTracker = new DeprecatedInfoTracker();
    }

    public void buildNewRa(RaParams deprecatedParams, RaParams newParams) {
        synchronized (this.mLock) {
            if (deprecatedParams != null) {
                this.mDeprecatedInfoTracker.putPrefixes(deprecatedParams.prefixes);
                this.mDeprecatedInfoTracker.putDnses(deprecatedParams.dnses);
            }
            if (newParams != null) {
                this.mDeprecatedInfoTracker.removePrefixes(newParams.prefixes);
                this.mDeprecatedInfoTracker.removeDnses(newParams.dnses);
            }
            this.mRaParams = newParams;
            assembleRaLocked();
        }
        maybeNotifyMulticastTransmitter();
    }

    public boolean start() {
        if (!createSocket()) {
            return false;
        }
        this.mMulticastTransmitter = new MulticastTransmitter(this, null);
        this.mMulticastTransmitter.start();
        this.mUnicastResponder = new UnicastResponder(this, null);
        this.mUnicastResponder.start();
        return true;
    }

    public void stop() {
        closeSocket();
        this.mMulticastTransmitter = null;
        this.mUnicastResponder = null;
    }

    private void assembleRaLocked() {
        ByteBuffer ra = ByteBuffer.wrap(this.mRA);
        ra.order(ByteOrder.BIG_ENDIAN);
        boolean shouldSendRA = false;
        try {
            boolean z;
            if (this.mRaParams != null) {
                z = this.mRaParams.hasDefaultRoute;
            } else {
                z = false;
            }
            putHeader(ra, z);
            putSlla(ra, this.mHwAddr);
            this.mRaLength = ra.position();
            if (this.mRaParams != null) {
                putMtu(ra, this.mRaParams.mtu);
                this.mRaLength = ra.position();
                for (IpPrefix ipp : this.mRaParams.prefixes) {
                    putPio(ra, ipp, DEFAULT_LIFETIME, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
                if (this.mRaParams.dnses.size() > 0) {
                    putRdnss(ra, this.mRaParams.dnses, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
            }
            for (IpPrefix ipp2 : this.mDeprecatedInfoTracker.getPrefixes()) {
                putPio(ra, ipp2, 0, 0);
                this.mRaLength = ra.position();
                shouldSendRA = true;
            }
            Set<Inet6Address> deprecatedDnses = this.mDeprecatedInfoTracker.getDnses();
            if (!deprecatedDnses.isEmpty()) {
                putRdnss(ra, deprecatedDnses, 0);
                this.mRaLength = ra.position();
                shouldSendRA = true;
            }
        } catch (BufferOverflowException e) {
            Log.e(TAG, "Could not construct new RA: " + e);
        }
        if (!shouldSendRA) {
            this.mRaLength = 0;
        }
    }

    private void maybeNotifyMulticastTransmitter() {
        MulticastTransmitter m = this.mMulticastTransmitter;
        if (m != null) {
            m.hup();
        }
    }

    private static Inet6Address getAllNodesForScopeId(int scopeId) {
        try {
            return Inet6Address.getByAddress("ff02::1", ALL_NODES, scopeId);
        } catch (UnknownHostException uhe) {
            Log.wtf(TAG, "Failed to construct ff02::1 InetAddress: " + uhe);
            return null;
        }
    }

    private static byte asByte(int value) {
        return (byte) value;
    }

    private static short asShort(int value) {
        return (short) value;
    }

    private static void putHeader(ByteBuffer ra, boolean hasDefaultRoute) {
        byte asByte;
        short asShort;
        ByteBuffer put = ra.put(ICMPV6_ND_ROUTER_ADVERT).put(asByte(0)).putShort(asShort(0)).put((byte) 64);
        if (hasDefaultRoute) {
            asByte = asByte(8);
        } else {
            asByte = asByte(0);
        }
        put = put.put(asByte);
        if (hasDefaultRoute) {
            asShort = asShort(DEFAULT_LIFETIME);
        } else {
            asShort = asShort(0);
        }
        put.putShort(asShort).putInt(0).putInt(0);
    }

    private static void putSlla(ByteBuffer ra, byte[] slla) {
        if (slla != null && slla.length == 6) {
            ra.put((byte) 1).put((byte) 1).put(slla);
        }
    }

    private static void putExpandedFlagsOption(ByteBuffer ra) {
        ra.put((byte) 26).put((byte) 1).putShort(asShort(0)).putInt(0);
    }

    private static void putMtu(ByteBuffer ra, int mtu) {
        ByteBuffer putShort = ra.put((byte) 5).put((byte) 1).putShort(asShort(0));
        if (mtu < IPV6_MIN_MTU) {
            mtu = IPV6_MIN_MTU;
        }
        putShort.putInt(mtu);
    }

    private static void putPio(ByteBuffer ra, IpPrefix ipp, int validTime, int preferredTime) {
        int prefixLength = ipp.getPrefixLength();
        if (prefixLength == 64) {
            if (validTime < 0) {
                validTime = 0;
            }
            if (preferredTime < 0) {
                preferredTime = 0;
            }
            if (preferredTime > validTime) {
                preferredTime = validTime;
            }
            ra.put((byte) 3).put((byte) 4).put(asByte(prefixLength)).put(asByte(192)).putInt(validTime).putInt(preferredTime).putInt(0).put(ipp.getAddress().getAddress());
        }
    }

    private static void putRio(ByteBuffer ra, IpPrefix ipp) {
        int prefixLength = ipp.getPrefixLength();
        if (prefixLength <= 64) {
            int i = prefixLength == 0 ? 1 : prefixLength <= 8 ? 2 : 3;
            byte RIO_NUM_8OCTETS = asByte(i);
            byte[] addr = ipp.getAddress().getAddress();
            ra.put((byte) 24).put(RIO_NUM_8OCTETS).put(asByte(prefixLength)).put(asByte(24)).putInt(DEFAULT_LIFETIME);
            if (prefixLength > 0) {
                if (prefixLength <= 64) {
                    i = 8;
                } else {
                    i = 16;
                }
                ra.put(addr, 0, i);
            }
        }
    }

    private static void putRdnss(ByteBuffer ra, Set<Inet6Address> dnses, int lifetime) {
        ra.put((byte) 25).put(asByte((dnses.size() * 2) + 1)).putShort(asShort(0)).putInt(lifetime);
        for (Inet6Address dns : dnses) {
            ra.put(dns.getAddress());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0036 A:{Splitter: B:1:0x0002, ExcHandler: android.system.ErrnoException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0036, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0037, code:
            android.util.Log.e(TAG, "Failed to create RA daemon socket: " + r1);
     */
    /* JADX WARNING: Missing block: B:7:0x0051, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean createSocket() {
        try {
            this.mSocket = Os.socket(OsConstants.AF_INET6, OsConstants.SOCK_RAW, OsConstants.IPPROTO_ICMPV6);
            Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(300));
            Os.setsockoptIfreq(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfName);
            NetworkUtils.protectFromVpn(this.mSocket);
            NetworkUtils.setupRaSocket(this.mSocket, this.mIfIndex);
            return true;
        } catch (Exception e) {
        }
    }

    private void closeSocket() {
        if (this.mSocket != null) {
            try {
                IoBridge.closeAndSignalBlockedThreads(this.mSocket);
            } catch (IOException e) {
            }
        }
        this.mSocket = null;
    }

    private boolean isSocketValid() {
        FileDescriptor s = this.mSocket;
        return s != null ? s.valid() : false;
    }

    private boolean isSuitableDestination(InetSocketAddress dest) {
        boolean z = true;
        if (this.mAllNodes.equals(dest)) {
            return true;
        }
        InetAddress destip = dest.getAddress();
        if (!(destip instanceof Inet6Address) || !destip.isLinkLocalAddress()) {
            z = false;
        } else if (((Inet6Address) destip).getScopeId() != this.mIfIndex) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0048 A:{Splitter: B:3:0x0008, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:18:0x0023, code:
            android.util.Log.d(TAG, "RA sendto " + r9.getAddress().getHostAddress());
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x0048, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:0x004d, code:
            if (isSocketValid() != false) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:26:0x004f, code:
            android.util.Log.e(TAG, "sendto error: " + r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeSendRA(InetSocketAddress dest) {
        SocketAddress dest2;
        if (dest2 == null || !isSuitableDestination(dest2)) {
            dest2 = this.mAllNodes;
        }
        try {
            synchronized (this.mLock) {
                if (this.mRaLength < 16) {
                    return;
                }
                Os.sendto(this.mSocket, this.mRA, 0, this.mRaLength, 0, dest2);
            }
        } catch (Exception e) {
        }
    }
}
