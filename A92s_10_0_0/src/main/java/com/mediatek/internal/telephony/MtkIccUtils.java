package com.mediatek.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.uicc.IccUtils;

public class MtkIccUtils extends IccUtils {
    static final String MTK_LOG_TAG = "MtkIccUtils";

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
        int v2 = (data[offset] >> 4) & 15;
        if (v2 < 0 || v2 > 9) {
            if (v2 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v2 + 48));
        int v3 = data[offset + 1] & 15;
        if (v3 < 0 || v3 > 9) {
            if (v3 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v3 + 48));
        int v4 = data[offset + 2] & 15;
        if (v4 < 0 || v4 > 9) {
            if (v4 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v4 + 48));
        int v5 = (data[offset + 2] >> 4) & 15;
        if (v5 < 0 || v5 > 9) {
            if (v5 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v5 + 48));
        int v6 = (data[offset + 1] >> 4) & 15;
        if (v6 < 0 || v6 > 9) {
            if (v6 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v6 + 48));
        return ret.toString();
    }

    public static String parseLanguageIndicator(byte[] rawData, int offset, int length) {
        if (rawData == null) {
            return null;
        }
        if (rawData.length >= offset + length) {
            return GsmAlphabet.gsm8BitUnpackedToString(rawData, offset, length);
        }
        Rlog.e(MTK_LOG_TAG, "length is invalid");
        return null;
    }

    public static String parsePlmnToString(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        int v = data[offset] & 15;
        if (v <= 9) {
            ret.append((char) (v + 48));
            int v2 = (data[offset] >> 4) & 15;
            if (v2 <= 9) {
                ret.append((char) (v2 + 48));
                int v3 = data[offset + 1] & 15;
                if (v3 <= 9) {
                    ret.append((char) (v3 + 48));
                    int v4 = data[offset + 2] & 15;
                    if (v4 <= 9) {
                        ret.append((char) (v4 + 48));
                        int v5 = (data[offset + 2] >> 4) & 15;
                        if (v5 <= 9) {
                            ret.append((char) (v5 + 48));
                            int v6 = (data[offset + 1] >> 4) & 15;
                            if (v6 <= 9) {
                                ret.append((char) (v6 + 48));
                            }
                        }
                    }
                }
            }
        }
        return ret.toString();
    }
}
