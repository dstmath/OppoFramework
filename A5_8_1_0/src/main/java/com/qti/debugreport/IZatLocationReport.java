package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatLocationReport implements Parcelable {
    public static final Creator<IZatLocationReport> CREATOR = new Creator<IZatLocationReport>() {
        public IZatLocationReport createFromParcel(Parcel source) {
            return new IZatLocationReport(source);
        }

        public IZatLocationReport[] newArray(int size) {
            return new IZatLocationReport[size];
        }
    };
    private static final int HAS_HORIZONTAL_COMPONENT = 1;
    private static final int HAS_SOURCE = 4;
    private static final int HAS_VERTICAL_COMPONENT = 2;
    private static String TAG = "IZatLocationReport";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private float mAccuracy;
    private double mAltitude;
    private float mAltitudeUncertainity;
    private double mLatitude;
    private double mLongitude;
    private IzatLocationSource mSource;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;
    private int mValidityBit;

    public enum IzatLocationSource {
        POSITION_SOURCE_UNKNOWN(0),
        POSITION_SOURCE_CPI(1),
        POSITION_SOURCE_REFERENCE_LOCATION(2),
        POSITION_SOURCE_TLE(3);
        
        private final int mValue;

        private IzatLocationSource(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public IZatLocationReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int validityMask, double lat, double lon, float horzAccuracy, double alt, float altUnc, int source) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mValidityBit = validityMask;
        if ((this.mValidityBit & 1) != 0) {
            this.mLatitude = lat;
            this.mLongitude = lon;
            this.mAccuracy = horzAccuracy;
        }
        if ((this.mValidityBit & 2) != 0) {
            this.mAltitude = alt;
            this.mAltitudeUncertainity = altUnc;
        }
        if ((this.mValidityBit & 4) != 0) {
            try {
                this.mSource = IzatLocationSource.values()[source];
            } catch (ArrayIndexOutOfBoundsException e) {
                this.mSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
            }
        }
    }

    public IZatLocationReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mValidityBit = source.readInt();
        if ((this.mValidityBit & 1) != 0) {
            this.mLatitude = source.readDouble();
            this.mLongitude = source.readDouble();
            this.mAccuracy = source.readFloat();
        }
        if ((this.mValidityBit & 2) != 0) {
            this.mAltitude = source.readDouble();
            this.mAltitudeUncertainity = source.readFloat();
        }
        if ((this.mValidityBit & 4) != 0) {
            try {
                this.mSource = IzatLocationSource.values()[source.readInt()];
            } catch (ArrayIndexOutOfBoundsException e) {
                this.mSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
            }
        }
    }

    public boolean hasHorizontalFix() {
        return (this.mValidityBit & 1) != 0;
    }

    public boolean hasVerticalFix() {
        return (this.mValidityBit & 2) != 0;
    }

    public boolean hasSource() {
        return (this.mValidityBit & 4) != 0;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public double getAltitude() {
        return this.mAltitude;
    }

    public float getAccuracy() {
        return this.mAccuracy;
    }

    public float getAltitudeUncertainity() {
        return this.mAltitudeUncertainity;
    }

    public IzatLocationSource getSource() {
        return this.mSource;
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
        dest.writeInt(this.mValidityBit);
        if ((this.mValidityBit & 1) != 0) {
            dest.writeDouble(this.mLatitude);
            dest.writeDouble(this.mLongitude);
            dest.writeFloat(this.mAccuracy);
        }
        if ((this.mValidityBit & 2) != 0) {
            dest.writeDouble(this.mAltitude);
            dest.writeFloat(this.mAltitudeUncertainity);
        }
        if ((this.mValidityBit & 4) != 0) {
            dest.writeInt(this.mSource.getValue());
        }
    }
}
