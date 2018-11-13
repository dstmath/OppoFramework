package android.net.dhcp;

import java.nio.ByteBuffer;

class Dhcp6SolicitPacket extends Dhcp6Packet {
    Dhcp6SolicitPacket(byte[] transId, byte[] clientMac) {
        super(transId, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac);
    }

    public String toString() {
        return super.toString() + " SOLICIT broadcast";
    }

    public ByteBuffer buildPacket(short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(1500);
        fillInPacket(INADDR_BROADCAST_ROUTER, INADDR_ANY, destUdp, srcUdp, result, (byte) 1);
        result.flip();
        return result;
    }

    void finishPacket(ByteBuffer buffer) {
        Dhcp6Packet.addTlv(buffer, (short) 1, getClientId());
        addCommonClientTlvs(buffer);
        Dhcp6Packet.addTlv(buffer, (short) 6, this.mRequestedParams);
    }
}
