package com.android.org.conscrypt;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.PrivateKey;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public abstract class OpenSSLSocketImpl extends AbstractConscryptSocket {
    public abstract byte[] getAlpnSelectedProtocol();

    public abstract byte[] getChannelId() throws SSLException;

    public abstract SSLSession getHandshakeSession();

    public abstract void setAlpnProtocols(byte[] bArr);

    public abstract void setAlpnProtocols(String[] strArr);

    public abstract void setChannelIdEnabled(boolean z);

    public abstract void setChannelIdPrivateKey(PrivateKey privateKey);

    public abstract void setUseSessionTickets(boolean z);

    OpenSSLSocketImpl() throws IOException {
    }

    OpenSSLSocketImpl(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    OpenSSLSocketImpl(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    OpenSSLSocketImpl(String hostname, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(hostname, port, clientAddress, clientPort);
    }

    OpenSSLSocketImpl(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    OpenSSLSocketImpl(Socket socket, String hostname, int port, boolean autoClose) throws IOException {
        super(socket, hostname, port, autoClose);
    }

    public String getHostname() {
        return super.getHostname();
    }

    public void setHostname(String hostname) {
        super.setHostname(hostname);
    }

    public String getHostnameOrIP() {
        return super.getHostnameOrIP();
    }

    public FileDescriptor getFileDescriptor$() {
        return super.getFileDescriptor$();
    }

    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        super.setSoWriteTimeout(writeTimeoutMilliseconds);
    }

    public int getSoWriteTimeout() throws SocketException {
        return super.getSoWriteTimeout();
    }

    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        super.setHandshakeTimeout(handshakeTimeoutMilliseconds);
    }

    public final byte[] getNpnSelectedProtocol() {
        return super.getNpnSelectedProtocol();
    }

    public final void setNpnProtocols(byte[] npnProtocols) {
        super.setNpnProtocols(npnProtocols);
    }
}
