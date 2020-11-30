package com.mediatek.mmsdk;

import android.os.Parcel;
import android.os.Parcelable;

public class Rect implements Parcelable {
    public static final Parcelable.Creator<Rect> CREATOR = new Parcelable.Creator<Rect>() {
        /* class com.mediatek.mmsdk.Rect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Rect createFromParcel(Parcel in) {
            return new Rect(in);
        }

        @Override // android.os.Parcelable.Creator
        public Rect[] newArray(int size) {
            return new Rect[size];
        }
    };
    private int bottom;
    private int left;
    private int right;
    private int top;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.left);
        out.writeInt(this.top);
        out.writeInt(this.right);
        out.writeInt(this.bottom);
    }

    public void readFromParcel(Parcel in) {
        this.left = in.readInt();
        this.top = in.readInt();
        this.right = in.readInt();
        this.bottom = in.readInt();
    }

    private Rect(Parcel in) {
        this.left = in.readInt();
        this.top = in.readInt();
        this.right = in.readInt();
        this.bottom = in.readInt();
    }
}
