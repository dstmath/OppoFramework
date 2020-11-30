package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.asm.Opcodes;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Properties;

public class IOUtils {
    public static final char[] ASCII_CHARS = {'0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F', '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1', 'A', '1', 'B', '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2', 'F'};
    public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static final Properties DEFAULT_PROPERTIES = new Properties();
    public static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final char[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    static final char[] DigitTens = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};
    public static final String FASTJSON_COMPATIBLEWITHFIELDNAME = "fastjson.compatibleWithFieldName";
    public static final String FASTJSON_COMPATIBLEWITHJAVABEAN = "fastjson.compatibleWithJavaBean";
    public static final String FASTJSON_PROPERTIES = "fastjson.properties";
    public static final int[] IA = new int[256];
    public static final Charset UTF8 = Charset.forName("UTF-8");
    static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static final boolean[] firstIdentifierFlags = new boolean[256];
    public static final boolean[] identifierFlags = new boolean[256];
    public static final char[] replaceChars = new char[93];
    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    public static final byte[] specicalFlags_doubleQuotes = new byte[Opcodes.IF_ICMPLT];
    public static final boolean[] specicalFlags_doubleQuotesFlags = new boolean[Opcodes.IF_ICMPLT];
    public static final byte[] specicalFlags_singleQuotes = new byte[Opcodes.IF_ICMPLT];
    public static final boolean[] specicalFlags_singleQuotesFlags = new boolean[Opcodes.IF_ICMPLT];

    static {
        for (char c = 0; c < firstIdentifierFlags.length; c = (char) (c + 1)) {
            if (c >= 'A' && c <= 'Z') {
                firstIdentifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                firstIdentifierFlags[c] = true;
            } else if (c == '_' || c == '$') {
                firstIdentifierFlags[c] = true;
            }
        }
        for (char c2 = 0; c2 < identifierFlags.length; c2 = (char) (c2 + 1)) {
            if (c2 >= 'A' && c2 <= 'Z') {
                identifierFlags[c2] = true;
            } else if (c2 >= 'a' && c2 <= 'z') {
                identifierFlags[c2] = true;
            } else if (c2 == '_') {
                identifierFlags[c2] = true;
            } else if (c2 >= '0' && c2 <= '9') {
                identifierFlags[c2] = true;
            }
        }
        try {
            loadPropertiesFromFile();
        } catch (Throwable th) {
        }
        specicalFlags_doubleQuotes[0] = 4;
        specicalFlags_doubleQuotes[1] = 4;
        specicalFlags_doubleQuotes[2] = 4;
        specicalFlags_doubleQuotes[3] = 4;
        specicalFlags_doubleQuotes[4] = 4;
        specicalFlags_doubleQuotes[5] = 4;
        specicalFlags_doubleQuotes[6] = 4;
        specicalFlags_doubleQuotes[7] = 4;
        specicalFlags_doubleQuotes[8] = 1;
        specicalFlags_doubleQuotes[9] = 1;
        specicalFlags_doubleQuotes[10] = 1;
        specicalFlags_doubleQuotes[11] = 4;
        specicalFlags_doubleQuotes[12] = 1;
        specicalFlags_doubleQuotes[13] = 1;
        specicalFlags_doubleQuotes[34] = 1;
        specicalFlags_doubleQuotes[92] = 1;
        specicalFlags_singleQuotes[0] = 4;
        specicalFlags_singleQuotes[1] = 4;
        specicalFlags_singleQuotes[2] = 4;
        specicalFlags_singleQuotes[3] = 4;
        specicalFlags_singleQuotes[4] = 4;
        specicalFlags_singleQuotes[5] = 4;
        specicalFlags_singleQuotes[6] = 4;
        specicalFlags_singleQuotes[7] = 4;
        specicalFlags_singleQuotes[8] = 1;
        specicalFlags_singleQuotes[9] = 1;
        specicalFlags_singleQuotes[10] = 1;
        specicalFlags_singleQuotes[11] = 4;
        specicalFlags_singleQuotes[12] = 1;
        specicalFlags_singleQuotes[13] = 1;
        specicalFlags_singleQuotes[92] = 1;
        specicalFlags_singleQuotes[39] = 1;
        for (int i = 14; i <= 31; i++) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }
        for (int i2 = 127; i2 < 160; i2++) {
            specicalFlags_doubleQuotes[i2] = 4;
            specicalFlags_singleQuotes[i2] = 4;
        }
        for (int i3 = 0; i3 < 161; i3++) {
            specicalFlags_doubleQuotesFlags[i3] = specicalFlags_doubleQuotes[i3] != 0;
            specicalFlags_singleQuotesFlags[i3] = specicalFlags_singleQuotes[i3] != 0;
        }
        replaceChars[0] = '0';
        replaceChars[1] = '1';
        replaceChars[2] = '2';
        replaceChars[3] = '3';
        replaceChars[4] = '4';
        replaceChars[5] = '5';
        replaceChars[6] = '6';
        replaceChars[7] = '7';
        replaceChars[8] = 'b';
        replaceChars[9] = 't';
        replaceChars[10] = 'n';
        replaceChars[11] = 'v';
        replaceChars[12] = 'f';
        replaceChars[13] = 'r';
        replaceChars[34] = '\"';
        replaceChars[39] = '\'';
        replaceChars[47] = '/';
        replaceChars[92] = '\\';
        Arrays.fill(IA, -1);
        int iS = CA.length;
        for (int i4 = 0; i4 < iS; i4++) {
            IA[CA[i4]] = i4;
        }
        IA[61] = 0;
    }

    public static String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
        }
        return prop == null ? DEFAULT_PROPERTIES.getProperty(name) : prop;
    }

    public static void loadPropertiesFromFile() {
        InputStream imputStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            /* class com.alibaba.fastjson.util.IOUtils.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public InputStream run() {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl != null) {
                    return cl.getResourceAsStream(IOUtils.FASTJSON_PROPERTIES);
                }
                return ClassLoader.getSystemResourceAsStream(IOUtils.FASTJSON_PROPERTIES);
            }
        });
        if (imputStream != null) {
            try {
                DEFAULT_PROPERTIES.load(imputStream);
                imputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
            }
        }
    }

    public static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p *= 10;
        }
        return 19;
    }

    public static void getChars(long i, int index, char[] buf) {
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i > 2147483647L) {
            long q = i / 100;
            int r = (int) (i - (((q << 6) + (q << 5)) + (q << 2)));
            i = q;
            int charPos2 = charPos - 1;
            buf[charPos2] = DigitOnes[r];
            charPos = charPos2 - 1;
            buf[charPos] = DigitTens[r];
        }
        int i2 = (int) i;
        while (i2 >= 65536) {
            int q2 = i2 / 100;
            int r2 = i2 - (((q2 << 6) + (q2 << 5)) + (q2 << 2));
            i2 = q2;
            int charPos3 = charPos - 1;
            buf[charPos3] = DigitOnes[r2];
            charPos = charPos3 - 1;
            buf[charPos] = DigitTens[r2];
        }
        do {
            int q22 = (52429 * i2) >>> 19;
            charPos--;
            buf[charPos] = digits[i2 - ((q22 << 3) + (q22 << 1))];
            i2 = q22;
        } while (i2 != 0);
        if (sign != 0) {
            buf[charPos - 1] = sign;
        }
    }

    public static void getChars(int i, int index, char[] buf) {
        int p = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i >= 65536) {
            int q = i / 100;
            int r = i - (((q << 6) + (q << 5)) + (q << 2));
            i = q;
            int p2 = p - 1;
            buf[p2] = DigitOnes[r];
            p = p2 - 1;
            buf[p] = DigitTens[r];
        }
        do {
            int q2 = (52429 * i) >>> 19;
            p--;
            buf[p] = digits[i - ((q2 << 3) + (q2 << 1))];
            i = q2;
        } while (i != 0);
        if (sign != 0) {
            buf[p - 1] = sign;
        }
    }

    public static void getChars(byte b, int index, char[] buf) {
        int i = b;
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        do {
            int q = (52429 * i) >>> 19;
            charPos--;
            buf[charPos] = digits[i - ((q << 3) + (q << 1))];
            i = q;
        } while (i != 0);
        if (sign != 0) {
            buf[charPos - 1] = sign;
        }
    }

    public static int stringSize(int x) {
        int i = 0;
        while (x > sizeTable[i]) {
            i++;
        }
        return i + 1;
    }

    public static void decode(CharsetDecoder charsetDecoder, ByteBuffer byteBuf, CharBuffer charByte) {
        try {
            CoderResult cr = charsetDecoder.decode(byteBuf, charByte, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            CoderResult cr2 = charsetDecoder.flush(charByte);
            if (!cr2.isUnderflow()) {
                cr2.throwException();
            }
        } catch (CharacterCodingException x) {
            throw new JSONException("utf8 decode error, " + x.getMessage(), x);
        }
    }

    public static boolean firstIdentifier(char ch) {
        return ch < firstIdentifierFlags.length && firstIdentifierFlags[ch];
    }

    public static boolean isIdent(char ch) {
        return ch < identifierFlags.length && identifierFlags[ch];
    }

    public static byte[] decodeBase64(char[] chars, int offset, int charsLen) {
        int sepCnt;
        int j = 0;
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx = offset;
        int eIx = (offset + charsLen) - 1;
        while (sIx < eIx && IA[chars[sIx]] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[chars[eIx]] < 0) {
            eIx--;
        }
        int pad = chars[eIx] == '=' ? chars[eIx + -1] == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (charsLen > 76) {
            sepCnt = (chars[76] == '\r' ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] bytes = new byte[len];
        int d = 0;
        int cc = 0;
        int eLen = (len / 3) * 3;
        while (d < eLen) {
            int sIx2 = sIx + 1;
            int sIx3 = sIx2 + 1;
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int i = (IA[chars[sIx]] << 18) | (IA[chars[sIx2]] << 12) | (IA[chars[sIx3]] << 6) | IA[chars[sIx4]];
            int d2 = d + 1;
            bytes[d] = (byte) (i >> 16);
            int d3 = d2 + 1;
            bytes[d2] = (byte) (i >> 8);
            int d4 = d3 + 1;
            bytes[d3] = (byte) i;
            if (sepCnt > 0 && (cc = cc + 1) == 19) {
                sIx5 += 2;
                cc = 0;
            }
            sIx = sIx5;
            d = d4;
        }
        if (d < len) {
            int i2 = 0;
            while (sIx <= eIx - pad) {
                i2 |= IA[chars[sIx]] << (18 - (j * 6));
                j++;
                sIx++;
            }
            int r = 16;
            while (d < len) {
                bytes[d] = (byte) (i2 >> r);
                r -= 8;
                d++;
            }
        }
        return bytes;
    }

    public static byte[] decodeBase64(String chars, int offset, int charsLen) {
        int sepCnt;
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx = offset;
        int eIx = (offset + charsLen) - 1;
        while (sIx < eIx && IA[chars.charAt(sIx)] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[chars.charAt(eIx)] < 0) {
            eIx--;
        }
        int pad = chars.charAt(eIx) == '=' ? chars.charAt(eIx + -1) == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (charsLen > 76) {
            sepCnt = (chars.charAt(76) == '\r' ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] bytes = new byte[len];
        int d = 0;
        int cc = 0;
        int eLen = (len / 3) * 3;
        while (d < eLen) {
            int sIx2 = sIx + 1;
            int sIx3 = sIx2 + 1;
            int i = (IA[chars.charAt(sIx)] << 18) | (IA[chars.charAt(sIx2)] << 12);
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int i2 = (IA[chars.charAt(sIx3)] << 6) | i | IA[chars.charAt(sIx4)];
            int d2 = d + 1;
            bytes[d] = (byte) (i2 >> 16);
            int d3 = d2 + 1;
            bytes[d2] = (byte) (i2 >> 8);
            int d4 = d3 + 1;
            bytes[d3] = (byte) i2;
            if (sepCnt > 0 && (cc = cc + 1) == 19) {
                sIx5 += 2;
                cc = 0;
            }
            d = d4;
            sIx = sIx5;
        }
        if (d < len) {
            int i3 = 0;
            int j = 0;
            while (sIx <= eIx - pad) {
                i3 |= IA[chars.charAt(sIx)] << (18 - (j * 6));
                j++;
                sIx++;
            }
            int r = 16;
            while (d < len) {
                bytes[d] = (byte) (i3 >> r);
                r -= 8;
                d++;
            }
        }
        return bytes;
    }

    public static byte[] decodeBase64(String s) {
        int sepCnt;
        int sLen = s.length();
        int j = 0;
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx = 0;
        int eIx = sLen - 1;
        while (sIx < eIx && IA[s.charAt(sIx) & 255] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[s.charAt(eIx) & 255] < 0) {
            eIx--;
        }
        int pad = s.charAt(eIx) == '=' ? s.charAt(eIx + -1) == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (sLen > 76) {
            sepCnt = (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int d = 0;
        int cc = 0;
        int eLen = (len / 3) * 3;
        while (d < eLen) {
            int sIx2 = sIx + 1;
            int sIx3 = sIx2 + 1;
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int i = (IA[s.charAt(sIx)] << 18) | (IA[s.charAt(sIx2)] << 12) | (IA[s.charAt(sIx3)] << 6) | IA[s.charAt(sIx4)];
            int d2 = d + 1;
            dArr[d] = (byte) (i >> 16);
            int d3 = d2 + 1;
            dArr[d2] = (byte) (i >> 8);
            int d4 = d3 + 1;
            dArr[d3] = (byte) i;
            if (sepCnt > 0 && (cc = cc + 1) == 19) {
                sIx5 += 2;
                cc = 0;
            }
            sIx = sIx5;
            d = d4;
        }
        if (d < len) {
            int i2 = 0;
            while (sIx <= eIx - pad) {
                i2 |= IA[s.charAt(sIx)] << (18 - (j * 6));
                j++;
                sIx++;
            }
            int r = 16;
            while (d < len) {
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d++;
            }
        }
        return dArr;
    }

    /* JADX INFO: Multiple debug info for r12v3 char: [D('offset' int), D('c' char)] */
    /* JADX INFO: Multiple debug info for r7v8 int: [D('dp' int), D('uc' int)] */
    public static int encodeUTF8(char[] chars, int offset, int len, byte[] bytes) {
        int dp;
        int uc;
        int sl = offset + len;
        int dp2 = 0;
        int dlASCII = Math.min(len, bytes.length) + 0;
        while (dp2 < dlASCII && chars[offset] < 128) {
            bytes[dp2] = (byte) chars[offset];
            dp2++;
            offset++;
        }
        while (offset < sl) {
            int offset2 = offset + 1;
            char c = chars[offset];
            if (c < 128) {
                dp = dp2 + 1;
                bytes[dp2] = (byte) c;
            } else {
                if (c < 2048) {
                    int dp3 = dp2 + 1;
                    bytes[dp2] = (byte) (192 | (c >> 6));
                    dp2 = dp3 + 1;
                    bytes[dp3] = (byte) ((c & '?') | Opcodes.IOR);
                } else if (c < 55296 || c >= 57344) {
                    int dp4 = dp2 + 1;
                    bytes[dp2] = (byte) (224 | (c >> '\f'));
                    int dp5 = dp4 + 1;
                    bytes[dp4] = (byte) ((63 & (c >> 6)) | Opcodes.IOR);
                    dp = dp5 + 1;
                    bytes[dp5] = (byte) ((c & '?') | Opcodes.IOR);
                } else {
                    int ip = offset2 - 1;
                    if (c < 55296 || c >= 56320) {
                        if (c < 56320 || c >= 57344) {
                            uc = c;
                        } else {
                            bytes[dp2] = 63;
                            offset = offset2;
                            dp2++;
                        }
                    } else if (sl - ip < 2) {
                        uc = -1;
                    } else {
                        char d = chars[ip + 1];
                        if (d < 56320 || d >= 57344) {
                            bytes[dp2] = 63;
                            offset = offset2;
                            dp2++;
                        } else {
                            uc = ((c << '\n') + d) - 56613888;
                        }
                    }
                    if (uc < 0) {
                        bytes[dp2] = 63;
                        dp2++;
                    } else {
                        int dp6 = dp2 + 1;
                        bytes[dp2] = (byte) (240 | (uc >> 18));
                        int dp7 = dp6 + 1;
                        bytes[dp6] = (byte) (((uc >> 12) & 63) | Opcodes.IOR);
                        int dp8 = dp7 + 1;
                        bytes[dp7] = (byte) ((63 & (uc >> 6)) | Opcodes.IOR);
                        dp2 = dp8 + 1;
                        bytes[dp8] = (byte) ((uc & 63) | Opcodes.IOR);
                        offset2++;
                    }
                }
                offset = offset2;
            }
            offset = offset2;
            dp2 = dp;
        }
        return dp2;
    }

    /* JADX INFO: Multiple debug info for r12v3 byte: [D('sp' int), D('b1' int)] */
    /* JADX INFO: Multiple debug info for r3v1 byte: [D('sp' int), D('b2' int)] */
    /* JADX INFO: Multiple debug info for r4v5 byte: [D('sp' int), D('b3' int)] */
    /* JADX INFO: Multiple debug info for r5v2 byte: [D('sp' int), D('b4' int)] */
    /* JADX INFO: Multiple debug info for r3v2 byte: [D('sp' int), D('b2' int)] */
    /* JADX INFO: Multiple debug info for r4v8 byte: [D('sp' int), D('b3' int)] */
    /* JADX INFO: Multiple debug info for r3v3 byte: [D('sp' int), D('b2' int)] */
    public static int decodeUTF8(byte[] sa, int sp, int len, char[] da) {
        int sl = sp + len;
        int dp = 0;
        int dlASCII = Math.min(len, da.length);
        while (dp < dlASCII && sa[sp] >= 0) {
            da[dp] = (char) sa[sp];
            dp++;
            sp++;
        }
        while (sp < sl) {
            int sp2 = sp + 1;
            byte b = sa[sp];
            if (b >= 0) {
                da[dp] = (char) b;
                sp = sp2;
                dp++;
            } else if ((b >> 5) != -2 || (b & 30) == 0) {
                if ((b >> 4) == -2) {
                    if (sp2 + 1 >= sl) {
                        return -1;
                    }
                    int sp3 = sp2 + 1;
                    byte b2 = sa[sp2];
                    int sp4 = sp3 + 1;
                    byte b3 = sa[sp3];
                    if ((b == -32 && (b2 & 224) == 128) || (b2 & 192) != 128 || (b3 & 192) != 128) {
                        return -1;
                    }
                    char c = (char) (((b << 12) ^ (b2 << 6)) ^ (-123008 ^ b3));
                    if (c >= 55296 && c < 57344) {
                        return -1;
                    }
                    da[dp] = c;
                    sp = sp4;
                    dp++;
                } else if ((b >> 3) != -2 || sp2 + 2 >= sl) {
                    return -1;
                } else {
                    int sp5 = sp2 + 1;
                    byte b4 = sa[sp2];
                    int sp6 = sp5 + 1;
                    byte b5 = sa[sp5];
                    int sp7 = sp6 + 1;
                    byte b6 = sa[sp6];
                    int uc = (((b << 18) ^ (b4 << 12)) ^ (b5 << 6)) ^ (3678080 ^ b6);
                    if ((b4 & 192) != 128 || (b5 & 192) != 128 || (b6 & 192) != 128 || uc < 65536 || uc >= 1114112) {
                        return -1;
                    }
                    int dp2 = dp + 1;
                    da[dp] = (char) ((uc >>> 10) + 55232);
                    dp = dp2 + 1;
                    da[dp2] = (char) ((uc & 1023) + 56320);
                    sp = sp7;
                }
            } else if (sp2 >= sl) {
                return -1;
            } else {
                int sp8 = sp2 + 1;
                byte b7 = sa[sp2];
                if ((b7 & 192) != 128) {
                    return -1;
                }
                da[dp] = (char) (((b << 6) ^ b7) ^ 3968);
                sp = sp8;
                dp++;
            }
        }
        return dp;
    }

    public static String readAll(Reader reader) {
        StringBuilder buf = new StringBuilder();
        try {
            char[] chars = new char[2048];
            while (true) {
                int len = reader.read(chars, 0, chars.length);
                if (len < 0) {
                    return buf.toString();
                }
                buf.append(chars, 0, len);
            }
        } catch (Exception ex) {
            throw new JSONException("read string from reader error", ex);
        }
    }

    public static boolean isValidJsonpQueryParam(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (!(ch == '.' || isIdent(ch))) {
                return false;
            }
        }
        return true;
    }
}
