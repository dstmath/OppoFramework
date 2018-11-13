package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.misc.IoTrace;

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
public class SocketAdaptor extends Socket {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f148-assertionsDisabled = false;
    private final SocketChannelImpl sc;
    private InputStream socketInputStream;
    private volatile int timeout;

    private class SocketInputStream extends ChannelInputStream {
        /* synthetic */ SocketInputStream(SocketAdaptor this$0, SocketInputStream socketInputStream) {
            this();
        }

        private SocketInputStream() {
            super(SocketAdaptor.this.sc);
        }

        /* JADX WARNING: Missing block: B:26:0x009b, code:
            return r8;
     */
        /* JADX WARNING: Missing block: B:68:0x017e, code:
            return r8;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected int read(ByteBuffer bb) throws IOException {
            synchronized (SocketAdaptor.this.sc.blockingLock()) {
                if (!SocketAdaptor.this.sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                } else if (SocketAdaptor.this.timeout == 0) {
                    int read = SocketAdaptor.this.sc.read(bb);
                    return read;
                } else {
                    SelectionKey sk = null;
                    Selector sel = null;
                    SocketInputStream socketInputStream = this;
                    SocketAdaptor.this.sc.configureBlocking(false);
                    int n = 0;
                    Object traceContext = IoTrace.socketReadBegin();
                    SocketInputStream -get0;
                    try {
                        -get0 = SocketAdaptor.this.sc;
                        socketInputStream = bb;
                        n = -get0.read(socketInputStream);
                        if (n != 0) {
                            IoTrace.socketReadEnd(traceContext, SocketAdaptor.this.getInetAddress(), SocketAdaptor.this.getPort(), SocketAdaptor.this.timeout, (long) (n > 0 ? n : 0));
                            if (SocketAdaptor.this.sc.isOpen()) {
                                SocketAdaptor.this.sc.configureBlocking(true);
                            }
                        } else {
                            sel = Util.getTemporarySelector(SocketAdaptor.this.sc);
                            sk = SocketAdaptor.this.sc.register(sel, 1);
                            long to = (long) SocketAdaptor.this.timeout;
                            while (true) {
                                socketInputStream = this;
                                if (SocketAdaptor.this.sc.isOpen()) {
                                    long st = System.currentTimeMillis();
                                    if (sel.select(to) > 0 && sk.isReadable()) {
                                        socketInputStream = bb;
                                        n = SocketAdaptor.this.sc.read(socketInputStream);
                                        if (n != 0) {
                                            IoTrace.socketReadEnd(traceContext, SocketAdaptor.this.getInetAddress(), SocketAdaptor.this.getPort(), SocketAdaptor.this.timeout, (long) (n > 0 ? n : 0));
                                            if (sk != null) {
                                                sk.cancel();
                                            }
                                            if (SocketAdaptor.this.sc.isOpen()) {
                                                SocketAdaptor.this.sc.configureBlocking(true);
                                            }
                                            if (sel != null) {
                                                Util.releaseTemporarySelector(sel);
                                            }
                                        }
                                    }
                                    sel.selectedKeys().remove(sk);
                                    to -= System.currentTimeMillis() - st;
                                    if (to <= 0) {
                                        throw new SocketTimeoutException();
                                    }
                                }
                                throw new ClosedChannelException();
                            }
                        }
                    } finally {
                        SocketInputStream socketInputStream2 = 
/*
Method generation error in method: sun.nio.ch.SocketAdaptor.SocketInputStream.read(java.nio.ByteBuffer):int, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x00d5: MERGE  (r16_0 'socketInputStream2' sun.nio.ch.SocketAdaptor$SocketInputStream) = (r3_14 '-get0' sun.nio.ch.SocketAdaptor$SocketInputStream), (r18_0 'this' sun.nio.ch.SocketAdaptor$SocketInputStream A:{THIS}) in method: sun.nio.ch.SocketAdaptor.SocketInputStream.read(java.nio.ByteBuffer):int, dex: 
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
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:128)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
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
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:234)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:220)
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
	... 38 more

*/
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketAdaptor.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SocketAdaptor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketAdaptor.<clinit>():void");
    }

    private SocketAdaptor(SocketChannelImpl sc) throws SocketException {
        super(new FileDescriptorHolderSocketImpl(sc.getFD()));
        this.timeout = 0;
        this.socketInputStream = null;
        this.sc = sc;
    }

    public static Socket create(SocketChannelImpl sc) {
        try {
            return new SocketAdaptor(sc);
        } catch (SocketException e) {
            throw new InternalError("Should not reach here");
        }
    }

    public SocketChannel getChannel() {
        return this.sc;
    }

    public void connect(SocketAddress remote) throws IOException {
        connect(remote, 0);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.nio.ch.SocketAdaptor.connect(java.net.SocketAddress, int):void, dom blocks: [B:22:0x0040, B:73:0x00f0]
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
    public void connect(java.net.SocketAddress r17, int r18) throws java.io.IOException {
        /*
        r16 = this;
        if (r17 != 0) goto L_0x000b;
    L_0x0002:
        r12 = new java.lang.IllegalArgumentException;
        r13 = "connect: The address can't be null";
        r12.<init>(r13);
        throw r12;
    L_0x000b:
        if (r18 >= 0) goto L_0x0016;
    L_0x000d:
        r12 = new java.lang.IllegalArgumentException;
        r13 = "connect: timeout can't be negative";
        r12.<init>(r13);
        throw r12;
    L_0x0016:
        r0 = r16;
        r12 = r0.sc;
        r13 = r12.blockingLock();
        monitor-enter(r13);
        r0 = r16;	 Catch:{ all -> 0x002f }
        r12 = r0.sc;	 Catch:{ all -> 0x002f }
        r12 = r12.isBlocking();	 Catch:{ all -> 0x002f }
        if (r12 != 0) goto L_0x0032;	 Catch:{ all -> 0x002f }
    L_0x0029:
        r12 = new java.nio.channels.IllegalBlockingModeException;	 Catch:{ all -> 0x002f }
        r12.<init>();	 Catch:{ all -> 0x002f }
        throw r12;	 Catch:{ all -> 0x002f }
    L_0x002f:
        r12 = move-exception;
        monitor-exit(r13);
        throw r12;
    L_0x0032:
        if (r18 != 0) goto L_0x004b;
    L_0x0034:
        r0 = r16;	 Catch:{ Exception -> 0x003f }
        r12 = r0.sc;	 Catch:{ Exception -> 0x003f }
        r0 = r17;	 Catch:{ Exception -> 0x003f }
        r12.connect(r0);	 Catch:{ Exception -> 0x003f }
    L_0x003d:
        monitor-exit(r13);
        return;
    L_0x003f:
        r2 = move-exception;
        sun.nio.ch.Net.translateException(r2);	 Catch:{ Exception -> 0x0044 }
        goto L_0x003d;
    L_0x0044:
        r11 = move-exception;
        r12 = 1;
        sun.nio.ch.Net.translateException(r11, r12);	 Catch:{ all -> 0x002f }
    L_0x0049:
        monitor-exit(r13);
        return;
    L_0x004b:
        r5 = 0;
        r4 = 0;
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r12 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r14 = 0;	 Catch:{ Exception -> 0x0044 }
        r12.configureBlocking(r14);	 Catch:{ Exception -> 0x0044 }
        r0 = r16;	 Catch:{ all -> 0x009a }
        r12 = r0.sc;	 Catch:{ all -> 0x009a }
        r0 = r17;	 Catch:{ all -> 0x009a }
        r12 = r12.connect(r0);	 Catch:{ all -> 0x009a }
        if (r12 == 0) goto L_0x0075;
    L_0x0061:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r12 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r12 = r12.isOpen();	 Catch:{ Exception -> 0x0044 }
        if (r12 == 0) goto L_0x0073;	 Catch:{ Exception -> 0x0044 }
    L_0x006b:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r12 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r14 = 1;	 Catch:{ Exception -> 0x0044 }
        r12.configureBlocking(r14);	 Catch:{ Exception -> 0x0044 }
    L_0x0073:
        monitor-exit(r13);
        return;
    L_0x0075:
        r0 = r16;	 Catch:{ all -> 0x009a }
        r12 = r0.sc;	 Catch:{ all -> 0x009a }
        r4 = sun.nio.ch.Util.getTemporarySelector(r12);	 Catch:{ all -> 0x009a }
        r0 = r16;	 Catch:{ all -> 0x009a }
        r12 = r0.sc;	 Catch:{ all -> 0x009a }
        r14 = 8;	 Catch:{ all -> 0x009a }
        r5 = r12.register(r4, r14);	 Catch:{ all -> 0x009a }
        r0 = r18;	 Catch:{ all -> 0x009a }
        r8 = (long) r0;	 Catch:{ all -> 0x009a }
    L_0x008a:
        r0 = r16;	 Catch:{ all -> 0x009a }
        r12 = r0.sc;	 Catch:{ all -> 0x009a }
        r12 = r12.isOpen();	 Catch:{ all -> 0x009a }
        if (r12 != 0) goto L_0x00b8;	 Catch:{ all -> 0x009a }
    L_0x0094:
        r12 = new java.nio.channels.ClosedChannelException;	 Catch:{ all -> 0x009a }
        r12.<init>();	 Catch:{ all -> 0x009a }
        throw r12;	 Catch:{ all -> 0x009a }
    L_0x009a:
        r12 = move-exception;
        if (r5 == 0) goto L_0x00a0;
    L_0x009d:
        r5.cancel();	 Catch:{ Exception -> 0x0044 }
    L_0x00a0:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r14 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r14 = r14.isOpen();	 Catch:{ Exception -> 0x0044 }
        if (r14 == 0) goto L_0x00b2;	 Catch:{ Exception -> 0x0044 }
    L_0x00aa:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r14 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r15 = 1;	 Catch:{ Exception -> 0x0044 }
        r14.configureBlocking(r15);	 Catch:{ Exception -> 0x0044 }
    L_0x00b2:
        if (r4 == 0) goto L_0x00b7;	 Catch:{ Exception -> 0x0044 }
    L_0x00b4:
        sun.nio.ch.Util.releaseTemporarySelector(r4);	 Catch:{ Exception -> 0x0044 }
    L_0x00b7:
        throw r12;	 Catch:{ Exception -> 0x0044 }
    L_0x00b8:
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x009a }
        r3 = r4.select(r8);	 Catch:{ all -> 0x009a }
        if (r3 <= 0) goto L_0x00f0;	 Catch:{ all -> 0x009a }
    L_0x00c2:
        r12 = r5.isConnectable();	 Catch:{ all -> 0x009a }
        if (r12 == 0) goto L_0x00f0;	 Catch:{ all -> 0x009a }
    L_0x00c8:
        r0 = r16;	 Catch:{ all -> 0x009a }
        r12 = r0.sc;	 Catch:{ all -> 0x009a }
        r12 = r12.finishConnect();	 Catch:{ all -> 0x009a }
        if (r12 == 0) goto L_0x00f0;
    L_0x00d2:
        if (r5 == 0) goto L_0x00d7;
    L_0x00d4:
        r5.cancel();	 Catch:{ Exception -> 0x0044 }
    L_0x00d7:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r12 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r12 = r12.isOpen();	 Catch:{ Exception -> 0x0044 }
        if (r12 == 0) goto L_0x00e9;	 Catch:{ Exception -> 0x0044 }
    L_0x00e1:
        r0 = r16;	 Catch:{ Exception -> 0x0044 }
        r12 = r0.sc;	 Catch:{ Exception -> 0x0044 }
        r14 = 1;	 Catch:{ Exception -> 0x0044 }
        r12.configureBlocking(r14);	 Catch:{ Exception -> 0x0044 }
    L_0x00e9:
        if (r4 == 0) goto L_0x0049;	 Catch:{ Exception -> 0x0044 }
    L_0x00eb:
        sun.nio.ch.Util.releaseTemporarySelector(r4);	 Catch:{ Exception -> 0x0044 }
        goto L_0x0049;
    L_0x00f0:
        r12 = r4.selectedKeys();	 Catch:{ all -> 0x009a }
        r12.remove(r5);	 Catch:{ all -> 0x009a }
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x009a }
        r14 = r14 - r6;
        r8 = r8 - r14;
        r14 = 0;
        r12 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));
        if (r12 > 0) goto L_0x008a;
    L_0x0103:
        r0 = r16;	 Catch:{ IOException -> 0x0110 }
        r12 = r0.sc;	 Catch:{ IOException -> 0x0110 }
        r12.close();	 Catch:{ IOException -> 0x0110 }
    L_0x010a:
        r12 = new java.net.SocketTimeoutException;	 Catch:{ all -> 0x009a }
        r12.<init>();	 Catch:{ all -> 0x009a }
        throw r12;	 Catch:{ all -> 0x009a }
    L_0x0110:
        r10 = move-exception;
        goto L_0x010a;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketAdaptor.connect(java.net.SocketAddress, int):void");
    }

    public void bind(SocketAddress local) throws IOException {
        try {
            this.sc.bind(local);
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }
        SocketAddress remote = this.sc.remoteAddress();
        if (remote == null) {
            return null;
        }
        return ((InetSocketAddress) remote).getAddress();
    }

    public InetAddress getLocalAddress() {
        if (this.sc.isOpen()) {
            InetSocketAddress local = this.sc.localAddress();
            if (local != null) {
                return Net.getRevealedLocalAddress(local).getAddress();
            }
        }
        return new InetSocketAddress(0).getAddress();
    }

    public int getPort() {
        if (!isConnected()) {
            return 0;
        }
        SocketAddress remote = this.sc.remoteAddress();
        if (remote == null) {
            return 0;
        }
        return ((InetSocketAddress) remote).getPort();
    }

    public int getLocalPort() {
        SocketAddress local = this.sc.localAddress();
        if (local == null) {
            return -1;
        }
        return ((InetSocketAddress) local).getPort();
    }

    public InputStream getInputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        } else if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (this.sc.isInputOpen()) {
            if (this.socketInputStream == null) {
                try {
                    this.socketInputStream = (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            return new SocketInputStream(SocketAdaptor.this, null);
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw ((IOException) e.getException());
                }
            }
            return this.socketInputStream;
        } else {
            throw new SocketException("Socket input is shutdown");
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        } else if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (this.sc.isOutputOpen()) {
            try {
                return (OutputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<OutputStream>() {
                    public OutputStream run() throws IOException {
                        return Channels.newOutputStream(SocketAdaptor.this.sc);
                    }
                });
            } catch (PrivilegedActionException e) {
                throw ((IOException) e.getException());
            }
        } else {
            throw new SocketException("Socket output is shutdown");
        }
    }

    private void setBooleanOption(SocketOption<Boolean> name, boolean value) throws SocketException {
        try {
            this.sc.setOption((SocketOption) name, Boolean.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.sc.setOption((SocketOption) name, Integer.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private boolean getBooleanOption(SocketOption<Boolean> name) throws SocketException {
        try {
            return ((Boolean) this.sc.getOption(name)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    private int getIntOption(SocketOption<Integer> name) throws SocketException {
        try {
            return ((Integer) this.sc.getOption(name)).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.TCP_NODELAY, on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return getBooleanOption(StandardSocketOptions.TCP_NODELAY);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (!on) {
            linger = -1;
        }
        setIntOption(StandardSocketOptions.SO_LINGER, linger);
    }

    public int getSoLinger() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_LINGER);
    }

    public void sendUrgentData(int data) throws IOException {
        Object obj = 1;
        synchronized (this.sc.blockingLock()) {
            if (this.sc.isBlocking()) {
                int n = this.sc.sendOutOfBandData((byte) data);
                if (!f148-assertionsDisabled) {
                    if (n != 1) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
            } else {
                throw new IllegalBlockingModeException();
            }
        }
    }

    public void setOOBInline(boolean on) throws SocketException {
        setBooleanOption(ExtendedSocketOption.SO_OOBINLINE, on);
    }

    public boolean getOOBInline() throws SocketException {
        return getBooleanOption(ExtendedSocketOption.SO_OOBINLINE);
    }

    public void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can't be negative");
        }
        this.timeout = timeout;
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
    }

    public void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid send size");
        }
        setIntOption(StandardSocketOptions.SO_SNDBUF, size);
    }

    public int getSendBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_SNDBUF);
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid receive size");
        }
        setIntOption(StandardSocketOptions.SO_RCVBUF, size);
    }

    public int getReceiveBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_RCVBUF);
    }

    public void setKeepAlive(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_KEEPALIVE, on);
    }

    public boolean getKeepAlive() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_KEEPALIVE);
    }

    public void setTrafficClass(int tc) throws SocketException {
        setIntOption(StandardSocketOptions.IP_TOS, tc);
    }

    public int getTrafficClass() throws SocketException {
        return getIntOption(StandardSocketOptions.IP_TOS);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_REUSEADDR, on);
    }

    public boolean getReuseAddress() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_REUSEADDR);
    }

    public void close() throws IOException {
        this.sc.close();
    }

    public void shutdownInput() throws IOException {
        try {
            this.sc.shutdownInput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public void shutdownOutput() throws IOException {
        try {
            this.sc.shutdownOutput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public String toString() {
        if (this.sc.isConnected()) {
            return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
        }
        return "Socket[unconnected]";
    }

    public boolean isConnected() {
        return this.sc.isConnected();
    }

    public boolean isBound() {
        return this.sc.localAddress() != null;
    }

    public boolean isClosed() {
        return !this.sc.isOpen();
    }

    public boolean isInputShutdown() {
        return !this.sc.isInputOpen();
    }

    public boolean isOutputShutdown() {
        return !this.sc.isOutputOpen();
    }

    public FileDescriptor getFileDescriptor$() {
        return this.sc.getFD();
    }
}
