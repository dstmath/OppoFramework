package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public class GPRSBearerDesc extends BearerDesc {
    public static final Parcelable.Creator<GPRSBearerDesc> CREATOR = new Parcelable.Creator<GPRSBearerDesc>() {
        /* class com.mediatek.internal.telephony.cat.GPRSBearerDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GPRSBearerDesc createFromParcel(Parcel in) {
            return new GPRSBearerDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public GPRSBearerDesc[] newArray(int size) {
            return new GPRSBearerDesc[size];
        }
    };
    public int bearerService;
    public int connectionElement;
    public int dataCompression;
    public int dataRate;
    public int delay;
    public int headerCompression;
    public int mean;
    public int pdpType;
    public int peak;
    public int precedence;
    public int reliability;

    public GPRSBearerDesc() {
        this.precedence = 0;
        this.delay = 0;
        this.reliability = 0;
        this.peak = 0;
        this.mean = 0;
        this.pdpType = 0;
        this.dataCompression = 0;
        this.headerCompression = 0;
        this.dataRate = 0;
        this.bearerService = 0;
        this.connectionElement = 0;
        this.bearerType = 2;
    }

    private GPRSBearerDesc(Parcel in) {
        this.precedence = 0;
        this.delay = 0;
        this.reliability = 0;
        this.peak = 0;
        this.mean = 0;
        this.pdpType = 0;
        this.dataCompression = 0;
        this.headerCompression = 0;
        this.dataRate = 0;
        this.bearerService = 0;
        this.connectionElement = 0;
        this.bearerType = in.readInt();
        this.precedence = in.readInt();
        this.delay = in.readInt();
        this.reliability = in.readInt();
        this.peak = in.readInt();
        this.mean = in.readInt();
        this.pdpType = in.readInt();
        this.dataCompression = in.readInt();
        this.headerCompression = in.readInt();
        this.dataRate = in.readInt();
        this.bearerService = in.readInt();
        this.connectionElement = in.readInt();
    }

    @Override // com.mediatek.internal.telephony.cat.BearerDesc
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bearerType);
        dest.writeInt(this.precedence);
        dest.writeInt(this.delay);
        dest.writeInt(this.reliability);
        dest.writeInt(this.peak);
        dest.writeInt(this.mean);
        dest.writeInt(this.pdpType);
        dest.writeInt(this.dataCompression);
        dest.writeInt(this.headerCompression);
        dest.writeInt(this.dataRate);
        dest.writeInt(this.bearerService);
        dest.writeInt(this.connectionElement);
    }
}
