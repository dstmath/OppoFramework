package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.IOException;
import java.nio.ByteBuffer;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public class Util {
    public static void writeCharacterString(ByteBuffer buf, byte[] bytes) {
        if (bytes.length <= 254) {
            buf.put((byte) bytes.length);
            buf.put(bytes);
        }
    }

    public static byte[] parseCharacterString(ByteBuffer buf, int maxLength) throws IOException {
        int length = buf.get(buf.position()) & OppoNfcChipVersion.NONE;
        if (length + 1 > maxLength) {
            throw new IOException("Tried to read " + (length + 1) + " bytes but only " + maxLength + " available.");
        } else if (length + 1 <= buf.remaining()) {
            return parseCharacterString(buf);
        } else {
            throw new IOException("Tried to read " + (length + 1) + " bytes but only " + buf.remaining() + " bytes are in the buffer.");
        }
    }

    private static byte[] parseCharacterString(ByteBuffer buf) {
        byte[] bytes = new byte[(buf.get() & OppoNfcChipVersion.NONE)];
        buf.get(bytes);
        return bytes;
    }
}
