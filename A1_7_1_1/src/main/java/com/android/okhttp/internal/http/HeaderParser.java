package com.android.okhttp.internal.http;

public final class HeaderParser {
    public static int skipUntil(String input, int pos, String characters) {
        while (pos < input.length() && characters.indexOf(input.charAt(pos)) == -1) {
            pos++;
        }
        return pos;
    }

    public static int skipWhitespace(String input, int pos) {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c != ' ' && c != 9) {
                break;
            }
            pos++;
        }
        return pos;
    }

    public static int parseSeconds(String value, int defaultValue) {
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
            return defaultValue;
        }
    }

    public static String getQuoteString(String input, String flag, int pos) {
        int posStart = skipUntil(input, pos, "\"") + 1;
        int posEnd = skipUntil(input, posStart + 1, "\"");
        String value = "";
        System.out.println(posStart + "/" + posEnd + "/" + input.length());
        if (posEnd > input.length()) {
            return "";
        }
        value = input.substring(posStart, posEnd);
        System.out.println(flag + ":" + value);
        return value;
    }

    private HeaderParser() {
    }
}
