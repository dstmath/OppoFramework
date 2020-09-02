package com.mediatek.mmsdk;

import android.os.Parcel;
import android.os.Parcelable;

public class HandDetectionEvent implements Parcelable {
    public static final Parcelable.Creator<HandDetectionEvent> CREATOR = new Parcelable.Creator<HandDetectionEvent>() {
        /* class com.mediatek.mmsdk.HandDetectionEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HandDetectionEvent createFromParcel(Parcel in) {
            return new HandDetectionEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public HandDetectionEvent[] newArray(int size) {
            return new HandDetectionEvent[size];
        }
    };
    private Parcelable boundBox;
    private float confidence;
    private int id;
    private int pose;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.boundBox, flags);
        out.writeFloat(this.confidence);
        out.writeInt(this.id);
        out.writeInt(this.pose);
    }

    public void readFromParcel(Parcel in) {
        this.boundBox = in.readParcelable(null);
        this.confidence = in.readFloat();
        this.id = in.readInt();
        this.pose = in.readInt();
    }

    private HandDetectionEvent(Parcel in) {
        this.boundBox = in.readParcelable(null);
        this.confidence = in.readFloat();
        this.id = in.readInt();
        this.pose = in.readInt();
    }
}
