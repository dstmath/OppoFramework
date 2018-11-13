package com.color.screenshot;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ColorLongshotViewInfo implements Parcelable {
    public static final Creator<ColorLongshotViewInfo> CREATOR = new Creator<ColorLongshotViewInfo>() {
        public ColorLongshotViewInfo createFromParcel(Parcel in) {
            return new ColorLongshotViewInfo(in);
        }

        public ColorLongshotViewInfo[] newArray(int size) {
            return new ColorLongshotViewInfo[size];
        }
    };
    private boolean mIsUnsupported = false;

    public ColorLongshotViewInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

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
