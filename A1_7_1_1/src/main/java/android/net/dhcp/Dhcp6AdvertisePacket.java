package android.net.dhcp;

import java.net.Inet6Address;
import java.nio.ByteBuffer;

class Dhcp6AdvertisePacket extends Dhcp6Packet {
    private final Inet6Address mSrcIp;

    Dhcp6AdvertisePacket(byte[] transId, Inet6Address serverIp, Inet6Address requestAddress, byte[] clientMac) {
        super(transId, requestAddress, serverIp, INADDR_ANY, clientMac);
        this.mSrcIp = serverIp;
    }

    public String toString() {
        String s = super.toString();
        String dnsServers = " DNS servers: ";
        for (Inet6Address dnsServer : this.mDnsServers) {
            dnsServers = dnsServers + dnsServer.toString() + " ";
        }
        return s + " ADVERTISE: your new IP " + this.mRequestedIp + ", netmask " + this.mSubnetMask + ", gateway " + this.mGateway + dnsServers + ", lease time " + this.mLeaseTime;
    }

    public ByteBuffer buildPacket(short destUdp, short srcUdp) {
        return null;
    }

    void finishPacket(ByteBuffer buffer) {
    }

    private static final int getInt(Integer v) {
        if (v == null) {
            return 0;
        }
        return v.intValue();
    }
}
