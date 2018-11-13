package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import sun.net.ResourceManager;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class DatagramChannelImpl extends DatagramChannel implements SelChImpl {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f140-assertionsDisabled = false;
    private static final int ST_CONNECTED = 1;
    private static final int ST_KILLED = 2;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
    private InetAddress cachedSenderInetAddress;
    private int cachedSenderPort;
    private final ProtocolFamily family;
    final FileDescriptor fd;
    private final int fdVal;
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object readLock;
    private volatile long readerThread;
    private InetSocketAddress remoteAddress;
    private boolean reuseAddressEmulated;
    private SocketAddress sender;
    private DatagramSocket socket;
    private int state;
    private final Object stateLock;
    private final Object writeLock;
    private volatile long writerThread;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<init>():void, dex: 
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
        private DefaultOptionsHolder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.util.Set<java.net.SocketOption<?>> defaultOptions() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.DatagramChannelImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.DatagramChannelImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.<clinit>():void");
    }

    private static native void disconnect0(FileDescriptor fileDescriptor, boolean z) throws IOException;

    private static native void initIDs();

    private native int receive0(FileDescriptor fileDescriptor, long j, int i, boolean z) throws IOException;

    private native int send0(boolean z, FileDescriptor fileDescriptor, long j, int i, InetAddress inetAddress, int i2) throws IOException;

    public DatagramChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        ResourceManager.beforeUdpCreate();
        try {
            this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
            this.fd = Net.socket(this.family, f140-assertionsDisabled);
            this.fdVal = IOUtil.fdVal(this.fd);
            this.state = 0;
        } catch (IOException ioe) {
            ResourceManager.afterUdpClose();
            throw ioe;
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, ProtocolFamily family) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        if (family == StandardProtocolFamily.INET || family == StandardProtocolFamily.INET6) {
            if (family != StandardProtocolFamily.INET6 || Net.isIPv6Available()) {
                this.family = family;
                this.fd = Net.socket(family, f140-assertionsDisabled);
                this.fdVal = IOUtil.fdVal(this.fd);
                this.state = 0;
                return;
            }
            throw new UnsupportedOperationException("IPv6 not available");
        } else if (family == null) {
            throw new NullPointerException("'family' is null");
        } else {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, FileDescriptor fd) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 0;
        this.localAddress = Net.localAddress(fd);
    }

    public DatagramSocket socket() {
        DatagramSocket datagramSocket;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = DatagramSocketAdaptor.create(this);
            }
            datagramSocket = this.socket;
        }
        return datagramSocket;
    }

    public SocketAddress getLocalAddress() throws IOException {
        SocketAddress revealedLocalAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                revealedLocalAddress = Net.getRevealedLocalAddress(this.localAddress);
            } else {
                throw new ClosedChannelException();
            }
        }
        return revealedLocalAddress;
    }

    public SocketAddress getRemoteAddress() throws IOException {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                socketAddress = this.remoteAddress;
            } else {
                throw new ClosedChannelException();
            }
        }
        return socketAddress;
    }

    /* JADX WARNING: Missing block: B:16:0x004b, code:
            return r8;
     */
    /* JADX WARNING: Missing block: B:43:0x008f, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (name == StandardSocketOptions.IP_TOS) {
                    if (this.family == StandardProtocolFamily.INET) {
                        Net.setSocketOption(this.fd, this.family, name, value);
                    }
                } else if (name == StandardSocketOptions.IP_MULTICAST_TTL || name == StandardSocketOptions.IP_MULTICAST_LOOP) {
                    Net.setSocketOption(this.fd, this.family, name, value);
                    return this;
                } else if (name != StandardSocketOptions.IP_MULTICAST_IF) {
                    if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind() && this.localAddress != null) {
                        this.reuseAddressEmulated = true;
                        this.isReuseAddress = ((Boolean) value).booleanValue();
                    }
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    return this;
                } else if (value == null) {
                    throw new IllegalArgumentException("Cannot set IP_MULTICAST_IF to 'null'");
                } else {
                    NetworkInterface interf = (NetworkInterface) value;
                    if (this.family == StandardProtocolFamily.INET6) {
                        int index = interf.getIndex();
                        if (index == -1) {
                            throw new IOException("Network interface cannot be identified");
                        }
                        Net.setInterface6(this.fd, index);
                    } else {
                        Inet4Address target = Net.anyInet4Address(interf);
                        if (target == null) {
                            throw new IOException("Network interface not configured for IPv4");
                        }
                        Net.setInterface4(this.fd, Net.inet4AsInt(target));
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                ensureOpen();
                T socketOption;
                if (name == StandardSocketOptions.IP_TOS) {
                    if (this.family == StandardProtocolFamily.INET) {
                        socketOption = Net.getSocketOption(this.fd, this.family, name);
                        return socketOption;
                    }
                    socketOption = Integer.valueOf(0);
                    return socketOption;
                } else if (name == StandardSocketOptions.IP_MULTICAST_TTL || name == StandardSocketOptions.IP_MULTICAST_LOOP) {
                    socketOption = Net.getSocketOption(this.fd, this.family, name);
                    return socketOption;
                } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
                    NetworkInterface ni;
                    if (this.family == StandardProtocolFamily.INET) {
                        int address = Net.getInterface4(this.fd);
                        if (address == 0) {
                            return null;
                        }
                        ni = NetworkInterface.getByInetAddress(Net.inet4FromInt(address));
                        if (ni == null) {
                            throw new IOException("Unable to map address to interface");
                        }
                        return ni;
                    }
                    int index = Net.getInterface6(this.fd);
                    if (index == 0) {
                        return null;
                    }
                    ni = NetworkInterface.getByIndex(index);
                    if (ni == null) {
                        throw new IOException("Unable to map index to interface");
                    }
                    return ni;
                } else if (name == StandardSocketOptions.SO_REUSEADDR && this.reuseAddressEmulated) {
                    socketOption = Boolean.valueOf(this.isReuseAddress);
                    return socketOption;
                } else {
                    socketOption = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return socketOption;
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:30:0x004d, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:53:0x008f, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:61:0x00a9, code:
            if (r0 == null) goto L_0x00ae;
     */
    /* JADX WARNING: Missing block: B:63:?, code:
            sun.nio.ch.Util.releaseTemporaryDirectBuffer(r0);
     */
    /* JADX WARNING: Missing block: B:64:0x00ae, code:
            r14.readerThread = 0;
     */
    /* JADX WARNING: Missing block: B:65:0x00b2, code:
            if (r2 > 0) goto L_0x00b6;
     */
    /* JADX WARNING: Missing block: B:66:0x00b4, code:
            if (r2 != -2) goto L_0x00c9;
     */
    /* JADX WARNING: Missing block: B:67:0x00b6, code:
            end(r6);
     */
    /* JADX WARNING: Missing block: B:68:0x00bb, code:
            if (-assertionsDisabled != false) goto L_0x00cb;
     */
    /* JADX WARNING: Missing block: B:70:0x00c1, code:
            if (sun.nio.ch.IOStatus.check(r2) != false) goto L_0x00cb;
     */
    /* JADX WARNING: Missing block: B:72:0x00c8, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:73:0x00c9, code:
            r6 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:75:0x00cc, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:81:?, code:
            r0.flip();
            r15.put(r0);
     */
    /* JADX WARNING: Missing block: B:101:0x0111, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        boolean z = true;
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (dst == null) {
            throw new NullPointerException();
        } else if (this.localAddress == null) {
            return null;
        } else {
            synchronized (this.readLock) {
                ensureOpen();
                int n = 0;
                ByteBuffer byteBuffer = null;
                try {
                    begin();
                    if (isOpen()) {
                        SecurityManager security = System.getSecurityManager();
                        this.readerThread = NativeThread.current();
                        if (isConnected() || security == null) {
                            do {
                                n = receive(this.fd, dst);
                                if (n != -3) {
                                    break;
                                }
                            } while (isOpen());
                            if (n == -2) {
                                this.readerThread = 0;
                                if (n <= 0 && n != -2) {
                                    z = f140-assertionsDisabled;
                                }
                                end(z);
                                if (f140-assertionsDisabled || IOStatus.check(n)) {
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                        byteBuffer = Util.getTemporaryDirectBuffer(dst.remaining());
                        while (true) {
                            n = receive(this.fd, byteBuffer);
                            if (n != -3 || !isOpen()) {
                                if (n != -2) {
                                    InetSocketAddress isa = this.sender;
                                    security.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                                    break;
                                }
                                break;
                            }
                        }
                        SocketAddress socketAddress = this.sender;
                        if (byteBuffer != null) {
                            Util.releaseTemporaryDirectBuffer(byteBuffer);
                        }
                        this.readerThread = 0;
                        if (n <= 0 && n != -2) {
                            z = f140-assertionsDisabled;
                        }
                        end(z);
                        if (f140-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.readerThread = 0;
                    end(f140-assertionsDisabled);
                    if (f140-assertionsDisabled || IOStatus.check(0)) {
                    } else {
                        throw new AssertionError();
                    }
                } catch (SecurityException e) {
                    byteBuffer.clear();
                } catch (Throwable th) {
                    if (byteBuffer != null) {
                        Util.releaseTemporaryDirectBuffer(byteBuffer);
                    }
                    this.readerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = f140-assertionsDisabled;
                    }
                    end(z);
                    if (!f140-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    private int receive(FileDescriptor fd, ByteBuffer dst) throws IOException {
        int i = 0;
        int pos = dst.position();
        int lim = dst.limit();
        if (!f140-assertionsDisabled) {
            if (pos <= lim) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : 0;
        if ((dst instanceof DirectBuffer) && rem > 0) {
            return receiveIntoNativeBuffer(fd, dst, rem, pos);
        }
        int newSize = Math.max(rem, 1);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            BlockGuard.getThreadPolicy().onNetwork();
            int n = receiveIntoNativeBuffer(fd, bb, newSize, 0);
            bb.flip();
            if (n > 0 && rem > 0) {
                dst.put(bb);
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return n;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int receiveIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, int rem, int pos) throws IOException {
        FileDescriptor fileDescriptor = fd;
        int i = rem;
        int n = receive0(fileDescriptor, ((long) pos) + ((DirectBuffer) bb).address(), i, isConnected());
        if (n > 0) {
            bb.position(pos + n);
        }
        return n;
    }

    /* JADX WARNING: Missing block: B:35:0x004b, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            begin();
            r4 = isOpen();
     */
    /* JADX WARNING: Missing block: B:38:0x0053, code:
            if (r4 != null) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            r11.writerThread = r4;
            end(-assertionsDisabled);
     */
    /* JADX WARNING: Missing block: B:41:0x005f, code:
            if (-assertionsDisabled != false) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:43:0x0065, code:
            if (sun.nio.ch.IOStatus.check(r2) != false) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:45:0x006c, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:58:0x0092, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            r11.writerThread = sun.nio.ch.NativeThread.current();
            dalvik.system.BlockGuard.getThreadPolicy().onNetwork();
     */
    /* JADX WARNING: Missing block: B:61:0x00a0, code:
            r2 = send(r11.fd, r12, r1);
     */
    /* JADX WARNING: Missing block: B:62:0x00a7, code:
            if (r2 != -3) goto L_0x00af;
     */
    /* JADX WARNING: Missing block: B:64:0x00ad, code:
            if (isOpen() != false) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:65:0x00af, code:
            r8 = r11.stateLock;
     */
    /* JADX WARNING: Missing block: B:66:0x00b1, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:69:0x00b6, code:
            if (isOpen() == false) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:71:0x00ba, code:
            if (r11.localAddress != null) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:72:0x00bc, code:
            r11.localAddress = sun.nio.ch.Net.localAddress(r11.fd);
     */
    /* JADX WARNING: Missing block: B:74:?, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:75:0x00c5, code:
            r4 = sun.nio.ch.IOStatus.normalize(r2);
     */
    /* JADX WARNING: Missing block: B:78:?, code:
            r11.writerThread = 0;
     */
    /* JADX WARNING: Missing block: B:79:0x00cd, code:
            if (r2 > 0) goto L_0x00d1;
     */
    /* JADX WARNING: Missing block: B:80:0x00cf, code:
            if (r2 != -2) goto L_0x0103;
     */
    /* JADX WARNING: Missing block: B:81:0x00d1, code:
            end(r5);
     */
    /* JADX WARNING: Missing block: B:82:0x00d6, code:
            if (-assertionsDisabled != false) goto L_0x0105;
     */
    /* JADX WARNING: Missing block: B:84:0x00dc, code:
            if (sun.nio.ch.IOStatus.check(r2) != false) goto L_0x0105;
     */
    /* JADX WARNING: Missing block: B:86:0x00e3, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:94:?, code:
            r11.writerThread = 0;
     */
    /* JADX WARNING: Missing block: B:97:0x00f0, code:
            end(r5);
     */
    /* JADX WARNING: Missing block: B:98:0x00f5, code:
            if (-assertionsDisabled != false) goto L_0x0109;
     */
    /* JADX WARNING: Missing block: B:101:0x00fd, code:
            r4 = new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:102:0x0103, code:
            r5 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:104:0x0106, code:
            return r4;
     */
    /* JADX WARNING: Missing block: B:105:0x0107, code:
            r5 = -assertionsDisabled;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        boolean z = true;
        if (src == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            ensureOpen();
            InetSocketAddress isa = Net.checkAddress(target);
            InetAddress ia = isa.getAddress();
            if (ia == null) {
                throw new IOException("Target address not resolved");
            }
            synchronized (this.stateLock) {
                if (isConnected()) {
                    if (target.equals(this.remoteAddress)) {
                        int write = write(src);
                        return write;
                    }
                    throw new IllegalArgumentException("Connected address not equal to target address");
                } else if (target == null) {
                    throw new NullPointerException();
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        if (ia.isMulticastAddress()) {
                            sm.checkMulticast(ia);
                        } else {
                            sm.checkConnect(ia.getHostAddress(), isa.getPort());
                        }
                    }
                }
            }
        }
    }

    private int send(FileDescriptor fd, ByteBuffer src, InetSocketAddress target) throws IOException {
        int rem = 0;
        if (src instanceof DirectBuffer) {
            return sendFromNativeBuffer(fd, src, target);
        }
        int pos = src.position();
        int lim = src.limit();
        if (!f140-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        }
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd, bb, target);
            if (n > 0) {
                src.position(pos + n);
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return n;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int sendFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, InetSocketAddress target) throws IOException {
        int written;
        int pos = bb.position();
        int lim = bb.limit();
        if (!f140-assertionsDisabled) {
            if ((pos <= lim ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : 0;
        try {
            written = send0(this.family != StandardProtocolFamily.INET ? true : f140-assertionsDisabled, fd, ((DirectBuffer) bb).address() + ((long) pos), rem, target.getAddress(), target.getPort());
        } catch (PortUnreachableException pue) {
            if (isConnected()) {
                throw pue;
            }
            written = rem;
        }
        if (written > 0) {
            bb.position(pos + written);
        }
        return written;
    }

    /* JADX WARNING: Missing block: B:35:0x004a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:56:0x0086, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.readLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                if (isOpen()) {
                    int n;
                    this.readerThread = NativeThread.current();
                    while (true) {
                        n = IOUtil.read(this.fd, buf, -1, nd);
                        if (n == -3) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    }
                    int normalize = IOStatus.normalize(n);
                    this.readerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = f140-assertionsDisabled;
                    }
                    end(z);
                    if (f140-assertionsDisabled || IOStatus.check(n)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.readerThread = 0;
                end(f140-assertionsDisabled);
                if (f140-assertionsDisabled || IOStatus.check(0)) {
                } else {
                    throw new AssertionError();
                }
            } finally {
                this.readerThread = 0;
                if (null <= null && 0 != -2) {
                    z = f140-assertionsDisabled;
                }
                end(z);
                if (!f140-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:61:0x0096, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                long isOpen = isOpen();
                if (isOpen == null) {
                    this.readerThread = isOpen;
                    end(f140-assertionsDisabled);
                    if (f140-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                long n;
                this.readerThread = NativeThread.current();
                while (true) {
                    n = IOUtil.read(this.fd, dsts, offset, length, nd);
                    if (n == -3) {
                        if (!isOpen()) {
                            break;
                        }
                    }
                    break;
                }
                long normalize = IOStatus.normalize(n);
                this.readerThread = 0;
                boolean z = (n > 0 || n == -2) ? true : f140-assertionsDisabled;
                end(z);
                if (f140-assertionsDisabled || IOStatus.check(n)) {
                } else {
                    throw new AssertionError();
                }
            } finally {
                this.readerThread = 0;
                boolean z2 = (0 > 0 || 0 == -2) ? true : f140-assertionsDisabled;
                end(z2);
                if (!f140-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x004a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:56:0x0086, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int write(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                if (isOpen()) {
                    int n;
                    this.writerThread = NativeThread.current();
                    while (true) {
                        n = IOUtil.write(this.fd, buf, -1, nd);
                        if (n == -3) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    }
                    int normalize = IOStatus.normalize(n);
                    this.writerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = f140-assertionsDisabled;
                    }
                    end(z);
                    if (f140-assertionsDisabled || IOStatus.check(n)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.writerThread = 0;
                end(f140-assertionsDisabled);
                if (f140-assertionsDisabled || IOStatus.check(0)) {
                } else {
                    throw new AssertionError();
                }
            } finally {
                this.writerThread = 0;
                if (null <= null && 0 != -2) {
                    z = f140-assertionsDisabled;
                }
                end(z);
                if (!f140-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:61:0x0096, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                long isOpen = isOpen();
                if (isOpen == null) {
                    this.writerThread = isOpen;
                    end(f140-assertionsDisabled);
                    if (f140-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                long n;
                this.writerThread = NativeThread.current();
                while (true) {
                    n = IOUtil.write(this.fd, srcs, offset, length, nd);
                    if (n == -3) {
                        if (!isOpen()) {
                            break;
                        }
                    }
                    break;
                }
                long normalize = IOStatus.normalize(n);
                this.writerThread = 0;
                boolean z = (n > 0 || n == -2) ? true : f140-assertionsDisabled;
                end(z);
                if (f140-assertionsDisabled || IOStatus.check(n)) {
                } else {
                    throw new AssertionError();
                }
            } finally {
                this.writerThread = 0;
                boolean z2 = (0 > 0 || 0 == -2) ? true : f140-assertionsDisabled;
                end(z2);
                if (!f140-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public SocketAddress localAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.localAddress;
        }
        return socketAddress;
    }

    public SocketAddress remoteAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.remoteAddress;
        }
        return socketAddress;
    }

    public DatagramChannel bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpen();
                    if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    }
                    InetSocketAddress isa;
                    if (local == null) {
                        isa = this.family == StandardProtocolFamily.INET ? new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0) : new InetSocketAddress(0);
                    } else {
                        isa = Net.checkAddress(local);
                        if (this.family == StandardProtocolFamily.INET && !(isa.getAddress() instanceof Inet4Address)) {
                            throw new UnsupportedAddressTypeException();
                        }
                    }
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkListen(isa.getPort());
                    }
                    Net.bind(this.family, this.fd, isa.getAddress(), isa.getPort());
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
        }
        return this;
    }

    public boolean isConnected() {
        boolean z = true;
        synchronized (this.stateLock) {
            if (this.state != 1) {
                z = f140-assertionsDisabled;
            }
        }
        return z;
    }

    void ensureOpenAndUnconnected() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.state != 0) {
                throw new IllegalStateException("Connect already invoked");
            }
        }
    }

    public DatagramChannel connect(SocketAddress sa) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpenAndUnconnected();
                    InetSocketAddress isa = Net.checkAddress(sa);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                    }
                    if (Net.connect(this.family, this.fd, isa.getAddress(), isa.getPort()) <= 0) {
                        throw new Error();
                    }
                    this.state = 1;
                    this.remoteAddress = isa;
                    this.sender = isa;
                    this.cachedSenderInetAddress = isa.getAddress();
                    this.cachedSenderPort = isa.getPort();
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
        }
        return this;
    }

    public DatagramChannel disconnect() throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (isConnected() && isOpen()) {
                        InetSocketAddress isa = this.remoteAddress;
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                        }
                        disconnect0(this.fd, this.family == StandardProtocolFamily.INET6 ? true : f140-assertionsDisabled);
                        this.remoteAddress = null;
                        this.state = 0;
                        this.localAddress = Net.localAddress(this.fd);
                        return this;
                    }
                    return this;
                }
            }
        }
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != 2) {
                nd.preClose(this.fd);
            }
            ResourceManager.afterUdpClose();
            long th = this.readerThread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            th = this.writerThread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    public void kill() throws IOException {
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == 2) {
            } else if (this.state == -1) {
                this.state = 2;
            } else {
                if (!f140-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                nd.close(this.fd);
                this.state = 2;
            }
        }
    }

    protected void finalize() throws IOException {
        if (this.fd != null) {
            close();
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        boolean z = true;
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((ops & 32) != 0) {
            return f140-assertionsDisabled;
        }
        if ((ops & 24) != 0) {
            newOps = intOps;
            sk.nioReadyOps(intOps);
            if (((~oldOps) & intOps) == 0) {
                z = f140-assertionsDisabled;
            }
            return z;
        }
        if (!((ops & 1) == 0 || (intOps & 1) == 0)) {
            newOps = initialOps | 1;
        }
        if (!((ops & 4) == 0 || (intOps & 4) == 0)) {
            newOps |= 4;
        }
        sk.nioReadyOps(newOps);
        if (((~oldOps) & newOps) == 0) {
            z = f140-assertionsDisabled;
        }
        return z;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = 0;
        if ((ops & 1) != 0) {
            newOps = 1;
        }
        if ((ops & 4) != 0) {
            newOps |= 4;
        }
        if ((ops & 8) != 0) {
            newOps |= 1;
        }
        sk.selector.putEventOps(sk, newOps);
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }
}
