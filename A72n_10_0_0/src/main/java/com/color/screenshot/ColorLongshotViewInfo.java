package com.color.screenshot;

import android.os.Parcel;
import android.os.Parcelable;

public final class ColorLongshotViewInfo implements Parcelable {
    public static final Parcelable.Creator<ColorLongshotViewInfo> CREATOR = new Parcelable.Creator<ColorLongshotViewInfo>() {
        /* class com.color.screenshot.ColorLongshotViewInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorLongshotViewInfo createFromParcel(Parcel in) {
            return new ColorLongshotViewInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorLongshotViewInfo[] newArray(int size) {
            return new ColorLongshotViewInfo[size];
        }
    };
    private boolean mIsUnsupported = false;

    public ColorLongshotViewInfo() {
    }

    public ColorLongshotViewInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mIsUnsupported ? 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        boolean z = true;
        if (1 != in.readInt()) {
            z = false;
        }
        this.mIsUnsupported = z;
    }

    public void reset() {
        this.mIsUnsupported = false;
    }

    public void setUnsupported() {
        this.mIsUnsupported = true;
    }

    public boolean isUnsupported() {
        return this.mIsUnsupported;
    }
}
