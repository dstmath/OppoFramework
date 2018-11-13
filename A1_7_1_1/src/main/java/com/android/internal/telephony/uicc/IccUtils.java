package com.android.internal.telephony.uicc;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.telephony.Rlog;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.GsmAlphabet;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.UnsupportedEncodingException;

public class IccUtils {
    private static final boolean DBG = true;
    static final String LOG_TAG = "IccUtils";

    public static String bcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            int i2;
            int v = data[i] & 15;
            ret.append((char) (v < 10 ? v + 48 : (v - 10) + 65));
            v = (data[i] >> 4) & 15;
            if (v < 10) {
                i2 = v + 48;
            } else {
                i2 = (v - 10) + 65;
            }
            ret.append((char) i2);
        }
        return ret.toString();
    }

    public static String bchToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            ret.append("0123456789abcdef".charAt(data[i] & 15));
            ret.append("0123456789abcdef".charAt((data[i] >> 4) & 15));
        }
        return ret.toString();
    }

    public static String cdmaBcdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length);
        int count = 0;
        int i = offset;
        while (count < length) {
            int v = data[i] & 15;
            if (v > 9) {
                v = 0;
            }
            ret.append((char) (v + 48));
            count++;
            if (count == length) {
                break;
            }
            v = (data[i] >> 4) & 15;
            if (v > 9) {
                v = 0;
            }
            ret.append((char) (v + 48));
            count++;
            i++;
        }
        return ret.toString();
    }

    public static int gsmBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & CallFailCause.CALL_BARRED) <= 144) {
            ret = (b >> 4) & 15;
        }
        if ((b & 15) <= 9) {
            return ret + ((b & 15) * 10);
        }
        return ret;
    }

    public static int cdmaBcdByteToInt(byte b) {
        int ret = 0;
        if ((b & CallFailCause.CALL_BARRED) <= 144) {
            ret = ((b >> 4) & 15) * 10;
        }
        if ((b & 15) <= 9) {
            return ret + (b & 15);
        }
        return ret;
    }

    public static String adnStringFieldToString(byte[] data, int offset, int length) {
        if (length == 0) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        if (length >= 1 && data[offset] == Byte.MIN_VALUE) {
            String str = null;
            try {
                str = new String(data, offset + 1, ((length - 1) / 2) * 2, "utf-16be");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            if (str != null) {
                int ucslen = str.length();
                while (ucslen > 0 && str.charAt(ucslen - 1) == 65535) {
                    ucslen--;
                }
                return str.substring(0, ucslen);
            }
        }
        boolean isucs2 = false;
        char base = 0;
        int len = 0;
        if (length >= 3 && data[offset] == (byte) -127) {
            len = data[offset + 1] & 255;
            if (len > length - 3) {
                len = length - 3;
            }
            base = (char) ((data[offset + 2] & 255) << 7);
            offset += 3;
            isucs2 = true;
        } else if (length >= 4 && data[offset] == (byte) -126) {
            len = data[offset + 1] & 255;
            if (len > length - 4) {
                len = length - 4;
            }
            base = (char) (((data[offset + 2] & 255) << 8) | (data[offset + 3] & 255));
            offset += 4;
            isucs2 = true;
        }
        if (isucs2) {
            StringBuilder ret = new StringBuilder();
            while (len > 0) {
                if (data[offset] < (byte) 0) {
                    ret.append((char) ((data[offset] & CallFailCause.INTERWORKING_UNSPECIFIED) + base));
                    offset++;
                    len--;
                }
                int count = 0;
                while (count < len && data[offset + count] >= (byte) 0) {
                    count++;
                }
                ret.append(GsmAlphabet.gsm8BitUnpackedToString(data, offset, count));
                offset += count;
                len -= count;
            }
            return ret.toString();
        }
        Resources resource = Resources.getSystem();
        String defaultCharset = UsimPBMemInfo.STRING_NOT_SET;
        try {
            defaultCharset = resource.getString(17039436);
        } catch (NotFoundException e) {
        }
        return GsmAlphabet.gsm8BitUnpackedToString(data, offset, length, defaultCharset.trim());
    }

    static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToBytes(String s) {
        if (s == null) {
            return null;
        }
        int sz = s.length();
        byte[] ret = new byte[(sz / 2)];
        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s.charAt(i + 1)));
        }
        return ret;
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append("0123456789abcdef".charAt((bytes[i] >> 4) & 15));
            ret.append("0123456789abcdef".charAt(bytes[i] & 15));
        }
        return ret.toString();
    }

    public static String networkNameToString(byte[] data, int offset, int length) {
        if ((data[offset] & 128) != 128 || length < 1) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        String ret;
        switch ((data[offset] >>> 4) & 7) {
            case 0:
                ret = GsmAlphabet.gsm7BitPackedToString(data, offset + 1, (((length - 1) * 8) - (data[offset] & 7)) / 7);
                break;
            case 1:
                try {
                    ret = new String(data, offset + 1, length - 1, CharacterSets.MIMENAME_UTF_16);
                    break;
                } catch (UnsupportedEncodingException ex) {
                    ret = UsimPBMemInfo.STRING_NOT_SET;
                    Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
                    break;
                }
            default:
                ret = UsimPBMemInfo.STRING_NOT_SET;
                break;
        }
        if ((data[offset] & 64) != 0) {
        }
        return ret;
    }

    public static Bitmap parseToBnW(byte[] data, int length) {
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int bitIndex = 7;
        byte currentByte = (byte) 0;
        int pixelIndex = 0;
        while (pixelIndex < numOfPixels) {
            int valueIndex2;
            if (pixelIndex % 8 == 0) {
                valueIndex2 = valueIndex + 1;
                currentByte = data[valueIndex];
                bitIndex = 7;
            } else {
                valueIndex2 = valueIndex;
            }
            int pixelIndex2 = pixelIndex + 1;
            int bitIndex2 = bitIndex - 1;
            pixels[pixelIndex] = bitToRGB((currentByte >> bitIndex) & 1);
            bitIndex = bitIndex2;
            pixelIndex = pixelIndex2;
            valueIndex = valueIndex2;
        }
        if (pixelIndex != numOfPixels) {
            Rlog.e(LOG_TAG, "parse end and size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }

    private static int bitToRGB(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency) {
        int[] resultArray;
        int width = data[0] & 255;
        int valueIndex = 1 + 1;
        int height = data[1] & 255;
        int valueIndex2 = valueIndex + 1;
        int bits = data[valueIndex] & 255;
        valueIndex = valueIndex2 + 1;
        int colorNumber = data[valueIndex2] & 255;
        valueIndex2 = valueIndex + 1;
        valueIndex = valueIndex2 + 1;
        int[] colorIndexArray = getCLUT(data, ((data[valueIndex] & 255) << 8) | (data[valueIndex2] & 255), colorNumber);
        if (transparency) {
            colorIndexArray[colorNumber - 1] = 0;
        }
        if (8 % bits == 0) {
            resultArray = mapTo2OrderBitColor(data, valueIndex, width * height, colorIndexArray, bits);
        } else {
            resultArray = mapToNon2OrderBitColor(data, valueIndex, width * height, colorIndexArray, bits);
        }
        return Bitmap.createBitmap(resultArray, width, height, Config.RGB_565);
    }

    private static int[] mapTo2OrderBitColor(byte[] data, int valueIndex, int length, int[] colorArray, int bits) {
        if (8 % bits != 0) {
            Rlog.e(LOG_TAG, "not event number of color");
            return mapToNon2OrderBitColor(data, valueIndex, length, colorArray, bits);
        }
        int mask = 1;
        switch (bits) {
            case 1:
                mask = 1;
                break;
            case 2:
                mask = 3;
                break;
            case 4:
                mask = 15;
                break;
            case 8:
                mask = 255;
                break;
        }
        int[] resultArray = new int[length];
        int resultIndex = 0;
        int run = 8 / bits;
        int valueIndex2 = valueIndex;
        while (resultIndex < length) {
            valueIndex = valueIndex2 + 1;
            byte tempByte = data[valueIndex2];
            int runIndex = 0;
            int resultIndex2 = resultIndex;
            while (runIndex < run) {
                resultIndex = resultIndex2 + 1;
                resultArray[resultIndex2] = colorArray[(tempByte >> (((run - runIndex) - 1) * bits)) & mask];
                runIndex++;
                resultIndex2 = resultIndex;
            }
            resultIndex = resultIndex2;
            valueIndex2 = valueIndex;
        }
        return resultArray;
    }

    private static int[] mapToNon2OrderBitColor(byte[] data, int valueIndex, int length, int[] colorArray, int bits) {
        if (8 % bits != 0) {
            return new int[length];
        }
        Rlog.e(LOG_TAG, "not odd number of color");
        return mapTo2OrderBitColor(data, valueIndex, length, colorArray, bits);
    }

    private static int[] getCLUT(byte[] rawData, int offset, int number) {
        if (rawData == null) {
            return null;
        }
        int[] result = new int[number];
        int endIndex = offset + (number * 3);
        int valueIndex = offset;
        int i = 0;
        while (true) {
            int colorIndex = i + 1;
            int valueIndex2 = valueIndex + 1;
            valueIndex = valueIndex2 + 1;
            valueIndex2 = valueIndex + 1;
            result[i] = ((((rawData[valueIndex] & 255) << 16) | -16777216) | ((rawData[valueIndex2] & 255) << 8)) | (rawData[valueIndex] & 255);
            if (valueIndex2 >= endIndex) {
                return result;
            }
            i = colorIndex;
            valueIndex = valueIndex2;
        }
    }

    public static String parseIccIdToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            int v = data[i] & 15;
            if (v < 0 || v > 9) {
                ret.append((char) ((v + 97) - 10));
            } else {
                ret.append((char) (v + 48));
            }
            v = (data[i] >> 4) & 15;
            if (v < 0 || v > 9) {
                ret.append((char) ((v + 97) - 10));
            } else {
                ret.append((char) (v + 48));
            }
        }
        return ret.toString();
    }

    public static String parsePlmnToStringForEfOpl(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        int v = data[offset] & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        v = (data[offset] >> 4) & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        v = data[offset + 1] & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        v = data[offset + 2] & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        v = (data[offset + 2] >> 4) & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        v = (data[offset + 1] >> 4) & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        return ret.toString();
    }

    public static String parseLanguageIndicator(byte[] rawData, int offset, int length) {
        if (rawData == null) {
            return null;
        }
        if (rawData.length >= offset + length) {
            return GsmAlphabet.gsm8BitUnpackedToString(rawData, offset, length);
        }
        Rlog.e(LOG_TAG, "length is invalid");
        return null;
    }

    public static String parsePlmnToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        int v = data[offset] & 15;
        if (v <= 9) {
            ret.append((char) (v + 48));
            v = (data[offset] >> 4) & 15;
            if (v <= 9) {
                ret.append((char) (v + 48));
                v = data[offset + 1] & 15;
                if (v <= 9) {
                    ret.append((char) (v + 48));
                    v = data[offset + 2] & 15;
                    if (v <= 9) {
                        ret.append((char) (v + 48));
                        v = (data[offset + 2] >> 4) & 15;
                        if (v <= 9) {
                            ret.append((char) (v + 48));
                            v = (data[offset + 1] >> 4) & 15;
                            if (v <= 9) {
                                ret.append((char) (v + 48));
                            }
                        }
                    }
                }
            }
        }
        return ret.toString();
    }

    public static byte[] stringTo0x81(String src) {
        int i;
        int len = src.length();
        byte[] ret = new byte[(len + 3)];
        int base = 0;
        boolean isNeeded = false;
        try {
            Rlog.d(LOG_TAG, "[stringTo0x81] byteTag," + bytesToHexString(src.getBytes("utf-16be")));
        } catch (UnsupportedEncodingException e) {
            Rlog.w(LOG_TAG, "[stringTo0x81] getBytes exception");
        }
        for (i = 0; i < len; i++) {
            if (src.charAt(i) > 127) {
                isNeeded = true;
                break;
            }
        }
        if (isNeeded) {
            ret[0] = (byte) -127;
            ret[1] = (byte) len;
            for (i = 0; i < len; i++) {
                char c = src.charAt(i);
                if (c < 128) {
                    ret[i + 3] = (byte) (c & 255);
                } else {
                    if (base == 0) {
                        base = (char) (c & 32640);
                    }
                    c = (char) (c ^ base);
                    if (c >= 128) {
                        Rlog.d(LOG_TAG, "[stringTo0x81] Can't encoding to 0x81, i: " + i + ", c: " + c);
                        return null;
                    }
                    ret[i + 3] = (byte) (((char) (c | 128)) & 255);
                }
            }
            ret[2] = (byte) (base >> 7);
            Rlog.d(LOG_TAG, "[stringTo0x81] success, ret: " + bytesToHexString(ret));
            return ret;
        }
        Rlog.d(LOG_TAG, "[stringTo0x81] no need to encoding to 0x81");
        return null;
    }

    public static byte[] stringTo0x82(String src) {
        int i;
        char c;
        int len = src.length();
        byte[] ret = new byte[(len + 4)];
        boolean isNeeded = false;
        char min = 65535;
        char max = 65535;
        try {
            Rlog.d(LOG_TAG, "[stringTo0x82] byteTag," + bytesToHexString(src.getBytes("utf-16be")));
        } catch (UnsupportedEncodingException e) {
            Rlog.w(LOG_TAG, "[stringTo0x82] getBytes exception");
        }
        for (i = 0; i < len; i++) {
            c = src.charAt(i);
            if (c > 127) {
                isNeeded = true;
                if (min < 0) {
                    max = c;
                    min = c;
                } else if (c < min) {
                    min = c;
                } else if (c > max) {
                    max = c;
                }
            }
        }
        if (!isNeeded) {
            Rlog.d(LOG_TAG, "[stringTo0x82] no need to encoding to 0x82");
            return null;
        } else if (max - min > 128) {
            Rlog.d(LOG_TAG, "[stringTo0x82] not support min: " + min + ", max: " + max);
            return null;
        } else {
            char base = min;
            ret[0] = (byte) -126;
            ret[1] = (byte) len;
            for (i = 0; i < len; i++) {
                c = src.charAt(i);
                if (c < 128) {
                    ret[i + 4] = (byte) (c & 255);
                } else {
                    ret[i + 4] = (byte) (((char) (((char) (c - base)) | 128)) & 255);
                }
            }
            ret[2] = (byte) (base >> 8);
            ret[3] = (byte) (base & 255);
            Rlog.d(LOG_TAG, "[stringTo0x82] success, ret: " + bytesToHexString(ret));
            return ret;
        }
    }
}
