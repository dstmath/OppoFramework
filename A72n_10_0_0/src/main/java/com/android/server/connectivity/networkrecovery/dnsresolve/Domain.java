package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public class Domain implements MessageContent<Domain> {
    private final Collection<String> labels = new ArrayList();

    public Domain() {
    }

    public Domain(String... labels2) {
        this.labels.addAll(Arrays.asList(labels2));
    }

    public static Domain fromQName(String name) {
        return new Domain(name.split("\\."));
    }

    public Collection<String> getLabels() {
        return Collections.unmodifiableCollection(this.labels);
    }

    public String getDomain() {
        return StringUtils.join((Iterator<?>) this.labels.iterator(), '.');
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Domain toBytes(ByteBuffer buf) {
        for (String l : this.labels) {
            Util.writeCharacterString(buf, l.getBytes());
        }
        buf.put((byte) 0);
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Domain fromBytes(ByteBuffer buf) throws IOException {
        this.labels.clear();
        while (true) {
            int labelLength = buf.get() & OppoNfcChipVersion.NONE;
            if (labelLength == 0) {
                break;
            } else if ((labelLength & 192) == 192) {
                int i = ((labelLength & 63) << 8) | (buf.get() & OppoNfcChipVersion.NONE);
                break;
            } else {
                byte[] labelBytes = new byte[labelLength];
                buf.get(labelBytes);
                this.labels.add(new String(labelBytes));
            }
        }
        return this;
    }

    public String toString() {
        return "Domain [labels=" + this.labels + "]";
    }
}
