package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class InheritedChannel {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f142-assertionsDisabled = false;
    private static final int O_RDONLY = 0;
    private static final int O_RDWR = 2;
    private static final int O_WRONLY = 1;
    private static final int SOCK_DGRAM = 2;
    private static final int SOCK_STREAM = 1;
    private static final int UNKNOWN = -1;
    private static Channel channel;
    private static int devnull;
    private static boolean haveChannel;

    public static class InheritedDatagramChannelImpl extends DatagramChannelImpl {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void, dex: 
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
        InheritedDatagramChannelImpl(java.nio.channels.spi.SelectorProvider r1, java.io.FileDescriptor r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.implCloseSelectableChannel():void, dex: 
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
        protected void implCloseSelectableChannel() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.implCloseSelectableChannel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.implCloseSelectableChannel():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.kill():void, dex: 
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
        public /* bridge */ /* synthetic */ void kill() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.kill():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.kill():void");
        }

        public /* bridge */ /* synthetic */ SocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ int read(ByteBuffer buf) {
            return super.read(buf);
        }

        public /* bridge */ /* synthetic */ long read(ByteBuffer[] dsts, int offset, int length) {
            return super.read(dsts, offset, length);
        }

        public /* bridge */ /* synthetic */ SocketAddress receive(ByteBuffer dst) {
            return super.receive(dst);
        }

        public /* bridge */ /* synthetic */ SocketAddress remoteAddress() {
            return super.remoteAddress();
        }

        public /* bridge */ /* synthetic */ int send(ByteBuffer src, SocketAddress target) {
            return super.send(src, target);
        }

        public /* bridge */ /* synthetic */ DatagramChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ NetworkChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ DatagramSocket socket() {
            return super.socket();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
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
        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int r1, sun.nio.ch.SelectionKeyImpl r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void");
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }

        public /* bridge */ /* synthetic */ int write(ByteBuffer buf) {
            return super.write(buf);
        }

        public /* bridge */ /* synthetic */ long write(ByteBuffer[] srcs, int offset, int length) {
            return super.write(srcs, offset, length);
        }
    }

    public static class InheritedServerSocketChannelImpl extends ServerSocketChannelImpl {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void, dex: 
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
        InheritedServerSocketChannelImpl(java.nio.channels.spi.SelectorProvider r1, java.io.FileDescriptor r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor):void");
        }

        public /* bridge */ /* synthetic */ SocketChannel accept() {
            return super.accept();
        }

        public /* bridge */ /* synthetic */ ServerSocketChannel bind(SocketAddress local, int backlog) {
            return super.bind(local, backlog);
        }

        public /* bridge */ /* synthetic */ FileDescriptor getFD() {
            return super.getFD();
        }

        public /* bridge */ /* synthetic */ int getFDVal() {
            return super.getFDVal();
        }

        public /* bridge */ /* synthetic */ SocketAddress getLocalAddress() {
            return super.getLocalAddress();
        }

        public /* bridge */ /* synthetic */ Object getOption(SocketOption name) {
            return super.getOption(name);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.implCloseSelectableChannel():void, dex: 
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
        protected void implCloseSelectableChannel() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.implCloseSelectableChannel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.implCloseSelectableChannel():void");
        }

        public /* bridge */ /* synthetic */ boolean isBound() {
            return super.isBound();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.kill():void, dex: 
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
        public /* bridge */ /* synthetic */ void kill() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.kill():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.kill():void");
        }

        public /* bridge */ /* synthetic */ InetSocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ NetworkChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ ServerSocketChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ ServerSocket socket() {
            return super.socket();
        }

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
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
        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int r1, sun.nio.ch.SelectionKeyImpl r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void");
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }
    }

    public static class InheritedSocketChannelImpl extends SocketChannelImpl {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor, java.net.InetSocketAddress):void, dex: 
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
        InheritedSocketChannelImpl(java.nio.channels.spi.SelectorProvider r1, java.io.FileDescriptor r2, java.net.InetSocketAddress r3) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor, java.net.InetSocketAddress):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.<init>(java.nio.channels.spi.SelectorProvider, java.io.FileDescriptor, java.net.InetSocketAddress):void");
        }

        public /* bridge */ /* synthetic */ NetworkChannel bind(SocketAddress local) {
            return super.bind(local);
        }

        public /* bridge */ /* synthetic */ SocketChannel bind(SocketAddress local) {
            return super.bind(local);
        }

        public /* bridge */ /* synthetic */ boolean connect(SocketAddress sa) {
            return super.connect(sa);
        }

        public /* bridge */ /* synthetic */ boolean finishConnect() {
            return super.finishConnect();
        }

        public /* bridge */ /* synthetic */ FileDescriptor getFD() {
            return super.getFD();
        }

        public /* bridge */ /* synthetic */ int getFDVal() {
            return super.getFDVal();
        }

        public /* bridge */ /* synthetic */ SocketAddress getLocalAddress() {
            return super.getLocalAddress();
        }

        public /* bridge */ /* synthetic */ Object getOption(SocketOption name) {
            return super.getOption(name);
        }

        public /* bridge */ /* synthetic */ SocketAddress getRemoteAddress() {
            return super.getRemoteAddress();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.implCloseSelectableChannel():void, dex: 
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
        protected void implCloseSelectableChannel() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.implCloseSelectableChannel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.implCloseSelectableChannel():void");
        }

        public /* bridge */ /* synthetic */ boolean isConnected() {
            return super.isConnected();
        }

        public /* bridge */ /* synthetic */ boolean isConnectionPending() {
            return super.isConnectionPending();
        }

        public /* bridge */ /* synthetic */ boolean isInputOpen() {
            return super.isInputOpen();
        }

        public /* bridge */ /* synthetic */ boolean isOutputOpen() {
            return super.isOutputOpen();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.kill():void, dex: 
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
        public /* bridge */ /* synthetic */ void kill() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.kill():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.kill():void");
        }

        public /* bridge */ /* synthetic */ InetSocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ int read(ByteBuffer buf) {
            return super.read(buf);
        }

        public /* bridge */ /* synthetic */ long read(ByteBuffer[] dsts, int offset, int length) {
            return super.read(dsts, offset, length);
        }

        public /* bridge */ /* synthetic */ SocketAddress remoteAddress() {
            return super.remoteAddress();
        }

        public /* bridge */ /* synthetic */ NetworkChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ SocketChannel setOption(SocketOption name, Object value) {
            return super.setOption(name, value);
        }

        public /* bridge */ /* synthetic */ SocketChannel shutdownInput() {
            return super.shutdownInput();
        }

        public /* bridge */ /* synthetic */ SocketChannel shutdownOutput() {
            return super.shutdownOutput();
        }

        public /* bridge */ /* synthetic */ Socket socket() {
            return super.socket();
        }

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
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
        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int r1, sun.nio.ch.SelectionKeyImpl r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.InheritedSocketChannelImpl.translateAndSetInterestOps(int, sun.nio.ch.SelectionKeyImpl):void");
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }

        public /* bridge */ /* synthetic */ int write(ByteBuffer buf) {
            return super.write(buf);
        }

        public /* bridge */ /* synthetic */ long write(ByteBuffer[] srcs, int offset, int length) {
            return super.write(srcs, offset, length);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.InheritedChannel.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.InheritedChannel.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.InheritedChannel.<clinit>():void");
    }

    private static native void close0(int i) throws IOException;

    private static native int dup(int i) throws IOException;

    private static native void dup2(int i, int i2) throws IOException;

    private static native int open0(String str, int i) throws IOException;

    private static native InetAddress peerAddress0(int i);

    private static native int peerPort0(int i);

    private static native int soType0(int i);

    InheritedChannel() {
    }

    private static void detachIOStreams() {
        try {
            dup2(devnull, 0);
            dup2(devnull, 1);
            dup2(devnull, 2);
        } catch (IOException e) {
            throw new InternalError();
        }
    }

    private static void checkAccess(Channel c) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("inheritedChannel"));
        }
    }

    private static Channel createChannel() throws IOException {
        int i = 1;
        int fdVal = dup(0);
        int st = soType0(fdVal);
        if (st == 1 || st == 2) {
            Class[] paramTypes = new Class[1];
            paramTypes[0] = Integer.TYPE;
            Constructor ctr = Reflect.lookupConstructor("java.io.FileDescriptor", paramTypes);
            Object[] args = new Object[1];
            args[0] = new Integer(fdVal);
            FileDescriptor fd = (FileDescriptor) Reflect.invoke(ctr, args);
            SelectorProvider provider = SelectorProvider.provider();
            if (f142-assertionsDisabled || (provider instanceof SelectorProviderImpl)) {
                Channel c;
                if (st == 1) {
                    InetAddress ia = peerAddress0(fdVal);
                    if (ia == null) {
                        c = new InheritedServerSocketChannelImpl(provider, fd);
                    } else {
                        int port = peerPort0(fdVal);
                        if (!f142-assertionsDisabled) {
                            if (port <= 0) {
                                i = 0;
                            }
                            if (i == 0) {
                                throw new AssertionError();
                            }
                        }
                        c = new InheritedSocketChannelImpl(provider, fd, new InetSocketAddress(ia, port));
                    }
                } else {
                    c = new InheritedDatagramChannelImpl(provider, fd);
                }
                return c;
            }
            throw new AssertionError();
        }
        close0(fdVal);
        return null;
    }

    public static synchronized Channel getChannel() throws IOException {
        Channel channel;
        synchronized (InheritedChannel.class) {
            if (devnull < 0) {
                devnull = open0("/dev/null", 2);
            }
            if (!haveChannel) {
                channel = createChannel();
                haveChannel = true;
            }
            if (channel != null) {
                checkAccess(channel);
            }
            channel = channel;
        }
        return channel;
    }
}
