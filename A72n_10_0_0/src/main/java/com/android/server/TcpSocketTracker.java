package com.android.server;

import android.content.Context;
import android.net.INetd;
import android.net.Network;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructInetDiagReqV2Oppo;
import android.net.netlink.StructNlMsgHdr;
import android.net.util.SocketUtils;
import android.os.IBinder;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TcpSocketTracker {
    private static final int[] ADDRESS_FAMILIES = {OsConstants.AF_INET6, OsConstants.AF_INET};
    private static final boolean DBG = true;
    private static final int DEFAULT_RECV_BUFSIZE = 60000;
    private static final int IDIAG_COOKIE_OFFSET = 44;
    private static final int INET_DIAG_MEMINFO = 1;
    private static final long IO_TIMEOUT = 3000;
    private static final int NULL_MASK = 0;
    private static final int SOCKDIAG_MSG_HEADER_SIZE = 88;
    private static final String TAG = "TcpSocketTracker";
    private static final int UNKNOWN_MARK = -1;
    private final Dependencies mDependencies;
    private int mLatestPacketFailPercentage;
    private int mLatestReceivedCount;
    private int mMinPacketsThreshold = 10;
    private final INetd mNetd;
    private final Network mNetwork;
    private int mSentSinceLastRecv;
    private final SparseArray<byte[]> mSockDiagMsg = new SparseArray<>();
    private final LongSparseArray<SocketInfo> mSocketInfos = new LongSparseArray<>();
    private int mTcpPacketsFailRateThreshold = 80;

    public static byte[] InetDiagReqV2Ext(int protocol, InetSocketAddress local, InetSocketAddress remote, int family, short flags, int pad, int idiagExt, int state) throws NullPointerException {
        byte[] bytes = new byte[72];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
        nlMsgHdr.nlmsg_len = bytes.length;
        nlMsgHdr.nlmsg_type = 20;
        nlMsgHdr.nlmsg_flags = flags;
        nlMsgHdr.pack(byteBuffer);
        new StructInetDiagReqV2Oppo(protocol, local, remote, family, pad, idiagExt, state).pack(byteBuffer);
        return bytes;
    }

    public TcpSocketTracker(Dependencies dps, Network network) {
        this.mDependencies = dps;
        this.mNetwork = network;
        this.mNetd = this.mDependencies.getNetd();
        if (this.mDependencies.isTcpInfoParsingSupported()) {
            int[] iArr = ADDRESS_FAMILIES;
            for (int family : iArr) {
                this.mSockDiagMsg.put(family, InetDiagReqV2Ext(OsConstants.IPPROTO_TCP, null, null, family, 769, 0, 2, 14));
            }
        }
    }

    private void closeSocketQuietly(FileDescriptor fd) {
        try {
            SocketUtils.closeSocket(fd);
        } catch (IOException e) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005c, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0060, code lost:
        r1 = r0;
        r8 = r5;
        r19 = r11;
        r18 = r13;
        r16 = r14;
        r11 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        android.util.Log.e(com.android.server.TcpSocketTracker.TAG, "Expect to get family " + r5 + " SOCK_DIAG_BY_FAMILY message but get " + ((int) r1.nlmsg_type));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c3, code lost:
        r19 = r11;
        r18 = r13;
        r16 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x011f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0120, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0187, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0188, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x018d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x018e, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01a6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01a7, code lost:
        r1 = r0;
        r11 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0202, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0203, code lost:
        r11 = r11;
        r1 = r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005b A[ExcHandler: ErrnoException | InterruptedIOException | SocketException (r0v13 'e' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:33:0x00a5] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x018d A[ExcHandler: ErrnoException | InterruptedIOException | SocketException (r0v7 'e' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r11 
      PHI: (r11v8 'fd' java.io.FileDescriptor) = (r11v5 'fd' java.io.FileDescriptor), (r11v5 'fd' java.io.FileDescriptor), (r11v9 'fd' java.io.FileDescriptor) binds: [B:11:0x0026, B:12:?, B:14:0x003d] A[DONT_GENERATE, DONT_INLINE], Splitter:B:11:0x0026] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01a6 A[ExcHandler: ErrnoException | InterruptedIOException | SocketException (r0v9 'e' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r19 
      PHI: (r19v6 'fd' java.io.FileDescriptor) = (r19v8 'fd' java.io.FileDescriptor), (r19v8 'fd' java.io.FileDescriptor), (r19v15 'fd' java.io.FileDescriptor), (r19v15 'fd' java.io.FileDescriptor) binds: [B:59:0x014c, B:60:?, B:49:0x011e, B:50:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:49:0x011e] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0202 A[ExcHandler: ErrnoException | InterruptedIOException | SocketException (r0v6 'e' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:76:0x01ad] */
    public boolean pollSocketsInfo() {
        FileDescriptor fd;
        Throwable th;
        Throwable th2;
        TcpStat stat;
        int i;
        FileDescriptor fd2;
        int[] iArr;
        int i2;
        ByteBuffer bytes;
        int family;
        if (!this.mDependencies.isTcpInfoParsingSupported()) {
            return false;
        }
        try {
            long time = SystemClock.elapsedRealtime();
            fd = this.mDependencies.connectToKernel();
            try {
                stat = new TcpStat();
                int[] iArr2 = ADDRESS_FAMILIES;
                int length = iArr2.length;
                int i3 = 0;
                while (i3 < length) {
                    try {
                        int family2 = iArr2[i3];
                        this.mDependencies.sendPollingRequest(fd, this.mSockDiagMsg.get(family2));
                        ByteBuffer bytes2 = this.mDependencies.recvMessage(fd);
                        while (true) {
                            if (!enoughBytesRemainForValidNlMsg(bytes2)) {
                                fd2 = fd;
                                iArr = iArr2;
                                i2 = length;
                                break;
                            }
                            StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(bytes2);
                            if (nlmsghdr == null) {
                                Log.e(TAG, "Badly formatted data.");
                                fd2 = fd;
                                iArr = iArr2;
                                i2 = length;
                                break;
                            }
                            try {
                                int nlmsgLen = nlmsghdr.nlmsg_len;
                                log("pollSocketsInfo: nlmsghdr=" + nlmsghdr + ", limit=" + bytes2.limit());
                                if (nlmsghdr.nlmsg_type == 3) {
                                    fd2 = fd;
                                    iArr = iArr2;
                                    i2 = length;
                                    break;
                                } else if (nlmsghdr.nlmsg_type != 20) {
                                    break;
                                } else {
                                    if (isValidInetDiagMsgSize(nlmsgLen)) {
                                        bytes2.position(bytes2.position() + 44);
                                        long cookie = bytes2.getLong();
                                        bytes2.position(((bytes2.position() + 72) - 44) - 8);
                                        i2 = length;
                                        iArr = iArr2;
                                        fd2 = fd;
                                        family = family2;
                                        bytes = bytes2;
                                        try {
                                            SocketInfo info = parseSockInfo(bytes2, family2, nlmsgLen, time);
                                            synchronized (this.mSocketInfos) {
                                                stat.accumulate(calculateLatestPacketsStat(info, this.mSocketInfos.get(cookie)));
                                                this.mSocketInfos.put(cookie, info);
                                            }
                                        } catch (ErrnoException | InterruptedIOException | SocketException e) {
                                        } catch (Throwable th3) {
                                            th = th3;
                                            fd = fd2;
                                            closeSocketQuietly(fd);
                                            throw th;
                                        }
                                    } else {
                                        family = family2;
                                        fd2 = fd;
                                        iArr = iArr2;
                                        i2 = length;
                                        bytes = bytes2;
                                    }
                                    family2 = family;
                                    bytes2 = bytes;
                                    length = i2;
                                    iArr2 = iArr;
                                    fd = fd2;
                                }
                            } catch (IllegalArgumentException | BufferUnderflowException e2) {
                                family = family2;
                                fd2 = fd;
                                iArr = iArr2;
                                i2 = length;
                                bytes = bytes2;
                                RuntimeException e3 = e2;
                                Log.wtf(TAG, "Unexpected socket info parsing, family " + family + " buffer:" + bytes + StringUtils.SPACE + Base64.getEncoder().encodeToString(bytes.array()), e3);
                                i3++;
                                length = i2;
                                iArr2 = iArr;
                                fd = fd2;
                            }
                        }
                        i3++;
                        length = i2;
                        iArr2 = iArr;
                        fd = fd2;
                    } catch (ErrnoException | InterruptedIOException | SocketException e4) {
                    }
                }
            } catch (ErrnoException | InterruptedIOException | SocketException e5) {
                th2 = e5;
                try {
                    Log.e(TAG, "Fail to get TCP info via netlink.", th2);
                    closeSocketQuietly(fd);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    closeSocketQuietly(fd);
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                closeSocketQuietly(fd);
                throw th;
            }
            try {
                if (stat.receivedCount == 0) {
                    i = this.mSentSinceLastRecv + stat.sentCount;
                } else {
                    i = 0;
                }
                try {
                    this.mSentSinceLastRecv = i;
                    this.mLatestReceivedCount = stat.receivedCount;
                    this.mLatestPacketFailPercentage = stat.sentCount != 0 ? ((stat.retransmitCount + stat.lostCount) * 100) / stat.sentCount : 0;
                    log("pollSocketsInfo.stat.retransmitCount=" + stat.retransmitCount + ",stat.lostCount=" + stat.lostCount + ",stat.sentCount=" + stat.sentCount);
                    cleanupSocketInfo(time);
                    closeSocketQuietly(fd);
                    return true;
                } catch (ErrnoException | InterruptedIOException | SocketException e6) {
                }
            } catch (Throwable th6) {
                fd = fd;
                th = th6;
                closeSocketQuietly(fd);
                throw th;
            }
        } catch (ErrnoException | InterruptedIOException | SocketException e7) {
            fd = null;
            th2 = e7;
            Log.e(TAG, "Fail to get TCP info via netlink.", th2);
            closeSocketQuietly(fd);
            return false;
        } catch (Throwable th7) {
            fd = null;
            th = th7;
            closeSocketQuietly(fd);
            throw th;
        }
    }

    private void cleanupSocketInfo(long time) {
        synchronized (this.mSocketInfos) {
            int size = this.mSocketInfos.size();
            List<Long> toRemove = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                long key = this.mSocketInfos.keyAt(i);
                if (this.mSocketInfos.get(key).updateTime < time) {
                    toRemove.add(Long.valueOf(key));
                }
            }
            for (Long key2 : toRemove) {
                this.mSocketInfos.remove(key2.longValue());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SocketInfo parseSockInfo(ByteBuffer bytes, int family, int nlmsgLen, long time) {
        int remainingDataSize = (bytes.position() + nlmsgLen) - 88;
        TcpInfo tcpInfo = null;
        int mark = 0;
        while (bytes.position() < remainingDataSize) {
            RoutingAttribute rtattr = new RoutingAttribute(bytes.getShort(), bytes.getShort());
            short dataLen = rtattr.getDataLength();
            if (rtattr.rtaType == 2) {
                tcpInfo = TcpInfo.parse(bytes, dataLen);
            } else if (rtattr.rtaType == 15) {
                mark = bytes.getInt();
            } else {
                skipRemainingAttributesBytesAligned(bytes, dataLen);
            }
        }
        SocketInfo info = new SocketInfo(tcpInfo, family, mark, time);
        log("parseSockInfo, " + info);
        return info;
    }

    public boolean isDataStallSuspected() {
        if (this.mDependencies.isTcpInfoParsingSupported() && getLatestPacketFailPercentage() >= getTcpPacketsFailRateThreshold()) {
            return true;
        }
        return false;
    }

    private TcpStat calculateLatestPacketsStat(SocketInfo current, SocketInfo previous) {
        TcpStat stat = new TcpStat();
        if (current.tcpInfo == null) {
            log("Current tcpInfo is null.");
            return null;
        }
        stat.sentCount = current.tcpInfo.mSegsOut;
        stat.receivedCount = current.tcpInfo.mSegsIn;
        stat.lostCount = current.tcpInfo.mLost;
        stat.retransmitCount = current.tcpInfo.mRetransmits;
        if (!(previous == null || previous.tcpInfo == null)) {
            stat.sentCount -= previous.tcpInfo.mSegsOut;
            stat.receivedCount -= previous.tcpInfo.mSegsIn;
            stat.lostCount -= previous.tcpInfo.mLost;
            stat.retransmitCount -= previous.tcpInfo.mRetransmits;
        }
        return stat;
    }

    public int getLatestPacketFailPercentage() {
        if (this.mDependencies.isTcpInfoParsingSupported() && getSentSinceLastRecv() >= getMinPacketsThreshold()) {
            return this.mLatestPacketFailPercentage;
        }
        return -1;
    }

    public int getSentSinceLastRecv() {
        if (!this.mDependencies.isTcpInfoParsingSupported()) {
            return -1;
        }
        return this.mSentSinceLastRecv;
    }

    public int getLatestReceivedCount() {
        if (!this.mDependencies.isTcpInfoParsingSupported()) {
            return -1;
        }
        return this.mLatestReceivedCount;
    }

    @VisibleForTesting
    static boolean enoughBytesRemainForValidNlMsg(ByteBuffer bytes) {
        return bytes.remaining() >= 16;
    }

    private static boolean isValidInetDiagMsgSize(int nlMsgLen) {
        return nlMsgLen >= SOCKDIAG_MSG_HEADER_SIZE;
    }

    private int getMinPacketsThreshold() {
        return this.mMinPacketsThreshold;
    }

    private int getTcpPacketsFailRateThreshold() {
        return this.mTcpPacketsFailRateThreshold;
    }

    private void skipRemainingAttributesBytesAligned(ByteBuffer buffer, short len) {
        buffer.position(NetlinkConstants.alignedLengthOf(len) + buffer.position());
    }

    private void log(String str) {
        Log.d(TAG, str);
    }

    /* access modifiers changed from: package-private */
    public class RoutingAttribute {
        public static final int HEADER_LENGTH = 4;
        public static final int INET_DIAG_INFO = 2;
        public static final int INET_DIAG_MARK = 15;
        public final short rtaLen;
        public final short rtaType;

        RoutingAttribute(short len, short type) {
            this.rtaLen = len;
            this.rtaType = type;
        }

        public short getDataLength() {
            return (short) (this.rtaLen - 4);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class SocketInfo {
        public static final int INIT_MARK_VALUE = 0;
        public final int fwmark;
        public final int ipFamily;
        public final TcpInfo tcpInfo;
        public final long updateTime;

        SocketInfo(TcpInfo info, int family, int mark, long time) {
            this.tcpInfo = info;
            this.ipFamily = family;
            this.updateTime = time;
            this.fwmark = mark;
        }

        public String toString() {
            return "SocketInfo {Type:" + ipTypeToString(this.ipFamily) + ", " + this.tcpInfo + ", mark:" + this.fwmark + " updated at " + this.updateTime + "}";
        }

        private String ipTypeToString(int type) {
            if (type == OsConstants.AF_INET) {
                return "IP";
            }
            if (type == OsConstants.AF_INET6) {
                return "IPV6";
            }
            return "UNKNOWN";
        }
    }

    /* access modifiers changed from: private */
    public class TcpStat {
        public int lostCount;
        public int receivedCount;
        public int retransmitCount;
        public int sentCount;

        private TcpStat() {
        }

        /* access modifiers changed from: package-private */
        public void accumulate(TcpStat stat) {
            if (stat != null) {
                this.sentCount += stat.sentCount;
                this.lostCount += stat.lostCount;
                this.receivedCount += stat.receivedCount;
                this.retransmitCount += stat.retransmitCount;
            }
        }
    }

    @VisibleForTesting
    public static class Dependencies {
        private final Context mContext;
        private final boolean mIsTcpInfoParsingSupported;

        public Dependencies(Context context, boolean tcpSupport) {
            this.mContext = context;
            this.mIsTcpInfoParsingSupported = tcpSupport;
        }

        public FileDescriptor connectToKernel() throws ErrnoException, SocketException {
            FileDescriptor fd = Os.socket(OsConstants.AF_NETLINK, OsConstants.SOCK_DGRAM | OsConstants.SOCK_CLOEXEC, OsConstants.NETLINK_INET_DIAG);
            Os.connect(fd, SocketUtils.makeNetlinkSocketAddress(0, 0));
            return fd;
        }

        public void sendPollingRequest(FileDescriptor fd, byte[] msg) throws ErrnoException, InterruptedIOException {
            Os.setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(3000));
            Os.write(fd, msg, 0, msg.length);
        }

        public boolean isTcpInfoParsingSupported() {
            return this.mIsTcpInfoParsingSupported;
        }

        public ByteBuffer recvMessage(FileDescriptor fd) throws ErrnoException, InterruptedIOException {
            return NetlinkSocket.recvMessage(fd, 60000, 3000);
        }

        public Context getContext() {
            return this.mContext;
        }

        public INetd getNetd() {
            return INetd.Stub.asInterface((IBinder) this.mContext.getSystemService("netd"));
        }
    }
}
