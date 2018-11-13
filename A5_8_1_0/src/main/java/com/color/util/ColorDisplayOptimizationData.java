package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class ColorDisplayOptimizationData implements Parcelable {
    public static final Creator<ColorDisplayOptimizationData> CREATOR = new Creator<ColorDisplayOptimizationData>() {
        public ColorDisplayOptimizationData createFromParcel(Parcel in) {
            return new ColorDisplayOptimizationData(in);
        }

        public ColorDisplayOptimizationData[] newArray(int size) {
            return new ColorDisplayOptimizationData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorDisplayOptimizationData";
    private List<String> mBlackList = new ArrayList();
    private boolean mEnableDisplatOpt = true;
    private boolean mEnableGraphicAccelerationSwitch = true;
    private int mEnablePolicy = 0;
    private List<String> mExcludeProcessList = new ArrayList();
    private List<String> mExcludeWindowList = new ArrayList();
    private List<String> mSpecialList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    public ColorDisplayOptimizationData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeStringList(this.mExcludeProcessList);
        out.writeStringList(this.mWhiteList);
        out.writeStringList(this.mBlackList);
        out.writeStringList(this.mSpecialList);
        out.writeStringList(this.mExcludeWindowList);
        if (this.mEnableDisplatOpt) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeByte((byte) i);
        if (!this.mEnableGraphicAccelerationSwitch) {
            i2 = 0;
        }
        out.writeByte((byte) i2);
        out.writeInt(this.mEnablePolicy);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mExcludeProcessList = in.createStringArrayList();
        this.mWhiteList = in.createStringArrayList();
        this.mBlackList = in.createStringArrayList();
        this.mSpecialList = in.createStringArrayList();
        this.mExcludeWindowList = in.createStringArrayList();
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mEnableDisplatOpt = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.mEnableGraphicAccelerationSwitch = z2;
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

    public List<String> getExcludeWindowList() {
        return this.mExcludeWindowList;
    }

    public void setExcludeWindowList(List<String> excludeWindowList) {
        this.mExcludeWindowList = excludeWindowList;
    }

    public boolean getGraphicAccelerationSwitchEnabled() {
        return this.mEnableGraphicAccelerationSwitch;
    }

    public void setGraphicAccelerationSwitchEnabled(boolean enabled) {
        this.mEnableGraphicAccelerationSwitch = enabled;
    }

    public int getEnablePolicy() {
        return this.mEnablePolicy;
    }

    public void setEnablePolicy(int policy) {
        this.mEnablePolicy = policy;
    }
}
