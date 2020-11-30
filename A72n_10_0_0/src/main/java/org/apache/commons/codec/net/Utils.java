package org.apache.commons.codec.net;

import org.apache.commons.codec.DecoderException;

/* access modifiers changed from: package-private */
public class Utils {
    Utils() {
    }

    static int digit16(byte b) throws DecoderException {
        int i = Character.digit((char) b, 16);
        if (i != -1) {
            return i;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Invalid URL encoding: not a valid digit (radix 16): ");
        stringBuffer.append((int) b);
        throw new DecoderException(stringBuffer.toString());
    }
}
