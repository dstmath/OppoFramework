package com.qualcomm.qti.telephonyservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class KsNafResponse implements Parcelable {
    public static final Creator<KsNafResponse> CREATOR = new Creator<KsNafResponse>() {
        public KsNafResponse createFromParcel(Parcel in) {
            return new KsNafResponse(in, null);
        }

        public KsNafResponse[] newArray(int size) {
            return new KsNafResponse[size];
        }
    };
    private String mBootstrapTransactionId;
    private String mLifetime;
    private byte[] mResponse;
    private int mType;

    /* synthetic */ KsNafResponse(Parcel in, KsNafResponse -this1) {
        this(in);
    }

    public int getType() {
        return this.mType;
    }

    public byte[] getResponse() {
        return this.mResponse;
    }

    public String getBootstrapTransactionId() {
        return this.mBootstrapTransactionId;
    }

    public String getLifetime() {
        return this.mLifetime;
    }

    protected KsNafResponse(int ksNafType, byte[] ksNafResponse, String bootstrapTransactionId, String ksLifetime) {
        this.mType = ksNafType;
        this.mResponse = ksNafResponse;
        this.mBootstrapTransactionId = bootstrapTransactionId;
        this.mLifetime = ksLifetime;
    }

    private KsNafResponse(Parcel in) {
        this.mType = in.readInt();
        this.mResponse = new byte[in.readInt()];
        in.readByteArray(this.mResponse);
        this.mBootstrapTransactionId = in.readString();
        this.mLifetime = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mType);
        out.writeInt(this.mResponse.length);
        out.writeByteArray(this.mResponse);
        out.writeString(this.mBootstrapTransactionId);
        out.writeString(this.mLifetime);
    }

    public String toString() {
        return "KsNafResponse mType: " + this.mType + " mResponse: " + Arrays.toString(this.mResponse) + " mBootstrapTransactionId: " + this.mBootstrapTransactionId + " mLifetime: " + this.mLifetime;
    }
}
