package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Cname extends RecordData<Cname> {
    private final Domain domain;

    public Cname() {
        this(new Domain());
    }

    public Cname(Domain domain2) {
        this.domain = domain2;
    }

    public Domain getDomain() {
        return this.domain;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Cname toBytes(ByteBuffer buf) throws IOException {
        this.domain.toBytes(buf);
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public RecordData<Cname> fromBytes(ByteBuffer buf) throws IOException {
        this.domain.fromBytes(buf);
        return this;
    }

    public String toString() {
        return "Cname [domain=" + this.domain + "]";
    }
}
