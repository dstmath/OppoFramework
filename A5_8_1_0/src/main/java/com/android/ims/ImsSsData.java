package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsSsData implements Parcelable {
    public static final Creator<ImsSsData> CREATOR = new Creator<ImsSsData>() {
        public ImsSsData createFromParcel(Parcel in) {
            return new ImsSsData(in);
        }

        public ImsSsData[] newArray(int size) {
            return new ImsSsData[size];
        }
    };
    public static final int SS_ACTIVATION = 0;
    public static final int SS_ALL_BARRING = 18;
    public static final int SS_ALL_DATA_TELESERVICES = 3;
    public static final int SS_ALL_TELESERVICES_EXCEPT_SMS = 5;
    public static final int SS_ALL_TELESEVICES = 1;
    public static final int SS_ALL_TELE_AND_BEARER_SERVICES = 0;
    public static final int SS_BAIC = 16;
    public static final int SS_BAIC_ROAMING = 17;
    public static final int SS_BAOC = 13;
    public static final int SS_BAOIC = 14;
    public static final int SS_BAOIC_EXC_HOME = 15;
    public static final int SS_CFU = 0;
    public static final int SS_CFUT = 6;
    public static final int SS_CF_ALL = 4;
    public static final int SS_CF_ALL_CONDITIONAL = 5;
    public static final int SS_CF_BUSY = 1;
    public static final int SS_CF_NOT_REACHABLE = 3;
    public static final int SS_CF_NO_REPLY = 2;
    public static final int SS_CLIP = 7;
    public static final int SS_CLIR = 8;
    public static final int SS_CNAP = 11;
    public static final int SS_COLP = 9;
    public static final int SS_COLR = 10;
    public static final int SS_DEACTIVATION = 1;
    public static final int SS_ERASURE = 4;
    public static final int SS_INCOMING_BARRING = 20;
    public static final int SS_INCOMING_BARRING_ANONYMOUS = 22;
    public static final int SS_INCOMING_BARRING_DN = 21;
    public static final int SS_INTERROGATION = 2;
    public static final int SS_OUTGOING_BARRING = 19;
    public static final int SS_REGISTRATION = 3;
    public static final int SS_SMS_SERVICES = 4;
    public static final int SS_TELEPHONY = 2;
    public static final int SS_WAIT = 12;
    public ImsCallForwardInfo[] mCfInfo;
    public ImsSsInfo[] mImsSsInfo;
    public int mRequestType;
    public int mResult;
    public int mServiceClass;
    public int mServiceType;
    public int[] mSsInfo;
    public int mTeleserviceType;

    public ImsSsData(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mServiceType);
        out.writeInt(this.mRequestType);
        out.writeInt(this.mTeleserviceType);
        out.writeInt(this.mServiceClass);
        out.writeInt(this.mResult);
        out.writeIntArray(this.mSsInfo);
        out.writeParcelableArray(this.mCfInfo, 0);
    }

    private void readFromParcel(Parcel in) {
        this.mServiceType = in.readInt();
        this.mRequestType = in.readInt();
        this.mTeleserviceType = in.readInt();
        this.mServiceClass = in.readInt();
        this.mResult = in.readInt();
        this.mSsInfo = in.createIntArray();
        this.mCfInfo = (ImsCallForwardInfo[]) in.readParcelableArray(getClass().getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public boolean isTypeCF() {
        if (this.mServiceType == 0 || this.mServiceType == 1 || this.mServiceType == 2 || this.mServiceType == 3 || this.mServiceType == 4 || this.mServiceType == 5) {
            return true;
        }
        return false;
    }

    public boolean isTypeUnConditional() {
        return this.mServiceType == 0 || this.mServiceType == 4;
    }

    public boolean isTypeCW() {
        return this.mServiceType == 12;
    }

    public boolean isTypeClip() {
        return this.mServiceType == 7;
    }

    public boolean isTypeColr() {
        return this.mServiceType == 10;
    }

    public boolean isTypeColp() {
        return this.mServiceType == 9;
    }

    public boolean isTypeClir() {
        return this.mServiceType == 8;
    }

    public boolean isTypeIcb() {
        if (this.mServiceType == 21 || this.mServiceType == 22) {
            return true;
        }
        return false;
    }

    public boolean isTypeBarring() {
        if (this.mServiceType == 13 || this.mServiceType == 14 || this.mServiceType == 15 || this.mServiceType == 16 || this.mServiceType == 17 || this.mServiceType == 18 || this.mServiceType == 19 || this.mServiceType == 20) {
            return true;
        }
        return false;
    }

    public boolean isTypeInterrogation() {
        return this.mRequestType == 2;
    }

    public String toString() {
        return "[ImsSsData] ServiceType: " + this.mServiceType + " RequestType: " + this.mRequestType + " TeleserviceType: " + this.mTeleserviceType + " ServiceClass: " + this.mServiceClass + " Result: " + this.mResult;
    }
}
