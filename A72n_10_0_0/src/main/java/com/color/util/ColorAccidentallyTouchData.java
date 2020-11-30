package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public final class ColorAccidentallyTouchData implements Parcelable {
    public static final Parcelable.Creator<ColorAccidentallyTouchData> CREATOR = new Parcelable.Creator<ColorAccidentallyTouchData>() {
        /* class com.color.util.ColorAccidentallyTouchData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAccidentallyTouchData createFromParcel(Parcel in) {
            return new ColorAccidentallyTouchData(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAccidentallyTouchData[] newArray(int size) {
            return new ColorAccidentallyTouchData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorAccidentallyTouchData";
    private String mEdgeEnable;
    private ArrayList<String> mEdgeList = new ArrayList<>();
    private String mEdgeT;
    private String mEdgeT1;
    private String mEdgeT2;
    private String mIsEnable;
    private String mLeftOffset;
    private ArrayList<String> mMultiList = new ArrayList<>();
    private String mPointLeftOffset;
    private String mPointRightOffset;
    private String mRightOffset;
    private ArrayList<String> mSingleList = new ArrayList<>();
    private ArrayList<String> mWhiteList = new ArrayList<>();

    public ColorAccidentallyTouchData() {
    }

    public ColorAccidentallyTouchData(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mEdgeEnable);
        out.writeString(this.mEdgeT);
        out.writeString(this.mEdgeT1);
        out.writeString(this.mEdgeT2);
        out.writeString(this.mIsEnable);
        out.writeString(this.mLeftOffset);
        out.writeString(this.mRightOffset);
        out.writeString(this.mPointLeftOffset);
        out.writeString(this.mPointRightOffset);
        out.writeStringList(this.mSingleList);
        out.writeStringList(this.mMultiList);
        out.writeStringList(this.mWhiteList);
        out.writeStringList(this.mEdgeList);
    }

    public void readFromParcel(Parcel in) {
        this.mEdgeEnable = in.readString();
        this.mEdgeT = in.readString();
        this.mEdgeT1 = in.readString();
        this.mEdgeT2 = in.readString();
        this.mIsEnable = in.readString();
        this.mLeftOffset = in.readString();
        this.mRightOffset = in.readString();
        this.mPointLeftOffset = in.readString();
        this.mPointRightOffset = in.readString();
        this.mSingleList = in.createStringArrayList();
        this.mMultiList = in.createStringArrayList();
        this.mWhiteList = in.createStringArrayList();
        this.mEdgeList = in.createStringArrayList();
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

    public void setEdgeEnable(String value) {
        this.mEdgeEnable = value;
    }

    public void setEdgeT(String value) {
        this.mEdgeT = value;
    }

    public void setEdgeT1(String value) {
        this.mEdgeT1 = value;
    }

    public void setEdgeT2(String value) {
        this.mEdgeT2 = value;
    }

    public String getEdgeEnable() {
        return this.mEdgeEnable;
    }

    public String getEdgeT() {
        return this.mEdgeT;
    }

    public String getEdgeT1() {
        return this.mEdgeT1;
    }

    public String getEdgeT2() {
        return this.mEdgeT2;
    }

    public ArrayList<String> getEdgeList() {
        return this.mEdgeList;
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
