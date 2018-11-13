package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Param implements Parcelable {
    public static final Creator<Param> CREATOR = new Creator<Param>() {
        public Param createFromParcel(Parcel source) {
            return new Param(source);
        }

        public Param[] newArray(int size) {
            return new Param[size];
        }
    };
    private int[] paramInt;
    private long[] paramLong;
    private String[] paramString;

    public Param(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.paramInt);
        dest.writeLongArray(this.paramLong);
        dest.writeStringArray(this.paramString);
    }

    public void readFromParcel(Parcel source) {
        this.paramInt = source.createIntArray();
        this.paramLong = source.createLongArray();
        this.paramString = source.createStringArray();
    }

    public int[] getParamInt() {
        return this.paramInt;
    }

    public void setParamInt(int[] paramInt) {
        this.paramInt = paramInt;
    }

    public long[] getParamLong() {
        return this.paramLong;
    }

    public void setParamLong(long[] paramLong) {
        this.paramLong = paramLong;
    }

    public String[] getParamString() {
        return this.paramString;
    }

    public void setParamString(String[] paramString) {
        this.paramString = paramString;
    }
}
