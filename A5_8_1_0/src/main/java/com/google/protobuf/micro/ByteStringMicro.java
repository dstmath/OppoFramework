package com.google.protobuf.micro;

import java.io.UnsupportedEncodingException;

public final class ByteStringMicro {
    public static final ByteStringMicro EMPTY = new ByteStringMicro(new byte[0]);
    private final byte[] bytes;
    private volatile int hash = 0;

    private ByteStringMicro(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte byteAt(int index) {
        return this.bytes[index];
    }

    public int size() {
        return this.bytes.length;
    }

    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

    public static ByteStringMicro copyFrom(byte[] bytes, int offset, int size) {
        byte[] copy = new byte[size];
        System.arraycopy(bytes, offset, copy, 0, size);
        return new ByteStringMicro(copy);
    }

    public static ByteStringMicro copyFrom(byte[] bytes) {
        return copyFrom(bytes, 0, bytes.length);
    }

    public static ByteStringMicro copyFrom(String text, String charsetName) throws UnsupportedEncodingException {
        return new ByteStringMicro(text.getBytes(charsetName));
    }

    public static ByteStringMicro copyFromUtf8(String text) {
        try {
            return new ByteStringMicro(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?");
        }
    }

    public void copyTo(byte[] target, int offset) {
        System.arraycopy(this.bytes, 0, target, offset, this.bytes.length);
    }

    public void copyTo(byte[] target, int sourceOffset, int targetOffset, int size) {
        System.arraycopy(this.bytes, sourceOffset, target, targetOffset, size);
    }

    public byte[] toByteArray() {
        int size = this.bytes.length;
        byte[] copy = new byte[size];
        System.arraycopy(this.bytes, 0, copy, 0, size);
        return copy;
    }

    public String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(this.bytes, charsetName);
    }

    public String toStringUtf8() {
        try {
            return new String(this.bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?");
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ByteStringMicro)) {
            return false;
        }
        ByteStringMicro other = (ByteStringMicro) o;
        int size = this.bytes.length;
        if (size != other.bytes.length) {
            return false;
        }
        byte[] thisBytes = this.bytes;
        byte[] otherBytes = other.bytes;
        for (int i = 0; i < size; i++) {
            if (thisBytes[i] != otherBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            byte[] thisBytes = this.bytes;
            int size = this.bytes.length;
            h = size;
            for (int i = 0; i < size; i++) {
                h = (h * 31) + thisBytes[i];
            }
            if (h == 0) {
                h = 1;
            }
            this.hash = h;
        }
        return h;
    }
}
