package com.coloros.deepthinker.sdk.aidl.proton.apptype;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.apptype.AppInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
    private String mPkgName;
    private int mType;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPkgName);
        dest.writeInt(this.mType);
    }

    public AppInfo(String pkgName, int type) {
        this.mPkgName = pkgName;
        this.mType = type;
    }

    protected AppInfo(Parcel in) {
        this.mPkgName = in.readString();
        this.mType = in.readInt();
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public int getType() {
        return this.mType;
    }
}
