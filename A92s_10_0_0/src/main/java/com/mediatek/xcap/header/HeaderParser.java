package com.mediatek.xcap.header;

import android.util.Log;

public final class HeaderParser {
    private static final String TAG = "XcapHeaderParser";

    public static int skipUntil(String input, int pos, String characters) {
        while (pos < input.length() && characters.indexOf(input.charAt(pos)) == -1) {
            pos++;
        }
        return pos;
    }

    public static int skipWhitespace(String input, int pos) {
        while (pos < input.length() && ((c = input.charAt(pos)) == ' ' || c == 9)) {
            pos++;
        }
        return pos;
    }

    public static int parseSeconds(String value) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds > 2147483647L) {
                return Integer.MAX_VALUE;
            }
            if (seconds < 0) {
                return 0;
            }
            return (int) seconds;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String getQuoteString(String input, String flag, int pos) {
        int posStart = skipUntil(input, pos, "\"") + 1;
        int posEnd = skipUntil(input, posStart + 1, "\"");
        Log.i(TAG, posStart + "/" + posEnd + "/" + input.length());
        if (posEnd > input.length()) {
            return "";
        }
        String value = input.substring(posStart, posEnd);
        Log.i(TAG, flag + ":" + value);
        return value;
    }

    private HeaderParser() {
    }
}
