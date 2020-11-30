package com.mediatek.capctrl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class CertRequestInfo implements Parcelable {
    public static final Parcelable.Creator<CertRequestInfo> CREATOR = new Parcelable.Creator<CertRequestInfo>() {
        /* class com.mediatek.capctrl.aidl.CertRequestInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CertRequestInfo createFromParcel(Parcel in) {
            return new CertRequestInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public CertRequestInfo[] newArray(int size) {
            return new CertRequestInfo[size];
        }
    };
    private static final String TAG = "CertRequest";
    public int mCallerId;
    public byte[] mCert;
    public byte[] mMsg;

    public CertRequestInfo() {
    }

    public CertRequestInfo(int callerId, byte[] cert, byte[] msg) {
        this.mCallerId = callerId;
        this.mCert = (byte[]) cert.clone();
        this.mMsg = (byte[]) msg.clone();
    }

    private CertRequestInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mCallerId = in.readInt();
        this.mCert = new byte[in.readInt()];
        in.readByteArray(this.mCert);
        this.mMsg = new byte[in.readInt()];
        in.readByteArray(this.mMsg);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCallerId);
        out.writeByteArray(this.mCert);
        out.writeByteArray(this.mMsg);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append(this.mCallerId);
        if (this.mCert != null) {
            int i = 0;
            while (true) {
                byte[] bArr = this.mCert;
                if (i >= bArr.length) {
                    break;
                }
                sb.append((int) bArr[i]);
                i++;
            }
        }
        if (this.mMsg != null) {
            int i2 = 0;
            while (true) {
                byte[] bArr2 = this.mMsg;
                if (i2 >= bArr2.length) {
                    break;
                }
                sb.append((int) bArr2[i2]);
                i2++;
            }
        }
        return sb.toString();
    }
}
