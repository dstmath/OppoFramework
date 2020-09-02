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

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.util.Iterator<?>, char):java.lang.String
     arg types: [java.util.Iterator<java.lang.String>, int]
     candidates:
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.lang.Iterable<?>, char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.lang.Iterable<?>, java.lang.String):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.util.Iterator<?>, java.lang.String):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(byte[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(char[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(double[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(float[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(int[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(long[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.lang.Object[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.lang.Object[], java.lang.String):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(short[], char):java.lang.String
      com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils.join(java.util.Iterator<?>, char):java.lang.String */
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
                byte b = ((labelLength & 63) << 8) | (buf.get() & OppoNfcChipVersion.NONE);
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
