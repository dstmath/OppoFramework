package android.net.arp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.net.util.NetdService;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.system.StructTimeval;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import libcore.io.IoBridge;
import libcore.util.HexEncoding;

public class OppoArpPeer {
    private static final byte[] ANY_MAC_BYTES = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final String ANY_MAC_STR = "any";
    public static final int ARP_DUP_RESPONSE_TIMEOUT = 5000;
    public static final int ARP_FIRST_RESPONSE_TIMEOUT = 2000;
    private static final int ARP_LENGTH = 28;
    private static final int ARP_READ_LENGTH = 60;
    private static final int ARP_TYPE = 2054;
    private static final int DUP_ARP_COUNT = 2;
    private static final int ETHERNET_LENGTH = 14;
    private static final int ETHERNET_TYPE = 1;
    private static final long IO_TIMEOUT = 300;
    private static final int IPV4_LENGTH = 4;
    private static final byte[] L2_BROADCAST = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final String TAG = "OppoArpPeer";
    private ArpPeerChangeCallback mCallback = null;
    private Context mContext;
    private byte[] mCurrentFixedMac = null;
    private List<byte[]> mDupGatewayList = new ArrayList(2);
    private Inet4Address mGateway;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private Inet4Address mMyAddr;
    private byte[] mMyMac = new byte[6];
    private Network mNetwork;
    private int mNextAvaibleMacIndex = 0;
    private boolean mProbeFinished = false;
    private FileDescriptor mSocket;
    private MyTask mTask = null;

    public interface ArpPeerChangeCallback {
        void onArpReponseChanged(int i);
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        /* synthetic */ MyTask(OppoArpPeer this$0, MyTask -this1) {
            this();
        }

        private MyTask() {
        }

        protected Void doInBackground(Void... params) {
            try {
                OppoArpPeer.this.findDupGateway(OppoArpPeer.ARP_DUP_RESPONSE_TIMEOUT);
                OppoArpPeer.this.tryToRestoreGatewayState();
            } catch (ErrnoException e) {
            }
            return null;
        }
    }

    public OppoArpPeer(Context context, Network network, ArpPeerChangeCallback callback) {
        this.mContext = context;
        this.mNetwork = network;
        this.mCallback = callback;
    }

    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            Log.i(TAG, "mac addr:" + byteArrayToHex(this.mHwAddr) + ":" + this.mIface.getIndex());
            this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.getIndex(), L2_BROADCAST);
            this.mInterfaceBroadcastAddr.sll_protocol = (short) 2054;
            return true;
        } catch (SocketException e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName);
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0021 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0021, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0022, code:
            android.util.Log.e(TAG, "Error creating packet socket", r1);
     */
    /* JADX WARNING: Missing block: B:6:0x002c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initSocket() {
        try {
            this.mSocket = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_ARP);
            Os.bind(this.mSocket, new PacketSocketAddress((short) OsConstants.ETH_P_ARP, this.mIface.getIndex()));
            return true;
        } catch (Exception e) {
        }
    }

    private Inet4Address getIpv4InterfaceAddress() {
        try {
            Enumeration<InetAddress> ipAddres = NetworkInterface.getByName(this.mIfaceName).getInetAddresses();
            while (ipAddres.hasMoreElements()) {
                InetAddress inetAddress = (InetAddress) ipAddres.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    Log.i(TAG, "Local Source address:" + inetAddress);
                    return (Inet4Address) inetAddress;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName);
        }
        return (Inet4Address) Inet4Address.ANY;
    }

    public boolean doDupArp(String iface, Inet4Address myAddress, Inet4Address target) {
        this.mIfaceName = iface;
        if (myAddress == null) {
            myAddress = getIpv4InterfaceAddress();
        }
        this.mMyAddr = myAddress;
        this.mGateway = target;
        try {
            initInterface();
            initSocket();
            if (this.mTask != null) {
                this.mTask.cancel(true);
            }
            this.mTask = new MyTask(this, null);
            this.mTask.execute(new Void[0]);
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "doDupArp ", exception);
            return false;
        }
    }

    public boolean hasDupGateway() {
        return this.mDupGatewayList.size() >= 2;
    }

    public void prepareNextAvailbeGateway() {
        byte[] mac = null;
        boolean needClearDNS = false;
        synchronized (this.mDupGatewayList) {
            if (hasLeftAvaibleGateway()) {
                mac = (byte[]) this.mDupGatewayList.get(this.mNextAvaibleMacIndex);
                needClearDNS = this.mNextAvaibleMacIndex > 0;
                this.mNextAvaibleMacIndex++;
            }
        }
        if (needClearDNS) {
            InetAddress.clearDnsCache();
            INetd netd = NetdService.getInstance();
            if (netd == null || this.mNetwork == null) {
                Log.e(TAG, "No netd service instance available");
            } else {
                try {
                    netd.resolveFlushCacheForNet(this.mNetwork.netId);
                } catch (Exception e) {
                    Log.e(TAG, "resolveFlushCacheForNet:" + e);
                }
            }
        }
        if (mac != null) {
            updateArpEntry(mac, StructNdMsg.NUD_PERMANENT);
        }
    }

    private boolean hasLeftAvaibleGateway() {
        boolean z = true;
        boolean z2 = false;
        if (this.mProbeFinished) {
            if (this.mDupGatewayList.size() <= 1 || this.mNextAvaibleMacIndex >= this.mDupGatewayList.size()) {
                z = false;
            }
            return z;
        }
        if (this.mDupGatewayList.size() > 0 && this.mNextAvaibleMacIndex < this.mDupGatewayList.size()) {
            z2 = true;
        }
        return z2;
    }

    private void tryToRestoreGatewayState() {
        if (isOriginalNetworkConnected()) {
            restoreGatwawayState();
        }
        synchronized (this.mDupGatewayList) {
            this.mProbeFinished = true;
        }
    }

    private boolean isOriginalNetworkConnected() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(this.mNetwork);
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

    private void restoreGatwawayState() {
        byte[] bArr = null;
        synchronized (this.mDupGatewayList) {
            if (this.mDupGatewayList.size() == 1) {
                this.mNextAvaibleMacIndex = 0;
                bArr = (byte[]) this.mDupGatewayList.get(this.mNextAvaibleMacIndex);
            }
        }
        if (bArr != null) {
            updateArpEntry(bArr, (short) 2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0093 A:{SYNTHETIC, Splitter: B:38:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00a9 A:{SYNTHETIC, Splitter: B:47:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00b2 A:{SYNTHETIC, Splitter: B:52:0x00b2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] fetchGatewayMacFromRoute() {
        Throwable th;
        String macAddress = null;
        BufferedReader reader = null;
        byte[] macFromRoute = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            try {
                String readLine = reader2.readLine();
                while (true) {
                    readLine = reader2.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] tokens = readLine.split("[ ]+");
                    if (tokens.length >= 6) {
                        String ip = tokens[0];
                        String mac = tokens[3];
                        if (this.mGateway.getHostAddress().equals(ip)) {
                            macAddress = mac;
                            break;
                        }
                    }
                }
                if (macAddress != null) {
                    macFromRoute = macAddressToByteArray(macAddress);
                    if (isValidMac(macFromRoute)) {
                        synchronized (this.mDupGatewayList) {
                            if (isDupGatewayArp(this.mDupGatewayList, macFromRoute) < 0) {
                                this.mDupGatewayList.add(macFromRoute);
                            }
                        }
                    } else {
                        macFromRoute = null;
                    }
                }
                Log.d(TAG, "fetchGatewayMacFromRoute gatway " + this.mGateway + "  mac " + macAddress);
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e2) {
                reader = reader2;
                try {
                    Log.d(TAG, "Could not open /proc/net/arp to lookup mac address");
                    if (reader != null) {
                    }
                    return macFromRoute;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e3) {
                reader = reader2;
                Log.d(TAG, "Could not read /proc/net/arp to lookup mac address");
                if (reader != null) {
                }
                return macFromRoute;
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            Log.d(TAG, "Could not open /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            return macFromRoute;
        } catch (IOException e7) {
            Log.d(TAG, "Could not read /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            return macFromRoute;
        }
        return macFromRoute;
    }

    private static byte[] macAddressToByteArray(String macStr) {
        if (TextUtils.isEmpty(macStr) || ANY_MAC_STR.equals(macStr)) {
            return ANY_MAC_BYTES;
        }
        String cleanMac = macStr.replace(":", "");
        if (cleanMac.length() == 12) {
            return HexEncoding.decode(cleanMac.toCharArray(), false);
        }
        throw new IllegalArgumentException("invalid mac string length: " + cleanMac);
    }

    private int findDupGateway(int timeoutMillis) throws ErrnoException {
        ByteBuffer buf = ByteBuffer.allocate(1500);
        byte[] desiredIp = this.mGateway.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        Log.i(TAG, "findDupGateway in " + timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(L2_BROADCAST);
        buf.put(this.mHwAddr);
        buf.putShort((short) 2054);
        buf.putShort((short) 1);
        buf.putShort((short) OsConstants.ETH_P_IP);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort((short) 1);
        buf.put(this.mHwAddr);
        buf.put(this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        try {
            Os.sendto(this.mSocket, buf.array(), 0, buf.limit(), 0, this.mInterfaceBroadcastAddr);
            byte[] socketBuf = new byte[1500];
            while (SystemClock.elapsedRealtime() < timeout && this.mDupGatewayList.size() < 2) {
                Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(timeout - SystemClock.elapsedRealtime()));
                try {
                    if (Os.read(this.mSocket, socketBuf, 0, 1500) >= 42) {
                        byte[] recvBuf = new byte[28];
                        System.arraycopy(socketBuf, 14, recvBuf, 0, 28);
                        if (recvBuf[0] == (byte) 0 && recvBuf[1] == (byte) 1 && recvBuf[2] == (byte) 8 && recvBuf[3] == (byte) 0 && recvBuf[4] == (byte) 6 && recvBuf[5] == (byte) 4 && recvBuf[6] == (byte) 0 && recvBuf[7] == (byte) 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                            byte[] gatewayMac = new byte[6];
                            System.arraycopy(recvBuf, 8, gatewayMac, 0, 6);
                            boolean newArpReponse = false;
                            if (isValidMac(gatewayMac)) {
                                synchronized (this.mDupGatewayList) {
                                    if (isDupGatewayArp(this.mDupGatewayList, gatewayMac) < 0) {
                                        newArpReponse = true;
                                        Log.i(TAG, "add entry:" + byteArrayToHex(gatewayMac));
                                        this.mDupGatewayList.add(gatewayMac);
                                    }
                                }
                                if (newArpReponse && this.mCallback != null) {
                                    this.mCallback.onArpReponseChanged(this.mDupGatewayList.size());
                                }
                                if (this.mDupGatewayList.size() == 2) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                } catch (Exception se) {
                    Log.e(TAG, "ARP read failure: " + se);
                    return 0;
                }
            }
            return this.mDupGatewayList.size();
        } catch (Exception se2) {
            Log.e(TAG, "ARP send failure: " + se2);
            return 0;
        }
    }

    private int isDupGatewayArp(List<byte[]> gatewayList, byte[] gatewayMacAddr) {
        if (gatewayList.size() == 0 || gatewayMacAddr == null) {
            return -1;
        }
        for (int i = 0; i < gatewayList.size(); i++) {
            if (isEqualArray((byte[]) gatewayList.get(i), gatewayMacAddr)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEqualArray(byte[] sourceArray, byte[] targetArray) {
        boolean z = true;
        if (sourceArray == null || targetArray == null) {
            if (sourceArray != targetArray) {
                z = false;
            }
            return z;
        }
        for (int i = 0; i < targetArray.length; i++) {
            if (sourceArray[i] != targetArray[i]) {
                return false;
            }
        }
        return true;
    }

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i < a.length - 1) {
                sb.append(String.format("%02x:", new Object[]{Byte.valueOf(a[i])}));
            } else {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(a[i])}));
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00fc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateArpEntry(byte[] llAddr, short newState) {
        Exception e;
        Throwable th;
        byte[] msg = RtNetlinkNeighborMessage.newNewNeighborMessage(1, this.mGateway, newState, this.mIface.getIndex(), llAddr);
        int errno = -OsConstants.EPROTO;
        msg[7] = (byte) 5;
        Log.d(TAG, "Update IP addr:" + this.mGateway + ":" + byteArrayToHex(llAddr) + " state " + newState);
        NetlinkSocket nlSocket;
        try {
            nlSocket = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
            try {
                nlSocket.connectToKernel();
                nlSocket.sendMessage(msg, 0, msg.length, 300);
                ByteBuffer bytes = nlSocket.recvMessage(300);
                NetlinkMessage response = NetlinkMessage.parse(bytes);
                if (response == null || !(response instanceof NetlinkErrorMessage) || ((NetlinkErrorMessage) response).getNlMsgError() == null) {
                    String errmsg;
                    if (response == null) {
                        bytes.position(0);
                        errmsg = "raw bytes: " + NetlinkConstants.hexify(bytes);
                    } else {
                        errmsg = response.toString();
                    }
                    Log.e(TAG, "Errmsg=" + errmsg);
                } else if (((NetlinkErrorMessage) response).getNlMsgError().error != 0) {
                    Log.e(TAG, "Errmsg=" + response.toString());
                }
                if (nlSocket != null) {
                    nlSocket.close();
                }
            } catch (Exception e2) {
                e = e2;
            }
        } catch (Exception e3) {
            e = e3;
            nlSocket = null;
            try {
                Log.e(TAG, "Error ", e);
                if (nlSocket != null) {
                    nlSocket.close();
                }
            } catch (Throwable th2) {
                th = th2;
                if (nlSocket != null) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            nlSocket = null;
            if (nlSocket != null) {
                nlSocket.close();
            }
            throw th;
        }
    }

    private boolean isValidMac(byte[] mac) {
        if (mac == null) {
            return false;
        }
        if (mac.length != 6 || byteCheck(mac, 0) || byteCheck(mac, 255)) {
            Log.d(TAG, "isValidMac false : " + byteArrayToHex(mac));
            return false;
        } else if (mac[0] % 2 == 0) {
            return true;
        } else {
            Log.d(TAG, "isValidMac false : " + byteArrayToHex(mac));
            return false;
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
        if (this.mSocket != null) {
            closeQuietly(this.mSocket);
            this.mSocket = null;
        }
        synchronized (this.mDupGatewayList) {
            this.mNextAvaibleMacIndex = 0;
            this.mDupGatewayList.clear();
        }
    }

    private static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mSocket != null) {
                Log.e(TAG, " OppoArpPeer was finalized without closing");
                close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }
}
