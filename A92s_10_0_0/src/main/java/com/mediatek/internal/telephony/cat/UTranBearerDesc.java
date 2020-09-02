package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public class UTranBearerDesc extends BearerDesc {
    public static final Parcelable.Creator<UTranBearerDesc> CREATOR = new Parcelable.Creator<UTranBearerDesc>() {
        /* class com.mediatek.internal.telephony.cat.UTranBearerDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UTranBearerDesc createFromParcel(Parcel in) {
            return new UTranBearerDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public UTranBearerDesc[] newArray(int size) {
            return new UTranBearerDesc[size];
        }
    };
    public int deliveryOfErroneousSdus;
    public int deliveryOrder;
    public int guarBitRateDL_High;
    public int guarBitRateDL_Low;
    public int guarBitRateUL_High;
    public int guarBitRateUL_Low;
    public int maxBitRateDL_High;
    public int maxBitRateDL_Low;
    public int maxBitRateUL_High;
    public int maxBitRateUL_Low;
    public int maxSduSize;
    public int pdpType;
    public int residualBitErrorRadio;
    public int sduErrorRatio;
    public int trafficClass;
    public int trafficHandlingPriority;
    public int transferDelay;

    public UTranBearerDesc() {
        this.trafficClass = 0;
        this.maxBitRateUL_High = 0;
        this.maxBitRateUL_Low = 0;
        this.maxBitRateDL_High = 0;
        this.maxBitRateDL_Low = 0;
        this.guarBitRateUL_High = 0;
        this.guarBitRateUL_Low = 0;
        this.guarBitRateDL_High = 0;
        this.guarBitRateDL_Low = 0;
        this.deliveryOrder = 0;
        this.maxSduSize = 0;
        this.sduErrorRatio = 0;
        this.residualBitErrorRadio = 0;
        this.deliveryOfErroneousSdus = 0;
        this.transferDelay = 0;
        this.trafficHandlingPriority = 0;
        this.pdpType = 0;
        this.bearerType = 9;
    }

    private UTranBearerDesc(Parcel in) {
        this.trafficClass = 0;
        this.maxBitRateUL_High = 0;
        this.maxBitRateUL_Low = 0;
        this.maxBitRateDL_High = 0;
        this.maxBitRateDL_Low = 0;
        this.guarBitRateUL_High = 0;
        this.guarBitRateUL_Low = 0;
        this.guarBitRateDL_High = 0;
        this.guarBitRateDL_Low = 0;
        this.deliveryOrder = 0;
        this.maxSduSize = 0;
        this.sduErrorRatio = 0;
        this.residualBitErrorRadio = 0;
        this.deliveryOfErroneousSdus = 0;
        this.transferDelay = 0;
        this.trafficHandlingPriority = 0;
        this.pdpType = 0;
        this.bearerType = in.readInt();
        this.trafficClass = in.readInt();
        this.maxBitRateUL_High = in.readInt();
        this.maxBitRateUL_Low = in.readInt();
        this.maxBitRateDL_High = in.readInt();
        this.maxBitRateDL_Low = in.readInt();
        this.guarBitRateUL_High = in.readInt();
        this.guarBitRateUL_Low = in.readInt();
        this.guarBitRateDL_High = in.readInt();
        this.guarBitRateDL_Low = in.readInt();
        this.deliveryOrder = in.readInt();
        this.maxSduSize = in.readInt();
        this.sduErrorRatio = in.readInt();
        this.residualBitErrorRadio = in.readInt();
        this.deliveryOfErroneousSdus = in.readInt();
        this.transferDelay = in.readInt();
        this.trafficHandlingPriority = in.readInt();
        this.pdpType = in.readInt();
    }

    @Override // com.mediatek.internal.telephony.cat.BearerDesc
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bearerType);
        dest.writeInt(this.trafficClass);
        dest.writeInt(this.maxBitRateUL_High);
        dest.writeInt(this.maxBitRateUL_Low);
        dest.writeInt(this.maxBitRateDL_High);
        dest.writeInt(this.maxBitRateDL_Low);
        dest.writeInt(this.guarBitRateUL_High);
        dest.writeInt(this.guarBitRateUL_Low);
        dest.writeInt(this.guarBitRateDL_High);
        dest.writeInt(this.guarBitRateDL_Low);
        dest.writeInt(this.deliveryOrder);
        dest.writeInt(this.maxSduSize);
        dest.writeInt(this.sduErrorRatio);
        dest.writeInt(this.residualBitErrorRadio);
        dest.writeInt(this.deliveryOfErroneousSdus);
        dest.writeInt(this.transferDelay);
        dest.writeInt(this.trafficHandlingPriority);
        dest.writeInt(this.pdpType);
    }
}
