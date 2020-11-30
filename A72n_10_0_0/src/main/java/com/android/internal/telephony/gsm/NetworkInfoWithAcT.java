package com.android.internal.telephony.gsm;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkInfoWithAcT implements Parcelable {
    public static final Parcelable.Creator<NetworkInfoWithAcT> CREATOR = new Parcelable.Creator<NetworkInfoWithAcT>() {
        /* class com.android.internal.telephony.gsm.NetworkInfoWithAcT.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkInfoWithAcT createFromParcel(Parcel in) {
            return new NetworkInfoWithAcT(in.readString(), in.readString(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
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

    public void setOperatorAlphaName(String operatorAlphaName2) {
        this.operatorAlphaName = operatorAlphaName2;
    }

    public void setOperatorNumeric(String operatorNumeric2) {
        this.operatorNumeric = operatorNumeric2;
    }

    public void setAccessTechnology(int nAct2) {
        this.nAct = nAct2;
    }

    public void setPriority(int nIndex) {
        this.nPriority = nIndex;
    }

    public NetworkInfoWithAcT(String operatorAlphaLong, String operatorNumeric2, int nAct2, int nPriority2) {
        this.operatorAlphaName = operatorAlphaLong;
        this.operatorNumeric = operatorNumeric2;
        this.nAct = nAct2;
        this.nPriority = nPriority2;
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
