package com.mediatek.wfo;

import android.os.Parcel;
import android.os.Parcelable;

public class DisconnectCause implements Parcelable {
    public static final Parcelable.Creator<DisconnectCause> CREATOR = new Parcelable.Creator<DisconnectCause>() {
        /* class com.mediatek.wfo.DisconnectCause.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DisconnectCause createFromParcel(Parcel source) {
            return new DisconnectCause(source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public DisconnectCause[] newArray(int size) {
            return new DisconnectCause[size];
        }
    };
    private int errorCause;
    private int subErrorCause;

    public DisconnectCause(int error, int subError) {
        this.errorCause = error;
        this.subErrorCause = subError;
    }

    public int getErrorCause() {
        return this.errorCause;
    }

    public int getSubErrorCause() {
        return this.subErrorCause;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.errorCause);
        dest.writeInt(this.subErrorCause);
    }

    public String toString() {
        return "DisconnectCause {errorCause=" + this.errorCause + ", subErrorCause=" + this.subErrorCause + "}";
    }
}
