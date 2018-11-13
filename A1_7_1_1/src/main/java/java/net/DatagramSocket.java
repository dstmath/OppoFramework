package java.net;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import libcore.io.Libcore;
import sun.security.util.SecurityConstants;

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
public class DatagramSocket implements Closeable {
    static final int ST_CONNECTED = 1;
    static final int ST_CONNECTED_NO_IMPL = 2;
    static final int ST_NOT_CONNECTED = 0;
    static DatagramSocketImplFactory factory;
    static Class implClass;
    private boolean bound;
    private Object closeLock;
    private boolean closed;
    int connectState;
    InetAddress connectedAddress;
    int connectedPort;
    private boolean created;
    DatagramSocketImpl impl;
    boolean oldImpl;
    private SocketException pendingConnectException;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.net.DatagramSocket.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.net.DatagramSocket.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.DatagramSocket.<clinit>():void");
    }

    private synchronized void connectInternal(InetAddress address, int port) throws SocketException {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("connect: " + port);
        } else if (address == null) {
            throw new IllegalArgumentException("connect: null address");
        } else {
            checkAddress(address, SecurityConstants.SOCKET_CONNECT_ACTION);
            if (!isClosed()) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    if (address.isMulticastAddress()) {
                        security.checkMulticast(address);
                    } else {
                        security.checkConnect(address.getHostAddress(), port);
                        security.checkAccept(address.getHostAddress(), port);
                    }
                }
                if (!isBound()) {
                    bind(new InetSocketAddress(0));
                }
                try {
                    if (this.oldImpl || ((this.impl instanceof AbstractPlainDatagramSocketImpl) && ((AbstractPlainDatagramSocketImpl) this.impl).nativeConnectDisabled())) {
                        this.connectState = 2;
                    } else {
                        getImpl().connect(address, port);
                        this.connectState = 1;
                    }
                    this.connectedAddress = address;
                    this.connectedPort = port;
                } catch (SocketException se) {
                    this.connectState = 2;
                    throw se;
                } catch (Throwable th) {
                    this.connectedAddress = address;
                    this.connectedPort = port;
                }
            }
        }
    }

    public DatagramSocket() throws SocketException {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.connectState = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        createImpl();
        bind(new InetSocketAddress(0));
    }

    protected DatagramSocket(DatagramSocketImpl impl) {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.connectState = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        if (impl == null) {
            throw new NullPointerException();
        }
        this.impl = impl;
        checkOldImpl();
    }

    public DatagramSocket(SocketAddress bindaddr) throws SocketException {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.connectState = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        createImpl();
        if (bindaddr != null) {
            bind(bindaddr);
        }
    }

    public DatagramSocket(int port) throws SocketException {
        this(port, null);
    }

    public DatagramSocket(int port, InetAddress laddr) throws SocketException {
        this(new InetSocketAddress(laddr, port));
    }

    private void checkOldImpl() {
        if (this.impl != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    public Void run() throws NoSuchMethodException {
                        Class[] cl = new Class[1];
                        cl[0] = DatagramPacket.class;
                        DatagramSocket.this.impl.getClass().getDeclaredMethod("peekData", cl);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                this.oldImpl = true;
            }
        }
    }

    void createImpl() throws SocketException {
        if (this.impl == null) {
            if (factory != null) {
                this.impl = factory.createDatagramSocketImpl();
                checkOldImpl();
            } else {
                this.impl = DefaultDatagramSocketImplFactory.createDatagramSocketImpl(this instanceof MulticastSocket);
                checkOldImpl();
            }
        }
        this.impl.create();
        this.created = true;
    }

    DatagramSocketImpl getImpl() throws SocketException {
        if (!this.created) {
            createImpl();
        }
        return this.impl;
    }

    public synchronized void bind(SocketAddress addr) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (isBound()) {
            throw new SocketException("already bound");
        } else {
            if (addr == null) {
                addr = new InetSocketAddress(0);
            }
            if (addr instanceof InetSocketAddress) {
                InetSocketAddress epoint = (InetSocketAddress) addr;
                if (epoint.isUnresolved()) {
                    throw new SocketException("Unresolved address");
                }
                InetAddress iaddr = epoint.getAddress();
                int port = epoint.getPort();
                checkAddress(iaddr, "bind");
                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    sec.checkListen(port);
                }
                try {
                    getImpl().bind(port, iaddr);
                    this.bound = true;
                } catch (SocketException e) {
                    getImpl().close();
                    throw e;
                }
            }
            throw new IllegalArgumentException("Unsupported address type!");
        }
    }

    void checkAddress(InetAddress addr, String op) {
        if (addr != null) {
            if (!(!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true)) {
                throw new IllegalArgumentException(op + ": invalid address type");
            }
        }
    }

    public void connect(InetAddress address, int port) {
        try {
            connectInternal(address, port);
        } catch (SocketException se) {
            this.pendingConnectException = se;
        }
    }

    public void connect(SocketAddress addr) throws SocketException {
        if (addr == null) {
            throw new IllegalArgumentException("Address can't be null");
        } else if (addr instanceof InetSocketAddress) {
            InetSocketAddress epoint = (InetSocketAddress) addr;
            if (epoint.isUnresolved()) {
                throw new SocketException("Unresolved address");
            }
            connectInternal(epoint.getAddress(), epoint.getPort());
        } else {
            throw new IllegalArgumentException("Unsupported address type");
        }
    }

    public void disconnect() {
        synchronized (this) {
            if (isClosed()) {
                return;
            }
            if (this.connectState == 1) {
                this.impl.disconnect();
            }
            this.connectedAddress = null;
            this.connectedPort = -1;
            this.connectState = 0;
        }
    }

    public boolean isBound() {
        return this.bound;
    }

    public boolean isConnected() {
        return this.connectState != 0;
    }

    public InetAddress getInetAddress() {
        return this.connectedAddress;
    }

    public int getPort() {
        return this.connectedPort;
    }

    public SocketAddress getRemoteSocketAddress() {
        if (isConnected()) {
            return new InetSocketAddress(getInetAddress(), getPort());
        }
        return null;
    }

    public SocketAddress getLocalSocketAddress() {
        if (!isClosed() && isBound()) {
            return new InetSocketAddress(getLocalAddress(), getLocalPort());
        }
        return null;
    }

    public void send(DatagramPacket p) throws IOException {
        synchronized (p) {
            if (isClosed()) {
                throw new SocketException("Socket is closed");
            }
            checkAddress(p.getAddress(), "send");
            if (this.connectState == 0) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    if (p.getAddress().isMulticastAddress()) {
                        security.checkMulticast(p.getAddress());
                    } else {
                        security.checkConnect(p.getAddress().getHostAddress(), p.getPort());
                    }
                }
            } else {
                InetAddress packetAddress = p.getAddress();
                if (packetAddress == null) {
                    p.setAddress(this.connectedAddress);
                    p.setPort(this.connectedPort);
                } else if (!(packetAddress.equals(this.connectedAddress) && p.getPort() == this.connectedPort)) {
                    throw new IllegalArgumentException("connected address and packet address differ");
                }
            }
            if (!isBound()) {
                bind(new InetSocketAddress(0));
            }
            getImpl().send(p);
        }
    }

    public synchronized void receive(DatagramPacket p) throws IOException {
        synchronized (p) {
            if (!isBound()) {
                bind(new InetSocketAddress(0));
            }
            if (this.pendingConnectException != null) {
                throw new SocketException("Pending connect failure", this.pendingConnectException);
            }
            int peekPort;
            DatagramPacket peekPacket;
            if (this.connectState == 0) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    while (true) {
                        String peekAd;
                        if (this.oldImpl) {
                            InetAddress adr = new InetAddress();
                            peekPort = getImpl().peek(adr);
                            peekAd = adr.getHostAddress();
                        } else {
                            peekPacket = new DatagramPacket(new byte[1], 1);
                            peekPort = getImpl().peekData(peekPacket);
                            peekAd = peekPacket.getAddress().getHostAddress();
                        }
                        try {
                            security.checkAccept(peekAd, peekPort);
                            break;
                        } catch (SecurityException e) {
                            getImpl().receive(new DatagramPacket(new byte[1], 1));
                        }
                    }
                }
            }
            if (this.connectState == 2) {
                boolean stop = false;
                while (!stop) {
                    InetAddress peekAddress;
                    if (this.oldImpl) {
                        peekAddress = new InetAddress();
                        peekPort = getImpl().peek(peekAddress);
                    } else {
                        peekPacket = new DatagramPacket(new byte[1], 1);
                        peekPort = getImpl().peekData(peekPacket);
                        peekAddress = peekPacket.getAddress();
                    }
                    if (this.connectedAddress.equals(peekAddress) && this.connectedPort == peekPort) {
                        stop = true;
                    } else {
                        getImpl().receive(new DatagramPacket(new byte[1], 1));
                    }
                }
            }
            getImpl().receive(p);
        }
    }

    public InetAddress getLocalAddress() {
        if (isClosed()) {
            return null;
        }
        InetAddress in;
        try {
            in = (InetAddress) getImpl().getOption(15);
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkConnect(in.getHostAddress(), -1);
            }
        } catch (Exception e) {
            in = InetAddress.anyLocalAddress();
        }
        return in;
    }

    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        try {
            return getImpl().getLocalPort();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    public synchronized int getSoTimeout() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (getImpl() == null) {
            return 0;
        } else {
            Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
            if (!(o instanceof Integer)) {
                return 0;
            }
            return ((Integer) o).intValue();
        }
    }

    public synchronized void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative send size");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            getImpl().setOption(SocketOptions.SO_SNDBUF, new Integer(size));
        }
    }

    public synchronized int getSendBufferSize() throws SocketException {
        int result;
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_SNDBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
        }
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        int result;
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }

    public synchronized void setReuseAddress(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (this.oldImpl) {
            int i;
            DatagramSocketImpl impl = getImpl();
            if (on) {
                i = -1;
            } else {
                i = 0;
            }
            impl.setOption(4, new Integer(i));
        } else {
            getImpl().setOption(4, Boolean.valueOf(on));
        }
    }

    public synchronized boolean getReuseAddress() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Boolean) getImpl().getOption(4)).booleanValue();
    }

    public synchronized void setBroadcast(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(32, Boolean.valueOf(on));
    }

    public synchronized boolean getBroadcast() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Boolean) getImpl().getOption(32)).booleanValue();
    }

    public synchronized void setTrafficClass(int tc) throws SocketException {
        if (tc < 0 || tc > 255) {
            throw new IllegalArgumentException("tc is not in range 0 -- 255");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            getImpl().setOption(3, new Integer(tc));
        }
    }

    public synchronized int getTrafficClass() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Integer) getImpl().getOption(3)).intValue();
    }

    public void close() {
        synchronized (this.closeLock) {
            if (isClosed()) {
                return;
            }
            this.impl.close();
            this.closed = true;
        }
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this.closeLock) {
            z = this.closed;
        }
        return z;
    }

    public DatagramChannel getChannel() {
        return null;
    }

    public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac) throws IOException {
        synchronized (DatagramSocket.class) {
            if (factory != null) {
                throw new SocketException("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            factory = fac;
        }
    }

    public FileDescriptor getFileDescriptor$() {
        return this.impl.fd;
    }

    public void setNetworkInterface(NetworkInterface netInterface) throws SocketException {
        if (netInterface == null) {
            throw new NullPointerException("netInterface == null");
        }
        try {
            Libcore.os.setsockoptIfreq(this.impl.fd, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, netInterface.getName());
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }
}
