package com.mediatek.socket.base;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import com.mediatek.socket.base.SocketUtils.BaseBuffer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient {
    private BaseBuffer mBuff;
    private String mChannelName;
    private String mHost;
    private InetAddress mInetAddress;
    private boolean mIsLocalSocket = false;
    private LocalSocket mLocalSocket;
    private Namespace mNamespace;
    private DatagramSocket mNetSocket;
    private DataOutputStream mOut;
    private DatagramPacket mPacket;
    private int mPort;

    public UdpClient(String host, int port, int sendBuffSize) {
        this.mBuff = new BaseBuffer(sendBuffSize);
        this.mHost = host;
        this.mPort = port;
    }

    public UdpClient(String channelName, Namespace namesapce, int sendBuffSize) {
        this.mBuff = new BaseBuffer(sendBuffSize);
        this.mChannelName = channelName;
        this.mNamespace = namesapce;
    }

    public boolean connect() {
        if (this.mIsLocalSocket) {
            try {
                this.mLocalSocket = new LocalSocket(1);
                this.mLocalSocket.connect(new LocalSocketAddress(this.mChannelName, this.mNamespace));
                this.mOut = new DataOutputStream(this.mLocalSocket.getOutputStream());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            this.mNetSocket = new DatagramSocket();
            if (this.mInetAddress == null) {
                this.mInetAddress = InetAddress.getByName(this.mHost);
            }
            this.mPacket = new DatagramPacket(this.mBuff.getBuff(), this.mBuff.getBuff().length, this.mInetAddress, this.mPort);
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public BaseBuffer getBuff() {
        return this.mBuff;
    }

    public boolean write() {
        if (this.mIsLocalSocket) {
            try {
                this.mOut.write(this.mBuff.getBuff(), 0, this.mBuff.getOffset());
                this.mBuff.setOffset(0);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            this.mPacket.setLength(this.mBuff.getOffset());
            this.mNetSocket.send(this.mPacket);
            this.mBuff.setOffset(0);
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (this.mIsLocalSocket) {
            try {
                if (this.mLocalSocket != null) {
                    this.mLocalSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.mNetSocket != null) {
            this.mNetSocket.close();
        }
    }
}
