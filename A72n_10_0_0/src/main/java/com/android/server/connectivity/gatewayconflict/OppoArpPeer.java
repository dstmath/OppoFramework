package com.android.server.connectivity.gatewayconflict;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.util.SocketUtils;
import android.os.SystemClock;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.system.StructTimeval;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import libcore.io.IoBridge;
import libcore.util.HexEncoding;

public class OppoArpPeer {
    private static final byte[] ANY_MAC_BYTES = {0, 0, 0, 0, 0, 0};
    private static final String ANY_MAC_STR = "any";
    public static final int ARP_DUP_RESPONSE_TIMEOUT = 5000;
    public static final int ARP_FIRST_RESPONSE_TIMEOUT = 2000;
    private static final int ARP_LENGTH = 28;
    private static final int ARP_READ_LENGTH = 60;
    private static final int ARP_TYPE = 2054;
    private static final int ETHERNET_LENGTH = 14;
    private static final int ETHERNET_TYPE = 1;
    private static final int IPV4_LENGTH = 4;
    private static final byte[] L2_BROADCAST = {-1, -1, -1, -1, -1, -1};
    private static final int MAC_ADDR_LENGTH = 6;
    protected static final int MAX_LENGTH = 1500;
    private static final String TAG = "OppoArpPeer";
    private ArpPeerChangeCallback mCallback = null;
    protected Context mContext;
    private int mDupArpListSize = 1;
    protected List<byte[]> mDupTarget;
    private byte[] mHwAddr;
    protected NetworkInterface mIface;
    private String mIfaceName;
    private SocketAddress mInterfaceBroadcastAddr;
    public boolean mIsIpDetector = false;
    private Inet4Address mMyAddr;
    protected Network mNetwork = null;
    protected NetworkAgentInfo mNetworkAgentInfo = null;
    private FileDescriptor mSocket;
    protected Inet4Address mTarget;

    public interface ArpPeerChangeCallback {
        void onArpReponseChanged(int i, Network network);
    }

    public OppoArpPeer(Context context, NetworkAgentInfo networkAgentInfo, ArpPeerChangeCallback callback, int dupArpListSize) {
        this.mContext = context;
        this.mNetworkAgentInfo = networkAgentInfo;
        if (this.mNetworkAgentInfo != null) {
            this.mNetwork = networkAgentInfo.network;
        }
        this.mCallback = callback;
        this.mDupArpListSize = dupArpListSize;
        this.mDupTarget = new ArrayList(this.mDupArpListSize);
    }

    private class ProbeTask implements Runnable {
        private ProbeTask() {
        }

        public void run() {
            try {
                OppoArpPeer.this.findDupTarget(5000);
                OppoArpPeer.this.onProbeFinished();
            } catch (ErrnoException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onProbeFinished() {
    }

    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            if (this.mIface == null) {
                Log.e(TAG, "mIface is null");
                return false;
            }
            this.mHwAddr = this.mIface.getHardwareAddress();
            Log.i(TAG, "mac addr:" + byteArrayToHex(this.mHwAddr) + "---" + this.mIface.getIndex());
            this.mInterfaceBroadcastAddr = SocketUtils.makePacketSocketAddress((int) ARP_TYPE, this.mIface.getIndex());
            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, "initInterface catch NullPointerException.");
            return false;
        } catch (SocketException e2) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName);
            return false;
        }
    }

    private boolean initSocket() {
        try {
            this.mSocket = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_ARP);
            Os.bind(this.mSocket, new PacketSocketAddress((short) OsConstants.ETH_P_ARP, this.mIface.getIndex()));
            return true;
        } catch (ErrnoException | SocketException e) {
            Log.e(TAG, "Error creating packet socket", e);
            return false;
        } catch (NullPointerException e2) {
            Log.e(TAG, "initSocket catch NullPointerException.");
            return false;
        }
    }

    private Inet4Address getIpv4InterfaceAddress() {
        try {
            NetworkInterface iface = NetworkInterface.getByName(this.mIfaceName);
            if (iface == null) {
                Log.e(TAG, "iface is null");
                return (Inet4Address) Inet4Address.ANY;
            }
            Enumeration<InetAddress> ipAddres = iface.getInetAddresses();
            while (ipAddres.hasMoreElements()) {
                InetAddress inetAddress = ipAddres.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    Log.i(TAG, "Local Source address:" + inetAddress);
                    return (Inet4Address) inetAddress;
                }
            }
            return (Inet4Address) Inet4Address.ANY;
        } catch (SocketException e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName);
        }
    }

    public boolean doDupArp(String iface, Inet4Address myAddress, Inet4Address target) {
        this.mIfaceName = iface;
        if (myAddress == null) {
            myAddress = getIpv4InterfaceAddress();
        }
        this.mMyAddr = myAddress;
        this.mTarget = target;
        try {
            initInterface();
            initSocket();
            new Thread(new ProbeTask()).start();
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "doDupArp ", exception);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isOriginalNetworkConnected() {
        if (this.mIsIpDetector) {
            Log.d(TAG, "mIsIpDetector is true");
            return true;
        }
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(this.mNetwork);
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

    protected static byte[] macAddressToByteArray(String macStr) {
        if (TextUtils.isEmpty(macStr) || ANY_MAC_STR.equals(macStr)) {
            return ANY_MAC_BYTES;
        }
        String cleanMac = macStr.replace(":", "");
        if (cleanMac.length() == 12) {
            return HexEncoding.decode(cleanMac.toCharArray(), false);
        }
        throw new IllegalArgumentException("invalid mac string length: " + cleanMac);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0181, code lost:
        if (r5 == false) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0185, code lost:
        if (r20.mCallback == null) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x018b, code lost:
        if (isOriginalNetworkConnected() == false) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x018d, code lost:
        r20.mCallback.onArpReponseChanged(r20.mDupTarget.size(), r20.mNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01a2, code lost:
        if (r20.mDupTarget.size() != r20.mDupArpListSize) goto L_0x01a6;
     */
    private int findDupTarget(int timeoutMillis) throws ErrnoException {
        ByteBuffer buf;
        Throwable th;
        int i = 1500;
        ByteBuffer buf2 = ByteBuffer.allocate(1500);
        byte[] desiredIp = this.mTarget.getAddress();
        int i2 = 0;
        if (this.mHwAddr == null) {
            return 0;
        }
        if (this.mMyAddr == null) {
            return 0;
        }
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        Log.i(TAG, "findDupTarget " + this.mTarget + " in " + timeoutMillis);
        buf2.clear();
        buf2.order(ByteOrder.BIG_ENDIAN);
        buf2.put(L2_BROADCAST);
        buf2.put(this.mHwAddr);
        buf2.putShort(2054);
        buf2.putShort(1);
        buf2.putShort((short) OsConstants.ETH_P_IP);
        buf2.put((byte) 6);
        buf2.put((byte) 4);
        buf2.putShort(1);
        buf2.put(this.mHwAddr);
        buf2.put(this.mMyAddr.getAddress());
        buf2.put(new byte[6]);
        buf2.put(desiredIp);
        buf2.flip();
        try {
            Os.sendto(this.mSocket, buf2.array(), 0, buf2.limit(), 0, this.mInterfaceBroadcastAddr);
            byte[] socketBuf = new byte[1500];
            while (true) {
                if (SystemClock.elapsedRealtime() < timeout && this.mDupTarget.size() < this.mDupArpListSize) {
                    Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(timeout - SystemClock.elapsedRealtime()));
                    try {
                        if (Os.read(this.mSocket, socketBuf, i2, i) >= 42) {
                            byte[] recvBuf = new byte[28];
                            System.arraycopy(socketBuf, 14, recvBuf, i2, 28);
                            if (recvBuf[i2] == 0 && recvBuf[1] == 1 && recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[4] == 6 && recvBuf[5] == 4 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                                byte[] peerMac = new byte[6];
                                System.arraycopy(recvBuf, 8, peerMac, 0, 6);
                                boolean newArpReponse = false;
                                if (!isValidPeerMac(peerMac)) {
                                    i2 = 0;
                                    i = 1500;
                                } else {
                                    if (this instanceof OppoGatewayDetector) {
                                        sendGatewayMacAddress(this.mIfaceName, byteArrayToHex(peerMac));
                                    }
                                    synchronized (this.mDupTarget) {
                                        try {
                                            if (isDupTargetArp(this.mDupTarget, peerMac) < 0) {
                                                newArpReponse = true;
                                                StringBuilder sb = new StringBuilder();
                                                buf = buf2;
                                                try {
                                                    sb.append("add entry:");
                                                    sb.append(byteArrayToHex(peerMac));
                                                    Log.i(TAG, sb.toString());
                                                    this.mDupTarget.add(peerMac);
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                            } else {
                                                buf = buf2;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    }
                                }
                            } else {
                                buf = buf2;
                            }
                        } else {
                            buf = buf2;
                        }
                        buf2 = buf;
                        i = 1500;
                        i2 = 0;
                    } catch (Exception se) {
                        Log.e(TAG, "ARP read failure: " + se);
                        return 0;
                    }
                }
            }
            return this.mDupTarget.size();
        } catch (Exception se2) {
            Log.e(TAG, "ARP send failure: " + se2);
            return 0;
        }
    }

    private void sendGatewayMacAddress(String ifaceName, String gatewayMac) {
        if (TextUtils.isEmpty(ifaceName) || TextUtils.isEmpty(gatewayMac)) {
            Log.e(TAG, "sendGatewayMacAddress invalid ifaceName or gatewayMac!");
            return;
        }
        Intent intent = new Intent("com.oppo.wifi.NOTIFY_GATEWAY_MAC");
        intent.putExtra("iface_name", ifaceName);
        intent.putExtra("gateway_mac", gatewayMac);
        intent.putExtra("gateway_ip", this.mTarget.getHostAddress());
        Log.d(TAG, "sendGatewayMacAddress ifaceName=" + ifaceName + " gatewayMac=" + gatewayMac + " gatewayIp=" + this.mTarget.getHostAddress());
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    public int isDupTargetArp(List<byte[]> gatewayList, byte[] peerMacAddr) {
        if (gatewayList.size() == 0 || peerMacAddr == null) {
            return -1;
        }
        for (int i = 0; i < gatewayList.size(); i++) {
            if (isEqualArray(gatewayList.get(i), peerMacAddr)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEqualArray(byte[] sourceArray, byte[] targetArray) {
        if (sourceArray == null || targetArray == null) {
            return sourceArray == targetArray;
        }
        for (int i = 0; i < targetArray.length; i++) {
            if (sourceArray[i] != targetArray[i]) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i < a.length - 1) {
                sb.append(String.format("%02x:", Byte.valueOf(a[i])));
            } else {
                sb.append(String.format("%02x", Byte.valueOf(a[i])));
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public boolean isValidPeerMac(byte[] mac) {
        if (mac == null) {
            return false;
        }
        if (mac.length != 6 || byteCheck(mac, 0) || byteCheck(mac, 255)) {
            Log.d(TAG, "isValidPeerMac false : " + byteArrayToHex(mac));
            return false;
        } else if (mac[0] % 2 != 0) {
            Log.d(TAG, "isValidPeerMac false : " + byteArrayToHex(mac));
            return false;
        } else if (isEqualArray(mac, this.mHwAddr)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean byteCheck(byte[] addr, int value) {
        for (byte i : addr) {
            if (i != value) {
                return false;
            }
        }
        return true;
    }

    public void close() {
        FileDescriptor fileDescriptor = this.mSocket;
        if (fileDescriptor != null) {
            closeQuietly(fileDescriptor);
            this.mSocket = null;
        }
        synchronized (this.mDupTarget) {
            this.mDupTarget.clear();
        }
    }

    private static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mSocket != null) {
                Log.e(TAG, " OppoArpPeer was finalized without closing");
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
