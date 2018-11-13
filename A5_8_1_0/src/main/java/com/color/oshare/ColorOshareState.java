package com.color.oshare;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
    
    public static final Creator<ColorOshareState> CREATOR = null;

    static {
        CREATOR = new Creator<ColorOshareState>() {
            public ColorOshareState createFromParcel(Parcel in) {
                return (ColorOshareState) in.readSerializable();
            }

            public ColorOshareState[] newArray(int size) {
                return new ColorOshareState[size];
            }
        };
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }
}
