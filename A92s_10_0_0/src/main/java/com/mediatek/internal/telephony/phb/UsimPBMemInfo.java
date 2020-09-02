package com.mediatek.internal.telephony.phb;

import android.os.Parcel;
import android.os.Parcelable;

public class UsimPBMemInfo implements Parcelable {
    public static final Parcelable.Creator<UsimPBMemInfo> CREATOR = new Parcelable.Creator<UsimPBMemInfo>() {
        /* class com.mediatek.internal.telephony.phb.UsimPBMemInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UsimPBMemInfo createFromParcel(Parcel source) {
            return UsimPBMemInfo.createFromParcel(source);
        }

        @Override // android.os.Parcelable.Creator
        public UsimPBMemInfo[] newArray(int size) {
            return new UsimPBMemInfo[size];
        }
    };
    public static final int INT_NOT_SET = -1;
    public static final String STRING_NOT_SET = "";
    private int mAasLength = -1;
    private int mAasTotal = -1;
    private int mAasType = -1;
    private int mAasUsed = -1;
    private int mAdnLength = -1;
    private int mAdnTotal = -1;
    private int mAdnType = -1;
    private int mAdnUsed = -1;
    private int mAnrLength = -1;
    private int mAnrTotal = -1;
    private int mAnrType = -1;
    private int mAnrUsed = -1;
    private int mCcpLength = -1;
    private int mCcpTotal = -1;
    private int mCcpType = -1;
    private int mCcpUsed = -1;
    private int mEmailLength = -1;
    private int mEmailTotal = -1;
    private int mEmailType = -1;
    private int mEmailUsed = -1;
    private int mExt1Length = -1;
    private int mExt1Total = -1;
    private int mExt1Type = -1;
    private int mExt1Used = -1;
    private int mGasLength = -1;
    private int mGasTotal = -1;
    private int mGasType = -1;
    private int mGasUsed = -1;
    private int mSliceIndex = -1;
    private int mSneLength = -1;
    private int mSneTotal = -1;
    private int mSneType = -1;
    private int mSneUsed = -1;

    public static UsimPBMemInfo createFromParcel(Parcel source) {
        UsimPBMemInfo p = new UsimPBMemInfo();
        p.mSliceIndex = source.readInt();
        p.mAdnLength = source.readInt();
        p.mAdnUsed = source.readInt();
        p.mAdnTotal = source.readInt();
        p.mAdnType = source.readInt();
        p.mExt1Length = source.readInt();
        p.mExt1Used = source.readInt();
        p.mExt1Total = source.readInt();
        p.mExt1Type = source.readInt();
        p.mGasLength = source.readInt();
        p.mGasUsed = source.readInt();
        p.mGasTotal = source.readInt();
        p.mGasType = source.readInt();
        p.mAnrLength = source.readInt();
        p.mAnrUsed = source.readInt();
        p.mAnrTotal = source.readInt();
        p.mAnrType = source.readInt();
        p.mAasLength = source.readInt();
        p.mAasUsed = source.readInt();
        p.mAasTotal = source.readInt();
        p.mAasType = source.readInt();
        p.mSneLength = source.readInt();
        p.mSneUsed = source.readInt();
        p.mSneTotal = source.readInt();
        p.mSneType = source.readInt();
        p.mEmailLength = source.readInt();
        p.mEmailUsed = source.readInt();
        p.mEmailTotal = source.readInt();
        p.mEmailType = source.readInt();
        p.mCcpLength = source.readInt();
        p.mCcpUsed = source.readInt();
        p.mCcpTotal = source.readInt();
        p.mCcpType = source.readInt();
        return p;
    }

    public void writeToParcel(Parcel dest) {
        dest.writeInt(this.mSliceIndex);
        dest.writeInt(this.mAdnLength);
        dest.writeInt(this.mAdnUsed);
        dest.writeInt(this.mAdnTotal);
        dest.writeInt(this.mAdnType);
        dest.writeInt(this.mExt1Length);
        dest.writeInt(this.mExt1Used);
        dest.writeInt(this.mExt1Total);
        dest.writeInt(this.mExt1Type);
        dest.writeInt(this.mGasLength);
        dest.writeInt(this.mGasUsed);
        dest.writeInt(this.mGasTotal);
        dest.writeInt(this.mGasType);
        dest.writeInt(this.mAnrLength);
        dest.writeInt(this.mAnrUsed);
        dest.writeInt(this.mAnrTotal);
        dest.writeInt(this.mAnrType);
        dest.writeInt(this.mAasLength);
        dest.writeInt(this.mAasUsed);
        dest.writeInt(this.mAasTotal);
        dest.writeInt(this.mAasType);
        dest.writeInt(this.mSneLength);
        dest.writeInt(this.mSneUsed);
        dest.writeInt(this.mSneTotal);
        dest.writeInt(this.mSneType);
        dest.writeInt(this.mEmailLength);
        dest.writeInt(this.mEmailUsed);
        dest.writeInt(this.mEmailTotal);
        dest.writeInt(this.mEmailType);
        dest.writeInt(this.mCcpLength);
        dest.writeInt(this.mCcpUsed);
        dest.writeInt(this.mCcpTotal);
        dest.writeInt(this.mCcpType);
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return super.toString() + " mSliceIndex: " + this.mSliceIndex + " mAdnLength: " + this.mAdnLength + " mAdnUsed: " + Integer.toString(this.mAdnUsed) + " mAdnTotal:" + Integer.toString(this.mAdnTotal) + " mAdnType:" + Integer.toString(this.mAdnType) + " mExt1Length:" + Integer.toString(this.mExt1Length) + " mExt1Used:" + Integer.toString(this.mExt1Used) + " mExt1Total" + Integer.toString(this.mExt1Total) + " mExt1Type" + Integer.toString(this.mExt1Type) + " mGasLength" + Integer.toString(this.mGasLength) + " mGasUsed" + Integer.toString(this.mGasUsed) + " mGasTotal: " + Integer.toString(this.mGasTotal) + " mGasType: " + Integer.toString(this.mGasType) + " mAnrLength: " + Integer.toString(this.mAnrLength) + " mAnrUsed: " + Integer.toString(this.mAnrUsed) + " mAnrTotal: " + Integer.toString(this.mAnrTotal) + " mAnrType: " + Integer.toString(this.mAnrType) + " mEmailLength: " + Integer.toString(this.mEmailLength) + " mEmailUsed: " + Integer.toString(this.mEmailUsed) + " mEmailTotal: " + Integer.toString(this.mEmailTotal) + " mEmailType: " + Integer.toString(this.mEmailType);
    }

    public int getSliceIndex() {
        return this.mSliceIndex;
    }

    public int getAdnLength() {
        return this.mAdnLength;
    }

    public int getAdnUsed() {
        return this.mAdnUsed;
    }

    public int getAdnTotal() {
        return this.mAdnTotal;
    }

    public int getAdnType() {
        return this.mAdnType;
    }

    public int getAdnFree() {
        return this.mAdnTotal - this.mAdnUsed;
    }

    public int getExt1Length() {
        return this.mExt1Length;
    }

    public int getExt1Used() {
        return this.mExt1Used;
    }

    public int getExt1Total() {
        return this.mExt1Total;
    }

    public int getExt1Type() {
        return this.mExt1Type;
    }

    public int getExt1Free() {
        return this.mExt1Total - this.mExt1Used;
    }

    public int getGasLength() {
        return this.mGasLength;
    }

    public int getGasUsed() {
        return this.mGasUsed;
    }

    public int getGasTotal() {
        return this.mGasTotal;
    }

    public int getGasType() {
        return this.mGasType;
    }

    public int getAnrLength() {
        return this.mAnrLength;
    }

    public int getAnrUsed() {
        return this.mAnrUsed;
    }

    public int getAnrTotal() {
        return this.mAnrTotal;
    }

    public int getAnrType() {
        return this.mAnrType;
    }

    public int getAnrFree() {
        return this.mAnrTotal - this.mAnrUsed;
    }

    public int getAasLength() {
        return this.mAasLength;
    }

    public int getAasUsed() {
        return this.mAasUsed;
    }

    public int getAasTotal() {
        return this.mAasTotal;
    }

    public int getAasType() {
        return this.mAasType;
    }

    public int getSneLength() {
        return this.mSneLength;
    }

    public int getSneUsed() {
        return this.mSneUsed;
    }

    public int getSneTotal() {
        return this.mSneTotal;
    }

    public int getSneType() {
        return this.mSneType;
    }

    public int getEmailLength() {
        return this.mEmailLength;
    }

    public int getEmailUsed() {
        return this.mEmailUsed;
    }

    public int getEmailTotal() {
        return this.mEmailTotal;
    }

    public int getEmailType() {
        return this.mEmailType;
    }

    public int getEmailFree() {
        return this.mEmailTotal - this.mEmailUsed;
    }

    public int getCcpLength() {
        return this.mCcpLength;
    }

    public int getCcpUsed() {
        return this.mCcpUsed;
    }

    public int getCcpTotal() {
        return this.mCcpTotal;
    }

    public int getCcpType() {
        return this.mCcpType;
    }

    public int getCcpFree() {
        return this.mCcpTotal - this.mCcpUsed;
    }

    public int getGasFree() {
        return this.mGasTotal - this.mGasUsed;
    }

    public int getAasFree() {
        return this.mAasTotal - this.mAasUsed;
    }

    public int getSneFree() {
        return this.mSneTotal - this.mSneUsed;
    }

    public void setSliceIndex(int value) {
        this.mSliceIndex = value;
    }

    public void setAdnLength(int value) {
        this.mAdnLength = value;
    }

    public void setAdnUsed(int value) {
        this.mAdnUsed = value;
    }

    public void setAdnTotal(int value) {
        this.mAdnTotal = value;
    }

    public void setAdnType(int value) {
        this.mAdnType = value;
    }

    public void setExt1Length(int value) {
        this.mExt1Length = value;
    }

    public void setExt1Used(int value) {
        this.mExt1Used = value;
    }

    public void setExt1Total(int value) {
        this.mExt1Total = value;
    }

    public void setExt1Type(int value) {
        this.mExt1Type = value;
    }

    public void setGasLength(int value) {
        this.mGasLength = value;
    }

    public void setGasUsed(int value) {
        this.mGasUsed = value;
    }

    public void setGasTotal(int value) {
        this.mGasTotal = value;
    }

    public void setGasType(int value) {
        this.mGasType = value;
    }

    public void setAnrLength(int value) {
        this.mAnrLength = value;
    }

    public void setAnrUsed(int value) {
        this.mAnrUsed = value;
    }

    public void setAnrTotal(int value) {
        this.mAnrTotal = value;
    }

    public void setAnrType(int value) {
        this.mAnrType = value;
    }

    public void setAasLength(int value) {
        this.mAasLength = value;
    }

    public void setAasUsed(int value) {
        this.mAasUsed = value;
    }

    public void setAasTotal(int value) {
        this.mAasTotal = value;
    }

    public void setAasType(int value) {
        this.mAasType = value;
    }

    public void setSneLength(int value) {
        this.mSneLength = value;
    }

    public void setSneUsed(int value) {
        this.mSneUsed = value;
    }

    public void setSneTotal(int value) {
        this.mSneTotal = value;
    }

    public void setSneType(int value) {
        this.mSneType = value;
    }

    public void setEmailLength(int value) {
        this.mEmailLength = value;
    }

    public void setEmailUsed(int value) {
        this.mEmailUsed = value;
    }

    public void setEmailTotal(int value) {
        this.mEmailTotal = value;
    }

    public void setEmailType(int value) {
        this.mEmailType = value;
    }

    public void setCcpLength(int value) {
        this.mCcpLength = value;
    }

    public void setCcpUsed(int value) {
        this.mCcpUsed = value;
    }

    public void setCcpTotal(int value) {
        this.mCcpTotal = value;
    }

    public void setCcpType(int value) {
        this.mCcpType = value;
    }
}
