package com.aiunit.aon.utils.core;

import android.os.Parcel;
import android.os.Parcelable;

public class FaceInfo implements Parcelable {
    private static final String CHARSET_UTF8 = "UTF-8";
    public static final Parcelable.Creator<FaceInfo> CREATOR = new Parcelable.Creator<FaceInfo>() {
        /* class com.aiunit.aon.utils.core.FaceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FaceInfo createFromParcel(Parcel source) {
            return new FaceInfo(source.readInt(), source.readInt(), source.readFloat(), source.readFloat(), source.readFloat());
        }

        @Override // android.os.Parcelable.Creator
        public FaceInfo[] newArray(int i) {
            return new FaceInfo[i];
        }
    };
    private int mHeight;
    private float mPitch;
    private float mRoll;
    private int mWidth;
    private float mYaw;

    public FaceInfo(int width, int height, float yaw, float pitch, float roll) {
        this.mWidth = width;
        this.mHeight = height;
        this.mYaw = yaw;
        this.mPitch = pitch;
        this.mRoll = roll;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mWidth);
        parcel.writeInt(this.mHeight);
        parcel.writeFloat(this.mYaw);
        parcel.writeFloat(this.mPitch);
        parcel.writeFloat(this.mRoll);
    }

    public void readFromParcel(Parcel parcel) {
        this.mWidth = parcel.readInt();
        this.mHeight = parcel.readInt();
        this.mYaw = parcel.readFloat();
        this.mPitch = (float) parcel.readInt();
        this.mRoll = (float) parcel.readInt();
    }

    public int getmWidth() {
        return this.mWidth;
    }

    public void setmWidth(int mWidth2) {
        this.mWidth = mWidth2;
    }

    public int getmHeight() {
        return this.mHeight;
    }

    public void setmHeight(int mHeight2) {
        this.mHeight = mHeight2;
    }

    public float getmYaw() {
        return this.mYaw;
    }

    public void setmYaw(float mYaw2) {
        this.mYaw = mYaw2;
    }

    public float getmPitch() {
        return this.mPitch;
    }

    public void setmPitch(float mPitch2) {
        this.mPitch = mPitch2;
    }

    public float getmRoll() {
        return this.mRoll;
    }

    public void setmRoll(float mRoll2) {
        this.mRoll = mRoll2;
    }
}
