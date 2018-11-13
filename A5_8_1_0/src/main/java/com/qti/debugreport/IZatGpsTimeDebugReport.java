package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatGpsTimeDebugReport implements Parcelable {
    public static final Creator<IZatGpsTimeDebugReport> CREATOR = new Creator<IZatGpsTimeDebugReport>() {
        public IZatGpsTimeDebugReport createFromParcel(Parcel source) {
            return new IZatGpsTimeDebugReport(source);
        }

        public IZatGpsTimeDebugReport[] newArray(int size) {
            return new IZatGpsTimeDebugReport[size];
        }
    };
    private static String TAG = "IZatGpsTimeReport";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private int mClockFrequencyBias;
    private int mClockFrequencyBiasUncertainity;
    private long mGpsTimeOfWeekInMs;
    private int mGpsWeek;
    private IZatTimeSource mTimeSource;
    private int mTimeUncertainity;
    private boolean mTimeValid;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public enum IZatTimeSource {
        TIME_SOURCE_ESTIMATE_INVALID(0),
        TIME_SOURCE_ESTIMATE_NETWORK_TIME_TRANSFER(1),
        TIME_SOURCE_ESTIMATE_NETWORK_TIME_TAGGING(2),
        TIME_SOURCE_ESTIMATE_EXTERNAL_INPUT(3),
        TIME_SOURCE_ESTIMATE_TOW_DECODE(4),
        TIME_SOURCE_ESTIMATE_TOW_CONFIRMED(5),
        TIME_SOURCE_ESTIMATE_TOW_AND_WEEK_CONFIRMED(6),
        TIME_SOURCE_ESTIMATE_TIME_ALIGNMENT(7),
        TIME_SOURCE_ESTIMATE_NAV_SOLUTION(8),
        TIME_SOURCE_ESTIMATE_SOLVE_FOR_TIME(9),
        TIME_SOURCE_ESTIMATE_GLO_TOD_DECODE(10),
        TIME_SOURCE_ESTIMATE_TIME_CONVERSION(11),
        TIME_SOURCE_ESTIMATE_SLEEP_CLOCK(12),
        TIME_SOURCE_ESTIMATE_SLEEP_CLOCK_TIME_TRANSFER(13),
        TIME_SOURCE_ESTIMATE_UNKNOWN(14),
        TIME_SOURCE_ESTIMATE_WCDMA_SLEEP_TIME_TAGGING(15),
        TIME_SOURCE_ESTIMATE_GSM_SLEEP_TIME_TAGGING(16),
        TIME_SOURCE_ESTIMATE_GAL_TOW_DECODE(17),
        TIME_SOURCE_ESTIMATE_BDS_SOW_DECODE(18),
        TIME_SOURCE_ESTIMATE_QZSS_TOW_DECODE(19);
        
        private final int mValue;

        private IZatTimeSource(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public IZatGpsTimeDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int gpsWeek, long gpsTimeOfweekInMs, boolean timeValid, int timeSource, int timeUncertainity, int clockfrequencyBias, int clockfrequencyBiasUncertainity) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mGpsWeek = gpsWeek;
        this.mGpsTimeOfWeekInMs = gpsTimeOfweekInMs;
        this.mTimeValid = timeValid;
        try {
            this.mTimeSource = IZatTimeSource.values()[timeSource];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.mTimeSource = IZatTimeSource.TIME_SOURCE_ESTIMATE_INVALID;
        }
        this.mTimeUncertainity = timeUncertainity;
        this.mClockFrequencyBias = clockfrequencyBias;
        this.mClockFrequencyBiasUncertainity = clockfrequencyBiasUncertainity;
    }

    public IZatGpsTimeDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mGpsWeek = source.readInt();
        this.mGpsTimeOfWeekInMs = source.readLong();
        this.mTimeValid = source.readInt() == 1;
        this.mTimeSource = IZatTimeSource.values()[source.readInt()];
        this.mTimeUncertainity = source.readInt();
        this.mClockFrequencyBias = source.readInt();
        this.mClockFrequencyBiasUncertainity = source.readInt();
    }

    public IZatUtcSpec getUTCTimestamp() {
        return this.mUtcTimeLastUpdated;
    }

    public IZatUtcSpec getLastReportedUTCTime() {
        return this.mUtcTimeLastReported;
    }

    public int getGpsWeek() {
        return this.mGpsWeek;
    }

    public long getGpsTimeOfWeek() {
        return this.mGpsTimeOfWeekInMs;
    }

    public boolean IsTimeValid() {
        return this.mTimeValid;
    }

    public IZatTimeSource getTimeSource() {
        return this.mTimeSource;
    }

    public int getTimeUncertainity() {
        return this.mTimeUncertainity;
    }

    public int getClockFrequencyBias() {
        return this.mClockFrequencyBias;
    }

    public int getClockFrequencyBiasUncertainity() {
        return this.mClockFrequencyBiasUncertainity;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        dest.writeParcelable(this.mUtcTimeLastUpdated, 0);
        dest.writeParcelable(this.mUtcTimeLastReported, 0);
        dest.writeInt(this.mGpsWeek);
        dest.writeLong(this.mGpsTimeOfWeekInMs);
        if (this.mTimeValid) {
            i = 1;
        }
        dest.writeInt(i);
        dest.writeInt(this.mTimeSource.getValue());
        dest.writeInt(this.mTimeUncertainity);
        dest.writeInt(this.mClockFrequencyBias);
        dest.writeInt(this.mClockFrequencyBiasUncertainity);
    }
}
