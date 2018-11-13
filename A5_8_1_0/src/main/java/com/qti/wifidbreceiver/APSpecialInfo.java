package com.qti.wifidbreceiver;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class APSpecialInfo implements Parcelable {
    public static final Creator<APSpecialInfo> CREATOR = new Creator<APSpecialInfo>() {
        public APSpecialInfo createFromParcel(Parcel in) {
            return new APSpecialInfo(in, null);
        }

        public APSpecialInfo[] newArray(int size) {
            return new APSpecialInfo[size];
        }
    };
    public int mInfo;
    public String mMacAddress;

    /* synthetic */ APSpecialInfo(Parcel in, APSpecialInfo -this1) {
        this(in);
    }

    private APSpecialInfo(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mMacAddress);
        out.writeInt(this.mInfo);
    }

    public void readFromParcel(Parcel in) {
        this.mMacAddress = in.readString();
        this.mInfo = in.readInt();
    }

    public int describeContents() {
        return 0;
    }
}
