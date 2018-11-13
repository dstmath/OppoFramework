package com.qti.wifidbreceiver;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class APInfo implements Parcelable {
    public static final Creator<APInfo> CREATOR = new Creator<APInfo>() {
        public APInfo createFromParcel(Parcel in) {
            return new APInfo(in, null);
        }

        public APInfo[] newArray(int size) {
            return new APInfo[size];
        }
    };
    public int mCellRegionID1;
    public int mCellRegionID2;
    public int mCellRegionID3;
    public int mCellRegionID4;
    public int mCellType;
    public String mMacAddress;
    public byte[] mSSID;

    /* synthetic */ APInfo(Parcel in, APInfo -this1) {
        this(in);
    }

    private APInfo(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mMacAddress);
        out.writeInt(this.mCellType);
        out.writeInt(this.mCellRegionID1);
        out.writeInt(this.mCellRegionID2);
        out.writeInt(this.mCellRegionID3);
        out.writeInt(this.mCellRegionID4);
        out.writeByteArray(this.mSSID);
    }

    public void readFromParcel(Parcel in) {
        this.mMacAddress = in.readString();
        this.mCellType = in.readInt();
        this.mCellRegionID1 = in.readInt();
        this.mCellRegionID2 = in.readInt();
        this.mCellRegionID3 = in.readInt();
        this.mCellRegionID4 = in.readInt();
        this.mSSID = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }
}
