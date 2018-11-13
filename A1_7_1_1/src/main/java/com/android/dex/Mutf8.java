package com.android.dex;

import com.android.dex.util.ByteInput;
import java.io.UTFDataFormatException;

public final class Mutf8 {
    private Mutf8() {
    }

    public static String decode(ByteInput in, char[] out) throws UTFDataFormatException {
        int s = 0;
        while (true) {
            char a = (char) (in.readByte() & 255);
            if (a == 0) {
                return new String(out, 0, s);
            }
            out[s] = a;
            int b;
            int s2;
            if (a < 128) {
                s++;
            } else if ((a & 224) == 192) {
                b = in.readByte() & 255;
                if ((b & 192) != 128) {
                    throw new UTFDataFormatException("bad second byte");
                }
                s2 = s + 1;
                out[s] = (char) (((a & 31) << 6) | (b & 63));
                s = s2;
            } else if ((a & 240) == 224) {
                b = in.readByte() & 255;
                int c = in.readByte() & 255;
                if ((b & 192) == 128 && (c & 192) == 128) {
                    s2 = s + 1;
                    out[s] = (char) ((((a & 15) << 12) | ((b & 63) << 6)) | (c & 63));
                    s = s2;
                }
            } else {
                throw new UTFDataFormatException("bad byte");
            }
        }
        throw new UTFDataFormatException("bad second or third byte");
    }

    private static long countBytes(String s, boolean shortLength) throws UTFDataFormatException {
        long result = 0;
        int length = s.length();
        int i = 0;
        while (i < length) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) {
                result++;
            } else if (ch <= 2047) {
                result += 2;
            } else {
                result += 3;
            }
            if (!shortLength || result <= 65535) {
                i++;
            } else {
                throw new UTFDataFormatException("String more than 65535 UTF bytes long");
            }
        }
        return result;
    }

    public static void encode(byte[] dst, int offset, String s) {
        int length = s.length();
        int i = 0;
        int offset2 = offset;
        while (i < length) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) {
                offset = offset2 + 1;
                dst[offset2] = (byte) ch;
            } else if (ch <= 2047) {
                offset = offset2 + 1;
                dst[offset2] = (byte) (((ch >> 6) & 31) | 192);
                offset2 = offset + 1;
                dst[offset] = (byte) ((ch & 63) | 128);
                offset = offset2;
            } else {
                offset = offset2 + 1;
                dst[offset2] = (byte) (((ch >> 12) & 15) | 224);
                offset2 = offset + 1;
                dst[offset] = (byte) (((ch >> 6) & 63) | 128);
                offset = offset2 + 1;
                dst[offset2] = (byte) ((ch & 63) | 128);
            }
            i++;
            offset2 = offset;
        }
    }

    public static byte[] encode(String s) throws UTFDataFormatException {
        byte[] result = new byte[((int) countBytes(s, true))];
        encode(result, 0, s);
        return result;
    }
}
