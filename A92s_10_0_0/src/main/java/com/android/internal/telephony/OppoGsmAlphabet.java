package com.android.internal.telephony;

import android.util.Log;
import java.io.UnsupportedEncodingException;

public class OppoGsmAlphabet {
    private static final String TAG = "GSM";
    private static boolean is0X80coding = false;
    private static boolean is0X81coding = false;
    private static boolean is0X82coding = false;
    private static int max;
    private static int min;

    public static byte[] stringToGsm8BitOrUCSPackedForADN(String s) {
        if (s == null) {
            return null;
        }
        try {
            byte[] ret = new byte[countGsmSeptets(s, true, 1)];
            GsmAlphabet.stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
            return ret;
        } catch (EncodeException e) {
            try {
                byte[] temp = s.getBytes("utf-16be");
                judge(temp, 0, temp.length);
                return ucs2ToAlphaField(temp, 0, temp.length, 0, new byte[(temp.length / 2)]);
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "unsurport encoding.", ex);
                return null;
            }
        }
    }

    public static boolean isEnglish(String s) {
        int sz = s.length();
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            if (c < '!' || c > '~') {
                return false;
            }
        }
        return true;
    }

    public static void judge(byte[] src, int srcOff, int srcLen) {
        min = 32767;
        max = 0;
        if (srcLen > 2) {
            int i = 0;
            while (true) {
                if (i >= srcLen) {
                    break;
                }
                if (src[srcOff + i] != 0) {
                    int temp = ((src[srcOff + i] << 8) & 65280) | (src[srcOff + i + 1] & 255);
                    if (temp < 0) {
                        max = min + 130;
                        break;
                    }
                    if (min > temp) {
                        min = temp;
                    }
                    if (max < temp) {
                        max = temp;
                    }
                }
                i += 2;
            }
        }
        int i2 = max;
        int i3 = min;
        if (i2 - i3 >= 129) {
            is0X80coding = true;
            is0X82coding = false;
            is0X81coding = false;
        } else if (((byte) (i3 & 128)) == ((byte) (i2 & 128))) {
            is0X81coding = true;
            is0X82coding = false;
            is0X80coding = false;
        } else {
            is0X82coding = true;
            is0X81coding = false;
            is0X80coding = false;
        }
    }

    public static byte[] ucs2ToAlphaField(byte[] src, int srcOff, int srcLen, int destOff, byte[] dest) {
        int outOff = 0;
        if (!is0X80coding) {
            if (is0X81coding) {
                dest = new byte[((srcLen / 2) + 3)];
                dest[destOff + 1] = (byte) (srcLen / 2);
                dest[destOff] = -127;
                min &= 32640;
                dest[destOff + 2] = (byte) ((min >> 7) & 255);
                outOff = destOff + 3;
            } else if (is0X82coding) {
                dest = new byte[((srcLen / 2) + 4)];
                dest[destOff + 1] = (byte) (srcLen / 2);
                dest[destOff] = -126;
                int i = min;
                dest[destOff + 2] = (byte) ((i >> 8) & 255);
                dest[destOff + 3] = (byte) (i & 255);
                outOff = destOff + 4;
            }
            for (int i2 = 0; i2 < srcLen; i2 += 2) {
                if (src[srcOff + i2] == 0) {
                    dest[outOff] = (byte) (src[srcOff + i2 + 1] & Byte.MAX_VALUE);
                } else {
                    dest[outOff] = (byte) (((((src[srcOff + i2] << 8) & 65280) | (src[(srcOff + i2) + 1] & 255)) - min) | 128);
                }
                outOff++;
            }
            return dest;
        }
        byte[] dest2 = new byte[(srcLen + 1)];
        dest2[destOff] = Byte.MIN_VALUE;
        System.arraycopy(src, 0, dest2, 1, srcLen);
        return dest2;
    }

    public static boolean enableToEncode0X80() {
        return is0X80coding;
    }

    public static boolean enableToEncode0X81() {
        return is0X81coding;
    }

    public static boolean enableToEncode0X82() {
        return is0X82coding;
    }

    public static int countGsmSeptets(CharSequence s, boolean throwsException, int rfu) throws EncodeException {
        int sz = s.length();
        int count = 0;
        for (int charIndex = 0; charIndex < sz; charIndex++) {
            count += GsmAlphabet.countGsmSeptets(s.charAt(charIndex), throwsException);
        }
        return count;
    }
}
