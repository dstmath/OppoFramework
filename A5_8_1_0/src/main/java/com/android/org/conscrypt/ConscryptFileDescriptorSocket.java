package com.android.org.conscrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

final class ConscryptFileDescriptorSocket extends OpenSSLSocketImpl implements SSLHandshakeCallbacks, AliasChooser, PSKCallbacks {
    private static final boolean DBG_STATE = false;
    private OpenSSLKey channelIdPrivateKey;
    private final Object guard = Platform.closeGuardGet();
    private int handshakeTimeoutMilliseconds = -1;
    private SSLInputStream is;
    private SSLOutputStream os;
    private final SslWrapper ssl;
    private final SSLParametersImpl sslParameters;
    private final ActiveSession sslSession;
    private int state = 0;
    private final Object stateLock = new Object();
    private int writeTimeoutMilliseconds = 0;

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
            ConscryptFileDescriptorSocket.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount == 0) {
                return 0;
            }
            int ret;
            synchronized (this.readLock) {
                synchronized (ConscryptFileDescriptorSocket.this.stateLock) {
                    if (ConscryptFileDescriptorSocket.this.state == 8) {
                        throw new SocketException("socket is closed");
                    }
                }
                ret = ConscryptFileDescriptorSocket.this.ssl.read(Platform.getFileDescriptor(ConscryptFileDescriptorSocket.this.socket), buf, offset, byteCount, ConscryptFileDescriptorSocket.this.getSoTimeout());
                if (ret == -1) {
                    synchronized (ConscryptFileDescriptorSocket.this.stateLock) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                }
            }
            return ret;
        }

        void awaitPendingOps() {
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
            ConscryptFileDescriptorSocket.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount != 0) {
                synchronized (this.writeLock) {
                    synchronized (ConscryptFileDescriptorSocket.this.stateLock) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                    ConscryptFileDescriptorSocket.this.ssl.write(Platform.getFileDescriptor(ConscryptFileDescriptorSocket.this.socket), buf, offset, byteCount, ConscryptFileDescriptorSocket.this.writeTimeoutMilliseconds);
                    synchronized (ConscryptFileDescriptorSocket.this.stateLock) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                }
            }
        }

        void awaitPendingOps() {
            synchronized (this.writeLock) {
            }
        }
    }

    ConscryptFileDescriptorSocket(SSLParametersImpl sslParameters) throws IOException {
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    ConscryptFileDescriptorSocket(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port);
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    ConscryptFileDescriptorSocket(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        super(address, port);
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    ConscryptFileDescriptorSocket(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    ConscryptFileDescriptorSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    ConscryptFileDescriptorSocket(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        super(socket, hostname, port, autoClose);
        this.sslParameters = sslParameters;
        this.ssl = newSsl(sslParameters, this);
        this.sslSession = new ActiveSession(this.ssl, sslParameters.getSessionContext());
    }

    private static SslWrapper newSsl(SSLParametersImpl sslParameters, ConscryptFileDescriptorSocket engine) {
        try {
            return SslWrapper.newInstance(sslParameters, engine, engine, engine);
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000e, code:
            r7 = true;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            com.android.org.conscrypt.Platform.closeGuardOpen(r15.guard, "close");
            r15.ssl.initialize(getHostname(), r15.channelIdPrivateKey);
     */
    /* JADX WARNING: Missing block: B:10:0x0026, code:
            if (getUseClientMode() == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:11:0x0028, code:
            r0 = clientSessionContext().getCachedSession(getHostnameOrIP(), getPort(), r15.sslParameters);
     */
    /* JADX WARNING: Missing block: B:12:0x003a, code:
            if (r0 == null) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:13:0x003c, code:
            r0.offerToResume(r15.ssl);
     */
    /* JADX WARNING: Missing block: B:14:0x0041, code:
            r8 = getSoTimeout();
            r9 = getSoWriteTimeout();
     */
    /* JADX WARNING: Missing block: B:15:0x004b, code:
            if (r15.handshakeTimeoutMilliseconds < 0) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:16:0x004d, code:
            setSoTimeout(r15.handshakeTimeoutMilliseconds);
            setSoWriteTimeout(r15.handshakeTimeoutMilliseconds);
     */
    /* JADX WARNING: Missing block: B:17:0x0057, code:
            r11 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:18:0x0059, code:
            monitor-enter(r11);
     */
    /* JADX WARNING: Missing block: B:22:0x005e, code:
            if (r15.state != 8) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:25:0x0061, code:
            if (1 == null) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:26:0x0063, code:
            r12 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:27:0x0065, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r15.state = 8;
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:31:0x006f, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            r15.ssl.doHandshake(com.android.org.conscrypt.Platform.getFileDescriptor(r15.socket), getSoTimeout());
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r11 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:50:0x0090, code:
            monitor-enter(r11);
     */
    /* JADX WARNING: Missing block: B:54:0x0095, code:
            if (r15.state != 8) goto L_0x012f;
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:57:0x0098, code:
            if (1 == null) goto L_0x00aa;
     */
    /* JADX WARNING: Missing block: B:58:0x009a, code:
            r12 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:59:0x009c, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            r15.state = 8;
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:63:0x00a6, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:65:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:71:0x00ae, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:74:0x00bd, code:
            throw ((javax.net.ssl.SSLHandshakeException) new javax.net.ssl.SSLHandshakeException("Handshake failed").initCause(r3));
     */
    /* JADX WARNING: Missing block: B:76:0x00bf, code:
            if (r7 != false) goto L_0x00c1;
     */
    /* JADX WARNING: Missing block: B:78:0x00c3, code:
            monitor-enter(r15.stateLock);
     */
    /* JADX WARNING: Missing block: B:81:?, code:
            r15.state = 8;
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:84:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:86:0x00d2, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:89:0x00d5, code:
            monitor-enter(r15.stateLock);
     */
    /* JADX WARNING: Missing block: B:93:0x00da, code:
            if (r15.state == 8) goto L_0x00dc;
     */
    /* JADX WARNING: Missing block: B:96:0x00dd, code:
            if (1 != null) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:98:0x00e1, code:
            monitor-enter(r15.stateLock);
     */
    /* JADX WARNING: Missing block: B:101:?, code:
            r15.state = 8;
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:104:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:105:0x00ef, code:
            return;
     */
    /* JADX WARNING: Missing block: B:113:0x0101, code:
            if (r2.getMessage().contains("unexpected CCS") != false) goto L_0x0103;
     */
    /* JADX WARNING: Missing block: B:114:0x0103, code:
            com.android.org.conscrypt.Platform.logEvent(java.lang.String.format("ssl_unexpected_ccs: host=%s", new java.lang.Object[]{getHostnameOrIP()}));
     */
    /* JADX WARNING: Missing block: B:115:0x0117, code:
            throw r2;
     */
    /* JADX WARNING: Missing block: B:119:0x011b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:120:0x011c, code:
            r10 = new javax.net.ssl.SSLHandshakeException(r1.getMessage());
            r10.initCause(r1);
     */
    /* JADX WARNING: Missing block: B:121:0x0128, code:
            throw r10;
     */
    /* JADX WARNING: Missing block: B:127:?, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:129:0x0132, code:
            if (r15.handshakeTimeoutMilliseconds < 0) goto L_0x013a;
     */
    /* JADX WARNING: Missing block: B:130:0x0134, code:
            setSoTimeout(r8);
            setSoWriteTimeout(r9);
     */
    /* JADX WARNING: Missing block: B:131:0x013a, code:
            r12 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:132:0x013c, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:135:0x0141, code:
            if (r15.state != 8) goto L_0x016a;
     */
    /* JADX WARNING: Missing block: B:136:0x0143, code:
            r7 = true;
     */
    /* JADX WARNING: Missing block: B:138:0x0147, code:
            if (r15.state != 2) goto L_0x016c;
     */
    /* JADX WARNING: Missing block: B:139:0x0149, code:
            r15.state = 4;
     */
    /* JADX WARNING: Missing block: B:140:0x014c, code:
            if (r7 != false) goto L_0x0153;
     */
    /* JADX WARNING: Missing block: B:141:0x014e, code:
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:143:?, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:144:0x0154, code:
            if (r7 == false) goto L_0x0166;
     */
    /* JADX WARNING: Missing block: B:145:0x0156, code:
            r12 = r15.stateLock;
     */
    /* JADX WARNING: Missing block: B:146:0x0158, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:149:?, code:
            r15.state = 8;
            r15.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:150:0x0162, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:152:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:158:0x016a, code:
            r7 = false;
     */
    /* JADX WARNING: Missing block: B:161:?, code:
            r15.state = 5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startHandshake() throws IOException {
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 0) {
                this.state = 2;
            }
        }
    }

    public void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.ssl.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals);
    }

    public int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.ssl.clientPSKKeyRequested(identityHint, identity, key);
    }

    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.ssl.serverPSKKeyRequested(identityHint, identity, key);
    }

    public void onSSLStateChange(int type, int val) {
        if (type == 32) {
            this.sslSession.onSessionEstablished(getHostnameOrIP(), getPort());
            synchronized (this.stateLock) {
                if (this.state == 8) {
                    return;
                }
                this.state = 5;
                notifyHandshakeCompletedListeners();
                synchronized (this.stateLock) {
                    this.stateLock.notifyAll();
                }
            }
        }
    }

    public void onNewSessionEstablished(long sslSessionNativePtr) {
        try {
            NativeCrypto.SSL_SESSION_up_ref(sslSessionNativePtr);
            sessionContext().cacheSession(SslSessionWrapper.newInstance(new SSL_SESSION(sslSessionNativePtr), this.sslSession));
        } catch (Exception e) {
        }
    }

    public long serverSessionRequested(byte[] id) {
        return 0;
    }

    public void verifyCertificateChain(long[] certRefs, String authMethod) throws CertificateException {
        try {
            X509TrustManager x509tm = this.sslParameters.getX509TrustManager();
            if (x509tm == null) {
                throw new CertificateException("No X.509 TrustManager");
            }
            if (certRefs != null) {
                if (certRefs.length != 0) {
                    X509Certificate[] peerCertChain = OpenSSLX509Certificate.createCertChain(certRefs);
                    this.sslSession.onPeerCertificatesReceived(getHostnameOrIP(), getPort(), peerCertChain);
                    if (getUseClientMode()) {
                        Platform.checkServerTrusted(x509tm, peerCertChain, authMethod, (AbstractConscryptSocket) this);
                        return;
                    } else {
                        Platform.checkClientTrusted(x509tm, peerCertChain, peerCertChain[0].getPublicKey().getAlgorithm(), (AbstractConscryptSocket) this);
                        return;
                    }
                }
            }
            throw new SSLException("Peer sent no certificate");
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e2) {
            throw new CertificateException(e2);
        }
    }

    public InputStream getInputStream() throws IOException {
        InputStream returnVal;
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 8) {
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
            if (this.state == 8) {
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
        if (this.state != 5 && this.state != 4) {
            throw new AssertionError("Invalid state: " + this.state);
        }
    }

    private void waitForHandshake() throws IOException {
        startHandshake();
        synchronized (this.stateLock) {
            while (this.state != 5 && this.state != 4 && this.state != 8) {
                try {
                    this.stateLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted waiting for handshake", e);
                }
            }
            if (this.state == 8) {
                throw new SocketException("Socket is closed");
            }
        }
    }

    public SSLSession getSession() {
        boolean handshakeCompleted = false;
        synchronized (this.stateLock) {
            try {
                handshakeCompleted = this.state >= 5;
                if (!handshakeCompleted && isConnected()) {
                    waitForHandshake();
                    handshakeCompleted = true;
                }
            } catch (IOException e) {
            }
        }
        if (handshakeCompleted) {
            return Platform.wrapSSLSession(this.sslSession);
        }
        return SSLNullSession.getNullSession();
    }

    SSLSession getActiveSession() {
        return this.sslSession;
    }

    public SSLSession getHandshakeSession() {
        SSLSession sSLSession;
        synchronized (this.stateLock) {
            sSLSession = (this.state < 2 || this.state >= 5) ? null : this.sslSession;
        }
        return sSLSession;
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
        this.sslParameters.setUseSessionTickets(useSessionTickets);
    }

    public void setHostname(String hostname) {
        this.sslParameters.setUseSni(hostname != null);
        super.setHostname(hostname);
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
            if (this.state != 5) {
                throw new IllegalStateException("Channel ID is only available after handshake completes");
            }
        }
        return this.ssl.getTlsChannelId();
    }

    public void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.stateLock) {
                if (this.state != 0) {
                    throw new IllegalStateException("Could not change Channel ID private key after the initial handshake has begun.");
                }
            }
            if (privateKey == null) {
                this.sslParameters.channelIdEnabled = false;
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

    /* JADX WARNING: Missing block: B:24:0x003c, code:
            if (r1 != null) goto L_0x0040;
     */
    /* JADX WARNING: Missing block: B:25:0x003e, code:
            if (r2 == null) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:26:0x0040, code:
            r6.ssl.interrupt();
     */
    /* JADX WARNING: Missing block: B:27:0x0045, code:
            if (r1 == null) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:28:0x0047, code:
            r1.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:29:0x004a, code:
            if (r2 == null) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:30:0x004c, code:
            r2.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:31:0x004f, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:32:0x0052, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        synchronized (this.stateLock) {
            if (this.state == 8) {
                return;
            }
            int oldState = this.state;
            this.state = 8;
            if (oldState == 0) {
                free();
                closeUnderlyingSocket();
                this.stateLock.notifyAll();
            } else if (oldState == 5 || oldState == 4) {
                this.stateLock.notifyAll();
                SSLInputStream sslInputStream = this.is;
                SSLOutputStream sslOutputStream = this.os;
            } else {
                this.ssl.interrupt();
                this.stateLock.notifyAll();
            }
        }
    }

    private void shutdownAndFreeSslNative() throws IOException {
        try {
            Platform.blockGuardOnNetwork();
            this.ssl.shutdown(Platform.getFileDescriptor(this.socket));
        } catch (IOException e) {
        } finally {
            free();
            closeUnderlyingSocket();
        }
    }

    private void closeUnderlyingSocket() throws IOException {
        super.close();
    }

    private void free() {
        if (!this.ssl.isClosed()) {
            this.ssl.close();
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

    public byte[] getAlpnSelectedProtocol() {
        return this.ssl.getAlpnSelectedProtocol();
    }

    public void setAlpnProtocols(String[] alpnProtocols) {
        this.sslParameters.setAlpnProtocols(alpnProtocols);
    }

    public void setAlpnProtocols(byte[] alpnProtocols) {
        this.sslParameters.setAlpnProtocols(alpnProtocols);
    }

    public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        Platform.getSSLParameters(params, this.sslParameters, (AbstractConscryptSocket) this);
        return params;
    }

    public void setSSLParameters(SSLParameters p) {
        super.setSSLParameters(p);
        Platform.setSSLParameters(p, this.sslParameters, (AbstractConscryptSocket) this);
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

    private ClientSessionContext clientSessionContext() {
        return this.sslParameters.getClientSessionContext();
    }

    private AbstractSessionContext sessionContext() {
        return this.sslParameters.getSessionContext();
    }
}
