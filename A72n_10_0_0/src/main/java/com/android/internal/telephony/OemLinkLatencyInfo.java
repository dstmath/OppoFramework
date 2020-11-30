package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class OemLinkLatencyInfo implements Parcelable {
    public static final Parcelable.Creator<OemLinkLatencyInfo> CREATOR = new Parcelable.Creator<OemLinkLatencyInfo>() {
        /* class com.android.internal.telephony.OemLinkLatencyInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OemLinkLatencyInfo createFromParcel(Parcel in) {
            return new OemLinkLatencyInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public OemLinkLatencyInfo[] newArray(int size) {
            return new OemLinkLatencyInfo[size];
        }
    };
    private long mEffectiveDownlink;
    private long mEffectiveUplink;
    private long mStatus;

    public OemLinkLatencyInfo() {
        this(0, 1, 1);
    }

    public OemLinkLatencyInfo(long status, long effectiveUplink, long effectiveDownlink) {
        this.mStatus = status;
        this.mEffectiveUplink = effectiveUplink;
        this.mEffectiveDownlink = effectiveDownlink;
    }

    public OemLinkLatencyInfo(Parcel in) {
        this.mStatus = in.readLong();
        this.mEffectiveUplink = in.readLong();
        this.mEffectiveDownlink = in.readLong();
    }

    public long getStatus() {
        return this.mStatus;
    }

    public long setStatus(long status) {
        this.mStatus = status;
        return status;
    }

    public long getEffectiveUplink() {
        return this.mEffectiveUplink;
    }

    public long setEffectiveUplink(long uplink) {
        this.mEffectiveUplink = uplink;
        return uplink;
    }

    public long getEffectiveDownlink() {
        return this.mEffectiveDownlink;
    }

    public long setEffectiveDownlink(long downlink) {
        this.mEffectiveDownlink = downlink;
        return downlink;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mStatus);
        dest.writeLong(this.mEffectiveUplink);
        dest.writeLong(this.mEffectiveDownlink);
    }
}
