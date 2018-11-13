package org.codeaurora.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.uicc.SpnOverride;

public class QtiVideoCallDataUsage implements Parcelable {
    public static final Creator<QtiVideoCallDataUsage> CREATOR = new Creator<QtiVideoCallDataUsage>() {
        public QtiVideoCallDataUsage createFromParcel(Parcel in) {
            return new QtiVideoCallDataUsage(in);
        }

        public QtiVideoCallDataUsage[] newArray(int size) {
            return new QtiVideoCallDataUsage[size];
        }
    };
    public static final int DATA_USAGE_INVALID_VALUE = -1;
    public static final int DATA_USAGE_LTE = 0;
    public static final int DATA_USAGE_WLAN = 1;
    private static final String[] TEXT = new String[]{"LteDataUsage = ", " WlanDataUsage = "};
    private long[] mDataUsage;

    public QtiVideoCallDataUsage(long[] dUsage) {
        if (dUsage == null || dUsage.length == 0) {
            throw new RuntimeException();
        }
        this.mDataUsage = dUsage;
    }

    public QtiVideoCallDataUsage(Parcel in) {
        readFromParcel(in);
    }

    public long getLteDataUsage() {
        if (this.mDataUsage.length > 0) {
            return this.mDataUsage[0];
        }
        return -1;
    }

    public long getWlanDataUsage() {
        if (this.mDataUsage.length > 1) {
            return this.mDataUsage[1];
        }
        return -1;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeLongArray(this.mDataUsage);
    }

    public void readFromParcel(Parcel in) {
        this.mDataUsage = in.createLongArray();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        if (this.mDataUsage == null) {
            return null;
        }
        String msg = SpnOverride.MVNO_TYPE_NONE;
        for (int i = 0; i < this.mDataUsage.length; i++) {
            msg = msg + TEXT[i] + this.mDataUsage[i];
        }
        return msg;
    }
}
