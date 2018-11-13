package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IZatErrorRecoveryReport implements Parcelable {
    public static final Creator<IZatErrorRecoveryReport> CREATOR = new Creator<IZatErrorRecoveryReport>() {
        public IZatErrorRecoveryReport createFromParcel(Parcel source) {
            return new IZatErrorRecoveryReport(source);
        }

        public IZatErrorRecoveryReport[] newArray(int size) {
            return new IZatErrorRecoveryReport[size];
        }
    };
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public IZatErrorRecoveryReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
    }

    public IZatErrorRecoveryReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
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
    }
}
