package com.color.zoomwindow;

import android.os.Parcel;
import android.os.Parcelable;

public class ColorZoomWindowSize implements Parcelable {
    public static final Parcelable.Creator<ColorZoomWindowSize> CREATOR = new Parcelable.Creator<ColorZoomWindowSize>() {
        /* class com.color.zoomwindow.ColorZoomWindowSize.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowSize createFromParcel(Parcel in) {
            return new ColorZoomWindowSize(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowSize[] newArray(int size) {
            return new ColorZoomWindowSize[size];
        }
    };
    private int mLandScapeHeight;
    private int mLandScapeWidth;
    private int mPortraitHeight;
    private int mPortraitWidth;

    public ColorZoomWindowSize() {
    }

    protected ColorZoomWindowSize(Parcel in) {
        this.mPortraitWidth = in.readInt();
        this.mPortraitHeight = in.readInt();
        this.mLandScapeWidth = in.readInt();
        this.mLandScapeHeight = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPortraitWidth);
        dest.writeInt(this.mPortraitHeight);
        dest.writeInt(this.mLandScapeWidth);
        dest.writeInt(this.mLandScapeHeight);
    }

    public int getPortraitWidth() {
        return this.mPortraitWidth;
    }

    public int getPortraitHeight() {
        return this.mPortraitHeight;
    }

    public int getLandScapeWidth() {
        return this.mLandScapeWidth;
    }

    public int getLandScapeHeight() {
        return this.mLandScapeHeight;
    }

    public void setZoomWindowPortraitWidth(int portraitWidth) {
        this.mPortraitWidth = portraitWidth;
    }

    public void setZoomWindowPortraitHeight(int portraitHeight) {
        this.mPortraitHeight = portraitHeight;
    }

    public void setZoomWindowLandScapeWidth(int landScapeWidth) {
        this.mLandScapeWidth = landScapeWidth;
    }

    public void setZoomWindowlandScapeHeight(int landScapeHeight) {
        this.mLandScapeHeight = landScapeHeight;
    }

    public void setZoomWindowSize(int portraitWidth, int portraitHeight, int landScapeWidth, int landScapeHeight) {
        this.mPortraitWidth = portraitWidth;
        this.mPortraitHeight = portraitHeight;
        this.mLandScapeWidth = landScapeWidth;
        this.mLandScapeHeight = landScapeHeight;
    }

    public String toString() {
        return "PortraitWidth = " + this.mPortraitWidth + ",PortraitHeight = " + this.mPortraitHeight + ",LandScapeWidth = " + this.mLandScapeWidth + ",LandScapeHeight = " + this.mLandScapeHeight;
    }
}
