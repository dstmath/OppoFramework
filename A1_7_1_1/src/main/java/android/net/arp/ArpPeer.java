package android.net.arp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.net.util.NetdService;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.system.StructTimeval;
import android.util.Log;
import com.android.server.display.DisplayTransformManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import libcore.io.IoBridge;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ArpPeer {
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STA";
    private static final int ARP_LENGTH = 28;
    private static final int ARP_READ_LENGTH = 60;
    private static final int ARP_TYPE = 2054;
    private static final String CHECK_INTERNET_URL = "http://conn1.oppomobile.com/generate_204";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final boolean DBG = false;
    private static final int DUP_ARP_COUNT = 2;
    private static final int ETHERNET_LENGTH = 14;
    private static final int ETHERNET_TYPE = 1;
    private static final String EXTRA_NETWORK_STATE = "network";
    private static final String EXTRA_WIFI_SSID = "ssid";
    private static final long IO_TIMEOUT = 300;
    private static final int IPV4_LENGTH = 4;
    private static final byte[] L2_BROADCAST = null;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final boolean PKT_DBG = false;
    private static final int READ_TIMEOUT = 10000;
    private static final int SLEEP_TIME = 1000;
    private static final String TAG = "ArpPeer";
    private static final int WIFI_CHECK_COUNT = 3;
    private static Context mContext;
    private static int sAttemptCount;
    private static Integer sInterfaceIndex;
    private static boolean sIsArpCheckDone;
    private static Network sNetwork;
    private static Handler sNetworkMonitorHandler;
    private static boolean sNoDupArp;
    private ConnectivityManager mConnMgr;
    private List<byte[]> mDupGatewayList;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private final InetAddress mMyAddr;
    private final byte[] mMyMac;
    private final InetAddress mPeer;
    private FileDescriptor mSocket;
    private WifiManager mWifiMgr;

    private static class NetworkAvaibilityChecker implements Runnable {
        private String mDomain;
        private InetAddress mGateway;
        private Network mNetwork;
        private boolean mNetworkAvailable = false;

        public NetworkAvaibilityChecker(Network network, String domain, InetAddress gateway) {
            this.mNetwork = network;
            this.mDomain = domain;
            this.mGateway = gateway;
        }

        public void run() {
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL(this.mDomain);
                String hostToResolve = url.getHost();
                InetAddress[] addresses = this.mNetwork.getAllByName(hostToResolve);
                if (addresses.length == 1 && addresses[0] != null && addresses[0].equals(this.mGateway)) {
                    Log.d(ArpPeer.TAG, "NetworkAvaibilityChecker IP for " + hostToResolve + "is " + addresses[0]);
                    return;
                }
                httpURLConnection = (HttpURLConnection) this.mNetwork.openConnectionWithoutConnectionPool(url, Proxy.NO_PROXY);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Connection", "Close");
                httpURLConnection.getInputStream();
                int rspCode = httpURLConnection.getResponseCode();
                if (rspCode == DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE && httpURLConnection.getContentLength() == 0) {
                    Log.d(ArpPeer.TAG, "Empty 200 response interpreted as 204 response.");
                    rspCode = 204;
                }
                if (rspCode == DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE && httpURLConnection.getHeaderField("Connection") != null && httpURLConnection.getHeaderField("Connection").equals("Keep-Alive")) {
                    Log.d(ArpPeer.TAG, "response 200 Connection - Alive.");
                    rspCode = 204;
                }
                if (rspCode == 204) {
                    this.mNetworkAvailable = true;
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (IOException e) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable th) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }

        public boolean isAvaiable() {
            return this.mNetworkAvailable;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.arp.ArpPeer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.arp.ArpPeer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.arp.ArpPeer.<clinit>():void");
    }

    public ArpPeer(Context context, String interfaceName, InetAddress myAddr, InetAddress peer) throws SocketException {
        this(interfaceName, myAddr, peer);
        mContext = context;
        this.mConnMgr = (ConnectivityManager) mContext.getSystemService("connectivity");
        this.mWifiMgr = (WifiManager) mContext.getSystemService("wifi");
    }

    public ArpPeer(String interfaceName, InetAddress myAddr, InetAddress peer) throws SocketException {
        this.mMyMac = new byte[6];
        this.mIfaceName = interfaceName;
        if (myAddr == null) {
            myAddr = getIpv4InterfaceAddress();
        }
        this.mMyAddr = myAddr;
        if ((myAddr instanceof Inet6Address) || (peer instanceof Inet6Address)) {
            throw new IllegalArgumentException("IPv6 unsupported");
        }
        this.mPeer = peer;
        initInterface();
        initSocket();
        Log.i(TAG, "ArpPeer in " + interfaceName + ":" + this.mMyAddr + ":" + this.mPeer);
    }

    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            Log.i(TAG, "mac addr:" + byteArrayToHex(this.mHwAddr) + ":" + this.mIface.getIndex());
            this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.getIndex(), L2_BROADCAST);
            sInterfaceIndex = Integer.valueOf(this.mIface.getIndex());
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

    public void setNetworkMonitorHandler(Handler h, int attempt) {
        sNetworkMonitorHandler = h;
        sAttemptCount = attempt;
    }

    private InetAddress getIpv4InterfaceAddress() {
        try {
            Enumeration<InetAddress> ipAddres = NetworkInterface.getByName(this.mIfaceName).getInetAddresses();
            while (ipAddres.hasMoreElements()) {
                InetAddress inetAddress = (InetAddress) ipAddres.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    Log.i(TAG, "Local Source address:" + inetAddress);
                    return inetAddress;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName);
        }
        return Inet4Address.ANY;
    }

    public byte[] doArp(int timeoutMillis) throws ErrnoException {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = this.mPeer.getAddress();
        Log.i(TAG, "My address IP:" + byteArrayToHex(this.mMyAddr.getAddress()));
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        Log.i(TAG, "doArp in " + timeoutMillis);
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
            byte[] socketBuf = new byte[MAX_LENGTH];
            while (SystemClock.elapsedRealtime() < timeout) {
                long duration = timeout - SystemClock.elapsedRealtime();
                Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(duration));
                Log.i(TAG, "Wait ARP reply in " + duration);
                try {
                    int readLen = Os.read(this.mSocket, socketBuf, 0, MAX_LENGTH);
                    if (readLen != 60) {
                        Log.i(TAG, "readLen: " + readLen);
                    }
                    if (readLen >= 42) {
                        byte[] recvBuf = new byte[28];
                        System.arraycopy(socketBuf, 14, recvBuf, 0, 28);
                        if (recvBuf[0] == (byte) 0 && recvBuf[1] == (byte) 1 && recvBuf[2] == (byte) 8 && recvBuf[3] == (byte) 0 && recvBuf[4] == (byte) 6 && recvBuf[5] == (byte) 4 && recvBuf[6] == (byte) 0 && recvBuf[7] == (byte) 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                            byte[] result = new byte[6];
                            System.arraycopy(recvBuf, 8, result, 0, 6);
                            Log.i(TAG, "target mac addr:" + byteArrayToHex(result));
                            return result;
                        }
                    }
                } catch (Exception se) {
                    Log.e(TAG, "ARP read failure: " + se);
                    return null;
                }
            }
            return null;
        } catch (Exception se2) {
            Log.e(TAG, "ARP send failure: " + se2);
            return null;
        }
    }

    public boolean doDupArp(int timeoutMillis) throws ErrnoException {
        int findCount = 0;
        Log.i(TAG, "doDupArp begin");
        sNoDupArp = true;
        sIsArpCheckDone = false;
        sNetworkMonitorHandler = null;
        sAttemptCount = 0;
        try {
            findCount = findDupGateway(timeoutMillis);
        } catch (Exception e) {
            Log.e(TAG, "findDupGateway:" + e);
        }
        if (findCount < 2) {
            Log.i(TAG, "No dup gateway");
            sNetworkMonitorHandler = null;
            sNoDupArp = true;
            return false;
        }
        sNoDupArp = false;
        int i = 0;
        while (i < 3) {
            try {
                Thread.sleep(1000);
                sNetwork = waitActiveWifi();
                if (sNetwork != null) {
                    break;
                }
                i++;
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(TAG, "doDupArp:" + e2);
                return true;
            }
        }
        if (sNetwork == null) {
            Log.e(TAG, "Can't obtain Wi-Fi network");
            close();
            return false;
        }
        i = this.mDupGatewayList.size() - 1;
        while (i >= 0) {
            if (this.mSocket == null) {
                Log.d(TAG, "Empty socket due to Wi-Fi is closed");
                return false;
            }
            byte[] gatewayMac = (byte[]) this.mDupGatewayList.get(i);
            Log.i(TAG, "Configure MAC addr:" + byteArrayToHex(gatewayMac) + " - " + sNetwork);
            updateArpEntry(gatewayMac);
            if (this.mSocket == null || !isNetworkAvaiable()) {
                i--;
            } else {
                Log.i(TAG, "Internet detected in ArpPeer");
                sendNetworkAvailable();
                return true;
            }
        }
        Log.e(TAG, "Can't detect Internet capabilty");
        return false;
    }

    public int findDupGateway(int timeoutMillis) throws ErrnoException {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = this.mPeer.getAddress();
        int findCount = 0;
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        Log.i(TAG, "doDupArp in " + timeoutMillis);
        this.mDupGatewayList = new ArrayList(2);
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
            byte[] socketBuf = new byte[MAX_LENGTH];
            while (SystemClock.elapsedRealtime() < timeout && findCount < 2) {
                long duration = timeout - SystemClock.elapsedRealtime();
                Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(duration));
                Log.i(TAG, "Wait ARP reply in " + duration);
                try {
                    int readLen = Os.read(this.mSocket, socketBuf, 0, MAX_LENGTH);
                    if (readLen != 60) {
                        Log.i(TAG, "readLen: " + readLen);
                    }
                    if (readLen >= 42) {
                        byte[] recvBuf = new byte[28];
                        System.arraycopy(socketBuf, 14, recvBuf, 0, 28);
                        if (recvBuf[0] == (byte) 0 && recvBuf[1] == (byte) 1 && recvBuf[2] == (byte) 8 && recvBuf[3] == (byte) 0 && recvBuf[4] == (byte) 6 && recvBuf[5] == (byte) 4 && recvBuf[6] == (byte) 0 && recvBuf[7] == (byte) 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                            byte[] gatewayMac = new byte[6];
                            System.arraycopy(recvBuf, 8, gatewayMac, 0, 6);
                            Log.i(TAG, "find mac addr:" + byteArrayToHex(gatewayMac));
                            if (isDupGatewayArp(this.mDupGatewayList, gatewayMac) < 0) {
                                Log.i(TAG, "add entry:" + findCount);
                                findCount++;
                                this.mDupGatewayList.add(gatewayMac);
                                if (findCount == 2) {
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
            return findCount;
        } catch (Exception se2) {
            Log.e(TAG, "ARP send failure: " + se2);
            return 0;
        }
    }

    public boolean isRetryByArpStatus() {
        if (sNoDupArp || sIsArpCheckDone) {
            return false;
        }
        sIsArpCheckDone = true;
        return true;
    }

    private static byte[] covertMacAddress(String macAddress) {
        try {
            String[] macAddressParts = macAddress.split(":");
            byte[] macAddressBytes = new byte[6];
            for (int i = 0; i < 6; i++) {
                macAddressBytes[i] = Integer.valueOf(Integer.parseInt(macAddressParts[i], 16)).byteValue();
            }
            return macAddressBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Network waitActiveWifi() {
        try {
            if (!isWifiConnected()) {
                return null;
            }
            for (Network network : this.mConnMgr.getAllNetworks()) {
                NetworkCapabilities nc = this.mConnMgr.getNetworkCapabilities(network);
                if (nc != null && nc.hasTransport(1) && this.mConnMgr.getNetworkInfo(network).isConnected()) {
                    return network;
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getWifiNetwork:" + e);
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
        for (int i = 0; i < targetArray.length; i++) {
            if (sourceArray[i] != targetArray[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWifiConnected() {
        WifiInfo wifiInfo = this.mWifiMgr.getConnectionInfo();
        if (wifiInfo == null) {
            Log.e(TAG, "Empty wifi info");
            return false;
        } else if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            return true;
        } else {
            Log.e(TAG, "SupplicantState is not completed:" + wifiInfo.getSupplicantState());
            return false;
        }
    }

    private boolean isNetworkAvaiable() {
        boolean bAvaiable = false;
        try {
            if (isWifiConnected()) {
                InetAddress.clearDnsCache();
                INetd netd = NetdService.getInstance();
                if (netd != null) {
                    try {
                        netd.resolveFlushCacheForNet(sNetwork.netId);
                    } catch (Exception e) {
                        Log.e(TAG, "resolveFlushCacheForNet:" + e);
                    }
                } else {
                    Log.e(TAG, "No netd service instance available");
                }
                NetworkAvaibilityChecker checker = new NetworkAvaibilityChecker(sNetwork, CHECK_INTERNET_URL, this.mPeer);
                Thread t = new Thread(checker);
                t.start();
                t.join(10000);
                bAvaiable = checker.isAvaiable();
                return bAvaiable;
            }
            Log.e(TAG, "Wi-Fi is not connected");
            close();
            return false;
        } catch (InterruptedException e2) {
            Log.e(TAG, "checkInternetByDns:" + e2);
        }
    }

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            Object[] objArr = new Object[1];
            objArr[0] = Byte.valueOf(b);
            sb.append(String.format("%02x:", objArr));
        }
        return sb.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ef  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateArpEntry(byte[] llAddr) {
        Exception e;
        Throwable th;
        byte[] msg = RtNetlinkNeighborMessage.newNewNeighborMessage(1, this.mPeer, StructNdMsg.NUD_PERMANENT, this.mIface.getIndex(), llAddr);
        int errno = -OsConstants.EPROTO;
        msg[7] = (byte) 5;
        Log.d(TAG, "Update IP addr:" + this.mPeer + ":" + byteArrayToHex(llAddr));
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
                    nlSocket.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            nlSocket = null;
            if (nlSocket != null) {
            }
            throw th;
        }
    }

    public static boolean doArp(String interfaceName, InetAddress myAddr, InetAddress peerAddr, int timeoutMillis) {
        return doArp(interfaceName, myAddr, peerAddr, timeoutMillis, 2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0061 A:{Splitter: B:1:0x0002, ExcHandler: java.net.SocketException (e java.net.SocketException)} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x008c A:{Splitter: B:5:0x000b, ExcHandler: java.net.SocketException (e java.net.SocketException)} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0085  */
    /* JADX WARNING: Missing block: B:25:0x0061, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            android.util.Log.e(TAG, "ARP test initiation failure: " + r5);
     */
    /* JADX WARNING: Missing block: B:28:0x007c, code:
            if (r2 != null) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:29:0x007e, code:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:36:0x008c, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:37:0x008d, code:
            r2 = r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean doArp(String interfaceName, InetAddress myAddr, InetAddress peerAddr, int timeoutMillis, int totalTimes) {
        Exception e;
        Throwable th;
        ArpPeer peer = null;
        try {
            ArpPeer peer2 = new ArpPeer(interfaceName, myAddr, peerAddr);
            int responses = 0;
            int i = 0;
            while (i < totalTimes) {
                try {
                    if (peer2.doArp(timeoutMillis) != null) {
                        responses++;
                    }
                    i++;
                } catch (SocketException e2) {
                } catch (Exception e3) {
                    e = e3;
                    peer = peer2;
                    try {
                        Log.e(TAG, "exception:" + e);
                        if (peer != null) {
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (peer != null) {
                            peer.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    peer = peer2;
                    if (peer != null) {
                    }
                    throw th;
                }
            }
            Log.d(TAG, "ARP test result: " + responses);
            if (responses == totalTimes) {
                if (peer2 != null) {
                    peer2.close();
                }
                return true;
            }
            if (peer2 != null) {
                peer2.close();
            }
            peer = peer2;
            return false;
        } catch (SocketException e4) {
        } catch (Exception e5) {
            e = e5;
            Log.e(TAG, "exception:" + e);
            if (peer != null) {
                peer.close();
            }
            return false;
        }
    }

    public void close() {
        Log.i(TAG, "Close arp");
        if (this.mSocket != null) {
            closeQuietly(this.mSocket);
        }
        this.mSocket = null;
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
                Log.e(TAG, " ArpPeer was finalized without closing");
                close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void sendNetworkAvailable() {
        Log.i(TAG, "sendNetworkAvailable to network monitor");
        sIsArpCheckDone = true;
        if (sNetworkMonitorHandler != null) {
            sNetworkMonitorHandler.sendMessage(sNetworkMonitorHandler.obtainMessage(532486, sAttemptCount, 0));
        }
        try {
            WifiInfo wifiInfo = this.mWifiMgr.getConnectionInfo();
            if (wifiInfo == null) {
                Log.e(TAG, "wifiInfo is null");
            }
            String ssid = wifiInfo.getSSID();
            if (ssid == null) {
                Log.e(TAG, "ssid is null");
                return;
            }
            Intent networkIntent = new Intent(ACTION_WIFI_NETWORK_STATE);
            networkIntent.addFlags(67108864);
            networkIntent.putExtra(EXTRA_WIFI_SSID, ssid);
            networkIntent.putExtra(EXTRA_NETWORK_STATE, true);
            mContext.sendBroadcastAsUser(networkIntent, UserHandle.ALL);
        } catch (Exception e) {
            Log.e(TAG, "sendNetworkAvailable:" + e);
        }
    }
}
