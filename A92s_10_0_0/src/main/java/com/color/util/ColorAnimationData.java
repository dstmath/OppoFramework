package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public final class ColorAnimationData implements Parcelable {
    public static final Parcelable.Creator<ColorAnimationData> CREATOR = new Parcelable.Creator<ColorAnimationData>() {
        /* class com.color.util.ColorAnimationData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAnimationData createFromParcel(Parcel in) {
            return new ColorAnimationData(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAnimationData[] newArray(int size) {
            return new ColorAnimationData[size];
        }
    };
    private boolean mAnimationEnable = false;
    private ArrayList<String> mAppExitAnimationBlackTokens = new ArrayList<>();
    private ArrayList<String> mAppExitCornerRadiusAnimationBlackPackages = new ArrayList<>();
    private ArrayList<String> mAppStartCornerRadiusAnimationBlackPackages = new ArrayList<>();

    public ColorAnimationData() {
    }

    public ColorAnimationData(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringList(this.mAppStartCornerRadiusAnimationBlackPackages);
        out.writeStringList(this.mAppExitCornerRadiusAnimationBlackPackages);
        out.writeStringList(this.mAppExitAnimationBlackTokens);
        out.writeByte(this.mAnimationEnable ? (byte) 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        this.mAppStartCornerRadiusAnimationBlackPackages = in.createStringArrayList();
        this.mAppExitCornerRadiusAnimationBlackPackages = in.createStringArrayList();
        this.mAppExitAnimationBlackTokens = in.createStringArrayList();
        this.mAnimationEnable = in.readByte() != 0;
    }

    public ArrayList<String> getAppStartCornerRadiusAnimationBlackPackages() {
        return this.mAppStartCornerRadiusAnimationBlackPackages;
    }

    public boolean getAnimationEnabled() {
        return this.mAnimationEnable;
    }

    public void setAnimationEnabled(boolean enabled) {
        this.mAnimationEnable = enabled;
    }

    public ArrayList<String> getAppExitCornerRadiusAnimationBlackPackages() {
        return this.mAppExitCornerRadiusAnimationBlackPackages;
    }

    public void setAppStartCornerRadiusAnimationBlackPackages(List<String> list) {
        this.mAppStartCornerRadiusAnimationBlackPackages.clear();
        this.mAppStartCornerRadiusAnimationBlackPackages.addAll(list);
    }

    public void setAppExitCornerRadiusAnimationBlackPackages(List<String> list) {
        this.mAppExitCornerRadiusAnimationBlackPackages.clear();
        this.mAppExitCornerRadiusAnimationBlackPackages.addAll(list);
    }

    public ArrayList<String> getAppExitAnimationBlackTokens() {
        return this.mAppExitAnimationBlackTokens;
    }

    public void setAppExitAnimationBlackTokens(List<String> list) {
        this.mAppExitAnimationBlackTokens.clear();
        this.mAppExitAnimationBlackTokens.addAll(list);
    }
}
