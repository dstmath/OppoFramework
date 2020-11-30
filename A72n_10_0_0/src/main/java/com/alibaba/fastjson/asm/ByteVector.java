package com.alibaba.fastjson.asm;

public class ByteVector {
    public byte[] data;
    public int length;

    public ByteVector() {
        this.data = new byte[64];
    }

    public ByteVector(int initialSize) {
        this.data = new byte[initialSize];
    }

    public ByteVector putByte(int b) {
        int length2 = this.length;
        if (length2 + 1 > this.data.length) {
            enlarge(1);
        }
        this.data[length2] = (byte) b;
        this.length = length2 + 1;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ByteVector put11(int b1, int b2) {
        int length2 = this.length;
        if (length2 + 2 > this.data.length) {
            enlarge(2);
        }
        byte[] data2 = this.data;
        int length3 = length2 + 1;
        data2[length2] = (byte) b1;
        data2[length3] = (byte) b2;
        this.length = length3 + 1;
        return this;
    }

    public ByteVector putShort(int s) {
        int length2 = this.length;
        if (length2 + 2 > this.data.length) {
            enlarge(2);
        }
        byte[] data2 = this.data;
        int length3 = length2 + 1;
        data2[length2] = (byte) (s >>> 8);
        data2[length3] = (byte) s;
        this.length = length3 + 1;
        return this;
    }

    public ByteVector put12(int b, int s) {
        int length2 = this.length;
        if (length2 + 3 > this.data.length) {
            enlarge(3);
        }
        byte[] data2 = this.data;
        int length3 = length2 + 1;
        data2[length2] = (byte) b;
        int length4 = length3 + 1;
        data2[length3] = (byte) (s >>> 8);
        data2[length4] = (byte) s;
        this.length = length4 + 1;
        return this;
    }

    public ByteVector putInt(int i) {
        int length2 = this.length;
        if (length2 + 4 > this.data.length) {
            enlarge(4);
        }
        byte[] data2 = this.data;
        int length3 = length2 + 1;
        data2[length2] = (byte) (i >>> 24);
        int length4 = length3 + 1;
        data2[length3] = (byte) (i >>> 16);
        int length5 = length4 + 1;
        data2[length4] = (byte) (i >>> 8);
        data2[length5] = (byte) i;
        this.length = length5 + 1;
        return this;
    }

    public ByteVector putUTF8(String s) {
        int charLength = s.length();
        int len = this.length;
        if (len + 2 + charLength > this.data.length) {
            enlarge(2 + charLength);
        }
        byte[] data2 = this.data;
        int len2 = len + 1;
        data2[len] = (byte) (charLength >>> 8);
        int len3 = len2 + 1;
        data2[len2] = (byte) charLength;
        int i = 0;
        while (i < charLength) {
            char c = s.charAt(i);
            if (c < 1 || c > 127) {
                throw new UnsupportedOperationException();
            }
            data2[len3] = (byte) c;
            i++;
            len3++;
        }
        this.length = len3;
        return this;
    }

    public ByteVector putByteArray(byte[] b, int off, int len) {
        if (this.length + len > this.data.length) {
            enlarge(len);
        }
        if (b != null) {
            System.arraycopy(b, off, this.data, this.length, len);
        }
        this.length += len;
        return this;
    }

    private void enlarge(int size) {
        int length1 = 2 * this.data.length;
        int length2 = this.length + size;
        byte[] newData = new byte[(length1 > length2 ? length1 : length2)];
        System.arraycopy(this.data, 0, newData, 0, this.length);
        this.data = newData;
    }
}
