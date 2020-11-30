package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.IOUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

public final class JSONScanner extends JSONLexerBase {
    private final int len;
    private final String text;

    public JSONScanner(String input) {
        this(input, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONScanner(String input, int features) {
        super(features);
        this.text = input;
        this.len = this.text.length();
        this.bp = -1;
        next();
        if (this.ch == 65279) {
            next();
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final char charAt(int index) {
        if (index >= this.len) {
            return JSONLexer.EOI;
        }
        return this.text.charAt(index);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final char next() {
        char c;
        int index = this.bp + 1;
        this.bp = index;
        if (index >= this.len) {
            c = JSONLexer.EOI;
        } else {
            c = this.text.charAt(index);
        }
        this.ch = c;
        return c;
    }

    public JSONScanner(char[] input, int inputLength) {
        this(input, inputLength, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONScanner(char[] input, int inputLength, int features) {
        this(new String(input, 0, inputLength), features);
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void copyTo(int offset, int count, char[] dest) {
        this.text.getChars(offset, offset + count, dest, 0);
    }

    static boolean charArrayCompare(String src, int offset, char[] dest) {
        int destLen = dest.length;
        if (destLen + offset > src.length()) {
            return false;
        }
        for (int i = 0; i < destLen; i++) {
            if (dest[i] != src.charAt(offset + i)) {
                return false;
            }
        }
        return true;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final boolean charArrayCompare(char[] chars) {
        return charArrayCompare(this.text, this.bp, chars);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final int indexOf(char ch, int startIndex) {
        return this.text.indexOf(ch, startIndex);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final String addSymbol(int offset, int len2, int hash, SymbolTable symbolTable) {
        return symbolTable.addSymbol(this.text, offset, len2, hash);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public byte[] bytesValue() {
        if (this.token != 26) {
            return IOUtils.decodeBase64(this.text, this.np + 1, this.sp);
        }
        int start = this.np + 1;
        int len2 = this.sp;
        if (len2 % 2 == 0) {
            byte[] bytes = new byte[(len2 / 2)];
            for (int i = 0; i < bytes.length; i++) {
                char c0 = this.text.charAt((i * 2) + start);
                char c1 = this.text.charAt((i * 2) + start + 1);
                char c = '7';
                int b0 = c0 - (c0 <= '9' ? '0' : '7');
                if (c1 <= '9') {
                    c = '0';
                }
                bytes[i] = (byte) ((b0 << 4) | (c1 - c));
            }
            return bytes;
        }
        throw new JSONException("illegal state. " + len2);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final String stringVal() {
        if (!this.hasSpecial) {
            return subString(this.np + 1, this.sp);
        }
        return new String(this.sbuf, 0, this.sp);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final String subString(int offset, int count) {
        if (!ASMUtils.IS_ANDROID) {
            return this.text.substring(offset, offset + count);
        }
        if (count < this.sbuf.length) {
            this.text.getChars(offset, offset + count, this.sbuf, 0);
            return new String(this.sbuf, 0, count);
        }
        char[] chars = new char[count];
        this.text.getChars(offset, offset + count, chars, 0);
        return new String(chars);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final char[] sub_chars(int offset, int count) {
        if (!ASMUtils.IS_ANDROID || count >= this.sbuf.length) {
            char[] chars = new char[count];
            this.text.getChars(offset, offset + count, chars, 0);
            return chars;
        }
        this.text.getChars(offset, offset + count, this.sbuf, 0);
        return this.sbuf;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final String numberString() {
        char chLocal = charAt((this.np + this.sp) - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }
        return subString(this.np, sp);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final BigDecimal decimalValue() {
        char chLocal = charAt((this.np + this.sp) - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }
        int offset = this.np;
        if (sp < this.sbuf.length) {
            this.text.getChars(offset, offset + sp, this.sbuf, 0);
            return new BigDecimal(this.sbuf, 0, sp);
        }
        char[] chars = new char[sp];
        this.text.getChars(offset, offset + sp, chars, 0);
        return new BigDecimal(chars);
    }

    public boolean scanISO8601DateIfMatch() {
        return scanISO8601DateIfMatch(true);
    }

    public boolean scanISO8601DateIfMatch(boolean strict) {
        return scanISO8601DateIfMatch(strict, this.len - this.bp);
    }

    /* JADX INFO: Multiple debug info for r1v87 long: [D('millis' long), D('c6' char)] */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x019d, code lost:
        if (r11 != '.') goto L_0x01a2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x06ff A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x0701  */
    private boolean scanISO8601DateIfMatch(boolean strict, int rest) {
        char c6;
        char c4;
        char c5;
        char c2;
        char c3;
        char c1;
        char c7;
        int i;
        boolean z;
        char d0;
        char M1;
        char M0;
        char y3;
        char y2;
        char y1;
        char d1;
        char c62;
        char y0;
        int seconds;
        int minute;
        int hour;
        int millis;
        char s0;
        char m1;
        char m0;
        char h1;
        char h0;
        char s1;
        char d02;
        char y22;
        char y12;
        char y02;
        char d12;
        char d03;
        char M12;
        char M02;
        char y32;
        int date_len;
        char c52;
        char c63;
        char t3;
        int timzeZoneLength;
        char t4;
        char t32;
        int t33;
        char t34;
        int timzeZoneLength2;
        char S0;
        char S2;
        char S1;
        char y23;
        char y13;
        char y03;
        int date_len2;
        char c10;
        if (rest < 8) {
            return false;
        }
        char c0 = charAt(this.bp);
        char c12 = charAt(this.bp + 1);
        char c22 = charAt(this.bp + 2);
        char c32 = charAt(this.bp + 3);
        char c42 = charAt(this.bp + 4);
        char c53 = charAt(this.bp + 5);
        char c64 = charAt(this.bp + 6);
        char c72 = charAt(this.bp + 7);
        if (!strict && rest > 13) {
            char c_r0 = charAt((this.bp + rest) - 1);
            char c_r1 = charAt((this.bp + rest) - 2);
            if (c0 == '/' && c12 == 'D' && c22 == 'a' && c32 == 't' && c42 == 'e' && c53 == '(' && c_r0 == '/' && c_r1 == ')') {
                int plusIndex = -1;
                for (int i2 = 6; i2 < rest; i2++) {
                    char c = charAt(this.bp + i2);
                    if (c != '+') {
                        if (c < '0' || c > '9') {
                            break;
                        }
                    } else {
                        plusIndex = i2;
                    }
                }
                if (plusIndex == -1) {
                    return false;
                }
                int offset = this.bp + 6;
                long millis2 = Long.parseLong(subString(offset, (this.bp + plusIndex) - offset));
                this.calendar = Calendar.getInstance(this.timeZone, this.locale);
                this.calendar.setTimeInMillis(millis2);
                this.token = 5;
                return true;
            }
        }
        if (rest == 8 || rest == 14) {
            c7 = c72;
            c4 = c42;
            c3 = c32;
            c2 = c22;
            i = 14;
            c1 = c12;
            c5 = c53;
            c6 = c64;
            z = false;
        } else if ((rest == 16 && ((c10 = charAt(this.bp + 10)) == 'T' || c10 == ' ')) || (rest == 17 && charAt(this.bp + 6) != '-')) {
            c7 = c72;
            c4 = c42;
            c3 = c32;
            c2 = c22;
            i = 14;
            c1 = c12;
            c5 = c53;
            c6 = c64;
            z = false;
        } else if (rest < 9) {
            return false;
        } else {
            char c8 = charAt(this.bp + 8);
            char c9 = charAt(this.bp + 9);
            int date_len3 = 10;
            if ((c42 == '-' && c72 == '-') || (c42 == '/' && c72 == '/')) {
                y32 = c32;
                M02 = c53;
                M12 = c64;
                d03 = c8;
                d12 = c9;
                y02 = c0;
                date_len = 10;
                y12 = c12;
                y22 = c22;
                c52 = c53;
                c63 = c64;
            } else {
                if (c42 == '-') {
                    c63 = c64;
                    if (c63 == '-') {
                        y32 = c32;
                        M02 = '0';
                        M12 = c53;
                        if (c8 == ' ') {
                            d03 = '0';
                            d12 = c72;
                            date_len2 = 8;
                        } else {
                            d03 = c72;
                            d12 = c8;
                            date_len2 = 9;
                        }
                        date_len = date_len2;
                        y02 = c0;
                        y12 = c12;
                        c52 = c53;
                        y22 = c22;
                    }
                } else {
                    c63 = c64;
                }
                if (c22 == '.') {
                    c52 = c53;
                } else {
                    c52 = c53;
                }
                if (!(c22 == '-' && c52 == '-')) {
                    if (c8 == 'T') {
                        y03 = c0;
                        y13 = c12;
                        y23 = c22;
                        y32 = c32;
                        M02 = c42;
                        M12 = c52;
                        d03 = c63;
                        d12 = c72;
                        date_len3 = 8;
                    } else if (c42 != 24180 && c42 != 45380) {
                        return false;
                    } else {
                        y03 = c0;
                        y13 = c12;
                        y23 = c22;
                        y32 = c32;
                        if (c72 == 26376 || c72 == 50900) {
                            M02 = c52;
                            M12 = c63;
                            if (c9 == 26085 || c9 == 51068) {
                                d03 = '0';
                                d12 = c8;
                            } else if (charAt(this.bp + 10) != 26085 && charAt(this.bp + 10) != 51068) {
                                return false;
                            } else {
                                d03 = c8;
                                d12 = c9;
                                date_len3 = 11;
                            }
                        } else if (c63 != 26376 && c63 != 50900) {
                            return false;
                        } else {
                            M02 = '0';
                            M12 = c52;
                            if (c8 == 26085 || c8 == 51068) {
                                d03 = '0';
                                d12 = c72;
                            } else if (c9 != 26085 && c9 != 51068) {
                                return false;
                            } else {
                                d03 = c72;
                                d12 = c8;
                            }
                        }
                    }
                    y02 = y03;
                    date_len = date_len3;
                    y12 = y13;
                    y22 = y23;
                }
                d03 = c0;
                d12 = c12;
                M02 = c32;
                M12 = c42;
                y03 = c63;
                y13 = c72;
                y23 = c8;
                y32 = c9;
                y02 = y03;
                date_len = date_len3;
                y12 = y13;
                y22 = y23;
            }
            if (!checkDate(y02, y12, y22, y32, M02, M12, d03, d12)) {
                return false;
            }
            setCalendar(y02, y12, y22, y32, M02, M12, d03, d12);
            char t = charAt(this.bp + date_len);
            if (t == 'T' && rest == 16 && date_len == 8 && charAt(this.bp + 15) == 'Z') {
                char h02 = charAt(this.bp + date_len + 1);
                char h12 = charAt(this.bp + date_len + 2);
                char m02 = charAt(this.bp + date_len + 3);
                char m12 = charAt(this.bp + date_len + 4);
                char s02 = charAt(this.bp + date_len + 5);
                char s12 = charAt(this.bp + date_len + 6);
                if (!checkTime(h02, h12, m02, m12, s02, s12)) {
                    return false;
                }
                setTime(h02, h12, m02, m12, s02, s12);
                this.calendar.set(14, 0);
                if (this.calendar.getTimeZone().getRawOffset() != 0) {
                    String[] timeZoneIDs = TimeZone.getAvailableIDs(0);
                    if (timeZoneIDs.length > 0) {
                        this.calendar.setTimeZone(TimeZone.getTimeZone(timeZoneIDs[0]));
                    }
                }
                this.token = 5;
                return true;
            } else if (t == 'T' || (t == ' ' && !strict)) {
                if (!(rest >= date_len + 9 && charAt(this.bp + date_len + 3) == ':' && charAt(this.bp + date_len + 6) == ':')) {
                    return false;
                }
                char h03 = charAt(this.bp + date_len + 1);
                char h13 = charAt(this.bp + date_len + 2);
                char m03 = charAt(this.bp + date_len + 4);
                char m13 = charAt(this.bp + date_len + 5);
                char s03 = charAt(this.bp + date_len + 7);
                char s13 = charAt(this.bp + date_len + 8);
                if (!checkTime(h03, h13, m03, m13, s03, s13)) {
                    return false;
                }
                setTime(h03, h13, m03, m13, s03, s13);
                int millisLen = -1;
                int millis3 = 0;
                if (charAt(this.bp + date_len + 9) == '.') {
                    if (rest < date_len + 11 || (S0 = charAt(this.bp + date_len + 10)) < '0' || S0 > '9') {
                        return false;
                    }
                    millis3 = S0 - '0';
                    millisLen = 1;
                    if (rest > date_len + 11 && (S1 = charAt(this.bp + date_len + 11)) >= '0' && S1 <= '9') {
                        millisLen = 2;
                        millis3 = (millis3 * 10) + (S1 - '0');
                    }
                    if (millisLen == 2 && (S2 = charAt(this.bp + date_len + 12)) >= '0' && S2 <= '9') {
                        millis3 = (millis3 * 10) + (S2 - '0');
                        millisLen = 3;
                    }
                }
                this.calendar.set(14, millis3);
                int timzeZoneLength3 = 0;
                char timeZoneFlag = charAt(this.bp + date_len + 10 + millisLen);
                if (timeZoneFlag == ' ') {
                    millisLen++;
                    timeZoneFlag = charAt(this.bp + date_len + 10 + millisLen);
                }
                if (timeZoneFlag == '+' || timeZoneFlag == '-') {
                    char t0 = charAt(this.bp + date_len + 10 + millisLen + 1);
                    if (t0 < '0') {
                        return false;
                    }
                    if (t0 > '1') {
                        return false;
                    }
                    char t1 = charAt(this.bp + date_len + 10 + millisLen + 2);
                    if (t1 < '0') {
                        return false;
                    }
                    if (t1 > '9') {
                        return false;
                    }
                    char t2 = charAt(this.bp + date_len + 10 + millisLen + 3);
                    if (t2 == ':') {
                        char t35 = charAt(this.bp + date_len + 10 + millisLen + 4);
                        if (!(t35 == '0' || t35 == '3')) {
                            return false;
                        }
                        t4 = charAt(this.bp + date_len + 10 + millisLen + 5);
                        t3 = t35;
                        if (t4 != '0') {
                            return false;
                        }
                        t33 = 6;
                    } else {
                        if (t2 == '0') {
                            char t36 = charAt(this.bp + date_len + 10 + millisLen + 4);
                            if (!(t36 == '0' || t36 == '3')) {
                                return false;
                            }
                            timzeZoneLength2 = 5;
                            t3 = t36;
                            t4 = '0';
                        } else {
                            if (t2 == '3') {
                                t32 = '0';
                                if (charAt(this.bp + date_len + 10 + millisLen + 4) == '0') {
                                    t34 = '3';
                                    t4 = '0';
                                    timzeZoneLength2 = 5;
                                }
                                if (t2 == '4' || charAt(this.bp + date_len + 10 + millisLen + 4) != '5') {
                                    t33 = 3;
                                    t4 = '0';
                                    t3 = t32;
                                } else {
                                    t34 = '4';
                                    t4 = '5';
                                    timzeZoneLength2 = 5;
                                }
                            } else {
                                t32 = '0';
                                if (t2 == '4') {
                                }
                                t33 = 3;
                                t4 = '0';
                                t3 = t32;
                            }
                            t3 = t34;
                        }
                        timzeZoneLength = timzeZoneLength2;
                        setTimeZone(timeZoneFlag, t0, t1, t3, t4);
                        timzeZoneLength3 = timzeZoneLength;
                    }
                    timzeZoneLength = t33;
                    setTimeZone(timeZoneFlag, t0, t1, t3, t4);
                    timzeZoneLength3 = timzeZoneLength;
                } else if (timeZoneFlag == 'Z') {
                    timzeZoneLength3 = 1;
                    if (this.calendar.getTimeZone().getRawOffset() != 0) {
                        String[] timeZoneIDs2 = TimeZone.getAvailableIDs(0);
                        if (timeZoneIDs2.length > 0) {
                            this.calendar.setTimeZone(TimeZone.getTimeZone(timeZoneIDs2[0]));
                        }
                    }
                }
                char end = charAt(this.bp + date_len + 10 + millisLen + timzeZoneLength3);
                if (!(end == 26 || end == '\"')) {
                    return false;
                }
                int i3 = this.bp + date_len + 10 + millisLen + timzeZoneLength3;
                this.bp = i3;
                this.ch = charAt(i3);
                this.token = 5;
                return true;
            } else if (t == '\"' || t == 26 || t == 26085 || t == 51068) {
                this.calendar.set(11, 0);
                this.calendar.set(12, 0);
                this.calendar.set(13, 0);
                this.calendar.set(14, 0);
                int i4 = this.bp + date_len;
                this.bp = i4;
                this.ch = charAt(i4);
                this.token = 5;
                return true;
            } else if ((t != '+' && t != '-') || this.len != date_len + 6 || charAt(this.bp + date_len + 3) != ':' || charAt(this.bp + date_len + 4) != '0' || charAt(this.bp + date_len + 5) != '0') {
                return false;
            } else {
                setTime('0', '0', '0', '0', '0', '0');
                this.calendar.set(14, 0);
                setTimeZone(t, charAt(this.bp + date_len + 1), charAt(this.bp + date_len + 2));
                return true;
            }
        }
        if (strict) {
            return z;
        }
        char c82 = charAt(this.bp + 8);
        boolean c_47 = c4 == '-' && c7 == '-';
        boolean sperate16 = c_47 && rest == 16;
        boolean sperate17 = c_47 && rest == 17;
        if (sperate17) {
            c62 = c6;
        } else if (sperate16) {
            c62 = c6;
        } else {
            if (c4 == '-') {
                c62 = c6;
                if (c62 == '-') {
                    y0 = c0;
                    y1 = c1;
                    y2 = c2;
                    y3 = c3;
                    M0 = '0';
                    M1 = c5;
                    d0 = '0';
                    d1 = c7;
                    if (!checkDate(y0, y1, y2, y3, M0, M1, d0, d1)) {
                        return false;
                    }
                    setCalendar(y0, y1, y2, y3, M0, M1, d0, d1);
                    if (rest != 8) {
                        char c92 = charAt(this.bp + 9);
                        char c102 = charAt(this.bp + 10);
                        char c11 = charAt(this.bp + 11);
                        char c122 = charAt(this.bp + 12);
                        char c13 = charAt(this.bp + 13);
                        if (!(sperate17 && c102 == 'T' && c13 == ':' && charAt(this.bp + 16) == 'Z') && (!sperate16 || !((c102 == ' ' || c102 == 'T') && c13 == ':'))) {
                            h0 = c82;
                            h1 = c92;
                            m0 = c102;
                            m1 = c11;
                            s0 = c122;
                            s1 = c13;
                        } else {
                            s1 = '0';
                            h0 = c11;
                            h1 = c122;
                            m0 = charAt(this.bp + i);
                            m1 = charAt(this.bp + 15);
                            s0 = '0';
                        }
                        if (!checkTime(h0, h1, m0, m1, s0, s1)) {
                            return false;
                        }
                        if (rest != 17 || sperate17) {
                            millis = 0;
                        } else {
                            char S02 = charAt(this.bp + i);
                            char S12 = charAt(this.bp + 15);
                            char S22 = charAt(this.bp + 16);
                            if (S02 < '0' || S02 > '9' || S12 < '0' || S12 > '9' || S22 < '0' || S22 > '9') {
                                return false;
                            }
                            millis = ((S02 - '0') * 100) + ((S12 - '0') * 10) + (S22 - '0');
                        }
                        hour = ((h0 - '0') * 10) + (h1 - '0');
                        minute = ((m0 - '0') * 10) + (m1 - '0');
                        seconds = ((s0 - '0') * 10) + (s1 - '0');
                    } else {
                        hour = 0;
                        minute = 0;
                        seconds = 0;
                        millis = 0;
                    }
                    this.calendar.set(11, hour);
                    this.calendar.set(12, minute);
                    this.calendar.set(13, seconds);
                    this.calendar.set(i, millis);
                    this.token = 5;
                    return true;
                }
            } else {
                c62 = c6;
            }
            y0 = c0;
            d02 = c62;
            y1 = c1;
            y2 = c2;
            y3 = c3;
            M0 = c4;
            M1 = c5;
            d1 = c7;
            d0 = d02;
            if (!checkDate(y0, y1, y2, y3, M0, M1, d0, d1)) {
            }
        }
        y0 = c0;
        d02 = c82;
        y1 = c1;
        y2 = c2;
        y3 = c3;
        M0 = c5;
        M1 = c62;
        d1 = charAt(this.bp + 9);
        d0 = d02;
        if (!checkDate(y0, y1, y2, y3, M0, M1, d0, d1)) {
        }
    }

    /* access modifiers changed from: protected */
    public void setTime(char h0, char h1, char m0, char m1, char s0, char s1) {
        this.calendar.set(11, ((h0 - '0') * 10) + (h1 - '0'));
        this.calendar.set(12, ((m0 - '0') * 10) + (m1 - '0'));
        this.calendar.set(13, ((s0 - '0') * 10) + (s1 - '0'));
    }

    /* access modifiers changed from: protected */
    public void setTimeZone(char timeZoneFlag, char t0, char t1) {
        setTimeZone(timeZoneFlag, t0, t1, '0', '0');
    }

    /* access modifiers changed from: protected */
    public void setTimeZone(char timeZoneFlag, char t0, char t1, char t3, char t4) {
        int timeZoneOffset = ((((t0 - '0') * 10) + (t1 - '0')) * 3600 * 1000) + ((((t3 - '0') * 10) + (t4 - '0')) * 60 * 1000);
        if (timeZoneFlag == '-') {
            timeZoneOffset = -timeZoneOffset;
        }
        if (this.calendar.getTimeZone().getRawOffset() != timeZoneOffset) {
            String[] timeZoneIDs = TimeZone.getAvailableIDs(timeZoneOffset);
            if (timeZoneIDs.length > 0) {
                this.calendar.setTimeZone(TimeZone.getTimeZone(timeZoneIDs[0]));
            }
        }
    }

    private boolean checkTime(char h0, char h1, char m0, char m1, char s0, char s1) {
        if (h0 == '0') {
            if (h1 < '0' || h1 > '9') {
                return false;
            }
        } else if (h0 == '1') {
            if (h1 < '0' || h1 > '9') {
                return false;
            }
        } else if (h0 != '2' || h1 < '0' || h1 > '4') {
            return false;
        }
        if (m0 < '0' || m0 > '5') {
            if (!(m0 == '6' && m1 == '0')) {
                return false;
            }
        } else if (m1 < '0' || m1 > '9') {
            return false;
        }
        if (s0 < '0' || s0 > '5') {
            if (s0 == '6' && s1 == '0') {
                return true;
            }
            return false;
        } else if (s1 < '0' || s1 > '9') {
            return false;
        } else {
            return true;
        }
    }

    private void setCalendar(char y0, char y1, char y2, char y3, char M0, char M1, char d0, char d1) {
        this.calendar = Calendar.getInstance(this.timeZone, this.locale);
        this.calendar.set(1, ((y0 - '0') * 1000) + ((y1 - '0') * 100) + ((y2 - '0') * 10) + (y3 - '0'));
        this.calendar.set(2, (((M0 - '0') * 10) + (M1 - '0')) - 1);
        this.calendar.set(5, ((d0 - '0') * 10) + (d1 - '0'));
    }

    static boolean checkDate(char y0, char y1, char y2, char y3, char M0, char M1, int d0, int d1) {
        if (y0 < '0' || y0 > '9' || y1 < '0' || y1 > '9' || y2 < '0' || y2 > '9' || y3 < '0' || y3 > '9') {
            return false;
        }
        if (M0 == '0') {
            if (M1 < '1' || M1 > '9') {
                return false;
            }
        } else if (M0 != '1') {
            return false;
        } else {
            if (!(M1 == '0' || M1 == '1' || M1 == '2')) {
                return false;
            }
        }
        if (d0 == 48) {
            if (d1 < 49 || d1 > 57) {
                return false;
            }
            return true;
        } else if (d0 == 49 || d0 == 50) {
            if (d1 < 48 || d1 > 57) {
                return false;
            }
            return true;
        } else if (d0 != 51) {
            return false;
        } else {
            if (d1 == 48 || d1 == 49) {
                return true;
            }
            return false;
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public boolean isEOF() {
        if (this.bp != this.len) {
            return this.ch == 26 && this.bp + 1 == this.len;
        }
        return true;
    }

    /* JADX INFO: Multiple debug info for r5v4 char: [D('ch' char), D('index' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ba  */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public int scanFieldInt(char[] fieldName) {
        int index;
        char ch;
        int index2;
        this.matchStat = 0;
        int startPos = this.bp;
        char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int index3 = this.bp + fieldName.length;
        int index4 = index3 + 1;
        char ch2 = charAt(index3);
        boolean quote = ch2 == '\"';
        if (quote) {
            ch2 = charAt(index4);
            index4++;
        }
        boolean negative = ch2 == '-';
        if (negative) {
            ch2 = charAt(index4);
            index4++;
        }
        if (ch2 < '0' || ch2 > '9') {
            this.matchStat = -1;
            return 0;
        }
        int value = ch2 - '0';
        while (true) {
            index = index4 + 1;
            ch = charAt(index4);
            if (ch >= '0' && ch <= '9') {
                int value_10 = value * 10;
                if (value_10 < value) {
                    this.matchStat = -1;
                    return 0;
                }
                value = value_10 + (ch - '0');
                index4 = index;
            }
        }
        if (ch == '.') {
            this.matchStat = -1;
            return 0;
        } else if (value < 0) {
            this.matchStat = -1;
            return 0;
        } else {
            if (quote) {
                if (ch != '\"') {
                    this.matchStat = -1;
                    return 0;
                }
                index2 = index + 1;
                ch = charAt(index);
                index = index2;
            }
            if (ch != ',' || ch == '}') {
                this.bp = index - 1;
                if (ch != ',') {
                    int i = this.bp + 1;
                    this.bp = i;
                    this.ch = charAt(i);
                    this.matchStat = 3;
                    this.token = 16;
                    return negative ? -value : value;
                }
                if (ch == '}') {
                    this.bp = index - 1;
                    int i2 = this.bp + 1;
                    this.bp = i2;
                    char ch3 = charAt(i2);
                    while (true) {
                        if (ch3 == ',') {
                            this.token = 16;
                            int i3 = this.bp + 1;
                            this.bp = i3;
                            this.ch = charAt(i3);
                            break;
                        } else if (ch3 == ']') {
                            this.token = 15;
                            int i4 = this.bp + 1;
                            this.bp = i4;
                            this.ch = charAt(i4);
                            break;
                        } else if (ch3 == '}') {
                            this.token = 13;
                            int i5 = this.bp + 1;
                            this.bp = i5;
                            this.ch = charAt(i5);
                            break;
                        } else if (ch3 == 26) {
                            this.token = 20;
                            break;
                        } else if (isWhitespace(ch3)) {
                            int i6 = this.bp + 1;
                            this.bp = i6;
                            ch3 = charAt(i6);
                        } else {
                            this.bp = startPos;
                            this.ch = startChar;
                            this.matchStat = -1;
                            return 0;
                        }
                    }
                    this.matchStat = 4;
                }
                return negative ? -value : value;
            }
            if (isWhitespace(ch)) {
                index2 = index + 1;
                ch = charAt(index);
                index = index2;
                if (ch != ',') {
                }
                this.bp = index - 1;
                if (ch != ',') {
                }
            }
            this.matchStat = -1;
            return 0;
        }
    }

    /* JADX INFO: Multiple debug info for r2v4 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public String scanFieldString(char[] fieldName) {
        this.matchStat = 0;
        int startPos = this.bp;
        char startChar = this.ch;
        while (!charArrayCompare(this.text, this.bp, fieldName)) {
            if (isWhitespace(this.ch)) {
                next();
            } else {
                this.matchStat = -2;
                return stringDefaultValue();
            }
        }
        int index = this.bp + fieldName.length;
        int index2 = index + 1;
        if (charAt(index) != 34) {
            this.matchStat = -1;
            return stringDefaultValue();
        }
        int endIndex = indexOf('\"', index2);
        if (endIndex != -1) {
            String stringVal = subString(index2, endIndex - index2);
            if (stringVal.indexOf(92) != -1) {
                while (true) {
                    int slashCount = 0;
                    int i = endIndex - 1;
                    while (i >= 0 && charAt(i) == '\\') {
                        slashCount++;
                        i--;
                    }
                    if (slashCount % 2 == 0) {
                        break;
                    }
                    endIndex = indexOf('\"', endIndex + 1);
                }
                int chars_len = endIndex - ((this.bp + fieldName.length) + 1);
                stringVal = readString(sub_chars(this.bp + fieldName.length + 1, chars_len), chars_len);
            }
            char ch = charAt(endIndex + 1);
            while (ch != ',' && ch != '}') {
                if (isWhitespace(ch)) {
                    endIndex++;
                    ch = charAt(endIndex + 1);
                } else {
                    this.matchStat = -1;
                    return stringDefaultValue();
                }
            }
            this.bp = endIndex + 1;
            this.ch = ch;
            if (ch == ',') {
                int i2 = this.bp + 1;
                this.bp = i2;
                this.ch = charAt(i2);
                this.matchStat = 3;
                return stringVal;
            }
            int i3 = this.bp + 1;
            this.bp = i3;
            char ch2 = charAt(i3);
            if (ch2 == ',') {
                this.token = 16;
                int i4 = this.bp + 1;
                this.bp = i4;
                this.ch = charAt(i4);
            } else if (ch2 == ']') {
                this.token = 15;
                int i5 = this.bp + 1;
                this.bp = i5;
                this.ch = charAt(i5);
            } else if (ch2 == '}') {
                this.token = 13;
                int i6 = this.bp + 1;
                this.bp = i6;
                this.ch = charAt(i6);
            } else if (ch2 == 26) {
                this.token = 20;
            } else {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return stringDefaultValue();
            }
            this.matchStat = 4;
            return stringVal;
        }
        throw new JSONException("unclosed str");
    }

    /* JADX INFO: Multiple debug info for r5v4 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public Date scanFieldDate(char[] fieldName) {
        Date dateVal;
        int index;
        int index2;
        this.matchStat = 0;
        int startPos = this.bp;
        char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int index3 = this.bp + fieldName.length;
        int index4 = index3 + 1;
        char ch = charAt(index3);
        if (ch == '\"') {
            int endIndex = indexOf('\"', index4);
            if (endIndex != -1) {
                this.bp = index4;
                if (scanISO8601DateIfMatch(false, endIndex - index4)) {
                    dateVal = this.calendar.getTime();
                    ch = charAt(endIndex + 1);
                    this.bp = startPos;
                    while (ch != ',' && ch != '}') {
                        if (isWhitespace(ch)) {
                            endIndex++;
                            ch = charAt(endIndex + 1);
                        } else {
                            this.matchStat = -1;
                            return null;
                        }
                    }
                    this.bp = endIndex + 1;
                    this.ch = ch;
                } else {
                    this.bp = startPos;
                    this.matchStat = -1;
                    return null;
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else {
            char c = '0';
            if (ch == '-' || (ch >= '0' && ch <= '9')) {
                long millis = 0;
                boolean negative = false;
                if (ch == '-') {
                    index = index4 + 1;
                    ch = charAt(index4);
                    negative = true;
                } else {
                    index = index4;
                }
                if (ch >= '0' && ch <= '9') {
                    long millis2 = (long) (ch - '0');
                    while (true) {
                        index2 = index + 1;
                        ch = charAt(index);
                        if (ch >= c && ch <= '9') {
                            index = index2;
                            millis2 = (10 * millis2) + ((long) (ch - '0'));
                            c = '0';
                        }
                    }
                    if (ch == ',' || ch == '}') {
                        this.bp = index2 - 1;
                    }
                    millis = millis2;
                }
                if (millis < 0) {
                    this.matchStat = -1;
                    return null;
                }
                if (negative) {
                    millis = -millis;
                }
                dateVal = new Date(millis);
            } else {
                this.matchStat = -1;
                return null;
            }
        }
        if (ch == ',') {
            int i = this.bp + 1;
            this.bp = i;
            this.ch = charAt(i);
            this.matchStat = 3;
            this.token = 16;
            return dateVal;
        }
        int i2 = this.bp + 1;
        this.bp = i2;
        char ch2 = charAt(i2);
        if (ch2 == ',') {
            this.token = 16;
            int i3 = this.bp + 1;
            this.bp = i3;
            this.ch = charAt(i3);
        } else if (ch2 == ']') {
            this.token = 15;
            int i4 = this.bp + 1;
            this.bp = i4;
            this.ch = charAt(i4);
        } else if (ch2 == '}') {
            this.token = 13;
            int i5 = this.bp + 1;
            this.bp = i5;
            this.ch = charAt(i5);
        } else if (ch2 == 26) {
            this.token = 20;
        } else {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return null;
        }
        this.matchStat = 4;
        return dateVal;
    }

    /* JADX INFO: Multiple debug info for r0v5 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public long scanFieldSymbol(char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int index = this.bp + fieldName.length;
        int index2 = index + 1;
        if (charAt(index) != 34) {
            this.matchStat = -1;
            return 0;
        }
        long hash = -3750763034362895579L;
        while (true) {
            int index3 = index2 + 1;
            char ch = charAt(index2);
            if (ch == '\"') {
                this.bp = index3;
                char charAt = charAt(this.bp);
                char ch2 = charAt;
                this.ch = charAt;
                while (ch2 != ',') {
                    if (ch2 == '}') {
                        next();
                        skipWhitespace();
                        char ch3 = getCurrent();
                        if (ch3 == ',') {
                            this.token = 16;
                            int i = this.bp + 1;
                            this.bp = i;
                            this.ch = charAt(i);
                        } else if (ch3 == ']') {
                            this.token = 15;
                            int i2 = this.bp + 1;
                            this.bp = i2;
                            this.ch = charAt(i2);
                        } else if (ch3 == '}') {
                            this.token = 13;
                            int i3 = this.bp + 1;
                            this.bp = i3;
                            this.ch = charAt(i3);
                        } else if (ch3 == 26) {
                            this.token = 20;
                        } else {
                            this.matchStat = -1;
                            return 0;
                        }
                        this.matchStat = 4;
                        return hash;
                    } else if (isWhitespace(ch2)) {
                        int i4 = this.bp + 1;
                        this.bp = i4;
                        ch2 = charAt(i4);
                    } else {
                        this.matchStat = -1;
                        return 0;
                    }
                }
                int i5 = this.bp + 1;
                this.bp = i5;
                this.ch = charAt(i5);
                this.matchStat = 3;
                return hash;
            } else if (index3 > this.len) {
                this.matchStat = -1;
                return 0;
            } else {
                hash = (hash ^ ((long) ch)) * 1099511628211L;
                index2 = index3;
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public Collection<String> newCollectionByType(Class<?> type) {
        if (type.isAssignableFrom(HashSet.class)) {
            return new HashSet<>();
        }
        if (type.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>();
        }
        try {
            return (Collection) type.newInstance();
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /* JADX INFO: Multiple debug info for r9v2 char: [D('ch' char), D('index' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ec, code lost:
        if (r9 != ']') goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f2, code lost:
        if (r6.size() != 0) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00f4, code lost:
        r2 = r11 + 1;
        r4 = charAt(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00fb, code lost:
        r19.matchStat = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ff, code lost:
        return null;
     */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public Collection<String> scanFieldStringArray(char[] fieldName, Class<?> type) {
        char ch;
        int index;
        int index2;
        int index3;
        this.matchStat = 0;
        while (true) {
            if (this.ch != '\n' && this.ch != ' ') {
                break;
            }
            char c = JSONLexer.EOI;
            int index4 = this.bp + 1;
            this.bp = index4;
            if (index4 < this.len) {
                c = this.text.charAt(index4);
            }
            this.ch = c;
        }
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return null;
        }
        Collection<String> list = newCollectionByType(type);
        int startPos = this.bp;
        char startChar = this.ch;
        int index5 = this.bp + fieldName.length;
        int index6 = index5 + 1;
        int i = -1;
        if (charAt(index5) == 91) {
            int index7 = index6 + 1;
            char ch2 = charAt(index6);
            while (true) {
                if (ch2 == '\"') {
                    int endIndex = indexOf('\"', index7);
                    if (endIndex != i) {
                        String stringVal = subString(index7, endIndex - index7);
                        if (stringVal.indexOf(92) != i) {
                            while (true) {
                                int slashCount = 0;
                                int i2 = endIndex - 1;
                                while (i2 >= 0 && charAt(i2) == '\\') {
                                    slashCount++;
                                    i2--;
                                }
                                if (slashCount % 2 == 0) {
                                    break;
                                }
                                endIndex = indexOf('\"', endIndex + 1);
                            }
                            int chars_len = endIndex - index7;
                            stringVal = readString(sub_chars(index7, chars_len), chars_len);
                        }
                        int index8 = endIndex + 1;
                        index2 = index8 + 1;
                        index3 = charAt(index8);
                        list.add(stringVal);
                    } else {
                        throw new JSONException("unclosed str");
                    }
                } else if (ch2 == 'n' && this.text.startsWith("ull", index7)) {
                    int index9 = index7 + 3;
                    index2 = index9 + 1;
                    index3 = charAt(index9);
                    list.add(null);
                }
                if (index3 == 44) {
                    index7 = index2 + 1;
                    ch2 = charAt(index2);
                    i = -1;
                } else if (index3 == 93) {
                    index = index2 + 1;
                    ch = charAt(index2);
                    while (isWhitespace(ch)) {
                        ch = charAt(index);
                        index++;
                    }
                } else {
                    this.matchStat = -1;
                    return null;
                }
            }
        } else if (this.text.startsWith("ull", index6)) {
            int index10 = index6 + 3;
            index = index10 + 1;
            ch = charAt(index10);
            list = null;
        } else {
            this.matchStat = -1;
            return null;
        }
        this.bp = index;
        if (ch == ',') {
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return list;
        } else if (ch == '}') {
            char ch3 = charAt(this.bp);
            while (true) {
                if (ch3 == ',') {
                    this.token = 16;
                    int i3 = this.bp + 1;
                    this.bp = i3;
                    this.ch = charAt(i3);
                    break;
                } else if (ch3 == ']') {
                    this.token = 15;
                    int i4 = this.bp + 1;
                    this.bp = i4;
                    this.ch = charAt(i4);
                    break;
                } else if (ch3 == '}') {
                    this.token = 13;
                    int i5 = this.bp + 1;
                    this.bp = i5;
                    this.ch = charAt(i5);
                    break;
                } else if (ch3 == 26) {
                    this.token = 20;
                    this.ch = ch3;
                    break;
                } else {
                    int index11 = index;
                    boolean space = false;
                    while (isWhitespace(ch3)) {
                        int index12 = index11 + 1;
                        ch3 = charAt(index11);
                        this.bp = index12;
                        space = true;
                        index11 = index12;
                    }
                    if (space) {
                        index = index11;
                    } else {
                        this.matchStat = -1;
                        return null;
                    }
                }
            }
            this.matchStat = 4;
            return list;
        } else {
            this.ch = startChar;
            this.bp = startPos;
            this.matchStat = -1;
            return null;
        }
    }

    /* JADX INFO: Multiple debug info for r5v4 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public long scanFieldLong(char[] fieldName) {
        int index;
        int index2;
        char ch;
        this.matchStat = 0;
        int startPos = this.bp;
        char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int index3 = this.bp + fieldName.length;
        int index4 = index3 + 1;
        char ch2 = charAt(index3);
        boolean quote = ch2 == '\"';
        if (quote) {
            index = index4 + 1;
            ch2 = charAt(index4);
        } else {
            index = index4;
        }
        boolean negative = false;
        if (ch2 == '-') {
            ch2 = charAt(index);
            negative = true;
            index++;
        }
        if (ch2 < '0' || ch2 > '9') {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return 0;
        }
        long value = (long) (ch2 - '0');
        while (true) {
            index2 = index + 1;
            ch = charAt(index);
            if (ch >= '0' && ch <= '9') {
                value = (10 * value) + ((long) (ch - '0'));
                index = index2;
            }
        }
        if (ch == '.') {
            this.matchStat = -1;
            return 0;
        }
        if (quote) {
            if (ch != '\"') {
                this.matchStat = -1;
                return 0;
            }
            ch = charAt(index2);
            index2++;
        }
        if (ch == ',' || ch == '}') {
            this.bp = index2 - 1;
        }
        if (!(value >= 0 || (value == Long.MIN_VALUE && negative))) {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return 0;
        }
        while (ch != ',') {
            if (ch == '}') {
                int i = 1;
                int i2 = this.bp + 1;
                this.bp = i2;
                char ch3 = charAt(i2);
                while (true) {
                    if (ch3 == ',') {
                        this.token = 16;
                        int i3 = this.bp + i;
                        this.bp = i3;
                        this.ch = charAt(i3);
                        break;
                    } else if (ch3 == ']') {
                        this.token = 15;
                        int i4 = this.bp + 1;
                        this.bp = i4;
                        this.ch = charAt(i4);
                        break;
                    } else if (ch3 == '}') {
                        this.token = 13;
                        int i5 = this.bp + 1;
                        this.bp = i5;
                        this.ch = charAt(i5);
                        break;
                    } else if (ch3 == 26) {
                        this.token = 20;
                        break;
                    } else if (isWhitespace(ch3)) {
                        i = 1;
                        int i6 = this.bp + 1;
                        this.bp = i6;
                        ch3 = charAt(i6);
                    } else {
                        this.bp = startPos;
                        this.ch = startChar;
                        this.matchStat = -1;
                        return 0;
                    }
                }
                this.matchStat = 4;
                return negative ? -value : value;
            } else if (isWhitespace(ch)) {
                this.bp = index2;
                ch = charAt(index2);
                index2++;
            } else {
                this.matchStat = -1;
                return 0;
            }
        }
        int i7 = this.bp + 1;
        this.bp = i7;
        this.ch = charAt(i7);
        this.matchStat = 3;
        this.token = 16;
        return negative ? -value : value;
    }

    /* JADX INFO: Multiple debug info for r2v3 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public boolean scanFieldBoolean(char[] fieldName) {
        boolean value;
        char ch;
        int index;
        this.matchStat = 0;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return false;
        }
        int startPos = this.bp;
        int index2 = this.bp + fieldName.length;
        int index3 = index2 + 1;
        char ch2 = charAt(index2);
        boolean quote = ch2 == '\"';
        if (quote) {
            ch2 = charAt(index3);
            index3++;
        }
        if (ch2 == 't') {
            int index4 = index3 + 1;
            if (charAt(index3) != 114) {
                this.matchStat = -1;
                return false;
            }
            int index5 = index4 + 1;
            if (charAt(index4) != 117) {
                this.matchStat = -1;
                return false;
            }
            int index6 = index5 + 1;
            if (charAt(index5) != 101) {
                this.matchStat = -1;
                return false;
            }
            if (quote) {
                index = index6 + 1;
                if (charAt(index6) != 34) {
                    this.matchStat = -1;
                    return false;
                }
            } else {
                index = index6;
            }
            this.bp = index;
            ch = charAt(this.bp);
            value = true;
        } else if (ch2 == 'f') {
            int index7 = index3 + 1;
            if (charAt(index3) != 97) {
                this.matchStat = -1;
                return false;
            }
            int index8 = index7 + 1;
            if (charAt(index7) != 108) {
                this.matchStat = -1;
                return false;
            }
            int index9 = index8 + 1;
            if (charAt(index8) != 115) {
                this.matchStat = -1;
                return false;
            }
            int index10 = index9 + 1;
            if (charAt(index9) != 101) {
                this.matchStat = -1;
                return false;
            }
            if (quote) {
                int index11 = index10 + 1;
                if (charAt(index10) != 34) {
                    this.matchStat = -1;
                    return false;
                }
                index10 = index11;
            }
            this.bp = index10;
            ch = charAt(this.bp);
            value = false;
        } else if (ch2 == '1') {
            if (quote) {
                int index12 = index3 + 1;
                if (charAt(index3) != 34) {
                    this.matchStat = -1;
                    return false;
                }
                index3 = index12;
            }
            this.bp = index3;
            ch = charAt(this.bp);
            value = true;
        } else if (ch2 == '0') {
            if (quote) {
                int index13 = index3 + 1;
                if (charAt(index3) != 34) {
                    this.matchStat = -1;
                    return false;
                }
                index3 = index13;
            }
            this.bp = index3;
            ch = charAt(this.bp);
            value = false;
        } else {
            this.matchStat = -1;
            return false;
        }
        while (true) {
            if (ch == ',') {
                int i = this.bp + 1;
                this.bp = i;
                this.ch = charAt(i);
                this.matchStat = 3;
                this.token = 16;
                break;
            } else if (ch == '}') {
                int i2 = this.bp + 1;
                this.bp = i2;
                char ch3 = charAt(i2);
                while (true) {
                    if (ch3 == ',') {
                        this.token = 16;
                        int i3 = this.bp + 1;
                        this.bp = i3;
                        this.ch = charAt(i3);
                        break;
                    } else if (ch3 == ']') {
                        this.token = 15;
                        int i4 = this.bp + 1;
                        this.bp = i4;
                        this.ch = charAt(i4);
                        break;
                    } else if (ch3 == '}') {
                        this.token = 13;
                        int i5 = this.bp + 1;
                        this.bp = i5;
                        this.ch = charAt(i5);
                        break;
                    } else if (ch3 == 26) {
                        this.token = 20;
                        break;
                    } else if (isWhitespace(ch3)) {
                        int i6 = this.bp + 1;
                        this.bp = i6;
                        ch3 = charAt(i6);
                    } else {
                        this.matchStat = -1;
                        return false;
                    }
                }
                this.matchStat = 4;
            } else if (isWhitespace(ch)) {
                int i7 = this.bp + 1;
                this.bp = i7;
                ch = charAt(i7);
            } else {
                this.bp = startPos;
                charAt(this.bp);
                this.matchStat = -1;
                return false;
            }
        }
        return value;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final int scanInt(char expectNext) {
        int offset;
        int offset2;
        char chLocal;
        this.matchStat = 0;
        int mark = this.bp;
        int offset3 = this.bp;
        int offset4 = offset3 + 1;
        char chLocal2 = charAt(offset3);
        while (isWhitespace(chLocal2)) {
            chLocal2 = charAt(offset4);
            offset4++;
        }
        boolean negative = true;
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(offset4);
            offset4++;
        }
        if (chLocal2 != '-') {
            negative = false;
        }
        if (negative) {
            chLocal2 = charAt(offset4);
            offset4++;
        }
        if (chLocal2 < '0' || chLocal2 > '9') {
            if (chLocal2 == 'n') {
                int offset5 = offset4 + 1;
                if (charAt(offset4) == 117) {
                    int offset6 = offset5 + 1;
                    if (charAt(offset5) == 108) {
                        offset5 = offset6 + 1;
                        if (charAt(offset6) == 108) {
                            this.matchStat = 5;
                            int offset7 = offset5 + 1;
                            char chLocal3 = charAt(offset5);
                            if (!quote || chLocal3 != '\"') {
                                offset = offset7;
                            } else {
                                offset = offset7 + 1;
                                chLocal3 = charAt(offset7);
                            }
                            while (chLocal3 != ',') {
                                if (chLocal3 == ']') {
                                    this.bp = offset;
                                    this.ch = charAt(this.bp);
                                    this.matchStat = 5;
                                    this.token = 15;
                                    return 0;
                                } else if (isWhitespace(chLocal3)) {
                                    chLocal3 = charAt(offset);
                                    offset++;
                                } else {
                                    this.matchStat = -1;
                                    return 0;
                                }
                            }
                            this.bp = offset;
                            this.ch = charAt(this.bp);
                            this.matchStat = 5;
                            this.token = 16;
                            return 0;
                        }
                    }
                }
            }
            this.matchStat = -1;
            return 0;
        }
        int value = chLocal2 - '0';
        while (true) {
            offset2 = offset4 + 1;
            chLocal = charAt(offset4);
            if (chLocal >= '0' && chLocal <= '9') {
                int value_10 = value * 10;
                if (value_10 >= value) {
                    value = value_10 + (chLocal - '0');
                    offset4 = offset2;
                } else {
                    throw new JSONException("parseInt error : " + subString(mark, offset2 - 1));
                }
            }
        }
        if (chLocal == '.') {
            this.matchStat = -1;
            return 0;
        }
        if (quote) {
            if (chLocal != '\"') {
                this.matchStat = -1;
                return 0;
            }
            chLocal = charAt(offset2);
            offset2++;
        }
        if (value < 0) {
            this.matchStat = -1;
            return 0;
        }
        while (chLocal != expectNext) {
            if (isWhitespace(chLocal)) {
                chLocal = charAt(offset2);
                offset2++;
            } else {
                this.matchStat = -1;
                return negative ? -value : value;
            }
        }
        this.bp = offset2;
        this.ch = charAt(this.bp);
        this.matchStat = 3;
        this.token = 16;
        return negative ? -value : value;
    }

    /* JADX INFO: Multiple debug info for r2v1 char: [D('chLocal' char), D('offset' int)] */
    /* JADX INFO: Multiple debug info for r1v6 char: [D('chLocal' char), D('offset' int)] */
    /* JADX INFO: Multiple debug info for r3v10 char: [D('offset' int), D('chLocal' char)] */
    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public double scanDouble(char r26) {
        /*
        // Method dump skipped, instructions count: 429
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONScanner.scanDouble(char):double");
    }

    /* JADX INFO: Multiple debug info for r2v1 char: [D('chLocal' char), D('offset' int)] */
    /* JADX INFO: Multiple debug info for r1v6 char: [D('chLocal' char), D('offset' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public long scanLong(char seperator) {
        int offset;
        char chLocal;
        this.matchStat = 0;
        int offset2 = this.bp;
        int offset3 = offset2 + 1;
        char chLocal2 = charAt(offset2);
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(offset3);
            offset3++;
        }
        boolean negative = chLocal2 == '-';
        if (negative) {
            chLocal2 = charAt(offset3);
            offset3++;
        }
        char c = '0';
        if (chLocal2 < '0' || chLocal2 > '9') {
            if (chLocal2 == 'n') {
                int offset4 = offset3 + 1;
                if (charAt(offset3) == 117) {
                    offset3 = offset4 + 1;
                    if (charAt(offset4) == 108) {
                        int offset5 = offset3 + 1;
                        if (charAt(offset3) == 108) {
                            this.matchStat = 5;
                            int offset6 = offset5 + 1;
                            char chLocal3 = charAt(offset5);
                            if (quote && chLocal3 == '\"') {
                                chLocal3 = charAt(offset6);
                                offset6++;
                            }
                            while (chLocal3 != ',') {
                                if (chLocal3 == ']') {
                                    this.bp = offset6;
                                    this.ch = charAt(this.bp);
                                    this.matchStat = 5;
                                    this.token = 15;
                                    return 0;
                                } else if (isWhitespace(chLocal3)) {
                                    chLocal3 = charAt(offset6);
                                    offset6++;
                                } else {
                                    this.matchStat = -1;
                                    return 0;
                                }
                            }
                            this.bp = offset6;
                            this.ch = charAt(this.bp);
                            this.matchStat = 5;
                            this.token = 16;
                            return 0;
                        }
                    }
                }
                this.matchStat = -1;
                return 0;
            }
            this.matchStat = -1;
            return 0;
        }
        long value = (long) (chLocal2 - '0');
        while (true) {
            offset = offset3 + 1;
            chLocal = charAt(offset3);
            if (chLocal >= c && chLocal <= '9') {
                value = (10 * value) + ((long) (chLocal - '0'));
                offset3 = offset;
                c = '0';
            }
        }
        if (chLocal == '.') {
            this.matchStat = -1;
            return 0;
        }
        if (quote) {
            if (chLocal != '\"') {
                this.matchStat = -1;
                return 0;
            }
            chLocal = charAt(offset);
            offset++;
        }
        if (!(value >= 0 || (value == Long.MIN_VALUE && negative))) {
            this.matchStat = -1;
            return 0;
        }
        while (chLocal != seperator) {
            if (isWhitespace(chLocal)) {
                chLocal = charAt(offset);
                offset++;
            } else {
                this.matchStat = -1;
                return value;
            }
        }
        this.bp = offset;
        this.ch = charAt(this.bp);
        this.matchStat = 3;
        this.token = 16;
        return negative ? -value : value;
    }

    /* JADX INFO: Multiple debug info for r4v1 char: [D('ch' char), D('index' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public Date scanDate(char seperator) {
        Date dateVal;
        int index;
        int index2;
        this.matchStat = 0;
        int startPos = this.bp;
        char startChar = this.ch;
        int index3 = this.bp;
        int index4 = index3 + 1;
        char ch = charAt(index3);
        if (ch == '\"') {
            int endIndex = indexOf('\"', index4);
            if (endIndex != -1) {
                this.bp = index4;
                if (scanISO8601DateIfMatch(false, endIndex - index4)) {
                    dateVal = this.calendar.getTime();
                    ch = charAt(endIndex + 1);
                    this.bp = startPos;
                    while (ch != ',' && ch != ']') {
                        if (isWhitespace(ch)) {
                            endIndex++;
                            ch = charAt(endIndex + 1);
                        } else {
                            this.bp = startPos;
                            this.ch = startChar;
                            this.matchStat = -1;
                            return null;
                        }
                    }
                    this.bp = endIndex + 1;
                    this.ch = ch;
                } else {
                    this.bp = startPos;
                    this.ch = startChar;
                    this.matchStat = -1;
                    return null;
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else {
            char c = '0';
            if (ch == '-' || (ch >= '0' && ch <= '9')) {
                long millis = 0;
                boolean negative = false;
                if (ch == '-') {
                    index = index4 + 1;
                    ch = charAt(index4);
                    negative = true;
                } else {
                    index = index4;
                }
                if (ch >= '0' && ch <= '9') {
                    long millis2 = (long) (ch - '0');
                    while (true) {
                        index2 = index + 1;
                        ch = charAt(index);
                        if (ch >= c && ch <= '9') {
                            index = index2;
                            millis2 = ((long) (ch - '0')) + (10 * millis2);
                            c = '0';
                        }
                    }
                    if (ch == ',' || ch == ']') {
                        this.bp = index2 - 1;
                    }
                    millis = millis2;
                }
                if (millis < 0) {
                    this.bp = startPos;
                    this.ch = startChar;
                    this.matchStat = -1;
                    return null;
                }
                if (negative) {
                    millis = -millis;
                }
                dateVal = new Date(millis);
            } else {
                if (ch == 'n') {
                    int index5 = index4 + 1;
                    if (charAt(index4) == 117) {
                        index4 = index5 + 1;
                        if (charAt(index5) == 108) {
                            int index6 = index4 + 1;
                            if (charAt(index4) == 108) {
                                ch = charAt(index6);
                                this.bp = index6;
                                dateVal = null;
                            }
                        }
                    }
                    this.bp = startPos;
                    this.ch = startChar;
                    this.matchStat = -1;
                    return null;
                }
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
        }
        if (ch == ',') {
            int i = this.bp + 1;
            this.bp = i;
            this.ch = charAt(i);
            this.matchStat = 3;
            return dateVal;
        }
        int i2 = this.bp + 1;
        this.bp = i2;
        char ch2 = charAt(i2);
        if (ch2 == ',') {
            this.token = 16;
            int i3 = this.bp + 1;
            this.bp = i3;
            this.ch = charAt(i3);
        } else if (ch2 == ']') {
            this.token = 15;
            int i4 = this.bp + 1;
            this.bp = i4;
            this.ch = charAt(i4);
        } else if (ch2 == '}') {
            this.token = 13;
            int i5 = this.bp + 1;
            this.bp = i5;
            this.ch = charAt(i5);
        } else if (ch2 == 26) {
            this.ch = JSONLexer.EOI;
            this.token = 20;
        } else {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return null;
        }
        this.matchStat = 4;
        return dateVal;
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void arrayCopy(int srcPos, char[] dest, int destPos, int length) {
        this.text.getChars(srcPos, srcPos + length, dest, destPos);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public String info() {
        StringBuilder buf = new StringBuilder();
        int column = 1;
        int line = 1;
        int i = 0;
        while (i < this.bp) {
            if (this.text.charAt(i) == '\n') {
                column = 1;
                line++;
            }
            i++;
            column++;
        }
        buf.append("pos ");
        buf.append(this.bp);
        buf.append(", line ");
        buf.append(line);
        buf.append(", column ");
        buf.append(column);
        if (this.text.length() < 65535) {
            buf.append(this.text);
        } else {
            buf.append(this.text.substring(0, 65535));
        }
        return buf.toString();
    }

    /* JADX INFO: Multiple debug info for r6v9 int: [D('typeIndex' int), D('types' java.lang.String[])] */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public String[] scanFieldStringArray(char[] fieldName, int argTypesCount, SymbolTable typeSymbolTable) {
        int offset;
        char ch;
        int startPos = this.bp;
        char starChar = this.ch;
        while (isWhitespace(this.ch)) {
            next();
        }
        String[] strArr = null;
        if (fieldName != null) {
            this.matchStat = 0;
            if (!charArrayCompare(fieldName)) {
                this.matchStat = -2;
                return null;
            }
            int offset2 = this.bp + fieldName.length;
            int offset3 = offset2 + 1;
            char ch2 = this.text.charAt(offset2);
            while (isWhitespace(ch2)) {
                ch2 = this.text.charAt(offset3);
                offset3++;
            }
            if (ch2 == ':') {
                offset = offset3 + 1;
                ch = this.text.charAt(offset3);
                while (isWhitespace(ch)) {
                    ch = this.text.charAt(offset);
                    offset++;
                }
            } else {
                this.matchStat = -1;
                return null;
            }
        } else {
            offset = this.bp + 1;
            ch = this.ch;
        }
        if (ch == '[') {
            this.bp = offset;
            this.ch = this.text.charAt(this.bp);
            String[] types = argTypesCount >= 0 ? new String[argTypesCount] : new String[4];
            int typeIndex = 0;
            while (true) {
                if (isWhitespace(this.ch)) {
                    next();
                } else if (this.ch != '\"') {
                    this.bp = startPos;
                    this.ch = starChar;
                    this.matchStat = -1;
                    return strArr;
                } else {
                    String type = scanSymbol(typeSymbolTable, '\"');
                    if (typeIndex == types.length) {
                        String[] array = new String[(types.length + (types.length >> 1) + 1)];
                        System.arraycopy(types, 0, array, 0, types.length);
                        types = array;
                    }
                    int typeIndex2 = typeIndex + 1;
                    types[typeIndex] = type;
                    while (isWhitespace(this.ch)) {
                        next();
                    }
                    if (this.ch == ',') {
                        next();
                        typeIndex = typeIndex2;
                        strArr = null;
                    } else {
                        if (types.length != typeIndex2) {
                            String[] array2 = new String[typeIndex2];
                            System.arraycopy(types, 0, array2, 0, typeIndex2);
                            types = array2;
                        }
                        while (isWhitespace(this.ch)) {
                            next();
                        }
                        if (this.ch == ']') {
                            next();
                            return types;
                        }
                        this.bp = startPos;
                        this.ch = starChar;
                        this.matchStat = -1;
                        return null;
                    }
                }
            }
        } else if (ch != 'n' || !this.text.startsWith("ull", this.bp + 1)) {
            this.matchStat = -1;
            return null;
        } else {
            this.bp += 4;
            this.ch = this.text.charAt(this.bp);
            return null;
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public boolean matchField2(char[] fieldName) {
        while (isWhitespace(this.ch)) {
            next();
        }
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return false;
        }
        int offset = this.bp + fieldName.length;
        int offset2 = offset + 1;
        char ch = this.text.charAt(offset);
        while (isWhitespace(ch)) {
            ch = this.text.charAt(offset2);
            offset2++;
        }
        if (ch == ':') {
            this.bp = offset2;
            this.ch = charAt(this.bp);
            return true;
        }
        this.matchStat = -2;
        return false;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void skipObject() {
        skipObject(false);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void skipObject(boolean valid) {
        boolean quote = false;
        int braceCnt = 0;
        int i = this.bp;
        while (i < this.text.length()) {
            char ch = this.text.charAt(i);
            if (ch == '\\') {
                if (i < this.len - 1) {
                    i++;
                } else {
                    this.ch = ch;
                    this.bp = i;
                    throw new JSONException("illegal str, " + info());
                }
            } else if (ch == '\"') {
                quote = !quote;
            } else if (ch == '{') {
                if (!quote) {
                    braceCnt++;
                }
            } else if (ch == '}' && !quote && braceCnt - 1 == -1) {
                this.bp = i + 1;
                int i2 = this.bp;
                int length = this.text.length();
                char c = JSONLexer.EOI;
                if (i2 == length) {
                    this.ch = JSONLexer.EOI;
                    this.token = 20;
                    return;
                }
                this.ch = this.text.charAt(this.bp);
                if (this.ch == ',') {
                    this.token = 16;
                    int index = this.bp + 1;
                    this.bp = index;
                    if (index < this.text.length()) {
                        c = this.text.charAt(index);
                    }
                    this.ch = c;
                    return;
                } else if (this.ch == '}') {
                    this.token = 13;
                    next();
                    return;
                } else if (this.ch == ']') {
                    this.token = 15;
                    next();
                    return;
                } else {
                    nextToken(16);
                    return;
                }
            }
            i++;
        }
        if (i == this.text.length()) {
            throw new JSONException("illegal str, " + info());
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void skipArray() {
        skipArray(false);
    }

    public final void skipArray(boolean valid) {
        boolean quote = false;
        int bracketCnt = 0;
        int i = this.bp;
        while (i < this.text.length()) {
            char ch = this.text.charAt(i);
            if (ch == '\\') {
                if (i < this.len - 1) {
                    i++;
                } else {
                    this.ch = ch;
                    this.bp = i;
                    throw new JSONException("illegal str, " + info());
                }
            } else if (ch == '\"') {
                quote = !quote;
            } else if (ch != '[') {
                char c = JSONLexer.EOI;
                if (ch == '{' && valid) {
                    int index = this.bp + 1;
                    this.bp = index;
                    if (index < this.text.length()) {
                        c = this.text.charAt(index);
                    }
                    this.ch = c;
                    skipObject(valid);
                } else if (ch == ']' && !quote && bracketCnt - 1 == -1) {
                    this.bp = i + 1;
                    if (this.bp == this.text.length()) {
                        this.ch = JSONLexer.EOI;
                        this.token = 20;
                        return;
                    }
                    this.ch = this.text.charAt(this.bp);
                    nextToken(16);
                    return;
                }
            } else if (!quote) {
                bracketCnt++;
            }
            i++;
        }
        if (i == this.text.length()) {
            throw new JSONException("illegal str, " + info());
        }
    }

    public final void skipString() {
        if (this.ch == '\"') {
            int i = this.bp;
            while (true) {
                i++;
                if (i < this.text.length()) {
                    char c = this.text.charAt(i);
                    if (c == '\\') {
                        if (i < this.len - 1) {
                            i++;
                        }
                    } else if (c == '\"') {
                        String str = this.text;
                        int i2 = i + 1;
                        this.bp = i2;
                        this.ch = str.charAt(i2);
                        return;
                    }
                } else {
                    throw new JSONException("unclosed str");
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public boolean seekArrayToItem(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must > 0, but " + index);
        } else if (this.token == 20) {
            return false;
        } else {
            if (this.token == 14) {
                for (int i = 0; i < index; i++) {
                    skipWhitespace();
                    if (this.ch == '\"' || this.ch == '\'') {
                        skipString();
                        if (this.ch == ',') {
                            next();
                        } else if (this.ch == ']') {
                            next();
                            nextToken(16);
                            return false;
                        } else {
                            throw new JSONException("illegal json.");
                        }
                    } else {
                        if (this.ch == '{') {
                            next();
                            this.token = 12;
                            skipObject(false);
                        } else if (this.ch == '[') {
                            next();
                            this.token = 14;
                            skipArray(false);
                        } else {
                            boolean match = false;
                            int j = this.bp + 1;
                            while (true) {
                                if (j >= this.text.length()) {
                                    break;
                                }
                                char c = this.text.charAt(j);
                                if (c == ',') {
                                    match = true;
                                    this.bp = j + 1;
                                    this.ch = charAt(this.bp);
                                    break;
                                } else if (c == ']') {
                                    this.bp = j + 1;
                                    this.ch = charAt(this.bp);
                                    nextToken();
                                    return false;
                                } else {
                                    j++;
                                }
                            }
                            if (!match) {
                                throw new JSONException("illegal json.");
                            }
                        }
                        if (this.token != 16) {
                            if (this.token == 15) {
                                return false;
                            }
                            throw new UnsupportedOperationException();
                        }
                    }
                }
                nextToken();
                return true;
            }
            throw new UnsupportedOperationException();
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public int seekObjectToField(long fieldNameHash, boolean deepScan) {
        int i;
        int i2 = -1;
        if (this.token == 20) {
            return -1;
        }
        int i3 = 13;
        if (this.token == 13 || this.token == 15) {
            nextToken();
            return -1;
        } else if (this.token == 12 || this.token == 16) {
            while (this.ch != '}') {
                if (this.ch == 26) {
                    return i2;
                }
                if (this.ch != '\"') {
                    skipWhitespace();
                }
                if (this.ch == '\"') {
                    long hash = -3750763034362895579L;
                    int i4 = this.bp + 1;
                    while (true) {
                        if (i4 >= this.text.length()) {
                            break;
                        }
                        char c = this.text.charAt(i4);
                        if (c == '\\') {
                            i4++;
                            if (i4 != this.text.length()) {
                                c = this.text.charAt(i4);
                            } else {
                                throw new JSONException("unclosed str, " + info());
                            }
                        }
                        if (c == '\"') {
                            this.bp = i4 + 1;
                            this.ch = this.bp >= this.text.length() ? 26 : this.text.charAt(this.bp);
                        } else {
                            hash = (hash ^ ((long) c)) * 1099511628211L;
                            i4++;
                        }
                    }
                    if (hash == fieldNameHash) {
                        if (this.ch != ':') {
                            skipWhitespace();
                        }
                        if (this.ch != ':') {
                            return 3;
                        }
                        int index = this.bp + 1;
                        this.bp = index;
                        this.ch = index >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index);
                        if (this.ch == 44) {
                            int index2 = this.bp + 1;
                            this.bp = index2;
                            this.ch = index2 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index2);
                            this.token = 16;
                            return 3;
                        } else if (this.ch == ']') {
                            int index3 = this.bp + 1;
                            this.bp = index3;
                            this.ch = index3 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index3);
                            this.token = 15;
                            return 3;
                        } else if (this.ch == '}') {
                            int index4 = this.bp + 1;
                            this.bp = index4;
                            this.ch = index4 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index4);
                            this.token = i3;
                            return 3;
                        } else if (this.ch < '0' || this.ch > '9') {
                            nextToken(2);
                            return 3;
                        } else {
                            this.sp = 0;
                            this.pos = this.bp;
                            scanNumber();
                            return 3;
                        }
                    } else {
                        if (this.ch != ':') {
                            skipWhitespace();
                        }
                        if (this.ch == ':') {
                            int index5 = this.bp + 1;
                            this.bp = index5;
                            this.ch = index5 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index5);
                            if (!(this.ch == '\"' || this.ch == '\'' || this.ch == '{' || this.ch == '[' || this.ch == '0' || this.ch == '1' || this.ch == '2' || this.ch == '3' || this.ch == '4' || this.ch == '5' || this.ch == '6' || this.ch == '7' || this.ch == '8' || this.ch == '9' || this.ch == '+' || this.ch == '-')) {
                                skipWhitespace();
                            }
                            if (this.ch == '-' || this.ch == '+' || (this.ch >= '0' && this.ch <= '9')) {
                                i = 13;
                                next();
                                while (this.ch >= '0' && this.ch <= '9') {
                                    next();
                                }
                                if (this.ch == '.') {
                                    next();
                                    while (this.ch >= '0' && this.ch <= '9') {
                                        next();
                                    }
                                }
                                if (this.ch == 'E' || this.ch == 'e') {
                                    next();
                                    if (this.ch == '-' || this.ch == '+') {
                                        next();
                                    }
                                    while (this.ch >= '0' && this.ch <= '9') {
                                        next();
                                    }
                                }
                                if (this.ch != ',') {
                                    skipWhitespace();
                                }
                                if (this.ch == ',') {
                                    next();
                                }
                            } else {
                                if (this.ch == '\"') {
                                    skipString();
                                    if (!(this.ch == ',' || this.ch == '}')) {
                                        skipWhitespace();
                                    }
                                    if (this.ch == ',') {
                                        next();
                                    }
                                } else if (this.ch == 't') {
                                    next();
                                    if (this.ch == 'r') {
                                        next();
                                        if (this.ch == 'u') {
                                            next();
                                            if (this.ch == 'e') {
                                                next();
                                            }
                                        }
                                    }
                                    if (!(this.ch == ',' || this.ch == '}')) {
                                        skipWhitespace();
                                    }
                                    if (this.ch == ',') {
                                        next();
                                    }
                                } else if (this.ch == 'n') {
                                    next();
                                    if (this.ch == 'u') {
                                        next();
                                        if (this.ch == 'l') {
                                            next();
                                            if (this.ch == 'l') {
                                                next();
                                            }
                                        }
                                    }
                                    if (!(this.ch == ',' || this.ch == '}')) {
                                        skipWhitespace();
                                    }
                                    if (this.ch == ',') {
                                        next();
                                    }
                                } else if (this.ch == 'f') {
                                    next();
                                    if (this.ch == 'a') {
                                        next();
                                        if (this.ch == 'l') {
                                            next();
                                            if (this.ch == 's') {
                                                next();
                                                if (this.ch == 'e') {
                                                    next();
                                                }
                                            }
                                        }
                                    }
                                    if (!(this.ch == ',' || this.ch == '}')) {
                                        skipWhitespace();
                                    }
                                    if (this.ch == ',') {
                                        next();
                                    }
                                } else if (this.ch == '{') {
                                    int index6 = this.bp + 1;
                                    this.bp = index6;
                                    this.ch = index6 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index6);
                                    if (deepScan) {
                                        this.token = 12;
                                        return 1;
                                    }
                                    skipObject(false);
                                    if (this.token == 13) {
                                        return -1;
                                    }
                                    i = 13;
                                } else if (this.ch == '[') {
                                    next();
                                    if (deepScan) {
                                        this.token = 14;
                                        return 2;
                                    }
                                    skipArray(false);
                                    i = 13;
                                    if (this.token == 13) {
                                        return -1;
                                    }
                                } else {
                                    throw new UnsupportedOperationException();
                                }
                                i = 13;
                            }
                            i3 = i;
                            i2 = -1;
                        } else {
                            throw new JSONException("illegal json, " + info());
                        }
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            next();
            nextToken();
            return i2;
        } else {
            throw new UnsupportedOperationException(JSONToken.name(this.token));
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public int seekObjectToField(long[] fieldNameHash) {
        int i = 16;
        if (this.token == 12 || this.token == 16) {
            while (this.ch != '}') {
                char c = this.ch;
                char c2 = JSONLexer.EOI;
                if (c == 26) {
                    this.matchStat = -1;
                    return -1;
                }
                if (this.ch != '\"') {
                    skipWhitespace();
                }
                if (this.ch == '\"') {
                    long hash = -3750763034362895579L;
                    int i2 = this.bp;
                    while (true) {
                        i2++;
                        if (i2 >= this.text.length()) {
                            break;
                        }
                        char c3 = this.text.charAt(i2);
                        if (c3 == '\\') {
                            i2++;
                            if (i2 != this.text.length()) {
                                c3 = this.text.charAt(i2);
                            } else {
                                throw new JSONException("unclosed str, " + info());
                            }
                        }
                        if (c3 == '\"') {
                            this.bp = i2 + 1;
                            this.ch = this.bp >= this.text.length() ? 26 : this.text.charAt(this.bp);
                        } else {
                            hash = (hash ^ ((long) c3)) * 1099511628211L;
                        }
                    }
                    int matchIndex = -1;
                    int i3 = 0;
                    while (true) {
                        if (i3 >= fieldNameHash.length) {
                            break;
                        } else if (hash == fieldNameHash[i3]) {
                            matchIndex = i3;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    if (matchIndex != -1) {
                        if (this.ch != ':') {
                            skipWhitespace();
                        }
                        if (this.ch == ':') {
                            int index = this.bp + 1;
                            this.bp = index;
                            this.ch = index >= this.text.length() ? 26 : this.text.charAt(index);
                            if (this.ch == 44) {
                                int index2 = this.bp + 1;
                                this.bp = index2;
                                if (index2 < this.text.length()) {
                                    c2 = this.text.charAt(index2);
                                }
                                this.ch = c2;
                                this.token = i;
                            } else if (this.ch == ']') {
                                int index3 = this.bp + 1;
                                this.bp = index3;
                                if (index3 < this.text.length()) {
                                    c2 = this.text.charAt(index3);
                                }
                                this.ch = c2;
                                this.token = 15;
                            } else if (this.ch == '}') {
                                int index4 = this.bp + 1;
                                this.bp = index4;
                                if (index4 < this.text.length()) {
                                    c2 = this.text.charAt(index4);
                                }
                                this.ch = c2;
                                this.token = 13;
                            } else if (this.ch < '0' || this.ch > '9') {
                                nextToken(2);
                            } else {
                                this.sp = 0;
                                this.pos = this.bp;
                                scanNumber();
                            }
                        }
                        this.matchStat = 3;
                        return matchIndex;
                    }
                    if (this.ch != ':') {
                        skipWhitespace();
                    }
                    if (this.ch == ':') {
                        int index5 = this.bp + 1;
                        this.bp = index5;
                        this.ch = index5 >= this.text.length() ? 26 : this.text.charAt(index5);
                        if (!(this.ch == '\"' || this.ch == '\'' || this.ch == '{' || this.ch == '[' || this.ch == '0' || this.ch == '1' || this.ch == '2' || this.ch == '3' || this.ch == '4' || this.ch == '5' || this.ch == '6' || this.ch == '7' || this.ch == '8' || this.ch == '9' || this.ch == '+' || this.ch == '-')) {
                            skipWhitespace();
                        }
                        if (this.ch == '-' || this.ch == '+' || (this.ch >= '0' && this.ch <= '9')) {
                            next();
                            while (this.ch >= '0' && this.ch <= '9') {
                                next();
                            }
                            if (this.ch == '.') {
                                next();
                                while (this.ch >= '0' && this.ch <= '9') {
                                    next();
                                }
                            }
                            if (this.ch == 'E' || this.ch == 'e') {
                                next();
                                if (this.ch == '-' || this.ch == '+') {
                                    next();
                                }
                                while (this.ch >= '0' && this.ch <= '9') {
                                    next();
                                }
                            }
                            if (this.ch != ',') {
                                skipWhitespace();
                            }
                            if (this.ch == ',') {
                                next();
                            }
                        } else if (this.ch == '\"') {
                            skipString();
                            if (!(this.ch == ',' || this.ch == '}')) {
                                skipWhitespace();
                            }
                            if (this.ch == ',') {
                                next();
                            }
                        } else if (this.ch == '{') {
                            int index6 = this.bp + 1;
                            this.bp = index6;
                            this.ch = index6 >= this.text.length() ? JSONLexer.EOI : this.text.charAt(index6);
                            skipObject(false);
                        } else if (this.ch == '[') {
                            next();
                            skipArray(false);
                        } else {
                            throw new UnsupportedOperationException();
                        }
                        i = 16;
                    } else {
                        throw new JSONException("illegal json, " + info());
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            next();
            nextToken();
            this.matchStat = -1;
            return -1;
        }
        throw new UnsupportedOperationException();
    }
}
