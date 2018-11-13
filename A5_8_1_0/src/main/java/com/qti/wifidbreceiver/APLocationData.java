package com.qti.wifidbreceiver;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class APLocationData implements Parcelable {
    public static final int AP_LOC_HORIZONTAL_ERR_VALID = 2;
    public static final int AP_LOC_MAR_VALID = 1;
    public static final int AP_LOC_RELIABILITY_VALID = 4;
    public static final int AP_LOC_WITH_LAT_LON = 0;
    public static final Creator<APLocationData> CREATOR = new Creator<APLocationData>() {
        public APLocationData createFromParcel(Parcel in) {
            return new APLocationData(in, null);
        }

        public APLocationData[] newArray(int size) {
            return new APLocationData[size];
        }
    };
    public float mHorizontalError;
    public float mLatitude;
    public float mLongitude;
    public String mMacAddress;
    public float mMaxAntenaRange;
    public int mReliability;
    public int mValidBits;

    /* synthetic */ APLocationData(Parcel in, APLocationData -this1) {
        this(in);
    }

    private APLocationData(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mMacAddress);
        out.writeFloat(this.mLatitude);
        out.writeFloat(this.mLongitude);
        out.writeFloat(this.mMaxAntenaRange);
        out.writeFloat(this.mHorizontalError);
        out.writeInt(this.mReliability);
        out.writeInt(this.mValidBits);
    }

    public void readFromParcel(Parcel in) {
        this.mMacAddress = in.readString();
        this.mLatitude = in.readFloat();
        this.mLongitude = in.readFloat();
        this.mMaxAntenaRange = in.readFloat();
        this.mHorizontalError = in.readFloat();
        this.mReliability = in.readInt();
        this.mValidBits = in.readInt();
    }

    public int describeContents() {
        return 0;
    }
}
