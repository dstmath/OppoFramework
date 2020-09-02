package com.mediatek.internal.telephony.cat;

import android.net.Network;
import com.android.internal.telephony.cat.CatCmdMessage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public abstract class Channel {
    protected static final int SOCKET_TIMEOUT = 3000;
    protected boolean isChannelOpened = false;
    protected boolean isReceiveDataTRSent = false;
    protected InetAddress mAddress = null;
    protected BipChannelManager mBipChannelManager = null;
    protected BipService mBipService = null;
    protected int mBufferSize = 0;
    protected int mChannelId = -1;
    protected int mChannelStatus = 0;
    protected ChannelStatus mChannelStatusData = null;
    private MtkCatService mHandler = null;
    protected int mLinkMode = 0;
    protected Object mLock;
    protected Network mNetwork = null;
    protected int mPort = 0;
    protected int mProtocolType = 0;
    protected ReceiveDataResult mRecvDataRet = null;
    protected byte[] mRxBuffer = null;
    protected int mRxBufferCacheCount = 0;
    protected int mRxBufferCount = 0;
    protected int mRxBufferOffset = 0;
    /* access modifiers changed from: private */
    public volatile boolean mStop = false;
    protected byte[] mTxBuffer = null;
    protected int mTxBufferCount = 0;
    protected int needCopy = 0;

    public abstract int closeChannel();

    public abstract int getTxAvailBufferSize();

    public abstract int openChannel(BipCmdMessage bipCmdMessage, Network network);

    public abstract int receiveData(int i, ReceiveDataResult receiveDataResult);

    public abstract ReceiveDataResult receiveData(int i);

    public abstract int sendData(byte[] bArr, int i);

    Channel(int cid, int linkMode, int protocolType, InetAddress address, int port, int bufferSize, MtkCatService handler, BipService bipManager) {
        this.mChannelId = cid;
        this.mLinkMode = linkMode;
        this.mProtocolType = protocolType;
        this.mAddress = address;
        this.mPort = port;
        this.mBufferSize = bufferSize;
        this.mLock = new Object();
        this.mHandler = handler;
        this.mBipService = bipManager;
        this.mBipChannelManager = this.mBipService.getBipChannelManager();
        this.mChannelStatusData = new ChannelStatus(cid, 0, 0);
    }

    public void dataAvailable(int bufferSize) {
        if (this.mBipService.mCurrentSetupEventCmd == null) {
            MtkCatLog.e(this, "mCurrentSetupEventCmd is null");
        } else if (!this.mBipService.hasPsEvent(9)) {
            MtkCatLog.d(this, "No need to send data available.");
        } else {
            MtkCatResponseMessage resMsg = new MtkCatResponseMessage(MtkCatCmdMessage.getCmdMsg(), 9);
            byte[] additionalInfo = new byte[7];
            additionalInfo[0] = -72;
            additionalInfo[1] = 2;
            additionalInfo[2] = (byte) (getChannelId() | this.mChannelStatusData.mChannelStatus);
            additionalInfo[3] = 0;
            additionalInfo[4] = -73;
            additionalInfo[5] = 1;
            if (bufferSize > 255) {
                additionalInfo[6] = -1;
            } else {
                additionalInfo[6] = (byte) bufferSize;
            }
            resMsg.setSourceId(130);
            resMsg.setDestinationId(129);
            resMsg.setEventDownload(9, additionalInfo);
            resMsg.setAdditionalInfo(additionalInfo);
            resMsg.setOneShot(false);
            MtkCatLog.d(this, "onEventDownload for dataAvailable");
            this.mHandler.onEventDownload(resMsg);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.mediatek.internal.telephony.cat.MtkCatResponseMessage.<init>(com.android.internal.telephony.cat.CatCmdMessage, int):void
     arg types: [com.mediatek.internal.telephony.cat.MtkCatCmdMessage, int]
     candidates:
      com.mediatek.internal.telephony.cat.MtkCatResponseMessage.<init>(com.android.internal.telephony.cat.CatCmdMessage, com.android.internal.telephony.cat.CatResponseMessage):void
      com.mediatek.internal.telephony.cat.MtkCatResponseMessage.<init>(com.android.internal.telephony.cat.CatCmdMessage, int):void */
    public void changeChannelStatus(byte status) {
        if (this.mBipService.mCurrentSetupEventCmd == null) {
            MtkCatLog.e(this, "mCurrentSetupEventCmd is null");
        } else if (!this.mBipService.hasPsEvent(10)) {
            MtkCatLog.d(this, "No need to send channel status.");
        } else {
            MtkCatResponseMessage resMsg = new MtkCatResponseMessage((CatCmdMessage) this.mBipService.mCurrentSetupEventCmd, 10);
            MtkCatLog.d("[BIP]", "[Channel]:changeChannelStatus:" + ((int) status));
            byte[] additionalInfo = {-72, 2, (byte) (getChannelId() | status), 0};
            resMsg.setSourceId(130);
            resMsg.setDestinationId(129);
            resMsg.setEventDownload(10, additionalInfo);
            resMsg.setAdditionalInfo(additionalInfo);
            resMsg.setOneShot(false);
            this.mHandler.onEventDownload(resMsg);
        }
    }

    public int getChannelStatus() {
        return this.mChannelStatus;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    public void clearChannelBuffer(boolean resetBuffer) {
        if (true == resetBuffer) {
            Arrays.fill(this.mRxBuffer, (byte) 0);
            Arrays.fill(this.mTxBuffer, (byte) 0);
        } else {
            this.mRxBuffer = null;
            this.mTxBuffer = null;
        }
        this.mRxBufferCount = 0;
        this.mRxBufferOffset = 0;
        this.mTxBufferCount = 0;
    }

    /* access modifiers changed from: protected */
    public int checkBufferSize() {
        int minBufferSize = 0;
        int maxBufferSize = 0;
        int defaultBufferSize = 0;
        int i = this.mProtocolType;
        if (i == 5 || i == 2 || i == 3) {
            minBufferSize = 255;
            maxBufferSize = 1400;
            defaultBufferSize = 1024;
        } else if (i == 4 || i == 1) {
            minBufferSize = 255;
            maxBufferSize = 1400;
            defaultBufferSize = 1024;
        }
        MtkCatLog.d("[BIP]", "mBufferSize:" + this.mBufferSize + " minBufferSize:" + minBufferSize + " maxBufferSize:" + maxBufferSize);
        int i2 = this.mBufferSize;
        if (i2 < minBufferSize || i2 > maxBufferSize) {
            if (this.mBufferSize > maxBufferSize) {
                MtkCatLog.d("[BIP]", "buffer size is too large, change it to maximum value");
                this.mBufferSize = maxBufferSize;
            } else {
                MtkCatLog.d("[BIP]", "buffer size is too small, change it to default value");
                this.mBufferSize = defaultBufferSize;
            }
            if (this.mBufferSize < 237) {
                MtkCatLog.d("[BIP]", "buffer size is smaller than 255, change it to MAX_APDU_SIZE");
                this.mBufferSize = BipUtils.MAX_APDU_SIZE;
            }
            return 3;
        }
        MtkCatLog.d("[BIP]", "buffer size is normal");
        return 0;
    }

    /* access modifiers changed from: protected */
    public synchronized void requestStop() {
        this.mStop = true;
        MtkCatLog.d("[BIP]", "requestStop: " + this.mStop);
    }

    protected class UdpReceiverThread implements Runnable {
        DatagramSocket udpSocket;

        UdpReceiverThread(DatagramSocket s) {
            this.udpSocket = s;
        }

        public void run() {
            byte[] localBuffer = new byte[1400];
            MtkCatLog.d("[BIP]", "[UDP]RecTr run");
            DatagramPacket recvPacket = new DatagramPacket(localBuffer, localBuffer.length);
            while (true) {
                try {
                    if (Channel.this.mStop) {
                        break;
                    }
                    MtkCatLog.d("[BIP]", "[UDP]RecTr: Wait data from network");
                    try {
                        Arrays.fill(localBuffer, (byte) 0);
                        this.udpSocket.receive(recvPacket);
                        int recvLen = recvPacket.getLength();
                        MtkCatLog.d("[BIP]", "[UDP]RecTr: recvLen:" + recvLen);
                        if (recvLen < 0) {
                            MtkCatLog.e("[BIP]", "[UDP]RecTr: end of file or server is disconnected.");
                            break;
                        }
                        synchronized (Channel.this.mLock) {
                            MtkCatLog.d("[BIP]", "[UDP]RecTr: mRxBufferCount:" + Channel.this.mRxBufferCount);
                            if (Channel.this.mRxBufferCount == 0) {
                                if (recvLen > Channel.this.mBufferSize && Channel.this.mBufferSize < 1024) {
                                    Channel.this.mRxBuffer = new byte[1024];
                                }
                                System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, 0, recvLen);
                                Channel.this.mRxBufferCount = recvLen;
                                Channel.this.mRxBufferOffset = 0;
                                Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                try {
                                    Channel.this.mLock.wait();
                                } catch (InterruptedException e) {
                                    MtkCatLog.e("[BIP]", "[UDP]RecTr: InterruptedException !!!");
                                    e.printStackTrace();
                                }
                            } else if (Channel.this.mRxBufferCount > 0) {
                                do {
                                    Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                    try {
                                        Channel.this.mLock.wait();
                                    } catch (InterruptedException e2) {
                                        MtkCatLog.e("[BIP]", "[UDP]RecTr: InterruptedException !!!");
                                        e2.printStackTrace();
                                    }
                                } while (Channel.this.mRxBufferCount > 0);
                                if (recvLen > 0) {
                                    System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, 0, recvLen);
                                    Channel.this.mRxBufferCount = recvLen;
                                    Channel.this.mRxBufferOffset = 0;
                                    Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                    try {
                                        Channel.this.mLock.wait();
                                    } catch (InterruptedException e3) {
                                        MtkCatLog.e("[BIP]", "[UDP]RecTr: InterruptedException !!!");
                                        e3.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e4) {
                        MtkCatLog.e("[BIP]", "[UDP]RecTr:read io exception.");
                        Arrays.fill(localBuffer, (byte) 0);
                        Channel.this.mChannelStatusData.mChannelStatus = 0;
                        Channel.this.clearChannelBuffer(false);
                    }
                } catch (Exception e5) {
                    MtkCatLog.d("[BIP]", "[UDP]RecTr:Error.");
                    e5.printStackTrace();
                    return;
                }
            }
            if (Channel.this.mStop) {
                MtkCatLog.d("[BIP]", "[UDP]RecTr: stop");
            }
        }
    }

    protected class TcpReceiverThread implements Runnable {
        DataInputStream di;

        TcpReceiverThread(DataInputStream s) {
            this.di = s;
        }

        public void run() {
            byte[] localBuffer = new byte[1400];
            MtkCatLog.d("[BIP]", "[TCP]RecTr: run");
            while (true) {
                try {
                    if (Channel.this.mStop) {
                        break;
                    }
                    MtkCatLog.d("[BIP]", "[TCP]RecTr: Wait data from network");
                    try {
                        Arrays.fill(localBuffer, (byte) 0);
                        int recvLen = this.di.read(localBuffer);
                        MtkCatLog.d("[BIP]", "[TCP]RecTr: recvLen:" + recvLen);
                        if (recvLen < 0) {
                            MtkCatLog.e("[BIP]", "[TCP]RecTr: end of file or server is disconnected.");
                            break;
                        }
                        synchronized (Channel.this.mLock) {
                            MtkCatLog.d("[BIP]", "[TCP]RecTr: mRxBufferCount:" + Channel.this.mRxBufferCount);
                            if (Channel.this.mRxBufferCount == 0) {
                                if (recvLen > Channel.this.mBufferSize && Channel.this.mBufferSize < 1024) {
                                    Channel.this.mRxBuffer = new byte[1024];
                                }
                                System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, 0, recvLen);
                                Channel.this.mRxBufferCount = recvLen;
                                Channel.this.mRxBufferOffset = 0;
                                Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                try {
                                    Channel.this.mLock.wait();
                                } catch (InterruptedException e) {
                                    MtkCatLog.e("[BIP]", "[TCP]RecTr: InterruptedException !!!");
                                    e.printStackTrace();
                                }
                            } else if (Channel.this.mRxBufferCount > 0) {
                                do {
                                    Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                    try {
                                        Channel.this.mLock.wait();
                                    } catch (InterruptedException e2) {
                                        MtkCatLog.e("[BIP]", "[TCP]RecTr: InterruptedException !!!");
                                        e2.printStackTrace();
                                    }
                                } while (Channel.this.mRxBufferCount > 0);
                                if (recvLen > 0) {
                                    System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, 0, recvLen);
                                    Channel.this.mRxBufferCount = recvLen;
                                    Channel.this.mRxBufferOffset = 0;
                                    Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                    try {
                                        Channel.this.mLock.wait();
                                    } catch (InterruptedException e3) {
                                        MtkCatLog.e("[BIP]", "[TCP]RecTr: InterruptedException !!!");
                                        e3.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e4) {
                        MtkCatLog.e("[BIP]", "[TCP]RecTr:read io exception.");
                        Arrays.fill(localBuffer, (byte) 0);
                        Channel.this.clearChannelBuffer(false);
                    }
                } catch (Exception e5) {
                    MtkCatLog.d("[BIP]", "[TCP]RecTr:Error");
                    e5.printStackTrace();
                    return;
                }
            }
            if (Channel.this.mStop) {
                MtkCatLog.d("[BIP]", "[TCP]RecTr: stop");
            }
        }
    }

    protected class UICCServerThread implements Runnable {
        private static final int RETRY_ACCEPT_SLEEPTIME = 100;
        private static final int RETRY_COUNT = 4;
        DataInputStream di = null;
        int mReTryCount = 0;
        TcpServerChannel mTcpServerChannel = null;

        UICCServerThread(TcpServerChannel tcpServerChannel) {
            MtkCatLog.d("[BIP]", "OpenServerSocketThread Init");
            this.mTcpServerChannel = tcpServerChannel;
        }

        public void run() {
            boolean goOnRead;
            int rSize;
            int rSize2;
            byte[] localBuffer = new byte[1400];
            MtkCatLog.d("[BIP]", "[UICC]ServerTr: Run Enter");
            while (true) {
                if (Channel.this.mChannelStatus == 4) {
                    if (this.mTcpServerChannel.getTcpStatus() != 64) {
                        this.mTcpServerChannel.setTcpStatus(BipUtils.TCP_STATUS_LISTEN, true);
                    } else {
                        MtkCatLog.d("[BIP]", "[UICC]ServerTr:TCP status = TCP_STATUS_LISTEN");
                    }
                    try {
                        MtkCatLog.d("[BIP]", "[UICC]ServerTr:Listen to wait client connection...");
                        this.mTcpServerChannel.mSocket = this.mTcpServerChannel.mSSocket.accept();
                        MtkCatLog.d("[BIP]", "[UICC]ServerTr:Receive a client connection.");
                        this.mTcpServerChannel.setTcpStatus(BipUtils.TCP_STATUS_ESTABLISHED, true);
                        if (this.mTcpServerChannel.mInput == null) {
                            try {
                                this.mTcpServerChannel.mInput = new DataInputStream(this.mTcpServerChannel.mSocket.getInputStream());
                                this.di = this.mTcpServerChannel.mInput;
                            } catch (IOException e) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IOException: getInputStream.");
                            }
                        }
                        if (this.mTcpServerChannel.mOutput == null) {
                            try {
                                this.mTcpServerChannel.mOutput = new BufferedOutputStream(this.mTcpServerChannel.mSocket.getOutputStream());
                            } catch (IOException e2) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IOException: getOutputStream.");
                            }
                        }
                        while (true) {
                            if (Channel.this.mStop) {
                                break;
                            }
                            MtkCatLog.d("[BIP]", "[UICC]ServerTr: Start to read data from network");
                            try {
                                Arrays.fill(localBuffer, (byte) 0);
                                int recvLen = this.di.read(localBuffer);
                                MtkCatLog.d("[BIP]", "[UICC]ServerTr: Receive data:" + recvLen);
                                if (recvLen >= 0) {
                                    int localBufferOffset = 0;
                                    synchronized (Channel.this.mLock) {
                                        MtkCatLog.d("[BIP]", "[UICC]ServerTr:mRxBufferCount: " + Channel.this.mRxBufferCount);
                                        if (Channel.this.mRxBufferCount == 0) {
                                            System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, 0, recvLen);
                                            Channel.this.mRxBufferCount = recvLen;
                                            Channel.this.mRxBufferOffset = 0;
                                            Channel.this.dataAvailable(Channel.this.mRxBufferCount);
                                        } else {
                                            System.arraycopy(Channel.this.mRxBuffer, Channel.this.mRxBufferOffset, Channel.this.mRxBuffer, 0, Channel.this.mRxBufferCount);
                                            if (recvLen <= Channel.this.mBufferSize - Channel.this.mRxBufferCount) {
                                                rSize2 = recvLen;
                                            } else {
                                                int i = Channel.this.mBufferSize - Channel.this.mRxBufferCount;
                                                rSize2 = i;
                                                localBufferOffset = i;
                                                Channel.this.mRxBufferCacheCount = recvLen - rSize2;
                                            }
                                            System.arraycopy(localBuffer, 0, Channel.this.mRxBuffer, Channel.this.mRxBufferCount, rSize2);
                                            Channel.this.mRxBufferCount += rSize2;
                                            Channel.this.mRxBufferOffset = 0;
                                            MtkCatLog.d("[BIP]", "[UICC]ServerTr:rSize: " + rSize2 + ", mRxBufferCacheCount: " + Channel.this.mRxBufferCacheCount);
                                        }
                                        while (true) {
                                            if (Channel.this.mRxBufferCount < Channel.this.mBufferSize) {
                                                goOnRead = true;
                                                break;
                                            }
                                            try {
                                                MtkCatLog.d("[BIP]", "[UICC]ServerTr:mRxBuffer is full.");
                                                Channel.this.mLock.wait();
                                            } catch (InterruptedException e3) {
                                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IE :mRxBufferCount >= mBufferSize");
                                                if (true == this.mTcpServerChannel.isCloseBackToTcpListen()) {
                                                    Channel.this.clearChannelBuffer(true);
                                                    this.mTcpServerChannel.setCloseBackToTcpListen(false);
                                                    goOnRead = false;
                                                    break;
                                                }
                                            }
                                            if (Channel.this.mRxBufferCacheCount > 0) {
                                                if (Channel.this.mRxBufferCount > 0) {
                                                    System.arraycopy(Channel.this.mRxBuffer, Channel.this.mRxBufferOffset, Channel.this.mRxBuffer, 0, Channel.this.mRxBufferCount);
                                                }
                                                if (Channel.this.mRxBufferCacheCount <= Channel.this.mBufferSize - Channel.this.mRxBufferCount) {
                                                    rSize = Channel.this.mRxBufferCacheCount;
                                                } else {
                                                    rSize = Channel.this.mBufferSize - Channel.this.mRxBufferCount;
                                                }
                                                System.arraycopy(localBuffer, localBufferOffset, Channel.this.mRxBuffer, Channel.this.mRxBufferCount, rSize);
                                                Channel.this.mRxBufferCount += rSize;
                                                Channel.this.mRxBufferCacheCount -= rSize;
                                                localBufferOffset += rSize;
                                                Channel.this.mRxBufferOffset = 0;
                                            }
                                        }
                                    }
                                    if (!goOnRead) {
                                        break;
                                    }
                                    MtkCatLog.d("[BIP]", "[UICC]ServerTr: buffer data:" + Channel.this.mRxBufferCount);
                                } else {
                                    MtkCatLog.e("[BIP]", "[UICC]ServerTr: client diconnected");
                                    try {
                                        if (this.mTcpServerChannel.mInput != null) {
                                            this.mTcpServerChannel.mInput.close();
                                        }
                                        if (this.mTcpServerChannel.mOutput != null) {
                                            this.mTcpServerChannel.mOutput.close();
                                        }
                                    } catch (IOException e4) {
                                        MtkCatLog.e("[BIP]", "[UICC]ServerTr:len<0,IOException input stream.");
                                    }
                                    Channel.this.clearChannelBuffer(true);
                                    this.mTcpServerChannel.setTcpStatus(BipUtils.TCP_STATUS_LISTEN, true);
                                }
                            } catch (IOException e5) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:read io exception.");
                                Arrays.fill(localBuffer, (byte) 0);
                                try {
                                    if (this.mTcpServerChannel.mInput != null) {
                                        this.mTcpServerChannel.mInput.close();
                                    }
                                    if (this.mTcpServerChannel.mOutput != null) {
                                        this.mTcpServerChannel.mOutput.close();
                                    }
                                    Channel.this.clearChannelBuffer(true);
                                } catch (IOException e6) {
                                    MtkCatLog.e("[BIP]", "[UICC]ServerTr:IOException input stream.");
                                }
                            }
                        }
                        if (Channel.this.mStop) {
                            MtkCatLog.d("[BIP]", "[UICC]ServerTr: stop");
                        }
                    } catch (IOException e7) {
                        MtkCatLog.e("[BIP]", "[UICC]ServerTr:Fail to accept server socket retry:" + this.mReTryCount);
                        int i2 = this.mReTryCount;
                        if (4 >= i2) {
                            this.mReTryCount = i2 + 1;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e8) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IE: sleep for SS accept retry.");
                            }
                        } else {
                            this.mReTryCount = 0;
                            try {
                                if (this.mTcpServerChannel.mInput != null) {
                                    this.mTcpServerChannel.mInput.close();
                                }
                                if (this.mTcpServerChannel.mOutput != null) {
                                    this.mTcpServerChannel.mOutput.close();
                                }
                            } catch (IOException e9) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IOE: input/output stream close.");
                            }
                            try {
                                this.mTcpServerChannel.mSSocket.close();
                            } catch (IOException e10) {
                                MtkCatLog.e("[BIP]", "[UICC]ServerTr:IOE: socket close.");
                            }
                            Channel.this.clearChannelBuffer(false);
                            Channel.this.closeChannel();
                            Channel.this.mBipChannelManager.removeChannel(Channel.this.mChannelId);
                            this.mTcpServerChannel.setTcpStatus((byte) 0, true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
