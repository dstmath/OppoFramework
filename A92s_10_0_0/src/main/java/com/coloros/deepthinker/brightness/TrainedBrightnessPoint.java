package com.coloros.deepthinker.brightness;

import android.os.Parcel;
import android.os.Parcelable;

public class TrainedBrightnessPoint implements Parcelable {
    public static final Parcelable.Creator<TrainedBrightnessPoint> CREATOR = new Parcelable.Creator<TrainedBrightnessPoint>() {
        /* class com.coloros.deepthinker.brightness.TrainedBrightnessPoint.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TrainedBrightnessPoint createFromParcel(Parcel source) {
            return new TrainedBrightnessPoint(source);
        }

        @Override // android.os.Parcelable.Creator
        public TrainedBrightnessPoint[] newArray(int size) {
            return new TrainedBrightnessPoint[size];
        }
    };
    public float x;
    public float y;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
    }

    public TrainedBrightnessPoint(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    protected TrainedBrightnessPoint(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }
}
