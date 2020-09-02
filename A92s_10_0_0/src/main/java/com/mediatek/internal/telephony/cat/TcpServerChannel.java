package com.mediatek.internal.telephony.cat;

import android.net.Network;
import com.mediatek.internal.telephony.cat.Channel;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

/* compiled from: Channel */
class TcpServerChannel extends Channel {
    private boolean mCloseBackToTcpListen = false;
    protected DataInputStream mInput = null;
    protected BufferedOutputStream mOutput = null;
    protected ServerSocket mSSocket = null;
    protected Socket mSocket = null;
    private Thread rt = null;

    TcpServerChannel(int cid, int linkMode, int protocolType, int port, int bufferSize, MtkCatService handler, BipService bipManager) {
        super(cid, linkMode, protocolType, null, port, bufferSize, handler, bipManager);
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int openChannel(BipCmdMessage cmdMsg, Network network) {
        this.mNetwork = network;
        MtkCatLog.d("[BIP]", "[UICC]openChannel mLinkMode:" + this.mLinkMode);
        try {
            MtkCatLog.d("[BIP]", "[UICC]New server socket.mChannelStatus:" + this.mChannelStatus + ",port:" + this.mPort);
            this.mSSocket = new ServerSocket(this.mPort, 0, Inet4Address.LOOPBACK);
            if (this.mChannelStatus == 0 || this.mChannelStatus == 2) {
                setTcpStatus(BipUtils.TCP_STATUS_LISTEN, false);
                this.mChannelStatus = 4;
                this.rt = new Thread(new Channel.UICCServerThread(this));
                this.rt.start();
            }
            int ret = checkBufferSize();
            if (ret == 3) {
                MtkCatLog.d("[BIP]", "[UICC]openChannel: buffer size is modified");
                cmdMsg.mBufferSize = this.mBufferSize;
            }
            cmdMsg.mChannelStatusData.mChannelStatus = getTcpStatus();
            this.mRxBuffer = new byte[this.mBufferSize];
            this.mTxBuffer = new byte[this.mBufferSize];
            return ret;
        } catch (IOException e) {
            MtkCatLog.d("[BIP]", "[UICC]IOEX to create server socket");
            return 5;
        } catch (Exception e2) {
            MtkCatLog.d("[BIP]", "[UICC]EX to create server socket " + e2);
            return 5;
        }
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int closeChannel() {
        MtkCatLog.d("[BIP]", "[UICC]closeChannel.");
        if (true != this.mCloseBackToTcpListen) {
            if (this.rt != null) {
                requestStop();
                this.rt = null;
            }
            try {
                if (this.mInput != null) {
                    this.mInput.close();
                }
                if (this.mOutput != null) {
                    this.mOutput.close();
                }
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
                if (this.mSSocket != null) {
                    this.mSSocket.close();
                }
            } catch (IOException e) {
                MtkCatLog.e("[BIP]", "[UICC]IOEX closeChannel");
            } catch (Throwable th) {
                this.mSocket = null;
                this.mRxBuffer = null;
                this.mTxBuffer = null;
                throw th;
            }
            this.mSocket = null;
            this.mRxBuffer = null;
            this.mTxBuffer = null;
        } else if (-128 == this.mChannelStatusData.mChannelStatus) {
            try {
                this.mChannelStatusData.mChannelStatus = 64;
                if (this.mInput != null) {
                    this.mInput.close();
                }
                if (this.mOutput != null) {
                    this.mOutput.close();
                }
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
                this.rt.interrupt();
            } catch (IOException e2) {
                MtkCatLog.e("[BIP]", "[UICC]IOEX closeChannel back to tcp listen.");
            } catch (Throwable th2) {
                this.mSocket = null;
                this.mRxBuffer = null;
                this.mTxBuffer = null;
                throw th2;
            }
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
        MtkCatLog.d("[BIP]", "[UICC]receiveData " + this.mRxBufferCount + "/" + requestCount + "/" + this.mRxBufferOffset);
        if (this.mRxBufferCount >= requestCount) {
            try {
                MtkCatLog.d("[BIP]", "[UICC]Start to copy data from buffer");
                System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, ret.buffer, 0, requestCount);
                this.mRxBufferCount -= requestCount;
                this.mRxBufferOffset += requestCount;
                ret.remainingCount = this.mRxBufferCount;
            } catch (IndexOutOfBoundsException e) {
                MtkCatLog.e("[BIP]", "IOOB-1");
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
                        this.mRxBufferOffset += canCopy;
                        this.mRxBufferCount -= canCopy;
                        countCopied += canCopy;
                        needCopy -= canCopy;
                    } catch (IndexOutOfBoundsException e2) {
                        MtkCatLog.e("[BIP]", "IOOB-2");
                    }
                } else {
                    try {
                        System.arraycopy(Integer.valueOf(this.mRxBufferCount), this.mRxBufferOffset, ret.buffer, countCopied, canCopy);
                        this.mRxBufferOffset += needCopy;
                        countCopied += needCopy;
                        needCopy = 0;
                    } catch (IndexOutOfBoundsException e3) {
                        MtkCatLog.e("[BIP]", "IOOB-3");
                    }
                }
                if (needCopy == 0) {
                    canExitLoop = true;
                } else {
                    try {
                        this.mRxBufferCount = this.mInput.read(this.mRxBuffer, 0, this.mRxBuffer.length);
                        this.mRxBufferOffset = 0;
                    } catch (IOException e4) {
                        MtkCatLog.e("[BIP]", "IOException");
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
            MtkCatLog.e("[BIP]", "[UICC]sendData - data null:");
            return 5;
        } else if (this.mTxBuffer == null) {
            MtkCatLog.e("[BIP]", "[UICC]sendData - mTxBuffer null:");
            return 5;
        } else {
            int txRemaining = this.mTxBuffer.length - this.mTxBufferCount;
            MtkCatLog.d("[BIP]", "[UICC]sendData: size of buffer:" + data.length + " mode:" + mode);
            MtkCatLog.d("[BIP]", "[UICC]sendData: size of buffer:" + this.mTxBuffer.length + " count:" + this.mTxBufferCount);
            if (this.mTxBufferCount == 0 && 1 == mode) {
                tmpBuffer = data;
                this.mTxBufferCount = data.length;
            } else {
                try {
                    if (txRemaining >= data.length) {
                        System.arraycopy(data, 0, this.mTxBuffer, this.mTxBufferCount, data.length);
                        this.mTxBufferCount += data.length;
                    } else {
                        MtkCatLog.d("[BIP]", "[UICC]sendData - tx buffer is not enough");
                    }
                    tmpBuffer = this.mTxBuffer;
                } catch (IndexOutOfBoundsException e) {
                    return 5;
                }
            }
            if (mode == 1 && this.mChannelStatus == 4 && this.mChannelStatusData.mChannelStatus == -128) {
                try {
                    MtkCatLog.d("[BIP]", "S[UICC]END_DATA_MODE_IMMEDIATE:" + this.mTxBuffer.length + " count:" + this.mTxBufferCount);
                    this.mOutput.write(tmpBuffer, 0, this.mTxBufferCount);
                    this.mOutput.flush();
                    this.mTxBufferCount = 0;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return 5;
                } catch (NullPointerException e22) {
                    e22.printStackTrace();
                    return 5;
                }
            }
            return 0;
        }
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int getTxAvailBufferSize() {
        if (this.mTxBuffer == null) {
            MtkCatLog.e("[BIP]", "[UICC]getTxAvailBufferSize - mTxBuffer null:");
            return 0;
        }
        int txRemaining = this.mTxBuffer.length - this.mTxBufferCount;
        MtkCatLog.d("[BIP]", "[UICC]available tx buffer size:" + txRemaining);
        return txRemaining;
    }

    @Override // com.mediatek.internal.telephony.cat.Channel
    public int receiveData(int requestSize, ReceiveDataResult rdr) {
        MtkCatLog.d("[BIP]", "[UICC]new receiveData method");
        int ret = 0;
        if (rdr == null) {
            MtkCatLog.d("[BIP]", "[UICC]rdr is null");
            return 5;
        }
        MtkCatLog.d("[BIP]", "[UICC]receiveData " + this.mRxBufferCount + "/" + requestSize + "/" + this.mRxBufferOffset);
        rdr.buffer = new byte[requestSize];
        synchronized (this.mLock) {
            if (this.mRxBufferCount >= requestSize) {
                MtkCatLog.d("[BIP]", "[UICC]rx buffer has enough data");
                try {
                    System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, rdr.buffer, 0, requestSize);
                    this.mRxBufferOffset += requestSize;
                    this.mRxBufferCount -= requestSize;
                    if (this.mRxBufferCount == 0) {
                        this.mRxBufferOffset = 0;
                    }
                    rdr.remainingCount = this.mRxBufferCount + this.mRxBufferCacheCount;
                    if (this.mRxBufferCount < this.mBufferSize) {
                        MtkCatLog.d("[BIP]", ">= [UICC]notify to read data more to mRxBuffer");
                        this.mLock.notify();
                    }
                    MtkCatLog.d("[BIP]", "[UICC]rx buffer has enough data - end");
                } catch (IndexOutOfBoundsException e) {
                    MtkCatLog.d("[BIP]", "[UICC]fail copy rx buffer out 1");
                    return 5;
                }
            } else {
                MtkCatLog.d("[BIP]", "[UICC]rx buffer is insufficient - being");
                try {
                    System.arraycopy(this.mRxBuffer, this.mRxBufferOffset, rdr.buffer, 0, this.mRxBufferCount);
                    this.mRxBufferOffset = 0;
                    this.mRxBufferCount = 0;
                    if (this.mRxBufferCount < this.mBufferSize) {
                        MtkCatLog.d("[BIP]", "< [UICC]notify to read data more to mRxBuffer");
                        this.mLock.notify();
                    }
                    rdr.remainingCount = 0;
                    ret = 9;
                    MtkCatLog.d("[BIP]", "[UICC]rx buffer is insufficient - end");
                } catch (IndexOutOfBoundsException e2) {
                    MtkCatLog.d("[BIP]", "[UICC]fail copy rx buffer out 2");
                    return 5;
                }
            }
        }
        return ret;
    }

    public void setTcpStatus(byte status, boolean isPackED) {
        if (this.mChannelStatusData.mChannelStatus != status) {
            MtkCatLog.d("[BIP]", "[UICC][TCPStatus]" + this.mChannelStatusData.mChannelStatus + "->" + ((int) status));
            this.mChannelStatusData.mChannelStatus = status;
            if (true == isPackED) {
                changeChannelStatus(status);
            }
        }
    }

    public byte getTcpStatus() {
        try {
            return (byte) this.mChannelStatusData.mChannelStatus;
        } catch (NullPointerException e) {
            MtkCatLog.e("[BIP]", "[TCP]getTcpStatus");
            return 0;
        }
    }

    public void setCloseBackToTcpListen(boolean isBackToTcpListen) {
        this.mCloseBackToTcpListen = isBackToTcpListen;
    }

    public boolean isCloseBackToTcpListen() {
        return this.mCloseBackToTcpListen;
    }
}
