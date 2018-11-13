package com.android.internal.telephony.gsm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkInfoWithAcT implements Parcelable {
    public static final Creator<NetworkInfoWithAcT> CREATOR = new Creator<NetworkInfoWithAcT>() {
        public NetworkInfoWithAcT createFromParcel(Parcel in) {
            return new NetworkInfoWithAcT(in.readString(), in.readString(), in.readInt(), in.readInt());
        }

        public NetworkInfoWithAcT[] newArray(int size) {
            return new NetworkInfoWithAcT[size];
        }
    };
    boolean mLastItem = true;
    int nAct;
    int nPriority;
    String operatorAlphaName;
    String operatorNumeric;

    public String getOperatorAlphaName() {
        return this.operatorAlphaName;
    }

    public String getOperatorNumeric() {
        return this.operatorNumeric;
    }

    public int getAccessTechnology() {
        return this.nAct;
    }

    public int getPriority() {
        return this.nPriority;
    }

    public void setOperatorAlphaName(String operatorAlphaName) {
        this.operatorAlphaName = operatorAlphaName;
    }

    public void setOperatorNumeric(String operatorNumeric) {
        this.operatorNumeric = operatorNumeric;
    }

    public void setAccessTechnology(int nAct) {
        this.nAct = nAct;
    }

    public void setPriority(int nIndex) {
        this.nPriority = nIndex;
    }

    public NetworkInfoWithAcT(String operatorAlphaLong, String operatorNumeric, int nAct, int nPriority) {
        this.operatorAlphaName = operatorAlphaLong;
        this.operatorNumeric = operatorNumeric;
        this.nAct = nAct;
        this.nPriority = nPriority;
    }

    public String toString() {
        return "NetworkInfoWithAcT " + this.operatorAlphaName + "/" + this.operatorNumeric + "/" + this.nAct + "/" + this.nPriority;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.operatorAlphaName);
        dest.writeString(this.operatorNumeric);
        dest.writeInt(this.nAct);
        dest.writeInt(this.nPriority);
    }

    public void setLastItem(boolean lastItem) {
        this.mLastItem = lastItem;
    }

    public boolean isLastItem() {
        return this.mLastItem;
    }
}
