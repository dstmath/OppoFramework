package com.nxp.nfc;

import android.os.Parcel;
import android.os.Parcelable;

public final class NfcAidServiceInfo implements Parcelable {
    public static final Parcelable.Creator<NfcAidServiceInfo> CREATOR = new Parcelable.Creator<NfcAidServiceInfo>() {
        /* class com.nxp.nfc.NfcAidServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NfcAidServiceInfo createFromParcel(Parcel source) {
            return new NfcAidServiceInfo(source.readString(), source.readInt(), ((Boolean) source.readValue(null)).booleanValue(), source.readString(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public NfcAidServiceInfo[] newArray(int size) {
            return new NfcAidServiceInfo[size];
        }
    };
    static final String TAG = "NfcAidServiceInfo";
    int mAidSize;
    String mComponentName;
    String mOtherCategoryAidGroupDescription;
    String mServiceDescription;
    boolean mState;

    public NfcAidServiceInfo(String componentName, int size, boolean state, String serviceDescription, String otherCategoryAidGroupDescription) {
        this.mComponentName = componentName;
        this.mAidSize = size;
        this.mState = state;
        this.mServiceDescription = serviceDescription;
        this.mOtherCategoryAidGroupDescription = otherCategoryAidGroupDescription;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mComponentName);
        dest.writeInt(this.mAidSize);
        dest.writeValue(Boolean.valueOf(this.mState));
        dest.writeString(this.mServiceDescription);
        dest.writeString(this.mOtherCategoryAidGroupDescription);
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append("componentName: " + getComponentName());
        out.append(" AidSize: " + String.valueOf(getAidSize()));
        out.append(" State: " + String.valueOf(getState()));
        out.append(" ServiceDescription: " + getServiceDescription());
        out.append(" OtherCategoryAidGroupDescription: " + getOtherCategoryAidGroupDescription());
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NfcAidServiceInfo)) {
            return false;
        }
        return ((NfcAidServiceInfo) o).getComponentName().equals(getComponentName());
    }

    public int hashCode() {
        return getComponentName().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public String getComponentName() {
        return this.mComponentName;
    }

    public String getServiceDescription() {
        return this.mServiceDescription;
    }

    public String getOtherCategoryAidGroupDescription() {
        return this.mOtherCategoryAidGroupDescription;
    }

    public Integer getAidSize() {
        return Integer.valueOf(this.mAidSize);
    }

    public boolean getState() {
        return this.mState;
    }
}
