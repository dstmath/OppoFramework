package com.android.server.connectivity.gatewayconflict;

import android.content.Context;
import android.net.IDnsResolver;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.net.util.NetworkConstants;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.system.OsConstants;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import libcore.io.IoUtils;
import oppo.util.OppoStatistics;

public class OppoGatewayDetector extends OppoArpPeer {
    private static final int DUP_ARP_COUNT = 2;
    private static final int DUP_GATEWAY_LIST_SIZE = 3;
    private static final long IO_TIMEOUT = 300;
    private static final String KEY_NETWORK = "key_network";
    private static final String TAG = "OppoGatewayDetector";
    private static final String WIFI_DUPLICATE_GATEWAY = "wifi_duplicate_gateway";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    public Inet4Address mIPv4Gateway = null;
    private Inet4Address mIPv4Self = null;
    private String mInterfaceName = null;
    private int mNextAvaibleMacIndex = 0;
    private boolean mProbeFinished = false;

    public OppoGatewayDetector(Context context, NetworkAgentInfo networkAgentInfo, OppoArpPeer.ArpPeerChangeCallback callback) {
        super(context, networkAgentInfo, callback, 3);
        setPreConditionForGatewayCheck(networkAgentInfo.linkProperties);
    }

    public boolean hasDupGateway() {
        return this.mDupTarget.size() >= 2;
    }

    public void prepareNextAvailbeGateway() {
        if (this.mIPv4Gateway != null) {
            byte[] mac = null;
            boolean needClearDNS = false;
            if (hasLeftAvaibleGateway()) {
                synchronized (this.mDupTarget) {
                    mac = (byte[]) this.mDupTarget.get(this.mNextAvaibleMacIndex);
                    needClearDNS = this.mNextAvaibleMacIndex > 0;
                    this.mNextAvaibleMacIndex++;
                }
            }
            if (needClearDNS) {
                InetAddress.clearDnsCache();
                clearDnsCache();
            }
            if (mac != null) {
                updateArpEntry(mac, StructNdMsg.NUD_PERMANENT);
            }
        }
    }

    private void clearDnsCache() {
        IDnsResolver dnsresolver = getDnsResolver();
        if (dnsresolver != null) {
            try {
                dnsresolver.resolveFlushCacheForNet(this.mNetwork.netId);
            } catch (RemoteException | ServiceSpecificException e) {
                Log.d(TAG, "Exception destroying network: ");
            }
        }
    }

    private static IDnsResolver getDnsResolver() {
        return IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
    }

    public void restoreLastGatewayState() {
        byte[] mac = null;
        synchronized (this.mDupTarget) {
            if (this.mDupTarget.size() >= 1) {
                mac = (byte[]) this.mDupTarget.get(this.mDupTarget.size() - 1);
            }
        }
        if (mac != null) {
            updateArpEntry(mac, 2);
        }
    }

    public boolean hasLeftAvaibleGateway() {
        synchronized (this.mDupTarget) {
            boolean z = false;
            if (this.mProbeFinished) {
                if (this.mDupTarget.size() > 1 && this.mNextAvaibleMacIndex < this.mDupTarget.size()) {
                    z = true;
                }
                return z;
            }
            if (this.mDupTarget.size() > 0 && this.mNextAvaibleMacIndex < this.mDupTarget.size()) {
                z = true;
            }
            return z;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.connectivity.gatewayconflict.OppoArpPeer
    public void onProbeFinished() {
        tryToRestoreGatewayState();
    }

    private void tryToRestoreGatewayState() {
        if (isOriginalNetworkConnected()) {
            restoreGatewayState();
        }
        synchronized (this.mDupTarget) {
            this.mProbeFinished = true;
        }
    }

    private void restoreGatewayState() {
        byte[] mac = null;
        synchronized (this.mDupTarget) {
            if (this.mDupTarget.size() == 1) {
                this.mNextAvaibleMacIndex = 0;
                mac = (byte[]) this.mDupTarget.get(this.mNextAvaibleMacIndex);
            }
        }
        if (mac != null) {
            updateArpEntry(mac, 2);
        }
    }

    public byte[] fetchGatewayMacFromRoute() {
        String macAddress = null;
        BufferedReader reader = null;
        byte[] macFromRoute = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            reader2.readLine();
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split("[ ]+");
                if (tokens.length >= 6) {
                    String ip = tokens[0];
                    String mac = tokens[3];
                    if (this.mTarget.getHostAddress().equals(ip)) {
                        macAddress = mac;
                        break;
                    }
                }
            }
            if (macAddress != null) {
                macFromRoute = macAddressToByteArray(macAddress);
                if (isValidPeerMac(macFromRoute)) {
                    synchronized (this.mDupTarget) {
                        if (isDupTargetArp(this.mDupTarget, macFromRoute) < 0) {
                            this.mDupTarget.add(macFromRoute);
                        }
                    }
                } else {
                    macFromRoute = null;
                }
            }
            Log.d(TAG, "fetchGatewayMacFromRoute gatway " + this.mTarget + "  mac " + macAddress);
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            Log.d(TAG, "Could not open /proc/net/arp to lookup mac address");
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e3) {
            Log.d(TAG, "Could not read /proc/net/arp to lookup mac address");
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
        return macFromRoute;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e9  */
    private boolean updateArpEntry(byte[] llAddr, short newState) {
        Throwable th;
        Exception e;
        String errmsg;
        if (this.mIface == null) {
            return false;
        }
        byte[] msg = RtNetlinkNeighborMessage.newNewNeighborMessage(1, this.mTarget, newState, this.mIface.getIndex(), llAddr);
        int i = -OsConstants.EPROTO;
        FileDescriptor fd = null;
        msg[7] = 5;
        Log.d(TAG, "Update IP addr:" + this.mTarget + ":" + byteArrayToHex(llAddr) + " state " + ((int) newState));
        try {
            FileDescriptor fd2 = NetlinkSocket.forProto(OsConstants.NETLINK_ROUTE);
            try {
                NetlinkSocket.connectToKernel(fd2);
                NetlinkSocket.sendMessage(fd2, msg, 0, msg.length, 300);
                ByteBuffer bytes = NetlinkSocket.recvMessage(fd2, NetworkConstants.ETHER_MTU, 300);
                NetlinkMessage response = NetlinkMessage.parse(bytes);
                if (response == null || !(response instanceof NetlinkErrorMessage) || ((NetlinkErrorMessage) response).getNlMsgError() == null) {
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
                if (fd2 != null) {
                    IoUtils.closeQuietly(fd2);
                }
            } catch (Exception e2) {
                e = e2;
                fd = fd2;
                try {
                    Log.e(TAG, "Error ", e);
                    if (fd == null) {
                    }
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    fd2 = fd;
                }
            } catch (Throwable th3) {
                th = th3;
                if (fd2 != null) {
                    IoUtils.closeQuietly(fd2);
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "Error ", e);
            if (fd == null) {
                IoUtils.closeQuietly(fd);
            }
            return true;
        }
        return true;
    }

    private void setPreConditionForGatewayCheck(LinkProperties linkProperties) {
        if (linkProperties != null) {
            String interfaceName = linkProperties.getInterfaceName();
            Inet4Address ipv4address = null;
            Inet4Address ipv4gateway = null;
            Iterator<RouteInfo> it = linkProperties.getRoutes().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RouteInfo routeInfo = it.next();
                if (routeInfo.hasGateway()) {
                    InetAddress gateway = routeInfo.getGateway();
                    if (gateway instanceof Inet4Address) {
                        ipv4gateway = (Inet4Address) gateway;
                        break;
                    }
                }
            }
            Iterator it2 = linkProperties.getAddresses().iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                InetAddress address = (InetAddress) it2.next();
                if (address instanceof Inet4Address) {
                    ipv4address = (Inet4Address) address;
                    break;
                }
            }
            this.mIPv4Gateway = ipv4gateway;
            this.mIPv4Self = ipv4address;
            this.mInterfaceName = interfaceName;
        }
    }

    public boolean probeGateway() {
        Inet4Address inet4Address = this.mIPv4Gateway;
        if (inet4Address != null) {
            return doDupArp(this.mInterfaceName, this.mIPv4Self, inet4Address);
        }
        return false;
    }

    public boolean needToCheckGateway() {
        return this.mIPv4Gateway != null;
    }

    public void setDuplicateGatewayStatics() {
        if (this.mNetworkAgentInfo != null) {
            HashMap<String, String> map = new HashMap<>();
            String ssid = this.mNetworkAgentInfo.networkInfo.getExtraInfo();
            StringBuilder message = new StringBuilder();
            message.append(this.mNetworkAgentInfo.network + ";");
            message.append(ssid + ";");
            map.put(KEY_NETWORK, message.toString());
            OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_DUPLICATE_GATEWAY, map, false);
        }
    }
}
