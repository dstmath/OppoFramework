package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public final class ColorSecureKeyboardData implements Parcelable {
    public static final Creator<ColorSecureKeyboardData> CREATOR = new Creator<ColorSecureKeyboardData>() {
        public ColorSecureKeyboardData createFromParcel(Parcel in) {
            return new ColorSecureKeyboardData(in);
        }

        public ColorSecureKeyboardData[] newArray(int size) {
            return new ColorSecureKeyboardData[size];
        }
    };
    private String mEnable = "true";
    private ArrayList<String> mList1 = new ArrayList();
    private ArrayList<String> mList2 = new ArrayList();

    public ColorSecureKeyboardData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mEnable);
        out.writeStringList(this.mList1);
        out.writeStringList(this.mList2);
    }

    public void readFromParcel(Parcel in) {
        this.mEnable = in.readString();
        this.mList1 = in.createStringArrayList();
        this.mList2 = in.createStringArrayList();
    }

    public void setEnable(String value) {
        this.mEnable = value;
    }

    public String getEnable() {
        return this.mEnable;
    }

    public ArrayList<String> getNormalAppList() {
        return this.mList1;
    }

    public ArrayList<String> getInputMethodAppList() {
        return this.mList2;
    }
}
