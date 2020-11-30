package org.apache.commons.codec.binary;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64OutputStream extends FilterOutputStream {
    private final Base64 base64;
    private final boolean doEncode;
    private final byte[] singleByte;

    public Base64OutputStream(OutputStream out) {
        this(out, true);
    }

    public Base64OutputStream(OutputStream out, boolean doEncode2) {
        super(out);
        this.singleByte = new byte[1];
        this.doEncode = doEncode2;
        this.base64 = new Base64();
    }

    public Base64OutputStream(OutputStream out, boolean doEncode2, int lineLength, byte[] lineSeparator) {
        super(out);
        this.singleByte = new byte[1];
        this.doEncode = doEncode2;
        this.base64 = new Base64(lineLength, lineSeparator);
    }

    @Override // java.io.OutputStream, java.io.FilterOutputStream
    public void write(int i) throws IOException {
        this.singleByte[0] = (byte) i;
        write(this.singleByte, 0, 1);
    }

    @Override // java.io.OutputStream, java.io.FilterOutputStream
    public void write(byte[] b, int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (offset > b.length || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len > 0) {
            if (this.doEncode) {
                this.base64.encode(b, offset, len);
            } else {
                this.base64.decode(b, offset, len);
            }
            flush(false);
        }
    }

    private void flush(boolean propogate) throws IOException {
        byte[] buf;
        int c;
        int avail = this.base64.avail();
        if (avail > 0 && (c = this.base64.readResults((buf = new byte[avail]), 0, avail)) > 0) {
            this.out.write(buf, 0, c);
        }
        if (propogate) {
            this.out.flush();
        }
    }

    @Override // java.io.OutputStream, java.io.FilterOutputStream, java.io.Flushable
    public void flush() throws IOException {
        flush(true);
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.io.FilterOutputStream, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.doEncode) {
            this.base64.encode(this.singleByte, 0, -1);
        } else {
            this.base64.decode(this.singleByte, 0, -1);
        }
        flush();
        this.out.close();
    }
}
