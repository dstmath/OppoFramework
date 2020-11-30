package com.color.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class ColorAppExitInfo implements Parcelable {
    public static final int APP_SWITCH_VERSION = 1;
    public static final Parcelable.Creator<ColorAppExitInfo> CREATOR = new Parcelable.Creator<ColorAppExitInfo>() {
        /* class com.color.app.ColorAppExitInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAppExitInfo createFromParcel(Parcel source) {
            return new ColorAppExitInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAppExitInfo[] newArray(int size) {
            return new ColorAppExitInfo[size];
        }
    };
    public static final int SWITCH_TYPE_ACTIVITY = 1;
    public static final int SWITCH_TYPE_APP = 2;
    public Bundle extension = new Bundle();
    public boolean hasResumingActivity;
    public boolean isResumingFirstStart;
    public boolean isResumingMultiApp;
    public String resumingActivityName;
    public String resumingPackageName;
    public int resumingWindowMode;
    public String targetName;

    public ColorAppExitInfo() {
    }

    public ColorAppExitInfo(Parcel in) {
        this.targetName = in.readString();
        boolean z = true;
        this.hasResumingActivity = in.readByte() != 0;
        this.resumingPackageName = in.readString();
        this.resumingActivityName = in.readString();
        this.resumingWindowMode = in.readInt();
        this.isResumingMultiApp = in.readByte() != 0;
        this.isResumingFirstStart = in.readByte() == 0 ? false : z;
        this.extension = in.readBundle();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.targetName);
        dest.writeByte(this.hasResumingActivity ? (byte) 1 : 0);
        dest.writeString(this.resumingPackageName);
        dest.writeString(this.resumingActivityName);
        dest.writeInt(this.resumingWindowMode);
        dest.writeByte(this.isResumingMultiApp ? (byte) 1 : 0);
        dest.writeByte(this.isResumingFirstStart ? (byte) 1 : 0);
        dest.writeBundle(this.extension);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorAppExitInfo = { ");
        sb.append(" targetName = " + this.targetName);
        sb.append(" hasResumingActivity = " + this.hasResumingActivity);
        sb.append(" resumingPackageName = " + this.resumingPackageName);
        sb.append(" resumingActivityName = " + this.resumingActivityName);
        sb.append(" resumingWindowMode = " + this.resumingWindowMode);
        sb.append(" isResumingMultiApp = " + this.isResumingMultiApp);
        sb.append(" isResumingFirstStart = " + this.isResumingFirstStart);
        sb.append(" extension = " + this.extension);
        sb.append("}");
        return sb.toString();
    }
}
