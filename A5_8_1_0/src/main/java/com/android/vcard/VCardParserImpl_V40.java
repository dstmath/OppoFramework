package com.android.vcard;

import java.util.Set;

class VCardParserImpl_V40 extends VCardParserImpl_V30 {
    public VCardParserImpl_V40(int vcardType) {
        super(vcardType);
    }

    protected int getVersion() {
        return 2;
    }

    protected String getVersionString() {
        return VCardConstants.VERSION_V40;
    }

    protected String maybeUnescapeText(String text) {
        return unescapeText(text);
    }

    public static String unescapeText(String text) {
        StringBuilder builder = new StringBuilder();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char ch = text.charAt(i);
            if (ch != '\\' || i >= length - 1) {
                builder.append(ch);
            } else {
                i++;
                char next_ch = text.charAt(i);
                if (next_ch == 'n' || next_ch == 'N') {
                    builder.append("\n");
                } else {
                    builder.append(next_ch);
                }
            }
            i++;
        }
        return builder.toString();
    }

    public static String unescapeCharacter(char ch) {
        if (ch == 'n' || ch == 'N') {
            return "\n";
        }
        return String.valueOf(ch);
    }

    protected Set<String> getKnownPropertyNameSet() {
        return VCardParser_V40.sKnownPropertyNameSet;
    }
}
