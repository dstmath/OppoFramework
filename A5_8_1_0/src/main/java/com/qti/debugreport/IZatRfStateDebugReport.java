package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatRfStateDebugReport implements Parcelable {
    public static final Creator<IZatRfStateDebugReport> CREATOR = new Creator<IZatRfStateDebugReport>() {
        public IZatRfStateDebugReport createFromParcel(Parcel source) {
            return new IZatRfStateDebugReport(source);
        }

        public IZatRfStateDebugReport[] newArray(int size) {
            return new IZatRfStateDebugReport[size];
        }
    };
    private static String TAG = "IZatRfStateReport";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private long mADCAmplitudeI;
    private long mADCAmplitudeQ;
    private long mErrorRecovery;
    private long mGPSBPAmpI;
    private long mGPSBPAmpQ;
    private long mJammerMetricBds;
    private long mJammerMetricGPS;
    private long mJammerMetricGal;
    private long mJammerMetricGlonass;
    private int mPGAGain;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public IZatRfStateDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int pgaGain, long bpAmplI, long bpAmplQ, long adcAmplI, long adcAmplQ, long jammermetricGps, long jammermetricGlonass, long jammermetricBds, long jammermetricGal) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mPGAGain = pgaGain;
        this.mGPSBPAmpI = bpAmplI;
        this.mGPSBPAmpQ = bpAmplQ;
        this.mADCAmplitudeI = adcAmplI;
        this.mADCAmplitudeQ = adcAmplQ;
        this.mJammerMetricGPS = jammermetricGps;
        this.mJammerMetricGlonass = jammermetricGlonass;
        this.mJammerMetricBds = jammermetricBds;
        this.mJammerMetricGal = jammermetricGal;
    }

    public IZatRfStateDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mPGAGain = source.readInt();
        this.mGPSBPAmpI = source.readLong();
        this.mGPSBPAmpQ = source.readLong();
        this.mADCAmplitudeI = source.readLong();
        this.mADCAmplitudeQ = source.readLong();
        this.mJammerMetricGPS = source.readLong();
        this.mJammerMetricGlonass = source.readLong();
        this.mJammerMetricBds = source.readLong();
        this.mJammerMetricGal = source.readLong();
    }

    public IZatUtcSpec getUTCTimestamp() {
        return this.mUtcTimeLastUpdated;
    }

    public IZatUtcSpec getLastReportedUTCTime() {
        return this.mUtcTimeLastReported;
    }

    public int getPGAGain() {
        return this.mPGAGain;
    }

    public long getGPSBPAmpI() {
        return this.mGPSBPAmpI;
    }

    public long getGPSBPAmpQ() {
        return this.mGPSBPAmpQ;
    }

    public long getADCAmplitudeI() {
        return this.mADCAmplitudeI;
    }

    public long getADCAmplitudeQ() {
        return this.mADCAmplitudeQ;
    }

    public long getJammerMetricGPS() {
        return this.mJammerMetricGPS;
    }

    public long getJammerMetricGlonass() {
        return this.mJammerMetricGlonass;
    }

    public long getJammerMetricBds() {
        return this.mJammerMetricBds;
    }

    public long getJammerMetricGal() {
        return this.mJammerMetricGal;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mUtcTimeLastUpdated, 0);
        dest.writeParcelable(this.mUtcTimeLastReported, 0);
        dest.writeInt(this.mPGAGain);
        dest.writeLong(this.mGPSBPAmpI);
        dest.writeLong(this.mGPSBPAmpQ);
        dest.writeLong(this.mADCAmplitudeI);
        dest.writeLong(this.mADCAmplitudeQ);
        dest.writeLong(this.mJammerMetricGPS);
        dest.writeLong(this.mJammerMetricGlonass);
        dest.writeLong(this.mJammerMetricBds);
        dest.writeLong(this.mJammerMetricGal);
    }
}
