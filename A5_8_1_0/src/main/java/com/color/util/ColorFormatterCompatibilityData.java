package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class ColorFormatterCompatibilityData implements Parcelable {
    public static final Creator<ColorFormatterCompatibilityData> CREATOR = new Creator<ColorFormatterCompatibilityData>() {
        public ColorFormatterCompatibilityData createFromParcel(Parcel in) {
            return new ColorFormatterCompatibilityData(in);
        }

        public ColorFormatterCompatibilityData[] newArray(int size) {
            return new ColorFormatterCompatibilityData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorFormatterCompatibilityData";
    private List<String> mBlackList = new ArrayList();
    private boolean mEnableDisplatOpt = true;
    private int mEnablePolicy = 0;
    private List<String> mExcludeProcessList = new ArrayList();
    private List<String> mSpecialList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    public ColorFormatterCompatibilityData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStringList(this.mExcludeProcessList);
        out.writeStringList(this.mWhiteList);
        out.writeStringList(this.mBlackList);
        out.writeStringList(this.mSpecialList);
        out.writeByte((byte) (this.mEnableDisplatOpt ? 1 : 0));
        out.writeInt(this.mEnablePolicy);
    }

    public void readFromParcel(Parcel in) {
        boolean z = false;
        this.mExcludeProcessList = in.createStringArrayList();
        this.mWhiteList = in.createStringArrayList();
        this.mBlackList = in.createStringArrayList();
        this.mSpecialList = in.createStringArrayList();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.mEnableDisplatOpt = z;
        this.mEnablePolicy = in.readInt();
    }

    public List<String> getExcludeProcessList() {
        return this.mExcludeProcessList;
    }

    public void setExcludeProcessList(List<String> excludeProcessList) {
        this.mExcludeProcessList = excludeProcessList;
    }

    public boolean getDisplatOptEnabled() {
        return this.mEnableDisplatOpt;
    }

    public void setDisplatOptEnabled(boolean enabled) {
        this.mEnableDisplatOpt = enabled;
    }

    public List<String> getWhiteList() {
        return this.mWhiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.mWhiteList = whiteList;
    }

    public List<String> getBlackList() {
        return this.mBlackList;
    }

    public void setBlackList(List<String> blackList) {
        this.mBlackList = blackList;
    }

    public List<String> getSpecialList() {
        return this.mSpecialList;
    }

    public void setSpecialList(List<String> specialList) {
        this.mSpecialList = specialList;
    }

    public int getEnablePolicy() {
        return this.mEnablePolicy;
    }

    public void setEnablePolicy(int policy) {
        this.mEnablePolicy = policy;
    }
}
