package com.google.protobuf.micro;

import java.io.IOException;

public abstract class MessageMicro {
    public abstract int getCachedSize();

    public abstract int getSerializedSize();

    public abstract MessageMicro mergeFrom(CodedInputStreamMicro codedInputStreamMicro) throws IOException;

    public abstract void writeTo(CodedOutputStreamMicro codedOutputStreamMicro) throws IOException;

    public byte[] toByteArray() {
        byte[] result = new byte[getSerializedSize()];
        toByteArray(result, 0, result.length);
        return result;
    }

    public void toByteArray(byte[] data, int offset, int length) {
        try {
            CodedOutputStreamMicro output = CodedOutputStreamMicro.newInstance(data, offset, length);
            writeTo(output);
            output.checkNoSpaceLeft();
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).");
        }
    }

    public MessageMicro mergeFrom(byte[] data) throws InvalidProtocolBufferMicroException {
        return mergeFrom(data, 0, data.length);
    }

    public MessageMicro mergeFrom(byte[] data, int off, int len) throws InvalidProtocolBufferMicroException {
        try {
            CodedInputStreamMicro input = CodedInputStreamMicro.newInstance(data, off, len);
            mergeFrom(input);
            input.checkLastTagWas(0);
            return this;
        } catch (InvalidProtocolBufferMicroException e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
        }
    }

    protected boolean parseUnknownField(CodedInputStreamMicro input, int tag) throws IOException {
        return input.skipField(tag);
    }
}
