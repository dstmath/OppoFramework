package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherTels implements Parcelable {
    public static final Creator<OtherTels> CREATOR = new Creator<OtherTels>() {
        public OtherTels createFromParcel(Parcel source) {
            return new OtherTels(source);
        }

        public OtherTels[] newArray(int size) {
            return new OtherTels[size];
        }
    };
    private HashMap<String, String> otherTels;

    public OtherTels() {
        this.otherTels = new HashMap();
    }

    public OtherTels(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(this.otherTels);
    }

    public void readFromParcel(Parcel source) {
        source.readMap(this.otherTels, getClass().getClassLoader());
    }

    public HashMap<String, String> getOtherTels() {
        return this.otherTels;
    }

    public void setOtherTels(HashMap<String, String> otherTels) {
        this.otherTels = otherTels;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("otherTels=" + this.otherTels);
        return list.toString();
    }
}
