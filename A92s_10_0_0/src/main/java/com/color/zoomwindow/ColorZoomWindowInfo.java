package com.color.zoomwindow;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class ColorZoomWindowInfo implements Parcelable {
    public static final Parcelable.Creator<ColorZoomWindowInfo> CREATOR = new Parcelable.Creator<ColorZoomWindowInfo>() {
        /* class com.color.zoomwindow.ColorZoomWindowInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowInfo createFromParcel(Parcel source) {
            return new ColorZoomWindowInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowInfo[] newArray(int size) {
            return new ColorZoomWindowInfo[size];
        }
    };
    public String cpnName;
    public Bundle extension;
    public int inputMethodType;
    public boolean inputShow;
    public int lastExitMethod;
    public String lockPkg;
    public int lockUserId;
    public int rotation;
    public boolean windowShown;
    public String zoomPkg;
    public Rect zoomRect;
    public int zoomUserId;

    public ColorZoomWindowInfo() {
        this.extension = new Bundle();
        this.zoomRect = new Rect();
    }

    public ColorZoomWindowInfo(Parcel in) {
        this.extension = new Bundle();
        this.rotation = in.readInt();
        boolean z = true;
        this.windowShown = in.readByte() != 0;
        this.lockPkg = in.readString();
        this.zoomRect = (Rect) in.readParcelable(null);
        this.zoomPkg = in.readString();
        this.lockUserId = in.readInt();
        this.zoomUserId = in.readInt();
        this.inputShow = in.readByte() == 0 ? false : z;
        this.cpnName = in.readString();
        this.lastExitMethod = in.readInt();
        this.inputMethodType = in.readInt();
        this.extension = in.readBundle();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rotation);
        dest.writeByte(this.windowShown ? (byte) 1 : 0);
        dest.writeString(this.lockPkg);
        dest.writeParcelable(this.zoomRect, 0);
        dest.writeString(this.zoomPkg);
        dest.writeInt(this.lockUserId);
        dest.writeInt(this.zoomUserId);
        dest.writeByte(this.inputShow ? (byte) 1 : 0);
        dest.writeString(this.cpnName);
        dest.writeInt(this.lastExitMethod);
        dest.writeInt(this.inputMethodType);
        dest.writeBundle(this.extension);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorZoomWindowInfo = { ");
        sb.append(" pName = " + this.zoomPkg);
        sb.append(" rotation = " + this.rotation);
        sb.append(" shown = " + this.windowShown);
        sb.append(" lockPkg = " + this.lockPkg);
        sb.append(" zoomRect = " + this.zoomRect);
        sb.append(" lockUserId = " + this.lockUserId);
        sb.append(" zoomUserId = " + this.zoomUserId);
        sb.append(" inputShow = " + this.inputShow);
        sb.append(" cpnName = " + this.cpnName);
        sb.append(" lastExitMethod = " + this.lastExitMethod);
        sb.append(" inputMethodType = " + this.inputMethodType);
        sb.append(" extension = " + this.extension);
        sb.append("}");
        return sb.toString();
    }
}
