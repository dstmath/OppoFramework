package com.mediatek.capctrl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class EnableCapabilityRequestInfo implements Parcelable {
    public static final Parcelable.Creator<EnableCapabilityRequestInfo> CREATOR = new Parcelable.Creator<EnableCapabilityRequestInfo>() {
        /* class com.mediatek.capctrl.aidl.EnableCapabilityRequestInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EnableCapabilityRequestInfo createFromParcel(Parcel in) {
            return new EnableCapabilityRequestInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public EnableCapabilityRequestInfo[] newArray(int size) {
            return new EnableCapabilityRequestInfo[size];
        }
    };
    private static final String TAG = "EnableCapabilityReqInfo";
    public int mCallerId;
    public String mFeatureName;
    public int mToActive;

    public EnableCapabilityRequestInfo(String featureName, int callerId, int toActive) {
        this.mFeatureName = featureName;
        this.mCallerId = callerId;
        this.mToActive = toActive;
    }

    private EnableCapabilityRequestInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mFeatureName = in.readString();
        this.mCallerId = in.readInt();
        this.mToActive = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mFeatureName);
        out.writeInt(this.mCallerId);
        out.writeInt(this.mToActive);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "EnableCapabilityReqInfo feature name: " + this.mFeatureName + "Caller Id: " + this.mCallerId + " toActive: " + this.mToActive;
    }
}
