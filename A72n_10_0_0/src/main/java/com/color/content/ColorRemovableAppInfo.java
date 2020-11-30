package com.color.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.io.PrintWriter;

public class ColorRemovableAppInfo implements Parcelable {
    public static final Parcelable.Creator<ColorRemovableAppInfo> CREATOR = new Parcelable.Creator<ColorRemovableAppInfo>() {
        /* class com.color.content.ColorRemovableAppInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorRemovableAppInfo createFromParcel(Parcel source) {
            return new ColorRemovableAppInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorRemovableAppInfo[] newArray(int size) {
            return new ColorRemovableAppInfo[size];
        }
    };
    private static final String TAG = ColorRemovableAppInfo.class.getSimpleName();
    private String baseCodePath = "";
    private String codePath = "";
    private long fileSize = 0;
    private String packageName = "";
    private boolean uninstalled = false;
    private long versionCode = -1;
    private String versionName = "";

    public ColorRemovableAppInfo(String packageName2) {
        this.packageName = packageName2;
    }

    public ColorRemovableAppInfo(Parcel in) {
        this.packageName = in.readString();
        this.versionCode = in.readLong();
        this.versionName = in.readString();
        this.codePath = in.readString();
        this.baseCodePath = in.readString();
        this.uninstalled = in.readBoolean();
        this.fileSize = in.readLong();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (TextUtils.isEmpty(this.packageName)) {
            dest.writeString("");
        } else {
            dest.writeString(this.packageName);
        }
        dest.writeLong(this.versionCode);
        if (TextUtils.isEmpty(this.versionName)) {
            dest.writeString("");
        } else {
            dest.writeString(this.versionName);
        }
        if (TextUtils.isEmpty(this.codePath)) {
            dest.writeString("");
        } else {
            dest.writeString(this.codePath);
        }
        if (TextUtils.isEmpty(this.baseCodePath)) {
            dest.writeString("");
        } else {
            dest.writeString(this.baseCodePath);
        }
        dest.writeBoolean(this.uninstalled);
        dest.writeLong(this.fileSize);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(long versionCode2) {
        this.versionCode = versionCode2;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName2) {
        this.versionName = versionName2;
    }

    public String getCodePath() {
        return this.codePath;
    }

    public void setCodePath(String codePath2) {
        this.codePath = codePath2;
    }

    public String getBaseCodePath() {
        return this.baseCodePath;
    }

    public void setBaseCodePath(String baseCodePath2) {
        this.baseCodePath = baseCodePath2;
    }

    public boolean isUninstalled() {
        return this.uninstalled;
    }

    public void setUninstalled(boolean uninstalled2) {
        this.uninstalled = uninstalled2;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize2) {
        this.fileSize = fileSize2;
    }

    public void dump(PrintWriter pw) {
        pw.append((CharSequence) ("packageName = " + this.packageName + ", versionCode = " + this.versionCode + ", versionName = " + this.versionName + ", codePath = " + this.codePath + ", baseCodePath = " + this.baseCodePath + ", uninstalled = " + this.uninstalled + ", fileSize = " + this.fileSize));
    }
}
