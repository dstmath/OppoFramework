package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.asm.Opcodes;
import com.alibaba.fastjson.util.IOUtils;
import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public abstract class JSONLexerBase implements JSONLexer, Closeable {
    protected static final int INT_MULTMIN_RADIX_TEN = -214748364;
    protected static final long MULTMIN_RADIX_TEN = -922337203685477580L;
    private static final ThreadLocal<char[]> SBUF_LOCAL = new ThreadLocal<>();
    protected static final int[] digits = new int[103];
    protected static final char[] typeFieldName = ("\"" + JSON.DEFAULT_TYPE_KEY + "\":\"").toCharArray();
    protected int bp;
    protected Calendar calendar = null;
    protected char ch;
    protected int eofPos;
    protected int features;
    protected boolean hasSpecial;
    protected Locale locale = JSON.defaultLocale;
    public int matchStat = 0;
    protected int np;
    protected int pos;
    protected char[] sbuf;
    protected int sp;
    protected String stringDefaultValue = null;
    protected TimeZone timeZone = JSON.defaultTimeZone;
    protected int token;

    public abstract String addSymbol(int i, int i2, int i3, SymbolTable symbolTable);

    /* access modifiers changed from: protected */
    public abstract void arrayCopy(int i, char[] cArr, int i2, int i3);

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public abstract byte[] bytesValue();

    /* access modifiers changed from: protected */
    public abstract boolean charArrayCompare(char[] cArr);

    public abstract char charAt(int i);

    /* access modifiers changed from: protected */
    public abstract void copyTo(int i, int i2, char[] cArr);

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public abstract BigDecimal decimalValue();

    public abstract int indexOf(char c, int i);

    public abstract boolean isEOF();

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public abstract char next();

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public abstract String numberString();

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public abstract String stringVal();

    public abstract String subString(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract char[] sub_chars(int i, int i2);

    /* access modifiers changed from: protected */
    public void lexError(String key, Object... args) {
        this.token = 1;
    }

    static {
        for (int i = 48; i <= 57; i++) {
            digits[i] = i - 48;
        }
        for (int i2 = 97; i2 <= 102; i2++) {
            digits[i2] = (i2 - 97) + 10;
        }
        for (int i3 = 65; i3 <= 70; i3++) {
            digits[i3] = (i3 - 65) + 10;
        }
    }

    public JSONLexerBase(int features2) {
        this.features = features2;
        if ((Feature.InitStringFieldAsEmpty.mask & features2) != 0) {
            this.stringDefaultValue = "";
        }
        this.sbuf = SBUF_LOCAL.get();
        if (this.sbuf == null) {
            this.sbuf = new char[512];
        }
    }

    public final int matchStat() {
        return this.matchStat;
    }

    public void setToken(int token2) {
        this.token = token2;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void nextToken() {
        this.sp = 0;
        while (true) {
            this.pos = this.bp;
            if (this.ch == '/') {
                skipComment();
            } else if (this.ch == '\"') {
                scanString();
                return;
            } else if (this.ch == ',') {
                next();
                this.token = 16;
                return;
            } else if (this.ch >= '0' && this.ch <= '9') {
                scanNumber();
                return;
            } else if (this.ch == '-') {
                scanNumber();
                return;
            } else {
                switch (this.ch) {
                    case JSONToken.NULL /* 8 */:
                    case '\t':
                    case '\n':
                    case JSONToken.LBRACE /* 12 */:
                    case JSONToken.RBRACE /* 13 */:
                    case Opcodes.ACC_SUPER /* 32 */:
                        next();
                        continue;
                    case '\'':
                        if (isEnabled(Feature.AllowSingleQuotes)) {
                            scanStringSingleQuote();
                            return;
                        }
                        throw new JSONException("Feature.AllowSingleQuotes is false");
                    case '(':
                        next();
                        this.token = 10;
                        return;
                    case ')':
                        next();
                        this.token = 11;
                        return;
                    case '+':
                        next();
                        scanNumber();
                        return;
                    case '.':
                        next();
                        this.token = 25;
                        return;
                    case Opcodes.ASTORE /* 58 */:
                        next();
                        this.token = 17;
                        return;
                    case ';':
                        next();
                        this.token = 24;
                        return;
                    case 'N':
                    case 'S':
                    case 'T':
                    case 'u':
                        scanIdent();
                        return;
                    case '[':
                        next();
                        this.token = 14;
                        return;
                    case ']':
                        next();
                        this.token = 15;
                        return;
                    case 'f':
                        scanFalse();
                        return;
                    case 'n':
                        scanNullOrNew();
                        return;
                    case 't':
                        scanTrue();
                        return;
                    case 'x':
                        scanHex();
                        return;
                    case '{':
                        next();
                        this.token = 12;
                        return;
                    case '}':
                        next();
                        this.token = 13;
                        return;
                    default:
                        if (isEOF()) {
                            if (this.token != 20) {
                                this.token = 20;
                                int i = this.bp;
                                this.pos = i;
                                this.eofPos = i;
                                return;
                            }
                            throw new JSONException("EOF error");
                        } else if (this.ch <= 31 || this.ch == 127) {
                            next();
                            continue;
                        } else {
                            lexError("illegal.char", String.valueOf((int) this.ch));
                            next();
                            return;
                        }
                }
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void nextToken(int expect) {
        this.sp = 0;
        while (true) {
            if (expect != 2) {
                if (expect != 4) {
                    if (expect != 12) {
                        if (expect != 18) {
                            if (expect != 20) {
                                switch (expect) {
                                    case 14:
                                        if (this.ch == '[') {
                                            this.token = 14;
                                            next();
                                            return;
                                        } else if (this.ch == '{') {
                                            this.token = 12;
                                            next();
                                            return;
                                        }
                                        break;
                                    case JSONToken.RBRACKET /* 15 */:
                                        if (this.ch == ']') {
                                            this.token = 15;
                                            next();
                                            return;
                                        }
                                        break;
                                    case 16:
                                        if (this.ch == ',') {
                                            this.token = 16;
                                            next();
                                            return;
                                        } else if (this.ch == '}') {
                                            this.token = 13;
                                            next();
                                            return;
                                        } else if (this.ch == ']') {
                                            this.token = 15;
                                            next();
                                            return;
                                        } else if (this.ch == 26) {
                                            this.token = 20;
                                            return;
                                        } else if (this.ch == 'n') {
                                            scanNullOrNew(false);
                                            return;
                                        }
                                        break;
                                }
                            }
                            if (this.ch == 26) {
                                this.token = 20;
                                return;
                            }
                        } else {
                            nextIdent();
                            return;
                        }
                    } else if (this.ch == '{') {
                        this.token = 12;
                        next();
                        return;
                    } else if (this.ch == '[') {
                        this.token = 14;
                        next();
                        return;
                    }
                } else if (this.ch == '\"') {
                    this.pos = this.bp;
                    scanString();
                    return;
                } else if (this.ch >= '0' && this.ch <= '9') {
                    this.pos = this.bp;
                    scanNumber();
                    return;
                } else if (this.ch == '[') {
                    this.token = 14;
                    next();
                    return;
                } else if (this.ch == '{') {
                    this.token = 12;
                    next();
                    return;
                }
            } else if (this.ch >= '0' && this.ch <= '9') {
                this.pos = this.bp;
                scanNumber();
                return;
            } else if (this.ch == '\"') {
                this.pos = this.bp;
                scanString();
                return;
            } else if (this.ch == '[') {
                this.token = 14;
                next();
                return;
            } else if (this.ch == '{') {
                this.token = 12;
                next();
                return;
            }
            if (this.ch == ' ' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b') {
                next();
            } else {
                nextToken();
                return;
            }
        }
    }

    public final void nextIdent() {
        while (isWhitespace(this.ch)) {
            next();
        }
        if (this.ch == '_' || this.ch == '$' || Character.isLetter(this.ch)) {
            scanIdent();
        } else {
            nextToken();
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void nextTokenWithColon() {
        nextTokenWithChar(':');
    }

    public final void nextTokenWithChar(char expect) {
        this.sp = 0;
        while (this.ch != expect) {
            if (this.ch == ' ' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b') {
                next();
            } else {
                throw new JSONException("not match " + expect + " - " + this.ch + ", info : " + info());
            }
        }
        next();
        nextToken();
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final int token() {
        return this.token;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final String tokenName() {
        return JSONToken.name(this.token);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final int pos() {
        return this.pos;
    }

    public final String stringDefaultValue() {
        return this.stringDefaultValue;
    }

    /* JADX INFO: Multiple debug info for r4v6 int: [D('i' int), D('digit' int)] */
    /* JADX INFO: Multiple debug info for r4v9 int: [D('i' int), D('digit' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final Number integerValue() throws NumberFormatException {
        long limit;
        long result = 0;
        boolean negative = false;
        if (this.np == -1) {
            this.np = 0;
        }
        int i = this.np;
        int max = this.np + this.sp;
        char type = ' ';
        char charAt = charAt(max - 1);
        if (charAt == 'B') {
            max--;
            type = 'B';
        } else if (charAt == 'L') {
            max--;
            type = 'L';
        } else if (charAt == 'S') {
            max--;
            type = 'S';
        }
        if (charAt(this.np) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i++;
        } else {
            limit = -9223372036854775807L;
        }
        if (i < max) {
            result = (long) (-(charAt(i) - 48));
            i++;
        }
        while (i < max) {
            int i2 = i + 1;
            int digit = charAt(i) - 48;
            if (result < MULTMIN_RADIX_TEN) {
                return new BigInteger(numberString());
            }
            long result2 = result * 10;
            if (result2 < ((long) digit) + limit) {
                return new BigInteger(numberString());
            }
            result = result2 - ((long) digit);
            i = i2;
        }
        if (!negative) {
            long result3 = -result;
            if (result3 > 2147483647L || type == 'L') {
                return Long.valueOf(result3);
            }
            if (type == 'S') {
                return Short.valueOf((short) ((int) result3));
            }
            if (type == 'B') {
                return Byte.valueOf((byte) ((int) result3));
            }
            return Integer.valueOf((int) result3);
        } else if (i <= this.np + 1) {
            throw new NumberFormatException(numberString());
        } else if (result < -2147483648L || type == 'L') {
            return Long.valueOf(result);
        } else {
            if (type == 'S') {
                return Short.valueOf((short) ((int) result));
            }
            if (type == 'B') {
                return Byte.valueOf((byte) ((int) result));
            }
            return Integer.valueOf((int) result);
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void nextTokenWithColon(int expect) {
        nextTokenWithChar(':');
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public float floatValue() {
        char c0;
        String strVal = numberString();
        float floatValue = Float.parseFloat(strVal);
        if ((floatValue != 0.0f && floatValue != Float.POSITIVE_INFINITY) || (c0 = strVal.charAt(0)) <= '0' || c0 > '9') {
            return floatValue;
        }
        throw new JSONException("float overflow : " + strVal);
    }

    public double doubleValue() {
        return Double.parseDouble(numberString());
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public void config(Feature feature, boolean state) {
        this.features = Feature.config(this.features, feature, state);
        if ((this.features & Feature.InitStringFieldAsEmpty.mask) != 0) {
            this.stringDefaultValue = "";
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final boolean isEnabled(Feature feature) {
        return isEnabled(feature.mask);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final boolean isEnabled(int feature) {
        return (this.features & feature) != 0;
    }

    public final boolean isEnabled(int features2, int feature) {
        return ((this.features & feature) == 0 && (features2 & feature) == 0) ? false : true;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final char getCurrent() {
        return this.ch;
    }

    /* access modifiers changed from: protected */
    public void skipComment() {
        next();
        if (this.ch == '/') {
            do {
                next();
                if (this.ch == '\n') {
                    next();
                    return;
                }
            } while (this.ch != 26);
        } else if (this.ch == '*') {
            next();
            while (this.ch != 26) {
                if (this.ch == '*') {
                    next();
                    if (this.ch == '/') {
                        next();
                        return;
                    }
                } else {
                    next();
                }
            }
        } else {
            throw new JSONException("invalid comment");
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final String scanSymbol(SymbolTable symbolTable) {
        skipWhitespace();
        if (this.ch == '\"') {
            return scanSymbol(symbolTable, '\"');
        }
        if (this.ch == '\'') {
            if (isEnabled(Feature.AllowSingleQuotes)) {
                return scanSymbol(symbolTable, '\'');
            }
            throw new JSONException("syntax error");
        } else if (this.ch == '}') {
            next();
            this.token = 13;
            return null;
        } else if (this.ch == ',') {
            next();
            this.token = 16;
            return null;
        } else if (this.ch == 26) {
            this.token = 20;
            return null;
        } else if (isEnabled(Feature.AllowUnQuotedFieldNames)) {
            return scanSymbolUnQuoted(symbolTable);
        } else {
            throw new JSONException("syntax error");
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final String scanSymbol(SymbolTable symbolTable, char quote) {
        String value;
        int offset;
        this.np = this.bp;
        this.sp = 0;
        int hash = 0;
        boolean hasSpecial2 = false;
        while (true) {
            char chLocal = next();
            if (chLocal == quote) {
                this.token = 4;
                if (!hasSpecial2) {
                    if (this.np == -1) {
                        offset = 0;
                    } else {
                        offset = this.np + 1;
                    }
                    value = addSymbol(offset, this.sp, hash, symbolTable);
                } else {
                    value = symbolTable.addSymbol(this.sbuf, 0, this.sp, hash);
                }
                this.sp = 0;
                next();
                return value;
            } else if (chLocal == 26) {
                throw new JSONException("unclosed.str");
            } else if (chLocal == '\\') {
                if (!hasSpecial2) {
                    hasSpecial2 = true;
                    if (this.sp >= this.sbuf.length) {
                        int newCapcity = this.sbuf.length * 2;
                        if (this.sp > newCapcity) {
                            newCapcity = this.sp;
                        }
                        char[] newsbuf = new char[newCapcity];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    arrayCopy(this.np + 1, this.sbuf, 0, this.sp);
                }
                char chLocal2 = next();
                switch (chLocal2) {
                    case '/':
                        hash = (31 * hash) + 47;
                        putChar('/');
                        continue;
                    case '0':
                        hash = (31 * hash) + chLocal2;
                        putChar(0);
                        continue;
                    case Opcodes.V1_5 /* 49 */:
                        hash = (31 * hash) + chLocal2;
                        putChar(1);
                        continue;
                    case '2':
                        hash = (31 * hash) + chLocal2;
                        putChar(2);
                        continue;
                    case '3':
                        hash = (31 * hash) + chLocal2;
                        putChar(3);
                        continue;
                    case '4':
                        hash = (31 * hash) + chLocal2;
                        putChar(4);
                        continue;
                    case '5':
                        hash = (31 * hash) + chLocal2;
                        putChar(5);
                        continue;
                    case Opcodes.ISTORE /* 54 */:
                        hash = (31 * hash) + chLocal2;
                        putChar(6);
                        continue;
                    case Opcodes.LSTORE /* 55 */:
                        hash = (31 * hash) + chLocal2;
                        putChar(7);
                        continue;
                    default:
                        switch (chLocal2) {
                            case 't':
                                hash = (31 * hash) + 9;
                                putChar('\t');
                                continue;
                            case 'u':
                                int val = Integer.parseInt(new String(new char[]{next(), next(), next(), next()}), 16);
                                hash = (31 * hash) + val;
                                putChar((char) val);
                                continue;
                            case 'v':
                                hash = (31 * hash) + 11;
                                putChar(11);
                                continue;
                            default:
                                switch (chLocal2) {
                                    case '\"':
                                        hash = (31 * hash) + 34;
                                        putChar('\"');
                                        continue;
                                    case '\'':
                                        hash = (31 * hash) + 39;
                                        putChar('\'');
                                        continue;
                                    case 'F':
                                    case 'f':
                                        hash = (31 * hash) + 12;
                                        putChar('\f');
                                        continue;
                                    case '\\':
                                        hash = (31 * hash) + 92;
                                        putChar('\\');
                                        continue;
                                    case 'b':
                                        hash = (31 * hash) + 8;
                                        putChar('\b');
                                        continue;
                                    case 'n':
                                        hash = (31 * hash) + 10;
                                        putChar('\n');
                                        continue;
                                    case 'r':
                                        hash = (31 * hash) + 13;
                                        putChar('\r');
                                        continue;
                                    case 'x':
                                        char x1 = next();
                                        this.ch = x1;
                                        char x2 = next();
                                        this.ch = x2;
                                        char x_char = (char) ((digits[x1] * 16) + digits[x2]);
                                        hash = (31 * hash) + x_char;
                                        putChar(x_char);
                                        continue;
                                        continue;
                                        continue;
                                    default:
                                        this.ch = chLocal2;
                                        throw new JSONException("unclosed.str.lit");
                                }
                        }
                }
            } else {
                hash = (31 * hash) + chLocal;
                if (!hasSpecial2) {
                    this.sp++;
                } else if (this.sp == this.sbuf.length) {
                    putChar(chLocal);
                } else {
                    char[] cArr = this.sbuf;
                    int i = this.sp;
                    this.sp = i + 1;
                    cArr[i] = chLocal;
                }
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void resetStringPosition() {
        this.sp = 0;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public String info() {
        return "";
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final String scanSymbolUnQuoted(SymbolTable symbolTable) {
        boolean firstFlag = false;
        if (this.token == 1 && this.pos == 0 && this.bp == 1) {
            this.bp = 0;
        }
        boolean[] firstIdentifierFlags = IOUtils.firstIdentifierFlags;
        char first = this.ch;
        if (this.ch >= firstIdentifierFlags.length || firstIdentifierFlags[first]) {
            firstFlag = true;
        }
        if (firstFlag) {
            boolean[] identifierFlags = IOUtils.identifierFlags;
            int hash = first;
            this.np = this.bp;
            this.sp = 1;
            while (true) {
                char chLocal = next();
                if (chLocal < identifierFlags.length && !identifierFlags[chLocal]) {
                    break;
                }
                hash = (31 * hash) + chLocal;
                this.sp++;
            }
            this.ch = charAt(this.bp);
            this.token = 18;
            if (this.sp == 4 && hash == 3392903 && charAt(this.np) == 'n' && charAt(this.np + 1) == 'u' && charAt(this.np + 2) == 'l' && charAt(this.np + 3) == 'l') {
                return null;
            }
            if (symbolTable == null) {
                return subString(this.np, this.sp);
            }
            return addSymbol(this.np, this.sp, hash, symbolTable);
        }
        throw new JSONException("illegal identifier : " + this.ch + info());
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void scanString() {
        this.np = this.bp;
        this.hasSpecial = false;
        while (true) {
            char ch2 = next();
            if (ch2 == '\"') {
                this.token = 4;
                this.ch = next();
                return;
            } else if (ch2 == 26) {
                if (!isEOF()) {
                    putChar(JSONLexer.EOI);
                } else {
                    throw new JSONException("unclosed string : " + ch2);
                }
            } else if (ch2 == '\\') {
                if (!this.hasSpecial) {
                    this.hasSpecial = true;
                    if (this.sp >= this.sbuf.length) {
                        int newCapcity = this.sbuf.length * 2;
                        if (this.sp > newCapcity) {
                            newCapcity = this.sp;
                        }
                        char[] newsbuf = new char[newCapcity];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    copyTo(this.np + 1, this.sp, this.sbuf);
                }
                char ch3 = next();
                switch (ch3) {
                    case '/':
                        putChar('/');
                        continue;
                    case '0':
                        putChar(0);
                        continue;
                    case Opcodes.V1_5 /* 49 */:
                        putChar(1);
                        continue;
                    case '2':
                        putChar(2);
                        continue;
                    case '3':
                        putChar(3);
                        continue;
                    case '4':
                        putChar(4);
                        continue;
                    case '5':
                        putChar(5);
                        continue;
                    case Opcodes.ISTORE /* 54 */:
                        putChar(6);
                        continue;
                    case Opcodes.LSTORE /* 55 */:
                        putChar(7);
                        continue;
                    default:
                        switch (ch3) {
                            case 't':
                                putChar('\t');
                                continue;
                            case 'u':
                                putChar((char) Integer.parseInt(new String(new char[]{next(), next(), next(), next()}), 16));
                                continue;
                            case 'v':
                                putChar(11);
                                continue;
                            default:
                                switch (ch3) {
                                    case '\"':
                                        putChar('\"');
                                        continue;
                                    case '\'':
                                        putChar('\'');
                                        continue;
                                    case 'F':
                                    case 'f':
                                        putChar('\f');
                                        continue;
                                    case '\\':
                                        putChar('\\');
                                        continue;
                                    case 'b':
                                        putChar('\b');
                                        continue;
                                    case 'n':
                                        putChar('\n');
                                        continue;
                                    case 'r':
                                        putChar('\r');
                                        continue;
                                    case 'x':
                                        putChar((char) ((digits[next()] * 16) + digits[next()]));
                                        continue;
                                        continue;
                                        continue;
                                    default:
                                        this.ch = ch3;
                                        throw new JSONException("unclosed string : " + ch3);
                                }
                        }
                }
            } else if (!this.hasSpecial) {
                this.sp++;
            } else if (this.sp == this.sbuf.length) {
                putChar(ch2);
            } else {
                char[] cArr = this.sbuf;
                int i = this.sp;
                this.sp = i + 1;
                cArr[i] = ch2;
            }
        }
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public void setTimeZone(TimeZone timeZone2) {
        this.timeZone = timeZone2;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public Locale getLocale() {
        return this.locale;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    /* JADX INFO: Multiple debug info for r2v4 char: [D('i' int), D('chLocal' char)] */
    /* JADX INFO: Multiple debug info for r2v8 int: [D('i' int), D('digit' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0035  */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final int intValue() {
        int limit;
        int i;
        if (this.np == -1) {
            this.np = 0;
        }
        int result = 0;
        boolean negative = false;
        int i2 = this.np;
        int max = this.np + this.sp;
        if (charAt(this.np) == '-') {
            negative = true;
            limit = Integer.MIN_VALUE;
            i2++;
        } else {
            limit = -2147483647;
        }
        if (i2 < max) {
            i = i2 + 1;
            result = -(charAt(i2) - 48);
            i2 = i;
        }
        if (i2 < max) {
            i = i2 + 1;
            char chLocal = charAt(i2);
            if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B') {
                i2 = i;
            } else {
                int digit = chLocal - '0';
                if (((long) result) >= -214748364) {
                    int result2 = result * 10;
                    if (result2 >= limit + digit) {
                        result = result2 - digit;
                        i2 = i;
                        if (i2 < max) {
                        }
                    }
                    throw new NumberFormatException(numberString());
                }
                throw new NumberFormatException(numberString());
            }
        }
        if (!negative) {
            return -result;
        }
        if (i2 > this.np + 1) {
            return result;
        }
        throw new NumberFormatException(numberString());
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (this.sbuf.length <= 8192) {
            SBUF_LOCAL.set(this.sbuf);
        }
        this.sbuf = null;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final boolean isRef() {
        if (this.sp == 4 && charAt(this.np + 1) == '$' && charAt(this.np + 2) == 'r' && charAt(this.np + 3) == 'e' && charAt(this.np + 4) == 'f') {
            return true;
        }
        return false;
    }

    public final int scanType(String type) {
        this.matchStat = 0;
        if (!charArrayCompare(typeFieldName)) {
            return -2;
        }
        int bpLocal = this.bp + typeFieldName.length;
        int typeLength = type.length();
        for (int i = 0; i < typeLength; i++) {
            if (type.charAt(i) != charAt(bpLocal + i)) {
                return -1;
            }
        }
        int bpLocal2 = bpLocal + typeLength;
        if (charAt(bpLocal2) != '\"') {
            return -1;
        }
        int bpLocal3 = bpLocal2 + 1;
        this.ch = charAt(bpLocal3);
        if (this.ch == ',') {
            int bpLocal4 = bpLocal3 + 1;
            this.ch = charAt(bpLocal4);
            this.bp = bpLocal4;
            this.token = 16;
            return 3;
        }
        if (this.ch == '}') {
            bpLocal3++;
            this.ch = charAt(bpLocal3);
            if (this.ch == ',') {
                this.token = 16;
                bpLocal3++;
                this.ch = charAt(bpLocal3);
            } else if (this.ch == ']') {
                this.token = 15;
                bpLocal3++;
                this.ch = charAt(bpLocal3);
            } else if (this.ch == '}') {
                this.token = 13;
                bpLocal3++;
                this.ch = charAt(bpLocal3);
            } else if (this.ch != 26) {
                return -1;
            } else {
                this.token = 20;
            }
            this.matchStat = 4;
        }
        this.bp = bpLocal3;
        return this.matchStat;
    }

    public final boolean matchField(char[] fieldName) {
        while (!charArrayCompare(fieldName)) {
            if (!isWhitespace(this.ch)) {
                return false;
            }
            next();
        }
        this.bp += fieldName.length;
        this.ch = charAt(this.bp);
        if (this.ch == '{') {
            next();
            this.token = 12;
        } else if (this.ch == '[') {
            next();
            this.token = 14;
        } else if (this.ch == 'S' && charAt(this.bp + 1) == 'e' && charAt(this.bp + 2) == 't' && charAt(this.bp + 3) == '[') {
            this.bp += 3;
            this.ch = charAt(this.bp);
            this.token = 21;
        } else {
            nextToken();
        }
        return true;
    }

    public int matchField(long fieldNameHash) {
        throw new UnsupportedOperationException();
    }

    public boolean seekArrayToItem(int index) {
        throw new UnsupportedOperationException();
    }

    public int seekObjectToField(long fieldNameHash, boolean deepScan) {
        throw new UnsupportedOperationException();
    }

    public int seekObjectToField(long[] fieldNameHash) {
        throw new UnsupportedOperationException();
    }

    public int seekObjectToFieldDeepScan(long fieldNameHash) {
        throw new UnsupportedOperationException();
    }

    public void skipObject() {
        throw new UnsupportedOperationException();
    }

    public void skipObject(boolean valid) {
        throw new UnsupportedOperationException();
    }

    public void skipArray() {
        throw new UnsupportedOperationException();
    }

    public String scanFieldString(char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return stringDefaultValue();
        }
        int offset = fieldName.length;
        int offset2 = offset + 1;
        if (charAt(this.bp + offset) != '\"') {
            this.matchStat = -1;
            return stringDefaultValue();
        }
        int endIndex = indexOf('\"', this.bp + fieldName.length + 1);
        if (endIndex != -1) {
            int startIndex2 = this.bp + fieldName.length + 1;
            String stringVal = subString(startIndex2, endIndex - startIndex2);
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
            int offset3 = offset2 + (endIndex - ((this.bp + fieldName.length) + 1)) + 1;
            int offset4 = offset3 + 1;
            char chLocal = charAt(this.bp + offset3);
            if (chLocal == ',') {
                this.bp += offset4;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                return stringVal;
            } else if (chLocal == '}') {
                int offset5 = offset4 + 1;
                char chLocal2 = charAt(this.bp + offset4);
                if (chLocal2 == ',') {
                    this.token = 16;
                    this.bp += offset5;
                    this.ch = charAt(this.bp);
                } else if (chLocal2 == ']') {
                    this.token = 15;
                    this.bp += offset5;
                    this.ch = charAt(this.bp);
                } else if (chLocal2 == '}') {
                    this.token = 13;
                    this.bp += offset5;
                    this.ch = charAt(this.bp);
                } else if (chLocal2 == 26) {
                    this.token = 20;
                    this.bp += offset5 - 1;
                    this.ch = JSONLexer.EOI;
                } else {
                    this.matchStat = -1;
                    return stringDefaultValue();
                }
                this.matchStat = 4;
                return stringVal;
            } else {
                this.matchStat = -1;
                return stringDefaultValue();
            }
        } else {
            throw new JSONException("unclosed str");
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public String scanString(char expectNextChar) {
        this.matchStat = 0;
        int offset = 0 + 1;
        char chLocal = charAt(this.bp + 0);
        if (chLocal != 'n') {
            while (chLocal != '\"') {
                if (isWhitespace(chLocal)) {
                    chLocal = charAt(this.bp + offset);
                    offset++;
                } else {
                    this.matchStat = -1;
                    return stringDefaultValue();
                }
            }
            int startIndex = this.bp + offset;
            int endIndex = indexOf('\"', startIndex);
            if (endIndex != -1) {
                String stringVal = subString(this.bp + offset, endIndex - startIndex);
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
                    int chars_len = endIndex - startIndex;
                    stringVal = readString(sub_chars(this.bp + 1, chars_len), chars_len);
                }
                int offset2 = offset + (endIndex - startIndex) + 1;
                int offset3 = offset2 + 1;
                char chLocal2 = charAt(this.bp + offset2);
                while (chLocal2 != expectNextChar) {
                    if (isWhitespace(chLocal2)) {
                        chLocal2 = charAt(this.bp + offset3);
                        offset3++;
                    } else {
                        this.matchStat = -1;
                        return stringVal;
                    }
                }
                this.bp += offset3;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return stringVal;
            }
            throw new JSONException("unclosed str");
        } else if (charAt(this.bp + offset) == 'u' && charAt(this.bp + offset + 1) == 'l' && charAt(this.bp + offset + 2) == 'l') {
            int offset4 = offset + 3;
            int offset5 = offset4 + 1;
            if (charAt(this.bp + offset4) == expectNextChar) {
                this.bp += offset5;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                return null;
            }
            this.matchStat = -1;
            return null;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public long scanFieldSymbol(char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int offset = fieldName.length;
        int offset2 = offset + 1;
        if (charAt(this.bp + offset) != '\"') {
            this.matchStat = -1;
            return 0;
        }
        long hash = -3750763034362895579L;
        while (true) {
            int offset3 = offset2 + 1;
            char chLocal = charAt(this.bp + offset2);
            if (chLocal == '\"') {
                int offset4 = offset3 + 1;
                char chLocal2 = charAt(this.bp + offset3);
                if (chLocal2 == ',') {
                    this.bp += offset4;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    return hash;
                } else if (chLocal2 == '}') {
                    int offset5 = offset4 + 1;
                    char chLocal3 = charAt(this.bp + offset4);
                    if (chLocal3 == ',') {
                        this.token = 16;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == ']') {
                        this.token = 15;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == '}') {
                        this.token = 13;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == 26) {
                        this.token = 20;
                        this.bp += offset5 - 1;
                        this.ch = JSONLexer.EOI;
                    } else {
                        this.matchStat = -1;
                        return 0;
                    }
                    this.matchStat = 4;
                    return hash;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            } else {
                hash = (hash ^ ((long) chLocal)) * 1099511628211L;
                if (chLocal == '\\') {
                    this.matchStat = -1;
                    return 0;
                }
                offset2 = offset3;
            }
        }
    }

    public long scanEnumSymbol(char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int offset = fieldName.length;
        int offset2 = offset + 1;
        if (charAt(this.bp + offset) != '\"') {
            this.matchStat = -1;
            return 0;
        }
        long hash = -3750763034362895579L;
        while (true) {
            int offset3 = offset2 + 1;
            char chLocal = charAt(this.bp + offset2);
            if (chLocal == '\"') {
                int offset4 = offset3 + 1;
                char chLocal2 = charAt(this.bp + offset3);
                if (chLocal2 == ',') {
                    this.bp += offset4;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    return hash;
                } else if (chLocal2 == '}') {
                    int offset5 = offset4 + 1;
                    char chLocal3 = charAt(this.bp + offset4);
                    if (chLocal3 == ',') {
                        this.token = 16;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == ']') {
                        this.token = 15;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == '}') {
                        this.token = 13;
                        this.bp += offset5;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == 26) {
                        this.token = 20;
                        this.bp += offset5 - 1;
                        this.ch = JSONLexer.EOI;
                    } else {
                        this.matchStat = -1;
                        return 0;
                    }
                    this.matchStat = 4;
                    return hash;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            } else {
                hash = (hash ^ ((long) ((chLocal < 'A' || chLocal > 'Z') ? chLocal : chLocal + ' '))) * 1099511628211L;
                if (chLocal == '\\') {
                    this.matchStat = -1;
                    return 0;
                }
                offset2 = offset3;
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public Enum<?> scanEnum(Class<?> enumClass, SymbolTable symbolTable, char serperator) {
        String name = scanSymbolWithSeperator(symbolTable, serperator);
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public String scanSymbolWithSeperator(SymbolTable symbolTable, char serperator) {
        this.matchStat = 0;
        int offset = 0 + 1;
        char chLocal = charAt(this.bp + 0);
        if (chLocal == 'n') {
            if (charAt(this.bp + offset) == 'u' && charAt(this.bp + offset + 1) == 'l' && charAt(this.bp + offset + 2) == 'l') {
                int offset2 = offset + 3;
                int offset3 = offset2 + 1;
                if (charAt(this.bp + offset2) == serperator) {
                    this.bp += offset3;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    return null;
                }
                this.matchStat = -1;
                return null;
            }
            this.matchStat = -1;
            return null;
        } else if (chLocal != '\"') {
            this.matchStat = -1;
            return null;
        } else {
            int hash = 0;
            while (true) {
                int offset4 = offset + 1;
                char chLocal2 = charAt(this.bp + offset);
                if (chLocal2 == '\"') {
                    int start = this.bp + 0 + 1;
                    String strVal = addSymbol(start, ((this.bp + offset4) - start) - 1, hash, symbolTable);
                    int offset5 = offset4 + 1;
                    char chLocal3 = charAt(this.bp + offset4);
                    while (chLocal3 != serperator) {
                        if (isWhitespace(chLocal3)) {
                            chLocal3 = charAt(this.bp + offset5);
                            offset5++;
                        } else {
                            this.matchStat = -1;
                            return strVal;
                        }
                    }
                    this.bp += offset5;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    return strVal;
                }
                hash = (31 * hash) + chLocal2;
                if (chLocal2 == '\\') {
                    this.matchStat = -1;
                    return null;
                }
                offset = offset4;
            }
        }
    }

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

    public Collection<String> scanFieldStringArray(char[] fieldName, Class<?> type) {
        int offset;
        char chLocal;
        int offset2;
        char chLocal2;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        Collection<String> list = newCollectionByType(type);
        int offset3 = fieldName.length;
        int offset4 = offset3 + 1;
        int i = -1;
        if (charAt(this.bp + offset3) != '[') {
            this.matchStat = -1;
            return null;
        }
        int offset5 = offset4 + 1;
        char chLocal3 = charAt(this.bp + offset4);
        while (true) {
            if (chLocal3 == '\"') {
                int endIndex = indexOf('\"', this.bp + offset5);
                if (endIndex != i) {
                    int startIndex2 = this.bp + offset5;
                    String stringVal = subString(startIndex2, endIndex - startIndex2);
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
                        int chars_len = endIndex - (this.bp + offset5);
                        stringVal = readString(sub_chars(this.bp + offset5, chars_len), chars_len);
                    }
                    int offset6 = offset5 + (endIndex - (this.bp + offset5)) + 1;
                    offset2 = offset6 + 1;
                    chLocal2 = charAt(this.bp + offset6);
                    list.add(stringVal);
                } else {
                    throw new JSONException("unclosed str");
                }
            } else if (chLocal3 == 'n' && charAt(this.bp + offset5) == 'u' && charAt(this.bp + offset5 + 1) == 'l' && charAt(this.bp + offset5 + 2) == 'l') {
                int offset7 = offset5 + 3;
                offset2 = offset7 + 1;
                chLocal2 = charAt(this.bp + offset7);
                list.add(null);
            }
            if (chLocal2 == ',') {
                offset5 = offset2 + 1;
                chLocal3 = charAt(this.bp + offset2);
                i = -1;
            } else if (chLocal2 == ']') {
                offset = offset2 + 1;
                chLocal = charAt(this.bp + offset2);
            } else {
                this.matchStat = -1;
                return null;
            }
        }
        if (chLocal3 == ']' && list.size() == 0) {
            offset = offset5 + 1;
            chLocal = charAt(this.bp + offset5);
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                return list;
            } else if (chLocal == '}') {
                int offset8 = offset + 1;
                char chLocal4 = charAt(this.bp + offset);
                if (chLocal4 == ',') {
                    this.token = 16;
                    this.bp += offset8;
                    this.ch = charAt(this.bp);
                } else if (chLocal4 == ']') {
                    this.token = 15;
                    this.bp += offset8;
                    this.ch = charAt(this.bp);
                } else if (chLocal4 == '}') {
                    this.token = 13;
                    this.bp += offset8;
                    this.ch = charAt(this.bp);
                } else if (chLocal4 == 26) {
                    this.bp += offset8 - 1;
                    this.token = 20;
                    this.ch = JSONLexer.EOI;
                } else {
                    this.matchStat = -1;
                    return null;
                }
                this.matchStat = 4;
                return list;
            } else {
                this.matchStat = -1;
                return null;
            }
        } else {
            throw new JSONException("illega str");
        }
    }

    /* JADX INFO: Multiple debug info for r4v24 int: [D('slashCount' int), D('chars_len' int)] */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public void scanStringArray(Collection<String> list, char seperator) {
        int offset;
        char chLocal;
        int offset2;
        char chLocal2;
        this.matchStat = 0;
        int offset3 = 0 + 1;
        char chLocal3 = charAt(this.bp + 0);
        char c = 'u';
        char c2 = 'n';
        char c3 = 'l';
        if (chLocal3 == 'n' && charAt(this.bp + offset3) == 'u' && charAt(this.bp + offset3 + 1) == 'l' && charAt(this.bp + offset3 + 2) == 'l' && charAt(this.bp + offset3 + 3) == seperator) {
            this.bp += 5;
            this.ch = charAt(this.bp);
            this.matchStat = 5;
        } else if (chLocal3 != '[') {
            this.matchStat = -1;
        } else {
            int offset4 = offset3 + 1;
            char chLocal4 = charAt(this.bp + offset3);
            while (true) {
                if (chLocal4 != c2 || charAt(this.bp + offset4) != c || charAt(this.bp + offset4 + 1) != c3 || charAt(this.bp + offset4 + 2) != c3) {
                    if (chLocal4 == ']' && list.size() == 0) {
                        offset = offset4 + 1;
                        chLocal = charAt(this.bp + offset4);
                        break;
                    } else if (chLocal4 != '\"') {
                        this.matchStat = -1;
                        return;
                    } else {
                        int startIndex = this.bp + offset4;
                        int endIndex = indexOf('\"', startIndex);
                        if (endIndex != -1) {
                            String stringVal = subString(this.bp + offset4, endIndex - startIndex);
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
                                int chars_len = endIndex - startIndex;
                                stringVal = readString(sub_chars(this.bp + offset4, chars_len), chars_len);
                            }
                            int offset5 = offset4 + (endIndex - (this.bp + offset4)) + 1;
                            chLocal2 = charAt(this.bp + offset5);
                            list.add(stringVal);
                            offset2 = offset5 + 1;
                        } else {
                            throw new JSONException("unclosed str");
                        }
                    }
                } else {
                    int offset6 = offset4 + 3;
                    offset2 = offset6 + 1;
                    chLocal2 = charAt(this.bp + offset6);
                    list.add(null);
                }
                if (chLocal2 == ',') {
                    offset4 = offset2 + 1;
                    chLocal4 = charAt(this.bp + offset2);
                    c = 'u';
                    c2 = 'n';
                    c3 = 'l';
                } else if (chLocal2 == ']') {
                    offset = offset2 + 1;
                    chLocal = charAt(this.bp + offset2);
                } else {
                    this.matchStat = -1;
                    return;
                }
            }
            if (chLocal == seperator) {
                this.bp += offset;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                return;
            }
            this.matchStat = -1;
        }
    }

    public int scanFieldInt(char[] fieldName) {
        int offset;
        char chLocal;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int offset2 = fieldName.length;
        int offset3 = offset2 + 1;
        char chLocal2 = charAt(this.bp + offset2);
        boolean negative = chLocal2 == '-';
        if (negative) {
            chLocal2 = charAt(this.bp + offset3);
            offset3++;
        }
        if (chLocal2 < '0' || chLocal2 > '9') {
            this.matchStat = -1;
            return 0;
        }
        int value = chLocal2 - '0';
        while (true) {
            offset = offset3 + 1;
            chLocal = charAt(this.bp + offset3);
            if (chLocal >= '0' && chLocal <= '9') {
                value = (value * 10) + (chLocal - '0');
                offset3 = offset;
            }
        }
        if (chLocal == '.') {
            this.matchStat = -1;
            return 0;
        } else if ((value < 0 || offset > 14 + fieldName.length) && !(value == Integer.MIN_VALUE && offset == 17 && negative)) {
            this.matchStat = -1;
            return 0;
        } else if (chLocal == ',') {
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? -value : value;
        } else if (chLocal == '}') {
            int offset4 = offset + 1;
            char chLocal3 = charAt(this.bp + offset);
            if (chLocal3 == ',') {
                this.token = 16;
                this.bp += offset4;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == ']') {
                this.token = 15;
                this.bp += offset4;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == '}') {
                this.token = 13;
                this.bp += offset4;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == 26) {
                this.token = 20;
                this.bp += offset4 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return 0;
            }
            this.matchStat = 4;
            return negative ? -value : value;
        } else {
            this.matchStat = -1;
            return 0;
        }
    }

    /* JADX INFO: Multiple debug info for r6v8 'arrayIndex'  int: [D('array' int[]), D('arrayIndex' int)] */
    /* JADX INFO: Multiple debug info for r6v13 int[]: [D('tmp' int[]), D('array' int[])] */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0131, code lost:
        return r4;
     */
    public final int[] scanFieldIntArray(char[] fieldName) {
        int offset;
        int arrayIndex;
        char chLocal;
        int offset2;
        int offset3;
        int offset4;
        this.matchStat = 0;
        int[] iArr = null;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset5 = fieldName.length;
        int offset6 = offset5 + 1;
        if (charAt(this.bp + offset5) != '[') {
            this.matchStat = -2;
            return null;
        }
        int arrayIndex2 = offset6 + 1;
        char chLocal2 = charAt(this.bp + offset6);
        int[] array = new int[16];
        int arrayIndex3 = 0;
        if (chLocal2 == ']') {
            offset = arrayIndex2 + 1;
            chLocal = charAt(this.bp + arrayIndex2);
            arrayIndex = 0;
        } else {
            while (true) {
                boolean nagative = false;
                if (chLocal2 == '-') {
                    offset2 = arrayIndex2 + 1;
                    chLocal2 = charAt(this.bp + arrayIndex2);
                    nagative = true;
                } else {
                    offset2 = arrayIndex2;
                }
                if (chLocal2 < '0' || chLocal2 > '9') {
                    this.matchStat = -1;
                } else {
                    int offset7 = chLocal2 - '0';
                    while (true) {
                        offset3 = offset2 + 1;
                        chLocal2 = charAt(this.bp + offset2);
                        if (chLocal2 >= '0' && chLocal2 <= '9') {
                            offset2 = offset3;
                            offset7 = (offset7 * 10) + (chLocal2 - '0');
                        }
                    }
                    if (arrayIndex3 >= array.length) {
                        int[] array2 = new int[((array.length * 3) / 2)];
                        System.arraycopy(array, 0, array2, 0, arrayIndex3);
                        array = array2;
                    }
                    arrayIndex = arrayIndex3 + 1;
                    array[arrayIndex3] = nagative ? -offset7 : offset7;
                    if (chLocal2 == ',') {
                        offset4 = offset3 + 1;
                        chLocal2 = charAt(this.bp + offset3);
                        iArr = null;
                    } else if (chLocal2 == ']') {
                        offset = offset3 + 1;
                        chLocal = charAt(this.bp + offset3);
                        break;
                    } else {
                        iArr = null;
                        offset4 = offset3;
                    }
                    arrayIndex3 = arrayIndex;
                    arrayIndex2 = offset4;
                }
            }
        }
        if (arrayIndex != array.length) {
            int[] tmp = new int[arrayIndex];
            System.arraycopy(array, 0, tmp, 0, arrayIndex);
            array = tmp;
        }
        if (chLocal == ',') {
            this.bp += offset - 1;
            next();
            this.matchStat = 3;
            this.token = 16;
            return array;
        } else if (chLocal == '}') {
            int offset8 = offset + 1;
            char chLocal3 = charAt(this.bp + offset);
            if (chLocal3 == ',') {
                this.token = 16;
                this.bp += offset8 - 1;
                next();
            } else if (chLocal3 == ']') {
                this.token = 15;
                this.bp += offset8 - 1;
                next();
            } else if (chLocal3 == '}') {
                this.token = 13;
                this.bp += offset8 - 1;
                next();
            } else if (chLocal3 == 26) {
                this.bp += offset8 - 1;
                this.token = 20;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return array;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00bd  */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public boolean scanBoolean(char expectNext) {
        int offset;
        this.matchStat = 0;
        int offset2 = 0 + 1;
        char chLocal = charAt(this.bp + 0);
        boolean value = false;
        if (chLocal != 't') {
            if (chLocal == 'f') {
                if (charAt(this.bp + offset2) == 'a' && charAt(this.bp + offset2 + 1) == 'l' && charAt(this.bp + offset2 + 2) == 's' && charAt(this.bp + offset2 + 3) == 'e') {
                    int offset3 = offset2 + 4;
                    offset = offset3 + 1;
                    chLocal = charAt(this.bp + offset3);
                    value = false;
                } else {
                    this.matchStat = -1;
                    return false;
                }
            } else if (chLocal == '1') {
                offset = offset2 + 1;
                chLocal = charAt(this.bp + offset2);
                value = true;
            } else if (chLocal == '0') {
                offset = offset2 + 1;
                chLocal = charAt(this.bp + offset2);
                value = false;
            }
            if (chLocal != expectNext) {
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return value;
        } else if (charAt(this.bp + offset2) == 'r' && charAt(this.bp + offset2 + 1) == 'u' && charAt(this.bp + offset2 + 2) == 'e') {
            int offset4 = offset2 + 3;
            offset = offset4 + 1;
            chLocal = charAt(this.bp + offset4);
            value = true;
            if (chLocal != expectNext) {
                if (isWhitespace(chLocal)) {
                    offset2 = offset + 1;
                    chLocal = charAt(this.bp + offset);
                }
                this.matchStat = -1;
                return value;
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return value;
        } else {
            this.matchStat = -1;
            return false;
        }
        offset = offset2;
        if (chLocal != expectNext) {
        }
        this.bp += offset;
        this.ch = charAt(this.bp);
        this.matchStat = 3;
        return value;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public int scanInt(char expectNext) {
        int offset;
        int offset2;
        char chLocal;
        this.matchStat = 0;
        int offset3 = 0 + 1;
        char chLocal2 = charAt(this.bp + 0);
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(this.bp + offset3);
            offset3++;
        }
        boolean negative = chLocal2 == '-';
        if (negative) {
            chLocal2 = charAt(this.bp + offset3);
            offset3++;
        }
        if (chLocal2 >= '0' && chLocal2 <= '9') {
            int value = chLocal2 - '0';
            while (true) {
                offset2 = offset3 + 1;
                chLocal = charAt(this.bp + offset3);
                if (chLocal >= '0' && chLocal <= '9') {
                    value = (value * 10) + (chLocal - '0');
                    offset3 = offset2;
                }
            }
            if (chLocal == '.') {
                this.matchStat = -1;
                return 0;
            } else if (value < 0) {
                this.matchStat = -1;
                return 0;
            } else {
                while (chLocal != expectNext) {
                    if (isWhitespace(chLocal)) {
                        chLocal = charAt(this.bp + offset2);
                        offset2++;
                    } else {
                        this.matchStat = -1;
                        return negative ? -value : value;
                    }
                }
                this.bp += offset2;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return negative ? -value : value;
            }
        } else if (chLocal2 == 'n' && charAt(this.bp + offset3) == 'u' && charAt(this.bp + offset3 + 1) == 'l' && charAt(this.bp + offset3 + 2) == 'l') {
            this.matchStat = 5;
            int offset4 = offset3 + 3;
            int offset5 = offset4 + 1;
            char chLocal3 = charAt(this.bp + offset4);
            if (!quote || chLocal3 != '\"') {
                offset = offset5;
            } else {
                offset = offset5 + 1;
                chLocal3 = charAt(this.bp + offset5);
            }
            while (chLocal3 != ',') {
                if (chLocal3 == ']') {
                    this.bp += offset;
                    this.ch = charAt(this.bp);
                    this.matchStat = 5;
                    this.token = 15;
                    return 0;
                } else if (isWhitespace(chLocal3)) {
                    chLocal3 = charAt(this.bp + offset);
                    offset++;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 5;
            this.token = 16;
            return 0;
        } else {
            this.matchStat = -1;
            return 0;
        }
    }

    public boolean scanFieldBoolean(char[] fieldName) {
        int offset;
        boolean value;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return false;
        }
        int offset2 = fieldName.length;
        int offset3 = offset2 + 1;
        char chLocal = charAt(this.bp + offset2);
        if (chLocal == 't') {
            int offset4 = offset3 + 1;
            if (charAt(this.bp + offset3) != 'r') {
                this.matchStat = -1;
                return false;
            }
            int offset5 = offset4 + 1;
            if (charAt(this.bp + offset4) != 'u') {
                this.matchStat = -1;
                return false;
            }
            int offset6 = offset5 + 1;
            if (charAt(this.bp + offset5) != 'e') {
                this.matchStat = -1;
                return false;
            }
            value = true;
            offset = offset6;
        } else if (chLocal == 'f') {
            int offset7 = offset3 + 1;
            if (charAt(this.bp + offset3) != 'a') {
                this.matchStat = -1;
                return false;
            }
            int offset8 = offset7 + 1;
            if (charAt(this.bp + offset7) != 'l') {
                this.matchStat = -1;
                return false;
            }
            int offset9 = offset8 + 1;
            if (charAt(this.bp + offset8) != 's') {
                this.matchStat = -1;
                return false;
            }
            offset = offset9 + 1;
            if (charAt(this.bp + offset9) != 'e') {
                this.matchStat = -1;
                return false;
            }
            value = false;
        } else {
            this.matchStat = -1;
            return false;
        }
        int offset10 = offset + 1;
        char chLocal2 = charAt(this.bp + offset);
        if (chLocal2 == ',') {
            this.bp += offset10;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return value;
        } else if (chLocal2 == '}') {
            int offset11 = offset10 + 1;
            char chLocal3 = charAt(this.bp + offset10);
            if (chLocal3 == ',') {
                this.token = 16;
                this.bp += offset11;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == ']') {
                this.token = 15;
                this.bp += offset11;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == '}') {
                this.token = 13;
                this.bp += offset11;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == 26) {
                this.token = 20;
                this.bp += offset11 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return false;
            }
            this.matchStat = 4;
            return value;
        } else {
            this.matchStat = -1;
            return false;
        }
    }

    public long scanFieldLong(char[] fieldName) {
        int offset;
        char chLocal;
        boolean valid = false;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int offset2 = fieldName.length;
        int offset3 = offset2 + 1;
        char chLocal2 = charAt(this.bp + offset2);
        boolean negative = false;
        if (chLocal2 == '-') {
            chLocal2 = charAt(this.bp + offset3);
            negative = true;
            offset3++;
        }
        if (chLocal2 >= '0') {
            char c = '9';
            if (chLocal2 <= '9') {
                long value = (long) (chLocal2 - '0');
                while (true) {
                    offset = offset3 + 1;
                    chLocal = charAt(this.bp + offset3);
                    if (chLocal >= '0' && chLocal <= c) {
                        value = ((long) (chLocal - '0')) + (10 * value);
                        offset3 = offset;
                        c = '9';
                    }
                }
                if (chLocal == '.') {
                    this.matchStat = -1;
                    return 0;
                }
                if (offset - fieldName.length < 21 && (value >= 0 || (value == Long.MIN_VALUE && negative))) {
                    valid = true;
                }
                if (!valid) {
                    this.matchStat = -1;
                    return 0;
                } else if (chLocal == ',') {
                    this.bp += offset;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    this.token = 16;
                    return negative ? -value : value;
                } else if (chLocal == '}') {
                    int offset4 = offset + 1;
                    char chLocal3 = charAt(this.bp + offset);
                    if (chLocal3 == ',') {
                        this.token = 16;
                        this.bp += offset4;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == ']') {
                        this.token = 15;
                        this.bp += offset4;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == '}') {
                        this.token = 13;
                        this.bp += offset4;
                        this.ch = charAt(this.bp);
                    } else if (chLocal3 == 26) {
                        this.token = 20;
                        this.bp += offset4 - 1;
                        this.ch = JSONLexer.EOI;
                    } else {
                        this.matchStat = -1;
                        return 0;
                    }
                    this.matchStat = 4;
                    return negative ? -value : value;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            }
        }
        this.matchStat = -1;
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b4  */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public long scanLong(char expectNextChar) {
        int offset;
        int offset2;
        char chLocal;
        int offset3;
        this.matchStat = 0;
        int offset4 = 0 + 1;
        char chLocal2 = charAt(this.bp + 0);
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        boolean negative = chLocal2 == '-';
        if (negative) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        if (chLocal2 >= '0') {
            char c = '9';
            if (chLocal2 <= '9') {
                long value = (long) (chLocal2 - '0');
                while (true) {
                    offset2 = offset4 + 1;
                    chLocal = charAt(this.bp + offset4);
                    if (chLocal >= '0' && chLocal <= c) {
                        value = (10 * value) + ((long) (chLocal - '0'));
                        offset4 = offset2;
                        c = '9';
                    }
                }
                if (chLocal == '.') {
                    this.matchStat = -1;
                    return 0;
                }
                if (value >= 0 || (value == Long.MIN_VALUE && negative)) {
                    if (quote) {
                        if (chLocal != '\"') {
                            this.matchStat = -1;
                            return 0;
                        }
                        offset3 = offset2 + 1;
                        chLocal = charAt(this.bp + offset2);
                        offset2 = offset3;
                    }
                    if (chLocal != expectNextChar) {
                        if (isWhitespace(chLocal)) {
                            offset3 = offset2 + 1;
                            chLocal = charAt(this.bp + offset2);
                            offset2 = offset3;
                            if (chLocal != expectNextChar) {
                            }
                        }
                        this.matchStat = -1;
                        return value;
                    }
                    this.bp += offset2;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    this.token = 16;
                    return negative ? -value : value;
                }
                throw new NumberFormatException(subString(this.bp, offset2 - 1));
            }
        }
        if (chLocal2 == 'n' && charAt(this.bp + offset4) == 'u' && charAt(this.bp + offset4 + 1) == 'l' && charAt(this.bp + offset4 + 2) == 'l') {
            this.matchStat = 5;
            int offset5 = offset4 + 3;
            int offset6 = offset5 + 1;
            char chLocal3 = charAt(this.bp + offset5);
            if (!quote || chLocal3 != '\"') {
                offset = offset6;
            } else {
                offset = offset6 + 1;
                chLocal3 = charAt(this.bp + offset6);
            }
            while (chLocal3 != ',') {
                if (chLocal3 == ']') {
                    this.bp += offset;
                    this.ch = charAt(this.bp);
                    this.matchStat = 5;
                    this.token = 15;
                    return 0;
                } else if (isWhitespace(chLocal3)) {
                    chLocal3 = charAt(this.bp + offset);
                    offset++;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 5;
            this.token = 16;
            return 0;
        }
        this.matchStat = -1;
        return 0;
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final float scanFieldFloat(char[] r25) {
        /*
        // Method dump skipped, instructions count: 623
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFieldFloat(char[]):float");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final float scanFloat(char r28) {
        /*
        // Method dump skipped, instructions count: 502
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFloat(char):float");
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public double scanDouble(char seperator) {
        int offset;
        int offset2;
        char chLocal;
        boolean negative;
        int count;
        int start;
        double value;
        int offset3;
        this.matchStat = 0;
        int offset4 = 0 + 1;
        char chLocal2 = charAt(this.bp + 0);
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        boolean negative2 = chLocal2 == '-';
        if (negative2) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        char c = '0';
        if (chLocal2 >= '0') {
            char c2 = '9';
            if (chLocal2 <= '9') {
                long intVal = (long) (chLocal2 - '0');
                while (true) {
                    offset2 = offset4 + 1;
                    chLocal = charAt(this.bp + offset4);
                    if (chLocal < '0' || chLocal > '9') {
                        long power = 1;
                    } else {
                        intVal = (10 * intVal) + ((long) (chLocal - '0'));
                        offset4 = offset2;
                    }
                }
                long power2 = 1;
                if (chLocal == '.') {
                    int offset5 = offset2 + 1;
                    char chLocal3 = charAt(this.bp + offset2);
                    if (chLocal3 < '0' || chLocal3 > '9') {
                        this.matchStat = -1;
                        return 0.0d;
                    }
                    negative = negative2;
                    power2 = 10;
                    intVal = (intVal * 10) + ((long) (chLocal3 - '0'));
                    while (true) {
                        offset3 = offset5 + 1;
                        chLocal = charAt(this.bp + offset5);
                        if (chLocal < c || chLocal > c2) {
                            offset2 = offset3;
                        } else {
                            intVal = (intVal * 10) + ((long) (chLocal - '0'));
                            power2 *= 10;
                            offset5 = offset3;
                            c = '0';
                            c2 = '9';
                        }
                    }
                    offset2 = offset3;
                } else {
                    negative = negative2;
                }
                boolean exp = chLocal == 'e' || chLocal == 'E';
                if (exp) {
                    int offset6 = offset2 + 1;
                    chLocal = charAt(this.bp + offset2);
                    if (chLocal == '+' || chLocal == '-') {
                        chLocal = charAt(this.bp + offset6);
                        offset2 = offset6 + 1;
                    } else {
                        offset2 = offset6;
                    }
                    while (chLocal >= '0' && chLocal <= '9') {
                        chLocal = charAt(this.bp + offset2);
                        offset2++;
                    }
                }
                if (!quote) {
                    start = this.bp;
                    count = ((this.bp + offset2) - start) - 1;
                } else if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0d;
                } else {
                    int offset7 = offset2 + 1;
                    chLocal = charAt(this.bp + offset2);
                    start = this.bp + 1;
                    count = ((this.bp + offset7) - start) - 2;
                    offset2 = offset7;
                }
                if (exp || count >= 17) {
                    value = Double.parseDouble(subString(start, count));
                } else {
                    value = ((double) intVal) / ((double) power2);
                    if (negative) {
                        value = -value;
                    }
                }
                if (chLocal == seperator) {
                    this.bp += offset2;
                    this.ch = charAt(this.bp);
                    this.matchStat = 3;
                    this.token = 16;
                    return value;
                }
                this.matchStat = -1;
                return value;
            }
        }
        if (chLocal2 == 'n' && charAt(this.bp + offset4) == 'u' && charAt(this.bp + offset4 + 1) == 'l' && charAt(this.bp + offset4 + 2) == 'l') {
            this.matchStat = 5;
            int offset8 = offset4 + 3;
            int offset9 = offset8 + 1;
            char chLocal4 = charAt(this.bp + offset8);
            if (!quote || chLocal4 != '\"') {
                offset = offset9;
            } else {
                offset = offset9 + 1;
                chLocal4 = charAt(this.bp + offset9);
            }
            while (chLocal4 != ',') {
                if (chLocal4 == ']') {
                    this.bp += offset;
                    this.ch = charAt(this.bp);
                    this.matchStat = 5;
                    this.token = 15;
                    return 0.0d;
                } else if (isWhitespace(chLocal4)) {
                    chLocal4 = charAt(this.bp + offset);
                    offset++;
                } else {
                    this.matchStat = -1;
                    return 0.0d;
                }
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 5;
            this.token = 16;
            return 0.0d;
        }
        this.matchStat = -1;
        return 0.0d;
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public java.math.BigDecimal scanDecimal(char r21) {
        /*
        // Method dump skipped, instructions count: 513
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanDecimal(char):java.math.BigDecimal");
    }

    /* JADX INFO: Multiple debug info for r11v2 int: [D('arrayIndex' int), D('tmp' float[])] */
    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final float[] scanFieldFloatArray(char[] r21) {
        /*
        // Method dump skipped, instructions count: 469
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFieldFloatArray(char[]):float[]");
    }

    /* JADX INFO: Multiple debug info for r12v2 int: [D('arrayIndex' int), D('array' float[])] */
    /* JADX INFO: Multiple debug info for r1v15 'arrayarrayIndex'  int: [D('arrayarray' float[][]), D('arrayarrayIndex' int)] */
    /* JADX INFO: Multiple debug info for r1v20 float[][]: [D('arrayarray' float[][]), D('tmp' float[][])] */
    /* JADX INFO: Multiple debug info for r12v6 float[]: [D('tmp' float[]), D('array' float[])] */
    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final float[][] scanFieldFloatArray2(char[] r23) {
        /*
        // Method dump skipped, instructions count: 572
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFieldFloatArray2(char[]):float[][]");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final double scanFieldDouble(char[] r27) {
        /*
        // Method dump skipped, instructions count: 630
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFieldDouble(char[]):double");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public java.math.BigDecimal scanFieldDecimal(char[] r21) {
        /*
        // Method dump skipped, instructions count: 535
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.fastjson.parser.JSONLexerBase.scanFieldDecimal(char[]):java.math.BigDecimal");
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0089  */
    public BigInteger scanFieldBigInteger(char[] fieldName) {
        int offset;
        int offset2;
        char chLocal;
        int count;
        int start;
        BigInteger value;
        boolean negative = false;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset3 = fieldName.length;
        int offset4 = offset3 + 1;
        char chLocal2 = charAt(this.bp + offset3);
        boolean quote = chLocal2 == '\"';
        if (quote) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        if (chLocal2 == '-') {
            negative = true;
        }
        if (negative) {
            chLocal2 = charAt(this.bp + offset4);
            offset4++;
        }
        char c = '0';
        if (chLocal2 >= '0' && chLocal2 <= '9') {
            long intVal = (long) (chLocal2 - '0');
            while (true) {
                offset2 = offset4 + 1;
                chLocal = charAt(this.bp + offset4);
                if (chLocal >= c && chLocal <= '9') {
                    intVal = (10 * intVal) + ((long) (chLocal - '0'));
                    offset4 = offset2;
                    c = '0';
                } else if (quote) {
                    start = this.bp + fieldName.length;
                    count = ((this.bp + offset2) - start) - 1;
                } else if (chLocal != '\"') {
                    this.matchStat = -1;
                    return null;
                } else {
                    int offset5 = offset2 + 1;
                    chLocal = charAt(this.bp + offset2);
                    start = this.bp + fieldName.length + 1;
                    count = ((this.bp + offset5) - start) - 2;
                    offset2 = offset5;
                }
            }
            if (quote) {
            }
            if (count < 20 || (negative && count < 21)) {
                value = BigInteger.valueOf(negative ? -intVal : intVal);
            } else {
                value = new BigInteger(subString(start, count));
            }
            if (chLocal == ',') {
                this.bp += offset2;
                this.ch = charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            } else if (chLocal == '}') {
                int offset6 = offset2 + 1;
                char chLocal3 = charAt(this.bp + offset2);
                if (chLocal3 == ',') {
                    this.token = 16;
                    this.bp += offset6;
                    this.ch = charAt(this.bp);
                } else if (chLocal3 == ']') {
                    this.token = 15;
                    this.bp += offset6;
                    this.ch = charAt(this.bp);
                } else if (chLocal3 == '}') {
                    this.token = 13;
                    this.bp += offset6;
                    this.ch = charAt(this.bp);
                } else if (chLocal3 == 26) {
                    this.token = 20;
                    this.bp += offset6 - 1;
                    this.ch = JSONLexer.EOI;
                } else {
                    this.matchStat = -1;
                    return null;
                }
                this.matchStat = 4;
                return value;
            } else {
                this.matchStat = -1;
                return null;
            }
        } else if (chLocal2 == 'n' && charAt(this.bp + offset4) == 'u' && charAt(this.bp + offset4 + 1) == 'l' && charAt(this.bp + offset4 + 2) == 'l') {
            this.matchStat = 5;
            int offset7 = offset4 + 3;
            int offset8 = offset7 + 1;
            char chLocal4 = charAt(this.bp + offset7);
            if (!quote || chLocal4 != '\"') {
                offset = offset8;
            } else {
                offset = offset8 + 1;
                chLocal4 = charAt(this.bp + offset8);
            }
            while (chLocal4 != ',') {
                if (chLocal4 == '}') {
                    this.bp += offset;
                    this.ch = charAt(this.bp);
                    this.matchStat = 5;
                    this.token = 13;
                    return null;
                } else if (isWhitespace(chLocal4)) {
                    chLocal4 = charAt(this.bp + offset);
                    offset++;
                } else {
                    this.matchStat = -1;
                    return null;
                }
            }
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 5;
            this.token = 16;
            return null;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public Date scanFieldDate(char[] fieldName) {
        int offset;
        Date dateVal;
        int offset2;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset3 = fieldName.length;
        int offset4 = offset3 + 1;
        char chLocal = charAt(this.bp + offset3);
        if (chLocal == '\"') {
            int endIndex = indexOf('\"', this.bp + fieldName.length + 1);
            if (endIndex != -1) {
                int startIndex2 = this.bp + fieldName.length + 1;
                String stringVal = subString(startIndex2, endIndex - startIndex2);
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
                int offset5 = offset4 + (endIndex - ((this.bp + fieldName.length) + 1)) + 1;
                offset = offset5 + 1;
                chLocal = charAt(this.bp + offset5);
                JSONScanner dateLexer = new JSONScanner(stringVal);
                try {
                    if (dateLexer.scanISO8601DateIfMatch(false)) {
                        dateVal = dateLexer.getCalendar().getTime();
                    } else {
                        this.matchStat = -1;
                        dateLexer.close();
                        return null;
                    }
                } finally {
                    dateLexer.close();
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else if (chLocal == '-' || (chLocal >= '0' && chLocal <= '9')) {
            long millis = 0;
            boolean negative = false;
            if (chLocal == '-') {
                offset = offset4 + 1;
                chLocal = charAt(this.bp + offset4);
                negative = true;
            } else {
                offset = offset4;
            }
            if (chLocal >= '0' && chLocal <= '9') {
                millis = (long) (chLocal - '0');
                while (true) {
                    offset2 = offset + 1;
                    chLocal = charAt(this.bp + offset);
                    if (chLocal < '0' || chLocal > '9') {
                        offset = offset2;
                    } else {
                        millis = (10 * millis) + ((long) (chLocal - '0'));
                        offset = offset2;
                    }
                }
                offset = offset2;
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
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return dateVal;
        } else if (chLocal == '}') {
            int offset6 = offset + 1;
            char chLocal2 = charAt(this.bp + offset);
            if (chLocal2 == ',') {
                this.token = 16;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == ']') {
                this.token = 15;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == '}') {
                this.token = 13;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == 26) {
                this.token = 20;
                this.bp += offset6 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return dateVal;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public Date scanDate(char seperator) {
        int offset;
        Date dateVal;
        int offset2;
        this.matchStat = 0;
        int offset3 = 0 + 1;
        char chLocal = charAt(this.bp + 0);
        if (chLocal == '\"') {
            int endIndex = indexOf('\"', this.bp + 1);
            if (endIndex != -1) {
                int startIndex2 = this.bp + 1;
                String stringVal = subString(startIndex2, endIndex - startIndex2);
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
                    int chars_len = endIndex - (this.bp + 1);
                    stringVal = readString(sub_chars(this.bp + 1, chars_len), chars_len);
                }
                int offset4 = offset3 + (endIndex - (this.bp + 1)) + 1;
                offset = offset4 + 1;
                chLocal = charAt(this.bp + offset4);
                JSONScanner dateLexer = new JSONScanner(stringVal);
                try {
                    if (dateLexer.scanISO8601DateIfMatch(false)) {
                        dateVal = dateLexer.getCalendar().getTime();
                    } else {
                        this.matchStat = -1;
                        dateLexer.close();
                        return null;
                    }
                } finally {
                    dateLexer.close();
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else if (chLocal == '-' || (chLocal >= '0' && chLocal <= '9')) {
            long millis = 0;
            boolean negative = false;
            if (chLocal == '-') {
                offset = offset3 + 1;
                chLocal = charAt(this.bp + offset3);
                negative = true;
            } else {
                offset = offset3;
            }
            if (chLocal >= '0' && chLocal <= '9') {
                millis = (long) (chLocal - '0');
                while (true) {
                    offset2 = offset + 1;
                    chLocal = charAt(this.bp + offset);
                    if (chLocal < '0' || chLocal > '9') {
                        offset = offset2;
                    } else {
                        millis = (10 * millis) + ((long) (chLocal - '0'));
                        offset = offset2;
                    }
                }
                offset = offset2;
            }
            if (millis < 0) {
                this.matchStat = -1;
                return null;
            }
            if (negative) {
                millis = -millis;
            }
            dateVal = new Date(millis);
        } else if (chLocal == 'n' && charAt(this.bp + offset3) == 'u' && charAt(this.bp + offset3 + 1) == 'l' && charAt(this.bp + offset3 + 2) == 'l') {
            this.matchStat = 5;
            dateVal = null;
            int offset5 = offset3 + 3;
            offset = offset5 + 1;
            chLocal = charAt(this.bp + offset5);
        } else {
            this.matchStat = -1;
            return null;
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return dateVal;
        } else if (chLocal == ']') {
            int offset6 = offset + 1;
            char chLocal2 = charAt(this.bp + offset);
            if (chLocal2 == ',') {
                this.token = 16;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == ']') {
                this.token = 15;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == '}') {
                this.token = 13;
                this.bp += offset6;
                this.ch = charAt(this.bp);
            } else if (chLocal2 == 26) {
                this.token = 20;
                this.bp += offset6 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return dateVal;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public UUID scanFieldUUID(char[] fieldName) {
        int offset;
        char chLocal;
        UUID uuid;
        UUID uuid2;
        int i;
        int num;
        int num2;
        int num3;
        int num4;
        int num5;
        int num6;
        int num7;
        this.matchStat = 0;
        if (!charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset2 = fieldName.length;
        int offset3 = offset2 + 1;
        char chLocal2 = charAt(this.bp + offset2);
        if (chLocal2 == '\"') {
            int startIndex = this.bp + fieldName.length + 1;
            int endIndex = indexOf('\"', startIndex);
            if (endIndex != -1) {
                int startIndex2 = this.bp + fieldName.length + 1;
                int len = endIndex - startIndex2;
                char c = 'F';
                char c2 = 'f';
                char c3 = '9';
                char c4 = 'A';
                char c5 = 'a';
                char c6 = '0';
                if (len == 36) {
                    long mostSigBits = 0;
                    long leastSigBits = 0;
                    int i2 = 0;
                    while (i2 < 8) {
                        char ch2 = charAt(startIndex2 + i2);
                        if (ch2 >= '0' && ch2 <= '9') {
                            num7 = ch2 - '0';
                        } else if (ch2 >= 'a' && ch2 <= c2) {
                            num7 = 10 + (ch2 - 'a');
                        } else if (ch2 < 'A' || ch2 > c) {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num7 = 10 + (ch2 - 'A');
                        }
                        mostSigBits = (mostSigBits << 4) | ((long) num7);
                        i2++;
                        chLocal2 = chLocal2;
                        c2 = 'f';
                        c = 'F';
                    }
                    int i3 = 9;
                    while (i3 < 13) {
                        char ch3 = charAt(startIndex2 + i3);
                        if (ch3 >= '0' && ch3 <= '9') {
                            num6 = ch3 - '0';
                        } else if (ch3 >= c5 && ch3 <= 'f') {
                            num6 = 10 + (ch3 - 'a');
                        } else if (ch3 < 'A' || ch3 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num6 = 10 + (ch3 - 'A');
                        }
                        mostSigBits = (mostSigBits << 4) | ((long) num6);
                        i3++;
                        endIndex = endIndex;
                        c5 = 'a';
                    }
                    long mostSigBits2 = mostSigBits;
                    for (int i4 = 14; i4 < 18; i4++) {
                        char ch4 = charAt(startIndex2 + i4);
                        if (ch4 >= '0' && ch4 <= '9') {
                            num5 = ch4 - '0';
                        } else if (ch4 >= 'a' && ch4 <= 'f') {
                            num5 = 10 + (ch4 - 'a');
                        } else if (ch4 < 'A' || ch4 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num5 = 10 + (ch4 - 'A');
                        }
                        mostSigBits2 = (mostSigBits2 << 4) | ((long) num5);
                    }
                    int i5 = 19;
                    while (i5 < 23) {
                        char ch5 = charAt(startIndex2 + i5);
                        if (ch5 >= '0' && ch5 <= c3) {
                            num4 = ch5 - '0';
                        } else if (ch5 >= 'a' && ch5 <= 'f') {
                            num4 = 10 + (ch5 - 'a');
                        } else if (ch5 < c4 || ch5 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num4 = 10 + (ch5 - 'A');
                        }
                        leastSigBits = (leastSigBits << 4) | ((long) num4);
                        i5++;
                        c4 = 'A';
                        c3 = '9';
                    }
                    long leastSigBits2 = leastSigBits;
                    for (int i6 = 24; i6 < 36; i6++) {
                        char ch6 = charAt(startIndex2 + i6);
                        if (ch6 >= '0' && ch6 <= '9') {
                            num3 = ch6 - '0';
                        } else if (ch6 >= 'a' && ch6 <= 'f') {
                            num3 = 10 + (ch6 - 'a');
                        } else if (ch6 < 'A' || ch6 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num3 = 10 + (ch6 - 'A');
                        }
                        leastSigBits2 = (leastSigBits2 << 4) | ((long) num3);
                    }
                    uuid = new UUID(mostSigBits2, leastSigBits2);
                    int offset4 = offset3 + (endIndex - ((this.bp + fieldName.length) + 1)) + 1;
                    offset = offset4 + 1;
                    chLocal = charAt(this.bp + offset4);
                } else if (len == 32) {
                    long mostSigBits3 = 0;
                    for (int i7 = 0; i7 < 16; i7++) {
                        char ch7 = charAt(startIndex2 + i7);
                        if (ch7 >= '0' && ch7 <= '9') {
                            num2 = ch7 - '0';
                        } else if (ch7 >= 'a' && ch7 <= 'f') {
                            num2 = 10 + (ch7 - 'a');
                        } else if (ch7 < 'A' || ch7 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num2 = 10 + (ch7 - 'A');
                        }
                        mostSigBits3 = (mostSigBits3 << 4) | ((long) num2);
                    }
                    long leastSigBits3 = 0;
                    int i8 = 16;
                    while (i8 < 32) {
                        char ch8 = charAt(startIndex2 + i8);
                        if (ch8 >= c6) {
                            if (ch8 <= '9') {
                                num = ch8 - '0';
                                leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                                i8++;
                                startIndex = startIndex;
                                c6 = '0';
                            }
                        }
                        if (ch8 >= 'a') {
                            if (ch8 <= 'f') {
                                num = 10 + (ch8 - 'a');
                                leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                                i8++;
                                startIndex = startIndex;
                                c6 = '0';
                            }
                        }
                        if (ch8 < 'A' || ch8 > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch8 - 'A');
                        leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                        i8++;
                        startIndex = startIndex;
                        c6 = '0';
                    }
                    UUID uuid3 = new UUID(mostSigBits3, leastSigBits3);
                    int offset5 = offset3 + (endIndex - ((this.bp + fieldName.length) + 1)) + 1;
                    chLocal = charAt(this.bp + offset5);
                    uuid = uuid3;
                    offset = offset5 + 1;
                } else {
                    this.matchStat = -1;
                    return null;
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else {
            if (chLocal2 == 'n') {
                int offset6 = offset3 + 1;
                if (charAt(this.bp + offset3) == 'u') {
                    offset3 = offset6 + 1;
                    if (charAt(this.bp + offset6) == 'l') {
                        int offset7 = offset3 + 1;
                        if (charAt(this.bp + offset3) == 'l') {
                            uuid = null;
                            chLocal = charAt(this.bp + offset7);
                            offset = offset7 + 1;
                        }
                    }
                }
                i = -1;
                uuid2 = null;
                this.matchStat = i;
                return uuid2;
            }
            i = -1;
            uuid2 = null;
            this.matchStat = i;
            return uuid2;
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return uuid;
        } else if (chLocal == '}') {
            int offset8 = offset + 1;
            char chLocal3 = charAt(this.bp + offset);
            if (chLocal3 == ',') {
                this.token = 16;
                this.bp += offset8;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == ']') {
                this.token = 15;
                this.bp += offset8;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == '}') {
                this.token = 13;
                this.bp += offset8;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == 26) {
                this.token = 20;
                this.bp += offset8 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return uuid;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public UUID scanUUID(char seperator) {
        int offset;
        char chLocal;
        UUID uuid;
        UUID uuid2;
        int i;
        int num;
        int num2;
        int num3;
        int num4;
        int num5;
        int num6;
        int num7;
        this.matchStat = 0;
        int offset2 = 0 + 1;
        char chLocal2 = charAt(this.bp + 0);
        char c = 4;
        if (chLocal2 == '\"') {
            int endIndex = indexOf('\"', this.bp + 1);
            if (endIndex != -1) {
                int startIndex2 = this.bp + 1;
                int len = endIndex - startIndex2;
                char c2 = 'F';
                char c3 = 'f';
                char c4 = '9';
                char c5 = 'A';
                char c6 = 'a';
                char c7 = '0';
                if (len == 36) {
                    long mostSigBits = 0;
                    long leastSigBits = 0;
                    int i2 = 0;
                    while (i2 < 8) {
                        char ch2 = charAt(startIndex2 + i2);
                        if (ch2 >= '0' && ch2 <= '9') {
                            num7 = ch2 - '0';
                        } else if (ch2 >= 'a' && ch2 <= c3) {
                            num7 = 10 + (ch2 - 'a');
                        } else if (ch2 < 'A' || ch2 > c2) {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num7 = 10 + (ch2 - 'A');
                        }
                        mostSigBits = (mostSigBits << c) | ((long) num7);
                        i2++;
                        c = 4;
                        c2 = 'F';
                        c3 = 'f';
                    }
                    int i3 = 9;
                    while (i3 < 13) {
                        char ch3 = charAt(startIndex2 + i3);
                        if (ch3 >= '0' && ch3 <= '9') {
                            num6 = ch3 - '0';
                        } else if (ch3 >= c6 && ch3 <= 'f') {
                            num6 = 10 + (ch3 - 'a');
                        } else if (ch3 < c5 || ch3 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num6 = 10 + (ch3 - 'A');
                        }
                        mostSigBits = (mostSigBits << 4) | ((long) num6);
                        i3++;
                        c6 = 'a';
                        c5 = 'A';
                    }
                    long mostSigBits2 = mostSigBits;
                    for (int i4 = 14; i4 < 18; i4++) {
                        char ch4 = charAt(startIndex2 + i4);
                        if (ch4 >= '0' && ch4 <= '9') {
                            num5 = ch4 - '0';
                        } else if (ch4 >= 'a' && ch4 <= 'f') {
                            num5 = 10 + (ch4 - 'a');
                        } else if (ch4 < 'A' || ch4 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num5 = 10 + (ch4 - 'A');
                        }
                        mostSigBits2 = (mostSigBits2 << 4) | ((long) num5);
                    }
                    int i5 = 19;
                    while (i5 < 23) {
                        char ch5 = charAt(startIndex2 + i5);
                        if (ch5 >= '0' && ch5 <= c4) {
                            num4 = ch5 - '0';
                        } else if (ch5 >= 'a' && ch5 <= 'f') {
                            num4 = 10 + (ch5 - 'a');
                        } else if (ch5 < 'A' || ch5 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num4 = 10 + (ch5 - 'A');
                        }
                        leastSigBits = (leastSigBits << 4) | ((long) num4);
                        i5++;
                        chLocal2 = chLocal2;
                        c4 = '9';
                    }
                    long leastSigBits2 = leastSigBits;
                    for (int i6 = 24; i6 < 36; i6++) {
                        char ch6 = charAt(startIndex2 + i6);
                        if (ch6 >= '0' && ch6 <= '9') {
                            num3 = ch6 - '0';
                        } else if (ch6 >= 'a' && ch6 <= 'f') {
                            num3 = 10 + (ch6 - 'a');
                        } else if (ch6 < 'A' || ch6 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num3 = 10 + (ch6 - 'A');
                        }
                        leastSigBits2 = (leastSigBits2 << 4) | ((long) num3);
                    }
                    uuid = new UUID(mostSigBits2, leastSigBits2);
                    int offset3 = offset2 + (endIndex - (this.bp + 1)) + 1;
                    chLocal = charAt(this.bp + offset3);
                    offset = offset3 + 1;
                } else if (len == 32) {
                    long mostSigBits3 = 0;
                    for (int i7 = 0; i7 < 16; i7++) {
                        char ch7 = charAt(startIndex2 + i7);
                        if (ch7 >= '0' && ch7 <= '9') {
                            num2 = ch7 - '0';
                        } else if (ch7 >= 'a' && ch7 <= 'f') {
                            num2 = 10 + (ch7 - 'a');
                        } else if (ch7 < 'A' || ch7 > 'F') {
                            this.matchStat = -2;
                            return null;
                        } else {
                            num2 = 10 + (ch7 - 'A');
                        }
                        mostSigBits3 = (mostSigBits3 << 4) | ((long) num2);
                    }
                    long leastSigBits3 = 0;
                    int i8 = 16;
                    while (i8 < 32) {
                        char ch8 = charAt(startIndex2 + i8);
                        if (ch8 >= c7) {
                            if (ch8 <= '9') {
                                num = ch8 - '0';
                                leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                                i8++;
                                c7 = '0';
                            }
                        }
                        if (ch8 >= 'a') {
                            if (ch8 <= 'f') {
                                num = 10 + (ch8 - 'a');
                                leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                                i8++;
                                c7 = '0';
                            }
                        }
                        if (ch8 < 'A' || ch8 > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch8 - 'A');
                        leastSigBits3 = (leastSigBits3 << 4) | ((long) num);
                        i8++;
                        c7 = '0';
                    }
                    UUID uuid3 = new UUID(mostSigBits3, leastSigBits3);
                    int offset4 = offset2 + (endIndex - (this.bp + 1)) + 1;
                    offset = offset4 + 1;
                    chLocal = charAt(this.bp + offset4);
                    uuid = uuid3;
                } else {
                    this.matchStat = -1;
                    return null;
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else {
            if (chLocal2 == 'n') {
                int offset5 = offset2 + 1;
                if (charAt(this.bp + offset2) == 'u') {
                    offset2 = offset5 + 1;
                    if (charAt(this.bp + offset5) == 'l') {
                        int offset6 = offset2 + 1;
                        if (charAt(this.bp + offset2) == 'l') {
                            uuid = null;
                            offset = offset6 + 1;
                            chLocal = charAt(this.bp + offset6);
                        }
                    }
                }
                i = -1;
                uuid2 = null;
                this.matchStat = i;
                return uuid2;
            }
            i = -1;
            uuid2 = null;
            this.matchStat = i;
            return uuid2;
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = charAt(this.bp);
            this.matchStat = 3;
            return uuid;
        } else if (chLocal == ']') {
            int offset7 = offset + 1;
            char chLocal3 = charAt(this.bp + offset);
            if (chLocal3 == ',') {
                this.token = 16;
                this.bp += offset7;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == ']') {
                this.token = 15;
                this.bp += offset7;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == '}') {
                this.token = 13;
                this.bp += offset7;
                this.ch = charAt(this.bp);
            } else if (chLocal3 == 26) {
                this.token = 20;
                this.bp += offset7 - 1;
                this.ch = JSONLexer.EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return uuid;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public final void scanTrue() {
        if (this.ch == 't') {
            next();
            if (this.ch == 'r') {
                next();
                if (this.ch == 'u') {
                    next();
                    if (this.ch == 'e') {
                        next();
                        if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == 26 || this.ch == '\f' || this.ch == '\b' || this.ch == ':' || this.ch == '/') {
                            this.token = 6;
                            return;
                        }
                        throw new JSONException("scan true error");
                    }
                    throw new JSONException("error parse true");
                }
                throw new JSONException("error parse true");
            }
            throw new JSONException("error parse true");
        }
        throw new JSONException("error parse true");
    }

    public final void scanNullOrNew() {
        scanNullOrNew(true);
    }

    public final void scanNullOrNew(boolean acceptColon) {
        if (this.ch == 'n') {
            next();
            if (this.ch == 'u') {
                next();
                if (this.ch == 'l') {
                    next();
                    if (this.ch == 'l') {
                        next();
                        if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == 26 || ((this.ch == ':' && acceptColon) || this.ch == '\f' || this.ch == '\b')) {
                            this.token = 8;
                            return;
                        }
                        throw new JSONException("scan null error");
                    }
                    throw new JSONException("error parse null");
                }
                throw new JSONException("error parse null");
            } else if (this.ch == 'e') {
                next();
                if (this.ch == 'w') {
                    next();
                    if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == 26 || this.ch == '\f' || this.ch == '\b') {
                        this.token = 9;
                        return;
                    }
                    throw new JSONException("scan new error");
                }
                throw new JSONException("error parse new");
            } else {
                throw new JSONException("error parse new");
            }
        } else {
            throw new JSONException("error parse null or new");
        }
    }

    public final void scanFalse() {
        if (this.ch == 'f') {
            next();
            if (this.ch == 'a') {
                next();
                if (this.ch == 'l') {
                    next();
                    if (this.ch == 's') {
                        next();
                        if (this.ch == 'e') {
                            next();
                            if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == 26 || this.ch == '\f' || this.ch == '\b' || this.ch == ':' || this.ch == '/') {
                                this.token = 7;
                                return;
                            }
                            throw new JSONException("scan false error");
                        }
                        throw new JSONException("error parse false");
                    }
                    throw new JSONException("error parse false");
                }
                throw new JSONException("error parse false");
            }
            throw new JSONException("error parse false");
        }
        throw new JSONException("error parse false");
    }

    public final void scanIdent() {
        this.np = this.bp - 1;
        this.hasSpecial = false;
        do {
            this.sp++;
            next();
        } while (Character.isLetterOrDigit(this.ch));
        String ident = stringVal();
        if ("null".equalsIgnoreCase(ident)) {
            this.token = 8;
        } else if ("new".equals(ident)) {
            this.token = 9;
        } else if ("true".equals(ident)) {
            this.token = 6;
        } else if ("false".equals(ident)) {
            this.token = 7;
        } else if ("undefined".equals(ident)) {
            this.token = 23;
        } else if ("Set".equals(ident)) {
            this.token = 21;
        } else if ("TreeSet".equals(ident)) {
            this.token = 22;
        } else {
            this.token = 18;
        }
    }

    public static String readString(char[] chars, int chars_len) {
        int len;
        char[] sbuf2 = new char[chars_len];
        int len2 = 0;
        int i = 0;
        while (i < chars_len) {
            char ch2 = chars[i];
            if (ch2 != '\\') {
                len = len2 + 1;
                sbuf2[len2] = ch2;
            } else {
                i++;
                char ch3 = chars[i];
                switch (ch3) {
                    case '/':
                        len = len2 + 1;
                        sbuf2[len2] = '/';
                        break;
                    case '0':
                        len = len2 + 1;
                        sbuf2[len2] = 0;
                        break;
                    case Opcodes.V1_5 /* 49 */:
                        len = len2 + 1;
                        sbuf2[len2] = 1;
                        break;
                    case '2':
                        len = len2 + 1;
                        sbuf2[len2] = 2;
                        break;
                    case '3':
                        len = len2 + 1;
                        sbuf2[len2] = 3;
                        break;
                    case '4':
                        len = len2 + 1;
                        sbuf2[len2] = 4;
                        break;
                    case '5':
                        len = len2 + 1;
                        sbuf2[len2] = 5;
                        break;
                    case Opcodes.ISTORE /* 54 */:
                        len = len2 + 1;
                        sbuf2[len2] = 6;
                        break;
                    case Opcodes.LSTORE /* 55 */:
                        len = len2 + 1;
                        sbuf2[len2] = 7;
                        break;
                    default:
                        switch (ch3) {
                            case 't':
                                len = len2 + 1;
                                sbuf2[len2] = '\t';
                                break;
                            case 'u':
                                len = len2 + 1;
                                int i2 = i + 1;
                                int i3 = i2 + 1;
                                int i4 = i3 + 1;
                                i = i4 + 1;
                                sbuf2[len2] = (char) Integer.parseInt(new String(new char[]{chars[i2], chars[i3], chars[i4], chars[i]}), 16);
                                break;
                            case 'v':
                                len = len2 + 1;
                                sbuf2[len2] = 11;
                                break;
                            default:
                                switch (ch3) {
                                    case '\"':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\"';
                                        break;
                                    case '\'':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\'';
                                        break;
                                    case 'F':
                                    case 'f':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\f';
                                        break;
                                    case '\\':
                                        sbuf2[len2] = '\\';
                                        len2++;
                                        continue;
                                        continue;
                                        continue;
                                        i++;
                                    case 'b':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\b';
                                        break;
                                    case 'n':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\n';
                                        break;
                                    case 'r':
                                        len = len2 + 1;
                                        sbuf2[len2] = '\r';
                                        break;
                                    case 'x':
                                        len = len2 + 1;
                                        int i5 = i + 1;
                                        i = i5 + 1;
                                        sbuf2[len2] = (char) ((digits[chars[i5]] * 16) + digits[chars[i]]);
                                        break;
                                    default:
                                        throw new JSONException("unclosed.str.lit");
                                }
                        }
                }
            }
            len2 = len;
            i++;
        }
        return new String(sbuf2, 0, len2);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public boolean isBlankInput() {
        int i = 0;
        while (true) {
            char chLocal = charAt(i);
            if (chLocal == 26) {
                this.token = 20;
                return true;
            } else if (!isWhitespace(chLocal)) {
                return false;
            } else {
                i++;
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void skipWhitespace() {
        while (this.ch <= '/') {
            if (this.ch == ' ' || this.ch == '\r' || this.ch == '\n' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b') {
                next();
            } else if (this.ch == '/') {
                skipComment();
            } else {
                return;
            }
        }
    }

    private void scanStringSingleQuote() {
        this.np = this.bp;
        this.hasSpecial = false;
        while (true) {
            char chLocal = next();
            if (chLocal == '\'') {
                this.token = 4;
                next();
                return;
            } else if (chLocal == 26) {
                if (!isEOF()) {
                    putChar(JSONLexer.EOI);
                } else {
                    throw new JSONException("unclosed single-quote string");
                }
            } else if (chLocal == '\\') {
                if (!this.hasSpecial) {
                    this.hasSpecial = true;
                    if (this.sp > this.sbuf.length) {
                        char[] newsbuf = new char[(this.sp * 2)];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    copyTo(this.np + 1, this.sp, this.sbuf);
                }
                char chLocal2 = next();
                switch (chLocal2) {
                    case '/':
                        putChar('/');
                        continue;
                    case '0':
                        putChar(0);
                        continue;
                    case Opcodes.V1_5 /* 49 */:
                        putChar(1);
                        continue;
                    case '2':
                        putChar(2);
                        continue;
                    case '3':
                        putChar(3);
                        continue;
                    case '4':
                        putChar(4);
                        continue;
                    case '5':
                        putChar(5);
                        continue;
                    case Opcodes.ISTORE /* 54 */:
                        putChar(6);
                        continue;
                    case Opcodes.LSTORE /* 55 */:
                        putChar(7);
                        continue;
                    default:
                        switch (chLocal2) {
                            case 't':
                                putChar('\t');
                                continue;
                            case 'u':
                                putChar((char) Integer.parseInt(new String(new char[]{next(), next(), next(), next()}), 16));
                                continue;
                            case 'v':
                                putChar(11);
                                continue;
                            default:
                                switch (chLocal2) {
                                    case '\"':
                                        putChar('\"');
                                        continue;
                                    case '\'':
                                        putChar('\'');
                                        continue;
                                    case 'F':
                                    case 'f':
                                        putChar('\f');
                                        continue;
                                    case '\\':
                                        putChar('\\');
                                        continue;
                                    case 'b':
                                        putChar('\b');
                                        continue;
                                    case 'n':
                                        putChar('\n');
                                        continue;
                                    case 'r':
                                        putChar('\r');
                                        continue;
                                    case 'x':
                                        putChar((char) ((digits[next()] * 16) + digits[next()]));
                                        continue;
                                        continue;
                                        continue;
                                    default:
                                        this.ch = chLocal2;
                                        throw new JSONException("unclosed single-quote string");
                                }
                        }
                }
            } else if (!this.hasSpecial) {
                this.sp++;
            } else if (this.sp == this.sbuf.length) {
                putChar(chLocal);
            } else {
                char[] cArr = this.sbuf;
                int i = this.sp;
                this.sp = i + 1;
                cArr[i] = chLocal;
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void putChar(char ch2) {
        if (this.sp == this.sbuf.length) {
            char[] newsbuf = new char[(this.sbuf.length * 2)];
            System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
            this.sbuf = newsbuf;
        }
        char[] newsbuf2 = this.sbuf;
        int i = this.sp;
        this.sp = i + 1;
        newsbuf2[i] = ch2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050  */
    public final void scanHex() {
        char ch2;
        if (this.ch == 'x') {
            next();
            if (this.ch == '\'') {
                this.np = this.bp;
                next();
                if (this.ch == '\'') {
                    next();
                    this.token = 26;
                    return;
                }
                int i = 0;
                while (true) {
                    ch2 = next();
                    if ((ch2 >= '0' && ch2 <= '9') || (ch2 >= 'A' && ch2 <= 'F')) {
                        this.sp++;
                        i++;
                    } else if (ch2 != '\'') {
                        this.sp++;
                        next();
                        this.token = 26;
                        return;
                    } else {
                        throw new JSONException("illegal state. " + ch2);
                    }
                }
                if (ch2 != '\'') {
                }
            } else {
                throw new JSONException("illegal state. " + this.ch);
            }
        } else {
            throw new JSONException("illegal state. " + this.ch);
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final void scanNumber() {
        this.np = this.bp;
        if (this.ch == '-') {
            this.sp++;
            next();
        }
        while (this.ch >= '0' && this.ch <= '9') {
            this.sp++;
            next();
        }
        boolean isDouble = false;
        if (this.ch == '.') {
            this.sp++;
            next();
            isDouble = true;
            while (this.ch >= '0' && this.ch <= '9') {
                this.sp++;
                next();
            }
        }
        if (this.ch == 'L') {
            this.sp++;
            next();
        } else if (this.ch == 'S') {
            this.sp++;
            next();
        } else if (this.ch == 'B') {
            this.sp++;
            next();
        } else if (this.ch == 'F') {
            this.sp++;
            next();
            isDouble = true;
        } else if (this.ch == 'D') {
            this.sp++;
            next();
            isDouble = true;
        } else if (this.ch == 'e' || this.ch == 'E') {
            this.sp++;
            next();
            if (this.ch == '+' || this.ch == '-') {
                this.sp++;
                next();
            }
            while (this.ch >= '0' && this.ch <= '9') {
                this.sp++;
                next();
            }
            if (this.ch == 'D' || this.ch == 'F') {
                this.sp++;
                next();
            }
            isDouble = true;
        }
        if (isDouble) {
            this.token = 3;
        } else {
            this.token = 2;
        }
    }

    /* JADX INFO: Multiple debug info for r3v5 char: [D('i' int), D('chLocal' char)] */
    /* JADX INFO: Multiple debug info for r3v9 int: [D('i' int), D('digit' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003b  */
    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final long longValue() throws NumberFormatException {
        long limit;
        int i;
        long result = 0;
        boolean negative = false;
        if (this.np == -1) {
            this.np = 0;
        }
        int i2 = this.np;
        int max = this.np + this.sp;
        if (charAt(this.np) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i2++;
        } else {
            limit = -9223372036854775807L;
        }
        if (i2 < max) {
            i = i2 + 1;
            result = (long) (-(charAt(i2) - 48));
            i2 = i;
        }
        if (i2 < max) {
            i = i2 + 1;
            char chLocal = charAt(i2);
            if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B') {
                i2 = i;
            } else {
                int digit = chLocal - '0';
                if (result >= MULTMIN_RADIX_TEN) {
                    long result2 = result * 10;
                    if (result2 >= ((long) digit) + limit) {
                        result = result2 - ((long) digit);
                        i2 = i;
                        if (i2 < max) {
                        }
                    }
                    throw new NumberFormatException(numberString());
                }
                throw new NumberFormatException(numberString());
            }
        }
        if (!negative) {
            return -result;
        }
        if (i2 > this.np + 1) {
            return result;
        }
        throw new NumberFormatException(numberString());
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public final Number decimalValue(boolean decimal) {
        char chLocal = charAt((this.np + this.sp) - 1);
        if (chLocal == 'F') {
            try {
                return Float.valueOf(Float.parseFloat(numberString()));
            } catch (NumberFormatException ex) {
                throw new JSONException(ex.getMessage() + ", " + info());
            }
        } else if (chLocal == 'D') {
            return Double.valueOf(Double.parseDouble(numberString()));
        } else {
            if (decimal) {
                return decimalValue();
            }
            return Double.valueOf(doubleValue());
        }
    }

    public static boolean isWhitespace(char ch2) {
        return ch2 <= ' ' && (ch2 == ' ' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t' || ch2 == '\f' || ch2 == '\b');
    }

    public String[] scanFieldStringArray(char[] fieldName, int argTypesCount, SymbolTable typeSymbolTable) {
        throw new UnsupportedOperationException();
    }

    public boolean matchField2(char[] fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer
    public int getFeatures() {
        return this.features;
    }
}
