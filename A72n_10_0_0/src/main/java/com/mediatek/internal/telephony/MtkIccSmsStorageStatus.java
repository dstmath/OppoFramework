package com.mediatek.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class MtkIccSmsStorageStatus implements Parcelable {
    public static final Parcelable.Creator<MtkIccSmsStorageStatus> CREATOR = new Parcelable.Creator<MtkIccSmsStorageStatus>() {
        /* class com.mediatek.internal.telephony.MtkIccSmsStorageStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkIccSmsStorageStatus createFromParcel(Parcel source) {
            return new MtkIccSmsStorageStatus(source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public MtkIccSmsStorageStatus[] newArray(int size) {
            return new MtkIccSmsStorageStatus[size];
        }
    };
    public int mTotal;
    public int mUsed;

    public MtkIccSmsStorageStatus() {
        this.mUsed = 0;
        this.mTotal = 0;
    }

    public MtkIccSmsStorageStatus(int used, int total) {
        this.mUsed = used;
        this.mTotal = total;
    }

    public int getUsedCount() {
        return this.mUsed;
    }

    public int getTotalCount() {
        return this.mTotal;
    }

    public int getUnused() {
        return this.mTotal - this.mUsed;
    }

    public void reset() {
        this.mUsed = 0;
        this.mTotal = 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUsed);
        dest.writeInt(this.mTotal);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("[");
        sb.append(this.mUsed);
        sb.append(", ");
        sb.append(this.mTotal);
        sb.append("]");
        return sb.toString();
    }
}
