package android.net.dhcp;

import java.nio.ByteBuffer;

class Dhcp6InfoRequestPacket extends Dhcp6Packet {
    Dhcp6InfoRequestPacket(byte[] transId, byte[] clientMac) {
        super(transId, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac);
    }

    public String toString() {
        return super.toString() + " REQUEST, desired IP " + this.mRequestedIp + ", param list length " + (this.mRequestedParams == null ? 0 : this.mRequestedParams.length);
    }

    public ByteBuffer buildPacket(short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(1500);
        fillInPacket(INADDR_BROADCAST_ROUTER, INADDR_ANY, destUdp, srcUdp, result, (byte) 11);
        result.flip();
        return result;
    }

    void finishPacket(ByteBuffer buffer) {
        Dhcp6Packet.addTlv(buffer, (short) 1, getClientId());
        if (this.mServerIdentifier != null) {
            Dhcp6Packet.addTlv(buffer, (short) 2, this.mServerIdentifier);
        }
        addCommonClientTlvs(buffer);
        Dhcp6Packet.addTlv(buffer, (short) 6, this.mRequestedParams);
    }
}
