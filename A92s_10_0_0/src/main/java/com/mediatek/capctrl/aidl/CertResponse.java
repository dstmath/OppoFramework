package com.mediatek.capctrl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class CertResponse implements Parcelable {
    public static final Parcelable.Creator<CertResponse> CREATOR = new Parcelable.Creator<CertResponse>() {
        /* class com.mediatek.capctrl.aidl.CertResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CertResponse createFromParcel(Parcel in) {
            return new CertResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public CertResponse[] newArray(int size) {
            return new CertResponse[size];
        }
    };
    private static final String TAG = "CertResponse";
    public int mCustId;
    public int mError;
    public byte[] mRnd;

    public CertResponse() {
    }

    public CertResponse(int error, byte[] rnd, int custId) {
        this.mError = error;
        this.mRnd = (byte[]) rnd.clone();
        this.mCustId = custId;
    }

    private CertResponse(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mError = in.readInt();
        this.mRnd = in.createByteArray();
        this.mCustId = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mError);
        out.writeByteArray(this.mRnd);
        out.writeInt(this.mCustId);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append(this.mError);
        if (this.mRnd != null) {
            int i = 0;
            while (true) {
                byte[] bArr = this.mRnd;
                if (i >= bArr.length) {
                    break;
                }
                sb.append((int) bArr[i]);
                i++;
            }
        }
        sb.append(this.mCustId);
        return sb.toString();
    }
}
