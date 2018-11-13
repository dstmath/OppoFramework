package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import sun.misc.IoTrace;
import sun.net.NetHooks;

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
class SocketChannelImpl extends SocketChannel implements SelChImpl {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f147-assertionsDisabled = false;
    private static final int ST_CONNECTED = 2;
    private static final int ST_KILLED = 4;
    private static final int ST_KILLPENDING = 3;
    private static final int ST_PENDING = 1;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
    private final FileDescriptor fd;
    private final int fdVal;
    private boolean isInputOpen;
    private boolean isOutputOpen;
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object readLock;
    private volatile long readerThread;
    private boolean readyToConnect;
    private InetSocketAddress remoteAddress;
    private Socket socket;
    private int state;
    private final Object stateLock;
    private final Object writeLock;
    private volatile long writerThread;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<init>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.defaultOptions():java.util.Set<java.net.SocketOption<?>>");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketChannelImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketChannelImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.<clinit>():void");
    }

    private static native int checkConnect(FileDescriptor fileDescriptor, boolean z, boolean z2) throws IOException;

    private static native int sendOutOfBandData(FileDescriptor fileDescriptor, byte b) throws IOException;

    SocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = f147-assertionsDisabled;
        this.fd = Net.socket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = 0;
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = f147-assertionsDisabled;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 0;
        if (bound) {
            this.localAddress = Net.localAddress(fd);
        }
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd, InetSocketAddress remote) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = f147-assertionsDisabled;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 2;
        this.localAddress = Net.localAddress(fd);
        this.remoteAddress = remote;
    }

    public Socket socket() {
        Socket socket;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = SocketAdaptor.create(this);
            }
            socket = this.socket;
        }
        return socket;
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

    /* JADX WARNING: Missing block: B:24:0x0057, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.IP_TOS) {
                    if (!Net.isIPv6Available()) {
                        Net.setSocketOption(this.fd, StandardProtocolFamily.INET, name, value);
                    }
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    this.isReuseAddress = ((Boolean) value).booleanValue();
                    return this;
                } else {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    return this;
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    /* JADX WARNING: Missing block: B:32:0x0067, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                T valueOf;
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    valueOf = Boolean.valueOf(this.isReuseAddress);
                    return valueOf;
                } else if (name != StandardSocketOptions.IP_TOS) {
                    valueOf = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return valueOf;
                } else if (Net.isIPv6Available()) {
                    valueOf = Integer.valueOf(0);
                } else {
                    valueOf = Net.getSocketOption(this.fd, StandardProtocolFamily.INET, name);
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    private boolean ensureReadOpen() throws ClosedChannelException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!isConnected()) {
                throw new NotYetConnectedException();
            } else if (this.isInputOpen) {
                return true;
            } else {
                return f147-assertionsDisabled;
            }
        }
    }

    private void ensureWriteOpen() throws ClosedChannelException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!this.isOutputOpen) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
            } else {
                throw new NotYetConnectedException();
            }
        }
    }

    private void readerCleanup() throws IOException {
        synchronized (this.stateLock) {
            this.readerThread = 0;
            if (this.state == 3) {
                kill();
            }
        }
    }

    private void writerCleanup() throws IOException {
        synchronized (this.stateLock) {
            this.writerThread = 0;
            if (this.state == 3) {
                kill();
            }
        }
    }

    /* JADX WARNING: Missing block: B:26:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:27:0x0037, code:
            if (isBlocking() == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:28:0x0039, code:
            sun.misc.IoTrace.socketReadEnd(r0, r13.remoteAddress.getAddress(), r13.remoteAddress.getPort(), 0, (long) null);
     */
    /* JADX WARNING: Missing block: B:29:0x004a, code:
            end(-assertionsDisabled);
            r1 = r13.stateLock;
     */
    /* JADX WARNING: Missing block: B:30:0x0050, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:33:0x0053, code:
            if (r13.isInputOpen == false) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:37:0x0058, code:
            if (-assertionsDisabled != false) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:39:0x005e, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:41:0x0065, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:45:0x006b, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:47:0x0070, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:52:0x0078, code:
            r6 = sun.nio.ch.IOUtil.read(r13.fd, r14, -1, nd);
     */
    /* JADX WARNING: Missing block: B:53:0x0083, code:
            if (r6 != -3) goto L_0x008b;
     */
    /* JADX WARNING: Missing block: B:55:0x0089, code:
            if (isOpen() != false) goto L_0x0078;
     */
    /* JADX WARNING: Missing block: B:56:0x008b, code:
            r8 = sun.nio.ch.IOStatus.normalize(r6);
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:59:0x0096, code:
            if (isBlocking() == false) goto L_0x00ac;
     */
    /* JADX WARNING: Missing block: B:60:0x0098, code:
            r1 = r13.remoteAddress.getAddress();
            r2 = r13.remoteAddress.getPort();
     */
    /* JADX WARNING: Missing block: B:61:0x00a4, code:
            if (r6 <= 0) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:62:0x00a7, code:
            sun.misc.IoTrace.socketReadEnd(r0, r1, r2, 0, (long) r3);
     */
    /* JADX WARNING: Missing block: B:63:0x00ac, code:
            if (r6 > 0) goto L_0x00b0;
     */
    /* JADX WARNING: Missing block: B:64:0x00ae, code:
            if (r6 != -2) goto L_0x0113;
     */
    /* JADX WARNING: Missing block: B:65:0x00b0, code:
            end(r9);
            r1 = r13.stateLock;
     */
    /* JADX WARNING: Missing block: B:66:0x00b5, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:67:0x00b6, code:
            if (r6 > 0) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:70:0x00ba, code:
            if (r13.isInputOpen == false) goto L_0x0115;
     */
    /* JADX WARNING: Missing block: B:72:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:74:0x00bf, code:
            if (-assertionsDisabled != false) goto L_0x011b;
     */
    /* JADX WARNING: Missing block: B:76:0x00c5, code:
            if (sun.nio.ch.IOStatus.check(r6) != false) goto L_0x011b;
     */
    /* JADX WARNING: Missing block: B:78:0x00cc, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:104:0x0111, code:
            r3 = 0;
     */
    /* JADX WARNING: Missing block: B:105:0x0113, code:
            r9 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:106:0x0115, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:108:0x0117, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:114:0x011c, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(ByteBuffer buf) throws IOException {
        int i;
        boolean z = true;
        boolean z2 = f147-assertionsDisabled;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                Object traceContext = null;
                if (isBlocking()) {
                    traceContext = IoTrace.socketReadBegin();
                }
                int n = 0;
                try {
                    begin();
                    i = this.stateLock;
                    synchronized (i) {
                        if (isOpen()) {
                            this.readerThread = NativeThread.current();
                        }
                    }
                } finally {
                    InetAddress inetAddress = 
/*
Method generation error in method: sun.nio.ch.SocketChannelImpl.read(java.nio.ByteBuffer):int, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r1_25 'inetAddress' java.net.InetAddress) = (r1_17 'inetAddress' java.net.InetAddress), (r6_4 'n' int) in method: sun.nio.ch.SocketChannelImpl.read(java.nio.ByteBuffer):int, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:298)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeSynchronizedRegion(RegionGen.java:228)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:65)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 37 more

*/

    /* JADX WARNING: Missing block: B:28:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:29:0x003c, code:
            if (isBlocking() == false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:30:0x003e, code:
            sun.misc.IoTrace.socketReadEnd(r0, r12.remoteAddress.getAddress(), r12.remoteAddress.getPort(), 0, 0);
     */
    /* JADX WARNING: Missing block: B:31:0x0050, code:
            end(-assertionsDisabled);
            r1 = r12.stateLock;
     */
    /* JADX WARNING: Missing block: B:32:0x0056, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:35:0x0059, code:
            if (r12.isInputOpen == false) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:39:0x005e, code:
            if (-assertionsDisabled != false) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:41:0x0064, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:43:0x006b, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:48:0x0073, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:51:0x007a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:56:0x0082, code:
            r6 = sun.nio.ch.IOUtil.read(r12.fd, r13, r14, r15, nd);
     */
    /* JADX WARNING: Missing block: B:57:0x008e, code:
            if (r6 != -3) goto L_0x0096;
     */
    /* JADX WARNING: Missing block: B:59:0x0094, code:
            if (isOpen() != false) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:60:0x0096, code:
            r10 = sun.nio.ch.IOStatus.normalize(r6);
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:63:0x00a1, code:
            if (isBlocking() == false) goto L_0x00ba;
     */
    /* JADX WARNING: Missing block: B:64:0x00a3, code:
            r1 = r12.remoteAddress.getAddress();
            r2 = r12.remoteAddress.getPort();
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:65:0x00b3, code:
            if (r6 <= 0) goto L_0x013b;
     */
    /* JADX WARNING: Missing block: B:66:0x00b6, code:
            sun.misc.IoTrace.socketReadEnd(r0, r1, r2, 0, r4);
     */
    /* JADX WARNING: Missing block: B:68:0x00be, code:
            if (r6 > 0) goto L_0x00c6;
     */
    /* JADX WARNING: Missing block: B:70:0x00c4, code:
            if (r6 != -2) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:71:0x00c6, code:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:72:0x00c7, code:
            end(r1);
            r1 = r12.stateLock;
     */
    /* JADX WARNING: Missing block: B:73:0x00cc, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:75:0x00d1, code:
            if (r6 > 0) goto L_0x00d7;
     */
    /* JADX WARNING: Missing block: B:78:0x00d5, code:
            if (r12.isInputOpen == false) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:80:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:82:0x00da, code:
            if (-assertionsDisabled != false) goto L_0x0149;
     */
    /* JADX WARNING: Missing block: B:84:0x00e0, code:
            if (sun.nio.ch.IOStatus.check(r6) != false) goto L_0x0149;
     */
    /* JADX WARNING: Missing block: B:86:0x00e7, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:115:0x013b, code:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:116:0x013f, code:
            r1 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:117:0x0141, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:120:0x0145, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:126:0x014a, code:
            return r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                long n = 0;
                Object traceContext = null;
                if (isBlocking()) {
                    traceContext = IoTrace.socketReadBegin();
                }
                try {
                    begin();
                    int i = this.stateLock;
                    synchronized (i) {
                        if (isOpen()) {
                            long current = NativeThread.current();
                            this.readerThread = current;
                        }
                    }
                } finally {
                    InetAddress inetAddress = 
/*
Method generation error in method: sun.nio.ch.SocketChannelImpl.read(java.nio.ByteBuffer[], int, int):long, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r1_31 'inetAddress' java.net.InetAddress) = (r1_18 'inetAddress' java.net.InetAddress), (r6_4 'n' long) in method: sun.nio.ch.SocketChannelImpl.read(java.nio.ByteBuffer[], int, int):long, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:298)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeSynchronizedRegion(RegionGen.java:228)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:65)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 37 more

*/

    /* JADX WARNING: Missing block: B:17:?, code:
            writerCleanup();
            sun.misc.IoTrace.socketWriteEnd(r1, r13.remoteAddress.getAddress(), r13.remoteAddress.getPort(), (long) null);
            end(-assertionsDisabled);
            r4 = r13.stateLock;
     */
    /* JADX WARNING: Missing block: B:18:0x003c, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:21:0x003f, code:
            if (r13.isOutputOpen == false) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:25:0x0044, code:
            if (-assertionsDisabled != false) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:27:0x004a, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:29:0x0051, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:35:0x005a, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:41:0x005f, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:46:0x0067, code:
            r0 = sun.nio.ch.IOUtil.write(r13.fd, r14, -1, nd);
     */
    /* JADX WARNING: Missing block: B:47:0x0072, code:
            if (r0 != -3) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:49:0x0078, code:
            if (isOpen() != false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:50:0x007a, code:
            r5 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            writerCleanup();
            r7 = r13.remoteAddress.getAddress();
            r8 = r13.remoteAddress.getPort();
     */
    /* JADX WARNING: Missing block: B:53:0x008d, code:
            if (r0 <= 0) goto L_0x00f1;
     */
    /* JADX WARNING: Missing block: B:54:0x008f, code:
            r2 = r0;
     */
    /* JADX WARNING: Missing block: B:55:0x0090, code:
            sun.misc.IoTrace.socketWriteEnd(r1, r7, r8, (long) r2);
     */
    /* JADX WARNING: Missing block: B:56:0x0094, code:
            if (r0 > 0) goto L_0x0098;
     */
    /* JADX WARNING: Missing block: B:57:0x0096, code:
            if (r0 != -2) goto L_0x00f3;
     */
    /* JADX WARNING: Missing block: B:58:0x0098, code:
            end(r4);
            r3 = r13.stateLock;
     */
    /* JADX WARNING: Missing block: B:59:0x009d, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:60:0x009e, code:
            if (r0 > 0) goto L_0x00a4;
     */
    /* JADX WARNING: Missing block: B:63:0x00a2, code:
            if (r13.isOutputOpen == false) goto L_0x00f5;
     */
    /* JADX WARNING: Missing block: B:65:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:67:0x00a7, code:
            if (-assertionsDisabled != false) goto L_0x00fe;
     */
    /* JADX WARNING: Missing block: B:69:0x00ad, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00fe;
     */
    /* JADX WARNING: Missing block: B:71:0x00b4, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:99:0x00f1, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:100:0x00f3, code:
            r4 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:103:0x00fa, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:109:0x00ff, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int write(ByteBuffer buf) throws IOException {
        boolean z = true;
        boolean z2 = f147-assertionsDisabled;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            Object traceContext = IoTrace.socketWriteBegin();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                int i;
                writerCleanup();
                InetAddress address = this.remoteAddress.getAddress();
                int port = this.remoteAddress.getPort();
                if (null > null) {
                    i = 0;
                } else {
                    i = 0;
                }
                IoTrace.socketWriteEnd(traceContext, address, port, (long) i);
                if (null > null || 0 == -2) {
                    z2 = true;
                }
                end(z2);
                synchronized (this.stateLock) {
                    if (null <= null) {
                        if (!this.isOutputOpen) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!f147-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:?, code:
            writerCleanup();
            sun.misc.IoTrace.socketWriteEnd(r2, r12.remoteAddress.getAddress(), r12.remoteAddress.getPort(), 0);
            end(-assertionsDisabled);
            r4 = r12.stateLock;
     */
    /* JADX WARNING: Missing block: B:20:0x0041, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:23:0x0044, code:
            if (r12.isOutputOpen == false) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:27:0x0049, code:
            if (-assertionsDisabled != false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:29:0x004f, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:31:0x0056, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:37:0x005f, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:44:0x0066, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:49:0x006e, code:
            r0 = sun.nio.ch.IOUtil.write(r12.fd, r13, r14, r15, nd);
     */
    /* JADX WARNING: Missing block: B:50:0x007a, code:
            if (r0 != -3) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:52:0x0080, code:
            if (isOpen() != false) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:53:0x0082, code:
            r8 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            writerCleanup();
            r3 = r12.remoteAddress.getAddress();
            r7 = r12.remoteAddress.getPort();
     */
    /* JADX WARNING: Missing block: B:56:0x0099, code:
            if (r0 <= 0) goto L_0x0118;
     */
    /* JADX WARNING: Missing block: B:57:0x009b, code:
            r4 = r0;
     */
    /* JADX WARNING: Missing block: B:58:0x009c, code:
            sun.misc.IoTrace.socketWriteEnd(r2, r3, r7, r4);
     */
    /* JADX WARNING: Missing block: B:59:0x00a3, code:
            if (r0 > 0) goto L_0x00ab;
     */
    /* JADX WARNING: Missing block: B:61:0x00a9, code:
            if (r0 != -2) goto L_0x011b;
     */
    /* JADX WARNING: Missing block: B:62:0x00ab, code:
            r3 = true;
     */
    /* JADX WARNING: Missing block: B:63:0x00ac, code:
            end(r3);
            r4 = r12.stateLock;
     */
    /* JADX WARNING: Missing block: B:64:0x00b1, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:66:0x00b6, code:
            if (r0 > 0) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:69:0x00ba, code:
            if (r12.isOutputOpen == false) goto L_0x011d;
     */
    /* JADX WARNING: Missing block: B:71:?, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:73:0x00bf, code:
            if (-assertionsDisabled != false) goto L_0x0126;
     */
    /* JADX WARNING: Missing block: B:75:0x00c5, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x0126;
     */
    /* JADX WARNING: Missing block: B:77:0x00cc, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:107:0x0118, code:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:108:0x011b, code:
            r3 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:111:0x0122, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:117:0x0127, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            Object traceContext = IoTrace.socketWriteBegin();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), 0 > 0 ? 0 : 0);
                boolean z = (0 > 0 || 0 == -2) ? true : f147-assertionsDisabled;
                end(z);
                synchronized (this.stateLock) {
                    if (0 <= 0) {
                        if (!this.isOutputOpen) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!f147-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:?, code:
            writerCleanup();
            end(-assertionsDisabled);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:15:0x0020, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:18:0x0023, code:
            if (r9.isOutputOpen == false) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:22:0x0028, code:
            if (-assertionsDisabled != false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:24:0x002e, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:26:0x0035, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:32:0x003e, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:38:0x0043, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:43:0x004b, code:
            r0 = sendOutOfBandData(r9.fd, r10);
     */
    /* JADX WARNING: Missing block: B:44:0x0052, code:
            if (r0 != -3) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:46:0x0058, code:
            if (isOpen() != false) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:47:0x005a, code:
            r1 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            writerCleanup();
     */
    /* JADX WARNING: Missing block: B:50:0x0061, code:
            if (r0 > 0) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:51:0x0063, code:
            if (r0 != -2) goto L_0x00aa;
     */
    /* JADX WARNING: Missing block: B:52:0x0065, code:
            end(r2);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:53:0x006a, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:54:0x006b, code:
            if (r0 > 0) goto L_0x0071;
     */
    /* JADX WARNING: Missing block: B:57:0x006f, code:
            if (r9.isOutputOpen == false) goto L_0x00ac;
     */
    /* JADX WARNING: Missing block: B:59:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:61:0x0074, code:
            if (-assertionsDisabled != false) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:63:0x007a, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:65:0x0081, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:89:0x00aa, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:92:0x00b1, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:98:0x00b6, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int sendOutOfBandData(byte b) throws IOException {
        boolean z = true;
        synchronized (this.writeLock) {
            ensureWriteOpen();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                if (null <= null && 0 != -2) {
                    z = f147-assertionsDisabled;
                }
                end(z);
                synchronized (this.stateLock) {
                    if (null <= null) {
                        if (!this.isOutputOpen) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!f147-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public InetSocketAddress localAddress() {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.localAddress;
        }
        return inetSocketAddress;
    }

    public SocketAddress remoteAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.remoteAddress;
        }
        return socketAddress;
    }

    public SocketChannel bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (!isOpen()) {
                        throw new ClosedChannelException();
                    } else if (this.state == 1) {
                        throw new ConnectionPendingException();
                    } else if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    } else {
                        InetSocketAddress isa = local == null ? new InetSocketAddress(0) : Net.checkAddress(local);
                        NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                        Net.bind(this.fd, isa.getAddress(), isa.getPort());
                        this.localAddress = Net.localAddress(this.fd);
                    }
                }
            }
        }
        return this;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.state == 2 ? true : f147-assertionsDisabled;
        }
        return z;
    }

    public boolean isConnectionPending() {
        boolean z = true;
        synchronized (this.stateLock) {
            if (this.state != 1) {
                z = f147-assertionsDisabled;
            }
        }
        return z;
    }

    void ensureOpenAndUnconnected() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.state == 2) {
                throw new AlreadyConnectedException();
            } else if (this.state == 1) {
                throw new ConnectionPendingException();
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:?, code:
            readerCleanup();
            end(-assertionsDisabled);
     */
    /* JADX WARNING: Missing block: B:22:0x003f, code:
            if (-assertionsDisabled != false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:24:0x0045, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:26:0x004c, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:27:0x004d, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            close();
     */
    /* JADX WARNING: Missing block: B:30:0x0051, code:
            throw r5;
     */
    /* JADX WARNING: Missing block: B:48:0x005f, code:
            return -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:56:0x0078, code:
            r0 = r1.getAddress();
     */
    /* JADX WARNING: Missing block: B:57:0x0080, code:
            if (r0.isAnyLocalAddress() == false) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:58:0x0082, code:
            r0 = java.net.InetAddress.getLocalHost();
     */
    /* JADX WARNING: Missing block: B:59:0x0086, code:
            r3 = sun.nio.ch.Net.connect(r14.fd, r0, r1.getPort());
     */
    /* JADX WARNING: Missing block: B:60:0x0091, code:
            if (r3 != -3) goto L_0x0099;
     */
    /* JADX WARNING: Missing block: B:62:0x0097, code:
            if (isOpen() != false) goto L_0x0078;
     */
    /* JADX WARNING: Missing block: B:64:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:65:0x009c, code:
            if (r3 > 0) goto L_0x00a1;
     */
    /* JADX WARNING: Missing block: B:67:0x009f, code:
            if (r3 != -2) goto L_0x00d5;
     */
    /* JADX WARNING: Missing block: B:68:0x00a1, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:69:0x00a2, code:
            end(r6);
     */
    /* JADX WARNING: Missing block: B:70:0x00a7, code:
            if (-assertionsDisabled != false) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:72:0x00ad, code:
            if (sun.nio.ch.IOStatus.check(r3) != false) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:74:0x00b4, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:92:0x00d5, code:
            r6 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:96:?, code:
            r7 = r14.stateLock;
     */
    /* JADX WARNING: Missing block: B:97:0x00dc, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:99:?, code:
            r14.remoteAddress = r1;
     */
    /* JADX WARNING: Missing block: B:100:0x00df, code:
            if (r3 <= 0) goto L_0x00f8;
     */
    /* JADX WARNING: Missing block: B:101:0x00e1, code:
            r14.state = 2;
     */
    /* JADX WARNING: Missing block: B:102:0x00e8, code:
            if (isOpen() == false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:103:0x00ea, code:
            r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Missing block: B:105:?, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:112:0x00f7, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:115:0x00fc, code:
            if (isBlocking() != false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:116:0x00fe, code:
            r14.state = 1;
     */
    /* JADX WARNING: Missing block: B:117:0x0105, code:
            if (isOpen() == false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:118:0x0107, code:
            r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Missing block: B:120:?, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:127:0x0114, code:
            return -assertionsDisabled;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean connect(SocketAddress sa) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                ensureOpenAndUnconnected();
                InetSocketAddress isa = Net.checkAddress(sa);
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                }
                synchronized (blockingLock()) {
                    try {
                        begin();
                        synchronized (this.stateLock) {
                            if (isOpen()) {
                                if (this.localAddress == null) {
                                    NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
                                }
                                this.readerThread = NativeThread.current();
                            }
                        }
                    } catch (Throwable th) {
                        readerCleanup();
                        boolean z = (null > null || 0 == -2) ? true : f147-assertionsDisabled;
                        end(z);
                        if (!f147-assertionsDisabled && !IOStatus.check(0)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.nio.ch.SocketChannelImpl.finishConnect():boolean, dom blocks: [B:55:0x004a, B:110:0x00b4]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public boolean finishConnect() throws java.io.IOException {
        /*
        r14 = this;
        r13 = 3;
        r12 = -2;
        r3 = 1;
        r4 = 0;
        r5 = r14.readLock;
        monitor-enter(r5);
        r6 = r14.writeLock;	 Catch:{ all -> 0x001f }
        monitor-enter(r6);	 Catch:{ all -> 0x001f }
        r7 = r14.stateLock;	 Catch:{ all -> 0x001c }
        monitor-enter(r7);	 Catch:{ all -> 0x001c }
        r2 = r14.isOpen();	 Catch:{ all -> 0x0019 }
        if (r2 != 0) goto L_0x0022;	 Catch:{ all -> 0x0019 }
    L_0x0013:
        r2 = new java.nio.channels.ClosedChannelException;	 Catch:{ all -> 0x0019 }
        r2.<init>();	 Catch:{ all -> 0x0019 }
        throw r2;	 Catch:{ all -> 0x0019 }
    L_0x0019:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        throw r2;	 Catch:{ all -> 0x001c }
    L_0x001c:
        r2 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        throw r2;	 Catch:{ all -> 0x001f }
    L_0x001f:
        r2 = move-exception;
        monitor-exit(r5);
        throw r2;
    L_0x0022:
        r2 = r14.state;	 Catch:{ all -> 0x0019 }
        r8 = 2;
        if (r2 != r8) goto L_0x002b;
    L_0x0027:
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r3;
    L_0x002b:
        r2 = r14.state;	 Catch:{ all -> 0x0019 }
        if (r2 == r3) goto L_0x0035;	 Catch:{ all -> 0x0019 }
    L_0x002f:
        r2 = new java.nio.channels.NoConnectionPendingException;	 Catch:{ all -> 0x0019 }
        r2.<init>();	 Catch:{ all -> 0x0019 }
        throw r2;	 Catch:{ all -> 0x0019 }
    L_0x0035:
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        r0 = 0;
        r14.begin();	 Catch:{  }
        r7 = r14.blockingLock();	 Catch:{  }
        monitor-enter(r7);	 Catch:{  }
        r8 = r14.stateLock;	 Catch:{ all -> 0x00d0 }
        monitor-enter(r8);	 Catch:{ all -> 0x00d0 }
        r2 = r14.isOpen();	 Catch:{  }
        if (r2 != 0) goto L_0x007e;
    L_0x0048:
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        monitor-exit(r7);	 Catch:{  }
        r9 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r9);	 Catch:{ IOException -> 0x0071 }
        r10 = 0;
        r14.readerThread = r10;	 Catch:{ all -> 0x0076 }
        r2 = r14.state;	 Catch:{ all -> 0x0076 }
        if (r2 != r13) goto L_0x0059;	 Catch:{ all -> 0x0076 }
    L_0x0055:
        r14.kill();	 Catch:{ all -> 0x0076 }
        r0 = 0;
    L_0x0059:
        monitor-exit(r9);	 Catch:{ all -> 0x00cd }
        if (r4 < 0) goto L_0x005e;
    L_0x005c:
        if (r12 != 0) goto L_0x0079;
    L_0x005e:
        r14.end(r3);	 Catch:{ IOException -> 0x0071 }
        r2 = f147-assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x007b;	 Catch:{ IOException -> 0x0071 }
    L_0x0065:
        r2 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x007b;	 Catch:{ IOException -> 0x0071 }
    L_0x006b:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0071:
        r1 = move-exception;
        r14.close();	 Catch:{ all -> 0x001c }
        throw r1;	 Catch:{ all -> 0x001c }
    L_0x0076:
        r2 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x00cd }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0079:
        r3 = r4;
        goto L_0x005e;
    L_0x007b:
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r4;
    L_0x007e:
        r10 = sun.nio.ch.NativeThread.current();	 Catch:{  }
        r14.readerThread = r10;	 Catch:{  }
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        r2 = dalvik.system.BlockGuard.getThreadPolicy();	 Catch:{ all -> 0x00d0 }
        r2.onNetwork();	 Catch:{ all -> 0x00d0 }
        r2 = r14.isBlocking();	 Catch:{ all -> 0x00d0 }
        if (r2 != 0) goto L_0x00fb;	 Catch:{ all -> 0x00d0 }
    L_0x0092:
        r2 = r14.fd;	 Catch:{ all -> 0x00d0 }
        r8 = r14.readyToConnect;	 Catch:{ all -> 0x00d0 }
        r9 = 0;	 Catch:{ all -> 0x00d0 }
        r0 = checkConnect(r2, r9, r8);	 Catch:{ all -> 0x00d0 }
        r2 = -3;	 Catch:{ all -> 0x00d0 }
        if (r0 != r2) goto L_0x00a4;	 Catch:{ all -> 0x00d0 }
    L_0x009e:
        r2 = r14.isOpen();	 Catch:{ all -> 0x00d0 }
        if (r2 != 0) goto L_0x0092;
    L_0x00a4:
        monitor-exit(r7);	 Catch:{  }
        r7 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r7);	 Catch:{ IOException -> 0x0071 }
        r8 = 0;
        r14.readerThread = r8;	 Catch:{ all -> 0x0110 }
        r2 = r14.state;	 Catch:{ all -> 0x0110 }
        if (r2 != r13) goto L_0x00b4;	 Catch:{ all -> 0x0110 }
    L_0x00b0:
        r14.kill();	 Catch:{ all -> 0x0110 }
        r0 = 0;
    L_0x00b4:
        monitor-exit(r7);	 Catch:{ all -> 0x00d3 }
        if (r0 > 0) goto L_0x00b9;
    L_0x00b7:
        if (r0 != r12) goto L_0x0113;
    L_0x00b9:
        r2 = r3;
    L_0x00ba:
        r14.end(r2);	 Catch:{ IOException -> 0x0071 }
        r2 = f147-assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x011b;	 Catch:{ IOException -> 0x0071 }
    L_0x00c1:
        r2 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x011b;	 Catch:{ IOException -> 0x0071 }
    L_0x00c7:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x00cd:
        r2 = move-exception;
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        throw r2;	 Catch:{ all -> 0x00d0 }
    L_0x00d0:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{  }
        throw r2;	 Catch:{  }
    L_0x00d3:
        r2 = move-exception;
        r7 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r7);	 Catch:{ IOException -> 0x0071 }
        r8 = 0;
        r14.readerThread = r8;	 Catch:{ all -> 0x0115 }
        r8 = r14.state;	 Catch:{ all -> 0x0115 }
        if (r8 != r13) goto L_0x00e3;	 Catch:{ all -> 0x0115 }
    L_0x00df:
        r14.kill();	 Catch:{ all -> 0x0115 }
        r0 = 0;
    L_0x00e3:
        monitor-exit(r7);	 Catch:{ IOException -> 0x0071 }
        if (r0 > 0) goto L_0x00e8;	 Catch:{ IOException -> 0x0071 }
    L_0x00e6:
        if (r0 != r12) goto L_0x0118;	 Catch:{ IOException -> 0x0071 }
    L_0x00e8:
        r14.end(r3);	 Catch:{ IOException -> 0x0071 }
        r3 = f147-assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r3 != 0) goto L_0x011a;	 Catch:{ IOException -> 0x0071 }
    L_0x00ef:
        r3 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r3 != 0) goto L_0x011a;	 Catch:{ IOException -> 0x0071 }
    L_0x00f5:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x00fb:
        r2 = r14.fd;	 Catch:{ all -> 0x00d0 }
        r8 = r14.readyToConnect;	 Catch:{ all -> 0x00d0 }
        r9 = 1;	 Catch:{ all -> 0x00d0 }
        r0 = checkConnect(r2, r9, r8);	 Catch:{ all -> 0x00d0 }
        if (r0 == 0) goto L_0x00fb;	 Catch:{ all -> 0x00d0 }
    L_0x0106:
        r2 = -3;	 Catch:{ all -> 0x00d0 }
        if (r0 != r2) goto L_0x00a4;	 Catch:{ all -> 0x00d0 }
    L_0x0109:
        r2 = r14.isOpen();	 Catch:{ all -> 0x00d0 }
        if (r2 == 0) goto L_0x00a4;
    L_0x010f:
        goto L_0x00fb;
    L_0x0110:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x00d3 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0113:
        r2 = r4;	 Catch:{ IOException -> 0x0071 }
        goto L_0x00ba;	 Catch:{ IOException -> 0x0071 }
    L_0x0115:
        r2 = move-exception;	 Catch:{ IOException -> 0x0071 }
        monitor-exit(r7);	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0118:
        r3 = r4;	 Catch:{ IOException -> 0x0071 }
        goto L_0x00e8;	 Catch:{ IOException -> 0x0071 }
    L_0x011a:
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x011b:
        if (r0 <= 0) goto L_0x0138;
    L_0x011d:
        r4 = r14.stateLock;	 Catch:{ all -> 0x001c }
        monitor-enter(r4);	 Catch:{ all -> 0x001c }
        r2 = 2;
        r14.state = r2;	 Catch:{ all -> 0x0135 }
        r2 = r14.isOpen();	 Catch:{ all -> 0x0135 }
        if (r2 == 0) goto L_0x0131;	 Catch:{ all -> 0x0135 }
    L_0x0129:
        r2 = r14.fd;	 Catch:{ all -> 0x0135 }
        r2 = sun.nio.ch.Net.localAddress(r2);	 Catch:{ all -> 0x0135 }
        r14.localAddress = r2;	 Catch:{ all -> 0x0135 }
    L_0x0131:
        monitor-exit(r4);	 Catch:{ all -> 0x001c }
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r3;
    L_0x0135:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x001c }
        throw r2;	 Catch:{ all -> 0x001c }
    L_0x0138:
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.finishConnect():boolean");
    }

    public SocketChannel shutdownInput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
                if (this.isInputOpen) {
                    Net.shutdown(this.fd, 0);
                    if (this.readerThread != 0) {
                        NativeThread.signal(this.readerThread);
                    }
                    this.isInputOpen = f147-assertionsDisabled;
                }
            } else {
                throw new NotYetConnectedException();
            }
        }
        return this;
    }

    public SocketChannel shutdownOutput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
                if (this.isOutputOpen) {
                    Net.shutdown(this.fd, 1);
                    if (this.writerThread != 0) {
                        NativeThread.signal(this.writerThread);
                    }
                    this.isOutputOpen = f147-assertionsDisabled;
                }
            } else {
                throw new NotYetConnectedException();
            }
        }
        return this;
    }

    public boolean isInputOpen() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.isInputOpen;
        }
        return z;
    }

    public boolean isOutputOpen() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.isOutputOpen;
        }
        return z;
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            this.isInputOpen = f147-assertionsDisabled;
            this.isOutputOpen = f147-assertionsDisabled;
            if (this.state != 4) {
                nd.preClose(this.fd);
            }
            if (this.readerThread != 0) {
                NativeThread.signal(this.readerThread);
            }
            if (this.writerThread != 0) {
                NativeThread.signal(this.writerThread);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    /* JADX WARNING: Missing block: B:34:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void kill() throws IOException {
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == 4) {
            } else if (this.state == -1) {
                this.state = 4;
            } else {
                if (!f147-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (this.readerThread == 0 && this.writerThread == 0) {
                    nd.close(this.fd);
                    this.state = 4;
                } else {
                    this.state = 3;
                }
            }
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        boolean z = true;
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((ops & 32) != 0) {
            return f147-assertionsDisabled;
        }
        if ((ops & 24) != 0) {
            newOps = intOps;
            sk.nioReadyOps(intOps);
            this.readyToConnect = true;
            if (((~oldOps) & intOps) == 0) {
                z = f147-assertionsDisabled;
            }
            return z;
        }
        if (!((ops & 1) == 0 || (intOps & 1) == 0 || this.state != 2)) {
            newOps = initialOps | 1;
        }
        if (!((ops & 4) == 0 || (intOps & 8) == 0 || (this.state != 0 && this.state != 1))) {
            newOps |= 8;
            this.readyToConnect = true;
        }
        if (!((ops & 4) == 0 || (intOps & 4) == 0 || this.state != 2)) {
            newOps |= 4;
        }
        sk.nioReadyOps(newOps);
        if (((~oldOps) & newOps) == 0) {
            z = f147-assertionsDisabled;
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
            newOps |= 4;
        }
        sk.selector.putEventOps(sk, newOps);
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSuperclass().getName());
        sb.append('[');
        if (isOpen()) {
            synchronized (this.stateLock) {
                switch (this.state) {
                    case 0:
                        sb.append("unconnected");
                        break;
                    case 1:
                        sb.append("connection-pending");
                        break;
                    case 2:
                        sb.append("connected");
                        if (!this.isInputOpen) {
                            sb.append(" ishut");
                        }
                        if (!this.isOutputOpen) {
                            sb.append(" oshut");
                            break;
                        }
                        break;
                }
                InetSocketAddress addr = localAddress();
                if (addr != null) {
                    sb.append(" local=");
                    sb.append(Net.getRevealedLocalAddressAsString(addr));
                }
                if (remoteAddress() != null) {
                    sb.append(" remote=");
                    sb.append(remoteAddress().toString());
                }
            }
        } else {
            sb.append("closed");
        }
        sb.append(']');
        return sb.toString();
    }
}
