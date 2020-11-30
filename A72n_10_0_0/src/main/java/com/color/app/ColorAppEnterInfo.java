package com.color.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class ColorAppEnterInfo implements Parcelable {
    public static final Parcelable.Creator<ColorAppEnterInfo> CREATOR = new Parcelable.Creator<ColorAppEnterInfo>() {
        /* class com.color.app.ColorAppEnterInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAppEnterInfo createFromParcel(Parcel source) {
            return new ColorAppEnterInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAppEnterInfo[] newArray(int size) {
            return new ColorAppEnterInfo[size];
        }
    };
    public static final int SWITCH_TYPE_ACTIVITY = 1;
    public static final int SWITCH_TYPE_APP = 2;
    public Bundle extension = new Bundle();
    public boolean firstStart;
    public Intent intent;
    public String launchedFromPackage;
    public boolean multiApp;
    public String targetName;
    public int windowMode;

    public ColorAppEnterInfo() {
    }

    public ColorAppEnterInfo(Parcel in) {
        this.intent = (Intent) in.readParcelable(Intent.class.getClassLoader());
        this.windowMode = in.readInt();
        this.targetName = in.readString();
        boolean z = true;
        this.multiApp = in.readByte() != 0;
        this.firstStart = in.readByte() == 0 ? false : z;
        this.launchedFromPackage = in.readString();
        this.extension = in.readBundle();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.intent, flags);
        dest.writeInt(this.windowMode);
        dest.writeString(this.targetName);
        dest.writeByte(this.multiApp ? (byte) 1 : 0);
        dest.writeByte(this.firstStart ? (byte) 1 : 0);
        dest.writeString(this.launchedFromPackage);
        dest.writeBundle(this.extension);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorAppEnterInfo = { ");
        sb.append(" windowMode = " + this.windowMode);
        sb.append(" targetName = " + this.targetName);
        sb.append(" multiApp = " + this.multiApp);
        sb.append(" firstStart = " + this.firstStart);
        sb.append(" launchedFromPackage = " + this.launchedFromPackage);
        sb.append(" intent = " + this.intent);
        sb.append(" extension = " + this.extension);
        sb.append("}");
        return sb.toString();
    }
}
