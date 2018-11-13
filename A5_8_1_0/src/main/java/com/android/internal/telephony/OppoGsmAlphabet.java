package com.android.internal.telephony;

import android.text.TextUtils;
import android.util.Log;
import android.view.textclassifier.logging.SmartSelectionEventTracker.SelectionEvent;
import com.android.internal.midi.MidiConstants;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;

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
        byte[] ret;
        try {
            ret = new byte[countGsmSeptets(s, true, 1)];
            GsmAlphabet.stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
        } catch (EncodeException e) {
            try {
                byte[] temp = s.getBytes("utf-16be");
                ret = new byte[(temp.length / 2)];
                judge(temp, 0, temp.length);
                ret = ucs2ToAlphaField(temp, 0, temp.length, 0, ret);
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "unsurport encoding.", ex);
                return null;
            }
        }
        return ret;
    }

    public static void judge(byte[] src, int srcOff, int srcLen) {
        min = SelectionEvent.OUT_OF_BOUNDS;
        max = 0;
        if (srcLen >= 2) {
            for (int i = 0; i < srcLen; i += 2) {
                if (src[srcOff + i] != (byte) 0) {
                    int temp = ((src[srcOff + i] << 8) & 65280) | (src[(srcOff + i) + 1] & 255);
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
                } else if ((src[(srcOff + i) + 1] & 128) != 0) {
                    max = min + 130;
                    break;
                }
            }
        }
        if (max - min >= 129) {
            is0X80coding = true;
            is0X82coding = false;
            is0X81coding = false;
        } else if (((byte) (min & 128)) == ((byte) (max & 128))) {
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
        if (is0X80coding) {
            dest = new byte[(srcLen + 1)];
            dest[destOff] = MidiConstants.STATUS_NOTE_OFF;
            System.arraycopy(src, 0, dest, 1, srcLen);
            return dest;
        }
        if (is0X81coding) {
            dest = new byte[((srcLen / 2) + 3)];
            dest[destOff + 1] = (byte) (srcLen / 2);
            dest[destOff] = (byte) -127;
            min &= 32640;
            dest[destOff + 2] = (byte) ((min >> 7) & 255);
            outOff = destOff + 3;
        } else if (is0X82coding) {
            dest = new byte[((srcLen / 2) + 4)];
            dest[destOff + 1] = (byte) (srcLen / 2);
            dest[destOff] = (byte) -126;
            dest[destOff + 2] = (byte) ((min >> 8) & 255);
            dest[destOff + 3] = (byte) (min & 255);
            outOff = destOff + 4;
        }
        for (int i = 0; i < srcLen; i += 2) {
            if (src[srcOff + i] == (byte) 0) {
                dest[outOff] = (byte) (src[(srcOff + i) + 1] & 127);
            } else {
                dest[outOff] = (byte) (((((src[srcOff + i] << 8) & 65280) | (src[(srcOff + i) + 1] & 255)) - min) | 128);
            }
            outOff++;
        }
        return dest;
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
        int count = 0;
        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            count += GsmAlphabet.countGsmSeptets(s.charAt(charIndex), throwsException);
        }
        return count;
    }

    public static boolean isChinese(char c) {
        UnicodeBlock ub = UnicodeBlock.of(c);
        if (ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == UnicodeBlock.GENERAL_PUNCTUATION || ub == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public static boolean containChinese(String strName) {
        if (TextUtils.isEmpty(strName)) {
            return false;
        }
        for (char c : strName.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
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

    public static boolean isThai(String s) {
        int sz = s.length();
        boolean ret = true;
        boolean hasThai = false;
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            if (c < '!' || c > '~') {
                if (c < 3585 || c > 3673) {
                    ret = false;
                    break;
                }
                hasThai = true;
            }
        }
        return ret ? hasThai : false;
    }

    public static boolean isRussian(String s) {
        int sz = s.length();
        boolean ret = true;
        boolean hasRussian = false;
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            if (c < '!' || c > '~') {
                if (c < 1024 || c > 1279) {
                    ret = false;
                    break;
                }
                hasRussian = true;
            }
        }
        return ret ? hasRussian : false;
    }

    public static boolean enableEncodeTo0x81(String s) {
        if (containChinese(s)) {
            return false;
        }
        if (isThai(s)) {
            return true;
        }
        if (isRussian(s)) {
            return true;
        }
        return false;
    }

    public static byte[] encodeTo0x81(String src) {
        byte base = (byte) 0;
        int len = src.length();
        byte[] b0x81 = new byte[(len + 3)];
        b0x81[0] = (byte) -127;
        b0x81[1] = (byte) len;
        int i = 0;
        while (i < len) {
            String temp = src.substring(i, i + 1);
            try {
                byte data;
                byte[] bytes = temp.getBytes("utf-16be");
                if (isEnglish(temp)) {
                    data = bytes[1];
                } else {
                    if (base == (byte) 0) {
                        base = (byte) (bytes[0] << 1);
                        b0x81[2] = base;
                    }
                    data = bytes[1];
                    if ((data & 128) == 0) {
                        data = (byte) (data | 128);
                    }
                }
                b0x81[i + 3] = data;
                i++;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "encodeTo0x81() : unsurport encoding of " + temp, e);
                return null;
            }
        }
        return b0x81;
    }
}
