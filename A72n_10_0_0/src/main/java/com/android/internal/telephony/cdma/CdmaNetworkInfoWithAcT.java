package com.android.internal.telephony.cdma;

import android.os.Parcel;
import android.os.Parcelable;

public class CdmaNetworkInfoWithAcT implements Parcelable {
    public static final Parcelable.Creator<CdmaNetworkInfoWithAcT> CREATOR = new Parcelable.Creator() {
        /* class com.android.internal.telephony.cdma.CdmaNetworkInfoWithAcT.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CdmaNetworkInfoWithAcT createFromParcel(Parcel in) {
            return new CdmaNetworkInfoWithAcT(in.readString(), in.readString(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public CdmaNetworkInfoWithAcT[] newArray(int size) {
            return new CdmaNetworkInfoWithAcT[size];
        }
    };
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

    public CdmaNetworkInfoWithAcT(String operatorAlphaLong, String operatorNumeric2, int nAct2, int nPriority2) {
        this.operatorAlphaName = operatorAlphaLong;
        this.operatorNumeric = operatorNumeric2;
        this.nAct = nAct2;
        this.nPriority = nPriority2;
    }

    public String toString() {
        return "CdmaNetworkInfoWithAcT " + this.operatorAlphaName + "/" + this.operatorNumeric + "/" + this.nAct + "/" + this.nPriority;
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
}
