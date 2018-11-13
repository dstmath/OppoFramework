package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class ColorPackageFreezeData implements Parcelable {
    public static final Creator<ColorPackageFreezeData> CREATOR = new Creator<ColorPackageFreezeData>() {
        public ColorPackageFreezeData createFromParcel(Parcel in) {
            return new ColorPackageFreezeData(in);
        }

        public ColorPackageFreezeData[] newArray(int size) {
            return new ColorPackageFreezeData[size];
        }
    };
    private int mCurAdj = 10000;
    private List<String> mPackageList = new ArrayList();
    private int mPid = 0;
    private String mProcessName = "";
    private int mUid = 0;
    private int mUserId = 0;

    public ColorPackageFreezeData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mPid);
        out.writeInt(this.mUid);
        out.writeInt(this.mCurAdj);
        out.writeInt(this.mUserId);
        out.writeString(this.mProcessName);
        out.writeStringList(this.mPackageList);
    }

    public void readFromParcel(Parcel in) {
        this.mPid = in.readInt();
        this.mUid = in.readInt();
        this.mCurAdj = in.readInt();
        this.mUserId = in.readInt();
        this.mProcessName = in.readString();
        this.mPackageList = in.createStringArrayList();
    }

    public void setPid(int pid) {
        this.mPid = pid;
    }

    public int getPid() {
        return this.mPid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setCurAdj(int curAdj) {
        this.mCurAdj = curAdj;
    }

    public int getCurAdj() {
        return this.mCurAdj;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setPackageList(List<String> packageList) {
        this.mPackageList.clear();
        if (packageList != null) {
            this.mPackageList.addAll(packageList);
        }
    }

    public List<String> getPackageList() {
        return this.mPackageList;
    }
}
