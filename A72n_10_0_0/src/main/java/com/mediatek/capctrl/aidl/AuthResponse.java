package com.mediatek.capctrl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthResponse implements Parcelable {
    public static final Parcelable.Creator<AuthResponse> CREATOR = new Parcelable.Creator<AuthResponse>() {
        /* class com.mediatek.capctrl.aidl.AuthResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthResponse createFromParcel(Parcel in) {
            return new AuthResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public AuthResponse[] newArray(int size) {
            return new AuthResponse[size];
        }
    };
    private static final String TAG = "AuthResponse";
    public int mCapMask;
    public byte[] mDevId;
    public int mError;

    public AuthResponse() {
    }

    public AuthResponse(int error, byte[] devId, int capMask) {
        this.mError = error;
        this.mDevId = (byte[]) devId.clone();
        this.mCapMask = capMask;
    }

    private AuthResponse(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mError = in.readInt();
        this.mDevId = in.createByteArray();
        this.mCapMask = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mError);
        out.writeByteArray(this.mDevId);
        out.writeInt(this.mCapMask);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append(this.mError);
        if (this.mDevId != null) {
            int i = 0;
            while (true) {
                byte[] bArr = this.mDevId;
                if (i >= bArr.length) {
                    break;
                }
                sb.append((int) bArr[i]);
                i++;
            }
        }
        sb.append(this.mCapMask);
        return sb.toString();
    }
}
