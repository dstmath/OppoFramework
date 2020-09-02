package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public class EUTranBearerDesc extends BearerDesc {
    public static final Parcelable.Creator<EUTranBearerDesc> CREATOR = new Parcelable.Creator<EUTranBearerDesc>() {
        /* class com.mediatek.internal.telephony.cat.EUTranBearerDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EUTranBearerDesc createFromParcel(Parcel in) {
            return new EUTranBearerDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public EUTranBearerDesc[] newArray(int size) {
            return new EUTranBearerDesc[size];
        }
    };
    public int QCI;
    public int guarBitRateD;
    public int guarBitRateDEx;
    public int guarBitRateU;
    public int guarBitRateUEx;
    public int maxBitRateD;
    public int maxBitRateDEx;
    public int maxBitRateU;
    public int maxBitRateUEx;
    public int pdnType;

    public EUTranBearerDesc() {
        this.QCI = 0;
        this.maxBitRateU = 0;
        this.maxBitRateD = 0;
        this.guarBitRateU = 0;
        this.guarBitRateD = 0;
        this.maxBitRateUEx = 0;
        this.maxBitRateDEx = 0;
        this.guarBitRateUEx = 0;
        this.guarBitRateDEx = 0;
        this.pdnType = 0;
        this.bearerType = 11;
    }

    private EUTranBearerDesc(Parcel in) {
        this.QCI = 0;
        this.maxBitRateU = 0;
        this.maxBitRateD = 0;
        this.guarBitRateU = 0;
        this.guarBitRateD = 0;
        this.maxBitRateUEx = 0;
        this.maxBitRateDEx = 0;
        this.guarBitRateUEx = 0;
        this.guarBitRateDEx = 0;
        this.pdnType = 0;
        this.bearerType = in.readInt();
        this.QCI = in.readInt();
        this.maxBitRateU = in.readInt();
        this.maxBitRateD = in.readInt();
        this.guarBitRateU = in.readInt();
        this.guarBitRateD = in.readInt();
        this.maxBitRateUEx = in.readInt();
        this.maxBitRateDEx = in.readInt();
        this.guarBitRateUEx = in.readInt();
        this.guarBitRateDEx = in.readInt();
        this.pdnType = in.readInt();
    }

    @Override // com.mediatek.internal.telephony.cat.BearerDesc
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bearerType);
        dest.writeInt(this.QCI);
        dest.writeInt(this.maxBitRateU);
        dest.writeInt(this.maxBitRateD);
        dest.writeInt(this.guarBitRateU);
        dest.writeInt(this.guarBitRateD);
        dest.writeInt(this.maxBitRateUEx);
        dest.writeInt(this.maxBitRateDEx);
        dest.writeInt(this.guarBitRateUEx);
        dest.writeInt(this.guarBitRateDEx);
        dest.writeInt(this.pdnType);
    }
}
