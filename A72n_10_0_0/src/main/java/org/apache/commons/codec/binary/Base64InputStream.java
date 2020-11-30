package org.apache.commons.codec.binary;

import com.alibaba.fastjson.util.IdentityHashMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream extends FilterInputStream {
    private final Base64 base64;
    private final boolean doEncode;
    private final byte[] singleByte;

    public Base64InputStream(InputStream in) {
        this(in, false);
    }

    public Base64InputStream(InputStream in, boolean doEncode2) {
        super(in);
        this.singleByte = new byte[1];
        this.doEncode = doEncode2;
        this.base64 = new Base64();
    }

    public Base64InputStream(InputStream in, boolean doEncode2, int lineLength, byte[] lineSeparator) {
        super(in);
        this.singleByte = new byte[1];
        this.doEncode = doEncode2;
        this.base64 = new Base64(lineLength, lineSeparator);
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read() throws IOException {
        int r = read(this.singleByte, 0, 1);
        while (r == 0) {
            r = read(this.singleByte, 0, 1);
        }
        if (r > 0) {
            return this.singleByte[0] < 0 ? 256 + this.singleByte[0] : this.singleByte[0];
        }
        return -1;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] b, int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (offset > b.length || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            if (!this.base64.hasData()) {
                byte[] buf = new byte[(this.doEncode ? 4096 : IdentityHashMap.DEFAULT_SIZE)];
                int c = this.in.read(buf);
                if (c > 0 && b.length == len) {
                    this.base64.setInitialBuffer(b, offset, len);
                }
                if (this.doEncode) {
                    this.base64.encode(buf, 0, c);
                } else {
                    this.base64.decode(buf, 0, c);
                }
            }
            return this.base64.readResults(b, offset, len);
        }
    }

    public boolean markSupported() {
        return false;
    }
}
