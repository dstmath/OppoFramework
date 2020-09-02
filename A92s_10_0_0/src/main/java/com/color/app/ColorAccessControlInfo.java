package com.color.app;

import android.os.Parcel;
import android.os.Parcelable;

public final class ColorAccessControlInfo implements Parcelable {
    public static final Parcelable.Creator<ColorAccessControlInfo> CREATOR = new Parcelable.Creator<ColorAccessControlInfo>() {
        /* class com.color.app.ColorAccessControlInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAccessControlInfo createFromParcel(Parcel source) {
            return new ColorAccessControlInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAccessControlInfo[] newArray(int size) {
            return new ColorAccessControlInfo[size];
        }
    };
    public boolean isEncrypted;
    public boolean isHideIcon;
    public boolean isHideInRecent;
    public boolean isHideNotice;
    public String mName;

    public ColorAccessControlInfo() {
    }

    public ColorAccessControlInfo(Parcel in) {
        this.mName = in.readString();
        boolean z = true;
        this.isEncrypted = in.readByte() != 0;
        this.isHideIcon = in.readByte() != 0;
        this.isHideInRecent = in.readByte() != 0;
        this.isHideNotice = in.readByte() == 0 ? false : z;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeByte(this.isEncrypted ? (byte) 1 : 0);
        dest.writeByte(this.isHideIcon ? (byte) 1 : 0);
        dest.writeByte(this.isHideInRecent ? (byte) 1 : 0);
        dest.writeByte(this.isHideNotice ? (byte) 1 : 0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorAccessControlInfo = { ");
        sb.append(" mName = " + this.mName);
        sb.append(" isEncrypted = " + this.isEncrypted);
        sb.append(" isHideIcon = " + this.isHideIcon);
        sb.append(" isHideInRecent = " + this.isHideInRecent);
        sb.append(" isHideNotice = " + this.isHideNotice);
        sb.append("}");
        return sb.toString();
    }
}
