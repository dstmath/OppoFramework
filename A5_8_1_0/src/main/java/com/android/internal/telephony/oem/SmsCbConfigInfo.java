package com.android.internal.telephony.oem;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmsCbConfigInfo implements Parcelable {
    public static final Creator<SmsCbConfigInfo> CREATOR = new Creator<SmsCbConfigInfo>() {
        public SmsCbConfigInfo createFromParcel(Parcel source) {
            return new SmsCbConfigInfo(source.readInt(), source.readInt(), source.readInt(), source.readInt(), source.readByte() != (byte) 0);
        }

        public SmsCbConfigInfo[] newArray(int size) {
            return new SmsCbConfigInfo[size];
        }
    };
    public int mFromCodeScheme;
    public int mFromServiceId;
    public boolean mSelected;
    public int mToCodeScheme;
    public int mToServiceId;

    public SmsCbConfigInfo(int fromId, int toId, int fromScheme, int toScheme, boolean selected) {
        this.mFromServiceId = fromId;
        this.mToServiceId = toId;
        this.mFromCodeScheme = fromScheme;
        this.mToCodeScheme = toScheme;
        this.mSelected = selected;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFromServiceId);
        dest.writeInt(this.mToServiceId);
        dest.writeInt(this.mFromCodeScheme);
        dest.writeInt(this.mToCodeScheme);
        dest.writeByte((byte) (this.mSelected ? 1 : 0));
    }

    public int describeContents() {
        return 0;
    }
}
