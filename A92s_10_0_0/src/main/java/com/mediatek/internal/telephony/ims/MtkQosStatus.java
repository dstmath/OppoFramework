package com.mediatek.internal.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;

public class MtkQosStatus implements Parcelable {
    public static final Parcelable.Creator<MtkQosStatus> CREATOR = new Parcelable.Creator<MtkQosStatus>() {
        /* class com.mediatek.internal.telephony.ims.MtkQosStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkQosStatus createFromParcel(Parcel source) {
            return MtkQosStatus.readFrom(source);
        }

        @Override // android.os.Parcelable.Creator
        public MtkQosStatus[] newArray(int size) {
            return new MtkQosStatus[size];
        }
    };
    public int mDlGbr;
    public int mDlMbr;
    public int mQci;
    public int mUlGbr;
    public int mUlMbr;

    public MtkQosStatus(int qci, int dlGbr, int ulGbr, int dlMbr, int ulMbr) {
        this.mQci = qci;
        this.mDlGbr = dlGbr;
        this.mUlGbr = ulGbr;
        this.mDlMbr = dlMbr;
        this.mUlMbr = ulMbr;
    }

    public static MtkQosStatus readFrom(Parcel p) {
        return new MtkQosStatus(p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
    }

    public void writeTo(Parcel p) {
        p.writeInt(this.mQci);
        p.writeInt(this.mDlGbr);
        p.writeInt(this.mUlGbr);
        p.writeInt(this.mDlMbr);
        p.writeInt(this.mUlMbr);
    }

    public String toString() {
        return "[qci=" + this.mQci + ", dlGbr=" + this.mDlGbr + ", ulGbr=" + this.mUlGbr + ", dlMbr=" + this.mDlMbr + ", ulMbr=" + this.mUlMbr + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeTo(dest);
    }
}
