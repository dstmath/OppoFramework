package com.mediatek.internal.telephony.cat;

import android.net.Network;
import com.mediatek.internal.telephony.cat.Channel;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/* compiled from: Channel */
class UdpChannel extends Channel {
    private static final int UDP_SOCKET_TIMEOUT = 3000;
    DatagramSocket mSocket = null;
    Thread rt = null;

    UdpChannel(int cid, int linkMode, int protocolType, InetAddress address, int port, int bufferSize, MtkCatService handler, BipService bipManager) {
        super(cid, linkMode, protocolType, address, port, bufferSize, handler, bipManager);
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int openChannel(BipCmdMessage cmdMsg, Network network) {
        int ret = 0;
        this.mNetwork = network;
        if (this.mLinkMode == 0) {
            try {
                this.mSocket = new DatagramSocket();
                this.mNetwork.bindSocket(this.mSocket);
                this.mChannelStatus = 4;
                this.mChannelStatusData.mChannelStatus = 128;
                this.rt = new Thread(new Channel.UdpReceiverThread(this.mSocket));
                this.rt.start();
                MtkCatLog.d("[BIP]", "[UDP]: sock status:" + this.mChannelStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ret = checkBufferSize();
            if (ret == 3) {
                MtkCatLog.d("[BIP]", "[UDP]openChannel: buffer size is modified");
                cmdMsg.mBufferSize = this.mBufferSize;
            }
            this.mRxBuffer = new byte[this.mBufferSize];
            this.mTxBuffer = new byte[this.mBufferSize];
        }
        return ret;
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int closeChannel() {
        MtkCatLog.d("[BIP]", "[UDP]closeChannel.");
        if (this.rt != null) {
            requestStop();
            this.rt = null;
        }
        if (this.mSocket != null) {
            MtkCatLog.d("[BIP]", "[UDP]closeSocket.");
            this.mSocket.close();
            this.mChannelStatus = 2;
            this.mSocket = null;
            this.mRxBuffer = null;
            this.mTxBuffer = null;
        }
        return 0;
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public ReceiveDataResult receiveData(int requestCount) {
        ReceiveDataResult ret = new ReceiveDataResult();
        ret.buffer = new byte[requestCount];
        MtkCatLog.d("[BIP]", "[UDP]receiveData " + this.mRxBufferCount + "/" + requestCount + "/" + this.mRxBufferOffset);
        if (this.mRxBufferCount >= requestCount) {
            try {
                System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, ret.buffer, 0, requestCount);
                this.mRxBufferOffset += requestCount;
                this.mRxBufferCount -= requestCount;
                ret.remainingCount = this.mRxBufferCount;
            } catch (IndexOutOfBoundsException e) {
            }
        } else {
            int needCopy = requestCount;
            int canCopy = this.mRxBufferCount;
            int countCopied = 0;
            boolean canExitLoop = false;
            while (!canExitLoop) {
                if (needCopy > canCopy) {
                    try {
                        System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, ret.buffer, countCopied, canCopy);
                        countCopied += canCopy;
                        needCopy -= canCopy;
                        this.mRxBufferOffset += canCopy;
                        this.mRxBufferCount -= canCopy;
                    } catch (IndexOutOfBoundsException e2) {
                    }
                } else {
                    try {
                        System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, ret.buffer, countCopied, needCopy);
                        this.mRxBufferOffset += needCopy;
                        this.mRxBufferCount -= needCopy;
                        countCopied += needCopy;
                        needCopy = 0;
                    } catch (IndexOutOfBoundsException e3) {
                    }
                }
                if (needCopy == 0) {
                    canExitLoop = true;
                } else {
                    try {
                        this.mSocket.setSoTimeout(UDP_SOCKET_TIMEOUT);
                        DatagramPacket packet = new DatagramPacket(this.mRxBuffer, this.mRxBuffer.length);
                        this.mSocket.receive(packet);
                        this.mRxBufferOffset = 0;
                        this.mRxBufferCount = packet.getLength();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
        return ret;
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int sendData(byte[] data, int mode) {
        byte[] tmpBuffer;
        if (data == null) {
            MtkCatLog.e("[BIP]", "[UDP]sendData - data null:");
            return 5;
        } else if (this.mTxBuffer == null) {
            MtkCatLog.e("[BIP]", "[UDP]sendData - mTxBuffer null:");
            return 5;
        } else {
            int txRemaining = this.mTxBuffer.length - this.mTxBufferCount;
            MtkCatLog.d("[BIP]", "[UDP]sendData: size of data:" + data.length + " mode:" + mode);
            MtkCatLog.d("[BIP]", "[UDP]sendData: size of buffer:" + this.mTxBuffer.length + " count:" + this.mTxBufferCount);
            try {
                if (this.mTxBufferCount == 0 && 1 == mode) {
                    tmpBuffer = data;
                    this.mTxBufferCount = data.length;
                } else {
                    if (txRemaining >= data.length) {
                        try {
                            System.arraycopy(data, 0, this.mTxBuffer, this.mTxBufferCount, data.length);
                            this.mTxBufferCount += data.length;
                        } catch (IndexOutOfBoundsException e) {
                            MtkCatLog.e("[BIP]", "[UDP]sendData - IndexOutOfBoundsException");
                        }
                    } else {
                        MtkCatLog.d("[BIP]", "[UDP]sendData - tx buffer is not enough:" + txRemaining);
                    }
                    tmpBuffer = this.mTxBuffer;
                }
                if (mode != 1) {
                    return 0;
                }
                MtkCatLog.d("[BIP]", "[UDP]Send data(" + this.mAddress + ":" + this.mPort + "):" + this.mTxBuffer.length + " count:" + this.mTxBufferCount);
                DatagramPacket packet = new DatagramPacket(tmpBuffer, 0, this.mTxBufferCount, this.mAddress, this.mPort);
                if (this.mSocket == null) {
                    return 0;
                }
                try {
                    this.mSocket.send(packet);
                    this.mTxBufferCount = 0;
                    return 0;
                } catch (Exception e2) {
                    MtkCatLog.e("[BIP]", "[UDP]sendData - Exception");
                    this.mChannelStatusData.mChannelStatus = 0;
                    e2.printStackTrace();
                    return 5;
                }
            } catch (NullPointerException ne) {
                MtkCatLog.d("[BIP]", "[UDP]sendData NE");
                ne.printStackTrace();
                return 5;
            }
        }
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int getTxAvailBufferSize() {
        if (this.mTxBuffer == null) {
            MtkCatLog.e("[BIP]", "[UDP]getTxAvailBufferSize - mTxBuffer null:");
            return 0;
        }
        int txRemaining = this.mTxBuffer.length - this.mTxBufferCount;
        MtkCatLog.d("[BIP]", "[UDP]available tx buffer size:" + txRemaining);
        return txRemaining;
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int receiveData(int requestSize, ReceiveDataResult rdr) {
        if (rdr == null) {
            MtkCatLog.e("[BIP]", "[UDP]rdr is null");
            return 5;
        }
        MtkCatLog.d("[BIP]", "[UDP]receiveData mRxBufferCount:" + this.mRxBufferCount + " requestSize: " + requestSize + " mRxBufferOffset:" + this.mRxBufferOffset);
        rdr.buffer = new byte[requestSize];
        if (this.mRxBufferCount >= requestSize) {
            try {
                synchronized (this.mLock) {
                    System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, rdr.buffer, 0, requestSize);
                    this.mRxBufferOffset += requestSize;
                    this.mRxBufferCount -= requestSize;
                    if (this.mRxBufferCount == 0) {
                        this.mRxBufferOffset = 0;
                    }
                    rdr.remainingCount = this.mRxBufferCount;
                }
                return 0;
            } catch (IndexOutOfBoundsException e) {
                MtkCatLog.e("[BIP]", "[UDP]fail copy rx buffer out 1");
                return 5;
            }
        } else {
            MtkCatLog.e("[BIP]", "[UDP]rx buffer is insufficient !!!");
            try {
                synchronized (this.mLock) {
                    System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, rdr.buffer, 0, this.mRxBufferCount);
                    this.mRxBufferOffset = 0;
                    this.mRxBufferCount = 0;
                    this.mLock.notify();
                }
                rdr.remainingCount = 0;
                return 9;
            } catch (IndexOutOfBoundsException e2) {
                MtkCatLog.e("[BIP]", "[UDP]fail copy rx buffer out 2");
                return 5;
            }
        }
    }
}
