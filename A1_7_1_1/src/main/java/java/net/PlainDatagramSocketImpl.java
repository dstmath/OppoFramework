package java.net;

import java.io.IOException;

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
class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.net.PlainDatagramSocketImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.net.PlainDatagramSocketImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.PlainDatagramSocketImpl.<clinit>():void");
    }

    private static native void init();

    protected native synchronized void bind0(int i, InetAddress inetAddress) throws SocketException;

    protected native void connect0(InetAddress inetAddress, int i) throws SocketException;

    protected native void datagramSocketClose();

    protected native void datagramSocketCreate() throws SocketException;

    protected native void disconnect0(int i);

    protected native byte getTTL() throws IOException;

    protected native int getTimeToLive() throws IOException;

    protected native void join(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native void leave(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native synchronized int peek(InetAddress inetAddress) throws IOException;

    protected native synchronized int peekData(DatagramPacket datagramPacket) throws IOException;

    protected native synchronized void receive0(DatagramPacket datagramPacket) throws IOException;

    protected native void send(DatagramPacket datagramPacket) throws IOException;

    protected native void setTTL(byte b) throws IOException;

    protected native void setTimeToLive(int i) throws IOException;

    protected native Object socketGetOption(int i) throws SocketException;

    protected native void socketSetOption(int i, Object obj) throws SocketException;

    PlainDatagramSocketImpl() {
    }
}
