package com.google.protobuf.micro;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public final class CodedOutputStreamMicro {
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int LITTLE_ENDIAN_32_SIZE = 4;
    public static final int LITTLE_ENDIAN_64_SIZE = 8;
    private final byte[] buffer;
    private final int limit;
    private final OutputStream output;
    private int position;

    public static class OutOfSpaceException extends IOException {
        private static final long serialVersionUID = -6947486886997889499L;

        OutOfSpaceException() {
            super("CodedOutputStream was writing to a flat byte array and ran out of space.");
        }
    }

    private CodedOutputStreamMicro(byte[] buffer, int offset, int length) {
        this.output = null;
        this.buffer = buffer;
        this.position = offset;
        this.limit = offset + length;
    }

    private CodedOutputStreamMicro(OutputStream output, byte[] buffer) {
        this.output = output;
        this.buffer = buffer;
        this.position = 0;
        this.limit = buffer.length;
    }

    public static CodedOutputStreamMicro newInstance(OutputStream output) {
        return newInstance(output, DEFAULT_BUFFER_SIZE);
    }

    public static CodedOutputStreamMicro newInstance(OutputStream output, int bufferSize) {
        return new CodedOutputStreamMicro(output, new byte[bufferSize]);
    }

    public static CodedOutputStreamMicro newInstance(byte[] flatArray) {
        return newInstance(flatArray, 0, flatArray.length);
    }

    public static CodedOutputStreamMicro newInstance(byte[] flatArray, int offset, int length) {
        return new CodedOutputStreamMicro(flatArray, offset, length);
    }

    public void writeDouble(int fieldNumber, double value) throws IOException {
        writeTag(fieldNumber, 1);
        writeDoubleNoTag(value);
    }

    public void writeFloat(int fieldNumber, float value) throws IOException {
        writeTag(fieldNumber, 5);
        writeFloatNoTag(value);
    }

    public void writeUInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeUInt64NoTag(value);
    }

    public void writeInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeInt64NoTag(value);
    }

    public void writeInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeInt32NoTag(value);
    }

    public void writeFixed64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 1);
        writeFixed64NoTag(value);
    }

    public void writeFixed32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 5);
        writeFixed32NoTag(value);
    }

    public void writeBool(int fieldNumber, boolean value) throws IOException {
        writeTag(fieldNumber, 0);
        writeBoolNoTag(value);
    }

    public void writeString(int fieldNumber, String value) throws IOException {
        writeTag(fieldNumber, 2);
        writeStringNoTag(value);
    }

    public void writeGroup(int fieldNumber, MessageMicro value) throws IOException {
        writeTag(fieldNumber, 3);
        writeGroupNoTag(value);
        writeTag(fieldNumber, 4);
    }

    public void writeMessage(int fieldNumber, MessageMicro value) throws IOException {
        writeTag(fieldNumber, 2);
        writeMessageNoTag(value);
    }

    public void writeBytes(int fieldNumber, ByteStringMicro value) throws IOException {
        writeTag(fieldNumber, 2);
        writeBytesNoTag(value);
    }

    public void writeByteArray(int fieldNumber, byte[] value) throws IOException {
        writeTag(fieldNumber, 2);
        writeByteArrayNoTag(value);
    }

    public void writeUInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeUInt32NoTag(value);
    }

    public void writeEnum(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeEnumNoTag(value);
    }

    public void writeSFixed32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 5);
        writeSFixed32NoTag(value);
    }

    public void writeSFixed64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 1);
        writeSFixed64NoTag(value);
    }

    public void writeSInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeSInt32NoTag(value);
    }

    public void writeSInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeSInt64NoTag(value);
    }

    public void writeDoubleNoTag(double value) throws IOException {
        writeRawLittleEndian64(Double.doubleToLongBits(value));
    }

    public void writeFloatNoTag(float value) throws IOException {
        writeRawLittleEndian32(Float.floatToIntBits(value));
    }

    public void writeUInt64NoTag(long value) throws IOException {
        writeRawVarint64(value);
    }

    public void writeInt64NoTag(long value) throws IOException {
        writeRawVarint64(value);
    }

    public void writeInt32NoTag(int value) throws IOException {
        if (value >= 0) {
            writeRawVarint32(value);
        } else {
            writeRawVarint64((long) value);
        }
    }

    public void writeFixed64NoTag(long value) throws IOException {
        writeRawLittleEndian64(value);
    }

    public void writeFixed32NoTag(int value) throws IOException {
        writeRawLittleEndian32(value);
    }

    public void writeBoolNoTag(boolean value) throws IOException {
        writeRawByte(value ? 1 : 0);
    }

    public void writeStringNoTag(String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        writeRawVarint32(bytes.length);
        writeRawBytes(bytes);
    }

    public void writeGroupNoTag(MessageMicro value) throws IOException {
        value.writeTo(this);
    }

    public void writeMessageNoTag(MessageMicro value) throws IOException {
        writeRawVarint32(value.getCachedSize());
        value.writeTo(this);
    }

    public void writeBytesNoTag(ByteStringMicro value) throws IOException {
        byte[] bytes = value.toByteArray();
        writeRawVarint32(bytes.length);
        writeRawBytes(bytes);
    }

    public void writeByteArrayNoTag(byte[] value) throws IOException {
        writeRawVarint32(value.length);
        writeRawBytes(value);
    }

    public void writeUInt32NoTag(int value) throws IOException {
        writeRawVarint32(value);
    }

    public void writeEnumNoTag(int value) throws IOException {
        writeRawVarint32(value);
    }

    public void writeSFixed32NoTag(int value) throws IOException {
        writeRawLittleEndian32(value);
    }

    public void writeSFixed64NoTag(long value) throws IOException {
        writeRawLittleEndian64(value);
    }

    public void writeSInt32NoTag(int value) throws IOException {
        writeRawVarint32(encodeZigZag32(value));
    }

    public void writeSInt64NoTag(long value) throws IOException {
        writeRawVarint64(encodeZigZag64(value));
    }

    public static int computeDoubleSize(int fieldNumber, double value) {
        return computeTagSize(fieldNumber) + computeDoubleSizeNoTag(value);
    }

    public static int computeFloatSize(int fieldNumber, float value) {
        return computeTagSize(fieldNumber) + computeFloatSizeNoTag(value);
    }

    public static int computeUInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeUInt64SizeNoTag(value);
    }

    public static int computeInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeInt64SizeNoTag(value);
    }

    public static int computeInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeInt32SizeNoTag(value);
    }

    public static int computeFixed64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeFixed64SizeNoTag(value);
    }

    public static int computeFixed32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeFixed32SizeNoTag(value);
    }

    public static int computeBoolSize(int fieldNumber, boolean value) {
        return computeTagSize(fieldNumber) + computeBoolSizeNoTag(value);
    }

    public static int computeStringSize(int fieldNumber, String value) {
        return computeTagSize(fieldNumber) + computeStringSizeNoTag(value);
    }

    public static int computeGroupSize(int fieldNumber, MessageMicro value) {
        return (computeTagSize(fieldNumber) * 2) + computeGroupSizeNoTag(value);
    }

    public static int computeMessageSize(int fieldNumber, MessageMicro value) {
        return computeTagSize(fieldNumber) + computeMessageSizeNoTag(value);
    }

    public static int computeBytesSize(int fieldNumber, ByteStringMicro value) {
        return computeTagSize(fieldNumber) + computeBytesSizeNoTag(value);
    }

    public static int computeByteArraySize(int fieldNumber, byte[] value) {
        return computeTagSize(fieldNumber) + computeByteArraySizeNoTag(value);
    }

    public static int computeUInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeUInt32SizeNoTag(value);
    }

    public static int computeEnumSize(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeEnumSizeNoTag(value);
    }

    public static int computeSFixed32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeSFixed32SizeNoTag(value);
    }

    public static int computeSFixed64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeSFixed64SizeNoTag(value);
    }

    public static int computeSInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeSInt32SizeNoTag(value);
    }

    public static int computeSInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeSInt64SizeNoTag(value);
    }

    public static int computeDoubleSizeNoTag(double value) {
        return 8;
    }

    public static int computeFloatSizeNoTag(float value) {
        return 4;
    }

    public static int computeUInt64SizeNoTag(long value) {
        return computeRawVarint64Size(value);
    }

    public static int computeInt64SizeNoTag(long value) {
        return computeRawVarint64Size(value);
    }

    public static int computeInt32SizeNoTag(int value) {
        if (value >= 0) {
            return computeRawVarint32Size(value);
        }
        return 10;
    }

    public static int computeFixed64SizeNoTag(long value) {
        return 8;
    }

    public static int computeFixed32SizeNoTag(int value) {
        return 4;
    }

    public static int computeBoolSizeNoTag(boolean value) {
        return 1;
    }

    public static int computeStringSizeNoTag(String value) {
        try {
            byte[] bytes = value.getBytes("UTF-8");
            return computeRawVarint32Size(bytes.length) + bytes.length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported.");
        }
    }

    public static int computeGroupSizeNoTag(MessageMicro value) {
        return value.getSerializedSize();
    }

    public static int computeMessageSizeNoTag(MessageMicro value) {
        int size = value.getSerializedSize();
        return computeRawVarint32Size(size) + size;
    }

    public static int computeBytesSizeNoTag(ByteStringMicro value) {
        return computeRawVarint32Size(value.size()) + value.size();
    }

    public static int computeByteArraySizeNoTag(byte[] value) {
        return computeRawVarint32Size(value.length) + value.length;
    }

    public static int computeUInt32SizeNoTag(int value) {
        return computeRawVarint32Size(value);
    }

    public static int computeEnumSizeNoTag(int value) {
        return computeRawVarint32Size(value);
    }

    public static int computeSFixed32SizeNoTag(int value) {
        return 4;
    }

    public static int computeSFixed64SizeNoTag(long value) {
        return 8;
    }

    public static int computeSInt32SizeNoTag(int value) {
        return computeRawVarint32Size(encodeZigZag32(value));
    }

    public static int computeSInt64SizeNoTag(long value) {
        return computeRawVarint64Size(encodeZigZag64(value));
    }

    private void refreshBuffer() throws IOException {
        if (this.output == null) {
            throw new OutOfSpaceException();
        }
        this.output.write(this.buffer, 0, this.position);
        this.position = 0;
    }

    public void flush() throws IOException {
        if (this.output != null) {
            refreshBuffer();
        }
    }

    public int spaceLeft() {
        if (this.output == null) {
            return this.limit - this.position;
        }
        throw new UnsupportedOperationException("spaceLeft() can only be called on CodedOutputStreams that are writing to a flat array.");
    }

    public void checkNoSpaceLeft() {
        if (spaceLeft() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public void writeRawByte(byte value) throws IOException {
        if (this.position == this.limit) {
            refreshBuffer();
        }
        byte[] bArr = this.buffer;
        int i = this.position;
        this.position = i + 1;
        bArr[i] = value;
    }

    public void writeRawByte(int value) throws IOException {
        writeRawByte((byte) value);
    }

    public void writeRawBytes(byte[] value) throws IOException {
        writeRawBytes(value, 0, value.length);
    }

    public void writeRawBytes(byte[] value, int offset, int length) throws IOException {
        if (this.limit - this.position >= length) {
            System.arraycopy(value, offset, this.buffer, this.position, length);
            this.position += length;
            return;
        }
        int bytesWritten = this.limit - this.position;
        System.arraycopy(value, offset, this.buffer, this.position, bytesWritten);
        offset += bytesWritten;
        length -= bytesWritten;
        this.position = this.limit;
        refreshBuffer();
        if (length <= this.limit) {
            System.arraycopy(value, offset, this.buffer, 0, length);
            this.position = length;
            return;
        }
        this.output.write(value, offset, length);
    }

    public void writeTag(int fieldNumber, int wireType) throws IOException {
        writeRawVarint32(WireFormatMicro.makeTag(fieldNumber, wireType));
    }

    public static int computeTagSize(int fieldNumber) {
        return computeRawVarint32Size(WireFormatMicro.makeTag(fieldNumber, 0));
    }

    public void writeRawVarint32(int value) throws IOException {
        while ((value & -128) != 0) {
            writeRawByte((value & 127) | 128);
            value >>>= 7;
        }
        writeRawByte(value);
    }

    public static int computeRawVarint32Size(int value) {
        if ((value & -128) == 0) {
            return 1;
        }
        if ((value & -16384) == 0) {
            return 2;
        }
        if ((-2097152 & value) == 0) {
            return 3;
        }
        if ((-268435456 & value) == 0) {
            return 4;
        }
        return 5;
    }

    public void writeRawVarint64(long value) throws IOException {
        while ((-128 & value) != 0) {
            writeRawByte((((int) value) & 127) | 128);
            value >>>= 7;
        }
        writeRawByte((int) value);
    }

    public static int computeRawVarint64Size(long value) {
        if ((-128 & value) == 0) {
            return 1;
        }
        if ((-16384 & value) == 0) {
            return 2;
        }
        if ((-2097152 & value) == 0) {
            return 3;
        }
        if ((-268435456 & value) == 0) {
            return 4;
        }
        if ((-34359738368L & value) == 0) {
            return 5;
        }
        if ((-4398046511104L & value) == 0) {
            return 6;
        }
        if ((-562949953421312L & value) == 0) {
            return 7;
        }
        if ((-72057594037927936L & value) == 0) {
            return 8;
        }
        if ((Long.MIN_VALUE & value) == 0) {
            return 9;
        }
        return 10;
    }

    public void writeRawLittleEndian32(int value) throws IOException {
        writeRawByte(value & 255);
        writeRawByte((value >> 8) & 255);
        writeRawByte((value >> 16) & 255);
        writeRawByte((value >> 24) & 255);
    }

    public void writeRawLittleEndian64(long value) throws IOException {
        writeRawByte(((int) value) & 255);
        writeRawByte(((int) (value >> 8)) & 255);
        writeRawByte(((int) (value >> 16)) & 255);
        writeRawByte(((int) (value >> 24)) & 255);
        writeRawByte(((int) (value >> 32)) & 255);
        writeRawByte(((int) (value >> 40)) & 255);
        writeRawByte(((int) (value >> 48)) & 255);
        writeRawByte(((int) (value >> 56)) & 255);
    }

    public static int encodeZigZag32(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static long encodeZigZag64(long n) {
        return (n << 1) ^ (n >> 63);
    }
}
