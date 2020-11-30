package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.IOUtils;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;

public final class JSONReaderScanner extends JSONLexerBase {
    private static final ThreadLocal<char[]> BUF_LOCAL = new ThreadLocal<>();
    private char[] buf;
    private int bufLength;
    private Reader reader;

    public JSONReaderScanner(String input) {
        this(input, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONReaderScanner(String input, int features) {
        this(new StringReader(input), features);
    }

    public JSONReaderScanner(char[] input, int inputLength) {
        this(input, inputLength, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONReaderScanner(Reader reader2) {
        this(reader2, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONReaderScanner(Reader reader2, int features) {
        super(features);
        this.reader = reader2;
        this.buf = BUF_LOCAL.get();
        if (this.buf != null) {
            BUF_LOCAL.set(null);
        }
        if (this.buf == null) {
            this.buf = new char[16384];
        }
        try {
            this.bufLength = reader2.read(this.buf);
            this.bp = -1;
            next();
            if (this.ch == 65279) {
                next();
            }
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public JSONReaderScanner(char[] input, int inputLength, int features) {
        this(new CharArrayReader(input, 0, inputLength), features);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final char charAt(int index) {
        if (index >= this.bufLength) {
            if (this.bufLength == -1) {
                if (index < this.sp) {
                    return this.buf[index];
                }
                return JSONLexer.EOI;
            } else if (this.bp == 0) {
                char[] buf2 = new char[((this.buf.length * 3) / 2)];
                System.arraycopy(this.buf, this.bp, buf2, 0, this.bufLength);
                try {
                    this.bufLength += this.reader.read(buf2, this.bufLength, buf2.length - this.bufLength);
                    this.buf = buf2;
                } catch (IOException e) {
                    throw new JSONException(e.getMessage(), e);
                }
            } else {
                int rest = this.bufLength - this.bp;
                if (rest > 0) {
                    System.arraycopy(this.buf, this.bp, this.buf, 0, rest);
                }
                try {
                    this.bufLength = this.reader.read(this.buf, rest, this.buf.length - rest);
                    if (this.bufLength == 0) {
                        throw new JSONException("illegal state, textLength is zero");
                    } else if (this.bufLength == -1) {
                        return JSONLexer.EOI;
                    } else {
                        this.bufLength += rest;
                        index -= this.bp;
                        this.np -= this.bp;
                        this.bp = 0;
                    }
                } catch (IOException e2) {
                    throw new JSONException(e2.getMessage(), e2);
                }
            }
        }
        return this.buf[index];
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final int indexOf(char ch, int startIndex) {
        int offset = startIndex - this.bp;
        while (true) {
            char chLoal = charAt(this.bp + offset);
            if (ch == chLoal) {
                return this.bp + offset;
            }
            if (chLoal == 26) {
                return -1;
            }
            offset++;
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final String addSymbol(int offset, int len, int hash, SymbolTable symbolTable) {
        return symbolTable.addSymbol(this.buf, offset, len, hash);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final char next() {
        int index = this.bp + 1;
        this.bp = index;
        if (index >= this.bufLength) {
            if (this.bufLength == -1) {
                return JSONLexer.EOI;
            }
            if (this.sp > 0) {
                int offset = this.bufLength - this.sp;
                if (this.ch == '\"' && offset > 0) {
                    offset--;
                }
                System.arraycopy(this.buf, offset, this.buf, 0, this.sp);
            }
            this.np = -1;
            int i = this.sp;
            this.bp = i;
            index = i;
            try {
                int startPos = this.bp;
                int readLength = this.buf.length - startPos;
                if (readLength == 0) {
                    char[] newBuf = new char[(this.buf.length * 2)];
                    System.arraycopy(this.buf, 0, newBuf, 0, this.buf.length);
                    this.buf = newBuf;
                    readLength = this.buf.length - startPos;
                }
                this.bufLength = this.reader.read(this.buf, this.bp, readLength);
                if (this.bufLength == 0) {
                    throw new JSONException("illegal stat, textLength is zero");
                } else if (this.bufLength == -1) {
                    this.ch = JSONLexer.EOI;
                    return JSONLexer.EOI;
                } else {
                    this.bufLength += this.bp;
                }
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
        }
        char c = this.buf[index];
        this.ch = c;
        return c;
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void copyTo(int offset, int count, char[] dest) {
        System.arraycopy(this.buf, offset, dest, 0, count);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final boolean charArrayCompare(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (charAt(this.bp + i) != chars[i]) {
                return false;
            }
        }
        return true;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public byte[] bytesValue() {
        if (this.token != 26) {
            return IOUtils.decodeBase64(this.buf, this.np + 1, this.sp);
        }
        throw new JSONException("TODO");
    }

    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final void arrayCopy(int srcPos, char[] dest, int destPos, int length) {
        System.arraycopy(this.buf, srcPos, dest, destPos, length);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final String stringVal() {
        if (this.hasSpecial) {
            return new String(this.sbuf, 0, this.sp);
        }
        int offset = this.np + 1;
        if (offset < 0) {
            throw new IllegalStateException();
        } else if (offset <= this.buf.length - this.sp) {
            return new String(this.buf, offset, this.sp);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final String subString(int offset, int count) {
        if (count >= 0) {
            return new String(this.buf, offset, count);
        }
        throw new StringIndexOutOfBoundsException(count);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public final char[] sub_chars(int offset, int count) {
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        } else if (offset == 0) {
            return this.buf;
        } else {
            char[] chars = new char[count];
            System.arraycopy(this.buf, offset, chars, 0, count);
            return chars;
        }
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final String numberString() {
        int offset = this.np;
        if (offset == -1) {
            offset = 0;
        }
        char chLocal = charAt((this.sp + offset) - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }
        return new String(this.buf, offset, sp);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final BigDecimal decimalValue() {
        int offset = this.np;
        if (offset == -1) {
            offset = 0;
        }
        char chLocal = charAt((this.sp + offset) - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }
        return new BigDecimal(this.buf, offset, sp);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        super.close();
        if (this.buf.length <= 65536) {
            BUF_LOCAL.set(this.buf);
        }
        this.buf = null;
        IOUtils.close(this.reader);
    }

    @Override // com.alibaba.fastjson.parser.JSONLexerBase
    public boolean isEOF() {
        if (this.bufLength == -1 || this.bp == this.buf.length) {
            return true;
        }
        return this.ch == 26 && this.bp + 1 == this.buf.length;
    }

    @Override // com.alibaba.fastjson.parser.JSONLexer, com.alibaba.fastjson.parser.JSONLexerBase
    public final boolean isBlankInput() {
        int i = 0;
        while (true) {
            char chLocal = this.buf[i];
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
}
