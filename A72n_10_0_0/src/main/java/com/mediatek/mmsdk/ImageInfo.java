package com.mediatek.mmsdk;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {
    public static final Parcelable.Creator<ImageInfo> CREATOR = new Parcelable.Creator<ImageInfo>() {
        /* class com.mediatek.mmsdk.ImageInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImageInfo createFromParcel(Parcel in) {
            return new ImageInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };
    private int format;
    private int height;
    private int numOfPlane;
    private int[] stride;
    private int width;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.format);
        out.writeInt(this.width);
        out.writeInt(this.height);
        out.writeInt(this.numOfPlane);
        out.writeInt(this.stride[0]);
        out.writeInt(this.stride[1]);
        out.writeInt(this.stride[2]);
    }

    public void readFromParcel(Parcel in) {
        this.format = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.numOfPlane = in.readInt();
        this.stride[0] = in.readInt();
        this.stride[1] = in.readInt();
        this.stride[2] = in.readInt();
    }

    private ImageInfo(Parcel in) {
        this.format = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.numOfPlane = in.readInt();
        this.stride = new int[3];
        this.stride[0] = in.readInt();
        this.stride[1] = in.readInt();
        this.stride[2] = in.readInt();
    }
}
