package com.coloros.eventhub.sdk.aidl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;

public class DeviceEventResult implements Parcelable {
    public static final Parcelable.Creator<DeviceEventResult> CREATOR = new Parcelable.Creator<DeviceEventResult>() {
        /* class com.coloros.eventhub.sdk.aidl.DeviceEventResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceEventResult createFromParcel(Parcel in) {
            return new DeviceEventResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceEventResult[] newArray(int size) {
            return new DeviceEventResult[size];
        }
    };
    private static final String TAG = "DeviceEventResult";
    private int mEventStateType;
    private int mEventType;
    private Bundle mExtraData;
    private int mPid;
    private String mPkgName;

    public DeviceEventResult(int eventType, int eventStateType, int pid, String pkgName, Bundle bundle) {
        this.mEventType = eventType;
        this.mEventStateType = eventStateType;
        this.mPid = pid;
        this.mPkgName = pkgName;
        this.mExtraData = bundle;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getEventStateType() {
        return this.mEventStateType;
    }

    public int getPid() {
        if (this.mPid == -1) {
            SDKLog.e(TAG, "This event is not supported.");
        }
        return this.mPid;
    }

    public String getPkgName() {
        if (this.mPkgName == null) {
            SDKLog.e(TAG, "This event is not supported.");
        }
        return this.mPkgName;
    }

    public Bundle getExtraData() {
        if (this.mExtraData == null) {
            SDKLog.e(TAG, "This event is not supported.");
        }
        return this.mExtraData;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("DeviceEventResult :");
        stringBuilder.append("\teventType is :");
        stringBuilder.append(this.mEventType);
        stringBuilder.append("\teventStateType is :");
        stringBuilder.append(this.mEventStateType);
        stringBuilder.append("\tpid is : ");
        stringBuilder.append(this.mPid);
        stringBuilder.append("\t\tpackageName is : ");
        stringBuilder.append(this.mPkgName);
        if (this.mExtraData != null) {
            stringBuilder.append("\tExtraData is : ");
            stringBuilder.append(this.mExtraData.toString());
        }
        return stringBuilder.toString();
    }

    public DeviceEventResult(Parcel in) {
        this.mEventType = in.readInt();
        this.mEventStateType = in.readInt();
        this.mPid = in.readInt();
        this.mPkgName = in.readString();
        this.mExtraData = in.readBundle(getClass().getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(this.mEventType);
        dest.writeInt(this.mEventStateType);
        dest.writeInt(this.mPid);
        dest.writeString(this.mPkgName);
        dest.writeBundle(this.mExtraData);
    }
}
