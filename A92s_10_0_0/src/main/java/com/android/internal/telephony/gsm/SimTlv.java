package com.android.internal.telephony.gsm;

public class SimTlv {
    int mCurDataLength;
    int mCurDataOffset;
    int mCurOffset;
    boolean mHasValidTlvObject = parseCurrentTlvObject();
    byte[] mRecord;
    int mTlvLength;
    int mTlvOffset;

    public SimTlv(byte[] record, int offset, int length) {
        this.mRecord = record;
        this.mTlvOffset = offset;
        this.mTlvLength = length;
        this.mCurOffset = offset;
    }

    public boolean nextObject() {
        if (!this.mHasValidTlvObject) {
            return false;
        }
        this.mCurOffset = this.mCurDataOffset + this.mCurDataLength;
        this.mHasValidTlvObject = parseCurrentTlvObject();
        return this.mHasValidTlvObject;
    }

    public boolean isValidObject() {
        return this.mHasValidTlvObject;
    }

    public int getTag() {
        if (!this.mHasValidTlvObject) {
            return 0;
        }
        return this.mRecord[this.mCurOffset] & 255;
    }

    public byte[] getData() {
        if (!this.mHasValidTlvObject) {
            return null;
        }
        int i = this.mCurDataLength;
        byte[] ret = new byte[i];
        System.arraycopy(this.mRecord, this.mCurDataOffset, ret, 0, i);
        return ret;
    }

    private boolean parseCurrentTlvObject() {
        try {
            if (this.mRecord[this.mCurOffset] != 0) {
                if ((this.mRecord[this.mCurOffset] & 255) != 255) {
                    if ((this.mRecord[this.mCurOffset + 1] & 255) < 128) {
                        this.mCurDataLength = this.mRecord[this.mCurOffset + 1] & 255;
                        this.mCurDataOffset = this.mCurOffset + 2;
                    } else if ((this.mRecord[this.mCurOffset + 1] & 255) != 129) {
                        return false;
                    } else {
                        this.mCurDataLength = this.mRecord[this.mCurOffset + 2] & 255;
                        this.mCurDataOffset = this.mCurOffset + 3;
                    }
                    if (this.mCurDataLength + this.mCurDataOffset > this.mTlvOffset + this.mTlvLength) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
