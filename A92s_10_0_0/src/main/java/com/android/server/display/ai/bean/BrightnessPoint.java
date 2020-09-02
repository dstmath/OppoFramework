package com.android.server.display.ai.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class BrightnessPoint implements Parcelable {
    public static final Parcelable.Creator<BrightnessPoint> CREATOR = new Parcelable.Creator<BrightnessPoint>() {
        /* class com.android.server.display.ai.bean.BrightnessPoint.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BrightnessPoint createFromParcel(Parcel in) {
            return new BrightnessPoint(in);
        }

        @Override // android.os.Parcelable.Creator
        public BrightnessPoint[] newArray(int size) {
            return new BrightnessPoint[size];
        }
    };
    public float x;
    public float y;
    public float ySrc;

    public BrightnessPoint() {
    }

    public BrightnessPoint(float x2, float y2) {
        this.x = x2;
        this.y = y2;
        this.ySrc = y2;
    }

    protected BrightnessPoint(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.ySrc = in.readFloat();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.x);
        parcel.writeFloat(this.y);
        parcel.writeFloat(this.ySrc);
    }

    public String toString() {
        return "BrightnessPoint:(" + this.x + ", " + this.y + ", " + this.ySrc + ")";
    }

    public static BrightnessPoint createPoint(BrightnessPoint point) {
        BrightnessPoint newPoint = new BrightnessPoint();
        newPoint.x = point.x;
        newPoint.y = point.y;
        newPoint.ySrc = point.ySrc;
        return newPoint;
    }
}
