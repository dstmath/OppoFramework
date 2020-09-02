package com.color.oshare;

import android.os.Parcel;
import android.os.Parcelable;

public enum ColorOshareState implements Parcelable {
    IDLE,
    READY,
    TRANSIT_WAIT,
    TRANSITING,
    CANCEL,
    TRANSIT_SUCCESS,
    TRANSIT_FAILED,
    TRANSIT_REJECT,
    TRANSIT_TIMEOUT,
    BUSUY,
    BUSY,
    CANCEL_WAIT,
    SPACE_NOT_ENOUGH;
    
    public static final Parcelable.Creator<ColorOshareState> CREATOR = new Parcelable.Creator<ColorOshareState>() {
        /* class com.color.oshare.ColorOshareState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorOshareState createFromParcel(Parcel in) {
            return (ColorOshareState) in.readSerializable();
        }

        @Override // android.os.Parcelable.Creator
        public ColorOshareState[] newArray(int size) {
            return new ColorOshareState[size];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }
}
