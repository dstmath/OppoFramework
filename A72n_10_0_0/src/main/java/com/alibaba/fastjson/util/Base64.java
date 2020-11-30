package com.alibaba.fastjson.util;

import java.util.Arrays;

public class Base64 {
    public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static final int[] IA = new int[256];

    static {
        Arrays.fill(IA, -1);
        int iS = CA.length;
        for (int i = 0; i < iS; i++) {
            IA[CA[i]] = i;
        }
        IA[61] = 0;
    }

    public static byte[] decodeFast(char[] chars, int offset, int charsLen) {
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

    public static byte[] decodeFast(String chars, int offset, int charsLen) {
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

    public static byte[] decodeFast(String s) {
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
}
