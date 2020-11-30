package com.android.server.display.ai.sensorrecorder;

import android.os.Parcel;
import android.os.Parcelable;

/* access modifiers changed from: package-private */
public class LuxData implements Parcelable {
    public static final Parcelable.Creator<LuxData> CREATOR = new Parcelable.Creator<LuxData>() {
        /* class com.android.server.display.ai.sensorrecorder.LuxData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LuxData createFromParcel(Parcel in) {
            return new LuxData(in);
        }

        @Override // android.os.Parcelable.Creator
        public LuxData[] newArray(int size) {
            return new LuxData[size];
        }
    };
    public float mLux;
    public long mTime;

    public LuxData(float lux, long time) {
        this.mLux = lux;
        this.mTime = time;
    }

    protected LuxData(Parcel in) {
        this.mLux = in.readFloat();
        this.mTime = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.mLux);
        parcel.writeLong(this.mTime);
    }
}
