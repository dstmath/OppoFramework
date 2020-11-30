package mediatek.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsCbCmasInfo;

public class MtkSmsCbCmasInfo extends SmsCbCmasInfo {
    public static final int CMAS_CLASS_PUBLIC_SAFETY = 7;
    public static final int CMAS_CLASS_WEA_TEST = 8;
    public static final long CMAS_EXPIRATION_UNKNOWN = 0;
    public static final Parcelable.Creator<SmsCbCmasInfo> CREATOR = new Parcelable.Creator<SmsCbCmasInfo>() {
        /* class mediatek.telephony.MtkSmsCbCmasInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SmsCbCmasInfo createFromParcel(Parcel in) {
            return new MtkSmsCbCmasInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public SmsCbCmasInfo[] newArray(int size) {
            return new MtkSmsCbCmasInfo[size];
        }
    };
    private long mExpiration;

    public MtkSmsCbCmasInfo(int messageClass, int category, int responseType, int severity, int urgency, int certainty, long expiration) {
        super(messageClass, category, responseType, severity, urgency, certainty);
        this.mExpiration = expiration;
    }

    public MtkSmsCbCmasInfo(Parcel in) {
        super(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
        this.mExpiration = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        MtkSmsCbCmasInfo.super.writeToParcel(dest, flags);
        dest.writeLong(this.mExpiration);
    }

    public String toString() {
        return MtkSmsCbCmasInfo.super.toString() + "{" + this.mExpiration + "}";
    }

    public long getExpiration() {
        return this.mExpiration;
    }
}
