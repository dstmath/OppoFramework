package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class DnsTransport {
    public abstract Message sendQuery(Message message) throws IOException;

    /* access modifiers changed from: protected */
    public byte[] serializeMessage(Message requestMessage, int maxPacketSize) throws IOException {
        ByteBuffer requestBuffer = ByteBuffer.allocate(maxPacketSize);
        requestMessage.toBytes(requestBuffer);
        requestBuffer.flip();
        byte[] messageBytes = new byte[requestBuffer.limit()];
        requestBuffer.get(messageBytes);
        return messageBytes;
    }
}
