package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class A extends RecordData<A> {
    private InetAddress address;

    public A() {
    }

    public A(InetAddress address2) {
        this.address = address2;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public A toBytes(ByteBuffer buf) throws IOException {
        buf.put(this.address.getAddress());
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public RecordData<A> fromBytes(ByteBuffer buf) throws IOException {
        byte[] b = new byte[4];
        buf.get(b);
        this.address = InetAddress.getByAddress(b);
        return this;
    }

    public String toString() {
        return "A [address=" + this.address + "]";
    }
}
