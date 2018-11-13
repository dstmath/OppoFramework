package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public final class ColorAccidentallyTouchData implements Parcelable {
    public static final Creator<ColorAccidentallyTouchData> CREATOR = new Creator<ColorAccidentallyTouchData>() {
        public ColorAccidentallyTouchData createFromParcel(Parcel in) {
            return new ColorAccidentallyTouchData(in);
        }

        public ColorAccidentallyTouchData[] newArray(int size) {
            return new ColorAccidentallyTouchData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorAccidentallyTouchData";
    private String mIsEnable;
    private String mLeftOffset;
    private ArrayList<String> mMultiList = new ArrayList();
    private String mPointLeftOffset;
    private String mPointRightOffset;
    private String mRightOffset;
    private ArrayList<String> mSingleList = new ArrayList();
    private ArrayList<String> mWhiteList = new ArrayList();

    public ColorAccidentallyTouchData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mIsEnable);
        out.writeString(this.mLeftOffset);
        out.writeString(this.mRightOffset);
        out.writeString(this.mPointLeftOffset);
        out.writeString(this.mPointRightOffset);
        out.writeStringList(this.mSingleList);
        out.writeStringList(this.mMultiList);
        out.writeStringList(this.mWhiteList);
    }

    public void readFromParcel(Parcel in) {
        this.mIsEnable = in.readString();
        this.mLeftOffset = in.readString();
        this.mRightOffset = in.readString();
        this.mPointLeftOffset = in.readString();
        this.mPointRightOffset = in.readString();
        this.mSingleList = in.createStringArrayList();
        this.mMultiList = in.createStringArrayList();
        this.mWhiteList = in.createStringArrayList();
    }

    public void setLeftOffset(String value) {
        this.mLeftOffset = value;
    }

    public void setRightOffset(String value) {
        this.mRightOffset = value;
    }

    public void setPointLeftOffset(String value) {
        this.mPointLeftOffset = value;
    }

    public void setPointRightOffset(String value) {
        this.mPointRightOffset = value;
    }

    public void setAccidentalltyTouchEnable(String value) {
        this.mIsEnable = value;
    }

    public String getLeftOffset() {
        return this.mLeftOffset;
    }

    public String getRightOffset() {
        return this.mRightOffset;
    }

    public String getPointLeftOffset() {
        return this.mPointLeftOffset;
    }

    public String getPointRightOffset() {
        return this.mPointRightOffset;
    }

    public String getAccidentalltyTouchEnable() {
        return this.mIsEnable;
    }

    public ArrayList<String> getSingleTouchList() {
        return this.mSingleList;
    }

    public ArrayList<String> getMultiTouchList() {
        return this.mMultiList;
    }

    public ArrayList<String> getTouchWhiteList() {
        return this.mWhiteList;
    }
}
