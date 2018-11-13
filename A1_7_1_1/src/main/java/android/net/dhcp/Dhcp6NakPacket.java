package android.net.dhcp;

import java.net.Inet6Address;
import java.nio.ByteBuffer;

class Dhcp6NakPacket extends Dhcp6Packet {
    Dhcp6NakPacket(byte[] transId, Inet6Address serverIp, Inet6Address requestAddress) {
        super(transId, requestAddress, serverIp, INADDR_ANY, null);
    }

    public String toString() {
        return super.toString();
    }

    public ByteBuffer buildPacket(short destUdp, short srcUdp) {
        return null;
    }

    void finishPacket(ByteBuffer buffer) {
    }
}
