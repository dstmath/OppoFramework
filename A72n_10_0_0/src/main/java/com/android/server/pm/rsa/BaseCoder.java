package com.android.server.pm.rsa;

import java.util.Arrays;

public abstract class BaseCoder {
    private final int chunkSeparatorLength;
    private final int encodedBlockSize;
    protected final int lineLength;
    @Deprecated
    protected final byte pad;
    private final int unencodedBlockSize;

    /* access modifiers changed from: package-private */
    public abstract void decode(byte[] bArr, int i, int i2, BaseContent baseContent);

    /* access modifiers changed from: package-private */
    public abstract void encode(byte[] bArr, int i, int i2, BaseContent baseContent);

    /* access modifiers changed from: protected */
    public abstract boolean isInAlphabet(byte b);

    protected BaseCoder(int unencodedBlockSize2, int encodedBlockSize2, int lineLength2, int chunkSeparatorLength2) {
        this(unencodedBlockSize2, encodedBlockSize2, lineLength2, chunkSeparatorLength2, (byte) 61);
    }

    protected BaseCoder(int unencodedBlockSize2, int encodedBlockSize2, int lineLength2, int chunkSeparatorLength2, byte pad2) {
        this.unencodedBlockSize = unencodedBlockSize2;
        this.encodedBlockSize = encodedBlockSize2;
        this.lineLength = lineLength2 > 0 && chunkSeparatorLength2 > 0 ? (lineLength2 / encodedBlockSize2) * encodedBlockSize2 : 0;
        this.chunkSeparatorLength = chunkSeparatorLength2;
        this.pad = pad2;
    }

    /* access modifiers changed from: package-private */
    public int available(BaseContent content) {
        if (content.mBuffer != null) {
            return content.mPos - content.mReadPos;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getDefaultBufferSize() {
        return 8192;
    }

    private byte[] resizeBuffer(BaseContent content) {
        if (content.mBuffer == null) {
            content.mBuffer = new byte[getDefaultBufferSize()];
            content.mPos = 0;
            content.mReadPos = 0;
        } else {
            byte[] b = new byte[(content.mBuffer.length * 2)];
            System.arraycopy(content.mBuffer, 0, b, 0, content.mBuffer.length);
            content.mBuffer = b;
        }
        return content.mBuffer;
    }

    /* access modifiers changed from: protected */
    public byte[] ensureBufferSize(int size, BaseContent content) {
        if (content.mBuffer == null || content.mBuffer.length < content.mPos + size) {
            return resizeBuffer(content);
        }
        return content.mBuffer;
    }

    /* access modifiers changed from: package-private */
    public int readResults(byte[] b, int bPos, int bAvail, BaseContent content) {
        if (content.mBuffer == null) {
            return content.mEof ? -1 : 0;
        }
        int len = Math.min(available(content), bAvail);
        System.arraycopy(content.mBuffer, content.mReadPos, b, bPos, len);
        content.mReadPos += len;
        if (content.mReadPos >= content.mPos) {
            content.mBuffer = null;
        }
        return len;
    }

    public static boolean isWhiteSpace(byte byteToCheck) {
        if (byteToCheck == 9 || byteToCheck == 10 || byteToCheck == 13 || byteToCheck == 32) {
            return true;
        }
        return false;
    }

    public String encodeToString(byte[] pArray) {
        return new String(encode(pArray));
    }

    public String encodeAsString(byte[] pArray) {
        return new String(encode(pArray));
    }

    public byte[] decodeString(String pArray) {
        return decode(pArray.getBytes());
    }

    public byte[] decode(byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        BaseContent context = new BaseContent();
        decode(pArray, 0, pArray.length, context);
        decode(pArray, 0, -1, context);
        byte[] result = new byte[context.mPos];
        readResults(result, 0, result.length, context);
        return result;
    }

    public byte[] encode(byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        BaseContent context = new BaseContent();
        encode(pArray, 0, pArray.length, context);
        encode(pArray, 0, -1, context);
        byte[] buf = new byte[(context.mPos - context.mReadPos)];
        readResults(buf, 0, buf.length, context);
        return buf;
    }

    public boolean containsAlphabetOrPad(byte[] arrayOctet) {
        if (arrayOctet == null) {
            return false;
        }
        for (byte element : arrayOctet) {
            if (this.pad == element || isInAlphabet(element)) {
                return true;
            }
        }
        return false;
    }

    public long getEncodedLength(byte[] pArray) {
        int length = pArray.length;
        int i = this.unencodedBlockSize;
        long len = ((long) (((length + i) - 1) / i)) * ((long) this.encodedBlockSize);
        int i2 = this.lineLength;
        if (i2 > 0) {
            return len + ((((((long) i2) + len) - 1) / ((long) i2)) * ((long) this.chunkSeparatorLength));
        }
        return len;
    }

    /* access modifiers changed from: package-private */
    public static class BaseContent {
        byte[] mBuffer;
        int mCurrentLinePos;
        boolean mEof;
        int mIbitWorkArea;
        long mLbitWorkArea;
        int mModulus;
        int mPos;
        int mReadPos;

        BaseContent() {
        }

        public String toString() {
            return "[buffer=" + Arrays.toString(this.mBuffer) + " currentLinePos=" + this.mCurrentLinePos + " eof=" + this.mEof + " ibitWorkArea=" + this.mIbitWorkArea + " lbitWorkArea=" + this.mLbitWorkArea + " modulus=" + this.mModulus + " pos=" + this.mPos + " readPos=" + this.mReadPos + "]";
        }
    }
}
