package java.net;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Enumeration;
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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
abstract class AbstractPlainDatagramSocketImpl extends DatagramSocketImpl {
    private static final boolean connectDisabled = false;
    private static final String os = null;
    boolean connected;
    private InetAddress connectedAddress;
    private int connectedPort;
    private final CloseGuard guard;
    private boolean loopbackMode;
    private int multicastInterface;
    int timeout;
    private int trafficClass;
    private int ttl;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.AbstractPlainDatagramSocketImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.AbstractPlainDatagramSocketImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.AbstractPlainDatagramSocketImpl.<clinit>():void");
    }

    protected abstract void bind0(int i, InetAddress inetAddress) throws SocketException;

    protected abstract void connect0(InetAddress inetAddress, int i) throws SocketException;

    protected abstract void datagramSocketClose();

    protected abstract void datagramSocketCreate() throws SocketException;

    protected abstract void disconnect0(int i);

    protected abstract byte getTTL() throws IOException;

    protected abstract int getTimeToLive() throws IOException;

    protected abstract void join(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract void leave(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract int peek(InetAddress inetAddress) throws IOException;

    protected abstract int peekData(DatagramPacket datagramPacket) throws IOException;

    protected abstract void receive0(DatagramPacket datagramPacket) throws IOException;

    protected abstract void send(DatagramPacket datagramPacket) throws IOException;

    protected abstract void setTTL(byte b) throws IOException;

    protected abstract void setTimeToLive(int i) throws IOException;

    protected abstract Object socketGetOption(int i) throws SocketException;

    protected abstract void socketSetOption(int i, Object obj) throws SocketException;

    AbstractPlainDatagramSocketImpl() {
        this.timeout = 0;
        this.connected = false;
        this.trafficClass = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        this.multicastInterface = 0;
        this.loopbackMode = true;
        this.ttl = -1;
        this.guard = CloseGuard.get();
    }

    protected synchronized void create() throws SocketException {
        ResourceManager.beforeUdpCreate();
        this.fd = new FileDescriptor();
        try {
            datagramSocketCreate();
            if (this.fd != null && this.fd.valid()) {
                this.guard.open("close");
            }
        } catch (SocketException ioe) {
            ResourceManager.afterUdpClose();
            this.fd = null;
            throw ioe;
        }
    }

    protected synchronized void bind(int lport, InetAddress laddr) throws SocketException {
        bind0(lport, laddr);
    }

    protected void connect(InetAddress address, int port) throws SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        connect0(address, port);
        this.connectedAddress = address;
        this.connectedPort = port;
        this.connected = true;
    }

    protected void disconnect() {
        disconnect0(this.connectedAddress.holder().getFamily());
        this.connected = false;
        this.connectedAddress = null;
        this.connectedPort = -1;
    }

    protected synchronized void receive(DatagramPacket p) throws IOException {
        receive0(p);
    }

    protected void join(InetAddress inetaddr) throws IOException {
        join(inetaddr, null);
    }

    protected void leave(InetAddress inetaddr) throws IOException {
        leave(inetaddr, null);
    }

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        join(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        leave(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected void close() {
        this.guard.close();
        if (this.fd != null) {
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            this.fd = null;
        }
    }

    protected boolean isClosed() {
        return this.fd == null;
    }

    protected void finalize() {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    public void setOption(int optID, Object o) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        switch (optID) {
            case 3:
                if (o != null && (o instanceof Integer)) {
                    this.trafficClass = ((Integer) o).intValue();
                    break;
                }
                throw new SocketException("bad argument for IP_TOS");
            case 4:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for SO_REUSEADDR");
                }
            case 15:
                throw new SocketException("Cannot re-bind Socket");
            case 16:
                if (o == null || !(o instanceof InetAddress)) {
                    throw new SocketException("bad argument for IP_MULTICAST_IF");
                }
            case 18:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for IP_MULTICAST_LOOP");
                }
            case SocketOptions.IP_MULTICAST_IF2 /*31*/:
                if (o != null && ((o instanceof Integer) || (o instanceof NetworkInterface))) {
                    if (o instanceof NetworkInterface) {
                        o = new Integer(((NetworkInterface) o).getIndex());
                        break;
                    }
                }
                throw new SocketException("bad argument for IP_MULTICAST_IF2");
                break;
            case 32:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for SO_BROADCAST");
                }
            case SocketOptions.SO_SNDBUF /*4097*/:
            case SocketOptions.SO_RCVBUF /*4098*/:
                if (o == null || !(o instanceof Integer) || ((Integer) o).intValue() < 0) {
                    throw new SocketException("bad argument for SO_SNDBUF or SO_RCVBUF");
                }
            case SocketOptions.SO_TIMEOUT /*4102*/:
                if (o == null || !(o instanceof Integer)) {
                    throw new SocketException("bad argument for SO_TIMEOUT");
                }
                int tmp = ((Integer) o).intValue();
                if (tmp < 0) {
                    throw new IllegalArgumentException("timeout < 0");
                }
                this.timeout = tmp;
                return;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        socketSetOption(optID, o);
    }

    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        Object result;
        switch (optID) {
            case 3:
                result = socketGetOption(optID);
                if (((Integer) result).intValue() == -1) {
                    result = new Integer(this.trafficClass);
                    break;
                }
                break;
            case 4:
            case 15:
            case 16:
            case 18:
            case SocketOptions.IP_MULTICAST_IF2 /*31*/:
            case 32:
            case SocketOptions.SO_SNDBUF /*4097*/:
            case SocketOptions.SO_RCVBUF /*4098*/:
                result = socketGetOption(optID);
                if (optID == 16) {
                    return getNIFirstAddress(((Integer) result).intValue());
                }
                break;
            case SocketOptions.SO_TIMEOUT /*4102*/:
                result = new Integer(this.timeout);
                break;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        return result;
    }

    static InetAddress getNIFirstAddress(int niIndex) throws SocketException {
        if (niIndex > 0) {
            Enumeration<InetAddress> addressesEnum = NetworkInterface.getByIndex(niIndex).getInetAddresses();
            if (addressesEnum.hasMoreElements()) {
                return (InetAddress) addressesEnum.nextElement();
            }
        }
        return InetAddress.anyLocalAddress();
    }

    protected boolean nativeConnectDisabled() {
        return connectDisabled;
    }
}
