package com.mediatek.socket.base;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import com.mediatek.socket.base.SocketUtils.BaseBuffer;
import com.mediatek.socket.base.SocketUtils.UdpServerInterface;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer implements UdpServerInterface {
    private BaseBuffer mBuff;
    private String mChannelName;
    private DataInputStream mIn;
    private boolean mIsLocalSocket = false;
    private LocalSocket mLocalSocket;
    private Namespace mNamespace;
    private DatagramSocket mNetSocket;
    private DatagramPacket mPacket;
    private int mPort;

    public UdpServer(int port, int recvBuffSize) {
        this.mBuff = new BaseBuffer(recvBuffSize);
        this.mPort = port;
        if (!bind()) {
            throw new RuntimeException("bind() fail");
        }
    }

    public UdpServer(String channelName, Namespace namespace, int recvBuffSize) {
        this.mBuff = new BaseBuffer(recvBuffSize);
        this.mChannelName = channelName;
        this.mNamespace = namespace;
        if (!bind()) {
            throw new RuntimeException("bind() fail");
        }
    }

    /* JADX WARNING: Missing block: B:13:0x003c, code:
            r2 = r2 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean bind() {
        int i = 0;
        while (i < 5) {
            if (this.mIsLocalSocket) {
                try {
                    this.mLocalSocket = new LocalSocket(1);
                    this.mLocalSocket.bind(new LocalSocketAddress(this.mChannelName, this.mNamespace));
                    this.mIn = new DataInputStream(this.mLocalSocket.getInputStream());
                    return true;
                } catch (IOException e) {
                    if (i == 4) {
                        throw new RuntimeException(e);
                    }
                    msleep(200);
                }
            } else {
                try {
                    this.mNetSocket = new DatagramSocket(this.mPort);
                    this.mPacket = new DatagramPacket(this.mBuff.getBuff(), this.mBuff.getBuff().length);
                    return true;
                } catch (SocketException e2) {
                    msleep(200);
                    if (i == 4) {
                        throw new RuntimeException(e2);
                    }
                }
            }
        }
        return false;
    }

    public boolean read() {
        this.mBuff.clear();
        if (this.mIsLocalSocket) {
            try {
                return this.mIn.read(this.mBuff.getBuff()) >= 8;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            this.mNetSocket.receive(this.mPacket);
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public BaseBuffer getBuff() {
        return this.mBuff;
    }

    public void close() {
        if (this.mIsLocalSocket) {
            try {
                UdpClient client = new UdpClient(this.mChannelName, this.mNamespace, 128);
                client.connect();
                client.getBuff().putInt(-1);
                client.write();
                client.close();
                this.mLocalSocket.close();
                this.mIn.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        this.mNetSocket.close();
    }

    public int available() {
        if (this.mIsLocalSocket) {
            try {
                return this.mIn.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                throw new RuntimeException("Network Type does not support available() API");
            } catch (Exception e2) {
                e2.printStackTrace();
                return -1;
            }
        }
    }

    public boolean setSoTimeout(int timeout) {
        if (this.mIsLocalSocket) {
            try {
                this.mLocalSocket.setSoTimeout(timeout);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            this.mNetSocket.setSoTimeout(timeout);
            return true;
        } catch (SocketException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private void msleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
