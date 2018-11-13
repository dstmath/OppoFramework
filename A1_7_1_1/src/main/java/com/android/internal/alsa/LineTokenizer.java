package com.android.internal.alsa;

import com.android.internal.telephony.PhoneConstants;

public class LineTokenizer {
    public static final int kTokenNotFound = -1;
    private String mDelimiters = PhoneConstants.MVNO_TYPE_NONE;

    public LineTokenizer(String delimiters) {
        this.mDelimiters = delimiters;
    }

    int nextToken(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) != -1) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return -1;
    }

    int nextDelimiter(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) == -1) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return -1;
    }
}
