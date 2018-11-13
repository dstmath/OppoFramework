package com.qti.snapdragon.sdk.display;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.qti.snapdragon.sdk.display.ColorManager.MODE_TYPE;

public class ModeInfo implements Parcelable {
    public static final Creator<ModeInfo> CREATOR = new Creator<ModeInfo>() {
        public ModeInfo createFromParcel(Parcel inParcel) {
            return new ModeInfo(inParcel.readInt(), inParcel.readString(), inParcel.readInt());
        }

        public ModeInfo[] newArray(int size) {
            return new ModeInfo[size];
        }
    };
    private int id = -1;
    private MODE_TYPE modeType = MODE_TYPE.MODE_SYSTEM;
    private String name = null;

    public ModeInfo(int pId, String pName, int pType) {
        this.id = pId;
        this.name = pName;
        this.modeType = MODE_TYPE.values()[pType];
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public MODE_TYPE getModeType() {
        return this.modeType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destParcel, int flags) {
        destParcel.writeInt(this.id);
        destParcel.writeString(this.name);
        destParcel.writeInt(this.modeType.getValue());
    }
}
