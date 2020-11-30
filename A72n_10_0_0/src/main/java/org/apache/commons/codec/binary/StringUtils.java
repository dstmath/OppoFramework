package org.apache.commons.codec.binary;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.CharEncoding;

public class StringUtils {
    public static byte[] getBytesIso8859_1(String string) {
        return getBytesUnchecked(string, CharEncoding.ISO_8859_1);
    }

    public static byte[] getBytesUsAscii(String string) {
        return getBytesUnchecked(string, CharEncoding.US_ASCII);
    }

    public static byte[] getBytesUtf16(String string) {
        return getBytesUnchecked(string, CharEncoding.UTF_16);
    }

    public static byte[] getBytesUtf16Be(String string) {
        return getBytesUnchecked(string, CharEncoding.UTF_16BE);
    }

    public static byte[] getBytesUtf16Le(String string) {
        return getBytesUnchecked(string, CharEncoding.UTF_16LE);
    }

    public static byte[] getBytesUtf8(String string) {
        return getBytesUnchecked(string, "UTF-8");
    }

    public static byte[] getBytesUnchecked(String string, String charsetName) {
        if (string == null) {
            return null;
        }
        try {
            return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw newIllegalStateException(charsetName, e);
        }
    }

    private static IllegalStateException newIllegalStateException(String charsetName, UnsupportedEncodingException e) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(charsetName);
        stringBuffer.append(": ");
        stringBuffer.append(e);
        return new IllegalStateException(stringBuffer.toString());
    }

    public static String newString(byte[] bytes, String charsetName) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw newIllegalStateException(charsetName, e);
        }
    }

    public static String newStringIso8859_1(byte[] bytes) {
        return newString(bytes, CharEncoding.ISO_8859_1);
    }

    public static String newStringUsAscii(byte[] bytes) {
        return newString(bytes, CharEncoding.US_ASCII);
    }

    public static String newStringUtf16(byte[] bytes) {
        return newString(bytes, CharEncoding.UTF_16);
    }

    public static String newStringUtf16Be(byte[] bytes) {
        return newString(bytes, CharEncoding.UTF_16BE);
    }

    public static String newStringUtf16Le(byte[] bytes) {
        return newString(bytes, CharEncoding.UTF_16LE);
    }

    public static String newStringUtf8(byte[] bytes) {
        return newString(bytes, "UTF-8");
    }
}
