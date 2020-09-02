package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.List;

public final class ColorResolveData implements Parcelable {
    public static final Parcelable.Creator<ColorResolveData> CREATOR = new Parcelable.Creator<ColorResolveData>() {
        /* class com.color.util.ColorResolveData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorResolveData createFromParcel(Parcel in) {
            return new ColorResolveData(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorResolveData[] newArray(int size) {
            return new ColorResolveData[size];
        }
    };
    private HashMap<String, List<String>> mMap1 = new HashMap<>();
    private HashMap<String, List<String>> mMap2 = new HashMap<>();
    private HashMap<String, List<String>> mMap3 = new HashMap<>();
    private HashMap<String, List<String>> mMap4 = new HashMap<>();
    private HashMap<String, List<String>> mMap5 = new HashMap<>();

    public ColorResolveData() {
    }

    public ColorResolveData(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(this.mMap1);
        out.writeMap(this.mMap2);
        out.writeMap(this.mMap3);
        out.writeMap(this.mMap4);
        out.writeMap(this.mMap5);
    }

    public void readFromParcel(Parcel in) {
        this.mMap1 = in.readHashMap(HashMap.class.getClassLoader());
        this.mMap2 = in.readHashMap(HashMap.class.getClassLoader());
        this.mMap3 = in.readHashMap(HashMap.class.getClassLoader());
        this.mMap4 = in.readHashMap(HashMap.class.getClassLoader());
        this.mMap5 = in.readHashMap(HashMap.class.getClassLoader());
    }

    public HashMap<String, List<String>> getBlackResolveMap() {
        return this.mMap1;
    }

    public HashMap<String, List<String>> getResolveMap() {
        return this.mMap2;
    }

    public HashMap<String, List<String>> getChooseMap() {
        return this.mMap3;
    }

    public HashMap<String, List<String>> getBlackChoosePackageMap() {
        return this.mMap4;
    }

    public HashMap<String, List<String>> getBlackChooseActivityMap() {
        return this.mMap5;
    }
}
