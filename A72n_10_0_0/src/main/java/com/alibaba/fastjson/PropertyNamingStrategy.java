package com.alibaba.fastjson;

public enum PropertyNamingStrategy {
    CamelCase,
    PascalCase,
    SnakeCase,
    KebabCase;

    public String translate(String propertyName) {
        int i = 0;
        switch (this) {
            case SnakeCase:
                StringBuilder buf = new StringBuilder();
                while (i < propertyName.length()) {
                    char ch = propertyName.charAt(i);
                    if (ch < 'A' || ch > 'Z') {
                        buf.append(ch);
                    } else {
                        char ch_ucase = (char) (ch + ' ');
                        if (i > 0) {
                            buf.append('_');
                        }
                        buf.append(ch_ucase);
                    }
                    i++;
                }
                return buf.toString();
            case KebabCase:
                StringBuilder buf2 = new StringBuilder();
                while (i < propertyName.length()) {
                    char ch2 = propertyName.charAt(i);
                    if (ch2 < 'A' || ch2 > 'Z') {
                        buf2.append(ch2);
                    } else {
                        char ch_ucase2 = (char) (ch2 + ' ');
                        if (i > 0) {
                            buf2.append('-');
                        }
                        buf2.append(ch_ucase2);
                    }
                    i++;
                }
                return buf2.toString();
            case PascalCase:
                char ch3 = propertyName.charAt(0);
                if (ch3 < 'a' || ch3 > 'z') {
                    return propertyName;
                }
                char[] chars = propertyName.toCharArray();
                chars[0] = (char) (chars[0] - ' ');
                return new String(chars);
            case CamelCase:
                char ch4 = propertyName.charAt(0);
                if (ch4 < 'A' || ch4 > 'Z') {
                    return propertyName;
                }
                char[] chars2 = propertyName.toCharArray();
                chars2[0] = (char) (chars2[0] + ' ');
                return new String(chars2);
            default:
                return propertyName;
        }
    }
}
