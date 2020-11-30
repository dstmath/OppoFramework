package com.mediatek.capctrl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthRequestInfo implements Parcelable {
    public static final Parcelable.Creator<AuthRequestInfo> CREATOR = new Parcelable.Creator<AuthRequestInfo>() {
        /* class com.mediatek.capctrl.aidl.AuthRequestInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthRequestInfo createFromParcel(Parcel in) {
            return new AuthRequestInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AuthRequestInfo[] newArray(int size) {
            return new AuthRequestInfo[size];
        }
    };
    private static final String TAG = "AuthRequest";
    public byte[] mAuthMsg;
    public int mCallerId;

    public AuthRequestInfo() {
    }

    public AuthRequestInfo(int callerId, byte[] authMsg) {
        this.mCallerId = callerId;
        this.mAuthMsg = (byte[]) authMsg.clone();
    }

    private AuthRequestInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mCallerId = in.readInt();
        this.mAuthMsg = new byte[in.readInt()];
        in.readByteArray(this.mAuthMsg);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCallerId);
        out.writeByteArray(this.mAuthMsg);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append(this.mCallerId);
        if (this.mAuthMsg != null) {
            int i = 0;
            while (true) {
                byte[] bArr = this.mAuthMsg;
                if (i >= bArr.length) {
                    break;
                }
                sb.append((int) bArr[i]);
                i++;
            }
        }
        return sb.toString();
    }
}
