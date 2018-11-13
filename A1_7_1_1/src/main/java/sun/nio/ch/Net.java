package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class Net {
    public static final int SHUT_RD = 0;
    public static final int SHUT_RDWR = 2;
    public static final int SHUT_WR = 1;
    static final ProtocolFamily UNSPEC = null;
    private static volatile boolean checkedIPv6;
    private static final boolean exclusiveBind = false;
    private static volatile boolean isIPv6Available;
    private static volatile boolean propRevealLocalAddress;
    private static boolean revealLocalAddress;

    /* renamed from: sun.nio.ch.Net$2 */
    static class AnonymousClass2 implements PrivilegedAction<String> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.Net.2.<init>():void, dex: 
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
        AnonymousClass2() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.Net.2.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.2.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.2.run():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.2.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.2.run():java.lang.Object");
        }

        public String run() {
            return System.getProperty("sun.net.useExclusiveBind");
        }
    }

    /* renamed from: sun.nio.ch.Net$3 */
    static class AnonymousClass3 implements PrivilegedExceptionAction<String> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.Net.3.<init>():void, dex: 
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
        AnonymousClass3() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.nio.ch.Net.3.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.3.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.3.run():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.3.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.3.run():java.lang.Object");
        }

        public String run() {
            return System.getProperty("jdk.net.revealLocalAddress");
        }
    }

    /* renamed from: sun.nio.ch.Net$4 */
    static class AnonymousClass4 implements PrivilegedAction<Inet4Address> {
        final /* synthetic */ NetworkInterface val$interf;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: sun.nio.ch.Net.4.<init>(java.net.NetworkInterface):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass4(java.net.NetworkInterface r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: sun.nio.ch.Net.4.<init>(java.net.NetworkInterface):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.4.<init>(java.net.NetworkInterface):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.4.run():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.4.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.4.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.Net.4.run():java.net.Inet4Address, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.net.Inet4Address run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.Net.4.run():java.net.Inet4Address, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.4.run():java.net.Inet4Address");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.Net.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.<clinit>():void");
    }

    private static native void bind0(FileDescriptor fileDescriptor, boolean z, boolean z2, InetAddress inetAddress, int i) throws IOException;

    private static native int blockOrUnblock4(boolean z, FileDescriptor fileDescriptor, int i, int i2, int i3) throws IOException;

    static native int blockOrUnblock6(boolean z, FileDescriptor fileDescriptor, byte[] bArr, int i, byte[] bArr2) throws IOException;

    private static native boolean canIPv6SocketJoinIPv4Group0();

    private static native boolean canJoin6WithIPv4Group0();

    private static native int connect0(boolean z, FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws IOException;

    private static native int getIntOption0(FileDescriptor fileDescriptor, boolean z, int i, int i2) throws IOException;

    static native int getInterface4(FileDescriptor fileDescriptor) throws IOException;

    static native int getInterface6(FileDescriptor fileDescriptor) throws IOException;

    private static native int isExclusiveBindAvailable();

    private static native boolean isIPv6Available0();

    private static native int joinOrDrop4(boolean z, FileDescriptor fileDescriptor, int i, int i2, int i3) throws IOException;

    private static native int joinOrDrop6(boolean z, FileDescriptor fileDescriptor, byte[] bArr, int i, byte[] bArr2) throws IOException;

    static native void listen(FileDescriptor fileDescriptor, int i) throws IOException;

    private static native InetAddress localInetAddress(FileDescriptor fileDescriptor) throws IOException;

    private static native int localPort(FileDescriptor fileDescriptor) throws IOException;

    private static native InetAddress remoteInetAddress(FileDescriptor fileDescriptor) throws IOException;

    private static native int remotePort(FileDescriptor fileDescriptor) throws IOException;

    private static native void setIntOption0(FileDescriptor fileDescriptor, boolean z, int i, int i2, int i3) throws IOException;

    static native void setInterface4(FileDescriptor fileDescriptor, int i) throws IOException;

    static native void setInterface6(FileDescriptor fileDescriptor, int i) throws IOException;

    static native void shutdown(FileDescriptor fileDescriptor, int i) throws IOException;

    private static native int socket0(boolean z, boolean z2, boolean z3);

    private Net() {
    }

    static boolean isIPv6Available() {
        if (!checkedIPv6) {
            isIPv6Available = isIPv6Available0();
            checkedIPv6 = true;
        }
        return isIPv6Available;
    }

    static boolean useExclusiveBind() {
        return exclusiveBind;
    }

    static boolean canIPv6SocketJoinIPv4Group() {
        return canIPv6SocketJoinIPv4Group0();
    }

    static boolean canJoin6WithIPv4Group() {
        return canJoin6WithIPv4Group0();
    }

    static InetSocketAddress checkAddress(SocketAddress sa) {
        if (sa == null) {
            throw new IllegalArgumentException("sa == null");
        } else if (sa instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) sa;
            if (isa.isUnresolved()) {
                throw new UnresolvedAddressException();
            }
            InetAddress addr = isa.getAddress();
            if (!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true) {
                return isa;
            }
            throw new IllegalArgumentException("Invalid address type");
        } else {
            throw new UnsupportedAddressTypeException();
        }
    }

    static InetSocketAddress asInetSocketAddress(SocketAddress sa) {
        if (sa instanceof InetSocketAddress) {
            return (InetSocketAddress) sa;
        }
        throw new UnsupportedAddressTypeException();
    }

    static void translateToSocketException(Exception x) throws SocketException {
        if (x instanceof SocketException) {
            throw ((SocketException) x);
        }
        Exception nx = x;
        if (x instanceof ClosedChannelException) {
            nx = new SocketException("Socket is closed");
        } else if (x instanceof NotYetConnectedException) {
            nx = new SocketException("Socket is not connected");
        } else if (x instanceof AlreadyBoundException) {
            nx = new SocketException("Already bound");
        } else if (x instanceof NotYetBoundException) {
            nx = new SocketException("Socket is not bound yet");
        } else if (x instanceof UnsupportedAddressTypeException) {
            nx = new SocketException("Unsupported address type");
        } else if (x instanceof UnresolvedAddressException) {
            nx = new SocketException("Unresolved address");
        } else if (x instanceof AlreadyConnectedException) {
            nx = new SocketException("Already connected");
        }
        if (nx != x) {
            nx.initCause(x);
        }
        if (nx instanceof SocketException) {
            throw ((SocketException) nx);
        } else if (nx instanceof RuntimeException) {
            throw ((RuntimeException) nx);
        } else {
            throw new Error("Untranslated exception", nx);
        }
    }

    static void translateException(Exception x, boolean unknownHostForUnresolved) throws IOException {
        if (x instanceof IOException) {
            throw ((IOException) x);
        } else if (unknownHostForUnresolved && (x instanceof UnresolvedAddressException)) {
            throw new UnknownHostException();
        } else {
            translateToSocketException(x);
        }
    }

    static void translateException(Exception x) throws IOException {
        translateException(x, false);
    }

    static InetSocketAddress getRevealedLocalAddress(InetSocketAddress addr) {
        SecurityManager sm = System.getSecurityManager();
        if (addr == null || sm == null) {
            return addr;
        }
        if (!getRevealLocalAddress()) {
            try {
                sm.checkConnect(addr.getAddress().getHostAddress(), -1);
            } catch (SecurityException e) {
                addr = getLoopbackAddress(addr.getPort());
            }
        }
        return addr;
    }

    static String getRevealedLocalAddressAsString(InetSocketAddress addr) {
        if (!(getRevealLocalAddress() || System.getSecurityManager() == null)) {
            addr = getLoopbackAddress(addr.getPort());
        }
        return addr.toString();
    }

    private static boolean getRevealLocalAddress() {
        if (!propRevealLocalAddress) {
            try {
                revealLocalAddress = Boolean.parseBoolean((String) AccessController.doPrivileged(new AnonymousClass3()));
            } catch (Exception e) {
            }
            propRevealLocalAddress = true;
        }
        return revealLocalAddress;
    }

    private static InetSocketAddress getLoopbackAddress(int port) {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    static java.net.Inet4Address anyInet4Address(java.net.NetworkInterface r1) {
        /*
        r0 = new sun.nio.ch.Net$4;
        r0.<init>(r1);
        r0 = java.security.AccessController.doPrivileged(r0);
        r0 = (java.net.Inet4Address) r0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Net.anyInet4Address(java.net.NetworkInterface):java.net.Inet4Address");
    }

    static int inet4AsInt(InetAddress ia) {
        if (ia instanceof Inet4Address) {
            byte[] addr = ia.getAddress();
            return (((addr[3] & 255) | ((addr[2] << 8) & 65280)) | ((addr[1] << 16) & 16711680)) | ((addr[0] << 24) & -16777216);
        }
        throw new AssertionError((Object) "Should not reach here");
    }

    static InetAddress inet4FromInt(int address) {
        byte[] addr = new byte[4];
        addr[0] = (byte) ((address >>> 24) & 255);
        addr[1] = (byte) ((address >>> 16) & 255);
        addr[2] = (byte) ((address >>> 8) & 255);
        addr[3] = (byte) (address & 255);
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            throw new AssertionError((Object) "Should not reach here");
        }
    }

    static byte[] inet6AsByteArray(InetAddress ia) {
        if (ia instanceof Inet6Address) {
            return ia.getAddress();
        }
        if (ia instanceof Inet4Address) {
            byte[] ip4address = ia.getAddress();
            byte[] address = new byte[16];
            address[10] = (byte) -1;
            address[11] = (byte) -1;
            address[12] = ip4address[0];
            address[13] = ip4address[1];
            address[14] = ip4address[2];
            address[15] = ip4address[3];
            return address;
        }
        throw new AssertionError((Object) "Should not reach here");
    }

    static void setSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name, Object value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Invalid option value");
        }
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class) {
            throw new AssertionError((Object) "Should not reach here");
        } else if ((name == StandardSocketOptions.SO_RCVBUF || name == StandardSocketOptions.SO_SNDBUF) && ((Integer) value).intValue() < 0) {
            throw new IllegalArgumentException("Invalid send/receive buffer size");
        } else {
            int i;
            if (name == StandardSocketOptions.SO_LINGER) {
                i = ((Integer) value).intValue();
                if (i < 0) {
                    value = Integer.valueOf(-1);
                }
                if (i > 65535) {
                    value = Integer.valueOf(65535);
                }
            }
            if (name == StandardSocketOptions.IP_TOS) {
                i = ((Integer) value).intValue();
                if (i < 0 || i > 255) {
                    throw new IllegalArgumentException("Invalid IP_TOS value");
                }
            }
            if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
                i = ((Integer) value).intValue();
                if (i < 0 || i > 255) {
                    throw new IllegalArgumentException("Invalid TTL/hop value");
                }
            }
            OptionKey key = SocketOptionRegistry.findOption(name, family);
            if (key == null) {
                throw new AssertionError((Object) "Option not found");
            }
            int arg = type == Integer.class ? ((Integer) value).intValue() : ((Boolean) value).booleanValue() ? 1 : 0;
            setIntOption0(fd, family == UNSPEC, key.level(), key.name(), arg);
        }
    }

    static Object getSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name) throws IOException {
        Class<?> type = name.type();
        if (type == Integer.class || type == Boolean.class) {
            OptionKey key = SocketOptionRegistry.findOption(name, family);
            if (key == null) {
                throw new AssertionError((Object) "Option not found");
            }
            int value = getIntOption0(fd, family == UNSPEC, key.level(), key.name());
            if (type == Integer.class) {
                return Integer.valueOf(value);
            }
            return value == 0 ? Boolean.FALSE : Boolean.TRUE;
        }
        throw new AssertionError((Object) "Should not reach here");
    }

    static FileDescriptor socket(boolean stream) throws IOException {
        return socket(UNSPEC, stream);
    }

    static FileDescriptor socket(ProtocolFamily family, boolean stream) throws IOException {
        boolean preferIPv6 = isIPv6Available() ? family != StandardProtocolFamily.INET : false;
        return IOUtil.newFD(socket0(preferIPv6, stream, false));
    }

    static FileDescriptor serverSocket(boolean stream) {
        return IOUtil.newFD(socket0(isIPv6Available(), stream, true));
    }

    static void bind(FileDescriptor fd, InetAddress addr, int port) throws IOException {
        bind(UNSPEC, fd, addr, port);
    }

    static void bind(ProtocolFamily family, FileDescriptor fd, InetAddress addr, int port) throws IOException {
        boolean preferIPv6 = isIPv6Available() ? family != StandardProtocolFamily.INET : false;
        bind0(fd, preferIPv6, exclusiveBind, addr, port);
    }

    static int connect(FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        return connect(UNSPEC, fd, remote, remotePort);
    }

    static int connect(ProtocolFamily family, FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        boolean preferIPv6 = false;
        BlockGuard.getThreadPolicy().onNetwork();
        if (isIPv6Available() && family != StandardProtocolFamily.INET) {
            preferIPv6 = true;
        }
        return connect0(preferIPv6, fd, remote, remotePort);
    }

    static InetSocketAddress localAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(localInetAddress(fd), localPort(fd));
    }

    static InetSocketAddress remoteAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(remoteInetAddress(fd), remotePort(fd));
    }

    static int join4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return joinOrDrop4(true, fd, group, interf, source);
    }

    static void drop4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        joinOrDrop4(false, fd, group, interf, source);
    }

    static int block4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return blockOrUnblock4(true, fd, group, interf, source);
    }

    static void unblock4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        blockOrUnblock4(false, fd, group, interf, source);
    }

    static int join6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return joinOrDrop6(true, fd, group, index, source);
    }

    static void drop6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        joinOrDrop6(false, fd, group, index, source);
    }

    static int block6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return blockOrUnblock6(true, fd, group, index, source);
    }

    static void unblock6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        blockOrUnblock6(false, fd, group, index, source);
    }
}
