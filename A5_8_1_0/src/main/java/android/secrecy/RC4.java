package android.secrecy;

import android.text.format.DateFormat;
import java.nio.ByteBuffer;

public class RC4 {
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, 'b', 'c', DateFormat.DATE, 'e', 'f'};

    public static void encrypt(byte[] sbox, byte[] data) {
        int i = 0;
        int j = 0;
        for (int d = 0; d < data.length; d++) {
            i = (i + 1) % 256;
            j = ((sbox[i] & 255) + j) % 256;
            byte tmp = sbox[i];
            sbox[i] = sbox[j];
            sbox[j] = tmp;
            data[d] = (byte) (data[d] ^ sbox[((sbox[i] & 255) + (sbox[j] & 255)) % 256]);
        }
    }

    static void encryptLog(byte[] sbox, byte[]... logs) {
        int i = 0;
        int j = 0;
        for (byte[] data : logs) {
            for (int d = 0; d < data.length; d++) {
                i = (i + 1) % 256;
                j = ((sbox[i] & 255) + j) % 256;
                byte tmp = sbox[i];
                sbox[i] = sbox[j];
                sbox[j] = tmp;
                data[d] = (byte) (data[d] ^ sbox[((sbox[i] & 255) + (sbox[j] & 255)) % 256]);
            }
        }
    }

    static void initSbox(byte[] sbox) {
        for (int i = 0; i < 256; i++) {
            sbox[i] = (byte) i;
        }
    }

    public static void mixSbox(byte[] sbox, byte[] key) {
        initSbox(sbox);
        int j = 0;
        int len = key.length;
        for (int i = 0; i < 256; i++) {
            j = (((sbox[i] & 255) + j) + (key[i % len] & 255)) % 256;
            byte tmp = sbox[i];
            sbox[i] = sbox[j];
            sbox[j] = tmp;
        }
    }

    public static byte[] decodeHexRC4(String data) throws IllegalArgumentException {
        if ((data.length() & 1) == 0) {
            return decodeHex(data);
        }
        byte[] key = new byte[256];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) i;
        }
        return key;
    }

    public static byte[] decodeHex(String data) {
        int len = data.length();
        if ((len & 1) != 0) {
            throw new IllegalArgumentException();
        }
        byte[] key = new byte[(len >> 1)];
        int i = 0;
        int j = 0;
        while (j < len) {
            int digit = Character.digit(data.charAt(j), 16);
            if (digit == -1) {
                throw new IllegalArgumentException();
            }
            int f = digit << 4;
            j++;
            digit = Character.digit(data.charAt(j), 16);
            if (digit == -1) {
                throw new IllegalArgumentException();
            }
            j++;
            key[i] = (byte) ((f | digit) & 255);
            i++;
        }
        return key;
    }

    public static String encodeHex(byte[] data) {
        int len = data.length;
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            sb.append(DIGITS_LOWER[(data[i] & 240) >>> 4]);
            sb.append(DIGITS_LOWER[data[i] & 15]);
        }
        return sb.toString();
    }

    public static String encodeLog(byte[]... logs) {
        byte[] log;
        int i = 0;
        String begin = "βεηiпΞ";
        String end = "Ξэпд";
        int len = 0;
        for (byte[] log2 : logs) {
            len += log2.length;
        }
        StringBuilder sb = new StringBuilder(((len * 2) + begin.length()) + end.length());
        sb.append(begin);
        int length = logs.length;
        while (i < length) {
            log2 = logs[i];
            for (int i2 = 0; i2 < log2.length; i2++) {
                sb.append(DIGITS_LOWER[(log2[i2] & 240) >>> 4]);
                sb.append(DIGITS_LOWER[log2[i2] & 15]);
            }
            i++;
        }
        sb.append(end);
        return sb.toString();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }
}
