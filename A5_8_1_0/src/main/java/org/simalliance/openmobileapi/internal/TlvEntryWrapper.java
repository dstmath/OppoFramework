package org.simalliance.openmobileapi.internal;

public class TlvEntryWrapper {
    private byte[] mTag;
    private int mTotalLength;
    private byte[] mValue;

    public TlvEntryWrapper(byte[] data, int startPosition, TlvParser parser) throws IllegalArgumentException {
        int position = startPosition;
        this.mTag = parser.getTagBytes(data, startPosition);
        position = startPosition + this.mTag.length;
        byte[] lengthBytes = parser.getLengthBytes(data, position);
        position += lengthBytes.length;
        this.mValue = new byte[parser.getLengthValue(lengthBytes)];
        System.arraycopy(data, position, this.mValue, 0, this.mValue.length);
        this.mTotalLength = (this.mTag.length + lengthBytes.length) + this.mValue.length;
    }

    public byte[] getTag() {
        return this.mTag;
    }

    public int getTotalLength() {
        return this.mTotalLength;
    }

    public byte[] getValue() {
        return this.mValue;
    }

    public byte[] encode() {
        byte[] encodedObject = new byte[this.mTotalLength];
        System.arraycopy(this.mTag, 0, encodedObject, 0, this.mTag.length);
        byte[] lengthBytes = DerTlvCoder.encodeLength(this.mValue.length);
        System.arraycopy(lengthBytes, this.mTag.length, encodedObject, this.mTag.length, lengthBytes.length);
        System.arraycopy(this.mValue, 0, encodedObject, this.mTag.length + lengthBytes.length, this.mValue.length);
        return encodedObject;
    }
}
