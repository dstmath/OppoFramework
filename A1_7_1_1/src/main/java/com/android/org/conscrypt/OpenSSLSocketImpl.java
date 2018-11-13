package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeCrypto.SSLHandshakeCallbacks;
import com.android.org.conscrypt.SSLParametersImpl.AliasChooser;
import com.android.org.conscrypt.SSLParametersImpl.PSKCallbacks;
import com.android.org.conscrypt.util.ArrayUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public class OpenSSLSocketImpl extends SSLSocket implements SSLHandshakeCallbacks, AliasChooser, PSKCallbacks {
    private static final boolean DBG_STATE = false;
    private static final int STATE_CLOSED = 5;
    private static final int STATE_HANDSHAKE_COMPLETED = 2;
    private static final int STATE_HANDSHAKE_STARTED = 1;
    private static final int STATE_NEW = 0;
    private static final int STATE_READY = 4;
    private static final int STATE_READY_HANDSHAKE_CUT_THROUGH = 3;
    private final boolean autoClose;
    OpenSSLKey channelIdPrivateKey;
    private final Object guard;
    private OpenSSLSessionImpl handshakeSession;
    private int handshakeTimeoutMilliseconds;
    private SSLInputStream is;
    private ArrayList<HandshakeCompletedListener> listeners;
    private SSLOutputStream os;
    private String peerHostname;
    private final int peerPort;
    private int readTimeoutMilliseconds;
    private final Socket socket;
    private long sslNativePointer;
    private final SSLParametersImpl sslParameters;
    private OpenSSLSessionImpl sslSession;
    private int state;
    private final Object stateLock;
    private int writeTimeoutMilliseconds;

    private class SSLInputStream extends InputStream {
        private final Object readLock = new Object();

        SSLInputStream() {
        }

        public int read() throws IOException {
            byte[] buffer = new byte[1];
            if (read(buffer, 0, 1) != -1) {
                return buffer[0] & 255;
            }
            return -1;
        }

        public int read(byte[] buf, int offset, int byteCount) throws IOException {
            Platform.blockGuardOnNetwork();
            OpenSSLSocketImpl.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount == 0) {
                return 0;
            }
            int SSL_read;
            synchronized (this.readLock) {
                synchronized (OpenSSLSocketImpl.this.stateLock) {
                    if (OpenSSLSocketImpl.this.state == OpenSSLSocketImpl.STATE_CLOSED) {
                        throw new SocketException("socket is closed");
                    }
                }
                SSL_read = NativeCrypto.SSL_read(OpenSSLSocketImpl.this.sslNativePointer, Platform.getFileDescriptor(OpenSSLSocketImpl.this.socket), OpenSSLSocketImpl.this, buf, offset, byteCount, OpenSSLSocketImpl.this.getSoTimeout());
            }
            return SSL_read;
        }

        public void awaitPendingOps() {
            synchronized (this.readLock) {
            }
        }
    }

    private class SSLOutputStream extends OutputStream {
        private final Object writeLock = new Object();

        SSLOutputStream() {
        }

        public void write(int oneByte) throws IOException {
            write(new byte[]{(byte) (oneByte & 255)});
        }

        public void write(byte[] buf, int offset, int byteCount) throws IOException {
            Platform.blockGuardOnNetwork();
            OpenSSLSocketImpl.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount != 0) {
                synchronized (this.writeLock) {
                    synchronized (OpenSSLSocketImpl.this.stateLock) {
                        if (OpenSSLSocketImpl.this.state == OpenSSLSocketImpl.STATE_CLOSED) {
                            throw new SocketException("socket is closed");
                        }
                    }
                    NativeCrypto.SSL_write(OpenSSLSocketImpl.this.sslNativePointer, Platform.getFileDescriptor(OpenSSLSocketImpl.this.socket), OpenSSLSocketImpl.this, buf, offset, byteCount, OpenSSLSocketImpl.this.writeTimeoutMilliseconds);
                }
            }
        }

        public void awaitPendingOps() {
            synchronized (this.writeLock) {
            }
        }
    }

    protected OpenSSLSocketImpl(SSLParametersImpl sslParameters) throws IOException {
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        super(address, port);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = socket;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = autoClose;
        this.sslParameters = sslParameters;
    }

    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (this.peerHostname == null && (endpoint instanceof InetSocketAddress)) {
            this.peerHostname = Platform.getHostStringFromInetSocketAddress((InetSocketAddress) endpoint);
        }
        super.connect(endpoint, timeout);
    }

    private void checkOpen() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0016, code:
            if (com.android.org.conscrypt.NativeCrypto.isBoringSSL != false) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:9:0x0018, code:
            r32 = r36.sslParameters.getSecureRandomMember();
     */
    /* JADX WARNING: Missing block: B:10:0x0022, code:
            if (r32 != null) goto L_0x011a;
     */
    /* JADX WARNING: Missing block: B:11:0x0024, code:
            com.android.org.conscrypt.NativeCrypto.RAND_load_file("/dev/urandom", 1024);
     */
    /* JADX WARNING: Missing block: B:12:0x002c, code:
            r11 = r36.sslParameters.getUseClientMode();
            r36.sslNativePointer = 0;
            r29 = true;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r4 = r36.sslParameters.getSessionContext().sslCtxNativePointer;
            r36.sslNativePointer = com.android.org.conscrypt.NativeCrypto.SSL_new(r4);
            com.android.org.conscrypt.Platform.closeGuardOpen(r36.guard, "close");
            r25 = getEnableSessionCreation();
     */
    /* JADX WARNING: Missing block: B:15:0x005e, code:
            if (r25 != false) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:16:0x0060, code:
            com.android.org.conscrypt.NativeCrypto.SSL_set_session_creation_enabled(r36.sslNativePointer, r25);
     */
    /* JADX WARNING: Missing block: B:17:0x0069, code:
            com.android.org.conscrypt.NativeCrypto.SSL_set_reject_peer_renegotiations(r36.sslNativePointer, DBG_STATE);
     */
    /* JADX WARNING: Missing block: B:18:0x0071, code:
            if (r11 == false) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:20:0x007f, code:
            if (r36.sslParameters.isCTVerificationEnabled(getHostname()) == false) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:21:0x0081, code:
            com.android.org.conscrypt.NativeCrypto.SSL_enable_signed_cert_timestamps(r36.sslNativePointer);
            com.android.org.conscrypt.NativeCrypto.SSL_enable_ocsp_stapling(r36.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:22:0x008f, code:
            r18 = r36.sslParameters.getSessionToReuse(r36.sslNativePointer, getHostnameOrIP(), getPort());
            r36.sslParameters.setSSLParameters(r4, r36.sslNativePointer, r36, r36, getHostname());
            r36.sslParameters.setCertificateValidation(r36.sslNativePointer);
            r36.sslParameters.setTlsChannelId(r36.sslNativePointer, r36.channelIdPrivateKey);
            r30 = getSoTimeout();
            r31 = getSoWriteTimeout();
     */
    /* JADX WARNING: Missing block: B:23:0x00dc, code:
            if (r36.handshakeTimeoutMilliseconds < 0) goto L_0x00f0;
     */
    /* JADX WARNING: Missing block: B:24:0x00de, code:
            setSoTimeout(r36.handshakeTimeoutMilliseconds);
            setSoWriteTimeout(r36.handshakeTimeoutMilliseconds);
     */
    /* JADX WARNING: Missing block: B:25:0x00f0, code:
            r3 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:26:0x00f4, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:30:0x00fa, code:
            if (r36.state != STATE_CLOSED) goto L_0x012c;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:33:0x00fd, code:
            if (1 == null) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:34:0x00ff, code:
            r6 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:35:0x0103, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r36.state = STATE_CLOSED;
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:39:0x0110, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:48:0x011a, code:
            com.android.org.conscrypt.NativeCrypto.RAND_seed(r32.generateSeed(1024));
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            r6 = r36.sslNativePointer;
            r8 = com.android.org.conscrypt.Platform.getFileDescriptor(r36.socket);
            r10 = getSoTimeout();
            r12 = r36.sslParameters.npnProtocols;
     */
    /* JADX WARNING: Missing block: B:57:0x0143, code:
            if (r11 == false) goto L_0x0244;
     */
    /* JADX WARNING: Missing block: B:58:0x0145, code:
            r13 = null;
     */
    /* JADX WARNING: Missing block: B:59:0x0146, code:
            r14 = com.android.org.conscrypt.NativeCrypto.SSL_do_handshake(r6, r8, r36, r10, r11, r12, r13);
     */
    /* JADX WARNING: Missing block: B:60:0x014b, code:
            r21 = DBG_STATE;
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            r6 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:63:0x0152, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:67:0x0158, code:
            if (r36.state != 2) goto L_0x02b3;
     */
    /* JADX WARNING: Missing block: B:68:0x015a, code:
            r21 = true;
     */
    /* JADX WARNING: Missing block: B:70:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:71:0x015d, code:
            r36.sslSession = r36.sslParameters.setupSession(r14, r36.sslNativePointer, r18, getHostnameOrIP(), getPort(), r21);
     */
    /* JADX WARNING: Missing block: B:72:0x017b, code:
            if (r36.handshakeTimeoutMilliseconds < 0) goto L_0x018b;
     */
    /* JADX WARNING: Missing block: B:73:0x017d, code:
            setSoTimeout(r30);
            setSoWriteTimeout(r31);
     */
    /* JADX WARNING: Missing block: B:74:0x018b, code:
            if (r21 == false) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:75:0x018d, code:
            r2 = r36.sslSession.getCipherSuite();
     */
    /* JADX WARNING: Missing block: B:76:0x0199, code:
            if (r2.length() <= 0) goto L_0x01ba;
     */
    /* JADX WARNING: Missing block: B:77:0x019b, code:
            java.lang.System.out.println("gba_cipher_suite:" + r2);
            java.lang.System.setProperty("gba.ciper.suite", r2);
     */
    /* JADX WARNING: Missing block: B:78:0x01ba, code:
            notifyHandshakeCompletedListeners();
     */
    /* JADX WARNING: Missing block: B:79:0x01bd, code:
            r6 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:80:0x01c1, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:83:0x01c7, code:
            if (r36.state != STATE_CLOSED) goto L_0x02db;
     */
    /* JADX WARNING: Missing block: B:84:0x01c9, code:
            r29 = true;
     */
    /* JADX WARNING: Missing block: B:86:0x01d0, code:
            if (r36.state != 1) goto L_0x02df;
     */
    /* JADX WARNING: Missing block: B:87:0x01d2, code:
            r36.state = 3;
     */
    /* JADX WARNING: Missing block: B:88:0x01d7, code:
            if (r29 != false) goto L_0x01e0;
     */
    /* JADX WARNING: Missing block: B:89:0x01d9, code:
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:91:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:92:0x01e1, code:
            if (r29 == false) goto L_0x01f8;
     */
    /* JADX WARNING: Missing block: B:93:0x01e3, code:
            r6 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:94:0x01e7, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            r36.state = STATE_CLOSED;
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:98:0x01f4, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:100:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:106:0x01fc, code:
            r24 = move-exception;
     */
    /* JADX WARNING: Missing block: B:108:?, code:
            java.lang.System.out.println("SSLProtocolException:" + r24.getMessage());
     */
    /* JADX WARNING: Missing block: B:109:0x022a, code:
            throw ((javax.net.ssl.SSLHandshakeException) new javax.net.ssl.SSLHandshakeException("Handshake failed").initCause(r24));
     */
    /* JADX WARNING: Missing block: B:111:0x022c, code:
            if (r29 != false) goto L_0x022e;
     */
    /* JADX WARNING: Missing block: B:113:0x0232, code:
            monitor-enter(r36.stateLock);
     */
    /* JADX WARNING: Missing block: B:116:?, code:
            r36.state = STATE_CLOSED;
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:119:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:122:?, code:
            r13 = r36.sslParameters.alpnProtocols;
     */
    /* JADX WARNING: Missing block: B:123:0x024c, code:
            r23 = move-exception;
     */
    /* JADX WARNING: Missing block: B:126:0x0251, code:
            monitor-enter(r36.stateLock);
     */
    /* JADX WARNING: Missing block: B:130:0x0257, code:
            if (r36.state == STATE_CLOSED) goto L_0x0259;
     */
    /* JADX WARNING: Missing block: B:133:0x025a, code:
            if (1 != null) goto L_0x025c;
     */
    /* JADX WARNING: Missing block: B:135:0x0260, code:
            monitor-enter(r36.stateLock);
     */
    /* JADX WARNING: Missing block: B:138:?, code:
            r36.state = STATE_CLOSED;
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:141:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:142:0x0271, code:
            return;
     */
    /* JADX WARNING: Missing block: B:150:0x0285, code:
            if (r23.getMessage().contains("unexpected CCS") != false) goto L_0x0287;
     */
    /* JADX WARNING: Missing block: B:151:0x0287, code:
            com.android.org.conscrypt.Platform.logEvent(java.lang.String.format("ssl_unexpected_ccs: host=%s", new java.lang.Object[]{getHostnameOrIP()}));
     */
    /* JADX WARNING: Missing block: B:152:0x029b, code:
            throw r23;
     */
    /* JADX WARNING: Missing block: B:156:0x029f, code:
            r22 = move-exception;
     */
    /* JADX WARNING: Missing block: B:157:0x02a0, code:
            r0 = new javax.net.ssl.SSLHandshakeException(r22.getMessage());
            r0.initCause(r22);
     */
    /* JADX WARNING: Missing block: B:158:0x02b2, code:
            throw r0;
     */
    /* JADX WARNING: Missing block: B:162:0x02b8, code:
            if (r36.state != STATE_CLOSED) goto L_0x015c;
     */
    /* JADX WARNING: Missing block: B:164:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:165:0x02bb, code:
            if (1 == null) goto L_0x02d2;
     */
    /* JADX WARNING: Missing block: B:166:0x02bd, code:
            r6 = r36.stateLock;
     */
    /* JADX WARNING: Missing block: B:167:0x02c1, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:170:?, code:
            r36.state = STATE_CLOSED;
            r36.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:171:0x02ce, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:173:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:183:0x02db, code:
            r29 = DBG_STATE;
     */
    /* JADX WARNING: Missing block: B:186:0x02e4, code:
            if (r36.state != 2) goto L_0x01d7;
     */
    /* JADX WARNING: Missing block: B:187:0x02e6, code:
            r36.state = 4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startHandshake() throws IOException {
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 0) {
                this.state = 1;
            }
        }
    }

    public String getHostname() {
        return this.peerHostname;
    }

    public String getHostnameOrIP() {
        if (this.peerHostname != null) {
            return this.peerHostname;
        }
        InetAddress peerAddress = getInetAddress();
        if (peerAddress != null) {
            return peerAddress.getHostAddress();
        }
        return null;
    }

    public int getPort() {
        return this.peerPort == -1 ? super.getPort() : this.peerPort;
    }

    public void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.sslParameters.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals, this.sslNativePointer, this);
    }

    public int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.sslParameters.clientPSKKeyRequested(identityHint, identity, key, this);
    }

    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.sslParameters.serverPSKKeyRequested(identityHint, identity, key, this);
    }

    /* JADX WARNING: Missing block: B:16:0x0018, code:
            r4.sslSession.resetId();
     */
    /* JADX WARNING: Missing block: B:17:0x0023, code:
            if (r4.sslParameters.getUseClientMode() == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:18:0x0025, code:
            r0 = r4.sslParameters.getClientSessionContext();
     */
    /* JADX WARNING: Missing block: B:19:0x002b, code:
            r0.putSession(r4.sslSession);
            notifyHandshakeCompletedListeners();
            r2 = r4.stateLock;
     */
    /* JADX WARNING: Missing block: B:20:0x0035, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r4.state = 4;
            r4.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:24:0x003e, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:35:0x004a, code:
            r0 = r4.sslParameters.getServerSessionContext();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSSLStateChange(long sslSessionNativePtr, int type, int val) {
        if (type == 32) {
            synchronized (this.stateLock) {
                if (this.state == 1) {
                    this.state = 2;
                } else if (this.state != 3) {
                    if (this.state == STATE_CLOSED) {
                    }
                }
            }
        }
    }

    private void notifyHandshakeCompletedListeners() {
        if (this.listeners != null && !this.listeners.isEmpty()) {
            HandshakeCompletedEvent event = new HandshakeCompletedEvent(this, this.sslSession);
            for (HandshakeCompletedListener listener : this.listeners) {
                try {
                    listener.handshakeCompleted(event);
                } catch (RuntimeException e) {
                    Thread thread = Thread.currentThread();
                    thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
                }
            }
        }
    }

    public void verifyCertificateChain(long sslSessionNativePtr, long[] certRefs, String authMethod) throws CertificateException {
        try {
            X509TrustManager x509tm = this.sslParameters.getX509TrustManager();
            if (x509tm == null) {
                throw new CertificateException("No X.509 TrustManager");
            }
            if (certRefs != null) {
                if (certRefs.length != 0) {
                    X509Certificate[] peerCertChain = new OpenSSLX509Certificate[certRefs.length];
                    for (int i = 0; i < certRefs.length; i++) {
                        peerCertChain[i] = new OpenSSLX509Certificate(certRefs[i]);
                    }
                    this.handshakeSession = new OpenSSLSessionImpl(sslSessionNativePtr, null, peerCertChain, getHostnameOrIP(), getPort(), null);
                    if (this.sslParameters.getUseClientMode()) {
                        Platform.checkServerTrusted(x509tm, peerCertChain, authMethod, this);
                        if (this.sslParameters.isCTVerificationEnabled(getHostname())) {
                            if (this.sslParameters.getCTVerifier().verifySignedCertificateTimestamps(peerCertChain, NativeCrypto.SSL_get_signed_cert_timestamp_list(this.sslNativePointer), NativeCrypto.SSL_get_ocsp_response(this.sslNativePointer)).getValidSCTs().size() == 0) {
                                throw new CertificateException("No valid SCT found");
                            }
                        }
                    }
                    Platform.checkClientTrusted(x509tm, peerCertChain, peerCertChain[0].getPublicKey().getAlgorithm(), this);
                    this.handshakeSession = null;
                    return;
                }
            }
            throw new SSLException("Peer sent no certificate");
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e2) {
            throw new CertificateException(e2);
        } catch (Throwable th) {
            this.handshakeSession = null;
        }
    }

    public InputStream getInputStream() throws IOException {
        InputStream returnVal;
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == STATE_CLOSED) {
                throw new SocketException("Socket is closed.");
            }
            if (this.is == null) {
                this.is = new SSLInputStream();
            }
            returnVal = this.is;
        }
        waitForHandshake();
        return returnVal;
    }

    public OutputStream getOutputStream() throws IOException {
        OutputStream returnVal;
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == STATE_CLOSED) {
                throw new SocketException("Socket is closed.");
            }
            if (this.os == null) {
                this.os = new SSLOutputStream();
            }
            returnVal = this.os;
        }
        waitForHandshake();
        return returnVal;
    }

    private void assertReadableOrWriteableState() {
        if (this.state != 4 && this.state != 3) {
            throw new AssertionError("Invalid state: " + this.state);
        }
    }

    private void waitForHandshake() throws IOException {
        startHandshake();
        synchronized (this.stateLock) {
            while (this.state != 4 && this.state != 3 && this.state != STATE_CLOSED) {
                try {
                    this.stateLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    IOException ioe = new IOException("Interrupted waiting for handshake");
                    ioe.initCause(e);
                    throw ioe;
                }
            }
            if (this.state == STATE_CLOSED) {
                throw new SocketException("Socket is closed");
            }
        }
    }

    public SSLSession getSession() {
        if (this.sslSession == null) {
            try {
                waitForHandshake();
            } catch (IOException e) {
                return SSLNullSession.getNullSession();
            }
        }
        return Platform.wrapSSLSession(this.sslSession);
    }

    public SSLSession getHandshakeSession() {
        return this.handshakeSession;
    }

    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        }
        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        } else if (this.listeners == null) {
            throw new IllegalArgumentException("Provided listener is not registered");
        } else if (!this.listeners.remove(listener)) {
            throw new IllegalArgumentException("Provided listener is not registered");
        }
    }

    public boolean getEnableSessionCreation() {
        return this.sslParameters.getEnableSessionCreation();
    }

    public void setEnableSessionCreation(boolean flag) {
        this.sslParameters.setEnableSessionCreation(flag);
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public String[] getEnabledCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public void setEnabledCipherSuites(String[] suites) {
        this.sslParameters.setEnabledCipherSuites(suites);
    }

    public String[] getSupportedProtocols() {
        return NativeCrypto.getSupportedProtocols();
    }

    public String[] getEnabledProtocols() {
        return this.sslParameters.getEnabledProtocols();
    }

    public void setEnabledProtocols(String[] protocols) {
        this.sslParameters.setEnabledProtocols(protocols);
    }

    public void setUseSessionTickets(boolean useSessionTickets) {
        this.sslParameters.useSessionTickets = useSessionTickets;
    }

    public void setHostname(String hostname) {
        this.sslParameters.setUseSni(hostname != null ? true : DBG_STATE);
        this.peerHostname = hostname;
    }

    public void setChannelIdEnabled(boolean enabled) {
        if (getUseClientMode()) {
            throw new IllegalStateException("Client mode");
        }
        synchronized (this.stateLock) {
            if (this.state != 0) {
                throw new IllegalStateException("Could not enable/disable Channel ID after the initial handshake has begun.");
            }
        }
        this.sslParameters.channelIdEnabled = enabled;
    }

    public byte[] getChannelId() throws SSLException {
        if (getUseClientMode()) {
            throw new IllegalStateException("Client mode");
        }
        synchronized (this.stateLock) {
            if (this.state != 4) {
                throw new IllegalStateException("Channel ID is only available after handshake completes");
            }
        }
        return NativeCrypto.SSL_get_tls_channel_id(this.sslNativePointer);
    }

    public void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.stateLock) {
                if (this.state != 0) {
                    throw new IllegalStateException("Could not change Channel ID private key after the initial handshake has begun.");
                }
            }
            if (privateKey == null) {
                this.sslParameters.channelIdEnabled = DBG_STATE;
                this.channelIdPrivateKey = null;
                return;
            }
            this.sslParameters.channelIdEnabled = true;
            ECParameterSpec ecParams = null;
            try {
                if (privateKey instanceof ECKey) {
                    ecParams = ((ECKey) privateKey).getParams();
                }
                if (ecParams == null) {
                    ecParams = OpenSSLECGroupContext.getCurveByName("prime256v1").getECParameterSpec();
                }
                this.channelIdPrivateKey = OpenSSLKey.fromECPrivateKeyForTLSStackOnly(privateKey, ecParams);
                return;
            } catch (InvalidKeyException e) {
                return;
            }
        }
        throw new IllegalStateException("Server mode");
    }

    public boolean getUseClientMode() {
        return this.sslParameters.getUseClientMode();
    }

    public void setUseClientMode(boolean mode) {
        synchronized (this.stateLock) {
            if (this.state != 0) {
                throw new IllegalArgumentException("Could not change the mode after the initial handshake has begun.");
            }
        }
        this.sslParameters.setUseClientMode(mode);
    }

    public boolean getWantClientAuth() {
        return this.sslParameters.getWantClientAuth();
    }

    public boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    public void setNeedClientAuth(boolean need) {
        this.sslParameters.setNeedClientAuth(need);
    }

    public void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public void sendUrgentData(int data) throws IOException {
        throw new SocketException("Method sendUrgentData() is not supported.");
    }

    public void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("Methods sendUrgentData, setOOBInline are not supported.");
    }

    public void setSoTimeout(int readTimeoutMilliseconds) throws SocketException {
        if (this.socket != this) {
            this.socket.setSoTimeout(readTimeoutMilliseconds);
        } else {
            super.setSoTimeout(readTimeoutMilliseconds);
        }
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
    }

    public int getSoTimeout() throws SocketException {
        return this.readTimeoutMilliseconds;
    }

    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        this.writeTimeoutMilliseconds = writeTimeoutMilliseconds;
        Platform.setSocketWriteTimeout(this, (long) writeTimeoutMilliseconds);
    }

    public int getSoWriteTimeout() throws SocketException {
        return this.writeTimeoutMilliseconds;
    }

    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        this.handshakeTimeoutMilliseconds = handshakeTimeoutMilliseconds;
    }

    /* JADX WARNING: Missing block: B:24:0x0039, code:
            if (r1 != null) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:25:0x003b, code:
            if (r2 == null) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:26:0x003d, code:
            com.android.org.conscrypt.NativeCrypto.SSL_interrupt(r8.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:27:0x0042, code:
            if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:28:0x0044, code:
            r1.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:29:0x0047, code:
            if (r2 == null) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:30:0x0049, code:
            r2.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:31:0x004c, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:32:0x004f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        synchronized (this.stateLock) {
            if (this.state == STATE_CLOSED) {
                return;
            }
            int oldState = this.state;
            this.state = STATE_CLOSED;
            if (oldState == 0) {
                closeUnderlyingSocket();
                this.stateLock.notifyAll();
            } else if (oldState == 4 || oldState == 3) {
                this.stateLock.notifyAll();
                SSLInputStream sslInputStream = this.is;
                SSLOutputStream sslOutputStream = this.os;
            } else {
                NativeCrypto.SSL_interrupt(this.sslNativePointer);
                this.stateLock.notifyAll();
            }
        }
    }

    private void shutdownAndFreeSslNative() throws IOException {
        try {
            Platform.blockGuardOnNetwork();
            NativeCrypto.SSL_shutdown(this.sslNativePointer, Platform.getFileDescriptor(this.socket), this);
        } catch (IOException e) {
        } finally {
            free();
            closeUnderlyingSocket();
        }
    }

    private void closeUnderlyingSocket() throws IOException {
        if (this.socket != this) {
            if (this.autoClose && !this.socket.isClosed()) {
                this.socket.close();
            }
        } else if (!super.isClosed()) {
            super.close();
        }
    }

    private void free() {
        if (this.sslNativePointer != 0) {
            NativeCrypto.SSL_free(this.sslNativePointer);
            this.sslNativePointer = 0;
            Platform.closeGuardClose(this.guard);
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                Platform.closeGuardWarnIfOpen(this.guard);
            }
            free();
        } finally {
            super.finalize();
        }
    }

    public FileDescriptor getFileDescriptor$() {
        if (this.socket == this) {
            return Platform.getFileDescriptorFromSSLSocket(this);
        }
        return Platform.getFileDescriptor(this.socket);
    }

    public byte[] getNpnSelectedProtocol() {
        return NativeCrypto.SSL_get_npn_negotiated_protocol(this.sslNativePointer);
    }

    public byte[] getAlpnSelectedProtocol() {
        return NativeCrypto.SSL_get0_alpn_selected(this.sslNativePointer);
    }

    public void setNpnProtocols(byte[] npnProtocols) {
        if (npnProtocols == null || npnProtocols.length != 0) {
            this.sslParameters.npnProtocols = npnProtocols;
            return;
        }
        throw new IllegalArgumentException("npnProtocols.length == 0");
    }

    public void setAlpnProtocols(byte[] alpnProtocols) {
        if (alpnProtocols == null || alpnProtocols.length != 0) {
            this.sslParameters.alpnProtocols = alpnProtocols;
            return;
        }
        throw new IllegalArgumentException("alpnProtocols.length == 0");
    }

    public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        Platform.getSSLParameters(params, this.sslParameters, this);
        return params;
    }

    public void setSSLParameters(SSLParameters p) {
        super.setSSLParameters(p);
        Platform.setSSLParameters(p, this.sslParameters, this);
    }

    public String chooseServerAlias(X509KeyManager keyManager, String keyType) {
        return keyManager.chooseServerAlias(keyType, null, this);
    }

    public String chooseClientAlias(X509KeyManager keyManager, X500Principal[] issuers, String[] keyTypes) {
        return keyManager.chooseClientAlias(keyTypes, null, this);
    }

    public String chooseServerPSKIdentityHint(PSKKeyManager keyManager) {
        return keyManager.chooseServerKeyIdentityHint((Socket) this);
    }

    public String chooseClientPSKIdentity(PSKKeyManager keyManager, String identityHint) {
        return keyManager.chooseClientKeyIdentity(identityHint, (Socket) this);
    }

    public SecretKey getPSKKey(PSKKeyManager keyManager, String identityHint, String identity) {
        return keyManager.getKey(identityHint, identity, (Socket) this);
    }
}
