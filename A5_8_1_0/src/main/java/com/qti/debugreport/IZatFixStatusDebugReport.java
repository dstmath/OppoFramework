package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatFixStatusDebugReport implements Parcelable {
    public static final Creator<IZatFixStatusDebugReport> CREATOR = new Creator<IZatFixStatusDebugReport>() {
        public IZatFixStatusDebugReport createFromParcel(Parcel source) {
            return new IZatFixStatusDebugReport(source);
        }

        public IZatFixStatusDebugReport[] newArray(int size) {
            return new IZatFixStatusDebugReport[size];
        }
    };
    private static final int IS_FINAL_FIX_SUCCESSFUL = 1;
    private static final int IS_HEPE_CHECK_FAIL = 4;
    private static final int IS_TOO_FEW_SV_SEEN = 2;
    private static final int IS_VERY_LOW_RELAIBILITY = 8;
    private static String TAG = "IZatFixStatus";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private IzatFixStatus mFixStatus;
    private int mFixStatusMask;
    private long mHepeLimit;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public enum IzatFixStatus {
        FINAL_FIX_SUCCESSFUL(0),
        TOO_FEW_SV(1),
        HEPE_CHECK_FAIL(2),
        VERY_LOW_RELAIBILITY_FIX(3);
        
        private final int mFixStatus;

        private IzatFixStatus(int status) {
            this.mFixStatus = status;
        }

        public int getValue() {
            return this.mFixStatus;
        }
    }

    public IZatFixStatusDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int fixStatusMask, long hepeLimit) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mFixStatusMask = fixStatusMask;
        if ((this.mFixStatusMask & 1) != 0) {
            this.mFixStatus = IzatFixStatus.values()[0];
        } else if ((this.mFixStatusMask & 2) != 0) {
            this.mFixStatus = IzatFixStatus.values()[1];
        } else if ((this.mFixStatusMask & 4) != 0) {
            this.mFixStatus = IzatFixStatus.values()[2];
        } else if ((this.mFixStatusMask & 8) != 0) {
            this.mFixStatus = IzatFixStatus.values()[3];
        }
        this.mHepeLimit = hepeLimit;
    }

    public IZatFixStatusDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mFixStatusMask = source.readInt();
        if ((this.mFixStatusMask & 1) != 0) {
            this.mFixStatus = IzatFixStatus.values()[0];
        } else if ((this.mFixStatusMask & 2) != 0) {
            this.mFixStatus = IzatFixStatus.values()[1];
        } else if ((this.mFixStatusMask & 4) != 0) {
            this.mFixStatus = IzatFixStatus.values()[2];
        } else if ((this.mFixStatusMask & 8) != 0) {
            this.mFixStatus = IzatFixStatus.values()[3];
        }
        this.mHepeLimit = source.readLong();
    }

    public IzatFixStatus getFixStatus() {
        return this.mFixStatus;
    }

    public long getHEPELimit() {
        return this.mHepeLimit;
    }

    public IZatUtcSpec getUTCTimestamp() {
        return this.mUtcTimeLastUpdated;
    }

    public IZatUtcSpec getLastReportedUTCTime() {
        return this.mUtcTimeLastReported;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mUtcTimeLastUpdated, 0);
        dest.writeParcelable(this.mUtcTimeLastReported, 0);
        dest.writeInt(this.mFixStatusMask);
        dest.writeLong(this.mHepeLimit);
    }
}
