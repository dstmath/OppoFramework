package com.google.protobuf.micro;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public final class CodedInputStreamMicro {
    private static final int BUFFER_SIZE = 4096;
    private static final int DEFAULT_RECURSION_LIMIT = 64;
    private static final int DEFAULT_SIZE_LIMIT = 67108864;
    private final byte[] buffer;
    private int bufferPos;
    private int bufferSize;
    private int bufferSizeAfterLimit;
    private int currentLimit;
    private final InputStream input;
    private int lastTag;
    private int recursionDepth;
    private int recursionLimit;
    private int sizeLimit;
    private int totalBytesRetired;

    public static CodedInputStreamMicro newInstance(InputStream input) {
        return new CodedInputStreamMicro(input);
    }

    public static CodedInputStreamMicro newInstance(byte[] buf) {
        return newInstance(buf, 0, buf.length);
    }

    public static CodedInputStreamMicro newInstance(byte[] buf, int off, int len) {
        return new CodedInputStreamMicro(buf, off, len);
    }

    public int readTag() throws IOException {
        if (isAtEnd()) {
            this.lastTag = 0;
            return 0;
        }
        this.lastTag = readRawVarint32();
        if (this.lastTag != 0) {
            return this.lastTag;
        }
        throw InvalidProtocolBufferMicroException.invalidTag();
    }

    public void checkLastTagWas(int value) throws InvalidProtocolBufferMicroException {
        if (this.lastTag != value) {
            throw InvalidProtocolBufferMicroException.invalidEndTag();
        }
    }

    public boolean skipField(int tag) throws IOException {
        switch (WireFormatMicro.getTagWireType(tag)) {
            case 0:
                readInt32();
                return true;
            case 1:
                readRawLittleEndian64();
                return true;
            case 2:
                skipRawBytes(readRawVarint32());
                return true;
            case 3:
                skipMessage();
                checkLastTagWas(WireFormatMicro.makeTag(WireFormatMicro.getTagFieldNumber(tag), 4));
                return true;
            case 4:
                return false;
            case 5:
                readRawLittleEndian32();
                return true;
            default:
                throw InvalidProtocolBufferMicroException.invalidWireType();
        }
    }

    public void skipMessage() throws IOException {
        int tag;
        do {
            tag = readTag();
            if (tag == 0) {
                return;
            }
        } while ((skipField(tag) ^ 1) == 0);
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public long readUInt64() throws IOException {
        return readRawVarint64();
    }

    public long readInt64() throws IOException {
        return readRawVarint64();
    }

    public int readInt32() throws IOException {
        return readRawVarint32();
    }

    public long readFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public boolean readBool() throws IOException {
        return readRawVarint32() != 0;
    }

    public String readString() throws IOException {
        int size = readRawVarint32();
        if (size > this.bufferSize - this.bufferPos || size <= 0) {
            return new String(readRawBytes(size), "UTF-8");
        }
        String result = new String(this.buffer, this.bufferPos, size, "UTF-8");
        this.bufferPos += size;
        return result;
    }

    public void readGroup(MessageMicro msg, int fieldNumber) throws IOException {
        if (this.recursionDepth >= this.recursionLimit) {
            throw InvalidProtocolBufferMicroException.recursionLimitExceeded();
        }
        this.recursionDepth++;
        msg.mergeFrom(this);
        checkLastTagWas(WireFormatMicro.makeTag(fieldNumber, 4));
        this.recursionDepth--;
    }

    public void readMessage(MessageMicro msg) throws IOException {
        int length = readRawVarint32();
        if (this.recursionDepth >= this.recursionLimit) {
            throw InvalidProtocolBufferMicroException.recursionLimitExceeded();
        }
        int oldLimit = pushLimit(length);
        this.recursionDepth++;
        msg.mergeFrom(this);
        checkLastTagWas(0);
        this.recursionDepth--;
        popLimit(oldLimit);
    }

    public ByteStringMicro readBytes() throws IOException {
        int size = readRawVarint32();
        if (size <= this.bufferSize - this.bufferPos && size > 0) {
            ByteStringMicro result = ByteStringMicro.copyFrom(this.buffer, this.bufferPos, size);
            this.bufferPos += size;
            return result;
        } else if (size == 0) {
            return ByteStringMicro.EMPTY;
        } else {
            return ByteStringMicro.copyFrom(readRawBytes(size));
        }
    }

    public int readUInt32() throws IOException {
        return readRawVarint32();
    }

    public int readEnum() throws IOException {
        return readRawVarint32();
    }

    public int readSFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public long readSFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readSInt32() throws IOException {
        return decodeZigZag32(readRawVarint32());
    }

    public long readSInt64() throws IOException {
        return decodeZigZag64(readRawVarint64());
    }

    public int readRawVarint32() throws IOException {
        byte tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return tmp;
        }
        int result = tmp & 127;
        tmp = readRawByte();
        if (tmp >= (byte) 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 127) << 7;
            tmp = readRawByte();
            if (tmp >= (byte) 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 127) << 14;
                tmp = readRawByte();
                if (tmp >= (byte) 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 127) << 21;
                    tmp = readRawByte();
                    result |= tmp << 28;
                    if (tmp < (byte) 0) {
                        for (int i = 0; i < 5; i++) {
                            if (readRawByte() >= (byte) 0) {
                                return result;
                            }
                        }
                        throw InvalidProtocolBufferMicroException.malformedVarint();
                    }
                }
            }
        }
        return result;
    }

    static int readRawVarint32(InputStream input) throws IOException {
        int b;
        int result = 0;
        int offset = 0;
        while (offset < 32) {
            b = input.read();
            if (b == -1) {
                throw InvalidProtocolBufferMicroException.truncatedMessage();
            }
            result |= (b & 127) << offset;
            if ((b & 128) == 0) {
                return result;
            }
            offset += 7;
        }
        while (offset < DEFAULT_RECURSION_LIMIT) {
            b = input.read();
            if (b == -1) {
                throw InvalidProtocolBufferMicroException.truncatedMessage();
            } else if ((b & 128) == 0) {
                return result;
            } else {
                offset += 7;
            }
        }
        throw InvalidProtocolBufferMicroException.malformedVarint();
    }

    public long readRawVarint64() throws IOException {
        long result = 0;
        for (int shift = 0; shift < DEFAULT_RECURSION_LIMIT; shift += 7) {
            byte b = readRawByte();
            result |= ((long) (b & 127)) << shift;
            if ((b & 128) == 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferMicroException.malformedVarint();
    }

    public int readRawLittleEndian32() throws IOException {
        return (((readRawByte() & 255) | ((readRawByte() & 255) << 8)) | ((readRawByte() & 255) << 16)) | ((readRawByte() & 255) << 24);
    }

    public long readRawLittleEndian64() throws IOException {
        return (((((((((long) readRawByte()) & 255) | ((((long) readRawByte()) & 255) << 8)) | ((((long) readRawByte()) & 255) << 16)) | ((((long) readRawByte()) & 255) << 24)) | ((((long) readRawByte()) & 255) << 32)) | ((((long) readRawByte()) & 255) << 40)) | ((((long) readRawByte()) & 255) << 48)) | ((((long) readRawByte()) & 255) << 56);
    }

    public static int decodeZigZag32(int n) {
        return (n >>> 1) ^ (-(n & 1));
    }

    public static long decodeZigZag64(long n) {
        return (n >>> 1) ^ (-(1 & n));
    }

    private CodedInputStreamMicro(byte[] buffer, int off, int len) {
        this.currentLimit = Integer.MAX_VALUE;
        this.recursionLimit = DEFAULT_RECURSION_LIMIT;
        this.sizeLimit = DEFAULT_SIZE_LIMIT;
        this.buffer = buffer;
        this.bufferSize = off + len;
        this.bufferPos = off;
        this.input = null;
    }

    private CodedInputStreamMicro(InputStream input) {
        this.currentLimit = Integer.MAX_VALUE;
        this.recursionLimit = DEFAULT_RECURSION_LIMIT;
        this.sizeLimit = DEFAULT_SIZE_LIMIT;
        this.buffer = new byte[4096];
        this.bufferSize = 0;
        this.bufferPos = 0;
        this.input = input;
    }

    public int setRecursionLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Recursion limit cannot be negative: " + limit);
        }
        int oldLimit = this.recursionLimit;
        this.recursionLimit = limit;
        return oldLimit;
    }

    public int setSizeLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Size limit cannot be negative: " + limit);
        }
        int oldLimit = this.sizeLimit;
        this.sizeLimit = limit;
        return oldLimit;
    }

    public void resetSizeCounter() {
        this.totalBytesRetired = 0;
    }

    public int pushLimit(int byteLimit) throws InvalidProtocolBufferMicroException {
        if (byteLimit < 0) {
            throw InvalidProtocolBufferMicroException.negativeSize();
        }
        byteLimit += this.totalBytesRetired + this.bufferPos;
        int oldLimit = this.currentLimit;
        if (byteLimit > oldLimit) {
            throw InvalidProtocolBufferMicroException.truncatedMessage();
        }
        this.currentLimit = byteLimit;
        recomputeBufferSizeAfterLimit();
        return oldLimit;
    }

    private void recomputeBufferSizeAfterLimit() {
        this.bufferSize += this.bufferSizeAfterLimit;
        int bufferEnd = this.totalBytesRetired + this.bufferSize;
        if (bufferEnd > this.currentLimit) {
            this.bufferSizeAfterLimit = bufferEnd - this.currentLimit;
            this.bufferSize -= this.bufferSizeAfterLimit;
            return;
        }
        this.bufferSizeAfterLimit = 0;
    }

    public void popLimit(int oldLimit) {
        this.currentLimit = oldLimit;
        recomputeBufferSizeAfterLimit();
    }

    public int getBytesUntilLimit() {
        if (this.currentLimit == Integer.MAX_VALUE) {
            return -1;
        }
        return this.currentLimit - (this.totalBytesRetired + this.bufferPos);
    }

    public boolean isAtEnd() throws IOException {
        return this.bufferPos == this.bufferSize ? refillBuffer(false) ^ 1 : false;
    }

    private boolean refillBuffer(boolean mustSucceed) throws IOException {
        if (this.bufferPos < this.bufferSize) {
            throw new IllegalStateException("refillBuffer() called when buffer wasn't empty.");
        } else if (this.totalBytesRetired + this.bufferSize != this.currentLimit) {
            this.totalBytesRetired += this.bufferSize;
            this.bufferPos = 0;
            this.bufferSize = this.input == null ? -1 : this.input.read(this.buffer);
            if (this.bufferSize == 0 || this.bufferSize < -1) {
                throw new IllegalStateException("InputStream#read(byte[]) returned invalid result: " + this.bufferSize + "\nThe InputStream implementation is buggy.");
            } else if (this.bufferSize == -1) {
                this.bufferSize = 0;
                if (!mustSucceed) {
                    return false;
                }
                throw InvalidProtocolBufferMicroException.truncatedMessage();
            } else {
                recomputeBufferSizeAfterLimit();
                int totalBytesRead = (this.totalBytesRetired + this.bufferSize) + this.bufferSizeAfterLimit;
                if (totalBytesRead <= this.sizeLimit && totalBytesRead >= 0) {
                    return true;
                }
                throw InvalidProtocolBufferMicroException.sizeLimitExceeded();
            }
        } else if (!mustSucceed) {
            return false;
        } else {
            throw InvalidProtocolBufferMicroException.truncatedMessage();
        }
    }

    public byte readRawByte() throws IOException {
        if (this.bufferPos == this.bufferSize) {
            refillBuffer(true);
        }
        byte[] bArr = this.buffer;
        int i = this.bufferPos;
        this.bufferPos = i + 1;
        return bArr[i];
    }

    public byte[] readRawBytes(int size) throws IOException {
        byte[] bytes;
        int pos;
        if (size < 0) {
            throw InvalidProtocolBufferMicroException.negativeSize();
        } else if ((this.totalBytesRetired + this.bufferPos) + size > this.currentLimit) {
            skipRawBytes((this.currentLimit - this.totalBytesRetired) - this.bufferPos);
            throw InvalidProtocolBufferMicroException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            bytes = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, bytes, 0, size);
            this.bufferPos += size;
            return bytes;
        } else if (size < 4096) {
            bytes = new byte[size];
            pos = this.bufferSize - this.bufferPos;
            System.arraycopy(this.buffer, this.bufferPos, bytes, 0, pos);
            this.bufferPos = this.bufferSize;
            refillBuffer(true);
            while (size - pos > this.bufferSize) {
                System.arraycopy(this.buffer, 0, bytes, pos, this.bufferSize);
                pos += this.bufferSize;
                this.bufferPos = this.bufferSize;
                refillBuffer(true);
            }
            System.arraycopy(this.buffer, 0, bytes, pos, size - pos);
            this.bufferPos = size - pos;
            return bytes;
        } else {
            byte[] chunk;
            int originalBufferPos = this.bufferPos;
            int originalBufferSize = this.bufferSize;
            this.totalBytesRetired += this.bufferSize;
            this.bufferPos = 0;
            this.bufferSize = 0;
            int sizeLeft = size - (originalBufferSize - originalBufferPos);
            Vector chunks = new Vector();
            while (sizeLeft > 0) {
                chunk = new byte[Math.min(sizeLeft, 4096)];
                int n;
                for (pos = 0; pos < chunk.length; pos += n) {
                    if (this.input == null) {
                        n = -1;
                    } else {
                        n = this.input.read(chunk, pos, chunk.length - pos);
                    }
                    if (n == -1) {
                        throw InvalidProtocolBufferMicroException.truncatedMessage();
                    }
                    this.totalBytesRetired += n;
                }
                sizeLeft -= chunk.length;
                chunks.addElement(chunk);
            }
            bytes = new byte[size];
            pos = originalBufferSize - originalBufferPos;
            System.arraycopy(this.buffer, originalBufferPos, bytes, 0, pos);
            for (int i = 0; i < chunks.size(); i++) {
                chunk = (byte[]) chunks.elementAt(i);
                System.arraycopy(chunk, 0, bytes, pos, chunk.length);
                pos += chunk.length;
            }
            return bytes;
        }
    }

    public void skipRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferMicroException.negativeSize();
        } else if ((this.totalBytesRetired + this.bufferPos) + size > this.currentLimit) {
            skipRawBytes((this.currentLimit - this.totalBytesRetired) - this.bufferPos);
            throw InvalidProtocolBufferMicroException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            this.bufferPos += size;
        } else {
            int pos = this.bufferSize - this.bufferPos;
            this.totalBytesRetired += this.bufferSize;
            this.bufferPos = 0;
            this.bufferSize = 0;
            while (pos < size) {
                int n = this.input == null ? -1 : (int) this.input.skip((long) (size - pos));
                if (n <= 0) {
                    throw InvalidProtocolBufferMicroException.truncatedMessage();
                }
                pos += n;
                this.totalBytesRetired += n;
            }
        }
    }
}
