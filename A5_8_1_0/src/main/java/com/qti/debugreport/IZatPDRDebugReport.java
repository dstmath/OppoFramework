package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatPDRDebugReport implements Parcelable {
    public static final Creator<IZatPDRDebugReport> CREATOR = new Creator<IZatPDRDebugReport>() {
        public IZatPDRDebugReport createFromParcel(Parcel source) {
            return new IZatPDRDebugReport(source);
        }

        public IZatPDRDebugReport[] newArray(int size) {
            return new IZatPDRDebugReport[size];
        }
    };
    private static final int HEADING_FILTER_ENGAGED = 1;
    private static final int INS_FILTER_ENGAGED = 2;
    private static final int PDR_ENGAGED = 4;
    private static final int PDR_MAG_CALIBRATED = 8;
    private static String TAG = "IZatPDR";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private int mPDRInfoMask;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public IZatPDRDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int pdrInfoMask) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mPDRInfoMask = pdrInfoMask;
    }

    public IZatPDRDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mPDRInfoMask = source.readInt();
    }

    public boolean isPDREngaged() {
        return (this.mPDRInfoMask & 4) != 0;
    }

    public boolean isPDRMagCalibrated() {
        return (this.mPDRInfoMask & 8) != 0;
    }

    public boolean isHDGFilterEngaged() {
        return (this.mPDRInfoMask & 1) != 0;
    }

    public boolean isINSFilterEngaged() {
        return (this.mPDRInfoMask & 2) != 0;
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
        dest.writeInt(this.mPDRInfoMask);
    }
}
