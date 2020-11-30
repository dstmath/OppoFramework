package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class BearerDesc implements Parcelable {
    public int bearerType = 0;

    public abstract void writeToParcel(Parcel parcel, int i);

    public int describeContents() {
        return 0;
    }
}
